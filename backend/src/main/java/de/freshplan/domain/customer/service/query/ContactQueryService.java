package de.freshplan.domain.customer.service.query;

import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.service.dto.ContactDTO;
import de.freshplan.domain.customer.service.mapper.ContactMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Query service for Contact management.
 * Handles all read operations for contacts.
 * 
 * This service is part of the CQRS pattern implementation, separating
 * query (read) operations from command (write) operations.
 * 
 * IMPORTANT: No @Transactional annotation on this service as all operations are read-only!
 * 
 * @author FreshPlan Team
 * @since 2.0.0 - CQRS Refactoring Phase 6
 */
@ApplicationScoped
public class ContactQueryService {

  @Inject ContactRepository contactRepository;
  @Inject ContactMapper contactMapper;

  /**
   * Get all contacts for a customer.
   * Contacts are sorted by primary status (primary first) and then by name.
   *
   * @param customerId the customer ID
   * @return list of contact DTOs
   */
  public List<ContactDTO> getContactsByCustomerId(UUID customerId) {
    return contactRepository.findByCustomerId(customerId).stream()
        .map(contactMapper::toDTO)
        .collect(Collectors.toList());
  }

  /**
   * Get a single contact by ID.
   *
   * @param contactId the contact ID
   * @return contact DTO
   * @throws NotFoundException if contact not found
   */
  public ContactDTO getContact(UUID contactId) {
    CustomerContact contact =
        contactRepository
            .findByIdOptional(contactId)
            .orElseThrow(() -> new NotFoundException("Contact not found: " + contactId));
    return contactMapper.toDTO(contact);
  }

  /**
   * Get a single contact by ID, verifying it belongs to the specified customer.
   * Additional security check for customer-scoped operations.
   *
   * @param customerId the customer ID
   * @param contactId the contact ID
   * @return contact DTO
   * @throws NotFoundException if contact not found or doesn't belong to customer
   */
  public ContactDTO getContact(UUID customerId, UUID contactId) {
    CustomerContact contact =
        contactRepository
            .findByIdOptional(contactId)
            .orElseThrow(() -> new NotFoundException("Contact not found: " + contactId));

    if (!contact.getCustomer().getId().equals(customerId)) {
      throw new NotFoundException("Contact not found: " + contactId);
    }

    return contactMapper.toDTO(contact);
  }

  /**
   * Get contacts assigned to a specific location.
   *
   * @param locationId the location ID
   * @return list of contact DTOs
   */
  public List<ContactDTO> getContactsByLocationId(UUID locationId) {
    return contactRepository.findByLocationId(locationId).stream()
        .map(contactMapper::toDTO)
        .collect(Collectors.toList());
  }

  /**
   * Get contacts with upcoming birthdays.
   * Note: Birthday functionality is not yet implemented in entity.
   *
   * @param daysAhead number of days to look ahead
   * @return list of contact DTOs with birthdays
   */
  public List<ContactDTO> getUpcomingBirthdays(int daysAhead) {
    return contactRepository.findUpcomingBirthdays(daysAhead).stream()
        .map(contactMapper::toDTO)
        .collect(Collectors.toList());
  }

  /**
   * Check if email is already used by another contact.
   * Useful for validation before creating/updating contacts.
   *
   * @param email the email to check
   * @param excludeContactId optional contact ID to exclude from check (for updates)
   * @return true if email is already in use by another contact
   */
  public boolean isEmailInUse(String email, UUID excludeContactId) {
    List<CustomerContact> contacts = contactRepository.findByEmail(email);
    if (excludeContactId == null) {
      return !contacts.isEmpty();
    }
    return contacts.stream().anyMatch(c -> !c.getId().equals(excludeContactId));
  }
}