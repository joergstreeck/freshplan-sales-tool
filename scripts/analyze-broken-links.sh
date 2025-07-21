#!/bin/bash

# Analyze all broken links and create categorized report
echo "🔍 Analyzing broken links..."

PROJECT_ROOT="/Users/joergstreeck/freshplan-sales-tool"
cd "$PROJECT_ROOT"

REPORT_FILE="docs/claude-work/daily-work/2025-07-21/link-analysis-report.md"
KOMPAKT_FILE="/tmp/kompakt-links.txt"
BROKEN_FILE="/tmp/broken-links.txt"

# Initialize report
cat > "$REPORT_FILE" << EOF
# Link Analysis Report

**Generated:** $(date)
**Total Files Scanned:** $(find docs/features -name "*.md" -type f | wc -l)

## Summary

EOF

# 1. Find all KOMPAKT links
echo "Finding KOMPAKT links..."
grep -r "KOMPAKT\.md" docs/features/ --include="*.md" | \
  sed 's/:/ | /' | \
  awk -F' | ' '{print $1 " | " $2}' > "$KOMPAKT_FILE"

KOMPAKT_COUNT=$(wc -l < "$KOMPAKT_FILE")

# 2. Create mapping table
echo -e "\n## KOMPAKT Link Mapping\n" >> "$REPORT_FILE"
echo "| Old Pattern | New Pattern | Count |" >> "$REPORT_FILE"
echo "|-------------|-------------|-------|" >> "$REPORT_FILE"

# Count unique KOMPAKT patterns
grep -o "[A-Z0-9_-]*KOMPAKT\.md" "$KOMPAKT_FILE" | \
  sort | uniq -c | sort -nr | \
  while read count pattern; do
    new_pattern="${pattern/KOMPAKT/TECH_CONCEPT}"
    echo "| $pattern | $new_pattern | $count |" >> "$REPORT_FILE"
  done

# 3. Find files with most KOMPAKT links
echo -e "\n## Top Files with KOMPAKT Links\n" >> "$REPORT_FILE"
echo "| File | KOMPAKT Count |" >> "$REPORT_FILE"
echo "|------|---------------|" >> "$REPORT_FILE"

grep -c "KOMPAKT\.md" docs/features/**/*.md 2>/dev/null | \
  grep -v ":0$" | \
  sort -t: -k2 -nr | \
  head -20 | \
  while IFS=: read file count; do
    echo "| $file | $count |" >> "$REPORT_FILE"
  done

# 4. Analyze other broken links
echo -e "\n## Other Broken Links\n" >> "$REPORT_FILE"

# Run comprehensive test and capture broken links
./tests/comprehensive-link-test.sh 2>&1 | \
  grep -E "❌ BROKEN:" | \
  grep -v "KOMPAKT" | \
  sed 's/.*❌ BROKEN: //' > "$BROKEN_FILE"

BROKEN_COUNT=$(wc -l < "$BROKEN_FILE")

# Categorize broken links
echo "### By Type" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

# Count by extension
echo "| Extension | Count |" >> "$REPORT_FILE"
echo "|-----------|-------|" >> "$REPORT_FILE"
grep -o "\.[a-zA-Z]*$" "$BROKEN_FILE" | sort | uniq -c | sort -nr | \
  while read count ext; do
    echo "| $ext | $count |" >> "$REPORT_FILE"
  done

# 5. Create fix priority list
echo -e "\n## Fix Priority\n" >> "$REPORT_FILE"
echo "### Priority 1: ACTIVE Features" >> "$REPORT_FILE"
grep "docs/features/ACTIVE" "$KOMPAKT_FILE" | wc -l | \
  xargs -I {} echo "- ACTIVE KOMPAKT links: {}" >> "$REPORT_FILE"

echo -e "\n### Priority 2: PLANNED Features (Top Features)" >> "$REPORT_FILE"
grep "docs/features/PLANNED" "$KOMPAKT_FILE" | head -10 >> "$REPORT_FILE"

# 6. Generate statistics
echo -e "\n## Statistics\n" >> "$REPORT_FILE"
echo "- Total KOMPAKT links: $KOMPAKT_COUNT" >> "$REPORT_FILE"
echo "- Other broken links: $BROKEN_COUNT" >> "$REPORT_FILE"
echo "- Total broken links: $((KOMPAKT_COUNT + BROKEN_COUNT))" >> "$REPORT_FILE"

# 7. Suggested sed commands
echo -e "\n## Suggested Fix Commands\n" >> "$REPORT_FILE"
echo '```bash' >> "$REPORT_FILE"
echo '# Basic KOMPAKT replacement' >> "$REPORT_FILE"
echo 'find docs/features -name "*.md" -type f -exec \' >> "$REPORT_FILE"
echo '  sed -i "" "s/KOMPAKT\.md/TECH_CONCEPT.md/g" {} \;' >> "$REPORT_FILE"
echo '' >> "$REPORT_FILE"
echo '# Specific patterns' >> "$REPORT_FILE"
grep -o "[A-Z0-9_-]*KOMPAKT\.md" "$KOMPAKT_FILE" | \
  sort | uniq | head -5 | \
  while read pattern; do
    new_pattern="${pattern/KOMPAKT/TECH_CONCEPT}"
    echo "sed -i '' 's|$pattern|$new_pattern|g' docs/features/**/*.md" >> "$REPORT_FILE"
  done
echo '```' >> "$REPORT_FILE"

echo "✅ Analysis complete! Report saved to: $REPORT_FILE"
echo ""
echo "Summary:"
echo "- KOMPAKT links: $KOMPAKT_COUNT"
echo "- Other broken: $BROKEN_COUNT"
echo "- Total broken: $((KOMPAKT_COUNT + BROKEN_COUNT))"

# Cleanup
rm -f "$KOMPAKT_FILE" "$BROKEN_FILE"