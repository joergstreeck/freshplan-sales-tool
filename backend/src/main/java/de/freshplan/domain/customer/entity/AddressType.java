package de.freshplan.domain.customer.entity;

/**
 * Enumeration for different types of addresses.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public enum AddressType {
    
    BILLING("Rechnungsadresse"),
    SHIPPING("Lieferadresse"),
    OFFICE("Büro"),
    WAREHOUSE("Lager"),
    PRODUCTION("Produktion"),
    HEADQUARTERS("Hauptsitz"),
    BRANCH("Filiale"),
    MAILING("Postadresse"),
    LEGAL("Rechtssitz"),
    OTHER("Sonstige");
    
    private final String displayName;
    
    AddressType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}