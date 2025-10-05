package de.freshplan.domain.customer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.test.builders.ContactTestDataFactory;
import de.freshplan.test.builders.CustomerTestDataFactory;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for ContactRepository - ONLY for custom queries and business logic.
 *
 * <p>Tests custom repository methods and business logic beyond basic Panache operations.
 */
@QuarkusTest
@Tag("integration")
class ContactRepositoryIT {

  @Inject ContactRepository contactRepository;

  @Inject CustomerRepository customerRepository;


  private Customer createTestCustomer() {
    Customer customer =
        customerBuilder
            .withCompanyName("Test Company IT GmbH")
            .withLocationsGermany(1)
            .withLocationsAustria(0)
            .withLocationsSwitzerland(0)
            .persist();
    return customer;
  }

  @Test
  @TestTransaction
  void findByCustomerId_shouldReturnSortedByPrimary() {
    Customer testCustomer = createTestCustomer();
    CustomerContact contact1 = createTestContact(testCustomer, "Max", "Mustermann", true);
    CustomerContact contact2 = createTestContact(testCustomer, "Maria", "Musterfrau", false);
    contactRepository.persist(contact1);
    contactRepository.persist(contact2);
    contactRepository.flush();

    List<CustomerContact> contacts = contactRepository.findByCustomerId(testCustomer.getId());

    assertThat(contacts).hasSize(2);
    assertThat(contacts.get(0).getIsPrimary()).isTrue(); // Primary first
    assertThat(contacts.get(0).getFirstName()).isEqualTo("Max");
    assertThat(contacts.get(1).getFirstName()).isEqualTo("Maria");
  }

  @Test
  @TestTransaction
  void findPrimaryByCustomerId_shouldReturnPrimaryContact() {
    Customer testCustomer = createTestCustomer();
    CustomerContact primary = createTestContact(testCustomer, "Max", "Mustermann", true);
    CustomerContact secondary = createTestContact(testCustomer, "Maria", "Musterfrau", false);
    contactRepository.persist(primary);
    contactRepository.persist(secondary);
    contactRepository.flush();

    Optional<CustomerContact> found =
        contactRepository.findPrimaryByCustomerId(testCustomer.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(primary.getId());
    assertThat(found.get().getIsPrimary()).isTrue();
  }

  @Test
  @TestTransaction
  void setPrimaryContact_shouldUpdatePrimaryStatus() {
    Customer testCustomer = createTestCustomer();
    CustomerContact contact1 = createTestContact(testCustomer, "Max", "Mustermann", true);
    CustomerContact contact2 = createTestContact(testCustomer, "Maria", "Musterfrau", false);
    contactRepository.persist(contact1);
    contactRepository.persist(contact2);
    contactRepository.flush();

    contactRepository.setPrimaryContact(testCustomer.getId(), contact2.getId());
    contactRepository.getEntityManager().flush();
    contactRepository.getEntityManager().clear();

    CustomerContact updated1 = contactRepository.findById(contact1.getId());
    CustomerContact updated2 = contactRepository.findById(contact2.getId());
    assertThat(updated1.getIsPrimary()).isFalse();
    assertThat(updated2.getIsPrimary()).isTrue();
  }

  @Test
  @TestTransaction
  void softDelete_shouldDeactivateContact() {
    Customer testCustomer = createTestCustomer();
    CustomerContact contact = createTestContact(testCustomer, "Max", "Mustermann", true);
    contactRepository.persist(contact);
    contactRepository.flush();

    int deleted = contactRepository.softDelete(contact.getId());
    contactRepository.getEntityManager().flush();
    contactRepository.getEntityManager().clear();

    assertThat(deleted).isEqualTo(1);
    CustomerContact found = contactRepository.findById(contact.getId());
    assertThat(found.getIsActive()).isFalse();

    List<CustomerContact> activeContacts = contactRepository.findByCustomerId(testCustomer.getId());
    assertThat(activeContacts).isEmpty();
  }

  @Test
  @TestTransaction
  void findByEmail_shouldBeCaseInsensitive() {
    Customer testCustomer = createTestCustomer();
    CustomerContact contact1 = createTestContact(testCustomer, "Max", "Mustermann", true);
    contact1.setEmail("max@example.com");
    CustomerContact contact2 = createTestContact(testCustomer, "Maria", "Musterfrau", false);
    contact2.setEmail("MAX@EXAMPLE.COM");
    contactRepository.persist(contact1);
    contactRepository.persist(contact2);
    contactRepository.flush();

    List<CustomerContact> found = contactRepository.findByEmail("max@example.com");

    assertThat(found).hasSize(2);
  }

  @Test
  @TestTransaction
  void countActiveByCustomerId_shouldExcludeInactive() {
    Customer testCustomer = createTestCustomer();
    CustomerContact active1 = createTestContact(testCustomer, "Max", "Mustermann", true);
    CustomerContact active2 = createTestContact(testCustomer, "Maria", "Musterfrau", false);
    CustomerContact inactive = createTestContact(testCustomer, "Hans", "Meier", false);
    inactive.setIsActive(false);

    contactRepository.persist(active1);
    contactRepository.persist(active2);
    contactRepository.persist(inactive);
    contactRepository.flush();

    long count = contactRepository.countActiveByCustomerId(testCustomer.getId());

    assertThat(count).isEqualTo(2);
  }

  private CustomerContact createTestContact(
      Customer customer, String firstName, String lastName, boolean isPrimary) {
    var builder =
        ContactTestDataFactory.builder()
            .forCustomer(customer)
            .withFirstName(firstName)
            .withLastName(lastName);

    if (isPrimary) {
      builder.asPrimary();
    }

    CustomerContact contact = builder.build();
    contact.setIsActive(true);
    contact.setCreatedBy("test");
    contact.setUpdatedBy("test");
    contact.setIsDecisionMaker(false);
    contact.setIsDeleted(false);
    return contact;
  }
}
