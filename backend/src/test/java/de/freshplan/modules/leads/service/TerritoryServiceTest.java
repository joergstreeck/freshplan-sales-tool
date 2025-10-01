package de.freshplan.modules.leads.service;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.modules.leads.domain.Territory;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Tests for TerritoryService. Sprint 2.1: Validates territory management without geographical
 * protection. Sprint 2.1.4 Fix: Added @TestTransaction to fix ContextNotActiveException Note: We
 * don't use @TestTransaction at class level because we need the territories to persist across
 * tests. Instead we use @Transactional on individual test methods.
 */
@QuarkusTest
@TestTransaction // Sprint 2.1.4 Fix: Add transaction context for all test methods
@Tag("integration")
class TerritoryServiceTest {

  @Inject TerritoryService territoryService;

  @BeforeEach
  @Transactional
  void setup() {
    // Ensure we have both DE and CH territories for tests
    // Note: We cannot delete territories due to potential FK constraints with leads
    if (Territory.count() == 2) {
      // Both territories already present
      return;
    }

    // Ensure DE territory exists
    if (Territory.findByCode("DE") == null) {
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
              .put("delivery_zones", List.of("north", "south", "east", "west"));
      de.persist();
    }

    // Ensure CH territory exists
    if (Territory.findByCode("CH") == null) {
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
              .put("delivery_zones", List.of("zurich", "basel", "bern"));
      ch.persist();
    }
  }

  @Test
  void getAllTerritories_shouldReturnDefaultTerritories() {
    var territories = territoryService.getAllTerritories();

    assertNotNull(territories);
    assertEquals(2, territories.size());

    // Check Germany
    var de = territories.stream().filter(t -> "DE".equals(t.id)).findFirst();
    assertTrue(de.isPresent());
    assertEquals("Deutschland", de.get().name);
    assertEquals("EUR", de.get().currencyCode);
    assertEquals(new BigDecimal("19.00"), de.get().taxRate);

    // Check Switzerland
    var ch = territories.stream().filter(t -> "CH".equals(t.id)).findFirst();
    assertTrue(ch.isPresent());
    assertEquals("Schweiz", ch.get().name);
    assertEquals("CHF", ch.get().currencyCode);
    assertEquals(new BigDecimal("7.70"), ch.get().taxRate);
  }

  @Test
  void getTerritory_withValidCode_shouldReturnTerritory() {
    Territory de = territoryService.getTerritory("DE");
    assertNotNull(de);
    assertEquals("DE", de.id);
    assertEquals("Deutschland", de.name);

    Territory ch = territoryService.getTerritory("CH");
    assertNotNull(ch);
    assertEquals("CH", ch.id);
    assertEquals("Schweiz", ch.name);
  }

  @Test
  void getTerritory_withInvalidCode_shouldReturnGermanyAsDefault() {
    Territory territory = territoryService.getTerritory("FR");
    assertNotNull(territory);
    assertEquals("DE", territory.id); // Defaults to Germany
  }

  @Test
  void determineTerritory_shouldReturnCorrectTerritory() {
    // Switzerland
    Territory ch = territoryService.determineTerritory("CH");
    assertNotNull(ch);
    assertEquals("CH", ch.id);

    // Germany as default
    Territory de = territoryService.determineTerritory("DE");
    assertNotNull(de);
    assertEquals("DE", de.id);

    // Unknown defaults to Germany
    Territory unknown = territoryService.determineTerritory("FR");
    assertNotNull(unknown);
    assertEquals("DE", unknown.id);
  }

  @Test
  void territory_businessRules_shouldBeAccessible() {
    Territory de = territoryService.getTerritory("DE");
    assertNotNull(de.businessRules);
    assertEquals(30, de.getPaymentTerms());

    Territory ch = territoryService.getTerritory("CH");
    assertNotNull(ch.businessRules);
    assertEquals(45, ch.getPaymentTerms());
  }

  @Test
  void territory_helpers_shouldWork() {
    Territory de = territoryService.getTerritory("DE");
    assertTrue(de.isGermany());
    assertFalse(de.isSwitzerland());
    assertEquals("19.00%", de.getFormattedTaxRate());

    Territory ch = territoryService.getTerritory("CH");
    assertFalse(ch.isGermany());
    assertTrue(ch.isSwitzerland());
    assertEquals("7.70%", ch.getFormattedTaxRate());
  }
}
