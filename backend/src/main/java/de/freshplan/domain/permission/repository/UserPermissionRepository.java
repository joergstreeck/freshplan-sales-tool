package de.freshplan.domain.permission.repository;

import de.freshplan.domain.permission.entity.UserPermission;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repository for UserPermission entity operations. */
@ApplicationScoped
public class UserPermissionRepository implements PanacheRepository<UserPermission> {

  public Optional<UserPermission> findDirectPermission(UUID userId, String permissionCode) {
    return find(
            "SELECT up FROM UserPermission up "
                + "JOIN up.permission p "
                + "WHERE up.userId = ?1 "
                + "AND p.permissionCode = ?2 "
                + "AND (up.expiresAt IS NULL OR up.expiresAt > CURRENT_TIMESTAMP)",
            userId,
            permissionCode)
        .firstResultOptional();
  }

  public List<UserPermission> findByUser(UUID userId) {
    return find("#UserPermission.findByUser", "userId", userId).list();
  }

  public List<UserPermission> findExpired() {
    return find("#UserPermission.findExpired").list();
  }

  public void cleanupExpired() {
    delete("expiresAt IS NOT NULL AND expiresAt <= CURRENT_TIMESTAMP");
  }
}
