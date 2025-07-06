package de.freshplan.domain.customer.service.dto.timeline;

import de.freshplan.domain.customer.entity.EventCategory;
import de.freshplan.domain.customer.entity.ImportanceLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating a new timeline event.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class CreateTimelineEventRequest {

  @NotBlank(message = "Event type is required")
  @Size(max = 50)
  private String eventType;

  @NotBlank(message = "Title is required")
  @Size(max = 255)
  private String title;

  @Size(max = 5000)
  private String description;

  @NotNull(message = "Category is required") private EventCategory category;

  private ImportanceLevel importance;

  @NotBlank(message = "Performed by is required")
  @Size(max = 100)
  private String performedBy;

  @Size(max = 50)
  private String performedByRole;

  private LocalDateTime eventDate;

  // Communication fields
  @Size(max = 30)
  private String communicationChannel;

  @Size(max = 20)
  private String communicationDirection;

  private Integer communicationDuration;

  // Follow-up fields
  private Boolean requiresFollowUp;
  private LocalDateTime followUpDate;

  @Size(max = 1000)
  private String followUpNotes;

  // Related entities
  private UUID relatedContactId;
  private UUID relatedLocationId;
  private UUID relatedDocumentId;

  // Business impact
  @Size(max = 500)
  private String businessImpact;

  private BigDecimal revenueImpact;

  // Tags and labels
  private List<String> tags;
  private List<String> labels;

  // External references
  @Size(max = 100)
  private String externalId;

  @Size(max = 500)
  private String externalUrl;

  // Constructors
  public CreateTimelineEventRequest() {}

  // Builder pattern for easier construction
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final CreateTimelineEventRequest request = new CreateTimelineEventRequest();

    public Builder eventType(String eventType) {
      request.eventType = eventType;
      return this;
    }

    public Builder title(String title) {
      request.title = title;
      return this;
    }

    public Builder description(String description) {
      request.description = description;
      return this;
    }

    public Builder category(EventCategory category) {
      request.category = category;
      return this;
    }

    public Builder importance(ImportanceLevel importance) {
      request.importance = importance;
      return this;
    }

    public Builder performedBy(String performedBy) {
      request.performedBy = performedBy;
      return this;
    }

    public Builder performedByRole(String performedByRole) {
      request.performedByRole = performedByRole;
      return this;
    }

    public Builder requiresFollowUp(LocalDateTime followUpDate, String notes) {
      request.requiresFollowUp = true;
      request.followUpDate = followUpDate;
      request.followUpNotes = notes;
      return this;
    }

    public CreateTimelineEventRequest build() {
      return request;
    }
  }

  // Getters and Setters
  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
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

  public LocalDateTime getEventDate() {
    return eventDate;
  }

  public void setEventDate(LocalDateTime eventDate) {
    this.eventDate = eventDate;
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
}
