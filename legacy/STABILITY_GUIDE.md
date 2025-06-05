# FreshPlan Stabilitäts-Guide

## 🛡️ Was wir für die Stabilität getan haben:

### 1. **Konfigurationen extern gespeichert**
Alle wichtigen Einstellungen sind jetzt in separaten Dateien:

- **`config/ui.config.json`** - Alle UI/Design-Einstellungen (Farben, Abstände, etc.)
- **`config/business-rules.config.json`** - Geschäftslogik (Rabatte, Regeln, etc.)
- **`config/text-content.config.json`** - Alle Texte und Übersetzungen

**Vorteil**: Bei Code-Änderungen bleiben Ihre Einstellungen erhalten!

### 2. **Backup-System**
```bash
# Vor Änderungen ausführen:
./scripts/backup-configs.sh
```
Erstellt automatisch ein Backup aller wichtigen Dateien mit Zeitstempel.

### 3. **Verifizierungs-Script**
```bash
# Installation prüfen:
./scripts/verify-installation.sh
```
Prüft ob alle Dateien vorhanden sind und die Umgebung korrekt ist.

### 4. **E2E-Tests**
```bash
# Funktionalität testen:
npm run test:e2e
```
Testet automatisch ob alle Features im Browser funktionieren.

### 5. **Stabile Standalone-Version**
`freshplan-complete.html` - Diese Datei funktioniert IMMER ohne Server!

## 📋 Checkliste für stabile Entwicklung:

### Vor Änderungen:
- [ ] Backup erstellen: `./scripts/backup-configs.sh`
- [ ] Tests laufen lassen: `npm run test:e2e`
- [ ] Development Server nutzen: `npm run dev`

### Nach Änderungen:
- [ ] Tests ausführen: `npm run test:e2e`
- [ ] Build erstellen: `npm run build`
- [ ] Installation verifizieren: `./scripts/verify-installation.sh`

### Bei Problemen:
1. Backup wiederherstellen: `cp backups/[DATUM]/* config/`
2. Standalone-Version nutzen: `freshplan-complete.html`
3. E2E-Tests debuggen: `npm run test:e2e:debug`

## 🔧 Einstellungen ändern:

### UI/Design anpassen:
```json
// config/ui.config.json
{
  "theme": {
    "colors": {
      "primary": "#004f7b",  // ← Hauptfarbe ändern
      "primaryGreen": "#94c456"  // ← Akzentfarbe ändern
    }
  }
}
```

### Geschäftsregeln anpassen:
```json
// config/business-rules.config.json
{
  "discounts": {
    "maxTotal": 15,  // ← Maximaler Rabatt
    "minOrderValue": 5000  // ← Mindestbestellwert
  }
}
```

### Texte ändern:
```json
// config/text-content.config.json
{
  "de": {
    "common": {
      "appName": "FreshPlan",  // ← App-Name
      "tagline": "So einfach, schnell und lecker!"  // ← Slogan
    }
  }
}
```

## 🚀 Deployment-Optionen:

### Option 1: Standalone (Empfohlen für Demos)
1. Kopieren Sie `freshplan-complete.html` auf einen Webserver
2. Fertig! Keine weiteren Dateien nötig.

### Option 2: Full Build (Für Produktion)
```bash
npm run build
# Inhalt von dist/ auf Webserver kopieren
```

### Option 3: Development
```bash
npm run dev
# Für lokale Entwicklung
```

## ⚠️ Häufige Probleme vermeiden:

### ❌ NICHT machen:
- `index.html` direkt öffnen (ES6 Module funktionieren nicht)
- Änderungen ohne Tests durchführen
- Konfigurationen im Code ändern

### ✅ STATTDESSEN:
- `npm run dev` für Entwicklung nutzen
- `freshplan-complete.html` für Demos
- Konfigurationen in `config/` Dateien ändern
- Vor Änderungen Backup erstellen

## 📊 Monitoring:

### Test-Status prüfen:
```bash
# Unit-Tests
npm test

# E2E-Tests
npm run test:e2e

# Alle Tests
npm run test && npm run test:e2e
```

### Code-Qualität:
```bash
# TypeScript prüfen
npm run type-check

# Linting
npm run lint

# Formatierung
npm run format:check
```

## 🆘 Notfall-Plan:

Falls etwas schief geht:

1. **Sofort-Lösung**: 
   - Öffnen Sie `freshplan-complete.html`
   - Diese funktioniert immer!

2. **Backup wiederherstellen**:
   ```bash
   ls backups/  # Verfügbare Backups anzeigen
   cp -r backups/[NEUESTES_BACKUP]/* config/
   ```

3. **Clean Install**:
   ```bash
   rm -rf node_modules dist
   npm install
   npm run build
   ```

4. **Support**:
   - E2E-Test Logs prüfen: `test-results/`
   - Console Errors im Browser prüfen
   - Git History nutzen: `git log --oneline`

---

Mit diesem Setup ist Ihre FreshPlan-Installation robust und zukunftssicher! 🎉