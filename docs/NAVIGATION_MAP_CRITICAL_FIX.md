# 🗺️ NAVIGATION MAP: Critical Architecture Fix

**Zweck:** Übersicht aller verlinkten Dokumente für den kritischen Architecture Fix

---

## 🚨 HAUPTDOKUMENTE (Start hier!)

### 1. Quick Fix Guide (Einstiegspunkt)
📍 **[/docs/CRITICAL_FIX_MAINLAYOUT_MISSING.md](/Users/joergstreeck/freshplan-sales-tool/docs/CRITICAL_FIX_MAINLAYOUT_MISSING.md)**
- Problem in 3 Sätzen
- Quick Fix Anleitung
- Verlinkt zu → Detaillierter Plan

### 2. Detaillierter Fix Plan (Implementierung)
📍 **[/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/CRITICAL_ARCHITECTURE_FIX_PLAN.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/CRITICAL_ARCHITECTURE_FIX_PLAN.md)**
- Vollständige Problemanalyse
- Code-Beispiele
- Schritt-für-Schritt Lösung
- Verlinkt zu → Sprint 2 Context

---

## 📚 KONTEXT-DOKUMENTE

### Sprint 2 Planning
- **[Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)** - Gesamt-Übersicht
- **[DAY1 Implementation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY1_IMPLEMENTATION.md)** - Tagesplanung
- **[Sidebar Discussion](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SIDEBAR_LEAD_DISCUSSION.md)** - UI/UX Entscheidungen

### Frontend Architecture
- **[Frontend README](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/README.md)** - Architektur-Übersicht
- **[Components Doc](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/01-components.md)** - Component Guidelines

### Code-Referenzen
- **[MainLayoutV2.tsx](/Users/joergstreeck/freshplan-sales-tool/frontend/src/components/layout/MainLayoutV2.tsx)** - Das Layout das ÜBERALL verwendet werden muss
- **[CustomersPageV2.tsx](/Users/joergstreeck/freshplan-sales-tool/frontend/src/pages/CustomersPageV2.tsx)** - Zu korrigierende Seite
- **[CockpitPage.tsx](/Users/joergstreeck/freshplan-sales-tool/frontend/src/pages/CockpitPage.tsx)** - Korrekte Implementierung als Beispiel

---

## 🔄 NAVIGATIONS-FLOW

```
1. START → CRITICAL_FIX_MAINLAYOUT_MISSING.md
   ↓
2. Details → CRITICAL_ARCHITECTURE_FIX_PLAN.md
   ↓
3. Context → Sprint 2 README.md
   ↓
4. Implementation → DAY1_IMPLEMENTATION.md
```

---

## 🎯 KERN-MESSAGE

**"ALLE Seiten MÜSSEN MainLayoutV2 verwenden!"**

- Sidebar darf NIEMALS verschwinden
- Wizards sind Modals, keine Routes
- Einheitliches Layout ist PFLICHT

---

## ⚡ QUICK LINKS FÜR NEUE CLAUDE SESSIONS

1. **Problem verstehen:** [CRITICAL FIX Guide](/Users/joergstreeck/freshplan-sales-tool/docs/CRITICAL_FIX_MAINLAYOUT_MISSING.md)
2. **Lösung implementieren:** [Architecture Fix Plan](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/CRITICAL_ARCHITECTURE_FIX_PLAN.md)
3. **Code-Beispiel ansehen:** [CockpitPage.tsx](/Users/joergstreeck/freshplan-sales-tool/frontend/src/pages/CockpitPage.tsx)

---

**Zeitaufwand:** 2-3 Stunden für kompletten Fix