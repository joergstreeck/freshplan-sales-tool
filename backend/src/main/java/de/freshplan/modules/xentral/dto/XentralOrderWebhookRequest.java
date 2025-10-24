package de.freshplan.modules.xentral.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Xentral Order Webhook Request DTO
 *
 * <p>Sprint 2.1.7.2: Xentral Webhook Integration
 *
 * <p>Represents the payload sent by Xentral ERP when an order is delivered.
 *
 * <p>Expected JSON structure:
 *
 * <pre>
 * {
 *   "xentralCustomerId": "12345",
 *   "orderNumber": "ORD-2025-001",
 *   "deliveryDate": "2025-01-24"
 * }
 * </pre>
 *
 * @param xentralCustomerId Customer ID in Xentral ERP system (required)
 * @param orderNumber Order number from Xentral (required)
 * @param deliveryDate Delivery date of the order (required)
 * @author FreshPlan Team
 * @since 2.0.0
 */
public record XentralOrderWebhookRequest(
    @NotBlank(message = "xentralCustomerId cannot be null or blank")
        String xentralCustomerId,
    @NotBlank(message = "orderNumber cannot be null or blank") String orderNumber,
    @NotNull(message = "deliveryDate cannot be null") LocalDate deliveryDate) {

  /**
   * Validates the DTO
   *
   * @throws IllegalArgumentException if required fields are missing or invalid
   */
  public void validate() {
    if (xentralCustomerId == null || xentralCustomerId.isBlank()) {
      throw new IllegalArgumentException("xentralCustomerId cannot be null or blank");
    }
    if (orderNumber == null || orderNumber.isBlank()) {
      throw new IllegalArgumentException("orderNumber cannot be null or blank");
    }
    if (deliveryDate == null) {
      throw new IllegalArgumentException("deliveryDate cannot be null");
    }
  }
}
