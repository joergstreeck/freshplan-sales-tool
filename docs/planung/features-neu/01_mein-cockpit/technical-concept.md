# Mein Cockpit - Technical Concept

**📊 Plan Status:** 🔵 Draft
**🎯 Owner:** Development Team
**⏱️ Timeline:** Q4 2025 → Q1 2026
**🔧 Effort:** M

## 🎯 Executive Summary (für Claude)

**Mission:** Entwicklung eines Multi-Channel-B2B-Sales-Dashboards für FreshFoodz Cook&Fresh® Vertrieb mit Channel-aware Account-Übersicht und ROI-Beratungstools
**Problem:** Aktuelles Cockpit ist generisches CRM-Dashboard ohne FreshFoodz-spezifische Multi-Channel-Account-Darstellung und ROI-Beratungsunterstützung
**Solution:** 3-Spalten-SmartLayout-Dashboard mit Channel-aware Account-Pipeline, FreshFoodz-Insights und integriertem ROI-Calculator-Modal
**Timeline:** 3-4 Monate schrittweise Entwicklung mit SmartLayout-Pilot
**Impact:** Channel-aware Account-Darstellung, 3x schnellere ROI-Demonstrationen, zentrale Genussberater-Schaltzentrale

**Quick Context:** Das Sales-Cockpit wird zum zentralen Dashboard für FreshFoodz-Genussberater mit Multi-Channel-Account-Übersicht, FreshFoodz-spezifischen KPIs und ROI-Calculator-Integration. Lead-Anmeldung erfolgt in separatem 02_neukundengewinnung/lead-erfassung Modul.

## 📋 Context & Dependencies

### Current State - Code-Analyse 18.09.2025:

#### ✅ **BEREITS IMPLEMENTIERT (Production-Ready):**
- **SalesCockpitV2.tsx:** 3-Spalten MUI-Layout vollständig funktional auf MainLayoutV2
- **ResizablePanels:** Professional drag-and-drop mit localStorage-Persistierung
- **Backend CQRS:** SalesCockpitQueryService migriert, 19/19 Tests grün, Performance-optimiert
- **Lazy Loading:** Suspense + ColumnSkeleton für alle 3 Spalten implementiert
- **Dashboard-KPIs:** Kunden/Aktive/Risiko/Überfällig bereits funktional
- **Industry-Enum:** HOTEL, RESTAURANT, CATERING, KANTINE = 1:1 FreshFoodz-Match
- **Hook-Integration:** useDashboardData + useAuth vollständig integriert

#### ❌ **FEHLENDE FreshFoodz-DOMÄNE:**
- **Generisches CRM:** Keine Cook&Fresh®-spezifischen KPIs oder Workflows
- **Multi-Channel:** Keine ChannelType (DIRECT/PARTNER) Unterscheidung
- **ROI-Calculator:** Komplett fehlend (Legacy Calculator unbrauchbar)
- **Sample-Management:** Keine Integration für Produktproben-Workflows
- **Channel-Conflicts:** Keine Lead-Anmeldung-Koordination zwischen Direct/Partner

### Target State:
- Multi-Channel-aware Sales-Dashboard mit Account-Übersicht nach Channel-Type
- FreshFoodz-spezifische Dashboard-KPIs und Account-Insights
- SmartLayout-Integration als Pilot für V3-Migration
- ROI-Calculator-Modal für Gastronomy-Beratung (Phase 2)
- Channel-aware Account-Darstellung mit Partner-Assignment-Info
- Integration mit 02_neukundengewinnung/lead-erfassung (Lead-Daten anzeigen) und 03_kundenmanagement

### Dependencies:
- **SmartLayout Migration:** → [../infrastruktur/SMARTLAYOUT_MIGRATION_PLAN.md](../infrastruktur/SMARTLAYOUT_MIGRATION_PLAN.md)
- **03_kundenmanagement/alle-kunden:** Account-APIs mit Channel-Information
- **02_neukundengewinnung/lead-erfassung:** Lead-Status-APIs für Dashboard-Integration
- **Component Library V3:** → [../infrastruktur/COMPONENT_LIBRARY_V3_MIGRATION_PLAN.md](../infrastruktur/COMPONENT_LIBRARY_V3_MIGRATION_PLAN.md)

## 🛠️ Implementation Phases

### Phase 1: Multi-Channel Foundation (2-3 Wochen) ⚡ Verkürzt!

**Goal:** FreshFoodz-spezifisches 3-Spalten-Dashboard mit Channel-aware Account-Darstellung

**✅ Bereits vorhanden (Code-Analyse):**
- 3-Spalten-Layout mit ResizablePanels (Production-Ready)
- MUI-Integration vollständig (MyDayColumnMUI, FocusListColumnMUI, ActionCenterColumnMUI)
- Backend CQRS bereits optimiert (SalesCockpitQueryService)
- Dashboard-Statistiken funktional (Header-KPIs)

**🔧 Noch zu entwickeln - FreshFoodz-spezifische Erweiterungen:**

#### **Sofort umsetzbar (Phase 1):**
```typescript
// Spalte 1: "Genussberater-Tag" - FreshFoodz-Dashboard-KPIs
interface GenussberaterDay {
  sampleFeedbacks: SampleTestResult[];     // 🔧 Priorität 1
  roiTermineHeute: ROIConsultation[];      // 🔧 Priorität 1
  newLeadsFromLeadErfassung: LeadSummary[]; // 🔧 Integration zu 02_neukundengewinnung
}

// Spalte 2: Multi-Channel-Pipeline
interface ChannelAwarePipeline {
  channelFilter: "all" | "direct" | "partner";  // 🔧 Priorität 1
  businessTypeFilter: Industry[];               // ✅ Bereits vorhanden
  channelConflictAlerts: ConflictAlert[];       // 🔧 Priorität 2
}

// Spalte 3: Account-Intelligence + ROI-Tools
interface AccountIntelligence {
  roiCalculatorModal: ROICalculatorModal;         // 🔧 Priorität 1 (Phase 2)
  channelInfo: ChannelAssignment;                 // 🔧 Priorität 1
  cookAndFreshAffinityScore: number;              // 🔧 Priorität 1
}
```

#### **🔮 Zukunftsfeatures (siehe /zukunft/):**
- **seasonalOpportunities:** Seasonal Lead Detection (Business-Logik zu definieren)
- **sampleHistory:** Vollständige Sample-Management-Integration
- **advancedROIFeatures:** Erweiterte ROI-Kalkulationen mit Pooling-Benefits
- **partnerPerformanceTracking:** Partner-Channel-Performance-Dashboards

**🔧 Backend Extensions (NEU zu entwickeln):**
```java
// ✅ Customer Entity erweitern (bereits vorhanden, nur ChannelType hinzufügen)
@Entity Customer {
  // ✅ Industry enum bereits FreshFoodz-kompatibel (HOTEL, RESTAURANT, CATERING, KANTINE)

  @Enumerated(EnumType.STRING)
  private ChannelType channelType; // 🔧 NEU: DIRECT, PARTNER

  @Column(name = "partner_assignment")
  private String partnerAssignment; // 🔧 NEU: Referenz zu Partner

  @Column(name = "cookfresh_affinity_score")
  private Integer cookFreshAffinityScore; // 🔧 NEU: FreshFoodz-Score 1-100
}

// ✅ SalesCockpitQueryService erweitern (bereits Production-Ready)
@ApplicationScoped
public class SalesCockpitQueryService {
  // ✅ Bereits implementiert: getDashboardData(), getDevDashboardData()

  // 🔧 NEU hinzufügen:
  public FreshFoodzKPIs getFreshFoodzDashboardKPIs(UUID userId);
  public List<ChannelAccount> getChannelAwareAccounts(ChannelFilter filter);
  public List<SampleTestSummary> getTodaysSampleTests(UUID userId);
}
```

**✅ Success Criteria (angepasst an Cockpit-Fokus):**
- ✅ 3-Spalten-Layout funktional (bereits erreicht)
- 🔧 Multi-Channel-KPIs in Header (Sample-Tests, ROI-Termine statt generisch)
- 🔧 FreshFoodz-Genussberater-Dashboard zeigt Cook&Fresh®-spezifische Daten
- 🔧 Channel-Filter in FocusListColumnMUI für Direct/Partner-Accounts

**Next:** → [Phase 2](#phase-2)

### Phase 2: SmartLayout-Pilot + ROI-Foundation (2 Wochen) ⚡ Verkürzt!

**Goal:** Cockpit als SmartLayout-Showcase + ROI-Calculator-Modal-Basis

**✅ Bereits vorhanden (Code-Analyse):**
- ResizablePanels bereits Professional-Level (react-resizable-panels)
- MUI-Integration vollständig (keine Migration erforderlich)
- Lazy Loading mit Suspense bereits implementiert

**SmartLayout Integration:**
```typescript
// Cockpit als Content-Type-Pilot
<SmartLayout forceWidth="full"> {/* Container */}
  <CockpitGrid>
    <GenussberaterTag contentType="narrow" />     {/* Cards/Widgets */}
    <MultiChannelPipeline contentType="full" />   {/* DataTable */}
    <AccountIntelligence contentType="form" />    {/* Detail-Forms */}
  </CockpitGrid>
</SmartLayout>
```

**ROI-Calculator-Modal (Foundation):**
```typescript
// Modal-Integration in Spalte 3
<ActionCenterColumn>
  <AccountProfile />
  <QuickActions>
    <CreateROICalculation
      onClick={() => openROIModal(selectedAccount)}
      disabled={!account.businessType}
    />
    <ConfigureSampleBox />
    <CreateOffer />
  </QuickActions>

  <ROICalculatorModal
    open={roiModalOpen}
    account={selectedAccount}
    onCalculationComplete={handleROIResult}
    onSave={saveROIScenario}
  />
</ActionCenterColumn>
```

**ROI-Calculator-MVP:**
```typescript
interface ROICalculatorInput {
  businessType: Industry;
  guestsPerDay: number;
  currentStaffingCosts: Money;
  currentWastePercentage: number;
  targetProductSelection: CookAndFreshProduct[];
}

interface ROICalculationResult {
  laborSavings: { monthly: Money, explanation: string };
  wasteSavings: { monthly: Money, explanation: string };
  qualityImprovements: QualityScore[];
  totalROI: { monthly: Money, breakEvenMonths: number };
  recommendedProducts: CookAndFreshProduct[];
}
```

**Success Criteria:**
- SmartLayout Content-Type-Detection funktional
- ROI-Calculator-Modal öffnet mit Account-Kontext
- Basis-ROI-Berechnung mit 3-4 Kern-Parametern
- ROI-Szenarien speicherbar an Account

**Next:** → [Phase 3](#phase-3)

### Phase 3: Advanced Dashboard-Features + ROI-Verfeinerung (Woche 9-12)

**Goal:** Erweiterte Dashboard-KPIs + Channel-Intelligence + ROI-Calculator-Perfektion

**Dashboard-Intelligence-Features:**
```typescript
// Channel-Performance-Tracking im Dashboard
interface ChannelPerformanceMetrics {
  directChannelKPIs: {
    conversionRate: number;
    avgDealSize: Money;
    timeToClose: number;
  };
  partnerChannelKPIs: {
    partnerCount: number;
    partnerPerformance: PartnerMetric[];
    channelConflicts: ConflictSummary[];
  };
}

// Integration zu 02_neukundengewinnung für Lead-Status-Anzeige
interface LeadStatusDisplay {
  newLeadsToday: LeadSummary[];
  conflictAlerts: ConflictAlert[]; // Nur Anzeige, nicht Bearbeitung
  leadSourceMetrics: SourcePerformance[];
}
```

**ROI-Calculator-Verfeinerung:**
```typescript
// Erweiterte ROI-Features (nach Jörgs Kalkulation)
interface EnhancedROICalculation {
  productionEfficiencies: PoolingBenefit[];
  volumeDiscounts: VolumeScaling[];
  procurementOptimization: ProcurementSaving[];
  seasonalFactors: SeasonalAdjustment[];
}
```

**Success Criteria:**
- Channel-Performance-Tracking vollständig funktional
- Lead-Status-Integration aus 02_neukundengewinnung sichtbar
- Erweiterte ROI-Parameter verfügbar
- Partner-Performance-Anzeige in Dashboard

**Next:** → [Production Ready](#production-ready)

### Phase 4: Production-Ready + Performance-Optimization (Woche 13-16)

**Goal:** P95-Performance-Targets + vollständige Feature-Integration

**Performance-Optimizations:**
- React Query Cache-Optimierung für Multi-Channel-Daten
- Virtualized Lists für große Account-Pipelines
- WebSocket-Integration für Dashboard-Updates bei neuen Leads
- ROI-Calculation-Caching für wiederholte Szenarien

**Integration-Completion:**
- 02_neukundengewinnung Lead-Status-Display (Read-Only)
- 03_kundenmanagement Account-Detail-Sync
- ROI-Calculator-Results in Timeline-Integration
- Channel-Performance-KPIs in Header

**Success Criteria:**
- P95 Page Load <2s, API Response <200ms
- Multi-Channel-Workflows vollständig getestet
- ROI-Calculator production-ready
- SmartLayout-Pilot erfolgreich evaluiert

## ✅ Success Metrics

**Quantitative:**
- **Page Load Time:** <2s P95 (aktuell 3-4s bei komplexen Dashboards)
- **API Response Time:** <200ms P95 für /cockpit/summary
- **Dashboard Load Time:** <1s für Multi-Channel-KPI-Updates
- **ROI Calculation Time:** <500ms für Standard-Szenarien (synchron)

**Qualitative:**
- Multi-Channel-Dashboard zeigt alle relevanten KPIs übersichtlich
- Genussberater können ROI-Szenarien in <2 Minuten erstellen
- Channel-Filter reduzieren Pipeline-Noise um >50%
- SmartLayout Content-Detection funktioniert für alle 3 Content-Types

**Business-Impact:**
- Lead-zu-Customer-Conversion durch ROI-Demonstration +30%
- Channel-Übersicht reduziert Informationssuche -80%
- Genussberater-Produktivität durch zentrale Tools +25%

**Timeline (basierend auf Code-Analyse):**
- Phase 1: Ende Q4 2025 (Multi-Channel-Foundation) - 2-3 Wochen ⚡
- Phase 2: Anfang Q1 2026 (SmartLayout-Pilot + ROI-Basis) - 2 Wochen ⚡
- Phase 3: Mitte Q1 2026 (Advanced Dashboard-Features) - 1-2 Wochen ⚡
- Phase 4: Ende Q1 2026 (Production-Ready) - 1 Woche ⚡

**Gesamtaufwand:** 6-8 Wochen (statt 12 Wochen) - 50% Einsparung durch solide Basis!

## 📁 Implementation Artifacts

### **🎯 Foundation Standards Compliant Artefakte (44 Dateien)**

**✅ Vollständige Implementierung verfügbar unter:** `./artefakte/`

#### **API Specifications (6 Dateien)**
- `./artefakte/api/cockpit_summary_spec_v1.1.yaml` - Dashboard KPI APIs
- `./artefakte/api/roi_calculator_spec_v1.1.yaml` - ROI Calculator APIs mit Investment-Field
- `./artefakte/api/sample_management_spec_v1.0.yaml` - Sample-Test APIs
- `./artefakte/api/account_insights_spec_v1.0.yaml` - Account-Intelligence APIs
- `./artefakte/api/channel_filter_spec_v1.0.yaml` - Multi-Channel Filter APIs
- `./artefakte/api/dashboard_metrics_spec_v1.0.yaml` - Performance Metrics APIs

#### **Backend Implementation (12 Dateien)**
- `./artefakte/backend/CockpitDTO.java` - Type-safe Dashboard DTOs
- `./artefakte/backend/ROIDTO.java` - ROI Calculator DTOs mit Investment-Support
- `./artefakte/backend/ROIResource.java` - Foundation Standards REST API
- `./artefakte/backend/ROIService.java` - Business Logic mit paybackMonths-Berechnung
- `./artefakte/backend/CockpitRepository.java` - ABAC-Security Database Access
- `./artefakte/backend/CockpitResource.java` - Dashboard REST APIs
- `./artefakte/backend/SampleTestDTO.java` - Sample Management DTOs
- `./artefakte/backend/ChannelType.java` - Multi-Channel Enum
- `./artefakte/backend/AccountInsightsDTO.java` - Account Intelligence
- `./artefakte/backend/DashboardMetricsDTO.java` - Performance KPIs
- `./artefakte/backend/CockpitSecurityFilter.java` - ABAC Territory/Channel Security
- `./artefakte/backend/CustomerExtended.java` - Customer Entity mit Channel-Support

#### **Frontend Components (15 Dateien)**
- `./artefakte/frontend/CockpitDashboard.tsx` - Main Dashboard Component
- `./artefakte/frontend/ROICalculatorModal.tsx` - ROI Calculator UI
- `./artefakte/frontend/ChannelFilterComponent.tsx` - Multi-Channel Filter
- `./artefakte/frontend/useCockpitData.ts` - Dashboard Data Hook (echte React Hooks!)
- `./artefakte/frontend/useROICalculator.ts` - ROI Calculator Hook
- `./artefakte/frontend/CockpitKPICards.tsx` - KPI Dashboard Cards
- `./artefakte/frontend/SampleTestsWidget.tsx` - Sample Management Widget
- `./artefakte/frontend/AccountInsightsPanel.tsx` - Account Intelligence Panel
- `./artefakte/frontend/ChannelPipelineView.tsx` - Multi-Channel Pipeline
- `./artefakte/frontend/DashboardMetrics.tsx` - Performance Metrics Display
- `./artefakte/frontend/cockpit.types.ts` - TypeScript Interfaces
- `./artefakte/frontend/roi-calculator.types.ts` - ROI TypeScript Types
- `./artefakte/frontend/CockpitLayout.tsx` - SmartLayout Integration
- `./artefakte/frontend/ErrorBoundary.tsx` - Error Handling
- `./artefakte/frontend/LoadingStates.tsx` - Loading/Skeleton Components

#### **Database Schema (1 Datei)**
- `./artefakte/sql/cockpit_schema_v2.sql` - PostgreSQL Schema mit RLS-Policies

#### **Testing (4 Dateien)**
- `./artefakte/testing/CockpitTest.java` - Backend Unit Tests
- `./artefakte/testing/CockpitDashboard.test.tsx` - React Component Tests
- `./artefakte/testing/roi_calculator_integration.test.js` - Integration Tests
- `./artefakte/testing/cockpit_performance_test.js` - k6 Performance Tests

> **🚀 MIGRATION HINWEIS für Production:**
> Bei Production-Start müssen alle Tests aus `/docs/planung/features-neu/01_mein-cockpit/artefakte/testing/`
> in die neue Enterprise Test-Struktur migriert werden:
> - Unit Tests → `/backend/src/test/java/unit/cockpit/` bzw. `/frontend/src/tests/unit/cockpit/`
> - Integration Tests → `/backend/src/test/java/integration/cockpit/` bzw. `/frontend/src/tests/integration/cockpit/`
> - Performance Tests → `/backend/src/test/java/performance/cockpit/`
> Siehe [TEST_STRUCTURE_PROPOSAL.md](../../features/TEST_STRUCTURE_PROPOSAL.md) für Details.

#### **CI/CD (1 Datei)**
- `./artefakte/ci-cd/cockpit-deployment.yml` - GitHub Actions Workflow

#### **Documentation (4 Dateien)**
- `./artefakte/docs/API_DOCUMENTATION.md` - API Dokumentation
- `./artefakte/docs/COMPONENT_GUIDE.md` - Frontend Component Guide
- `./artefakte/docs/TESTING_STRATEGY.md` - Testing Documentation
- `./artefakte/docs/DEPLOYMENT_GUIDE.md` - Deployment Instructions

#### **Status (1 Datei)**
- `./artefakte/README_100_PROZENT_KOMPLETT.md` - **100% Foundation Standards Compliance!**

### **🎯 Foundation Standards Compliance: 100%**
- ✅ **Design System V2:** FreshFoodz CI (#94C456, #004F7B, Antonio Bold)
- ✅ **API Standards:** v1.1 konsistent mit Investment-Field Support
- ✅ **Security ABAC:** Territory + Channel Row-Level-Security
- ✅ **Backend Architecture:** Clean CQRS + Repository Pattern
- ✅ **Frontend Integration:** React Hooks + TypeScript Type-Safety
- ✅ **SQL Standards:** PostgreSQL RLS-Policies implementiert
- ✅ **Testing Standards:** Unit + Integration + Performance Tests
- ✅ **CI/CD Standards:** GitHub Actions Deployment Pipeline

## 🔗 Related Documentation

**Foundation Knowledge:**
- **Design Standards:** → [../../grundlagen/DESIGN_SYSTEM.md](../../grundlagen/DESIGN_SYSTEM.md)
- **Multi-Channel-Konzept:** → [../../CRM_AI_CONTEXT_SCHNELL.md](../../CRM_AI_CONTEXT_SCHNELL.md)
- **Performance Standards:** → [../../grundlagen/PERFORMANCE_STANDARDS.md](../../grundlagen/PERFORMANCE_STANDARDS.md)

**Infrastructure Dependencies:**
- **SmartLayout Migration:** → [../../infrastruktur/SMARTLAYOUT_MIGRATION_PLAN.md](../../infrastruktur/SMARTLAYOUT_MIGRATION_PLAN.md)
- **Component Library V3:** → [../../infrastruktur/COMPONENT_LIBRARY_V3_MIGRATION_PLAN.md](../../infrastruktur/COMPONENT_LIBRARY_V3_MIGRATION_PLAN.md)

**Feature Integrations:**
- **Neukundengewinnung:** → [../../features-neu/02_neukundengewinnung/](../../features-neu/02_neukundengewinnung/)
- **Kundenmanagement:** → [../../features-neu/03_kundenmanagement/](../../features-neu/03_kundenmanagement/)

**Technical Implementation:**
- **API Contracts:** Backend SalesCockpitResource bereits vorhanden
- **Frontend Components:** SalesCockpitV2.tsx als Basis-Implementation
- **Data Models:** Industry-Enum bereits FreshFoodz-kompatibel

## 🤖 Claude Handover Section

**Für nächsten Claude:**

**Aktueller Stand:**
Technical Concept für Multi-Channel-B2B-Cockpit erstellt mit 4-Phasen-Roadmap. SmartLayout-Pilot-Strategie definiert, Channel-aware Dashboard-System konzipiert, ROI-Calculator-Integration geplant. Code-Analyse bestätigt solide technische Basis.

**Nächster konkreter Schritt:**
Phase 1 starten - FreshFoodz-Domain-Transformation. Customer-Entity um ChannelType erweitern, MyDayColumnMUI für Cook&Fresh®-KPIs anpassen, FocusListColumnMUI um Channel-Filter ergänzen.

**Basis bereits da:**
✅ SalesCockpitV2.tsx Production-Ready
✅ Backend CQRS optimiert
✅ Alle Infrastruktur-Komponenten funktional
→ Fokus auf FreshFoodz-Geschäftslogik!

**Wichtige Dateien für Context:**
- `SalesCockpitV2.tsx` - Basis-Implementation mit 3-Spalten-Layout
- `Customer.java` - Entity für Channel-Erweiterung
- `Industry.java` - Bereits FreshFoodz-kompatible Enum-Werte
- `SMARTLAYOUT_MIGRATION_PLAN.md` - Parallel laufende Infrastructure-Migration

**Offene Entscheidungen:**
- ROI-Calculator-Komplexität basierend auf Jörgs aktueller Kalkulation
- SmartLayout-Pilot-Timing mit Infrastructure-Team koordinieren
- Dashboard-Integration-Details für 02_neukundengewinnung und 03_kundenmanagement
- WebSocket vs. Polling für Dashboard-Updates bei neuen Leads

**Kontext-Links:**
- **Grundlagen:** → [PLANUNGSMETHODIK.md](../../PLANUNGSMETHODIK.md)
- **Dependencies:** → [Infrastructure Master Index](../../infrastruktur/00_MASTER_INDEX.md)
- **Business Context:** → [CRM_AI_CONTEXT_SCHNELL.md](../../CRM_AI_CONTEXT_SCHNELL.md)