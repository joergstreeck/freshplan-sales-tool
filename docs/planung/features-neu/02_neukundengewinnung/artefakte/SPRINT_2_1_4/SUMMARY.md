---
module: "02_neukundengewinnung"
sprint: "2.1.4"
doc_type: "konzept"
status: "approved"
owner: "team/leads-backend"
updated: "2025-10-01"
---

# Sprint 2.1.4 – Artefakte Summary

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → SPRINT_2_1_4 → Summary

## 🎯 Sprint-Ziel
Lead Deduplication & Data Quality – Phase 1: Normalisierung, Soft-Deduplizierung und Idempotenz

## 📋 Deliverables

### **1. Datenbank-Migrationen**
- **V247:** Normalisierungs-Spalten (`email_normalized`, `phone_e164`, `company_name_normalized`)
- **V10012:** CI-only Indizes (non-CONCURRENTLY für schnelle Tests)
  - **Migration-Historie:** Ursprünglich als V248 geplant, verschoben nach V10012 (Test/Dev-Range)
  - **Grund:** V10xxx = Test-only Migrations, werden in Production übersprungen (flyway.ignoreMigrationPatterns)
  - **Inhalt:** UNIQUE Indizes für email/phone/company ohne CONCURRENTLY (CI-Performance: table locks akzeptabel)
  - **Production:** Manuelle INDEX CREATION mit CONCURRENTLY erforderlich (siehe Migration-Kommentare)
- **V251-V254:** Idempotency-Fixes, Events `published` column, Registered-at backdating
- **R__normalize_functions.sql:** Repeatable normalization functions (PostgreSQL)
- **Partielle UNIQUE Indizes:** WHERE status != 'DELETED' für email/phone/company

### **2. Backend-Services**
- **LeadNormalizationService:** 31 Tests, Normalisierung für E-Mail (lowercase), Telefon (E.164), Firma (ohne Suffixe/Rechtsformen)
- **IdempotencyService:** 8 Tests + Integration, 24h TTL, SHA-256 Request-Hash, atomic INSERT … ON CONFLICT
- **LeadResource (erweitert):** Konflikt-Handling mit RFC7807 Problem Details

### **3. API-Erweiterungen**
- **Header:** `Idempotency-Key` Support (optional, SHA-256 falls nicht angegeben)
- **Responses:**
  - 201 Created: Neuer Lead erstellt
  - 200 OK: Idempotente Wiederholung (cached response)
  - 409 Conflict: Duplikat erkannt mit RFC7807 Details (`errors.email`, `errors.phone`, `errors.company`)

### **4. Tests & CI Performance**
- **1196 Tests** in 7m29s (0 Failures) - 70% schneller als vorher (24min)
- **Root Causes behoben:**
  - junit-platform.properties override entfernt (Maven Surefire steuert Parallelität)
  - ValidatorFactory → @BeforeAll static (56s gespart über 8 Test-Klassen)
- **Test-Migration:** @QuarkusTest ↓27% (8 DTO-Tests → Plain JUnit mit Mockito)
- **Dokumentiert:** TEST_DEBUGGING_GUIDE.md mit Performance Patterns

## 📊 Metriken
- `leads_conflicts_total{tenant_id}` - Anzahl Duplikat-Konflikte
- `idempotency_hits_total` - Anzahl idempotente Wiederholungen
- `normalization_errors_total{field}` - Fehler bei Normalisierung

## 📝 Migration Notes

### V248 → V10012 Verschiebung (Test/Dev-Range)

**Ursprüngliche Planung:** V248__leads_unique_indexes_simple.sql

**Finale Implementierung:** V10012__leads_unique_indexes_simple.sql

**Begründung:**
- **V10000-V10003, V10012 = CI-only Migrations:** Werden in Production übersprungen via `flyway.ignoreMigrationPatterns=*:10000,*:10001,*:10003,*:10012` (+ V8001, V8002)
- **WICHTIG:** Nicht alle V10xxx sind Test-only! V10002, V10008-V10011, V10013+ laufen in Production!
- **CONCURRENTLY Problem:** UNIQUE Index-Creation ohne CONCURRENTLY sperrt Tabellen (akzeptabel in Test/CI, NICHT in Production)
- **Production Strategy:** Manuelle Index-Creation mit CONCURRENTLY außerhalb Flyway (Zero-Downtime)

**Auswirkungen:**
- ✅ **CI/Test:** V10012 läuft automatisch, schnelle Index-Creation (~2s statt 30s+)
- ✅ **Production:** V10012 wird übersprungen, DBA führt CONCURRENTLY Indizes manuell durch
- ✅ **Flyway History:** Keine V248 Einträge in `flyway_schema_history` (nie existiert in finaler Form)

**Referenz-Migration:**
```sql
-- V10012__leads_unique_indexes_simple.sql (Zeilen 1-11)
-- ⚠️ WARNING: This migration is for DEV/TEST environments ONLY
-- ⚠️ Creates UNIQUE indexes WITHOUT CONCURRENTLY which locks tables
-- ⚠️ For PRODUCTION deployments:
-- ⚠️   1. Skip this migration (add to flyway.ignoreMigrationPatterns)
-- ⚠️   2. Use manual CONCURRENTLY index creation
```

## 🔗 Verweise
- **Trigger:** [TRIGGER_SPRINT_2_1_4.md](../../../../TRIGGER_SPRINT_2_1_4.md)
- **Backend-Übersicht:** [backend/_index.md](../../backend/_index.md)
- **ADR:** [ADR-002-normalization.md](../../shared/adr/ADR-002-normalization.md)
- **V10012 Migration:** [V10012__leads_unique_indexes_simple.sql](../../../../../backend/src/main/resources/db/migration/V10012__leads_unique_indexes_simple.sql)

## ✅ Definition of Done
- [x] Migration V247 erfolgreich ausgeführt (+ V10012, V251-V254)
- [x] Normalisierung funktioniert für alle Felder (email, phone, company)
- [x] Partielle UNIQUE Constraints verhindern neue Duplikate (WHERE status != 'DELETED')
- [x] Idempotenz-Store aktiv mit 24h TTL (SHA-256 Hash, atomic INSERT … ON CONFLICT)
- [x] RFC7807 409-Responses korrekt mit field-level errors
- [x] Tests grün: 1196 Tests in 7m29s, 0 Failures
- [x] Metriken erfasst (Prometheus-ready)
- [x] CI Performance optimiert: 24min → 7min (70% schneller)
- [x] Dokumentation: TEST_DEBUGGING_GUIDE.md mit Performance Patterns
- [x] PR #123 merged in main