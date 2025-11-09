package de.freshplan.modules.leads.api.admin;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import de.freshplan.modules.leads.api.admin.dto.LeadImportRequest;
import de.freshplan.modules.leads.api.admin.dto.LeadImportRequest.LeadImportData;
import de.freshplan.modules.leads.domain.Territory;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration Tests für Bestandsleads-Migration API.
 *
 * <p>Sprint 2.1.6 - User Story 1
 */
@QuarkusTest
class LeadImportResourceTest {

  @Inject EntityManager em;

  @BeforeEach
  @Transactional
  void setup() {
    // IMPORTANT: Delete in correct order (FK constraints!)
    em.createQuery("DELETE FROM Opportunity").executeUpdate();

    em.createQuery("DELETE FROM LeadContact").executeUpdate();
    em.createQuery("DELETE FROM LeadActivity").executeUpdate();
    em.createQuery("DELETE FROM Lead").executeUpdate();

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
  }

  @Test
  @TestSecurity(user = "admin-user", roles = "ROLE_ADMIN")
  void shouldImportLeadWithDryRun() {
    LeadImportRequest request = createValidRequest(true);

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/admin/migration/leads/import")
        .then()
        .statusCode(200)
        .body("dryRun", is(true))
        .body("statistics.totalLeads", is(1))
        .body("statistics.successCount", is(1))
        .body("results[0].status", is("SUCCESS"))
        .body("results[0].leadId", nullValue()); // No ID in dry-run
  }

  @Test
  @TestSecurity(user = "admin-user", roles = "ROLE_ADMIN")
  void shouldImportLeadSuccessfully() {
    LeadImportRequest request = createValidRequest(false);

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/admin/migration/leads/import")
        .then()
        .statusCode(201)
        .body("dryRun", is(false))
        .body("statistics.successCount", is(1))
        .body("results[0].status", is("SUCCESS"))
        .body("results[0].leadId", notNullValue());
  }

  @Test
  @TestSecurity(user = "manager-user", roles = "ROLE_MANAGER")
  void shouldRejectNonAdminUser() {
    LeadImportRequest request = createValidRequest(false);

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/admin/migration/leads/import")
        .then()
        .statusCode(403); // Forbidden
  }

  @Test
  @TestSecurity(user = "admin-user", roles = "ROLE_ADMIN")
  void shouldRejectBatchOver1000() {
    LeadImportRequest request = new LeadImportRequest();
    request.dryRun = true;
    request.leads = new ArrayList<>();

    // Add 1001 leads
    for (int i = 0; i < 1001; i++) {
      LeadImportData lead = new LeadImportData();
      lead.companyName = "Company " + i;
      lead.city = "City " + i;
      lead.registeredAt = LocalDateTime.now().minusDays(30);
      lead.importReason = "Test";
      request.leads.add(lead);
    }

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/admin/migration/leads/import")
        .then()
        .statusCode(400);
  }

  @Test
  @TestSecurity(user = "admin-user", roles = "ROLE_ADMIN")
  void shouldReturnValidationErrors() {
    LeadImportRequest request = createValidRequest(false);
    // Future date (invalid)
    request.leads.get(0).registeredAt = LocalDateTime.now().plusDays(1);

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/admin/migration/leads/import")
        .then()
        .statusCode(206) // 206 Partial Content when validation errors exist
        .body("statistics.validationErrors", is(1))
        .body("results[0].status", is("VALIDATION_ERROR"))
        .body("results[0].validationErrors", hasSize(greaterThan(0)));
  }

  @Test
  @TestSecurity(user = "admin-user", roles = "ROLE_ADMIN")
  void shouldCalculateRequestHash() {
    LeadImportRequest request = createValidRequest(true);

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/admin/migration/leads/import")
        .then()
        .statusCode(200)
        .body("requestHash", notNullValue())
        .body("requestHash", not(emptyString()));
  }

  // Helper Methods

  private LeadImportRequest createValidRequest(boolean dryRun) {
    LeadImportRequest request = new LeadImportRequest();
    request.dryRun = dryRun;
    request.leads = new ArrayList<>();

    LeadImportData lead = new LeadImportData();
    lead.companyName = "Gasthaus Müller";
    lead.city = "München";
    lead.postalCode = "80331";
    lead.countryCode = "DE";
    lead.registeredAt = LocalDateTime.now().minusMonths(2);
    lead.importReason = "Bestandsdaten-Migration";
    lead.territoryCode = "DE";

    request.leads.add(lead);
    return request;
  }
}
