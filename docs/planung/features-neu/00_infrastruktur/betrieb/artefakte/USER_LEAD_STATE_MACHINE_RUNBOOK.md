---
Title: User-Lead State Machine – Operations Runbook
Purpose: Betrieb der userbasierten Lead-Protection (6M + 60T Reminder + 10T Grace) inkl. Holds/Stop-Clock
Audience: Operations, Sales Ops, Backend
Last Updated: 2025-09-21
Status: Final
---

## 30-Second Summary
Die Lead-Protection ist **userbasiert** (kein Territory-Schutz). Schutz endet nach **6 Monaten** oder **vorzeitig** bei
> **60 Tagen Inaktivität** (+ **10 Tage Grace**), **Holds** pausieren beide Timer. Dieses Runbook steuert Reminder, Grace‑Monitoring, Expiry und Auditing.

## Zustände (vereinfachte State Machine)
- **PROTECTED** – aktiver Schutz (seit Assignment), Timer läuft mit Holds‑Pause.
- **REMINDER_DUE** – effektive Inaktivität ≥ 60T (< 70T), Reminder senden.
- **GRACE** – 10T Frist nach Reminder; neue qualifizierte Aktivität reaktiviert Schutz.
- **EXPIRED** – Schutz erloschen (6M erreicht **oder** 60T+10T ohne Aktivität).

**Qualifizierte Aktivitäten** (belegbar): `QUALIFIED_CALL`, `CUSTOMER_REACTION`, `SCHEDULED_FOLLOWUP`, `SAMPLE_FEEDBACK`, `ROI_PRESENTATION`.

## Tagesroutine (00:15 UTC)
1. **Compute effective states** (SQL‑View `v_user_lead_protection`) → Segmente: PROTECTED/REMINDER_DUE/GRACE/EXPIRED.
2. **Sende Reminder** an `REMINDER_DUE` (einmalig pro Fenster) und logge `lead.protection.reminder` Event.
3. **Mark Grace/Expire**: Falls `GRACE` und 10T ohne qualifizierte Aktivität → `EXPIRED`, Event `lead.protection.expired`.
4. **Holds beachten**: Holds subtrahieren Zeit sowohl vom 6M‑ als auch vom 60T‑Timer (Stop‑Clock).

## Sofort‑Kommandos
- **Hold setzen (Stop‑Clock):** siehe `ops/SQL/holds.sql` (INSERT)  
- **Hold beenden:** `ops/SQL/holds.sql` (UPDATE end_at = now())  
- **Reminder replay:** `ops/SQL/reminders.sql` (idempotent, Event Outbox)  
- **Lead‑Status prüfen:** SELECT auf `v_user_lead_protection` (Filtern nach `user_id`/`lead_id`).

## Runbook‑KPIs
- Reminders gesendet (24h), offene GRACE, **Expiries** last 7d, Reaktivierungen nach GRACE, SLA: Reminder < 1h nach Erreichen 60T.

## Failure Scenarios
- **Outbox lag > Ziel** → Replay‑Runbook, Priorität SEV2.  
- **Reminder‑Burst** → Rate‑Limit per User, Backoff/Jitter erhöhen.  
- **Holds overrun** → Audit prüfen (max. Hold‑Dauer policygesteuert).
