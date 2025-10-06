package de.freshplan.modules.leads.api.admin.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response for Lead → Customer conversion.
 *
 * <p>Sprint 2.1.6 - User Story 2
 */
public class LeadConvertResponse {

  public Long leadId;
  public UUID customerId;
  public String customerNumber;
  public LocalDateTime convertedAt;
  public String message;

  public static LeadConvertResponse success(
      Long leadId, UUID customerId, String customerNumber, LocalDateTime convertedAt) {
    LeadConvertResponse response = new LeadConvertResponse();
    response.leadId = leadId;
    response.customerId = customerId;
    response.customerNumber = customerNumber;
    response.convertedAt = convertedAt;
    response.message =
        "Lead successfully converted to Customer " + customerNumber + " (ID: " + customerId + ")";
    return response;
  }
}
