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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Security tests for UserResource endpoints. Tests admin-only access and role management security.
 */
@QuarkusTest
@TestProfile(SecurityDisabledTestProfile.class)
class UserResourceSecurityTest {

  private static final String USERS_BASE_PATH = "/api/users";

  @Nested
  @DisplayName("Admin-Only Access Tests")
  class AdminOnlyAccessTests {

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should be able to create users")
    void adminShouldBeAbleToCreateUsers() {
      CreateUserRequest request = createValidUserRequest();

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post(USERS_BASE_PATH)
          .then()
          .statusCode(Response.Status.CREATED.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "manager",
        roles = {"manager"})
    @DisplayName("Manager should NOT be able to create users")
    void managerShouldNotBeAbleToCreateUsers() {
      CreateUserRequest request = createValidUserRequest();

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post(USERS_BASE_PATH)
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "sales",
        roles = {"sales"})
    @DisplayName("Sales user should NOT be able to create users")
    void salesShouldNotBeAbleToCreateUsers() {
      CreateUserRequest request = createValidUserRequest();

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post(USERS_BASE_PATH)
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    // Note: Viewer role was removed - sales role now represents lowest access level

    @Test
    @DisplayName("Unauthenticated user should NOT be able to create users")
    void unauthenticatedShouldNotBeAbleToCreateUsers() {
      CreateUserRequest request = createValidUserRequest();

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post(USERS_BASE_PATH)
          .then()
          .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }
  }

  @Nested
  @DisplayName("User Read Access Tests")
  class UserReadAccessTests {

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should be able to read all users")
    void adminShouldBeAbleToReadAllUsers() {
      given().when().get(USERS_BASE_PATH).then().statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should be able to read individual user")
    void adminShouldBeAbleToReadIndividualUser() {
      UUID userId = createTestUser();

      given()
          .when()
          .get(USERS_BASE_PATH + "/" + userId)
          .then()
          .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "manager",
        roles = {"manager"})
    @DisplayName("Manager should NOT be able to read users")
    void managerShouldNotBeAbleToReadUsers() {
      given()
          .when()
          .get(USERS_BASE_PATH)
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "sales",
        roles = {"sales"})
    @DisplayName("Sales user should NOT be able to read users")
    void salesShouldNotBeAbleToReadUsers() {
      given()
          .when()
          .get(USERS_BASE_PATH)
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }
  }

  @Nested
  @DisplayName("User Update Tests")
  class UserUpdateTests {

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should be able to update users")
    void adminShouldBeAbleToUpdateUsers() {
      UUID userId = createTestUser();
      UpdateUserRequest request = createValidUpdateRequest();

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .put(USERS_BASE_PATH + "/" + userId)
          .then()
          .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "manager",
        roles = {"manager"})
    @DisplayName("Manager should NOT be able to update users")
    void managerShouldNotBeAbleToUpdateUsers() {
      UUID userId = createTestUser();
      UpdateUserRequest request = createValidUpdateRequest();

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .put(USERS_BASE_PATH + "/" + userId)
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should be able to enable users")
    void adminShouldBeAbleToEnableUsers() {
      UUID userId = createTestUser();

      given()
          .when()
          .put(USERS_BASE_PATH + "/" + userId + "/enable")
          .then()
          .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should be able to disable users")
    void adminShouldBeAbleToDisableUsers() {
      UUID userId = createTestUser();

      given()
          .when()
          .put(USERS_BASE_PATH + "/" + userId + "/disable")
          .then()
          .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }
  }

  @Nested
  @DisplayName("User Deletion Tests")
  class UserDeletionTests {

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should be able to delete users")
    void adminShouldBeAbleToDeleteUsers() {
      UUID userId = createTestUser();

      given()
          .when()
          .delete(USERS_BASE_PATH + "/" + userId)
          .then()
          .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "manager",
        roles = {"manager"})
    @DisplayName("Manager should NOT be able to delete users")
    void managerShouldNotBeAbleToDeleteUsers() {
      UUID userId = createTestUser();

      given()
          .when()
          .delete(USERS_BASE_PATH + "/" + userId)
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }
  }

  @Nested
  @DisplayName("Role Management Security Tests")
  class RoleManagementTests {

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should be able to update user roles")
    void adminShouldBeAbleToUpdateUserRoles() {
      UUID userId = createTestUser();
      UpdateUserRolesRequest request = createValidRolesUpdateRequest();

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .put(USERS_BASE_PATH + "/" + userId + "/roles")
          .then()
          .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "manager",
        roles = {"manager"})
    @DisplayName("Manager should NOT be able to update user roles")
    void managerShouldNotBeAbleToUpdateUserRoles() {
      UUID userId = createTestUser();
      UpdateUserRolesRequest request = createValidRolesUpdateRequest();

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .put(USERS_BASE_PATH + "/" + userId + "/roles")
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "sales",
        roles = {"sales"})
    @DisplayName("Sales user should NOT be able to update user roles")
    void salesShouldNotBeAbleToUpdateUserRoles() {
      UUID userId = createTestUser();
      UpdateUserRolesRequest request = createValidRolesUpdateRequest();

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .put(USERS_BASE_PATH + "/" + userId + "/roles")
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @DisplayName("Unauthenticated user should NOT be able to update user roles")
    void unauthenticatedShouldNotBeAbleToUpdateUserRoles() {
      UUID userId = createTestUser();
      UpdateUserRolesRequest request = createValidRolesUpdateRequest();

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .put(USERS_BASE_PATH + "/" + userId + "/roles")
          .then()
          .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }
  }

  @Nested
  @DisplayName("User Search Security Tests")
  class UserSearchTests {

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should be able to search users by email")
    void adminShouldBeAbleToSearchUsersByEmail() {
      given()
          .queryParam("email", "test@example.com")
          .when()
          .get(USERS_BASE_PATH + "/search")
          .then()
          .statusCode(
              anyOf(
                  is(Response.Status.OK.getStatusCode()),
                  is(Response.Status.NOT_FOUND.getStatusCode())));
    }

    @Test
    @TestSecurity(
        user = "manager",
        roles = {"manager"})
    @DisplayName("Manager should NOT be able to search users")
    void managerShouldNotBeAbleToSearchUsers() {
      given()
          .queryParam("email", "test@example.com")
          .when()
          .get(USERS_BASE_PATH + "/search")
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should get bad request for search without email parameter")
    void adminShouldGetBadRequestForSearchWithoutEmail() {
      given()
          .when()
          .get(USERS_BASE_PATH + "/search")
          .then()
          .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }
  }

  @Nested
  @DisplayName("User List Filtering Security Tests")
  class UserListFilteringTests {

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should be able to list users with filters")
    void adminShouldBeAbleToListUsersWithFilters() {
      given()
          .queryParam("page", 0)
          .queryParam("size", 10)
          .queryParam("enabledOnly", true)
          .when()
          .get(USERS_BASE_PATH)
          .then()
          .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should be able to search users with search term")
    void adminShouldBeAbleToSearchUsersWithSearchTerm() {
      given()
          .queryParam("search", "test")
          .queryParam("page", 0)
          .queryParam("size", 10)
          .when()
          .get(USERS_BASE_PATH)
          .then()
          .statusCode(Response.Status.OK.getStatusCode());
    }
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

  /**
   * Creates a real test user for security tests. This ensures the user exists in the database when
   * testing authorization rules.
   */
  private UUID createTestUser() {
    // Create a real user for testing
    CreateUserRequest request = createValidUserRequest();

    var response =
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post(USERS_BASE_PATH)
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .extract()
            .response();

    // Extract ID from Location header
    String location = response.getHeader("Location");
    if (location != null && location.contains("/")) {
      String idString = location.substring(location.lastIndexOf('/') + 1);
      return UUID.fromString(idString);
    }

    // Fallback to response body
    return response.jsonPath().getUUID("id");
  }
}
