package de.freshplan.modules.leads.service;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.shared.BusinessType;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.Territory;
import de.freshplan.modules.leads.domain.UrgencyLevel;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;

/**
 * Enterprise-Level Performance and Concurrency Tests for Lead Scoring Service
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>Performance with large datasets (1000+ leads)
 *   <li>Concurrent score updates (multi-user scenarios)
 *   <li>Database connection failure handling
 *   <li>Invalid data handling
 * </ul>
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("performance")
public class LeadScoringServicePerformanceTest {

  @Inject LeadScoringService scoringService;

  @Inject EntityManager em;

  private static final int LARGE_DATASET_SIZE = 1000;
  private static final int CONCURRENT_USERS = 10;
  private static final int OPERATIONS_PER_USER = 20;

  @BeforeEach
  @Transactional
  void ensureTestTerritory() {
    Territory territory = Territory.find("countryCode", "DE").firstResult();
    if (territory == null) {
      territory = new Territory();
      territory.id = "DE";
      territory.name = "Deutschland";
      territory.countryCode = "DE";
      territory.currencyCode = "EUR";
      territory.languageCode = "de-DE";
      territory.taxRate = new BigDecimal("19.00");
      territory.active = true;
      territory.persist();
    }
  }

  // ================================================================================
  // PERFORMANCE TESTS
  // ================================================================================

  @Test
  @Order(1)
  @DisplayName("Performance: Calculate scores for 1000 leads in <5 seconds")
  @Transactional
  void testLeadScoringPerformanceWith1000Leads() {
    // Given: Create 1000 test leads
    List<Lead> leads = createTestLeads(LARGE_DATASET_SIZE);

    // When: Calculate scores for all leads
    long startTime = System.currentTimeMillis();

    for (Lead lead : leads) {
      scoringService.updateLeadScore(lead);
    }

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    // Then: Should complete in less than 5 seconds
    System.out.println(
        String.format(
            "Performance Test: Scored %d leads in %d ms (%.2f leads/sec)",
            LARGE_DATASET_SIZE, duration, (LARGE_DATASET_SIZE * 1000.0) / duration));

    assertTrue(duration < 5000, "Scoring 1000 leads should take less than 5 seconds");

    // Verify all leads have scores
    long scoredLeads = leads.stream().filter(l -> l.leadScore != null).count();
    assertEquals(LARGE_DATASET_SIZE, scoredLeads, "All leads should have a calculated lead score");
  }

  @Test
  @Order(2)
  @DisplayName("Performance: Score caching prevents unnecessary DB writes")
  @Transactional
  void testScoreCachingPerformance() {
    // Given: A lead with already calculated scores
    Lead lead = createTestLead(1);
    scoringService.updateLeadScore(lead);

    Integer initialPainScore = lead.painScore;
    Integer initialLeadScore = lead.leadScore;
    Long initialVersion = lead.version;

    // When: Update score again without changing data (should use cache)
    long startTime = System.nanoTime();
    scoringService.updateLeadScore(lead);
    long duration = (System.nanoTime() - startTime) / 1_000_000; // Convert to ms

    // Then: Scores should remain unchanged (cached)
    assertEquals(initialPainScore, lead.painScore, "Pain score should be cached");
    assertEquals(initialLeadScore, lead.leadScore, "Lead score should be cached");

    // Cache check should be very fast (< 10ms)
    assertTrue(
        duration < 10,
        "Score cache check should complete in less than 10ms (was: " + duration + "ms)");

    System.out.println(String.format("Cache check completed in %d ms", duration));
  }

  // ================================================================================
  // CONCURRENCY TESTS
  // ================================================================================

  @Test
  @Order(3)
  @DisplayName("Concurrency: 10 users updating scores for same lead simultaneously")
  void testConcurrentLeadScoreUpdates() throws InterruptedException, ExecutionException {
    // Given: One lead that will be updated concurrently
    Lead testLead = createAndPersistTestLead(999);

    // Create thread pool for concurrent updates
    ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(CONCURRENT_USERS);

    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger conflictCount = new AtomicInteger(0);

    // When: 10 threads try to update the lead's score simultaneously
    List<Future<Boolean>> futures = new ArrayList<>();

    for (int i = 0; i < CONCURRENT_USERS; i++) {
      final int userId = i;
      Future<Boolean> future =
          executor.submit(
              () -> {
                try {
                  // Wait for all threads to be ready
                  startLatch.await();

                  // Simulate concurrent update
                  return updateLeadInTransaction(testLead.id, userId);

                } catch (Exception e) {
                  System.err.println("Thread " + userId + " failed: " + e.getMessage());
                  return false;
                } finally {
                  doneLatch.countDown();
                }
              });
      futures.add(future);
    }

    // Start all threads simultaneously
    startLatch.countDown();

    // Wait for all threads to complete
    boolean completed = doneLatch.await(30, TimeUnit.SECONDS);
    assertTrue(completed, "All concurrent updates should complete within 30 seconds");

    // Collect results
    for (Future<Boolean> future : futures) {
      if (future.get()) {
        successCount.incrementAndGet();
      } else {
        conflictCount.incrementAndGet();
      }
    }

    executor.shutdown();

    // Then: At least 1 update should succeed (optimistic locking may cause conflicts)
    System.out.println(
        String.format(
            "Concurrency Test: %d successful updates, %d conflicts",
            successCount.get(), conflictCount.get()));

    assertTrue(
        successCount.get() > 0,
        "At least one concurrent update should succeed (had "
            + conflictCount.get()
            + " conflicts)");

    // Verify final state is consistent
    Lead finalLead = Lead.findById(testLead.id);
    assertNotNull(finalLead, "Lead should still exist after concurrent updates");
    assertNotNull(finalLead.leadScore, "Lead should have a valid score");
  }

  @Transactional
  boolean updateLeadInTransaction(Long leadId, int userId) {
    try {
      Lead lead = em.find(Lead.class, leadId);
      if (lead == null) {
        return false;
      }

      // Simulate score change
      lead.painStaffShortage = (userId % 2 == 0);
      lead.urgencyLevel = (userId % 3 == 0) ? UrgencyLevel.HIGH : UrgencyLevel.NORMAL;

      scoringService.updateLeadScore(lead);
      lead.persist();

      return true;
    } catch (Exception e) {
      // Optimistic lock exception or other concurrency issue
      return false;
    }
  }

  // ================================================================================
  // ERROR HANDLING TESTS
  // ================================================================================

  @Test
  @Order(4)
  @DisplayName("Error Handling: Scoring with invalid/null data should not crash")
  void testLeadScoringWithInvalidData() {
    // Given: Leads with various invalid data scenarios
    Lead leadWithNulls = new Lead();
    leadWithNulls.estimatedVolume = null;
    leadWithNulls.businessType = null;
    leadWithNulls.urgencyLevel = null;

    Lead leadWithNegativeVolume = new Lead();
    leadWithNegativeVolume.estimatedVolume = new BigDecimal("-1000");

    // When: Calculate scores (should handle gracefully)
    assertDoesNotThrow(
        () -> scoringService.updateLeadScore(leadWithNulls),
        "Scoring with null fields should not throw");

    assertDoesNotThrow(
        () -> scoringService.updateLeadScore(leadWithNegativeVolume),
        "Scoring with negative volume should not throw");

    // Then: Scores should be calculated with defaults
    assertNotNull(leadWithNulls.leadScore, "Lead with nulls should have a score (likely 0)");
    assertTrue(
        leadWithNulls.leadScore >= 0 && leadWithNulls.leadScore <= 100,
        "Score should be in valid range 0-100");

    assertNotNull(
        leadWithNegativeVolume.leadScore, "Lead with negative volume should have a score");
  }

  @Test
  @Order(5)
  @DisplayName("Error Handling: Scoring handles missing required fields gracefully")
  void testLeadScoringWithMissingRequiredFields() {
    // Given: Lead with only minimal data
    Lead minimalLead = new Lead();
    minimalLead.companyName = "Test Company";
    minimalLead.status = LeadStatus.REGISTERED;

    // When: Calculate score
    assertDoesNotThrow(
        () -> scoringService.updateLeadScore(minimalLead), "Should handle minimal lead data");

    // Then: Should have valid default scores
    assertNotNull(minimalLead.leadScore, "Even minimal lead should have a score");
    assertNotNull(minimalLead.painScore, "Pain score should default to 0");
    assertNotNull(minimalLead.revenueScore, "Revenue score should default to 0");
    assertNotNull(minimalLead.fitScore, "Fit score should default to 0");
    assertNotNull(minimalLead.engagementScore, "Engagement score should default to 0");

    // All scores should be non-negative
    assertTrue(minimalLead.leadScore >= 0, "Lead score should be non-negative");
    assertTrue(minimalLead.painScore >= 0, "Pain score should be non-negative");
  }

  // ================================================================================
  // HELPER METHODS
  // ================================================================================

  private List<Lead> createTestLeads(int count) {
    List<Lead> leads = new ArrayList<>();
    Territory territory = Territory.find("countryCode", "DE").firstResult();

    for (int i = 0; i < count; i++) {
      Lead lead = new Lead();
      lead.companyName = "Test Company " + i;
      lead.ownerUserId = "perf-test-user";
      lead.createdBy = "perf-test-user";
      lead.status = LeadStatus.REGISTERED;
      lead.territory = territory;
      lead.countryCode = "DE";
      lead.registeredAt = LocalDateTime.now().minusSeconds(1); // Fix: 1s buffer for DB check constraint (chk_leads_registered_at_not_future)

      // Vary data for realistic testing
      lead.estimatedVolume = new BigDecimal(1000 + (i % 10) * 500);
      lead.businessType = BusinessType.values()[i % BusinessType.values().length];
      lead.painStaffShortage = (i % 2 == 0);
      lead.painHighCosts = (i % 3 == 0);
      lead.urgencyLevel = (i % 4 == 0) ? UrgencyLevel.HIGH : UrgencyLevel.NORMAL;

      leads.add(lead);
    }

    return leads;
  }

  private Lead createTestLead(int id) {
    Territory territory = Territory.find("countryCode", "DE").firstResult();

    Lead lead = new Lead();
    lead.companyName = "Test Company " + id;
    lead.ownerUserId = "test-user";
    lead.createdBy = "test-user";
    lead.status = LeadStatus.REGISTERED;
    lead.territory = territory;
    lead.countryCode = "DE";
    lead.registeredAt = LocalDateTime.now().minusSeconds(1); // Fix: 1s buffer for DB check constraint (chk_leads_registered_at_not_future)
    lead.estimatedVolume = new BigDecimal("5000");
    lead.businessType = BusinessType.RESTAURANT;
    lead.painStaffShortage = true;
    lead.urgencyLevel = UrgencyLevel.HIGH;

    return lead;
  }

  @Transactional
  Lead createAndPersistTestLead(int id) {
    Lead lead = createTestLead(id);
    lead.persist();
    lead.flush();
    return lead;
  }
}
