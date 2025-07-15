package de.freshplan.e2e;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.*;

/**
 * End-to-End tests for Keycloak integration.
 *
 * <p>These tests require a running Keycloak instance. Start it with: ./scripts/start-keycloak.sh
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@TestProfile(KeycloakE2ETest.E2ETestProfile.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("e2e")
public class KeycloakE2ETest {

  private static final String KEYCLOAK_URL = "http://localhost:8180";
  private static final String REALM = "freshplan";
  private static final String CLIENT_ID = "freshplan-backend";
  private static final String CLIENT_SECRET = "secret";

  private static boolean keycloakAvailable = false;

  @BeforeAll
  public static void checkKeycloakAvailability() {
    try {
      // Check if Keycloak is available by accessing the root endpoint
      Response response = given().get(KEYCLOAK_URL);

      keycloakAvailable = response.getStatusCode() == 200;

      if (!keycloakAvailable) {
        System.err.println(
            "⚠️  Keycloak is not running! Start it with: ./scripts/start-keycloak.sh");
        System.err.println(
            "Response: " + response.getStatusCode() + " - " + response.getBody().asString());
      }
    } catch (Exception e) {
      System.err.println("⚠️  Cannot connect to Keycloak at " + KEYCLOAK_URL);
      System.err.println("Error: " + e.getMessage());
      keycloakAvailable = false;
    }
  }

  @BeforeEach
  public void skipIfKeycloakNotAvailable() {
    Assumptions.assumeTrue(keycloakAvailable, "Keycloak is not available. Skipping E2E tests.");
  }

  @Test
  @Order(1)
  @DisplayName("Should obtain access token for admin user")
  void testAdminLogin() {
    // Given
    String username = "admin";
    String password = "admin123";

    // When - Get access token from Keycloak
    Response tokenResponse =
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "password")
            .formParam("client_id", CLIENT_ID)
            .formParam("client_secret", CLIENT_SECRET)
            .formParam("username", username)
            .formParam("password", password)
            .when()
            .post(KEYCLOAK_URL + "/realms/" + REALM + "/protocol/openid-connect/token");

    // Then
    assertThat(tokenResponse.getStatusCode()).isEqualTo(200);

    String accessToken = tokenResponse.jsonPath().getString("access_token");
    assertThat(accessToken).isNotNull().isNotEmpty();

    // Verify token contains expected claims
    Map<String, Object> tokenBody = decodeJwtBody(accessToken);
    assertThat(tokenBody.get("preferred_username")).isEqualTo(username);
    assertThat(tokenBody.get("email")).isEqualTo("admin@freshplan.de");

    // Verify realm roles
    Map<String, Object> realmAccess = (Map<String, Object>) tokenBody.get("realm_access");
    assertThat(realmAccess).isNotNull();
    assertThat((java.util.List<String>) realmAccess.get("roles"))
        .contains("admin", "manager", "sales"); // Admin has all roles
  }

  @Test
  @Order(2)
  @DisplayName("Should obtain access token for manager user")
  void testManagerLogin() {
    // Given
    String username = "manager";
    String password = "manager123";

    // When
    Response tokenResponse =
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "password")
            .formParam("client_id", CLIENT_ID)
            .formParam("client_secret", CLIENT_SECRET)
            .formParam("username", username)
            .formParam("password", password)
            .when()
            .post(KEYCLOAK_URL + "/realms/" + REALM + "/protocol/openid-connect/token");

    // Then
    assertThat(tokenResponse.getStatusCode()).isEqualTo(200);

    String accessToken = tokenResponse.jsonPath().getString("access_token");
    assertThat(accessToken).isNotNull().isNotEmpty();

    // Verify roles
    Map<String, Object> tokenBody = decodeJwtBody(accessToken);
    Map<String, Object> realmAccess = (Map<String, Object>) tokenBody.get("realm_access");
    assertThat((java.util.List<String>) realmAccess.get("roles"))
        .contains("manager")
        .doesNotContain("admin", "sales");
  }

  @Test
  @Order(3)
  @DisplayName("Should obtain access token for sales user")
  void testSalesLogin() {
    // Given
    String username = "sales";
    String password = "sales123";

    // When
    Response tokenResponse =
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "password")
            .formParam("client_id", CLIENT_ID)
            .formParam("client_secret", CLIENT_SECRET)
            .formParam("username", username)
            .formParam("password", password)
            .when()
            .post(KEYCLOAK_URL + "/realms/" + REALM + "/protocol/openid-connect/token");

    // Then
    assertThat(tokenResponse.getStatusCode()).isEqualTo(200);

    String accessToken = tokenResponse.jsonPath().getString("access_token");
    assertThat(accessToken).isNotNull().isNotEmpty();

    // Verify roles
    Map<String, Object> tokenBody = decodeJwtBody(accessToken);
    Map<String, Object> realmAccess = (Map<String, Object>) tokenBody.get("realm_access");
    assertThat((java.util.List<String>) realmAccess.get("roles"))
        .contains("sales")
        .doesNotContain("admin", "manager");
  }

  @Test
  @Order(4)
  @DisplayName("Should reject invalid credentials")
  void testInvalidLogin() {
    // When
    Response tokenResponse =
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "password")
            .formParam("client_id", CLIENT_ID)
            .formParam("client_secret", CLIENT_SECRET)
            .formParam("username", "invalid")
            .formParam("password", "wrong")
            .when()
            .post(KEYCLOAK_URL + "/realms/" + REALM + "/protocol/openid-connect/token");

    // Then
    assertThat(tokenResponse.getStatusCode()).isEqualTo(401);
    assertThat(tokenResponse.jsonPath().getString("error")).isEqualTo("invalid_grant");
  }

  @Test
  @Order(5)
  @DisplayName("Should access protected API endpoint with valid token")
  void testProtectedApiAccess() {
    // Given - Get admin token
    String accessToken = getAccessToken("admin", "admin123");

    // When - Access protected endpoint
    Response apiResponse =
        given().header("Authorization", "Bearer " + accessToken).when().get("/api/users");

    // Then
    assertThat(apiResponse.getStatusCode()).isEqualTo(200);
    assertThat(apiResponse.jsonPath().getList("$")).isNotEmpty();
  }

  @Test
  @Order(6)
  @DisplayName("Should reject API access without token")
  void testUnauthorizedApiAccess() {
    // When
    Response apiResponse = given().when().get("/api/users");

    // Then
    assertThat(apiResponse.getStatusCode()).isEqualTo(401);
  }

  @Test
  @Order(7)
  @DisplayName("Should respect role-based access control")
  void testRoleBasedAccess() {
    // Given - Sales user should not access user management
    String salesToken = getAccessToken("sales", "sales123");

    // When
    Response apiResponse =
        given()
            .header("Authorization", "Bearer " + salesToken)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "username": "newuser",
                    "firstName": "New",
                    "lastName": "User",
                    "email": "new@example.com",
                    "password": "password123",
                    "roles": ["sales"]
                }
                """)
            .when()
            .post("/api/users");

    // Then - Should be forbidden (sales can't create users)
    assertThat(apiResponse.getStatusCode()).isEqualTo(403);
  }

  @Test
  @Order(8)
  @DisplayName("Should refresh access token")
  void testTokenRefresh() {
    // Given - Get initial token with refresh token
    Response initialResponse =
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "password")
            .formParam("client_id", CLIENT_ID)
            .formParam("client_secret", CLIENT_SECRET)
            .formParam("username", "admin")
            .formParam("password", "admin123")
            .when()
            .post(KEYCLOAK_URL + "/realms/" + REALM + "/protocol/openid-connect/token");

    String refreshToken = initialResponse.jsonPath().getString("refresh_token");
    assertThat(refreshToken).isNotNull();

    // When - Use refresh token
    Response refreshResponse =
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "refresh_token")
            .formParam("client_id", CLIENT_ID)
            .formParam("client_secret", CLIENT_SECRET)
            .formParam("refresh_token", refreshToken)
            .when()
            .post(KEYCLOAK_URL + "/realms/" + REALM + "/protocol/openid-connect/token");

    // Then
    assertThat(refreshResponse.getStatusCode()).isEqualTo(200);
    String newAccessToken = refreshResponse.jsonPath().getString("access_token");
    assertThat(newAccessToken).isNotNull().isNotEmpty();
  }

  /** Helper method to get access token for a user */
  private String getAccessToken(String username, String password) {
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

    assertThat(response.getStatusCode()).isEqualTo(200);
    return response.jsonPath().getString("access_token");
  }

  /** Simple JWT decoder to extract claims */
  private Map<String, Object> decodeJwtBody(String jwt) {
    String[] parts = jwt.split("\\.");
    if (parts.length != 3) {
      throw new IllegalArgumentException("Invalid JWT format");
    }

    String payload = parts[1];
    byte[] decodedBytes = java.util.Base64.getUrlDecoder().decode(payload);
    String decodedString = new String(decodedBytes);

    // Parse JSON using RestAssured's JSON parser
    return io.restassured.path.json.JsonPath.from(decodedString).getMap("$");
  }

  /** Test profile for E2E tests with enabled OIDC */
  public static class E2ETestProfile implements QuarkusTestProfile {
    @Override
    public String getConfigProfile() {
      return "e2e";
    }
  }
}
