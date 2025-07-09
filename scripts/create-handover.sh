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

echo "## 📝 CHANGE LOGS DIESER SESSION" >> "$HANDOVER_FILE"
echo "- [ ] Change Log erstellt für: [Feature-Name]" >> "$HANDOVER_FILE"
echo "  - Link: \`/docs/claude-work/daily-work/$DATE/$DATE\_CHANGE_LOG_feature.md\`" >> "$HANDOVER_FILE"
echo "- [ ] Weitere Change Logs: [Liste weitere wenn vorhanden]" >> "$HANDOVER_FILE"
echo "" >> "$HANDOVER_FILE"

echo "## 📑 FEATURE-KONZEPTE STATUS-UPDATE" >> "$HANDOVER_FILE"
echo "- [ ] FC-001 (Dynamic Focus List) Status aktualisiert: ✅ Backend / 🔄 Frontend" >> "$HANDOVER_FILE"
echo "  - Link: \`/docs/features/2025-07-07_TECH_CONCEPT_dynamic-focus-list.md\`" >> "$HANDOVER_FILE"
echo "- [ ] Weitere FC-Updates: [Liste weitere aktive Feature-Konzepte]" >> "$HANDOVER_FILE"
echo "" >> "$HANDOVER_FILE"

# Add new FOKUS section
echo "## 🎯 HEUTIGER FOKUS" >> "$HANDOVER_FILE"

# Use get-active-module.sh to find current module info
ACTIVE_MODULE_INFO=$(./scripts/get-active-module.sh 2>/dev/null)

# Check if .current-focus exists and read it
if [ -f ".current-focus" ]; then
    FEATURE=$(grep '"feature"' .current-focus | cut -d'"' -f4 2>/dev/null || echo "")
    MODULE=$(grep '"module"' .current-focus | cut -d'"' -f4 2>/dev/null || echo "")
    LASTFILE=$(grep '"lastFile"' .current-focus | cut -d'"' -f4 2>/dev/null || echo "")
    LASTLINE=$(grep '"lastLine"' .current-focus | cut -d'"' -f4 2>/dev/null || echo "")
    NEXTTASK=$(grep '"nextTask"' .current-focus | cut -d'"' -f4 2>/dev/null || echo "")
    
    if [ "$MODULE" != "null" ] && [ -n "$MODULE" ]; then
        FULL_MODULE="$FEATURE-$MODULE"
        echo "**Aktives Modul:** $FULL_MODULE" >> "$HANDOVER_FILE"
        
        # Try to find the actual spoke document
        SPOKE_DOC=$(find docs/features -name "${FULL_MODULE}*.md" 2>/dev/null | head -1)
        if [ -n "$SPOKE_DOC" ]; then
            echo "**Modul-Dokument:** \`$SPOKE_DOC\` ⭐" >> "$HANDOVER_FILE"
            
            # Extract next step from spoke document
            if grep -q "NÄCHSTER SCHRITT" "$SPOKE_DOC"; then
                echo "" >> "$HANDOVER_FILE"
                echo "**Dokumentierter nächster Schritt aus Spoke:**" >> "$HANDOVER_FILE"
                grep -A 5 "NÄCHSTER SCHRITT" "$SPOKE_DOC" | tail -n +2 | head -5 >> "$HANDOVER_FILE"
                echo "" >> "$HANDOVER_FILE"
            fi
        else
            echo "**Modul-Dokument:** \`/docs/features/$FULL_MODULE-*.md\` ⭐" >> "$HANDOVER_FILE"
        fi
        
        # Hub document
        HUB_DOC=$(find docs/features -name "${FEATURE}-hub.md" 2>/dev/null | head -1)
        if [ -n "$HUB_DOC" ]; then
            echo "**Hub-Dokument:** \`$HUB_DOC\` (Referenz)" >> "$HANDOVER_FILE"
        else
            echo "**Hub-Dokument:** \`/docs/features/$FEATURE-hub.md\` (Referenz)" >> "$HANDOVER_FILE"
        fi
    else
        echo "**Aktives Feature:** $FEATURE" >> "$HANDOVER_FILE"
    fi
    
    if [ "$LASTFILE" != "null" ] && [ -n "$LASTFILE" ]; then
        echo "**Letzte Datei:** $LASTFILE" >> "$HANDOVER_FILE"
        if [ "$LASTLINE" != "null" ] && [ -n "$LASTLINE" ]; then
            echo "**Letzte Zeile:** $LASTLINE" >> "$HANDOVER_FILE"
        fi
    fi
    
    if [ "$NEXTTASK" != "null" ] && [ -n "$NEXTTASK" ]; then
        echo "**Nächster Schritt (aus .current-focus):** $NEXTTASK" >> "$HANDOVER_FILE"
    fi
else
    echo "**Aktives Modul:** [FC-XXX-MX Name]" >> "$HANDOVER_FILE"
    echo "**Modul-Dokument:** [Pfad] ⭐" >> "$HANDOVER_FILE"
    echo "**Hub-Dokument:** [Pfad] (Referenz)" >> "$HANDOVER_FILE"
    echo "**Letzte Zeile bearbeitet:** [Component:Line]" >> "$HANDOVER_FILE"
    echo "**Nächster Schritt:** [Konkrete Aufgabe]" >> "$HANDOVER_FILE"
fi

echo "" >> "$HANDOVER_FILE"

# Add architecture and planning section
echo "## 🏛️ ARCHITEKTUR & PLANUNG" >> "$HANDOVER_FILE"
echo "- [ ] **Feature-Konzept [FC-XXX] geprüft:** Das Konzept ist auf dem neuesten Stand und enthält alle notwendigen, \"kompressionssicheren\" Implementierungsdetails." >> "$HANDOVER_FILE"
echo "  - Dateipfade und Komponenten-Namen definiert" >> "$HANDOVER_FILE"
echo "  - Props und State vollständig spezifiziert" >> "$HANDOVER_FILE"
echo "  - State Management Stores zugeordnet" >> "$HANDOVER_FILE"
echo "  - API-Interaktionen dokumentiert" >> "$HANDOVER_FILE"
echo "  - Kernlogik beschrieben" >> "$HANDOVER_FILE"
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

# 6. Aktives Modul identifizieren
./scripts/get-active-module.sh

# 7. [SPEZIFISCHE BEFEHLE FÜR AKTUELLE AUFGABE]
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

# Ask for next focus module
echo "📍 FOKUS-MODUL für nächste Session festlegen:"
echo ""
echo "Aktuelle Feature-Module:"
echo "1) FC-002-M1: Hauptnavigation"
echo "2) FC-002-M2: Quick-Create"
echo "3) FC-002-M3: Cockpit"
echo "4) FC-002-M4: Neukundengewinnung"
echo "5) FC-002-M5: Kundenmanagement"
echo "6) FC-002-M6: Berichte"
echo "7) FC-002-M7: Einstellungen"
echo "8) Anderes Feature/Modul"
echo "9) Kein spezifisches Modul"
echo ""
read -p "Auswahl (1-9): " choice

# Update .current-focus based on choice
case $choice in
    1)
        MODULE="M1"
        MODULE_NAME="Hauptnavigation"
        ;;
    2)
        MODULE="M2"
        MODULE_NAME="Quick-Create"
        ;;
    3)
        MODULE="M3"
        MODULE_NAME="Cockpit"
        ;;
    4)
        MODULE="M4"
        MODULE_NAME="Neukundengewinnung"
        ;;
    5)
        MODULE="M5"
        MODULE_NAME="Kundenmanagement"
        ;;
    6)
        MODULE="M6"
        MODULE_NAME="Berichte"
        ;;
    7)
        MODULE="M7"
        MODULE_NAME="Einstellungen"
        ;;
    8)
        read -p "Feature-Code (z.B. FC-003): " CUSTOM_FEATURE
        read -p "Modul-Code (z.B. M1): " CUSTOM_MODULE
        FEATURE=$CUSTOM_FEATURE
        MODULE=$CUSTOM_MODULE
        MODULE_NAME="Custom"
        ;;
    9)
        MODULE="null"
        MODULE_NAME="Kein spezifisches Modul"
        ;;
    *)
        MODULE="null"
        MODULE_NAME="Ungültige Auswahl"
        ;;
esac

# Default to FC-002 if not custom
if [ "$choice" -ne 8 ]; then
    FEATURE="FC-002"
fi

# Ask for next task
echo ""
read -p "Nächste konkrete Aufgabe: " NEXT_TASK

# Update .current-focus file
cat > .current-focus << EOF
{
  "feature": "$FEATURE",
  "module": "$MODULE",
  "lastFile": null,
  "lastLine": null,
  "nextTask": "$NEXT_TASK",
  "lastUpdated": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF

echo ""
echo "✅ Fokus für nächste Session gesetzt: $FEATURE-$MODULE ($MODULE_NAME)"
echo "📋 Nächste Aufgabe: $NEXT_TASK"
echo ""
echo "To edit handover: code $HANDOVER_FILE"