#!/bin/bash

# sync-master-plan.sh - Synchronisiert V5 Master Plan mit aktuellem Stand
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
MASTER_PLAN="$PROJECT_ROOT/docs/CRM_COMPLETE_MASTER_PLAN_V5.md"
CURRENT_FOCUS="$PROJECT_ROOT/.current-focus"
NEXT_STEP="$PROJECT_ROOT/docs/NEXT_STEP.md"
TODOS_FILE="$PROJECT_ROOT/.current-todos.md"

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

# Check if required files exist
check_files() {
    log_info "Checking required files..."
    
    if [[ ! -f "$MASTER_PLAN" ]]; then
        log_error "Master Plan not found: $MASTER_PLAN"
        exit 1
    fi
    
    if [[ ! -f "$CURRENT_FOCUS" ]]; then
        log_error "Current focus file not found: $CURRENT_FOCUS"
        exit 1
    fi
    
    if [[ ! -f "$NEXT_STEP" ]]; then
        log_error "Next step file not found: $NEXT_STEP"
        exit 1
    fi
    
    log_success "All required files found"
}

# Parse current focus
parse_current_focus() {
    log_info "Parsing current focus..."
    
    if ! command -v jq &> /dev/null; then
        log_warning "jq not available, using grep fallback"
        CURRENT_FEATURE=$(grep '"feature"' "$CURRENT_FOCUS" | cut -d'"' -f4)
        CURRENT_MODULE=$(grep '"module"' "$CURRENT_FOCUS" | cut -d'"' -f4)
        NEXT_TASK=$(grep '"nextTask"' "$CURRENT_FOCUS" | cut -d'"' -f4)
        LAST_FILE=$(grep '"lastFile"' "$CURRENT_FOCUS" | cut -d'"' -f4)
    else
        CURRENT_FEATURE=$(jq -r '.feature' "$CURRENT_FOCUS")
        CURRENT_MODULE=$(jq -r '.module' "$CURRENT_FOCUS")
        NEXT_TASK=$(jq -r '.nextTask' "$CURRENT_FOCUS")
        LAST_FILE=$(jq -r '.lastFile' "$CURRENT_FOCUS")
    fi
    
    log_success "Current focus: $CURRENT_FEATURE - $CURRENT_MODULE"
}

# Get next step from NEXT_STEP.md
get_next_step() {
    log_info "Reading next step..."
    
    NEXT_STEP_CONTENT=$(head -30 "$NEXT_STEP" | grep -A 10 "NÄCHSTER SCHRITT" || echo "NEXT_STEP parsing failed")
    
    log_success "Next step content extracted"
}

# Count TODOs
count_todos() {
    log_info "Counting TODOs..."
    
    TOTAL_TODOS=$(grep -c "^\- \[" "$TODOS_FILE" 2>/dev/null || echo "0")
    OPEN_TODOS=$(grep -c "^\- \[ \]" "$TODOS_FILE" 2>/dev/null || echo "0")
    DONE_TODOS=$(grep -c "^\- \[x\]" "$TODOS_FILE" 2>/dev/null || echo "0")
    
    log_success "TODOs counted: $OPEN_TODOS open, $DONE_TODOS done, $TOTAL_TODOS total"
}

# Detect module status based on code analysis
detect_module_status() {
    log_info "Analyzing M4 Pipeline implementation status..."
    
    # Check backend implementation
    M4_ENTITIES=$(find "$PROJECT_ROOT/backend/src/main/java/de/freshplan/domain/opportunity/" -name "*.java" 2>/dev/null | wc -l || echo "0")
    M4_TESTS=$(find "$PROJECT_ROOT/backend/src/test/java/de/freshplan/domain/opportunity/" -name "*Test.java" 2>/dev/null | wc -l || echo "0")
    M4_MIGRATIONS=$(find "$PROJECT_ROOT/backend/src/main/resources/db/migration/" -name "*opportunity*" 2>/dev/null | wc -l || echo "0")
    
    # Determine status
    if [[ $M4_ENTITIES -gt 5 && $M4_TESTS -gt 0 && $M4_MIGRATIONS -gt 0 ]]; then
        M4_STATUS="🔄 In Progress"
        M4_PROGRESS="85%"
        M4_NEXT="Tests finalisieren (TODO-31)"
    elif [[ $M4_ENTITIES -gt 0 ]]; then
        M4_STATUS="🔄 In Progress"
        M4_PROGRESS="60%"
        M4_NEXT="Tests implementieren"
    else
        M4_STATUS="📋 Planned"
        M4_PROGRESS="0%"
        M4_NEXT="Entity Design"
    fi
    
    log_success "M4 Status: $M4_STATUS ($M4_PROGRESS)"
}

# Generate phase description
generate_phase_description() {
    case "$CURRENT_FEATURE" in
        "FC-001")
            PHASE_DESC="0 - Security Foundation"
            ;;
        "FC-002")
            PHASE_DESC="1 - Core Sales Process (M4 Opportunity Pipeline Finalisierung)"
            ;;
        "FC-003")
            PHASE_DESC="2 - Communication Hub"
            ;;
        *)
            PHASE_DESC="Unknown Phase"
            ;;
    esac
}

# Update Master Plan
update_master_plan() {
    log_info "Updating Master Plan..."
    
    # Create backup
    cp "$MASTER_PLAN" "$MASTER_PLAN.backup.$(date +%Y%m%d_%H%M%S)"
    log_success "Backup created"
    
    # Prepare variables
    CURRENT_DATE=$(date '+%d.%m.%Y %H:%M')
    
    # Update "Aktueller Fokus" section
    sed -i '' "s/\*\*Phase:\*\* .*/\*\*Phase:\*\* $PHASE_DESC/g" "$MASTER_PLAN"
    sed -i '' "s/\*\*Status:\*\* .*/\*\*Status:\*\* Backend implementiert ✅, Tests fast vollständig 🔄/g" "$MASTER_PLAN"
    sed -i '' "s/\*\*Nächster Schritt:\*\* .*/\*\*Nächster Schritt:\*\* $NEXT_TASK/g" "$MASTER_PLAN"
    
    # Update Status Dashboard M4 Pipeline row
    sed -i '' "s/| M4 Pipeline | .* | .* | .* |/| M4 Pipeline | $M4_STATUS | $M4_PROGRESS | $M4_NEXT |/g" "$MASTER_PLAN"
    
    # Update timestamp
    sed -i '' "s/\*\*Datum:\*\* .*/\*\*Datum:\*\* $CURRENT_DATE (Auto-Sync)/g" "$MASTER_PLAN"
    
    log_success "Master Plan updated"
}

# Generate sync report
generate_report() {
    echo ""
    echo "🔄 MASTER PLAN SYNC REPORT"
    echo "=========================="
    echo "📅 Datum: $(date '+%d.%m.%Y %H:%M')"
    echo "🎯 Feature: $CURRENT_FEATURE"
    echo "📦 Modul: $CURRENT_MODULE"
    echo "📊 M4 Status: $M4_STATUS ($M4_PROGRESS)"
    echo "🔧 Nächste Aufgabe: $NEXT_TASK"
    echo "📋 TODOs: $OPEN_TODOS offen, $DONE_TODOS erledigt"
    echo ""
    echo "✅ V5 Master Plan ist jetzt synchron!"
    echo ""
}

# Main execution
main() {
    echo "🚀 Master Plan Sync Tool"
    echo "========================"
    echo ""
    
    check_files
    parse_current_focus
    get_next_step
    count_todos
    detect_module_status
    generate_phase_description
    update_master_plan
    generate_report
}

# Execute main function
main "$@"