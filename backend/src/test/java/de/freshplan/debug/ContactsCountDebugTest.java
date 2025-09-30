package de.freshplan.debug;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.repository.CustomerContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.CustomerService;
import de.freshplan.domain.customer.service.dto.CustomerResponse;
import de.freshplan.domain.customer.service.query.CustomerQueryService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Debug-Test für ContactsCount-Bug. Legacy zeigt 12, CQRS zeigt 15 Kontakte für denselben Kunden.
 */
@QuarkusTest
@Tag("quarantine")
@TestTransaction  // Sprint 2.1.4: Fix ContextNotActiveException
public class ContactsCountDebugTest {

  @Inject CustomerRepository customerRepository;

  @Inject CustomerContactRepository customerContactRepository;

  @Inject ContactRepository contactRepository;

  @Inject CustomerService customerService; // Legacy

  @Inject CustomerQueryService queryService; // CQRS

  private static final UUID BIOFRESH_CUSTOMER_ID =
      UUID.fromString("39ca3e6d-17dc-426c-bd8e-b5e1dc75d8fc");

  @Test
  @Transactional
  void debugContactsCountDiscrepancy() {
    // Direkter DB-Zugriff
    Customer customer = customerRepository.findById(BIOFRESH_CUSTOMER_ID);
    if (customer == null) {
      System.out.println("❌ Customer nicht gefunden: " + BIOFRESH_CUSTOMER_ID);
      return;
    }

    System.out.println("\n=== ContactsCount Debug für: " + customer.getCompanyName() + " ===");

    // 1. Direkte Entity-Abfrage
    List<CustomerContact> allContacts = customer.getContacts();
    System.out.println("1. Alle Kontakte (Entity.getContacts()): " + allContacts.size());

    // 2. Gefilterte Kontakte via Entity-Methode
    List<CustomerContact> activeContacts = customer.getActiveContacts();
    System.out.println("2. Aktive Kontakte (Entity.getActiveContacts()): " + activeContacts.size());

    int activeCount = customer.getActiveContactsCount();
    System.out.println("3. Active Count (Entity.getActiveContactsCount()): " + activeCount);

    // 3. Details der Kontakte
    System.out.println("\n=== Kontakt-Details ===");
    for (CustomerContact cc : allContacts) {
      System.out.printf(
          "  - ID: %s, Active: %s, Deleted: %s, Primary: %s%n",
          cc.getId().toString().substring(0, 8),
          cc.getIsActive(),
          cc.getIsDeleted(),
          cc.getIsPrimary());
    }

    // 4. Legacy Service
    try {
      CustomerResponse legacyResponse = customerService.getCustomer(BIOFRESH_CUSTOMER_ID);
      System.out.println("\n4. Legacy Service ContactsCount: " + legacyResponse.contactsCount());
    } catch (Exception e) {
      System.out.println("Legacy Service Error: " + e.getMessage());
    }

    // 5. CQRS Query Service
    try {
      CustomerResponse cqrsResponse = queryService.getCustomer(BIOFRESH_CUSTOMER_ID);
      System.out.println("5. CQRS Service ContactsCount: " + cqrsResponse.contactsCount());
    } catch (Exception e) {
      System.out.println("CQRS Service Error: " + e.getMessage());
    }

    // 6. Direkte DB-Query
    long dbCount =
        customerContactRepository
            .find(
                "customer.id = ?1 AND isDeleted = false AND isActive = true", BIOFRESH_CUSTOMER_ID)
            .count();
    System.out.println("\n6. Direkte DB-Query Count: " + dbCount);

    // 7. Prüfe auf Lazy Loading Issues
    System.out.println("\n=== Lazy Loading Check ===");
    System.out.println("Contacts Collection Initialized: " + customer.getContacts().size());

    // Check for duplicates
    long uniqueIds = customer.getContacts().stream().map(CustomerContact::getId).distinct().count();
    System.out.println("Unique Contact IDs: " + uniqueIds);
  }

  @Test
  void findCustomersWithManyContacts() {
    System.out.println("\n=== Kunden mit vielen Kontakten ===");

    List<Customer> customers = customerRepository.findAll().list();
    for (Customer c : customers) {
      int contactCount = c.getActiveContactsCount();
      if (contactCount > 10) {
        System.out.printf(
            "Customer: %s (ID: %s) - Contacts: %d%n",
            c.getCompanyName(), c.getId().toString().substring(0, 8), contactCount);
      }
    }
  }
}
