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

1. **Verkaufsfokussierte Wizard-Struktur** - Angebotsstruktur → Bedarf → Potenzial 🆕
2. **Standort-basierte Datenstruktur** - Customer → Location → Contact 🆕
3. **Pain Point → Solution Mapping** - Jedes Problem wird zur Verkaufschance 🆕
4. **Automatische Potenzialberechnung** - Motivation auf einen Blick 🆕
5. **Field Theme System** - Intelligente Feldgrößen-Berechnung ✅
6. **Task Preview MVP** - "Das System denkt mit" erlebbar machen 🔄

---

### 🆕 Adaptive Layout Dokumente:
- **Konzept & Evolution:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/ADAPTIVE_LAYOUT_EVOLUTION.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/ADAPTIVE_LAYOUT_EVOLUTION.md)
- **NEU:** [Dropdown Auto-Width Plan](./DROPDOWN_AUTO_WIDTH_PLAN.md) - Automatische Breitenanpassung für Dropdowns 🔧
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

### 🆕 Neue Struktur (nach Diskussion 30.07.2025):

#### Wizard-Struktur:
- **AKTUELL:** [Wizard Struktur V2](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/WIZARD_STRUCTURE_V2.md) - Verkaufsfokussiert!
- **NEU:** [Wizard Adaptive Integration](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/WIZARD_ADAPTIVE_INTEGRATION.md) - Theme-System bleibt!
- [Step 1: Basis & Filialstruktur](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP1_BASIS_FILIALSTRUKTUR.md)
- [Step 2: Angebot & Pain Points](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP2_ANGEBOT_PAINPOINTS.md)
- [Step 3: Ansprechpartner](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP3_ANSPRECHPARTNER.md)

#### Backend-Integration:
- **NEU:** [Backend Requirements](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/backend/BACKEND_REQUIREMENTS.md) - Entity-Erweiterungen
- **NEU:** [API Endpoints](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/backend/API_ENDPOINTS.md) - REST-Schnittstellen
- **NEU:** [Entity Extensions](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/backend/ENTITY_EXTENSIONS.md) - Java-Entities

#### Field Catalog (vollständig):
- **NEU:** [Field Catalog Complete](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_COMPLETE.md) - Step 1 Felder
- **NEU:** [Field Catalog Services](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_SERVICES.md) - Step 2 Felder
- **NEU:** [Field Catalog Contacts](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_CONTACTS.md) - Step 3 Felder
- **NEU:** [Adaptive Behavior Spec](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/ADAPTIVE_BEHAVIOR_SPEC.md) - Flexible Feldgrößen
- [Field Catalog Extension](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_EXTENSION.md) - Übersicht

#### Implementierung:
- [Potenzialberechnung](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/POTENTIAL_CALCULATION.md) - Erfahrungswerte
- [Pain Point Mapping](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/PAIN_POINT_MAPPING.md) - Freshfoodz-Lösungen
- [Vending/bonPeti Integration](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/VENDING_BONPETI_INTEGRATION.md) - Zusatzgeschäft

#### Archivierte Dokumente:
- [Sprint 2 Key Decisions](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SPRINT2_KEY_DECISIONS_SUMMARY.md)

---

## ✅ Was wurde entschieden? (UPDATE 30.07.2025)

### 🆕 Neue Kern-Entscheidungen:
- **Standort-basierte Struktur:** Jeder Kunde hat mind. 1 Standort
- **Ansprechpartner strukturiert:** Anrede, Titel, Vor-/Nachname getrennt
- **Angebotsstruktur = Bedarfsindikator:** Nicht "was habt ihr", sondern "was bietet ihr an"
- **Pain Points = Verkaufschancen:** Jeder Pain Point hat eine Freshfoodz-Lösung
- **80%-MVP-Modell:** Nur verkaufsrelevante Felder

### Wizard-Struktur NEU:
- **Step 1:** Basis + Filialstruktur + Geschäftsmodell + Adresse
- **Step 2:** Angebotsstruktur + Pain Points + Live-Potenzial
- **Step 3:** Ansprechpartner strukturiert + Beziehungsaufbau

### Freshfoodz-Spezifika integriert:
- **Cook&Fresh®:** 40 Tage Haltbarkeit, 15 Min Regenerierung
- **bonPeti:** Vending/Automaten-Checkbox
- **Berlin-Produktion:** 640 Mitarbeiter, 250.000 Portionen täglich

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
1. **📊 NEUE ROADMAP:** [Implementation Roadmap](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/IMPLEMENTATION_ROADMAP.md) - Tag-für-Tag Plan!
2. **📦 START MIT:** [Field Catalog Extension](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_EXTENSION.md) - TODO-8
3. Git Branch checken: `git checkout feature/sprint-2-customer-ui-integration`
4. Los geht's mit der neuen Struktur! 🚀

---

## 🔗 Weiterführende Links

**→ Weiter zu:** [Tag 1 Implementation](./DAY1_IMPLEMENTATION.md)  
**↑ Übergeordnet:** [FC-005 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md)  
**🚨 Kritisch:** [Architecture Fix Plan](./CRITICAL_ARCHITECTURE_FIX_PLAN.md)

### 📋 Sprint 2 Diskussionen & Ergebnisse:
- **WICHTIG:** [Customer Structure Redesign](./discussions/2025-07-30_CUSTOMER_STRUCTURE_REDESIGN.md) - Komplette Neuausrichtung auf Verkaufsfokus!
  - Angebotsstruktur als Bedarfsindikator
  - Pain Points als Verkaufshebel  
  - Filialstruktur für Skalierung
  - Freshfoodz-spezifische Lösungen
- **NEU:** [Diskussion Ergebnisse Kompakt](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DISKUSSION_ERGEBNISSE_KOMPAKT.md) - Alle 10 Kernergebnisse auf einen Blick!