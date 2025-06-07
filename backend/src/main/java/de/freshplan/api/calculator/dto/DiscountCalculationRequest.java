package de.freshplan.api.calculator.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request DTO for discount calculation API.
 * 
 * Represents the input for calculating FreshPlan B2B wholesale discounts.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public final class DiscountCalculationRequest {
    
    @NotNull(message = "Order value cannot be null")
    @DecimalMin(value = "0.01", message = "Order value must be positive")
    private final BigDecimal orderValueNet;
    
    @NotNull(message = "Lead time days cannot be null")
    @Min(value = 0, message = "Lead time days cannot be negative")
    private final Integer leadTimeDays;
    
    @NotNull(message = "Self pickup flag cannot be null")
    private final Boolean selfPickup;
    
    /**
     * Creates a discount calculation request.
     * 
     * @param orderValueNet net order value in EUR
     * @param leadTimeDays days between order and delivery
     * @param selfPickup true if customer picks up order
     */
    @JsonCreator
    public DiscountCalculationRequest(
            @JsonProperty("orderValueNet") BigDecimal orderValueNet, 
            @JsonProperty("leadTimeDays") Integer leadTimeDays, 
            @JsonProperty("selfPickup") Boolean selfPickup) {
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
            "DiscountCalculationRequest{orderValue=%s, leadTime=%s, selfPickup=%s}",
            orderValueNet != null ? String.format("€%.2f", orderValueNet) : "null",
            leadTimeDays != null ? leadTimeDays + " days" : "null",
            selfPickup
        );
    }
}