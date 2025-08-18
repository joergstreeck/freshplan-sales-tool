package de.freshplan.test;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.test.builders.UserTestDataFactory;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Arrays;
import org.jboss.logging.Logger;

/**
 * Test data initializer that creates test users for integration tests. Active only in test profile.
 */
@ApplicationScoped
@IfBuildProfile("test")
public class TestDataInitializer {

  private static final Logger LOG = Logger.getLogger(TestDataInitializer.class);

  @Inject UserRepository userRepository;

  @Transactional
  void onStart(@Observes StartupEvent ev) {
    LOG.info("Initializing test data...");

    // Check if we already have users
    if (userRepository.count() > 0) {
      LOG.info("Test users already exist, skipping initialization");
      return;
    }

    // Create test users for tests
    createUser("testuser", "Test", "User", "testuser@test.com", "admin", "manager", "sales");
    createUser("admin", "Admin", "User", "admin@test.com", "admin");
    createUser("manager", "Manager", "User", "manager@test.com", "manager");
    createUser("sales", "Sales", "User", "sales@test.com", "sales");

    LOG.info("Test data initialized successfully");
  }

  private void createUser(
      String username, String firstName, String lastName, String email, String... roles) {
    User user =
        UserTestDataFactory.builder()
            .withUsername(username)
            .withFirstName(firstName)
            .withLastName(lastName)
            .withEmail(email)
            .build();
    user.setRoles(Arrays.asList(roles));
    userRepository.persist(user);
    LOG.infof("Created test user: %s with roles: %s", username, String.join(", ", roles));
  }
}
