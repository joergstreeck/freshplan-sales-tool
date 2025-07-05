package de.freshplan.domain.customer.service.dto.timeline;

import de.freshplan.domain.customer.entity.ImportanceLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for updating a timeline event.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class UpdateTimelineEventRequest {
    
    @Size(max = 255)
    private String title;
    
    @Size(max = 5000)
    private String description;
    
    private ImportanceLevel importance;
    
    @Size(max = 500)
    private String businessImpact;
    
    private BigDecimal revenueImpact;
    
    private List<String> tags;
    
    @NotBlank(message = "Updated by is required")
    @Size(max = 100)
    private String updatedBy;
    
    // Constructors
    public UpdateTimelineEventRequest() {}
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public ImportanceLevel getImportance() {
        return importance;
    }
    
    public void setImportance(ImportanceLevel importance) {
        this.importance = importance;
    }
    
    public String getBusinessImpact() {
        return businessImpact;
    }
    
    public void setBusinessImpact(String businessImpact) {
        this.businessImpact = businessImpact;
    }
    
    public BigDecimal getRevenueImpact() {
        return revenueImpact;
    }
    
    public void setRevenueImpact(BigDecimal revenueImpact) {
        this.revenueImpact = revenueImpact;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}