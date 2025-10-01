package de.freshplan.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.freshplan.domain.profile.service.ProfileService;
import de.freshplan.domain.profile.service.dto.CreateProfileRequest;
import de.freshplan.domain.profile.service.dto.ProfileResponse;
import de.freshplan.domain.profile.service.dto.UpdateProfileRequest;
import de.freshplan.domain.profile.service.exception.DuplicateProfileException;
import de.freshplan.domain.profile.service.exception.ProfileNotFoundException;
import de.freshplan.testsupport.TestIds;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Mock-basierte Unit Tests für ProfileResource.
 *
 * <p>Sprint 2.1.4: Migriert von @QuarkusTest zu Mockito (~15s Ersparnis).
 *
 * @see TEST_DEBUGGING_GUIDE.md
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("ProfileResource Unit Tests")
class ProfileResourceMockTest {

  @Mock ProfileService profileService;

  @InjectMocks ProfileResource resource;

  private ProfileResponse testProfileResponse;
  private UUID profileId;
  private String customerId;

  @BeforeEach
  void setUp() {
    profileId = TestIds.uniqueUUID();
    customerId = TestIds.uniqueCustomerNumber();

    testProfileResponse =
        ProfileResponse.builder()
            .id(profileId)
            .customerId(customerId)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
  }

  // ===== POST /api/profiles Tests =====

  @Test
  @DisplayName("createProfile should return 201 with Location header")
  void createProfile_shouldReturn201() {
    // Given
    CreateProfileRequest request = CreateProfileRequest.builder().customerId(customerId).build();
    when(profileService.createProfile(any(CreateProfileRequest.class)))
        .thenReturn(testProfileResponse);

    // When
    Response response = resource.createProfile(request);

    // Then
    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getHeaderString("Location")).contains("/api/profiles/" + profileId);

    ProfileResponse body = (ProfileResponse) response.getEntity();
    assertThat(body.getId()).isEqualTo(profileId);
    assertThat(body.getCustomerId()).isEqualTo(customerId);

    verify(profileService, times(1)).createProfile(any(CreateProfileRequest.class));
  }

  // Note: Exception-Tests werden in Integration-Tests geprüft (JAX-RS ExceptionMapper)
  // Mock-Tests fokussieren auf Happy-Path + Service-Delegation

  // ===== GET /api/profiles/{id} Tests =====

  @Test
  @DisplayName("getProfile with existing ID should return 200")
  void getProfile_withExistingId_shouldReturn200() {
    // Given
    when(profileService.getProfile(profileId)).thenReturn(testProfileResponse);

    // When
    Response response = resource.getProfile(profileId);

    // Then
    assertThat(response.getStatus()).isEqualTo(200);

    ProfileResponse body = (ProfileResponse) response.getEntity();
    assertThat(body.getId()).isEqualTo(profileId);
    assertThat(body.getCustomerId()).isEqualTo(customerId);

    verify(profileService, times(1)).getProfile(profileId);
  }

  // Note: 404-Tests werden in Integration-Tests geprüft (JAX-RS ExceptionMapper)

  // ===== GET /api/profiles/customer/{customerId} Tests =====

  @Test
  @DisplayName("getProfileByCustomerId with existing customerId should return 200")
  void getProfileByCustomerId_withExistingCustomerId_shouldReturn200() {
    // Given
    when(profileService.getProfileByCustomerId(customerId)).thenReturn(testProfileResponse);

    // When
    Response response = resource.getProfileByCustomerId(customerId);

    // Then
    assertThat(response.getStatus()).isEqualTo(200);

    ProfileResponse body = (ProfileResponse) response.getEntity();
    assertThat(body.getCustomerId()).isEqualTo(customerId);

    verify(profileService, times(1)).getProfileByCustomerId(customerId);
  }

  // ===== PUT /api/profiles/{id} Tests =====

  @Test
  @DisplayName("updateProfile with existing profile should return 200")
  void updateProfile_withExistingProfile_shouldReturn200() {
    // Given
    UpdateProfileRequest updateRequest = UpdateProfileRequest.builder().build();
    when(profileService.updateProfile(eq(profileId), any(UpdateProfileRequest.class)))
        .thenReturn(testProfileResponse);

    // When
    Response response = resource.updateProfile(profileId, updateRequest);

    // Then
    assertThat(response.getStatus()).isEqualTo(200);

    ProfileResponse body = (ProfileResponse) response.getEntity();
    assertThat(body.getId()).isEqualTo(profileId);

    verify(profileService, times(1)).updateProfile(eq(profileId), any(UpdateProfileRequest.class));
  }

  // ===== DELETE /api/profiles/{id} Tests =====

  @Test
  @DisplayName("deleteProfile with existing profile should return 204")
  void deleteProfile_withExistingProfile_shouldReturn204() {
    // Given
    doNothing().when(profileService).deleteProfile(profileId);

    // When
    Response response = resource.deleteProfile(profileId);

    // Then
    assertThat(response.getStatus()).isEqualTo(204);

    verify(profileService, times(1)).deleteProfile(profileId);
  }

  // ===== GET /api/profiles Tests =====

  @Test
  @DisplayName("getAllProfiles should return 200 with list")
  void getAllProfiles_shouldReturn200WithList() {
    // Given
    when(profileService.getAllProfiles()).thenReturn(List.of(testProfileResponse));

    // When
    Response response = resource.getAllProfiles();

    // Then
    assertThat(response.getStatus()).isEqualTo(200);

    @SuppressWarnings("unchecked")
    List<ProfileResponse> body = (List<ProfileResponse>) response.getEntity();
    assertThat(body).hasSize(1);
    assertThat(body.get(0).getCustomerId()).isEqualTo(customerId);

    verify(profileService, times(1)).getAllProfiles();
  }

  // ===== HEAD /api/profiles/customer/{customerId} Tests =====

  @Test
  @DisplayName("checkProfileExists with existing customerId should return 200")
  void checkProfileExists_withExistingCustomerId_shouldReturn200() {
    // Given
    when(profileService.profileExists(customerId)).thenReturn(true);

    // When
    Response response = resource.checkProfileExists(customerId);

    // Then
    assertThat(response.getStatus()).isEqualTo(200);

    verify(profileService, times(1)).profileExists(customerId);
  }

  @Test
  @DisplayName("checkProfileExists with non-existing customerId should return 404")
  void checkProfileExists_withNonExistingCustomerId_shouldReturn404() {
    // Given
    when(profileService.profileExists(customerId)).thenReturn(false);

    // When
    Response response = resource.checkProfileExists(customerId);

    // Then
    assertThat(response.getStatus()).isEqualTo(404);

    verify(profileService, times(1)).profileExists(customerId);
  }

  // ===== GET /api/profiles/{id}/export/pdf Tests =====

  @Test
  @DisplayName("exportProfilePdf with existing profile should return PDF")
  void exportProfilePdf_withExistingProfile_shouldReturnPdf() {
    // Given
    byte[] pdfContent = "PDF-CONTENT".getBytes();
    when(profileService.exportProfileAsPdf(profileId)).thenReturn(pdfContent);

    // When
    Response response = resource.exportProfilePdf(profileId);

    // Then
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getHeaderString("Content-Disposition"))
        .contains("attachment; filename=\"profile-" + profileId + ".pdf\"");
    assertThat(response.getEntity()).isEqualTo(pdfContent);

    verify(profileService, times(1)).exportProfileAsPdf(profileId);
  }
}
