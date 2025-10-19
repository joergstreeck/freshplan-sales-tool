package de.freshplan.domain.opportunity.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/**
 * OpportunityMultiplier Entity - Sprint 2.1.7.3
 *
 * <p>Business-Type-Matrix für intelligente expectedValue-Schätzung.
 *
 * <p>Formel: expectedValue = baseVolume × multiplier
 *
 * <ul>
 *   <li>baseVolume = customer.actualAnnualVolume (Xentral - BEST)
 *   <li>baseVolume = customer.expectedAnnualVolume (Lead estimate - OK)
 *   <li>baseVolume = 0 (Manual entry - FALLBACK)
 * </ul>
 *
 * <p>Beispiel: HOTEL (baseVolume=100k€) → SORTIMENTSERWEITERUNG (multiplier=0.65) = expectedValue =
 * 65.000€
 *
 * @since Sprint 2.1.7.3
 */
@Entity
@Table(
    name = "opportunity_multipliers",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_opportunity_multipliers_type_combo",
          columnNames = {"business_type", "opportunity_type"})
    })
public class OpportunityMultiplier extends PanacheEntityBase {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  /**
   * Business Type (Customer.businessType)
   *
   * <p>Values: RESTAURANT, HOTEL, CATERING, KANTINE, BILDUNG, GESUNDHEIT, GROSSHANDEL, LEH,
   * SONSTIGES
   *
   * <p>⚠️ IMPORTANT: Must match {@link de.freshplan.domain.shared.BusinessType} enum values (stored
   * as VARCHAR, not Enum column)
   */
  @Column(name = "business_type", length = 50, nullable = false)
  private String businessType;

  /**
   * Opportunity Type (Opportunity.opportunityType)
   *
   * <p>Values: NEUGESCHAEFT, SORTIMENTSERWEITERUNG, NEUER_STANDORT, VERLAENGERUNG
   *
   * <p>⚠️ IMPORTANT: Must match {@link OpportunityType} enum values (stored as VARCHAR)
   */
  @Column(name = "opportunity_type", length = 50, nullable = false)
  private String opportunityType;

  /**
   * Multiplier (0.00 - 10.00)
   *
   * <p>Typical values:
   *
   * <ul>
   *   <li>NEUGESCHAEFT: 1.00 (100% - new business = full volume)
   *   <li>SORTIMENTSERWEITERUNG: 0.15 - 0.65 (varies by industry)
   *   <li>NEUER_STANDORT: 0.40 - 0.90 (expansion potential)
   *   <li>VERLAENGERUNG: 0.70 - 0.95 (renewal retention)
   * </ul>
   */
  @Column(name = "multiplier", precision = 5, scale = 2, nullable = false)
  private BigDecimal multiplier;

  /** Created timestamp (audit trail) */
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /** Updated timestamp (audit trail) */
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  // ============================================================================
  // LIFECYCLE HOOKS
  // ============================================================================

  @PrePersist
  protected void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  // ============================================================================
  // STATIC QUERY HELPERS (Panache Repository Pattern)
  // ============================================================================

  /**
   * Find multiplier for given business type and opportunity type combination.
   *
   * @param businessType Business type (RESTAURANT, HOTEL, etc.)
   * @param opportunityType Opportunity type (NEUGESCHAEFT, SORTIMENTSERWEITERUNG, etc.)
   * @return OpportunityMultiplier or null if not found
   */
  public static OpportunityMultiplier findByTypes(String businessType, String opportunityType) {
    return find("businessType = ?1 AND opportunityType = ?2", businessType, opportunityType)
        .firstResult();
  }

  /**
   * Get multiplier value for given types, or default fallback.
   *
   * @param businessType Business type
   * @param opportunityType Opportunity type
   * @param defaultValue Fallback value if not found
   * @return Multiplier value (BigDecimal)
   */
  public static BigDecimal getMultiplierValue(
      String businessType, String opportunityType, BigDecimal defaultValue) {
    OpportunityMultiplier multiplier = findByTypes(businessType, opportunityType);
    return multiplier != null ? multiplier.getMultiplier() : defaultValue;
  }

  // ============================================================================
  // GETTERS & SETTERS
  // ============================================================================

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getBusinessType() {
    return businessType;
  }

  public void setBusinessType(String businessType) {
    this.businessType = businessType;
  }

  public String getOpportunityType() {
    return opportunityType;
  }

  public void setOpportunityType(String opportunityType) {
    this.opportunityType = opportunityType;
  }

  public BigDecimal getMultiplier() {
    return multiplier;
  }

  public void setMultiplier(BigDecimal multiplier) {
    this.multiplier = multiplier;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
