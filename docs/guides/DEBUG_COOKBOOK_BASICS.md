# 🏗️ SERVICE BASICS & QUICK START

**Zweck:** Grundlegende Service-Informationen und Start-Befehle  
**Zielgruppe:** Neue Claude-Sessions und Entwickler  

---

## 📊 Service Übersicht & Ports

| Service | Port | Technologie | Zweck |
|---------|------|-------------|-------|
| **Frontend** | `5173` | Vite/React 18 | Single Page Application |
| **Backend** | `8080` | Quarkus/Java 17 | REST API |
| **PostgreSQL** | `5432` | PostgreSQL 15+ | Datenbank |
| **Keycloak** | `8180` | Keycloak 25 | Auth Service |

## ⚠️ KRITISCHE REQUIREMENTS

### Java Version
```bash
# MUSS Java 17 sein!
java -version
# Expected: openjdk version "17.0.x"

# Bei falscher Version setzen:
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
```

### Node Version
```bash
# Node 18+ erforderlich
node --version
# Expected: v18.x.x oder höher
```

## 🚀 Service Management

### Alle Services starten
```bash
# Empfohlener Weg - startet alles in korrekter Reihenfolge
/Users/joergstreeck/freshplan-sales-tool/scripts/start-dev.sh
```

### Service Status prüfen
```bash
/Users/joergstreeck/freshplan-sales-tool/scripts/check-services.sh
```

### Einzelne Services starten

#### Frontend
```bash
cd /Users/joergstreeck/freshplan-sales-tool/frontend
npm run dev
```

#### Backend
```bash
# Java 17 sicherstellen!
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
cd /Users/joergstreeck/freshplan-sales-tool/backend
mvn quarkus:dev
```

#### Backend Manager (empfohlen)
```bash
/Users/joergstreeck/freshplan-sales-tool/scripts/backend-manager.sh start
/Users/joergstreeck/freshplan-sales-tool/scripts/backend-manager.sh status
/Users/joergstreeck/freshplan-sales-tool/scripts/backend-manager.sh restart
/Users/joergstreeck/freshplan-sales-tool/scripts/backend-manager.sh stop
```

### Services stoppen
```bash
/Users/joergstreeck/freshplan-sales-tool/scripts/stop-dev.sh
```

## 🔍 Health Checks

```bash
# Backend Health
curl http://localhost:8080/q/health

# Backend API Test
curl http://localhost:8080/api/customers

# Frontend Check
curl http://localhost:5173

# Keycloak Check  
curl http://localhost:8180

# PostgreSQL Check
psql -h localhost -p 5432 -U freshplan -d freshplan -c "SELECT 1;"
```

## 📍 Wichtige Pfade

```bash
# Projekt Root
cd /Users/joergstreeck/freshplan-sales-tool

# Logs
tail -f /Users/joergstreeck/freshplan-sales-tool/logs/backend.log
tail -f /Users/joergstreeck/freshplan-sales-tool/logs/frontend.log

# Configs
/Users/joergstreeck/freshplan-sales-tool/backend/src/main/resources/application.properties
/Users/joergstreeck/freshplan-sales-tool/frontend/.env
```

## 🆘 Erste Hilfe

Bei Problemen:
1. Service Status prüfen: `/Users/joergstreeck/freshplan-sales-tool/scripts/check-services.sh`
2. Java Version prüfen: `java -version` (MUSS 17 sein!)
3. Ports prüfen: `lsof -i :5173` und `lsof -i :8080`
4. Vollständiges Troubleshooting: [DEBUG_COOKBOOK.md](/Users/joergstreeck/freshplan-sales-tool/docs/guides/DEBUG_COOKBOOK.md)

---

**Zurück zu:** [DEBUG_COOKBOOK.md](/Users/joergstreeck/freshplan-sales-tool/docs/guides/DEBUG_COOKBOOK.md)