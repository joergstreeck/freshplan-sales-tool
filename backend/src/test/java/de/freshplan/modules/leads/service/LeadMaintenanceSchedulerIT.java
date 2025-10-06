package de.freshplan.modules.leads.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStage;
import de.freshplan.modules.leads.domain.LeadStatus;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration Tests für LeadMaintenanceScheduler (Sprint 2.1.6 Phase 3).
 *
 * <p>Tests für Scheduler-Schicht mit echter Datenbank (siehe ADR-005: Hybrid Test Strategy).
 *
 * <p>Strategy: @QuarkusTest, reale DB, langsamer (~5s), aber komplett integriert.
 *
 * <p><strong>Wichtig:</strong> Diese Tests validieren das Zusammenspiel von Scheduler →
 * Service → DB → Events. Nicht die Business-Logik Details (das macht LeadMaintenanceServiceTest).
 */
@QuarkusTest
@DisplayName("LeadMaintenanceScheduler Integration Tests - Sprint 2.1.6 Phase 3")
@Tag("integration")
class LeadMaintenanceSchedulerIT {

  @Inject EntityManager em;

  @Inject LeadMaintenanceScheduler scheduler;

  @Inject LeadMaintenanceService service;

  @BeforeEach
  @Transactional
  void cleanupTestData() {
    // Cleanup von Test-Leads (falls vorhanden)
    em.createQuery("DELETE FROM Lead l WHERE l.companyName LIKE 'IT-TEST-%'").executeUpdate();
  }

  // ========== Job 1: Progress Warning Check ==========

  @Test
  @DisplayName("Job 1: Should process progress warnings end-to-end")
  @Transactional
  void shouldProcessProgressWarningsEndToEnd() {
    // Arrange: Erstelle Test-Lead mit Deadline in 5 Tagen
    Lead lead = createTestLead("IT-TEST-WARNING-LEAD");
    lead.progressDeadline = LocalDateTime.now().plusDays(5);
    lead.progressWarningSentAt = null;
    lead.status = LeadStatus.ACTIVE;
    lead.persist();

    Long leadId = lead.id;

    // Act: Trigger Scheduler (ruft Service auf)
    scheduler.scheduleProgressWarningCheck();

    // Assert: Lead wurde gewarnt (progressWarningSentAt gesetzt)
    em.clear(); // Clear Entity Cache für frische DB-Abfrage
    Lead updatedLead = Lead.findById(leadId);

    assertThat(updatedLead).isNotNull();
    assertThat(updatedLead.progressWarningSentAt).isNotNull();
    assertThat(updatedLead.progressWarningSentAt)
        .isBetween(LocalDateTime.now().minusSeconds(5), LocalDateTime.now().plusSeconds(1));
  }

  @Test
  @DisplayName("Job 1: Should skip leads with no warning needed")
  @Transactional
  void shouldSkipLeadsWithNoWarningNeeded() {
    // Arrange: Lead mit Deadline in 20 Tagen (kein Warning nötig)
    Lead lead = createTestLead("IT-TEST-NO-WARNING");
    lead.progressDeadline = LocalDateTime.now().plusDays(20);
    lead.progressWarningSentAt = null;
    lead.status = LeadStatus.ACTIVE;
    lead.persist();

    Long leadId = lead.id;

    // Act
    scheduler.scheduleProgressWarningCheck();

    // Assert: Lead wurde NICHT gewarnt
    em.clear();
    Lead updatedLead = Lead.findById(leadId);

    assertThat(updatedLead).isNotNull();
    assertThat(updatedLead.progressWarningSentAt).isNull();
  }

  // ========== Job 2: Protection Expiry Check ==========

  @Test
  @DisplayName("Job 2: Should expire lead protection end-to-end")
  @Transactional
  void shouldExpireLeadProtectionEndToEnd() {
    // Arrange: Lead mit Warnung vor 11 Tagen (Nachfrist abgelaufen)
    Lead lead = createTestLead("IT-TEST-EXPIRY-LEAD");
    lead.progressWarningSentAt = LocalDateTime.now().minusDays(11);
    lead.progressDeadline = LocalDateTime.now().minusDays(4);
    lead.status = LeadStatus.ACTIVE;
    lead.ownerUserId = "partner123";
    lead.persist();

    Long leadId = lead.id;

    // Act
    scheduler.scheduleProtectionExpiryCheck();

    // Assert: Lead ist expired (Status=EXPIRED, ownerUserId=NULL)
    em.clear();
    Lead updatedLead = Lead.findById(leadId);

    assertThat(updatedLead).isNotNull();
    assertThat(updatedLead.status).isEqualTo(LeadStatus.EXPIRED);
    assertThat(updatedLead.ownerUserId).isNull();
  }

  // ========== Job 3: DSGVO Pseudonymization ==========

  @Test
  @DisplayName("Job 3: Should pseudonymize expired leads end-to-end")
  @Transactional
  void shouldPseudonymizeExpiredLeadsEndToEnd() {
    // Arrange: Expired Lead mit PII-Daten (61 Tage alt)
    Lead lead = createTestLead("IT-TEST-PSEUDONYMIZE");
    lead.email = "test-pseudonymize@hotel.de";
    lead.phone = "+49123456789";
    lead.contactPerson = "Max Mustermann";
    lead.status = LeadStatus.EXPIRED;
    lead.updatedAt = LocalDateTime.now().minusDays(61);
    lead.pseudonymizedAt = null;
    lead.persist();

    Long leadId = lead.id;
    String originalCompanyName = lead.companyName; // Firmendaten bleiben erhalten!

    // Act
    scheduler.schedulePseudonymization();

    // Assert: PII pseudonymisiert, Firmendaten bleiben
    em.clear();
    Lead updatedLead = Lead.findById(leadId);

    assertThat(updatedLead).isNotNull();

    // PII entfernt
    assertThat(updatedLead.email).isNotEqualTo("test-pseudonymize@hotel.de"); // Gehasht
    assertThat(updatedLead.email).matches("^[a-f0-9]{64}$"); // SHA-256 Hash
    assertThat(updatedLead.phone).isNull();
    assertThat(updatedLead.contactPerson).isEqualTo("ANONYMIZED");
    assertThat(updatedLead.pseudonymizedAt).isNotNull();

    // Firmendaten bleiben (Analytics)
    assertThat(updatedLead.companyName).isEqualTo(originalCompanyName);
    assertThat(updatedLead.city).isNotNull();
  }

  @Test
  @DisplayName("Job 3: Should skip leads not yet 60 days expired")
  @Transactional
  void shouldSkipLeadsNotYet60DaysExpired() {
    // Arrange: Expired Lead (nur 50 Tage alt)
    Lead lead = createTestLead("IT-TEST-SKIP-PSEUDONYMIZE");
    lead.email = "test-skip@hotel.de";
    lead.status = LeadStatus.EXPIRED;
    lead.updatedAt = LocalDateTime.now().minusDays(50);
    lead.pseudonymizedAt = null;
    lead.persist();

    Long leadId = lead.id;

    // Act
    scheduler.schedulePseudonymization();

    // Assert: Lead wurde NICHT pseudonymisiert
    em.clear();
    Lead updatedLead = Lead.findById(leadId);

    assertThat(updatedLead).isNotNull();
    assertThat(updatedLead.email).isEqualTo("test-skip@hotel.de"); // Unchanged
    assertThat(updatedLead.pseudonymizedAt).isNull();
  }

  // ========== Job 4: Import Jobs Archival (Placeholder) ==========

  @Test
  @DisplayName("Job 4: Should complete without errors (placeholder)")
  @Transactional
  void shouldCompleteImportArchivalPlaceholder() {
    // Act (placeholder implementation - returns 0)
    scheduler.scheduleImportJobsArchival();

    // Assert: Kein Fehler geworfen
    assertThat(true).isTrue(); // Smoke-Test
  }

  // ========== Helper Methods ==========

  private Lead createTestLead(String companyName) {
    // Create or get default territory
    de.freshplan.modules.leads.domain.Territory territory =
        de.freshplan.modules.leads.domain.Territory.findById(1L);
    if (territory == null) {
      territory = new de.freshplan.modules.leads.domain.Territory();
      territory.name = "IT-TEST-TERRITORY";
      territory.countryCode = "DE";
      territory.currencyCode = "EUR";
      territory.taxRate = java.math.BigDecimal.valueOf(19.0);
      territory.persist();
    }

    Lead lead = new Lead();
    lead.companyName = companyName;
    lead.city = "Berlin";
    lead.status = LeadStatus.ACTIVE;
    lead.stage = LeadStage.VORMERKUNG;
    lead.ownerUserId = "IT-TEST-USER";
    lead.territory = territory;
    lead.createdBy = "IT-TEST";
    lead.updatedBy = "IT-TEST";
    lead.createdAt = LocalDateTime.now();
    lead.updatedAt = LocalDateTime.now();
    return lead;
  }

}
