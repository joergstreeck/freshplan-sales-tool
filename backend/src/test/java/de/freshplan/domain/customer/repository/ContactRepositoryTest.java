package de.freshplan.domain.customer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.customer.entity.Contact;
import de.freshplan.domain.customer.entity.Customer;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ContactRepository. Tests multi-contact support, primary contact handling, and
 * location-based queries.
 */
@QuarkusTest
class ContactRepositoryTest {

  @Inject ContactRepository contactRepository;

  @Inject CustomerRepository customerRepository;

  private Customer createTestCustomer() {
    // Create test customer with all required Sprint 2 fields
    Customer customer = new Customer();
    customer.setCustomerNumber("TEST-" + System.currentTimeMillis());
    customer.setCompanyName("Test Company GmbH");
    customer.setCreatedBy("test");
    // Sprint 2 required fields
    customer.setLocationsGermany(1);
    customer.setLocationsAustria(0);
    customer.setLocationsSwitzerland(0);
    customer.setLocationsRestEU(0);
    customer.setTotalLocationsEU(1);
    customer.setPainPoints(new java.util.ArrayList<>());
    customer.setPrimaryFinancing(de.freshplan.domain.customer.entity.FinancingType.PRIVATE);
    customerRepository.persist(customer);
    customerRepository.flush(); // Ensure customer is persisted before contacts
    return customer;
  }

  @Test
  @TestTransaction
  void shouldCreateAndFindContact() {
    // Given
    Customer testCustomer = createTestCustomer();
    Contact contact = createTestContact(testCustomer, "Max", "Mustermann", true);
    contactRepository.persist(contact);

    // When
    Optional<Contact> found = contactRepository.findByIdOptional(contact.getId());

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getFirstName()).isEqualTo("Max");
    assertThat(found.get().getLastName()).isEqualTo("Mustermann");
    assertThat(found.get().isPrimary()).isTrue();
  }

  @Test
  @TestTransaction
  void shouldFindContactsByCustomerId() {
    // Given
    Customer testCustomer = createTestCustomer();
    Contact contact1 = createTestContact(testCustomer, "Max", "Mustermann", true);
    Contact contact2 = createTestContact(testCustomer, "Maria", "Musterfrau", false);
    contactRepository.persist(contact1);
    contactRepository.persist(contact2);

    // When
    List<Contact> contacts = contactRepository.findByCustomerId(testCustomer.getId());

    // Then
    assertThat(contacts).hasSize(2);
    assertThat(contacts.get(0).isPrimary()).isTrue(); // Primary first
    assertThat(contacts.get(0).getFirstName()).isEqualTo("Max");
    assertThat(contacts.get(1).getFirstName()).isEqualTo("Maria");
  }

  @Test
  @TestTransaction
  void shouldFindPrimaryContact() {
    // Given
    Customer testCustomer = createTestCustomer();
    Contact primary = createTestContact(testCustomer, "Max", "Mustermann", true);
    Contact secondary = createTestContact(testCustomer, "Maria", "Musterfrau", false);
    contactRepository.persist(primary);
    contactRepository.persist(secondary);

    // When
    Optional<Contact> found = contactRepository.findPrimaryByCustomerId(testCustomer.getId());

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(primary.getId());
    assertThat(found.get().isPrimary()).isTrue();
  }

  @Test
  @TestTransaction
  void shouldSetPrimaryContact() {
    // Given
    Customer testCustomer = createTestCustomer();
    Contact contact1 = createTestContact(testCustomer, "Max", "Mustermann", true);
    Contact contact2 = createTestContact(testCustomer, "Maria", "Musterfrau", false);
    contactRepository.persist(contact1);
    contactRepository.persist(contact2);

    // When
    contactRepository.setPrimaryContact(testCustomer.getId(), contact2.getId());
    contactRepository.getEntityManager().flush(); // Force update
    contactRepository.getEntityManager().clear(); // Clear persistence context to force reload

    // Then
    Contact updated1 = contactRepository.findById(contact1.getId());
    Contact updated2 = contactRepository.findById(contact2.getId());
    assertThat(updated1.isPrimary()).isFalse();
    assertThat(updated2.isPrimary()).isTrue();
  }

  @Test
  @TestTransaction
  void shouldHandleSoftDelete() {
    // Given
    Customer testCustomer = createTestCustomer();
    Contact contact = createTestContact(testCustomer, "Max", "Mustermann", true);
    contactRepository.persist(contact);

    // When
    int deleted = contactRepository.softDelete(contact.getId());
    contactRepository.getEntityManager().flush(); // Force update
    contactRepository.getEntityManager().clear(); // Clear persistence context to force reload

    // Then
    assertThat(deleted).isEqualTo(1);
    Contact found = contactRepository.findById(contact.getId());
    assertThat(found.isActive()).isFalse();

    // Should not find in active contacts
    List<Contact> activeContacts = contactRepository.findByCustomerId(testCustomer.getId());
    assertThat(activeContacts).isEmpty();
  }

  @Test
  @TestTransaction
  void shouldFindContactsByEmail() {
    // Given
    Customer testCustomer = createTestCustomer();
    Contact contact1 = createTestContact(testCustomer, "Max", "Mustermann", true);
    contact1.setEmail("max@example.com");
    Contact contact2 = createTestContact(testCustomer, "Maria", "Musterfrau", false);
    contact2.setEmail("MAX@EXAMPLE.COM"); // Different case
    contactRepository.persist(contact1);
    contactRepository.persist(contact2);

    // When
    List<Contact> found = contactRepository.findByEmail("max@example.com");

    // Then
    assertThat(found).hasSize(2); // Case-insensitive search
  }

  @Test
  @TestTransaction
  void shouldCountActiveContacts() {
    // Given
    Customer testCustomer = createTestCustomer();
    Contact active1 = createTestContact(testCustomer, "Max", "Mustermann", true);
    Contact active2 = createTestContact(testCustomer, "Maria", "Musterfrau", false);
    Contact inactive = createTestContact(testCustomer, "Hans", "Meier", false);
    inactive.setActive(false);

    contactRepository.persist(active1);
    contactRepository.persist(active2);
    contactRepository.persist(inactive);

    // When
    long count = contactRepository.countActiveByCustomerId(testCustomer.getId());

    // Then
    assertThat(count).isEqualTo(2);
  }

  private Contact createTestContact(Customer customer, String firstName, String lastName, boolean isPrimary) {
    Contact contact = new Contact();
    contact.setCustomer(customer);
    contact.setFirstName(firstName);
    contact.setLastName(lastName);
    contact.setPrimary(isPrimary);
    contact.setActive(true);
    contact.setCreatedBy("test");
    contact.setUpdatedBy("test");
    // Legacy fields for compatibility
    contact.setIsDecisionMaker(false);
    contact.setIsDeleted(false);
    return contact;
  }
}
