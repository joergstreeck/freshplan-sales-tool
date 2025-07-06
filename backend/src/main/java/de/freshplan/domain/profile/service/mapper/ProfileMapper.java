package de.freshplan.domain.profile.service.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.freshplan.domain.profile.entity.Profile;
import de.freshplan.domain.profile.service.dto.CreateProfileRequest;
import de.freshplan.domain.profile.service.dto.ProfileResponse;
import de.freshplan.domain.profile.service.dto.UpdateProfileRequest;
import de.freshplan.domain.profile.service.exception.ProfileMappingException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Mapper for converting between Profile entities and DTOs.
 *
 * @author FreshPlan Team
 * @since 1.0.0
 */
@ApplicationScoped
public class ProfileMapper {

  @Inject ObjectMapper objectMapper;

  /**
   * Map CreateProfileRequest to Profile entity.
   *
   * @param request the create request
   * @return new Profile entity
   */
  public Profile toEntity(CreateProfileRequest request) {
    Profile profile = new Profile();
    profile.setCustomerId(request.getCustomerId());
    profile.setNotes(request.getNotes());

    // Convert nested objects to JSON
    profile.setCompanyInfo(toJson(request.getCompanyInfo()));
    profile.setContactInfo(toJson(request.getContactInfo()));
    profile.setFinancialInfo(toJson(request.getFinancialInfo()));

    return profile;
  }

  /**
   * Update existing Profile entity with data from UpdateProfileRequest.
   *
   * @param profile existing profile entity
   * @param request update request
   * @return updated profile entity
   */
  public Profile updateEntity(Profile profile, UpdateProfileRequest request) {
    if (request.getCompanyInfo() != null) {
      profile.setCompanyInfo(toJson(request.getCompanyInfo()));
    }

    if (request.getContactInfo() != null) {
      profile.setContactInfo(toJson(request.getContactInfo()));
    }

    if (request.getFinancialInfo() != null) {
      profile.setFinancialInfo(toJson(request.getFinancialInfo()));
    }

    if (request.getNotes() != null) {
      profile.setNotes(request.getNotes());
    }

    return profile;
  }

  /**
   * Map Profile entity to ProfileResponse DTO.
   *
   * @param profile the profile entity
   * @return profile response DTO
   */
  public ProfileResponse toResponse(Profile profile) {
    return ProfileResponse.builder()
        .id(profile.getId())
        .customerId(profile.getCustomerId())
        .companyInfo(fromJson(profile.getCompanyInfo(), CreateProfileRequest.CompanyInfo.class))
        .contactInfo(fromJson(profile.getContactInfo(), CreateProfileRequest.ContactInfo.class))
        .financialInfo(
            fromJson(profile.getFinancialInfo(), CreateProfileRequest.FinancialInfo.class))
        .notes(profile.getNotes())
        .createdAt(profile.getCreatedAt())
        .updatedAt(profile.getUpdatedAt())
        .createdBy(profile.getCreatedBy())
        .updatedBy(profile.getUpdatedBy())
        .version(profile.getVersion())
        .build();
  }

  /**
   * Convert object to JSON string.
   *
   * @param object the object to convert
   * @return JSON string or null if object is null
   */
  private String toJson(Object object) {
    if (object == null) {
      return null;
    }

    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ProfileMappingException("Failed to convert object to JSON", e);
    }
  }

  /**
   * Convert JSON string to object.
   *
   * @param json the JSON string
   * @param clazz the target class
   * @param <T> the type parameter
   * @return the converted object or null if json is null
   */
  private <T> T fromJson(String json, Class<T> clazz) {
    if (json == null || json.isBlank()) {
      return null;
    }

    try {
      return objectMapper.readValue(json, clazz);
    } catch (JsonProcessingException e) {
      throw new ProfileMappingException(
          "Failed to parse JSON for class: " + clazz.getSimpleName(), e);
    }
  }
}
