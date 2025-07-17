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

# Step 2.5: CRITICAL BACKEND CHECK (AUTO-FIX)
echo "🚨 CRITICAL BACKEND VALIDATION"
echo "==============================="
if ! curl -s http://localhost:8080/q/health > /dev/null 2>&1; then
    echo -e "${RED}❌ BACKEND IS DOWN - AUTO-FIXING...${NC}"
    echo "🔧 Setting Java 17..."
    export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
    export PATH=$JAVA_HOME/bin:$PATH
    
    echo "🚀 Starting Backend in background..."
    cd backend
    nohup mvn quarkus:dev > ../logs/backend.log 2>&1 &
    cd ..
    echo "⏳ Backend starting... (will be ready in ~30 seconds)"
    echo -e "${YELLOW}💡 TIP: Run 'curl http://localhost:8080/q/health' in 30s to verify${NC}"
else
    echo -e "${GREEN}✅ Backend is running${NC}"
fi
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
    
    # Frontend Detection
    if [[ "$FEATURE" =~ (M1|M2|M3|M7|UI|Frontend|Navigation|Cockpit|Settings|Calculator) ]]; then
        echo ""
        echo -e "${YELLOW}🎨 HINWEIS: $FEATURE ist ein Frontend-Modul!${NC}"
        echo "   Bei Frontend-Arbeit: ./scripts/ui-development-start.sh"
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
echo -e "${RED}🚨 KRITISCH - TODO-MANAGEMENT:${NC}"
echo "• IMMER als ERSTES: TodoRead ausführen!"
echo "• TODO-Anzahl mit Übergabe vergleichen"
echo "• Bei Diskrepanz: ALLE TODOs aus Übergabe wiederherstellen"
echo "• Bei JEDER Änderung: TodoWrite nutzen"
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