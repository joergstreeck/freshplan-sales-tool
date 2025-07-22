# FC-009 Advanced Permissions System - CLAUDE TECH 🤖

## 🧠 QUICK-LOAD (30 Sekunden bis zur Produktivität)

**Feature:** RBAC mit Resource-based Permissions + Inheritance + Admin UI  
**Stack:** Quarkus + JPA + React Permission Context + MUI Admin Dashboard  
**Status:** 📋 Ready for Implementation - Backend-Frontend Integration  
**Dependencies:** FC-008 Security (✅ verfügbar) | Erweitert: User Management  

**Jump to:** [📚 Recipes](#-implementation-recipes) | [🧪 Tests](#-test-patterns) | [🔌 Integration](#-integration-cookbook) | [🎯 Admin UI](#-admin-ui-recipes)

**Core Purpose in 1 Line:** `Check Permission → Show/Hide UI → Secure API → Audit Changes`

---

## 🍳 IMPLEMENTATION RECIPES

### Recipe 1: Backend Permission Service in 5 Minuten
```java
// 1. Core Permission Service (copy-paste ready)
@ApplicationScoped
@Transactional
public class PermissionService {
    @Inject PermissionRepository permissionRepository;
    @Inject SecurityContextProvider securityProvider;
    @Inject AuditService auditService;
    
    public boolean hasPermission(String resource, String action) {
        User currentUser = securityProvider.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("No authenticated user"));
        
        String permissionCode = resource + ":" + action;
        return hasPermission(currentUser, permissionCode);
    }
    
    public boolean hasPermission(User user, String permissionCode) {
        try {
            // 1. Check direct user permissions (overrides)
            Optional<UserPermission> directPermission = permissionRepository
                .findUserDirectPermission(user.getId(), permissionCode);
            
            if (directPermission.isPresent()) {
                auditService.logPermissionCheck(user, permissionCode, "direct", 
                    directPermission.get().isGranted());
                return directPermission.get().isGranted();
            }
            
            // 2. Check role-based permissions
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
}

// 2. Permission Interceptor (auto-secure all endpoints)
@Interceptor
@PermissionRequired
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 200)
public class PermissionInterceptor {
    @Inject PermissionService permissionService;
    
    @AroundInvoke
    public Object checkPermission(InvocationContext context) throws Exception {
        PermissionRequired annotation = context.getMethod()
            .getAnnotation(PermissionRequired.class);
        
        if (annotation != null && !permissionService.hasPermission(annotation.value())) {
            throw new ForbiddenException("Missing permission: " + annotation.value());
        }
        
        return context.proceed();
    }
}

// 3. Permission Annotation
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@InterceptorBinding
public @interface PermissionRequired {
    String value(); // "customers:read", "customers:write", etc.
}
```

### Recipe 2: Frontend Permission Context
```typescript
// 4. Permission Context Provider (copy-paste ready)
interface PermissionContextType {
    hasPermission: (permissionCode: string) => boolean;
    hasAnyPermission: (permissionCodes: string[]) => boolean;
    hasAllPermissions: (permissionCodes: string[]) => boolean;
    permissions: Set<string>;
    isLoading: boolean;
}

const PermissionContext = createContext<PermissionContextType | undefined>(undefined);

export const PermissionProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [permissions, setPermissions] = useState<Set<string>>(new Set());
    const [isLoading, setIsLoading] = useState(true);
    const { user } = useAuth();
    
    useEffect(() => {
        if (user) {
            loadUserPermissions();
        }
    }, [user]);
    
    const loadUserPermissions = async () => {
        try {
            const response = await apiClient.get('/api/permissions/me');
            setPermissions(new Set(response.data.permissions));
        } catch (error) {
            console.error('Failed to load permissions:', error);
            setPermissions(new Set());
        } finally {
            setIsLoading(false);
        }
    };
    
    const hasPermission = useCallback((permissionCode: string): boolean => {
        return permissions.has(permissionCode) || permissions.has('*:*');
    }, [permissions]);
    
    const hasAnyPermission = useCallback((permissionCodes: string[]): boolean => {
        return permissionCodes.some(code => hasPermission(code));
    }, [hasPermission]);
    
    const hasAllPermissions = useCallback((permissionCodes: string[]): boolean => {
        return permissionCodes.every(code => hasPermission(code));
    }, [hasPermission]);
    
    return (
        <PermissionContext.Provider value={{
            hasPermission,
            hasAnyPermission,
            hasAllPermissions,
            permissions,
            isLoading
        }}>
            {children}
        </PermissionContext.Provider>
    );
};

export const usePermissions = () => {
    const context = useContext(PermissionContext);
    if (!context) throw new Error('usePermissions must be used within PermissionProvider');
    return context;
};
```

### Recipe 3: Permission-Aware Components
```typescript
// 5. PermissionGate Component (copy-paste ready)
export const PermissionGate: React.FC<{
    permission: string;
    children: React.ReactNode;
    fallback?: React.ReactNode;
}> = ({ permission, children, fallback = null }) => {
    const { hasPermission, isLoading } = usePermissions();
    
    if (isLoading) return <Skeleton variant="rectangular" height={40} />;
    
    return hasPermission(permission) ? <>{children}</> : <>{fallback}</>;
};

// 6. PermissionButton Component
export const PermissionButton: React.FC<
    ButtonProps & { permission: string }
> = ({ permission, children, ...props }) => {
    const { hasPermission } = usePermissions();
    
    if (!hasPermission(permission)) return null;
    
    return <Button {...props}>{children}</Button>;
};

// 7. Usage in Customer List
export const CustomerList: React.FC = () => {
    return (
        <Box>
            <Box display="flex" justifyContent="space-between" mb={2}>
                <Typography variant="h6">Kunden</Typography>
                
                <PermissionButton
                    permission="customers:write"
                    variant="contained"
                    startIcon={<AddIcon />}
                    onClick={() => navigate('/customers/new')}
                >
                    Neuer Kunde
                </PermissionButton>
            </Box>
            
            <DataGrid
                columns={[
                    { field: 'name', headerName: 'Name' },
                    {
                        field: 'actions',
                        headerName: 'Aktionen',
                        renderCell: (params) => (
                            <Box>
                                <PermissionGate permission="customers:write">
                                    <IconButton onClick={() => handleEdit(params.row.id)}>
                                        <EditIcon />
                                    </IconButton>
                                </PermissionGate>
                                
                                <PermissionGate permission="customers:delete">
                                    <IconButton onClick={() => handleDelete(params.row.id)}>
                                        <DeleteIcon />
                                    </IconButton>
                                </PermissionGate>
                            </Box>
                        )
                    }
                ]}
            />
        </Box>
    );
};
```

---

## 🧪 TEST PATTERNS

### Pattern 1: Backend Permission Tests
```java
@QuarkusTest
class PermissionServiceTest {
    @Test
    void testDirectUserPermissionOverridesRole() {
        // Given
        User user = createTestUser("manager");
        grantRolePermission("manager", "customers:read");
        createDirectUserPermission(user, "customers:read", false); // Explicit deny
        
        // When
        boolean result = permissionService.hasPermission(user, "customers:read");
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void testRoleInheritance() {
        // Given
        Role admin = createRole("admin", "manager"); // admin inherits manager
        User user = createTestUser("admin");
        grantRolePermission("manager", "customers:read");
        
        // When
        boolean result = permissionService.hasPermission(user, "customers:read");
        
        // Then
        assertThat(result).isTrue();
    }
}
```

### Pattern 2: Frontend Permission Tests
```typescript
describe('PermissionGate', () => {
    it('should show content when permission granted', () => {
        mockUsePermissions({ hasPermission: () => true });
        
        render(
            <PermissionGate permission="customers:read">
                <div>Protected Content</div>
            </PermissionGate>
        );
        
        expect(screen.getByText('Protected Content')).toBeInTheDocument();
    });
});
```

---

## 🔌 INTEGRATION COOKBOOK

### Mit bestehenden API Endpoints
```java
// Secure existing endpoints with one annotation
@Path("/api/customers")
@ApplicationScoped
public class CustomerResource {
    
    @GET
    @PermissionRequired("customers:read")
    public Response getAllCustomers() {
        return Response.ok(customerService.getAllCustomers()).build();
    }
    
    @POST
    @PermissionRequired("customers:write")
    public Response createCustomer(CustomerRequest request) {
        return Response.status(201).entity(customerService.create(request)).build();
    }
    
    @DELETE
    @Path("/{id}")
    @PermissionRequired("customers:delete")
    public Response deleteCustomer(@PathParam("id") UUID id) {
        customerService.delete(id);
        return Response.noContent().build();
    }
}
```

### Mit Navigation (M1)
```typescript
// Dynamic navigation based on permissions
export const useNavigationItems = () => {
    const { hasPermission } = usePermissions();
    
    return useMemo(() => [
        {
            title: 'Dashboard',
            path: '/dashboard',
            icon: <DashboardIcon />,
            visible: true
        },
        {
            title: 'Kunden',
            path: '/customers',
            icon: <PersonIcon />,
            visible: hasPermission('customers:read')
        },
        {
            title: 'Admin',
            path: '/admin',
            icon: <AdminIcon />,
            visible: hasPermission('admin:access')
        }
    ].filter(item => item.visible), [hasPermission]);
};
```

---

## 🎯 ADMIN UI RECIPES

### Recipe 4: Permission Management Dashboard
```typescript
// Admin Permission Editor (copy-paste ready)
export const PermissionManagement: React.FC = () => {
    const [selectedUser, setSelectedUser] = useState<User | null>(null);
    const { data: users } = useUsers();
    const { data: allPermissions } = useAllPermissions();
    
    const handlePermissionToggle = async (userId: string, permission: string, granted: boolean) => {
        try {
            await apiClient.post('/api/admin/permissions/grant', {
                userId,
                permissionCode: permission,
                granted,
                reason: 'Admin manual assignment'
            });
            toast.success('Permission updated');
        } catch (error) {
            toast.error('Failed to update permission');
        }
    };
    
    return (
        <PermissionGate permission="admin:permissions" fallback={<AccessDenied />}>
            <Grid container spacing={3}>
                <Grid item xs={4}>
                    <Paper sx={{ p: 2 }}>
                        <Typography variant="h6">Benutzer</Typography>
                        <List>
                            {users?.map(user => (
                                <ListItem
                                    key={user.id}
                                    button
                                    selected={selectedUser?.id === user.id}
                                    onClick={() => setSelectedUser(user)}
                                >
                                    <ListItemText
                                        primary={`${user.firstName} ${user.lastName}`}
                                        secondary={user.role}
                                    />
                                </ListItem>
                            ))}
                        </List>
                    </Paper>
                </Grid>
                
                <Grid item xs={8}>
                    {selectedUser && (
                        <Paper sx={{ p: 2 }}>
                            <Typography variant="h6" gutterBottom>
                                Berechtigungen für {selectedUser.firstName}
                            </Typography>
                            
                            <PermissionTree
                                permissions={allPermissions}
                                userPermissions={selectedUser.permissions}
                                onChange={(permission, granted) => 
                                    handlePermissionToggle(selectedUser.id, permission, granted)
                                }
                            />
                        </Paper>
                    )}
                </Grid>
            </Grid>
        </PermissionGate>
    );
};
```

### Recipe 5: Permission Groups UI
```typescript
// Permission grouped by resource
const PermissionTree: React.FC<{
    permissions: Permission[];
    userPermissions: string[];
    onChange: (permission: string, granted: boolean) => void;
}> = ({ permissions, userPermissions, onChange }) => {
    const groupedPermissions = useMemo(() => {
        return permissions.reduce((acc, perm) => {
            const [resource, action] = perm.code.split(':');
            if (!acc[resource]) acc[resource] = [];
            acc[resource].push(perm);
            return acc;
        }, {} as Record<string, Permission[]>);
    }, [permissions]);
    
    return (
        <List>
            {Object.entries(groupedPermissions).map(([resource, perms]) => (
                <Accordion key={resource}>
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography>{resource}</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                        {perms.map(perm => (
                            <FormControlLabel
                                key={perm.code}
                                control={
                                    <Switch
                                        checked={userPermissions.includes(perm.code)}
                                        onChange={(e) => onChange(perm.code, e.target.checked)}
                                    />
                                }
                                label={perm.name}
                            />
                        ))}
                    </AccordionDetails>
                </Accordion>
            ))}
        </List>
    );
};
```

---

## 📚 DEEP KNOWLEDGE (Bei Bedarf expandieren)

<details>
<summary>🏗️ Database Schema</summary>

### Permission Tables
```sql
-- Core permission definition
CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    permission_code VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_permission_code (permission_code),
    INDEX idx_resource_action (resource, action)
);

-- Role to permission mapping
CREATE TABLE role_permissions (
    role_id UUID REFERENCES roles(id),
    permission_id UUID REFERENCES permissions(id),
    granted BOOLEAN DEFAULT true,
    granted_by UUID REFERENCES users(id),
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id)
);

-- Direct user permissions (overrides)
CREATE TABLE user_permissions (
    user_id UUID REFERENCES users(id),
    permission_id UUID REFERENCES permissions(id),
    granted BOOLEAN NOT NULL,
    granted_by UUID REFERENCES users(id),
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    reason TEXT,
    PRIMARY KEY (user_id, permission_id)
);

-- Permission audit log
CREATE TABLE permission_audit (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    permission_code VARCHAR(100),
    check_type VARCHAR(20), -- 'direct', 'role', 'error'
    result BOOLEAN,
    checked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_permission (user_id, permission_code),
    INDEX idx_checked_at (checked_at)
);
```

</details>

<details>
<summary>🚀 Advanced Features</summary>

### Temporary Permissions
```java
public void grantTemporaryPermission(UUID userId, String permissionCode, 
                                    Duration duration, String reason) {
    UserPermission temp = UserPermission.builder()
        .userId(userId)
        .permissionId(getPermissionId(permissionCode))
        .granted(true)
        .grantedBy(getCurrentUserId())
        .expiresAt(LocalDateTime.now().plus(duration))
        .reason(reason)
        .build();
    
    permissionRepository.save(temp);
}
```

### Permission Delegation
```java
@PermissionRequired("admin:delegate")
public void delegatePermission(UUID fromUser, UUID toUser, 
                              String permissionCode, String reason) {
    // Allow user to delegate their permissions
    if (!hasPermission(fromUser, permissionCode)) {
        throw new ForbiddenException("Cannot delegate permission you don't have");
    }
    
    grantPermission(toUser, permissionCode, 
        "Delegated from " + getUserName(fromUser) + ": " + reason);
}
```

</details>

---

**🎯 Nächster Schritt:** Backend Permission Service implementieren und mit FC-008 Security integrieren