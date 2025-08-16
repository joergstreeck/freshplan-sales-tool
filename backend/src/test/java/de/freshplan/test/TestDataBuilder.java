package de.freshplan.test;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.opportunity.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for creating unique test data to avoid constraint violations.
 * 
 * <p>Each method generates unique values to prevent duplicate key violations
 * and other constraint issues in parallel test execution.
 */
public class TestDataBuilder {

  private static final AtomicInteger customerCounter = new AtomicInteger(1);
  private static final AtomicInteger opportunityCounter = new AtomicInteger(1);

  /**
   * Creates a unique customer number for testing.
   * Format: KD-TEST-TIMESTAMP-XXXX to ensure absolute uniqueness
   */
  public static String uniqueCustomerNumber() {
    return String.format("KD-TEST-%d-%04d", 
        System.currentTimeMillis() % 100000, 
        customerCounter.getAndIncrement());
  }

  /**
   * Creates a test customer with all required fields set.
   * Uses unique customer number to avoid duplicate key violations.
   */
  public static Customer createTestCustomer(String companyName) {
    Customer customer = new Customer();
    customer.setCustomerNumber(uniqueCustomerNumber());
    customer.setCompanyName(companyName);
    customer.setStatus(CustomerStatus.AKTIV);
    customer.setCustomerType(CustomerType.UNTERNEHMEN);
    customer.setIndustry(Industry.HOTEL);
    customer.setCreatedAt(LocalDateTime.now());
    customer.setCreatedBy("test");
    customer.setUpdatedBy("test");
    customer.setUpdatedAt(LocalDateTime.now());
    return customer;
  }

  /**
   * Creates a test opportunity with all required fields set.
   * Ensures positive expected value to avoid check constraint violations.
   */
  public static Opportunity createTestOpportunity(String name, Customer customer) {
    Opportunity opportunity = new Opportunity();
    opportunity.setName(name);
    opportunity.setCustomer(customer);
    opportunity.setExpectedValue(new BigDecimal("10000.00")); // Always positive
    opportunity.setStage(OpportunityStage.NEW_LEAD);
    opportunity.setProbability(20);
    return opportunity;
  }

  /**
   * Creates a test contact with all required fields set.
   */
  public static CustomerContact createTestContact(Customer customer, String firstName, String lastName) {
    CustomerContact contact = new CustomerContact();
    contact.setCustomer(customer);
    contact.setFirstName(firstName);
    contact.setLastName(lastName);
    contact.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@test.de");
    contact.setPhone("+49 123 " + System.currentTimeMillis() % 1000000);
    contact.setPosition("Test Position");
    contact.setIsPrimary(false);
    contact.setIsActive(true);
    contact.setIsDeleted(false);
    contact.setCreatedAt(LocalDateTime.now());
    contact.setCreatedBy("test");
    contact.setUpdatedAt(LocalDateTime.now());
    contact.setUpdatedBy("test");
    return contact;
  }

  /**
   * Creates a test timeline event with all required fields set.
   */
  public static CustomerTimelineEvent createTestTimelineEvent(Customer customer, String eventType) {
    CustomerTimelineEvent event = new CustomerTimelineEvent();
    event.setCustomer(customer);
    event.setEventType(eventType);
    event.setTitle("Test Event " + System.currentTimeMillis());
    event.setDescription("Test event description");
    event.setCategory(EventCategory.NOTE);
    event.setImportance(ImportanceLevel.MEDIUM);
    event.setPerformedBy("test");
    event.setEventDate(LocalDateTime.now());
    event.setCreatedAt(LocalDateTime.now());
    return event;
  }

  /**
   * Reset counters for isolated test execution.
   * Call this in @BeforeAll or @BeforeEach if needed.
   */
  public static void resetCounters() {
    customerCounter.set(1);
    opportunityCounter.set(1);
  }
}