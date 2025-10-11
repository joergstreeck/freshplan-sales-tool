package de.freshplan.domain.shared;

import com.fasterxml.jackson.annotation.JsonValue;
import java.math.BigDecimal;

/**
 * Deal size categories for revenue scoring (Sprint 2.1.6+ Lead Scoring System).
 *
 * <p>Based on estimated annual volume (monthly × 12).
 *
 * <p>Scoring Impact: Part of Revenue Score calculation (25% of total lead score): - SMALL (1-5k):
 * 25 points (baseline) - MEDIUM (5-20k): 50 points - LARGE (20-100k): 75 points - ENTERPRISE
 * (100k+): 100 points
 *
 * <p>Usage: - Auto-calculated from {@code estimatedVolume} field - Can be manually overridden by
 * sales rep (domain expertise)
 */
public enum DealSize {
  /** Small deal: 1-5k €/year (baseline revenue potential) */
  SMALL("Klein (1-5k €/Jahr)", new BigDecimal("1000"), new BigDecimal("5000")),

  /** Medium deal: 5-20k €/year (standard customer) */
  MEDIUM("Mittel (5-20k €/Jahr)", new BigDecimal("5000"), new BigDecimal("20000")),

  /** Large deal: 20-100k €/year (high-value customer) */
  LARGE("Groß (20-100k €/Jahr)", new BigDecimal("20000"), new BigDecimal("100000")),

  /** Enterprise deal: 100k+ €/year (strategic account) */
  ENTERPRISE("Enterprise (100k+ €/Jahr)", new BigDecimal("100000"), null);

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
