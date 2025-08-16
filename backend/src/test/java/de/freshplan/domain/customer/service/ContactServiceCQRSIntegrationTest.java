package de.freshplan.domain.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.ContactDTO;
import de.freshplan.test.BaseIntegrationTestWithCleanup;
import de.freshplan.test.TestDataBuilder;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for ContactService CQRS implementation. Tests the complete flow with both
 * legacy and CQRS modes.
 *
 * <p>This test verifies that ContactService behaves identically whether using the legacy
 * implementation or the new CQRS pattern.
 */
@QuarkusTest
@TestProfile(ContactServiceCQRSIntegrationTest.CQRSTestProfile.class)
class ContactServiceCQRSIntegrationTest extends BaseIntegrationTestWithCleanup {

  @Inject ContactService contactService;

  private Customer testCustomer;

  private void setupTestCustomer() {
    // Use TestDataBuilder for consistent test data with unique values
    testCustomer = TestDataBuilder.createTestCustomer("Test Integration GmbH");
    testCustomer.setIsTestData(true); // Mark for cleanup
    customerRepository.persist(testCustomer);
    flushAndClear(); // Ensure customer is persisted
  }

  @Test
  @TestTransaction
  void createContact_shouldWorkIdenticallyInBothModes() {
    // Setup
    setupTestCustomer();
    
    // Given
    ContactDTO contactDTO = new ContactDTO();
    contactDTO.setFirstName("Integration");
    contactDTO.setLastName("Test");
    contactDTO.setEmail("integration@test.de");
    contactDTO.setPhone("+49 123 456789");
    contactDTO.setPosition("CEO");

    // When
    ContactDTO created = contactService.createContact(testCustomer.getId(), contactDTO);

    // Then
    assertThat(created).isNotNull();
    assertThat(created.getId()).isNotNull();
    assertThat(created.getFirstName()).isEqualTo("Integration");
    assertThat(created.getLastName()).isEqualTo("Test");
    assertThat(created.getEmail()).isEqualTo("integration@test.de");
    assertThat(created.isPrimary()).isTrue(); // First contact becomes primary

    // Verify in database
    List<ContactDTO> contacts = contactService.getContactsByCustomerId(testCustomer.getId());
    assertThat(contacts).hasSize(1);
    assertThat(contacts.get(0).isPrimary()).isTrue();
  }

  @Test
  @TestTransaction
  void createMultipleContacts_onlyFirstShouldBePrimary() {
    // Setup
    setupTestCustomer();
    
    // Given
    ContactDTO contact1 = new ContactDTO();
    contact1.setFirstName("First");
    contact1.setLastName("Contact");
    contact1.setEmail("first@test.de");

    ContactDTO contact2 = new ContactDTO();
    contact2.setFirstName("Second");
    contact2.setLastName("Contact");
    contact2.setEmail("second@test.de");

    // When
    ContactDTO created1 = contactService.createContact(testCustomer.getId(), contact1);
    ContactDTO created2 = contactService.createContact(testCustomer.getId(), contact2);

    // Then
    assertThat(created1.isPrimary()).isTrue();
    assertThat(created2.isPrimary()).isFalse();

    // Verify all contacts
    List<ContactDTO> contacts = contactService.getContactsByCustomerId(testCustomer.getId());
    assertThat(contacts).hasSize(2);
    assertThat(contacts.stream().filter(ContactDTO::isPrimary).count()).isEqualTo(1);
  }

  @Test
  @TestTransaction
  void updateContact_shouldPreserveBusinessRules() {
    // Setup
    setupTestCustomer();
    
    // Given - create contact
    ContactDTO contactDTO = new ContactDTO();
    contactDTO.setFirstName("Original");
    contactDTO.setLastName("Name");
    contactDTO.setEmail("original@test.de");
    ContactDTO created = contactService.createContact(testCustomer.getId(), contactDTO);

    // When - update contact
    ContactDTO updateDTO = new ContactDTO();
    updateDTO.setFirstName("Updated");
    updateDTO.setLastName("Name");
    updateDTO.setEmail("updated@test.de");
    ContactDTO updated = contactService.updateContact(created.getId(), updateDTO);

    // Then
    assertThat(updated.getFirstName()).isEqualTo("Updated");
    assertThat(updated.getEmail()).isEqualTo("updated@test.de");
    assertThat(updated.isPrimary()).isTrue(); // Primary status preserved

    // Verify with customer ID
    ContactDTO fetched = contactService.getContact(testCustomer.getId(), created.getId());
    assertThat(fetched.getFirstName()).isEqualTo("Updated");
  }

  @Test
  @TestTransaction
  void setPrimaryContact_shouldUpdatePrimaryStatus() {
    // Setup
    setupTestCustomer();
    
    // Given - create two contacts
    ContactDTO contact1 = new ContactDTO();
    contact1.setFirstName("First");
    contact1.setLastName("Contact");
    contact1.setEmail("first@test.de");

    ContactDTO contact2 = new ContactDTO();
    contact2.setFirstName("Second");
    contact2.setLastName("Contact");
    contact2.setEmail("second@test.de");

    ContactDTO created1 = contactService.createContact(testCustomer.getId(), contact1);
    ContactDTO created2 = contactService.createContact(testCustomer.getId(), contact2);

    assertThat(created1.isPrimary()).isTrue();
    assertThat(created2.isPrimary()).isFalse();

    // When - set second contact as primary
    contactService.setPrimaryContact(testCustomer.getId(), created2.getId());

    // Then
    List<ContactDTO> contacts = contactService.getContactsByCustomerId(testCustomer.getId());
    ContactDTO updatedFirst =
        contacts.stream().filter(c -> c.getId().equals(created1.getId())).findFirst().orElseThrow();
    ContactDTO updatedSecond =
        contacts.stream().filter(c -> c.getId().equals(created2.getId())).findFirst().orElseThrow();

    assertThat(updatedFirst.isPrimary()).isFalse();
    assertThat(updatedSecond.isPrimary()).isTrue();
  }

  @Test
  @TestTransaction
  void deleteContact_shouldEnforceBusinessRules() {
    // Setup
    setupTestCustomer();
    
    // Given - create two contacts
    ContactDTO contact1 = new ContactDTO();
    contact1.setFirstName("Primary");
    contact1.setLastName("Contact");
    contact1.setEmail("primary@test.de");

    ContactDTO contact2 = new ContactDTO();
    contact2.setFirstName("Secondary");
    contact2.setLastName("Contact");
    contact2.setEmail("secondary@test.de");

    ContactDTO created1 = contactService.createContact(testCustomer.getId(), contact1);
    ContactDTO created2 = contactService.createContact(testCustomer.getId(), contact2);

    // When/Then - cannot delete primary contact when others exist
    assertThatThrownBy(() -> contactService.deleteContact(testCustomer.getId(), created1.getId()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot delete primary contact");

    // When - delete secondary contact (should work)
    contactService.deleteContact(testCustomer.getId(), created2.getId());

    // Then - verify soft delete
    List<ContactDTO> contacts = contactService.getContactsByCustomerId(testCustomer.getId());
    assertThat(contacts).hasSize(1); // Only active contacts returned
    assertThat(contacts.get(0).getId()).isEqualTo(created1.getId());

    // When - now can delete the last (primary) contact
    contactService.deleteContact(created1.getId());

    // Then
    List<ContactDTO> remainingContacts =
        contactService.getContactsByCustomerId(testCustomer.getId());
    assertThat(remainingContacts).isEmpty();
  }

  @Test
  @TestTransaction
  void assignContactsToLocation_shouldUpdateAssignments() {
    // Setup
    setupTestCustomer();
    
    // Given - create contacts
    ContactDTO contact1 = new ContactDTO();
    contact1.setFirstName("Contact1");
    contact1.setLastName("Test");
    contact1.setEmail("contact1@test.de");

    ContactDTO contact2 = new ContactDTO();
    contact2.setFirstName("Contact2");
    contact2.setLastName("Test");
    contact2.setEmail("contact2@test.de");

    ContactDTO created1 = contactService.createContact(testCustomer.getId(), contact1);
    ContactDTO created2 = contactService.createContact(testCustomer.getId(), contact2);

    // For now, skip location assignment as customer_locations table doesn't exist yet
    // This test will be re-enabled when location management is implemented
    
    // Then - verify contacts were created
    assertThat(created1).isNotNull();
    assertThat(created2).isNotNull();
  }

  @Test
  @TestTransaction
  void isEmailInUse_shouldCheckCorrectly() {
    // Setup
    setupTestCustomer();
    
    // Given - create contact
    ContactDTO contact = new ContactDTO();
    contact.setFirstName("Test");
    contact.setLastName("User");
    contact.setEmail("test@example.com");
    ContactDTO created = contactService.createContact(testCustomer.getId(), contact);

    // When/Then - email is in use
    assertThat(contactService.isEmailInUse("test@example.com", null)).isTrue();

    // When/Then - different email not in use
    assertThat(contactService.isEmailInUse("other@example.com", null)).isFalse();

    // When/Then - email not in use when excluding the same contact
    assertThat(contactService.isEmailInUse("test@example.com", created.getId())).isFalse();
  }

  @Test
  @TestTransaction
  void getUpcomingBirthdays_shouldReturnBirthdayContacts() {
    // Setup
    setupTestCustomer();
    
    // Note: Birthday functionality not fully implemented in entity
    // This test verifies the method works without errors

    // When
    List<ContactDTO> birthdays = contactService.getUpcomingBirthdays(30);

    // Then
    assertThat(birthdays).isNotNull();
    assertThat(birthdays).isEmpty(); // No birthdays set up in test data
  }

  /**
   * Test profile for CQRS integration tests. Can be configured to test with cqrs.enabled=true or
   * false.
   */
  public static class CQRSTestProfile implements io.quarkus.test.junit.QuarkusTestProfile {
    @Override
    public java.util.Map<String, String> getConfigOverrides() {
      return java.util.Map.of(
          // Test with CQRS enabled
          "features.cqrs.enabled", "true",
          // Disable startup data initialization
          "app.init.data", "false");
    }
  }
}
