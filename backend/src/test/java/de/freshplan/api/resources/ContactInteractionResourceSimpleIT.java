package de.freshplan.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("migrate")
@DisplayName("Contact Interaction Resource Simple Integration Tests")
class ContactInteractionResourceSimpleIT {

  @Test
  @DisplayName("Should get data quality metrics endpoint")
  void shouldGetDataQualityMetrics() {
    given()
        .when()
        .get("/api/contact-interactions/metrics/data-quality")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("totalContacts", notNullValue())
        .body("contactsWithInteractions", notNullValue())
        .body("averageInteractionsPerContact", notNullValue())
        .body("dataCompletenessScore", notNullValue())
        .body("contactsWithWarmthScore", notNullValue())
        .body("freshContacts", notNullValue())
        .body("agingContacts", notNullValue())
        .body("staleContacts", notNullValue())
        .body("criticalContacts", notNullValue())
        .body("showDataCollectionHints", notNullValue())
        .body("criticalDataGaps", notNullValue())
        .body("improvementSuggestions", notNullValue())
        .body(
            "overallDataQuality", oneOf("CRITICAL", "POOR", "FAIR", "GOOD", "EXCELLENT", "UNKNOWN"))
        .body("interactionCoverage", notNullValue());
  }

  @Test
  @DisplayName("Should handle invalid contact ID gracefully")
  void shouldHandleInvalidContactId() {
    String invalidContactId = "invalid-uuid";

    given()
        .when()
        .get("/api/contact-interactions/contact/{contactId}", invalidContactId)
        .then()
        .statusCode(anyOf(equalTo(400), equalTo(404)));
  }

  @Test
  @DisplayName("Should validate API endpoints exist")
  void shouldValidateApiEndpointsExist() {
    // Test warmth endpoint exists
    given()
        .when()
        .get("/api/contact-interactions/warmth/00000000-0000-0000-0000-000000000000")
        .then()
        .statusCode(anyOf(equalTo(404), equalTo(400), equalTo(500))); // Not 405 Method Not Allowed

    // Test note endpoint exists
    given()
        .contentType(ContentType.JSON)
        .body("{\"note\": \"test\"}")
        .when()
        .post("/api/contact-interactions/note/00000000-0000-0000-0000-000000000000")
        .then()
        .statusCode(anyOf(equalTo(404), equalTo(400), equalTo(500))); // Not 405 Method Not Allowed
  }
}
