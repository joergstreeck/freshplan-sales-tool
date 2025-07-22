package de.freshplan.domain.permission.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.GenericGenerator;

/**
 * Permission entity representing system permissions.
 *
 * <p>Permissions follow the format: "resource:action" (e.g., "customers:read", "customers:write")
 *
 * <p>V103 Migration applied - Permission tables available
 */
@Entity
@Table(name = "permissions")
@NamedQueries({
  @NamedQuery(
      name = "Permission.findByCode",
      query = "SELECT p FROM Permission p WHERE p.permissionCode = :code"),
  @NamedQuery(
      name = "Permission.findByResource",
      query = "SELECT p FROM Permission p WHERE p.resource = :resource ORDER BY p.action")
})
public class Permission extends PanacheEntityBase {

  @Id
  @GeneratedValue(generator = "uuid2")
  @GenericGenerator(name = "uuid2", strategy = "uuid2")
  @Column(columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "permission_code", length = 100, unique = true, nullable = false)
  private String permissionCode;

  @Column(length = 255, nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(length = 50, nullable = false)
  private String resource;

  @Column(length = 50, nullable = false)
  private String action;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected Permission() {
    // Required by JPA
  }

  public Permission(String permissionCode, String name, String description) {
    this.permissionCode = Objects.requireNonNull(permissionCode, "Permission code cannot be null");
    this.name = Objects.requireNonNull(name, "Name cannot be null");
    this.description = description;

    // Parse resource and action from permission code
    String[] parts = permissionCode.split(":");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Permission code must follow format 'resource:action'");
    }
    this.resource = parts[0];
    this.action = parts[1];
  }

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
  }

  // Getters
  public UUID getId() {
    return id;
  }

  public String getPermissionCode() {
    return permissionCode;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getResource() {
    return resource;
  }

  public String getAction() {
    return action;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  // Business methods
  public boolean matches(String resource, String action) {
    return this.resource.equals(resource) && this.action.equals(action);
  }

  public boolean matchesWildcard(String permissionCode) {
    if ("*:*".equals(this.permissionCode)) {
      return true;
    }

    String[] thisParts = this.permissionCode.split(":");
    String[] otherParts = permissionCode.split(":");

    if (thisParts.length != 2 || otherParts.length != 2) {
      return false;
    }

    return (thisParts[0].equals("*") || thisParts[0].equals(otherParts[0]))
        && (thisParts[1].equals("*") || thisParts[1].equals(otherParts[1]));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Permission)) return false;
    Permission that = (Permission) o;
    return Objects.equals(permissionCode, that.permissionCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(permissionCode);
  }

  @Override
  public String toString() {
    return "Permission{"
        + "id="
        + id
        + ", permissionCode='"
        + permissionCode
        + '\''
        + ", name='"
        + name
        + '\''
        + '}';
  }
}
