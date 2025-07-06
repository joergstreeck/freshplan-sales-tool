package de.freshplan.domain.calculator.service;

import de.freshplan.domain.calculator.service.dto.CalculatorRequest;
import de.freshplan.domain.calculator.service.dto.CalculatorResponse;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.List;

/** Service for discount calculations based on FreshPlan business rules. */
@ApplicationScoped
public class CalculatorService {

  // Business rules from FreshPlan documentation (freshplan_summary.md)
  // Basisrabatt je Einzelbestellung (netto)
  private static final List<DiscountRule> BASE_RULES =
      Arrays.asList(
          new DiscountRule(75000, 10), // ab 75.000 EUR: 10%
          new DiscountRule(50000, 9), // 50.000 - 74.999 EUR: 9%
          new DiscountRule(30000, 8), // 30.000 - 49.999 EUR: 8%
          new DiscountRule(15000, 6), // 15.000 - 29.999 EUR: 6%
          new DiscountRule(5000, 3), // 5.000 - 14.999 EUR: 3%
          new DiscountRule(0, 0) // unter 5.000 EUR: 0%
          );

  // Frühbucherrabatt (zusätzlich) - korrigiert: ab 30 Tage nach oben offen
  private static final List<EarlyBookingRule> EARLY_BOOKING_RULES =
      Arrays.asList(
          new EarlyBookingRule(30, 3), // ab 30 Tage: +3%
          new EarlyBookingRule(15, 2), // 15 - 29 Tage: +2%
          new EarlyBookingRule(10, 1), // 10 - 14 Tage: +1%
          new EarlyBookingRule(0, 0) // unter 10 Tage: 0%
          );

  private static final double PICKUP_DISCOUNT = 2.0;
  // Mindestbestellwert für Abholrabatt
  private static final double PICKUP_MIN_ORDER_VALUE = 5000.0;
  private static final double MAX_TOTAL_DISCOUNT = 15.0;

  /** Calculate discount based on input parameters. */
  public CalculatorResponse calculate(CalculatorRequest request) {
    // Calculate individual discounts
    double baseDiscount = calculateBaseDiscount(request.getOrderValue());
    double earlyDiscount = calculateEarlyBookingDiscount(request.getLeadTime());
    // Abholrabatt nur ab Mindestbestellwert von 5.000 EUR
    double pickupDiscount = calculatePickupDiscount(request.getPickup(), request.getOrderValue());
    double chainDiscount = 0; // Chain discount logic can be added later

    // Calculate total discount (capped at maximum)
    double totalDiscount =
        Math.min(baseDiscount + earlyDiscount + pickupDiscount + chainDiscount, MAX_TOTAL_DISCOUNT);

    // Calculate amounts
    double discountAmount = request.getOrderValue() * (totalDiscount / 100);
    double finalPrice = request.getOrderValue() - discountAmount;
    double savingsAmount = discountAmount; // Same as discount amount for now

    return CalculatorResponse.builder()
        .orderValue(request.getOrderValue())
        .leadTime(request.getLeadTime())
        .pickup(request.getPickup())
        .chain(request.getChain())
        .baseDiscount(baseDiscount)
        .earlyDiscount(earlyDiscount)
        .pickupDiscount(pickupDiscount)
        .chainDiscount(chainDiscount)
        .totalDiscount(totalDiscount)
        .discountAmount(discountAmount)
        .savingsAmount(savingsAmount)
        .finalPrice(finalPrice)
        .build();
  }

  private double calculateBaseDiscount(double orderValue) {
    for (DiscountRule rule : BASE_RULES) {
      if (orderValue >= rule.minAmount) {
        return rule.discount;
      }
    }
    return 0;
  }

  private double calculateEarlyBookingDiscount(int leadTime) {
    for (EarlyBookingRule rule : EARLY_BOOKING_RULES) {
      if (leadTime >= rule.days) {
        return rule.discount;
      }
    }
    return 0;
  }

  private double calculatePickupDiscount(boolean pickup, double orderValue) {
    if (pickup && orderValue >= PICKUP_MIN_ORDER_VALUE) {
      return PICKUP_DISCOUNT;
    }
    return 0;
  }

  // Inner classes for rules
  private static class DiscountRule {
    final double minAmount;
    final double discount;

    DiscountRule(double minAmount, double discount) {
      this.minAmount = minAmount;
      this.discount = discount;
    }
  }

  private static class EarlyBookingRule {
    final int days;
    final double discount;

    EarlyBookingRule(int days, double discount) {
      this.days = days;
      this.discount = discount;
    }
  }
}
