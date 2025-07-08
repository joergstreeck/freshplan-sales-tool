#!/bin/bash

echo "🚀 FreshPlan Session Start"
echo "========================="
echo ""
echo "📅 $(date '+%A, %d. %B %Y - %H:%M Uhr')"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m'

# Step 1: Validate Configuration
echo "1️⃣  Validating Configuration..."
./scripts/validate-config.sh
echo ""

# Step 2: Start Services
echo "2️⃣  Starting Services..."
./scripts/start-services.sh
echo ""

# Step 3: Git Status
echo "3️⃣  Git Repository Status"
echo "------------------------"
echo "Branch: $(git branch --show-current)"
echo "Status:"
git status --short
echo ""
echo "Recent commits:"
git log --oneline -5
echo ""

# Step 4: Check current focus
echo "4️⃣  Current Focus"
echo "----------------"
if [ -f ".current-focus" ]; then
    FEATURE=$(grep '"feature"' .current-focus | cut -d'"' -f4)
    MODULE=$(grep '"module"' .current-focus | cut -d'"' -f4)
    LASTFILE=$(grep '"lastFile"' .current-focus | cut -d'"' -f4)
    LASTLINE=$(grep '"lastLine"' .current-focus | cut -d'"' -f4)
    NEXTTASK=$(grep '"nextTask"' .current-focus | cut -d'"' -f4)
    
    echo -e "${GREEN}📍 Letzter Fokus gefunden:${NC}"
    if [ "$MODULE" != "null" ]; then
        echo "   Feature: $FEATURE-$MODULE"
        echo "   Modul-Dokument: docs/features/$FEATURE-$MODULE-*.md ⭐"
    else
        echo "   Feature: $FEATURE"
    fi
    
    if [ "$LASTFILE" != "null" ] && [ -n "$LASTFILE" ]; then
        echo "   Letzte Datei: $LASTFILE"
        if [ "$LASTLINE" != "null" ] && [ -n "$LASTLINE" ]; then
            echo "   Letzte Zeile: $LASTLINE"
        fi
    fi
    
    if [ "$NEXTTASK" != "null" ] && [ -n "$NEXTTASK" ]; then
        echo -e "   ${YELLOW}Nächste Aufgabe: $NEXTTASK${NC}"
    fi
    echo ""
else
    echo -e "${YELLOW}Kein Fokus gesetzt. Verwende ./scripts/create-handover.sh am Ende der Session.${NC}"
    echo ""
fi

# Step 5: Find latest handover
echo "5️⃣  Latest Handover Document"
echo "---------------------------"
LATEST_HANDOVER=$(find docs/claude-work/daily-work -name "*HANDOVER*.md" -type f -print0 | xargs -0 ls -t | head -1)
if [ -n "$LATEST_HANDOVER" ]; then
    echo -e "${GREEN}Found: $LATEST_HANDOVER${NC}"
    echo ""
    echo "Summary (first 20 lines):"
    echo "------------------------"
    head -20 "$LATEST_HANDOVER" | grep -E "(KRITISCH|TODO|Nächster Schritt|Status:)" || head -20 "$LATEST_HANDOVER"
else
    echo -e "${YELLOW}No previous handover found${NC}"
fi
echo ""

# Step 6: Show TODOs
echo "6️⃣  Current TODOs"
echo "----------------"
echo "[Use TodoRead in your session]"
echo ""

# Step 7: Quick Actions
echo "📋 Quick Actions:"
echo "----------------"
echo "• Read CLAUDE.md:        cat docs/CLAUDE.md | head -50"
echo "• Read latest handover:  cat $LATEST_HANDOVER"
echo "• Create new handover:   ./scripts/create-handover.sh"
echo "• Run tests:            cd backend && ./mvnw test"
echo "• View logs:            tail -f logs/backend.log"
echo ""

# Step 8: Important reminders
echo -e "${YELLOW}⚠️  Wichtige Erinnerungen:${NC}"
echo "• Gründlichkeit vor Schnelligkeit"
echo "• Two-Pass Review bei wichtigen Änderungen"
echo "• Keine neuen Dateien ohne Grund erstellen"
echo "• Repository vor Git-Push säubern: ./scripts/quick-cleanup.sh"
echo ""

echo -e "${GREEN}✅ Session ready! Happy coding!${NC}"
echo ""
echo "Next: Read the documents in order:"
echo "1. docs/CLAUDE.md"
echo "2. Latest handover"
echo "3. docs/STANDARDUBERGABE_NEU.md"
echo ""
echo "🔒 Critical docs backup:"
echo "   ./scripts/backup-critical-docs.sh  - Create backup"
echo "   ./scripts/restore-critical-docs.sh - Restore from backup"