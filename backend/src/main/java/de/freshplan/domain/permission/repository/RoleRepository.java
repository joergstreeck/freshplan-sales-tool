package de.freshplan.domain.permission.repository;

import de.freshplan.domain.permission.entity.Role;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repository for Role entity operations. */
@ApplicationScoped
public class RoleRepository implements PanacheRepository<Role> {

  public Optional<Role> findByName(String name) {
    return find("#Role.findByName", "name", name).firstResultOptional();
  }

  public List<Role> findActive() {
    return find("#Role.findActive").list();
  }

  public List<Role> findActiveRolesByUser(UUID userId) {
    // This queries the legacy user_roles table
    return find(
            "SELECT r FROM Role r "
                + "WHERE r.name IN ("
                + "  SELECT ur.role FROM app_user u "
                + "  JOIN u.roles ur "
                + "  WHERE u.id = ?1"
                + ") AND r.active = true",
            userId)
        .list();
  }

  public List<Role> findByNamePattern(String pattern) {
    return find("name LIKE ?1", "%" + pattern + "%").list();
  }

  public boolean existsByName(String name) {
    return count("name", name) > 0;
  }
}
