# 🧮 M8 CALCULATOR MODAL (KOMPAKT)

**Erstellt:** 17.07.2025 14:25  
**Status:** 📋 READY TO START  
**Feature-Typ:** 🎨 FRONTEND  
**Priorität:** HIGH - Direkt nach M4  

## 🚨 BEI FRONTEND-ARBEIT:
```bash
./scripts/ui-development-start.sh --module=calculator
```

---

## 🧠 WAS WIR BAUEN

**Problem:** Calculator ist isoliert, keine Kontext-Daten  
**Lösung:** Modal-Wrapper mit intelligenter Datenübernahme  
**Value:** Nahtloser Workflow von Opportunity → Angebot  

> **Business Case:** 80% weniger Dateneingabe durch Context-Awareness

### 🎯 Geführte Freiheit Integration:
- **Trigger:** Action Button "Angebot erstellen" auf Opportunity
- **Context:** Kundendaten + Opportunity-Wert vorausgefüllt
- **Guided Flow:** Nach Kalkulation → direkt zu E-Mail/PDF
- **Flexibilität:** Auch standalone über Menü erreichbar

---

## 🚀 SOFORT LOSLEGEN (15 MIN)

### 1. **Modal Wrapper Component:**
```bash
cd frontend/src/features/calculator
touch CalculatorModal.tsx
# → Verwendet bestehenden Calculator
```

### 2. **Context Provider:**
```bash
touch contexts/CalculatorContext.tsx
# → Managed Datenübergabe
```

### 3. **Integration in Opportunity:**
```typescript
// In OpportunityCard.tsx
<Button onClick={() => openCalculator({
  customer: opportunity.customer,
  value: opportunity.value,
  title: opportunity.title
})}>
  Angebot erstellen
</Button>
```

**Geschätzt: 3-5 Tage**

---

## 📋 CONTEXT-AWARE FEATURES

### Was wird übernommen:
- **Kunde:** Name, Ansprechpartner, Konditionen
- **Opportunity:** Titel → Angebots-Titel
- **Wert:** Als Richtwert/Ziel
- **Produkte:** Letzte Bestellung als Template

### Modal Controls:
- **Save & Close:** Zurück zur Opportunity
- **Save & Email:** Öffnet E-Mail-Dialog
- **Save & PDF:** Download + Attach to Opportunity
- **Als Template:** Für ähnliche Angebote

---

## 🔗 VOLLSTÄNDIGE DETAILS

**Implementation Guide:** [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md)
- [Modal Setup](#modal-setup) - MUI Dialog Best Practices
- [Context Flow](#context-flow) - Datenübergabe-Architektur
- [Calculator API](#calculator-api) - Integration Points
- [State Management](#state-management) - Redux/Context
- [Guided Actions](#guided-actions) - Nach-Kalkulation Flow

**Entscheidungen:** [DECISION_LOG.md](./DECISION_LOG.md)
- Modal vs. Full Page?
- Autosave Frequenz?
- Template Management?

---

## 📞 NÄCHSTE SCHRITTE

1. **CalculatorModal Component** - MUI Dialog
2. **Context Provider** - Datenübergabe
3. **API Integration** - Load/Save Kalkulationen
4. **Opportunity Binding** - Verknüpfung
5. **Action Buttons** - Save & Continue
6. **Template System** - Wiederverwendung
7. **Mobile Responsive** - Touch-optimiert

**WICHTIG:** Legacy Calculator Code NICHT ändern, nur wrappen!

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[🔒 FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_KOMPAKT.md)** - User Authentication für Calculator-Zugriff
- **[📊 M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_KOMPAKT.md)** - Trigger-Context für Modal

### ⚡ Integration mit:
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)** - Quick-Action in Customer Card
- **[👨‍💼 FC-007 Chef-Dashboard](/docs/features/PLANNED/10_chef_dashboard/FC-007_KOMPAKT.md)** - Angebots-KPIs
- **[📱 FC-006 Mobile App](/docs/features/PLANNED/09_mobile_app/FC-006_KOMPAKT.md)** - Mobile Calculator-Ansicht

### 🚀 Ermöglicht folgende Features:
- **[📄 PDF-001 PDF Generator](#)** *(geplant)* - Angebots-PDF aus Calculator
- **[📧 FC-003 E-Mail Integration](/docs/features/PLANNED/06_email_integration/FC-003_KOMPAKT.md)** - Angebot per Mail versenden
- **[🎯 FC-031 Smart Templates](/docs/features/PLANNED/31_smart_templates/FC-031_KOMPAKT.md)** - Wiederverwendbare Kalkulationen

### 🎨 UI Integration:
- **[🧭 M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_NAVIGATION_KOMPAKT.md)** - Menü-Eintrag für standalone
- **[➕ M2 Quick Create](/docs/features/ACTIVE/05_ui_foundation/M2_QUICK_CREATE_KOMPAKT.md)** - FAB für neue Kalkulation

### 🔧 Technische Details:
- [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md) *(geplant)* - Vollständige technische Umsetzung
- [DECISION_LOG.md](./DECISION_LOG.md) *(geplant)* - Architektur-Entscheidungen