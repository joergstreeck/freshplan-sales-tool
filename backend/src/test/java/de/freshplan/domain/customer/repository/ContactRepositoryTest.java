package de.freshplan.domain.customer.repository;

import de.freshplan.domain.customer.entity.Contact;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerLocation;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ContactRepository.
 * Tests multi-contact support, primary contact handling, and location-based queries.
 */
@QuarkusTest
@TestTransaction
class ContactRepositoryTest {
    
    @Inject
    ContactRepository contactRepository;
    
    @Inject
    CustomerRepository customerRepository;
    
    private Customer testCustomer;
    
    @BeforeEach
    void setUp() {
        // Create test customer
        testCustomer = new Customer();
        testCustomer.setCustomerNumber("TEST-001");
        testCustomer.setCompanyName("Test Company GmbH");
        testCustomer.setCreatedBy("test");
        customerRepository.persist(testCustomer);
    }
    
    @Test
    void shouldCreateAndFindContact() {
        // Given
        Contact contact = createTestContact("Max", "Mustermann", true);
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
    void shouldFindContactsByCustomerId() {
        // Given
        Contact contact1 = createTestContact("Max", "Mustermann", true);
        Contact contact2 = createTestContact("Maria", "Musterfrau", false);
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
    void shouldFindPrimaryContact() {
        // Given
        Contact primary = createTestContact("Max", "Mustermann", true);
        Contact secondary = createTestContact("Maria", "Musterfrau", false);
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
    void shouldSetPrimaryContact() {
        // Given
        Contact contact1 = createTestContact("Max", "Mustermann", true);
        Contact contact2 = createTestContact("Maria", "Musterfrau", false);
        contactRepository.persist(contact1);
        contactRepository.persist(contact2);
        
        // When
        contactRepository.setPrimaryContact(testCustomer.getId(), contact2.getId());
        contactRepository.flush(); // Force update
        
        // Then
        Contact updated1 = contactRepository.findById(contact1.getId());
        Contact updated2 = contactRepository.findById(contact2.getId());
        assertThat(updated1.isPrimary()).isFalse();
        assertThat(updated2.isPrimary()).isTrue();
    }
    
    @Test
    void shouldHandleSoftDelete() {
        // Given
        Contact contact = createTestContact("Max", "Mustermann", true);
        contactRepository.persist(contact);
        
        // When
        int deleted = contactRepository.softDelete(contact.getId());
        
        // Then
        assertThat(deleted).isEqualTo(1);
        Contact found = contactRepository.findById(contact.getId());
        assertThat(found.isActive()).isFalse();
        
        // Should not find in active contacts
        List<Contact> activeContacts = contactRepository.findByCustomerId(testCustomer.getId());
        assertThat(activeContacts).isEmpty();
    }
    
    @Test
    void shouldFindContactsByEmail() {
        // Given
        Contact contact1 = createTestContact("Max", "Mustermann", true);
        contact1.setEmail("max@example.com");
        Contact contact2 = createTestContact("Maria", "Musterfrau", false);
        contact2.setEmail("MAX@EXAMPLE.COM"); // Different case
        contactRepository.persist(contact1);
        contactRepository.persist(contact2);
        
        // When
        List<Contact> found = contactRepository.findByEmail("max@example.com");
        
        // Then
        assertThat(found).hasSize(2); // Case-insensitive search
    }
    
    @Test
    void shouldCountActiveContacts() {
        // Given
        Contact active1 = createTestContact("Max", "Mustermann", true);
        Contact active2 = createTestContact("Maria", "Musterfrau", false);
        Contact inactive = createTestContact("Hans", "Meier", false);
        inactive.setActive(false);
        
        contactRepository.persist(active1);
        contactRepository.persist(active2);
        contactRepository.persist(inactive);
        
        // When
        long count = contactRepository.countActiveByCustomerId(testCustomer.getId());
        
        // Then
        assertThat(count).isEqualTo(2);
    }
    
    private Contact createTestContact(String firstName, String lastName, boolean isPrimary) {
        Contact contact = new Contact();
        contact.setCustomer(testCustomer);
        contact.setFirstName(firstName);
        contact.setLastName(lastName);
        contact.setPrimary(isPrimary);
        contact.setActive(true);
        contact.setCreatedBy("test");
        contact.setUpdatedBy("test");
        return contact;
    }
}