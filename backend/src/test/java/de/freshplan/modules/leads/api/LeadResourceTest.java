package de.freshplan.modules.leads.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.Territory;
import de.freshplan.modules.leads.domain.UserLeadSettings;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.*;

/**
 * Integration tests for Lead REST API. Tests user-based protection system and lead lifecycle
 * management.
 */
@QuarkusTest
@TestHTTPEndpoint(LeadResource.class)
class LeadResourceTest {

  @Inject EntityManager em;

  @BeforeEach
  @Transactional
  void setup() {
    // Clean up test data
    em.createQuery("DELETE FROM LeadActivity").executeUpdate();
    em.createQuery("DELETE FROM Lead").executeUpdate();
    em.createQuery("DELETE FROM UserLeadSettings").executeUpdate();

    // Ensure test territory exists
    ensureTestTerritoryExists();
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
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("Should create new lead with user as owner")
  void testCreateLead() {
    Map<String, Object> leadRequest = new HashMap<>();
    leadRequest.put("companyName", "Test Restaurant GmbH");
    leadRequest.put("contactPerson", "Max Mustermann");
    leadRequest.put("email", "max@restaurant.de");
    leadRequest.put("phone", "+49 123 456789");
    leadRequest.put("street", "Teststraße 1");
    leadRequest.put("postalCode", "10115");
    leadRequest.put("city", "Berlin");
    leadRequest.put("countryCode", "DE");
    leadRequest.put("businessType", "Restaurant");
    leadRequest.put("kitchenSize", "medium");
    leadRequest.put("employeeCount", 15);
    leadRequest.put("estimatedVolume", 50000);

    Long leadId =
        given()
            .contentType(ContentType.JSON)
            .body(leadRequest)
            .when()
            .post()
            .then()
            .statusCode(201)
            .header("Location", notNullValue())
            .body("id", notNullValue())
            .body("companyName", is("Test Restaurant GmbH"))
            .body("ownerUserId", is("user1"))
            .body("status", is("REGISTERED"))
            .body("territory.countryCode", is("DE"))
            .extract()
            .path("id");

    assertNotNull(leadId);
  }

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("Should list leads for owner")
  void testListLeadsAsOwner() {
    // Create test lead first
    createTestLead("user1");

    given()
        .when()
        .get()
        .then()
        .statusCode(200)
        .body("data", hasSize(1))
        .body("data[0].ownerUserId", is("user1"))
        .body("total", is(1));
  }

  @Test
  @TestSecurity(user = "user2", roles = {"USER"})
  @DisplayName("Should not list leads of other users")
  void testListLeadsAsNonOwner() {
    // Create lead owned by user1
    createTestLead("user1");

    given()
        .when()
        .get()
        .then()
        .statusCode(200)
        .body("data", hasSize(0))
        .body("total", is(0));
  }

  @Test
  @TestSecurity(user = "admin", roles = {"ADMIN"})
  @DisplayName("Admin should see all leads")
  void testListLeadsAsAdmin() {
    // Create leads for different users
    createTestLead("user1");
    createTestLead("user2");

    given()
        .when()
        .get()
        .then()
        .statusCode(200)
        .body("data", hasSize(2))
        .body("total", is(2));
  }

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("Should update lead status with state machine validation")
  void testUpdateLeadStatus() {
    Long leadId = createTestLead("user1");

    Map<String, Object> updateRequest = new HashMap<>();
    updateRequest.put("status", "ACTIVE");

    given()
        .contentType(ContentType.JSON)
        .body(updateRequest)
        .when()
        .patch("/{id}", leadId)
        .then()
        .statusCode(200)
        .body("status", is("ACTIVE"))
        .body("lastActivityAt", notNullValue());
  }

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("Should implement stop-the-clock feature")
  void testStopTheClockFeature() {
    Long leadId = createTestLead("user1");

    // First enable stop clock permission for user
    setupUserSettings("user1", true);

    Map<String, Object> stopClockRequest = new HashMap<>();
    stopClockRequest.put("stopClock", true);
    stopClockRequest.put("stopReason", "Customer requested pause during vacation");

    given()
        .contentType(ContentType.JSON)
        .body(stopClockRequest)
        .when()
        .patch("/{id}", leadId)
        .then()
        .statusCode(200)
        .body("clockStoppedAt", notNullValue())
        .body("stopReason", is("Customer requested pause during vacation"))
        .body("stopApprovedBy", is("user1"));
  }

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("Should add activity and update lastActivityAt")
  void testAddActivity() {
    Long leadId = createTestLead("user1");

    Map<String, Object> activityRequest = new HashMap<>();
    activityRequest.put("activityType", "CALL");
    activityRequest.put("description", "Initial contact call with customer");

    given()
        .contentType(ContentType.JSON)
        .body(activityRequest)
        .when()
        .post("/{id}/activities", leadId)
        .then()
        .statusCode(200)
        .body("activityType", is("CALL"))
        .body("description", is("Initial contact call with customer"))
        .body("userId", is("user1"));

    // Verify lastActivityAt was updated
    given()
        .when()
        .get("/{id}", leadId)
        .then()
        .statusCode(200)
        .body("lastActivityAt", notNullValue());
  }

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("Should manage collaborators")
  void testManageCollaborators() {
    Long leadId = createTestLead("user1");

    Map<String, Object> collaboratorRequest = new HashMap<>();
    collaboratorRequest.put("addCollaborators", new String[] {"user2", "user3"});

    given()
        .contentType(ContentType.JSON)
        .body(collaboratorRequest)
        .when()
        .patch("/{id}", leadId)
        .then()
        .statusCode(200)
        .body("collaboratorUserIds", hasItems("user2", "user3"));
  }

  @Test
  @TestSecurity(user = "user2", roles = {"USER"})
  @DisplayName("Collaborator should access lead")
  void testCollaboratorAccess() {
    Long leadId = createTestLead("user1");

    // Add user2 as collaborator
    addCollaborator(leadId, "user2");

    // Now user2 should be able to access the lead
    given()
        .when()
        .get("/{id}", leadId)
        .then()
        .statusCode(200)
        .body("id", is(leadId.intValue()));
  }

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("Should filter leads by status")
  void testFilterByStatus() {
    // Create leads with different statuses
    Long lead1 = createTestLead("user1");
    Long lead2 = createTestLead("user1");
    updateLeadStatus(lead2, LeadStatus.ACTIVE);

    given()
        .queryParam("status", "ACTIVE")
        .when()
        .get()
        .then()
        .statusCode(200)
        .body("data", hasSize(1))
        .body("data[0].status", is("ACTIVE"));
  }

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("Should search leads by company name")
  void testSearchLeads() {
    createTestLeadWithName("user1", "Restaurant Berlin");
    createTestLeadWithName("user1", "Hotel München");

    given()
        .queryParam("search", "berlin")
        .when()
        .get()
        .then()
        .statusCode(200)
        .body("data", hasSize(1))
        .body("data[0].companyName", containsString("Berlin"));
  }

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("Should handle pagination")
  void testPagination() {
    // Create multiple leads
    for (int i = 0; i < 25; i++) {
      createTestLeadWithName("user1", "Company " + i);
    }

    given()
        .queryParam("page", 0)
        .queryParam("size", 10)
        .when()
        .get()
        .then()
        .statusCode(200)
        .body("data", hasSize(10))
        .body("page", is(0))
        .body("size", is(10))
        .body("total", is(25))
        .body("totalPages", is(3));
  }

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("Should soft delete lead")
  void testDeleteLead() {
    Long leadId = createTestLead("user1");

    given().when().delete("/{id}", leadId).then().statusCode(204);

    // Verify lead is soft deleted (status = DELETED)
    Lead deletedLead = Lead.findById(leadId);
    assertEquals(LeadStatus.DELETED, deletedLead.status);
  }

  // Helper methods
  @Transactional
  Long createTestLead(String ownerUserId) {
    Lead lead = new Lead();
    lead.companyName = "Test Company";
    lead.ownerUserId = ownerUserId;
    lead.status = LeadStatus.REGISTERED;
    lead.territory = Territory.find("countryCode", "DE").firstResult();
    lead.countryCode = "DE";
    lead.createdBy = ownerUserId;
    lead.persist();
    return lead.id;
  }

  @Transactional
  Long createTestLeadWithName(String ownerUserId, String companyName) {
    Lead lead = new Lead();
    lead.companyName = companyName;
    lead.ownerUserId = ownerUserId;
    lead.status = LeadStatus.REGISTERED;
    lead.territory = Territory.find("countryCode", "DE").firstResult();
    lead.countryCode = "DE";
    lead.createdBy = ownerUserId;
    lead.persist();
    return lead.id;
  }

  @Transactional
  void updateLeadStatus(Long leadId, LeadStatus status) {
    Lead lead = Lead.findById(leadId);
    lead.status = status;
    lead.persist();
  }

  @Transactional
  void addCollaborator(Long leadId, String userId) {
    Lead lead = Lead.findById(leadId);
    lead.addCollaborator(userId);
    lead.persist();
  }

  @Transactional
  void setupUserSettings(String userId, boolean canStopClock) {
    UserLeadSettings settings = UserLeadSettings.find("userId", userId).firstResult();
    if (settings == null) {
      settings = new UserLeadSettings();
      settings.userId = userId;
    }
    settings.canStopClock = canStopClock;
    settings.persist();
  }
}