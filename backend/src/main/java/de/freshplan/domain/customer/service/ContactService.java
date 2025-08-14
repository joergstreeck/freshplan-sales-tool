package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.command.ContactCommandService;
import de.freshplan.domain.customer.service.query.ContactQueryService;
import de.freshplan.domain.customer.service.dto.ContactDTO;
import de.freshplan.domain.customer.service.mapper.ContactMapper;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Service layer for Contact management. 
 * 
 * This service now acts as a FACADE with Feature Flag support for gradual CQRS migration.
 * When cqrs.enabled=true, it delegates to ContactCommandService and ContactQueryService.
 * When cqrs.enabled=false, it uses the legacy implementation.
 * 
 * @deprecated Will be removed once CQRS migration is complete
 */
@ApplicationScoped
@Transactional
public class ContactService {

  @Inject ContactRepository contactRepository;

  @Inject CustomerRepository customerRepository;

  @Inject ContactMapper contactMapper;

  @Inject SecurityIdentity securityIdentity;
  
  // CQRS Services
  @Inject ContactCommandService commandService;
  @Inject ContactQueryService queryService;
  
  // Feature Flag for CQRS
  @ConfigProperty(name = "features.cqrs.enabled", defaultValue = "false")
  boolean cqrsEnabled;

  /**
   * Create a new contact for a customer
   *
   * @param customerId the customer ID
   * @param contactDTO the contact data
   * @return created contact DTO
   */
  public ContactDTO createContact(UUID customerId, ContactDTO contactDTO) {
    if (cqrsEnabled) {
      return commandService.createContact(customerId, contactDTO);
    }
    // Legacy implementation below
    // Verify customer exists
    Customer customer =
        customerRepository
            .findByIdOptional(customerId)
            .orElseThrow(() -> new NotFoundException("Customer not found: " + customerId));

    // Create contact entity
    CustomerContact contact = contactMapper.toEntity(contactDTO);
    contact.setCustomer(customer);

    // Set audit fields
    String username = securityIdentity.getPrincipal().getName();
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
   * Update an existing contact
   *
   * @param contactId the contact ID
   * @param contactDTO the updated contact data
   * @return updated contact DTO
   */
  public ContactDTO updateContact(UUID contactId, ContactDTO contactDTO) {
    if (cqrsEnabled) {
      return commandService.updateContact(contactId, contactDTO);
    }
    // Legacy implementation below
    CustomerContact contact =
        contactRepository
            .findByIdOptional(contactId)
            .orElseThrow(() -> new NotFoundException("Contact not found: " + contactId));

    // Update fields
    contactMapper.updateEntityFromDTO(contactDTO, contact);

    // Update audit field
    contact.setUpdatedBy(securityIdentity.getPrincipal().getName());

    // Note: isPrimary is not updated here, use setPrimaryContact method

    return contactMapper.toDTO(contact);
  }

  /**
   * Update an existing contact, verifying it belongs to the specified customer
   *
   * @param customerId the customer ID
   * @param contactId the contact ID
   * @param contactDTO the updated contact data
   * @return updated contact DTO
   */
  public ContactDTO updateContact(UUID customerId, UUID contactId, ContactDTO contactDTO) {
    if (cqrsEnabled) {
      return commandService.updateContact(customerId, contactId, contactDTO);
    }
    // Legacy implementation below
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
    contact.setUpdatedBy(securityIdentity.getPrincipal().getName());

    // Note: isPrimary is not updated here, use setPrimaryContact method

    return contactMapper.toDTO(contact);
  }

  /**
   * Get all contacts for a customer
   *
   * @param customerId the customer ID
   * @return list of contact DTOs
   */
  public List<ContactDTO> getContactsByCustomerId(UUID customerId) {
    if (cqrsEnabled) {
      return queryService.getContactsByCustomerId(customerId);
    }
    // Legacy implementation below
    return contactRepository.findByCustomerId(customerId).stream()
        .map(contactMapper::toDTO)
        .collect(Collectors.toList());
  }

  /**
   * Get a single contact by ID
   *
   * @param contactId the contact ID
   * @return contact DTO
   */
  public ContactDTO getContact(UUID contactId) {
    if (cqrsEnabled) {
      return queryService.getContact(contactId);
    }
    // Legacy implementation below
    CustomerContact contact =
        contactRepository
            .findByIdOptional(contactId)
            .orElseThrow(() -> new NotFoundException("Contact not found: " + contactId));
    return contactMapper.toDTO(contact);
  }

  /**
   * Get a single contact by ID, verifying it belongs to the specified customer
   *
   * @param customerId the customer ID
   * @param contactId the contact ID
   * @return contact DTO
   */
  public ContactDTO getContact(UUID customerId, UUID contactId) {
    if (cqrsEnabled) {
      return queryService.getContact(customerId, contactId);
    }
    // Legacy implementation below
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
   * Set a contact as primary for a customer
   *
   * @param customerId the customer ID
   * @param contactId the contact ID to set as primary
   */
  public void setPrimaryContact(UUID customerId, UUID contactId) {
    if (cqrsEnabled) {
      commandService.setPrimaryContact(customerId, contactId);
      return;
    }
    // Legacy implementation below
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
   * Soft delete a contact
   *
   * @param contactId the contact ID
   */
  public void deleteContact(UUID contactId) {
    if (cqrsEnabled) {
      commandService.deleteContact(contactId);
      return;
    }
    // Legacy implementation below
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
    contact.setUpdatedBy(securityIdentity.getPrincipal().getName());
  }

  /**
   * Soft delete a contact, verifying it belongs to the specified customer
   *
   * @param customerId the customer ID
   * @param contactId the contact ID
   */
  public void deleteContact(UUID customerId, UUID contactId) {
    if (cqrsEnabled) {
      commandService.deleteContact(customerId, contactId);
      return;
    }
    // Legacy implementation below
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
    contact.setUpdatedBy(securityIdentity.getPrincipal().getName());
  }

  /**
   * Get contacts assigned to a location
   *
   * @param locationId the location ID
   * @return list of contact DTOs
   */
  public List<ContactDTO> getContactsByLocationId(UUID locationId) {
    if (cqrsEnabled) {
      return queryService.getContactsByLocationId(locationId);
    }
    // Legacy implementation below
    return contactRepository.findByLocationId(locationId).stream()
        .map(contactMapper::toDTO)
        .collect(Collectors.toList());
  }

  /**
   * Assign contacts to a location
   *
   * @param contactIds list of contact IDs
   * @param locationId the location ID (null to unassign)
   * @return number of updated contacts
   */
  public int assignContactsToLocation(List<UUID> contactIds, UUID locationId) {
    if (cqrsEnabled) {
      return commandService.assignContactsToLocation(contactIds, locationId);
    }
    // Legacy implementation below
    return contactRepository.updateLocationAssignment(contactIds, locationId);
  }

  /**
   * Get contacts with upcoming birthdays
   *
   * @param daysAhead number of days to look ahead
   * @return list of contact DTOs with birthdays
   */
  public List<ContactDTO> getUpcomingBirthdays(int daysAhead) {
    if (cqrsEnabled) {
      return queryService.getUpcomingBirthdays(daysAhead);
    }
    // Legacy implementation below
    return contactRepository.findUpcomingBirthdays(daysAhead).stream()
        .map(contactMapper::toDTO)
        .collect(Collectors.toList());
  }

  /**
   * Check if email is already used by another contact
   *
   * @param email the email to check
   * @param excludeContactId optional contact ID to exclude from check
   * @return true if email is already in use
   */
  public boolean isEmailInUse(String email, UUID excludeContactId) {
    if (cqrsEnabled) {
      return queryService.isEmailInUse(email, excludeContactId);
    }
    // Legacy implementation below
    List<CustomerContact> contacts = contactRepository.findByEmail(email);
    if (excludeContactId == null) {
      return !contacts.isEmpty();
    }
    return contacts.stream().anyMatch(c -> !c.getId().equals(excludeContactId));
  }
}
