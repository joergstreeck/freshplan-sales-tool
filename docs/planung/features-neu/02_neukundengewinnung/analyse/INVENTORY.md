# Frontend-Inventur: Modul 02 Neukundengewinnung

**Erstellt:** 2025-09-27
**Status:** Inventur abgeschlossen
**Zweck:** Stack-Analyse und Gap-Assessment für Lead-Management Frontend

## 📦 Stack & Infrastructure

### Core Technologies
```json
{
  "framework": "React 18.3.1",
  "bundler": "Vite 6.3.5",
  "typescript": "5.8.3",
  "routing": "react-router-dom 7.6.2",
  "state": "zustand 5.0.6 + @tanstack/react-query 5.80.6",
  "ui": "@mui/material 7.2.0",
  "forms": "react-hook-form 7.57.0 + zod 3.25.56",
  "testing": "vitest 3.2.2 + @playwright/test 1.52.0",
  "mocking": "msw 2.10.0"
}
```

### Environment Configuration
- **API_URL:** `http://localhost:8080` (Backend)
- **AUTH:** Keycloak-ready, aber `VITE_AUTH_BYPASS=true` in dev
- **MSW:** `VITE_USE_MSW=true` für Mock-Daten wenn Backend offline
- **Feature Flags:** Noch keine Lead-spezifischen Flags definiert

## 🗂️ Ordnerstruktur

```
frontend/src/
├── features/           # Feature-basierte Module
│   ├── cockpit/       # Dashboard-Components
│   ├── customers/     # Kundenverwaltung (API-Services vorhanden)
│   └── [FEHLT: leads/]  # Lead-Management noch nicht implementiert
├── pages/             # Route-Components
│   ├── NeukundengewinnungDashboard.tsx  # ✅ Existiert (Platzhalter-UI)
│   └── [FEHLT: Lead-spezifische Pages]
├── services/          # Globale API-Services
│   └── api.ts         # Basis API-Client (nur ping())
├── theme/             # Design System
│   └── freshfoodz.ts  # CI-konformes Theme (#94C456, #004F7B)
└── providers.tsx      # App-Provider mit Routes
```

## 🛣️ Routen & Navigation

### Aktuelle Routen (in providers.tsx)
```tsx
// Lazy-loaded Dashboard
const NeukundengewinnungDashboard = lazy(() =>
  import('./pages/NeukundengewinnungDashboard')
);

// Routes in Dashboard verlinkt:
'/neukundengewinnung/posteingang'  # ❌ Route fehlt
'/neukundengewinnung/leads'        # ❌ Route fehlt
'/neukundengewinnung/kampagnen'    # ❌ Route fehlt
```

### Layout-Integration
- **MainLayoutV2:** Wrapper mit Navigation (verwendet in Dashboard)
- **ProtectedRoute:** Auth-Guard (aber bypassed in dev)

## 🎨 Design System & Theme

### FreshFoodz CI (Theme V2)
```typescript
// Theme-Tokens (freshfoodz.ts)
palette: {
  primary: '#94C456',      // FreshFoodz Grün
  secondary: '#004F7B',    // Dunkelblau
  freshfoodz: {
    success: '#94C456',
    background: '#F5F5F5'
  }
}

// Typography
headlines: 'Antonio, sans-serif' (bold)
body: 'Poppins, sans-serif'
```

### Component Library
- **MUI v7:** Vollständige Material-UI Integration
- **Custom Components:** Button-Transition, Card-Transition
- **Icons:** @mui/icons-material + lucide-react

## 🔌 API Layer

### Vorhandene Services (customers/)
```typescript
// Wiederverwendbar für Leads:
- api-client.ts         # Fetch-Wrapper mit Auth
- customerApi.ts        # CRUD-Pattern (adaptierbar)
- fieldDefinitionApi.ts # Custom Fields (für Lead-Fields)
```

### Fehlende Lead-APIs
```typescript
// Zu implementieren:
GET  /api/leads/stats      # Dashboard KPIs
GET  /api/leads            # Lead-Liste mit Filter
GET  /api/leads/:id        # Lead-Details
POST /api/leads            # Lead erstellen
PATCH /api/leads/:id       # Lead aktualisieren

GET  /api/followups        # T+3/T+7 Follow-ups
POST /api/followups/:id/complete  # Follow-up abschließen

// Event-Subscription (SSE/WebSocket)
WS   /api/events/dashboard # Real-time Updates
```

## 🧪 Test-Setup

### Unit/Integration Tests
- **Vitest:** Konfiguriert mit Coverage-Reports
- **Testing Library:** React Testing eingerichtet
- **MSW:** Mock Service Worker für API-Mocks

### E2E Tests
- **Playwright:** Setup vorhanden, aber keine Lead-Tests
- **Test-Scripts:** `npm run test:e2e`

## 🎨 Theme v2 & Design Tokens

### Token-Quelle
```typescript
// frontend/src/theme/freshfoodz.ts
palette: {
  primary: { main: '#94C456' },      // FreshFoodz Grün
  secondary: { main: '#004F7B' },    // Dunkelblau
  freshfoodz: {
    primary: '#94C456',
    secondary: '#004F7B',
    success: '#94C456',
    background: '#F5F5F5'
  },
  status: {
    won: '#4CAF50',
    lost: '#F44336',
    probabilityHigh: '#94C456',
    probabilityMedium: '#FFA726',
    probabilityLow: '#EF5350'
  }
}

typography: {
  h1-h6: 'Antonio, sans-serif',  // Bold für Headlines
  body: 'Poppins, sans-serif'    // Regular für Text
}
```

### MUI → Tailwind Mapping (noch zu definieren)
```css
/* Benötigt: frontend/src/styles/theme-tokens.css */
:root {
  --fresh-primary: #94C456;
  --fresh-secondary: #004F7B;
  --fresh-success: #94C456;

  /* Spacing Scale */
  --spacing-xs: 0.25rem;  /* 4px */
  --spacing-sm: 0.5rem;   /* 8px */
  --spacing-md: 1rem;     /* 16px */
  --spacing-lg: 1.5rem;   /* 24px */
  --spacing-xl: 2rem;     /* 32px */

  /* Border Radius */
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
}
```

## 🗺️ Navigation Gating

### Feature-Flag Integration
```typescript
// Geplant: frontend/src/layouts/MainLayoutV2.tsx
const LEAD_FEATURES_ENABLED = import.meta.env.VITE_FEATURE_LEADGEN === 'true';

// Navigation Items konditional
const navItems = [
  { label: 'Cockpit', path: '/cockpit' },
  { label: 'Kunden', path: '/customers' },
  ...(LEAD_FEATURES_ENABLED ? [
    { label: 'Neukundengewinnung', path: '/neukundengewinnung' }
  ] : []),
];
```

### Route-Protection
```typescript
// Geplant: frontend/src/providers.tsx
{LEAD_FEATURES_ENABLED && (
  <Route path="/neukundengewinnung/*" element={<NeukundengewinnungRoutes />} />
)}
```

## 🔄 State Ownership

### Aufteilung der Verantwortlichkeiten
| State-Typ | Verwaltung | Zweck |
|-----------|------------|-------|
| **Server State** | React Query | API-Daten (Leads, Stats, Follow-ups) |
| **UI State** | Zustand | Filter, Sortierung, Modal-Zustände |
| **Form State** | React Hook Form | Eingabefelder, Validierung |
| **Auth State** | React Context | User, Rollen, Territory |
| **Feature Flags** | import.meta.env | Build-time Configuration |

### State-Sync Pattern
```typescript
// Server → UI State Sync
const { data: leads } = useQuery(['leads', filters]);
const setActiveLeadId = useLeadStore(s => s.setActiveLeadId);

useEffect(() => {
  if (leads?.[0]) setActiveLeadId(leads[0].id);
}, [leads]);
```

## ⚠️ Error Handling

### Error-Strategie nach Kontext
| Error-Typ | HTTP-Code | UI-Verhalten | User-Feedback |
|-----------|-----------|--------------|---------------|
| **Auth** | 401 | Redirect to Login | Toast + Redirect |
| **Permission** | 403 | Show Forbidden Message | Inline Alert |
| **Validation** | 422 | Highlight Fields | Field-level Errors |
| **Conflict** | 409 | Retry Dialog | Modal mit Options |
| **Server** | 5xx | Error Boundary | Fallback UI |
| **Network** | - | Retry with Backoff | Toast mit Retry |

### Error-Komponenten
```typescript
// Toast für temporäre Fehler
import { toast } from 'react-hot-toast';

// Inline für Formular-Fehler
<Alert severity="error">{error.message}</Alert>

// Error Boundary für kritische Fehler
<ErrorBoundary fallback={<ErrorFallback />}>
  <LeadManagement />
</ErrorBoundary>
```

## 🌍 Internationalization (i18n)

### Status
- **Aktuell:** Deutsch only, hardcoded Strings
- **i18n-Ready:** i18next installiert aber nicht konfiguriert
- **Plan:** Vorerst DE-only, später AT/CH Varianten

### Vorbereitung für spätere i18n
```typescript
// Statt: <Typography>Neukundengewinnung</Typography>
// Besser: <Typography>{t('leads.title')}</Typography>

// Aber für MVP: Hardcoded DE-Strings akzeptabel
```

## 🔴 Gaps & Fehlende Komponenten

### Critical Gaps
1. **Lead-Feature-Module fehlt komplett**
   - Keine `/features/leads/` Struktur
   - Keine Lead-spezifischen Hooks
   - Keine Lead-API-Services

2. **Routen nicht implementiert**
   - Dashboard verlinkt auf nicht-existente Pfade
   - Keine Page-Components für Leads/Inbox/Campaigns

3. **Real-time Integration fehlt**
   - Kein SSE/WebSocket-Setup
   - Keine Event-Subscription für Dashboard-Updates

4. **RBAC nicht implementiert**
   - useAuth() vorhanden, aber keine Role-Checks
   - Manager vs. User Views nicht differenziert

### Nice-to-have Gaps
- Keine Lead-spezifischen Feature-Flags
- Kein Polling-Mechanismus für Updates
- Keine Offline-Queue für Lead-Actions
- Keine Performance-Optimierungen (virtualized lists)

## 💡 Vorschläge & Next Steps

### 1. Minimal Viable Implementation (2-3 Tage)
```typescript
// Neue Struktur:
frontend/src/features/leads/
├── api/
│   ├── leadApi.ts         # CRUD + Stats
│   └── followupApi.ts     # Follow-up Management
├── components/
│   ├── LeadDashboard.tsx  # KPI-Tiles
│   ├── LeadList.tsx       # Tabelle mit Filter
│   └── FollowupInbox.tsx  # T+3/T+7 Queue
├── hooks/
│   ├── useLeadStats.ts    # Dashboard-Daten
│   └── useFollowups.ts    # Follow-up-Liste
└── types.ts               # Lead, FollowUp, Stats
```

### 2. Integration Pattern
```typescript
// React Query für Data-Fetching
const { data: stats } = useQuery({
  queryKey: ['lead-stats'],
  queryFn: () => leadApi.getStats(),
  refetchInterval: 20000, // Polling alle 20s
});

// Zustand für UI-State
const useLeadStore = create((set) => ({
  filter: { status: 'all' },
  setFilter: (filter) => set({ filter }),
}));
```

### 3. Feature-Flag Integration
```typescript
// .env.development
VITE_FEATURE_LEADGEN=true
VITE_FEATURE_LEADGEN_REALTIME=false

// In Component
if (!import.meta.env.VITE_FEATURE_LEADGEN) {
  return <ComingSoon />;
}
```

## 📊 Aufwandsschätzung

### Phase 1: Foundation (1 Tag)
- API-Services implementieren
- Basis-Komponenten (Dashboard, List)
- Mock-Daten mit MSW

### Phase 2: Integration (1 Tag)
- Routen verdrahten
- RBAC-Logic (Manager-View)
- Polling für Updates

### Phase 3: Polish (0.5 Tag)
- Error-Handling
- Loading-States
- Basic E2E-Test

**Gesamt: 2.5 Tage für MVP**

## ✅ Checkliste Research-Completeness

- [x] Stack erfasst
- [x] Ordnerstruktur analysiert
- [x] Routen identifiziert
- [x] Theme v2 Tokens dokumentiert
- [x] Navigation Gating geplant
- [x] State Ownership definiert
- [x] Error Handling Strategie
- [x] i18n Status notiert
- [x] API-Services bewertet
- [x] Gaps identifiziert