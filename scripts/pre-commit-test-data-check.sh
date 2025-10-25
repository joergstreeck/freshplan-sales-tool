#!/bin/bash

# ============================================================================
# Pre-Commit Hook: Test Data Documentation Enforcer
# ============================================================================
#
# Prüft ob Test-Daten-Dokumentation synchron mit Code bleibt.
#
# STUFE 1 (Sanft): Entity/Enum/Migration geändert
#   → Erinnerung, dass Test-Daten geprüft werden sollten
#   → Überspringbar mit "y"
#
# STUFE 2 (SCHARF): TestDataService.java geändert
#   → ZWINGT Aktualisierung von TEST_DATA_QUALITY_CONCEPT.md
#   → Commit BLOCKIERT wenn Doku fehlt
#   → Nur überspringbar mit --no-verify (NOT RECOMMENDED)
#
# Installation:
#   1. Manuell: cp scripts/pre-commit-test-data-check.sh .git/hooks/pre-commit && chmod +x .git/hooks/pre-commit
#   2. Husky: npx husky add .husky/pre-commit "sh scripts/pre-commit-test-data-check.sh"
#   3. Lefthook: Siehe lefthook.yml
#
# ============================================================================

set -e

# Colors for output
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get list of staged files
STAGED_FILES=$(git diff --cached --name-only --diff-filter=ACM)

# Flags
ENTITY_ENUM_MIGRATION_CHANGED=false
TEST_DATA_SERVICE_CHANGED=false
QUALITY_CONCEPT_DOC_CHANGED=false

# ============================================================================
# PRÜFE 1: Entity/Enum/Migration Änderungen
# ============================================================================

if echo "$STAGED_FILES" | grep -qE "(entity/.*\.java|domain/.*\.java|migration/V[0-9]+.*\.sql)"; then
  ENTITY_ENUM_MIGRATION_CHANGED=true
fi

# ============================================================================
# PRÜFE 2: TestDataService Änderungen
# ============================================================================

if echo "$STAGED_FILES" | grep -q "TestDataService.java"; then
  TEST_DATA_SERVICE_CHANGED=true
fi

# ============================================================================
# PRÜFE 3: Dokumentations-Änderungen
# ============================================================================

if echo "$STAGED_FILES" | grep -q "TEST_DATA_QUALITY_CONCEPT.md"; then
  QUALITY_CONCEPT_DOC_CHANGED=true
fi

# ============================================================================
# SCHARFE PRÜFUNG: TestDataService geändert
# ============================================================================

if [ "$TEST_DATA_SERVICE_CHANGED" = true ]; then
  echo ""
  echo -e "${RED}🚨 KRITISCH: TestDataService wurde geändert!${NC}"
  echo ""
  echo "   Du MUSST die Dokumentation aktualisieren:"
  echo ""

  if [ "$QUALITY_CONCEPT_DOC_CHANGED" = false ]; then
    echo -e "   ${RED}❌ TEST_DATA_QUALITY_CONCEPT.md nicht geändert${NC}"
  else
    echo -e "   ${GREEN}✅ TEST_DATA_QUALITY_CONCEPT.md aktualisiert${NC}"
  fi

  echo ""

  # Wenn Doku fehlt → BLOCKIEREN
  if [ "$QUALITY_CONCEPT_DOC_CHANGED" = false ]; then
    echo -e "${RED}   Commit BLOCKIERT. Bitte aktualisiere die Dokumentation.${NC}"
    echo ""
    echo "   📋 Erforderliche Änderungen:"
    echo "      1. docs/testing/TEST_DATA_QUALITY_CONCEPT.md aktualisieren"
    echo "         - Szenario-Matrix erweitern (falls neue Entities)"
    echo "         - Datenmodell-Anforderungen ergänzen (falls neue Felder)"
    echo "         - Implementierungsplan aktualisieren"
    echo ""
    echo -e "   ${YELLOW}Zum Überspringen (NOT RECOMMENDED): git commit --no-verify${NC}"
    echo ""

    exit 1
  else
    # Doku aktualisiert → SUCCESS
    echo -e "${GREEN}✅ TestDataService + Doku aktualisiert${NC}"
    echo -e "${GREEN}✅ Commit erlaubt${NC}"
    echo ""
    exit 0
  fi
fi

# ============================================================================
# SANFTE ERINNERUNG: Entity/Enum/Migration geändert (aber kein TestDataService)
# ============================================================================

if [ "$ENTITY_ENUM_MIGRATION_CHANGED" = true ] && [ "$TEST_DATA_SERVICE_CHANGED" = false ]; then
  echo ""
  echo -e "${YELLOW}⚠️  TESTDATEN-REMINDER:${NC}"
  echo ""
  echo "   Du hast Entities/Enums/Migrations geändert."
  echo "   Bitte prüfe, ob TEST-DATEN aktualisiert werden müssen:"
  echo ""
  echo "   1. Datenmodell: docs/testing/TEST_DATA_QUALITY_CONCEPT.md"
  echo "   2. TestDataService: Neue Szenarien hinzufügen?"
  echo "   3. Szenario-Matrix: Neue Test-Fälle erforderlich?"
  echo ""
  echo -e "   ${YELLOW}Zum Überspringen: git commit --no-verify${NC}"
  echo ""

  # Interaktive Bestätigung (optional)
  read -p "   Testdaten geprüft? (y/n) " -n 1 -r
  echo
  if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo ""
    echo -e "${RED}❌ Commit abgebrochen. Bitte prüfe Testdaten.${NC}"
    echo ""
    exit 1
  fi

  echo ""
  echo -e "${GREEN}✅ Commit erfolgreich${NC}"
  echo ""
  exit 0
fi

# ============================================================================
# KEINE RELEVANTEN ÄNDERUNGEN
# ============================================================================

# Commit durchlassen
exit 0
