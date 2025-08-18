package de.freshplan.api.resources;

import static io.restassured.RestAssured.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.CustomerType;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.service.dto.*;
import de.freshplan.test.BaseIntegrationTest;
import de.freshplan.test.TestcontainersProfile;
import de.freshplan.test.builders.CustomerBuilder;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration Tests für CustomerResource REST API.
 *
 * <p>Testet alle 13 Endpoints mit echten HTTP-Requests und PostgreSQL-Datenbank. Nutzt RestAssured
 * für API-Tests und Testcontainers für die DB.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
@TestProfile(TestcontainersProfile.class)
@TestTransaction
public class CustomerResourceIntegrationTest extends BaseIntegrationTest {

  private static final String BASE_PATH = "/api/customers";

  @Inject CustomerBuilder customerBuilder;

  @BeforeEach
  void setUp() {
    RestAssured.basePath = BASE_PATH;
  }

  // ========== CRUD OPERATIONS TESTS ==========

  @Test
  void createCustomer_withValidData_shouldReturn201() {
    CreateCustomerRequest request =
        customerBuilder
            .withCompanyName("[TEST] Test Company GmbH")
            .asEnterprise()
            .inIndustry(Industry.HOTEL)
            .buildCreateRequest();

    CustomerResponse response =
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post()
            .then()
            .statusCode(201)
            .extract()
            .as(CustomerResponse.class);

    assertNotNull(response.id());
    assertEquals(request.companyName(), response.companyName());
    assertEquals(request.customerType(), response.customerType());
    assertEquals(CustomerStatus.LEAD, response.status());
    assertNotNull(response.customerNumber());
    assertTrue(response.customerNumber().startsWith("KD-2025-"));
  }

  @Test
  void createCustomer_withInvalidData_shouldReturn400() {
    // Company name is required
    CreateCustomerRequest request =
        new CreateCustomerRequest(
            null, // companyName is null
            null, // tradingName
            null, // legalForm
            CustomerType.UNTERNEHMEN, // customerType
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    given().contentType(ContentType.JSON).body(request).when().post().then().statusCode(400);
  }

  @Test
  void getCustomer_withValidId_shouldReturn200() {
    // First create a customer
    CustomerResponse created = createTestCustomerViaAPI("[TEST] Customer for Get");

    CustomerResponse response =
        given()
            .when()
            .get("/{id}", created.id())
            .then()
            .statusCode(200)
            .extract()
            .as(CustomerResponse.class);

    assertEquals(created.id(), response.id());
    assertEquals(created.companyName(), response.companyName());
  }

  @Test
  void getCustomer_withInvalidId_shouldReturn404() {
    UUID randomId = UUID.randomUUID();

    given()
        .when()
        .get("/{id}", randomId)
        .then()
        .statusCode(404)
        .body(containsString("Customer not found"));
  }

  @Test
  void updateCustomer_withValidData_shouldReturn200() {
    // First create a customer
    CustomerResponse created = createTestCustomerViaAPI("[TEST] Customer for Update");

    UpdateCustomerRequest updateRequest =
        UpdateCustomerRequest.builder()
            .companyName("Updated Company Name")
            .tradingName("Updated Trading Name")
            .customerType(created.customerType())
            .legalForm(created.legalForm())
            .industry(created.industry())
            .build();

    CustomerResponse response =
        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .when()
            .put("/{id}", created.id())
            .then()
            .statusCode(200)
            .extract()
            .as(CustomerResponse.class);

    assertEquals("Updated Company Name", response.companyName());
    assertEquals("Updated Trading Name", response.tradingName());
  }

  @Test
  void deleteCustomer_shouldReturn204() {
    // First create a customer
    CustomerResponse created = createTestCustomerViaAPI("[TEST] Customer for Delete");

    given()
        .queryParam("reason", "Test deletion")
        .when()
        .delete("/{id}", created.id())
        .then()
        .statusCode(204);

    // Verify customer is soft deleted
    given().when().get("/{id}", created.id()).then().statusCode(404);
  }

  @Test
  void restoreCustomer_shouldReturn200() {
    // First create and delete a customer
    CustomerResponse created = createTestCustomerViaAPI("[TEST] Customer for Restore");

    given()
        .queryParam("reason", "Test deletion")
        .when()
        .delete("/{id}", created.id())
        .then()
        .statusCode(204);

    // Now restore
    CustomerResponse restored =
        given()
            .when()
            .put("/{id}/restore", created.id())
            .then()
            .statusCode(200)
            .extract()
            .as(CustomerResponse.class);

    assertEquals(created.id(), restored.id());
    assertFalse(restored.isDeleted());
  }

  // ========== LIST & SEARCH OPERATIONS TESTS ==========

  @Test
  void getAllCustomers_withPagination_shouldReturn200() {
    // Create multiple customers with unique names to avoid conflicts
    String testPrefix = "[TEST] Pagination_" + UUID.randomUUID().toString();
    for (int i = 0; i < 5; i++) {
      createTestCustomerViaAPI(testPrefix + "_Company_" + i);
    }

    CustomerListResponse response =
        given()
            .queryParam("page", 0)
            .queryParam("size", 3)
            .when()
            .get()
            .then()
            .statusCode(200)
            .extract()
            .as(CustomerListResponse.class);

    // Check that pagination works correctly (we get max 3 items per page)
    assertEquals(3, response.content().size());
    assertEquals(0, response.page());
    assertEquals(3, response.size());
    // Total elements will be at least 5 (could be more from other tests)
    assertTrue(
        response.totalElements() >= 5,
        "Expected at least 5 elements, got " + response.totalElements());
    // There should be at least 2 pages
    assertTrue(
        response.totalPages() >= 2, "Expected at least 2 pages, got " + response.totalPages());
    assertTrue(response.first());
    assertFalse(response.last());
  }

  @Test
  void getAllCustomers_withStatusFilter_shouldReturnFiltered() {
    // Create customers with unique names
    String testPrefix = "[TEST] StatusFilter_" + UUID.randomUUID().toString();
    CustomerResponse lead = createTestCustomerViaAPI(testPrefix + "_Company");

    // Update one to AKTIV status
    changeCustomerStatus(lead.id(), CustomerStatus.AKTIV);

    CustomerListResponse response =
        given()
            .queryParam("status", CustomerStatus.AKTIV)
            .when()
            .get()
            .then()
            .statusCode(200)
            .extract()
            .as(CustomerListResponse.class);

    // We should have at least one AKTIV customer
    assertTrue(response.content().size() >= 1, "Expected at least 1 AKTIV customer");
    // All returned customers should have AKTIV status
    assertTrue(
        response.content().stream().allMatch(c -> c.status() == CustomerStatus.AKTIV),
        "All customers should have AKTIV status");
  }

  // Note: Search functionality has been moved to POST /api/customers/search
  // See CustomerSearchResourceTest for comprehensive search API tests

  // ========== ANALYTICS & DASHBOARD TESTS ==========

  @Test
  void getDashboardData_shouldReturnMetrics() {
    // Get baseline count
    CustomerDashboardResponse baseline =
        given()
            .when()
            .get("/dashboard")
            .then()
            .statusCode(200)
            .extract()
            .as(CustomerDashboardResponse.class);

    long baselineCount = baseline.totalCustomers();

    // Create test data with unique names
    String testPrefix = "[TEST] Dashboard_" + UUID.randomUUID().toString();
    createTestCustomerViaAPI(testPrefix + "_Company_1");
    createTestCustomerViaAPI(testPrefix + "_Company_2");
    createTestCustomerViaAPI(testPrefix + "_Company_3");

    CustomerDashboardResponse response =
        given()
            .when()
            .get("/dashboard")
            .then()
            .statusCode(200)
            .extract()
            .as(CustomerDashboardResponse.class);

    // Check that we have at least 3 more customers than baseline
    assertTrue(
        response.totalCustomers() >= baselineCount + 3,
        "Expected at least "
            + (baselineCount + 3)
            + " customers, got "
            + response.totalCustomers());
    assertNotNull(response.customersByStatus());
    assertNotNull(response.customersByLifecycle());
    assertTrue(response.customersByStatus().containsKey(CustomerStatus.LEAD));
  }

  @Test
  void getCustomersAtRisk_shouldReturnRiskCustomers() {
    // Create a customer with unique name
    String testPrefix = "[TEST] Risk_" + UUID.randomUUID().toString();
    CustomerResponse created = createTestCustomerViaAPI(testPrefix + "_Company");

    // We need to test risk assessment endpoint
    CustomerListResponse response =
        given()
            .queryParam("minRiskScore", 70)
            .when()
            .get("/analytics/risk-assessment")
            .then()
            .statusCode(200)
            .extract()
            .as(CustomerListResponse.class);

    assertNotNull(response);
    // We can't predict exact count due to other tests, but it should be a valid response
    // The test is mainly checking that the endpoint works
    assertTrue(response.content() != null);
    assertTrue(response.totalElements() >= 0);
  }

  // ========== HIERARCHY MANAGEMENT TESTS ==========

  @Test
  void getCustomerHierarchy_shouldReturnHierarchyTree() {
    // Create parent customer
    CustomerResponse parent = createTestCustomerViaAPI("[TEST] Parent Company");

    CustomerResponse hierarchy =
        given()
            .when()
            .get("/{id}/hierarchy", parent.id())
            .then()
            .statusCode(200)
            .extract()
            .as(CustomerResponse.class);

    assertEquals(parent.id(), hierarchy.id());
    assertNotNull(hierarchy.childCustomerIds());
    assertEquals(0, hierarchy.childCustomerIds().size());
  }

  @Test
  void addChildCustomer_shouldLinkCustomers() {
    // Create parent and child with unique names
    String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
    CustomerResponse parent = createTestCustomerViaAPI("[TEST] Parent Company " + uniqueSuffix);
    CustomerResponse child = createTestCustomerViaAPI("[TEST] Child Company " + uniqueSuffix);

    AddChildCustomerRequest request = new AddChildCustomerRequest(UUID.fromString(child.id()));

    CustomerResponse updatedChild =
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/{parentId}/children", parent.id())
            .then()
            .statusCode(200)
            .extract()
            .as(CustomerResponse.class);

    assertEquals(child.id(), updatedChild.id());
    assertEquals(parent.id(), updatedChild.parentCustomerId());
  }

  // ========== UTILITY OPERATIONS TESTS ==========

  @Test
  void checkDuplicates_shouldReturnPotentialDuplicates() {
    // Create customers with similar names
    createTestCustomerViaAPI("[TEST] ABC Company GmbH");
    createTestCustomerViaAPI("[TEST] ABC Company AG");

    CheckDuplicatesRequest request = new CheckDuplicatesRequest("ABC Company");

    List<CustomerResponse> duplicates =
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/check-duplicates")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<List<CustomerResponse>>() {});

    assertTrue(duplicates.size() >= 2);
    assertTrue(duplicates.stream().allMatch(c -> c.companyName().contains("ABC Company")));
  }

  @Test
  void mergeCustomers_shouldCombineCustomers() {
    // Create two customers
    CustomerResponse target = createTestCustomerViaAPI("[TEST] Target Company");
    CustomerResponse source = createTestCustomerViaAPI("[TEST] Source Company");

    MergeCustomersRequest request = new MergeCustomersRequest(UUID.fromString(source.id()));

    CustomerResponse merged =
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/{targetId}/merge", target.id())
            .then()
            .statusCode(200)
            .extract()
            .as(CustomerResponse.class);

    assertEquals(target.id(), merged.id());

    // Verify source is deleted
    given().when().get("/{id}", source.id()).then().statusCode(404);
  }

  @Test
  void changeCustomerStatus_shouldUpdateStatus() {
    // Create customer
    CustomerResponse customer = createTestCustomerViaAPI("[TEST] Customer for Status Change");
    assertEquals(CustomerStatus.LEAD, customer.status());

    ChangeStatusRequest request = new ChangeStatusRequest(CustomerStatus.AKTIV);

    CustomerResponse updated =
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/{id}/status", customer.id())
            .then()
            .statusCode(200)
            .extract()
            .as(CustomerResponse.class);

    assertEquals(CustomerStatus.AKTIV, updated.status());
  }

  // ========== HELPER METHODS ==========

  private CreateCustomerRequest createValidCustomerRequest() {
    return new CreateCustomerRequest(
        "[TEST] Test Company GmbH", // companyName
        "Test Trading Name", // tradingName
        "GmbH", // legalForm
        CustomerType.UNTERNEHMEN, // customerType
        Industry.HOTEL, // industry
        null, // classification
        null, // parentCustomerId
        null, // hierarchyType
        null, // status (defaults to LEAD)
        null, // lifecycleStage
        new BigDecimal("100000.00"), // expectedAnnualVolume
        null, // actualAnnualVolume
        null, // paymentTerms
        null, // creditLimit
        null, // deliveryCondition
        null, // lastContactDate
        null // nextFollowUpDate
        );
  }

  private CustomerResponse createTestCustomerViaAPI(String companyName) {
    CreateCustomerRequest request =
        new CreateCustomerRequest(
            companyName, // companyName
            null, // tradingName
            "GmbH", // legalForm
            CustomerType.UNTERNEHMEN, // customerType
            Industry.HOTEL, // industry
            null, // classification
            null, // parentCustomerId
            null, // hierarchyType
            null, // status
            null, // lifecycleStage
            null, // expectedAnnualVolume
            null, // actualAnnualVolume
            null, // paymentTerms
            null, // creditLimit
            null, // deliveryCondition
            null, // lastContactDate
            null // nextFollowUpDate
            );

    return given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post()
        .then()
        .statusCode(201)
        .extract()
        .as(CustomerResponse.class);
  }

  private void changeCustomerStatus(String customerId, CustomerStatus newStatus) {
    ChangeStatusRequest request = new ChangeStatusRequest(newStatus);

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put("/{id}/status", customerId)
        .then()
        .statusCode(200);
  }
}
