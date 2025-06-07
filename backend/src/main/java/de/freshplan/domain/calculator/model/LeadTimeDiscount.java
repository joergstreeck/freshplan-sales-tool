package de.freshplan.domain.calculator.model;

/**
 * FreshPlan early booking discounts based on lead time.
 * 
 * Additional discount for orders placed in advance.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public enum LeadTimeDiscount {
    
    /**
     * Less than 10 days - No additional discount
     */
    NO_DISCOUNT(0, 9, 0.0),
    
    /**
     * 10-14 days advance - Additional 1% discount
     */
    SHORT_LEAD(10, 14, 0.01),
    
    /**
     * 15-29 days advance - Additional 2% discount
     */
    MEDIUM_LEAD(15, 29, 0.02),
    
    /**
     * 30+ days advance - Additional 3% discount
     */
    LONG_LEAD(30, Integer.MAX_VALUE, 0.03);
    
    private final int minDays;
    private final int maxDays;
    private final double additionalDiscountRate;
    
    LeadTimeDiscount(int minDays, int maxDays, double additionalDiscountRate) {
        this.minDays = minDays;
        this.maxDays = maxDays;
        this.additionalDiscountRate = additionalDiscountRate;
    }
    
    public int getMinDays() {
        return minDays;
    }
    
    public int getMaxDays() {
        return maxDays;
    }
    
    public double getAdditionalDiscountRate() {
        return additionalDiscountRate;
    }
    
    /**
     * Determines the lead time discount for given days in advance.
     * 
     * @param daysInAdvance number of days between order and delivery
     * @return the applicable lead time discount
     */
    public static LeadTimeDiscount forLeadTime(int daysInAdvance) {
        for (LeadTimeDiscount discount : values()) {
            if (daysInAdvance >= discount.minDays && daysInAdvance <= discount.maxDays) {
                return discount;
            }
        }
        return NO_DISCOUNT; // fallback
    }
}