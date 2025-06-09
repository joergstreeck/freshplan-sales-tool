package de.freshplan.domain.calculator.service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for calculator discount calculations.
 */
public class CalculatorRequest {
    
    @NotNull(message = "Bestellwert ist erforderlich")
    @Min(value = 0, message = "Bestellwert muss mindestens 0€ sein")
    @Max(value = 1000000, message = "Bestellwert darf maximal 1.000.000€ sein")
    private Double orderValue;
    
    @NotNull(message = "Vorlaufzeit ist erforderlich")
    @Min(value = 0, message = "Vorlaufzeit muss mindestens 0 Tage sein")
    @Max(value = 365, message = "Vorlaufzeit darf maximal 365 Tage sein")
    private Integer leadTime;
    
    @NotNull(message = "Abholung ist erforderlich")
    private Boolean pickup;
    
    @NotNull(message = "Kette ist erforderlich")
    private Boolean chain;
    
    // Constructors
    public CalculatorRequest() {}
    
    public CalculatorRequest(Double orderValue, Integer leadTime, Boolean pickup, Boolean chain) {
        this.orderValue = orderValue;
        this.leadTime = leadTime;
        this.pickup = pickup;
        this.chain = chain;
    }
    
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
    
    public Boolean getPickup() {
        return pickup;
    }
    
    public void setPickup(Boolean pickup) {
        this.pickup = pickup;
    }
    
    public Boolean getChain() {
        return chain;
    }
    
    public void setChain(Boolean chain) {
        this.chain = chain;
    }
}