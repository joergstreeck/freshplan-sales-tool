# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**ALLE FILIALSTRUKTUR PROBLEME VOLLSTÄNDIG GELÖST**

**Stand 30.07.2025 22:15:**
- ✅ Dropdown Auto-Width mit useDropdownWidth Hook implementiert
- ✅ CSS-Klasse .field-dropdown-auto in AdaptiveFormContainer + FilialstrukturLayout
- ✅ Info-Hilfe-Icons in FilialstrukturLayout hinzugefügt
- ✅ Nummer-Felder kompakt (60-90px) mit .field-number-compact
- ✅ Zeilenumbruch funktioniert mit Flexbox statt Grid
- ✅ Responsive Breakpoints bei 900px und 600px
- ✅ 13 Unit Tests für useDropdownWidth bestehen alle
- ✅ V6 Migration für expansion_planned vorbereitet

**🚀 NÄCHSTER SCHRITT:**

**Quick-Win-Generator erstellen (TODO-12)**
```bash
# 1. Quick-Win-Generator Konzept erstellen:
# - Basierend auf Pain Points automatisch Verkaufschancen identifizieren
# - Top 3 Verkaufschancen mit konkreten Lösungsvorschlägen
# - Integration in Customer Onboarding Wizard

# 2. Implementation:
# - Backend: QuickWinService + QuickWinController
# - Frontend: QuickWinDisplay Component + Integration in Step 3
```

**KONKRETE AUFGABEN:**
1. **Quick-Win-Generator** (TODO-12) - 2-3 Std
2. **Task Preview MVP** (TODO-6) - 3-4 Std

**WICHTIGE DOKUMENTE:**
- Dropdown Lösung: `/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DROPDOWN_AUTO_WIDTH_*.md` (7 Dateien)
- Flexbox Fix: `/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/FILIALSTRUKTUR_FLEXBOX_FIX.md`
- Responsive Fix: `/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/FILIALSTRUKTUR_RESPONSIVE_FIX.md`

**TECHNISCHE DETAILS:**
- FilialstrukturLayout nutzt jetzt Flexbox mit flex-wrap: wrap
- Nummer-Felder: flex: 0 0 auto mit 60-90px Breite
- Dropdown-Felder: flex: 0 0 auto mit berechneter Breite
- Mobile: flex-direction: column bei <600px

---

## 📊 UI STATUS:
- Filialstruktur komplett responsive: ✅
- Dropdown-Breiten adaptiv: ✅ 
- Nummer-Felder kompakt: ✅
- Info-Icons vorhanden: ✅