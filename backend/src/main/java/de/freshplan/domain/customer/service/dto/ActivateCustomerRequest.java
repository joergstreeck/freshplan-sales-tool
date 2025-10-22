package de.freshplan.domain.customer.service.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for activating a PROSPECT customer to AKTIV status.
 *
 * <p>Sprint 2.1.7.4: Manual Activation Button
 *
 * <p>Business Rule: PROSPECT → AKTIV when first order delivered
 *
 * @param orderNumber Optional order number for audit trail
 */
public record ActivateCustomerRequest(
    @Size(max = 50, message = "Order number must not exceed 50 characters") String orderNumber) {

  /**
   * Factory method for better readability.
   *
   * @param orderNumber Order number
   * @return ActivateCustomerRequest instance
   */
  public static ActivateCustomerRequest of(String orderNumber) {
    return new ActivateCustomerRequest(orderNumber);
  }

  /**
   * Factory method without order number.
   *
   * @return ActivateCustomerRequest instance with null orderNumber
   */
  public static ActivateCustomerRequest withoutOrderNumber() {
    return new ActivateCustomerRequest(null);
  }
}
