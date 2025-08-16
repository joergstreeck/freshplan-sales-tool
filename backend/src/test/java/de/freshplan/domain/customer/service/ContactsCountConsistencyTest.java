package de.freshplan.domain.customer.service;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.CustomerContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.CustomerResponse;
import de.freshplan.domain.customer.service.query.CustomerQueryService;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * Test zur Verifikation der ContactsCount-Konsistenz zwischen Legacy und CQRS Services. Stellt
 * sicher, dass beide Services denselben Count zurückgeben.
 */
@QuarkusTest
public class ContactsCountConsistencyTest {

  @Inject CustomerRepository customerRepository;

  @Inject CustomerContactRepository customerContactRepository;

  @Inject CustomerService customerService; // Legacy

  @Inject CustomerQueryService queryService; // CQRS

  @Test
  @TestTransaction
  void testContactsCountConsistencyForNewCustomer() {
    // 1. Neuen Kunden erstellen
    Customer customer = new Customer();
    customer.setCompanyName("Test ContactCount Company");
    customer.setCustomerNumber("TCT-" + (System.currentTimeMillis() % 10000000));
    customer.setStatus(de.freshplan.domain.customer.entity.CustomerStatus.AKTIV);
    customer.setIndustry(de.freshplan.domain.customer.entity.Industry.BILDUNG);
    customer.setCreatedBy("test");
    customer.setUpdatedBy("test");
    customerRepository.persist(customer);

    // 2. Kontakte hinzufügen
    for (int i = 0; i < 5; i++) {
      CustomerContact cc = new CustomerContact();
      cc.setCustomer(customer);
      cc.setFirstName("Contact");
      cc.setLastName("Number" + i);
      cc.setEmail("contact" + i + "@test.com");
      cc.setIsActive(true);
      cc.setIsDeleted(false);
      cc.setIsPrimary(i == 0); // Nur der erste ist Primary
      cc.setCreatedBy("test");
      cc.setUpdatedBy("test");
      customerContactRepository.persist(cc);

      customer.getContacts().add(cc);
    }

    // 3. Einige Kontakte als gelöscht markieren
    CustomerContact deletedContact = customer.getContacts().get(3);
    deletedContact.setIsDeleted(true);

    // 4. Einen Kontakt inaktiv machen
    CustomerContact inactiveContact = customer.getContacts().get(4);
    inactiveContact.setIsActive(false);

    customerRepository.persist(customer);
    customerRepository.flush();

    // 5. Erwartung: 3 aktive Kontakte (5 - 1 gelöscht - 1 inaktiv)
    int expectedActiveCount = 3;

    // 6. Entity-Methode testen
    int entityCount = customer.getActiveContactsCount();
    assertThat(entityCount)
        .as("Entity.getActiveContactsCount() sollte %d zurückgeben", expectedActiveCount)
        .isEqualTo(expectedActiveCount);

    // 7. Legacy Service testen
    CustomerResponse legacyResponse = customerService.getCustomer(customer.getId());
    assertThat(legacyResponse.contactsCount())
        .as("Legacy Service sollte %d aktive Kontakte zeigen", expectedActiveCount)
        .isEqualTo(expectedActiveCount);

    // 8. CQRS Service testen
    CustomerResponse cqrsResponse = queryService.getCustomer(customer.getId());
    assertThat(cqrsResponse.contactsCount())
        .as("CQRS Service sollte %d aktive Kontakte zeigen", expectedActiveCount)
        .isEqualTo(expectedActiveCount);

    // 9. Konsistenz prüfen
    assertThat(legacyResponse.contactsCount())
        .as("Legacy und CQRS sollten denselben Count zurückgeben")
        .isEqualTo(cqrsResponse.contactsCount());

    System.out.println("\n✅ ContactsCount Konsistenz-Test erfolgreich:");
    System.out.println("  - Entity Count: " + entityCount);
    System.out.println("  - Legacy Count: " + legacyResponse.contactsCount());
    System.out.println("  - CQRS Count: " + cqrsResponse.contactsCount());
  }

  @Test
  @TestTransaction
  void testContactsCountWithAllInactiveContacts() {
    // Customer mit nur inaktiven Kontakten
    Customer customer = new Customer();
    customer.setCompanyName("Test All Inactive Company");
    customer.setCustomerNumber("TCI-" + (System.currentTimeMillis() % 10000000));
    customer.setStatus(de.freshplan.domain.customer.entity.CustomerStatus.AKTIV);
    customer.setIndustry(de.freshplan.domain.customer.entity.Industry.BILDUNG);
    customer.setCreatedBy("test");
    customer.setUpdatedBy("test");
    customerRepository.persist(customer);

    // Nur inaktive Kontakte hinzufügen
    for (int i = 0; i < 3; i++) {
      CustomerContact cc = new CustomerContact();
      cc.setCustomer(customer);
      cc.setFirstName("Inactive");
      cc.setLastName("Contact" + i);
      cc.setEmail("inactive" + i + "@test.com");
      cc.setIsActive(false); // Alle inaktiv
      cc.setIsDeleted(false);
      cc.setIsPrimary(false);
      cc.setCreatedBy("test");
      cc.setUpdatedBy("test");
      customerContactRepository.persist(cc);

      customer.getContacts().add(cc);
    }

    customerRepository.flush();

    // Erwartung: 0 aktive Kontakte
    assertThat(customer.getActiveContactsCount())
        .as("Keine aktiven Kontakte erwartet")
        .isEqualTo(0);

    CustomerResponse legacyResponse = customerService.getCustomer(customer.getId());
    CustomerResponse cqrsResponse = queryService.getCustomer(customer.getId());

    assertThat(legacyResponse.contactsCount())
        .as("Legacy und CQRS sollten beide 0 zeigen")
        .isEqualTo(0)
        .isEqualTo(cqrsResponse.contactsCount());
  }

  @Test
  @TestTransaction
  void testContactsCountWithAllDeletedContacts() {
    // Customer mit nur gelöschten Kontakten
    Customer customer = new Customer();
    customer.setCompanyName("Test All Deleted Company");
    customer.setCustomerNumber("TCD-" + (System.currentTimeMillis() % 10000000));
    customer.setStatus(de.freshplan.domain.customer.entity.CustomerStatus.AKTIV);
    customer.setIndustry(de.freshplan.domain.customer.entity.Industry.BILDUNG);
    customer.setCreatedBy("test");
    customer.setUpdatedBy("test");
    customerRepository.persist(customer);

    // Nur gelöschte Kontakte hinzufügen
    for (int i = 0; i < 3; i++) {
      CustomerContact cc = new CustomerContact();
      cc.setCustomer(customer);
      cc.setFirstName("Deleted");
      cc.setLastName("Contact" + i);
      cc.setEmail("deleted" + i + "@test.com");
      cc.setIsActive(true);
      cc.setIsDeleted(true); // Alle gelöscht
      cc.setIsPrimary(false);
      cc.setCreatedBy("test");
      cc.setUpdatedBy("test");
      customerContactRepository.persist(cc);

      customer.getContacts().add(cc);
    }

    customerRepository.flush();

    // Erwartung: 0 aktive Kontakte
    assertThat(customer.getActiveContactsCount())
        .as("Keine aktiven Kontakte erwartet (alle gelöscht)")
        .isEqualTo(0);

    CustomerResponse legacyResponse = customerService.getCustomer(customer.getId());
    CustomerResponse cqrsResponse = queryService.getCustomer(customer.getId());

    assertThat(legacyResponse.contactsCount())
        .as("Legacy und CQRS sollten beide 0 zeigen")
        .isEqualTo(0)
        .isEqualTo(cqrsResponse.contactsCount());
  }

  @Test
  void testContactsCountForExistingCustomers() {
    // Teste alle existierenden Kunden auf Konsistenz
    int inconsistencies = 0;
    int totalTested = 0;

    System.out.println("\n=== Konsistenz-Check für existierende Kunden ===");

    for (Customer customer : customerRepository.findAll().list()) {
      if (customer.getContacts().size() > 0) {
        totalTested++;

        CustomerResponse legacyResponse = customerService.getCustomer(customer.getId());
        CustomerResponse cqrsResponse = queryService.getCustomer(customer.getId());

        if (!legacyResponse.contactsCount().equals(cqrsResponse.contactsCount())) {
          inconsistencies++;
          System.out.printf(
              "❌ Inkonsistenz bei %s (ID: %s): Legacy=%d, CQRS=%d%n",
              customer.getCompanyName(),
              customer.getId().toString().substring(0, 8),
              legacyResponse.contactsCount(),
              cqrsResponse.contactsCount());
        }
      }
    }

    System.out.printf(
        "\n📊 Ergebnis: %d von %d Kunden getestet, %d Inkonsistenzen gefunden%n",
        totalTested, customerRepository.count(), inconsistencies);

    assertThat(inconsistencies)
        .as("Es sollten keine Inkonsistenzen zwischen Legacy und CQRS bestehen")
        .isEqualTo(0);
  }
}
