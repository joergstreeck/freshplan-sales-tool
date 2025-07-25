package de.freshplan.domain.audit;

import de.freshplan.domain.audit.annotation.Auditable;
import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.repository.AuditRepository;
import de.freshplan.domain.audit.service.AuditService;
import de.freshplan.domain.audit.service.dto.AuditContext;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration tests for the complete Audit System
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@TestTransaction
@TestSecurity(user = "integrationtest", roles = {"admin"})
class AuditSystemIntegrationTest {
    
    @Inject
    AuditService auditService;
    
    @Inject
    AuditRepository auditRepository;
    
    @Inject
    TestAuditableService testService;
    
    private UUID testEntityId;
    
    @BeforeEach
    void setUp() {
        testEntityId = UUID.randomUUID();
        // Clean any existing audit entries
        auditRepository.deleteAll();
    }
    
    @Test
    void testCompleteAuditFlow() throws Exception {
        // Given
        String initialValue = "Initial";
        String updatedValue = "Updated";
        String reason = "Integration test update";
        
        // When - Create entity
        testService.createEntity(testEntityId, initialValue);
        
        // Wait for async audit to complete
        await().atMost(2, TimeUnit.SECONDS).until(() -> 
            auditRepository.count() >= 1
        );
        
        // When - Update entity
        testService.updateEntity(testEntityId, initialValue, updatedValue, reason);
        
        // Wait for second audit entry
        await().atMost(2, TimeUnit.SECONDS).until(() -> 
            auditRepository.count() >= 2
        );
        
        // Then - Verify audit trail
        List<AuditEntry> entries = auditRepository.findByEntity("test-entity", testEntityId);
        assertThat(entries).hasSize(2);
        
        // Verify creation audit
        AuditEntry createEntry = entries.stream()
                .filter(e -> e.getEventType() == AuditEventType.OPPORTUNITY_CREATED)
                .findFirst()
                .orElseThrow();
        
        assertThat(createEntry.getEntityId()).isEqualTo(testEntityId);
        assertThat(createEntry.getNewValue()).contains(initialValue);
        assertThat(createEntry.getOldValue()).isNull();
        
        // Verify update audit
        AuditEntry updateEntry = entries.stream()
                .filter(e -> e.getEventType() == AuditEventType.OPPORTUNITY_UPDATED)
                .findFirst()
                .orElseThrow();
        
        assertThat(updateEntry.getEntityId()).isEqualTo(testEntityId);
        assertThat(updateEntry.getOldValue()).contains(initialValue);
        assertThat(updateEntry.getNewValue()).contains(updatedValue);
        assertThat(updateEntry.getChangeReason()).isEqualTo(reason);
    }
    
    @Test
    void testHashChainIntegrity() throws Exception {
        // Create multiple audit entries
        for (int i = 0; i < 5; i++) {
            auditService.logSync(AuditContext.builder()
                    .eventType(AuditEventType.SYSTEM_STARTUP)
                    .entityType("system")
                    .entityId(UUID.randomUUID())
                    .newValue(Map.of("iteration", i))
                    .build());
        }
        
        // Verify hash chain
        Instant now = Instant.now();
        var integrityIssues = auditRepository.verifyIntegrity(
            now.minusSeconds(60), 
            now.plusSeconds(60)
        );
        
        assertThat(integrityIssues).isEmpty();
        
        // Verify each entry has previous hash
        List<AuditEntry> allEntries = auditRepository.listAll();
        assertThat(allEntries).hasSize(5);
        
        String previousHash = null;
        for (AuditEntry entry : allEntries) {
            if (previousHash != null) {
                assertThat(entry.getPreviousHash()).isEqualTo(previousHash);
            }
            previousHash = entry.getDataHash();
        }
    }
    
    @Test
    void testSecurityEventAuditing() {
        // Log security event
        UUID auditId = auditService.logSecurityEvent(
            AuditEventType.PERMISSION_DENIED,
            "User attempted to access restricted resource"
        );
        
        // Verify it was logged synchronously
        assertThat(auditId).isNotNull();
        
        // Find security events
        List<AuditEntry> securityEvents = auditRepository.findSecurityEvents(
            Instant.now().minusSeconds(60),
            Instant.now()
        );
        
        assertThat(securityEvents).hasSize(1);
        assertThat(securityEvents.get(0).getEventType()).isEqualTo(AuditEventType.PERMISSION_DENIED);
        assertThat(securityEvents.get(0).getEntityType()).isEqualTo("SECURITY");
    }
    
    @Test
    void testAuditExceptionHandling() {
        // Test that audit failures don't break business logic
        try {
            testService.failingMethod(testEntityId);
        } catch (RuntimeException e) {
            assertThat(e).hasMessage("Business logic failure");
        }
        
        // Wait for audit
        await().atMost(2, TimeUnit.SECONDS).until(() -> 
            auditRepository.count() >= 1
        );
        
        // Verify failure was audited
        List<AuditEntry> entries = auditRepository.findFailures(
            Instant.now().minusSeconds(60),
            Instant.now()
        );
        
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).getEventType()).isEqualTo(AuditEventType.ERROR_OCCURRED);
    }
    
    @Test
    void testAuditSearch() throws Exception {
        // Create various audit entries
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        
        // User 1 activities
        auditService.logSync(AuditContext.builder()
                .eventType(AuditEventType.LOGIN_SUCCESS)
                .entityType("session")
                .entityId(UUID.randomUUID())
                .userId(userId1)
                .userName("User One")
                .userRole("admin")
                .build());
        
        auditService.logSync(AuditContext.builder()
                .eventType(AuditEventType.OPPORTUNITY_CREATED)
                .entityType("opportunity")
                .entityId(UUID.randomUUID())
                .userId(userId1)
                .userName("User One")
                .userRole("admin")
                .build());
        
        // User 2 activities
        auditService.logSync(AuditContext.builder()
                .eventType(AuditEventType.CUSTOMER_CREATED)
                .entityType("customer")
                .entityId(UUID.randomUUID())
                .userId(userId2)
                .userName("User Two")
                .userRole("sales")
                .build());
        
        // Search by user
        var userSearchCriteria = AuditRepository.AuditSearchCriteria.builder()
                .userId(userId1)
                .build();
        
        List<AuditEntry> user1Entries = auditRepository.search(userSearchCriteria);
        assertThat(user1Entries).hasSize(2);
        assertThat(user1Entries).extracting(AuditEntry::getUserId)
                .containsOnly(userId1);
        
        // Search by entity type
        var entitySearchCriteria = AuditRepository.AuditSearchCriteria.builder()
                .entityType("opportunity")
                .build();
        
        List<AuditEntry> opportunityEntries = auditRepository.search(entitySearchCriteria);
        assertThat(opportunityEntries).hasSize(1);
        assertThat(opportunityEntries.get(0).getEntityType()).isEqualTo("opportunity");
    }
    
    /**
     * Test service with auditable methods
     */
    @ApplicationScoped
    public static class TestAuditableService {
        
        @Inject
        AuditService auditService;
        
        @Auditable(eventType = AuditEventType.OPPORTUNITY_CREATED, entityType = "test-entity")
        public String createEntity(UUID id, String value) {
            return value;
        }
        
        public String updateEntity(UUID id, String oldValue, String newValue, String reason) {
            // Manual audit for more control
            auditService.logAsync(AuditContext.builder()
                    .eventType(AuditEventType.OPPORTUNITY_UPDATED)
                    .entityType("test-entity")
                    .entityId(id)
                    .oldValue(Map.of("value", oldValue))
                    .newValue(Map.of("value", newValue))
                    .changeReason(reason)
                    .build());
            
            return newValue;
        }
        
        @Auditable(eventType = AuditEventType.OPPORTUNITY_DELETED, entityType = "test-entity")
        public void failingMethod(UUID id) {
            throw new RuntimeException("Business logic failure");
        }
    }
}