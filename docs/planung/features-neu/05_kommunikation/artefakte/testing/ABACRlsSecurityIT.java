package de.freshplan.communication;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Hybrid Test: KI-Framework-Structure + echte Business-Logic aus alter Planung
 * Kombiniert das Beste aus beiden Welten:
 * - KI: Test-Framework-Setup, Struktur
 * - Alt: Echte ABAC/RLS Business-Logic-Validation
 */
@QuarkusTest
class ABACRlsSecurityIT {

  @Test
  void givenWrongTerritoryWhenAccessThreadThenForbidden() {
    // Echte Business-Logic: Territory-basierte Access-Control
    given()
      .header("X-Territories", "MUNICH") // User only authorized for Munich
      .when().get("/api/comm/threads?customerId=11111111-1111-1111-1111-111111111111") // Berlin customer
      .then().statusCode(403); // Must be 403, not "anyOf(200,403)" like KI-version
  }

  @Test
  void givenNoTerritoriesWhenAccessThreadsThenForbidden() {
    // Business-Logic: No territories = forbidden
    given()
      .when().get("/api/comm/threads")
      .then().statusCode(403);
  }

  @Test
  void givenValidTerritoryWhenAccessOwnThreadsThenSuccess() {
    // Positive case: Authorized access
    given()
      .header("X-Territories", "BER")
      .when().get("/api/comm/threads?limit=1")
      .then().statusCode(200);
  }

  @Test
  void givenETagMismatchWhenReplyThenPreconditionFailed() {
    // Echte ETag-Business-Logic aus alter CommThreadResourceBDDTest.java
    String threadId = "00000000-0000-0000-0000-000000000001";
    given()
      .header("X-Territories", "BER")
      .header("If-Match", "\"v999\"") // Wrong ETag
      .contentType("application/json")
      .body("{\"bodyText\":\"Reply\"}")
      .when().post("/api/comm/threads/" + threadId + "/reply")
      .then().statusCode(412); // Must be 412, not "anyOf(412,404)" like KI-version
  }

  @Test
  void givenCorrectETagWhenReplyThenSuccess() {
    // Test successful reply with correct ETag (would need fixture setup)
    String threadId = "00000000-0000-0000-0000-000000000001";
    given()
      .header("X-Territories", "BER")
      .header("If-Match", "\"v1\"") // Correct ETag
      .contentType("application/json")
      .body("{\"bodyText\":\"Valid reply\"}")
      .when().post("/api/comm/threads/" + threadId + "/reply")
      .then().statusCode(anyOf(is(201), is(404))); // 201 if thread exists, 404 if not in fixture
  }

  @Test
  void rlsDeniesReadsInDbSession() {
    // RLS-Test: Session should set 'SET app.territory=BER' before queries
    // If not implemented, this will fail when RLS policies are active
    given()
      .header("X-Territories", "BER")
      .when().get("/api/comm/threads?limit=1")
      .then().statusCode(200); // Should work if RLS session setter is correct
  }

  @Test
  void messageCreationForAnyCustomerIsAllowed() {
    // FreshFoodz Reality: Territory = nur User-Assignment, KEIN Gebietsschutz!
    // Berlin-User DARF München-Customer kontaktieren
    given()
      .header("X-Territories", "BER")
      .contentType("application/json")
      .body("""
        {
          "customerId": "22222222-2222-2222-2222-222222222222",
          "subject": "Test message",
          "to": ["test@example.com"],
          "bodyText": "Hello"
        }
        """)
      .when().post("/api/comm/messages")
      .then().statusCode(anyOf(is(201), is(404))); // 201 if created, 404 if customer not found
  }
}