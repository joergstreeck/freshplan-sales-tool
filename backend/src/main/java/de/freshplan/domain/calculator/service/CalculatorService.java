package de.freshplan.domain.calculator.service;

import de.freshplan.domain.calculator.service.dto.CalculatorRequest;
import de.freshplan.domain.calculator.service.dto.CalculatorResponse;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.List;

/**
 * Service for discount calculations based on FreshPlan business rules.
 */
@ApplicationScoped
public class CalculatorService {
    
    // Business rules from legacy configuration
    private static final List<DiscountRule> BASE_RULES = Arrays.asList(
        new DiscountRule(50000, 10),
        new DiscountRule(30000, 8),
        new DiscountRule(15000, 6),
        new DiscountRule(10000, 4),
        new DiscountRule(5000, 3),
        new DiscountRule(0, 0)
    );
    
    private static final List<EarlyBookingRule> EARLY_BOOKING_RULES = Arrays.asList(
        new EarlyBookingRule(30, 3),
        new EarlyBookingRule(21, 2),
        new EarlyBookingRule(14, 1),
        new EarlyBookingRule(0, 0)
    );
    
    private static final double PICKUP_DISCOUNT = 2.0;
    private static final double MAX_TOTAL_DISCOUNT = 15.0;
    
    /**
     * Calculate discount based on input parameters.
     */
    public CalculatorResponse calculate(CalculatorRequest request) {
        // Calculate individual discounts
        double baseDiscount = calculateBaseDiscount(request.getOrderValue());
        double earlyDiscount = calculateEarlyBookingDiscount(request.getLeadTime());
        double pickupDiscount = request.getPickup() ? PICKUP_DISCOUNT : 0;
        double chainDiscount = 0; // Chain discount logic can be added later
        
        // Calculate total discount (capped at maximum)
        double totalDiscount = Math.min(
            baseDiscount + earlyDiscount + pickupDiscount + chainDiscount,
            MAX_TOTAL_DISCOUNT
        );
        
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