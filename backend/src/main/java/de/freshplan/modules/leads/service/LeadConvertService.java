package de.freshplan.modules.leads.service;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.CustomerNumberGeneratorService;
import de.freshplan.modules.leads.api.admin.dto.LeadConvertRequest;
import de.freshplan.modules.leads.api.admin.dto.LeadConvertResponse;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Service for converting Leads to Customers.
 *
 * <p>Handles the conversion workflow from Lead → Customer with optional Lead record retention for
 * audit purposes.
 *
 * <p>Sprint 2.1.6 - User Story 2 (Bestandsleads-Migration Phase 2)
 */
@ApplicationScoped
public class LeadConvertService {

  @Inject CustomerRepository customerRepository;

  @Inject CustomerNumberGeneratorService numberGenerator;

  /**
   * Convert a Lead to a Customer.
   *
   * @param leadId Lead ID to convert
   * @param request Conversion request with optional customer number and notes
   * @param currentUserId User performing the conversion
   * @return Conversion response with new Customer ID and number
   */
  @Transactional
  public LeadConvertResponse convertToCustomer(
      Long leadId, LeadConvertRequest request, String currentUserId) {

    Log.infof(
        "Converting lead %d to customer by user %s (keepLead=%b)",
        leadId, currentUserId, request.keepLeadRecord);

    // 1. Validate lead exists
    Lead lead = Lead.findById(leadId);
    if (lead == null) {
      throw new NotFoundException("Lead not found: " + leadId);
    }

    // 2. Validate lead is not already converted
    if (lead.status == LeadStatus.CONVERTED) {
      throw new IllegalStateException(
          "Lead " + leadId + " already converted (status: " + lead.status + ")");
    }

    // 3. Generate or use provided customer number
    String customerNumber =
        (request.customerNumber != null && !request.customerNumber.isBlank())
            ? request.customerNumber
            : numberGenerator.generateNext();

    // 4. Check for duplicate customer number
    if (customerRepository.findByCustomerNumber(customerNumber).isPresent()) {
      throw new IllegalArgumentException("Customer number already exists: " + customerNumber);
    }

    // 5. Create Customer from Lead data
    Customer customer = new Customer();
    customer.setCustomerNumber(customerNumber);
    customer.setCompanyName(lead.companyName);
    customer.setStatus(CustomerStatus.AKTIV); // New customer starts as AKTIV
    customer.setOriginalLeadId(leadId); // Track Lead → Customer conversion (V261)
    customer.setCreatedBy(currentUserId);
    customer.setCreatedAt(LocalDateTime.now());
    customer.setUpdatedBy(currentUserId);
    customer.setUpdatedAt(LocalDateTime.now());

    // 6. Persist Customer (required before creating related entities)
    customerRepository.persist(customer);

    // 7. Create CustomerLocation (Main Location)
    if (lead.city != null || lead.street != null) {
      CustomerLocation mainLocation = new CustomerLocation();
      mainLocation.setCustomer(customer);
      mainLocation.setLocationName(lead.companyName + " - Hauptsitz");
      mainLocation.setIsMainLocation(true);
      mainLocation.setCategory(LocationCategory.HEADQUARTERS);

      // Copy phone to location if available
      if (lead.phone != null) {
        mainLocation.setPhone(lead.phone);
      }

      mainLocation.setCreatedBy(currentUserId);
      mainLocation.setCreatedAt(LocalDateTime.now());
      mainLocation.setUpdatedBy(currentUserId);
      mainLocation.setUpdatedAt(LocalDateTime.now());
      mainLocation.persist();

      // 8. Create CustomerAddress (from Lead address data)
      if (lead.city != null) {
        CustomerAddress address = new CustomerAddress();
        address.setLocation(mainLocation);
        address.setAddressType(AddressType.BILLING);
        address.setStreet(lead.street != null ? lead.street : "");
        address.setPostalCode(lead.postalCode);
        address.setCity(lead.city);
        address.setCountry(mapCountryCode(lead.countryCode)); // ISO-3166-1 alpha-3
        address.setIsPrimaryForType(true);
        address.setCreatedBy(currentUserId);
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedBy(currentUserId);
        address.setUpdatedAt(LocalDateTime.now());
        address.persist();
      }
    }

    // 9. Create CustomerContact (from Lead contact data)
    if (lead.contactPerson != null || lead.email != null) {
      CustomerContact contact = new CustomerContact();
      contact.setCustomer(customer);

      // Parse contact person name (simple split: "Max Mustermann" → firstName/lastName)
      // TODO (Future Sprint - Issue #135): Improve name parsing robustness
      // Current logic fails for complex names (e.g., "Dr. Max von Mustermann", "Maria Anna Schmidt")
      // Consider using a name parsing library or more sophisticated logic for better data quality
      // For now, this simple split is acceptable for MVP (most German names: "FirstName LastName")
      if (lead.contactPerson != null && !lead.contactPerson.isBlank()) {
        String[] nameParts = lead.contactPerson.trim().split("\\s+", 2);
        contact.setFirstName(nameParts[0]);
        if (nameParts.length > 1) {
          contact.setLastName(nameParts[1]);
        } else {
          contact.setLastName(""); // Required field
        }
      } else {
        contact.setFirstName("Ansprechpartner");
        contact.setLastName("Nicht angegeben");
      }

      contact.setEmail(lead.email);
      contact.setPhone(lead.phone);
      contact.setIsPrimary(true);
      contact.setCreatedBy(currentUserId);
      contact.setCreatedAt(LocalDateTime.now());
      contact.setUpdatedBy(currentUserId);
      contact.setUpdatedAt(LocalDateTime.now());
      contact.persist();
    }

    // 10. Archive Lead (ALWAYS keep for audit trail)
    // Fix Sprint 2.1.6 Phase 2: Never hard-delete, always archive with status=CONVERTED
    // Hard deletion only for DSGVO compliance (separate Pseudonymization Job in Phase 3)
    lead.status = LeadStatus.CONVERTED;
    lead.updatedAt = LocalDateTime.now();
    lead.updatedBy = currentUserId;
    lead.persist();

    if (request.keepLeadRecord) {
      Log.infof("Lead %d marked as CONVERTED (record retained for audit)", leadId);
    } else {
      Log.infof("Lead %d marked as CONVERTED (keepLeadRecord=false ignored - audit trail preserved)", leadId);
    }

    // 11. Audit log
    Log.infof(
        "AUDIT: lead_converted_to_customer - leadId=%d, customerId=%s, customerNumber=%s, user=%s",
        leadId, customer.getId(), customerNumber, currentUserId);

    // 12. Return response
    return LeadConvertResponse.success(
        leadId, customer.getId(), customerNumber, LocalDateTime.now());
  }

  /**
   * Maps ISO 3166-1 alpha-2 country code (Lead) to alpha-3 (Customer).
   *
   * <p>Lead uses 2-letter codes ("DE", "AT", "CH"). Customer uses 3-letter codes ("DEU", "AUT",
   * "CHE"). Uses Java's built-in Locale for automatic conversion (supports 200+ countries).
   *
   * @param alpha2Code ISO 3166-1 alpha-2 country code (e.g. "DE")
   * @return ISO 3166-1 alpha-3 country code (e.g. "DEU")
   */
  private String mapCountryCode(String alpha2Code) {
    if (alpha2Code == null || alpha2Code.isBlank()) {
      return "DEU"; // Default: Germany
    }

    try {
      Locale locale = new Locale("", alpha2Code.toUpperCase());
      String iso3 = locale.getISO3Country();

      // Validation: Check if conversion worked (some invalid codes return empty)
      if (iso3 == null || iso3.isBlank()) {
        Log.warnf("Invalid country code: %s, using default DEU", alpha2Code);
        return "DEU";
      }

      return iso3;
    } catch (Exception e) {
      Log.warnf(e, "Failed to convert country code: %s, using default DEU", alpha2Code);
      return "DEU";
    }
  }
}
