package de.freshplan.domain.customer.service;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.customer.service.SmartSortService.SmartSortStrategy;
import de.freshplan.domain.customer.service.dto.SortCriteria;
import io.quarkus.panache.common.Sort;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for SmartSortService business logic.
 *
 * <p><b>Migration:</b> Converted from @QuarkusTest to plain JUnit (saves ~15s per test run)
 *
 * <p>Tests smart sorting strategies, sort criteria generation, and strategy descriptions without
 * requiring Quarkus boot or database operations.
 */
@Tag("unit")
@DisplayName("SmartSortService - Strategy Logic")
class SmartSortServiceTest {

  private SmartSortService smartSortService;

  @BeforeEach
  void setUp() {
    smartSortService = new SmartSortService();
  }

  @Test
  @DisplayName("SALES_PRIORITY strategy should return correct sort order")
  void createSmartSort_withSalesPriority_shouldReturnCorrectOrder() {
    // Given
    SmartSortStrategy strategy = SmartSortStrategy.SALES_PRIORITY;

    // When
    List<SortCriteria> result = smartSortService.createSmartSort(strategy);

    // Then
    assertThat(result).hasSize(4);
    assertThat(result.get(0).getField()).isEqualTo("riskScore");
    assertThat(result.get(0).getDirection()).isEqualTo("DESC");
    assertThat(result.get(1).getField()).isEqualTo("expectedAnnualVolume");
    assertThat(result.get(1).getDirection()).isEqualTo("DESC");
    assertThat(result.get(2).getField()).isEqualTo("lastContactDate");
    assertThat(result.get(2).getDirection()).isEqualTo("ASC");
    assertThat(result.get(3).getField()).isEqualTo("riskScore");
    assertThat(result.get(3).getDirection()).isEqualTo("ASC");
  }

  @Test
  @DisplayName("RISK_MITIGATION strategy should return correct sort order")
  void createSmartSort_withRiskMitigation_shouldReturnCorrectOrder() {
    // Given
    SmartSortStrategy strategy = SmartSortStrategy.RISK_MITIGATION;

    // When
    List<SortCriteria> result = smartSortService.createSmartSort(strategy);

    // Then
    assertThat(result).hasSize(4);
    assertThat(result.get(0).getField()).isEqualTo("riskScore");
    assertThat(result.get(0).getDirection()).isEqualTo("DESC");
    assertThat(result.get(1).getField()).isEqualTo("lastContactDate");
    assertThat(result.get(1).getDirection()).isEqualTo("ASC");
    assertThat(result.get(2).getField()).isEqualTo("expectedAnnualVolume");
    assertThat(result.get(2).getDirection()).isEqualTo("DESC");
    assertThat(result.get(3).getField()).isEqualTo("companyName");
    assertThat(result.get(3).getDirection()).isEqualTo("ASC");
  }

  @Test
  @DisplayName("ENGAGEMENT_FOCUS strategy should return correct sort order")
  void createSmartSort_withEngagementFocus_shouldReturnCorrectOrder() {
    // Given
    SmartSortStrategy strategy = SmartSortStrategy.ENGAGEMENT_FOCUS;

    // When
    List<SortCriteria> result = smartSortService.createSmartSort(strategy);

    // Then
    assertThat(result).hasSize(4);
    assertThat(result.get(0).getField()).isEqualTo("nextFollowUpDate");
    assertThat(result.get(0).getDirection()).isEqualTo("ASC");
    assertThat(result.get(1).getField()).isEqualTo("lastContactDate");
    assertThat(result.get(1).getDirection()).isEqualTo("ASC");
    assertThat(result.get(2).getField()).isEqualTo("riskScore");
    assertThat(result.get(2).getDirection()).isEqualTo("DESC");
    assertThat(result.get(3).getField()).isEqualTo("companyName");
    assertThat(result.get(3).getDirection()).isEqualTo("ASC");
  }

  @Test
  @DisplayName("REVENUE_POTENTIAL strategy should return correct sort order")
  void createSmartSort_withRevenuePotential_shouldReturnCorrectOrder() {
    // Given
    SmartSortStrategy strategy = SmartSortStrategy.REVENUE_POTENTIAL;

    // When
    List<SortCriteria> result = smartSortService.createSmartSort(strategy);

    // Then
    assertThat(result).hasSize(4);
    assertThat(result.get(0).getField()).isEqualTo("expectedAnnualVolume");
    assertThat(result.get(0).getDirection()).isEqualTo("DESC");
    assertThat(result.get(1).getField()).isEqualTo("status");
    assertThat(result.get(1).getDirection()).isEqualTo("ASC");
    assertThat(result.get(2).getField()).isEqualTo("riskScore");
    assertThat(result.get(2).getDirection()).isEqualTo("ASC");
    assertThat(result.get(3).getField()).isEqualTo("lastContactDate");
    assertThat(result.get(3).getDirection()).isEqualTo("ASC");
  }

  @Test
  @DisplayName("CONTACT_FREQUENCY strategy should return correct sort order")
  void createSmartSort_withContactFrequency_shouldReturnCorrectOrder() {
    // Given
    SmartSortStrategy strategy = SmartSortStrategy.CONTACT_FREQUENCY;

    // When
    List<SortCriteria> result = smartSortService.createSmartSort(strategy);

    // Then
    assertThat(result).hasSize(4);
    assertThat(result.get(0).getField()).isEqualTo("lastContactDate");
    assertThat(result.get(0).getDirection()).isEqualTo("ASC");
    assertThat(result.get(1).getField()).isEqualTo("riskScore");
    assertThat(result.get(1).getDirection()).isEqualTo("DESC");
    assertThat(result.get(2).getField()).isEqualTo("expectedAnnualVolume");
    assertThat(result.get(2).getDirection()).isEqualTo("DESC");
    assertThat(result.get(3).getField()).isEqualTo("customerNumber");
    assertThat(result.get(3).getDirection()).isEqualTo("ASC");
  }

  @Test
  @DisplayName("Panache Sort for SALES_PRIORITY should be valid")
  void createPanacheSort_withSalesPriority_shouldReturnValidSort() {
    // Given
    SmartSortStrategy strategy = SmartSortStrategy.SALES_PRIORITY;

    // When
    Sort result = smartSortService.createPanacheSort(strategy);

    // Then
    assertThat(result).isNotNull();
    // The Sort object is complex to test in detail, but we can verify it's created
    assertThat(result.getColumns()).isNotEmpty();
  }

  @Test
  @DisplayName("SALES_PRIORITY strategy should have valid description")
  void getStrategyDescription_withValidStrategy_shouldReturnDescription() {
    // Given
    SmartSortStrategy strategy = SmartSortStrategy.SALES_PRIORITY;

    // When
    String description = smartSortService.getStrategyDescription(strategy);

    // Then
    assertThat(description).isNotNull();
    assertThat(description).isNotEmpty();
    assertThat(description).contains("high-value sales opportunities");
  }

  @Test
  @DisplayName("RISK_MITIGATION strategy should have risk-focused description")
  void getStrategyDescription_withRiskMitigation_shouldReturnRiskDescription() {
    // Given
    SmartSortStrategy strategy = SmartSortStrategy.RISK_MITIGATION;

    // When
    String description = smartSortService.getStrategyDescription(strategy);

    // Then
    assertThat(description).contains("at-risk customers");
    assertThat(description).contains("churn");
  }

  @Test
  @DisplayName("ENGAGEMENT_FOCUS strategy should have engagement description")
  void getStrategyDescription_withEngagementFocus_shouldReturnEngagementDescription() {
    // Given
    SmartSortStrategy strategy = SmartSortStrategy.ENGAGEMENT_FOCUS;

    // When
    String description = smartSortService.getStrategyDescription(strategy);

    // Then
    assertThat(description).contains("relationship management");
    assertThat(description).contains("communication");
  }

  @Test
  @DisplayName("REVENUE_POTENTIAL strategy should have revenue description")
  void getStrategyDescription_withRevenuePotential_shouldReturnRevenueDescription() {
    // Given
    SmartSortStrategy strategy = SmartSortStrategy.REVENUE_POTENTIAL;

    // When
    String description = smartSortService.getStrategyDescription(strategy);

    // Then
    assertThat(description).contains("financial opportunity");
  }

  @Test
  @DisplayName("CONTACT_FREQUENCY strategy should have contact description")
  void getStrategyDescription_withContactFrequency_shouldReturnContactDescription() {
    // Given
    SmartSortStrategy strategy = SmartSortStrategy.CONTACT_FREQUENCY;

    // When
    String description = smartSortService.getStrategyDescription(strategy);

    // Then
    assertThat(description).contains("systematic contact");
  }

  @Test
  @DisplayName("All strategies should have valid descriptions")
  void allStrategies_shouldHaveValidDescriptions() {
    // Given: All available strategies
    SmartSortStrategy[] strategies = SmartSortStrategy.values();

    // When & Then: Each strategy should have a non-empty description
    for (SmartSortStrategy strategy : strategies) {
      String description = smartSortService.getStrategyDescription(strategy);
      assertThat(description)
          .as("Strategy %s should have a description", strategy)
          .isNotNull()
          .isNotEmpty()
          .doesNotContain("Unknown");
    }
  }

  @Test
  @DisplayName("All strategies should create valid sort criteria")
  void allStrategies_shouldCreateValidSortCriteria() {
    // Given: All available strategies
    SmartSortStrategy[] strategies = SmartSortStrategy.values();

    // When & Then: Each strategy should create valid sort criteria
    for (SmartSortStrategy strategy : strategies) {
      List<SortCriteria> sortCriteria = smartSortService.createSmartSort(strategy);

      assertThat(sortCriteria)
          .as("Strategy %s should create sort criteria", strategy)
          .isNotNull()
          .isNotEmpty();

      // Verify each criterion has valid fields
      for (SortCriteria criterion : sortCriteria) {
        assertThat(criterion.getField())
            .as("Sort criterion field should not be empty")
            .isNotNull()
            .isNotEmpty();
        assertThat(criterion.getDirection())
            .as("Sort criterion direction should be valid")
            .isIn("ASC", "DESC");
      }
    }
  }
}
