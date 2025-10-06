package de.freshplan.modules.leads.service;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
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

    // NOTE: Address/Contact data mapping is intentionally simplified for Sprint 2.1.6
    // Full CustomerAddress/CustomerContact creation will be added in Sprint 2.1.7
    // For now, basic customer record is sufficient for Bestandsleads-Migration

    // 6. Persist Customer
    customerRepository.persist(customer);

    // 7. Update or soft-delete Lead
    if (request.keepLeadRecord) {
      lead.status = LeadStatus.CONVERTED;
      lead.updatedAt = LocalDateTime.now();
      lead.updatedBy = currentUserId;
      lead.persist();
      Log.infof("Lead %d marked as CONVERTED (record retained)", leadId);
    } else {
      lead.delete();
      Log.infof("Lead %d hard-deleted after conversion", leadId);
    }

    // 8. Audit log
    Log.infof(
        "AUDIT: lead_converted_to_customer - leadId=%d, customerId=%s, customerNumber=%s, user=%s",
        leadId, customer.getId(), customerNumber, currentUserId);

    // 9. Return response
    return LeadConvertResponse.success(
        leadId, customer.getId(), customerNumber, LocalDateTime.now());
  }
}
