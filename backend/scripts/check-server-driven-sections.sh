#!/bin/bash
set -euo pipefail

# Server-Driven Sections Architecture Enforcement
# Ensures backend controls ALL wizard structure, frontend renders generically

# Farben
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🏗️  Server-Driven Sections Architecture Check${NC}"
echo "================================================"
echo ""

ERRORS_FOUND=0

# ============================================================================
# BACKEND CHECKS
# ============================================================================

echo -e "${YELLOW}1️⃣  Backend: Prüfe FieldDefinition Wizard-Metadaten...${NC}"
echo ""

# Finde ALLE *SchemaResource.java Dateien im gesamten Projekt
SCHEMA_FILES=$(find src/main/java -name "*SchemaResource.java" -type f)

if [ -z "$SCHEMA_FILES" ]; then
    echo -e "${RED}❌ FEHLER: Keine *SchemaResource.java Dateien gefunden!${NC}"
    exit 1
fi

echo -e "${BLUE}Gefundene Schema-Resources:${NC}"
echo "$SCHEMA_FILES" | while read file; do
    echo -e "  - $(basename "$file")"
done
echo ""

# Extrahiere alle FieldDefinitions mit showInWizard(true)
# Pattern: .showInWizard(true) ... .build()
# Prüfe ob wizardSectionId und wizardSectionTitle vorhanden sind

echo -e "${BLUE}Suche nach Wizard-Fields ohne Section-Metadaten...${NC}"

TOTAL_WIZARD_FIELDS=0
TOTAL_SECTION_IDS=0
TOTAL_SECTION_TITLES=0
MISSING_SECTION_ID=0
MISSING_SECTION_TITLE=0

# Prüfe jede Schema-Resource-Datei
echo "$SCHEMA_FILES" | while read SCHEMA_FILE; do
    if [ -z "$SCHEMA_FILE" ]; then
        continue
    fi

    RESOURCE_NAME=$(basename "$SCHEMA_FILE" .java)

    # Temporäre Datei für Analyse
    TMP_FILE=$(mktemp)
    grep -A 20 'showInWizard(true)' "$SCHEMA_FILE" > "$TMP_FILE" 2>/dev/null || true

    # Zähle showInWizard(true) Vorkommen
    WIZARD_FIELDS=$(grep -c 'showInWizard(true)' "$SCHEMA_FILE" 2>/dev/null || echo "0")
    # Remove any whitespace/newlines and ensure it's a number
    WIZARD_FIELDS=$(echo "$WIZARD_FIELDS" | tr -d '\n' | tr -d ' ')
    WIZARD_FIELDS=${WIZARD_FIELDS:-0}

    if [ "$WIZARD_FIELDS" -gt 0 ] 2>/dev/null; then
        echo -e "${BLUE}  📋 $RESOURCE_NAME: $WIZARD_FIELDS Wizard-Fields${NC}"

        # Zähle wizardSectionId und wizardSectionTitle
        SECTION_IDS=$(grep -c 'wizardSectionId(' "$TMP_FILE" || echo "0")
        SECTION_TITLES=$(grep -c 'wizardSectionTitle(' "$TMP_FILE" || echo "0")

        # Prüfe ob die Anzahl übereinstimmt
        if [ "$SECTION_IDS" -lt "$WIZARD_FIELDS" ]; then
            MISSING=$((WIZARD_FIELDS - SECTION_IDS))
            echo -e "${RED}     ❌ $MISSING Fields ohne wizardSectionId${NC}"
            ERRORS_FOUND=1
        fi

        if [ "$SECTION_TITLES" -lt "$WIZARD_FIELDS" ]; then
            MISSING=$((WIZARD_FIELDS - SECTION_TITLES))
            echo -e "${RED}     ❌ $MISSING Fields ohne wizardSectionTitle${NC}"
            ERRORS_FOUND=1
        fi

        if [ "$SECTION_IDS" -eq "$WIZARD_FIELDS" ] && [ "$SECTION_TITLES" -eq "$WIZARD_FIELDS" ]; then
            echo -e "${GREEN}     ✅ Alle Fields haben Section-Metadaten${NC}"
        fi
    fi

    rm -f "$TMP_FILE"
done

# Gesamtzahl ausgeben (wenn mindestens eine Datei Wizard-Fields hat)
TOTAL_WIZARD_FIELDS=$(echo "$SCHEMA_FILES" | xargs grep -h 'showInWizard(true)' 2>/dev/null | wc -l)
if [ "$TOTAL_WIZARD_FIELDS" -gt 0 ]; then
    echo ""
    echo -e "${BLUE}Gesamt: ${GREEN}$TOTAL_WIZARD_FIELDS${BLUE} Wizard-Fields gefunden${NC}"
fi

echo ""

# ============================================================================
# FRONTEND CHECKS
# ============================================================================

echo -e "${YELLOW}2️⃣  Frontend: Prüfe auf hardcodierte Sections...${NC}"
echo ""

# Finde ALLE Step-Komponenten in ALLEN Features
STEP_FILES=$(find ../frontend/src/features -name "Step*.tsx" -type f 2>/dev/null)

if [ -z "$STEP_FILES" ]; then
    echo -e "${BLUE}ℹ️  Keine Step-Komponenten gefunden${NC}"
else
    HARDCODED_SECTIONS=0

    echo -e "${BLUE}Prüfe Step-Komponenten in allen Features...${NC}"

    echo "$STEP_FILES" | while read -r STEP_FILE; do
        if [ -z "$STEP_FILE" ]; then continue; fi
        FILENAME=$(basename "$STEP_FILE")
        FEATURE=$(echo "$STEP_FILE" | sed 's|.*features/\([^/]*\)/.*|\1|')

        # Skip Step3 und Step4 (verwalten Contacts/Services, nicht Schema-Felder)
        if [[ "$FILENAME" == "Step3"* ]] || [[ "$FILENAME" == "Step4"* ]]; then
            echo -e "${BLUE}  ⏭️  [$FEATURE] $FILENAME (verwaltet separate Entities)${NC}"
            continue
        fi

        # Pattern 1: Hardcodierte Section-Arrays
        if grep -q "const sections\s*=\s*\[" "$STEP_FILE" 2>/dev/null; then
            # Prüfe ob es sich um useMemo mit wizardSectionId handelt (erlaubt)
            if ! grep -q "wizardSectionId" "$STEP_FILE" 2>/dev/null; then
                echo -e "${RED}  ❌ [$FEATURE] $FILENAME enthält hardcodierte sections!${NC}"
                HARDCODED_SECTIONS=$((HARDCODED_SECTIONS + 1))
                ERRORS_FOUND=1
            else
                echo -e "${GREEN}  ✅ [$FEATURE] $FILENAME nutzt server-driven sections${NC}"
            fi
        # Pattern 2: Prüfe ob Schema-Hook verwendet wird (für Step1/Step2)
        elif [[ "$FILENAME" == "Step1"* ]] || [[ "$FILENAME" == "Step2"* ]]; then
            # Suche nach useSchema-Pattern (generisch: useCustomerSchema, useLeadSchema, etc.)
            if grep -q "use.*Schema" "$STEP_FILE" 2>/dev/null; then
                SCHEMA_HOOK=$(grep -o "use[A-Z][a-zA-Z]*Schema" "$STEP_FILE" | head -1)
                echo -e "${GREEN}  ✅ [$FEATURE] $FILENAME nutzt $SCHEMA_HOOK${NC}"
            else
                echo -e "${YELLOW}  ⚠️  [$FEATURE] $FILENAME nutzt keinen Schema-Hook${NC}"
            fi
        fi
    done

    if [ "$HARDCODED_SECTIONS" -eq 0 ]; then
        echo ""
        echo -e "${GREEN}✅ Keine hardcodierten Sections gefunden${NC}"
    fi
fi

echo ""

# ============================================================================
# ZUSAMMENFASSUNG
# ============================================================================

echo -e "${BLUE}📋 Zusammenfassung:${NC}"
echo "================================================"

if [ "$ERRORS_FOUND" -eq 0 ]; then
    echo -e "${GREEN}✅ Server-Driven Sections Architecture: OK${NC}"
    echo ""
    echo -e "${BLUE}Statistiken:${NC}"

    # Gesamtzahl Backend Wizard-Fields
    TOTAL_WIZARD=$(echo "$SCHEMA_FILES" | xargs grep -h 'showInWizard(true)' 2>/dev/null | wc -l | tr -d ' ')
    echo -e "  - Backend Wizard-Fields (gesamt): ${GREEN}$TOTAL_WIZARD${NC}"

    # Anzahl Schema-Resources
    RESOURCE_COUNT=$(echo "$SCHEMA_FILES" | wc -l | tr -d ' ')
    echo -e "  - Schema-Resources gefunden: ${GREEN}$RESOURCE_COUNT${NC}"

    # Frontend Step-Komponenten
    STEP_COUNT=$(echo "$STEP_FILES" | wc -l | tr -d ' ')
    echo -e "  - Frontend Step-Komponenten: ${GREEN}$STEP_COUNT${NC}"
    echo -e "  - Hardcodierte Sections: ${GREEN}0${NC}"
    echo ""
    echo -e "${GREEN}✅ Safe to commit!${NC}"
    exit 0
else
    echo -e "${RED}❌ FEHLER: Server-Driven Sections Violations gefunden!${NC}"
    echo ""
    echo -e "${YELLOW}Bitte behebe folgende Probleme:${NC}"
    echo -e "${YELLOW}  • Backend: Alle Wizard-Fields MÜSSEN wizardSectionId + wizardSectionTitle haben${NC}"
    echo -e "${YELLOW}  • Frontend: Keine hardcodierten Section-Arrays erlaubt${NC}"
    echo ""
    echo -e "${YELLOW}📖 Siehe: docs/planung/SPEC_D11_CUSTOMER_DETAIL_COCKPIT.md${NC}"
    exit 1
fi
