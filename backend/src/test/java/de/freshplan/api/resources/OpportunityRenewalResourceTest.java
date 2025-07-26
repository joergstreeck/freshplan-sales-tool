package de.freshplan.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;

import de.freshplan.domain.opportunity.service.dto.CreateOpportunityRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Integration Tests für RENEWAL Stage API Endpoints
 *
 * <p>Testet die REST API für Contract Renewal Workflows über HTTP Calls. Prüft Request/Response
 * Formate, Status Codes und Business Logic Integration.
 */
@QuarkusTest
@TestSecurity(
    user = "ci-test-user",
    roles = {"admin", "manager", "sales"})
@Execution(ExecutionMode.SAME_THREAD)
class OpportunityRenewalResourceTest {

  /** Helper method to move an opportunity through stages to reach CLOSED_WON */
  private void moveOpportunityToClosedWon(String opportunityId) {
    // NEW_LEAD → QUALIFICATION
    given()
        .queryParam("reason", "Lead qualified")
        .when()
        .put("/api/opportunities/{id}/stage/{stage}", opportunityId, "QUALIFICATION")
        .then()
        .statusCode(200);

    // QUALIFICATION → NEEDS_ANALYSIS
    given()
        .queryParam("reason", "Analyzing needs")
        .when()
        .put("/api/opportunities/{id}/stage/{stage}", opportunityId, "NEEDS_ANALYSIS")
        .then()
        .statusCode(200);

    // NEEDS_ANALYSIS → PROPOSAL
    given()
        .queryParam("reason", "Proposal created")
        .when()
        .put("/api/opportunities/{id}/stage/{stage}", opportunityId, "PROPOSAL")
        .then()
        .statusCode(200);

    // PROPOSAL → NEGOTIATION
    given()
        .queryParam("reason", "In negotiation")
        .when()
        .put("/api/opportunities/{id}/stage/{stage}", opportunityId, "NEGOTIATION")
        .then()
        .statusCode(200);

    // NEGOTIATION → CLOSED_WON
    given()
        .queryParam("reason", "Deal successfully closed")
        .when()
        .put("/api/opportunities/{id}/stage/{stage}", opportunityId, "CLOSED_WON")
        .then()
        .statusCode(200);
  }

  @Nested
  @DisplayName("RENEWAL Stage Creation and Management")
  class RenewalStageManagement {

    @Test
    @DisplayName("POST /api/opportunities - Create basic opportunity for later RENEWAL")
    void shouldCreateOpportunityForRenewal() {
      // Arrange
      CreateOpportunityRequest request = new CreateOpportunityRequest();
      request.setName("Hotel Adler - Potential Renewal");
      request.setDescription("Annual contract opportunity for Hotel Adler");
      request.setExpectedValue(new BigDecimal("45000.00"));
      request.setCustomerId(UUID.randomUUID());
      request.setAssignedTo(UUID.randomUUID());

      // Act & Assert
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/opportunities")
          .then()
          .statusCode(201)
          .body("name", equalTo("Hotel Adler - Potential Renewal"))
          .body("expectedValue", equalTo(45000.0f))
          .body("id", notNullValue())
          .body("createdAt", notNullValue());
    }

    @Test
    @DisplayName("PUT /api/opportunities/{id}/stage/RENEWAL - Change stage to RENEWAL")
    void shouldChangeStageToRenewal() {
      // Arrange - Create opportunity in CLOSED_WON first
      CreateOpportunityRequest createRequest = new CreateOpportunityRequest();
      createRequest.setName("Restaurant Schmidt - Won Deal");
      createRequest.setExpectedValue(new BigDecimal("25000.00"));
      createRequest.setCustomerId(UUID.randomUUID());
      createRequest.setAssignedTo(UUID.randomUUID());

      String opportunityId =
          given()
              .contentType(ContentType.JSON)
              .body(createRequest)
              .when()
              .post("/api/opportunities")
              .then()
              .statusCode(201)
              .extract()
              .path("id");

      // Move through stages to reach CLOSED_WON
      moveOpportunityToClosedWon(opportunityId);

      // Now change to RENEWAL
      given()
          .queryParam("reason", "Customer contract is expiring in 90 days")
          .when()
          .put("/api/opportunities/{id}/stage/{stage}", opportunityId, "RENEWAL")
          .then()
          .statusCode(200)
          .body("id", equalTo(opportunityId))
          .body("stage", equalTo("RENEWAL"))
          .body("probability", equalTo(75)) // Should update to RENEWAL default
          .body("name", equalTo("Restaurant Schmidt - Won Deal")) // Preserve other fields
          .body("expectedValue", equalTo(25000.0f))
          .body("updatedAt", notNullValue());
    }

    @Test
    @DisplayName("PUT /api/opportunities/{id}/stage/CLOSED_WON - RENEWAL to CLOSED_WON transition")
    void shouldTransitionFromRenewalToClosedWon() {
      // Arrange - Create opportunity and move to RENEWAL
      CreateOpportunityRequest createRequest = new CreateOpportunityRequest();
      createRequest.setName("Catering Müller - Renewal Process");
      createRequest.setExpectedValue(new BigDecimal("18000.00"));
      createRequest.setCustomerId(UUID.randomUUID());
      createRequest.setAssignedTo(UUID.randomUUID());

      String opportunityId =
          given()
              .contentType(ContentType.JSON)
              .body(createRequest)
              .when()
              .post("/api/opportunities")
              .then()
              .statusCode(201)
              .extract()
              .path("id");

      // First move to CLOSED_WON through all required stages
      moveOpportunityToClosedWon(opportunityId);

      given()
          .queryParam("reason", "Starting renewal process")
          .when()
          .put("/api/opportunities/{id}/stage/{stage}", opportunityId, "RENEWAL")
          .then()
          .statusCode(200);

      // Now test successful renewal
      given()
          .queryParam("reason", "Contract renewal successfully completed")
          .when()
          .put("/api/opportunities/{id}/stage/{stage}", opportunityId, "CLOSED_WON")
          .then()
          .statusCode(200)
          .body("stage", equalTo("CLOSED_WON"))
          .body("probability", equalTo(100))
          .body("updatedAt", notNullValue());
    }

    @Test
    @DisplayName(
        "PUT /api/opportunities/{id}/stage/CLOSED_LOST - RENEWAL to CLOSED_LOST transition")
    void shouldTransitionFromRenewalToClosedLost() {
      // Arrange - Create opportunity and move to RENEWAL
      CreateOpportunityRequest createRequest = new CreateOpportunityRequest();
      createRequest.setName("Hotel Bergblick - Renewal Process");
      createRequest.setExpectedValue(new BigDecimal("35000.00"));
      createRequest.setCustomerId(UUID.randomUUID());
      createRequest.setAssignedTo(UUID.randomUUID());

      String opportunityId =
          given()
              .contentType(ContentType.JSON)
              .body(createRequest)
              .when()
              .post("/api/opportunities")
              .then()
              .statusCode(201)
              .extract()
              .path("id");

      // Move to CLOSED_WON through all required stages
      moveOpportunityToClosedWon(opportunityId);

      given()
          .queryParam("reason", "Starting renewal process")
          .when()
          .put("/api/opportunities/{id}/stage/{stage}", opportunityId, "RENEWAL")
          .then()
          .statusCode(200);

      // Test failed renewal
      given()
          .queryParam("reason", "Customer decided not to renew - found cheaper alternative")
          .when()
          .put("/api/opportunities/{id}/stage/{stage}", opportunityId, "CLOSED_LOST")
          .then()
          .statusCode(200)
          .body("stage", equalTo("CLOSED_LOST"))
          .body("probability", equalTo(0));
    }

    @Test
    @DisplayName("PUT /api/opportunities/{id}/stage/RENEWAL - Invalid transition to RENEWAL")
    void shouldRejectInvalidTransitionToRenewal() {
      // Arrange - Create opportunity in NEW_LEAD stage
      CreateOpportunityRequest createRequest = new CreateOpportunityRequest();
      createRequest.setName("New Customer Lead");
      createRequest.setExpectedValue(new BigDecimal("15000.00"));
      createRequest.setCustomerId(UUID.randomUUID());
      createRequest.setAssignedTo(UUID.randomUUID());

      String opportunityId =
          given()
              .contentType(ContentType.JSON)
              .body(createRequest)
              .when()
              .post("/api/opportunities")
              .then()
              .statusCode(201)
              .extract()
              .path("id");

      // Try invalid transition NEW_LEAD → RENEWAL (should fail)
      given()
          .queryParam("reason", "Invalid transition attempt")
          .when()
          .put("/api/opportunities/{id}/stage/{stage}", opportunityId, "RENEWAL")
          .then()
          .statusCode(400)
          .body("message", equalTo("Invalid stage transition from NEW_LEAD to RENEWAL"));
    }
  }

  @Nested
  @DisplayName("RENEWAL Stage Query Operations")
  class RenewalQueryOperations {

    @Test
    @DisplayName("GET /api/opportunities/stage/RENEWAL - Filter by RENEWAL stage")
    void shouldFilterOpportunitiesByRenewalStage() {
      // Arrange - Create opportunities in different stages
      CreateOpportunityRequest renewal1 = new CreateOpportunityRequest();
      renewal1.setName("Renewal Opportunity 1");
      renewal1.setExpectedValue(new BigDecimal("30000.00"));
      renewal1.setCustomerId(UUID.randomUUID());
      renewal1.setAssignedTo(UUID.randomUUID());

      CreateOpportunityRequest renewal2 = new CreateOpportunityRequest();
      renewal2.setName("Renewal Opportunity 2");
      renewal2.setExpectedValue(new BigDecimal("45000.00"));
      renewal2.setCustomerId(UUID.randomUUID());
      renewal2.setAssignedTo(UUID.randomUUID());

      CreateOpportunityRequest proposal = new CreateOpportunityRequest();
      proposal.setName("Proposal Opportunity");
      proposal.setExpectedValue(new BigDecimal("20000.00"));
      proposal.setCustomerId(UUID.randomUUID());
      proposal.setAssignedTo(UUID.randomUUID());

      // Create all opportunities
      String renewalId1 =
          given()
              .contentType(ContentType.JSON)
              .body(renewal1)
              .post("/api/opportunities")
              .then()
              .statusCode(201)
              .extract()
              .path("id");
      String renewalId2 =
          given()
              .contentType(ContentType.JSON)
              .body(renewal2)
              .post("/api/opportunities")
              .then()
              .statusCode(201)
              .extract()
              .path("id");
      given()
          .contentType(ContentType.JSON)
          .body(proposal)
          .post("/api/opportunities")
          .then()
          .statusCode(201);

      // Move first two to CLOSED_WON then RENEWAL
      moveOpportunityToClosedWon(renewalId1);
      moveOpportunityToClosedWon(renewalId2);
      given()
          .queryParam("reason", "Starting renewal")
          .put("/api/opportunities/{id}/stage/{stage}", renewalId1, "RENEWAL")
          .then()
          .statusCode(200);
      given()
          .queryParam("reason", "Starting renewal")
          .put("/api/opportunities/{id}/stage/{stage}", renewalId2, "RENEWAL")
          .then()
          .statusCode(200);

      // Act & Assert - Filter by RENEWAL stage
      given()
          .when()
          .get("/api/opportunities/stage/{stage}", "RENEWAL")
          .then()
          .statusCode(200)
          .body("", hasSize(2)) // Only RENEWAL opportunities
          .body("every { it.stage == 'RENEWAL' }", equalTo(true))
          .body("find { it.name == 'Renewal Opportunity 1' }", notNullValue())
          .body("find { it.name == 'Renewal Opportunity 2' }", notNullValue());
    }

    @Test
    @DisplayName("GET /api/opportunities/{id} - Get RENEWAL opportunity by ID")
    void shouldGetRenewalOpportunityById() {
      // Arrange
      CreateOpportunityRequest request = new CreateOpportunityRequest();
      request.setName("Single RENEWAL Test");
      request.setDescription("Detailed description for renewal opportunity");
      request.setExpectedValue(new BigDecimal("55000.00"));
      request.setCustomerId(UUID.randomUUID());
      request.setAssignedTo(UUID.randomUUID());

      String opportunityId =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .when()
              .post("/api/opportunities")
              .then()
              .statusCode(201)
              .extract()
              .path("id");

      // Move to RENEWAL
      moveOpportunityToClosedWon(opportunityId);
      given()
          .queryParam("reason", "Starting renewal")
          .put("/api/opportunities/{id}/stage/{stage}", opportunityId, "RENEWAL")
          .then()
          .statusCode(200);

      // Act & Assert
      given()
          .pathParam("id", opportunityId)
          .when()
          .get("/api/opportunities/{id}")
          .then()
          .statusCode(200)
          .body("id", equalTo(opportunityId))
          .body("name", equalTo("Single RENEWAL Test"))
          .body("description", equalTo("Detailed description for renewal opportunity"))
          .body("stage", equalTo("RENEWAL"))
          .body("probability", equalTo(75))
          .body("expectedValue", equalTo(55000.0f));
    }
  }

  @Nested
  @DisplayName("RENEWAL Stage Error Handling")
  class RenewalErrorHandling {

    @Test
    @DisplayName("Handle non-existent opportunity ID for RENEWAL update")
    void shouldHandleNonExistentOpportunityForRenewal() {
      // Arrange
      String nonExistentId = "00000000-0000-0000-0000-000000000000";

      // Act & Assert
      given()
          .queryParam("reason", "Testing non-existent ID")
          .when()
          .put("/api/opportunities/{id}/stage/{stage}", nonExistentId, "RENEWAL")
          .then()
          .statusCode(404)
          .body(
              "message",
              equalTo("Opportunity not found with ID: 00000000-0000-0000-0000-000000000000"));
    }

    @Test
    @DisplayName("Handle invalid stage parameter")
    void shouldHandleInvalidStageParameter() {
      // Arrange
      CreateOpportunityRequest request = new CreateOpportunityRequest();
      request.setName("Test Opportunity");
      request.setExpectedValue(new BigDecimal("10000.00"));
      request.setCustomerId(UUID.randomUUID());
      request.setAssignedTo(UUID.randomUUID());

      String opportunityId =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .when()
              .post("/api/opportunities")
              .then()
              .statusCode(201)
              .extract()
              .path("id");

      // Act & Assert - Try invalid stage
      // Note: JAX-RS returns 404 for invalid enum values in path parameters
      given()
          .queryParam("reason", "Testing invalid stage")
          .when()
          .put("/api/opportunities/{id}/stage/{stage}", opportunityId, "INVALID_STAGE")
          .then()
          .statusCode(404);
    }
  }
}
