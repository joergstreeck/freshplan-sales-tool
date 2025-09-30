package de.freshplan.modules.leads.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.freshplan.modules.leads.domain.UserLeadSettings;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Sprint 2.1.4 Fix: Added @TestTransaction to fix ContextNotActiveException
@QuarkusTest
@TestTransaction
class UserLeadSettingsServiceTest {

  @Inject UserLeadSettingsService service;

  private String testUserId;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean up any existing test data
    UserLeadSettings.deleteAll();
    testUserId = UUID.randomUUID().toString();
  }

  @Test
  void testGetOrCreateForUser_CreatesNewSettings() {
    // When: Getting settings for a new user
    UserLeadSettings settings = service.getOrCreateForUser(testUserId);

    // Then: New settings should be created with defaults
    assertThat(settings).isNotNull();
    assertThat(settings.userId).isEqualTo(testUserId);
    assertThat(settings.defaultProvisionRate).isEqualTo(new BigDecimal("0.0700"));
    assertThat(settings.reducedProvisionRate).isEqualTo(new BigDecimal("0.0200"));
    assertThat(settings.leadProtectionMonths).isEqualTo(6);
    assertThat(settings.activityReminderDays).isEqualTo(60);
    assertThat(settings.gracePeriodDays).isEqualTo(10);
    assertThat(settings.preferredTerritories).contains("DE");
    assertThat(settings.canAccessAllTerritories).isTrue();
    assertThat(settings.emailNotifications).isTrue();
  }

  @Test
  void testGetOrCreateForUser_ReturnsExistingSettings() {
    // Given: Existing settings
    UserLeadSettings created = service.getOrCreateForUser(testUserId);
    Long originalId = created.id;

    // When: Getting settings for the same user again
    UserLeadSettings retrieved = service.getOrCreateForUser(testUserId);

    // Then: Should return the same settings
    assertThat(retrieved.id).isEqualTo(originalId);
    assertThat(retrieved.userId).isEqualTo(testUserId);
  }

  @Test
  void testUpdateSettings() {
    // Given: Existing settings
    UserLeadSettings original = service.getOrCreateForUser(testUserId);

    // When: Updating settings
    UserLeadSettings updates = new UserLeadSettings();
    updates.defaultProvisionRate = new BigDecimal("0.0800");
    updates.reducedProvisionRate = new BigDecimal("0.0300");
    updates.leadProtectionMonths = 12;
    updates.activityReminderDays = 90;
    updates.gracePeriodDays = 15;
    updates.preferredTerritories = List.of("DE", "CH");
    updates.canAccessAllTerritories = false;
    updates.canStopClock = true;
    updates.canOverrideProtection = true;
    updates.maxLeadsPerMonth = 200;
    updates.emailNotifications = false;
    updates.pushNotifications = true;

    UserLeadSettings updated = service.updateSettings(testUserId, updates);

    // Then: Settings should be updated
    assertThat(updated.id).isEqualTo(original.id);
    assertThat(updated.defaultProvisionRate).isEqualTo(new BigDecimal("0.0800"));
    assertThat(updated.reducedProvisionRate).isEqualTo(new BigDecimal("0.0300"));
    assertThat(updated.leadProtectionMonths).isEqualTo(12);
    assertThat(updated.activityReminderDays).isEqualTo(90);
    assertThat(updated.gracePeriodDays).isEqualTo(15);
    assertThat(updated.preferredTerritories).containsExactlyInAnyOrder("DE", "CH");
    assertThat(updated.canAccessAllTerritories).isFalse();
    assertThat(updated.canStopClock).isTrue();
    assertThat(updated.canOverrideProtection).isTrue();
    assertThat(updated.maxLeadsPerMonth).isEqualTo(200);
    assertThat(updated.emailNotifications).isFalse();
    assertThat(updated.pushNotifications).isTrue();
  }

  @Test
  void testUpdateSettings_ThrowsExceptionForNonExistentUser() {
    // Given: Non-existent user
    String nonExistentUserId = UUID.randomUUID().toString();
    UserLeadSettings updates = new UserLeadSettings();

    // When/Then: Should throw exception
    assertThatThrownBy(() -> service.updateSettings(nonExistentUserId, updates))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("UserLeadSettings not found");
  }

  @Test
  void testDeleteSettings() {
    // Given: Existing settings
    service.getOrCreateForUser(testUserId);

    // When: Deleting settings
    boolean deleted = service.deleteSettings(testUserId);

    // Then: Should be deleted
    assertThat(deleted).isTrue();
    assertThat(UserLeadSettings.findByUserId(testUserId)).isNull();
  }

  @Test
  void testDeleteSettings_ReturnsFalseForNonExistentUser() {
    // Given: Non-existent user
    String nonExistentUserId = UUID.randomUUID().toString();

    // When: Trying to delete non-existent settings
    boolean deleted = service.deleteSettings(nonExistentUserId);

    // Then: Should return false
    assertThat(deleted).isFalse();
  }

  @Test
  void testStaticMethodThrowsException() {
    // When/Then: Static method should throw exception
    assertThatThrownBy(() -> UserLeadSettings.getOrCreateForUser(testUserId))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessageContaining("Use UserLeadSettingsService#getOrCreateForUser instead");
  }

  @Test
  @Transactional
  void testConcurrentCreation_HandledGracefully() {
    // Given: Pre-create settings to simulate race condition
    UserLeadSettings preCreated = new UserLeadSettings();
    preCreated.userId = testUserId;
    preCreated.persist();

    // When: Service tries to create (simulating concurrent creation)
    UserLeadSettings result = service.getOrCreateForUser(testUserId);

    // Then: Should return existing settings
    assertThat(result).isNotNull();
    assertThat(result.userId).isEqualTo(testUserId);
    assertThat(UserLeadSettings.count("userId", testUserId)).isEqualTo(1);
  }
}
