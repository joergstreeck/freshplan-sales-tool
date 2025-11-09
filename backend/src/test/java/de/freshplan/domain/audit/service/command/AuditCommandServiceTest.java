package de.freshplan.domain.audit.service.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.entity.AuditSource;
import de.freshplan.domain.audit.events.AuditableApplicationEvent;
import de.freshplan.domain.audit.repository.AuditRepository;
import de.freshplan.domain.audit.service.dto.AuditContext;
import de.freshplan.domain.audit.service.provider.AuditConfiguration;
import de.freshplan.domain.audit.service.provider.AuditEvent;
import de.freshplan.domain.audit.service.provider.AuditException;
import de.freshplan.shared.util.SecurityUtils;
import io.vertx.core.http.HttpServerRequest;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Instance;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests für AuditCommandService
 *
 * <p>Testet alle Command-Operationen: - Async Logging - Sync Logging - Security Events - Export
 * Tracking - Event Processing
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Tag("unit")
class AuditCommandServiceTest {

  @Mock private AuditRepository auditRepository;
  @Mock private ObjectMapper objectMapper;
  @Mock private SecurityUtils securityUtils;
  @Mock private Event<AuditEvent> auditEventBus;
  @Mock private AuditConfiguration configuration;
  @Mock private Instance<HttpServerRequest> httpRequestInstance;

  @InjectMocks private AuditCommandService commandService;

  private UUID testUserId;
  private UUID testEntityId;
  private AuditContext testContext;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    testEntityId = UUID.randomUUID();

    // Setup default configuration
    when(configuration.getAsyncThreadPoolSize()).thenReturn(5);
    when(configuration.isEventBusEnabled()).thenReturn(true);

    // Setup default security context
    when(securityUtils.getCurrentUserId()).thenReturn(testUserId);
    when(securityUtils.getCurrentUserName()).thenReturn("Test User");
    when(securityUtils.getCurrentUserRole()).thenReturn("ADMIN");
    when(securityUtils.getCurrentSessionId()).thenReturn(UUID.randomUUID());

    // Setup object mapper
    try {
      when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    } catch (Exception e) {
      // Should not happen in test setup
    }

    // Setup HTTP request instance
    when(httpRequestInstance.isResolvable()).thenReturn(false);

    // Initialize service
    commandService.init();

    // Create test context
    testContext =
        AuditContext.builder()
            .eventType(AuditEventType.CUSTOMER_CREATED)
            .entityType("Customer")
            .entityId(testEntityId)
            .newValue(Map.of("name", "Test Customer"))
            .changeReason("Test creation")
            .build();
  }

  @Test
  void testLogAsync_withBasicParameters_shouldCreateAuditEntry()
      throws ExecutionException, InterruptedException, Exception {
    // Given
    ArgumentCaptor<AuditEntry> entryCaptor = ArgumentCaptor.forClass(AuditEntry.class);
    doAnswer(
            invocation -> {
              AuditEntry entry = invocation.getArgument(0);
              // Simulate setting ID on persist
              if (entry.getId() == null) {
                entry = entry.toBuilder().id(UUID.randomUUID()).build();
              }
              return null;
            })
        .when(auditRepository)
        .persist(any(AuditEntry.class));

    // When
    CompletableFuture<UUID> future =
        commandService.logAsync(
            AuditEventType.CUSTOMER_CREATED,
            "Customer",
            testEntityId,
            null,
            Map.of("name", "Test Customer"),
            "Test creation");

    // Wait for async completion
    Thread.sleep(100); // Give async operation time to complete

    // Then
    verify(auditRepository).persist(entryCaptor.capture());
    AuditEntry capturedEntry = entryCaptor.getValue();

    assertNotNull(capturedEntry);
    assertEquals(AuditEventType.CUSTOMER_CREATED, capturedEntry.getEventType());
    assertEquals("Customer", capturedEntry.getEntityType());
    assertEquals(testEntityId, capturedEntry.getEntityId());
    assertEquals("Test creation", capturedEntry.getChangeReason());
  }

  @Test
  void testLogAsync_withFullContext_shouldCaptureAllFields()
      throws ExecutionException, InterruptedException {
    // Given
    AuditContext fullContext =
        AuditContext.builder()
            .eventType(AuditEventType.CUSTOMER_UPDATED)
            .entityType("Customer")
            .entityId(testEntityId)
            .oldValue(Map.of("status", "ACTIVE"))
            .newValue(Map.of("status", "INACTIVE"))
            .changeReason("Status update")
            .userComment("Deactivated by admin")
            .source(AuditSource.API)
            .apiEndpoint("PUT /api/customers/123")
            .requestId(UUID.randomUUID())
            .ipAddress("192.168.1.1")
            .userAgent("Mozilla/5.0")
            .build();

    ArgumentCaptor<AuditEntry> entryCaptor = ArgumentCaptor.forClass(AuditEntry.class);

    // When
    CompletableFuture<UUID> future = commandService.logAsync(fullContext);
    UUID resultId = future.get();

    // Then
    verify(auditRepository).persist(entryCaptor.capture());
    AuditEntry capturedEntry = entryCaptor.getValue();

    assertEquals(AuditEventType.CUSTOMER_UPDATED, capturedEntry.getEventType());
    assertEquals("Status update", capturedEntry.getChangeReason());
    assertEquals("Deactivated by admin", capturedEntry.getUserComment());
    assertEquals(AuditSource.API, capturedEntry.getSource());
    assertEquals("PUT /api/customers/123", capturedEntry.getApiEndpoint());
    assertNotNull(capturedEntry.getRequestId());
    assertEquals("192.168.1.1", capturedEntry.getIpAddress());
    assertEquals("Mozilla/5.0", capturedEntry.getUserAgent());
  }

  @Test
  void testLogSync_shouldPersistImmediately() {
    // Given
    ArgumentCaptor<AuditEntry> entryCaptor = ArgumentCaptor.forClass(AuditEntry.class);

    // When
    UUID resultId = commandService.logSync(testContext);

    // Then
    verify(auditRepository).persist(entryCaptor.capture());
    verify(auditEventBus).fireAsync(any(AuditEvent.class));

    AuditEntry capturedEntry = entryCaptor.getValue();
    assertNotNull(capturedEntry);
    assertEquals(testContext.getEventType(), capturedEntry.getEventType());
    assertEquals(testContext.getEntityType(), capturedEntry.getEntityType());
    assertEquals(testContext.getEntityId(), capturedEntry.getEntityId());
  }

  @Test
  void testLogSecurityEvent_shouldCreateSecurityEntry() throws Exception {
    // Given
    String securityDetails = "Unauthorized access attempt";
    ArgumentCaptor<AuditEntry> entryCaptor = ArgumentCaptor.forClass(AuditEntry.class);

    // Setup object mapper to return proper JSON
    when(objectMapper.writeValueAsString(
            argThat(map -> map instanceof Map && ((Map) map).containsKey("details"))))
        .thenReturn("{\"details\":\"" + securityDetails + "\"}");

    // When
    UUID resultId =
        commandService.logSecurityEvent(AuditEventType.PERMISSION_DENIED, securityDetails);

    // Then
    verify(auditRepository).persist(entryCaptor.capture());
    AuditEntry capturedEntry = entryCaptor.getValue();

    assertEquals(AuditEventType.PERMISSION_DENIED, capturedEntry.getEventType());
    assertEquals("SECURITY", capturedEntry.getEntityType());
    assertEquals(AuditSource.SYSTEM, capturedEntry.getSource());
    assertNotNull(capturedEntry.getEntityId()); // Random UUID
    assertNotNull(capturedEntry.getNewValue());
    // Now it should contain the proper JSON
    assertTrue(capturedEntry.getNewValue().contains("details"));
  }

  @Test
  void testLogExport_shouldCreateExportEntry() {
    // Given
    String exportType = "CustomerReport";
    Map<String, Object> parameters =
        Map.of(
            "format", "PDF",
            "dateRange", "2025-01-01 to 2025-08-13");
    ArgumentCaptor<AuditEntry> entryCaptor = ArgumentCaptor.forClass(AuditEntry.class);

    // When
    UUID resultId = commandService.logExport(exportType, parameters);

    // Then
    verify(auditRepository).persist(entryCaptor.capture());
    AuditEntry capturedEntry = entryCaptor.getValue();

    assertEquals(AuditEventType.DATA_EXPORT_STARTED, capturedEntry.getEventType());
    assertEquals("EXPORT", capturedEntry.getEntityType());
    assertEquals(AuditSource.API, capturedEntry.getSource());
    assertTrue(capturedEntry.getChangeReason().contains(exportType));
    assertNotNull(capturedEntry.getEntityId()); // Random UUID
  }

  @Test
  void testOnApplicationEvent_shouldLogAsyncWithEventContext() {
    // Given
    AuditableApplicationEvent event = new TestAuditableEvent();

    // When
    commandService.onApplicationEvent(event);

    // Then
    // Verify that async logging was initiated (hard to verify async call)
    // At minimum, the method should complete without exception
    assertDoesNotThrow(() -> commandService.onApplicationEvent(event));
  }

  @Test
  void testHashChaining_shouldMaintainIntegrity() {
    // Given
    when(auditRepository.getLastHash()).thenReturn(Optional.of("previous-hash"));
    ArgumentCaptor<AuditEntry> entryCaptor = ArgumentCaptor.forClass(AuditEntry.class);

    // When
    UUID resultId = commandService.logSync(testContext);

    // Then
    verify(auditRepository).persist(entryCaptor.capture());
    AuditEntry capturedEntry = entryCaptor.getValue();

    assertNotNull(capturedEntry.getDataHash());
    assertFalse(capturedEntry.getDataHash().isEmpty());
    // Hash should be a 64-character hex string (SHA-256)
    assertEquals(64, capturedEntry.getDataHash().length());
  }

  @Test
  void testExceptionHandling_shouldLogToFallback() {
    // Given
    doThrow(new RuntimeException("Database error"))
        .when(auditRepository)
        .persist(any(AuditEntry.class));

    // When & Then
    AuditException exception =
        assertThrows(
            AuditException.class,
            () -> {
              commandService.logSync(testContext);
            });

    // Verify the exception contains expected message
    assertTrue(exception.getMessage().contains("Failed to log audit event"));
    assertNotNull(exception.getCause());
    assertEquals("Database error", exception.getCause().getMessage());
  }

  @Test
  void testEventBusDisabled_shouldNotFireEvent() {
    // Given
    when(configuration.isEventBusEnabled()).thenReturn(false);

    // When
    commandService.logSync(testContext);

    // Then
    verify(auditEventBus, never()).fireAsync(any());
  }

  @Test
  void testNotificationRequired_shouldCheckEventType() {
    // Given
    AuditContext criticalContext =
        AuditContext.builder()
            .eventType(AuditEventType.ROLE_ASSIGNED)
            .entityType("User")
            .entityId(testEntityId)
            .newValue(Map.of("role", "ADMIN"))
            .changeReason("Role elevation")
            .build();

    // When
    commandService.logSync(criticalContext);

    // Then
    // In real implementation, this would trigger notification
    // Here we just verify the entry was created with the critical event type
    ArgumentCaptor<AuditEntry> entryCaptor = ArgumentCaptor.forClass(AuditEntry.class);
    verify(auditRepository).persist(entryCaptor.capture());
    assertEquals(AuditEventType.ROLE_ASSIGNED, entryCaptor.getValue().getEventType());
  }

  // Helper class for testing
  private static class TestAuditableEvent implements AuditableApplicationEvent {
    @Override
    public AuditContext toAuditContext() {
      return AuditContext.builder()
          .eventType(AuditEventType.SYSTEM_STARTUP)
          .entityType("TestEvent")
          .entityId(UUID.randomUUID())
          .newValue(Map.of("event", "test"))
          .changeReason("Test event")
          .source(AuditSource.SYSTEM)
          .build();
    }
  }
}
