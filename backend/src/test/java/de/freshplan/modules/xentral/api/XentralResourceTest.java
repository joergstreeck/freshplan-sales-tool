package de.freshplan.modules.xentral.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration Tests for XentralResource (Main Xentral API).
 *
 * <p>Sprint 2.1.7.2 - Enterprise-Grade Test Coverage
 *
 * <p><b>API Endpoints Tested:</b>
 *
 * <ul>
 *   <li>GET /api/xentral/customers?salesRepId={id}
 *   <li>GET /api/xentral/customers/{xentralId}
 *   <li>GET /api/xentral/invoices?customerId={id}
 *   <li>GET /api/xentral/employees/sales-reps
 * </ul>
 *
 * <p><b>Test Strategy:</b>
 *
 * <ul>
 *   <li>Security: user role required (not admin!)
 *   <li>Validation: Required query parameters
 *   <li>404 Handling: Non-existent resources
 *   <li>Mock Mode: Returns realistic test data
 * </ul>
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.2
 */
@QuarkusTest
class XentralResourceTest {

  // ============================================================================
  // GET /customers?salesRepId={id}
  // ============================================================================

  @Test
  @DisplayName("GET /customers - Valid salesRepId → 200 OK with list")
  @TestSecurity(user = "user", roles = "user")
  void testGetCustomers_ValidSalesRepId_Returns200WithList() {
    given()
        .queryParam("salesRepId", "SALES-REP-001") // Mock uses "SALES-REP-001"
        .when()
        .get("/api/xentral/customers")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("$", not(empty())) // Mock should return data
        .body("[0].xentralId", notNullValue())
        .body("[0].companyName", notNullValue());
  }

  @Test
  @DisplayName("GET /customers - Missing salesRepId → 400 BAD_REQUEST")
  @TestSecurity(user = "user", roles = "user")
  void testGetCustomers_MissingSalesRepId_Returns400() {
    given()
        .when()
        .get("/api/xentral/customers")
        .then()
        .statusCode(400)
        .body("error", containsString("Missing required parameter: salesRepId"));
  }

  @Test
  @DisplayName("GET /customers - Empty salesRepId → 400 BAD_REQUEST")
  @TestSecurity(user = "user", roles = "user")
  void testGetCustomers_EmptySalesRepId_Returns400() {
    given()
        .queryParam("salesRepId", "")
        .when()
        .get("/api/xentral/customers")
        .then()
        .statusCode(400)
        .body("error", containsString("Missing required parameter: salesRepId"));
  }

  @Test
  @DisplayName("GET /customers - NO AUTH → 401 UNAUTHORIZED")
  void testGetCustomers_NoAuth_Returns401() {
    given()
        .queryParam("salesRepId", "SALES-REP-001")
        .when()
        .get("/api/xentral/customers")
        .then()
        .statusCode(401);
  }

  // ============================================================================
  // GET /customers/{xentralId}
  // ============================================================================

  @Test
  @DisplayName("GET /customers/{id} - Existing customer → 200 OK")
  @TestSecurity(user = "user", roles = "user")
  void testGetCustomerById_ExistingCustomer_Returns200() {
    given()
        .pathParam("xentralId", "XENT-001") // Mock uses "XENT-001"
        .when()
        .get("/api/xentral/customers/{xentralId}")
        .then()
        .statusCode(200)
        .body("xentralId", equalTo("XENT-001"))
        .body("companyName", notNullValue())
        .body("totalRevenue", notNullValue()); // Financial data
  }

  @Test
  @DisplayName("GET /customers/{id} - Non-existent customer → 404 NOT_FOUND")
  @TestSecurity(user = "user", roles = "user")
  void testGetCustomerById_NonExistentCustomer_Returns404() {
    given()
        .pathParam("xentralId", "NONEXISTENT999")
        .when()
        .get("/api/xentral/customers/{xentralId}")
        .then()
        .statusCode(404)
        .body("error", containsString("Customer not found"));
  }

  @Test
  @DisplayName("GET /customers/{id} - NO AUTH → 401 UNAUTHORIZED")
  void testGetCustomerById_NoAuth_Returns401() {
    given()
        .pathParam("xentralId", "XENT-001")
        .when()
        .get("/api/xentral/customers/{xentralId}")
        .then()
        .statusCode(401);
  }

  // ============================================================================
  // GET /invoices?customerId={id}
  // ============================================================================

  @Test
  @DisplayName("GET /invoices - Valid customerId → 200 OK with list")
  @TestSecurity(user = "user", roles = "user")
  void testGetInvoices_ValidCustomerId_Returns200WithList() {
    given()
        .queryParam("customerId", "XENT-001") // Mock uses "XENT-001"
        .when()
        .get("/api/xentral/invoices")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("$", not(empty())) // Mock should return invoices
        .body("[0].invoiceId", notNullValue())
        .body("[0].amount", notNullValue());
  }

  @Test
  @DisplayName("GET /invoices - Missing customerId → 400 BAD_REQUEST")
  @TestSecurity(user = "user", roles = "user")
  void testGetInvoices_MissingCustomerId_Returns400() {
    given()
        .when()
        .get("/api/xentral/invoices")
        .then()
        .statusCode(400)
        .body("error", containsString("Missing required parameter: customerId"));
  }

  @Test
  @DisplayName("GET /invoices - Empty customerId → 400 BAD_REQUEST")
  @TestSecurity(user = "user", roles = "user")
  void testGetInvoices_EmptyCustomerId_Returns400() {
    given()
        .queryParam("customerId", "")
        .when()
        .get("/api/xentral/invoices")
        .then()
        .statusCode(400)
        .body("error", containsString("Missing required parameter: customerId"));
  }

  @Test
  @DisplayName("GET /invoices - NO AUTH → 401 UNAUTHORIZED")
  void testGetInvoices_NoAuth_Returns401() {
    given()
        .queryParam("customerId", "XENT-001")
        .when()
        .get("/api/xentral/invoices")
        .then()
        .statusCode(401);
  }

  // ============================================================================
  // GET /employees/sales-reps
  // ============================================================================

  @Test
  @DisplayName("GET /employees/sales-reps → 200 OK with list")
  @TestSecurity(user = "user", roles = "user")
  void testGetSalesReps_Returns200WithList() {
    given()
        .when()
        .get("/api/xentral/employees/sales-reps")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("$", not(empty())) // Mock should return sales reps
        .body("[0].employeeId", notNullValue())
        .body("[0].firstName", notNullValue())
        .body("[0].email", notNullValue());
  }

  @Test
  @DisplayName("GET /employees/sales-reps - NO AUTH → 401 UNAUTHORIZED")
  void testGetSalesReps_NoAuth_Returns401() {
    given().when().get("/api/xentral/employees/sales-reps").then().statusCode(401);
  }

  // ============================================================================
  // Security Tests - Critical!
  // ============================================================================

  @Test
  @DisplayName("All endpoints require authentication (401 without auth)")
  void testAllEndpoints_NoAuth_Returns401() {
    // GET /customers
    given()
        .queryParam("salesRepId", "SALES-REP-001")
        .when()
        .get("/api/xentral/customers")
        .then()
        .statusCode(401);

    // GET /customers/{id}
    given()
        .pathParam("xentralId", "XENT-001")
        .when()
        .get("/api/xentral/customers/{xentralId}")
        .then()
        .statusCode(401);

    // GET /invoices
    given()
        .queryParam("customerId", "XENT-001")
        .when()
        .get("/api/xentral/invoices")
        .then()
        .statusCode(401);

    // GET /employees/sales-reps
    given().when().get("/api/xentral/employees/sales-reps").then().statusCode(401);
  }
}
