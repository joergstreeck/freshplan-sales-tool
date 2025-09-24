package de.freshplan.modules.leads.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User preferences for lead management and territory operations. Sprint 2.1: Configuration for
 * user-specific lead handling, NO geographical restrictions.
 */
@Entity
@Table(name = "user_lead_settings")
public class UserLeadSettings extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @NotNull @Size(max = 50)
  @Column(name = "user_id", nullable = false, unique = true)
  public String userId;

  // Provision settings
  @Column(name = "default_provision_rate", nullable = false, precision = 5, scale = 4)
  public BigDecimal defaultProvisionRate = new BigDecimal("0.0700"); // 7%

  @Column(name = "reduced_provision_rate", nullable = false, precision = 5, scale = 4)
  public BigDecimal reducedProvisionRate = new BigDecimal("0.0200"); // 2%

  // Protection timing
  @Column(name = "lead_protection_months", nullable = false)
  public Integer leadProtectionMonths = 6;

  @Column(name = "activity_reminder_days", nullable = false)
  public Integer activityReminderDays = 60;

  @Column(name = "grace_period_days", nullable = false)
  public Integer gracePeriodDays = 10;

  // Territory preferences (NOT restrictions - leads are available nationwide)
  @ElementCollection
  @CollectionTable(
      name = "user_preferred_territories",
      joinColumns = @JoinColumn(name = "user_settings_id"))
  @Column(name = "territory_id")
  public List<String> preferredTerritories = new ArrayList<>();

  @Column(name = "can_access_all_territories")
  public boolean canAccessAllTerritories = true;

  // Permissions
  @Column(name = "can_stop_clock")
  public boolean canStopClock = false;

  @Column(name = "can_override_protection")
  public boolean canOverrideProtection = false;

  @Column(name = "max_leads_per_month")
  public Integer maxLeadsPerMonth = 100;

  // Settings
  @Column(name = "email_notifications")
  public boolean emailNotifications = true;

  @Column(name = "push_notifications")
  public boolean pushNotifications = false;

  @Column(name = "created_at", nullable = false)
  public LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  public LocalDateTime updatedAt;

  // Business methods
  public boolean hasPreferredTerritory(String territoryId) {
    return preferredTerritories.contains(territoryId);
  }

  public void addPreferredTerritory(String territoryId) {
    if (!preferredTerritories.contains(territoryId)) {
      preferredTerritories.add(territoryId);
    }
  }

  public void removePreferredTerritory(String territoryId) {
    preferredTerritories.remove(territoryId);
  }

  public BigDecimal getProvisionRate(boolean isReduced) {
    return isReduced ? reducedProvisionRate : defaultProvisionRate;
  }

  public static UserLeadSettings findByUserId(String userId) {
    return find("userId", userId).firstResult();
  }

  /**
   * @deprecated Use {@link de.freshplan.modules.leads.service.UserLeadSettingsService#getOrCreateForUser(String)}
   *             instead. This static method cannot properly handle transactions and may cause race conditions.
   * @param userId the user ID
   * @return UserLeadSettings (throws exception to force migration)
   */
  @Deprecated(since = "Sprint 2.1", forRemoval = true)
  public static UserLeadSettings getOrCreateForUser(String userId) {
    throw new UnsupportedOperationException(
        "Use UserLeadSettingsService#getOrCreateForUser instead. " +
        "Static method cannot handle transactions properly.");
  }

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (preferredTerritories.isEmpty()) {
      preferredTerritories.add("DE"); // Default to Germany
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
