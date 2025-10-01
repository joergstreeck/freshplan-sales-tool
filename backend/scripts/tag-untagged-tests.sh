#!/bin/bash
#
# Script: tag-untagged-tests.sh
# Purpose: Fügt @Tag Annotationen zu Tests ohne Tags hinzu
# Phase 2.4: Test-Tags systematisch
#
# Usage:
#   ./scripts/tag-untagged-tests.sh --dry-run
#   ./scripts/tag-untagged-tests.sh --execute
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

DRY_RUN=true

# Parse arguments
while [[ $# -gt 0 ]]; do
  case $1 in
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
      echo "Usage: $0 [--dry-run|--execute]"
      exit 1
      ;;
  esac
done

echo -e "${BLUE}=== Tag Untagged Tests ===${NC}"
echo -e "${YELLOW}Mode: $([ "$DRY_RUN" = true ] && echo "DRY RUN" || echo "EXECUTE")${NC}"
echo ""

# Function to determine target tag based on file analysis
determine_target_tag() {
  local file="$1"
  local has_quarkus_test=$(grep -q "@QuarkusTest" "$file" && echo "yes" || echo "no")
  local has_mockito=$(grep -q "@ExtendWith(MockitoExtension\|@Mock\|@InjectMocks" "$file" && echo "yes" || echo "no")
  local has_rest_assured=$(grep -q "RestAssured\|@TestHTTPEndpoint\|given()" "$file" && echo "yes" || echo "no")
  local is_api_test=$(echo "$file" | grep -q "/api/.*Test\.java\|Resource.*Test\.java" && echo "yes" || echo "no")

  if [ "$has_quarkus_test" = "no" ]; then
    # Plain JUnit → unit (Mockito, DTO validation, Entity tests, etc.)
    echo "unit"
  elif [ "$has_rest_assured" = "yes" ] || [ "$is_api_test" = "yes" ]; then
    # REST-API Tests → e2e
    echo "e2e"
  else
    # Service/Repository/Infrastructure Tests → integration
    echo "integration"
  fi
}

# Function to add @Tag annotation after import statements
add_tag_annotation() {
  local file="$1"
  local tag="$2"

  # Find the last import line number
  local last_import_line=$(grep -n "^import " "$file" | tail -1 | cut -d: -f1)

  if [ -z "$last_import_line" ]; then
    echo -e "${RED}Error: Could not find import statements in $file${NC}"
    return 1
  fi

  # Check if org.junit.jupiter.api.Tag is already imported
  if ! grep -q "import org.junit.jupiter.api.Tag;" "$file"; then
    # Add import after last existing import
    if [[ "$OSTYPE" == "darwin"* ]]; then
      # macOS
      sed -i '' "${last_import_line}a\\
import org.junit.jupiter.api.Tag;
" "$file"
    else
      # Linux
      sed -i "${last_import_line}a\\import org.junit.jupiter.api.Tag;" "$file"
    fi

    # Update last_import_line since we added a line
    last_import_line=$((last_import_line + 1))
  fi

  # Find the class declaration line (after imports)
  local class_line=$(tail -n +"$last_import_line" "$file" | grep -n "^class \|^public class " | head -1 | cut -d: -f1)

  if [ -z "$class_line" ]; then
    echo -e "${RED}Error: Could not find class declaration in $file${NC}"
    return 1
  fi

  # Calculate absolute line number
  class_line=$((last_import_line + class_line - 1))

  # Insert @Tag annotation before class declaration
  if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS - insert before class line
    sed -i '' "${class_line}i\\
@Tag(\"$tag\")\\
" "$file"
  else
    # Linux
    sed -i "${class_line}i\\@Tag(\"$tag\")" "$file"
  fi
}

# Find all files without @Tag annotation
FILES=$(find "$TEST_DIR" -name "*Test.java" -type f | xargs grep -L "@Tag")

if [ -z "$FILES" ]; then
  echo -e "${YELLOW}No untagged test files found!${NC}"
  exit 0
fi

# Count by target tag
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

echo -e "${GREEN}Found $TOTAL untagged test files:${NC}"
[ $UNIT_COUNT -gt 0 ] && echo -e "  → @Tag(\"unit\"): $UNIT_COUNT files"
[ $INTEGRATION_COUNT -gt 0 ] && echo -e "  → @Tag(\"integration\"): $INTEGRATION_COUNT files"
[ $E2E_COUNT -gt 0 ] && echo -e "  → @Tag(\"e2e\"): $E2E_COUNT files"
echo ""

# Perform tagging
if [ "$DRY_RUN" = true ]; then
  echo -e "${YELLOW}DRY RUN - Showing what would be changed:${NC}"
  echo ""

  while IFS= read -r file; do
    target_tag=$(determine_target_tag "$file")
    rel_path="${file#$PROJECT_ROOT/}"
    echo -e "${BLUE}$rel_path${NC}"
    echo -e "  → @Tag(\"${target_tag}\")"
    echo ""
  done <<< "$FILES"

  echo -e "${YELLOW}To apply changes, run: $0 --execute${NC}"
else
  echo -e "${GREEN}EXECUTING tagging...${NC}"
  echo ""

  TAGGED=0
  while IFS= read -r file; do
    target_tag=$(determine_target_tag "$file")
    rel_path="${file#$PROJECT_ROOT/}"

    # Add @Tag annotation
    if add_tag_annotation "$file" "$target_tag"; then
      echo -e "${GREEN}✓${NC} $rel_path → @Tag(\"$target_tag\")"
      TAGGED=$((TAGGED + 1))
    else
      echo -e "${RED}✗${NC} $rel_path (failed)"
    fi
  done <<< "$FILES"

  echo ""
  echo -e "${GREEN}Tagging complete: $TAGGED files updated${NC}"
  echo -e "${YELLOW}Next steps:${NC}"
  echo "  1. Run tests: ./mvnw test"
  echo "  2. Commit changes: git add -A && git commit -m \"chore: add @Tag annotations to untagged tests\""
fi
