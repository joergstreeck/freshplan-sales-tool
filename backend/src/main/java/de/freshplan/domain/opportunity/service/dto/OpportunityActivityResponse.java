package de.freshplan.domain.opportunity.service.dto;

import de.freshplan.domain.opportunity.entity.OpportunityActivity;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO für Opportunity Activities
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class OpportunityActivityResponse {

  private UUID id;
  private OpportunityActivity.ActivityType activityType;
  private String activityTypeDisplayName;
  private String title;
  private String description;
  private UUID createdById;
  private String createdByName;
  private LocalDateTime scheduledDate;
  private boolean completed;
  private LocalDateTime completedAt;
  private LocalDateTime createdAt;

  // Default constructor
  public OpportunityActivityResponse() {}

  // Builder pattern
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private OpportunityActivityResponse response = new OpportunityActivityResponse();

    public Builder id(UUID id) {
      response.id = id;
      return this;
    }

    public Builder activityType(OpportunityActivity.ActivityType activityType) {
      response.activityType = activityType;
      if (activityType != null) {
        response.activityTypeDisplayName = activityType.getDisplayName();
      }
      return this;
    }

    public Builder title(String title) {
      response.title = title;
      return this;
    }

    public Builder description(String description) {
      response.description = description;
      return this;
    }

    public Builder createdById(UUID createdById) {
      response.createdById = createdById;
      return this;
    }

    public Builder createdByName(String createdByName) {
      response.createdByName = createdByName;
      return this;
    }

    public Builder scheduledDate(LocalDateTime scheduledDate) {
      response.scheduledDate = scheduledDate;
      return this;
    }

    public Builder completed(boolean completed) {
      response.completed = completed;
      return this;
    }

    public Builder completedAt(LocalDateTime completedAt) {
      response.completedAt = completedAt;
      return this;
    }

    public Builder createdAt(LocalDateTime createdAt) {
      response.createdAt = createdAt;
      return this;
    }

    public OpportunityActivityResponse build() {
      return response;
    }
  }

  // Getters und Setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public OpportunityActivity.ActivityType getActivityType() {
    return activityType;
  }

  public void setActivityType(OpportunityActivity.ActivityType activityType) {
    this.activityType = activityType;
    if (activityType != null) {
      this.activityTypeDisplayName = activityType.getDisplayName();
    }
  }

  public String getActivityTypeDisplayName() {
    return activityTypeDisplayName;
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

  public UUID getCreatedById() {
    return createdById;
  }

  public void setCreatedById(UUID createdById) {
    this.createdById = createdById;
  }

  public String getCreatedByName() {
    return createdByName;
  }

  public void setCreatedByName(String createdByName) {
    this.createdByName = createdByName;
  }

  public LocalDateTime getScheduledDate() {
    return scheduledDate;
  }

  public void setScheduledDate(LocalDateTime scheduledDate) {
    this.scheduledDate = scheduledDate;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public LocalDateTime getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(LocalDateTime completedAt) {
    this.completedAt = completedAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
