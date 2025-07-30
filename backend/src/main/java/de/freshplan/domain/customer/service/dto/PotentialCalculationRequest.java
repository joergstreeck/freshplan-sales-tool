package de.freshplan.domain.customer.service.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * Request DTO for calculating customer sales potential.
 * Includes industry and service offerings for accurate calculation.
 * 
 * @since 2.0.0
 */
public class PotentialCalculationRequest {
    
    @NotNull(message = "Industry is required for potential calculation")
    private String industry;
    
    private Map<String, Object> serviceOfferings;
    
    // Additional calculation parameters
    private Integer numberOfLocations;
    private Boolean includeQuickWins = true;
    
    // Constructors
    public PotentialCalculationRequest() {}
    
    public PotentialCalculationRequest(String industry, Map<String, Object> serviceOfferings) {
        this.industry = industry;
        this.serviceOfferings = serviceOfferings;
    }
    
    // Helper methods
    public Object getServiceOffering(String key) {
        return serviceOfferings != null ? serviceOfferings.get(key) : null;
    }
    
    public boolean hasServiceOffering(String key) {
        return serviceOfferings != null && serviceOfferings.containsKey(key);
    }
    
    public Integer getIntegerValue(String key, Integer defaultValue) {
        Object value = getServiceOffering(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
    
    public Boolean getBooleanValue(String key, Boolean defaultValue) {
        Object value = getServiceOffering(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }
    
    // Getters and Setters
    public String getIndustry() {
        return industry;
    }
    
    public void setIndustry(String industry) {
        this.industry = industry;
    }
    
    public Map<String, Object> getServiceOfferings() {
        return serviceOfferings;
    }
    
    public void setServiceOfferings(Map<String, Object> serviceOfferings) {
        this.serviceOfferings = serviceOfferings;
    }
    
    public Integer getNumberOfLocations() {
        return numberOfLocations;
    }
    
    public void setNumberOfLocations(Integer numberOfLocations) {
        this.numberOfLocations = numberOfLocations;
    }
    
    public Boolean getIncludeQuickWins() {
        return includeQuickWins;
    }
    
    public void setIncludeQuickWins(Boolean includeQuickWins) {
        this.includeQuickWins = includeQuickWins;
    }
}