package de.freshplan.domain.user.repository;

import de.freshplan.domain.user.entity.User;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserRepository.
 * 
 * Tests repository methods against real database using Testcontainers.
 * Each test runs in its own transaction for isolation.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@TestTransaction
class UserRepositoryTest {
    
    @Inject
    UserRepository userRepository;
    
    private User testUser;
    
    @BeforeEach
    @Transactional
    void setUp() {
        // Clear any existing data
        userRepository.deleteAll();
        
        // Create test user
        testUser = createAndPersistUser(
            "john.doe",
            "John",
            "Doe",
            "john.doe@freshplan.de"
        );
    }
    
    @Test
    void testFindByUsername_ExistingUser_ShouldReturn() {
        // When
        Optional<User> found = userRepository
                .findByUsername("john.doe");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername())
                .isEqualTo("john.doe");
        assertThat(found.get().getEmail())
                .isEqualTo("john.doe@freshplan.de");
    }
    
    @Test
    void testFindByUsername_NonExistingUser_ShouldReturnEmpty() {
        // When
        Optional<User> found = userRepository
                .findByUsername("nonexistent");
        
        // Then
        assertThat(found).isEmpty();
    }
    
    @Test
    void testFindByEmail_ExistingUser_ShouldReturn() {
        // When
        Optional<User> found = userRepository
                .findByEmail("john.doe@freshplan.de");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername())
                .isEqualTo("john.doe");
    }
    
    @Test
    void testFindByEmail_NonExistingUser_ShouldReturnEmpty() {
        // When
        Optional<User> found = userRepository
                .findByEmail("nonexistent@freshplan.de");
        
        // Then
        assertThat(found).isEmpty();
    }
    
    @Test
    void testExistsByUsername_ExistingUser_ShouldReturnTrue() {
        // When
        boolean exists = userRepository
                .existsByUsername("john.doe");
        
        // Then
        assertThat(exists).isTrue();
    }
    
    @Test
    void testExistsByUsername_NonExistingUser_ShouldReturnFalse() {
        // When
        boolean exists = userRepository
                .existsByUsername("nonexistent");
        
        // Then
        assertThat(exists).isFalse();
    }
    
    @Test
    void testExistsByEmail_ExistingUser_ShouldReturnTrue() {
        // When
        boolean exists = userRepository
                .existsByEmail("john.doe@freshplan.de");
        
        // Then
        assertThat(exists).isTrue();
    }
    
    @Test
    void testExistsByEmail_NonExistingUser_ShouldReturnFalse() {
        // When
        boolean exists = userRepository
                .existsByEmail("nonexistent@freshplan.de");
        
        // Then
        assertThat(exists).isFalse();
    }
    
    @Test
    void testExistsByUsernameAndIdNot_DifferentUser_ShouldReturnTrue() {
        // Given
        User anotherUser = createAndPersistUser(
            "jane.smith",
            "Jane",
            "Smith",
            "jane.smith@freshplan.de"
        );
        
        // When
        boolean exists = userRepository
                .existsByUsernameAndIdNot(
                    "john.doe", 
                    anotherUser.getId()
                );
        
        // Then
        assertThat(exists).isTrue();
    }
    
    @Test
    void testExistsByUsernameAndIdNot_SameUser_ShouldReturnFalse() {
        // When
        boolean exists = userRepository
                .existsByUsernameAndIdNot(
                    "john.doe", 
                    testUser.getId()
                );
        
        // Then
        assertThat(exists).isFalse();
    }
    
    @Test
    void testExistsByEmailAndIdNot_DifferentUser_ShouldReturnTrue() {
        // Given
        User anotherUser = createAndPersistUser(
            "jane.smith",
            "Jane",
            "Smith",
            "jane.smith@freshplan.de"
        );
        
        // When
        boolean exists = userRepository
                .existsByEmailAndIdNot(
                    "john.doe@freshplan.de", 
                    anotherUser.getId()
                );
        
        // Then
        assertThat(exists).isTrue();
    }
    
    @Test
    void testExistsByEmailAndIdNot_SameUser_ShouldReturnFalse() {
        // When
        boolean exists = userRepository
                .existsByEmailAndIdNot(
                    "john.doe@freshplan.de", 
                    testUser.getId()
                );
        
        // Then
        assertThat(exists).isFalse();
    }
    
    @Test
    void testFindAllActive_ShouldReturnOnlyEnabledUsers() {
        // Given
        User disabledUser = createAndPersistUser(
            "disabled.user",
            "Disabled",
            "User",
            "disabled@freshplan.de"
        );
        // Lade den User neu, um ihn zu ändern
        User userToDisable = userRepository.findById(disabledUser.getId());
        userToDisable.disable();
        userRepository.flush(); // Speichere die Änderung
        
        createAndPersistUser(
            "enabled.user",
            "Enabled",
            "User",
            "enabled@freshplan.de"
        );
        
        // When
        List<User> activeUsers = userRepository.findAllActive();
        
        // Then
        assertThat(activeUsers).hasSize(2); // testUser + enabledUser
        assertThat(activeUsers)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder(
                    "john.doe", 
                    "enabled.user"
                );
    }
    
    @Test
    void testPersistAndFlush_ShouldGenerateId() {
        // Given
        User newUser = new User(
            "new.user",
            "New",
            "User",
            "new.user@freshplan.de"
        );
        
        // When
        userRepository.persist(newUser);
        userRepository.flush();
        
        // Then
        assertThat(newUser.getId()).isNotNull();
        assertThat(newUser.getCreatedAt()).isNotNull();
        assertThat(newUser.getUpdatedAt()).isNotNull();
    }
    
    @Test
    void testUpdate_ShouldUpdateTimestamp() throws InterruptedException {
        // Given
        // Lade den User innerhalb des Tests neu, um sicherzustellen, dass er "managed" ist
        User userToUpdate = userRepository.findById(testUser.getId());
        var originalUpdatedAt = userToUpdate.getUpdatedAt();
        Thread.sleep(100); // Nötig, um einen Zeitunterschied sicherzustellen

        // When
        userToUpdate.setFirstName("Jonathan");
        userRepository.flush(); // Änderungen in die DB schreiben

        // Then
        // Lade den User erneut, um den von der DB aktualisierten Zeitstempel zu prüfen
        User updatedUser = userRepository.findById(testUser.getId());
        assertThat(updatedUser.getUpdatedAt()).isAfter(originalUpdatedAt);
    }
    
    @Test
    void testDelete_ShouldRemoveUser() {
        // Given
        UUID userId = testUser.getId();
        // Stelle sicher, dass der User existiert, bevor wir ihn löschen
        assertThat(userRepository.findByIdOptional(userId)).isPresent();
        
        // When
        // Verwende deleteById, um Probleme mit "detached" Objekten zu vermeiden
        userRepository.deleteById(userId);
        userRepository.flush();
        
        // Then
        Optional<User> found = userRepository.findByIdOptional(userId);
        assertThat(found).isEmpty();
    }
    
    @Test
    void testListAll_ShouldReturnAllUsers() {
        // Given
        createAndPersistUser(
            "user1",
            "User",
            "One",
            "user1@freshplan.de"
        );
        createAndPersistUser(
            "user2",
            "User",
            "Two",
            "user2@freshplan.de"
        );
        
        // When
        List<User> allUsers = userRepository.listAll();
        
        // Then
        assertThat(allUsers).hasSize(3); // testUser + 2 new users
    }
    
    @Test
    void testCount_ShouldReturnCorrectCount() {
        // Given
        createAndPersistUser(
            "extra.user",
            "Extra",
            "User",
            "extra@freshplan.de"
        );
        
        // When
        long count = userRepository.count();
        
        // Then
        assertThat(count).isEqualTo(2); // testUser + extra user
    }
    
    // Helper methods
    
    private User createAndPersistUser(
            String username,
            String firstName,
            String lastName,
            String email) {
        User user = new User(username, firstName, lastName, email);
        userRepository.persist(user);
        userRepository.flush();
        return user;
    }
}