#!/bin/bash

# handover-with-sync.sh - Erweiterte Übergabe mit automatischer Master Plan Synchronisation
# Author: Claude
# Date: 2025-07-22

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Paths
PROJECT_ROOT="/Users/joergstreeck/freshplan-sales-tool"
HANDOVER_DIR="$PROJECT_ROOT/docs/claude-work/daily-work/$(date +%Y-%m-%d)"

# Helper functions
log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Main execution
main() {
    echo "🔄 ERWEITERTE ÜBERGABE MIT SYNC"
    echo "==============================="
    echo ""
    
    # Step 1: Sync Master Plan
    log_info "Schritt 1: Master Plan synchronisieren..."
    if [[ -x "$PROJECT_ROOT/scripts/sync-master-plan.sh" ]]; then
        "$PROJECT_ROOT/scripts/sync-master-plan.sh"
        log_success "Master Plan synchronisiert"
    else
        log_warning "sync-master-plan.sh nicht gefunden oder nicht ausführbar"
    fi
    
    echo ""
    
    # Step 2: Create handover directory
    log_info "Schritt 2: Übergabe-Ordner erstellen..."
    mkdir -p "$HANDOVER_DIR"
    log_success "Ordner erstellt: $HANDOVER_DIR"
    
    # Step 3: Generate handover template
    log_info "Schritt 3: Übergabe-Template erstellen..."
    HANDOVER_FILE="$HANDOVER_DIR/$(date +%Y-%m-%d)_HANDOVER_$(date +%H-%M).md"
    
    # Create handover content
    cat > "$HANDOVER_FILE" << 'EOF'
# 🔄 STANDARDÜBERGABE - [DATUM] [UHRZEIT]

**WICHTIG: Lies ZUERST diese Dokumente in dieser Reihenfolge:**
1. `/CLAUDE.md` (Arbeitsrichtlinien und Standards)
2. Diese Übergabe
3. `/docs/STANDARDUBERGABE_NEU.md` als Hauptanleitung

## 🚨 KRITISCHE TECHNISCHE INFORMATIONEN

### 🖥️ Service-Konfiguration
| Service | Port | Technologie | Status |
|---------|------|-------------|--------|
| **Backend** | `8080` | Quarkus mit Java 17 | [MANUAL: ✅/❌] |
| **Frontend** | `5173` | React/Vite | [MANUAL: ✅/❌] |
| **PostgreSQL** | `5432` | PostgreSQL 15+ | [MANUAL: ✅/❌] |
| **Keycloak** | `8180` | Auth Service | [MANUAL: ✅/❌] |

### ⚠️ WICHTIGE HINWEISE
- **Java Version:** MUSS Java 17 sein! (aktuell: [MANUAL CHECK])
- **Node Version:** v22.16.0+ erforderlich (aktuell: [MANUAL CHECK])
- **Working Directory:** `/Users/joergstreeck/freshplan-sales-tool`
- **Branch-Regel:** NIEMALS direkt in `main` pushen!

## 🎯 AKTUELLER STAND

### Git Status
[MANUAL: git status output einfügen]

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

## Aktuelle TODOs - [DATUM] [UHRZEIT]

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

# 6. [MANUAL: Spezifische Befehle für aktuelle Aufgabe]
```

## ✅ VALIDIERUNG:
- [ ] [MANUAL: TodoRead ausgeführt? (Anzahl: X)]
- [ ] [MANUAL: Alle TODOs in Übergabe? (Anzahl: X offen, Y erledigt)]
- [ ] [MANUAL: Zahlen stimmen überein? ✅/❌]
- [ ] [MANUAL: Git-Status korrekt? ✅/❌]
- [ ] [MANUAL: Service-Status geprüft? ✅/❌]
- [ ] [MANUAL: V5 Fokus aktualisiert? ✅/❌ (Auto-Sync)]
- [ ] [MANUAL: NEXT_STEP.md aktuell? ✅/❌]
- [ ] [MANUAL: Nächste Schritte klar? ✅/❌]
- [ ] [MANUAL: Strategische Pläne verlinkt? ✅/❌]

---
**Session-Ende:** [MANUAL: HH:MM]
**Hauptaufgabe:** [MANUAL: Was war die Hauptaufgabe?]
**Status:** [MANUAL: Fortschritt/Ergebnis]
EOF
    
    # Replace placeholders with actual values
    sed -i '' "s/\[DATUM\]/$(date '+%d.%m.%Y')/g" "$HANDOVER_FILE"
    sed -i '' "s/\[UHRZEIT\]/$(date '+%H:%M')/g" "$HANDOVER_FILE"
    
    log_success "Übergabe-Template erstellt: $HANDOVER_FILE"
    
    # Step 4: Generate quick commands for completion
    echo ""
    log_info "Schritt 4: Hilfs-Befehle für Übergabe..."
    echo ""
    echo "📋 BEFEHLE ZUM AUSFÜLLEN DER ÜBERGABE:"
    echo "======================================"
    echo ""
    echo "# Git Status:"
    echo "git status"
    echo ""
    echo "# TODO Status:"
    echo "TodoRead"
    echo ""
    echo "# Service Status:"
    echo "./scripts/check-services.sh"
    echo ""
    echo "# Java/Node Version:"
    echo "java -version | head -1"
    echo "node --version"
    echo ""
    echo "# Aktiver Fokus:"
    echo "cat .current-focus"
    echo ""
    
    # Step 5: Summary
    echo ""
    log_success "ÜBERGABE-VORBEREITUNG ABGESCHLOSSEN!"
    echo ""
    echo "📁 Datei: $HANDOVER_FILE"
    echo "🔧 Master Plan: ✅ Automatisch synchronisiert"
    echo "📝 Nächster Schritt: Template manuell ausfüllen"
    echo ""
    echo "💡 Tipp: Öffne die Datei und ersetze alle [MANUAL: ...] Markierungen"
}

# Execute main function
main "$@"