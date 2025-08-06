package de.freshplan.api.resources;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class DataQualityResourceTest {

    @Test
    @TestSecurity(user = "testuser", roles = {"admin", "manager", "sales"})
    public void testGetDataQualityMetrics() {
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
    @TestSecurity(user = "testuser", roles = {"admin", "manager", "sales"})
    public void testGetDataFreshnessStatistics() {
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

    @Test
    public void testGetDataQualityMetrics_Unauthorized() {
        given()
            .when()
            .get("/api/contact-interactions/data-quality/metrics")
            .then()
            .statusCode(401);
    }

    @Test
    public void testGetDataFreshnessStatistics_Unauthorized() {
        given()
            .when()
            .get("/api/contact-interactions/data-freshness/statistics")
            .then()
            .statusCode(401);
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"viewer"})
    public void testGetDataQualityMetrics_Forbidden() {
        given()
            .when()
            .get("/api/contact-interactions/data-quality/metrics")
            .then()
            .statusCode(403);
    }

    @Test
    @TestSecurity(user = "testuser", roles = {"viewer"})
    public void testGetDataFreshnessStatistics_Forbidden() {
        given()
            .when()
            .get("/api/contact-interactions/data-freshness/statistics")
            .then()
            .statusCode(403);
    }
}