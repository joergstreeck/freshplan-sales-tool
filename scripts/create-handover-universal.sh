#!/bin/bash

# =====================================================
# UNIVERSELLES HANDOVER-SCRIPT
# =====================================================
# Funktioniert aus JEDEM Verzeichnis
# Findet automatisch das Projekt-Root
# Erstellt immer eine vollständige Übergabe
# =====================================================

# Farben für Output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Finde Projekt-Root (suche nach .git)
find_project_root() {
    local dir="$PWD"
    while [[ "$dir" != "/" ]]; do
        if [[ -d "$dir/.git" ]]; then
            echo "$dir"
            return 0
        fi
        dir="$(dirname "$dir")"
    done
    echo ""
    return 1
}

# Projekt-Root finden
PROJECT_ROOT=$(find_project_root)
if [[ -z "$PROJECT_ROOT" ]]; then
    echo -e "${RED}❌ Fehler: Kein Git-Repository gefunden!${NC}"
    echo "Bitte im FreshPlan-Projekt ausführen."
    exit 1
fi

echo -e "${GREEN}✅ Projekt-Root gefunden: $PROJECT_ROOT${NC}"
cd "$PROJECT_ROOT"

# Timestamp
TIMESTAMP=$(date +"%Y-%m-%d_%H-%M")
DATE=$(date +"%Y-%m-%d")
TIME=$(date +"%H:%M")

# Handover-Verzeichnis
HANDOVER_DIR="$PROJECT_ROOT/docs/planung/claude-work/daily-work/$DATE"
mkdir -p "$HANDOVER_DIR"

# Handover-Datei
HANDOVER_FILE="$HANDOVER_DIR/${DATE}_HANDOVER_${TIME//:/-}.md"

echo -e "${BLUE}📝 Erstelle Handover: $(basename $HANDOVER_FILE)${NC}"

# =====================================================
# DATEN SAMMELN
# =====================================================

# Git-Status
CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || echo "unbekannt")
MODIFIED_FILES=$(git status --porcelain 2>/dev/null | wc -l | tr -d ' ')
LAST_COMMIT=$(git log -1 --oneline 2>/dev/null || echo "Keine Commits")

# Migration-Nummer
if [[ -d "$PROJECT_ROOT/backend/src/main/resources/db/migration" ]]; then
    LAST_MIGRATION=$(ls -1 "$PROJECT_ROOT/backend/src/main/resources/db/migration/" 2>/dev/null | tail -1 | grep -oE 'V[0-9]+' | sed 's/V//')
    NEXT_MIGRATION=$((LAST_MIGRATION + 1))
elif [[ -d "$PROJECT_ROOT/src/main/resources/db/migration" ]]; then
    LAST_MIGRATION=$(ls -1 "$PROJECT_ROOT/src/main/resources/db/migration/" 2>/dev/null | tail -1 | grep -oE 'V[0-9]+' | sed 's/V//')
    NEXT_MIGRATION=$((LAST_MIGRATION + 1))
else
    NEXT_MIGRATION="UNBEKANNT"
fi

# TODOs aus NEXT_STEP.md
NEXT_STEP_CONTENT=""
if [[ -f "$PROJECT_ROOT/docs/NEXT_STEP.md" ]]; then
    NEXT_STEP_CONTENT=$(cat "$PROJECT_ROOT/docs/NEXT_STEP.md" | head -50)
elif [[ -f "$PROJECT_ROOT/NEXT_STEP.md" ]]; then
    NEXT_STEP_CONTENT=$(cat "$PROJECT_ROOT/NEXT_STEP.md" | head -50)
fi

# Backend Tests Status
cd "$PROJECT_ROOT/backend" 2>/dev/null || cd "$PROJECT_ROOT"
TEST_STATUS="Nicht getestet"
if command -v mvn &> /dev/null || [[ -f "./mvnw" ]]; then
    echo -e "${YELLOW}⏳ Führe schnellen Test-Check aus...${NC}"
    if [[ -f "./mvnw" ]]; then
        TEST_OUTPUT=$(timeout 5 ./mvnw test --version 2>&1)
        TEST_STATUS="Maven verfügbar"
    else
        TEST_OUTPUT=$(timeout 5 mvn test --version 2>&1)
        TEST_STATUS="Maven verfügbar"
    fi
fi

# =====================================================
# HANDOVER ERSTELLEN
# =====================================================

cat > "$HANDOVER_FILE" << EOF
# 🤝 Übergabe: $DATE $TIME
**Branch:** $CURRENT_BRANCH
**Geänderte Dateien:** $MODIFIED_FILES
**Letzter Commit:** $LAST_COMMIT

## 🚨 KRITISCHE INFORMATIONEN

### ⚠️ NÄCHSTE MIGRATION-NUMMER: V${NEXT_MIGRATION}
**NIEMALS eine bereits verwendete Nummer nutzen!**

### 📍 Working Directory
\`\`\`
Aktuell: $(pwd)
Projekt: $PROJECT_ROOT
\`\`\`

## 📋 TODO-Status

### Aus NEXT_STEP.md:
\`\`\`
$NEXT_STEP_CONTENT
\`\`\`

## 🎯 Nächster Schritt

1. **Branch prüfen:**
   \`\`\`bash
   git branch --show-current
   \`\`\`

2. **Migration-Nummer prüfen:**
   \`\`\`bash
   ls -la backend/src/main/resources/db/migration/ | tail -3
   # NÄCHSTE: V${NEXT_MIGRATION}
   \`\`\`

3. **System starten:**
   \`\`\`bash
   cd $PROJECT_ROOT/backend
   ./mvnw quarkus:dev
   \`\`\`

## 🔧 Technische Details

### Git-Status
\`\`\`
$(git status --short 2>/dev/null || echo "Git-Status nicht verfügbar")
\`\`\`

### Test-Status
\`\`\`
$TEST_STATUS
\`\`\`

## ⚠️ Bekannte Probleme

- Scripts müssen aus Projekt-Root aufgerufen werden
- Working Directory kann backend/ oder project-root sein

## 📝 Notizen

_Automatisch erstellt von create-handover-universal.sh_
_Funktioniert aus jedem Verzeichnis!_
EOF

# =====================================================
# ABSCHLUSS
# =====================================================

echo -e "${GREEN}✅ Handover erstellt: $HANDOVER_FILE${NC}"
echo ""
echo -e "${YELLOW}📋 Quick-Info:${NC}"
echo "  Branch: $CURRENT_BRANCH"
echo "  Migration: V${NEXT_MIGRATION}"
echo "  Geänderte Dateien: $MODIFIED_FILES"
echo ""
echo -e "${BLUE}📂 Handover öffnen:${NC}"
echo "  cat $HANDOVER_FILE"
echo ""
echo -e "${GREEN}✨ Fertig!${NC}"