package de.freshplan.domain.help.service.query;

import de.freshplan.domain.help.entity.HelpContent;
import de.freshplan.domain.help.entity.HelpType;
import de.freshplan.domain.help.entity.UserLevel;
import de.freshplan.domain.help.repository.HelpContentRepository;
import de.freshplan.domain.help.service.HelpAnalyticsService;
import de.freshplan.domain.help.service.UserStruggleDetectionService;
import de.freshplan.domain.help.service.dto.HelpAnalytics;
import de.freshplan.domain.help.service.dto.HelpRequest;
import de.freshplan.domain.help.service.dto.HelpResponse;
import de.freshplan.domain.help.service.dto.UserStruggle;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CQRS Query Service für Help Content Management - Read Operations
 *
 * <p>Handles all read operations for help content management: - Help content retrieval and
 * selection - Content search and filtering - Analytics and reporting - Feature coverage analysis
 *
 * <p>This service provides pure read operations without side effects, following CQRS principles for
 * optimal query performance.
 *
 * <p>Part of Phase 12.2 CQRS migration from HelpContentService.
 *
 * @since Phase 12.2 CQRS Migration
 */
@ApplicationScoped
public class HelpContentQueryService {

  private static final Logger LOG = LoggerFactory.getLogger(HelpContentQueryService.class);

  @Inject HelpContentRepository helpRepository;

  @Inject UserStruggleDetectionService struggleDetectionService;

  @Inject HelpAnalyticsService analyticsService;

  /**
   * Gets the best help content for a feature request.
   *
   * <p>This is a PURE QUERY operation - no side effects like view count updates. The caller is
   * responsible for triggering view count updates via Command service.
   *
   * @param request The help request with context
   * @return HelpResponse with selected content, or empty response if none found
   */
  public HelpResponse getHelpForFeature(HelpRequest request) {
    LOG.debug(
        "Getting help for feature: {} (user: {}, level: {})",
        request.feature(),
        request.userId(),
        request.userLevel());

    // 1. Standard Hilfe-Inhalte laden
    List<HelpContent> contents =
        helpRepository.findForUserLevel(
            request.feature(), request.userLevel(), request.userRoles());

    // 2. Struggle Detection - proaktive Hilfe?
    UserStruggle struggle =
        struggleDetectionService.detectStruggle(
            request.userId(), request.feature(), request.context());

    // 3. Adaptive Content Selection
    HelpContent selectedContent = selectBestContent(contents, request, struggle);

    if (selectedContent != null) {
      // NOTE: NO view count increment here - pure query!
      // View count will be updated by Command service after this call
      return buildHelpResponse(selectedContent, struggle, request);
    }

    LOG.warn(
        "No help content found for feature: {} (user level: {})",
        request.feature(),
        request.userLevel());

    return HelpResponse.empty(request.feature());
  }

  /**
   * Searches help content based on search term and user context.
   *
   * @param searchTerm The search query
   * @param userLevel User's experience level
   * @param userRoles User's roles for content filtering
   * @return List of matching help responses
   */
  public List<HelpResponse> searchHelp(
      String searchTerm, String userLevel, List<String> userRoles) {
    LOG.debug("Searching help content for: '{}' (level: {})", searchTerm, userLevel);

    List<HelpContent> results = helpRepository.searchContent(searchTerm, userLevel, userRoles);

    return results.stream()
        .map(
            content ->
                HelpResponse.builder()
                    .id(content.id)
                    .feature(content.feature)
                    .title(content.title)
                    .type(content.helpType)
                    .content(selectContentByLevel(content, userLevel))
                    .videoUrl(content.videoUrl)
                    .helpfulnessRate(content.getHelpfulnessRate())
                    .viewCount(content.viewCount)
                    .build())
        .toList();
  }

  /**
   * Gets overall help system analytics.
   *
   * @return Comprehensive analytics data
   */
  public HelpAnalytics getAnalytics() {
    return analyticsService.getOverallAnalytics();
  }

  /**
   * Identifies features that lack adequate help content coverage.
   *
   * @return List of features needing help content
   */
  public List<String> getFeatureCoverageGaps() {
    List<String> allFeatures = helpRepository.getFeaturesWithHelp();
    List<String> tooltipGaps = helpRepository.getFeaturesWithoutType(HelpType.TOOLTIP);
    List<String> tourGaps = helpRepository.getFeaturesWithoutType(HelpType.TOUR);

    LOG.debug(
        "Help coverage: {} features total, {} without tooltips, {} without tours",
        allFeatures.size(),
        tooltipGaps.size(),
        tourGaps.size());

    return tourGaps; // Tours sind kritischer als Tooltips
  }

  /**
   * Gets detailed help content for administrative purposes.
   *
   * @param helpId The help content ID
   * @return Optional help content with full details
   */
  public Optional<HelpContent> getHelpContentDetails(java.util.UUID helpId) {
    return helpRepository.findByIdOptional(helpId);
  }

  /**
   * Gets all help content for a specific feature.
   *
   * @param feature The feature name
   * @return List of all help content for the feature
   */
  public List<HelpContent> getHelpContentByFeature(String feature) {
    return helpRepository.findActiveByFeature(feature);
  }

  /**
   * Gets help content statistics for performance monitoring.
   *
   * @return Content statistics
   */
  public HelpContentStats getContentStatistics() {
    // Use available repository methods
    long totalViews = helpRepository.getTotalViews();
    double avgHelpfulness = helpRepository.getOverallHelpfulnessRate();
    List<HelpContent> mostRequested = helpRepository.findMostRequested(10);
    List<String> topFeatures =
        mostRequested.stream().map(content -> content.feature).distinct().toList();

    return HelpContentStats.builder()
        .totalActiveContent(mostRequested.size()) // Approximation
        .totalViews(totalViews)
        .averageHelpfulness(avgHelpfulness)
        .topRequestedFeatures(topFeatures)
        .lastUpdated(java.time.LocalDateTime.now())
        .build();
  }

  // Private Helper Methods - exakte Kopie der Original-Logic

  /** Wählt den besten Hilfe-Inhalt basierend auf Kontext aus (exakte Kopie) */
  private HelpContent selectBestContent(
      List<HelpContent> contents, HelpRequest request, UserStruggle struggle) {
    if (contents.isEmpty()) {
      return null;
    }

    // Bei Struggle: Passende proaktive Hilfe bevorzugen
    if (struggle.isDetected()) {
      Optional<HelpContent> proactiveHelp =
          contents.stream()
              .filter(c -> c.helpType == HelpType.PROACTIVE || c.helpType == HelpType.CORRECTION)
              .findFirst();

      if (proactiveHelp.isPresent()) {
        LOG.info(
            "Providing proactive help for struggle: {} (feature: {})",
            struggle.getType(),
            request.feature());
        return proactiveHelp.get();
      }
    }

    // Erste Nutzung: First-Time Hilfe
    if (request.isFirstTime()) {
      Optional<HelpContent> firstTimeHelp =
          contents.stream()
              .filter(c -> c.helpType == HelpType.FIRST_TIME || c.helpType == HelpType.TOUR)
              .findFirst();

      if (firstTimeHelp.isPresent()) {
        return firstTimeHelp.get();
      }
    }

    // Spezifischer Typ angefragt
    if (request.preferredType() != null) {
      Optional<HelpContent> typeSpecific =
          contents.stream().filter(c -> c.helpType == request.preferredType()).findFirst();

      if (typeSpecific.isPresent()) {
        return typeSpecific.get();
      }
    }

    // Default: Besten verfügbaren Content (nach Priority und Helpfulness)
    return contents.stream()
        .filter(c -> c.helpType == HelpType.TOOLTIP || c.helpType == HelpType.TUTORIAL)
        .min(
            (a, b) -> {
              // Erst nach Priority, dann nach Hilfreichkeit
              int priorityCompare = Integer.compare(a.priority, b.priority);
              if (priorityCompare != 0) return priorityCompare;

              return Double.compare(b.getHelpfulnessRate(), a.getHelpfulnessRate());
            })
        .orElse(contents.get(0));
  }

  /** Baut die Help Response auf (exakte Kopie) */
  private HelpResponse buildHelpResponse(
      HelpContent content, UserStruggle struggle, HelpRequest request) {
    HelpResponse.Builder builder =
        HelpResponse.builder()
            .id(content.id)
            .feature(content.feature)
            .title(content.title)
            .type(content.helpType)
            .priority(content.priority);

    // Content basierend auf User Level und Preference
    String selectedContent = selectContentByLevel(content, request.userLevel());
    builder.content(selectedContent);

    // Video URL falls vorhanden
    if (content.videoUrl != null && !content.videoUrl.isEmpty()) {
      builder.videoUrl(content.videoUrl);
    }

    // Interaction Data (für Tours, etc.)
    if (content.interactionData != null) {
      builder.interactionData(content.interactionData);
    }

    // Struggle-spezifische Anpassungen
    if (struggle.isDetected()) {
      builder
          .struggleDetected(true)
          .struggleType(struggle.getType().toString())
          .suggestionLevel(struggle.getSeverity());
    }

    // Analytics Daten
    builder.viewCount(content.viewCount).helpfulnessRate(content.getHelpfulnessRate());

    return builder.build();
  }

  /** Wählt passenden Content für User Level (exakte Kopie) */
  private String selectContentByLevel(HelpContent content, String userLevel) {
    UserLevel level = UserLevel.valueOf(userLevel.toUpperCase());

    return switch (level) {
      case BEGINNER -> {
        // Beginners bekommen detailed content wenn verfügbar
        if (content.detailedContent != null && !content.detailedContent.isEmpty()) {
          yield content.detailedContent;
        }
        if (content.mediumContent != null && !content.mediumContent.isEmpty()) {
          yield content.mediumContent;
        }
        yield content.shortContent;
      }
      case INTERMEDIATE -> {
        // Intermediate users bekommen medium content
        if (content.mediumContent != null && !content.mediumContent.isEmpty()) {
          yield content.mediumContent;
        }
        if (content.shortContent != null && !content.shortContent.isEmpty()) {
          yield content.shortContent;
        }
        yield content.detailedContent;
      }
      case EXPERT -> {
        // Experts bekommen kurze, prägnante Hilfe
        if (content.shortContent != null && !content.shortContent.isEmpty()) {
          yield content.shortContent;
        }
        yield content.mediumContent;
      }
    };
  }

  // DTO for content statistics
  public static class HelpContentStats {
    private final long totalActiveContent;
    private final long totalViews;
    private final double averageHelpfulness;
    private final List<String> topRequestedFeatures;
    private final java.time.LocalDateTime lastUpdated;

    private HelpContentStats(Builder builder) {
      this.totalActiveContent = builder.totalActiveContent;
      this.totalViews = builder.totalViews;
      this.averageHelpfulness = builder.averageHelpfulness;
      this.topRequestedFeatures = builder.topRequestedFeatures;
      this.lastUpdated = builder.lastUpdated;
    }

    public static Builder builder() {
      return new Builder();
    }

    // Getters
    public long getTotalActiveContent() {
      return totalActiveContent;
    }

    public long getTotalViews() {
      return totalViews;
    }

    public double getAverageHelpfulness() {
      return averageHelpfulness;
    }

    public List<String> getTopRequestedFeatures() {
      return topRequestedFeatures;
    }

    public java.time.LocalDateTime getLastUpdated() {
      return lastUpdated;
    }

    public static class Builder {
      private long totalActiveContent;
      private long totalViews;
      private double averageHelpfulness;
      private List<String> topRequestedFeatures;
      private java.time.LocalDateTime lastUpdated;

      public Builder totalActiveContent(long totalActiveContent) {
        this.totalActiveContent = totalActiveContent;
        return this;
      }

      public Builder totalViews(long totalViews) {
        this.totalViews = totalViews;
        return this;
      }

      public Builder averageHelpfulness(double averageHelpfulness) {
        this.averageHelpfulness = averageHelpfulness;
        return this;
      }

      public Builder topRequestedFeatures(List<String> topRequestedFeatures) {
        this.topRequestedFeatures = topRequestedFeatures;
        return this;
      }

      public Builder lastUpdated(java.time.LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
      }

      public HelpContentStats build() {
        return new HelpContentStats(this);
      }
    }
  }
}
