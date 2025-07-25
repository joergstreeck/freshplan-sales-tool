package de.freshplan.domain.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.entity.AuditSource;
import de.freshplan.domain.audit.repository.AuditRepository;
import de.freshplan.domain.audit.service.dto.AuditContext;
import de.freshplan.shared.util.SecurityUtils;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for AuditService
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@TestSecurity(
    user = "testuser",
    roles = {"admin"})
class AuditServiceTest {

  @Inject AuditService auditService;

  @Inject AuditRepository auditRepository;

  @InjectMock SecurityUtils securityUtils;

  private UUID testUserId;
  private UUID testEntityId;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    testEntityId = UUID.randomUUID();

    // Setup security mocks
    when(securityUtils.getCurrentUserId()).thenReturn(testUserId);
    when(securityUtils.getCurrentUserName()).thenReturn("Test User");
    when(securityUtils.getCurrentUserRole()).thenReturn("admin");
    when(securityUtils.getCurrentSessionId()).thenReturn(UUID.randomUUID());
  }

  @Test
  void testLogSync_Success() {
    // Given
    AuditContext context =
        AuditContext.builder()
            .eventType(AuditEventType.OPPORTUNITY_CREATED)
            .entityType("opportunity")
            .entityId(testEntityId)
            .newValue(Map.of("name", "Test Opportunity"))
            .changeReason("Created via test")
            .build();

    // When
    UUID auditId = auditService.logSync(context);

    // Then
    assertThat(auditId).isNotNull();

    // Verify that entry was persisted by querying it back
    Optional<AuditEntry> persistedEntry = auditRepository.findByIdOptional(auditId);
    assertThat(persistedEntry).isPresent();

    AuditEntry entry = persistedEntry.get();
    assertThat(entry.getEventType()).isEqualTo(AuditEventType.OPPORTUNITY_CREATED);
    assertThat(entry.getEntityType()).isEqualTo("opportunity");
    assertThat(entry.getEntityId()).isEqualTo(testEntityId);
    assertThat(entry.getUserId()).isEqualTo(testUserId);
    assertThat(entry.getUserName()).isEqualTo("Test User");
    assertThat(entry.getChangeReason()).isEqualTo("Created via test");
  }

  @Test
  void testLogAsync_Success() throws Exception {
    // Given
    AuditContext context =
        AuditContext.builder()
            .eventType(AuditEventType.OPPORTUNITY_UPDATED)
            .entityType("opportunity")
            .entityId(testEntityId)
            .oldValue(Map.of("value", 1000))
            .newValue(Map.of("value", 2000))
            .changeReason("Value updated")
            .build();

    // When
    CompletableFuture<UUID> future = auditService.logAsync(context);
    UUID auditId = future.get(); // Wait for completion

    // Then
    assertThat(auditId).isNotNull();

    // Verify that entry was persisted by querying it back
    Optional<AuditEntry> persistedEntry = auditRepository.findByIdOptional(auditId);
    assertThat(persistedEntry).isPresent();

    AuditEntry entry = persistedEntry.get();
    assertThat(entry.getEventType()).isEqualTo(AuditEventType.OPPORTUNITY_UPDATED);
    assertThat(entry.getEntityType()).isEqualTo("opportunity");
    assertThat(entry.getEntityId()).isEqualTo(testEntityId);
  }

  @Test
  void testSecurityEvent_AlwaysSync() {
    // Given
    String securityDetails = "Unauthorized access attempt";

    // When
    UUID auditId = auditService.logSecurityEvent(AuditEventType.PERMISSION_DENIED, securityDetails);

    // Then
    assertThat(auditId).isNotNull();

    // Verify that entry was persisted by querying it back
    Optional<AuditEntry> persistedEntry = auditRepository.findByIdOptional(auditId);
    assertThat(persistedEntry).isPresent();

    AuditEntry entry = persistedEntry.get();
    assertThat(entry.getEventType()).isEqualTo(AuditEventType.PERMISSION_DENIED);
    assertThat(entry.getEntityType()).isEqualTo("SECURITY");
    assertThat(entry.getSource()).isEqualTo(AuditSource.SYSTEM);
  }

  @Test
  void testAuditWithFullContext() {
    // Given
    AuditContext context =
        AuditContext.builder()
            .eventType(AuditEventType.OPPORTUNITY_STAGE_CHANGED)
            .entityType("opportunity")
            .entityId(testEntityId)
            .oldValue(Map.of("stage", "QUALIFICATION"))
            .newValue(Map.of("stage", "PROPOSAL"))
            .changeReason("Moving to proposal stage")
            .userComment("Customer approved initial discussions")
            .source(AuditSource.UI)
            .apiEndpoint("PUT /api/opportunities/123/stage")
            .requestId(UUID.randomUUID())
            .build();

    // When
    UUID auditId = auditService.logSync(context);

    // Then
    assertThat(auditId).isNotNull();

    // Verify that entry was persisted by querying it back
    Optional<AuditEntry> persistedEntry = auditRepository.findByIdOptional(auditId);
    assertThat(persistedEntry).isPresent();

    AuditEntry entry = persistedEntry.get();
    assertThat(entry.getChangeReason()).isEqualTo("Moving to proposal stage");
    assertThat(entry.getUserComment()).isEqualTo("Customer approved initial discussions");
    assertThat(entry.getSource()).isEqualTo(AuditSource.UI);
    assertThat(entry.getApiEndpoint()).isEqualTo("PUT /api/opportunities/123/stage");
  }

  @Test
  void testHashChaining() {
    // Given - Create a first entry to establish a previous hash
    AuditContext firstContext =
        AuditContext.builder()
            .eventType(AuditEventType.OPPORTUNITY_CREATED)
            .entityType("opportunity")
            .entityId(UUID.randomUUID())
            .newValue(Map.of("name", "First Opportunity"))
            .build();

    UUID firstId = auditService.logSync(firstContext);
    Optional<AuditEntry> firstEntry = auditRepository.findByIdOptional(firstId);
    assertThat(firstEntry).isPresent();
    String firstHash = firstEntry.get().getDataHash();

    // When - Create a second entry that should chain to the first
    AuditContext secondContext =
        AuditContext.builder()
            .eventType(AuditEventType.OPPORTUNITY_UPDATED)
            .entityType("opportunity")
            .entityId(testEntityId)
            .newValue(Map.of("name", "Second Opportunity"))
            .build();

    UUID secondId = auditService.logSync(secondContext);

    // Then
    Optional<AuditEntry> secondEntry = auditRepository.findByIdOptional(secondId);
    assertThat(secondEntry).isPresent();

    AuditEntry entry = secondEntry.get();
    assertThat(entry.getPreviousHash()).isEqualTo(firstHash);
    assertThat(entry.getDataHash()).isNotNull();
    assertThat(entry.getDataHash()).hasSize(64); // SHA-256 hex string
    assertThat(entry.getDataHash())
        .isNotEqualTo(firstHash); // Different entries should have different hashes
  }

  @Test
  void testAuditIntegrityCheck() {
    // This would test the hash verification logic
    // Implementation depends on specific requirements
  }
}
