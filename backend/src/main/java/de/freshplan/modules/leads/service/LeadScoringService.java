package de.freshplan.modules.leads.service;

import de.freshplan.domain.shared.BusinessType;
import de.freshplan.domain.shared.DealSize;
import de.freshplan.domain.shared.LeadSource;
import de.freshplan.modules.leads.domain.Lead;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

/**
 * Lead Scoring Service - Calculates quality scores for sales prioritization.
 *
 * <p>Sprint 2.1.6+ Lead Scoring System
 *
 * <p>Scoring Model (0-100 total):
 *
 * <ul>
 *   <li>Pain Points: 25% - Customer pain severity + urgency
 *   <li>Revenue: 25% - Deal value potential (volume + budget + size)
 *   <li>Fit: 25% - ICP match quality (segment + location + source)
 *   <li>Engagement: 25% - Relationship strength (contact + activities)
 * </ul>
 *
 * <p>Usage: Call {@link #updateLeadScore(Lead)} after lead creation/update.
 */
@ApplicationScoped
public class LeadScoringService {

  // ================================================================================
  // ICP Configuration (Ideal Customer Profile)
  // TODO: Move to settings/database for configurability
  // ================================================================================

  private static final Set<BusinessType> IDEAL_SEGMENTS =
      Set.of(
          BusinessType.RESTAURANT, BusinessType.CATERING, BusinessType.HOTEL, BusinessType.KANTINE);

  private static final Set<String> IDEAL_CITIES =
      Set.of(
          "Berlin",
          "München",
          "Hamburg",
          "Köln",
          "Frankfurt",
          "Stuttgart",
          "Düsseldorf",
          "Dortmund",
          "Essen",
          "Leipzig",
          "Bremen",
          "Dresden",
          "Hannover",
          "Nürnberg",
          "Duisburg");

  private static final Set<LeadSource> HIGH_QUALITY_SOURCES =
      Set.of(LeadSource.EMPFEHLUNG, LeadSource.PARTNER, LeadSource.MESSE);

  // ================================================================================
  // Main Scoring Method
  // ================================================================================

  /**
   * Calculate and update all scoring dimensions for a lead.
   *
   * <p>This method:
   *
   * <ul>
   *   <li>Calculates all 4 sub-scores (pain, revenue, fit, engagement)
   *   <li>Computes weighted total score (25% each)
   *   <li>Updates cached score fields in Lead entity
   * </ul>
   *
   * <p>Call this method:
   *
   * <ul>
   *   <li>After lead creation
   *   <li>After lead update (business data, pain points, engagement)
   *   <li>After new activity added
   * </ul>
   *
   * @param lead The lead to score (will be updated in-place)
   */
  @Transactional
  public void updateLeadScore(Lead lead) {
    if (lead == null) {
      Log.warn("Cannot update score for null lead");
      return;
    }

    // Calculate all 4 dimensions
    int painScore = calculatePainScore(lead);
    int revenueScore = calculateRevenueScore(lead);
    int fitScore = calculateFitScore(lead);
    int engagementScore = calculateEngagementScore(lead);

    // Weighted average (25% each dimension)
    int totalScore =
        (int)
            Math.round(
                (painScore * 0.25)
                    + (revenueScore * 0.25)
                    + (fitScore * 0.25)
                    + (engagementScore * 0.25));

    // OPTIMIZATION: Only persist if scores changed (avoid unnecessary DB writes)
    boolean scoresChanged =
        !Integer.valueOf(painScore).equals(lead.painScore)
            || !Integer.valueOf(revenueScore).equals(lead.revenueScore)
            || !Integer.valueOf(fitScore).equals(lead.fitScore)
            || !Integer.valueOf(engagementScore).equals(lead.engagementScore)
            || !Integer.valueOf(totalScore).equals(lead.leadScore);

    if (scoresChanged) {
      // Update cached scores
      lead.painScore = painScore;
      lead.revenueScore = revenueScore;
      lead.fitScore = fitScore;
      lead.engagementScore = engagementScore;
      lead.leadScore = totalScore;

      Log.infof(
          "Lead %s score updated: Total=%d (Pain=%d, Revenue=%d, Fit=%d, Engagement=%d)",
          lead.id, totalScore, painScore, revenueScore, fitScore, engagementScore);
    } else {
      Log.debugf("Lead %s scores unchanged, skipping DB write", lead.id);
    }
  }

  // ================================================================================
  // Pain Score Calculation (0-100)
  // ================================================================================

  /**
   * Calculate Pain Score based on identified pain points and urgency.
   *
   * <p>Scoring breakdown:
   *
   * <ul>
   *   <li>Pain count (0-8 pains): 5 points each = max 40 points
   *   <li>Multi-pain bonus: +30 points (if multiPainBonus > 0)
   *   <li>Urgency level: EMERGENCY=30, HIGH=22, MEDIUM=15, NORMAL=8 points
   * </ul>
   *
   * @param lead The lead to score
   * @return Pain score (0-100)
   */
  public int calculatePainScore(Lead lead) {
    int score = 0;

    // 1. Count identified pain points (40 points max)
    int painCount = 0;
    if (Boolean.TRUE.equals(lead.painStaffShortage)) painCount++;
    if (Boolean.TRUE.equals(lead.painHighCosts)) painCount++;
    if (Boolean.TRUE.equals(lead.painFoodWaste)) painCount++;
    if (Boolean.TRUE.equals(lead.painQualityInconsistency)) painCount++;
    if (Boolean.TRUE.equals(lead.painUnreliableDelivery)) painCount++;
    if (Boolean.TRUE.equals(lead.painPoorService)) painCount++;
    if (Boolean.TRUE.equals(lead.painSupplierQuality)) painCount++;
    if (Boolean.TRUE.equals(lead.painTimePressure)) painCount++;

    score += Math.min(painCount * 5, 40); // Max 40 points (8 pains × 5)

    // 2. Multi-pain bonus (30 points)
    if (lead.multiPainBonus != null && lead.multiPainBonus > 0) {
      score += 30;
    }

    // 3. Urgency level (30 points max)
    if (lead.urgencyLevel != null) {
      score +=
          switch (lead.urgencyLevel) {
            case EMERGENCY -> 30;
            case HIGH -> 22;
            case MEDIUM -> 15;
            case NORMAL -> 0; // Baseline: Keine Dringlichkeit = 0 Punkte
          };
    }

    return Math.min(score, 100);
  }

  // ================================================================================
  // Revenue Score Calculation (0-100)
  // ================================================================================

  /**
   * Calculate Revenue Score based on deal value potential.
   *
   * <p>Scoring breakdown:
   *
   * <ul>
   *   <li>Estimated volume (annual): ENTERPRISE=40, LARGE=30, MEDIUM=20, SMALL=10
   *   <li>Budget confirmed: +30 points
   *   <li>Deal size category: ENTERPRISE=30, LARGE=22, MEDIUM=15, SMALL=8
   * </ul>
   *
   * @param lead The lead to score
   * @return Revenue score (0-100)
   */
  public int calculateRevenueScore(Lead lead) {
    int score = 0;

    // 1. Estimated Volume (40 points max)
    if (lead.estimatedVolume != null) {
      BigDecimal annualVolume = lead.estimatedVolume.multiply(new BigDecimal("12"));

      if (annualVolume.compareTo(new BigDecimal("100000")) >= 0) {
        score += 40; // Enterprise: 100k+ €/year
      } else if (annualVolume.compareTo(new BigDecimal("20000")) >= 0) {
        score += 30; // Large: 20-100k €/year
      } else if (annualVolume.compareTo(new BigDecimal("5000")) >= 0) {
        score += 20; // Medium: 5-20k €/year
      } else {
        score += 10; // Small: 1-5k €/year
      }
    }

    // 2. Budget Confirmed (30 points)
    if (Boolean.TRUE.equals(lead.budgetConfirmed)) {
      score += 30;
    }

    // 3. Deal Size Category (30 points max)
    if (lead.dealSize != null) {
      score +=
          switch (lead.dealSize) {
            case ENTERPRISE -> 30;
            case LARGE -> 22;
            case MEDIUM -> 15;
            case SMALL -> 8;
          };
    } else if (lead.estimatedVolume != null) {
      // Auto-calculate deal size if not set
      BigDecimal annualVolume = lead.estimatedVolume.multiply(new BigDecimal("12"));
      DealSize autoSize = DealSize.fromAnnualVolume(annualVolume);
      if (autoSize != null) {
        lead.dealSize = autoSize; // Set it for next time
        score +=
            switch (autoSize) {
              case ENTERPRISE -> 30;
              case LARGE -> 22;
              case MEDIUM -> 15;
              case SMALL -> 8;
            };
      }
    }

    return Math.min(score, 100);
  }

  // ================================================================================
  // Fit Score Calculation (0-100)
  // ================================================================================

  /**
   * Calculate Fit Score based on ICP (Ideal Customer Profile) match.
   *
   * <p>Scoring breakdown:
   *
   * <ul>
   *   <li>Segment match: Ideal=40, Other gastro=15 points
   *   <li>Location: Top 15 cities=25, Other=10 points
   *   <li>Source quality: High (Referral/Partner)=35, Medium (Messe)=25, Low (Web)=15, Other=5
   * </ul>
   *
   * @param lead The lead to score
   * @return Fit score (0-100)
   */
  public int calculateFitScore(Lead lead) {
    int score = 0;

    // 1. Segment Match (40 points max)
    if (lead.businessType != null) {
      if (IDEAL_SEGMENTS.contains(lead.businessType)) {
        score += 40; // Perfect fit: Restaurant, Catering, Hotel, Kantine
      } else {
        score += 15; // Other B2B gastro = acceptable but not ideal
      }
    }

    // 2. Location Match (25 points max)
    if (lead.city != null) {
      if (IDEAL_CITIES.contains(lead.city)) {
        score += 25; // Top 15 cities in Germany
      } else {
        score += 10; // Other cities
      }
    }

    // 3. Source Quality (35 points max)
    if (lead.source != null) {
      if (HIGH_QUALITY_SOURCES.contains(lead.source)) {
        score += 35; // Referral/Partner = high quality
      } else if (lead.source == LeadSource.MESSE) {
        score += 25; // Messe = medium quality
      } else if (lead.source == LeadSource.WEB_FORMULAR) {
        score += 15; // Web = lower quality
      } else {
        score += 5; // Other sources (TELEFON, SONSTIGES)
      }
    }

    return Math.min(score, 100);
  }

  // ================================================================================
  // Engagement Score Calculation (0-100)
  // ================================================================================

  /**
   * Calculate Engagement Score based on relationship quality and activity.
   *
   * <p>Scoring breakdown:
   *
   * <ul>
   *   <li>Relationship status: COLD=0, CONTACTED=5, ENGAGED_SKEPTICAL=8, ENGAGED_POSITIVE=12,
   *       TRUSTED=17, ADVOCATE=25
   *   <li>Decision maker access: UNKNOWN=0, BLOCKED=-3, INDIRECT=10, DIRECT=20,
   *       IS_DECISION_MAKER=25
   *   <li>Internal champion: +30 points if present
   *   <li>Recent activity bonus: +10 points if active in last 7 days
   * </ul>
   *
   * @param lead The lead to score
   * @return Engagement score (0-100, capped)
   */
  public int calculateEngagementScore(Lead lead) {
    int score = 0;

    // 1. Relationship Quality (use enum's built-in points: 0-25)
    if (lead.relationshipStatus != null) {
      score += lead.relationshipStatus.getPoints();
    }

    // 2. Decision Maker Access (use enum's built-in points: -3 to +25)
    if (lead.decisionMakerAccess != null) {
      score += lead.decisionMakerAccess.getPoints();
    }

    // 3. Internal Champion (30 points)
    if (lead.internalChampionName != null && !lead.internalChampionName.isBlank()) {
      score += 30;
    }

    // 4. Recent Activity Bonus (+10 if active in last 7 days)
    if (lead.lastActivityAt != null) {
      long daysSinceActivity =
          ChronoUnit.DAYS.between(
              lead.lastActivityAt.toLocalDate(), LocalDateTime.now().toLocalDate());
      if (daysSinceActivity <= 7) {
        score += 10;
      }
    }

    return Math.min(score, 100);
  }
}
