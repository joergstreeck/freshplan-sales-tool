package de.freshplan.domain.calculator.service;

import de.freshplan.domain.calculator.service.dto.CalculatorRequest;
import de.freshplan.domain.calculator.service.dto.CalculatorResponse;
import de.freshplan.shared.constants.CalculatorConstants;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Service for discount calculations based on FreshPlan business rules. */
@ApplicationScoped
public class CalculatorService {

  private static final Logger log = LoggerFactory.getLogger(CalculatorService.class);

  // Business rules from FreshPlan documentation (freshplan_summary.md)
  // Basisrabatt je Einzelbestellung (netto)
  private static final List<DiscountRule> BASE_RULES =
      Arrays.asList(
          new DiscountRule(
              (int) CalculatorConstants.VOLUME_THRESHOLD_TIER_1,
              (int) CalculatorConstants.VOLUME_DISCOUNT_TIER_1), // ab 75.000 EUR: 10%
          new DiscountRule(
              (int) CalculatorConstants.VOLUME_THRESHOLD_TIER_2,
              (int) CalculatorConstants.VOLUME_DISCOUNT_TIER_2), // 50.000 - 74.999 EUR: 9%
          new DiscountRule(
              (int) CalculatorConstants.VOLUME_THRESHOLD_TIER_3,
              (int) CalculatorConstants.VOLUME_DISCOUNT_TIER_3), // 30.000 - 49.999 EUR: 8%
          new DiscountRule(
              (int) CalculatorConstants.VOLUME_THRESHOLD_TIER_4,
              (int) CalculatorConstants.VOLUME_DISCOUNT_TIER_4), // 15.000 - 29.999 EUR: 6%
          new DiscountRule(
              (int) CalculatorConstants.VOLUME_THRESHOLD_TIER_5,
              (int) CalculatorConstants.VOLUME_DISCOUNT_TIER_5), // 5.000 - 14.999 EUR: 3%
          new DiscountRule(0, 0) // unter 5.000 EUR: 0%
          );

  // Frühbucherrabatt (zusätzlich) - korrigiert: ab 30 Tage nach oben offen
  private static final List<EarlyBookingRule> EARLY_BOOKING_RULES =
      Arrays.asList(
          new EarlyBookingRule(
              CalculatorConstants.EARLY_BOOKING_DAYS_TIER_1,
              (int) CalculatorConstants.EARLY_BOOKING_DISCOUNT_TIER_1), // ab 30 Tage: +3%
          new EarlyBookingRule(
              CalculatorConstants.EARLY_BOOKING_DAYS_TIER_2,
              (int) CalculatorConstants.EARLY_BOOKING_DISCOUNT_TIER_2), // 15 - 29 Tage: +2%
          new EarlyBookingRule(
              CalculatorConstants.EARLY_BOOKING_DAYS_TIER_3,
              (int) CalculatorConstants.EARLY_BOOKING_DISCOUNT_TIER_3), // 10 - 14 Tage: +1%
          new EarlyBookingRule(0, 0) // unter 10 Tage: 0%
          );

  private static final double PICKUP_DISCOUNT = CalculatorConstants.PICKUP_DISCOUNT_PERCENTAGE;
  // Mindestbestellwert für Abholrabatt
  private static final double PICKUP_MIN_ORDER_VALUE =
      CalculatorConstants.PICKUP_MINIMUM_ORDER_VALUE;
  private static final double MAX_TOTAL_DISCOUNT =
      CalculatorConstants.MAX_TOTAL_DISCOUNT_PERCENTAGE;

  /** Calculate discount based on input parameters. */
  public CalculatorResponse calculate(CalculatorRequest request) {
    // Defensive validation
    if (request == null) {
      throw new IllegalArgumentException("CalculatorRequest cannot be null");
    }
    if (request.getOrderValue() < 0) {
      throw new IllegalArgumentException("Order value cannot be negative");
    }
    if (request.getLeadTime() < 0) {
      throw new IllegalArgumentException("Lead time cannot be negative");
    }

    log.debug(
        "Calculating discount for order value: {}, lead time: {} days, pickup: {}, chain: {}",
        request.getOrderValue(),
        request.getLeadTime(),
        request.getPickup(),
        request.getChain());
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

    CalculatorResponse response =
        CalculatorResponse.builder()
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

    log.info(
        "Discount calculation completed - Order: {}€, Total discount: {}%, Final price: {}€",
        request.getOrderValue(), totalDiscount, finalPrice);

    return response;
  }

  private double calculateBaseDiscount(double orderValue) {
    log.trace("Calculating base discount for order value: {}€", orderValue);
    for (DiscountRule rule : BASE_RULES) {
      if (orderValue >= rule.minAmount) {
        return rule.discount;
      }
    }
    return 0;
  }

  private double calculateEarlyBookingDiscount(int leadTime) {
    log.trace("Calculating early booking discount for {} days lead time", leadTime);
    for (EarlyBookingRule rule : EARLY_BOOKING_RULES) {
      if (leadTime >= rule.days) {
        return rule.discount;
      }
    }
    return 0;
  }

  private double calculatePickupDiscount(boolean pickup, double orderValue) {
    log.trace("Calculating pickup discount - pickup: {}, order value: {}€", pickup, orderValue);
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
