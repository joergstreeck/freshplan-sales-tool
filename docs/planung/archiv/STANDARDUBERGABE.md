# STANDARDÜBERGABE - Erweiterte Version mit Troubleshooting

**🔧 Nur bei ernsten Problemen verwenden!**

> Für normale Sessions verwende STANDARDUBERGABE_NEU.md

## Inhaltsverzeichnis
1. [Häufige Probleme und Lösungen](#häufige-probleme-und-lösungen)
2. [Erweiterte Debugging-Strategien](#erweiterte-debugging-strategien)
3. [Historischer Kontext](#historischer-kontext)
4. [Notfall-Prozeduren](#notfall-prozeduren)

## Häufige Probleme und Lösungen

### 🔴 Problem: Tests schlagen fehl mit CDI-Fehler
**Symptom:**
```
declares an interceptor binding but it must be ignored per CDI rules
```

**Lösung:**
```java
// Option 1: @TestTransaction auf Klassen-Ebene
@QuarkusTest
@TestTransaction  // Hier statt in @Nested
public class CustomerRepositoryTest {
    // Keine @Nested Classes verwenden
}

// Option 2: Tests ohne @Nested strukturieren
@Test
void testCustomerCreation() { }
@Test
void testCustomerDeletion() { }
```

### 🔴 Problem: H2 URL wird trotz PostgreSQL-Konfiguration geladen
**Symptom:**
```
Driver does not support the provided URL: jdbc:h2:mem:test
```

**Debugging-Schritte:**
```bash
# 1. Suche nach H2-Konfiguration
find backend -name "*.properties" -o -name "*.xml" | xargs grep -l "h2:mem"

# 2. Prüfe Test-Classpath
./mvnw dependency:tree | grep h2

# 3. Clean Build
./mvnw clean
rm -rf ~/.m2/repository/io/quarkus/quarkus-test-h2/

# 4. Explizite PostgreSQL-Konfiguration
export QUARKUS_DATASOURCE_DB_KIND=postgresql
export QUARKUS_DATASOURCE_DEVSERVICES_ENABLED=true
```

### 🔴 Problem: Port bereits belegt
**Symptom:**
```
Address already in use: bind
```

**Lösung:**
```bash
# Finde und beende Prozess
lsof -ti:8080 | xargs kill -9
# oder
./scripts/stop-dev.sh
```

### 🔴 Problem: Maven Build schlägt fehl
**Symptom:**
```
Could not resolve dependencies
```

**Lösung:**
```bash
# 1. Cache löschen
rm -rf ~/.m2/repository

# 2. Offline-Modus deaktivieren
./mvnw clean install -U

# 3. Spezifische Version erzwingen
./mvnw versions:display-dependency-updates
```

## Erweiterte Debugging-Strategien

### 🐛 Strategie 1: Schrittweise Isolation
```bash
# 1. Minimaler Test
./mvnw test -Dtest=UserServiceTest#testMinimal

# 2. Mit Debug-Output
./mvnw test -Dtest=UserServiceTest -X

# 3. Nur Compilation
./mvnw test-compile

# 4. Ohne Tests
./mvnw package -DskipTests
```

### 🐛 Strategie 2: Logging erhöhen
```properties
# In application.properties
quarkus.log.level=DEBUG
quarkus.log.category."de.freshplan".level=TRACE
quarkus.log.category."org.hibernate".level=DEBUG
```

### 🐛 Strategie 3: Remote Debugging
```bash
# Backend mit Debug-Port starten
./mvnw quarkus:dev -Ddebug=5005

# In IDE: Remote Debug Configuration auf localhost:5005
```

## Historischer Kontext

### Migration von Legacy zu Quarkus
- **Warum Quarkus?** Cloud-native, schnelle Startzeiten, geringe Memory-Footprint
- **Migration-Strategie:** Schrittweise Feature-Migration, API-Kompatibilität
- **Lessons Learned:** 
  - Panache vereinfacht JPA erheblich
  - Dev Services sind Gold wert
  - Native Builds brauchen Anpassungen

### Architektur-Entscheidungen
1. **Soft Delete Pattern:** Compliance-Anforderungen, Audit-Trail
2. **Event-Driven Updates:** Skalierbarkeit, lose Kopplung
3. **DTO-First API:** Versionierung, Backward-Compatibility

## Notfall-Prozeduren

### 🚨 Kompletter Reset
```bash
#!/bin/bash
# WARNUNG: Löscht alle lokalen Änderungen!

# 1. Git Reset
git stash
git checkout main
git pull origin main

# 2. Clean Build
cd backend
./mvnw clean
rm -rf target/

cd ../frontend
rm -rf node_modules/
npm install

# 3. Database Reset
docker-compose down -v
docker-compose up -d postgres

# 4. Neu starten
./scripts/start-dev.sh
```

### 🚨 Rollback auf funktionierenden Stand
```bash
# Letzten funktionierenden Commit finden
git log --oneline --grep="✅" -10

# Auf diesen Stand zurück
git checkout <commit-hash>

# Neuen Branch erstellen
git checkout -b hotfix/rollback-to-stable
```

### 🚨 Produktions-Hotfix
```bash
# 1. Hotfix-Branch von main
git checkout main
git pull
git checkout -b hotfix/critical-fix

# 2. Minimaler Fix
# ... Änderungen ...

# 3. Schnell-Test
./mvnw test -Dtest=AffectedTest

# 4. Direct Push (Ausnahme!)
git push origin hotfix/critical-fix

# 5. Sofort PR erstellen
gh pr create --title "HOTFIX: [Beschreibung]" --body "Critical fix for..."
```

## Kontakt bei kritischen Problemen

1. **Erst:** Dieses Dokument durcharbeiten
2. **Dann:** Team in Slack informieren
3. **Notfall:** Eskalation gemäß Incident Response Plan

---

**Denke daran:** Dieses Dokument ist für Ausnahmesituationen. 
Für normale Arbeit → STANDARDUBERGABE_NEU.md