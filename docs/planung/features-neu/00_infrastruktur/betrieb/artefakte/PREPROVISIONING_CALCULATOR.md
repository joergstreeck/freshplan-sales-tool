---
Title: Pre‑Provisioning Calculator
Purpose: Kostenoptimierte Kapazitätsplanung für 3×/4×/5× Lastspitzen
Audience: DevOps/SRE
Last Updated: 2025-09-21
Status: Final
---

## Formel (pro Route‑Klasse)
Empfohlenes Pod‑Ziel:
```
pods = ceil( (baseline_rps * peak_factor) / (target_rps_per_pod * target_utilization) )
```
- `baseline_rps`: gemessene RPS normal
- `peak_factor`: 3/4/5
- `target_rps_per_pod`: gemessen (p95 eingehalten)
- `target_utilization`: 0.6 (App), 0.5 (Worker)

**DB‑Headroom**: RAM +20/25/30%, `max_connections` = `pods * conns_per_pod` (PgBouncer), Reserve 10–15%.

## Beispiel
- baseline_rps=50, peak=5, target_rps_per_pod=20, util=0.6 → pods= ceil(250/(12))=21.
