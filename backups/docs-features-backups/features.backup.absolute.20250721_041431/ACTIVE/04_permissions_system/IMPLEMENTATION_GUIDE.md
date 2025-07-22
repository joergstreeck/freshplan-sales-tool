# 📘 FC-009 IMPLEMENTATION GUIDE

**Zweck:** Detaillierte Code-Snippets und Schritt-für-Schritt Anleitung  
**Prerequisite:** [FC-009_TECH_CONCEPT.md](./FC-009_TECH_CONCEPT.md) gelesen  

---

## Phase 1: Database Setup (Tag 1)

### Schritt 1.1: Migration erstellen
```bash
cd backend/src/main/resources/db/migration
cat > V3.0__create_permission_tables.sql << 'EOF'
-- Permission Definition
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    menu_item VARCHAR(100) NOT NULL,
    capability VARCHAR(50) NOT NULL,
    scope VARCHAR(20) NOT NULL CHECK (scope IN ('all', 'team', 'own')),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(menu_item, capability)
);

-- User Permissions
CREATE TABLE user_permissions (
    user_id UUID NOT NULL REFERENCES users(id),
    permission_id UUID NOT NULL REFERENCES permissions(id),
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    granted_by UUID REFERENCES users(id),
    PRIMARY KEY (user_id, permission_id)
);

-- Teams
CREATE TABLE teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    parent_team_id UUID REFERENCES teams(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_user_permissions_user ON user_permissions(user_id);
CREATE INDEX idx_teams_parent ON teams(parent_team_id);
EOF
```

### Schritt 1.2: Test Migration
```bash
cd backend
./mvnw quarkus:dev -Dquarkus.flyway.migrate-at-start=true
# Check logs für "Migrating schema \"public\" to version 3.0"
```

---

## Phase 2: Backend Entities (Tag 2-3)

### Schritt 2.1: Permission Entity
```bash
mkdir -p src/main/java/de/freshplan/domain/permission/entity
cat > src/main/java/de/freshplan/domain/permission/entity/Permission.java << 'EOF'
package de.freshplan.domain.permission.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "permissions")
@Cacheable
public class Permission extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    
    @Column(name = "menu_item", nullable = false, length = 100)
    public String menuItem;
    
    @Column(nullable = false, length = 50)
    public String capability;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public PermissionScope scope;
    
    @Column(columnDefinition = "TEXT")
    public String description;
    
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();
    
    // Finder methods
    public static Permission findByMenuAndCapability(String menu, String capability) {
        return find("menuItem = ?1 and capability = ?2", menu, capability).firstResult();
    }
    
    public enum PermissionScope {
        ALL, TEAM, OWN
    }
}
EOF
```

### Schritt 2.2: Repository
```bash
cat > src/main/java/de/freshplan/domain/permission/repository/PermissionRepository.java << 'EOF'
package de.freshplan.domain.permission.repository;

import de.freshplan.domain.permission.entity.Permission;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;

@ApplicationScoped
public class PermissionRepository implements PanacheRepositoryBase<Permission, UUID> {
    
    public Set<Permission> findUserPermissions(UUID userId) {
        return new HashSet<>(find(
            "SELECT p FROM Permission p " +
            "JOIN UserPermission up ON up.permissionId = p.id " +
            "WHERE up.userId = ?1", userId
        ).list());
    }
    
    public boolean hasPermission(UUID userId, String menuItem, String capability) {
        return count(
            "SELECT COUNT(p) FROM Permission p " +
            "JOIN UserPermission up ON up.permissionId = p.id " +
            "WHERE up.userId = ?1 AND p.menuItem = ?2 AND p.capability = ?3",
            userId, menuItem, capability
        ) > 0;
    }
}
EOF
```

---

## Phase 3: Security Integration (Tag 4-5)

### Schritt 3.1: Enhanced SecurityContextProvider
```bash
cat > src/main/java/de/freshplan/infrastructure/security/EnhancedSecurityContextProvider.java << 'EOF'
package de.freshplan.infrastructure.security;

import de.freshplan.domain.permission.repository.PermissionRepository;
import de.freshplan.domain.permission.entity.Permission;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;

@ApplicationScoped
public class EnhancedSecurityContextProvider extends SecurityContextProvider {
    
    @Inject
    PermissionRepository permissionRepository;
    
    @ConfigProperty(name = "feature.new-permissions.enabled", defaultValue = "false")
    boolean useNewPermissions;
    
    @Override
    public boolean hasRole(String role) {
        // Legacy support
        if (!useNewPermissions) {
            return super.hasRole(role);
        }
        
        // Map old roles to new permissions
        return switch(role) {
            case "admin" -> hasCapability("system", "admin_access");
            case "manager" -> hasCapability("team", "manage_team");
            case "sales" -> hasCapability("customer", "view_own");
            default -> false;
        };
    }
    
    @CacheResult(cacheName = "user-permissions")
    public boolean hasCapability(String menuItem, String capability) {
        UUID userId = getCurrentUserId();
        return permissionRepository.hasPermission(userId, menuItem, capability);
    }
    
    public Set<Permission> getUserPermissions() {
        UUID userId = getCurrentUserId();
        return permissionRepository.findUserPermissions(userId);
    }
}
EOF
```

### Schritt 3.2: Test Security
```bash
# Unit Test erstellen
cat > src/test/java/de/freshplan/infrastructure/security/EnhancedSecurityContextProviderTest.java << 'EOF'
// Test implementation...
EOF

# Test ausführen
./mvnw test -Dtest=EnhancedSecurityContextProviderTest
```

---

## Phase 4: Migration Script (Tag 6)

### Schritt 4.1: Daten-Migration
```sql
-- V3.1__migrate_roles_to_permissions.sql
-- Default permissions für Menu Items
INSERT INTO permissions (menu_item, capability, scope, description) VALUES
('customer_management', 'view_all', 'all', 'View all customers'),
('customer_management', 'view_team', 'team', 'View team customers'),
('customer_management', 'view_own', 'own', 'View own customers'),
('customer_management', 'edit_all', 'all', 'Edit all customers'),
('customer_management', 'edit_team', 'team', 'Edit team customers'),
('customer_management', 'edit_own', 'own', 'Edit own customers');

-- Migration der bestehenden Rollen
INSERT INTO user_permissions (user_id, permission_id, granted_at)
SELECT 
    u.id,
    p.id,
    CURRENT_TIMESTAMP
FROM users u
CROSS JOIN permissions p
WHERE 
    (u.role = 'admin' AND p.capability LIKE '%_all') OR
    (u.role = 'manager' AND p.capability LIKE '%_team') OR
    (u.role = 'sales' AND p.capability LIKE '%_own');
```

---

## Debugging & Troubleshooting

### Permission Check nicht erfolgreich?
```bash
# 1. Cache leeren
curl -X DELETE http://localhost:8080/api/cache/user-permissions

# 2. SQL Debug aktivieren
echo "quarkus.hibernate-orm.log.sql=true" >> src/main/resources/application.properties

# 3. Check User Permissions
psql -d freshplan -c "
SELECT u.email, p.menu_item, p.capability, p.scope 
FROM users u 
JOIN user_permissions up ON u.id = up.user_id
JOIN permissions p ON up.permission_id = p.id
WHERE u.email = 'test@example.com';
"
```

### Performance Issues?
```java
// Add Query Hint
@QueryHint(name = "org.hibernate.fetchSize", value = "50")
```

---

## Rollback Plan

```bash
# Feature Flag deaktivieren
curl -X PUT http://localhost:8080/api/config \
  -H "Content-Type: application/json" \
  -d '{"feature.new-permissions.enabled": false}'

# Oder in application.properties
echo "feature.new-permissions.enabled=false" >> src/main/resources/application.properties
```