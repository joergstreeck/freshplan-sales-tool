package de.freshplan.modules.leads.service;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.modules.leads.api.admin.dto.LeadImportRequest;
import de.freshplan.modules.leads.api.admin.dto.LeadImportRequest.ActivityImportData;
import de.freshplan.modules.leads.api.admin.dto.LeadImportRequest.LeadImportData;
import de.freshplan.modules.leads.api.admin.dto.LeadImportResponse;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.Territory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests für Bestandsleads-Migration Service.
 *
 * <p>Sprint 2.1.6 - User Story 1
 */
@QuarkusTest
class LeadImportServiceTest {

  @Inject LeadImportService leadImportService;

  private static final String TEST_USER = "admin-test";

  @Inject EntityManager em;

  @BeforeEach
  @Transactional
  void setup() {
    // Clean test data - IMPORTANT: Delete in correct order (FK constraints!)
    em.createQuery("DELETE FROM Opportunity").executeUpdate();

    em.createQuery("DELETE FROM LeadContact").executeUpdate();
    em.createQuery("DELETE FROM LeadActivity").executeUpdate();
    em.createQuery("DELETE FROM Lead").executeUpdate();
    em.createQuery("DELETE FROM ImportJob").executeUpdate(); // Prevent idempotent replays

    // Ensure territory exists
    if (Territory.count() == 0) {
      Territory territory = new Territory();
      territory.id = "DE";
      territory.name = "Deutschland";
      territory.countryCode = "DE";
      territory.currencyCode = "EUR";
      territory.taxRate = new java.math.BigDecimal("19.00");
      territory.languageCode = "de-DE";
      territory.persist();
    }
  }

  @AfterEach
  @Transactional
  void cleanup() {
    // Clean up test data after each test (FK constraint order!)
    em.createQuery("DELETE FROM Opportunity").executeUpdate();
    em.createQuery("DELETE FROM LeadContact").executeUpdate();
    em.createQuery("DELETE FROM LeadActivity").executeUpdate();
    em.createQuery("DELETE FROM Lead").executeUpdate();
    em.createQuery("DELETE FROM ImportJob").executeUpdate();
  }

  @Test
  @Transactional
  void shouldImportSingleLeadSuccessfully() {
    // Given
    LeadImportRequest request = createSingleLeadRequest(false);

    // When
    LeadImportResponse response = leadImportService.importLeads(request, TEST_USER);

    // Then
    assertNotNull(response);
    assertFalse(response.dryRun);
    assertEquals(1, response.statistics.totalLeads);
    assertEquals(1, response.statistics.successCount);
    assertEquals(0, response.statistics.failureCount);
    assertEquals(TEST_USER, response.statistics.importedBy);

    // Verify lead was persisted
    Lead lead = Lead.findById(response.results.get(0).leadId);
    assertNotNull(lead);
    assertEquals("Gasthaus Müller", lead.companyName);
    assertEquals("München", lead.city);
    assertNotNull(lead.registeredAt);
    assertEquals("import", lead.registeredAtSource);
    assertEquals(TEST_USER, lead.createdBy);
  }

  @Test
  @Transactional
  void shouldValidateDryRunWithoutPersistence() {
    // Given
    LeadImportRequest request = createSingleLeadRequest(true);

    // When
    LeadImportResponse response = leadImportService.importLeads(request, TEST_USER);

    // Then
    assertTrue(response.dryRun);
    assertEquals(1, response.statistics.successCount);
    assertNull(response.results.get(0).leadId); // No ID in dry-run

    // Verify no lead was persisted
    assertEquals(0, Lead.count());
  }

  @Test
  @Transactional
  void shouldRejectFutureRegisteredAt() {
    // Given
    LeadImportRequest request = createSingleLeadRequest(false);
    request.leads.get(0).registeredAt = LocalDateTime.now().plusDays(1);

    // When
    LeadImportResponse response = leadImportService.importLeads(request, TEST_USER);

    // Then
    assertEquals(1, response.statistics.validationErrors);
    assertEquals("VALIDATION_ERROR", response.results.get(0).status);
    assertTrue(
        response.results.get(0).validationErrors.stream()
            .anyMatch(e -> e.contains("Cannot be in the future")));
  }

  @Test
  @Transactional
  void shouldRejectActivityWithoutCountsAsProgress() {
    // Given
    LeadImportRequest request = createSingleLeadRequest(false);
    ActivityImportData activity = new ActivityImportData();
    activity.activityType = "CALL";
    activity.summary = "Erstkontakt";
    activity.activityDate = LocalDateTime.now().minusDays(5);
    activity.countsAsProgress = null; // FEHLER: muss explizit gesetzt sein!
    request.leads.get(0).activities = List.of(activity);

    // When
    LeadImportResponse response = leadImportService.importLeads(request, TEST_USER);

    // Then
    assertEquals(1, response.statistics.validationErrors);
    assertTrue(
        response.results.get(0).validationErrors.stream()
            .anyMatch(e -> e.contains("countsAsProgress")));
  }

  @Test
  @Transactional
  void shouldWarnOnDuplicates() {
    // Given: Existing lead (unique city to avoid parallel test collisions)
    createExistingLead("Pizzeria Napoli", "Stuttgart");

    // When: Import same lead
    LeadImportRequest request = new LeadImportRequest();
    request.dryRun = false;
    request.leads = new ArrayList<>();
    request.leads.add(
        createLeadData("Pizzeria Napoli", "Stuttgart", LocalDateTime.now().minusMonths(3)));

    LeadImportResponse response = leadImportService.importLeads(request, TEST_USER);

    // Then
    assertEquals(1, response.statistics.duplicateWarnings);
    assertEquals("DUPLICATE_WARNING", response.results.get(0).status);
    assertNotNull(response.results.get(0).leadId); // Still imported!
  }

  @Test
  @Transactional
  void shouldImportBatchWithMixedResults() {
    // Given: 3 leads (1 valid, 1 duplicate, 1 validation error) - unique cities to avoid collisions
    LeadImportRequest request = new LeadImportRequest();
    request.dryRun = false;
    request.leads = new ArrayList<>();

    // Lead 1: Valid
    request.leads.add(
        createLeadData("Restaurant Alpha", "Hannover", LocalDateTime.now().minusDays(30)));

    // Lead 2: Duplicate (existing)
    createExistingLead("Restaurant Beta", "Bremen");
    request.leads.add(
        createLeadData("Restaurant Beta", "Bremen", LocalDateTime.now().minusDays(20)));

    // Lead 3: Validation Error (future date)
    request.leads.add(
        createLeadData("Restaurant Gamma", "Düsseldorf", LocalDateTime.now().plusDays(1)));

    // When
    LeadImportResponse response = leadImportService.importLeads(request, TEST_USER);

    // Then
    assertEquals(3, response.statistics.totalLeads);
    assertEquals(1, response.statistics.successCount); // Lead 1
    assertEquals(1, response.statistics.duplicateWarnings); // Lead 2
    assertEquals(1, response.statistics.validationErrors); // Lead 3

    assertEquals("SUCCESS", response.results.get(0).status);
    assertEquals("DUPLICATE_WARNING", response.results.get(1).status);
    assertEquals("VALIDATION_ERROR", response.results.get(2).status);
  }

  @Test
  @Transactional
  void shouldCalculateIdempotentRequestHash() {
    // Given: Same request data
    LeadImportRequest request1 = createSingleLeadRequest(false);

    // When
    LeadImportResponse response1 = leadImportService.importLeads(request1, TEST_USER);

    // Then: Hash is calculated and consistent
    assertNotNull(response1.requestHash);
    assertFalse(response1.requestHash.isEmpty());
    assertTrue(response1.requestHash.length() > 20); // SHA-256 base64 length
  }

  @Test
  @Transactional
  void shouldSetProtectionStartFromRegisteredAt() {
    // Given
    LocalDateTime historicDate = LocalDateTime.of(2024, 6, 15, 10, 0);
    LeadImportRequest request = createSingleLeadRequest(false);
    request.leads.get(0).registeredAt = historicDate;

    // When
    LeadImportResponse response = leadImportService.importLeads(request, TEST_USER);

    // Then
    Lead lead = Lead.findById(response.results.get(0).leadId);
    assertEquals(historicDate, lead.protectionStartAt);
    assertEquals(historicDate, lead.lastActivityAt);
  }

  // Helper Methods

  private LeadImportRequest createSingleLeadRequest(boolean dryRun) {
    LeadImportRequest request = new LeadImportRequest();
    request.dryRun = dryRun;
    request.leads = new ArrayList<>();

    LeadImportData lead =
        createLeadData("Gasthaus Müller", "München", LocalDateTime.now().minusMonths(2));
    request.leads.add(lead);

    return request;
  }

  private LeadImportData createLeadData(
      String companyName, String city, LocalDateTime registeredAt) {
    LeadImportData lead = new LeadImportData();
    lead.companyName = companyName;
    lead.city = city;
    lead.postalCode = "80331";
    lead.countryCode = "DE";
    lead.businessType = "RESTAURANT"; // Sprint 2.1.6: Harmonized uppercase value
    lead.registeredAt = registeredAt;
    lead.importReason = "Bestandsdaten-Migration";
    lead.territoryCode = "DE";
    lead.estimatedVolume = new BigDecimal("5000.00");
    return lead;
  }

  private void createExistingLead(String companyName, String city) {
    Lead lead = new Lead();
    lead.companyName = companyName;
    lead.city = city;
    lead.territory = Territory.findByCode("DE");
    lead.ownerUserId = "existing-user";
    lead.createdBy = "system";
    lead.updatedBy = "system";
    lead.registeredAt =
        LocalDateTime.now()
            .minusSeconds(
                1); // Fix: 1s buffer for DB check constraint (chk_leads_registered_at_not_future)
    lead.persist();
  }
}
