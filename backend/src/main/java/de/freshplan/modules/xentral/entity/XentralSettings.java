package de.freshplan.modules.xentral.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/**
 * XentralSettings entity for storing Xentral API configuration.
 *
 * <p>Sprint 2.1.7.2 - D5: Admin-UI für Xentral-Einstellungen
 *
 * <p>This entity stores Xentral ERP integration settings that can be managed via Admin-UI.
 * Database settings have priority over application.properties defaults.
 *
 * <p><b>Architecture:</b>
 *
 * <ul>
 *   <li>SINGLETON pattern: Only one configuration row allowed (enforced by DB constraint)
 *   <li>Database overrides application.properties
 *   <li>If no row exists, application.properties values are used as fallback
 * </ul>
 *
 * <p><b>Security:</b>
 *
 * <ul>
 *   <li>Admin-only access enforced by @RolesAllowed("ADMIN") in XentralSettingsResource
 *   <li>apiToken should be encrypted at rest (TODO: future enhancement)
 * </ul>
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.2
 */
@Entity
@Table(
    name = "xentral_settings",
    indexes = {@Index(name = "idx_xentral_settings_singleton", columnList = "singleton_guard", unique = true)})
public class XentralSettings extends PanacheEntityBase {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  /**
   * Xentral ERP API base URL.
   *
   * <p>Example: https://644b6ff97320d.xentral.biz
   */
  @Column(name = "api_url", nullable = false, length = 255)
  private String apiUrl;

  /**
   * Xentral API authentication token.
   *
   * <p>TODO: Encrypt at rest in production (e.g., using Quarkus Vault or AES-256).
   */
  @Column(name = "api_token", nullable = false, length = 500)
  private String apiToken;

  /**
   * Mock mode feature flag.
   *
   * <p>- true: Use MockXentralApiClient (development/testing) - false: Use real Xentral API calls
   * (production)
   */
  @Column(name = "mock_mode", nullable = false)
  private Boolean mockMode = true;

  /**
   * Singleton enforcement field.
   *
   * <p>Always true, enforced by UNIQUE INDEX. Ensures only one configuration row exists.
   */
  @Column(name = "singleton_guard", nullable = false)
  private Boolean singletonGuard = true;

  // Audit Fields
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // ============================================================================
  // JPA Lifecycle Callbacks
  // ============================================================================

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    singletonGuard = true; // Enforce singleton
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  // ============================================================================
  // Business Logic - Singleton Helper
  // ============================================================================

  /**
   * Get the singleton XentralSettings instance.
   *
   * <p>Returns the first (and only) row if exists, null otherwise.
   *
   * @return XentralSettings instance or null
   */
  public static XentralSettings getSingleton() {
    return find("singletonGuard", true).firstResult();
  }

  /**
   * Check if settings exist in database.
   *
   * @return true if settings exist, false otherwise
   */
  public static boolean settingsExist() {
    return count("singletonGuard", true) > 0;
  }

  // ============================================================================
  // Getters and Setters
  // ============================================================================

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getApiUrl() {
    return apiUrl;
  }

  public void setApiUrl(String apiUrl) {
    this.apiUrl = apiUrl;
  }

  public String getApiToken() {
    return apiToken;
  }

  public void setApiToken(String apiToken) {
    this.apiToken = apiToken;
  }

  public Boolean getMockMode() {
    return mockMode;
  }

  public void setMockMode(Boolean mockMode) {
    this.mockMode = mockMode;
  }

  public Boolean getSingletonGuard() {
    return singletonGuard;
  }

  public void setSingletonGuard(Boolean singletonGuard) {
    this.singletonGuard = singletonGuard;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
