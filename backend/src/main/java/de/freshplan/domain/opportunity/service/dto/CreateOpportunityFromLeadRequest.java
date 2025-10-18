package de.freshplan.domain.opportunity.service.dto;

import de.freshplan.domain.opportunity.entity.OpportunityType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for creating an Opportunity from a Lead.
 *
 * <p>Used in Lead → Opportunity conversion workflow. All fields are optional except leadId (passed
 * separately). If name is null, it will be auto-generated from lead data.
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.6.2 Phase 2
 */
public class CreateOpportunityFromLeadRequest {

  /** Opportunity name (optional - auto-generated from lead if null) */
  private String name;

  /** Opportunity description (optional) */
  private String description;

  /** Opportunity Type - Sprint 2.1.7.1 (Freshfoodz Business Type - defaults to NEUGESCHAEFT if not provided) */
  private OpportunityType opportunityType;

  /** Deal type (e.g., "Liefervertrag", "Erstauftrag", "Rahmenvertrag") */
  private String dealType;

  /** Expected timeframe (e.g., "Q2 2025", "2025-06", "Sofort") */
  private String timeframe;

  /** Expected deal value in EUR */
  private BigDecimal expectedValue;

  /** Expected close date */
  private LocalDate expectedCloseDate;

  /** Custom assignee (optional - defaults to lead owner) */
  private UUID assignedTo;

  // Constructors

  public CreateOpportunityFromLeadRequest() {}

  // Getters and Setters

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

  public OpportunityType getOpportunityType() {
    return opportunityType;
  }

  public void setOpportunityType(OpportunityType opportunityType) {
    this.opportunityType = opportunityType;
  }

  public String getDealType() {
    return dealType;
  }

  public void setDealType(String dealType) {
    this.dealType = dealType;
  }

  public String getTimeframe() {
    return timeframe;
  }

  public void setTimeframe(String timeframe) {
    this.timeframe = timeframe;
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

  public UUID getAssignedTo() {
    return assignedTo;
  }

  public void setAssignedTo(UUID assignedTo) {
    this.assignedTo = assignedTo;
  }

  // Builder pattern (optional but convenient)

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final CreateOpportunityFromLeadRequest request = new CreateOpportunityFromLeadRequest();

    public Builder name(String name) {
      request.name = name;
      return this;
    }

    public Builder description(String description) {
      request.description = description;
      return this;
    }

    public Builder opportunityType(OpportunityType opportunityType) {
      request.opportunityType = opportunityType;
      return this;
    }

    public Builder dealType(String dealType) {
      request.dealType = dealType;
      return this;
    }

    public Builder timeframe(String timeframe) {
      request.timeframe = timeframe;
      return this;
    }

    public Builder expectedValue(BigDecimal expectedValue) {
      request.expectedValue = expectedValue;
      return this;
    }

    public Builder expectedCloseDate(LocalDate expectedCloseDate) {
      request.expectedCloseDate = expectedCloseDate;
      return this;
    }

    public Builder assignedTo(UUID assignedTo) {
      request.assignedTo = assignedTo;
      return this;
    }

    public CreateOpportunityFromLeadRequest build() {
      return request;
    }
  }

  @Override
  public String toString() {
    return "CreateOpportunityFromLeadRequest{"
        + "name='"
        + name
        + '\''
        + ", dealType='"
        + dealType
        + '\''
        + ", timeframe='"
        + timeframe
        + '\''
        + ", expectedValue="
        + expectedValue
        + ", expectedCloseDate="
        + expectedCloseDate
        + ", assignedTo="
        + assignedTo
        + '}';
  }
}
