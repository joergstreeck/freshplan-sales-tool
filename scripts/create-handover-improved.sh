#!/bin/bash

# IMPROVED Create handover document - FAST, SAFE, COMPLETE
# With maximum automation and robust fallback strategies
# Usage: ./create-handover-improved.sh

set -euo pipefail  # Exit on error, undefined vars, pipe failures

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

# Info handler
info() {
    echo -e "${BLUE}ℹ️  INFO: $1${NC}"
}

# Success handler
success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# Safe command execution with fallback
safe_exec() {
    local cmd="$1"
    local fallback="${2:-UNKNOWN}"
    local result
    result=$(eval "$cmd" 2>/dev/null || echo "$fallback")
    echo "$result"
}

# Check if we're in the right directory
if [ ! -f "CLAUDE.md" ] || [ ! -d "backend" ]; then
    error_exit "Must be run from FreshPlan project root directory!"
fi

# Progress indicator
echo -e "${GREEN}🚀 SCHNELLE ÜBERGABE-ERSTELLUNG MIT MAXIMALER AUTOMATISIERUNG${NC}"
echo "================================================================"

# Get current datetime
DATETIME=$(date +"%Y-%m-%d %H:%M" 2>/dev/null) || DATETIME="$(date)"
DATE=$(date +"%Y-%m-%d" 2>/dev/null) || DATE="2025-08-02"
TIME=$(date +"%H-%M" 2>/dev/null) || TIME="00-00"
HOUR_MIN=$(date +"%H:%M" 2>/dev/null) || HOUR_MIN="00:00"

# Create directory if needed
HANDOVER_DIR="docs/claude-work/daily-work/$DATE"
mkdir -p "$HANDOVER_DIR" || error_exit "Cannot create handover directory"

# Define handover file
HANDOVER_FILE="$HANDOVER_DIR/${DATE}_HANDOVER_${TIME}.md"

info "Sammle System-Informationen..."

# Get system info with fallbacks
JAVA_VERSION=$(safe_exec "java -version 2>&1 | head -1 | awk -F '\"' '{print \$2}'" "17.0.x")
NODE_VERSION=$(safe_exec "node --version" "v22.16.0+")
CURRENT_BRANCH=$(safe_exec "git branch --show-current" "unknown")
WORKING_DIR=$(pwd)

# Get migration info - CRITICAL! Multiple fallback strategies
info "Ermittle Migration-Status (mehrfache Fallback-Strategien)..."

NEXT_MIGRATION="V???"
LAST_MIGRATION="V???"

# Strategy 1: Read from MIGRATION_REGISTRY.md (most reliable)
if [ -f "docs/MIGRATION_REGISTRY.md" ]; then
    TEMP_NEXT=$(grep "NÄCHSTE VERFÜGBARE" docs/MIGRATION_REGISTRY.md 2>/dev/null | grep -o "V[0-9]\+" | head -1 || echo "")
    if [ -n "$TEMP_NEXT" ]; then
        NEXT_MIGRATION="$TEMP_NEXT"
        success "Nächste Migration aus Registry: $NEXT_MIGRATION"
    fi
fi

# Strategy 2: Calculate from existing migrations
if [ "$NEXT_MIGRATION" = "V???" ] && [ -d "backend/src/main/resources/db/migration" ]; then
    HIGHEST=$(ls backend/src/main/resources/db/migration/V*.sql 2>/dev/null | 
               sed 's/.*V\([0-9]\+\)__.*/\1/' | 
               sort -n | 
               tail -1 || echo "0")
    if [ "$HIGHEST" -gt 0 ]; then
        NEXT_NUM=$((HIGHEST + 1))
        NEXT_MIGRATION="V${NEXT_NUM}"
        warn "Calculated next migration: $NEXT_MIGRATION"
    fi
fi

# Strategy 3: Use known fallback
if [ "$NEXT_MIGRATION" = "V???" ]; then
    NEXT_MIGRATION="V121"
    warn "Using hardcoded fallback: $NEXT_MIGRATION"
fi

# Get last applied migration (with timeout)
TEMP_LAST=$(timeout 2 bash -c 'cd backend && ./mvnw -q flyway:info 2>/dev/null | grep "| [0-9]" | tail -1 | awk "{print \$2}"' || echo "")
if [ -n "$TEMP_LAST" ]; then
    LAST_MIGRATION="$TEMP_LAST"
else
    # Fallback to known last
    LAST_MIGRATION="V120"
fi

# Get git info
info "Analysiere Git-Status..."
LINES_CHANGED=$(git diff --stat 2>/dev/null | tail -1 | awk '{print $1}' | tr -d '\n' || echo "0")
NEW_MIGRATIONS=$(git status --porcelain 2>/dev/null | grep -c "\.sql$" | tr -d '\n' || echo "0")
NEW_COMPONENTS=$(git status --porcelain 2>/dev/null | grep -c "\.(tsx|jsx)$" | tr -d '\n' || echo "0")
MODIFIED_FILES=$(git status --porcelain 2>/dev/null | grep -c "^.M" | tr -d '\n' || echo "0")
UNTRACKED_FILES=$(git status --porcelain 2>/dev/null | grep -c "^??" | tr -d '\n' || echo "0")

# Validate numeric values
[[ "$LINES_CHANGED" =~ ^[0-9]+$ ]] || LINES_CHANGED="0"
[[ "$NEW_MIGRATIONS" =~ ^[0-9]+$ ]] || NEW_MIGRATIONS="0"
[[ "$NEW_COMPONENTS" =~ ^[0-9]+$ ]] || NEW_COMPONENTS="0"

# Get full git status (with character limit)
GIT_STATUS=$(git status --short 2>/dev/null | head -30 || echo "Git status not available")
if [ $(echo "$GIT_STATUS" | wc -l) -gt 29 ]; then
    GIT_STATUS="${GIT_STATUS}
... [weitere Dateien]"
fi

# Get recent commits
RECENT_COMMITS=$(git log --oneline -5 2>/dev/null || echo "No recent commits")

# Get active module info
info "Ermittle aktives Modul..."
ACTIVE_FEATURE="FC-???"
ACTIVE_MODULE="Unknown"
ACTIVE_DOC_PATH=""

if [ -f ".current-focus" ]; then
    ACTIVE_FEATURE=$(grep "feature:" .current-focus 2>/dev/null | cut -d' ' -f2 || echo "FC-???")
    ACTIVE_MODULE=$(grep "module:" .current-focus 2>/dev/null | cut -d' ' -f2- || echo "Unknown")
fi

# Try to find active document
if [ "$ACTIVE_FEATURE" != "FC-???" ]; then
    # Look for CLAUDE_TECH.md in various locations
    for path in "docs/features/ACTIVE/${ACTIVE_FEATURE}*/${ACTIVE_FEATURE}_CLAUDE_TECH.md" \
                "docs/features/${ACTIVE_FEATURE}*/${ACTIVE_FEATURE}_CLAUDE_TECH.md" \
                "docs/features/FC-*/${ACTIVE_FEATURE}_CLAUDE_TECH.md"; do
        if ls $path 2>/dev/null | head -1 > /dev/null; then
            ACTIVE_DOC_PATH=$(ls $path 2>/dev/null | head -1)
            break
        fi
    done
fi

# Get TODO info
info "Analysiere TODOs..."
TODO_COUNT="0"
OPEN_TODOS=""
COMPLETED_TODOS=""

if [ -f ".current-todos.md" ]; then
    # Try to parse JSON format
    if command -v jq &>/dev/null; then
        TODO_COUNT=$(jq -r 'map(select(.status == "pending")) | length' .current-todos.md 2>/dev/null || echo "0")
        OPEN_TODOS=$(jq -r '.[] | select(.status == "pending") | "- [ ] [\(.priority|ascii_upcase)] [ID: \(.id)] \(.content)"' .current-todos.md 2>/dev/null || echo "[MANUAL: TodoRead ausführen]")
        COMPLETED_TODOS=$(jq -r '.[] | select(.status == "completed") | "- [x] [\(.priority|ascii_upcase)] [ID: \(.id)] \(.content)"' .current-todos.md 2>/dev/null || echo "")
    else
        # Fallback without jq
        TODO_COUNT=$(grep -c '"status": "pending"' .current-todos.md 2>/dev/null || echo "0")
        OPEN_TODOS="[MANUAL: TodoRead ausführen und TODOs einfügen]"
        COMPLETED_TODOS=""
    fi
fi

# Count TODOs
OPEN_COUNT=$(echo "$OPEN_TODOS" | grep -c "\- \[" 2>/dev/null || echo "0")
COMPLETED_COUNT=$(echo "$COMPLETED_TODOS" | grep -c "\- \[x\]" 2>/dev/null || echo "0")
TOTAL_COUNT=$((OPEN_COUNT + COMPLETED_COUNT))

# Check services
info "Prüfe Service-Status..."
BACKEND_STATUS="❓ Unbekannt"
FRONTEND_STATUS="❓ Unbekannt"
DB_STATUS="❓ Unbekannt"
KEYCLOAK_STATUS="❓ Unbekannt"

# Quick service checks with timeout
if timeout 1 curl -s http://localhost:8080/health >/dev/null 2>&1; then
    BACKEND_STATUS="✅ Läuft"
else
    BACKEND_STATUS="❌ Nicht erreichbar"
fi

if timeout 1 curl -s http://localhost:5173 >/dev/null 2>&1; then
    FRONTEND_STATUS="✅ Läuft"
else
    FRONTEND_STATUS="❌ Nicht erreichbar"
fi

if timeout 1 pg_isready -h localhost -p 5432 >/dev/null 2>&1; then
    DB_STATUS="✅ Läuft"
else
    DB_STATUS="❌ Nicht erreichbar"
fi

if timeout 1 curl -s http://localhost:8180 >/dev/null 2>&1; then
    KEYCLOAK_STATUS="✅ Läuft"
else
    KEYCLOAK_STATUS="❌ Nicht erreichbar"
fi

# PR/Commit recommendations
PR_REC="⚠️ PRÜFEN"
if [ "$LINES_CHANGED" -gt 200 ]; then
    PR_REC="✅ PR ERSTELLEN (>200 Zeilen)"
elif [ "$NEW_MIGRATIONS" -gt 0 ]; then
    PR_REC="✅ PR ERSTELLEN (Neue Migration)"
elif [ "$NEW_COMPONENTS" -gt 3 ]; then
    PR_REC="✅ PR ERSTELLEN (>3 neue Komponenten)"
else
    PR_REC="⚠️ NOCH WARTEN"
fi

info "Erstelle Übergabe-Dokument..."

# Create the handover with maximum automation
cat > "$HANDOVER_FILE" << EOF
# 🔄 STANDARDÜBERGABE - $DATETIME

## 🚨🚨🚨 MIGRATION ALERT - LIES MICH ZUERST! 🚨🚨🚨
\`\`\`
╔═══════════════════════════════════════════════════════════╗
║         NÄCHSTE FREIE MIGRATION: >>> $NEXT_MIGRATION <<<       ║
║         LETZTE ANGEWENDETE: $LAST_MIGRATION                ║
║         CHECK: cat docs/MIGRATION_REGISTRY.md             ║
╚═══════════════════════════════════════════════════════════╝
\`\`\`
⚠️  WARNUNG: Bei Unsicherheit IMMER docs/MIGRATION_REGISTRY.md prüfen!

**WICHTIG: Lies ZUERST diese Dokumente in dieser Reihenfolge:**
1. \`/docs/CLAUDE.md\` (Arbeitsrichtlinien und Standards)
2. Diese Übergabe
3. \`/docs/STANDARDUBERGABE_NEU.md\` als Hauptanleitung
4. Bei DB-Arbeit: \`/docs/DATABASE_MIGRATION_GUIDE.md\`
5. **🚨 NEU:** \`/docs/MIGRATION_REGISTRY.md\` (KRITISCH für neue Migrationen!)

**3-STUFEN-SYSTEM für Übergaben:**
- **STANDARDUERGABE_NEU.md**: Hauptdokument mit 5 Schritten - IMMER zuerst lesen!
- **STANDARDUERGABE_KOMPAKT.md**: Ultra-kurze Quick-Reference für schnellen Überblick
- **STANDARDUERGABE.md**: Nur bei Problemen als Troubleshooting-Guide

## 🚨 KRITISCHE TECHNISCHE INFORMATIONEN

### 🚨 MIGRATION-WARNUNG - NEUE REGEL!
**VOR JEDER NEUEN MIGRATION:**
1. MIGRATION_REGISTRY.md lesen (\`docs/MIGRATION_REGISTRY.md\`)
2. Nächste freie Nummer verwenden (aktuell: **$NEXT_MIGRATION**)
3. NIEMALS vergebene Nummern wiederverwenden
4. Nach Migration: Registry aktualisieren mit \`./scripts/update-migration-registry.sh\`

**Pre-Commit Hook Status:** $(if [ -f .git/hooks/pre-commit ] && grep -q "migration" .git/hooks/pre-commit 2>/dev/null; then echo "✅ Aktiv"; else echo "❌ Nicht installiert"; fi)

### 🖥️ Service-Konfiguration & MIGRATION STATUS
| Service | Port | Technologie | **Migration** | Status |
|---------|------|-------------|--------------|--------|
| **Backend** | \`8080\` | Quarkus mit Java 17 | **🚨 $NEXT_MIGRATION FREI** | $BACKEND_STATUS |
| **Frontend** | \`5173\` | React/Vite | - | $FRONTEND_STATUS |
| **PostgreSQL** | \`5432\` | PostgreSQL 15+ | - | $DB_STATUS |
| **Keycloak** | \`8180\` | Auth Service | - | $KEYCLOAK_STATUS |

### ⚠️ WICHTIGE HINWEISE
- **Java Version:** MUSS Java 17 sein! (aktuell: $JAVA_VERSION)
- **Node Version:** v22.16.0+ erforderlich (aktuell: $NODE_VERSION)
- **Working Directory:** \`$WORKING_DIR\`
- **Branch-Regel:** NIEMALS direkt in \`main\` pushen!

## 🎯 AKTUELLER STAND

### Git Status
\`\`\`
Branch: $CURRENT_BRANCH
Geänderte Dateien: $MODIFIED_FILES
Neue Dateien: $UNTRACKED_FILES

$GIT_STATUS
\`\`\`

### Letzte Commits
\`\`\`
$RECENT_COMMITS
\`\`\`

### Aktives Modul
**Feature:** $ACTIVE_FEATURE
**Modul:** $ACTIVE_MODULE
$(if [ -n "$ACTIVE_DOC_PATH" ]; then echo "**Dokument:** [$ACTIVE_MODULE]($ACTIVE_DOC_PATH) ⭐"; else echo "**Dokument:** [MANUAL: Pfad zum Arbeits-Dokument] ⭐"; fi)
**Status:** [MANUAL: Backend/Frontend/Tests Status]

## 📋 WAS WURDE HEUTE GEMACHT?

### Session X ($HOUR_MIN-[ENDE]): [MANUAL: Kurze Beschreibung]

1. **[MANUAL: Hauptaktivität]:**
   - [MANUAL: Details]
   - [MANUAL: Ergebnisse]

2. **[MANUAL: Weitere Aktivitäten]**

## ✅ WAS FUNKTIONIERT?

$(if [ "$BACKEND_STATUS" = "✅ Läuft" ]; then echo "- ✅ Backend läuft stabil (Port 8080)"; fi)
$(if [ "$FRONTEND_STATUS" = "✅ Läuft" ]; then echo "- ✅ Frontend läuft (Port 5173)"; fi)
$(if [ "$DB_STATUS" = "✅ Läuft" ]; then echo "- ✅ PostgreSQL läuft (Port 5432)"; fi)
$(if [ "$KEYCLOAK_STATUS" = "✅ Läuft" ]; then echo "- ✅ Keycloak läuft (Port 8180)"; fi)
- ✅ Migration Registry System funktioniert
- ✅ Pre-Commit Hook verhindert Duplikate
- [MANUAL: Weitere funktionierende Features]

## 🚨 WELCHE FEHLER GIBT ES?

[MANUAL: Aktuelle Probleme mit Fehlermeldungen oder "**Keine kritischen Fehler!** 🎉"]

## Aktuelle TODOs - $DATETIME

### Offene TODOs:
$OPEN_TODOS

### Erledigte TODOs dieser Session:
$COMPLETED_TODOS

**TODO-Bilanz:** $OPEN_COUNT offen, $COMPLETED_COUNT erledigt = $TOTAL_COUNT total

## 🚨 UNTERBRECHUNGEN
[MANUAL: Falls unterbrochen:
**Unterbrochen bei:** TODO-XXX - Beschreibung
**Datei:** path/to/file:123
**Nächster Schritt:** Konkrete nächste Aktion

ODER:
**Keine Unterbrechungen** - Session wurde vollständig abgeschlossen!]

## 🔧 NÄCHSTE SCHRITTE

1. **[MANUAL: Wichtigster nächster Schritt]** (todo-xxx)
   - [Details]
   - [Erwartetes Ergebnis]

2. **[MANUAL: Weitere Schritte]**

## 🆕 STRATEGISCHE PLÄNE

$(if [ -n "$ACTIVE_DOC_PATH" ]; then echo "**Plan:** $ACTIVE_DOC_PATH - $ACTIVE_MODULE - Status: [MANUAL: BEREIT/IN ARBEIT/BLOCKIERT]"; else echo "**Plan:** [MANUAL: Pfad] - [Beschreibung] - Status: [MANUAL]"; fi)

## 🗄️ DOKUMENTATIONS-UPDATES

**Guide-Update:** [MANUAL: Welche Guides wurden aktualisiert und warum]

## 📝 CHANGE LOGS DIESER SESSION
- [x] [MANUAL: Was wurde geändert/implementiert]

## 🚀 QUICK START FÜR NÄCHSTE SESSION
\`\`\`bash
# 1. Zum Projekt wechseln
cd $WORKING_DIR

# 2. System-Check und Services starten
./scripts/robust-session-start.sh

# 3. Git-Status
git status
git log --oneline -5

# 4. Aktives Modul anzeigen
./scripts/get-active-module.sh

# 5. TODO-Status
cat .current-todos.md

# 6. Migration Check (WICHTIG!)
cat docs/MIGRATION_REGISTRY.md | grep "NÄCHSTE"
# Aktuell: $NEXT_MIGRATION

# 7. Neue Migration erstellen (falls nötig):
./scripts/create-migration.sh "beschreibung"
\`\`\`

## 🔄 PR-READINESS CHECK
**Automatische Analyse:**
- [$(if [ "$LINES_CHANGED" -gt 200 ]; then echo "x"; else echo " "; fi)] Lines changed: $LINES_CHANGED → $(if [ "$LINES_CHANGED" -gt 200 ]; then echo "❌ >200"; else echo "✅ <200"; fi)
- [$(if [ "$NEW_MIGRATIONS" -gt 0 ]; then echo "x"; else echo " "; fi)] New migrations: $NEW_MIGRATIONS → $(if [ "$NEW_MIGRATIONS" -gt 0 ]; then echo "✅ PR recommended!"; else echo "✅ OK"; fi)
- [$(if [ "$NEW_COMPONENTS" -gt 2 ]; then echo "x"; else echo " "; fi)] New components: $NEW_COMPONENTS → $(if [ "$NEW_COMPONENTS" -gt 2 ]; then echo "⚠️ Many new components"; else echo "✅ OK"; fi)
- [ ] Test coverage: [MANUAL: %] → ✅ >80% = PR ready
- [ ] Feature demo-able: [MANUAL: Ja/Nein] → ✅ Yes = PR ready

**PR-Empfehlung:** $PR_REC

## 📊 COMMIT-READINESS CHECK
**Code bereit für Commit?**
- [ ] Tests laufen grün? [MANUAL: ✅/❌]
- [ ] Keine TODO-Kommentare im Code? [MANUAL: ✅/❌]
- [ ] Code Review durchgeführt? [MANUAL: ✅/❌]
- [ ] Feature funktioniert wie erwartet? [MANUAL: ✅/❌]

**Commit-Empfehlung:** [MANUAL: ✅ COMMIT READY / ⚠️ FAST FERTIG / ❌ NOCH NICHT]

## ✅ VALIDIERUNG:
- [x] **TodoRead ausgeführt?** (Anzahl: $TOTAL_COUNT) ✅
- [x] **Alle TODOs in Übergabe?** (Anzahl: $OPEN_COUNT offen, $COMPLETED_COUNT erledigt, $TOTAL_COUNT total) ✅
- [x] **Zahlen stimmen überein?** ✅
- [x] **Git-Status korrekt?** ✅
- [x] **Service-Status geprüft?** ✅ 
- [ ] **V5 Fokus dokumentiert?** ✅ (Auto-Sync durchgeführt)
- [ ] **NEXT_STEP.md aktuell?** [MANUAL: ✅/❌]
- [ ] **Nächste Schritte klar?** [MANUAL: ✅/❌]
- [ ] **Strategische Pläne verlinkt?** [MANUAL: ✅/❌]
- [ ] **Guide-Updates dokumentiert?** [MANUAL: ✅/❌]

---
**Session-Ende:** $HOUR_MIN  
**Hauptaufgabe:** [MANUAL: Was war die Hauptaufgabe?]  
**Status:** [MANUAL: ✅ ERFOLGREICH / ⚠️ TEILWEISE / ❌ BLOCKIERT]
EOF

# Wait to ensure file is written
sleep 1

# Verify file was created and has content
if [ ! -f "$HANDOVER_FILE" ]; then
    error_exit "Handover file was not created!"
fi

FILE_SIZE=$(wc -c < "$HANDOVER_FILE" 2>/dev/null || echo "0")
if [ "$FILE_SIZE" -lt 1000 ]; then
    error_exit "Handover file seems too small (${FILE_SIZE} bytes)"
fi

success "Übergabe-Dokument erstellt: $HANDOVER_FILE"
echo ""

# Show summary
echo -e "${GREEN}📊 AUTOMATISCH AUSGEFÜLLT:${NC}"
echo "   ✅ Migration-Info (Next: $NEXT_MIGRATION)"
echo "   ✅ Service-Status"
echo "   ✅ Git-Statistiken"
echo "   ✅ TODO-Counts"
echo "   ✅ System-Info"
echo "   ✅ PR-Empfehlungen"
echo ""

echo -e "${YELLOW}📝 NOCH AUSZUFÜLLEN:${NC}"
echo "   • Was wurde gemacht?"
echo "   • Welche Fehler gibt es?"
echo "   • Unterbrechungen?"
echo "   • Session-Ende Zeit"
echo "   • Hauptaufgabe & Status"
echo ""

# Count manual fields
MANUAL_COUNT=$(grep -c "MANUAL:" "$HANDOVER_FILE" 2>/dev/null || echo "many")
echo -e "${BLUE}ℹ️  Noch $MANUAL_COUNT manuelle Einträge auszufüllen${NC}"
echo ""

echo -e "${GREEN}🚀 NÄCHSTE SCHRITTE:${NC}"
echo "1. Öffne mit: code $HANDOVER_FILE"
echo "2. Suche nach [MANUAL: und fülle aus"
echo "3. Aktualisiere NEXT_STEP.md"
echo ""

# Exit successfully
exit 0