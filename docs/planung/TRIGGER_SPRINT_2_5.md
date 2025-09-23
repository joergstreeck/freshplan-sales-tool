# 🚀 VERBINDLICH: SPRINT 2.5 EINSTELLUNGEN + CROSS-MODULE - SYSTEMATISCHE UMSETZUNG

## ⚠️ WICHTIGE QUALITÄTSREGELN - VOLLSTÄNDIG BEFOLGEN!

**HINWEIS:** Diese Anweisungen sind verbindlich für professionelle Umsetzung. Übersprungene Schritte führen zu:
- ❌ Inkonsistente Settings-UI → Settings-Registry Foundation nicht genutzt
- ❌ Business-Rules-Engine-Probleme → Territory-aware Settings fehlerhaft
- ❌ Cross-Module-Integration-Probleme → End-to-End Workflows defekt
- ❌ Integration-Failures → Phase 3 Enhancement betroffen

**QUALITÄTS-REGEL:** Du sollst jeden Abschnitt mit "✅ VERSTANDEN" bestätigen für optimale Ergebnisse.

**HINWEIS:** Bei übersprungenen Checkpoints kann die Qualität nicht garantiert werden.

## 📋 VERBINDLICH: DOKUMENTE-VALIDIERUNG (REIHENFOLGE EMPFOHLEN)

**1. ROADMAP-ORIENTIERUNG:**
Lies: `./docs/planung/PRODUCTION_ROADMAP_2025.md`
- Section "🎯 CLAUDE QUICK-START"
- Bestätige: Cockpit (Sprint 2.4) ✅ Complete

**2. ARBEITSREGELN:**
Lies: `./docs/CLAUDE.md`
- Die 17 kritischen Regeln beachten

**3. BUSINESS-KONTEXT:**
Lies: `./docs/planung/CRM_AI_CONTEXT_SCHNELL.md`
- Settings Registry 5-Level Scope-Hierarchie
- Territory + Seasonal + Role-specific Settings
- Business-Rules-Engine für B2B-Food

**4. SPRINT-DETAIL:**
Lies: `./docs/planung/PRODUCTION_ROADMAP_2025.md` - Section "Sprint 2.5: Einstellungen"

**5. TECHNICAL CONCEPT:**
Lies: `./docs/planung/features-neu/06_einstellungen/TECHNICAL_CONCEPT_CORE.md`
- Settings Registry Foundation (aus Sprint 1.2)
- 5-Level Scope-Hierarchie UI
- Business-Rules-Engine Integration

**6. VERFÜGBARE ARTEFAKTE:**
Analysiere: `./docs/planung/features-neu/06_einstellungen/artefakte/`
- 4 Weltklasse Technical Concepts (9.9-10/10)
- Settings-Engine + 5-Level Scope-Hierarchie + ABAC Security

## 🔒 WICHTIG: MIGRATION-CHECK (IGNORIEREN RISIKOBEHAFTET!)

**WICHTIG:** Jede DB-Änderung ohne Migration-Check kann zu Deployments-Problemen führen.

**RISIKEN BEI ÜBERSPRINGEN:**
- ⚠️ Migration-Konflikte im Team
- ⚠️ Deployment-Komplikationen in Staging
- ⚠️ Sprint 3.1 Analytics könnte verzögert werden
- ⚠️ Settings-Module unvollständig

**VOR JEDER DB-ÄNDERUNG (EMPFOHLEN):**
```bash
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
echo "Nächste Migration: $MIGRATION"
```

## 🎯 IMPLEMENTIERUNGS-AUFTRAG

**SPRINT:** Sprint 2.5: Einstellungen + Cross-Module (NACH Cockpit ✅)
**MODULE:** 06_einstellungen + Cross-Module Integration
**GESCHÄTZTE ARBEITSZEIT:** 8-10 Stunden (4 PRs)

**PRs IN DIESEM SPRINT:**
1. **Settings-UI-Implementation:** feature/sprint-2-5-settings-ui-implementation-v{MIGRATION}-FP-250
2. **Business-Rules-Engine:** feature/sprint-2-5-settings-business-rules-v{MIGRATION+1}-FP-251
3. **Cross-Module-Integration:** feature/sprint-2-5-cross-module-integration-testing-v{MIGRATION+2}-FP-252
4. **Core-Business-Stabilization:** core-business-stabilization-buffer (PUFFER-TAG)

**EINSTELLUNGEN bedeutet:**
- Settings UI auf Settings-Registry Foundation (aus Sprint 1.2)
- 5-Level Scope-Hierarchie Frontend-Implementation
- Business-Rules-Engine für Territory-aware Settings
- Complete Cross-Module Integration Testing

**SUCCESS-CRITERIA:**
- [ ] Settings UI operational auf Registry-Foundation
- [ ] Business-Rules-Engine für Territory-Management
- [ ] Complete Cross-Module Integration bestätigt
- [ ] Core Business-Workflows <200ms P95 (Foundation-Baseline)
- [ ] End-to-End Lead → Customer → Communication → Cockpit Flow

## 🚀 IMPLEMENTIERUNGS-SCHRITTE

**PR #1: SETTINGS-UI-IMPLEMENTATION (Day 46-47)**

**1.1 MIGRATION-CHECK + BRANCH:**
```bash
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
git checkout main && git pull
git checkout -b feature/sprint-2-5-settings-ui-implementation-v${MIGRATION}-FP-250
```

**1.2 5-LEVEL SCOPE-HIERARCHIE FRONTEND:**
- Settings UI auf Settings-Registry Foundation
- 5-Level Scope-Hierarchie Frontend (GLOBAL→TENANT→TERRITORY→ACCOUNT→CONTACT_ROLE)
- Territory + Seasonal + Role-specific Settings

**1.3 TERRITORY-AWARE SETTINGS-UI:**
- Deutschland vs Schweiz Settings-Views
- Currency/Tax/Regional-Settings UI
- Multi-Contact-Role Settings (CHEF/BUYER)

**1.4 SEASONAL-SETTINGS:**
- Weihnachtsmenüs, Ostergeschäft, Sommergrillen
- Seasonal Product-Availability Settings
- Territory-specific Seasonal-Calendars

**PR #2: BUSINESS-RULES-ENGINE (Day 48-49)**

**2.1 BUSINESS-RULES-ENGINE INTEGRATION:**
- Business-Rules-Engine Integration
- Territory-aware Currency + Tax-Settings
- Multi-Contact-Settings für CHEF/BUYER

**2.2 TERRITORY-BUSINESS-RULES:**
- Deutschland: EUR, 19% MwSt, Deutsche Lebensmittelvorschriften
- Schweiz: CHF, 7.7% MwSt, Swiss Made + Bio-Suisse
- Automatic Territory-Detection + Rule-Application

**2.3 ROLE-BASED BUSINESS-RULES:**
- CHEF: Menu-Planung-Settings, Qualitäts-Präferenzen
- BUYER: Preis-Settings, Lieferkonditionen-Präferenzen
- ADMIN: System-Settings, Rechnung-Settings

**PR #3: CROSS-MODULE-INTEGRATION-TESTING (Day 50-52)**

**3.1 END-TO-END CROSS-MODULE INTEGRATION:**
- End-to-End Cross-Module Integration Tests
- Lead → Customer → Communication → Cockpit Flow
- Performance-Tests für Complete Business-Workflows

**3.2 COMPLETE BUSINESS-WORKFLOWS:**
- Lead-Erfassung → Customer-Conversion → Follow-up-Communication → Dashboard-KPIs
- Multi-Contact-Workflows über alle Module
- Territory-Performance End-to-End

**3.3 CROSS-MODULE EVENT-FLOWS:**
- LISTEN/NOTIFY Events zwischen allen Modulen
- Activity-Timeline Cross-Module-Integration
- Real-time Updates über Complete Pipeline

**PR #4: CORE-BUSINESS-STABILIZATION (Day 53)**

**4.1 PUFFER-TAG AKTIVITÄTEN:**
- PUFFER-TAG für Core-Business-Stabilisierung
- Performance-Benchmarks für alle Module
- Cross-Module Event-Flows dokumentiert

**4.2 PERFORMANCE-VALIDATION:**
- Alle Module <200ms P95 bestätigt (Foundation-Baseline)
- Cross-Module Performance ohne Degradation
- Load-Testing für Complete Business-Workflows

**4.3 DOCUMENTATION + HANDOVER:**
- Cross-Module Integration dokumentiert
- Performance-Benchmarks documented
- Ready für Phase 3 (Analytics/Help/Administration)

## ⚠️ KRITISCHE REGELN

**SETTINGS-REGISTRY FOUNDATION:**
- KEIN neues Settings-System bauen
- Settings-Registry aus Sprint 1.2 nutzen
- 5-Level Scope-Hierarchie ist empfohlen

**CROSS-MODULE INTEGRATION:**
- End-to-End Business-Workflows sollten funktionieren
- Performance sollte nicht degradieren
- Alle LISTEN/NOTIFY Events sollten funktional sein

**TERRITORY-BUSINESS-RULES:**
- Deutschland/Schweiz Business-Rules sind Business-Critical
- Automatic Territory-Detection + Rule-Application
- Settings müssen Territory-aware sein

## ✅ ERFOLGSMESSUNG

**SPRINT 2.5 IST FERTIG WENN:**
- [ ] 3 PRs erfolgreich merged + Puffer-Tag completed
- [ ] Settings UI operational auf Registry-Foundation
- [ ] Business-Rules-Engine für Territory-Management
- [ ] Complete Cross-Module Integration bestätigt
- [ ] Core Business-Workflows <200ms P95 (Foundation-Baseline)
- [ ] End-to-End Lead → Customer → Communication → Cockpit Flow

**ROADMAP-UPDATE:**
Nach Sprint 2.5 Complete:
- Progress: 21/35 → 24/35 (3 PRs)
- Status: ✅ Sprint 2.5 (YYYY-MM-DD) + ✅ PHASE 2 CORE BUSINESS COMPLETE
- Next Action: 🚀 PHASE 3 START - Sprint 3.1 Auswertungen
- Integration: Complete Core-Business-Platform ready für Enhancement-Phase

**BUSINESS-VALUE ERREICHT:**
- Complete Settings-Management für B2B-Food-Business
- Territory-aware Business-Rules Deutschland/Schweiz
- End-to-End Cross-Module Integration functional
- Complete Core-Business-Platform operational

**🚀 PHASE 2 CORE BUSINESS COMPLETE:**
Alle Core-Business-Module (02+03+05+01+06) sind operational und integriert!

**🔓 PHASE 3 ENHANCEMENT READY:**
Analytics, Help-System und Administration können auf Complete Core-Business-Platform aufbauen!

Arbeite systematisch PR #1 → #2 → #3 → Puffer-Tag!