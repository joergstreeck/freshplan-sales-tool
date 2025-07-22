# FC-009 Advanced Permissions System - Tech Concept

**Feature-Code:** FC-009  
**Feature-Name:** Advanced Permissions System  
**Feature-Typ:** 🔧 BACKEND FOUNDATION + UI  
**Erstellt:** 2025-07-20  
**Status:** Tech Concept  
**Priorität:** HIGH - Security Foundation  
**Geschätzter Aufwand:** 6-8 Tage  

---

## 🧠 CLAUDE WORKING SECTION (15-Min Context Chunk)

### 📍 Kontext laden für produktive Arbeit

**Was ist FC-009?** Granulares Permissions-System mit Resource-Based Access Control (RBAC) und Dynamic Permission Management

**Warum kritisch nach FC-008?** FC-008 bietet Basic Auth (admin/manager/sales), FC-009 erweitert zu granularer Zugriffssteuerung auf Feature-Level

**Abhängigkeiten verstehen:**
- **Benötigt:** FC-008 Security Foundation (JWT + User Context) - ✅ VERFÜGBAR
- **Blockiert:** FC-004 Verkäuferschutz (Provisionsregeln), FC-010 Customer Import (Datenfreigabe)
- **Erweitert:** M3 Sales Cockpit (Feature-Toggle), M7 Settings (Permission-Management)

**Technischer Kern - Permission Check:**
```java
@ApplicationScoped
public class PermissionService {
    public boolean hasPermission(User user, String resource, String action) {
        // Resource: "customers", Action: "read", "write", "delete"
        return checkUserPermissions(user, resource, action);
    }
}
```

**Frontend-Integration:**
```typescript
export const usePermissions = () => {
    const { hasPermission } = useQuery(['user-permissions'], permissionService.getUserPermissions);
    return { hasPermission: (resource: string, action: string) => hasPermission(resource, action) };
};
```

---

## 🎯 ÜBERSICHT

### Business Value
**Problem:** Simple 3-Rollen-System (admin/manager/sales) aus FC-008 ist nicht flexibel genug für Enterprise-Anforderungen

**Lösung:** Granulares Permission-System mit Resource-Based Access Control für flexible Zugriffssteuerung

**ROI:** 
- **Kosten:** 6-8 Entwicklertage (~€8.000)
- **Security Value:** Compliance-ready für Enterprise-Kunden (+€100.000 Potential)
- **Flexibility:** Ermöglicht Custom Roles ohne Code-Änderungen (+40% Developer Productivity)
- **ROI-Ratio:** 12.5:1 (Break-even nach 1 Monat)

### Kernfunktionen
1. **Resource-Based Permissions** - Granulare Kontrolle auf Feature-Level
2. **Dynamic Role Management** - Admin-Interface für Permission-Konfiguration
3. **Permission Inheritance** - Hierarchische Rolle-zu-Rolle Vererbung
4. **Feature Toggles** - UI-Elemente basierend auf Permissions ein/ausblenden
5. **Audit Trail** - Nachverfolgung aller Permission-Änderungen
6. **API-Level Protection** - Automatische Endpoint-Sicherung

---

## 🏗️ ARCHITEKTUR

### Permission Model Design
```
Users → Roles → Permissions → Resources
  ↓       ↓          ↓          ↓
Sarah   Manager   customer:read  Customer List
John    Sales     customer:write Customer Edit
Admin   Admin     *:*           All Resources
```

### Database Schema - Advanced Permissions
```sql
-- Resources (Features/Modules)
CREATE TABLE resources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_name VARCHAR(100) UNIQUE NOT NULL, -- 'customers', 'opportunities', 'reports'
    resource_category VARCHAR(50), -- 'core', 'analytics', 'admin'
    description TEXT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Actions (CRUD Operations)
CREATE TABLE actions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    action_name VARCHAR(50) UNIQUE NOT NULL, -- 'read', 'write', 'delete', 'export', 'import'
    description TEXT,
    severity_level INTEGER DEFAULT 1, -- 1=safe, 5=critical
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Permissions (Resource + Action combinations)
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_id UUID NOT NULL REFERENCES resources(id),
    action_id UUID NOT NULL REFERENCES actions(id),
    permission_code VARCHAR(150) UNIQUE NOT NULL, -- Generated: 'customers:read'
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(resource_id, action_id)
);

-- Roles (Enhanced from FC-008)
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_name VARCHAR(100) UNIQUE NOT NULL,
    role_code VARCHAR(50) UNIQUE NOT NULL, -- 'admin', 'manager', 'sales', 'custom_role_1'
    description TEXT,
    is_system_role BOOLEAN DEFAULT false, -- Cannot be deleted
    parent_role_id UUID REFERENCES roles(id), -- Role inheritance
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Role-Permission Assignments
CREATE TABLE role_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id UUID NOT NULL REFERENCES roles(id),
    permission_id UUID NOT NULL REFERENCES permissions(id),
    granted BOOLEAN DEFAULT true, -- Allow explicit denial
    granted_by UUID REFERENCES users(id),
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP, -- Optional expiry
    UNIQUE(role_id, permission_id)
);

-- User-Role Assignments (Enhanced from FC-008)
CREATE TABLE user_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    role_id UUID NOT NULL REFERENCES roles(id),
    assigned_by UUID REFERENCES users(id),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP, -- Optional expiry
    active BOOLEAN DEFAULT true,
    UNIQUE(user_id, role_id)
);

-- Direct User Permissions (Override system)
CREATE TABLE user_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    permission_id UUID NOT NULL REFERENCES permissions(id),
    granted BOOLEAN DEFAULT true, -- Can override role permissions
    granted_by UUID REFERENCES users(id),
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    reason TEXT, -- Why this override was needed
    UNIQUE(user_id, permission_id)
);

-- Permission Audit Log
CREATE TABLE permission_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    target_user_id UUID REFERENCES users(id), -- User being affected
    permission_id UUID REFERENCES permissions(id),
    role_id UUID REFERENCES roles(id),
    action_performed VARCHAR(50) NOT NULL, -- 'grant', 'revoke', 'check'
    old_value JSONB, -- Previous state
    new_value JSONB, -- New state
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_role_permissions_role ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission ON role_permissions(permission_id);
CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);
CREATE INDEX idx_user_permissions_user ON user_permissions(user_id);
CREATE INDEX idx_permissions_code ON permissions(permission_code);
CREATE INDEX idx_permission_audit_user ON permission_audit_log(user_id);
CREATE INDEX idx_permission_audit_target ON permission_audit_log(target_user_id);
```

### Backend-Architecture - Advanced Permission Service
```java
// Core Permission Service
@ApplicationScoped
@Transactional
public class PermissionService {
    
    @Inject UserRepository userRepository;
    @Inject RoleRepository roleRepository;
    @Inject PermissionRepository permissionRepository;
    @Inject PermissionAuditService auditService;
    @Inject SecurityContextProvider securityProvider;
    
    public boolean hasPermission(User user, String permissionCode) {
        try {
            // Check user direct permissions first (overrides)
            Optional<UserPermission> directPermission = permissionRepository
                .findUserDirectPermission(user.getId(), permissionCode);
            
            if (directPermission.isPresent()) {
                auditService.logPermissionCheck(user, permissionCode, "direct", directPermission.get().isGranted());
                return directPermission.get().isGranted();
            }
            
            // Check role-based permissions
            List<Role> userRoles = roleRepository.findActiveRolesByUser(user.getId());
            boolean hasRolePermission = userRoles.stream()
                .anyMatch(role -> hasRolePermission(role, permissionCode));
            
            auditService.logPermissionCheck(user, permissionCode, "role", hasRolePermission);
            return hasRolePermission;
            
        } catch (Exception e) {
            auditService.logPermissionCheck(user, permissionCode, "error", false);
            return false; // Deny on error
        }
    }
    
    public boolean hasPermission(String resource, String action) {
        User currentUser = securityProvider.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("No authenticated user"));
        
        String permissionCode = resource + ":" + action;
        return hasPermission(currentUser, permissionCode);
    }
    
    private boolean hasRolePermission(Role role, String permissionCode) {
        // Check direct role permissions
        boolean hasDirectPermission = permissionRepository
            .hasRolePermission(role.getId(), permissionCode);
        
        if (hasDirectPermission) {
            return true;
        }
        
        // Check inherited permissions from parent roles
        if (role.getParentRole() != null) {
            return hasRolePermission(role.getParentRole(), permissionCode);
        }
        
        return false;
    }
    
    public List<Permission> getUserPermissions(UUID userId) {
        User user = userRepository.findByIdOrThrow(userId);
        
        // Combine role permissions and direct permissions
        Set<Permission> allPermissions = new HashSet<>();
        
        // Add role-based permissions
        List<Role> userRoles = roleRepository.findActiveRolesByUser(userId);
        for (Role role : userRoles) {
            allPermissions.addAll(getRolePermissions(role));
        }
        
        // Add/Remove direct user permissions (overrides)
        List<UserPermission> directPermissions = permissionRepository
            .findDirectUserPermissions(userId);
        
        for (UserPermission userPerm : directPermissions) {
            if (userPerm.isGranted()) {
                allPermissions.add(userPerm.getPermission());
            } else {
                allPermissions.remove(userPerm.getPermission());
            }
        }
        
        return new ArrayList<>(allPermissions);
    }
    
    private Set<Permission> getRolePermissions(Role role) {
        Set<Permission> permissions = new HashSet<>();
        
        // Direct role permissions
        permissions.addAll(permissionRepository.findPermissionsByRole(role.getId()));
        
        // Inherited permissions
        if (role.getParentRole() != null) {
            permissions.addAll(getRolePermissions(role.getParentRole()));
        }
        
        return permissions;
    }
    
    public void grantPermission(UUID userId, String permissionCode, String reason) {
        User currentUser = securityProvider.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("No authenticated user"));
        
        User targetUser = userRepository.findByIdOrThrow(userId);
        Permission permission = permissionRepository.findByCode(permissionCode)
            .orElseThrow(() -> new NotFoundException("Permission not found: " + permissionCode));
        
        // Check if current user can grant this permission
        if (!canGrantPermission(currentUser, permission)) {
            throw new ForbiddenException("Cannot grant permission: " + permissionCode);
        }
        
        // Create or update user permission
        UserPermission userPermission = UserPermission.builder()
            .userId(userId)
            .permissionId(permission.getId())
            .granted(true)
            .grantedBy(currentUser.getId())
            .reason(reason)
            .build();
        
        permissionRepository.saveUserPermission(userPermission);
        
        auditService.logPermissionGrant(currentUser, targetUser, permission, reason);
    }
    
    public void revokePermission(UUID userId, String permissionCode, String reason) {
        User currentUser = securityProvider.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("No authenticated user"));
        
        User targetUser = userRepository.findByIdOrThrow(userId);
        Permission permission = permissionRepository.findByCode(permissionCode)
            .orElseThrow(() -> new NotFoundException("Permission not found: " + permissionCode));
        
        // Check if current user can revoke this permission
        if (!canRevokePermission(currentUser, permission)) {
            throw new ForbiddenException("Cannot revoke permission: " + permissionCode);
        }
        
        // Create explicit denial
        UserPermission userPermission = UserPermission.builder()
            .userId(userId)
            .permissionId(permission.getId())
            .granted(false)
            .grantedBy(currentUser.getId())
            .reason(reason)
            .build();
        
        permissionRepository.saveUserPermission(userPermission);
        
        auditService.logPermissionRevoke(currentUser, targetUser, permission, reason);
    }
    
    private boolean canGrantPermission(User granter, Permission permission) {
        // Admin can grant any permission
        if (granter.getRole().equals("admin")) {
            return true;
        }
        
        // Managers can grant non-admin permissions
        if (granter.getRole().equals("manager")) {
            return !permission.getPermissionCode().contains("admin:");
        }
        
        return false;
    }
    
    private boolean canRevokePermission(User revoker, Permission permission) {
        return canGrantPermission(revoker, permission);
    }
}

// Permission-based Authorization Interceptor
@Interceptor
@PermissionRequired
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 200)
public class PermissionInterceptor {
    
    @Inject PermissionService permissionService;
    
    @AroundInvoke
    public Object checkPermission(InvocationContext context) throws Exception {
        Method method = context.getMethod();
        PermissionRequired annotation = method.getAnnotation(PermissionRequired.class);
        
        if (annotation == null) {
            annotation = method.getDeclaringClass().getAnnotation(PermissionRequired.class);
        }
        
        if (annotation != null) {
            String requiredPermission = annotation.value();
            
            if (!permissionService.hasPermission(requiredPermission)) {
                throw new ForbiddenException("Missing required permission: " + requiredPermission);
            }
        }
        
        return context.proceed();
    }
}

// Custom Permission Annotation
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@InterceptorBinding
public @interface PermissionRequired {
    String value(); // Permission code like "customers:read"
}

// Enhanced API Resources with Permissions
@Path("/api/customers")
@ApplicationScoped
public class CustomerResource {
    
    @Inject CustomerService customerService;
    
    @GET
    @PermissionRequired("customers:read")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        return Response.ok(customers).build();
    }
    
    @POST
    @PermissionRequired("customers:write")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCustomer(CustomerRequest request) {
        Customer customer = customerService.createCustomer(request);
        return Response.status(Response.Status.CREATED).entity(customer).build();
    }
    
    @DELETE
    @Path("/{id}")
    @PermissionRequired("customers:delete")
    public Response deleteCustomer(@PathParam("id") UUID id) {
        customerService.deleteCustomer(id);
        return Response.noContent().build();
    }
    
    @GET
    @Path("/export")
    @PermissionRequired("customers:export")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response exportCustomers() {
        byte[] excelData = customerService.exportToExcel();
        return Response.ok(excelData)
            .header("Content-Disposition", "attachment; filename=customers.xlsx")
            .build();
    }
}

// Permission Repository
@ApplicationScoped
public class PermissionRepository implements PanacheRepositoryBase<Permission, UUID> {
    
    public Optional<Permission> findByCode(String permissionCode) {
        return find("permission_code", permissionCode).firstResultOptional();
    }
    
    public List<Permission> findPermissionsByRole(UUID roleId) {
        return getEntityManager()
            .createQuery("""
                SELECT p FROM Permission p 
                JOIN RolePermission rp ON p.id = rp.permission.id 
                WHERE rp.role.id = :roleId AND rp.granted = true
                """, Permission.class)
            .setParameter("roleId", roleId)
            .getResultList();
    }
    
    public boolean hasRolePermission(UUID roleId, String permissionCode) {
        Long count = getEntityManager()
            .createQuery("""
                SELECT COUNT(rp) FROM RolePermission rp 
                JOIN rp.permission p 
                WHERE rp.role.id = :roleId 
                AND p.permissionCode = :permissionCode 
                AND rp.granted = true
                """, Long.class)
            .setParameter("roleId", roleId)
            .setParameter("permissionCode", permissionCode)
            .getSingleResult();
            
        return count > 0;
    }
    
    public Optional<UserPermission> findUserDirectPermission(UUID userId, String permissionCode) {
        return getEntityManager()
            .createQuery("""
                SELECT up FROM UserPermission up 
                JOIN up.permission p 
                WHERE up.user.id = :userId 
                AND p.permissionCode = :permissionCode
                """, UserPermission.class)
            .setParameter("userId", userId)
            .setParameter("permissionCode", permissionCode)
            .getResultStream()
            .findFirst();
    }
    
    public List<UserPermission> findDirectUserPermissions(UUID userId) {
        return getEntityManager()
            .createQuery("""
                SELECT up FROM UserPermission up 
                WHERE up.user.id = :userId 
                AND (up.expiresAt IS NULL OR up.expiresAt > CURRENT_TIMESTAMP)
                """, UserPermission.class)
            .setParameter("userId", userId)
            .getResultList();
    }
    
    public void saveUserPermission(UserPermission userPermission) {
        // Check if permission already exists
        Optional<UserPermission> existing = findUserDirectPermission(
            userPermission.getUserId(), 
            userPermission.getPermission().getPermissionCode()
        );
        
        if (existing.isPresent()) {
            // Update existing
            UserPermission existingPerm = existing.get();
            existingPerm.setGranted(userPermission.isGranted());
            existingPerm.setGrantedBy(userPermission.getGrantedBy());
            existingPerm.setGrantedAt(LocalDateTime.now());
            existingPerm.setReason(userPermission.getReason());
            persist(existingPerm);
        } else {
            // Create new
            persist(userPermission);
        }
    }
}
```

### Frontend-Architecture - Permission-Aware UI
```typescript
// Permission Service
class PermissionService {
    private permissions: Set<string> = new Set();
    
    async loadUserPermissions(): Promise<void> {
        try {
            const response = await apiClient.get('/api/permissions/me');
            this.permissions = new Set(response.data.permissions);
        } catch (error) {
            console.error('Failed to load permissions:', error);
            this.permissions = new Set(); // Empty permissions on error
        }
    }
    
    hasPermission(permissionCode: string): boolean {
        return this.permissions.has(permissionCode) || this.permissions.has('*:*');
    }
    
    hasAnyPermission(permissionCodes: string[]): boolean {
        return permissionCodes.some(code => this.hasPermission(code));
    }
    
    hasAllPermissions(permissionCodes: string[]): boolean {
        return permissionCodes.every(code => this.hasPermission(code));
    }
    
    getPermissionsForResource(resource: string): string[] {
        return Array.from(this.permissions)
            .filter(permission => permission.startsWith(`${resource}:`));
    }
}

// Permission Context Provider
interface PermissionContextType {
    permissions: Set<string>;
    hasPermission: (permissionCode: string) => boolean;
    hasAnyPermission: (permissionCodes: string[]) => boolean;
    hasAllPermissions: (permissionCodes: string[]) => boolean;
    isLoading: boolean;
    refreshPermissions: () => Promise<void>;
}

const PermissionContext = createContext<PermissionContextType | undefined>(undefined);

export const PermissionProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [permissions, setPermissions] = useState<Set<string>>(new Set());
    const [isLoading, setIsLoading] = useState(true);
    const { user } = useAuth();
    const permissionService = useRef(new PermissionService()).current;
    
    useEffect(() => {
        if (user) {
            loadPermissions();
        }
    }, [user]);
    
    const loadPermissions = async () => {
        setIsLoading(true);
        try {
            await permissionService.loadUserPermissions();
            setPermissions(new Set(permissionService.permissions));
        } catch (error) {
            console.error('Failed to load permissions:', error);
            setPermissions(new Set());
        } finally {
            setIsLoading(false);
        }
    };
    
    const hasPermission = useCallback((permissionCode: string): boolean => {
        return permissionService.hasPermission(permissionCode);
    }, [permissions]);
    
    const hasAnyPermission = useCallback((permissionCodes: string[]): boolean => {
        return permissionService.hasAnyPermission(permissionCodes);
    }, [permissions]);
    
    const hasAllPermissions = useCallback((permissionCodes: string[]): boolean => {
        return permissionService.hasAllPermissions(permissionCodes);
    }, [permissions]);
    
    const contextValue: PermissionContextType = {
        permissions,
        hasPermission,
        hasAnyPermission,
        hasAllPermissions,
        isLoading,
        refreshPermissions: loadPermissions
    };
    
    return (
        <PermissionContext.Provider value={contextValue}>
            {children}
        </PermissionContext.Provider>
    );
};

// Permission Hook
export const usePermissions = (): PermissionContextType => {
    const context = useContext(PermissionContext);
    if (!context) {
        throw new Error('usePermissions must be used within PermissionProvider');
    }
    return context;
};

// Permission-Protected Component
interface PermissionGateProps {
    permission: string;
    permissions?: string[]; // Multiple permissions (OR logic)
    requireAll?: boolean; // AND logic for multiple permissions
    children: React.ReactNode;
    fallback?: React.ReactNode;
    showFallback?: boolean;
}

export const PermissionGate: React.FC<PermissionGateProps> = ({
    permission,
    permissions,
    requireAll = false,
    children,
    fallback = null,
    showFallback = false
}) => {
    const { hasPermission, hasAnyPermission, hasAllPermissions, isLoading } = usePermissions();
    
    if (isLoading) {
        return <Skeleton variant="rectangular" width="100%" height={40} />;
    }
    
    let hasAccess = false;
    
    if (permissions) {
        hasAccess = requireAll ? hasAllPermissions(permissions) : hasAnyPermission(permissions);
    } else if (permission) {
        hasAccess = hasPermission(permission);
    }
    
    if (hasAccess) {
        return <>{children}</>;
    }
    
    return showFallback ? <>{fallback}</> : null;
};

// Permission-Aware Button Component
interface PermissionButtonProps extends ButtonProps {
    permission: string;
    permissions?: string[];
    requireAll?: boolean;
}

export const PermissionButton: React.FC<PermissionButtonProps> = ({
    permission,
    permissions,
    requireAll = false,
    children,
    disabled = false,
    ...buttonProps
}) => {
    const { hasPermission, hasAnyPermission, hasAllPermissions } = usePermissions();
    
    let hasAccess = false;
    
    if (permissions) {
        hasAccess = requireAll ? hasAllPermissions(permissions) : hasAnyPermission(permissions);
    } else if (permission) {
        hasAccess = hasPermission(permission);
    }
    
    if (!hasAccess) {
        return null; // Hide button completely
    }
    
    return (
        <Button disabled={disabled} {...buttonProps}>
            {children}
        </Button>
    );
};

// Customer List with Permissions
export const CustomerListWithPermissions: React.FC = () => {
    const { data: customers, isLoading } = useCustomers();
    const { hasPermission } = usePermissions();
    
    if (isLoading) return <CircularProgress />;
    
    return (
        <Paper sx={{ p: 2 }}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant="h6">Kunden</Typography>
                
                <Box>
                    <PermissionButton
                        permission="customers:write"
                        variant="contained"
                        startIcon={<AddIcon />}
                        onClick={() => navigate('/customers/new')}
                    >
                        Neuer Kunde
                    </PermissionButton>
                    
                    <PermissionButton
                        permission="customers:export"
                        variant="outlined"
                        startIcon={<DownloadIcon />}
                        sx={{ ml: 1 }}
                        onClick={handleExport}
                    >
                        Exportieren
                    </PermissionButton>
                </Box>
            </Box>
            
            <DataGrid
                rows={customers}
                columns={[
                    { field: 'name', headerName: 'Name', width: 200 },
                    { field: 'email', headerName: 'E-Mail', width: 250 },
                    { field: 'phone', headerName: 'Telefon', width: 150 },
                    {
                        field: 'actions',
                        headerName: 'Aktionen',
                        width: 200,
                        renderCell: (params) => (
                            <Box>
                                <PermissionGate permission="customers:read">
                                    <IconButton onClick={() => navigate(`/customers/${params.row.id}`)}>
                                        <VisibilityIcon />
                                    </IconButton>
                                </PermissionGate>
                                
                                <PermissionGate permission="customers:write">
                                    <IconButton onClick={() => navigate(`/customers/${params.row.id}/edit`)}>
                                        <EditIcon />
                                    </IconButton>
                                </PermissionGate>
                                
                                <PermissionGate permission="customers:delete">
                                    <IconButton 
                                        color="error"
                                        onClick={() => handleDelete(params.row.id)}
                                    >
                                        <DeleteIcon />
                                    </IconButton>
                                </PermissionGate>
                            </Box>
                        )
                    }
                ]}
                pageSize={25}
                disableSelectionOnClick
            />
        </Paper>
    );
};

// Admin Permission Management Interface
export const PermissionManagementDashboard: React.FC = () => {
    const { data: users } = useUsers();
    const { data: roles } = useRoles();
    const { data: permissions } = useAllPermissions();
    const [selectedUser, setSelectedUser] = useState<User | null>(null);
    
    return (
        <PermissionGate permission="admin:permissions" showFallback>
            <Grid container spacing={3}>
                <Grid item xs={12} md={4}>
                    <Paper sx={{ p: 2 }}>
                        <Typography variant="h6" gutterBottom>
                            Benutzer
                        </Typography>
                        <UserList 
                            users={users}
                            onUserSelect={setSelectedUser}
                            selectedUser={selectedUser}
                        />
                    </Paper>
                </Grid>
                
                <Grid item xs={12} md={8}>
                    {selectedUser && (
                        <Paper sx={{ p: 2 }}>
                            <Typography variant="h6" gutterBottom>
                                Berechtigungen für {selectedUser.firstName} {selectedUser.lastName}
                            </Typography>
                            
                            <UserPermissionEditor
                                user={selectedUser}
                                roles={roles}
                                permissions={permissions}
                                onPermissionChange={handlePermissionChange}
                            />
                        </Paper>
                    )}
                </Grid>
            </Grid>
        </PermissionGate>
    );
};
```

---

## 🔄 ABHÄNGIGKEITEN

### Benötigt diese Features:
- **FC-008 Security Foundation** - JWT Auth und User Context ✅ VERFÜGBAR
- **User Management** - Erweitert bestehende User-Tabelle aus FC-008

### Ermöglicht diese Features:
- **FC-004 Verkäuferschutz** - Granulare Permissions für Provisionsregeln
- **FC-010 Customer Import** - Datenfreigabe-Kontrolle für Import-Operationen
- **M3 Sales Cockpit** - Feature-Toggles basierend auf User-Permissions
- **M7 Settings** - Permission-Management Interface für Admins

### Integriert mit:
- **ALLE API-Endpoints** - Automatische Permission-Checks via @PermissionRequired
- **ALLE Frontend-Features** - UI-Elemente ein-/ausblenden basierend auf Permissions
- **FC-023 Event Sourcing** - Audit Trail für Permission-Änderungen

---

## 🧪 TESTING-STRATEGIE

### Unit Tests - Permission Service
```java
@QuarkusTest
class PermissionServiceTest {
    
    @Test
    void testHasPermission_withDirectUserPermission_shouldReturnTrue() {
        // Given
        User user = createTestUser("sales");
        String permissionCode = "customers:read";
        createDirectUserPermission(user, permissionCode, true);
        
        // When
        boolean result = permissionService.hasPermission(user, permissionCode);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void testHasPermission_withRoleInheritance_shouldReturnTrue() {
        // Given
        User user = createTestUser("manager");
        Role managerRole = createRole("manager", "sales"); // manager inherits from sales
        String permissionCode = "customers:read";
        grantRolePermission("sales", permissionCode);
        
        // When
        boolean result = permissionService.hasPermission(user, permissionCode);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void testHasPermission_withExplicitDenial_shouldReturnFalse() {
        // Given
        User user = createTestUser("manager");
        grantRolePermission("manager", "customers:read");
        createDirectUserPermission(user, "customers:read", false); // Explicit denial
        
        // When
        boolean result = permissionService.hasPermission(user, "customers:read");
        
        // Then
        assertThat(result).isFalse();
    }
}
```

### Integration Tests - Permission Endpoints
```java
@QuarkusTest
@TestSecurity(user = "testuser", roles = {"admin"})
class PermissionIntegrationTest {
    
    @Test
    void testCustomersEndpoint_withReadPermission_shouldReturn200() {
        grantUserPermission("testuser", "customers:read");
        
        given()
            .when().get("/api/customers")
            .then()
                .statusCode(200);
    }
    
    @Test
    @TestSecurity(user = "salesuser", roles = {"sales"})
    void testCustomersDelete_withoutDeletePermission_shouldReturn403() {
        // Sales user doesn't have delete permission
        
        given()
            .when().delete("/api/customers/test-id")
            .then()
                .statusCode(403);
    }
}
```

### Frontend Tests
```typescript
describe('PermissionGate', () => {
    it('should show children when user has permission', () => {
        const mockPermissions = {
            hasPermission: jest.fn().mockReturnValue(true)
        };
        
        jest.mocked(usePermissions).mockReturnValue(mockPermissions);
        
        render(
            <PermissionGate permission="customers:read">
                <div>Protected Content</div>
            </PermissionGate>
        );
        
        expect(screen.getByText('Protected Content')).toBeInTheDocument();
    });
    
    it('should hide children when user lacks permission', () => {
        const mockPermissions = {
            hasPermission: jest.fn().mockReturnValue(false)
        };
        
        jest.mocked(usePermissions).mockReturnValue(mockPermissions);
        
        render(
            <PermissionGate permission="customers:delete">
                <div>Protected Content</div>
            </PermissionGate>
        );
        
        expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
    });
});
```

---

## 🚀 IMPLEMENTATION GUIDE

### Phase 1: Backend Foundation (3 Tage)
1. **Database Schema** - Erweiterte Permission-Tabellen
2. **Core Permission Service** - RBAC-Logik und Permission-Checks
3. **Permission Interceptor** - Automatische API-Endpoint-Sicherung
4. **Data Migration** - Bestehende Rollen zu neuem System migrieren

### Phase 2: Frontend Integration (2 Tage)
1. **Permission Context** - React Context für Permission-Management
2. **PermissionGate Component** - UI-Element Ein-/Ausblendung
3. **Permission-Aware Components** - Buttons, Menus, Listen
4. **User Experience** - Graceful Degradation bei fehlenden Permissions

### Phase 3: Admin Interface (2 Tage)
1. **Permission Management Dashboard** - Admin-Interface für Permission-Zuweisung
2. **Role Management** - Rollen erstellen, bearbeiten, zuweisen
3. **User Permission Editor** - Granulare User-Permission-Kontrolle
4. **Audit Trail UI** - Permission-Änderungen nachverfolgen

### Phase 4: Advanced Features (1 Tag)
1. **Permission Inheritance** - Hierarchische Rolle-zu-Rolle Vererbung
2. **Temporary Permissions** - Zeitlich begrenzte Zugriffe
3. **Permission Groups** - Logische Gruppierung von Permissions
4. **API Documentation** - OpenAPI-Spezifikation für Permission-Endpoints

---

## 📊 SUCCESS CRITERIA

### Funktionale Anforderungen
- ✅ Granulare Resource-based Permissions (customers:read, customers:write, etc.)
- ✅ Role-based Permission Management mit Inheritance
- ✅ Direct User Permission Overrides
- ✅ Admin Interface für Permission-Management
- ✅ Frontend UI Permission-Awareness
- ✅ Complete Audit Trail für Compliance

### Performance-Anforderungen
- ✅ Permission Check < 10ms P95
- ✅ User Permission Load < 50ms P95
- ✅ Admin Dashboard < 2 Sekunden Loading
- ✅ Real-time Permission Updates < 1 Sekunde
- ✅ Support für 1000+ Concurrent Permission Checks

### Security-Anforderungen
- ✅ Secure Permission Storage mit Encryption
- ✅ Fail-Safe Defaults (Deny by Default)
- ✅ Permission Escalation Protection
- ✅ Audit Trail für alle Permission-Änderungen
- ✅ Role Hierarchy Validation

---

## 🔗 NAVIGATION ZU ALLEN 40 FEATURES

### Core Sales Features
- [FC-001 Customer Acquisition](/docs/features/PLANNED/01_customer_acquisition/FC-001_TECH_CONCEPT.md) | [FC-002 Smart Customer Insights](/docs/features/PLANNED/02_smart_insights/FC-002_TECH_CONCEPT.md) | [M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md) | [M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_KOMPAKT.md)
- [FC-004 Verkäuferschutz](/docs/features/PLANNED/07_verkaeuferschutz/FC-004_TECH_CONCEPT.md) | [FC-013 Duplicate Detection](/docs/features/PLANNED/15_duplicate_detection/FC-013_TECH_CONCEPT.md) | [FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_TECH_CONCEPT.md)
- [FC-015 Deal Loss Analysis](/docs/features/PLANNED/17_deal_loss_analysis/FC-015_TECH_CONCEPT.md) | [FC-016 Opportunity Cloning](/docs/features/PLANNED/18_opportunity_cloning/FC-016_TECH_CONCEPT.md) | [FC-019 Advanced Sales Metrics](/docs/features/PLANNED/19_advanced_metrics/FC-019_TECH_CONCEPT.md)

### Security & Foundation Features  
- [FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_TECH_CONCEPT.md) | [FC-025 DSGVO Compliance](/docs/features/PLANNED/25_dsgvo_compliance/FC-025_TECH_CONCEPT.md) | [FC-023 Event Sourcing](/docs/features/PLANNED/23_event_sourcing/FC-023_TECH_CONCEPT.md)
- [FC-038 Multi-Tenant Architecture](/docs/features/PLANNED/38_multitenant/FC-038_TECH_CONCEPT.md) | [FC-039 API Gateway](/docs/features/PLANNED/39_api_gateway/FC-039_TECH_CONCEPT.md) | [FC-040 Performance Monitoring](/docs/features/PLANNED/40_performance_monitoring/FC-040_TECH_CONCEPT.md)

### Mobile & Field Service Features
- [FC-006 Mobile App](/docs/features/PLANNED/09_mobile_app/FC-006_TECH_CONCEPT.md) | [FC-018 Mobile PWA](/docs/features/PLANNED/09_mobile_app/FC-018_MOBILE_FIELD_SALES.md) | [FC-022 Mobile Light](/docs/features/PLANNED/22_mobile_light/FC-022_TECH_CONCEPT.md)
- [FC-029 Voice-First Interface](/docs/features/PLANNED/29_voice_first/FC-029_TECH_CONCEPT.md) | [FC-030 One-Tap Actions](/docs/features/PLANNED/30_one_tap_actions/FC-030_TECH_CONCEPT.md) | [FC-032 Offline-First](/docs/features/PLANNED/32_offline_first/FC-032_TECH_CONCEPT.md)

### Communication Features
- [FC-003 E-Mail Integration](/docs/features/PLANNED/06_email_integration/FC-003_TECH_CONCEPT.md) | [FC-012 Team Communication](/docs/features/PLANNED/14_team_communication/FC-012_TECH_CONCEPT.md) | [FC-028 WhatsApp Business](/docs/features/PLANNED/28_whatsapp_integration/FC-028_TECH_CONCEPT.md)
- [FC-035 Social Selling Helper](/docs/features/PLANNED/35_social_selling/FC-035_TECH_CONCEPT.md)

### Analytics & Intelligence Features
- [M6 Analytics Module](/docs/features/PLANNED/13_analytics_m6/M6_KOMPAKT.md) | [FC-007 Chef-Dashboard](/docs/features/PLANNED/10_chef_dashboard/FC-007_TECH_CONCEPT.md) | [FC-026 Analytics Platform](/docs/features/PLANNED/26_analytics_platform/FC-026_TECH_CONCEPT.md)
- [FC-034 Instant Insights](/docs/features/PLANNED/34_instant_insights/FC-034_TECH_CONCEPT.md) | [FC-037 Advanced Reporting](/docs/features/PLANNED/37_advanced_reporting/FC-037_TECH_CONCEPT.md)

### Integration & Infrastructure Features
- [FC-005 Xentral Integration](/docs/features/PLANNED/08_xentral_integration/FC-005_TECH_CONCEPT.md) | [FC-010 Customer Import](/docs/features/PLANNED/11_customer_import/FC-010_KOMPAKT.md) | [FC-021 Integration Hub](/docs/features/PLANNED/21_integration_hub/FC-021_TECH_CONCEPT.md)
- [FC-024 File Management](/docs/features/PLANNED/24_file_management/FC-024_TECH_CONCEPT.md) | [M5 Customer Refactor](/docs/features/PLANNED/12_customer_refactor_m5/M5_KOMPAKT.md)

### UI & Productivity Features
- [M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_NAVIGATION_KOMPAKT.md) | [M2 Quick Create](/docs/features/ACTIVE/05_ui_foundation/M2_QUICK_CREATE_KOMPAKT.md) | [M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_KOMPAKT.md)
- [FC-020 Quick Wins](/docs/features/PLANNED/20_quick_wins/FC-020_TECH_CONCEPT.md) | [FC-031 Smart Templates](/docs/features/PLANNED/31_smart_templates/FC-031_TECH_CONCEPT.md) | [FC-033 Visual Customer Cards](/docs/features/PLANNED/33_visual_cards/FC-033_TECH_CONCEPT.md)

### Advanced Features
- [FC-011 Bonitätsprüfung](/docs/features/ACTIVE/02_opportunity_pipeline/integrations/FC-011_KOMPAKT.md) | [FC-017 Sales Gamification](/docs/features/PLANNED/99_sales_gamification/FC-017_KOMPAKT.md) | [FC-027 Magic Moments](/docs/features/PLANNED/27_magic_moments/FC-027_TECH_CONCEPT.md)
- [FC-036 Beziehungsmanagement](/docs/features/PLANNED/36_relationship_mgmt/FC-036_TECH_CONCEPT.md)

---

**🔐 FC-009 Advanced Permissions System - Ready for Implementation!**  
**⏱️ Geschätzter Aufwand:** 6-8 Tage | **ROI:** 12.5:1 | **Priorität:** HIGH