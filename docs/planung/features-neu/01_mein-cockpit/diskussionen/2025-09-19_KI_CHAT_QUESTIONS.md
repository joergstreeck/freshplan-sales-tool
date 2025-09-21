# 🤖 KI-Chat-Fragen für Modul 01 Cockpit Foundation Standards Implementation

**📅 Erstellt:** 2025-09-19
**🎯 Zweck:** Strukturierte Chat-Anfragen für externe KI zur Foundation Standards Compliance
**📊 Ziel:** Von 22% auf 100% Foundation Standards Compliance (wie Modul 03)
**📋 Basis:** COMPLIANCE_REQUEST.md + REFERENCE_BUNDLE.md

---

## 🎯 **CHAT-ANFRAGE 1: FOUNDATION STANDARDS CORE IMPLEMENTATION**

### **Eingabe-Kontext für externe KI:**

```
Ich benötige deine Hilfe bei der Foundation Standards Implementation für Modul 01 "Mein Cockpit" - das zentrale Multi-Channel Sales Dashboard für FreshFoodz B2B-Convenience-Food-Vertrieb.

**WICHTIGER KONTEXT:**
- FreshFoodz ist B2B-Convenience-Food-Hersteller (Cook&Fresh® Produktlinie)
- Multi-Channel-Vertrieb: Direct Sales + Partner Channel
- Zielgruppe: HOTEL/RESTAURANT/CATERING/BETRIEBSGASTRONOMIE
- Aktuell: 22% Foundation Standards Compliance
- Ziel: 100% Compliance (wie bereits erfolgreich in Modul 03 erreicht)

**COMPLIANCE-DOKUMENTE:**
[Hier die Inhalte von COMPLIANCE_REQUEST.md einfügen]

**FOUNDATION STANDARDS REFERENCE:**
[Hier die Inhalte von REFERENCE_BUNDLE.md einfügen]
```

### **Spezifische Anfrage an die KI:**

```
**WAS ICH VON DIR BENÖTIGE:**

1. **CORE FOUNDATION STANDARDS ARTEFAKTE (20+ Dateien):**

   **API-Specifications (OpenAPI 3.1):**
   - cockpit-dashboard.yaml (Multi-Channel KPIs + Real-time Updates)
   - multi-channel-kpis.yaml (Direct/Partner Channel Metriken)
   - roi-calculator.yaml (B2B-Food ROI-Berechnungen)
   - common-errors.yaml (RFC7807 Foundation Standards)

   **Backend-Services (Java/Quarkus mit Foundation Standards):**
   - SalesCockpitResource.java (Multi-Channel Dashboard REST-API)
   - MultiChannelKPIService.java (Channel-übergreifende Metriken)
   - ROICalculatorService.java (B2B-Food ROI-Algorithmen)
   - SecurityScopeFilter.java (ABAC Territory + Channel Scoping)

   **Frontend-Components (React/TypeScript mit Theme V2):**
   - SalesCockpitV2.tsx (3-Spalten Dashboard mit Theme V2)
   - MultiChannelPipeline.tsx (Direct/Partner Filter-Komponente)
   - ROICalculatorModal.tsx (Modal für ROI-Berechnungen)
   - theme-v2-cockpit.mui.ts (FreshFoodz CI Integration)

2. **SPEZIFISCHE COCKPIT-REQUIREMENTS:**

   **3-Spalten-Dashboard-Architektur:**
   - Spalte 1: Genussberater-Tag (Sample-Tests, ROI-Termine, FreshFoodz-KPIs)
   - Spalte 2: Multi-Channel-Pipeline (Direct/Partner-Filter, Account-Übersicht)
   - Spalte 3: Account-Intelligence (ROI-Calculator, Channel-Info, Quick Actions)

   **Performance-Kritische Features:**
   - Dashboard-KPIs: Real-time Updates für Sales-Performance
   - Multi-Channel-Filter: <100ms für Channel-switching
   - ROI-Calculator: <500ms für Standard-Kalkulationen

**FOUNDATION STANDARDS FOCUS:**
- ABAC Security: Territory + Channel-based Scoping via JWT Claims
- FreshFoodz CI: #94C456 (Primary), #004F7B (Secondary), Antonio Bold, Poppins
- Package Structure: de.freshplan.cockpit.* durchgängig
- API Standards: OpenAPI 3.1 + JavaDoc Foundation References
- Testing: BDD-Pattern mit Given-When-Then

**OUTPUT-FORMAT:**
Bitte erstelle production-ready Code-Dateien mit:
- Vollständige Implementierung (copy-paste-ready)
- Foundation Standards Compliance Markierungen
- Ausführliche JavaDoc/JSDoc mit Foundation References
- BDD-Tests für kritische Business-Logic

Beginne mit den API-Specifications und Backend-Services.
```

---

## 🎯 **CHAT-ANFRAGE 2: ADVANCED FEATURES + BUSINESS INTEGRATION**

### **Eingabe-Kontext für externe KI:**

```
FORTSETZUNG: Foundation Standards Implementation für Modul 01 Cockpit

Du hast bereits die Core-Artefakte (API-Specs, Backend-Services, Frontend-Components) erstellt.
Jetzt benötige ich die erweiterten Features und Business-Integration.

**AKTUELLE BASIS:**
- Core Foundation Standards Artefakte implementiert
- Multi-Channel Dashboard Grundstruktur vorhanden
- ABAC Security + Theme V2 Integration etabliert
```

### **Spezifische Anfrage an die KI:**

```
**WAS ICH JETZT BENÖTIGE (20+ zusätzliche Dateien):**

1. **SQL-SCHEMAS (PostgreSQL mit Foundation Standards):**
   - cockpit_kpis_projection.sql (Performance-optimierte KPI-Views)
   - multi_channel_views.sql (Direct/Partner Channel Aggregationen)
   - roi_calculations.sql (B2B-Food ROI-Formeln als Stored Functions)
   - RLS Policies für Territory + Channel-based Security

2. **TESTING-SUITE (BDD + Security + Performance):**
   - SalesCockpitServiceTest.java (BDD-Tests für Dashboard-Logic)
   - ROICalculatorTest.java (B2B-Food ROI-Algorithmus-Tests)
   - MultiChannelABACIT.java (ABAC Integration-Tests für Channel-Security)
   - coverage-config.xml (80%+ Coverage-Gates)

3. **CI/CD + MONITORING:**
   - github-actions-cockpit.yml (GitHub Actions Pipeline)
   - compliance_matrix.md (Foundation Standards Compliance-Status)
   - performance_budget.md (Dashboard-Performance SLOs)
   - multi_channel_security.md (ABAC-Dokumentation)

4. **B2B-FOOD BUSINESS-LOGIC:**

   **ROI-Calculator-Spezifika:**
   - Convenience-Food vs. Eigenproduktion Kalkulationen
   - Cook&Fresh® Produktkosten-Matrices
   - Arbeitszeit-Einsparungen in HOTEL/RESTAURANT/CATERING
   - Lagerkosten-Optimierungen durch Convenience-Food

   **Multi-Channel-KPIs:**
   - Direct Sales: Sample-Test → ROI-Demo → Abschluss Funnel
   - Partner Channel: Channel-Partner Performance + Commission Tracking
   - Cross-Channel: Account-Overlap-Detection + Channel-Conflict-Resolution

5. **INTEGRATION-DEPENDENCIES:**
   - 02_neukundengewinnung: Lead-Status-Display (Read-Only)
   - 03_kundenmanagement: Account-Detail-Sync
   - SmartLayout-System: Content-Type-Detection für V3-Migration

**BUSINESS-KONTEXT VERTIEFUNG:**
- Sample-Test-Workflow: Cook&Fresh® Produktproben → Feedback → ROI-Beratung
- Genussberater-Tagesplanung: Territory-basierte Termine + Sample-Koordination
- Channel-Management: Partner-Performance vs. Direct-Sales-Effizienz

**QUALITY REQUIREMENTS:**
- Performance: P95 <200ms für alle Dashboard-APIs
- Security: Territory + Channel RLS Policies
- Testing: 80%+ Coverage mit BDD-Pattern
- Monitoring: Real-time KPI-Updates + SLO-Alerting

**OUTPUT-FORMAT:**
Production-ready Artefakte mit:
- Vollständige B2B-Food Business-Logic
- Foundation Standards Compliance
- Integration-ready für Module 02/03
- Enterprise-Grade Security + Performance
```

---

## 🎯 **WAS DIE KI ÄNDERN/ERSTELLEN SOLL:**

### **1. FOUNDATION STANDARDS COMPLIANCE VON 22% AUF 100%:**

| Standard | IST | SOLL | KI-Aufgabe |
|----------|-----|------|------------|
| **Design System V2** | 10% | 100% | FreshFoodz CI + Theme V2 in allen Components |
| **API Standards** | 20% | 100% | OpenAPI 3.1 + JavaDoc Foundation Refs |
| **Security ABAC** | 15% | 100% | Territory + Channel-Scoping RLS Policies |
| **Testing Standards** | 5% | 100% | BDD-Tests + 80%+ Coverage |
| **Package Structure** | 30% | 100% | de.freshplan.cockpit.* durchgängig |
| **Performance Standards** | 70% | 100% | P95 <200ms + SLO-Monitoring |
| **SQL Standards** | 0% | 100% | RLS Policies + Strategic Indices |

### **2. SPEZIFISCHE ÄNDERUNGEN/ERGÄNZUNGEN:**

**NEUE ARTEFAKTE ERSTELLEN (40+ Dateien):**
- Komplett neue `/artefakte/` Struktur (aktuell 0 Artefakte!)
- 8 Kategorien: API/Backend/Frontend/SQL/Testing/CI/Docs/Performance
- Production-ready Implementation

**BUSINESS-LOGIC IMPLEMENTIEREN:**
- Multi-Channel B2B Sales Dashboard (Direct + Partner)
- ROI-Calculator für B2B-Food-Industrie
- 3-Spalten-Architektur mit Real-time Updates
- Territory + Channel-based ABAC Security

**INTEGRATION VORBEREITEN:**
- Module 02/03 Dependencies definieren
- SmartLayout V3-Migration Pilot
- Real-time Dashboard-Updates via WebSocket

### **3. SUCCESS CRITERIA FÜR DIE KI:**

**DELIVERABLES:**
- **40+ Production-Ready Artefakte** (wie Modul 03: 39 Artefakte)
- **8 Kategorien:** API/Backend/Frontend/SQL/Testing/CI/Docs/Performance
- **100% Foundation Standards Compliance**
- **Copy-Paste-Ready Implementation**

**QUALITY GATES:**
- Alle Services mit JavaDoc Foundation References
- BDD-Tests mit Given-When-Then Pattern
- FreshFoodz CI durchgängig (#94C456, #004F7B)
- P95 <200ms Performance-Budget
- ABAC Territory + Channel Security

**BUSINESS ALIGNMENT:**
- B2B-Convenience-Food Sales-Prozesse
- Cook&Fresh® Sample-Management Integration
- Multi-Channel Conflict-Resolution
- ROI-Demonstration für Gastronomiebetriebe

---

## 🚀 **IMPLEMENTIERUNGS-STRATEGIE:**

### **Phasen-Aufteilung:**

**Chat 1 (Core Foundation):**
- API-Specifications + Backend-Services
- Frontend-Components + Theme V2
- Grundlegende ABAC Security

**Chat 2 (Advanced Features):**
- SQL-Schemas + Performance-Optimization
- Testing-Suite + CI/CD Pipeline
- B2B-Food Business-Logic + Integration

### **Erfolgs-Indikator:**
Nach beiden Chat-Sessions sollte Modul 01 Cockpit die gleiche **100% Foundation Standards Compliance** erreichen wie Modul 03.

**🎯 Ziel:** Enterprise-Grade Multi-Channel Sales Dashboard mit Foundation Standards Excellence!