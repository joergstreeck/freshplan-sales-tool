# Reports WebSocket Events (V1)

**Prinzip:** Badges live, KPIs via Polling (30s). Events dienen als **Stale-Markierung** und leichte Inkremente.
Quelle sind Domain-Events (samples, activities, email) + Aggregator (ReportsService).

## Topics
- `reports.badge.changed` – kleine Zähler/Badges im Dashboard
- `reports.snapshot.updated` – täglicher Snapshot recomputed (notify UI zum Refresh)
- `reports.export.ready` – Export asynchron fertig (optional)

## Envelopes (CloudEvents-artig)
```json
{
  "type": "reports.badge.changed",
  "version": "1.0",
  "id": "evt-...",
  "time": "2025-09-19T10:00:00Z",
  "source": "crm.reports",
  "correlationId": "…",
  "actor": { "type": "system", "id": "aggregator" },
  "data": {
    "metric": "at_risk_customers",
    "delta": 1,
    "newValue": 42
  }
}
```
```json
{
  "type": "reports.snapshot.updated",
  "version": "1.0",
  "id": "evt-...",
  "time": "2025-09-19T02:05:00Z",
  "source": "crm.reports",
  "data": { "day": "2025-09-19", "snapshot": "rpt_sales_daily" }
}
```

## Aggregator-Trigger (Events → Badges)
- `sample.status.changed` → beeinflusst `sample_success_rate` (UI markiert stale)
- `activity.created{PRODUCTTEST_FEEDBACK|ROI_CONSULTATION}` → steigert Aktivitäts-Badges
- `customer.contactability.changed` → ändert `at_risk_customers`

**Fallback:** Wenn WS down, UI zeigt Stale-Indicator & Polling.
