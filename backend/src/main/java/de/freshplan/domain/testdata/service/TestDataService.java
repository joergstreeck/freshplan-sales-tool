package de.freshplan.domain.testdata.service;

import de.freshplan.domain.communication.entity.Activity;
import de.freshplan.domain.communication.service.ActivityService;
import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.repository.CustomerTimelineRepository;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.domain.testdata.service.command.TestDataCommandService;
import de.freshplan.domain.testdata.service.provider.CleanupResult;
import de.freshplan.domain.testdata.service.provider.SeedResult;
import de.freshplan.domain.testdata.service.provider.TestDataStats;
import de.freshplan.domain.testdata.service.query.TestDataQueryService;
import de.freshplan.modules.leads.domain.ActivityOutcome;
import de.freshplan.modules.leads.domain.ActivityType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
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
 *
 * <p><b>Test Data Coverage (Sprint 2.1.7.2):</b> This service provides test data coverage for the
 * following entities:
 *
 * <ul>
 *   <li><b>Customer</b> - 10 diverse test customers with realistic scenarios (already implemented)
 *   <li><b>Activity</b> - Unified communication activities for Leads and Customers (already
 *       implemented)
 *   <li><b>User</b> - Test users for activities and authentication (covered via TEST_USER constant)
 *   <li><b>CustomerLocation</b> - Customer delivery locations (coverage planned)
 *   <li><b>Opportunity</b> - Sales opportunities and pipeline tracking (coverage planned)
 *   <li><b>LeadContact</b> - Lead contact persons (coverage via modules/leads)
 *   <li><b>CustomerContact</b> - Customer contact persons (coverage planned)
 *   <li><b>ContactInteraction</b> - Interactions with contacts (coverage planned)
 *   <li><b>CustomerAddress</b> - Customer addresses (coverage planned)
 *   <li><b>LeadActivity</b> - Lead-specific activities (coverage via modules/leads)
 *   <li><b>OpportunityActivity</b> - Opportunity-related activities (coverage planned)
 *   <li><b>OpportunityMultiplier</b> - Opportunity probability multipliers (coverage planned)
 *   <li><b>Profile</b> - User profiles (coverage planned)
 *   <li><b>UserLeadSettings</b> - User-specific lead management settings (coverage via
 *       modules/leads)
 * </ul>
 *
 * <p>For detailed test data scenarios, see: docs/testing/TEST_DATA_SCENARIOS.md
 */
@SuppressWarnings("PMD.NcssCount") // TestData service with comprehensive scenario coverage
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

  @Inject OpportunityRepository opportunityRepository;

  @Inject ActivityService activityService;

  @Inject EntityManager entityManager;

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
    LOG.infof(
        "Cleaned up %d old customers and %d timeline events",
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

      Activity lead1Activity2 = Activity.forLead(10001L, TEST_USER, ActivityType.DEMO, null);
      lead1Activity2.summary = "Produktdemo bei Bio-Großhandel Meyer vor Ort";
      lead1Activity2.description =
          "Demo im Lager durchgeführt. Sehr positive Resonanz auf Frische und Qualität. Entscheidung für Konvertierung gefallen.";
      lead1Activity2.outcome = ActivityOutcome.SUCCESSFUL;
      lead1Activity2.activityDate = LocalDateTime.now().minusDays(185);
      activityService.createActivity(lead1Activity2);

      // 🎯 CUSTOMER-PHASE Activities für 90006
      Activity customer1Activity1 =
          Activity.forCustomer(convertedCustomer1.getId(), TEST_USER, ActivityType.MEETING, null);
      customer1Activity1.summary = "Vertragsverhandlung - 200k€ Jahresumsatz vereinbart";
      customer1Activity1.description =
          "Gold-Partner Status zugesichert. Lieferintervall 2x wöchentlich. Sehr zufrieden mit Service.";
      customer1Activity1.outcome = ActivityOutcome.SUCCESSFUL;
      customer1Activity1.activityDate = LocalDateTime.now().minusDays(7);
      activityService.createActivity(customer1Activity1);

      // 🎯 LEAD-PHASE Activities für 90007 (Ex-Lead 10002)
      Activity lead2Activity1 = Activity.forLead(10002L, TEST_USER, ActivityType.CALL, null);
      lead2Activity1.summary = "Anruf Strandhotel Ostsee - Saisonbetrieb";
      lead2Activity1.description =
          "Hotel nur April-Oktober geöffnet. Interesse an Bio-Produkten für Frühstücksbuffet.";
      lead2Activity1.outcome = ActivityOutcome.CALLBACK_REQUESTED;
      lead2Activity1.activityDate = LocalDateTime.now().minusDays(280);
      activityService.createActivity(lead2Activity1);

      Activity lead2Activity2 = Activity.forLead(10002L, TEST_USER, ActivityType.SAMPLE_SENT, null);
      lead2Activity2.summary = "Musterlieferung Gemüse + Obst versendet";
      lead2Activity2.description = "5kg Musterpaket an Küchenchef gesendet. Wartet auf Feedback.";
      lead2Activity2.outcome = ActivityOutcome.INFO_SENT;
      lead2Activity2.activityDate = LocalDateTime.now().minusDays(260);
      activityService.createActivity(lead2Activity2);

      // 🎯 CUSTOMER-PHASE Activities für 90007 (INAKTIV - lange kein Kontakt!)
      Activity customer2Activity1 =
          Activity.forCustomer(convertedCustomer2.getId(), TEST_USER, ActivityType.NOTE, null);
      customer2Activity1.summary = "Kunde meldet sich nicht mehr - saisonbedingt geschlossen";
      customer2Activity1.description =
          "Mehrere Kontaktversuche erfolglos. Hotel vermutlich in Winterpause. Status auf INAKTIV gesetzt.";
      customer2Activity1.activityDate = LocalDateTime.now().minusDays(65);
      activityService.createActivity(customer2Activity1);

      // 🎯 CUSTOMER-PHASE Activities für 90008 (KEIN Lead - direkt als Kunde)
      Activity customer3Activity1 =
          Activity.forCustomer(normalCustomer1.getId(), TEST_USER, ActivityType.EMAIL, null);
      customer3Activity1.summary = "Angebot Kantine Siemens - Wochenlieferung Bio-Salate";
      customer3Activity1.description =
          "Wöchentliche Lieferung 50kg Bio-Salate + Gemüse. Zahlung Netto 30.";
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
          Activity.forCustomer(convertedCustomer3.getId(), TEST_USER, ActivityType.FOLLOW_UP, null);
      customer4Activity1.summary = "Follow-up: Saisonstart April - Bestellung läuft";
      customer4Activity1.description =
          "Erste Lieferung erfolgt. Kunde sehr zufrieden. Silber-Partner Status.";
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

      LOG.infof(
          "✅ Sprint 2.1.7.2: Created %d enhanced test customers with Lead→Customer history", 5);

      // Persist all timeline events
      for (CustomerTimelineEvent event : createdEvents) {
        timelineRepository.persist(event);
      }

      LOG.infof(
          "Successfully seeded %d test customers with %d timeline events",
          createdCustomers.size(), createdEvents.size());

      // ========================================================================
      // Super-Customer Scenario (L3 + C1)
      // ========================================================================
      Customer superCustomer = seedSuperCustomerScenario();
      createdCustomers.add(superCustomer);
      LOG.infof("✅ Created Super-Customer scenario: Customer ID %s", superCustomer.getId());

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
    LOG.info("Starting test data cleanup (legacy mode)...");

    try {
      // STEP 1: Get all test customer IDs (entities WITHOUT isTestData must be deleted via parent
      // relationship)
      @SuppressWarnings("unchecked")
      List<Object> testCustomerIds =
          entityManager
              .createQuery("SELECT c.id FROM Customer c WHERE c.isTestData = true")
              .getResultList();

      LOG.debugf("Found %d test customers to clean", testCustomerIds.size());

      if (testCustomerIds.isEmpty()) {
        LOG.info("No test data to clean");
        return new CleanupResult(0, 0);
      }

      // STEP 2: Delete child entities WITHOUT isTestData (via parent relationship)

      // 2.1. Delete Activities (polymorphic pattern - entityType='CUSTOMER', entityId=customer.id)
      // Activity uses polymorphic association: entityType + entityId (TEXT)
      // Convert UUID objects to strings for comparison with entityId column
      List<String> customerIdStrings =
          testCustomerIds.stream()
              .map(Object::toString)
              .collect(java.util.stream.Collectors.toList());

      long deletedActivities =
          entityManager
              .createQuery(
                  "DELETE FROM Activity a WHERE a.entityType = 'CUSTOMER' AND a.entityId IN :customerIdStrings")
              .setParameter("customerIdStrings", customerIdStrings)
              .executeUpdate();
      LOG.debugf("Deleted %d activities", deletedActivities);

      // 2.2. Delete Opportunities (belongs to Customer - no isTestData field)
      long deletedOpportunities =
          entityManager
              .createQuery("DELETE FROM Opportunity o WHERE o.customer.id IN :customerIds")
              .setParameter("customerIds", testCustomerIds)
              .executeUpdate();
      LOG.debugf("Deleted %d opportunities", deletedOpportunities);

      // 2.3. Delete CustomerContacts (belongs to Customer - no isTestData field)
      long deletedContacts =
          entityManager
              .createQuery("DELETE FROM CustomerContact cc WHERE cc.customer.id IN :customerIds")
              .setParameter("customerIds", testCustomerIds)
              .executeUpdate();
      LOG.debugf("Deleted %d customer contacts", deletedContacts);

      // 2.4. Delete CustomerAddresses (belongs to CustomerLocation - no isTestData field)
      long deletedAddresses =
          entityManager
              .createQuery(
                  "DELETE FROM CustomerAddress ca WHERE ca.location.customer.id IN :customerIds")
              .setParameter("customerIds", testCustomerIds)
              .executeUpdate();
      LOG.debugf("Deleted %d customer addresses", deletedAddresses);

      // 2.5. Delete CustomerLocations (belongs to Customer - no isTestData field)
      long deletedLocations =
          entityManager
              .createQuery("DELETE FROM CustomerLocation cl WHERE cl.customer.id IN :customerIds")
              .setParameter("customerIds", testCustomerIds)
              .executeUpdate();
      LOG.debugf("Deleted %d customer locations", deletedLocations);

      // STEP 3: Delete entities WITH isTestData field

      // 3.1. Delete CustomerTimelineEvents (has isTestData = true)
      long deletedEvents = timelineRepository.delete("isTestData", true);
      LOG.debugf("Deleted %d timeline events", deletedEvents);

      // 3.2. Leads: Sprint 2.1.7.2 - Lead entity does NOT have isTestData field
      // Test Leads are identified by naming pattern (e.g., "L3 Großhandel - Super Customer")
      // Delete via company name pattern matching (% matches any characters)
      long deletedLeads =
          entityManager
              .createQuery(
                  "DELETE FROM Lead l WHERE l.companyName LIKE 'L% %' AND (l.companyName LIKE 'L_ %' OR l.companyName LIKE 'L__ %' OR l.companyName LIKE 'L___ %')")
              .executeUpdate();
      LOG.debugf("Deleted %d test leads (pattern: 'L# ...')", deletedLeads);

      // STEP 4: Delete Customers last (parent entity with isTestData = true)
      long deletedCustomers = customerRepository.delete("isTestData", true);
      LOG.debugf("Deleted %d customers", deletedCustomers);

      LOG.infof(
          "Successfully cleaned up: %d customers, %d locations, %d addresses, %d contacts, %d opportunities, %d activities, %d leads, %d timeline events",
          deletedCustomers,
          deletedLocations,
          deletedAddresses,
          deletedContacts,
          deletedOpportunities,
          deletedActivities,
          deletedLeads,
          deletedEvents);

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
  @SuppressWarnings("PMD.CognitiveComplexity") // TestData generation with diverse scenarios
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

  /**
   * Seeds L3 (CONVERTED Lead) and C1 (Super-Customer) with complete relationships.
   *
   * <p>Sprint 2.1.7.2 (Xentral Integration): Creates realistic test data for:
   *
   * <ul>
   *   <li>L3: CONVERTED Lead with 2 LeadContacts + 3 Activities
   *   <li>C1: Super-Customer (from L3) with all required fields
   *   <li>2 CustomerLocations (HQ + Branch) with CASCADE addresses
   *   <li>2 CustomerContacts (Primary + Secondary)
   *   <li>1 Opportunity (Q4 Großbestellung)
   *   <li>5 Activities (3 Lead-Phase + 2 Customer-Phase)
   * </ul>
   *
   * @return Created Customer C1 for logging purposes
   */
  @Transactional
  public Customer seedSuperCustomerScenario() {
    if (cqrsEnabled) {
      LOG.debugf("CQRS enabled - delegating seedSuperCustomerScenario to TestDataCommandService");
      // TODO: Implement in command service when CQRS is enabled
      throw new UnsupportedOperationException(
          "seedSuperCustomerScenario not yet implemented in CQRS mode");
    }

    LOG.info("Creating L3 (CONVERTED Lead) and C1 (Super-Customer) test scenario...");

    try {
      // ========================================================================
      // 1. CREATE L3 (CONVERTED LEAD)
      // ========================================================================
      de.freshplan.modules.leads.domain.Lead lead3 = new de.freshplan.modules.leads.domain.Lead();
      lead3.companyName = "Großhandel Frisch AG";
      lead3.companyNameNormalized = "grosshandel frisch ag";
      lead3.status = de.freshplan.modules.leads.domain.LeadStatus.CONVERTED;
      lead3.source = de.freshplan.domain.shared.LeadSource.MESSE; // TRADE_SHOW → MESSE
      lead3.businessType =
          de.freshplan.domain.shared.BusinessType.GROSSHANDEL; // RETAIL → GROSSHANDEL
      lead3.contactPerson = "Michael Frisch";
      lead3.email = "info@frisch-ag.de";
      lead3.emailNormalized = "info@frisch-ag.de";
      lead3.phone = "+49 30 12345678";
      lead3.phoneE164 = "+493012345678";
      lead3.street = "Industriestraße 10";
      lead3.postalCode = "12345";
      lead3.city = "Berlin";
      lead3.countryCode = "DE";
      lead3.kitchenSize = de.freshplan.domain.shared.KitchenSize.GROSS; // LARGE → GROSS
      lead3.employeeCount = 50;
      lead3.estimatedVolume = new BigDecimal("150000.00");
      lead3.branchCount = 2;
      lead3.isChain = false;
      lead3.ownerUserId = TEST_USER;
      lead3.createdBy = TEST_USER; // Required @NotNull field
      lead3.registeredAt = LocalDateTime.now().minusDays(200);
      // Note: Lead entity doesn't have convertedAt field - removed

      // Persist Lead using Panache (Lead extends PanacheEntityBase)
      lead3.persist();
      LOG.infof("✅ Created L3 Lead: id=%d, companyName=%s", lead3.id, lead3.companyName);

      // Create 2 LeadContacts for L3
      de.freshplan.modules.leads.domain.LeadContact leadContact1 =
          new de.freshplan.modules.leads.domain.LeadContact();
      leadContact1.setLead(lead3);
      leadContact1.setFirstName("Michael");
      leadContact1.setLastName("Frisch");
      leadContact1.setEmail("michael.frisch@frisch-ag.de");
      leadContact1.setPosition("Geschäftsführer");
      leadContact1.setPrimary(true);
      leadContact1.persist();

      de.freshplan.modules.leads.domain.LeadContact leadContact2 =
          new de.freshplan.modules.leads.domain.LeadContact();
      leadContact2.setLead(lead3);
      leadContact2.setFirstName("Julia");
      leadContact2.setLastName("Neumann");
      leadContact2.setEmail("julia.neumann@frisch-ag.de");
      leadContact2.setPosition("Einkaufsleiterin");
      leadContact2.setPrimary(false);
      leadContact2.persist();

      LOG.infof("✅ Created 2 LeadContacts for L3");

      // Create 3 Lead-Phase Activities for L3
      de.freshplan.domain.communication.entity.Activity leadActivity1 =
          de.freshplan.domain.communication.entity.Activity.forLead(
              lead3.id,
              TEST_USER,
              de.freshplan.modules.leads.domain.ActivityType.QUALIFIED_CALL,
              null);
      leadActivity1.summary = "Erstkontakt Großhandel Frisch AG - Interesse an Bio-Produkten";
      leadActivity1.description =
          "Geschäftsführer Michael Frisch sehr interessiert an Bio-Großhandel für Gastronomie. Plant Expansion.";
      leadActivity1.outcome = de.freshplan.modules.leads.domain.ActivityOutcome.QUALIFIED;
      leadActivity1.activityDate = LocalDateTime.now().minusDays(200);
      activityService.createActivity(leadActivity1);

      de.freshplan.domain.communication.entity.Activity leadActivity2 =
          de.freshplan.domain.communication.entity.Activity.forLead(
              lead3.id, TEST_USER, de.freshplan.modules.leads.domain.ActivityType.DEMO, null);
      leadActivity2.summary = "Produktdemo vor Ort - Frische & Qualität überzeugt";
      leadActivity2.description =
          "Demo im Lager durchgeführt. Sehr positive Resonanz. Entscheidung für Konvertierung gefallen.";
      leadActivity2.outcome = de.freshplan.modules.leads.domain.ActivityOutcome.SUCCESSFUL;
      leadActivity2.activityDate = LocalDateTime.now().minusDays(185);
      activityService.createActivity(leadActivity2);

      de.freshplan.domain.communication.entity.Activity leadActivity3 =
          de.freshplan.domain.communication.entity.Activity.forLead(
              lead3.id, TEST_USER, de.freshplan.modules.leads.domain.ActivityType.MEETING, null);
      leadActivity3.summary = "Vertragsverhandlung - Lead zu Kunde konvertiert";
      leadActivity3.description =
          "Jahresumsatz 150k€ vereinbart. Gold-Partner Status zugesichert. Lead erfolgreich konvertiert.";
      leadActivity3.outcome = de.freshplan.modules.leads.domain.ActivityOutcome.SUCCESSFUL;
      leadActivity3.activityDate = LocalDateTime.now().minusDays(180);
      activityService.createActivity(leadActivity3);

      LOG.infof("✅ Created 3 Lead-Phase Activities for L3");

      // ========================================================================
      // 2. CREATE C1 (SUPER-CUSTOMER FROM L3)
      // ========================================================================
      Customer customer =
          createTestCustomer(
              "90101",
              "[TEST] Großhandel Frisch AG - Super Customer",
              CustomerStatus.AKTIV,
              LocalDateTime.now().minusDays(7),
              Industry
                  .EINZELHANDEL); // Legacy Industry (deprecated) - maps to both LEH and GROSSHANDEL

      customer.setOriginalLeadId(lead3.id); // Link to L3
      customer.setBusinessType(
          de.freshplan.domain.shared.BusinessType
              .GROSSHANDEL); // Set correct BusinessType (overrides Industry mapping)
      customer.setPartnerStatus(PartnerStatus.GOLD_PARTNER);
      customer.setExpectedAnnualVolume(new BigDecimal("150000.00"));
      customer.setActualAnnualVolume(new BigDecimal("145000.00"));
      customer.setRiskScore(5);
      customer.setCreatedAt(LocalDateTime.now().minusDays(180));
      customer.setKitchenSize(de.freshplan.domain.shared.KitchenSize.GROSS); // LARGE → GROSS
      customer.setEmployeeCount(50);
      customer.setBranchCount(2);
      customer.setIsChain(false);
      customer.setEstimatedVolume(new BigDecimal("150000.00"));
      customer.setCreditLimit(new BigDecimal("20000.00"));
      customer.setClassification(Classification.A_KUNDE);

      customerRepository.persist(customer);
      LOG.infof("✅ Created C1 Customer: id=%s, customerNumber=%s", customer.getId(), "90101");

      // ========================================================================
      // 3. CREATE 2 CUSTOMER LOCATIONS (CASCADE ADDRESSES)
      // ========================================================================

      // Location 1: HQ Berlin
      CustomerLocation locationHQ = new CustomerLocation();
      locationHQ.setCustomer(customer);
      locationHQ.setLocationName("Hauptsitz Berlin");
      locationHQ.setLocationCode("HQ-BER-001");
      locationHQ.setCategory(LocationCategory.HEADQUARTERS);
      locationHQ.setIsMainLocation(true);
      locationHQ.setIsActive(true);
      locationHQ.setIsBillingLocation(true);
      locationHQ.setIsShippingLocation(true);
      locationHQ.setPhone("+49 30 12345678");
      locationHQ.setEmail("hq@frisch-ag.de");
      locationHQ.setCreatedBy(TEST_USER);

      // Address for HQ (CASCADE will persist this automatically)
      CustomerAddress addressHQ = new CustomerAddress();
      addressHQ.setLocation(locationHQ);
      addressHQ.setAddressType(AddressType.BILLING);
      addressHQ.setStreet("Industriestraße");
      addressHQ.setStreetNumber("10");
      addressHQ.setPostalCode("12345");
      addressHQ.setCity("Berlin");
      addressHQ.setCountry("DEU");
      addressHQ.setIsPrimaryForType(true);
      addressHQ.setIsActive(true);
      addressHQ.setCreatedBy(TEST_USER);

      locationHQ.addAddress(addressHQ);
      customer.addLocation(locationHQ);

      // Location 2: Branch Potsdam
      CustomerLocation locationBranch = new CustomerLocation();
      locationBranch.setCustomer(customer);
      locationBranch.setLocationName("Filiale Potsdam");
      locationBranch.setLocationCode("BR-POT-001");
      locationBranch.setCategory(LocationCategory.BRANCH_OFFICE);
      locationBranch.setIsMainLocation(false);
      locationBranch.setIsActive(true);
      locationBranch.setIsBillingLocation(false);
      locationBranch.setIsShippingLocation(true);
      locationBranch.setPhone("+49 331 9876543");
      locationBranch.setEmail("potsdam@frisch-ag.de");
      locationBranch.setCreatedBy(TEST_USER);

      // Address for Branch (CASCADE will persist this automatically)
      CustomerAddress addressBranch = new CustomerAddress();
      addressBranch.setLocation(locationBranch);
      addressBranch.setAddressType(AddressType.SHIPPING);
      addressBranch.setStreet("Potsdamer Straße");
      addressBranch.setStreetNumber("20");
      addressBranch.setPostalCode("14467");
      addressBranch.setCity("Potsdam");
      addressBranch.setCountry("DEU");
      addressBranch.setIsPrimaryForType(true);
      addressBranch.setIsActive(true);
      addressBranch.setCreatedBy(TEST_USER);

      locationBranch.addAddress(addressBranch);
      customer.addLocation(locationBranch);

      LOG.infof(
          "✅ Created 2 CustomerLocations with CASCADE addresses (HQ Berlin + Branch Potsdam)");

      // ========================================================================
      // 4. CREATE 2 CUSTOMER CONTACTS
      // ========================================================================
      CustomerContact contact1 = new CustomerContact();
      contact1.setCustomer(customer);
      contact1.setFirstName("Michael");
      contact1.setLastName("Frisch");
      contact1.setEmail("michael.frisch@frisch-ag.de");
      contact1.setPhone("+49 30 12345678");
      contact1.setPosition("Geschäftsführer");
      contact1.setDepartment("Management");
      contact1.setIsPrimary(true);
      contact1.setIsDecisionMaker(true);
      contact1.setIsActive(true);
      contact1.setAssignedLocation(locationHQ);
      contact1.setCreatedBy(TEST_USER);

      customer.addContact(contact1);

      CustomerContact contact2 = new CustomerContact();
      contact2.setCustomer(customer);
      contact2.setFirstName("Julia");
      contact2.setLastName("Neumann");
      contact2.setEmail("julia.neumann@frisch-ag.de");
      contact2.setPhone("+49 30 12345679");
      contact2.setPosition("Einkaufsleiterin");
      contact2.setDepartment("Einkauf");
      contact2.setIsPrimary(false);
      contact2.setIsDecisionMaker(false);
      contact2.setIsActive(true);
      contact2.setAssignedLocation(locationHQ);
      contact2.setCreatedBy(TEST_USER);

      customer.addContact(contact2);

      LOG.infof("✅ Created 2 CustomerContacts (Michael Frisch + Julia Neumann)");

      // ========================================================================
      // 5. CREATE 1 OPPORTUNITY FOR C1
      // ========================================================================
      de.freshplan.domain.opportunity.entity.Opportunity opportunity =
          new de.freshplan.domain.opportunity.entity.Opportunity();
      opportunity.setName("Q4 Großbestellung Backwaren");
      opportunity.setStage(de.freshplan.domain.opportunity.entity.OpportunityStage.QUALIFICATION);
      opportunity.setOpportunityType(
          de.freshplan.domain.opportunity.entity.OpportunityType.SORTIMENTSERWEITERUNG);
      opportunity.setCustomer(customer);
      opportunity.setExpectedValue(new BigDecimal("150000.00"));
      opportunity.setProbability(50);
      opportunity.setExpectedCloseDate(LocalDate.of(2024, 12, 31));
      opportunity.setDescription(
          "Großbestellung Backwaren für Q4 2024. Erweiterung des Sortiments um frische Backwaren.");

      // Persist opportunity using injected repository
      opportunityRepository.persist(opportunity);

      LOG.infof("✅ Created 1 Opportunity: name=%s, value=%s", opportunity.getName(), "150000.00");

      // ========================================================================
      // 6. CREATE 2 CUSTOMER-PHASE ACTIVITIES
      // ========================================================================
      de.freshplan.domain.communication.entity.Activity customerActivity1 =
          de.freshplan.domain.communication.entity.Activity.forCustomer(
              customer.getId(),
              TEST_USER,
              de.freshplan.modules.leads.domain.ActivityType.MEETING,
              null);
      customerActivity1.summary = "Vertragsverhandlung - 150k€ Jahresumsatz vereinbart";
      customerActivity1.description =
          "Gold-Partner Status zugesichert. Lieferintervall 2x wöchentlich. Sehr zufrieden mit Service.";
      customerActivity1.outcome = de.freshplan.modules.leads.domain.ActivityOutcome.SUCCESSFUL;
      customerActivity1.activityDate = LocalDateTime.now().minusDays(7);
      activityService.createActivity(customerActivity1);

      de.freshplan.domain.communication.entity.Activity customerActivity2 =
          de.freshplan.domain.communication.entity.Activity.forCustomer(
              customer.getId(),
              TEST_USER,
              de.freshplan.modules.leads.domain.ActivityType.FOLLOW_UP,
              null);
      customerActivity2.summary = "Follow-up: Kundenzufriedenheit & Sortimentserweiterung";
      customerActivity2.description =
          "Kunde sehr zufrieden mit Qualität und Service. Interesse an Sortimentserweiterung (Backwaren) für Q4.";
      customerActivity2.outcome = de.freshplan.modules.leads.domain.ActivityOutcome.SUCCESSFUL;
      customerActivity2.activityDate = LocalDateTime.now().minusDays(3);
      activityService.createActivity(customerActivity2);

      LOG.infof("✅ Created 2 Customer-Phase Activities");

      // ========================================================================
      // 7. FINAL PERSIST
      // ========================================================================
      // Customer has CASCADE, so locations, contacts, and addresses will be persisted automatically
      customerRepository.persist(customer);

      LOG.infof(
          "✅ Sprint 2.1.7.2: Successfully created L3→C1 Super-Customer Scenario "
              + "(Lead id=%d → Customer id=%s, 2 Locations, 2 Contacts, 1 Opportunity, 5 Activities)",
          lead3.id, customer.getId());

      return customer;

    } catch (Exception e) {
      LOG.error("Error seeding Super-Customer scenario", e);
      throw new RuntimeException("Failed to seed Super-Customer scenario: " + e.getMessage(), e);
    }
  }
}
