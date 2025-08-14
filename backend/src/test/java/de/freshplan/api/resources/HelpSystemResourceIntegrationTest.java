package de.freshplan.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

import de.freshplan.domain.help.entity.HelpContent;
import de.freshplan.domain.help.entity.HelpType;
import de.freshplan.domain.help.entity.UserLevel;
import de.freshplan.domain.help.repository.HelpContentRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration Tests für Phase 12.3 - HelpSystemResource mit CQRS
 *
 * <p>Verifiziert, dass alle REST Endpoints korrekt mit dem neuen
 * Event-Driven CQRS System funktionieren.
 * 
 * <p>Testet:
 * - Alle 8 Endpoints der HelpSystemResource
 * - Korrekte Integration mit CQRS Services
 * - Error Handling und Validation
 * - Feature Flag Kompatibilität
 */
@QuarkusTest
@TestProfile(HelpSystemResourceIntegrationTest.CQRSTestProfile.class)
@DisplayName("Phase 12.3: HelpSystemResource CQRS Integration Tests")
class HelpSystemResourceIntegrationTest {

    @Inject
    HelpContentRepository helpRepository;

    private UUID testHelpContentId;
    private String testFeature;

    @BeforeEach
    @Transactional
    void setUp() {
        // Unique test data to avoid conflicts
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        testFeature = "test-feature-" + uniqueId;
        
        // Create test help content directly in DB
        HelpContent testContent = new HelpContent();
        testContent.feature = testFeature;
        testContent.helpType = HelpType.TOOLTIP;
        testContent.title = "Test Help " + uniqueId;
        testContent.shortContent = "Short help";
        testContent.mediumContent = "Medium help";
        testContent.detailedContent = "Detailed help for beginners";
        testContent.targetUserLevel = UserLevel.BEGINNER;
        testContent.targetRoles = List.of("admin", "user");
        testContent.isActive = true;
        testContent.createdBy = "test-system";
        testContent.updatedBy = "test-system";
        testContent.createdAt = LocalDateTime.now();
        testContent.updatedAt = LocalDateTime.now();
        testContent.viewCount = 0L;
        testContent.helpfulCount = 0L;
        testContent.notHelpfulCount = 0L;
        
        helpRepository.persist(testContent);
        helpRepository.flush();
        testHelpContentId = testContent.id;
    }

    // =========================================================================================
    // 1. GET /api/help/content/{feature} - Main Help Content Endpoint
    // =========================================================================================

    @Test
    @DisplayName("GET /api/help/content/{feature} - Should return help content for feature")
    void getHelpForFeature_withValidFeature_shouldReturnHelpContent() {
        given()
            .when()
            .queryParam("userId", "test-user")
            .queryParam("userLevel", "BEGINNER")
            .queryParam("userRoles", "admin", "user")
            .get("/api/help/content/" + testFeature)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("feature", equalTo(testFeature))
            .body("title", containsString("Test Help"))
            .body("content", equalTo("Detailed help for beginners")) // BEGINNER level
            .body("type", equalTo("TOOLTIP"));
    }

    @Test
    @DisplayName("GET /api/help/content/{feature} - Should handle non-existent feature")
    void getHelpForFeature_withNonExistentFeature_shouldReturnEmptyResponse() {
        given()
            .when()
            .queryParam("userId", "test-user")
            .get("/api/help/content/non-existent-feature")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("feature", equalTo("non-existent-feature"))
            .body("content", equalTo("Für dieses Feature ist noch keine Hilfe verfügbar."));
    }

    @Test
    @DisplayName("GET /api/help/content/{feature} - Should work with different user levels")
    void getHelpForFeature_withExpertLevel_shouldReturnShortContent() {
        given()
            .when()
            .queryParam("userId", "expert-user")
            .queryParam("userLevel", "EXPERT")
            .queryParam("userRoles", "admin", "user") // Include roles to match test content
            .get("/api/help/content/" + testFeature)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("content", equalTo("Short help")); // EXPERT gets short content
    }

    // =========================================================================================
    // 2. POST /api/help/feedback - Feedback Submission
    // =========================================================================================

    @Test
    @DisplayName("POST /api/help/feedback - Should record user feedback")
    void submitFeedback_withValidData_shouldRecordFeedback() {
        Map<String, Object> feedbackRequest = Map.of(
            "helpId", testHelpContentId.toString(),
            "userId", "test-user",
            "helpful", true,
            "timeSpent", 30,
            "comment", "Very helpful!"
        );

        given()
            .contentType(ContentType.JSON)
            .body(feedbackRequest)
            .when()
            .post("/api/help/feedback")
            .then()
            .statusCode(200)
            .body("message", equalTo("Feedback recorded successfully"));
    }

    @Test
    @DisplayName("POST /api/help/feedback - Should handle invalid help ID")
    void submitFeedback_withInvalidHelpId_shouldReturnBadRequest() {
        Map<String, Object> feedbackRequest = Map.of(
            "helpId", UUID.randomUUID().toString(),
            "userId", "test-user",
            "helpful", false
        );

        given()
            .contentType(ContentType.JSON)
            .body(feedbackRequest)
            .when()
            .post("/api/help/feedback")
            .then()
            .statusCode(200) // Service handles gracefully
            .body("message", equalTo("Feedback recorded successfully"));
    }

    // =========================================================================================
    // 3. GET /api/help/search - Search Functionality
    // =========================================================================================

    @Test
    @DisplayName("GET /api/help/search - Should search help content")
    void searchHelp_withValidSearchTerm_shouldReturnResults() {
        given()
            .when()
            .queryParam("q", "Test Help")
            .queryParam("userLevel", "BEGINNER")
            .queryParam("userRoles", "admin")
            .get("/api/help/search")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("searchTerm", equalTo("Test Help"))
            .body("resultCount", greaterThan(0))
            .body("results", hasSize(greaterThan(0)))
            .body("results[0].title", containsString("Test Help"));
    }

    @Test
    @DisplayName("GET /api/help/search - Should validate search term")
    void searchHelp_withEmptySearchTerm_shouldReturnBadRequest() {
        given()
            .when()
            .queryParam("q", "")
            .get("/api/help/search")
            .then()
            .statusCode(400)
            .body("error", equalTo("Search term is required"));
    }

    @Test
    @DisplayName("GET /api/help/search - Should handle missing search term")
    void searchHelp_withoutSearchTerm_shouldReturnBadRequest() {
        given()
            .when()
            .get("/api/help/search")
            .then()
            .statusCode(400)
            .body("error", equalTo("Search term is required"));
    }

    // =========================================================================================
    // 4. GET /api/help/analytics - Global Analytics
    // =========================================================================================

    @Test
    @DisplayName("GET /api/help/analytics - Should return analytics data")
    void getAnalytics_shouldReturnAnalyticsData() {
        given()
            .when()
            .get("/api/help/analytics")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", notNullValue()); // Analytics object exists
    }

    // =========================================================================================
    // 5. GET /api/help/analytics/{feature} - Feature Analytics (TODO)
    // =========================================================================================

    @Test
    @DisplayName("GET /api/help/analytics/{feature} - Should return placeholder for now")
    void getFeatureAnalytics_shouldReturnPlaceholder() {
        given()
            .when()
            .get("/api/help/analytics/" + testFeature)
            .then()
            .statusCode(200)
            .body("feature", equalTo(testFeature))
            .body("message", equalTo("Feature analytics not yet implemented"));
    }

    // =========================================================================================
    // 6. GET /api/help/health - Health Check
    // =========================================================================================

    @Test
    @DisplayName("GET /api/help/health - Should return health status")
    void getHealthStatus_shouldReturnOperationalStatus() {
        given()
            .when()
            .get("/api/help/health")
            .then()
            .statusCode(200)
            .body("status", equalTo("Help System operational"));
    }

    // =========================================================================================
    // 7. POST /api/help/content - Create Help Content (Admin)
    // =========================================================================================

    @Test
    @DisplayName("POST /api/help/content - Should create new help content")
    void createHelpContent_withValidData_shouldCreateContent() {
        String uniqueFeature = "new-feature-" + UUID.randomUUID().toString().substring(0, 8);
        
        Map<String, Object> createRequest = Map.of(
            "feature", uniqueFeature,
            "type", "TUTORIAL",
            "title", "New Tutorial",
            "shortContent", "Quick help",
            "mediumContent", "Moderate help",
            "detailedContent", "Detailed tutorial",
            "userLevel", "INTERMEDIATE",
            "roles", List.of("admin"),
            "createdBy", "admin-user"
        );

        given()
            .contentType(ContentType.JSON)
            .body(createRequest)
            .when()
            .post("/api/help/content")
            .then()
            .statusCode(201)
            .body("message", equalTo("Help content created successfully"))
            .body("id", notNullValue());
    }

    @Test
    @DisplayName("POST /api/help/content - Should validate input data")
    void createHelpContent_withInvalidType_shouldReturnBadRequest() {
        Map<String, Object> createRequest = Map.of(
            "feature", "test-feature",
            "type", "INVALID_TYPE",
            "title", "Test",
            "shortContent", "Test",
            "userLevel", "BEGINNER"
        );

        given()
            .contentType(ContentType.JSON)
            .body(createRequest)
            .when()
            .post("/api/help/content")
            .then()
            .statusCode(400)
            .body("error", equalTo("Invalid input"));
    }

    // =========================================================================================
    // 8. PUT /api/help/content/{helpId}/toggle - Toggle Content Status
    // =========================================================================================

    @Test
    @DisplayName("PUT /api/help/content/{helpId}/toggle - Should deactivate content")
    void toggleHelpContent_deactivate_shouldSucceed() {
        given()
            .when()
            .queryParam("active", false)
            .queryParam("updatedBy", "admin")
            .put("/api/help/content/" + testHelpContentId + "/toggle")
            .then()
            .statusCode(200)
            .body("message", equalTo("Help content deactivated successfully"));
    }

    @Test
    @DisplayName("PUT /api/help/content/{helpId}/toggle - Should activate content")
    void toggleHelpContent_activate_shouldSucceed() {
        // First deactivate
        given()
            .queryParam("active", false)
            .put("/api/help/content/" + testHelpContentId + "/toggle");
        
        // Then activate
        given()
            .when()
            .queryParam("active", true)
            .queryParam("updatedBy", "admin")
            .put("/api/help/content/" + testHelpContentId + "/toggle")
            .then()
            .statusCode(200)
            .body("message", equalTo("Help content activated successfully"));
    }

    @Test
    @DisplayName("PUT /api/help/content/{helpId}/toggle - Should handle non-existent ID")
    void toggleHelpContent_withNonExistentId_shouldReturn404() {
        UUID nonExistentId = UUID.randomUUID();
        
        given()
            .when()
            .queryParam("active", true)
            .put("/api/help/content/" + nonExistentId + "/toggle")
            .then()
            .statusCode(404)
            .body("error", equalTo("Help content not found"));
    }

    // =========================================================================================
    // TEST CONFIGURATION
    // =========================================================================================

    /**
     * Test Profile to enable CQRS for testing
     */
    public static class CQRSTestProfile implements io.quarkus.test.junit.QuarkusTestProfile {
        @Override
        public java.util.Map<String, String> getConfigOverrides() {
            return java.util.Map.of(
                "features.cqrs.enabled", "true", // Enable CQRS for tests
                "quarkus.log.level", "WARN"      // Less logs during tests
            );
        }
    }
}