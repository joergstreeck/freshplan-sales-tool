package de.freshplan.domain.help.service;

import de.freshplan.domain.help.entity.HelpContent;
import de.freshplan.domain.help.entity.HelpType;
import de.freshplan.domain.help.entity.UserLevel;
import de.freshplan.domain.help.events.HelpContentViewedEvent;
import de.freshplan.domain.help.repository.HelpContentRepository;
import de.freshplan.domain.help.service.command.HelpContentCommandService;
import de.freshplan.domain.help.service.dto.HelpAnalytics;
import de.freshplan.domain.help.service.dto.HelpRequest;
import de.freshplan.domain.help.service.dto.HelpResponse;
import de.freshplan.domain.help.service.dto.UserStruggle;
import de.freshplan.domain.help.service.query.HelpContentQueryService;
import de.freshplan.infrastructure.events.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FACADE Service für Help Content Management - CQRS Migration mit Feature Flag
 *
 * <p>Dieses Service agiert als Facade während der CQRS-Migration und delegiert
 * an die neuen Command und Query Services basierend auf dem Feature Flag.
 * 
 * <p>Zentrale Business Logic für: - Kontextuelle Hilfe-Bereitstellung - User Struggle Detection -
 * Hilfe-Analytics und Feedback - Adaptive Help Delivery
 * 
 * <p>Part of Phase 12.2 CQRS migration.
 *
 * @since Phase 12.2 CQRS Migration
 */
@ApplicationScoped
@Transactional
public class HelpContentService {

  private static final Logger LOG = LoggerFactory.getLogger(HelpContentService.class);

  @ConfigProperty(name = "features.cqrs.enabled", defaultValue = "false")
  boolean cqrsEnabled;

  @Inject HelpContentCommandService commandService;
  
  @Inject HelpContentQueryService queryService;

  @Inject EventBus eventBus;

  // Legacy Dependencies (when CQRS disabled)
  @Inject HelpContentRepository helpRepository;

  @Inject UserStruggleDetectionService struggleDetectionService;

  @Inject HelpAnalyticsService analyticsService;

  /** Holt die beste Hilfe für eine Feature-Anfrage */
  public HelpResponse getHelpForFeature(HelpRequest request) {
    if (cqrsEnabled) {
      LOG.debug("Using CQRS Event-Driven implementation for help content delivery");
      
      // EVENT-DRIVEN APPROACH: Pure Query + Async Event for Side Effects
      
      // 1. Get help content (Pure Query - no side effects)
      HelpResponse helpResponse = queryService.getHelpForFeature(request);
      
      // 2. Publish event for side effects (asynchronous)
      if (helpResponse.id() != null) {
        UserStruggle struggle = struggleDetectionService.detectStruggle(
            request.userId(), request.feature(), request.context());
        
        HelpContentViewedEvent event = HelpContentViewedEvent.create(
            helpResponse.id(), request, struggle);
        
        // Publish asynchronously - no blocking, no transaction coupling
        eventBus.publishAsync(event);
        
        LOG.debug("Published HelpContentViewedEvent for content: {} by user: {}", 
            helpResponse.id(), request.userId());
      }
      
      return helpResponse;
    }

    LOG.debug("Using legacy implementation for help content delivery");
    
    // Legacy implementation (exakte Kopie des Original-Codes)
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
      // View tracking
      selectedContent.incrementViewCount();
      helpRepository.persist(selectedContent);

      // Analytics
      analyticsService.trackHelpRequest(request, selectedContent, struggle);

      return buildHelpResponse(selectedContent, struggle, request);
    }

    LOG.warn(
        "No help content found for feature: {} (user level: {})",
        request.feature(),
        request.userLevel());

    return HelpResponse.empty(request.feature());
  }

  /** Wählt den besten Hilfe-Inhalt basierend auf Kontext aus */
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

  /** Baut die Help Response auf */
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

  /** Wählt passenden Content für User Level */
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

  /**
   * Helper method to create a minimal HelpContent object from HelpResponse for analytics.
   */
  private HelpContent createContentFromResponse(HelpResponse response) {
    // Create a minimal HelpContent object for analytics tracking
    // This is only used in CQRS mode when we need to pass a HelpContent to analytics
    HelpContent content = new HelpContent();
    content.id = response.id();
    content.feature = response.feature();
    content.title = response.title();
    content.helpType = response.type();
    content.viewCount = response.viewCount();
    return content;
  }

  /** Registriert User Feedback für Hilfe-Inhalt */
  @Transactional
  public void recordFeedback(
      UUID helpId, String userId, boolean helpful, Integer timeSpent, String comment) {
    if (cqrsEnabled) {
      LOG.debug("Using CQRS implementation for feedback recording");
      commandService.recordFeedback(helpId, userId, helpful, timeSpent, comment);
      return;
    }
    LOG.debug(
        "Recording feedback for help {}: helpful={}, timeSpent={}", helpId, helpful, timeSpent);

    Optional<HelpContent> contentOpt = helpRepository.findByIdOptional(helpId);
    if (contentOpt.isEmpty()) {
      LOG.warn("Help content not found for feedback: {}", helpId);
      return;
    }

    HelpContent content = contentOpt.get();
    content.recordFeedback(helpful, timeSpent);
    helpRepository.persist(content);

    // Analytics
    analyticsService.trackFeedback(helpId, userId, helpful, timeSpent, comment);

    LOG.info(
        "Feedback recorded for help '{}': {}% helpful ({} total)",
        content.title,
        content.getHelpfulnessRate(),
        content.helpfulCount + content.notHelpfulCount);
  }

  /** Sucht in Hilfe-Inhalten */
  public List<HelpResponse> searchHelp(
      String searchTerm, String userLevel, List<String> userRoles) {
    if (cqrsEnabled) {
      LOG.debug("Using CQRS implementation for help content search");
      return queryService.searchHelp(searchTerm, userLevel, userRoles);
    }
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

  /** Holt Analytics für Help System */
  public HelpAnalytics getAnalytics() {
    if (cqrsEnabled) {
      LOG.debug("Using CQRS implementation for analytics");
      return queryService.getAnalytics();
    }
    return analyticsService.getOverallAnalytics();
  }

  /** Erstellt oder aktualisiert Hilfe-Inhalt */
  @Transactional
  public HelpContent createOrUpdateHelpContent(
      String feature,
      HelpType type,
      String title,
      String shortContent,
      String mediumContent,
      String detailedContent,
      UserLevel userLevel,
      List<String> roles,
      String createdBy) {
    if (cqrsEnabled) {
      LOG.debug("Using CQRS implementation for content creation");
      return commandService.createOrUpdateHelpContent(
          feature, type, title, shortContent, mediumContent, detailedContent,
          userLevel, roles, createdBy);
    }

    LOG.info("Creating help content: {} - {} ({})", feature, title, type);

    HelpContent content = new HelpContent();
    content.feature = feature;
    content.helpType = type;
    content.title = title;
    content.shortContent = shortContent;
    content.mediumContent = mediumContent;
    content.detailedContent = detailedContent;
    content.targetUserLevel = userLevel;
    content.targetRoles = roles;
    content.createdBy = createdBy;
    content.updatedBy = createdBy;
    content.createdAt = LocalDateTime.now();
    content.updatedAt = LocalDateTime.now();

    helpRepository.persist(content);
    return content;
  }

  /** Aktiviert/Deaktiviert Hilfe-Inhalt */
  @Transactional
  public void toggleHelpContent(UUID helpId, boolean active, String updatedBy) {
    if (cqrsEnabled) {
      LOG.debug("Using CQRS implementation for content toggle");
      commandService.toggleHelpContent(helpId, active, updatedBy);
      return;
    }
    Optional<HelpContent> contentOpt = helpRepository.findByIdOptional(helpId);
    if (contentOpt.isEmpty()) {
      throw new IllegalArgumentException("Help content not found: " + helpId);
    }

    HelpContent content = contentOpt.get();
    content.isActive = active;
    content.updatedBy = updatedBy;
    content.updatedAt = LocalDateTime.now();

    helpRepository.persist(content);

    LOG.info(
        "Help content '{}' {}: {}", content.title, active ? "activated" : "deactivated", helpId);
  }

  /** Holt Feature Coverage Report */
  public List<String> getFeatureCoverageGaps() {
    if (cqrsEnabled) {
      LOG.debug("Using CQRS implementation for coverage gaps analysis");
      return queryService.getFeatureCoverageGaps();
    }
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
}
