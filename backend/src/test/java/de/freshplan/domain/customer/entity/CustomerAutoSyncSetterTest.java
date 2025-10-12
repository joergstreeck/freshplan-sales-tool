package de.freshplan.domain.customer.entity;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.shared.BusinessType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Customer Auto-Sync Setter logic (Sprint 2.1.6.1 Phase 1). Tests the bidirectional
 * synchronization between legacy industry field and new businessType field during migration phase.
 *
 * <p>Background: - Sprint 2.1.6 Phase 5: businessType field added to Customer entity - Sprint 2.1.6
 * Phase 5: Auto-Sync Setters implemented for backward compatibility - Sprint 2.1.6.1 Phase 1:
 * Database migration V10026 (adds business_type column) - Sprint 2.1.6.1 Phase 1: These tests
 * validate Auto-Sync Setter behavior
 *
 * <p>Test Coverage: 1. setBusinessType() → industry auto-sync (NEW → LEGACY) 2. setIndustry() →
 * businessType auto-sync (LEGACY → NEW) 3. BusinessType.fromLegacyIndustry() mapping validation 4.
 * NULL handling for both directions
 *
 * @see Customer#setBusinessType(BusinessType)
 * @see Customer#setIndustry(Industry)
 * @see BusinessType#fromLegacyIndustry(Industry)
 * @author FreshPlan Team
 * @since 2.1.6.1
 */
@Tag("unit")
@DisplayName("Customer Auto-Sync Setter Tests (Sprint 2.1.6.1 Phase 1)")
class CustomerAutoSyncSetterTest {

  // ========== TEST 1: setBusinessType() → industry auto-sync ==========

  @Test
  @DisplayName("setBusinessType(RESTAURANT) should auto-sync to industry=RESTAURANT")
  void setBusinessType_restaurant_shouldAutoSyncToIndustry() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setBusinessType(BusinessType.RESTAURANT);

    // Then
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.RESTAURANT);
    assertThat(customer.getIndustry()).isEqualTo(Industry.RESTAURANT);
  }

  @Test
  @DisplayName("setBusinessType(HOTEL) should auto-sync to industry=HOTEL")
  void setBusinessType_hotel_shouldAutoSyncToIndustry() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setBusinessType(BusinessType.HOTEL);

    // Then
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.HOTEL);
    assertThat(customer.getIndustry()).isEqualTo(Industry.HOTEL);
  }

  @Test
  @DisplayName("setBusinessType(CATERING) should auto-sync to industry=CATERING")
  void setBusinessType_catering_shouldAutoSyncToIndustry() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setBusinessType(BusinessType.CATERING);

    // Then
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.CATERING);
    assertThat(customer.getIndustry()).isEqualTo(Industry.CATERING);
  }

  @Test
  @DisplayName("setBusinessType(KANTINE) should auto-sync to industry=KANTINE")
  void setBusinessType_kantine_shouldAutoSyncToIndustry() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setBusinessType(BusinessType.KANTINE);

    // Then
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.KANTINE);
    assertThat(customer.getIndustry()).isEqualTo(Industry.KANTINE);
  }

  @Test
  @DisplayName("setBusinessType(GESUNDHEIT) should auto-sync to industry=GESUNDHEITSWESEN")
  void setBusinessType_gesundheit_shouldAutoSyncToGesundheitswesen() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setBusinessType(BusinessType.GESUNDHEIT);

    // Then
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.GESUNDHEIT);
    assertThat(customer.getIndustry()).isEqualTo(Industry.GESUNDHEITSWESEN);
  }

  @Test
  @DisplayName("setBusinessType(BILDUNG) should auto-sync to industry=BILDUNG")
  void setBusinessType_bildung_shouldAutoSyncToBildung() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setBusinessType(BusinessType.BILDUNG);

    // Then
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.BILDUNG);
    assertThat(customer.getIndustry()).isEqualTo(Industry.BILDUNG);
  }

  @Test
  @DisplayName("setBusinessType(LEH) should auto-sync to industry=EINZELHANDEL")
  void setBusinessType_leh_shouldAutoSyncToEinzelhandel() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setBusinessType(BusinessType.LEH);

    // Then
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.LEH);
    assertThat(customer.getIndustry()).isEqualTo(Industry.EINZELHANDEL);
  }

  @Test
  @DisplayName("setBusinessType(GROSSHANDEL) should auto-sync to industry=EINZELHANDEL")
  void setBusinessType_grosshandel_shouldAutoSyncToEinzelhandel() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setBusinessType(BusinessType.GROSSHANDEL);

    // Then
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.GROSSHANDEL);
    assertThat(customer.getIndustry()).isEqualTo(Industry.EINZELHANDEL);
  }

  @Test
  @DisplayName("setBusinessType(SONSTIGES) should auto-sync to industry=SONSTIGE")
  void setBusinessType_sonstiges_shouldAutoSyncToSonstige() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setBusinessType(BusinessType.SONSTIGES);

    // Then
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.SONSTIGES);
    assertThat(customer.getIndustry()).isEqualTo(Industry.SONSTIGE);
  }

  @Test
  @DisplayName("setBusinessType(null) should not crash and keep existing industry")
  void setBusinessType_null_shouldNotCrash() {
    // Given
    Customer customer = new Customer();
    customer.setIndustry(Industry.HOTEL); // Set existing value

    // When
    customer.setBusinessType(null);

    // Then
    assertThat(customer.getBusinessType()).isNull();
    assertThat(customer.getIndustry()).isEqualTo(Industry.HOTEL); // Unchanged
  }

  // ========== TEST 2: setIndustry() → businessType auto-sync ==========

  @Test
  @DisplayName("setIndustry(RESTAURANT) should auto-sync to businessType=RESTAURANT")
  void setIndustry_restaurant_shouldAutoSyncToBusinessType() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setIndustry(Industry.RESTAURANT);

    // Then
    assertThat(customer.getIndustry()).isEqualTo(Industry.RESTAURANT);
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.RESTAURANT);
  }

  @Test
  @DisplayName("setIndustry(HOTEL) should auto-sync to businessType=HOTEL")
  void setIndustry_hotel_shouldAutoSyncToBusinessType() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setIndustry(Industry.HOTEL);

    // Then
    assertThat(customer.getIndustry()).isEqualTo(Industry.HOTEL);
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.HOTEL);
  }

  @Test
  @DisplayName("setIndustry(CATERING) should auto-sync to businessType=CATERING")
  void setIndustry_catering_shouldAutoSyncToBusinessType() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setIndustry(Industry.CATERING);

    // Then
    assertThat(customer.getIndustry()).isEqualTo(Industry.CATERING);
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.CATERING);
  }

  @Test
  @DisplayName("setIndustry(KANTINE) should auto-sync to businessType=KANTINE")
  void setIndustry_kantine_shouldAutoSyncToBusinessType() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setIndustry(Industry.KANTINE);

    // Then
    assertThat(customer.getIndustry()).isEqualTo(Industry.KANTINE);
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.KANTINE);
  }

  @Test
  @DisplayName("setIndustry(GESUNDHEITSWESEN) should auto-sync to businessType=GESUNDHEIT")
  void setIndustry_gesundheitswesen_shouldAutoSyncToGesundheit() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setIndustry(Industry.GESUNDHEITSWESEN);

    // Then
    assertThat(customer.getIndustry()).isEqualTo(Industry.GESUNDHEITSWESEN);
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.GESUNDHEIT);
  }

  @Test
  @DisplayName("setIndustry(BILDUNG) should auto-sync to businessType=BILDUNG")
  void setIndustry_bildung_shouldAutoSyncToBusinessType() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setIndustry(Industry.BILDUNG);

    // Then
    assertThat(customer.getIndustry()).isEqualTo(Industry.BILDUNG);
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.BILDUNG);
  }

  @Test
  @DisplayName("setIndustry(EINZELHANDEL) should auto-sync to businessType=LEH")
  void setIndustry_einzelhandel_shouldAutoSyncToLeh() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setIndustry(Industry.EINZELHANDEL);

    // Then
    assertThat(customer.getIndustry()).isEqualTo(Industry.EINZELHANDEL);
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.LEH);
  }

  @Test
  @DisplayName("setIndustry(VERANSTALTUNG) should auto-sync to businessType=SONSTIGES")
  void setIndustry_veranstaltung_shouldAutoSyncToSonstiges() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setIndustry(Industry.VERANSTALTUNG);

    // Then
    assertThat(customer.getIndustry()).isEqualTo(Industry.VERANSTALTUNG);
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.SONSTIGES);
  }

  @Test
  @DisplayName("setIndustry(SONSTIGE) should auto-sync to businessType=SONSTIGES")
  void setIndustry_sonstige_shouldAutoSyncToSonstiges() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setIndustry(Industry.SONSTIGE);

    // Then
    assertThat(customer.getIndustry()).isEqualTo(Industry.SONSTIGE);
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.SONSTIGES);
  }

  @Test
  @DisplayName("setIndustry(null) should not crash and keep existing businessType")
  void setIndustry_null_shouldNotCrash() {
    // Given
    Customer customer = new Customer();
    customer.setBusinessType(BusinessType.HOTEL); // Set existing value

    // When
    customer.setIndustry(null);

    // Then
    assertThat(customer.getIndustry()).isNull();
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.HOTEL); // Unchanged
  }

  // ========== TEST 3: BusinessType.fromLegacyIndustry() mapping validation ==========

  @Test
  @DisplayName("BusinessType.fromLegacyIndustry() should map all 9 Industry values correctly")
  void businessType_fromLegacyIndustry_shouldMapAllValues() {
    // Direct 1:1 mappings
    assertThat(BusinessType.fromLegacyIndustry(Industry.HOTEL)).isEqualTo(BusinessType.HOTEL);
    assertThat(BusinessType.fromLegacyIndustry(Industry.RESTAURANT))
        .isEqualTo(BusinessType.RESTAURANT);
    assertThat(BusinessType.fromLegacyIndustry(Industry.CATERING))
        .isEqualTo(BusinessType.CATERING);
    assertThat(BusinessType.fromLegacyIndustry(Industry.KANTINE))
        .isEqualTo(BusinessType.KANTINE);

    // Semantic mappings
    assertThat(BusinessType.fromLegacyIndustry(Industry.GESUNDHEITSWESEN))
        .isEqualTo(BusinessType.GESUNDHEIT);
    assertThat(BusinessType.fromLegacyIndustry(Industry.BILDUNG)).isEqualTo(BusinessType.BILDUNG);
    assertThat(BusinessType.fromLegacyIndustry(Industry.EINZELHANDEL))
        .isEqualTo(BusinessType.LEH);

    // Catch-all mappings
    assertThat(BusinessType.fromLegacyIndustry(Industry.VERANSTALTUNG))
        .isEqualTo(BusinessType.SONSTIGES);
    assertThat(BusinessType.fromLegacyIndustry(Industry.SONSTIGE))
        .isEqualTo(BusinessType.SONSTIGES);
  }

  // ========== TEST 4: Bidirectional sync consistency ==========

  @Test
  @DisplayName("Setting businessType then industry should result in industry value (last write wins)")
  void bidirectionalSync_lastWriteWins() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setBusinessType(BusinessType.HOTEL);
    customer.setIndustry(Industry.RESTAURANT);

    // Then - industry setter was last, so RESTAURANT wins
    assertThat(customer.getIndustry()).isEqualTo(Industry.RESTAURANT);
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.RESTAURANT);
  }

  @Test
  @DisplayName(
      "Setting industry then businessType should result in businessType value (last write wins)")
  void bidirectionalSync_businessTypeWinsWhenSetLast() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setIndustry(Industry.HOTEL);
    customer.setBusinessType(BusinessType.RESTAURANT);

    // Then - businessType setter was last, so RESTAURANT wins
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.RESTAURANT);
    assertThat(customer.getIndustry()).isEqualTo(Industry.RESTAURANT);
  }

  // ========== TEST 5: Edge cases with GROSSHANDEL (maps to EINZELHANDEL) ==========

  @Test
  @DisplayName(
      "GROSSHANDEL (new value) should map to EINZELHANDEL for backward compatibility (wholesale = retail for legacy)")
  void businessType_grosshandel_shouldMapToEinzelhandel() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setBusinessType(BusinessType.GROSSHANDEL);

    // Then - GROSSHANDEL maps to EINZELHANDEL in legacy Industry enum (see BusinessType.toLegacyIndustry())
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.GROSSHANDEL);
    assertThat(customer.getIndustry()).isEqualTo(Industry.EINZELHANDEL);
  }

  @Test
  @DisplayName(
      "Setting EINZELHANDEL then GROSSHANDEL should preserve GROSSHANDEL with EINZELHANDEL industry")
  void grosshandel_afterEinzelhandel_shouldPreserveGrosshandel() {
    // Given
    Customer customer = new Customer();

    // When
    customer.setIndustry(Industry.EINZELHANDEL);
    customer.setBusinessType(BusinessType.GROSSHANDEL);

    // Then
    assertThat(customer.getBusinessType()).isEqualTo(BusinessType.GROSSHANDEL);
    assertThat(customer.getIndustry())
        .isEqualTo(Industry.EINZELHANDEL); // GROSSHANDEL → EINZELHANDEL
  }
}
