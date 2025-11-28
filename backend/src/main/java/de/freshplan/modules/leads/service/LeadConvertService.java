package de.freshplan.modules.leads.service;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.util.CustomerNumberGeneratorService;
import de.freshplan.modules.leads.api.admin.dto.LeadConvertRequest;
import de.freshplan.modules.leads.api.admin.dto.LeadConvertResponse;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.time.Clock;
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

  // Sprint 2.1.7 Issue #127: Clock Injection Standard
  // Clock für testbare Zeit-Logik (injected via ClockProvider)
  @Inject Clock clock;

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

    // Sprint 2.1.7.4: Set PROSPECT (not AKTIV!) - waiting for first order
    customer.setStatus(CustomerStatus.PROSPECT);

    customer.setOriginalLeadId(leadId); // Track Lead → Customer conversion (V261)

    // Copy business fields using helper methods (PMD Complexity Refactoring - Issue #146)
    copyClassificationFields(lead, customer);
    copyPainScoringFields(lead, customer);

    // Audit fields
    customer.setCreatedBy(currentUserId);
    customer.setCreatedAt(LocalDateTime.now(clock));
    customer.setUpdatedBy(currentUserId);
    customer.setUpdatedAt(LocalDateTime.now(clock));

    // 6. Persist Customer (required before creating related entities)
    customerRepository.persist(customer);

    // 7. Create CustomerLocation and Address (helper method for complexity reduction)
    createLocationAndAddress(lead, customer, currentUserId);

    // 8. Copy contacts (Multi-Contact Migration or Legacy Fallback)
    if (lead.contacts != null && !lead.contacts.isEmpty()) {
      copyLeadContacts(lead, customer, currentUserId);
    } else {
      createFallbackContact(lead, customer, currentUserId);
    }

    // 10. Archive Lead (ALWAYS keep for audit trail)
    // Fix Sprint 2.1.6 Phase 2: Never hard-delete, always archive with status=CONVERTED
    // Hard deletion only for DSGVO compliance (separate Pseudonymization Job in Phase 3)
    lead.status = LeadStatus.CONVERTED;
    lead.updatedAt = LocalDateTime.now(clock);
    lead.updatedBy = currentUserId;
    lead.persist();

    if (request.keepLeadRecord) {
      Log.infof("Lead %d marked as CONVERTED (record retained for audit)", leadId);
    } else {
      Log.infof(
          "Lead %d marked as CONVERTED (keepLeadRecord=false ignored - audit trail preserved)",
          leadId);
    }

    // 11. Audit log
    Log.infof(
        "AUDIT: lead_converted_to_customer - leadId=%d, customerId=%s, customerNumber=%s, user=%s",
        leadId, customer.getId(), customerNumber, currentUserId);

    // 12. Return response
    return LeadConvertResponse.success(
        leadId, customer.getId(), customerNumber, LocalDateTime.now(clock));
  }

  // ============================================================================
  // HELPER METHODS - PMD Complexity Refactoring (Issue #146)
  // ============================================================================

  /** Copy classification fields from Lead to Customer. */
  private void copyClassificationFields(Lead lead, Customer customer) {
    if (lead.businessType != null) {
      customer.setBusinessType(lead.businessType);
    }
    if (lead.kitchenSize != null) {
      customer.setKitchenSize(lead.kitchenSize);
    }
    if (lead.employeeCount != null) {
      customer.setEmployeeCount(lead.employeeCount);
    }
    if (lead.branchCount != null) {
      customer.setBranchCount(lead.branchCount);
      customer.setTotalLocationsEU(lead.branchCount);
    }
    if (lead.isChain != null) {
      customer.setIsChain(lead.isChain);
    }
    if (lead.estimatedVolume != null) {
      customer.setEstimatedVolume(lead.estimatedVolume);
      customer.setExpectedAnnualVolume(lead.estimatedVolume);
    }
  }

  /** Copy Pain Scoring System V3 fields from Lead to Customer (8 Boolean fields + notes). */
  private void copyPainScoringFields(Lead lead, Customer customer) {
    if (lead.painStaffShortage != null) customer.setPainStaffShortage(lead.painStaffShortage);
    if (lead.painHighCosts != null) customer.setPainHighCosts(lead.painHighCosts);
    if (lead.painFoodWaste != null) customer.setPainFoodWaste(lead.painFoodWaste);
    if (lead.painQualityInconsistency != null)
      customer.setPainQualityInconsistency(lead.painQualityInconsistency);
    if (lead.painTimePressure != null) customer.setPainTimePressure(lead.painTimePressure);
    if (lead.painSupplierQuality != null) customer.setPainSupplierQuality(lead.painSupplierQuality);
    if (lead.painUnreliableDelivery != null)
      customer.setPainUnreliableDelivery(lead.painUnreliableDelivery);
    if (lead.painPoorService != null) customer.setPainPoorService(lead.painPoorService);
    if (lead.painNotes != null) customer.setPainNotes(lead.painNotes);
  }

  /** Create CustomerLocation and CustomerAddress from Lead data. */
  private void createLocationAndAddress(Lead lead, Customer customer, String currentUserId) {
    if (lead.city == null && lead.street == null) {
      return;
    }

    CustomerLocation mainLocation = new CustomerLocation();
    mainLocation.setCustomer(customer);
    mainLocation.setLocationName(lead.companyName + " - Hauptsitz");
    mainLocation.setIsMainLocation(true);
    mainLocation.setCategory(LocationCategory.HEADQUARTERS);
    if (lead.phone != null) {
      mainLocation.setPhone(lead.phone);
    }
    mainLocation.setCreatedBy(currentUserId);
    mainLocation.setCreatedAt(LocalDateTime.now(clock));
    mainLocation.setUpdatedBy(currentUserId);
    mainLocation.setUpdatedAt(LocalDateTime.now(clock));
    mainLocation.persist();

    if (lead.city != null) {
      CustomerAddress address = new CustomerAddress();
      address.setLocation(mainLocation);
      address.setAddressType(AddressType.BILLING);
      address.setStreet(lead.street != null ? lead.street : "");
      address.setPostalCode(lead.postalCode);
      address.setCity(lead.city);
      address.setCountry(mapCountryCode(lead.countryCode));
      address.setIsPrimaryForType(true);
      address.setCreatedBy(currentUserId);
      address.setCreatedAt(LocalDateTime.now(clock));
      address.setUpdatedBy(currentUserId);
      address.setUpdatedAt(LocalDateTime.now(clock));
      address.persist();
    }
  }

  /** Copy all LeadContacts to CustomerContacts (Multi-Contact Migration). */
  private void copyLeadContacts(Lead lead, Customer customer, String currentUserId) {
    for (de.freshplan.modules.leads.domain.LeadContact leadContact : lead.contacts) {
      CustomerContact customerContact = new CustomerContact();
      customerContact.setCustomer(customer);
      customerContact.setSalutation(leadContact.getSalutation());
      customerContact.setTitle(leadContact.getTitle());
      customerContact.setFirstName(leadContact.getFirstName());
      customerContact.setLastName(leadContact.getLastName());
      customerContact.setPosition(leadContact.getPosition());
      customerContact.setDecisionLevel(leadContact.getDecisionLevel());
      customerContact.setEmail(leadContact.getEmail());
      customerContact.setPhone(leadContact.getPhone());
      customerContact.setMobile(leadContact.getMobile());
      customerContact.setIsPrimary(leadContact.isPrimary());
      customerContact.setIsActive(leadContact.isActive());
      customerContact.setIsDecisionMaker(leadContact.getIsDecisionMaker());
      customerContact.setBirthday(leadContact.getBirthday());
      customerContact.setHobbies(leadContact.getHobbies());
      customerContact.setFamilyStatus(leadContact.getFamilyStatus());
      customerContact.setChildrenCount(leadContact.getChildrenCount());
      customerContact.setPersonalNotes(leadContact.getPersonalNotes());
      customerContact.setWarmthScore(leadContact.getWarmthScore());
      customerContact.setWarmthConfidence(leadContact.getWarmthConfidence());
      customerContact.setDataQualityScore(leadContact.getDataQualityScore());
      customerContact.setDataQualityRecommendations(leadContact.getDataQualityRecommendations());
      customerContact.setLastInteractionDate(leadContact.getLastInteractionDate());
      customerContact.setCreatedBy(currentUserId);
      customerContact.setCreatedAt(LocalDateTime.now(clock));
      customerContact.setUpdatedBy(currentUserId);
      customerContact.setUpdatedAt(LocalDateTime.now(clock));
      customerContact.persist();
    }
    Log.infof(
        "Copied %d contacts from Lead %d to Customer %s",
        lead.contacts.size(), lead.id, customer.getId());
  }

  /** Create fallback contact from legacy Lead fields (single-field contact). */
  private void createFallbackContact(Lead lead, Customer customer, String currentUserId) {
    if (lead.contactPerson == null && lead.email == null) {
      return;
    }

    CustomerContact contact = new CustomerContact();
    contact.setCustomer(customer);

    if (lead.contactPerson != null && !lead.contactPerson.isBlank()) {
      String[] nameParts = lead.contactPerson.trim().split("\\s+", 2);
      contact.setFirstName(nameParts[0]);
      contact.setLastName(nameParts.length > 1 ? nameParts[1] : "");
    } else {
      contact.setFirstName("Ansprechpartner");
      contact.setLastName("Nicht angegeben");
    }

    contact.setEmail(lead.email);
    contact.setPhone(lead.phone);
    contact.setIsPrimary(true);
    contact.setCreatedBy(currentUserId);
    contact.setCreatedAt(LocalDateTime.now(clock));
    contact.setUpdatedBy(currentUserId);
    contact.setUpdatedAt(LocalDateTime.now(clock));
    contact.persist();

    Log.infof(
        "Created fallback contact from Lead single-fields (leadId=%d, customerId=%s)",
        lead.id, customer.getId());
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
