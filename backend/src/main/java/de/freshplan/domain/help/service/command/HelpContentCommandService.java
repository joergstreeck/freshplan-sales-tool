package de.freshplan.domain.help.service.command;

import de.freshplan.domain.help.entity.HelpContent;
import de.freshplan.domain.help.entity.HelpType;
import de.freshplan.domain.help.entity.UserLevel;
import de.freshplan.domain.help.repository.HelpContentRepository;
import de.freshplan.domain.help.service.provider.HelpAnalyticsProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CQRS Command Service für Help Content Management - Write Operations
 *
 * <p>Handles all write operations for help content management: - Content creation and updates -
 * Feedback recording and analytics - Content status management (activate/deactivate) - View count
 * tracking and maintenance
 *
 * <p>Part of Phase 12.2 CQRS migration from HelpContentService.
 *
 * @since Phase 12.2 CQRS Migration
 */
@ApplicationScoped
@Transactional
public class HelpContentCommandService {

  private static final Logger LOG = LoggerFactory.getLogger(HelpContentCommandService.class);

  @Inject HelpContentRepository helpRepository;

  @Inject HelpAnalyticsProvider analyticsService;

  /**
   * Records user feedback for help content.
   *
   * <p>This includes updating the help content's feedback counters and tracking analytics for
   * improvement insights.
   *
   * @param helpId The help content ID
   * @param userId The user providing feedback
   * @param helpful Whether the help was helpful
   * @param timeSpent Time spent reading/using the help
   * @param comment Optional user comment
   */
  @Transactional
  public void recordFeedback(
      UUID helpId, String userId, boolean helpful, Integer timeSpent, String comment) {
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

    // Analytics tracking
    analyticsService.trackFeedback(helpId, userId, helpful, timeSpent, comment);

    LOG.info(
        "Feedback recorded for help '{}': {}% helpful ({} total)",
        content.title,
        content.getHelpfulnessRate(),
        content.helpfulCount + content.notHelpfulCount);
  }

  /**
   * Creates or updates help content.
   *
   * <p>Creates new help content with all necessary metadata and makes it immediately available for
   * users.
   *
   * @param feature The feature this help is for
   * @param type The type of help content
   * @param title The help title
   * @param shortContent Brief help text
   * @param mediumContent Moderate help text
   * @param detailedContent Detailed help text
   * @param userLevel Target user level
   * @param roles Target user roles
   * @param createdBy Who created this content
   * @return The created help content
   */
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

  /**
   * Activates or deactivates help content.
   *
   * <p>Manages the lifecycle of help content by enabling/disabling its visibility to users.
   *
   * @param helpId The help content ID
   * @param active Whether to activate or deactivate
   * @param updatedBy Who made this change
   */
  @Transactional
  public void toggleHelpContent(UUID helpId, boolean active, String updatedBy) {
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

  /**
   * Increments view count for help content.
   *
   * <p>This is called when help content is actually viewed by users. Separated from the query
   * operations to maintain CQRS principles.
   *
   * @param helpId The help content ID
   */
  @Transactional
  public void incrementViewCount(UUID helpId) {
    Optional<HelpContent> contentOpt = helpRepository.findByIdOptional(helpId);
    if (contentOpt.isEmpty()) {
      LOG.warn("Cannot increment view count - help content not found: {}", helpId);
      return;
    }

    HelpContent content = contentOpt.get();
    content.incrementViewCount();
    helpRepository.persist(content);

    LOG.debug(
        "View count incremented for help '{}': {} total views", content.title, content.viewCount);
  }

  /**
   * Bulk operation to reset view counts (for maintenance/testing).
   *
   * @param helpIds List of help content IDs to reset
   */
  @Transactional
  public void resetViewCounts(List<UUID> helpIds) {
    LOG.info("Resetting view counts for {} help contents", helpIds.size());

    for (UUID helpId : helpIds) {
      Optional<HelpContent> contentOpt = helpRepository.findByIdOptional(helpId);
      if (contentOpt.isPresent()) {
        HelpContent content = contentOpt.get();
        content.viewCount = 0L;
        helpRepository.persist(content);
      }
    }

    LOG.info("View count reset completed for {} contents", helpIds.size());
  }

  /**
   * Updates help content metadata without changing the actual content.
   *
   * @param helpId The help content ID
   * @param priority New priority value
   * @param updatedBy Who made this change
   */
  @Transactional
  public void updateContentMetadata(UUID helpId, Integer priority, String updatedBy) {
    Optional<HelpContent> contentOpt = helpRepository.findByIdOptional(helpId);
    if (contentOpt.isEmpty()) {
      throw new IllegalArgumentException("Help content not found: " + helpId);
    }

    HelpContent content = contentOpt.get();
    if (priority != null) {
      content.priority = priority;
    }
    content.updatedBy = updatedBy;
    content.updatedAt = LocalDateTime.now();

    helpRepository.persist(content);

    LOG.info("Metadata updated for help content '{}': priority={}", content.title, priority);
  }
}
