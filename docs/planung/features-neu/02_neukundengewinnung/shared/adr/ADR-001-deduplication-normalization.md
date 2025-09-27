---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "adr"
status: "draft"
sprint: "2.1.4"
owner: "team/leads"
updated: "2025-09-27"
---

# ADR-001: Normalisierung & deterministische Deduplizierung (Phase 1)

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Shared → ADR → Normalisierung

## Status

Draft - Sprint 2.1.4

## Kontext

Die Lead-Erfassung aus mehreren Quellen (manuell, Import, API) erfordert eine robuste und erklärbare Duplikatvermeidung. Unterschiedliche Schreibweisen derselben E-Mail-Adresse oder Telefonnummer sollen als identisch erkannt werden.

## Entscheidung

### E-Mail-Normalisierung:
- **Basis:** `lowercase(trim(email))`
- **Optional per Flag:** Plus-Tag-Entfernung (`user+tag@example.com` → `user@example.com`)
- **Persistenz:** Zusätzliche Spalte `email_normalized`
- **Index:** Partieller UNIQUE Index (WHERE NOT NULL)

### Telefon-Normalisierung:
- **Format:** E.164 via libphonenumber
- **Persistenz:** Zusätzliche Spalte `phone_e164`
- **Validierung:** Nur valide Nummern werden normalisiert gespeichert
- **Index:** Partieller UNIQUE Index (WHERE NOT NULL)

### Konfliktbehandlung:
- **Response:** RFC7807 Problem Details mit feldspezifischen Errors
- **Status:** 409 Conflict
- **Body:** `{ "errors": { "email": ["Already exists"], "phone": ["Already exists"] }}`

## Konsequenzen

### Positiv:
- ✅ Höhere Datenqualität
- ✅ Deterministisch und nachvollziehbar
- ✅ Performance durch DB-Indizes
- ✅ Klare API-Fehler für Frontend

### Negativ:
- ⚠️ Breaking Change für bestehende Duplikate
- ⚠️ Zusätzliche DB-Spalten (Storage)
- ⚠️ Migration muss Duplikate bereinigen

### Neutral:
- Provider-spezifische Regeln (Gmail-Dots) initial NICHT implementiert (zu viele False Positives)

## Alternativen (verworfen)

1. **Voller Identitätsgraph in Phase 1**
   - Verworfen: Overkill für MVP
   - Geplant für Sprint 2.1.6

2. **Nur Soft-Dedupe ohne UNIQUE**
   - Verworfen: Risiko stiller Duplikate
   - Race Conditions möglich

3. **Normalisierung nur in Application Layer**
   - Verworfen: Keine DB-Constraints
   - Inkonsistenzen bei direkten DB-Writes

## Implementation Notes

### Migration (PostgreSQL):
```sql
ALTER TABLE leads ADD COLUMN email_normalized TEXT;
ALTER TABLE leads ADD COLUMN phone_e164 TEXT;

CREATE UNIQUE INDEX leads_email_normalized_uniq
  ON leads (email_normalized)
  WHERE email_normalized IS NOT NULL;

CREATE UNIQUE INDEX leads_phone_e164_uniq
  ON leads (phone_e164)
  WHERE phone_e164 IS NOT NULL;
```

### Feature Flags:
- `EMAIL_REMOVE_PLUS_TAGS=false` (default)
- `PHONE_COUNTRY_DEFAULT=DE` (für Parsing ohne Ländervorwahl)

## Referenzen

- RFC7807: Problem Details for HTTP APIs
- E.164: International Telecommunication Union standard
- libphonenumber: Google's phone number handling library