package de.freshplan.modules.xentral.dto.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Xentral v1 API Invoice entity (from /api/v1/invoices).
 *
 * <p>Structure mirrors Xentral v25.40.2 v1 API response:
 *
 * <pre>
 * {
 *   "id": 123,
 *   "invoice_number": "RE-2024-00123",
 *   "customer_id": "456",
 *   "total": 1234.56,
 *   "invoice_date": "2024-10-15",
 *   "due_date": "2024-11-15",
 *   "status": "paid"
 * }
 * </pre>
 *
 * <p>Sprint: 2.1.7.2 - D2a Real API DTOs
 *
 * @see XentralV1InvoiceResponse
 * @see XentralV1InvoiceBalance
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record XentralV1Invoice(
    String id,
    @JsonProperty("invoice_number") String invoiceNumber,
    @JsonProperty("customer_id") String customerId,
    BigDecimal total,
    @JsonProperty("invoice_date") LocalDate invoiceDate,
    @JsonProperty("due_date") LocalDate dueDate,
    String status) {

  /**
   * Validates that the invoice has required fields.
   *
   * @throws IllegalArgumentException if id or customerId is missing
   */
  public void validate() {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("Invoice id cannot be null or blank");
    }
    if (customerId == null || customerId.isBlank()) {
      throw new IllegalArgumentException("Invoice customerId cannot be null or blank");
    }
  }

  /**
   * Checks if the invoice is overdue (due_date < today and status != 'paid').
   *
   * @return true if overdue, false otherwise
   */
  public boolean isOverdue() {
    if (dueDate == null || "paid".equalsIgnoreCase(status)) {
      return false;
    }
    return dueDate.isBefore(LocalDate.now());
  }

  /**
   * Checks if the invoice is paid.
   *
   * @return true if status is 'paid' or 'completed'
   */
  public boolean isPaid() {
    return "paid".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status);
  }
}
