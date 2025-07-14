package de.freshplan.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.domain.user.service.dto.CreateUserRequest;
import de.freshplan.domain.user.service.dto.UpdateUserRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for UserResource REST API.
 *
 * <p>Tests the complete HTTP request/response cycle including authentication, validation, and error
 * handling.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@TestHTTPEndpoint(UserResource.class)
class UserResourceIT {

  @Inject UserRepository userRepository;

  private User testUser;
  private CreateUserRequest validCreateRequest;
  private UpdateUserRequest validUpdateRequest;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean database
    userRepository.deleteAll();

    // Create test data
    testUser = createAndPersistUser();
    validCreateRequest = createValidCreateRequest();
    validUpdateRequest = createValidUpdateRequest();
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  void testCreateUser_Success() {
    given()
        .contentType(ContentType.JSON)
        .body(validCreateRequest)
        .when()
        .post()
        .then()
        .statusCode(201)
        .header("Location", containsString("/api/users/"))
        .body("username", equalTo("new.user"))
        .body("firstName", equalTo("New"))
        .body("lastName", equalTo("User"))
        .body("email", equalTo("new.user@freshplan.de"))
        .body("enabled", equalTo(true))
        .body("id", notNullValue())
        .body("createdAt", notNullValue())
        .body("updatedAt", notNullValue());
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  void testCreateUser_InvalidData_ShouldReturn400() {
    CreateUserRequest invalidRequest =
        CreateUserRequest.builder()
            .username("a") // Too short
            .firstName("") // Blank
            .lastName("User")
            .email("invalid-email") // Invalid format
            .build();

    given()
        .contentType(ContentType.JSON)
        .body(invalidRequest)
        .when()
        .post()
        .then()
        .statusCode(400)
        .body("status", equalTo(400))
        .body("error", equalTo("Bad Request"))
        .body("message", equalTo("Validation failed"));
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  void testCreateUser_DuplicateUsername_ShouldReturn409() {
    CreateUserRequest duplicateRequest =
        CreateUserRequest.builder()
            .username(testUser.getUsername())
            .firstName("Another")
            .lastName("User")
            .email("another@freshplan.de")
            .build();

    given()
        .contentType(ContentType.JSON)
        .body(duplicateRequest)
        .when()
        .post()
        .then()
        .statusCode(409)
        .body("status", equalTo(409))
        .body("error", equalTo("Conflict"))
        .body("message", containsString(testUser.getUsername()));
  }

  @Test
  void testCreateUser_Unauthorized_ShouldReturn401() {
    given()
        .contentType(ContentType.JSON)
        .body(validCreateRequest)
        .when()
        .post()
        .then()
        .statusCode(401);
  }

  @Test
  @TestSecurity(user = "sales", roles = "sales")
  void testCreateUser_Forbidden_ShouldReturn403() {
    given()
        .contentType(ContentType.JSON)
        .body(validCreateRequest)
        .when()
        .post()
        .then()
        .statusCode(403);
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  void testGetAllUsers_Success() {
    given()
        .when()
        .get()
        .then()
        .statusCode(200)
        .body("$", hasSize(1))
        .body("[0].username", equalTo(testUser.getUsername()));
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  void testGetUserById_Success() {
    given()
        .when()
        .get("/{id}", testUser.getId())
        .then()
        .statusCode(200)
        .body("id", equalTo(testUser.getId().toString()))
        .body("username", equalTo(testUser.getUsername()))
        .body("firstName", equalTo(testUser.getFirstName()))
        .body("lastName", equalTo(testUser.getLastName()))
        .body("email", equalTo(testUser.getEmail()));
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  void testGetUserById_NotFound_ShouldReturn404() {
    UUID nonExistentId = UUID.randomUUID();

    given()
        .when()
        .get("/{id}", nonExistentId)
        .then()
        .statusCode(404)
        .body("status", equalTo(404))
        .body("error", equalTo("USER_NOT_FOUND"))
        .body("message", containsString(nonExistentId.toString()));
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  void testUpdateUser_Success() {
    given()
        .contentType(ContentType.JSON)
        .body(validUpdateRequest)
        .when()
        .put("/{id}", testUser.getId())
        .then()
        .statusCode(200)
        .body("username", equalTo("updated.user"))
        .body("firstName", equalTo("Updated"))
        .body("lastName", equalTo("User"))
        .body("email", equalTo("updated@freshplan.de"));
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  void testUpdateUser_NotFound_ShouldReturn404() {
    UUID nonExistentId = UUID.randomUUID();

    given()
        .contentType(ContentType.JSON)
        .body(validUpdateRequest)
        .when()
        .put("/{id}", nonExistentId)
        .then()
        .statusCode(404);
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  void testUpdateUser_InvalidData_ShouldReturn400() {
    UpdateUserRequest invalidRequest =
        UpdateUserRequest.builder()
            .username("ab") // Too short
            .firstName("Updated")
            .lastName("") // Blank
            .email("not-an-email") // Invalid
            .enabled(true)
            .build();

    given()
        .contentType(ContentType.JSON)
        .body(invalidRequest)
        .when()
        .put("/{id}", testUser.getId())
        .then()
        .statusCode(400)
        .body("status", equalTo(400))
        .body("error", equalTo("Bad Request"))
        .body("message", equalTo("Validation failed"));
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  void testDeleteUser_Success() {
    given().when().delete("/{id}", testUser.getId()).then().statusCode(204);

    // Verify deletion
    given().when().get("/{id}", testUser.getId()).then().statusCode(404);
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  void testDeleteUser_NotFound_ShouldReturn404() {
    UUID nonExistentId = UUID.randomUUID();

    given().when().delete("/{id}", nonExistentId).then().statusCode(404);
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  @Transactional
  void testEnableUser_Success() {
    // First disable the user
    testUser.disable();
    userRepository.getEntityManager().merge(testUser);

    given().when().put("/{id}/enable", testUser.getId()).then().statusCode(204);
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  void testDisableUser_Success() {
    given().when().put("/{id}/disable", testUser.getId()).then().statusCode(204);
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  void testSearchByEmail_Found() {
    given()
        .queryParam("email", testUser.getEmail())
        .when()
        .get("/search")
        .then()
        .statusCode(200)
        .body("username", equalTo(testUser.getUsername()));
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  void testSearchByEmail_NotFound() {
    given()
        .queryParam("email", "notfound@freshplan.de")
        .when()
        .get("/search")
        .then()
        .statusCode(404);
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  void testSearchByEmail_MissingParam_ShouldReturn400() {
    given().when().get("/search").then().statusCode(400);
  }

  // Helper methods

  @Transactional
  User createAndPersistUser() {
    User user = new User("test.user", "Test", "User", "test.user@freshplan.de");
    userRepository.persist(user);
    return user;
  }

  private CreateUserRequest createValidCreateRequest() {
    return CreateUserRequest.builder()
        .username("new.user")
        .firstName("New")
        .lastName("User")
        .email("new.user@freshplan.de")
        .build();
  }

  private UpdateUserRequest createValidUpdateRequest() {
    return UpdateUserRequest.builder()
        .username("updated.user")
        .firstName("Updated")
        .lastName("User")
        .email("updated@freshplan.de")
        .enabled(true)
        .build();
  }
}
