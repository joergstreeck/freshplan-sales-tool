---
module: "02_neukundengewinnung"
domain: "shared"
sprint: "2.1.3"
doc_type: "deltalog"
status: "approved"
owner: "team/leads"
updated: "2025-09-28"
---

# Release Notes – Sprint 2.1.3

## Highlights
- Lead-Erstellung & -Liste (Frontend)
- Validierung: Name ≥ 2, E-Mail-Format
- RFC7807-Fehler inkl. Feldfehler
- Duplikate (Light): 409-Warning bei gleicher E-Mail (MSW)
- i18n (de/en) vollständig; keine hardcoded Strings
- Stabile Dev-Konsole (Ping-Check entfernt), CI grün

## Kompatibilität
- Keine Breaking Changes
- Backend weiterhin via MSW simuliert (persistente Deduplizierung folgt in 2.1.4)

## Deployment
- Frontend auf `main` (PR #122)
- Keine DB-Migration in 2.1.3

## Nächste Schritte
- Sprint 2.1.4: Normalisierung (email_normalized, phone_e164), UNIQUE, Idempotency