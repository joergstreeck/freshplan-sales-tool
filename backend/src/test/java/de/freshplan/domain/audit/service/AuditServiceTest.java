package de.freshplan.domain.audit.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditService
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@TestSecurity(user = "testuser", roles = {"admin"})
class AuditServiceTest {
    
    @Inject
    AuditService auditService;
    
    @InjectMock
    AuditRepository auditRepository;
    
    @InjectMock
    SecurityUtils securityUtils;
    
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
        AuditContext context = AuditContext.builder()
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
        
        ArgumentCaptor<AuditEntry> entryCaptor = ArgumentCaptor.forClass(AuditEntry.class);
        verify(auditRepository, times(1)).persist(entryCaptor.capture());
        
        AuditEntry capturedEntry = entryCaptor.getValue();
        assertThat(capturedEntry.getEventType()).isEqualTo(AuditEventType.OPPORTUNITY_CREATED);
        assertThat(capturedEntry.getEntityType()).isEqualTo("opportunity");
        assertThat(capturedEntry.getEntityId()).isEqualTo(testEntityId);
        assertThat(capturedEntry.getUserId()).isEqualTo(testUserId);
        assertThat(capturedEntry.getUserName()).isEqualTo("Test User");
        assertThat(capturedEntry.getChangeReason()).isEqualTo("Created via test");
    }
    
    @Test
    void testLogAsync_Success() throws Exception {
        // Given
        AuditContext context = AuditContext.builder()
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
        verify(auditRepository, times(1)).persist(any(AuditEntry.class));
    }
    
    @Test
    void testSecurityEvent_AlwaysSync() {
        // Given
        String securityDetails = "Unauthorized access attempt";
        
        // When
        UUID auditId = auditService.logSecurityEvent(
            AuditEventType.PERMISSION_DENIED, 
            securityDetails
        );
        
        // Then
        assertThat(auditId).isNotNull();
        
        ArgumentCaptor<AuditEntry> entryCaptor = ArgumentCaptor.forClass(AuditEntry.class);
        verify(auditRepository, times(1)).persist(entryCaptor.capture());
        
        AuditEntry capturedEntry = entryCaptor.getValue();
        assertThat(capturedEntry.getEventType()).isEqualTo(AuditEventType.PERMISSION_DENIED);
        assertThat(capturedEntry.getEntityType()).isEqualTo("SECURITY");
        assertThat(capturedEntry.getSource()).isEqualTo(AuditSource.SYSTEM);
    }
    
    @Test
    void testAuditWithFullContext() {
        // Given
        AuditContext context = AuditContext.builder()
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
        
        ArgumentCaptor<AuditEntry> entryCaptor = ArgumentCaptor.forClass(AuditEntry.class);
        verify(auditRepository, times(1)).persist(entryCaptor.capture());
        
        AuditEntry capturedEntry = entryCaptor.getValue();
        assertThat(capturedEntry.getChangeReason()).isEqualTo("Moving to proposal stage");
        assertThat(capturedEntry.getUserComment()).isEqualTo("Customer approved initial discussions");
        assertThat(capturedEntry.getSource()).isEqualTo(AuditSource.UI);
        assertThat(capturedEntry.getApiEndpoint()).isEqualTo("PUT /api/opportunities/123/stage");
    }
    
    @Test
    void testHashChaining() {
        // Given
        when(auditRepository.getLastHash()).thenReturn(Optional.of("previous-hash-value"));
        
        AuditContext context = AuditContext.builder()
                .eventType(AuditEventType.OPPORTUNITY_CREATED)
                .entityType("opportunity")
                .entityId(testEntityId)
                .build();
        
        // When
        auditService.logSync(context);
        
        // Then
        ArgumentCaptor<AuditEntry> entryCaptor = ArgumentCaptor.forClass(AuditEntry.class);
        verify(auditRepository).persist(entryCaptor.capture());
        
        AuditEntry capturedEntry = entryCaptor.getValue();
        assertThat(capturedEntry.getPreviousHash()).isEqualTo("previous-hash-value");
        assertThat(capturedEntry.getDataHash()).isNotNull();
        assertThat(capturedEntry.getDataHash()).hasSize(64); // SHA-256 hex string
    }
    
    @Test
    void testAuditIntegrityCheck() {
        // This would test the hash verification logic
        // Implementation depends on specific requirements
    }
}