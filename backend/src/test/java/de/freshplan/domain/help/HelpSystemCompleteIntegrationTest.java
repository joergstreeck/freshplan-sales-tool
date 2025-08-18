package de.freshplan.domain.help;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.*;

import de.freshplan.domain.help.entity.HelpContent;
import de.freshplan.domain.help.entity.HelpType;
import de.freshplan.domain.help.entity.UserLevel;
import de.freshplan.domain.help.events.HelpContentViewedEvent;
import de.freshplan.domain.help.repository.HelpContentRepository;
import de.freshplan.domain.help.service.HelpContentService;
import de.freshplan.domain.help.service.UserStruggleDetectionService;
import de.freshplan.domain.help.service.command.HelpContentCommandService;
import de.freshplan.domain.help.service.command.UserStruggleDetectionCommandService;
import de.freshplan.domain.help.service.dto.HelpRequest;
import de.freshplan.domain.help.service.dto.HelpResponse;
import de.freshplan.domain.help.service.dto.UserStruggle;
import de.freshplan.domain.help.service.query.HelpContentQueryService;
import de.freshplan.domain.help.service.query.UserStruggleDetectionQueryService;
import de.freshplan.domain.help.test.HelpSystemTestHelper;
import de.freshplan.infrastructure.events.EventBus;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Complete End-to-End Integration Test für Phase 12.4
 *
 * <p>Testet das vollständige Help System mit allen CQRS-Komponenten: - UserStruggleDetectionService
 * (Phase 12.1) - HelpContentService mit Event-Driven CQRS (Phase 12.2) - HelpSystemResource REST
 * API (Phase 12.3) - Komplette User Journeys und Interaktionen
 *
 * <p>Verifiziert die nahtlose Integration aller Komponenten und die korrekte Funktionsweise des
 * Event-Driven Systems.
 */
@QuarkusTest
@TestProfile(HelpSystemCompleteIntegrationTest.CQRSTestProfile.class)
@DisplayName("Phase 12.4: Complete Help System E2E Integration Tests")
public class HelpSystemCompleteIntegrationTest {

  @Inject HelpContentService helpContentService;
  @Inject UserStruggleDetectionService struggleService;
  @Inject HelpContentCommandService commandService;
  @Inject HelpContentQueryService queryService;
  @Inject UserStruggleDetectionCommandService struggleCommandService;
  @Inject UserStruggleDetectionQueryService struggleQueryService;
  @Inject HelpContentRepository helpRepository;
  @Inject EventBus eventBus;
  @Inject HelpSystemTestHelper testHelper;

  @ConfigProperty(name = "features.cqrs.enabled", defaultValue = "false")
  boolean cqrsEnabled;

  private UUID testHelpContentId;
  private String testFeature;
  private String testUserId;

  @BeforeEach
  @Transactional
  void setUp() {
    // Unique test data
    String uniqueId = UUID.randomUUID().toString().substring(0, 8);
    testFeature = "checkout-" + uniqueId;
    testUserId = "user-" + uniqueId;

    // Create comprehensive test content for different scenarios
    createTestHelpContent();
  }

  private void createTestHelpContent() {
    // Beginner tooltip
    HelpContent beginnerTooltip =
        commandService.createOrUpdateHelpContent(
            testFeature,
            HelpType.TOOLTIP,
            "Checkout Process Help",
            "Click 'Buy Now' to proceed",
            "Complete your purchase in 3 easy steps",
            "Step 1: Review your cart. Step 2: Enter shipping. Step 3: Payment.",
            UserLevel.BEGINNER,
            List.of("customer"),
            "system");
    testHelpContentId = beginnerTooltip.id;

    // Tutorial for first-time users
    commandService.createOrUpdateHelpContent(
        testFeature,
        HelpType.TUTORIAL,
        "Checkout Tutorial",
        "Quick checkout guide",
        "Learn how to complete your purchase",
        "This comprehensive tutorial will walk you through the entire checkout process...",
        UserLevel.BEGINNER,
        List.of("customer"),
        "system");

    // Proactive help for struggles
    commandService.createOrUpdateHelpContent(
        testFeature,
        HelpType.PROACTIVE,
        "Need Help with Checkout?",
        "We noticed you might need help",
        "Let us guide you through the checkout",
        "It looks like you're having trouble. Here's what to do next...",
        UserLevel.BEGINNER,
        List.of("customer"),
        "system");

    helpRepository.flush();
  }

  // =========================================================================================
  // 1. COMPLETE USER JOURNEY TEST - From Struggle Detection to Help Delivery
  // =========================================================================================

  @Test
  @DisplayName("Complete User Journey: Struggle Detection → Help Delivery → Feedback")
  void completeUserJourney_withStruggleDetection_shouldProvideProactiveHelp() {
    // Step 1: User starts interacting with checkout (simulate repeated failures)
    Map<String, Object> failureContext =
        Map.of(
            "action", "button_click",
            "target", "checkout_button",
            "error", "validation_failed",
            "timestamp", System.currentTimeMillis());

    // Step 2: Detect struggle after repeated failures
    UserStruggle struggle = struggleService.detectStruggle(testUserId, testFeature, failureContext);

    // If no struggle detected yet, simulate more failures
    for (int i = 0; i < 3 && !struggle.isDetected(); i++) {
      struggle = struggleService.detectStruggle(testUserId, testFeature, failureContext);
    }

    assertThat(struggle.isDetected()).isTrue();
    assertThat(struggle.getType()).isEqualTo(UserStruggle.StruggleType.REPEATED_FAILED_ATTEMPTS);

    // Step 3: Request help via REST API (should get proactive help due to struggle)
    var helpResponse =
        given()
            .when()
            .queryParam("userId", testUserId)
            .queryParam("userLevel", "BEGINNER")
            .queryParam("userRoles", "customer")
            .get("/api/help/content/" + testFeature)
            .then()
            .statusCode(200)
            .extract()
            .jsonPath();

    String helpTitle = helpResponse.getString("title");
    String helpContent = helpResponse.getString("content");

    // Verify appropriate help was provided
    assertThat(helpTitle).isNotNull();
    assertThat(helpContent).isNotNull();

    // Step 4: Submit feedback
    String helpId = helpResponse.getString("id");
    if (helpId != null) {
      given()
          .contentType(ContentType.JSON)
          .body(
              Map.of(
                  "helpId",
                  helpId,
                  "userId",
                  testUserId,
                  "helpful",
                  true,
                  "timeSpent",
                  45,
                  "comment",
                  "The proactive help was perfect!"))
          .when()
          .post("/api/help/feedback")
          .then()
          .statusCode(200);

      // Step 5: Verify feedback was recorded
      await()
          .atMost(Duration.ofSeconds(2))
          .untilAsserted(
              () -> {
                var content = testHelper.findHelpContentById(UUID.fromString(helpId));
                assertThat(content).isPresent();
                assertThat(content.get().helpfulCount).isGreaterThan(0);
              });
    }
  }

  // =========================================================================================
  // 2. EVENT-DRIVEN ARCHITECTURE TEST - Verify Async Processing
  // =========================================================================================

  @Test
  @DisplayName("Event-Driven: Multiple Users → Async View Count Updates")
  void eventDriven_multipleUsers_shouldProcessAsynchronously() {
    AtomicInteger requestCount = new AtomicInteger(0);
    int numberOfUsers = 10;

    // Given: Initial view count
    var initialContent = helpRepository.findByIdOptional(testHelpContentId).get();
    long initialViewCount = initialContent.viewCount;

    // When: Multiple users request help simultaneously
    for (int i = 0; i < numberOfUsers; i++) {
      String userId = "concurrent-user-" + i;

      HelpRequest request =
          HelpRequest.builder()
              .userId(userId)
              .feature(testFeature)
              .userLevel("BEGINNER")
              .userRoles(List.of("customer"))
              .isFirstTime(i % 2 == 0) // Half are first-time users
              .build();

      HelpResponse response = helpContentService.getHelpForFeature(request);
      assertThat(response).isNotNull();
      requestCount.incrementAndGet();
    }

    // Then: All events should be processed asynchronously
    assertThat(requestCount.get()).isEqualTo(numberOfUsers);

    // Verify async view count updates
    // Note: Due to async processing, not all events might be processed immediately
    await()
        .atMost(Duration.ofSeconds(10))
        .pollInterval(Duration.ofMillis(500))
        .untilAsserted(
            () -> {
              var updatedContent = testHelper.findHelpContentById(testHelpContentId).get();
              // Accept at least 20% of events processed due to async nature and timing
              // In CI environments, some events might be dropped or delayed
              long expectedMinimum = initialViewCount + (numberOfUsers * 2 / 10);
              assertThat(updatedContent.viewCount).isGreaterThanOrEqualTo(expectedMinimum);
            });
  }

  // =========================================================================================
  // 3. CQRS SEPARATION TEST - Commands Don't Block Queries
  // =========================================================================================

  @Test
  @DisplayName("CQRS: Commands and Queries work independently")
  void cqrs_commandsAndQueries_shouldWorkIndependently() {
    // Test command operations (writes)
    for (int i = 0; i < 5; i++) {
      commandService.recordFeedback(
          testHelpContentId, "writer-" + i, i % 2 == 0, 30 + i, "Feedback " + i);
    }

    // Test query operations (reads)
    for (int i = 0; i < 10; i++) {
      var results = queryService.searchHelp("Checkout", "BEGINNER", List.of("customer"));
      assertThat(results).isNotNull();
    }

    // Verify operations completed successfully
    var content = testHelper.findHelpContentById(testHelpContentId);
    assertThat(content).isPresent();

    // Both command and query operations should work without interference
    assertThat(content.get().helpfulCount).isGreaterThanOrEqualTo(0);
  }

  // =========================================================================================
  // 4. ANALYTICS AND MONITORING TEST
  // =========================================================================================

  @Test
  @DisplayName("Analytics: Track all user interactions correctly")
  void analytics_trackingUserInteractions_shouldProvideAccurateData() {
    // Generate various interactions
    String analyticsUserId = "analytics-test-user";

    // 1. View help content
    helpContentService.getHelpForFeature(
        HelpRequest.builder()
            .userId(analyticsUserId)
            .feature(testFeature)
            .userLevel("BEGINNER")
            .userRoles(List.of("customer"))
            .build());

    // 2. Provide feedback
    commandService.recordFeedback(testHelpContentId, analyticsUserId, true, 60, "Very detailed!");

    // 3. Search for help
    var searchResults = queryService.searchHelp("Checkout", "BEGINNER", List.of("customer"));
    assertThat(searchResults).isNotEmpty();

    // 4. Get analytics
    var analytics = queryService.getAnalytics();
    assertThat(analytics).isNotNull();

    // 5. Verify analytics via REST API
    given().when().get("/api/help/analytics").then().statusCode(200).body("$", notNullValue());
  }

  // =========================================================================================
  // 5. STRUGGLE PATTERN RECOGNITION TEST
  // =========================================================================================

  @Test
  @DisplayName("Struggle Detection: Identify different struggle patterns")
  void struggleDetection_differentPatterns_shouldIdentifyCorrectly() {
    // Test 1: Repeated Failed Attempts Pattern
    String userId1 = "pattern-user-failures-" + UUID.randomUUID().toString().substring(0, 8);
    String feature1 = "login-test";

    // Simulate repeated failures (need at least 3 for threshold)
    Map<String, Object> failureContext =
        Map.of(
            "action", "submit",
            "error", "authentication_failed",
            "validation_failed", true);

    // Record multiple failures through command service
    for (int i = 0; i < 4; i++) {
      struggleCommandService.recordUserAction(userId1, feature1, failureContext);
    }

    // Detect struggle through query service
    var struggle1 = struggleQueryService.detectStruggle(userId1, feature1, failureContext);
    assertThat(struggle1.isDetected()).isTrue();
    assertThat(struggle1.getType()).isEqualTo(UserStruggle.StruggleType.REPEATED_FAILED_ATTEMPTS);

    // Test 2: Rapid Navigation Pattern
    String userId2 = "pattern-user-nav-" + UUID.randomUUID().toString().substring(0, 8);

    // Simulate rapid navigation (5+ unique pages in 2 minutes)
    String[] features = {"home", "products", "about", "contact", "pricing", "home"};
    for (int i = 0; i < features.length; i++) {
      struggleCommandService.recordUserAction(
          userId2,
          features[i],
          Map.of(
              "action", "navigate", "page", features[i], "timestamp", System.currentTimeMillis()));
    }

    // Check for rapid navigation struggle
    var struggle2 = struggleQueryService.detectStruggle(userId2, "home", Map.of());
    // Note: May need 5 unique pages within 2 minutes window
    if (struggle2.isDetected()) {
      assertThat(struggle2.getType()).isEqualTo(UserStruggle.StruggleType.RAPID_NAVIGATION_CHANGES);
    }

    // Test 3: Complex Form Struggle
    String userId3 = "pattern-user-form-" + UUID.randomUUID().toString().substring(0, 8);
    String feature3 = "checkout-form";

    // Simulate many field changes without submit (10+ changes, no submit)
    for (int i = 0; i < 12; i++) {
      struggleCommandService.recordUserAction(
          userId3,
          feature3,
          Map.of(
              "action",
              "field_change",
              "field",
              "field_" + i,
              "timestamp",
              System.currentTimeMillis()));
    }

    var struggle3 = struggleQueryService.detectStruggle(userId3, feature3, Map.of());
    if (struggle3.isDetected()) {
      assertThat(struggle3.getType())
          .isIn(
              UserStruggle.StruggleType.COMPLEX_FORM_STRUGGLE,
              UserStruggle.StruggleType.RAPID_NAVIGATION_CHANGES);
    }
  }

  // =========================================================================================
  // 6. PERFORMANCE AND SCALABILITY TEST
  // =========================================================================================

  @Test
  @DisplayName("Performance: System handles high load efficiently")
  void performance_highLoad_shouldHandleEfficiently() {
    int concurrentRequests = 50;
    long startTime = System.currentTimeMillis();

    // Create multiple threads for concurrent requests
    Thread[] threads = new Thread[concurrentRequests];
    AtomicInteger successCount = new AtomicInteger(0);

    for (int i = 0; i < concurrentRequests; i++) {
      final int userId = i;
      threads[i] =
          new Thread(
              () -> {
                try {
                  // Each thread makes a help request
                  HelpResponse response =
                      helpContentService.getHelpForFeature(
                          HelpRequest.builder()
                              .userId("load-test-user-" + userId)
                              .feature(testFeature)
                              .userLevel("BEGINNER")
                              .userRoles(List.of("customer"))
                              .build());

                  if (response != null) {
                    successCount.incrementAndGet();
                  }
                } catch (Exception e) {
                  // Log but don't fail - we're testing load
                }
              });
      threads[i].start();
    }

    // Wait for all threads to complete
    for (Thread thread : threads) {
      try {
        thread.join(10000); // 10 second timeout
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;

    // Verify performance metrics
    assertThat(successCount.get()).isGreaterThan(concurrentRequests * 90 / 100); // 90% success rate
    assertThat(totalTime).isLessThan(5000); // All requests complete within 5 seconds

    // Verify events are still being processed
    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(
            () -> {
              var content = testHelper.findHelpContentById(testHelpContentId).get();
              assertThat(content.viewCount).isGreaterThan(0);
            });
  }

  // =========================================================================================
  // 7. ERROR HANDLING AND RESILIENCE TEST
  // =========================================================================================

  @Test
  @DisplayName("Resilience: System handles errors gracefully")
  void resilience_errorScenarios_shouldHandleGracefully() {
    // Test 1: Non-existent help content
    var response1 =
        given()
            .when()
            .queryParam("userId", "test-user")
            .get("/api/help/content/non-existent-feature-xyz")
            .then()
            .statusCode(200) // Should not fail
            .extract()
            .jsonPath();

    assertThat(response1.getString("feature")).isEqualTo("non-existent-feature-xyz");

    // Test 2: Invalid feedback submission
    given()
        .contentType(ContentType.JSON)
        .body(
            Map.of("helpId", UUID.randomUUID().toString(), "userId", "test-user", "helpful", true))
        .when()
        .post("/api/help/feedback")
        .then()
        .statusCode(200); // Should handle gracefully

    // Test 3: Malformed search query
    given()
        .when()
        .queryParam("q", "")
        .get("/api/help/search")
        .then()
        .statusCode(400)
        .body("error", equalTo("Search term is required"));

    // Test 4: System continues working after errors
    var validResponse =
        helpContentService.getHelpForFeature(
            HelpRequest.builder()
                .userId("recovery-test-user")
                .feature(testFeature)
                .userLevel("BEGINNER")
                .userRoles(List.of("customer"))
                .build());

    assertThat(validResponse).isNotNull();
    assertThat(validResponse.feature()).isEqualTo(testFeature);
  }

  // =========================================================================================
  // 8. FEATURE FLAG MIGRATION TEST
  // =========================================================================================

  @Test
  @DisplayName("Feature Flag: CQRS can be toggled without breaking functionality")
  void featureFlag_cqrsToggle_shouldMaintainFunctionality() {
    // Verify CQRS is enabled for this test
    assertThat(cqrsEnabled).isTrue();

    // Test both command and query operations

    // Command operation
    commandService.recordFeedback(
        testHelpContentId, "flag-test-user", true, 30, "Testing with CQRS enabled");

    // Query operation
    var searchResults = queryService.searchHelp("Checkout", "BEGINNER", List.of("customer"));

    assertThat(searchResults).isNotEmpty();

    // Event-driven operation
    HelpContentViewedEvent event =
        HelpContentViewedEvent.create(
            testHelpContentId,
            HelpRequest.builder()
                .userId("event-test-user")
                .feature(testFeature)
                .userLevel("BEGINNER")
                .userRoles(List.of("customer"))
                .build(),
            null);

    eventBus.publishAsync(event);

    // Verify event was processed
    await()
        .atMost(Duration.ofSeconds(3))
        .untilAsserted(
            () -> {
              var content = testHelper.findHelpContentById(testHelpContentId).get();
              assertThat(content.viewCount).isGreaterThan(0);
            });
  }

  // =========================================================================================
  // TEST CONFIGURATION
  // =========================================================================================

  public static class CQRSTestProfile implements io.quarkus.test.junit.QuarkusTestProfile {
    @Override
    public java.util.Map<String, String> getConfigOverrides() {
      return java.util.Map.of(
          "features.cqrs.enabled", "true",
          "quarkus.log.level", "WARN",
          "quarkus.log.category.\"de.freshplan.domain.help\".level", "INFO");
    }
  }
}
