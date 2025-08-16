package de.freshplan.testsupport;

import de.freshplan.domain.permission.entity.Permission;
import de.freshplan.domain.permission.repository.PermissionRepository;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

/**
 * Test-only service for race-safe permission creation in parallel test execution.
 * Uses pessimistic locking and REQUIRES_NEW transaction to prevent duplicate key violations.
 * 
 * This service is only available in test and CI profiles.
 */
@ApplicationScoped
@IfBuildProfile(anyOf = {"test", "ci"})
public class PermissionServiceTestOnly {
  
  @Inject 
  PermissionRepository permissionRepository;

  /**
   * Find or create a base permission with pessimistic locking to prevent race conditions.
   * Uses REQUIRES_NEW transaction to avoid deadlocks.
   * 
   * @param code the permission code (e.g., "customers:read")
   * @return the existing or newly created permission
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public Permission findOrCreateBasePermission(String code) {
    // Try to find existing permission with pessimistic lock
    var existing = permissionRepository
        .find("code", code)
        .withLock(LockModeType.PESSIMISTIC_WRITE)
        .firstResultOptional();
    
    if (existing.isPresent()) {
      return existing.get();
    }

    // Create new permission
    Permission permission = new Permission();
    permission.setCode(code);
    permission.setName(code.replace(":", " ").toUpperCase());
    permission.setDescription("Test permission for " + code);
    permission.setActive(true);
    permission.setCreatedAt(LocalDateTime.now());
    permission.setUpdatedAt(LocalDateTime.now());
    
    try {
      permissionRepository.persistAndFlush(permission);
      return permission;
    } catch (PersistenceException e) {
      // Another fork created it in parallel - fetch the existing one
      return permissionRepository
          .find("code", code)
          .withLock(LockModeType.PESSIMISTIC_WRITE)
          .firstResult();
    }
  }

  /**
   * Create a unique permission for test isolation.
   * Adds fork suffix to make it unique across parallel test runs.
   * 
   * @param baseCode the base permission code
   * @return a new unique permission
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public Permission createUniquePermission(String baseCode) {
    String uniqueCode = UniqueData.permissionCode(baseCode);
    
    Permission permission = new Permission();
    permission.setCode(uniqueCode);
    permission.setName(uniqueCode.replace(":", " ").toUpperCase());
    permission.setDescription("Test permission for " + uniqueCode);
    permission.setActive(true);
    permission.setCreatedAt(LocalDateTime.now());
    permission.setUpdatedAt(LocalDateTime.now());
    
    permissionRepository.persist(permission);
    return permission;
  }
}