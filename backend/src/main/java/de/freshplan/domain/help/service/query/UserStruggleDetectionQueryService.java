package de.freshplan.domain.help.service.query;

import de.freshplan.domain.help.service.command.UserStruggleDetectionCommandService;
import de.freshplan.domain.help.service.command.UserStruggleDetectionCommandService.UserAction;
import de.freshplan.domain.help.service.command.UserStruggleDetectionCommandService.UserBehaviorSession;
import de.freshplan.domain.help.service.dto.UserStruggle;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CQRS Query Service für User Struggle Detection - Analysis & Detection
 *
 * <p>Handles all read operations for user behavior analysis: - Struggle pattern detection and
 * analysis - Behavioral analytics and insights - Session data queries and reporting
 *
 * <p>This service accesses the shared session state managed by the Command service to perform
 * real-time struggle detection without modifying the underlying data.
 *
 * <p>Part of Phase 12 CQRS migration from UserStruggleDetectionService.
 *
 * @since Phase 12 CQRS Migration
 */
@ApplicationScoped
public class UserStruggleDetectionQueryService {

  private static final Logger LOG =
      LoggerFactory.getLogger(UserStruggleDetectionQueryService.class);

  // Struggle Detection Thresholds (exakte Kopie vom Original)
  private static final int REPEATED_FAILURE_THRESHOLD = 3;
  private static final int RAPID_NAVIGATION_THRESHOLD = 5; // 5 Seitenwechsel in 2 Minuten
  private static final Duration IDLE_THRESHOLD = Duration.ofMinutes(2);
  private static final int ABANDONED_WORKFLOW_THRESHOLD = 2;

  /**
   * Detects user struggle based on current context and session history.
   *
   * <p>This is the main public method, matching the original UserStruggleDetectionService API. It
   * performs read-only analysis of user behavior patterns to detect struggles.
   *
   * @param userId The user ID
   * @param feature The feature being used
   * @param context Additional context information
   * @return UserStruggle with detection results and suggestions
   */
  public UserStruggle detectStruggle(String userId, String feature, Map<String, Object> context) {
    UserBehaviorSession session = getUserSession(userId);

    if (session == null) {
      LOG.debug("No session found for user: {} - no struggle detected", userId);
      return UserStruggle.noStruggle(userId, feature);
    }

    // Verschiedene Struggle-Patterns analysieren
    UserStruggle.StruggleType detectedType = analyzeStrugglePatterns(session, feature);

    if (detectedType != null) {
      int severity = calculateSeverity(session, detectedType);

      LOG.info(
          "Struggle detected for user {}: {} (severity: {}) in feature: {}",
          userId,
          detectedType,
          severity,
          feature);

      return UserStruggle.builder()
          .userId(userId)
          .feature(feature)
          .type(detectedType)
          .severity(severity)
          .detected(true)
          .detectedAt(LocalDateTime.now())
          .context(context)
          .suggestions(generateSuggestions(detectedType, feature))
          .build();
    }

    return UserStruggle.noStruggle(userId, feature);
  }

  /** Gets user session statistics for analytics. */
  public UserSessionStats getUserSessionStats(String userId) {
    UserBehaviorSession session = getUserSession(userId);

    if (session == null) {
      return UserSessionStats.empty(userId);
    }

    return UserSessionStats.builder()
        .userId(userId)
        .totalActions(session.getActionCount())
        .totalFailures(session.getTotalFailures())
        .helpRequestCount(session.getHelpRequestCount())
        .lastActivity(session.getLastActivity())
        .sessionDuration(
            Duration.between(
                session.getRecentActions(Duration.ofHours(24)).isEmpty()
                    ? LocalDateTime.now()
                    : session.getRecentActions(Duration.ofHours(24)).get(0).timestamp,
                session.getLastActivity()))
        .build();
  }

  /** Gets recent user actions for debugging or analytics. */
  public List<UserAction> getRecentUserActions(String userId, Duration window) {
    UserBehaviorSession session = getUserSession(userId);

    if (session == null) {
      return List.of();
    }

    return session.getRecentActions(window);
  }

  // Private Methods - exakte Kopie der Analyse-Logic vom Original

  /** Analysiert verschiedene Struggle-Patterns (exakte Kopie) */
  private UserStruggle.StruggleType analyzeStrugglePatterns(
      UserBehaviorSession session, String feature) {
    // 1. Wiederholte fehlgeschlagene Versuche
    if (hasRepeatedFailures(session, feature)) {
      return UserStruggle.StruggleType.REPEATED_FAILED_ATTEMPTS;
    }

    // 2. Hektische Navigation
    if (hasRapidNavigation(session)) {
      return UserStruggle.StruggleType.RAPID_NAVIGATION_CHANGES;
    }

    // 3. Lange Pause nach Start
    if (hasLongIdleAfterStart(session, feature)) {
      return UserStruggle.StruggleType.LONG_IDLE_AFTER_START;
    }

    // 4. Abgebrochene Workflows
    if (hasAbandonedWorkflows(session, feature)) {
      return UserStruggle.StruggleType.ABANDONED_WORKFLOW_PATTERN;
    }

    // 5. Komplexe Form-Struggles
    if (hasComplexFormStruggle(session, feature)) {
      return UserStruggle.StruggleType.COMPLEX_FORM_STRUGGLE;
    }

    return null; // Kein Struggle erkannt
  }

  private boolean hasRepeatedFailures(UserBehaviorSession session, String feature) {
    List<UserAction> recentActions = session.getRecentActions(Duration.ofMinutes(5));

    long failureCount =
        recentActions.stream()
            .filter(action -> action.feature.equals(feature))
            .filter(action -> isFailureAction(action))
            .count();

    return failureCount >= REPEATED_FAILURE_THRESHOLD;
  }

  private boolean hasRapidNavigation(UserBehaviorSession session) {
    List<UserAction> recentActions = session.getRecentActions(Duration.ofMinutes(2));

    long uniquePages = recentActions.stream().map(action -> action.feature).distinct().count();

    return uniquePages >= RAPID_NAVIGATION_THRESHOLD;
  }

  private boolean hasLongIdleAfterStart(UserBehaviorSession session, String feature) {
    List<UserAction> recentActions = session.getRecentActions(Duration.ofMinutes(10));

    // Suche nach Start-Aktion gefolgt von langer Pause
    for (int i = 0; i < recentActions.size() - 1; i++) {
      UserAction action = recentActions.get(i);
      UserAction nextAction = recentActions.get(i + 1);

      if (action.feature.equals(feature) && isStartAction(action)) {
        Duration gap = Duration.between(action.timestamp, nextAction.timestamp);
        if (gap.compareTo(IDLE_THRESHOLD) > 0) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean hasAbandonedWorkflows(UserBehaviorSession session, String feature) {
    List<UserAction> recentActions = session.getRecentActions(Duration.ofHours(1));

    long abandonedCount =
        recentActions.stream()
            .filter(action -> action.feature.equals(feature))
            .filter(action -> isWorkflowStart(action))
            .filter(action -> !hasCorrespondingCompletion(recentActions, action))
            .count();

    return abandonedCount >= ABANDONED_WORKFLOW_THRESHOLD;
  }

  private boolean hasComplexFormStruggle(UserBehaviorSession session, String feature) {
    List<UserAction> recentActions = session.getRecentActions(Duration.ofMinutes(5));

    // Erkennt viele Feld-Wechsel ohne Submit
    long formFieldChanges =
        recentActions.stream()
            .filter(action -> action.feature.equals(feature))
            .filter(action -> isFormFieldAction(action))
            .count();

    boolean hasSubmit =
        recentActions.stream()
            .anyMatch(action -> action.feature.equals(feature) && isSubmitAction(action));

    return formFieldChanges > 10 && !hasSubmit;
  }

  /** Berechnet Severity basierend auf Struggle-Typ und Session-History (exakte Kopie) */
  private int calculateSeverity(UserBehaviorSession session, UserStruggle.StruggleType type) {
    int baseSeverity =
        switch (type) {
          case REPEATED_FAILED_ATTEMPTS -> 8; // Hoch
          case COMPLEX_FORM_STRUGGLE -> 7;
          case ABANDONED_WORKFLOW_PATTERN -> 6;
          case LONG_IDLE_AFTER_START -> 5;
          case RAPID_NAVIGATION_CHANGES -> 4; // Niedrig
        };

    // Modifikation basierend auf Session-Historie
    int failureCount = session.getTotalFailures();
    int helpRequestCount = session.getHelpRequestCount();

    // Mehr Failures = höhere Severity
    if (failureCount > 10) baseSeverity += 2;
    else if (failureCount > 5) baseSeverity += 1;

    // Bereits um Hilfe gebeten aber immer noch Probleme = höhere Severity
    if (helpRequestCount > 2) baseSeverity += 2;
    else if (helpRequestCount > 0) baseSeverity += 1;

    return Math.min(10, Math.max(1, baseSeverity));
  }

  /** Generiert Suggestions basierend auf Struggle-Typ (exakte Kopie) */
  private List<UserStruggle.Suggestion> generateSuggestions(
      UserStruggle.StruggleType type, String feature) {

    return switch (type) {
      case REPEATED_FAILED_ATTEMPTS ->
          List.of(
              new UserStruggle.Suggestion("video", "Video-Tutorial ansehen", 1),
              new UserStruggle.Suggestion("tutorial", "Schritt-für-Schritt Anleitung", 2),
              new UserStruggle.Suggestion("support", "Support kontaktieren", 3));

      case RAPID_NAVIGATION_CHANGES ->
          List.of(
              new UserStruggle.Suggestion("search", "Zur Suche", 1),
              new UserStruggle.Suggestion("sitemap", "Sitemap anzeigen", 2),
              new UserStruggle.Suggestion("tour", "Guided Tour starten", 3));

      case LONG_IDLE_AFTER_START ->
          List.of(
              new UserStruggle.Suggestion("tutorial", "Schritt-für-Schritt Anleitung", 1),
              new UserStruggle.Suggestion("tips", "Tipps anzeigen", 2),
              new UserStruggle.Suggestion("restart", "Neu starten", 3));

      case ABANDONED_WORKFLOW_PATTERN ->
          List.of(
              new UserStruggle.Suggestion("shortcut", "Abkürzung zeigen", 1),
              new UserStruggle.Suggestion("save", "Fortschritt speichern", 2),
              new UserStruggle.Suggestion("simplify", "Vereinfachte Version", 3));

      case COMPLEX_FORM_STRUGGLE ->
          List.of(
              new UserStruggle.Suggestion("minimal", "Nur wichtige Felder", 1),
              new UserStruggle.Suggestion("autofill", "Auto-Vervollständigung", 2),
              new UserStruggle.Suggestion("save", "Zwischenspeichern", 3));
    };
  }

  // Helper Methods für Action Classification (exakte Kopie)

  private boolean isFailureAction(UserAction action) {
    return action.context.containsKey("error")
        || action.context.containsKey("validation_failed")
        || "error".equals(action.context.get("status"));
  }

  private boolean isStartAction(UserAction action) {
    return "start".equals(action.context.get("action"))
        || action.context.containsKey("workflow_start");
  }

  private boolean isWorkflowStart(UserAction action) {
    return action.context.get("action") != null
        && action.context.get("action").toString().endsWith("_start");
  }

  private boolean hasCorrespondingCompletion(List<UserAction> actions, UserAction startAction) {
    String expectedCompletion =
        startAction.context.get("action").toString().replace("_start", "_complete");

    return actions.stream()
        .anyMatch(action -> expectedCompletion.equals(action.context.get("action")));
  }

  private boolean isFormFieldAction(UserAction action) {
    return action.context.containsKey("field")
        || "field_change".equals(action.context.get("action"));
  }

  private boolean isSubmitAction(UserAction action) {
    return "submit".equals(action.context.get("action"))
        || "form_submit".equals(action.context.get("action"));
  }

  /** Helper method to get user session from Command service's shared state */
  private UserBehaviorSession getUserSession(String userId) {
    Map<String, UserBehaviorSession> sessions =
        UserStruggleDetectionCommandService.getUserSessions();
    return sessions.get(userId);
  }

  // DTO for session statistics
  public static class UserSessionStats {
    private final String userId;
    private final int totalActions;
    private final int totalFailures;
    private final int helpRequestCount;
    private final LocalDateTime lastActivity;
    private final Duration sessionDuration;

    private UserSessionStats(Builder builder) {
      this.userId = builder.userId;
      this.totalActions = builder.totalActions;
      this.totalFailures = builder.totalFailures;
      this.helpRequestCount = builder.helpRequestCount;
      this.lastActivity = builder.lastActivity;
      this.sessionDuration = builder.sessionDuration;
    }

    public static Builder builder() {
      return new Builder();
    }

    public static UserSessionStats empty(String userId) {
      return builder()
          .userId(userId)
          .totalActions(0)
          .totalFailures(0)
          .helpRequestCount(0)
          .lastActivity(null)
          .sessionDuration(Duration.ZERO)
          .build();
    }

    // Getters
    public String getUserId() {
      return userId;
    }

    public int getTotalActions() {
      return totalActions;
    }

    public int getTotalFailures() {
      return totalFailures;
    }

    public int getHelpRequestCount() {
      return helpRequestCount;
    }

    public LocalDateTime getLastActivity() {
      return lastActivity;
    }

    public Duration getSessionDuration() {
      return sessionDuration;
    }

    public static class Builder {
      private String userId;
      private int totalActions;
      private int totalFailures;
      private int helpRequestCount;
      private LocalDateTime lastActivity;
      private Duration sessionDuration;

      public Builder userId(String userId) {
        this.userId = userId;
        return this;
      }

      public Builder totalActions(int totalActions) {
        this.totalActions = totalActions;
        return this;
      }

      public Builder totalFailures(int totalFailures) {
        this.totalFailures = totalFailures;
        return this;
      }

      public Builder helpRequestCount(int helpRequestCount) {
        this.helpRequestCount = helpRequestCount;
        return this;
      }

      public Builder lastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
        return this;
      }

      public Builder sessionDuration(Duration sessionDuration) {
        this.sessionDuration = sessionDuration;
        return this;
      }

      public UserSessionStats build() {
        return new UserSessionStats(this);
      }
    }
  }
}
