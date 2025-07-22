package de.freshplan.domain.permission.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** Composite key for UserPermission entity. */
public class UserPermissionId implements Serializable {

  private UUID userId;
  private UUID permission;

  public UserPermissionId() {
    // Required by JPA
  }

  public UserPermissionId(UUID userId, UUID permission) {
    this.userId = userId;
    this.permission = permission;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
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
    if (!(o instanceof UserPermissionId)) return false;
    UserPermissionId that = (UserPermissionId) o;
    return Objects.equals(userId, that.userId) && Objects.equals(permission, that.permission);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, permission);
  }
}
