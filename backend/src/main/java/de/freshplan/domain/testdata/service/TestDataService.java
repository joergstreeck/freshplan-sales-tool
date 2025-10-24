package de.freshplan.domain.testdata.service;

import de.freshplan.domain.communication.entity.Activity;
import de.freshplan.domain.communication.entity.EntityType;
import de.freshplan.domain.communication.service.ActivityService;
import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.repository.CustomerTimelineRepository;
import de.freshplan.domain.testdata.service.command.TestDataCommandService;
import de.freshplan.domain.testdata.service.query.TestDataQueryService;
import de.freshplan.modules.leads.domain.ActivityOutcome;
import de.freshplan.modules.leads.domain.ActivityType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Service for managing test data in the development environment. Provides clean seeding and removal
 * of test data.
 *
 * <p>CQRS Refactoring: This service now acts as a facade that delegates to Command and Query
 * services based on a feature flag. When cqrs.enabled=true, it uses the new split services. When
 * false, it falls back to the legacy implementation.
 */
@ApplicationScoped
public class TestDataService {

  private static final Logger LOG = Logger.getLogger(TestDataService.class);
  private static final String TEST_USER = "test-data-seeder";

  @ConfigProperty(name = "features.cqrs.enabled", defaultValue = "false")
  boolean cqrsEnabled;

  @Inject TestDataCommandService commandService;

  @Inject TestDataQueryService queryService;

  @Inject CustomerRepository customerRepository;

  @Inject CustomerTimelineRepository timelineRepository;

  @Inject ActivityService activityService;

  /**
   * Seeds the database with 10 diverse test customers (90001-90010).
   *
   * <p>Sprint 2.1.7.2 (D8): Enhanced with converted customers (Lead→Customer) and unified activity
   * timeline.
   *
   * <p>All test data is marked with is_test_data = true and prefixed with [TEST].
   */
  @Transactional
  public SeedResult seedTestData() {
    if (cqrsEnabled) {
      LOG.debugf("CQRS enabled - delegating seedTestData to TestDataCommandService");
      return commandService.seedTestData();
    }

    // Legacy implementation
    LOG.info("Starting test data seeding...");

    // AUTO-CLEANUP: Remove old test data before seeding new data
    LOG.info("Cleaning up old test data before seeding...");
    CleanupResult cleanup = cleanTestData();
    LOG.infof("Cleaned up %d old customers and %d timeline events",
              cleanup.customersDeleted(), cleanup.eventsDeleted());

    List<Customer> createdCustomers = new ArrayList<>();
    List<CustomerTimelineEvent> createdEvents = new ArrayList<>();

    try {
      // 1. Risiko-Kunde (90+ Tage ohne Kontakt) - HIGH Risk
      Customer riskCustomer =
          createTestCustomer(
              "90001",
              "[TEST] Bäckerei Schmidt - 90 Tage ohne Kontakt",
              CustomerStatus.AKTIV, // Aktiv aber mit hohem Risiko
              LocalDateTime.now().minusDays(92),
              Industry.EINZELHANDEL // Bäckerei passt zu Einzelhandel
              );
      riskCustomer.setRiskScore(85);
      riskCustomer.setExpectedAnnualVolume(new BigDecimal("65000.00")); // Mittlerer Umsatz
      riskCustomer.setCreatedAt(LocalDateTime.now().minusDays(365)); // Alt-Kunde
      customerRepository.persist(riskCustomer);
      createdCustomers.add(riskCustomer);

      // Add timeline events
      createdEvents.add(
          createTimelineEvent(
              riskCustomer,
              EventCategory.NOTE,
              "Letzter Kontakt vor 3 Monaten - dringend anrufen!",
              LocalDateTime.now().minusDays(92)));

      // 2. Aktiver Partner-Kunde - Top-Kunde mit hohem Umsatz
      Customer activeCustomer =
          createTestCustomer(
              "90002",
              "[TEST] Hotel Sonnenschein - Top-Kunde 150k€",
              CustomerStatus.AKTIV,
              LocalDateTime.now().minusDays(5),
              Industry.HOTEL);
      activeCustomer.setPartnerStatus(PartnerStatus.GOLD_PARTNER);
      activeCustomer.setExpectedAnnualVolume(new BigDecimal("150000.00")); // Top-Kunde > 100k
      activeCustomer.setRiskScore(10);
      activeCustomer.setCreatedAt(LocalDateTime.now().minusDays(200));
      customerRepository.persist(activeCustomer);
      createdCustomers.add(activeCustomer);

      createdEvents.add(
          createTimelineEvent(
              activeCustomer,
              EventCategory.QUOTE,
              "Neues Angebot für Frühstücksbuffet versendet",
              LocalDateTime.now().minusDays(5)));

      // 3. Neuer Kunde (erstellt vor 15 Tagen)
      Customer leadCustomer =
          createTestCustomer(
              "90003",
              "[TEST] Restaurant Mustermeier - Neuer Kunde (15 Tage)",
              CustomerStatus.AKTIV,
              LocalDateTime.now().minusDays(3),
              Industry.RESTAURANT);
      leadCustomer.setRiskScore(20);
      leadCustomer.setExpectedAnnualVolume(new BigDecimal("45000.00")); // Kleinerer Umsatz
      leadCustomer.setCreatedAt(LocalDateTime.now().minusDays(15)); // Neuer Kunde < 30 Tage
      customerRepository.persist(leadCustomer);
      createdCustomers.add(leadCustomer);

      // 4. Medium Risk Kunde (45 Tage ohne Kontakt)
      Customer inactiveCustomer =
          createTestCustomer(
              "90004",
              "[TEST] Catering Service - Medium Risk (45 Tage)",
              CustomerStatus.AKTIV,
              LocalDateTime.now().minusDays(45),
              Industry.CATERING);
      inactiveCustomer.setRiskScore(60); // Medium Risk
      inactiveCustomer.setExpectedAnnualVolume(new BigDecimal("75000.00")); // Mittlerer Umsatz
      inactiveCustomer.setCreatedAt(LocalDateTime.now().minusDays(400));
      customerRepository.persist(inactiveCustomer);
      createdCustomers.add(inactiveCustomer);

      createdEvents.add(
          createTimelineEvent(
              inactiveCustomer,
              EventCategory.PHONE_CALL,
              "Nachfass-Termin vereinbart",
              LocalDateTime.now().minusDays(45)));

      // 5. Weiterer aktiver Kunde für "Aktive Kunden" Filter
      Customer prospectCustomer =
          createTestCustomer(
              "90005",
              "[TEST] Kantine TechHub - Aktiver Kunde",
              CustomerStatus.AKTIV,
              LocalDateTime.now().minusDays(2),
              Industry.KANTINE // Kantine passt zu Großverbraucher
              );
      prospectCustomer.setNextFollowUpDate(LocalDateTime.now().plusDays(5));
      prospectCustomer.setRiskScore(15);
      prospectCustomer.setExpectedAnnualVolume(new BigDecimal("85000.00")); // Guter Umsatz
      prospectCustomer.setCreatedAt(LocalDateTime.now().minusDays(180));
      customerRepository.persist(prospectCustomer);
      createdCustomers.add(prospectCustomer);

      createdEvents.add(
          createTimelineEvent(
              prospectCustomer,
              EventCategory.EMAIL,
              "Interesse an Bio-Produkten bekundet",
              LocalDateTime.now().minusDays(15)));

      // ========================================================================
      // Sprint 2.1.7.2 (D8): NEUE TEST-KUNDEN MIT MEHR TIEFE
      // ========================================================================
      // 90006-90010: Konvertierte Kunden + Activities für Unified Timeline
      // ========================================================================

      // 6. Konvertierter Kunde (Lead→Customer), AKTIV, ganzjährig, Top-Kunde
      Customer convertedCustomer1 =
          createTestCustomer(
              "90006",
              "[TEST] Bio-Großhandel Meyer - Konvertiert (Ex-Lead 10001)",
              CustomerStatus.AKTIV,
              LocalDateTime.now().minusDays(7),
              Industry.EINZELHANDEL); // Großhandel = Einzelhandel
      convertedCustomer1.setOriginalLeadId(10001L); // 🎯 KONVERTIERT VON LEAD!
      convertedCustomer1.setPartnerStatus(PartnerStatus.GOLD_PARTNER);
      convertedCustomer1.setExpectedAnnualVolume(new BigDecimal("200000.00")); // Top-Kunde
      convertedCustomer1.setRiskScore(5);
      convertedCustomer1.setCreatedAt(LocalDateTime.now().minusDays(180)); // Kunde seit 6 Monaten
      customerRepository.persist(convertedCustomer1);
      createdCustomers.add(convertedCustomer1);

      // 7. Konvertierter Kunde (Lead→Customer), INAKTIV, saisonal
      Customer convertedCustomer2 =
          createTestCustomer(
              "90007",
              "[TEST] Strandhotel Ostsee - Konvertiert, INAKTIV (Ex-Lead 10002)",
              CustomerStatus.INAKTIV, // INAKTIV!
              LocalDateTime.now().minusDays(65),
              Industry.HOTEL);
      convertedCustomer2.setOriginalLeadId(10002L); // 🎯 KONVERTIERT VON LEAD!
      convertedCustomer2.setPartnerStatus(PartnerStatus.KEIN_PARTNER);
      convertedCustomer2.setExpectedAnnualVolume(new BigDecimal("35000.00")); // Kleinerer Umsatz
      convertedCustomer2.setRiskScore(75); // High Risk
      convertedCustomer2.setCreatedAt(LocalDateTime.now().minusDays(250));
      customerRepository.persist(convertedCustomer2);
      createdCustomers.add(convertedCustomer2);

      // 8. Normaler Kunde (KEIN Lead), AKTIV, ganzjährig
      Customer normalCustomer1 =
          createTestCustomer(
              "90008",
              "[TEST] Kantine Siemens Campus - Ganzjährig aktiv",
              CustomerStatus.AKTIV,
              LocalDateTime.now().minusDays(3),
              Industry.KANTINE);
      // KEIN originalLeadId → direkt als Kunde angelegt!
      normalCustomer1.setPartnerStatus(PartnerStatus.SILBER_PARTNER);
      normalCustomer1.setExpectedAnnualVolume(new BigDecimal("95000.00"));
      normalCustomer1.setRiskScore(12);
      normalCustomer1.setCreatedAt(LocalDateTime.now().minusDays(120));
      customerRepository.persist(normalCustomer1);
      createdCustomers.add(normalCustomer1);

      // 9. Konvertierter Kunde (Lead→Customer), AKTIV, saisonal
      Customer convertedCustomer3 =
          createTestCustomer(
              "90009",
              "[TEST] Biergarten Alpensee - Konvertiert, saisonal (Ex-Lead 10003)",
              CustomerStatus.AKTIV,
              LocalDateTime.now().minusDays(10),
              Industry.RESTAURANT);
      convertedCustomer3.setOriginalLeadId(10003L); // 🎯 KONVERTIERT VON LEAD!
      convertedCustomer3.setPartnerStatus(PartnerStatus.SILBER_PARTNER);
      convertedCustomer3.setExpectedAnnualVolume(new BigDecimal("55000.00"));
      convertedCustomer3.setRiskScore(25);
      convertedCustomer3.setCreatedAt(LocalDateTime.now().minusDays(90));
      customerRepository.persist(convertedCustomer3);
      createdCustomers.add(convertedCustomer3);

      // 10. Normaler Kunde (KEIN Lead), INAKTIV, ganzjährig
      Customer normalCustomer2 =
          createTestCustomer(
              "90010",
              "[TEST] Catering Deluxe - INAKTIV ohne Lead-Historie",
              CustomerStatus.INAKTIV, // INAKTIV!
              LocalDateTime.now().minusDays(80),
              Industry.CATERING);
      // KEIN originalLeadId → direkt als Kunde angelegt!
      normalCustomer2.setPartnerStatus(PartnerStatus.KEIN_PARTNER);
      normalCustomer2.setExpectedAnnualVolume(new BigDecimal("42000.00"));
      normalCustomer2.setRiskScore(70);
      normalCustomer2.setCreatedAt(LocalDateTime.now().minusDays(300));
      customerRepository.persist(normalCustomer2);
      createdCustomers.add(normalCustomer2);

      // ========================================================================
      // Sprint 2.1.7.2 (D8): ACTIVITIES für Unified Timeline
      // ========================================================================
      // Lead-Phase Activities (für konvertierte Kunden 90006, 90007, 90009)
      // Customer-Phase Activities (für ALLE neuen Kunden 90006-90010)
      // ========================================================================

      // 🎯 LEAD-PHASE Activities für 90006 (Ex-Lead 10001)
      Activity lead1Activity1 =
          Activity.forLead(10001L, TEST_USER, ActivityType.QUALIFIED_CALL, null);
      lead1Activity1.summary = "Erstkontakt Bio-Großhandel Meyer - Interesse an regionalem Gemüse";
      lead1Activity1.description =
          "Geschäftsführer Herr Meyer sehr interessiert an Bio-Gemüse aus der Region. Plant Expansion im Bereich Gastronomie-Belieferung.";
      lead1Activity1.outcome = ActivityOutcome.QUALIFIED;
      lead1Activity1.activityDate = LocalDateTime.now().minusDays(200);
      activityService.createActivity(lead1Activity1);

      Activity lead1Activity2 =
          Activity.forLead(10001L, TEST_USER, ActivityType.DEMO, null);
      lead1Activity2.summary = "Produktdemo bei Bio-Großhandel Meyer vor Ort";
      lead1Activity2.description =
          "Demo im Lager durchgeführt. Sehr positive Resonanz auf Frische und Qualität. Entscheidung für Konvertierung gefallen.";
      lead1Activity2.outcome = ActivityOutcome.SUCCESSFUL;
      lead1Activity2.activityDate = LocalDateTime.now().minusDays(185);
      activityService.createActivity(lead1Activity2);

      // 🎯 CUSTOMER-PHASE Activities für 90006
      Activity customer1Activity1 =
          Activity.forCustomer(
              convertedCustomer1.getId(), TEST_USER, ActivityType.MEETING, null);
      customer1Activity1.summary = "Vertragsverhandlung - 200k€ Jahresumsatz vereinbart";
      customer1Activity1.description =
          "Gold-Partner Status zugesichert. Lieferintervall 2x wöchentlich. Sehr zufrieden mit Service.";
      customer1Activity1.outcome = ActivityOutcome.SUCCESSFUL;
      customer1Activity1.activityDate = LocalDateTime.now().minusDays(7);
      activityService.createActivity(customer1Activity1);

      // 🎯 LEAD-PHASE Activities für 90007 (Ex-Lead 10002)
      Activity lead2Activity1 =
          Activity.forLead(10002L, TEST_USER, ActivityType.CALL, null);
      lead2Activity1.summary = "Anruf Strandhotel Ostsee - Saisonbetrieb";
      lead2Activity1.description =
          "Hotel nur April-Oktober geöffnet. Interesse an Bio-Produkten für Frühstücksbuffet.";
      lead2Activity1.outcome = ActivityOutcome.CALLBACK_REQUESTED;
      lead2Activity1.activityDate = LocalDateTime.now().minusDays(280);
      activityService.createActivity(lead2Activity1);

      Activity lead2Activity2 =
          Activity.forLead(10002L, TEST_USER, ActivityType.SAMPLE_SENT, null);
      lead2Activity2.summary = "Musterlieferung Gemüse + Obst versendet";
      lead2Activity2.description = "5kg Musterpaket an Küchenchef gesendet. Wartet auf Feedback.";
      lead2Activity2.outcome = ActivityOutcome.INFO_SENT;
      lead2Activity2.activityDate = LocalDateTime.now().minusDays(260);
      activityService.createActivity(lead2Activity2);

      // 🎯 CUSTOMER-PHASE Activities für 90007 (INAKTIV - lange kein Kontakt!)
      Activity customer2Activity1 =
          Activity.forCustomer(
              convertedCustomer2.getId(), TEST_USER, ActivityType.NOTE, null);
      customer2Activity1.summary = "Kunde meldet sich nicht mehr - saisonbedingt geschlossen";
      customer2Activity1.description =
          "Mehrere Kontaktversuche erfolglos. Hotel vermutlich in Winterpause. Status auf INAKTIV gesetzt.";
      customer2Activity1.activityDate = LocalDateTime.now().minusDays(65);
      activityService.createActivity(customer2Activity1);

      // 🎯 CUSTOMER-PHASE Activities für 90008 (KEIN Lead - direkt als Kunde)
      Activity customer3Activity1 =
          Activity.forCustomer(normalCustomer1.getId(), TEST_USER, ActivityType.EMAIL, null);
      customer3Activity1.summary = "Angebot Kantine Siemens - Wochenlieferung Bio-Salate";
      customer3Activity1.description = "Wöchentliche Lieferung 50kg Bio-Salate + Gemüse. Zahlung Netto 30.";
      customer3Activity1.outcome = ActivityOutcome.SUCCESSFUL;
      customer3Activity1.activityDate = LocalDateTime.now().minusDays(3);
      activityService.createActivity(customer3Activity1);

      // 🎯 LEAD-PHASE Activities für 90009 (Ex-Lead 10003)
      Activity lead3Activity1 =
          Activity.forLead(10003L, TEST_USER, ActivityType.QUALIFIED_CALL, null);
      lead3Activity1.summary = "Qualifizierter Anruf Biergarten Alpensee";
      lead3Activity1.description =
          "Biergarten sucht regionalen Lieferanten für Sommersaison. Interesse an Kartoffeln, Salaten, Kräutern.";
      lead3Activity1.outcome = ActivityOutcome.QUALIFIED;
      lead3Activity1.activityDate = LocalDateTime.now().minusDays(100);
      activityService.createActivity(lead3Activity1);

      Activity lead3Activity2 =
          Activity.forLead(10003L, TEST_USER, ActivityType.ROI_PRESENTATION, null);
      lead3Activity2.summary = "ROI-Präsentation: Bio spart Kosten durch weniger Food Waste";
      lead3Activity2.description =
          "Längere Haltbarkeit Bio-Produkte nachgewiesen. Einsparung ca. 15% durch weniger Verderb. Kunde überzeugt.";
      lead3Activity2.outcome = ActivityOutcome.SUCCESSFUL;
      lead3Activity2.activityDate = LocalDateTime.now().minusDays(92);
      activityService.createActivity(lead3Activity2);

      // 🎯 CUSTOMER-PHASE Activities für 90009
      Activity customer4Activity1 =
          Activity.forCustomer(
              convertedCustomer3.getId(), TEST_USER, ActivityType.FOLLOW_UP, null);
      customer4Activity1.summary = "Follow-up: Saisonstart April - Bestellung läuft";
      customer4Activity1.description = "Erste Lieferung erfolgt. Kunde sehr zufrieden. Silber-Partner Status.";
      customer4Activity1.outcome = ActivityOutcome.SUCCESSFUL;
      customer4Activity1.activityDate = LocalDateTime.now().minusDays(10);
      activityService.createActivity(customer4Activity1);

      // 🎯 CUSTOMER-PHASE Activities für 90010 (KEIN Lead - direkt als Kunde, INAKTIV)
      Activity customer5Activity1 =
          Activity.forCustomer(normalCustomer2.getId(), TEST_USER, ActivityType.CALL, null);
      customer5Activity1.summary = "Anruf Catering Deluxe - keine Reaktion";
      customer5Activity1.description =
          "Mehrere Versuche, Kunden zu erreichen. Mailbox voll. Vermutlich Geschäft aufgegeben.";
      customer5Activity1.outcome = ActivityOutcome.NO_ANSWER;
      customer5Activity1.activityDate = LocalDateTime.now().minusDays(80);
      activityService.createActivity(customer5Activity1);

      LOG.infof("✅ Sprint 2.1.7.2: Created %d enhanced test customers with Lead→Customer history", 5);

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
    if (cqrsEnabled) {
      LOG.debugf("CQRS enabled - delegating cleanTestData to TestDataCommandService");
      return commandService.cleanTestData();
    }

    // Legacy implementation
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
    if (cqrsEnabled) {
      LOG.debugf("CQRS enabled - delegating getTestDataStats to TestDataQueryService");
      return queryService.getTestDataStats();
    }

    // Legacy implementation
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
    if (cqrsEnabled) {
      LOG.debugf("CQRS enabled - delegating cleanOldTestData to TestDataCommandService");
      return commandService.cleanOldTestData();
    }

    // Legacy implementation
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
    if (cqrsEnabled) {
      LOG.debugf("CQRS enabled - delegating seedAdditionalTestData to TestDataCommandService");
      return commandService.seedAdditionalTestData();
    }

    // Legacy implementation
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
    if (cqrsEnabled) {
      LOG.debugf("CQRS enabled - delegating seedComprehensiveTestData to TestDataCommandService");
      return commandService.seedComprehensiveTestData();
    }

    // Legacy implementation
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
