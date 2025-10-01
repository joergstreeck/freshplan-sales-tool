---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "deltalog"
status: "approved"
sprint: "2.1.5"
owner: "team/leads-backend"
updated: "2025-10-01"
---

# Sprint 2.1.5 – Delta Log (Scope-Änderung)

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5 → Delta Log

## Scope-Änderung

### Original Scope (vor 2025-09-28)
**Titel:** Fuzzy Matching & Review UI
**Fokus:** Duplikat-Erkennung und Merge-Funktionalität

**Geplante Features:**
- Fuzzy-Matching Algorithmus
- Kandidaten-Review UI
- Merge/Unmerge Operationen
- Match-Score Visualisierung
- Konflikt-Resolution bei Merge

### Neuer Scope (ab 2025-09-28)
**Titel:** Lead Protection & Progressive Profiling (B2B)
**Fokus:** Vertragliche Schutz-Mechanismen und DSGVO-konforme Erfassung

**Neue Features:**
- 6-Monats Lead-Schutz (Vertrag §3.2)
- 60-Tage Aktivitätsstandard (Vertrag §3.3)
- Stop-the-Clock Mechanismus (Vertrag §3.3.2)
- Progressive Profiling (3 Stufen)
- Fuzzy-Matching nur für Soft-Duplicates (reduziert)

## Begründung

### Vertragliche Dringlichkeit
Der Handelsvertretervertrag definiert **verbindliche Lead-Schutz-Regeln**, die technisch umgesetzt werden müssen:
- 6 Monate Schutz ab Registrierung
- 60 Tage Aktivitätspflicht
- Stop-the-Clock bei FreshFoodz-Verzögerungen

Diese Regeln sind **vertraglich verpflichtend** und haben Priorität vor Nice-to-Have Features.

### DSGVO-Anforderungen
Progressive Profiling ermöglicht **Datenminimierung**:
- Stage 0: Keine personenbezogenen Daten (nur Firma/Ort)
- Stage 1: Optional Kontaktdaten
- Stage 2: Vollständige Qualifizierung

Dies entspricht den DSGVO-Prinzipien und reduziert rechtliche Risiken.

### Business Impact
Lead-Schutz ist **geschäftskritisch**:
- Verhindert Partner-Konflikte
- Sichert Provisionsansprüche
- Schafft faire Wettbewerbsbedingungen
- Erhöht Partner-Zufriedenheit

## Verschobene Features

### Nach Sprint 2.1.6
**Matching & Review (erweitert)**
- Vollständiger Fuzzy-Match Algorithmus
- Merge/Unmerge mit Identitätsgraph
- Konflikt-Resolution UI
- Match-Historie und Audit

**Begründung:** Diese Features sind wichtig, aber nicht vertraglich verpflichtend. Sie werden in Sprint 2.1.6 zusammen mit Lead-Transfer implementiert, da beide Features ähnliche UI-Komponenten nutzen.

## Impact-Analyse

### Positive Impacts
- ✅ Vertragliche Compliance erreicht
- ✅ DSGVO-konforme Datenerfassung
- ✅ Reduzierte Partner-Konflikte
- ✅ Klarere Business-Logik

### Negative Impacts
- ⚠️ Merge-Funktionalität verzögert (1 Sprint)
- ⚠️ Vollständiges Fuzzy-Matching verzögert
- ⚠️ Zusätzliche Migration erforderlich (V249/V250)

### Mitigation
- Basis Fuzzy-Matching in 2.1.5 für Soft-Duplicates
- Merge UI-Komponenten können wiederverwendet werden
- Documentation bereits vorbereitet für 2.1.6

## Stakeholder-Kommunikation

### Informierte Stakeholder
- Product Owner: ✅ Approved (2025-09-28)
- Tech Lead: ✅ Approved
- Partner Management: ✅ Informed
- Legal/Compliance: ✅ Confirmed contract alignment

### Kommunikationskanäle
- Slack #team-leads-backend: Announcement gepostet
- JIRA MOD02-215: Scope Update dokumentiert
- Sprint Planning Meeting: Präsentiert am 2025-09-28

## Retention Policy

### Stubs für verschobene Features
Gemäß 2-Sprint-Regel werden Stubs angelegt für:
- `artefakte/SPRINT_2_1_6/matching-review-stub.md`
- `artefakte/SPRINT_2_1_6/merge-unmerge-stub.md`

Diese verweisen auf die neue Implementierung in Sprint 2.1.6.

## Lessons Learned

### Was gut lief
- Frühzeitige Erkennung der vertraglichen Priorität
- Klare Scope-Definition für beide Sprints
- Stakeholder-Alignment erreicht

### Verbesserungspotential
- Vertragliche Anforderungen früher in Sprint-Planung einbeziehen
- Legal Review vor Sprint-Start durchführen
- Buffer für regulatorische Anforderungen einplanen

## Approval

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Product Owner | | 2025-09-28 | ✅ |
| Tech Lead | | 2025-09-28 | ✅ |
| Scrum Master | | 2025-09-28 | ✅ |

## Implementierungs-Entscheidungen (2025-10-01)

### Architektur-Entscheidung: PLAN B (Inline-First)

**Ursprünglich geplant (V249-Artefakt):**
- Separate Tabelle `lead_protection` als Source of Truth
- Komplexe Trigger-Synchronisation mit `leads`-Tabelle
- 300+ Zeilen Migration mit lead_protection + lead_activities + lead_transfers

**ENTSCHIEDEN:**
- ✅ **Inline-Felder in `leads` bleiben Source of Truth**
- ✅ Keine separate `lead_protection`-Tabelle in Sprint 2.1.5
- ✅ V249-Artefakt wird aufgeteilt und angepasst (V255-V257)
- ✅ Additive Migrations (ALTER TABLE only, kein DROP/CREATE)

**Begründung:**
- Lead-Entity hat bereits Protection-Felder (protectionStartAt, protectionMonths, etc.)
- LeadProtectionService nutzt diese Felder aktiv
- Vermeidung von Datenredundanz und Trigger-Komplexität
- Einfacherer Scope für Sprint 2.1.5
- Separate Tabelle kann in 2.1.6+ evaluiert werden (mit ADR)

### Migrations-Struktur: V255-V257

**V255: leads_protection_basics_and_stage.sql**
- ALTER TABLE leads: progress_warning_sent_at, progress_deadline
- ALTER TABLE leads: stage (0..2) für Progressive Profiling

**V256: lead_activities_augment.sql**
- ALTER TABLE lead_activities: counts_as_progress (DEFAULT FALSE - konservativ)
- Neue Felder: summary, outcome, next_action, next_action_date, performed_by
- Backfill performed_by aus user_id

**V257: lead_progress_helpers_and_triggers.sql**
- Functions: calculate_protection_until(), calculate_progress_deadline()
- Trigger: update_progress_on_activity (bei counts_as_progress=true)

**V258: NICHT IMPLEMENTIERT in Sprint 2.1.5**
- lead_transfers Tabelle verschoben nach Sprint 2.1.6
- Grund: Scope-Fokus auf Protection, nicht Transfer

### Datentyp-Entscheidungen

**lead_id:** BIGINT (nicht UUID)
- Lead.id ist Long mit IDENTITY
- Konsistent mit bestehendem Schema

**performed_by:** VARCHAR(50) (nicht UUID)
- User-IDs kommen von Keycloak (OIDC)
- Keine users-Tabelle, kein FK
- Konsistent mit owner_user_id, created_by

**counts_as_progress:** DEFAULT FALSE (nicht TRUE)
- Konservativ: Aktivitäten müssen explizit als Progress markiert werden
- Konsistent mit is_meaningful_contact/resets_timer (beide FALSE)
- Verhindert falsche Progress-Zählungen

### Verschobene Features auf Sprint 2.1.6

**Lead Transfers:**
- Komplette lead_transfers Tabelle
- Transfer-Endpoints
- Approval-Workflow

**Backdating Endpoint:**
- PUT /api/leads/{id}/registered-at
- Grund: Backend-Scope bereits groß
- Felder existieren bereits (registeredAtOverrideReason, etc.)
- Kann bei Bedarf in 2.1.6 nachgeholt werden

**Fuzzy-Matching & Review:**
- Kompletter Matching-Algorithmus
- DuplicateReviewModal.vue
- Merge/Unmerge Operations
- Grund: Bereits im ursprünglichen DELTA_LOG auf 2.1.6 verschoben

### Test-Strategie

**Unit Tests (Mock-only):**
- Stage-Regeln (Input → erwartete Stage)
- Protection-Transitions (Business-Logik ohne DB)
- Validierungen (Backdating, Activity-Regeln)

**Integration Tests (gezielt):**
- Trigger-Pfad: Insert lead_activities → Update leads
- Migrations: V255-V257 erfolgreich
- LIMIT: Nur kritische DB-Artefakte (Functions/Triggers)

**CI-Pipeline:**
- Schritt 1: mvn -Punit (schnell)
- Schritt 2: mvn -Pintegration (klein & zielgerichtet)

### Frontend-Scope

**PHASE 1 (Sprint 2.1.5 - Backend First):**
- Migrations V255-V257
- Entity/Service Anpassungen
- Tests (Unit + gezielte ITs)
- API-Contracts aktualisieren

**PHASE 2 (Sprint 2.1.5 - Frontend Parallel/Follow-up):**
- LeadWizard (Stage 0/1/2)
- ProtectionBadge (Status-Indikator)
- ActivityTimeline (Progress-Tracking)

**Entscheidung:** Backend zuerst fertigstellen, Frontend danach oder parallel

## References

- Handelsvertretervertrag (§3.2, §3.3, §3.3.2)
- DSGVO Art. 5 (Datenminimierung)
- Original Sprint Planning: TRIGGER_SPRINT_2_1_5.md (old version)
- Updated Sprint Planning: TRIGGER_SPRINT_2_1_5.md (current)
- Sprint 2.1.6 Planning: TRIGGER_SPRINT_2_1_6.md (upcoming)
- V249-Artefakt: V249__lead_protection_tables.sql.sprint215 (Materiallager)