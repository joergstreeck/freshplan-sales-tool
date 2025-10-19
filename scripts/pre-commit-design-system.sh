#!/usr/bin/env bash
# Pre-commit Hook: Design System Compliance Check
# Prevents commits with Design System violations (hardcoded colors, fonts, English text)
# Sprint 2.1.7.2: Design System Guards

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Only run if relevant frontend files changed
CHANGED_FILES=$(git diff --cached --name-only)

if ! echo "$CHANGED_FILES" | grep -qE "frontend/src/.*\.(tsx?|jsx?)$"; then
  exit 0
fi

echo ""
echo "🎨 Running Design System Compliance Check..."
echo ""

if python3 "$SCRIPT_DIR/check-design-system.py"; then
  exit 0
else
  echo ""
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo "❌ PRE-COMMIT BLOCKED: Design System violated"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo ""
  echo "Violations detected:"
  echo "  • Hardcoded colors (use theme.palette)"
  echo "  • Hardcoded fonts (use theme.typography)"
  echo "  • English UI text (use German)"
  echo ""
  echo "Reference: docs/planung/grundlagen/DESIGN_SYSTEM.md"
  echo ""
  echo "To bypass this check (NOT recommended):"
  echo "  git commit --no-verify"
  exit 1
fi
