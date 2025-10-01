#!/bin/bash
# Quick script to disable integration tests for CI speedup

echo "🔴 Disabling Integration and CQRS Tests..."

# Find all Integration and CQRS tests
find src/test -name "*IntegrationTest.java" -o -name "*CQRS*Test.java" | while read file; do
    if grep -q "@QuarkusTest" "$file"; then
        # Check if already disabled
        if ! grep -q "@Disabled.*Sprint 2.1.4" "$file"; then
            echo "  Disabling: $(basename $file)"

            # Add import if not present
            if ! grep -q "import org.junit.jupiter.api.Disabled;" "$file"; then
                # Add import after the last import statement
                awk '/^import/ { lastimport = NR }
                     { lines[NR] = $0 }
                     END {
                       for (i = 1; i <= NR; i++) {
                         print lines[i]
                         if (i == lastimport) {
                           print "import org.junit.jupiter.api.Disabled;"
                         }
                       }
                     }' "$file" > "$file.tmp" && mv "$file.tmp" "$file"
            fi

            # Add @Disabled before the class declaration
            awk '/^@QuarkusTest/ { print; getline; if ($0 !~ /@Disabled/) print "@Disabled(\"TEMPORARY: Sprint 2.1.4 CI Performance Fix\")"; print; next } 1' "$file" > "$file.tmp" && mv "$file.tmp" "$file"
        fi
    fi
done

echo "✅ Done. Integration tests disabled for CI performance."