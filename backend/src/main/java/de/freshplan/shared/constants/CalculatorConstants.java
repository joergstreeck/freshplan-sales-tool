package de.freshplan.shared.constants;

/**
 * Constants for price calculation and discount rules.
 * 
 * <p>These constants define the business rules for discounts based on
 * order volume, early booking, and pickup options.
 */
public final class CalculatorConstants {
    
    // Volume-based discount thresholds (in EUR)
    public static final double VOLUME_THRESHOLD_TIER_1 = 75000.0;
    public static final double VOLUME_THRESHOLD_TIER_2 = 50000.0;
    public static final double VOLUME_THRESHOLD_TIER_3 = 30000.0;
    public static final double VOLUME_THRESHOLD_TIER_4 = 15000.0;
    public static final double VOLUME_THRESHOLD_TIER_5 = 5000.0;
    
    // Volume-based discount percentages
    public static final double VOLUME_DISCOUNT_TIER_1 = 10.0;
    public static final double VOLUME_DISCOUNT_TIER_2 = 9.0;
    public static final double VOLUME_DISCOUNT_TIER_3 = 8.0;
    public static final double VOLUME_DISCOUNT_TIER_4 = 6.0;
    public static final double VOLUME_DISCOUNT_TIER_5 = 3.0;
    
    // Early booking thresholds (in days)
    public static final int EARLY_BOOKING_DAYS_TIER_1 = 30;
    public static final int EARLY_BOOKING_DAYS_TIER_2 = 15;
    public static final int EARLY_BOOKING_DAYS_TIER_3 = 10;
    
    // Early booking discount percentages
    public static final double EARLY_BOOKING_DISCOUNT_TIER_1 = 3.0;
    public static final double EARLY_BOOKING_DISCOUNT_TIER_2 = 2.0;
    public static final double EARLY_BOOKING_DISCOUNT_TIER_3 = 1.0;
    
    // Pickup discount
    public static final double PICKUP_DISCOUNT_PERCENTAGE = 2.0;
    public static final double PICKUP_MINIMUM_ORDER_VALUE = 5000.0;
    
    // Maximum total discount allowed
    public static final double MAX_TOTAL_DISCOUNT_PERCENTAGE = 15.0;
    
    // Default values
    public static final double DEFAULT_PRICE = 0.0;
    public static final double DEFAULT_DISCOUNT = 0.0;
    
    // Private constructor to prevent instantiation
    private CalculatorConstants() {
        throw new AssertionError("Constants class should not be instantiated");
    }
}