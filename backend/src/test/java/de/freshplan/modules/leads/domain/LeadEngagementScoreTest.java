package de.freshplan.modules.leads.domain;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for Lead Engagement Score Calculation (Sprint 2.1.6 Phase 5+ - V280).
 *
 * <p>Validates calculateEngagementScore() method: - Relationship Status (40% weight, 0-25 points) -
 * Decision Maker Access (60% weight, -3 to +25 points) - Recency Bonus (-5 to +5 points) -
 * Touchpoint Bonus (0 to +5 points) - Final cap: 0-25 points
 */
@QuarkusTest
class LeadEngagementScoreTest {

  // ================= MAX SCORE TESTS =================

  @Test
  @DisplayName("Max Score: ADVOCATE + IS_DECISION_MAKER = 25 points")
  void testMaxEngagementScore() {
    Lead lead = new Lead();
    lead.relationshipStatus = RelationshipStatus.ADVOCATE; // 25 points
    lead.decisionMakerAccess = DecisionMakerAccess.IS_DECISION_MAKER; // 25 points
    lead.activities = new ArrayList<>();

    int score = lead.calculateEngagementScore();

    // (25 × 0.4) + (25 × 0.6) = 10 + 15 = 25 (before bonuses)
    // Recency: -5 (no activities)
    // Final: 20, capped to 0-25
    assertTrue(score >= 15 && score <= 25, "Max relationship should score 15-25");
  }

  // ================= BLOCKED TESTS (Negative Points) =================

  @Test
  @DisplayName("BLOCKED Access: ADVOCATE + BLOCKED = ~8 points (not negative!)")
  void testBlockedAccessWithAdvocate() {
    Lead lead = new Lead();
    lead.relationshipStatus = RelationshipStatus.ADVOCATE; // 25 points
    lead.decisionMakerAccess = DecisionMakerAccess.BLOCKED; // -3 points
    lead.activities = new ArrayList<>();

    int score = lead.calculateEngagementScore();

    // (25 × 0.4) + (-3 × 0.6) = 10 + (-1.8) = 8.2
    // Recency: -5 (no activities)
    // Final: ~3-8, capped to ≥0
    assertTrue(score >= 0, "Score should never be negative");
    assertTrue(score <= 10, "BLOCKED should reduce score significantly");
  }

  @Test
  @DisplayName("BLOCKED Access: COLD + BLOCKED = 0 points (floor)")
  void testBlockedAccessWithCold() {
    Lead lead = new Lead();
    lead.relationshipStatus = RelationshipStatus.COLD; // 0 points
    lead.decisionMakerAccess = DecisionMakerAccess.BLOCKED; // -3 points
    lead.activities = new ArrayList<>();

    int score = lead.calculateEngagementScore();

    // (0 × 0.4) + (-3 × 0.6) = 0 + (-1.8) = -1.8
    // Recency: -5 (no activities)
    // Final: -6.8, capped to 0
    assertEquals(0, score, "Negative scores should be capped at 0");
  }

  // ================= MEANINGFUL ACTIVITY DETECTION (Indirect via Score) =================

  @Test
  @DisplayName("Meaningful Activity: isMeaningfulContact = true → increases score")
  void testMeaningfulActivityFlag() {
    Lead lead = new Lead();
    lead.relationshipStatus = RelationshipStatus.COLD; // 0 points
    lead.decisionMakerAccess = DecisionMakerAccess.UNKNOWN; // 0 points
    lead.activities = new ArrayList<>();

    LeadActivity meaningfulActivity = new LeadActivity();
    meaningfulActivity.isMeaningfulContact = true;
    meaningfulActivity.description = "Short note"; // <100 chars
    meaningfulActivity.activityDate = LocalDateTime.now().minusDays(3); // Recent

    lead.activities.add(meaningfulActivity);

    int score = lead.calculateEngagementScore();

    // (0 × 0.4) + (0 × 0.6) = 0, Recency: +5, Touchpoint: 0 → ~5
    assertTrue(score >= 3, "isMeaningfulContact should trigger recency bonus");
  }

  @Test
  @DisplayName("Meaningful Activity: description > 100 chars → increases score")
  void testMeaningfulActivityLongDescription() {
    Lead lead = new Lead();
    lead.relationshipStatus = RelationshipStatus.COLD; // 0 points
    lead.decisionMakerAccess = DecisionMakerAccess.UNKNOWN; // 0 points
    lead.activities = new ArrayList<>();

    LeadActivity longActivity = new LeadActivity();
    longActivity.isMeaningfulContact = false;
    longActivity.description = "A".repeat(101); // >100 chars
    longActivity.activityDate = LocalDateTime.now().minusDays(3); // Recent

    lead.activities.add(longActivity);

    int score = lead.calculateEngagementScore();

    // Long description = meaningful → recency bonus
    assertTrue(score >= 3, "Description >100 chars should trigger recency bonus");
  }

  @Test
  @DisplayName("Meaningful Activity: Short description + no flag = no recency bonus")
  void testNotMeaningfulActivity() {
    Lead lead = new Lead();
    lead.relationshipStatus = RelationshipStatus.CONTACTED; // 5 points
    lead.decisionMakerAccess = DecisionMakerAccess.UNKNOWN; // 0 points
    lead.activities = new ArrayList<>();

    LeadActivity shortActivity = new LeadActivity();
    shortActivity.isMeaningfulContact = false;
    shortActivity.description = "Short note"; // <100 chars
    shortActivity.activityDate = LocalDateTime.now().minusDays(3);

    lead.activities.add(shortActivity);

    int score = lead.calculateEngagementScore();

    // (5 × 0.4) = 2, no meaningful activity → recency penalty
    assertEquals(0, score, "Short description without flag should NOT trigger recency bonus");
  }

  // ================= EDGE CASES =================

  @Test
  @DisplayName("Edge Case: NULL relationshipStatus + decisionMakerAccess = 0 points")
  void testNullRelationshipFields() {
    Lead lead = new Lead();
    lead.relationshipStatus = null;
    lead.decisionMakerAccess = null;
    lead.activities = new ArrayList<>();

    int score = lead.calculateEngagementScore();

    // (0 × 0.4) + (0 × 0.6) = 0
    // Recency: -5 (no activities)
    // Final: -5, capped to 0
    assertEquals(0, score, "NULL relationship fields should result in 0 score");
  }

  @Test
  @DisplayName("Edge Case: Empty activities list = -5 recency penalty")
  void testEmptyActivitiesList() {
    Lead lead = new Lead();
    lead.relationshipStatus = RelationshipStatus.CONTACTED; // 5 points
    lead.decisionMakerAccess = DecisionMakerAccess.UNKNOWN; // 0 points
    lead.activities = new ArrayList<>(); // Empty

    int score = lead.calculateEngagementScore();

    // (5 × 0.4) + (0 × 0.6) = 2 + 0 = 2
    // Recency: -5 (no activities)
    // Final: -3, capped to 0
    assertEquals(0, score, "Empty activities should result in 0 score due to recency penalty");
  }

  @Test
  @DisplayName("Edge Case: NULL activities list = no crash")
  void testNullActivitiesList() {
    Lead lead = new Lead();
    lead.relationshipStatus = RelationshipStatus.CONTACTED;
    lead.decisionMakerAccess = DecisionMakerAccess.DIRECT;
    lead.activities = null; // NULL!

    // Should not crash
    assertDoesNotThrow(() -> lead.calculateEngagementScore());
  }

  @Test
  @DisplayName("Recency Bonus: Recent activity (<7 days) = +5 points")
  void testRecencyBonusRecent() {
    Lead lead = new Lead();
    lead.relationshipStatus = RelationshipStatus.CONTACTED; // 5 points
    lead.decisionMakerAccess = DecisionMakerAccess.UNKNOWN; // 0 points

    LeadActivity recentActivity = new LeadActivity();
    recentActivity.isMeaningfulContact = true;
    recentActivity.activityDate = LocalDateTime.now().minusDays(3); // 3 days ago

    lead.activities = new ArrayList<>();
    lead.activities.add(recentActivity);

    int score = lead.calculateEngagementScore();

    // (5 × 0.4) + (0 × 0.6) = 2 + 0 = 2
    // Recency: +5 (<7 days)
    // Final: 7
    assertTrue(score >= 5, "Recent activity should add +5 bonus");
  }

  @Test
  @DisplayName("Touchpoint Bonus: >10 meaningful activities = +5 points")
  void testTouchpointBonusMany() {
    Lead lead = new Lead();
    lead.relationshipStatus = RelationshipStatus.CONTACTED; // 5 points
    lead.decisionMakerAccess = DecisionMakerAccess.UNKNOWN; // 0 points
    lead.activities = new ArrayList<>();

    // Add 12 meaningful activities
    for (int i = 0; i < 12; i++) {
      LeadActivity activity = new LeadActivity();
      activity.isMeaningfulContact = true;
      activity.activityDate = LocalDateTime.now().minusDays(i);
      lead.activities.add(activity);
    }

    int score = lead.calculateEngagementScore();

    // (5 × 0.4) + (0 × 0.6) = 2 + 0 = 2
    // Recency: +5 (recent activity)
    // Touchpoints: +5 (>10)
    // Final: 12
    assertTrue(score >= 10, "12 meaningful activities should add +5 touchpoint bonus");
  }
}
