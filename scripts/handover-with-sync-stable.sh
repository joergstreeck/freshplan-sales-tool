#!/bin/bash

# STABILIZED VERSION - Enhanced handover creation with Master Plan sync
# Author: Claude
# Date: 2025-08-08
# Version: 3.0 - Prominent migration number display

set -euo pipefail
trap 'handle_error $? $LINENO' ERR

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
BOLD='\033[1m'
NC='\033[0m'

# Configuration with robust path detection
find_project_root() {
    local dir="$PWD"
    while [ "$dir" != "/" ]; do
        if [ -d "$dir/.git" ] && [ -f "$dir/CLAUDE.md" ]; then
            echo "$dir"
            return 0
        fi
        dir=$(dirname "$dir")
    done
    # Fallback to script location
    local script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" 2>/dev/null && pwd)"
    if [ -n "$script_dir" ]; then
        echo "$(cd "$script_dir/.." 2>/dev/null && pwd)"
        return 0
    fi
    echo ""
    return 1
}

# Determine project root robustly
PROJECT_ROOT=$(find_project_root)
if [ -z "$PROJECT_ROOT" ]; then
    echo "ERROR: Could not find project root. Please run from within freshplan-sales-tool."
    exit 1
fi

SCRIPT_DIR="$PROJECT_ROOT/scripts"
DATE=$(date +"%Y-%m-%d")
TIME=$(date +"%H-%M")
HANDOVER_DIR="$PROJECT_ROOT/docs/claude-work/daily-work/$DATE"

# Error handler with line number
handle_error() {
    local exit_code=$1
    local line_no=$2
    log_error "Fehler in Zeile $line_no (Exit Code: $exit_code)"
    log_warning "Verwende manuellen Fallback..."
    manual_fallback
    exit 0  # Exit gracefully after fallback
}

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_critical() {
    echo -e "${RED}${BOLD}🚨 $1${NC}"
}

# Get next migration number with fallback
get_next_migration() {
    local migration_dir="$PROJECT_ROOT/backend/src/main/resources/db/migration"
    
    if [[ -d "$migration_dir" ]]; then
        local last_migration=$(ls "$migration_dir"/*.sql 2>/dev/null | sort -V | tail -1 | grep -oE 'V[0-9]+' | sed 's/V//' || echo "211")
        if [[ "$last_migration" =~ ^[0-9]+$ ]]; then
            echo "V$((last_migration + 1))"
        else
            echo "V212"  # Default fallback
        fi
    else
        echo "V212"  # Default if directory doesn't exist
    fi
}

# Display migration number prominently
show_migration_warning() {
    local next_migration=$(get_next_migration)
    
    echo ""
    echo -e "${RED}╔════════════════════════════════════════════════╗${NC}"
    echo -e "${RED}║${BOLD}     🚨 MIGRATION-NUMMER - KRITISCH! 🚨        ${RED}║${NC}"
    echo -e "${RED}╠════════════════════════════════════════════════╣${NC}"
    echo -e "${RED}║${NC}                                                ${RED}║${NC}"
    echo -e "${RED}║${NC}   ${BOLD}${MAGENTA}NÄCHSTE MIGRATION: ${GREEN}${next_migration}${NC}                     ${RED}║${NC}"
    echo -e "${RED}║${NC}                                                ${RED}║${NC}"
    echo -e "${RED}║${NC}   ${YELLOW}⚠️  NIEMALS alte Nummern verwenden!${NC}         ${RED}║${NC}"
    echo -e "${RED}║${NC}   ${YELLOW}⚠️  Diese Nummer MUSS in Übergabe!${NC}          ${RED}║${NC}"
    echo -e "${RED}║${NC}                                                ${RED}║${NC}"
    echo -e "${RED}╚════════════════════════════════════════════════╝${NC}"
    echo ""
    
    # Save to file for reference
    echo "$next_migration" > "$PROJECT_ROOT/.current-migration-number"
    
    return 0
}

# Check if script exists and is executable
check_script() {
    local script_path="$1"
    if [[ -x "$script_path" ]]; then
        return 0
    else
        return 1
    fi
}

# Manual fallback for handover creation
manual_fallback() {
    log_info "Erstelle Übergabe manuell..."
    
    # Get migration number first
    local next_migration=$(get_next_migration)
    
    # Create directory if it doesn't exist
    mkdir -p "$HANDOVER_DIR"
    
    local handover_file="$HANDOVER_DIR/${DATE}_HANDOVER_${TIME}.md"
    
    cat > "$handover_file" << EOF
# 📋 ÜBERGABE-DOKUMENT

**WICHTIG: Lies ZUERST diese Dokumente in dieser Reihenfolge:**
1) \`/docs/CLAUDE.md\`
2) Diese Übergabe
3) \`/docs/STANDARDUBERGABE_NEU.md\` als Hauptanleitung
4) Bei DB-Arbeit: \`/docs/DATABASE_MIGRATION_GUIDE.md\`

**3-STUFEN-SYSTEM:**
- \`STANDARDUBERGABE_NEU.md\` = Hauptdokument mit 5 Schritten
- \`STANDARDUBERGABE_KOMPAKT.md\` = Ultra-kurz für Quick-Reference
- \`STANDARDUBERGABE.md\` = Nur bei Problemen

---

## 🚨 MIGRATION-WARNUNG

**NÄCHSTE FREIE MIGRATION: ${next_migration}**
**NIEMALS** alte Nummern wiederverwenden!
**IMMER** diese Nummer für neue Migrationen verwenden!

---

## 📋 TODO-STATUS

### Erledigte TODOs dieser Session: ✅
[MANUAL: Füge erledigte TODOs ein]

### Offene TODOs: 📌
[MANUAL: Führe TodoRead aus und füge hier ein]

**TODO-Statistik:** X erledigt, Y offen, Z total

---

## 🎯 WAS WURDE GEMACHT?
[MANUAL: git diff --stat zeigt die Änderungen]

---

## ✅ WAS FUNKTIONIERT?
[MANUAL: Liste verifizierte Features]

---

## ❌ BEKANNTE PROBLEME
[MANUAL: Exakte Fehlermeldungen]

---

## 🗄️ DOKUMENTATIONS-UPDATES
[MANUAL: Guide-Updates eintragen]

---

## 🎯 STRATEGISCHE PLÄNE
[MANUAL: Aktive Planungsdokumente verlinken]

---

## 🚨 UNTERBRECHUNGEN
[MANUAL: Wo genau unterbrochen?]

---

## 📂 DATEIEN GEÄNDERT
[MANUAL: git status]

---

## 🔧 GIT STATUS
[MANUAL: git status output]

---

## 💡 NÄCHSTE SCHRITTE
[MANUAL: Aus TODOs und NEXT_STEP.md]

---

## 🔍 SERVICE-STATUS

| Service | Status | Port | Details |
|---------|--------|------|---------|
| Backend | [CHECK] | 8080 | [MANUAL] |
| Frontend | [CHECK] | 5173 | [MANUAL] |
| PostgreSQL | [CHECK] | 5432 | [MANUAL] |

---

## ✅ VALIDIERUNG

- [ ] TodoRead ausgeführt? (Anzahl: ___)
- [ ] Alle TODOs in Übergabe? (Anzahl: ___)
- [ ] Zahlen stimmen überein?
- [ ] Git-Status korrekt?
- [ ] Service-Status geprüft?
- [ ] V5 Fokus dokumentiert?
- [ ] NEXT_STEP.md aktualisiert?
- [ ] Nächste Schritte klar?
- [ ] Strategische Pläne verlinkt?
- [ ] Guide-Updates dokumentiert?

---

**Session-Ende:** $(date +"%Y-%m-%d %H:%M") Uhr
**Nächste Migration:** ${next_migration} ⚠️
EOF
    
    log_success "Manuelle Übergabe erstellt: $handover_file"
    echo ""
    echo "📋 NÄCHSTE SCHRITTE:"
    echo "1. Öffne: code $handover_file"
    echo "2. Fülle alle [MANUAL: ...] Bereiche aus"
    echo "3. Führe TodoRead aus und füge die TODOs ein"
    echo "4. Aktualisiere NEXT_STEP.md"
}

# Sync Master Plan with error handling
sync_master_plan() {
    log_info "Synchronisiere Master Plan V5..."
    
    if check_script "$PROJECT_ROOT/scripts/sync-master-plan.sh"; then
        if "$PROJECT_ROOT/scripts/sync-master-plan.sh" 2>/dev/null; then
            log_success "Master Plan synchronisiert"
            return 0
        else
            log_warning "sync-master-plan.sh fehlgeschlagen, fahre fort..."
            return 1
        fi
    else
        log_warning "sync-master-plan.sh nicht verfügbar"
        return 1
    fi
}

# Create handover with robust handling
create_handover() {
    log_info "Erstelle Übergabe mit Migration-Warnung..."
    
    if check_script "$PROJECT_ROOT/scripts/create-handover.sh"; then
        if "$PROJECT_ROOT/scripts/create-handover.sh" 2>/dev/null; then
            log_success "Übergabe erstellt"
            return 0
        else
            log_warning "create-handover.sh fehlgeschlagen"
            return 1
        fi
    else
        log_warning "create-handover.sh nicht verfügbar"
        return 1
    fi
}

# Main execution
main() {
    echo ""
    echo "🔄 STABILIZED HANDOVER MIT SYNC v3.0"
    echo "====================================="
    echo ""
    
    # Change to project root
    cd "$PROJECT_ROOT" || exit 1
    
    # CRITICAL: Show migration number FIRST
    show_migration_warning
    
    # Step 1: Try to sync Master Plan (non-critical)
    sync_master_plan || true
    
    echo ""
    
    # Step 2: Try to create handover
    if ! create_handover; then
        log_warning "Automatische Erstellung fehlgeschlagen"
        manual_fallback
    else
        # Success path
        echo ""
        log_success "✅ V5 Master Plan wurde synchronisiert (falls verfügbar)"
        log_success "✅ Übergabe mit Migration-Warnung erstellt"
        
        # Find latest handover
        if [[ -d "$HANDOVER_DIR" ]]; then
            LATEST_HANDOVER=$(ls -t "$HANDOVER_DIR"/*_HANDOVER_*.md 2>/dev/null | head -1)
            
            if [[ -n "$LATEST_HANDOVER" ]]; then
                # Insert migration number into handover
                local next_migration=$(get_next_migration)
                if grep -q "NÄCHSTE FREIE MIGRATION:" "$LATEST_HANDOVER"; then
                    sed -i "" "s/NÄCHSTE FREIE MIGRATION:.*/NÄCHSTE FREIE MIGRATION: ${next_migration}/" "$LATEST_HANDOVER"
                fi
                
                echo ""
                echo "📋 NÄCHSTE SCHRITTE:"
                echo "1. Öffne: code $LATEST_HANDOVER"
                echo "2. Fülle alle [MANUAL: ...] Bereiche aus"
                echo "3. Führe TodoRead aus und füge die TODOs ein"
                echo ""
            fi
        fi
    fi
    
    # Always show migration number again at the end
    echo ""
    log_critical "MIGRATION-NUMMER FÜR ÜBERGABE: $(get_next_migration)"
    echo ""
    
    log_success "Handover-Prozess abgeschlossen!"
}

# Run main function
main "$@"