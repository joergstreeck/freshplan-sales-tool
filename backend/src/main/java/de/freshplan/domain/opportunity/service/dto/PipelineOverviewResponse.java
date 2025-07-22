package de.freshplan.domain.opportunity.service.dto;

import de.freshplan.domain.opportunity.entity.OpportunityStage;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO für Pipeline-Übersicht mit Statistiken
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class PipelineOverviewResponse {

  private List<StageStatistic> stageStatistics;
  private BigDecimal totalForecast;
  private Double conversionRate;
  private List<OpportunityResponse> highPriorityOpportunities;
  private List<OpportunityResponse> overdueOpportunities;

  // Default constructor
  public PipelineOverviewResponse() {}

  // Builder pattern
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private PipelineOverviewResponse response = new PipelineOverviewResponse();

    public Builder stageStatistics(List<StageStatistic> stageStatistics) {
      response.stageStatistics = stageStatistics;
      return this;
    }

    public Builder totalForecast(BigDecimal totalForecast) {
      response.totalForecast = totalForecast;
      return this;
    }

    public Builder conversionRate(Double conversionRate) {
      response.conversionRate = conversionRate;
      return this;
    }

    public Builder highPriorityOpportunities(List<OpportunityResponse> highPriorityOpportunities) {
      response.highPriorityOpportunities = highPriorityOpportunities;
      return this;
    }

    public Builder overdueOpportunities(List<OpportunityResponse> overdueOpportunities) {
      response.overdueOpportunities = overdueOpportunities;
      return this;
    }

    public PipelineOverviewResponse build() {
      return response;
    }
  }

  /** Statistik für eine einzelne Pipeline-Stage */
  public static class StageStatistic {
    private OpportunityStage stage;
    private String stageDisplayName;
    private String stageColor;
    private Long count;
    private BigDecimal totalValue;

    // Default constructor
    public StageStatistic() {}

    // Builder pattern
    public static StageStatistic.Builder builder() {
      return new StageStatistic.Builder();
    }

    public static class Builder {
      private StageStatistic statistic = new StageStatistic();

      public Builder stage(OpportunityStage stage) {
        statistic.stage = stage;
        if (stage != null) {
          statistic.stageDisplayName = stage.getDisplayName();
          statistic.stageColor = stage.getColor();
        }
        return this;
      }

      public Builder count(Long count) {
        statistic.count = count;
        return this;
      }

      public Builder totalValue(BigDecimal totalValue) {
        statistic.totalValue = totalValue;
        return this;
      }

      public StageStatistic build() {
        return statistic;
      }
    }

    // Getters und Setters
    public OpportunityStage getStage() {
      return stage;
    }

    public String getStageDisplayName() {
      return stageDisplayName;
    }

    public String getStageColor() {
      return stageColor;
    }

    public Long getCount() {
      return count;
    }

    public BigDecimal getTotalValue() {
      return totalValue;
    }
  }

  // Getters und Setters
  public List<StageStatistic> getStageStatistics() {
    return stageStatistics;
  }

  public void setStageStatistics(List<StageStatistic> stageStatistics) {
    this.stageStatistics = stageStatistics;
  }

  public BigDecimal getTotalForecast() {
    return totalForecast;
  }

  public void setTotalForecast(BigDecimal totalForecast) {
    this.totalForecast = totalForecast;
  }

  public Double getConversionRate() {
    return conversionRate;
  }

  public void setConversionRate(Double conversionRate) {
    this.conversionRate = conversionRate;
  }

  public List<OpportunityResponse> getHighPriorityOpportunities() {
    return highPriorityOpportunities;
  }

  public void setHighPriorityOpportunities(List<OpportunityResponse> highPriorityOpportunities) {
    this.highPriorityOpportunities = highPriorityOpportunities;
  }

  public List<OpportunityResponse> getOverdueOpportunities() {
    return overdueOpportunities;
  }

  public void setOverdueOpportunities(List<OpportunityResponse> overdueOpportunities) {
    this.overdueOpportunities = overdueOpportunities;
  }
}
