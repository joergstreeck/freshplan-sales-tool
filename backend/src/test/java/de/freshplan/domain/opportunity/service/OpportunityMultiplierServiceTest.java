package de.freshplan.domain.opportunity.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.opportunity.entity.OpportunityMultiplier;
import de.freshplan.domain.opportunity.service.dto.OpportunityMultiplierResponse;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration Tests for OpportunityMultiplierService (Sprint 2.1.7.3)
 *
 * <p>Tests Business-Type-Matrix functionality:
 *
 * <ul>
 *   <li>getAllMultipliers() - Returns all 36 multipliers (9 BusinessTypes × 4 OpportunityTypes)
 *   <li>getMultipliersByBusinessType() - Filters by business type
 *   <li>findMultiplier() - Finds specific multiplier combination
 * </ul>
 *
 * <p>Data Source: Migration V10031 seeds 36 multipliers (Factory Defaults)
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.3
 */
@QuarkusTest
@Tag("integration")
@TestSecurity(user = "testuser", roles = {"admin", "manager", "sales"})
@DisplayName("OpportunityMultiplierService Integration Tests")
public class OpportunityMultiplierServiceTest {

  @Inject OpportunityMultiplierService multiplierService;

  @BeforeEach
  @Transactional
  void setUp() {
    // Migration V10031 seeds 36 multipliers - no cleanup needed
    // Verify seed data exists
    long count = OpportunityMultiplier.count();
    assertThat(count)
        .as("Migration V10031 should seed 36 multipliers (9 BusinessTypes × 4 OpportunityTypes)")
        .isGreaterThanOrEqualTo(36);
  }

  // ==========================================================================
  // getAllMultipliers() Tests
  // ==========================================================================

  @Test
  @DisplayName("getAllMultipliers() - Should return all 36 factory default multipliers")
  void getAllMultipliers_shouldReturnAll36Multipliers() {
    // Act
    List<OpportunityMultiplierResponse> multipliers = multiplierService.getAllMultipliers();

    // Assert
    assertThat(multipliers)
        .as("Should return 36 multipliers (9 BusinessTypes × 4 OpportunityTypes)")
        .hasSize(36);

    // Verify all BusinessTypes present (9 types)
    List<String> businessTypes =
        multipliers.stream().map(OpportunityMultiplierResponse::getBusinessType).distinct().toList();

    assertThat(businessTypes)
        .as("Should contain all 9 BusinessTypes")
        .containsExactlyInAnyOrder(
            "RESTAURANT",
            "HOTEL",
            "CATERING",
            "KANTINE",
            "BILDUNG",
            "GESUNDHEIT",
            "GROSSHANDEL",
            "LEH",
            "SONSTIGES");

    // Verify all OpportunityTypes present (4 types)
    List<String> opportunityTypes =
        multipliers.stream()
            .map(OpportunityMultiplierResponse::getOpportunityType)
            .distinct()
            .toList();

    assertThat(opportunityTypes)
        .as("Should contain all 4 OpportunityTypes")
        .containsExactlyInAnyOrder(
            "NEUGESCHAEFT", "SORTIMENTSERWEITERUNG", "NEUER_STANDORT", "VERLAENGERUNG");
  }

  @Test
  @DisplayName("getAllMultipliers() - Should return valid multiplier values (0.00-10.00)")
  void getAllMultipliers_shouldReturnValidMultiplierValues() {
    // Act
    List<OpportunityMultiplierResponse> multipliers = multiplierService.getAllMultipliers();

    // Assert
    multipliers.forEach(
        m ->
            assertThat(m.getMultiplier())
                .as(
                    "Multiplier for %s × %s should be between 0.00 and 10.00",
                    m.getBusinessType(), m.getOpportunityType())
                .isBetween(BigDecimal.ZERO, new BigDecimal("10.00")));
  }

  @Test
  @DisplayName(
      "getAllMultipliers() - NEUGESCHAEFT should always be 1.00 (100% of base volume)")
  void getAllMultipliers_neugeschaeftShouldBe100Percent() {
    // Act
    List<OpportunityMultiplierResponse> multipliers = multiplierService.getAllMultipliers();

    // Assert - NEUGESCHAEFT = 100% for all business types
    List<OpportunityMultiplierResponse> neugeschaeftMultipliers =
        multipliers.stream().filter(m -> "NEUGESCHAEFT".equals(m.getOpportunityType())).toList();

    assertThat(neugeschaeftMultipliers)
        .as("Should have 9 NEUGESCHAEFT multipliers (one per BusinessType)")
        .hasSize(9);

    neugeschaeftMultipliers.forEach(
        m ->
            assertThat(m.getMultiplier())
                .as(
                    "NEUGESCHAEFT multiplier for %s should be 1.00 (new business = full volume)",
                    m.getBusinessType())
                .isEqualByComparingTo(new BigDecimal("1.00")));
  }

  // ==========================================================================
  // getMultipliersByBusinessType() Tests
  // ==========================================================================

  @Test
  @DisplayName(
      "getMultipliersByBusinessType() - Should return 4 multipliers for RESTAURANT")
  void getMultipliersByBusinessType_restaurant_shouldReturn4Multipliers() {
    // Act
    List<OpportunityMultiplier> multipliers =
        multiplierService.getMultipliersByBusinessType("RESTAURANT");

    // Assert
    assertThat(multipliers)
        .as("RESTAURANT should have 4 multipliers (one per OpportunityType)")
        .hasSize(4);

    List<String> opportunityTypes =
        multipliers.stream().map(OpportunityMultiplier::getOpportunityType).toList();

    assertThat(opportunityTypes)
        .as("Should contain all 4 OpportunityTypes")
        .containsExactlyInAnyOrder(
            "NEUGESCHAEFT", "SORTIMENTSERWEITERUNG", "NEUER_STANDORT", "VERLAENGERUNG");

    // Verify all belong to RESTAURANT
    multipliers.forEach(
        m ->
            assertThat(m.getBusinessType())
                .as("All multipliers should belong to RESTAURANT")
                .isEqualTo("RESTAURANT"));
  }

  @Test
  @DisplayName("getMultipliersByBusinessType() - Should return 4 multipliers for HOTEL")
  void getMultipliersByBusinessType_hotel_shouldReturn4Multipliers() {
    // Act
    List<OpportunityMultiplier> multipliers =
        multiplierService.getMultipliersByBusinessType("HOTEL");

    // Assert
    assertThat(multipliers)
        .as("HOTEL should have 4 multipliers")
        .hasSize(4);

    // Verify HOTEL has highest SORTIMENTSERWEITERUNG multiplier (0.65)
    OpportunityMultiplier sortimentserweiterung =
        multipliers.stream()
            .filter(m -> "SORTIMENTSERWEITERUNG".equals(m.getOpportunityType()))
            .findFirst()
            .orElseThrow();

    assertThat(sortimentserweiterung.getMultiplier())
        .as("HOTEL SORTIMENTSERWEITERUNG should be 0.65 (highest potential)")
        .isEqualByComparingTo(new BigDecimal("0.65"));
  }

  @Test
  @DisplayName(
      "getMultipliersByBusinessType() - Should return empty list for unknown BusinessType")
  void getMultipliersByBusinessType_unknownType_shouldReturnEmptyList() {
    // Act
    List<OpportunityMultiplier> multipliers =
        multiplierService.getMultipliersByBusinessType("INVALID_TYPE");

    // Assert
    assertThat(multipliers)
        .as("Unknown BusinessType should return empty list")
        .isEmpty();
  }

  // ==========================================================================
  // findMultiplier() Tests
  // ==========================================================================

  @Test
  @DisplayName(
      "findMultiplier() - Should find RESTAURANT × SORTIMENTSERWEITERUNG = 0.25")
  void findMultiplier_restaurantSortimentserweiterung_shouldReturn025() {
    // Act
    OpportunityMultiplier multiplier =
        multiplierService.findMultiplier("RESTAURANT", "SORTIMENTSERWEITERUNG");

    // Assert
    assertThat(multiplier)
        .as("RESTAURANT × SORTIMENTSERWEITERUNG should exist")
        .isNotNull();

    assertThat(multiplier.getMultiplier())
        .as("RESTAURANT SORTIMENTSERWEITERUNG should be 0.25")
        .isEqualByComparingTo(new BigDecimal("0.25"));

    assertThat(multiplier.getBusinessType()).isEqualTo("RESTAURANT");
    assertThat(multiplier.getOpportunityType()).isEqualTo("SORTIMENTSERWEITERUNG");
  }

  @Test
  @DisplayName("findMultiplier() - Should find HOTEL × SORTIMENTSERWEITERUNG = 0.65")
  void findMultiplier_hotelSortimentserweiterung_shouldReturn065() {
    // Act
    OpportunityMultiplier multiplier =
        multiplierService.findMultiplier("HOTEL", "SORTIMENTSERWEITERUNG");

    // Assert
    assertThat(multiplier)
        .as("HOTEL × SORTIMENTSERWEITERUNG should exist")
        .isNotNull();

    assertThat(multiplier.getMultiplier())
        .as("HOTEL SORTIMENTSERWEITERUNG should be 0.65 (highest potential)")
        .isEqualByComparingTo(new BigDecimal("0.65"));
  }

  @Test
  @DisplayName("findMultiplier() - Should find CATERING × SORTIMENTSERWEITERUNG = 0.50")
  void findMultiplier_cateringSortimentserweiterung_shouldReturn050() {
    // Act
    OpportunityMultiplier multiplier =
        multiplierService.findMultiplier("CATERING", "SORTIMENTSERWEITERUNG");

    // Assert
    assertThat(multiplier).isNotNull();
    assertThat(multiplier.getMultiplier())
        .isEqualByComparingTo(new BigDecimal("0.50"));
  }

  @Test
  @DisplayName("findMultiplier() - Should find BILDUNG × SORTIMENTSERWEITERUNG = 0.20")
  void findMultiplier_bildungSortimentserweiterung_shouldReturn020() {
    // Act
    OpportunityMultiplier multiplier =
        multiplierService.findMultiplier("BILDUNG", "SORTIMENTSERWEITERUNG");

    // Assert
    assertThat(multiplier).isNotNull();
    assertThat(multiplier.getMultiplier())
        .as("BILDUNG SORTIMENTSERWEITERUNG should be 0.20 (lowest potential)")
        .isEqualByComparingTo(new BigDecimal("0.20"));
  }

  @Test
  @DisplayName("findMultiplier() - Should return null for unknown combination")
  void findMultiplier_unknownCombination_shouldReturnNull() {
    // Act
    OpportunityMultiplier multiplier =
        multiplierService.findMultiplier("INVALID_TYPE", "SORTIMENTSERWEITERUNG");

    // Assert
    assertThat(multiplier)
        .as("Unknown BusinessType should return null")
        .isNull();
  }

  @Test
  @DisplayName("findMultiplier() - Should return null for unknown OpportunityType")
  void findMultiplier_unknownOpportunityType_shouldReturnNull() {
    // Act
    OpportunityMultiplier multiplier =
        multiplierService.findMultiplier("RESTAURANT", "INVALID_TYPE");

    // Assert
    assertThat(multiplier)
        .as("Unknown OpportunityType should return null")
        .isNull();
  }

  // ==========================================================================
  // Business Logic Validation Tests
  // ==========================================================================

  @Test
  @DisplayName(
      "Business Logic - VERLAENGERUNG should have high multipliers (70%-95%)")
  void businessLogic_verlaengerung_shouldHaveHighMultipliers() {
    // Act
    List<OpportunityMultiplierResponse> multipliers = multiplierService.getAllMultipliers();

    // Filter VERLAENGERUNG
    List<OpportunityMultiplierResponse> verlaengerungMultipliers =
        multipliers.stream()
            .filter(m -> "VERLAENGERUNG".equals(m.getOpportunityType()))
            .toList();

    // Assert - VERLAENGERUNG should be high (renewal retention)
    verlaengerungMultipliers.forEach(
        m ->
            assertThat(m.getMultiplier())
                .as(
                    "VERLAENGERUNG for %s should be between 0.70 and 0.95 (high retention)",
                    m.getBusinessType())
                .isBetween(new BigDecimal("0.70"), new BigDecimal("0.95")));
  }

  @Test
  @DisplayName(
      "Business Logic - HOTEL should have higher multipliers than RESTAURANT")
  void businessLogic_hotel_shouldHaveHigherMultipliersThanRestaurant() {
    // Act
    OpportunityMultiplier hotelSortiment =
        multiplierService.findMultiplier("HOTEL", "SORTIMENTSERWEITERUNG");
    OpportunityMultiplier restaurantSortiment =
        multiplierService.findMultiplier("RESTAURANT", "SORTIMENTSERWEITERUNG");

    // Assert
    assertThat(hotelSortiment.getMultiplier())
        .as("HOTEL should have higher SORTIMENTSERWEITERUNG potential than RESTAURANT")
        .isGreaterThan(restaurantSortiment.getMultiplier());
  }
}
