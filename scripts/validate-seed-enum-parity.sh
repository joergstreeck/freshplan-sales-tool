#!/bin/bash

##############################################################################
# SEED-ENUM PARITY VALIDATOR (Simple & Robust)
#
# Prüft ob ALL-CAPS String-Literale aus Seed-Daten in Java-Enums existieren.
#
# Problem: V90009 verwendete FOOD_SERVICE, DAP, PLATINUM bevor
#          die Java-Enums erweitert wurden → Backend-Crash
#
# Sprint: 2.1.7.2 - Prevention für reproduzierbaren Enum-Bug
##############################################################################

set -e

BACKEND_DIR="backend"
SEED_DIR="${BACKEND_DIR}/src/main/resources/db/dev-seed"
ENUM_DIR="${BACKEND_DIR}/src/main/java"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "════════════════════════════════════════════════════════════════"
echo "🔍 SEED-ENUM PARITY CHECK (Simple Mode)"
echo "════════════════════════════════════════════════════════════════"

if [ ! -d "$SEED_DIR" ]; then
  echo -e "${YELLOW}⚠️  No seed directory - skipping${NC}"
  exit 0
fi

##############################################################################
# STEP 1: Extract ALL enum values from Java files
##############################################################################
echo ""
echo "📦 Extracting Java enum values..."

# Combine all enum values from all Java enum files
# Matches:
#   ENUM_VALUE,                    (enum with comma)
#   ENUM_VALUE("text"),            (enum with constructor)
#   ENUM_VALUE;                    (last enum with semicolon)
#   ENUM_VALUE                     (last enum, no trailing punctuation)
ALL_ENUM_VALUES=$(find "$ENUM_DIR" -name "*.java" -type f -exec grep -hE "^[[:space:]]*[A-Z_][A-Z_]*[[:space:]]*(,|\(|;|$)" {} \; 2>/dev/null | \
  sed 's/^[[:space:]]*//' | \
  sed 's/[[:space:]]*(.*//' | \
  sed 's/[,;].*//' | \
  sed 's/[[:space:]]*$//' | \
  sort -u)

ENUM_COUNT=$(echo "$ALL_ENUM_VALUES" | wc -l | xargs)
echo "  Found ${ENUM_COUNT} unique enum values"

##############################################################################
# STEP 2: Extract ALL-CAPS string literals from seed files
##############################################################################
echo ""
echo "📄 Extracting seed data values..."

# Extract all 'VALUE' patterns where VALUE is all-caps
SEED_VALUES=$(grep -h -o "'[A-Z_][A-Z_]*'" "$SEED_DIR"/*.sql 2>/dev/null | \
  tr -d "'" | \
  sort -u)

if [ -z "$SEED_VALUES" ]; then
  echo "  No ALL-CAPS values found in seed data"
  exit 0
fi

SEED_COUNT=$(echo "$SEED_VALUES" | wc -l | xargs)
echo "  Found ${SEED_COUNT} unique ALL-CAPS values"

##############################################################################
# STEP 3: Define whitelist for known non-enum ALL-CAPS values
##############################################################################
# These are legitimate ALL-CAPS values that are NOT enums:
# - Country codes (ISO 3166)
# - Legal entity forms
# - Common SQL keywords/literals
# - UUID/ID prefixes
# - Non-enum business values

WHITELIST="
DE
AT
CHE
DEU
AUT
AG
GMBH
KG
CO
EU
HEADQUARTER
BRANCH_OFFICE
STANDALONE
ADVOCATE
INDIRECT
DIRECT
MAIN
PUBLIC
PRIVATE
KD
LEAD
OPP
QUOTE
INV
ORD
SUMMER
WINTER
"

##############################################################################
# STEP 4: Check each seed value against enum values
##############################################################################
echo ""
echo "🔍 Validating seed values..."
echo ""

ERRORS=0
CHECKED=0
SKIPPED=0

while IFS= read -r seed_value; do
  if [ -z "$seed_value" ]; then
    continue
  fi

  # Check if value is in whitelist (skip validation)
  if echo "$WHITELIST" | grep -q "^${seed_value}$"; then
    echo -e "  ${YELLOW}⊘${NC} ${seed_value} (whitelisted)"
    SKIPPED=$((SKIPPED + 1))
    continue
  fi

  CHECKED=$((CHECKED + 1))

  if echo "$ALL_ENUM_VALUES" | grep -q "^${seed_value}$"; then
    echo -e "  ${GREEN}✓${NC} ${seed_value}"
  else
    echo -e "  ${RED}✗ ${seed_value} - NOT FOUND IN ANY JAVA ENUM!${NC}"
    ERRORS=$((ERRORS + 1))
  fi
done <<< "$SEED_VALUES"

##############################################################################
# FINAL RESULT
##############################################################################
echo ""
echo "════════════════════════════════════════════════════════════════"
echo "📊 Checked ${CHECKED} seed values against ${ENUM_COUNT} enum values"
echo "   Skipped ${SKIPPED} whitelisted values (non-enums)"
echo "════════════════════════════════════════════════════════════════"

if [ $ERRORS -eq 0 ]; then
  echo -e "${GREEN}✅ ALL SEED-ENUM PARITY CHECKS PASSED${NC}"
  echo ""
  exit 0
else
  echo -e "${RED}❌ FOUND $ERRORS INVALID SEED VALUE(S)${NC}"
  echo ""
  echo "🔧 FIX:"
  echo "   1. Add missing enum values to Java enum files, OR"
  echo "   2. Fix seed data to use existing enum values"
  echo ""
  echo "📍 Locations:"
  echo "   - Seed:  ${SEED_DIR}/*.sql"
  echo "   - Enums: ${ENUM_DIR}/**/*.java (all packages)"
  echo ""
  exit 1
fi
