# 🚀 VERBINDLICH: SPRINT 2.2 KUNDENMANAGEMENT - SYSTEMATISCHE UMSETZUNG

## 🔧 GIT WORKFLOW (KRITISCH!)

**PFLICHT-REGELN für alle Sprint-Arbeiten:**

### ✅ ERLAUBT (ohne User-Freigabe):
- `git commit` - Commits erstellen wenn User darum bittet
- `git add` - Dateien stagen
- `git status` / `git diff` - Status prüfen
- Feature-Branches anlegen (`git checkout -b feature/...`)

### 🚫 VERBOTEN (ohne explizite User-Freigabe):
- **`git push`** - NIEMALS ohne User-Erlaubnis pushen!
- **PR-Erstellung** - Nur auf explizite Anforderung
- **PR-Merge** - Nur wenn User explizit zustimmt
- **Branch-Deletion** - Remote-Branches nur mit User-OK löschen

### 📋 Standard-Workflow:
1. **Feature-Branch anlegen:** `git checkout -b feature/modXX-sprint-Y.Z-description`
2. **Arbeiten & Committen:** Code schreiben, Tests validieren, `git commit`
3. **User fragen:** "Branch ist bereit. Soll ich pushen und PR erstellen?"
4. **Erst nach Freigabe:** `git push` + PR-Erstellung

**Referenz:** `/CLAUDE.md` → Sektion "🚫 GIT PUSH POLICY (KRITISCH!)"

---

## ⚠️ WICHTIGE QUALITÄTSREGELN - VOLLSTÄNDIG BEFOLGEN!

**HINWEIS:** Diese Anweisungen sind verbindlich für professionelle Umsetzung. Übersprungene Schritte führen zu:
- ❌ Inkonsistente Field-based Customer Architecture → Wochen Nacharbeit
- ❌ Multi-Contact-System-Probleme → CHEF/BUYER-Workflows fehlerhaft
- ❌ Territory-Scoping-Probleme → Deutschland/Schweiz-Integration defekt
- ❌ Integration-Failures → Communication-Module (Sprint 2.3) betroffen

**QUALITÄTS-REGEL:** Du sollst jeden Abschnitt mit "✅ VERSTANDEN" bestätigen für optimale Ergebnisse.

**HINWEIS:** Bei übersprungenen Checkpoints kann die Qualität nicht garantiert werden.

## 📋 VERBINDLICH: DOKUMENTE-VALIDIERUNG (REIHENFOLGE EMPFOHLEN)

**1. ROADMAP-ORIENTIERUNG:**
Lies: `./docs/planung/PRODUCTION_ROADMAP_2025.md`
- Section "🎯 CLAUDE QUICK-START"
- Bestätige: Lead-Management (Sprint 2.1) ✅ Complete

**2. ARBEITSREGELN:**
Lies: `./docs/CLAUDE.md`
- Die 17 kritischen Regeln beachten

**3. BUSINESS-KONTEXT:**
Lies: `./docs/planung/CRM_AI_CONTEXT_SCHNELL.md`
- Field-based Customer Architecture
- Multi-Contact B2B-Management (CHEF/BUYER)
- Territory-Management Deutschland/Schweiz

**4. SPRINT-DETAIL:**
Lies: `./docs/planung/PRODUCTION_ROADMAP_2025.md` - Section "Sprint 2.2: Kundenmanagement"

**5. TECHNICAL CONCEPT:**
Lies: `./docs/planung/features-neu/03_kundenmanagement/technical-concept.md`
- Dynamic Customer-Schema statt Entity-based
- Field-based Architecture mit JSONB
- Multi-Contact Support für B2B-Food

**6. VERFÜGBARE ARTEFAKTE:**
Analysiere: `./docs/planung/features-neu/03_kundenmanagement/artefakte/`
- 39 Production-Ready Deliverables (EXCEPTIONAL Quality 10/10)
- Field-based Architecture + ABAC Security + Testing 80%+

**🎯 COPY-PASTE READY PATTERNS (aus PR #110):**
- `./docs/planung/features-neu/02_neukundengewinnung/artefakte/SECURITY_TEST_PATTERN.md`
  → Nutze für Customer-Security-Tests (@TestSecurity, Fail-Closed)
- `./docs/planung/features-neu/02_neukundengewinnung/artefakte/PERFORMANCE_TEST_PATTERN.md`
  → Nutze für Customer-API P95 Validation (measureP95(), assertP95())
- `./docs/planung/features-neu/02_neukundengewinnung/artefakte/EVENT_SYSTEM_PATTERN.md`
  → Nutze für CUSTOMER_CREATED/UPDATED Events

## 🔒 WICHTIG: MIGRATION-CHECK (IGNORIEREN RISIKOBEHAFTET!)

**WICHTIG:** Jede DB-Änderung ohne Migration-Check kann zu Deployments-Problemen führen.

**RISIKEN BEI ÜBERSPRINGEN:**
- ⚠️ Migration-Konflikte im Team
- ⚠️ Deployment-Komplikationen in Staging
- ⚠️ Sprint 2.3+ könnten verzögert werden
- ⚠️ Customer-Management unvollständig

**VOR JEDER DB-ÄNDERUNG (EMPFOHLEN):**
```bash
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
echo "Nächste Migration: $MIGRATION"
# Bei Script-Fehler: ls backend/src/main/resources/db/migration/ | tail -3
```

**BESTÄTIGUNG EMPFOHLEN:** Schreibe "✅ MIGRATION-CHECK ABGESCHLOSSEN: V{NUMMER}"

## 🎯 IMPLEMENTIERUNGS-AUFTRAG

**SPRINT:** Sprint 2.2: Kundenmanagement (NACH Lead-Management ✅)
**MODULE:** 03_kundenmanagement
**GESCHÄTZTE ARBEITSZEIT:** 10-12 Stunden (5 PRs)

**PRs IN DIESEM SPRINT:**
1. **Field-Architecture-Core:** feature/sprint-2-2-customers-field-architecture-core-v{MIGRATION}-FP-237
2. **Multi-Contact-System:** feature/sprint-2-2-customers-multi-contact-system-v{MIGRATION+1}-FP-238
3. **Territory-Scoping:** feature/sprint-2-2-customers-territory-scoping-v{MIGRATION+2}-FP-239
4. **Frontend-Integration:** feature/sprint-2-2-customers-frontend-integration-v{MIGRATION+3}-FP-240
5. **Complete-Testing:** feature/sprint-2-2-customers-complete-testing-v{MIGRATION+4}-FP-241

**KUNDENMANAGEMENT bedeutet:**
- Field-based Customer Architecture (JSONB, nicht Entity-based)
- Multi-Contact Support für B2B-Food (CHEF/BUYER parallel)
- Territory-Management Deutschland/Schweiz
- Lead-to-Customer Conversion Pipeline

**SUCCESS-CRITERIA:**
- [ ] Field-based Customer Architecture operational
- [ ] Multi-Contact-System für B2B-Food funktional
- [ ] Territory-Management Deutschland/Schweiz
- [ ] Integration mit Lead-Management bestätigt
- [ ] Performance <200ms P95 auf CQRS Foundation (Foundation-Baseline)

## 🚀 IMPLEMENTIERUNGS-SCHRITTE

**PR #1: FIELD-ARCHITECTURE-CORE (Day 23-24)**

**1.1 MIGRATION-CHECK + BRANCH:**
```bash
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
git checkout main && git pull
git checkout -b feature/sprint-2-2-customers-field-architecture-core-v${MIGRATION}-FP-237
```

**1.2 DYNAMIC CUSTOMER-SCHEMA:**
- Field-based Architecture statt Entity-based
- JSONB + Performance-Optimization
- Flexible Schema für verschiedene Gastronomiebetriebe

**1.3 CORE CUSTOMER-ENTITIES:**
- Customer-Core-Entity mit JSONB Fields
- Dynamic Field-Definitions
- Performance-Indices für JSONB-Queries

**PR #2: MULTI-CONTACT-SYSTEM (Day 25-26)**

**2.1 MULTI-CONTACT SUPPORT:**
- Multi-Contact Support (CHEF/BUYER parallel)
- Contact-Role-Management
- Complex Gastronomiebetrieb-Requirements

**2.2 B2B-FOOD-KONTAKTE:**
- CHEF: Menu-Planung, Qualität, Produktauswahl
- BUYER: Einkauf, Preise, Lieferkonditionen
- ADMIN: Administration, Rechnungen, Verträge

**2.3 CONTACT-WORKFLOWS:**
- Lead-to-Contact Conversion aus Sprint 2.1
- Multi-Contact Communication-Preparation
- Role-based Permissions Integration

**PR #3: TERRITORY-SCOPING (Day 27-28)**

**3.1 TERRITORY-MANAGEMENT:**
- Territory-Management Deutschland/Schweiz
- Currency + Tax + Regional-Specialties
- RLS Territory-Scoping Integration

**3.2 DEUTSCHLAND-SPEZIFIKA:**
- EUR Currency, 19% MwSt
- Deutsche Lebensmittelvorschriften
- Regionale Spezialitäten

**3.3 SCHWEIZ-SPEZIFIKA:**
- CHF Currency, 7.7% MwSt
- Schweizer Lebensmittelvorschriften
- Bio-Suisse + Swiss Made

**PR #4: FRONTEND-INTEGRATION (Day 29-30)**

**4.1 REACT FRONTEND COMPONENTS:**
- React Frontend Components
- Field-Based Forms + Validation
- Multi-Contact UI/UX

**4.2 CUSTOMER-FORMS:**
- Dynamic Field-based Customer-Forms
- Multi-Contact-Management UI
- Territory-aware Currency/Tax Display

**4.3 INTEGRATION-WORKFLOWS:**
- Lead-to-Customer Conversion UI
- Customer-Detail-Views
- Multi-Contact-Timeline Integration

**PR #5: COMPLETE-TESTING (Day 31)**

**5.1 END-TO-END CUSTOMER-LIFECYCLE:**
- End-to-End Customer-Lifecycle Tests
- Performance-Validation auf CQRS
- Cross-Module Integration mit 02 Leads

**5.2 PERFORMANCE-VALIDATION:**
- Customer-Queries <200ms P95
- JSONB-Query-Performance optimiert
- LISTEN/NOTIFY für Customer-Changes

**5.3 CROSS-MODULE INTEGRATION:**
- Lead-to-Customer Conversion Pipeline
- Activity-Timeline Cross-Module-Events
- Preparation für Communication-Module (Sprint 2.3)

## ⚠️ KRITISCHE REGELN

**FIELD-BASED ARCHITECTURE:**
- KEINE Entity-based Lösung verwenden
- JSONB für flexible Customer-Fields
- Performance-Indices für alle JSONB-Queries

**TERRITORY-ISOLATION:**
- Deutschland/Schweiz ABSOLUT getrennt
- Currency/Tax automatisch per Territory
- RLS Territory-Scoping ist VERPFLICHTEND

**MULTI-CONTACT SUPPORT:**
- B2B-Food braucht parallel CHEF/BUYER
- Contact-Roles sind Business-Critical
- Keine Single-Contact-Limitation

## ✅ ERFOLGSMESSUNG

**SPRINT 2.2 IST FERTIG WENN:**
- [ ] 5 PRs erfolgreich merged (Field + Contact + Territory + Frontend + Testing)
- [ ] Field-based Customer Architecture operational
- [ ] Multi-Contact-System für B2B-Food funktional
- [ ] Territory-Management Deutschland/Schweiz
- [ ] Lead-to-Customer Conversion Pipeline
- [ ] Performance <200ms P95 bestätigt (Foundation-Baseline)

**ROADMAP-UPDATE:**
Nach Sprint 2.2 Complete:
- Progress: 9/35 → 14/35 (5 PRs)
- Status: ✅ Sprint 2.2 (YYYY-MM-DD)
- Next Action: Security-Gate Validation für Sprint 2.3
- Integration: Ready für Communication-Module

**BUSINESS-VALUE ERREICHT:**
- Field-based Customer Management für B2B-Food
- Multi-Contact-Support für komplexe Gastronomiebetriebe
- Territory-Management für Deutschland/Schweiz-Expansion
- Lead-to-Customer Conversion Pipeline operational

**🔓 NEXT PHASE READY:**
Communication-Module (Sprint 2.3) kann auf Customer/Lead-Context zugreifen!

Arbeite systematisch PR #1 → #2 → #3 → #4 → #5!