---
module: "shared"
domain: "shared"
doc_type: "guideline"
status: "approved"
owner: "team/leads"
updated: "2025-10-02"
---

# 📋 Documentation Maintenance Checklist

**📍 Navigation:** Home → Planung → DOCUMENTATION_CHECKLIST.md

**Zweck:** Checkliste für alle Dokumentations-Updates - Welche Dokumente müssen bei welchen Änderungen aktualisiert werden?

---

## 🎯 Verwendung

**Wann nutzen?**
- ✅ Vor jedem Sprint-Abschluss
- ✅ Nach jeder neuen Migration
- ✅ Nach größeren Architekturentscheidungen (ADRs)
- ✅ Bei Modul-Erweiterungen
- ✅ Vor Pull Request Merge

**Wie nutzen?**
1. Identifiziere Änderungs-Typ (siehe Checklisten unten)
2. Arbeite Checkliste ab
3. Verlinke alle aktualisierten Dokumente im PR

---

## ✅ Checkliste: Neue Datenbank-Migration

**Wann:** Neue Migration V*** oder R__*** erstellt

### Pflicht-Updates:

- [ ] **`/docs/planung/MIGRATIONS.md`**
  - [ ] Neue Migration in Tabelle eintragen (Version, Beschreibung, Sprint, Status)
  - [ ] Sprint-Zuordnung aktualisieren
  - [ ] Migration Dependencies aktualisieren (falls relevant)
  - [ ] "Letzte Aktualisierung" Datum aktualisieren
  - [ ] "Nächste Migration" Nummer aktualisieren

- [ ] **Sprint-Trigger-Dokument** (z.B. `TRIGGER_SPRINT_2_1_5.md`)
  - [ ] Migration in Sprint-Ziel erwähnen (falls relevant)
  - [ ] Warnhinweis-Box aktualisieren mit neuer Versionsnummer

- [ ] **Sprint-Artefakte** (`artefakte/SPRINT_X_Y_Z/SUMMARY.md`)
  - [ ] Migration unter "Datenbank-Migrationen" auflisten
  - [ ] Definition of Done: Migration-Checkbox hinzufügen

- [ ] **Modul Backend `_index.md`** (z.B. `02_neukundengewinnung/backend/_index.md`)
  - [ ] "Current Sprint" Sektion aktualisieren
  - [ ] Migration unter "Datenmodell-Erweiterungen" dokumentieren (falls relevant)

### Optional (bei Breaking Changes):

- [ ] **ADR erstellen** (wenn Architekturentscheidung getroffen wurde)
  - Pfad: `features-neu/{modul}/shared/adr/ADR-XXX-{titel}.md`

- [ ] **Contract-Mapping aktualisieren** (falls vertragliche Anforderungen)
  - Pfad: `features-neu/{modul}/shared/CONTRACT_MAPPING.md`

---

## ✅ Checkliste: Neuer Sprint gestartet

**Wann:** Neuer Sprint beginnt (z.B. Sprint 2.1.6)

### Pflicht-Updates:

- [ ] **`/docs/planung/TRIGGER_INDEX.md`**
  - [ ] Neuen Sprint in Tabelle eintragen
  - [ ] Status des vorherigen Sprints auf "complete" setzen
  - [ ] Aktuelle Sprint-Nummer aktualisieren

- [ ] **Neues Trigger-Dokument erstellen** (`TRIGGER_SPRINT_X_Y_Z.md`)
  - [ ] YAML Front-Matter mit allen Pflichtfeldern
  - [ ] Breadcrumbs: `**📍 Navigation:** Home → Planung → Sprint X.Y.Z`
  - [ ] Warnhinweise (Test-Strategie, Migrationen) kopieren/anpassen
  - [ ] Arbeitsanweisung mit entry_points
  - [ ] Sprint-Ziel definieren
  - [ ] User Stories mit Akzeptanzkriterien
  - [ ] Definition of Done

- [ ] **Modul `SPRINT_MAP.md`** aktualisieren
  - [ ] Neuen Sprint in Tabelle eintragen
  - [ ] Links zu neuem Trigger-Dokument

- [ ] **Modul `_index.md`** aktualisieren
  - [ ] Status-Dashboard aktualisieren (aktueller Sprint)
  - [ ] "Current Sprint" Sektion aktualisieren

- [ ] **Neues Artefakte-Verzeichnis** erstellen
  - Pfad: `features-neu/{modul}/artefakte/SPRINT_X_Y_Z/`
  - [ ] `SUMMARY.md` (Template kopieren von vorherigem Sprint)
  - [ ] `TEST_PLAN.md` (falls relevant)

### Optional:

- [ ] **`/docs/planung/PRODUCTION_ROADMAP_2025.md`** aktualisieren
  - Sprint-Meilensteine anpassen

---

## ✅ Checkliste: Neue Architecture Decision Record (ADR)

**Wann:** Wichtige Architekturentscheidung getroffen

### Pflicht-Updates:

- [ ] **Neue ADR erstellen**
  - Pfad: `features-neu/{modul}/shared/adr/ADR-XXX-{titel}.md`
  - [ ] YAML Front-Matter (module, domain: "shared", doc_type: "adr", status, sprint, owner, decision_date, updated)
  - [ ] Breadcrumbs
  - [ ] Status (ACCEPTED/REJECTED/SUPERSEDED)
  - [ ] Context, Decision, Consequences, Alternatives Considered

- [ ] **Modul `backend/_index.md`** (falls Backend-ADR)
  - [ ] ADR unter "Architecture Decision Records" verlinken

- [ ] **Sprint-Artefakte `SUMMARY.md`**
  - [ ] ADR unter "Verweise" auflisten

- [ ] **`/docs/planung/MIGRATIONS.md`** (falls DB-relevante ADR)
  - [ ] ADR in Referenzen-Sektion verlinken

### Optional:

- [ ] **`CRM_COMPLETE_MASTER_PLAN_V5.md`** aktualisieren
  - ADR in Decisions-Sektion verlinken

---

## ✅ Checkliste: Sprint abgeschlossen

**Wann:** Sprint erfolgreich abgeschlossen, PR gemerged

### Pflicht-Updates:

- [ ] **Sprint-Trigger-Dokument** (z.B. `TRIGGER_SPRINT_2_1_5.md`)
  - [ ] Status auf "complete" setzen
  - [ ] `pr_refs` mit gemergter PR-Nummer aktualisieren
  - [ ] `updated` Datum aktualisieren

- [ ] **Sprint-Artefakte `SUMMARY.md`**
  - [ ] Definition of Done: Alle Checkboxen abhaken
  - [ ] Status auf "approved" setzen
  - [ ] Metriken eintragen (Test-Ergebnisse, Performance, Coverage)
  - [ ] `updated` Datum aktualisieren

- [ ] **`/docs/planung/TRIGGER_INDEX.md`**
  - [ ] Sprint-Status auf "✅ Complete" setzen
  - [ ] PR-Nummer verlinken

- [ ] **Modul `_index.md`**
  - [ ] Status-Dashboard aktualisieren (z.B. "Backend: ✅ Production")
  - [ ] "Current Sprint" auf nächsten Sprint aktualisieren

- [ ] **Modul `SPRINT_MAP.md`**
  - [ ] Sprint-Zeile: Status auf "Complete" setzen
  - [ ] Nächsten Sprint vorbereiten

### Optional:

- [ ] **`CRM_COMPLETE_MASTER_PLAN_V5.md`**
  - [ ] SESSION_LOG: Sprint-Abschluss dokumentieren
  - [ ] NEXT_STEPS: Nächsten Sprint-Tasks aktualisieren

---

## ✅ Checkliste: Backend-Service hinzugefügt/geändert

**Wann:** Neuer Service, Repository, oder größere Service-Änderung

### Pflicht-Updates:

- [ ] **Modul `backend/_index.md`**
  - [ ] "Core Services" Sektion aktualisieren
  - [ ] JavaDoc-Stil Kommentar für Service hinzufügen

- [ ] **API Contract** (falls REST-Endpoint)
  - Pfad: `features-neu/{modul}/frontend/analyse/API_CONTRACT.md`
  - [ ] Endpoint dokumentieren (Method, Path, Request/Response)
  - [ ] Event-Schema dokumentieren (falls Event-Publishing)

- [ ] **Tests dokumentieren** (falls neue Test-Pattern)
  - Pfad: `features-neu/{modul}/artefakte/` (z.B. `SERVICE_TEST_PATTERN.md`)

### Optional:

- [ ] **Performance Benchmarks** hinzufügen
  - In `backend/_index.md` unter "Performance & Metrics"

---

## ✅ Checkliste: Frontend-Feature hinzugefügt

**Wann:** Neue Feature-Module, Pages, oder größere UI-Komponenten

### Pflicht-Updates:

- [ ] **Modul `frontend/_index.md`**
  - [ ] "Core Components" Sektion aktualisieren
  - [ ] Routing-Tabelle aktualisieren (falls neue Route)

- [ ] **API Contract** (falls neue Backend-Calls)
  - Pfad: `features-neu/{modul}/frontend/analyse/API_CONTRACT.md`
  - [ ] Endpoints dokumentieren, die das Feature nutzt

- [ ] **Design-Tokens** (falls neue UI-Patterns)
  - Pfad: `features-neu/{modul}/shared/DESIGN_TOKENS.md`

### Optional:

- [ ] **Accessibility-Review dokumentieren**
  - In Sprint-Artefakte `SUMMARY.md` unter "Quality Gates"

---

## ✅ Checkliste: Breaking Change (API/DB/Contract)

**Wann:** Inkompatible Änderung an API, Datenbank, oder Contracts

### Pflicht-Updates:

- [ ] **Migration erstellen** (für DB Breaking Changes)
  - [ ] Siehe "Neue Datenbank-Migration" Checkliste

- [ ] **ADR erstellen** (Begründung für Breaking Change)
  - [ ] Siehe "Neue ADR" Checkliste

- [ ] **API Contract aktualisieren**
  - [ ] Version-Bump dokumentieren
  - [ ] Breaking Change in CHANGELOG notieren

- [ ] **Modul `backend/_index.md`** ODER `frontend/_index.md`**
  - [ ] Breaking Change in "Current Sprint" warnen
  - [ ] Migration-Anleitung für bestehende Clients dokumentieren

- [ ] **Sprint-Artefakte `SUMMARY.md`**
  - [ ] Breaking Change unter "Deliverables" explizit erwähnen

### Pflicht-Kommunikation:

- [ ] **GitHub Issue/Discussion** erstellen
  - Breaking Change ankündigen
  - Migration-Guide verlinken

---

## ✅ Checkliste: Test-Strategie geändert

**Wann:** Neue Test-Patterns, Coverage-Ziele, oder CI-Änderungen

### Pflicht-Updates:

- [ ] **`TEST_MIGRATION_PLAN.md`** (falls vorhanden)
  - Pfad: `features-neu/{modul}/backend/TEST_MIGRATION_PLAN.md`
  - [ ] Neue Strategie dokumentieren

- [ ] **Modul `backend/_index.md`**
  - [ ] "Testing Strategy" Sektion aktualisieren
  - [ ] "Quality Gates" aktualisieren

- [ ] **Sprint-Trigger-Dokument**
  - [ ] Warnhinweis "⚠️ TEST-STRATEGIE BEACHTEN!" aktualisieren

- [ ] **Sprint-Artefakte `SUMMARY.md`**
  - [ ] Test-Metriken aktualisieren (Coverage, Performance)

---

## 🔗 Referenzen

### Kern-Dokumente (immer pflegen):

| Dokument | Pfad | Aktualisieren bei |
|----------|------|-------------------|
| **MIGRATIONS.md** | `/docs/planung/MIGRATIONS.md` | Jede neue Migration |
| **TRIGGER_INDEX.md** | `/docs/planung/TRIGGER_INDEX.md` | Neuer Sprint, Sprint abgeschlossen |
| **PLANUNGSMETHODIK.md** | `/docs/planung/PLANUNGSMETHODIK.md` | Strukturänderungen |
| **CRM_COMPLETE_MASTER_PLAN_V5.md** | `/docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md` | Session-Updates, Meilensteine |

### Modul-spezifische Dokumente:

| Dokument | Pfad-Pattern | Aktualisieren bei |
|----------|--------------|-------------------|
| **`_index.md`** | `features-neu/{modul}/_index.md` | Jeder Sprint |
| **`backend/_index.md`** | `features-neu/{modul}/backend/_index.md` | Backend-Änderungen |
| **`frontend/_index.md`** | `features-neu/{modul}/frontend/_index.md` | Frontend-Änderungen |
| **`SPRINT_MAP.md`** | `features-neu/{modul}/SPRINT_MAP.md` | Neuer Sprint |
| **ADRs** | `features-neu/{modul}/shared/adr/ADR-XXX-*.md` | Architekturentscheidungen |
| **API_CONTRACT.md** | `features-neu/{modul}/frontend/analyse/API_CONTRACT.md` | API-Änderungen |

### Sprint-spezifische Dokumente:

| Dokument | Pfad-Pattern | Aktualisieren bei |
|----------|--------------|-------------------|
| **Trigger-Dokument** | `/docs/planung/TRIGGER_SPRINT_X_Y_Z.md` | Sprint-Start, Sprint-Ende |
| **SUMMARY.md** | `features-neu/{modul}/artefakte/SPRINT_X_Y_Z/SUMMARY.md` | Sprint-Abschluss |
| **TEST_PLAN.md** | `features-neu/{modul}/artefakte/SPRINT_X_Y_Z/TEST_PLAN.md` | Test-Änderungen |

---

## 🤖 Automation-Potenzial

**Aktuell manuell, zukünftig automatisierbar:**

- [ ] **Migration-Nummer validieren** (via CI: Prüfe ob MIGRATIONS.md aktualisiert wurde)
- [ ] **Front-Matter Lint** (CI: Prüfe Pflichtfelder in allen `.md`-Dateien)
- [ ] **Link-Check** (CI: Validiere alle internen Links)
- [ ] **Sprint-Status-Sync** (Script: TRIGGER_INDEX.md ↔ Trigger-Dokumente)

**Siehe:** `.github/workflows/docs-validation.yml` für bestehende Checks

---

## 📝 Template-Checkliste für neue Claude-Instanz

**Beim Einstieg in einen neuen Sprint:**

1. [ ] `TRIGGER_INDEX.md` lesen → Aktuellen Sprint finden
2. [ ] `TRIGGER_SPRINT_X_Y_Z.md` öffnen → Arbeitsanweisung folgen
3. [ ] `MIGRATIONS.md` lesen → Migrations-Kontext verstehen
4. [ ] Modul `_index.md` öffnen → Status-Dashboard prüfen
5. [ ] `SPRINT_MAP.md` öffnen → Sprint-Kontext im Modul verstehen
6. [ ] Sprint-Artefakte `SUMMARY.md` lesen → Was wurde bereits erreicht?

**Bei jeder größeren Änderung:**

1. [ ] Diese `DOCUMENTATION_CHECKLIST.md` konsultieren
2. [ ] Relevante Checkliste abarbeiten
3. [ ] Alle aktualisierten Dokumente im Commit/PR erwähnen

---

**Letzte Aktualisierung:** 2025-10-02

**Verantwortlich:** Alle Claude Code Instanzen + Product Owner

**Nächste Review:** Bei nächster Strukturänderung oder Sprint 2.2.0
