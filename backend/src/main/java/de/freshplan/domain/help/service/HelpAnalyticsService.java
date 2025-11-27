package de.freshplan.domain.help.service;

import de.freshplan.domain.help.entity.HelpContent;
import de.freshplan.domain.help.repository.HelpContentRepository;
import de.freshplan.domain.help.service.dto.HelpAnalytics;
import de.freshplan.domain.help.service.dto.HelpRequest;
import de.freshplan.domain.help.service.dto.UserStruggle;
import de.freshplan.domain.help.service.provider.HelpAnalyticsProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service für Help System Analytics
 *
 * <p>Sammelt und analysiert: - Help Request Patterns - User Feedback - Struggle Detection Metrics -
 * Feature Adoption Correlation
 *
 * <p>Sprint 2.1.7.7 Cycle 3-6 fix: Implements HelpAnalyticsProvider interface to break circular
 * dependency (Dependency Inversion Principle).
 */
@ApplicationScoped
public class HelpAnalyticsService implements HelpAnalyticsProvider {

  private static final Logger LOG = LoggerFactory.getLogger(HelpAnalyticsService.class);

  @Inject HelpContentRepository helpRepository;

  // In-Memory Analytics Storage (in Production: InfluxDB oder ähnlich)
  private final Map<String, Integer> helpRequestCounts = new ConcurrentHashMap<>();
  private final Map<String, Integer> struggleDetectionCounts = new ConcurrentHashMap<>();
  private final Map<String, List<Long>> responseTimesByFeature = new ConcurrentHashMap<>();

  /** Trackt eine Help Request */
  public void trackHelpRequest(HelpRequest request, HelpContent content, UserStruggle struggle) {
    String key = request.feature();
    helpRequestCounts.merge(key, 1, Integer::sum);

    if (struggle.isDetected()) {
      String struggleKey = request.feature() + ":" + struggle.getType();
      struggleDetectionCounts.merge(struggleKey, 1, Integer::sum);
    }

    LOG.debug("Tracked help request: {} (total: {})", key, helpRequestCounts.get(key));
  }

  /** Trackt User Feedback */
  public void trackFeedback(
      UUID helpId, String userId, boolean helpful, Integer timeSpent, String comment) {
    // Hier würden wir normalerweise detaillierte Feedback-Daten speichern
    // Für die Demo loggen wir nur
    LOG.info("Feedback tracked: helpId={}, helpful={}, timeSpent={}s", helpId, helpful, timeSpent);

    if (timeSpent != null && timeSpent > 0) {
      // Track response times für Performance-Analyse
      // Implementation würde hier erfolgen
    }
  }

  /** Generiert Overall Analytics */
  public HelpAnalytics getOverallAnalytics() {
    long totalViews = helpRepository.getTotalViews();
    long totalFeedback = helpRepository.getTotalFeedback();
    double overallHelpfulness = helpRepository.getOverallHelpfulnessRate();

    List<HelpContent> mostRequested = helpRepository.findMostRequested(10);
    List<HelpContent> mostHelpful = helpRepository.findMostHelpful(10);
    List<HelpContent> needingImprovement = helpRepository.findNeedingImprovement(10);

    // Feature Coverage
    List<String> featuresWithHelp = helpRepository.getFeaturesWithHelp();
    List<String> coverageGaps = calculateCoverageGaps();

    // Struggle Analytics
    Map<String, Integer> strugglesByType = calculateStrugglesByType();
    Map<String, Integer> helpRequestsByFeature = new HashMap<>(helpRequestCounts);

    return HelpAnalytics.builder()
        .totalViews(totalViews)
        .totalFeedback(totalFeedback)
        .overallHelpfulnessRate(overallHelpfulness)
        .mostRequestedTopics(convertToTopTopics(mostRequested))
        .mostHelpfulContent(convertToTopTopics(mostHelpful))
        .contentNeedingImprovement(convertToTopTopics(needingImprovement))
        .featureCoverage(featuresWithHelp.size())
        .coverageGaps(coverageGaps)
        .helpRequestsByFeature(helpRequestsByFeature)
        .struggleDetectionsByType(strugglesByType)
        .averageResponseTime(calculateAverageResponseTime())
        .userSatisfactionScore(calculateUserSatisfaction())
        .build();
  }

  /** Feature-spezifische Analytics */
  public HelpAnalytics.FeatureAnalytics getFeatureAnalytics(String feature) {
    List<HelpContent> featureContent = helpRepository.findActiveByFeature(feature);

    long totalViews = featureContent.stream().mapToLong(c -> c.viewCount).sum();
    long totalFeedback =
        featureContent.stream().mapToLong(c -> c.helpfulCount + c.notHelpfulCount).sum();

    double avgHelpfulness =
        featureContent.stream().mapToDouble(HelpContent::getHelpfulnessRate).average().orElse(0.0);

    Integer helpRequests = helpRequestCounts.getOrDefault(feature, 0);
    Integer struggleDetections =
        struggleDetectionCounts.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(feature + ":"))
            .mapToInt(Map.Entry::getValue)
            .sum();

    return HelpAnalytics.FeatureAnalytics.builder()
        .feature(feature)
        .contentCount(featureContent.size())
        .totalViews(totalViews)
        .totalFeedback(totalFeedback)
        .averageHelpfulness(avgHelpfulness)
        .helpRequests(helpRequests)
        .struggleDetections(struggleDetections)
        .strugglesPerRequest(helpRequests > 0 ? (double) struggleDetections / helpRequests : 0.0)
        .lastUpdated(LocalDateTime.now())
        .build();
  }

  /** Performance Analytics */
  public Map<String, Object> getPerformanceMetrics() {
    Map<String, Object> metrics = new HashMap<>();

    // Response Times
    Map<String, Double> avgResponseTimes = new HashMap<>();
    responseTimesByFeature.forEach(
        (feature, times) -> {
          double avg = times.stream().mapToLong(Long::longValue).average().orElse(0.0);
          avgResponseTimes.put(feature, avg);
        });
    metrics.put("averageResponseTimes", avgResponseTimes);

    // Help Effectiveness (weniger Struggles nach Help)
    Map<String, Double> effectiveness = calculateHelpEffectiveness();
    metrics.put("helpEffectiveness", effectiveness);

    // Coverage Metrics
    metrics.put("featureCoveragePercentage", calculateCoveragePercentage());
    metrics.put("helpTypeDistribution", calculateHelpTypeDistribution());

    return metrics;
  }

  // Private Helper Methods

  private List<HelpAnalytics.TopTopic> convertToTopTopics(List<HelpContent> contents) {
    return contents.stream()
        .map(
            content ->
                HelpAnalytics.TopTopic.builder()
                    .id(content.id)
                    .title(content.title)
                    .feature(content.feature)
                    .requests(content.viewCount.intValue())
                    .helpfulRate(content.getHelpfulnessRate())
                    .build())
        .toList();
  }

  private List<String> calculateCoverageGaps() {
    // Hier würden wir alle Features aus dem System laden und prüfen
    // welche keine Hilfe haben. Für Demo: Hardcoded Liste
    return List.of("advanced-reporting", "bulk-operations", "data-export");
  }

  private Map<String, Integer> calculateStrugglesByType() {
    Map<String, Integer> byType = new HashMap<>();

    struggleDetectionCounts.forEach(
        (key, count) -> {
          String type = key.substring(key.indexOf(':') + 1);
          byType.merge(type, count, Integer::sum);
        });

    return byType;
  }

  private double calculateAverageResponseTime() {
    return responseTimesByFeature.values().stream()
        .flatMap(List::stream)
        .mapToLong(Long::longValue)
        .average()
        .orElse(0.0);
  }

  private double calculateUserSatisfaction() {
    // Basiert auf Helpfulness Rate und reduzierten Struggle Detections
    double helpfulness = helpRepository.getOverallHelpfulnessRate();

    // Berechne Struggle-Rate (niedrigere ist besser)
    int totalHelp = helpRequestCounts.values().stream().mapToInt(Integer::intValue).sum();
    int totalStruggles =
        struggleDetectionCounts.values().stream().mapToInt(Integer::intValue).sum();

    double struggleRate = totalHelp > 0 ? (double) totalStruggles / totalHelp : 0.0;
    double struggleScore = Math.max(0, 100 - (struggleRate * 100));

    // Gewichteter Durchschnitt: 70% Helpfulness, 30% reduzierte Struggles
    return (helpfulness * 0.7) + (struggleScore * 0.3);
  }

  private Map<String, Double> calculateHelpEffectiveness() {
    Map<String, Double> effectiveness = new HashMap<>();

    helpRequestCounts.forEach(
        (feature, requests) -> {
          int struggles =
              struggleDetectionCounts.entrySet().stream()
                  .filter(entry -> entry.getKey().startsWith(feature + ":"))
                  .mapToInt(Map.Entry::getValue)
                  .sum();

          // Effectiveness: je weniger Struggles pro Help Request, desto besser
          double rate = requests > 0 ? 100 - ((double) struggles / requests * 100) : 100;
          effectiveness.put(feature, Math.max(0, rate));
        });

    return effectiveness;
  }

  private double calculateCoveragePercentage() {
    // Hardcoded für Demo - in Production würden wir alle Features scannen
    int totalFeatures = 25; // Beispiel-Anzahl Features im System
    int coveredFeatures = helpRepository.getFeaturesWithHelp().size();

    return (double) coveredFeatures / totalFeatures * 100.0;
  }

  private Map<String, Integer> calculateHelpTypeDistribution() {
    // SQL Query um Help Types zu zählen
    Map<String, Integer> distribution = new HashMap<>();

    // Für Demo: Simulierte Daten
    distribution.put("TOOLTIP", 45);
    distribution.put("TUTORIAL", 20);
    distribution.put("TOUR", 15);
    distribution.put("FAQ", 12);
    distribution.put("VIDEO", 8);

    return distribution;
  }

  /** Reset Analytics (für Testing) */
  public void resetAnalytics() {
    helpRequestCounts.clear();
    struggleDetectionCounts.clear();
    responseTimesByFeature.clear();
    LOG.info("Help analytics reset");
  }
}
