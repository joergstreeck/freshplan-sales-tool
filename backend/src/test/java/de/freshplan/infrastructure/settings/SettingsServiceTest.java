package de.freshplan.infrastructure.settings;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Settings Registry Service (Sprint 1.2 PR #2).
 * Verifies hierarchical resolution, ETag support, and caching behavior.
 */
@QuarkusTest
@DisplayName("Settings Service Tests")
class SettingsServiceTest {

    @Inject
    SettingsService settingsService;

    private static final String TEST_USER = "test-user";
    private static final String TEST_KEY = "test.setting";
    private static final String TENANT_ID = "freshfoodz";
    private static final String TERRITORY_DE = "DE";
    private static final String TERRITORY_CH = "CH";
    private static final String ACCOUNT_ID = "acc-123";

    @BeforeEach
    @Transactional
    void cleanup() {
        // Clean up all test data to ensure test isolation
        Setting.delete("key", TEST_KEY);
        Setting.delete("key", "setting1");
        Setting.delete("key", "setting2");
        Setting.delete("key", "setting3");
        Setting.delete("key", "tax.config");
        Setting.delete("key", "hierarchical.test");
    }

    @Test
    @DisplayName("Should create and retrieve a global setting")
    @Transactional
    void testCreateAndRetrieveGlobalSetting() {
        // Given
        JsonObject value = new JsonObject()
            .put("enabled", true)
            .put("threshold", 100);
        JsonObject metadata = new JsonObject()
            .put("description", "Test setting");

        // When
        Setting created = settingsService.saveSetting(
            SettingsScope.GLOBAL, null, TEST_KEY,
            value, metadata, TEST_USER
        );

        // Then
        assertNotNull(created.id);
        assertEquals(SettingsScope.GLOBAL, created.scope);
        assertNull(created.scopeId);
        assertEquals(TEST_KEY, created.key);
        assertEquals(value, created.value);
        assertNotNull(created.etag);
        assertEquals(1, created.version);

        // Verify retrieval
        Optional<Setting> retrieved = settingsService.getSetting(
            SettingsScope.GLOBAL, null, TEST_KEY
        );
        assertTrue(retrieved.isPresent());
        assertEquals(created.id, retrieved.get().id);
    }

    @Test
    @DisplayName("Should resolve settings hierarchically")
    @Transactional
    void testHierarchicalResolution() {
        // Given - Create settings at different scopes
        JsonObject globalValue = new JsonObject().put("level", "global");
        JsonObject tenantValue = new JsonObject().put("level", "tenant");
        JsonObject territoryValue = new JsonObject().put("level", "territory");
        JsonObject accountValue = new JsonObject().put("level", "account");

        settingsService.saveSetting(SettingsScope.GLOBAL, null, TEST_KEY,
            globalValue, null, TEST_USER);
        settingsService.saveSetting(SettingsScope.TENANT, TENANT_ID, TEST_KEY,
            tenantValue, null, TEST_USER);
        settingsService.saveSetting(SettingsScope.TERRITORY, TERRITORY_DE, TEST_KEY,
            territoryValue, null, TEST_USER);
        settingsService.saveSetting(SettingsScope.ACCOUNT, ACCOUNT_ID, TEST_KEY,
            accountValue, null, TEST_USER);

        // When - Resolve with full context (should get account-level)
        var fullContext = new SettingsService.SettingsContext(
            TENANT_ID, TERRITORY_DE, ACCOUNT_ID, null
        );
        Optional<Setting> resolved = settingsService.resolveSetting(TEST_KEY, fullContext);

        // Then
        assertTrue(resolved.isPresent());
        assertEquals("account", resolved.get().value.getString("level"));

        // When - Resolve with partial context (should get territory-level)
        var territoryContext = new SettingsService.SettingsContext(
            TENANT_ID, TERRITORY_DE, null, null
        );
        resolved = settingsService.resolveSetting(TEST_KEY, territoryContext);

        // Then
        assertTrue(resolved.isPresent());
        assertEquals("territory", resolved.get().value.getString("level"));

        // When - Resolve with minimal context (should get global)
        var globalContext = SettingsService.SettingsContext.global();
        resolved = settingsService.resolveSetting(TEST_KEY, globalContext);

        // Then
        assertTrue(resolved.isPresent());
        assertEquals("global", resolved.get().value.getString("level"));
    }

    @Test
    @DisplayName("Should handle ETag-based optimistic locking")
    @Transactional
    void testEtagOptimisticLocking() {
        // Given - Create a setting
        JsonObject initialValue = new JsonObject().put("count", 1);
        Setting created = settingsService.saveSetting(
            SettingsScope.GLOBAL, null, TEST_KEY,
            initialValue, null, TEST_USER
        );
        String originalEtag = created.etag;
        UUID id = created.id;

        // When - Update with correct ETag
        JsonObject updatedValue = new JsonObject().put("count", 2);
        Setting updated = settingsService.updateSettingWithEtag(
            id, updatedValue, null, originalEtag, TEST_USER
        );

        // Then
        assertNotEquals(originalEtag, updated.etag);
        assertEquals(2, updated.version);
        assertEquals(2, updated.value.getInteger("count"));

        // When/Then - Update with wrong ETag should fail (412)
        JsonObject failedValue = new JsonObject().put("count", 3);
        WebApplicationException ex412 = assertThrows(WebApplicationException.class, () ->
            settingsService.updateSettingWithEtag(id, failedValue, null, originalEtag, TEST_USER)
        );
        assertEquals(412, ex412.getResponse().getStatus());

        // When/Then - Update without ETag should fail (428 Precondition Required)
        WebApplicationException ex428 = assertThrows(WebApplicationException.class, () ->
            settingsService.updateSettingWithEtag(id, failedValue, null, null, TEST_USER)
        );
        assertEquals(428, ex428.getResponse().getStatus());
    }

    @Test
    @DisplayName("Should check ETag for conditional requests")
    @Transactional
    void testEtagCheck() {
        // Given
        JsonObject value = new JsonObject().put("test", true);
        Setting created = settingsService.saveSetting(
            SettingsScope.GLOBAL, null, TEST_KEY,
            value, null, TEST_USER
        );

        // When/Then
        assertTrue(settingsService.checkEtag(created.id, created.etag));
        assertFalse(settingsService.checkEtag(created.id, "wrong-etag"));
        assertFalse(settingsService.checkEtag(UUID.randomUUID(), created.etag));
    }

    @Test
    @DisplayName("Should list settings by scope")
    @Transactional
    void testListSettings() {
        // Given - Create settings in different scopes
        settingsService.saveSetting(SettingsScope.TENANT, TENANT_ID, "setting1",
            new JsonObject(), null, TEST_USER);
        settingsService.saveSetting(SettingsScope.TENANT, TENANT_ID, "setting2",
            new JsonObject(), null, TEST_USER);
        settingsService.saveSetting(SettingsScope.TERRITORY, TERRITORY_DE, "setting3",
            new JsonObject(), null, TEST_USER);

        // When
        List<Setting> tenantSettings = settingsService.listSettings(
            SettingsScope.TENANT, TENANT_ID
        );
        List<Setting> territorySettings = settingsService.listSettings(
            SettingsScope.TERRITORY, TERRITORY_DE
        );

        // Then
        assertEquals(2, tenantSettings.size());
        assertEquals(1, territorySettings.size());
    }

    @Test
    @DisplayName("Should delete settings")
    @Transactional
    void testDeleteSetting() {
        // Given
        Setting created = settingsService.saveSetting(
            SettingsScope.GLOBAL, null, TEST_KEY,
            new JsonObject(), null, TEST_USER
        );

        // When
        boolean deleted = settingsService.deleteSetting(created.id);

        // Then
        assertTrue(deleted);
        Optional<Setting> retrieved = settingsService.getSetting(
            SettingsScope.GLOBAL, null, TEST_KEY
        );
        assertFalse(retrieved.isPresent());

        // Deleting again should return false
        assertFalse(settingsService.deleteSetting(created.id));
    }

    @Test
    @DisplayName("Should enforce territory isolation")
    @Transactional
    void testTerritoryIsolation() {
        // Given - Create settings for different territories
        JsonObject deValue = new JsonObject()
            .put("currency", "EUR")
            .put("vatRate", 19);
        JsonObject chValue = new JsonObject()
            .put("currency", "CHF")
            .put("vatRate", 7.7);

        settingsService.saveSetting(SettingsScope.TERRITORY, TERRITORY_DE,
            "tax.config", deValue, null, TEST_USER);
        settingsService.saveSetting(SettingsScope.TERRITORY, TERRITORY_CH,
            "tax.config", chValue, null, TEST_USER);

        // When - Resolve for DE context
        var deContext = new SettingsService.SettingsContext(
            TENANT_ID, TERRITORY_DE, null, null
        );
        Optional<Setting> deSetting = settingsService.resolveSetting("tax.config", deContext);

        // Then
        assertTrue(deSetting.isPresent());
        assertEquals("EUR", deSetting.get().value.getString("currency"));
        assertEquals(19, deSetting.get().value.getInteger("vatRate"));

        // When - Resolve for CH context
        var chContext = new SettingsService.SettingsContext(
            TENANT_ID, TERRITORY_CH, null, null
        );
        Optional<Setting> chSetting = settingsService.resolveSetting("tax.config", chContext);

        // Then
        assertTrue(chSetting.isPresent());
        assertEquals("CHF", chSetting.get().value.getString("currency"));
        assertEquals(7.7, chSetting.get().value.getDouble("vatRate"), 0.01);
    }

    @Test
    @DisplayName("Should prevent race condition on create")
    @Transactional
    void testCreateRaceCondition() {
        // Given
        JsonObject value = new JsonObject().put("test", "race");

        // When - Create first setting
        Setting first = settingsService.createSettingStrict(
            SettingsScope.GLOBAL, null, "race.test",
            value, null, TEST_USER
        );

        // Then - First creation succeeds
        assertNotNull(first);
        assertNotNull(first.id);
        assertNotNull(first.etag);

        // When/Then - Second creation with same key should fail
        WebApplicationException ex = assertThrows(WebApplicationException.class, () ->
            settingsService.createSettingStrict(
                SettingsScope.GLOBAL, null, "race.test",
                value, null, TEST_USER
            )
        );
        assertEquals(409, ex.getResponse().getStatus());

        // Cleanup
        first.delete();
    }

    @Test
    @DisplayName("Should get cache statistics")
    void testCacheStats() {
        // When
        var stats = settingsService.getCacheStats();

        // Then
        assertNotNull(stats);
        assertTrue(stats.hitRate() >= 0 && stats.hitRate() <= 100);
        assertTrue(stats.availability() >= 0 && stats.availability() <= 1);
        assertTrue(stats.avgResponseTimeMs() > 0);
    }
}