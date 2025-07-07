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

  // Result classes
  public record SeedResult(int customersCreated, int eventsCreated) {}

  public record CleanupResult(long customersDeleted, long eventsDeleted) {}

  public record TestDataStats(long customerCount, long eventCount) {}
}
