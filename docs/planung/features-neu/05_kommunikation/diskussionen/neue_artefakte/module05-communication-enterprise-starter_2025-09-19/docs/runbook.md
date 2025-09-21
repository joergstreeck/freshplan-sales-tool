# Runbook – Communication Module
Stand: 2025-09-19

## SLOs & Alerts
- P95 latency < 200ms (Prometheus rule HighLatencyP95)
- HTTP 5xx rate < 1% (HighErrorRate)
- Outbox backlog < 200 (OutboxBacklogHigh)
- Bounce rate < 5% (BounceRateHigh)

## Incident Playbooks
### HighLatencyP95
1. Grafana: Panel 'HTTP p95 latency by route' – Identifiziere Route
2. Quarkus logs (correlation-id) prüfen
3. DB: Langsame Queries (pg_stat_statements), ggf. Index ergänzen
4. Scale up: `kubectl scale deploy/freshplan-comm-backend --replicas=5 -n comm`

### OutboxBacklogHigh
1. Worker Logs: EmailOutboxProcessor – Fehlercodes
2. SMTP‑Gateway Status, Rate Limits prüfen
3. Temporär BATCH erhöhen: env OUTBOX_BATCH_SIZE (roll restart)
4. Bounce‑Rate‑Panel auf Anomalien checken

### RLS Denies steigen
1. JWT Claim Mapping prüfen (territories)
2. Quarkus Filter: SET app.territory korrekt?
3. Verdächtige Requests (IDOR) in Logs mit 403 betrachten

## Zero‑Downtime Deploy
- RollingUpdate (maxUnavailable=0, maxSurge=1)
- Healthchecks: `/health` Backend, `/health` Frontend
- Blue/Green optional via Kustomize overlay
