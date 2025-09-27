---
module: "02_neukundengewinnung"
domain: "frontend"
doc_type: "contract"
sprint: "2.1.2"
status: "approved"
owner: "team/leads"
updated: "2025-09-27"
---

# API Contract: Lead-Management Frontend ↔ Backend

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Frontend → Analyse → API_CONTRACT

**Sprint:** 2.1.2 – Frontend Research
**Erstellt:** 2025-09-27
**Version:** 1.0.0
**Status:** Production-Ready (basierend auf PR #111)

## 🔔 Event System

### Event Envelope Format (Unified)
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "source": "lead-management",
  "type": "dashboard.lead_status_changed",    // NICHT "eventType"!
  "time": "2025-09-27T10:30:00Z",            // UTC Instant (ISO-8601)
  "idempotencyKey": "deterministic-uuid-v5",
  "data": {
    // Event-spezifische Payload
  }
}
```

**PostgreSQL Channel:** `dashboard_updates`
**Event Types:** `dashboard.lead_status_changed`, `dashboard.followup_completed`

### Dashboard Events

#### 1. Lead Status Change Event
```json
{
  "type": "dashboard.lead_status_changed",
  "data": {
    "leadId": "123e4567-e89b-12d3-a456-426614174000",
    "companyName": "Restaurant Sonne GmbH",
    "oldStatus": "REGISTERED",
    "newStatus": "QUALIFIED",
    "userId": "user-456",
    "changedAt": "2025-09-27T10:30:00Z",
    "territory": "DE-BY"
  }
}
```

#### 2. Follow-up Completed Event
```json
{
  "type": "dashboard.followup_completed",
  "data": {
    "leadId": "123e4567-e89b-12d3-a456-426614174000",  // Optional bei BATCH
    "followUpType": "T3",                              // T3|T7|BATCH
    "t3Count": 5,
    "t7Count": 2,
    "success": true,
    "userId": "user-456",
    "processedAt": "2025-09-27T10:30:00Z"
  }
}
```

### Truncation Handling (8KB Limit)
```json
{
  "type": "dashboard.lead_status_changed",
  "data": {
    "truncated": true,
    "reference": "550e8400-e29b-41d4-a716-446655440000",
    "original_size_bytes": 9248,
    "message": "Payload exceeded 8KB limit. Fetch via API."
  }
}
```

**Frontend Fallback bei Truncation:**
```typescript
if (event.data.truncated) {
  // Vollständige Daten via REST nachladen
  const fullData = await leadApi.getEvent(event.data.reference);
  // UI: Badge "Details laden" + Retry-Action anzeigen
  showTruncationBadge({
    text: "Details aus Backend laden",
    action: () => fetchFullData(event.data.reference)
  });
}
```

## 🔐 RBAC Security

### Role-Based Access
```typescript
interface UserContext {
  userId: string;
  roles: ('MANAGER' | 'SALES' | 'ADMIN')[];
  territory?: string;
}
```

### UI-Sichtbarkeitsmatrix

| UI-Element | SALES | MANAGER | ADMIN | Beschreibung |
|------------|-------|---------|-------|-------------|
| **Dashboard KPI-Tiles** ||||
| Meine Pending T3/T7 | ✅ | ✅ | ✅ | Eigene Follow-ups |
| Team Pending T3/T7 | ❌ | ✅ | ✅ | Aggregation Team |
| Territory Stats | ❌ | ✅ | ✅ | DE/AT/CH Metriken |
| Pipeline Value | ❌ | ✅ | ✅ | Monetäre Werte |
| **Lead-Liste** ||||
| Eigene Leads | ✅ | ✅ | ✅ | Owned by User |
| Team-Leads | ❌ | ✅ | ✅ | Alle im Team |
| Alle Leads | ❌ | ❌ | ✅ | System-weit |
| Export-Button | ❌ | ✅ | ✅ | CSV/Excel Export |
| **Follow-up Inbox** ||||
| Eigene Follow-ups | ✅ | ✅ | ✅ | Assigned to User |
| Team-Queue | ❌ | ✅ | ✅ | Unassigned Pool |
| Reassign-Action | ❌ | ✅ | ✅ | Follow-up zuweisen |
| **Analytics** ||||
| Conversion Funnel | ❌ | ✅ | ✅ | Lead → Customer |
| Success Rates | ❌ | ✅ | ✅ | T3/T7 Performance |
| Trend Charts | ❌ | ✅ | ✅ | Zeitverläufe |

### API Response Filtering
```json
// SALES User Response
{
  "stats": {
    "myLeads": 23,
    "myT3Pending": 5,
    "myT7Pending": 2,
    "myConversionRate": 0.28
  }
}

// MANAGER User Response
{
  "stats": {
    "myLeads": 23,
    "teamLeads": 142,
    "territoryLeads": 567,
    "myT3Pending": 5,
    "teamT3Pending": 34,
    "myConversionRate": 0.28,
    "teamConversionRate": 0.31,
    "pipelineValue": 145000,
    "trend": {
      "weekly": "+12%",
      "monthly": "+8%"
    }
  }
}
```

## ⏰ Pending-Definition & Zeitlogik

### "Pending" Follow-ups
- **Definition:** Follow-ups mit `dueAt <= NOW()` UND `status = 'PENDING'`
- **Inkludiert:** Überfällige Items (`dueAt < NOW() - 24h` werden als "overdue" markiert)
- **Zeitzone:**
  - Backend speichert UTC
  - Frontend konvertiert zu User-Timezone (default: `Europe/Berlin`)
  - Display: "Heute 14:00", "Gestern", "Vor 2 Tagen"

### Berechnungslogik
```typescript
// T3 = 3 Tage nach Sample-Versand
const t3DueDate = addDays(sampleSentDate, 3);
// Wochenenden überspringen
if (isWeekend(t3DueDate)) {
  t3DueDate = nextMonday(t3DueDate);
}

// T7 = 7 Tage nach erstem Kontakt
const t7DueDate = addDays(firstContactDate, 7);
```

## 🔄 Real-time Strategy

### Phase 1: Polling (Current)
```typescript
// React Query mit Auto-Refetch
const POLL_INTERVAL = 20000; // 20 Sekunden

useQuery({
  queryKey: ['lead-stats'],
  queryFn: leadApi.getStats,
  refetchInterval: POLL_INTERVAL,
  refetchIntervalInBackground: false, // Pause wenn Tab inaktiv
  refetchOnWindowFocus: true,
  retry: 3,
  retryDelay: attemptIndex => Math.min(1000 * 2 ** attemptIndex, 30000),
});
```

### Phase 2: SSE (Planned)
```typescript
// Server-Sent Events für Real-time Updates
const eventSource = new EventSource('/api/events/stream', {
  withCredentials: true
});

eventSource.addEventListener('lead_update', (e) => {
  const event = JSON.parse(e.data);
  queryClient.invalidateQueries(['leads', event.leadId]);
});
```

### Fallback-Strategie
1. Versuche SSE-Connection
2. Bei Fehler: Fallback auf Polling
3. Bei Netzwerk-Reconnect: SSE neu versuchen

## 📊 REST API Endpoints

### Lead Statistics
```http
GET /api/leads/stats
Authorization: Bearer <token>

Response 200:
{
  "pendingT3": 12,
  "pendingT7": 8,
  "processedToday": 34,
  "conversionRate": 0.28,
  "totalActive": 156,
  "trends": {
    "t3Success": 0.72,
    "t7Success": 0.61,
    "weeklyGrowth": 0.12
  }
}
```

### Lead List
```http
GET /api/leads?status=QUALIFIED&territory=DE-BY&page=0&size=20
Authorization: Bearer <token>

Response 200:
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "companyName": "Restaurant Sonne GmbH",
      "status": "QUALIFIED",
      "createdAt": "2025-09-25T08:00:00Z",
      "lastActivity": "2025-09-27T09:15:00Z",
      "ownerUserId": "user-456",
      "territory": "DE-BY",
      "nextFollowUp": {
        "type": "T3",
        "dueAt": "2025-09-28T10:00:00Z"
      }
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 156,
  "totalPages": 8
}
```

### Follow-up Queue
```http
GET /api/followups?type=T3&status=PENDING&due=TODAY
Authorization: Bearer <token>

Response 200:
{
  "followUps": [
    {
      "id": "987e6543-e21b-98d7-b654-987321654000",
      "leadId": "123e4567-e89b-12d3-a456-426614174000",
      "type": "T3",
      "dueAt": "2025-09-27T14:00:00Z",
      "priority": "HIGH",
      "lead": {
        "companyName": "Restaurant Sonne GmbH",
        "contactPerson": "Max Mustermann",
        "phone": "+49 89 123456",
        "email": "info@sonne.de"
      },
      "lastInteraction": {
        "type": "SAMPLE_SENT",
        "date": "2025-09-24T10:00:00Z",
        "notes": "Cook&Fresh Probe-Box versendet"
      }
    }
  ],
  "summary": {
    "totalPending": 12,
    "overdue": 2,
    "dueToday": 8,
    "dueTomorrow": 2
  }
}
```

### Complete Follow-up
```http
POST /api/followups/987e6543-e21b-98d7-b654-987321654000/complete
Authorization: Bearer <token>
Content-Type: application/json

{
  "outcome": "SUCCESS",
  "notes": "Kunde begeistert von Probe, Angebot angefordert",
  "nextAction": "SEND_OFFER",
  "scheduledFor": "2025-09-28T10:00:00Z"
}

Response 200:
{
  "followUpId": "987e6543-e21b-98d7-b654-987321654000",
  "completedAt": "2025-09-27T14:30:00Z",
  "leadStatusUpdated": true,
  "newLeadStatus": "OPPORTUNITY",
  "nextFollowUp": {
    "type": "OFFER_FOLLOWUP",
    "scheduledFor": "2025-09-28T10:00:00Z"
  }
}
```

## 📈 Prometheus Metrics (Frontend-relevant)

### Counter Metriken
```promql
# Events published (ohne _total suffix!)
freshplan_events_published{event_type="dashboard.lead_status_changed",module="leads",result="success"}
freshplan_events_published{event_type="dashboard.followup_completed",module="leads",result="denied"}

# Events consumed
freshplan_events_consumed{event_type="dashboard.lead_status_changed",module="dashboard"}
```

### Latency Histogramme
```promql
# API Response Times
freshplan_event_latency{event_type="lead_status_change",path="publish_notify"}
```

### Cache Metriken
```promql
# Deduplizierung
freshplan_dedupe_cache_entries            # Aktuelle Anzahl
freshplan_dedupe_cache_hit_rate           # Hit-Rate (0.0-1.0)
```

### Pagination & Filtering

#### Query-Parameter für GET /api/leads
```http
GET /api/leads?
  page=0&              # 0-basiert
  size=20&             # Default: 20, Max: 100
  sort=createdAt,desc& # Format: field,direction
  status=QUALIFIED&    # Enum: REGISTERED|QUALIFIED|CONTACTED|...
  territory=DE-BY&     # ISO + Region
  owner=user-123&      # User-ID (nur Manager/Admin)
  search=Restaurant&   # Volltext in companyName
  hasFollowUp=true&    # Boolean Filter
  createdAfter=2025-09-01T00:00:00Z
```

#### Response-Header
```http
X-Total-Count: 156
X-Total-Pages: 8
X-Current-Page: 0
X-Page-Size: 20
```

## 🔴 Error Responses & UI-Verhalten

### HTTP Error Mapping

| HTTP Code | Error Type | UI-Verhalten | User-Feedback |
|-----------|------------|--------------|---------------|
| **401** | Unauthorized | Redirect zu Login nach 3s | Toast "Session abgelaufen" + Countdown |
| **403** | Forbidden | Inline-Alert in betroffener Komponente | "Sie haben keine Berechtigung für diese Aktion" |
| **409** | Conflict | Modal mit Konflikt-Details | "Lead wurde bereits bearbeitet. Neu laden?" |
| **422** | Validation Error | Feld-Markierung rot | Inline unter jedem Feld |
| **429** | Rate Limited | Backoff + Auto-Retry | Toast "Zu viele Anfragen. Warte X Sekunden..." |
| **500** | Server Error | Error Boundary aktiviert | Fallback-UI "Technischer Fehler" |
| **503** | Service Unavailable | Wartungsmodus-Banner | "System in Wartung. Bitte später..." |
| **Network** | Offline/Timeout | Offline-Indicator | "Keine Verbindung. Automatischer Retry..." |

### Error Response Format (RFC 7807)
```json
{
  "type": "https://api.freshplan.de/errors/validation",
  "title": "Validation Failed",
  "status": 422,
  "detail": "Lead-Daten unvollständig",
  "instance": "/api/leads/123",
  "errors": [
    {
      "field": "companyName",
      "message": "Firmenname erforderlich"
    },
    {
      "field": "email",
      "message": "Ungültiges E-Mail-Format"
    }
  ]
}
```

### Frontend Error Handler
```typescript
// Zentraler Error-Interceptor
const handleApiError = (error: ApiError) => {
  switch (error.status) {
    case 401:
      toast.error('Session abgelaufen');
      setTimeout(() => navigate('/login'), 3000);
      break;
    case 403:
      // Component-level handling
      return { type: 'forbidden', message: error.detail };
    case 409:
      openConflictModal(error);
      break;
    case 422:
      // Return for form handling
      return { type: 'validation', fields: error.errors };
    case 429:
      const retryAfter = error.retryAfter || 60;
      toast.loading(`Warte ${retryAfter}s...`);
      setTimeout(() => retry(), retryAfter * 1000);
      break;
    default:
      // Let Error Boundary handle
      throw error;
  }
};
```


## 🧪 Testing

### Mock Responses (MSW)
```typescript
// Mock Lead Stats
http.get('/api/leads/stats', () => {
  return HttpResponse.json({
    pendingT3: 12,
    pendingT7: 8,
    processedToday: 34,
    // Manager-only fields (check role)
    ...(hasRole('MANAGER') && {
      teamStats: { ... }
    })
  });
});
```

### E2E Validation
```typescript
test('Dashboard updates on follow-up completion', async ({ page }) => {
  // Initial state
  await expect(page.locator('[data-testid="t3-pending"]')).toHaveText('12');

  // Complete follow-up
  await page.click('[data-testid="complete-followup"]');

  // Wait for update (polling or SSE)
  await expect(page.locator('[data-testid="t3-pending"]')).toHaveText('11');
});
```

## 📝 Implementation Notes

1. **Idempotenz:** Frontend sollte `idempotencyKey` für Duplikat-Erkennung nutzen
2. **UTC-Zeiten:** Alle Timestamps in UTC, lokale Anzeige via date-fns
3. **RBAC-Cache:** User-Rollen im Context cachen, nicht bei jedem Request prüfen
4. **Truncation:** Graceful degradation - essenzielle Daten immer im Envelope
5. **Error Boundaries:** Fehlerhafte Events dürfen UI nicht crashen