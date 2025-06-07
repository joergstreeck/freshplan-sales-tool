package de.freshplan.domain.calculator.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request for FreshPlan discount calculation.
 * 
 * Represents a food wholesale order calculation request.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public final class FreshPlanCalculationRequest {
    
    @NotNull(message = "Order value cannot be null")
    @DecimalMin(value = "0.01", message = "Order value must be positive")
    private final BigDecimal orderValueNet;
    
    @NotNull(message = "Lead time days cannot be null")
    @Min(value = 0, message = "Lead time days cannot be negative")
    private final Integer leadTimeDays;
    
    @NotNull(message = "Self pickup flag cannot be null")
    private final Boolean selfPickup;
    
    /**
     * Creates a FreshPlan calculation request.
     * 
     * @param orderValueNet net order value in EUR
     * @param leadTimeDays days between order and delivery
     * @param selfPickup true if customer picks up order
     */
    public FreshPlanCalculationRequest(BigDecimal orderValueNet, 
                                     Integer leadTimeDays, 
                                     Boolean selfPickup) {
        this.orderValueNet = orderValueNet;
        this.leadTimeDays = leadTimeDays;
        this.selfPickup = selfPickup;
    }
    
    public BigDecimal getOrderValueNet() {
        return orderValueNet;
    }
    
    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }
    
    public Boolean getSelfPickup() {
        return selfPickup;
    }
    
    @Override
    public String toString() {
        return String.format(
            "FreshPlanCalculationRequest{orderValue=€%.2f, leadTime=%d days, selfPickup=%s}",
            orderValueNet, leadTimeDays, selfPickup
        );
    }
}