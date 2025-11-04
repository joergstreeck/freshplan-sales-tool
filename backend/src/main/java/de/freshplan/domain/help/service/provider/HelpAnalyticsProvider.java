package de.freshplan.domain.help.service.provider;

import de.freshplan.domain.help.entity.HelpContent;
import de.freshplan.domain.help.service.dto.HelpAnalytics;
import de.freshplan.domain.help.service.dto.HelpRequest;
import de.freshplan.domain.help.service.dto.UserStruggle;
import java.util.Map;
import java.util.UUID;

/**
 * Interface for help analytics operations.
 *
 * <p>Extracted during Sprint 2.1.7.7 Cycle 3-6 fix to break circular dependency between
 * help.service, help.service.query, help.service.command, and help.events packages. Follows
 * Dependency Inversion Principle (SOLID).
 *
 * <p>This interface allows query/command/events packages to track analytics without depending on
 * the concrete HelpAnalyticsService implementation, breaking the circular dependency.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public interface HelpAnalyticsProvider {

  /**
   * Tracks a help request.
   *
   * @param request The help request
   * @param content The help content that was provided
   * @param struggle The detected user struggle
   */
  void trackHelpRequest(HelpRequest request, HelpContent content, UserStruggle struggle);

  /**
   * Tracks user feedback for help content.
   *
   * @param helpId The help content ID
   * @param userId The user providing feedback
   * @param helpful Whether the help was helpful
   * @param timeSpent Time spent reading/using the help
   * @param comment Optional user comment
   */
  void trackFeedback(
      UUID helpId, String userId, boolean helpful, Integer timeSpent, String comment);

  /**
   * Gets overall help system analytics.
   *
   * @return Comprehensive analytics data
   */
  HelpAnalytics getOverallAnalytics();

  /**
   * Gets feature-specific analytics.
   *
   * @param feature The feature name
   * @return Feature-specific analytics
   */
  HelpAnalytics.FeatureAnalytics getFeatureAnalytics(String feature);

  /**
   * Gets performance metrics for help system.
   *
   * @return Performance metrics
   */
  Map<String, Object> getPerformanceMetrics();
}
