package de.freshplan.domain.help.service.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Analytics DTO für Help System Metriken */
public record HelpAnalytics(
    long totalViews,
    long totalFeedback,
    double overallHelpfulnessRate,
    List<TopTopic> mostRequestedTopics,
    List<TopTopic> mostHelpfulContent,
    List<TopTopic> contentNeedingImprovement,
    int featureCoverage,
    List<String> coverageGaps,
    Map<String, Integer> helpRequestsByFeature,
    Map<String, Integer> struggleDetectionsByType,
    double averageResponseTime,
    double userSatisfactionScore) {

  public record TopTopic(UUID id, String title, String feature, int requests, double helpfulRate) {
    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private UUID id;
      private String title;
      private String feature;
      private int requests;
      private double helpfulRate;

      public Builder id(UUID id) {
        this.id = id;
        return this;
      }

      public Builder title(String title) {
        this.title = title;
        return this;
      }

      public Builder feature(String feature) {
        this.feature = feature;
        return this;
      }

      public Builder requests(int requests) {
        this.requests = requests;
        return this;
      }

      public Builder helpfulRate(double helpfulRate) {
        this.helpfulRate = helpfulRate;
        return this;
      }

      public TopTopic build() {
        return new TopTopic(id, title, feature, requests, helpfulRate);
      }
    }
  }

  public record FeatureAnalytics(
      String feature,
      int contentCount,
      long totalViews,
      long totalFeedback,
      double averageHelpfulness,
      int helpRequests,
      int struggleDetections,
      double strugglesPerRequest,
      LocalDateTime lastUpdated) {
    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private String feature;
      private int contentCount;
      private long totalViews;
      private long totalFeedback;
      private double averageHelpfulness;
      private int helpRequests;
      private int struggleDetections;
      private double strugglesPerRequest;
      private LocalDateTime lastUpdated;

      public Builder feature(String feature) {
        this.feature = feature;
        return this;
      }

      public Builder contentCount(int contentCount) {
        this.contentCount = contentCount;
        return this;
      }

      public Builder totalViews(long totalViews) {
        this.totalViews = totalViews;
        return this;
      }

      public Builder totalFeedback(long totalFeedback) {
        this.totalFeedback = totalFeedback;
        return this;
      }

      public Builder averageHelpfulness(double averageHelpfulness) {
        this.averageHelpfulness = averageHelpfulness;
        return this;
      }

      public Builder helpRequests(int helpRequests) {
        this.helpRequests = helpRequests;
        return this;
      }

      public Builder struggleDetections(int struggleDetections) {
        this.struggleDetections = struggleDetections;
        return this;
      }

      public Builder strugglesPerRequest(double strugglesPerRequest) {
        this.strugglesPerRequest = strugglesPerRequest;
        return this;
      }

      public Builder lastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
      }

      public FeatureAnalytics build() {
        return new FeatureAnalytics(
            feature,
            contentCount,
            totalViews,
            totalFeedback,
            averageHelpfulness,
            helpRequests,
            struggleDetections,
            strugglesPerRequest,
            lastUpdated);
      }
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private long totalViews;
    private long totalFeedback;
    private double overallHelpfulnessRate;
    private List<TopTopic> mostRequestedTopics = List.of();
    private List<TopTopic> mostHelpfulContent = List.of();
    private List<TopTopic> contentNeedingImprovement = List.of();
    private int featureCoverage;
    private List<String> coverageGaps = List.of();
    private Map<String, Integer> helpRequestsByFeature = Map.of();
    private Map<String, Integer> struggleDetectionsByType = Map.of();
    private double averageResponseTime;
    private double userSatisfactionScore;

    public Builder totalViews(long totalViews) {
      this.totalViews = totalViews;
      return this;
    }

    public Builder totalFeedback(long totalFeedback) {
      this.totalFeedback = totalFeedback;
      return this;
    }

    public Builder overallHelpfulnessRate(double overallHelpfulnessRate) {
      this.overallHelpfulnessRate = overallHelpfulnessRate;
      return this;
    }

    public Builder mostRequestedTopics(List<TopTopic> mostRequestedTopics) {
      this.mostRequestedTopics = mostRequestedTopics;
      return this;
    }

    public Builder mostHelpfulContent(List<TopTopic> mostHelpfulContent) {
      this.mostHelpfulContent = mostHelpfulContent;
      return this;
    }

    public Builder contentNeedingImprovement(List<TopTopic> contentNeedingImprovement) {
      this.contentNeedingImprovement = contentNeedingImprovement;
      return this;
    }

    public Builder featureCoverage(int featureCoverage) {
      this.featureCoverage = featureCoverage;
      return this;
    }

    public Builder coverageGaps(List<String> coverageGaps) {
      this.coverageGaps = coverageGaps;
      return this;
    }

    public Builder helpRequestsByFeature(Map<String, Integer> helpRequestsByFeature) {
      this.helpRequestsByFeature = helpRequestsByFeature;
      return this;
    }

    public Builder struggleDetectionsByType(Map<String, Integer> struggleDetectionsByType) {
      this.struggleDetectionsByType = struggleDetectionsByType;
      return this;
    }

    public Builder averageResponseTime(double averageResponseTime) {
      this.averageResponseTime = averageResponseTime;
      return this;
    }

    public Builder userSatisfactionScore(double userSatisfactionScore) {
      this.userSatisfactionScore = userSatisfactionScore;
      return this;
    }

    public HelpAnalytics build() {
      return new HelpAnalytics(
          totalViews,
          totalFeedback,
          overallHelpfulnessRate,
          mostRequestedTopics,
          mostHelpfulContent,
          contentNeedingImprovement,
          featureCoverage,
          coverageGaps,
          helpRequestsByFeature,
          struggleDetectionsByType,
          averageResponseTime,
          userSatisfactionScore);
    }
  }
}
