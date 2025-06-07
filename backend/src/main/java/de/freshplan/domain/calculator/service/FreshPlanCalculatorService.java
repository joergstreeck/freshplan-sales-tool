package de.freshplan.domain.calculator.service;

import de.freshplan.domain.calculator.model.FreshPlanCalculationRequest;
import de.freshplan.domain.calculator.model.FreshPlanCalculationResult;

import java.math.BigDecimal;

/**
 * Core business logic for FreshPlan discount calculations.
 * 
 * Pure Java class implementing Freshfoodz GmbH business rules
 * for B2B food wholesale discount system.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class FreshPlanCalculatorService {
    
    /**
     * Minimum order value for any FreshPlan calculation.
     */
    private static final BigDecimal MIN_ORDER_VALUE = new BigDecimal("100.00");
    
    /**
     * Maximum order value for standard processing.
     */
    private static final BigDecimal MAX_STANDARD_ORDER_VALUE = new BigDecimal("100000.00");
    
    /**
     * Calculates FreshPlan discounts for given request.
     * 
     * Applies:
     * - Volume-based discount tiers (0% to 10%)
     * - Lead time discounts (1% to 3%)
     * - Self-pickup discount (2%, min €5,000 order)
     * - Maximum total discount: 15%
     * 
     * @param request the calculation request
     * @return calculated pricing result with all discounts
     * @throws IllegalArgumentException if request is invalid
     */
    public FreshPlanCalculationResult calculateDiscounts(FreshPlanCalculationRequest request) {
        validateRequest(request);
        
        return new FreshPlanCalculationResult(request);
    }
    
    /**
     * Validates the calculation request.
     * 
     * @param request the request to validate
     * @throws IllegalArgumentException if request is invalid
     */
    private void validateRequest(FreshPlanCalculationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Calculation request cannot be null");
        }
        
        if (request.getOrderValueNet() == null) {
            throw new IllegalArgumentException("Order value cannot be null");
        }
        
        if (request.getOrderValueNet().compareTo(MIN_ORDER_VALUE) < 0) {
            throw new IllegalArgumentException(
                String.format("Minimum order value is €%.2f", MIN_ORDER_VALUE)
            );
        }
        
        if (request.getOrderValueNet().compareTo(MAX_STANDARD_ORDER_VALUE) > 0) {
            throw new IllegalArgumentException(
                String.format("Order value exceeds maximum of €%.2f - contact sales", 
                             MAX_STANDARD_ORDER_VALUE)
            );
        }
        
        if (request.getLeadTimeDays() == null || request.getLeadTimeDays() < 0) {
            throw new IllegalArgumentException("Lead time days must be non-negative");
        }
        
        if (request.getSelfPickup() == null) {
            throw new IllegalArgumentException("Self pickup flag cannot be null");
        }
    }
    
    /**
     * Checks if an order qualifies for FreshPlan partnership discounts.
     * 
     * @param orderValue the net order value
     * @return true if order qualifies for discounts
     */
    public boolean isQualifyingOrder(BigDecimal orderValue) {
        return orderValue != null && orderValue.compareTo(MIN_ORDER_VALUE) >= 0;
    }
    
    /**
     * Gets the minimum order value for FreshPlan discounts.
     * 
     * @return minimum order value in EUR
     */
    public BigDecimal getMinimumOrderValue() {
        return MIN_ORDER_VALUE;
    }
}