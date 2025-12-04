---
sprint_id: "2.1.8"
title: "DSGVO Compliance & Lead-Import"
doc_type: "trigger"
status: "in_progress"
owner: "Claude Code"
date_start: "2025-12-04"
branch: "feature/sprint-2-1-8-dsgvo-lead-import"
modules: ["02_neukundengewinnung"]
updated: "2025-12-04"
---

# Sprint 2.1.8 – DSGVO Compliance & Lead-Import

**Branch:** `feature/sprint-2-1-8-dsgvo-lead-import`
**Status:** 🚧 IN PROGRESS

---

## 🎯 Sprint-Ziel (Kurzfassung)

**Gesetzliche Pflicht-Features + B2B-Standard Lead-Import**

| Deliverable | Priorität | Status |
|-------------|-----------|--------|
| DSGVO Art. 15 (Auskunft) | 🔴 PFLICHT | ⬜ TODO |
| DSGVO Art. 17 (Löschung) | 🔴 PFLICHT | ⬜ TODO |
| DSGVO Art. 7.3 (Widerruf) | 🟡 SOLLTE | ⬜ TODO |
| Lead-Import (CSV/Excel) | 🔴 KRITISCH | ⬜ TODO |
| Admin-UI (/admin/dsgvo, /admin/imports) | 🟡 WICHTIG | ⬜ TODO |
| Advanced Search | 🟢 KANN | ⬜ DEFERRED |
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

### Phase 1: DSGVO-Kern (Prio 1)

**Scope:** Art. 15, 17, 7.3 Implementierung

- [ ] Migration: DSGVO-Felder + Tabellen
- [ ] GdprService (Backend)
- [ ] PdfGeneratorService (Apache PDFBox)
- [ ] GdprResource (REST Endpoints)
- [ ] Frontend: GdprActionsMenu, GdprDeleteDialog, ContactBlockedBadge
- [ ] Tests: Unit + Integration

**Details:** → `artefakte/sprint-2.1.8/DSGVO_TECHNICAL_SPEC.md`

### Phase 2: Lead-Import (Prio 2)

**Scope:** Self-Service Import mit Quota-System

- [ ] Migration: import_logs Tabelle
- [ ] ImportQuotaService
- [ ] LeadImportService (CSV/Excel Parser)
- [ ] LeadImportResource
- [ ] Frontend: LeadImportWizard (4 Steps)
- [ ] Tests: Unit + Integration

**Details:** → `artefakte/sprint-2.1.8/LEAD_IMPORT_SPEC.md`

### Phase 3: Admin-UI + Routing

**Scope:** Neue Admin-Routen

- [ ] `/admin/dsgvo` - DSGVO-Verwaltung
- [ ] `/admin/imports` - Import-Verwaltung
- [ ] Navigation-Integration

### Phase 4: Advanced Search + BANT (Optional)

**Status:** DEFERRED - nur wenn Zeit übrig

- [ ] PostgreSQL Full-Text-Search
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

### Minimum (MUSS):
- [ ] DSGVO Art. 15, 17, 7.3 funktional
- [ ] Lead-Import funktional (CSV + Excel)
- [ ] Admin-Routen verfügbar
- [ ] Tests ≥80% Coverage
- [ ] CI GREEN

### Nice-to-have:
- [ ] Advanced Search
- [ ] BANT-Wizard

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

**Letzte Aktualisierung:** 2025-12-04
