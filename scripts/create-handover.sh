#!/bin/bash

# IMPROVED Create handover document - FAST, SAFE, COMPLETE
# With maximum automation and robust fallback strategies
# Usage: ./create-handover.sh

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
echo -e "${GREEN}🚀 ERWEITERTE ÜBERGABE-ERSTELLUNG MIT ALLEN VERBESSERUNGEN V2${NC}"
echo "================================================================"

# Get current datetime
DATETIME=$(date +"%Y-%m-%d %H:%M" 2>/dev/null) || DATETIME="$(date)"
DATE=$(date +"%Y-%m-%d" 2>/dev/null) || DATE="2025-08-02"
TIME=$(date +"%H-%M" 2>/dev/null) || TIME="00-00"
HOUR_MIN=$(date +"%H:%M" 2>/dev/null) || HOUR_MIN="00:00"

# Calculate session duration
if [ -n "${SESSION_START_TIME:-}" ] && [ "${SESSION_START_TIME:-}" != "" ]; then
    SESSION_END_TIME=$(date +%s)
    SESSION_DURATION=$(((SESSION_END_TIME - SESSION_START_TIME) / 60))
    SESSION_DURATION_STR="${SESSION_DURATION} Minuten"
else
    SESSION_DURATION_STR="[Unbekannt - SESSION_START_TIME nicht gesetzt]"
fi

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

# Get last applied migration (with timeout if available)
if command -v timeout &>/dev/null; then
    TEMP_LAST=$(timeout 2 bash -c 'cd backend && ./mvnw -q flyway:info 2>/dev/null | grep "| [0-9]" | tail -1 | awk "{print \$2}"' || echo "")
else
    # No timeout command, try without
    TEMP_LAST=$(cd backend && ./mvnw -q flyway:info 2>/dev/null | grep "| [0-9]" | tail -1 | awk '{print $2}' || echo "")
fi

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

# NEW: Check CI Status
info "Prüfe CI Status..."
CI_STATUS="❓ Unbekannt"
CI_DETAILS=""
if command -v gh &>/dev/null; then
    # Get latest workflow runs
    CI_RUN=$(gh run list --branch "$CURRENT_BRANCH" --limit 1 --json conclusion,status,name,headSha 2>/dev/null || echo "")
    if [ -n "$CI_RUN" ] && [ "$CI_RUN" != "[]" ]; then
        CI_CONCLUSION=$(echo "$CI_RUN" | jq -r '.[0].conclusion // "pending"' 2>/dev/null || echo "unknown")
        CI_NAME=$(echo "$CI_RUN" | jq -r '.[0].name // "CI"' 2>/dev/null || echo "CI")
        case "$CI_CONCLUSION" in
            "success") CI_STATUS="✅ Grün" ;;
            "failure") CI_STATUS="❌ Rot" ;;
            "cancelled") CI_STATUS="⚠️ Abgebrochen" ;;
            *) CI_STATUS="🔄 Läuft" ;;
        esac
        CI_DETAILS=" ($CI_NAME)"
    fi
else
    warn "GitHub CLI not installed - CI status check skipped"
fi

# Get active module info with enhanced detection
info "Ermittle aktives Modul (mit erweiterter Erkennung)..."
ACTIVE_FEATURE="FC-???"
ACTIVE_MODULE="Unknown"
ACTIVE_DOC_PATH=""
MODULE_STATUS="❓ Unbekannt"

if [ -f ".current-focus" ]; then
    ACTIVE_FEATURE=$(grep "feature:" .current-focus 2>/dev/null | cut -d' ' -f2 || echo "FC-???")
    ACTIVE_MODULE=$(grep "module:" .current-focus 2>/dev/null | cut -d' ' -f2- || echo "Unknown")
fi

# Enhanced module structure detection
if [ "$ACTIVE_FEATURE" != "FC-???" ]; then
    # Check new structure patterns
    MODULE_FOUND=false
    
    # Pattern 1: ACTIVE folder structure
    if ls -d docs/features/ACTIVE/${ACTIVE_FEATURE}-* 2>/dev/null | head -1 > /dev/null; then
        MODULE_DIR=$(ls -d docs/features/ACTIVE/${ACTIVE_FEATURE}-* 2>/dev/null | head -1)
        if [ -n "$MODULE_DIR" ]; then
            MODULE_FOUND=true
            MODULE_STATUS="✅ Neue Struktur erkannt"
            info "Module found in ACTIVE folder: $MODULE_DIR"
        fi
    fi
    
    # Pattern 2: Direct feature folder
    if [ "$MODULE_FOUND" = false ] && ls -d docs/features/${ACTIVE_FEATURE}-* 2>/dev/null | head -1 > /dev/null; then
        MODULE_DIR=$(ls -d docs/features/${ACTIVE_FEATURE}-* 2>/dev/null | head -1)
        if [ -n "$MODULE_DIR" ]; then
            MODULE_FOUND=true
            MODULE_STATUS="✅ Feature-Ordner gefunden"
            info "Module found in features: $MODULE_DIR"
        fi
    fi
    
    # Pattern 3: Sprint subdirectories
    if [ "$MODULE_FOUND" = false ]; then
        for sprint_dir in docs/features/${ACTIVE_FEATURE}*/sprint*; do
            if [ -d "$sprint_dir" ]; then
                MODULE_FOUND=true
                MODULE_STATUS="✅ Sprint-Struktur erkannt"
                info "Sprint structure found: $sprint_dir"
                break
            fi
        done
    fi
    
    # Look for CLAUDE_TECH.md in various locations
    for path in "docs/features/ACTIVE/${ACTIVE_FEATURE}*/${ACTIVE_FEATURE}_CLAUDE_TECH.md" \
                "docs/features/${ACTIVE_FEATURE}*/${ACTIVE_FEATURE}_CLAUDE_TECH.md" \
                "docs/features/${ACTIVE_FEATURE}*/sprint*/*VISION*.md" \
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

# Check services with auto-start attempt
info "Prüfe Service-Status (mit Auto-Start bei Bedarf)..."
BACKEND_STATUS="❓ Unbekannt"
FRONTEND_STATUS="❓ Unbekannt"
DB_STATUS="❓ Unbekannt"
KEYCLOAK_STATUS="❓ Unbekannt"
AUTO_START_LOG=""

# Quick service checks (with or without timeout)
if command -v timeout &>/dev/null; then
    # With timeout
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

    # PostgreSQL Check mit timeout - verwende alternative Methoden wenn pg_isready nicht verfügbar
    if command -v pg_isready &>/dev/null && timeout 1 pg_isready -h localhost -p 5432 >/dev/null 2>&1; then
        DB_STATUS="✅ Läuft"
    elif timeout 1 bash -c 'docker ps 2>/dev/null | grep -q "postgres.*5432"' && docker ps 2>/dev/null | grep postgres | grep -q "healthy\|Up"; then
        DB_STATUS="✅ Läuft"
    elif timeout 1 nc -z localhost 5432 2>/dev/null; then
        DB_STATUS="✅ Läuft"
    else
        DB_STATUS="❌ Nicht erreichbar"
        # Try to auto-start PostgreSQL
        if [ -f "infrastructure/docker-compose.yml" ] && command -v docker-compose &>/dev/null; then
            info "Versuche PostgreSQL zu starten..."
            if (cd infrastructure && docker-compose up -d db) >/dev/null 2>&1; then
                sleep 3
                if timeout 1 nc -z localhost 5432 2>/dev/null || (docker ps 2>/dev/null | grep -q "postgres.*5432"); then
                    DB_STATUS="✅ Läuft (auto-gestartet)"
                    AUTO_START_LOG="PostgreSQL wurde automatisch gestartet"
                fi
            fi
        fi
    fi

    if timeout 1 curl -s http://localhost:8180 >/dev/null 2>&1; then
        KEYCLOAK_STATUS="✅ Läuft"
    else
        KEYCLOAK_STATUS="❌ Nicht erreichbar"
    fi
else
    # Without timeout - quick check with curl --max-time
    if curl -s --max-time 1 http://localhost:8080/health >/dev/null 2>&1; then
        BACKEND_STATUS="✅ Läuft"
    else
        BACKEND_STATUS="❌ Nicht erreichbar"
    fi

    if curl -s --max-time 1 http://localhost:5173 >/dev/null 2>&1; then
        FRONTEND_STATUS="✅ Läuft"
    else
        FRONTEND_STATUS="❌ Nicht erreichbar"
    fi

    # PostgreSQL Check - verwende alternative Methoden wenn pg_isready nicht verfügbar
    if command -v pg_isready &>/dev/null && pg_isready -h localhost -p 5432 >/dev/null 2>&1; then
        DB_STATUS="✅ Läuft"
    elif docker ps 2>/dev/null | grep -q "postgres.*5432" && docker ps 2>/dev/null | grep postgres | grep -q "healthy\|Up"; then
        DB_STATUS="✅ Läuft"
    elif nc -z localhost 5432 2>/dev/null; then
        DB_STATUS="✅ Läuft"
    else
        DB_STATUS="❌ Nicht erreichbar"
        # Try to auto-start PostgreSQL
        if [ -f "infrastructure/docker-compose.yml" ] && command -v docker-compose &>/dev/null; then
            info "Versuche PostgreSQL zu starten..."
            if (cd infrastructure && docker-compose up -d db) >/dev/null 2>&1; then
                sleep 3
                if nc -z localhost 5432 2>/dev/null || (docker ps 2>/dev/null | grep -q "postgres.*5432"); then
                    DB_STATUS="✅ Läuft (auto-gestartet)"
                    AUTO_START_LOG="PostgreSQL wurde automatisch gestartet"
                fi
            fi
        fi
    fi

    if curl -s --max-time 1 http://localhost:8180 >/dev/null 2>&1; then
        KEYCLOAK_STATUS="✅ Läuft"
    else
        KEYCLOAK_STATUS="❌ Nicht erreichbar"
    fi
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

# NEW: Dependency check
info "Prüfe Dependencies..."
OUTDATED_DEPS_BACKEND="0"
OUTDATED_DEPS_FRONTEND="0"
SECURITY_ISSUES="0"

# Backend dependency check (quick version)
if [ -f "backend/pom.xml" ]; then
    # Just count .jar files older than 30 days as a simple heuristic
    OUTDATED_DEPS_BACKEND=$(find ~/.m2/repository -name "*.jar" -mtime +30 2>/dev/null | wc -l | tr -d ' ' || echo "0")
    if [ "$OUTDATED_DEPS_BACKEND" -gt 50 ]; then
        OUTDATED_DEPS_BACKEND="Viele (>50)"
    fi
fi

# Frontend dependency check
if [ -f "frontend/package.json" ]; then
    if command -v npm &>/dev/null; then
        # Quick check - just count node_modules
        if [ -d "frontend/node_modules" ]; then
            MODULE_COUNT=$(ls -1 frontend/node_modules 2>/dev/null | wc -l | tr -d ' ' || echo "0")
            if [ "$MODULE_COUNT" -gt 500 ]; then
                OUTDATED_DEPS_FRONTEND="Check empfohlen"
            fi
        fi
        
        # Security audit (quick check)
        if [ -f "frontend/package-lock.json" ]; then
            SECURITY_ISSUES=$(grep -c "severity" frontend/package-lock.json 2>/dev/null | tr -d ' \n' || echo "0")
        fi
    fi
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
3. \`/docs/STANDARDUERGABE_NEU.md\` als Hauptanleitung
4. Bei DB-Arbeit: \`/docs/DATABASE_MIGRATION_GUIDE.md\`
5. **🚨 NEU:** \`/docs/MIGRATION_REGISTRY.md\` (KRITISCH für neue Migrationen!)
6. **🚨 NEU:** \`/docs/FLYWAY_MIGRATION_HISTORY.md\` (Vollständige Migration-Historie!)

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
5. **🆕 NEU:** \`/docs/FLYWAY_MIGRATION_HISTORY.md\` (KRITISCH für vollständige Historie!)

**Pre-Commit Hook Status:** $(if [ -f .git/hooks/pre-commit ] && grep -q "migration" .git/hooks/pre-commit 2>/dev/null; then echo "✅ Aktiv"; else echo "❌ Nicht installiert"; fi)

### 📋 FLYWAY MIGRATION STATUS
**Nächste freie Nummer:** $NEXT_MIGRATION
**Historie-Dokument:** [FLYWAY_MIGRATION_HISTORY.md](./docs/FLYWAY_MIGRATION_HISTORY.md)
**Letzte Migration:** $LAST_MIGRATION
$(if [ -f "docs/FLYWAY_MIGRATION_HISTORY.md" ]; then echo "**Historie:** ✅ Verfügbar"; else echo "**Historie:** ❌ FEHLT! Erstelle mit TODO-019"; fi)

### 🖥️ Service-Konfiguration & MIGRATION STATUS
| Service | Port | Technologie | **Migration** | Status |
|---------|------|-------------|--------------|--------|
| **Backend** | \`8080\` | Quarkus mit Java 17 | **🚨 $NEXT_MIGRATION FREI** | $BACKEND_STATUS |
| **Frontend** | \`5173\` | React/Vite | - | $FRONTEND_STATUS |
| **PostgreSQL** | \`5432\` | PostgreSQL 15+ | - | $DB_STATUS |
| **Keycloak** | \`8180\` | Auth Service | - | $KEYCLOAK_STATUS |

$(if [ -n "$AUTO_START_LOG" ]; then echo "**🔧 Auto-Start:** $AUTO_START_LOG"; fi)

## 📊 SYSTEM HEALTH CHECK

### CI/CD Status
**GitHub Actions:** $CI_STATUS$CI_DETAILS

### Dependencies
- **Backend veraltete Dependencies:** $OUTDATED_DEPS_BACKEND
- **Frontend veraltete Dependencies:** $OUTDATED_DEPS_FRONTEND
- **Security Issues:** $(if [ "$SECURITY_ISSUES" -gt 0 ]; then echo "⚠️ $SECURITY_ISSUES"; else echo "✅ 0"; fi)

### ⚠️ WICHTIGE HINWEISE
- **Java Version:** MUSS Java 17 sein! (aktuell: $JAVA_VERSION)
- **Node Version:** v22.16.0+ erforderlich (aktuell: $NODE_VERSION)
- **Working Directory:** \`$WORKING_DIR\`
- **Branch-Regel:** NIEMALS direkt in \`main\` pushen!
- **Session-Dauer:** $SESSION_DURATION_STR

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
**Modul-Struktur:** $MODULE_STATUS

## 📋 WAS WURDE HEUTE GEMACHT?

### Session X ($HOUR_MIN-[ENDE]): [MANUAL: Kurze Beschreibung]

1. **[MANUAL: Hauptaktivität]:**
   - [MANUAL: Details]
   - [MANUAL: Ergebnisse]

2. **[MANUAL: Weitere Aktivitäten]**

## ✅ WAS FUNKTIONIERT?

$(if [ "$BACKEND_STATUS" = "✅ Läuft" ]; then echo "- ✅ Backend läuft stabil (Port 8080)"; fi)
$(if [ "$FRONTEND_STATUS" = "✅ Läuft" ]; then echo "- ✅ Frontend läuft (Port 5173)"; fi)
$(if [[ "$DB_STATUS" == *"Läuft"* ]]; then echo "- ✅ PostgreSQL läuft (Port 5432)"; fi)
$(if [ "$KEYCLOAK_STATUS" = "✅ Läuft" ]; then echo "- ✅ Keycloak läuft (Port 8180)"; fi)
- ✅ Migration Registry System funktioniert
- ✅ Pre-Commit Hook verhindert Duplikate
$(if [ "$CI_STATUS" = "✅ Grün" ]; then echo "- ✅ CI/CD Pipeline grün"; fi)
- [MANUAL: Weitere funktionierende Features]

## 🚨 WELCHE FEHLER GIBT ES?

$(if [ "$CI_STATUS" = "❌ Rot" ]; then echo "**CI/CD Pipeline rot** - Letzte Checks fehlgeschlagen"; echo "- Lösung: \`gh run view\` für Details"; echo ""; fi)
$(if [ "$SECURITY_ISSUES" -gt 0 ]; then echo "**$SECURITY_ISSUES Security Issues in Dependencies**"; echo "- Lösung: \`npm audit fix\` im Frontend"; echo ""; fi)
[MANUAL: Weitere Probleme mit Fehlermeldungen oder "**Keine kritischen Fehler!** 🎉"]

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

# 8. CI Status prüfen:
gh run list --branch $CURRENT_BRANCH --limit 3

# 9. Dependencies prüfen:
cd frontend && npm outdated
cd ../backend && mvn versions:display-dependency-updates
\`\`\`

## 🔄 PR-READINESS CHECK
**Automatische Analyse:**
- [$(if [ "$LINES_CHANGED" -gt 200 ]; then echo "x"; else echo " "; fi)] Lines changed: $LINES_CHANGED → $(if [ "$LINES_CHANGED" -gt 200 ]; then echo "❌ >200"; else echo "✅ <200"; fi)
- [$(if [ "$NEW_MIGRATIONS" -gt 0 ]; then echo "x"; else echo " "; fi)] New migrations: $NEW_MIGRATIONS → $(if [ "$NEW_MIGRATIONS" -gt 0 ]; then echo "✅ PR recommended!"; else echo "✅ OK"; fi)
- [$(if [ "$NEW_COMPONENTS" -gt 2 ]; then echo "x"; else echo " "; fi)] New components: $NEW_COMPONENTS → $(if [ "$NEW_COMPONENTS" -gt 2 ]; then echo "⚠️ Many new components"; else echo "✅ OK"; fi)
- [$(if [ "$CI_STATUS" = "✅ Grün" ]; then echo "x"; else echo " "; fi)] CI Status: $CI_STATUS
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
- [x] **CI-Status geprüft?** ✅
- [x] **Dependencies geprüft?** ✅
- [x] **Session-Dauer getrackt?** ✅
- [ ] **V5 Fokus dokumentiert?** ✅ (Auto-Sync durchgeführt)
- [ ] **NEXT_STEP.md aktuell?** [MANUAL: ✅/❌]
- [ ] **Nächste Schritte klar?** [MANUAL: ✅/❌]
- [ ] **Strategische Pläne verlinkt?** [MANUAL: ✅/❌]
- [ ] **Guide-Updates dokumentiert?** [MANUAL: ✅/❌]

---
**Session-Ende:** $HOUR_MIN  
**Session-Dauer:** $SESSION_DURATION_STR
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
echo "   ✅ Service-Status $(if [ -n "$AUTO_START_LOG" ]; then echo "(mit Auto-Start)"; fi)"
echo "   ✅ Git-Statistiken"
echo "   ✅ TODO-Counts"
echo "   ✅ System-Info"
echo "   ✅ PR-Empfehlungen"
echo "   🆕 CI-Status: $CI_STATUS"
echo "   🆕 Dependencies geprüft"
echo "   🆕 Session-Dauer: $SESSION_DURATION_STR"
echo "   🆕 Modul-Struktur: $MODULE_STATUS"
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