package de.freshplan.domain.profile.service.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.freshplan.domain.profile.entity.Profile;
import de.freshplan.domain.profile.repository.ProfileRepository;
import de.freshplan.domain.profile.service.dto.CreateProfileRequest;
import de.freshplan.domain.profile.service.dto.ProfileResponse;
import de.freshplan.domain.profile.service.dto.UpdateProfileRequest;
import de.freshplan.domain.profile.service.exception.DuplicateProfileException;
import de.freshplan.domain.profile.service.exception.ProfileNotFoundException;
import de.freshplan.domain.profile.service.mapper.ProfileMapper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Unit tests for ProfileCommandService. Tests all write operations for profiles with CQRS pattern.
 */
@QuarkusTest
@Tag("migrate")
class ProfileCommandServiceTest {

  @Inject ProfileCommandService commandService;

  @InjectMock ProfileRepository profileRepository;

  @InjectMock ProfileMapper profileMapper;

  private Profile testProfile;
  private ProfileResponse testResponse;
  private CreateProfileRequest createRequest;
  private UpdateProfileRequest updateRequest;
  private UUID testId;

  @BeforeEach
  void setUp() {
    testId = UUID.randomUUID();

    // Setup test profile
    testProfile = new Profile();
    testProfile.setId(testId);
    testProfile.setCustomerId("CUST-001");
    testProfile.setCompanyInfo("Test Company");
    testProfile.setContactInfo("test@example.com");
    testProfile.setFinancialInfo("Revenue: 1M EUR");
    testProfile.setNotes("Test notes");
    testProfile.setCreatedBy("system");
    testProfile.setUpdatedBy("system");
    testProfile.setCreatedAt(LocalDateTime.now());
    testProfile.setUpdatedAt(LocalDateTime.now());

    // Setup test response using builder
    testResponse =
        ProfileResponse.builder().id(testId).customerId("CUST-001").notes("Test notes").build();

    // Setup create request with nested DTOs
    createRequest = new CreateProfileRequest();
    createRequest.setCustomerId("CUST-001");

    CreateProfileRequest.CompanyInfo companyInfo = new CreateProfileRequest.CompanyInfo();
    companyInfo.setName("Test Company");
    companyInfo.setIndustry("Technology");
    createRequest.setCompanyInfo(companyInfo);

    CreateProfileRequest.ContactInfo contactInfo = new CreateProfileRequest.ContactInfo();
    contactInfo.setEmail("test@example.com");
    contactInfo.setPhone("+49 123 456789");
    createRequest.setContactInfo(contactInfo);

    createRequest.setNotes("Test notes");

    // Setup update request with nested DTOs (uses CreateProfileRequest's nested classes)
    updateRequest = new UpdateProfileRequest();

    CreateProfileRequest.CompanyInfo updateCompanyInfo = new CreateProfileRequest.CompanyInfo();
    updateCompanyInfo.setName("Updated Company");
    updateCompanyInfo.setIndustry("Updated Industry");
    updateRequest.setCompanyInfo(updateCompanyInfo);

    CreateProfileRequest.ContactInfo updateContactInfo = new CreateProfileRequest.ContactInfo();
    updateContactInfo.setEmail("updated@example.com");
    updateContactInfo.setPhone("+49 987 654321");
    updateRequest.setContactInfo(updateContactInfo);
  }

  @Test
  void createProfile_withValidRequest_shouldCreateSuccessfully() {
    // Given
    when(profileRepository.existsByCustomerId("CUST-001")).thenReturn(false);
    when(profileMapper.toEntity(any(CreateProfileRequest.class))).thenReturn(testProfile);
    when(profileMapper.toResponse(any(Profile.class))).thenReturn(testResponse);

    // When
    ProfileResponse result = commandService.createProfile(createRequest);

    // Then
    assertNotNull(result);
    assertEquals("CUST-001", result.getCustomerId());

    // Verify profile was persisted
    ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
    verify(profileRepository).persist(profileCaptor.capture());

    Profile persistedProfile = profileCaptor.getValue();
    assertEquals("system", persistedProfile.getCreatedBy());
    assertEquals("system", persistedProfile.getUpdatedBy());
  }

  @Test
  void createProfile_withNullRequest_shouldThrowException() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> commandService.createProfile(null),
        "CreateProfileRequest cannot be null");
  }

  @Test
  void createProfile_withNullCustomerId_shouldThrowException() {
    // Given
    createRequest.setCustomerId(null);

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> commandService.createProfile(createRequest),
        "Customer ID cannot be null or empty");
  }

  @Test
  void createProfile_withEmptyCustomerId_shouldThrowException() {
    // Given
    createRequest.setCustomerId("  ");

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> commandService.createProfile(createRequest),
        "Customer ID cannot be null or empty");
  }

  @Test
  void createProfile_withDuplicateCustomerId_shouldThrowException() {
    // Given
    when(profileRepository.existsByCustomerId("CUST-001")).thenReturn(true);

    // When & Then
    assertThrows(
        DuplicateProfileException.class, () -> commandService.createProfile(createRequest));

    // Verify no persistence happened
    verify(profileRepository, never()).persist(any(Profile.class));
  }

  @Test
  void updateProfile_withValidRequest_shouldUpdateSuccessfully() {
    // Given
    when(profileRepository.findByIdOptional(testId)).thenReturn(Optional.of(testProfile));
    when(profileMapper.toResponse(any(Profile.class))).thenReturn(testResponse);

    // When
    ProfileResponse result = commandService.updateProfile(testId, updateRequest);

    // Then
    assertNotNull(result);

    // Verify mapper was called to update entity
    verify(profileMapper).updateEntity(testProfile, updateRequest);

    // Verify audit fields were updated
    ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
    verify(profileRepository).persist(profileCaptor.capture());

    Profile updatedProfile = profileCaptor.getValue();
    assertEquals("system", updatedProfile.getUpdatedBy());
    assertNotNull(updatedProfile.getUpdatedAt());
  }

  @Test
  void updateProfile_withNullId_shouldThrowException() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> commandService.updateProfile(null, updateRequest),
        "Profile ID cannot be null");
  }

  @Test
  void updateProfile_withNullRequest_shouldThrowException() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> commandService.updateProfile(testId, null),
        "UpdateProfileRequest cannot be null");
  }

  @Test
  void updateProfile_withNonExistentId_shouldThrowException() {
    // Given
    when(profileRepository.findByIdOptional(testId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(
        ProfileNotFoundException.class, () -> commandService.updateProfile(testId, updateRequest));

    // Verify no persistence happened
    verify(profileRepository, never()).persist(any(Profile.class));
  }

  @Test
  void deleteProfile_withValidId_shouldDeleteSuccessfully() {
    // Given
    when(profileRepository.findByIdOptional(testId)).thenReturn(Optional.of(testProfile));

    // When
    commandService.deleteProfile(testId);

    // Then
    verify(profileRepository).delete(testProfile);
  }

  @Test
  void deleteProfile_withNullId_shouldThrowException() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> commandService.deleteProfile(null),
        "Profile ID cannot be null");
  }

  @Test
  void deleteProfile_withNonExistentId_shouldThrowException() {
    // Given
    when(profileRepository.findByIdOptional(testId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ProfileNotFoundException.class, () -> commandService.deleteProfile(testId));

    // Verify no deletion happened
    verify(profileRepository, never()).delete(any(Profile.class));
  }

  @Test
  void verifyNoWriteOperationsInQueryContext() {
    // This test ensures CommandService only does write operations
    // All methods should result in persist or delete calls

    // Create operation
    when(profileRepository.existsByCustomerId("CUST-002")).thenReturn(false);
    when(profileMapper.toEntity(any(CreateProfileRequest.class))).thenReturn(testProfile);
    when(profileMapper.toResponse(any(Profile.class))).thenReturn(testResponse);

    createRequest.setCustomerId("CUST-002");
    commandService.createProfile(createRequest);
    verify(profileRepository, atLeastOnce()).persist(any(Profile.class));

    // Update operation
    when(profileRepository.findByIdOptional(testId)).thenReturn(Optional.of(testProfile));
    commandService.updateProfile(testId, updateRequest);
    verify(profileRepository, atLeast(2)).persist(any(Profile.class));

    // Delete operation
    when(profileRepository.findByIdOptional(testId)).thenReturn(Optional.of(testProfile));
    commandService.deleteProfile(testId);
    verify(profileRepository).delete(any(Profile.class));
  }
}
