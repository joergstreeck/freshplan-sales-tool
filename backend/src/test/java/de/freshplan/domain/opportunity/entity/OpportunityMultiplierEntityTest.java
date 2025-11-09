package de.freshplan.domain.opportunity.entity;

import static org.assertj.core.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration Tests for OpportunityMultiplier Entity (Sprint 2.1.7.3)
 *
 * <p>Tests Panache Repository pattern and query methods:
 *
 * <ul>
 *   <li>findByTypes() - Static query helper
 *   <li>getMultiplierValue() - Static helper with fallback
 *   <li>Entity lifecycle (PrePersist, PreUpdate timestamps)
 * </ul>
 *
 * <p>Data Source: Migration V10031 seeds 36 multipliers
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.3
 */
@QuarkusTest
@Tag("integration")
@DisplayName("OpportunityMultiplier Entity Integration Tests")
public class OpportunityMultiplierEntityTest {

  @BeforeEach
  @Transactional
  void setUp() {
    // Migration V10031 seeds 36 multipliers - no cleanup needed
    long count = OpportunityMultiplier.count();
    assertThat(count).as("Migration V10031 should seed 36 multipliers").isGreaterThanOrEqualTo(36);
  }

  @AfterEach
  @Transactional
  void cleanup() {
    // Migration V10031 provides seed data (36 multipliers)
    // No test data cleanup needed - only reads seed data
  }

  // ==========================================================================
  // findByTypes() Static Query Tests
  // ==========================================================================

  @Test
  @Transactional
  @DisplayName("findByTypes() - Should find RESTAURANT × SORTIMENTSERWEITERUNG")
  void findByTypes_restaurantSortimentserweiterung_shouldFindMultiplier() {
    // Act
    OpportunityMultiplier multiplier =
        OpportunityMultiplier.findByTypes("RESTAURANT", "SORTIMENTSERWEITERUNG");

    // Assert
    assertThat(multiplier).isNotNull();
    assertThat(multiplier.getBusinessType()).isEqualTo("RESTAURANT");
    assertThat(multiplier.getOpportunityType()).isEqualTo("SORTIMENTSERWEITERUNG");
    assertThat(multiplier.getMultiplier()).isEqualByComparingTo(new BigDecimal("0.25"));
  }

  @Test
  @Transactional
  @DisplayName("findByTypes() - Should find HOTEL × SORTIMENTSERWEITERUNG")
  void findByTypes_hotelSortimentserweiterung_shouldFindMultiplier() {
    // Act
    OpportunityMultiplier multiplier =
        OpportunityMultiplier.findByTypes("HOTEL", "SORTIMENTSERWEITERUNG");

    // Assert
    assertThat(multiplier).isNotNull();
    assertThat(multiplier.getMultiplier()).isEqualByComparingTo(new BigDecimal("0.65"));
  }

  @Test
  @Transactional
  @DisplayName("findByTypes() - Should return null for unknown BusinessType")
  void findByTypes_unknownBusinessType_shouldReturnNull() {
    // Act
    OpportunityMultiplier multiplier =
        OpportunityMultiplier.findByTypes("INVALID_TYPE", "SORTIMENTSERWEITERUNG");

    // Assert
    assertThat(multiplier).isNull();
  }

  @Test
  @Transactional
  @DisplayName("findByTypes() - Should return null for unknown OpportunityType")
  void findByTypes_unknownOpportunityType_shouldReturnNull() {
    // Act
    OpportunityMultiplier multiplier =
        OpportunityMultiplier.findByTypes("RESTAURANT", "INVALID_TYPE");

    // Assert
    assertThat(multiplier).isNull();
  }

  @Test
  @Transactional
  @DisplayName("findByTypes() - Should handle null parameters gracefully")
  void findByTypes_nullParameters_shouldReturnNull() {
    // Act
    OpportunityMultiplier nullBusiness =
        OpportunityMultiplier.findByTypes(null, "SORTIMENTSERWEITERUNG");
    OpportunityMultiplier nullOpportunity = OpportunityMultiplier.findByTypes("RESTAURANT", null);
    OpportunityMultiplier bothNull = OpportunityMultiplier.findByTypes(null, null);

    // Assert
    assertThat(nullBusiness).isNull();
    assertThat(nullOpportunity).isNull();
    assertThat(bothNull).isNull();
  }

  // ==========================================================================
  // getMultiplierValue() Static Helper Tests
  // ==========================================================================

  @Test
  @Transactional
  @DisplayName("getMultiplierValue() - Should return multiplier if found")
  void getMultiplierValue_found_shouldReturnValue() {
    // Act
    BigDecimal multiplier =
        OpportunityMultiplier.getMultiplierValue(
            "RESTAURANT", "SORTIMENTSERWEITERUNG", new BigDecimal("0.99"));

    // Assert
    assertThat(multiplier)
        .as("Should return actual multiplier, not default")
        .isEqualByComparingTo(new BigDecimal("0.25"));
  }

  @Test
  @Transactional
  @DisplayName("getMultiplierValue() - Should return default if not found")
  void getMultiplierValue_notFound_shouldReturnDefault() {
    // Act
    BigDecimal defaultValue = new BigDecimal("0.99");
    BigDecimal multiplier =
        OpportunityMultiplier.getMultiplierValue(
            "INVALID_TYPE", "SORTIMENTSERWEITERUNG", defaultValue);

    // Assert
    assertThat(multiplier)
        .as("Should return default value for unknown BusinessType")
        .isEqualByComparingTo(defaultValue);
  }

  @Test
  @Transactional
  @DisplayName("getMultiplierValue() - Should handle null default value")
  void getMultiplierValue_nullDefault_shouldWorkCorrectly() {
    // Act
    BigDecimal foundValue =
        OpportunityMultiplier.getMultiplierValue("RESTAURANT", "SORTIMENTSERWEITERUNG", null);
    BigDecimal notFoundValue =
        OpportunityMultiplier.getMultiplierValue("INVALID_TYPE", "SORTIMENTSERWEITERUNG", null);

    // Assert
    assertThat(foundValue).isEqualByComparingTo(new BigDecimal("0.25"));
    assertThat(notFoundValue).isNull();
  }

  // ==========================================================================
  // Entity Lifecycle Tests (Timestamps)
  // ==========================================================================

  @Test
  @Transactional
  @DisplayName("Entity - Should have createdAt and updatedAt timestamps from migration")
  void entity_shouldHaveTimestamps() {
    // Act
    OpportunityMultiplier multiplier =
        OpportunityMultiplier.findByTypes("RESTAURANT", "SORTIMENTSERWEITERUNG");

    // Assert
    assertThat(multiplier.getCreatedAt()).as("CreatedAt should be set by migration").isNotNull();

    assertThat(multiplier.getUpdatedAt()).as("UpdatedAt should be set by migration").isNotNull();
  }

  @Test
  @Transactional
  @DisplayName("Entity - UpdatedAt should be equal or after CreatedAt")
  void entity_updatedAtShouldBeAfterOrEqualCreatedAt() {
    // Act
    OpportunityMultiplier multiplier =
        OpportunityMultiplier.findByTypes("HOTEL", "SORTIMENTSERWEITERUNG");

    // Assert
    assertThat(multiplier.getUpdatedAt())
        .as("UpdatedAt should be equal or after CreatedAt")
        .isAfterOrEqualTo(multiplier.getCreatedAt());
  }

  // ==========================================================================
  // Business Logic Validation Tests
  // ==========================================================================

  @Test
  @Transactional
  @DisplayName("Business Logic - All NEUGESCHAEFT multipliers should be 1.00")
  void businessLogic_allNeugeschaeft_shouldBe100() {
    // Act - Sample NEUGESCHAEFT multipliers
    OpportunityMultiplier restaurant =
        OpportunityMultiplier.findByTypes("RESTAURANT", "NEUGESCHAEFT");
    OpportunityMultiplier hotel = OpportunityMultiplier.findByTypes("HOTEL", "NEUGESCHAEFT");
    OpportunityMultiplier catering = OpportunityMultiplier.findByTypes("CATERING", "NEUGESCHAEFT");

    // Assert - All NEUGESCHAEFT should be 1.00 (100%)
    assertThat(restaurant.getMultiplier())
        .as("RESTAURANT NEUGESCHAEFT should be 1.00")
        .isEqualByComparingTo(new BigDecimal("1.00"));

    assertThat(hotel.getMultiplier())
        .as("HOTEL NEUGESCHAEFT should be 1.00")
        .isEqualByComparingTo(new BigDecimal("1.00"));

    assertThat(catering.getMultiplier())
        .as("CATERING NEUGESCHAEFT should be 1.00")
        .isEqualByComparingTo(new BigDecimal("1.00"));
  }

  @Test
  @Transactional
  @DisplayName("Business Logic - HOTEL SORTIMENTSERWEITERUNG should be highest (0.65)")
  void businessLogic_hotelSortimentserweiterung_shouldBeHighest() {
    // Act
    OpportunityMultiplier hotel =
        OpportunityMultiplier.findByTypes("HOTEL", "SORTIMENTSERWEITERUNG");
    OpportunityMultiplier restaurant =
        OpportunityMultiplier.findByTypes("RESTAURANT", "SORTIMENTSERWEITERUNG");

    // Assert - HOTEL should be highest
    assertThat(hotel.getMultiplier())
        .as("HOTEL SORTIMENTSERWEITERUNG should be 0.65 (highest)")
        .isEqualByComparingTo(new BigDecimal("0.65"));

    assertThat(hotel.getMultiplier())
        .as("HOTEL should have higher multiplier than RESTAURANT")
        .isGreaterThan(restaurant.getMultiplier());
  }

  @Test
  @Transactional
  @DisplayName("Business Logic - BILDUNG SORTIMENTSERWEITERUNG should be lowest (0.20)")
  void businessLogic_bildungSortimentserweiterung_shouldBeLowest() {
    // Act
    OpportunityMultiplier bildung =
        OpportunityMultiplier.findByTypes("BILDUNG", "SORTIMENTSERWEITERUNG");
    OpportunityMultiplier sonstiges =
        OpportunityMultiplier.findByTypes("SONSTIGES", "SORTIMENTSERWEITERUNG");

    // Assert - BILDUNG should be 0.20
    assertThat(bildung.getMultiplier())
        .as("BILDUNG SORTIMENTSERWEITERUNG should be 0.20")
        .isEqualByComparingTo(new BigDecimal("0.20"));

    // SONSTIGES (0.15) is even lower - verify BILDUNG > SONSTIGES
    assertThat(bildung.getMultiplier())
        .as("BILDUNG should be higher than SONSTIGES")
        .isGreaterThan(sonstiges.getMultiplier());
  }

  @Test
  @Transactional
  @DisplayName("Business Logic - Sample multipliers should be within valid range")
  void businessLogic_sampleMultipliers_shouldBeWithinValidRange() {
    // Act - Sample multipliers
    OpportunityMultiplier restaurant =
        OpportunityMultiplier.findByTypes("RESTAURANT", "SORTIMENTSERWEITERUNG");
    OpportunityMultiplier hotel =
        OpportunityMultiplier.findByTypes("HOTEL", "SORTIMENTSERWEITERUNG");
    OpportunityMultiplier catering =
        OpportunityMultiplier.findByTypes("CATERING", "NEUER_STANDORT");

    // Assert - All should be between 0.00 and 10.00
    assertThat(restaurant.getMultiplier())
        .as("RESTAURANT multiplier should be valid")
        .isBetween(BigDecimal.ZERO, new BigDecimal("10.00"));

    assertThat(hotel.getMultiplier())
        .as("HOTEL multiplier should be valid")
        .isBetween(BigDecimal.ZERO, new BigDecimal("10.00"));

    assertThat(catering.getMultiplier())
        .as("CATERING multiplier should be valid")
        .isBetween(BigDecimal.ZERO, new BigDecimal("10.00"));
  }
}
