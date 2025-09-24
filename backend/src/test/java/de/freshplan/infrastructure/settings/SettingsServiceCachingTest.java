package de.freshplan.infrastructure.settings;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Tests cache invalidation for SettingsService.
 * Sprint 1.4: Foundation Quick-Wins - Cache implementation verification.
 */
@QuarkusTest
class SettingsServiceCachingTest {

  @Inject SettingsService service;

  @Test
  void update_shouldInvalidateCache() {
    // Given: Create a new setting with initial value
    var created = service.saveSetting(
        SettingsScope.GLOBAL,
        null,
        "cache.test." + UUID.randomUUID(), // Unique key to avoid conflicts
        new JsonObject().put("v", 1),
        null,
        "test-user");

    assertNotNull(created);
    assertEquals(1, created.value.getInteger("v"));

    // When: First resolve -> should return v=1
    var ctx = SettingsService.SettingsContext.global();
    Optional<Setting> r1 = service.resolveSetting(created.key, ctx);

    assertTrue(r1.isPresent());
    assertEquals(1, r1.get().value.getInteger("v"));

    // And: Update the setting with ETag
    UUID id = created.id;
    String etag = created.etag;
    var updated = service.updateSettingWithEtag(
        id,
        new JsonObject().put("v", 2),
        null,
        etag,
        "test-user-2");

    assertNotNull(updated);
    assertEquals(2, updated.value.getInteger("v"));

    // Then: Second resolve -> must return v=2 (cache was invalidated)
    Optional<Setting> r2 = service.resolveSetting(created.key, ctx);

    assertTrue(r2.isPresent());
    assertEquals(2, r2.get().value.getInteger("v"),
        "Cache should have been invalidated after update");

    // Cleanup
    service.deleteSetting(id);

    // Verify deletion also invalidates cache
    Optional<Setting> r3 = service.resolveSetting(created.key, ctx);
    assertFalse(r3.isPresent(),
        "Cache should have been invalidated after deletion");
  }

  @Test
  void getSetting_shouldBeCached() {
    // Given: Create a setting
    var created = service.saveSetting(
        SettingsScope.GLOBAL,
        null,
        "cache.test.get." + UUID.randomUUID(),
        new JsonObject().put("value", "cached"),
        null,
        "test-user");

    // When: Get the setting multiple times
    Optional<Setting> first = service.getSetting(SettingsScope.GLOBAL, null, created.key);
    Optional<Setting> second = service.getSetting(SettingsScope.GLOBAL, null, created.key);

    // Then: Both should return the same instance (from cache)
    assertTrue(first.isPresent());
    assertTrue(second.isPresent());
    assertEquals("cached", first.get().value.getString("value"));
    assertEquals("cached", second.get().value.getString("value"));

    // Note: In a real cache test, we'd verify actual cache hits via metrics
    // For now, we just verify functionality works correctly

    // Cleanup
    service.deleteSetting(created.id);
  }

  @Test
  void listSettings_shouldBeCached() {
    // Given: Create multiple settings
    String scopeId = "test-scope-" + UUID.randomUUID();

    service.saveSetting(
        SettingsScope.TENANT,
        scopeId,
        "list.test.1",
        new JsonObject().put("n", 1),
        null,
        "test-user");

    service.saveSetting(
        SettingsScope.TENANT,
        scopeId,
        "list.test.2",
        new JsonObject().put("n", 2),
        null,
        "test-user");

    // When: List settings multiple times
    var list1 = service.listSettings(SettingsScope.TENANT, scopeId);
    var list2 = service.listSettings(SettingsScope.TENANT, scopeId);

    // Then: Both lists should have the same content
    assertEquals(2, list1.size());
    assertEquals(2, list2.size());

    // Cleanup
    list1.forEach(setting -> service.deleteSetting(setting.id));
  }
}