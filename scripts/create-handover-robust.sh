#!/bin/bash

# Create handover document with PROMINENT migration warning and ROBUST error handling
# Usage: ./create-handover-robust.sh

set -euo pipefail  # Exit on error, undefined vars, pipe failures

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Error handler
error_exit() {
    echo -e "${RED}❌ ERROR: $1${NC}" >&2
    exit 1
}

# Warning handler
warn() {
    echo -e "${YELLOW}⚠️  WARNING: $1${NC}" >&2
}

# Check if we're in the right directory
if [ ! -f "CLAUDE.md" ] || [ ! -d "backend" ]; then
    error_exit "Must be run from FreshPlan project root directory!"
fi

# Get current datetime
DATETIME=$(date +"%Y-%m-%d %H:%M" 2>/dev/null) || DATETIME="$(date)"
DATE=$(date +"%Y-%m-%d" 2>/dev/null) || DATE="2025-08-02"
TIME=$(date +"%H-%M" 2>/dev/null) || TIME="00-00"

# Create directory if needed
HANDOVER_DIR="docs/claude-work/daily-work/$DATE"
mkdir -p "$HANDOVER_DIR" || error_exit "Cannot create handover directory"

# Define handover file
HANDOVER_FILE="$HANDOVER_DIR/${DATE}_HANDOVER_${TIME}.md"

echo -e "${GREEN}📝 Erstelle Übergabe-Dokument...${NC}"

# Get system info with fallbacks
JAVA_VERSION="UNKNOWN"
if command -v java &>/dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -1 | awk -F '"' '{print $2}' || echo "UNKNOWN")
fi

NODE_VERSION="UNKNOWN"
if command -v node &>/dev/null; then
    NODE_VERSION=$(node --version 2>/dev/null || echo "UNKNOWN")
fi

# Get migration info - CRITICAL! Multiple fallback strategies
NEXT_MIGRATION="V???"
LAST_MIGRATION="V???"

# Strategy 1: Read from MIGRATION_REGISTRY.md
if [ -f "docs/MIGRATION_REGISTRY.md" ]; then
    TEMP_NEXT=$(grep "NÄCHSTE VERFÜGBARE" docs/MIGRATION_REGISTRY.md 2>/dev/null | grep -o "V[0-9]\+" | head -1 || echo "")
    if [ -n "$TEMP_NEXT" ]; then
        NEXT_MIGRATION="$TEMP_NEXT"
    else
        warn "Could not extract next migration from MIGRATION_REGISTRY.md"
    fi
else
    warn "MIGRATION_REGISTRY.md not found!"
fi

# Strategy 2: If still unknown, try to calculate from existing migrations
if [ "$NEXT_MIGRATION" = "V???" ] && [ -d "backend/src/main/resources/db/migration" ]; then
    HIGHEST=$(ls backend/src/main/resources/db/migration/V*.sql 2>/dev/null | 
               sed 's/.*V\([0-9]\+\)__.*/\1/' | 
               sort -n | 
               tail -1 || echo "0")
    if [ "$HIGHEST" -gt 0 ]; then
        NEXT_NUM=$((HIGHEST + 1))
        NEXT_MIGRATION="V${NEXT_NUM}"
        warn "Calculated next migration from directory: $NEXT_MIGRATION"
    fi
fi

# Strategy 3: Use known fallback
if [ "$NEXT_MIGRATION" = "V???" ]; then
    NEXT_MIGRATION="V121"
    warn "Using hardcoded fallback: $NEXT_MIGRATION"
fi

# Get last applied migration - with timeout and fallback
if [ -d "backend" ] && [ -f "backend/mvnw" ]; then
    # Try to get from Flyway, but with strict timeout
    TEMP_LAST=$(cd backend && timeout 3 ./mvnw -q flyway:info 2>/dev/null | 
                grep "| [0-9]" | 
                tail -1 | 
                awk '{print $2}' || echo "")
    if [ -n "$TEMP_LAST" ]; then
        LAST_MIGRATION="$TEMP_LAST"
    else
        # Fallback: Check schema_version table directly if possible
        if command -v psql &>/dev/null; then
            TEMP_LAST=$(PGPASSWORD=freshplan psql -h localhost -U freshplan -d freshplan -t -c \
                       "SELECT version FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1" 2>/dev/null | 
                       xargs || echo "")
            if [ -n "$TEMP_LAST" ]; then
                LAST_MIGRATION="V${TEMP_LAST}"
            fi
        fi
    fi
fi

# Final fallback for last migration
if [ "$LAST_MIGRATION" = "V???" ] || [ -z "$LAST_MIGRATION" ]; then
    LAST_MIGRATION="V120"
    warn "Using known last migration: $LAST_MIGRATION"
fi

# Get git info with robust error handling
CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || echo "unknown")
LINES_CHANGED=$(git diff --stat 2>/dev/null | tail -1 | awk '{print $1}' | tr -d '\n' || echo "0")
NEW_MIGRATIONS=$(git status --porcelain 2>/dev/null | grep -c "A.*\.sql$" | tr -d '\n' || echo "0")
NEW_COMPONENTS=$(git status --porcelain 2>/dev/null | grep -c "A.*\.(tsx|jsx)$" | tr -d '\n' || echo "0")

# Validate numeric values
[[ "$LINES_CHANGED" =~ ^[0-9]+$ ]] || LINES_CHANGED="0"
[[ "$NEW_MIGRATIONS" =~ ^[0-9]+$ ]] || NEW_MIGRATIONS="0"
[[ "$NEW_COMPONENTS" =~ ^[0-9]+$ ]] || NEW_COMPONENTS="0"

# Create template with SUPER PROMINENT migration warning
cat > "$HANDOVER_FILE" << 'EOF'
# 🔄 STANDARDÜBERGABE - DATETIME_PLACEHOLDER

## 🚨🚨🚨 MIGRATION ALERT - LIES MICH ZUERST! 🚨🚨🚨
```
╔═══════════════════════════════════════════════════════════╗
║         NÄCHSTE FREIE MIGRATION: MIGRATION_NEXT_BIG       ║
║         LETZTE ANGEWENDETE: MIGRATION_LAST                ║
║         CHECK: cat docs/MIGRATION_REGISTRY.md             ║
╚═══════════════════════════════════════════════════════════╝
```
⚠️  WARNUNG: Bei Unsicherheit IMMER docs/MIGRATION_REGISTRY.md prüfen!

**WICHTIG: Lies ZUERST diese Dokumente in dieser Reihenfolge:**
1. `/docs/CLAUDE.md` (Arbeitsrichtlinien und Standards)
2. Diese Übergabe
3. `/docs/STANDARDUBERGABE_NEU.md` als Hauptanleitung
4. Bei DB-Arbeit: `/docs/DATABASE_MIGRATION_GUIDE.md`
5. **🚨 NEU:** `/docs/MIGRATION_REGISTRY.md` (KRITISCH für neue Migrationen!)

**3-STUFEN-SYSTEM für Übergaben:**
- **STANDARDUERGABE_NEU.md**: Hauptdokument mit 5 Schritten - IMMER zuerst lesen!
- **STANDARDUERGABE_KOMPAKT.md**: Ultra-kurze Quick-Reference für schnellen Überblick
- **STANDARDUERGABE.md**: Nur bei Problemen als Troubleshooting-Guide

## 🚨 KRITISCHE TECHNISCHE INFORMATIONEN

### 🚨 MIGRATION-WARNUNG - NEUE REGEL!
**VOR JEDER NEUEN MIGRATION:**
1. MIGRATION_REGISTRY.md lesen (`docs/MIGRATION_REGISTRY.md`)
2. Nächste freie Nummer verwenden (aktuell: **MIGRATION_NEXT**)
3. NIEMALS vergebene Nummern wiederverwenden
4. Nach Migration: Registry aktualisieren mit `./scripts/update-migration-registry.sh`

**Problem heute:** Massive Duplikate bei V6-V17 haben Backend lahmgelegt!
**Lösung:** V120__fix_migration_duplicates.sql erstellt (DATABASE_MIGRATION_GUIDE.md befolgt)

### 🖥️ Service-Konfiguration
| Service | Port | Technologie | Status |
|---------|------|-------------|--------|
| **Backend** | `8080` | Quarkus mit Java 17 | [Von Script prüfen] |
| **Frontend** | `5173` | React/Vite | [Von Script prüfen] |
| **PostgreSQL** | `5432` | PostgreSQL 15+ | [Von Script prüfen] |
| **Keycloak** | `8180` | Auth Service | [Von Script prüfen] |

### ⚠️ WICHTIGE HINWEISE
- **Java Version:** MUSS Java 17 sein! (aktuell: JAVA_VERSION_PLACEHOLDER)
- **Node Version:** v22.16.0+ erforderlich (aktuell: NODE_VERSION_PLACEHOLDER)
- **Working Directory:** `/Users/joergstreeck/freshplan-sales-tool`
- **Branch-Regel:** NIEMALS direkt in `main` pushen!

## 🎯 AKTUELLER STAND

### Git Status
```
[MANUAL: git status output einfügen]
```

### Aktives Modul
**Feature:** [MANUAL: aus .current-focus]
**Modul:** [MANUAL: aus .current-focus]
**Dokument:** [MANUAL: Pfad zum Arbeits-Dokument] ⭐
**Status:** [MANUAL: Backend/Frontend/Tests Status]

## 📋 WAS WURDE HEUTE GEMACHT?

[MANUAL: Detaillierte Liste der heutigen Aktivitäten]

## ✅ WAS FUNKTIONIERT?

[MANUAL: Verifizierte, funktionierende Features]

## 🚨 WELCHE FEHLER GIBT ES?

[MANUAL: Aktuelle Probleme mit Fehlermeldungen]

## Aktuelle TODOs - DATE_TIME_PLACEHOLDER

### Offene TODOs:
[MANUAL: Aus TodoRead kopieren]

### Erledigte TODOs dieser Session:
[MANUAL: Heute erledigte TODOs]

## 🚨 UNTERBRECHUNGEN
**Unterbrochen bei:** [MANUAL: Beschreibung wo unterbrochen]
**Datei:** [MANUAL: Pfad:Zeile]
**Nächster Schritt:** [MANUAL: Konkrete nächste Aufgabe]

## 🔧 NÄCHSTE SCHRITTE

[MANUAL: Priorisierte Liste mit Zeitschätzungen]

## 🆕 STRATEGISCHE PLÄNE

**Plan:** [MANUAL: Aktuelles Feature-Konzept] - Status: [MANUAL: IN ARBEIT/ABGESCHLOSSEN]

## 🗄️ DOKUMENTATIONS-UPDATES

**Guide-Update:** [MANUAL: Welche Guides wurden aktualisiert und warum]

## 📝 CHANGE LOGS DIESER SESSION
- [ ] [MANUAL: Change Log erstellt? Link einfügen]

## 🚀 QUICK START FÜR NÄCHSTE SESSION
```bash
# 1. Zum Projekt wechseln
cd /Users/joergstreeck/freshplan-sales-tool

# 2. System-Check und Services starten
./scripts/validate-config.sh
./scripts/check-services.sh

# Falls Services nicht laufen:
./scripts/start-services.sh

# 3. Git-Status
git status
git log --oneline -5

# 4. Aktives Modul anzeigen
./scripts/get-active-module.sh

# 5. TODO-Status
cat .current-todos.md

# 6. Migration Check (WICHTIG!)
cat docs/MIGRATION_REGISTRY.md | grep "NÄCHSTE"
# Aktuell: MIGRATION_NEXT

# 7. Neue Migration erstellen (falls nötig):
./scripts/create-migration.sh "beschreibung"
# Oder mit expliziter Nummer:
./scripts/create-migration.sh NEXT_NUMBER "beschreibung"
```

## 🔄 PR-READINESS CHECK
**Automatische Analyse:**
- [ ] Lines changed: LINES_CHANGED → PR_RECOMMENDATION_LINES
- [ ] New migrations: NEW_MIGRATIONS → PR_RECOMMENDATION_MIGRATIONS
- [ ] New components: NEW_COMPONENTS → PR_RECOMMENDATION_COMPONENTS
- [ ] Test coverage: [MANUAL: %] → ✅ >80% = PR ready
- [ ] Feature demo-able: [MANUAL: Ja/Nein] → ✅ Yes = PR ready

**PR-Empfehlung:** [MANUAL: ✅ PR ERSTELLEN / ⚠️ NOCH WARTEN / ❌ ZU FRÜH]

## 📊 COMMIT-READINESS CHECK
**Code bereit für Commit?**
- [ ] Tests laufen grün? [MANUAL: ✅/❌]
- [ ] Keine TODO-Kommentare im Code? [MANUAL: ✅/❌]
- [ ] Code Review durchgeführt? [MANUAL: ✅/❌]
- [ ] Feature funktioniert wie erwartet? [MANUAL: ✅/❌]

**Commit-Empfehlung:** [MANUAL: ✅ COMMIT READY / ⚠️ FAST FERTIG / ❌ NOCH NICHT]

## ✅ VALIDIERUNG:
- [ ] **TodoRead ausgeführt?** (Anzahl: ___) ✅
- [ ] **Alle TODOs in Übergabe?** (Anzahl: ___ offen, ___ erledigt, ___ total) ✅
- [ ] **Zahlen stimmen überein?** ✅
- [ ] **Git-Status korrekt?** ✅
- [ ] **Service-Status geprüft?** ✅ 
- [ ] **V5 Fokus dokumentiert?** ✅ (Auto-Sync durchgeführt)
- [ ] **NEXT_STEP.md aktuell?** ✅
- [ ] **Nächste Schritte klar?** ✅
- [ ] **Strategische Pläne verlinkt?** ✅
- [ ] **Guide-Updates dokumentiert?** ✅

---
**Session-Ende:** TIME_PLACEHOLDER  
**Hauptaufgabe:** [MANUAL: Was war die Hauptaufgabe?]  
**Status:** [MANUAL: Fortschritt/Ergebnis]
EOF

# Replace placeholders with robust sed commands
sed -i '' "s/DATETIME_PLACEHOLDER/$DATETIME/g" "$HANDOVER_FILE" 2>/dev/null || warn "Could not replace DATETIME"
sed -i '' "s/DATE_TIME_PLACEHOLDER/$DATETIME/g" "$HANDOVER_FILE" 2>/dev/null || warn "Could not replace DATE_TIME"
sed -i '' "s/TIME_PLACEHOLDER/$TIME/g" "$HANDOVER_FILE" 2>/dev/null || warn "Could not replace TIME"
sed -i '' "s/JAVA_VERSION_PLACEHOLDER/$JAVA_VERSION/g" "$HANDOVER_FILE" 2>/dev/null || warn "Could not replace JAVA_VERSION"
sed -i '' "s/NODE_VERSION_PLACEHOLDER/$NODE_VERSION/g" "$HANDOVER_FILE" 2>/dev/null || warn "Could not replace NODE_VERSION"
sed -i '' "s/LINES_CHANGED/$LINES_CHANGED/g" "$HANDOVER_FILE" 2>/dev/null || warn "Could not replace LINES_CHANGED"
sed -i '' "s/NEW_MIGRATIONS/$NEW_MIGRATIONS/g" "$HANDOVER_FILE" 2>/dev/null || warn "Could not replace NEW_MIGRATIONS"
sed -i '' "s/NEW_COMPONENTS/$NEW_COMPONENTS/g" "$HANDOVER_FILE" 2>/dev/null || warn "Could not replace NEW_COMPONENTS"

# Migration info with BIG formatting
sed -i '' "s/MIGRATION_NEXT_BIG/>>> $NEXT_MIGRATION <<</g" "$HANDOVER_FILE" 2>/dev/null
sed -i '' "s/MIGRATION_NEXT/$NEXT_MIGRATION/g" "$HANDOVER_FILE" 2>/dev/null
sed -i '' "s/MIGRATION_LAST/$LAST_MIGRATION/g" "$HANDOVER_FILE" 2>/dev/null
sed -i '' "s/NEXT_NUMBER/${NEXT_MIGRATION#V}/g" "$HANDOVER_FILE" 2>/dev/null

# Service check placeholder
sed -i '' "s/SERVICE_CHECK_PLACEHOLDER/[Von Script prüfen]/g" "$HANDOVER_FILE" 2>/dev/null

# PR Recommendations
if [ "$LINES_CHANGED" -gt 200 ] 2>/dev/null; then
    sed -i '' "s/PR_RECOMMENDATION_LINES/❌ >200/g" "$HANDOVER_FILE" 2>/dev/null
else
    sed -i '' "s/PR_RECOMMENDATION_LINES/✅ <200/g" "$HANDOVER_FILE" 2>/dev/null
fi

if [ "$NEW_MIGRATIONS" -gt 0 ] 2>/dev/null; then
    sed -i '' "s/PR_RECOMMENDATION_MIGRATIONS/✅ PR recommended!/g" "$HANDOVER_FILE" 2>/dev/null
else
    sed -i '' "s/PR_RECOMMENDATION_MIGRATIONS/✅ OK/g" "$HANDOVER_FILE" 2>/dev/null
fi

if [ "$NEW_COMPONENTS" -gt 2 ] 2>/dev/null; then
    sed -i '' "s/PR_RECOMMENDATION_COMPONENTS/⚠️ Many new components/g" "$HANDOVER_FILE" 2>/dev/null
else
    sed -i '' "s/PR_RECOMMENDATION_COMPONENTS/✅ OK/g" "$HANDOVER_FILE" 2>/dev/null
fi

echo -e "${GREEN}✅ Übergabe-Template erstellt: $HANDOVER_FILE${NC}"
echo ""

# Show migration info prominently
echo -e "${YELLOW}🚨 MIGRATION INFO:${NC}"
echo -e "   Nächste freie: ${GREEN}$NEXT_MIGRATION${NC}"
echo -e "   Letzte applied: ${GREEN}$LAST_MIGRATION${NC}"
echo ""

# Show warnings if any
if [ "$NEXT_MIGRATION" = "V121" ] || [ "$LAST_MIGRATION" = "V120" ]; then
    echo -e "${YELLOW}⚠️  Using fallback values - please verify!${NC}"
fi

echo -e "${YELLOW}⚠️  WICHTIG: Bitte fülle die [MANUAL: ...] Bereiche aus!${NC}"
echo -e "${YELLOW}📋 TODO: Führe TodoRead aus und füge die TODOs ein${NC}"
echo ""
echo -e "Öffne mit: code $HANDOVER_FILE"

# Exit successfully
exit 0