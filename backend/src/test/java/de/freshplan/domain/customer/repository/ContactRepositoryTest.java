package de.freshplan.domain.customer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.entity.Customer;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
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
    customer.setUpdatedBy("test");
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
    CustomerContact contact = createTestContact(testCustomer, "Max", "Mustermann", true);
    contactRepository.persist(contact);
    contactRepository.flush();

    // When
    Optional<CustomerContact> found = contactRepository.findByIdOptional(contact.getId());

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getFirstName()).isEqualTo("Max");
    assertThat(found.get().getLastName()).isEqualTo("Mustermann");
    assertThat(found.get().getIsPrimary()).isTrue();
  }

  @Test
  @TestTransaction
  void shouldFindContactsByCustomerId() {
    // Given
    Customer testCustomer = createTestCustomer();
    CustomerContact contact1 = createTestContact(testCustomer, "Max", "Mustermann", true);
    CustomerContact contact2 = createTestContact(testCustomer, "Maria", "Musterfrau", false);
    contactRepository.persist(contact1);
    contactRepository.persist(contact2);
    contactRepository.flush();

    // When
    List<CustomerContact> contacts = contactRepository.findByCustomerId(testCustomer.getId());

    // Then
    assertThat(contacts).hasSize(2);
    assertThat(contacts.get(0).getIsPrimary()).isTrue(); // Primary first
    assertThat(contacts.get(0).getFirstName()).isEqualTo("Max");
    assertThat(contacts.get(1).getFirstName()).isEqualTo("Maria");
  }

  @Test
  @TestTransaction
  void shouldFindPrimaryContact() {
    // Given
    Customer testCustomer = createTestCustomer();
    CustomerContact primary = createTestContact(testCustomer, "Max", "Mustermann", true);
    CustomerContact secondary = createTestContact(testCustomer, "Maria", "Musterfrau", false);
    contactRepository.persist(primary);
    contactRepository.persist(secondary);
    contactRepository.flush();

    // When
    Optional<CustomerContact> found = contactRepository.findPrimaryByCustomerId(testCustomer.getId());

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(primary.getId());
    assertThat(found.get().getIsPrimary()).isTrue();
  }

  @Test
  @TestTransaction
  void shouldSetPrimaryContact() {
    // Given
    Customer testCustomer = createTestCustomer();
    CustomerContact contact1 = createTestContact(testCustomer, "Max", "Mustermann", true);
    CustomerContact contact2 = createTestContact(testCustomer, "Maria", "Musterfrau", false);
    contactRepository.persist(contact1);
    contactRepository.persist(contact2);
    contactRepository.flush();

    // When
    contactRepository.setPrimaryContact(testCustomer.getId(), contact2.getId());
    contactRepository.getEntityManager().flush(); // Force update
    contactRepository.getEntityManager().clear(); // Clear persistence context to force reload

    // Then
    CustomerContact updated1 = contactRepository.findById(contact1.getId());
    CustomerContact updated2 = contactRepository.findById(contact2.getId());
    assertThat(updated1.getIsPrimary()).isFalse();
    assertThat(updated2.getIsPrimary()).isTrue();
  }

  @Test
  @TestTransaction
  void shouldHandleSoftDelete() {
    // Given
    Customer testCustomer = createTestCustomer();
    CustomerContact contact = createTestContact(testCustomer, "Max", "Mustermann", true);
    contactRepository.persist(contact);
    contactRepository.flush();

    // When
    int deleted = contactRepository.softDelete(contact.getId());
    contactRepository.getEntityManager().flush(); // Force update
    contactRepository.getEntityManager().clear(); // Clear persistence context to force reload

    // Then
    assertThat(deleted).isEqualTo(1);
    CustomerContact found = contactRepository.findById(contact.getId());
    assertThat(found.getIsActive()).isFalse();

    // Should not find in active contacts
    List<CustomerContact> activeContacts = contactRepository.findByCustomerId(testCustomer.getId());
    assertThat(activeContacts).isEmpty();
  }

  @Test
  @TestTransaction
  void shouldFindContactsByEmail() {
    // Given
    Customer testCustomer = createTestCustomer();
    CustomerContact contact1 = createTestContact(testCustomer, "Max", "Mustermann", true);
    contact1.setEmail("max@example.com");
    CustomerContact contact2 = createTestContact(testCustomer, "Maria", "Musterfrau", false);
    contact2.setEmail("MAX@EXAMPLE.COM"); // Different case
    contactRepository.persist(contact1);
    contactRepository.persist(contact2);
    contactRepository.flush();

    // When
    List<CustomerContact> found = contactRepository.findByEmail("max@example.com");

    // Then
    assertThat(found).hasSize(2); // Case-insensitive search
  }

  @Test
  @TestTransaction
  void shouldCountActiveContacts() {
    // Given
    Customer testCustomer = createTestCustomer();
    CustomerContact active1 = createTestContact(testCustomer, "Max", "Mustermann", true);
    CustomerContact active2 = createTestContact(testCustomer, "Maria", "Musterfrau", false);
    CustomerContact inactive = createTestContact(testCustomer, "Hans", "Meier", false);
    inactive.setIsActive(false);

    contactRepository.persist(active1);
    contactRepository.persist(active2);
    contactRepository.persist(inactive);
    contactRepository.flush();

    // When
    long count = contactRepository.countActiveByCustomerId(testCustomer.getId());

    // Then
    assertThat(count).isEqualTo(2);
  }

  private CustomerContact createTestContact(
      Customer customer, String firstName, String lastName, boolean isPrimary) {
    CustomerContact contact = new CustomerContact();
    contact.setCustomer(customer);
    contact.setFirstName(firstName);
    contact.setLastName(lastName);
    contact.setIsPrimary(isPrimary);
    contact.setIsActive(true);
    contact.setCreatedBy("test");
    contact.setUpdatedBy("test");
    // Legacy fields for compatibility
    contact.setIsDecisionMaker(false);
    contact.setIsDeleted(false);
    return contact;
  }
}
