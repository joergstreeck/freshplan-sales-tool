package de.freshplan.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.infrastructure.settings.Setting;
import de.freshplan.infrastructure.settings.SettingsScope;
import de.freshplan.infrastructure.settings.SettingsService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test RLS Connection Affinity for infrastructure services. Verifies that GUC variables are
 * properly set on the same connection used for database operations.
 */
@QuarkusTest
public class RlsConnectionAffinityTest {

  @Inject EntityManager em;

  @Inject SettingsService settingsService;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean up test data
    em.createQuery("DELETE FROM Setting WHERE key LIKE 'test_%'").executeUpdate();
  }

  @Test
  @TestSecurity(user = "test-user", roles = "user")
  @Transactional
  void testGucVariablesSetOnConnection() {
    // Given: We have a user context
    String expectedUser = "test-user";

    // When: We access the database through an @RlsContext annotated method
    settingsService.saveSetting(
        SettingsScope.GLOBAL,
        null,
        "test_guc_check",
        new JsonObject().put("value", "test"),
        null,
        expectedUser);

    // Then: GUC variables should be set on the connection
    String actualUser =
        (String)
            em.createNativeQuery("SELECT current_setting('app.current_user', true)")
                .getSingleResult();

    assertEquals(expectedUser, actualUser, "GUC variable should be set on connection");
  }

  @Test
  @Transactional
  void testFailClosedWithoutGuc() {
    // Given: No GUC variables are set (no user context)

    // When: We try to access protected data
    String result =
        (String)
            em.createNativeQuery("SELECT current_setting('app.current_user', true)")
                .getSingleResult();

    // Then: Should return null (no context set)
    assertNull(result, "Without RLS context, GUC should be null");

    // And: RLS policies should block access (0 rows returned)
    Long count = (Long) em.createNativeQuery("SELECT COUNT(*) FROM leads").getSingleResult();

    assertEquals(0L, count, "RLS should block access without GUC context");
  }

  @Test
  @TestSecurity(user = "user-de", roles = "user")
  void testTerritoryIsolation() {
    // Given: User with DE territory
    em.createNativeQuery("SET LOCAL app.current_territory = 'DE'").executeUpdate();

    // When: Creating settings for different territories
    em.createNativeQuery(
            "INSERT INTO settings (id, scope, key, value, territory, created_at, version, etag) "
                + "VALUES (gen_random_uuid(), 'GLOBAL', 'test_de', '{}'::jsonb, 'DE', now(), 1, gen_random_uuid())")
        .executeUpdate();

    em.createNativeQuery(
            "INSERT INTO settings (id, scope, key, value, territory, created_at, version, etag) "
                + "VALUES (gen_random_uuid(), 'GLOBAL', 'test_ch', '{}'::jsonb, 'CH', now(), 1, gen_random_uuid())")
        .executeUpdate();

    // Then: Should only see DE settings
    Long deCount =
        (Long)
            em.createNativeQuery("SELECT COUNT(*) FROM settings WHERE key = 'test_de'")
                .getSingleResult();

    Long chCount =
        (Long)
            em.createNativeQuery("SELECT COUNT(*) FROM settings WHERE key = 'test_ch'")
                .getSingleResult();

    assertEquals(1L, deCount, "Should see DE settings");
    assertEquals(0L, chCount, "Should not see CH settings");

    // Clean up
    em.createNativeQuery("DELETE FROM settings WHERE key LIKE 'test_%'").executeUpdate();
  }

  @Test
  @TestSecurity(user = "admin-user", roles = "admin")
  void testAdminBypassesRls() {
    // Given: Admin user
    em.createNativeQuery("SET LOCAL app.current_role = 'admin'").executeUpdate();
    em.createNativeQuery("SET LOCAL app.current_territory = 'DE'").executeUpdate();

    // When: Creating settings for different territories
    em.createNativeQuery(
            "INSERT INTO settings (id, scope, key, value, territory, created_at, version, etag) "
                + "VALUES (gen_random_uuid(), 'GLOBAL', 'test_admin_de', '{}'::jsonb, 'DE', now(), 1, gen_random_uuid())")
        .executeUpdate();

    em.createNativeQuery(
            "INSERT INTO settings (id, scope, key, value, territory, created_at, version, etag) "
                + "VALUES (gen_random_uuid(), 'GLOBAL', 'test_admin_ch', '{}'::jsonb, 'CH', now(), 1, gen_random_uuid())")
        .executeUpdate();

    // Then: Admin should see all settings
    Long totalCount =
        (Long)
            em.createNativeQuery("SELECT COUNT(*) FROM settings WHERE key LIKE 'test_admin_%'")
                .getSingleResult();

    assertEquals(2L, totalCount, "Admin should see all territories");

    // Clean up
    em.createNativeQuery("DELETE FROM settings WHERE key LIKE 'test_admin_%'").executeUpdate();
  }

  @Test
  void testHealthcheckFunction() {
    // Given: The healthcheck function exists

    // When: We call it
    Object[] result =
        (Object[]) em.createNativeQuery("SELECT * FROM check_rls_context()").getSingleResult();

    // Then: Should return context information
    assertNotNull(result, "Healthcheck should return results");
    assertEquals(5, result.length, "Should return 5 fields");

    // Verify RLS is active
    Boolean rlsActive = (Boolean) result[4];
    assertTrue(rlsActive, "RLS should be active");
  }

  @Test
  @TestSecurity(user = "test-user", roles = "user")
  @Transactional
  void testConnectionAffinityMaintained() {
    // Given: Multiple operations in same transaction
    String testKey = "test_affinity_" + System.currentTimeMillis();

    // When: Performing multiple database operations
    settingsService.saveSetting(
        SettingsScope.GLOBAL,
        null,
        testKey,
        new JsonObject().put("value", "initial"),
        null,
        "test-user");

    // Verify GUC is still set
    String user1 =
        (String)
            em.createNativeQuery("SELECT current_setting('app.current_user', true)")
                .getSingleResult();

    // Update the setting
    Setting setting = Setting.findByScopeAndKey(SettingsScope.GLOBAL, null, testKey);
    assertNotNull(setting);

    settingsService.saveSetting(
        SettingsScope.GLOBAL,
        null,
        testKey,
        new JsonObject().put("value", "updated"),
        null,
        "test-user");

    // Verify GUC is still set after second operation
    String user2 =
        (String)
            em.createNativeQuery("SELECT current_setting('app.current_user', true)")
                .getSingleResult();

    // Then: GUC should be maintained throughout transaction
    assertEquals("test-user", user1, "GUC should be set after first operation");
    assertEquals("test-user", user2, "GUC should still be set after second operation");
    assertEquals(user1, user2, "GUC should be consistent throughout transaction");
  }
}
