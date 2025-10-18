package de.freshplan.modules.leads.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.entity.OpportunityType;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.Territory;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration Tests für LeadResource GET /api/leads/{id}/opportunities Endpoint Sprint 2.1.7.1 -
 * Lead → Opportunity Traceability
 *
 * @description E2E Tests für das Abrufen aller Opportunities eines Leads
 * @since 2025-10-18
 */
@QuarkusTest
@TestHTTPEndpoint(LeadResource.class)
@Tag("e2e")
@DisplayName("LeadResource GET /opportunities - Sprint 2.1.7.1 Enterprise Tests")
public class LeadResourceOpportunitiesTest {

  @Inject EntityManager em;

  @Inject OpportunityRepository opportunityRepository;

  private Lead testLead;

  private int opportunityCounter = 0;

  @BeforeEach
  @Transactional
  void setup() {
    // Reset counter for each test
    opportunityCounter = 0;

    // Cleanup only TEST data (not production seeds!)
    // Delete only entities created by test user to preserve seed data
    em.createQuery("DELETE FROM Opportunity o WHERE o.lead.createdBy = 'testuser'").executeUpdate();
    em.createQuery("DELETE FROM LeadContact lc WHERE lc.lead.createdBy = 'testuser'")
        .executeUpdate();
    em.createQuery("DELETE FROM LeadActivity la WHERE la.lead.createdBy = 'testuser'")
        .executeUpdate();
    em.createQuery("DELETE FROM Lead l WHERE l.createdBy = 'testuser'").executeUpdate();

    // Ensure test territory exists
    ensureTestTerritoryExists();

    // Create test lead
    testLead = new Lead();
    testLead.companyName = "Test Catering GmbH";
    testLead.contactPerson = "Max Mustermann";
    testLead.email = "max@testcatering.de";
    testLead.phone = "+49 30 12345678";
    testLead.street = "Teststraße 123";
    testLead.postalCode = "10115";
    testLead.city = "Berlin";
    testLead.countryCode = "DE";
    testLead.status = LeadStatus.QUALIFIED;
    testLead.estimatedVolume = new BigDecimal("75000");
    testLead.createdBy = "testuser"; // @NotNull - required for validation
    testLead.createdAt = LocalDateTime.now();
    testLead.updatedAt = LocalDateTime.now();
    testLead.registeredAt = LocalDateTime.now(); // Best practice per Variante B
    testLead.persist();

    em.flush(); // Sicherstellen dass Lead ID generiert wurde

    // Base timestamp for deterministic ordering
    LocalDateTime baseTime = LocalDateTime.now();

    // Create opportunities for this lead (with incremental timestamps)
    createOpportunity(
        "Opportunity 1 - Neugeschäft",
        OpportunityStage.QUALIFICATION,
        OpportunityType.NEUGESCHAEFT,
        new BigDecimal("25000"),
        60,
        LocalDate.now().plusDays(30),
        baseTime);

    createOpportunity(
        "Opportunity 2 - Sortimentserweiterung",
        OpportunityStage.PROPOSAL,
        OpportunityType.SORTIMENTSERWEITERUNG,
        new BigDecimal("50000"),
        80,
        LocalDate.now().plusDays(60),
        baseTime.plusSeconds(1));

    createOpportunity(
        "Opportunity 3 - Neuer Standort",
        OpportunityStage.CLOSED_WON,
        OpportunityType.NEUER_STANDORT,
        new BigDecimal("100000"),
        100,
        LocalDate.now().plusDays(90),
        baseTime.plusSeconds(2));

    em.flush();
  }

  @AfterEach
  @Transactional
  void cleanup() {
    // Cleanup test data after each test (safety measure)
    // Only delete data created by test user
    em.createQuery("DELETE FROM Opportunity o WHERE o.lead.createdBy = 'testuser'").executeUpdate();
    em.createQuery("DELETE FROM LeadContact lc WHERE lc.lead.createdBy = 'testuser'")
        .executeUpdate();
    em.createQuery("DELETE FROM LeadActivity la WHERE la.lead.createdBy = 'testuser'")
        .executeUpdate();
    em.createQuery("DELETE FROM Lead l WHERE l.createdBy = 'testuser'").executeUpdate();
  }

  @Transactional
  void createOpportunity(
      String name,
      OpportunityStage stage,
      OpportunityType type,
      BigDecimal value,
      Integer probability,
      LocalDate closeDate,
      LocalDateTime createdAt) {

    Opportunity opp = new Opportunity();
    opp.setName(name);
    opp.setStage(stage);
    opp.setOpportunityType(type);
    opp.setExpectedValue(value);
    opp.setProbability(probability);
    opp.setExpectedCloseDate(closeDate);
    opp.setLead(testLead);

    // Set createdAt via reflection for deterministic ordering in tests
    try {
      Field createdAtField = Opportunity.class.getDeclaredField("createdAt");
      createdAtField.setAccessible(true);
      createdAtField.set(opp, createdAt);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set createdAt field", e);
    }

    opportunityRepository.persist(opp);
  }

  @Transactional
  void ensureTestTerritoryExists() {
    Territory territory = Territory.find("countryCode", "DE").firstResult();
    if (territory == null) {
      territory = new Territory();
      territory.id = "DE";
      territory.name = "Deutschland";
      territory.countryCode = "DE";
      territory.currencyCode = "EUR";
      territory.languageCode = "de-DE";
      territory.taxRate = new BigDecimal("19.00");
      territory.active = true;
      territory.persist();
    }
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"USER"})
  @DisplayName("GET /leads/{id}/opportunities - Returns all opportunities for a lead")
  void testGetLeadOpportunities_Success() {
    given()
        .contentType(ContentType.JSON)
        .when()
        .get("/{id}/opportunities", testLead.id)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("$", hasSize(3))
        .body("[0].name", equalTo("Opportunity 1 - Neugeschäft"))
        .body("[0].stage", equalTo("QUALIFICATION"))
        .body("[0].opportunityType", equalTo("NEUGESCHAEFT"))
        .body("[0].expectedValue", equalTo(25000f)) // BigDecimal serialized as Float in JSON
        .body("[0].leadId", equalTo(testLead.id.intValue()))
        .body("[1].name", equalTo("Opportunity 2 - Sortimentserweiterung"))
        .body("[1].stage", equalTo("PROPOSAL"))
        .body("[1].opportunityType", equalTo("SORTIMENTSERWEITERUNG"))
        .body("[2].name", equalTo("Opportunity 3 - Neuer Standort"))
        .body("[2].stage", equalTo("CLOSED_WON"))
        .body("[2].opportunityType", equalTo("NEUER_STANDORT"));
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"USER"})
  @DisplayName("GET /leads/{id}/opportunities - Returns empty array for lead without opportunities")
  void testGetLeadOpportunities_EmptyList() {
    // Create a lead without opportunities
    Lead leadWithoutOpps = createLeadWithoutOpportunities();

    given()
        .contentType(ContentType.JSON)
        .when()
        .get("/{id}/opportunities", leadWithoutOpps.id)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("$", hasSize(0));
  }

  @Transactional
  Lead createLeadWithoutOpportunities() {
    Lead lead = new Lead();
    lead.companyName = "Lead Without Opps GmbH";
    lead.contactPerson = "Test Person";
    lead.email = "test@noop.de";
    lead.phone = "+49 30 99999999";
    lead.postalCode = "10115";
    lead.city = "Berlin";
    lead.countryCode = "DE";
    lead.status = LeadStatus.REGISTERED;
    lead.createdBy = "testuser"; // @NotNull - required for validation
    lead.createdAt = LocalDateTime.now();
    lead.updatedAt = LocalDateTime.now();
    lead.registeredAt = LocalDateTime.now(); // Best practice per Variante B
    lead.persist();
    em.flush();
    return lead;
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"USER"})
  @DisplayName("GET /leads/{id}/opportunities - Returns 404 for non-existent lead")
  void testGetLeadOpportunities_LeadNotFound() {
    Long nonExistentLeadId = 999999L;

    given()
        .contentType(ContentType.JSON)
        .when()
        .get("/{id}/opportunities", nonExistentLeadId)
        .then()
        .statusCode(404)
        .body("error", equalTo("Lead not found"));
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"MANAGER"})
  @DisplayName("GET /leads/{id}/opportunities - Works for MANAGER role")
  void testGetLeadOpportunities_ManagerRole() {
    given()
        .contentType(ContentType.JSON)
        .when()
        .get("/{id}/opportunities", testLead.id)
        .then()
        .statusCode(200)
        .body("$", hasSize(3));
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"ADMIN"})
  @DisplayName("GET /leads/{id}/opportunities - Works for ADMIN role")
  void testGetLeadOpportunities_AdminRole() {
    given()
        .contentType(ContentType.JSON)
        .when()
        .get("/{id}/opportunities", testLead.id)
        .then()
        .statusCode(200)
        .body("$", hasSize(3));
  }

  @Test
  @DisplayName("GET /leads/{id}/opportunities - Returns 401 for unauthenticated user")
  void testGetLeadOpportunities_Unauthorized() {
    given()
        .contentType(ContentType.JSON)
        .when()
        .get("/{id}/opportunities", testLead.id)
        .then()
        .statusCode(401);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"USER"})
  @DisplayName("GET /leads/{id}/opportunities - Returns correct leadCompanyName")
  void testGetLeadOpportunities_LeadCompanyName() {
    given()
        .contentType(ContentType.JSON)
        .when()
        .get("/{id}/opportunities", testLead.id)
        .then()
        .statusCode(200)
        .body("[0].leadCompanyName", equalTo("Test Catering GmbH"))
        .body("[1].leadCompanyName", equalTo("Test Catering GmbH"))
        .body("[2].leadCompanyName", equalTo("Test Catering GmbH"));
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"USER"})
  @DisplayName("GET /leads/{id}/opportunities - Returns opportunities in all stages")
  void testGetLeadOpportunities_AllStages() {
    given()
        .contentType(ContentType.JSON)
        .when()
        .get("/{id}/opportunities", testLead.id)
        .then()
        .statusCode(200)
        .body("stage", hasItems("QUALIFICATION", "PROPOSAL", "CLOSED_WON"));
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"USER"})
  @DisplayName("GET /leads/{id}/opportunities - Correct probability values")
  void testGetLeadOpportunities_ProbabilityValues() {
    given()
        .contentType(ContentType.JSON)
        .when()
        .get("/{id}/opportunities", testLead.id)
        .then()
        .statusCode(200)
        .body("[0].probability", equalTo(60))
        .body("[1].probability", equalTo(80))
        .body("[2].probability", equalTo(100));
  }
}
