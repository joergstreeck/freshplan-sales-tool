---
Title: Data Classification & Retention (DSGVO)
Purpose: Rechtssichere, transparente Datenbehandlung für FreshPlan.
Audience: Compliance, Security, Engineering, DPO
Last Updated: 2025-09-20
Status: Final
---

# 📋 Data Classification & Retention (DSGVO)

## 30-Second Summary
- Klare Klassifikation (Personal, Business, Analytics).
- Retention-Policies pro Datendomäne; DSAR/Export/Delete definiert.
- Anonymisierung/Minimierung konsequent.

## Classification

### Personal Data
- Kontaktdaten, Kommunikationsinhalte, Nutzerspuren.

### Business Data
- Leads, Opportunities, Orders, Credit Results (minimiert).

### Analytics Data
- Aggregierte KPIs, Event-Metriken.

## Retention (Richtwerte; per Policy umsetzbar)
- **Leads/Activities:** 5 Jahre nach letzter Aktivität (gesetzliche Aufbewahrung beachten).
- **Product Feedback/Test Results:** 2 Jahre (Qualität/Produktoptimierung).
- **Events/Outbox:** 90 Tage operativ, anschließend Archiv.
- **Audit-Logs:** 5–10 Jahre je Compliance-Anforderung.

## DSAR/Export/Delete
- **Export:** JSON/CSV; Frist einhalten; Logging der Herausgabe.
- **Delete:** Soft-Delete + Anonymisierung (PII entfernen), Referenzen bewahren.
- **Legal Hold:** Overrides via Policy dokumentiert.

## Security
- **Encryption:** in Transit/At Rest; Key Rotation jährlich; Zugriff per ABAC/RLS.
- **Minimierung:** nur notwendige Felder; Pseudonymisierung im Training/Analytics.

## Integration Points
- **Settings:** Retention-Keys optional in Registry
- **Ops:** Runbook (DSAR-Incidents, Backlog)
- **Event-Kette:** Löschevents vermeiden Reimports.

## Troubleshooting

### Unvollständige Löschung
DSAR-Job schrittweise prüfen (CRM, Events, Caches, Search-Index).