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

# Step 4: Find latest handover
echo "4️⃣  Latest Handover Document"
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

# Step 5: Show TODOs
echo "5️⃣  Current TODOs"
echo "----------------"
echo "[Use TodoRead in your session]"
echo ""

# Step 6: Quick Actions
echo "📋 Quick Actions:"
echo "----------------"
echo "• Read CLAUDE.md:        cat docs/CLAUDE.md | head -50"
echo "• Read latest handover:  cat $LATEST_HANDOVER"
echo "• Create new handover:   ./scripts/create-handover.sh"
echo "• Run tests:            cd backend && ./mvnw test"
echo "• View logs:            tail -f logs/backend.log"
echo ""

# Step 7: Important reminders
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