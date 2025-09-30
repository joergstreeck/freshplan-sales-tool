package de.freshplan.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.entity.AuditSource;
import de.freshplan.domain.audit.repository.AuditRepository;
import de.freshplan.domain.audit.service.AuditService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * REST API tests for AuditResource
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@Tag("migrate")
@TestHTTPEndpoint(AuditResource.class)
class AuditResourceTest {

  @InjectMock AuditRepository auditRepository;

  @InjectMock AuditService auditService;

  private UUID testEntityId;
  private UUID testUserId;

  @BeforeEach
  void setUp() {
    testEntityId = UUID.randomUUID();
    testUserId = UUID.randomUUID();

    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  @Test
  @TestSecurity(
      user = "admin",
      roles = {"admin"})
  void testGetEntityAuditTrail() {
    // Given
    List<AuditEntry> mockEntries =
        List.of(
            createMockAuditEntry(AuditEventType.OPPORTUNITY_CREATED),
            createMockAuditEntry(AuditEventType.OPPORTUNITY_UPDATED));

    when(auditRepository.findByEntity("opportunity", testEntityId, 0, 50)).thenReturn(mockEntries);
    when(auditService.logAsync(any()))
        .thenReturn(CompletableFuture.completedFuture(UUID.randomUUID()));

    // When/Then
    given()
        .when()
        .get("/entity/opportunity/{entityId}", testEntityId)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("$", hasSize(2))
        .body("[0].eventType", equalTo("OPPORTUNITY_CREATED"))
        .body("[1].eventType", equalTo("OPPORTUNITY_UPDATED"));

    verify(auditRepository).findByEntity("opportunity", testEntityId, 0, 50);
    verify(auditService).logAsync(any());
  }

  @Test
  @TestSecurity(
      user = "user",
      roles = {"sales"})
  void testGetEntityAuditTrail_Forbidden() {
    // When/Then
    given().when().get("/entity/opportunity/{entityId}", testEntityId).then().statusCode(403);
  }

  @Test
  @TestSecurity(
      user = "admin",
      roles = {"admin"})
  void testSearchAuditTrail() {
    // Given
    List<AuditEntry> mockEntries =
        List.of(createMockAuditEntry(AuditEventType.OPPORTUNITY_CREATED));

    when(auditRepository.search(any())).thenReturn(mockEntries);

    // When/Then
    given()
        .queryParam("entityType", "opportunity")
        .queryParam("eventType", "OPPORTUNITY_CREATED")
        .queryParam("page", 0)
        .queryParam("size", 20)
        .when()
        .get("/search")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("$", hasSize(1))
        .body("[0].entityType", equalTo("opportunity"));
  }

  @Test
  @TestSecurity(
      user = "admin",
      roles = {"admin"})
  void testExportAuditTrailCsv() {
    // Given
    List<AuditEntry> mockEntries =
        List.of(
            createMockAuditEntry(AuditEventType.OPPORTUNITY_CREATED),
            createMockAuditEntry(AuditEventType.OPPORTUNITY_UPDATED));

    when(auditRepository.search(any())).thenReturn(mockEntries);
    when(auditService.logSync(any())).thenReturn(UUID.randomUUID());

    // When/Then
    given()
        .queryParam("format", "csv")
        .queryParam("entityType", "opportunity")
        .when()
        .get("/export")
        .then()
        .statusCode(200)
        .contentType("text/csv")
        .header("Content-Disposition", containsString("attachment; filename=\""));

    verify(auditService).logAsync(any());
  }

  @Test
  @TestSecurity(
      user = "admin",
      roles = {"admin"})
  void testExportAuditTrailJson() {
    // Given
    List<AuditEntry> mockEntries =
        List.of(createMockAuditEntry(AuditEventType.OPPORTUNITY_CREATED));

    when(auditRepository.search(any())).thenReturn(mockEntries);
    when(auditService.logSync(any())).thenReturn(UUID.randomUUID());

    // When/Then
    given()
        .queryParam("format", "json")
        .when()
        .get("/export")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .header("Content-Disposition", containsString("attachment; filename=\""))
        .body("data", hasSize(1))
        .body("metadata.recordCount", equalTo(1));

    verify(auditRepository).search(any());
    verify(auditService).logAsync(any());
  }

  @Test
  @TestSecurity(
      user = "admin",
      roles = {"admin"})
  void testGetStatistics() {
    // Given
    var mockStats =
        AuditRepository.AuditStatistics.builder()
            .totalEvents(100L)
            .uniqueUsers(10L)
            .uniqueEntities(50L)
            .failureCount(5L)
            .period(Instant.now().minusSeconds(86400), Instant.now())
            .build();

    when(auditRepository.getStatistics(any(), any())).thenReturn(mockStats);

    // When/Then
    given()
        .when()
        .get("/statistics")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("totalEvents", equalTo(100))
        .body("uniqueUsers", equalTo(10))
        .body("failureCount", equalTo(5));
  }

  @Test
  @TestSecurity(
      user = "admin",
      roles = {"admin", "security"})
  void testGetSecurityEvents() {
    // Given
    List<AuditEntry> mockEntries =
        List.of(
            createMockAuditEntry(AuditEventType.LOGIN_SUCCESS),
            createMockAuditEntry(AuditEventType.PERMISSION_DENIED));

    when(auditRepository.findSecurityEvents(any(), any())).thenReturn(mockEntries);

    // When/Then
    given()
        .queryParam("hours", 24)
        .when()
        .get("/security-events")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("$", hasSize(2))
        .body("[0].eventType", equalTo("LOGIN_SUCCESS"))
        .body("[1].eventType", equalTo("PERMISSION_DENIED"));
  }

  @Test
  @TestSecurity(
      user = "admin",
      roles = {"admin"})
  void testGetFailures() {
    // Given
    List<AuditEntry> mockEntries =
        List.of(
            createMockAuditEntry(AuditEventType.LOGIN_FAILURE),
            createMockAuditEntry(AuditEventType.API_ERROR));

    when(auditRepository.findFailures(any(), any())).thenReturn(mockEntries);

    // When/Then
    given()
        .queryParam("hours", 24)
        .when()
        .get("/failures")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("$", hasSize(2));
  }

  @Test
  @TestSecurity(
      user = "admin",
      roles = {"admin", "auditor"})
  void testVerifyIntegrity_Valid() {
    // Given
    when(auditRepository.verifyIntegrity(any(), any())).thenReturn(List.of());

    // When/Then
    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/verify-integrity")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("status", equalTo("valid"))
        .body("message", containsString("verified"));
  }

  @Test
  @TestSecurity(
      user = "admin",
      roles = {"admin", "auditor"})
  void testVerifyIntegrity_Compromised() {
    // Given
    var issue =
        new AuditRepository.AuditIntegrityIssue(
            UUID.randomUUID(), "Hash chain broken", "expected-hash", "actual-hash");

    when(auditRepository.verifyIntegrity(any(), any())).thenReturn(List.of(issue));

    // When/Then
    given()
        .contentType(ContentType.JSON)
        .when()
        .post("/verify-integrity")
        .then()
        .statusCode(409)
        .contentType(ContentType.JSON)
        .body("status", equalTo("compromised"))
        .body("issues", hasSize(1));
  }

  @Test
  @TestSecurity(
      user = "manager",
      roles = {"manager"})
  void testPaginationParameters() {
    // Given
    when(auditRepository.findByEntity(any(), any(), eq(2), eq(25))).thenReturn(List.of());

    // When/Then
    given()
        .queryParam("page", 2)
        .queryParam("size", 25)
        .when()
        .get("/entity/opportunity/{entityId}", testEntityId)
        .then()
        .statusCode(200);

    verify(auditRepository).findByEntity("opportunity", testEntityId, 2, 25);
  }

  // Helper methods

  private AuditEntry createMockAuditEntry(AuditEventType eventType) {
    return AuditEntry.builder()
        .id(UUID.randomUUID())
        .timestamp(Instant.now())
        .eventType(eventType)
        .entityType("opportunity")
        .entityId(testEntityId)
        .userId(testUserId)
        .userName("Test User")
        .userRole("admin")
        .source(AuditSource.UI)
        .dataHash("mock-hash")
        .schemaVersion(1)
        .build();
  }
}
