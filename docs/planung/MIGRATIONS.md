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
| **V20000+** | Reserved (Future) | ⏸️ TBD | Noch nicht genutzt |
| **R__*** | Repeatable Migrations | ✅ Ja | R__normalize_functions.sql |

**Production Skip Pattern:** `flyway.ignoreMigrationPatterns=*:10*,*:11*,*:12*,*:13*,*:14*,*:15*,*:16*,*:17*,*:18*,*:19*`

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

---

### V10000-V10012: Test/Dev-Only Migrations (SKIPPED in Production)

⚠️ **Production:** Diese Migrations werden übersprungen via `flyway.ignoreMigrationPatterns=*:10*`

| Version | Beschreibung | Sprint | Notes |
|---------|--------------|--------|-------|
| **V10000** | Cleanup Test Data in CI | 2.1 | CI-only: DELETE WHERE test_data = true |
| **V10001** | Test Data Contract Guard | 2.1 | CI-only: CONSTRAINT CHECK für test_data |
| **V10002** | Ensure Unique Constraints | 2.1 | CI-only: Test-Validierung |
| **V10003** | Test Data Dashboard | 2.1 | CI-only: Statistik-View für Tests |
| **V10005** | Seed Sample Customers | 2.1 | DISABLED (.disabled Extension) |
| **V10008** | Remove Seed Protection | 2.1 | CI-only: Cleanup |
| **V10009** | Add test_data Flag to Users | 2.1 | CI-only: users.test_data Column |
| **V10010** | Fix Settings scope_type | 2.1 | CI-only: Enum Constraint Fix |
| **V10011** | Fix Settings ETag Functions | 2.1 | CI-only: Function Signature Fix |
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

### Sprint 2.1.6 (Admin APIs & BusinessType Harmonization)
```
V261 (Customer.originalLeadId)
  - Lead → Customer conversion tracking
  - Soft reference (no FK constraint)
  ↓
V262 (Stop-the-Clock Cumulative Pause + Idempotency Infrastructure)
  - progress_pause_total_seconds (BIGINT)
  - import_jobs table (Idempotency-Key Tracking)
  ↓
V263 (BusinessType Harmonization)
  - Migrate lowercase → uppercase (restaurant → RESTAURANT, etc.)
  - CHECK constraint: 9 unified values
  - EnumResource.java: GET /api/enums/business-types
```

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
- **Range:** V10000-V19999
- **Production Skip:** `flyway.ignoreMigrationPatterns=*:10*,...`
- **Use Cases:** Test Data Cleanup, CI-Performance Indices, Dev Debugging Views

### 5. Repeatable Migrations
- **Naming:** `R__{DESCRIPTION}.sql`
- **Idempotenz:** MUSS idempotent sein (CREATE OR REPLACE FUNCTION, etc.)
- **Versionierung:** Flyway nutzt Checksum für Änderungserkennung

---

## 🔗 Referenzen

- **Master Plan:** `/docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md`
- **Sprint 2.1.4 Summary:** `/docs/planung/features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_4/SUMMARY.md`
- **Sprint 2.1.5 ADR-004:** `/docs/planung/features-neu/02_neukundengewinnung/shared/adr/ADR-004-lead-protection-inline-first.md`
- **Flyway Docs:** https://flywaydb.org/documentation/

---

**Letzte Aktualisierung:** 2025-10-06 (V264, Sprint 2.1.6 Phase 2 - BusinessType Harmonization)

**Nächste Migration:** (ermitteln via `./scripts/get-next-migration.sh`)
