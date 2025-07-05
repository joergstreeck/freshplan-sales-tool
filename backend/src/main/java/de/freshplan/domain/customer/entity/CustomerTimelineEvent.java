package de.freshplan.domain.customer.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Customer timeline event entity representing all activities and changes 
 * related to a customer. Provides comprehensive audit trail and activity history.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Entity
@Table(name = "customer_timeline_events", indexes = {
    @Index(name = "idx_timeline_customer", columnList = "customer_id"),
    @Index(name = "idx_timeline_date", columnList = "event_date DESC"),
    @Index(name = "idx_timeline_category", columnList = "category"),
    @Index(name = "idx_timeline_importance", columnList = "importance"),
    @Index(name = "idx_timeline_type", columnList = "event_type"),
    @Index(name = "idx_timeline_customer_date", columnList = "customer_id, event_date DESC")
})
public class CustomerTimelineEvent extends PanacheEntityBase {
    
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    // Event Basic Information
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;
    
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    // Categorization
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private EventCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "importance", nullable = false, length = 20)
    private ImportanceLevel importance = ImportanceLevel.MEDIUM;
    
    // Context Information
    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;
    
    @Column(name = "performed_by_role", length = 50)
    private String performedByRole;
    
    @Column(name = "source_system", length = 50)
    private String sourceSystem = "FreshPlan CRM";
    
    @Column(name = "source_reference", length = 255)
    private String sourceReference;
    
    // Related Entities (optional foreign keys)
    @Column(name = "related_contact_id")
    private UUID relatedContactId;
    
    @Column(name = "related_location_id")
    private UUID relatedLocationId;
    
    @Column(name = "related_document_id")
    private UUID relatedDocumentId;
    
    // Communication specific fields
    @Column(name = "communication_channel", length = 30)
    private String communicationChannel; // email, phone, teams, etc.
    
    @Column(name = "communication_direction", length = 20)
    private String communicationDirection; // inbound, outbound
    
    @Column(name = "communication_duration")
    private Integer communicationDuration; // in minutes
    
    // Follow-up Information
    @Column(name = "requires_follow_up", nullable = false)
    private Boolean requiresFollowUp = false;
    
    @Column(name = "follow_up_date")
    private LocalDateTime followUpDate;
    
    @Column(name = "follow_up_notes", columnDefinition = "TEXT")
    private String followUpNotes;
    
    @Column(name = "follow_up_completed", nullable = false)
    private Boolean followUpCompleted = false;
    
    // Data Change Information (for audit trail)
    @Column(name = "old_values", columnDefinition = "VARCHAR(2000)")
    private String oldValues; // JSON string of changed fields
    
    @Column(name = "new_values", columnDefinition = "VARCHAR(2000)")
    private String newValues; // JSON string of new values
    
    @Column(name = "changed_fields", length = 500)
    private String changedFields; // Comma-separated list of field names
    
    // Business Impact
    @Column(name = "business_impact", length = 500)
    private String businessImpact;
    
    @Column(name = "revenue_impact", precision = 12, scale = 2)
    private java.math.BigDecimal revenueImpact;
    
    // Visibility and Access Control
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;
    
    @Column(name = "is_customer_visible", nullable = false)
    private Boolean isCustomerVisible = false;
    
    @Column(name = "access_level", length = 20)
    private String accessLevel = "INTERNAL"; // INTERNAL, CUSTOMER, PUBLIC
    
    // Tags and Labels
    @Column(name = "tags", length = 500)
    private String tags; // Comma-separated tags
    
    @Column(name = "labels", length = 500)
    private String labels; // Comma-separated labels
    
    // External References
    @Column(name = "external_id", length = 100)
    private String externalId;
    
    @Column(name = "external_url", length = 500)
    private String externalUrl;
    
    // Soft Delete (for sensitive events)
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Column(name = "deleted_by", length = 100)
    private String deletedBy;
    
    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    // Lifecycle Methods
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (eventDate == null) {
            eventDate = LocalDateTime.now();
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
        if (isPublic == null) {
            isPublic = true;
        }
        if (isCustomerVisible == null) {
            isCustomerVisible = false;
        }
        if (requiresFollowUp == null) {
            requiresFollowUp = false;
        }
        if (followUpCompleted == null) {
            followUpCompleted = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business Methods
    
    /**
     * Marks this event as requiring follow-up.
     */
    public void setFollowUpRequired(LocalDateTime followUpDate, String notes) {
        this.requiresFollowUp = true;
        this.followUpDate = followUpDate;
        this.followUpNotes = notes;
        this.followUpCompleted = false;
    }
    
    /**
     * Marks the follow-up as completed.
     */
    public void completeFollowUp(String completedBy) {
        this.followUpCompleted = true;
        this.updatedBy = completedBy;
    }
    
    /**
     * Checks if this event is overdue for follow-up.
     */
    public boolean isFollowUpOverdue() {
        return Boolean.TRUE.equals(requiresFollowUp) && 
               !Boolean.TRUE.equals(followUpCompleted) &&
               followUpDate != null && 
               followUpDate.isBefore(LocalDateTime.now());
    }
    
    /**
     * Adds a tag to this event.
     */
    public void addTag(String tag) {
        if (tag != null && !tag.isBlank()) {
            if (this.tags == null || this.tags.isBlank()) {
                this.tags = tag;
            } else if (!this.tags.contains(tag)) {
                this.tags += "," + tag;
            }
        }
    }
    
    /**
     * Adds a label to this event.
     */
    public void addLabel(String label) {
        if (label != null && !label.isBlank()) {
            if (this.labels == null || this.labels.isBlank()) {
                this.labels = label;
            } else if (!this.labels.contains(label)) {
                this.labels += "," + label;
            }
        }
    }
    
    /**
     * Checks if this event has a specific tag.
     */
    public boolean hasTag(String tag) {
        return this.tags != null && this.tags.contains(tag);
    }
    
    /**
     * Checks if this event has a specific label.
     */
    public boolean hasLabel(String label) {
        return this.labels != null && this.labels.contains(label);
    }
    
    /**
     * Gets the event age in days.
     */
    public long getEventAgeInDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(
            eventDate.toLocalDate(), 
            LocalDateTime.now().toLocalDate()
        );
    }
    
    /**
     * Checks if this is a system-generated event.
     */
    public boolean isSystemEvent() {
        return category == EventCategory.SYSTEM;
    }
    
    /**
     * Checks if this is a user-generated event.
     */
    public boolean isUserEvent() {
        return category != EventCategory.SYSTEM;
    }
    
    /**
     * Gets a summary description for displays.
     */
    public String getSummary() {
        if (title != null && !title.isBlank()) {
            return title;
        }
        if (description != null && description.length() > 100) {
            return description.substring(0, 97) + "...";
        }
        return description != null ? description : eventType;
    }
    
    // Static factory methods for common event types
    public static CustomerTimelineEvent createSystemEvent(
            Customer customer, 
            String eventType, 
            String description, 
            String performedBy) {
        
        CustomerTimelineEvent event = new CustomerTimelineEvent();
        event.setCustomer(customer);
        event.setEventType(eventType);
        event.setTitle(eventType);
        event.setDescription(description);
        event.setCategory(EventCategory.SYSTEM);
        event.setImportance(ImportanceLevel.MEDIUM);
        event.setPerformedBy(performedBy);
        event.setEventDate(LocalDateTime.now());
        return event;
    }
    
    public static CustomerTimelineEvent createCommunicationEvent(
            Customer customer,
            String channel,
            String direction,
            String description,
            String performedBy) {
        
        CustomerTimelineEvent event = new CustomerTimelineEvent();
        event.setCustomer(customer);
        event.setEventType("COMMUNICATION");
        event.setTitle("Kommunikation via " + channel);
        event.setDescription(description);
        event.setCategory(EventCategory.COMMUNICATION);
        event.setImportance(ImportanceLevel.MEDIUM);
        event.setPerformedBy(performedBy);
        event.setCommunicationChannel(channel);
        event.setCommunicationDirection(direction);
        event.setEventDate(LocalDateTime.now());
        return event;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
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

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getSourceReference() {
        return sourceReference;
    }

    public void setSourceReference(String sourceReference) {
        this.sourceReference = sourceReference;
    }

    public UUID getRelatedContactId() {
        return relatedContactId;
    }

    public void setRelatedContactId(UUID relatedContactId) {
        this.relatedContactId = relatedContactId;
    }

    public UUID getRelatedLocationId() {
        return relatedLocationId;
    }

    public void setRelatedLocationId(UUID relatedLocationId) {
        this.relatedLocationId = relatedLocationId;
    }

    public UUID getRelatedDocumentId() {
        return relatedDocumentId;
    }

    public void setRelatedDocumentId(UUID relatedDocumentId) {
        this.relatedDocumentId = relatedDocumentId;
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

    public String getOldValues() {
        return oldValues;
    }

    public void setOldValues(String oldValues) {
        this.oldValues = oldValues;
    }

    public String getNewValues() {
        return newValues;
    }

    public void setNewValues(String newValues) {
        this.newValues = newValues;
    }

    public String getChangedFields() {
        return changedFields;
    }

    public void setChangedFields(String changedFields) {
        this.changedFields = changedFields;
    }

    public String getBusinessImpact() {
        return businessImpact;
    }

    public void setBusinessImpact(String businessImpact) {
        this.businessImpact = businessImpact;
    }

    public java.math.BigDecimal getRevenueImpact() {
        return revenueImpact;
    }

    public void setRevenueImpact(java.math.BigDecimal revenueImpact) {
        this.revenueImpact = revenueImpact;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Boolean getIsCustomerVisible() {
        return isCustomerVisible;
    }

    public void setIsCustomerVisible(Boolean isCustomerVisible) {
        this.isCustomerVisible = isCustomerVisible;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
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

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        return "CustomerTimelineEvent{" +
                "id=" + id +
                ", eventType='" + eventType + '\'' +
                ", eventDate=" + eventDate +
                ", title='" + title + '\'' +
                ", category=" + category +
                ", importance=" + importance +
                ", performedBy='" + performedBy + '\'' +
                '}';
    }
}