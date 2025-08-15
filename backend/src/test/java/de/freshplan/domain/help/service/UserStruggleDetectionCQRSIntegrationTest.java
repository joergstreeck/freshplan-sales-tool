package de.freshplan.domain.help.service;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.help.service.command.UserStruggleDetectionCommandService;
import de.freshplan.domain.help.service.dto.UserStruggle;
import de.freshplan.domain.help.service.query.UserStruggleDetectionQueryService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.TestTransaction;import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration Test für Phase 12.1 - UserStruggleDetectionService CQRS Migration
 *
 * <p>Testet alle kritischen Aspekte der CQRS-Implementierung:
 * - Feature Flag Switching zwischen Legacy und CQRS
 * - Command/Query Service Integration
 * - Shared State zwischen Command und Query
 * - Struggle Detection Logic in beiden Modi
 * - Session Management und Cleanup
 */
@QuarkusTest
@TestProfile(UserStruggleDetectionCQRSIntegrationTest.CQRSTestProfile.class)
@DisplayName("Phase 12.1: UserStruggleDetectionService CQRS Integration")
class UserStruggleDetectionCQRSIntegrationTest {

  @Inject UserStruggleDetectionService facadeService;

  @Inject UserStruggleDetectionCommandService commandService;

  @Inject UserStruggleDetectionQueryService queryService;

  @ConfigProperty(name = "features.cqrs.enabled", defaultValue = "false")
  boolean cqrsEnabled;

  private String testUserId;
  private String testFeature;

  @BeforeEach
  void setUp() {
    testUserId = "test-user-" + UUID.randomUUID().toString().substring(0, 8);
    testFeature = "test-feature";
    
    // Cleanup: Reset User Session vor jedem Test
    commandService.resetUserSession(testUserId);
  }

  // =========================================================================================
  // 1. FEATURE FLAG TESTS - Kritisch für Migration
  // =========================================================================================

  @Test
  @DisplayName("Feature Flag: CQRS Mode aktiviert")
  void featureFlag_cqrsMode_shouldUseCommandAndQueryServices() {
    // Given: CQRS ist via TestProfile aktiviert
    assertThat(cqrsEnabled).isTrue();

    // When: Struggle Detection aufrufen
    Map<String, Object> context = Map.of("error", "validation_failed");
    UserStruggle result = facadeService.detectStruggle(testUserId, testFeature, context);

    // Then: Sollte über CQRS laufen (schwer direkt zu testen, aber prüfen wir Funktionalität)
    assertThat(result).isNotNull();
    assertThat(result.userId()).isEqualTo(testUserId);
    assertThat(result.feature()).isEqualTo(testFeature);
  }

  // =========================================================================================
  // 2. COMMAND SERVICE TESTS - Write Operations
  // =========================================================================================

  @Test
  @DisplayName("Command Service: Action Recording")
  void commandService_recordAction_shouldCreateSession() {
    // When: User Action aufzeichnen
    Map<String, Object> context = Map.of("action", "start", "timestamp", System.currentTimeMillis());
    commandService.recordUserAction(testUserId, testFeature, context);

    // Then: Session sollte existieren
    var sessions = UserStruggleDetectionCommandService.getUserSessions();
    assertThat(sessions).containsKey(testUserId);
    assertThat(sessions.get(testUserId).getActionCount()).isEqualTo(1);
    assertThat(sessions.get(testUserId).getUserId()).isEqualTo(testUserId);
  }

  @Test
  @DisplayName("Command Service: Multiple Actions and Counters")
  void commandService_multipleActions_shouldTrackCounters() {
    // Given: Verschiedene Aktionen mit Fehlern und Help Requests
    commandService.recordUserAction(testUserId, testFeature, Map.of("action", "start"));
    commandService.recordUserAction(testUserId, testFeature, Map.of("error", "validation_failed"));
    commandService.recordUserAction(testUserId, testFeature, Map.of("action", "help_request"));
    commandService.recordUserAction(testUserId, testFeature, Map.of("error", "timeout"));

    // When: Session prüfen
    var session = UserStruggleDetectionCommandService.getUserSessions().get(testUserId);

    // Then: Counters sollten korrekt sein
    assertThat(session.getActionCount()).isEqualTo(4);
    assertThat(session.getTotalFailures()).isEqualTo(2); // 2 Errors
    assertThat(session.getHelpRequestCount()).isEqualTo(1); // 1 Help Request
  }

  @Test
  @DisplayName("Command Service: Session Cleanup")
  void commandService_sessionCleanup_shouldRemoveOldSessions() {
    // Given: Test Session erstellen
    commandService.recordUserAction(testUserId, testFeature, Map.of("action", "test"));
    assertThat(UserStruggleDetectionCommandService.getUserSessions()).containsKey(testUserId);

    // When: Session explizit resetten
    commandService.resetUserSession(testUserId);

    // Then: Session sollte entfernt sein
    assertThat(UserStruggleDetectionCommandService.getUserSessions()).doesNotContainKey(testUserId);
  }

  // =========================================================================================
  // 3. QUERY SERVICE TESTS - Read Operations & Struggle Detection
  // =========================================================================================

  @Test
  @DisplayName("Query Service: No Session - No Struggle")
  void queryService_noSession_shouldReturnNoStruggle() {
    // When: Struggle Detection ohne Session
    UserStruggle result = queryService.detectStruggle(testUserId, testFeature, Map.of());

    // Then: Kein Struggle erkannt
    assertThat(result.isDetected()).isFalse();
    assertThat(result.getType()).isNull();
    assertThat(result.userId()).isEqualTo(testUserId);
    assertThat(result.feature()).isEqualTo(testFeature);
  }

  @Test
  @DisplayName("Query Service: Repeated Failures Detection")
  void queryService_repeatedFailures_shouldDetectStruggle() {
    // Given: Wiederholte Fehler aufzeichnen (Command)
    for (int i = 0; i < 4; i++) {
      commandService.recordUserAction(testUserId, testFeature, Map.of("error", "validation_failed"));
    }

    // When: Struggle Detection (Query)
    UserStruggle result = queryService.detectStruggle(testUserId, testFeature, Map.of("context", "test"));

    // Then: REPEATED_FAILED_ATTEMPTS erkannt
    assertThat(result.isDetected()).isTrue();
    assertThat(result.getType()).isEqualTo(UserStruggle.StruggleType.REPEATED_FAILED_ATTEMPTS);
    assertThat(result.getSeverity()).isGreaterThan(5); // Sollte hoch sein
    assertThat(result.suggestions()).isNotEmpty();
  }

  @Test
  @DisplayName("Query Service: Recent Actions Window")
  void queryService_recentActions_shouldReturnTimeFiltered() {
    // Given: Aktionen aufzeichnen
    commandService.recordUserAction(testUserId, testFeature, Map.of("action", "action1"));
    commandService.recordUserAction(testUserId, testFeature, Map.of("action", "action2"));

    // When: Recent Actions abrufen
    var recentActions = queryService.getRecentUserActions(testUserId, Duration.ofMinutes(1));

    // Then: Beide Aktionen sollten "recent" sein
    assertThat(recentActions).hasSize(2);
    assertThat(recentActions).extracting(action -> action.context.get("action"))
        .containsExactly("action1", "action2");
  }

  @Test
  @DisplayName("Query Service: Session Statistics")
  void queryService_sessionStats_shouldReturnCorrectData() {
    // Given: Session mit verschiedenen Aktionen
    commandService.recordUserAction(testUserId, testFeature, Map.of("action", "start"));
    commandService.recordUserAction(testUserId, testFeature, Map.of("error", "test_error"));
    commandService.recordUserAction(testUserId, testFeature, Map.of("action", "help_request"));

    // When: Session Stats abrufen
    var stats = queryService.getUserSessionStats(testUserId);

    // Then: Statistiken sollten korrekt sein
    assertThat(stats.getUserId()).isEqualTo(testUserId);
    assertThat(stats.getTotalActions()).isEqualTo(3);
    assertThat(stats.getTotalFailures()).isEqualTo(1);
    assertThat(stats.getHelpRequestCount()).isEqualTo(1);
    assertThat(stats.getLastActivity()).isNotNull();
    assertThat(stats.getSessionDuration()).isNotNull();
  }

  // =========================================================================================
  // 4. FACADE INTEGRATION TESTS - Command + Query Zusammenspiel
  // =========================================================================================

  @Test
  @DisplayName("Facade Integration: Full CQRS Flow")
  void facade_fullFlow_shouldIntegrateCommandAndQuery() {
    // Given: Mehrere Aktionen über Facade (sollte Command aufrufen)
    Map<String, Object> errorContext = Map.of("error", "critical_error", "timestamp", System.currentTimeMillis());
    
    // When: Erste Detection (erstellt Session via Command)
    UserStruggle result1 = facadeService.detectStruggle(testUserId, testFeature, errorContext);
    
    // Then: Noch kein Struggle (erst 1 Fehler)
    assertThat(result1.isDetected()).isFalse();

    // When: Weitere Fehler hinzufügen
    facadeService.detectStruggle(testUserId, testFeature, errorContext);
    facadeService.detectStruggle(testUserId, testFeature, errorContext);
    UserStruggle result2 = facadeService.detectStruggle(testUserId, testFeature, errorContext);

    // Then: Jetzt sollte Struggle erkannt werden (4 Fehler total)
    assertThat(result2.isDetected()).isTrue();
    assertThat(result2.getType()).isEqualTo(UserStruggle.StruggleType.REPEATED_FAILED_ATTEMPTS);
  }

  @Test
  @DisplayName("Facade Integration: Shared State Consistency")
  void facade_sharedState_shouldBeConsistentBetweenCommandAndQuery() {
    // Given: Actions über Command Service direkt
    commandService.recordUserAction(testUserId, testFeature, Map.of("test", "direct_command"));
    
    // When: Über Query Service lesen
    var recentActions = queryService.getRecentUserActions(testUserId, Duration.ofMinutes(1));
    
    // Then: Action sollte sichtbar sein (Shared State funktioniert)
    assertThat(recentActions).hasSize(1);
    assertThat(recentActions.get(0).context.get("test")).isEqualTo("direct_command");
    
    // When: Über Facade weitere Action hinzufügen
    facadeService.detectStruggle(testUserId, testFeature, Map.of("test", "via_facade"));
    
    // Then: Beide Actions sollten sichtbar sein
    var allActions = queryService.getRecentUserActions(testUserId, Duration.ofMinutes(1));
    assertThat(allActions).hasSize(2);
  }

  // =========================================================================================
  // 5. STRUGGLE DETECTION LOGIC TESTS
  // =========================================================================================

  @Test
  @DisplayName("Struggle Detection: Complex Form Struggle")
  void struggleDetection_complexForm_shouldDetectPattern() {
    // Given: Viele Feld-Änderungen ohne Submit
    for (int i = 0; i < 12; i++) {
      commandService.recordUserAction(testUserId, testFeature, 
          Map.of("action", "field_change", "field", "field_" + i));
    }

    // When: Struggle Detection
    UserStruggle result = queryService.detectStruggle(testUserId, testFeature, Map.of());

    // Then: COMPLEX_FORM_STRUGGLE erkannt
    assertThat(result.isDetected()).isTrue();
    assertThat(result.getType()).isEqualTo(UserStruggle.StruggleType.COMPLEX_FORM_STRUGGLE);
    assertThat(result.suggestions()).hasSize(3); // Sollte 3 Suggestions haben
  }

  @Test
  @DisplayName("Struggle Detection: Rapid Navigation")
  void struggleDetection_rapidNavigation_shouldDetectPattern() {
    // Given: Schnelle Navigation zwischen Features
    String[] features = {"feature1", "feature2", "feature3", "feature4", "feature5", "feature6"};
    for (String feature : features) {
      commandService.recordUserAction(testUserId, feature, Map.of("action", "navigate"));
    }

    // When: Struggle Detection
    UserStruggle result = queryService.detectStruggle(testUserId, "any_feature", Map.of());

    // Then: RAPID_NAVIGATION_CHANGES erkannt
    assertThat(result.isDetected()).isTrue();
    assertThat(result.getType()).isEqualTo(UserStruggle.StruggleType.RAPID_NAVIGATION_CHANGES);
  }

  // =========================================================================================
  // 6. ERROR HANDLING & EDGE CASES
  // =========================================================================================

  @Test
  @DisplayName("Error Handling: Empty Session Stats")
  void errorHandling_emptySession_shouldReturnEmptyStats() {
    // When: Stats für nicht-existierenden User
    var stats = queryService.getUserSessionStats("non-existent-user");

    // Then: Empty Stats zurückgeben
    assertThat(stats.getUserId()).isEqualTo("non-existent-user");
    assertThat(stats.getTotalActions()).isEqualTo(0);
    assertThat(stats.getTotalFailures()).isEqualTo(0);
    assertThat(stats.getHelpRequestCount()).isEqualTo(0);
    assertThat(stats.getLastActivity()).isNull();
  }

  @Test
  @DisplayName("Error Handling: Large Action History Cleanup")
  void errorHandling_largeHistory_shouldCleanupAutomatically() {
    // Given: Sehr viele Actions hinzufügen (über Limit)
    for (int i = 0; i < 1100; i++) { // Über das 1000er Limit
      commandService.recordUserAction(testUserId, testFeature, Map.of("action", "test_" + i));
    }

    // When: Session prüfen
    var session = UserStruggleDetectionCommandService.getUserSessions().get(testUserId);

    // Then: Sollte automatisch auf ca. 500 gekürzt sein
    assertThat(session.getActionCount()).isLessThanOrEqualTo(1000);
    assertThat(session.getActionCount()).isGreaterThan(500); // Sollte nicht komplett leer sein
  }

  // =========================================================================================
  // TEST CONFIGURATION
  // =========================================================================================

  /**
   * Test Profile um CQRS zu aktivieren
   */
  public static class CQRSTestProfile implements io.quarkus.test.junit.QuarkusTestProfile {
    @Override
    public java.util.Map<String, String> getConfigOverrides() {
      return java.util.Map.of(
          "features.cqrs.enabled", "true", // KRITISCH: CQRS für Test aktivieren
          "quarkus.log.level", "WARN" // Weniger Logs während Tests
      );
    }
  }
}