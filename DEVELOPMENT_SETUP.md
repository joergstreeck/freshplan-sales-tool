# FreshPlan Development Setup Guide

## Quick Start

1. **Xcode Command Line Tools installieren** (nach macOS Update erforderlich):
   ```bash
   xcode-select --install
   ```

2. **Development Environment starten**:
   ```bash
   ./start-dev.sh
   ```

## Services

Nach dem Start sind folgende Services verfügbar:

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/q/swagger-ui
- **Keycloak**: http://localhost:8180 (admin/admin)
- **PostgreSQL**: localhost:5432 (freshplan/freshplan)

## Development Login (ohne Keycloak)

Für die lokale Entwicklung ohne Keycloak-Setup:

1. Gehe zu http://localhost:5173/login-bypass
2. Wähle eine Rolle aus:
   - **Admin**: Vollzugriff auf alle Features
   - **Manager**: Kann User verwalten und Reports einsehen
   - **Sales**: Kann Bestellungen erstellen und verwalten
   - **Viewer**: Nur Lesezugriff

## Test-User

Das System erstellt automatisch folgende Test-User:

| Username | Email | Rolle | 
|----------|-------|-------|
| admin | admin@freshplan.de | admin |
| manager | manager@freshplan.de | manager |
| sales | sales@freshplan.de | sales |
| viewer | viewer@freshplan.de | viewer |
| max.mustermann | max@example.com | sales |
| erika.musterfrau | erika@example.com | sales |

## Technische Details

### Development-spezifische Konfiguration

1. **Backend**: OIDC ist im Dev-Modus deaktiviert (`%dev.quarkus.oidc.enabled=false`)
2. **Frontend**: Liest Mock-JWT-Token aus localStorage
3. **DevUserResource**: Ersetzt die gesicherte UserResource im Dev-Modus

### Wichtige Dateien

- `start-dev.sh`: Start-Script für alle Services
- `LoginBypassPage.tsx`: Mock-Login für Development
- `DevUserResource.java`: Ungesicherte API für Development
- `DevDataInitializer.java`: Erstellt Test-User beim Start

## Troubleshooting

### Docker läuft nicht
```bash
# Docker Desktop starten oder:
brew install --cask docker
open /Applications/Docker.app
```

### Backend startet nicht
```bash
cd backend
./mvnw clean compile
./mvnw quarkus:dev
```

### Frontend Port belegt
```bash
# Port 5173 freigeben:
lsof -ti:5173 | xargs kill -9
```

### Datenbank-Verbindung fehlgeschlagen
```bash
cd infrastructure
docker-compose down -v
docker-compose up -d
```