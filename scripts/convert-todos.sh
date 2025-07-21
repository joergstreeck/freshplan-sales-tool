#!/bin/bash

# Convert JSON TODOs to Markdown checkboxes
# Usage: ./scripts/convert-todos.sh

if [ ! -f ".current-todos.md" ]; then
    echo "❌ Keine .current-todos.md gefunden!"
    echo "   Führe zuerst 'TodoRead > .current-todos.md' aus"
    exit 1
fi

# Check if already in checkbox format
if grep -q "^\- \[" ".current-todos.md"; then
    echo "✅ TODOs sind bereits im Checkbox-Format"
    exit 0
fi

# Check if JSON format
if ! grep -q '^\[{' ".current-todos.md"; then
    echo "❌ Unbekanntes TODO-Format in .current-todos.md"
    exit 1
fi

# Convert JSON to checkboxes
echo "🔄 Konvertiere JSON zu Checkboxen..."

# Backup original
cp .current-todos.md .current-todos.json.bak

# Convert
{
    echo "PENDING:"
    cat .current-todos.md | jq -r '.[] | select(.status=="pending") | "- [ ] [\(.priority|ascii_upcase)] [ID: \(.id)] \(.content)"'
    echo ""
    echo "COMPLETED:"
    cat .current-todos.md | jq -r '.[] | select(.status=="completed") | "- [x] [\(.priority|ascii_upcase)] [ID: \(.id)] \(.content)"'
} > .current-todos.md

echo "✅ Konvertierung abgeschlossen!"
echo "   Backup: .current-todos.json.bak"