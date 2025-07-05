package de.freshplan.domain.customer.service.dto.timeline;

import de.freshplan.domain.customer.entity.EventCategory;
import de.freshplan.domain.customer.entity.ImportanceLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for timeline events.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class TimelineEventResponse {
    
    private UUID id;
    private String eventType;
    private LocalDateTime eventDate;
    private String title;
    private String description;
    private EventCategory category;
    private ImportanceLevel importance;
    private String performedBy;
    private String performedByRole;
    
    // Communication details
    private String communicationChannel;
    private String communicationDirection;
    private Integer communicationDuration;
    
    // Follow-up information
    private Boolean requiresFollowUp;
    private LocalDateTime followUpDate;
    private String followUpNotes;
    private Boolean followUpCompleted;
    
    // Related entities
    private UUID relatedContactId;
    private String relatedContactName;
    private UUID relatedLocationId;
    private String relatedLocationName;
    
    // Business impact
    private String businessImpact;
    private BigDecimal revenueImpact;
    
    // Metadata
    private List<String> tags;
    private List<String> labels;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private Long eventAgeInDays;
    private Boolean isFollowUpOverdue;
    private String summary;
    
    // Constructors
    public TimelineEventResponse() {}
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public LocalDateTime getEventDate() {
        return eventDate;
    }
    
    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }
    
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
    
    public EventCategory getCategory() {
        return category;
    }
    
    public void setCategory(EventCategory category) {
        this.category = category;
    }
    
    public ImportanceLevel getImportance() {
        return importance;
    }
    
    public void setImportance(ImportanceLevel importance) {
        this.importance = importance;
    }
    
    public String getPerformedBy() {
        return performedBy;
    }
    
    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }
    
    public String getPerformedByRole() {
        return performedByRole;
    }
    
    public void setPerformedByRole(String performedByRole) {
        this.performedByRole = performedByRole;
    }
    
    public String getCommunicationChannel() {
        return communicationChannel;
    }
    
    public void setCommunicationChannel(String communicationChannel) {
        this.communicationChannel = communicationChannel;
    }
    
    public String getCommunicationDirection() {
        return communicationDirection;
    }
    
    public void setCommunicationDirection(String communicationDirection) {
        this.communicationDirection = communicationDirection;
    }
    
    public Integer getCommunicationDuration() {
        return communicationDuration;
    }
    
    public void setCommunicationDuration(Integer communicationDuration) {
        this.communicationDuration = communicationDuration;
    }
    
    public Boolean getRequiresFollowUp() {
        return requiresFollowUp;
    }
    
    public void setRequiresFollowUp(Boolean requiresFollowUp) {
        this.requiresFollowUp = requiresFollowUp;
    }
    
    public LocalDateTime getFollowUpDate() {
        return followUpDate;
    }
    
    public void setFollowUpDate(LocalDateTime followUpDate) {
        this.followUpDate = followUpDate;
    }
    
    public String getFollowUpNotes() {
        return followUpNotes;
    }
    
    public void setFollowUpNotes(String followUpNotes) {
        this.followUpNotes = followUpNotes;
    }
    
    public Boolean getFollowUpCompleted() {
        return followUpCompleted;
    }
    
    public void setFollowUpCompleted(Boolean followUpCompleted) {
        this.followUpCompleted = followUpCompleted;
    }
    
    public UUID getRelatedContactId() {
        return relatedContactId;
    }
    
    public void setRelatedContactId(UUID relatedContactId) {
        this.relatedContactId = relatedContactId;
    }
    
    public String getRelatedContactName() {
        return relatedContactName;
    }
    
    public void setRelatedContactName(String relatedContactName) {
        this.relatedContactName = relatedContactName;
    }
    
    public UUID getRelatedLocationId() {
        return relatedLocationId;
    }
    
    public void setRelatedLocationId(UUID relatedLocationId) {
        this.relatedLocationId = relatedLocationId;
    }
    
    public String getRelatedLocationName() {
        return relatedLocationName;
    }
    
    public void setRelatedLocationName(String relatedLocationName) {
        this.relatedLocationName = relatedLocationName;
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
    
    public List<String> getLabels() {
        return labels;
    }
    
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
    
    public Boolean getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Long getEventAgeInDays() {
        return eventAgeInDays;
    }
    
    public void setEventAgeInDays(Long eventAgeInDays) {
        this.eventAgeInDays = eventAgeInDays;
    }
    
    public Boolean getIsFollowUpOverdue() {
        return isFollowUpOverdue;
    }
    
    public void setIsFollowUpOverdue(Boolean isFollowUpOverdue) {
        this.isFollowUpOverdue = isFollowUpOverdue;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
}