package de.freshplan.infrastructure.security;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import de.freshplan.domain.customer.service.dto.CreateCustomerRequest;
import de.freshplan.domain.customer.service.dto.UpdateCustomerRequest;
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
 * Security tests for CustomerResource endpoints. Tests RBAC implementation and security
 * annotations.
 */
@QuarkusTest
@TestProfile(SecurityDisabledTestProfile.class)
class CustomerResourceSecurityTest {

  private static final String CUSTOMERS_BASE_PATH = "/api/customers";

  @Nested
  @DisplayName("Customer Creation Security Tests")
  class CustomerCreationSecurityTests {

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should be able to create customers")
    void adminShouldBeAbleToCreateCustomers() {
      CreateCustomerRequest request = createValidCustomerRequest();

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post(CUSTOMERS_BASE_PATH)
          .then()
          .statusCode(Response.Status.CREATED.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "manager",
        roles = {"manager"})
    @DisplayName("Manager should be able to create customers")
    void managerShouldBeAbleToCreateCustomers() {
      CreateCustomerRequest request = createValidCustomerRequest();

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post(CUSTOMERS_BASE_PATH)
          .then()
          .statusCode(Response.Status.CREATED.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "sales",
        roles = {"sales"})
    @DisplayName("Sales user should NOT be able to create customers")
    void salesShouldNotBeAbleToCreateCustomers() {
      CreateCustomerRequest request = createValidCustomerRequest();

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post(CUSTOMERS_BASE_PATH)
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "viewer",
        roles = {"viewer"})
    @DisplayName("Viewer should NOT be able to create customers")
    void viewerShouldNotBeAbleToCreateCustomers() {
      CreateCustomerRequest request = createValidCustomerRequest();

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post(CUSTOMERS_BASE_PATH)
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @DisplayName("Unauthenticated user should NOT be able to create customers")
    void unauthenticatedShouldNotBeAbleToCreateCustomers() {
      CreateCustomerRequest request = createValidCustomerRequest();

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post(CUSTOMERS_BASE_PATH)
          .then()
          .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }
  }

  @Nested
  @DisplayName("Customer Update Security Tests")
  class CustomerUpdateSecurityTests {

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should be able to update customers")
    void adminShouldBeAbleToUpdateCustomers() {
      UUID customerId = createTestCustomer();
      UpdateCustomerRequest request = createValidUpdateRequest();

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .put(CUSTOMERS_BASE_PATH + "/" + customerId)
          .then()
          .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "manager",
        roles = {"manager"})
    @DisplayName("Manager should be able to update customers")
    void managerShouldBeAbleToUpdateCustomers() {
      UUID customerId = createTestCustomer();
      UpdateCustomerRequest request = createValidUpdateRequest();

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .put(CUSTOMERS_BASE_PATH + "/" + customerId)
          .then()
          .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "sales",
        roles = {"sales"})
    @DisplayName("Sales user should NOT be able to update customers")
    void salesShouldNotBeAbleToUpdateCustomers() {
      UUID customerId = createTestCustomer();
      UpdateCustomerRequest request = createValidUpdateRequest();

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .put(CUSTOMERS_BASE_PATH + "/" + customerId)
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }
  }

  @Nested
  @DisplayName("Customer Deletion Security Tests")
  class CustomerDeletionSecurityTests {

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should be able to delete customers")
    void adminShouldBeAbleToDeleteCustomers() {
      UUID customerId = createTestCustomer();

      given()
          .when()
          .delete(CUSTOMERS_BASE_PATH + "/" + customerId)
          .then()
          .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "manager",
        roles = {"manager"})
    @DisplayName("Manager should NOT be able to delete customers")
    void managerShouldNotBeAbleToDeleteCustomers() {
      UUID customerId = createTestCustomer();

      given()
          .when()
          .delete(CUSTOMERS_BASE_PATH + "/" + customerId)
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "sales",
        roles = {"sales"})
    @DisplayName("Sales user should NOT be able to delete customers")
    void salesShouldNotBeAbleToDeleteCustomers() {
      UUID customerId = createTestCustomer();

      given()
          .when()
          .delete(CUSTOMERS_BASE_PATH + "/" + customerId)
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "viewer",
        roles = {"viewer"})
    @DisplayName("Viewer should NOT be able to delete customers")
    void viewerShouldNotBeAbleToDeleteCustomers() {
      UUID customerId = createTestCustomer();

      given()
          .when()
          .delete(CUSTOMERS_BASE_PATH + "/" + customerId)
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }
  }

  @Nested
  @DisplayName("Customer Read Access Tests")
  class CustomerReadAccessTests {

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should be able to read all customers")
    void adminShouldBeAbleToReadAllCustomers() {
      given().when().get(CUSTOMERS_BASE_PATH).then().statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "manager",
        roles = {"manager"})
    @DisplayName("Manager should be able to read all customers")
    void managerShouldBeAbleToReadAllCustomers() {
      given().when().get(CUSTOMERS_BASE_PATH).then().statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "sales",
        roles = {"sales"})
    @DisplayName("Sales user should be able to read all customers")
    void salesShouldBeAbleToReadAllCustomers() {
      given().when().get(CUSTOMERS_BASE_PATH).then().statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "viewer",
        roles = {"viewer"})
    @DisplayName("Viewer should NOT be able to access customer endpoints")
    void viewerShouldNotBeAbleToAccessCustomerEndpoints() {
      given()
          .when()
          .get(CUSTOMERS_BASE_PATH)
          .then()
          .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "sales",
        roles = {"sales"})
    @DisplayName("Sales user should be able to read individual customer")
    void salesShouldBeAbleToReadIndividualCustomer() {
      UUID customerId = createTestCustomer();

      given()
          .when()
          .get(CUSTOMERS_BASE_PATH + "/" + customerId)
          .then()
          .statusCode(Response.Status.OK.getStatusCode());
    }
  }

  @Nested
  @DisplayName("Customer Analytics Security Tests")
  class CustomerAnalyticsSecurityTests {

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should be able to access dashboard")
    void adminShouldBeAbleToAccessDashboard() {
      given()
          .when()
          .get(CUSTOMERS_BASE_PATH + "/dashboard")
          .then()
          .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "manager",
        roles = {"manager"})
    @DisplayName("Manager should be able to access dashboard")
    void managerShouldBeAbleToAccessDashboard() {
      given()
          .when()
          .get(CUSTOMERS_BASE_PATH + "/dashboard")
          .then()
          .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "sales",
        roles = {"sales"})
    @DisplayName("Sales user should be able to access dashboard")
    void salesShouldBeAbleToAccessDashboard() {
      given()
          .when()
          .get(CUSTOMERS_BASE_PATH + "/dashboard")
          .then()
          .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "sales",
        roles = {"sales"})
    @DisplayName("Sales user should be able to access risk assessment")
    void salesShouldBeAbleToAccessRiskAssessment() {
      given()
          .when()
          .get(CUSTOMERS_BASE_PATH + "/analytics/risk-assessment")
          .then()
          .statusCode(Response.Status.OK.getStatusCode());
    }
  }

  @Nested
  @DisplayName("Customer Utility Operations Security Tests")
  class CustomerUtilitySecurityTests {

    @Test
    @TestSecurity(
        user = "sales",
        roles = {"sales"})
    @DisplayName("Sales user should be able to check duplicates")
    void salesShouldBeAbleToCheckDuplicates() {
      given()
          .contentType(ContentType.JSON)
          .body("{\"companyName\": \"Test Company\"}")
          .when()
          .post(CUSTOMERS_BASE_PATH + "/check-duplicates")
          .then()
          .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Admin should be able to merge customers")
    void adminShouldBeAbleToMergeCustomers() {
      UUID targetId = createTestCustomer();
      UUID sourceId = createTestCustomer();

      given()
          .contentType(ContentType.JSON)
          .body("{\"sourceId\": \"" + sourceId + "\"}")
          .when()
          .post(CUSTOMERS_BASE_PATH + "/" + targetId + "/merge")
          .then()
          .statusCode(
              anyOf(
                  is(Response.Status.OK.getStatusCode()),
                  is(Response.Status.BAD_REQUEST.getStatusCode())));
    }

    @Test
    @TestSecurity(
        user = "sales",
        roles = {"sales"})
    @DisplayName("Sales user should be able to change customer status")
    void salesShouldBeAbleToChangeCustomerStatus() {
      UUID customerId = createTestCustomer();

      given()
          .contentType(ContentType.JSON)
          .body("{\"newStatus\": \"ACTIVE\"}")
          .when()
          .put(CUSTOMERS_BASE_PATH + "/" + customerId + "/status")
          .then()
          .statusCode(
              anyOf(
                  is(Response.Status.OK.getStatusCode()),
                  is(Response.Status.BAD_REQUEST.getStatusCode())));
    }
  }

  // Helper methods
  private CreateCustomerRequest createValidCustomerRequest() {
    return CreateCustomerRequest.builder()
        .companyName("Test Company Security " + UUID.randomUUID().toString().substring(0, 8))
        .customerType(de.freshplan.domain.customer.entity.CustomerType.NEUKUNDE)
        .build();
  }

  private UpdateCustomerRequest createValidUpdateRequest() {
    return UpdateCustomerRequest.builder()
        .companyName("Updated Test Company " + UUID.randomUUID().toString().substring(0, 8))
        .build();
  }

  /**
   * Returns a random UUID for security tests. Security tests focus on access control, not business
   * logic, so we don't need real customers in the database.
   */
  private UUID createTestCustomer() {
    // Create a real customer for testing
    CreateCustomerRequest request = createValidCustomerRequest();
    
    var response = given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post(CUSTOMERS_BASE_PATH)
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
