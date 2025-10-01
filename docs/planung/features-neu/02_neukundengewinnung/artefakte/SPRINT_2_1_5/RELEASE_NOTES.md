---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "deltalog"
status: "draft"
sprint: "2.1.5"
owner: "team/leads-backend"
updated: "2025-09-28"
---

# Sprint 2.1.5 – Release Notes

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5 → Release Notes

## Version: 2.1.5
**Release Date:** 2025-10-11 (geplant)
**Sprint Duration:** 2025-10-05 - 2025-10-11

## 🎯 Release Highlights

- **Lead Protection System**: 6-Monats-Schutz mit 60-Tage-Aktivitätsstandard
- **Progressive Profiling**: 3-Stufen B2B-Lead-Erfassung (DSGVO-konform)
- **Stop-the-Clock**: Pause-Mechanismus für Schutzfristen
- **Fuzzy Matching UI**: Review-Flow für potentielle Duplikate

## 🚀 New Features

### Lead Protection (Backend)
- Automatische 6-Monats-Schutzfrist bei Lead-Registrierung
- 60-Tage-Aktivitätsstandard mit Warnsystem
- Status-Übergang: `protected` → `warning` → `expired` → `released`
- Stop-the-Clock mit Pflichtgrund und Audit-Trail
- Helper-Funktionen für Deadline-Berechnung

### Progressive Profiling (Frontend)
- **Stage 0**: Vormerkung (Firma + Stadt, keine personenbezogenen Daten)
- **Stage 1**: Registrierung mit optionalem Kontakt
- **Stage 2**: Qualifizierung mit VAT-ID und Volumendaten
- Wizard-basierte Erfassung mit Validierung

### API Enhancements
- Enhanced `POST /api/leads` mit Stage-System
- 201/202/409 Response-Semantik für Duplikat-Handling
- Idempotency-Key Support für sichere Retry-Logik
- Soft-Duplicate Detection mit Score-basiertem Ranking

## 🔧 Technical Changes

### Database
- Migration V249: `lead_protection` und `lead_activities` Tabellen
- Migration V250: Protection-Status Trigger und Jobs
- Neue Indizes für Performance-Optimierung
- pg_trgm für Fuzzy-Matching aktiviert

### Frontend Components
- `LeadWizard.vue` - Progressive 3-Stufen-Form
- `LeadProtectionBadge.vue` - Schutzstatus-Indikator
- `ActivityTimeline.vue` - 60-Tage-Progress-Visualisierung
- `DuplicateReviewModal.vue` - Kandidaten-Review UI

### Backend Services
- `LeadProtectionService` - Schutzfrist-Management
- `LeadActivityTracker` - Progress-Monitoring
- `FuzzyMatchingService` - Duplikat-Erkennung
- `StopTheClockService` - Pausierung mit Audit

## 📊 Performance Improvements

- Fuzzy-Match Response: P95 < 200ms (mit pg_trgm Indizes)
- Batch Protection Check: P95 < 500ms für 1000 Leads
- Frontend Bundle: +12KB (gzipped) für neue Components

## 🐛 Bug Fixes

- Fixed: Race Condition bei parallelen Lead-Registrierungen
- Fixed: Timezone-Handling bei Protection-Deadlines
- Fixed: Memory Leak in Activity-Timeline Component
- Fixed: Incorrect Stop-the-Clock Calculation

## 💔 Breaking Changes

- `POST /api/leads` erwartet neuen Parameter `stage` (Default: 1)
- `lead.source` ist nun Pflichtfeld (Migration setzt Default: 'manual')
- Response-Format geändert für 202 (Kandidatenliste statt Boolean)

## 📚 Documentation

- Contract-Mapping Dokument mit Vertragsreferenzen
- OpenAPI Spezifikation für neue Endpoints
- RBAC/RLS Architecture Decision Record
- Progressive Profiling Guideline (DSGVO-konform)

## 🔐 Security

- Input-Validation für alle neuen Felder
- Rate-Limiting für Fuzzy-Match API (100/min)
- Audit-Events für Stop-the-Clock und Protection-Changes
- RBAC: Manager-Approval für Force-Create bei Duplikaten

## 📦 Dependencies

### Added
- pg_trgm PostgreSQL Extension (für Fuzzy-Matching)
- vue-wizard-component@2.1.0 (Frontend)

### Updated
- flyway-core: 9.22.2 → 9.22.3
- @vue/test-utils: 2.4.1 → 2.4.3

## 🚧 Known Issues

- Stop-the-Clock UI zeigt falsches Datum bei DST-Wechsel
- Performance-Degradation bei > 10k aktiven Protections
- Fuzzy-Match Score nicht optimal für kurze Firmennamen

## 📝 Migration Guide

### For Existing Leads
```sql
-- Batch-Job läuft automatisch bei Deployment
-- Erstellt Protection-Records für alle bestehenden Leads
-- Default: 6 Monate ab jetzt, Status = 'protected'
```

### Feature Flags
```yaml
lead_protection:
  enabled: true
  warning_days: 7

progressive_profiling:
  enabled: true
  require_stage_0: false  # Für Übergang

fuzzy_matching:
  enabled: true
  threshold: 0.7
```

## 🎯 Next Sprint Preview

**Sprint 2.1.6**: Lead Transfer & Team Management
- Lead-Übergabe zwischen Partnern
- Quotenregelung und Genehmigungen
- Merge/Unmerge mit Audit-Historie
- Team-basierte Sichtbarkeit

## 📞 Support

Bei Problemen oder Fragen:
- Slack: #team-leads-backend
- Jira: MOD02-LEADS-2.1.5
- Dokumentation: /docs/planung/features-neu/02_neukundengewinnung/

---

**Generated:** 2025-09-28T10:00:00Z
**Approved by:** [Pending]