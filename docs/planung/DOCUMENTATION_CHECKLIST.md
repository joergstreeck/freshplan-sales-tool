---
module: "shared"
domain: "shared"
doc_type: "guideline"
status: "approved"
owner: "team/leads"
updated: "2025-10-02"
---

# 📋 Documentation Checklist (Kompakt)

**📍 Navigation:** Home → Planung → DOCUMENTATION_CHECKLIST.md

**Zweck:** Schnelle 5-Minuten-Checkliste für Dokumentations-Updates.

**Prinzip:** Erst **Pflicht-Dokumente** (immer), dann **Kontext-Blöcke** (nur wenn relevant).

---

## 🔶 Kurz-Prozess (5 Minuten)

1. **Changelist:** PR-Titel, Migrationsnummern, APIs, Module notieren
2. **Pflicht-Block "Immer pflegen"** abhaken (7 Einträge)
3. **Kontext-Blöcke** (DB/API/UI/Security/Tests/Ops) *nur wenn relevant*
4. **PR-Kommentar:** "Docs aktualisiert ✅" mit Liste posten

---

## ✅ Immer pflegen (Pflicht)

- [ ] **Master Plan V5** - Session Log + Next Steps
  `/docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md`

- [ ] **Production Roadmap** - Sprint-Status
  `/docs/planung/PRODUCTION_ROADMAP_2025.md`

- [ ] **Trigger Index** - Aktuellen Sprint markieren
  `/docs/planung/TRIGGER_INDEX.md`

- [ ] **Sprint Trigger** - Alle dort referenzierten Docs
  `/docs/planung/TRIGGER_SPRINT_X_Y_Z.md`

- [ ] **Sprint Map** - Modul-Status + PR-Links
  `/docs/planung/features-neu/{modul}/SPRINT_MAP.md`

- [ ] **Sprint Summary** - Deliverables + DoD
  `/docs/planung/features-neu/{modul}/artefakte/SPRINT_X_Y_Z/SUMMARY.md`

- [ ] **Modul _index.md** - Status-Dashboard
  `/docs/planung/features-neu/{modul}/_index.md`

---

## 🧩 Falls DB-Änderung (Migrationen)

- [ ] **MIGRATIONS.md** - Neue Migration eintragen (Owner, PR, Rollback, Risk, Downtime)
  `/docs/planung/MIGRATIONS.md`

- [ ] **Sprint Trigger** - Migrations-Warnhinweis aktualisieren
  `/docs/planung/TRIGGER_SPRINT_X_Y_Z.md` (⚠️-Box)

- [ ] **Sprint Summary** - Unter "Datenbank-Migrationen" auflisten
  `/docs/planung/features-neu/{modul}/artefakte/SPRINT_X_Y_Z/SUMMARY.md`

- [ ] **Modul backend/_index.md** - "Current Sprint" Sektion aktualisieren
  `/docs/planung/features-neu/{modul}/backend/_index.md`

- [ ] **ADR** erstellen (falls Architekturentscheidung)
  `/docs/planung/features-neu/{modul}/shared/adr/ADR-XXX-*.md`

- [ ] **Contract Mapping** aktualisieren (falls vertragliche Anforderungen)
  `/docs/planung/features-neu/{modul}/shared/CONTRACT_MAPPING.md`

---

## 🔌 Falls API-Änderung

- [ ] **Contract Mapping** - Request/Response, Fehlercodes
  `/docs/planung/features-neu/{modul}/shared/CONTRACT_MAPPING.md`

- [ ] **API Contract** - Endpoints, Schemas dokumentieren
  `/docs/planung/features-neu/{modul}/frontend/analyse/API_CONTRACT.md`

- [ ] **Modul backend/_index.md** - "Core Services" Sektion
  `/docs/planung/features-neu/{modul}/backend/_index.md`

---

## 🖼️ Falls UI/UX (Frontend)

- [ ] **Modul frontend/_index.md** - Komponenten + Routing
  `/docs/planung/features-neu/{modul}/frontend/_index.md`

- [ ] **Theme V2** - FreshFoodz CI dokumentieren (falls neue UI-Pattern)
  Siehe: FRESHFOODZ CI (#94C456, #004F7B, Antonio Bold, Poppins)
  Ort: Sprint Summary unter "Frontend Changes"

- [ ] **API Contract** - Frontend-Backend-Interaktion
  `/docs/planung/features-neu/{modul}/frontend/analyse/API_CONTRACT.md`

---

## 🛡️ Falls Security/Compliance

- [ ] **ADR** erstellen/aktualisieren
  `/docs/planung/features-neu/{modul}/shared/adr/ADR-XXX-*.md`

- [ ] **Contract Mapping** - DSGVO-Bezüge dokumentieren
  `/docs/planung/features-neu/{modul}/shared/CONTRACT_MAPPING.md`

- [ ] **Data Retention** dokumentieren (falls relevant)
  `/docs/operations/runbooks/{modul}/data-retention.md`

---

## 🧪 Falls Tests/CI

- [ ] **TESTING_GUIDE.md** - Test-Strategie aktualisieren
  `/backend/TESTING_GUIDE.md`

- [ ] **Sprint Summary** - Test-Metriken eintragen
  `/docs/planung/features-neu/{modul}/artefakte/SPRINT_X_Y_Z/SUMMARY.md`

- [ ] **Modul backend/_index.md** - "Testing Strategy" Sektion
  `/docs/planung/features-neu/{modul}/backend/_index.md`

---

## 🛠️ Falls Operations/Runbooks

- [ ] **Runbook** erstellen/aktualisieren
  `/docs/operations/runbooks/{modul}/{feature}.md`

- [ ] **Monitoring** - Neue Metriken dokumentieren
  Sprint Summary unter "Metriken" Sektion

---

## 🧩 PR-Kommentar Template (Copy/Paste)

```markdown
## Docs aktualisiert ✅

**Pflicht-Dokumente:**
- Master Plan V5 (Session Log, Next Steps)
- Production Roadmap (Sprint Status)
- Trigger Index + TRIGGER_SPRINT_X.Y.Z
- Sprint Map + Summary
- Modul _index.md

**Kontext (falls relevant):**
- MIGRATIONS.md (V255-V257, Owner/PR/Rollback/Risk)
- Contract Mapping + ADR-XXX
- TESTING_GUIDE + Test-Metriken
- Runbook (ops)

**PR-Nummern:** #124
**Risk:** Low
**Rollback:** Yes (siehe MIGRATIONS.md)
```

---

## 📊 Referenz-Tabelle: Kern-Dokumente

| Dokument | Pfad | Wann aktualisieren |
|----------|------|-------------------|
| **Master Plan V5** | `/docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md` | Immer (Session Log) |
| **Production Roadmap** | `/docs/planung/PRODUCTION_ROADMAP_2025.md` | Immer (Sprint Status) |
| **Trigger Index** | `/docs/planung/TRIGGER_INDEX.md` | Neuer Sprint, Sprint Ende |
| **Sprint Trigger** | `/docs/planung/TRIGGER_SPRINT_X_Y_Z.md` | Sprint Start/Ende |
| **MIGRATIONS.md** | `/docs/planung/MIGRATIONS.md` | Jede neue Migration |
| **Sprint Map** | `/docs/planung/features-neu/{modul}/SPRINT_MAP.md` | Jeder Sprint |
| **Sprint Summary** | `/docs/planung/features-neu/{modul}/artefakte/SPRINT_X_Y_Z/SUMMARY.md` | Sprint-Abschluss |
| **Modul _index.md** | `/docs/planung/features-neu/{modul}/_index.md` | Jede Modul-Änderung |
| **backend/_index.md** | `/docs/planung/features-neu/{modul}/backend/_index.md` | Backend-Änderungen |
| **frontend/_index.md** | `/docs/planung/features-neu/{modul}/frontend/_index.md` | Frontend-Änderungen |
| **Contract Mapping** | `/docs/planung/features-neu/{modul}/shared/CONTRACT_MAPPING.md` | API/Vertrags-Änderungen |
| **ADR** | `/docs/planung/features-neu/{modul}/shared/adr/ADR-XXX-*.md` | Architektur-Entscheidungen |
| **TESTING_GUIDE** | `/backend/TESTING_GUIDE.md` | Test-Strategie-Änderungen |
| **Runbooks** | `/docs/operations/runbooks/{modul}/{feature}.md` | Neue Features mit Ops-Impact |

---

## 🤖 Für neue Claude-Instanz

**Beim Einstieg in Sprint:**
1. [ ] TRIGGER_INDEX.md → Aktuellen Sprint finden
2. [ ] TRIGGER_SPRINT_X_Y_Z.md → Arbeitsanweisung lesen
3. [ ] MIGRATIONS.md → Migrations-Kontext verstehen
4. [ ] Modul _index.md → Status-Dashboard prüfen
5. [ ] Sprint Summary → Was wurde erreicht?

**Bei jeder Änderung:**
1. [ ] Diese Checkliste konsultieren
2. [ ] Relevante Blöcke abarbeiten
3. [ ] Alle aktualisierten Docs im PR erwähnen

---

**Letzte Aktualisierung:** 2025-10-02

**Nächste Review:** Bei Strukturänderung oder Sprint 2.2.0
