# 📋 Sprint 2 - Zentrale Entscheidungen Zusammenfassung

**Sprint:** Sprint 2  
**Datum:** 27.07.2025  
**Status:** ✅ Finalisiert

---

## 📋 Navigation
**Parent:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  
**Previous:** [Sidebar Navigation Config](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SIDEBAR_NAVIGATION_CONFIG.md)  
**Related Documents:**
- [Sidebar Discussion](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SIDEBAR_LEAD_DISCUSSION.md)
- [Lead-Kunde-Trennung](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/LEAD_CUSTOMER_SEPARATION_DECISION.md)
- [Sidebar Config](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SIDEBAR_NAVIGATION_CONFIG.md)
- [FC-020 Lead Management](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-020-LEAD-MANAGEMENT_TECH_CONCEPT.md)

---

## 🎯 Die 4 Kernentscheidungen für Sprint 2

### 1. **Lead-Kunde-Trennung (Architektur)**
- **Entscheidung:** Lead Management wird NICHT Teil von FC-005
- **Stattdessen:** Eigenständiges Feature FC-020 in Phase 2
- **Begründung:** Bounded Contexts, unterschiedliche Domänen
- **Details:** [Lead-Kunde-Trennung](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/LEAD_CUSTOMER_SEPARATION_DECISION.md)

### 2. **Kundenerfassung UI (Beide Wege!)**
**WICHTIG:** Wir implementieren BEIDE Varianten - sie ergänzen sich!

#### Sidebar-Navigation:
- Position: "3. Kundenmanagement → 3.2 Neuer Kunde"
- Zielgruppe: Nutzer die über Navigation gehen
- Standard CRM-Pattern

#### Header Quick-Action (ZUSÄTZLICH!):
- Button: [+ Neuer Kunde] prominent im Header
- Zielgruppe: Power-User, häufige Nutzung
- Keyboard Shortcut: Ctrl+N
- **"Die Sidebar übersichtlich halten UND Power-User bedienen"**

**Details:** [Sidebar Navigation Config](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SIDEBAR_NAVIGATION_CONFIG.md)

### 3. **CustomerOnboardingWizard Vorbereitung**
```typescript
interface CustomerOnboardingWizardProps {
  open: boolean;
  onClose: () => void;
  // Für Phase 2 vorbereitet:
  initialData?: Partial<CustomerFormData>;     // Sprint 2: undefined
  source?: 'direct' | 'lead_conversion' | 'import';  // Sprint 2: 'direct'
  onComplete: (data: CustomerFormData) => void;
}
```

**Warum jetzt schon?** 
- Keine Breaking Changes in Phase 2
- Lead-Konvertierung kann einfach "eingepluggt" werden
- Props sind da, werden nur noch nicht genutzt

### 4. **TypeScript Import Type Strategie**
```typescript
// ✅ IMMER bei Types/Interfaces:
import type { FieldDefinition, Customer } from './types';

// ❌ NIEMALS normale Imports für Types:
import { FieldDefinition } from './types';  // Build-Fehler!
```

**Gilt für:** ALLE Sprint 2 Implementierungen!  
**Details:** [TypeScript Import Type Guide](/Users/joergstreeck/freshplan-sales-tool/docs/guides/TYPESCRIPT_IMPORT_TYPE_GUIDE.md)

---

## 💡 Die Philosophie dahinter

### "Klare Trennung in der Kommunikation" (Jörg):
- **Customer Management** = Bestandskundenverwaltung
- **Lead Management** = Kommt in Phase 2 (FC-020)
- Architektur ist vorbereitet, aber alles bleibt wartbar, fokussiert, entkoppelt

### "Beide Varianten ergänzen sich" (Claude):
- Sidebar für strukturierte Navigation
- Header-Button für Produktivität
- Kein Entweder-Oder, sondern komplementär

### "Zukunftssicher ohne Overhead":
- Sprint 2 bleibt schlank (keine Lead-Logik)
- Aber: Props sind vorbereitet
- Phase 2 kann nahtlos andocken

---

## ✅ Sprint 2 Implementierungs-Checkliste

- [ ] Sidebar: "3.2 Neuer Kunde" unter Kundenmanagement
- [ ] Header: [+ Neuer Kunde] Button in CustomerListHeader
- [ ] Keyboard: Ctrl+N Shortcut funktioniert
- [ ] Wizard: initialData & source Props vorhanden (aber undefined/'direct')
- [ ] Lead-Erfassung: Als disabled Menüpunkt sichtbar
- [ ] TypeScript: Alle Type-Imports mit `import type`

---

**Zusammengefasst:** Sprint 2 implementiert direkte Kundenanlage mit optimaler UX (Sidebar + Header), bereitet die Architektur für Lead-Konvertierung vor, aber bleibt fokussiert und schlank!