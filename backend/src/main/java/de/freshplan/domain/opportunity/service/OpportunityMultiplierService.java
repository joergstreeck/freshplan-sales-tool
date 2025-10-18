package de.freshplan.domain.opportunity.service;

import de.freshplan.domain.opportunity.entity.OpportunityMultiplier;
import de.freshplan.domain.opportunity.service.dto.OpportunityMultiplierResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
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

    return OpportunityMultiplier.list(
        "businessType = ?1 ORDER BY opportunityType", businessType);
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
        "Finding multiplier for businessType=" + businessType + ", opportunityType=" +
        opportunityType);

    return OpportunityMultiplier.findByTypes(businessType, opportunityType);
  }

  // ============================================================================
  // FUTURE: Admin CRUD Operations (Modul 08 - Admin-Dashboard)
  // ============================================================================

  // updateMultiplier(id, newValue) - UPDATE via Admin-UI
  // createMultiplier(dto) - INSERT custom multiplier
  // deleteMultiplier(id) - DELETE custom multiplier
  // resetToDefaults() - RESET to factory defaults (36 seed entries)
}
