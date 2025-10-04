package de.freshplan.modules.leads.service;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.modules.leads.domain.Territory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * Tests for TerritoryService. Sprint 2.1: Validates territory management without geographical
 * protection.
 *
 * <p>Sprint 2.1.5 Fix: Use @BeforeAll with UserTransaction to initialize territories once before
 * all tests, ensuring they persist across @TestTransaction rollbacks.
 */
@QuarkusTest
@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TerritoryServiceTest {

  @Inject TerritoryService territoryService;

  @Inject UserTransaction userTransaction;

  @BeforeAll
  void setup() throws Exception {
    // Initialize default territories once before all tests
    // Use UserTransaction to commit before tests run
    userTransaction.begin();
    try {
      territoryService.initializeDefaultTerritories();
      userTransaction.commit();
    } catch (Exception e) {
      userTransaction.rollback();
      throw e;
    }
  }

  @Test
  @Transactional
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
  @Transactional
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
  @Transactional
  void getTerritory_withInvalidCode_shouldReturnGermanyAsDefault() {
    Territory territory = territoryService.getTerritory("FR");
    assertNotNull(territory);
    assertEquals("DE", territory.id); // Defaults to Germany
  }

  @Test
  @Transactional
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
  @Transactional
  void territory_businessRules_shouldBeAccessible() {
    Territory de = territoryService.getTerritory("DE");
    assertNotNull(de.businessRules);
    assertEquals(30, de.getPaymentTerms());

    Territory ch = territoryService.getTerritory("CH");
    assertNotNull(ch.businessRules);
    assertEquals(45, ch.getPaymentTerms());
  }

  @Test
  @Transactional
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
