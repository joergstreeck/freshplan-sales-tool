package de.freshplan.test.builders;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.test.utils.TestDataUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating test User entities. Provides fluent API for setting user properties and
 * predefined roles.
 *
 * <p>This builder ensures all test users are identifiable through their username prefix since the
 * User entity doesn't have an isTestData field.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class UserBuilder {

  @Inject UserRepository repository;

  // Required fields
  private String username = "test.user";
  private String firstName = "Test";
  private String lastName = "User";
  private String email = null; // Will be generated if not set

  // Optional fields
  private boolean enabled = true; // Default to enabled for test users
  private List<String> roles = new ArrayList<>();

  // Test marker - users don't have isTestData field, so we mark via username
  private final String testPrefix = "test-";

  /**
   * Resets the builder to default values for creating a new user.
   *
   * @return this builder instance for chaining
   */
  public UserBuilder reset() {
    this.username = "test.user";
    this.firstName = "Test";
    this.lastName = "User";
    this.email = null;
    this.enabled = true;
    this.roles = new ArrayList<>();
    // Default role per User entity constructor
    this.roles.add("sales");
    return this;
  }

  // Basic setters
  public UserBuilder withUsername(String username) {
    this.username = username;
    return this;
  }

  public UserBuilder withFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public UserBuilder withLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public UserBuilder withFullName(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
    return this;
  }

  public UserBuilder withEmail(String email) {
    this.email = email;
    return this;
  }

  // Status setters
  public UserBuilder enabled() {
    this.enabled = true;
    return this;
  }

  public UserBuilder disabled() {
    this.enabled = false;
    return this;
  }

  // Role management
  public UserBuilder withRole(String role) {
    if (!this.roles.contains(role)) {
      this.roles.add(role);
    }
    return this;
  }

  public UserBuilder withRoles(String... roles) {
    for (String role : roles) {
      if (!this.roles.contains(role)) {
        this.roles.add(role);
      }
    }
    return this;
  }

  public UserBuilder clearRoles() {
    this.roles.clear();
    return this;
  }

  // Predefined scenarios
  public UserBuilder asAdmin() {
    this.withRole("admin");
    this.withRole("manager");
    this.withRole("sales");
    this.firstName = "Admin";
    this.lastName = "User";
    this.enabled = true;
    return this;
  }

  public UserBuilder asManager() {
    this.clearRoles();
    this.withRole("manager");
    this.withRole("sales");
    this.firstName = "Manager";
    this.lastName = "User";
    this.enabled = true;
    return this;
  }

  public UserBuilder asSalesRep() {
    this.clearRoles();
    this.withRole("sales");
    this.firstName = "Sales";
    this.lastName = "Representative";
    this.enabled = true;
    return this;
  }

  public UserBuilder asReadOnlyUser() {
    this.clearRoles();
    this.withRole("readonly");
    this.firstName = "ReadOnly";
    this.lastName = "User";
    this.enabled = true;
    return this;
  }

  public UserBuilder asDeactivatedUser() {
    this.enabled = false;
    this.firstName = "Deactivated";
    this.lastName = "User";
    return this;
  }

  public UserBuilder asExternalPartner() {
    this.clearRoles();
    this.withRole("partner");
    this.firstName = "External";
    this.lastName = "Partner";
    this.enabled = true;
    return this;
  }

  /**
   * Builds a User entity WITHOUT persisting to database. Use this for unit tests or when you need
   * an entity without DB interaction.
   *
   * @return a new User entity with test data markers
   */
  public User build() {
    String id = TestDataUtils.uniqueId();

    // Generate unique username with test prefix
    String finalUsername = testPrefix + username + "-" + id;

    // Generate email if not set
    if (email == null) {
      email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "@test-" + id + ".com";
    }

    // Create user using constructor (which handles null checks)
    User user = new User(finalUsername, firstName, lastName, email);

    // Set enabled status
    if (!enabled) {
      user.disable();
    }

    // Set roles - clear default and add configured ones
    user.setRoles(new ArrayList<>(roles));

    return user;
  }

  /**
   * Builds and persists a User entity to the database. Use this for integration tests that need DB
   * interaction.
   *
   * @return the persisted User entity
   */
  @Transactional
  public User persist() {
    User user = build();
    repository.persist(user);
    repository.flush(); // Force immediate constraint validation
    return user;
  }
}
