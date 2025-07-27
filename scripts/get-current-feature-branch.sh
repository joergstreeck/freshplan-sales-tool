#!/bin/bash

# 🎯 Feature Branch Detection Script (V2.3)
# Ermittelt den aktuellen Feature Branch basierend auf dem aktiven Modul

echo "🔍 Ermittle aktuellen Feature Branch..."
echo "=================================="

# 1. Aktuelle Branch anzeigen
CURRENT_BRANCH=$(git branch --show-current)
echo "📍 Aktueller Branch: $CURRENT_BRANCH"

# 2. Aktives Feature aus .current-focus ermitteln
if [[ -f ".current-focus" ]]; then
    FEATURE=$(jq -r '.feature // "UNKNOWN"' .current-focus)
    echo "🎯 Aktives Feature: $FEATURE"
else
    echo "⚠️  .current-focus nicht gefunden"
    FEATURE="UNKNOWN"
fi

# 3. Passende Feature Branches suchen
echo ""
echo "🔍 Suche Feature Branches für '$FEATURE'..."
FEATURE_BRANCHES=$(git branch | grep -E "feature.*$FEATURE" | head -1 | xargs)

if [[ -n "$FEATURE_BRANCHES" ]]; then
    echo "✅ Gefundener Branch: $FEATURE_BRANCHES"
    echo ""
    echo "💡 EMPFEHLUNG:"
    echo "   git checkout $FEATURE_BRANCHES"
else
    echo "❌ Kein Feature Branch für '$FEATURE' gefunden"
    echo ""
    echo "💡 EMPFEHLUNG:"
    echo "   git checkout -b feature/fc-$(echo $FEATURE | tr '[:upper:]' '[:lower:]')-[description]"
fi

echo ""
echo "🎯 AKTUELLER STATUS:"
echo "   Branch: $CURRENT_BRANCH"
echo "   Feature: $FEATURE"
echo "   Arbeitsphase: $(if [[ $CURRENT_BRANCH == feature/* ]]; then echo '✅ BEREIT'; else echo '⚠️  ORIENTIERUNG'; fi)"