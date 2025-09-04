package de.freshplan.test.builders;

import de.freshplan.domain.user.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test data factory for User entities. Provides builder pattern for creating test users without
 * CDI.
 *
 * @author Claude
 * @since Migration Phase 4 - Quick Wins
 * @since Phase 2A - Enhanced with collision-free ID generation and isTestData flag
 */
public class UserTestDataFactory {

  // KOLLISIONSFREIE ID-GENERIERUNG - Thread-Safe & CI-kompatibel
  private static final AtomicLong SEQ = new AtomicLong();

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
    private boolean isTestData = true; // PFLICHT für alle Test-User
    private List<String> roles = new ArrayList<>();

    /**
     * Run-ID für eindeutige Test-Identifikation.
     * Nutzt test.run.id (CI) -> GITHUB_RUN_ID (Fallback) -> "LOCAL"
     */
    private static String runId() {
      return System.getProperty("test.run.id",
          System.getenv().getOrDefault("GITHUB_RUN_ID", "LOCAL"));
    }

    /**
     * Generiert eindeutige User-Identifiers basierend auf Run-ID.
     * Verhindert Kollisionen bei parallelen Tests.
     */
    private void generateUniqueIdentifiers() {
      String runId = runId();
      long sequence = SEQ.incrementAndGet();
      String uniqueSuffix = runId + "_" + sequence;
      
      // Email IMMER unique machen - auch wenn explizit gesetzt
      // Füge suffix hinzu um Kollisionen zu vermeiden
      if (email == null) {
        email = "test." + uniqueSuffix + "@test.example.com";
      } else if (!email.contains(uniqueSuffix)) {
        // Explizit gesetzte Email auch unique machen
        String localPart = email.substring(0, email.indexOf('@'));
        String domain = email.substring(email.indexOf('@'));
        email = localPart + "." + uniqueSuffix + domain;
      }
      
      // Username unique machen falls Default verwendet
      if ("testuser".equals(username)) {
        username = "testuser_" + uniqueSuffix;
      } else if (!username.contains(uniqueSuffix)) {
        // Explizit gesetzten Username auch unique machen
        username = username + "_" + uniqueSuffix;
      }
    }

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
     * Mark as test data (always true for test users).
     * Note: This is always true regardless of what you set here.
     *
     * @param isTestData ignored - always true
     * @return This builder
     */
    public Builder asTestData(boolean isTestData) {
      // Always true for test users - parameter ignored
      this.isTestData = true;
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
      // Generiere eindeutige Identifiers falls nötig
      generateUniqueIdentifiers();

      // Use constructor with parameters
      User user =
          new User(
              username != null ? username : "testuser",
              firstName != null ? firstName : "Test",
              lastName != null ? lastName : "User",
              email != null ? email : "testuser@test.example.com");

      // KRITISCH: Immer als Test-User markieren
      user.setIsTestData(true);

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
