package de.freshplan.domain.customer.service.command;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.ContactDTO;
import de.freshplan.domain.customer.service.mapper.ContactMapper;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.UUID;

/**
 * Command service for Contact management. Handles all write operations (create, update, delete) for
 * contacts.
 *
 * <p>This service is part of the CQRS pattern implementation, separating command (write) operations
 * from query (read) operations.
 *
 * @author FreshPlan Team
 * @since 2.0.0 - CQRS Refactoring Phase 6
 */
@ApplicationScoped
public class ContactCommandService {

  @Inject ContactRepository contactRepository;
  @Inject CustomerRepository customerRepository;
  @Inject ContactMapper contactMapper;
  @Inject SecurityIdentity securityIdentity;

  /**
   * Create a new contact for a customer. First contact automatically becomes primary.
   *
   * @param customerId the customer ID
   * @param contactDTO the contact data
   * @return created contact DTO
   */
  @Transactional
  public ContactDTO createContact(UUID customerId, ContactDTO contactDTO) {
    // Verify customer exists
    Customer customer =
        customerRepository
            .findByIdOptional(customerId)
            .orElseThrow(() -> new NotFoundException("Customer not found: " + customerId));

    // Create contact entity
    CustomerContact contact = contactMapper.toEntity(contactDTO);
    contact.setCustomer(customer);

    // Set audit fields
    String username = getCurrentUser();
    contact.setCreatedBy(username);
    contact.setUpdatedBy(username);

    // If this is the first contact, make it primary
    if (!contactRepository.hasContacts(customerId)) {
      contact.setIsPrimary(true);
    }

    // Persist and return
    contactRepository.persist(contact);
    return contactMapper.toDTO(contact);
  }

  /**
   * Helper method to get current user with fallback for tests. Uses same 3-step fallback as other
   * services.
   */
  private String getCurrentUser() {
    try {
      return securityIdentity.getPrincipal().getName();
    } catch (Exception e) {
      // Fallback for tests
      String testUser = System.getProperty("test.user", "testuser");
      if ("testuser".equals(testUser)) {
        return "ci-test-user"; // CI Environment
      }
      return testUser.isEmpty() ? "temp" : testUser;
    }
  }

  /**
   * Update an existing contact. Note: isPrimary is not updated here, use setPrimaryContact method.
   *
   * @param contactId the contact ID
   * @param contactDTO the updated contact data
   * @return updated contact DTO
   */
  @Transactional
  public ContactDTO updateContact(UUID contactId, ContactDTO contactDTO) {
    CustomerContact contact =
        contactRepository
            .findByIdOptional(contactId)
            .orElseThrow(() -> new NotFoundException("Contact not found: " + contactId));

    // Update fields
    contactMapper.updateEntityFromDTO(contactDTO, contact);

    // Update audit field
    contact.setUpdatedBy(getCurrentUser());

    // Note: isPrimary is not updated here, use setPrimaryContact method

    return contactMapper.toDTO(contact);
  }

  /**
   * Update an existing contact, verifying it belongs to the specified customer. Note: isPrimary is
   * not updated here, use setPrimaryContact method.
   *
   * @param customerId the customer ID
   * @param contactId the contact ID
   * @param contactDTO the updated contact data
   * @return updated contact DTO
   */
  @Transactional
  public ContactDTO updateContact(UUID customerId, UUID contactId, ContactDTO contactDTO) {
    CustomerContact contact =
        contactRepository
            .findByIdOptional(contactId)
            .orElseThrow(() -> new NotFoundException("Contact not found: " + contactId));

    if (!contact.getCustomer().getId().equals(customerId)) {
      throw new NotFoundException("Contact not found: " + contactId);
    }

    // Update fields
    contactMapper.updateEntityFromDTO(contactDTO, contact);

    // Update audit field
    contact.setUpdatedBy(getCurrentUser());

    // Note: isPrimary is not updated here, use setPrimaryContact method

    return contactMapper.toDTO(contact);
  }

  /**
   * Set a contact as primary for a customer. Unsets all other primary flags for this customer.
   *
   * @param customerId the customer ID
   * @param contactId the contact ID to set as primary
   */
  @Transactional
  public void setPrimaryContact(UUID customerId, UUID contactId) {
    // Verify contact belongs to customer
    CustomerContact contact =
        contactRepository
            .findByIdOptional(contactId)
            .orElseThrow(() -> new NotFoundException("Contact not found: " + contactId));

    if (!contact.getCustomer().getId().equals(customerId)) {
      throw new IllegalArgumentException("Contact does not belong to the specified customer");
    }

    // Update primary status
    contactRepository.setPrimaryContact(customerId, contactId);
  }

  /**
   * Soft delete a contact. Cannot delete primary contact if there are other contacts.
   *
   * @param contactId the contact ID
   */
  @Transactional
  public void deleteContact(UUID contactId) {
    CustomerContact contact =
        contactRepository
            .findByIdOptional(contactId)
            .orElseThrow(() -> new NotFoundException("Contact not found: " + contactId));

    // Don't delete primary contact if there are other contacts
    if (contact.getIsPrimary()) {
      long contactCount = contactRepository.countActiveByCustomerId(contact.getCustomer().getId());
      if (contactCount > 1) {
        throw new IllegalStateException(
            "Cannot delete primary contact. Please assign another contact as primary first.");
      }
    }

    // Soft delete
    contact.setIsActive(false);
    contact.setUpdatedBy(getCurrentUser());
  }

  /**
   * Soft delete a contact, verifying it belongs to the specified customer. Cannot delete primary
   * contact if there are other contacts.
   *
   * @param customerId the customer ID
   * @param contactId the contact ID
   */
  @Transactional
  public void deleteContact(UUID customerId, UUID contactId) {
    CustomerContact contact =
        contactRepository
            .findByIdOptional(contactId)
            .orElseThrow(() -> new NotFoundException("Contact not found: " + contactId));

    if (!contact.getCustomer().getId().equals(customerId)) {
      throw new NotFoundException("Contact not found: " + contactId);
    }

    // Don't delete primary contact if there are other contacts
    if (contact.getIsPrimary()) {
      long contactCount = contactRepository.countActiveByCustomerId(contact.getCustomer().getId());
      if (contactCount > 1) {
        throw new IllegalStateException(
            "Cannot delete primary contact. Please assign another contact as primary first.");
      }
    }

    // Soft delete
    contact.setIsActive(false);
    contact.setUpdatedBy(getCurrentUser());
  }

  /**
   * Assign contacts to a location. This method updates the location assignment for multiple
   * contacts.
   *
   * @param contactIds list of contact IDs
   * @param locationId the location ID (null to unassign)
   * @return number of updated contacts
   */
  @Transactional
  public int assignContactsToLocation(List<UUID> contactIds, UUID locationId) {
    return contactRepository.updateLocationAssignment(contactIds, locationId);
  }
}
