package de.freshplan.domain.profile.repository;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.profile.entity.Profile;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales", "viewer"})
@TestTransaction
class ProfileRepositoryTest {

  @Inject ProfileRepository profileRepository;

  private Profile testProfile;

  @BeforeEach
  void setUp() {
    // Clear repository
    profileRepository.deleteAll();

    // Create test profile
    testProfile = new Profile();
    testProfile.setCustomerId("CUST-001");
    testProfile.setCreatedAt(LocalDateTime.now());
    testProfile.setUpdatedAt(LocalDateTime.now());
  }

  @Test
  void persist_shouldSaveProfileWithGeneratedId() {
    // When
    profileRepository.persist(testProfile);

    // Then
    assertThat(testProfile.getId()).isNotNull();
    assertThat(profileRepository.count()).isEqualTo(1);
  }

  @Test
  void findByCustomerId_withExistingCustomerId_shouldReturnProfile() {
    // Given
    profileRepository.persist(testProfile);

    // When
    Optional<Profile> found = profileRepository.findByCustomerId("CUST-001");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getCustomerId()).isEqualTo("CUST-001");
  }

  @Test
  void findByCustomerId_withNonExistingCustomerId_shouldReturnEmpty() {
    // Given
    profileRepository.persist(testProfile);

    // When
    Optional<Profile> found = profileRepository.findByCustomerId("CUST-999");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  void findByIdOptional_withExistingId_shouldReturnProfile() {
    // Given
    profileRepository.persist(testProfile);
    UUID profileId = testProfile.getId();

    // When
    Optional<Profile> found = profileRepository.findByIdOptional(profileId);

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(profileId);
  }

  @Test
  void update_shouldModifyExistingProfile() {
    // Given
    profileRepository.persist(testProfile);
    Profile saved = profileRepository.findByCustomerId("CUST-001").orElseThrow();

    // When
    saved.setCustomerId("CUST-002");
    profileRepository.persist(saved);

    // Then
    Optional<Profile> updated = profileRepository.findByCustomerId("CUST-002");
    assertThat(updated).isPresent();
    assertThat(updated.get().getCustomerId()).isEqualTo("CUST-002");
    assertThat(profileRepository.findByCustomerId("CUST-001")).isEmpty();
  }

  @Test
  void delete_shouldRemoveProfile() {
    // Given
    profileRepository.persist(testProfile);
    assertThat(profileRepository.count()).isEqualTo(1);

    // When
    profileRepository.delete(testProfile);

    // Then
    assertThat(profileRepository.count()).isEqualTo(0);
    assertThat(profileRepository.findByCustomerId("CUST-001")).isEmpty();
  }

  @Test
  void listAll_shouldReturnAllProfiles() {
    // Given
    Profile profile1 = new Profile();
    profile1.setCustomerId("CUST-001");
    profile1.setCreatedAt(LocalDateTime.now());
    profile1.setUpdatedAt(LocalDateTime.now());

    Profile profile2 = new Profile();
    profile2.setCustomerId("CUST-002");
    profile2.setCreatedAt(LocalDateTime.now());
    profile2.setUpdatedAt(LocalDateTime.now());

    profileRepository.persist(profile1);
    profileRepository.persist(profile2);

    // When
    var profiles = profileRepository.listAll();

    // Then
    assertThat(profiles).hasSize(2);
    assertThat(profiles)
        .extracting(Profile::getCustomerId)
        .containsExactlyInAnyOrder("CUST-001", "CUST-002");
  }

  @Test
  void count_shouldReturnNumberOfProfiles() {
    // Given
    assertThat(profileRepository.count()).isEqualTo(0);

    profileRepository.persist(testProfile);

    // When
    long count = profileRepository.count();

    // Then
    assertThat(count).isEqualTo(1);
  }
}
