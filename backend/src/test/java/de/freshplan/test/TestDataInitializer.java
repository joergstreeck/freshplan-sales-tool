package de.freshplan.test;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.test.builders.UserTestDataFactory;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

  // DISABLED: In SEED-free setup, tests must create their own data
  // @Transactional
  // void onStart(@Observes StartupEvent ev) {
  //   LOG.info("DISABLED: TestDataInitializer - Tests must use TestDataFactory");
  // }

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
