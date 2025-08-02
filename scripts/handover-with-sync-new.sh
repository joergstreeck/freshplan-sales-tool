#!/bin/bash

# Enhanced handover creation with Master Plan sync and migration warning
# This script combines sync-master-plan.sh with create-handover.sh

set -euo pipefail

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
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

# Get project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_ROOT" || exit 1

# Main execution
main() {
    echo ""
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
    
    # Step 2: Create handover with robust script
    log_info "Schritt 2: Übergabe mit Migration-Warnung erstellen..."
    if [[ -x "$PROJECT_ROOT/scripts/create-handover.sh" ]]; then
        # Run create-handover.sh which now has all the robust features
        "$PROJECT_ROOT/scripts/create-handover.sh"
        
        echo ""
        log_success "✅ V5 Master Plan wurde automatisch synchronisiert"
        log_success "✅ Übergabe mit prominenter Migration-Warnung erstellt"
        
        # Get the latest handover file
        DATE=$(date +"%Y-%m-%d")
        HANDOVER_DIR="$PROJECT_ROOT/docs/claude-work/daily-work/$DATE"
        LATEST_HANDOVER=$(ls -t "$HANDOVER_DIR"/*_HANDOVER_*.md 2>/dev/null | head -1)
        
        if [[ -n "$LATEST_HANDOVER" ]]; then
            echo ""
            echo "📋 NÄCHSTE SCHRITTE:"
            echo "1. Öffne die Übergabe mit: code $LATEST_HANDOVER"
            echo "2. Fülle alle [MANUAL: ...] Bereiche aus"
            echo "3. Führe TodoRead aus und füge die TODOs ein"
            echo ""
        fi
        
        log_success "Handover-Prozess abgeschlossen!"
    else
        log_error "create-handover.sh nicht gefunden oder nicht ausführbar"
        exit 1
    fi
}

# Run main function
main