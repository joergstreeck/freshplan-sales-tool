package de.freshplan.modules.leads.service;

import de.freshplan.modules.leads.domain.Territory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import org.jboss.logging.Logger;

/**
 * Service for territory management. Sprint 2.1: Handles currency, tax and business rules per
 * territory - NO geographical protection.
 */
@ApplicationScoped
public class TerritoryService {

  private static final Logger LOG = Logger.getLogger(TerritoryService.class);

  /** Get all territories. */
  public List<Territory> getAllTerritories() {
    return Territory.listAll();
  }

  /** Get territory by code. */
  public Territory getTerritory(String code) {
    Territory territory = Territory.findByCode(code);
    if (territory == null) {
      LOG.warnf("Territory not found: %s, defaulting to DE", code);
      return Territory.findByCode("DE"); // Default to Germany
    }
    return territory;
  }

  /** Determine territory based on country code. */
  public Territory determineTerritory(String countryCode) {
    if ("CH".equals(countryCode)) {
      return getTerritory("CH");
    }
    if ("AT".equals(countryCode)) {
      // TODO: Add AT territory when configured in initializeDefaultTerritories
      LOG.infof("Austria (AT) territory requested but not yet configured, defaulting to DE");
      return getTerritory("DE");
    }
    // Default to Germany for all other cases
    return getTerritory("DE");
  }

  /** Initialize default territories if not present. */
  @Transactional
  public void initializeDefaultTerritories() {
    if (Territory.count() == 0) {
      LOG.info("Initializing default territories");

      // Germany
      Territory de = new Territory();
      de.id = "DE";
      de.name = "Deutschland";
      de.countryCode = "DE";
      de.currencyCode = "EUR";
      de.taxRate = new java.math.BigDecimal("19.00");
      de.languageCode = "de-DE";
      de.businessRules.put("invoicing", "monthly");
      de.businessRules.put("payment_terms", 30);
      de.businessRules.put("delivery_zones", java.util.List.of("north", "south", "east", "west"));
      de.persist();

      // Switzerland
      Territory ch = new Territory();
      ch.id = "CH";
      ch.name = "Schweiz";
      ch.countryCode = "CH";
      ch.currencyCode = "CHF";
      ch.taxRate = new java.math.BigDecimal("7.70");
      ch.languageCode = "de-CH";
      ch.businessRules.put("invoicing", "monthly");
      ch.businessRules.put("payment_terms", 45);
      ch.businessRules.put("delivery_zones", java.util.List.of("zurich", "basel", "bern"));
      ch.persist();

      LOG.info("Default territories initialized");
    }
  }
}
