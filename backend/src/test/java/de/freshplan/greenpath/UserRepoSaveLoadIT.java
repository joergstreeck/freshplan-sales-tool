package de.freshplan.greenpath;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.test.builders.UserTestDataFactory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Minimal repository test to verify basic save/load functionality. Part of the green-path strategy
 * to get CI working.
 */
@QuarkusTest
@Tag("core")
class UserRepoSaveLoadIT {

  @Inject UserRepository repo;

  @Test
  @Transactional
  void saveLoad() {
    var u =
        UserTestDataFactory.builder()
            .withUsername("testuser_" + System.currentTimeMillis())
            .withFirstName("Test")
            .withLastName("User")
            .withEmail("test" + System.currentTimeMillis() + "@example.com")
            .build();
    repo.persistAndFlush(u);
    assertThat(repo.findById(u.getId())).isNotNull();
  }
}
