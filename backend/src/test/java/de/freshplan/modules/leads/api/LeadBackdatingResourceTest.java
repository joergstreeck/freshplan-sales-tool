package de.freshplan.modules.leads.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import de.freshplan.modules.leads.api.admin.dto.BackdatingRequest;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.Territory;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration Tests für Backdating REST API.
 *
 * <p>Sprint 2.1.6 - User Story 4
 */
@QuarkusTest
class LeadBackdatingResourceTest {

  @Inject EntityManager em;

  private Lead testLead;

  @BeforeEach
  @Transactional
  void setup() {
    // IMPORTANT: Delete LeadContact BEFORE Lead (Hibernate bulk delete doesn't trigger CASCADE)
    em.createQuery("DELETE FROM Opportunity").executeUpdate();

    em.createQuery("DELETE FROM LeadContact").executeUpdate();
    em.createQuery("DELETE FROM LeadActivity").executeUpdate();
    em.createQuery("DELETE FROM Lead").executeUpdate();

    // Clear persistence context to avoid stale references
    em.flush();
    em.clear();

    // Get or create Territory - always use em.find/em.merge to ensure managed state
    Territory territory = em.find(Territory.class, "DE");
    if (territory == null) {
      territory = new Territory();
      territory.id = "DE";
      territory.name = "Deutschland";
      territory.countryCode = "DE";
      territory.currencyCode = "EUR";
      territory.languageCode = "de-DE";
      territory.taxRate = new java.math.BigDecimal("19.00");
      territory.active = true;
      em.persist(territory);
      em.flush();
    }

    testLead = new Lead();
    testLead.companyName = "Backdating Test Restaurant";
    testLead.city = "Berlin";
    testLead.territory = territory;
    testLead.ownerUserId = "admin-user";
    testLead.createdBy = "admin-user";
    testLead.updatedBy = "admin-user";
    testLead.registeredAt = LocalDateTime.now().minusMonths(2);
    testLead.protectionStartAt = testLead.registeredAt;
    testLead.progressDeadline = testLead.registeredAt.plusDays(60);
    testLead.persist();
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
  @TestSecurity(
      user = "admin-user",
      roles = {"ADMIN"})
  void shouldBackdateRegisteredAtSuccessfully() {
    LocalDateTime newDate = LocalDateTime.now().minusMonths(5);

    BackdatingRequest request = new BackdatingRequest();
    request.registeredAt = newDate;
    request.reason = "Migration von Altsystem - Import Bestandsdaten Q2 2024";

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put("/api/leads/" + testLead.id + "/registered-at")
        .then()
        .statusCode(200)
        .body("leadId", equalTo(testLead.id.intValue()))
        .body("newRegisteredAt", notNullValue())
        .body("newProtectionUntil", notNullValue())
        .body("newProgressDeadline", notNullValue())
        .body("message", containsString("successfully"));
  }

  @Test
  @TestSecurity(
      user = "manager-user",
      roles = {"MANAGER"})
  void shouldAllowManagerToBackdate() {
    LocalDateTime newDate = LocalDateTime.now().minusMonths(3);

    BackdatingRequest request = new BackdatingRequest();
    request.registeredAt = newDate;
    request.reason = "Historisches Datum korrigieren";

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put("/api/leads/" + testLead.id + "/registered-at")
        .then()
        .statusCode(200)
        .body("leadId", equalTo(testLead.id.intValue()));
  }

  @Test
  @TestSecurity(
      user = "regular-user",
      roles = {"USER"})
  void shouldRejectNonAdminUser() {
    BackdatingRequest request = new BackdatingRequest();
    request.registeredAt = LocalDateTime.now().minusMonths(1);
    request.reason = "Test";

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put("/api/leads/" + testLead.id + "/registered-at")
        .then()
        .statusCode(403); // Forbidden
  }

  @Test
  @TestSecurity(
      user = "admin-user",
      roles = {"ADMIN"})
  void shouldRejectFutureDate() {
    BackdatingRequest request = new BackdatingRequest();
    request.registeredAt = LocalDateTime.now().plusDays(1);
    request.reason = "Invalid future date";

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put("/api/leads/" + testLead.id + "/registered-at")
        .then()
        .statusCode(400)
        .body("error", containsString("future"));
  }

  @Test
  @TestSecurity(
      user = "admin-user",
      roles = {"ADMIN"})
  void shouldReturn404ForNonExistentLead() {
    BackdatingRequest request = new BackdatingRequest();
    request.registeredAt = LocalDateTime.now().minusMonths(1);
    request.reason = "Valid reason for testing 404 response";

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put("/api/leads/99999/registered-at")
        .then()
        .statusCode(404)
        .body("error", containsString("not found"));
  }

  @Test
  @TestSecurity(
      user = "admin-user",
      roles = {"ADMIN"})
  void shouldValidateReasonMinLength() {
    BackdatingRequest request = new BackdatingRequest();
    request.registeredAt = LocalDateTime.now().minusMonths(1);
    request.reason = "Short"; // Too short (min 10 chars)

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put("/api/leads/" + testLead.id + "/registered-at")
        .then()
        .statusCode(400); // Validation error
  }
}
