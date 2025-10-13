package de.freshplan.test.builders;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * RealisticDataGeneratorTest - Unit tests for RealisticDataGenerator
 *
 * <p>Track 2C - Advanced Test Infrastructure
 *
 * <p>Tests: - German company/person/address data - Email normalization (Umlaute) - Date generation
 * - Edge cases (long names, special characters) - Seed-based repeatability
 */
@Tag("unit")
class RealisticDataGeneratorTest {

  @Test
  void testGermanCompanyName_shouldReturnRealisticName() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();

    // When
    String companyName = gen.germanCompanyName();

    // Then
    assertThat(companyName).isNotBlank().contains(" "); // Name + Suffix
    assertThat(companyName)
        .matches(".*(GmbH|AG|e\\.V\\.|OHG|GmbH & Co\\. KG)$"); // German legal forms
  }

  @Test
  void testGermanCateringCompanyName_shouldContainCateringKeywords() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();

    // When
    String companyName = gen.germanCateringCompanyName();

    // Then
    assertThat(companyName).isNotBlank();
    assertThat(companyName)
        .containsAnyOf(
            "Frische Küche",
            "Catering",
            "BioGourmet",
            "Premium Food",
            "Gastro Service",
            "Genuss",
            "Delikatessen");
  }

  @Test
  void testGermanIndustry_shouldReturnValidIndustry() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();

    // When
    String industry = gen.germanIndustry();

    // Then
    assertThat(industry)
        .isIn(
            "Gastronomie",
            "Catering",
            "Großhandel",
            "Einzelhandel",
            "Lebensmittel",
            "Hotellerie",
            "Event-Service",
            "Kantine");
  }

  @Test
  void testGermanAddress_shouldReturnRealisticAddress() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();

    // When
    String street = gen.germanStreet();
    String city = gen.germanCity();
    String postalCode = gen.germanPostalCode();
    String state = gen.germanState();

    // Then
    assertThat(street).isNotBlank().contains(" "); // Street + Number
    assertThat(city).isNotBlank();
    assertThat(postalCode).hasSize(5).matches("\\d{5}"); // 5-digit postal code
    assertThat(state)
        .isIn(
            "Bayern",
            "Baden-Württemberg",
            "Nordrhein-Westfalen",
            "Hessen",
            "Berlin",
            "Hamburg",
            "Sachsen",
            "Niedersachsen");
  }

  @Test
  void testGermanPhoneNumber_shouldReturnValidFormat() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();

    // When
    String phoneNumber = gen.germanPhoneNumber();

    // Then
    assertThat(phoneNumber).startsWith("+49 ").matches("\\+49 \\d{3} \\d{7}");
  }

  @Test
  void testGermanMobileNumber_shouldReturnValidFormat() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();

    // When
    String mobileNumber = gen.germanMobileNumber();

    // Then
    assertThat(mobileNumber).startsWith("+49 ").matches("\\+49 1[5-7][0-5] \\d{8}");
  }

  @Test
  void testEmail_shouldNormalizeUmlaute() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();

    // When
    String email1 = gen.email("Müller", "Schröder", "example.com");
    String email2 = gen.email("Björn", "Österreich", "example.com");
    String email3 = gen.email("Hans", "Süß", "example.com");

    // Then
    assertThat(email1).isEqualTo("mueller.schroeder@example.com"); // ü→ue, ö→oe
    assertThat(email2).isEqualTo("bjoern.oesterreich@example.com"); // ö→oe
    assertThat(email3).isEqualTo("hans.suess@example.com"); // ü→ue, ß→ss
  }

  @Test
  void testRandomEmail_shouldReturnValidEmail() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();

    // When
    String email = gen.email();

    // Then
    assertThat(email).contains("@").contains(".");
  }

  @Test
  void testGermanPersonNames_shouldReturnNonEmpty() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();

    // When
    String firstName = gen.germanFirstName();
    String lastName = gen.germanLastName();
    String fullName = gen.germanFullName();

    // Then
    assertThat(firstName).isNotBlank();
    assertThat(lastName).isNotBlank();
    assertThat(fullName).isNotBlank().contains(" ");
  }

  @Test
  void testPastDate_shouldReturnPastDate() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();
    LocalDate now = LocalDate.now();

    // When
    LocalDate pastDate = gen.pastDate(30);

    // Then
    assertThat(pastDate).isBefore(now).isAfter(now.minusDays(31));
  }

  @Test
  void testFutureDate_shouldReturnFutureDate() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();
    LocalDate now = LocalDate.now();

    // When
    LocalDate futureDate = gen.futureDate(30);

    // Then
    assertThat(futureDate).isAfterOrEqualTo(now).isBefore(now.plusDays(31));
  }

  @Test
  void testPastDateTime_shouldReturnPastDateTime() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();
    LocalDateTime now = LocalDateTime.now();

    // When
    LocalDateTime pastDateTime = gen.pastDateTime(24);

    // Then
    assertThat(pastDateTime).isBefore(now).isAfter(now.minusHours(25));
  }

  @Test
  void testFutureDateTime_shouldReturnFutureDateTime() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();
    LocalDateTime now = LocalDateTime.now();

    // When
    LocalDateTime futureDateTime = gen.futureDateTime(24);

    // Then
    assertThat(futureDateTime).isAfter(now).isBefore(now.plusHours(25));
  }

  @Test
  void testBusinessDescription_shouldReturnMultipleSentences() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();

    // When
    String description = gen.businessDescription();

    // Then
    assertThat(description).isNotBlank().contains(". ");
  }

  @Test
  void testShortNote_shouldReturnSingleSentence() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();

    // When
    String note = gen.shortNote();

    // Then
    assertThat(note).isNotBlank();
  }

  @Test
  void testRealisticRevenue_shouldBeInRange() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();

    // When
    double revenue = gen.realisticRevenue();

    // Then
    assertThat(revenue).isBetween(10_000.0, 10_000_000.0);
  }

  @Test
  void testRealisticEmployeeCount_shouldBeInRange() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();

    // When
    int employeeCount = gen.realisticEmployeeCount();

    // Then
    assertThat(employeeCount).isBetween(1, 5000);
  }

  @Test
  void testRealisticBudget_shouldBeInRange() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();

    // When
    double budget = gen.realisticBudget();

    // Then
    assertThat(budget).isBetween(500.0, 500_000.0);
  }

  @Test
  void testEdgeCaseCompanyName_shouldContainSpecialCharacters() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();

    // When
    String companyName = gen.edgeCaseCompanyName();

    // Then
    assertThat(companyName)
        .isNotBlank()
        .satisfiesAnyOf(
            name -> assertThat(name).containsAnyOf("ü", "ö", "ä", "ß", "é"), // Umlaute
            name -> assertThat(name).contains("&"), // Special chars
            name -> assertThat(name).contains("-"), // Hyphens
            name -> assertThat(name).contains("'"), // Apostrophes
            name -> assertThat(name).hasSizeGreaterThan(50) // Long names
            );
  }

  @Test
  void testEdgeCasePersonName_shouldContainSpecialCharacters() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();

    // When
    String personName = gen.edgeCasePersonName();

    // Then
    assertThat(personName)
        .isNotBlank()
        .satisfiesAnyOf(
            name -> assertThat(name).containsAnyOf("ü", "ö", "ä", "ø", "é"), // Umlaute
            name -> assertThat(name).contains("-"), // Hyphens
            name -> assertThat(name).contains("'"), // Apostrophes
            name -> assertThat(name).contains(" von "), // Nobility names
            name -> assertThat(name).hasSizeGreaterThan(30) // Long names
            );
  }

  @Test
  void testEdgeCaseEmail_shouldContainSpecialFormats() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();

    // When
    String email = gen.edgeCaseEmail();

    // Then
    assertThat(email).contains("@").contains(".");
    // Edge-case emails are from predefined list - just verify they're valid edge cases
    assertThat(email)
        .isIn(
            "very.long.email.address.with.many.dots@example.com",
            "special+characters@example.com",
            "name_with_underscores@example.com",
            "1234567890@example.com",
            "a@b.co");
  }

  @Test
  void testSeededGenerator_shouldProduceDeterministicResults() {
    // Given
    RealisticDataGenerator gen1 = new RealisticDataGenerator(42L);
    RealisticDataGenerator gen2 = new RealisticDataGenerator(42L);

    // When
    String company1 = gen1.germanCompanyName();
    String company2 = gen2.germanCompanyName();

    String email1 = gen1.email();
    String email2 = gen2.email();

    // Then
    assertThat(company1).isEqualTo(company2); // Same seed → same result
    assertThat(email1).isEqualTo(email2);
  }

  @Test
  void testNumberBetween_shouldReturnValueInRange() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();

    // When
    int number = gen.numberBetween(10, 20);

    // Then
    assertThat(number).isBetween(10, 20);
  }

  @Test
  void testRandomElement_shouldReturnElementFromList() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();
    List<String> list = List.of("A", "B", "C", "D");

    // When
    String element = gen.randomElement(list);

    // Then
    assertThat(element).isIn(list);
  }

  @Test
  void testGetFaker_shouldReturnNonNull() {
    // Given
    RealisticDataGenerator gen = new RealisticDataGenerator();

    // When
    var faker = gen.getFaker();

    // Then
    assertThat(faker).isNotNull();
  }
}
