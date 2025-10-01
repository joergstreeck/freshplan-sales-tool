package de.freshplan.domain.audit.service.query;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.freshplan.domain.audit.dto.ComplianceAlertDto;
import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.repository.AuditRepository;
import de.freshplan.domain.export.service.dto.ExportRequest;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests für AuditQueryService
 *
 * <p>Testet alle Query-Operationen: - Find operations - Statistics and metrics - Dashboard data -
 * Compliance checks - Export operations
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AuditQueryServiceTest {

  @Mock private AuditRepository auditRepository;

  @InjectMocks private AuditQueryService queryService;

  private UUID testEntityId;
  private UUID testUserId;
  private Instant testFrom;
  private Instant testTo;
  private List<AuditEntry> mockAuditEntries;

  @BeforeEach
  void setUp() {
    testEntityId = UUID.randomUUID();
    testUserId = UUID.randomUUID();
    testFrom = Instant.now().minusSeconds(86400); // 24 hours ago
    testTo = Instant.now();

    // Create mock audit entries
    mockAuditEntries = createMockAuditEntries();
  }

  @Test
  void testFindByEntity_shouldDelegateToRepository() {
    // Given
    when(auditRepository.findByEntity("Customer", testEntityId)).thenReturn(mockAuditEntries);

    // When
    List<AuditEntry> result = queryService.findByEntity("Customer", testEntityId);

    // Then
    verify(auditRepository).findByEntity("Customer", testEntityId);
    assertEquals(mockAuditEntries, result);
    assertEquals(3, result.size());
  }

  @Test
  void testFindByEntityWithPagination_shouldDelegateToRepository() {
    // Given
    int page = 0;
    int size = 10;
    when(auditRepository.findByEntity("Customer", testEntityId, page, size))
        .thenReturn(mockAuditEntries);

    // When
    List<AuditEntry> result = queryService.findByEntity("Customer", testEntityId, page, size);

    // Then
    verify(auditRepository).findByEntity("Customer", testEntityId, page, size);
    assertEquals(mockAuditEntries, result);
  }

  @Test
  void testFindByUser_shouldDelegateToRepository() {
    // Given
    when(auditRepository.findByUser(testUserId, testFrom, testTo)).thenReturn(mockAuditEntries);

    // When
    List<AuditEntry> result = queryService.findByUser(testUserId, testFrom, testTo);

    // Then
    verify(auditRepository).findByUser(testUserId, testFrom, testTo);
    assertEquals(mockAuditEntries, result);
  }

  @Test
  void testFindByEventType_shouldDelegateToRepository() {
    // Given
    AuditEventType eventType = AuditEventType.CUSTOMER_CREATED;
    when(auditRepository.findByEventType(eventType, testFrom, testTo)).thenReturn(mockAuditEntries);

    // When
    List<AuditEntry> result = queryService.findByEventType(eventType, testFrom, testTo);

    // Then
    verify(auditRepository).findByEventType(eventType, testFrom, testTo);
    assertEquals(mockAuditEntries, result);
  }

  @Test
  void testFindSecurityEvents_shouldDelegateToRepository() {
    // Given
    when(auditRepository.findSecurityEvents(testFrom, testTo)).thenReturn(mockAuditEntries);

    // When
    List<AuditEntry> result = queryService.findSecurityEvents(testFrom, testTo);

    // Then
    verify(auditRepository).findSecurityEvents(testFrom, testTo);
    assertEquals(mockAuditEntries, result);
  }

  @Test
  void testFindFailures_shouldDelegateToRepository() {
    // Given
    when(auditRepository.findFailures(testFrom, testTo)).thenReturn(mockAuditEntries);

    // When
    List<AuditEntry> result = queryService.findFailures(testFrom, testTo);

    // Then
    verify(auditRepository).findFailures(testFrom, testTo);
    assertEquals(mockAuditEntries, result);
  }

  @Test
  void testSearch_shouldDelegateToRepository() {
    // Given
    AuditRepository.AuditSearchCriteria criteria =
        AuditRepository.AuditSearchCriteria.builder()
            .entityType("Customer")
            .from(testFrom)
            .to(testTo)
            .build();

    when(auditRepository.search(criteria)).thenReturn(mockAuditEntries);

    // When
    List<AuditEntry> result = queryService.search(criteria);

    // Then
    verify(auditRepository).search(criteria);
    assertEquals(mockAuditEntries, result);
  }

  @Test
  void testStreamForExport_shouldDelegateToRepository() {
    // Given
    AuditRepository.AuditSearchCriteria criteria =
        AuditRepository.AuditSearchCriteria.builder().entityType("Customer").build();

    Stream<AuditEntry> mockStream = mockAuditEntries.stream();
    when(auditRepository.streamForExport(criteria)).thenReturn(mockStream);

    // When
    Stream<AuditEntry> result = queryService.streamForExport(criteria);

    // Then
    verify(auditRepository).streamForExport(criteria);
    assertNotNull(result);
  }

  @Test
  void testGetStatistics_shouldDelegateToRepository() {
    // Given
    AuditRepository.AuditStatistics mockStats =
        AuditRepository.AuditStatistics.builder()
            .totalEvents(100L)
            .uniqueUsers(10L)
            .uniqueEntities(50L)
            .failureCount(5L)
            .period(testFrom, testTo)
            .build();

    when(auditRepository.getStatistics(testFrom, testTo)).thenReturn(mockStats);

    // When
    AuditRepository.AuditStatistics result = queryService.getStatistics(testFrom, testTo);

    // Then
    verify(auditRepository).getStatistics(testFrom, testTo);
    assertEquals(100L, result.getTotalEvents());
    assertEquals(10L, result.getUniqueUsers());
    assertEquals(50L, result.getUniqueEntities());
    assertEquals(5L, result.getFailureCount());
  }

  @Test
  void testGetDashboardMetrics_shouldDelegateToRepository() {
    // Given
    AuditRepository.DashboardMetrics mockMetrics = new AuditRepository.DashboardMetrics();
    mockMetrics.totalEventsToday = 50L;
    mockMetrics.activeUsers = 5L;
    mockMetrics.criticalEventsToday = 2L;
    mockMetrics.coverage = 85.5;
    mockMetrics.integrityStatus = "valid";
    mockMetrics.retentionCompliance = 100;
    mockMetrics.lastAudit = Instant.now().toString();

    when(auditRepository.getDashboardMetrics()).thenReturn(mockMetrics);

    // When
    AuditRepository.DashboardMetrics result = queryService.getDashboardMetrics();

    // Then
    verify(auditRepository).getDashboardMetrics();
    assertEquals(50L, result.totalEventsToday);
    assertEquals(5L, result.activeUsers);
    assertEquals(2L, result.criticalEventsToday);
    assertEquals(85.5, result.coverage);
    assertEquals("valid", result.integrityStatus);
  }

  @Test
  void testGetActivityChartData_shouldDelegateToRepository() {
    // Given
    int days = 7;
    String groupBy = "day";
    List<Map<String, Object>> mockChartData =
        List.of(
            Map.of("time", "2025-08-01", "value", 10L),
            Map.of("time", "2025-08-02", "value", 15L),
            Map.of("time", "2025-08-03", "value", 20L));

    when(auditRepository.getActivityChartData(days, groupBy)).thenReturn(mockChartData);

    // When
    List<Map<String, Object>> result = queryService.getActivityChartData(days, groupBy);

    // Then
    verify(auditRepository).getActivityChartData(days, groupBy);
    assertEquals(3, result.size());
    assertEquals(10L, result.get(0).get("value"));
  }

  @Test
  void testGetCriticalEvents_shouldDelegateToRepository() {
    // Given
    int limit = 10;
    when(auditRepository.getCriticalEvents(limit)).thenReturn(mockAuditEntries);

    // When
    List<AuditEntry> result = queryService.getCriticalEvents(limit);

    // Then
    verify(auditRepository).getCriticalEvents(limit);
    assertEquals(mockAuditEntries, result);
  }

  @Test
  void testFindRequiringNotification_shouldDelegateToRepository() {
    // Given
    when(auditRepository.findRequiringNotification()).thenReturn(mockAuditEntries);

    // When
    List<AuditEntry> result = queryService.findRequiringNotification();

    // Then
    verify(auditRepository).findRequiringNotification();
    assertEquals(mockAuditEntries, result);
  }

  @Test
  void testVerifyIntegrity_shouldDelegateToRepository() {
    // Given
    List<AuditRepository.AuditIntegrityIssue> mockIssues =
        List.of(
            new AuditRepository.AuditIntegrityIssue(
                UUID.randomUUID(), "Hash chain broken", "expected-hash", "actual-hash"));

    when(auditRepository.verifyIntegrity(testFrom, testTo)).thenReturn(mockIssues);

    // When
    List<AuditRepository.AuditIntegrityIssue> result =
        queryService.verifyIntegrity(testFrom, testTo);

    // Then
    verify(auditRepository).verifyIntegrity(testFrom, testTo);
    assertEquals(1, result.size());
    assertEquals("Hash chain broken", result.get(0).getIssue());
  }

  @Test
  void testGetComplianceAlerts_shouldDelegateToRepository() {
    // Given
    List<ComplianceAlertDto> mockAlerts =
        List.of(
            ComplianceAlertDto.builder()
                .id("alert-1")
                .type(ComplianceAlertDto.AlertType.RETENTION)
                .severity(ComplianceAlertDto.AlertSeverity.WARNING)
                .title("Test Alert")
                .description("Test Description")
                .build());

    when(auditRepository.getComplianceAlerts()).thenReturn(mockAlerts);

    // When
    List<ComplianceAlertDto> result = queryService.getComplianceAlerts();

    // Then
    verify(auditRepository).getComplianceAlerts();
    assertEquals(1, result.size());
    assertEquals("Test Alert", result.get(0).getTitle());
  }

  @Test
  void testGetLastHash_shouldDelegateToRepository() {
    // Given
    String expectedHash = "abc123def456";
    when(auditRepository.getLastHash()).thenReturn(Optional.of(expectedHash));

    // When
    String result = queryService.getLastHash();

    // Then
    verify(auditRepository).getLastHash();
    assertEquals(expectedHash, result);
  }

  @Test
  void testGetLastHash_whenEmpty_shouldReturnNull() {
    // Given
    when(auditRepository.getLastHash()).thenReturn(Optional.empty());

    // When
    String result = queryService.getLastHash();

    // Then
    verify(auditRepository).getLastHash();
    assertNull(result);
  }

  @Test
  void testFindByFilters_shouldDelegateToRepository() {
    // Given
    ExportRequest request = new ExportRequest();
    request.setEntityType("Customer");
    request.setPage(0);
    request.setSize(100);

    when(auditRepository.findByFilters(request)).thenReturn(mockAuditEntries);

    // When
    List<AuditEntry> result = queryService.findByFilters(request);

    // Then
    verify(auditRepository).findByFilters(request);
    assertEquals(mockAuditEntries, result);
  }

  @Test
  void testCountByFilters_shouldDelegateToRepository() {
    // Given
    ExportRequest request = new ExportRequest();
    request.setEntityType("Customer");

    when(auditRepository.countByFilters(request)).thenReturn(100L);

    // When
    long result = queryService.countByFilters(request);

    // Then
    verify(auditRepository).countByFilters(request);
    assertEquals(100L, result);
  }

  @Test
  void testCountOlderThan_shouldDelegateToRepository() {
    // Given
    Instant cutoffDate = Instant.now().minusSeconds(90 * 24 * 60 * 60); // 90 days ago
    when(auditRepository.deleteOlderThan(cutoffDate, true)).thenReturn(50L);

    // When
    long result = queryService.countOlderThan(cutoffDate);

    // Then
    verify(auditRepository).deleteOlderThan(cutoffDate, true);
    assertEquals(50L, result);
  }

  @Test
  void testDeleteOlderThan_shouldDelegateToRepository() {
    // Given
    Instant cutoffDate = Instant.now().minusSeconds(90 * 24 * 60 * 60); // 90 days ago
    when(auditRepository.deleteOlderThan(cutoffDate, false)).thenReturn(50L);

    // When
    long result = queryService.deleteOlderThan(cutoffDate);

    // Then
    verify(auditRepository).deleteOlderThan(cutoffDate, false);
    assertEquals(50L, result);
  }

  // Helper method to create mock audit entries
  private List<AuditEntry> createMockAuditEntries() {
    List<AuditEntry> entries = new ArrayList<>();

    for (int i = 0; i < 3; i++) {
      AuditEntry entry =
          AuditEntry.builder()
              .timestamp(Instant.now().minusSeconds(i * 3600))
              .eventType(AuditEventType.CUSTOMER_CREATED)
              .entityType("Customer")
              .entityId(testEntityId)
              .userId(testUserId)
              .userName("Test User " + i)
              .userRole("ADMIN")
              .changeReason("Test reason " + i)
              .build();
      entries.add(entry);
    }

    return entries;
  }
}
