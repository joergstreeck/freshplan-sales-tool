package de.freshplan.domain.opportunity.service.dto;

import de.freshplan.domain.opportunity.entity.OpportunityStage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for creating an Opportunity for an existing Customer.
 *
 * <p>Used for Upsell, Cross-sell, and Renewal opportunities. All fields are optional - name and
 * description will be auto-generated if not provided.
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.6.2 Phase 2
 */
public class CreateOpportunityForCustomerRequest {

  /** Opportunity name (optional - auto-generated if null) */
  private String name;

  /** Opportunity description (optional - auto-generated if null) */
  private String description;

  /** Initial stage (optional - defaults to NEEDS_ANALYSIS for customer opportunities) */
  private OpportunityStage stage;

  /** Opportunity type (e.g., "Upsell", "Cross-sell", "Renewal", "Expansion") */
  private String opportunityType;

  /** Expected timeframe (e.g., "Q3 2025", "2025-08", "H2 2025") */
  private String timeframe;

  /** Expected deal value in EUR */
  private BigDecimal expectedValue;

  /** Expected close date */
  private LocalDate expectedCloseDate;

  /** Assigned user ID (optional - can be set later) */
  private UUID assignedTo;

  // Constructors

  public CreateOpportunityForCustomerRequest() {}

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

  public OpportunityStage getStage() {
    return stage;
  }

  public void setStage(OpportunityStage stage) {
    this.stage = stage;
  }

  public String getOpportunityType() {
    return opportunityType;
  }

  public void setOpportunityType(String opportunityType) {
    this.opportunityType = opportunityType;
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

  // Builder Pattern

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final CreateOpportunityForCustomerRequest request =
        new CreateOpportunityForCustomerRequest();

    public Builder name(String name) {
      request.name = name;
      return this;
    }

    public Builder description(String description) {
      request.description = description;
      return this;
    }

    public Builder stage(OpportunityStage stage) {
      request.stage = stage;
      return this;
    }

    public Builder opportunityType(String opportunityType) {
      request.opportunityType = opportunityType;
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

    public CreateOpportunityForCustomerRequest build() {
      return request;
    }
  }

  @Override
  public String toString() {
    return "CreateOpportunityForCustomerRequest{"
        + "name='"
        + name
        + '\''
        + ", opportunityType='"
        + opportunityType
        + '\''
        + ", timeframe='"
        + timeframe
        + '\''
        + ", expectedValue="
        + expectedValue
        + ", expectedCloseDate="
        + expectedCloseDate
        + '}';
  }
}
