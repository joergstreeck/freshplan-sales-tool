# Database Migrations Registry

**📍 Navigation:** Home → Planung → MIGRATIONS.md

**Status:** Living Document - wird automatisch bei jeder neuen Migration aktualisiert

**Zweck:** Zentrale Übersicht aller Datenbank-Migrationen mit Kontext, Sprint-Zuordnung und Abhängigkeiten

---

## 📋 Migration Ranges

| Range | Zweck | Production | Beispiele |
|-------|-------|------------|-----------|
| **V1-V199** | Legacy/Initial Schema | ✅ Ja | V1-V37, V102-V109, V121 |
| **V200-V9999** | Feature Migrations | ✅ Ja | V200+, Sprint 1.x-2.x |
| **V10000-V19999** | Test/Dev-Only | ❌ Nein | V10000-V10012 (CI-only) |
| **V20000-V89999** | Reserved (Future) | ⏸️ TBD | Noch nicht genutzt |
| **V90000-V99999** | DEV-SEED (Development Data) | ❌ Nein | V90001-V90002 (in db/dev-seed/) |
| **R__*** | Repeatable Migrations | ✅ Ja | R__normalize_functions.sql |

**Production Skip Pattern:** `flyway.ignoreMigrationPatterns=*:8001,*:8002,*:10000,*:10001,*:10003,*:10012`

**Ignoriert:**
- V8001-V8002: Dev-only (CQRS events, RLS demo) in `db/dev-migration/`
- V10000-V10001, V10003: CI-only (test data cleanup/monitoring)
- V10012: Test-only (conflicts with V259 business logic)

**NICHT ignoriert (Production-kritisch):**
- V10002: opportunities/audit_trail.is_test_data (TestDataService Infrastructure!)
- V10008-V10011: Production fixes (SEED cleanup, users.is_test_data mit 33 Code-Refs!, settings fixes)
- V10013-V10022: Production migrations (renamed from V272-V280)

---

## 📝 Quick-Template (Copy/Paste für neue Migration)

```markdown
| Field | Value |
|-------|-------|
| **Version** | V{NR} (via ./scripts/get-next-migration.sh) |
| **Beschreibung** | {Kurzbeschreibung} |
| **Sprint** | {X.Y.Z} |
| **Owner** | @{github-user} |
| **PR** | #{pr-nummer} |
| **Rollback** | ✅ Yes / ⚠️ Manual / ❌ No |
| **Risk** | 🟢 Low / 🟡 Medium / 🔴 High |
| **Downtime** | None / Requires window |

**SQL Preview:**
\`\`\`sql
ALTER TABLE {table} ADD COLUMN {column} {type};
\`\`\`

**Rollback SQL:**
\`\`\`sql
ALTER TABLE {table} DROP COLUMN IF EXISTS {column};
\`\`\`
```

**Verwendung:**
1. `./scripts/get-next-migration.sh` → Nächste V-Nummer
2. Migration in Tabelle unten eintragen (nur Version, Beschreibung, Sprint, Status, Notes)
3. Für wichtige Migrations (Sprint 2.1.x+): Owner, PR, Rollback, Risk, Downtime ergänzen
4. `updated` Datum am Dateiende aktualisieren

---

## 🗂️ Migrations Übersicht

**Hinweis:** Spalten Owner/PR/Rollback/Risk/Downtime nur für neueste Migrations (V245+) gepflegt.

### Repeatable Migrations (R__)

| Migration | Beschreibung | Sprint | Abhängigkeiten |
|-----------|--------------|--------|----------------|
| **R__normalize_functions.sql** | PostgreSQL Functions für E-Mail/Telefon/Firma Normalisierung | 2.1.4 | V247 |

---

### V1-V37: Initial Schema & Legacy Migrations

| Version | Beschreibung | Sprint | Status | Notes |
|---------|--------------|--------|--------|-------|
| **V1** | Initial Schema | 1.0 | ✅ Deployed | Basis-Tabellen: users, customers, profiles |
| **V2** | Create User Table | 1.0 | ✅ Deployed | - |
| **V3** | Add User Roles | 1.0 | ✅ Deployed | - |
| **V4** | Create Profile Tables | 1.0 | ✅ Deployed | - |
| **V5** | Create Customer Tables | 1.0 | ✅ Deployed | - |
| **V6** | Add expansion_planned Field | 1.0 | ✅ Deployed | - |
| **V7** | Create Opportunities Table | 1.0 | ✅ Deployed | - |
| **V8** | Add Data Quality Fields | 1.0 | ✅ Deployed | - |
| **V9** | Create Audit Trail | 1.0 | ✅ Deployed | - |
| **V10** | Complete Schema Alignment | 1.0 | ✅ Deployed | - |
| **V11** | Make updated Fields Nullable | 1.0 | ✅ Deployed | - |
| **V12** | Add last_modified Audit Fields | 1.0 | ✅ Deployed | - |
| **V13** | Add Missing Timeline Event Columns | 1.0 | ✅ Deployed | - |
| **V14** | Fix Contact Roles Columns | 1.0 | ✅ Deployed | - |
| **V15** | Add last_contact_date to Contacts | 1.0 | ✅ Deployed | - |
| **V16** | Rename preferred_contact → communication_method | 1.0 | ✅ Deployed | - |
| **V17** | Add Missing Columns for Entities | 1.0 | ✅ Deployed | Duplicate mit V18 |
| **V18** | Add Missing Columns for Entities | 1.0 | ✅ Deployed | Duplicate mit V17 |
| **V19** | Add test_data Flag | 1.0 | ✅ Deployed | - |
| **V20** | Add Customer Search Performance Indices | 1.0 | ✅ Deployed | Duplicate mit V102 |
| **V21** | Fix Audit Trail IP Address Type | 1.0 | ✅ Deployed | Duplicate mit V107 |
| **V22** | Fix Audit Trail Value Columns Type | 1.0 | ✅ Deployed | Duplicate mit V108 |
| **V23** | Add renewal_stage to Opportunity Stage Enum | 1.0 | ✅ Deployed | Duplicate mit V109 |
| **V24** | Add Data Quality Fields | 1.0 | ✅ Deployed | Duplicate mit V8 |
| **V25** | Make updated Fields Nullable | 1.0 | ✅ Deployed | Duplicate mit V11 |
| **V26** | Fix Flyway History | 1.0 | ✅ Deployed | Cleanup Migration |
| **V27** | Create Cost Management Tables | 1.1 | ✅ Deployed | - |
| **V28** | Create Help System Tables | 1.1 | ✅ Deployed | - |
| **V29** | Fix Help System Column Types | 1.1 | ✅ Deployed | - |
| **V30** | Remove Redundant Contact Trigger | 1.1 | ✅ Deployed | - |
| **V31** | Fix PostgreSQL Specific Helpfulness Index | 1.1 | ✅ Deployed | - |
| **V32** | Create Contact Interaction Table | 1.1 | ✅ Deployed | - |
| **V35** | Add Sprint2 Location Fields | 1.2 | ✅ Deployed | - |
| **V36** | Add Sprint2 Pain Points Field | 1.2 | ✅ Deployed | - |
| **V37** | Add Remaining Sprint2 Fields | 1.2 | ✅ Deployed | - |

---

### V102-V121: Early Performance & Feature Migrations

| Version | Beschreibung | Sprint | Status | Notes |
|---------|--------------|--------|--------|-------|
| **V102** | Add Customer Search Performance Indices | 1.0 | ✅ Deployed | Duplicate mit V20 |
| **V103** | Create Permission System Core | 1.2 | ✅ Deployed | - |
| **V105** | Create Opportunity Activities Table | 1.2 | ✅ Deployed | - |
| **V107** | Fix Audit Trail IP Address Type | 1.0 | ✅ Deployed | Duplicate mit V21 |
| **V108** | Fix Audit Trail Value Columns Type | 1.0 | ✅ Deployed | Duplicate mit V22 |
| **V109** | Add renewal_stage to Opportunity Stage Enum | 1.0 | ✅ Deployed | Duplicate mit V23 |
| **V121** | Add children_count to Contacts | 1.2 | ✅ Deployed | - |

---

### V200-V257: Feature Migrations (Sprints 2.x)

| Version | Beschreibung | Sprint | Status | Notes |
|---------|--------------|--------|--------|-------|
| **V200** | Future Features Placeholder | 2.0 | ✅ Deployed | Marker für Sprint 2.x |
| **V201** | Add Missing Contact Fields | 2.0 | ✅ Deployed | - |
| **V202** | Add family_status Column | 2.0 | ✅ Deployed | - |
| **V203** | Add Remaining Contact Fields | 2.0 | ✅ Deployed | - |
| **V204** | Fix customer_locations Table | 2.0 | ✅ Deployed | - |
| **V205** | Fix location_details Type | 2.0 | ✅ Deployed | - |
| **V206** | Fix All JSONB Columns | 2.0 | ✅ Deployed | - |
| **V207** | Fix primary_financing Constraint | 2.0 | ✅ Deployed | - |
| **V208** | Fix contact.is_decision_maker Default | 2.0 | ✅ Deployed | - |
| **V209** | Add Contact Roles | 2.0 | ✅ Deployed | - |
| **V210** | Add Contact Location Assignments | 2.0 | ✅ Deployed | - |
| **V211** | Add Soft Delete Fields | 2.0 | ✅ Deployed | - |
| **V212** | Create Audit Tables | 2.0 | ✅ Deployed | - |
| **V213** | Remove Duplicate Hash Trigger | 2.0 | ✅ Deployed | - |
| **V214** | Alter Audit user_id to String | 2.0 | ✅ Deployed | - |
| **V215** | Add Search Indexes | 2.0 | ✅ Deployed | - |
| **V216** | Add Extended Search Indexes | 2.0 | ✅ Deployed | - |
| **V218** | Fix Audit Trail Trigger | 2.0 | ✅ Deployed | - |
| **V221** | Add Customer Performance Indexes | 2.0 | ✅ Deployed | - |
| **V222** | Create Missing Audit Partitions | 2.0 | ✅ Deployed | - |
| **V223** | Audit Trail Partitions 2025 | 2.0 | ✅ Deployed | - |
| **V224** | Audit Trail Remove Trigger and Function | 2.0 | ✅ Deployed | - |
| **V225** | CQRS Light Foundation | 2.1 | ✅ Deployed | Event Sourcing Basis |
| **V226** | Fix event_type Constraint | 2.1 | ✅ Deployed | - |
| **V227** | Security Context Core | 2.1 | ✅ Deployed | Row-Level Security Foundation |
| **V228** | Settings Registry ETag | 2.1 | ✅ Deployed | - |
| **V229** | Lead Territory Management | 2.1 | ✅ Deployed | Leads-Modul Basis |
| **V230** | Remove Redundant Triggers | 2.1 | ✅ Deployed | Cleanup |
| **V231** | Add Missing @ElementCollection Tables | 2.1 | ✅ Deployed | - |
| **V232** | Campaign Templates Foundation | 2.1 | ✅ Deployed | - |
| **V233** | Territories Add active Column | 2.1 | ✅ Deployed | - |
| **V234** | Lead DoD Requirements | 2.1 | ✅ Deployed | - |
| **V235** | Lead RLS Policies | 2.1 | ✅ Deployed | Row-Level Security für Leads |
| **V236** | Consent Assignment Skeleton | 2.1 | ✅ Deployed | DSGVO Consent Tracking |
| **V237** | Lead Search Performance Indexes | 2.1 | ✅ Deployed | - |
| **V238** | Update activity_type Constraint | 2.1 | ✅ Deployed | - |
| **V239** | Update lead_status Constraint | 2.1 | ✅ Deployed | - |
| **V240** | Lead Email Unique Index | 2.1 | ✅ Deployed | Soft-Constraint (deaktiviert via WHERE false) |
| **V241** | activity_type Constraint: clock_stop | 2.1 | ✅ Deployed | - |
| **V242** | RLS Fail-Closed Policies | 2.1 | ✅ Deployed | Security Hardening |
| **V243** | Update GUC Keys | 2.1 | ✅ Deployed | Security Context Variable Namen |
| **V244** | Cleanup Redundant GRANT | 2.1 | ✅ Deployed | - |
| **V245** | Add Follow-up Tracking | 2.1.3 | ✅ Deployed | FP-235: Follow-up System |
| **V246** | Follow-up Backfill Flags | 2.1.3 | ✅ Deployed | FP-235: Backfill t3/t7 Flags |
| **V247** | Leads Normalization & Deduplication | 2.1.4 | ✅ Deployed | FP-234: Normalisierungs-Spalten |
| **V251** | Idempotency Tenant Unique Forward Fix | 2.1.4 | ✅ Deployed | FP-234: Idempotenz-Store Fix |
| **V252** | Leads registered_at Backdating | 2.1.4 | ✅ Deployed | FP-234: Timestamp Override System |
| **V254** | Events Add published Column | 2.1.4 | ✅ Deployed | FP-234: Event Publishing State |
| **V255** | Leads Protection Basics & Stage | 2.1.5 | ✅ Deployed | @joergstreeck | #124 | ✅ Yes | 🟢 Low | None | FP-235 Protection |
| **V256** | Lead Activities Augment | 2.1.5 | ✅ Deployed | @joergstreeck | #124 | ✅ Yes | 🟢 Low | None | FP-235 Activities |
| **V257** | Lead Progress Helpers & Triggers | 2.1.5 | ✅ Deployed | @joergstreeck | #124 | ✅ Yes | 🟢 Low | None | FP-235 Triggers |
| **V258** | *(SKIPPED - Number reserved but not used)* | - | ⏭️ Skipped | - | - | - | - | - | Migration number gap |
| **V259** | Remove leads company_city unique constraint | 2.1.5 | ✅ Deployed | @joergstreeck | #129 | ✅ Yes | 🟢 Low | None | Soft Collision Policy |
| **V260** | Add Sprint 2.1.5 Activity Types | 2.1.5 | ✅ Deployed | @joergstreeck | #130 | ✅ Yes | 🟢 Low | None | Progressive Profiling Types |
| **V261** | Add customer original_lead_id | 2.1.6 | ✅ Deployed | @joergstreeck | #133 | ✅ Yes | 🟢 Low | None | Lead → Customer Tracking |
| **V262** | Stop-the-Clock Cumulative Pause & Idempotency Infrastructure | 2.1.6 | ✅ Deployed | @joergstreeck | #133 | ✅ Yes | 🟢 Low | None | Phase 2 Review Fix #4 + #2 |
| **V263** | BusinessType Harmonization & CHECK Constraint (Lead) | 2.1.6 | ✅ Deployed | @joergstreeck | TBD | ✅ Yes | 🟢 Low | None | Lead: industry→businessType + CHECK |
| **V264** | Customer.businessType + Data Migration | 2.1.6 | ✅ Deployed | @joergstreeck | TBD | ✅ Yes | 🟢 Low | None | Customer: Industry→BusinessType + Sync |
| **V265** | Add lead pseudonymized_at | 2.1.6 | ✅ Deployed | @joergstreeck | TBD | ✅ Yes | 🟢 Low | None | DSGVO Compliance |
| **V266** | Idempotent Fix customer original_lead_id | 2.1.6 | ✅ Deployed | @joergstreeck | TBD | ✅ Yes | 🟢 Low | None | Idempotency Fix |
| **V267** | Make lead owner_user_id nullable | 2.1.6 | ✅ Deployed | @joergstreeck | TBD | ✅ Yes | 🟢 Low | None | Unassigned Leads Support |
| **V268** | Create outbox_emails table | 2.1.6 Phase 3 | ✅ Deployed | @joergstreeck | TBD | ✅ Yes | 🟢 Low | None | Minimal Outbox Pattern |
| **V269** | Add lead_score column | 2.1.6 Phase 4 | ⚠️ Conflict | @joergstreeck | TBD | ✅ Yes | 🟢 Low | None | Out of Order conflict, applied via V271 |
| **V270** | Fix outbox_emails add failed_at | 2.1.6 Phase 4 | ✅ Deployed | @joergstreeck | TBD | ✅ Yes | 🟢 Low | None | Hotfix V268 Schema Mismatch |
| **V271** | Fix add lead_score column (V269 Hotfix) | 2.1.6 Phase 4 | ✅ Deployed | @joergstreeck | TBD | ✅ Yes | 🟢 Low | None | ADR-006 Phase 2 Scoring (Idempotent) |
| **V272** | *(Reserved - Next available)* | - | 📋 Available | - | - | - | - | - | Use `./scripts/get-next-migration.sh` |

---

### V10013-V10024: Sprint 2.1.6 Phase 5 Migrations (PR #137)

⚠️ **WICHTIG:** Diese Migrationen sind **PRODUCTION-RELEVANT** und werden NICHT ignoriert!

| Version | Beschreibung | Sprint | Owner | PR | Rollback | Risk | Downtime | Notes |
|---------|--------------|--------|-------|-----|----------|------|----------|-------|
| **V10013** | Settings ETag Triggers | 2.1.6 Phase 5 | @joergstreeck | #137 | ✅ Yes | 🟢 Low | None | ETag-Trigger für settings Tabelle (45 Zeilen SQL) |
| **V10014** | Lead Enums (VARCHAR + CHECK) | 2.1.6 Phase 5 | @joergstreeck | #137 | ✅ Yes | 🟢 Low | None | lead_source, business_type, kitchen_size mit CHECK Constraints (312 Zeilen) |
| **V10015** | Add first_contact_documented_at | 2.1.6 Phase 5 | @joergstreeck | #137 | ✅ Yes | 🟢 Low | None | MESSE/TELEFON Pre-Claim Logic (28 Zeilen) |
| **V10016** | Create lead_contacts Table | 2.1.6 Phase 5 | @joergstreeck | #137 | ⚠️ Manual | 🔴 HIGH | None | 26 Felder, 100% Customer Parity, **Daten gehen bei Rollback verloren!** |
| **V10017** | Backward Compatibility Trigger | 2.1.6 Phase 5 | @joergstreeck | #137 | ❌ No | 🔴 HIGH | None | **KRITISCH!** Synchronisiert primary contact → legacy fields. **NIEMALS löschen!** |
| **V10018** | Pain Score Base | 2.1.6 Phase 5 | @joergstreeck | #137 | ✅ Yes | 🟢 Low | None | pain_score INT, urgency_level VARCHAR(20) |
| **V10019** | Pain Fields Part 1 | 2.1.6 Phase 5 | @joergstreeck | #137 | ✅ Yes | 🟢 Low | None | 4 Pain-Felder (quality_issues, cost_pressure, time_pressure, compliance_gaps) |
| **V10020** | Pain Fields Part 2 | 2.1.6 Phase 5 | @joergstreeck | #137 | ✅ Yes | 🟢 Low | None | 4 Pain-Felder (scalability_limits, staff_turnover, system_integration, market_competition) |
| **V10021** | Estimated Volume | 2.1.6 Phase 5 | @joergstreeck | #137 | ✅ Yes | 🟢 Low | None | estimated_volume INT |
| **V10022** | Make territory_id Nullable | 2.1.6 Phase 5 | @joergstreeck | #137 | ✅ Yes | 🟠 MEDIUM | None | Fixes Lead creation validation error, ermöglicht unassigned Leads |
| **V10023** | Lead Contacts Constraints | 2.1.6 Phase 5 | @joergstreeck | #137 | ✅ Yes | 🟢 Low | None | Unique constraint für is_primary per lead_id |
| **V10024** | Lead Scoring Complete | 2.1.6 Phase 5 | @joergstreeck | #137 | ✅ Yes | 🟠 MEDIUM | None | 5 Score-Felder (pain_score, revenue_score, fit_score, engagement_score, lead_score) |

**Rollback Scripts:**

**Phase 1 (V10013-V10015) - Einfach:**
```sql
ALTER TABLE leads DROP COLUMN IF EXISTS first_contact_documented_at;
-- V10014: Enums bleiben (Breaking Change vermeiden)
-- V10013: Triggers bleiben (harmlos)
```

**Phase 2 (V10016-V10017) - KRITISCH! ⚠️**
```sql
-- ⚠️ WARNUNG: DATEN GEHEN VERLOREN!
DROP TRIGGER IF EXISTS sync_primary_contact_to_lead_trigger ON lead_contacts;
DROP FUNCTION IF EXISTS sync_primary_contact_to_lead();
DROP TABLE IF EXISTS lead_contacts CASCADE;

-- Legacy contact_person bleibt erhalten (aus Backup)
```

**Alternative (Daten erhalten):**
- ❌ Keine Migration zurückrollen
- ✅ Frontend-Feature-Flag deaktivieren
- ✅ Trigger läuft weiter (keine Breaking Changes)

**Phase 3 (V10018-V10024) - Einfach:**
```sql
ALTER TABLE leads ALTER COLUMN territory_id SET NOT NULL;

UPDATE leads SET
  pain_score = NULL,
  revenue_score = NULL,
  fit_score = NULL,
  engagement_score = NULL,
  lead_score = NULL;
```

---

### V10000-V10012: Test/Dev-Only Migrations (SKIPPED in Production)

⚠️ **Production:** Folgende CI-only Migrations werden übersprungen: V10000, V10001, V10003, V10012

**Pattern:** `ignoreMigrationPatterns=*:10000,*:10001,*:10003,*:10012` (+ V8001, V8002 in dev-migration/)

**WICHTIG:** V10002, V10008-V10011, V10013-V10022 sind **Production-relevant** und werden NICHT ignoriert!

| Version | Beschreibung | Sprint | Notes |
|---------|--------------|--------|-------|
| **V10000** | Cleanup Test Data in CI | 2.1 | CI-only: DELETE WHERE test_data = true |
| **V10001** | Test Data Contract Guard | 2.1 | CI-only: CONSTRAINT CHECK für test_data |
| **V10002** | Ensure Unique Constraints (opportunities/audit_trail.is_test_data) | 2.1.6 | ✅ Production-KRITISCH: TestDataService Infrastructure |
| **V10003** | Test Data Dashboard | 2.1 | CI-only: Statistik-View für Tests |
| **V10005** | Seed Sample Customers | 2.1 | ❌ DELETED (2025-09-28, commit 753c9272c - caused CI failures) |
| **V10008** | Remove Seed Protection | 2.1.6 | ✅ Production: Cleanup (Konsistenz) |
| **V10009** | Add test_data Flag to Users | 2.1.6 | ✅ Production-KRITISCH: 33 Code-Referenzen! |
| **V10010** | Fix Settings scope_type | 2.1.6 | ✅ Production: Enum Constraint Fix |
| **V10011** | Fix Settings ETag Functions | 2.1.6 | ✅ Production: Function Signature Fix |
| **V10012** | Leads UNIQUE Indexes (Simple) | 2.1.4 | **CI-only:** UNIQUE Indizes OHNE CONCURRENTLY |

#### V10012 Details (ehem. V248)

**Migration-Historie:** Ursprünglich als V248 geplant, verschoben nach V10012 (Test/Dev-Range)

**Grund für Verschiebung:**
- UNIQUE Index-Creation OHNE `CONCURRENTLY` → Sperrt Tabellen (OK in CI, NICHT in Production)
- V10xxx Range wird in Production automatisch übersprungen
- Production: Manuelle INDEX CREATION mit `CONCURRENTLY` außerhalb Flyway erforderlich

**Inhalt:**
```sql
-- uq_leads_email_canonical_v2 (email_normalized, WHERE is_canonical AND status != 'DELETED')
-- ui_leads_phone_e164 (phone_e164, WHERE is_canonical AND status != 'DELETED')
-- ui_leads_company_city (company_name_normalized + city, WHERE is_canonical AND status != 'DELETED')
```

**Production Deployment:**
```sql
-- Manuell außerhalb Flyway ausführen:
CREATE UNIQUE INDEX CONCURRENTLY uq_leads_email_canonical_v2 ON leads(email_normalized)
  WHERE email_normalized IS NOT NULL AND is_canonical = true AND status != 'DELETED';

CREATE UNIQUE INDEX CONCURRENTLY ui_leads_phone_e164 ON leads(phone_e164)
  WHERE phone_e164 IS NOT NULL AND is_canonical = true AND status != 'DELETED';

CREATE UNIQUE INDEX CONCURRENTLY ui_leads_company_city ON leads(company_name_normalized, city)
  WHERE company_name_normalized IS NOT NULL AND city IS NOT NULL AND is_canonical = true AND status != 'DELETED';
```

**Referenz:** [Sprint 2.1.4 SUMMARY.md](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_4/SUMMARY.md#-migration-notes)

---

### V90000-V99999: DEV-SEED Migrations (Development Data)

⚠️ **Zweck:** Realistische Testdaten für lokale Entwicklung (NICHT für CI-Tests, NICHT für Production!)

**Folder:** `backend/src/main/resources/db/dev-seed/`

**Strategie:** Separate Migrations für production-ähnliche Entwicklungsdaten, die beim lokalen `./mvnw quarkus:dev` automatisch geladen werden.

**Im Gegensatz zu:**
- **V10xxx (CI-Tests):** TestDataBuilder-Pattern in @QuarkusTest (programmatisch)
- **V90xxx (DEV-SEED):** SQL-Migrations für manuelles Testen im Browser

| Version | Beschreibung | Sprint | Notes |
|---------|--------------|--------|-------|
| **V90001** | Seed DEV Customers Complete | 2.1.6.2 | 5 realistische Customers (IDs 90001-90005): Hotel, Catering, Betriebskantine, Restaurant, Bäckerei |
| **V90002** | Seed DEV Leads Complete | 2.1.6.2 | 10 Leads (IDs 90001-90010) + 21 Contacts + 21 Activities, Score-Range 21-59, Edge Cases: PreClaim, Grace Period, LOST |

**Details:**
- **V90001:** 5 Customers mit vollständigen Daten (Adressen, Kontakte, Notes, BusinessTypes: GASTRONOMIE, CATERING, etc.)
- **V90002:** 10 Lead-Szenarien mit verschiedenen Stati (REGISTERED, LEAD, LOST), Hot Leads: 90003 (Score 59), 90007 (Score 57)

**Verwendung:**
```bash
# Automatisch geladen bei lokalem Dev-Server
./mvnw quarkus:dev

# Datenbank neu aufsetzen (DEV-SEED wird automatisch ausgeführt)
PGPASSWORD=freshplan123 psql -h localhost -U freshplan_user -d freshplan_db -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
./mvnw flyway:migrate
```

**Production Deployment:**
- ✅ DEV-SEED Migrations werden in Production **automatisch übersprungen** (Flyway prüft Folder)
- ✅ Oder explizit via `flyway.locations=classpath:db/migration` (ohne db/dev-seed)

**Dokumentation:**
- [DEV-SEED README](../../backend/src/main/resources/db/dev-seed/README.md) - Vollständige Strategie-Dokumentation
- [DEV_SEED_INFRASTRUCTURE_SUMMARY.md](features-neu/00_infrastruktur/migrationen/artefakte/DEV_SEED_INFRASTRUCTURE_SUMMARY.md) - Session-Details (2025-10-13)

**Geschichte:**
- **V10005 (OBSOLETE):** "Seed Sample Customers" wurde gelöscht (2025-09-28, commit 753c9272c - caused CI failures)
- **V90001-V90002 (AKTUELL):** Neue DEV-SEED Strategie (2025-10-13), separate Folder, bessere Organisation

---

## 📊 Migration Dependencies

### Sprint 2.1.4 (Lead Deduplication)
```
V247 (Normalisierungs-Spalten)
  ↓
R__normalize_functions.sql (Normalisierungs-Functions)
  ↓
V10012 (UNIQUE Indizes, CI-only) → Production: Manuelle CONCURRENTLY Creation
  ↓
V251 (Idempotenz-Fix)
  ↓
V252 (Backdating System)
  ↓
V254 (Event Publishing State)
```

### Sprint 2.1.5 (Lead Protection & Progressive Profiling)
```
V255 (Protection Felder + stage)
  ↓
V256 (Lead Activities Augmentation)
  ↓
V257 (DB Functions + Triggers)
```

### Sprint 2.1.6 Phase 1-4 (Admin APIs & BusinessType Harmonization)
```
V261 (Customer.originalLeadId)
  - Lead → Customer conversion tracking
  - Soft reference (no FK constraint)
  ↓
V262 (Stop-the-Clock Cumulative Pause + Idempotency Infrastructure)
  - progress_pause_total_seconds (BIGINT)
  - import_jobs table (Idempotency-Key Tracking)
  ↓
V263 (BusinessType Harmonization - Lead)
  - Migrate lowercase → uppercase (restaurant → RESTAURANT, etc.)
  - CHECK constraint: 9 unified values
  - EnumResource.java: GET /api/enums/business-types
  ↓
V264 (BusinessType Harmonization - Customer)
  - Customer.industry → Customer.businessType Migration
  - Data Migration + Auto-Sync Setter
  ↓
V265-V271 (Lead Scoring Phase 4)
  - V265: pseudonymized_at
  - V266: Idempotent fixes
  - V267: owner_user_id nullable
  - V268: outbox_emails
  - V269-V271: lead_score (mit V269 conflict fix)
```

### Sprint 2.1.6 Phase 5 (Multi-Contact + Lead Scoring + Security) ✅ PR #137
```
V10013 (Settings ETag Triggers)
  ↓
V10014 (Lead Enums - VARCHAR + CHECK)
  - lead_source, business_type, kitchen_size
  - CHECK Constraints (nicht PostgreSQL ENUM Type!)
  ↓
V10015 (first_contact_documented_at)
  - MESSE/TELEFON Pre-Claim Logic
  ↓
V10016 (lead_contacts Table) 🔴 KRITISCH
  - 26 Felder, 100% Customer Parity
  - Primary Contact Management
  ↓
V10017 (Backward Compatibility Trigger) 🔴 KRITISCH - NIEMALS LÖSCHEN!
  - Synchronisiert primary contact → legacy fields
  - Breaking Change wenn gelöscht!
  ↓
V10018-V10021 (Pain Scoring V3)
  - V10018: pain_score + urgency_level
  - V10019: 4 Pain-Felder (quality_issues, cost_pressure, ...)
  - V10020: 4 Pain-Felder (scalability_limits, staff_turnover, ...)
  - V10021: estimated_volume
  ↓
V10022 (territory_id nullable)
  - Fixes Lead creation validation error
  ↓
V10023 (lead_contacts Constraints)
  - Unique is_primary per lead_id
  ↓
V10024 (Lead Scoring Complete)
  - 5 Score-Felder (pain, revenue, fit, engagement, lead_score)
```

**PR #137 Details:**
- 50 Commits, 3 Wochen Entwicklung
- 125 Files changed (+17.930/-1.826 LOC)
- Tests: 31/31 LeadResourceTest + 10/10 Security Tests GREEN
- Performance: N+1 Query Fix (7x faster), Score Caching (90% weniger DB-Writes)
- Security: 5 Layer (Rate Limiting, Audit Logs, XSS, Error Disclosure, HTTP Headers)

### Sprint 2.1.6.1 (Enum-Migration Phase 2+3)
```
V27X (Customer BusinessType Migration) [PLANNED]
  - Customer.industry → Customer.businessType (9 Werte harmonisiert)
  - Dual-Mode: Auto-Sync Setter (industry ↔ businessType)
  - Data Migration: Industry → BusinessType Mapping
  - Frontend: CustomerEditDialog nutzt useBusinessTypes()
  ↓
V27Y-V281 (CRM-weit Enum-Harmonisierung) [PLANNED]
  - V27Y: ActivityType erweitern (SAMPLE_REQUEST, CONTRACT_SIGNED, etc.)
  - V27Z: OpportunityStatus Enum (LEAD, QUALIFIED, PROPOSAL, NEGOTIATION, WON, LOST)
  - V280: PaymentMethod Enum (SEPA_LASTSCHRIFT, KREDITKARTE, RECHNUNG)
  - V281: DeliveryMethod Enum (STANDARD, EXPRESS, SAMEDAY, PICKUP)
```

**Artefakt:** [ENUM_MIGRATION_STRATEGY.md](features-neu/02_neukundengewinnung/artefakte/ENUM_MIGRATION_STRATEGY.md)

---

## 🔧 Migration Commands

### Nächste Migration-Nummer ermitteln
```bash
./scripts/get-next-migration.sh
# Output: V258
```

### Migration erstellen
```bash
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
touch backend/src/main/resources/db/migration/${MIGRATION}__description.sql
```

### Flyway Repair (bei Schema History Corruption)
```bash
cd backend
./mvnw flyway:repair
```

### Flyway Info (Migration Status)
```bash
cd backend
./mvnw flyway:info
```

---

## 📝 Migration Guidelines

### 1. Naming Convention
```
V{VERSION}__{DESCRIPTION}.sql

Beispiele:
✅ V258__add_user_timezone.sql
✅ V10013__test_data_cleanup.sql
❌ V258_add-user-timezone.sql (Unterstrich statt Bindestrich!)
❌ v258__add_user_timezone.sql (Großbuchstabe V!)
```

### 2. Header Template
```sql
-- V{VERSION}__{DESCRIPTION}.sql
-- Sprint {SPRINT}: {FEATURE_NAME}
-- {TICKET_ID}: {TICKET_DESCRIPTION}
--
-- Kontext:
-- - {CONTEXT_LINE_1}
-- - {CONTEXT_LINE_2}
--
-- Migration: {ADDITIVE/BREAKING/CLEANUP}
```

### 3. CONCURRENTLY Rules

**CI/Test (V10xxx):**
- ✅ CREATE INDEX (ohne CONCURRENTLY) → Schnell, Locks OK

**Production (V200-V9999):**
- ✅ CREATE INDEX CONCURRENTLY (in Migration) → Zero-Downtime
- ⚠️ Wenn CONCURRENTLY nicht möglich → V10xxx + Manuelle Production Creation

### 4. Test-Only Migrations
- **Range:** V8000-V8999 (dev-migration/), V10000-V10099 (migration/, CI-only)
- **Production Skip:** `flyway.ignoreMigrationPatterns=*:8001,*:8002,*:10000,*:10001,*:10003,*:10012`
- **WICHTIG:** Nicht alle V10xxx sind Test-only! V10002, V10008-V10011, V10013+ sind Production!
- **Use Cases:** Test Data Cleanup, CI-Performance Indices, Dev Debugging Views
- **Prozess:** Siehe Section "Test-Migrationen erstellen" unten

### 5. Repeatable Migrations
- **Naming:** `R__{DESCRIPTION}.sql`
- **Idempotenz:** MUSS idempotent sein (CREATE OR REPLACE FUNCTION, etc.)
- **Versionierung:** Flyway nutzt Checksum für Änderungserkennung

### 6. Test-Migrationen erstellen

**Wann Test-Only Migrationen verwenden:**
- **Dev-only:** Debugging, Demo-Daten, RLS-Tests → V8000-V8999 in `db/dev-migration/`
- **CI-only:** Test Data Cleanup, Performance-Checks → V10000-V10099 in `db/migration/`

**⚠️ WICHTIG:** Nicht jede V10xxx Migration ist Test-only! V10002, V10008-V10011, V10013+ sind Production!

**Prozess:**

1. **Lege Migration in korrekten Ordner:**
   ```bash
   # Dev-only (V8xxx)
   touch backend/src/main/resources/db/dev-migration/V8003__your_debug_feature.sql

   # CI-only (V100xx) - nur wenn wirklich Test-only!
   MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
   touch backend/src/main/resources/db/migration/${MIGRATION}__your_ci_test.sql
   ```

2. **Füge zu ignoreMigrationPatterns hinzu:**
   ```properties
   # backend/src/main/resources/application.properties (Zeile 64)
   quarkus.flyway.ignoreMigrationPatterns=...,*:NEUE_NUMMER
   ```

3. **Teste lokal:**
   ```bash
   ./mvnw flyway:info  # Prüfe: Migration hat Status "Ignored"
   ./mvnw test         # Prüfe: Tests sind grün
   ```

4. **Dokumentiere in diesem File:**
   - Trage Migration in Section "V10000-V10012: Test/Dev-Only Migrations" ein
   - Begründe, warum Test-only (z.B. "CI-only: Cleanup logic")

**Beispiel:**
```sql
-- V10050__cleanup_old_test_data.sql
-- CI-only: Delete test data older than 30 days
-- ⚠️  WICHTIG: Muss zu application.properties ignoreMigrationPatterns hinzugefügt werden!

DELETE FROM customers WHERE is_test_data = true AND created_at < NOW() - INTERVAL '30 days';
```

---

## 🔗 Referenzen

- **Master Plan:** `/docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md`
- **Sprint 2.1.4 Summary:** `/docs/planung/features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_4/SUMMARY.md`
- **Sprint 2.1.5 ADR-004:** `/docs/planung/features-neu/02_neukundengewinnung/shared/adr/ADR-004-lead-protection-inline-first.md`
- **Flyway Docs:** https://flywaydb.org/documentation/

---

**Letzte Aktualisierung:** 2025-10-11 (V10013-V10024 dokumentiert, PR #137 erstellt)

**Nächste Migration:** V272 oder V10025+ (ermitteln via `./scripts/get-next-migration.sh`)

**Aktuelle PR:** #137 - Sprint 2.1.6 Phase 5 (READY FOR REVIEW)
**Branch:** feature/mod02-sprint-2.1.6-enum-migration-phase-1
