---
Title: Performance SLO Catalog (Normal & Seasonal Peak)
Purpose: Verbindliche SLOs, Testprofile und Alert-Regeln für FreshPlan.
Audience: DevOps, Performance, Monitoring, Engineering
Last Updated: 2025-09-20
Status: Final
---

# ⚡ Performance SLO Catalog (Normal & Seasonal Peak)

## 30-Second Summary
- Normalbetrieb: CreditCheck p95 < 300 ms; Admin-Reads p95 < 200 ms; UI-TTI < 2 s.
- Peak (5× Last; Oktober/November): CreditCheck p95 < 450–500 ms; Error < 2 %.
- k6-Profile und Prometheus-Alerts definieren Go/No-Go operational.

## Core SLOs

### APIs (Reads)
- **p95 < 200 ms, Error < 0.5 %**

### CreditCheck
- **Normal:** p95 < 300 ms, Error < 1 %.
- **Peak:** p95 < 450–500 ms, Error < 2 %.

### UI
- **TTI < 2 s, LCP < 2.5 s (Desktop), < 3.0 s (Mobile)**

### Concurrency-Ziele
- 50+ Sales Manager parallel.
- 200 parallele Sessions (Normal), 500 parallele CreditChecks (Peak).

## k6 Snippets (Kurz)

### Normal
```javascript
vus: 50;
duration: 15m;
thresholds: http_req_duration{tag:credit}<300
```

### Peak
```javascript
vus: 200–500 (Credit);
staged ramp;
thresholds: http_req_duration{tag:credit}<500
```

## Monitoring & Alerts

### Prometheus Query Ideen
```promql
credit_check_p95_ms
credit_check_error_rate
http_request_duration_seconds_bucket{route="/api/credit"} → p95
integration_jobs_backlog{provider}
dlq_size{provider}
```

### Alert-Schwellen
- **Warning:** p95 > Ziel 15 min.
- **Critical:** p95 > Ziel 30 min ODER Error > Budget 10 min.

## Mitigation Patterns
- **Cache TTL:** 4–8 h (Amount-Buckets)
- **Micro-Batching:** 25–50 ms
- **Priority Queues:** OrderSubmit > PreCheck
- **Circuit-Breaker:** pro Provider
- **Graceful Degradation:** „Credit Pending"

## Integration Points
- **Events:** [EVENT_CATALOG.md](../../integration/artefakte/EVENT_CATALOG.md) (credit.checked)
- **Ops:** [OPERATIONS_RUNBOOK.md](../../operations/artefakte/OPERATIONS_RUNBOOK.md)
- **Settings:** [SETTINGS_REGISTRY_COMPLETE.md](../../governance/artefakte/SETTINGS_REGISTRY_COMPLETE.md) (credit.*)

## Troubleshooting

### p95 Regression im Peak
Prüfe Caching aktiv, Batch-Window, Provider-Backlog, DLQ, Connection Pools.