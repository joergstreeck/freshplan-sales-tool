package de.freshplan.api.resources;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.domain.opportunity.service.dto.CreateOpportunityRequest;
import de.freshplan.domain.opportunity.service.dto.UpdateOpportunityRequest;
import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Umfassende Integration Tests für OpportunityResource - API Functionality
 *
 * <p>Tests decken ab: - REST API Endpoints - HTTP Status Codes - Request/Response Validation -
 * Content-Type Handling - Error Responses - Security Integration
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@TestSecurity(authorizationEnabled = false)
public class OpportunityResourceIntegrationTest {

  @Inject OpportunityRepository opportunityRepository;

  @Inject CustomerRepository customerRepository;

  @Inject UserRepository userRepository;

  private Customer testCustomer;
  private User testUser;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean up existing test data
    opportunityRepository.deleteAll();

    // Create test customer
    testCustomer = getOrCreateCustomer("Test Company", "test@example.com");

    // Create test user
    testUser = getOrCreateUser("testuser", "Test", "User");
  }

  @Nested
  @DisplayName("GET /api/opportunities - List Opportunities")
  class ListOpportunitiesTests {

    @Test
    @DisplayName("Should return empty list when no opportunities exist")
    void getOpportunities_noData_shouldReturnEmptyList() {
      given()
          .when()
          .get("/api/opportunities")
          .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("", hasSize(0));
    }

    @Test
    @DisplayName("Should return all opportunities with correct structure")
    void getOpportunities_withData_shouldReturnCorrectStructure() {
      // Arrange
      createTestOpportunity("Test Opportunity 1", OpportunityStage.NEW_LEAD);
      createTestOpportunity("Test Opportunity 2", OpportunityStage.PROPOSAL);

      // Act & Assert
      given()
          .when()
          .get("/api/opportunities")
          .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("", hasSize(2))
          .body("[0].name", notNullValue())
          .body("[0].stage", notNullValue())
          .body("[0].id", notNullValue())
          .body("[0].createdAt", notNullValue())
          .body("[0].stageChangedAt", notNullValue());
    }

    @Test
    @DisplayName("Should filter opportunities by stage")
    void getOpportunities_filterByStage_shouldReturnFiltered() {
      // Arrange
      createTestOpportunity("New Lead", OpportunityStage.NEW_LEAD);
      createTestOpportunity("Proposal", OpportunityStage.PROPOSAL);
      createTestOpportunity("Another New Lead", OpportunityStage.NEW_LEAD);

      // Act & Assert
      given()
          .queryParam("stage", "NEW_LEAD")
          .when()
          .get("/api/opportunities")
          .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("", hasSize(2))
          .body("[0].stage", equalTo("NEW_LEAD"))
          .body("[1].stage", equalTo("NEW_LEAD"));
    }

    @Test
    @DisplayName("Should handle invalid stage parameter gracefully")
    void getOpportunities_invalidStage_shouldReturnBadRequest() {
      given()
          .queryParam("stage", "INVALID_STAGE")
          .when()
          .get("/api/opportunities")
          .then()
          .statusCode(400)
          .contentType(ContentType.JSON);
    }
  }

  @Nested
  @DisplayName("GET /api/opportunities/{id} - Get Single Opportunity")
  class GetSingleOpportunityTests {

    @Test
    @DisplayName("Should return opportunity by valid ID")
    void getOpportunity_validId_shouldReturnOpportunity() {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", OpportunityStage.PROPOSAL);

      // Act & Assert
      given()
          .pathParam("id", opportunity.getId())
          .when()
          .get("/api/opportunities/{id}")
          .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("id", equalTo(opportunity.getId().toString()))
          .body("name", equalTo("Test Opportunity"))
          .body("stage", equalTo("PROPOSAL"))
          .body("probability", equalTo(60)); // Default for PROPOSAL
    }

    @Test
    @DisplayName("Should return 404 for non-existent ID")
    void getOpportunity_nonExistentId_shouldReturn404() {
      // Arrange
      var nonExistentId = UUID.randomUUID();

      // Act & Assert
      given()
          .pathParam("id", nonExistentId)
          .when()
          .get("/api/opportunities/{id}")
          .then()
          .statusCode(404)
          .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("Should return 400 for invalid ID format")
    void getOpportunity_invalidIdFormat_shouldReturn400() {
      given()
          .pathParam("id", "not-a-valid-uuid")
          .when()
          .get("/api/opportunities/{id}")
          .then()
          .statusCode(400);
    }
  }

  @Nested
  @DisplayName("POST /api/opportunities - Create Opportunity")
  class CreateOpportunityTests {

    @Test
    @DisplayName("Should create opportunity with valid request")
    void createOpportunity_validRequest_shouldReturn201() {
      // Arrange
      var request =
          CreateOpportunityRequest.builder()
              .name("New Test Opportunity")
              .description("Test Description")
              .customerId(testCustomer.getId())
              .assignedTo(testUser.getId())
              .expectedValue(BigDecimal.valueOf(25000))
              .expectedCloseDate(LocalDate.now().plusDays(30))
              .build();

      // Act & Assert
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/opportunities")
          .then()
          .statusCode(201)
          .contentType(ContentType.JSON)
          .body("name", equalTo("New Test Opportunity"))
          .body("description", equalTo("Test Description"))
          .body("stage", equalTo("NEW_LEAD"))
          .body("expectedValue", equalTo(25000))
          .body("probability", equalTo(10))
          .body("id", notNullValue())
          .body("createdAt", notNullValue());
    }

    @Test
    @DisplayName("Should create opportunity with minimal required fields")
    void createOpportunity_minimalRequest_shouldReturn201() {
      // Arrange
      var request =
          CreateOpportunityRequest.builder()
              .name("Minimal Opportunity")
              .customerId(testCustomer.getId())
              .build();

      // Act & Assert
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/opportunities")
          .then()
          .statusCode(201)
          .contentType(ContentType.JSON)
          .body("name", equalTo("Minimal Opportunity"))
          .body("stage", equalTo("NEW_LEAD"))
          .body("description", nullValue())
          .body("assignedTo", nullValue())
          .body("expectedValue", nullValue());
    }

    @Test
    @DisplayName("Should return 400 for missing required fields")
    void createOpportunity_missingRequiredFields_shouldReturn400() {
      // Test missing name
      var requestWithoutName =
          CreateOpportunityRequest.builder().customerId(testCustomer.getId()).build();

      given()
          .contentType(ContentType.JSON)
          .body(requestWithoutName)
          .when()
          .post("/api/opportunities")
          .then()
          .statusCode(400)
          .contentType(ContentType.JSON);

      // Test missing customer ID
      var requestWithoutCustomer =
          CreateOpportunityRequest.builder().name("Test Opportunity").build();

      given()
          .contentType(ContentType.JSON)
          .body(requestWithoutCustomer)
          .when()
          .post("/api/opportunities")
          .then()
          .statusCode(400)
          .contentType(ContentType.JSON);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    @DisplayName("Should return 400 for blank name")
    void createOpportunity_blankName_shouldReturn400(String blankName) {
      // Arrange
      var request =
          CreateOpportunityRequest.builder()
              .name(blankName)
              .customerId(testCustomer.getId())
              .build();

      // Act & Assert
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/opportunities")
          .then()
          .statusCode(400)
          .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("Should return 400 for negative expected value")
    void createOpportunity_negativeValue_shouldReturn400() {
      // Arrange
      var request =
          CreateOpportunityRequest.builder()
              .name("Test Opportunity")
              .customerId(testCustomer.getId())
              .expectedValue(BigDecimal.valueOf(-1000))
              .build();

      // Act & Assert
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/opportunities")
          .then()
          .statusCode(400)
          .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("Should return 400 for past expected close date")
    void createOpportunity_pastCloseDate_shouldReturn400() {
      // Arrange
      var request =
          CreateOpportunityRequest.builder()
              .name("Test Opportunity")
              .customerId(testCustomer.getId())
              .expectedCloseDate(LocalDate.now().minusDays(1))
              .build();

      // Act & Assert
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/opportunities")
          .then()
          .statusCode(400)
          .contentType(ContentType.JSON);
    }
  }

  @Nested
  @DisplayName("PUT /api/opportunities/{id} - Update Opportunity")
  class UpdateOpportunityTests {

    @Test
    @DisplayName("Should update opportunity with valid request")
    void updateOpportunity_validRequest_shouldReturn200() {
      // Arrange
      var opportunity = createTestOpportunity("Original Name", OpportunityStage.NEW_LEAD);
      var updateRequest =
          UpdateOpportunityRequest.builder()
              .name("Updated Name")
              .description("Updated Description")
              .expectedValue(BigDecimal.valueOf(35000))
              .expectedCloseDate(LocalDate.now().plusDays(60))
              .build();

      // Act & Assert
      given()
          .contentType(ContentType.JSON)
          .pathParam("id", opportunity.getId())
          .body(updateRequest)
          .when()
          .put("/api/opportunities/{id}")
          .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("name", equalTo("Updated Name"))
          .body("description", equalTo("Updated Description"))
          .body("expectedValue", equalTo(35000))
          .body("updatedAt", notNullValue());
    }

    @Test
    @DisplayName("Should handle partial updates")
    void updateOpportunity_partialUpdate_shouldReturn200() {
      // Arrange
      var opportunity = createTestOpportunity("Original Name", OpportunityStage.NEW_LEAD);
      var updateRequest = UpdateOpportunityRequest.builder().name("New Name Only").build();

      // Act & Assert
      given()
          .contentType(ContentType.JSON)
          .pathParam("id", opportunity.getId())
          .body(updateRequest)
          .when()
          .put("/api/opportunities/{id}")
          .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("name", equalTo("New Name Only"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent ID")
    void updateOpportunity_nonExistentId_shouldReturn404() {
      // Arrange
      var nonExistentId = UUID.randomUUID();
      var updateRequest = UpdateOpportunityRequest.builder().name("Updated Name").build();

      // Act & Assert
      given()
          .contentType(ContentType.JSON)
          .pathParam("id", nonExistentId)
          .body(updateRequest)
          .when()
          .put("/api/opportunities/{id}")
          .then()
          .statusCode(404)
          .contentType(ContentType.JSON);
    }
  }

  @Nested
  @DisplayName("DELETE /api/opportunities/{id} - Delete Opportunity")
  class DeleteOpportunityTests {

    @Test
    @DisplayName("Should delete existing opportunity")
    void deleteOpportunity_existingId_shouldReturn204() {
      // Arrange
      var opportunity = createTestOpportunity("To Be Deleted", OpportunityStage.NEW_LEAD);

      // Act & Assert
      given()
          .pathParam("id", opportunity.getId())
          .when()
          .delete("/api/opportunities/{id}")
          .then()
          .statusCode(204);

      // Verify deletion
      given()
          .pathParam("id", opportunity.getId())
          .when()
          .get("/api/opportunities/{id}")
          .then()
          .statusCode(404);
    }

    @Test
    @DisplayName("Should return 404 for non-existent ID")
    void deleteOpportunity_nonExistentId_shouldReturn404() {
      // Arrange
      var nonExistentId = UUID.randomUUID();

      // Act & Assert
      given()
          .pathParam("id", nonExistentId)
          .when()
          .delete("/api/opportunities/{id}")
          .then()
          .statusCode(404)
          .contentType(ContentType.JSON);
    }
  }

  @Nested
  @DisplayName("PUT /api/opportunities/{id}/stage - Change Stage")
  class ChangeStageTests {

    @Test
    @DisplayName("Should change stage with valid request")
    void changeStage_validStage_shouldReturn200() {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", OpportunityStage.NEW_LEAD);

      // Act & Assert
      given()
          .contentType(ContentType.JSON)
          .pathParam("id", opportunity.getId())
          .body("{\"stage\": \"QUALIFICATION\"}")
          .when()
          .put("/api/opportunities/{id}/stage")
          .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("stage", equalTo("QUALIFICATION"))
          .body("probability", equalTo(25))
          .body("stageChangedAt", notNullValue());
    }

    @Test
    @DisplayName("Should return 400 for invalid stage")
    void changeStage_invalidStage_shouldReturn400() {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", OpportunityStage.NEW_LEAD);

      // Act & Assert
      given()
          .contentType(ContentType.JSON)
          .pathParam("id", opportunity.getId())
          .body("{\"stage\": \"INVALID_STAGE\"}")
          .when()
          .put("/api/opportunities/{id}/stage")
          .then()
          .statusCode(400)
          .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("Should return 404 for non-existent opportunity")
    void changeStage_nonExistentId_shouldReturn404() {
      // Arrange
      var nonExistentId = UUID.randomUUID();

      // Act & Assert
      given()
          .contentType(ContentType.JSON)
          .pathParam("id", nonExistentId)
          .body("{\"stage\": \"QUALIFICATION\"}")
          .when()
          .put("/api/opportunities/{id}/stage")
          .then()
          .statusCode(404)
          .contentType(ContentType.JSON);
    }
  }

  @Nested
  @DisplayName("GET /api/opportunities/analytics/forecast - Forecast Analytics")
  class ForecastAnalyticsTests {

    @Test
    @DisplayName("Should return forecast calculation")
    void getForecast_shouldReturnCorrectValue() {
      // Arrange
      createOpportunityWithValue("Opp1", BigDecimal.valueOf(10000), 50);
      createOpportunityWithValue("Opp2", BigDecimal.valueOf(20000), 60);

      // Act & Assert
      given()
          .when()
          .get("/api/opportunities/analytics/forecast")
          .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("forecast", equalTo(17000)); // 5000 + 12000
    }

    @Test
    @DisplayName("Should return zero forecast when no opportunities exist")
    void getForecast_noOpportunities_shouldReturnZero() {
      given()
          .when()
          .get("/api/opportunities/analytics/forecast")
          .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("forecast", equalTo(0));
    }
  }

  @Nested
  @DisplayName("GET /api/opportunities/analytics/stage-distribution - Stage Distribution")
  class StageDistributionTests {

    @Test
    @DisplayName("Should return stage distribution")
    void getStageDistribution_shouldReturnCorrectDistribution() {
      // Arrange
      createTestOpportunity("New1", OpportunityStage.NEW_LEAD);
      createTestOpportunity("New2", OpportunityStage.NEW_LEAD);
      createTestOpportunity("Proposal1", OpportunityStage.PROPOSAL);

      // Act & Assert
      given()
          .when()
          .get("/api/opportunities/analytics/stage-distribution")
          .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("NEW_LEAD", equalTo(2))
          .body("PROPOSAL", equalTo(1));
    }
  }

  @Nested
  @DisplayName("Content-Type and Error Handling")
  class ContentTypeAndErrorTests {

    @Test
    @DisplayName("Should return 415 for unsupported content type")
    void createOpportunity_unsupportedContentType_shouldReturn415() {
      given()
          .contentType("text/plain")
          .body("invalid content")
          .when()
          .post("/api/opportunities")
          .then()
          .statusCode(415);
    }

    @Test
    @DisplayName("Should return 400 for malformed JSON")
    void createOpportunity_malformedJson_shouldReturn400() {
      given()
          .contentType(ContentType.JSON)
          .body("{ invalid json")
          .when()
          .post("/api/opportunities")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Should include error details in response")
    void createOpportunity_validationError_shouldIncludeErrorDetails() {
      // Arrange
      var requestWithoutName =
          CreateOpportunityRequest.builder().customerId(testCustomer.getId()).build();

      // Act & Assert
      given()
          .contentType(ContentType.JSON)
          .body(requestWithoutName)
          .when()
          .post("/api/opportunities")
          .then()
          .statusCode(400)
          .contentType(ContentType.JSON)
          .body("message", notNullValue());
    }
  }

  // Helper methods

  @Transactional
  Customer getOrCreateCustomer(String companyName, String email) {
    var existingCustomer = customerRepository.find("companyName", companyName).firstResult();
    if (existingCustomer != null) {
      return existingCustomer;
    }

    // Create minimal test customer with all required fields
    var customer = new Customer();
    customer.setCompanyName(companyName);

    // Set required NOT NULL fields to avoid constraint violations
    customer.setCustomerNumber("TEST-" + System.currentTimeMillis()); // Unique customer number
    customer.setIsTestData(true); // Mark as test data
    customer.setIsDeleted(false); // Not deleted
    customer.setCreatedAt(java.time.LocalDateTime.now()); // Set created timestamp
    customer.setCreatedBy("test-system"); // Set created by

    customerRepository.persist(customer);
    return customer;
  }

  @Transactional
  User getOrCreateUser(String username, String firstName, String lastName) {
    var existingUser = userRepository.find("username", username).firstResult();
    if (existingUser != null) {
      return existingUser;
    }

    // Cannot create User directly - find existing or fail
    throw new UnsupportedOperationException(
        "Cannot create User directly - use existing test users");
  }

  @Transactional
  Opportunity createTestOpportunity(String name, OpportunityStage stage) {
    var opportunity = new Opportunity();
    opportunity.setName(name);
    opportunity.setStage(stage);
    opportunity.setCustomer(testCustomer);
    opportunity.setAssignedTo(testUser);
    opportunityRepository.persist(opportunity);
    return opportunity;
  }

  @Transactional
  Opportunity createOpportunityWithValue(
      String name, BigDecimal expectedValue, Integer probability) {
    var opportunity = createTestOpportunity(name, OpportunityStage.PROPOSAL);
    opportunity.setExpectedValue(expectedValue);
    opportunity.setProbability(probability);
    opportunityRepository.persist(opportunity);
    return opportunity;
  }
}
