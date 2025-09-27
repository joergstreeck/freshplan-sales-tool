---
module: "02_neukundengewinnung"
domain: "frontend"
doc_type: "analyse"
sprint: "2.1.2"
status: "approved"
owner: "team/leads"
updated: "2025-09-27"
---

# Research Answers: Modul 02 Frontend

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Frontend → Analyse → RESEARCH_ANSWERS

**Sprint:** 2.1.2 – Frontend Research
**Erstellt:** 2025-09-27
**Zweck:** Antworten auf 11 offene Fragen vor Implementation

## 1. "Pending T3/T7" Definition

**Antwort:**
- **Zeitbasis:** Backend speichert UTC (Instant), Frontend zeigt User-Timezone (default: `Europe/Berlin`)
- **Pending:** `dueAt <= NOW()` UND `status = 'PENDING'`
- **Überfällig:** `dueAt < NOW() - 24h` werden mit Label "overdue" markiert
- **Wochenenden:** T3/T7 überspringen Wochenenden automatisch (nextMonday)
- **Feiertage:** v1 keine Feiertage, v2 optional via Settings

```typescript
// Display-Logik
const formatDueDate = (dueAt: Date) => {
  if (isToday(dueAt)) return "Heute 14:00";
  if (isYesterday(dueAt)) return "Gestern (überfällig)";
  if (differenceInDays(now, dueAt) > 1) return `Vor ${days} Tagen (überfällig)`;
  if (isTomorrow(dueAt)) return "Morgen";
  return format(dueAt, 'dd.MM. HH:mm');
};
```

## 2. RBAC-Anzeige für Manager

**Manager sehen zusätzlich:**

| UI-Element | SALES | MANAGER | Beschreibung |
|------------|-------|---------|--------------|
| Team-KPIs | ❌ | ✅ | Aggregierte Team-Metriken |
| Territory Stats | ❌ | ✅ | DE/AT/CH Aufschlüsselung |
| Pipeline Value | ❌ | ✅ | Monetäre Werte (€/CHF) |
| Team-Lead-Liste | ❌ | ✅ | Alle Leads im Team |
| Export-Button | ❌ | ✅ | CSV/Excel Export |
| Reassign-Action | ❌ | ✅ | Follow-ups neu zuweisen |
| Conversion Funnel | ❌ | ✅ | Lead → Customer Flow |

## 3. Polling v1 Strategie

**Implementierung:**
- **Intervall:** 20 Sekunden (konfigurierbar via ENV)
- **Backoff:** Bei 3 Fehlern → 60s, dann exponentiell
- **Network-Error:** Toast mit Retry-Counter
- **Tab-Inaktiv:** Polling pausieren, bei Focus reaktivieren

```typescript
const POLL_INTERVAL = import.meta.env.VITE_POLL_INTERVAL || 20000;

useQuery({
  queryKey: ['lead-stats'],
  queryFn: leadApi.getStats,
  refetchInterval: POLL_INTERVAL,
  refetchIntervalInBackground: false,
  refetchOnWindowFocus: true,
  retry: 3,
  retryDelay: attemptIndex => Math.min(1000 * 2 ** attemptIndex, 30000)
});
```

## 4. Follow-up Activity Datensatz

**Minimale Felder:**
```typescript
interface FollowUpActivity {
  id: string;
  leadId: string;
  leadCompanyName: string;
  followUpType: 'T3' | 'T7' | 'CUSTOM';
  processedAt: string;  // ISO-8601
  processedBy: { id: string; name: string };
  outcome: 'SUCCESS' | 'NO_ANSWER' | 'POSTPONED';
  nextAction?: string;
}
```

**Sortierung:**
- Default: `processedAt DESC` (neueste zuerst)
- Optional: Nach Type, Outcome, User

## 5. Leads-Liste Filter v1

**Pflicht-Filter:**
- **Status:** REGISTERED | QUALIFIED | CONTACTED | OPPORTUNITY | LOST
- **Territory:** DE-BY | DE-BW | AT | CH (nur Manager)
- **Textsuche:** Firmenname (Volltext)
- **Eigene/Team:** Toggle (Manager only)

**Nice-to-have v2:**
- Owner (User-Dropdown)
- Datum-Range (createdAt)
- Has-Follow-up (Boolean)
- Tags/Labels

## 6. Timezone/Formatierung

**Strategie:**
- Backend liefert **immer UTC** (Instant/ISO-8601)
- Frontend konvertiert zu User-Timezone
- Default: `Europe/Berlin` (später via User-Settings)

```typescript
import { format, parseISO } from 'date-fns';
import { de } from 'date-fns/locale';

// Backend: "2025-09-27T10:30:00Z"
// Frontend: "27.09.2025, 12:30 Uhr"
const displayDate = format(parseISO(isoString), 'dd.MM.yyyy, HH:mm', { locale: de });
```

## 7. Error UX

**Einheitliche Patterns:**

```typescript
const errorHandlers = {
  401: () => {
    toast.error('Session abgelaufen');
    setTimeout(() => navigate('/login'), 3000);
  },
  403: (error) => {
    // In-Component Alert
    return <Alert severity="error">Keine Berechtigung für diese Aktion</Alert>;
  },
  409: (error) => {
    openConflictModal({
      title: 'Konflikt',
      message: 'Daten wurden zwischenzeitlich geändert',
      actions: ['Neu laden', 'Änderungen verwerfen']
    });
  },
  422: (error) => {
    // Field-level errors
    return error.errors.reduce((acc, e) => ({
      ...acc,
      [e.field]: e.message
    }), {});
  },
  500: () => {
    // Error Boundary catches
    throw error;
  }
};
```

## 8. Theme v2 Mapping

**Tokens-Quelle:** `/frontend/src/theme/freshfoodz.ts`

```css
/* frontend/src/styles/tokens.css */
:root {
  /* FreshFoodz CI */
  --fresh-primary: #94C456;
  --fresh-secondary: #004F7B;
  --fresh-success: #94C456;
  --fresh-background: #F5F5F5;

  /* Typography */
  --font-headline: 'Antonio', sans-serif;
  --font-body: 'Poppins', sans-serif;

  /* Spacing (Tailwind-kompatibel) */
  --spacing-xs: 0.25rem;  /* 4px */
  --spacing-sm: 0.5rem;   /* 8px */
  --spacing-md: 1rem;     /* 16px */
  --spacing-lg: 1.5rem;   /* 24px */
  --spacing-xl: 2rem;     /* 32px */

  /* Radii */
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
}
```

## 9. Feature-Flag Strategie

**Implementation:**
- **Default:** `VITE_FEATURE_LEADGEN=false` auf main
- **Gate-Location:** `/frontend/src/providers.tsx`

```typescript
// providers.tsx
const LEAD_FEATURES_ENABLED = import.meta.env.VITE_FEATURE_LEADGEN === 'true';

// Routes
{LEAD_FEATURES_ENABLED && (
  <Route path="/neukundengewinnung/*" element={<LeadRoutes />} />
)}

// Navigation in MainLayoutV2.tsx
const navItems = [
  { label: 'Cockpit', path: '/cockpit' },
  ...(LEAD_FEATURES_ENABLED ? [
    { label: 'Neukundengewinnung', path: '/neukundengewinnung' }
  ] : [])
];
```

## 10. MSW-Mocks Coverage

**Mock-Szenarien:**
```typescript
// Success Cases
- GET /api/leads/stats → Normal KPIs
- GET /api/leads → 20 Leads paginated
- GET /api/followups → 5 pending T3, 3 pending T7

// Edge Cases
- Empty results (0 leads)
- Truncated event (>8KB payload)
- Pagination edge (last page)

// Error Cases
- 401 Unauthorized
- 403 Forbidden (no manager role)
- 422 Validation error
- 500 Server error
- Network timeout

// RBAC Testing
- Manager view (full stats)
- Sales view (limited stats)
- Admin view (all features)
```

## 11. API-Pfadpräfix

**Konvention:**
```typescript
// Base URL
const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

// Resources (RESTful)
/api/leads            # Lead CRUD
/api/leads/stats      # Dashboard KPIs
/api/leads/:id        # Single lead
/api/followups        # Follow-up list
/api/followups/:id/complete  # Complete action

// Events (future SSE)
/api/events/stream    # Server-Sent Events

// Naming: Plural für Collections, Singular für Actions
```

## Zusammenfassung

Alle 11 Fragen sind beantwortet mit konkreten Implementierungs-Details. Die Antworten bilden die Basis für den Thin Vertical Slice nach Abnahme der Research-Dokumentation.

**Nächste Schritte:**
1. Draft-PR mit docs-only Guardrails erstellen
2. Review & Merge der Research-Doku
3. Neuer Feature-Branch für Thin Vertical Slice mit Feature-Flag