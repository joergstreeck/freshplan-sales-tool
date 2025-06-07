package de.freshplan.domain.calculator.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Result of FreshPlan discount calculation.
 * 
 * Contains all applied discounts and final pricing.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public final class FreshPlanCalculationResult {
    
    private final BigDecimal orderValueNet;
    private final DiscountTier volumeDiscountTier;
    private final LeadTimeDiscount leadTimeDiscount;
    private final boolean selfPickupDiscount;
    
    private final BigDecimal volumeDiscountRate;
    private final BigDecimal leadTimeDiscountRate;
    private final BigDecimal selfPickupDiscountRate;
    private final BigDecimal totalDiscountRate;
    
    private final BigDecimal volumeDiscountAmount;
    private final BigDecimal leadTimeDiscountAmount;
    private final BigDecimal selfPickupDiscountAmount;
    private final BigDecimal totalDiscountAmount;
    
    private final BigDecimal finalNetValue;
    private final BigDecimal vatAmount;
    private final BigDecimal finalGrossValue;
    
    private static final BigDecimal VAT_RATE = new BigDecimal("0.19"); // 19% German VAT
    private static final BigDecimal SELF_PICKUP_DISCOUNT_RATE = new BigDecimal("0.02"); // 2%
    private static final BigDecimal MIN_ORDER_FOR_PICKUP_DISCOUNT = new BigDecimal("5000"); // €5,000
    private static final BigDecimal MAX_TOTAL_DISCOUNT = new BigDecimal("0.15"); // 15% maximum
    
    /**
     * Creates a FreshPlan calculation result.
     * 
     * @param request the original calculation request
     */
    public FreshPlanCalculationResult(FreshPlanCalculationRequest request) {
        this.orderValueNet = request.getOrderValueNet();
        
        // Determine discount tiers
        this.volumeDiscountTier = DiscountTier.forOrderValue(orderValueNet.doubleValue());
        this.leadTimeDiscount = LeadTimeDiscount.forLeadTime(request.getLeadTimeDays());
        this.selfPickupDiscount = request.getSelfPickup() && 
            orderValueNet.compareTo(MIN_ORDER_FOR_PICKUP_DISCOUNT) >= 0;
        
        // Set discount rates
        this.volumeDiscountRate = BigDecimal.valueOf(volumeDiscountTier.getDiscountRate());
        this.leadTimeDiscountRate = BigDecimal.valueOf(leadTimeDiscount.getAdditionalDiscountRate());
        this.selfPickupDiscountRate = selfPickupDiscount ? SELF_PICKUP_DISCOUNT_RATE : BigDecimal.ZERO;
        
        // Calculate total discount rate (capped at 15%)
        BigDecimal calculatedTotalRate = volumeDiscountRate
            .add(leadTimeDiscountRate)
            .add(selfPickupDiscountRate);
        this.totalDiscountRate = calculatedTotalRate.min(MAX_TOTAL_DISCOUNT);
        
        // Calculate discount amounts
        this.volumeDiscountAmount = orderValueNet
            .multiply(volumeDiscountRate)
            .setScale(2, RoundingMode.HALF_UP);
        
        this.leadTimeDiscountAmount = orderValueNet
            .multiply(leadTimeDiscountRate)
            .setScale(2, RoundingMode.HALF_UP);
        
        this.selfPickupDiscountAmount = orderValueNet
            .multiply(selfPickupDiscountRate)
            .setScale(2, RoundingMode.HALF_UP);
        
        this.totalDiscountAmount = orderValueNet
            .multiply(totalDiscountRate)
            .setScale(2, RoundingMode.HALF_UP);
        
        // Calculate final values
        this.finalNetValue = orderValueNet
            .subtract(totalDiscountAmount)
            .setScale(2, RoundingMode.HALF_UP);
        
        this.vatAmount = finalNetValue
            .multiply(VAT_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        
        this.finalGrossValue = finalNetValue
            .add(vatAmount)
            .setScale(2, RoundingMode.HALF_UP);
    }
    
    // Getters
    public BigDecimal getOrderValueNet() { return orderValueNet; }
    public DiscountTier getVolumeDiscountTier() { return volumeDiscountTier; }
    public LeadTimeDiscount getLeadTimeDiscount() { return leadTimeDiscount; }
    public boolean hasSelfPickupDiscount() { return selfPickupDiscount; }
    
    public BigDecimal getVolumeDiscountRate() { return volumeDiscountRate; }
    public BigDecimal getLeadTimeDiscountRate() { return leadTimeDiscountRate; }
    public BigDecimal getSelfPickupDiscountRate() { return selfPickupDiscountRate; }
    public BigDecimal getTotalDiscountRate() { return totalDiscountRate; }
    
    public BigDecimal getVolumeDiscountAmount() { return volumeDiscountAmount; }
    public BigDecimal getLeadTimeDiscountAmount() { return leadTimeDiscountAmount; }
    public BigDecimal getSelfPickupDiscountAmount() { return selfPickupDiscountAmount; }
    public BigDecimal getTotalDiscountAmount() { return totalDiscountAmount; }
    
    public BigDecimal getFinalNetValue() { return finalNetValue; }
    public BigDecimal getVatAmount() { return vatAmount; }
    public BigDecimal getFinalGrossValue() { return finalGrossValue; }
    
    @Override
    public String toString() {
        return String.format(
            "FreshPlanCalculationResult{orderValue=€%.2f, totalDiscount=%.1f%%, finalGross=€%.2f}",
            orderValueNet, totalDiscountRate.multiply(BigDecimal.valueOf(100)), finalGrossValue
        );
    }
}