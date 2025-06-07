package de.freshplan.domain.calculator.model;

/**
 * FreshPlan discount tiers based on order value.
 * 
 * Based on Freshfoodz GmbH business rules for B2B food wholesale.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public enum DiscountTier {
    
    /**
     * Under 5,000 EUR - No discount
     */
    TIER_0(0, 4999.99, 0.0),
    
    /**
     * 5,000 - 14,999 EUR - 3% discount
     */
    TIER_1(5000, 14999.99, 0.03),
    
    /**
     * 15,000 - 29,999 EUR - 6% discount
     */
    TIER_2(15000, 29999.99, 0.06),
    
    /**
     * 30,000 - 49,999 EUR - 8% discount
     */
    TIER_3(30000, 49999.99, 0.08),
    
    /**
     * 50,000 - 74,999 EUR - 9% discount
     */
    TIER_4(50000, 74999.99, 0.09),
    
    /**
     * 75,000+ EUR - 10% discount
     */
    TIER_5(75000, Double.MAX_VALUE, 0.10);
    
    private final double minOrderValue;
    private final double maxOrderValue;
    private final double discountRate;
    
    DiscountTier(double minOrderValue, double maxOrderValue, double discountRate) {
        this.minOrderValue = minOrderValue;
        this.maxOrderValue = maxOrderValue;
        this.discountRate = discountRate;
    }
    
    public double getMinOrderValue() {
        return minOrderValue;
    }
    
    public double getMaxOrderValue() {
        return maxOrderValue;
    }
    
    public double getDiscountRate() {
        return discountRate;
    }
    
    /**
     * Determines the discount tier for a given order value.
     * 
     * @param orderValue the net order value in EUR
     * @return the applicable discount tier
     */
    public static DiscountTier forOrderValue(double orderValue) {
        for (DiscountTier tier : values()) {
            if (orderValue >= tier.minOrderValue && orderValue <= tier.maxOrderValue) {
                return tier;
            }
        }
        return TIER_0; // fallback
    }
}