package de.freshplan.modules.leads.service;

import de.freshplan.modules.leads.domain.Lead;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Service for calculating Lead Scores based on 4 factors (Sprint 2.1.6 Phase 4 - ADR-006 Phase 2).
 *
 * <p>Score Calculation (0-100 points): - Umsatzpotenzial (25%): estimatedVolume + employeeCount -
 * Engagement (25%): lastActivityAt + followupCount - Fit (25%): businessType + kitchenSize + stage
 * - Dringlichkeit (25%): progressDeadline proximity + protectionUntil proximity
 */
@ApplicationScoped
public class LeadScoringService {

  private static final int MAX_SCORE = 100;
  private static final int FACTOR_WEIGHT = 25; // Each factor contributes 25%

  /**
   * Calculates and updates the lead score based on 4 factors.
   *
   * @param lead The lead to score (will be modified in-place)
   * @return The calculated score (0-100)
   */
  @Transactional
  public int calculateScore(Lead lead) {
    int umsatzpotenzial = calculateUmsatzpotenzial(lead);
    int engagement = calculateEngagement(lead);
    int fit = calculateFit(lead);
    int dringlichkeit = calculateDringlichkeit(lead);

    // Weighted sum (each factor = 25%)
    int totalScore = umsatzpotenzial + engagement + fit + dringlichkeit;

    // Ensure 0-100 range
    lead.leadScore = Math.max(0, Math.min(MAX_SCORE, totalScore));
    return lead.leadScore;
  }

  /**
   * Faktor 1: Umsatzpotenzial (25% weight).
   *
   * <p>Sub-factors: - estimatedVolume (15 points): €0 = 0, €10k = 5, €50k = 15 - employeeCount (10
   * points): 0 = 0, 10 = 3, 25+ = 10
   */
  private int calculateUmsatzpotenzial(Lead lead) {
    int volumeScore = 0;
    int employeeScore = 0;

    // Volume scoring (max 15 points)
    if (lead.estimatedVolume != null) {
      BigDecimal volume = lead.estimatedVolume;
      if (volume.compareTo(BigDecimal.valueOf(50000)) >= 0) {
        volumeScore = 15; // ≥€50k
      } else if (volume.compareTo(BigDecimal.valueOf(25000)) >= 0) {
        volumeScore = 10; // €25k-€49k
      } else if (volume.compareTo(BigDecimal.valueOf(10000)) >= 0) {
        volumeScore = 5; // €10k-€24k
      } else if (volume.compareTo(BigDecimal.ZERO) > 0) {
        volumeScore = 2; // >€0
      }
    }

    // Employee scoring (max 10 points)
    if (lead.employeeCount != null) {
      if (lead.employeeCount >= 25) {
        employeeScore = 10;
      } else if (lead.employeeCount >= 10) {
        employeeScore = 6;
      } else if (lead.employeeCount >= 5) {
        employeeScore = 3;
      } else if (lead.employeeCount > 0) {
        employeeScore = 1;
      }
    }

    return Math.min(FACTOR_WEIGHT, volumeScore + employeeScore);
  }

  /**
   * Faktor 2: Engagement (25% weight).
   *
   * <p>Sub-factors: - lastActivityAt (15 points): <7d = 15, <30d = 10, <90d = 5, else 0 -
   * followupCount (10 points): 0 = 0, 1-2 = 3, 3-5 = 7, 6+ = 10
   */
  private int calculateEngagement(Lead lead) {
    int activityScore = 0;
    int followupScore = 0;

    // Activity recency (max 15 points)
    if (lead.lastActivityAt != null) {
      long daysSinceActivity = ChronoUnit.DAYS.between(lead.lastActivityAt, LocalDateTime.now());
      if (daysSinceActivity < 7) {
        activityScore = 15; // Very recent
      } else if (daysSinceActivity < 30) {
        activityScore = 10; // Recent
      } else if (daysSinceActivity < 90) {
        activityScore = 5; // Moderate
      }
      // else: 0 points (>90 days = stale)
    }

    // Followup count (max 10 points)
    if (lead.followupCount != null) {
      if (lead.followupCount >= 6) {
        followupScore = 10;
      } else if (lead.followupCount >= 3) {
        followupScore = 7;
      } else if (lead.followupCount >= 1) {
        followupScore = 3;
      }
    }

    return Math.min(FACTOR_WEIGHT, activityScore + followupScore);
  }

  /**
   * Faktor 3: Fit (25% weight).
   *
   * <p>Sub-factors: - businessType (10 points): Restaurant/Hotel = 10, Catering = 7, Kantine = 5,
   * Other = 2 - kitchenSize (8 points): large = 8, medium = 5, small = 2 - stage (7 points):
   * QUALIFIZIERT = 7, REGISTRIERUNG = 4, VORMERKUNG = 1
   */
  private int calculateFit(Lead lead) {
    int businessTypeScore = 0;
    int kitchenSizeScore = 0;
    int stageScore = 0;

    // Business type (max 10 points)
    if (lead.businessType != null) {
      String type = lead.businessType.toLowerCase();
      if (type.contains("restaurant") || type.contains("hotel")) {
        businessTypeScore = 10; // High-value segments
      } else if (type.contains("catering")) {
        businessTypeScore = 7;
      } else if (type.contains("kantine") || type.contains("kantinen")) {
        businessTypeScore = 5;
      } else {
        businessTypeScore = 2; // Generic
      }
    }

    // Kitchen size (max 8 points)
    if (lead.kitchenSize != null) {
      String size = lead.kitchenSize.toLowerCase();
      if (size.equals("large") || size.equals("groß")) {
        kitchenSizeScore = 8;
      } else if (size.equals("medium") || size.equals("mittel")) {
        kitchenSizeScore = 5;
      } else if (size.equals("small") || size.equals("klein")) {
        kitchenSizeScore = 2;
      }
    }

    // Stage (max 7 points)
    if (lead.stage != null) {
      switch (lead.stage) {
        case QUALIFIZIERT:
          stageScore = 7;
          break;
        case REGISTRIERUNG:
          stageScore = 4;
          break;
        case VORMERKUNG:
          stageScore = 1;
          break;
      }
    }

    return Math.min(FACTOR_WEIGHT, businessTypeScore + kitchenSizeScore + stageScore);
  }

  /**
   * Faktor 4: Dringlichkeit (25% weight).
   *
   * <p>Sub-factors: - progressDeadline (15 points): <3d = 15, <7d = 10, <14d = 5, else 0 -
   * protectionUntil (10 points): <30d = 10, <90d = 5, <180d = 2, else 0
   */
  private int calculateDringlichkeit(Lead lead) {
    int deadlineScore = 0;
    int protectionScore = 0;

    // Progress deadline urgency (max 15 points)
    if (lead.progressDeadline != null) {
      long daysUntilDeadline = ChronoUnit.DAYS.between(LocalDateTime.now(), lead.progressDeadline);
      if (daysUntilDeadline < 3) {
        deadlineScore = 15; // Very urgent
      } else if (daysUntilDeadline < 7) {
        deadlineScore = 10; // Urgent
      } else if (daysUntilDeadline < 14) {
        deadlineScore = 5; // Moderate urgency
      }
      // else: 0 points (>14 days = not urgent)
    }

    // Protection expiry urgency (max 10 points)
    // TODO: Refactor - protectionUntil calculation is duplicated in LeadDTO.java
    //       Consider moving to Lead entity as helper method (e.g., lead.getProtectionUntil())
    if (lead.protectionStartAt != null && lead.protectionMonths != null) {
      LocalDateTime protectionUntil = lead.protectionStartAt.plusMonths(lead.protectionMonths);
      long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDateTime.now(), protectionUntil);

      if (daysUntilExpiry < 30) {
        protectionScore = 10; // Expiring soon
      } else if (daysUntilExpiry < 90) {
        protectionScore = 5; // Expiring medium-term
      } else if (daysUntilExpiry < 180) {
        protectionScore = 2; // Long-term
      }
      // else: 0 points (>180 days = safe)
    }

    return Math.min(FACTOR_WEIGHT, deadlineScore + protectionScore);
  }

  /**
   * Recalculates scores for all leads in batch (for nightly jobs).
   *
   * @param leads List of leads to score
   * @return Count of leads scored
   */
  @Transactional
  public int recalculateAllScores(Iterable<Lead> leads) {
    int count = 0;
    for (Lead lead : leads) {
      calculateScore(lead);
      count++;
    }
    return count;
  }
}
