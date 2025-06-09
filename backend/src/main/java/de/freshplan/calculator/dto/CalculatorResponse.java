package de.freshplan.calculator.dto;

/**
 * Response DTO for calculator endpoint.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class CalculatorResponse {
    
    private Double orderValue;
    private Integer leadTime;
    private Boolean pickup;
    private Double baseDiscount;
    private Double earlyDiscount;
    private Double pickupDiscount;
    private Double totalDiscount;
    private Double discountAmount;
    private Double finalPrice;
    
    // Private constructor for Builder
    private CalculatorResponse() {}
    
    // Getters
    public Double getOrderValue() {
        return orderValue;
    }
    
    public Integer getLeadTime() {
        return leadTime;
    }
    
    public Boolean isPickup() {
        return pickup;
    }
    
    public Double getBaseDiscount() {
        return baseDiscount;
    }
    
    public Double getEarlyDiscount() {
        return earlyDiscount;
    }
    
    public Double getPickupDiscount() {
        return pickupDiscount;
    }
    
    public Double getTotalDiscount() {
        return totalDiscount;
    }
    
    public Double getDiscountAmount() {
        return discountAmount;
    }
    
    public Double getFinalPrice() {
        return finalPrice;
    }
    
    // Builder
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final CalculatorResponse response = new CalculatorResponse();
        
        public Builder orderValue(Double orderValue) {
            response.orderValue = orderValue;
            return this;
        }
        
        public Builder leadTime(Integer leadTime) {
            response.leadTime = leadTime;
            return this;
        }
        
        public Builder pickup(Boolean pickup) {
            response.pickup = pickup;
            return this;
        }
        
        public Builder baseDiscount(Double baseDiscount) {
            response.baseDiscount = baseDiscount;
            return this;
        }
        
        public Builder earlyDiscount(Double earlyDiscount) {
            response.earlyDiscount = earlyDiscount;
            return this;
        }
        
        public Builder pickupDiscount(Double pickupDiscount) {
            response.pickupDiscount = pickupDiscount;
            return this;
        }
        
        public Builder totalDiscount(Double totalDiscount) {
            response.totalDiscount = totalDiscount;
            return this;
        }
        
        public Builder discountAmount(Double discountAmount) {
            response.discountAmount = discountAmount;
            return this;
        }
        
        public Builder finalPrice(Double finalPrice) {
            response.finalPrice = finalPrice;
            return this;
        }
        
        public CalculatorResponse build() {
            return response;
        }
    }
}