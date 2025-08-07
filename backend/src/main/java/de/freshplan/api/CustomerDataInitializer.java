package de.freshplan.api;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.repository.CustomerTimelineRepository;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
  // TEMPORARILY DISABLED - Sprint-2 fields need to be initialized
  // TODO: Add locations_germany, locations_austria, etc. fields
  void onStartDisabled_TODO_FIX_SPRINT2_FIELDS_DISABLED(@Observes @Priority(500) StartupEvent ev) {
    LOG.info("🧪 Initializing comprehensive test data for ALL modules (Intelligence, Cockpit, Opportunities)...");

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
              "customer_timeline_events", 
              "customer_contacts", 
              "customer_locations", 
              "customers");

      // Derive allowed tables from the clearing list to ensure consistency
      var allowedTables = java.util.Set.copyOf(tablesToClear);

      // CRITICAL FIX: Delete opportunities first to avoid foreign key constraint violations
      LOG.info("Clearing all module data (opportunities, audit) before customers...");
      em.createNativeQuery("DELETE FROM audit_trail").executeUpdate();
      // TODO: contact_interactions und contact_warmth_scores Tabellen müssen erst angelegt werden
      // em.createNativeQuery("DELETE FROM contact_interactions").executeUpdate();
      // em.createNativeQuery("DELETE FROM contact_warmth_scores").executeUpdate();
      em.createNativeQuery("DELETE FROM opportunity_activities").executeUpdate();
      em.createNativeQuery("DELETE FROM opportunities").executeUpdate();

      for (String table : tablesToClear) {
        if (!allowedTables.contains(table)) {
          throw new IllegalArgumentException("Invalid table name for deletion: " + table);
        }
        em.createNativeQuery("DELETE FROM " + table).executeUpdate();
      }
      LOG.info("Existing data for all modules cleared via SQL");
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
    
    // 8. DATA INTELLIGENCE SCENARIOS
    LOG.info("Creating Data Intelligence test scenarios...");
    createDataIntelligenceScenarios();
    
    // 9. SALES COCKPIT SCENARIOS  
    LOG.info("Creating Sales Cockpit test scenarios...");
    createSalesCockpitScenarios();
    
    // 10. OPPORTUNITY PIPELINE SCENARIOS
    LOG.info("Creating Opportunity Pipeline test scenarios...");
    createOpportunityPipelineScenarios();

    long totalCustomers = customerRepository.count();
    LOG.info(
        "🎯 Comprehensive test data initialized successfully! Total customers: " + totalCustomers);
    LOG.info("💡 This covers all edge cases and modules for thorough testing");
    LOG.info("📊 Modules covered: Data Intelligence, Data Freshness, Cockpit, Opportunities");
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
    // Sprint 2 fields
    customer.setLocationsGermany(3);
    customer.setLocationsAustria(0);
    customer.setLocationsSwitzerland(0);
    customer.setLocationsRestEU(0);
    customer.setTotalLocationsEU(3);
    customer.setPainPoints(new ArrayList<>());
    customer.setPrimaryFinancing(FinancingType.PRIVATE);
    customer.setCreatedBy("system");

    setSprint2FieldDefaults(customer);
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
    // Sprint 2 fields
    customer.setLocationsGermany(1);
    customer.setLocationsAustria(0);
    customer.setLocationsSwitzerland(0);
    customer.setLocationsRestEU(0);
    customer.setTotalLocationsEU(1);
    customer.setPainPoints(new ArrayList<>());
    customer.setPrimaryFinancing(FinancingType.PRIVATE);
    customer.setCreatedBy("system");

    setSprint2FieldDefaults(customer);
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
    // Sprint 2 fields
    customer.setLocationsGermany(2);
    customer.setLocationsAustria(1);
    customer.setLocationsSwitzerland(0);
    customer.setLocationsRestEU(0);
    customer.setTotalLocationsEU(3);
    customer.setPainPoints(new ArrayList<>());
    customer.setPrimaryFinancing(FinancingType.PUBLIC);
    customer.setCreatedBy("system");

    setSprint2FieldDefaults(customer);
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
    // Sprint 2 fields
    customer.setLocationsGermany(1);
    customer.setLocationsAustria(0);
    customer.setLocationsSwitzerland(0);
    customer.setLocationsRestEU(0);
    customer.setTotalLocationsEU(1);
    customer.setPainPoints(new ArrayList<>());
    customer.setPrimaryFinancing(FinancingType.PRIVATE);
    customer.setCreatedBy("system");

    setSprint2FieldDefaults(customer);
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
    // Sprint 2 fields
    customer.setLocationsGermany(5);
    customer.setLocationsAustria(0);
    customer.setLocationsSwitzerland(0);
    customer.setLocationsRestEU(2);
    customer.setTotalLocationsEU(7);
    customer.setPainPoints(new ArrayList<>());
    customer.setPrimaryFinancing(FinancingType.PUBLIC);
    customer.setCreatedBy("system");

    setSprint2FieldDefaults(customer);
    customerRepository.persist(customer);
    LOG.info("Created event customer: " + customer.getCompanyName());
  }

  // Helper method to set Sprint 2 field defaults
  private void setSprint2FieldDefaults(Customer customer) {
    if (customer.getLocationsGermany() == null) {
      customer.setLocationsGermany(1);
    }
    if (customer.getLocationsAustria() == null) {
      customer.setLocationsAustria(0);
    }
    if (customer.getLocationsSwitzerland() == null) {
      customer.setLocationsSwitzerland(0);
    }
    if (customer.getLocationsRestEU() == null) {
      customer.setLocationsRestEU(0);
    }
    if (customer.getTotalLocationsEU() == null) {
      customer.setTotalLocationsEU(1);
    }
    if (customer.getPainPoints() == null) {
      customer.setPainPoints(new ArrayList<>());
    }
    if (customer.getPrimaryFinancing() == null) {
      customer.setPrimaryFinancing(FinancingType.PRIVATE);
    }
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
    // Sprint 2 fields
    minimal.setLocationsGermany(1);
    minimal.setLocationsAustria(0);
    minimal.setLocationsSwitzerland(0);
    minimal.setLocationsRestEU(0);
    minimal.setTotalLocationsEU(1);
    minimal.setPainPoints(new ArrayList<>());
    minimal.setPrimaryFinancing(FinancingType.PRIVATE);
    setSprint2FieldDefaults(minimal);
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
    // Sprint 2 fields
    maximal.setLocationsGermany(1);
    maximal.setLocationsAustria(0);
    maximal.setLocationsSwitzerland(0);
    maximal.setLocationsRestEU(0);
    maximal.setTotalLocationsEU(1);
    maximal.setPainPoints(new ArrayList<>());
    maximal.setPrimaryFinancing(FinancingType.PRIVATE);
    setSprint2FieldDefaults(maximal);
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
    setSprint2FieldDefaults(special);
    setSprint2FieldDefaults(special);
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
    setSprint2FieldDefaults(zeroCustomer);
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
    setSprint2FieldDefaults(maxNumeric);
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
    setSprint2FieldDefaults(precise);
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
    setSprint2FieldDefaults(pastCustomer);
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
    setSprint2FieldDefaults(futureCustomer);
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
    setSprint2FieldDefaults(leapYear);
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
      setSprint2FieldDefaults(customer);
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
      setSprint2FieldDefaults(customer);
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
      setSprint2FieldDefaults(customer);
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
    setSprint2FieldDefaults(headquarter);
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
    setSprint2FieldDefaults(branch);
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
    setSprint2FieldDefaults(highRisk);
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
    setSprint2FieldDefaults(lowRisk);
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
    setSprint2FieldDefaults(german);
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
    setSprint2FieldDefaults(french);
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
    setSprint2FieldDefaults(chinese);
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
    setSprint2FieldDefaults(emoji);
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
    setSprint2FieldDefaults(punctuation);
    customerRepository.persist(punctuation);

    LOG.info("✅ Unicode and special character tests created");
  }
  
  // 8. DATA INTELLIGENCE SCENARIOS
  private void createDataIntelligenceScenarios() {
    // Kunde mit vielen aktuellen Interaktionen (FRESH)
    Customer activeCustomer = new Customer();
    activeCustomer.setCustomerNumber("DI-FRESH-001");
    activeCustomer.setCompanyName("Fresh Contact GmbH - Sehr aktiver Kunde");
    activeCustomer.setTradingName("Fresh Contact");
    activeCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
    activeCustomer.setIndustry(Industry.HOTEL);
    activeCustomer.setClassification(Classification.A_KUNDE);
    activeCustomer.setStatus(CustomerStatus.AKTIV);
    activeCustomer.setLastContactDate(LocalDateTime.now().minusDays(1)); // Gestern kontaktiert
    activeCustomer.setNextFollowUpDate(LocalDateTime.now().plusDays(7));
    activeCustomer.setCreatedBy("data-intelligence");
    setSprint2FieldDefaults(activeCustomer);
    customerRepository.persist(activeCustomer);
    
    // Kunde mit alternden Daten (AGING - 90-180 Tage)
    Customer agingCustomer = new Customer();
    agingCustomer.setCustomerNumber("DI-AGING-002");
    agingCustomer.setCompanyName("Aging Data AG - Kontakt vor 4 Monaten");
    agingCustomer.setTradingName("Aging Data");
    agingCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
    agingCustomer.setIndustry(Industry.RESTAURANT);
    agingCustomer.setClassification(Classification.B_KUNDE);
    agingCustomer.setStatus(CustomerStatus.AKTIV);
    agingCustomer.setLastContactDate(LocalDateTime.now().minusDays(120)); // 4 Monate alt
    agingCustomer.setNextFollowUpDate(LocalDateTime.now().minusDays(90)); // Überfällig
    agingCustomer.setCreatedBy("data-intelligence");
    setSprint2FieldDefaults(agingCustomer);
    customerRepository.persist(agingCustomer);
    
    // Kunde mit veralteten Daten (STALE - 180-365 Tage)
    Customer staleCustomer = new Customer();
    staleCustomer.setCustomerNumber("DI-STALE-003");
    staleCustomer.setCompanyName("Stale Records Ltd - Kontakt vor 8 Monaten");
    staleCustomer.setTradingName("Stale Records");
    staleCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
    staleCustomer.setIndustry(Industry.CATERING);
    staleCustomer.setClassification(Classification.C_KUNDE);
    staleCustomer.setStatus(CustomerStatus.AKTIV);
    staleCustomer.setLastContactDate(LocalDateTime.now().minusDays(240)); // 8 Monate alt
    staleCustomer.setNextFollowUpDate(LocalDateTime.now().minusDays(200)); // Lange überfällig
    staleCustomer.setCreatedBy("data-intelligence");
    setSprint2FieldDefaults(staleCustomer);
    customerRepository.persist(staleCustomer);
    
    // Kunde mit kritisch alten Daten (CRITICAL - >365 Tage)
    Customer criticalCustomer = new Customer();
    criticalCustomer.setCustomerNumber("DI-CRITICAL-004");
    criticalCustomer.setCompanyName("Critical Alert Inc - Kontakt vor 2 Jahren");
    criticalCustomer.setTradingName("Critical Alert");
    criticalCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
    criticalCustomer.setIndustry(Industry.BILDUNG);
    criticalCustomer.setClassification(Classification.C_KUNDE);
    criticalCustomer.setStatus(CustomerStatus.INAKTIV);
    criticalCustomer.setLastContactDate(LocalDateTime.now().minusDays(730)); // 2 Jahre alt
    criticalCustomer.setNextFollowUpDate(LocalDateTime.now().minusDays(700)); // Extrem überfällig
    criticalCustomer.setCreatedBy("data-intelligence");
    setSprint2FieldDefaults(criticalCustomer);
    customerRepository.persist(criticalCustomer);
    
    // Kunde ohne jegliche Interaktionen
    Customer noInteractionCustomer = new Customer();
    noInteractionCustomer.setCustomerNumber("DI-NO-INT-005");
    noInteractionCustomer.setCompanyName("Never Contacted Corp - Noch nie kontaktiert");
    noInteractionCustomer.setTradingName("Never Contacted");
    noInteractionCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
    noInteractionCustomer.setIndustry(Industry.SONSTIGE);
    noInteractionCustomer.setClassification(Classification.D_KUNDE);
    noInteractionCustomer.setStatus(CustomerStatus.LEAD);
    noInteractionCustomer.setLastContactDate(null); // Nie kontaktiert
    noInteractionCustomer.setNextFollowUpDate(LocalDateTime.now().plusDays(1)); // Dringend kontaktieren
    noInteractionCustomer.setCreatedBy("data-intelligence");
    setSprint2FieldDefaults(noInteractionCustomer);
    customerRepository.persist(noInteractionCustomer);
    
    LOG.info("✅ Data Intelligence test scenarios created (5 customers with different freshness levels)");
  }
  
  // 9. SALES COCKPIT SCENARIOS
  private void createSalesCockpitScenarios() {
    // Top-Performer Kunde
    Customer topPerformer = new Customer();
    topPerformer.setCustomerNumber("SC-TOP-001");
    topPerformer.setCompanyName("Top Performer GmbH - Hoher Umsatz");
    topPerformer.setTradingName("Top Performer");
    topPerformer.setCustomerType(CustomerType.UNTERNEHMEN);
    topPerformer.setIndustry(Industry.HOTEL);
    topPerformer.setClassification(Classification.A_KUNDE);
    topPerformer.setStatus(CustomerStatus.AKTIV);
    topPerformer.setExpectedAnnualVolume(new BigDecimal("500000.00"));
    topPerformer.setActualAnnualVolume(new BigDecimal("550000.00")); // Übertrifft Erwartungen
    topPerformer.setPaymentTerms(PaymentTerms.NETTO_60);
    topPerformer.setCreditLimit(new BigDecimal("100000.00"));
    topPerformer.setLifecycleStage(CustomerLifecycleStage.GROWTH);
    topPerformer.setLastContactDate(LocalDateTime.now().minusDays(3));
    topPerformer.setCreatedBy("sales-cockpit");
    setSprint2FieldDefaults(topPerformer);
    customerRepository.persist(topPerformer);
    
    // At-Risk Kunde
    Customer atRisk = new Customer();
    atRisk.setCustomerNumber("SC-RISK-002");
    atRisk.setCompanyName("At Risk Ltd - Umsatz rückläufig");
    atRisk.setTradingName("At Risk");
    atRisk.setCustomerType(CustomerType.UNTERNEHMEN);
    atRisk.setIndustry(Industry.RESTAURANT);
    atRisk.setClassification(Classification.B_KUNDE);
    atRisk.setStatus(CustomerStatus.AKTIV);
    atRisk.setExpectedAnnualVolume(new BigDecimal("100000.00"));
    atRisk.setActualAnnualVolume(new BigDecimal("60000.00")); // Nur 60% erreicht
    atRisk.setPaymentTerms(PaymentTerms.NETTO_30);
    atRisk.setCreditLimit(new BigDecimal("20000.00"));
    atRisk.setLifecycleStage(CustomerLifecycleStage.RECOVERY);
    atRisk.setLastContactDate(LocalDateTime.now().minusDays(45)); // Lange nicht kontaktiert
    atRisk.setCreatedBy("sales-cockpit");
    setSprint2FieldDefaults(atRisk);
    customerRepository.persist(atRisk);
    
    // Churn Risk Kunde
    Customer churnRisk = new Customer();
    churnRisk.setCustomerNumber("SC-CHURN-003");
    churnRisk.setCompanyName("Churn Alert AG - Kurz vor Verlust");
    churnRisk.setTradingName("Churn Alert");
    churnRisk.setCustomerType(CustomerType.UNTERNEHMEN);
    churnRisk.setIndustry(Industry.CATERING);
    churnRisk.setClassification(Classification.C_KUNDE);
    churnRisk.setStatus(CustomerStatus.RISIKO);
    churnRisk.setExpectedAnnualVolume(new BigDecimal("50000.00"));
    churnRisk.setActualAnnualVolume(new BigDecimal("5000.00")); // Nur 10% erreicht
    churnRisk.setPaymentTerms(PaymentTerms.SOFORT);
    churnRisk.setCreditLimit(new BigDecimal("0.00")); // Kein Kredit mehr
    churnRisk.setLifecycleStage(CustomerLifecycleStage.RECOVERY);
    churnRisk.setLastContactDate(LocalDateTime.now().minusDays(180));
    churnRisk.setCreatedBy("sales-cockpit");
    setSprint2FieldDefaults(churnRisk);
    customerRepository.persist(churnRisk);
    
    // Neukunde mit Potenzial
    Customer newPotential = new Customer();
    newPotential.setCustomerNumber("SC-NEW-004");
    newPotential.setCompanyName("New Potential GmbH - Vielversprechender Neukunde");
    newPotential.setTradingName("New Potential");
    newPotential.setCustomerType(CustomerType.UNTERNEHMEN);
    newPotential.setIndustry(Industry.VERANSTALTUNG);
    newPotential.setClassification(Classification.A_KUNDE);
    newPotential.setStatus(CustomerStatus.PROSPECT);
    newPotential.setExpectedAnnualVolume(new BigDecimal("300000.00"));
    newPotential.setActualAnnualVolume(new BigDecimal("0.00")); // Noch kein Umsatz
    newPotential.setPaymentTerms(PaymentTerms.NETTO_14);
    newPotential.setCreditLimit(new BigDecimal("50000.00"));
    newPotential.setLifecycleStage(CustomerLifecycleStage.ONBOARDING);
    newPotential.setLastContactDate(LocalDateTime.now());
    newPotential.setNextFollowUpDate(LocalDateTime.now().plusDays(3));
    newPotential.setCreatedBy("sales-cockpit");
    setSprint2FieldDefaults(newPotential);
    customerRepository.persist(newPotential);
    
    LOG.info("✅ Sales Cockpit test scenarios created (4 customers with different performance levels)");
  }
  
  // 10. OPPORTUNITY PIPELINE SCENARIOS
  private void createOpportunityPipelineScenarios() {
    // Kunde mit Hot Lead
    Customer hotLead = new Customer();
    hotLead.setCustomerNumber("OP-HOT-001");
    hotLead.setCompanyName("Hot Opportunity AG - Kurz vor Abschluss");
    hotLead.setTradingName("Hot Opportunity");
    hotLead.setCustomerType(CustomerType.UNTERNEHMEN);
    hotLead.setIndustry(Industry.HOTEL);
    hotLead.setClassification(Classification.A_KUNDE);
    hotLead.setStatus(CustomerStatus.AKTIV);
    hotLead.setExpectedAnnualVolume(new BigDecimal("250000.00"));
    hotLead.setLifecycleStage(CustomerLifecycleStage.GROWTH);
    hotLead.setLastContactDate(LocalDateTime.now());
    hotLead.setNextFollowUpDate(LocalDateTime.now().plusDays(1)); // Morgen Abschluss
    hotLead.setCreatedBy("opportunity-pipeline");
    setSprint2FieldDefaults(hotLead);
    customerRepository.persist(hotLead);
    
    // Kunde in Verhandlung
    Customer negotiation = new Customer();
    negotiation.setCustomerNumber("OP-NEGO-002");
    negotiation.setCompanyName("Negotiation Inc - In Preisverhandlung");
    negotiation.setTradingName("Negotiation");
    negotiation.setCustomerType(CustomerType.UNTERNEHMEN);
    negotiation.setIndustry(Industry.CATERING);
    negotiation.setClassification(Classification.B_KUNDE);
    negotiation.setStatus(CustomerStatus.AKTIV);
    negotiation.setExpectedAnnualVolume(new BigDecimal("150000.00"));
    negotiation.setLifecycleStage(CustomerLifecycleStage.RETENTION);
    negotiation.setLastContactDate(LocalDateTime.now().minusDays(2));
    negotiation.setNextFollowUpDate(LocalDateTime.now().plusDays(5));
    negotiation.setCreatedBy("opportunity-pipeline");
    setSprint2FieldDefaults(negotiation);
    customerRepository.persist(negotiation);
    
    // Kunde mit verlorenem Deal
    Customer lostDeal = new Customer();
    lostDeal.setCustomerNumber("OP-LOST-003");
    lostDeal.setCompanyName("Lost Deal Ltd - An Konkurrenz verloren");
    lostDeal.setTradingName("Lost Deal");
    lostDeal.setCustomerType(CustomerType.UNTERNEHMEN);
    lostDeal.setIndustry(Industry.RESTAURANT);
    lostDeal.setClassification(Classification.D_KUNDE);
    lostDeal.setStatus(CustomerStatus.INAKTIV);
    lostDeal.setExpectedAnnualVolume(new BigDecimal("80000.00"));
    lostDeal.setActualAnnualVolume(new BigDecimal("0.00"));
    lostDeal.setLifecycleStage(CustomerLifecycleStage.RECOVERY);
    lostDeal.setLastContactDate(LocalDateTime.now().minusDays(30));
    lostDeal.setCreatedBy("opportunity-pipeline");
    setSprint2FieldDefaults(lostDeal);
    customerRepository.persist(lostDeal);
    
    // Kunde mit Renewal-Opportunity
    Customer renewal = new Customer();
    renewal.setCustomerNumber("OP-RENEWAL-004");
    renewal.setCompanyName("Renewal Corp - Vertragsverlängerung anstehend");
    renewal.setTradingName("Renewal");
    renewal.setCustomerType(CustomerType.UNTERNEHMEN);
    renewal.setIndustry(Industry.BILDUNG);
    renewal.setClassification(Classification.A_KUNDE);
    renewal.setStatus(CustomerStatus.AKTIV);
    renewal.setExpectedAnnualVolume(new BigDecimal("180000.00"));
    renewal.setActualAnnualVolume(new BigDecimal("175000.00"));
    renewal.setLifecycleStage(CustomerLifecycleStage.RETENTION);
    renewal.setLastContactDate(LocalDateTime.now().minusDays(7));
    renewal.setNextFollowUpDate(LocalDateTime.now().plusDays(14)); // Renewal in 2 Wochen
    renewal.setCreatedBy("opportunity-pipeline");
    setSprint2FieldDefaults(renewal);
    customerRepository.persist(renewal);
    
    // Kunde mit Upsell-Potenzial
    Customer upsell = new Customer();
    upsell.setCustomerNumber("OP-UPSELL-005");
    upsell.setCompanyName("Upsell Potential GmbH - Erweiterung möglich");
    upsell.setTradingName("Upsell Potential");
    upsell.setCustomerType(CustomerType.UNTERNEHMEN);
    upsell.setIndustry(Industry.VERANSTALTUNG);
    upsell.setClassification(Classification.B_KUNDE);
    upsell.setStatus(CustomerStatus.AKTIV);
    upsell.setExpectedAnnualVolume(new BigDecimal("120000.00"));
    upsell.setActualAnnualVolume(new BigDecimal("100000.00"));
    upsell.setLifecycleStage(CustomerLifecycleStage.GROWTH);
    upsell.setLastContactDate(LocalDateTime.now().minusDays(10));
    upsell.setNextFollowUpDate(LocalDateTime.now().plusDays(7));
    upsell.setCreatedBy("opportunity-pipeline");
    setSprint2FieldDefaults(upsell);
    customerRepository.persist(upsell);
    
    LOG.info("✅ Opportunity Pipeline test scenarios created (5 customers with different opportunity stages)");
  }
}
