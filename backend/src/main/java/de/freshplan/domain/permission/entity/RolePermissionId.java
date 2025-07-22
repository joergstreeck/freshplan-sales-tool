package de.freshplan.domain.permission.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** Composite key for RolePermission entity. */
public class RolePermissionId implements Serializable {

  private UUID role;
  private UUID permission;

  public RolePermissionId() {
    // Required by JPA
  }

  public RolePermissionId(UUID role, UUID permission) {
    this.role = role;
    this.permission = permission;
  }

  public UUID getRole() {
    return role;
  }

  public void setRole(UUID role) {
    this.role = role;
  }

  public UUID getPermission() {
    return permission;
  }

  public void setPermission(UUID permission) {
    this.permission = permission;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RolePermissionId)) return false;
    RolePermissionId that = (RolePermissionId) o;
    return Objects.equals(role, that.role) && Objects.equals(permission, that.permission);
  }

  @Override
  public int hashCode() {
    return Objects.hash(role, permission);
  }
}
