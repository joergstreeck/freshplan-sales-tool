package de.freshplan.domain.help.service.provider;

import de.freshplan.domain.help.service.dto.UserStruggle;
import java.util.Map;

/**
 * Interface for user struggle detection operations.
 *
 * <p>Extracted during Sprint 2.1.7.7 Cycle 3-6 fix to break circular dependency between
 * help.service, help.service.query, and help.service.command packages. Follows Dependency Inversion
 * Principle (SOLID).
 *
 * <p>This interface allows query/command packages to detect user struggles without depending on the
 * concrete UserStruggleDetectionService implementation, breaking the circular dependency.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public interface StruggleDetectionProvider {

  /**
   * Detects user struggle based on current context.
   *
   * <p>Analyzes user behavior patterns to identify struggles and provide contextual help
   * suggestions.
   *
   * @param userId The user ID
   * @param feature The feature being used
   * @param context Additional context information about the user's actions
   * @return UserStruggle with detection results and suggestions
   */
  UserStruggle detectStruggle(String userId, String feature, Map<String, Object> context);
}
