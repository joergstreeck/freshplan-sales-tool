package de.freshplan.modules.leads.service;

import de.freshplan.modules.leads.domain.UserLeadSettings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

/**
 * Service for managing UserLeadSettings with proper transaction handling.
 * Replaces the static method in UserLeadSettings to ensure thread-safety
 * and proper CDI proxy support for @Transactional.
 */
@ApplicationScoped
public class UserLeadSettingsService {

  private static final Logger LOG = Logger.getLogger(UserLeadSettingsService.class);

  /**
   * Get or create UserLeadSettings for a user with proper transaction handling.
   * Uses pessimistic locking to prevent race conditions.
   *
   * @param userId the user ID
   * @return existing or newly created settings
   */
  @Transactional
  public UserLeadSettings getOrCreateForUser(String userId) {
    // Try to find existing settings with pessimistic lock to prevent concurrent creation
    UserLeadSettings existing = UserLeadSettings.find("userId", userId)
        .withLock(LockModeType.PESSIMISTIC_WRITE)
        .firstResult();

    if (existing != null) {
      LOG.debugf("Found existing UserLeadSettings for user: %s", userId);
      return existing;
    }

    // Create new settings if not found
    try {
      UserLeadSettings settings = new UserLeadSettings();
      settings.userId = userId;
      // Default values are set in the entity constructor/field initialization
      settings.persistAndFlush();

      LOG.infof("Created new UserLeadSettings for user: %s", userId);
      return settings;

    } catch (PersistenceException e) {
      // Handle the race condition where another thread created the settings
      // between our check and insert (unique constraint violation)
      LOG.debugf("Concurrent creation detected for user %s, fetching existing", userId);

      UserLeadSettings settings = UserLeadSettings.find("userId", userId).firstResult();
      if (settings != null) {
        return settings;
      }

      // If still not found, something else went wrong
      LOG.errorf("Failed to create or find UserLeadSettings for user %s", userId);
      throw e;
    }
  }

  /**
   * Update user lead settings.
   *
   * @param userId the user ID
   * @param settings the settings to update
   * @return updated settings
   */
  @Transactional
  public UserLeadSettings updateSettings(String userId, UserLeadSettings settings) {
    UserLeadSettings existing = UserLeadSettings.find("userId", userId)
        .withLock(LockModeType.PESSIMISTIC_WRITE)
        .firstResult();

    if (existing == null) {
      throw new IllegalArgumentException("UserLeadSettings not found for user: " + userId);
    }

    // Update fields
    existing.defaultProvisionRate = settings.defaultProvisionRate;
    existing.reducedProvisionRate = settings.reducedProvisionRate;
    existing.leadProtectionMonths = settings.leadProtectionMonths;
    existing.activityReminderDays = settings.activityReminderDays;
    existing.gracePeriodDays = settings.gracePeriodDays;
    existing.preferredTerritories = settings.preferredTerritories;
    existing.canAccessAllTerritories = settings.canAccessAllTerritories;
    existing.canStopClock = settings.canStopClock;
    existing.canOverrideProtection = settings.canOverrideProtection;
    existing.maxLeadsPerMonth = settings.maxLeadsPerMonth;
    existing.emailNotifications = settings.emailNotifications;
    existing.pushNotifications = settings.pushNotifications;

    existing.persist();

    LOG.infof("Updated UserLeadSettings for user: %s", userId);
    return existing;
  }

  /**
   * Delete user lead settings.
   *
   * @param userId the user ID
   * @return true if deleted, false if not found
   */
  @Transactional
  public boolean deleteSettings(String userId) {
    long deleted = UserLeadSettings.delete("userId", userId);
    if (deleted > 0) {
      LOG.infof("Deleted UserLeadSettings for user: %s", userId);
      return true;
    }
    return false;
  }
}