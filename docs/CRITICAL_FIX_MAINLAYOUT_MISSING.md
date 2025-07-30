# 🚨 KRITISCHER FIX: MainLayoutV2 fehlt auf allen Seiten

**SOFORT-MASSNAHME ERFORDERLICH!**

---

## 📍 Navigation
**← Detaillierter Plan:** [CRITICAL ARCHITECTURE FIX PLAN](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/CRITICAL_ARCHITECTURE_FIX_PLAN.md)  
**→ Sprint 2 Context:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  
**↑ Master Plan:** [CRM V5 Master Plan](/Users/joergstreeck/freshplan-sales-tool/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)  
**📚 Arbeitsrichtlinien:** [CLAUDE.md](/Users/joergstreeck/freshplan-sales-tool/CLAUDE.md)

---

## 🔥 DAS PROBLEM IN 3 SÄTZEN

1. **Sidebar verschwindet** bei "Neuer Kunde" - Nutzer sind gefangen
2. **Kein einheitliches Layout** - Jede Seite macht ihr eigenes Ding  
3. **Navigation kaputt** - Nutzer müssen Browser-Back verwenden

## 📸 BEWEIS (Screenshots von Jörg)

- **Bild 1:** Kundenliste MIT Sidebar ✅
- **Bild 2:** "Neuer Kunde" OHNE Sidebar ❌ + redundante Kundenliste oben

## 🎯 DIE LÖSUNG

### Falsch (Aktuell):
```typescript
// ❌ CustomersPageV2 - OHNE Layout
export function CustomersPageV2() {
  return <Box>...</Box>;
}
```

### Richtig (Muss so sein):
```typescript
// ✅ CustomersPageV2 - MIT MainLayoutV2
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';

export function CustomersPageV2() {
  return (
    <MainLayoutV2>
      {/* Content */}
    </MainLayoutV2>
  );
}
```

## 📋 QUICK FIX ANLEITUNG

### 1. CustomersPageV2.tsx anpassen:
```bash
cd frontend/src/pages
# Öffne CustomersPageV2.tsx
# Wrappe ALLES in <MainLayoutV2>
```

### 2. Wizard als Modal (nicht Route):
```bash
# LÖSCHE: <Route path="/customers/new" ...>
# ERSTELLE: CustomerOnboardingWizardModal.tsx
```

### 3. Check alle anderen Seiten:
```bash
grep -r "export function" --include="*.tsx" pages/
# Jede Seite MUSS MainLayoutV2 haben!
```

## ⚡ WARUM IST DAS KRITISCH?

- **User verlieren Orientierung** → Abbruchrate ↑
- **Inkonsistente UX** → Vertrauen ↓  
- **Widerspricht eigener Doku** → Glaubwürdigkeit ↓

## 🚀 NÄCHSTE SCHRITTE

1. **JETZT:** CustomersPageV2 fixen (30 Min)
2. **HEUTE:** Alle Seiten prüfen (1h)
3. **MORGEN:** ESLint Rule für MainLayoutV2

## 📖 DETAILS

Vollständiger Plan: [CRITICAL ARCHITECTURE FIX PLAN](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/CRITICAL_ARCHITECTURE_FIX_PLAN.md)

---

**⏰ Time to Fix: 2-3 Stunden**  
**🎯 Impact: ALLE Nutzer betroffen**  
**🔴 Priorität: KRITISCH**

---

## 🔗 Weiterführende Links

### Implementation:
- **→ Detaillierter Fix-Plan:** [CRITICAL ARCHITECTURE FIX PLAN](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/CRITICAL_ARCHITECTURE_FIX_PLAN.md)
- **→ Sprint 2 Kontext:** [DAY1 IMPLEMENTATION](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY1_IMPLEMENTATION.md)
- **→ Sidebar Diskussion:** [SIDEBAR LEAD DISCUSSION](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SIDEBAR_LEAD_DISCUSSION.md)

### Architektur:
- **→ Frontend Docs:** [Frontend Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/README.md)
- **→ Component Guide:** [Components Documentation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/01-components.md)
- **→ MainLayoutV2 Code:** [MainLayoutV2.tsx](/Users/joergstreeck/freshplan-sales-tool/frontend/src/components/layout/MainLayoutV2.tsx)

### Übergeordnet:
- **↑ FC-005 Overview:** [Customer Management Feature](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md)
- **↑ Master Plan:** [CRM V5 Complete](/Users/joergstreeck/freshplan-sales-tool/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)
- **↑ Arbeitsrichtlinien:** [CLAUDE.md](/Users/joergstreeck/freshplan-sales-tool/CLAUDE.md)