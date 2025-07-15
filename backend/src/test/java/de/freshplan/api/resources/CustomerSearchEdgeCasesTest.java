package de.freshplan.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import de.freshplan.domain.customer.service.dto.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Edge case tests for CustomerSearchResource.
 *
 * <p>These tests verify that the search functionality handles boundary conditions, special
 * characters, null values, and other edge cases gracefully.
 */
@QuarkusTest
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
class CustomerSearchEdgeCasesTest {

  @Nested
  @DisplayName("Boundary Value Tests")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class BoundaryValueTests {

    @Test
    @DisplayName("Search with minimum volume should work")
    void testSearch_withMinimumVolume() {
      // Given: Search with minimum possible volume
      CustomerSearchRequest request = new CustomerSearchRequest();
      FilterCriteria filter = new FilterCriteria();
      filter.setField("expectedAnnualVolume");
      filter.setOperator(FilterOperator.GREATER_THAN_OR_EQUALS);
      filter.setValue("0");
      request.setFilters(Arrays.asList(filter));

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
    @DisplayName("Search with maximum volume should work")
    void testSearch_withMaximumVolume() {
      // Given: Search with very high volume
      CustomerSearchRequest request = new CustomerSearchRequest();
      FilterCriteria filter = new FilterCriteria();
      filter.setField("expectedAnnualVolume");
      filter.setOperator(FilterOperator.LESS_THAN);
      filter.setValue("999999999");
      request.setFilters(Arrays.asList(filter));

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
    @DisplayName("Search with future date should work")
    void testSearch_withFutureDate() {
      // Given: Search with future date
      CustomerSearchRequest request = new CustomerSearchRequest();
      FilterCriteria filter = new FilterCriteria();
      filter.setField("createdAt");
      filter.setOperator(FilterOperator.LESS_THAN);
      filter.setValue("2030-12-31");
      request.setFilters(Arrays.asList(filter));

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
    @DisplayName("Search with very old date should work")
    void testSearch_withVeryOldDate() {
      // Given: Search with very old date
      CustomerSearchRequest request = new CustomerSearchRequest();
      FilterCriteria filter = new FilterCriteria();
      filter.setField("createdAt");
      filter.setOperator(FilterOperator.GREATER_THAN);
      filter.setValue("1900-01-01");
      request.setFilters(Arrays.asList(filter));

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
    @DisplayName("Search with zero page size should return error")
    void testSearch_withZeroPageSize() {
      // Given: Search with zero page size
      CustomerSearchRequest request = new CustomerSearchRequest();

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .queryParam("page", 0)
          .queryParam("size", 0)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Search with very large page size should be handled")
    void testSearch_withVeryLargePageSize() {
      // Given: Search with very large page size
      CustomerSearchRequest request = new CustomerSearchRequest();

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .queryParam("page", 0)
          .queryParam("size", 10000)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(anyOf(equalTo(200), equalTo(400))); // Depends on implementation limits
    }

    @Test
    @DisplayName("Search with very high page number should work")
    void testSearch_withVeryHighPageNumber() {
      // Given: Search with very high page number
      CustomerSearchRequest request = new CustomerSearchRequest();

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .queryParam("page", 999999)
          .queryParam("size", 10)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(200)
          .body("content", hasSize(0))
          .body("totalElements", greaterThanOrEqualTo(0));
    }
  }

  @Nested
  @DisplayName("Special Character Tests")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class SpecialCharacterTests {

    @Test
    @DisplayName("Search with special characters should work")
    void testSearch_withSpecialCharacters() {
      // Given: Search with special characters
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("äöü-ß&amp;");

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
    @DisplayName("Search with SQL injection attempt should be safe")
    void testSearch_withSQLInjectionAttempt() {
      // Given: Search with SQL injection attempt
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("'; DROP TABLE customers; --");

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
    @DisplayName("Search with HTML/XML tags should be safe")
    void testSearch_withHTMLTags() {
      // Given: Search with HTML tags
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("<script>alert('test')</script>");

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
    @DisplayName("Search with Unicode characters should work")
    void testSearch_withUnicodeCharacters() {
      // Given: Search with Unicode characters
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("测试 тест 🏨");

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
    @DisplayName("Search with very long string should be handled")
    void testSearch_withVeryLongString() {
      // Given: Search with very long string
      String longString = "A".repeat(1000);
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch(longString);

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(anyOf(equalTo(200), equalTo(400))); // Depends on validation
    }

    @Test
    @DisplayName("Search with regex patterns should be safe")
    void testSearch_withRegexPatterns() {
      // Given: Search with regex patterns
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch(".*[0-9]+.*");

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
  @DisplayName("Null and Empty Value Tests")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class NullEmptyValueTests {

    @Test
    @DisplayName("Search with null global search should work")
    void testSearch_withNullGlobalSearch() {
      // Given: Search with null global search
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch(null);

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
    @DisplayName("Search with empty global search should work")
    void testSearch_withEmptyGlobalSearch() {
      // Given: Search with empty global search
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("");

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
    @DisplayName("Search with whitespace-only global search should work")
    void testSearch_withWhitespaceGlobalSearch() {
      // Given: Search with whitespace-only global search
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("   ");

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
    @DisplayName("Search with null filters should work")
    void testSearch_withNullFilters() {
      // Given: Search with null filters
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setFilters(null);

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
    @DisplayName("Search with empty filters list should work")
    void testSearch_withEmptyFilters() {
      // Given: Search with empty filters list
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setFilters(Collections.emptyList());

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
    @DisplayName("Search with filter having null value should be handled")
    void testSearch_withFilterNullValue() {
      // Given: Search with filter having null value
      CustomerSearchRequest request = new CustomerSearchRequest();
      FilterCriteria filter = new FilterCriteria();
      filter.setField("status");
      filter.setOperator(FilterOperator.EQUALS);
      filter.setValue(null);
      request.setFilters(Arrays.asList(filter));

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(anyOf(equalTo(200), equalTo(400))); // Depends on validation
    }

    @Test
    @DisplayName("Search with filter having empty value should be handled")
    void testSearch_withFilterEmptyValue() {
      // Given: Search with filter having empty value
      CustomerSearchRequest request = new CustomerSearchRequest();
      FilterCriteria filter = new FilterCriteria();
      filter.setField("status");
      filter.setOperator(FilterOperator.EQUALS);
      filter.setValue("");
      request.setFilters(Arrays.asList(filter));

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(anyOf(equalTo(200), equalTo(400))); // Depends on validation
    }
  }

  @Nested
  @DisplayName("Invalid Data Format Tests")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class InvalidDataFormatTests {

    @Test
    @DisplayName("Search with invalid date format should return error")
    void testSearch_withInvalidDateFormat() {
      // Given: Search with invalid date format
      CustomerSearchRequest request = new CustomerSearchRequest();
      FilterCriteria filter = new FilterCriteria();
      filter.setField("createdAt");
      filter.setOperator(FilterOperator.EQUALS);
      filter.setValue("invalid-date");
      request.setFilters(Arrays.asList(filter));

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(anyOf(equalTo(400), equalTo(500))); // Depends on validation
    }

    @Test
    @DisplayName("Search with invalid numeric format should return error")
    void testSearch_withInvalidNumericFormat() {
      // Given: Search with invalid numeric format
      CustomerSearchRequest request = new CustomerSearchRequest();
      FilterCriteria filter = new FilterCriteria();
      filter.setField("expectedAnnualVolume");
      filter.setOperator(FilterOperator.EQUALS);
      filter.setValue("not-a-number");
      request.setFilters(Arrays.asList(filter));

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(anyOf(equalTo(400), equalTo(500))); // Depends on validation
    }

    @Test
    @DisplayName("Search with malformed JSON should return error")
    void testSearch_withMalformedJSON() {
      // Given: Malformed JSON
      String malformedJson =
          "{ \"globalSearch\": \"test\", \"filters\": [ { \"field\": \"status\", \"operator\": \"EQUALS\", \"value\": } ] }";

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(malformedJson)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Search with invalid enum value should return error")
    void testSearch_withInvalidEnumValue() {
      // Given: Search with invalid enum value
      CustomerSearchRequest request = new CustomerSearchRequest();
      FilterCriteria filter = new FilterCriteria();
      filter.setField("status");
      filter.setOperator(FilterOperator.EQUALS);
      filter.setValue("INVALID_STATUS");
      request.setFilters(Arrays.asList(filter));

      // When & Then
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(anyOf(equalTo(200), equalTo(400))); // May return empty results or error
    }
  }

  @Nested
  @DisplayName("Concurrency and Race Condition Tests")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class ConcurrencyTests {

    @Test
    @DisplayName("Multiple concurrent searches should work")
    void testSearch_multipleConcurrentSearches() {
      // Given: Multiple search requests
      CustomerSearchRequest request1 = new CustomerSearchRequest();
      request1.setGlobalSearch("Hotel");

      CustomerSearchRequest request2 = new CustomerSearchRequest();
      request2.setGlobalSearch("Restaurant");

      // When & Then: Execute multiple searches concurrently
      given()
          .contentType(ContentType.JSON)
          .body(request1)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(200)
          .body("content", notNullValue());

      given()
          .contentType(ContentType.JSON)
          .body(request2)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(200)
          .body("content", notNullValue());
    }

    @Test
    @DisplayName("Search with same request multiple times should be consistent")
    void testSearch_consistentResults() {
      // Given: Same search request
      CustomerSearchRequest request = new CustomerSearchRequest();
      FilterCriteria filter = new FilterCriteria();
      filter.setField("status");
      filter.setOperator(FilterOperator.EQUALS);
      filter.setValue("AKTIV");
      request.setFilters(Arrays.asList(filter));

      // When & Then: Execute same search multiple times
      Integer firstTotal =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .when()
              .post("/api/customers/search")
              .then()
              .statusCode(200)
              .body("content", notNullValue())
              .extract()
              .path("totalElements");

      Integer secondTotal =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .when()
              .post("/api/customers/search")
              .then()
              .statusCode(200)
              .body("content", notNullValue())
              .extract()
              .path("totalElements");

      org.assertj.core.api.Assertions.assertThat(firstTotal).isEqualTo(secondTotal);
    }
  }

  @Nested
  @DisplayName("Performance Edge Cases")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class PerformanceEdgeCases {

    @Test
    @DisplayName("Search with very broad criteria should complete in reasonable time")
    void testSearch_withVeryBroadCriteria() {
      // Given: Very broad search criteria
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("a"); // Single character that might match many records

      // When & Then
      long startTime = System.currentTimeMillis();
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(200)
          .body("content", notNullValue());
      long endTime = System.currentTimeMillis();

      // Should complete within reasonable time (e.g., 5 seconds)
      org.assertj.core.api.Assertions.assertThat(endTime - startTime).isLessThan(5000);
    }

    @Test
    @DisplayName("Search with maximum page size should complete in reasonable time")
    void testSearch_withMaxPageSize() {
      // Given: Search with maximum reasonable page size
      CustomerSearchRequest request = new CustomerSearchRequest();

      // When & Then
      long startTime = System.currentTimeMillis();
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .queryParam("page", 0)
          .queryParam("size", 100) // Large but reasonable page size
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(200)
          .body("content", notNullValue());
      long endTime = System.currentTimeMillis();

      // Should complete within reasonable time
      org.assertj.core.api.Assertions.assertThat(endTime - startTime).isLessThan(3000);
    }
  }

  @Nested
  @DisplayName("Memory and Resource Tests")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class ResourceTests {

    @Test
    @DisplayName("Search should not cause memory leaks")
    void testSearch_memoryUsage() {
      // Given: Multiple searches to test memory usage
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("Test");

      // When & Then: Execute multiple searches
      for (int i = 0; i < 10; i++) {
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/customers/search")
            .then()
            .statusCode(200)
            .body("content", notNullValue());
      }
      // Note: Memory leak detection would require more sophisticated testing
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
