package de.freshplan.modules.leads.performance;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.Territory;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;

/**
 * Performance Validation Tests für Lead-Queries (FP-236).
 *
 * <p>Requirement: Lead-Queries <200ms P95 auf CQRS Foundation Diese Tests validieren die
 * Performance-Anforderungen aus Sprint 2.1
 */
@QuarkusTest
@DisplayName("Lead Performance Validation - FP-236")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LeadPerformanceValidationTest {

  @Inject DataSource dataSource;

  private Territory territoryDE;
  private List<Lead> testLeads = new ArrayList<>();
  private static final int TEST_LEAD_COUNT = 100;
  private static final long P95_THRESHOLD_MS = 200;

  @BeforeEach
  @Transactional
  void setUp() {
    // Setup territory
    territoryDE = Territory.findById("DE");
    if (territoryDE == null) {
      territoryDE = new Territory();
      territoryDE.id = "DE";
      territoryDE.name = "Deutschland";
      territoryDE.countryCode = "DE";
      territoryDE.currencyCode = "EUR";
      territoryDE.taxRate = new java.math.BigDecimal("19.00");
      territoryDE.languageCode = "de-DE";
      territoryDE.active = true;
      territoryDE.persist();
    }

    // Create test data for performance testing
    for (int i = 0; i < TEST_LEAD_COUNT; i++) {
      Lead lead = new Lead();
      lead.companyName = "Test Company " + i;
      lead.contactPerson = "Contact " + i;
      lead.email = "contact" + i + "@test.de";
      lead.territory = territoryDE;
      lead.ownerUserId = UUID.randomUUID().toString();
      lead.status = i % 2 == 0 ? LeadStatus.REGISTERED : LeadStatus.ACTIVE;
      lead.registeredAt = LocalDateTime.now().minusDays(i);
      lead.protectionStartAt = LocalDateTime.now().minusDays(i);
      lead.protectionMonths = 6;
      lead.createdAt = LocalDateTime.now().minusDays(i);
      lead.createdBy = lead.ownerUserId;
      lead.persist();
      testLeads.add(lead);
    }
  }

  @AfterEach
  @Transactional
  void tearDown() {
    // Clean up test data
    for (Lead lead : testLeads) {
      Lead.deleteById(lead.id);
    }
    testLeads.clear();
  }

  @Test
  @Order(1)
  @DisplayName("Find by territory performs under P95 threshold")
  @Transactional
  void testFindByTerritoryPerformance() {
    List<Long> executionTimes = new ArrayList<>();

    // Warm-up runs
    for (int i = 0; i < 5; i++) {
      Lead.find("territory", territoryDE).list();
    }

    // Measure performance
    for (int i = 0; i < 20; i++) {
      long startTime = System.currentTimeMillis();
      List<Lead> leads = Lead.find("territory", territoryDE).list();
      long endTime = System.currentTimeMillis();
      executionTimes.add(endTime - startTime);

      assertFalse(leads.isEmpty(), "Should find leads");
    }

    // Calculate P95
    Collections.sort(executionTimes);
    int p95Index = (int) Math.ceil(executionTimes.size() * 0.95) - 1;
    long p95Time = executionTimes.get(p95Index);

    System.out.println("Find by territory P95: " + p95Time + "ms");
    assertTrue(
        p95Time < P95_THRESHOLD_MS,
        "P95 (" + p95Time + "ms) should be under " + P95_THRESHOLD_MS + "ms");
  }

  @Test
  @Order(2)
  @DisplayName("Find by status performs under P95 threshold")
  @Transactional
  void testFindByStatusPerformance() {
    List<Long> executionTimes = new ArrayList<>();

    // Warm-up runs
    for (int i = 0; i < 5; i++) {
      Lead.find("status", LeadStatus.ACTIVE).list();
    }

    // Measure performance
    for (int i = 0; i < 20; i++) {
      long startTime = System.currentTimeMillis();
      List<Lead> leads = Lead.find("status", LeadStatus.ACTIVE).list();
      long endTime = System.currentTimeMillis();
      executionTimes.add(endTime - startTime);

      assertFalse(leads.isEmpty(), "Should find active leads");
    }

    // Calculate P95
    Collections.sort(executionTimes);
    int p95Index = (int) Math.ceil(executionTimes.size() * 0.95) - 1;
    long p95Time = executionTimes.get(p95Index);

    System.out.println("Find by status P95: " + p95Time + "ms");
    assertTrue(
        p95Time < P95_THRESHOLD_MS,
        "P95 (" + p95Time + "ms) should be under " + P95_THRESHOLD_MS + "ms");
  }

  @Test
  @Order(3)
  @DisplayName("Find by owner performs under P95 threshold")
  @Transactional
  void testFindByOwnerPerformance() {
    List<Long> executionTimes = new ArrayList<>();
    String testOwnerId = testLeads.get(0).ownerUserId;

    // Warm-up runs
    for (int i = 0; i < 5; i++) {
      Lead.find("ownerUserId", testOwnerId).list();
    }

    // Measure performance
    for (int i = 0; i < 20; i++) {
      long startTime = System.currentTimeMillis();
      List<Lead> leads = Lead.find("ownerUserId", testOwnerId).list();
      long endTime = System.currentTimeMillis();
      executionTimes.add(endTime - startTime);

      assertFalse(leads.isEmpty(), "Should find leads by owner");
    }

    // Calculate P95
    Collections.sort(executionTimes);
    int p95Index = (int) Math.ceil(executionTimes.size() * 0.95) - 1;
    long p95Time = executionTimes.get(p95Index);

    System.out.println("Find by owner P95: " + p95Time + "ms");
    assertTrue(
        p95Time < P95_THRESHOLD_MS,
        "P95 (" + p95Time + "ms) should be under " + P95_THRESHOLD_MS + "ms");
  }

  @Test
  @Order(4)
  @DisplayName("Complex query with multiple criteria performs under threshold")
  @Transactional
  void testComplexQueryPerformance() {
    List<Long> executionTimes = new ArrayList<>();

    // Warm-up runs
    for (int i = 0; i < 5; i++) {
      Lead.find("territory = ?1 and status = ?2", territoryDE, LeadStatus.ACTIVE).list();
    }

    // Measure performance
    for (int i = 0; i < 20; i++) {
      long startTime = System.currentTimeMillis();
      List<Lead> leads =
          Lead.find("territory = ?1 and status = ?2", territoryDE, LeadStatus.ACTIVE).list();
      long endTime = System.currentTimeMillis();
      executionTimes.add(endTime - startTime);

      assertFalse(leads.isEmpty(), "Should find leads with complex criteria");
    }

    // Calculate P95
    Collections.sort(executionTimes);
    int p95Index = (int) Math.ceil(executionTimes.size() * 0.95) - 1;
    long p95Time = executionTimes.get(p95Index);

    System.out.println("Complex query P95: " + p95Time + "ms");
    assertTrue(
        p95Time < P95_THRESHOLD_MS,
        "P95 (" + p95Time + "ms) should be under " + P95_THRESHOLD_MS + "ms");
  }

  @Test
  @Order(5)
  @DisplayName("Pagination query performs under threshold")
  @Transactional
  void testPaginationPerformance() {
    List<Long> executionTimes = new ArrayList<>();

    // Warm-up runs
    for (int i = 0; i < 5; i++) {
      Lead.findAll().page(0, 10).list();
    }

    // Measure performance for different pages
    for (int page = 0; page < 10; page++) {
      long startTime = System.currentTimeMillis();
      List<Lead> leads = Lead.findAll().page(page, 10).list();
      long endTime = System.currentTimeMillis();
      executionTimes.add(endTime - startTime);

      assertNotNull(leads, "Should return paginated results");
    }

    // Calculate P95
    Collections.sort(executionTimes);
    int p95Index = (int) Math.ceil(executionTimes.size() * 0.95) - 1;
    long p95Time = executionTimes.get(p95Index);

    System.out.println("Pagination P95: " + p95Time + "ms");
    assertTrue(
        p95Time < P95_THRESHOLD_MS,
        "P95 (" + p95Time + "ms) should be under " + P95_THRESHOLD_MS + "ms");
  }
}
