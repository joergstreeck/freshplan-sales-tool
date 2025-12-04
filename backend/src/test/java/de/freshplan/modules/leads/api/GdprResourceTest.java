package de.freshplan.modules.leads.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.Territory;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * REST API Integration Tests für GdprResource.
 *
 * <p>Sprint 2.1.8 - Phase 1: DSGVO Compliance
 *
 * <p>Testet HTTP Endpoints für DSGVO-Operationen:
 *
 * <ul>
 *   <li>GET /api/gdpr/leads/{id}/data-export - PDF-Export
 *   <li>DELETE /api/gdpr/leads/{id} - DSGVO-Löschung (204 No Content)
 *   <li>POST /api/gdpr/leads/{id}/revoke-consent - Einwilligungswiderruf (204 No Content)
 *   <li>GET /api/gdpr/leads/{id}/contact-allowed - Kontaktprüfung
 * </ul>
 *
 * <p><strong>RBAC Tests:</strong> ADMIN/MANAGER erlaubt, SALES limitiert
 */
@QuarkusTest
@Transactional
class GdprResourceTest {

  @Inject EntityManager em;

  private Lead testLead;
  private String testPrefix;

  @BeforeEach
  void setup() {
    testPrefix = "GDPR-API-TEST-" + UUID.randomUUID().toString().substring(0, 8);

    // Ensure territory exists
    Territory territory = Territory.findByCode("DE");
    if (territory == null) {
      territory = new Territory();
      territory.id = "DE";
      territory.name = "Deutschland";
      territory.countryCode = "DE";
      territory.currencyCode = "EUR";
      territory.taxRate = new BigDecimal("19.0");
      territory.persist();
    }

    // Create test lead
    testLead = new Lead();
    testLead.companyName = testPrefix + " Test GmbH";
    testLead.contactPerson = "Test Person";
    testLead.email = "test@" + testPrefix.toLowerCase() + ".de";
    testLead.phone = "+49 89 12345678";
    testLead.street = "Teststraße 1";
    testLead.city = "München";
    testLead.postalCode = "80331";
    testLead.status = LeadStatus.REGISTERED;
    testLead.registeredAt = LocalDateTime.now().minusDays(5);
    testLead.territory = territory;
    testLead.createdBy = "test-user";
    testLead.createdAt = LocalDateTime.now();
    testLead.updatedAt = LocalDateTime.now();
    testLead.persist();

    em.flush();
  }

  @AfterEach
  void cleanup() {
    em.createQuery("DELETE FROM GdprDeletionLog g WHERE g.entityId = :leadId")
        .setParameter("leadId", testLead.id)
        .executeUpdate();

    em.createQuery("DELETE FROM GdprDataRequest g WHERE g.entityId = :leadId")
        .setParameter("leadId", testLead.id)
        .executeUpdate();

    em.createQuery("DELETE FROM Opportunity o WHERE o.lead.id = :leadId")
        .setParameter("leadId", testLead.id)
        .executeUpdate();

    em.createQuery("DELETE FROM Lead l WHERE l.companyName LIKE :prefix")
        .setParameter("prefix", "GDPR-API-TEST-%")
        .executeUpdate();
  }

  // ============================================================================
  // Art. 15 - Datenexport Endpoint
  // ============================================================================

  @Test
  @TestSecurity(
      user = "admin-user",
      roles = {"ADMIN"})
  @DisplayName("GET /data-export: ADMIN kann PDF exportieren")
  void testDataExport_AdminSuccess() {
    byte[] response =
        given()
            .when()
            .get("/api/gdpr/leads/" + testLead.id + "/data-export")
            .then()
            .statusCode(200)
            .contentType("application/pdf")
            .header("Content-Disposition", containsString("attachment"))
            .extract()
            .asByteArray();

    // Verify PDF magic bytes
    assertEquals((byte) 0x25, response[0]); // %
    assertEquals((byte) 0x50, response[1]); // P
    assertEquals((byte) 0x44, response[2]); // D
    assertEquals((byte) 0x46, response[3]); // F
  }

  @Test
  @TestSecurity(
      user = "manager-user",
      roles = {"MANAGER"})
  @DisplayName("GET /data-export: MANAGER kann PDF exportieren")
  void testDataExport_ManagerSuccess() {
    given()
        .when()
        .get("/api/gdpr/leads/" + testLead.id + "/data-export")
        .then()
        .statusCode(200)
        .contentType("application/pdf");
  }

  @Test
  @TestSecurity(
      user = "sales-user",
      roles = {"SALES"})
  @DisplayName("GET /data-export: SALES wird abgelehnt (403)")
  void testDataExport_SalesForbidden() {
    given().when().get("/api/gdpr/leads/" + testLead.id + "/data-export").then().statusCode(403);
  }

  @Test
  @TestSecurity(
      user = "admin-user",
      roles = {"ADMIN"})
  @DisplayName("GET /data-export: Nicht existierender Lead gibt 404")
  void testDataExport_NotFound() {
    given().when().get("/api/gdpr/leads/999999/data-export").then().statusCode(404);
  }

  // ============================================================================
  // Art. 17 - DSGVO-Löschung Endpoint (204 No Content)
  // ============================================================================

  @Test
  @TestSecurity(
      user = "admin-user",
      roles = {"ADMIN"})
  @DisplayName("DELETE: ADMIN kann Lead löschen (204)")
  void testGdprDelete_AdminSuccess() {
    given()
        .contentType(ContentType.JSON)
        .body(Map.of("reason", "Art. 17 - Betroffener hat Löschung beantragt"))
        .when()
        .delete("/api/gdpr/leads/" + testLead.id)
        .then()
        .statusCode(204); // No Content

    // Verify lead is marked as deleted
    em.clear();
    Lead reloadedLead = Lead.findById(testLead.id);
    assertTrue(reloadedLead.gdprDeleted);
  }

  @Test
  @TestSecurity(
      user = "manager-user",
      roles = {"MANAGER"})
  @DisplayName("DELETE: MANAGER kann Lead löschen (204)")
  void testGdprDelete_ManagerSuccess() {
    given()
        .contentType(ContentType.JSON)
        .body(Map.of("reason", "Art. 17 - Test-Löschung durch Manager"))
        .when()
        .delete("/api/gdpr/leads/" + testLead.id)
        .then()
        .statusCode(204);
  }

  @Test
  @TestSecurity(
      user = "sales-user",
      roles = {"SALES"})
  @DisplayName("DELETE: SALES wird abgelehnt (403)")
  void testGdprDelete_SalesForbidden() {
    given()
        .contentType(ContentType.JSON)
        .body(Map.of("reason", "Versuch durch Sales"))
        .when()
        .delete("/api/gdpr/leads/" + testLead.id)
        .then()
        .statusCode(403);
  }

  @Test
  @TestSecurity(
      user = "admin-user",
      roles = {"ADMIN"})
  @DisplayName("DELETE: Nicht existierender Lead gibt 404")
  void testGdprDelete_NotFound() {
    given()
        .contentType(ContentType.JSON)
        .body(Map.of("reason", "Art. 17 - Löschung für nicht existierenden Lead"))
        .when()
        .delete("/api/gdpr/leads/999999")
        .then()
        .statusCode(404);
  }

  // Note: This test is moved to GdprServiceTest as unit test because
  // REST endpoint tests run in separate transactions, making it hard
  // to test the Opportunity blocking scenario through HTTP.
  // The business logic is verified in GdprServiceTest.testGdprDeleteLead_BlockedByOpportunity

  // ============================================================================
  // Art. 7.3 - Einwilligungswiderruf Endpoint (204 No Content)
  // ============================================================================

  @Test
  @TestSecurity(
      user = "admin-user",
      roles = {"ADMIN"})
  @DisplayName("POST /revoke-consent: ADMIN kann Einwilligung widerrufen (204)")
  void testRevokeConsent_AdminSuccess() {
    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/api/gdpr/leads/" + testLead.id + "/revoke-consent")
        .then()
        .statusCode(204);

    // Verify lead consent is revoked
    em.clear();
    Lead reloadedLead = Lead.findById(testLead.id);
    assertNotNull(reloadedLead.consentRevokedAt);
    assertTrue(reloadedLead.contactBlocked);
  }

  @Test
  @TestSecurity(
      user = "manager-user",
      roles = {"MANAGER"})
  @DisplayName("POST /revoke-consent: MANAGER kann Einwilligung widerrufen (204)")
  void testRevokeConsent_ManagerSuccess() {
    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/api/gdpr/leads/" + testLead.id + "/revoke-consent")
        .then()
        .statusCode(204);
  }

  @Test
  @TestSecurity(
      user = "sales-user",
      roles = {"SALES"})
  @DisplayName("POST /revoke-consent: SALES kann auch widerrufen (Kunde hat Recht!)")
  void testRevokeConsent_SalesAllowed() {
    // Note: According to Art. 7.3, the customer has the right to revoke consent
    // Therefore, all authenticated users should be able to perform this action
    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/api/gdpr/leads/" + testLead.id + "/revoke-consent")
        .then()
        .statusCode(204);
  }

  @Test
  @TestSecurity(
      user = "admin-user",
      roles = {"ADMIN"})
  @DisplayName("POST /revoke-consent: Nicht existierender Lead gibt 404")
  void testRevokeConsent_NotFound() {
    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/api/gdpr/leads/999999/revoke-consent")
        .then()
        .statusCode(404);
  }

  // ============================================================================
  // Kontakt-Erlaubnis Endpoint
  // ============================================================================

  @Test
  @TestSecurity(
      user = "sales-user",
      roles = {"SALES"})
  @DisplayName("GET /contact-allowed: Normaler Lead erlaubt Kontakt")
  void testContactAllowed_NormalLead() {
    given()
        .when()
        .get("/api/gdpr/leads/" + testLead.id + "/contact-allowed")
        .then()
        .statusCode(200)
        .body("contactAllowed", is(true))
        .body("leadId", equalTo(testLead.id.intValue()));
  }

  @Test
  @TestSecurity(
      user = "admin-user",
      roles = {"ADMIN"})
  @DisplayName("GET /contact-allowed: Nach Widerruf nicht erlaubt")
  void testContactAllowed_AfterRevocation() {
    // First revoke consent
    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/api/gdpr/leads/" + testLead.id + "/revoke-consent")
        .then()
        .statusCode(204);

    // Then check contact allowed
    given()
        .when()
        .get("/api/gdpr/leads/" + testLead.id + "/contact-allowed")
        .then()
        .statusCode(200)
        .body("contactAllowed", is(false));
  }

  @Test
  @TestSecurity(
      user = "sales-user",
      roles = {"SALES"})
  @DisplayName("GET /contact-allowed: Nicht existierender Lead gibt false")
  void testContactAllowed_NotFound() {
    given()
        .when()
        .get("/api/gdpr/leads/999999/contact-allowed")
        .then()
        .statusCode(200)
        .body("contactAllowed", is(false));
  }

  // ============================================================================
  // Audit-Logs Endpoints
  // ============================================================================

  @Test
  @TestSecurity(
      user = "admin-user",
      roles = {"ADMIN"})
  @DisplayName("GET /data-requests: ADMIN kann Anfragen abrufen")
  void testGetDataRequests_AdminSuccess() {
    // First generate a data export to create a request
    given().when().get("/api/gdpr/leads/" + testLead.id + "/data-export").then().statusCode(200);

    // Then get the requests
    given()
        .when()
        .get("/api/gdpr/leads/" + testLead.id + "/data-requests")
        .then()
        .statusCode(200)
        .body("size()", greaterThan(0))
        .body("[0].entityType", equalTo("LEAD"))
        .body("[0].pdfGenerated", is(true));
  }

  @Test
  @TestSecurity(
      user = "admin-user",
      roles = {"ADMIN"})
  @DisplayName("GET /deletion-logs: ADMIN kann Löschprotokolle abrufen")
  void testGetDeletionLogs_AdminSuccess() {
    // First delete the lead
    given()
        .contentType(ContentType.JSON)
        .body(Map.of("reason", "Art. 17 - Test für Löschprotokoll"))
        .when()
        .delete("/api/gdpr/leads/" + testLead.id)
        .then()
        .statusCode(204);

    // Then get the logs
    given()
        .when()
        .get("/api/gdpr/leads/" + testLead.id + "/deletion-logs")
        .then()
        .statusCode(200)
        .body("size()", greaterThan(0))
        .body("[0].entityType", equalTo("LEAD"))
        .body("[0].originalDataHash", notNullValue());
  }
}
