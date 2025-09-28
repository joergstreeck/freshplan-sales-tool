---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "guideline"
status: "approved"
owner: "team/leads"
updated: "2025-09-27"
---

# 🗺️ Sprint-Map – Modul 02 Neukundengewinnung

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Sprint-Map

**Start here:** Dieser Index verweist auf die *zentralen* Sprint-Dokumente (keine Kopien!).

## 🎯 Relevante Sprints

### **Sprint 2.1 – Modul 02 Backend (COMPLETE)**
**Zentral:** [TRIGGER_SPRINT_2_1.md](../../TRIGGER_SPRINT_2_1.md)
**Status:** ✅ 100% COMPLETE
**Ergebnisse:**
- Lead-Management System ohne Gebietsschutz
- Territory-Scoping (Deutschland/Schweiz)
- CQRS Light Integration
- Security Test Pattern, Performance Test Pattern, Event System Pattern

**PRs:** #103, #105, #110 (alle merged)

---

### **Sprint 2.1.1 – P0 Hotfix Integration Gaps (COMPLETE)**
**Zentral:** [TRIGGER_SPRINT_2_1_1.md](../../TRIGGER_SPRINT_2_1_1.md)
**Spezifisches Log:** [SPRINT_2_1_1_DELTA_LOG.md](../../SPRINT_2_1_1_DELTA_LOG.md)
**Status:** ✅ COMPLETE
**Ergebnisse:**
- Event Distribution Pipeline (PostgreSQL LISTEN/NOTIFY)
- Dashboard Widget Integration
- Metrics Implementation (freshplan_* ohne _total suffix)
- FP-235: T+3/T+7 Follow-up Automation
- AFTER_COMMIT Pattern + Idempotenz + RBAC

**PRs:** #111 (merged 2025-09-26)

---

### **Sprint 2.1.2 – Frontend Research (COMPLETE)**
**Modul-Spezifisch:** [frontend/analyse/_index.md](./frontend/analyse/_index.md)
**Status:** ✅ Research Complete (PR #112, Draft)
**Ergebnisse:**
- **INVENTORY.md:** Stack-Analyse + Gaps + Foundation Patterns
- **API_CONTRACT.md:** Event-System + RBAC + REST-Endpoints + Polling Strategy
- **RESEARCH_ANSWERS.md:** 11 offene Fragen beantwortet
- **VALIDATED_FOUNDATION_PATTERNS.md:** Konsolidierte Patterns aus grundlagen/ & infrastruktur/

**Nächster Schritt:** Thin Vertical Slice mit Feature-Flag `VITE_FEATURE_LEADGEN`

---

### **Sprint 2.1.3 – Frontend Implementation (COMPLETE)**
**Zentral:** [TRIGGER_SPRINT_2_1_3.md](../../TRIGGER_SPRINT_2_1_3.md)
**Status:** ✅ COMPLETE
**Ergebnisse:**
- Thin Vertical Slice: `/leads` Route + `LeadList` + `LeadCreateDialog`
- Feature-Flag: `VITE_FEATURE_LEADGEN=true` aktiviert
- Vollständige Business-Logik:
  - Client-seitige Validierung (Name ≥2, E-Mail-Format)
  - Duplikat-Erkennung (409 Response bei gleicher E-Mail)
  - Source-Tracking (`source='manual'`)
  - RFC7807 Error Handling mit Feld-Fehlern
- Vollständige i18n (de/en) ohne hardcoded Strings
- MUI Theme V2 Integration + MainLayoutV2 Wrapper
- Tests: 90% Coverage (Integration Tests für alle Business-Flows)
- MSW für realistische API-Simulation

**PRs:** #122 (merged 2025-09-28)

---

### **Sprint 2.1.4 – Lead Deduplication & Data Quality (COMPLETE)**
**Zentral:** [TRIGGER_SPRINT_2_1_4.md](../../TRIGGER_SPRINT_2_1_4.md)
**Status:** ✅ COMPLETE
**Scope:** Lead Normalization & Deduplication – Phase 1
**Ergebnisse:**
- Normalisierung: E-Mail, Telefon (E.164), Firmennamen
- Soft-Delete mit `is_canonical` Flag
- Idempotency-Key Support für sichere Retries
- Migration V247 (additive-only) + V248 (CONCURRENTLY Index)
- Repeatable Migration R__normalize_functions.sql

**Deliverables:**
- Normalized Fields: `email_normalized`, `phone_e164`, `company_name_normalized`
- Unique Constraints mit WHERE-Klauseln
- Idempotency Store (24h TTL)
- RFC7807 Problem Details für 409

**Artefakte:** [`artefakte/SPRINT_2_1_4/`](./artefakte/SPRINT_2_1_4/)
**PRs:** #123 (ready for review)

---

### **Sprint 2.1.5 – Lead Protection & Progressive Profiling (IN_PROGRESS)**
**Zentral:** [TRIGGER_SPRINT_2_1_5.md](../../TRIGGER_SPRINT_2_1_5.md)
**Status:** 🔧 IN_PROGRESS
**Scope:** Vertragliche Lead-Schutz-Mechanismen + B2B Progressive Profiling

**Sprint-Ziel:**
- 6-Monats-Schutz gemäß Handelsvertretervertrag
- 60-Tage-Aktivitätsstandard mit Warnsystem
- Stop-the-Clock Mechanismus
- Progressive Profiling (3 Stufen: Vormerkung/Registration/Qualifiziert)

**Deliverables:**
- Migration V249: `lead_protection` und `lead_activities` Tabellen
- Migration V250: Protection Trigger und Status-Jobs
- Frontend: LeadWizard, ProtectionBadge, ActivityTimeline
- API: Enhanced POST /api/leads mit 201/202/409 Semantik
- Protection-Endpoints: Reminder, Extend, Stop-Clock, Data-Deletion

**Artefakte:** [`artefakte/SPRINT_2_1_5/`](./artefakte/SPRINT_2_1_5/)
- ✅ CONTRACT_MAPPING.md
- ✅ TEST_PLAN.md
- ✅ RELEASE_NOTES.md
- ✅ CHANGELOG.md
- ✅ QA_CHECKLIST.md
- ✅ OpenAPI Contract: [`analyse/api/leads.openapi.md`](./analyse/api/leads.openapi.md)
- ✅ RBAC ADR: [`shared/adr/ADR-002-rbac-lead-protection.md`](./shared/adr/ADR-002-rbac-lead-protection.md)

**Delta:** Scope geändert von "Matching & Review" zu "Protection & Progressive" (siehe DELTA_LOG_2_1_5.md)

---

### **Sprint 2.1.6 – Lead Transfer & Team Management (PLANNED)**
**Status:** 📅 PLANNED (2025-10-12 - 2025-10-18)
**Scope:** Lead-Übergabe, Team-Management, Merge/Unmerge

**Geplante Features:**
- Lead-Transfer zwischen Partnern mit Genehmigung
- Quotenregelung für Teams
- Fuzzy-Matching & Review-Flow (Scoring, Kandidatenliste, Merge/Reject/Create-New)
- Merge/Unmerge mit Identitätsgraph
- Audit-Historie für alle Transfers
- Team-basierte Sichtbarkeit (RLS Phase 1)

**Note:** Enthält Matching & Review Features (ursprünglich für 2.1.5 geplant)

---

## 🔗 **Cross-Module Dependencies**

### **Infrastruktur-Sprints (relevant für Modul 02):**
- **TRIGGER_SPRINT_1_2.md:** Security Foundation (RBAC + Territory RLS)
- **TRIGGER_SPRINT_1_3.md:** CQRS Foundation (Event-System Basis)
- **TRIGGER_SPRINT_1_5.md:** Performance Foundation (Bundle <200KB)

### **ADRs (Architecture Decisions):**
- **ADR-0002:** PostgreSQL LISTEN/NOTIFY (Event-Architektur)
- **ADR-0004:** Territory RLS vs Lead-Ownership (Security-Modell)

## 📋 **Implementation Timeline**

```yaml
Phase 1 (Backend): Sprint 2.1 + 2.1.1
  Status: ✅ COMPLETE
  Result: Production-ready Lead-Management Backend

Phase 2 (Frontend Research): Sprint 2.1.2
  Status: ✅ COMPLETE
  Result: Vollständige Frontend-Analyse + API-Contract

Phase 3 (Frontend Implementation): Sprint 2.1.3
  Status: ✅ COMPLETE
  Result: Lead Management MVP mit Business-Logik
  PR: #122 (merged)
```

## 🎯 **Für neue Claude-Instanzen**

**Start immer mit den zentralen Trigger-Dokumenten** – sie enthalten die vollständigen Sprint-Kontexte, Cross-Module-Dependencies und Entscheidungshistorie. Diese Sprint-Map ist nur ein **Navigationshelfer**, nicht die Quelle der Wahrheit.