package de.freshplan.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import de.freshplan.domain.user.service.dto.CreateUserRequest;
import de.freshplan.domain.user.service.dto.UpdateUserRequest;
import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
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
 * DEBUG VERSION: Integration tests for UserResource REST API.
 * 
 * This debug version includes extensive logging to identify race conditions
 * and timing issues in CI environment.
 */
@QuarkusTest
@TestHTTPEndpoint(UserResource.class)
class UserResourceITDebug {

  @Inject UserRepository userRepository;

  private User testUser;
  private CreateUserRequest validCreateRequest;
  private UpdateUserRequest validUpdateRequest;

  @BeforeEach
  @Transactional
  void setUp() {
    System.out.println("=== DEBUG: setUp() started ===");
    
    // Clean up any existing test users
    userRepository.delete("username LIKE ?1", "test.user%");
    userRepository.delete("username LIKE ?1", "new.user%");
    userRepository.delete("username LIKE ?1", "to.delete%");
    userRepository.delete("username LIKE ?1", "updated.user%");
    
    long remainingUsers = userRepository.count();
    System.out.println("=== DEBUG: Remaining users after cleanup: " + remainingUsers + " ===");

    // Create fresh test user with unique timestamp
    String timestamp = String.valueOf(System.currentTimeMillis());
    testUser = new User("test.user." + timestamp, "Test", "User", "test.user." + timestamp + "@freshplan.de");
    userRepository.persist(testUser);
    
    System.out.println("=== DEBUG: Created testUser with ID: " + testUser.getId() + ", username: " + testUser.getUsername() + " ===");

    validCreateRequest = createValidCreateRequest();
    validUpdateRequest = createValidUpdateRequest();
    
    System.out.println("=== DEBUG: setUp() completed ===");
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  void testDeleteUser_Success_DEBUG() {
    System.out.println("=== DEBUG: testDeleteUser_Success_DEBUG started ===");
    
    // Create user via API to ensure it's properly committed
    String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
    System.out.println("=== DEBUG: Using timestamp: " + uniqueTimestamp + " ===");
    
    CreateUserRequest createRequest = CreateUserRequest.builder()
        .username("to.delete.debug." + uniqueTimestamp)
        .firstName("To")
        .lastName("Delete")
        .email("to.delete.debug." + uniqueTimestamp + "@freshplan.de")
        .build();
    
    System.out.println("=== DEBUG: Creating user with username: " + createRequest.getUsername() + " ===");
    
    String location = given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post()
        .then()
        .statusCode(201)
        .extract()
        .header("Location");
    
    System.out.println("=== DEBUG: User created, location header: " + location + " ===");
    
    // Extract ID from location header
    String userId = location.substring(location.lastIndexOf("/") + 1);
    System.out.println("=== DEBUG: Extracted userId: " + userId + " ===");
    
    // Verify user exists before deletion
    given().when().get("/{id}", userId).then().statusCode(200);
    System.out.println("=== DEBUG: User exists, proceeding with deletion ===");
    
    // Delete the user
    given().when().delete("/{id}", userId).then().statusCode(204);
    System.out.println("=== DEBUG: User deleted successfully ===");

    // Verify deletion
    given().when().get("/{id}", userId).then().statusCode(404);
    System.out.println("=== DEBUG: Deletion verified - user not found ===");
    
    System.out.println("=== DEBUG: testDeleteUser_Success_DEBUG completed ===");
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  void testCreateUser_Success_DEBUG() {
    System.out.println("=== DEBUG: testCreateUser_Success_DEBUG started ===");
    
    String timestamp = String.valueOf(System.currentTimeMillis());
    CreateUserRequest debugRequest = CreateUserRequest.builder()
        .username("create.debug." + timestamp)
        .firstName("Create")
        .lastName("Debug")
        .email("create.debug." + timestamp + "@freshplan.de")
        .build();
    
    System.out.println("=== DEBUG: Creating user: " + debugRequest.getUsername() + " ===");
    
    given()
        .contentType(ContentType.JSON)
        .body(debugRequest)
        .when()
        .post()
        .then()
        .statusCode(201)
        .header("Location", org.hamcrest.Matchers.containsString("/api/users/"));
    
    System.out.println("=== DEBUG: User creation successful ===");
    System.out.println("=== DEBUG: testCreateUser_Success_DEBUG completed ===");
  }

  private CreateUserRequest createValidCreateRequest() {
    String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
    System.out.println("=== DEBUG: createValidCreateRequest with timestamp: " + uniqueTimestamp + " ===");
    
    return CreateUserRequest.builder()
        .username("new.user.debug." + uniqueTimestamp)
        .firstName("New")
        .lastName("User")
        .email("new.user.debug." + uniqueTimestamp + "@freshplan.de")
        .build();
  }

  private UpdateUserRequest createValidUpdateRequest() {
    String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
    System.out.println("=== DEBUG: createValidUpdateRequest with timestamp: " + uniqueTimestamp + " ===");
    
    return UpdateUserRequest.builder()
        .username("updated.user.debug." + uniqueTimestamp)
        .firstName("Updated")
        .lastName("User")
        .email("updated.user.debug." + uniqueTimestamp + "@freshplan.de")
        .build();
  }
}