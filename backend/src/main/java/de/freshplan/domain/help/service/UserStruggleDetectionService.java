package de.freshplan.domain.help.service;

import de.freshplan.domain.help.service.command.UserStruggleDetectionCommandService;
import de.freshplan.domain.help.service.dto.UserStruggle;
import de.freshplan.domain.help.service.query.UserStruggleDetectionQueryService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FACADE Service für User Struggle Detection - CQRS Migration mit Feature Flag
 *
 * <p>Dieses Service agiert als Facade während der CQRS-Migration und delegiert an die neuen Command
 * und Query Services basierend auf dem Feature Flag.
 *
 * <p>Analysiert User-Verhalten und erkennt: - Wiederholte fehlgeschlagene Versuche - Hektische
 * Navigation - Lange Pausen nach dem Start - Abgebrochene Workflows
 *
 * <p>Part of Phase 12 CQRS migration.
 *
 * @since Phase 12 CQRS Migration
 */
@ApplicationScoped
public class UserStruggleDetectionService {

  private static final Logger LOG = LoggerFactory.getLogger(UserStruggleDetectionService.class);

  @ConfigProperty(name = "features.cqrs.enabled", defaultValue = "false")
  boolean cqrsEnabled;

  @Inject UserStruggleDetectionCommandService commandService;
  @Inject UserStruggleDetectionQueryService queryService;

  // Legacy In-Memory Tracking (wenn CQRS disabled)
  private final Map<String, UserBehaviorSession> userSessions = new ConcurrentHashMap<>();

  // Legacy Struggle Detection Thresholds
  private static final int REPEATED_FAILURE_THRESHOLD = 3;
  private static final int RAPID_NAVIGATION_THRESHOLD = 5; // 5 Seitenwechsel in 2 Minuten
  private static final Duration IDLE_THRESHOLD = Duration.ofMinutes(2);
  private static final int ABANDONED_WORKFLOW_THRESHOLD = 2;

  /** Erkennt User Struggle basierend auf aktuellem Kontext */
  public UserStruggle detectStruggle(String userId, String feature, Map<String, Object> context) {
    if (cqrsEnabled) {
      LOG.debug("Using CQRS implementation for user struggle detection");
      // 1. Record user action (Command)
      commandService.recordUserAction(userId, feature, context);
      // 2. Detect struggle (Query)
      return queryService.detectStruggle(userId, feature, context);
    }

    LOG.debug("Using legacy implementation for user struggle detection");
    // Legacy implementation (exakte Kopie des Original-Codes)
    UserBehaviorSession session = getOrCreateSession(userId);

    // Aktuelle Aktion registrieren
    session.recordAction(feature, context);

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

  /** Analysiert verschiedene Struggle-Patterns */
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

  /** Berechnet Severity basierend auf Struggle-Typ und Session-History */
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

  /** Generiert Suggestions basierend auf Struggle-Typ */
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

  // Helper Methods für Action Classification

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

  private UserBehaviorSession getOrCreateSession(String userId) {
    return userSessions.computeIfAbsent(userId, k -> new UserBehaviorSession(userId));
  }

  // Inner Classes

  private static class UserBehaviorSession {
    private final String userId;
    private final List<UserAction> actions = new java.util.ArrayList<>();
    private int totalFailures = 0;
    private int helpRequestCount = 0;

    public UserBehaviorSession(String userId) {
      this.userId = userId;
    }

    public void recordAction(String feature, Map<String, Object> context) {
      UserAction action = new UserAction(feature, LocalDateTime.now(), context);
      actions.add(action);

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

    public List<UserAction> getRecentActions(Duration window) {
      LocalDateTime cutoff = LocalDateTime.now().minus(window);
      return actions.stream().filter(action -> action.timestamp.isAfter(cutoff)).toList();
    }

    public int getTotalFailures() {
      return totalFailures;
    }

    public int getHelpRequestCount() {
      return helpRequestCount;
    }
  }

  private static class UserAction {
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
