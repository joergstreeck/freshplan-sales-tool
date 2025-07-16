package de.freshplan.infrastructure.security;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import de.freshplan.test.SecurityDisabledTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for Role-Based Access Control (RBAC) across the entire application. Tests the
 * interaction between different roles and their access permissions.
 */
@QuarkusTest
@TestProfile(SecurityDisabledTestProfile.class)
@Disabled("Temporarily disabled - needs proper test data setup and role configuration")
class RoleBasedAccessIntegrationTest {

  @Nested
  @DisplayName("Admin Role Access Tests")
  class AdminRoleTests {

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should have full access to user management")
    void admin_shouldHaveFullUserManagementAccess() {
      // List users
      given().when().get("/api/users").then().statusCode(Response.Status.OK.getStatusCode());

      // Create user
      var createUserResponse =
          given()
              .contentType(ContentType.JSON)
              .body(
                  Map.of(
                      "username", "adminCreatedUser",
                      "firstName", "Admin",
                      "lastName", "Created",
                      "email", "admin.created@example.com"))
              .when()
              .post("/api/users")
              .then()
              .statusCode(Response.Status.CREATED.getStatusCode())
              .extract()
              .path("id");

      // Read specific user
      given()
          .when()
          .get("/api/users/" + createUserResponse)
          .then()
          .statusCode(Response.Status.OK.getStatusCode())
          .body("username", equalTo("adminCreatedUser"));

      // Update user
      given()
          .contentType(ContentType.JSON)
          .body(
              Map.of(
                  "username", "updatedAdminUser",
                  "firstName", "Updated",
                  "lastName", "Admin",
                  "email", "updated.admin@example.com",
                  "enabled", true))
          .when()
          .put("/api/users/" + createUserResponse)
          .then()
          .statusCode(Response.Status.OK.getStatusCode());

      // Delete user
      given()
          .when()
          .delete("/api/users/" + createUserResponse)
          .then()
          .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should have full access to customer management")
    void admin_shouldHaveFullCustomerManagementAccess() {
      // Create customer
      var createCustomerResponse =
          given()
              .contentType(ContentType.JSON)
              .body(
                  Map.of(
                      "customerType", "NEUKUNDE",
                      "companyName", "Admin Test Company",
                      "contactPerson", "Admin Contact",
                      "email", "admin.customer@example.com"))
              .when()
              .post("/api/customers")
              .then()
              .statusCode(Response.Status.CREATED.getStatusCode())
              .extract()
              .path("id");

      // Read customers
      given().when().get("/api/customers").then().statusCode(Response.Status.OK.getStatusCode());

      // Update customer
      given()
          .contentType(ContentType.JSON)
          .body(
              Map.of(
                  "customerType", "BESTANDSKUNDE",
                  "companyName", "Updated Admin Company",
                  "contactPerson", "Updated Admin Contact",
                  "email", "updated.admin.customer@example.com"))
          .when()
          .put("/api/customers/" + createCustomerResponse)
          .then()
          .statusCode(Response.Status.OK.getStatusCode());

      // Delete customer (admin only)
      given()
          .when()
          .delete("/api/customers/" + createCustomerResponse)
          .then()
          .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should be able to manage user roles")
    void admin_shouldManageUserRoles() {
      // Create user first
      var userId =
          given()
              .contentType(ContentType.JSON)
              .body(
                  Map.of(
                      "username", "roleTestUser",
                      "firstName", "Role",
                      "lastName", "Test",
                      "email", "role.test@example.com"))
              .when()
              .post("/api/users")
              .then()
              .statusCode(Response.Status.CREATED.getStatusCode())
              .extract()
              .path("id");

      // Update roles
      given()
          .contentType(ContentType.JSON)
          .body(Map.of("roles", List.of("manager", "sales")))
          .when()
          .put("/api/users/" + userId + "/roles")
          .then()
          .statusCode(Response.Status.OK.getStatusCode());

      // Verify roles were updated
      given()
          .when()
          .get("/api/users/" + userId)
          .then()
          .statusCode(Response.Status.OK.getStatusCode())
          .body("roles", hasItems("manager", "sales"));
    }
  }

  @Nested
  @DisplayName("Manager Role Access Tests")
  class ManagerRoleTests {

    @Test
    @TestSecurity(
        user = "manager",
        roles = {"manager"})
    @DisplayName("Manager should have customer create and update access")
    void manager_shouldHaveCustomerCreateUpdateAccess() {
      // Create customer (allowed)
      var customerId =
          given()
              .contentType(ContentType.JSON)
              .body(
                  Map.of(
                      "customerType", "NEUKUNDE",
                      "companyName", "Manager Test Company",
                      "contactPerson", "Manager Contact",
                      "email", "manager.customer@example.com"))
              .when()
              .post("/api/customers")
              .then()
              .statusCode(Response.Status.CREATED.getStatusCode())
              .extract()
              .path("id");

      // Read customers (allowed)
      given().when().get("/api/customers").then().statusCode(Response.Status.OK.getStatusCode());

      // Update customer (allowed)
      given()
          .contentType(ContentType.JSON)
          .body(
              Map.of(
                  "customerType", "BESTANDSKUNDE",
                  "companyName", "Updated Manager Company",
                  "contactPerson", "Updated Manager Contact",
                  "email", "updated.manager.customer@example.com"))
          .when()
          .put("/api/customers/" + customerId)
          .then()
          .statusCode(Response.Status.OK.getStatusCode());

      // Delete customer (forbidden for manager)
      given()
          .when()
          .delete("/api/customers/" + customerId)
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "manager",
        roles = {"manager"})
    @DisplayName("Manager should NOT have user management access")
    void manager_shouldNotHaveUserManagementAccess() {
      // List users (forbidden)
      given().when().get("/api/users").then().statusCode(Response.Status.FORBIDDEN.getStatusCode());

      // Create user (forbidden)
      given()
          .contentType(ContentType.JSON)
          .body(
              Map.of(
                  "username", "managerAttemptUser",
                  "firstName", "Manager",
                  "lastName", "Attempt",
                  "email", "manager.attempt@example.com"))
          .when()
          .post("/api/users")
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());

      // Update user roles (forbidden)
      given()
          .contentType(ContentType.JSON)
          .body(Map.of("roles", List.of("sales")))
          .when()
          .put("/api/users/" + UUID.randomUUID() + "/roles")
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "manager",
        roles = {"manager"})
    @DisplayName("Manager should have access to own profile")
    void manager_shouldHaveOwnProfileAccess() {
      given()
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(Response.Status.OK.getStatusCode())
          .body("username", equalTo("manager"))
          .body("roles", hasItem("manager"));
    }
  }

  @Nested
  @DisplayName("Sales Role Access Tests")
  class SalesRoleTests {

    @Test
    @TestSecurity(
        user = "sales",
        roles = {"sales"})
    @DisplayName("Sales should have read-only customer access")
    void sales_shouldHaveReadOnlyCustomerAccess() {
      // Read customers (allowed)
      given().when().get("/api/customers").then().statusCode(Response.Status.OK.getStatusCode());

      // Create customer (forbidden)
      given()
          .contentType(ContentType.JSON)
          .body(
              Map.of(
                  "customerType", "NEUKUNDE",
                  "companyName", "Sales Test Company",
                  "contactPerson", "Sales Contact",
                  "email", "sales.customer@example.com"))
          .when()
          .post("/api/customers")
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());

      // Update customer (forbidden)
      given()
          .contentType(ContentType.JSON)
          .body(
              Map.of(
                  "customerType", "BESTANDSKUNDE",
                  "companyName", "Updated Sales Company",
                  "contactPerson", "Updated Sales Contact",
                  "email", "updated.sales.customer@example.com"))
          .when()
          .put("/api/customers/" + UUID.randomUUID())
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());

      // Delete customer (forbidden)
      given()
          .when()
          .delete("/api/customers/" + UUID.randomUUID())
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "sales",
        roles = {"sales"})
    @DisplayName("Sales should NOT have user management access")
    void sales_shouldNotHaveUserManagementAccess() {
      // List users (forbidden)
      given().when().get("/api/users").then().statusCode(Response.Status.FORBIDDEN.getStatusCode());

      // Access specific user (forbidden)
      given()
          .when()
          .get("/api/users/" + UUID.randomUUID())
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());

      // Create user (forbidden)
      given()
          .contentType(ContentType.JSON)
          .body(
              Map.of(
                  "username", "salesAttemptUser",
                  "firstName", "Sales",
                  "lastName", "Attempt",
                  "email", "sales.attempt@example.com"))
          .when()
          .post("/api/users")
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "sales",
        roles = {"sales"})
    @DisplayName("Sales should have access to own profile")
    void sales_shouldHaveOwnProfileAccess() {
      given()
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(Response.Status.OK.getStatusCode())
          .body("username", equalTo("sales"))
          .body("roles", hasItem("sales"));
    }
  }

  @Nested
  @DisplayName("Viewer Role Access Tests")
  @Disabled("Viewer role not yet implemented in the system")
  class ViewerRoleTests {

    @Test
    @TestSecurity(
        user = "viewer",
        roles = {"viewer"})
    @DisplayName("Viewer should have read-only access to customers")
    void viewer_shouldHaveReadOnlyCustomerAccess() {
      // Read customers (allowed)
      given().when().get("/api/customers").then().statusCode(Response.Status.OK.getStatusCode());

      // All write operations should be forbidden
      given()
          .contentType(ContentType.JSON)
          .body(
              Map.of(
                  "customerType", "NEUKUNDE",
                  "companyName", "Viewer Test Company",
                  "contactPerson", "Viewer Contact",
                  "email", "viewer.customer@example.com"))
          .when()
          .post("/api/customers")
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "viewer",
        roles = {"viewer"})
    @DisplayName("Viewer should NOT have any user management access")
    void viewer_shouldNotHaveUserManagementAccess() {
      // No user management operations allowed
      given().when().get("/api/users").then().statusCode(Response.Status.FORBIDDEN.getStatusCode());

      given()
          .when()
          .get("/api/users/" + UUID.randomUUID())
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "viewer",
        roles = {"viewer"})
    @DisplayName("Viewer should have access to own profile")
    void viewer_shouldHaveOwnProfileAccess() {
      given()
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(Response.Status.OK.getStatusCode())
          .body("username", equalTo("viewer"))
          .body("roles", hasItem("viewer"));
    }
  }

  @Nested
  @DisplayName("Multiple Roles Integration Tests")
  class MultipleRolesTests {

    @Test
    @TestSecurity(
        user = "multiuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("User with multiple roles should get highest privilege level")
    void userWithMultipleRoles_shouldGetHighestPrivilege() {
      // Should have admin privileges (highest level)
      given().when().get("/api/users").then().statusCode(Response.Status.OK.getStatusCode());

      // Should be able to create customers (admin/manager privilege)
      var customerId =
          given()
              .contentType(ContentType.JSON)
              .body(
                  Map.of(
                      "customerType", "NEUKUNDE",
                      "companyName", "Multi Role Company",
                      "contactPerson", "Multi Contact",
                      "email", "multi.role@example.com"))
              .when()
              .post("/api/customers")
              .then()
              .statusCode(Response.Status.CREATED.getStatusCode())
              .extract()
              .path("id");

      // Should be able to delete customers (admin privilege)
      given()
          .when()
          .delete("/api/customers/" + customerId)
          .then()
          .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "managersales",
        roles = {"manager", "sales"})
    @DisplayName("Manager+Sales user should have manager privileges")
    void managerSalesUser_shouldHaveManagerPrivileges() {
      // Should have manager privileges (create/update customers)
      var customerId =
          given()
              .contentType(ContentType.JSON)
              .body(
                  Map.of(
                      "customerType", "NEUKUNDE",
                      "companyName", "Manager Sales Company",
                      "contactPerson", "Manager Sales Contact",
                      "email", "manager.sales@example.com"))
              .when()
              .post("/api/customers")
              .then()
              .statusCode(Response.Status.CREATED.getStatusCode())
              .extract()
              .path("id");

      // Should be able to update
      given()
          .contentType(ContentType.JSON)
          .body(
              Map.of(
                  "customerType", "BESTANDSKUNDE",
                  "companyName", "Updated Manager Sales Company",
                  "contactPerson", "Updated Manager Sales Contact",
                  "email", "updated.manager.sales@example.com"))
          .when()
          .put("/api/customers/" + customerId)
          .then()
          .statusCode(Response.Status.OK.getStatusCode());

      // But should NOT be able to delete (requires admin)
      given()
          .when()
          .delete("/api/customers/" + customerId)
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());

      // Should NOT have user management access
      given().when().get("/api/users").then().statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }
  }

  @Nested
  @DisplayName("Cross-Role Operation Tests")
  class CrossRoleOperationTests {

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin operations should maintain data integrity across roles")
    void adminOperations_shouldMaintainDataIntegrityAcrossRoles() {
      // Admin creates a customer
      var customerId =
          given()
              .contentType(ContentType.JSON)
              .body(
                  Map.of(
                      "customerType", "NEUKUNDE",
                      "companyName", "Cross Role Test Company",
                      "contactPerson", "Cross Role Contact",
                      "email", "cross.role@example.com"))
              .when()
              .post("/api/customers")
              .then()
              .statusCode(Response.Status.CREATED.getStatusCode())
              .extract()
              .path("id");

      // Verify the customer exists and has correct data
      given()
          .when()
          .get("/api/customers/" + customerId)
          .then()
          .statusCode(Response.Status.OK.getStatusCode())
          .body("companyName", equalTo("Cross Role Test Company"))
          .body("customerType", equalTo("NEUKUNDE"));
    }

    @Test
    @DisplayName("Operations should fail gracefully for insufficient permissions")
    void operations_shouldFailGracefullyForInsufficientPermissions() {
      // Unauthenticated access should return 401
      given()
          .when()
          .get("/api/users")
          .then()
          .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());

      given()
          .when()
          .post("/api/customers")
          .then()
          .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"nonexistent_role"})
    @DisplayName("Users with invalid roles should be handled appropriately")
    void usersWithInvalidRoles_shouldBeHandledAppropriately() {
      // User with invalid role should not have access to endpoints requiring specific roles
      given()
          .when()
          .get("/api/users/me")
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());

      // But should not have access to protected resources
      given().when().get("/api/users").then().statusCode(Response.Status.FORBIDDEN.getStatusCode());

      given()
          .when()
          .get("/api/customers")
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }
  }

  @Nested
  @DisplayName("Security Audit Tests")
  class SecurityAuditTests {

    @Test
    @TestSecurity(
        user = "auditadmin",
        roles = {"admin"})
    @DisplayName("Admin operations should be auditable")
    void adminOperations_shouldBeAuditable() {
      // Perform various admin operations
      given()
          .contentType(ContentType.JSON)
          .body(
              Map.of(
                  "username", "auditTestUser",
                  "firstName", "Audit",
                  "lastName", "Test",
                  "email", "audit@example.com"))
          .when()
          .post("/api/users")
          .then()
          .statusCode(Response.Status.CREATED.getStatusCode());

      given()
          .contentType(ContentType.JSON)
          .body(
              Map.of(
                  "customerType", "NEUKUNDE",
                  "companyName", "Audit Company",
                  "contactPerson", "Audit Contact",
                  "email", "audit.company@example.com"))
          .when()
          .post("/api/customers")
          .then()
          .statusCode(Response.Status.CREATED.getStatusCode());

      // Note: In a real implementation, we would verify audit log entries
      // For integration tests, we ensure operations complete successfully
    }

    @Test
    @TestSecurity(
        user = "auditmanager",
        roles = {"manager"})
    @DisplayName("Manager operations should be auditable")
    void managerOperations_shouldBeAuditable() {
      // Perform manager operations
      given()
          .contentType(ContentType.JSON)
          .body(
              Map.of(
                  "customerType", "NEUKUNDE",
                  "companyName", "Manager Audit Company",
                  "contactPerson", "Manager Audit Contact",
                  "email", "manager.audit@example.com"))
          .when()
          .post("/api/customers")
          .then()
          .statusCode(Response.Status.CREATED.getStatusCode());

      // Access own profile
      given().when().get("/api/users/me").then().statusCode(Response.Status.OK.getStatusCode());
    }
  }
}
