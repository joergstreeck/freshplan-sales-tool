# 📚 Dokumentations-Update Summary - Sprint 2.1.6 & 2.1.7

**Erstellt:** 2025-10-05
**Zweck:** Issue #130 in Sprint 2.1.6 integriert, Sprint 2.1.7 für Test Infrastructure erstellt
**Status:** ✅ COMPLETE

---

## 🎯 ZUSAMMENFASSUNG

### **Was wurde gemacht?**

1. ✅ **Issue #130 Quick Fix in Sprint 2.1.6 integriert**
   - User Story 0.5 hinzugefügt (BLOCKER - MUST DO FIRST)
   - Technische Details, Migration-Pattern, Akzeptanzkriterien dokumentiert
   - 12 broken Tests identifiziert, Fix-Strategie beschrieben

2. ✅ **Sprint 2.1.6 Scope bereinigt**
   - Lead-Transfer, RLS, Team Management, Fuzzy-Matching auf Sprint 2.1.7 verschoben
   - Fokus auf Admin-Features: Migration-API, Convert-Flow, Stop-the-Clock, Jobs
   - Optional: Lead-Scoring, Workflows, Activity-Timeline (ADR-006 Phase 2)

3. ✅ **Sprint 2.1.7 neu erstellt**
   - **Track 1:** Lead-Transfer + RLS + Team Management + Fuzzy-Matching (verschoben aus 2.1.6)
   - **Track 2:** Test Infrastructure Overhaul (CRM Szenario-Builder, Faker, Test-Patterns)
   - Strategisches Investment: Test-Qualität für Sprint 2.2+ Velocity

4. ✅ **Alle Dokumentations-Pfade aktualisiert**
   - TRIGGER_INDEX.md: Sprint 2.1.6 & 2.1.7 aktualisiert
   - SPRINT_MAP.md: Scope-Änderungen dokumentiert
   - PRODUCTION_ROADMAP_2025.md: Live Dashboard aktualisiert
   - Issue #130 Analyse-Dokument erstellt

---

## 📋 AKTUALISIERTE DOKUMENTE

### 🟢 Backend-Status & MUI Accessibility Dokumentation (NEU - 2025-10-05 Nachmittag):

**MUI_ACCESSIBILITY_DECISION.md** ⭐ NEU
**Pfad:** `/docs/planung/claude-work/daily-work/2025-10-05/MUI_ACCESSIBILITY_DECISION.md`

**Zweck:** Entscheidungsdokumentation - Warum MUI Dialog Accessibility KERN-DELIVERABLE ist

**Inhalt:**
1. **Problem-Zusammenfassung:** aria-hidden Warning in MUI Dialogs
2. **Lösungsalternativen:**
   - Alternative 1: KERN-DELIVERABLE ✅ GEWÄHLT
   - Alternative 2: OPTIONAL ❌ ABGELEHNT
   - Alternative 3: Hybrid ❌ ABGELEHNT
3. **Entscheidung:** Alternative 1 (5 Begründungen)
   - Gesetzliche Pflicht (EU Accessibility Act 2025)
   - Minimaler Aufwand (1-2h)
   - Sprint 2.1.6 hat Zeit (Backend-Ready)
   - Einmalige Investition (alle Dialoge profitieren)
   - Professioneller Standard (keine Audit-Probleme)
4. **Implementierte Änderungen:** User Story 8 aktualisiert
5. **Erwartete Ergebnisse:** Alle Dialoge WCAG 2.1 Level A konform

---

### 🟢 Backend-Status Dokumentation (NEU - 2025-10-05 Nachmittag):

**BACKEND_STATUS_SPRINT_2_1_6.md** ⭐ NEU
**Pfad:** `/docs/planung/claude-work/daily-work/2025-10-05/BACKEND_STATUS_SPRINT_2_1_6.md`

**Zweck:** Game Changer - Backend ist weiter als erwartet!

**Kernaussage:**
- ✅ Sprint 2.1.6 ist **primär API + Frontend Work** (Backend-Felder existieren bereits!)
- ✅ Stop-the-Clock, Backdating, Automated Jobs: Backend-Ready
- ✅ **KEINE** neuen DB-Migrations erforderlich
- ✅ Aufwandsreduktion: 16-22h (statt 30-40h)

**Inhalt:**
1. ✅ Backend-Ready Features (nur API/UI fehlen)
2. ❌ Backend-Felder fehlen (V260-V262 in Sprint 2.1.7)
3. 📊 Migration-Übersicht (V255-V259 deployed, V260 next)
4. 🎯 Konsequenzen für Sprint 2.1.6 (schneller, sicherer, weniger Risiko)

---

**TRIGGER_SPRINT_2_1_6.md - Backend-Status-Übersicht hinzugefügt**
**Pfad:** `/docs/planung/TRIGGER_SPRINT_2_1_6.md`

**Neuer Abschnitt:** "🟢 Backend-Status-Übersicht" (Zeile 271-343)
- Detaillierte Felder-Auflistung aus `Lead.java` mit Zeilennummern
- Backend-Ready Status für Stop-the-Clock, Backdating, Automated Jobs
- API-Specs für neue Endpoints (Stop-Clock, Resume-Clock, Backdating)
- Migration-Check-Referenz: `./scripts/get-next-migration.sh`
- Konsequenzen dokumentiert

**User Story 3 (Stop-the-Clock) erweitert:**
- Backend-Status: ✅ Backend-Ready
- API-Spec hinzugefügt (PUT /stop-clock, /resume-clock)

**Backdating Abschnitt erweitert:**
- Backend-Status: ✅ Backend-Ready
- API-Spec aktualisiert

**Automated Jobs erweitert:**
- Backend-Status: ✅ Backend teilweise Ready
- 3 @Scheduled Jobs dokumentiert (Progress Warning, Expiry, Pseudonymisierung)

---

### **1. TRIGGER_SPRINT_2_1_6.md**
**Pfad:** `/docs/planung/TRIGGER_SPRINT_2_1_6.md`

**Änderungen:**
- ✅ User Story 0 (Issue #125) als ✅ COMPLETE markiert (PR #131)
- ✅ User Story 0.5 (Issue #130) **NEU** hinzugefügt - **🔴 BLOCKER - MUST DO FIRST**
  - Problem-Analyse: CDI-Konflikt zwischen Legacy (src/main) und neuen Buildern (src/test)
  - 12 broken Tests dokumentiert (ContactInteractionServiceIT)
  - Migration-Pattern: Alt (TestDataBuilder) → Neu (TestDataFactory)
  - Akzeptanzkriterien: Legacy löschen, Tests migrieren, CI reaktivieren
- ✅ User Story 1, 4, 5, 6 als ❌ **VERSCHOBEN AUF SPRINT 2.1.7** markiert
  - Lead-Transfer (zu komplex)
  - Fuzzy-Matching (verdient eigenen Sprint)
  - RLS + Team Management (komplex, gehören zusammen)
- ✅ Sprint-Ziel aktualisiert: Fokus auf Admin-Features + Issue #130 Fix
- ✅ Scope-Änderung (05.10.2025) dokumentiert

**Neue Inhalte:**
```markdown
### 0.5. TestDataBuilder Refactoring (Issue #130) - **🔴 BLOCKER - MUST DO FIRST**

**Problem-Analyse:**
- Root Cause: Doppelte TestDataBuilder in src/main und src/test
- CDI-Konflikt: Quarkus lädt beide → Legacy nutzt CustomerContactRepository, Neue nutzen ContactRepository
- Fehler: EntityExistsException: detached entity passed to persist
- Impact: 12 Tests broken, Worktree CI disabled

**Akzeptanzkriterien:**
- [ ] Legacy Builder aus src/main/java/de/freshplan/test/builders/ löschen
- [ ] 12 Tests auf neue Builder umstellen
- [ ] Worktree CI reaktivieren
- [ ] Migration Guide dokumentiert
```

---

### **2. TRIGGER_SPRINT_2_1_7.md (NEU)**
**Pfad:** `/docs/planung/TRIGGER_SPRINT_2_1_7.md`

**Zweck:** Verschobene Features aus 2.1.6 + Strategisches Test Infrastructure Investment

**Struktur:**
```markdown
# Sprint 2.1.7 – Lead Team Management & Test Infrastructure Overhaul

## Sprint-Ziel

### Track 1: Lead Team Management (Business Features)
- Lead-Transfer Workflow (V260 Migration, POST /api/leads/{id}/transfer)
- Fuzzy-Matching & Review (Scoring-Algorithmus mit Schwellwerten)
- Row-Level-Security (V261 RLS Policies)
- Team Management (V262 teams + team_members)

### Track 2: Test Infrastructure Overhaul (Strategisch)
- CRM Szenario-Builder (Lead-Journey, Customer-Journey, Opportunity-Pipeline)
- Faker-Integration (RealisticDataGenerator für deutsche Testdaten)
- Lead-spezifische TestDataFactories
- Test-Pattern Library & Documentation

## User Stories (8 Stories total)

**Track 1 (Business):**
1. Lead-Transfer Workflow (8-12h)
2. Fuzzy-Matching & Review (12-16h)
3. Row-Level-Security (10-14h)
4. Team Management (8-10h)

**Track 2 (Test Infrastructure):**
5. CRM Szenario-Builder (12-16h) ⭐ STRATEGISCH
6. Faker-Integration (4-6h)
7. Lead-spezifische TestDataFactories (6-8h)
8. Test-Pattern Library (4-6h)
```

**Begründung Track 2 (dokumentiert):**
- ✅ Quality Investment: Hochwertige Tests = weniger Bugs = schnellere Entwicklung
- ✅ Sprint 2.2 Readiness: Kundenmanagement braucht komplexe Customer-Test-Szenarien
- ✅ Onboarding: Neue Entwickler verstehen CRM-Workflows durch Testdaten
- ✅ Regression Prevention: Alle Edge-Cases als Test-Szenarien dokumentiert

---

### **3. TRIGGER_INDEX.md**
**Pfad:** `/docs/planung/TRIGGER_INDEX.md`

**Änderungen:**
- ✅ Sprint 2.1.5 Status: ✅ COMPLETE (Backend PR #124, Frontend PR #129, Enum PR #131)
- ✅ Sprint 2.1.6 Status: 🔧 IN PROGRESS (UPDATED 05.10.2025)
  - PRIORITY #0: Issue #130 Fix
  - Bestandsleads-Migration, Convert-Flow, Stop-the-Clock UI, Jobs
  - VERSCHOBEN: Lead-Transfer, RLS, Team Management, Fuzzy-Matching
- ✅ Sprint 2.1.7 Status: 📅 PLANNED (NEU 05.10.2025)
  - Track 1: Business Features (verschoben aus 2.1.6)
  - Track 2: Test Infrastructure Overhaul
  - Strategisches Investment

**Neue Zeilen:**
```markdown
🔧 TRIGGER_SPRINT_2_1_6.md - Lead Completion & Admin Features (UPDATED 05.10.2025)
   - **PRIORITY #0:** Issue #130 Fix (TestDataBuilder CDI-Konflikt - BLOCKER)
   - **VERSCHOBEN AUF 2.1.7:** Lead-Transfer, RLS, Team Management, Fuzzy-Matching

📋 TRIGGER_SPRINT_2_1_7.md - Team Management & Test Infrastructure (NEU 05.10.2025)
   - **Track 1 - Business:** Lead-Transfer, RLS, Team Management, Fuzzy-Matching
   - **Track 2 - Test Infra:** CRM Szenario-Builder, Faker-Integration, Test-Patterns
```

---

### **4. SPRINT_MAP.md (Modul 02)**
**Pfad:** `/docs/planung/features-neu/02_neukundengewinnung/SPRINT_MAP.md`

**Änderungen:**
- ✅ Sprint 2.1.6 aktualisiert: Titel + Scope + BLOCKER-Hinweis
- ✅ Verschobene Features als ❌ markiert mit Verweis auf 2.1.7
- ✅ Sprint 2.1.7 komplett neu dokumentiert (Track 1 + Track 2)

**Neue Abschnitte:**
```markdown
### **Sprint 2.1.6 – Lead Completion & Admin Features (IN PROGRESS)**
**Status:** 🔧 IN PROGRESS (2025-10-12 - 2025-10-18)

**⚠️ PRIORITY #0 - BLOCKER FIRST:**
- Issue #130: TestDataBuilder Refactoring (12 Tests broken, CI disabled)
- Root Cause: CDI-Konflikt zwischen Legacy Builder (src/main) und neuen Builder (src/test)

**❌ VERSCHOBEN AUF SPRINT 2.1.7:**
- ~~Lead-Transfer~~ (User Story 1 - zu komplex!)
- ~~Fuzzy-Matching~~ (User Story 4 - verdient eigenen Sprint)
- ~~RLS~~ (User Story 5 - komplex, gehört zu Transfer)
- ~~Team Management~~ (User Story 6 - komplex, gehört zu RLS)

### **Sprint 2.1.7 – Team Management & Test Infrastructure Overhaul (PLANNED)**
**Scope:** Lead-Transfer + RLS + Team Management + Test Infrastructure

**🎯 ZWEI PARALLELE TRACKS:**
- Track 1: Business Features (verschoben aus 2.1.6)
- Track 2: Test Infrastructure (NEU - STRATEGISCH!)
```

---

### **5. PRODUCTION_ROADMAP_2025.md**
**Pfad:** `/docs/planung/PRODUCTION_ROADMAP_2025.md`

**Änderungen:**
- ✅ Sprint 2.1.6 komplett neu beschrieben (IN PROGRESS, UPDATED 05.10.2025)
- ✅ Sprint 2.1.7 komplett neu beschrieben (PLANNED, NEU 05.10.2025)
- ✅ Live Dashboard aktualisiert (Blocker, verschobene Features)

**Neue Beschreibungen:**
```markdown
Sprint 2.1.6: Lead Completion         🔧 IN PROGRESS (12-18.10.2025) - UPDATED 05.10.2025
                                      → **PRIORITY #0 (BLOCKER):** Issue #130 - TestDataBuilder CDI-Konflikt
                                      → **QUICK FIX:** Legacy Builder löschen, Tests migrieren (1-2h)
                                      → **Kern-Deliverables:** Migration-API, Convert-Flow, Stop-the-Clock UI, Jobs
                                      → **Optional:** Lead-Scoring, Workflows, Activity-Timeline (ADR-006 Phase 2)
                                      → **VERSCHOBEN:** Lead-Transfer, RLS, Team Management, Fuzzy-Matching

Sprint 2.1.7: Team Mgmt & Test Infra  📅 PLANNED (19-25.10.2025) - NEU 05.10.2025
                                      → **Track 1 - Business:** Lead-Transfer, RLS, Team Management, Fuzzy-Matching
                                      → **Track 2 - Test Infra:** Szenario-Builder, Faker, TestDataFactories, Patterns
                                      → **Begründung Track 2:** Quality Investment für Sprint 2.2+ Velocity
```

---

### **6. ISSUE_130_ANALYSIS.md (NEU)**
**Pfad:** `/docs/planung/claude-work/daily-work/2025-10-05/ISSUE_130_ANALYSIS.md`

**Zweck:** Detaillierte Analyse + Migration Guide für Issue #130

**Inhalte:**
1. **Problem-Zusammenfassung (Nicht-Technisch)**
   - Analogie: Zwei Abteilungen mit verschiedenen Systemen
   - Konkreter Fehler erklärt
2. **Technische Analyse**
   - Root Cause: Doppelte Builder in src/main + src/test
   - CDI-Konflikt erklärt
   - 12 broken Tests aufgelistet
3. **Lösung: Quick Fix (1-2h)**
   - Schritt 1: Legacy Builder löschen
   - Schritt 2: Tests auf neue Builder migrieren
   - Schritt 3: ContactInteractionServiceIT umschreiben
   - Schritt 4: Worktree CI reaktivieren
4. **Migration Guide**
   - Pattern 1: Customer + Contact Creation (alt → neu)
   - Pattern 2: Contact with Position (alt → neu)
   - Pattern 3: Opportunity Creation (alt → neu)
5. **Next Steps**
   - Sofort: Issue #130 Fix
   - Sprint 2.1.6: Migration-API, Convert-Flow, Jobs
   - Sprint 2.1.7: Test Infrastructure Overhaul

---

## 🔗 DOKUMENTATIONS-PFADE FÜR NEUEN CLAUDE

### **Einstieg (Trigger-First Strategie):**

```
1. TRIGGER_INDEX.md lesen
   └─→ Übersicht aller Sprints, aktueller Status

2. TRIGGER_SPRINT_2_1_6.md öffnen
   └─→ User Story 0.5: Issue #130 (BLOCKER - MUST DO FIRST!)
   └─→ User Story 2-8: Kern-Deliverables (Migration, Convert, Jobs)
   └─→ Verschobene Features: Verweis auf 2.1.7

3. ISSUE_130_ANALYSIS.md lesen (bei Bedarf)
   └─→ Detaillierte Analyse + Migration Guide
   └─→ Nicht-technische Erklärung + Code-Beispiele

4. TRIGGER_SPRINT_2_1_7.md lesen (für Kontext)
   └─→ Verstehen, warum Features verschoben wurden
   └─→ Track 2: Test Infrastructure ist strategisch wichtig

5. SPRINT_MAP.md (Modul 02) lesen
   └─→ Modul-spezifische Navigation
   └─→ Sprint-Übersicht + Artefakte

6. PRODUCTION_ROADMAP_2025.md lesen
   └─→ Gesamtkontext: 36 PRs über 15 Wochen
   └─→ Phase 2 Status: 45% complete (Sprint 2.1.6 IN PROGRESS)
```

### **Wichtige Referenzen:**

- **Master Plan:** [CRM_COMPLETE_MASTER_PLAN_V5.md](../CRM_COMPLETE_MASTER_PLAN_V5.md)
- **AI Context:** [CRM_AI_CONTEXT_SCHNELL.md](../CRM_AI_CONTEXT_SCHNELL.md)
- **Testing Guide:** [TESTING_GUIDE.md](../../grundlagen/TESTING_GUIDE.md)
- **Issue #130:** https://github.com/joergstreeck/freshplan-sales-tool/issues/130

---

## ✅ VALIDIERUNG

### **Dokumentations-Konsistenz:**

- ✅ **TRIGGER_INDEX.md** verweist auf TRIGGER_SPRINT_2_1_6.md und TRIGGER_SPRINT_2_1_7.md
- ✅ **TRIGGER_SPRINT_2_1_6.md** User Story 1, 4, 5, 6 verweisen auf TRIGGER_SPRINT_2_1_7.md
- ✅ **SPRINT_MAP.md** verweist auf TRIGGER_SPRINT_2_1_7.md für verschobene Features
- ✅ **PRODUCTION_ROADMAP_2025.md** enthält beide Sprints mit Links
- ✅ **ISSUE_130_ANALYSIS.md** verweist auf TRIGGER_SPRINT_2_1_6.md User Story 0.5

### **Migrations-Nummern konsistent:**

- ✅ V259: Bereits deployed (remove_leads_company_city_unique_constraint.sql)
- ✅ V260: Nächste verfügbar (per `get-next-migration.sh`)
- ✅ Sprint 2.1.7: V260-V262 (lead_transfers, RLS Policies, teams)
- ✅ Alle Dokumente nutzen `get-next-migration.sh` Verweis statt feste V-Nummern

### **Sprint-Scope klar definiert:**

- ✅ **Sprint 2.1.6:** Admin-Features (Migration, Convert, Jobs) + Issue #130 Fix (BLOCKER)
- ✅ **Sprint 2.1.7:** Team-Features (Transfer, RLS, Team Mgmt) + Test Infrastructure (STRATEGISCH)
- ✅ Keine Überschneidungen, klare Abgrenzung

---

## 📊 ZUSAMMENFASSUNG FÜR NEUEN CLAUDE

### **Sprint 2.1.6 - Start hier:**

1. **PRIORITY #0 (1-2h):** Issue #130 Fix
   - Legacy Builder löschen (src/main/java/de/freshplan/test/builders/)
   - 12 Tests auf neue Builder migrieren (ContactInteractionServiceIT)
   - Worktree CI reaktivieren

2. **Kern-Deliverables (6-8 Tage):**
   - Bestandsleads-Migrations-API (Modul 08, Dry-Run + Real Import)
   - Lead → Kunde Convert Flow (POST /api/leads/{id}/convert)
   - Stop-the-Clock UI (Manager-only, StopTheClockDialog)
   - Backdating Endpoint (PUT /api/leads/{id}/registered-at)
   - Automated Jobs (Nightly: Progress Warning, Expiry, Pseudonymisierung)

3. **Optional (Falls Zeit):**
   - Lead-Scoring-System (Backend + Frontend)
   - Lead-Status-Workflows (UI)
   - Lead-Activity-Timeline
   - MUI Dialog Accessibility Fix

### **Sprint 2.1.7 - Danach:**

**Track 1 (Business):**
- Lead-Transfer Workflow
- Fuzzy-Matching & Review
- Row-Level-Security
- Team Management

**Track 2 (Test Infrastructure - STRATEGISCH!):**
- CRM Szenario-Builder
- Faker-Integration
- Lead-spezifische TestDataFactories
- Test-Pattern Library

**Begründung Track 2:**
- Quality Investment (bessere Tests = schnellere Entwicklung)
- Sprint 2.2 Readiness (komplexe Customer-Test-Szenarien)
- Onboarding (neue Entwickler verstehen CRM-Workflows)

---

**✅ Dokumentation vollständig aktualisiert**
**✅ Alle Pfade konsistent**
**✅ Neuer Claude kann nahtlos starten**

_Erstellt von Claude Code (Sonnet 4.5) am 2025-10-05_
