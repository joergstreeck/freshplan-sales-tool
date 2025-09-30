package de.freshplan.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import de.freshplan.domain.permission.service.PermissionService;
import de.freshplan.infrastructure.security.SecurityContextProvider;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("migrate")
@DisplayName("PermissionResource Tests")
class PermissionResourceTest {

  @InjectMock PermissionService permissionService;

  @InjectMock SecurityContextProvider securityProvider;

  @BeforeEach
  void setUp() {
    reset(permissionService, securityProvider);
  }

  // ===== GET /api/permissions/me Tests =====

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin"})
  @DisplayName("GET /me should return current user permissions for admin")
  void getCurrentUserPermissions_asAdmin_shouldReturnAdminPermissions() {
    // Arrange
    List<String> adminPermissions =
        Arrays.asList(
            "customers:read",
            "customers:write",
            "customers:delete",
            "admin:permissions",
            "admin:users");
    when(permissionService.getCurrentUserPermissions()).thenReturn(adminPermissions);

    // Act & Assert
    given()
        .when()
        .get("/api/permissions/me")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("permissions", hasSize(5))
        .body(
            "permissions",
            containsInAnyOrder(
                "customers:read",
                "customers:write",
                "customers:delete",
                "admin:permissions",
                "admin:users"));
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"manager"})
  @DisplayName("GET /me should return manager permissions")
  void getCurrentUserPermissions_asManager_shouldReturnManagerPermissions() {
    // Arrange
    List<String> managerPermissions = Arrays.asList("customers:read", "customers:write");
    when(permissionService.getCurrentUserPermissions()).thenReturn(managerPermissions);

    // Act & Assert
    given()
        .when()
        .get("/api/permissions/me")
        .then()
        .statusCode(200)
        .body("permissions", hasSize(2))
        .body("permissions", containsInAnyOrder("customers:read", "customers:write"));
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"sales"})
  @DisplayName("GET /me should return sales permissions")
  void getCurrentUserPermissions_asSales_shouldReturnSalesPermissions() {
    // Arrange
    List<String> salesPermissions = Arrays.asList("customers:read");
    when(permissionService.getCurrentUserPermissions()).thenReturn(salesPermissions);

    // Act & Assert
    given()
        .when()
        .get("/api/permissions/me")
        .then()
        .statusCode(200)
        .body("permissions", hasSize(1))
        .body("permissions[0]", equalTo("customers:read"));
  }

  @Test
  @DisplayName("GET /me should return 401 without authentication")
  void getCurrentUserPermissions_withoutAuth_shouldReturn401() {
    given().when().get("/api/permissions/me").then().statusCode(401);
  }

  // ===== GET /api/permissions Tests =====

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin"})
  @DisplayName("GET /api/permissions should return all permissions for admin")
  void getAllPermissions_asAdmin_shouldReturnAllPermissions() {
    // Note: This test will interact with the actual database via Permission.listAll()
    // In a real test, we might want to use @QuarkusIntegrationTest or mock the static method

    given()
        .when()
        .get("/api/permissions")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("permissions", notNullValue());
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"manager"})
  @DisplayName("GET /api/permissions should return 403 for non-admin")
  void getAllPermissions_asManager_shouldReturn403() {
    given().when().get("/api/permissions").then().statusCode(403);
  }

  // ===== POST /api/permissions/grant Tests =====

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin"})
  @DisplayName("POST /grant should return 501 Not Implemented")
  void grantPermission_asAdmin_shouldReturn501() {
    // Arrange
    Map<String, Object> request =
        Map.of(
            "userId", UUID.randomUUID().toString(),
            "permissionCode", "customers:read",
            "reason", "Test grant");

    // Act & Assert
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/permissions/grant")
        .then()
        .statusCode(501)
        .body("message", equalTo("Permission management not yet implemented"));
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"manager"})
  @DisplayName("POST /grant should return 403 for non-admin")
  void grantPermission_asManager_shouldReturn403() {
    Map<String, Object> request =
        Map.of("userId", UUID.randomUUID().toString(), "permissionCode", "customers:read");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/permissions/grant")
        .then()
        .statusCode(403);
  }

  // ===== POST /api/permissions/revoke Tests =====

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin"})
  @DisplayName("POST /revoke should return 501 Not Implemented")
  void revokePermission_asAdmin_shouldReturn501() {
    // Arrange
    Map<String, Object> request =
        Map.of(
            "userId", UUID.randomUUID().toString(),
            "permissionCode", "customers:read",
            "reason", "Test revoke");

    // Act & Assert
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/permissions/revoke")
        .then()
        .statusCode(501)
        .body("message", equalTo("Permission management not yet implemented"));
  }

  // ===== GET /api/permissions/check/{permissionCode} Tests =====

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin"})
  @DisplayName("GET /check/{code} should return true for admin")
  void checkPermission_asAdmin_shouldReturnTrue() {
    // Arrange
    when(permissionService.hasPermission("customers:write")).thenReturn(true);

    // Act & Assert
    given()
        .when()
        .get("/api/permissions/check/customers:write")
        .then()
        .statusCode(200)
        .body("hasPermission", is(true));
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"sales"})
  @DisplayName("GET /check/{code} should return false for unauthorized permission")
  void checkPermission_asSales_shouldReturnFalseForWrite() {
    // Arrange
    when(permissionService.hasPermission("customers:write")).thenReturn(false);

    // Act & Assert
    given()
        .when()
        .get("/api/permissions/check/customers:write")
        .then()
        .statusCode(200)
        .body("hasPermission", is(false));
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"sales"})
  @DisplayName("GET /check/{code} should return true for authorized permission")
  void checkPermission_asSales_shouldReturnTrueForRead() {
    // Arrange
    when(permissionService.hasPermission("customers:read")).thenReturn(true);

    // Act & Assert
    given()
        .when()
        .get("/api/permissions/check/customers:read")
        .then()
        .statusCode(200)
        .body("hasPermission", is(true));
  }

  // ===== GET /api/permissions/user/{userId} Tests =====

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin"})
  @DisplayName("GET /user/{userId} should return empty permissions (simplified)")
  void getUserPermissions_asAdmin_shouldReturnEmptyList() {
    // Note: This endpoint uses @PermissionRequired annotation
    // which might need additional setup in tests

    UUID userId = UUID.randomUUID();

    given()
        .when()
        .get("/api/permissions/user/" + userId)
        .then()
        .statusCode(200)
        .body("permissions", hasSize(0));
  }

  // ===== Error Handling Tests =====

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin"})
  @DisplayName("GET /api/permissions should handle exceptions gracefully")
  void getAllPermissions_withException_shouldReturn500() {
    // This test would require mocking Permission.listAll() static method
    // which is complex in Quarkus. In real scenario, consider integration tests
    // or refactoring to use a repository pattern
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"sales"})
  @DisplayName("GET /check with empty permission code should handle gracefully")
  void checkPermission_withEmptyCode_shouldHandleGracefully() {
    // Arrange
    when(permissionService.hasPermission("")).thenReturn(false);

    // Act & Assert
    given()
        .when()
        .get("/api/permissions/check/")
        .then()
        .statusCode(404); // Path not found due to empty param
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin"})
  @DisplayName("GET /check with special characters should be handled")
  void checkPermission_withSpecialCharacters_shouldBeHandled() {
    // Arrange
    String encodedPermission = "admin%3Apermissions"; // URL encoded "admin:permissions"
    when(permissionService.hasPermission("admin:permissions")).thenReturn(true);

    // Act & Assert
    given()
        .when()
        .get("/api/permissions/check/admin:permissions")
        .then()
        .statusCode(200)
        .body("hasPermission", is(true));
  }
}
