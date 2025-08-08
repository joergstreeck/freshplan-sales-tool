package de.freshplan.audit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.freshplan.audit.entity.AuditLog;
import de.freshplan.audit.entity.AuditLog.*;
import de.freshplan.audit.repository.AuditRepository;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;
import jakarta.inject.Inject;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@QuarkusTest
@DisplayName("AuditService Tests")
class AuditServiceTest {

  @Inject AuditService auditService;

  @InjectMock AuditRepository auditRepository;

  @InjectMock SecurityIdentity securityIdentity;

  @InjectMock HttpServerRequest request;

  private UUID testUserId;
  private UUID testEntityId;
  private String testUserName = "test.user@freshplan.de";

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    testEntityId = UUID.randomUUID();

    // Mock Security Identity
    Principal principal = mock(Principal.class);
    when(principal.getName()).thenReturn(testUserName);
    when(securityIdentity.getPrincipal()).thenReturn(principal);
    when(securityIdentity.getAttribute("sub")).thenReturn(testUserId.toString());
    when(securityIdentity.getRoles()).thenReturn(Set.of("admin"));

    // Mock Request
    SocketAddress remoteAddress = mock(SocketAddress.class);
    when(remoteAddress.hostAddress()).thenReturn("192.168.1.100");
    when(request.remoteAddress()).thenReturn(remoteAddress);
    when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
    when(request.getHeader("X-Session-Id")).thenReturn("session-123");
    when(request.getHeader("X-Request-Id")).thenReturn("request-456");

    // Mock Repository
    when(auditRepository.findLastEntry()).thenReturn(Optional.empty());
    doNothing().when(auditRepository).persist(any(AuditLog.class));
  }

  @Test
  @DisplayName("Should create audit log for CREATE action")
  void testAuditCreate() {
    // Given
    CustomerTestData customer = new CustomerTestData("Test Customer GmbH");

    // When
    auditService.auditCreate(EntityType.CUSTOMER, testEntityId, customer.name, customer);

    // Then
    ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
    verify(auditRepository).persist(captor.capture());

    AuditLog captured = captor.getValue();
    assertEquals(EntityType.CUSTOMER, captured.getEntityType());
    assertEquals(testEntityId, captured.getEntityId());
    assertEquals(AuditAction.CREATE, captured.getAction());
    assertEquals(testUserName, captured.getUserName());
    assertEquals(testUserId, captured.getUserId());
    assertNotNull(captured.getNewValues());
    assertNull(captured.getOldValues());
    assertEquals("192.168.1.100", captured.getIpAddress());
  }

  @Test
  @DisplayName("Should create audit log for UPDATE action with changed fields detection")
  void testAuditUpdate() {
    // Given
    CustomerTestData oldCustomer = new CustomerTestData("Old Name");
    CustomerTestData newCustomer = new CustomerTestData("New Name");

    // When
    auditService.auditUpdate(
        EntityType.CUSTOMER, testEntityId, newCustomer.name, oldCustomer, newCustomer);

    // Then
    ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
    verify(auditRepository).persist(captor.capture());

    AuditLog captured = captor.getValue();
    assertEquals(AuditAction.UPDATE, captured.getAction());
    assertNotNull(captured.getOldValues());
    assertNotNull(captured.getNewValues());
    assertNotNull(captured.getChangedFields());
    assertTrue(captured.getChangedFieldsAsSet().contains("name"));
  }

  @Test
  @DisplayName("Should create audit log for DELETE action with reason")
  void testAuditDelete() {
    // Given
    CustomerTestData customer = new CustomerTestData("Deleted Customer");
    String deleteReason = "Customer requested data deletion";

    // When
    auditService.auditDelete(
        EntityType.CUSTOMER, testEntityId, customer.name, customer, deleteReason);

    // Then
    ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
    verify(auditRepository).persist(captor.capture());

    AuditLog captured = captor.getValue();
    assertEquals(AuditAction.DELETE, captured.getAction());
    assertEquals(deleteReason, captured.getReason());
    assertNotNull(captured.getOldValues());
    assertNull(captured.getNewValues());
    assertTrue(captured.isCritical());
  }

  @Test
  @DisplayName("Should mark DSGVO-relevant actions correctly")
  void testDsgvoRelevance() {
    // When
    auditService.auditDataAccess(
        EntityType.CUSTOMER, testEntityId, "Test Customer", AuditAction.EXPORT);

    // Then
    ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
    verify(auditRepository).persist(captor.capture());

    AuditLog captured = captor.getValue();
    assertTrue(captured.getIsDsgvoRelevant());
    assertEquals(LegalBasis.LEGITIMATE_INTERESTS, captured.getLegalBasis());
  }

  @Test
  @DisplayName("Should handle consent operations")
  void testAuditConsent() {
    // Given
    UUID consentId = UUID.randomUUID();
    String purpose = "Marketing emails";

    // When
    auditService.auditConsent(testEntityId, "Test Customer", true, consentId, purpose);

    // Then
    ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
    verify(auditRepository).persist(captor.capture());

    AuditLog captured = captor.getValue();
    assertEquals(AuditAction.CONSENT_GIVEN, captured.getAction());
    assertEquals(consentId, captured.getConsentId());
    assertEquals(purpose, captured.getReason());
    assertTrue(captured.getIsDsgvoRelevant());
    assertEquals(LegalBasis.CONSENT, captured.getLegalBasis());
  }

  @Test
  @DisplayName("Should calculate hash chain correctly")
  void testHashChainCalculation() {
    // Given
    AuditLog previousLog = new AuditLog();
    previousLog.setCurrentHash("previous-hash-abc123");
    when(auditRepository.findLastEntry()).thenReturn(Optional.of(previousLog));

    // When
    auditService.auditSimple(
        EntityType.CUSTOMER, testEntityId, "Test Customer", AuditAction.VIEW);

    // Then
    ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
    verify(auditRepository).persist(captor.capture());

    AuditLog captured = captor.getValue();
    assertEquals("previous-hash-abc123", captured.getPreviousHash());
    assertNotNull(captured.getCurrentHash());
    assertNotEquals("", captured.getCurrentHash());
  }

  @Test
  @DisplayName("Should handle system user when no security identity")
  void testSystemUserAudit() {
    // Given
    when(securityIdentity.getPrincipal()).thenReturn(null);

    // When
    auditService.auditSimple(
        EntityType.SYSTEM, testEntityId, "System Process", AuditAction.SYSTEM_EVENT);

    // Then
    ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
    verify(auditRepository).persist(captor.capture());

    AuditLog captured = captor.getValue();
    assertEquals("SYSTEM", captured.getUserName());
    assertEquals("SYSTEM", captured.getUserRole());
  }

  @Test
  @DisplayName("Should verify hash chain integrity")
  void testVerifyIntegrity() {
    // Given
    LocalDateTime from = LocalDateTime.now().minusDays(1);
    LocalDateTime to = LocalDateTime.now();
    when(auditRepository.verifyHashChain(from, to)).thenReturn(true);

    // When
    boolean result = auditService.verifyIntegrity(from, to);

    // Then
    assertTrue(result);
    verify(auditRepository).verifyHashChain(from, to);
  }

  @Test
  @DisplayName("Should generate compliance report")
  void testGenerateComplianceReport() {
    // Given
    LocalDateTime from = LocalDateTime.now().minusDays(30);
    LocalDateTime to = LocalDateTime.now();

    List<AuditLog> dsgvoEvents = createDsgvoTestData();
    when(auditRepository.findDsgvoRelevant(from, to)).thenReturn(dsgvoEvents);
    when(auditRepository.verifyHashChain(from, to)).thenReturn(true);

    // When
    Map<String, Object> report = auditService.generateComplianceReport(from, to);

    // Then
    assertNotNull(report);
    assertEquals(dsgvoEvents.size(), report.get("dsgvoEventCount"));
    assertTrue((Boolean) report.get("hashChainValid"));
    assertNotNull(report.get("byLegalBasis"));
    assertNotNull(report.get("dataSubjectRequests"));
    assertNotNull(report.get("consentOperations"));
  }

  @Test
  @DisplayName("Should handle exceptions gracefully")
  void testErrorHandling() {
    // Given
    when(auditRepository.findLastEntry()).thenThrow(new RuntimeException("Database error"));

    // When
    AuditLog result =
        auditService.audit(
            EntityType.CUSTOMER,
            testEntityId,
            "Test",
            AuditAction.CREATE,
            null,
            null,
            null,
            null);

    // Then
    assertNull(result); // Should return null on error, not throw
  }

  @Test
  @DisplayName("Should detect critical actions")
  void testCriticalActionDetection() {
    // Test critical actions
    for (AuditAction action :
        Arrays.asList(
            AuditAction.DELETE,
            AuditAction.BULK_DELETE,
            AuditAction.PERMISSION_CHANGE,
            AuditAction.DATA_DELETION)) {

      // When
      auditService.auditSimple(EntityType.CUSTOMER, testEntityId, "Test", action);

      // Then
      ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
      verify(auditRepository, atLeastOnce()).persist(captor.capture());

      AuditLog captured = captor.getValue();
      assertTrue(captured.isCritical(), "Action " + action + " should be critical");
    }
  }

  @Test
  @DisplayName("Should set retention policy correctly")
  void testRetentionPolicy() {
    // When - Data deletion (10 years retention)
    auditService.auditSimple(
        EntityType.CUSTOMER, testEntityId, "Test", AuditAction.DATA_DELETION);

    // Then
    ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
    verify(auditRepository).persist(captor.capture());

    AuditLog captured = captor.getValue();
    assertNotNull(captured.getRetentionUntil());
    assertTrue(
        captured.getRetentionUntil().isAfter(LocalDateTime.now().plusYears(9)),
        "DATA_DELETION should have 10 years retention");
  }

  @Test
  @DisplayName("Should handle null values in audit")
  void testNullValueHandling() {
    // When
    AuditLog result =
        auditService.audit(
            EntityType.CUSTOMER, testEntityId, null, AuditAction.VIEW, null, null, null, null);

    // Then
    assertNotNull(result);
    assertNull(result.getEntityName());
    assertNull(result.getOldValues());
    assertNull(result.getNewValues());
    assertNull(result.getReason());
    assertNull(result.getComment());
  }

  // Helper Methods

  private List<AuditLog> createDsgvoTestData() {
    List<AuditLog> events = new ArrayList<>();

    AuditLog consent = new AuditLog();
    consent.setAction(AuditAction.CONSENT_GIVEN);
    consent.setIsDsgvoRelevant(true);
    consent.setLegalBasis(LegalBasis.CONSENT);
    events.add(consent);

    AuditLog dataRequest = new AuditLog();
    dataRequest.setAction(AuditAction.DATA_REQUEST);
    dataRequest.setIsDsgvoRelevant(true);
    dataRequest.setLegalBasis(LegalBasis.LEGITIMATE_INTERESTS);
    events.add(dataRequest);

    AuditLog export = new AuditLog();
    export.setAction(AuditAction.EXPORT);
    export.setIsDsgvoRelevant(true);
    export.setLegalBasis(LegalBasis.CONTRACT);
    events.add(export);

    return events;
  }

  // Test Data Class
  static class CustomerTestData {
    public String name;

    public CustomerTestData(String name) {
      this.name = name;
    }
  }
}