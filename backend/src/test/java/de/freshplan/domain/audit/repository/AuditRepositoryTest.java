package de.freshplan.domain.audit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.entity.AuditSource;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for AuditRepository
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
class AuditRepositoryTest {

  @Inject AuditRepository auditRepository;

  private UUID testEntityId;
  private UUID testUserId;

  @BeforeEach
  void setUp() {
    testEntityId = UUID.randomUUID();
    testUserId = UUID.randomUUID();
  }
  
  @TestTransaction
  void cleanupBeforeTest() {
    // Clean up any existing test data completely for THIS test
    auditRepository.deleteAll();
    auditRepository.flush();
  }

  @Test
  @TestTransaction
  void testFindByEntity() {
    // Given
    createAuditEntry("opportunity", testEntityId, AuditEventType.OPPORTUNITY_CREATED);
    createAuditEntry("opportunity", testEntityId, AuditEventType.OPPORTUNITY_UPDATED);
    createAuditEntry("customer", UUID.randomUUID(), AuditEventType.CUSTOMER_CREATED);

    // When
    List<AuditEntry> entries = auditRepository.findByEntity("opportunity", testEntityId);

    // Then
    assertThat(entries).hasSize(2);
    assertThat(entries).extracting(AuditEntry::getEntityType).containsOnly("opportunity");
    assertThat(entries).extracting(AuditEntry::getEntityId).containsOnly(testEntityId);
  }

  @Test
  @TestTransaction
  void testFindByUser() {
    // Given
    Instant now = Instant.now();
    Instant yesterday = now.minus(1, ChronoUnit.DAYS);
    Instant tomorrow = now.plus(1, ChronoUnit.DAYS);

    createAuditEntryWithUser(testUserId, AuditEventType.LOGIN_SUCCESS);
    createAuditEntryWithUser(testUserId, AuditEventType.OPPORTUNITY_CREATED);
    createAuditEntryWithUser(UUID.randomUUID(), AuditEventType.CUSTOMER_CREATED);

    // When
    List<AuditEntry> entries = auditRepository.findByUser(testUserId, yesterday, tomorrow);

    // Then
    assertThat(entries).hasSize(2);
    assertThat(entries).extracting(AuditEntry::getUserId).containsOnly(testUserId);
  }

  @Test
  @TestTransaction
  void testFindSecurityEvents() {
    cleanupBeforeTest();
    
    // Given
    Instant now = Instant.now();
    Instant anHourAgo = now.minus(1, ChronoUnit.HOURS);

    createAuditEntry("security", UUID.randomUUID(), AuditEventType.LOGIN_SUCCESS);
    createAuditEntry("security", UUID.randomUUID(), AuditEventType.PERMISSION_DENIED);
    createAuditEntry("security", UUID.randomUUID(), AuditEventType.ROLE_ASSIGNED);
    createAuditEntry("opportunity", UUID.randomUUID(), AuditEventType.OPPORTUNITY_CREATED);

    // When
    List<AuditEntry> entries = auditRepository.findSecurityEvents(anHourAgo, now);

    // Then
    assertThat(entries).hasSize(3);
    assertThat(entries)
        .extracting(AuditEntry::getEventType)
        .containsExactlyInAnyOrder(
            AuditEventType.LOGIN_SUCCESS,
            AuditEventType.PERMISSION_DENIED,
            AuditEventType.ROLE_ASSIGNED);
  }

  @Test
  @TestTransaction
  void testFindFailures() {
    cleanupBeforeTest();
    
    // Given
    Instant now = Instant.now();
    Instant anHourAgo = now.minus(1, ChronoUnit.HOURS);

    createAuditEntry("api", UUID.randomUUID(), AuditEventType.API_ERROR);
    createAuditEntry("security", UUID.randomUUID(), AuditEventType.LOGIN_FAILURE);
    createAuditEntry("security", UUID.randomUUID(), AuditEventType.PERMISSION_DENIED);
    createAuditEntry("opportunity", UUID.randomUUID(), AuditEventType.OPPORTUNITY_CREATED);

    // When
    List<AuditEntry> entries = auditRepository.findFailures(anHourAgo, now);

    // Then
    assertThat(entries).hasSize(3);
    assertThat(entries)
        .extracting(AuditEntry::getEventType)
        .allMatch(
            type ->
                type.name().contains("FAILURE")
                    || type.name().contains("DENIED")
                    || type.name().contains("ERROR"));
  }

  @Test
  @TestTransaction
  void testAdvancedSearch() {
    // Given
    createAuditEntry("opportunity", testEntityId, AuditEventType.OPPORTUNITY_CREATED);
    createAuditEntry("opportunity", testEntityId, AuditEventType.OPPORTUNITY_UPDATED);
    createAuditEntry("customer", testEntityId, AuditEventType.CUSTOMER_CREATED);

    var criteria =
        AuditRepository.AuditSearchCriteria.builder()
            .entityType("opportunity")
            .entityId(testEntityId)
            .eventTypes(List.of(AuditEventType.OPPORTUNITY_CREATED))
            .page(0)
            .size(10)
            .build();

    // When
    List<AuditEntry> entries = auditRepository.search(criteria);

    // Then
    assertThat(entries).hasSize(1);
    assertThat(entries.get(0).getEventType()).isEqualTo(AuditEventType.OPPORTUNITY_CREATED);
  }

  @Test
  @TestTransaction
  void testGetStatistics() {
    cleanupBeforeTest();
    
    // Given
    Instant now = Instant.now();
    Instant yesterday = now.minus(1, ChronoUnit.DAYS);

    createAuditEntry("opportunity", UUID.randomUUID(), AuditEventType.OPPORTUNITY_CREATED);
    createAuditEntry("opportunity", UUID.randomUUID(), AuditEventType.OPPORTUNITY_UPDATED);
    createAuditEntry("api", UUID.randomUUID(), AuditEventType.API_ERROR);
    createAuditEntryWithUser(testUserId, AuditEventType.LOGIN_SUCCESS);
    createAuditEntryWithUser(UUID.randomUUID(), AuditEventType.LOGIN_SUCCESS);

    // When
    var stats = auditRepository.getStatistics(yesterday, now);

    // Then
    assertThat(stats.getTotalEvents()).isEqualTo(5L);
    assertThat(stats.getUniqueUsers()).isEqualTo(2L);
    assertThat(stats.getFailureCount()).isEqualTo(1L);
  }

  @Test
  @TestTransaction
  void testHashChaining() {
    cleanupBeforeTest();
    
    // Given - Create first entry with proper hash chain
    createAuditEntryWithPreviousHash("test", UUID.randomUUID(), AuditEventType.SYSTEM_STARTUP, null);

    // When
    var lastHash = auditRepository.getLastHash();

    // Then
    assertThat(lastHash).isPresent();
    assertThat(lastHash.get()).hasSize(64); // SHA-256 hex

    // Create another entry and verify previous hash is set
    createAuditEntryWithPreviousHash("test", UUID.randomUUID(), AuditEventType.SYSTEM_SHUTDOWN, lastHash.get());

    List<AuditEntry> allEntries = auditRepository.listAll();
    assertThat(allEntries).hasSize(2);

    // The second entry should reference the first entry's hash
    AuditEntry secondEntry =
        allEntries.stream()
            .filter(e -> e.getEventType() == AuditEventType.SYSTEM_SHUTDOWN)
            .findFirst()
            .orElseThrow();

    assertThat(secondEntry.getPreviousHash()).isEqualTo(lastHash.get());
  }

  @Test
  @TestTransaction
  void testPagination() {
    // Given - create 25 entries with the same entity
    UUID sharedEntityId = UUID.randomUUID();
    for (int i = 0; i < 25; i++) {
      createAuditEntry("test", sharedEntityId, AuditEventType.OPPORTUNITY_CREATED);
    }

    // When - get first page
    List<AuditEntry> page1 = auditRepository.findByEntity("test", sharedEntityId, 0, 10);

    // When - get second page
    List<AuditEntry> page2 = auditRepository.findByEntity("test", sharedEntityId, 1, 10);

    // Then
    assertThat(page1).hasSize(10);
    assertThat(page2).hasSize(10);
    assertThat(page1).doesNotContainAnyElementsOf(page2);
  }

  // Helper methods

  private void createAuditEntry(String entityType, UUID entityId, AuditEventType eventType) {
    AuditEntry entry =
        AuditEntry.builder()
            .eventType(eventType)
            .entityType(entityType)
            .entityId(entityId)
            .userId(testUserId)
            .userName("Test User")
            .userRole("admin")
            .source(AuditSource.TEST)
            .dataHash(generateHash())
            .timestamp(Instant.now())
            .build();

    auditRepository.persist(entry);
    auditRepository.flush(); // Ensure data is committed
  }

  private void createAuditEntryWithUser(UUID userId, AuditEventType eventType) {
    AuditEntry entry =
        AuditEntry.builder()
            .eventType(eventType)
            .entityType("test")
            .entityId(UUID.randomUUID())
            .userId(userId)
            .userName("User " + userId)
            .userRole("user")
            .source(AuditSource.TEST)
            .dataHash(generateHash())
            .timestamp(Instant.now())
            .build();

    auditRepository.persist(entry);
    auditRepository.flush(); // Ensure data is committed
  }

  private void createAuditEntryWithPreviousHash(String entityType, UUID entityId, AuditEventType eventType, String previousHash) {
    AuditEntry entry =
        AuditEntry.builder()
            .eventType(eventType)
            .entityType(entityType)
            .entityId(entityId)
            .userId(testUserId)
            .userName("Test User")
            .userRole("admin")
            .source(AuditSource.TEST)
            .dataHash(generateHash())
            .previousHash(previousHash)
            .timestamp(Instant.now())
            .build();

    auditRepository.persist(entry);
    auditRepository.flush(); // Ensure data is committed
  }

  private String generateHash() {
    return UUID.randomUUID().toString().replace("-", "")
        + UUID.randomUUID().toString().replace("-", "");
  }
}
