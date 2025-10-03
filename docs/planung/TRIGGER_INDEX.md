# 📋 TRIGGER-TEXTS INDEX - Alle Sprint-Aufträge

**📅 Erstellt:** 2025-01-22
**🎯 Zweck:** Übersicht aller 13 Sprint-spezifischen Trigger-Texts
**✅ Status:** Production-Ready mit Migration-Check + CRM AI Context
**🔄 Verbesserungen:** Migration-Script + Business-Kontext + Security-Gates

Für Modul‑konkrete Navigation verweisen die Trigger auf die **SPRINT_MAP.md** im jeweiligen Modul.

---

## 🗺️ ALLE TRIGGER-TEXTS ÜBERSICHT

### **PHASE 1: FOUNDATION (6 Sprints) - ✅ COMPLETE**
```yaml
✅ TRIGGER_SPRINT_1_1.md - CQRS Light Foundation [PR #94 MERGED]
   - PostgreSQL LISTEN/NOTIFY + Event-Schema
   - Mock-Governance Setup (ESLint/CI/Dev-Seeds)
   - Migration V225-227 deployed
   - Status: ✅ COMPLETE

✅ TRIGGER_SPRINT_1_2.md - Security + Foundation [PR #95-96 MERGED]
   - ABAC/RLS + Settings Registry
   - Security Context operational
   - Status: ✅ COMPLETE

✅ TRIGGER_SPRINT_1_3.md - Security Gates + CI [PR #97-101 MERGED]
   - CI Pipeline Split (PR <10min, Nightly ~30min)
   - P95 Performance Benchmarks
   - ETag Hit-Rate ≥70% achieved
   - Status: ✅ COMPLETE

✅ TRIGGER_SPRINT_1_4.md - Foundation Quick-Wins [PR #102 MERGED]
   - Quarkus-Cache für Settings-Service
   - Prod-Config Härtung
   - Cache-Invalidierung bei Writes
   - Status: ✅ COMPLETE

✅ TRIGGER_SPRINT_1_5.md - Security Retrofit [PR #106 MERGED] 🔒
   - RLS Connection Affinity (KRITISCHER SECURITY FIX)
   - CDI Interceptor Pattern mit @RlsContext
   - Migrations V242-243 (fail-closed policies)
   - Gemini Review: "Exzellent und äußerst wichtig"
   - Status: ✅ COMPLETE (25.09.2025, 18:42 Uhr)

✅ TRIGGER_SPRINT_1_6.md - RLS Adoption in Modulen [PR #107 MERGED]
   - Modul 02 Services mit @RlsContext (P0 - Sprint 2.1 entblockt)
   - CI-Guard für RLS-Compliance implementiert
   - RLS-Badge in allen 8 Modulen dokumentiert
   - Status: ✅ COMPLETE (25.09.2025, 20:24 Uhr)

📊 **Phase 1 Performance Report:** [phase-1-foundation-benchmark-2025-09-24.md](../performance/phase-1-foundation-benchmark-2025-09-24.md)
🔒 **Security Update:** [SECURITY_UPDATE_SPRINT_1_5.md](./SECURITY_UPDATE_SPRINT_1_5.md)
```

### **PHASE 2: CORE BUSINESS (5 Sprints) - 🔧 IN PROGRESS**
```yaml
✅ TRIGGER_SPRINT_2_1.md - Neukundengewinnung [PR #103, #105, #110 MERGED]
   - Lead-Management ohne Gebietsschutz
   - 8-10h, 3/4 PRs complete (FP-233 ✅, FP-234 ✅, FP-236 ✅)
   - FP-236: 23 Tests, P95 < 7ms, Gemini Review adressiert
   - **NEUE ARTEFAKTE:**
     - [Security Test Pattern](features-neu/02_neukundengewinnung/artefakte/SECURITY_TEST_PATTERN.md)
     - [Performance Test Pattern](features-neu/02_neukundengewinnung/artefakte/PERFORMANCE_TEST_PATTERN.md)
     - [Event System Pattern](features-neu/02_neukundengewinnung/artefakte/EVENT_SYSTEM_PATTERN.md)
   - **FRONTEND RESEARCH (Sprint 2.1.2):**
     - [Analyse-Übersicht](features-neu/02_neukundengewinnung/analyse/_index.md)
     - [INVENTORY.md](features-neu/02_neukundengewinnung/analyse/INVENTORY.md)
     - [API_CONTRACT.md](features-neu/02_neukundengewinnung/analyse/API_CONTRACT.md)
   - Status: ✅ 100% COMPLETE (FP-235 in PR #111 abgeschlossen)

✅ TRIGGER_SPRINT_2_1_3.md - Frontend Implementation (Lead-Management)
   - Thin Vertical Slice: /leads Route + Create Dialog
   - Feature-Flag: VITE_FEATURE_LEADGEN (default: off)
   - MUI Theme V2 + RFC7807 Error Handling
   - Coverage ≥80% für neue Frontend-Komponenten
   - Status: ✅ COMPLETE (PR #122 merged)

✅ TRIGGER_SPRINT_2_1_1.md - P0 HOTFIX Integration Gaps [PR #111 MERGED]
   - Event Distribution, Dashboard Widget, Metrics implementiert
   - Kritische Integration für Follow-up Automation
   - FP-235: T+3/T+7 Follow-up Automation ✅ COMPLETE
   - Status: ✅ COMPLETE (PR #111 merged 2025-09-26)

✅ TRIGGER_SPRINT_2_1_4.md - Lead Deduplication & Data Quality
   - Normalisierung (Email/Phone) + Unique Indizes
   - IdempotencyService für API-Resilienz (24h TTL)
   - V247 + V250 Migrationen deployed
   - 39 Tests (31 Normalization + 8 Idempotency) ✅
   - Status: ✅ COMPLETE (Backend fertig)

✅ TRIGGER_SPRINT_2_1_5.md - Lead Protection & Progressive Profiling [DOKU COMPLETE 04.10.2025]
   - **Backend Phase 1:** V255-V257 Migrations ✅ (Progress Tracking + Stage + Functions/Trigger, 24 Unit Tests)
   - **Backend Phase 2:** V258 Migration ✅ (Activity-Type Constraint 6→13 Types, 7 Unit Tests)
   - **Dokumentation COMPLETE:** FRONTEND_DELTA.md (877 Zeilen zentrale Frontend-Spec) ✅
     - 13 Activity-Types + ACTIVITY_PROGRESS_MAP, LeadSource Typ (6 Werte)
     - Erstkontakt-Block UI → activities[] Transformation
     - Problem.extensions (severity, duplicates[]) für Dedupe 409 (Hard/Soft)
     - Dedupe Query-Params (overrideReason, reason), Pre-Claim UX (Badge ⏳ + 4 Filter)
   - **Frontend Code:** ⏸️ types.ts, api.ts, LeadWizard.tsx, Dedupe-Dialoge ausstehend
   - **PR #129:** Offen (54 files, 3 Komponenten vorhanden), Doku-First Ansatz
   - Status: 🔄 DOKU COMPLETE, Code-Anpassung ausstehend
   - **NEUE ARTEFAKTE:**
     - [Frontend Delta (zentral)](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/FRONTEND_DELTA.md) ⭐ NEU
     - [Pre-Claim Logic](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/PRE_CLAIM_LOGIC.md)
     - [Dedupe Policy](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/DEDUPE_POLICY.md)
     - [Activity Types Mapping](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/ACTIVITY_TYPES_PROGRESS_MAPPING.md)
     - [DSGVO Consent Spec](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/DSGVO_CONSENT_SPECIFICATION.md)

📋 TRIGGER_SPRINT_2_1_6.md - Lead Transfer & Team Management
   - Lead-Transfer zwischen Partnern
   - Team-basierte Sichtbarkeit (RLS Phase 1)
   - Fuzzy-Matching & Review-Flow (verschoben aus 2.1.5)
   - Merge/Unmerge mit Identitätsgraph
   - ADR-003 Row-Level-Security Design
   - Status: 📋 PLANNED

✅ TRIGGER_SPRINT_2_2.md - Kundenmanagement
   - Field-based Customer Architecture
   - 10-12h, 5 PRs (FP-237 bis FP-241)
   - **NUTZT PATTERNS:** Security Test Pattern, Performance Test Pattern aus PR #110

✅ TRIGGER_SPRINT_2_3.md - Kommunikation (NACH SECURITY-GATE)
   - Thread/Message/Outbox + Email-Engine
   - 6-8h, 4 PRs (FP-242 bis FP-245)
   - **NUTZT PATTERNS:** Event System Pattern, Security Test Pattern aus PR #110

✅ TRIGGER_SPRINT_2_4.md - Cockpit
   - ROI-Dashboard + Real-time Widgets
   - 6-8h, 4 PRs (FP-246 bis FP-249)
   - **NUTZT PATTERNS:** Event System Pattern für Dashboard-Updates aus PR #110

✅ TRIGGER_SPRINT_2_5.md - Einstellungen + Cross-Module
   - Settings UI + Cross-Module Integration
   - 8-10h, 3 PRs (FP-250 bis FP-252) + Puffer-Tag
   - **NUTZT PATTERNS:** Alle 3 Patterns für Cross-Module Integration
```

### **PHASE 3: ENHANCEMENT (3 Sprints)**
```yaml
✅ TRIGGER_SPRINT_3_1.md - Auswertungen
   - Analytics Platform auf CQRS Foundation
   - 6-8h, 4 PRs (FP-253 bis FP-256)
   - **NUTZT PATTERNS:** Performance Test Pattern für Analytics-Queries

✅ TRIGGER_SPRINT_3_2.md - Hilfe + Administration
   - CAR-Strategy + Enterprise User-Management
   - 6-8h, 4 PRs (FP-257 bis FP-260)
   - **NUTZT PATTERNS:** Security Test Pattern für User-Management

✅ TRIGGER_SPRINT_3_3.md - Final Integration
   - **Nginx+OIDC Gateway** (Pflicht) + **Kong/Envoy** (optional)
   - 4-6h, 2 PRs (FP-261, Final Integration)
```

---

## 🔧 VERBESSERUNGEN IN ALLEN TRIGGER-TEXTS

### **1. ✅ MIGRATION-CHECK INTEGRIERT:**
```bash
# Dynamische Migration-Nummer (NIEMALS hardcoded):
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)

# In jedem Trigger-Text als Pflicht-Schritt
# Fallback bei Script-Fehler dokumentiert
```

### **2. ✅ CRM_AI_CONTEXT_SCHNELL.md PFLICHT-LESEFOLGE:**
```yaml
Neue Reihenfolge (7 Dokumente):
1. 🗺️ ROADMAP-ORIENTIERUNG: PRODUCTION_ROADMAP_2025.md
2. 📋 ARBEITSREGELN: CLAUDE.md
3. 🍃 BUSINESS-KONTEXT: CRM_AI_CONTEXT_SCHNELL.md ← NEU!
4. 🎯 SPRINT-DETAIL: PRODUCTION_ROADMAP_2025.md (Sprint-Section)
5. 🏗️ TECHNICAL-CONCEPT: features-neu/{MODULE}/technical-concept.md
6. 📦 ARTEFAKTE: features-neu/{MODULE}/artefakte/ ← PR #110 PATTERNS!
   - SECURITY_TEST_PATTERN.md (23 Tests, @TestSecurity) → Copy-Paste für alle Module
   - PERFORMANCE_TEST_PATTERN.md (P95 < 200ms Validation) → Helper-Methoden wiederverwendbar
   - EVENT_SYSTEM_PATTERN.md (LISTEN/NOTIFY mit AFTER_COMMIT) → Cross-Module Events
7. 🔧 QUALITY-GATES: PRODUCTION_ROADMAP_2025.md (Quality Gates)
```

### **3. ✅ SECURITY-GATE ENFORCEMENT:**
```yaml
Sprint 2.3 Kommunikation:
- 🚨 SECURITY-GATE CHECKPOINT Validierung
- Verbindliche Freigabe-Kriterien prüfen
- STOPP bei nicht-grünem Security-Gate

Weitere Security-Gates:
- Nach Phase 1 vor Phase 2
- Vor kritischen Cross-Module-Integrationen
```

### **4. ✅ SPRINT-SPEZIFISCHE DETAILS:**
```yaml
Jeder Trigger-Text enthält:
- Konkrete PR-Branch-Namen mit Ticket-IDs
- Geschätzte Arbeitszeit
- Success-Criteria spezifisch für Sprint
- Artefakte-Pfade modul-spezifisch
- Quality-Gates angepasst an Modul-Requirements
```

---

## 📝 TRIGGER-TEXT VERWENDUNG

### **FÜR JÖRG (AUFTRAG-GEBER):**
```bash
# Sprint starten:
1. Aktuellen Sprint aus Index wählen
2. Entsprechende TRIGGER_SPRINT_X_Y.md öffnen
3. Kompletten Text an Claude geben
4. Claude arbeitet systematisch alle Schritte ab

# Beispiel Sprint 1.1:
"Hier ist dein Implementierungs-Auftrag:"
[Kompletter Inhalt von TRIGGER_SPRINT_1_1.md einfügen]
```

### **FÜR CLAUDE (AUFTRAG-NEHMER):**
```bash
# Systematisches Vorgehen:
1. Alle 7 Pflicht-Dokumente in Reihenfolge lesen
2. Migration-Check ausführen (dynamisch)
3. Business-Kontext verstehen (CRM_AI_CONTEXT_SCHNELL.md)
4. Sprint-spezifische Implementation durchführen
5. Quality-Gates erfüllen
6. PR(s) erstellen
7. Roadmap-Update vornehmen
```

---

## ⚠️ KRITISCHE HINWEISE

### **MIGRATION-NUMMERN:**
- **NIEMALS hardcoded V225 verwenden**
- **IMMER ./scripts/get-next-migration.sh ausführen**
- **Bei Script-Fehler: Fallback mit ls-Command**

### **SECURITY-GATES:**
- **Sprint 2.3 nur nach Security-Gate-Validation**
- **Phase 2 nur nach kompletter Foundation**
- **Cross-Module nur nach Dependencies erfüllt**

### **BUSINESS-KONTEXT:**
- **CRM_AI_CONTEXT_SCHNELL.md ist PFLICHT für alle Sprints**
- **FreshFoodz B2B-Food-Geschäftsmodell verstehen**
- **Multi-Channel-Vertrieb + Territory-Management**

### **ARTEFAKTE-NUTZUNG:**
- **Immer vorhandene Artefakte bevorzugen**
- **300+ Production-Ready Assets optimal nutzen**
- **Minimale Anpassungen dokumentieren**

---

## 🚀 NÄCHSTE SCHRITTE

### **SOFORT VERFÜGBAR:**
- ✅ **TRIGGER_SPRINT_1_1.md** - CQRS Light Foundation (97% Compliance)
- ✅ **TRIGGER_SPRINT_1_2.md** - Security + Settings
- ✅ **TRIGGER_SPRINT_1_3.md** - Security Gates + CI
- ✅ **TRIGGER_SPRINT_2_1.md** - Neukundengewinnung
- ✅ **TRIGGER_SPRINT_2_3.md** - Kommunikation

### **✅ ALLE TRIGGER-TEXTS COMPLETE:**
- ✅ Alle 13 Trigger-Texts erfolgreich erstellt
- ✅ Production-Ready für komplette 15-Wochen-Roadmap
- ✅ Systematische Implementation möglich ab Sprint 1.1

### **RECOMMENDED START:**
```bash
# Begin with Foundation (97% Compliance):
docs/planung/TRIGGER_SPRINT_1_1.md

# Continue sequential:
docs/planung/TRIGGER_SPRINT_1_2.md
docs/planung/TRIGGER_SPRINT_1_3.md
# ... etc
```

---

## ✅ QUALITÄTS-BESTÄTIGUNG

**🎯 EXTERN VALIDIERT:**
- ✅ 9/10 Bewertung durch neuen Claude
- ✅ Alle kritischen Verbesserungen integriert
- ✅ Migration-Check + Business-Kontext + Security-Gates

**🔧 PRODUCTION-READY:**
- ✅ Systematische Dokumenten-Lesefolge
- ✅ Sprint-spezifische Implementation-Schritte
- ✅ Quality-Gates für jeden Sprint
- ✅ Roadmap-Update-Mechanismus

**🚀 COMPLETE TRIGGER-TEXT-SYSTEM READY:**
Alle 13 Trigger-Texts sind selbstständig ausführbar und führen Claude systematisch durch die komplette 15-Wochen Enterprise-CRM-Implementation!

**📋 Dokument-Zweck:** Production-Ready Index für alle Sprint-Trigger-Texts
**🔄 Letzte Aktualisierung:** 2025-01-22
**✅ Status:** Ready for Sprint 1.1 Start