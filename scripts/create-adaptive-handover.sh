#!/bin/bash
# Adaptive Handover Creator

TEMPLATE_FILE="docs/templates/HANDOVER_ADAPTIVE_TEMPLATE.md"
OUTPUT_DIR="docs/claude-work/daily-work/$(date +%Y-%m-%d)"
OUTPUT_FILE="$OUTPUT_DIR/$(date +%Y-%m-%d_%H-%M)_ADAPTIVE_HANDOVER.md"

# Ensure output directory exists
mkdir -p "$OUTPUT_DIR"

echo "🤖 Adaptive Handover Creator"
echo "📍 Typ der Beendigung:"
echo "1) Geplant (Standard)"
echo "2) Unterbrochen" 
echo "3) Fehler-Unterbrochen"
read -p "Wähle (1-3): " session_type

# Copy template
cp "$TEMPLATE_FILE" "$OUTPUT_FILE"

# Replace basic placeholders
sed -i '' "s/{{DATE_TIME}}/$(date '+%Y-%m-%d %H:%M:%S')/g" "$OUTPUT_FILE"

case $session_type in
  1)
    # Standard - remove interruption block
    sed -i '' '/## 🚨 UNTERBRECHUNGS-BLOCK/,$d' "$OUTPUT_FILE"
    echo "✅ Standard-Übergabe erstellt"
    ;;
  2)
    # Interrupted - keep interruption block, remove error section
    sed -i '' '/### Bei Fehler-Unterbrechung zusätzlich:/,$d' "$OUTPUT_FILE"
    sed -i '' 's/- \[ \] Ungeplante Unterbrechung/- [x] Ungeplante Unterbrechung/' "$OUTPUT_FILE"
    echo "🚨 Unterbrechungs-Übergabe erstellt"
    ;;
  3)
    # Error interrupted - keep everything
    sed -i '' 's/- \[ \] Fehler-Unterbrechung/- [x] Fehler-Unterbrechung/' "$OUTPUT_FILE"
    echo "❌ Fehler-Unterbrechungs-Übergabe erstellt"
    ;;
esac

echo "📄 Übergabe erstellt: $OUTPUT_FILE"
echo "📝 Bitte manuell ausfüllen!"

# Open in editor (optional)
if command -v code >/dev/null 2>&1; then
  code "$OUTPUT_FILE"
fi