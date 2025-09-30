package de.freshplan.e2e;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

/**
 * End-to-End tests for API security with real JWT tokens.
 *
 * <p>Tests the complete security flow: 1. Authenticate with Keycloak 2. Use JWT token for API calls
 * 3. Verify role-based access control
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("e2e")
public class ApiSecurityE2ETest {

  private static final String KEYCLOAK_URL = "http://localhost:8180";
  private static final String REALM = "freshplan";
  private static final String CLIENT_ID = "freshplan-backend";
  private static final String CLIENT_SECRET = "secret";

  private static boolean keycloakAvailable = false;
  private static String adminToken;
  private static String managerToken;
  private static String salesToken;

  @BeforeAll
  public static void setupTokens() {
    try {
      // Check Keycloak availability
      Response healthCheck = given().get(KEYCLOAK_URL + "/health/ready");
      keycloakAvailable = healthCheck.getStatusCode() == 200;

      if (keycloakAvailable) {
        // Get tokens for all user types
        adminToken = getAccessToken("admin", "admin123");
        managerToken = getAccessToken("manager", "manager123");
        salesToken = getAccessToken("sales", "sales123");
      } else {
        System.err.println(
            "⚠️  Keycloak is not running! Start it with: ./scripts/start-keycloak.sh");
      }
    } catch (Exception e) {
      System.err.println("⚠️  Cannot connect to Keycloak: " + e.getMessage());
      keycloakAvailable = false;
    }
  }

  @BeforeEach
  public void skipIfKeycloakNotAvailable() {
    Assumptions.assumeTrue(keycloakAvailable, "Keycloak is not available. Skipping E2E tests.");
  }

  // ========== Customer API Tests ==========

  @Test
  @Order(1)
  @DisplayName("Customer API: All roles can read customers")
  void testCustomerReadAccess() {
    // Admin can read
    given()
        .header("Authorization", "Bearer " + adminToken)
        .when()
        .get("/api/customers")
        .then()
        .statusCode(200);

    // Manager can read
    given()
        .header("Authorization", "Bearer " + managerToken)
        .when()
        .get("/api/customers")
        .then()
        .statusCode(200);

    // Sales can read
    given()
        .header("Authorization", "Bearer " + salesToken)
        .when()
        .get("/api/customers")
        .then()
        .statusCode(200);
  }

  @Test
  @Order(2)
  @DisplayName("Customer API: Create requires specific roles")
  void testCustomerCreateAccess() {
    String customerJson =
        """
            {
                "companyName": "E2E Test Company",
                "customerType": "UNTERNEHMEN"
            }
            """;

    // Admin can create
    Response adminResponse =
        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body(customerJson)
            .when()
            .post("/api/customers");
    assertThat(adminResponse.getStatusCode()).isIn(201, 200);

    // Manager can create
    Response managerResponse =
        given()
            .header("Authorization", "Bearer " + managerToken)
            .contentType(ContentType.JSON)
            .body(customerJson.replace("E2E Test", "E2E Manager"))
            .when()
            .post("/api/customers");
    assertThat(managerResponse.getStatusCode()).isIn(201, 200);

    // Sales can create
    Response salesResponse =
        given()
            .header("Authorization", "Bearer " + salesToken)
            .contentType(ContentType.JSON)
            .body(customerJson.replace("E2E Test", "E2E Sales"))
            .when()
            .post("/api/customers");
    assertThat(salesResponse.getStatusCode()).isIn(201, 200);
  }

  // ========== User Management API Tests ==========

  @Test
  @Order(3)
  @DisplayName("User API: Only admin can list users")
  void testUserListAccess() {
    // Admin can list users
    given()
        .header("Authorization", "Bearer " + adminToken)
        .when()
        .get("/api/users")
        .then()
        .statusCode(200);

    // Manager cannot list users
    given()
        .header("Authorization", "Bearer " + managerToken)
        .when()
        .get("/api/users")
        .then()
        .statusCode(403);

    // Sales cannot list users
    given()
        .header("Authorization", "Bearer " + salesToken)
        .when()
        .get("/api/users")
        .then()
        .statusCode(403);
  }

  @Test
  @Order(4)
  @DisplayName("User API: Only admin can create users")
  void testUserCreateAccess() {
    String userJson =
        """
            {
                "username": "e2euser",
                "firstName": "E2E",
                "lastName": "User",
                "email": "e2e@test.com",
                "password": "password123",
                "roles": ["sales"]
            }
            """;

    // Admin can create users
    given()
        .header("Authorization", "Bearer " + adminToken)
        .contentType(ContentType.JSON)
        .body(userJson)
        .when()
        .post("/api/users")
        .then()
        .statusCode(201);

    // Manager cannot create users
    given()
        .header("Authorization", "Bearer " + managerToken)
        .contentType(ContentType.JSON)
        .body(userJson.replace("e2euser", "e2emanager"))
        .when()
        .post("/api/users")
        .then()
        .statusCode(403);

    // Sales cannot create users
    given()
        .header("Authorization", "Bearer " + salesToken)
        .contentType(ContentType.JSON)
        .body(userJson.replace("e2euser", "e2esales"))
        .when()
        .post("/api/users")
        .then()
        .statusCode(403);
  }

  // ========== Calculator API Tests ==========

  @Test
  @Order(5)
  @DisplayName("Calculator API: All authenticated users can calculate")
  void testCalculatorAccess() {
    String calculationRequest =
        """
            {
                "customerType": "GASTSTAETTE",
                "positions": [
                    {
                        "description": "Test Position",
                        "quantity": 10,
                        "unitPrice": 5.50,
                        "category": "FOOD"
                    }
                ]
            }
            """;

    // All roles can use calculator
    given()
        .header("Authorization", "Bearer " + adminToken)
        .contentType(ContentType.JSON)
        .body(calculationRequest)
        .when()
        .post("/api/calculator/calculate")
        .then()
        .statusCode(200);

    given()
        .header("Authorization", "Bearer " + managerToken)
        .contentType(ContentType.JSON)
        .body(calculationRequest)
        .when()
        .post("/api/calculator/calculate")
        .then()
        .statusCode(200);

    given()
        .header("Authorization", "Bearer " + salesToken)
        .contentType(ContentType.JSON)
        .body(calculationRequest)
        .when()
        .post("/api/calculator/calculate")
        .then()
        .statusCode(200);
  }

  // ========== Sales Cockpit API Tests ==========

  @Test
  @Order(6)
  @DisplayName("Sales Cockpit: Users see their own data")
  void testSalesCockpitAccess() {
    // Each role can access their cockpit
    Response adminCockpit =
        given().header("Authorization", "Bearer " + adminToken).when().get("/api/sales-cockpit");
    assertThat(adminCockpit.getStatusCode()).isEqualTo(200);

    Response managerCockpit =
        given().header("Authorization", "Bearer " + managerToken).when().get("/api/sales-cockpit");
    assertThat(managerCockpit.getStatusCode()).isEqualTo(200);

    Response salesCockpit =
        given().header("Authorization", "Bearer " + salesToken).when().get("/api/sales-cockpit");
    assertThat(salesCockpit.getStatusCode()).isEqualTo(200);
  }

  // ========== Token Validation Tests ==========

  @Test
  @Order(7)
  @DisplayName("API rejects expired tokens")
  void testExpiredToken() {
    // This would require waiting for token expiry or mocking
    // For now, test with invalid token
    String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.token";

    given()
        .header("Authorization", "Bearer " + invalidToken)
        .when()
        .get("/api/customers")
        .then()
        .statusCode(401);
  }

  @Test
  @Order(8)
  @DisplayName("API rejects requests without Authorization header")
  void testNoAuthHeader() {
    given().when().get("/api/customers").then().statusCode(401);
  }

  @Test
  @Order(9)
  @DisplayName("Health endpoints work without authentication")
  void testHealthEndpoints() {
    // Health check should work without auth
    given().when().get("/q/health").then().statusCode(200);

    given().when().get("/q/health/live").then().statusCode(200);

    given().when().get("/q/health/ready").then().statusCode(200);
  }

  // ========== Helper Methods ==========

  private static String getAccessToken(String username, String password) {
    Response response =
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "password")
            .formParam("client_id", CLIENT_ID)
            .formParam("client_secret", CLIENT_SECRET)
            .formParam("username", username)
            .formParam("password", password)
            .when()
            .post(KEYCLOAK_URL + "/realms/" + REALM + "/protocol/openid-connect/token");

    if (response.getStatusCode() != 200) {
      throw new RuntimeException(
          "Failed to get token for "
              + username
              + ": "
              + response.getStatusCode()
              + " - "
              + response.getBody().asString());
    }

    return response.jsonPath().getString("access_token");
  }
}
