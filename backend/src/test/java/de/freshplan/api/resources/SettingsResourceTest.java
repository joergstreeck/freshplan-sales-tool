package de.freshplan.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import de.freshplan.domain.opportunity.entity.OpportunityMultiplier;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration Tests for SettingsResource REST API (Sprint 2.1.7.3)
 *
 * <p>Tests GET /api/settings/opportunity-multipliers endpoint:
 *
 * <ul>
 *   <li>Returns 36 multipliers (9 BusinessTypes × 4 OpportunityTypes)
 *   <li>Requires authentication (USER/MANAGER/ADMIN roles)
 *   <li>Returns JSON array with multiplier objects
 * </ul>
 *
 * <p>Data Source: Migration V10031 seeds 36 multipliers
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.3
 */
@QuarkusTest
@TestHTTPEndpoint(SettingsResource.class)
@DisplayName("SettingsResource REST API Integration Tests")
@Tag("e2e")
public class SettingsResourceTest {

  @BeforeEach
  @Transactional
  void setUp() {
    // Migration V10031 seeds 36 multipliers - no cleanup needed
    // Verify seed data exists
    long count = OpportunityMultiplier.count();
    if (count < 36) {
      throw new IllegalStateException(
          "Migration V10031 not applied! Expected 36 multipliers, found: " + count);
    }
  }

  // ==========================================================================
  // GET /api/settings/opportunity-multipliers Tests
  // ==========================================================================

  @Test
  @TestSecurity(user = "testuser", roles = "USER")
  @DisplayName("GET /opportunity-multipliers - Should return 36 multipliers (USER role)")
  void getOpportunityMultipliers_userRole_shouldReturn36Multipliers() {
    given()
        .accept(ContentType.JSON)
        .when()
        .get("/opportunity-multipliers")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("$", hasSize(36)) // Array of 36 multipliers
        .body("[0].id", notNullValue())
        .body("[0].businessType", notNullValue())
        .body("[0].opportunityType", notNullValue())
        .body("[0].multiplier", notNullValue());
  }

  @Test
  @TestSecurity(user = "manager", roles = "MANAGER")
  @DisplayName("GET /opportunity-multipliers - Should work for MANAGER role")
  void getOpportunityMultipliers_managerRole_shouldReturn200() {
    given()
        .accept(ContentType.JSON)
        .when()
        .get("/opportunity-multipliers")
        .then()
        .statusCode(200)
        .body("$", hasSize(36));
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADMIN")
  @DisplayName("GET /opportunity-multipliers - Should work for ADMIN role")
  void getOpportunityMultipliers_adminRole_shouldReturn200() {
    given()
        .accept(ContentType.JSON)
        .when()
        .get("/opportunity-multipliers")
        .then()
        .statusCode(200)
        .body("$", hasSize(36));
  }

  @Test
  @DisplayName("GET /opportunity-multipliers - Should require authentication")
  void getOpportunityMultipliers_noAuth_shouldReturn401() {
    given()
        .accept(ContentType.JSON)
        .when()
        .get("/opportunity-multipliers")
        .then()
        .statusCode(401);
  }

  // ==========================================================================
  // Response Structure Tests
  // ==========================================================================

  @Test
  @TestSecurity(user = "testuser", roles = "USER")
  @DisplayName(
      "GET /opportunity-multipliers - Should contain all 9 BusinessTypes")
  void getOpportunityMultipliers_shouldContainAll9BusinessTypes() {
    given()
        .accept(ContentType.JSON)
        .when()
        .get("/opportunity-multipliers")
        .then()
        .statusCode(200)
        .body("businessType", hasItems(
            "RESTAURANT",
            "HOTEL",
            "CATERING",
            "KANTINE",
            "BILDUNG",
            "GESUNDHEIT",
            "GROSSHANDEL",
            "LEH",
            "SONSTIGES"
        ));
  }

  @Test
  @TestSecurity(user = "testuser", roles = "USER")
  @DisplayName(
      "GET /opportunity-multipliers - Should contain all 4 OpportunityTypes")
  void getOpportunityMultipliers_shouldContainAll4OpportunityTypes() {
    given()
        .accept(ContentType.JSON)
        .when()
        .get("/opportunity-multipliers")
        .then()
        .statusCode(200)
        .body("opportunityType", hasItems(
            "NEUGESCHAEFT",
            "SORTIMENTSERWEITERUNG",
            "NEUER_STANDORT",
            "VERLAENGERUNG"
        ));
  }

  @Test
  @TestSecurity(user = "testuser", roles = "USER")
  @DisplayName(
      "GET /opportunity-multipliers - RESTAURANT SORTIMENTSERWEITERUNG should be 0.25")
  void getOpportunityMultipliers_restaurantSortimentserweiterung_shouldBe025() {
    given()
        .accept(ContentType.JSON)
        .when()
        .get("/opportunity-multipliers")
        .then()
        .statusCode(200)
        .body(
            "find { it.businessType == 'RESTAURANT' && it.opportunityType == 'SORTIMENTSERWEITERUNG' }.multiplier",
            equalTo(0.25f));
  }

  @Test
  @TestSecurity(user = "testuser", roles = "USER")
  @DisplayName(
      "GET /opportunity-multipliers - HOTEL SORTIMENTSERWEITERUNG should be 0.65")
  void getOpportunityMultipliers_hotelSortimentserweiterung_shouldBe065() {
    given()
        .accept(ContentType.JSON)
        .when()
        .get("/opportunity-multipliers")
        .then()
        .statusCode(200)
        .body(
            "find { it.businessType == 'HOTEL' && it.opportunityType == 'SORTIMENTSERWEITERUNG' }.multiplier",
            equalTo(0.65f));
  }

  @Test
  @TestSecurity(user = "testuser", roles = "USER")
  @DisplayName(
      "GET /opportunity-multipliers - All NEUGESCHAEFT should be 1.00")
  void getOpportunityMultipliers_allNeugeschaeft_shouldBe100() {
    given()
        .accept(ContentType.JSON)
        .when()
        .get("/opportunity-multipliers")
        .then()
        .statusCode(200)
        .body(
            "findAll { it.opportunityType == 'NEUGESCHAEFT' }.multiplier",
            everyItem(equalTo(1.00f)));
  }

  // ==========================================================================
  // Performance Tests
  // ==========================================================================

  @Test
  @TestSecurity(user = "testuser", roles = "USER")
  @DisplayName("GET /opportunity-multipliers - Should respond in under 500ms")
  void getOpportunityMultipliers_shouldRespondQuickly() {
    long start = System.currentTimeMillis();

    given()
        .accept(ContentType.JSON)
        .when()
        .get("/opportunity-multipliers")
        .then()
        .statusCode(200);

    long duration = System.currentTimeMillis() - start;

    if (duration > 500) {
      throw new AssertionError(
          "API took " + duration + "ms, expected < 500ms (Performance Degradation!)");
    }
  }

  // ==========================================================================
  // Caching Recommendations Tests
  // ==========================================================================

  @Test
  @TestSecurity(user = "testuser", roles = "USER")
  @DisplayName(
      "GET /opportunity-multipliers - Response should be cacheable (for React Query)")
  void getOpportunityMultipliers_shouldBeCacheable() {
    // Frontend should cache this response (React Query 5min stale time)
    // Verify response is consistent on multiple calls
    String response1 =
        given()
            .accept(ContentType.JSON)
            .when()
            .get("/opportunity-multipliers")
            .then()
            .statusCode(200)
            .extract()
            .asString();

    String response2 =
        given()
            .accept(ContentType.JSON)
            .when()
            .get("/opportunity-multipliers")
            .then()
            .statusCode(200)
            .extract()
            .asString();

    if (!response1.equals(response2)) {
      throw new AssertionError("Multipliers response is not consistent (caching issue!)");
    }
  }
}
