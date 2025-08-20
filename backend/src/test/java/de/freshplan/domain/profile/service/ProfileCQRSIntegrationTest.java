package de.freshplan.domain.profile.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.profile.entity.Profile;
import de.freshplan.domain.profile.repository.ProfileRepository;
import de.freshplan.domain.profile.service.dto.*;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
/**
 * Integration Test for ProfileService CQRS Implementation.
 *
 * <p>Tests the CQRS-separated ProfileCommandService and ProfileQueryService with Feature Flag
 * enabled to ensure proper delegation and functionality.
 *
 * <p>Key aspects tested: - Profile creation and updates via CommandService - Profile retrieval and
 * search via QueryService - Dashboard customization - Profile deletion
 *
 * @author Claude
 * @since Phase 14.3 - Feature Flag Switching Tests
 */
@QuarkusTest
@Tag("migrate")@TestProfile(ProfileCQRSTestProfile.class)
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
@DisplayName("Profile Service CQRS Integration Test")
class ProfileCQRSIntegrationTest {

  @Inject ProfileService profileService; // Test via Facade to verify Feature Flag switching

  @Inject ProfileRepository profileRepository;

  @ConfigProperty(name = "features.cqrs.enabled")
  boolean cqrsEnabled;

  private String testCustomerId;
  private String uniqueSuffix;

  @BeforeEach
  @TestTransaction
  void setUp() {
    uniqueSuffix = String.valueOf(System.currentTimeMillis());
    testCustomerId = "CUST-" + uniqueSuffix;

    // Clean up any existing test data
    profileRepository.deleteAll();
    profileRepository.flush();
  }

  @Test
  @DisplayName("Feature Flag should be enabled for CQRS tests")
  void testCQRSModeIsEnabled() {
    assertThat(cqrsEnabled).as("CQRS Feature Flag must be enabled for this test").isTrue();
  }

  // =====================================
  // COMMAND OPERATIONS (Write)
  // =====================================

  @Test
  @TestTransaction
  @DisplayName("Create profile should delegate to CommandService")
  void createProfile_inCQRSMode_shouldCreateSuccessfully() {
    // Given
    CreateProfileRequest.CompanyInfo companyInfo = new CreateProfileRequest.CompanyInfo();
    companyInfo.setName("Test Company " + uniqueSuffix);
    companyInfo.setIndustry("Technology");
    companyInfo.setRegistrationNumber("REG-123");
    companyInfo.setVatNumber("VAT-456");

    CreateProfileRequest.ContactInfo contactInfo = new CreateProfileRequest.ContactInfo();
    contactInfo.setPrimaryContact("John Doe");
    contactInfo.setEmail("john@test.com");
    contactInfo.setPhone("+49 123 456789");
    contactInfo.setAddress("Test Street 1, 12345 Berlin");

    CreateProfileRequest.FinancialInfo financialInfo = new CreateProfileRequest.FinancialInfo();
    financialInfo.setPaymentTerms("Net 30");
    financialInfo.setCreditLimit("100000");
    financialInfo.setTaxExempt("false");

    CreateProfileRequest request =
        CreateProfileRequest.builder()
            .customerId(testCustomerId)
            .companyInfo(companyInfo)
            .contactInfo(contactInfo)
            .financialInfo(financialInfo)
            .notes("Test profile notes")
            .build();

    // When
    ProfileResponse response = profileService.createProfile(request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getId()).isNotNull();
    assertThat(response.getCustomerId()).isEqualTo(testCustomerId);
    assertThat(response.getCompanyInfo()).isNotNull();
    assertThat(response.getCompanyInfo().getName()).isEqualTo("Test Company " + uniqueSuffix);
    assertThat(response.getContactInfo()).isNotNull();
    assertThat(response.getContactInfo().getPrimaryContact()).isEqualTo("John Doe");
    assertThat(response.getNotes()).isEqualTo("Test profile notes");

    // Verify in database
    Profile saved = profileRepository.findById(response.getId());
    assertThat(saved).isNotNull();
    assertThat(saved.getCustomerId()).isEqualTo(testCustomerId);
  }

  @Test
  @TestTransaction
  @DisplayName("Update profile should delegate to CommandService")
  void updateProfile_inCQRSMode_shouldUpdateSuccessfully() {
    // Given - Create a profile first
    CreateProfileRequest createRequest =
        CreateProfileRequest.builder().customerId(testCustomerId).notes("Original notes").build();

    ProfileResponse created = profileService.createProfile(createRequest);
    UUID profileId = created.getId();

    // Prepare update
    CreateProfileRequest.CompanyInfo updatedCompanyInfo = new CreateProfileRequest.CompanyInfo();
    updatedCompanyInfo.setName("Updated Company");
    updatedCompanyInfo.setIndustry("Finance");

    UpdateProfileRequest updateRequest =
        UpdateProfileRequest.builder()
            .companyInfo(updatedCompanyInfo)
            .notes("Updated notes")
            .build();

    // When
    ProfileResponse updated = profileService.updateProfile(profileId, updateRequest);

    // Then
    assertThat(updated).isNotNull();
    assertThat(updated.getId()).isEqualTo(profileId);
    assertThat(updated.getCompanyInfo()).isNotNull();
    assertThat(updated.getCompanyInfo().getName()).isEqualTo("Updated Company");
    assertThat(updated.getNotes()).isEqualTo("Updated notes");
  }

  @Test
  @TestTransaction
  @DisplayName("Delete profile should delegate to CommandService")
  void deleteProfile_inCQRSMode_shouldDeleteSuccessfully() {
    // Given - Create a profile first
    CreateProfileRequest createRequest =
        CreateProfileRequest.builder().customerId(testCustomerId).notes("To be deleted").build();

    ProfileResponse created = profileService.createProfile(createRequest);
    UUID profileId = created.getId();

    // When
    profileService.deleteProfile(profileId);

    // Then - Should not find the profile
    Profile deleted = profileRepository.findById(profileId);
    assertThat(deleted).isNull();
  }

  // =====================================
  // QUERY OPERATIONS (Read)
  // =====================================

  @Test
  @TestTransaction
  @DisplayName("Get profile by ID should delegate to QueryService")
  void getProfile_inCQRSMode_shouldReturnProfile() {
    // Given - Create a profile first
    CreateProfileRequest createRequest =
        CreateProfileRequest.builder()
            .customerId(testCustomerId)
            .notes("Test profile for retrieval")
            .build();

    ProfileResponse created = profileService.createProfile(createRequest);
    UUID profileId = created.getId();

    // When
    ProfileResponse retrieved = profileService.getProfile(profileId);

    // Then
    assertThat(retrieved).isNotNull();
    assertThat(retrieved.getId()).isEqualTo(profileId);
    assertThat(retrieved.getCustomerId()).isEqualTo(testCustomerId);
    assertThat(retrieved.getNotes()).isEqualTo("Test profile for retrieval");
  }

  @Test
  @TestTransaction
  @DisplayName("Get profile by customer ID should delegate to QueryService")
  void getProfileByCustomerId_inCQRSMode_shouldReturnProfile() {
    // Given - Create a profile first
    CreateProfileRequest createRequest =
        CreateProfileRequest.builder()
            .customerId(testCustomerId)
            .notes("Test by customer ID")
            .build();

    ProfileResponse created = profileService.createProfile(createRequest);

    // When
    ProfileResponse retrieved = profileService.getProfileByCustomerId(testCustomerId);

    // Then
    assertThat(retrieved).isNotNull();
    assertThat(retrieved.getId()).isEqualTo(created.getId());
    assertThat(retrieved.getCustomerId()).isEqualTo(testCustomerId);
    assertThat(retrieved.getNotes()).isEqualTo("Test by customer ID");
  }

  @Test
  @TestTransaction
  @DisplayName("Get all profiles should delegate to QueryService")
  void getAllProfiles_inCQRSMode_shouldReturnList() {
    // Given - Create multiple profiles
    for (int i = 1; i <= 3; i++) {
      CreateProfileRequest request =
          CreateProfileRequest.builder()
              .customerId("CUST-" + uniqueSuffix + "-" + i)
              .notes("Profile " + i)
              .build();
      profileService.createProfile(request);
    }

    // When
    List<ProfileResponse> profiles = profileService.getAllProfiles();

    // Then - filter for our unique profiles to avoid test isolation issues
    List<ProfileResponse> ourProfiles =
        profiles.stream()
            .filter(p -> p.getCustomerId().startsWith("CUST-" + uniqueSuffix))
            .toList();

    assertThat(ourProfiles).isNotNull().hasSize(3);

    assertThat(ourProfiles)
        .extracting(ProfileResponse::getCustomerId)
        .containsExactlyInAnyOrder(
            "CUST-" + uniqueSuffix + "-1",
            "CUST-" + uniqueSuffix + "-2",
            "CUST-" + uniqueSuffix + "-3");
  }

  // =====================================
  // ERROR HANDLING
  // =====================================

  @Test
  @DisplayName("Create duplicate profile should fail")
  void createDuplicateProfile_shouldThrowException() {
    // Given - Create first profile
    CreateProfileRequest request =
        CreateProfileRequest.builder().customerId(testCustomerId).notes("First profile").build();

    profileService.createProfile(request);

    // When/Then - Try to create duplicate
    CreateProfileRequest duplicateRequest =
        CreateProfileRequest.builder()
            .customerId(testCustomerId)
            .notes("Duplicate profile")
            .build();

    assertThatThrownBy(() -> profileService.createProfile(duplicateRequest))
        .hasMessageContaining("already exists");
  }

  @Test
  @DisplayName("Get non-existent profile should throw exception")
  void getNonExistentProfile_shouldThrowException() {
    // Given
    UUID nonExistentId = UUID.randomUUID();

    // When/Then
    assertThatThrownBy(() -> profileService.getProfile(nonExistentId))
        .hasMessageContaining("not found");
  }

  @Test
  @DisplayName("Update non-existent profile should throw exception")
  void updateNonExistentProfile_shouldThrowException() {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    UpdateProfileRequest request = UpdateProfileRequest.builder().notes("Should fail").build();

    // When/Then
    assertThatThrownBy(() -> profileService.updateProfile(nonExistentId, request))
        .hasMessageContaining("not found");
  }

  // =====================================
  // CQRS BEHAVIOR VERIFICATION
  // =====================================

  @Test
  @TestTransaction
  @DisplayName("CQRS mode should properly delegate all operations")
  void cqrsMode_shouldProperlyDelegateAllOperations() {
    // This test verifies that with the feature flag enabled,
    // all operations are properly delegated to Command/Query services

    // 1. Create (Command)
    CreateProfileRequest createRequest =
        CreateProfileRequest.builder()
            .customerId(testCustomerId)
            .notes("CQRS test profile")
            .build();

    ProfileResponse created = profileService.createProfile(createRequest);
    assertThat(created).isNotNull();

    // 2. Read by ID (Query)
    ProfileResponse retrieved = profileService.getProfile(created.getId());
    assertThat(retrieved.getCustomerId()).isEqualTo(testCustomerId);

    // 3. Read by Customer ID (Query)
    ProfileResponse byCustomerId = profileService.getProfileByCustomerId(testCustomerId);
    assertThat(byCustomerId.getId()).isEqualTo(created.getId());

    // 4. Update (Command)
    UpdateProfileRequest updateRequest =
        UpdateProfileRequest.builder().notes("Updated via CQRS").build();

    ProfileResponse updated = profileService.updateProfile(created.getId(), updateRequest);
    assertThat(updated.getNotes()).isEqualTo("Updated via CQRS");

    // 5. List All (Query) - filter for our unique profile to avoid test isolation issues
    List<ProfileResponse> all = profileService.getAllProfiles();
    List<ProfileResponse> ourProfiles =
        all.stream().filter(p -> p.getCustomerId().equals(testCustomerId)).toList();
    assertThat(ourProfiles).hasSize(1);

    // 6. Delete (Command)
    profileService.deleteProfile(created.getId());

    // 7. Verify deletion (Query should fail)
    assertThatThrownBy(() -> profileService.getProfile(created.getId()))
        .hasMessageContaining("not found");
  }
}
