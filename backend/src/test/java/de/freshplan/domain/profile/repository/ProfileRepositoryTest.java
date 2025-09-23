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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("migrate")
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
class ProfileRepositoryTest {

  @Inject ProfileRepository profileRepository;

  private long initialProfileCount;

  private Profile createTestProfile(String customerId) {
    Profile profile = new Profile();
    profile.setCustomerId(customerId);
    profile.setCreatedAt(LocalDateTime.now());
    profile.setUpdatedAt(LocalDateTime.now());
    return profile;
  }

  @Test
  @TestTransaction
  void persist_shouldSaveProfileWithGeneratedId() {
    // Given
    long initialCount = profileRepository.count();
    Profile testProfile = createTestProfile("CUST-001");

    // When
    profileRepository.persist(testProfile);

    // Then
    assertThat(testProfile.getId()).isNotNull();
    assertThat(profileRepository.count()).isEqualTo(initialCount + 1);
  }

  @Test
  @TestTransaction
  void findByCustomerId_withExistingCustomerId_shouldReturnProfile() {
    // Given
    Profile testProfile = createTestProfile("CUST-001");
    profileRepository.persist(testProfile);

    // When
    Optional<Profile> found = profileRepository.findByCustomerId("CUST-001");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getCustomerId()).isEqualTo("CUST-001");
  }

  @Test
  @TestTransaction
  void findByCustomerId_withNonExistingCustomerId_shouldReturnEmpty() {
    // Given
    Profile testProfile = createTestProfile("CUST-001");
    profileRepository.persist(testProfile);

    // When
    Optional<Profile> found = profileRepository.findByCustomerId("CUST-999");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @TestTransaction
  void findByIdOptional_withExistingId_shouldReturnProfile() {
    // Given
    Profile testProfile = createTestProfile("CUST-001");
    profileRepository.persist(testProfile);
    UUID profileId = testProfile.getId();

    // When
    Optional<Profile> found = profileRepository.findByIdOptional(profileId);

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(profileId);
  }

  @Test
  @TestTransaction
  void update_shouldModifyExistingProfile() {
    // Given
    Profile testProfile = createTestProfile("CUST-001");
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
  @TestTransaction
  void delete_shouldRemoveProfile() {
    // Given
    long initialCount = profileRepository.count();
    Profile testProfile = createTestProfile("CUST-001");
    profileRepository.persist(testProfile);
    assertThat(profileRepository.count()).isEqualTo(initialCount + 1);

    // When
    profileRepository.delete(testProfile);

    // Then
    assertThat(profileRepository.count()).isEqualTo(initialCount);
    assertThat(profileRepository.findByCustomerId("CUST-001")).isEmpty();
  }

  @Test
  @TestTransaction
  void listAll_shouldReturnAllProfiles() {
    // Given
    int initialSize = profileRepository.listAll().size();

    Profile profile1 = new Profile();
    profile1.setCustomerId("CUST-TEST-001");
    profile1.setCreatedAt(LocalDateTime.now());
    profile1.setUpdatedAt(LocalDateTime.now());

    Profile profile2 = new Profile();
    profile2.setCustomerId("CUST-TEST-002");
    profile2.setCreatedAt(LocalDateTime.now());
    profile2.setUpdatedAt(LocalDateTime.now());

    profileRepository.persist(profile1);
    profileRepository.persist(profile2);

    // When
    var profiles = profileRepository.listAll();

    // Then
    assertThat(profiles).hasSize(initialSize + 2);
    assertThat(profiles)
        .extracting(Profile::getCustomerId)
        .contains("CUST-TEST-001", "CUST-TEST-002");
  }

  @Test
  @TestTransaction
  void count_shouldReturnNumberOfProfiles() {
    // Given
    long initialCount = profileRepository.count();

    Profile testProfile = createTestProfile("CUST-001");
    profileRepository.persist(testProfile);

    // When
    long count = profileRepository.count();

    // Then
    assertThat(count).isEqualTo(initialCount + 1);
  }
}
