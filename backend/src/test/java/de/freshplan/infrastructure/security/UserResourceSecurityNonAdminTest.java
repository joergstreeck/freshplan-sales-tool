package de.freshplan.infrastructure.security;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import de.freshplan.domain.user.service.dto.CreateUserRequest;
import de.freshplan.domain.user.service.dto.UpdateUserRequest;
import de.freshplan.domain.user.service.dto.UpdateUserRolesRequest;
import de.freshplan.test.SecurityDisabledTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Security tests for UserResource that require pre-created test users. These tests verify that
 * non-admin users cannot perform admin operations.
 *
 * <p>Note: These tests use mock UUIDs because the test security context prevents creating real
 * users during test setup.
 */
@QuarkusTest
@TestProfile(SecurityDisabledTestProfile.class)
class UserResourceSecurityNonAdminTest {

  private static final String USERS_BASE_PATH = "/api/users";
  // Use a static UUID that doesn't need to exist for 403/401 tests
  private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Test
  @TestSecurity(
      user = "manager",
      roles = {"manager"})
  @DisplayName("Manager should NOT be able to update users")
  void managerShouldNotBeAbleToUpdateUsers() {
    UpdateUserRequest request = createValidUpdateRequest();

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put(USERS_BASE_PATH + "/" + TEST_USER_ID)
        .then()
        .statusCode(Response.Status.FORBIDDEN.getStatusCode());
  }

  @Test
  @TestSecurity(
      user = "manager",
      roles = {"manager"})
  @DisplayName("Manager should NOT be able to delete users")
  void managerShouldNotBeAbleToDeleteUsers() {
    given()
        .when()
        .delete(USERS_BASE_PATH + "/" + TEST_USER_ID)
        .then()
        .statusCode(Response.Status.FORBIDDEN.getStatusCode());
  }

  @Test
  @TestSecurity(
      user = "manager",
      roles = {"manager"})
  @DisplayName("Manager should NOT be able to update user roles")
  void managerShouldNotBeAbleToUpdateUserRoles() {
    UpdateUserRolesRequest request = createValidRolesUpdateRequest();

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put(USERS_BASE_PATH + "/" + TEST_USER_ID + "/roles")
        .then()
        .statusCode(Response.Status.FORBIDDEN.getStatusCode());
  }

  @Test
  @TestSecurity(
      user = "sales",
      roles = {"sales"})
  @DisplayName("Sales user should NOT be able to update user roles")
  void salesShouldNotBeAbleToUpdateUserRoles() {
    UpdateUserRolesRequest request = createValidRolesUpdateRequest();

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put(USERS_BASE_PATH + "/" + TEST_USER_ID + "/roles")
        .then()
        .statusCode(Response.Status.FORBIDDEN.getStatusCode());
  }

  @Test
  @DisplayName("Unauthenticated user should NOT be able to update user roles")
  void unauthenticatedShouldNotBeAbleToUpdateUserRoles() {
    UpdateUserRolesRequest request = createValidRolesUpdateRequest();

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put(USERS_BASE_PATH + "/" + TEST_USER_ID + "/roles")
        .then()
        .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
  }

  // Helper methods
  private CreateUserRequest createValidUserRequest() {
    String uniqueId = UUID.randomUUID().toString().substring(0, 8);
    return new CreateUserRequest(
        "testuser" + uniqueId,
        "Test",
        "User Security " + uniqueId,
        "security.test." + uniqueId + "@example.com");
  }

  private UpdateUserRequest createValidUpdateRequest() {
    return new UpdateUserRequest(
        "updatedUserSecurity",
        "Updated Test",
        "User Security",
        "updated.security.test@example.com",
        true);
  }

  private UpdateUserRolesRequest createValidRolesUpdateRequest() {
    return new UpdateUserRolesRequest(java.util.List.of("manager", "sales"));
  }
}
