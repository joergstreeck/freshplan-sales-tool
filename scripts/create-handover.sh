#!/bin/bash

echo "📝 Creating Handover Documentation"
echo "================================="
echo ""

# Get current date and time
DATE=$(date +%Y-%m-%d)
TIME=$(date +%H-%M)
DATETIME=$(date +"%d.%m.%Y %H:%M")

# Create directory if it doesn't exist
HANDOVER_DIR="docs/claude-work/daily-work/$DATE"
mkdir -p "$HANDOVER_DIR"

# Handover file
HANDOVER_FILE="$HANDOVER_DIR/${DATE}_HANDOVER_${TIME}.md"

# Start creating the handover
cat > "$HANDOVER_FILE" << EOF
# 🔄 STANDARDÜBERGABE - $DATETIME

**WICHTIG: Lies ZUERST diese Dokumente in dieser Reihenfolge:**
1. \`/docs/CLAUDE.md\` (Arbeitsrichtlinien und Standards)
2. Diese Übergabe
3. \`/docs/STANDARDUBERGABE_NEU.md\` als Hauptanleitung

## 📚 Das 3-STUFEN-SYSTEM verstehen

**STANDARDUBERGABE_NEU.md** (Hauptdokument)
- 5-Schritt-Prozess: System-Check → Orientierung → Arbeiten → Problemlösung → Übergabe
- Verwende IMMER als primäre Anleitung
- Enthält alle wichtigen Scripts und Befehle

**STANDARDUBERGABE_KOMPAKT.md** (Ultra-kurz)
- Nur für Quick-Reference wenn du den Prozess schon kennst
- Komprimierte Version für erfahrene Sessions

**STANDARDUBERGABE.md** (Vollständig)
- Nur bei ernsten Problemen verwenden
- Detaillierte Troubleshooting-Anleitungen

---

## 🎯 AKTUELLER STAND (Code-Inspektion-Validiert)

### ✅ SYSTEM-STATUS ($TIME)
EOF

# Add system check results
echo '```' >> "$HANDOVER_FILE"
./scripts/check-services.sh >> "$HANDOVER_FILE" 2>&1
echo '```' >> "$HANDOVER_FILE"
echo "" >> "$HANDOVER_FILE"

# Add Git status
echo "### 📊 Git Status" >> "$HANDOVER_FILE"
echo '```' >> "$HANDOVER_FILE"
echo "Branch: $(git branch --show-current)" >> "$HANDOVER_FILE"
echo "Status: $(git status --porcelain | wc -l) uncommitted changes" >> "$HANDOVER_FILE"
echo "" >> "$HANDOVER_FILE"
echo "Recent commits:" >> "$HANDOVER_FILE"
git log --oneline -5 >> "$HANDOVER_FILE"
echo '```' >> "$HANDOVER_FILE"
echo "" >> "$HANDOVER_FILE"

# Add implementation status
echo "### 🏗️ IMPLEMENTIERTE FEATURES (Code-validiert)" >> "$HANDOVER_FILE"
echo "" >> "$HANDOVER_FILE"
echo "**Customer Module Backend:**" >> "$HANDOVER_FILE"
echo '```bash' >> "$HANDOVER_FILE"
echo "# Entities: $(find backend/src/main/java/de/freshplan/domain/customer/entity -name "*.java" 2>/dev/null | wc -l)" >> "$HANDOVER_FILE"
echo "# Services: $(find backend/src/main/java/de/freshplan/domain/customer/service -name "*.java" 2>/dev/null | wc -l)" >> "$HANDOVER_FILE"
echo "# DTOs: $(find backend/src/main/java/de/freshplan/domain/customer/service/dto -name "*.java" 2>/dev/null | wc -l)" >> "$HANDOVER_FILE"
echo "# Migrations: $(ls backend/src/main/resources/db/migration/V*.sql 2>/dev/null | wc -l)" >> "$HANDOVER_FILE"
echo '```' >> "$HANDOVER_FILE"
echo "" >> "$HANDOVER_FILE"

# Add TODO section
echo "## 📋 WAS WURDE HEUTE GEMACHT?" >> "$HANDOVER_FILE"
echo "" >> "$HANDOVER_FILE"
echo "[MANUELL AUSFÜLLEN]" >> "$HANDOVER_FILE"
echo "" >> "$HANDOVER_FILE"

echo "## 🛠️ WAS FUNKTIONIERT?" >> "$HANDOVER_FILE"
echo "" >> "$HANDOVER_FILE"
echo "[MANUELL AUSFÜLLEN]" >> "$HANDOVER_FILE"
echo "" >> "$HANDOVER_FILE"

echo "## 🚨 WELCHE FEHLER GIBT ES?" >> "$HANDOVER_FILE"
echo "" >> "$HANDOVER_FILE"
echo "[MANUELL AUSFÜLLEN]" >> "$HANDOVER_FILE"
echo "" >> "$HANDOVER_FILE"

echo "## 🔧 WIE WURDEN SIE GELÖST / WAS IST ZU TUN?" >> "$HANDOVER_FILE"
echo "" >> "$HANDOVER_FILE"
echo "[MANUELL AUSFÜLLEN]" >> "$HANDOVER_FILE"
echo "" >> "$HANDOVER_FILE"

echo "## 📈 NÄCHSTE KONKRETE SCHRITTE" >> "$HANDOVER_FILE"
echo "" >> "$HANDOVER_FILE"
echo "[MANUELL AUSFÜLLEN]" >> "$HANDOVER_FILE"
echo "" >> "$HANDOVER_FILE"

echo "## 📚 MASSGEBLICHE DOKUMENTE" >> "$HANDOVER_FILE"
echo "" >> "$HANDOVER_FILE"
echo "- \`/docs/CRM_COMPLETE_MASTER_PLAN.md\` - Phase 1 Customer Management" >> "$HANDOVER_FILE"
echo "- \`/docs/CLAUDE.md\` - Arbeitsrichtlinien" >> "$HANDOVER_FILE"
echo "" >> "$HANDOVER_FILE"

# Add quick start section
cat >> "$HANDOVER_FILE" << 'EOF'
## 🚀 NACH KOMPRIMIERUNG SOFORT AUSFÜHREN

```bash
# 1. Zum Projekt wechseln
cd /Users/joergstreeck/freshplan-sales-tool

# 2. System-Check und Services starten
./scripts/validate-config.sh
./scripts/start-services.sh

# 3. Git-Status
git status
git log --oneline -5

# 4. TODO-Status
TodoRead

# 5. Letzte Übergabe lesen
cat docs/claude-work/daily-work/$(date +%Y-%m-%d)/*HANDOVER*.md | head -50

# 6. [SPEZIFISCHE BEFEHLE FÜR AKTUELLE AUFGABE]
```

---

**Session-Ende:** [ZEIT EINTRAGEN]  
**Hauptaufgabe:** [AUFGABE EINTRAGEN]  
**Status:** [FORTSCHRITT EINTRAGEN]  
**Nächster Schritt:** [PRIORITÄT EINTRAGEN]
EOF

echo ""
echo "✅ Handover template created: $HANDOVER_FILE"
echo ""
echo "📝 Please fill in the [MANUELL AUSFÜLLEN] sections!"
echo ""
echo "To edit: code $HANDOVER_FILE"