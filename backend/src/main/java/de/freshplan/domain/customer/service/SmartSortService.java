package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.service.dto.SortCriteria;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.jboss.logging.Logger;

/**
 * Service for intelligent customer sorting based on sales priorities and business logic.
 *
 * <p>SmartSort provides pre-configured sorting strategies that prioritize customers based on sales
 * opportunities, risk factors, and engagement levels.
 */
@ApplicationScoped
public class SmartSortService {

  private static final Logger LOG = Logger.getLogger(SmartSortService.class);

  /** Predefined smart sort strategies for different business scenarios. */
  public enum SmartSortStrategy {
    /** Prioritize high-value sales opportunities */
    SALES_PRIORITY,
    /** Focus on at-risk customers needing attention */
    RISK_MITIGATION,
    /** Sort by engagement and follow-up needs */
    ENGAGEMENT_FOCUS,
    /** Prioritize by revenue potential */
    REVENUE_POTENTIAL,
    /** Sort by last contact to ensure regular communication */
    CONTACT_FREQUENCY
  }

  /**
   * Creates a smart sort configuration based on the given strategy.
   *
   * @param strategy the smart sorting strategy to use
   * @return a list of sort criteria implementing the strategy
   */
  public List<SortCriteria> createSmartSort(SmartSortStrategy strategy) {
    LOG.debugf("Creating smart sort for strategy: %s", strategy);

    switch (strategy) {
      case SALES_PRIORITY:
        return createSalesPrioritySort();
      case RISK_MITIGATION:
        return createRiskMitigationSort();
      case ENGAGEMENT_FOCUS:
        return createEngagementFocusSort();
      case REVENUE_POTENTIAL:
        return createRevenuePotentialSort();
      case CONTACT_FREQUENCY:
        return createContactFrequencySort();
      default:
        LOG.warnf("Unknown smart sort strategy: %s, falling back to sales priority", strategy);
        return createSalesPrioritySort();
    }
  }

  /**
   * Sales Priority Sort: Focus on high-value opportunities with immediate potential.
   *
   * <p>Priority order: 1. At-risk customers (need immediate attention) 2. Expected annual volume
   * (high to low) 3. Last contact date (oldest first - needs follow-up) 4. Risk score (low to high)
   */
  private List<SortCriteria> createSalesPrioritySort() {
    return List.of(
        SortCriteria.desc("riskScore"), // Highest risk first
        SortCriteria.desc("expectedAnnualVolume"), // High volume potential
        SortCriteria.asc("lastContactDate"), // Oldest contact first (needs attention)
        SortCriteria.asc("riskScore") // Lower risk is better
        );
  }

  /**
   * Risk Mitigation Sort: Prioritize customers at risk of churn.
   *
   * <p>Priority order: 1. Risk score (high to low - highest risk first) 2. Last contact date
   * (oldest first) 3. Expected annual volume (protect high-value customers) 4. Company name
   * (alphabetical fallback)
   */
  private List<SortCriteria> createRiskMitigationSort() {
    return List.of(
        SortCriteria.desc("riskScore"), // Highest risk first
        SortCriteria.asc("lastContactDate"), // Oldest contact needs attention
        SortCriteria.desc("expectedAnnualVolume"), // Protect high-value customers
        SortCriteria.asc("companyName") // Alphabetical fallback
        );
  }

  /**
   * Engagement Focus Sort: Optimize for customer relationship management.
   *
   * <p>Priority order: 1. Next follow-up date (upcoming dates first) 2. Last contact date (ensure
   * regular communication) 3. Risk score (proactive attention for high risk) 4. Company name
   * (alphabetical for systematic approach)
   */
  private List<SortCriteria> createEngagementFocusSort() {
    return List.of(
        SortCriteria.asc("nextFollowUpDate"), // Upcoming follow-ups first
        SortCriteria.asc("lastContactDate"), // Ensure regular contact
        SortCriteria.desc("riskScore"), // High risk needs attention
        SortCriteria.asc("companyName") // Alphabetical for systematic work
        );
  }

  /**
   * Revenue Potential Sort: Focus purely on financial opportunity.
   *
   * <p>Priority order: 1. Expected annual volume (high to low) 2. Status (active customers first)
   * 3. Risk score (low risk preferred for revenue) 4. Last contact date (maintain high-value
   * relationships)
   */
  private List<SortCriteria> createRevenuePotentialSort() {
    return List.of(
        SortCriteria.desc("expectedAnnualVolume"), // Highest revenue first
        SortCriteria.asc("status"), // Active customers first
        SortCriteria.asc("riskScore"), // Lower risk for stable revenue
        SortCriteria.asc("lastContactDate") // Maintain relationships
        );
  }

  /**
   * Contact Frequency Sort: Ensure no customer is forgotten.
   *
   * <p>Priority order: 1. Last contact date (oldest first - most urgent) 2. At-risk flag (need
   * immediate attention) 3. Expected annual volume (prioritize valuable customers) 4. Customer
   * number (systematic approach)
   */
  private List<SortCriteria> createContactFrequencySort() {
    return List.of(
        SortCriteria.asc("lastContactDate"), // Oldest contact first
        SortCriteria.desc("riskScore"), // High risk needs attention
        SortCriteria.desc("expectedAnnualVolume"), // Value-based priority
        SortCriteria.asc("customerNumber") // Systematic approach
        );
  }

  /**
   * Converts a smart sort strategy to a native Panache Sort object for database queries.
   *
   * @param strategy the smart sort strategy
   * @return a configured Sort object
   */
  public Sort createPanacheSort(SmartSortStrategy strategy) {
    List<SortCriteria> criteria = createSmartSort(strategy);
    return convertToSort(criteria);
  }

  /** Converts a list of sort criteria to a Panache Sort object. */
  private Sort convertToSort(List<SortCriteria> criteria) {
    if (criteria.isEmpty()) {
      return Sort.by("companyName").ascending();
    }

    // Start with the first criterion
    SortCriteria first = criteria.get(0);
    Sort sort =
        first.isAscending()
            ? Sort.by(first.getField()).ascending()
            : Sort.by(first.getField()).descending();

    // Chain additional criteria
    for (int i = 1; i < criteria.size(); i++) {
      SortCriteria criterion = criteria.get(i);
      sort =
          criterion.isAscending()
              ? sort.and(criterion.getField(), Sort.Direction.Ascending)
              : sort.and(criterion.getField(), Sort.Direction.Descending);
    }

    return sort;
  }

  /**
   * Gets a description of what a smart sort strategy optimizes for.
   *
   * @param strategy the strategy to describe
   * @return a human-readable description
   */
  public String getStrategyDescription(SmartSortStrategy strategy) {
    switch (strategy) {
      case SALES_PRIORITY:
        return "Prioritizes high-value sales opportunities with immediate potential";
      case RISK_MITIGATION:
        return "Focuses on at-risk customers to prevent churn";
      case ENGAGEMENT_FOCUS:
        return "Optimizes for customer relationship management and communication";
      case REVENUE_POTENTIAL:
        return "Prioritizes customers by pure financial opportunity";
      case CONTACT_FREQUENCY:
        return "Ensures systematic contact with all customers";
      default:
        return "Unknown strategy";
    }
  }
}
