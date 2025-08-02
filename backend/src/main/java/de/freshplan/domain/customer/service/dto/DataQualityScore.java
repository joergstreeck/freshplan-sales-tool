package de.freshplan.domain.customer.service.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO für umfassende Datenqualitätsbewertung.
 *
 * <p>Beinhaltet Score, Empfehlungen und Freshness-Informationen basierend auf dem Data Strategy
 * Intelligence Konzept.
 */
public class DataQualityScore {

  private Integer score;
  private List<String> recommendations;
  private DataFreshnessLevel freshnessLevel;
  private LocalDateTime lastCalculated;
  private String overallAssessment;

  public DataQualityScore() {}

  private DataQualityScore(Builder builder) {
    this.score = builder.score;
    this.recommendations = builder.recommendations;
    this.freshnessLevel = builder.freshnessLevel;
    this.lastCalculated = builder.lastCalculated;
    this.overallAssessment = generateOverallAssessment(score, freshnessLevel);
  }

  public static Builder builder() {
    return new Builder();
  }

  public Integer getScore() {
    return score;
  }

  public void setScore(Integer score) {
    this.score = score;
  }

  public List<String> getRecommendations() {
    return recommendations;
  }

  public void setRecommendations(List<String> recommendations) {
    this.recommendations = recommendations;
  }

  public DataFreshnessLevel getFreshnessLevel() {
    return freshnessLevel;
  }

  public void setFreshnessLevel(DataFreshnessLevel freshnessLevel) {
    this.freshnessLevel = freshnessLevel;
  }

  public LocalDateTime getLastCalculated() {
    return lastCalculated;
  }

  public void setLastCalculated(LocalDateTime lastCalculated) {
    this.lastCalculated = lastCalculated;
  }

  public String getOverallAssessment() {
    return overallAssessment;
  }

  public void setOverallAssessment(String overallAssessment) {
    this.overallAssessment = overallAssessment;
  }

  /** Generiert eine textuelle Gesamtbewertung. */
  private String generateOverallAssessment(Integer score, DataFreshnessLevel freshnessLevel) {
    if (score == null) {
      return "Nicht bewertet";
    }

    String qualityAssessment;
    if (score >= 90) {
      qualityAssessment = "Exzellent";
    } else if (score >= 75) {
      qualityAssessment = "Gut";
    } else if (score >= 60) {
      qualityAssessment = "Befriedigend";
    } else if (score >= 40) {
      qualityAssessment = "Mangelhaft";
    } else {
      qualityAssessment = "Kritisch";
    }

    if (freshnessLevel != null && freshnessLevel == DataFreshnessLevel.CRITICAL) {
      return qualityAssessment + " (Daten veraltet)";
    }

    return qualityAssessment;
  }

  /** Prüft ob sofortiges Handeln erforderlich ist. */
  public boolean requiresImmediateAction() {
    return (score != null && score < 40) || (freshnessLevel == DataFreshnessLevel.CRITICAL);
  }

  /** Gibt die Priorität für Updates zurück. */
  public String getUpdatePriority() {
    if (requiresImmediateAction()) {
      return "HIGH";
    } else if (score != null && score < 60) {
      return "MEDIUM";
    } else if (freshnessLevel == DataFreshnessLevel.STALE) {
      return "MEDIUM";
    } else if (freshnessLevel == DataFreshnessLevel.AGING) {
      return "LOW";
    } else {
      return "NONE";
    }
  }

  public static class Builder {
    private Integer score;
    private List<String> recommendations;
    private DataFreshnessLevel freshnessLevel;
    private LocalDateTime lastCalculated;

    public Builder score(Integer score) {
      this.score = score;
      return this;
    }

    public Builder recommendations(List<String> recommendations) {
      this.recommendations = recommendations;
      return this;
    }

    public Builder freshnessLevel(DataFreshnessLevel freshnessLevel) {
      this.freshnessLevel = freshnessLevel;
      return this;
    }

    public Builder lastCalculated(LocalDateTime lastCalculated) {
      this.lastCalculated = lastCalculated;
      return this;
    }

    public DataQualityScore build() {
      return new DataQualityScore(this);
    }
  }

  @Override
  public String toString() {
    return "DataQualityScore{"
        + "score="
        + score
        + ", freshnessLevel="
        + freshnessLevel
        + ", overallAssessment='"
        + overallAssessment
        + '\''
        + ", requiresAction="
        + requiresImmediateAction()
        + '}';
  }
}
