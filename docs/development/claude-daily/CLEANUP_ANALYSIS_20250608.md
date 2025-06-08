# Cleanup-Analyse - FreshPlan Sales Tool
**Datum:** 08.06.2025
**Analysiert von:** Claude

## 🔍 Zusammenfassung der gefundenen Altlasten

### 1. **Sicher löschbar** ✅

#### Build-Artefakte und temporäre Ordner:
- `/dist/` - Alter Build-Output (enthält Legacy JS/CSS Bundle)
- `/frontend/dist/` - Frontend Build-Output
- `/backend/target/` - Maven Build-Output (JAR-Dateien, Classes)
- `/temp/` - Temporärer Arbeitsordner (laut README für temporäre Dateien)
- `/.archive/_customer-module-v2.full.bak` - Alte Backup-Datei

#### macOS System-Dateien:
- `.DS_Store` Dateien (3 Stück):
  - `/Users/joergstreeck/freshplan-sales-tool/.DS_Store`
  - `/Users/joergstreeck/freshplan-sales-tool/docs/.DS_Store`
  - `/Users/joergstreeck/freshplan-sales-tool/docs/business/.DS_Store`

#### Entwicklungsumgebungen:
- `/venv/` - Python Virtual Environment (316 Bytes pyvenv.cfg zeigt: nicht aktiv genutzt)
- `/node_modules/` - Root-Level node_modules (sollte nur in frontend/backend sein)

### 2. **Prüfung erforderlich** ⚠️

#### .env Dateien:
- `/frontend/.env` - Könnte aktuelle Konfiguration enthalten
- `/frontend/.env.test` - Test-Konfiguration
- `/frontend/.env.example` - Beispiel-Konfiguration (sollte bleiben)
- `/frontend/.env.test.example` - Test-Beispiel-Konfiguration (sollte bleiben)

#### TODO/FIXME im Code:
- `/backend/src/main/java/de/freshplan/api/CalculatorResource.java` - Zeile 87: `// TODO: Implement rules endpoint if needed`
- `/frontend/src/shared/lib/apiClient.ts` - Zeile 86: `// TODO: Integrate with AuthContext once Keycloak is ready`

### 3. **Möglicherweise wichtig** 🤔

#### Kein legacy/ Ordner gefunden:
- Der in der Struktur erwähnte `/legacy/` Ordner existiert nicht (mehr?)
- Dies könnte bedeuten, dass er bereits gelöscht wurde oder nie erstellt wurde

## 📊 Speicherplatz-Analyse

### Geschätzter freigebbarer Speicherplatz:
1. `/dist/` - ~5-10 MB (Legacy Bundles)
2. `/frontend/dist/` - ~2-5 MB
3. `/backend/target/` - ~50-100 MB (JAR-Dateien, Classes)
4. `/node_modules/` (root) - ~200-500 MB
5. `/venv/` - ~50-100 MB
6. `.DS_Store` Dateien - ~18 KB

**Gesamt: ~300-700 MB potentiell freizugeben**

## 🚀 Empfohlene Aktionen

### Sofort löschbar:
```bash
# Build-Artefakte
rm -rf /Users/joergstreeck/freshplan-sales-tool/dist/
rm -rf /Users/joergstreeck/freshplan-sales-tool/frontend/dist/
rm -rf /Users/joergstreeck/freshplan-sales-tool/backend/target/

# Temporäre Dateien
rm -rf /Users/joergstreeck/freshplan-sales-tool/temp/
rm -f /Users/joergstreeck/freshplan-sales-tool/.archive/_customer-module-v2.full.bak

# System-Dateien
find /Users/joergstreeck/freshplan-sales-tool -name ".DS_Store" -delete

# Ungenutzte Entwicklungsumgebungen
rm -rf /Users/joergstreeck/freshplan-sales-tool/venv/
rm -rf /Users/joergstreeck/freshplan-sales-tool/node_modules/
```

### Nach Prüfung:
1. **Prüfe .env Dateien** - Sichere wichtige Konfigurationen bevor du löschst
2. **TODO-Kommentare** - Entscheide ob die Features implementiert werden sollen

### Für .gitignore hinzufügen:
```gitignore
# Build-Artefakte
dist/
target/
coverage/

# System-Dateien
.DS_Store
Thumbs.db

# Temporäre Dateien
*.bak
*.backup
*.old
*~
*.swp
*.swo

# Entwicklungsumgebungen
venv/
.venv/

# Logs
*.log
npm-debug.log*
yarn-debug.log*
yarn-error.log*
```

## 🔧 Automatisierung für die Zukunft

### Cleanup-Script erstellen:
```bash
#!/bin/bash
# cleanup.sh - Entfernt Build-Artefakte und temporäre Dateien

echo "🧹 Cleaning up FreshPlan Sales Tool..."

# Build-Artefakte
find . -name "dist" -type d -not -path "*/node_modules/*" -exec rm -rf {} +
find . -name "target" -type d -exec rm -rf {} +
find . -name "coverage" -type d -exec rm -rf {} +

# System-Dateien
find . -name ".DS_Store" -delete
find . -name "Thumbs.db" -delete

# Temporäre Dateien
find . -name "*.bak" -delete
find . -name "*.backup" -delete
find . -name "*.old" -delete
find . -name "*~" -delete

echo "✅ Cleanup complete!"
```

## 📝 Notizen

1. **Fehlender legacy/ Ordner**: Der in der Projektstruktur erwähnte legacy-Ordner existiert nicht. Dies sollte geklärt werden.

2. **Root node_modules**: Es gibt einen node_modules Ordner im Hauptverzeichnis, der normalerweise nicht dort sein sollte (sollte nur in frontend/backend sein).

3. **Wenige Altlasten**: Das Projekt ist relativ sauber. Die meisten gefundenen Dateien sind normale Build-Artefakte.

4. **TODOs im Code**: Nur 2 TODO-Kommentare gefunden, beide scheinen legitime zukünftige Features zu sein.

## ✅ Fazit

Das Projekt ist insgesamt sehr sauber gehalten. Die meisten "Altlasten" sind normale Build-Artefakte, die bei der Entwicklung entstehen. Nach dem Löschen der empfohlenen Dateien und dem Hinzufügen der .gitignore-Einträge sollte das Projekt optimal aufgeräumt sein.