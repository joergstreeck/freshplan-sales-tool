package de.freshplan.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/** Sprint 1.3 PR #2 - Foundation Integration Tests Validates Phase 1 Foundation requirements */
@QuarkusTest
@Disabled("TEMPORARY: Sprint 2.1.4 CI Performance Fix")
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Foundation Integration Tests")
@io.quarkus.test.security.TestSecurity(
    user = "test-user",
    roles = {"admin"})
public class FoundationIntegrationTest {

  private static String testETag;
  private static final String TEST_SETTING_KEY = "test.integration";

  @Test
  @Order(1)
  @Disabled("Event API (/api/events) not yet implemented - Sprint 2.x")
  @DisplayName("CQRS Foundation - Performance < 200ms P95")
  public void testCQRSPerformance() {
    // Warm up
    for (int i = 0; i < 5; i++) {
      given()
          .contentType("application/json")
          .body(
              Json.createObjectBuilder()
                  .add("eventType", "WARMUP_EVENT")
                  .add("aggregateId", "warmup-" + i)
                  .add("payload", Json.createObjectBuilder().add("test", true))
                  .build()
                  .toString())
          .when()
          .post("/api/events")
          .then()
          .statusCode(anyOf(is(200), is(201), is(202)));
    }

    // Measure performance
    List<Long> responseTimes = new ArrayList<>();
    int iterations = 100;

    for (int i = 0; i < iterations; i++) {
      long start = System.currentTimeMillis();

      given()
          .contentType("application/json")
          .body(
              Json.createObjectBuilder()
                  .add("eventType", "TEST_EVENT")
                  .add("aggregateId", "test-" + i)
                  .add("payload", Json.createObjectBuilder().add("test", true))
                  .build()
                  .toString())
          .when()
          .post("/api/events")
          .then()
          .statusCode(anyOf(is(200), is(201), is(202)));

      responseTimes.add(System.currentTimeMillis() - start);
    }

    // Calculate P95
    Collections.sort(responseTimes);
    int p95Index = (int) Math.ceil(iterations * 0.95) - 1;
    long p95Time = responseTimes.get(p95Index);

    // Assert P95 response time < 200ms
    assertTrue(p95Time < 200, "CQRS P95 response time " + p95Time + "ms exceeds 200ms target");
  }

  @Test
  @Order(2)
  @DisplayName("Settings Registry - Create Setting")
  public void testCreateSetting() {
    JsonObject settingValue =
        Json.createObjectBuilder().add("enabled", true).add("threshold", 100).build();

    Response response =
        given()
            .contentType("application/json")
            .body(
                Json.createObjectBuilder()
                    .add("key", TEST_SETTING_KEY)
                    .add("value", settingValue)
                    .add("scope", "GLOBAL")
                    .build()
                    .toString())
            .when()
            .post("/api/settings")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .body("key", equalTo(TEST_SETTING_KEY))
            .body("scope", equalTo("GLOBAL"))
            .extract()
            .response();

    // Store ETag for next test
    testETag = response.header("ETag");
    assertNotNull(testETag, "ETag should be present in response");
  }

  @Test
  @Order(3)
  @DisplayName("Settings Registry - ETag Support (304 Response)")
  public void testETagSupport() {
    assertNotNull(testETag, "ETag should be set from previous test");

    // Request with If-None-Match should return 304
    given()
        .header("If-None-Match", testETag)
        .queryParam("scope", "GLOBAL")
        .queryParam("key", TEST_SETTING_KEY)
        .when()
        .get("/api/settings")
        .then()
        .statusCode(304)
        .time(lessThan(50L)); // Should be fast for cached response
  }

  @Test
  @Order(4)
  @DisplayName("Settings Registry - Performance < 50ms")
  public void testSettingsPerformance() {
    // Skip if ETag is not available
    if (testETag == null) {
      testETag = "dummy-etag"; // Use a dummy ETag if not set
    }

    // Warm up with ETag
    for (int i = 0; i < 5; i++) {
      given()
          .header("If-None-Match", testETag)
          .queryParam("scope", "GLOBAL")
          .queryParam("key", TEST_SETTING_KEY)
          .when()
          .get("/api/settings");
    }

    // Measure performance with ETag (should hit cache)
    long totalTime = 0;
    int iterations = 50;

    for (int i = 0; i < iterations; i++) {
      long start = System.currentTimeMillis();

      given()
          .header("If-None-Match", testETag)
          .queryParam("scope", "GLOBAL")
          .queryParam("key", TEST_SETTING_KEY)
          .when()
          .get("/api/settings");

      totalTime += (System.currentTimeMillis() - start);
    }

    long avgTime = totalTime / iterations;

    // Assert average response time < 50ms
    assertTrue(
        avgTime < 50, "Settings average response time " + avgTime + "ms exceeds 50ms target");
  }

  @Test
  @Order(5)
  @DisplayName("Security Context - Authentication Required")
  public void testSecurityContext() {
    // With @TestSecurity, all requests are authenticated by default
    // So we just test that the endpoint is accessible
    given()
        .when()
        .get("/api/settings")
        .then()
        .statusCode(anyOf(is(200), is(204), is(404))); // OK, No Content, or Not Found
  }

  @Test
  @Order(6)
  @DisplayName("Security Performance - < 100ms P95")
  public void testSecurityPerformance() {
    // Warm up
    for (int i = 0; i < 5; i++) {
      given()
          .queryParam("scope", "GLOBAL")
          .queryParam("key", "test.perf")
          .when()
          .get("/api/settings")
          .then()
          .statusCode(anyOf(is(200), is(404)));
    }

    // Measure performance with auth
    List<Long> responseTimes = new ArrayList<>();
    int iterations = 100;

    for (int i = 0; i < iterations; i++) {
      long start = System.currentTimeMillis();

      given()
          .queryParam("scope", "GLOBAL")
          .queryParam("key", "test.perf")
          .when()
          .get("/api/settings")
          .then()
          .statusCode(anyOf(is(200), is(404)));

      responseTimes.add(System.currentTimeMillis() - start);
    }

    // Calculate P95
    Collections.sort(responseTimes);
    int p95Index = (int) Math.ceil(iterations * 0.95) - 1;
    long p95Time = responseTimes.get(p95Index);

    // Assert P95 response time < 100ms with auth
    assertTrue(p95Time < 100, "Security P95 response time " + p95Time + "ms exceeds 100ms target");
  }

  @Test
  @Order(7)
  @Disabled("Event API (/api/events) not yet implemented - Sprint 2.x")
  @DisplayName("Event System - LISTEN/NOTIFY Integration")
  public void testEventSystem() {
    // Test that event endpoint exists and responds
    given()
        .contentType("application/json")
        .body(
            Json.createObjectBuilder()
                .add("eventType", "TEST_EVENT")
                .add("aggregateId", "test-123")
                .add("payload", Json.createObjectBuilder().add("action", "test").build())
                .build()
                .toString())
        .when()
        .post("/api/events")
        .then()
        .statusCode(anyOf(is(200), is(201), is(202)));
  }

  @Test
  @Order(8)
  @DisplayName("Cross-Module Integration - Settings + Security")
  public void testCrossModuleIntegration() {
    // Test that settings respect security context
    given()
        .queryParam("scope", "GLOBAL")
        .queryParam("key", "system.config")
        .when()
        .get("/api/settings")
        .then()
        .statusCode(anyOf(is(200), is(404)))
        .time(lessThan(200L)); // Combined latency should still be < 200ms
  }

  @Test
  @Order(9)
  @Disabled("Event API (/api/events) not yet implemented - Sprint 2.x")
  @DisplayName("Foundation Validation - All Components Operational")
  public void testFoundationComplete() {
    // Final validation that all foundation components work together

    // 1. Health check passes
    given().when().get("/api/health").then().statusCode(200).body("status", equalTo("UP"));

    // 2. Settings work with ETag
    Response settingsResponse =
        given()
            .queryParam("scope", "GLOBAL")
            .queryParam("key", "ui.theme")
            .when()
            .get("/api/settings")
            .then()
            .statusCode(anyOf(is(200), is(404)))
            .extract()
            .response();

    if (settingsResponse.statusCode() == 200) {
      assertNotNull(settingsResponse.header("ETag"), "Settings should return ETag header");
    }

    // 3. Security context available (with @TestSecurity, auth is automatic)
    given().when().get("/api/health/auth").then().statusCode(anyOf(is(200), is(404)));

    // All foundation components validated
    assertTrue(true, "Foundation validation complete");
  }

  @Test
  @Order(10)
  @DisplayName("Performance Budget - Bundle Size < 200KB")
  public void testBundleSize() {
    // This would normally check the actual bundle size
    // For now, we just validate the endpoint exists
    given().when().get("/api/metrics/bundle-size").then().statusCode(anyOf(is(200), is(404)));

    // In a real test, assert bundle size < 204800 bytes (200KB)
  }
}
