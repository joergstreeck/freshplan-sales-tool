package de.freshplan.domain.help.service.command;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CQRS Command Service für User Struggle Detection - Session Management
 *
 * <p>Handles all write operations for user behavior tracking: - Session creation and management -
 * Action recording and state updates - Session cleanup and maintenance
 *
 * <p>Part of Phase 12 CQRS migration from UserStruggleDetectionService.
 *
 * @since Phase 12 CQRS Migration
 */
@ApplicationScoped
public class UserStruggleDetectionCommandService {

  private static final Logger LOG =
      LoggerFactory.getLogger(UserStruggleDetectionCommandService.class);

  // Shared in-memory state (in Production: Redis oder DB)
  // WICHTIG: Static für Shared State zwischen Command und Query Service
  private static final Map<String, UserBehaviorSession> userSessions = new ConcurrentHashMap<>();

  /**
   * Records a user action and updates the session state.
   *
   * <p>This method handles the WRITE aspect of user behavior tracking, maintaining session state
   * for later analysis by the Query service.
   *
   * @param userId The user ID
   * @param feature The feature being used
   * @param context Additional context information
   */
  @Transactional
  public void recordUserAction(String userId, String feature, Map<String, Object> context) {
    LOG.debug("Recording user action: user={}, feature={}, context={}", userId, feature, context);

    UserBehaviorSession session = getOrCreateSession(userId);
    session.recordAction(feature, context);

    LOG.debug("User session updated: {} actions total", session.getActionCount());
  }

  /**
   * Cleans up old user sessions to prevent memory leaks.
   *
   * <p>Removes sessions that haven't been active for a specified duration.
   */
  @Transactional
  public void cleanupOldSessions() {
    LOG.debug("Starting session cleanup");

    final LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
    final int[] removedCount = {0}; // Array für mutable variable in lambda

    userSessions
        .entrySet()
        .removeIf(
            entry -> {
              UserBehaviorSession session = entry.getValue();
              if (session.getLastActivity().isBefore(cutoff)) {
                removedCount[0]++;
                return true;
              }
              return false;
            });

    LOG.info("Session cleanup completed: {} sessions removed", removedCount[0]);
  }

  /** Resets a user's session (for testing or explicit cleanup). */
  @Transactional
  public void resetUserSession(String userId) {
    LOG.debug("Resetting session for user: {}", userId);
    userSessions.remove(userId);
  }

  /**
   * Gets or creates a user behavior session.
   *
   * @param userId The user ID
   * @return The user's behavior session
   */
  private UserBehaviorSession getOrCreateSession(String userId) {
    return userSessions.computeIfAbsent(
        userId,
        k -> {
          LOG.debug("Creating new session for user: {}", userId);
          return new UserBehaviorSession(userId);
        });
  }

  /**
   * Static method to provide access to sessions for Query service.
   *
   * <p>WICHTIG: Ermöglicht Query Service Zugriff auf Shared State
   */
  public static Map<String, UserBehaviorSession> getUserSessions() {
    return userSessions;
  }

  // Inner Classes - Shared zwischen Command und Query

  public static class UserBehaviorSession {
    private final String userId;
    private final java.util.List<UserAction> actions = new java.util.ArrayList<>();
    private int totalFailures = 0;
    private int helpRequestCount = 0;
    private LocalDateTime lastActivity;

    public UserBehaviorSession(String userId) {
      this.userId = userId;
      this.lastActivity = LocalDateTime.now();
    }

    public synchronized void recordAction(String feature, Map<String, Object> context) {
      UserAction action = new UserAction(feature, LocalDateTime.now(), context);
      actions.add(action);
      this.lastActivity = LocalDateTime.now();

      // Track counters
      if (context.containsKey("error")) {
        totalFailures++;
      }
      if ("help_request".equals(context.get("action"))) {
        helpRequestCount++;
      }

      // Cleanup alte Actions (max 1000 Actions pro Session)
      if (actions.size() > 1000) {
        actions.subList(0, 500).clear();
      }
    }

    public java.util.List<UserAction> getRecentActions(java.time.Duration window) {
      LocalDateTime cutoff = LocalDateTime.now().minus(window);
      return actions.stream().filter(action -> action.timestamp.isAfter(cutoff)).toList();
    }

    public int getTotalFailures() {
      return totalFailures;
    }

    public int getHelpRequestCount() {
      return helpRequestCount;
    }

    public int getActionCount() {
      return actions.size();
    }

    public LocalDateTime getLastActivity() {
      return lastActivity;
    }

    public String getUserId() {
      return userId;
    }
  }

  public static class UserAction {
    public final String feature;
    public final LocalDateTime timestamp;
    public final Map<String, Object> context;

    public UserAction(String feature, LocalDateTime timestamp, Map<String, Object> context) {
      this.feature = feature;
      this.timestamp = timestamp;
      this.context = new HashMap<>(context);
    }
  }
}
