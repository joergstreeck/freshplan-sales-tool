#!/bin/bash
# Script to disable/enable tests for debugging hanging CI

set -e

MODE=${1:-disable}
PATTERN=${2:-"*Test.java"}

# Detect OS for sed compatibility
if [[ "$OSTYPE" == "darwin"* ]]; then
    SED_INPLACE="sed -i ''"
else
    SED_INPLACE="sed -i"
fi

if [ "$MODE" = "disable" ]; then
    echo "🔴 Disabling all @QuarkusTest tests matching pattern: $PATTERN"

    # Find all test files and add @Disabled annotation
    find src/test -name "$PATTERN" -type f | while read file; do
        if grep -q "@QuarkusTest" "$file"; then
            # Check if already disabled
            if ! grep -q "@Disabled.*DEBUG: Finding hang" "$file"; then
                # Add import if not present
                if ! grep -q "import org.junit.jupiter.api.Disabled;" "$file"; then
                    $SED_INPLACE '/import org.junit.jupiter.api.Test;/a import org.junit.jupiter.api.Disabled;' "$file"
                fi
                # Add @Disabled before @QuarkusTest
                $SED_INPLACE '/@QuarkusTest/i @Disabled("DEBUG: Finding hang")' "$file"
                echo "  ✓ Disabled: $(basename $file)"
            fi
        fi
    done

elif [ "$MODE" = "enable" ]; then
    echo "🟢 Enabling tests matching pattern: $PATTERN"

    find src/test -name "$PATTERN" -type f | while read file; do
        if grep -q "@Disabled.*DEBUG: Finding hang" "$file"; then
            # Remove the @Disabled annotation
            $SED_INPLACE '/@Disabled("DEBUG: Finding hang")/d' "$file"
            echo "  ✓ Enabled: $(basename $file)"
        fi
    done

elif [ "$MODE" = "list" ]; then
    echo "📋 Test files status:"
    echo "Disabled tests:"
    grep -r "@Disabled.*DEBUG: Finding hang" src/test --include="*.java" | cut -d: -f1 | sort | uniq
    echo ""
    echo "Active @QuarkusTest tests:"
    find src/test -name "*.java" -exec grep -l "@QuarkusTest" {} \; | while read file; do
        if ! grep -q "@Disabled" "$file"; then
            echo "  $(basename $file)"
        fi
    done | sort

else
    echo "Usage: $0 [disable|enable|list] [pattern]"
    echo "  disable - Add @Disabled to all tests matching pattern"
    echo "  enable  - Remove @Disabled from tests matching pattern"
    echo "  list    - Show current test status"
    echo ""
    echo "Examples:"
    echo "  $0 disable                    # Disable all tests"
    echo "  $0 enable 'User*Test.java'    # Enable only User tests"
    echo "  $0 list                        # Show test status"
fi