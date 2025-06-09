package de.freshplan.calculator.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for calculator endpoint.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class CalculatorRequest {
    
    @NotNull(message = "Bestellwert ist erforderlich")
    @Min(value = 0, message = "Bestellwert muss positiv sein")
    private Double orderValue;
    
    @NotNull(message = "Vorlaufzeit ist erforderlich")
    @Min(value = 0, message = "Vorlaufzeit muss positiv sein")
    private Integer leadTime;
    
    @NotNull(message = "Abholung ist erforderlich")
    private Boolean pickup;
    
    // Getters and Setters
    public Double getOrderValue() {
        return orderValue;
    }
    
    public void setOrderValue(Double orderValue) {
        this.orderValue = orderValue;
    }
    
    public Integer getLeadTime() {
        return leadTime;
    }
    
    public void setLeadTime(Integer leadTime) {
        this.leadTime = leadTime;
    }
    
    public Boolean isPickup() {
        return pickup;
    }
    
    public void setPickup(Boolean pickup) {
        this.pickup = pickup;
    }
}