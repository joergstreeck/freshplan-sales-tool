package de.freshplan.domain.opportunity.service.dto;

import de.freshplan.domain.opportunity.entity.OpportunityStage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO für Opportunity-Daten
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class OpportunityResponse {

  private UUID id;
  private String name;
  private String description;
  private OpportunityStage stage;
  private String stageDisplayName;
  private String stageColor;
  private UUID customerId;
  private String customerName;
  private Long leadId; // Sprint 2.1.7.1 - Lead-Origin Traceability
  private String leadCompanyName; // Sprint 2.1.7.1 - Lead-Name Fallback
  private UUID assignedToId;
  private String assignedToName;
  private BigDecimal expectedValue;
  private LocalDate expectedCloseDate;
  private Integer probability;
  private LocalDateTime createdAt;
  private LocalDateTime stageChangedAt;
  private LocalDateTime updatedAt;
  private List<OpportunityActivityResponse> activities;

  // Default constructor
  public OpportunityResponse() {}

  // Builder pattern
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private OpportunityResponse response = new OpportunityResponse();

    public Builder id(UUID id) {
      response.id = id;
      return this;
    }

    public Builder name(String name) {
      response.name = name;
      return this;
    }

    public Builder description(String description) {
      response.description = description;
      return this;
    }

    public Builder stage(OpportunityStage stage) {
      response.stage = stage;
      if (stage != null) {
        response.stageDisplayName = stage.getDisplayName();
        response.stageColor = stage.getColor();
      }
      return this;
    }

    public Builder customerId(UUID customerId) {
      response.customerId = customerId;
      return this;
    }

    public Builder customerName(String customerName) {
      response.customerName = customerName;
      return this;
    }

    public Builder leadId(Long leadId) {
      response.leadId = leadId;
      return this;
    }

    public Builder leadCompanyName(String leadCompanyName) {
      response.leadCompanyName = leadCompanyName;
      return this;
    }

    public Builder assignedToId(UUID assignedToId) {
      response.assignedToId = assignedToId;
      return this;
    }

    public Builder assignedToName(String assignedToName) {
      response.assignedToName = assignedToName;
      return this;
    }

    public Builder expectedValue(BigDecimal expectedValue) {
      response.expectedValue = expectedValue;
      return this;
    }

    public Builder expectedCloseDate(LocalDate expectedCloseDate) {
      response.expectedCloseDate = expectedCloseDate;
      return this;
    }

    public Builder probability(Integer probability) {
      response.probability = probability;
      return this;
    }

    public Builder createdAt(LocalDateTime createdAt) {
      response.createdAt = createdAt;
      return this;
    }

    public Builder stageChangedAt(LocalDateTime stageChangedAt) {
      response.stageChangedAt = stageChangedAt;
      return this;
    }

    public Builder updatedAt(LocalDateTime updatedAt) {
      response.updatedAt = updatedAt;
      return this;
    }

    public Builder activities(List<OpportunityActivityResponse> activities) {
      response.activities = activities;
      return this;
    }

    public OpportunityResponse build() {
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public OpportunityStage getStage() {
    return stage;
  }

  public void setStage(OpportunityStage stage) {
    this.stage = stage;
    if (stage != null) {
      this.stageDisplayName = stage.getDisplayName();
      this.stageColor = stage.getColor();
    }
  }

  public String getStageDisplayName() {
    return stageDisplayName;
  }

  public String getStageColor() {
    return stageColor;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public void setCustomerId(UUID customerId) {
    this.customerId = customerId;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public Long getLeadId() {
    return leadId;
  }

  public void setLeadId(Long leadId) {
    this.leadId = leadId;
  }

  public String getLeadCompanyName() {
    return leadCompanyName;
  }

  public void setLeadCompanyName(String leadCompanyName) {
    this.leadCompanyName = leadCompanyName;
  }

  public UUID getAssignedToId() {
    return assignedToId;
  }

  public void setAssignedToId(UUID assignedToId) {
    this.assignedToId = assignedToId;
  }

  public String getAssignedToName() {
    return assignedToName;
  }

  public void setAssignedToName(String assignedToName) {
    this.assignedToName = assignedToName;
  }

  public BigDecimal getExpectedValue() {
    return expectedValue;
  }

  public void setExpectedValue(BigDecimal expectedValue) {
    this.expectedValue = expectedValue;
  }

  public LocalDate getExpectedCloseDate() {
    return expectedCloseDate;
  }

  public void setExpectedCloseDate(LocalDate expectedCloseDate) {
    this.expectedCloseDate = expectedCloseDate;
  }

  public Integer getProbability() {
    return probability;
  }

  public void setProbability(Integer probability) {
    this.probability = probability;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getStageChangedAt() {
    return stageChangedAt;
  }

  public void setStageChangedAt(LocalDateTime stageChangedAt) {
    this.stageChangedAt = stageChangedAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public List<OpportunityActivityResponse> getActivities() {
    return activities;
  }

  public void setActivities(List<OpportunityActivityResponse> activities) {
    this.activities = activities;
  }
}
