# E2E Testing mit Keycloak

## Übersicht

Die E2E-Tests validieren die komplette Security-Integration mit einem echten Keycloak-Server.

## Voraussetzungen

- Docker und Docker Compose installiert
- Port 8180 frei (für Keycloak)
- Maven installiert

## Quick Start

```bash
# 1. Keycloak starten
./scripts/start-keycloak.sh

# 2. E2E-Tests ausführen
./scripts/run-e2e-tests.sh
```

## Test-Struktur

### KeycloakE2ETest
- Token-Generierung für alle Rollen
- JWT-Validierung
- Refresh-Token-Flow
- Invalid Credentials Handling

### ApiSecurityE2ETest
- Customer API: Alle Rollen können lesen/erstellen
- User API: Nur Admin-Zugriff
- Calculator API: Alle authentifizierten User
- Sales Cockpit: Rollenbasierter Zugriff
- Health Endpoints: Ohne Authentifizierung

## Test-User

| Username | Password | Rolle | E-Mail |
|----------|----------|-------|---------|
| admin | admin123 | admin | admin@freshplan.de |
| manager | manager123 | manager | manager@freshplan.de |
| sales | sales123 | sales | sales@freshplan.de |

## Keycloak-Konfiguration

### Development Mode
```bash
# Startet Keycloak mit In-Memory H2 Database
./scripts/start-keycloak.sh dev
```

### Production Mode
```bash
# Startet Keycloak mit PostgreSQL
./scripts/start-keycloak.sh prod
```

## Docker Compose

Die `docker-compose.keycloak.yml` definiert:
- Keycloak auf Port 8180
- Automatischer Realm-Import
- Optional: PostgreSQL für Production

## Realm-Konfiguration

Das FreshPlan-Realm (`infrastructure/keycloak/freshplan-realm.json`) enthält:
- 3 Rollen: admin, manager, sales
- Test-User für jede Rolle
- Client: freshplan-backend (Bearer-only)
- Frontend-Client: freshplan-frontend (Public)

## Troubleshooting

### Keycloak startet nicht
```bash
# Logs prüfen
docker-compose -f docker-compose.keycloak.yml logs keycloak

# Container neu starten
docker-compose -f docker-compose.keycloak.yml down
docker-compose -f docker-compose.keycloak.yml up -d
```

### Tests schlagen fehl
1. Keycloak-Status prüfen: http://localhost:8180/health/ready
2. Realm prüfen: http://localhost:8180/admin (admin/admin)
3. Token-Endpoint testen:
```bash
curl -X POST http://localhost:8180/realms/freshplan/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=freshplan-backend&client_secret=secret&username=admin&password=admin123"
```

### Port-Konflikte
Wenn Port 8180 belegt ist, in `docker-compose.keycloak.yml` ändern:
```yaml
ports:
  - "8280:8080"  # Neuer Port
```

Und in den Tests `KEYCLOAK_URL` anpassen.

## CI/CD Integration

Für GitHub Actions:
```yaml
- name: Start Keycloak
  run: |
    docker-compose -f docker-compose.keycloak.yml up -d
    ./scripts/wait-for-keycloak.sh

- name: Run E2E Tests
  run: ./scripts/run-e2e-tests.sh

- name: Stop Keycloak
  if: always()
  run: docker-compose -f docker-compose.keycloak.yml down
```

## Lokale Entwicklung

### Backend application.properties
```properties
# Keycloak OIDC Configuration
quarkus.oidc.auth-server-url=http://localhost:8180/realms/freshplan
quarkus.oidc.client-id=freshplan-backend
quarkus.oidc.credentials.secret=secret
quarkus.oidc.tls.verification=none
```

### Frontend .env
```env
VITE_KEYCLOAK_URL=http://localhost:8180
VITE_KEYCLOAK_REALM=freshplan
VITE_KEYCLOAK_CLIENT_ID=freshplan-frontend
```

## Nächste Schritte

1. **Performance-Tests**: JMeter/k6 Integration
2. **Security-Scans**: OWASP ZAP Integration
3. **Multi-Tenant**: Realm pro Kunde
4. **Token-Rotation**: Automatisches Refresh