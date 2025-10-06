---
sprint_id: "2.1.9"
title: "Lead-Kollaboration & Tracking"
doc_type: "konzept"
status: "planned"
owner: "team/leads-backend"
date_start: "2025-11-02"
date_end: "2025-11-04"
modules: ["02_neukundengewinnung"]
entry_points:
  - "features-neu/02_neukundengewinnung/_index.md"
  - "features-neu/02_neukundengewinnung/backend/_index.md"
  - "features-neu/02_neukundengewinnung/SPRINT_MAP.md"
pr_refs: []
updated: "2025-10-05"
---

# Sprint 2.1.9 – Lead-Kollaboration & Tracking

**📍 Navigation:** Home → Planung → Sprint 2.1.9

> **🎯 Arbeitsanweisung – Reihenfolge**
> 1. **SPRINT_MAP des Moduls öffnen** → `features-neu/02_neukundengewinnung/SPRINT_MAP.md`
> 2. **MIGRATION-CHECK (🚨 PFLICHT bei DB-Arbeit!)**
>    ```bash
>    # Primär:
>    /Users/joergstreeck/freshplan-sales-tool/scripts/get-next-migration.sh
>    # Ausgabe MUSS V[0-9]+ sein (z.B. V260)
>    # Diese Nummer für heutige DB-Arbeit verwenden!
>
>    # Fallback (Templates ausblenden):
>    ls -la backend/src/main/resources/db/migration/ | grep -v 'VXXX__' | tail -3
>    ```
> 3. **Backend:** Notizen-System + Status-Historie + Temperatur-Flag
> 4. **Frontend:** Notizen-Timeline + Status-Historie + Temperatur-Badge

## 🔧 GIT WORKFLOW (KRITISCH!)

**PFLICHT-REGELN für alle Sprint-Arbeiten:**

### ✅ ERLAUBT (ohne User-Freigabe):
- `git commit` - Commits erstellen wenn User darum bittet
- `git add` - Dateien stagen
- `git status` / `git diff` - Status prüfen
- Feature-Branches anlegen (`git checkout -b feature/...`)

### 🚫 VERBOTEN (ohne explizite User-Freigabe):
- **`git push`** - NIEMALS ohne User-Erlaubnis pushen!
- **PR-Erstellung** - Nur auf explizite Anforderung
- **PR-Merge** - Nur wenn User explizit zustimmt
- **Branch-Deletion** - Remote-Branches nur mit User-OK löschen

### 📋 Standard-Workflow:
1. **Feature-Branch anlegen:** `git checkout -b feature/modXX-sprint-Y.Z-description`
2. **Arbeiten & Committen:** Code schreiben, Tests validieren, `git commit`
3. **User fragen:** "Branch ist bereit. Soll ich pushen und PR erstellen?"
4. **Erst nach Freigabe:** `git push` + PR-Erstellung

**Referenz:** `/CLAUDE.md` → Sektion "🚫 GIT PUSH POLICY (KRITISCH!)"

---


## Sprint-Ziel

**Team-Workflows & Nachvollziehbarkeit verbessern**

Nach Sprint 2.1.8 (DSGVO & Import) fehlen noch **Team-Kollaborations-Features** und **Nachvollziehbarkeit** für Lead-Änderungen.

**Kern-Deliverables:**
1. **Lead-Notizen & Kommentare** - Team-Kollaboration (Notizen für Kollegen)
2. **Lead-Status-Änderungs-Historie** - Audit-Trail für Status-Änderungen
3. **Lead-Temperatur (Hot/Warm/Cold)** - Visuelle Priorisierung

**Scope-Klarstellung:**
- Sprint 2.1.5: Lead-Activities (Erstkontakt, Progress-Aktivitäten)
- Sprint 2.1.9: Lead-Notizen (freie Team-Kommentare, unabhängig von Activities)

---

## User Stories

### 1. Lead-Notizen & Kommentare (Team-Kollaboration) - 🟡 WICHTIG

**Begründung:** Team-Kollaboration - "Habe heute angerufen, kein Interesse" (für Kollegen sichtbar), Manager-Feedback, Übergabe-Notizen

**Unterschied zu Lead-Activities:**
- **Lead-Activities:** Strukturiert (Activity-Type, countsAsProgress) - für Schutz-Regeln
- **Lead-Notizen:** Freie Team-Kommentare - für Kollaboration

**Akzeptanzkriterien:**
- [ ] **Notizen-System (unabhängig von Activities)**
  - Tabelle: lead_notes (id, lead_id, user_id, note, created_at, updated_at)
  - Migration-Nummer: siehe `get-next-migration.sh` (CREATE TABLE lead_notes)
- [ ] **Chronologische Timeline (neueste zuerst)**
  - GET /api/leads/{id}/notes → sortiert nach created_at DESC
  - UI: Timeline-Widget in Lead-Detail-Seite
- [ ] **CRUD-Operationen**
  - POST /api/leads/{id}/notes (Notiz erstellen)
  - PUT /api/notes/{id} (Notiz bearbeiten - nur eigene Notizen!)
  - DELETE /api/notes/{id} (Notiz löschen - nur eigene Notizen!)
  - GET /api/leads/{id}/notes (alle Notizen für Lead)
- [ ] **Notiz-Typen** (OPTIONAL - für Sprint 2.2+)
  - INTERNAL: Nur für Team sichtbar
  - PUBLIC: Für Kunde sichtbar (später, wenn Customer-Portal kommt)
  - Aktuell: Alle Notizen sind INTERNAL
- [ ] **@ Mentions (OPTIONAL - für Sprint 2.2+)**
  - "@Max bitte übernehmen" → Notification an User "Max"
  - Aktuell: Nur freier Text, keine Mentions
- [ ] **Notizen in Lead-Detail anzeigen**
  - Timeline-Widget: Notizen + Activities gemischt (chronologisch)
  - Unterscheidung: Notizen (💬 Icon), Activities (📅 Icon)

**Betroffene Komponenten:**
- **Backend:**
  - LeadNotesResource.java (CRUD-Endpoints)
  - lead_notes Tabelle (Migration V268)
- **Frontend:**
  - LeadNotesTimeline.tsx (Timeline-Widget mit Notizen + Activities)
  - AddNoteDialog.tsx (Notiz erstellen)
  - EditNoteButton.tsx (nur eigene Notizen bearbeitbar)

**Aufwand:** 6-8h (Medium Complexity - CRUD + Timeline-UI)

**Referenzen:**
- Lead-Activities: [ACTIVITY_TYPES_PROGRESS_MAPPING.md](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/ACTIVITY_TYPES_PROGRESS_MAPPING.md)

---

### 2. Lead-Status-Änderungs-Historie (Audit-Trail) - 🟡 SOLLTE

**Begründung:** Nachvollziehbarkeit - "Wann wurde Lead von PROSPECT → LEAD zurückgestuft? Warum?", Audit-Compliance, Performance-Tracking

**Aktueller Stand:**
- Lead-Activities: Aktivitäten-Log ✅
- ABER: **Status-Änderungen nicht getrackt!**

**Akzeptanzkriterien:**
- [ ] **Status-Historie-Tabelle** (Migration-Nummer: siehe `get-next-migration.sh`)
  - lead_status_history (id, lead_id, from_status, to_status, changed_by, reason, changed_at)
  - Index: CREATE INDEX idx_lead_status_history_lead_id ON lead_status_history(lead_id)
- [ ] **Automatisches Tracking bei Status-Änderung**
  - Entity-Listener: @PostUpdate in Lead.java
  - Wenn stage geändert → Eintrag in lead_status_history
  - changed_by = current_user_id (aus SecurityContext)
- [ ] **Änderungs-Grund (optional)**
  - UI: Bei Status-Downgrade (z.B. PROSPECT → LEAD) → Dialog "Warum zurückgestuft?"
  - Freitext: reason (min. 10 Zeichen, optional)
  - Bei Upgrade (z.B. LEAD → PROSPECT) → kein Grund nötig
- [ ] **Status-Historie in Lead-Detail anzeigen**
  - Timeline-Widget: Status-Änderungen mit Icon (🔄)
  - Format: "2025-10-05: LEAD → PROSPECT (Max Mustermann) - Grund: Kunde qualifiziert"
- [ ] **Performance-Tracking (Dashboard)**
  - Durchschnittliche Dauer: LEAD → PROSPECT (X Tage)
  - Durchschnittliche Dauer: PROSPECT → AKTIV (Y Tage)
  - Conversion-Rate: LEAD → AKTIV (Z %)

**Betroffene Komponenten:**
- **Backend:**
  - LeadStatusHistoryService.java (Auto-Tracking via Entity-Listener)
  - lead_status_history Tabelle (Migration V269)
- **Frontend:**
  - StatusChangeDialog.tsx (Downgrade → Grund erfassen)
  - LeadNotesTimeline.tsx (Status-Änderungen anzeigen)
  - LeadPerformanceDashboard.tsx (Performance-Metriken)

**Aufwand:** 4-6h (Low Complexity - Entity-Listener + Timeline-UI)

**Referenzen:**
- JPA Entity-Listener: https://www.baeldung.com/jpa-entity-lifecycle-events

---

### 3. Lead-Temperatur (Hot/Warm/Cold) - 🟡 SOLLTE

**Begründung:** Visuelle Priorisierung - Schnelle Fokussierung auf wichtige Leads (Hot = sofort kontaktieren, Cold = niedrige Priorität)

**Was ist Lead-Temperatur?**
- **🔴 Hot:** Hohe Kaufwahrscheinlichkeit, sofort kontaktieren (z.B. BANT-Score 4, Timeline IMMEDIATE)
- **🟡 Warm:** Mittlere Kaufwahrscheinlichkeit, regelmäßig nachfassen (z.B. BANT-Score 2-3)
- **⚪ Cold:** Niedrige Kaufwahrscheinlichkeit, gelegentlich nachfassen (z.B. BANT-Score 0-1)

**Akzeptanzkriterien:**
- [ ] **Temperatur-Feld im Lead-Entity** (Migration-Nummer: siehe `get-next-migration.sh`)
  - temperature (ENUM: HOT, WARM, COLD, NULL)
  - Default: NULL (User muss manuell setzen)
- [ ] **Automatische Temperatur-Vorschlag (OPTIONAL - für Sprint 2.2+)**
  - BANT-Score 4 → HOT
  - BANT-Score 2-3 → WARM
  - BANT-Score 0-1 → COLD
  - Aktuell: Nur manuelle Eingabe
- [ ] **Temperatur-Badge in Lead-List**
  - 🔴 Hot, 🟡 Warm, ⚪ Cold (Color-Coded Badge)
  - Sortierung: Hot zuerst (in Lead-List)
- [ ] **Filter: "Heiße Leads" (temperature = HOT)**
  - IntelligentFilterBar: Quick-Filter "Heiß 🔴"
- [ ] **Dashboard-Widget: Temperatur-Verteilung**
  - Pie-Chart: Hot (X Leads), Warm (Y Leads), Cold (Z Leads)

**Betroffene Komponenten:**
- **Backend:**
  - Lead Entity: temperature Feld
  - Migration-Nummer: siehe `get-next-migration.sh` (temperature ENUM: HOT, WARM, COLD)
- **Frontend:**
  - TemperatureBadge.tsx (Lead-List, Lead-Detail)
  - TemperatureSelector.tsx (Dropdown in Lead-Detail)
  - TemperatureDashboardWidget.tsx (Dashboard)

**Aufwand:** 2-3h (Low Complexity - nur Backend-Feld + UI-Badge)

**Referenzen:**
- BANT-Score (Sprint 2.1.8): Kann für automatische Temperatur-Vorschlag genutzt werden

---

## Definition of Done (Sprint 2.1.9)

**Backend (Kern-Deliverables):**
- [ ] **Lead-Notizen-System funktionsfähig** (CRUD-Endpoints + Timeline)
- [ ] **Lead-Status-Änderungs-Historie** (Auto-Tracking + Audit-Trail)
- [ ] **Lead-Temperatur-Feld** (ENUM + Filter)
- [ ] **Backend Tests ≥80% Coverage**
- [ ] **Migrations deployed** (Nummern: siehe `get-next-migration.sh` – 3 Migrations für Notizen, Status-Historie, Temperatur)

**Frontend (Kern-Deliverables):**
- [ ] **Lead-Notizen-Timeline** (Notizen + Activities gemischt)
- [ ] **Status-Änderungs-Historie** (in Timeline anzeigen)
- [ ] **Temperatur-Badge** (Lead-List + Lead-Detail)
- [ ] **Frontend Tests ≥75% Coverage**

**Dokumentation:**
- [ ] **Notizen-System erklärt** (für Sales-Team)
- [ ] **Status-Historie dokumentiert** (Performance-Tracking)
- [ ] **Temperatur-Methode erklärt** (Hot/Warm/Cold Kriterien)

---

## Risiken & Mitigation

**Sprint 2.1.9 spezifisch:**
- **Notizen-Spam:** User erstellen zu viele Notizen → Timeline unübersichtlich
  - **Mitigation:** Pagination (10 Notizen pro Seite), Filter (nur Notizen/nur Activities)
- **Status-Historie Performance:** Bei 10.000 Leads → viele Einträge
  - **Mitigation:** Index auf lead_id, nur letzte 50 Einträge laden

---

## Abhängigkeiten

- Sprint 2.1.5 (Lead-Activities) muss abgeschlossen sein (für Timeline-Integration)
- Sprint 2.1.8 (BANT-Score) sollte abgeschlossen sein (für automatische Temperatur-Vorschläge - OPTIONAL)

---

## Test Strategy

**Notizen-Tests:**
- Unit-Tests: CRUD-Operationen (Create, Read, Update, Delete)
- Integration-Tests: Timeline mit Notizen + Activities
- Manual-Tests: Notizen in Lead-Detail anzeigen

**Status-Historie-Tests:**
- Unit-Tests: Entity-Listener (Status-Änderung tracken)
- Integration-Tests: Status-Downgrade mit Grund
- Manual-Tests: Performance-Dashboard (Durchschnittliche Dauer)

**Temperatur-Tests:**
- Unit-Tests: Temperatur-Feld setzen/ändern
- Integration-Tests: Filter "Heiße Leads"
- Manual-Tests: Dashboard-Widget (Temperatur-Verteilung)

---

## Monitoring & KPIs

- **Notizen pro Lead:** Durchschnitt (Ziel: 2-3 Notizen pro Lead)
- **Status-Änderungen pro Lead:** Durchschnitt (Ziel: 3-4 Status-Änderungen)
- **Temperatur-Verteilung:** Hot/Warm/Cold Ratio (Dashboard-Widget)
- **Lead-Conversion-Zeit:** LEAD → AKTIV Durchschnitt (aus Status-Historie)

---

## Next Sprint Preview

**Sprint 2.2+: "Lead-Advanced-Features"**
- Lead-Kampagnen-Tracking (ROI-Tracking)
- Lead-E-Mail-Integration (Outlook/Gmail)
- Lead-Telefon-Integration (Click-to-Call)
- Lead-Web-Formular (öffentliches Embedded Widget)

**Geschätzter Aufwand:** 40-60h (~1 Woche)

---

**Erstellt:** 2025-10-05
**Status:** 📅 PLANNED
**Gesamt-Aufwand:** 12-17h (~2-3 Tage)
