package de.freshplan.domain.calculator.model;

/**
 * Types of buffet menus for calculation.
 * 
 * MVP Version: Only basic buffet types with fixed pricing.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public enum BuffetMenuType {
    
    /**
     * Basic buffet: Salads, bread, 2 warm dishes.
     * Base price: €12.50 per person
     */
    BASIC("Basic Buffet", 12.50),
    
    /**
     * Premium buffet: Salads, bread, 3 warm dishes, dessert.
     * Base price: €18.50 per person
     */
    PREMIUM("Premium Buffet", 18.50),
    
    /**
     * Deluxe buffet: Full spread with appetizers, multiple warm dishes, desserts.
     * Base price: €24.90 per person
     */
    DELUXE("Deluxe Buffet", 24.90);
    
    private final String displayName;
    private final double basePricePerPerson;
    
    BuffetMenuType(String displayName, double basePricePerPerson) {
        this.displayName = displayName;
        this.basePricePerPerson = basePricePerPerson;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public double getBasePricePerPerson() {
        return basePricePerPerson;
    }
}