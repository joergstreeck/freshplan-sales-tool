package de.freshplan.domain.permission.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Direct user permission entity for overrides and temporary permissions.
 *
 * <p>V103 Migration applied - UserPermission tables available
 */
@Entity
@Table(name = "user_permissions")
@IdClass(UserPermissionId.class)
@NamedQueries({
  @NamedQuery(
      name = "UserPermission.findByUser",
      query =
          "SELECT up FROM UserPermission up WHERE up.userId = :userId AND (up.expiresAt IS NULL OR up.expiresAt > CURRENT_TIMESTAMP)"),
  @NamedQuery(
      name = "UserPermission.findDirectPermission",
      query =
          "SELECT up FROM UserPermission up WHERE up.userId = :userId AND up.permission.permissionCode = :permissionCode AND (up.expiresAt IS NULL OR up.expiresAt > CURRENT_TIMESTAMP)"),
  @NamedQuery(
      name = "UserPermission.findExpired",
      query =
          "SELECT up FROM UserPermission up WHERE up.expiresAt IS NOT NULL AND up.expiresAt <= CURRENT_TIMESTAMP")
})
public class UserPermission extends PanacheEntityBase {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "permission_id")
  private Permission permission;

  @Column(nullable = false)
  private boolean granted;

  @Column(name = "granted_by")
  private UUID grantedBy;

  @Column(name = "granted_at", nullable = false)
  private Instant grantedAt;

  @Column(name = "expires_at")
  private LocalDateTime expiresAt;

  @Column(columnDefinition = "TEXT")
  private String reason;

  protected UserPermission() {
    // Required by JPA
  }

  public UserPermission(
      UUID userId, Permission permission, boolean granted, UUID grantedBy, String reason) {
    this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
    this.permission = Objects.requireNonNull(permission, "Permission cannot be null");
    this.granted = granted;
    this.grantedBy = grantedBy;
    this.reason = reason;
    this.grantedAt = Instant.now();
  }

  public UserPermission(
      UUID userId,
      Permission permission,
      boolean granted,
      UUID grantedBy,
      LocalDateTime expiresAt,
      String reason) {
    this(userId, permission, granted, grantedBy, reason);
    this.expiresAt = expiresAt;
  }

  // Business methods - Note: use PermissionService.isUserPermissionExpired() for timezone-safe
  // checks
  @Deprecated
  public boolean isExpired() {
    // DEPRECATED: This method uses system timezone and should not be used in production
    // Use PermissionService.isUserPermissionExpired(UserPermission, Clock) instead
    return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
  }

  @Deprecated
  public boolean isActive() {
    // DEPRECATED: This method uses system timezone and should not be used in production
    // Use PermissionService.isUserPermissionActive(UserPermission, Clock) instead
    return !isExpired();
  }

  // Getters
  public UUID getUserId() {
    return userId;
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

  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(LocalDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UserPermission)) return false;
    UserPermission that = (UserPermission) o;
    return Objects.equals(userId, that.userId) && Objects.equals(permission, that.permission);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, permission);
  }

  @Override
  public String toString() {
    return "UserPermission{"
        + "userId="
        + userId
        + ", permission="
        + (permission != null ? permission.getPermissionCode() : null)
        + ", granted="
        + granted
        + ", expiresAt="
        + expiresAt
        + '}';
  }
}
