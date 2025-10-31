package de.freshplan.domain.shared;

import com.fasterxml.jackson.annotation.JsonValue;
import java.math.BigDecimal;

/**
 * Deal size categories for revenue scoring (Sprint 2.1.6+ Lead Scoring System).
 *
 * <p>Based on estimated annual volume (ANNUAL since Sprint 2.1.7.2 D11 Phase 2).
 *
 * <p><strong>IMPORTANT:</strong> Thresholds updated to match B2B-Gastro reality:
 *
 * <ul>
 *   <li>SMALL (<200k): 10 points - Small gastro operations
 *   <li>MEDIUM (200k-1M): 20 points - Mid-market restaurants/catering
 *   <li>LARGE (1M-2M): 30 points - High-value customers (chains, hotels)
 *   <li>ENTERPRISE (>2M): 40 points - Strategic accounts (large chains, corporates)
 * </ul>
 *
 * <p>Usage: Auto-calculated from {@code estimatedVolume} field in LeadScoringService
 */
public enum DealSize {
  /** Small deal: <200k €/year (baseline revenue potential) */
  SMALL("Klein (<200k €/Jahr)", BigDecimal.ZERO, new BigDecimal("200000")),

  /** Medium deal: 200k-1M €/year (standard customer) */
  MEDIUM("Mittel (200k-1M €/Jahr)", new BigDecimal("200000"), new BigDecimal("1000000")),

  /** Large deal: 1M-2M €/year (high-value customer) */
  LARGE("Groß (1M-2M €/Jahr)", new BigDecimal("1000000"), new BigDecimal("2000000")),

  /** Enterprise deal: 2M+ €/year (strategic account) */
  ENTERPRISE("Enterprise (2M+ €/Jahr)", new BigDecimal("2000000"), null);

  private final String displayName;
  private final BigDecimal minAnnualVolume;
  private final BigDecimal maxAnnualVolume; // null for ENTERPRISE (unbounded)

  DealSize(String displayName, BigDecimal minAnnualVolume, BigDecimal maxAnnualVolume) {
    this.displayName = displayName;
    this.minAnnualVolume = minAnnualVolume;
    this.maxAnnualVolume = maxAnnualVolume;
  }

  /**
   * JSON serialization: Return enum name (e.g., "SMALL").
   *
   * @return Enum name for API responses
   */
  @JsonValue
  public String getValue() {
    return name();
  }

  /**
   * Get human-readable display name.
   *
   * @return Display name (e.g., "Klein (1-5k €/Jahr)")
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Get minimum annual volume for this category.
   *
   * @return Minimum annual volume in euros
   */
  public BigDecimal getMinAnnualVolume() {
    return minAnnualVolume;
  }

  /**
   * Get maximum annual volume for this category.
   *
   * @return Maximum annual volume in euros, or null for ENTERPRISE (unbounded)
   */
  public BigDecimal getMaxAnnualVolume() {
    return maxAnnualVolume;
  }

  /**
   * Auto-calculate deal size from estimated annual volume.
   *
   * <p>Algorithm: Find highest matching category where volume >= minAnnualVolume
   *
   * <p>Example: 15000 € → MEDIUM (>= 5k, < 20k)
   *
   * @param annualVolume Estimated annual volume (monthly × 12)
   * @return Matching deal size category, or null if volume is null
   */
  public static DealSize fromAnnualVolume(BigDecimal annualVolume) {
    if (annualVolume == null) {
      return null;
    }

    // Check from highest to lowest (ENTERPRISE first)
    if (annualVolume.compareTo(ENTERPRISE.minAnnualVolume) >= 0) {
      return ENTERPRISE;
    }
    if (annualVolume.compareTo(LARGE.minAnnualVolume) >= 0) {
      return LARGE;
    }
    if (annualVolume.compareTo(MEDIUM.minAnnualVolume) >= 0) {
      return MEDIUM;
    }

    return SMALL; // Default: anything >= 1k is at least SMALL
  }

  /**
   * Get scoring points for this deal size (0-100 scale).
   *
   * <p>Used in Revenue Score calculation (25% of total lead score)
   *
   * @return Points for this deal size (25, 50, 75, or 100)
   */
  public int getScoringPoints() {
    return switch (this) {
      case SMALL -> 25;
      case MEDIUM -> 50;
      case LARGE -> 75;
      case ENTERPRISE -> 100;
    };
  }
}
