package de.freshplan.domain.permission.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.GenericGenerator;

/**
 * Role entity representing user roles with associated permissions.
 *
 * <p>V103 Migration applied - Role tables available
 */
@Entity
@Table(name = "roles")
@NamedQueries({
  @NamedQuery(name = "Role.findByName", query = "SELECT r FROM Role r WHERE r.name = :name"),
  @NamedQuery(
      name = "Role.findActive",
      query = "SELECT r FROM Role r WHERE r.active = true ORDER BY r.name")
})
public class Role extends PanacheEntityBase {

  @Id
  @GeneratedValue(generator = "uuid2")
  @GenericGenerator(name = "uuid2", strategy = "uuid2")
  @Column(columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id;

  @Column(length = 50, unique = true, nullable = false)
  private String name;

  @Column(length = 255)
  private String description;

  @Column(nullable = false)
  private boolean active = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<RolePermission> rolePermissions = new HashSet<>();

  protected Role() {
    // Required by JPA
  }

  public Role(String name, String description) {
    this.name = Objects.requireNonNull(name, "Name cannot be null");
    this.description = description;
  }

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
    updatedAt = Instant.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }

  // Business methods
  public void grantPermission(Permission permission, UUID grantedBy) {
    RolePermission rolePermission = new RolePermission(this, permission, true, grantedBy);
    rolePermissions.add(rolePermission);
  }

  public void revokePermission(Permission permission, UUID revokedBy) {
    rolePermissions.removeIf(rp -> rp.getPermission().equals(permission));
    // Add explicit revoke record
    RolePermission revoke = new RolePermission(this, permission, false, revokedBy);
    rolePermissions.add(revoke);
  }

  public boolean hasPermission(String permissionCode) {
    return rolePermissions.stream()
        .filter(RolePermission::isGranted)
        .anyMatch(
            rp ->
                rp.getPermission().getPermissionCode().equals(permissionCode)
                    || rp.getPermission().matchesWildcard(permissionCode));
  }

  // Getters
  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = Objects.requireNonNull(name, "Name cannot be null");
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Set<RolePermission> getRolePermissions() {
    return new HashSet<>(rolePermissions);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Role)) return false;
    Role role = (Role) o;
    return Objects.equals(name, role.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return "Role{" + "id=" + id + ", name='" + name + '\'' + ", active=" + active + '}';
  }
}
