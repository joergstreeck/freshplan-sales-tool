package de.freshplan.domain.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import de.freshplan.domain.profile.entity.Profile;
import de.freshplan.domain.profile.repository.ProfileRepository;
import de.freshplan.domain.profile.service.dto.*;
import de.freshplan.domain.profile.service.exception.DuplicateProfileException;
import de.freshplan.domain.profile.service.exception.ProfileNotFoundException;
import de.freshplan.domain.profile.service.mapper.ProfileMapper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("migrate")
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
class ProfileServiceTest {

  @Inject ProfileService profileService;

  @InjectMock ProfileRepository profileRepository;

  @InjectMock ProfileMapper profileMapper;

  private Profile testProfile;
  private ProfileResponse testProfileResponse;
  private CreateProfileRequest createRequest;
  private UpdateProfileRequest updateRequest;

  @BeforeEach
  void setUp() {
    // Test Profile Entity
    testProfile = new Profile();
    testProfile.setId(UUID.randomUUID());
    testProfile.setCustomerId("CUST-001");
    testProfile.setCreatedAt(LocalDateTime.now());
    testProfile.setUpdatedAt(LocalDateTime.now());

    // Test Profile Response
    testProfileResponse =
        ProfileResponse.builder()
            .id(testProfile.getId())
            .customerId(testProfile.getCustomerId())
            .createdAt(testProfile.getCreatedAt())
            .updatedAt(testProfile.getUpdatedAt())
            .build();

    // Create Request
    createRequest = CreateProfileRequest.builder().customerId("CUST-001").build();

    // Update Request
    updateRequest = UpdateProfileRequest.builder().build();
  }

  @Test
  void createProfile_withValidData_shouldReturnCreatedProfile() {
    // Given
    when(profileRepository.existsByCustomerId(anyString())).thenReturn(false);
    when(profileMapper.toEntity(any(CreateProfileRequest.class))).thenReturn(testProfile);
    when(profileMapper.toResponse(any(Profile.class))).thenReturn(testProfileResponse);

    // When
    ProfileResponse result = profileService.createProfile(createRequest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getCustomerId()).isEqualTo("CUST-001");

    verify(profileRepository).persist(any(Profile.class));
    verify(profileRepository).existsByCustomerId("CUST-001");
  }

  @Test
  void createProfile_withDuplicateCustomerId_shouldThrowException() {
    // Given
    when(profileRepository.existsByCustomerId(anyString())).thenReturn(true);

    // When/Then
    assertThatThrownBy(() -> profileService.createProfile(createRequest))
        .isInstanceOf(DuplicateProfileException.class)
        .hasMessageContaining("CUST-001");

    verify(profileRepository, never()).persist(any(Profile.class));
  }

  @Test
  void getProfile_withExistingId_shouldReturnProfile() {
    // Given
    UUID profileId = testProfile.getId();
    when(profileRepository.findByIdOptional(profileId)).thenReturn(Optional.of(testProfile));
    when(profileMapper.toResponse(testProfile)).thenReturn(testProfileResponse);

    // When
    ProfileResponse result = profileService.getProfile(profileId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(profileId);
  }

  @Test
  void getProfile_withNonExistingId_shouldThrowException() {
    // Given
    UUID profileId = UUID.randomUUID();
    when(profileRepository.findByIdOptional(profileId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> profileService.getProfile(profileId))
        .isInstanceOf(ProfileNotFoundException.class)
        .hasMessageContaining(profileId.toString());
  }

  @Test
  void getProfileByCustomerId_withExistingCustomerId_shouldReturnProfile() {
    // Given
    when(profileRepository.findByCustomerId("CUST-001")).thenReturn(Optional.of(testProfile));
    when(profileMapper.toResponse(testProfile)).thenReturn(testProfileResponse);

    // When
    ProfileResponse result = profileService.getProfileByCustomerId("CUST-001");

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getCustomerId()).isEqualTo("CUST-001");
  }

  @Test
  void updateProfile_withExistingProfile_shouldReturnUpdatedProfile() {
    // Given
    UUID profileId = testProfile.getId();
    when(profileRepository.findByIdOptional(profileId)).thenReturn(Optional.of(testProfile));
    when(profileMapper.toResponse(any(Profile.class))).thenReturn(testProfileResponse);

    // When
    ProfileResponse result = profileService.updateProfile(profileId, updateRequest);

    // Then
    assertThat(result).isNotNull();
    verify(profileMapper).updateEntity(testProfile, updateRequest);
    verify(profileRepository).persist(testProfile);
  }

  @Test
  void deleteProfile_withExistingProfile_shouldDeleteSuccessfully() {
    // Given
    UUID profileId = testProfile.getId();
    when(profileRepository.findByIdOptional(profileId)).thenReturn(Optional.of(testProfile));

    // When
    profileService.deleteProfile(profileId);

    // Then
    verify(profileRepository).delete(testProfile);
  }

  @Test
  void getAllProfiles_shouldReturnAllProfiles() {
    // Given
    List<Profile> profiles = List.of(testProfile);
    when(profileRepository.listAll()).thenReturn(profiles);
    when(profileMapper.toResponse(any(Profile.class))).thenReturn(testProfileResponse);

    // When
    List<ProfileResponse> result = profileService.getAllProfiles();

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getCustomerId()).isEqualTo("CUST-001");
  }

  @Test
  void profileExists_withExistingCustomerId_shouldReturnTrue() {
    // Given
    when(profileRepository.existsByCustomerId("CUST-001")).thenReturn(true);

    // When
    boolean exists = profileService.profileExists("CUST-001");

    // Then
    assertThat(exists).isTrue();
  }

  @Test
  void profileExists_withNonExistingCustomerId_shouldReturnFalse() {
    // Given
    when(profileRepository.existsByCustomerId("CUST-999")).thenReturn(false);

    // When
    boolean exists = profileService.profileExists("CUST-999");

    // Then
    assertThat(exists).isFalse();
  }
}
