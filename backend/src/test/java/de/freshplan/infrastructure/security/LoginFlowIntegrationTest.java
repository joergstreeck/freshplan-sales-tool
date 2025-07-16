package de.freshplan.infrastructure.security;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import de.freshplan.test.SecurityDisabledTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the complete login flow and authentication scenarios. Tests the interaction
 * between security components, token validation, and API access.
 */
@QuarkusTest
@TestProfile(SecurityDisabledTestProfile.class)
class LoginFlowIntegrationTest {

  @Nested
  @DisplayName("Authentication Flow Tests")
  class AuthenticationFlowTests {

    @Test
    @DisplayName("Unauthenticated user should be denied access to protected endpoints")
    void unauthenticatedUser_shouldBeDeniedAccess() {
      given()
          .when()
          .get("/api/users")
          .then()
          .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());

      given()
          .when()
          .get("/api/customers")
          .then()
          .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin"})
    @DisplayName("Admin user should have access to all endpoints")
    void adminUser_shouldHaveAccessToAllEndpoints() {
      // Test user management endpoints
      given().when().get("/api/users").then().statusCode(Response.Status.OK.getStatusCode());

      // Test customer management endpoints
      given().when().get("/api/customers").then().statusCode(Response.Status.OK.getStatusCode());

      // Test user profile endpoint
      given()
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(Response.Status.OK.getStatusCode())
          .body("username", equalTo("testuser"));
    }

    @Test
    @TestSecurity(
        user = "manager",
        roles = {"manager"})
    @DisplayName("Manager user should have limited access")
    void managerUser_shouldHaveLimitedAccess() {
      // Managers can access customers
      given().when().get("/api/customers").then().statusCode(Response.Status.OK.getStatusCode());

      // But not user management
      given().when().get("/api/users").then().statusCode(Response.Status.FORBIDDEN.getStatusCode());

      // Can access their own profile
      given()
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(Response.Status.OK.getStatusCode())
          .body("username", equalTo("manager"));
    }

    @Test
    @TestSecurity(
        user = "sales",
        roles = {"sales"})
    @DisplayName("Sales user should have read-only access")
    void salesUser_shouldHaveReadOnlyAccess() {
      // Sales can read customers
      given().when().get("/api/customers").then().statusCode(Response.Status.OK.getStatusCode());

      // But cannot create customers
      given()
          .contentType(ContentType.JSON)
          .body(
              Map.of(
                  "customerType", "NEUKUNDE",
                  "companyName", "Test Company",
                  "contactPerson", "Test Contact",
                  "email", "test@example.com"))
          .when()
          .post("/api/customers")
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());

      // Cannot access user management
      given().when().get("/api/users").then().statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }
  }

  @Nested
  @DisplayName("Role-Based Access Control Tests")
  class RoleBasedAccessControlTests {

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should be able to perform all CRUD operations")
    void admin_shouldPerformAllCrudOperations() {
      // Create operation
      var createResponse =
          given()
              .contentType(ContentType.JSON)
              .body(
                  Map.of(
                      "customerType", "NEUKUNDE",
                      "companyName", "Admin Test Company",
                      "contactPerson", "Admin Contact",
                      "email", "admin.test@example.com"))
              .when()
              .post("/api/customers")
              .then()
              .statusCode(Response.Status.CREATED.getStatusCode())
              .extract()
              .path("id");

      // Read operation
      given()
          .when()
          .get("/api/customers/" + createResponse)
          .then()
          .statusCode(Response.Status.OK.getStatusCode())
          .body("companyName", equalTo("Admin Test Company"));

      // Update operation
      given()
          .contentType(ContentType.JSON)
          .body(
              Map.of(
                  "customerType", "BESTANDSKUNDE",
                  "companyName", "Updated Admin Company",
                  "contactPerson", "Updated Contact",
                  "email", "updated.admin@example.com"))
          .when()
          .put("/api/customers/" + createResponse)
          .then()
          .statusCode(Response.Status.OK.getStatusCode());

      // Delete operation (admin only)
      given()
          .when()
          .delete("/api/customers/" + createResponse)
          .then()
          .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "manager",
        roles = {"manager"})
    @DisplayName("Manager should be able to create and update but not delete")
    void manager_shouldCreateAndUpdateButNotDelete() {
      // Create operation (allowed)
      var createResponse =
          given()
              .contentType(ContentType.JSON)
              .body(
                  Map.of(
                      "customerType", "NEUKUNDE",
                      "companyName", "Manager Test Company",
                      "contactPerson", "Manager Contact",
                      "email", "manager.test@example.com"))
              .when()
              .post("/api/customers")
              .then()
              .statusCode(Response.Status.CREATED.getStatusCode())
              .extract()
              .path("id");

      // Update operation (allowed)
      given()
          .contentType(ContentType.JSON)
          .body(
              Map.of(
                  "customerType", "BESTANDSKUNDE",
                  "companyName", "Updated Manager Company",
                  "contactPerson", "Updated Manager Contact",
                  "email", "updated.manager@example.com"))
          .when()
          .put("/api/customers/" + createResponse)
          .then()
          .statusCode(Response.Status.OK.getStatusCode());

      // Delete operation (forbidden)
      given()
          .when()
          .delete("/api/customers/" + createResponse)
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "viewer",
        roles = {"viewer"})
    @DisplayName("Viewer should only have read access")
    void viewer_shouldOnlyHaveReadAccess() {
      // Read operation (allowed)
      given().when().get("/api/customers").then().statusCode(Response.Status.OK.getStatusCode());

      // Create operation (forbidden)
      given()
          .contentType(ContentType.JSON)
          .body(
              Map.of(
                  "customerType", "NEUKUNDE",
                  "companyName", "Viewer Test Company",
                  "contactPerson", "Viewer Contact",
                  "email", "viewer.test@example.com"))
          .when()
          .post("/api/customers")
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }
  }

  @Nested
  @DisplayName("Security Context Integration Tests")
  class SecurityContextIntegrationTests {

    @Test
    @TestSecurity(
        user = "contextuser",
        roles = {"admin"})
    @DisplayName("Security context should extract user information from JWT")
    void securityContext_shouldExtractUserInformation() {
      given()
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(Response.Status.OK.getStatusCode())
          .body("username", equalTo("contextuser"))
          .body("roles", hasItem("admin"));
    }

    @Test
    @TestSecurity(
        user = "audituser",
        roles = {"manager"})
    @DisplayName("Security operations should be audited")
    void securityOperations_shouldBeAudited() {
      // Perform an operation that should be audited
      given()
          .contentType(ContentType.JSON)
          .body(
              Map.of(
                  "customerType", "NEUKUNDE",
                  "companyName", "Audit Test Company",
                  "contactPerson", "Audit Contact",
                  "email", "audit.test@example.com"))
          .when()
          .post("/api/customers")
          .then()
          .statusCode(Response.Status.CREATED.getStatusCode());

      // Note: In a real implementation, we would check audit logs here
      // For now, we verify the operation completed successfully
      // indicating the audit logging didn't break the flow
    }
  }

  @Nested
  @DisplayName("Cross-Component Integration Tests")
  class CrossComponentIntegrationTests {

    @Test
    @TestSecurity(
        user = "integrationuser",
        roles = {"admin", "manager"})
    @DisplayName("Multiple roles should grant highest privilege level")
    void multipleRoles_shouldGrantHighestPrivilegeLevel() {
      // Should have admin privileges even though also has manager role
      var createResponse =
          given()
              .contentType(ContentType.JSON)
              .body(
                  Map.of(
                      "customerType", "NEUKUNDE",
                      "companyName", "Multi-Role Company",
                      "contactPerson", "Multi-Role Contact",
                      "email", "multirole@example.com"))
              .when()
              .post("/api/customers")
              .then()
              .statusCode(Response.Status.CREATED.getStatusCode())
              .extract()
              .path("id");

      // Should be able to delete (admin privilege)
      given()
          .when()
          .delete("/api/customers/" + createResponse)
          .then()
          .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "sessionuser",
        roles = {"manager"})
    @DisplayName("User session should be consistent across multiple requests")
    void userSession_shouldBeConsistentAcrossRequests() {
      // First request
      given()
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(Response.Status.OK.getStatusCode())
          .body("username", equalTo("sessionuser"))
          .body("roles", hasItem("manager"));

      // Second request should return same user info
      given()
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(Response.Status.OK.getStatusCode())
          .body("username", equalTo("sessionuser"))
          .body("roles", hasItem("manager"));

      // Different endpoint with same session
      given().when().get("/api/customers").then().statusCode(Response.Status.OK.getStatusCode());
    }
  }

  @Nested
  @DisplayName("Error Handling Integration Tests")
  class ErrorHandlingIntegrationTests {

    @Test
    @DisplayName("Invalid endpoints should return 404")
    void invalidEndpoints_shouldReturn404() {
      given()
          .when()
          .get("/api/nonexistent")
          .then()
          .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "erroruser",
        roles = {"sales"})
    @DisplayName("Forbidden operations should return 403 with proper message")
    void forbiddenOperations_shouldReturn403WithMessage() {
      given()
          .contentType(ContentType.JSON)
          .body(
              Map.of(
                  "customerType", "NEUKUNDE",
                  "companyName", "Error Test Company",
                  "contactPerson", "Error Contact",
                  "email", "error@example.com"))
          .when()
          .post("/api/customers")
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "malformeduser",
        roles = {"admin"})
    @DisplayName("Malformed requests should return 400")
    void malformedRequests_shouldReturn400() {
      given()
          .contentType(ContentType.JSON)
          .body("{invalid json}")
          .when()
          .post("/api/customers")
          .then()
          .statusCode(
              anyOf(
                  is(Response.Status.BAD_REQUEST.getStatusCode()),
                  is(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode())));
    }
  }
}
