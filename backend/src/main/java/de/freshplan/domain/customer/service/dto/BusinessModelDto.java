package de.freshplan.domain.customer.service.dto;

import de.freshplan.domain.customer.entity.FinancingType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * DTO for customer business model information.
 * Includes financing type and pain points for sales opportunities.
 * 
 * @since 2.0.0
 */
public class BusinessModelDto {
    
    private FinancingType primaryFinancing;
    
    @Size(max = 20, message = "Maximum 20 pain points allowed")
    private List<String> painPoints;
    
    // Constructors
    public BusinessModelDto() {}
    
    public BusinessModelDto(FinancingType primaryFinancing, List<String> painPoints) {
        this.primaryFinancing = primaryFinancing;
        this.painPoints = painPoints;
    }
    
    // Helper method to check if customer has specific pain point
    public boolean hasPainPoint(String painPoint) {
        return painPoints != null && painPoints.contains(painPoint);
    }
    
    // Helper method to count pain points
    public int getPainPointCount() {
        return painPoints != null ? painPoints.size() : 0;
    }
    
    // Getters and Setters
    public FinancingType getPrimaryFinancing() {
        return primaryFinancing;
    }
    
    public void setPrimaryFinancing(FinancingType primaryFinancing) {
        this.primaryFinancing = primaryFinancing;
    }
    
    public List<String> getPainPoints() {
        return painPoints;
    }
    
    public void setPainPoints(List<String> painPoints) {
        this.painPoints = painPoints;
    }
}