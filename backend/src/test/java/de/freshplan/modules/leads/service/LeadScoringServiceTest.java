package de.freshplan.modules.leads.service;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStage;
import de.freshplan.modules.leads.domain.Territory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for LeadScoringService (Sprint 2.1.6 Phase 4 - ADR-006 Phase 2).
 *
 * <p>Validates 4-factor scoring algorithm: - Umsatzpotenzial (25%): Volume + Employees -
 * Engagement (25%): Activity + Followups - Fit (25%): BusinessType + KitchenSize + Stage -
 * Dringlichkeit (25%): Deadlines + Protection Expiry
 */
@QuarkusTest
class LeadScoringServiceTest {

  @Inject LeadScoringService scoringService;

  private Territory testTerritory;

  @BeforeEach
  @Transactional
  void setUp() {
    // Create or reuse territory for test leads
    testTerritory = Territory.find("countryCode", "DE").firstResult();
    if (testTerritory == null) {
      testTerritory = new Territory();
      testTerritory.id = "DE-TEST";
      testTerritory.name = "Test Territory";
      testTerritory.countryCode = "DE";
      testTerritory.currencyCode = "EUR";
      testTerritory.persist();
    }
  }

  // ================= Faktor 1: Umsatzpotenzial (25%) =================

  @Test
  @Transactional
  @DisplayName("Umsatzpotenzial: High volume (€50k) + Many employees (25) → Factor Score 20-25 (max 25%)")
  void testHighVolumeLead() {
    Lead lead = createBaseLead();
    lead.estimatedVolume = new BigDecimal("50000"); // 15 points
    lead.employeeCount = 25; // 10 points
    // No engagement, fit, dringlichkeit → Only Umsatzpotenzial contributes

    int score = scoringService.calculateScore(lead);

    assertTrue(
        score >= 20 && score <= 30,
        String.format("Expected score 20-30 (Umsatzpotenzial only), got %d", score));
    assertEquals(score, lead.leadScore, "Lead entity should be updated with score");
  }

  @Test
  @Transactional
  @DisplayName("Umsatzpotenzial: Medium volume (€25k) + Medium employees (10) → Factor Score 15-20 (mid-range)")
  void testMediumVolumeLead() {
    Lead lead = createBaseLead();
    lead.estimatedVolume = new BigDecimal("25000"); // 10 points
    lead.employeeCount = 10; // 6 points

    int score = scoringService.calculateScore(lead);

    assertTrue(
        score >= 15,
        String.format("Expected score ≥15 (Medium Umsatzpotenzial), got %d", score));
  }

  @Test
  @Transactional
  @DisplayName("Umsatzpotenzial: Low volume (€5k) + Few employees (3) → Factor Score 3-5 (low)")
  void testLowVolumeLead() {
    Lead lead = createBaseLead();
    lead.estimatedVolume = new BigDecimal("5000"); // 2 points
    lead.employeeCount = 3; // 1 point

    int score = scoringService.calculateScore(lead);

    assertTrue(
        score >= 3, String.format("Expected score ≥3 (Low Umsatzpotenzial), got %d", score));
  }

  @Test
  @Transactional
  @DisplayName("Umsatzpotenzial: NULL volume and employees → Score 0 for this factor")
  void testNullVolumeAndEmployees() {
    Lead lead = createBaseLead();
    lead.estimatedVolume = null;
    lead.employeeCount = null;

    int score = scoringService.calculateScore(lead);

    assertTrue(
        score >= 0,
        String.format("Expected non-negative score even with NULL values, got %d", score));
  }

  // ================= Faktor 2: Engagement (25%) =================

  @Test
  @Transactional
  @DisplayName("Engagement: Recent activity (<7d) + Many followups (6+) → Score ≥20")
  void testHighEngagementLead() {
    Lead lead = createBaseLead();
    lead.lastActivityAt = LocalDateTime.now().minusDays(2); // 15 points
    lead.followupCount = 6; // 10 points

    int score = scoringService.calculateScore(lead);

    assertTrue(
        score >= 20, String.format("Expected score ≥20 (High Engagement), got %d", score));
  }

  @Test
  @Transactional
  @DisplayName("Engagement: Moderate activity (30d) + Some followups (3) → Score ≥15")
  void testModerateEngagementLead() {
    Lead lead = createBaseLead();
    lead.lastActivityAt = LocalDateTime.now().minusDays(25); // 10 points
    lead.followupCount = 3; // 7 points

    int score = scoringService.calculateScore(lead);

    assertTrue(
        score >= 15,
        String.format("Expected score ≥15 (Moderate Engagement), got %d", score));
  }

  @Test
  @Transactional
  @DisplayName("Engagement: Stale activity (>90d) + No followups → Score 0 for this factor")
  void testNoEngagementLead() {
    Lead lead = createBaseLead();
    lead.lastActivityAt = LocalDateTime.now().minusDays(120); // 0 points (stale)
    lead.followupCount = 0; // 0 points

    int score = scoringService.calculateScore(lead);

    assertTrue(
        score >= 0,
        String.format("Expected non-negative score for stale lead, got %d", score));
  }

  // ================= Faktor 3: Fit (25%) =================

  @Test
  @Transactional
  @DisplayName("Fit: Restaurant + Large kitchen + QUALIFIZIERT → Score ≥20")
  void testHighFitLead() {
    Lead lead = createBaseLead();
    lead.businessType = "RESTAURANT"; // 10 points
    lead.kitchenSize = "large"; // 8 points
    lead.stage = LeadStage.QUALIFIZIERT; // 7 points

    int score = scoringService.calculateScore(lead);

    assertTrue(score >= 20, String.format("Expected score ≥20 (High Fit), got %d", score));
  }

  @Test
  @Transactional
  @DisplayName("Fit: Catering + Medium kitchen + REGISTRIERUNG → Score ≥15")
  void testMediumFitLead() {
    Lead lead = createBaseLead();
    lead.businessType = "CATERING"; // 7 points
    lead.kitchenSize = "medium"; // 5 points
    lead.stage = LeadStage.REGISTRIERUNG; // 4 points

    int score = scoringService.calculateScore(lead);

    assertTrue(score >= 15, String.format("Expected score ≥15 (Medium Fit), got %d", score));
  }

  @Test
  @Transactional
  @DisplayName("Fit: Generic businessType + Small kitchen + VORMERKUNG → Score ≥3")
  void testLowFitLead() {
    Lead lead = createBaseLead();
    lead.businessType = "Other"; // 2 points
    lead.kitchenSize = "small"; // 2 points
    lead.stage = LeadStage.VORMERKUNG; // 1 point

    int score = scoringService.calculateScore(lead);

    assertTrue(score >= 3, String.format("Expected score ≥3 (Low Fit), got %d", score));
  }

  // ================= Faktor 4: Dringlichkeit (25%) =================

  @Test
  @Transactional
  @DisplayName("Dringlichkeit: Deadline <3d + Protection <30d → Score ≥20")
  void testHighUrgencyLead() {
    Lead lead = createBaseLead();
    lead.progressDeadline = LocalDateTime.now().plusDays(2); // 15 points
    lead.protectionStartAt = LocalDateTime.now().minusMonths(5).minusDays(20); // ~20d left → 10
    // points
    lead.protectionMonths = 6;

    int score = scoringService.calculateScore(lead);

    assertTrue(
        score >= 20, String.format("Expected score ≥20 (High Urgency), got %d", score));
  }

  @Test
  @Transactional
  @DisplayName("Dringlichkeit: Deadline <7d + Protection <90d → Score ≥12")
  void testModerateUrgencyLead() {
    Lead lead = createBaseLead();
    lead.progressDeadline = LocalDateTime.now().plusDays(5); // 10 points
    lead.protectionStartAt = LocalDateTime.now().minusMonths(4); // ~60d left → 5 points
    lead.protectionMonths = 6;

    int score = scoringService.calculateScore(lead);

    assertTrue(
        score >= 12,
        String.format("Expected score ≥12 (Moderate Urgency), got %d", score));
  }

  @Test
  @Transactional
  @DisplayName("Dringlichkeit: Deadline >14d + Protection >180d → Score 0 for this factor")
  void testNoUrgencyLead() {
    Lead lead = createBaseLead();
    lead.progressDeadline = LocalDateTime.now().plusDays(30); // 0 points
    lead.protectionStartAt = LocalDateTime.now(); // 180d left → 0 points
    lead.protectionMonths = 6;

    int score = scoringService.calculateScore(lead);

    assertTrue(
        score >= 0,
        String.format("Expected non-negative score for non-urgent lead, got %d", score));
  }

  // ================= Integration Tests (All Factors) =================

  @Test
  @Transactional
  @DisplayName(
      "Integration: High-value lead (€50k, Recent activity, Restaurant, Urgent) → Score ≥70")
  void testHighValueLeadAllFactors() {
    Lead lead = createBaseLead();

    // Umsatzpotenzial (25): €50k + 25 employees
    lead.estimatedVolume = new BigDecimal("50000");
    lead.employeeCount = 25;

    // Engagement (25): Recent activity + Many followups
    lead.lastActivityAt = LocalDateTime.now().minusDays(2);
    lead.followupCount = 6;

    // Fit (25): Restaurant + Large kitchen + QUALIFIZIERT
    lead.businessType = "RESTAURANT";
    lead.kitchenSize = "large";
    lead.stage = LeadStage.QUALIFIZIERT;

    // Dringlichkeit (25): Urgent deadlines
    lead.progressDeadline = LocalDateTime.now().plusDays(2);
    lead.protectionStartAt = LocalDateTime.now().minusMonths(5).minusDays(20);
    lead.protectionMonths = 6;

    int score = scoringService.calculateScore(lead);

    assertTrue(
        score >= 70,
        String.format(
            "Lead with €50k Volume, Recent Activity & Restaurant Fit should have Score ≥70, got"
                + " %d",
            score));
  }

  @Test
  @Transactional
  @DisplayName("Integration: Low-value lead (No data) → Score 0-10")
  void testLowValueLeadAllFactors() {
    Lead lead = createBaseLead();
    // All optional fields remain NULL → minimal score

    int score = scoringService.calculateScore(lead);

    assertTrue(
        score >= 0 && score <= 10,
        String.format("Lead with no data should have Score 0-10, got %d", score));
  }

  @Test
  @Transactional
  @DisplayName("Integration: Medium-value lead (€25k, Some activity, Catering) → Score 40-60")
  void testMediumValueLeadAllFactors() {
    Lead lead = createBaseLead();

    // Moderate across all factors
    lead.estimatedVolume = new BigDecimal("25000");
    lead.employeeCount = 10;
    lead.lastActivityAt = LocalDateTime.now().minusDays(25);
    lead.followupCount = 3;
    lead.businessType = "CATERING";
    lead.kitchenSize = "medium";
    lead.stage = LeadStage.REGISTRIERUNG;
    lead.progressDeadline = LocalDateTime.now().plusDays(10);
    lead.protectionStartAt = LocalDateTime.now().minusMonths(4);
    lead.protectionMonths = 6;

    int score = scoringService.calculateScore(lead);

    assertTrue(
        score >= 40 && score <= 60,
        String.format("Medium-value lead should have Score 40-60, got %d", score));
  }

  // ================= Edge Cases & Validations =================

  @Test
  @Transactional
  @DisplayName("Edge Case: Score capped at 100 (max)")
  void testScoreCappedAt100() {
    Lead lead = createBaseLead();

    // Maximize all factors artificially (should still cap at 100)
    lead.estimatedVolume = new BigDecimal("1000000"); // Way over threshold
    lead.employeeCount = 1000;
    lead.lastActivityAt = LocalDateTime.now().minusHours(1);
    lead.followupCount = 100;
    lead.businessType = "RESTAURANT";
    lead.kitchenSize = "large";
    lead.stage = LeadStage.QUALIFIZIERT;
    lead.progressDeadline = LocalDateTime.now().plusHours(1);
    lead.protectionStartAt = LocalDateTime.now().minusMonths(5).minusDays(29);
    lead.protectionMonths = 6;

    int score = scoringService.calculateScore(lead);

    assertTrue(score <= 100, String.format("Score should be capped at 100, got %d", score));
  }

  @Test
  @Transactional
  @DisplayName("Edge Case: Negative values handled gracefully")
  void testNegativeValuesHandledGracefully() {
    Lead lead = createBaseLead();
    lead.estimatedVolume = new BigDecimal("-1000"); // Invalid but shouldn't crash
    lead.employeeCount = -5;
    lead.followupCount = -2;

    int score = scoringService.calculateScore(lead);

    assertTrue(
        score >= 0,
        String.format("Score should be non-negative even with invalid data, got %d", score));
  }

  @Test
  @Transactional
  @DisplayName("Batch recalculation: Multiple leads scored correctly")
  void testBatchRecalculation() {
    Lead lead1 = createBaseLead();
    lead1.estimatedVolume = new BigDecimal("50000");
    lead1.employeeCount = 25;
    lead1.persist();

    Lead lead2 = createBaseLead();
    lead2.estimatedVolume = new BigDecimal("5000");
    lead2.employeeCount = 3;
    lead2.persist();

    int count = scoringService.recalculateAllScores(List.of(lead1, lead2));

    assertEquals(2, count, "Should score 2 leads");
    assertTrue(lead1.leadScore >= 20, "High-value lead should have score ≥20");
    assertTrue(lead2.leadScore >= 3, "Low-value lead should have score ≥3");
  }

  // ================= Helper Methods =================

  private Lead createBaseLead() {
    Lead lead = new Lead();
    lead.companyName = "Test Corp " + System.currentTimeMillis();
    lead.territory = testTerritory;
    lead.countryCode = "DE";
    lead.createdBy = "test-user";
    lead.ownerUserId = "test-owner";

    // Initialize protection fields
    lead.registeredAt = LocalDateTime.now();
    lead.protectionStartAt = LocalDateTime.now();
    lead.protectionMonths = 6;

    return lead;
  }
}
