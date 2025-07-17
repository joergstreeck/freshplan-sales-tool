#!/bin/bash

# UI Development Start Script
# Zweck: UI-Konsistenz sicherstellen vor Frontend-Entwicklung
# Autor: Claude
# Datum: 17.07.2025

set -e

# Farben für Output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Module Parameter
MODULE=${1:-""}
if [ ! -z "$MODULE" ]; then
    MODULE=$(echo "$MODULE" | sed 's/--module=//')
fi

echo -e "${YELLOW}🎨 UI-ENTWICKLUNG VORBEREITEN${NC}"
echo "================================"
echo ""

# Checkliste
echo -e "${YELLOW}📋 PFLICHT-CHECKLISTE${NC}"
echo ""

# 1. UI Sprachregeln
echo -n "1. Hast du UI_SPRACHREGELN.md gelesen? [y/n]: "
read -r sprachregeln
if [ "$sprachregeln" != "y" ]; then
    echo -e "${RED}❌ Abgebrochen. Lies erst die Sprachregeln!${NC}"
    echo "   cat docs/UI_SPRACHREGELN.md"
    exit 1
fi

# 2. Referenz-Komponente
echo -n "2. Hast du eine Referenz-Komponente angeschaut? [y/n]: "
read -r referenz
if [ "$referenz" != "y" ]; then
    echo -e "${YELLOW}📖 Hier sind Referenz-Beispiele:${NC}"
    echo "   - Settings: frontend/src/pages/SettingsPage.tsx"
    echo "   - Sales Cockpit: frontend/src/features/cockpit/components/SalesCockpitV2.tsx"
    echo "   - Customer List: frontend/src/features/customer/components/CustomerListView.tsx"
    echo ""
    echo -e "${RED}❌ Schaue dir erst eine Referenz an!${NC}"
    exit 1
fi

# 3. Base Components
echo -n "3. Kennst du die verfügbaren Base Components? [y/n]: "
read -r base_components
if [ "$base_components" != "y" ]; then
    echo -e "${YELLOW}🎯 Verfügbare Base Components:${NC}"
    if [ -d "frontend/src/components/base" ]; then
        ls -la frontend/src/components/base/ 2>/dev/null || echo "   Noch keine Base Components erstellt"
    else
        echo "   PageWrapper - Für jede Seite"
        echo "   SectionCard - Für jeden Bereich"
        echo "   StandardTable - Für jede Liste"
        echo "   FormDialog - Für jedes Formular"
    fi
    echo ""
    echo -e "${RED}❌ Verstehe erst die Base Components!${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}✅ CHECKLISTE VOLLSTÄNDIG!${NC}"
echo ""

# Templates anzeigen
echo -e "${YELLOW}📖 HIER SIND DEINE TEMPLATES:${NC}"
echo ""
echo "### Neue Seite erstellen:"
echo '```tsx'
echo 'import React from "react";'
echo 'import { MainLayoutV2 } from "../components/layout/MainLayoutV2";'
echo 'import { PageWrapper } from "../components/base/PageWrapper";'
echo 'import { SectionCard } from "../components/base/SectionCard";'
echo 'import { useTranslation } from "../hooks/useTranslation";'
echo ''
echo 'export function [PageName]Page() {'
echo '  const { t } = useTranslation();'
echo '  '
echo '  return ('
echo '    <MainLayoutV2>'
echo '      <PageWrapper title="nav.[pagename]">'
echo '        <SectionCard title="section.[sectionname]">'
echo '          {/* DEIN CONTENT HIER */}'
echo '        </SectionCard>'
echo '      </PageWrapper>'
echo '    </MainLayoutV2>'
echo '  );'
echo '}'
echo '```'
echo ""

echo "### Übersetzungen hinzufügen:"
echo "1. Öffne: frontend/src/i18n/locales/de/[module].json"
echo "2. Ergänze neue Keys:"
echo '   "nav": {'
echo '     "[pagename]": "[Deutscher Begriff]"'
echo '   }'
echo ""

echo -e "${YELLOW}⚠️  WICHTIGE REGELN:${NC}"
echo "- NIEMALS hardcoded Text (z.B. 'Dashboard')"
echo "- IMMER t() für Übersetzungen"
echo "- IMMER Base Components nutzen"
echo "- IMMER deutsche Begriffe aus UI_SPRACHREGELN.md"
echo ""

# Validation Script Info
echo -e "${YELLOW}📊 NACH JEDER KOMPONENTE:${NC}"
echo "   ./scripts/validate-ui-consistency.sh [datei]"
echo ""

# Module-spezifische Hinweise
if [ ! -z "$MODULE" ]; then
    echo -e "${GREEN}🎯 MODUL: ${MODULE}${NC}"
    
    case "$MODULE" in
        "navigation")
            echo "   - Fokus: Rolle-basierte Menüpunkte"
            echo "   - Wichtig: navigation.json nutzen"
            ;;
        "sales-cockpit")
            echo "   - Fokus: 3-Spalten Layout erweitern"
            echo "   - Wichtig: Verkaufszentrale statt Sales Cockpit"
            ;;
        "settings")
            echo "   - Fokus: Tab-basierte Einstellungen"
            echo "   - Wichtig: Einstellungen statt Settings"
            ;;
        "calculator")
            echo "   - Fokus: Modal-Integration"
            echo "   - Wichtig: Kalkulator statt Calculator"
            ;;
        "quick-create")
            echo "   - Fokus: FAB + Dialoge"
            echo "   - Wichtig: Schnellerfassung statt Quick Create"
            ;;
    esac
fi

echo ""
echo -e "${GREEN}✨ Du darfst jetzt entwickeln!${NC}"
echo ""
echo "Happy Coding! 🚀"