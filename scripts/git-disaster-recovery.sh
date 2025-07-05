#!/bin/bash

echo "🚨 Git Disaster Recovery for Critical Docs"
echo "========================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m'

# Critical files
CRITICAL_FILES=(
    "CLAUDE.md"
    "docs/STANDARDUBERGABE_NEU.md"
    "docs/STANDARDUBERGABE_KOMPAKT.md"
    "docs/STANDARDUBERGABE.md"
    "docs/TRIGGER_TEXTS.md"
    "docs/CRM_COMPLETE_MASTER_PLAN.md"
)

echo "This script helps recover critical documentation after Git disasters."
echo ""
echo "Choose recovery method:"
echo "1) Recover from Git history (reflog)"
echo "2) Recover from local backup"
echo "3) Show file history"
echo "4) Emergency: Create minimal templates"
echo ""
read -p "Enter choice (1-4): " choice

case $choice in
    1)
        echo ""
        echo "🔍 Searching Git reflog for critical files..."
        echo ""
        
        # Show recent commits that touched critical files
        echo "Recent commits with critical docs:"
        git log --oneline --grep="STANDARDUBERGABE\|TRIGGER\|CLAUDE.md" -20
        echo ""
        
        # For each critical file, find last known good version
        for file in "${CRITICAL_FILES[@]}"; do
            echo "Checking: $file"
            LAST_COMMIT=$(git log -1 --format="%h" -- "$file" 2>/dev/null)
            if [ -n "$LAST_COMMIT" ]; then
                echo -e "${GREEN}Found in commit: $LAST_COMMIT${NC}"
                echo "  Preview: git show $LAST_COMMIT:$file | head -20"
                echo "  Restore: git checkout $LAST_COMMIT -- $file"
            else
                echo -e "${RED}Not found in recent history${NC}"
            fi
            echo ""
        done
        ;;
        
    2)
        echo ""
        echo "🗄️  Checking local backups..."
        if [ -d "docs/backups" ]; then
            ./scripts/restore-critical-docs.sh
        else
            echo -e "${RED}No backup directory found!${NC}"
            echo "Run ./scripts/backup-critical-docs.sh regularly!"
        fi
        ;;
        
    3)
        echo ""
        echo "📜 File history for critical docs:"
        for file in "${CRITICAL_FILES[@]}"; do
            if git ls-files --error-unmatch "$file" >/dev/null 2>&1; then
                echo ""
                echo "=== $file ==="
                git log --oneline -10 -- "$file" 2>/dev/null || echo "No history found"
            fi
        done
        ;;
        
    4)
        echo ""
        echo -e "${YELLOW}⚠️  Emergency mode - Creating minimal templates${NC}"
        echo "This will create basic templates to get you started."
        echo ""
        read -p "Continue? (y/n): " confirm
        
        if [[ "$confirm" == "y" || "$confirm" == "Y" ]]; then
            # Create minimal TRIGGER_TEXTS.md
            if [ ! -f "docs/TRIGGER_TEXTS.md" ]; then
                cat > "docs/TRIGGER_TEXTS.md" << 'EOF'
# TRIGGER TEXTS - EMERGENCY RECOVERY

## Teil 1: Übergabe erstellen
Erstelle eine vollständige Übergabe mit allen wichtigen Informationen.
Nutze TodoRead, verifiziere Code-Stand, dokumentiere Probleme und Lösungen.

## Teil 2: Session-Start
Führe session-start.sh aus, lies alle Dokumente, melde dich mit Status.
EOF
                echo "✅ Created minimal docs/TRIGGER_TEXTS.md"
            fi
            
            # Create minimal STANDARDUBERGABE_NEU.md
            if [ ! -f "docs/STANDARDUBERGABE_NEU.md" ]; then
                cat > "docs/STANDARDUBERGABE_NEU.md" << 'EOF'
# STANDARDÜBERGABE NEU - EMERGENCY RECOVERY

## 5-Schritte-System
1. System-Check: validate-config.sh, check-services.sh
2. Orientierung: Docs lesen, Git-Status, TodoRead
3. Arbeiten: Code-Validierung, Two-Pass Review
4. Problemlösung: Analysieren, Dokumentieren, Lösungen
5. Übergabe: create-handover.sh ausführen

WICHTIG: Dies ist nur ein Notfall-Template!
Suche die vollständige Version in Git-History oder Backups.
EOF
                echo "✅ Created minimal docs/STANDARDUBERGABE_NEU.md"
            fi
            
            echo ""
            echo -e "${GREEN}Emergency templates created.${NC}"
            echo -e "${YELLOW}⚠️  Find and restore the full versions ASAP!${NC}"
        fi
        ;;
        
    *)
        echo -e "${RED}Invalid choice${NC}"
        exit 1
        ;;
esac

echo ""
echo "💡 Prevention tips:"
echo "1. Run ./scripts/backup-critical-docs.sh regularly"
echo "2. Always use ./scripts/quick-cleanup.sh before commits"
echo "3. Never force-push without checking critical docs"
echo "4. Keep the TRIGGER_TEXTS.md printed/saved externally"