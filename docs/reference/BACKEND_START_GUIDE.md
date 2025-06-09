# Backend Start Guide

**📅 Aktuelles Datum: 09.06.2025 (System: 09.06.2025)**

## Aktuelle Situation

Das Backend benötigt folgende Services:
- **PostgreSQL** auf Port 5432
- **Keycloak** auf Port 8180

## Option 1: Docker Desktop (Empfohlen)

1. **Docker Desktop installieren:**
   - Download: https://www.docker.com/products/docker-desktop/
   - Installation durchführen und Docker starten

2. **Services starten:**
   ```bash
   cd infrastructure
   ./start-local-env.sh
   ```

3. **Backend starten:**
   ```bash
   cd backend
   ~/apache-maven-3.9.6/bin/mvn quarkus:dev
   ```

## Option 2: PostgreSQL lokal

1. **PostgreSQL installieren:**
   ```bash
   # Versuche mit Homebrew
   brew install postgresql@15
   
   # Oder manuell von https://www.postgresql.org/download/macosx/
   ```

2. **PostgreSQL starten:**
   ```bash
   brew services start postgresql@15
   # oder
   pg_ctl -D /usr/local/var/postgresql@15 start
   ```

3. **Datenbank einrichten:**
   ```bash
   createdb freshplan
   createuser freshplan
   psql -c "ALTER USER freshplan WITH PASSWORD 'freshplan';"
   psql -c "GRANT ALL PRIVILEGES ON DATABASE freshplan TO freshplan;"
   ```

4. **Backend ohne Keycloak starten:**
   ```bash
   cd backend
   ~/apache-maven-3.9.6/bin/mvn quarkus:dev \
     -Dquarkus.oidc.enabled=false \
     -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/freshplan
   ```

## Option 3: Frontend mit Mocks nutzen

Das Frontend funktioniert bereits vollständig mit Mock-Daten:

1. **Frontend starten:**
   ```bash
   cd frontend
   npm run dev
   ```

2. **Features testen:**
   - Calculator: http://localhost:5173/calculator
   - Integration Tests: http://localhost:5173/integration-test
   - Users: http://localhost:5173/users

Die MSW (Mock Service Worker) Mocks implementieren die komplette Business Logic und sind identisch zum geplanten Backend-Verhalten.

## Test-Script

Um den Backend-Status zu prüfen:
```bash
cd frontend
node test-backend-integration.js
```

## Troubleshooting

### Maven nicht gefunden
```bash
# Maven ist bereits heruntergeladen in:
~/apache-maven-3.9.6/bin/mvn
```

### Port bereits belegt
```bash
# Prüfe was auf Port 8080 läuft
lsof -i :8080

# Prozess beenden
kill -9 <PID>
```

### Datenbank-Verbindung schlägt fehl
```bash
# Prüfe ob PostgreSQL läuft
pg_isready -h localhost -p 5432

# PostgreSQL Logs anzeigen
tail -f /usr/local/var/log/postgresql@15.log
```