#!/bin/bash

# Universal handover creation script - works from ANY directory
# This is a smart wrapper that finds and executes the best available handover script
# Version: 4.0 - Ultra-robust with multiple fallback strategies

set -euo pipefail

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Find project root using multiple strategies
find_project_root() {
    # Strategy 1: Walk up from current directory looking for .git and CLAUDE.md
    local dir="$PWD"
    while [ "$dir" != "/" ]; do
        if [ -d "$dir/.git" ] && [ -f "$dir/CLAUDE.md" ]; then
            echo "$dir"
            return 0
        fi
        dir=$(dirname "$dir")
    done
    
    # Strategy 2: Check if we're in a subdirectory of freshplan-sales-tool
    dir="$PWD"
    while [ "$dir" != "/" ]; do
        if [[ "$dir" == *"freshplan-sales-tool"* ]]; then
            # Found a dir with the project name, walk up to find the actual root
            while [ "$dir" != "/" ]; do
                if [ -f "$dir/CLAUDE.md" ] && [ -d "$dir/backend" ]; then
                    echo "$dir"
                    return 0
                fi
                dir=$(dirname "$dir")
            done
        fi
        dir=$(dirname "$dir")
    done
    
    # Strategy 3: Use script location as hint
    local script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" 2>/dev/null && pwd)"
    if [ -n "$script_dir" ]; then
        local potential_root="$(cd "$script_dir/.." 2>/dev/null && pwd)"
        if [ -f "$potential_root/CLAUDE.md" ]; then
            echo "$potential_root"
            return 0
        fi
    fi
    
    # Strategy 4: Check common locations
    for location in \
        "/Users/joergstreeck/freshplan-sales-tool" \
        "$HOME/freshplan-sales-tool" \
        "$HOME/projects/freshplan-sales-tool" \
        "$HOME/workspace/freshplan-sales-tool"; do
        if [ -d "$location" ] && [ -f "$location/CLAUDE.md" ]; then
            echo "$location"
            return 0
        fi
    done
    
    echo ""
    return 1
}

# Main execution
main() {
    echo -e "${BLUE}🔍 Searching for FreshPlan project root...${NC}"
    
    PROJECT_ROOT=$(find_project_root)
    
    if [ -z "$PROJECT_ROOT" ]; then
        echo -e "${RED}❌ Could not find FreshPlan project root!${NC}"
        echo -e "${YELLOW}Please ensure you're running this from within the freshplan-sales-tool directory tree.${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Found project root: $PROJECT_ROOT${NC}"
    
    # Change to project root
    cd "$PROJECT_ROOT" || {
        echo -e "${RED}❌ Failed to change to project root${NC}"
        exit 1
    }
    
    # Try to execute the best available handover script
    echo -e "${BLUE}🚀 Starting handover creation...${NC}"
    
    # Priority order of scripts to try
    if [ -x "./scripts/create-handover-universal.sh" ]; then
        echo -e "${GREEN}Using: create-handover-universal.sh (universal version)${NC}"
        exec ./scripts/create-handover-universal.sh "$@"
    elif [ -x "./scripts/handover-with-sync-stable.sh" ]; then
        echo -e "${GREEN}Using: handover-with-sync-stable.sh (with Master Plan sync)${NC}"
        exec ./scripts/handover-with-sync-stable.sh "$@"
    elif [ -x "./scripts/create-handover-improved.sh" ]; then
        echo -e "${GREEN}Using: create-handover-improved.sh (improved version)${NC}"
        exec ./scripts/create-handover-improved.sh "$@"
    elif [ -x "./scripts/handover-with-sync.sh" ]; then
        echo -e "${GREEN}Using: handover-with-sync.sh${NC}"
        exec ./scripts/handover-with-sync.sh "$@"
    else
        echo -e "${YELLOW}⚠️  No specialized handover script found!${NC}"
        echo -e "${BLUE}Creating basic handover document...${NC}"
        
        # Fallback: Create a minimal handover document
        DATE=$(date +"%Y-%m-%d")
        TIME=$(date +"%H-%M")
        HANDOVER_DIR="docs/claude-work/daily-work/$DATE"
        HANDOVER_FILE="$HANDOVER_DIR/${DATE}_HANDOVER_${TIME}.md"
        
        mkdir -p "$HANDOVER_DIR"
        
        cat > "$HANDOVER_FILE" << 'EOF'
# 📋 ÜBERGABE-DOKUMENT

**Erstellt:** $(date "+%Y-%m-%d %H:%M")
**Script:** Minimal fallback

## 🚨 MIGRATION-WARNUNG

Prüfe nächste Migration mit:
```bash
./scripts/get-next-migration.sh
```

## 📋 TODO-STATUS

[MANUAL: TodoRead ausführen und hier eintragen]

## 🎯 WAS WURDE GEMACHT?

[MANUAL: Letzte Aktivitäten eintragen]

## ✅ WAS FUNKTIONIERT?

[MANUAL: Service-Status prüfen]

## ❌ BEKANNTE PROBLEME

[MANUAL: Aktuelle Issues]

## 💡 NÄCHSTE SCHRITTE

[MANUAL: Aus NEXT_STEP.md]

---

**WARNUNG:** Dies ist ein minimales Fallback-Template. 
Nutze wenn möglich die vollständigen Handover-Scripts!
EOF
        
        echo -e "${GREEN}✅ Basic handover created: $HANDOVER_FILE${NC}"
        echo -e "${YELLOW}Please fill in the [MANUAL] sections!${NC}"
    fi
}

# Run main function
main "$@"