package de.freshplan.modules.xentral.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Xentral Invoice DTO
 *
 * <p>Sprint 2.1.7.2: Xentral Integration
 *
 * <p>Represents an invoice from Xentral ERP system (Neue Xentral API v25.39+)
 *
 * <p>Mapped from JSON:API response: GET /api/invoices
 *
 * @param invoiceId Invoice ID in Xentral
 * @param invoiceNumber Invoice number (human-readable)
 * @param customerId Customer ID in Xentral (references XentralCustomerDTO.xentralId)
 * @param amount Invoice amount
 * @param invoiceDate Invoice date
 * @param dueDate Payment due date
 * @param paymentDate Actual payment date (null if not paid)
 * @param status Invoice status (OPEN, PAID, OVERDUE, etc.)
 * @author FreshPlan Team
 * @since 2.0.0
 */
public record XentralInvoiceDTO(
    String invoiceId,
    String invoiceNumber,
    String customerId,
    BigDecimal amount,
    LocalDate invoiceDate,
    LocalDate dueDate,
    LocalDate paymentDate,
    String status) {

  /**
   * Validates the DTO
   *
   * @throws IllegalArgumentException if required fields are missing
   */
  public void validate() {
    if (invoiceId == null || invoiceId.isBlank()) {
      throw new IllegalArgumentException("invoiceId cannot be null or blank");
    }
    if (customerId == null || customerId.isBlank()) {
      throw new IllegalArgumentException("customerId cannot be null or blank");
    }
    if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("amount must be >= 0");
    }
  }

  /**
   * Checks if invoice is paid
   *
   * @return true if paymentDate is not null
   */
  public boolean isPaid() {
    return paymentDate != null;
  }

  /**
   * Calculates days to pay (from invoice date to payment date)
   *
   * @return days to pay, or null if not paid yet
   */
  public Integer getDaysToPay() {
    if (paymentDate == null) {
      return null;
    }
    return (int) java.time.temporal.ChronoUnit.DAYS.between(invoiceDate, paymentDate);
  }
}
