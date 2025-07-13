package de.freshplan.api;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Arrays;
import org.jboss.logging.Logger;

/** Development data initializer that creates test users. Only active in dev profile. */
@ApplicationScoped
@IfBuildProfile("dev")
public class DevDataInitializer {

  private static final Logger LOG = Logger.getLogger(DevDataInitializer.class);

  @Inject UserRepository userRepository;

  @Transactional
  void onStart(@Observes StartupEvent ev) {
    LOG.info("Initializing development data...");

    // Check if we already have users
    if (userRepository.count() > 0) {
      LOG.info("Users already exist, skipping initialization");
      return;
    }

    // Create test users
    createUser("admin", "Admin", "User", "admin@freshplan.de", "admin");
    createUser("manager", "Manager", "User", "manager@freshplan.de", "manager");
    createUser("sales", "Sales", "User", "sales@freshplan.de", "sales");
    createUser("max.mustermann", "Max", "Mustermann", "max@example.com", "sales");
    createUser("erika.musterfrau", "Erika", "Musterfrau", "erika@example.com", "sales");

    LOG.info("Development data initialized successfully");
  }

  private void createUser(
      String username, String firstName, String lastName, String email, String... roles) {
    User user = new User(username, firstName, lastName, email);
    user.setRoles(Arrays.asList(roles));
    userRepository.persist(user);
    LOG.infof("Created user: %s with roles: %s", username, String.join(", ", roles));
  }
}
