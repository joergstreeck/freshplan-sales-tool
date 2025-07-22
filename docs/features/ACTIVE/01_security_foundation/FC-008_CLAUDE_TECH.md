# FC-008 Security Foundation - CLAUDE TECH 🤖

## 🧠 QUICK-LOAD (30 Sekunden bis zur Produktivität)

**Feature:** JWT-basierte Security mit Keycloak Integration für Auth & Authorization  
**Stack:** Quarkus + JWT + Keycloak OIDC + React Context  
**Status:** 100% fertig - Alle Tests grün + Backend-Problem behoben ✅  
**Dependencies:** KEINE (Foundation) | Blockiert: ALLE anderen Features  

**Jump to:** [📚 Recipes](#-implementation-recipes) | [🧪 Tests](#-test-patterns) | [🔌 Integration](#-integration-cookbook) | [🚨 Current Issues](#-current-issues)

**Core Purpose in 1 Line:** `JWT Token → User Context → @RolesAllowed → Protected Resources`

---

## 🍳 IMPLEMENTATION RECIPES

### Recipe 1: JWT Security in 5 Minuten
```bash
# 1. Dependencies hinzufügen (pom.xml)
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-jwt</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-oidc</artifactId>
</dependency>

# 2. Keycloak konfigurieren (application.properties)
quarkus.oidc.auth-server-url=http://localhost:8180/realms/freshplan
quarkus.oidc.client-id=freshplan-backend
quarkus.oidc.credentials.secret=${KEYCLOAK_CLIENT_SECRET}
quarkus.oidc.tls.verification=none
%dev.quarkus.oidc.enabled=false  # Für Tests

# 3. SecurityContextProvider erstellen
```

```java
// Copy-paste ready SecurityContextProvider
@ApplicationScoped
public class SecurityContextProvider {
    @Inject JsonWebToken jwt;
    @Inject UserRepository userRepository;
    
    public Optional<User> getCurrentUser() {
        if (jwt == null) return Optional.empty();
        
        String keycloakId = jwt.getSubject();
        return userRepository.findByKeycloakId(keycloakId)
            .or(() -> createUserFromToken(jwt));
    }
    
    public boolean hasRole(String role) {
        return getCurrentUser()
            .map(user -> user.hasRole(role))
            .orElse(false);
    }
}
```

### Recipe 2: Protected REST Endpoint
```java
// Copy-paste für geschützte Endpoints
@Path("/api/users")
@ApplicationScoped
@Authenticated  // Requires valid JWT
public class UserResource {
    @Inject SecurityContextProvider security;
    
    @GET
    @Path("/me")
    @RolesAllowed({"admin", "manager", "sales"})
    public Response getCurrentUser() {
        return security.getCurrentUser()
            .map(user -> Response.ok(user).build())
            .orElse(Response.status(401).build());
    }
}
```

### Recipe 3: Frontend Auth Hook
```typescript
// Copy-paste ready useAuth Hook
export const useAuth = () => {
    const { data: user, isLoading } = useQuery({
        queryKey: ['auth-user'],
        queryFn: async () => {
            const response = await apiClient.get('/api/users/me');
            return response.data;
        },
        retry: false,
        staleTime: 5 * 60 * 1000 // 5 min cache
    });
    
    return {
        user,
        isAuthenticated: !!user,
        isLoading,
        hasRole: (role: string) => user?.roles?.includes(role) ?? false
    };
};

// Usage in Component
const MyComponent = () => {
    const { user, isAuthenticated, hasRole } = useAuth();
    
    if (!isAuthenticated) return <Navigate to="/login" />;
    if (!hasRole('admin')) return <Unauthorized />;
    
    return <AdminPanel user={user} />;
};
```

---

## 🧪 TEST PATTERNS

### Pattern 1: Security Test mit @TestSecurity
```java
// Copy-paste Test Template
@QuarkusTest
class SecurityTest {
    @Test
    @TestSecurity(user = "testuser", roles = {"admin"})
    void testAdminAccess() {
        given()
            .when().get("/api/users/me")
            .then()
                .statusCode(200)
                .body("username", is("testuser"));
    }
    
    @Test
    @TestSecurity(user = "testuser", roles = {"sales"})
    void testInsufficientRole() {
        given()
            .when().get("/api/admin/settings")
            .then()
                .statusCode(403);
    }
}
```

### Pattern 2: JWT Token Mock
```java
// JWT Mock für Integration Tests
@QuarkusTest
class JWTIntegrationTest {
    @Test
    void testWithMockJWT() {
        String token = Jwt.issuer("https://keycloak/realm")
            .upn("testuser")
            .groups(Set.of("admin"))
            .expiresIn(300)
            .sign();
            
        given()
            .header("Authorization", "Bearer " + token)
            .when().get("/api/protected")
            .then().statusCode(200);
    }
}
```

---

## 🔌 INTEGRATION COOKBOOK

### Mit Frontend verbinden
```typescript
// 1. Axios Interceptor für Auto-Auth
apiClient.interceptors.request.use((config) => {
    const token = keycloak.token;
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// 2. Protected Route Component
export const ProtectedRoute: React.FC<{
    children: React.ReactNode;
    requiredRole?: string;
}> = ({ children, requiredRole }) => {
    const { isAuthenticated, hasRole } = useAuth();
    
    if (!isAuthenticated) return <Navigate to="/login" />;
    if (requiredRole && !hasRole(requiredRole)) return <Forbidden />;
    
    return <>{children}</>;
};
```

### Mit anderen Backend Services
```java
// Security in anderen Services nutzen
@ApplicationScoped
public class CustomerService {
    @Inject SecurityContextProvider security;
    
    public List<Customer> getMyCustomers() {
        User currentUser = security.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException());
            
        return customerRepository.findByAssignedTo(currentUser.getId());
    }
}
```

---

## ✅ RESOLVED ISSUES

### TODO-077: UserResourceSecurityTest Failures (GELÖST)
```java
// PROBLEM: Tests versuchten als non-Admin User zu erstellen
// LÖSUNG: Separate Test-Klasse für non-Admin Tests erstellt

// UserResourceSecurityNonAdminTest.java
// Verwendet statische UUID für 403/401 Tests
private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
```

### Alle Security Tests grün:
- SecurityContextProviderUnitTest: 16/16 ✅
- SecurityContextProviderWithSecurityTest: 8/8 ✅  
- UserResourceSecurityTest: 18/18 ✅
- UserResourceSecurityNonAdminTest: 5/5 ✅

---

## 📊 QUICK REFERENCE

### Security Annotations
```java
@Authenticated          // Nur eingeloggt
@RolesAllowed("admin")  // Spezifische Rolle
@PermitAll             // Öffentlich
@DenyAll               // Komplett gesperrt
```

### Keycloak URLs
- **Admin:** http://localhost:8180/admin
- **Realm:** http://localhost:8180/realms/freshplan
- **Token:** POST /realms/freshplan/protocol/openid-connect/token

### Test Users (Dev)
```properties
# Für lokale Entwicklung
test.admin=admin@freshplan.de / admin123
test.manager=manager@freshplan.de / manager123
test.sales=sales@freshplan.de / sales123
```

---

## 📚 DEEP KNOWLEDGE (Bei Bedarf expandieren)

<details>
<summary>🏗️ Vollständige Architektur Details</summary>

### Security Flow Diagramm
```
Frontend Request → Authentication Filter → JWT Validation → User Context → Resource Access
        ↓                ↓                     ↓               ↓              ↓
    Bearer Token    Token Extraction    Keycloak Verify   SecurityContext   @RolesAllowed
    Authorization   Header Check        Public Key         User Details      Permission Check
    HTTP Header     Extract JWT         Signature Valid    Role Mapping      Allow/Deny
```

### Database Schema
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    keycloak_id VARCHAR(100) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'sales',
    active BOOLEAN DEFAULT true,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE security_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    event_type VARCHAR(50) NOT NULL,
    resource_accessed VARCHAR(200),
    ip_address INET,
    success BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

</details>

<details>
<summary>🔧 Advanced Configuration</summary>

### Multi-Tenant Setup
```properties
# Für Mandantenfähigkeit
quarkus.oidc.tenant-enabled=true
quarkus.oidc.freshfoodz.auth-server-url=http://keycloak/realms/freshfoodz
quarkus.oidc.catering.auth-server-url=http://keycloak/realms/catering
```

### Performance Tuning
```properties
# JWT Cache Settings
quarkus.oidc.token-cache.max-size=1000
quarkus.oidc.token-cache.time-to-live=5M

# Connection Pool
quarkus.oidc.connection-pool-size=10
```

</details>

---

**🎯 Status:** FC-008 Security Foundation ist vollständig implementiert und getestet! ✅