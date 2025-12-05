package de.freshplan.modules.leads.service;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.modules.leads.domain.ImportLog;
import de.freshplan.modules.leads.domain.ImportLog.ImportLogStatus;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.Territory;
import de.freshplan.modules.leads.service.ImportQuotaService.QuotaCheckResult;
import de.freshplan.modules.leads.service.ImportQuotaService.QuotaInfo;
import de.freshplan.modules.leads.service.ImportQuotaService.UserRole;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration Tests für ImportQuotaService - Sprint 2.1.8 Phase 2.
 *
 * <p>Testet das Quota-System für Self-Service Lead-Import:
 *
 * <ul>
 *   <li>Max. offene Leads pro User (nach Rolle)
 *   <li>Max. Imports pro Tag (nach Rolle)
 *   <li>Max. Leads pro Import (nach Rolle)
 * </ul>
 */
@QuarkusTest
@Transactional
class ImportQuotaServiceTest {

  @Inject ImportQuotaService quotaService;

  @Inject EntityManager em;

  private static final String TEST_USER = "quota-test-user";
  private String testPrefix;

  @BeforeEach
  void setup() {
    testPrefix = "QUOTA-TEST-" + UUID.randomUUID().toString().substring(0, 8);

    // Ensure territory exists
    Territory territory = Territory.findByCode("DE");
    if (territory == null) {
      territory = new Territory();
      territory.id = "DE";
      territory.name = "Deutschland";
      territory.countryCode = "DE";
      territory.currencyCode = "EUR";
      territory.taxRate = new BigDecimal("19.0");
      territory.languageCode = "de-DE";
      territory.persist();
    }
  }

  @AfterEach
  void cleanup() {
    // Clean up test data
    em.createQuery("DELETE FROM ImportLog i WHERE i.userId = :userId")
        .setParameter("userId", TEST_USER)
        .executeUpdate();

    em.createQuery("DELETE FROM Lead l WHERE l.companyName LIKE :prefix")
        .setParameter("prefix", "QUOTA-TEST-%")
        .executeUpdate();
  }

  // ============================================================================
  // Basic Quota Check Tests
  // ============================================================================

  @Test
  @DisplayName("SALES: Quota OK bei leerem Konto")
  void testSalesQuota_EmptyAccount() {
    // When
    QuotaCheckResult result = quotaService.checkQuota(TEST_USER, UserRole.SALES, 10);

    // Then
    assertTrue(result.approved());
    assertEquals("Quota OK", result.message());
  }

  @Test
  @DisplayName("SALES: Abgelehnt bei >100 Leads pro Import")
  void testSalesQuota_TooManyLeadsPerImport() {
    // When
    QuotaCheckResult result = quotaService.checkQuota(TEST_USER, UserRole.SALES, 101);

    // Then
    assertFalse(result.approved());
    assertTrue(result.message().contains("Zu viele Leads"));
    assertTrue(result.message().contains("101"));
    assertTrue(result.message().contains("100"));
  }

  @Test
  @DisplayName("MANAGER: Erlaubt bis 200 Leads pro Import")
  void testManagerQuota_MaxLeadsPerImport() {
    // When
    QuotaCheckResult result = quotaService.checkQuota(TEST_USER, UserRole.MANAGER, 200);

    // Then
    assertTrue(result.approved());
  }

  @Test
  @DisplayName("ADMIN: Erlaubt bis 1000 Leads pro Import")
  void testAdminQuota_MaxLeadsPerImport() {
    // When
    QuotaCheckResult result = quotaService.checkQuota(TEST_USER, UserRole.ADMIN, 1000);

    // Then
    assertTrue(result.approved());
  }

  @Test
  @DisplayName("ADMIN: Abgelehnt bei >1000 Leads pro Import")
  void testAdminQuota_TooManyLeadsPerImport() {
    // When
    QuotaCheckResult result = quotaService.checkQuota(TEST_USER, UserRole.ADMIN, 1001);

    // Then
    assertFalse(result.approved());
  }

  // ============================================================================
  // Daily Import Limit Tests
  // ============================================================================

  @Test
  @DisplayName("SALES: Abgelehnt nach 3 Imports am Tag")
  void testSalesQuota_DailyLimitExceeded() {
    // Given: 3 imports today
    for (int i = 0; i < 3; i++) {
      createImportLog(TEST_USER, ImportLogStatus.COMPLETED);
    }
    em.flush();

    // When
    QuotaCheckResult result = quotaService.checkQuota(TEST_USER, UserRole.SALES, 10);

    // Then
    assertFalse(result.approved());
    assertTrue(result.message().contains("Tageslimit"));
  }

  @Test
  @DisplayName("MANAGER: Erlaubt nach 3 Imports (Limit ist 5)")
  void testManagerQuota_ThreeImportsOk() {
    // Given: 3 imports today
    for (int i = 0; i < 3; i++) {
      createImportLog(TEST_USER, ImportLogStatus.COMPLETED);
    }
    em.flush();

    // When
    QuotaCheckResult result = quotaService.checkQuota(TEST_USER, UserRole.MANAGER, 10);

    // Then
    assertTrue(result.approved());
  }

  @Test
  @DisplayName("SALES: Rejected Imports zählen nicht zum Tageslimit")
  void testSalesQuota_RejectedImportsNotCounted() {
    // Given: 3 rejected imports today
    for (int i = 0; i < 3; i++) {
      createImportLog(TEST_USER, ImportLogStatus.REJECTED);
    }
    em.flush();

    // When
    QuotaCheckResult result = quotaService.checkQuota(TEST_USER, UserRole.SALES, 10);

    // Then
    assertTrue(result.approved(), "Rejected imports should not count toward daily limit");
  }

  // ============================================================================
  // Open Leads Limit Tests
  // ============================================================================

  @Test
  @DisplayName("SALES: Abgelehnt wenn offene Leads + neue > 100")
  void testSalesQuota_OpenLeadsLimitExceeded() {
    // Given: 95 open leads
    for (int i = 0; i < 95; i++) {
      createTestLead(testPrefix + "-Lead-" + i);
    }
    em.flush();

    // When: Try to import 10 more (95 + 10 = 105 > 100)
    QuotaCheckResult result = quotaService.checkQuota(TEST_USER, UserRole.SALES, 10);

    // Then
    assertFalse(result.approved());
    assertTrue(result.message().contains("Lead-Limit"));
  }

  @Test
  @DisplayName("SALES: Erlaubt wenn offene Leads + neue <= 100")
  void testSalesQuota_OpenLeadsOk() {
    // Given: 90 open leads
    for (int i = 0; i < 90; i++) {
      createTestLead(testPrefix + "-Lead-" + i);
    }
    em.flush();

    // When: Try to import 10 more (90 + 10 = 100)
    QuotaCheckResult result = quotaService.checkQuota(TEST_USER, UserRole.SALES, 10);

    // Then
    assertTrue(result.approved());
  }

  @Test
  @DisplayName("GDPR-gelöschte Leads zählen nicht zu offenen Leads")
  void testQuota_DeletedLeadsNotCounted() {
    // Given: 100 leads, 50 davon GDPR-gelöscht
    for (int i = 0; i < 50; i++) {
      createTestLead(testPrefix + "-Active-" + i);
    }
    for (int i = 0; i < 50; i++) {
      Lead lead = createTestLead(testPrefix + "-Deleted-" + i);
      lead.gdprDeleted = true;
      lead.gdprDeletedAt = LocalDateTime.now();
    }
    em.flush();

    // When: Only 50 active leads, so 50 more should fit
    QuotaCheckResult result = quotaService.checkQuota(TEST_USER, UserRole.SALES, 50);

    // Then
    assertTrue(result.approved(), "GDPR-deleted leads should not count toward open leads limit");
  }

  // ============================================================================
  // QuotaInfo Tests
  // ============================================================================

  @Test
  @DisplayName("getQuotaInfo gibt korrekte Werte zurück")
  void testGetQuotaInfo() {
    // Given: 10 open leads, 2 imports today
    for (int i = 0; i < 10; i++) {
      createTestLead(testPrefix + "-Info-" + i);
    }
    createImportLog(TEST_USER, ImportLogStatus.COMPLETED);
    createImportLog(TEST_USER, ImportLogStatus.COMPLETED);
    em.flush();

    // When
    QuotaInfo info = quotaService.getQuotaInfo(TEST_USER, UserRole.SALES);

    // Then
    assertEquals(10, info.currentOpenLeads());
    assertEquals(100, info.maxOpenLeads());
    assertEquals(2, info.todayImports());
    assertEquals(3, info.maxImportsPerDay());
    assertEquals(100, info.maxLeadsPerImport());
    assertEquals(90, info.remainingCapacity()); // 100 - 10
  }

  @Test
  @DisplayName("AUDITOR: Kann nicht importieren")
  void testAuditorQuota_NoImportAllowed() {
    // When
    QuotaCheckResult result = quotaService.checkQuota(TEST_USER, UserRole.AUDITOR, 1);

    // Then
    assertFalse(result.approved());
    assertTrue(result.message().contains("Zu viele Leads"));
  }

  // ============================================================================
  // Helper Methods
  // ============================================================================

  private Lead createTestLead(String companyName) {
    Territory territory = Territory.findByCode("DE");

    Lead lead = new Lead();
    lead.companyName = companyName;
    lead.city = "München";
    lead.territory = territory;
    lead.ownerUserId = TEST_USER;
    lead.status = LeadStatus.REGISTERED;
    lead.createdBy = TEST_USER;
    lead.createdAt = LocalDateTime.now();
    lead.updatedAt = LocalDateTime.now();
    lead.registeredAt = LocalDateTime.now().minusDays(1);
    lead.persist();

    return lead;
  }

  private void createImportLog(String userId, ImportLogStatus status) {
    ImportLog log = new ImportLog();
    log.userId = userId;
    log.importedAt = LocalDateTime.now();
    log.totalRows = 10;
    log.importedCount = 10;
    log.skippedCount = 0;
    log.errorCount = 0;
    log.status = status;
    log.persist();
  }
}
