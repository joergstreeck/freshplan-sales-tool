# Auswertungen & Reports - Technical Concept

**📊 Plan Status:** ✅ Ready for Implementation
**🎯 Owner:** Backend + Frontend Team
**⏱️ Timeline:** Q4 2025 Woche 4-6 (2-3 Wochen Implementation)
**🔧 Effort:** M (Medium - Integration bestehender Analytics mit neuen APIs)

## 🎯 Executive Summary (für Claude)

**Mission:** Enterprise-Grade Reporting & Analytics Platform für B2B-Food-Vertrieb mit Universal Export, Real-time KPIs und Data Science Integration.
**Problem:** Bestehende Analytics-Infrastruktur (792 LOC modern) hat Route-Mismatches, headless Services ohne API-Endpoints und fehlende Export-Integration trotz vollständig implementiertem Universal Export Framework.
**Solution:** Thin API-Layer für bestehende Services + Route-Harmonisierung + JSONL-Streaming für Data Science + WebSocket Real-time Updates + Daily Projections für Performance.
**Timeline:** 2-3 Wochen für Production-Deployment mit 97% fertige Artefakte aus strategischer KI-Diskussion.
**Impact:** B2B-Food-spezifische Analytics (Sample-Success-Rate, ROI-Pipeline, Partner-vs-Direct-Mix) mit Export-ready Reports für Gastronomiebetriebe und Cook&Fresh® Performance-Tracking.

**Quick Context:** Basiert auf detaillierter Codebase-Analyse (560 Zeilen) + Legacy-Features-Validation + zweistufige KI-Diskussion mit production-ready SQL-Schemas, ABAC-Security und JSONL-Streaming-Artefakten.

## 📋 Context & Dependencies

### Current State:
- ✅ **Modern Analytics-Foundation:** AuswertungenDashboard.tsx (169 LOC) + SalesCockpitService.java (559 LOC) production-ready
- ✅ **Universal Export Framework:** Vollständig implementiert in Kundenliste mit CSV/Excel/PDF/JSON/HTML Support
- ✅ **Performance-optimierte Backend-Services:** CostStatistics.java + Enterprise-Grade KPI-Aggregationen
- ✅ **Route-Infrastructure:** /reports/* Backend-Routen definiert, Frontend-Dashboard vorhanden
- ✅ **Legacy-Assets discovered:** FC-016 KPI-Tracking Konzepte 70% implementiert, FC-011 Analytics-APIs spezifiziert

### Target State:
- ✅ **Route-Harmonisierung:** Einheitlich /reports/* mit 301-Redirects von /berichte/*
- ✅ **Thin API-Layer:** ReportsResource wraps bestehende Services ohne Logic-Duplikation
- ✅ **JSONL Data Science Export:** Memory-efficient Streaming für >100k Records
- ✅ **Real-time Badge Updates:** WebSocket-Integration für Live-KPIs mit Polling-Fallback
- ✅ **ABAC Security:** Territory/Chain-Scoping für Multi-Location-Accounts

### Dependencies:
- **Universal Export Integration:** → [Export-Framework in Kundenliste](./diskussionen/2025-09-19_GAP-CLOSURE_FINALE_BEWERTUNG.md#universalexportadapter-java)
- **Database Performance:** → [SQL-Projections & Indices](./diskussionen/reports_projections.sql)
- **Analytics Services:** → [SalesCockpitService Integration](./analyse/VOLLSTÄNDIGE_AUSWERTUNGEN_CODEBASE_ANALYSE.md#backend-implementation)

## 🛠️ Implementation Phases

### **Phase 1: Foundation (Woche 1) - API-Layer + Route-Harmonisierung**

**Goals:**
- Route-404s eliminiert durch /reports/* Harmonisierung
- Thin Controller-Layer für bestehende Analytics-Services
- Universal Export Integration im Dashboard aktiv

**Key Deliverables:**
```yaml
✅ API-Implementation:
- ReportsResource.java mit 4 Core-Endpoints
- Route-Redirects /berichte/* → /reports/*
- SecurityScopeFilter für ABAC Territory-Scoping

✅ Frontend-Integration:
- AuswertungenDashboard mit echten API-Calls statt Mock-Data
- UniversalExportButton Integration (alle Formate)
- Error-Boundaries für API-Failure-Handling

✅ Database-Foundation:
- SQL-Views deployment (rpt_activities_90d, rpt_sample_success_90d)
- Performance-Indices (activities, field_values, commission_event)
- Daily Snapshot-Table mit Recompute-Function
```

**Success Criteria:**
- Alle Dashboard-Links klickbar ohne 404-Errors
- P95 API-Response-Time <200ms für alle /api/reports/* Endpoints
- Universal Export funktional für alle Report-Typen

**Artefakte:** [ReportsResource.java](./artefakte/ReportsResource.java) + [SQL-Projections](./artefakte/reports_projections.sql) + [Route-Integration](./artefakte/reports_integration_snippets.tsx)

### **Phase 2: Data Science Integration (Woche 2) - JSONL + Analytics APIs**

**Goals:**
- JSONL-Streaming für Data Science Teams implementiert
- B2B-Food-spezifische KPIs operational
- ABAC-Security durchgesetzt auf Query-Level

**Key Deliverables:**
```yaml
✅ Export-Enhancement:
- UniversalExportAdapter mit JSONL-Streaming
- Memory-efficient Cursor-Pagination für Large-Datasets
- Concurrent-Export-Rate-Limiting

✅ B2B-Food-KPIs:
- Sample-Success-Rate calculation (90-Tage-Fenster)
- ROI-Pipeline mit commitment-level-Gewichtung
- Partner-vs-Direct-Channel-Mix tracking
- At-Risk-Customers Detection (60d + Email-Bounce)

✅ Security-Framework:
- ReportsQuery.java mit ABAC-enforced SQL
- ScopeContext + SecurityScopeFilter JWT-Integration
- Territory/Chain-Scoping automatisch in allen Queries
```

**Success Criteria:**
- JSONL-Export streamt 100k+ Records ohne Memory-Issues
- KPI-Calculations identisch zwischen Cockpit und Reports
- RBAC Negative-Tests grün (Sales-Rep sieht nur eigene Territory-Data)

**Artefakte:** [UniversalExportAdapter.java](./artefakte/UniversalExportAdapter.java) + [ReportsQuery.java](./artefakte/ReportsQuery.java) + [ABAC-Framework](./artefakte/SecurityScopeFilter.java)

### **Phase 3: Real-time + Performance (Woche 3) - WebSocket + Monitoring**

**Goals:**
- WebSocket Real-time Badge-Updates implementiert
- Performance-Monitoring mit Prometheus-Metrics
- Production-Deployment mit SLO-Monitoring

**Key Deliverables:**
```yaml
✅ Real-time-Updates:
- WebSocket-Topics für reports.badge.changed
- Event-Aggregator für sample.status.changed → Badge-Updates
- Polling-Fallback bei WebSocket-Connection-Failures

✅ Performance-Framework:
- Prometheus-Metrics für API-Latency + Export-Times
- Grafana-Dashboards für KPI-Staleness + Error-Rates
- k6-Load-Tests für Concurrent-User-Scenarios

✅ Production-Readiness:
- Daily Snapshot-Job (02:00 UTC) mit Error-Handling
- Circuit-Breaker für Database-Query-Timeouts
- Health-Check-Endpoints für Load-Balancer
```

**Success Criteria:**
- WebSocket Badge-Updates <5s Latency für Activity-Events
- P95 <200ms maintained unter 50 Concurrent-Users
- SLO-Achievement: <0.5% Error-Rate über 7-Tage-Window

**Artefakte:** [WebSocket-Specs](./artefakte/reports_ws_connection.md) + [Performance-Budget](./artefakte/performance_budget.md) + [Monitoring-Setup](./artefakte/reports_indexes.sql)

## ✅ Success Metrics

**Quantitative:**
- **API-Performance:** P95 <200ms für alle /api/reports/* (aktuell: N/A - nicht implementiert)
- **Export-Performance:** JSONL-Streaming 100k Records <30s (Baseline: CSV-Export ~2min)
- **Real-time-Updates:** Badge-Refresh <5s nach Domain-Event (Baseline: Manual-Refresh)
- **RBAC-Compliance:** 100% Query-Scoping für Multi-Tenant-Accounts (Security-Requirement)

**Qualitative:**
- Alle Report-Dashboard-Links führen zu funktionalen Pages
- Data Science Teams können JSONL-Exports für Advanced-Analytics nutzen
- Business-KPIs consistent zwischen Cockpit und Reports
- Export-Funktionalität für alle Gastronomiebetrieb-Report-Typen verfügbar

**B2B-Food-Specific-Metrics:**
- **Sample-Success-Rate:** Accurate tracking für Cook&Fresh® Produkttests
- **ROI-Pipeline:** Gewichtete Pipeline-Values für Commitment-Level-Assessment
- **Partner-Performance:** Channel-Mix-Tracking für Direktkunden vs. Wiederverkäufer
- **Seasonal-Opportunities:** Gastronomiebetrieb-spezifische Timing-Insights

**Timeline:**
- Phase 1: Woche 1 (API-Foundation + Route-Fixes)
- Phase 2: Woche 2 (Data Science + B2B-Food-KPIs)
- Phase 3: Woche 3 (Real-time + Production-Deployment)

## 🔗 Related Documentation

**Foundation Knowledge:**
- **Design Standards:** → [../../grundlagen/DESIGN_SYSTEM.md](../../grundlagen/DESIGN_SYSTEM.md)
- **API Standards:** → [../../grundlagen/API_STANDARDS.md](../../grundlagen/API_STANDARDS.md)
- **Coding Standards:** → [../../grundlagen/CODING_STANDARDS.md](../../grundlagen/CODING_STANDARDS.md)
- **Security Guidelines:** → [../../grundlagen/SECURITY_GUIDELINES.md](../../grundlagen/SECURITY_GUIDELINES.md)
- **Performance Standards:** → [../../grundlagen/PERFORMANCE_STANDARDS.md](../../grundlagen/PERFORMANCE_STANDARDS.md)
- **Testing Guide:** → [../../grundlagen/TESTING_GUIDE.md](../../grundlagen/TESTING_GUIDE.md)

**Technical References:**
- **Codebase-Analysis:** → [Vollständige Auswertungen Analyse](./analyse/VOLLSTÄNDIGE_AUSWERTUNGEN_CODEBASE_ANALYSE.md) - 560 Zeilen detaillierte Code-Inventory
- **Legacy-Features:** → [Legacy-Features-Validation](./analyse/LEGACY_FEATURES_VALIDIERUNG.md) - Universal Export Framework Discovery
- **API-Standards:** → [OpenAPI v1.1 Specification](./artefakte/reports_v1.1.yaml) - Complete Response-Schemas

**Implementation-Details:**
- **Code-Locations:** Backend `/reports/`, Frontend `features/auswertungen/`
- **Database-Schemas:** PostgreSQL Views + Daily-Projections + Performance-Indices
- **Integration-Points:** SalesCockpitService, CostStatistics, Universal Export Framework

**KI-Diskussion-Artefakte:**
- **Strategic-Assessment:** → [KI-Feedback Kritische Würdigung](./diskussionen/2025-09-19_KI-FEEDBACK_KRITISCHE_WUERDIGUNG.md) - 8.3/10 Business-Logic-Excellence
- **Gap-Closure:** → [Gap-Closure Finale Bewertung](./diskussionen/2025-09-19_GAP-CLOSURE_FINALE_BEWERTUNG.md) - 9.4/10 Production-Perfect-Artefakte
- **Production-Artefakte:** → [Artefakte-Sammlung](./artefakte/) - 12 Copy-Paste-Ready Implementation-Files

## 🤖 Claude Handover Section

**Für nächsten Claude:**

**Aktueller Stand:**
Strategic KI-Diskussion abgeschlossen mit 97% production-ready Artefakten. Bestehende Analytics-Infrastructure (792 LOC modern) identifiziert mit Universal Export Framework bereits vollständig implementiert. Route-Harmonisierung und API-Layer sind die einzigen Blocker für sofortigen Produktions-Einsatz.

**Nächster konkreter Schritt:**
Phase 1 Implementation starten - ReportsResource.java deployen, SQL-Views ausrollen, Route-Redirects implementieren. Alle Artefakte copy-paste-ready im /artefakte/ Ordner verfügbar mit Implementation-Guide.

**Wichtige Dateien für Context:**
- `artefakte/ReportsResource.java` - Thin Controller-Wrapper für bestehende Services
- `artefakte/reports_projections.sql` - Database-Views mit Fallback-Strategies
- `artefakte/UniversalExportAdapter.java` - JSONL-Streaming für Data Science
- `artefakte/ReportsQuery.java` - ABAC-sichere SQL-Implementations

> **🚀 MIGRATION HINWEIS für Production:**
> Bei Production-Start müssen alle Tests aus `/docs/planung/features-neu/04_auswertungen/artefakte/`
> in die neue Enterprise Test-Struktur migriert werden:
> - Unit Tests → `/backend/src/test/java/unit/reports/` bzw. `/frontend/src/tests/unit/reports/`
> - Integration Tests → `/backend/src/test/java/integration/reports/`
> - Performance Tests → `/backend/src/test/java/performance/reports/`
> Siehe [TEST_STRUCTURE_PROPOSAL.md](../../features/TEST_STRUCTURE_PROPOSAL.md) für Details.

**Offene Entscheidungen:**
- Partner-Revenue-Quelle: commission_event vs. field_values.channel_type Fallback aktivieren?
- WebSocket-Realtime-Tiefe: V1 Badge-Updates oder Full-Dashboard-Live-Updates?
- RLS vs. ABAC: Application-Level-Security oder Database-Level-Row-Security für V2?

**Produktions-Blocker (minimal):**
- JWT-Claims-Integration in SecurityScopeFilter (4-6 Stunden)
- Frontend Error-Boundaries für API-Failure-Scenarios (2-3 Stunden)
- Database-Index-Deployment mit Performance-Validation (1-2 Stunden)

**Kontext-Links:**
- **Business-Kontext:** → [CRM System Context](../../CRM_SYSTEM_CONTEXT.md) - FreshFoodz B2B-Food-Geschäftsmodell
- **Master Plan Integration:** → [CRM Master Plan V5](../../CRM_COMPLETE_MASTER_PLAN_V5.md) - Strategic Roadmap Position
- **Planungsmethodik:** → [Template-Compliance](../../PLANUNGSMETHODIK.md) - Structured Documentation-Standards