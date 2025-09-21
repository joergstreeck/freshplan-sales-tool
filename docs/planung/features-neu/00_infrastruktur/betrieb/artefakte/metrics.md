---
Title: Domain Metrics & KPIs – Corrected Monitoring Strategy
Purpose: Einheitliche KPIs für User-Lead-Excellence + Seasonal B2B-Food
Audience: SRE, Product Ops, Sales Ops
Last Updated: 2025-09-21
Status: Final
---

## User-Lead KPIs
- `lead_reminder_due_total` / `lead_reminder_sent_total` – SLA: Sent >= Due innerhalb 60m.
- `lead_protection_state{state}` – Gauge pro Zustand; Ziel: EXPIRED minimal.
- `lead_activity_rate` – qualifizierte Aktivitäten pro User/Tag.
- `lead_conversion_success_total` – Conversions (Lead→Customer) pro Zeitraum.
- `commission_validation_errors_total` – Delta ≠ 0 Ergebnisse aus `provision_validation.sql`.

## B2B-Food Business Metrics
- `sample_request_total` / `sample_request_success_total` – Success‑Rate > 85% normal / > 80% peak.
- `event_catering_sla_breaches_total` – Verletzungen definierter Zeitfenster (Oktoberfest/Weihnachten).

## Seasonal Ops Dashboard (Panels)
1) p95 route‑wise (critical paths)  
2) Outbox Lag & DLQ  
3) Reminder Due vs. Sent (SLA)  
4) Expiries & Reactivations  
5) Capacity Utilization (CPU/RAM) vs. HPA Scale  
6) Cost per Lead & Order (aus FinOps Export)

## Cost per Lead Analytics
- Input: AI‑Kosten (Tokens/Provider), Infra‑Kosten (pro Modul), Sales‑Zeit.  
- Output: `cost_per_lead = (AI + Infra + SalesTime) / new_leads`; `cost_per_order` analog.  
- Dimensionen: **User**, Team, optional Region.
