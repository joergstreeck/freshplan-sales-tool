#!/usr/bin/env bash
#
# Pre-commit Hook: Backend/Frontend Field Parity Check
# =====================================================
# Prevents commits with ghost fields in fieldCatalog.json
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Only run if fieldCatalog.json or Customer.java changed
CHANGED_FILES=$(git diff --cached --name-only)

if ! echo "$CHANGED_FILES" | grep -qE "(fieldCatalog\.json|Customer\.java|CustomerLocation\.java)"; then
  # No relevant files changed, skip check
  exit 0
fi

echo ""
echo "⏳ Running Backend/Frontend Field Parity Check..."
echo ""

# Run parity check
if python3 "$SCRIPT_DIR/check-field-parity.py"; then
  exit 0
else
  echo ""
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo "❌ PRE-COMMIT BLOCKED: Field parity violated"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo ""
  echo "To bypass this check (NOT recommended):"
  echo "  git commit --no-verify"
  echo ""
  exit 1
fi
