package de.freshplan.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.user.entity.User;
import de.freshplan.test.builders.UserTestDataFactory;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for UserRepository - ONLY for custom queries.
 *
 * <p>Focuses exclusively on custom repository methods beyond basic Panache operations. Basic CRUD
 * operations (persist, delete, findAll, count) are guaranteed by Panache and not tested here.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@Tag("integration")
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
class UserRepositoryIT {

  @Inject UserRepository userRepository;
  @Inject de.freshplan.domain.opportunity.repository.OpportunityRepository opportunityRepository;
  @Inject EntityManager entityManager;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clear any existing test data in correct order (children first!)
    entityManager.createQuery("DELETE FROM OpportunityActivity").executeUpdate();
    opportunityRepository.deleteAll();
    opportunityRepository.flush();
    entityManager.createQuery("DELETE FROM User").executeUpdate();
    entityManager.flush();
  }

  // ==========================================
  // CUSTOM QUERY TESTS (Business Logic)
  // ==========================================

  @Test
  @Transactional
  void findByUsername_existingUser_shouldReturnUser() {
    User user = createAndPersistUser("john.doe", "John", "Doe", "john.doe@freshplan.de");
    String actualUsername = user.getUsername();

    Optional<User> found = userRepository.findByUsername(actualUsername);

    assertThat(found).isPresent();
    assertThat(found.get().getUsername()).isEqualTo(actualUsername);
  }

  @Test
  @Transactional
  void findByEmail_existingUser_shouldReturnUser() {
    User user = createAndPersistUser("john.doe", "John", "Doe", "john.doe@freshplan.de");
    String actualEmail = user.getEmail();

    Optional<User> found = userRepository.findByEmail(actualEmail);

    assertThat(found).isPresent();
    assertThat(found.get().getEmail()).isEqualTo(actualEmail);
  }

  @Test
  @Transactional
  void existsByUsernameAndIdNot_differentUser_shouldReturnTrue() {
    User firstUser = createAndPersistUser("john.doe", "John", "Doe", "john.doe@freshplan.de");
    User anotherUser =
        createAndPersistUser("jane.smith", "Jane", "Smith", "jane.smith@freshplan.de");

    boolean exists =
        userRepository.existsByUsernameAndIdNot(firstUser.getUsername(), anotherUser.getId());

    assertThat(exists).isTrue();
  }

  @Test
  @Transactional
  void existsByEmailAndIdNot_differentUser_shouldReturnTrue() {
    User firstUser = createAndPersistUser("john.doe", "John", "Doe", "john.doe@freshplan.de");
    User anotherUser =
        createAndPersistUser("jane.smith", "Jane", "Smith", "jane.smith@freshplan.de");

    boolean exists =
        userRepository.existsByEmailAndIdNot(firstUser.getEmail(), anotherUser.getId());

    assertThat(exists).isTrue();
  }

  @Test
  @Transactional
  void findAllActive_shouldReturnOnlyEnabledUsers() {
    User enabledUser1 = createAndPersistUser("john.doe", "John", "Doe", "john.doe@freshplan.de");

    User disabledUser =
        createAndPersistUser("disabled.user", "Disabled", "User", "disabled@freshplan.de");
    User userToDisable = userRepository.findById(disabledUser.getId());
    userToDisable.disable();
    userRepository.flush();

    User enabledUser2 =
        createAndPersistUser("enabled.user", "Enabled", "User", "enabled@freshplan.de");

    List<User> activeUsers = userRepository.findAllActive();

    assertThat(activeUsers).hasSize(2);
    assertThat(activeUsers)
        .extracting(User::getUsername)
        .containsExactlyInAnyOrder(enabledUser1.getUsername(), enabledUser2.getUsername());
  }

  // Helper methods

  private User createAndPersistUser(
      String username, String firstName, String lastName, String email) {
    User user =
        UserTestDataFactory.builder()
            .withUsername(username)
            .withFirstName(firstName)
            .withLastName(lastName)
            .withEmail(email)
            .build();
    userRepository.persist(user);
    userRepository.flush();

    if (user.getId() == null) {
      throw new IllegalStateException("User ID is null after persist and flush!");
    }

    return user;
  }
}
