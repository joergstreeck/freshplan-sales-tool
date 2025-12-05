package de.freshplan.modules.leads.api.admin;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.modules.leads.domain.ImportLog;
import de.freshplan.modules.leads.domain.ImportLog.ImportLogStatus;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * REST API Integration Tests für ImportAdminResource.
 *
 * <p>Sprint 2.1.8 - Phase 3: Admin-UI + Routing
 *
 * <p>Testet HTTP Endpoints für Import Admin-Übersicht:
 *
 * <ul>
 *   <li>GET /api/admin/imports/stats - Import-Statistiken
 *   <li>GET /api/admin/imports - Alle Imports
 *   <li>GET /api/admin/imports/pending - Pending Approvals
 *   <li>POST /api/admin/imports/{id}/approve - Import genehmigen
 *   <li>POST /api/admin/imports/{id}/reject - Import ablehnen
 * </ul>
 *
 * <p><strong>RBAC Tests:</strong> ADMIN/MANAGER erlaubt, SALES verboten
 *
 * @see ImportAdminResource
 * @since Sprint 2.1.8
 */
@QuarkusTest
@Tag("integration")
@DisplayName("ImportAdminResource REST API Tests")
class ImportAdminResourceTest {

  @Inject EntityManager em;

  private String testUserId;
  private ImportLog completedImport;
  private ImportLog pendingImport;
  private ImportLog rejectedImport;

  @BeforeEach
  @Transactional
  void setup() {
    testUserId = "TEST-USER-" + UUID.randomUUID().toString().substring(0, 8);

    // Create completed import
    completedImport = new ImportLog();
    completedImport.userId = testUserId;
    completedImport.importedAt = LocalDateTime.now().minusDays(2);
    completedImport.totalRows = 50;
    completedImport.importedCount = 45;
    completedImport.skippedCount = 3;
    completedImport.errorCount = 2;
    completedImport.duplicateRate = new BigDecimal("6.00");
    completedImport.source = "MESSE_FRANKFURT_2025";
    completedImport.fileName = "leads_completed.csv";
    completedImport.fileSizeBytes = 25600L;
    completedImport.fileType = "CSV";
    completedImport.status = ImportLogStatus.COMPLETED;
    completedImport.persist();

    // Create pending approval import (>10% duplicates)
    pendingImport = new ImportLog();
    pendingImport.userId = testUserId;
    pendingImport.importedAt = LocalDateTime.now().minusHours(2);
    pendingImport.totalRows = 100;
    pendingImport.importedCount = 85;
    pendingImport.skippedCount = 12;
    pendingImport.errorCount = 3;
    pendingImport.duplicateRate = new BigDecimal("12.00");
    pendingImport.source = "MANUAL_UPLOAD";
    pendingImport.fileName = "leads_pending.xlsx";
    pendingImport.fileSizeBytes = 51200L;
    pendingImport.fileType = "XLSX";
    pendingImport.status = ImportLogStatus.PENDING_APPROVAL;
    pendingImport.persist();

    // Create rejected import
    rejectedImport = new ImportLog();
    rejectedImport.userId = testUserId;
    rejectedImport.importedAt = LocalDateTime.now().minusDays(5);
    rejectedImport.totalRows = 200;
    rejectedImport.importedCount = 0;
    rejectedImport.skippedCount = 180;
    rejectedImport.errorCount = 20;
    rejectedImport.duplicateRate = new BigDecimal("90.00");
    rejectedImport.source = "EXTERNAL_API";
    rejectedImport.fileName = "leads_rejected.csv";
    rejectedImport.fileSizeBytes = 102400L;
    rejectedImport.fileType = "CSV";
    rejectedImport.status = ImportLogStatus.REJECTED;
    rejectedImport.approvedBy = "admin-user";
    rejectedImport.approvedAt = LocalDateTime.now().minusDays(4);
    rejectedImport.rejectionReason = "Zu viele Duplikate (90%)";
    rejectedImport.persist();

    em.flush();
  }

  @AfterEach
  @Transactional
  void cleanup() {
    em.createQuery("DELETE FROM ImportLog i WHERE i.userId = :userId")
        .setParameter("userId", testUserId)
        .executeUpdate();
  }

  // ============================================================================
  // GET /api/admin/imports/stats - Import-Statistiken
  // ============================================================================

  @Nested
  @DisplayName("GET /api/admin/imports/stats")
  class StatsEndpoint {

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("ADMIN kann Statistiken abrufen")
    void testGetStats_AdminSuccess() {
      given()
          .when()
          .get("/api/admin/imports/stats")
          .then()
          .statusCode(200)
          .body("totalImports", greaterThanOrEqualTo(3))
          .body("pendingApprovals", greaterThanOrEqualTo(1))
          .body("completedImports", greaterThanOrEqualTo(1))
          .body("rejectedImports", greaterThanOrEqualTo(1))
          .body("totalImported", greaterThanOrEqualTo(45))
          .body("totalSkipped", greaterThanOrEqualTo(3))
          .body("totalErrors", greaterThanOrEqualTo(2));
    }

    @Test
    @TestSecurity(
        user = "manager-user",
        roles = {"MANAGER"})
    @DisplayName("MANAGER kann Statistiken abrufen")
    void testGetStats_ManagerSuccess() {
      given()
          .when()
          .get("/api/admin/imports/stats")
          .then()
          .statusCode(200)
          .body("totalImports", greaterThanOrEqualTo(0));
    }

    @Test
    @TestSecurity(
        user = "sales-user",
        roles = {"SALES"})
    @DisplayName("SALES wird abgelehnt (403)")
    void testGetStats_SalesForbidden() {
      given().when().get("/api/admin/imports/stats").then().statusCode(403);
    }

    @Test
    @DisplayName("Unauthenticated wird abgelehnt (401)")
    void testGetStats_Unauthenticated() {
      given().when().get("/api/admin/imports/stats").then().statusCode(401);
    }
  }

  // ============================================================================
  // GET /api/admin/imports - Alle Imports
  // ============================================================================

  @Nested
  @DisplayName("GET /api/admin/imports")
  class AllImportsEndpoint {

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("ADMIN kann alle Imports abrufen")
    void testGetAllImports_AdminSuccess() {
      given()
          .when()
          .get("/api/admin/imports")
          .then()
          .statusCode(200)
          .body("size()", greaterThanOrEqualTo(3))
          .body("[0].userId", notNullValue())
          .body("[0].status", notNullValue())
          .body("[0].fileName", notNullValue());
    }

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("Filter: nur COMPLETED Status")
    void testGetAllImports_FilterByStatus() {
      given()
          .queryParam("status", "COMPLETED")
          .when()
          .get("/api/admin/imports")
          .then()
          .statusCode(200)
          .body("findAll { it.status == 'COMPLETED' }.size()", greaterThanOrEqualTo(1))
          .body("findAll { it.status != 'COMPLETED' }.size()", equalTo(0));
    }

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("Limit-Parameter funktioniert")
    void testGetAllImports_WithLimit() {
      given()
          .queryParam("limit", 1)
          .when()
          .get("/api/admin/imports")
          .then()
          .statusCode(200)
          .body("size()", lessThanOrEqualTo(1));
    }

    @Test
    @TestSecurity(
        user = "manager-user",
        roles = {"MANAGER"})
    @DisplayName("MANAGER kann Imports abrufen")
    void testGetAllImports_ManagerSuccess() {
      given().when().get("/api/admin/imports").then().statusCode(200);
    }

    @Test
    @TestSecurity(
        user = "sales-user",
        roles = {"SALES"})
    @DisplayName("SALES wird abgelehnt (403)")
    void testGetAllImports_SalesForbidden() {
      given().when().get("/api/admin/imports").then().statusCode(403);
    }
  }

  // ============================================================================
  // GET /api/admin/imports/pending - Pending Approvals
  // ============================================================================

  @Nested
  @DisplayName("GET /api/admin/imports/pending")
  class PendingApprovalsEndpoint {

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("ADMIN kann Pending Approvals abrufen")
    void testGetPendingApprovals_AdminSuccess() {
      given()
          .when()
          .get("/api/admin/imports/pending")
          .then()
          .statusCode(200)
          .body("size()", greaterThanOrEqualTo(1))
          .body("[0].status", equalTo("PENDING_APPROVAL"))
          .body("[0].duplicateRate", greaterThan(10.0f));
    }

    @Test
    @TestSecurity(
        user = "manager-user",
        roles = {"MANAGER"})
    @DisplayName("MANAGER kann Pending Approvals abrufen")
    void testGetPendingApprovals_ManagerSuccess() {
      given().when().get("/api/admin/imports/pending").then().statusCode(200);
    }

    @Test
    @TestSecurity(
        user = "sales-user",
        roles = {"SALES"})
    @DisplayName("SALES wird abgelehnt (403)")
    void testGetPendingApprovals_SalesForbidden() {
      given().when().get("/api/admin/imports/pending").then().statusCode(403);
    }
  }

  // ============================================================================
  // Helper Methods (outside @Nested classes for CDI compatibility)
  // ============================================================================

  @Transactional
  ImportLog createFreshPendingImport() {
    ImportLog fresh = new ImportLog();
    fresh.userId = testUserId;
    fresh.importedAt = LocalDateTime.now();
    fresh.totalRows = 50;
    fresh.importedCount = 40;
    fresh.skippedCount = 8;
    fresh.errorCount = 2;
    fresh.duplicateRate = new BigDecimal("16.00");
    fresh.status = ImportLogStatus.PENDING_APPROVAL;
    fresh.persist();
    em.flush();
    return fresh;
  }

  // ============================================================================
  // POST /api/admin/imports/{id}/approve - Import genehmigen
  // ============================================================================

  @Nested
  @DisplayName("POST /api/admin/imports/{id}/approve")
  class ApproveEndpoint {

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("ADMIN kann Import genehmigen")
    void testApproveImport_AdminSuccess() {
      given()
          .when()
          .post("/api/admin/imports/" + pendingImport.id + "/approve")
          .then()
          .statusCode(200)
          .body("status", equalTo("COMPLETED"))
          .body("approvedBy", equalTo("admin-user"))
          .body("approvedAt", notNullValue());
    }

    @Test
    @TestSecurity(
        user = "manager-user",
        roles = {"MANAGER"})
    @DisplayName("MANAGER kann Import genehmigen")
    void testApproveImport_ManagerSuccess() {
      // Create fresh pending import for this test
      ImportLog freshPending = createFreshPendingImport();

      given()
          .when()
          .post("/api/admin/imports/" + freshPending.id + "/approve")
          .then()
          .statusCode(200)
          .body("status", equalTo("COMPLETED"))
          .body("approvedBy", equalTo("manager-user"));
    }

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("Nicht existierender Import gibt 404")
    void testApproveImport_NotFound() {
      UUID nonExistentId = UUID.randomUUID();

      given()
          .when()
          .post("/api/admin/imports/" + nonExistentId + "/approve")
          .then()
          .statusCode(404)
          .body("message", containsString("nicht gefunden"));
    }

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("Bereits abgeschlossener Import gibt 400")
    void testApproveImport_AlreadyCompleted() {
      given()
          .when()
          .post("/api/admin/imports/" + completedImport.id + "/approve")
          .then()
          .statusCode(400)
          .body("message", containsString("nicht in Status PENDING_APPROVAL"));
    }

    @Test
    @TestSecurity(
        user = "sales-user",
        roles = {"SALES"})
    @DisplayName("SALES wird abgelehnt (403)")
    void testApproveImport_SalesForbidden() {
      given()
          .when()
          .post("/api/admin/imports/" + pendingImport.id + "/approve")
          .then()
          .statusCode(403);
    }
  }

  // ============================================================================
  // POST /api/admin/imports/{id}/reject - Import ablehnen
  // ============================================================================

  @Nested
  @DisplayName("POST /api/admin/imports/{id}/reject")
  class RejectEndpoint {

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("ADMIN kann Import ablehnen mit Begründung")
    void testRejectImport_AdminSuccess() {
      // Create fresh pending import for this test
      ImportLog freshPending = createFreshPendingImport();

      given()
          .contentType(ContentType.JSON)
          .body(Map.of("reason", "Datenqualität unzureichend"))
          .when()
          .post("/api/admin/imports/" + freshPending.id + "/reject")
          .then()
          .statusCode(200)
          .body("status", equalTo("REJECTED"))
          .body("approvedBy", equalTo("admin-user"))
          .body("rejectionReason", equalTo("Datenqualität unzureichend"));
    }

    @Test
    @TestSecurity(
        user = "manager-user",
        roles = {"MANAGER"})
    @DisplayName("MANAGER kann Import ablehnen")
    void testRejectImport_ManagerSuccess() {
      ImportLog freshPending = createFreshPendingImport();

      given()
          .contentType(ContentType.JSON)
          .body(Map.of("reason", "Zu viele Duplikate"))
          .when()
          .post("/api/admin/imports/" + freshPending.id + "/reject")
          .then()
          .statusCode(200)
          .body("status", equalTo("REJECTED"))
          .body("approvedBy", equalTo("manager-user"));
    }

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("Nicht existierender Import gibt 404")
    void testRejectImport_NotFound() {
      UUID nonExistentId = UUID.randomUUID();

      given()
          .contentType(ContentType.JSON)
          .body(Map.of("reason", "Test"))
          .when()
          .post("/api/admin/imports/" + nonExistentId + "/reject")
          .then()
          .statusCode(404)
          .body("message", containsString("nicht gefunden"));
    }

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("Bereits abgeschlossener Import gibt 400")
    void testRejectImport_AlreadyCompleted() {
      given()
          .contentType(ContentType.JSON)
          .body(Map.of("reason", "Test"))
          .when()
          .post("/api/admin/imports/" + completedImport.id + "/reject")
          .then()
          .statusCode(400)
          .body("message", containsString("nicht in Status PENDING_APPROVAL"));
    }

    @Test
    @TestSecurity(
        user = "sales-user",
        roles = {"SALES"})
    @DisplayName("SALES wird abgelehnt (403)")
    void testRejectImport_SalesForbidden() {
      given()
          .contentType(ContentType.JSON)
          .body(Map.of("reason", "Test"))
          .when()
          .post("/api/admin/imports/" + pendingImport.id + "/reject")
          .then()
          .statusCode(403);
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
    @DisplayName("ImportStatsResponse enthält alle Felder")
    void testStatsResponse_AllFields() {
      given()
          .when()
          .get("/api/admin/imports/stats")
          .then()
          .statusCode(200)
          .body("$", hasKey("totalImports"))
          .body("$", hasKey("pendingApprovals"))
          .body("$", hasKey("completedImports"))
          .body("$", hasKey("rejectedImports"))
          .body("$", hasKey("totalImported"))
          .body("$", hasKey("totalSkipped"))
          .body("$", hasKey("totalErrors"));
    }

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("ImportLogDTO enthält alle Felder")
    void testImportLogDto_AllFields() {
      given()
          .when()
          .get("/api/admin/imports")
          .then()
          .statusCode(200)
          .body("[0]", hasKey("id"))
          .body("[0]", hasKey("userId"))
          .body("[0]", hasKey("importedAt"))
          .body("[0]", hasKey("totalRows"))
          .body("[0]", hasKey("importedCount"))
          .body("[0]", hasKey("skippedCount"))
          .body("[0]", hasKey("errorCount"))
          .body("[0]", hasKey("duplicateRate"))
          .body("[0]", hasKey("source"))
          .body("[0]", hasKey("fileName"))
          .body("[0]", hasKey("fileSizeBytes"))
          .body("[0]", hasKey("fileType"))
          .body("[0]", hasKey("status"));
    }

    @Test
    @TestSecurity(
        user = "admin-user",
        roles = {"ADMIN"})
    @DisplayName("Rejected ImportLogDTO enthält Ablehnungsgrund")
    void testImportLogDto_RejectedFields() {
      given()
          .queryParam("status", "REJECTED")
          .when()
          .get("/api/admin/imports")
          .then()
          .statusCode(200)
          .body("[0].status", equalTo("REJECTED"))
          .body("[0].approvedBy", notNullValue())
          .body("[0].approvedAt", notNullValue())
          .body("[0].rejectionReason", notNullValue());
    }
  }
}
