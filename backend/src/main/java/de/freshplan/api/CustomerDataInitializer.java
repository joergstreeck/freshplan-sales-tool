package de.freshplan.api;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.repository.CustomerTimelineRepository;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

/**
 * Comprehensive test data initializer for all edge cases and scenarios.
 *
 * <p>Creates systematic test data to catch bugs early: - Normal business cases (realistic data) -
 * String boundary tests (min/max lengths, special chars) - Numeric edge cases (zero, max values,
 * precision) - Date/time edge cases (past, future, timezones) - All enum combinations - Business
 * logic variations
 *
 * <p>Only active in dev profile.
 */
@ApplicationScoped
@IfBuildProfile("dev")
public class CustomerDataInitializer {

  private static final Logger LOG = Logger.getLogger(CustomerDataInitializer.class);

  @Inject CustomerRepository customerRepository;
  @Inject CustomerTimelineRepository timelineRepository;

  @Transactional
  void onStart(@Observes StartupEvent ev) {
    LOG.info("🧪 Initializing comprehensive test data for edge case testing...");

    // For comprehensive testing: Always recreate test data
    // Clear existing data - SQL-based cascade deletion approach
    long existingCount = customerRepository.count();
    if (existingCount > 0) {
      LOG.info("Found " + existingCount + " existing customers, clearing for fresh test data...");
      // Use SQL directly to avoid Hibernate/JPA cascade issues
      // Delete in dependency order: child tables first, then parent tables
      var em = customerRepository.getEntityManager();

      // Security: Define allowed tables for deletion (prevents SQL injection)
      var tablesToClear =
          java.util.List.of(
              "customer_timeline_events", "customer_contacts", "customer_locations", "customers");

      // Derive allowed tables from the clearing list to ensure consistency
      var allowedTables = java.util.Set.copyOf(tablesToClear);

      // CRITICAL FIX: Delete opportunities first to avoid foreign key constraint violations
      LOG.info("Clearing opportunities before customers to avoid FK violations...");
      em.createNativeQuery("DELETE FROM opportunity_activities").executeUpdate();
      em.createNativeQuery("DELETE FROM opportunities").executeUpdate();

      for (String table : tablesToClear) {
        if (!allowedTables.contains(table)) {
          throw new IllegalArgumentException("Invalid table name for deletion: " + table);
        }
        em.createNativeQuery("DELETE FROM " + table).executeUpdate();
      }
      LOG.info("Existing customers and related data cleared via SQL");
    }

    // 1. NORMAL BUSINESS CASES (Realistische Szenarien)
    LOG.info("Creating normal business test cases...");
    createHotelCustomer();
    createRestaurantCustomer();
    createCateringCustomer();
    createSchoolCustomer();
    createEventCustomer();

    // 2. STRING BOUNDARY TESTS
    LOG.info("Creating string boundary test cases...");
    createStringBoundaryTests();

    // 3. NUMERIC EDGE CASES
    LOG.info("Creating numeric edge case tests...");
    createNumericEdgeCases();

    // 4. DATE/TIME EDGE CASES
    LOG.info("Creating date/time edge cases...");
    createDateTimeEdgeCases();

    // 5. ENUM BOUNDARY TESTING
    LOG.info("Creating enum boundary tests...");
    createEnumBoundaryTests();

    // 6. BUSINESS LOGIC VARIATIONS
    LOG.info("Creating business logic variation tests...");
    createBusinessLogicVariations();

    // 7. UNICODE & SPECIAL CHARACTER TESTS
    LOG.info("Creating unicode and special character tests...");
    createUnicodeTests();

    long totalCustomers = customerRepository.count();
    LOG.info(
        "🎯 Comprehensive test data initialized successfully! Total customers: " + totalCustomers);
    LOG.info("💡 This covers all edge cases for thorough testing");
  }

  private void createHotelCustomer() {
    Customer customer = new Customer();
    customer.setCustomerNumber("KD-2025-00001");
    customer.setCompanyName("Grand Hotel Berlin");
    customer.setTradingName("Grand Hotel");
    customer.setLegalForm("GmbH");
    customer.setCustomerType(CustomerType.UNTERNEHMEN);
    customer.setIndustry(Industry.HOTEL);
    customer.setClassification(Classification.A_KUNDE);
    customer.setHierarchyType(CustomerHierarchyType.STANDALONE);
    customer.setStatus(CustomerStatus.AKTIV);
    customer.setLifecycleStage(CustomerLifecycleStage.GROWTH);
    customer.setExpectedAnnualVolume(new BigDecimal("120000.00"));
    customer.setActualAnnualVolume(new BigDecimal("98000.00"));
    customer.setPaymentTerms(PaymentTerms.NETTO_30);
    customer.setCreditLimit(new BigDecimal("25000.00"));
    customer.setDeliveryCondition(DeliveryCondition.STANDARD);
    customer.setLastContactDate(LocalDateTime.now().minusDays(5));
    customer.setNextFollowUpDate(LocalDateTime.now().plusDays(10));
    customer.setCreatedBy("system");

    customerRepository.persist(customer);
    LOG.info("Created hotel customer: " + customer.getCompanyName());
  }

  private void createRestaurantCustomer() {
    Customer customer = new Customer();
    customer.setCustomerNumber("KD-2025-00002");
    customer.setCompanyName("Bella Italia Restaurant");
    customer.setTradingName("Bella Italia");
    customer.setLegalForm("Einzelunternehmen");
    customer.setCustomerType(CustomerType.UNTERNEHMEN);
    customer.setIndustry(Industry.RESTAURANT);
    customer.setClassification(Classification.B_KUNDE);
    customer.setHierarchyType(CustomerHierarchyType.STANDALONE);
    customer.setStatus(CustomerStatus.AKTIV);
    customer.setLifecycleStage(CustomerLifecycleStage.RETENTION);
    customer.setExpectedAnnualVolume(new BigDecimal("45000.00"));
    customer.setActualAnnualVolume(new BigDecimal("42000.00"));
    customer.setPaymentTerms(PaymentTerms.NETTO_14);
    customer.setCreditLimit(new BigDecimal("10000.00"));
    customer.setDeliveryCondition(DeliveryCondition.EXPRESS);
    customer.setLastContactDate(LocalDateTime.now().minusDays(2));
    customer.setNextFollowUpDate(LocalDateTime.now().plusDays(7));
    customer.setCreatedBy("system");

    customerRepository.persist(customer);
    LOG.info("Created restaurant customer: " + customer.getCompanyName());
  }

  private void createCateringCustomer() {
    Customer customer = new Customer();
    customer.setCustomerNumber("KD-2025-00003");
    customer.setCompanyName("Event Catering München GmbH");
    customer.setTradingName("Event Catering");
    customer.setLegalForm("GmbH");
    customer.setCustomerType(CustomerType.UNTERNEHMEN);
    customer.setIndustry(Industry.CATERING);
    customer.setClassification(Classification.A_KUNDE);
    customer.setHierarchyType(CustomerHierarchyType.HEADQUARTER);
    customer.setStatus(CustomerStatus.AKTIV);
    customer.setLifecycleStage(CustomerLifecycleStage.GROWTH);
    customer.setExpectedAnnualVolume(new BigDecimal("200000.00"));
    customer.setActualAnnualVolume(new BigDecimal("180000.00"));
    customer.setPaymentTerms(PaymentTerms.NETTO_30);
    customer.setCreditLimit(new BigDecimal("50000.00"));
    customer.setDeliveryCondition(DeliveryCondition.STANDARD);
    customer.setLastContactDate(LocalDateTime.now().minusDays(1));
    customer.setNextFollowUpDate(LocalDateTime.now().plusDays(14));
    customer.setCreatedBy("system");

    customerRepository.persist(customer);
    LOG.info("Created catering customer: " + customer.getCompanyName());
  }

  private void createSchoolCustomer() {
    Customer customer = new Customer();
    customer.setCustomerNumber("KD-2025-00004");
    customer.setCompanyName("Städtische Grundschule Nord");
    customer.setTradingName("Grundschule Nord");
    customer.setLegalForm("Öffentliche Einrichtung");
    customer.setCustomerType(CustomerType.INSTITUTION);
    customer.setIndustry(Industry.BILDUNG);
    customer.setClassification(Classification.C_KUNDE);
    customer.setHierarchyType(CustomerHierarchyType.STANDALONE);
    customer.setStatus(CustomerStatus.AKTIV);
    customer.setLifecycleStage(CustomerLifecycleStage.RETENTION);
    customer.setExpectedAnnualVolume(new BigDecimal("35000.00"));
    customer.setActualAnnualVolume(new BigDecimal("33000.00"));
    customer.setPaymentTerms(PaymentTerms.NETTO_60);
    customer.setCreditLimit(new BigDecimal("15000.00"));
    customer.setDeliveryCondition(DeliveryCondition.STANDARD);
    customer.setLastContactDate(LocalDateTime.now().minusDays(10));
    customer.setNextFollowUpDate(LocalDateTime.now().plusDays(30));
    customer.setCreatedBy("system");

    customerRepository.persist(customer);
    LOG.info("Created school customer: " + customer.getCompanyName());
  }

  private void createEventCustomer() {
    Customer customer = new Customer();
    customer.setCustomerNumber("KD-2025-00005");
    customer.setCompanyName("Messe Frankfurt Event GmbH");
    customer.setTradingName("Messe Frankfurt");
    customer.setLegalForm("GmbH");
    customer.setCustomerType(CustomerType.UNTERNEHMEN);
    customer.setIndustry(Industry.VERANSTALTUNG);
    customer.setClassification(Classification.A_KUNDE);
    customer.setHierarchyType(CustomerHierarchyType.STANDALONE);
    customer.setStatus(CustomerStatus.LEAD);
    customer.setLifecycleStage(CustomerLifecycleStage.ACQUISITION);
    customer.setExpectedAnnualVolume(new BigDecimal("300000.00"));
    customer.setActualAnnualVolume(new BigDecimal("0.00"));
    customer.setPaymentTerms(PaymentTerms.NETTO_30);
    customer.setCreditLimit(new BigDecimal("75000.00"));
    customer.setDeliveryCondition(DeliveryCondition.EXPRESS);
    customer.setLastContactDate(LocalDateTime.now().minusDays(3));
    customer.setNextFollowUpDate(LocalDateTime.now().plusDays(5));
    customer.setCreatedBy("system");

    customerRepository.persist(customer);
    LOG.info("Created event customer: " + customer.getCompanyName());
  }

  // ========================================================================
  // STRING BOUNDARY TESTS
  // ========================================================================

  private void createStringBoundaryTests() {
    // Test 1: Minimal string length (1 character)
    Customer minimal = new Customer();
    minimal.setCustomerNumber("MIN-001");
    minimal.setCompanyName("A"); // Minimal 1 Zeichen
    minimal.setCustomerType(CustomerType.UNTERNEHMEN);
    minimal.setIndustry(Industry.SONSTIGE);
    minimal.setClassification(Classification.C_KUNDE);
    minimal.setStatus(CustomerStatus.LEAD);
    minimal.setCreatedBy("test");
    customerRepository.persist(minimal);

    // Test 2: Maximum string length (255 characters for companyName)
    Customer maximal = new Customer();
    maximal.setCustomerNumber("MAX-001");
    String maxName = "X".repeat(254) + "Z"; // Exakt 255 Zeichen
    maximal.setCompanyName(maxName);
    maximal.setCustomerType(CustomerType.UNTERNEHMEN);
    maximal.setIndustry(Industry.SONSTIGE);
    maximal.setClassification(Classification.C_KUNDE);
    maximal.setStatus(CustomerStatus.LEAD);
    maximal.setCreatedBy("test");
    customerRepository.persist(maximal);

    // Test 3: Special characters & umlauts
    Customer special = new Customer();
    special.setCustomerNumber("SPEC-001");
    special.setCompanyName("Café & Restaurant 'Zur Sonne' - Inh. Müller");
    special.setTradingName("Café 'L'Étoile'");
    special.setLegalForm("GmbH & Co. KG");
    special.setCustomerType(CustomerType.UNTERNEHMEN);
    special.setIndustry(Industry.RESTAURANT);
    special.setClassification(Classification.B_KUNDE);
    special.setStatus(CustomerStatus.AKTIV);
    special.setCreatedBy("test");
    customerRepository.persist(special);

    LOG.info("✅ String boundary tests created");
  }

  // ========================================================================
  // NUMERIC EDGE CASES
  // ========================================================================

  private void createNumericEdgeCases() {
    // Test 1: Zero values
    Customer zeroCustomer = new Customer();
    zeroCustomer.setCustomerNumber("ZERO-001");
    zeroCustomer.setCompanyName("Zero Values Test Company");
    zeroCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
    zeroCustomer.setIndustry(Industry.SONSTIGE);
    zeroCustomer.setClassification(Classification.C_KUNDE);
    zeroCustomer.setStatus(CustomerStatus.LEAD);
    zeroCustomer.setExpectedAnnualVolume(BigDecimal.ZERO);
    zeroCustomer.setActualAnnualVolume(BigDecimal.ZERO);
    zeroCustomer.setCreditLimit(BigDecimal.ZERO);
    zeroCustomer.setRiskScore(0);
    zeroCustomer.setCreatedBy("test");
    customerRepository.persist(zeroCustomer);

    // Test 2: Maximum numeric values
    Customer maxNumeric = new Customer();
    maxNumeric.setCustomerNumber("MAXNUM-001");
    maxNumeric.setCompanyName("Maximum Numbers Test Company");
    maxNumeric.setCustomerType(CustomerType.UNTERNEHMEN);
    maxNumeric.setIndustry(Industry.SONSTIGE);
    maxNumeric.setClassification(Classification.A_KUNDE);
    maxNumeric.setStatus(CustomerStatus.AKTIV);
    maxNumeric.setExpectedAnnualVolume(
        new BigDecimal("9999999999.99")); // Max 10 Stellen + 2 Nachkomma
    maxNumeric.setActualAnnualVolume(new BigDecimal("9999999999.99"));
    maxNumeric.setCreditLimit(new BigDecimal("9999999999.99"));
    maxNumeric.setRiskScore(100); // Maximum Risk Score
    maxNumeric.setCreatedBy("test");
    customerRepository.persist(maxNumeric);

    // Test 3: Precise decimal values
    Customer precise = new Customer();
    precise.setCustomerNumber("PREC-001");
    precise.setCompanyName("Decimal Precision Test Company");
    precise.setCustomerType(CustomerType.UNTERNEHMEN);
    precise.setIndustry(Industry.SONSTIGE);
    precise.setClassification(Classification.B_KUNDE);
    precise.setStatus(CustomerStatus.AKTIV);
    precise.setExpectedAnnualVolume(new BigDecimal("999.99"));
    precise.setActualAnnualVolume(new BigDecimal("1000.01"));
    precise.setCreditLimit(new BigDecimal("0.01")); // Minimal positive value
    precise.setRiskScore(50); // Medium risk
    precise.setCreatedBy("test");
    customerRepository.persist(precise);

    LOG.info("✅ Numeric edge cases created");
  }

  // ========================================================================
  // DATE/TIME EDGE CASES
  // ========================================================================

  private void createDateTimeEdgeCases() {
    LocalDateTime now = LocalDateTime.now();

    // Test 1: Past dates
    Customer pastCustomer = new Customer();
    pastCustomer.setCustomerNumber("PAST-001");
    pastCustomer.setCompanyName("Past Dates Test Company");
    pastCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
    pastCustomer.setIndustry(Industry.SONSTIGE);
    pastCustomer.setClassification(Classification.C_KUNDE);
    pastCustomer.setStatus(CustomerStatus.INAKTIV);
    pastCustomer.setLastContactDate(now.minusYears(2)); // 2 Jahre alt
    pastCustomer.setNextFollowUpDate(now.plusDays(1)); // Zukunft (required @Future)
    pastCustomer.setCreatedBy("test");
    customerRepository.persist(pastCustomer);

    // Test 2: Far future dates
    Customer futureCustomer = new Customer();
    futureCustomer.setCustomerNumber("FUTURE-001");
    futureCustomer.setCompanyName("Future Dates Test Company");
    futureCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
    futureCustomer.setIndustry(Industry.SONSTIGE);
    futureCustomer.setClassification(Classification.B_KUNDE);
    futureCustomer.setStatus(CustomerStatus.LEAD);
    futureCustomer.setLastContactDate(now.minusDays(1));
    futureCustomer.setNextFollowUpDate(now.plusYears(1)); // 1 Jahr in Zukunft
    futureCustomer.setCreatedBy("test");
    customerRepository.persist(futureCustomer);

    // Test 3: Leap year and timezone edge cases
    Customer leapYear = new Customer();
    leapYear.setCustomerNumber("LEAP-001");
    leapYear.setCompanyName("Leap Year Test Company");
    leapYear.setCustomerType(CustomerType.UNTERNEHMEN);
    leapYear.setIndustry(Industry.SONSTIGE);
    leapYear.setClassification(Classification.B_KUNDE);
    leapYear.setStatus(CustomerStatus.AKTIV);
    leapYear.setLastContactDate(LocalDateTime.of(2024, 2, 29, 12, 0)); // Schaltjahr
    leapYear.setNextFollowUpDate(LocalDateTime.of(2024, 12, 31, 23, 59)); // Jahresende
    leapYear.setCreatedBy("test");
    customerRepository.persist(leapYear);

    LOG.info("✅ Date/time edge cases created");
  }

  // ========================================================================
  // ENUM BOUNDARY TESTING
  // ========================================================================

  private void createEnumBoundaryTests() {
    int counter = 1;

    // Test alle CustomerType Werte
    for (CustomerType type : CustomerType.values()) {
      Customer customer = new Customer();
      customer.setCustomerNumber("TYPE-" + String.format("%03d", counter++));
      customer.setCompanyName("Test Company for " + type.name());
      customer.setCustomerType(type);
      customer.setIndustry(Industry.SONSTIGE);
      customer.setClassification(Classification.C_KUNDE);
      customer.setStatus(CustomerStatus.LEAD);
      customer.setCreatedBy("test");
      customerRepository.persist(customer);
    }

    // Test alle Industry Werte
    counter = 1;
    for (Industry industry : Industry.values()) {
      Customer customer = new Customer();
      customer.setCustomerNumber("IND-" + String.format("%03d", counter++));
      customer.setCompanyName("Test Company for " + industry.name());
      customer.setCustomerType(CustomerType.UNTERNEHMEN);
      customer.setIndustry(industry);
      customer.setClassification(Classification.B_KUNDE);
      customer.setStatus(CustomerStatus.AKTIV);
      customer.setCreatedBy("test");
      customerRepository.persist(customer);
    }

    // Test alle CustomerStatus Werte
    counter = 1;
    for (CustomerStatus status : CustomerStatus.values()) {
      Customer customer = new Customer();
      customer.setCustomerNumber("STAT-" + String.format("%03d", counter++));
      customer.setCompanyName("Test Company for " + status.name());
      customer.setCustomerType(CustomerType.UNTERNEHMEN);
      customer.setIndustry(Industry.SONSTIGE);
      customer.setClassification(Classification.B_KUNDE);
      customer.setStatus(status);
      customer.setCreatedBy("test");
      customerRepository.persist(customer);
    }

    LOG.info("✅ Enum boundary tests created");
  }

  // ========================================================================
  // BUSINESS LOGIC VARIATIONS
  // ========================================================================

  private void createBusinessLogicVariations() {
    // Test 1: Hierarchie-Ketten (Headquarter -> Branch -> Subsidiary)
    Customer headquarter = new Customer();
    headquarter.setCustomerNumber("HQ-001");
    headquarter.setCompanyName("Global Catering Headquarter");
    headquarter.setCustomerType(CustomerType.UNTERNEHMEN);
    headquarter.setIndustry(Industry.CATERING);
    headquarter.setClassification(Classification.A_KUNDE);
    headquarter.setHierarchyType(CustomerHierarchyType.HEADQUARTER);
    headquarter.setStatus(CustomerStatus.AKTIV);
    headquarter.setExpectedAnnualVolume(new BigDecimal("500000.00"));
    headquarter.setCreatedBy("test");
    customerRepository.persist(headquarter);

    Customer branch = new Customer();
    branch.setCustomerNumber("BR-001");
    branch.setCompanyName("Global Catering Branch Berlin");
    branch.setCustomerType(CustomerType.UNTERNEHMEN);
    branch.setIndustry(Industry.CATERING);
    branch.setClassification(Classification.A_KUNDE);
    branch.setHierarchyType(CustomerHierarchyType.FILIALE);
    branch.setStatus(CustomerStatus.AKTIV);
    branch.setExpectedAnnualVolume(new BigDecimal("150000.00"));
    branch.setCreatedBy("test");
    customerRepository.persist(branch);

    // Test 2: Risk Score Extreme Cases
    Customer highRisk = new Customer();
    highRisk.setCustomerNumber("RISK-HIGH");
    highRisk.setCompanyName("High Risk Customer Ltd.");
    highRisk.setCustomerType(CustomerType.UNTERNEHMEN);
    highRisk.setIndustry(Industry.SONSTIGE);
    highRisk.setClassification(Classification.C_KUNDE);
    highRisk.setStatus(CustomerStatus.RISIKO);
    highRisk.setRiskScore(95); // Sehr hohes Risiko
    highRisk.setCreditLimit(new BigDecimal("1000.00")); // Niedriges Limit bei hohem Risiko
    highRisk.setPaymentTerms(PaymentTerms.VORKASSE); // Sicherste Zahlung
    highRisk.setCreatedBy("test");
    customerRepository.persist(highRisk);

    Customer lowRisk = new Customer();
    lowRisk.setCustomerNumber("RISK-LOW");
    lowRisk.setCompanyName("Premium Low Risk Customer GmbH");
    lowRisk.setCustomerType(CustomerType.UNTERNEHMEN);
    lowRisk.setIndustry(Industry.HOTEL);
    lowRisk.setClassification(Classification.A_KUNDE);
    lowRisk.setStatus(CustomerStatus.AKTIV);
    lowRisk.setRiskScore(5); // Sehr niedriges Risiko
    lowRisk.setCreditLimit(new BigDecimal("100000.00")); // Hohes Limit bei niedrigem Risiko
    lowRisk.setPaymentTerms(PaymentTerms.NETTO_60); // Längere Zahlungsfrist
    lowRisk.setCreatedBy("test");
    customerRepository.persist(lowRisk);

    LOG.info("✅ Business logic variations created");
  }

  // ========================================================================
  // UNICODE & SPECIAL CHARACTER TESTS
  // ========================================================================

  private void createUnicodeTests() {
    // Test 1: Deutsche Umlaute und Sonderzeichen
    Customer german = new Customer();
    german.setCustomerNumber("UNI-DE");
    german.setCompanyName("Bäckerei Müßigmann GmbH & Co. KG");
    german.setTradingName("Bäckerei Müßigmann");
    german.setLegalForm("GmbH & Co. KG");
    german.setCustomerType(CustomerType.UNTERNEHMEN);
    german.setIndustry(Industry.EINZELHANDEL);
    german.setClassification(Classification.B_KUNDE);
    german.setStatus(CustomerStatus.AKTIV);
    german.setCreatedBy("test");
    customerRepository.persist(german);

    // Test 2: Französische Akzente
    Customer french = new Customer();
    french.setCustomerNumber("UNI-FR");
    french.setCompanyName("Café L'Étoile - Spécialités Françaises");
    french.setTradingName("Café L'Étoile");
    french.setCustomerType(CustomerType.UNTERNEHMEN);
    french.setIndustry(Industry.RESTAURANT);
    french.setClassification(Classification.B_KUNDE);
    french.setStatus(CustomerStatus.AKTIV);
    french.setCreatedBy("test");
    customerRepository.persist(french);

    // Test 3: Chinesische Zeichen (falls internationaler Support)
    Customer chinese = new Customer();
    chinese.setCustomerNumber("UNI-CN");
    chinese.setCompanyName("北京烤鸭餐厅"); // "Beijing Roast Duck Restaurant"
    chinese.setTradingName("Beijing Restaurant");
    chinese.setCustomerType(CustomerType.UNTERNEHMEN);
    chinese.setIndustry(Industry.RESTAURANT);
    chinese.setClassification(Classification.B_KUNDE);
    chinese.setStatus(CustomerStatus.AKTIV);
    chinese.setCreatedBy("test");
    customerRepository.persist(chinese);

    // Test 4: Emojis und spezielle Unicode
    Customer emoji = new Customer();
    emoji.setCustomerNumber("UNI-EMOJI");
    emoji.setCompanyName("🏨 Hotel Emoji & Wellness Resort");
    emoji.setTradingName("Hotel Emoji");
    emoji.setCustomerType(CustomerType.UNTERNEHMEN);
    emoji.setIndustry(Industry.HOTEL);
    emoji.setClassification(Classification.A_KUNDE);
    emoji.setStatus(CustomerStatus.AKTIV);
    emoji.setCreatedBy("test");
    customerRepository.persist(emoji);

    // Test 5: Bindestrich, Apostrophe und Anführungszeichen
    Customer punctuation = new Customer();
    punctuation.setCustomerNumber("UNI-PUNCT");
    punctuation.setCompanyName("O'Connor's Irish Pub - \"The Best\" Food & Drinks");
    punctuation.setTradingName("O'Connor's");
    punctuation.setCustomerType(CustomerType.UNTERNEHMEN);
    punctuation.setIndustry(Industry.RESTAURANT);
    punctuation.setClassification(Classification.B_KUNDE);
    punctuation.setStatus(CustomerStatus.AKTIV);
    punctuation.setCreatedBy("test");
    customerRepository.persist(punctuation);

    LOG.info("✅ Unicode and special character tests created");
  }
}
