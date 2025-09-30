package de.freshplan.domain.customer.service;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.customer.service.SmartSortService.SmartSortStrategy;
import de.freshplan.domain.customer.service.dto.SortCriteria;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Tests for SmartSortService functionality. */
@QuarkusTest
@Tag("migrate")
class SmartSortServiceTest {

  @Inject SmartSortService smartSortService;

  @Test
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
  void getStrategyDescription_withRevenuePotential_shouldReturnRevenueDescription() {
    // Given
    SmartSortStrategy strategy = SmartSortStrategy.REVENUE_POTENTIAL;

    // When
    String description = smartSortService.getStrategyDescription(strategy);

    // Then
    assertThat(description).contains("financial opportunity");
  }

  @Test
  void getStrategyDescription_withContactFrequency_shouldReturnContactDescription() {
    // Given
    SmartSortStrategy strategy = SmartSortStrategy.CONTACT_FREQUENCY;

    // When
    String description = smartSortService.getStrategyDescription(strategy);

    // Then
    assertThat(description).contains("systematic contact");
  }

  @Test
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
