---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "deltalog"
status: "draft"
sprint: "2.1.5"
owner: "team/leads-backend"
updated: "2025-09-28"
---

# Sprint 2.1.5 – Changelog

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5 → Changelog

## [2.1.5] - 2025-10-11 (Unreleased)

### Added
- **Backend**: Lead Protection System mit 6-Monats-Schutzfrist
- **Backend**: Activity Tracking für 60-Tage-Aktivitätsstandard
- **Backend**: Stop-the-Clock Mechanismus mit Audit-Trail
- **Backend**: Automatische Status-Transition (protected → warning → expired)
- **Backend**: Migration V249 für lead_protection Tabellen
- **Backend**: Migration V250 für Protection Trigger und Jobs
- **Backend**: FuzzyMatchingService für Soft-Duplicate Detection
- **Backend**: Idempotency-Key Support für API-Calls
- **Frontend**: LeadWizard Component für Progressive Profiling
- **Frontend**: LeadProtectionBadge für Schutzstatus-Anzeige
- **Frontend**: ActivityTimeline für Progress-Visualisierung
- **Frontend**: DuplicateReviewModal für Kandidaten-Review
- **API**: Enhanced POST /api/leads mit Stage-System (0/1/2)
- **API**: 202 Response für Soft-Duplicates mit Kandidatenliste
- **API**: 409 Response für Hard-Duplicates mit RFC7807 Format
- **DB**: lead_protection Tabelle mit Schutzfrist-Management
- **DB**: lead_activities Tabelle für Progress-Tracking
- **DB**: pg_trgm Extension für Fuzzy-Matching
- **Docs**: CONTRACT_MAPPING.md mit Vertragsreferenzen
- **Docs**: Progressive Profiling Guideline (DSGVO-konform)

### Changed
- **API**: POST /api/leads erwartet neuen Parameter `stage` (Default: 1)
- **API**: Response-Format für Duplikate (202 mit Kandidatenliste statt Boolean)
- **DB**: leads Tabelle erweitert um `source` (Pflichtfeld)
- **DB**: leads Tabelle erweitert um `stage` (0/1/2)
- **Frontend**: Lead-Form auf Wizard-basierte Erfassung umgestellt
- **Backend**: LeadService refactored für Stage-basierte Validierung

### Fixed
- **Backend**: Race Condition bei parallelen Lead-Registrierungen
- **Backend**: Timezone-Handling bei Protection-Deadlines
- **Frontend**: Memory Leak in ActivityTimeline Component
- **Frontend**: Incorrect Date Display bei Stop-the-Clock
- **Tests**: Flaky Tests durch clean-at-start behoben

### Security
- **API**: Input-Validation für alle neuen Stage-Felder
- **API**: Rate-Limiting für Fuzzy-Match API (100/min)
- **Backend**: Audit-Events für alle Protection-Änderungen
- **Frontend**: XSS-Prevention in DuplicateReviewModal

### Performance
- **DB**: Composite Index auf (email_normalized, is_canonical, status)
- **DB**: pg_trgm GIN Index für company_name_normalized
- **API**: Batch-Processing für Protection-Status-Updates
- **Frontend**: Lazy-Loading für ActivityTimeline Events

### Dependencies
- **Added**: pg_trgm PostgreSQL Extension
- **Added**: vue-wizard-component@2.1.0
- **Updated**: flyway-core 9.22.2 → 9.22.3
- **Updated**: @vue/test-utils 2.4.1 → 2.4.3

### Deprecated
- **API**: `POST /api/leads` ohne `stage` Parameter (entfernt in 2.2.0)
- **DB**: `leads.registered_by` Feld (ersetzt durch `source` + `owner_user_id`)

### Technical Debt
- Performance-Optimierung für > 10k aktive Protections ausstehend
- Fuzzy-Match Score-Algorithmus für kurze Namen verbessern
- Stop-the-Clock UI DST-Bug fixen

### Configuration
```yaml
# Neue Feature Flags
lead_protection:
  enabled: true
  warning_days: 7
  batch_size: 100

progressive_profiling:
  enabled: true
  require_stage_0: false
  validate_vat: true

fuzzy_matching:
  enabled: true
  threshold: 0.7
  max_candidates: 5
```

### Database Migrations
- V249__lead_protection_tables.sql
- V250__lead_protection_triggers.sql
- R__protection_helper_functions.sql (NEW Repeatable)

### Breaking Changes
- `POST /api/leads` Parameter `stage` ist erforderlich
- `lead.source` ist Pflichtfeld (Migration setzt Default: 'manual')
- Response-Format für 202 geändert (Objekt statt Boolean)

---

## Previous Releases

### [2.1.4] - 2025-10-04
- Lead Normalization & Deduplication
- Email/Phone/Company Normalisierung
- Soft-Delete mit is_canonical Flag

### [2.1.3] - 2025-09-27
- Initial Lead Management Implementation
- Basic CRUD Operations
- Simple Duplicate Check