package de.freshplan.test.builders;

import de.freshplan.domain.user.entity.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Test data factory for User entities. Provides builder pattern for creating test users without
 * CDI.
 *
 * @author Claude
 * @since Migration Phase 4 - Quick Wins
 */
public class UserTestDataFactory {

  /** Create a new builder instance. */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    // Default values
    private String username = "testuser";
    private String firstName = "Test";
    private String lastName = "User";
    private String email;
    private boolean enabled = true;
    private List<String> roles = new ArrayList<>();

    /**
     * Set the username.
     *
     * @param username Username
     * @return This builder
     */
    public Builder withUsername(String username) {
      this.username = username;
      return this;
    }

    /**
     * Set the first name.
     *
     * @param firstName First name
     * @return This builder
     */
    public Builder withFirstName(String firstName) {
      this.firstName = firstName;
      return this;
    }

    /**
     * Set the last name.
     *
     * @param lastName Last name
     * @return This builder
     */
    public Builder withLastName(String lastName) {
      this.lastName = lastName;
      return this;
    }

    /**
     * Set the email address.
     *
     * @param email Email address
     * @return This builder
     */
    public Builder withEmail(String email) {
      this.email = email;
      return this;
    }

    /**
     * Mark as disabled.
     *
     * @return This builder
     */
    public Builder asDisabled() {
      this.enabled = false;
      return this;
    }

    /**
     * Mark as enabled (default).
     *
     * @return This builder
     */
    public Builder asEnabled() {
      this.enabled = true;
      return this;
    }

    /**
     * Add a role.
     *
     * @param role Role to add
     * @return This builder
     */
    public Builder withRole(String role) {
      this.roles.add(role);
      return this;
    }

    /**
     * Set multiple roles.
     *
     * @param roles List of roles
     * @return This builder
     */
    public Builder withRoles(List<String> roles) {
      this.roles = new ArrayList<>(roles);
      return this;
    }

    /**
     * Build the user entity without persisting. Uses the constructor with parameters as User has
     * protected default constructor.
     *
     * @return The built user entity
     */
    public User build() {
      // Generate email if not set
      if (email == null && username != null) {
        email = (username + "@freshplan.de").toLowerCase();
      }

      // Use constructor with parameters
      User user =
          new User(
              username != null ? username : "testuser",
              firstName != null ? firstName : "Test",
              lastName != null ? lastName : "User",
              email != null ? email : "testuser@freshplan.de");

      // Set additional fields if available
      if (!roles.isEmpty()) {
        user.setRoles(roles);
      }

      // Handle enabled/disabled state
      if (!enabled) {
        user.disable();
      }

      return user;
    }

    /** Build a default test user. Convenience method for common test case. */
    public User buildDefault() {
      return withUsername("john.doe")
          .withFirstName("John")
          .withLastName("Doe")
          .withEmail("john.doe@freshplan.de")
          .build();
    }
  }
}
