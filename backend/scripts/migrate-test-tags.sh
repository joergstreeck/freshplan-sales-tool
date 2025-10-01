#!/bin/bash
#
# Script: migrate-test-tags.sh
# Purpose: Migriert veraltete Test-Tags zu semantischen Tags
# Phase 2.4: Test-Tags systematisch
#
# Usage:
#   ./scripts/migrate-test-tags.sh --from migrate --dry-run
#   ./scripts/migrate-test-tags.sh --from core --execute
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
TEST_DIR="$PROJECT_ROOT/src/test/java"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

FROM_TAG=""
DRY_RUN=true

# Parse arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --from)
      FROM_TAG="$2"
      shift 2
      ;;
    --dry-run)
      DRY_RUN=true
      shift
      ;;
    --execute)
      DRY_RUN=false
      shift
      ;;
    *)
      echo -e "${RED}Unknown option: $1${NC}"
      exit 1
      ;;
  esac
done

if [ -z "$FROM_TAG" ]; then
  echo -e "${RED}Error: --from <tag> is required${NC}"
  echo "Usage: $0 --from <migrate|core> [--dry-run|--execute]"
  exit 1
fi

echo -e "${BLUE}=== Test Tag Migration ===${NC}"
echo -e "${YELLOW}From Tag: @Tag(\"$FROM_TAG\")${NC}"
echo -e "${YELLOW}Mode: $([ "$DRY_RUN" = true ] && echo "DRY RUN" || echo "EXECUTE")${NC}"
echo ""

# Function to determine target tag based on file analysis
determine_target_tag() {
  local file="$1"
  local has_quarkus_test=$(grep -q "@QuarkusTest" "$file" && echo "yes" || echo "no")
  local has_rest_assured=$(grep -q "RestAssured\|@TestHTTPEndpoint\|given()" "$file" && echo "yes" || echo "no")
  local has_resource=$(echo "$file" | grep -q "Resource.*IT\.java\|ResourceIT\.java\|api/.*IT\.java" && echo "yes" || echo "no")

  if [ "$has_quarkus_test" = "no" ]; then
    # Plain JUnit → unit
    echo "unit"
  elif [ "$has_rest_assured" = "yes" ] || [ "$has_resource" = "yes" ]; then
    # REST-API Tests → e2e
    echo "e2e"
  else
    # Service/Repository Tests → integration
    echo "integration"
  fi
}

# Find all files with the source tag
FILES=$(find "$TEST_DIR" -name "*Test.java" -type f -exec grep -l "@Tag(\"$FROM_TAG\")" {} \;)

if [ -z "$FILES" ]; then
  echo -e "${YELLOW}No files found with @Tag(\"$FROM_TAG\")${NC}"
  exit 0
fi

# Count by target tag (Bash 3 compatible)
UNIT_COUNT=0
INTEGRATION_COUNT=0
E2E_COUNT=0
TOTAL=0

echo -e "${BLUE}Analyzing files...${NC}"
while IFS= read -r file; do
  target_tag=$(determine_target_tag "$file")
  case "$target_tag" in
    unit) UNIT_COUNT=$((UNIT_COUNT + 1)) ;;
    integration) INTEGRATION_COUNT=$((INTEGRATION_COUNT + 1)) ;;
    e2e) E2E_COUNT=$((E2E_COUNT + 1)) ;;
  esac
  TOTAL=$((TOTAL + 1))
done <<< "$FILES"

echo -e "${GREEN}Found $TOTAL files to migrate:${NC}"
[ $UNIT_COUNT -gt 0 ] && echo -e "  → @Tag(\"unit\"): $UNIT_COUNT files"
[ $INTEGRATION_COUNT -gt 0 ] && echo -e "  → @Tag(\"integration\"): $INTEGRATION_COUNT files"
[ $E2E_COUNT -gt 0 ] && echo -e "  → @Tag(\"e2e\"): $E2E_COUNT files"
echo ""

# Perform migration
if [ "$DRY_RUN" = true ]; then
  echo -e "${YELLOW}DRY RUN - Showing what would be changed:${NC}"
  echo ""

  while IFS= read -r file; do
    target_tag=$(determine_target_tag "$file")
    rel_path="${file#$PROJECT_ROOT/}"
    echo -e "${BLUE}$rel_path${NC}"
    echo -e "  @Tag(\"${FROM_TAG}\") → @Tag(\"${target_tag}\")"
    echo ""
  done <<< "$FILES"

  echo -e "${YELLOW}To apply changes, run: $0 --from $FROM_TAG --execute${NC}"
else
  echo -e "${GREEN}EXECUTING migration...${NC}"
  echo ""

  MIGRATED=0
  while IFS= read -r file; do
    target_tag=$(determine_target_tag "$file")
    rel_path="${file#$PROJECT_ROOT/}"

    # Perform sed replacement
    if [[ "$OSTYPE" == "darwin"* ]]; then
      # macOS
      sed -i '' "s/@Tag(\"$FROM_TAG\")/@Tag(\"$target_tag\")/" "$file"
    else
      # Linux
      sed -i "s/@Tag(\"$FROM_TAG\")/@Tag(\"$target_tag\")/" "$file"
    fi

    echo -e "${GREEN}✓${NC} $rel_path → @Tag(\"$target_tag\")"
    MIGRATED=$((MIGRATED + 1))
  done <<< "$FILES"

  echo ""
  echo -e "${GREEN}Migration complete: $MIGRATED files updated${NC}"
  echo -e "${YELLOW}Next steps:${NC}"
  echo "  1. Run tests: ./mvnw test"
  echo "  2. Commit changes: git add -A && git commit -m \"chore: migrate @Tag(\\\"$FROM_TAG\\\") to semantic tags\""
fi
