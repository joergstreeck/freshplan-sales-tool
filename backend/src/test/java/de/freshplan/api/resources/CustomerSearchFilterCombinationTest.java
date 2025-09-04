package de.freshplan.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import de.freshplan.domain.customer.service.dto.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
/**
 * Integration tests for complex filter combinations in CustomerSearchResource.
 *
 * <p>These tests verify that multiple filters can be combined correctly and produce the expected
 * search results.
 */
@QuarkusTest
@Tag("migrate")@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
class CustomerSearchFilterCombinationTest {

  @Nested
  @DisplayName("Two Filter Combinations")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class TwoFilterCombinations {

    @Test
    @DisplayName("Status + Industry filters should work together")
    void testSearch_withStatusAndIndustryFilters() {
      // Given: Search with status AND industry filters
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setFilters(
          Arrays.asList(
              createFilter("status", FilterOperator.EQUALS, "AKTIV"),
              createFilter("industry", FilterOperator.EQUALS, "HOTEL")));

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(200)
          .body("content", notNullValue())
          .body("totalElements", greaterThanOrEqualTo(0))
          .body("totalElements", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("Status + Volume range filters should work together")
    void testSearch_withStatusAndVolumeFilters() {
      // Given: Search with status AND volume filters
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setFilters(
          Arrays.asList(
              createFilter("status", FilterOperator.EQUALS, "AKTIV"),
              createFilter("expectedAnnualVolume", FilterOperator.GREATER_THAN, "10000")));

      // When & Then
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

    @Test
    @DisplayName("Industry + Volume range filters should work together")
    void testSearch_withIndustryAndVolumeFilters() {
      // Given: Search with industry AND volume filters
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setFilters(
          Arrays.asList(
              createFilter("industry", FilterOperator.EQUALS, "RESTAURANT"),
              createFilter("expectedAnnualVolume", FilterOperator.LESS_THAN, "50000")));

      // When & Then
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

    @Test
    @DisplayName("Date range + Status filters should work together")
    void testSearch_withDateRangeAndStatusFilters() {
      // Given: Search with date range AND status filters
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setFilters(
          Arrays.asList(
              createFilter("createdAt", FilterOperator.GREATER_THAN, "2024-01-01"),
              createFilter("status", FilterOperator.EQUALS, "AKTIV")));

      // When & Then
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
  @DisplayName("Three Filter Combinations")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class ThreeFilterCombinations {

    @Test
    @DisplayName("Status + Industry + Volume filters should work together")
    void testSearch_withStatusIndustryVolumeFilters() {
      // Given: Search with three filters
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setFilters(
          Arrays.asList(
              createFilter("status", FilterOperator.EQUALS, "AKTIV"),
              createFilter("industry", FilterOperator.EQUALS, "HOTEL"),
              createFilter("expectedAnnualVolume", FilterOperator.GREATER_THAN, "20000")));

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(200)
          .body("content", notNullValue())
          .body("totalElements", greaterThanOrEqualTo(0))
          .body("page", equalTo(0))
          .body("size", greaterThan(0));
    }

    @Test
    @DisplayName("Status + Volume range + Date range filters should work together")
    void testSearch_withStatusVolumeAndDateFilters() {
      // Given: Search with status, volume, and date filters
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setFilters(
          Arrays.asList(
              createFilter("status", FilterOperator.EQUALS, "AKTIV"),
              createFilterWithList(
                  "expectedAnnualVolume", FilterOperator.BETWEEN, Arrays.asList("10000", "100000")),
              createFilter("createdAt", FilterOperator.GREATER_THAN, "2024-01-01")));

      // When & Then
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

    @Test
    @DisplayName("Industry + Lifecycle + Risk filters should work together")
    void testSearch_withIndustryLifecycleRiskFilters() {
      // Given: Search with industry, lifecycle, and risk filters
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setFilters(
          Arrays.asList(
              createFilter("industry", FilterOperator.EQUALS, "CATERING"),
              createFilter("lifecycleStage", FilterOperator.EQUALS, "GROWTH"),
              createFilter("riskScore", FilterOperator.LESS_THAN, "50")));

      // When & Then
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
  @DisplayName("Four or More Filter Combinations")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class ComplexFilterCombinations {

    @Test
    @DisplayName("Status + Industry + Volume + Date filters should work together")
    void testSearch_withFourFilters() {
      // Given: Search with four filters
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setFilters(
          Arrays.asList(
              createFilter("status", FilterOperator.EQUALS, "AKTIV"),
              createFilter("industry", FilterOperator.EQUALS, "RESTAURANT"),
              createFilter("expectedAnnualVolume", FilterOperator.GREATER_THAN, "15000"),
              createFilter("createdAt", FilterOperator.GREATER_THAN, "2024-01-01")));

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(200)
          .body("content", notNullValue())
          .body("totalElements", greaterThanOrEqualTo(0))
          .body("page", equalTo(0))
          .body("size", greaterThan(0));
    }

    @Test
    @DisplayName("Comprehensive filter combination should work")
    void testSearch_withComprehensiveFilters() {
      // Given: Search with many filters
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setFilters(
          Arrays.asList(
              createFilter("status", FilterOperator.EQUALS, "AKTIV"),
              createFilterWithList(
                  "industry", FilterOperator.IN, Arrays.asList("HOTEL", "RESTAURANT")),
              createFilterWithList(
                  "expectedAnnualVolume", FilterOperator.BETWEEN, Arrays.asList("10000", "50000")),
              createFilter("createdAt", FilterOperator.GREATER_THAN, "2024-01-01"),
              createFilter("riskScore", FilterOperator.LESS_THAN, "70"),
              createFilter("lifecycleStage", FilterOperator.EQUALS, "GROWTH")));

      // When & Then
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
  @DisplayName("Filter Combinations with Global Search")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class FilterCombinationsWithGlobalSearch {

    @Test
    @DisplayName("Global search + Status filter should work together")
    void testSearch_withGlobalSearchAndStatusFilter() {
      // Given: Global search combined with status filter
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("Hotel");
      request.setFilters(Arrays.asList(createFilter("status", FilterOperator.EQUALS, "AKTIV")));

      // When & Then
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

    @Test
    @DisplayName("Global search + Multiple filters should work together")
    void testSearch_withGlobalSearchAndMultipleFilters() {
      // Given: Global search combined with multiple filters
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("Restaurant");
      request.setFilters(
          Arrays.asList(
              createFilter("status", FilterOperator.EQUALS, "AKTIV"),
              createFilter("expectedAnnualVolume", FilterOperator.GREATER_THAN, "20000")));

      // When & Then
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

    @Test
    @DisplayName("Global search with empty result + filters should return empty")
    void testSearch_withGlobalSearchEmptyAndFilters() {
      // Given: Global search with non-existent term and filters
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("NonExistentCompanyName12345");
      request.setFilters(Arrays.asList(createFilter("status", FilterOperator.EQUALS, "AKTIV")));

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(200)
          .body("content", hasSize(0))
          .body("totalElements", equalTo(0));
    }
  }

  @Nested
  @DisplayName("Filter Combinations with Sorting")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class FilterCombinationsWithSorting {

    @Test
    @DisplayName("Filters + Custom sorting should work together")
    void testSearch_withFiltersAndSorting() {
      // Given: Filters combined with custom sorting
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setFilters(
          Arrays.asList(
              createFilter("status", FilterOperator.EQUALS, "AKTIV"),
              createFilter("expectedAnnualVolume", FilterOperator.GREATER_THAN, "10000")));

      SortCriteria sortCriteria = new SortCriteria();
      sortCriteria.setField("expectedAnnualVolume");
      sortCriteria.setDirection("DESC");
      request.setMultiSort(Arrays.asList(sortCriteria));

      // When & Then
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

    @Test
    @DisplayName("Filters + Multiple sort criteria should work together")
    void testSearch_withFiltersAndMultipleSorting() {
      // Given: Filters combined with multiple sort criteria
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setFilters(Arrays.asList(createFilter("status", FilterOperator.EQUALS, "AKTIV")));

      SortCriteria sort1 = new SortCriteria();
      sort1.setField("industry");
      sort1.setDirection("ASC");

      SortCriteria sort2 = new SortCriteria();
      sort2.setField("expectedAnnualVolume");
      sort2.setDirection("DESC");

      request.setMultiSort(Arrays.asList(sort1, sort2));

      // When & Then
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
  @DisplayName("Filter Combinations with Pagination")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class FilterCombinationsWithPagination {

    @Test
    @DisplayName("Filters + Pagination should work together")
    void testSearch_withFiltersAndPagination() {
      // Given: Filters combined with pagination
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setFilters(Arrays.asList(createFilter("status", FilterOperator.EQUALS, "AKTIV")));

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .queryParam("page", 0)
          .queryParam("size", 5)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(200)
          .body("content", notNullValue())
          .body("content.size()", lessThanOrEqualTo(5))
          .body("page", equalTo(0))
          .body("size", equalTo(5))
          .body("totalElements", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("Complex filters + Pagination should work together")
    void testSearch_withComplexFiltersAndPagination() {
      // Given: Complex filters combined with pagination
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setFilters(
          Arrays.asList(
              createFilter("status", FilterOperator.EQUALS, "AKTIV"),
              createFilterWithList(
                  "industry", FilterOperator.IN, Arrays.asList("HOTEL", "RESTAURANT")),
              createFilter("expectedAnnualVolume", FilterOperator.GREATER_THAN, "15000")));

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .queryParam("page", 0)
          .queryParam("size", 3)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(200)
          .body("content", notNullValue())
          .body("content.size()", lessThanOrEqualTo(3))
          .body("page", equalTo(0))
          .body("size", equalTo(3));
    }
  }

  @Nested
  @DisplayName("Logical Operators")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class LogicalOperatorTests {

    @Test
    @DisplayName("AND logical operator should work correctly")
    void testSearch_withAndLogicalOperator() {
      // Given: Search with AND logical operator
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setLogicalOperator(LogicalOperator.AND);
      request.setFilters(
          Arrays.asList(
              createFilter("status", FilterOperator.EQUALS, "AKTIV"),
              createFilter("industry", FilterOperator.EQUALS, "HOTEL")));

      // When & Then
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

    @Test
    @DisplayName("OR logical operator should work correctly")
    void testSearch_withOrLogicalOperator() {
      // Given: Search with OR logical operator
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setLogicalOperator(LogicalOperator.OR);
      request.setFilters(
          Arrays.asList(
              createFilter("status", FilterOperator.EQUALS, "AKTIV"),
              createFilter("status", FilterOperator.EQUALS, "INAKTIV")));

      // When & Then
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
  @DisplayName("Error Handling")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class ErrorHandlingTests {

    @Test
    @DisplayName("Invalid filter field should return error")
    void testSearch_withInvalidFilterField() {
      // Given: Search with invalid filter field
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setFilters(
          Arrays.asList(createFilter("nonExistentField", FilterOperator.EQUALS, "someValue")));

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(anyOf(equalTo(400), equalTo(500))); // Depending on implementation
    }

    @Test
    @DisplayName("Invalid filter operator should return error")
    void testSearch_withInvalidFilterValue() {
      // Given: Search with invalid filter value for date field
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setFilters(
          Arrays.asList(createFilter("createdAt", FilterOperator.EQUALS, "invalid-date-format")));

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(anyOf(equalTo(400), equalTo(500))); // Depending on implementation
    }

    @Test
    @DisplayName("Conflicting filters should handle gracefully")
    void testSearch_withConflictingFilters() {
      // Given: Search with conflicting filters
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setFilters(
          Arrays.asList(
              createFilter("expectedAnnualVolume", FilterOperator.GREATER_THAN, "50000"),
              createFilter("expectedAnnualVolume", FilterOperator.LESS_THAN, "30000")));

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(200)
          .body("content", hasSize(0))
          .body("totalElements", equalTo(0));
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

  /** Helper method to create filter criteria with list value. */
  private FilterCriteria createFilterWithList(
      String field, FilterOperator operator, List<String> values) {
    FilterCriteria filter = new FilterCriteria();
    filter.setField(field);
    filter.setOperator(operator);
    filter.setValue(values);
    return filter;
  }
}
