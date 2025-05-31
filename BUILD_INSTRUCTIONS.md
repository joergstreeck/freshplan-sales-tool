# Build-Anweisungen für FreshPlan Sales Tool

## Voraussetzungen

- Node.js >= 18.0.0
- npm >= 9.0.0

## Installation und Build

### 1. Abhängigkeiten installieren

Aufgrund von Berechtigungsproblemen im npm-Cache müssen Sie zuerst die Berechtigungen korrigieren:

```bash
# Cache-Berechtigungen korrigieren
sudo chown -R $(whoami) ~/.npm

# Dann Abhängigkeiten installieren
npm ci
```

Falls es Probleme mit Peer-Dependencies gibt:
```bash
npm ci --legacy-peer-deps
```

### 2. Tests ausführen

```bash
# Alle Tests ausführen
npm test

# Tests mit Coverage
npm run test:coverage

# Tests im Watch-Modus
npm run test:watch

# Tests mit UI
npm run test:ui
```

### 3. Produktions-Build erstellen

```bash
# TypeScript kompilieren und Bundle erstellen
npm run build
```

Dies erstellt:
- Kompilierte TypeScript-Dateien
- Optimiertes Bundle im `dist/` Verzeichnis
- Code-Splitting für bessere Performance

### 4. Entwicklungsserver starten

```bash
# Vite Entwicklungsserver
npm run dev
```

Der Server läuft dann auf http://localhost:5173

### 5. Vorschau des Builds

```bash
# Production-Build lokal testen
npm run preview
```

## Weitere nützliche Befehle

```bash
# TypeScript Type-Checking
npm run type-check

# Linting
npm run lint
npm run lint:fix

# Code formatieren
npm run format

# Aufräumen
npm run clean
```

## Projektstruktur

```
src/
├── core/           # Kern-Module (EventBus, DOMHelper, Module)
├── modules/        # Anwendungsmodule (alle .ts Dateien)
├── store/          # Zustand State Management
├── types/          # TypeScript Typdefinitionen
├── utils/          # Hilfsfunktionen
├── FreshPlanApp.ts # Haupt-Anwendungsklasse
└── main.ts         # Einstiegspunkt
```

## Fehlerbehebung

### npm Cache Probleme
```bash
# Cache löschen
npm cache clean --force

# Oder mit neuem Cache-Verzeichnis
npm install --cache /tmp/npm-cache
```

### TypeScript Fehler
- Stellen Sie sicher, dass alle Importe korrekt sind
- Prüfen Sie die tsconfig.json Einstellungen
- Führen Sie `npm run type-check` aus

### Build Fehler
- Löschen Sie `node_modules` und `package-lock.json`
- Führen Sie `npm install` erneut aus
- Prüfen Sie die Vite-Konfiguration