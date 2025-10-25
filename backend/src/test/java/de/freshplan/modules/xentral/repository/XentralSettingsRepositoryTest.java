package de.freshplan.modules.xentral.repository;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.modules.xentral.entity.XentralSettings;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.junit.jupiter.api.*;

/**
 * Integration Tests for XentralSettingsRepository.
 *
 * <p>Sprint 2.1.7.2 - Enterprise-Grade Test Coverage (Repository Layer)
 *
 * <p><b>Test Strategy:</b> Integration tests with real H2 database
 *
 * <p><b>Bug Detection Focus:</b>
 *
 * <ul>
 *   <li>UPSERT Logic: Ensure createOrUpdate doesn't create duplicates
 *   <li>Singleton Constraint: Only one settings row allowed
 *   <li>DB Operations: Ensure persistence works correctly
 * </ul>
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.2
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class XentralSettingsRepositoryTest {

  @Inject XentralSettingsRepository repository;

  private static final String TEST_URL = "https://test.xentral.biz";
  private static final String TEST_TOKEN = "test-token-12345";
  private static final String UPDATED_URL = "https://updated.xentral.biz";
  private static final String UPDATED_TOKEN = "updated-token-67890";

  // Clean DB before each test
  @BeforeEach
  @Transactional
  void cleanDatabase() {
    repository.deleteAll();
  }

  // ============================================================================
  // getSingleton() - Returns Optional<Entity>
  // ============================================================================

  @Test
  @Order(1)
  @DisplayName("getSingleton() - DB empty → Optional.empty()")
  @Transactional
  void testGetSingleton_DbEmpty_ReturnsEmptyOptional() {
    // WHEN
    Optional<XentralSettings> result = repository.getSingleton();

    // THEN
    assertFalse(result.isPresent(), "Should return empty Optional when DB is empty");
  }

  @Test
  @Order(2)
  @DisplayName("getSingleton() - DB has data → Optional.of(entity)")
  @Transactional
  void testGetSingleton_DbHasData_ReturnsEntity() {
    // GIVEN: Insert settings
    repository.createOrUpdate(TEST_URL, TEST_TOKEN, true);

    // WHEN
    Optional<XentralSettings> result = repository.getSingleton();

    // THEN
    assertTrue(result.isPresent(), "Should return Optional with entity");
    assertEquals(TEST_URL, result.get().getApiUrl());
    assertEquals(TEST_TOKEN, result.get().getApiToken());
    assertTrue(result.get().getMockMode());
  }

  // ============================================================================
  // settingsExist() - Boolean check
  // ============================================================================

  @Test
  @Order(3)
  @DisplayName("settingsExist() - DB empty → false")
  @Transactional
  void testSettingsExist_DbEmpty_ReturnsFalse() {
    // WHEN
    boolean exists = repository.settingsExist();

    // THEN
    assertFalse(exists, "Should return false when DB is empty");
  }

  @Test
  @Order(4)
  @DisplayName("settingsExist() - DB has data → true")
  @Transactional
  void testSettingsExist_DbHasData_ReturnsTrue() {
    // GIVEN: Insert settings
    repository.createOrUpdate(TEST_URL, TEST_TOKEN, true);

    // WHEN
    boolean exists = repository.settingsExist();

    // THEN
    assertTrue(exists, "Should return true when DB has data");
  }

  // ============================================================================
  // createOrUpdate() - UPSERT Logic (CRITICAL BUG DETECTION!)
  // ============================================================================

  @Test
  @Order(5)
  @DisplayName("createOrUpdate() - First time → CREATE (count = 1)")
  @Transactional
  void testCreateOrUpdate_FirstTime_CreatesNewEntity() {
    // WHEN
    XentralSettings result = repository.createOrUpdate(TEST_URL, TEST_TOKEN, true);

    // THEN
    assertNotNull(result);
    assertNotNull(result.getId(), "ID should be generated");
    assertEquals(TEST_URL, result.getApiUrl());
    assertEquals(TEST_TOKEN, result.getApiToken());
    assertTrue(result.getMockMode());

    // Verify only ONE row exists
    long count = repository.count();
    assertEquals(1, count, "Should have exactly ONE row after first createOrUpdate()");
  }

  @Test
  @Order(6)
  @DisplayName("createOrUpdate() - Second time → UPDATE (count STILL 1, no duplicates!)")
  @Transactional
  void testCreateOrUpdate_SecondTime_UpdatesExistingEntity() {
    // GIVEN: First insert
    XentralSettings first = repository.createOrUpdate(TEST_URL, TEST_TOKEN, true);
    long firstCount = repository.count();

    // WHEN: Second insert with different values
    XentralSettings second = repository.createOrUpdate(UPDATED_URL, UPDATED_TOKEN, false);

    // THEN: Should UPDATE, not CREATE
    assertNotNull(second);
    assertEquals(first.getId(), second.getId(), "ID should be SAME (updated, not created!)");
    assertEquals(UPDATED_URL, second.getApiUrl(), "URL should be updated");
    assertEquals(UPDATED_TOKEN, second.getApiToken(), "Token should be updated");
    assertFalse(second.getMockMode(), "MockMode should be updated");

    // CRITICAL: Verify STILL only ONE row (no duplicates!)
    long secondCount = repository.count();
    assertEquals(
        1, secondCount, "Should STILL have exactly ONE row after second createOrUpdate()!");
    assertEquals(firstCount, secondCount, "Row count should NOT increase (UPSERT, not INSERT!)");
  }

  @Test
  @Order(7)
  @DisplayName("createOrUpdate() - Third time → UPDATE (count STILL 1)")
  @Transactional
  void testCreateOrUpdate_ThirdTime_StillUpdatesWithoutDuplicates() {
    // GIVEN: Two previous inserts
    repository.createOrUpdate(TEST_URL, TEST_TOKEN, true);
    repository.createOrUpdate(UPDATED_URL, UPDATED_TOKEN, false);

    // WHEN: Third insert
    XentralSettings third =
        repository.createOrUpdate("https://third.xentral.biz", "third-token", true);

    // THEN: Should UPDATE, not CREATE
    assertNotNull(third);
    assertEquals("https://third.xentral.biz", third.getApiUrl());
    assertEquals("third-token", third.getApiToken());
    assertTrue(third.getMockMode());

    // CRITICAL: Verify STILL only ONE row
    long count = repository.count();
    assertEquals(1, count, "Should STILL have exactly ONE row after third createOrUpdate()!");
  }

  // ============================================================================
  // Singleton Pattern - Verify singleton_guard constraint
  // ============================================================================

  @Test
  @Order(8)
  @DisplayName("Singleton Constraint - createOrUpdate ensures only ONE row exists")
  @Transactional
  void testSingletonConstraint_MultipleCreateOrUpdate_OnlyOneRow() {
    // WHEN: Multiple createOrUpdate calls
    repository.createOrUpdate(TEST_URL, TEST_TOKEN, true);
    repository.createOrUpdate(UPDATED_URL, UPDATED_TOKEN, false);
    repository.createOrUpdate("https://final.xentral.biz", "final-token", true);

    // THEN: Only ONE row should exist
    long count = repository.count();
    assertEquals(1, count, "Singleton pattern: Only ONE row should exist!");

    // Verify it's the LAST updated values
    Optional<XentralSettings> settings = repository.getSingleton();
    assertTrue(settings.isPresent());
    assertEquals("https://final.xentral.biz", settings.get().getApiUrl());
    assertEquals("final-token", settings.get().getApiToken());
  }
}
