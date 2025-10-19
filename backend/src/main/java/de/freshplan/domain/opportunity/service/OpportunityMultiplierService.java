package de.freshplan.domain.opportunity.service;

import de.freshplan.domain.opportunity.entity.OpportunityMultiplier;
import de.freshplan.domain.opportunity.service.dto.OpportunityMultiplierResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

/**
 * OpportunityMultiplierService - Sprint 2.1.7.3
 *
 * <p>Service for managing Business-Type-Matrix multipliers.
 *
 * <p>Provides READ-only access to multipliers (Admin-UI for CRUD comes in Modul 08).
 *
 * @since Sprint 2.1.7.3
 */
@ApplicationScoped
public class OpportunityMultiplierService {

  private static final Logger logger = Logger.getLogger(OpportunityMultiplierService.class);

  /**
   * Get all multipliers (for Frontend: GET /api/settings/opportunity-multipliers)
   *
   * <p>Returns all 36 multiplier combinations (9 BusinessTypes × 4 OpportunityTypes)
   *
   * @return List of all multipliers (sorted by business_type, opportunity_type)
   */
  @Transactional
  public List<OpportunityMultiplierResponse> getAllMultipliers() {
    logger.debug("Fetching all opportunity multipliers");

    List<OpportunityMultiplier> entities = OpportunityMultiplier.listAll();

    return entities.stream()
        .map(OpportunityMultiplierResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get multipliers for specific business type
   *
   * <p>Use case: Pre-filter multipliers for a specific customer business type
   *
   * @param businessType Business type (RESTAURANT, HOTEL, etc.)
   * @return List of multipliers for this business type (4 entries: NEUGESCHAEFT,
   *     SORTIMENTSERWEITERUNG, NEUER_STANDORT, VERLAENGERUNG)
   */
  @Transactional
  public List<OpportunityMultiplier> getMultipliersByBusinessType(String businessType) {
    logger.debug("Fetching multipliers for business type: " + businessType);

    return OpportunityMultiplier.list("businessType = ?1 ORDER BY opportunityType", businessType);
  }

  /**
   * Find specific multiplier by business type and opportunity type
   *
   * <p>Use case: Get single multiplier for calculation
   *
   * @param businessType Business type (RESTAURANT, HOTEL, etc.)
   * @param opportunityType Opportunity type (NEUGESCHAEFT, SORTIMENTSERWEITERUNG, etc.)
   * @return OpportunityMultiplier or null if not found
   */
  @Transactional
  public OpportunityMultiplier findMultiplier(String businessType, String opportunityType) {
    logger.debug(
        "Finding multiplier for businessType="
            + businessType
            + ", opportunityType="
            + opportunityType);

    return OpportunityMultiplier.findByTypes(businessType, opportunityType);
  }

  // ============================================================================
  // Admin CRUD Operations (Sprint 2.1.7.3 - Edit-Funktionalität)
  // ============================================================================

  /**
   * Update multiplier value (Admin-Only)
   *
   * <p>Sprint 2.1.7.3 - Allows admins to adjust multipliers
   *
   * @param id Multiplier ID (UUID)
   * @param newMultiplier New multiplier value (0.0-2.0)
   * @return Updated multiplier
   * @throws jakarta.ws.rs.NotFoundException if multiplier not found
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional
  public OpportunityMultiplier updateMultiplier(java.util.UUID id, BigDecimal newMultiplier) {
    logger.info("Updating multiplier: id=" + id + ", newMultiplier=" + newMultiplier);

    // 1. Validate multiplier range
    if (newMultiplier.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Multiplier must be >= 0.0 (was: " + newMultiplier + ")");
    }
    if (newMultiplier.compareTo(new BigDecimal("2.0")) > 0) {
      throw new IllegalArgumentException("Multiplier must be <= 2.0 (was: " + newMultiplier + ")");
    }

    // 2. Find existing multiplier
    OpportunityMultiplier multiplier = OpportunityMultiplier.findById(id);
    if (multiplier == null) {
      logger.warn("Multiplier not found: " + id);
      throw new jakarta.ws.rs.NotFoundException("Multiplier not found: " + id);
    }

    // 3. Update with audit trail (timestamp only)
    BigDecimal oldValue = multiplier.getMultiplier();
    multiplier.setMultiplier(newMultiplier);
    multiplier.setUpdatedAt(java.time.LocalDateTime.now());

    // 4. Persist
    multiplier.persist();

    logger.info(
        "Successfully updated multiplier "
            + id
            + ": "
            + oldValue
            + " → "
            + newMultiplier
            + " (businessType="
            + multiplier.getBusinessType()
            + ", opportunityType="
            + multiplier.getOpportunityType()
            + ")");

    return multiplier;
  }

  // ============================================================================
  // FUTURE: Extended Admin Features (Modul 08)
  // ============================================================================

  // createMultiplier(dto) - INSERT custom multiplier
  // deleteMultiplier(id) - DELETE custom multiplier
  // resetToDefaults() - RESET to factory defaults (36 seed entries)
}
