package de.freshplan.domain.permission.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Role-Permission mapping entity with audit information.
 *
 * <p>V103 Migration applied - RolePermission tables available
 */
@Entity
@Table(name = "role_permissions")
@IdClass(RolePermissionId.class)
@NamedQueries({
  @NamedQuery(
      name = "RolePermission.findByRole",
      query = "SELECT rp FROM RolePermission rp WHERE rp.role.id = :roleId AND rp.granted = true"),
  @NamedQuery(
      name = "RolePermission.findByPermission",
      query =
          "SELECT rp FROM RolePermission rp WHERE rp.permission.id = :permissionId AND rp.granted = true")
})
public class RolePermission extends PanacheEntityBase {

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "role_id")
  private Role role;

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "permission_id")
  private Permission permission;

  @Column(nullable = false)
  private boolean granted = true;

  @Column(name = "granted_by")
  private UUID grantedBy;

  @Column(name = "granted_at", nullable = false)
  private Instant grantedAt;

  protected RolePermission() {
    // Required by JPA
  }

  public RolePermission(Role role, Permission permission, boolean granted, UUID grantedBy) {
    this.role = Objects.requireNonNull(role, "Role cannot be null");
    this.permission = Objects.requireNonNull(permission, "Permission cannot be null");
    this.granted = granted;
    this.grantedBy = grantedBy;
    this.grantedAt = Instant.now();
  }

  // Getters
  public Role getRole() {
    return role;
  }

  public Permission getPermission() {
    return permission;
  }

  public boolean isGranted() {
    return granted;
  }

  public UUID getGrantedBy() {
    return grantedBy;
  }

  public Instant getGrantedAt() {
    return grantedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RolePermission)) return false;
    RolePermission that = (RolePermission) o;
    return Objects.equals(role, that.role) && Objects.equals(permission, that.permission);
  }

  @Override
  public int hashCode() {
    return Objects.hash(role, permission);
  }

  @Override
  public String toString() {
    return "RolePermission{"
        + "role="
        + (role != null ? role.getId() : null)
        + ", permission="
        + (permission != null ? permission.getId() : null)
        + ", granted="
        + granted
        + '}';
  }
}
