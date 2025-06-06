# Team BACK Workspace

## Quick Start
```bash
cd backend
./mvnw quarkus:dev
```

## Aktueller Sprint – User Management
- [ ] OIDC-Integration mit Keycloak
- [ ] User CRUD API erweitern  
- [ ] Row-Level Security

## API-Status

| Endpoint | Status |
|----------|--------|
| `POST /api/users` | ✅ |
| `GET /api/users/search` | ✅ |
| `PUT /api/users/{id}/roles` | ✅ |
| `GET /api/users/{id}/permissions` | 📋 |
| `POST /api/auth/refresh` | 📋 |

## Team-Rituale
- Daily Sync: 09:00 CET (10 min)
- API Review: Mi 14:00 CET
- Siehe `.github/TEAM_SYNC.yml`

## Wichtige Befehle
```bash
# Dev Server mit Keycloak
./mvnw quarkus:dev -Dquarkus.keycloak.devservices.enabled=true

# Tests mit Debug
./mvnw test -Dtest.debug=true

# Nur Backend Tests
./mvnw -Pgreen clean verify
```

## Kontakt
- Lead: Team BACK (@Max)
- Slack: #team-back
- Wiki: [Backend Architecture](../docs/adr/)