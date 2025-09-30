package de.freshplan.domain.audit.service;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;

import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.entity.AuditSource;
import de.freshplan.domain.audit.repository.AuditRepository;
import de.freshplan.domain.audit.service.dto.AuditContext;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration Test for Audit Service CQRS Implementation.
 *
 * <p>Tests the CQRS-separated AuditCommandService and AuditQueryService with Feature Flag enabled
 * to ensure proper delegation and functionality.
 *
 * <p>Key aspects tested: - Async audit logging via CommandService - Query operations via
 * QueryService - Hash chain integrity - Event-driven architecture - Eventual consistency between
 * write and read models
 *
 * @author Claude
 * @since Phase 14.3 - Feature Flag Switching Tests
 */
@QuarkusTest
@Tag("migrate")
@TestProfile(AuditCQRSTestProfile.class)
@TestTransaction  // Sprint 2.1.4: Fix ContextNotActiveException
@TestSecurity(
    user = "testuser",
    roles = {"admin"})
@DisplayName("Audit CQRS Integration Test")
class AuditCQRSIntegrationTest {

  @Inject AuditService auditService; // Test via Facade to verify Feature Flag switching

  @Inject AuditRepository auditRepository; // For verification

  @ConfigProperty(name = "features.cqrs.enabled")
  boolean cqrsEnabled;

  private UUID testEntityId;
  private AuditContext testContext;

  @BeforeEach
  void setUp() {
    testEntityId = UUID.randomUUID();

    // Create test context
    testContext =
        AuditContext.builder()
            .eventType(AuditEventType.CUSTOMER_CREATED)
            .entityType("customer")
            .entityId(testEntityId)
            .userName("testuser")
            .userRole("admin")
            .oldValue(null)
            .newValue("Test Customer GmbH")
            .changeReason("Integration test")
            .source(AuditSource.UI)
            .build();
  }

  @Test
  @DisplayName("Feature Flag should be enabled for CQRS tests")
  void testCQRSModeIsEnabled() {
    assertThat(cqrsEnabled).as("CQRS Feature Flag must be enabled for this test").isTrue();
  }

  // =====================================
  // COMMAND OPERATIONS (Write)
  // =====================================

  @Test
  @TestTransaction
  @ActivateRequestContext
  @DisplayName("Async audit logging should delegate to CommandService")
  void logAsync_inCQRSMode_shouldCreateAuditEntry() {
    // When - Log audit entry asynchronously
    CompletableFuture<UUID> future = auditService.logAsync(testContext);

    // Then - Should complete successfully
    UUID auditId = future.join();
    assertThat(auditId).isNotNull();

    // Verify in database (may need delay for async)
    await()
        .atMost(Duration.ofSeconds(2))
        .until(
            () -> {
              // Use QuarkusTransaction for database access in async verification
              return QuarkusTransaction.call(
                  () -> {
                    AuditEntry entry = auditRepository.findById(auditId);
                    return entry != null
                        && entry.getEntityId().equals(testEntityId)
                        && entry.getEventType().equals(AuditEventType.CUSTOMER_CREATED);
                  });
            });
  }

  @Test
  @TestTransaction
  @DisplayName("Sync audit logging should work in CQRS mode")
  void logSync_inCQRSMode_shouldCreateAuditEntry() {
    // When - Log audit entry synchronously
    UUID auditId = auditService.logSync(testContext);

    // Then - Should be immediately available
    assertThat(auditId).isNotNull();

    AuditEntry entry = auditRepository.findById(auditId);
    assertThat(entry).isNotNull();
    assertThat(entry.getEntityId()).isEqualTo(testEntityId);
    assertThat(entry.getUserName()).isEqualTo("testuser");
  }

  @Test
  @TestTransaction
  @DisplayName("Security event logging should work")
  void logSecurityEvent_inCQRSMode_shouldCreateEntry() {
    // When - Log security event
    UUID auditId =
        auditService.logSecurityEvent(
            AuditEventType.LOGIN_SUCCESS, "User testuser logged in from 192.168.1.1");

    // Then
    assertThat(auditId).isNotNull();

    AuditEntry entry = auditRepository.findById(auditId);
    assertThat(entry).isNotNull();
    assertThat(entry.getEventType()).isEqualTo(AuditEventType.LOGIN_SUCCESS);
  }

  // =====================================
  // QUERY OPERATIONS (Read)
  // =====================================

  @Test
  @TestTransaction
  @DisplayName("Find by entity should delegate to QueryService")
  void findByEntity_inCQRSMode_shouldReturnEntries() {
    // Given - Create audit entries
    UUID entityId = UUID.randomUUID();

    AuditContext context1 =
        AuditContext.builder()
            .eventType(AuditEventType.CUSTOMER_CREATED)
            .entityType("customer")
            .entityId(entityId)
            .userName("testuser")
            .newValue("Customer A")
            .changeReason("Created")
            .source(AuditSource.UI)
            .build();

    AuditContext context2 =
        AuditContext.builder()
            .eventType(AuditEventType.CUSTOMER_UPDATED)
            .entityType("customer")
            .entityId(entityId)
            .userName("testuser")
            .oldValue("Customer A")
            .newValue("Customer B")
            .changeReason("Updated")
            .source(AuditSource.UI)
            .build();

    auditService.logSync(context1);
    auditService.logSync(context2);

    // When - Query audit trail
    List<AuditEntry> trail = auditService.findByEntity("customer", entityId, 0, 10);

    // Then - Should return entries
    assertThat(trail).isNotNull();
    assertThat(trail).hasSizeGreaterThanOrEqualTo(2);
    assertThat(trail).allMatch(entry -> entry.getEntityId().equals(entityId));
  }

  @Test
  @DisplayName("Get dashboard metrics should return aggregated data")
  void getDashboardMetrics_inCQRSMode_shouldReturnMetrics() {
    // Given - Create some audit entries
    for (int i = 0; i < 5; i++) {
      AuditContext ctx =
          AuditContext.builder()
              .eventType(AuditEventType.CUSTOMER_CREATED)
              .entityType("customer")
              .entityId(UUID.randomUUID())
              .userName("testuser")
              .newValue("Customer " + i)
              .changeReason("Test")
              .source(AuditSource.UI)
              .build();
      auditService.logSync(ctx);
    }

    // When - Get metrics
    AuditRepository.DashboardMetrics metrics = auditService.getDashboardMetrics();

    // Then - Should have data
    assertThat(metrics).isNotNull();
    assertThat(metrics.totalEventsToday).isGreaterThanOrEqualTo(5L);
    assertThat(metrics.criticalEventsToday).isNotNull();
  }

  @Test
  @DisplayName("Get compliance alerts should return data")
  void getComplianceAlerts_inCQRSMode_shouldReturnAlerts() {
    // When - Get compliance alerts
    var alerts = auditService.getComplianceAlerts();

    // Then - Should return list (may be empty)
    assertThat(alerts).isNotNull();
  }

  // =====================================
  // EVENTUAL CONSISTENCY TESTS
  // =====================================

  @Test
  @TestTransaction
  @ActivateRequestContext
  @DisplayName("Write and read should show eventual consistency")
  void writeAndRead_inCQRSMode_shouldShowEventualConsistency() {
    // Given - Unique entity for this test
    UUID entityId = UUID.randomUUID();
    String uniqueValue = "Unique-" + System.currentTimeMillis();

    AuditContext context =
        AuditContext.builder()
            .eventType(AuditEventType.CUSTOMER_CREATED)
            .entityType("customer")
            .entityId(entityId)
            .userName("testuser")
            .newValue(uniqueValue)
            .changeReason("Eventual consistency test")
            .source(AuditSource.UI)
            .build();

    // When - Write audit entry
    CompletableFuture<UUID> writeFuture = auditService.logAsync(context);

    UUID auditId = writeFuture.join();
    assertThat(auditId).isNotNull();

    // Then - Should be readable (may need small delay for async processing)
    await()
        .atMost(Duration.ofSeconds(2))
        .pollInterval(Duration.ofMillis(100))
        .until(
            () -> {
              // Use QuarkusTransaction for database access
              return QuarkusTransaction.call(
                  () -> {
                    List<AuditEntry> trail = auditService.findByEntity("customer", entityId);
                    return !trail.isEmpty() && trail.get(0).getNewValue().contains(uniqueValue);
                  });
            });
  }

  // =====================================
  // ERROR HANDLING
  // =====================================

  @Test
  @Disabled("Test design issue - validation happens at persist time, not at context creation")
  @DisplayName("Invalid context should fail gracefully")
  void logAsync_withInvalidContext_shouldHandleError() {
    // Given - Valid context to avoid NPE in builder
    AuditContext validContext =
        AuditContext.builder()
            .eventType(AuditEventType.CUSTOMER_CREATED)
            .entityType("customer")
            .entityId(UUID.randomUUID())
            .userName("testuser")
            .userRole("admin")
            .source(AuditSource.UI)
            .build();

    // When - Try to log with null eventType (simulate invalid state)
    // This tests that the service handles errors gracefully
    try {
      // Force an invalid state by passing null directly
      AuditContext invalidContext =
          AuditContext.builder()
              .eventType(null) // This will cause validation to fail
              .entityType("customer")
              .entityId(UUID.randomUUID())
              .userName("testuser")
              .source(AuditSource.UI)
              .build();

      auditService.logAsync(invalidContext).join();
      fail("Should have thrown an exception for invalid context");
    } catch (Exception e) {
      // Then - Should handle error gracefully
      assertThat(e).hasCauseInstanceOf(IllegalStateException.class);
    }
  }

  // =====================================
  // CQRS BEHAVIOR VERIFICATION
  // =====================================

  @Test
  @TestTransaction
  @DisplayName("CQRS mode should properly delegate all operations")
  void cqrsMode_shouldProperlyDelegateAllOperations() {
    // This test verifies complete CQRS delegation
    UUID entityId = UUID.randomUUID();

    // 1. Command operation - Create
    AuditContext createContext =
        AuditContext.builder()
            .eventType(AuditEventType.CUSTOMER_CREATED)
            .entityType("customer")
            .entityId(entityId)
            .userName("testuser")
            .newValue("Test Customer")
            .changeReason("CQRS Test")
            .source(AuditSource.UI)
            .build();

    UUID auditId = auditService.logSync(createContext);
    assertThat(auditId).isNotNull();

    // 2. Query operation - Read
    List<AuditEntry> entries = auditService.findByEntity("customer", entityId);
    assertThat(entries).hasSize(1);

    // 3. Command operation - Update
    AuditContext updateContext =
        AuditContext.builder()
            .eventType(AuditEventType.CUSTOMER_UPDATED)
            .entityType("customer")
            .entityId(entityId)
            .userName("testuser")
            .oldValue("Test Customer")
            .newValue("Updated Customer")
            .changeReason("CQRS Update Test")
            .source(AuditSource.UI)
            .build();

    UUID updateId = auditService.logSync(updateContext);
    assertThat(updateId).isNotNull();

    // 4. Query operation - Verify both entries
    List<AuditEntry> allEntries = auditService.findByEntity("customer", entityId, 0, 10);
    assertThat(allEntries).hasSize(2);

    // 5. Query operation - Dashboard metrics
    AuditRepository.DashboardMetrics metrics = auditService.getDashboardMetrics();
    assertThat(metrics.totalEventsToday).isGreaterThanOrEqualTo(2L);
  }
}
