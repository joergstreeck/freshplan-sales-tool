package de.freshplan.modules.leads.service;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.shared.BusinessType;
import de.freshplan.domain.shared.KitchenSize;
import de.freshplan.modules.leads.domain.ActivityType;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadActivity;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.Territory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration Tests für GdprPdfGeneratorService.
 *
 * <p>Sprint 2.1.8 - Phase 1: DSGVO Compliance
 *
 * <p>Testet PDF-Generierung für DSGVO Art. 15 Datenexport:
 *
 * <ul>
 *   <li>PDF-Struktur und Magic Bytes
 *   <li>Alle Lead-Felder im PDF
 *   <li>Aktivitäten-Sektion
 *   <li>Consent-Status
 *   <li>DSGVO-Löschstatus
 * </ul>
 */
@QuarkusTest
@Transactional
class GdprPdfGeneratorServiceTest {

  @Inject GdprPdfGeneratorService pdfService;

  @Inject EntityManager em;

  private Lead testLead;
  private String testPrefix;

  @BeforeEach
  void setup() {
    testPrefix = "PDF-TEST-" + UUID.randomUUID().toString().substring(0, 8);

    // Ensure territory exists
    Territory territory = Territory.findByCode("DE");
    if (territory == null) {
      territory = new Territory();
      territory.id = "DE";
      territory.name = "Deutschland";
      territory.countryCode = "DE";
      territory.currencyCode = "EUR";
      territory.taxRate = new BigDecimal("19.0");
      territory.persist();
    }

    // Create comprehensive test lead
    testLead = new Lead();
    testLead.companyName = testPrefix + " Premium Catering GmbH";
    testLead.contactPerson = "Dr. Hans Müller";
    testLead.email = "hans.mueller@" + testPrefix.toLowerCase() + ".de";
    testLead.phone = "+49 89 123456789";
    testLead.street = "Maximilianstraße 1";
    testLead.city = "München";
    testLead.postalCode = "80539";
    testLead.countryCode = "DE";
    testLead.website = "https://www." + testPrefix.toLowerCase() + ".de";
    testLead.businessType = BusinessType.RESTAURANT;
    testLead.kitchenSize = KitchenSize.GROSS;
    testLead.employeeCount = 50;
    testLead.estimatedVolume = new BigDecimal("75000.00");
    testLead.branchCount = 3;
    testLead.isChain = true;
    testLead.status = LeadStatus.REGISTERED;
    testLead.registeredAt = LocalDateTime.now().minusDays(30);
    testLead.protectionStartAt = LocalDateTime.now().minusDays(30);
    testLead.protectionMonths = 12;
    testLead.territory = territory;
    testLead.createdBy = "test-user";
    testLead.createdAt = LocalDateTime.now().minusDays(30);
    testLead.updatedAt = LocalDateTime.now();
    testLead.activities = new ArrayList<>();
    testLead.persist();

    em.flush();
  }

  @AfterEach
  void cleanup() {
    em.createQuery("DELETE FROM LeadActivity a WHERE a.lead.id = :leadId")
        .setParameter("leadId", testLead.id)
        .executeUpdate();

    em.createQuery("DELETE FROM Lead l WHERE l.companyName LIKE :prefix")
        .setParameter("prefix", "PDF-TEST-%")
        .executeUpdate();
  }

  // ============================================================================
  // PDF Structure Tests
  // ============================================================================

  @Test
  @DisplayName("PDF-Struktur: Generiert valides PDF mit Magic Bytes")
  void testGeneratePdf_ValidMagicBytes() {
    // When
    byte[] pdf = pdfService.generateLeadDataExport(testLead);

    // Then
    assertNotNull(pdf);
    assertTrue(pdf.length > 100, "PDF sollte mehr als 100 Bytes haben");

    // PDF Magic Bytes: %PDF-
    assertEquals((byte) 0x25, pdf[0]); // %
    assertEquals((byte) 0x50, pdf[1]); // P
    assertEquals((byte) 0x44, pdf[2]); // D
    assertEquals((byte) 0x46, pdf[3]); // F
    assertEquals((byte) 0x2D, pdf[4]); // -
  }

  @Test
  @DisplayName("PDF-Struktur: PDF enthält EOF-Marker")
  void testGeneratePdf_ContainsEof() {
    // When
    byte[] pdf = pdfService.generateLeadDataExport(testLead);

    // Then
    String pdfEnd = new String(pdf, pdf.length - 10, 10);
    assertTrue(
        pdfEnd.contains("%%EOF") || containsEofSequence(pdf), "PDF sollte %%EOF Marker enthalten");
  }

  private boolean containsEofSequence(byte[] pdf) {
    // Look for %%EOF in last 100 bytes
    int start = Math.max(0, pdf.length - 100);
    String tail = new String(pdf, start, pdf.length - start);
    return tail.contains("%%EOF");
  }

  // ============================================================================
  // Content Tests
  // ============================================================================

  @Test
  @DisplayName("PDF-Inhalt: PDF hat vernünftige Größe")
  void testGeneratePdf_ReasonableSize() {
    // When
    byte[] pdf = pdfService.generateLeadDataExport(testLead);

    // Then
    // A simple PDF with our content should be between 5KB and 500KB
    assertTrue(pdf.length > 5000, "PDF sollte mindestens 5KB haben");
    assertTrue(pdf.length < 500000, "PDF sollte unter 500KB bleiben");
  }

  @Test
  @DisplayName("PDF-Inhalt: PDF mit Aktivitäten ist größer")
  void testGeneratePdf_WithActivities() {
    // Given - Add activities to lead
    for (int i = 0; i < 5; i++) {
      LeadActivity activity = new LeadActivity();
      activity.lead = testLead;
      activity.activityType = ActivityType.CALL;
      activity.activityDate = LocalDateTime.now().minusDays(i);
      activity.description = "Telefonat #" + (i + 1) + " mit Kunden";
      activity.userId = "test-user";
      activity.createdAt = LocalDateTime.now();
      activity.persist();
      testLead.activities.add(activity);
    }
    em.flush();

    // When
    byte[] pdfWithActivities = pdfService.generateLeadDataExport(testLead);

    // Create lead without activities for comparison
    Lead emptyLead = new Lead();
    emptyLead.companyName = testPrefix + " Empty Lead";
    emptyLead.status = LeadStatus.REGISTERED;
    emptyLead.registeredAt = LocalDateTime.now();
    emptyLead.territory = testLead.territory;
    emptyLead.createdBy = "test";
    emptyLead.createdAt = LocalDateTime.now();
    emptyLead.updatedAt = LocalDateTime.now();
    emptyLead.activities = new ArrayList<>();
    emptyLead.persist();
    em.flush();

    byte[] pdfWithoutActivities = pdfService.generateLeadDataExport(emptyLead);

    // Then - PDF with activities should be larger
    assertTrue(
        pdfWithActivities.length > pdfWithoutActivities.length,
        "PDF mit Aktivitäten sollte größer sein als ohne");
  }

  // ============================================================================
  // Edge Cases
  // ============================================================================

  @Test
  @DisplayName("Edge Case: Generiert PDF mit minimalen Daten")
  void testGeneratePdf_MinimalData() {
    // Given - Minimal lead
    Lead minimalLead = new Lead();
    minimalLead.companyName = testPrefix + " Minimal GmbH";
    minimalLead.status = LeadStatus.REGISTERED;
    minimalLead.registeredAt = LocalDateTime.now();
    minimalLead.territory = testLead.territory;
    minimalLead.createdBy = "test";
    minimalLead.createdAt = LocalDateTime.now();
    minimalLead.updatedAt = LocalDateTime.now();
    minimalLead.activities = new ArrayList<>();
    minimalLead.persist();
    em.flush();

    // When
    byte[] pdf = pdfService.generateLeadDataExport(minimalLead);

    // Then
    assertNotNull(pdf);
    assertTrue(pdf.length > 0);
    assertEquals((byte) 0x25, pdf[0]); // PDF magic byte
  }

  @Test
  @DisplayName("Edge Case: Generiert PDF mit widerrufener Einwilligung")
  void testGeneratePdf_ConsentRevoked() {
    // Given
    testLead.consentRevokedAt = LocalDateTime.now().minusDays(1);
    testLead.consentRevokedBy = "admin-user";
    testLead.contactBlocked = true;
    em.merge(testLead);
    em.flush();

    // When
    byte[] pdf = pdfService.generateLeadDataExport(testLead);

    // Then
    assertNotNull(pdf);
    assertTrue(pdf.length > 0);
  }

  @Test
  @DisplayName("Edge Case: Generiert PDF mit DSGVO-Löschung")
  void testGeneratePdf_GdprDeleted() {
    // Given
    testLead.gdprDeleted = true;
    testLead.gdprDeletedAt = LocalDateTime.now().minusHours(2);
    testLead.gdprDeletedBy = "admin-user";
    testLead.gdprDeletionReason = "Art. 17 - Betroffener hat Löschung beantragt";
    testLead.companyName = "DSGVO-GELÖSCHT-12345678";
    testLead.contactPerson = null;
    testLead.email = null;
    testLead.phone = null;
    em.merge(testLead);
    em.flush();

    // When
    byte[] pdf = pdfService.generateLeadDataExport(testLead);

    // Then
    assertNotNull(pdf);
    assertTrue(pdf.length > 0);
  }

  @Test
  @DisplayName("Edge Case: Generiert PDF mit vielen Aktivitäten (max 50)")
  void testGeneratePdf_ManyActivities() {
    // Given - Add 60 activities (more than the 50 limit)
    for (int i = 0; i < 60; i++) {
      LeadActivity activity = new LeadActivity();
      activity.lead = testLead;
      activity.activityType = ActivityType.NOTE;
      activity.activityDate = LocalDateTime.now().minusDays(i);
      activity.description = "Notiz #" + (i + 1);
      activity.userId = "test-user";
      activity.createdAt = LocalDateTime.now();
      activity.persist();
      testLead.activities.add(activity);
    }
    em.flush();

    // When
    byte[] pdf = pdfService.generateLeadDataExport(testLead);

    // Then - Should not throw, should handle gracefully
    assertNotNull(pdf);
    assertTrue(pdf.length > 0);
  }

  @Test
  @DisplayName("Edge Case: Wirft Exception für null Lead")
  void testGeneratePdf_NullLead() {
    // When & Then
    assertThrows(NullPointerException.class, () -> pdfService.generateLeadDataExport(null));
  }
}
