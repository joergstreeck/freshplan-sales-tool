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

  /**
   * Initialize default territories if not present. Note: No @RlsContext needed - Territories are
   * global master data, not tenant-specific.
   */
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
      de.active = true;
      de.businessRules =
          new io.vertx.core.json.JsonObject()
              .put("invoicing", "monthly")
              .put("payment_terms", 30)
              .put("delivery_zones", java.util.List.of("north", "south", "east", "west"));
      de.persist();
      LOG.infof("Territory DE persisted: %s", de.id);

      // Switzerland
      Territory ch = new Territory();
      ch.id = "CH";
      ch.name = "Schweiz";
      ch.countryCode = "CH";
      ch.currencyCode = "CHF";
      ch.taxRate = new java.math.BigDecimal("7.70");
      ch.languageCode = "de-CH";
      ch.active = true;
      ch.businessRules =
          new io.vertx.core.json.JsonObject()
              .put("invoicing", "monthly")
              .put("payment_terms", 45)
              .put("delivery_zones", java.util.List.of("zurich", "basel", "bern"));
      ch.persist();
      LOG.infof("Territory CH persisted: %s", ch.id);

      LOG.infof("Default territories initialized (count: %d)", Territory.count());
    }
  }
}
