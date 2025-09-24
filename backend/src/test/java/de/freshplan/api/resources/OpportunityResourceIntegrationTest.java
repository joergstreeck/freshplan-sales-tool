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
import de.freshplan.test.builders.CustomerBuilder;
import de.freshplan.test.builders.OpportunityBuilder;
import de.freshplan.test.builders.UserTestDataFactory;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
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
@Tag("core")
public class OpportunityResourceIntegrationTest {

  @Inject OpportunityRepository opportunityRepository;

  @Inject CustomerRepository customerRepository;

  @Inject UserRepository userRepository;

  @Inject EntityManager entityManager;

  @Inject CustomerBuilder customerBuilder;

  @Inject OpportunityBuilder opportunityBuilder;

  private Customer testCustomer;
  private User testUser;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean up existing test data - delete activities first due to foreign key constraints
    entityManager.createNativeQuery("DELETE FROM opportunity_activities").executeUpdate();
    opportunityRepository.deleteAll();

    // Create test customer
    testCustomer = getOrCreateCustomer("Test Company", "test@example.com");

    // Get or create test user for CI environment
    testUser = userRepository.find("username", "testuser").firstResult();
    if (testUser == null) {
      // Create a test user if none exists (happens in CI) using TestDataFactory
      testUser =
          UserTestDataFactory.builder()
              .withUsername("testuser")
              .withFirstName("Test")
              .withLastName("User")
              .withEmail("testuser@test.com")
              .build();
      testUser.setRoles(Arrays.asList("admin", "manager", "sales"));
      userRepository.persist(testUser);
    }
  }

  @Nested
  @Tag("core")
  @DisplayName("GET /api/opportunities - List Opportunities")
  class ListOpportunitiesTests {

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
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
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
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
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("Should return all opportunities without stage filter")
    void getOpportunities_withStageParam_shouldReturnAll() {
      // Arrange
      createTestOpportunity("New Lead", OpportunityStage.NEW_LEAD);
      createTestOpportunity("Proposal", OpportunityStage.PROPOSAL);
      createTestOpportunity("Another New Lead", OpportunityStage.NEW_LEAD);

      // Act & Assert - stage filtering not implemented in main endpoint
      given()
          .queryParam("stage", "NEW_LEAD")
          .when()
          .get("/api/opportunities")
          .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("", hasSize(3)); // Returns all, no filtering
    }

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("Should ignore invalid stage parameter")
    void getOpportunities_invalidStage_shouldIgnoreAndReturnAll() {
      // Act & Assert - Invalid stage is ignored
      given()
          .queryParam("stage", "INVALID_STAGE")
          .when()
          .get("/api/opportunities")
          .then()
          .statusCode(200)
          .contentType(ContentType.JSON);
    }
  }

  @Nested
  @Tag("core")
  @DisplayName("GET /api/opportunities/{id} - Get Single Opportunity")
  class GetSingleOpportunityTests {

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
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
          .body(
              "name",
              containsString(
                  "Test Opportunity")) // Changed to containsString to handle [TEST] prefix
          .body("stage", equalTo("PROPOSAL"))
          .body("probability", equalTo(60)); // Default for PROPOSAL
    }

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
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
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("Should return 404 for invalid ID format")
    void getOpportunity_invalidIdFormat_shouldReturn404() {
      // JAX-RS returns 404 when it cannot convert path parameters
      given()
          .pathParam("id", "not-a-valid-uuid")
          .when()
          .get("/api/opportunities/{id}")
          .then()
          .statusCode(404);
    }
  }

  @Nested
  @Tag("core")
  @DisplayName("POST /api/opportunities - Create Opportunity")
  class CreateOpportunityTests {

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
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
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
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
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
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
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
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
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
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
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
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
  @Tag("core")
  @DisplayName("PUT /api/opportunities/{id} - Update Opportunity")
  class UpdateOpportunityTests {

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
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
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
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
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
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
  @Tag("core")
  @DisplayName("DELETE /api/opportunities/{id} - Delete Opportunity")
  class DeleteOpportunityTests {

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("Should return NOT_IMPLEMENTED for delete")
    void deleteOpportunity_existingId_shouldReturn501() {
      // Arrange
      var opportunity = createTestOpportunity("To Be Deleted", OpportunityStage.NEW_LEAD);

      // Act & Assert - DELETE is not implemented yet
      given()
          .pathParam("id", opportunity.getId())
          .when()
          .delete("/api/opportunities/{id}")
          .then()
          .statusCode(501)
          .body(containsString("not yet implemented"));
    }

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("Should return NOT_IMPLEMENTED for non-existent ID delete")
    void deleteOpportunity_nonExistentId_shouldReturn501() {
      // Arrange
      var nonExistentId = UUID.randomUUID();

      // Act & Assert - DELETE is not implemented yet
      given()
          .pathParam("id", nonExistentId)
          .when()
          .delete("/api/opportunities/{id}")
          .then()
          .statusCode(501)
          .body(containsString("not yet implemented"));
    }
  }

  @Nested
  @Tag("core")
  @DisplayName("PUT /api/opportunities/{id}/stage - Change Stage")
  class ChangeStageTests {

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("Should change stage with valid request")
    void changeStage_validStage_shouldReturn200() {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", OpportunityStage.NEW_LEAD);

      // Act & Assert - Using path parameters for stage
      given()
          .pathParam("id", opportunity.getId())
          .pathParam("stage", "QUALIFICATION")
          .when()
          .put("/api/opportunities/{id}/stage/{stage}")
          .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("stage", equalTo("QUALIFICATION"))
          .body("probability", equalTo(25))
          .body("stageChangedAt", notNullValue());
    }

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("Should return 404 for invalid stage")
    void changeStage_invalidStage_shouldReturn404() {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", OpportunityStage.NEW_LEAD);

      // Act & Assert - JAX-RS returns 404 when it cannot convert enum path parameters
      given()
          .pathParam("id", opportunity.getId())
          .pathParam("stage", "INVALID_STAGE")
          .when()
          .put("/api/opportunities/{id}/stage/{stage}")
          .then()
          .statusCode(404);
    }

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("Should return 404 for non-existent opportunity")
    void changeStage_nonExistentId_shouldReturn404() {
      // Arrange
      var nonExistentId = UUID.randomUUID();

      // Act & Assert - Using path parameters
      given()
          .pathParam("id", nonExistentId)
          .pathParam("stage", "QUALIFICATION")
          .when()
          .put("/api/opportunities/{id}/stage/{stage}")
          .then()
          .statusCode(404)
          .contentType(ContentType.JSON);
    }
  }

  @Nested
  @Tag("core")
  @DisplayName("GET /api/opportunities/pipeline/overview - Pipeline Overview")
  class PipelineOverviewTests {

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("Should return pipeline overview")
    void getPipelineOverview_shouldReturnData() {
      // Arrange
      createTestOpportunity("New1", OpportunityStage.NEW_LEAD);
      createTestOpportunity("New2", OpportunityStage.NEW_LEAD);
      createTestOpportunity("Proposal1", OpportunityStage.PROPOSAL);

      // Act & Assert - Test existing endpoint
      given()
          .when()
          .get("/api/opportunities/pipeline/overview")
          .then()
          .statusCode(200)
          .contentType(ContentType.JSON)
          .body("stageStatistics", notNullValue())
          .body("totalForecast", notNullValue())
          .body("conversionRate", notNullValue());
    }
  }

  @Nested
  @Tag("core")
  @DisplayName("Content-Type and Error Handling")
  class ContentTypeAndErrorTests {

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
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
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
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
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
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

    // Create test customer using CustomerBuilder
    var customer = customerBuilder.withCompanyName(companyName).build();

    // Override to use exact company name without [TEST-xxx] prefix
    customer.setCompanyName(companyName);
    customer.setCustomerNumber("TEST-" + System.currentTimeMillis()); // Keep unique customer number
    customer.setIsTestData(true); // Mark as test data

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
    var opportunity =
        opportunityBuilder
            .withName(name)
            .inStage(stage)
            .assignedTo(testUser)
            .forCustomer(testCustomer)
            .persist();
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
