#!/bin/bash
# robust-session-start.sh - Fehlerresistente Version mit besserer Fehlerbehandlung

# Exit on error, undefined variables, and pipe failures
set -euo pipefail

# Trap errors for cleanup
trap 'echo -e "\n${RED}❌ Script failed at line $LINENO${NC}"; exit 1' ERR

# Get script directory reliably
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Source configuration with fallback
if [[ -f "${SCRIPT_DIR}/config/paths.conf" ]]; then
    source "${SCRIPT_DIR}/config/paths.conf"
else
    echo "⚠️  Configuration not found, using defaults"
    PROJECT_ROOT="/Users/joergstreeck/freshplan-sales-tool"
    CLAUDE_MD="${PROJECT_ROOT}/CLAUDE.md"
fi

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m'

# Helper function for safe execution
safe_run() {
    local cmd="$1"
    local description="${2:-Running command}"
    
    echo -n "$description... "
    if eval "$cmd" > /tmp/safe_run.log 2>&1; then
        echo -e "${GREEN}✅${NC}"
        return 0
    else
        echo -e "${RED}❌${NC}"
        echo "Error output:"
        cat /tmp/safe_run.log
        return 1
    fi
}

# Main execution
main() {
    echo "🚀 FreshPlan Session Start (Robust Version)"
    echo "=========================================="
    echo ""
    echo "📅 $(date '+%A, %d. %B %Y - %H:%M Uhr')"
    echo ""
    
    # Check if we're in the right directory
    if [[ ! -f "package.json" ]]; then
        echo -e "${YELLOW}⚠️  Not in project root, attempting to navigate...${NC}"
        if [[ -d "$PROJECT_ROOT" ]]; then
            cd "$PROJECT_ROOT"
            echo -e "${GREEN}✅ Changed to project root${NC}"
        else
            echo -e "${RED}❌ Project root not found at: $PROJECT_ROOT${NC}"
            exit 1
        fi
    fi
    
    # Step 1: Validate Configuration
    echo ""
    echo "1️⃣  Validating Configuration..."
    if [[ -x "./scripts/validate-config.sh" ]]; then
        safe_run "./scripts/validate-config.sh" "Checking development tools"
    else
        echo -e "${YELLOW}⚠️  validate-config.sh not found or not executable${NC}"
    fi
    
    # Step 2: Check Services
    echo ""
    echo "2️⃣  Checking Services..."
    if [[ -x "./scripts/check-services.sh" ]]; then
        if ! safe_run "./scripts/check-services.sh" "Checking running services"; then
            echo -e "${YELLOW}⚠️  Some services might not be running${NC}"
            echo "Attempting to start services..."
            if [[ -x "./scripts/start-services.sh" ]]; then
                safe_run "./scripts/start-services.sh" "Starting services"
            fi
        fi
    fi
    
    # Step 3: Git Status (with error handling)
    echo ""
    echo "3️⃣  Git Repository Status"
    echo "------------------------"
    if command -v git &> /dev/null; then
        echo "Branch: $(git branch --show-current 2>/dev/null || echo 'unknown')"
        echo "Status:"
        git status --short 2>/dev/null || echo "Git status unavailable"
        echo ""
        echo "Recent commits:"
        git log --oneline -5 2>/dev/null || echo "Git log unavailable"
    else
        echo -e "${RED}❌ Git not found${NC}"
    fi
    
    # Step 4: Current Focus with sync check
    echo ""
    echo "4️⃣  Current Focus & Sync Check"
    echo "-----------------------------"
    if [[ -f ".current-focus" ]]; then
        # Safe JSON parsing
        FEATURE=$(grep '"feature"' .current-focus 2>/dev/null | cut -d'"' -f4 || echo "unknown")
        echo -e "${GREEN}📍 Feature: $FEATURE${NC}"
        
        # Check sync with V5
        if [[ -x "./scripts/sync-current-focus.sh" ]]; then
            echo "🔄 Checking focus synchronization..."
            if ./scripts/sync-current-focus.sh > /dev/null 2>&1; then
                echo -e "${GREEN}✅ Focus synchronized with V5${NC}"
            else
                echo -e "${YELLOW}⚠️  Focus sync failed, manual check needed${NC}"
            fi
        fi
    else
        echo -e "${YELLOW}⚠️  No .current-focus file found${NC}"
    fi
    
    # Step 5: Find latest handover (with better error handling)
    echo ""
    echo "5️⃣  Latest Handover Document"
    echo "---------------------------"
    if [[ -d "docs/claude-work/daily-work" ]]; then
        LATEST_HANDOVER=$(find docs/claude-work/daily-work -name "*HANDOVER*.md" -type f 2>/dev/null | sort -r | head -1)
        if [[ -n "$LATEST_HANDOVER" ]]; then
            echo -e "${GREEN}Found: $LATEST_HANDOVER${NC}"
        else
            echo -e "${YELLOW}No handover documents found${NC}"
        fi
    else
        echo -e "${YELLOW}Handover directory not found${NC}"
    fi
    
    # Summary
    echo ""
    echo "📊 Session Start Summary"
    echo "======================="
    
    # Check critical files
    local issues=0
    
    if [[ ! -f "$CLAUDE_MD" ]]; then
        echo -e "${RED}❌ CLAUDE.md not found at: $CLAUDE_MD${NC}"
        ((issues++))
    else
        echo -e "${GREEN}✅ CLAUDE.md found${NC}"
    fi
    
    if [[ ! -x "./scripts/get-active-module.sh" ]]; then
        echo -e "${YELLOW}⚠️  get-active-module.sh not executable${NC}"
        ((issues++))
    else
        echo -e "${GREEN}✅ get-active-module.sh ready${NC}"
    fi
    
    echo ""
    if [[ $issues -eq 0 ]]; then
        echo -e "${GREEN}✅ Session ready! All systems operational.${NC}"
    else
        echo -e "${YELLOW}⚠️  Session ready with $issues warnings.${NC}"
    fi
    
    echo ""
    echo "💡 Next steps:"
    echo "1. Read CLAUDE.md: cat ${CLAUDE_MD}"
    echo "2. Check active module: ./scripts/get-active-module.sh"
    echo "3. Read latest handover (if exists)"
}

# Run main function
main "$@"