# Keycloak Setup für FreshPlan 2.0

**📅 Aktuelles Datum: <!-- AUTO_DATE --> (System: 08.06.2025)**

## Übersicht

Keycloak ist unser Identity und Access Management System für FreshPlan 2.0. Diese Dokumentation beschreibt das lokale Development Setup.

## Quick Start

```bash
cd infrastructure
./start-keycloak.sh
```

Das Skript startet:
- PostgreSQL Datenbank
- Keycloak mit automatischem Realm-Import

## Realm-Konfiguration

Der FreshPlan Realm (`infrastructure/keycloak/freshplan-realm.json`) enthält:

### Rollen
- **admin**: Vollzugriff auf alle Funktionen (composite role)
- **manager**: Erweiterte Berechtigungen für Team-Leads
- **sales**: Verkäufer mit Standard-Berechtigungen
- **viewer**: Nur-Lese-Zugriff

### Clients
1. **freshplan-backend** (Confidential Client)
   - Client ID: `freshplan-backend`
   - Client Secret: `secret`
   - Bearer-only: true
   - Service Accounts: enabled

2. **freshplan-frontend** (Public Client)
   - Client ID: `freshplan-frontend`
   - PKCE enabled: true
   - Redirect URIs: `http://localhost:3000/*`

### Test-Benutzer
| Email | Passwort | Rollen |
|-------|----------|---------|
| admin@freshplan.de | admin123 | admin |
| manager@freshplan.de | manager123 | manager, viewer |
| sales@freshplan.de | sales123 | sales |

## Backend-Integration

### Application Properties
```properties
# Keycloak OIDC Configuration
quarkus.oidc.auth-server-url=http://localhost:8180/realms/freshplan
quarkus.oidc.client-id=freshplan-backend
quarkus.oidc.credentials.secret=${KEYCLOAK_CLIENT_SECRET:secret}
quarkus.oidc.tls.verification=none

# Token Validation
quarkus.oidc.token.issuer=http://localhost:8180/realms/freshplan
quarkus.oidc.token.audience=freshplan-backend
```

### Securing Endpoints
```java
@Path("/api/users")
@Authenticated  // Requires any authenticated user
public class UserResource {
    
    @GET
    @Path("/{id}/roles")
    @RolesAllowed("admin")  // Only admin role
    public List<String> getUserRoles(@PathParam("id") UUID userId) {
        // ...
    }
}
```

## Frontend-Integration

### Keycloak JavaScript Adapter
```javascript
import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
    url: 'http://localhost:8180',
    realm: 'freshplan',
    clientId: 'freshplan-frontend'
});

// Initialize with PKCE
keycloak.init({
    onLoad: 'check-sso',
    pkceMethod: 'S256'
});
```

## Token-Informationen

### Access Token Claims
- `sub`: User ID
- `email`: User Email
- `realm_access.roles`: Realm Rollen
- `preferred_username`: Username

### Beispiel Token Payload
```json
{
  "exp": 1704985200,
  "iat": 1704981600,
  "jti": "unique-token-id",
  "iss": "http://localhost:8180/realms/freshplan",
  "aud": "freshplan-backend",
  "sub": "user-uuid",
  "typ": "Bearer",
  "azp": "freshplan-frontend",
  "preferred_username": "testuser",
  "email": "test@freshplan.de",
  "realm_access": {
    "roles": ["user", "manager"]
  }
}
```

## Troubleshooting

### Container neu starten
```bash
docker-compose down
docker-compose up -d db keycloak
```

### Realm neu importieren
1. Container stoppen: `docker-compose down`
2. Keycloak-Daten löschen: `docker volume rm freshplan_postgres_data`
3. Neu starten: `./start-keycloak.sh`

### Logs anzeigen
```bash
docker-compose logs -f keycloak
```

### Health Check
```bash
curl http://localhost:8180/health/ready
```

## Security Best Practices

1. **Production Settings**:
   - SSL/TLS aktivieren
   - Sichere Client Secrets verwenden
   - Token-Lebensdauer anpassen
   - Brute-Force-Schutz konfigurieren

2. **Client Configuration**:
   - PKCE für Public Clients
   - Refresh Token Rotation
   - Strict redirect URI validation

3. **User Management**:
   - Password Policies
   - Account Lockout
   - Email Verification

## Weiterführende Links

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Quarkus OIDC Guide](https://quarkus.io/guides/security-openid-connect)
- [Keycloak JavaScript Adapter](https://www.keycloak.org/docs/latest/securing_apps/#_javascript_adapter)