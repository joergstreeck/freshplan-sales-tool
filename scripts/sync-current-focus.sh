#!/bin/bash
# sync-current-focus.sh - Synchronisiert .current-focus mit V5 Master Plan

set -euo pipefail

echo "🔄 Synchronisiere .current-focus mit V5 Master Plan..."

V5_FILE="docs/CRM_COMPLETE_MASTER_PLAN_V5.md"

# Extrahiere Feature aus V5 (suche FC-XXX Pattern)
V5_FEATURE=$(grep -o "FC-[0-9][0-9][0-9]" "$V5_FILE" | head -1)

# Extrahiere Module-Name aus Arbeits-Dokument Zeile
V5_MODULE=$(grep "Arbeits-Dokument:" "$V5_FILE" | sed 's/.*\[\([^]]*\)\].*/\1/' | head -1)

# Fallback falls keine FC-XXX gefunden
if [[ -z "$V5_FEATURE" ]]; then
    echo "⚠️  Kein FC-XXX Feature in V5 gefunden, verwende FC-005 als Fallback"
    V5_FEATURE="FC-005"
fi

# Fallback für Module
if [[ -z "$V5_MODULE" ]]; then
    echo "⚠️  Kein Modul-Name gefunden, verwende 'Customer Management' als Fallback"
    V5_MODULE="Customer Management Field-Based Architecture"
fi

echo "📍 Gefunden in V5:"
echo "   Feature: $V5_FEATURE"
echo "   Modul: $V5_MODULE"

# Backup der alten .current-focus
if [[ -f .current-focus ]]; then
    cp .current-focus .current-focus.backup
    echo "💾 Backup erstellt: .current-focus.backup"
fi

# Erstelle neue .current-focus
cat > .current-focus << EOF
{
  "feature": "$V5_FEATURE",
  "module": "$V5_MODULE",
  "lastFile": "auto-sync-from-v5",
  "lastLine": "0",
  "nextTask": "Store mit API Services verbinden",
  "lastUpdated": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF

echo "✅ .current-focus erfolgreich synchronisiert!"
echo "📄 Neue Inhalte:"
cat .current-focus

# Teste das Ergebnis
echo -e "\n🧪 Test: get-active-module.sh nach Sync:"
./scripts/get-active-module.sh