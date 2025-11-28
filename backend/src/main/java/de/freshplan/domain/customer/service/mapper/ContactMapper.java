package de.freshplan.domain.customer.service.mapper;

import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.entity.CustomerLocation;
import de.freshplan.domain.customer.service.dto.ContactDTO;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mapper for converting between Contact entity and ContactDTO. Handles all transformations with
 * proper validation.
 */
@ApplicationScoped
public class ContactMapper {

  /**
   * Convert Contact entity to ContactDTO
   *
   * @param contact the entity
   * @return the DTO
   */
  public ContactDTO toDTO(CustomerContact contact) {
    if (contact == null) {
      return null;
    }

    ContactDTO dto = new ContactDTO();
    dto.setId(contact.getId());
    dto.setCustomerId(contact.getCustomer() != null ? contact.getCustomer().getId() : null);

    // Basic Info
    dto.setSalutation(contact.getSalutation());
    dto.setTitle(contact.getTitle());
    dto.setFirstName(contact.getFirstName());
    dto.setLastName(contact.getLastName());
    dto.setPosition(contact.getPosition());
    dto.setDecisionLevel(contact.getDecisionLevel());

    // Contact Info
    dto.setEmail(contact.getEmail());
    dto.setPhone(contact.getPhone());
    dto.setMobile(contact.getMobile());

    // Flags
    dto.setPrimary(contact.getIsPrimary());
    dto.setActive(contact.getIsActive());

    // Location (Single - DEPRECATED)
    if (contact.getAssignedLocation() != null) {
      dto.setAssignedLocationId(contact.getAssignedLocation().getId());
      dto.setAssignedLocationName(contact.getAssignedLocation().getLocationName());
    }

    // Sprint 2.1.7.7: Multi-Location Assignment
    dto.setResponsibilityScope(contact.getResponsibilityScope());
    if (contact.getAssignedLocations() != null && !contact.getAssignedLocations().isEmpty()) {
      List<UUID> locationIds = new ArrayList<>();
      List<String> locationNames = new ArrayList<>();
      for (CustomerLocation location : contact.getAssignedLocations()) {
        locationIds.add(location.getId());
        locationNames.add(location.getLocationName());
      }
      dto.setAssignedLocationIds(locationIds);
      dto.setAssignedLocationNames(locationNames);
    }

    // Relationship Data
    dto.setBirthday(contact.getBirthday());
    dto.setHobbies(contact.getHobbies());
    dto.setFamilyStatus(contact.getFamilyStatus());
    dto.setChildrenCount(contact.getChildrenCount());
    dto.setPersonalNotes(contact.getPersonalNotes());

    // V2 Fields (Sprint 2.1.7.2 D11.1)
    dto.setLinkedin(contact.getLinkedin());
    dto.setXing(contact.getXing());
    dto.setNotes(contact.getNotes());

    // Audit Info
    dto.setCreatedAt(contact.getCreatedAt());
    dto.setUpdatedAt(contact.getUpdatedAt());
    dto.setCreatedBy(contact.getCreatedBy());
    dto.setUpdatedBy(contact.getUpdatedBy());

    // Computed fields
    dto.setFullName(contact.getFullName());
    dto.setDisplayName(contact.getDisplayName());

    return dto;
  }

  /**
   * Convert ContactDTO to Contact entity
   *
   * @param dto the DTO
   * @return the entity
   */
  public CustomerContact toEntity(ContactDTO dto) {
    if (dto == null) {
      return null;
    }

    CustomerContact contact = new CustomerContact();

    // Basic Info
    contact.setSalutation(dto.getSalutation());
    contact.setTitle(dto.getTitle());
    contact.setFirstName(dto.getFirstName());
    contact.setLastName(dto.getLastName());
    contact.setPosition(dto.getPosition());
    contact.setDecisionLevel(dto.getDecisionLevel());

    // Contact Info
    contact.setEmail(dto.getEmail());
    contact.setPhone(dto.getPhone());
    contact.setMobile(dto.getMobile());

    // Flags
    contact.setIsPrimary(dto.isPrimary());
    contact.setIsActive(dto.isActive());

    // Relationship Data
    contact.setBirthday(dto.getBirthday());
    contact.setHobbies(dto.getHobbies());
    contact.setFamilyStatus(dto.getFamilyStatus());
    contact.setChildrenCount(dto.getChildrenCount());
    contact.setPersonalNotes(dto.getPersonalNotes());

    // Note: Customer, Location, and audit fields are set in service layer

    return contact;
  }

  /**
   * Update existing entity from DTO
   *
   * @param dto the source DTO
   * @param contact the target entity to update
   */
  public void updateEntityFromDTO(ContactDTO dto, CustomerContact contact) {
    if (dto == null || contact == null) {
      return;
    }

    // PMD Complexity Refactoring (Issue #146) - Extracted to helper methods
    updateBasicInfo(dto, contact);
    updateContactInfo(dto, contact);
    updateRelationshipData(dto, contact);
    updateSocialProfiles(dto, contact);
  }

  // ============================================================================
  // PMD Complexity Refactoring (Issue #146) - Helper methods for updateEntityFromDTO()
  // ============================================================================

  private void updateBasicInfo(ContactDTO dto, CustomerContact contact) {
    if (dto.getSalutation() != null) {
      contact.setSalutation(dto.getSalutation());
    }
    if (dto.getTitle() != null) {
      contact.setTitle(dto.getTitle());
    }
    if (dto.getFirstName() != null) {
      contact.setFirstName(dto.getFirstName());
    }
    if (dto.getLastName() != null) {
      contact.setLastName(dto.getLastName());
    }
    if (dto.getPosition() != null) {
      contact.setPosition(dto.getPosition());
    }
    if (dto.getDecisionLevel() != null) {
      contact.setDecisionLevel(dto.getDecisionLevel());
    }
  }

  private void updateContactInfo(ContactDTO dto, CustomerContact contact) {
    if (dto.getEmail() != null) {
      contact.setEmail(dto.getEmail());
    }
    if (dto.getPhone() != null) {
      contact.setPhone(dto.getPhone());
    }
    if (dto.getMobile() != null) {
      contact.setMobile(dto.getMobile());
    }
  }

  private void updateRelationshipData(ContactDTO dto, CustomerContact contact) {
    if (dto.getBirthday() != null) {
      contact.setBirthday(dto.getBirthday());
    }
    if (dto.getHobbies() != null) {
      contact.setHobbies(dto.getHobbies());
    }
    if (dto.getFamilyStatus() != null) {
      contact.setFamilyStatus(dto.getFamilyStatus());
    }
    if (dto.getChildrenCount() != null) {
      contact.setChildrenCount(dto.getChildrenCount());
    }
    if (dto.getPersonalNotes() != null) {
      contact.setPersonalNotes(dto.getPersonalNotes());
    }
  }

  private void updateSocialProfiles(ContactDTO dto, CustomerContact contact) {
    if (dto.getLinkedin() != null) {
      contact.setLinkedin(dto.getLinkedin());
    }
    if (dto.getXing() != null) {
      contact.setXing(dto.getXing());
    }
    if (dto.getNotes() != null) {
      contact.setNotes(dto.getNotes());
    }
  }
}
