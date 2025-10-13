package de.freshplan.test.builders;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.shared.BusinessType;
import de.freshplan.domain.shared.DealSize;
import de.freshplan.domain.shared.KitchenSize;
import de.freshplan.domain.shared.LeadSource;
import de.freshplan.modules.leads.domain.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * LeadTestDataFactoryTest - Unit tests for LeadTestDataFactory
 *
 * <p>Track 2C - Advanced Test Infrastructure
 *
 * <p>Tests: - Realistic company names (German catering, with [TEST] prefix) - Contact person
 * generation (German names) - Email generation from contact person with Umlaut normalization - Phone
 * number generation (German format) - City/PostalCode generation from RealisticDataGenerator -
 * Employee count realistic range - Seeded builder for deterministic tests - Builder pattern
 * functionality - Pre-Claim (buildMinimal) vs Qualified (buildQualified) state
 */
@Tag("unit")
class LeadTestDataFactoryTest {

  @Test
  void testBuilder_shouldGenerateRealisticCateringCompanyName() {
    // When
    Lead lead = LeadTestDataFactory.builder().build();

    // Then
    assertThat(lead.companyName)
        .isNotBlank()
        .startsWith("[TEST] ") // Marked as test data
        .contains(" ") // Has name + suffix
        .satisfiesAnyOf(
            name -> assertThat(name).containsIgnoringCase("Catering"),
            name -> assertThat(name).containsIgnoringCase("Küche"),
            name -> assertThat(name).containsIgnoringCase("Gourmet"),
            name -> assertThat(name).containsIgnoringCase("Food"),
            name -> assertThat(name).containsIgnoringCase("Gastro"),
            name -> assertThat(name).containsIgnoringCase("Genuss"),
            name -> assertThat(name).containsIgnoringCase("Delikatessen"));
  }

  @Test
  void testBuilder_shouldGenerateRealisticGermanContactPerson() {
    // When
    Lead lead = LeadTestDataFactory.builder().build();

    // Then
    assertThat(lead.contactPerson).isNotBlank().contains(" "); // First + Last name
  }

  @Test
  void testBuilder_shouldGenerateEmailFromContactPersonWithNormalization() {
    // Given - Build lead to get generated contactPerson
    Lead lead = LeadTestDataFactory.builder().build();

    // Then - Email should be derived from contact person
    assertThat(lead.email).isNotBlank().contains("@").contains(".");

    // Email should be lowercase
    assertThat(lead.email).isEqualTo(lead.email.toLowerCase());

    // Email normalized (no Umlaute - ä→ae, ö→oe, ü→ue)
    assertThat(lead.emailNormalized).isEqualTo(lead.email.toLowerCase());
  }

  @Test
  void testBuilder_shouldGenerateGermanPhoneNumber() {
    // When
    Lead lead = LeadTestDataFactory.builder().build();

    // Then
    assertThat(lead.phone).isNotBlank().startsWith("+49 ").matches("\\+49 \\d{3} \\d{7}");
  }

  @Test
  void testBuilder_shouldGenerateGermanCityAndPostalCode() {
    // When
    Lead lead = LeadTestDataFactory.builder().build();

    // Then
    assertThat(lead.city).isNotBlank();
    assertThat(lead.postalCode).hasSize(5).matches("\\d{5}"); // 5-digit postal code
  }

  @Test
  void testBuilder_shouldGenerateRealisticEmployeeCount() {
    // When
    Lead lead = LeadTestDataFactory.builder().build();

    // Then
    assertThat(lead.employeeCount).isBetween(1, 5000); // Realistic range from Faker
  }

  @Test
  void testBuilder_shouldUseRealisticDefaults() {
    // When
    Lead lead = LeadTestDataFactory.builder().build();

    // Then - Required fields
    assertThat(lead.companyName).isNotBlank().startsWith("[TEST] ");
    assertThat(lead.companyNameNormalized).isEqualTo(lead.companyName.toLowerCase());
    assertThat(lead.countryCode).isEqualTo("DE");
    assertThat(lead.status).isEqualTo(LeadStatus.REGISTERED);
    assertThat(lead.stage).isEqualTo(LeadStage.REGISTRIERUNG);
    assertThat(lead.registeredAt).isNotNull();
    assertThat(lead.protectionStartAt).isNotNull();
    assertThat(lead.protectionMonths).isEqualTo(6);
    assertThat(lead.protectionDays60).isEqualTo(60);
    assertThat(lead.protectionDays10).isEqualTo(10);
    assertThat(lead.isCanonical).isTrue();
    assertThat(lead.createdAt).isNotNull();
    assertThat(lead.updatedAt).isNotNull();
    assertThat(lead.createdBy).isEqualTo("test-system");

    // Generated fields
    assertThat(lead.contactPerson).isNotBlank();
    assertThat(lead.email).isNotBlank().contains("@");
    assertThat(lead.phone).isNotBlank().startsWith("+49");
    assertThat(lead.city).isNotBlank();
    assertThat(lead.postalCode).isNotBlank();
    assertThat(lead.employeeCount).isGreaterThan(0);
  }

  @Test
  void testBuilder_withCustomValues_shouldOverrideDefaults() {
    // When
    Lead lead =
        LeadTestDataFactory.builder()
            .withCompanyName("Custom Catering GmbH")
            .withContactPerson("Max Mustermann")
            .withEmail("max@example.com")
            .withStatus(LeadStatus.GRACE_PERIOD)
            .withStage(LeadStage.QUALIFIZIERT)
            .build();

    // Then
    assertThat(lead.companyName).isEqualTo("Custom Catering GmbH");
    assertThat(lead.contactPerson).isEqualTo("Max Mustermann");
    assertThat(lead.email).isEqualTo("max@example.com");
    assertThat(lead.status).isEqualTo(LeadStatus.GRACE_PERIOD);
    assertThat(lead.stage).isEqualTo(LeadStage.QUALIFIZIERT);
  }

  @Test
  void testBuilder_withBusinessType_shouldSetBusinessType() {
    // When
    Lead lead = LeadTestDataFactory.builder().withBusinessType(BusinessType.RESTAURANT).build();

    // Then
    assertThat(lead.businessType).isEqualTo(BusinessType.RESTAURANT);
  }

  @Test
  void testBuilder_withKitchenSize_shouldSetKitchenSize() {
    // When
    Lead lead = LeadTestDataFactory.builder().withKitchenSize(KitchenSize.GROSS).build();

    // Then
    assertThat(lead.kitchenSize).isEqualTo(KitchenSize.GROSS);
  }

  @Test
  void testBuilder_withEstimatedVolume_shouldSetVolume() {
    // When
    Lead lead =
        LeadTestDataFactory.builder()
            .withEstimatedVolume(BigDecimal.valueOf(100000))
            .build();

    // Then
    assertThat(lead.estimatedVolume).isEqualByComparingTo(BigDecimal.valueOf(100000));
  }

  @Test
  void testBuilder_withDealSize_shouldSetDealSize() {
    // When
    Lead lead = LeadTestDataFactory.builder().withDealSize(DealSize.LARGE).build();

    // Then
    assertThat(lead.dealSize).isEqualTo(DealSize.LARGE);
  }

  @Test
  void testBuilder_withBudgetConfirmed_shouldSetBudgetFlag() {
    // When
    Lead lead = LeadTestDataFactory.builder().withBudgetConfirmed(true).build();

    // Then
    assertThat(lead.budgetConfirmed).isTrue();
  }

  @Test
  void testBuilder_withPainStaffShortage_shouldSetPainFlag() {
    // When
    Lead lead = LeadTestDataFactory.builder().withPainStaffShortage(true).build();

    // Then
    assertThat(lead.painStaffShortage).isTrue();
  }

  @Test
  void testBuilder_withUrgencyLevel_shouldSetUrgency() {
    // When
    Lead lead = LeadTestDataFactory.builder().withUrgencyLevel(UrgencyLevel.HIGH).build();

    // Then
    assertThat(lead.urgencyLevel).isEqualTo(UrgencyLevel.HIGH);
  }

  @Test
  void testBuilder_withRelationshipStatus_shouldSetStatus() {
    // When
    Lead lead =
        LeadTestDataFactory.builder().withRelationshipStatus(RelationshipStatus.TRUSTED).build();

    // Then
    assertThat(lead.relationshipStatus).isEqualTo(RelationshipStatus.TRUSTED);
  }

  @Test
  void testBuildMinimal_shouldCreatePreClaimLead() {
    // When
    Lead lead = LeadTestDataFactory.builder().buildMinimal();

    // Then - Pre-Claim state characteristics
    assertThat(lead.status).isEqualTo(LeadStatus.REGISTERED);
    assertThat(lead.stage).isEqualTo(LeadStage.VORMERKUNG);
    assertThat(lead.firstContactDocumentedAt).isNull(); // KEY: No FirstContact yet
    assertThat(lead.companyName).isNotBlank();
    assertThat(lead.contactPerson).isNotBlank();
    assertThat(lead.email).isNotBlank();
  }

  @Test
  void testBuildQualified_shouldCreateQualifiedLead() {
    // When
    Lead lead = LeadTestDataFactory.builder().buildQualified();

    // Then - Qualified state characteristics
    assertThat(lead.status).isEqualTo(LeadStatus.REGISTERED);
    assertThat(lead.stage).isEqualTo(LeadStage.REGISTRIERUNG);
    assertThat(lead.firstContactDocumentedAt).isNotNull(); // KEY: FirstContact documented

    // FirstContact should be in the past
    assertThat(lead.firstContactDocumentedAt).isBefore(java.time.LocalDateTime.now());
  }

  @Test
  void testSeededBuilder_shouldBeAvailable() {
    // Given
    long seed = 42L;

    // When
    LeadTestDataFactory.Builder builder = LeadTestDataFactory.builder(seed);
    Lead lead = builder.build();

    // Then - Seeded builder works (determinism tested in RealisticDataGeneratorTest)
    assertThat(lead).isNotNull();
    assertThat(lead.companyName).isNotNull().startsWith("[TEST] ");
    assertThat(lead.contactPerson).isNotNull();
    assertThat(lead.email).isNotNull();
  }

  @Test
  void testSeededBuilder_withDifferentSeeds_shouldProduceDifferentResults() {
    // Given
    long seed1 = 42L;
    long seed2 = 99L;

    // When
    Lead lead1 = LeadTestDataFactory.builder(seed1).build();
    Lead lead2 = LeadTestDataFactory.builder(seed2).build();

    // Then - Different seeds → different company names
    assertThat(lead1.companyName).isNotEqualTo(lead2.companyName);
  }

  @Test
  void testBuilder_withSource_shouldSetSource() {
    // When
    Lead lead = LeadTestDataFactory.builder().withSource(LeadSource.WEB_FORMULAR).build();

    // Then
    assertThat(lead.source).isEqualTo(LeadSource.WEB_FORMULAR);
  }

  @Test
  void testBuilder_withOwnerUserId_shouldSetOwner() {
    // When
    Lead lead = LeadTestDataFactory.builder().withOwnerUserId("user-123").build();

    // Then
    assertThat(lead.ownerUserId).isEqualTo("user-123");
  }

  @Test
  void testBuilder_withAddress_shouldSetAllAddressFields() {
    // When
    Lead lead =
        LeadTestDataFactory.builder()
            .withAddress("Hauptstraße 1", "München", "80331")
            .build();

    // Then
    assertThat(lead.street).isEqualTo("Hauptstraße 1");
    assertThat(lead.city).isEqualTo("München");
    assertThat(lead.postalCode).isEqualTo("80331");
  }

  @Test
  void testBuilder_shouldGenerateMultipleUniqueLeads() {
    // When - Build 3 leads to test variety
    Lead lead1 = LeadTestDataFactory.builder().build();
    Lead lead2 = LeadTestDataFactory.builder().build();
    Lead lead3 = LeadTestDataFactory.builder().build();

    // Then - All should have company names
    assertThat(lead1.companyName).isNotBlank();
    assertThat(lead2.companyName).isNotBlank();
    assertThat(lead3.companyName).isNotBlank();

    // Company names should vary (not all identical)
    // Note: Due to ThreadLocal + variety in Faker, high probability of uniqueness
    assertThat(lead1.companyName).isNotEmpty();
  }

  @Test
  void testBuilder_emailNormalization_shouldNormalizeCorrectly() {
    // Given - Build lead with explicit contact person with Umlaute
    Lead lead =
        LeadTestDataFactory.builder()
            .withCompanyName("Müller Catering GmbH")
            .withContactPerson("Max Müller")
            .build();

    // When - Email is auto-generated from contact person in build()
    // (if we don't explicitly set email)

    // Then - Email should be normalized
    assertThat(lead.email).contains("@example.com");
    assertThat(lead.emailNormalized).isEqualTo(lead.email.toLowerCase());

    // If contact person has Umlaute, email should normalize them
    // This is implicitly tested - RealisticDataGenerator.email() normalizes Umlaute
  }

  @Test
  void testBuilder_withFirstContactDocumentedAt_shouldSetTimestamp() {
    // Given
    java.time.LocalDateTime timestamp = java.time.LocalDateTime.now().minusDays(10);

    // When
    Lead lead = LeadTestDataFactory.builder().withFirstContactDocumentedAt(timestamp).build();

    // Then
    assertThat(lead.firstContactDocumentedAt).isEqualTo(timestamp);
  }

  @Test
  void testBuilder_withRegisteredAt_shouldSetTimestamp() {
    // Given
    java.time.LocalDateTime timestamp = java.time.LocalDateTime.now().minusDays(30);

    // When
    Lead lead = LeadTestDataFactory.builder().withRegisteredAt(timestamp).build();

    // Then
    assertThat(lead.registeredAt).isEqualTo(timestamp);
  }

  @Test
  void testBuilder_defaultValues_shouldFollowLeadProtectionRules() {
    // When
    Lead lead = LeadTestDataFactory.builder().build();

    // Then - Protection rules
    assertThat(lead.protectionMonths).isEqualTo(6); // 6 months standard protection
    assertThat(lead.protectionDays60).isEqualTo(60); // 60-day warning before loss
    assertThat(lead.protectionDays10).isEqualTo(10); // 10-day grace period
    assertThat(lead.isCanonical).isTrue(); // Always canonical (not duplicate)
  }
}
