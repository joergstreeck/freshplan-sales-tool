#!/bin/bash
# safe-run.sh - Wrapper für sichere Script-Ausführung

set -euo pipefail

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m'

# Check if script is provided
if [[ $# -eq 0 ]]; then
    echo "Usage: $0 <script> [arguments]"
    echo "Example: $0 ./scripts/session-start.sh"
    exit 1
fi

SCRIPT="$1"
shift

# Get script name for display
SCRIPT_NAME=$(basename "$SCRIPT")

echo "🛡️  Safe Script Execution"
echo "========================"
echo "Script: $SCRIPT_NAME"
echo ""

# Pre-flight checks
echo "🔍 Pre-flight checks..."

# 1. Check if script exists
if [[ ! -f "$SCRIPT" ]]; then
    echo -e "${RED}❌ Script not found: $SCRIPT${NC}"
    exit 1
fi

# 2. Check if executable
if [[ ! -x "$SCRIPT" ]]; then
    echo -e "${YELLOW}⚠️  Script not executable, fixing...${NC}"
    chmod +x "$SCRIPT"
fi

# 3. Check working directory
if [[ ! -f "package.json" ]] && [[ ! -f "pom.xml" ]]; then
    echo -e "${YELLOW}⚠️  Not in project root${NC}"
    
    # Try to find project root
    SEARCH_DIR="$(pwd)"
    while [[ "$SEARCH_DIR" != "/" ]]; do
        if [[ -f "$SEARCH_DIR/package.json" ]] || [[ -f "$SEARCH_DIR/pom.xml" ]]; then
            echo -e "${GREEN}✅ Found project root: $SEARCH_DIR${NC}"
            cd "$SEARCH_DIR"
            break
        fi
        SEARCH_DIR="$(dirname "$SEARCH_DIR")"
    done
fi

# 4. Create log file
LOG_FILE="/tmp/freshplan_${SCRIPT_NAME}_$(date +%Y%m%d_%H%M%S).log"
echo "📄 Log file: $LOG_FILE"
echo ""

# Execute with timeout and error handling
echo "🚀 Executing script..."
echo "===================="
echo ""

# Run script with timeout (5 minutes default)
TIMEOUT=${SCRIPT_TIMEOUT:-300}

if timeout --preserve-status $TIMEOUT "$SCRIPT" "$@" 2>&1 | tee "$LOG_FILE"; then
    echo ""
    echo -e "${GREEN}✅ Script completed successfully${NC}"
    exit 0
else
    EXIT_CODE=$?
    echo ""
    echo -e "${RED}❌ Script failed with exit code: $EXIT_CODE${NC}"
    echo ""
    echo "📋 Error details from log:"
    echo "========================"
    tail -20 "$LOG_FILE" | grep -E "(error|Error|ERROR|failed|Failed|FAILED|❌)" || echo "No specific errors found in log tail"
    echo ""
    echo "💡 Troubleshooting tips:"
    echo "- Check full log: cat $LOG_FILE"
    echo "- Verify services: ./scripts/check-services.sh"
    echo "- Check Java version: java -version"
    echo "- Validate config: ./scripts/validate-config.sh"
    exit $EXIT_CODE
fi