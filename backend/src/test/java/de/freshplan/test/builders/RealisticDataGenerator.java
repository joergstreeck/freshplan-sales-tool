package de.freshplan.test.builders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import net.datafaker.Faker;

/**
 * RealisticDataGenerator - Realistic German test data using Datafaker
 *
 * <p>Track 2C - Advanced Test Infrastructure
 *
 * <p>Features: - German company names, addresses, phone numbers - Edge-case data (Umlaute,
 * Sonderzeichen, long names) - Realistic dates, emails, descriptions - Seed-based repeatability
 *
 * <p>Usage: RealisticDataGenerator gen = new RealisticDataGenerator(42L); // Seeded String
 * companyName = gen.germanCompanyName(); // "Müller Catering GmbH" String email =
 * gen.email("max.mustermann", "example.com"); // max.mustermann@example.com
 *
 * <p>Best Practices: - Use seed for deterministic tests - Use realistic() for production-like data
 * - Use edgeCases() for boundary testing
 */
public class RealisticDataGenerator {

  private final Faker faker;

  /** Default constructor with random seed */
  public RealisticDataGenerator() {
    this.faker = new Faker(new Locale("de", "DE"));
  }

  /**
   * Constructor with seed for deterministic test data
   *
   * @param seed Seed value for repeatable randomization
   */
  public RealisticDataGenerator(long seed) {
    this.faker = new Faker(new Locale("de", "DE"), new java.util.Random(seed));
  }

  // ================== COMPANY DATA ==================

  /**
   * Generate realistic German company name
   *
   * <p>Examples: - "Müller Catering GmbH" - "Schmidt Gastronomie GmbH & Co. KG" - "Meyer Großhandel
   * AG"
   */
  public String germanCompanyName() {
    String baseName = faker.company().name();
    List<String> suffixes = List.of("GmbH", "GmbH & Co. KG", "AG", "e.V.", "OHG");
    String suffix = suffixes.get(faker.random().nextInt(suffixes.size()));
    return baseName + " " + suffix;
  }

  /**
   * Generate realistic German food/catering company name
   *
   * <p>Examples: - "Frische Küche Müller GmbH" - "Catering Meyer & Partner" - "BioGourmet Schmidt
   * AG"
   */
  public String germanCateringCompanyName() {
    List<String> prefixes =
        List.of(
            "Frische Küche",
            "Catering",
            "BioGourmet",
            "Premium Food",
            "Gastro Service",
            "Genuss",
            "Delikatessen");
    String prefix = prefixes.get(faker.random().nextInt(prefixes.size()));
    String lastName = faker.name().lastName();
    List<String> suffixes = List.of("GmbH", "& Partner", "AG", "Service");
    String suffix = suffixes.get(faker.random().nextInt(suffixes.size()));
    return prefix + " " + lastName + " " + suffix;
  }

  /** Generate realistic company industry (German) */
  public String germanIndustry() {
    List<String> industries =
        List.of(
            "Gastronomie",
            "Catering",
            "Großhandel",
            "Einzelhandel",
            "Lebensmittel",
            "Hotellerie",
            "Event-Service",
            "Kantine");
    return industries.get(faker.random().nextInt(industries.size()));
  }

  // ================== ADDRESS DATA ==================

  /** Generate realistic German street address */
  public String germanStreet() {
    return faker.address().streetName() + " " + faker.address().streetAddressNumber();
  }

  /** Generate realistic German city */
  public String germanCity() {
    return faker.address().city();
  }

  /** Generate realistic German postal code (5 digits) */
  public String germanPostalCode() {
    return faker.address().zipCode();
  }

  /** Generate realistic German federal state */
  public String germanState() {
    List<String> states =
        List.of(
            "Bayern",
            "Baden-Württemberg",
            "Nordrhein-Westfalen",
            "Hessen",
            "Berlin",
            "Hamburg",
            "Sachsen",
            "Niedersachsen");
    return states.get(faker.random().nextInt(states.size()));
  }

  // ================== CONTACT DATA ==================

  /** Generate realistic German phone number (format: +49 xxx xxxxxxx) */
  public String germanPhoneNumber() {
    return "+49 " + faker.number().digits(3) + " " + faker.number().digits(7);
  }

  /** Generate realistic German mobile number (format: +49 1xx xxxxxxxx) */
  public String germanMobileNumber() {
    List<String> prefixes = List.of("151", "152", "160", "170", "171", "175");
    String prefix = prefixes.get(faker.random().nextInt(prefixes.size()));
    return "+49 " + prefix + " " + faker.number().digits(8);
  }

  /**
   * Generate email from first/last name
   *
   * <p>Example: email("Max", "Müller", "example.com") → max.mueller@example.com
   */
  public String email(String firstName, String lastName, String domain) {
    String normalizedFirst = normalizeForEmail(firstName);
    String normalizedLast = normalizeForEmail(lastName);
    return normalizedFirst.toLowerCase() + "." + normalizedLast.toLowerCase() + "@" + domain;
  }

  /** Generate random email */
  public String email() {
    return faker.internet().emailAddress();
  }

  /** Normalize German Umlaute for email (ä→ae, ö→oe, ü→ue, ß→ss) */
  private String normalizeForEmail(String input) {
    return input
        .replace("ä", "ae")
        .replace("ö", "oe")
        .replace("ü", "ue")
        .replace("Ä", "Ae")
        .replace("Ö", "Oe")
        .replace("Ü", "Ue")
        .replace("ß", "ss");
  }

  // ================== PERSON DATA ==================

  /** Generate realistic German first name */
  public String germanFirstName() {
    return faker.name().firstName();
  }

  /** Generate realistic German last name */
  public String germanLastName() {
    return faker.name().lastName();
  }

  /** Generate realistic German full name */
  public String germanFullName() {
    return faker.name().fullName();
  }

  // ================== DATE/TIME DATA ==================

  /** Generate date in the past (within last N days) */
  public LocalDate pastDate(int daysAgo) {
    return LocalDate.now().minusDays(faker.random().nextInt(daysAgo));
  }

  /** Generate date in the future (within next N days) */
  public LocalDate futureDate(int daysAhead) {
    return LocalDate.now().plusDays(faker.random().nextInt(daysAhead));
  }

  /** Generate datetime in the past (within last N hours) */
  public LocalDateTime pastDateTime(int hoursAgo) {
    return LocalDateTime.now().minusHours(faker.random().nextInt(hoursAgo));
  }

  /** Generate datetime in the future (within next N hours) */
  public LocalDateTime futureDateTime(int hoursAhead) {
    return LocalDateTime.now().plusHours(faker.random().nextInt(hoursAhead));
  }

  // ================== TEXT DATA ==================

  /** Generate realistic business description (1-3 sentences) */
  public String businessDescription() {
    return faker.company().catchPhrase()
        + ". "
        + faker.company().bs()
        + ". "
        + faker.lorem().sentence();
  }

  /** Generate short note/comment */
  public String shortNote() {
    return faker.lorem().sentence();
  }

  /** Generate paragraph (3-5 sentences) */
  public String paragraph() {
    return faker.lorem().paragraph();
  }

  // ================== NUMERIC DATA ==================

  /** Generate realistic revenue (10k - 10M EUR) */
  public double realisticRevenue() {
    return faker.number().numberBetween(10_000, 10_000_000);
  }

  /** Generate realistic employee count (1-5000) */
  public int realisticEmployeeCount() {
    return faker.number().numberBetween(1, 5000);
  }

  /** Generate realistic budget (500 - 500k EUR) */
  public double realisticBudget() {
    return faker.number().numberBetween(500, 500_000);
  }

  // ================== EDGE CASES ==================

  /**
   * Generate edge-case company name (Umlaute, long name, special characters)
   *
   * <p>Examples: - "Müller-Großhandel GmbH & Co. KG" - "Café Österreich GmbH" - "Süß & Würzig AG"
   */
  public String edgeCaseCompanyName() {
    List<String> edgeCases =
        List.of(
            "Müller-Großhandel GmbH & Co. KG",
            "Café Österreich GmbH",
            "Süß & Würzig AG",
            "Schröder's Delikatessen",
            "O'Brien Catering",
            "123-Food Service",
            "A".repeat(100) + " GmbH" // Max length test
            );
    return edgeCases.get(faker.random().nextInt(edgeCases.size()));
  }

  /**
   * Generate edge-case person name (Umlaute, double names, long names)
   *
   * <p>Examples: - "Müller-Schmidt" - "O'Connor" - "Björn von Münchhausen"
   */
  public String edgeCasePersonName() {
    List<String> edgeCases =
        List.of(
            "Müller-Schmidt",
            "O'Connor",
            "Björn von Münchhausen",
            "Søren Österreich",
            "José García-López",
            "A".repeat(50) // Max length test
            );
    return edgeCases.get(faker.random().nextInt(edgeCases.size()));
  }

  /**
   * Generate edge-case email (long local part, special characters)
   *
   * <p>Examples: - "very.long.email.address.with.many.dots@example.com" -
   * "special+characters@example.com"
   */
  public String edgeCaseEmail() {
    List<String> edgeCases =
        List.of(
            "very.long.email.address.with.many.dots@example.com",
            "special+characters@example.com",
            "name_with_underscores@example.com",
            "1234567890@example.com",
            "a@b.co" // Short email
            );
    return edgeCases.get(faker.random().nextInt(edgeCases.size()));
  }

  // ================== HELPER METHODS ==================

  /** Get raw Faker instance for advanced usage */
  public Faker getFaker() {
    return faker;
  }

  /** Generate random integer between min and max (inclusive) */
  public int numberBetween(int min, int max) {
    return faker.number().numberBetween(min, max);
  }

  /** Generate random element from list */
  public <T> T randomElement(List<T> list) {
    return list.get(faker.random().nextInt(list.size()));
  }
}
