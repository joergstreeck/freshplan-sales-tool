package de.freshplan.greenpath;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.domain.user.service.UserService;
import de.freshplan.domain.user.service.dto.CreateUserRequest;
import de.freshplan.domain.user.service.dto.UpdateUserRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Minimal service test to verify update functionality. Part of the green-path strategy to get CI
 * working.
 */
@QuarkusTest
@Tag("migrate")
class UserServiceUpdateIT {

  @Inject UserService svc;

  @Inject UserRepository repo;

  @Inject EntityManager em;

  @Test
  @Transactional
  void update() {
    var createReq =
        CreateUserRequest.builder()
            .username("user")
            .firstName("Test")
            .lastName("User")
            .email("user@test.com")
            .build();
    var created = svc.createUser(createReq);

    em.flush(); // erzwingt DB-Write (PrePersist ausgeführt)
    em.clear(); // trennt Entity vom Kontext

    var before = repo.findById(created.getId()).getUpdatedAt(); // frische Version

    var updateReq =
        UpdateUserRequest.builder()
            .username("user")
            .firstName("Test")
            .lastName("User")
            .email("user2@test.com")
            .enabled(true)
            .build();
    var updated = svc.updateUser(created.getId(), updateReq);

    em.flush(); // PreUpdate ausgeführt
    em.clear();

    var after = repo.findById(updated.getId()).getUpdatedAt();

    assertThat(after).isAfter(before);
  }
}
