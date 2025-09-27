---
module: "shared"
domain: "shared"
doc_type: "guideline"
status: "approved"
owner: "team/leads"
updated: "2025-09-27"
---

# 🤖 START HERE FOR AI - FreshPlan Sales Tool Navigation

**📍 Navigation:** Home → Planung → AI-Startseite

**Zweck:** Einstiegspunkt für neue Claude-Instanzen zur schnellen Orientierung im FreshPlan Sales Tool Repository.

---

## 🚀 **QUICK START (1-2 Minuten)**

### **1. Master Plan lesen (PFLICHT)**
```bash
# Single Source of Truth
docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md
```

### **2. Trigger-Index verstehen**
```bash
# Sprint-Übersicht und Reihenfolge
docs/planung/TRIGGER_INDEX.md
```

### **3. Aktuelle Migration prüfen**
```bash
# NIEMALS Nummern hardcoden!
./scripts/get-next-migration.sh
```

---

## 🗺️ **DOKUMENTATIONS-NAVIGATION**

### **Zentrale Planung (Sprint-Trigger)**
```
docs/planung/
├── TRIGGER_INDEX.md                     # Sprint-Übersicht
├── CRM_COMPLETE_MASTER_PLAN_V5.md      # Master Plan (Single Source of Truth)
├── TRIGGER_SPRINT_2_1.md               # Sprint 2.1: Backend Implementation
├── TRIGGER_SPRINT_2_1_1.md             # Sprint 2.1.1: Hotfix Integration
└── PLANUNGSMETHODIK.md                 # Dokumentations-Standards (Hybrid-Prinzip)
```

### **Modulare Overlays (dezentral)**
```
docs/planung/features-neu/NN_modulname/
├── _index.md                           # Modul-Einstieg
├── SPRINT_MAP.md                       # Sprint-Links (keine Kopien!)
├── backend/
│   ├── _index.md                       # Backend-Konzepte
│   ├── analyse/                        # Backend-Analysen
│   └── konzepte/                       # API-Specs, Patterns
├── frontend/
│   ├── _index.md                       # Frontend-Konzepte
│   ├── analyse/                        # Stack-Analysen, Inventuren
│   └── konzepte/                       # UI-Patterns, API-Contracts
└── shared/
    ├── _index.md                       # Cross-cutting Concerns
    └── contracts/                      # Kanonische Verträge
```

---

## 🧭 **FRONT-MATTER VERSTEHEN**

Jedes Dokument beginnt mit Metadaten:

```yaml
---
module: "02_neukundengewinnung"      # Modul-ID
domain: "frontend"                   # frontend | backend | shared
doc_type: "analyse"                  # analyse | konzept | contract | guideline
sprint: "2.1.2"                      # optional: Sprint-Bezug
status: "approved"                   # draft | approved | obsoleted
owner: "team/leads"                  # Verantwortung
updated: "2025-09-27"                # Letztes Update
---
```

**Navigation-Breadcrumb** im Kopfbereich:
```
**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Frontend → Analyse → INVENTORY
```

---

## 🎯 **BEISPIEL: MODUL 02 NAVIGATION**

### **Einstieg über Module**
1. **Modul-Root:** `docs/planung/features-neu/02_neukundengewinnung/_index.md`
2. **Sprint-Links:** `docs/planung/features-neu/02_neukundengewinnung/SPRINT_MAP.md`

### **Backend-Kontext**
- **Backend-Einstieg:** `backend/_index.md`
- **Production-Status:** ✅ COMPLETE (Sprint 2.1 + 2.1.1)
- **Zentrale ADRs:** ADR-0002 (PostgreSQL LISTEN/NOTIFY)

### **Frontend-Kontext**
- **Frontend-Einstieg:** `frontend/_index.md`
- **Research-Status:** ✅ COMPLETE (Sprint 2.1.2)
- **Key-Artefakte:** `analyse/INVENTORY.md`, `analyse/API_CONTRACT.md`

---

## ⚠️ **WICHTIGE GUARDRAILS**

### **NIEMALS diese Dinge tun:**
```bash
# ❌ Migration-Nummern hardcoden
CREATE TABLE V123_leads...

# ✅ Script verwenden
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
```

### **Docs-only vs. Code-Änderungen:**
- **Branch `docs/*`** → nur Dokumentation
- **CI `docs-only-check.yml`** → Fehler bei Code-Änderungen
- **Separate PRs** für Docs und Code

### **Link-Standards:**
- **Zentrale Trigger** immer verlinken, nie kopieren
- **Relative Links** innerhalb `docs/planung/`
- **Keine GitHub-URLs** für interne Verweise

---

## 🔧 **HÄUFIGE TASKS**

### **1. Neues Modul analysieren**
```bash
# 1. Modul-Root lesen
docs/planung/features-neu/NN_modulname/_index.md

# 2. Sprint-Links folgen
docs/planung/features-neu/NN_modulname/SPRINT_MAP.md

# 3. Domain-spezifisch vertiefen
backend/_index.md | frontend/_index.md | shared/_index.md
```

### **2. Sprint-Kontext verstehen**
```bash
# 1. Trigger-Index
docs/planung/TRIGGER_INDEX.md

# 2. Spezifischen Sprint
docs/planung/TRIGGER_SPRINT_X_Y.md

# 3. Modul-Overlay (optional)
docs/planung/features-neu/NN_modulname/SPRINT_MAP.md
```

### **3. API-Integration recherchieren**
```bash
# 1. Frontend-Analyse
frontend/analyse/API_CONTRACT.md

# 2. Backend-Patterns
backend/analyse/ | backend/konzepte/

# 3. Shared-Contracts
shared/contracts/ | shared/_index.md
```

---

## 🤖 **CLAUDE-OPTIMIERTE WORKFLOWS**

### **Kontext schnell aufbauen:**
1. **Master Plan V5** → aktueller Projektstand
2. **Trigger-Index** → Sprint-Reihenfolge
3. **Modul-Einstieg** → spezifischer Kontext
4. **Domain-Analyse** → technische Details

### **Für Code-Tasks:**
1. **Migration-Check** → `./scripts/get-next-migration.sh`
2. **Test-Coverage** → `≥80%` Pflicht
3. **CI-Status** → grün vor Review
4. **Bundle-Size** → `<200KB` Frontend

### **Für Docs-Tasks:**
1. **Front-Matter** → vollständig ausfüllen
2. **Breadcrumb** → Navigation-Pfad
3. **DoD-Checkliste** → siehe `PLANUNGSMETHODIK.md`
4. **Link-Validation** → keine toten Links

---

## 📚 **WEITERFÜHRENDE REFERENZEN**

- **Planungsmethodik:** `docs/planung/PLANUNGSMETHODIK.md`
- **AI-Context:** `docs/planung/CRM_AI_CONTEXT_SCHNELL.md`
- **Production Roadmap:** `docs/planung/PRODUCTION_ROADMAP_2025.md`
- **ADR-Index:** `docs/planung/adr/`

---

**🎯 Ziel:** Neue Claude-Instanzen sollen in <5 Minuten produktiv werden.
**📱 Bei Fragen:** Immer erst Master Plan V5 + Trigger-Index checken!