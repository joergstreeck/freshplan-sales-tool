#!/usr/bin/env bash
set -euo pipefail

# ============================================================================
# Backend/Frontend Field Parity Guard
# ============================================================================
# Purpose: Ensure ALL fields in fieldCatalog.json exist in Customer.java
# Usage: ./scripts/check-field-parity.sh
# Exit: 0 = OK, 1 = Ghost fields found
# ============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

CUSTOMER_ENTITY="$PROJECT_ROOT/backend/src/main/java/de/freshplan/domain/customer/entity/Customer.java"
FIELD_CATALOG="$PROJECT_ROOT/frontend/src/features/customers/data/fieldCatalog.json"

echo "🔍 Backend/Frontend Field Parity Check"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check if files exist
if [[ ! -f "$CUSTOMER_ENTITY" ]]; then
  echo "❌ ERROR: Customer.java not found at $CUSTOMER_ENTITY"
  exit 1
fi

if [[ ! -f "$FIELD_CATALOG" ]]; then
  echo "❌ ERROR: fieldCatalog.json not found at $FIELD_CATALOG"
  exit 1
fi

# Extract backend field names from Customer.java
# Pattern: private <Type> <fieldName>;
echo "📋 Extracting backend fields from Customer.java..."
BACKEND_FIELDS=$(grep -E '^\s*private\s+\w+(\<.*\>)?\s+\w+\s*;' "$CUSTOMER_ENTITY" \
  | sed -E 's/.*private\s+\w+(<.*>)?\s+(\w+)\s*;.*/\2/' \
  | sort -u)

BACKEND_COUNT=$(echo "$BACKEND_FIELDS" | wc -l | tr -d ' ')
echo "✅ Found $BACKEND_COUNT backend fields"

# Extract frontend field keys from fieldCatalog.json
echo "📋 Extracting frontend fields from fieldCatalog.json..."
FRONTEND_FIELDS=$(grep -o '"key": "[^"]*"' "$FIELD_CATALOG" \
  | sed 's/"key": "//' \
  | sed 's/"//' \
  | sort -u)

FRONTEND_COUNT=$(echo "$FRONTEND_FIELDS" | wc -l | tr -d ' ')
echo "✅ Found $FRONTEND_COUNT frontend fields"

# Find ghost fields (in frontend but not in backend)
echo ""
echo "🔍 Checking for ghost fields (frontend-only)..."

GHOST_FIELDS=""
GHOST_COUNT=0

while IFS= read -r frontend_field; do
  # Skip empty lines
  [[ -z "$frontend_field" ]] && continue
  
  # Check if field exists in backend
  if ! echo "$BACKEND_FIELDS" | grep -q "^${frontend_field}$"; then
    GHOST_FIELDS+="  - $frontend_field\n"
    ((GHOST_COUNT++))
  fi
done <<< "$FRONTEND_FIELDS"

# Report results
echo ""
if [[ $GHOST_COUNT -eq 0 ]]; then
  echo "✅ SUCCESS: All frontend fields exist in backend!"
  echo "   Backend: $BACKEND_COUNT fields"
  echo "   Frontend: $FRONTEND_COUNT fields"
  echo "   Ghost: 0 fields"
  exit 0
else
  echo "❌ FAILURE: Found $GHOST_COUNT ghost fields (frontend-only):"
  echo ""
  echo -e "$GHOST_FIELDS"
  echo ""
  echo "🚫 RULE VIOLATION: Backend/Frontend Parity (ZERO TOLERANCE)"
  echo ""
  echo "📖 Fix by one of:"
  echo "   1. Remove ghost fields from fieldCatalog.json"
  echo "   2. Add missing fields to Customer.java (+ migration)"
  echo ""
  echo "   See CLAUDE.md → BACKEND/FRONTEND PARITY for details"
  exit 1
fi
