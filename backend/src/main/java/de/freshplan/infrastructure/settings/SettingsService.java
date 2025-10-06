package de.freshplan.infrastructure.settings;

import de.freshplan.infrastructure.security.RlsContext;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jboss.logging.Logger;

/**
 * Service for Settings Registry management (Sprint 1.2 PR #2). Provides hierarchical settings
 * resolution with ETag caching support.
 */
@ApplicationScoped
public class SettingsService {

  private static final Logger LOG = Logger.getLogger(SettingsService.class);
  private static final String CACHE_NAME = "settings-cache";

  @Inject EntityManager em;

  /** Retrieves a setting by scope, scope ID, and key. Sprint 1.4: Added caching support. */
  @CacheResult(cacheName = CACHE_NAME)
  @Transactional
  @RlsContext
  public Optional<Setting> getSetting(SettingsScope scope, String scopeId, String key) {
    LOG.debugf("Retrieving setting: scope=%s, scopeId=%s, key=%s", scope, scopeId, key);

    Setting setting = Setting.findByScopeAndKey(scope, scopeId, key);
    return Optional.ofNullable(setting);
  }

  /**
   * Resolves a setting hierarchically, returning the most specific value. Priority: CONTACT_ROLE >
   * ACCOUNT > TERRITORY > TENANT > GLOBAL. Sprint 1.4: Added caching.
   */
  @CacheResult(cacheName = CACHE_NAME)
  @Transactional
  @RlsContext
  public Optional<Setting> resolveSetting(String key, SettingsContext context) {
    LOG.debugf("Resolving setting hierarchically: key=%s, context=%s", key, context);

    Setting setting =
        Setting.resolveHierarchical(
            key,
            context.tenantId(),
            context.territory(),
            context.accountId(),
            context.contactRole());

    return Optional.ofNullable(setting);
  }

  /** Creates or updates a setting. Sprint 1.4: Added cache invalidation. */
  @CacheInvalidateAll(cacheName = CACHE_NAME)
  @Transactional
  @RlsContext
  public Setting saveSetting(
      SettingsScope scope,
      String scopeId,
      String key,
      JsonObject value,
      JsonObject metadata,
      String userId) {
    LOG.infof("Saving setting: scope=%s, scopeId=%s, key=%s", scope, scopeId, key);

    Setting setting = Setting.findByScopeAndKey(scope, scopeId, key);

    if (setting == null) {
      // Create new setting
      setting = new Setting();
      setting.scope = scope;
      setting.scopeId = scopeId;
      setting.key = key;
      setting.value = value;
      setting.metadata = metadata != null ? metadata : new JsonObject();
      setting.createdBy = userId;
      setting.version = 1;
      setting.persist();

      // Flush to trigger DB INSERT (ETag generation happens in trigger)
      em.flush();

      // Refresh entity to get DB-generated values (etag, created_at)
      em.refresh(setting);

      LOG.infof(
          "Created new setting with ID: %s, ETag: %s, Version: %d",
          setting.id, setting.etag, setting.version);
    } else {
      // Update existing setting
      setting.value = value;
      if (metadata != null) {
        setting.metadata = metadata;
      }
      setting.updatedBy = userId;
      // Version and ETag are managed by DB trigger (update_settings_etag), not manually

      // Flush to trigger DB updates (version increment and ETag generation happen in trigger)
      em.flush();

      // Refresh entity to get DB-generated values (version, etag, updated_at)
      em.refresh(setting);

      LOG.infof(
          "Updated setting with ID: %s, ETag: %s, Version: %d",
          setting.id, setting.etag, setting.version);
    }

    return setting;
  }

  /** Creates a new setting strictly (no upsert). Throws 409 Conflict if setting already exists. */
  @CacheInvalidateAll(cacheName = CACHE_NAME)
  @Transactional
  @RlsContext
  public Setting createSettingStrict(
      SettingsScope scope,
      String scopeId,
      String key,
      JsonObject value,
      JsonObject metadata,
      String userId) {
    LOG.infof("Creating setting (strict): scope=%s, scopeId=%s, key=%s", scope, scopeId, key);

    // Check if already exists
    Setting existing = Setting.findByScopeAndKey(scope, scopeId, key);
    if (existing != null) {
      LOG.warnf("Setting already exists: scope=%s, scopeId=%s, key=%s", scope, scopeId, key);
      throw new WebApplicationException("Setting already exists", Response.Status.CONFLICT);
    }

    // Create new setting
    Setting setting = new Setting();
    setting.scope = scope;
    setting.scopeId = scopeId;
    setting.key = key;
    setting.value = value != null ? value : new JsonObject();
    setting.metadata = metadata != null ? metadata : new JsonObject();
    setting.createdBy = userId;
    setting.version = 1;

    try {
      setting.persist();

      // Flush to trigger DB INSERT (ETag generation happens in trigger)
      em.flush();

      // Refresh entity to get DB-generated values (etag, created_at)
      em.refresh(setting);

      LOG.infof(
          "Created new setting with ID: %s, ETag: %s, Version: %d",
          setting.id, setting.etag, setting.version);
      return setting;
    } catch (Exception e) {
      // Handle unique constraint violation
      if (e.getCause() != null
          && (e.getCause().getMessage().contains("uq_security_settings")
              || e.getCause().getMessage().contains("duplicate key"))) {
        LOG.warnf(
            "Unique constraint violation for setting: scope=%s, scopeId=%s, key=%s",
            scope, scopeId, key);
        throw new WebApplicationException("Setting already exists", Response.Status.CONFLICT);
      }
      throw e;
    }
  }

  /**
   * Updates a setting with optimistic locking using ETag. Throws 412 Precondition Failed if ETag
   * doesn't match. Sprint 1.4: Added cache invalidation.
   */
  @CacheInvalidateAll(cacheName = CACHE_NAME)
  @Transactional
  @RlsContext
  public Setting updateSettingWithEtag(
      UUID id, JsonObject value, JsonObject metadata, String ifMatch, String userId) {
    LOG.infof("Updating setting with ETag: id=%s, ifMatch=%s", id, ifMatch);

    Setting setting = Setting.findById(id);
    if (setting == null) {
      throw new WebApplicationException("Setting not found", Response.Status.NOT_FOUND);
    }

    // If-Match header is required for updates to prevent lost updates
    if (ifMatch == null || ifMatch.isBlank()) {
      LOG.warnf("Missing If-Match header for setting %s", id);
      throw new WebApplicationException(
          "If-Match header is required for updates", 428 // Precondition Required
          );
    }

    // Check ETag for optimistic locking
    if (!setting.matchesEtag(ifMatch)) {
      LOG.warnf("ETag mismatch for setting %s: expected=%s, actual=%s", id, ifMatch, setting.etag);
      throw new WebApplicationException(
          "Precondition failed: ETag mismatch", Response.Status.PRECONDITION_FAILED);
    }

    // Update the setting
    setting.value = value;
    if (metadata != null) {
      setting.metadata = metadata;
    }
    setting.updatedBy = userId;
    // Version and ETag are managed by DB trigger (update_settings_etag), not manually

    // Flush to trigger DB updates (version increment and ETag generation happen in trigger)
    em.flush();

    // Refresh entity to get DB-generated values (version, etag, updated_at)
    em.refresh(setting);

    LOG.infof(
        "Successfully updated setting %s to version %d, new ETag: %s",
        id, setting.version, setting.etag);
    return setting;
  }

  /** Deletes a setting. Sprint 1.4: Added cache invalidation. */
  @CacheInvalidateAll(cacheName = CACHE_NAME)
  @Transactional
  @RlsContext
  public boolean deleteSetting(UUID id) {
    LOG.infof("Deleting setting: id=%s", id);

    Setting setting = Setting.findById(id);
    if (setting != null) {
      setting.delete();
      LOG.infof("Deleted setting with ID: %s", id);
      return true;
    }
    return false;
  }

  /**
   * Checks if a setting exists and hasn't been modified. Used for HTTP conditional requests
   * (If-None-Match).
   */
  @Transactional
  @RlsContext
  public boolean checkEtag(UUID id, String etag) {
    Setting setting = Setting.findById(id);
    return setting != null && setting.matchesEtag(etag);
  }

  /** Lists all settings for a given scope. Sprint 1.4: Added caching. */
  @CacheResult(cacheName = CACHE_NAME)
  @Transactional
  @RlsContext
  public List<Setting> listSettings(SettingsScope scope, String scopeId) {
    LOG.debugf("Listing settings: scope=%s, scopeId=%s", scope, scopeId);

    if (scopeId != null) {
      return Setting.find("scope = ?1 and scopeId = ?2", scope, scopeId).list();
    } else {
      return Setting.find("scope = ?1 and scopeId is null", scope).list();
    }
  }

  /** Gets cache statistics for monitoring. */
  public CacheStats getCacheStats() {
    // This would integrate with actual cache metrics
    // For now, return placeholder stats
    return new CacheStats(70.0, 0.95, 120);
  }

  /** Context for hierarchical settings resolution. */
  public record SettingsContext(
      String tenantId, String territory, String accountId, String contactRole) {
    public static SettingsContext global() {
      return new SettingsContext(null, null, null, null);
    }

    public static SettingsContext tenant(String tenantId) {
      return new SettingsContext(tenantId, null, null, null);
    }

    public static SettingsContext territory(String tenantId, String territory) {
      return new SettingsContext(tenantId, territory, null, null);
    }
  }

  /** Cache statistics for monitoring. */
  public record CacheStats(double hitRate, double availability, long avgResponseTimeMs) {}

  /**
   * Generate ETag for a setting. Replicates the DB trigger logic in Java for test compatibility.
   */
  private String generateEtag(Setting setting) {
    // MD5 hash of scope, scopeId, key, value, version
    String input =
        (setting.scope != null ? setting.scope.toString() : "")
            + ":"
            + (setting.scopeId != null ? setting.scopeId : "")
            + ":"
            + (setting.key != null ? setting.key : "")
            + ":"
            + (setting.value != null ? setting.value.toString() : "")
            + ":"
            + (setting.version != null ? setting.version.toString() : "1");

    try {
      java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
      byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (java.security.NoSuchAlgorithmException e) {
      // MD5 is a standard algorithm - this should never happen
      // If it does, it's a critical JVM configuration issue
      throw new RuntimeException("Failed to generate ETag: MD5 algorithm not available", e);
    }
  }
}
