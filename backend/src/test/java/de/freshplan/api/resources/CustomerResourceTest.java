package de.freshplan.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.service.CustomerService;
import de.freshplan.domain.customer.service.dto.*;
import de.freshplan.domain.customer.service.exception.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Integration tests for CustomerResource REST API. Tests all HTTP endpoints with proper
 * request/response handling.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@DisplayName("CustomerResource Integration Tests")
class CustomerResourceTest {

  @InjectMock CustomerService customerService;

  private CustomerResponse customerResponse;
  private CustomerListResponse customerListResponse;
  private CustomerDashboardResponse dashboardResponse;
  private CreateCustomerRequest createRequest;
  private UpdateCustomerRequest updateRequest;

  @BeforeEach
  void setUp() {
    Mockito.reset(customerService);

    // Create test response DTOs
    UUID customerId = UUID.randomUUID();
    customerResponse =
        new CustomerResponse(
            customerId.toString(),
            "KD-2025-00001",
            "Test Hotel GmbH",
            "Hotel Test",
            "GmbH",
            CustomerType.UNTERNEHMEN,
            Industry.HOTEL,
            Classification.A_KUNDE,
            null, // parentCustomerId
            CustomerHierarchyType.STANDALONE,
            new ArrayList<>(), // childCustomerIds
            false, // hasChildren
            CustomerStatus.AKTIV,
            CustomerLifecycleStage.GROWTH,
            null, // partnerStatus
            new BigDecimal("50000.00"), // expectedAnnualVolume
            new BigDecimal("45000.00"), // actualAnnualVolume
            PaymentTerms.NETTO_30,
            new BigDecimal("10000.00"), // creditLimit
            DeliveryCondition.STANDARD,
            25, // riskScore
            false, // atRisk
            LocalDateTime.now().minusDays(15), // lastContactDate
            LocalDateTime.now().plusDays(30), // nextFollowUpDate
            LocalDateTime.now().minusDays(30), // createdAt
            "test-user", // createdBy
            LocalDateTime.now().minusDays(5), // updatedAt
            "update-user", // updatedBy
            false, // isDeleted
            null, // deletedAt
            null // deletedBy
            );

    customerListResponse =
        new CustomerListResponse(
            List.of(customerResponse),
            0, // page
            20, // size
            1L, // totalElements
            1, // totalPages
            true, // first
            true // last
            );

    Map<CustomerStatus, Long> statusCounts =
        Map.of(
            CustomerStatus.LEAD, 20L,
            CustomerStatus.AKTIV, 85L,
            CustomerStatus.RISIKO, 8L);

    Map<CustomerLifecycleStage, Long> lifecycleCounts =
        Map.of(
            CustomerLifecycleStage.ACQUISITION, 55L,
            CustomerLifecycleStage.GROWTH, 60L);

    dashboardResponse =
        new CustomerDashboardResponse(
            150L, // totalCustomers
            120L, // activeCustomers
            15L, // newThisMonth
            8L, // atRiskCount
            23L, // upcomingFollowUps
            statusCounts,
            lifecycleCounts);

    createRequest =
        CreateCustomerRequest.builder()
            .companyName("New Hotel GmbH")
            .customerType(CustomerType.UNTERNEHMEN)
            .industry(Industry.HOTEL)
            .expectedAnnualVolume(new BigDecimal("75000.00"))
            .build();

    updateRequest =
        new UpdateCustomerRequest(
            "Updated Hotel GmbH",
            "Updated Trading Name",
            "AG",
            CustomerType.UNTERNEHMEN,
            Industry.HOTEL,
            Classification.A_KUNDE,
            null, // parentCustomerId
            CustomerHierarchyType.HEADQUARTER,
            CustomerStatus.AKTIV,
            CustomerLifecycleStage.GROWTH,
            new BigDecimal("100000.00"),
            new BigDecimal("80000.00"),
            PaymentTerms.NETTO_30,
            new BigDecimal("15000.00"),
            DeliveryCondition.STANDARD,
            LocalDateTime.now().minusDays(3),
            LocalDateTime.now().plusDays(14));
  }

  @Nested
  @DisplayName("CRUD Operations")
  class CrudOperations {

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("POST /api/customers - Should create customer")
    void createCustomer_withValidRequest_shouldReturn201() {
      // Given
      when(customerService.createCustomer(
              org.mockito.ArgumentMatchers.any(CreateCustomerRequest.class), eq("testuser")))
          .thenReturn(customerResponse);

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(createRequest)
          .when()
          .post("/api/customers")
          .then()
          .statusCode(201);

      verify(customerService)
          .createCustomer(
              org.mockito.ArgumentMatchers.any(CreateCustomerRequest.class), eq("testuser"));
    }

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("POST /api/customers - Should return 400 for invalid request")
    void createCustomer_withInvalidRequest_shouldReturn400() {
      // Given - Request without required company name
      Map<String, Object> invalidRequest =
          Map.of(
              "customerType", "UNTERNEHMEN"
              // missing companyName
              );

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(invalidRequest)
          .when()
          .post("/api/customers")
          .then()
          .statusCode(400);

      verify(customerService, never()).createCustomer(any(), any());
    }

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("GET /api/customers/{id} - Should return customer")
    void getCustomer_withValidId_shouldReturn200() {
      // Given
      UUID customerId = UUID.randomUUID();
      when(customerService.getCustomer(customerId)).thenReturn(customerResponse);

      // When & Then
      given()
          .pathParam("id", customerId)
          .when()
          .get("/api/customers/{id}")
          .then()
          .statusCode(200)
          .body("id", notNullValue())
          .body("companyName", equalTo("Test Hotel GmbH"));

      verify(customerService).getCustomer(customerId);
    }

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("GET /api/customers/{id} - Should return 404 for non-existent customer")
    void getCustomer_withInvalidId_shouldReturn404() {
      // Given
      UUID customerId = UUID.randomUUID();
      when(customerService.getCustomer(customerId))
          .thenThrow(new CustomerNotFoundException(customerId));

      // When & Then
      given()
          .pathParam("id", customerId)
          .when()
          .get("/api/customers/{id}")
          .then()
          .statusCode(404)
          .body("error", equalTo("CUSTOMER_NOT_FOUND"));

      verify(customerService).getCustomer(customerId);
    }

    @Test
    @TestSecurity(
        user = "system",
        roles = {"admin", "manager", "sales"})
    @DisplayName("PUT /api/customers/{id} - Should update customer")
    void updateCustomer_withValidRequest_shouldReturn200() {
      // Given
      UUID customerId = UUID.randomUUID();
      when(customerService.updateCustomer(
              eq(customerId),
              org.mockito.ArgumentMatchers.any(UpdateCustomerRequest.class),
              eq("system")))
          .thenReturn(customerResponse);

      // When & Then
      given()
          .pathParam("id", customerId)
          .contentType(ContentType.JSON)
          .body(updateRequest)
          .when()
          .put("/api/customers/{id}")
          .then()
          .statusCode(200);

      verify(customerService)
          .updateCustomer(
              eq(customerId),
              org.mockito.ArgumentMatchers.any(UpdateCustomerRequest.class),
              eq("system"));
    }

    @Test
    @TestSecurity(
        user = "system",
        roles = {"admin", "manager", "sales"})
    @DisplayName("DELETE /api/customers/{id} - Should soft delete customer")
    void deleteCustomer_withValidId_shouldReturn204() {
      // Given
      UUID customerId = UUID.randomUUID();
      String reason = "Test deletion";
      doNothing().when(customerService).deleteCustomer(customerId, "system", reason);

      // When & Then
      given()
          .pathParam("id", customerId)
          .queryParam("reason", reason)
          .when()
          .delete("/api/customers/{id}")
          .then()
          .statusCode(204);

      verify(customerService).deleteCustomer(customerId, "system", reason);
    }

    @Test
    @TestSecurity(
        user = "system",
        roles = {"admin", "manager", "sales"})
    @DisplayName("DELETE /api/customers/{id} - Should return 400 when customer has children")
    void deleteCustomer_withChildren_shouldReturn400() {
      // Given
      UUID customerId = UUID.randomUUID();
      doThrow(new CustomerHasChildrenException(customerId, "delete"))
          .when(customerService)
          .deleteCustomer(eq(customerId), eq("system"), any());

      // When & Then
      given()
          .pathParam("id", customerId)
          .when()
          .delete("/api/customers/{id}")
          .then()
          .statusCode(400)
          .body("error", equalTo("CUSTOMER_HAS_CHILDREN"));

      verify(customerService).deleteCustomer(eq(customerId), eq("system"), any());
    }

    @Test
    @TestSecurity(
        user = "system",
        roles = {"admin", "manager", "sales"})
    @DisplayName("PUT /api/customers/{id}/restore - Should restore customer")
    void restoreCustomer_withValidId_shouldReturn200() {
      // Given
      UUID customerId = UUID.randomUUID();
      when(customerService.restoreCustomer(customerId, "system")).thenReturn(customerResponse);

      // When & Then
      given()
          .pathParam("id", customerId)
          .when()
          .put("/api/customers/{id}/restore")
          .then()
          .statusCode(200);

      verify(customerService).restoreCustomer(customerId, "system");
    }
  }

  @Nested
  @DisplayName("List and Search Operations")
  class ListAndSearchOperations {

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("GET /api/customers - Should return paginated customer list")
    void getAllCustomers_withPagination_shouldReturn200() {
      // Given
      when(customerService.getAllCustomers(0, 20)).thenReturn(customerListResponse);

      // When & Then
      given()
          .queryParam("page", 0)
          .queryParam("size", 20)
          .when()
          .get("/api/customers")
          .then()
          .statusCode(200)
          .body("content", hasSize(1))
          .body("page", equalTo(0))
          .body("size", equalTo(20))
          .body("totalElements", equalTo(1))
          .body("totalPages", equalTo(1))
          .body("first", equalTo(true))
          .body("last", equalTo(true));

      verify(customerService).getAllCustomers(0, 20);
    }

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("GET /api/customers - Should handle status filter")
    void getAllCustomers_withStatusFilter_shouldReturn200() {
      // Given
      CustomerStatus status = CustomerStatus.AKTIV;
      when(customerService.getCustomersByStatus(status, 0, 20)).thenReturn(customerListResponse);

      // When & Then
      given()
          .queryParam("page", 0)
          .queryParam("size", 20)
          .queryParam("status", "AKTIV")
          .when()
          .get("/api/customers")
          .then()
          .statusCode(200)
          .body("content", hasSize(1));

      verify(customerService).getCustomersByStatus(status, 0, 20);
    }

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("GET /api/customers - Should handle industry filter")
    void getAllCustomers_withIndustryFilter_shouldReturn200() {
      // Given
      Industry industry = Industry.HOTEL;
      when(customerService.getCustomersByIndustry(industry, 0, 20))
          .thenReturn(customerListResponse);

      // When & Then
      given()
          .queryParam("page", 0)
          .queryParam("size", 20)
          .queryParam("industry", "HOTEL")
          .when()
          .get("/api/customers")
          .then()
          .statusCode(200)
          .body("content", hasSize(1));

      verify(customerService).getCustomersByIndustry(industry, 0, 20);
    }

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("GET /api/customers - Should validate pagination parameters")
    void getAllCustomers_withInvalidPagination_shouldCorrectParameters() {
      // Given
      when(customerService.getAllCustomers(0, 20)).thenReturn(customerListResponse);

      // When & Then - Invalid parameters should be corrected
      given()
          .queryParam("page", -1)
          .queryParam("size", 200) // > 100
          .when()
          .get("/api/customers")
          .then()
          .statusCode(200);

      // Should call with corrected parameters: page=0, size=20
      verify(customerService).getAllCustomers(0, 20);
    }

    // Note: Search functionality has been moved to POST /api/customers/search
    // Tests for the new search API are in CustomerSearchResourceTest
  }

  @Nested
  @DisplayName("Analytics and Dashboard")
  class AnalyticsAndDashboard {

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("GET /api/customers/dashboard - Should return dashboard data")
    void getDashboardData_shouldReturn200() {
      // Given
      when(customerService.getDashboardData()).thenReturn(dashboardResponse);

      // When & Then
      given()
          .when()
          .get("/api/customers/dashboard")
          .then()
          .statusCode(200)
          .body("totalCustomers", equalTo(150))
          .body("activeCustomers", equalTo(120))
          .body("newThisMonth", equalTo(15))
          .body("atRiskCount", equalTo(8))
          .body("upcomingFollowUps", equalTo(23))
          .body("customersByStatus.AKTIV", equalTo(85))
          .body("customersByLifecycle.GROWTH", equalTo(60));

      verify(customerService).getDashboardData();
    }

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("GET /api/customers/analytics/risk-assessment - Should return at-risk customers")
    void getCustomersAtRisk_shouldReturn200() {
      // Given
      int minRiskScore = 70;
      when(customerService.getCustomersAtRisk(minRiskScore, 0, 20))
          .thenReturn(customerListResponse);

      // When & Then
      given()
          .queryParam("minRiskScore", minRiskScore)
          .queryParam("page", 0)
          .queryParam("size", 20)
          .when()
          .get("/api/customers/analytics/risk-assessment")
          .then()
          .statusCode(200)
          .body("content", hasSize(1));

      verify(customerService).getCustomersAtRisk(minRiskScore, 0, 20);
    }

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName(
        "GET /api/customers/analytics/risk-assessment - Should validate risk score parameter")
    void getCustomersAtRisk_withInvalidRiskScore_shouldCorrectParameter() {
      // Given
      when(customerService.getCustomersAtRisk(70, 0, 20)).thenReturn(customerListResponse);

      // When & Then - Invalid risk score should be corrected to default
      given()
          .queryParam("minRiskScore", 150) // > 100
          .when()
          .get("/api/customers/analytics/risk-assessment")
          .then()
          .statusCode(200);

      // Should call with corrected parameter: minRiskScore=70
      verify(customerService).getCustomersAtRisk(70, 0, 20);
    }
  }

  @Nested
  @DisplayName("Hierarchy Management")
  class HierarchyManagement {

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("GET /api/customers/{id}/hierarchy - Should return customer hierarchy")
    void getCustomerHierarchy_withValidId_shouldReturn200() {
      // Given
      UUID customerId = UUID.randomUUID();
      when(customerService.getCustomerHierarchy(customerId)).thenReturn(customerResponse);

      // When & Then
      given()
          .pathParam("id", customerId)
          .when()
          .get("/api/customers/{id}/hierarchy")
          .then()
          .statusCode(200)
          .body("companyName", equalTo("Test Hotel GmbH"));

      verify(customerService).getCustomerHierarchy(customerId);
    }

    @Test
    @TestSecurity(
        user = "system",
        roles = {"admin", "manager", "sales"})
    @DisplayName("POST /api/customers/{parentId}/children - Should add child customer")
    void addChildCustomer_withValidIds_shouldReturn200() {
      // Given
      UUID parentId = UUID.randomUUID();
      UUID childId = UUID.randomUUID();
      AddChildCustomerRequest request = new AddChildCustomerRequest(childId);

      when(customerService.addChildCustomer(parentId, childId, "system"))
          .thenReturn(customerResponse);

      // When & Then
      given()
          .pathParam("parentId", parentId)
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/customers/{parentId}/children")
          .then()
          .statusCode(200);

      verify(customerService).addChildCustomer(parentId, childId, "system");
    }
  }

  @Nested
  @DisplayName("Utility Operations")
  class UtilityOperations {

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("POST /api/customers/check-duplicates - Should check for duplicates")
    void checkDuplicates_withCompanyName_shouldReturn200() {
      // Given
      String companyName = "Test Hotel";
      CheckDuplicatesRequest request = new CheckDuplicatesRequest(companyName);
      List<CustomerResponse> duplicates = List.of(customerResponse);

      when(customerService.checkDuplicates(companyName)).thenReturn(duplicates);

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/customers/check-duplicates")
          .then()
          .statusCode(200)
          .body("$", hasSize(1))
          .body("[0].companyName", equalTo("Test Hotel GmbH"));

      verify(customerService).checkDuplicates(companyName);
    }

    @Test
    @TestSecurity(
        user = "system",
        roles = {"admin", "manager", "sales"})
    @DisplayName("POST /api/customers/{targetId}/merge - Should merge customers")
    void mergeCustomers_withValidIds_shouldReturn200() {
      // Given
      UUID targetId = UUID.randomUUID();
      UUID sourceId = UUID.randomUUID();
      MergeCustomersRequest request = new MergeCustomersRequest(sourceId);

      when(customerService.mergeCustomers(targetId, sourceId, "system"))
          .thenReturn(customerResponse);

      // When & Then
      given()
          .pathParam("targetId", targetId)
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/customers/{targetId}/merge")
          .then()
          .statusCode(200);

      verify(customerService).mergeCustomers(targetId, sourceId, "system");
    }

    @Test
    @TestSecurity(
        user = "system",
        roles = {"admin", "manager", "sales"})
    @DisplayName("PUT /api/customers/{id}/status - Should change customer status")
    void changeCustomerStatus_withValidStatus_shouldReturn200() {
      // Given
      UUID customerId = UUID.randomUUID();
      CustomerStatus newStatus = CustomerStatus.AKTIV;
      ChangeStatusRequest request = new ChangeStatusRequest(newStatus);

      when(customerService.changeStatus(customerId, newStatus, "system"))
          .thenReturn(customerResponse);

      // When & Then
      given()
          .pathParam("id", customerId)
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .put("/api/customers/{id}/status")
          .then()
          .statusCode(200);

      verify(customerService).changeStatus(customerId, newStatus, "system");
    }
  }

  @Nested
  @DisplayName("Error Handling")
  class ErrorHandling {

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("Should handle CustomerAlreadyExistsException")
    void handleCustomerAlreadyExistsException_shouldReturn409() {
      // Given
      when(customerService.createCustomer(
              org.mockito.ArgumentMatchers.any(CreateCustomerRequest.class), any()))
          .thenThrow(new CustomerAlreadyExistsException("company", "Test Hotel GmbH"));

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(createRequest)
          .when()
          .post("/api/customers")
          .then()
          .statusCode(409)
          .body("error", equalTo("CUSTOMER_ALREADY_EXISTS"))
          .body("field", nullValue())
          .body("conflictingValue", equalTo("Test Hotel GmbH"));
    }

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("Should handle validation errors")
    void handleValidationErrors_shouldReturn400() {
      // Given - Request with invalid data
      Map<String, Object> invalidRequest =
          Map.of(
              "companyName", "", // Blank company name
              "customerType", "INVALID_TYPE", // Invalid enum
              "expectedAnnualVolume", -1000 // Negative value
              );

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(invalidRequest)
          .when()
          .post("/api/customers")
          .then()
          .statusCode(400);
    }

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("Should handle malformed JSON")
    void handleMalformedJson_shouldReturn400() {
      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body("{invalid json}")
          .when()
          .post("/api/customers")
          .then()
          .statusCode(400);
    }

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("Should handle unsupported media type")
    void handleUnsupportedMediaType_shouldReturn415() {
      // When & Then
      given()
          .contentType(ContentType.TEXT)
          .body("text content")
          .when()
          .post("/api/customers")
          .then()
          .statusCode(415);
    }
  }

  @Nested
  @DisplayName("Content Negotiation")
  class ContentNegotiation {

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("Should accept and return JSON content type")
    void contentNegotiation_shouldHandleJson() {
      // Given
      when(customerService.createCustomer(
              org.mockito.ArgumentMatchers.any(CreateCustomerRequest.class), any()))
          .thenReturn(customerResponse);

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .accept(ContentType.JSON)
          .body(createRequest)
          .when()
          .post("/api/customers")
          .then()
          .statusCode(201)
          .contentType(ContentType.JSON)
          .body("companyName", equalTo("Test Hotel GmbH"));
    }

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("Should handle missing Accept header")
    void contentNegotiation_withoutAcceptHeader_shouldDefaultToJson() {
      // Given
      UUID customerId = UUID.randomUUID();
      when(customerService.getCustomer(customerId)).thenReturn(customerResponse);

      // When & Then
      given().pathParam("id", customerId).when().get("/api/customers/{id}").then().statusCode(200);
    }
  }

  @Nested
  @DisplayName("Performance and Load")
  class PerformanceAndLoad {

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("Should handle concurrent requests")
    void handleConcurrentRequests_shouldPerformWell() {
      // Given
      UUID customerId = UUID.randomUUID();
      when(customerService.getCustomer(customerId)).thenReturn(customerResponse);

      // When & Then - Simulate multiple concurrent requests
      for (int i = 0; i < 10; i++) {
        given()
            .pathParam("id", customerId)
            .when()
            .get("/api/customers/{id}")
            .then()
            .statusCode(200)
            .time(lessThan(1000L)); // Should respond within 1 second
      }

      verify(customerService, times(10)).getCustomer(customerId);
    }

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager", "sales"})
    @DisplayName("Should handle large pagination requests efficiently")
    void handleLargePagination_shouldPerformWell() {
      // Given
      when(customerService.getAllCustomers(anyInt(), anyInt())).thenReturn(customerListResponse);

      // When & Then - Request large page
      given()
          .queryParam("page", 100)
          .queryParam("size", 100)
          .when()
          .get("/api/customers")
          .then()
          .statusCode(200)
          .time(lessThan(2000L)); // Should handle large pagination efficiently

      verify(customerService).getAllCustomers(100, 100); // Size is passed through as requested
    }
  }
}
