# 📋 Sprint 2 Overview - Customer UI Integration & Task Preview

**Sprint:** Sprint 2  
**Dauer:** 3.5 Tage  
**Start:** 28.07.2025  
**Status:** 🚀 Ready to Start  

---

## 📍 Navigation
**← Zurück:** [FC-005 Customer Management](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md)  
**⬆️ Master Plan:** [V5 Complete Master Plan](/Users/joergstreeck/freshplan-sales-tool/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)  
**🚨 KRITISCH:** [Architecture Fix Required](/Users/joergstreeck/freshplan-sales-tool/docs/CRITICAL_FIX_MAINLAYOUT_MISSING.md)  
**→ Start hier:** [Sprint 2 Key Decisions](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SPRINT2_KEY_DECISIONS_SUMMARY.md)

---

## ⚠️ KRITISCHE VORAUSSETZUNG: TypeScript Import Types

**WICHTIG:** Bei `verbatimModuleSyntax: true` müssen ALLE Type-Imports explizit mit `import type` erfolgen!

```typescript
// ✅ RICHTIG für ALLE Sprint 2 Implementierungen:
import type { Customer, Task, FieldDefinition } from './types';

// ❌ FALSCH - führt zu "does not provide an export named" Fehlern:
import { Customer, Task } from './types';
```

**Referenzen:**
- [TypeScript Import Type Guide](/Users/joergstreeck/freshplan-sales-tool/docs/guides/TYPESCRIPT_IMPORT_TYPE_GUIDE.md)
- [Debug Session vom 27.07.2025](/Users/joergstreeck/freshplan-sales-tool/docs/claude-work/daily-work/2025-07-27/2025-07-27_DEBUG_typescript-import-type-marathon.md)

**Diese Regel gilt für ALLE Code-Beispiele in den Sprint 2 Dokumenten!**

---

## 🎯 Sprint Goals auf einen Blick

1. **Customer UI Integration** - Nahtlose Einbindung des CustomerOnboardingWizard
2. **Field Theme System** - Intelligente Feldgrößen-Berechnung ✅
3. **Adaptive Layout Evolution** - Dynamische, inhaltsbasierte Feldanpassung 🆕
4. **Task Preview MVP** - "Das System denkt mit" erlebbar machen  
5. **FC-012 Audit UI** - Transparenz für Admins

---

### 🆕 Adaptive Layout Dokumente:
- **Konzept & Evolution:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/ADAPTIVE_LAYOUT_EVOLUTION.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/ADAPTIVE_LAYOUT_EVOLUTION.md)
- **Implementation Guide:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/ADAPTIVE_LAYOUT_IMPLEMENTATION_GUIDE.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/ADAPTIVE_LAYOUT_IMPLEMENTATION_GUIDE.md)
- **Rollout & Testing:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/ADAPTIVE_LAYOUT_ROLLOUT_GUIDE.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/ADAPTIVE_LAYOUT_ROLLOUT_GUIDE.md)
- **🔧 FIX PLAN:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/ADAPTIVE_THEME_FIX_PLAN.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/ADAPTIVE_THEME_FIX_PLAN.md) - Korrektur für aktuelle Probleme
- **🛠️ IMPLEMENTATION:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/ADAPTIVE_THEME_IMPLEMENTATION.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/ADAPTIVE_THEME_IMPLEMENTATION.md) - Schritt-für-Schritt Anleitung

---

## 📍 Navigation (Absolute Links für Claude)

### Kern-Dokumente für Sprint 2:
- **Philosophie & Ansatz:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/PHILOSOPHY_AND_APPROACH.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/PHILOSOPHY_AND_APPROACH.md)
- **Quick Reference:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/QUICK_REFERENCE.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/QUICK_REFERENCE.md)

### Tägliche Implementierungs-Guides:
- **Tag 1:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY1_IMPLEMENTATION.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY1_IMPLEMENTATION.md) - Foundation & Quick Wins
- **Tag 2:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY2_IMPLEMENTATION.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY2_IMPLEMENTATION.md) - Task Engine & API
- **Tag 3:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY3_IMPLEMENTATION.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY3_IMPLEMENTATION.md) - Mobile & Audit UI
- **Tag 3.5:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY3_5_FINAL.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY3_5_FINAL.md) - Final Polish

### 🧪 Sprint 2 Prototypes:
- **Field Theme System Konzept:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/FIELD_THEME_SYSTEM_PROTOTYPE.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/FIELD_THEME_SYSTEM_PROTOTYPE.md) - Blueprint & Konzept
- **Field Theme Implementation:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/FIELD_THEME_IMPLEMENTATION.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/FIELD_THEME_IMPLEMENTATION.md) - Code & Sprint 2 Guide

### Weitere Dokumente:

### 🎯 Wichtige Entscheidungen (Navigations-Reihenfolge):
1. **Sidebar Discussion:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SIDEBAR_LEAD_DISCUSSION.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SIDEBAR_LEAD_DISCUSSION.md) - UI/UX Diskussion
2. **Lead-Kunde-Trennung:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/LEAD_CUSTOMER_SEPARATION_DECISION.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/LEAD_CUSTOMER_SEPARATION_DECISION.md) - Warum FC-020 separat
3. **Sidebar Config:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SIDEBAR_NAVIGATION_CONFIG.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SIDEBAR_NAVIGATION_CONFIG.md) - Finale Implementierung
4. **⭐ Zusammenfassung ALLER Entscheidungen:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SPRINT2_KEY_DECISIONS_SUMMARY.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SPRINT2_KEY_DECISIONS_SUMMARY.md) - START HIER!
- **UI Diskussion & Entscheidungen:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/discussions/2025-07-27_UI_INTEGRATION_DISCUSSION.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/discussions/2025-07-27_UI_INTEGRATION_DISCUSSION.md)

---

## ✅ Was wurde entschieden?

### UI Integration:
- **Button-Platzierung:** Hybrid-Ansatz (über Tabelle + Empty State Hero)
- **Wizard:** Modal (Desktop) / Drawer (Mobile)
- **Nach Anlegen:** Navigation zur Customer Detail Page

### Task Preview:
- **3 Core Rules:** Willkommen (2 Tage), Angebot (7 Tage), Inaktivität (60 Tage)
- **Visual Feedback:** NEU-Badge für 24h, ÜBERFÄLLIG in rot
- **Toast mit Action:** "Aufgabe anzeigen" Button direkt in Erfolgsmeldung

### Quick Wins:
- **Keyboard Shortcuts:** Ctrl+N (Neuer Kunde), Ctrl+T (Task), Ctrl+K (Command Palette)
- **Smart Empty States:** Motivierende Messages statt leere Screens
- **Cockpit Teaser:** Mini-Dashboard mit Task-Stats

---

## 🚀 Killer Features für Sprint 2

### 1. **"Das System denkt mit" - Task Automation**
```typescript
// Automatisch nach Kundenanlage:
{
  title: "Neukunde Marriott Hotels begrüßen",
  dueDate: "In 2 Tagen",
  priority: "high",
  assignedTo: currentUser
}
```

### 2. **Mobile First für Außendienst**
- Swipeable Task Cards (rechts = erledigt, links = später)
- Touch-optimierte UI (min. 44px Targets)
- Offline-Ready Architecture

### 3. **Progressive Enhancement**
- Cockpit Teaser zeigt Vorgeschmack auf FC-011
- Basis für spätere KI-Features gelegt
- Performance Monitoring von Anfang an

---

## 📊 Metriken für Sprint Success

| Metrik | Ziel | Messung |
|--------|------|---------|
| Task Creation Time | < 2 Sek | Nach Customer Save |
| Empty → First Customer | < 3 Klicks | UX Test |
| Mobile Swipe Success | > 95% | Touch Events |
| Code Coverage | > 80% | Jest/Vitest |

---

## 🔄 Daily Stand-up Topics

### Tag 1 Fokus:
- CustomersPage Refactoring
- Empty State Implementation
- Quick Win: Keyboard Shortcuts

### Tag 2 Fokus:
- Task Engine Core
- API Integration
- Quick Win: Toast Actions

### Tag 3 Fokus:
- Mobile Optimization
- FC-012 Audit UI
- Performance Tuning

### Tag 3.5 Fokus:
- Testing & Bug Fixes
- Documentation
- Sprint Review Prep

---

## ⚡ Definition of Ready

- [x] UI Mockups abgestimmt
- [x] Task Rules definiert
- [x] API Contracts klar
- [x] Test-Strategie definiert
- [x] Quick Wins priorisiert

---

## 🎯 Definition of Done

- [ ] Feature funktioniert in Chrome, Firefox, Safari
- [ ] Mobile UI getestet auf iPhone & Android
- [ ] Unit Test Coverage > 80%
- [ ] E2E Tests für Happy Path
- [ ] Performance Budget eingehalten
- [ ] Dokumentation aktualisiert
- [ ] Code Review abgeschlossen

---

## 🔗 Verwandte Features

- **FC-011:** Cockpit Integration (Teaser schon in Sprint 2)
- **FC-009:** Contract Renewal (Task Rules vorbereitet)
- **FC-019:** Customer Profile Generator (Widget-System Foundation)

---

**Nächste Schritte:** 
1. **🚨 ZUERST:** [Critical Architecture Fix](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/CRITICAL_ARCHITECTURE_FIX_PLAN.md) lesen!
2. Git Branch checken: `git checkout feature/sprint-2-customer-ui-integration`
3. Dependencies prüfen: `cd frontend && npm install`
4. Los geht's mit Tag 1! 🚀

---

## 🔗 Weiterführende Links

**→ Weiter zu:** [Tag 1 Implementation](./DAY1_IMPLEMENTATION.md)  
**↑ Übergeordnet:** [FC-005 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md)  
**🚨 Kritisch:** [Architecture Fix Plan](./CRITICAL_ARCHITECTURE_FIX_PLAN.md)