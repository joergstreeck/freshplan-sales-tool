# 📘 FC-008 IMPLEMENTATION GUIDE

**Zweck:** Code und Lösungen für Security Foundation  
**Status:** 85% implementiert, Tests blockiert  

---

## <a id="test-endpoints"></a>🔴 Fehlende Test-Endpoints

### Das Problem:
Tests erwarten diese Endpoints, die nicht existieren:
- `/api/test/public` - Öffentlich zugänglich
- `/api/test/authenticated` - Nur mit gültigem Token
- `/api/test/admin` - Nur für admin Rolle
- `/api/test/roles` - Gibt User-Rollen zurück

### Die Lösung:

```java
package de.freshplan.api.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import io.quarkus.security.identity.SecurityIdentity;
import java.util.Map;

@Path("/api/test")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TestResource {
    
    @Inject
    SecurityIdentity identity;
    
    @GET
    @Path("/public")
    @PermitAll
    public Response publicEndpoint() {
        return Response.ok(Map.of(
            "message", "Public endpoint - no auth required",
            "timestamp", System.currentTimeMillis()
        )).build();
    }
    
    @GET
    @Path("/authenticated")
    public Response authenticatedEndpoint() {
        return Response.ok(Map.of(
            "message", "Authenticated endpoint",
            "user", identity.getPrincipal().getName(),
            "authenticated", identity.isAnonymous() ? "false" : "true"
        )).build();
    }
    
    @GET
    @Path("/admin")
    @RolesAllowed("admin")
    public Response adminEndpoint() {
        return Response.ok(Map.of(
            "message", "Admin only endpoint",
            "user", identity.getPrincipal().getName(),
            "roles", identity.getRoles()
        )).build();
    }
    
    @GET
    @Path("/roles")
    public Response getRoles() {
        return Response.ok(Map.of(
            "principal", identity.getPrincipal().getName(),
            "roles", identity.getRoles(),
            "attributes", identity.getAttributes()
        )).build();
    }
}
```

### Tests wieder aktivieren:

```bash
# 1. Finde alle deaktivierten Tests
grep -r "@Disabled" backend/src/test --include="*.java"

# 2. Entferne @Disabled Annotations
sed -i '' '/@Disabled/d' backend/src/test/java/.../SecurityContextProviderIntegrationTest.java

# 3. Tests ausführen
cd backend
./mvnw test -Dtest=SecurityContextProviderIntegrationTest
```

---

## <a id="test-strategy"></a>🧪 Test Strategy

### Unit Tests:
```java
@QuarkusTest
class SecurityContextProviderTest {
    
    @Test
    void testGetCurrentUser() {
        // Mock SecurityIdentity
        var identity = Mockito.mock(SecurityIdentity.class);
        when(identity.getPrincipal()).thenReturn(() -> "test-user");
        
        // Test
        var user = provider.getCurrentUser();
        assertThat(user.username).isEqualTo("test-user");
    }
}
```

### Integration Tests:
```java
@QuarkusTest
@TestHTTPEndpoint(UserResource.class)
class UserResourceIntegrationTest {
    
    @Test
    void testUnauthorized() {
        given()
            .when().get("/users")
            .then().statusCode(401);
    }
    
    @Test
    @TestSecurity(user = "testuser", roles = "admin")
    void testAuthorizedAsAdmin() {
        given()
            .when().get("/users")
            .then().statusCode(200);
    }
}
```

---

## <a id="security-flow"></a>🔐 Security Flow

### Request Flow:
```
Client Request
    ↓
AuthenticationFilter [1]
    ↓
JWT Validation [2]
    ↓
SecurityIdentity Creation [3]
    ↓
Role Check [4]
    ↓
Resource Method [5]
```

### Code-Walkthrough:

#### [1] AuthenticationFilter
```java
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    
    @Override
    public void filter(ContainerRequestContext request) {
        String token = extractToken(request);
        if (token != null) {
            // Token wird automatisch von Quarkus validiert
            // SecurityIdentity wird erstellt
        }
    }
}
```

#### [2] JWT Validation (Quarkus OIDC)
```properties
# application.properties
quarkus.oidc.auth-server-url=http://localhost:8180/realms/freshplan
quarkus.oidc.client-id=freshplan-backend
quarkus.oidc.application-type=service
```

#### [3] SecurityContextProvider
```java
@ApplicationScoped
public class SecurityContextProvider {
    
    @Inject
    SecurityIdentity identity;
    
    public UserContext getCurrentUser() {
        if (identity.isAnonymous()) {
            throw new NotAuthenticatedException();
        }
        
        return UserContext.builder()
            .id(getUserId())
            .username(identity.getPrincipal().getName())
            .roles(identity.getRoles())
            .build();
    }
}
```

---

## <a id="troubleshooting"></a>🔧 Troubleshooting

### Problem: 401 Unauthorized überall
```bash
# 1. Check Keycloak läuft
curl http://localhost:8180/realms/freshplan

# 2. Token manuell holen
TOKEN=$(curl -X POST http://localhost:8180/realms/freshplan/protocol/openid-connect/token \
  -d "client_id=freshplan-backend" \
  -d "grant_type=password" \
  -d "username=admin" \
  -d "password=admin" | jq -r '.access_token')

# 3. Test mit Token
curl http://localhost:8080/api/users -H "Authorization: Bearer $TOKEN"
```

### Problem: SecurityIdentity ist null
```java
// In Test: @TestSecurity vergessen?
@Test
@TestSecurity(user = "test", roles = "admin")
void testMethod() {
    // identity ist jetzt nicht null
}

// In Dev Mode: Token fehlt?
// Prüfe Authorization Header
```

### Problem: Roles werden nicht erkannt
```bash
# JWT Token decoden
echo $TOKEN | cut -d. -f2 | base64 -d | jq

# Sollte enthalten:
{
  "realm_access": {
    "roles": ["admin", "manager", "sales"]
  }
}
```

---

## <a id="future-work"></a>📅 Future Work

### TODO-015: Audit Logging
```java
@ApplicationScoped
public class SecurityAuditLogger {
    
    void logAccess(String user, String resource, String action) {
        // Implement audit trail
    }
}
```

### TODO-016: Rate Limiting
```java
@ApplicationScoped
public class RateLimiter {
    
    @ConfigProperty(name = "api.rate-limit.requests")
    int maxRequests = 100;
    
    boolean allowRequest(String user) {
        // Implement rate limiting
    }
}
```