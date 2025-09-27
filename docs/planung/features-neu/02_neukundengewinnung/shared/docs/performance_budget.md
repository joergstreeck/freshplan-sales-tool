---
module: "02_neukundengewinnung"
doc_type: "guideline"
status: "draft"
owner: "team/architecture"
updated: "2025-09-27"
---

# Performance Budget – Modul 02 (Foundation V2)
- API P95: < 200 ms (GET/POST /api/leads, /api/leads/export)
- Email Batch: 1000 mails in < 2 s, Error < 0.5%
- Frontend: FCP < 1.5 s, TTI < 2.0 s, Bundle < 250 KB initial
- WS: Reconnect < 5 s Backoff max, Heartbeat 30 s
