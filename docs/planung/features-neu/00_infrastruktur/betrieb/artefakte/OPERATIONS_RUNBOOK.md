---
Title: Operations Runbook (Incident Response)
Purpose: Schnelle, standardisierte Reaktionen auf typische Störungen in FreshPlan.
Audience: SRE/DevOps, On-Call, Duty Manager
Last Updated: 2025-09-20
Status: Final
---

# 🛠️ Operations Runbook (Incident Response)

## 30-Second Summary
- Playbooks für Credit-Degradation, Email-Bounce-Peaks, AI-Budget-Cap, Outbox-Backlog, Peak-Scaling.
- Jedes Playbook enthält Checks, Actions, Escalation.

## Playbook: Credit-Degradation

### Checks
- p95 > SLO? Error > Budget? Provider-Status? Backlog/DLQ?

### Actions
- Cache TTL erhöhen, Batch-Window weiten, Priorisierung OrderSubmit.
- Circuit-Breaker auf PreCheck, manuelles Review aktivieren.

### Escalation
- Provider Ticket; Duty Manager informieren.

## Playbook: Email-Bounce Peak

### Checks
- Bounce-Rate, SMTP-Queue, DNS/TLS Status.

### Actions
- Rate-Limit drosseln; Retry Backoff; temporär alternative Route.

### Escalation
- Postmaster-Check; Kundenkommunikation.

## Playbook: AI-Budget-Cap erreicht

### Checks
- ai_cost_eur_day, model breakdown small/large.

### Actions
- Large-Routing sperren; nur Small erlauben; CAR Nudge-Budget reduzieren.

### Escalation
- Org-Budget-Freigabe in Admin.

## Playbook: Outbox-Backlog

### Checks
- integration_jobs_backlog, dlq_size.

### Actions
- Worker skalieren; DLQ replayen; gift messages isolieren.

### Escalation
- Integrations-Owner einschalten.

## Playbook: Peak-Load Scaling

### Actions
- App replizieren; DB Read-Replicas; Pool-Größen anheben; CDN-Kachelung.

### Validation
- k6 Peak-Profil erneut ausführen; Alerts überwachen.

## Integration Points
- **SLO:** [PERFORMANCE_SLO_CATALOG.md](../../leistung/artefakte/PERFORMANCE_SLO_CATALOG.md)
- **Events:** [EVENT_CATALOG.md](../../integration/artefakte/EVENT_CATALOG.md)
- **Settings:** [SETTINGS_REGISTRY_COMPLETE.md](../../verwaltung/artefakte/SETTINGS_REGISTRY_COMPLETE.md)