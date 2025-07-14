package de.freshplan.test;

import de.freshplan.infrastructure.security.CurrentUser;
import de.freshplan.infrastructure.security.UserPrincipal;
import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.util.Set;

/**
 * Mock security setup for tests. Provides a default test user principal when @TestSecurity is not
 * used.
 */
@Mock
@ApplicationScoped
public class MockSecuritySetup {

  @Produces
  @CurrentUser
  @ApplicationScoped
  public UserPrincipal mockCurrentUser() {
    return UserPrincipal.builder()
        .username("testuser")
        .email("testuser@example.com")
        .roles(Set.of("admin", "manager", "sales"))
        .authenticated(true)
        .build();
  }
}
