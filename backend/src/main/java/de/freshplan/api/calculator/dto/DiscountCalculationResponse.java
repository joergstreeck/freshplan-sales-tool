package de.freshplan.api.calculator.dto;

import de.freshplan.domain.calculator.model.DiscountTier;
import de.freshplan.domain.calculator.model.LeadTimeDiscount;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for discount calculation API.
 * 
 * Contains the complete discount calculation result with applied discounts
 * and final pricing.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public final class DiscountCalculationResponse {
    
    // Input values
    private final BigDecimal orderValueNet;
    private final Integer leadTimeDays;
    private final Boolean selfPickup;
    
    // Discount classification
    private final DiscountTier volumeDiscountTier;
    private final LeadTimeDiscount leadTimeDiscountCategory;
    private final Boolean hasSelfPickupDiscount;
    
    // Discount rates (as decimals, e.g., 0.03 for 3%)
    private final BigDecimal volumeDiscountRate;
    private final BigDecimal leadTimeDiscountRate;
    private final BigDecimal selfPickupDiscountRate;
    private final BigDecimal totalDiscountRate;
    
    // Discount amounts in EUR
    private final BigDecimal volumeDiscountAmount;
    private final BigDecimal leadTimeDiscountAmount;
    private final BigDecimal selfPickupDiscountAmount;
    private final BigDecimal totalDiscountAmount;
    
    // Final values
    private final BigDecimal finalNetValue;
    
    // Metadata for future API features
    private final LocalDateTime calculatedAt;
    
    /**
     * Creates a discount calculation response.
     */
    public DiscountCalculationResponse(BigDecimal orderValueNet,
                                     Integer leadTimeDays,
                                     Boolean selfPickup,
                                     DiscountTier volumeDiscountTier,
                                     LeadTimeDiscount leadTimeDiscountCategory,
                                     Boolean hasSelfPickupDiscount,
                                     BigDecimal volumeDiscountRate,
                                     BigDecimal leadTimeDiscountRate,
                                     BigDecimal selfPickupDiscountRate,
                                     BigDecimal totalDiscountRate,
                                     BigDecimal volumeDiscountAmount,
                                     BigDecimal leadTimeDiscountAmount,
                                     BigDecimal selfPickupDiscountAmount,
                                     BigDecimal totalDiscountAmount,
                                     BigDecimal finalNetValue,
                                     LocalDateTime calculatedAt) {
        this.orderValueNet = orderValueNet;
        this.leadTimeDays = leadTimeDays;
        this.selfPickup = selfPickup;
        this.volumeDiscountTier = volumeDiscountTier;
        this.leadTimeDiscountCategory = leadTimeDiscountCategory;
        this.hasSelfPickupDiscount = hasSelfPickupDiscount;
        this.volumeDiscountRate = volumeDiscountRate;
        this.leadTimeDiscountRate = leadTimeDiscountRate;
        this.selfPickupDiscountRate = selfPickupDiscountRate;
        this.totalDiscountRate = totalDiscountRate;
        this.volumeDiscountAmount = volumeDiscountAmount;
        this.leadTimeDiscountAmount = leadTimeDiscountAmount;
        this.selfPickupDiscountAmount = selfPickupDiscountAmount;
        this.totalDiscountAmount = totalDiscountAmount;
        this.finalNetValue = finalNetValue;
        this.calculatedAt = calculatedAt;
    }
    
    // Getters for input values
    public BigDecimal getOrderValueNet() { return orderValueNet; }
    public Integer getLeadTimeDays() { return leadTimeDays; }
    public Boolean getSelfPickup() { return selfPickup; }
    
    // Getters for discount classification
    public DiscountTier getVolumeDiscountTier() { return volumeDiscountTier; }
    public LeadTimeDiscount getLeadTimeDiscountCategory() { return leadTimeDiscountCategory; }
    public Boolean getHasSelfPickupDiscount() { return hasSelfPickupDiscount; }
    
    // Getters for discount rates
    public BigDecimal getVolumeDiscountRate() { return volumeDiscountRate; }
    public BigDecimal getLeadTimeDiscountRate() { return leadTimeDiscountRate; }
    public BigDecimal getSelfPickupDiscountRate() { return selfPickupDiscountRate; }
    public BigDecimal getTotalDiscountRate() { return totalDiscountRate; }
    
    // Getters for discount amounts
    public BigDecimal getVolumeDiscountAmount() { return volumeDiscountAmount; }
    public BigDecimal getLeadTimeDiscountAmount() { return leadTimeDiscountAmount; }
    public BigDecimal getSelfPickupDiscountAmount() { return selfPickupDiscountAmount; }
    public BigDecimal getTotalDiscountAmount() { return totalDiscountAmount; }
    
    // Getters for final values
    public BigDecimal getFinalNetValue() { return finalNetValue; }
    
    // Getters for metadata
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    
    @Override
    public String toString() {
        return String.format(
            "DiscountCalculationResponse{orderValue=%s, totalDiscount=%s, finalNet=%s}",
            orderValueNet != null ? String.format("€%.2f", orderValueNet) : "null",
            totalDiscountRate != null ? 
                String.format("%.1f%%", totalDiscountRate.multiply(BigDecimal.valueOf(100))) : "null",
            finalNetValue != null ? String.format("€%.2f", finalNetValue) : "null"
        );
    }
}