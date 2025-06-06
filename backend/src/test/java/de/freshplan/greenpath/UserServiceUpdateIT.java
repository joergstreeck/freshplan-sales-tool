package de.freshplan.greenpath;

import de.freshplan.domain.user.service.UserService;
import de.freshplan.domain.user.service.dto.CreateUserRequest;
import de.freshplan.domain.user.service.dto.UpdateUserRequest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Minimal service test to verify update functionality.
 * Part of the green-path strategy to get CI working.
 */
@QuarkusTest
class UserServiceUpdateIT {
    
    @Inject
    UserService svc;
    
    @Test
    @Transactional
    void update() {
        var createReq = CreateUserRequest.builder()
                .username("user")
                .firstName("Test")
                .lastName("User")
                .email("user@test.com")
                .build();
        var created = svc.createUser(createReq);
        var before = created.getUpdatedAt();
        
        var updateReq = UpdateUserRequest.builder()
                .username("user")
                .firstName("Test")
                .lastName("User")
                .email("user2@test.com")
                .enabled(true)
                .build();
        var updated = svc.updateUser(created.getId(), updateReq);
        
        assertThat(updated.getUpdatedAt()).isAfter(before);
    }
}