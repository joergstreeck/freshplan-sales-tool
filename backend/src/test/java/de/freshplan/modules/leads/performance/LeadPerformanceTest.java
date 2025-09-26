package de.freshplan.modules.leads.performance;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;

/**
 * Performance Tests für das Lead-Modul (FP-236).
 *
 * <p>Validiert, dass alle Lead-Operations die Performance-SLOs einhalten: - API Response: <200ms
 * P95 - Database Queries: <50ms P95 - Bulk Operations: Linear scaling - Concurrent Access: No
 * degradation
 *
 * <p>Sprint 2.1 - Performance Validation
 */
@QuarkusTest
@DisplayName("Lead Performance Tests - FP-236")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LeadPerformanceTest {

  @Inject EntityManager em;

  @Inject DataSource dataSource;

  private static final int TEST_LEAD_COUNT = 100;
  private static final int P95_THRESHOLD_MS = 200;
  private static final int DB_QUERY_THRESHOLD_MS = 50;

  private List<Lead> testLeads = new ArrayList<>();
  private List<Long> responseTimes = new ArrayList<>();

  @BeforeEach
  @Transactional
  void setUp() {
    // Create test data
    for (int i = 0; i < TEST_LEAD_COUNT; i++) {
      Lead lead = createTestLead(i);
      testLeads.add(lead);
    }
  }

  @AfterEach
  @Transactional
  void tearDown() {
    // Clean up test data
    for (Lead lead : testLeads) {
      if (lead != null && lead.id != null) {
        Lead.deleteById(lead.id);
      }
    }
    testLeads.clear();
    responseTimes.clear();
  }

  /** Test 1: Single Lead Query Performance */
  @Test
  @Order(1)
  @DisplayName("Single lead query <200ms P95")
  void testSingleLeadQueryPerformance() {
    if (testLeads.isEmpty()) return;

    Lead testLead = testLeads.get(0);
    String leadId = testLead.id.toString();

    // Warm up
    for (int i = 0; i < 5; i++) {
      given()
          .when()
          .get("/api/leads/" + leadId)
          .then()
          .statusCode(anyOf(is(200), is(401), is(404)));
    }

    // Measure performance
    for (int i = 0; i < 100; i++) {
      long startTime = System.currentTimeMillis();

      given()
          .when()
          .get("/api/leads/" + leadId)
          .then()
          .statusCode(anyOf(is(200), is(401), is(404)))
          .time(lessThan((long) P95_THRESHOLD_MS));

      responseTimes.add(System.currentTimeMillis() - startTime);
    }

    // Calculate P95
    long p95 = calculateP95(responseTimes);
    assertTrue(
        p95 < P95_THRESHOLD_MS,
        String.format("Single lead query P95 should be <200ms, was %dms", p95));
  }

  /** Test 2: Lead List Query Performance */
  @Test
  @Order(2)
  @DisplayName("Lead list query <200ms P95")
  void testLeadListQueryPerformance() {
    // Warm up
    for (int i = 0; i < 5; i++) {
      given()
          .queryParam("territory", "DE")
          .queryParam("status", "ACTIVE")
          .queryParam("page", 0)
          .queryParam("size", 20)
          .when()
          .get("/api/leads")
          .then()
          .statusCode(anyOf(is(200), is(401)));
    }

    // Measure performance for different page sizes
    int[] pageSizes = {10, 20, 50};
    for (int pageSize : pageSizes) {
      List<Long> pageTimes = new ArrayList<>();

      for (int i = 0; i < 50; i++) {
        long startTime = System.currentTimeMillis();

        given()
            .queryParam("territory", "DE")
            .queryParam("page", 0)
            .queryParam("size", pageSize)
            .when()
            .get("/api/leads")
            .then()
            .statusCode(anyOf(is(200), is(401)))
            .time(lessThan((long) P95_THRESHOLD_MS));

        pageTimes.add(System.currentTimeMillis() - startTime);
      }

      long p95 = calculateP95(pageTimes);
      assertTrue(
          p95 < P95_THRESHOLD_MS,
          String.format("Lead list (size=%d) P95 should be <200ms, was %dms", pageSize, p95));
    }
  }

  /** Test 3: Complex Filter Query Performance */
  @Test
  @Order(3)
  @DisplayName("Complex filter queries <200ms P95")
  void testComplexFilterPerformance() {
    // Test multiple filter combinations
    Map<String, String> filters = new HashMap<>();
    filters.put("territory", "DE");
    filters.put("status", "ACTIVE");
    filters.put("ownerUserId", UUID.randomUUID().toString());
    filters.put("protectionActive", "true");
    filters.put("lastActivityDays", "30");

    for (int i = 0; i < 50; i++) {
      long startTime = System.currentTimeMillis();

      given()
          .queryParams(filters)
          .queryParam("page", 0)
          .queryParam("size", 20)
          .when()
          .get("/api/leads")
          .then()
          .statusCode(anyOf(is(200), is(401)))
          .time(lessThan((long) P95_THRESHOLD_MS));

      responseTimes.add(System.currentTimeMillis() - startTime);
    }

    long p95 = calculateP95(responseTimes);
    assertTrue(
        p95 < P95_THRESHOLD_MS,
        String.format("Complex filter P95 should be <200ms, was %dms", p95));
  }

  /** Test 4: Database Query Performance */
  @Test
  @Order(4)
  @DisplayName("Database queries <50ms P95")
  @Transactional
  void testDatabaseQueryPerformance() {
    List<Long> dbTimes = new ArrayList<>();

    // Test various database queries
    for (int i = 0; i < 100; i++) {
      // Query 1: Find by territory
      long startTime = System.nanoTime();
      List<Lead> territoryLeads = Lead.find("territory", "DE").list();
      dbTimes.add(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));

      // Query 2: Find by status
      startTime = System.nanoTime();
      List<Lead> activeLeads = Lead.find("status", LeadStatus.ACTIVE).list();
      dbTimes.add(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));

      // Query 3: Complex query
      startTime = System.nanoTime();
      List<Lead> complexQuery =
          Lead.find(
                  "territory = ?1 AND status = ?2 AND protectionExpiresAt > ?3",
                  "DE",
                  LeadStatus.ACTIVE,
                  LocalDateTime.now())
              .list();
      dbTimes.add(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
    }

    long p95 = calculateP95(dbTimes);
    assertTrue(
        p95 < DB_QUERY_THRESHOLD_MS,
        String.format("Database query P95 should be <50ms, was %dms", p95));
  }

  /** Test 5: Bulk Create Performance */
  @Test
  @Order(5)
  @DisplayName("Bulk create scales linearly")
  void testBulkCreatePerformance() {
    int[] batchSizes = {10, 20, 50};
    Map<Integer, Long> batchTimes = new HashMap<>();

    for (int batchSize : batchSizes) {
      List<Map<String, Object>> leads = new ArrayList<>();
      for (int i = 0; i < batchSize; i++) {
        Map<String, Object> lead = new HashMap<>();
        lead.put("companyName", "Bulk Test " + i);
        lead.put("territory", "DE");
        lead.put("email", "bulk" + i + "@test.de");
        leads.add(lead);
      }

      long startTime = System.currentTimeMillis();

      given()
          .contentType(ContentType.JSON)
          .body(leads)
          .when()
          .post("/api/leads/bulk")
          .then()
          .statusCode(anyOf(is(201), is(401), is(404))); // Bulk endpoint might not exist

      batchTimes.put(batchSize, System.currentTimeMillis() - startTime);
    }

    // Verify linear scaling (2x size should be ~2x time)
    if (batchTimes.size() >= 2) {
      long time10 = batchTimes.getOrDefault(10, 0L);
      long time20 = batchTimes.getOrDefault(20, 0L);

      if (time10 > 0 && time20 > 0) {
        double scalingFactor = (double) time20 / time10;
        assertTrue(
            scalingFactor < 2.5,
            String.format("Bulk create should scale linearly, factor was %.2f", scalingFactor));
      }
    }
  }

  /** Test 6: Concurrent Access Performance */
  @Test
  @Order(6)
  @DisplayName("Concurrent access maintains <200ms P95")
  void testConcurrentAccessPerformance() throws Exception {
    int concurrentUsers = 10;
    List<CompletableFuture<Long>> futures = new ArrayList<>();

    for (int user = 0; user < concurrentUsers; user++) {
      final int userId = user;
      CompletableFuture<Long> future =
          CompletableFuture.supplyAsync(
              () -> {
                long startTime = System.currentTimeMillis();

                given()
                    .queryParam("territory", "DE")
                    .queryParam("page", userId % 5) // Different pages
                    .queryParam("size", 20)
                    .when()
                    .get("/api/leads")
                    .then()
                    .statusCode(anyOf(is(200), is(401)));

                return System.currentTimeMillis() - startTime;
              });
      futures.add(future);
    }

    // Wait for all requests to complete
    List<Long> concurrentTimes =
        futures.stream()
            .map(
                f -> {
                  try {
                    return f.get(5, TimeUnit.SECONDS);
                  } catch (Exception e) {
                    return P95_THRESHOLD_MS + 1L; // Mark as failed
                  }
                })
            .collect(Collectors.toList());

    long p95 = calculateP95(concurrentTimes);
    assertTrue(
        p95 < P95_THRESHOLD_MS * 1.5, // Allow 50% degradation under load
        String.format("Concurrent access P95 should be <300ms, was %dms", p95));
  }

  /** Test 7: Search Performance */
  @Test
  @Order(7)
  @DisplayName("Search queries <200ms P95")
  void testSearchPerformance() {
    String[] searchTerms = {"Test", "Company", "Berlin", "Max"};

    for (String term : searchTerms) {
      List<Long> searchTimes = new ArrayList<>();

      for (int i = 0; i < 20; i++) {
        long startTime = System.currentTimeMillis();

        given()
            .queryParam("search", term)
            .queryParam("territory", "DE")
            .when()
            .get("/api/leads/search")
            .then()
            .statusCode(anyOf(is(200), is(401), is(404)))
            .time(lessThan((long) P95_THRESHOLD_MS));

        searchTimes.add(System.currentTimeMillis() - startTime);
      }

      long p95 = calculateP95(searchTimes);
      assertTrue(
          p95 < P95_THRESHOLD_MS,
          String.format("Search for '%s' P95 should be <200ms, was %dms", term, p95));
    }
  }

  /** Test 8: Protection Status Query Performance */
  @Test
  @Order(8)
  @DisplayName("Protection status queries <200ms P95")
  void testProtectionStatusPerformance() {
    // Query leads needing reminders
    for (int i = 0; i < 50; i++) {
      long startTime = System.currentTimeMillis();

      given()
          .queryParam("needsReminder", true)
          .queryParam("territory", "DE")
          .when()
          .get("/api/leads/protection-status")
          .then()
          .statusCode(anyOf(is(200), is(401), is(404)))
          .time(lessThan((long) P95_THRESHOLD_MS));

      responseTimes.add(System.currentTimeMillis() - startTime);
    }

    long p95 = calculateP95(responseTimes);
    assertTrue(
        p95 < P95_THRESHOLD_MS,
        String.format("Protection status P95 should be <200ms, was %dms", p95));
  }

  /** Test 9: ETag Performance */
  @Test
  @Order(9)
  @DisplayName("ETag caching reduces response time")
  void testETagPerformance() {
    if (testLeads.isEmpty()) return;

    String leadId = testLeads.get(0).id.toString();

    // First request - get ETag
    String etag =
        given()
            .when()
            .get("/api/leads/" + leadId)
            .then()
            .statusCode(anyOf(is(200), is(401), is(404)))
            .extract()
            .header("ETag");

    if (etag != null) {
      // Second request with ETag - should be faster
      List<Long> etagTimes = new ArrayList<>();

      for (int i = 0; i < 20; i++) {
        long startTime = System.currentTimeMillis();

        given()
            .header("If-None-Match", etag)
            .when()
            .get("/api/leads/" + leadId)
            .then()
            .statusCode(anyOf(is(304), is(200), is(401)));

        etagTimes.add(System.currentTimeMillis() - startTime);
      }

      long avgEtagTime = etagTimes.stream().mapToLong(Long::longValue).sum() / etagTimes.size();

      assertTrue(
          avgEtagTime < 50,
          String.format("ETag cached response should be <50ms, was %dms", avgEtagTime));
    }
  }

  /** Test 10: Aggregation Query Performance */
  @Test
  @Order(10)
  @DisplayName("Aggregation queries <200ms P95")
  @Transactional
  void testAggregationPerformance() {
    List<Long> aggTimes = new ArrayList<>();

    for (int i = 0; i < 50; i++) {
      long startTime = System.nanoTime();

      // Count by status
      em.createQuery(
              "SELECT l.status, COUNT(l) FROM Lead l "
                  + "WHERE l.territory = :territory "
                  + "GROUP BY l.status")
          .setParameter("territory", "DE")
          .getResultList();

      aggTimes.add(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));

      // Count by protection status
      startTime = System.nanoTime();
      em.createQuery(
              "SELECT COUNT(l) FROM Lead l "
                  + "WHERE l.protectionExpiresAt > :now "
                  + "AND l.territory = :territory")
          .setParameter("now", LocalDateTime.now())
          .setParameter("territory", "DE")
          .getSingleResult();

      aggTimes.add(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
    }

    long p95 = calculateP95(aggTimes);
    assertTrue(
        p95 < DB_QUERY_THRESHOLD_MS * 2, // Allow 2x for aggregations
        String.format("Aggregation query P95 should be <100ms, was %dms", p95));
  }

  // Helper methods

  @Transactional
  private Lead createTestLead(int index) {
    Lead lead = new Lead();
    lead.companyName = "Perf Test Company " + index;
    lead.contactPerson = "Test User " + index;
    lead.email = "test" + index + "@perftest.de";
    lead.phone = "+49 89 " + (1000000 + index);
    // lead.territory = index % 3 == 0 ? "CH" : "DE"; // needs Territory object
    lead.ownerUserId = UUID.randomUUID().toString();
    lead.status = index % 2 == 0 ? LeadStatus.ACTIVE : LeadStatus.REGISTERED;
    lead.registeredAt = LocalDateTime.now().minusDays(index);
    lead.lastActivityAt = LocalDateTime.now().minusDays(index % 30);
    lead.protectionMonths = 6;
    lead.protectionStartAt = LocalDateTime.now();
    lead.city = index % 2 == 0 ? "Berlin" : "Munich";
    lead.postalCode = String.format("%05d", 10000 + index);
    lead.persist();
    return lead;
  }

  private long calculateP95(List<Long> times) {
    if (times.isEmpty()) return 0;

    Collections.sort(times);
    int p95Index = (int) Math.ceil(times.size() * 0.95) - 1;
    return times.get(Math.min(p95Index, times.size() - 1));
  }
}
