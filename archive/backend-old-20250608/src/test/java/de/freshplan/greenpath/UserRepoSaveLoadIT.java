package de.freshplan.greenpath;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Minimal repository test to verify basic save/load functionality.
 * Part of the green-path strategy to get CI working.
 */
@QuarkusTest
class UserRepoSaveLoadIT {
    
    @Inject
    UserRepository repo;
    
    @Test
    @Transactional
    void saveLoad() {
        var u = new User("testuser", "Test", "User", "test@example.com");
        repo.persistAndFlush(u);
        assertThat(repo.findById(u.getId())).isNotNull();
    }
}