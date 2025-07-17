# 🔧 FIX: Keycloak Database Error - 16.07.2025 21:25

## Problem
Nach der Änderung der Keycloak-Konfiguration zeigte die Frontend-Anwendung einen "Serverfehler" und leitete zu Keycloak weiter mit:
```
http://localhost:8180/realms/freshplan/protocol/openid-connect/auth?...&prompt=none
```

## Ursache
Keycloak hatte einen internen Datenbankfehler in der H2-Datenbank. Die Docker Logs zeigten einen SQL Parser Error:
```
at org.h2.command.Parser.parseQueryPrimary(Parser.java:2708)
```

Dies war kein Problem mit unserem Code, sondern ein internes Keycloak-Problem.

## Lösung
Neustart des Keycloak Docker Containers:
```bash
docker restart freshplan-keycloak
```

## Ergebnis
✅ Keycloak läuft wieder normal
✅ Frontend kann wieder aufgerufen werden
✅ Authentifizierung funktioniert wieder

## Hinweis
Dies kann passieren, wenn:
- Die H2-Datenbank von Keycloak korrupt wird
- Keycloak unsauber beendet wurde
- Docker-Container länger läuft ohne Neustart

Für Production sollte eine externe Datenbank (PostgreSQL) für Keycloak verwendet werden statt der eingebetteten H2-Datenbank.