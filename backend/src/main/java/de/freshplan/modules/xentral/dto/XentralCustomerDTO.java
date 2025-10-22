package de.freshplan.modules.xentral.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Xentral Customer DTO
 *
 * <p>Sprint 2.1.7.2: Xentral Integration
 *
 * <p>Represents a customer from Xentral ERP system (Neue Xentral API v25.39+)
 *
 * <p>Mapped from JSON:API response: GET /api/customers/{id}
 *
 * @param xentralId Customer ID in Xentral (not FreshPlan UUID!)
 * @param companyName Company name
 * @param email Primary email
 * @param phone Primary phone
 * @param totalRevenue Total revenue (all-time) - 2025 Feature in v25.39+
 * @param averageDaysToPay Average payment days (for payment behavior calculation)
 * @param lastOrderDate Last order date (for churn detection)
 * @param salesRepId Xentral Employee ID (Sales Rep)
 * @author FreshPlan Team
 * @since 2.0.0
 */
public record XentralCustomerDTO(
    String xentralId,
    String companyName,
    String email,
    String phone,
    BigDecimal totalRevenue,
    Integer averageDaysToPay,
    LocalDate lastOrderDate,
    String salesRepId) {

  /**
   * Validates the DTO
   *
   * @throws IllegalArgumentException if required fields are missing
   */
  public void validate() {
    if (xentralId == null || xentralId.isBlank()) {
      throw new IllegalArgumentException("xentralId cannot be null or blank");
    }
    if (companyName == null || companyName.isBlank()) {
      throw new IllegalArgumentException("companyName cannot be null or blank");
    }
  }
}
