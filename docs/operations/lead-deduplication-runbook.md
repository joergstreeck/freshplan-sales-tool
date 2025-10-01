---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "guideline"
status: "approved"
owner: "team/operations"
updated: "2025-10-01"
---

# Lead Deduplication & Idempotency - Operations Runbook

**📍 Navigation:** Home → Operations → Lead Deduplication Runbook

## 🎯 Übersicht

Dieses Runbook beschreibt operative Aufgaben für Lead-Normalisierung, Deduplizierung und Idempotency nach Sprint 2.1.4.

**Verwandte Dokumente:**
- [TRIGGER_SPRINT_2_1_4.md](../planung/TRIGGER_SPRINT_2_1_4.md)
- [ADR-001-deduplication-normalization.md](../planung/features-neu/02_neukundengewinnung/shared/adr/ADR-001-deduplication-normalization.md)

---

## 📊 Production Index Build (CONCURRENTLY)

### Kontext
Sprint 2.1.4 nutzt in CI/Dev **V10012** (non-CONCURRENTLY Indizes für Geschwindigkeit).
Production braucht **CONCURRENTLY-Indizes** um Table Locks zu vermeiden.

### SQL-Kommandos

```sql
-- 1. Email Index (CONCURRENTLY für Production)
CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS uq_leads_email_normalized_v2
  ON leads(tenant_id, email_normalized)
  WHERE email_normalized IS NOT NULL
    AND is_canonical = true
    AND status != 'DELETED';

-- 2. Phone Index (CONCURRENTLY für Production)
CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS uq_leads_phone_e164
  ON leads(tenant_id, phone_e164)
  WHERE phone_e164 IS NOT NULL
    AND is_canonical = true
    AND status != 'DELETED';

-- 3. Company Index (CONCURRENTLY für Production)
CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS uq_leads_company_normalized
  ON leads(tenant_id, company_name_normalized)
  WHERE company_name_normalized IS NOT NULL
    AND is_canonical = true
    AND status != 'DELETED';
```

### Ausführung

1. **Timing:** Außerhalb der Stoßzeiten (nachts/Wochenende)
2. **Monitoring:** `pg_stat_progress_create_index` während Build
3. **Dauer:** ~5-15 Min je Index (abhängig von Datenmenge)
4. **Rollback:** `DROP INDEX CONCURRENTLY idx_name;`

### Verification

```sql
-- Prüfe Index-Status
SELECT schemaname, tablename, indexname, indexdef
FROM pg_indexes
WHERE tablename = 'leads'
  AND indexname LIKE '%normalized%';

-- Prüfe Index-Nutzung
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read
FROM pg_stat_user_indexes
WHERE tablename = 'leads';
```

---

## 🔍 Monitoring & Alerts

### Key Metriken

```promql
# 409 Conflict Rate (Duplikate)
rate(leads_conflicts_total[5m])

# Idempotency Hit Rate
rate(idempotency_hits_total[5m]) / rate(lead_create_requests_total[5m])

# Normalisierungs-Fehler
sum by (field) (rate(normalization_errors_total[5m]))

# Index Bloat Check (PostgreSQL)
SELECT
  schemaname,
  tablename,
  indexname,
  pg_size_pretty(pg_relation_size(indexrelid)) AS index_size,
  idx_scan,
  idx_tup_read
FROM pg_stat_user_indexes
WHERE tablename = 'leads'
ORDER BY pg_relation_size(indexrelid) DESC;
```

### Alert Rules

```yaml
# Hohe 409-Rate (viele Duplikate)
- alert: HighLeadConflictRate
  expr: rate(leads_conflicts_total[5m]) > 0.1
  for: 10m
  annotations:
    summary: "Hohe Lead-Duplikat-Rate: {{ $value }} Konflikte/s"

# Idempotency Store Fehler
- alert: IdempotencyStoreErrors
  expr: rate(idempotency_store_errors_total[5m]) > 0
  for: 5m
  annotations:
    summary: "Idempotency Store hat Fehler"

# Index Bloat
- alert: LeadIndexBloat
  expr: pg_index_size_bytes{table="leads"} > 1073741824  # 1GB
  for: 24h
  annotations:
    summary: "Lead-Index größer als 1GB - prüfen auf Bloat"
```

---

## 🛠️ Troubleshooting

### Problem: Hohe 409-Conflict-Rate

**Symptome:** Viele `leads_conflicts_total` Metriken, User-Beschwerden über "Duplikat"-Fehler

**Diagnose:**
```sql
-- Top 10 Konflikt-Felder
SELECT
  CASE
    WHEN email_normalized IS NOT NULL THEN 'email'
    WHEN phone_e164 IS NOT NULL THEN 'phone'
    WHEN company_name_normalized IS NOT NULL THEN 'company'
  END AS conflict_field,
  COUNT(*) as conflict_count
FROM leads
WHERE status != 'DELETED'
  AND is_canonical = true
GROUP BY conflict_field
ORDER BY conflict_count DESC
LIMIT 10;
```

**Lösungen:**
1. **Legitime Duplikate:** User über Duplikat informieren, Lead mergen
2. **Normalisierungs-Bug:** Prüfe `LeadNormalizationService` Logik
3. **Race Conditions:** Idempotency-Keys fehlen → Client-Fix

### Problem: Idempotency Key-Konflikte

**Symptome:** Requests mit gleicher `Idempotency-Key` aber unterschiedlichem Payload

**Diagnose:**
```sql
-- Finde Idempotency-Konflikte
SELECT
  idempotency_key,
  request_hash,
  COUNT(*) as request_count
FROM idempotency_store
WHERE created_at > NOW() - INTERVAL '24 hours'
GROUP BY idempotency_key, request_hash
HAVING COUNT(*) > 1;
```

**Lösungen:**
1. **Client-Fehler:** Client generiert nicht-eindeutige Keys → Client-Fix
2. **Payload-Änderung:** Warn-Log, aber kein Fehler (409 Conflict zurück)

### Problem: Normalisierungs-Performance

**Symptome:** Lead-Creation langsam, `normalization_duration_ms` hoch

**Diagnose:**
```sql
-- Performance-Analyse
EXPLAIN ANALYZE
SELECT normalize_email('Test@Example.COM');

-- Check Function Stats
SELECT
  funcname,
  calls,
  total_time,
  self_time,
  avg_time
FROM pg_stat_user_functions
WHERE funcname LIKE 'normalize_%'
ORDER BY total_time DESC;
```

**Lösungen:**
1. **Regex-Performance:** Prüfe `R__normalize_functions.sql` auf ineffiziente Regex
2. **Caching:** Normalisierungs-Cache für häufige Werte (Redis)

---

## 🔄 Backfill historischer Leads

### Zweck
Normalisierung für bestehende Leads (vor V247) nachziehen.

### SQL-Script

```sql
-- Backfill in Batches (10k Rows)
DO $$
DECLARE
  batch_size INT := 10000;
  processed INT := 0;
BEGIN
  LOOP
    UPDATE leads
    SET
      email_normalized = normalize_email(email),
      phone_e164 = normalize_phone(phone),
      company_name_normalized = normalize_company_name(company_name)
    WHERE id IN (
      SELECT id FROM leads
      WHERE email_normalized IS NULL
         OR phone_e164 IS NULL
         OR company_name_normalized IS NULL
      LIMIT batch_size
    );

    GET DIAGNOSTICS processed = ROW_COUNT;
    EXIT WHEN processed = 0;

    RAISE NOTICE 'Processed % rows', processed;
    PERFORM pg_sleep(0.1);  -- Throttle
  END LOOP;
END$$;
```

### Ausführung

1. **Timing:** Nachts/Wochenende (low traffic)
2. **Monitoring:** `pg_stat_activity` für Lock-Konflikte
3. **Dauer:** ~1-5 Min je 10k Rows
4. **Rollback:** Backup vorher erstellen

### Verification

```sql
-- Prüfe Backfill-Fortschritt
SELECT
  COUNT(*) FILTER (WHERE email_normalized IS NOT NULL) AS email_normalized,
  COUNT(*) FILTER (WHERE phone_e164 IS NOT NULL) AS phone_normalized,
  COUNT(*) FILTER (WHERE company_name_normalized IS NOT NULL) AS company_normalized,
  COUNT(*) AS total_leads
FROM leads
WHERE status != 'DELETED';
```

---

## 📋 Maintenance Tasks

### Wöchentlich
- [ ] Index-Bloat prüfen (`pg_stat_user_indexes`)
- [ ] 409-Conflict-Rate analysieren (Trends)
- [ ] Idempotency-Store Cleanup (>24h alte Einträge)

### Monatlich
- [ ] Normalisierungs-Performance Review
- [ ] VACUUM ANALYZE auf `leads` Tabelle
- [ ] Index-Rebuild bei >30% Bloat

### Quartal
- [ ] Normalisierungs-Logik Review (neue Suffixe, Rechtsformen)
- [ ] Idempotency-TTL Review (24h noch passend?)
- [ ] Deduplizierungs-Metriken Dashboard-Review

---

## 🔗 Verwandte Dokumente

- [Migration V247](../../backend/src/main/resources/db/migration/V247__leads_normalization_deduplication.sql)
- [LeadNormalizationService](../../backend/src/main/java/de/freshplan/modules/leads/service/LeadNormalizationService.java)
- [IdempotencyService](../../backend/src/main/java/de/freshplan/modules/leads/service/IdempotencyService.java)
- [TEST_DEBUGGING_GUIDE.md](../../backend/TEST_DEBUGGING_GUIDE.md)
