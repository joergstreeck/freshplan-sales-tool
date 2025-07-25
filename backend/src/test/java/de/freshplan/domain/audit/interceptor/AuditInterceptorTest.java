package de.freshplan.domain.audit.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.freshplan.domain.audit.annotation.Auditable;
import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.service.AuditService;
import de.freshplan.domain.audit.service.dto.AuditContext;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Unit tests for AuditInterceptor
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
class AuditInterceptorTest {

  @Inject TestService testService;

  @InjectMock AuditService auditService;

  @BeforeEach
  void setUp() {
    reset(auditService);
    when(auditService.logAsync(any()))
        .thenReturn(CompletableFuture.completedFuture(UUID.randomUUID()));
    when(auditService.logSync(any())).thenReturn(UUID.randomUUID());
  }

  @Test
  void testAuditableMethodSuccess() {
    // Given
    UUID entityId = UUID.randomUUID();
    String newValue = "Updated Value";

    // When
    String result = testService.updateEntity(entityId, newValue);

    // Then
    assertThat(result).isEqualTo(newValue);

    ArgumentCaptor<AuditContext> contextCaptor = ArgumentCaptor.forClass(AuditContext.class);
    verify(auditService, times(1)).logAsync(contextCaptor.capture());

    AuditContext capturedContext = contextCaptor.getValue();
    assertThat(capturedContext.getEventType()).isEqualTo(AuditEventType.OPPORTUNITY_UPDATED);
    assertThat(capturedContext.getEntityType()).isEqualTo("test");
    assertThat(capturedContext.getEntityId()).isEqualTo(entityId);
    assertThat(capturedContext.getNewValue()).isEqualTo(newValue);
  }

  @Test
  void testAuditableMethodWithOldValue() {
    // Given
    UUID entityId = UUID.randomUUID();
    String oldValue = "Old Value";
    String newValue = "New Value";

    // When
    String result = testService.updateEntityWithOldValue(entityId, oldValue, newValue);

    // Then
    assertThat(result).isEqualTo(newValue);

    ArgumentCaptor<AuditContext> contextCaptor = ArgumentCaptor.forClass(AuditContext.class);
    verify(auditService, times(1)).logAsync(contextCaptor.capture());

    AuditContext capturedContext = contextCaptor.getValue();
    assertThat(capturedContext.getOldValue()).isEqualTo(oldValue);
    assertThat(capturedContext.getNewValue()).isEqualTo(newValue);
  }

  @Test
  void testAuditableMethodWithReason() {
    // Given
    UUID entityId = UUID.randomUUID();
    String value = "Value";
    String reason = "Business reason for change";

    // When
    String result = testService.updateEntityWithReason(entityId, value, reason);

    // Then
    assertThat(result).isEqualTo(value);

    ArgumentCaptor<AuditContext> contextCaptor = ArgumentCaptor.forClass(AuditContext.class);
    verify(auditService, times(1)).logAsync(contextCaptor.capture());

    AuditContext capturedContext = contextCaptor.getValue();
    assertThat(capturedContext.getChangeReason()).isEqualTo(reason);
  }

  @Test
  void testAuditableMethodSyncMode() {
    // Given
    UUID entityId = UUID.randomUUID();

    // When
    testService.syncAuditMethod(entityId);

    // Then
    verify(auditService, times(1)).logSync(any(AuditContext.class));
    verify(auditService, never()).logAsync(any());
  }

  @Test
  void testAuditableMethodFailure() {
    // Given
    UUID entityId = UUID.randomUUID();

    // When/Then
    assertThatThrownBy(() -> testService.failingMethod(entityId))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Intentional failure");

    // Verify failure was audited synchronously
    ArgumentCaptor<AuditContext> contextCaptor = ArgumentCaptor.forClass(AuditContext.class);
    verify(auditService, times(1)).logSync(contextCaptor.capture());

    AuditContext capturedContext = contextCaptor.getValue();
    assertThat(capturedContext.getEventType()).isEqualTo(AuditEventType.ERROR_OCCURRED);
    assertThat(capturedContext.getNewValue()).isNotNull();
    assertThat(capturedContext.getNewValue().toString()).contains("RuntimeException");
  }

  @Test
  void testEntityIdExtraction() {
    // Test UUID parameter
    UUID uuid = UUID.randomUUID();
    testService.updateEntity(uuid, "value");

    ArgumentCaptor<AuditContext> contextCaptor = ArgumentCaptor.forClass(AuditContext.class);
    verify(auditService).logAsync(contextCaptor.capture());
    assertThat(contextCaptor.getValue().getEntityId()).isEqualTo(uuid);

    // Test String UUID parameter
    reset(auditService);
    when(auditService.logAsync(any()))
        .thenReturn(CompletableFuture.completedFuture(UUID.randomUUID()));

    String stringUuid = UUID.randomUUID().toString();
    testService.updateEntityWithStringId(stringUuid, "value");

    verify(auditService).logAsync(contextCaptor.capture());
    assertThat(contextCaptor.getValue().getEntityId()).isEqualTo(UUID.fromString(stringUuid));
  }

  @Test
  void testNoResultCapture() {
    // Given
    UUID entityId = UUID.randomUUID();

    // When
    testService.updateWithoutResultCapture(entityId);

    // Then
    ArgumentCaptor<AuditContext> contextCaptor = ArgumentCaptor.forClass(AuditContext.class);
    verify(auditService).logAsync(contextCaptor.capture());

    AuditContext capturedContext = contextCaptor.getValue();
    assertThat(capturedContext.getNewValue()).isNull();
  }

  /** Test service with auditable methods */
  @ApplicationScoped
  public static class TestService {

    @Auditable(eventType = AuditEventType.OPPORTUNITY_UPDATED, entityType = "test")
    public String updateEntity(UUID id, String newValue) {
      return newValue;
    }

    @Auditable(
        eventType = AuditEventType.OPPORTUNITY_UPDATED,
        entityType = "test",
        oldValueParam = 1)
    public String updateEntityWithOldValue(UUID id, String oldValue, String newValue) {
      return newValue;
    }

    @Auditable(eventType = AuditEventType.OPPORTUNITY_UPDATED, entityType = "test", reasonParam = 2)
    public String updateEntityWithReason(UUID id, String value, String reason) {
      return value;
    }

    @Auditable(eventType = AuditEventType.SYSTEM_STARTUP, entityType = "test", sync = true)
    public void syncAuditMethod(UUID id) {
      // Method with synchronous audit
    }

    @Auditable(eventType = AuditEventType.OPPORTUNITY_UPDATED, entityType = "test")
    public void failingMethod(UUID id) {
      throw new RuntimeException("Intentional failure");
    }

    @Auditable(eventType = AuditEventType.OPPORTUNITY_UPDATED, entityType = "test")
    public String updateEntityWithStringId(String id, String value) {
      return value;
    }

    @Auditable(
        eventType = AuditEventType.OPPORTUNITY_UPDATED,
        entityType = "test",
        includeResult = false)
    public void updateWithoutResultCapture(UUID id) {
      // Method that doesn't capture result
    }
  }
}
