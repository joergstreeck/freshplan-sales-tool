---
module: "02_neukundengewinnung"
domain: "frontend"
doc_type: "guideline"
status: "approved"
owner: "team/leads"
updated: "2025-09-27"
---

# 🎨 Frontend – Modul 02 Neukundengewinnung

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Frontend

**Status:** ✅ IMPLEMENTATION COMPLETE (Sprint 2.1.3) → Production Ready

## 🎯 Executive Summary

React-basiertes Lead-Management Frontend mit Real-time Dashboard, RBAC-Sichtbarkeiten und Event-getriebenem Update-System. ~~Vollständige Research abgeschlossen, bereit für Thin Vertical Slice Implementation.~~ **✅ Sprint 2.1.3 COMPLETE: Lead Management MVP produktiv auf main Branch (PR #122).**

## 🚀 Sprint 2.1.3 Implementation (COMPLETE)

### **Umgesetzte Features:**
- ✅ `/leads` Route mit `LeadList` und `LeadCreateDialog`
- ✅ Vollständige Business-Logik (Validierung, Duplikate, Source-Tracking)
- ✅ RFC7807 Error Handling mit Feld-spezifischen Fehlern
- ✅ i18n komplett (de/en) ohne hardcoded Strings
- ✅ MSW für realistische API-Simulation
- ✅ 90% Test-Coverage (Integration Tests)
- ✅ MainLayoutV2 Integration (konsistente Navigation)

### **Technische Highlights:**
- **Duplikat-Erkennung:** 409 Response mit Warning-Alert + Field-Error
- **Normalisierung:** E-Mail lowercase + trim für Vergleiche
- **Source-Tracking:** Alle Leads mit `source='manual'` markiert
- **Environment:** `VITE_FEATURE_LEADGEN=true` aktiviert Feature

### **Qualität:**
- CI/CD komplett grün (Lint, Prettier, Tests)
- Copilot-Review Findings behoben
- Keine Konsolenfehler (MSW-Ping entfernt)

## 🧭 Frontend Blueprint (Sprint 2.1.3 Ready)

### **Routen**
- `/leads` (V1): Liste + Create-Dialog (**Flag** `VITE_FEATURE_LEADGEN`)
- `/leads/:id` (V2): Detail/Statuswechsel (Backlog)

### **Datenflüsse**
- GET `/api/leads` → `LeadList`
- POST `/api/leads` → `LeadCreateDialog`
- Fehleradapter: RFC7807 → { title, detail, fieldErrors? }

### **State/Architektur**
- Query‑Layer: einfacher Fetch‑Client (später Query‑Lib möglich)
- UI‑State: lokaler Zustand (Dialog), leichte Store‑Schnittstelle vorbereiten
- Realtime: Hook‑Platzhalter (später WebSocket)

### **Theme/UX**
- MUI Theme V2 Tokens
- Leere Zustände, Skeletons, zugängliche Fehlermeldungen (WCAG 2.1 AA)

### **Testing**
- Vitest (Komponenten/Adapter), Playwright (Smoke)
- Coverage ≥ 80% für neue Artefakte

### **Telemetrie (später)**
- Page‑Load, API‑Latenzen, Fehlerquote

### **Backlog (geordnet)**
1) Filter/Suche, Paging
2) Statuswechsel‑UI (State Machine)
3) Realtime (WS)
4) Export/CSV, Column‑Customizer

## 📚 **Research-Dokumentation (Sprint 2.1.2)**

### **Vollständige Analyse:**
- **[INVENTORY.md](./analyse/INVENTORY.md)** – Stack-Analyse (React/Vite/TanStack Query/MUI), Routen, Theme v2, State-Management, Gaps
- **[API_CONTRACT.md](./analyse/API_CONTRACT.md)** – Event-System, RBAC-Matrix, REST-Endpoints, Polling-Strategy, Error-Handling
- **[RESEARCH_ANSWERS.md](./analyse/RESEARCH_ANSWERS.md)** – 11 offene Implementation-Fragen beantwortet
- **[VALIDATED_FOUNDATION_PATTERNS.md](./analyse/VALIDATED_FOUNDATION_PATTERNS.md)** – Corporate Identity, TypeScript-Patterns, Bundle-Optimization, Testing-Standards

## 🏗️ **Tech Stack (validiert)**

### **Core Framework:**
```yaml
Framework: React 18.3.1 + TypeScript 5.8.3
Bundler: Vite 6.3.5 (Target: <200KB Bundle)
Routing: react-router-dom 7.6.2
State: Zustand 5.0.6 + @tanstack/react-query 5.80.6
UI: @mui/material 7.2.0 (Theme v2: #94C456, #004F7B)
Testing: Vitest 3.2.2 + @playwright/test 1.52.0
```

### **Corporate Identity (verbindlich):**
```css
--color-primary: #94C456;     /* FreshFoodz Grün */
--color-secondary: #004F7B;   /* Dunkelblau */
--font-headline: 'Antonio', sans-serif;  /* Bold Headlines */
--font-body: 'Poppins', sans-serif;      /* Body Text */
```

## 🎯 **Planned Implementation (Thin Vertical Slice)**

### **MVP Scope:**
```yaml
Dashboard (/neukundengewinnung):
  - KPI-Tiles: Pending T3, Pending T7, Today Processed
  - Activity Feed: Letzte 10 Follow-ups
  - RBAC: Manager-View mit Team-Aggregationen

Lead-Liste (/neukundengewinnung/leads):
  - Tabelle mit Filter (Status, Territory, Textsuche)
  - Pagination + Sorting
  - Export (Manager only)

Follow-up Inbox (/neukundengewinnung/inbox):
  - Meine Follow-ups (T3/T7) sortiert nach Fälligkeit
  - Complete-Action mit Outcome
  - Overdue-Markierung

Campaigns (/neukundengewinnung/kampagnen):
  - Placeholder + "Bald verfügbar"
```

### **Real-time Strategy:**
```typescript
// Phase 1: Polling (20s Interval)
useQuery({
  queryKey: ['lead-stats'],
  queryFn: leadApi.getStats,
  refetchInterval: 20000,
  refetchIntervalInBackground: false
});

// Phase 2: SSE (Future)
const eventSource = new EventSource('/api/events/stream');
```

## 🔐 **RBAC Implementation**

### **Sichtbarkeitsmatrix:**
| UI-Element | SALES | MANAGER | ADMIN |
|------------|-------|---------|-------|
| Eigene KPIs | ✅ | ✅ | ✅ |
| Team-Aggregationen | ❌ | ✅ | ✅ |
| Export-Funktionen | ❌ | ✅ | ✅ |
| Reassign-Actions | ❌ | ✅ | ✅ |

```typescript
// Implementation Pattern
const { hasRole } = useAuth();
const canViewTeamStats = hasRole(['MANAGER', 'ADMIN']);
```

## 🧪 **Testing Strategy**

### **Coverage Targets:**
- **Unit Tests:** 70% (Business Logic)
- **Integration:** 20% (API Layer)
- **E2E:** 10% (Critical Paths)
- **Total:** ≥80% (CI enforced)

### **Test Stack:**
```typescript
// Vitest + React Testing Library
describe('LeadDashboard', () => {
  it('should display pending T3 count', async () => {
    render(<LeadDashboard />);
    await waitFor(() => {
      expect(getByTestId('t3-pending')).toHaveTextContent('12');
    });
  });
});

// MSW für API Mocks
http.get('/api/leads/stats', () => {
  return HttpResponse.json({ pendingT3: 12, pendingT7: 8 });
});
```

## 🚀 **Next Steps (Implementation)**

### **Feature-Flag Strategy:**
```bash
# Environment
VITE_FEATURE_LEADGEN=true  # Default: false

# Component Gating
if (!import.meta.env.VITE_FEATURE_LEADGEN) {
  return <ComingSoon />;
}
```

### **Implementation Timeline:**
1. **Branch:** `feature/fe-vertical-slice-02-leads`
2. **PR Dependencies:** PR #112 (Research) merged
3. **DoD:** ≥80% Test Coverage + Bundle <200KB + E2E Smoke Test
4. **No Go-Live:** Feature-Flag bleibt `false` auf main

## 🔗 **Dependencies & Contracts**

- **Backend APIs:** Siehe [API_CONTRACT.md](./analyse/API_CONTRACT.md)
- **Event-System:** Aligned mit Backend (Envelope v2, Channels)
- **Design System:** [DESIGN_SYSTEM.md](../../../grundlagen/DESIGN_SYSTEM.md)
- **Bundle Optimization:** [Frontend Bundle Plan](../../../features-neu/00_infrastruktur/leistung/implementation-plans/01_FRONTEND_BUNDLE_OPTIMIZATION_PLAN.md)