package de.freshplan.modules.leads.service;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.shared.BusinessType;
import de.freshplan.domain.shared.DealSize;
import de.freshplan.domain.shared.LeadSource;
import de.freshplan.modules.leads.domain.DecisionMakerAccess;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.RelationshipStatus;
import de.freshplan.modules.leads.domain.UrgencyLevel;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for LeadScoringService - 4-dimensional lead scoring.
 *
 * <p>Sprint 2.1.6+ Lead Scoring System
 *
 * <p>Tests verify:
 *
 * <ul>
 *   <li>Revenue Score calculation (volume + budget + deal size)
 *   <li>Fit Score calculation (segment + location + source)
 *   <li>Pain Score calculation (pain count + urgency + multi-pain bonus)
 *   <li>Engagement Score calculation (relationship + decision maker + champion)
 *   <li>Total Score calculation (weighted average 25% each dimension)
 * </ul>
 */
@QuarkusTest
public class LeadScoringServiceTest {

  @Inject LeadScoringService scoringService;

  @Test
  @DisplayName("Revenue Score: High volume + budget confirmed = high score")
  public void testRevenueScore_HighVolumeWithBudget() {
    Lead lead = new Lead();
    lead.estimatedVolume = new BigDecimal("10000"); // 120k €/year = ENTERPRISE
    lead.budgetConfirmed = true;
    lead.dealSize = DealSize.ENTERPRISE;

    int score = scoringService.calculateRevenueScore(lead);

    // 40 (volume) + 30 (budget) + 30 (deal size) = 100
    assertEquals(100, score);
  }

  @Test
  @DisplayName("Revenue Score: Low volume + no budget = low score")
  public void testRevenueScore_LowVolumeNoBudget() {
    Lead lead = new Lead();
    lead.estimatedVolume = new BigDecimal("300"); // 3.6k €/year = SMALL
    lead.budgetConfirmed = false;

    int score = scoringService.calculateRevenueScore(lead);

    // 10 (small volume) + 0 (no budget) + 8 (auto-calculated SMALL) = 18
    assertEquals(18, score);
  }

  @Test
  @DisplayName("Fit Score: Perfect ICP match = high score")
  public void testFitScore_PerfectMatch() {
    Lead lead = new Lead();
    lead.businessType = BusinessType.RESTAURANT; // Ideal segment
    lead.city = "Berlin"; // Top city
    lead.source = LeadSource.EMPFEHLUNG; // High quality

    int score = scoringService.calculateFitScore(lead);

    // 40 (ideal segment) + 25 (top city) + 35 (high quality source) = 100
    assertEquals(100, score);
  }

  @Test
  @DisplayName("Fit Score: Poor match = low score")
  public void testFitScore_PoorMatch() {
    Lead lead = new Lead();
    lead.businessType = BusinessType.SONSTIGES; // Not ideal (changed from SONSTIGE)
    lead.city = "Kleinstadthausen"; // Not top city
    lead.source = LeadSource.SONSTIGES; // Low quality

    int score = scoringService.calculateFitScore(lead);

    // 15 (other segment) + 10 (other city) + 5 (other source) = 30
    assertEquals(30, score);
  }

  @Test
  @DisplayName("Pain Score: Multiple pains + high urgency = high score")
  public void testPainScore_MultiplePainsHighUrgency() {
    Lead lead = new Lead();
    lead.painStaffShortage = true;
    lead.painHighCosts = true;
    lead.painFoodWaste = true;
    lead.painQualityInconsistency = true;
    lead.multiPainBonus = 1; // Non-zero triggers bonus
    lead.urgencyLevel = UrgencyLevel.EMERGENCY; // Changed from CRITICAL

    int score = scoringService.calculatePainScore(lead);

    // (4 pains × 5 = 20) + 30 (multi-pain) + 30 (emergency) = 80
    assertEquals(80, score);
  }

  @Test
  @DisplayName("Engagement Score: No contact = zero score")
  public void testEngagementScore_NoContact() {
    Lead lead = new Lead();
    lead.relationshipStatus = RelationshipStatus.COLD; // COLD = 0 points
    lead.decisionMakerAccess = DecisionMakerAccess.UNKNOWN; // UNKNOWN = 0 points

    int score = scoringService.calculateEngagementScore(lead);

    // 0 (cold) + 0 (unknown) = 0
    assertEquals(0, score);
  }

  @Test
  @DisplayName("Engagement Score: Decision maker + champion = high score")
  public void testEngagementScore_DecisionMakerWithChampion() {
    Lead lead = new Lead();
    lead.relationshipStatus = RelationshipStatus.ADVOCATE; // ADVOCATE = 25 points (highest)
    lead.decisionMakerAccess =
        DecisionMakerAccess.IS_DECISION_MAKER; // IS_DECISION_MAKER = 25 points
    lead.internalChampionName = "Max Mustermann";

    int score = scoringService.calculateEngagementScore(lead);

    // 25 (advocate) + 25 (is_decision_maker) + 30 (champion) = 80
    assertEquals(80, score);
  }

  @Test
  @DisplayName("Total Score: Perfect lead = high score")
  public void testTotalScore_PerfectLead() {
    Lead lead = new Lead();

    // Perfect revenue
    lead.estimatedVolume = new BigDecimal("10000");
    lead.budgetConfirmed = true;
    lead.dealSize = DealSize.ENTERPRISE;

    // Perfect fit
    lead.businessType = BusinessType.RESTAURANT;
    lead.city = "München";
    lead.source = LeadSource.EMPFEHLUNG;

    // Perfect pain
    lead.painStaffShortage = true;
    lead.painHighCosts = true;
    lead.painFoodWaste = true;
    lead.painQualityInconsistency = true;
    lead.multiPainBonus = 1;
    lead.urgencyLevel = UrgencyLevel.EMERGENCY;

    // Perfect engagement
    lead.relationshipStatus = RelationshipStatus.ADVOCATE; // ADVOCATE = 25 points
    lead.decisionMakerAccess =
        DecisionMakerAccess.IS_DECISION_MAKER; // IS_DECISION_MAKER = 25 points
    lead.internalChampionName = "Anna Schmidt";

    scoringService.updateLeadScore(lead);

    // Verify individual scores
    assertEquals(100, lead.revenueScore); // 40 + 30 + 30 = 100
    assertEquals(100, lead.fitScore); // 40 + 25 + 35 = 100
    assertEquals(80, lead.painScore); // 20 + 30 + 30 = 80
    assertEquals(80, lead.engagementScore); // 25 + 25 + 30 = 80

    // Total score calculation:
    // (100 * 0.25) + (100 * 0.25) + (80 * 0.25) + (80 * 0.25) = 90
    assertEquals(90, lead.leadScore);
  }
}
