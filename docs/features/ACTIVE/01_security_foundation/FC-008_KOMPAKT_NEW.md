# FC-008 Security Foundation - KOMPAKT ⚡

**Feature-Typ:** 🔧 BACKEND FOUNDATION  
**Priorität:** KRITISCH  
**Aufwand:** 5-7 Tage  
**Status:** 🔄 85% fertig (Tests deaktiviert)  

---

## 🎯 ÜBERBLICK

### Business Context
JWT-basierte Security Foundation mit Keycloak Integration für vollständige Authentifizierung und Autorisierung. Foundation Feature - ALLE anderen Features benötigen User-Context und Zugriffsschutz.

### Technical Vision
Enterprise-Grade Security mit JWT Token Validation, Role-based Authorization (admin/manager/sales), Keycloak OIDC Integration und Security Headers. Zentraler SecurityContextProvider für User-Context in allen Features.

---

## 🏗️ CORE ARCHITEKTUR

### Security Flow
```
Frontend Request → Authentication Filter → JWT Validation → User Context → Resource Access
      ↓                ↓                     ↓               ↓              ↓
  Bearer Token    Token Extraction    Keycloak Verify   SecurityContext   @RolesAllowed
```

### Backend Core Components
```java
// 1. Security Context Provider - Zentral für alle Features
@ApplicationScoped 
public class SecurityContextProvider {
    public Optional<User> getCurrentUser() {
        return extractUserFromToken(getCurrentToken());
    }
    
    public boolean hasRole(String role) {
        return getCurrentUser()
            .map(user -> user.getRoles().contains(role))
            .orElse(false);
    }
}

// 2. JWT Authentication Filter
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JWTAuthenticationFilter implements ContainerRequestFilter {
    public void filter(ContainerRequestContext context) {
        String token = extractToken(context);
        if (isValidToken(token)) {
            setSecurityContext(context, getUserFromToken(token));
        }
    }
}

// 3. User Resource - Test Endpoints
@Path("/api/users")
@ApplicationScoped
public class UserResource {
    @GET @Path("/me")
    @RolesAllowed({"admin", "manager", "sales"})
    public UserResponse getCurrentUser() {
        return securityContext.getCurrentUser()
            .map(userMapper::toResponse)
            .orElseThrow(() -> new NotAuthenticatedException());
    }
}
```

### Frontend Integration
```typescript
// Auth Service
export class AuthService {
    async getCurrentUser(): Promise<User> {
        return await apiClient.get('/api/users/me');
    }
    
    async login(token: string): Promise<void> {
        localStorage.setItem('token', token);
        apiClient.defaults.headers['Authorization'] = `Bearer ${token}`;
    }
}

// React Hook
export const useAuth = () => {
    const { data: user } = useQuery(['auth-user'], authService.getCurrentUser);
    return { 
        user, 
        isAuthenticated: !!user,
        hasRole: (role: string) => user?.roles?.includes(role) ?? false 
    };
};
```

### Database Schema
```sql
-- Users Table (synced from Keycloak)
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
```

---

## 🔗 DEPENDENCIES

- **Benötigt:** KEINE (Foundation Feature)
- **Blockiert:** FC-009 Permissions, M4 Pipeline, alle UI-Features mit User-Context
- **Keycloak:** läuft auf localhost:8180, Realm: freshplan
- **PostgreSQL:** User-Sync und Audit-Logging

---

## 🧪 TESTING

### Aktuelles Problem (TODO-024/028)
```java
// Tests deaktiviert wegen fehlender Test-Endpoints
@Test
@TestSecurity(user = "testuser", roles = {"admin", "manager", "sales"})
void testGetCurrentUser() {
    // PROBLEM: @TestSecurity wird nicht erkannt
    // LÖSUNG: Quarkus 3.17.4+ verwenden
}
```

### Test Strategy
```java
// 1. Security Context Tests
@QuarkusTest
class SecurityContextTest {
    @Test
    @TestSecurity(user = "admin", roles = {"admin"})
    void shouldProvideUserContext() {
        assertThat(securityContext.getCurrentUser()).isPresent();
    }
}

// 2. JWT Validation Tests  
@Test
void shouldValidateJWTToken() {
    String token = createValidJWT();
    assertThat(jwtValidator.validate(token)).isTrue();
}

// 3. Role Authorization Tests
@Test
@TestSecurity(user = "sales", roles = {"sales"})  
void shouldAllowSalesAccess() {
    given().when().get("/api/users/me").then().statusCode(200);
}
```

---

## 📋 QUICK IMPLEMENTATION

### 🕒 15-Min Claude Working Section

**Aufgabe:** Security Foundation Tests reaktivieren und TODO-024/028 lösen

**Sofort loslegen:**
1. Quarkus Version auf 3.17.4+ aktualisieren für @TestSecurity Support
2. Test-Endpoints für User-Context implementieren  
3. JWT Token Validation testen
4. Role-based Authorization verifizieren

**Quick-Win Code:**
```java
// 1. Test-Endpoint reaktivieren
@GET @Path("/test/user")
@RolesAllowed({"admin", "manager", "sales"})
public Response testCurrentUser() {
    return Response.ok(securityContext.getCurrentUser()).build();
}

// 2. @TestSecurity verwenden
@Test
@TestSecurity(user = "testuser", roles = {"admin", "manager", "sales"})
void getCurrentUser_shouldReturnAuthenticatedUser() {
    given()
        .when().get("/api/users/test/user")
        .then().statusCode(200);
}

// 3. Security Context validieren  
@Inject SecurityContextProvider securityContext;

// 4. Tests grün bekommen
./mvnw test -Dtest=UserResourceTest
```

**Nächste Schritte:**
- Phase 1: Tests reaktivieren (2h)
- Phase 2: Security Headers implementieren (1h)  
- Phase 3: Audit Logging vervollständigen (2h)

**Erfolgs-Kriterien:**
- ✅ @TestSecurity funktioniert 
- ✅ Alle Security-Tests grün
- ✅ User-Context in allen Requests verfügbar
- ✅ Role-based Authorization funktional

---

**🔗 DETAIL-DOCS:**
- [TECH_CONCEPT](/docs/features/ACTIVE/01_security_foundation/FC-008_TECH_CONCEPT.md) - Vollständige technische Spezifikation
- [IMPLEMENTATION_GUIDE](/docs/features/ACTIVE/01_security_foundation/FC-008_IMPLEMENTATION_GUIDE.md) - Detaillierte Umsetzungsanleitung

**🎯 Nächster Schritt:** Tests reaktivieren mit @TestSecurity und TODO-024/028 lösen