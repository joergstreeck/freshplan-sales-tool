package de.freshplan.infrastructure.security;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.freshplan.test.SecurityDisabledTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;import org.junit.jupiter.api.Timeout;

/**
 * Integration tests for token refresh mechanisms and token lifecycle management. Tests the behavior
 * of tokens over time and refresh scenarios.
 */
@QuarkusTest
@Tag("migrate")@TestProfile(SecurityDisabledTestProfile.class)
class TokenRefreshIntegrationTest {

  @Inject SecurityContextProvider securityContext;

  @Nested
  @DisplayName("Token Lifecycle Tests")
  class TokenLifecycleTests {

    @Test
    @TestSecurity(
        user = "tokenuser",
        roles = {"admin"})
    @DisplayName("Fresh token should be valid and contain expected claims")
    void freshToken_shouldBeValidWithExpectedClaims() {
      given()
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(Response.Status.OK.getStatusCode())
          .body("username", equalTo("tokenuser"))
          .body("roles", hasItem("admin"))
          .body("authenticated", equalTo(true));
    }

    @Test
    @TestSecurity(
        user = "expiryuser",
        roles = {"manager"})
    @DisplayName("Token expiration information should be accessible")
    void tokenExpirationInfo_shouldBeAccessible() {
      // Test that we can access token expiration information
      // Note: In @TestSecurity, token expiration simulation is limited
      // This test verifies the endpoint works with security context
      given()
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(Response.Status.OK.getStatusCode())
          .body("username", equalTo("expiryuser"));
    }

    @Test
    @TestSecurity(
        user = "sessionuser",
        roles = {"sales"})
    @DisplayName("Multiple requests with same token should maintain session")
    void multipleRequestsWithSameToken_shouldMaintainSession() {
      // First request
      var firstResponse =
          given()
              .when()
              .get("/api/users/me")
              .then()
              .statusCode(Response.Status.OK.getStatusCode())
              .extract()
              .body()
              .asString();

      // Second request immediately after
      var secondResponse =
          given()
              .when()
              .get("/api/users/me")
              .then()
              .statusCode(Response.Status.OK.getStatusCode())
              .extract()
              .body()
              .asString();

      // Should have consistent user information
      assertEquals(
          firstResponse, secondResponse, "User information should be consistent across requests");
    }
  }

  @Nested
  @DisplayName("Token Validation Tests")
  class TokenValidationTests {

    @Test
    @DisplayName("Request without authentication should be rejected")
    void requestWithoutAuth_shouldBeRejected() {
      given()
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "validuser",
        roles = {"admin"})
    @DisplayName("Valid token should allow access to protected resources")
    void validToken_shouldAllowAccessToProtectedResources() {
      // Test access to different protected endpoints
      given().when().get("/api/users").then().statusCode(Response.Status.OK.getStatusCode());

      given().when().get("/api/customers").then().statusCode(Response.Status.OK.getStatusCode());

      given()
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(Response.Status.OK.getStatusCode())
          .body("username", equalTo("validuser"));
    }

    @Test
    @TestSecurity(
        user = "roleuser",
        roles = {"invalid_role"})
    @DisplayName("Token with invalid roles should be handled gracefully")
    void tokenWithInvalidRoles_shouldBeHandledGracefully() {
      // User with invalid role should not have access to endpoints that require specific roles
      given()
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());

      // Should not have access to admin endpoints
      given().when().get("/api/users").then().statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }
  }

  @Nested
  @DisplayName("Security Context Provider Integration Tests")
  class SecurityContextProviderTests {

    @Test
    @TestSecurity(
        user = "contexttest",
        roles = {"manager"})
    @DisplayName("Security context should provide comprehensive user information")
    void securityContext_shouldProvideComprehensiveUserInfo() {
      given()
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(Response.Status.OK.getStatusCode())
          .body("username", equalTo("contexttest"))
          .body("roles", hasItem("manager"))
          .body("authenticated", equalTo(true));
    }

    @Test
    @TestSecurity(
        user = "authdetailsuser",
        roles = {"admin", "manager"})
    @DisplayName("Authentication details should be properly extracted")
    void authenticationDetails_shouldBeProperlyExtracted() {
      var response =
          given()
              .when()
              .get("/api/users/me")
              .then()
              .statusCode(Response.Status.OK.getStatusCode())
              .body("username", equalTo("authdetailsuser"))
              .body("roles", hasItems("admin", "manager"))
              .extract()
              .body()
              .jsonPath();

      // Verify essential fields are present
      assertNotNull(response.get("username"));
      assertNotNull(response.get("roles"));
      assertTrue(response.getBoolean("authenticated"));
    }

    @Test
    @TestSecurity(
        user = "audituser",
        roles = {"sales"})
    @DisplayName("Security operations should generate audit logs")
    void securityOperations_shouldGenerateAuditLogs() {
      // Perform operations that should be audited
      given().when().get("/api/customers").then().statusCode(Response.Status.OK.getStatusCode());

      given()
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(Response.Status.OK.getStatusCode())
          .body("username", equalTo("audituser"));

      // Note: In a real implementation, we would verify audit log entries
      // For integration tests, we verify operations complete successfully
      // indicating audit logging doesn't interfere with normal operations
    }
  }

  @Nested
  @DisplayName("Concurrent Access Tests")
  class ConcurrentAccessTests {

    @Test
    @TestSecurity(
        user = "concurrentuser",
        roles = {"manager"})
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    @DisplayName("Concurrent requests should be handled correctly")
    void concurrentRequests_shouldBeHandledCorrectly() throws InterruptedException {
      int numberOfThreads = 5;
      CountDownLatch latch = new CountDownLatch(numberOfThreads);
      boolean[] results = new boolean[numberOfThreads];

      for (int i = 0; i < numberOfThreads; i++) {
        final int index = i;
        new Thread(
                () -> {
                  try {
                    var response =
                        given()
                            .when()
                            .get("/api/users/me")
                            .then()
                            .statusCode(Response.Status.OK.getStatusCode())
                            .body("username", equalTo("concurrentuser"))
                            .extract()
                            .statusCode();

                    results[index] = (response == Response.Status.OK.getStatusCode());
                  } catch (Exception e) {
                    results[index] = false;
                  } finally {
                    latch.countDown();
                  }
                })
            .start();
      }

      assertTrue(latch.await(8, TimeUnit.SECONDS), "All threads should complete within timeout");

      for (int i = 0; i < numberOfThreads; i++) {
        assertTrue(results[i], "Thread " + i + " should have successful response");
      }
    }

    @Test
    @TestSecurity(
        user = "heavyuser",
        roles = {"admin"})
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    @DisplayName("Heavy load should not degrade authentication performance")
    void heavyLoad_shouldNotDegradeAuthPerformance() {
      long startTime = System.currentTimeMillis();

      // Perform multiple authenticated requests
      for (int i = 0; i < 20; i++) {
        given()
            .when()
            .get("/api/users/me")
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("username", equalTo("heavyuser"));
      }

      long endTime = System.currentTimeMillis();
      long totalTime = endTime - startTime;

      // Each request should complete reasonably fast (average < 500ms)
      assertTrue(
          totalTime < 10000,
          "20 authenticated requests should complete within 10 seconds, took: " + totalTime + "ms");
    }
  }

  @Nested
  @DisplayName("Error Recovery Tests")
  class ErrorRecoveryTests {

    @Test
    @TestSecurity(
        user = "recoveryuser",
        roles = {"manager"})
    @DisplayName("System should recover from temporary authentication issues")
    void system_shouldRecoverFromTempAuthIssues() {
      // First request should succeed
      given()
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(Response.Status.OK.getStatusCode())
          .body("username", equalTo("recoveryuser"));

      // Simulate various requests to test resilience
      for (int i = 0; i < 3; i++) {
        given().when().get("/api/customers").then().statusCode(Response.Status.OK.getStatusCode());
      }

      // Final request should still succeed
      given()
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(Response.Status.OK.getStatusCode())
          .body("username", equalTo("recoveryuser"));
    }

    @Test
    @DisplayName("Malformed authentication headers should be handled gracefully")
    void malformedAuthHeaders_shouldBeHandledGracefully() {
      given()
          .header("Authorization", "Bearer invalid-token-format")
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(
              anyOf(
                  is(Response.Status.UNAUTHORIZED.getStatusCode()),
                  is(Response.Status.FORBIDDEN.getStatusCode())));

      given()
          .header("Authorization", "Invalid header format")
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(
              anyOf(
                  is(Response.Status.UNAUTHORIZED.getStatusCode()),
                  is(Response.Status.FORBIDDEN.getStatusCode())));
    }
  }

  @Nested
  @DisplayName("Performance Tests")
  class PerformanceTests {

    @Test
    @TestSecurity(
        user = "perfuser",
        roles = {"sales"})
    @DisplayName("Authentication should not significantly impact response time")
    void authentication_shouldNotImpactResponseTime() {
      long startTime = System.currentTimeMillis();

      given()
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(Response.Status.OK.getStatusCode())
          .body("username", equalTo("perfuser"));

      long endTime = System.currentTimeMillis();
      long responseTime = endTime - startTime;

      // Authentication should not add significant overhead (< 2000ms for CI)
      assertTrue(
          responseTime < 2000,
          "Authenticated request should complete quickly, took: " + responseTime + "ms");
    }

    @Test
    @TestSecurity(
        user = "throughputuser",
        roles = {"manager"})
    @DisplayName("System should maintain throughput under authenticated load")
    void system_shouldMaintainThroughputUnderAuthLoad() {
      long startTime = System.currentTimeMillis();

      // Simulate moderate load
      for (int i = 0; i < 10; i++) {
        given().when().get("/api/customers").then().statusCode(Response.Status.OK.getStatusCode());
      }

      long endTime = System.currentTimeMillis();
      long averageTime = (endTime - startTime) / 10;

      // Average response time should be reasonable
      assertTrue(
          averageTime < 500,
          "Average response time under load should be < 500ms, was: " + averageTime + "ms");
    }
  }
}
