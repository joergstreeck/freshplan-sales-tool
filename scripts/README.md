# Cleanup Scripts für FreshPlan Sales Tool

## 🧹 Verfügbare Scripts

### 0. `test-app.sh` - Lockerer App-Test
**Verwendung:**
```bash
./scripts/test-app.sh
```

**Features:**
- ✅ Lockerer Test der wichtigsten Funktionen
- ✅ Frontend, Backend, Users-Route
- ✅ Toleranter gegenüber kleinen Problemen
- ✅ Schnell (5s Timeout pro Test)

### 1. `cleanup.sh` - Vollständige Bereinigung
**Verwendung:**
```bash
./scripts/cleanup.sh
```

**Features:**
- ✅ Entfernt macOS System-Dateien (.DS_Store, etc.)
- ✅ Löscht Editor-Backup-Dateien (*~, *.swp)
- ✅ Bereinigt temporäre Dateien
- ✅ Entfernt Frontend dist/ (sicher - kann neu gebaut werden)
- ✅ Löscht Coverage Reports
- ✅ Bereinigt alte Logs (>7 Tage)
- ✅ Entfernt inaktive Python venv/
- ✅ Löscht alte Backup-Ordner (>30 Tage)

**Interaktiver Modus:**
```bash
INTERACTIVE=true ./scripts/cleanup.sh
```

### 2. `quick-cleanup.sh` - Tägliche Schnellbereinigung
**Verwendung:**
```bash
./scripts/quick-cleanup.sh
```

**Features:**
- ✅ Nur sichere Dateien (keine Build-Artefakte)
- ✅ .DS_Store, temporäre Dateien, alte Logs
- ✅ Keine Rückfragen - für automatische Ausführung

## 🔄 Automatisierung

### Git Hook (empfohlen)
Füge zu `.git/hooks/pre-commit` hinzu:
```bash
#!/bin/bash
./scripts/quick-cleanup.sh
```

### Cron Job (optional)
Wöchentliche Bereinigung:
```bash
# Jeden Sonntag um 2 Uhr
0 2 * * 0 cd /path/to/freshplan-sales-tool && ./scripts/cleanup.sh
```

## ⚠️ Wichtige Hinweise

- **Sichere Dateien:** Scripts löschen nur Dateien die sicher regeneriert werden können
- **Backup:** Bei Unsicherheit: `INTERACTIVE=true` Modus verwenden
- **Build-Artefakte:** Können mit `npm run build` / `./mvnw package` neu erstellt werden
- **Git-Tracking:** Alle ignorierten Dateitypen sind in `.gitignore` definiert

## 🎯 Nutzen

- **Speicherplatz:** Hält das Repository schlank
- **Performance:** Weniger Dateien = schnellere Git-Operationen
- **Sauberkeit:** Verhindert Ansammlung von Altlasten
- **Automatisch:** Einmal einrichten, dann automatisch sauber

---
*Erstellt: 08.06.2025 - Nach erfolgreicher 246MB Aufräumaktion*