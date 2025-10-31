package de.freshplan.domain.user.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * User entity representing application users.
 *
 * <p>This entity is mapped to the 'app_user' table to avoid conflicts with PostgreSQL reserved
 * keywords. It uses UUID as primary key for better distribution and security.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Entity
@Table(name = "app_user")
@NamedQueries({
  @NamedQuery(
      name = "User.findByUsername",
      query = "SELECT u FROM User u WHERE u.username = :username"),
  @NamedQuery(name = "User.findByEmail", query = "SELECT u FROM User u WHERE u.email = :email"),
  @NamedQuery(
      name = "User.findEnabled",
      query = "SELECT u FROM User u WHERE u.enabled = true ORDER BY u.lastName, u.firstName")
})
public class User extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id;

  @Column(length = 60, unique = true, nullable = false)
  private String username;

  @Column(name = "first_name", length = 60, nullable = false)
  private String firstName;

  @Column(name = "last_name", length = 60, nullable = false)
  private String lastName;

  @Column(length = 120, unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private boolean enabled = false;

  @Column(name = "is_test_data", nullable = false)
  private boolean isTestData = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "role")
  private List<String> roles = new ArrayList<>();

  // ============================================================================
  // Xentral Integration Fields (Sprint 2.1.7.2 - Migration V10035)
  // ============================================================================

  /**
   * Xentral Employee ID - Maps FreshPlan User to Xentral Sales Rep.
   *
   * <p>Auto-synced via Nightly Sales-Rep Sync Job (Email-Matching). Used for RLS Security: Sales
   * users see only customers assigned to them in Xentral.
   */
  @Column(name = "xentral_sales_rep_id", length = 50)
  private String xentralSalesRepId;

  /**
   * Manager reference - Team hierarchy for RLS Security.
   *
   * <p>Managers can see all customers of their team members. Set manually via Admin-UI User
   * Management.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "manager_id")
  private User manager;

  /**
   * Team members reporting to this user (inverse side of manager relationship).
   *
   * <p>Used to calculate which customers a manager can see (all customers of team members).
   */
  @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
  private List<User> teamMembers = new ArrayList<>();

  /**
   * Manager privilege: Can see customers without assigned sales rep.
   *
   * <p>Default: false. When true, manager sees Team-Kunden + Unassigned-Kunden. Future: Migrate to
   * customer_groups system if more flexibility needed.
   */
  @Column(name = "can_see_unassigned_customers", nullable = false)
  private Boolean canSeeUnassignedCustomers = false;

  /** Default constructor required by JPA. */
  protected User() {
    // Required by JPA
  }

  /**
   * Creates a new user with the given details.
   *
   * @param username unique username
   * @param firstName user's first name
   * @param lastName user's last name
   * @param email user's email address
   */
  public User(String username, String firstName, String lastName, String email) {
    this.username = Objects.requireNonNull(username, "Username cannot be null");
    this.firstName = Objects.requireNonNull(firstName, "First name cannot be null");
    this.lastName = Objects.requireNonNull(lastName, "Last name cannot be null");
    this.email = Objects.requireNonNull(email, "Email cannot be null");
    this.enabled = true; // New users are enabled by default
    this.roles.add("sales"); // Default role per PHASE2_KICKOFF.md
  }

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
    updatedAt = Instant.now();
  }

  @PreUpdate
  protected void onUpdate() {
    // updatedAt is now handled by @UpdateTimestamp
  }

  /**
   * Returns the user's full name.
   *
   * @return concatenated first and last name
   */
  public String getFullName() {
    return String.format("%s %s", firstName, lastName);
  }

  /** Disables this user account. */
  public void disable() {
    this.enabled = false;
  }

  /** Enables this user account. */
  public void enable() {
    this.enabled = true;
  }

  // Getters and Setters with proper encapsulation

  public UUID getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = Objects.requireNonNull(username, "Username cannot be null");
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = Objects.requireNonNull(firstName, "First name cannot be null");
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = Objects.requireNonNull(lastName, "Last name cannot be null");
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = Objects.requireNonNull(email, "Email cannot be null");
  }

  public boolean isEnabled() {
    return enabled;
  }

  public boolean isTestData() {
    return isTestData;
  }

  public void setIsTestData(boolean isTestData) {
    this.isTestData = isTestData;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public List<String> getRoles() {
    return new ArrayList<>(roles); // Return defensive copy
  }

  public void setRoles(List<String> roles) {
    this.roles = roles != null ? new ArrayList<>(roles) : new ArrayList<>();
  }

  /**
   * Adds a role to this user.
   *
   * @param role the role to add
   */
  public void addRole(String role) {
    if (role != null && !roles.contains(role)) {
      roles.add(role);
    }
  }

  /**
   * Removes a role from this user.
   *
   * @param role the role to remove
   */
  public void removeRole(String role) {
    roles.remove(role);
  }

  /**
   * Checks if the user has a specific role.
   *
   * @param role the role to check
   * @return true if the user has the role, false otherwise
   */
  public boolean hasRole(String role) {
    return roles.contains(role);
  }

  // ============================================================================
  // Xentral Integration Field Accessors (Sprint 2.1.7.2)
  // ============================================================================

  public String getXentralSalesRepId() {
    return xentralSalesRepId;
  }

  public void setXentralSalesRepId(String xentralSalesRepId) {
    this.xentralSalesRepId = xentralSalesRepId;
  }

  public User getManager() {
    return manager;
  }

  public void setManager(User manager) {
    this.manager = manager;
  }

  public List<User> getTeamMembers() {
    return new ArrayList<>(teamMembers); // Defensive copy
  }

  public Boolean getCanSeeUnassignedCustomers() {
    return canSeeUnassignedCustomers != null ? canSeeUnassignedCustomers : false;
  }

  public void setCanSeeUnassignedCustomers(Boolean canSeeUnassignedCustomers) {
    this.canSeeUnassignedCustomers =
        canSeeUnassignedCustomers != null ? canSeeUnassignedCustomers : false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User)) return false;
    User user = (User) o;
    return Objects.equals(id, user.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "User{"
        + "id="
        + id
        + ", username='"
        + username
        + '\''
        + ", email='"
        + email
        + '\''
        + ", enabled="
        + enabled
        + '}';
  }
}
