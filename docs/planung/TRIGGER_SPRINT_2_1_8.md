---
sprint_id: "2.1.8"
title: "DSGVO Compliance & Lead-Import"
doc_type: "trigger"
status: "complete"
owner: "Claude Code"
date_start: "2025-12-04"
date_end: "2025-12-05"
branch: "feature/sprint-2-1-8-dsgvo-lead-import"
modules: ["02_neukundengewinnung"]
updated: "2025-12-05"
commits: 16
files_changed: 285
lines_added: 21526
lines_removed: 806
---

# Sprint 2.1.8 – DSGVO Compliance & Lead-Import

**Branch:** `feature/sprint-2-1-8-dsgvo-lead-import`
**Status:** ✅ COMPLETE (05.12.2025)

---

## 🎯 Sprint-Ziel (Kurzfassung)

**Gesetzliche Pflicht-Features + B2B-Standard Lead-Import**

| Deliverable | Priorität | Status |
|-------------|-----------|--------|
| DSGVO Art. 15 (Auskunft) | 🔴 PFLICHT | ✅ DONE |
| DSGVO Art. 17 (Löschung) | 🔴 PFLICHT | ✅ DONE |
| DSGVO Art. 7.3 (Widerruf) | 🟡 SOLLTE | ✅ DONE |
| Lead-Import (CSV/Excel) | 🔴 KRITISCH | ✅ DONE |
| Fuzzy Auto-Mapping (3-Tier) | 🟡 WICHTIG | ✅ DONE |
| Historical Import (originalCreatedAt) | 🟡 WICHTIG | ✅ DONE |
| Admin-UI (/admin/dsgvo, /admin/imports) | 🟡 WICHTIG | ✅ DONE |
| Advanced Search (pg_trgm) | 🟢 KANN | ✅ DONE |
| BANT-Qualifizierung | 🟢 KANN | ⬜ DEFERRED |

---

## 📋 Arbeitsanweisung

### Pflicht-Checks vor Arbeitsbeginn

```bash
# 1. Branch prüfen
git branch --show-current
# Erwartet: feature/sprint-2-1-8-dsgvo-lead-import

# 2. Migration-Nummer holen (NIEMALS hardcoden!)
./scripts/get-next-migration.sh

# 3. Services starten
cd backend && ./mvnw quarkus:dev
cd frontend && npm run dev
```

### Artefakte lesen

**Vor Implementierung diese Dokumente lesen:**

1. **DECISIONS.md** - Getroffene Entscheidungen
   → `docs/planung/artefakte/sprint-2.1.8/DECISIONS.md`

2. **DSGVO_TECHNICAL_SPEC.md** - DSGVO-Implementierung
   → `docs/planung/artefakte/sprint-2.1.8/DSGVO_TECHNICAL_SPEC.md`

3. **LEAD_IMPORT_SPEC.md** - Import-System
   → `docs/planung/artefakte/sprint-2.1.8/LEAD_IMPORT_SPEC.md`

---

## 🔧 GIT WORKFLOW

### ✅ ERLAUBT (ohne User-Freigabe):
- `git commit` - Commits erstellen
- `git add` - Dateien stagen
- Feature-Branches anlegen

### 🚫 VERBOTEN (ohne explizite User-Freigabe):
- **`git push`** - NIEMALS ohne User-Erlaubnis!
- **PR-Erstellung** - Nur auf Anforderung
- **PR-Merge** - Nur mit User-OK

---

## 📦 Phasen-Übersicht

### Phase 1: DSGVO-Kern (Prio 1) ✅ ABGESCHLOSSEN

**Scope:** Art. 15, 17, 7.3 Implementierung

- [x] Migration: DSGVO-Felder + Tabellen (V10050)
- [x] GdprService (Backend) - Löschung, Auskunft, Widerruf
- [x] GdprPdfGeneratorService (OpenPDF - Apache 2.0)
- [x] GdprResource (REST Endpoints)
- [x] Frontend: GdprActionsMenu, GdprDeleteDialog, GdprDeletedBadge
- [x] Tests: 41 Tests (Unit + Integration) - ALLE GRÜN

**Implementierte Entitäten:**
- `GdprDataRequest.java` - Art. 15 Datenexport-Anfragen
- `GdprDeletionLog.java` - Art. 17 Löschprotokolle

**Details:** → `artefakte/sprint-2.1.8/DSGVO_TECHNICAL_SPEC.md`

### Phase 2: Lead-Import (Prio 2) ✅ ABGESCHLOSSEN

**Scope:** Self-Service Import mit Quota-System

- [x] Migration V10051: import_logs Tabelle
- [x] ImportQuotaService (Quota-Check + Management)
- [x] FileParserService (CSV/Excel Parser)
- [x] SelfServiceImportService + REST Endpoints
- [x] Frontend: LeadImportWizard (4 Steps: Upload → Mapping → Preview → Execute)
- [x] Frontend: Integration in LeadsPage
- [x] Tests: 13 Unit-Tests für ImportQuotaService

**Details:** → `artefakte/sprint-2.1.8/LEAD_IMPORT_SPEC.md`

### Phase 3: Admin-UI + Routing ✅ ABGESCHLOSSEN

**Scope:** Neue Admin-Routen

- [x] `/admin/dsgvo` - DsgvoAdminPage (Löschungen, Anfragen, gelöschte Leads)
- [x] `/admin/imports` - ImportsAdminPage (Approve/Reject Workflow)
- [x] Navigation-Integration (AdminDashboard Schnellzugriff)

### Phase 4: Advanced Search ✅ ABGESCHLOSSEN

**Scope:** Fuzzy Search mit pg_trgm Extension

- [x] Migration V10052: pg_trgm Extension + GIN Index
- [x] LeadFuzzySearchService (Fuzzy-Matching)
- [x] LeadSearchResource (REST Endpoint /api/leads/search/fuzzy)
- [x] Tests: Fuzzy-Search Unit-Tests

### Phase 5: Fuzzy Auto-Mapping ✅ ABGESCHLOSSEN

**Scope:** Intelligente Spalten-Erkennung für Import

- [x] 3-Tier Matching: Exact Dictionary → Token-based → Levenshtein (70%)
- [x] 19 Sprachen-Dictionary (DE/EN)
- [x] suggestedMapping in Backend-Response
- [x] Frontend-Integration in LeadImportWizard

### Phase 6: Historical Import ✅ ABGESCHLOSSEN

**Scope:** Historisches Erstelldatum für Messe-Leads

- [x] Migration V10053: original_created_at + effective_created_at Felder
- [x] effectiveCreatedAt Computed Field (COALESCE)
- [x] CSV/Excel Spalte "Ursprüngliches Datum"
- [x] DatePicker im Import-Wizard (globalOriginalDate)

### BANT-Qualifizierung (DEFERRED)

**Status:** Auf späteren Sprint verschoben

- [ ] BANT-Felder + Score
- [ ] Dashboard-Widget

---

## 🎯 Entscheidungen (Kurzfassung)

| Thema | Entscheidung |
|-------|--------------|
| PDF Library | Apache PDFBox (Apache 2.0) |
| Import-Modell | Quota + Auto-Approval |
| DSGVO-Löschung | Soft-Delete + PII-Anonymisierung |
| Neue Routen | /admin/dsgvo, /admin/imports |

**Details:** → `artefakte/sprint-2.1.8/DECISIONS.md`

---

## 📊 Quota-System (Lead-Import)

| Rolle | Max. Offene Leads | Imports/Tag | Leads/Import |
|-------|-------------------|-------------|--------------|
| SALES | 100 | 3 | 100 |
| MANAGER | 200 | 5 | 200 |
| ADMIN | ∞ | ∞ | 1000 |

**Auto-Approval:** Bei <10% Duplikaten
**Eskalation:** Bei ≥10% Duplikaten → Manager/Admin

---

## ✅ Definition of Done

### Minimum (MUSS): ✅ ALLE ERFÜLLT
- [x] DSGVO Art. 15, 17, 7.3 funktional
- [x] Lead-Import funktional (CSV + Excel)
- [x] Admin-Routen verfügbar
- [x] Tests ≥80% Coverage (100+ Tests)
- [x] CI GREEN (alle Workflows)

### Nice-to-have: ✅ TEILWEISE ERFÜLLT
- [x] Advanced Search (pg_trgm Fuzzy-Suche)
- [ ] BANT-Wizard (DEFERRED)

---

## 📚 Artefakte

```
docs/planung/artefakte/sprint-2.1.8/
├── DECISIONS.md           # Entscheidungen & Festlegungen
├── DSGVO_TECHNICAL_SPEC.md   # DSGVO-Implementierung
└── LEAD_IMPORT_SPEC.md    # Import-System
```

---

## 🔗 Referenzen

- **DSGVO Art. 15:** https://dsgvo-gesetz.de/art-15-dsgvo/
- **DSGVO Art. 17:** https://dsgvo-gesetz.de/art-17-dsgvo/
- **DSGVO Art. 7:** https://dsgvo-gesetz.de/art-7-dsgvo/
- **Apache PDFBox:** https://pdfbox.apache.org/

---

**Letzte Aktualisierung:** 2025-12-05

---

## 📊 Sprint-Metriken

| Metrik | Wert |
|--------|------|
| **Commits** | 16 |
| **Files Changed** | 285 |
| **Lines Added** | +21.526 |
| **Lines Removed** | -806 |
| **Migrationen** | V10050-V10054 (5 Migrations) |
| **Tests** | 100+ (Backend + Frontend) |
| **Duration** | 04.12.2025 - 05.12.2025 |

## 📝 Commit-Historie

1. `feat(gdpr): Add GDPR compliance Phase 1` - Art. 15/17/7.3 Backend
2. `feat(gdpr): Add GDPR frontend components` - GdprActionsMenu, Dialogs
3. `feat(import): Add self-service lead import` - 4-Schritt Wizard
4. `feat(import): Add quota system` - Rollen-basierte Limits
5. `feat(admin): Add DSGVO admin dashboard` - /admin/dsgvo
6. `feat(admin): Add imports admin dashboard` - /admin/imports
7. `feat(search): Add pg_trgm fuzzy search` - V10052 Migration
8. `feat(import): Add fuzzy auto-mapping` - 3-Tier Matching
9. `feat(import): Add historical import` - originalCreatedAt
10. `test: Add GDPR unit tests` - GdprService Tests
11. `test: Add import unit tests` - ImportQuotaService Tests
12. `test: Add frontend tests` - LeadImportWizard Tests
13. `fix: Fix auto-mapping field naming` - suggestedMapping → autoMapping
14. `fix: Add help system /view endpoint` - 404 Fix
15. `chore: Create test data CSV` - test-leads-import.csv
16. `docs: Update sprint documentation` - Artefakte
