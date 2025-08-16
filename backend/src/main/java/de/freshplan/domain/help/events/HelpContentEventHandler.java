package de.freshplan.domain.help.events;

import de.freshplan.domain.help.entity.HelpContent;
import de.freshplan.domain.help.service.HelpAnalyticsService;
import de.freshplan.domain.help.service.command.HelpContentCommandService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event Handler for Help Content Domain Events.
 *
 * <p>Handles side effects of help content operations asynchronously to maintain pure CQRS
 * separation and improve performance.
 *
 * <p>This handler processes events like: - View count updates when content is viewed - Analytics
 * tracking for usage patterns - Caching updates for performance optimization
 *
 * <p>Part of Event-Driven CQRS architecture for Phase 12.2.
 */
@ApplicationScoped
public class HelpContentEventHandler {

  private static final Logger LOG = LoggerFactory.getLogger(HelpContentEventHandler.class);

  @Inject HelpContentCommandService commandService;

  @Inject HelpAnalyticsService analyticsService;

  /**
   * Handles help content viewed events asynchronously.
   *
   * <p>This handler executes in a separate transaction from the original query operation, ensuring
   * that side effects don't impact query performance or cause the main operation to fail.
   *
   * @param event The help content viewed event
   */
  @Transactional
  @ActivateRequestContext
  public void onHelpContentViewed(@ObservesAsync HelpContentViewedEvent event) {
    LOG.debug(
        "Processing HelpContentViewedEvent for content: {} by user: {}",
        event.helpContentId(),
        event.userId());

    try {
      // 1. Update view count (Command)
      commandService.incrementViewCount(event.helpContentId());

      // 2. Track analytics (if full context available)
      if (event.originalRequest() != null && event.detectedStruggle() != null) {
        HelpContent contentForAnalytics = createContentFromEvent(event);
        analyticsService.trackHelpRequest(
            event.originalRequest(), contentForAnalytics, event.detectedStruggle());
      }

      LOG.info(
          "Successfully processed view event for help content: {} (user: {})",
          event.helpContentId(),
          event.userId());

    } catch (Exception e) {
      LOG.error(
          "Failed to process HelpContentViewedEvent for content: {} by user: {}",
          event.helpContentId(),
          event.userId(),
          e);

      // In async handlers, we log errors but don't propagate them
      // to avoid affecting the original operation
    }
  }

  /**
   * Alternative synchronous event handler for critical operations.
   *
   * <p>Use this when view count updates must succeed within the same transaction as the query
   * operation.
   *
   * @param event The help content viewed event
   */
  @Transactional
  public void onHelpContentViewedSync(@Observes HelpContentViewedEvent event) {
    LOG.debug(
        "Processing synchronous HelpContentViewedEvent for content: {}", event.helpContentId());

    // This handler is only used when synchronous processing is required
    // For normal operations, the async handler above is preferred

    try {
      commandService.incrementViewCount(event.helpContentId());
      LOG.debug("Synchronously updated view count for content: {}", event.helpContentId());
    } catch (Exception e) {
      LOG.error(
          "Critical: Failed to update view count synchronously for content: {}",
          event.helpContentId(),
          e);
      throw e; // Propagate error in sync mode
    }
  }

  /** Helper method to create a minimal HelpContent object from event data. */
  private HelpContent createContentFromEvent(HelpContentViewedEvent event) {
    HelpContent content = new HelpContent();
    content.id = event.helpContentId();
    content.feature = event.feature();
    content.viewCount = 1L; // This event represents one view

    // Set minimal data for analytics
    if (event.originalRequest() != null) {
      content.title = "Event-based content"; // Placeholder
      content.helpType = event.originalRequest().preferredType();
    }

    return content;
  }

  /**
   * Handles bulk operations like cache warming or cleanup.
   *
   * <p>This could be triggered by scheduled jobs or admin operations.
   */
  public void onHelpContentBulkOperation(@ObservesAsync HelpContentBulkEvent event) {
    LOG.debug(
        "Processing bulk operation: {} for {} items", event.operation(), event.contentIds().size());

    try {
      switch (event.operation()) {
        case CACHE_WARM -> warmCacheForContent(event.contentIds());
        case CLEANUP_VIEWS -> cleanupOldViewData(event.contentIds());
        case RECALCULATE_STATS -> recalculateStatsForContent(event.contentIds());
        default -> LOG.warn("Unknown bulk operation: {}", event.operation());
      }
    } catch (Exception e) {
      LOG.error("Failed to process bulk operation: {}", event.operation(), e);
    }
  }

  private void warmCacheForContent(java.util.List<java.util.UUID> contentIds) {
    LOG.debug("Warming cache for {} content items", contentIds.size());
    // Implementation would pre-load frequently accessed content
  }

  private void cleanupOldViewData(java.util.List<java.util.UUID> contentIds) {
    LOG.debug("Cleaning up old view data for {} content items", contentIds.size());
    // Implementation would clean up old analytics data
  }

  private void recalculateStatsForContent(java.util.List<java.util.UUID> contentIds) {
    LOG.debug("Recalculating stats for {} content items", contentIds.size());
    // Implementation would recalculate helpfulness rates, etc.
  }

  /** Simple event for bulk operations. */
  public record HelpContentBulkEvent(
      BulkOperation operation, java.util.List<java.util.UUID> contentIds) {
    public enum BulkOperation {
      CACHE_WARM,
      CLEANUP_VIEWS,
      RECALCULATE_STATS
    }
  }
}
