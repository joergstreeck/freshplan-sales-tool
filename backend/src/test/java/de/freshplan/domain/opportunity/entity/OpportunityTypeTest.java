package de.freshplan.domain.opportunity.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit Tests für OpportunityType Enum.
 *
 * <p>Sprint 2.1.7.1: OpportunityType Backend Quick Win - Freshfoodz Business Types
 *
 * <p>Testet: - Enum Values Vollständigkeit - DisplayName Korrektheit - valueOf() Funktionalität
 */
@Tag("unit")
@DisplayName("OpportunityType Enum Unit Tests")
class OpportunityTypeTest {

  @Test
  @DisplayName("Should have exactly 4 Freshfoodz business types")
  void values_shouldHaveFourTypes() {
    OpportunityType[] types = OpportunityType.values();

    assertThat(types).hasSize(4);
    assertThat(Arrays.asList(types))
        .containsExactlyInAnyOrder(
            OpportunityType.NEUGESCHAEFT,
            OpportunityType.SORTIMENTSERWEITERUNG,
            OpportunityType.NEUER_STANDORT,
            OpportunityType.VERLAENGERUNG);
  }

  @Test
  @DisplayName("NEUGESCHAEFT should have correct label")
  void neugeschaeft_shouldHaveCorrectLabel() {
    assertThat(OpportunityType.NEUGESCHAEFT.getLabel()).isEqualTo("Neugeschäft");
  }

  @Test
  @DisplayName("SORTIMENTSERWEITERUNG should have correct label")
  void sortimentserweiterung_shouldHaveCorrectLabel() {
    assertThat(OpportunityType.SORTIMENTSERWEITERUNG.getLabel())
        .isEqualTo("Sortimentserweiterung");
  }

  @Test
  @DisplayName("NEUER_STANDORT should have correct label")
  void neuerStandort_shouldHaveCorrectLabel() {
    assertThat(OpportunityType.NEUER_STANDORT.getLabel()).isEqualTo("Neuer Standort");
  }

  @Test
  @DisplayName("VERLAENGERUNG should have correct label")
  void verlaengerung_shouldHaveCorrectLabel() {
    assertThat(OpportunityType.VERLAENGERUNG.getLabel()).isEqualTo("Vertragsverlängerung");
  }

  @Test
  @DisplayName("valueOf should work for all enum constants")
  void valueOf_shouldWorkForAllConstants() {
    assertThat(OpportunityType.valueOf("NEUGESCHAEFT")).isEqualTo(OpportunityType.NEUGESCHAEFT);
    assertThat(OpportunityType.valueOf("SORTIMENTSERWEITERUNG"))
        .isEqualTo(OpportunityType.SORTIMENTSERWEITERUNG);
    assertThat(OpportunityType.valueOf("NEUER_STANDORT")).isEqualTo(OpportunityType.NEUER_STANDORT);
    assertThat(OpportunityType.valueOf("VERLAENGERUNG")).isEqualTo(OpportunityType.VERLAENGERUNG);
  }

  @Test
  @DisplayName("All types should have non-null labels")
  void allTypes_shouldHaveNonNullLabels() {
    for (OpportunityType type : OpportunityType.values()) {
      assertThat(type.getLabel()).as("Label for %s", type).isNotNull().isNotBlank();
    }
  }

  @Test
  @DisplayName("All labels should be unique")
  void allLabels_shouldBeUnique() {
    long uniqueCount =
        Arrays.stream(OpportunityType.values()).map(OpportunityType::getLabel).distinct().count();

    assertThat(uniqueCount).isEqualTo(OpportunityType.values().length);
  }

  @Test
  @DisplayName("isNewBusiness should only be true for NEUGESCHAEFT")
  void isNewBusiness_shouldOnlyBeTrueForNeugeschaeft() {
    assertThat(OpportunityType.NEUGESCHAEFT.isNewBusiness()).isTrue();
    assertThat(OpportunityType.SORTIMENTSERWEITERUNG.isNewBusiness()).isFalse();
    assertThat(OpportunityType.NEUER_STANDORT.isNewBusiness()).isFalse();
    assertThat(OpportunityType.VERLAENGERUNG.isNewBusiness()).isFalse();
  }

  @Test
  @DisplayName("isRenewal should only be true for VERLAENGERUNG")
  void isRenewal_shouldOnlyBeTrueForVerlaengerung() {
    assertThat(OpportunityType.VERLAENGERUNG.isRenewal()).isTrue();
    assertThat(OpportunityType.NEUGESCHAEFT.isRenewal()).isFalse();
    assertThat(OpportunityType.SORTIMENTSERWEITERUNG.isRenewal()).isFalse();
    assertThat(OpportunityType.NEUER_STANDORT.isRenewal()).isFalse();
  }

  @Test
  @DisplayName("isExpansion should be true for SORTIMENTSERWEITERUNG and NEUER_STANDORT")
  void isExpansion_shouldBeTrueForExpansionTypes() {
    assertThat(OpportunityType.SORTIMENTSERWEITERUNG.isExpansion()).isTrue();
    assertThat(OpportunityType.NEUER_STANDORT.isExpansion()).isTrue();
    assertThat(OpportunityType.NEUGESCHAEFT.isExpansion()).isFalse();
    assertThat(OpportunityType.VERLAENGERUNG.isExpansion()).isFalse();
  }

  @Test
  @DisplayName("Enum name should match database constraint values")
  void enumName_shouldMatchDatabaseConstraintValues() {
    // Diese Werte MÜSSEN mit dem CHECK Constraint in V10030 übereinstimmen
    assertThat(OpportunityType.NEUGESCHAEFT.name()).isEqualTo("NEUGESCHAEFT");
    assertThat(OpportunityType.SORTIMENTSERWEITERUNG.name()).isEqualTo("SORTIMENTSERWEITERUNG");
    assertThat(OpportunityType.NEUER_STANDORT.name()).isEqualTo("NEUER_STANDORT");
    assertThat(OpportunityType.VERLAENGERUNG.name()).isEqualTo("VERLAENGERUNG");
  }
}
