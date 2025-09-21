---
Title: Seasonal Playbook – Oktoberfest (September, ~4× Last)
Purpose: Event‑Catering‑Spitzen stabil bedienen
Audience: Operations, DevOps, Sales Ops
Last Updated: 2025-09-21
Status: Final
---

## Timeline (T‑Meilensteine)
- **T‑21d** Lasttest 4×; **Order‑Submit** und **Credit‑Final** besonders.  
- **T‑14d** Pre‑Provisioning: App 1.75×, Worker 2.5×, DB +25 % RAM.  
- **T‑7d** Freeze, Circuit‑Breaker‑Policies aggressiver (Provider).  
- **T‑1d** Warm‑ups, Eskalationsmatrix, manueller Backup‑Snapshot.

## Alerts
- `credit_final_p95_ms` > 500ms (10m)  
- `order_submit_error_rate` > 1% (5m)  
- `outbox_backlog` > 10k (5m)

## Day‑of
- War‑Room in 2‑h Slots, Provider‑Statusboard, manuelle Fallbacks bereit (PreCheck Cache).
