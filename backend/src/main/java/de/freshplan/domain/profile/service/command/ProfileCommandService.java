package de.freshplan.domain.profile.service.command;

import de.freshplan.domain.profile.entity.Profile;
import de.freshplan.domain.profile.repository.ProfileRepository;
import de.freshplan.domain.profile.service.dto.CreateProfileRequest;
import de.freshplan.domain.profile.service.dto.ProfileResponse;
import de.freshplan.domain.profile.service.dto.UpdateProfileRequest;
import de.freshplan.domain.profile.service.exception.DuplicateProfileException;
import de.freshplan.domain.profile.service.exception.ProfileNotFoundException;
import de.freshplan.domain.profile.service.mapper.ProfileMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import org.jboss.logging.Logger;

/**
 * Command service for Profile write operations.
 * Handles all state-changing operations for profiles.
 * 
 * Part of CQRS refactoring - Phase 11
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class ProfileCommandService {

  private static final Logger LOG = Logger.getLogger(ProfileCommandService.class);

  @Inject ProfileRepository profileRepository;

  @Inject ProfileMapper profileMapper;

  /**
   * Create a new profile.
   * EXACT COPY from ProfileService.createProfile() lines 45-75
   *
   * @param request the create request
   * @return the created profile response
   * @throws DuplicateProfileException if profile already exists
   */
  @Transactional
  public ProfileResponse createProfile(CreateProfileRequest request) {
    // Defensive validation - EXACT COPY
    if (request == null) {
      throw new IllegalArgumentException("CreateProfileRequest cannot be null");
    }
    if (request.getCustomerId() == null || request.getCustomerId().trim().isEmpty()) {
      throw new IllegalArgumentException("Customer ID cannot be null or empty");
    }

    LOG.debugf("Creating profile for customer: %s", request.getCustomerId());

    // Check for duplicate - EXACT COPY
    if (profileRepository.existsByCustomerId(request.getCustomerId())) {
      LOG.warnf("Duplicate profile attempt for customer: %s", request.getCustomerId());
      throw new DuplicateProfileException(request.getCustomerId());
    }

    // Create and persist profile - EXACT COPY
    Profile profile = profileMapper.toEntity(request);
    // TODO: Get user from security context when auth is implemented
    profile.setCreatedBy("system");
    profile.setUpdatedBy("system");

    profileRepository.persist(profile);

    LOG.infof(
        "Profile created successfully for customer: %s with ID: %s",
        request.getCustomerId(), profile.getId());

    return profileMapper.toResponse(profile);
  }

  /**
   * Update an existing profile.
   * EXACT COPY from ProfileService.updateProfile() lines 127-150
   *
   * @param id the profile ID
   * @param request the update request
   * @return the updated profile response
   * @throws ProfileNotFoundException if not found
   */
  @Transactional
  public ProfileResponse updateProfile(UUID id, UpdateProfileRequest request) {
    // Defensive validation - EXACT COPY
    if (id == null) {
      throw new IllegalArgumentException("Profile ID cannot be null");
    }
    if (request == null) {
      throw new IllegalArgumentException("UpdateProfileRequest cannot be null");
    }

    LOG.debugf("Updating profile with ID: %s", id);
    Profile profile =
        profileRepository.findByIdOptional(id).orElseThrow(() -> new ProfileNotFoundException(id));

    // Update profile - EXACT COPY
    profileMapper.updateEntity(profile, request);
    // TODO: Get user from security context when auth is implemented
    profile.setUpdatedBy("system");
    profile.setUpdatedAt(LocalDateTime.now());

    profileRepository.persist(profile);

    LOG.infof("Profile updated successfully with ID: %s", id);
    return profileMapper.toResponse(profile);
  }

  /**
   * Delete a profile.
   * EXACT COPY from ProfileService.deleteProfile() lines 158-170
   * 
   * IMPORTANT: This is a HARD DELETE - no soft delete implemented!
   * TODO: Consider implementing soft delete for data recovery
   *
   * @param id the profile ID
   * @throws ProfileNotFoundException if not found
   */
  @Transactional
  public void deleteProfile(UUID id) {
    // Defensive validation - EXACT COPY
    if (id == null) {
      throw new IllegalArgumentException("Profile ID cannot be null");
    }

    LOG.debugf("Deleting profile with ID: %s", id);
    Profile profile =
        profileRepository.findByIdOptional(id).orElseThrow(() -> new ProfileNotFoundException(id));

    profileRepository.delete(profile);
    LOG.infof("Profile deleted successfully with ID: %s", id);
  }
}