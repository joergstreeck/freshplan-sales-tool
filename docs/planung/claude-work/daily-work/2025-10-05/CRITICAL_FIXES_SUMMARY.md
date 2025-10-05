# ✅ Kritische Fixes - Sprint 2.1.6 Dokumentation

**Erstellt:** 2025-10-05
**Status:** ✅ COMPLETE
**Betroffene Dokumente:** TRIGGER_SPRINT_2_1_6.md

---

## 🔴 FIX 1: Migration-Nummern-Chaos ✅ GELÖST

### **Problem:**
- Dokumente referenzierten V258/V259, aber **V259 ist bereits deployed**
- Inkonsistente Migration-Nummern in verschobenen Features

### **Lösung:**
✅ **User Story 1 (Lead-Transfer):**
- ❌ ALT: "V260: lead_transfers Tabelle (Migration-Nummer korrigiert - V259 ist deployed!)"
- ✅ NEU: "Migration: lead_transfers Tabelle (siehe `get-next-migration.sh` in Sprint 2.1.7)"

✅ **Technische Details - Lead Transfers:**
- ❌ Kompletten Abschnitt gelöscht (da auf 2.1.7 verschoben)
- ✅ Nur Backdating, Convert-Flow, Automated Jobs bleiben

✅ **Konsistenz:**
- Alle Migrations-Referenzen nutzen jetzt `get-next-migration.sh`
- V260 ist nächste verfügbare Nummer (wird in Sprint 2.1.7 verwendet)

---

## 🔴 FIX 2: Scope-Widerspruch Lead-Transfer ✅ GELÖST

### **Problem:**
- TRIGGER sagte: "Transfer verschoben auf 2.1.7"
- ABER: User Story 1 beschrieb Transfer detailliert!
- UND: Technische Details enthielten Lead-Transfer SQL-Code

### **Lösung:**
✅ **User Story 1:**
- Status: ❌ **VERSCHOBEN AUF SPRINT 2.1.7**
- Begründung hinzugefügt: "Zu komplex für Sprint 2.1.6"
- Verweis auf TRIGGER_SPRINT_2_1_7.md (User Story 1)
- Original-Scope dokumentiert (für Kontext)

✅ **Technische Details:**
- Lead Transfers Abschnitt **komplett gelöscht**
- Nur noch: Backdating, Convert-Flow, Automated Jobs (Sprint 2.1.6 Features)

✅ **Konsistenz:**
- Sprint-Ziel erwähnt Lead-Transfer NICHT mehr
- User Stories 1, 4, 5, 6 alle als "VERSCHOBEN" markiert
- Definition of Done enthält Lead-Transfer NICHT mehr

---

## 🟡 FIX 3: ADR-006 vs TRIGGER Inkonsistenz ✅ GELÖST

### **Problem:**
- ADR-006 beschrieb Lead-Scoring/Workflows/Timeline (Phase 2 Features)
- TRIGGER User Story 7 erwähnte nur "Status-Labels & Action-Buttons"
- **Kein Alignment** zwischen ADR und TRIGGER

### **Lösung:**
✅ **User Story 7 komplett neu geschrieben:**

**❌ ALT: "Frontend UI Improvements"**
- Lead Status-Labels
- Lead Action-Buttons
- Lead Detail-Seite
- Context-aware CustomerTable

**✅ NEU: "Frontend UI Phase 2 (ADR-006 - OPTIONAL)"**
- **Lead-Scoring-System (0-100 Punkte):**
  - Backend: `ALTER TABLE leads ADD COLUMN lead_score INTEGER`
  - Scoring-Faktoren: Umsatzpotenzial 25%, Engagement 25%, Fit 25%, Dringlichkeit 25%
  - Frontend: LeadScoreIndicator.tsx mit Progress Bar
  - Hook: useLeadScore.ts
- **Lead-Status-Workflows:**
  - UI für LEAD → PROSPECT → AKTIV
  - Frontend: LeadStatusWorkflow.tsx + LeadQualificationForm.tsx
- **Lead-Activity-Timeline:**
  - Activity-Log mit Icons
  - Frontend: LeadActivityTimeline.tsx
- **Lead-Protection aktivieren (Quick Win):**
  - Backend: `GET /api/leads?assignedTo={userId}`
  - Frontend: Quick Filter "Meine Leads"

**Aufwand:** 12-16h (High Complexity - aber OPTIONAL!)

**Referenzen:**
- ADR-006: Lead-Management Hybrid-Architektur (Phase 2 Features)
- Phase 1 (Sprint 2.1.5): CustomersPageV2 mit Context-Prop bereits COMPLETE

✅ **Definition of Done aktualisiert:**
- Neuer Abschnitt: **"Optional (ADR-006 Phase 2 - Falls Zeit!)"**
- Lead-Scoring, Workflows, Activity-Timeline als OPTIONAL markiert

---

## 📊 ZUSÄTZLICHE VERBESSERUNGEN

### **0. Backend-Status dokumentiert (GAME CHANGER!):**

**NEU: Backend-Status-Übersicht in TRIGGER_SPRINT_2_1_6.md (Zeile 271-343):**
- ✅ Stop-the-Clock Felder existieren bereits (`clockStoppedAt`, `stopReason`, `stopApprovedBy`)
- ✅ Backdating Felder existieren bereits (`registeredAtOverrideReason`, `registeredAtSetBy`)
- ✅ Automated Jobs Felder existieren bereits (`progressWarningSentAt`, `progressDeadline`)
- ✅ **KEINE** neuen DB-Migrations erforderlich für Sprint 2.1.6!

**Konsequenzen:**
- ✅ Sprint 2.1.6 ist primär **API + Frontend Work** (nicht Backend-Grundlagen)
- ✅ Schnellere Umsetzung möglich (16-22h statt 30-40h)
- ✅ Geringeres Risiko (Backend-Schema stabil)
- ✅ Fokus auf REST-Endpoints + React Components

**Neues Dokument:**
- `/docs/planung/claude-work/daily-work/2025-10-05/BACKEND_STATUS_SPRINT_2_1_6.md`

### **1. Definition of Done umstrukturiert:**

**NEU: Klare Prioritäten:**
```markdown
**PRIORITY #0 - BLOCKER (MUST DO FIRST!):**
- Issue #130 Fix - TestDataBuilder Refactoring (1-2h)

**Backend (Kern-Deliverables):**
- Bestandsleads-Migrations-API
- Lead → Kunde Convert Flow
- Backdating Endpoint
- Automated Jobs

**Frontend (Kern-Deliverables):**
- Stop-the-Clock UI
- MUI Dialog Accessibility Fix

**Optional (ADR-006 Phase 2 - Falls Zeit!):**
- Lead-Scoring-System
- Lead-Status-Workflows
- Lead-Activity-Timeline
```

### **2. Risiken & Mitigation aktualisiert:**

**Sprint 2.1.6 spezifisch:**
- Issue #130 Regression (Migration Guide + Code Review)
- Migration-API Datenverlust (Dry-Run PFLICHT)
- Convert-Flow Race-Conditions (Optimistic Locking)

**Verschoben auf Sprint 2.1.7:**
- ~~RLS Performance~~
- ~~Policy-Konflikte~~
- ~~Transfer-Deadlocks~~
- ~~False Positives~~

### **3. Scope-Änderung klargestellt:**

**Sprint-Ziel (aktualisiert):**
```markdown
**PRIORITY #0:** Issue #130 Fix (BLOCKER - MUSS ZUERST!)

**Kern-Deliverables:**
1. Bestandsleads-Migrations-API
2. Lead → Kunde Convert Flow
3. Stop-the-Clock UI
4. Backdating Endpoint
5. Automated Jobs

**Optional (ADR-006 Phase 2):**
6. Lead-Scoring-System
7. Lead-Status-Workflows
8. Lead-Activity-Timeline
9. MUI Dialog Accessibility Fix

**VERSCHOBEN AUF 2.1.7:**
- Lead-Transfer (User Story 1)
- Fuzzy-Matching (User Story 4)
- RLS (User Story 5)
- Team Management (User Story 6)
```

---

## ✅ VALIDIERUNG

### **Dokumentations-Konsistenz:**

- ✅ **Sprint-Ziel:** Erwähnt nur noch Features in Sprint 2.1.6 (keine verschobenen)
- ✅ **User Stories:** 1, 4, 5, 6 als "VERSCHOBEN" markiert mit Verweis auf 2.1.7
- ✅ **User Story 7:** Mit ADR-006 Phase 2 synchronisiert
- ✅ **Technische Details:** Nur noch Sprint 2.1.6 Features (Lead-Transfer gelöscht)
- ✅ **Definition of Done:** Issue #130 als PRIORITY #0, ADR-006 als OPTIONAL
- ✅ **Risiken:** Sprint 2.1.6 spezifisch, verschobene Risiken markiert

### **Migrations-Nummern konsistent:**

- ✅ **V259:** Deployed (remove_leads_company_city_unique_constraint.sql)
- ✅ **V260:** Nächste verfügbar (per `get-next-migration.sh`)
- ✅ **Sprint 2.1.7:** V260-V262 geplant (lead_transfers, RLS, teams)
- ✅ **Alle Referenzen:** Nutzen `get-next-migration.sh` statt feste V-Nummern

### **ADR-006 Alignment:**

- ✅ **Phase 1 (Sprint 2.1.5):** CustomersPageV2 mit Context-Prop ✅ COMPLETE
- ✅ **Phase 2 (Sprint 2.1.6):** Lead-Scoring, Workflows, Timeline als OPTIONAL dokumentiert
- ✅ **Referenzen:** User Story 7 verweist auf ADR-006 Phase 2 Features

---

## 📋 BETROFFENE DATEIEN

**Geändert:**
- ✅ `/docs/planung/TRIGGER_SPRINT_2_1_6.md` (alle 3 Fixes)

**Keine Änderungen nötig (bereits korrekt):**
- ✅ `/docs/planung/TRIGGER_SPRINT_2_1_7.md` (Lead-Transfer korrekt dokumentiert)
- ✅ `/docs/planung/TRIGGER_INDEX.md` (Scope-Änderung bereits erwähnt)
- ✅ `/docs/planung/features-neu/02_neukundengewinnung/SPRINT_MAP.md` (bereits aktualisiert)

---

## 🎯 ZUSAMMENFASSUNG FÜR NEUEN CLAUDE

### **Sprint 2.1.6 - Klarheit durch Fixes:**

**PRIORITY #0 (1-2h):**
1. ✅ Issue #130 Fix (TestDataBuilder Refactoring - BLOCKER)

**Kern-Deliverables (6-8 Tage):**
2. ✅ Bestandsleads-Migrations-API (Modul 08, Dry-Run + Real Import)
3. ✅ Lead → Kunde Convert Flow (POST /api/leads/{id}/convert)
4. ✅ Backdating Endpoint (PUT /api/leads/{id}/registered-at)
5. ✅ Automated Jobs (Progress Warning, Expiry, Pseudonymisierung)
6. ✅ Stop-the-Clock UI (Manager-only, StopTheClockDialog)
7. ✅ MUI Dialog Accessibility Fix (WCAG 2.1 Level A)

**Optional (Falls Zeit!):**
8. ⚪ Lead-Scoring-System (Backend + Frontend)
9. ⚪ Lead-Status-Workflows (UI)
10. ⚪ Lead-Activity-Timeline (Interaktions-Historie)

**NICHT in Sprint 2.1.6 (auf 2.1.7 verschoben):**
- ❌ Lead-Transfer Workflow
- ❌ Fuzzy-Matching & Review
- ❌ Row-Level-Security
- ❌ Team Management

### **Migrations-Nummern:**
- V259: ✅ Deployed (remove_leads_company_city_unique_constraint)
- V260: Nächste verfügbar (per `get-next-migration.sh`)
- Sprint 2.1.7: V260-V262 (lead_transfers, RLS, teams)

### **ADR-006 Phase 2:**
- Phase 1: ✅ COMPLETE (Sprint 2.1.5 - CustomersPageV2 Reuse)
- Phase 2: ⚪ OPTIONAL (Sprint 2.1.6 - Lead-spezifische Features)

---

**✅ Alle kritischen Fixes implementiert**
**✅ Dokumentation konsistent**
**✅ Scope klar definiert**

_Erstellt von Claude Code (Sonnet 4.5) am 2025-10-05_
