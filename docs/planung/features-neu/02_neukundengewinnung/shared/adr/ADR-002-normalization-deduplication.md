---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "adr"
status: "approved"
sprint: "2.1.4"
owner: "team/leads-backend"
updated: "2025-09-28"
---

# ADR-002: Normalisierung und Soft-Deduplizierung für Lead-Management

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Shared → ADR → ADR-002

## Status
Accepted (2025-09-28)

## Context
Das Lead-Management-System benötigt eine robuste Lösung zur Vermeidung von Duplikaten. Bisherige Erfahrungen zeigen:
- Benutzer geben E-Mail-Adressen unterschiedlich ein (Groß-/Kleinschreibung, Leerzeichen)
- Telefonnummern werden in verschiedenen Formaten eingegeben
- Namen enthalten oft inkonsistente Leerzeichen oder Diakritika
- Historische Daten enthalten bereits Duplikate, die nicht hart blockiert werden dürfen

## Decision

### 1. Normalisierte Felder in der Datenbank
Wir fügen normalisierte Felder zusätzlich zu den Original-Feldern hinzu:
- `email_normalized` (CITEXT) - Case-insensitive Text
- `name_normalized` (TEXT) - Kleinbuchstaben, normalisierte Whitespace
- `phone_e164` (TEXT) - E.164 Format

### 2. Partial Unique Index (Soft-Deduplizierung)
```sql
CREATE UNIQUE INDEX CONCURRENTLY uq_leads_tenant_email_normalized
ON leads(tenant_id, email_normalized)
WHERE email_normalized IS NOT NULL;
```

Dieser Ansatz:
- Verhindert neue Duplikate
- Blockiert nicht bei historischen Duplikaten
- Erlaubt NULL-Werte (Leads ohne E-Mail)

### 3. Idempotenz für API-Calls
- Header: `Idempotency-Key: <uuid>`
- Store mit 24h TTL
- Request-Hash-Validierung zur Sicherheit

### 4. Normalisierungsregeln
- **E-Mail:** `lower(trim(email))`, optional Plus-Tag-Entfernung per Flag
- **Name:** `unaccent(lower(regexp_replace(trim(name), '\s+', ' ', 'g')))`
- **Telefon:** libphonenumber → E.164 mit Ländercode-Kontext

## Consequences

### Positive
- ✅ Keine Breaking Changes für bestehende Daten
- ✅ Deterministische Duplikat-Vermeidung
- ✅ Resiliente API durch Idempotenz
- ✅ Zero-Downtime-Migration möglich
- ✅ Multi-Tenant-fähig

### Negative
- ❌ Zusätzlicher Speicherplatz für normalisierte Felder (~30% mehr)
- ❌ Komplexität in der Anwendungslogik
- ❌ Historische Duplikate bleiben zunächst bestehen

### Neutral
- Migration muss in Batches erfolgen bei großen Datenmengen
- Feature-Flags für schrittweisen Rollout empfohlen

## Alternatives Considered

### Alternative 1: Harter Unique-Constraint
**Verworfen:** Würde bei historischen Duplikaten die Migration brechen

### Alternative 2: Nur Application-Level-Checks
**Verworfen:** Race-Conditions bei gleichzeitigen Inserts möglich

### Alternative 3: Fuzzy-Matching mit Elasticsearch
**Verworfen:** Zu komplex für aktuellen Scope, kann in Sprint 2.1.5 ergänzt werden

## Implementation Notes

### Migration-Strategie
1. ADD COLUMN für neue Felder (nullable)
2. Backfill in Batches (1000 Records pro Batch)
3. CREATE INDEX CONCURRENTLY (kein Lock)
4. Aktivierung per Feature-Flag

### Rollback-Plan
- DROP INDEX (sofort)
- Feature-Flag deaktivieren
- Columns können bleiben (harmlos)

### Monitoring
- `leads_conflicts_total` - Duplikat-Konflikte
- `normalization_duration_ms` - Performance-Tracking
- `idempotency_cache_hit_rate` - Cache-Effizienz

## References
- [TRIGGER_SPRINT_2_1_4.md](../../../../TRIGGER_SPRINT_2_1_4.md)
- [RFC7807](https://datatracker.ietf.org/doc/html/rfc7807) - Problem Details
- [E.164 Standard](https://www.itu.int/rec/T-REC-E.164)
- [PostgreSQL CITEXT](https://www.postgresql.org/docs/current/citext.html)