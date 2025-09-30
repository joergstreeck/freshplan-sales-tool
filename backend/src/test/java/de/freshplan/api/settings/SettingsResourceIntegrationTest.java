package de.freshplan.api.settings;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import de.freshplan.infrastructure.settings.Setting;
import de.freshplan.infrastructure.settings.SettingsScope;
import de.freshplan.infrastructure.settings.SettingsService;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for Settings REST API (Sprint 1.2 PR #2). Verifies ETag headers, conditional
 * requests, and optimistic locking with real HTTP calls.
 *
 * <p>Unit tests for business logic are in SettingsResourceUnitTest.
 *
 * <p>Sprint 2.1.4 Fix: Removed @TestTransaction from class level to fix test isolation issues
 */
@QuarkusTest
@TestHTTPEndpoint(SettingsResource.class)
@DisplayName("Settings REST API Integration Tests")
class SettingsResourceIntegrationTest {

  private static final String TEST_KEY = "api.test.setting";

  @Inject SettingsService settingsService;

  @BeforeEach
  @Transactional
  void cleanup() {
    Setting.delete("key", TEST_KEY);
    Setting.delete("key", "hierarchical.test");
    Setting.delete("key", "list.test1");
    Setting.delete("key", "list.test2");
    Setting.delete("key", "list.test3");
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  @DisplayName("Should create a setting and return ETag")
  void testCreateSetting() {
    JsonObject value = new JsonObject().put("enabled", true).put("maxItems", 50);
    JsonObject metadata = new JsonObject().put("description", "Test API setting");

    JsonObject request =
        new JsonObject()
            .put("scope", "GLOBAL")
            .put("key", TEST_KEY)
            .put("value", value)
            .put("metadata", metadata);

    given()
        .contentType(ContentType.JSON)
        .body(request.encode())
        .when()
        .post()
        .then()
        .statusCode(201)
        .header("ETag", notNullValue())
        .header("Location", containsString("/api/settings/"))
        .body("scope", equalTo("GLOBAL"))
        .body("key", equalTo(TEST_KEY))
        .body("value.enabled", equalTo(true))
        .body("value.maxItems", equalTo(50))
        .body("etag", notNullValue())
        .body("version", equalTo(1));
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  @DisplayName("Should get setting with ETag and cache headers")
  void testGetSettingWithEtag() {
    // Given - Create a setting via REST API
    JsonObject value = new JsonObject().put("test", "value");
    JsonObject request =
        new JsonObject().put("scope", "GLOBAL").put("key", TEST_KEY).put("value", value);

    given()
        .contentType(ContentType.JSON)
        .body(request.encode())
        .when()
        .post()
        .then()
        .statusCode(201);

    // When/Then - Get with ETag
    given()
        .queryParam("scope", "GLOBAL")
        .queryParam("key", TEST_KEY)
        .when()
        .get()
        .then()
        .statusCode(200)
        .header("ETag", notNullValue())
        .header("Cache-Control", containsString("max-age=60"))
        .header("Cache-Control", containsString("must-revalidate"))
        .body("value.test", equalTo("value"));
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  @DisplayName("Should return 304 Not Modified for matching ETag")
  void testConditionalGetWithEtag() {
    // Given - Create a setting via REST API and get ETag
    JsonObject value = new JsonObject().put("test", "value");
    JsonObject request =
        new JsonObject().put("scope", "GLOBAL").put("key", TEST_KEY).put("value", value);

    String etag =
        given()
            .contentType(ContentType.JSON)
            .body(request.encode())
            .when()
            .post()
            .then()
            .statusCode(201)
            .extract()
            .header("ETag");

    // When/Then - Request with matching If-None-Match
    given()
        .queryParam("scope", "GLOBAL")
        .queryParam("key", TEST_KEY)
        .header("If-None-Match", etag)
        .when()
        .get()
        .then()
        .statusCode(304)
        .header("ETag", equalTo(etag))
        .body(emptyString());
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  @DisplayName("Should update setting with optimistic locking")
  void testUpdateWithOptimisticLocking() {
    // Given - Create a setting using the service
    JsonObject initialValue = new JsonObject().put("version", 1);
    Setting setting =
        settingsService.saveSetting(
            SettingsScope.GLOBAL, null, TEST_KEY, initialValue, null, "test-user");

    String settingId = setting.id.toString();
    String originalEtag = setting.etag;
    JsonObject updatedValue = new JsonObject().put("version", 2);
    JsonObject updateRequest = new JsonObject().put("value", updatedValue);

    // When/Then - Update with correct ETag
    given()
        .contentType(ContentType.JSON)
        .header("If-Match", originalEtag)
        .body(updateRequest.encode())
        .when()
        .put("/{id}", settingId)
        .then()
        .statusCode(200)
        .header("ETag", notNullValue())
        .header("ETag", not(equalTo(originalEtag)))
        .body("version", equalTo(2))
        .body("value.version", equalTo(2));

    // When/Then - Update with wrong ETag should fail
    given()
        .contentType(ContentType.JSON)
        .header("If-Match", "wrong-etag")
        .body(updateRequest.encode())
        .when()
        .put("/{id}", settingId)
        .then()
        .statusCode(412); // Precondition Failed
  }

  @Test
  @TestSecurity(user = "user", roles = "user")
  @DisplayName("Should resolve setting hierarchically")
  void testHierarchicalResolution() {
    // Given - Create settings at different scopes using service
    settingsService.saveSetting(
        SettingsScope.GLOBAL,
        null,
        "hierarchical.test",
        new JsonObject().put("level", "global"),
        null,
        "test-user");

    settingsService.saveSetting(
        SettingsScope.TENANT,
        "freshfoodz",
        "hierarchical.test",
        new JsonObject().put("level", "tenant"),
        null,
        "test-user");

    // When/Then - Resolve with tenant context
    given()
        .queryParam("tenantId", "freshfoodz")
        .when()
        .get("/resolve/{key}", "hierarchical.test")
        .then()
        .statusCode(200)
        .body("value.level", equalTo("tenant"))
        .body("scope", equalTo("TENANT"));

    // When/Then - Resolve without context (gets global)
    given()
        .when()
        .get("/resolve/{key}", "hierarchical.test")
        .then()
        .statusCode(200)
        .body("value.level", equalTo("global"))
        .body("scope", equalTo("GLOBAL"));
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  @DisplayName("Should list settings by scope")
  void testListSettings() {
    // Given - Create multiple settings using service
    for (int i = 1; i <= 3; i++) {
      settingsService.saveSetting(
          SettingsScope.TENANT,
          "freshfoodz",
          "list.test" + i,
          new JsonObject().put("index", i),
          null,
          "test-user");
    }

    // When/Then
    given()
        .queryParam("scope", "TENANT")
        .queryParam("scopeId", "freshfoodz")
        .when()
        .get("/list")
        .then()
        .statusCode(200)
        .header("Cache-Control", notNullValue())
        .body("$", hasSize(3))
        .body("[0].scope", equalTo("TENANT"))
        .body("[0].scopeId", equalTo("freshfoodz"));
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  @DisplayName("Should delete setting")
  void testDeleteSetting() {
    // Given - Create setting using service
    Setting setting =
        settingsService.saveSetting(
            SettingsScope.GLOBAL, null, TEST_KEY, new JsonObject(), null, "test-user");

    String settingId = setting.id.toString();

    // When/Then
    given().when().delete("/{id}", settingId).then().statusCode(204);

    // Verify deleted
    given()
        .queryParam("scope", "GLOBAL")
        .queryParam("key", TEST_KEY)
        .when()
        .get()
        .then()
        .statusCode(404);
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  @DisplayName("Should get cache statistics")
  void testGetCacheStats() {
    given()
        .when()
        .get("/stats/cache")
        .then()
        .statusCode(200)
        .body("hitRate", notNullValue())
        .body("availability", notNullValue())
        .body("avgResponseTimeMs", notNullValue());
  }

  @Test
  @TestSecurity(user = "user", roles = "user")
  @DisplayName("Should deny non-admin users from creating settings")
  void testAuthorizationForCreate() {
    JsonObject request =
        new JsonObject().put("scope", "GLOBAL").put("key", TEST_KEY).put("value", new JsonObject());

    given()
        .contentType(ContentType.JSON)
        .body(request.encode())
        .when()
        .post()
        .then()
        .statusCode(403); // Forbidden
  }

  @Test
  @TestSecurity(user = "admin", roles = "admin")
  @DisplayName("Should return 409 when creating duplicate setting")
  void testCreateDuplicateSetting() {
    // Given - Create first setting using service
    settingsService.saveSetting(
        SettingsScope.GLOBAL, null, TEST_KEY, new JsonObject(), null, "test-user");

    // When/Then - Try to create duplicate
    JsonObject request =
        new JsonObject().put("scope", "GLOBAL").put("key", TEST_KEY).put("value", new JsonObject());

    given()
        .contentType(ContentType.JSON)
        .body(request.encode())
        .when()
        .post()
        .then()
        .statusCode(409)
        .body(containsString("already exists"));
  }
}
