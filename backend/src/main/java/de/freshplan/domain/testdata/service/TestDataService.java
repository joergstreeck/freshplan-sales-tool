package de.freshplan.domain.testdata.service;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.repository.CustomerTimelineRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.jboss.logging.Logger;

/**
 * Service for managing test data in the development environment. Provides clean seeding and removal
 * of test data.
 */
@ApplicationScoped
public class TestDataService {

  private static final Logger LOG = Logger.getLogger(TestDataService.class);
  private static final String TEST_USER = "test-data-seeder";

  @Inject CustomerRepository customerRepository;

  @Inject CustomerTimelineRepository timelineRepository;

  /**
   * Seeds the database with 5 diverse test customers. All test data is marked with is_test_data =
   * true and prefixed with [TEST].
   */
  @Transactional
  public SeedResult seedTestData() {
    LOG.info("Starting test data seeding...");

    List<Customer> createdCustomers = new ArrayList<>();
    List<CustomerTimelineEvent> createdEvents = new ArrayList<>();

    try {
      // 1. Risiko-Kunde (90+ Tage ohne Kontakt)
      Customer riskCustomer =
          createTestCustomer(
              "90001",
              "[TEST] Bäckerei Schmidt - 90 Tage ohne Kontakt",
              CustomerStatus.RISIKO,
              LocalDateTime.now().minusDays(92),
              Industry.EINZELHANDEL // Bäckerei passt zu Einzelhandel
              );
      riskCustomer.setRiskScore(85);
      customerRepository.persist(riskCustomer);
      createdCustomers.add(riskCustomer);

      // Add timeline events
      createdEvents.add(
          createTimelineEvent(
              riskCustomer,
              EventCategory.NOTE,
              "Letzter Kontakt vor 3 Monaten - dringend anrufen!",
              LocalDateTime.now().minusDays(92)));

      // 2. Aktiver Partner-Kunde
      Customer activeCustomer =
          createTestCustomer(
              "90002",
              "[TEST] Hotel Sonnenschein - Aktiver Partner",
              CustomerStatus.AKTIV,
              LocalDateTime.now().minusDays(5),
              Industry.HOTEL);
      activeCustomer.setPartnerStatus(PartnerStatus.GOLD_PARTNER);
      activeCustomer.setExpectedAnnualVolume(new BigDecimal("50000.00"));
      activeCustomer.setRiskScore(10);
      customerRepository.persist(activeCustomer);
      createdCustomers.add(activeCustomer);

      createdEvents.add(
          createTimelineEvent(
              activeCustomer,
              EventCategory.QUOTE,
              "Neues Angebot für Frühstücksbuffet versendet",
              LocalDateTime.now().minusDays(5)));

      // 3. Neuer Lead ohne Kontakt
      Customer leadCustomer =
          createTestCustomer(
              "90003",
              "[TEST] Restaurant Mustermeier - Neuer Lead",
              CustomerStatus.LEAD,
              null, // Noch nie kontaktiert
              Industry.RESTAURANT);
      leadCustomer.setRiskScore(50);
      customerRepository.persist(leadCustomer);
      createdCustomers.add(leadCustomer);

      // 4. Inaktiver Kunde (180+ Tage)
      Customer inactiveCustomer =
          createTestCustomer(
              "90004",
              "[TEST] Catering Service Alt - 180 Tage inaktiv",
              CustomerStatus.INAKTIV,
              LocalDateTime.now().minusDays(185),
              Industry.CATERING);
      inactiveCustomer.setRiskScore(95);
      customerRepository.persist(inactiveCustomer);
      createdCustomers.add(inactiveCustomer);

      createdEvents.add(
          createTimelineEvent(
              inactiveCustomer,
              EventCategory.PHONE_CALL,
              "Kunde hat kein Interesse mehr - zu teuer",
              LocalDateTime.now().minusDays(185)));

      // 5. Prospect mit überfälligem Follow-up
      Customer prospectCustomer =
          createTestCustomer(
              "90005",
              "[TEST] Kantine TechHub - Überfälliger Follow-up",
              CustomerStatus.PROSPECT,
              LocalDateTime.now().minusDays(15),
              Industry.KANTINE // Kantine passt zu Großverbraucher
              );
      prospectCustomer.setNextFollowUpDate(LocalDateTime.now().minusDays(3));
      prospectCustomer.setRiskScore(60);
      customerRepository.persist(prospectCustomer);
      createdCustomers.add(prospectCustomer);

      createdEvents.add(
          createTimelineEvent(
              prospectCustomer,
              EventCategory.EMAIL,
              "Interesse an Bio-Produkten bekundet",
              LocalDateTime.now().minusDays(15)));

      // Persist all timeline events
      for (CustomerTimelineEvent event : createdEvents) {
        timelineRepository.persist(event);
      }

      LOG.infof(
          "Successfully seeded %d test customers with %d timeline events",
          createdCustomers.size(), createdEvents.size());

      return new SeedResult(createdCustomers.size(), createdEvents.size());

    } catch (Exception e) {
      LOG.error("Error seeding test data", e);
      throw new RuntimeException("Failed to seed test data: " + e.getMessage(), e);
    }
  }

  /** Removes all test data from the database. */
  @Transactional
  public CleanupResult cleanTestData() {
    LOG.info("Starting test data cleanup...");

    try {
      // Delete timeline events first (due to foreign key constraints)
      long deletedEvents = timelineRepository.delete("isTestData", true);

      // Delete customers
      long deletedCustomers = customerRepository.delete("isTestData", true);

      LOG.infof(
          "Successfully cleaned up %d customers and %d timeline events",
          deletedCustomers, deletedEvents);

      return new CleanupResult(deletedCustomers, deletedEvents);

    } catch (Exception e) {
      LOG.error("Error cleaning test data", e);
      throw new RuntimeException("Failed to clean test data: " + e.getMessage(), e);
    }
  }

  /** Counts existing test data in the database. */
  public TestDataStats getTestDataStats() {
    long customerCount = customerRepository.count("isTestData", true);
    long eventCount = timelineRepository.count("isTestData", true);

    return new TestDataStats(customerCount, eventCount);
  }

  /**
   * Removes old test data (non-[TEST] prefixed) from the database. This is a one-time cleanup
   * operation.
   */
  @Transactional
  public CleanupResult cleanOldTestData() {
    LOG.info("Starting old test data cleanup...");

    try {
      // Delete timeline events for old test customers first
      long deletedEvents =
          timelineRepository.delete(
              "(isTestData is null or isTestData = false) and customer.companyName not like '[TEST]%'");

      // Delete old test customers
      long deletedCustomers =
          customerRepository.delete(
              "(isTestData is null or isTestData = false) and companyName not like '[TEST]%'");

      LOG.infof(
          "Successfully cleaned up %d old customers and %d timeline events",
          deletedCustomers, deletedEvents);

      return new CleanupResult(deletedCustomers, deletedEvents);

    } catch (Exception e) {
      LOG.error("Error cleaning old test data", e);
      throw new RuntimeException("Failed to clean old test data: " + e.getMessage(), e);
    }
  }

  private Customer createTestCustomer(
      String customerNumber,
      String companyName,
      CustomerStatus status,
      LocalDateTime lastContactDate,
      Industry industry) {

    Customer customer = new Customer();
    customer.setCustomerNumber(customerNumber);
    customer.setCompanyName(companyName);
    customer.setStatus(status);
    customer.setLastContactDate(lastContactDate);
    customer.setIndustry(industry);

    // Common fields
    customer.setCustomerType(CustomerType.UNTERNEHMEN);
    customer.setLifecycleStage(CustomerLifecycleStage.ACQUISITION);
    customer.setPaymentTerms(PaymentTerms.NETTO_30);
    customer.setCreatedBy(TEST_USER);
    customer.setIsDeleted(false);

    // Mark as test data
    customer.setIsTestData(true);

    // Sprint 2 fields - set defaults to avoid NOT NULL constraint violations
    customer.setLocationsGermany(1);
    customer.setLocationsAustria(0);
    customer.setLocationsSwitzerland(0);
    customer.setLocationsRestEU(0);
    customer.setTotalLocationsEU(1);
    customer.setPainPoints(new ArrayList<>());
    customer.setPrimaryFinancing(FinancingType.PRIVATE);

    return customer;
  }

  private CustomerTimelineEvent createTimelineEvent(
      Customer customer, EventCategory category, String description, LocalDateTime eventDate) {

    CustomerTimelineEvent event = new CustomerTimelineEvent();
    event.setCustomer(customer);
    event.setCategory(category);
    event.setDescription(description);
    event.setEventDate(eventDate);
    event.setIsDeleted(false);

    // Required fields
    event.setEventType(category.toString());
    event.setTitle("[TEST] " + description.substring(0, Math.min(description.length(), 50)));
    event.setPerformedBy(TEST_USER);
    event.setImportance(ImportanceLevel.MEDIUM);

    // Mark as test data
    event.setIsTestData(true);

    return event;
  }

  /**
   * Seeds additional test customers to reach exactly 58 total customers. Call this after
   * seedTestData() and seedComprehensiveTestData() to get the missing 14 customers.
   */
  @Transactional
  public SeedResult seedAdditionalTestData() {
    LOG.info("Seeding additional 14 test customers to reach 58 total...");

    List<Customer> createdCustomers = new ArrayList<>();

    try {
      // Create 14 additional diverse test customers
      for (int i = 1; i <= 14; i++) {
        Customer customer = new Customer();
        customer.setCustomerNumber("ADD-" + String.format("%03d", i));
        customer.setCompanyName("[TEST] Zusatzkunde " + i);
        customer.setCustomerType(CustomerType.UNTERNEHMEN);
        customer.setStatus(
            i % 4 == 0
                ? CustomerStatus.AKTIV
                : i % 3 == 0
                    ? CustomerStatus.RISIKO
                    : i % 2 == 0 ? CustomerStatus.INAKTIV : CustomerStatus.LEAD);
        customer.setIndustry(
            i % 5 == 0
                ? Industry.HOTEL
                : i % 4 == 0
                    ? Industry.RESTAURANT
                    : i % 3 == 0
                        ? Industry.EINZELHANDEL
                        : i % 2 == 0 ? Industry.CATERING : Industry.SONSTIGE);
        customer.setLifecycleStage(CustomerLifecycleStage.ACQUISITION);
        customer.setPaymentTerms(PaymentTerms.NETTO_30);
        customer.setCreatedBy(TEST_USER);
        customer.setIsDeleted(false);
        customer.setIsTestData(true);

        // Sprint 2 fields
        customer.setLocationsGermany(i);
        customer.setLocationsAustria(i % 3);
        customer.setLocationsSwitzerland(i % 4);
        customer.setLocationsRestEU(i % 2);
        customer.setTotalLocationsEU(i + (i % 3) + (i % 4) + (i % 2));
        customer.setPainPoints(new ArrayList<>());
        customer.setPrimaryFinancing(i % 2 == 0 ? FinancingType.PRIVATE : FinancingType.PUBLIC);
        customer.setRiskScore(i * 7 % 100);

        if (i % 3 == 0) {
          customer.setLastContactDate(LocalDateTime.now().minusDays(i * 10));
        }

        customerRepository.persist(customer);
        createdCustomers.add(customer);
      }

      LOG.infof("Successfully seeded %d additional test customers", createdCustomers.size());
      return new SeedResult(createdCustomers.size(), 0);

    } catch (Exception e) {
      LOG.error("Error seeding additional test data", e);
      throw new RuntimeException("Failed to seed additional test data: " + e.getMessage(), e);
    }
  }

  /**
   * Seeds comprehensive edge-case test data for thorough testing.
   *
   * <p>This creates systematic test data to catch bugs early: - String boundary tests (min/max
   * lengths, special chars) - Numeric edge cases (zero, max values, precision) - Date/time edge
   * cases (past, future, timezones) - All enum combinations - Business logic variations - Unicode &
   * special character tests
   */
  @Transactional
  public SeedResult seedComprehensiveTestData() {
    LOG.info("🧪 Starting comprehensive edge-case test data seeding...");

    List<Customer> createdCustomers = new ArrayList<>();

    try {
      // 1. STRING BOUNDARY TESTS
      LOG.info("Creating string boundary test cases...");
      createdCustomers.addAll(createStringBoundaryTests());

      // 2. NUMERIC EDGE CASES
      LOG.info("Creating numeric edge case tests...");
      createdCustomers.addAll(createNumericEdgeCases());

      // 3. DATE/TIME EDGE CASES
      LOG.info("Creating date/time edge cases...");
      createdCustomers.addAll(createDateTimeEdgeCases());

      // 4. ENUM BOUNDARY TESTING
      LOG.info("Creating enum boundary tests...");
      createdCustomers.addAll(createEnumBoundaryTests());

      // 5. BUSINESS LOGIC VARIATIONS
      LOG.info("Creating business logic variation tests...");
      createdCustomers.addAll(createBusinessLogicVariations());

      // 6. UNICODE & SPECIAL CHARACTER TESTS
      LOG.info("Creating unicode and special character tests...");
      createdCustomers.addAll(createUnicodeTests());

      // Persist all customers
      for (Customer customer : createdCustomers) {
        customerRepository.persist(customer);
      }

      LOG.infof("🎯 Comprehensive test data seeded! Total customers: %d", createdCustomers.size());
      LOG.info("💡 This covers all edge cases for thorough testing");

      return new SeedResult(createdCustomers.size(), 0);

    } catch (Exception e) {
      LOG.error("Error seeding comprehensive test data", e);
      throw new RuntimeException("Failed to seed comprehensive test data: " + e.getMessage(), e);
    }
  }

  private List<Customer> createStringBoundaryTests() {
    List<Customer> customers = new ArrayList<>();

    // Test 1: Minimal string length (1 character)
    Customer minimal =
        createTestCustomer(
            "MIN-001",
            "[TEST] A", // Minimal 1 Zeichen
            CustomerStatus.LEAD,
            LocalDateTime.now().minusDays(1),
            Industry.SONSTIGE);
    customers.add(minimal);

    // Test 2: Maximum string length (254 characters - limit for [TEST] prefix)
    String maxName = "[TEST] " + "X".repeat(247); // 254 total chars
    Customer maximal =
        createTestCustomer(
            "MAX-001",
            maxName,
            CustomerStatus.LEAD,
            LocalDateTime.now().minusDays(1),
            Industry.SONSTIGE);
    customers.add(maximal);

    // Test 3: Special characters & umlauts
    Customer special =
        createTestCustomer(
            "SPEC-001",
            "[TEST] Café & Restaurant 'Zur Sonne' - Inh. Müller",
            CustomerStatus.AKTIV,
            LocalDateTime.now().minusDays(1),
            Industry.RESTAURANT);
    special.setTradingName("Café 'L'Étoile'");
    special.setLegalForm("GmbH & Co. KG");
    customers.add(special);

    return customers;
  }

  private List<Customer> createNumericEdgeCases() {
    List<Customer> customers = new ArrayList<>();

    // Test 1: Zero values
    Customer zeroCustomer =
        createTestCustomer(
            "ZERO-001",
            "[TEST] Zero Values Test Company",
            CustomerStatus.LEAD,
            LocalDateTime.now().minusDays(1),
            Industry.SONSTIGE);
    zeroCustomer.setExpectedAnnualVolume(BigDecimal.ZERO);
    zeroCustomer.setActualAnnualVolume(BigDecimal.ZERO);
    zeroCustomer.setCreditLimit(BigDecimal.ZERO);
    zeroCustomer.setRiskScore(0);
    customers.add(zeroCustomer);

    // Test 2: Maximum numeric values
    Customer maxNumeric =
        createTestCustomer(
            "MAXNUM-001",
            "[TEST] Maximum Numbers Test Company",
            CustomerStatus.AKTIV,
            LocalDateTime.now().minusDays(1),
            Industry.SONSTIGE);
    maxNumeric.setExpectedAnnualVolume(
        new BigDecimal("9999999999.99")); // Max 10 Stellen + 2 Nachkomma
    maxNumeric.setActualAnnualVolume(new BigDecimal("9999999999.99"));
    maxNumeric.setCreditLimit(new BigDecimal("9999999999.99"));
    maxNumeric.setRiskScore(100); // Maximum Risk Score
    customers.add(maxNumeric);

    // Test 3: Precise decimal values
    Customer precise =
        createTestCustomer(
            "PREC-001",
            "[TEST] Decimal Precision Test Company",
            CustomerStatus.AKTIV,
            LocalDateTime.now().minusDays(1),
            Industry.SONSTIGE);
    precise.setExpectedAnnualVolume(new BigDecimal("999.99"));
    precise.setActualAnnualVolume(new BigDecimal("1000.01"));
    precise.setCreditLimit(new BigDecimal("0.01")); // Minimal positive value
    precise.setRiskScore(50); // Medium risk
    customers.add(precise);

    return customers;
  }

  private List<Customer> createDateTimeEdgeCases() {
    List<Customer> customers = new ArrayList<>();
    LocalDateTime now = LocalDateTime.now();

    // Test 1: Past dates
    Customer pastCustomer =
        createTestCustomer(
            "PAST-001",
            "[TEST] Past Dates Test Company",
            CustomerStatus.INAKTIV,
            now.minusYears(2), // 2 Jahre alt
            Industry.SONSTIGE);
    pastCustomer.setNextFollowUpDate(now.plusDays(1)); // Zukunft (required @Future)
    customers.add(pastCustomer);

    // Test 2: Far future dates
    Customer futureCustomer =
        createTestCustomer(
            "FUTURE-001",
            "[TEST] Future Dates Test Company",
            CustomerStatus.LEAD,
            now.minusDays(1),
            Industry.SONSTIGE);
    futureCustomer.setNextFollowUpDate(now.plusYears(1)); // 1 Jahr in Zukunft
    customers.add(futureCustomer);

    // Test 3: Leap year and timezone edge cases
    Customer leapYear =
        createTestCustomer(
            "LEAP-001",
            "[TEST] Leap Year Test Company",
            CustomerStatus.AKTIV,
            LocalDateTime.of(2024, 2, 29, 12, 0), // Schaltjahr
            Industry.SONSTIGE);
    leapYear.setNextFollowUpDate(LocalDateTime.of(2024, 12, 31, 23, 59)); // Jahresende
    customers.add(leapYear);

    return customers;
  }

  private List<Customer> createEnumBoundaryTests() {
    List<Customer> customers = new ArrayList<>();
    int counter = 1;

    // Test alle CustomerType Werte
    for (CustomerType type : CustomerType.values()) {
      Customer customer =
          createTestCustomer(
              "TYPE-" + String.format("%03d", counter++),
              "[TEST] Test Company for " + type.name(),
              CustomerStatus.LEAD,
              LocalDateTime.now().minusDays(1),
              Industry.SONSTIGE);
      customer.setCustomerType(type);
      customers.add(customer);
    }

    // Test alle Industry Werte
    counter = 1;
    for (Industry industry : Industry.values()) {
      Customer customer =
          createTestCustomer(
              "IND-" + String.format("%03d", counter++),
              "[TEST] Test Company for " + industry.name(),
              CustomerStatus.AKTIV,
              LocalDateTime.now().minusDays(1),
              industry);
      customers.add(customer);
    }

    // Test alle CustomerStatus Werte
    counter = 1;
    for (CustomerStatus status : CustomerStatus.values()) {
      Customer customer =
          createTestCustomer(
              "STAT-" + String.format("%03d", counter++),
              "[TEST] Test Company for " + status.name(),
              status,
              LocalDateTime.now().minusDays(1),
              Industry.SONSTIGE);
      customers.add(customer);
    }

    return customers;
  }

  private List<Customer> createBusinessLogicVariations() {
    List<Customer> customers = new ArrayList<>();

    // Test 1: Hierarchie-Ketten (Headquarter -> Filiale)
    Customer headquarter =
        createTestCustomer(
            "HQ-001",
            "[TEST] Global Catering Headquarter",
            CustomerStatus.AKTIV,
            LocalDateTime.now().minusDays(1),
            Industry.CATERING);
    headquarter.setClassification(Classification.A_KUNDE);
    headquarter.setHierarchyType(CustomerHierarchyType.HEADQUARTER);
    headquarter.setExpectedAnnualVolume(new BigDecimal("500000.00"));
    customers.add(headquarter);

    Customer filiale =
        createTestCustomer(
            "FIL-001",
            "[TEST] Global Catering Filiale Berlin",
            CustomerStatus.AKTIV,
            LocalDateTime.now().minusDays(1),
            Industry.CATERING);
    filiale.setClassification(Classification.A_KUNDE);
    filiale.setHierarchyType(CustomerHierarchyType.FILIALE);
    filiale.setExpectedAnnualVolume(new BigDecimal("150000.00"));
    customers.add(filiale);

    // Test 2: Risk Score Extreme Cases
    Customer highRisk =
        createTestCustomer(
            "RISK-HIGH",
            "[TEST] High Risk Customer Ltd.",
            CustomerStatus.RISIKO,
            LocalDateTime.now().minusDays(1),
            Industry.SONSTIGE);
    highRisk.setClassification(Classification.C_KUNDE);
    highRisk.setRiskScore(95); // Sehr hohes Risiko
    highRisk.setCreditLimit(new BigDecimal("1000.00")); // Niedriges Limit bei hohem Risiko
    highRisk.setPaymentTerms(PaymentTerms.VORKASSE); // Sicherste Zahlung
    customers.add(highRisk);

    Customer lowRisk =
        createTestCustomer(
            "RISK-LOW",
            "[TEST] Premium Low Risk Customer GmbH",
            CustomerStatus.AKTIV,
            LocalDateTime.now().minusDays(1),
            Industry.HOTEL);
    lowRisk.setClassification(Classification.A_KUNDE);
    lowRisk.setRiskScore(5); // Sehr niedriges Risiko
    lowRisk.setCreditLimit(new BigDecimal("100000.00")); // Hohes Limit bei niedrigem Risiko
    lowRisk.setPaymentTerms(PaymentTerms.NETTO_60); // Längere Zahlungsfrist
    customers.add(lowRisk);

    return customers;
  }

  private List<Customer> createUnicodeTests() {
    List<Customer> customers = new ArrayList<>();

    // Test 1: Deutsche Umlaute und Sonderzeichen
    Customer german =
        createTestCustomer(
            "UNI-DE",
            "[TEST] Bäckerei Müßigmann GmbH & Co. KG",
            CustomerStatus.AKTIV,
            LocalDateTime.now().minusDays(1),
            Industry.EINZELHANDEL);
    german.setTradingName("Bäckerei Müßigmann");
    german.setLegalForm("GmbH & Co. KG");
    customers.add(german);

    // Test 2: Französische Akzente
    Customer french =
        createTestCustomer(
            "UNI-FR",
            "[TEST] Café L'Étoile - Spécialités Françaises",
            CustomerStatus.AKTIV,
            LocalDateTime.now().minusDays(1),
            Industry.RESTAURANT);
    french.setTradingName("Café L'Étoile");
    customers.add(french);

    // Test 3: Chinesische Zeichen (falls internationaler Support)
    Customer chinese =
        createTestCustomer(
            "UNI-CN",
            "[TEST] 北京烤鸭餐厅", // "Beijing Roast Duck Restaurant"
            CustomerStatus.AKTIV,
            LocalDateTime.now().minusDays(1),
            Industry.RESTAURANT);
    chinese.setTradingName("Beijing Restaurant");
    customers.add(chinese);

    // Test 4: Emojis und spezielle Unicode
    Customer emoji =
        createTestCustomer(
            "UNI-EMOJI",
            "[TEST] 🏨 Hotel Emoji & Wellness Resort",
            CustomerStatus.AKTIV,
            LocalDateTime.now().minusDays(1),
            Industry.HOTEL);
    emoji.setTradingName("Hotel Emoji");
    customers.add(emoji);

    // Test 5: Bindestrich, Apostrophe und Anführungszeichen
    Customer punctuation =
        createTestCustomer(
            "UNI-PUNCT",
            "[TEST] O'Connor's Irish Pub - \"The Best\" Food & Drinks",
            CustomerStatus.AKTIV,
            LocalDateTime.now().minusDays(1),
            Industry.RESTAURANT);
    punctuation.setTradingName("O'Connor's");
    customers.add(punctuation);

    return customers;
  }

  // Result classes
  public record SeedResult(int customersCreated, int eventsCreated) {}

  public record CleanupResult(long customersDeleted, long eventsDeleted) {}

  public record TestDataStats(long customerCount, long eventCount) {}
}
