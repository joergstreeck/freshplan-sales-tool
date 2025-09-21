---
Title: Seasonal Playbook – Weihnachten (Nov–Dez, ~5× Last)
Purpose: Premium‑Catering‑Peak sicher durchsteuern
Audience: Operations, DevOps, Sales Ops
Last Updated: 2025-09-21
Status: Final
---

## Timeline
- **T‑28d** Lasttest 5× + Chaos (Provider Down, DLQ Replay).  
- **T‑21d** Pre‑Provisioning: App 2×, Worker 3×, DB +30 % RAM.  
- **T‑14d** Feature‑Freeze; On‑Call Verstärkung; Limits hochfahren.  
- **T‑7d** Dry‑Run DR‑Restore; Credit Provider‑Failover testen.  
- **T‑1d** Warm‑ups; Eskalation confirmed.

## Alerts (verschärft)
- p95 Reads > 350ms (5m), Credit‑PreCheck > 500ms (5m)  
- DLQ > 0 sofort CRIT; Consumer‑Lag > Ziel 10m.

## Betrieb
- War‑Room täglich; Backpressure feinjustieren; Kostenmonitoring (Pre‑Provision vs. Autoscale).
