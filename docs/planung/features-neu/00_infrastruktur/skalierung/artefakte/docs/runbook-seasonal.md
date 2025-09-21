
---
Title: Seasonal Scaling Runbook (CQRS Light)
Purpose: Operative Leitlinie für planbares Pre-Scaling & sichere Peaks (Oktoberfest, Weihnachten, Spargel) ohne Over-Engineering
Audience: Ops, Dev, SalesOps
Last Updated: 2025-09-21
Status: Final
---

## 30-Second Summary
- **Schedules > ML:** Nutzung von KEDA Cron-Scalern + leichten Heuristiken (PromQL) liefert 95% Nutzen bei minimaler Komplexität.
- **Ein Cluster, Territory-Labels:** DE/CH/AT in einem Cluster; getrennte ScaledObjects pro Territory & Workload (queries/commands).
- **Guardrails:** LISTEN/NOTIFY mit Journal-Backup; Alerts für p95, Outbox-Lag, Notify-Queue.

## Pre-Scale Windows (Empfehlung)
| Saison        | Zeitraum         | Territory | Queries min→peak | Commands min→peak |
|---------------|------------------|-----------|------------------|-------------------|
| Oktoberfest   | Sep 1 – Oct 15   | DE (Bayern Fokus) | 2 → 6 | 1 → 4 |
| Weihnachten   | Nov 1 – Dec 31   | DE/AT/CH | 2 → 6 (DE), 1 → 4 (CH/AT) | 1 → 4 |
| Spargel       | Apr 1 – Jun 30   | DE (BW Fokus) | 2 → 4 | 1 → 3 |

> Umsetzung: `keda-scalers.yaml` Cron‑Trigger aktiv; Anpassungen via PR. Schedules sind deterministisch & auditierbar.

## Rollback-Strategien
- **Pre-Scale zu hoch / kein Lastanstieg:** `desiredReplicas` reduzieren (Cron) + `minReplicaCount` zurücksetzen.  
- **Unerwartete Lastspitze:** temporär `maxReplicaCount` +2; prüfen: p95‑Alerts, RPS‑Scaler aktiv?  
- **Notify‑Queue > 90%:** Event‑Journal greift; Listener‑Pool prüfen; `integration.replay_pending()` nach Abfluss.

## Territory-specific Playbooks
- **DE (Oktoberfest/Weihnachten):** Queries + Commands aktiv pre‑scalen; Credit‑Check Bulkhead anheben (Settings: `credit.limit.concurrent`), Cache TTL 5–15 min.
- **CH:** Moderate Pre‑Scale, höhere Latenzschwellen (p95 220–280 ms okay); Kampagnen kleiner batchen.
- **AT:** Kleinste Flotte; nur Cron‑Floor anheben, PromQL‑Scaler regelt Rest.

## Operational Checks (täglich in Peak)
1. Dashboards: p95 per Route/Territory, RPS, Outbox‑Lag, Notify‑Queue Usage.  
2. Credit‑Check inflight per Provider < 25; Error‑Rate < 2%.  
3. Outbox‑Queue depth < 500; Lag < 60s.  
4. DB Health: connections, autovacuum, hot indices (no seq scans auf Top‑Routen).

## Emergency Procedures
- **Queries p95 > 500ms:** Replika lesen für Reporting auslagern, Projektion re‑compute (nightly job).  
- **Commands p95 > 600ms:** Bulkhead enger, Lock‑Hotspots analysieren (EXPLAIN, lock tables).  
- **Notify Queue > 95%:** Temporär Notify aus `enqueue_event` drosseln (threshold 0.9), nur Journal schreiben; später `replay_pending()`.

## Integration Points
- **Settings Registry:** `features.cqrs.enabled`, `events.notify.payloadVersion`, `credit.limit.concurrent`, `email.ratelimit.per_min`.  
- **CI Gates:** Perf‑Gates auf p95 ms & error‑rate, Territory‑spezifischer Smoke (k6).  
- **SalesOps:** Campaign‑Start → Ops informieren (Burst‑Window + Ratios).

## Troubleshooting (Kurz)
- **Hohe p95 nur auf Commands:** Prüfe DB‑Locks, verifiziere ETag/If‑Match auf Activities/Notes.  
- **E‑Mail Verzögerungen:** Outbox‑Worker skaliert? Bounce‑Rate? Rate‑Limiter zu streng?  
- **Credit‑Check Timeouts:** Provider‑Limit, Backoff/Jitter aktivieren, Cache TTL hoch.

## Links
- keda-scalers.yaml
- alerts-scaling.yml
- listener-and-journal.sql
