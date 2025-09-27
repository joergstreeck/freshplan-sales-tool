---
module: "shared"
domain: "shared"
doc_type: "guideline"
status: "approved"
owner: "team/leads"
updated: "2025-09-27"
---

# 📋 Planungsmethodik – Einstieg über Sprints, Arbeit in Modulen

**Kurzfassung:**
- **Einstieg immer über Sprints** (zentral unter `docs/planung/`).
- **Konkrete Arbeit in Modulen** über Overlays: `backend/`, `frontend/`, `shared/`.
- **Recherche & Muster** liegen in `analyse/` und `artefakte/`.
- **Historie & Detailplanungen** werden nach `legacy-planning/` ausgelagert (mit Stubs abgesichert).

---

## 1) Navigationsprinzip (für Menschen & KI)

1. **Sprint öffnen** (zentral, z. B. `TRIGGER_SPRINT_2_1.md`): Ziel, Arbeitsauftrag, PR‑Bezug.
2. **SPRINT_MAP des Moduls**: Brücke vom Sprint zum Modul (keine Kopien, nur Links).
3. **Modulstart (`_index.md`)**: kurzes **Status‑Dashboard** & klare Reihenfolge.
4. **Overlays**: in `backend/`, `frontend/`, `shared/` arbeiten.
5. **Nachschlagen**: `analyse/` (Research, API‑Contracts) & `artefakte/` (bewährte Patterns).
6. **Historie**: `legacy-planning/` (alte Detailplanungen), mit **Stubs** an alten Pfaden.

**Modul‑Blaupause (Struktur pro Modul `NN_modulname/`):**

```
NN_modulname/
├── _index.md
├── SPRINT_MAP.md
├── backend/
├── frontend/
├── shared/
├── analyse/
├── artefakte/
├── technical-concept.md          # Überblick (keine Feature-Details)
└── legacy-planning/              # Historie/Detailplanungen (mit Stubs)
```

---

## 2) Sprints – der Startpunkt

**Ort:**
- **Alle Sprints bleiben** unter `docs/planung/` (z. B. `TRIGGER_SPRINT_2_1.md`, `TRIGGER_SPRINT_2_1_1.md`, …).
- `TRIGGER_INDEX.md` bleibt zentrale Übersicht.

**Sprint‑Header (Pflicht, YAML am Dokumentanfang):**
```yaml
---
sprint_id: "2.1.2"
title: "Frontend Research – Modul 02"
status: "complete"        # draft | in_progress | complete
date_start: "2025-09-24"
date_end: "2025-09-27"
modules: ["02_neukundengewinnung"]
entry_points:
  - "features-neu/02_neukundengewinnung/SPRINT_MAP.md"
  - "features-neu/02_neukundengewinnung/frontend/_index.md"
pr_refs: ["#112"]
updated: "2025-09-27"
---
```

**Arbeitsanweisung (Pflichtkasten, direkt unter dem Header):**

> **🎯 Arbeitsanweisung – Reihenfolge**
> 1. **SPRINT_MAP des Moduls öffnen**
> 2. **Modul‑Start (_index.md) → Status prüfen**
> 3. **Overlay wählen:** `backend/` oder `frontend/` (Cross‑Cutting: `shared/`)
> 4. **Details:** `analyse/` (Research, Contracts)
> 5. **Muster:** `artefakte/` (Patterns kopieren, wo sinnvoll)

**Definition of Done (Sprint):**
- [ ] **Ergebnis erreicht** (1–2 Sätze Beleg)
- [ ] **Relevante Modul‑Docs aktualisiert** (`_index.md`, ggf. `analyse/`…)
- [ ] **Link‑Check grün** (keine kaputten Links)
- [ ] **Stubs gesetzt**, falls Pfade geändert wurden (mind. 2 Sprints bestehen lassen)

---

## 3) Modul‑Overlays (Backend, Frontend, Shared)

- **`backend/`** – Server‑Logik, Persistenz, Events, Security.
- **`frontend/`** – UI, Routen, Daten‑Fetching, State.
- **`shared/`** – Standards & Contracts: Design‑Tokens, Event‑Envelope, Metriken, Security‑Policies.

**Status‑Dashboard (Pflichtbestandteil im Modul‑`_index.md`):**

```markdown
**Status:**
- Backend: ✅ Production (letzter Sprint z. B. 2.1/2.1.1)
- Frontend: 📋 Research complete → Implementation next
- Patterns: ✅ Copy‑Paste ready (z. B. 3 Artefakte)
- Legacy: 📚 Archiviert (→ legacy-planning/)
```

**Root‑Ordnung (Guardrail):**
Modul‑Root zeigt max. **8 Kern‑Items** (`_index.md`, `SPRINT_MAP.md`, `backend/`, `frontend/`, `shared/`, `analyse/`, `artefakte/`, `technical-concept.md`) + `legacy-planning/`.

**Ausnahme:** **Stub‑Verzeichnisse** (2‑Sprint‑Retention) zählen nicht zu den 8 Kern‑Items. Sie sind temporär und sollten nicht für Navigation verwendet werden.

---

## 4) Dokument‑Standards (Front‑Matter, Breadcrumbs, Links)

**Front‑Matter (Pflicht in jedem Doc):**

```yaml
---
module: "02_neukundengewinnung"     # NN_modulname oder "shared"
domain: "frontend"                  # backend | frontend | shared
doc_type: "analyse"                 # analyse | konzept | contract | guideline | deltalog | adr
status: "approved"                  # draft | approved | obsoleted
sprint: "2.1.2"                     # optional – wenn sprintbezogen
owner: "team/leads"
updated: "2025-09-27"
---
```

**Breadcrumb (Pflicht direkt nach H1):**

```markdown
**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Frontend → Analyse → API_CONTRACT
```

**Link‑Regeln:**
- Relative Pfade innerhalb von `docs/planung/`.
- Keine GitHub‑URLs für interne Links.
- Zentrale Sprints verlinken, niemals kopieren.

---

## 5) Research & Patterns

- **`analyse/`** – Inventuren, API‑Contracts, Entscheidungsgrundlagen.
- **`artefakte/`** – Bewährte Patterns (z. B. Security‑Tests, Performance‑Tests, Event‑System), copy‑paste‑fähig.

---

## 6) Legacy‑Planungen & Stubs (Weiterleitungen)

**Zweck:** Alte/umfangreiche Inhalte sichtbar, aber nicht im Weg.

**Ort:** `legacy-planning/`…

**Stubs:** Am alten Pfad belassen (mindestens 2 Sprints), damit externe/alte Verweise nicht brechen.

**Stub‑Template (minimal):**

```markdown
---
status: "moved"
moved_to: "./legacy-planning/lead-erfassung/technical-concept.md"
updated: "2025-09-27"
---

# ➡️ Dokument verschoben

**Neuer Pfad:** `./legacy-planning/lead-erfassung/technical-concept.md`
**Grund:** Strukturvereinfachung (aktuelle Arbeit erfolgt in den Modul‑Overlays)
```

**Doppelungen vermeiden:**
- **Modul‑Root:** `technical-concept.md` ist Überblick.
- **Legacy‑Details** werden umbenannt (z. B. `LEAD_FEATURE_CONCEPT.md`, `EMAIL_FEATURE_CONCEPT.md`).

---

## 7) Guardrails (CI)

- **Docs‑only Check** (bestehend) – verhindert Codeänderungen in Docs‑PRs.
- **Link‑Check** – prüft Referenzen in `docs/**`.
- **Structure‑Guard** – stellt sicher: Modul‑Root hat max. 8 Kern‑Items + `legacy-planning/`.
- **Front‑Matter‑Lint** – prüft Pflichtfelder & gültige Werte in Markdown‑Front‑Matter.

(Guardrails laufen als GitHub Actions und sichern Konsistenz über die Zeit.)

---

## 8) Quick‑Start für neue Claude‑Instanzen

1. **Sprint öffnen** → Arbeitsanweisung lesen.
2. **SPRINT_MAP des Moduls** aufrufen.
3. **Modul‑Start (`_index.md`)** → Status prüfen.
4. **In `backend/` oder `frontend/` arbeiten**.
5. **`analyse/` für Details, `artefakte/` für Muster**.
6. **Historie in `legacy-planning/`**.

---

**Letzte Aktualisierung:** 2025‑09‑27