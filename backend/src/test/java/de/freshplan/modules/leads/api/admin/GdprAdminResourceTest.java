package de.freshplan.modules.leads.api.admin;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.modules.leads.domain.GdprDataRequest;
import de.freshplan.modules.leads.domain.GdprDeletionLog;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.Territory;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * REST API Integration Tests für GdprAdminResource.
 *
 * <p>Sprint 2.1.8 - Phase 3: Admin-UI + Routing
 *
 * <p>Testet HTTP Endpoints für DSGVO Admin-Übersicht:
 *
 * <ul>
 *   <li>GET /api/admin/gdpr/stats - DSGVO-Statistiken
 *   <li>GET /api/admin/gdpr/deletions - Löschprotokolle
 *   <li>GET /api/admin/gdpr/data-requests - Datenexport-Anfragen
 *   <li>GET /api/admin/gdpr/deleted-leads - Gelöschte Leads
 * </ul>
 *
 * <p><strong>RBAC Tests:</strong> ADMIN/MANAGER erlaubt, SALES verboten
 *
 * @see GdprAdminResource
 * @since Sprint 2.1.8
 */
@QuarkusTest
@Tag("integration")
@DisplayName("GdprAdminResource REST API Tests")
class GdprAdminResourceTest {

  @Inject EntityManager em;

  private String testPrefix;
  private Lead testLead;
  private Lead deletedLead;

  @BeforeEach
  @Transactional
  void setup() {
    testPrefix = "GDPR-ADMIN-TEST-" + UUID.randomUUID().toString().substring(0, 8);

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

    // Create normal test lead
    testLead = new Lead();
    testLead.companyName = testPrefix + " Normal GmbH";
    testLead.contactPerson = "Test Person";
    testLead.email = "normal@" + testPrefix.toLowerCase() + ".de";
    testLead.phone = "+49 89 12345678";
    testLead.city = "München";
    testLead.postalCode = "80331";
    testLead.status = LeadStatus.REGISTERED;
    testLead.registeredAt = LocalDateTime.now().minusDays(5);
    testLead.territory = territory;
    testLead.createdBy = "test-user";
    testLead.createdAt = LocalDateTime.now();
    testLead.updatedAt = LocalDateTime.now();
    testLead.persist();

    // Create GDPR-deleted lead
    deletedLead = new Lead();
    deletedLead.companyName = testPrefix + " Gelöscht GmbH";
    deletedLead.contactPerson = "[DSGVO GELÖSCHT]";
    deletedLead.email = "deleted@example.de";
    deletedLead.city = "Berlin";
    deletedLead.postalCode = "10115";
    deletedLead.status = LeadStatus.REGISTERED;
    deletedLead.registeredAt = LocalDateTime.now().minusDays(30);
    deletedLead.territory = territory;
    deletedLead.createdBy = "test-user";
    deletedLead.createdAt = LocalDateTime.now();
    deletedLead.updatedAt = LocalDateTime.now();
    deletedLead.gdprDeleted = true;
    deletedLead.gdprDeletedAt = LocalDateTime.now().minusDays(1);
    deletedLead.gdprDeletedBy = "admin-user";
    deletedLead.gdprDeletionReason = "Art. 17 - Betroffener hat Löschung beantragt";
    deletedLead.persist();

    // Create deletion log
    GdprDeletionLog deletionLog = new GdprDeletionLog();
    deletionLog.entityType = "LEAD";
    deletionLog.entityId = deletedLead.id;
    deletionLog.deletedBy = "admin-user";
    deletionLog.deletedAt = LocalDateTime.now().minusDays(1);
    deletionLog.deletionReason = "Art. 17 - Betroffener hat Löschung beantragt";
    deletionLog.originalDataHash = "abc123hash";
    deletionLog.persist();

    // Create data request
    GdprDataRequest dataRequest = new GdprDataRequest();
    dataRequest.entityType = "LEAD";
    dataRequest.entityId = testLead.id;
    dataRequest.requestedBy = "manager-user";
    dataRequest.requestedAt = LocalDateTime.now().minusHours(2);
    dataRequest.pdfGenerated = true;
    dataRequest.pdfGeneratedAt = LocalDateTime.now().minusHours(1);
    dataRequest.persist();

    // Create pending data request
    GdprDataRequest pendingRequest = new GdprDataRequest();
    pendingRequest.entityType = "LEAD";
    pendingRequest.entityId = testLead.id;
    pendingRequest.requestedBy = "admin-user";
    pendingRequest.requestedAt = LocalDateTime.now();
    pendingRequest.pdfGenerated = false;
    pendingRequest.persist();

    em.flush();
  }

  @AfterEach
  @Transactional
  void cleanup() {
    // Clean up in correct order (FK constraints)
    em.createQuery(
            "DELETE FROM GdprDataRequest g WHERE g.entityId IN "
                + "(SELECT l.id FROM Lead l WHERE l.companyName LIKE :prefix)")
        .setParameter("prefix", "GDPR-ADMIN-TEST-%")
        .executeUpdate();

    em.createQuery(
            "DELETE FROM GdprDeletionLog g WHERE g.entityId IN "
                + "(SELECT l.id FROM Lead l WHERE l.companyName LIKE :prefix)")
        .setParameter("prefix", "GDPR-ADMIN-TEST-%")
        .executeUpdate();

    em.createQuery("DELETE FROM Lead l WHERE l.companyName LIKE :prefix")
        .setParameter("prefix", "GDPR-ADMIN-TEST-%")
        .executeUpdate();
  }

  // ============================================================================
  // GET /api/admin/gdpr/stats - DSGVO-Statistiken
  // ============================================================================

  @Nested
  @DisplayName("GET /api/admin/gdpr/stats")
  class StatsEndpoint {

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("ADMIN kann Statistiken abrufen")
    void testGetStats_AdminSuccess() {
      given()
          .when()
          .get("/api/admin/gdpr/stats")
          .then()
          .statusCode(200)
          .body("totalDeletions", greaterThanOrEqualTo(1))
          .body("totalDataRequests", greaterThanOrEqualTo(2))
          .body("pendingRequests", greaterThanOrEqualTo(1))
          .body("deletedLeads", greaterThanOrEqualTo(1))
          .body("blockedContacts", greaterThanOrEqualTo(0));
    }

    @Test
    @TestSecurity(
        user = "manager-user",
        roles = {"MANAGER"})
    @DisplayName("MANAGER kann Statistiken abrufen")
    void testGetStats_ManagerSuccess() {
      given()
          .when()
          .get("/api/admin/gdpr/stats")
          .then()
          .statusCode(200)
          .body("totalDeletions", greaterThanOrEqualTo(0));
    }

    @Test
    @TestSecurity(
        user = "sales-user",
        roles = {"SALES"})
    @DisplayName("SALES wird abgelehnt (403)")
    void testGetStats_SalesForbidden() {
      given().when().get("/api/admin/gdpr/stats").then().statusCode(403);
    }

    @Test
    @DisplayName("Unauthenticated wird abgelehnt (401)")
    void testGetStats_Unauthenticated() {
      given().when().get("/api/admin/gdpr/stats").then().statusCode(401);
    }
  }

  // ============================================================================
  // GET /api/admin/gdpr/deletions - Löschprotokolle
  // ============================================================================

  @Nested
  @DisplayName("GET /api/admin/gdpr/deletions")
  class DeletionsEndpoint {

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("ADMIN kann Löschprotokolle abrufen")
    void testGetDeletions_AdminSuccess() {
      given()
          .when()
          .get("/api/admin/gdpr/deletions")
          .then()
          .statusCode(200)
          .body("size()", greaterThanOrEqualTo(1))
          .body("[0].entityType", equalTo("LEAD"))
          .body("[0].deletedBy", notNullValue())
          .body("[0].deletionReason", notNullValue())
          .body("[0].originalDataHash", notNullValue());
    }

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("Filterung nach Datumsbereich funktioniert")
    void testGetDeletions_WithDateRange() {
      String from = LocalDateTime.now().minusDays(7).toLocalDate().toString();
      String to = LocalDateTime.now().toLocalDate().toString();

      given()
          .queryParam("from", from)
          .queryParam("to", to)
          .when()
          .get("/api/admin/gdpr/deletions")
          .then()
          .statusCode(200)
          .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("Limit-Parameter funktioniert")
    void testGetDeletions_WithLimit() {
      given()
          .queryParam("limit", 1)
          .when()
          .get("/api/admin/gdpr/deletions")
          .then()
          .statusCode(200)
          .body("size()", lessThanOrEqualTo(1));
    }

    @Test
    @TestSecurity(
        user = "manager-user",
        roles = {"MANAGER"})
    @DisplayName("MANAGER kann Löschprotokolle abrufen")
    void testGetDeletions_ManagerSuccess() {
      given().when().get("/api/admin/gdpr/deletions").then().statusCode(200);
    }

    @Test
    @TestSecurity(
        user = "sales-user",
        roles = {"SALES"})
    @DisplayName("SALES wird abgelehnt (403)")
    void testGetDeletions_SalesForbidden() {
      given().when().get("/api/admin/gdpr/deletions").then().statusCode(403);
    }
  }

  // ============================================================================
  // GET /api/admin/gdpr/data-requests - Datenexport-Anfragen
  // ============================================================================

  @Nested
  @DisplayName("GET /api/admin/gdpr/data-requests")
  class DataRequestsEndpoint {

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("ADMIN kann Datenexport-Anfragen abrufen")
    void testGetDataRequests_AdminSuccess() {
      given()
          .when()
          .get("/api/admin/gdpr/data-requests")
          .then()
          .statusCode(200)
          .body("size()", greaterThanOrEqualTo(2))
          .body("[0].entityType", equalTo("LEAD"))
          .body("[0].requestedBy", notNullValue())
          .body("[0].requestedAt", notNullValue());
    }

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("Filter: nur pending Anfragen")
    void testGetDataRequests_PendingOnly() {
      given()
          .queryParam("pending", true)
          .when()
          .get("/api/admin/gdpr/data-requests")
          .then()
          .statusCode(200)
          .body("size()", greaterThanOrEqualTo(1))
          .body("findAll { it.pdfGenerated == false }.size()", greaterThanOrEqualTo(1));
    }

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("Limit-Parameter funktioniert")
    void testGetDataRequests_WithLimit() {
      given()
          .queryParam("limit", 1)
          .when()
          .get("/api/admin/gdpr/data-requests")
          .then()
          .statusCode(200)
          .body("size()", lessThanOrEqualTo(1));
    }

    @Test
    @TestSecurity(
        user = "manager-user",
        roles = {"MANAGER"})
    @DisplayName("MANAGER kann Anfragen abrufen")
    void testGetDataRequests_ManagerSuccess() {
      given().when().get("/api/admin/gdpr/data-requests").then().statusCode(200);
    }

    @Test
    @TestSecurity(
        user = "sales-user",
        roles = {"SALES"})
    @DisplayName("SALES wird abgelehnt (403)")
    void testGetDataRequests_SalesForbidden() {
      given().when().get("/api/admin/gdpr/data-requests").then().statusCode(403);
    }
  }

  // ============================================================================
  // GET /api/admin/gdpr/deleted-leads - Gelöschte Leads
  // ============================================================================

  @Nested
  @DisplayName("GET /api/admin/gdpr/deleted-leads")
  class DeletedLeadsEndpoint {

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("ADMIN kann gelöschte Leads abrufen")
    void testGetDeletedLeads_AdminSuccess() {
      given()
          .when()
          .get("/api/admin/gdpr/deleted-leads")
          .then()
          .statusCode(200)
          .body("size()", greaterThanOrEqualTo(1))
          .body("[0].companyName", notNullValue())
          .body("[0].gdprDeletedAt", notNullValue())
          .body("[0].gdprDeletedBy", notNullValue())
          .body("[0].gdprDeletionReason", notNullValue());
    }

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("Limit-Parameter funktioniert")
    void testGetDeletedLeads_WithLimit() {
      given()
          .queryParam("limit", 1)
          .when()
          .get("/api/admin/gdpr/deleted-leads")
          .then()
          .statusCode(200)
          .body("size()", lessThanOrEqualTo(1));
    }

    @Test
    @TestSecurity(
        user = "manager-user",
        roles = {"MANAGER"})
    @DisplayName("MANAGER kann gelöschte Leads abrufen")
    void testGetDeletedLeads_ManagerSuccess() {
      given().when().get("/api/admin/gdpr/deleted-leads").then().statusCode(200);
    }

    @Test
    @TestSecurity(
        user = "sales-user",
        roles = {"SALES"})
    @DisplayName("SALES wird abgelehnt (403)")
    void testGetDeletedLeads_SalesForbidden() {
      given().when().get("/api/admin/gdpr/deleted-leads").then().statusCode(403);
    }
  }

  // ============================================================================
  // DTO Response Validation
  // ============================================================================

  @Nested
  @DisplayName("DTO Response Validation")
  class DtoValidation {

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("GdprStatsResponse enthält alle Felder")
    void testStatsResponse_AllFields() {
      given()
          .when()
          .get("/api/admin/gdpr/stats")
          .then()
          .statusCode(200)
          .body("$", hasKey("totalDeletions"))
          .body("$", hasKey("totalDataRequests"))
          .body("$", hasKey("pendingRequests"))
          .body("$", hasKey("deletedLeads"))
          .body("$", hasKey("blockedContacts"));
    }

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("GdprDeletionLogDTO enthält alle Felder")
    void testDeletionLogDto_AllFields() {
      given()
          .when()
          .get("/api/admin/gdpr/deletions")
          .then()
          .statusCode(200)
          .body("[0]", hasKey("id"))
          .body("[0]", hasKey("entityType"))
          .body("[0]", hasKey("entityId"))
          .body("[0]", hasKey("deletedBy"))
          .body("[0]", hasKey("deletedAt"))
          .body("[0]", hasKey("deletionReason"))
          .body("[0]", hasKey("originalDataHash"));
    }

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("GdprDataRequestDTO enthält alle Felder")
    void testDataRequestDto_AllFields() {
      given()
          .when()
          .get("/api/admin/gdpr/data-requests")
          .then()
          .statusCode(200)
          .body("[0]", hasKey("id"))
          .body("[0]", hasKey("entityType"))
          .body("[0]", hasKey("entityId"))
          .body("[0]", hasKey("requestedBy"))
          .body("[0]", hasKey("requestedAt"))
          .body("[0]", hasKey("pdfGenerated"))
          .body("[0]", hasKey("pdfGeneratedAt"));
    }

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("DeletedLeadDTO enthält alle Felder")
    void testDeletedLeadDto_AllFields() {
      given()
          .when()
          .get("/api/admin/gdpr/deleted-leads")
          .then()
          .statusCode(200)
          .body("[0]", hasKey("id"))
          .body("[0]", hasKey("companyName"))
          .body("[0]", hasKey("gdprDeletedAt"))
          .body("[0]", hasKey("gdprDeletedBy"))
          .body("[0]", hasKey("gdprDeletionReason"));
    }
  }
}
