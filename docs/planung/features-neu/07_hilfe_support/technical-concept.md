# Modul 07 Hilfe & Support - Technical Concept

**📊 Plan Status:** ✅ Approved - Ready for Implementation
**🎯 Owner:** Development Team
**⏱️ Timeline:** 2025-09-20 → 2025-10-04 (2 Wochen)
**🔧 Effort:** M (Medium) - 1.5 Wochen Development + 0.5 Wochen Testing

---

## 🎯 Executive Summary (für Claude)

**Mission:** Hybrid Help & Support System mit "Calibrated Assistive Rollout" (CAR) Strategy
**Problem:** 90% implementiertes, innovatives Hilfe-System versteckt unter Admin-Routen
**Solution:** Router-Integration + Enterprise Browse-Mode + CAR-Guardrails für proaktive Hilfe
**Timeline:** 2-Wochen-Sprint mit copy-paste-ready AI-Artefakten
**Impact:** Unique Struggle Detection + Traditional Help Center = Enterprise-grade User Experience

**Quick Context:** Bestehende Struggle Detection + ProactiveHelp bereits production-ready. Nur Router-Integration und Browse-Mode fehlen für vollständiges Hilfe-System mit Innovation + Enterprise-Standards.

---

## 📋 Context & Dependencies

### Current State:
- ✅ **90% Help System implementiert:** HelpProvider, ProactiveHelp, HelpModal, Analytics
- ✅ **Struggle Detection ready:** Pattern recognition für REPEATED_FAILED_ATTEMPTS, RAPID_NAVIGATION_CHANGES
- ✅ **Admin Dashboard functional:** Content-Management, Analytics, Tour-Builder
- ✅ **AI-Artefakte delivered:** 25 copy-paste-ready files (9.4/10 quality)
- ❌ **Router-Integration fehlt:** Keine user-facing Navigation zu Hilfe-Features

### Target State:
- ✅ **Hybrid Architecture:** "Assistive-First, Browse-Always" für optimal UX
- ✅ **CAR-Strategy active:** Confidence ≥0.7, Dynamic Budget, Context-aware Cooldowns
- ✅ **Guided Operations:** Follow-Up T+3/T+7, ROI Quick-Check workflows
- ✅ **Enterprise Browse-Mode:** Searchable Knowledge Base mit traditional navigation
- ✅ **Full Observability:** 5 Core KPIs für Go/No-Go decisions

### Dependencies:
- **Completed:** → [Modul 06 Settings System](../06_einstellungen/) (für CAR-Parameter)
- **Concurrent:** → [Modul 05 Communication](../05_kommunikation/) (für Activities-Integration)
- **Foundation:** → [Design System V2](../../grundlagen/DESIGN_SYSTEM.md) (für MUI v5 + Tokens)

---

## 🛠️ Implementation Phases

### Phase 1: CAR-Foundation (Woche 1, Tag 1-3)

**Goal:** Router-Integration + CAR-Guardrails aktivieren
**Actions:**

#### Backend Setup (Tag 1):
- [ ] **Deploy SQL Migration:** `VXXX__help_core.sql` (Nummer via ./scripts/get-next-migration.sh ermitteln)
- [ ] **Copy Backend Services:** HelpService.java + HelpRepository.java + Resources
- [ ] **Settings Integration:** Wire CAR-Parameters from Modul 06 Settings-Service
- [ ] **Test Basic APIs:** `/api/help/menu`, `/api/help/suggest` funktional

**Code Changes:**
```java
// HelpResource.java - Replace hardcoded CAR values
double minConfidence = settingsService.getDouble("help.nudge.confidence.min", 0.7);
int base = settingsService.getInt("help.nudge.budget.session.base", 2);
```

#### Frontend Integration (Tag 2-3):
- [ ] **Copy React Components:** HelpHubPage.tsx, helpApi.ts, helpRoutes.tsx
- [ ] **Router Integration:** Add helpRoutes to App.tsx
- [ ] **HelpProvider Activation:** Enable in MainLayout globally
- [ ] **Test CAR Features:** Session budgets, confidence filtering working

**Success Criteria:**
- Backend APIs respond with correct CAR-filtering (confidence ≥0.7)
- Frontend routes `/hilfe/*` accessible and functional
- HelpProvider detects struggle patterns automatically
- Settings-Service provides dynamic CAR-configuration

**Next:** → [Phase 2](#phase-2-guided-operations)

---

### Phase 2: Guided Operations (Woche 1, Tag 4-5)

**Goal:** T+3/T+7 Follow-Up + ROI Quick-Check workflows operational
**Actions:**

#### Guided Workflows (Tag 4):
- [ ] **Copy Guided Components:** FollowUpWizard.tsx + ROIMiniCheck.tsx
- [ ] **API Integration:** GuidedResource.java endpoints active
- [ ] **Activities Mock-to-Real:** Wire Follow-Up creation to Modul 05 Activities API
- [ ] **Test Guided Flows:** End-to-end T+3/T+7 planning + ROI calculations

**Code Changes:**
```java
// HelpService.planFollowUp() - Replace mock with real Activities API
POST /api/activities/bulk {
  activities: [
    { type: "FOLLOW_UP", dueDate: now.plusDays(3), accountId: req.accountId },
    { type: "FOLLOW_UP", dueDate: now.plusDays(7), accountId: req.accountId }
  ]
}
```

#### Content & Browse-Mode (Tag 5):
- [ ] **Settings Registry Import:** help-specific keys for CAR-Parameters
- [ ] **Seed Content Creation:** 5-10 Artikel via Admin-Dashboard
- [ ] **Browse-Mode Polish:** Search, filtering, categorization working
- [ ] **Cross-Mode Navigation:** Links zwischen Assistive ↔ Browse Modes

**Success Criteria:**
- Follow-Up Wizard creates real Activities in Modul 05
- ROI Quick-Check delivers business-relevant calculations
- Browse-Mode shows searchable, filterable content
- Seamless navigation between proactive + traditional help

**Next:** → [Phase 3](#phase-3-monitoring-optimization)

---

### Phase 3: Monitoring & Optimization (Woche 2, Tag 1-3)

**Goal:** Full observability + performance optimization ready
**Actions:**

#### Metrics & Monitoring (Tag 1-2):
- [ ] **Import Grafana Dashboard:** help_dashboard.json with 5 Core KPIs
- [ ] **Configure Prometheus Alerts:** help_alerts.yaml for Go/No-Go criteria
- [ ] **Metrics Wiring:** HelpMetrics.java integration with actual events
- [ ] **Performance Validation:** k6 tests confirm p95 < 150ms

**Monitoring KPIs:**
```yaml
Core Metrics (Go/No-Go):
  - Nudge Acceptance Rate: ≥30%
  - False Positive Rate: ≤10%
  - Time-to-Help p95: ≤30s
  - Self-Serve Rate: ≥15%
  - Guided→Activity Conversion: ≥20%
```

#### Testing & Quality (Tag 3):
- [ ] **BDD Tests Running:** HelpServiceBDDTest.java + ABAC contract tests
- [ ] **Performance Testing:** k6 load tests with realistic scenarios
- [ ] **E2E Validation:** Critical user journeys Struggle→Suggestion→Resolution
- [ ] **ABAC Security:** Row-Level Security + persona/territory filtering verified

**Success Criteria:**
- All 5 KPIs measurable in Grafana dashboard
- Prometheus alerts fire correctly for threshold violations
- Performance tests confirm <150ms p95 response times
- Security tests validate proper ABAC filtering

**Next:** → [Phase 4](#phase-4-production-readiness)

---

### Phase 4: Production Readiness (Woche 2, Tag 4-5)

**Goal:** Enterprise-grade deployment readiness + documentation complete
**Actions:**

#### Production Deployment (Tag 4):
- [ ] **Feature Flags Setup:** help.nudge.enabled for gradual rollout
- [ ] **Content Population:** 15-20 production Artikel via CMS
- [ ] **Integration Testing:** Full workflow Struggle→Help→Activity→Resolution
- [ ] **Load Testing:** Production-level traffic simulation

#### Documentation & Handover (Tag 5):
- [ ] **Admin Documentation:** Content-Management workflow guide
- [ ] **User Training Materials:** Help-System usage examples
- [ ] **Operational Runbook:** Monitoring, alerts, troubleshooting procedures
- [ ] **Knowledge Transfer:** Team walkthrough + Q&A session

**Success Criteria:**
- System handles production load (100+ concurrent users)
- Content creation workflow documented and tested
- Monitoring alerts verified with realistic scenarios
- Team confident in system operation and maintenance

**Next:** → Production Go-Live 🚀

---

## ✅ Success Metrics

### Quantitative (Week 2):
- **API Performance:** P95 < 150ms (currently: new system)
- **Nudge Acceptance:** ≥30% user acceptance rate
- **False Positive Rate:** ≤10% irrelevant suggestions
- **Self-Serve Success:** ≥15% problems solved without escalation
- **Activity Creation:** ≥20% guided workflows create actual Activities

### Qualitative:
- **Struggle Detection:** Proactively identifies user problems automatically
- **Seamless UX:** Users easily navigate between assistive + browse modes
- **Enterprise Compliance:** Full ABAC security + audit trails
- **Content Management:** Non-developers can create/edit help content
- **Monitoring Ready:** Full observability with actionable alerts

### Timeline:
- **Week 1:** CAR-System + Guided Operations operational
- **Week 2:** Monitoring + Production readiness complete
- **Go-Live:** Ready for gradual rollout with feature flags

---

## 🔗 Related Documentation

### Foundation Knowledge:
- **CAR Strategy Deep-Dive:** → [AI Discussion Results](diskussionen/2025-09-20_KI_NACHFRAGE_ANTWORT_WUERDIGUNG.md)
- **Codebase Analysis:** → [Complete Analysis](analyse/01_CODEBASE_ANALYSIS.md)
- **Strategic Recommendations:** → [Option B Hybrid](analyse/03_STRATEGIC_RECOMMENDATIONS.md)

### Implementation Details:
- **AI Artefakte (25 Files):** → [/diskussionen/artefakte/](diskussionen/artefakte/)
- **API Specification:** → [help-api.yaml](diskussionen/artefakte/help-api.yaml)
- **Frontend Components:** → [React Components](diskussionen/artefakte/HelpHubPage.tsx)
- **Database Schema:** → [VXXX__help_core.sql](artefakte/backend/sql/VXXX__help_core.sql) (Nummer via Scripts ermitteln)

### Integration Points:
- **Settings Service:** → [Modul 06 Einstellungen](../06_einstellungen/technical-concept.md)
- **Activities API:** → [Modul 05 Communication](../05_kommunikation/technical-concept.md)
- **Design System:** → [Foundation Standards](../../grundlagen/DESIGN_SYSTEM.md)

### Quality & Testing:
- **AI Artefakte Review:** → [Kritische Würdigung](diskussionen/2025-09-20_FINALE_AI_ARTEFAKTE_KRITISCHE_WUERDIGUNG.md)
- **Foundation Compliance:** → [Standards Checklist](../../grundlagen/FOUNDATION_STANDARDS.md)

---

## 🤖 Claude Handover Section

**Für nächsten Claude:**

### Aktueller Stand:
Technical Concept komplett, AI-Artefakte analysiert (9.4/10 Quality), CAR-Strategy finalisiert. Alle 25 Implementation-Files copy-paste-ready in `/artefakte/`. 2-Wochen-Sprint-Plan definiert mit konkreten Daily Actions. Migration numbering auf dynamische Script-basierte Ermittlung umgestellt.

### Nächster konkreter Schritt:
**Ermittle korrekte Migrationsnummer** + **Deploy VXXX SQL Migration** + **Copy Backend Services** (Phase 1, Tag 1). Alle Files sind in `/artefakte/` bereit. Starte mit `./scripts/get-next-migration.sh`, dann `VXXX__help_core.sql` mit korrekter Nummer deployen, danach HelpService.java + HelpRepository.java kopieren.

### Wichtige Dateien für Context:
- **`artefakte/backend/api/help-api.yaml`** - OpenAPI 3.1 Specification mit CAR-Endpoints
- **`artefakte/backend/java/HelpService.java`** - Core Business Logic mit CAR-Algorithm
- **`artefakte/frontend/hooks/helpApi.ts`** - Frontend API Client mit Type Safety
- **`artefakte/backend/sql/VXXX__help_core.sql`** - Database Schema (Nummer via Scripts ermitteln)
- **`diskussionen/2025-09-20_FINALE_AI_ARTEFAKTE_KRITISCHE_WUERDIGUNG.md`** - Quality Assessment (9.4/10)

### Offene Entscheidungen:
- **Migration Nummer:** VXXX muss über ./scripts/get-next-migration.sh zur Implementierungszeit ermittelt werden
- **Settings Service Integration:** 2-3 Zeilen Code-Change in HelpResource.java nötig
- **Activities API Wiring:** Follow-Up Mock → Real API Connection in Phase 2

### Kontext-Links:
- **Grundlagen:** → [Planungsmethodik](../../PLANUNGSMETHODIK.md)
- **Dependencies:** → [Settings System](../06_einstellungen/) + [Communication](../05_kommunikation/)
- **Quality Assurance:** → [Foundation Standards](../../grundlagen/FOUNDATION_STANDARDS.md)

### Integration-Ready Assets:
- **25 AI-Artefakte:** 92% copy-paste-ready, exceptional quality
- **CAR-Parameters:** Exakt spezifiziert und implementiert
- **Test Suite:** BDD + Performance + ABAC tests ready
- **Monitoring:** Grafana Dashboard + Prometheus Alerts ready

**Ready für immediate 2-week sprint start! 🚀**