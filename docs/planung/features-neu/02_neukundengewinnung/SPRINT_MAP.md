---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "guideline"
status: "approved"
owner: "team/leads"
updated: "2025-09-27"
---

# 🗺️ Sprint-Map – Modul 02 Neukundengewinnung

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Sprint-Map

**Start here:** Dieser Index verweist auf die *zentralen* Sprint-Dokumente (keine Kopien!).

## 🎯 Relevante Sprints

### **Sprint 2.1 – Modul 02 Backend (COMPLETE)**
**Zentral:** [TRIGGER_SPRINT_2_1.md](../../TRIGGER_SPRINT_2_1.md)
**Status:** ✅ 100% COMPLETE
**Ergebnisse:**
- Lead-Management System ohne Gebietsschutz
- Territory-Scoping (Deutschland/Schweiz)
- CQRS Light Integration
- Security Test Pattern, Performance Test Pattern, Event System Pattern

**PRs:** #103, #105, #110 (alle merged)

---

### **Sprint 2.1.1 – P0 Hotfix Integration Gaps (COMPLETE)**
**Zentral:** [TRIGGER_SPRINT_2_1_1.md](../../TRIGGER_SPRINT_2_1_1.md)
**Spezifisches Log:** [SPRINT_2_1_1_DELTA_LOG.md](../../SPRINT_2_1_1_DELTA_LOG.md)
**Status:** ✅ COMPLETE
**Ergebnisse:**
- Event Distribution Pipeline (PostgreSQL LISTEN/NOTIFY)
- Dashboard Widget Integration
- Metrics Implementation (freshplan_* ohne _total suffix)
- FP-235: T+3/T+7 Follow-up Automation
- AFTER_COMMIT Pattern + Idempotenz + RBAC

**PRs:** #111 (merged 2025-09-26)

---

### **Sprint 2.1.2 – Frontend Research (COMPLETE)**
**Modul-Spezifisch:** [frontend/analyse/_index.md](./frontend/analyse/_index.md)
**Status:** ✅ Research Complete (PR #112, Draft)
**Ergebnisse:**
- **INVENTORY.md:** Stack-Analyse + Gaps + Foundation Patterns
- **API_CONTRACT.md:** Event-System + RBAC + REST-Endpoints + Polling Strategy
- **RESEARCH_ANSWERS.md:** 11 offene Fragen beantwortet
- **VALIDATED_FOUNDATION_PATTERNS.md:** Konsolidierte Patterns aus grundlagen/ & infrastruktur/

**Nächster Schritt:** Thin Vertical Slice mit Feature-Flag `VITE_FEATURE_LEADGEN`

---

### **Sprint 2.1.3 – Frontend Implementation (COMPLETE)**
**Zentral:** [TRIGGER_SPRINT_2_1_3.md](../../TRIGGER_SPRINT_2_1_3.md)
**Status:** ✅ COMPLETE
**Ergebnisse:**
- Thin Vertical Slice: `/leads` Route + `LeadList` + `LeadCreateDialog`
- Feature-Flag: `VITE_FEATURE_LEADGEN=true` aktiviert
- Vollständige Business-Logik:
  - Client-seitige Validierung (Name ≥2, E-Mail-Format)
  - Duplikat-Erkennung (409 Response bei gleicher E-Mail)
  - Source-Tracking (`source='manual'`)
  - RFC7807 Error Handling mit Feld-Fehlern
- Vollständige i18n (de/en) ohne hardcoded Strings
- MUI Theme V2 Integration + MainLayoutV2 Wrapper
- Tests: 90% Coverage (Integration Tests für alle Business-Flows)
- MSW für realistische API-Simulation

**PRs:** #122 (merged 2025-09-28)

---

### **Sprint 2.1.4 – Backend Integration (IN_PROGRESS)**
**Zentral:** [TRIGGER_SPRINT_2_1_4.md](../../TRIGGER_SPRINT_2_1_4.md)
**Status:** 🔧 IN_PROGRESS
**Scope:** Lead Deduplication & Data Quality – Phase 1
- Normalisierung E-Mail/Telefon + Unique-Indizes
- Partielle UNIQUE auf `email_normalized` und `phone_e164` (WHERE NOT NULL)
- Idempotency-Key Support für `POST /api/leads`
- RFC7807 409 Response bei Duplikaten

**Deliverables:**
- Migration V247 für normalisierte Felder
- Backend-Service mit Normalisierungs-Logik
- Idempotency-Store Implementation
- Tests für Konflikt-Handling und Idempotenz

**Artefakte:** [`artefakte/SPRINT_2_1_4/`](./artefakte/SPRINT_2_1_4/)

---

## 🔗 **Cross-Module Dependencies**

### **Infrastruktur-Sprints (relevant für Modul 02):**
- **TRIGGER_SPRINT_1_2.md:** Security Foundation (RBAC + Territory RLS)
- **TRIGGER_SPRINT_1_3.md:** CQRS Foundation (Event-System Basis)
- **TRIGGER_SPRINT_1_5.md:** Performance Foundation (Bundle <200KB)

### **ADRs (Architecture Decisions):**
- **ADR-0002:** PostgreSQL LISTEN/NOTIFY (Event-Architektur)
- **ADR-0004:** Territory RLS vs Lead-Ownership (Security-Modell)

## 📋 **Implementation Timeline**

```yaml
Phase 1 (Backend): Sprint 2.1 + 2.1.1
  Status: ✅ COMPLETE
  Result: Production-ready Lead-Management Backend

Phase 2 (Frontend Research): Sprint 2.1.2
  Status: ✅ COMPLETE
  Result: Vollständige Frontend-Analyse + API-Contract

Phase 3 (Frontend Implementation): Sprint 2.1.3
  Status: ✅ COMPLETE
  Result: Lead Management MVP mit Business-Logik
  PR: #122 (merged)
```

## 🎯 **Für neue Claude-Instanzen**

**Start immer mit den zentralen Trigger-Dokumenten** – sie enthalten die vollständigen Sprint-Kontexte, Cross-Module-Dependencies und Entscheidungshistorie. Diese Sprint-Map ist nur ein **Navigationshelfer**, nicht die Quelle der Wahrheit.