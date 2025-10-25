package de.freshplan.domain.customer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Revenue Metrics DTO
 *
 * <p>Sprint 2.1.7.2: Customer Dashboard - Revenue Metrics from Xentral
 *
 * <p>Contains: - Revenue for 30/90/365 days - Payment behavior classification - Average days to pay
 * - Last order date (for churn detection)
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public record RevenueMetrics(
    BigDecimal revenue30Days,
    BigDecimal revenue90Days,
    BigDecimal revenue365Days,
    PaymentBehavior paymentBehavior,
    Integer averageDaysToPay,
    LocalDate lastOrderDate) {

  /**
   * Create empty metrics (for customers without Xentral-ID or no data)
   *
   * @return Empty metrics with all values set to zero/N_A
   */
  public static RevenueMetrics empty() {
    return new RevenueMetrics(
        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, PaymentBehavior.N_A, null, null);
  }

  /**
   * Check if customer has recent activity (for churn detection)
   *
   * @param churnThresholdDays Days of inactivity before customer is considered "at risk"
   * @return true if last order is older than threshold
   */
  public boolean isChurnRisk(int churnThresholdDays) {
    if (lastOrderDate == null) {
      return true; // No orders = churn risk
    }

    LocalDate threshold = LocalDate.now().minusDays(churnThresholdDays);
    return lastOrderDate.isBefore(threshold);
  }

  /**
   * Get days since last order
   *
   * @return Days since last order, or null if no last order date
   */
  public Integer getDaysSinceLastOrder() {
    if (lastOrderDate == null) {
      return null;
    }

    return (int) java.time.temporal.ChronoUnit.DAYS.between(lastOrderDate, LocalDate.now());
  }
}
