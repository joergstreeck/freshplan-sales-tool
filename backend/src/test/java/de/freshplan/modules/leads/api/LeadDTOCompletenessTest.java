package de.freshplan.modules.leads.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadContact;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.Territory;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.*;

/**
 * DTO Completeness Test for LeadDTO - Sprint 2.1.6 Phase 5+ (ADR-007).
 *
 * <p><b>Purpose:</b> Ensures ALL fields in LeadContact entity are correctly mapped to LeadDTO.
 *
 * <p><b>Why?</b> Testing Guide: "DTO Completeness Tests prevent missing fields in API responses"
 *
 * <p><b>Example Bug Prevented:</b> leadScore existed in DB but was missing in LeadDTO mapping →
 * Frontend couldn't display score.
 *
 * @see LeadDTO LeadDTO.from() mapping
 * @see de.freshplan.modules.leads.domain.LeadContact LeadContact Entity
 */
@QuarkusTest
@TestHTTPEndpoint(LeadResource.class)
@Tag("integration")
class LeadDTOCompletenessTest {

  @Inject EntityManager em;

  @BeforeEach
  @Transactional
  void setup() {
    // Cleanup for test isolation
    em.createQuery("DELETE FROM LeadContact").executeUpdate();
    em.createQuery("DELETE FROM LeadActivity").executeUpdate();
    em.createQuery("DELETE FROM Lead").executeUpdate();
    em.createQuery("DELETE FROM Territory").executeUpdate();
  }

  /** Creates test territory for lead creation. */
  @Transactional
  Territory createTestTerritory() {
    Territory territory = new Territory();
    territory.id = "DE";
    territory.name = "Deutschland";
    territory.countryCode = "DE";
    territory.currencyCode = "EUR";
    territory.languageCode = "de-DE";
    territory.taxRate = new BigDecimal("19.00");
    territory.active = true;
    territory.persist();
    return territory;
  }

  /** Creates test lead with contacts for DTO completeness validation. */
  @Transactional
  Long createTestLeadWithContacts(String userId) {
    Territory territory = createTestTerritory();

    Lead lead = new Lead();
    lead.companyName = "Test Restaurant GmbH";
    lead.contactPerson = "Max Mustermann"; // Legacy field (deprecated)
    lead.email = "max@restaurant.de"; // Legacy field (deprecated)
    lead.phone = "+49 123 456789"; // Legacy field (deprecated)
    lead.city = "Berlin";
    lead.postalCode = "10115";
    lead.countryCode = "DE";
    lead.territory = territory;
    lead.status = LeadStatus.REGISTERED;
    lead.ownerUserId = userId;
    lead.createdBy = userId;
    lead.registeredAt = LocalDateTime.now();
    lead.protectionStartAt = LocalDateTime.now();
    lead.persist();

    // Create primary contact
    LeadContact primary = new LeadContact();
    primary.setLead(lead);
    primary.setFirstName("Maria");
    primary.setLastName("Schmidt");
    primary.setEmail("maria.schmidt@restaurant.de");
    primary.setPhone("+49 30 12345678");
    primary.setPosition("Geschäftsführerin");
    primary.setDecisionLevel("executive");
    primary.setPrimary(true);

    // CRM Intelligence Data
    primary.setWarmthScore(85);
    primary.setWarmthConfidence(90);
    primary.setInteractionCount(12);
    primary.setDataQualityScore(95);

    primary.setCreatedBy(userId);
    primary.persist();

    // Create secondary contact
    LeadContact secondary = new LeadContact();
    secondary.setLead(lead);
    secondary.setFirstName("Karl");
    secondary.setLastName("Meier");
    secondary.setEmail("karl.meier@restaurant.de");
    secondary.setMobile("+49 170 1234567");
    secondary.setPosition("Küchenchef");
    secondary.setDecisionLevel("operational");
    secondary.setPrimary(false);
    secondary.setCreatedBy(userId);
    secondary.persist();

    em.flush();
    return lead.id;
  }

  // ===========================
  // 1. DTO Completeness Tests (Testing Guide Section 3)
  // ===========================

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("LeadDTO should include contacts array (non-null)")
  void testLeadDtoIncludesContactsArray() {
    Long leadId = createTestLeadWithContacts("user1");

    given()
        .when()
        .get("/" + leadId)
        .then()
        .statusCode(200)
        .body("id", equalTo(leadId.intValue()))
        .body("contacts", notNullValue())
        .body("contacts", isA(java.util.List.class));
  }

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("LeadDTO.contacts should have correct count (2 contacts)")
  void testLeadDtoContactsCount() {
    Long leadId = createTestLeadWithContacts("user1");

    given()
        .when()
        .get("/" + leadId)
        .then()
        .statusCode(200)
        .body("contacts", hasSize(2))
        .body("contacts[0]", notNullValue())
        .body("contacts[1]", notNullValue());
  }

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("LeadDTO.contacts should include ALL basic fields (firstName, lastName, email, etc.)")
  void testLeadDtoContactBasicFields() {
    Long leadId = createTestLeadWithContacts("user1");

    given()
        .when()
        .get("/" + leadId)
        .then()
        .statusCode(200)
        // First contact (Maria - primary)
        .body("contacts[0].firstName", equalTo("Maria"))
        .body("contacts[0].lastName", equalTo("Schmidt"))
        .body("contacts[0].email", equalTo("maria.schmidt@restaurant.de"))
        .body("contacts[0].phone", equalTo("+49 30 12345678"))
        .body("contacts[0].position", equalTo("Geschäftsführerin"))
        .body("contacts[0].decisionLevel", equalTo("executive"))
        .body("contacts[0].primary", equalTo(true))
        // Second contact (Karl - secondary)
        .body("contacts[1].firstName", equalTo("Karl"))
        .body("contacts[1].lastName", equalTo("Meier"))
        .body("contacts[1].email", equalTo("karl.meier@restaurant.de"))
        .body("contacts[1].mobile", equalTo("+49 170 1234567"))
        .body("contacts[1].position", equalTo("Küchenchef"))
        .body("contacts[1].primary", equalTo(false));
  }

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("LeadDTO.contacts should include CRM Intelligence fields (warmthScore, dataQualityScore)")
  void testLeadDtoContactIntelligenceFields() {
    Long leadId = createTestLeadWithContacts("user1");

    given()
        .when()
        .get("/" + leadId)
        .then()
        .statusCode(200)
        // CRM Intelligence Data for first contact (Maria - primary)
        .body("contacts[0].warmthScore", equalTo(85))
        .body("contacts[0].warmthConfidence", equalTo(90))
        .body("contacts[0].interactionCount", equalTo(12))
        .body("contacts[0].dataQualityScore", equalTo(95));
  }

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("LeadDTO.contacts should include computed fields (fullName, displayName)")
  void testLeadDtoContactComputedFields() {
    Long leadId = createTestLeadWithContacts("user1");

    given()
        .when()
        .get("/" + leadId)
        .then()
        .statusCode(200)
        // Computed fields for first contact (Maria - primary)
        .body("contacts[0].fullName", equalTo("Maria Schmidt"))
        .body("contacts[0].displayName", equalTo("Maria Schmidt"))
        // Computed fields for second contact (Karl - secondary)
        .body("contacts[1].fullName", equalTo("Karl Meier"))
        .body("contacts[1].displayName", equalTo("Karl Meier"));
  }

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("LeadDTO.contacts should include audit fields (createdAt, createdBy)")
  void testLeadDtoContactAuditFields() {
    Long leadId = createTestLeadWithContacts("user1");

    given()
        .when()
        .get("/" + leadId)
        .then()
        .statusCode(200)
        // Audit fields for first contact (Maria - primary)
        .body("contacts[0].createdAt", notNullValue())
        .body("contacts[0].updatedAt", notNullValue())
        .body("contacts[0].createdBy", equalTo("user1"));
  }

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("LeadDTO.contacts should distinguish primary vs secondary contact")
  void testLeadDtoContactPrimaryFlag() {
    Long leadId = createTestLeadWithContacts("user1");

    given()
        .when()
        .get("/" + leadId)
        .then()
        .statusCode(200)
        .body("contacts[0].primary", equalTo(true))  // Maria
        .body("contacts[1].primary", equalTo(false)); // Karl
  }

  // ===========================
  // 2. Backward Compatibility Test (Legacy Fields)
  // ===========================

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("LeadDTO should still include deprecated contactPerson field (Backward Compatibility)")
  void testLeadDtoBackwardCompatibility() {
    Long leadId = createTestLeadWithContacts("user1");

    given()
        .when()
        .get("/" + leadId)
        .then()
        .statusCode(200)
        // V276 trigger should have synced primary contact to legacy fields
        .body("contactPerson", equalTo("Maria Schmidt"))
        .body("email", equalTo("maria.schmidt@restaurant.de"))
        .body("phone", equalTo("+49 30 12345678"))
        // New contacts array should ALSO be present
        .body("contacts", hasSize(2));
  }

  // ===========================
  // 3. V280 Relationship Dimension Fields
  // ===========================

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("V280: LeadDTO should include all 4 relationship fields")
  void testLeadDtoRelationshipFields() {
    Long leadId = createTestLeadWithContacts("user1");

    // Update lead with relationship data via domain model (not REST API)
    updateLeadRelationshipData(leadId);

    given()
        .when()
        .get("/" + leadId)
        .then()
        .statusCode(200)
        .body("relationshipStatus", equalTo("ADVOCATE"))
        .body("decisionMakerAccess", equalTo("DIRECT"))
        .body("competitorInUse", equalTo("Metro"))
        .body("internalChampionName", equalTo("Max Müller"));
  }

  /** Helper: Update lead with V280 relationship data. */
  @Transactional
  void updateLeadRelationshipData(Long leadId) {
    Lead lead = em.find(Lead.class, leadId);
    lead.relationshipStatus = de.freshplan.modules.leads.domain.RelationshipStatus.ADVOCATE;
    lead.decisionMakerAccess = de.freshplan.modules.leads.domain.DecisionMakerAccess.DIRECT;
    lead.competitorInUse = "Metro";
    lead.internalChampionName = "Max Müller";
    em.flush();
  }

}
