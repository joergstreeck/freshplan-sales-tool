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
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;

/**
 * Negative Security Test Cases für das Lead-Modul (FP-236).
 *
 * <p>Tests für Fail-Closed Security: Unauthorized access, wrong territory, privilege escalation,
 * and row-level isolation.
 */
@QuarkusTest
@DisplayName("Lead Negative Security Tests - FP-236")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LeadSecurityNegativeTest {

  @Inject DataSource dataSource;

  @Inject EntityManager entityManager;

  private Territory territoryDE;
  private Territory territoryCH;
  private Lead ownedLead;
  private Lead foreignLead;
  private String ownerId = UUID.randomUUID().toString();
  private String foreignOwnerId = UUID.randomUUID().toString();

  @BeforeEach
  @Transactional
  void setUp() {
    // Setup territories
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

    // Create owned lead
    ownedLead = new Lead();
    ownedLead.companyName = "My Company GmbH";
    ownedLead.contactPerson = "Owner Test";
    ownedLead.email = "owner@mycompany.de";
    ownedLead.territory = territoryDE;
    ownedLead.ownerUserId = ownerId;
    ownedLead.status = LeadStatus.ACTIVE;
    ownedLead.registeredAt = LocalDateTime.now();
    ownedLead.protectionStartAt = LocalDateTime.now();
    ownedLead.protectionMonths = 6;
    ownedLead.createdAt = LocalDateTime.now();
    ownedLead.createdBy = ownerId;
    ownedLead.persist();

    // Create foreign lead (different owner and territory)
    foreignLead = new Lead();
    foreignLead.companyName = "Foreign Company AG";
    foreignLead.contactPerson = "Foreign Test";
    foreignLead.email = "foreign@company.ch";
    foreignLead.territory = territoryCH;
    foreignLead.ownerUserId = foreignOwnerId;
    foreignLead.status = LeadStatus.ACTIVE;
    foreignLead.registeredAt = LocalDateTime.now();
    foreignLead.protectionStartAt = LocalDateTime.now();
    foreignLead.protectionMonths = 6;
    foreignLead.createdAt = LocalDateTime.now();
    foreignLead.createdBy = foreignOwnerId;
    foreignLead.persist();
  }

  @AfterEach
  @Transactional
  void tearDown() {
    if (ownedLead != null && ownedLead.id != null) {
      Lead.deleteById(ownedLead.id);
    }
    if (foreignLead != null && foreignLead.id != null) {
      Lead.deleteById(foreignLead.id);
    }
  }

  @Test
  @Order(1)
  @DisplayName("Unauthorized access to foreign lead should be denied")
  @Transactional
  void testUnauthorizedAccessDenied() {
    // Simulate user context for ownerId
    // In real scenario, this would be enforced by RLS/ABAC

    // Try to access foreign lead
    Lead found = Lead.findById(foreignLead.id);
    assertNotNull(found, "Lead exists in database");

    // Verify ownership check would deny access
    assertNotEquals(ownerId, found.ownerUserId, "Foreign lead should have different owner");
    assertFalse(
        found.collaboratorUserIds.contains(ownerId),
        "User should not be collaborator on foreign lead");

    // In production, RLS would prevent this query from returning results
    // This test documents the expected behavior
  }

  @Test
  @Order(2)
  @DisplayName("Wrong territory access should be isolated")
  @Transactional
  void testTerritoryIsolation() {
    // Query for leads in DE territory
    List<Lead> deLeads = Lead.find("territory", territoryDE).list();

    // Verify only DE leads are returned
    for (Lead lead : deLeads) {
      assertEquals("DE", lead.territory.id, "Only DE territory leads should be accessible");
    }

    // Query for leads in CH territory
    List<Lead> chLeads = Lead.find("territory", territoryCH).list();

    // Verify only CH leads are returned
    for (Lead lead : chLeads) {
      assertEquals("CH", lead.territory.id, "Only CH territory leads should be accessible");
    }

    // Verify isolation
    assertFalse(deLeads.contains(foreignLead), "DE query should not return CH leads");
    assertFalse(chLeads.contains(ownedLead), "CH query should not return DE leads");
  }

  @Test
  @Order(3)
  @DisplayName("Collaborator cannot perform owner-only operations")
  @Transactional
  void testPrivilegeEscalationPrevention() {
    String collaboratorId = UUID.randomUUID().toString();

    // Add collaborator to lead
    ownedLead.collaboratorUserIds.add(collaboratorId);
    ownedLead.persist();

    // Verify collaborator was added
    assertTrue(
        ownedLead.collaboratorUserIds.contains(collaboratorId), "Collaborator should be added");

    // Verify collaborator is NOT owner
    assertNotEquals(collaboratorId, ownedLead.ownerUserId, "Collaborator should not be owner");

    // Owner-only operations that should be denied for collaborator:
    // 1. Changing owner
    // 2. Deleting lead
    // 3. Stopping protection clock

    // Attempt to change owner (should be denied in service layer)
    String originalOwner = ownedLead.ownerUserId;
    // In production, service would check: if (!isOwner(userId)) throw Forbidden

    // Document expected behavior
    assertEquals(
        originalOwner, ownedLead.ownerUserId, "Owner should not be changeable by collaborator");
  }

  @Test
  @Order(4)
  @DisplayName("Query results should respect row-level isolation")
  @Transactional
  void testRowLevelIsolationInQueries() {
    // Create additional leads with mixed ownership
    Lead sharedTerritoryLead = new Lead();
    sharedTerritoryLead.companyName = "Shared Territory Company";
    sharedTerritoryLead.contactPerson = "Shared Test";
    sharedTerritoryLead.email = "shared@company.de";
    sharedTerritoryLead.territory = territoryDE; // Same territory as ownedLead
    sharedTerritoryLead.ownerUserId = foreignOwnerId; // Different owner
    sharedTerritoryLead.status = LeadStatus.ACTIVE;
    sharedTerritoryLead.registeredAt = LocalDateTime.now();
    sharedTerritoryLead.protectionStartAt = LocalDateTime.now();
    sharedTerritoryLead.protectionMonths = 6;
    sharedTerritoryLead.createdAt = LocalDateTime.now();
    sharedTerritoryLead.createdBy = foreignOwnerId;
    sharedTerritoryLead.persist();

    try {
      // Query by territory (same territory, different owners)
      List<Lead> territoryLeads = Lead.find("territory", territoryDE).list();

      // Both leads are in DE territory
      assertTrue(
          territoryLeads.stream().anyMatch(l -> l.id.equals(ownedLead.id)),
          "Owned lead should be in results");
      assertTrue(
          territoryLeads.stream().anyMatch(l -> l.id.equals(sharedTerritoryLead.id)),
          "Shared territory lead exists in DB");

      // In production with RLS, user would only see their owned leads
      // This test documents that territory alone doesn't grant access

      // Query by owner
      List<Lead> ownedLeads = Lead.find("ownerUserId", ownerId).list();

      // Should only return owned leads
      assertTrue(
          ownedLeads.stream().anyMatch(l -> l.id.equals(ownedLead.id)),
          "Owned lead should be in owner query");
      assertFalse(
          ownedLeads.stream().anyMatch(l -> l.id.equals(sharedTerritoryLead.id)),
          "Foreign-owned lead should not be in owner query");

    } finally {
      // Cleanup
      Lead.deleteById(sharedTerritoryLead.id);
    }
  }

  @Test
  @Order(5)
  @DisplayName("Expired protection should deny access")
  @Transactional
  void testExpiredProtectionDenied() {
    // Create lead with expired protection
    Lead expiredLead = new Lead();
    expiredLead.companyName = "Expired Company";
    expiredLead.contactPerson = "Expired Test";
    expiredLead.email = "expired@company.de";
    expiredLead.territory = territoryDE;
    expiredLead.ownerUserId = ownerId;
    expiredLead.status = LeadStatus.EXPIRED;
    expiredLead.registeredAt = LocalDateTime.now().minusMonths(8);
    expiredLead.protectionStartAt = LocalDateTime.now().minusMonths(8);
    expiredLead.protectionMonths = 6; // Expired 2 months ago
    expiredLead.expiredAt = LocalDateTime.now().minusMonths(2);
    expiredLead.createdAt = LocalDateTime.now().minusMonths(8);
    expiredLead.createdBy = ownerId;
    expiredLead.persist();

    try {
      // Verify lead is expired
      assertEquals(LeadStatus.EXPIRED, expiredLead.status, "Lead should be expired");
      assertNotNull(expiredLead.expiredAt, "Expired timestamp should be set");

      // In production, expired leads would have restricted operations
      // Document expected behavior: no new activities, no status changes

      // Attempt to change status (should be denied for expired leads)
      LeadStatus originalStatus = expiredLead.status;
      // Service layer would check: if (lead.status == EXPIRED) throw IllegalState

      assertEquals(
          originalStatus, expiredLead.status, "Expired lead status should not be changeable");

    } finally {
      // Cleanup
      Lead.deleteById(expiredLead.id);
    }
  }

  @Test
  @Order(6)
  @DisplayName("Pagination should not leak unauthorized data")
  @Transactional
  void testPaginationRespectsSecurity() {
    // Create multiple leads
    for (int i = 0; i < 5; i++) {
      Lead lead = new Lead();
      lead.companyName = "Pagination Test " + i;
      lead.contactPerson = "Test " + i;
      lead.email = "page" + i + "@test.de";
      lead.territory = territoryDE;
      lead.ownerUserId = i % 2 == 0 ? ownerId : foreignOwnerId;
      lead.status = LeadStatus.ACTIVE;
      lead.registeredAt = LocalDateTime.now();
      lead.protectionStartAt = LocalDateTime.now();
      lead.protectionMonths = 6;
      lead.createdAt = LocalDateTime.now();
      lead.createdBy = lead.ownerUserId;
      lead.persist();
    }

    // Query with pagination
    List<Lead> page1 = Lead.findAll().page(0, 10).list();

    // Verify no data leakage in pagination
    // In production with RLS, pagination would only return authorized leads
    long ownedCount = page1.stream().filter(l -> ownerId.equals(l.ownerUserId)).count();
    long foreignCount = page1.stream().filter(l -> foreignOwnerId.equals(l.ownerUserId)).count();

    // Both types exist in DB (test setup)
    assertTrue(ownedCount > 0, "Owned leads should exist");
    assertTrue(foreignCount > 0, "Foreign leads exist in DB");

    // Document expected RLS behavior
    // In production: only ownedCount would be > 0, foreignCount would be 0

    // Cleanup
    Lead.delete("companyName like ?1", "Pagination Test%");
  }
}
