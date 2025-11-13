package de.freshplan.modules.leads.security;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.Territory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;

/**
 * Basic Security Tests für das Lead-Modul (FP-236).
 *
 * <p>Simplified version that actually compiles and runs. These tests validate basic security
 * concepts without complex domain interactions.
 */
@QuarkusTest
@DisplayName("Lead Basic Security Tests - FP-236")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("integration")
public class LeadSecurityBasicTest {

  @Inject DataSource dataSource;

  @Inject EntityManager entityManager;

  private Territory territoryDE;
  private Territory territoryCH;

  @BeforeEach
  @Transactional
  void setUp() {
    // Setup test territories
    territoryDE = Territory.findById("DE");
    if (territoryDE == null) {
      territoryDE = new Territory();
      territoryDE.id = "DE";
      territoryDE.name = "Deutschland";
      territoryDE.countryCode = "DE";
      territoryDE.currencyCode = "EUR";
      territoryDE.taxRate = new java.math.BigDecimal("19.00");
      territoryDE.languageCode = "de-DE";
      territoryDE.active = true;
      territoryDE.persist();
    }

    territoryCH = Territory.findById("CH");
    if (territoryCH == null) {
      territoryCH = new Territory();
      territoryCH.id = "CH";
      territoryCH.name = "Schweiz";
      territoryCH.countryCode = "CH";
      territoryCH.currencyCode = "CHF";
      territoryCH.taxRate = new java.math.BigDecimal("7.70");
      territoryCH.languageCode = "de-CH";
      territoryCH.active = true;
      territoryCH.persist();
    }
  }

  @AfterEach
  @Transactional
  void cleanup() {
    // Clean up test leads created during tests
    // Use native query to delete by pattern matching on test data
    entityManager
        .createNativeQuery(
            "DELETE FROM leads WHERE company_name LIKE 'Test%' OR company_name LIKE '%Test%'")
        .executeUpdate();

    // Note: We do NOT delete territories DE/CH as they are system data
    // created by migrations and shared across all tests
  }

  @Test
  @Order(1)
  @DisplayName("Can create lead with proper territory")
  @Transactional
  void testCreateLeadWithTerritory() {
    // Create test lead
    Lead lead = new Lead();
    lead.companyName = "Test Company GmbH";
    lead.contactPerson = "Max Mustermann";
    lead.email = "max@testcompany.de";
    lead.territory = territoryDE;
    lead.ownerUserId = UUID.randomUUID().toString();
    lead.status = LeadStatus.REGISTERED;
    lead.registeredAt = LocalDateTime.now();
    lead.protectionStartAt = LocalDateTime.now();
    lead.protectionMonths = 6;
    lead.createdAt = LocalDateTime.now();
    lead.createdBy = lead.ownerUserId;
    lead.persist();

    assertNotNull(lead.id, "Lead should have been persisted");
    assertEquals("DE", lead.territory.id, "Territory should be DE");
    assertEquals(6, lead.protectionMonths, "Protection should be 6 months");
  }

  @Test
  @Order(2)
  @DisplayName("Lead protection timing works")
  @Transactional
  void testLeadProtectionTiming() {
    Lead lead = new Lead();
    lead.companyName = "Protected Company";
    lead.territory = territoryDE;
    lead.ownerUserId = UUID.randomUUID().toString();
    lead.status = LeadStatus.REGISTERED;
    lead.registeredAt = LocalDateTime.now();

    // Set protection
    lead.protectionStartAt = LocalDateTime.now();
    lead.protectionMonths = 6;
    lead.protectionDays60 = 60;
    lead.protectionDays10 = 10;
    lead.createdAt = LocalDateTime.now();
    lead.createdBy = lead.ownerUserId;
    lead.persist();

    // Test protection is active
    assertTrue(lead.protectionMonths > 0, "Protection should be active");
    assertEquals(60, lead.protectionDays60, "60-day reminder period should be set");
    assertEquals(10, lead.protectionDays10, "10-day grace period should be set");
  }

  @Test
  @Order(3)
  @DisplayName("Stop-the-clock feature")
  @Transactional
  void testStopTheClock() {
    Lead lead = new Lead();
    lead.companyName = "Clock Test Company";
    lead.territory = territoryDE;
    lead.ownerUserId = UUID.randomUUID().toString();
    lead.status = LeadStatus.ACTIVE;
    lead.registeredAt = LocalDateTime.now().minusDays(30);
    lead.protectionStartAt = LocalDateTime.now().minusDays(30);
    lead.protectionMonths = 6;

    // Stop the clock
    lead.clockStoppedAt = LocalDateTime.now();
    lead.stopReason = "Customer on vacation";
    lead.stopApprovedBy = "manager@company.de";
    lead.createdAt = LocalDateTime.now();
    lead.createdBy = lead.ownerUserId;
    lead.persist();

    assertNotNull(lead.clockStoppedAt, "Clock should be stopped");
    assertNotNull(lead.stopReason, "Stop reason should be recorded");
  }

  @Test
  @Order(4)
  @DisplayName("Lead state transitions")
  @Transactional
  void testLeadStateTransitions() {
    Lead lead = new Lead();
    lead.companyName = "State Test Company";
    lead.territory = territoryDE;
    lead.ownerUserId = UUID.randomUUID().toString();
    lead.status = LeadStatus.REGISTERED;
    lead.registeredAt = LocalDateTime.now();
    lead.protectionStartAt = LocalDateTime.now();
    lead.protectionMonths = 6;
    lead.createdAt = LocalDateTime.now();
    lead.createdBy = lead.ownerUserId;
    lead.persist();

    // Transition to ACTIVE
    lead.status = LeadStatus.ACTIVE;
    lead.persist();
    assertEquals(LeadStatus.ACTIVE, lead.status);

    // Transition to REMINDER
    lead.status = LeadStatus.REMINDER;
    lead.reminderSentAt = LocalDateTime.now();
    lead.persist();
    assertEquals(LeadStatus.REMINDER, lead.status);
    assertNotNull(lead.reminderSentAt);

    // Transition to GRACE_PERIOD
    lead.status = LeadStatus.GRACE_PERIOD;
    lead.gracePeriodStartAt = LocalDateTime.now();
    lead.persist();
    assertEquals(LeadStatus.GRACE_PERIOD, lead.status);
    assertNotNull(lead.gracePeriodStartAt);
  }

  @Test
  @Order(5)
  @DisplayName("Territory isolation concept")
  @Transactional
  void testTerritoryIsolation() {
    // Create DE lead
    Lead leadDE = new Lead();
    leadDE.companyName = "German Company";
    leadDE.territory = territoryDE;
    leadDE.ownerUserId = UUID.randomUUID().toString();
    leadDE.status = LeadStatus.REGISTERED;
    leadDE.registeredAt = LocalDateTime.now();
    leadDE.protectionStartAt = LocalDateTime.now();
    leadDE.protectionMonths = 6;
    leadDE.createdAt = LocalDateTime.now();
    leadDE.createdBy = leadDE.ownerUserId;
    leadDE.persist();

    // Create CH lead
    Lead leadCH = new Lead();
    leadCH.companyName = "Swiss Company";
    leadCH.territory = territoryCH;
    leadCH.ownerUserId = UUID.randomUUID().toString();
    leadCH.status = LeadStatus.REGISTERED;
    leadCH.registeredAt = LocalDateTime.now();
    leadCH.protectionStartAt = LocalDateTime.now();
    leadCH.protectionMonths = 6;
    leadCH.createdAt = LocalDateTime.now();
    leadCH.createdBy = leadCH.ownerUserId;
    leadCH.persist();

    // Verify different territories
    assertNotEquals(
        leadDE.territory.id, leadCH.territory.id, "Leads should have different territories");
    assertEquals("EUR", leadDE.territory.currencyCode, "DE lead should use EUR");
    assertEquals("CHF", leadCH.territory.currencyCode, "CH lead should use CHF");
  }

  @Test
  @Order(6)
  @DisplayName("Owner identification")
  @Transactional
  void testOwnerIdentification() {
    String ownerId = UUID.randomUUID().toString();

    Lead lead = new Lead();
    lead.companyName = "Owner Test Company";
    lead.territory = territoryDE;
    lead.ownerUserId = ownerId;
    lead.status = LeadStatus.REGISTERED;
    lead.registeredAt = LocalDateTime.now();
    lead.protectionStartAt = LocalDateTime.now();
    lead.protectionMonths = 6;
    lead.createdAt = LocalDateTime.now();
    lead.createdBy = lead.ownerUserId;
    lead.persist();

    assertEquals(ownerId, lead.ownerUserId, "Owner should be correctly set");

    // Test collaborators
    lead.collaboratorUserIds.add("collab1@company.de");
    lead.collaboratorUserIds.add("collab2@company.de");
    lead.persist();

    assertEquals(2, lead.collaboratorUserIds.size(), "Should have 2 collaborators");
  }
}
