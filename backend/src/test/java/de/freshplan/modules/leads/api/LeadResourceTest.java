package de.freshplan.modules.leads.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.shared.BusinessType;
import de.freshplan.domain.shared.KitchenSize;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.Territory;
import de.freshplan.modules.leads.domain.UserLeadSettings;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;

/**
 * Integration tests for Lead REST API. Tests user-based protection system and lead lifecycle
 * management.
 *
 * <p>Phase 5C Fix: Removed class-level @TestTransaction - REST tests need data visible to HTTP
 * endpoints. Cleanup in @BeforeEach ensures test isolation.
 */
@QuarkusTest
@TestHTTPEndpoint(LeadResource.class)
@Tag("e2e")
class LeadResourceTest {

  @Inject EntityManager em;

  @BeforeEach
  @Transactional
  void setup() {
    // Phase 5C Fix: Re-added cleanup for REST tests (no class-level @TestTransaction)
    // IMPORTANT: Delete LeadContact BEFORE Lead (Hibernate bulk delete doesn't trigger CASCADE)
    em.createQuery("DELETE FROM LeadContact").executeUpdate();
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
  @TestSecurity(
      user = "user1",
      roles = {"USER"})
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
    leadRequest.put("businessType", "RESTAURANT");
    leadRequest.put("kitchenSize", "medium");
    leadRequest.put("employeeCount", 15);
    leadRequest.put("estimatedVolume", 50000);

    Integer leadIdInt =
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

    Long leadId = leadIdInt.longValue();

    assertNotNull(leadId);
  }

  @Test
  @TestSecurity(
      user = "user1",
      roles = {"USER"})
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
  @TestSecurity(
      user = "user2",
      roles = {"USER"})
  @DisplayName("Should not list leads of other users")
  void testListLeadsAsNonOwner() {
    // Create lead owned by user1
    createTestLead("user1");

    given().when().get().then().statusCode(200).body("data", hasSize(0)).body("total", is(0));
  }

  @Test
  @TestSecurity(
      user = "admin",
      roles = {"ADMIN"})
  @DisplayName("Admin should see all leads")
  void testListLeadsAsAdmin() {
    // Create leads for different users
    createTestLead("user1");
    createTestLead("user2");

    given().when().get().then().statusCode(200).body("data", hasSize(2)).body("total", is(2));
  }

  @Test
  @TestSecurity(
      user = "user1",
      roles = {"USER"})
  @DisplayName("Should update lead status with state machine validation")
  void testUpdateLeadStatus() {
    Long leadId = createTestLead("user1");

    // First fetch the lead to get the ETag (now returns strong ETag without W/ prefix)
    String etag =
        given().when().get("/{id}", leadId).then().statusCode(200).extract().header("ETag");

    assertNotNull("ETag should not be null", etag);
    // Strong ETags are quoted but don't have W/ prefix
    assertTrue(!etag.startsWith("W/"), "ETag should be strong (no W/ prefix)");
    System.out.println("Got strong ETag: " + etag);

    Map<String, Object> updateRequest = new HashMap<>();
    updateRequest.put("status", "ACTIVE");

    given()
        .contentType(ContentType.JSON)
        .header("If-Match", etag)
        .body(updateRequest)
        .when()
        .patch("/{id}", leadId)
        .then()
        .statusCode(200)
        .body("status", is("ACTIVE"))
        .body("lastActivityAt", notNullValue());
  }

  @Test
  @TestSecurity(
      user = "user1",
      roles = {"USER"})
  @DisplayName("Should implement stop-the-clock feature")
  void testStopTheClockFeature() {
    Long leadId = createTestLead("user1");

    // First enable stop clock permission for user
    setupUserSettings("user1", true);

    // Fetch the lead to get the strong ETag
    String etag =
        given().when().get("/{id}", leadId).then().statusCode(200).extract().header("ETag");

    assertNotNull("ETag should not be null", etag);

    Map<String, Object> stopClockRequest = new HashMap<>();
    stopClockRequest.put("stopClock", true);
    stopClockRequest.put("stopReason", "Customer requested pause during vacation");

    given()
        .contentType(ContentType.JSON)
        .header("If-Match", etag)
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
  @TestSecurity(
      user = "user1",
      roles = {"USER"})
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
        .body("userId", is("user1"))
        .body("leadId", is(leadId.intValue())); // DTO returns leadId instead of full lead object

    // Verify lastActivityAt was updated
    given()
        .when()
        .get("/{id}", leadId)
        .then()
        .statusCode(200)
        .body("lastActivityAt", notNullValue());
  }

  @Test
  @TestSecurity(
      user = "user1",
      roles = {"USER"})
  @DisplayName("Should manage collaborators")
  void testManageCollaborators() {
    Long leadId = createTestLead("user1");

    // Fetch the lead to get the strong ETag
    String etag =
        given().when().get("/{id}", leadId).then().statusCode(200).extract().header("ETag");

    assertNotNull("ETag should not be null", etag);

    Map<String, Object> collaboratorRequest = new HashMap<>();
    collaboratorRequest.put("addCollaborators", new String[] {"user2", "user3"});

    given()
        .contentType(ContentType.JSON)
        .header("If-Match", etag)
        .body(collaboratorRequest)
        .when()
        .patch("/{id}", leadId)
        .then()
        .statusCode(200)
        .body("collaboratorUserIds", hasItems("user2", "user3"));
  }

  @Test
  @TestSecurity(
      user = "user2",
      roles = {"USER"})
  @DisplayName("Collaborator should access lead")
  void testCollaboratorAccess() {
    Long leadId = createTestLead("user1");

    // Add user2 as collaborator
    addCollaborator(leadId, "user2");

    // Now user2 should be able to access the lead
    given().when().get("/{id}", leadId).then().statusCode(200).body("id", is(leadId.intValue()));
  }

  @Test
  @TestSecurity(
      user = "user1",
      roles = {"USER"})
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
  @TestSecurity(
      user = "user1",
      roles = {"USER"})
  @DisplayName("Should search leads by company name")
  void testSearchLeads() {
    createTestLeadWithName("user1", "Restaurant Berlin");
    createTestLeadWithName("user1", "Hotel München");

    // Search is case-insensitive
    given()
        .queryParam("search", "berlin") // Lowercase for case-insensitive search
        .when()
        .get()
        .then()
        .log().all() // Debug: print full response
        .statusCode(200)
        .body("data", hasSize(greaterThanOrEqualTo(1)))  // At least 1 result
        .body("data[0].companyName", containsString("Berlin"));
  }

  @Test
  @TestSecurity(
      user = "user1",
      roles = {"USER"})
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
  @ActivateRequestContext
  @TestSecurity(
      user = "user1",
      roles = {"ADMIN"})
  @DisplayName("Should soft delete lead with If-Match header")
  void testDeleteLead() {
    Long leadId = createTestLead("user1");

    // Fetch the lead to get the ETag for safe deletion
    String etag =
        given().when().get("/{id}", leadId).then().statusCode(200).extract().header("ETag");

    assertNotNull("ETag should not be null", etag);

    // Delete with If-Match header
    given().header("If-Match", etag).when().delete("/{id}", leadId).then().statusCode(204);

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
    lead.registeredAt = LocalDateTime.now(); // Variante B: IMMER gesetzt
    // Don't set version manually - let JPA handle it
    lead.persist();
    lead.flush(); // Ensure version is set
    // Reload to get the version
    lead = Lead.findById(lead.id);
    System.out.println("Created lead with version: " + lead.version);
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
    lead.registeredAt = LocalDateTime.now(); // Variante B: IMMER gesetzt
    // Don't set version manually - let JPA handle it
    lead.persist();
    lead.flush(); // Ensure version is set
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

  // ==================== Sprint 2.1.6 Phase 4 Tests ====================

  @Test
  @TestSecurity(
      user = "user1",
      roles = {"ADMIN"})
  @DisplayName("Stop-Clock Resume: Should calculate cumulative pause duration")
  void testStopClockResumeCalculatesCumulativePause() throws InterruptedException {
    // Given: Create a lead via REST API to get initial version
    Long leadId = createTestLead("user1");

    // Get initial ETag from response header (don't construct manually!)
    String initialEtag =
        given().when().get("/" + leadId).then().statusCode(200).extract().header("ETag");

    // When: Stop the clock with a reason (If-Match requires ETag format)
    Map<String, Object> stopRequest = new HashMap<>();
    stopRequest.put("stopClock", true);
    stopRequest.put("stopReason", "Urlaub");

    // Extract ETag from response header (more reliable than version field)
    String etagAfterStop =
        given()
            .contentType(ContentType.JSON)
            .header("If-Match", initialEtag)
            .body(stopRequest)
            .when()
            .patch("/" + leadId)
            .then()
            .statusCode(200)
            .extract()
            .header("ETag");

    // Wait for 2 seconds to simulate pause duration
    Thread.sleep(2000);

    // Then: Resume the clock
    Map<String, Object> resumeRequest = new HashMap<>();
    resumeRequest.put("stopClock", false);

    given()
        .contentType(ContentType.JSON)
        .header("If-Match", etagAfterStop)
        .body(resumeRequest)
        .when()
        .patch("/" + leadId)
        .then()
        .statusCode(200);

    // Verify: progressPauseTotalSeconds should be >= 2 seconds (via REST API)
    given()
        .when()
        .get("/" + leadId)
        .then()
        .statusCode(200)
        .body("progressPauseTotalSeconds", greaterThanOrEqualTo(2))
        .body("clockStoppedAt", nullValue())
        .body("stopReason", nullValue());
  }

  @Test
  @TestSecurity(
      user = "user1",
      roles = {"ADMIN"})
  @DisplayName("Stop-Clock Resume: Should add pause duration to activity log")
  @ActivateRequestContext
  void testStopClockResumeLogsPauseDuration() throws InterruptedException {
    // Given: Create a lead
    Long leadId = createTestLead("user1");

    // Get initial ETag from response header (don't construct manually!)
    String initialEtag =
        given().when().get("/" + leadId).then().statusCode(200).extract().header("ETag");

    // When: Stop and resume clock (If-Match requires ETag format)
    Map<String, Object> stopRequest = new HashMap<>();
    stopRequest.put("stopClock", true);
    stopRequest.put("stopReason", "Krankheit");

    // Extract ETag from response header (more reliable than version field)
    String etagAfterStop =
        given()
            .contentType(ContentType.JSON)
            .header("If-Match", initialEtag)
            .body(stopRequest)
            .when()
            .patch("/" + leadId)
            .then()
            .statusCode(200)
            .extract()
            .header("ETag");

    Thread.sleep(1000); // 1 second pause

    Map<String, Object> resumeRequest = new HashMap<>();
    resumeRequest.put("stopClock", false);

    given()
        .contentType(ContentType.JSON)
        .header("If-Match", etagAfterStop)
        .body(resumeRequest)
        .when()
        .patch("/" + leadId)
        .then()
        .statusCode(200);

    // Then: Verify clock was resumed and pause duration logged
    Lead lead = Lead.findById(leadId);
    assertNotNull(lead);
    assertNull(lead.clockStoppedAt);
    assertNull(lead.stopReason);
    assertTrue(lead.progressPauseTotalSeconds >= 1, "Pause duration should be at least 1 second");
  }

  @Test
  @TestSecurity(
      user = "user1",
      roles = {"ADMIN"})
  @DisplayName("RBAC: ADMIN role should access updateRegisteredAt endpoint")
  @ActivateRequestContext
  void testUpdateRegisteredAtWithAdminRole() {
    // Given: Create a lead as user1
    Long leadId = createTestLead("user1");

    // When: ADMIN user updates registeredAt
    Map<String, String> backdatingRequest = new HashMap<>();
    backdatingRequest.put("registeredAt", "2025-01-01T00:00:00");
    backdatingRequest.put("reason", "Datenmigration - Test");

    given()
        .contentType(ContentType.JSON)
        .body(backdatingRequest)
        .when()
        .put("/" + leadId + "/registered-at")
        .then()
        .statusCode(200);

    // Verify lead was updated
    Lead lead = Lead.findById(leadId);
    assertNotNull(lead);
    assertTrue(lead.registeredAt.toString().startsWith("2025-01-01"));
  }

  @Test
  @TestSecurity(
      user = "user1",
      roles = {"MANAGER"})
  @DisplayName("RBAC: MANAGER role should access updateRegisteredAt endpoint")
  void testUpdateRegisteredAtWithManagerRole() {
    // Given: Create a lead as user1
    Long leadId = createTestLead("user1");

    // When: MANAGER user updates registeredAt
    Map<String, String> backdatingRequest = new HashMap<>();
    backdatingRequest.put("registeredAt", "2025-02-01T12:30:00");
    backdatingRequest.put("reason", "Korrektur nach Rücksprache");

    given()
        .contentType(ContentType.JSON)
        .body(backdatingRequest)
        .when()
        .put("/" + leadId + "/registered-at")
        .then()
        .statusCode(200);
  }

  @Test
  @TestSecurity(
      user = "user1",
      roles = {"USER"})
  @DisplayName("RBAC: USER role should be forbidden from updateRegisteredAt endpoint")
  void testUpdateRegisteredAtWithUserRoleForbidden() {
    // Given: Create a lead as user1
    Long leadId = createTestLead("user1");

    // When: USER tries to update registeredAt
    // Then: Should be forbidden (403)
    Map<String, String> backdatingRequest = new HashMap<>();
    backdatingRequest.put("registeredAt", "2025-03-01T00:00:00");
    backdatingRequest.put("reason", "Unauthorized attempt test");

    given()
        .contentType(ContentType.JSON)
        .body(backdatingRequest)
        .when()
        .put("/" + leadId + "/registered-at")
        .then()
        .statusCode(403);
  }

  @Test
  @TestSecurity(
      user = "user1",
      roles = {"ADMIN"})
  @DisplayName("RBAC: ADMIN role should access convertToCustomer endpoint")
  void testConvertToCustomerWithAdminRole() {
    // Given: Create a lead in QUALIFIED status (ready for conversion)
    Long leadId = createTestLead("user1");
    updateLeadStatus(leadId, LeadStatus.QUALIFIED);

    // When: ADMIN converts lead to customer
    // Then: Should succeed with ADMIN role
    Map<String, Object> convertRequest = new HashMap<>();
    convertRequest.put("keepLeadRecord", true);
    convertRequest.put("conversionNotes", "Test conversion");

    given()
        .contentType(ContentType.JSON)
        .body(convertRequest)
        .when()
        .post("/" + leadId + "/convert")
        .then()
        .statusCode(201) // Created
        .body("customerId", notNullValue());
  }

  @Test
  @TestSecurity(
      user = "user1",
      roles = {"MANAGER"})
  @DisplayName("RBAC: MANAGER role should access convertToCustomer endpoint")
  void testConvertToCustomerWithManagerRole() {
    // Given: Create a lead in QUALIFIED status (ready for conversion)
    Long leadId = createTestLead("user1");
    updateLeadStatus(leadId, LeadStatus.QUALIFIED);

    // When: MANAGER converts lead to customer
    // Then: Should succeed with MANAGER role
    Map<String, Object> convertRequest = new HashMap<>();
    convertRequest.put("keepLeadRecord", true);

    given()
        .contentType(ContentType.JSON)
        .body(convertRequest)
        .when()
        .post("/" + leadId + "/convert")
        .then()
        .statusCode(201) // Created
        .body("customerId", notNullValue());
  }

  @Test
  @TestSecurity(
      user = "user1",
      roles = {"USER"})
  @DisplayName("RBAC: USER role should be forbidden from convertToCustomer endpoint")
  void testConvertToCustomerWithUserRoleForbidden() {
    // Given: Create a lead in QUALIFIED status (ready for conversion)
    Long leadId = createTestLead("user1");
    updateLeadStatus(leadId, LeadStatus.QUALIFIED);

    // When: USER tries to convert lead
    // Then: Should be forbidden (403)
    Map<String, Object> convertRequest = new HashMap<>();
    convertRequest.put("keepLeadRecord", true);

    given()
        .contentType(ContentType.JSON)
        .body(convertRequest)
        .when()
        .post("/" + leadId + "/convert")
        .then()
        .statusCode(403);
  }

  // ================= DTO Completeness Tests (Sprint 2.1.6 Phase 4 - Test Gap Analysis)
  // =================

  @Test
  @TestSecurity(
      user = "user1",
      roles = {"USER"})
  @DisplayName("DTO Completeness: leadScore field is mapped correctly")
  void testLeadDtoIncludesLeadScore() {
    // Given: Create a lead with leadScore set
    Long leadId = createTestLeadWithScore("user1", 75);

    // When: GET lead via API
    // Then: leadScore should be present in response
    given().when().get("/" + leadId).then().statusCode(200).body("leadScore", equalTo(75));
  }

  @Test
  @TestSecurity(
      user = "user1",
      roles = {"USER"})
  @DisplayName("DTO Completeness: progressPauseTotalSeconds field is mapped correctly")
  void testLeadDtoIncludesProgressPauseTotalSeconds() {
    // Given: Create a lead with progressPauseTotalSeconds set
    Long leadId = createTestLeadWithPause("user1", 3600L); // 1 hour pause

    // When: GET lead via API
    // Then: progressPauseTotalSeconds should be present in response
    given()
        .when()
        .get("/" + leadId)
        .then()
        .statusCode(200)
        .body("progressPauseTotalSeconds", equalTo(3600));
  }

  @Test
  @TestSecurity(
      user = "user1",
      roles = {"USER"})
  @DisplayName("DTO Completeness: All nullable fields are mapped (not missing in JSON)")
  void testLeadDtoCompleteness() {
    // Given: Create a lead with all optional fields set
    Long leadId = createTestLeadWithAllFields("user1");

    // When: GET lead via API
    // Then: All fields should be present (not missing from JSON)
    given()
        .when()
        .get("/" + leadId)
        .then()
        .statusCode(200)
        .body("leadScore", notNullValue())
        .body("progressPauseTotalSeconds", notNullValue())
        .body("clockStoppedAt", nullValue()) // Explicitly nullable
        .body("stopReason", nullValue()) // Explicitly nullable
        .body("progressDeadline", notNullValue())
        .body("protectionUntil", notNullValue()); // Calculated field
  }

  // ================= Helper Methods for DTO Tests =================

  @Transactional
  Long createTestLeadWithScore(String ownerUserId, Integer score) {
    Territory territory = Territory.find("countryCode", "DE").firstResult();

    Lead lead = new Lead();
    lead.companyName = "Test Corp " + System.currentTimeMillis();
    lead.territory = territory;
    lead.ownerUserId = ownerUserId;
    lead.createdBy = ownerUserId;
    lead.countryCode = "DE";
    lead.registeredAt = LocalDateTime.now(); // Variante B: IMMER gesetzt
    lead.leadScore = score; // Set score explicitly
    lead.persist();
    return lead.id;
  }

  @Transactional
  Long createTestLeadWithPause(String ownerUserId, Long pauseSeconds) {
    Territory territory = Territory.find("countryCode", "DE").firstResult();

    Lead lead = new Lead();
    lead.companyName = "Test Corp " + System.currentTimeMillis();
    lead.territory = territory;
    lead.ownerUserId = ownerUserId;
    lead.createdBy = ownerUserId;
    lead.countryCode = "DE";
    lead.registeredAt = LocalDateTime.now(); // Variante B: IMMER gesetzt
    lead.progressPauseTotalSeconds = pauseSeconds; // Set pause explicitly
    lead.persist();
    return lead.id;
  }

  @Transactional
  Long createTestLeadWithAllFields(String ownerUserId) {
    Territory territory = Territory.find("countryCode", "DE").firstResult();

    Lead lead = new Lead();
    lead.companyName = "Complete Test Corp " + System.currentTimeMillis();
    lead.territory = territory;
    lead.ownerUserId = ownerUserId;
    lead.createdBy = ownerUserId;
    lead.countryCode = "DE";
    lead.registeredAt = LocalDateTime.now(); // Variante B: IMMER gesetzt

    // Set all nullable/optional fields
    lead.leadScore = 80;
    lead.progressPauseTotalSeconds = 1800L;
    lead.estimatedVolume = new BigDecimal("50000");
    lead.employeeCount = 25;
    lead.businessType = BusinessType.RESTAURANT;
    lead.kitchenSize = KitchenSize.SEHR_GROSS;
    lead.progressDeadline = java.time.LocalDateTime.now().plusDays(5);

    lead.persist();
    return lead.id;
  }

  // ===========================
  // Sprint 2.1.6 Phase 5+ Tests - Structured Contact Data (ADR-007)
  // ===========================

  @Test
  @TestSecurity(
      user = "user1",
      roles = {"USER"})
  @DisplayName(
      "Should create lead with structured contact data and return contacts array in response")
  void testCreateLeadWithStructuredContact() {
    Map<String, Object> contactData = new HashMap<>();
    contactData.put("firstName", "Maria");
    contactData.put("lastName", "Schmidt");
    contactData.put("email", "maria.schmidt@restaurant.de");
    contactData.put("phone", "+49 30 12345678");

    Map<String, Object> leadRequest = new HashMap<>();
    leadRequest.put("companyName", "Test Restaurant GmbH");
    leadRequest.put("contact", contactData); // NEW: Structured contact
    leadRequest.put("street", "Teststraße 1");
    leadRequest.put("postalCode", "10115");
    leadRequest.put("city", "Berlin");
    leadRequest.put("countryCode", "DE");
    leadRequest.put("businessType", "RESTAURANT");

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
        // Verify contacts array is populated
        .body("contacts", hasSize(1))
        .body("contacts[0].firstName", is("Maria"))
        .body("contacts[0].lastName", is("Schmidt"))
        .body("contacts[0].email", is("maria.schmidt@restaurant.de"))
        .body("contacts[0].phone", is("+49 30 12345678"))
        .body("contacts[0].primary", is(true))
        .body("contacts[0].fullName", is("Maria Schmidt"))
        .body("contacts[0].displayName", is("Maria Schmidt"))
        // Backward compatibility: legacy fields should also be synced via V276 trigger
        .body("contactPerson", is("Maria Schmidt"))
        .body("email", is("maria.schmidt@restaurant.de"))
        .body("phone", is("+49 30 12345678"));
  }

  @Test
  @TestSecurity(
      user = "user1",
      roles = {"USER"})
  @DisplayName("Should support backward compatibility with legacy flat contactPerson field")
  void testCreateLeadWithLegacyContactPerson() {
    Map<String, Object> leadRequest = new HashMap<>();
    leadRequest.put("companyName", "Test Restaurant Alt GmbH");
    leadRequest.put("contactPerson", "Max Mustermann"); // Legacy flat field
    leadRequest.put("email", "max@restaurant.de");
    leadRequest.put("phone", "+49 123 456789");
    leadRequest.put("city", "Berlin");
    leadRequest.put("countryCode", "DE");

    given()
        .contentType(ContentType.JSON)
        .body(leadRequest)
        .when()
        .post()
        .then()
        .statusCode(201)
        .body("companyName", is("Test Restaurant Alt GmbH"))
        // Verify contact was created from legacy data (split "Max Mustermann" → firstName, lastName)
        .body("contacts", hasSize(1))
        .body("contacts[0].firstName", is("Max"))
        .body("contacts[0].lastName", is("Mustermann"))
        .body("contacts[0].email", is("max@restaurant.de"))
        .body("contacts[0].primary", is(true))
        // Backward compatibility: legacy fields synced
        .body("contactPerson", is("Max Mustermann"))
        .body("email", is("max@restaurant.de"));
  }

  // ================= V280: Relationship Dimension Tests =================

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("V280: Should update relationship fields via PATCH")
  void testUpdateRelationshipFields() {
    Long leadId = createTestLead("user1");

    // Get current ETag
    String etag =
        given().when().get("/" + leadId).then().statusCode(200).extract().header("ETag");

    // PATCH request with all 4 relationship fields
    Map<String, Object> request = new HashMap<>();
    request.put("relationshipStatus", "ADVOCATE");
    request.put("decisionMakerAccess", "DIRECT");
    request.put("competitorInUse", "Metro");
    request.put("internalChampionName", "Max Müller");

    given()
        .contentType(ContentType.JSON)
        .header("If-Match", etag)
        .body(request)
        .when()
        .patch("/" + leadId)
        .then()
        .statusCode(200)
        .body("relationshipStatus", equalTo("ADVOCATE"))
        .body("decisionMakerAccess", equalTo("DIRECT"))
        .body("competitorInUse", equalTo("Metro"))
        .body("internalChampionName", equalTo("Max Müller"));
    // Note: leadScore is calculated by LeadScoringService, not automatically on PATCH
  }

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("V280: GET should return relationship fields")
  void testGetRelationshipFields() {
    Long leadId = createTestLead("user1");

    // Update via PATCH first
    String etag =
        given().when().get("/" + leadId).then().statusCode(200).extract().header("ETag");

    Map<String, Object> request = new HashMap<>();
    request.put("relationshipStatus", "TRUSTED");
    request.put("decisionMakerAccess", "IS_DECISION_MAKER");

    given()
        .contentType(ContentType.JSON)
        .header("If-Match", etag)
        .body(request)
        .when()
        .patch("/" + leadId);

    // GET should reflect updated values
    given()
        .when()
        .get("/" + leadId)
        .then()
        .statusCode(200)
        .body("relationshipStatus", equalTo("TRUSTED"))
        .body("decisionMakerAccess", equalTo("IS_DECISION_MAKER"));
  }

  // ================= Sprint 2.1.6 Phase 5+: Pain & Engagement Auto-Save Tests =================

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("Pain Score: Should update pain fields and trigger auto-scoring")
  void testUpdateLeadWithPainFields() {
    Long leadId = createTestLead("user1");

    // Get current ETag
    String etag =
        given().when().get("/" + leadId).then().statusCode(200).extract().header("ETag");

    // Update with pain fields
    Map<String, Object> request = new HashMap<>();
    request.put("painStaffShortage", true);
    request.put("painHighCosts", true);
    request.put("urgencyLevel", "HIGH");

    given()
        .contentType(ContentType.JSON)
        .header("If-Match", etag)
        .body(request)
        .when()
        .patch("/" + leadId)
        .then()
        .statusCode(200)
        .body("painStaffShortage", is(true))
        .body("painHighCosts", is(true))
        .body("urgencyLevel", equalTo("HIGH"))
        .body("painScore", notNullValue())
        .body("painScore", greaterThanOrEqualTo(0));
  }

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("Engagement Score: Should update engagement fields and trigger auto-scoring")
  void testUpdateLeadWithEngagementFields() {
    Long leadId = createTestLead("user1");

    // Get current ETag
    String etag =
        given().when().get("/" + leadId).then().statusCode(200).extract().header("ETag");

    // Update with engagement fields
    Map<String, Object> request = new HashMap<>();
    request.put("relationshipStatus", "ADVOCATE");
    request.put("decisionMakerAccess", "IS_DECISION_MAKER");
    request.put("internalChampionName", "Max Mustermann");

    given()
        .contentType(ContentType.JSON)
        .header("If-Match", etag)
        .body(request)
        .when()
        .patch("/" + leadId)
        .then()
        .statusCode(200)
        .body("relationshipStatus", equalTo("ADVOCATE"))
        .body("decisionMakerAccess", equalTo("IS_DECISION_MAKER"))
        .body("internalChampionName", equalTo("Max Mustermann"))
        .body("engagementScore", notNullValue())
        .body("engagementScore", greaterThanOrEqualTo(50)); // High engagement
  }

  @Test
  @TestSecurity(user = "user1", roles = {"USER"})
  @DisplayName("Lead Score: Should recalculate total score after pain update")
  void testScoreRecalculationOnPainUpdate() {
    Long leadId = createTestLead("user1");

    // Get current ETag
    String etag =
        given().when().get("/" + leadId).then().statusCode(200).extract().header("ETag");

    // Update pain fields with high urgency
    Map<String, Object> request = new HashMap<>();
    request.put("painStaffShortage", true);
    request.put("painHighCosts", true);
    request.put("painFoodWaste", true);
    request.put("urgencyLevel", "EMERGENCY");

    given()
        .contentType(ContentType.JSON)
        .header("If-Match", etag)
        .body(request)
        .when()
        .patch("/" + leadId)
        .then()
        .statusCode(200)
        .body("painScore", greaterThanOrEqualTo(30)) // Should be high with 3 pains + emergency
        .body("leadScore", notNullValue()); // Total score should be recalculated
  }
}
