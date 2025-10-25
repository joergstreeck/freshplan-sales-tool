package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.dto.RevenueMetrics;
import de.freshplan.domain.customer.entity.Customer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Customer Health Score Service
 *
 * <p>Sprint 2.1.7.2 D11: Server-Driven Customer Cards
 *
 * <p>Calculates Health Score for customers based on 5 factors:
 *
 * <ol>
 *   <li>Order Recency (30%) - How long since last order?
 *   <li>Order Frequency (25%) - How often do they order?
 *   <li>Revenue Growth (20%) - Is revenue growing?
 *   <li>Communication (15%) - How engaged are they?
 *   <li>Payment Behavior (10%) - Do they pay on time?
 * </ol>
 *
 * <p><strong>Score Ranges:</strong>
 *
 * <ul>
 *   <li>🟢 80-100: Healthy
 *   <li>🟡 50-79: Watch (needs attention)
 *   <li>🔴 0-49: Risk (immediate action required!)
 * </ul>
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class CustomerHealthScoreService {

  private static final Logger log = LoggerFactory.getLogger(CustomerHealthScoreService.class);

  @Inject RevenueMetricsService revenueMetricsService;

  /**
   * Calculate Health Score for a customer
   *
   * <p>Formula: healthScore = ( orderRecencyScore * 0.30 + orderFrequencyScore * 0.25 +
   * revenueGrowthScore * 0.20 + communicationScore * 0.15 + paymentBehaviorScore * 0.10 ) * 100
   *
   * @param customer Customer entity
   * @return Health Score (0-100)
   */
  public int calculateHealthScore(Customer customer) {
    if (customer == null) {
      log.warn("Cannot calculate health score for null customer");
      return 0;
    }

    log.debug("Calculating health score for customer: {}", customer.getId());

    // Get revenue metrics (includes Xentral data if available)
    RevenueMetrics metrics = revenueMetricsService.getRevenueMetrics(customer.getId());

    // Calculate individual scores (0.0 - 1.0)
    double recencyScore = calculateOrderRecencyScore(metrics);
    double frequencyScore = calculateOrderFrequencyScore(customer);
    double growthScore = calculateRevenueGrowthScore(customer, metrics);
    double communicationScore = calculateCommunicationScore(customer);
    double paymentScore = calculatePaymentBehaviorScore(metrics);

    // Weighted average
    double healthScore =
        (recencyScore * 0.30)
            + (frequencyScore * 0.25)
            + (growthScore * 0.20)
            + (communicationScore * 0.15)
            + (paymentScore * 0.10);

    // Convert to 0-100 scale
    int finalScore = (int) Math.round(healthScore * 100);

    log.info(
        "Health score for customer {}: {} (recency:{}, freq:{}, growth:{}, comm:{}, payment:{})",
        customer.getId(),
        finalScore,
        Math.round(recencyScore * 100),
        Math.round(frequencyScore * 100),
        Math.round(growthScore * 100),
        Math.round(communicationScore * 100),
        Math.round(paymentScore * 100));

    return finalScore;
  }

  /**
   * Order Recency Score (30% weight)
   *
   * <p>How long since last order?
   *
   * <ul>
   *   <li>0-7 days: 1.0 (perfect)
   *   <li>8-30 days: 0.8 (good)
   *   <li>31-60 days: 0.5 (watch)
   *   <li>61-90 days: 0.2 (risk)
   *   <li>90+ days: 0.0 (critical)
   * </ul>
   */
  private double calculateOrderRecencyScore(RevenueMetrics metrics) {
    if (metrics.lastOrderDate() == null) {
      return 0.0; // No orders = critical
    }

    long daysSinceLastOrder = ChronoUnit.DAYS.between(metrics.lastOrderDate(), LocalDate.now());

    if (daysSinceLastOrder <= 7) return 1.0;
    if (daysSinceLastOrder <= 30) return 0.8;
    if (daysSinceLastOrder <= 60) return 0.5;
    if (daysSinceLastOrder <= 90) return 0.2;
    return 0.0;
  }

  /**
   * Order Frequency Score (25% weight)
   *
   * <p>How often do they order? (estimated from last order date)
   *
   * <p>Since we don't have order count yet, we estimate from last order date:
   *
   * <ul>
   *   <li>Orders in last 7 days: High frequency (1.0)
   *   <li>Orders in last 30 days: Medium frequency (0.7)
   *   <li>Orders in last 60 days: Low frequency (0.4)
   *   <li>No orders in 90+ days: No frequency (0.0)
   * </ul>
   */
  private double calculateOrderFrequencyScore(Customer customer) {
    if (customer.getLastOrderDate() == null) {
      return 0.0;
    }

    long daysSinceLastOrder = ChronoUnit.DAYS.between(customer.getLastOrderDate(), LocalDate.now());

    if (daysSinceLastOrder <= 7) return 1.0;
    if (daysSinceLastOrder <= 30) return 0.7;
    if (daysSinceLastOrder <= 60) return 0.4;
    return 0.0;
  }

  /**
   * Revenue Growth Score (20% weight)
   *
   * <p>Is revenue growing?
   *
   * <p>Compare expected vs actual annual volume:
   *
   * <ul>
   *   <li>Actual > Expected + 20%: Excellent growth (1.0)
   *   <li>Actual > Expected: Good growth (0.8)
   *   <li>Actual ≈ Expected (±10%): On track (0.6)
   *   <li>Actual < Expected - 10%: Below target (0.3)
   *   <li>Actual < Expected - 30%: Critical decline (0.0)
   * </ul>
   */
  private double calculateRevenueGrowthScore(Customer customer, RevenueMetrics metrics) {
    if (customer.getExpectedAnnualVolume() == null || customer.getActualAnnualVolume() == null) {
      return 0.5; // Neutral if no data
    }

    double expected = customer.getExpectedAnnualVolume().doubleValue();
    double actual = customer.getActualAnnualVolume().doubleValue();

    if (expected == 0) return 0.5; // Avoid division by zero

    double growthRate = (actual - expected) / expected;

    if (growthRate >= 0.20) return 1.0; // 20%+ growth
    if (growthRate >= 0.00) return 0.8; // Positive growth
    if (growthRate >= -0.10) return 0.6; // Within 10% of target
    if (growthRate >= -0.30) return 0.3; // Below target
    return 0.0; // Critical decline
  }

  /**
   * Communication Score (15% weight)
   *
   * <p>How engaged are they?
   *
   * <p>Based on last contact date:
   *
   * <ul>
   *   <li>Contact in last 7 days: Excellent (1.0)
   *   <li>Contact in last 14 days: Good (0.8)
   *   <li>Contact in last 30 days: OK (0.5)
   *   <li>No contact in 30+ days: Poor (0.2)
   * </ul>
   */
  private double calculateCommunicationScore(Customer customer) {
    if (customer.getLastContactDate() == null) {
      return 0.2; // No contact = poor engagement
    }

    long daysSinceLastContact =
        ChronoUnit.DAYS.between(customer.getLastContactDate(), LocalDate.now());

    if (daysSinceLastContact <= 7) return 1.0;
    if (daysSinceLastContact <= 14) return 0.8;
    if (daysSinceLastContact <= 30) return 0.5;
    return 0.2;
  }

  /**
   * Payment Behavior Score (10% weight)
   *
   * <p>Do they pay on time?
   *
   * <p>Based on PaymentBehavior from Xentral:
   *
   * <ul>
   *   <li>EXCELLENT: 1.0
   *   <li>GOOD: 0.8
   *   <li>WARNING: 0.5
   *   <li>CRITICAL: 0.0
   *   <li>N_A (no data): 0.5 (neutral)
   * </ul>
   */
  private double calculatePaymentBehaviorScore(RevenueMetrics metrics) {
    if (metrics.paymentBehavior() == null) {
      return 0.5; // Neutral if no data
    }

    return switch (metrics.paymentBehavior()) {
      case EXCELLENT -> 1.0;
      case GOOD -> 0.8;
      case WARNING -> 0.5;
      case CRITICAL -> 0.0;
      case N_A -> 0.5;
    };
  }

  /**
   * Get Health Score Label
   *
   * <p>Convert numeric score to human-readable label:
   *
   * <ul>
   *   <li>80-100: "Gesund" (Green)
   *   <li>50-79: "Beobachten" (Yellow)
   *   <li>0-49: "Risiko" (Red)
   * </ul>
   *
   * @param score Health score (0-100)
   * @return Label ("Gesund", "Beobachten", "Risiko")
   */
  public String getHealthScoreLabel(int score) {
    if (score >= 80) return "Gesund";
    if (score >= 50) return "Beobachten";
    return "Risiko";
  }

  /**
   * Get Health Score Color
   *
   * <p>For UI rendering:
   *
   * <ul>
   *   <li>80-100: "success" (Green)
   *   <li>50-79: "warning" (Yellow)
   *   <li>0-49: "error" (Red)
   * </ul>
   *
   * @param score Health score (0-100)
   * @return Color ("success", "warning", "error")
   */
  public String getHealthScoreColor(int score) {
    if (score >= 80) return "success";
    if (score >= 50) return "warning";
    return "error";
  }
}
