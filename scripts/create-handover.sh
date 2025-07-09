#!/bin/bash

# Simple handover creation script - minimal output to avoid crashes
# This script creates a handover template and lets Claude fill it

DATE=$(date +%Y-%m-%d)
TIME=$(date +%H-%M)
DATETIME=$(date +"%d.%m.%Y %H:%M")

# Create directory
HANDOVER_DIR="docs/claude-work/daily-work/$DATE"
mkdir -p "$HANDOVER_DIR"

# Handover file
HANDOVER_FILE="$HANDOVER_DIR/${DATE}_HANDOVER_${TIME}.md"

# Create minimal template
cat > "$HANDOVER_FILE" << 'EOF'
# 🔄 STANDARDÜBERGABE - DATETIME_PLACEHOLDER

**WICHTIG: Lies ZUERST diese Dokumente:**
1. `/docs/CLAUDE.md` (Arbeitsrichtlinien)
2. Diese Übergabe
3. `/docs/STANDARDUBERGABE_NEU.md` als Hauptanleitung

## 🎯 AKTUELLER STAND

### Git Status
```
[Git status wird von Claude eingefügt]
```

### Aktives Modul
**Feature:** [Von Claude ausfüllen]
**Modul:** [Von Claude ausfüllen]
**Dokument:** [Von Claude ausfüllen]

## 📋 WAS WURDE HEUTE GEMACHT?
[Von Claude ausfüllen - konkrete Code-Änderungen]

## ✅ WAS FUNKTIONIERT?
[Von Claude ausfüllen - verifiziert durch Tests]

## 🚨 WELCHE FEHLER GIBT ES?
[Von Claude ausfüllen - mit genauer Fehlermeldung]

## 🔧 NÄCHSTE SCHRITTE
[Von Claude ausfüllen - konkret und umsetzbar]

## 🚀 QUICK START FÜR NÄCHSTE SESSION
```bash
cd /Users/joergstreeck/freshplan-sales-tool
./scripts/validate-config.sh
./scripts/start-services.sh
git status
# [Weitere spezifische Befehle von Claude]
```

---
**Session-Ende:** TIME_PLACEHOLDER  
**Hauptaufgabe:** [Von Claude ausfüllen]  
**Status:** [Von Claude ausfüllen]
EOF

# Replace placeholders
sed -i.bak "s/DATETIME_PLACEHOLDER/$DATETIME/g" "$HANDOVER_FILE"
sed -i.bak "s/TIME_PLACEHOLDER/$TIME/g" "$HANDOVER_FILE"
rm "$HANDOVER_FILE.bak"

# Simple output
echo "✅ Handover template created:"
echo "   $HANDOVER_FILE"
echo ""
echo "📝 Claude, please fill in the template with:"
echo "   - Git status and recent commits"
echo "   - What was done today"
echo "   - Current state and any issues"
echo "   - Next steps for the next session"