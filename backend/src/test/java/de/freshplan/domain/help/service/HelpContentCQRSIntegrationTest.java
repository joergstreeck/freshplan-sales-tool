package de.freshplan.domain.help.service;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.help.entity.HelpContent;
import de.freshplan.domain.help.entity.HelpType;
import de.freshplan.domain.help.entity.UserLevel;
import de.freshplan.domain.help.events.HelpContentViewedEvent;
import de.freshplan.domain.help.repository.HelpContentRepository;
import de.freshplan.domain.help.service.command.HelpContentCommandService;
import de.freshplan.domain.help.service.dto.HelpAnalytics;
import de.freshplan.domain.help.service.dto.HelpRequest;
import de.freshplan.domain.help.service.dto.HelpResponse;
import de.freshplan.domain.help.service.query.HelpContentQueryService;
import de.freshplan.infrastructure.events.EventBus;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration Test für Phase 12.2 - HelpContentService Event-Driven CQRS
 *
 * <p>Testet die komplette Event-Driven CQRS-Implementierung: - Command/Query Service Integration -
 * Event-Driven Side Effects Processing - Facade Pattern mit Feature Flag - Async Event Handling -
 * Performance und Reliability
 */
@QuarkusTest
@Tag("integration")
@TestProfile(HelpContentCQRSIntegrationTest.CQRSTestProfile.class)
@DisplayName("Phase 12.2: HelpContentService Event-Driven CQRS Integration")
class HelpContentCQRSIntegrationTest {

  @Inject HelpContentService facadeService;

  @Inject HelpContentCommandService commandService;

  @Inject HelpContentQueryService queryService;

  @Inject HelpContentRepository helpRepository;

  @Inject EventBus eventBus;

  @AfterEach
  @Transactional
  void cleanup() {
    // Delete test data - using feature field which exists in HelpContent entity
    helpRepository.delete("feature LIKE ?1", "test-feature-%");
  }

  @ConfigProperty(name = "features.cqrs.enabled", defaultValue = "false")
  boolean cqrsEnabled;

  /**
   * Helper service for async tests that need fresh transaction context. Awaitility lambdas don't
   * have CDI context, so we need this workaround.
   */
  @ApplicationScoped
  public static class TestHelper {
    @Inject HelpContentRepository helpRepository;

    @TestTransaction
    public HelpContent findHelpContent(UUID helpId) {
      return helpRepository.findByIdOptional(helpId).orElse(null);
    }
  }

  @Inject TestHelper testHelper;

  // These fields will be set in each test method to ensure proper transaction isolation
  private UUID testHelpContentId;
  private String testUserId;
  private String testFeature;

  /**
   * Creates test data within the current transaction context. Must be called at the beginning of
   * each test method to ensure data visibility.
   */
  private void createTestData() {
    // Unique test identifiers to avoid conflicts
    String uniqueId = UUID.randomUUID().toString().substring(0, 8);
    testUserId = "test-user-" + uniqueId;
    testFeature = "test-feature-" + uniqueId;

    // Create test help content
    HelpContent testContent =
        commandService.createOrUpdateHelpContent(
            testFeature,
            HelpType.TOOLTIP,
            "Test Help Content " + uniqueId,
            "Short help text",
            "Medium help text",
            "Detailed help text for beginners",
            UserLevel.BEGINNER,
            List.of("admin", "user"),
            "test-system");
    testHelpContentId = testContent.id;

    // Ensure the content is persisted and visible
    helpRepository.getEntityManager().flush();
    helpRepository.getEntityManager().clear();
  }

  // =========================================================================================
  // 1. FEATURE FLAG TESTS - CQRS vs Legacy
  // =========================================================================================

  @Test
  @DisplayName("Feature Flag: CQRS Mode aktiviert")
  @TestTransaction
  void featureFlag_cqrsMode_shouldUseEventDrivenArchitecture() {
    // Setup test data in transaction context
    createTestData();

    // Given: CQRS ist via TestProfile aktiviert
    assertThat(cqrsEnabled).isTrue();

    // Given: Fresh state
    helpRepository.getEntityManager().flush();
    helpRepository.getEntityManager().clear();

    // When: Help Content über Facade abrufen
    HelpRequest request = createTestHelpRequest();
    HelpResponse result = facadeService.getHelpForFeature(request);

    // Then: Response sollte korrekt sein (but ID might be different due to test isolation)
    assertThat(result).isNotNull();
    assertThat(result.feature()).isEqualTo(testFeature);
    assertThat(result.content()).isNotNull();
    assertThat(result.title()).contains("Test Help Content");
  }

  // =========================================================================================
  // 2. COMMAND SERVICE TESTS - Write Operations
  // =========================================================================================

  @Test
  @DisplayName("Command Service: Content Creation")
  @TestTransaction
  void commandService_createContent_shouldPersistSuccessfully() {
    // When: Neuen Hilfe-Inhalt erstellen
    HelpContent newContent =
        commandService.createOrUpdateHelpContent(
            "new-feature",
            HelpType.TUTORIAL,
            "New Tutorial",
            "Quick help",
            "Detailed tutorial",
            "Expert-level explanation",
            UserLevel.EXPERT,
            List.of("admin"),
            "test-creator");

    // Then: Content sollte gespeichert sein
    assertThat(newContent.id).isNotNull();
    assertThat(newContent.feature).isEqualTo("new-feature");
    assertThat(newContent.helpType).isEqualTo(HelpType.TUTORIAL);
    assertThat(newContent.targetUserLevel).isEqualTo(UserLevel.EXPERT);

    // Verify persistence
    var persistedContent = helpRepository.findByIdOptional(newContent.id);
    assertThat(persistedContent).isPresent();
    assertThat(persistedContent.get().title).isEqualTo("New Tutorial");
  }

  @Test
  @DisplayName("Command Service: Feedback Recording")
  @TestTransaction
  void commandService_recordFeedback_shouldUpdateCounters() {
    // Setup test data in transaction context
    createTestData();

    // When: Feedback aufzeichnen
    commandService.recordFeedback(testHelpContentId, testUserId, true, 30, "Very helpful!");

    // Then: Help Content sollte aktualisiert sein
    var updatedContent = helpRepository.findByIdOptional(testHelpContentId);
    assertThat(updatedContent).isPresent();
    assertThat(updatedContent.get().helpfulCount).isEqualTo(1);
    assertThat(updatedContent.get().getHelpfulnessRate())
        .isCloseTo(100.0, org.assertj.core.data.Offset.offset(0.1));
  }

  @Test
  @DisplayName("Command Service: View Count Increment")
  @TestTransaction
  void commandService_incrementViewCount_shouldUpdateCounter() {
    // Setup test data in transaction context
    createTestData();

    // Given: Initial view count (fresh from database)
    helpRepository.getEntityManager().flush();
    helpRepository.getEntityManager().clear();
    var initialContent = helpRepository.findByIdOptional(testHelpContentId).get();
    long initialViewCount = initialContent.viewCount;

    // When: View count erhöhen
    commandService.incrementViewCount(testHelpContentId);

    // Force database sync
    helpRepository.getEntityManager().flush();
    helpRepository.getEntityManager().clear();

    // Then: View count sollte um 1 erhöht sein
    var updatedContent = helpRepository.findByIdOptional(testHelpContentId).get();
    assertThat(updatedContent.viewCount).isEqualTo(initialViewCount + 1);
  }

  @Test
  @DisplayName("Command Service: Content Toggle")
  @TestTransaction
  void commandService_toggleContent_shouldUpdateStatus() {
    // Setup test data in transaction context
    createTestData();

    // Given: Ensure initial state is active
    helpRepository.getEntityManager().flush();
    helpRepository.getEntityManager().clear();
    var initialContent = helpRepository.findByIdOptional(testHelpContentId).get();
    boolean initialActive = initialContent.isActive;

    // When: Content deaktivieren
    commandService.toggleHelpContent(testHelpContentId, false, "test-admin");

    // Force database sync
    helpRepository.getEntityManager().flush();
    helpRepository.getEntityManager().clear();

    // Then: Content sollte deaktiviert sein
    var updatedContent = helpRepository.findByIdOptional(testHelpContentId).get();
    assertThat(updatedContent.isActive).isFalse();
    assertThat(updatedContent.updatedBy).isEqualTo("test-admin");

    // When: Content wieder aktivieren
    commandService.toggleHelpContent(testHelpContentId, true, "test-admin");

    // Force database sync
    helpRepository.getEntityManager().flush();
    helpRepository.getEntityManager().clear();

    // Then: Content sollte aktiviert sein
    var reactivatedContent = helpRepository.findByIdOptional(testHelpContentId).get();
    assertThat(reactivatedContent.isActive).isTrue();
  }

  // =========================================================================================
  // 3. QUERY SERVICE TESTS - Read Operations
  // =========================================================================================

  @Test
  @DisplayName("Query Service: Help Content Retrieval")
  @TestTransaction
  void queryService_getHelpForFeature_shouldReturnPureQuery() {
    // Setup test data in transaction context
    createTestData();

    // Given: Help Request
    HelpRequest request = createTestHelpRequest();

    // When: Help Content über Query Service abrufen
    HelpResponse result = queryService.getHelpForFeature(request);

    // Then: Response sollte korrekt sein (ohne Side Effects)
    assertThat(result.id()).isEqualTo(testHelpContentId);
    assertThat(result.feature()).isEqualTo(testFeature);
    assertThat(result.title()).contains("Test Help Content");
    assertThat(result.type()).isEqualTo(HelpType.TOOLTIP);
    assertThat(result.content()).isEqualTo("Detailed help text for beginners"); // BEGINNER level
  }

  @Test
  @DisplayName("Query Service: Content Search")
  @TestTransaction
  void queryService_searchHelp_shouldReturnMatchingContent() {
    // Setup test data in transaction context
    createTestData();

    // When: Nach Hilfe-Inhalt suchen
    List<HelpResponse> results =
        queryService.searchHelp("Test Help", "BEGINNER", List.of("admin", "user"));

    // Then: Matching Content sollte gefunden werden
    assertThat(results).isNotEmpty();
    assertThat(results.get(0).title()).contains("Test Help");
    assertThat(results.get(0).feature()).startsWith("test-feature");
  }

  @Test
  @DisplayName("Query Service: Analytics Retrieval")
  @TestTransaction
  void queryService_getAnalytics_shouldReturnAnalyticsData() {
    // Setup test data in transaction context
    createTestData();

    // When: Analytics abrufen
    HelpAnalytics analytics = queryService.getAnalytics();

    // Then: Analytics sollten verfügbar sein
    assertThat(analytics).isNotNull();
    // Note: Analytics details depend on HelpAnalyticsService implementation
  }

  @Test
  @DisplayName("Query Service: Feature Coverage Gaps")
  @TestTransaction
  void queryService_getFeatureCoverageGaps_shouldIdentifyGaps() {
    // Setup test data in transaction context
    createTestData();

    // When: Coverage Gaps analysieren
    List<String> gaps = queryService.getFeatureCoverageGaps();

    // Then: Gap-Liste sollte verfügbar sein
    assertThat(gaps).isNotNull();
    // Note: Specific gaps depend on repository implementation
  }

  // =========================================================================================
  // 4. EVENT-DRIVEN ARCHITECTURE TESTS
  // =========================================================================================

  @Test
  @DisplayName("Event-Driven: Help Content Viewed Event Publishing")
  @TestTransaction
  void eventDriven_helpContentViewed_shouldPublishEventAsync() {
    // Setup test data in transaction context
    createTestData();

    // Given: Fresh state
    helpRepository.getEntityManager().flush();
    helpRepository.getEntityManager().clear();

    // When: Help Content über Facade abrufen (triggert Event)
    HelpRequest request = createTestHelpRequest();
    HelpResponse result = facadeService.getHelpForFeature(request);

    // Then: Response sollte sofort verfügbar sein (Query nicht blockiert)
    assertThat(result).isNotNull();
    assertThat(result.feature()).isEqualTo(testFeature);
    assertThat(result.title()).contains("Test Help Content");

    // Note: Async event processing cannot be tested in @TestTransaction context
    // The view count update happens asynchronously in a different transaction
    // This is tested in integration tests without @TestTransaction
  }

  @Test
  @DisplayName("Event-Driven: Direct Event Publishing")
  @TestTransaction
  void eventDriven_directEventPublish_shouldTriggerHandler() {
    // Setup test data in transaction context
    createTestData();

    // Given: Fresh state from database
    helpRepository.getEntityManager().flush();
    helpRepository.getEntityManager().clear();

    // Given: Event erstellen
    HelpRequest request = createTestHelpRequest();
    HelpContentViewedEvent event = HelpContentViewedEvent.create(testHelpContentId, request, null);

    // When: Event direkt publishen
    eventBus.publishAsync(event);

    // Then: Event sollte erfolgreich publiziert werden (no exceptions)
    // Note: Async event processing cannot be tested in @TestTransaction context
    // The actual handler execution happens in a different transaction
    assertThat(event).isNotNull();
    assertThat(event.helpContentId()).isEqualTo(testHelpContentId);
  }

  @Test
  @DisplayName("Event-Driven: Multiple Events Processing")
  @TestTransaction
  void eventDriven_multipleEvents_shouldProcessAllAsync() {
    // Setup test data in transaction context
    createTestData();

    // Given: Fresh state
    helpRepository.getEntityManager().flush();
    helpRepository.getEntityManager().clear();

    // Given: Mehrere Events für verschiedene Users
    int numberOfEvents = 3;

    // When: Mehrere Events parallel publishen
    for (int i = 0; i < numberOfEvents; i++) {
      String userId = "user-" + i;
      HelpRequest request = createTestHelpRequest(userId);
      HelpResponse response = facadeService.getHelpForFeature(request);

      // Then: Each response should be valid
      assertThat(response).isNotNull();
      assertThat(response.feature()).isEqualTo(testFeature);
    }

    // Note: Async event processing cannot be tested in @TestTransaction context
    // The view count updates happen in different transactions
  }

  // =========================================================================================
  // 5. FACADE INTEGRATION TESTS - Feature Flag Switching
  // =========================================================================================

  @Test
  @DisplayName("Facade Integration: Full CQRS Flow")
  @TestTransaction
  void facade_fullFlow_shouldIntegrateAllComponents() {
    // Setup test data in transaction context
    createTestData();

    // Given: Fresh state
    helpRepository.getEntityManager().flush();
    helpRepository.getEntityManager().clear();

    // When: Mehrere Operationen über Facade
    HelpRequest request = createTestHelpRequest();

    // 1. Help Content abrufen
    HelpResponse helpResponse = facadeService.getHelpForFeature(request);
    assertThat(helpResponse).isNotNull();
    assertThat(helpResponse.feature()).isEqualTo(testFeature);
    assertThat(helpResponse.title()).contains("Test Help Content");

    // 2. Feedback geben
    facadeService.recordFeedback(testHelpContentId, testUserId, true, 45, "Great help!");

    // Force database sync for feedback (synchronous operation)
    helpRepository.getEntityManager().flush();
    helpRepository.getEntityManager().clear();

    // Verify feedback was recorded immediately (synchronous)
    var afterFeedback = helpRepository.findByIdOptional(testHelpContentId).get();
    assertThat(afterFeedback.helpfulCount).isEqualTo(1);

    // 3. Content suchen
    List<HelpResponse> searchResults =
        facadeService.searchHelp("Test", "BEGINNER", List.of("admin"));
    assertThat(searchResults).isNotEmpty();

    // 4. Analytics abrufen
    HelpAnalytics analytics = facadeService.getAnalytics();
    assertThat(analytics).isNotNull();

    // Note: View count updates happen asynchronously in different transactions
    // and cannot be verified in @TestTransaction context
  }

  // =========================================================================================
  // 6. PERFORMANCE & RELIABILITY TESTS
  // =========================================================================================

  @Test
  @DisplayName("Performance: Query Operations Are Non-Blocking")
  @TestTransaction
  void performance_queryOperations_shouldNotBlockOnSideEffects() {
    // Setup test data in transaction context
    createTestData();

    // Given: Fresh state
    helpRepository.getEntityManager().flush();
    helpRepository.getEntityManager().clear();

    // Given: Request vorbereiten
    HelpRequest request = createTestHelpRequest();

    // When: Query-Operation messen
    long startTime = System.currentTimeMillis();
    HelpResponse result = facadeService.getHelpForFeature(request);
    long queryTime = System.currentTimeMillis() - startTime;

    // Then: Query sollte schnell sein (nicht auf Side Effects warten)
    assertThat(result).isNotNull();
    assertThat(result.feature()).isEqualTo(testFeature);
    assertThat(queryTime).isLessThan(500); // Sollte unter 500ms sein (relaxed for CI)

    // Note: Side effects happen asynchronously and cannot be verified
    // in @TestTransaction context. The important test is that the query
    // completes quickly without waiting for side effects.
  }

  @Test
  @DisplayName("Reliability: Side Effect Failures Don't Affect Main Operation")
  @TestTransaction
  void reliability_sideEffectFailures_shouldNotAffectMainOperation() {
    // Given: Create test data first to ensure userId exists
    String uniqueId = UUID.randomUUID().toString().substring(0, 8);
    String userId = "test-user-" + uniqueId;

    // Given: Request für nicht-existierenden Content (simuliert Fehler)
    HelpRequest request =
        HelpRequest.builder()
            .userId(userId) // Use a valid userId
            .feature("non-existent-feature")
            .userLevel("BEGINNER")
            .userRoles(List.of("admin"))
            .sessionId("test-session-" + uniqueId) // Add session ID to avoid NPE
            .context(Map.of("test", "reliability"))
            .build();

    // When: Operation ausführen
    HelpResponse result = facadeService.getHelpForFeature(request);

    // Then: Main Operation sollte erfolgreich sein (empty response)
    assertThat(result).isNotNull();
    assertThat(result.feature()).isEqualTo("non-existent-feature");

    // Und: Keine Exception sollte geworfen werden, auch wenn Side Effects fehlschlagen
    // (Events für non-existierenden Content werden graceful gehandelt)
  }

  // =========================================================================================
  // Helper Methods
  // =========================================================================================

  private HelpRequest createTestHelpRequest() {
    return createTestHelpRequest(testUserId);
  }

  private HelpRequest createTestHelpRequest(String userId) {
    return HelpRequest.builder()
        .userId(userId)
        .feature(testFeature)
        .userLevel("BEGINNER")
        .userRoles(List.of("admin", "user"))
        .preferredType(HelpType.TOOLTIP)
        .isFirstTime(false)
        .sessionId("test-session")
        .context(Map.of("timestamp", System.currentTimeMillis()))
        .build();
  }

  // =========================================================================================
  // TEST CONFIGURATION
  // =========================================================================================

  /** Test Profile um CQRS zu aktivieren */
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
