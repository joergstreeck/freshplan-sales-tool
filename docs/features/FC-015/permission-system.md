# FC-015: Permission System - Detaillierte Implementierung

**Zurück:** [FC-015 Hauptkonzept](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-015-rights-roles-concept.md)

## 📋 Übersicht

Implementierung des feingranularen Permission-Systems als Erweiterung der FC-008 Security Foundation.

## 🏗️ Backend-Architektur

### Permission Entity
```java
@Entity
@Table(name = "permissions")
public class Permission extends PanacheEntityBase {
    @Id
    @Column(name = "permission_key", length = 100)
    public String key; // z.B. "opportunity.change_stage"
    
    @Column(nullable = false)
    public String category; // OPPORTUNITY, CONTRACT, DISCOUNT, SYSTEM
    
    @Column(nullable = false)
    public String action; // VIEW, CREATE, EDIT, DELETE, APPROVE
    
    @Column(length = 500)
    public String description;
    
    @Column(name = "risk_level")
    public RiskLevel riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    
    @ElementCollection
    @CollectionTable(name = "permission_conditions")
    public Map<String, String> conditions; // z.B. "max_discount" -> "20"
}
```

### Permission Service
```java
@ApplicationScoped
public class PermissionService {
    @Inject
    SecurityContextProvider securityContext;
    
    @Inject
    RolePermissionRepository rolePermissionRepo;
    
    @Inject
    UserPermissionOverrideRepository overrideRepo;
    
    @Inject
    DelegationService delegationService;
    
    @Inject
    @Channel("permission-events")
    Emitter<PermissionEvent> eventEmitter;
    
    public boolean hasPermission(String permissionKey) {
        var user = securityContext.getCurrentUser();
        
        // 1. Check user-specific overrides
        var override = overrideRepo.findByUserAndPermission(user.id, permissionKey);
        if (override.isPresent()) {
            logPermissionCheck(user, permissionKey, override.get().granted);
            return override.get().granted;
        }
        
        // 2. Check delegations
        var delegation = delegationService.getActiveDelegation(user.id, permissionKey);
        if (delegation.isPresent()) {
            logPermissionCheck(user, permissionKey, true, "DELEGATION");
            return true;
        }
        
        // 3. Check role permissions
        boolean hasPermission = user.getRoles().stream()
            .anyMatch(role -> rolePermissionRepo.hasPermission(role, permissionKey));
            
        logPermissionCheck(user, permissionKey, hasPermission);
        return hasPermission;
    }
    
    public boolean hasPermissionWithContext(String permissionKey, PermissionContext context) {
        if (!hasPermission(permissionKey)) return false;
        
        // Additional context checks
        var permission = Permission.findById(permissionKey);
        if (permission.conditions != null) {
            return evaluateConditions(permission.conditions, context);
        }
        
        return true;
    }
}
```

### Permission Annotations
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {
    String value(); // Permission key
    String[] alternatives() default {}; // OR permissions
    boolean checkOwnership() default false;
}

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PermissionContext {
    String value(); // Context parameter name
}
```

### Permission Interceptor
```java
@RequiresPermission
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class PermissionInterceptor {
    @Inject
    PermissionService permissionService;
    
    @AroundInvoke
    public Object checkPermission(InvocationContext context) throws Exception {
        var annotation = getAnnotation(context);
        
        // Build permission context from parameters
        var permContext = buildContext(context);
        
        // Check main permission
        if (permissionService.hasPermissionWithContext(annotation.value(), permContext)) {
            return context.proceed();
        }
        
        // Check alternatives
        for (String alt : annotation.alternatives()) {
            if (permissionService.hasPermissionWithContext(alt, permContext)) {
                return context.proceed();
            }
        }
        
        throw new ForbiddenException("Missing permission: " + annotation.value());
    }
}
```

## 🎯 Integration mit bestehenden Features

### M4 Opportunity Pipeline
```java
@Path("/api/opportunities")
public class OpportunityResource {
    
    @POST
    @Path("/{id}/stage")
    @RequiresPermission("opportunity.change_stage")
    public Response changeStage(
        @PathParam("id") UUID id,
        @Valid StageChangeRequest request,
        @PermissionContext("opportunity") UUID opportunityId
    ) {
        // Permission check happens automatically
        return opportunityService.changeStage(id, request);
    }
    
    @DELETE
    @Path("/{id}")
    @RequiresPermission(value = "opportunity.delete", checkOwnership = true)
    public Response deleteOpportunity(@PathParam("id") UUID id) {
        // Ownership check included
        return opportunityService.delete(id);
    }
}
```

### FC-012 Audit Trail Integration
```java
private void logPermissionCheck(UserPrincipal user, String permission, boolean granted, String reason) {
    var event = new AuditEvent.Builder()
        .eventType(AuditEventType.PERMISSION_CHECK)
        .userId(user.getId())
        .resourceType("Permission")
        .resourceId(permission)
        .details(Map.of(
            "granted", granted,
            "reason", reason != null ? reason : "ROLE_BASED",
            "roles", user.getRoles()
        ))
        .build();
        
    auditService.logEvent(event);
}
```

## 🔄 Migration von FC-008

### UserPrincipal Erweiterung
```java
public class UserPrincipal {
    // Existing fields...
    
    private Set<String> permissions; // Cached permissions
    private Instant permissionsCachedAt;
    
    @JsonIgnore
    public boolean hasPermission(String permission) {
        refreshPermissionsIfNeeded();
        return permissions.contains(permission);
    }
    
    private void refreshPermissionsIfNeeded() {
        if (permissionsCachedAt == null || 
            Duration.between(permissionsCachedAt, Instant.now()).toMinutes() > 5) {
            // Refresh from PermissionService
            this.permissions = permissionService.getUserPermissions(this.id);
            this.permissionsCachedAt = Instant.now();
        }
    }
}
```

## 📊 Performance-Optimierung

### Permission Cache
```java
@ApplicationScoped
public class PermissionCache {
    @Inject
    @ConfigProperty(name = "permissions.cache.ttl", defaultValue = "300")
    int cacheTtlSeconds;
    
    private final LoadingCache<String, Set<String>> userPermissionCache = 
        Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(cacheTtlSeconds, TimeUnit.SECONDS)
            .build(this::loadUserPermissions);
            
    private final LoadingCache<String, Set<String>> rolePermissionCache = 
        Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(cacheTtlSeconds * 2, TimeUnit.SECONDS)
            .build(this::loadRolePermissions);
}
```

## ⚠️ Breaking Changes

1. **@RolesAllowed → @RequiresPermission Migration**
   - Alle bestehenden @RolesAllowed müssen migriert werden
   - Mapping-Tabelle: role → permissions erstellen

2. **SecurityContextProvider Updates**
   - Neue Methoden für Permission-Checks
   - Cache-Integration

## 🧪 Test-Strategie

```java
@QuarkusTest
class PermissionServiceTest {
    @Test
    void testHierarchicalPermissions() {
        // Admin hat alle Permissions
        given(securityContext.getCurrentUser())
            .willReturn(createUser("admin", Set.of("ADMIN")));
            
        assertThat(permissionService.hasPermission("opportunity.delete"))
            .isTrue();
    }
    
    @Test
    void testConditionalPermissions() {
        var context = new PermissionContext();
        context.put("discount_percentage", 15);
        
        // Sales hat max 10% Discount
        given(securityContext.getCurrentUser())
            .willReturn(createUser("sales", Set.of("SALES")));
            
        assertThat(permissionService.hasPermissionWithContext(
            "discount.approve", context))
            .isFalse();
    }
}
```

## 🔗 Verknüpfungen

- **Nächstes Dokument:** [Delegation & Vertretung](./delegation-vertretung.md)
- **Siehe auch:** [FC-008 Security Foundation](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-16_TECH_CONCEPT_FC-008_security-foundation.md)
- **API Changes:** [FC-015 API Migration Guide](./api-migration.md)