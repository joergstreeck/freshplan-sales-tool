# Docker für macOS installieren

## 1 · Docker Desktop via Homebrew (✓ empfohlen)

```bash
brew update
brew install --cask docker        # lädt ≈ 500 MB
open -a Docker                    # Whale-Icon erscheint
```

## 2 · Alternativ: Direkt-Download

Lade die passende DMG:

- **Apple Silicon** → https://docs.docker.com/desktop/install/mac/install-mac/#apple-silicon
- **Intel** → https://docs.docker.com/desktop/install/mac/install-mac/#intel

Docker Desktop in **Programme** ziehen, DMG auswerfen, App öffnen.

Beim ersten Start verlangt macOS Admin-Rechte für Netzwerk-Treiber; bestätigen und warten bis das Whale-Icon grün ist.

## 3 · Installation prüfen

```bash
docker --version
docker compose version
docker info | head -n 20          # Engine OK?
```

## 4 · Dev-Stack starten

```bash
cd infrastructure
docker compose pull               # Bilder vorkacheln
docker compose up -d              # Postgres (5432) & Keycloak (8180)
```

## 5 · Backend starten

```bash
cd ../backend
./mvnw -Dmaven.multiModuleProjectDirectory=$(pwd) quarkus:dev
```

## Trouble-Shooting

| Symptom | Lösung |
|---------|--------|
| `port 5432 already in use` | Lokalen Postgres beenden oder Port in docker-compose.yml ändern. |
| `docker: command not found` | Neues Terminal öffnen – $PATH wurde erst nach Installation gesetzt. |
| Firewall-Popup | „Zulassen" (erforderlich für DB/Keycloak-Ports). |

## Erfolgs-Indikatoren

Nach erfolgreichem Start sollten in den Quarkus-Logs erscheinen:
- `Datasource <default> connected`
- `OIDC Tenant -> CONNECTED`

Das zeigt, dass beide Container (PostgreSQL & Keycloak) erreichbar sind.

## Nächster Schritt

Team BACK installiert Docker nach dieser Anleitung, führt `docker compose up -d` aus und bestätigt im TEAM_SYNC_LOG.md, dass Backend + Keycloak laufen. Danach Quarkus Dev neu starten ➜ Sprint-2 kann losgehen. 💚