# 🔧 FIX: Keycloak Redirect URI Error - 16.07.2025 21:27

## Problem
Keycloak zeigte Fehler: "Ungültiger Parameter: redirect_uri"

Die Frontend-Anwendung läuft auf Port 5173, aber Keycloak war nur für Port 3000 konfiguriert.

## Ursache
In der Keycloak Realm-Konfiguration (`infrastructure/keycloak/freshplan-realm.json`) waren die URLs für den `freshplan-frontend` Client auf Port 3000 eingestellt:
- rootUrl: http://localhost:3000
- redirectUris: http://localhost:3000/*
- webOrigins: http://localhost:3000

## Lösung
1. Realm-Konfiguration angepasst auf Port 5173
2. Beide Ports (3000 und 5173) in redirectUris und webOrigins aufgenommen für Flexibilität
3. Keycloak neu gestartet mit aktualisierter Konfiguration

### Geänderte Datei:
- `infrastructure/keycloak/freshplan-realm.json`

### Code-Änderungen:
```json
"rootUrl": "http://localhost:5173",
"adminUrl": "http://localhost:5173", 
"baseUrl": "http://localhost:5173",
"redirectUris": [
  "http://localhost:5173/*",
  "http://localhost:3000/*"
],
"webOrigins": [
  "http://localhost:5173",
  "http://localhost:3000"
]
```

### Keycloak Neustart:
```bash
docker-compose -f docker-compose.keycloak.yml down
docker-compose -f docker-compose.keycloak.yml up -d keycloak
```

## Ergebnis
✅ Keycloak akzeptiert jetzt redirect_uri von Port 5173
✅ Frontend kann sich erfolgreich mit Keycloak authentifizieren
✅ Beide Ports (3000 und 5173) werden unterstützt für Flexibilität

## Hinweis
Die Realm-Konfiguration wird beim Start von Keycloak automatisch importiert durch die `--import-realm` Option in docker-compose.