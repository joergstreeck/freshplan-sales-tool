package de.freshplan.infrastructure.settings;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import io.vertx.core.json.JsonObject;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entity for Settings Registry (Sprint 1.2 PR #2). Maps to the security_settings table with ETag
 * support.
 */
@Entity
@Table(name = "security_settings")
@NamedQueries({
  @NamedQuery(
      name = "Setting.findByScopeAndKey",
      query =
          "SELECT s FROM Setting s WHERE s.scope = :scope "
              + "AND (:scopeId IS NULL AND s.scopeId IS NULL OR s.scopeId = :scopeId) "
              + "AND s.key = :key"),
  @NamedQuery(name = "Setting.findByEtag", query = "SELECT s FROM Setting s WHERE s.etag = :etag"),
  @NamedQuery(
      name = "Setting.resolveHierarchy",
      query =
          "SELECT s FROM Setting s WHERE s.key = :key "
              + "AND ((s.scope = 'GLOBAL' AND s.scopeId IS NULL) "
              + "OR (s.scope = 'TENANT' AND s.scopeId = :tenantId) "
              + "OR (s.scope = 'TERRITORY' AND s.scopeId = :territory) "
              + "OR (s.scope = 'ACCOUNT' AND s.scopeId = :accountId) "
              + "OR (s.scope = 'CONTACT_ROLE' AND s.scopeId = :contactRole)) "
              + "ORDER BY CASE s.scope "
              + "WHEN 'CONTACT_ROLE' THEN 5 "
              + "WHEN 'ACCOUNT' THEN 4 "
              + "WHEN 'TERRITORY' THEN 3 "
              + "WHEN 'TENANT' THEN 2 "
              + "WHEN 'GLOBAL' THEN 1 END DESC")
})
public class Setting extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  public UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public SettingsScope scope;

  @Column(name = "scope_id")
  public String scopeId;

  @Column(nullable = false)
  public String key;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  public JsonObject value;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  public JsonObject metadata;

  @Column public String etag;

  // Note: Version is managed by DB trigger, not by Hibernate @Version
  // This avoids conflicts between Hibernate's optimistic locking and DB triggers
  @Column(nullable = false)
  public Integer version = 1;

  @Column(name = "created_at")
  public Instant createdAt;

  @Column(name = "updated_at")
  public Instant updatedAt;

  @Column(name = "created_by")
  public String createdBy;

  @Column(name = "updated_by")
  public String updatedBy;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
    if (metadata == null) {
      metadata = new JsonObject();
    }
    // Generate ETag on create
    generateEtag();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
    // Increment version for optimistic locking
    // Note: Could also be managed by DB trigger, but doing it here for consistency
    if (version == null) {
      version = 1;
    } else {
      version++;
    }
    // Regenerate ETag after version increment
    generateEtag();
  }

  /**
   * Generates ETag based on version, value, and metadata.
   * Uses MD5 hash for consistency with DB trigger (if implemented).
   */
  private void generateEtag() {
    String data = String.format("%d:%s:%s",
        version != null ? version : 1,
        value != null ? value.encode() : "",
        metadata != null ? metadata.encode() : "");

    try {
      java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
      byte[] hash = md.digest(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder();
      for (byte b : hash) {
        hex.append(String.format("%02x", b));
      }
      this.etag = "\"" + hex.toString() + "\""; // Quoted ETag per HTTP spec
    } catch (java.security.NoSuchAlgorithmException e) {
      // Fallback to simple hash if MD5 not available
      this.etag = "\"" + Integer.toHexString(data.hashCode()) + "\"";
    }
  }

  /** Finds a setting by scope, scope ID, and key. */
  public static Setting findByScopeAndKey(SettingsScope scope, String scopeId, String key) {
    return find(
            "#Setting.findByScopeAndKey",
            Parameters.with("scope", scope).and("scopeId", scopeId).and("key", key))
        .firstResult();
  }

  /** Finds a setting by its ETag. */
  public static Setting findByEtag(String etag) {
    return find("#Setting.findByEtag", Parameters.with("etag", etag)).firstResult();
  }

  /** Resolves settings hierarchically from most to least specific scope. */
  public static Setting resolveHierarchical(
      String key, String tenantId, String territory, String accountId, String contactRole) {
    return find(
            "#Setting.resolveHierarchy",
            Parameters.with("key", key)
                .and("tenantId", tenantId)
                .and("territory", territory)
                .and("accountId", accountId)
                .and("contactRole", contactRole))
        .firstResult();
  }

  /** Checks if the ETag matches (for conditional requests). */
  public boolean matchesEtag(String otherEtag) {
    return etag != null && etag.equals(otherEtag);
  }

  /** Creates a unique cache key for this setting. */
  public String getCacheKey() {
    return String.format("setting:%s:%s:%s", scope, scopeId != null ? scopeId : "null", key);
  }
}
