package de.freshplan.domain.profile.repository;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.profile.entity.Profile;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for ProfileRepository - ONLY for custom queries.
 *
 * <p>Tests custom repository methods beyond basic Panache operations. Basic CRUD (persist, delete,
 * findAll, count) are guaranteed by Panache and not tested.
 */
@QuarkusTest
@Tag("integration")
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
class ProfileRepositoryIT {

  @Inject ProfileRepository profileRepository;

  private Profile createTestProfile(String customerId) {
    Profile profile = new Profile();
    profile.setCustomerId(customerId);
    profile.setCreatedAt(LocalDateTime.now());
    profile.setUpdatedAt(LocalDateTime.now());
    return profile;
  }

  @Test
  @TestTransaction
  void findByCustomerId_withExistingCustomerId_shouldReturnProfile() {
    Profile testProfile = createTestProfile("CUST-001-IT");
    profileRepository.persist(testProfile);

    Optional<Profile> found = profileRepository.findByCustomerId("CUST-001-IT");

    assertThat(found).isPresent();
    assertThat(found.get().getCustomerId()).isEqualTo("CUST-001-IT");
  }

  @Test
  @TestTransaction
  void findByCustomerId_withNonExistingCustomerId_shouldReturnEmpty() {
    Profile testProfile = createTestProfile("CUST-002-IT");
    profileRepository.persist(testProfile);

    Optional<Profile> found = profileRepository.findByCustomerId("CUST-999-IT");

    assertThat(found).isEmpty();
  }
}
