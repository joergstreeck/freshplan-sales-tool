# Backend Startup Problem - Maven Installation Fix

**Datum**: 2025-06-28
**Kategorie**: FIX
**Status**: ✅ Erfolgreich gelöst

## Problem

Das Backend konnte nicht gestartet werden aufgrund eines Maven Wrapper Fehlers:
```
-Dmaven.multiModuleProjectDirectory system property is not set.
```

## Ursache

Der Maven Wrapper (`mvnw`) hatte Probleme mit der Umgebungsvariable `maven.multiModuleProjectDirectory`. Dies ist ein bekanntes Problem bei Maven Wrapper auf macOS.

## Lösung

### 1. Maven Installation über Homebrew

```bash
brew install maven
```

Dies installierte:
- Maven 3.9.10
- OpenJDK 24 (als Dependency)

### 2. Backend Start mit korrekter Java Version

Da das Projekt Java 17 benötigt:

```bash
# Java 17 setzen
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Backend starten (ohne Keycloak da Realm-Config fehlt)
mvn quarkus:dev -Dquarkus.oidc.enabled=false -Dquarkus.analytics.disabled=true
```

### 3. Backend im Hintergrund starten

Für dauerhafte Nutzung:

```bash
nohup mvn quarkus:dev -Dquarkus.oidc.enabled=false -Dquarkus.analytics.disabled=true > backend.log 2>&1 &
```

## Verifizierung

Alle Backend-Endpoints sind jetzt erreichbar:

- **Swagger UI**: http://localhost:8080/q/swagger-ui/
- **Dev UI**: http://localhost:8080/q/dev-ui
- **API Ping**: http://localhost:8080/api/ping

```bash
# Test API Ping
curl -s http://localhost:8080/api/ping
# Response: {"dbTime":"2025-06-28 15:24:06.726657","message":"pong","user":"test-user","timestamp":"2025-06-28T13:24:06.727023Z"}
```

## Offene Punkte

1. **Doppelte User-Entity Klassen**: 
   - `de.freshplan.user.User`
   - `de.freshplan.domain.user.entity.User`
   - Verursacht Hibernate Warnungen, aber Backend läuft trotzdem

2. **Keycloak Integration**:
   - Realm-Config auf auth.z-catering.de fehlt noch
   - Aktuell mit `-Dquarkus.oidc.enabled=false` deaktiviert

## Empfehlung

Für zukünftige Entwickler sollte Maven direkt installiert werden, um Maven Wrapper Probleme zu vermeiden. Dies kann in die Entwicklungsumgebung-Dokumentation aufgenommen werden.