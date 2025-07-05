package de.freshplan.domain.customer.entity;

/**
 * Enumeration for different categories of customer locations.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public enum LocationCategory {
    
    HEADQUARTERS("Hauptsitz"),
    BRANCH_OFFICE("Filiale"),
    WAREHOUSE("Lager"),
    PRODUCTION_FACILITY("Produktionsstätte"),
    RETAIL_STORE("Einzelhandelsgeschäft"),
    DISTRIBUTION_CENTER("Verteilzentrum"),
    OFFICE("Büro"),
    SERVICE_CENTER("Service Center"),
    RESEARCH_FACILITY("Forschungseinrichtung"),
    TEMPORARY("Temporär"),
    OTHER("Sonstige");
    
    private final String displayName;
    
    LocationCategory(String displayName) {
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