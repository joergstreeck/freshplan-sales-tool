---
sprint_id: "2.1.7.0"
title: "Design System Migration - FreshFoodz CI V2 + Test Infrastructure"
doc_type: "konzept"
status: "complete"
owner: "team/leads"
date_start: "2025-10-14"
date_end: "2025-10-15"
modules: ["frontend", "shared", "backend", "docs"]
entry_points:
  - "docs/planung/grundlagen/DESIGN_SYSTEM.md"
  - "frontend/src/components/layout/MainLayoutV2.tsx"
  - "docs/planung/CRM_AI_CONTEXT_SCHNELL.md"
  - "docs/planung/claude-work/daily-work/2025-10-14/SPRINT_2_1_7_0_COMPLETE_SUMMARY.md"
pr_refs: ["#140"]
pr_merge_commit: "f6642321b"
date_merged: "2025-10-15 22:54 CEST"
updated: "2025-10-15 23:00"
---

# 🎯 TRIGGER SPRINT 2.1.7.0 - Layout System Cleanup + CRM_AI_CONTEXT Restructure

**📍 Navigation:** Home → Planung → Sprint 2.1.7.0

**Sprint-Typ:** Refactoring & Code Cleanup + Documentation Restructure
**Priorität:** MEDIUM (Technische Schuld beseitigen + KI-Onboarding optimieren)
**Zeithorizont:** 12 Stunden (tatsächlich - inkl. CRM_AI_CONTEXT Phase 3+4)
**Abhängigkeiten:** Sprint 2.1.7 COMPLETE ✅
**Status:** ✅ GEMERGED (PR #140 - MERGED TO MAIN - 15.10.2025 22:54 CEST)
**PR:** #140 - https://github.com/joergstreeck/freshplan-sales-tool/pull/140
**Merge Commit:** f6642321b
**CI Status:** ✅ 29/29 Workflows GREEN
**Dokumentation:** [SPRINT_2_1_7_0_COMPLETE_SUMMARY.md](claude-work/daily-work/2025-10-14/SPRINT_2_1_7_0_COMPLETE_SUMMARY.md)

---

> **🎯 Arbeitsanweisung – Reihenfolge**
> 1. **Dieses Dokument komplett lesen** (Ziel, Deliverables, Hinweise verstehen)
> 2. **PLANUNGSMETHODIK.md** prüfen (Git Workflow Policy!)
>
> **TEIL A: Design System Migration (COMPLETE ✅)**
> 3. **Phase 1:** MainLayoutV2 Code erweitern (30 Min) ✅
> 4. **Phase 2:** Seiten-Inventar erstellen (30 Min) ✅
> 5. **Phase 3:** Cleanup in 6 Batches (4h) ✅
> 6. **Phase 4:** Dokumentation aktualisieren (30 Min) ✅
> 7. **Phase 5:** Tests & Validierung (45 Min) ✅
>
> **TEIL B: CRM_AI_CONTEXT_SCHNELL.md Restructure (ACTIVE 🔄)**
> 8. **Phase 1-2:** Neue thematische Struktur erstellt (COMPLETE ✅)
> 9. **Phase 3:** Quick Facts + Common Pitfalls hinzugefügt (COMPLETE ✅)
> 10. **Phase 4:** IST vs. SOLL Trennung - Opportunities Frontend Gap (IN PROGRESS 🔄)
> 11. **Phase 5:** Codebase-Validierung - Backend (PENDING ⏳)
> 12. **Phase 6:** Codebase-Validierung - Frontend (PENDING ⏳)
> 13. **Phase 7:** IST-Zustand-Liste erstellen (PENDING ⏳)

---

## 🚫 GIT WORKFLOW POLICY (KRITISCH!)

**NIEMALS `git push` ohne explizite User-Erlaubnis!**

### Erlaubt (automatic):
- ✅ `git add .` (Änderungen stagen)
- ✅ `git commit -m "..."` (lokaler Commit mit Message)
- ✅ `git status` (Status prüfen)
- ✅ `git diff` (Änderungen anzeigen)

### VERBOTEN (requires user permission):
- ❌ `git push` (zu Remote pushen)
- ❌ `git push --force` (Force-Push)
- ❌ `gh pr create` (Pull Request erstellen)

### Commit Message Template:
```
Sprint 2.1.7.0: Layout System Cleanup (Option A)

- MainLayoutV2: Add maxWidth prop (default: 'xl')
- Remove 22 double-container anti-patterns
- Update DESIGN_SYSTEM.md (reflect reality)
- Delete DESIGN_SYSTEM_V2.md (obsolete)

Changes:
- MainLayoutV2.tsx: +4 lines (interface + implementation)
- 22 Pages: -110 lines (containers removed)
- DESIGN_SYSTEM.md: updated (30 lines)
- DESIGN_SYSTEM_V2.md: deleted

Tests:
- ✅ Visual Check: All 23 pages (Desktop, Laptop, Tablet)
- ✅ ESLint: 0 new warnings
- ✅ Build: SUCCESS

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

---

## 📋 EXECUTIVE SUMMARY

### TEIL A: Design System Migration ✅ COMPLETE

**Problem:**
22 von 23 Seiten nutzen ein **Double-Container Anti-Pattern**: `MainLayoutV2` (maxWidth: 'xl') + MUI `Container` (maxWidth: 'xl'/'lg'/'md'). Dies führt zu:
- Inkonsistenter Breitensteuerung (8× xl, 4× lg, 1× sm)
- Unnötiger Code-Duplikation (2 Container statt 1)
- Verwirrung für Entwickler (welcher Container steuert die Breite?)
- Dokumentation widerspricht dem Code

**Lösung:**
**Option A (Minimal)** - MainLayoutV2 um eine einzige Prop erweitern:
```typescript
interface MainLayoutV2Props {
  children: React.ReactNode;
  showHeader?: boolean;
  hideHeader?: boolean;
  maxWidth?: 'full' | 'xl' | 'lg' | 'md' | 'sm'; // NEU, default: 'xl'
}
```

Dann 22 doppelte Container entfernen. **Fertig.**

**Business Impact:**
- ✅ **Wartbarkeit:** Ein Container statt zwei (weniger Code)
- ✅ **Klarheit:** Breite explizit per Prop gesteuert
- ✅ **Backward Compatibility:** Default 'xl' = keine Breaking Changes
- ✅ **Zeit:** 5-6h statt 2-4 Tage (SmartLayout-Ansatz)

**Risiko:** VERY LOW (nur 1 Prop hinzufügen, default = aktuelles Verhalten)

---

### TEIL B: CRM_AI_CONTEXT_SCHNELL.md Restructure 🔄 ACTIVE

**Problem:**
Das aktuelle `CRM_AI_CONTEXT_SCHNELL_ALT.md` ist chronologisch nach Sprints strukturiert (Sprint 2.1.4, 2.1.6, 2.1.7, etc.). Dies führt zu:
- **Unübersichtlichkeit:** Niemand (auch keine KI) kann 780+ Zeilen Sprint-Protokolle überblicken
- **Schlechtes KI-Onboarding:** Neue Claude-Instanzen brauchen 10+ Minuten um Kontext zu verstehen
- **Duplikationen:** Migrations und Features werden mehrfach erwähnt (über mehrere Sprints)
- **Keine IST-Validierung:** Dokumentation beschreibt Planung, nicht Codebase-Realität

**Lösung:**
**Thematische Struktur** statt chronologischer Sprint-Logs:
1. **Quick Facts** (30 Sekunden KI-Onboarding) ✅ NEU
2. **Common Pitfalls** (Fehler vermeiden) ✅ NEU
3. **Strategischer Kontext** (Business-Mission, ROI)
4. **System-Architektur** (8-Module-Ecosystem, Infrastructure)
5. **Technical Implementation** (Tech-Stack, Event-Backbone, **Migrations konsolidiert**)
6. **Frontend & Design** (Theme V2, SmartLayout)
7. **Development-Standards** (Code-Standards, Testing)
8. **Codebase-Reality** (IST vs. SOLL, Modul-Status-Matrix, **Gaps dokumentiert**)

**Business Impact:**
- ✅ **KI-Onboarding:** 10+ Minuten → 30 Sekunden (Quick Facts)
- ✅ **Fehlerreduktion:** Common Pitfalls verhindert typische KI-Fehler
- ✅ **IST-Validierung:** Dokumentation spiegelt Codebase-Realität wider
- ✅ **Wartbarkeit:** Thematisch = einfach zu aktualisieren (kein Sprint-Protokoll-Chaos)

**Zeithorizont:** 1.5h (Phase 3-7)
- Phase 3: Quick Facts + Pitfalls ✅ 20 Min (DONE)
- Phase 4: IST vs. SOLL Trennung 🔄 15 Min (IN PROGRESS)
- Phase 5-6: Codebase-Validierung ⏳ 50 Min (Backend + Frontend)
- Phase 7: IST-Zustand-Liste ⏳ 10 Min

**Risiko:** VERY LOW (nur Dokumentations-Refactoring, kein Code-Impact)

---

## 🎯 STRATEGISCHE ZIELE

### Technical-Ziele
1. **Code Cleanup:** Doppelte Container eliminieren (22 Seiten betroffen)
2. **Explizite Breiten-Steuerung:** Prop statt impliziter Verschachtelung
3. **Backward Compatibility:** Default-Wert 'xl' erhält aktuelles Verhalten
4. **YAGNI:** Nur was JETZT gebraucht wird (keine Automation, keine Variants)

### UX-Ziele
1. **Keine Änderung:** User sieht keinen Unterschied (reines Refactoring)
2. **Konsistenz:** Alle Seiten folgen gleichem Muster
3. **Lesbarkeit:** Code ist einfacher zu verstehen

---

## 📊 IST-ANALYSE

### Aktueller Zustand (Codebase-Reality)

**Code-Statistik:**
```bash
# Verwendung MainLayoutV2
grep -r "MainLayoutV2" frontend/src/pages/*.tsx | wc -l
# → 23 Seiten (23/23 = 100%)

# Verwendung SmartLayout
grep -r "SmartLayout" frontend/src/pages/*.tsx | wc -l
# → 0 Seiten (SmartLayout existiert, aber ungenutzt)

# Double-Container Pattern
grep -A5 "MainLayoutV2" frontend/src/pages/*.tsx | grep -c "Container maxWidth"
# → 22 Seiten haben doppelte Container (22/23 = 96%)
```

**Betroffene Seiten (Double-Container):**

| Seite | Container 1 | Container 2 | Status |
|-------|-------------|-------------|--------|
| LeadDetailPage.tsx | MainLayoutV2 (xl) | Container (xl) | ❌ Doppelt |
| LeadListPage.tsx | MainLayoutV2 (xl) | Container (xl) | ❌ Doppelt |
| LeadWizard.tsx | MainLayoutV2 (xl) | Container (xl) | ❌ Doppelt |
| TerritoryListPage.tsx | MainLayoutV2 (xl) | Container (xl) | ❌ Doppelt |
| LoginPage.tsx | MainLayoutV2 (xl) | Container (xl) | ❌ Doppelt |
| RegisterPage.tsx | MainLayoutV2 (xl) | Container (xl) | ❌ Doppelt |
| ForgotPasswordPage.tsx | MainLayoutV2 (xl) | Container (xl) | ❌ Doppelt |
| ResetPasswordPage.tsx | MainLayoutV2 (xl) | Container (xl) | ❌ Doppelt |
| NotFoundPage.tsx | MainLayoutV2 (xl) | Container (sm) | ❌ Doppelt |
| UnauthorizedPage.tsx | MainLayoutV2 (xl) | Container (lg) | ❌ Doppelt |
| ErrorBoundaryPage.tsx | MainLayoutV2 (xl) | Container (lg) | ❌ Doppelt |
| MaintenancePage.tsx | MainLayoutV2 (xl) | Container (lg) | ❌ Doppelt |
| ComingSoonPage.tsx | MainLayoutV2 (xl) | Container (lg) | ❌ Doppelt |
| DashboardPage.tsx | MainLayoutV2 (xl) | Container (xl) | ❌ Doppelt |
| ... (weitere 8 Seiten) | ... | ... | ❌ Doppelt |
| CustomersPageV2.tsx | MainLayoutV2 (xl) | - | ✅ Korrekt |
| SettingsPage.tsx | MainLayoutV2 (xl) | - | ✅ Korrekt |

**Inkonsistente Breiten:**
- 8× Container maxWidth="xl" (1536px)
- 4× Container maxWidth="lg" (1200px)
- 1× Container maxWidth="sm" (600px)
- 2× KEIN Container (korrekt!)

**Dokumentations-Gap:**
- `DESIGN_SYSTEM.md` beschreibt SmartLayout (theoretisch, nicht implementiert)
- `DESIGN_SYSTEM_V2.md` existiert im Archiv (veraltet, sollte gelöscht werden)
- Code nutzt MainLayoutV2 (reale Implementation widerspricht Doku)

---

## 🏗️ SOLL-ZUSTAND (Nach Cleanup)

### Option A: Minimal (GEWÄHLT)

**Änderung 1: MainLayoutV2 Interface erweitern**
```typescript
// frontend/src/components/layout/MainLayoutV2.tsx
interface MainLayoutV2Props {
  children: React.ReactNode;
  showHeader?: boolean;
  hideHeader?: boolean;
  maxWidth?: 'full' | 'xl' | 'lg' | 'md' | 'sm'; // NEU, default: 'xl'
}
```

**Änderung 2: MainLayoutV2 Implementation anpassen**
```typescript
// frontend/src/components/layout/MainLayoutV2.tsx (Zeile ~112)
<Box
  sx={{
    p: { xs: 2, sm: 3, md: 4 },
    maxWidth: maxWidth === 'full' ? '100%' : maxWidth || 'xl', // GEÄNDERT
    mx: 'auto',
    minHeight: '100vh',
    position: 'relative',
    width: '100%',
    contain: 'layout style',
  }}
>
  {children}
</Box>
```

**Änderung 3: Seiten aufräumen (22× Beispiel)**
```diff
// VORHER (Double-Container)
<MainLayoutV2>
  <Container maxWidth="xl" sx={{ py: 4 }}>
    <Typography>Inhalt</Typography>
  </Container>
</MainLayoutV2>

// NACHHER (Single-Container mit expliziter Prop)
<MainLayoutV2 maxWidth="xl">
  <Typography sx={{ py: 4 }}>Inhalt</Typography>
</MainLayoutV2>
```

**Geschätzte Änderungen:**
- 1 Interface (3 Zeilen)
- 1 Implementation (1 Zeile)
- 22 Seiten × ~5 Zeilen = **110 Zeilen** entfernt
- **Gesamt: +4 Zeilen, -110 Zeilen = -106 Zeilen Code** (cleaner!)

---

## 🎯 SCOPE & DELIVERABLES

### Phase 1: MainLayoutV2 erweitern (30 min)
- [x] Entscheidung für Option A (Minimal)
- [ ] Interface erweitern: `maxWidth?: 'full' | 'xl' | 'lg' | 'md' | 'sm'`
- [ ] Implementation anpassen: `maxWidth || 'xl'` (default)
- [ ] Backward Compatibility sicherstellen (default 'xl')

### Phase 2: Seiten-Inventar (30 min)
- [ ] Alle 23 Seiten analysieren
- [ ] Kategorisieren: welche brauchen 'full', welche 'lg', welche 'md'?
- [ ] Liste erstellen: Page → Target maxWidth

**Erwartetes Inventar:**
```
FULL (100% Breite):
- CustomersPageV2.tsx (bereits korrekt, nur Prop hinzufügen)
- DashboardPage.tsx (Container entfernen, maxWidth="full")
- LeadListPage.tsx (Container entfernen, maxWidth="full")
- TerritoryListPage.tsx (Container entfernen, maxWidth="full")

XL (1536px - Default):
- LeadDetailPage.tsx (Container entfernen, keine Prop nötig)
- SettingsPage.tsx (bereits korrekt, keine Änderung)
- (alle anderen Seiten falls nicht anders benötigt)

LG (1200px):
- UnauthorizedPage.tsx (Container entfernen, maxWidth="lg")
- ErrorBoundaryPage.tsx (Container entfernen, maxWidth="lg")
- MaintenancePage.tsx (Container entfernen, maxWidth="lg")
- ComingSoonPage.tsx (Container entfernen, maxWidth="lg")

SM (600px):
- NotFoundPage.tsx (Container entfernen, maxWidth="sm")
```

### Phase 3: Cleanup (2.5 Stunden)
**Batch 1: Tabellen-Seiten (volle Breite)**
- [ ] CustomersPageV2.tsx → `maxWidth="full"` Prop hinzufügen
- [ ] LeadListPage.tsx → Container entfernen + `maxWidth="full"`
- [ ] TerritoryListPage.tsx → Container entfernen + `maxWidth="full"`
- [ ] DashboardPage.tsx → Container entfernen + `maxWidth="full"`

**Batch 2: Formular-Seiten (xl = Default)**
- [ ] LeadDetailPage.tsx → Container entfernen (kein Prop nötig, xl ist default)
- [ ] LeadWizard.tsx → Container entfernen
- [ ] LoginPage.tsx → Container entfernen
- [ ] RegisterPage.tsx → Container entfernen
- [ ] ForgotPasswordPage.tsx → Container entfernen
- [ ] ResetPasswordPage.tsx → Container entfernen

**Batch 3: Error/Info-Seiten (lg/sm)**
- [ ] UnauthorizedPage.tsx → Container entfernen + `maxWidth="lg"`
- [ ] ErrorBoundaryPage.tsx → Container entfernen + `maxWidth="lg"`
- [ ] MaintenancePage.tsx → Container entfernen + `maxWidth="lg"`
- [ ] ComingSoonPage.tsx → Container entfernen + `maxWidth="lg"`
- [ ] NotFoundPage.tsx → Container entfernen + `maxWidth="sm"`

**Batch 4: Restliche Seiten**
- [ ] ... (alle übrigen Seiten analysieren + aufräumen)

### Phase 4: Dokumentation (30 min)
- [ ] DESIGN_SYSTEM_V2.md **LÖSCHEN** (nicht archivieren - "falsche Konzepte verdienen kein Archiv")
- [ ] DESIGN_SYSTEM.md aktualisieren (MainLayoutV2 Realität beschreiben, 30 Zeilen)
- [ ] Kein ausufernder Text, nur Facts: Interface, Props, Beispiele

### Phase 5: Testing (45 min)
- [ ] Visual Check: Alle 23 Seiten im Browser öffnen
- [ ] Responsive Check: Desktop (1920px), Laptop (1440px), Tablet (768px)
- [ ] ESLint: `npm run lint` (0 neue Warnings)
- [ ] Build: `npm run build` (0 Errors)

---

## 🚨 RISIKEN & MITIGATION

### Risk 1: Breaking Changes durch Default-Wert
**Wahrscheinlichkeit:** VERY LOW
**Impact:** MEDIUM
**Mitigation:**
- Default `maxWidth || 'xl'` entspricht aktuellem Verhalten
- Alle Seiten ohne Prop-Änderung funktionieren exakt wie vorher
- Nur explizite Prop-Setzungen ändern Verhalten (bewusst!)

### Risk 2: Padding/Spacing Unterschiede
**Wahrscheinlichkeit:** LOW
**Impact:** LOW
**Mitigation:**
- User-Screenshot bestätigt: KEIN Padding-Problem im Cockpit
- Kein `variant` Prop nötig (YAGNI!)
- Falls doch Padding-Probleme: Einzelne Seiten mit `sx={{ py: 4 }}` anpassen

### Risk 3: Übersehene Seiten
**Wahrscheinlichkeit:** MEDIUM
**Impact:** LOW
**Mitigation:**
- Phase 2: Vollständiges Inventar aller 23 Seiten
- Phase 5: Visual Check ALLER Seiten (nicht nur Stichproben)
- Git Diff vor Commit reviewen

---

## 📏 SUCCESS METRICS

### Quantitative Metriken
- ✅ **MainLayoutV2 erweitert:** 1 Prop hinzugefügt
- ✅ **22 Seiten bereinigt:** Container entfernt
- ✅ **-106 Zeilen Code:** Weniger Code = cleaner
- ✅ **0 Breaking Changes:** Alle Seiten funktionieren

### Qualitative Metriken
- ✅ **Code Clarity:** Ein Container statt zwei
- ✅ **Consistency:** Alle Seiten folgen gleichem Muster
- ✅ **YAGNI Applied:** Nur 1 Prop, keine Overengineering

### Business Metriken
- ✅ **Zeit:** 5-6h statt 2-4 Tage (SmartLayout-Ansatz)
- ✅ **Risiko:** VERY LOW (minimal changes)
- ✅ **Wartbarkeit:** Einfacher für neue Entwickler

---

## 🛠️ TECHNICAL IMPLEMENTATION

### Architektur-Änderungen

**Component-Hierarchie (unverändert):**
```
App
├── MainLayoutV2 (ENHANCED - 1 neue Prop)
│   ├── Header
│   ├── Navigation
│   └── Content (jetzt mit flexibler Breite via Prop)
└── SmartLayout (EXISTS but UNUSED - 0 Usages)
```

**Code-Änderungen:**

**1. MainLayoutV2.tsx Interface**
```diff
interface MainLayoutV2Props {
  children: React.ReactNode;
  showHeader?: boolean;
  hideHeader?: boolean;
+ maxWidth?: 'full' | 'xl' | 'lg' | 'md' | 'sm'; // default: 'xl'
}
```

**2. MainLayoutV2.tsx Implementation**
```diff
<Box
  sx={{
    p: { xs: 2, sm: 3, md: 4 },
-   maxWidth: 'xl',
+   maxWidth: maxWidth === 'full' ? '100%' : maxWidth || 'xl',
    mx: 'auto',
    minHeight: '100vh',
    position: 'relative',
    width: '100%',
    contain: 'layout style',
  }}
>
  {children}
</Box>
```

**3. Beispiel-Migrationen**

**Tabellen-Seite (volle Breite):**
```diff
// CustomersPageV2.tsx
- <MainLayoutV2>
+ <MainLayoutV2 maxWidth="full">
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <CustomerListHeader />
    </Box>
  </MainLayoutV2>
```

**Formular-Seite (default xl):**
```diff
// LeadDetailPage.tsx
  <MainLayoutV2>
-   <Container maxWidth="xl" sx={{ py: 4 }}>
-     <Typography>Lädt...</Typography>
-   </Container>
+   <Typography sx={{ py: 4 }}>Lädt...</Typography>
  </MainLayoutV2>
```

**Error-Seite (lg):**
```diff
// UnauthorizedPage.tsx
- <MainLayoutV2>
+ <MainLayoutV2 maxWidth="lg">
-   <Container maxWidth="lg">
      <Typography>Keine Berechtigung</Typography>
-   </Container>
  </MainLayoutV2>
```

---

## 🧪 TESTING-STRATEGIE

### Manual Visual Testing (PRIMARY)
```
Phase 5 (45 min):
1. Dev-Server starten: npm run dev
2. Alle 23 Seiten öffnen + visuell prüfen
3. Responsive Testing:
   - Desktop: 1920px (volle Breite für Tabellen?)
   - Laptop: 1440px (normale Ansicht)
   - Tablet: 768px (mobile Layout)
4. Kein Screenshot-Diff nötig (nur visueller Check)
```

### Automated Testing (SECONDARY)
```bash
# ESLint (0 neue Warnings)
npm run lint

# Build (0 Errors)
npm run build

# Optional: Type-Check
npm run type-check
```

### Git Diff Review (MANDATORY)
```bash
# Vor Commit: Review aller Änderungen
git diff frontend/src/components/layout/MainLayoutV2.tsx
git diff frontend/src/pages/*.tsx

# Erwartung:
# - MainLayoutV2.tsx: +4 Zeilen (Interface + Implementation)
# - 22 Pages: jeweils -5 Zeilen (Container entfernt)
```

---

## 📅 TIMELINE & MILESTONES

**Gesamtdauer: 5-6 Stunden**

### Milestone 1: Vorbereitung (1 Stunde)
- ✅ TRIGGER_SPRINT_2_1_7_0.md aktualisiert (Option A dokumentiert)
- ✅ Feature-Branch angelegt: `feature/sprint-2-1-7-0-layout-cleanup`
- [ ] Phase 1: MainLayoutV2 erweitern (30 min)
- [ ] Phase 2: Seiten-Inventar erstellen (30 min)

### Milestone 2: Implementation (2.5 Stunden)
- [ ] Batch 1: Tabellen-Seiten (4 Seiten, 45 min)
- [ ] Batch 2: Formular-Seiten (6 Seiten, 60 min)
- [ ] Batch 3: Error/Info-Seiten (5 Seiten, 30 min)
- [ ] Batch 4: Restliche Seiten (7 Seiten, 15 min)

### Milestone 3: Dokumentation & Testing (1.5 Stunden)
- [ ] Phase 4: Dokumentation aktualisieren (30 min)
- [ ] Phase 5: Testing + Visual Check (45 min)
- [ ] Git Commit vorbereiten (15 min)

### Milestone 4: Review & Merge
- [ ] User-Review (Stichproben im Browser)
- [ ] Git Commit (siehe Commit Message Template oben in Git Workflow Policy)
- [ ] **KEIN git push ohne User-Erlaubnis!** (siehe Git Workflow Policy oben)

---

## 🔗 DEPENDENCIES & PREREQUISITES

### Hard Dependencies (BLOCKING)
- ✅ Sprint 2.1.7 COMPLETE (ActivityOutcome Feature gemerged)
- ✅ MainLayoutV2.tsx existiert (frontend/src/components/layout/MainLayoutV2.tsx)
- ✅ 23 Seiten nutzen MainLayoutV2 (grep bestätigt)

### Soft Dependencies (NICHT BLOCKING)
- ⏳ Opportunities Frontend UI (kann parallel entwickelt werden)
- ⏳ Team Management Features (kann parallel entwickelt werden)

### Prerequisites
- ✅ Node.js 18+ & npm 9+
- ✅ Frontend Dev-Server läuft (`npm run dev`)
- ✅ Git-Branch angelegt

---

## 📚 RELATED DOCUMENTS

### Planungsdokumente
- `/docs/planung/DESIGN_SYSTEM.md` - **Wird aktualisiert** (MainLayoutV2 Reality)
- `/docs/planung/archiv/design-alt/design/DESIGN_SYSTEM_V2.md` - **Wird gelöscht**
- `/docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md` - Master Plan
- `/docs/planung/PRODUCTION_ROADMAP_2025.md` - Roadmap

### Technical Documentation
- `/frontend/src/components/layout/MainLayoutV2.tsx` - **Wird erweitert** (1 Prop)
- `/frontend/src/pages/*.tsx` - **22 Seiten bereinigen**
- `/frontend/README.md` - Frontend Setup

### Alternative (NICHT gewählt):
- `/docs/planung/alternatives/OPTION_B_CONVENTION_LAYER.md` - Convention over Configuration (verworfen wegen YAGNI)
- `/docs/planung/alternatives/OPTION_C_SMART_LAYOUT.md` - SmartLayout Migration (verworfen wegen Overengineering)

---

## 🎯 ACCEPTANCE CRITERIA

### Functional Acceptance
- [ ] MainLayoutV2 hat `maxWidth` Prop (default: 'xl')
- [ ] 22 Seiten haben keine doppelten Container mehr
- [ ] Alle 23 Seiten funktionieren wie vorher (0 Breaking Changes)
- [ ] Visual Check: Keine Layout-Unterschiede (außer explizite Prop-Änderungen)

### Technical Acceptance
- [ ] ESLint: 0 neue Warnings
- [ ] Build: SUCCESS (npm run build)
- [ ] Type-Check: SUCCESS (TypeScript)
- [ ] Git Diff: +4 Zeilen MainLayoutV2, -110 Zeilen Pages

### Documentation Acceptance
- [ ] DESIGN_SYSTEM.md aktualisiert (beschreibt MainLayoutV2 Reality, ~30 Zeilen)
- [ ] DESIGN_SYSTEM_V2.md gelöscht (nicht archiviert)
- [ ] TRIGGER_SPRINT_2_1_7_0.md finalisiert (Option A dokumentiert)

---

## 📝 NOTES & LEARNINGS

### Warum Option A (Minimal)?

**Verworfen: SmartLayout Migration (ursprünglicher Plan)**
- ❌ SmartLayout existiert, aber ungenutzt (0/23 Seiten)
- ❌ Auto-Detection fragil (pathname.includes('/list') = String-Matching)
- ❌ Production Bugs (minification bricht element.type.name)
- ❌ 2-4 Tage Aufwand für theoretisches System

**Verworfen: Option B (Convention Layer)**
- ❌ YAGNI: Wir brauchen JETZT keine Auto-Detection
- ❌ String-Matching fragil (pathname.includes = false positives)
- ❌ User hat keine Probleme mit aktuellem Layout (Screenshot bestätigt)

**Gewählt: Option A (Minimal)**
- ✅ 1 Prop = simpelste Lösung
- ✅ Explizit statt implizit (klar, wartbar)
- ✅ 5-6h statt 2-4 Tage
- ✅ YAGNI Applied: Nur was JETZT gebraucht wird

### External AI Feedback (berücksichtigt)
> "4 Props sind Premature Optimization. [...] Das ist wieder Overengineering - aber diesmal in der Doku! [...] Convention over Configuration ist elegant, aber: YAGNI! [...] DESIGN_SYSTEM_V2.md sollte GELÖSCHT werden, nicht archiviert - falsche Konzepte verdienen kein Archiv."

**Angewendet:**
- ✅ Nur 1 Prop (maxWidth)
- ✅ Dokumentation minimal (30 Zeilen DESIGN_SYSTEM.md)
- ✅ Keine Convention Layer (YAGNI)
- ✅ DESIGN_SYSTEM_V2.md LÖSCHEN statt archivieren

### Open Questions (RESOLVED)
1. **Brauchen wir `variant` Prop für Padding?**
   → ❌ NEIN (User-Screenshot bestätigt: Cockpit hat kein Padding-Problem)

2. **Brauchen wir Auto-Detection?**
   → ❌ NEIN (YAGNI! User hat keine Probleme mit manuellem Prop-Setzen)

3. **Sollen wir SmartLayout migrieren?**
   → ❌ NEIN (existiert, aber ungenutzt - nicht production-ready)

---

## 🚀 NEXT STEPS (für nächste Session)

### Sofort starten:
1. **Feature-Branch anlegen:**
   ```bash
   git checkout main
   git pull
   git checkout -b feature/sprint-2-1-7-0-layout-cleanup
   ```

2. **Phase 1: MainLayoutV2 erweitern** (30 min)
   - Interface: `maxWidth?: 'full' | 'xl' | 'lg' | 'md' | 'sm'`
   - Implementation: `maxWidth || 'xl'` (default)

3. **Phase 2: Seiten-Inventar** (30 min)
   - Alle 23 Seiten analysieren
   - Kategorisieren: full/xl/lg/md/sm

4. **Phase 3-5: Implementation + Testing** (4 Stunden)
   - Batches 1-4 abarbeiten
   - Visual Testing
   - Dokumentation aktualisieren

### Nach Implementierung:
- **User-Review** (Stichproben im Browser)
- **Git Commit** (mit Template-Message)
- **WARTEN auf User-Erlaubnis für `git push`!**

---

**Created:** 2025-10-14 13:25
**Updated:** 2025-10-14 14:30 (Option A Dokumentation)
**Author:** @joergstreeck + Claude
**Sprint Type:** Refactoring & Code Cleanup
**Priority:** MEDIUM (Technische Schuld)
**Status:** 📝 DOKUMENTATION COMPLETE, READY FOR IMPLEMENTATION
