package de.freshplan.audit.repository;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.audit.entity.AuditLog;
import de.freshplan.audit.entity.AuditLog.*;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("migrate")
@TestTransaction
@DisplayName("AuditRepository Tests")
class AuditRepositoryTest {

  @Inject AuditRepository auditRepository;

  private UUID testUserId;
  private UUID testEntityId;
  private LocalDateTime testTime;

  @BeforeEach
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  void setUp() {
    testUserId = UUID.randomUUID();
    testEntityId = UUID.randomUUID();
    testTime = LocalDateTime.now();

    // Phase 5C Fix: Separate transaction (REQUIRES_NEW) commits BEFORE test transaction
    // This prevents test data leakage while avoiding deadlocks
    auditRepository.deleteAll();
  }

  @Test
  @DisplayName("Should save and retrieve audit log")
  void testSaveAndRetrieve() {
    // Given
    AuditLog auditLog = createTestAuditLog(AuditAction.CREATE);

    // When
    auditRepository.persist(auditLog);
    Optional<AuditLog> retrieved = auditRepository.findByIdOptional(auditLog.getId());

    // Then
    assertTrue(retrieved.isPresent());
    assertEquals(auditLog.getEntityType(), retrieved.get().getEntityType());
    assertEquals(auditLog.getEntityId(), retrieved.get().getEntityId());
    assertEquals(auditLog.getAction(), retrieved.get().getAction());
  }

  @Test
  @DisplayName("Should find audit logs by entity")
  void testFindByEntity() {
    // Given
    AuditLog log1 = createTestAuditLog(AuditAction.CREATE);
    AuditLog log2 = createTestAuditLog(AuditAction.UPDATE);
    AuditLog log3 = createTestAuditLog(AuditAction.DELETE);
    log3.setEntityId(UUID.randomUUID()); // Different entity

    auditRepository.persist(log1);
    auditRepository.persist(log2);
    auditRepository.persist(log3);

    // When
    List<AuditLog> results = auditRepository.findByEntity(EntityType.CUSTOMER, testEntityId);

    // Then
    assertEquals(2, results.size());
    assertTrue(results.stream().allMatch(log -> log.getEntityId().equals(testEntityId)));
  }

  @Test
  @DisplayName("Should find audit logs by user")
  void testFindByUser() {
    // Given
    AuditLog log1 = createTestAuditLog(AuditAction.CREATE);
    AuditLog log2 = createTestAuditLog(AuditAction.UPDATE);
    UUID differentUserId = UUID.randomUUID();
    log2.setUserId(differentUserId); // Different user

    auditRepository.persist(log1);
    auditRepository.persist(log2);

    // When
    List<AuditLog> results = auditRepository.findByUser(testUserId);

    // Then
    assertEquals(1, results.size());
    assertEquals(testUserId, results.get(0).getUserId());
  }

  @Test
  @DisplayName("Should find audit logs in time period")
  void testFindInPeriod() {
    // Given
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);

    AuditLog todayLog = createTestAuditLog(AuditAction.CREATE);
    AuditLog oldLog = createTestAuditLog(AuditAction.UPDATE);
    oldLog.setOccurredAt(LocalDateTime.now().minusDays(2));

    auditRepository.persist(todayLog);
    auditRepository.persist(oldLog);

    // When
    List<AuditLog> results = auditRepository.findInPeriod(yesterday, tomorrow);

    // Then
    assertEquals(1, results.size());
    assertEquals(todayLog.getId(), results.get(0).getId());
  }

  @Test
  @DisplayName("Should find critical actions")
  void testFindCriticalActions() {
    // Given
    AuditLog criticalLog = createTestAuditLog(AuditAction.DELETE);
    AuditLog normalLog = createTestAuditLog(AuditAction.VIEW);

    auditRepository.persist(criticalLog);
    auditRepository.persist(normalLog);

    // When
    List<AuditLog> results = auditRepository.findCriticalActions(LocalDateTime.now().minusHours(1));

    // Then
    assertEquals(1, results.size());
    assertEquals(AuditAction.DELETE, results.get(0).getAction());
  }

  @Test
  @DisplayName("Should find DSGVO relevant entries")
  void testFindDsgvoRelevant() {
    // Given
    AuditLog dsgvoLog = createTestAuditLog(AuditAction.CONSENT_GIVEN);
    dsgvoLog.setIsDsgvoRelevant(true);
    dsgvoLog.setLegalBasis(LegalBasis.CONSENT);

    AuditLog normalLog = createTestAuditLog(AuditAction.CREATE);
    normalLog.setIsDsgvoRelevant(false);

    auditRepository.persist(dsgvoLog);
    auditRepository.persist(normalLog);

    // When
    List<AuditLog> results =
        auditRepository.findDsgvoRelevant(
            LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

    // Then
    assertEquals(1, results.size());
    assertTrue(results.get(0).getIsDsgvoRelevant());
  }

  @Test
  @DisplayName("Should find by transaction ID")
  void testFindByTransactionId() {
    // Given
    String transactionId = "txn-123";
    AuditLog log1 = createTestAuditLog(AuditAction.CREATE);
    log1.setTransactionId(transactionId);
    AuditLog log2 = createTestAuditLog(AuditAction.UPDATE);
    log2.setTransactionId(transactionId);
    AuditLog log3 = createTestAuditLog(AuditAction.DELETE);
    log3.setTransactionId("txn-456");

    auditRepository.persist(log1);
    auditRepository.persist(log2);
    auditRepository.persist(log3);

    // When
    List<AuditLog> results = auditRepository.findByTransactionId(transactionId);

    // Then
    assertEquals(2, results.size());
    assertTrue(results.stream().allMatch(log -> transactionId.equals(log.getTransactionId())));
  }

  @Test
  @DisplayName("Should count user actions")
  void testCountUserActions() {
    // Given
    AuditLog create1 = createTestAuditLog(AuditAction.CREATE);
    AuditLog create2 = createTestAuditLog(AuditAction.CREATE);
    AuditLog update = createTestAuditLog(AuditAction.UPDATE);
    AuditLog delete = createTestAuditLog(AuditAction.DELETE);

    auditRepository.persist(create1);
    auditRepository.persist(create2);
    auditRepository.persist(update);
    auditRepository.persist(delete);

    // When
    Map<AuditAction, Long> counts =
        auditRepository.countUserActions(testUserId, LocalDateTime.now().minusHours(1));

    // Then
    assertEquals(2L, counts.get(AuditAction.CREATE));
    assertEquals(1L, counts.get(AuditAction.UPDATE));
    assertEquals(1L, counts.get(AuditAction.DELETE));
  }

  @Test
  @DisplayName("Should verify hash chain integrity")
  void testVerifyHashChain() {
    // Given
    AuditLog log1 = createTestAuditLog(AuditAction.CREATE);
    log1.setPreviousHash("GENESIS");
    log1.setCurrentHash("hash1");
    log1.setOccurredAt(LocalDateTime.now().minusMinutes(2));

    AuditLog log2 = createTestAuditLog(AuditAction.UPDATE);
    log2.setPreviousHash("hash1");
    log2.setCurrentHash("hash2");
    log2.setOccurredAt(LocalDateTime.now().minusMinutes(1));

    AuditLog log3 = createTestAuditLog(AuditAction.DELETE);
    log3.setPreviousHash("hash2");
    log3.setCurrentHash("hash3");
    log3.setOccurredAt(LocalDateTime.now());

    auditRepository.persist(log1);
    auditRepository.persist(log2);
    auditRepository.persist(log3);

    // When
    boolean isValid =
        auditRepository.verifyHashChain(
            LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));

    // Then
    assertTrue(isValid);
  }

  @Test
  @DisplayName("Should detect broken hash chain")
  void testDetectBrokenHashChain() {
    // Given
    AuditLog log1 = createTestAuditLog(AuditAction.CREATE);
    log1.setPreviousHash("GENESIS");
    log1.setCurrentHash("hash1");
    log1.setOccurredAt(LocalDateTime.now().minusMinutes(2));

    AuditLog log2 = createTestAuditLog(AuditAction.UPDATE);
    log2.setPreviousHash("WRONG_HASH"); // Broken chain
    log2.setCurrentHash("hash2");
    log2.setOccurredAt(LocalDateTime.now().minusMinutes(1));

    auditRepository.persist(log1);
    auditRepository.persist(log2);

    // When
    boolean isValid =
        auditRepository.verifyHashChain(
            LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));

    // Then
    assertFalse(isValid);
  }

  @Test
  @DisplayName("Should find expiring entries")
  void testFindExpiringEntries() {
    // Given
    AuditLog expiringLog = createTestAuditLog(AuditAction.VIEW);
    expiringLog.setRetentionUntil(LocalDateTime.now().plusDays(5));

    AuditLog futureLog = createTestAuditLog(AuditAction.CREATE);
    futureLog.setRetentionUntil(LocalDateTime.now().plusDays(100));

    auditRepository.persist(expiringLog);
    auditRepository.persist(futureLog);

    // When
    List<AuditLog> results = auditRepository.findExpiringEntries(10);

    // Then
    assertEquals(1, results.size());
    assertEquals(expiringLog.getId(), results.get(0).getId());
  }

  @Test
  @DisplayName("Should get dashboard statistics")
  void testGetDashboardStatistics() {
    // Given
    createAndPersistTestData();

    // When
    Map<String, Object> stats =
        auditRepository.getDashboardStatistics(
            LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

    // Then
    assertNotNull(stats);
    assertTrue((Long) stats.get("totalEvents") > 0);
    assertNotNull(stats.get("criticalEvents"));
    assertNotNull(stats.get("dsgvoRelevant"));
    assertNotNull(stats.get("activeUsers"));
    assertNotNull(stats.get("actionsByType"));
  }

  @Test
  @DisplayName("Should find last entry for hash chain")
  void testFindLastEntry() {
    // Given
    AuditLog log1 = createTestAuditLog(AuditAction.CREATE);
    log1.setOccurredAt(LocalDateTime.now().minusMinutes(2));

    AuditLog log2 = createTestAuditLog(AuditAction.UPDATE);
    log2.setOccurredAt(LocalDateTime.now().minusMinutes(1));

    AuditLog log3 = createTestAuditLog(AuditAction.DELETE);
    log3.setOccurredAt(LocalDateTime.now());

    auditRepository.persist(log1);
    auditRepository.persist(log2);
    auditRepository.persist(log3);

    // When
    Optional<AuditLog> lastEntry = auditRepository.findLastEntry();

    // Then
    assertTrue(lastEntry.isPresent());
    assertEquals(log3.getId(), lastEntry.get().getId());
  }

  @Test
  @DisplayName("Should search with pagination and filters")
  void testSearchWithPagination() {
    // Given
    createAndPersistTestData();

    // When
    List<AuditLog> results =
        auditRepository.search(
            EntityType.CUSTOMER,
            null,
            null,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1),
            0,
            2);

    // Then
    assertEquals(2, results.size()); // Page size is 2
    assertTrue(results.stream().allMatch(log -> log.getEntityType() == EntityType.CUSTOMER));
  }

  @Test
  @DisplayName("Should find failed login attempts")
  void testFindFailedLogins() {
    // Given
    AuditLog failedLogin1 = createTestAuditLog(AuditAction.FAILED_LOGIN);
    AuditLog failedLogin2 = createTestAuditLog(AuditAction.FAILED_LOGIN);
    AuditLog successLogin = createTestAuditLog(AuditAction.LOGIN);

    auditRepository.persist(failedLogin1);
    auditRepository.persist(failedLogin2);
    auditRepository.persist(successLogin);

    // When
    List<AuditLog> results =
        auditRepository.findFailedLogins(testUserId, LocalDateTime.now().minusHours(1));

    // Then
    assertEquals(2, results.size());
    assertTrue(results.stream().allMatch(log -> log.getAction() == AuditAction.FAILED_LOGIN));
  }

  // Helper Methods

  private AuditLog createTestAuditLog(AuditAction action) {
    AuditLog log = new AuditLog();
    log.setEntityType(EntityType.CUSTOMER);
    log.setEntityId(testEntityId);
    log.setEntityName("Test Customer");
    log.setAction(action);
    log.setUserId(testUserId); // This will convert UUID to String internally
    log.setUserName("test.user");
    log.setUserRole("admin");
    log.setOccurredAt(testTime);
    log.setIpAddress("192.168.1.100");
    log.setUserAgent("Test Agent");
    return log;
  }

  private void createAndPersistTestData() {
    // Create various types of audit logs
    AuditLog create = createTestAuditLog(AuditAction.CREATE);
    AuditLog update = createTestAuditLog(AuditAction.UPDATE);
    AuditLog delete = createTestAuditLog(AuditAction.DELETE);
    delete.setIsDsgvoRelevant(true);
    AuditLog consent = createTestAuditLog(AuditAction.CONSENT_GIVEN);
    consent.setIsDsgvoRelevant(true);

    auditRepository.persist(create);
    auditRepository.persist(update);
    auditRepository.persist(delete);
    auditRepository.persist(consent);
  }
}
