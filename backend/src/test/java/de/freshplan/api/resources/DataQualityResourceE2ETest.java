package de.freshplan.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * E2E tests for DataQualityResource - ONLY Happy-Path HTTP verification.
 *
 * <p>Tests only the HTTP layer (status codes, content types, response format). Business logic is
 * tested in DataQualityService. Auth/Security tests are omitted (guaranteed by Quarkus Security).
 */
@QuarkusTest
@Tag("e2e")
public class DataQualityResourceE2ETest {

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  public void testGetDataQualityMetrics_shouldReturn200WithCorrectFormat() {
    given()
        .when()
        .get("/api/contact-interactions/data-quality/metrics")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("totalContacts", notNullValue())
        .body("contactsWithInteractions", notNullValue())
        .body("contactsWithoutInteractions", notNullValue())
        .body("totalInteractions", notNullValue())
        .body("freshContacts", notNullValue())
        .body("agingContacts", notNullValue())
        .body("staleContacts", notNullValue())
        .body("criticalContacts", notNullValue())
        .body("dataCompletenessScore", notNullValue())
        .body("interactionCoverage", notNullValue())
        .body("averageInteractionsPerContact", notNullValue())
        .body("contactsWithWarmthScore", notNullValue())
        .body("overallDataQuality", equalTo("GOOD"))
        .body("showDataCollectionHints", equalTo(true))
        .body("criticalDataGaps", hasSize(3))
        .body("improvementSuggestions", hasSize(3))
        .body("lastUpdated", notNullValue());
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  public void testGetDataFreshnessStatistics_shouldReturn200WithCorrectFormat() {
    given()
        .when()
        .get("/api/contact-interactions/data-freshness/statistics")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("FRESH", equalTo(45))
        .body("AGING", equalTo(35))
        .body("STALE", equalTo(40))
        .body("CRITICAL", equalTo(30))
        .body("total", equalTo(150));
  }
}
