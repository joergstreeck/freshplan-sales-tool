package de.freshplan.modules.leads.service;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.modules.leads.domain.GdprDataRequest;
import de.freshplan.modules.leads.domain.GdprDeletionLog;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.Territory;
import de.freshplan.modules.leads.service.GdprService.GdprDeletionBlockedException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration Tests für GdprService - DSGVO Compliance.
 *
 * <p>Sprint 2.1.8 - Phase 1: DSGVO Compliance
 *
 * <p>Testet gegen echte PostgreSQL-Datenbank (Stage 3 - E2E Tests):
 *
 * <ul>
 *   <li>Art. 15: Datenexport (generateDataExport)
 *   <li>Art. 17: Löschrecht (gdprDeleteLead, PII-Anonymisierung)
 *   <li>Art. 7.3: Einwilligungswiderruf (revokeConsent)
 *   <li>Kontakt-Prüfung (isContactAllowed)
 * </ul>
 *
 * <p><strong>Test-Pattern:</strong> Self-Contained mit UUID-Isolation
 */
@QuarkusTest
@Transactional
class GdprServiceTest {

  @Inject GdprService gdprService;

  @Inject EntityManager em;

  private static final String TEST_USER = "gdpr-test-user";

  private Lead testLead;
  private String testPrefix;

  @BeforeEach
  void setup() {
    // Unique prefix for this test run (Self-Contained Pattern)
    testPrefix = "GDPR-TEST-" + UUID.randomUUID().toString().substring(0, 8);

    // Ensure territory exists
    Territory territory = Territory.findByCode("DE");
    if (territory == null) {
      territory = new Territory();
      territory.id = "DE";
      territory.name = "Deutschland";
      territory.countryCode = "DE";
      territory.currencyCode = "EUR";
      territory.languageCode = "de-DE";
      territory.taxRate = new BigDecimal("19.0");
      territory.persist();
    }

    // Create test lead with PII data
    testLead = new Lead();
    testLead.companyName = testPrefix + " Catering GmbH";
    testLead.contactPerson = "Max Mustermann";
    testLead.email = "max.mustermann@" + testPrefix.toLowerCase() + ".de";
    testLead.phone = "+49 89 12345678";
    testLead.street = "Teststraße 123";
    testLead.city = "München";
    testLead.postalCode = "80331";
    testLead.website = "https://www." + testPrefix.toLowerCase() + ".de";
    testLead.status = LeadStatus.REGISTERED;
    testLead.registeredAt = LocalDateTime.now().minusDays(5);
    testLead.territory = territory;
    testLead.createdBy = TEST_USER;
    testLead.createdAt = LocalDateTime.now();
    testLead.updatedAt = LocalDateTime.now();
    testLead.persist();

    em.flush();
  }

  @AfterEach
  void cleanup() {
    // Clean up in correct order (FK constraints)
    em.createQuery("DELETE FROM GdprDeletionLog g WHERE g.entityId = :leadId")
        .setParameter("leadId", testLead.id)
        .executeUpdate();

    em.createQuery("DELETE FROM GdprDataRequest g WHERE g.entityId = :leadId")
        .setParameter("leadId", testLead.id)
        .executeUpdate();

    em.createQuery("DELETE FROM Opportunity o WHERE o.lead.id = :leadId")
        .setParameter("leadId", testLead.id)
        .executeUpdate();

    em.createQuery("DELETE FROM LeadActivity a WHERE a.lead.id = :leadId")
        .setParameter("leadId", testLead.id)
        .executeUpdate();

    em.createQuery("DELETE FROM Lead l WHERE l.companyName LIKE :prefix")
        .setParameter("prefix", "GDPR-TEST-%")
        .executeUpdate();
  }

  // ============================================================================
  // Art. 15 - Auskunftsrecht (Datenexport)
  // ============================================================================

  @Test
  @DisplayName("Art. 15: Generiert PDF-Export für gültigen Lead")
  void testGenerateDataExport_Success() {
    // When
    byte[] pdf = gdprService.generateDataExport(testLead.id, TEST_USER);

    // Then
    assertNotNull(pdf);
    assertTrue(pdf.length > 0, "PDF sollte nicht leer sein");

    // PDF Header check (magic bytes: %PDF)
    assertEquals((byte) 0x25, pdf[0]); // %
    assertEquals((byte) 0x50, pdf[1]); // P
    assertEquals((byte) 0x44, pdf[2]); // D
    assertEquals((byte) 0x46, pdf[3]); // F
  }

  @Test
  @DisplayName("Art. 15: Erstellt GdprDataRequest Audit-Eintrag")
  void testGenerateDataExport_CreatesAuditRecord() {
    // When
    gdprService.generateDataExport(testLead.id, TEST_USER);

    // Then
    List<GdprDataRequest> requests = gdprService.getDataRequestsForLead(testLead.id);
    assertFalse(requests.isEmpty(), "Mindestens ein Request sollte existieren");

    GdprDataRequest request = requests.get(0);
    assertEquals("LEAD", request.entityType);
    assertEquals(testLead.id, request.entityId);
    assertEquals(TEST_USER, request.requestedBy);
    assertTrue(request.pdfGenerated, "PDF should be marked as generated");
    assertNotNull(request.pdfGeneratedAt);
  }

  @Test
  @DisplayName("Art. 15: Wirft Exception für nicht existierenden Lead")
  void testGenerateDataExport_LeadNotFound() {
    // Given
    Long invalidId = 999999L;

    // When & Then
    assertThrows(
        IllegalArgumentException.class, () -> gdprService.generateDataExport(invalidId, TEST_USER));
  }

  // ============================================================================
  // Art. 17 - Löschrecht (Soft-Delete + PII-Anonymisierung)
  // ============================================================================

  @Test
  @DisplayName("Art. 17: Anonymisiert alle PII-Felder")
  void testGdprDeleteLead_AnonymizesPii() {
    // When
    gdprService.gdprDeleteLead(
        testLead.id, TEST_USER, "Art. 17 - Betroffener hat Löschung beantragt");

    // Then - Reload entity from DB
    em.flush();
    em.clear();
    testLead = Lead.findById(testLead.id);

    // PII fields should be null
    assertNull(testLead.email, "Email sollte anonymisiert sein");
    assertNull(testLead.emailNormalized, "Email normalized sollte anonymisiert sein");
    assertNull(testLead.phone, "Phone sollte anonymisiert sein");
    assertNull(testLead.phoneE164, "Phone E164 sollte anonymisiert sein");
    assertNull(testLead.contactPerson, "Contact person sollte anonymisiert sein");
    assertNull(testLead.street, "Street sollte anonymisiert sein");
    assertNull(testLead.city, "City sollte anonymisiert sein");
    assertNull(testLead.postalCode, "Postal code sollte anonymisiert sein");
    assertNull(testLead.website, "Website sollte anonymisiert sein");
    assertNull(testLead.websiteDomain, "Website domain sollte anonymisiert sein");

    // Company name should be anonymized with prefix
    assertTrue(
        testLead.companyName.startsWith("DSGVO-GELÖSCHT-"),
        "Company name sollte mit DSGVO-GELÖSCHT- beginnen");

    // GDPR flags should be set
    assertTrue(testLead.gdprDeleted, "gdprDeleted flag sollte gesetzt sein");
    assertNotNull(testLead.gdprDeletedAt, "gdprDeletedAt sollte gesetzt sein");
    assertEquals(TEST_USER, testLead.gdprDeletedBy);
    assertNotNull(testLead.gdprDeletionReason);

    // Contact should be blocked
    assertTrue(testLead.contactBlocked, "contactBlocked sollte gesetzt sein");
  }

  @Test
  @DisplayName("Art. 17: Erstellt GdprDeletionLog mit Hash")
  void testGdprDeleteLead_CreatesAuditLog() {
    // When
    gdprService.gdprDeleteLead(testLead.id, TEST_USER, "Art. 17 - Test-Löschung");

    // Then
    List<GdprDeletionLog> logs = gdprService.getDeletionLogsForLead(testLead.id);
    assertFalse(logs.isEmpty(), "Mindestens ein Log sollte existieren");

    GdprDeletionLog log = logs.get(0);
    assertEquals("LEAD", log.entityType);
    assertEquals(testLead.id, log.entityId);
    assertEquals(TEST_USER, log.deletedBy);
    assertNotNull(log.deletedAt);
    assertNotNull(log.originalDataHash, "Data hash sollte gesetzt sein (für Audit)");
    assertEquals(64, log.originalDataHash.length(), "SHA-256 hash hat 64 Zeichen");
  }

  @Test
  @DisplayName("Art. 17: Blockiert Löschung bei offenen Opportunities")
  void testGdprDeleteLead_BlockedByOpportunity() {
    // Given - Create opportunity for lead
    Opportunity opportunity = new Opportunity();
    opportunity.setName(testPrefix + " Opportunity");
    opportunity.setLead(testLead);
    opportunity.setStage(OpportunityStage.QUALIFICATION);
    em.persist(opportunity);
    em.flush();

    // When & Then
    GdprDeletionBlockedException exception =
        assertThrows(
            GdprDeletionBlockedException.class,
            () -> gdprService.gdprDeleteLead(testLead.id, TEST_USER, "Test-Löschung"));

    assertTrue(exception.getMessage().contains("Opportunity"));
  }

  @Test
  @DisplayName("Art. 17: Verhindert doppelte Löschung")
  void testGdprDeleteLead_AlreadyDeleted() {
    // Given - First deletion
    gdprService.gdprDeleteLead(testLead.id, TEST_USER, "Erste Löschung");
    em.flush();

    // When & Then - Second deletion should fail
    assertThrows(
        IllegalStateException.class,
        () -> gdprService.gdprDeleteLead(testLead.id, TEST_USER, "Zweite Löschung"));
  }

  @Test
  @DisplayName("Art. 17: Wirft Exception für nicht existierenden Lead")
  void testGdprDeleteLead_LeadNotFound() {
    // Given
    Long invalidId = 999999L;

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> gdprService.gdprDeleteLead(invalidId, TEST_USER, "Test"));
  }

  // ============================================================================
  // Art. 7.3 - Einwilligungswiderruf
  // ============================================================================

  @Test
  @DisplayName("Art. 7.3: Widerruft Einwilligung und sperrt Kontakt")
  void testRevokeConsent_Success() {
    // When
    gdprService.revokeConsent(testLead.id, TEST_USER);

    // Then - Reload entity from DB
    em.flush();
    em.clear();
    testLead = Lead.findById(testLead.id);

    assertNotNull(testLead.consentRevokedAt, "consentRevokedAt sollte gesetzt sein");
    assertEquals(TEST_USER, testLead.consentRevokedBy);
    assertTrue(testLead.contactBlocked, "Kontakt sollte gesperrt sein");
  }

  @Test
  @DisplayName("Art. 7.3: Verhindert doppelten Widerruf")
  void testRevokeConsent_AlreadyRevoked() {
    // Given - First revocation
    gdprService.revokeConsent(testLead.id, TEST_USER);
    em.flush();

    // When & Then - Second revocation should fail
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> gdprService.revokeConsent(testLead.id, "other-user"));

    assertTrue(exception.getMessage().contains("bereits"));
  }

  @Test
  @DisplayName("Art. 7.3: Wirft Exception für nicht existierenden Lead")
  void testRevokeConsent_LeadNotFound() {
    // Given
    Long invalidId = 999999L;

    // When & Then
    assertThrows(
        IllegalArgumentException.class, () -> gdprService.revokeConsent(invalidId, TEST_USER));
  }

  // ============================================================================
  // Kontakt-Erlaubnis-Prüfung
  // ============================================================================

  @Test
  @DisplayName("Kontakt: Erlaubt Kontakt für normalen Lead")
  void testIsContactAllowed_NormalLead() {
    // When
    boolean allowed = gdprService.isContactAllowed(testLead.id);

    // Then
    assertTrue(allowed, "Kontakt sollte für normalen Lead erlaubt sein");
  }

  @Test
  @DisplayName("Kontakt: Verweigert Kontakt nach Einwilligungswiderruf")
  void testIsContactAllowed_ConsentRevoked() {
    // Given
    gdprService.revokeConsent(testLead.id, TEST_USER);
    em.flush();

    // When
    boolean allowed = gdprService.isContactAllowed(testLead.id);

    // Then
    assertFalse(allowed, "Kontakt sollte nach Widerruf nicht erlaubt sein");
  }

  @Test
  @DisplayName("Kontakt: Verweigert Kontakt nach DSGVO-Löschung")
  void testIsContactAllowed_GdprDeleted() {
    // Given
    gdprService.gdprDeleteLead(testLead.id, TEST_USER, "Test-Löschung für Kontaktprüfung");
    em.flush();

    // When
    boolean allowed = gdprService.isContactAllowed(testLead.id);

    // Then
    assertFalse(allowed, "Kontakt sollte nach DSGVO-Löschung nicht erlaubt sein");
  }

  @Test
  @DisplayName("Kontakt: Gibt false für nicht existierenden Lead")
  void testIsContactAllowed_LeadNotFound() {
    // Given
    Long invalidId = 999999L;

    // When
    boolean allowed = gdprService.isContactAllowed(invalidId);

    // Then
    assertFalse(allowed, "Kontakt sollte für nicht existierenden Lead nicht erlaubt sein");
  }
}
