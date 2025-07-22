package de.freshplan.domain.opportunity.service.dto;

import de.freshplan.domain.opportunity.entity.OpportunityStage;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO für Stage-Änderungen von Opportunities
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class ChangeStageRequest {

  @NotNull(message = "Stage cannot be null") private OpportunityStage stage;

  @Min(value = 0, message = "Probability must be between 0 and 100")
  @Max(value = 100, message = "Probability must be between 0 and 100")
  private Integer customProbability;

  private String reason;

  // Constructors
  public ChangeStageRequest() {}

  private ChangeStageRequest(Builder builder) {
    this.stage = builder.stage;
    this.customProbability = builder.customProbability;
    this.reason = builder.reason;
  }

  // Getters
  public OpportunityStage getStage() {
    return stage;
  }

  public Integer getCustomProbability() {
    return customProbability;
  }

  public String getReason() {
    return reason;
  }

  // Setters
  public void setStage(OpportunityStage stage) {
    this.stage = stage;
  }

  public void setCustomProbability(Integer customProbability) {
    this.customProbability = customProbability;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  // Builder
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private OpportunityStage stage;
    private Integer customProbability;
    private String reason;

    public Builder stage(OpportunityStage stage) {
      this.stage = stage;
      return this;
    }

    public Builder customProbability(Integer customProbability) {
      this.customProbability = customProbability;
      return this;
    }

    public Builder reason(String reason) {
      this.reason = reason;
      return this;
    }

    public ChangeStageRequest build() {
      return new ChangeStageRequest(this);
    }
  }
}
