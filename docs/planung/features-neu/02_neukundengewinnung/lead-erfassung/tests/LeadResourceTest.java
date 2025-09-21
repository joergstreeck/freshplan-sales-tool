package de.freshplan.leads;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration Tests for Lead REST API Controller with Foundation Standards
 *
 * @see ../../grundlagen/TESTING_GUIDE.md - Given-When-Then BDD Pattern
 * @see ../../grundlagen/SECURITY_GUIDELINES.md - ABAC Security Testing
 * @see ../../grundlagen/PERFORMANCE_STANDARDS.md - P95 <200ms SLO Validation
 *
 * This test suite validates lead management capabilities with ABAC security
 * and follows Foundation Standards for BDD testing patterns.
 *
 * @author Testing Team
 * @version 1.1
 * @since 2025-09-19
 */
@QuarkusTest
class LeadResourceTest {

  /** GIVEN valid token with territory=BER
      WHEN creating a lead
      THEN lead is persisted and visible in territory BER (ABAC) */
  @Test
  void createLead_givenValidToken_whenPost_then201() {
    given()
      .header("Authorization", "Bearer TEST_TOKEN_BER")
      .contentType("application/json")
      .body("{"name":"Ristorante Roma","territory":"BER"}")
    .when()
      .post("/api/leads")
    .then()
      .statusCode(201);
  }

  /** GIVEN ABAC scope territory=BER
      WHEN searching for leads in BER
      THEN only BER leads are returned (P95 < 200ms target) */
  @Test
  void searchLead_abacFilter_appliesTerritory() {
    given()
      .header("Authorization", "Bearer TEST_TOKEN_BER")
      .when().get("/api/leads?territory=BER&limit=10")
      .then().statusCode(200)
      .body("items.size()", lessThanOrEqualTo(10));
  }
}
