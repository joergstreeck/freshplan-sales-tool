---
Title: Seasonal Playbook – Spargel-Saison (April–Juni, ~3× Last)
Purpose: Sichere Skalierung & Stabilität für saisonale B2B-Food Nachfrage
Audience: Operations, DevOps, Sales Ops
Last Updated: 2025-09-21
Status: Final
---

## Timeline (T‑Meilensteine)
- **T‑21d** Lasttest (k6) mit Faktor **3×** auf Kernpfade (Lead→Sample→Trial→Order, Credit‑PreCheck).  
- **T‑14d** Pre‑Provisioning (App 1.5×, Worker 2×), DB‑Headroom +20 %, Caches warm.  
- **T‑7d** Feature‑Freeze, Readiness Probe‑Schwellen tighten, Error‑Budgets aktiv.  
- **T‑1d** Cache Warm‑up, JIT warm, Eskalationsmatrix bestätigen.
- **T** Monitoring War‑Room: p95, Outbox Lag, Bounce Rate, Credit Errors.

## Kapazitätsempfehlung (Daumenregel)
- App‑Pods: **baseline_pods × 1.5**; Worker: **baseline × 2.0** (Outbox, Reminder).  
- DB: größerer Instance‑Typ **+20% RAM**, PgBouncer Limits prüfen, `work_mem` konservativ.

## Alerts (aktiv)
- `http_p95_ms{route in ["/api/credit/precheck","/api/activities"]}` > 300ms (5m)  
- `outbox_lag_seconds` > 3s (5m), `bounce_rate` > 2% (15m)  
- `db_connections_utilization` > 80% (5m)

## Day‑of Operations
- Hourly Review p95/Lag/Errors, Backpressure‑Hebel bereit (HPA Grenzen, Queue Batchsize).  
- Kommunikation: Sales informiert über degradierte Provider (Credit/E‑Mail).

## Post‑Mortem Checklist
- SLO Verletzungen & Kostenreport (Pre‑Provision vs. Auto‑Scale), Learnings ins Calculator.
