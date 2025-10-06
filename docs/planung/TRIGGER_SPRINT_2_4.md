# 🚀 VERBINDLICH: SPRINT 2.4 COCKPIT - SYSTEMATISCHE UMSETZUNG

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
- ❌ Inkonsistente Dashboard-Performance → <200ms P95 nicht erreichbar
- ❌ ROI-Calculator-Probleme → B2B-Food-Business-Intelligence fehlerhaft
- ❌ Real-time-Updates-Probleme → LISTEN/NOTIFY-Integration defekt
- ❌ Integration-Failures → Settings-Module (Sprint 2.5) betroffen

**QUALITÄTS-REGEL:** Du sollst jeden Abschnitt mit "✅ VERSTANDEN" bestätigen für optimale Ergebnisse.

**HINWEIS:** Bei übersprungenen Checkpoints kann die Qualität nicht garantiert werden.

## 📋 VERBINDLICH: DOKUMENTE-VALIDIERUNG (REIHENFOLGE EMPFOHLEN)

**1. ROADMAP-ORIENTIERUNG:**
Lies: `./docs/planung/PRODUCTION_ROADMAP_2025.md`
- Section "🎯 CLAUDE QUICK-START"
- Bestätige: Kommunikation (Sprint 2.3) ✅ Complete

**2. ARBEITSREGELN:**
Lies: `./docs/CLAUDE.md`
- Die 17 kritischen Regeln beachten

**3. BUSINESS-KONTEXT:**
Lies: `./docs/planung/CRM_AI_CONTEXT_SCHNELL.md`
- ROI-Dashboard für B2B-Food-Business
- Real-time Widgets mit Live-Updates
- Territory-Performance Insights

**4. SPRINT-DETAIL:**
Lies: `./docs/planung/PRODUCTION_ROADMAP_2025.md` - Section "Sprint 2.4: Cockpit"

**5. TECHNICAL CONCEPT:**
Lies: `./docs/planung/features-neu/01_mein-cockpit/technical-concept.md`
- CQRS-optimierte Dashboard-Performance
- Real-time Updates via LISTEN/NOTIFY
- ROI-Calculator für Business-Value-Demo

**6. VERFÜGBARE ARTEFAKTE:**
Analysiere: `./docs/planung/features-neu/01_mein-cockpit/artefakte/`
- 44 Production-Ready Artefakte (Enterprise Assessment A+ 95/100)
- ABAC Security + ROI-Calculator + Multi-Channel Dashboard

**🎯 COPY-PASTE READY PATTERNS (aus PR #110):**
- `./docs/planung/features-neu/02_neukundengewinnung/artefakte/EVENT_SYSTEM_PATTERN.md`
  → Nutze für Dashboard-Widget-Updates (Real-time via LISTEN/NOTIFY)
- `./docs/planung/features-neu/02_neukundengewinnung/artefakte/PERFORMANCE_TEST_PATTERN.md`
  → Nutze für Dashboard-Query P95 < 200ms Validation
- `./docs/planung/features-neu/02_neukundengewinnung/artefakte/SECURITY_TEST_PATTERN.md`
  → Nutze für Dashboard-Access-Control Tests

## 🔒 WICHTIG: MIGRATION-CHECK (IGNORIEREN RISIKOBEHAFTET!)

**WICHTIG:** Jede DB-Änderung ohne Migration-Check kann zu Deployments-Problemen führen.

**RISIKEN BEI ÜBERSPRINGEN:**
- ⚠️ Migration-Konflikte im Team
- ⚠️ Deployment-Komplikationen in Staging
- ⚠️ Sprint 2.5+ könnten verzögert werden
- ⚠️ Dashboard-Module unvollständig

**VOR JEDER DB-ÄNDERUNG (EMPFOHLEN):**

```bash
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
echo "Nächste Migration: $MIGRATION"
# Bei Script-Fehler: ls backend/src/main/resources/db/migration/ | tail -3
```

**BESTÄTIGUNG EMPFOHLEN:** Schreibe "✅ MIGRATION-CHECK ABGESCHLOSSEN: V{NUMMER}"

## 🎯 IMPLEMENTIERUNGS-AUFTRAG

**SPRINT:** Sprint 2.4: Cockpit (NACH Kommunikation ✅)
**MODULE:** 01_mein-cockpit
**GESCHÄTZTE ARBEITSZEIT:** 6-8 Stunden (4 PRs)

**PRs IN DIESEM SPRINT:**
1. **Dashboard-Widgets:** feature/sprint-2-4-cockpit-dashboard-widgets-v{MIGRATION}-FP-246
2. **ROI-Calculator:** feature/sprint-2-4-cockpit-roi-calculator-v{MIGRATION+1}-FP-247
3. **CQRS-Integration:** feature/sprint-2-4-cockpit-cqrs-integration-v{MIGRATION+2}-FP-248
4. **Performance-Optimization:** feature/sprint-2-4-cockpit-performance-optimization-v{MIGRATION+3}-FP-249

**COCKPIT bedeutet:**
- ROI-Dashboard für B2B-Food-Business-Intelligence
- Real-time Widgets mit LISTEN/NOTIFY Live-Updates
- Territory-Performance + Multi-Channel-Analytics
- Cross-Module KPI-Integration (Leads + Customers + Communication)

**SUCCESS-CRITERIA:**
- [ ] Dashboard-Performance <200ms P95 Standard, 400ms Heavy-KPIs
- [ ] ROI-Calculator für B2B-Food-Business funktional
- [ ] Real-time Updates via LISTEN/NOTIFY
- [ ] Cross-Module KPI-Integration bestätigt
- [ ] Territory-Performance Insights operational

## 🚀 IMPLEMENTIERUNGS-SCHRITTE

**PR #1: DASHBOARD-WIDGETS (Day 39-40)**

**1.1 MIGRATION-CHECK + BRANCH:**
```bash
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
git checkout main && git pull
git checkout -b feature/sprint-2-4-cockpit-dashboard-widgets-v${MIGRATION}-FP-246
```

**1.2 REAL-TIME DASHBOARD-WIDGETS:**
- Territory-Performance + ROI-Insights
- Lead-Conversion-Tracking
- Customer-Acquisition-Metrics
- Communication-Activity-Stream

**1.3 LISTEN/NOTIFY LIVE-UPDATES:**
- CQRS Event-Integration für Real-time Updates
- WebSocket-Connection für Dashboard-Widgets
- Live-Badges via LISTEN/NOTIFY + Journal-Fallback

**1.4 B2B-FOOD BUSINESS-KPIS:**
- Sample-Success-Rate (T+3/T+7 Follow-up)
- Territory-Penetration Deutschland/Schweiz
- Multi-Contact-Engagement (CHEF/BUYER)
- ROI per Lead-Source

**PR #2: ROI-CALCULATOR (Day 41-42)**

**2.1 ROI-CALCULATOR CORE:**
- ROI-Calculator für Business-Value-Demo
- Multi-Channel B2B-Food-Calculations
- Cost-per-Lead + Conversion-Tracking

**2.2 B2B-FOOD ROI-METRICS:**
- Sample-Cost vs Order-Value
- Territory-Expansion ROI (DE vs CH)
- Multi-Contact-ROI (CHEF vs BUYER Conversion)
- Seasonal-Performance-Analysis

**2.3 BUSINESS-VALUE-DEMO:**
- Before/After ROI-Comparisons
- Territory-Optimization Recommendations
- Lead-Source-Performance-Ranking
- Sample-Strategy-Optimization

**PR #3: CQRS-INTEGRATION (Day 43-44)**

**3.1 HOT-PROJECTIONS:**
- Hot-Projections für Dashboard-Performance
- Read-optimized Views auf CQRS Foundation
- ETag-Caching für <200ms Dashboard-Loads

**3.2 PERFORMANCE-OPTIMIERUNG:**
- Dashboard-Queries auf Read-Replica
- Materialized Views für KPI-Calculations
- CQRS Query-Services für Dashboard-Data

**3.3 CACHE-STRATEGIE:**
- ETag-Caching für Dashboard-Components
- LISTEN/NOTIFY Cache-Invalidation
- Performance-Budget <400ms P95 (Heavy-KPI-Aggregationen), Standard-Widgets <200ms P95

**PR #4: PERFORMANCE-OPTIMIZATION (Day 45)**

**4.1 CROSS-MODULE KPI-INTEGRATION:**
- Cross-Module KPI-Integration (02+03+05)
- Lead-to-Customer-to-Communication Pipeline-Metrics
- Territory-Performance Cross-Module

**4.2 PERFORMANCE-VALIDATION:**
- Dashboard-Performance <400ms P95 für Heavy-KPIs, Standard-Widgets <200ms P95 bestätigt
- Live-Updates ohne OLTP-Performance-Impact
- Concurrent-User-Load-Testing

**4.3 FINAL INTEGRATION:**
- Complete Dashboard-Functionality
- All Widgets operational mit Live-Updates
- Cross-Module Event-Integration validated

## ⚠️ KRITISCHE REGELN

**PERFORMANCE-WICHTIG:**
- Dashboard sollte OLTP-Performance nicht beeinträchtigen
- Alle Queries bevorzugt auf Read-Replica/Materialized Views
- <200ms P95 Standard, <400ms Heavy-KPIs (angemessen für 5-50 User internes Tool)

**REAL-TIME UPDATES:**
- LISTEN/NOTIFY für alle Live-Updates
- WebSocket-Connection für Dashboard-Clients
- Fallback-Mechanism bei Connection-Loss

**CROSS-MODULE INTEGRATION:**
- Dashboard zeigt ALLE Module-KPIs
- Lead → Customer → Communication Pipeline sichtbar
- Territory-Performance übergreifend

## ✅ ERFOLGSMESSUNG

**SPRINT 2.4 IST FERTIG WENN:**
- [ ] 4 PRs erfolgreich merged (Widgets + ROI + CQRS + Performance)
- [ ] Dashboard-Performance <200ms P95 Standard, 400ms Heavy-KPIs
- [ ] ROI-Calculator für B2B-Food-Business funktional
- [ ] Real-time Updates via LISTEN/NOTIFY
- [ ] Cross-Module KPI-Integration bestätigt
- [ ] Territory-Performance Insights operational

**ROADMAP-UPDATE:**
Nach Sprint 2.4 Complete:
- Progress: 17/35 → 21/35 (4 PRs)
- Status: ✅ Sprint 2.4 (YYYY-MM-DD)
- Next Action: Sprint 2.5 Einstellungen + Cross-Module
- Integration: Dashboard ready für Settings-Integration

**BUSINESS-VALUE ERREICHT:**
- Executive-Dashboard für B2B-Food-Business-Intelligence
- ROI-Calculator für Business-Value-Demonstration
- Real-time Territory-Performance-Insights
- Complete Lead-to-Customer-to-Communication Pipeline-Visibility

**🔓 NEXT PHASE READY:**
Settings-Module (Sprint 2.5) kann Dashboard-Konfiguration nutzen!

Arbeite systematisch PR #1 → #2 → #3 → #4!