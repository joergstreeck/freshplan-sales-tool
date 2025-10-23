package de.freshplan.modules.xentral.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import de.freshplan.modules.xentral.dto.XentralSettingsDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

/**
 * Simplified Integration Tests for XentralSettingsResource.
 *
 * <p>Sprint 2.1.7.2 - D5: Enterprise-Grade Test Coverage (Pragmatic Approach)
 *
 * <p><b>Focus on Critical Bugs:</b>
 *
 * <ul>
 *   <li>Security: ADMIN-only access
 *   <li>Validation: Input rejection (400 status)
 *   <li>Happy Path: GET and PUT work correctly
 *   <li>Connection Test: Returns success in mock mode
 * </ul>
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.2
 */
@QuarkusTest
class XentralSettingsResourceSimpleTest {

  private static final String ADMIN_API_PATH = "/api/admin/xentral/settings";
  private static final String TEST_CONNECTION_PATH = "/api/admin/xentral/test-connection";

  private static final String VALID_URL = "https://test.xentral.biz";
  private static final String VALID_TOKEN = "test-token-12345";

  // ============================================================================
  // Security Tests - CRITICAL!
  // ============================================================================

  @Test
  @DisplayName("GET /settings - NO AUTH → 401")
  void testGetSettings_NoAuth_Returns401() {
    given().when().get(ADMIN_API_PATH).then().statusCode(401);
  }

  @Test
  @DisplayName("GET /settings - USER role → 403 FORBIDDEN")
  @TestSecurity(user = "user", roles = "user")
  void testGetSettings_UserRole_Returns403() {
    given().when().get(ADMIN_API_PATH).then().statusCode(403);
  }

  @Test
  @DisplayName("PUT /settings - USER role → 403 FORBIDDEN")
  @TestSecurity(user = "user", roles = "user")
  void testUpdateSettings_UserRole_Returns403() {
    XentralSettingsDTO dto = new XentralSettingsDTO(VALID_URL, VALID_TOKEN, true);

    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .put(ADMIN_API_PATH)
        .then()
        .statusCode(403);
  }

  @Test
  @DisplayName("GET /test-connection - USER role → 403 FORBIDDEN")
  @TestSecurity(user = "user", roles = "user")
  void testConnectionTest_UserRole_Returns403() {
    given().when().get(TEST_CONNECTION_PATH).then().statusCode(403);
  }

  // ============================================================================
  // Happy Path Tests
  // ============================================================================

  @Test
  @DisplayName("GET /settings - ADMIN → 200 OK (returns data)")
  @TestSecurity(user = "admin", roles = "ADMIN")
  void testGetSettings_AdminRole_Returns200() {
    given()
        .when()
        .get(ADMIN_API_PATH)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("apiUrl", notNullValue())
        .body("apiToken", notNullValue())
        .body("mockMode", notNullValue());
  }

  @Test
  @DisplayName("PUT /settings - Valid DTO → 200 OK")
  @TestSecurity(user = "admin", roles = "ADMIN")
  void testUpdateSettings_ValidDto_Returns200() {
    XentralSettingsDTO dto = new XentralSettingsDTO(VALID_URL, VALID_TOKEN, true);

    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .put(ADMIN_API_PATH)
        .then()
        .statusCode(200)
        .body("apiUrl", equalTo(VALID_URL))
        .body("apiToken", equalTo(VALID_TOKEN))
        .body("mockMode", equalTo(true));
  }

  @Test
  @DisplayName("GET /test-connection - Mock Mode → SUCCESS")
  @TestSecurity(user = "admin", roles = "ADMIN")
  void testConnectionTest_MockMode_ReturnsSuccess() {
    given()
        .when()
        .get(TEST_CONNECTION_PATH)
        .then()
        .statusCode(200)
        .body("status", equalTo("success")) // lowercase as per ConnectionTestResponse.success()
        .body("message", containsString("Verbindung erfolgreich"));
  }

  // ============================================================================
  // Validation Tests - Input Rejection
  // ============================================================================

  @Test
  @DisplayName("PUT /settings - NULL apiUrl → 400 BAD_REQUEST")
  @TestSecurity(user = "admin", roles = "ADMIN")
  void testUpdateSettings_NullApiUrl_Returns400() {
    XentralSettingsDTO dto = new XentralSettingsDTO(null, VALID_TOKEN, true);

    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .put(ADMIN_API_PATH)
        .then()
        .statusCode(400); // Validation fails
  }

  @Test
  @DisplayName("PUT /settings - EMPTY apiUrl → 400 BAD_REQUEST")
  @TestSecurity(user = "admin", roles = "ADMIN")
  void testUpdateSettings_EmptyApiUrl_Returns400() {
    XentralSettingsDTO dto = new XentralSettingsDTO("", VALID_TOKEN, true);

    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .put(ADMIN_API_PATH)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("PUT /settings - Invalid URL (no protocol) → 400 BAD_REQUEST")
  @TestSecurity(user = "admin", roles = "ADMIN")
  void testUpdateSettings_InvalidUrl_Returns400() {
    XentralSettingsDTO dto = new XentralSettingsDTO("test.xentral.biz", VALID_TOKEN, true);

    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .put(ADMIN_API_PATH)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("PUT /settings - NULL apiToken → 400 BAD_REQUEST")
  @TestSecurity(user = "admin", roles = "ADMIN")
  void testUpdateSettings_NullApiToken_Returns400() {
    XentralSettingsDTO dto = new XentralSettingsDTO(VALID_URL, null, true);

    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .put(ADMIN_API_PATH)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("PUT /settings - EMPTY apiToken → 400 BAD_REQUEST")
  @TestSecurity(user = "admin", roles = "ADMIN")
  void testUpdateSettings_EmptyApiToken_Returns400() {
    XentralSettingsDTO dto = new XentralSettingsDTO(VALID_URL, "", true);

    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .put(ADMIN_API_PATH)
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("PUT /settings - NULL mockMode → 400 BAD_REQUEST")
  @TestSecurity(user = "admin", roles = "ADMIN")
  void testUpdateSettings_NullMockMode_Returns400() {
    XentralSettingsDTO dto = new XentralSettingsDTO(VALID_URL, VALID_TOKEN, null);

    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .put(ADMIN_API_PATH)
        .then()
        .statusCode(400);
  }
}
