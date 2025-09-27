---
module: "02_neukundengewinnung"
domain: "frontend"
doc_type: "guideline"
sprint: "2.1.2"
status: "approved"
owner: "team/leads"
updated: "2025-09-27"
---

# 🔍 Validated Foundation Patterns für Modul 02 Frontend

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Frontend → Analyse → VALIDATED_FOUNDATION_PATTERNS

**Sprint:** 2.1.2 – Frontend Research
**Erstellt:** 2025-09-27
**Status:** Konsolidiert aus Grundlagen + Infrastruktur
**Zweck:** Verbindliche Patterns für Lead-Management Frontend Implementation

## 🎨 FreshFoodz Corporate Identity (VERBINDLICH)

### Farben (aus DESIGN_SYSTEM.md)
```css
:root {
  /* NICHT ÄNDERN - CI Vorgaben */
  --color-primary: #94C456;     /* Primärgrün */
  --color-secondary: #004F7B;   /* Dunkelblau */
  --color-white: #FFFFFF;
  --color-black: #000000;

  /* Hover-Varianten (WCAG AA) */
  --color-primary-hover: #7BA945;
  --color-secondary-hover: #003A5C;

  /* Status-Farben */
  --color-success: var(--color-primary);
  --color-error: #DC3545;
  --color-warning: #FFC107;
  --color-disabled: #CCCCCC;
}
```

### Typography (PFLICHT)
```css
/* Google Fonts - Performance-optimiert */
@import url('https://fonts.googleapis.com/css2?family=Antonio:wght@700&family=Poppins:wght@400;500&display=swap');

.font-headline {
  font-family: 'Antonio', sans-serif;
  font-weight: 700;
}

.font-body {
  font-family: 'Poppins', sans-serif;
  font-weight: 400;
}
```

## 💻 TypeScript Import Patterns (KRITISCH)

### Aus CODING_STANDARDS.md - Vite verbatimModuleSyntax
```typescript
// ✅ RICHTIG - Type Imports
import type { FieldDefinition, FieldCatalog } from './types';

// ✅ RICHTIG - Direkte Type Exports
export interface LeadData { ... }
export type LeadStatus = 'REGISTERED' | 'QUALIFIED';

// ❌ FALSCH - Re-Exports vermeiden
type Foo = { ... };
export { Foo };  // Build-Fehler!

// ❌ FALSCH - Normale Imports für Types
import { LeadData } from './types';  // Runtime-Fehler!
```

## 📦 Bundle Optimization (aus 00_infrastruktur)

### Target Metrics
- **Total Bundle:** <200KB (aktuell 750KB)
- **App Shell:** <100KB für Mobile Field-Sales
- **Module Chunks:** Max 120KB pro Modul
- **CSS:** Max 80KB

### Vite Configuration Pattern
```typescript
// vite.config.ts - Module-based Splitting
export default defineConfig({
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          // Module 02 - Lead Management
          if (id.includes('features/leads')) {
            return 'module-leads';
          }
          // MUI Optimierung
          if (id.includes('@mui/material')) {
            return 'vendor-mui';
          }
        }
      }
    }
  }
});
```

### CI Budget Enforcement
```json
// package.json
{
  "size-limit": [
    { "path": "dist/assets/module-leads.*.js", "limit": "120 KB" },
    { "path": "dist/assets/vendor.*.js", "limit": "180 KB" }
  ]
}
```

## 🔔 Event Patterns (CloudEvents 1.0)

### Envelope Format (aus ADR-0002 + Event-Schema Plan)
```typescript
interface EventEnvelope {
  id: string;                    // UUID v4
  source: string;                // "lead-management"
  type: string;                   // "dashboard.lead_status_changed"
  time: string;                   // UTC ISO-8601
  idempotencyKey: string;        // Deterministic UUID v5
  data: unknown;                 // Event payload
}

// Idempotenz-Key generieren (Backend-Pattern)
const key = UUID.nameUUIDFromBytes(
  `${leadId}|${type}|${timestamp}`.getBytes()
);
```

### Frontend Event Handling
```typescript
// Polling v1 (SSE später)
const POLL_INTERVAL = 20000; // 20s

useQuery({
  queryKey: ['lead-stats'],
  queryFn: leadApi.getStats,
  refetchInterval: POLL_INTERVAL,
  refetchIntervalInBackground: false,
  refetchOnWindowFocus: true
});

// Truncation Handling
if (event.data.truncated) {
  const fullData = await leadApi.getEvent(event.data.reference);
}
```

## 🧪 Testing Standards (aus TESTING_GUIDE.md)

### Coverage Targets
- **Unit Tests:** 70% der Business Logic
- **Integration:** 20% der API Layer
- **E2E:** 10% Critical Paths
- **Total Coverage:** ≥80% (CI enforced)

### Frontend Test Stack
```typescript
// Vitest + React Testing Library
describe('LeadDashboard', () => {
  it('should display pending T3 count', async () => {
    const { getByTestId } = render(<LeadDashboard />);
    await waitFor(() => {
      expect(getByTestId('t3-pending')).toHaveTextContent('12');
    });
  });
});

// MSW für API Mocks
http.get('/api/leads/stats', () => {
  return HttpResponse.json({
    pendingT3: 12,
    pendingT7: 8
  });
});
```

## 🏗️ Module Architecture Pattern

### Aus MODULE_INTEGRATION_MAP.md
```
frontend/src/features/leads/
├── api/                    # API Layer
│   ├── leadApi.ts         # REST calls
│   └── types.ts           # API types
├── components/            # UI Components
│   ├── LeadDashboard.tsx
│   ├── LeadList.tsx
│   └── FollowupInbox.tsx
├── hooks/                 # Custom Hooks
│   ├── useLeadStats.ts
│   └── useFollowups.ts
├── store/                 # Zustand Store
│   └── leadStore.ts
└── types.ts              # Domain types
```

### Integration Pattern
```typescript
// Event-Driven (bevorzugt)
const handleLeadUpdate = (event: EventEnvelope) => {
  if (event.type === 'dashboard.lead_status_changed') {
    queryClient.invalidateQueries(['leads']);
  }
};

// API-Driven (Fallback)
const lead = await leadApi.getLead(id);
```

## ⚠️ Critical Constraints

### Aus aktueller Analyse:
1. **NO Territory Protection** - Leads sind deutschland-weit verfügbar
2. **RBAC Required** - Manager vs. Sales Views unterscheiden
3. **Mobile First** - Field Sales Performance kritisch
4. **DE-only v1** - Keine i18n Komplexität initial
5. **Feature Flag** - `VITE_FEATURE_LEADGEN` für Safe Rollout

## ✅ Validation Checklist

- [x] CI Farben aus DESIGN_SYSTEM.md validiert
- [x] Typography verbindlich (Antonio + Poppins)
- [x] TypeScript Patterns für Vite confirmed
- [x] Bundle Size Targets definiert (<200KB)
- [x] Event Envelope CloudEvents 1.0 compliant
- [x] Testing Standards ≥80% Coverage
- [x] Module Structure pattern klar
- [x] Mobile Performance Constraints beachtet

## 🚀 Next Steps

Diese Patterns sind verbindlich für die Implementation des Thin Vertical Slice:
1. Theme Setup mit exakten CI-Farben
2. Bundle Configuration mit Size Limits
3. Event Polling mit 20s Interval
4. Test Setup mit MSW + Vitest
5. RBAC-aware Components