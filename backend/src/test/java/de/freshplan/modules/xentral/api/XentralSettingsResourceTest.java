package de.freshplan.modules.xentral.api;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

import de.freshplan.modules.xentral.dto.XentralSettingsDTO;
import de.freshplan.modules.xentral.entity.XentralSettings;
import de.freshplan.modules.xentral.repository.XentralSettingsRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.TestTransaction;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;

/**
 * Integration Tests for XentralSettingsResource (Admin API).
 *
 * <p>Sprint 2.1.7.2 - D5: Enterprise-Grade Test Coverage
 *
 * <p><b>Test Strategy: Bug Detection</b>
 *
 * <ul>
 *   <li>Edge Cases: null, empty, invalid input
 *   <li>UPSERT Pattern: create vs. update behavior
 *   <li>Security: ADMIN vs. USER authorization
 *   <li>Validation: DTO constraint enforcement
 *   <li>Fallback: DB empty → application.properties defaults
 *   <li>Connection Test: Mock vs. Real API behavior
 * </ul>
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.2
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class XentralSettingsResourceTest {

  @Inject XentralSettingsRepository repository;

  private static final String ADMIN_API_PATH = "/api/admin/xentral/settings";
  private static final String TEST_CONNECTION_PATH = "/api/admin/xentral/test-connection";

  // Valid Test Data
  private static final String VALID_URL = "https://test.xentral.biz";
  private static final String VALID_TOKEN = "test-token-12345";

  // Invalid Test Data
  private static final String INVALID_URL_NO_PROTOCOL = "test.xentral.biz";
  private static final String INVALID_URL_FTP = "ftp://test.xentral.biz";

  /**
   * Clean up database before each test to ensure isolated test runs.
   *
   * <p>Critical: Tests must not depend on each other's state!
   */
  @BeforeEach
  @TestTransaction
  void cleanDatabase() {
    XentralSettings.deleteAll();
  }

  // ============================================================================
  // TEST GROUP 1: GET /settings - Fallback and DB Behavior
  // ============================================================================

  @Test
  @Order(1)
  @DisplayName("GET /settings - DB empty → returns application.properties defaults")
  @TestSecurity(user = "admin", roles = "ADMIN")
  void testGetSettings_DatabaseEmpty_ReturnsDefaults() {
    // Given: Database is empty (ensured by @BeforeEach)
    assertThat(repository.count()).isZero();

    // When: GET /settings
    given()
        .when()
        .get(ADMIN_API_PATH)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("apiUrl", notNullValue()) // application.properties default
        .body("apiToken", notNullValue()) // application.properties default
        .body("mockMode", equalTo(true)); // application.properties default
  }

  @Test
  @Order(2)
  @DisplayName("GET /settings - DB has data → returns DB values (not defaults)")
  @TestSecurity(user = "admin", roles = "ADMIN")
  @TestTransaction
  void testGetSettings_DatabaseHasData_ReturnsDbValues() {
    // Given: Settings exist in database
    XentralSettings settings = new XentralSettings();
    settings.setApiUrl("https://db.xentral.biz");
    settings.setApiToken("db-token-67890");
    settings.setMockMode(false);
    repository.persist(settings);

    // When: GET /settings
    given()
        .when()
        .get(ADMIN_API_PATH)
        .then()
        .statusCode(200)
        .body("apiUrl", equalTo("https://db.xentral.biz"))
        .body("apiToken", equalTo("db-token-67890"))
        .body("mockMode", equalTo(false));
  }

  // ============================================================================
  // TEST GROUP 2: PUT /settings - Validation (Bug Detection!)
  // ============================================================================

  @Test
  @Order(10)
  @DisplayName("PUT /settings - NULL apiUrl → 400 BAD_REQUEST")
  @TestSecurity(user = "admin", roles = "ADMIN")
  void testUpdateSettings_NullApiUrl_Returns400() {
    // Given: DTO with null apiUrl
    XentralSettingsDTO dto = new XentralSettingsDTO(null, VALID_TOKEN, true);

    // When: PUT /settings
    // Then: 400 BAD_REQUEST (Validation Error)
    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .put(ADMIN_API_PATH)
        .then()
        .statusCode(400)
        .body("error", containsString("API URL darf nicht leer sein"));
  }

  @Test
  @Order(11)
  @DisplayName("PUT /settings - EMPTY apiUrl → 400 BAD_REQUEST")
  @TestSecurity(user = "admin", roles = "ADMIN")
  void testUpdateSettings_EmptyApiUrl_Returns400() {
    // Given: DTO with empty apiUrl
    XentralSettingsDTO dto = new XentralSettingsDTO("", VALID_TOKEN, true);

    // When: PUT /settings
    // Then: 400 BAD_REQUEST
    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .put(ADMIN_API_PATH)
        .then()
        .statusCode(400)
        .body("error", containsString("API URL darf nicht leer sein"));
  }

  @Test
  @Order(12)
  @DisplayName("PUT /settings - apiUrl without http(s):// → 400 BAD_REQUEST")
  @TestSecurity(user = "admin", roles = "ADMIN")
  void testUpdateSettings_InvalidUrlNoProtocol_Returns400() {
    // Given: DTO with invalid URL (no protocol)
    XentralSettingsDTO dto = new XentralSettingsDTO(INVALID_URL_NO_PROTOCOL, VALID_TOKEN, true);

    // When: PUT /settings
    // Then: 400 BAD_REQUEST
    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .put(ADMIN_API_PATH)
        .then()
        .statusCode(400)
        .body("error", containsString("http:// oder https://"));
  }

  @Test
  @Order(13)
  @DisplayName("PUT /settings - apiUrl with ftp:// → 400 BAD_REQUEST")
  @TestSecurity(user = "admin", roles = "ADMIN")
  void testUpdateSettings_InvalidUrlFtpProtocol_Returns400() {
    // Given: DTO with invalid URL (ftp protocol)
    XentralSettingsDTO dto = new XentralSettingsDTO(INVALID_URL_FTP, VALID_TOKEN, true);

    // When: PUT /settings
    // Then: 400 BAD_REQUEST
    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .put(ADMIN_API_PATH)
        .then()
        .statusCode(400)
        .body("error", containsString("http:// oder https://"));
  }

  @Test
  @Order(14)
  @DisplayName("PUT /settings - NULL apiToken → 400 BAD_REQUEST")
  @TestSecurity(user = "admin", roles = "ADMIN")
  void testUpdateSettings_NullApiToken_Returns400() {
    // Given: DTO with null apiToken
    XentralSettingsDTO dto = new XentralSettingsDTO(VALID_URL, null, true);

    // When: PUT /settings
    // Then: 400 BAD_REQUEST
    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .put(ADMIN_API_PATH)
        .then()
        .statusCode(400)
        .body("error", containsString("API Token darf nicht leer sein"));
  }

  @Test
  @Order(15)
  @DisplayName("PUT /settings - EMPTY apiToken → 400 BAD_REQUEST")
  @TestSecurity(user = "admin", roles = "ADMIN")
  void testUpdateSettings_EmptyApiToken_Returns400() {
    // Given: DTO with empty apiToken
    XentralSettingsDTO dto = new XentralSettingsDTO(VALID_URL, "", true);

    // When: PUT /settings
    // Then: 400 BAD_REQUEST
    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .put(ADMIN_API_PATH)
        .then()
        .statusCode(400)
        .body("error", containsString("API Token darf nicht leer sein"));
  }

  @Test
  @Order(16)
  @DisplayName("PUT /settings - NULL mockMode → 400 BAD_REQUEST")
  @TestSecurity(user = "admin", roles = "ADMIN")
  void testUpdateSettings_NullMockMode_Returns400() {
    // Given: DTO with null mockMode
    XentralSettingsDTO dto = new XentralSettingsDTO(VALID_URL, VALID_TOKEN, null);

    // When: PUT /settings
    // Then: 400 BAD_REQUEST
    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .put(ADMIN_API_PATH)
        .then()
        .statusCode(400)
        .body("error", containsString("Mock-Mode darf nicht null sein"));
  }

  // ============================================================================
  // TEST GROUP 3: PUT /settings - UPSERT Pattern (Critical Bug Detection!)
  // ============================================================================

  @Test
  @Order(20)
  @DisplayName("PUT /settings - FIRST TIME (DB empty) → creates new entry")
  @TestSecurity(user = "admin", roles = "ADMIN")
  void testUpdateSettings_FirstTime_CreatesNewEntry() {
    // Given: Database is empty
    assertThat(repository.count()).isZero();

    // Given: Valid DTO
    XentralSettingsDTO dto = new XentralSettingsDTO(VALID_URL, VALID_TOKEN, true);

    // When: PUT /settings
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

    // Then: Entry was created in DB
    assertThat(repository.count()).isEqualTo(1);
    assertThat(repository.getSingleton()).isPresent();
  }

  @Test
  @Order(21)
  @DisplayName("PUT /settings - SECOND TIME (DB has entry) → updates existing (no duplicate!)")
  @TestSecurity(user = "admin", roles = "ADMIN")
  @TestTransaction
  void testUpdateSettings_SecondTime_UpdatesExisting_NoDuplicate() {
    // Given: Settings already exist in DB
    XentralSettings existing = new XentralSettings();
    existing.setApiUrl("https://old.xentral.biz");
    existing.setApiToken("old-token");
    existing.setMockMode(true);
    repository.persist(existing);
    repository.flush(); // Ensure entity is persisted before API call

    assertThat(repository.count()).isEqualTo(1);

    // Given: Updated DTO
    XentralSettingsDTO updatedDto =
        new XentralSettingsDTO("https://new.xentral.biz", "new-token", false);

    // When: PUT /settings
    given()
        .contentType(ContentType.JSON)
        .body(updatedDto)
        .when()
        .put(ADMIN_API_PATH)
        .then()
        .statusCode(200)
        .body("apiUrl", equalTo("https://new.xentral.biz"))
        .body("apiToken", equalTo("new-token"))
        .body("mockMode", equalTo(false));

    // Then: CRITICAL BUG DETECTION - No duplicate entry created!
    assertThat(repository.count())
        .withFailMessage("UPSERT Pattern broken: Duplicate entry created instead of update!")
        .isEqualTo(1);

    // Then: Existing entry was updated
    XentralSettings updated = repository.getSingleton().orElseThrow();
    assertThat(updated.getApiUrl()).isEqualTo("https://new.xentral.biz");
    assertThat(updated.getApiToken()).isEqualTo("new-token");
    assertThat(updated.getMockMode()).isFalse();
  }

  // ============================================================================
  // TEST GROUP 4: GET /test-connection - Mock Mode
  // ============================================================================

  @Test
  @Order(30)
  @DisplayName("GET /test-connection - Mock Mode → SUCCESS")
  @TestSecurity(user = "admin", roles = "ADMIN")
  void testConnectionTest_MockMode_ReturnsSuccess() {
    // Given: Mock Mode is active (default in tests)

    // When: GET /test-connection
    given()
        .when()
        .get(TEST_CONNECTION_PATH)
        .then()
        .statusCode(200)
        .body("status", equalTo("SUCCESS"))
        .body("message", containsString("Verbindung erfolgreich"))
        .body("message", containsString("Mitarbeiter gefunden"));
  }

  // ============================================================================
  // TEST GROUP 5: Security - Authorization
  // ============================================================================

  @Test
  @Order(40)
  @DisplayName("GET /settings - NO AUTH → 401 UNAUTHORIZED")
  void testGetSettings_NoAuth_Returns401() {
    // When: GET /settings without authentication
    // Then: 401 UNAUTHORIZED
    given().when().get(ADMIN_API_PATH).then().statusCode(401);
  }

  @Test
  @Order(41)
  @DisplayName("GET /settings - USER role (not ADMIN) → 403 FORBIDDEN")
  @TestSecurity(user = "user", roles = "user")
  void testGetSettings_UserRole_Returns403() {
    // When: GET /settings with USER role (not ADMIN)
    // Then: 403 FORBIDDEN
    given().when().get(ADMIN_API_PATH).then().statusCode(403);
  }

  @Test
  @Order(42)
  @DisplayName("PUT /settings - USER role (not ADMIN) → 403 FORBIDDEN")
  @TestSecurity(user = "user", roles = "user")
  void testUpdateSettings_UserRole_Returns403() {
    // Given: Valid DTO
    XentralSettingsDTO dto = new XentralSettingsDTO(VALID_URL, VALID_TOKEN, true);

    // When: PUT /settings with USER role (not ADMIN)
    // Then: 403 FORBIDDEN
    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .put(ADMIN_API_PATH)
        .then()
        .statusCode(403);
  }

  @Test
  @Order(43)
  @DisplayName("GET /test-connection - USER role (not ADMIN) → 403 FORBIDDEN")
  @TestSecurity(user = "user", roles = "user")
  void testConnectionTest_UserRole_Returns403() {
    // When: GET /test-connection with USER role (not ADMIN)
    // Then: 403 FORBIDDEN
    given().when().get(TEST_CONNECTION_PATH).then().statusCode(403);
  }

  // ============================================================================
  // TEST GROUP 6: Entity Lifecycle - Audit Fields
  // ============================================================================

  @Test
  @Order(50)
  @DisplayName("PUT /settings - createdAt is set on create")
  @TestSecurity(user = "admin", roles = "ADMIN")
  @TestTransaction
  void testUpdateSettings_CreatedAtIsSet() {
    // Given: Database is empty
    assertThat(repository.count()).isZero();

    // Given: Valid DTO
    XentralSettingsDTO dto = new XentralSettingsDTO(VALID_URL, VALID_TOKEN, true);

    // When: PUT /settings (creates new entry)
    given().contentType(ContentType.JSON).body(dto).when().put(ADMIN_API_PATH).then().statusCode(200);

    // Then: createdAt was set
    XentralSettings created = repository.getSingleton().orElseThrow();
    assertThat(created.getCreatedAt())
        .withFailMessage("createdAt should be set on @PrePersist")
        .isNotNull();
  }

  @Test
  @Order(51)
  @DisplayName("PUT /settings - updatedAt is set on update")
  @TestSecurity(user = "admin", roles = "ADMIN")
  @TestTransaction
  void testUpdateSettings_UpdatedAtIsSet() throws InterruptedException {
    // Given: Settings already exist
    XentralSettings existing = new XentralSettings();
    existing.setApiUrl("https://old.xentral.biz");
    existing.setApiToken("old-token");
    existing.setMockMode(true);
    repository.persist(existing);
    repository.flush(); // Ensure entity is persisted before API call

    // Wait 100ms to ensure updatedAt is different from createdAt
    Thread.sleep(100);

    // Given: Updated DTO
    XentralSettingsDTO updatedDto =
        new XentralSettingsDTO("https://new.xentral.biz", "new-token", false);

    // When: PUT /settings (updates existing entry)
    given().contentType(ContentType.JSON).body(updatedDto).when().put(ADMIN_API_PATH).then().statusCode(200);

    // Then: updatedAt was set
    XentralSettings updated = repository.getSingleton().orElseThrow();
    assertThat(updated.getUpdatedAt())
        .withFailMessage("updatedAt should be set on @PreUpdate")
        .isNotNull();
  }
}
