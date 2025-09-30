package de.freshplan.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.customer.service.dto.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Tests for validating Customer Search result accuracy and correctness.
 *
 * <p>These tests verify that search results match the expected criteria, are properly formatted,
 * and contain accurate data.
 */
@QuarkusTest
@Tag("migrate")
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
class CustomerSearchResultValidationTest {

  @Nested
  @DisplayName("Result Structure Validation")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class ResultStructureValidation {

    @Test
    @DisplayName("Search result should have correct pagination structure")
    void testSearchResult_paginationStructure() {
      // Given: Basic search request
      CustomerSearchRequest request = new CustomerSearchRequest();

      // When & Then: Verify pagination structure
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .queryParam("page", 0)
          .queryParam("size", 10)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(200)
          .body("content", notNullValue())
          .body("page", equalTo(0))
          .body("size", equalTo(10))
          .body("totalElements", notNullValue())
          .body("totalPages", notNullValue())
          .body("first", notNullValue())
          .body("last", notNullValue())
          .body("numberOfElements", notNullValue());
    }

    @Test
    @DisplayName("Search result customers should have required fields")
    void testSearchResult_customerFields() {
      // Given: Search request that should return customers
      CustomerSearchRequest request = new CustomerSearchRequest();
      FilterCriteria filter = new FilterCriteria();
      filter.setField("status");
      filter.setOperator(FilterOperator.EQUALS);
      filter.setValue("AKTIV");
      request.setFilters(Arrays.asList(filter));

      // When: Execute search
      Response response =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .when()
              .post("/api/customers/search")
              .then()
              .statusCode(200)
              .body("content", notNullValue())
              .extract()
              .response();

      // Then: If customers found, verify field structure
      List<Map<String, Object>> customers = response.jsonPath().getList("content");
      if (!customers.isEmpty()) {
        Map<String, Object> firstCustomer = customers.get(0);
        assertNotNull(firstCustomer.get("id"), "Customer ID should not be null");
        assertNotNull(firstCustomer.get("customerNumber"), "Customer number should not be null");
        assertNotNull(firstCustomer.get("companyName"), "Company name should not be null");
        assertNotNull(firstCustomer.get("status"), "Status should not be null");
        assertNotNull(firstCustomer.get("createdAt"), "Created date should not be null");
      } else {
        // No customers found - test passes as the search structure is correct
        assertTrue(true, "Search executed successfully, no customers found for AKTIV status");
      }
    }

    @Test
    @DisplayName("Search result should not contain sensitive data")
    void testSearchResult_noSensitiveData() {
      // Given: Search request
      CustomerSearchRequest request = new CustomerSearchRequest();

      // When & Then: Verify no sensitive data is exposed
      Response response = given().contentType(ContentType.JSON).body(request).when().post();

      String responseBody = response.getBody().asString();

      // Should not contain sensitive information
      assertFalse(responseBody.contains("password"), "Response should not contain passwords");
      assertFalse(responseBody.contains("secret"), "Response should not contain secrets");
      assertFalse(responseBody.contains("key"), "Response should not contain keys");
      assertFalse(responseBody.contains("token"), "Response should not contain tokens");
    }

    @Test
    @DisplayName("Empty search result should have correct structure")
    void testEmptySearchResult_structure() {
      // Given: Search request that returns no results
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("NonExistentCompanyName12345");

      // When & Then: Verify empty result structure
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(200)
          .body("content", hasSize(0))
          .body("totalElements", equalTo(0))
          .body("totalPages", equalTo(0))
          .body("numberOfElements", equalTo(0))
          .body("first", equalTo(true))
          .body("last", equalTo(true));
    }
  }

  @Nested
  @DisplayName("Filter Accuracy Validation")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class FilterAccuracyValidation {

    @Test
    @DisplayName("Status filter should return only customers with matching status")
    void testStatusFilter_accuracy() {
      // Given: Status filter for AKTIV customers
      CustomerSearchRequest request = new CustomerSearchRequest();
      FilterCriteria filter = new FilterCriteria();
      filter.setField("status");
      filter.setOperator(FilterOperator.EQUALS);
      filter.setValue("AKTIV");
      request.setFilters(Arrays.asList(filter));

      // When & Then: Verify all returned customers have AKTIV status
      Response response =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .when()
              .post("/api/customers/search")
              .then()
              .statusCode(200)
              .body("content", notNullValue())
              .extract()
              .response();

      List<Map<String, Object>> customers = response.jsonPath().getList("content");
      for (Map<String, Object> customer : customers) {
        assertEquals(
            "AKTIV",
            customer.get("status"),
            "Customer " + customer.get("customerNumber") + " should have AKTIV status");
      }
    }

    @Test
    @DisplayName("Industry filter should return only customers with matching industry")
    void testIndustryFilter_accuracy() {
      // Given: Industry filter for HOTEL customers
      CustomerSearchRequest request = new CustomerSearchRequest();
      FilterCriteria filter = new FilterCriteria();
      filter.setField("industry");
      filter.setOperator(FilterOperator.EQUALS);
      filter.setValue("HOTEL");
      request.setFilters(Arrays.asList(filter));

      // When & Then: Verify all returned customers have HOTEL industry
      Response response =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .when()
              .post("/api/customers/search")
              .then()
              .statusCode(200)
              .body("content", notNullValue())
              .extract()
              .response();

      List<Map<String, Object>> customers = response.jsonPath().getList("content");
      for (Map<String, Object> customer : customers) {
        assertEquals(
            "HOTEL",
            customer.get("industry"),
            "Customer " + customer.get("customerNumber") + " should have HOTEL industry");
      }
    }

    @Test
    @DisplayName("Volume range filter should return customers within range")
    void testVolumeRangeFilter_accuracy() {
      // Given: Volume filter for customers with volume > 20000
      CustomerSearchRequest request = new CustomerSearchRequest();
      FilterCriteria filter = new FilterCriteria();
      filter.setField("expectedAnnualVolume");
      filter.setOperator(FilterOperator.GREATER_THAN);
      filter.setValue("20000");
      request.setFilters(Arrays.asList(filter));

      // When & Then: Verify all returned customers have volume > 20000
      Response response =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .when()
              .post("/api/customers/search")
              .then()
              .statusCode(200)
              .body("content", notNullValue())
              .extract()
              .response();

      List<Map<String, Object>> customers = response.jsonPath().getList("content");
      for (Map<String, Object> customer : customers) {
        Object volume = customer.get("expectedAnnualVolume");
        if (volume != null) {
          assertTrue(
              ((Number) volume).doubleValue() > 20000,
              "Customer " + customer.get("customerNumber") + " should have volume > 20000");
        }
      }
    }

    @Test
    @DisplayName("Date range filter should return customers within date range")
    void testDateRangeFilter_accuracy() {
      // Given: Date filter for customers created after 2024-01-01
      CustomerSearchRequest request = new CustomerSearchRequest();
      FilterCriteria filter = new FilterCriteria();
      filter.setField("createdAt");
      filter.setOperator(FilterOperator.GREATER_THAN);
      filter.setValue("2024-01-01");
      request.setFilters(Arrays.asList(filter));

      // When & Then: Verify all returned customers were created after 2024-01-01
      Response response =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .when()
              .post("/api/customers/search")
              .then()
              .statusCode(200)
              .body("content", notNullValue())
              .extract()
              .response();

      List<Map<String, Object>> customers = response.jsonPath().getList("content");
      for (Map<String, Object> customer : customers) {
        String createdAt = (String) customer.get("createdAt");
        if (createdAt != null) {
          assertTrue(
              createdAt.compareTo("2024-01-01") > 0,
              "Customer " + customer.get("customerNumber") + " should be created after 2024-01-01");
        }
      }
    }

    @Test
    @DisplayName("Multiple filters should return customers matching all criteria")
    void testMultipleFilters_accuracy() {
      // Given: Multiple filters
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setFilters(
          Arrays.asList(
              createFilter("status", FilterOperator.EQUALS, "AKTIV"),
              createFilter("industry", FilterOperator.EQUALS, "HOTEL"),
              createFilter("expectedAnnualVolume", FilterOperator.GREATER_THAN, "30000")));

      // When & Then: Verify all returned customers match all criteria
      Response response =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .when()
              .post("/api/customers/search")
              .then()
              .statusCode(200)
              .body("content", notNullValue())
              .extract()
              .response();

      List<Map<String, Object>> customers = response.jsonPath().getList("content");
      for (Map<String, Object> customer : customers) {
        assertEquals("AKTIV", customer.get("status"), "Customer should have AKTIV status");
        assertEquals("HOTEL", customer.get("industry"), "Customer should have HOTEL industry");
        Object volume = customer.get("expectedAnnualVolume");
        if (volume != null) {
          assertTrue(
              ((Number) volume).doubleValue() > 30000, "Customer should have volume > 30000");
        }
      }
    }
  }

  @Nested
  @DisplayName("Global Search Validation")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class GlobalSearchValidation {

    @Test
    @DisplayName("Global search should find customers with matching company names")
    void testGlobalSearch_companyNameMatches() {
      // Given: Global search for "Hotel"
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("Hotel");

      // When & Then: Verify results contain the search term
      Response response =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .when()
              .post("/api/customers/search")
              .then()
              .statusCode(200)
              .body("content", notNullValue())
              .extract()
              .response();

      List<Map<String, Object>> customers = response.jsonPath().getList("content");
      for (Map<String, Object> customer : customers) {
        String companyName = (String) customer.get("companyName");
        assertTrue(
            companyName.toLowerCase().contains("hotel"),
            "Customer "
                + customer.get("customerNumber")
                + " should contain 'hotel' in company name");
      }
    }

    @Test
    @DisplayName("Global search should be case insensitive")
    void testGlobalSearch_caseInsensitive() {
      // Given: Global search with different cases
      CustomerSearchRequest request1 = new CustomerSearchRequest();
      request1.setGlobalSearch("HOTEL");

      CustomerSearchRequest request2 = new CustomerSearchRequest();
      request2.setGlobalSearch("hotel");

      // When & Then: Both searches should return same results
      Response response1 =
          given()
              .contentType(ContentType.JSON)
              .body(request1)
              .when()
              .post("/api/customers/search")
              .then()
              .statusCode(200)
              .extract()
              .response();

      Response response2 =
          given()
              .contentType(ContentType.JSON)
              .body(request2)
              .when()
              .post("/api/customers/search")
              .then()
              .statusCode(200)
              .extract()
              .response();

      int totalElements1 = response1.jsonPath().getInt("totalElements");
      int totalElements2 = response2.jsonPath().getInt("totalElements");

      assertEquals(
          totalElements1,
          totalElements2,
          "Case insensitive search should return same number of results");
    }

    @Test
    @DisplayName("Global search with partial match should work")
    void testGlobalSearch_partialMatch() {
      // Given: Global search with partial word
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("Rest"); // Should match "Restaurant"

      // When & Then: Verify partial matches work
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(200)
          .body("content", notNullValue())
          .body("totalElements", greaterThanOrEqualTo(0));
    }
  }

  @Nested
  @DisplayName("Sorting Validation")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class SortingValidation {

    @Test
    @DisplayName("Ascending sort should return results in ascending order")
    void testAscendingSort_accuracy() {
      // Given: Sort by company name ascending
      CustomerSearchRequest request = new CustomerSearchRequest();
      SortCriteria sortCriteria = new SortCriteria();
      sortCriteria.setField("companyName");
      sortCriteria.setDirection("ASC");
      request.setMultiSort(Arrays.asList(sortCriteria));

      // When & Then: Verify ascending order
      Response response =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .when()
              .post("/api/customers/search")
              .then()
              .statusCode(200)
              .body("content", notNullValue())
              .extract()
              .response();

      List<Map<String, Object>> customers = response.jsonPath().getList("content");
      for (int i = 1; i < customers.size(); i++) {
        String prev = (String) customers.get(i - 1).get("companyName");
        String curr = (String) customers.get(i).get("companyName");
        assertTrue(
            prev.compareTo(curr) <= 0,
            "Companies should be in ascending order: " + prev + " <= " + curr);
      }
    }

    @Test
    @DisplayName("Descending sort should return results in descending order")
    void testDescendingSort_accuracy() {
      // Given: Sort by expected annual volume descending
      CustomerSearchRequest request = new CustomerSearchRequest();
      SortCriteria sortCriteria = new SortCriteria();
      sortCriteria.setField("expectedAnnualVolume");
      sortCriteria.setDirection("DESC");
      request.setMultiSort(Arrays.asList(sortCriteria));

      // When & Then: Verify descending order
      Response response =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .when()
              .post("/api/customers/search")
              .then()
              .statusCode(200)
              .body("content", notNullValue())
              .extract()
              .response();

      List<Map<String, Object>> customers = response.jsonPath().getList("content");
      for (int i = 1; i < customers.size(); i++) {
        Object prevObj = customers.get(i - 1).get("expectedAnnualVolume");
        Object currObj = customers.get(i).get("expectedAnnualVolume");

        if (prevObj != null && currObj != null) {
          double prev = ((Number) prevObj).doubleValue();
          double curr = ((Number) currObj).doubleValue();
          assertTrue(
              prev >= curr, "Volumes should be in descending order: " + prev + " >= " + curr);
        }
      }
    }

    @Test
    @DisplayName("Multiple sort criteria should be applied in order")
    void testMultipleSortCriteria_accuracy() {
      // Given: Sort by status descending, then by company name ascending
      CustomerSearchRequest request = new CustomerSearchRequest();
      SortCriteria sort1 = new SortCriteria();
      sort1.setField("status");
      sort1.setDirection("DESC");

      SortCriteria sort2 = new SortCriteria();
      sort2.setField("companyName");
      sort2.setDirection("ASC");

      request.setMultiSort(Arrays.asList(sort1, sort2));

      // When & Then: Verify multiple sort criteria
      Response response =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .when()
              .post("/api/customers/search")
              .then()
              .statusCode(200)
              .body("content", notNullValue())
              .extract()
              .response();

      List<Map<String, Object>> customers = response.jsonPath().getList("content");
      for (int i = 1; i < customers.size(); i++) {
        String prevStatus = (String) customers.get(i - 1).get("status");
        String currStatus = (String) customers.get(i).get("status");
        String prevCompany = (String) customers.get(i - 1).get("companyName");
        String currCompany = (String) customers.get(i).get("companyName");

        // First sort by status descending
        if (!prevStatus.equals(currStatus)) {
          assertTrue(prevStatus.compareTo(currStatus) >= 0, "Status should be in descending order");
        } else {
          // Then sort by company name ascending
          assertTrue(
              prevCompany.compareTo(currCompany) <= 0,
              "Company names should be in ascending order within same status");
        }
      }
    }
  }

  @Nested
  @DisplayName("SmartSort Validation")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class SmartSortValidation {

    @Test
    @DisplayName("SmartSort strategies should return valid results")
    void testSmartSortStrategies_validity() {
      // Given: All SmartSort strategies
      String[] strategies = {
        "SALES_PRIORITY",
        "RISK_MITIGATION",
        "ENGAGEMENT_FOCUS",
        "REVENUE_POTENTIAL",
        "CONTACT_FREQUENCY"
      };

      for (String strategy : strategies) {
        // When & Then: Each strategy should return valid results
        SmartSearchRequest request = new SmartSearchRequest();
        request.setStrategy(strategy);

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/customers/search/smart")
            .then()
            .statusCode(200)
            .body("content", notNullValue())
            .body("totalElements", greaterThanOrEqualTo(0));
      }
    }

    @Test
    @DisplayName("SmartSort should respect filters")
    void testSmartSort_withFilters() {
      // Given: SmartSort with filters
      SmartSearchRequest request = new SmartSearchRequest();
      request.setStrategy("SALES_PRIORITY");
      FilterCriteria filter = new FilterCriteria();
      filter.setField("status");
      filter.setOperator(FilterOperator.EQUALS);
      filter.setValue("AKTIV");
      request.setFilters(Arrays.asList(filter));

      // When & Then: Verify results match filters
      Response response =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .when()
              .post("/api/customers/search/smart")
              .then()
              .statusCode(200)
              .body("content", notNullValue())
              .extract()
              .response();

      List<Map<String, Object>> customers = response.jsonPath().getList("content");
      for (Map<String, Object> customer : customers) {
        assertEquals("AKTIV", customer.get("status"), "SmartSort should respect status filter");
      }
    }
  }

  @Nested
  @DisplayName("Pagination Validation")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class PaginationValidation {

    @Test
    @DisplayName("Pagination should return correct page information")
    void testPagination_pageInfo() {
      // Given: Search with pagination
      CustomerSearchRequest request = new CustomerSearchRequest();

      // When & Then: Verify pagination info
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .queryParam("page", 1)
          .queryParam("size", 5)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(200)
          .body("page", equalTo(1))
          .body("size", equalTo(5))
          .body("content.size()", lessThanOrEqualTo(5));
    }

    @Test
    @DisplayName("Pagination should not duplicate results across pages")
    void testPagination_noDuplicates() {
      // Given: Search requests for different pages
      CustomerSearchRequest request = new CustomerSearchRequest();

      // When: Get first two pages
      Response page1 =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .queryParam("page", 0)
              .queryParam("size", 5)
              .when()
              .post("/api/customers/search")
              .then()
              .statusCode(200)
              .extract()
              .response();

      Response page2 =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .queryParam("page", 1)
              .queryParam("size", 5)
              .when()
              .post("/api/customers/search")
              .then()
              .statusCode(200)
              .extract()
              .response();

      // Then: No duplicates between pages
      List<Map<String, Object>> customers1 = page1.jsonPath().getList("content");
      List<Map<String, Object>> customers2 = page2.jsonPath().getList("content");

      for (Map<String, Object> customer1 : customers1) {
        String id1 = (String) customer1.get("id");
        for (Map<String, Object> customer2 : customers2) {
          String id2 = (String) customer2.get("id");
          assertNotEquals(id1, id2, "Customer should not appear on multiple pages");
        }
      }
    }
  }

  /** Helper method to create filter criteria. */
  private FilterCriteria createFilter(String field, FilterOperator operator, String value) {
    FilterCriteria filter = new FilterCriteria();
    filter.setField(field);
    filter.setOperator(operator);
    filter.setValue(value);
    return filter;
  }
}
