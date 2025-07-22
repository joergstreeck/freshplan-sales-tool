package de.freshplan.domain.permission.repository;

import de.freshplan.domain.permission.entity.Permission;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repository for Permission entity operations. */
@ApplicationScoped
public class PermissionRepository implements PanacheRepository<Permission> {

  public Optional<Permission> findByCode(String permissionCode) {
    return find("#Permission.findByCode", "code", permissionCode).firstResultOptional();
  }

  public List<Permission> findByResource(String resource) {
    return find("#Permission.findByResource", "resource", resource).list();
  }

  public List<Permission> findAllActive() {
    return listAll();
  }

  public List<Permission> findPermissionsByRole(UUID roleId) {
    return find(
            "SELECT p FROM Permission p "
                + "JOIN RolePermission rp ON rp.permission.id = p.id "
                + "WHERE rp.role.id = ?1 AND rp.granted = true",
            roleId)
        .list();
  }

  public long countByResource(String resource) {
    return count("resource", resource);
  }
}
