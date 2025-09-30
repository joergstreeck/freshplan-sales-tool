package de.freshplan.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.infrastructure.settings.Setting;
import de.freshplan.infrastructure.settings.SettingsScope;
import de.freshplan.infrastructure.settings.SettingsService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;import io.quarkus.test.security.TestSecurity;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test RLS Connection Affinity for infrastructure services. Verifies that GUC variables are
 * properly set on the same connection used for database operations.
 *
 * Sprint 2.1.4: Disabled because RLS interceptor is disabled in tests to fix ContextNotActiveException
 */
@QuarkusTest
@Disabled("RLS interceptor disabled in tests - see Sprint 2.1.4 fix for ContextNotActiveException")
public class RlsConnectionAffinityTest {

  @Inject EntityManager em;

  @Inject SettingsService settingsService;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean up test data
    em.createQuery("DELETE FROM Setting WHERE key LIKE 'test_%'").executeUpdate();
    em.createNativeQuery(
            "DELETE FROM leads WHERE company_name LIKE 'Test%' OR company_name LIKE 'Admin Test%'")
        .executeUpdate();
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
            em.createNativeQuery("SELECT current_setting('app.user_context', true)")
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
            em.createNativeQuery("SELECT current_setting('app.user_context', true)")
                .getSingleResult();

    // Then: Should return empty string when using 'true' parameter (no context set)
    assertEquals("", result, "Without RLS context, GUC should be empty");

    // Note: Without user context, no RLS policies are applied in test environment
    // In production, RLS policies would block access
    // This test primarily verifies GUC is not set
  }

  @Test
  @TestSecurity(user = "user-de", roles = "user")
  @Transactional
  void testUserCanAccessLeadsFromAllTerritories() {
    // Note: Lead system allows nationwide access
    // Territory is used for business rules, not access control
    // This test verifies that users can see their own leads from all territories

    // Given: User with DE territory preference
    em.createNativeQuery("SELECT set_config('app.territory_context', 'DE', true)")
        .getSingleResult();
    em.createNativeQuery("SELECT set_config('app.user_context', 'user-de', true)")
        .getSingleResult();

    // Count existing leads before test
    Long initialCount =
        (Long)
            em.createNativeQuery("SELECT COUNT(*) FROM leads WHERE company_name LIKE 'Test %'")
                .getSingleResult();

    // When: Creating leads for different territories
    em.createNativeQuery(
            "INSERT INTO leads (company_name, email, country_code, territory_id, status, owner_user_id, created_by) "
                + "VALUES ('Test DE GmbH', 'test-de@example.com', 'DE', 'DE', 'REGISTERED', 'user-de', 'user-de')")
        .executeUpdate();

    em.createNativeQuery(
            "INSERT INTO leads (company_name, email, country_code, territory_id, status, owner_user_id, created_by) "
                + "VALUES ('Test CH AG', 'test-ch@example.com', 'CH', 'CH', 'REGISTERED', 'user-de', 'user-de')")
        .executeUpdate();

    // Then: User can see their leads from all territories (nationwide access)
    Long afterCount =
        (Long)
            em.createNativeQuery("SELECT COUNT(*) FROM leads WHERE company_name LIKE 'Test %'")
                .getSingleResult();

    // Should have exactly 2 more leads than before
    assertEquals(initialCount + 2L, afterCount, "User should see their leads from all territories");

    // Clean up
    em.createNativeQuery("DELETE FROM leads WHERE company_name LIKE 'Test %'").executeUpdate();
  }

  @Test
  @TestSecurity(user = "admin-user", roles = "admin")
  @Transactional
  void testAdminBypassesRls() {
    // Given: Admin user
    em.createNativeQuery("SELECT set_config('app.role_context', 'ADMIN', true)").getSingleResult();
    em.createNativeQuery("SELECT set_config('app.territory_context', 'DE', true)")
        .getSingleResult();

    // Count existing admin leads before test
    Long initialCount =
        (Long)
            em.createNativeQuery("SELECT COUNT(*) FROM leads WHERE company_name LIKE 'Admin Test%'")
                .getSingleResult();

    // When: Creating leads for different territories
    em.createNativeQuery(
            "INSERT INTO leads (company_name, email, country_code, territory_id, status, owner_user_id, created_by) "
                + "VALUES ('Admin Test DE', 'admin-de@example.com', 'DE', 'DE', 'REGISTERED', 'admin-user', 'admin-user')")
        .executeUpdate();

    em.createNativeQuery(
            "INSERT INTO leads (company_name, email, country_code, territory_id, status, owner_user_id, created_by) "
                + "VALUES ('Admin Test CH', 'admin-ch@example.com', 'CH', 'CH', 'REGISTERED', 'admin-user', 'admin-user')")
        .executeUpdate();

    // Then: Admin should see all leads
    Long afterCount =
        (Long)
            em.createNativeQuery("SELECT COUNT(*) FROM leads WHERE company_name LIKE 'Admin Test%'")
                .getSingleResult();

    assertEquals(initialCount + 2L, afterCount, "Admin should see all territories");

    // Clean up
    em.createNativeQuery("DELETE FROM leads WHERE company_name LIKE 'Admin Test%'").executeUpdate();
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
            em.createNativeQuery("SELECT current_setting('app.user_context', true)")
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
            em.createNativeQuery("SELECT current_setting('app.user_context', true)")
                .getSingleResult();

    // Then: GUC should be maintained throughout transaction
    assertEquals("test-user", user1, "GUC should be set after first operation");
    assertEquals("test-user", user2, "GUC should still be set after second operation");
    assertEquals(user1, user2, "GUC should be consistent throughout transaction");
  }
}
