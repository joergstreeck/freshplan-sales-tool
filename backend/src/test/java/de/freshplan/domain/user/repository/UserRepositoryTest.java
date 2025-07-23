package de.freshplan.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.user.entity.User;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for UserRepository.
 *
 * <p>Tests repository methods against real database using Testcontainers. Each test runs in its own
 * transaction for isolation.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
class UserRepositoryTest {

  @Inject UserRepository userRepository;
  @Inject de.freshplan.domain.opportunity.repository.OpportunityRepository opportunityRepository;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clear any existing data - opportunities first due to foreign key constraints
    opportunityRepository.deleteAll();
    opportunityRepository.flush();
    userRepository.deleteAll();
    userRepository.flush();
  }

  @Test
  @Transactional
  void testFindByUsername_ExistingUser_ShouldReturn() {
    // Given
    User user = createAndPersistUser("john.doe", "John", "Doe", "john.doe@freshplan.de");

    // When
    Optional<User> found = userRepository.findByUsername("john.doe");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getUsername()).isEqualTo("john.doe");
    assertThat(found.get().getEmail()).isEqualTo("john.doe@freshplan.de");
  }

  @Test
  @Transactional
  void testFindByUsername_NonExistingUser_ShouldReturnEmpty() {
    // When
    Optional<User> found = userRepository.findByUsername("nonexistent");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @Transactional
  void testFindByEmail_ExistingUser_ShouldReturn() {
    // Given
    User user = createAndPersistUser("john.doe", "John", "Doe", "john.doe@freshplan.de");

    // When
    Optional<User> found = userRepository.findByEmail("john.doe@freshplan.de");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getUsername()).isEqualTo("john.doe");
  }

  @Test
  @Transactional
  void testFindByEmail_NonExistingUser_ShouldReturnEmpty() {
    // When
    Optional<User> found = userRepository.findByEmail("nonexistent@freshplan.de");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  @Transactional
  void testExistsByUsername_ExistingUser_ShouldReturnTrue() {
    // Given
    createAndPersistUser("john.doe", "John", "Doe", "john.doe@freshplan.de");

    // When
    boolean exists = userRepository.existsByUsername("john.doe");

    // Then
    assertThat(exists).isTrue();
  }

  @Test
  @Transactional
  void testExistsByUsername_NonExistingUser_ShouldReturnFalse() {
    // When
    boolean exists = userRepository.existsByUsername("nonexistent");

    // Then
    assertThat(exists).isFalse();
  }

  @Test
  @Transactional
  void testExistsByEmail_ExistingUser_ShouldReturnTrue() {
    // Given
    createAndPersistUser("john.doe", "John", "Doe", "john.doe@freshplan.de");

    // When
    boolean exists = userRepository.existsByEmail("john.doe@freshplan.de");

    // Then
    assertThat(exists).isTrue();
  }

  @Test
  @Transactional
  void testExistsByEmail_NonExistingUser_ShouldReturnFalse() {
    // When
    boolean exists = userRepository.existsByEmail("nonexistent@freshplan.de");

    // Then
    assertThat(exists).isFalse();
  }

  @Test
  @Transactional
  void testExistsByUsernameAndIdNot_DifferentUser_ShouldReturnTrue() {
    // Given
    User firstUser = createAndPersistUser("john.doe", "John", "Doe", "john.doe@freshplan.de");
    User anotherUser =
        createAndPersistUser("jane.smith", "Jane", "Smith", "jane.smith@freshplan.de");

    // When
    boolean exists = userRepository.existsByUsernameAndIdNot("john.doe", anotherUser.getId());

    // Then
    assertThat(exists).isTrue();
  }

  @Test
  @Transactional
  void testExistsByUsernameAndIdNot_SameUser_ShouldReturnFalse() {
    // Given
    User user = createAndPersistUser("john.doe", "John", "Doe", "john.doe@freshplan.de");

    // When
    boolean exists = userRepository.existsByUsernameAndIdNot("john.doe", user.getId());

    // Then
    assertThat(exists).isFalse();
  }

  @Test
  @Transactional
  void testExistsByEmailAndIdNot_DifferentUser_ShouldReturnTrue() {
    // Given
    User firstUser = createAndPersistUser("john.doe", "John", "Doe", "john.doe@freshplan.de");
    User anotherUser =
        createAndPersistUser("jane.smith", "Jane", "Smith", "jane.smith@freshplan.de");

    // When
    boolean exists =
        userRepository.existsByEmailAndIdNot("john.doe@freshplan.de", anotherUser.getId());

    // Then
    assertThat(exists).isTrue();
  }

  @Test
  @Transactional
  void testExistsByEmailAndIdNot_SameUser_ShouldReturnFalse() {
    // Given
    User user = createAndPersistUser("john.doe", "John", "Doe", "john.doe@freshplan.de");

    // When
    boolean exists = userRepository.existsByEmailAndIdNot("john.doe@freshplan.de", user.getId());

    // Then
    assertThat(exists).isFalse();
  }

  @Test
  @Transactional
  void testFindAllActive_ShouldReturnOnlyEnabledUsers() {
    // Given
    User enabledUser1 = createAndPersistUser("john.doe", "John", "Doe", "john.doe@freshplan.de");

    User disabledUser =
        createAndPersistUser("disabled.user", "Disabled", "User", "disabled@freshplan.de");
    // Lade den User neu, um ihn zu ändern
    User userToDisable = userRepository.findById(disabledUser.getId());
    userToDisable.disable();
    userRepository.flush(); // Speichere die Änderung

    User enabledUser2 =
        createAndPersistUser("enabled.user", "Enabled", "User", "enabled@freshplan.de");

    // When
    List<User> activeUsers = userRepository.findAllActive();

    // Then
    assertThat(activeUsers).hasSize(2); // john.doe + enabled.user
    assertThat(activeUsers)
        .extracting(User::getUsername)
        .containsExactlyInAnyOrder("john.doe", "enabled.user");
  }

  @Test
  @Transactional
  void testPersistAndFlush_ShouldGenerateId() {
    // Given
    User newUser = new User("new.user", "New", "User", "new.user@freshplan.de");

    // When
    userRepository.persist(newUser);
    userRepository.flush();

    // Then
    assertThat(newUser.getId()).isNotNull();
    assertThat(newUser.getCreatedAt()).isNotNull();
    assertThat(newUser.getUpdatedAt()).isNotNull();
  }

  @Test
  @Transactional
  void testUpdate_ShouldUpdateTimestamp() throws InterruptedException {
    // Given
    User user = createAndPersistUser("john.doe", "John", "Doe", "john.doe@freshplan.de");
    var originalUpdatedAt = user.getUpdatedAt();
    Thread.sleep(100); // Nötig, um einen Zeitunterschied sicherzustellen

    // When
    user.setFirstName("Jonathan");
    userRepository.flush(); // Änderungen in die DB schreiben

    // Then
    // Lade den User erneut, um den von der DB aktualisierten Zeitstempel zu prüfen
    User updatedUser = userRepository.findById(user.getId());
    assertThat(updatedUser.getUpdatedAt()).isAfter(originalUpdatedAt);
  }

  @Test
  @Transactional
  void testDelete_ShouldRemoveUser() {
    // Given
    User user = createAndPersistUser("john.doe", "John", "Doe", "john.doe@freshplan.de");
    UUID userId = user.getId();
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
  @Transactional
  void testListAll_ShouldReturnAllUsers() {
    // Given
    createAndPersistUser("user1", "User", "One", "user1@freshplan.de");
    createAndPersistUser("user2", "User", "Two", "user2@freshplan.de");
    createAndPersistUser("user3", "User", "Three", "user3@freshplan.de");

    // When
    List<User> allUsers = userRepository.listAll();

    // Then
    assertThat(allUsers).hasSize(3); // 3 users
  }

  @Test
  @Transactional
  void testCount_ShouldReturnCorrectCount() {
    // Given
    createAndPersistUser("user1", "User", "One", "user1@freshplan.de");
    createAndPersistUser("user2", "User", "Two", "user2@freshplan.de");

    // When
    long count = userRepository.count();

    // Then
    assertThat(count).isEqualTo(2); // 2 users
  }

  // Helper methods

  private User createAndPersistUser(
      String username, String firstName, String lastName, String email) {
    User user = new User(username, firstName, lastName, email);
    userRepository.persist(user);
    userRepository.flush();

    // Verify the user was actually persisted with an ID
    if (user.getId() == null) {
      throw new IllegalStateException("User ID is null after persist and flush!");
    }

    return user;
  }
}
