package de.freshplan.modules.xentral.dto.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Xentral v1 API Invoice Balance entity (from /api/v1/invoices/{id}/balance).
 *
 * <p>Contains financial details about payment status:
 *
 * <pre>
 * {
 *   "total": 1234.56,
 *   "paid": 1234.56,
 *   "open": 0.00,
 *   "payment_date": "2024-10-20"
 * }
 * </pre>
 *
 * <p>Used to calculate financial metrics: - totalRevenue = SUM(paid) - averageDaysToPay =
 * AVG(paymentDate - invoiceDate)
 *
 * <p>Sprint: 2.1.7.2 - D2a Real API DTOs
 *
 * @see XentralV1Invoice
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record XentralV1InvoiceBalance(
    BigDecimal total,
    BigDecimal paid,
    BigDecimal open,
    @JsonProperty("payment_date") LocalDate paymentDate) {

  /**
   * Checks if the invoice is fully paid (paid == total).
   *
   * @return true if fully paid, false otherwise
   */
  public boolean isFullyPaid() {
    if (total == null || paid == null) {
      return false;
    }
    return paid.compareTo(total) >= 0;
  }

  /**
   * Checks if the invoice is partially paid (paid > 0 but < total).
   *
   * @return true if partially paid, false otherwise
   */
  public boolean isPartiallyPaid() {
    if (total == null || paid == null) {
      return false;
    }
    return paid.compareTo(BigDecimal.ZERO) > 0 && paid.compareTo(total) < 0;
  }

  /**
   * Gets the payment date if available.
   *
   * @return payment date or null if not yet paid
   */
  public LocalDate getPaymentDate() {
    return paymentDate;
  }

  /**
   * Gets the paid amount (defaults to 0 if null).
   *
   * @return paid amount
   */
  public BigDecimal getPaidAmount() {
    return paid != null ? paid : BigDecimal.ZERO;
  }

  /**
   * Gets the open amount (defaults to 0 if null).
   *
   * @return open amount
   */
  public BigDecimal getOpenAmount() {
    return open != null ? open : BigDecimal.ZERO;
  }
}
