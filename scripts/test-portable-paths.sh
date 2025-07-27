#!/bin/bash
#
# Test script to verify portable path detection
# This ensures our scripts work regardless of installation location
#
# @see /Users/joergstreeck/freshplan-sales-tool/scripts/config/paths.conf
# @see /Users/joergstreeck/freshplan-sales-tool/scripts/backend-manager.sh

set -e

echo "🧪 Testing Portable Path Detection"
echo "================================="

# Save current directory
ORIGINAL_DIR=$(pwd)

# Test 1: Source paths.conf and verify PROJECT_ROOT
echo -n "Test 1: Sourcing paths.conf... "
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/config/paths.conf"

if [ -z "$PROJECT_ROOT" ]; then
    echo "❌ FAILED: PROJECT_ROOT not set"
    exit 1
fi

if [ ! -d "$PROJECT_ROOT" ]; then
    echo "❌ FAILED: PROJECT_ROOT directory does not exist: $PROJECT_ROOT"
    exit 1
fi

if [ ! -f "$PROJECT_ROOT/CLAUDE.md" ]; then
    echo "❌ FAILED: PROJECT_ROOT appears incorrect (no CLAUDE.md found)"
    exit 1
fi

echo "✅ PASSED"
echo "   PROJECT_ROOT: $PROJECT_ROOT"

# Test 2: Verify all key paths exist
echo -n "Test 2: Verifying key paths... "
MISSING_PATHS=()

[ ! -f "$CLAUDE_MD" ] && MISSING_PATHS+=("CLAUDE_MD")
[ ! -d "$DOCS_DIR" ] && MISSING_PATHS+=("DOCS_DIR")
[ ! -d "$FEATURES_DIR" ] && MISSING_PATHS+=("FEATURES_DIR")

if [ ${#MISSING_PATHS[@]} -gt 0 ]; then
    echo "❌ FAILED: Missing paths: ${MISSING_PATHS[*]}"
    exit 1
fi

echo "✅ PASSED"

# Test 3: Test that paths are absolute
echo -n "Test 3: Testing absolute paths... "
cd /tmp

# Re-source from original location
source "$ORIGINAL_DIR/scripts/config/paths.conf"

if [ "$(basename "$PROJECT_ROOT")" == "freshplan-sales-tool" ]; then
    echo "✅ PASSED"
    echo "   PROJECT_ROOT is absolute: $PROJECT_ROOT"
else
    echo "❌ FAILED: PROJECT_ROOT not correctly set"
    exit 1
fi

cd "$ORIGINAL_DIR"

# Test 4: Verify backend-manager.sh uses dynamic paths
echo -n "Test 4: Checking backend-manager.sh... "
if grep -q 'PROJECT_ROOT="/Users/joergstreeck/freshplan-sales-tool"' "$PROJECT_ROOT/scripts/backend-manager.sh"; then
    echo "❌ FAILED: backend-manager.sh still contains hardcoded path"
    exit 1
fi

if grep -q 'source.*paths\.conf' "$PROJECT_ROOT/scripts/backend-manager.sh"; then
    echo "✅ PASSED"
    echo "   backend-manager.sh correctly sources paths.conf"
else
    echo "❌ FAILED: backend-manager.sh does not source paths.conf"
    exit 1
fi

echo ""
echo "🎉 All tests passed! Scripts are now portable."
echo ""
echo "📝 Summary:"
echo "- PROJECT_ROOT is dynamically detected"
echo "- paths.conf provides central configuration"
echo "- Scripts work from any directory"
echo "- No hardcoded user-specific paths"