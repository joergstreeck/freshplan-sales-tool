package de.freshplan.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.customer.service.dto.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Performance tests for CustomerSearchResource.
 *
 * <p>These tests verify that the search functionality performs well under load and with large
 * datasets. Some tests are disabled by default to avoid impacting regular test runs.
 */
@QuarkusTest
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
class CustomerSearchPerformanceTest {

  private static final int PERFORMANCE_THRESHOLD_MS = 2000; // 2 seconds
  private static final int CONCURRENT_USERS = 10;
  private static final int REQUESTS_PER_USER = 5;

  @Nested
  @DisplayName("Response Time Tests")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class ResponseTimeTests {

    @Test
    @DisplayName("Basic search should complete within performance threshold")
    void testBasicSearch_responseTime() {
      // Given: Basic search request
      CustomerSearchRequest request = new CustomerSearchRequest();

      // When & Then: Measure response time
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

      long responseTime = endTime - startTime;
      assertTrue(
          responseTime < PERFORMANCE_THRESHOLD_MS,
          "Response time "
              + responseTime
              + "ms exceeds threshold "
              + PERFORMANCE_THRESHOLD_MS
              + "ms");
    }

    @Test
    @DisplayName("Global search should complete within performance threshold")
    void testGlobalSearch_responseTime() {
      // Given: Global search request
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("Hotel");

      // When & Then: Measure response time
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

      long responseTime = endTime - startTime;
      assertTrue(
          responseTime < PERFORMANCE_THRESHOLD_MS,
          "Response time "
              + responseTime
              + "ms exceeds threshold "
              + PERFORMANCE_THRESHOLD_MS
              + "ms");
    }

    @Test
    @DisplayName("Complex filtered search should complete within performance threshold")
    void testComplexSearch_responseTime() {
      // Given: Complex search request with multiple filters
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("Restaurant");
      request.setFilters(
          Arrays.asList(
              createFilter("status", FilterOperator.EQUALS, "AKTIV"),
              createFilter("industry", FilterOperator.EQUALS, "RESTAURANT"),
              createFilter("expectedAnnualVolume", FilterOperator.GREATER_THAN, "20000")));

      // When & Then: Measure response time
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

      long responseTime = endTime - startTime;
      assertTrue(
          responseTime < PERFORMANCE_THRESHOLD_MS,
          "Response time "
              + responseTime
              + "ms exceeds threshold "
              + PERFORMANCE_THRESHOLD_MS
              + "ms");
    }

    @Test
    @DisplayName("SmartSort search should complete within performance threshold")
    void testSmartSort_responseTime() {
      // Given: SmartSort search request
      SmartSearchRequest request = new SmartSearchRequest();
      request.setStrategy("SALES_PRIORITY");
      request.setGlobalSearch("Hotel");

      // When & Then: Measure response time
      long startTime = System.currentTimeMillis();
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .when()
          .post("/api/customers/search/smart")
          .then()
          .statusCode(200)
          .body("content", notNullValue());
      long endTime = System.currentTimeMillis();

      long responseTime = endTime - startTime;
      assertTrue(
          responseTime < PERFORMANCE_THRESHOLD_MS,
          "Response time "
              + responseTime
              + "ms exceeds threshold "
              + PERFORMANCE_THRESHOLD_MS
              + "ms");
    }
  }

  @Nested
  @DisplayName("Pagination Performance Tests")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class PaginationPerformanceTests {

    @Test
    @DisplayName("First page should load quickly")
    void testFirstPage_responseTime() {
      // Given: First page request
      CustomerSearchRequest request = new CustomerSearchRequest();

      // When & Then: Measure response time
      long startTime = System.currentTimeMillis();
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .queryParam("page", 0)
          .queryParam("size", 20)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(200)
          .body("content", notNullValue());
      long endTime = System.currentTimeMillis();

      long responseTime = endTime - startTime;
      assertTrue(
          responseTime < 1000, // First page should be especially fast
          "First page response time " + responseTime + "ms exceeds 1000ms");
    }

    @Test
    @DisplayName("Middle page should load within reasonable time")
    void testMiddlePage_responseTime() {
      // Given: Middle page request
      CustomerSearchRequest request = new CustomerSearchRequest();

      // When & Then: Measure response time
      long startTime = System.currentTimeMillis();
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .queryParam("page", 5)
          .queryParam("size", 20)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(200)
          .body("content", notNullValue());
      long endTime = System.currentTimeMillis();

      long responseTime = endTime - startTime;
      assertTrue(
          responseTime < PERFORMANCE_THRESHOLD_MS,
          "Middle page response time "
              + responseTime
              + "ms exceeds threshold "
              + PERFORMANCE_THRESHOLD_MS
              + "ms");
    }

    @Test
    @DisplayName("Large page size should complete within reasonable time")
    void testLargePageSize_responseTime() {
      // Given: Large page size request
      CustomerSearchRequest request = new CustomerSearchRequest();

      // When & Then: Measure response time
      long startTime = System.currentTimeMillis();
      given()
          .contentType(ContentType.JSON)
          .body(request)
          .queryParam("page", 0)
          .queryParam("size", 100)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(200)
          .body("content", notNullValue());
      long endTime = System.currentTimeMillis();

      long responseTime = endTime - startTime;
      assertTrue(
          responseTime < PERFORMANCE_THRESHOLD_MS * 2, // Allow more time for large pages
          "Large page response time "
              + responseTime
              + "ms exceeds threshold "
              + (PERFORMANCE_THRESHOLD_MS * 2)
              + "ms");
    }
  }

  @Nested
  @DisplayName("Concurrent User Tests")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class ConcurrentUserTests {

    @Test
    @DisplayName("Multiple concurrent users should not degrade performance significantly")
    void testConcurrentUsers_performance() throws Exception {
      // Given: Multiple concurrent users
      ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
      List<Future<Long>> futures = new ArrayList<>();

      // When: Execute concurrent searches
      for (int i = 0; i < CONCURRENT_USERS; i++) {
        futures.add(
            executor.submit(
                () -> {
                  CustomerSearchRequest request = new CustomerSearchRequest();
                  request.setGlobalSearch("Hotel");

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

                  return endTime - startTime;
                }));
      }

      // Then: All requests should complete within reasonable time
      executor.shutdown();
      executor.awaitTermination(30, TimeUnit.SECONDS);

      for (Future<Long> future : futures) {
        long responseTime = future.get();
        assertTrue(
            responseTime < PERFORMANCE_THRESHOLD_MS * 2, // Allow more time for concurrent access
            "Concurrent user response time "
                + responseTime
                + "ms exceeds threshold "
                + (PERFORMANCE_THRESHOLD_MS * 2)
                + "ms");
      }
    }

    @Test
    @DisplayName("Repeated searches by same user should maintain consistent performance")
    void testRepeatedSearches_consistentPerformance() {
      // Given: Same search request repeated multiple times
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("Restaurant");

      List<Long> responseTimes = new ArrayList<>();

      // When: Execute multiple searches
      for (int i = 0; i < REQUESTS_PER_USER; i++) {
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

        responseTimes.add(endTime - startTime);
      }

      // Then: All response times should be within threshold
      for (int i = 0; i < responseTimes.size(); i++) {
        long responseTime = responseTimes.get(i);
        assertTrue(
            responseTime < PERFORMANCE_THRESHOLD_MS,
            "Request "
                + (i + 1)
                + " response time "
                + responseTime
                + "ms exceeds threshold "
                + PERFORMANCE_THRESHOLD_MS
                + "ms");
      }

      // Check that performance doesn't degrade over time
      double averageFirstHalf =
          responseTimes.subList(0, responseTimes.size() / 2).stream()
              .mapToLong(Long::longValue)
              .average()
              .orElse(0);
      double averageSecondHalf =
          responseTimes.subList(responseTimes.size() / 2, responseTimes.size()).stream()
              .mapToLong(Long::longValue)
              .average()
              .orElse(0);

      assertTrue(
          averageSecondHalf < averageFirstHalf * 1.5, // Allow 50% degradation
          "Performance degraded significantly: first half avg="
              + averageFirstHalf
              + "ms, second half avg="
              + averageSecondHalf
              + "ms");
    }
  }

  @Nested
  @DisplayName("Memory and Resource Tests")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class MemoryResourceTests {

    @Test
    @DisplayName("Search should not cause excessive memory usage")
    void testSearch_memoryUsage() {
      // Given: Multiple searches to test memory usage
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("Test");

      // When: Execute multiple searches and monitor memory
      Runtime runtime = Runtime.getRuntime();
      long initialMemory = runtime.totalMemory() - runtime.freeMemory();

      for (int i = 0; i < 20; i++) {
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/customers/search")
            .then()
            .statusCode(200)
            .body("content", notNullValue());
      }

      // Force garbage collection
      System.gc();
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      long finalMemory = runtime.totalMemory() - runtime.freeMemory();
      long memoryIncrease = finalMemory - initialMemory;

      // Then: Memory increase should be reasonable (less than 50MB)
      assertTrue(
          memoryIncrease < 50 * 1024 * 1024,
          "Memory usage increased by "
              + (memoryIncrease / 1024 / 1024)
              + "MB, which seems excessive");
    }

    @Test
    @DisplayName("Large result set should not cause memory issues")
    void testLargeResultSet_memoryUsage() {
      // Given: Search that returns many results
      CustomerSearchRequest request = new CustomerSearchRequest();
      // Use a broad search that should match many customers
      request.setGlobalSearch("a");

      // When & Then: Execute search with large page size
      Runtime runtime = Runtime.getRuntime();
      long initialMemory = runtime.totalMemory() - runtime.freeMemory();

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .queryParam("page", 0)
          .queryParam("size", 100)
          .when()
          .post("/api/customers/search")
          .then()
          .statusCode(200)
          .body("content", notNullValue());

      long finalMemory = runtime.totalMemory() - runtime.freeMemory();
      long memoryIncrease = finalMemory - initialMemory;

      // Memory increase should be reasonable for large result set
      assertTrue(
          memoryIncrease < 20 * 1024 * 1024,
          "Memory usage for large result set increased by "
              + (memoryIncrease / 1024 / 1024)
              + "MB");
    }
  }

  @Nested
  @DisplayName("Stress Tests")
  @Disabled("Heavy stress tests - run manually when needed")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class StressTests {

    @Test
    @DisplayName("High volume concurrent searches should maintain performance")
    void testHighVolumeConcurrentSearches() throws Exception {
      // Given: High volume concurrent searches
      ExecutorService executor = Executors.newFixedThreadPool(50);
      List<Future<Boolean>> futures = new ArrayList<>();

      // When: Execute many concurrent searches
      for (int i = 0; i < 100; i++) {
        futures.add(
            executor.submit(
                () -> {
                  CustomerSearchRequest request = new CustomerSearchRequest();
                  request.setGlobalSearch("Test" + Math.random());

                  try {
                    long startTime = System.currentTimeMillis();
                    Response response =
                        given()
                            .contentType(ContentType.JSON)
                            .body(request)
                            .when()
                            .post("/api/customers/search");
                    long endTime = System.currentTimeMillis();

                    return response.statusCode() == 200 && (endTime - startTime) < 5000;
                  } catch (Exception e) {
                    return false;
                  }
                }));
      }

      // Then: Most requests should succeed
      executor.shutdown();
      executor.awaitTermination(60, TimeUnit.SECONDS);

      long successCount =
          futures.stream()
              .mapToLong(
                  f -> {
                    try {
                      return f.get() ? 1 : 0;
                    } catch (Exception e) {
                      return 0;
                    }
                  })
              .sum();

      assertTrue(
          successCount > 80, // Allow some failures under stress
          "Only " + successCount + " out of 100 requests succeeded under stress");
    }

    @Test
    @DisplayName("Sustained load should maintain performance")
    void testSustainedLoad() throws Exception {
      // Given: Sustained load over time
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("Hotel");

      List<Long> responseTimes = new ArrayList<>();
      long testDuration = 30000; // 30 seconds
      long startTime = System.currentTimeMillis();

      // When: Execute searches continuously for test duration
      while (System.currentTimeMillis() - startTime < testDuration) {
        long requestStart = System.currentTimeMillis();
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/customers/search")
            .then()
            .statusCode(200)
            .body("content", notNullValue());
        long requestEnd = System.currentTimeMillis();

        responseTimes.add(requestEnd - requestStart);

        // Small delay to avoid overwhelming the server
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }

      // Then: Performance should remain stable
      double averageResponseTime =
          responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);

      assertTrue(
          averageResponseTime < PERFORMANCE_THRESHOLD_MS,
          "Average response time under sustained load "
              + averageResponseTime
              + "ms exceeds threshold "
              + PERFORMANCE_THRESHOLD_MS
              + "ms");
    }
  }

  @Nested
  @DisplayName("Database Performance Tests")
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  class DatabasePerformanceTests {

    @Test
    @DisplayName("Complex join queries should complete within reasonable time")
    void testComplexJoinQuery_performance() {
      // Given: Search that requires complex joins
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("Hotel");
      request.setFilters(
          Arrays.asList(
              createFilter("status", FilterOperator.EQUALS, "AKTIV"),
              createFilter("industry", FilterOperator.EQUALS, "HOTEL"),
              createFilter("expectedAnnualVolume", FilterOperator.GREATER_THAN, "50000"),
              createFilter("riskScore", FilterOperator.LESS_THAN, "50")));

      // When & Then: Measure response time
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

      long responseTime = endTime - startTime;
      assertTrue(
          responseTime < PERFORMANCE_THRESHOLD_MS,
          "Complex join query response time "
              + responseTime
              + "ms exceeds threshold "
              + PERFORMANCE_THRESHOLD_MS
              + "ms");
    }

    @Test
    @DisplayName("Full-text search should complete within reasonable time")
    void testFullTextSearch_performance() {
      // Given: Full-text search request
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("Hotel Restaurant Catering Berlin Munich");

      // When & Then: Measure response time
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

      long responseTime = endTime - startTime;
      assertTrue(
          responseTime < PERFORMANCE_THRESHOLD_MS,
          "Full-text search response time "
              + responseTime
              + "ms exceeds threshold "
              + PERFORMANCE_THRESHOLD_MS
              + "ms");
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
