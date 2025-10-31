# 🚀 Sprint 2.1.7.2 - Customer-Management + Xentral-Integration

**Sprint-ID:** 2.1.7.2
**Status:** 📋 PLANNING → 🚀 READY TO START
**Priority:** P1 (High)
**Estimated Effort:** 25h (3+ Arbeitstage)
**Owner:** TBD
**Created:** 2025-10-16
**Updated:** 2025-10-24 (XentralApiConfig Architecture Decision dokumentiert)
**Dependencies:** Sprint 2.1.7.1 COMPLETE, Sprint 2.1.7.4 COMPLETE

---

## 🎯 SPRINT GOALS

### **Business Value**

**Vertriebler können Kunden managen mit ECHTEN Umsatzdaten aus Xentral:**

- ✅ Opportunity → Customer konvertieren (mit Xentral-Verknüpfung)
- ✅ Customer-Dashboard mit Live-Umsatzdaten (30/90/365 Tage)
- ✅ Zahlungsverhalten-Überblick (Ampel-System, keine Detail-Rechnungen)
- ✅ Churn-Alarm (variabel pro Kunde konfigurierbar)
- ✅ Xentral-Kunden-Dropdown (verkäufer-gefiltert, kein manuelles Tippen!)
- ✅ Automatische PROSPECT → AKTIV Aktivierung (via Xentral Webhook)

**Business Impact:**
- Verkäufer sehen sofort: "Läuft der Kunde gut?" (Umsatz + Zahlungsmoral)
- Churn-Prevention: Frühwarnung bei Inaktivität (individuell einstellbar)
- Provision-Transparenz: Umsatz ≠ Provision (klar getrennt!)
- Innendienst entlastet: Verkäufer haben Self-Service-Dashboard
- Automatische Aktivierung: Keine manuelle Arbeit mehr!

### **Technical Context**

**Xentral-Integration ist FERTIG dokumentiert (Modul 08):**
- ✅ FC-005: Xentral Invoice/Payment APIs (840 Zeilen Doku)
- ✅ FC-009: Xentral Contract Management APIs (311 Zeilen Doku)
- ✅ Sales-Rep Mapping: Dokumentiert in FC-005
- ✅ Xentral läuft BEREITS (Production-Ready)

**Backend existiert bereits:**
- ✅ V261: `customers.original_lead_id` (Lead → Customer Traceability)
- ✅ OpportunityService.convertToCustomer() (Backend fertig!)
- ✅ REST API: POST /api/opportunities/{id}/convert-to-customer

**Status-Architektur (Sprint 2.1.7.4):**
- ✅ CustomerStatus: PROSPECT → AKTIV → RISIKO → INAKTIV → ARCHIVIERT
- ✅ Lead → Customer setzt **PROSPECT** Status (nicht AKTIV!)
- ✅ PROSPECT → AKTIV Trigger: Erste Bestellung geliefert
- ✅ XentralOrderEventHandler Interface (für Webhook-Integration)
- ✅ ChurnDetectionService mit Seasonal Business Support

---

## 📦 DELIVERABLES

### **1. Opportunity → Customer Conversion** (4h)

**Ziel:** Vertriebler kann Opportunity CLOSED_WON → Customer konvertieren mit Xentral-Verknüpfung

**Tasks:**
- [x] ConvertToCustomerDialog Component (MUI Dialog)
- [x] Xentral-Kunden-Dropdown (Autocomplete, verkäufer-gefiltert)
- [x] API Integration: GET /api/xentral/customers?salesRepId={id}
- [x] API Integration: POST /api/opportunities/{id}/convert-to-customer
- [x] PROSPECT Status Info-Box (Sprint 2.1.7.4 Integration)
- [x] Success-Notification + Navigation

**Tests:** 8 Tests (5 Component + 3 Integration)

---

### **2. Xentral-API-Client Implementation** (3h)

**Ziel:** XentralApiClient für Umsatz- und Zahlungsdaten

**⚡ WICHTIG:** Verwendet **Neue Xentral API (v25.39+)** - NICHT Legacy v1 REST API!

**🚨 KRITISCH:** **READ-ONLY Integration** (User-Requirement!)
- ❌ **KEINE** POST/PUT/PATCH/DELETE Operations auf Xentral!
- ✅ **NUR** GET-Requests (Daten lesen)
- Grund: PAT hat WRITE-Rechte, Xentral kann nicht einschränken
- Schutz: 5-Layer Security (Code + Hook + Review + Tests + Doku)

**Tasks:**
- [x] XentralApiClient Service (Quarkus REST Client + JSON:API)
- [x] GET /api/customers (filter by salesRep.id) - JSON:API Format
- [x] GET /api/customers/{id} (includes financial data - 2025 Feature!)
- [x] GET /api/invoices (filter by customer.id) - JSON:API Format
- [x] GET /api/employees (filter by role=sales) - JSON:API Format
- [x] JSON:API Response Parsing (meta, data, links → Simple DTOs)
- [x] Personal Access Token (PAT) Authentication
- [x] Feature-Flag: mock-mode=true (Hybrid-Ansatz)
- [x] Error Handling (Xentral down → Fallback)
- [x] **Security Guardrails: READ-ONLY Enforcement** (5 Layers)

**Tests:** 10 Tests (Mock + Integration)

---

### **3. Customer-Dashboard mit echten Daten** (6h)

**Ziel:** Customer-Dashboard zeigt Live-Umsatzdaten aus Xentral

**Backend:**
- [x] CustomerService.getRevenueMetrics() (30/90/365 Tage)
- [x] GET /api/customers/{id}/revenue-metrics
- [x] PaymentBehaviorCalculation (Ampel-System)

**Frontend:**
- [x] CustomerDetailPage erweitern (Revenue Cards)
- [x] RevenueMetricsWidget Component
- [x] PaymentBehaviorIndicator (🟢🟡🔴 Ampel)
- [x] ChurnRiskAlert (bei Inaktivität)

**Tests:** 12 Tests (7 Backend + 5 Frontend)

---

### **4. Churn-Alarm Konfiguration** (2h)

**Ziel:** Pro Kunde konfigurierbare Churn-Schwelle

**Backend:**
- [x] customers.churn_threshold_days (Default: 90)
- [x] ChurnDetectionService Integration (Sprint 2.1.7.4)

**Frontend:**
- [x] Edit Customer Dialog erweitert (Churn-Schwelle)
- [x] Validation (14-365 Tage)

**Tests:** 5 Tests (3 Backend + 2 Frontend)

---

### **5. Admin-UI für Xentral-Einstellungen** (2h)

**Ziel:** Admin kann Xentral-API konfigurieren

**Frontend:**
- [x] XentralSettingsPage Component (/admin/integrations/xentral)
- [x] API-Endpoint, Token, Mock-Mode Toggle
- [x] Connection-Test Button

**Backend:**
- [x] PUT /api/admin/xentral/settings
- [x] GET /api/admin/xentral/test-connection

**Tests:** 4 Tests (2 Backend + 2 Frontend)

---

### **6. Sales-Rep Mapping Auto-Sync + Admin-UI** (1h)

**Ziel:** Automatischer Sync von Xentral Sales-Rep-IDs via Email-Matching

**Backend:**
- [x] SalesRepSyncJob (@Scheduled, täglich 2:00 Uhr)
- [x] Email-basiertes Matching (Xentral ↔ Users)

**Frontend:**
- [x] UserManagementPage erweitert (Spalte: Xentral Sales-Rep ID)

**Tests:** 3 Tests (2 Backend + 1 Frontend)

---

### **7. Testing & Bugfixes** (3h)

**Ziel:** Integration Testing + E2E Testing

**Tasks:**
- [x] Backend Integration Tests (10 Tests)
- [x] Frontend Integration Tests (8 Tests)
- [x] E2E: Opportunity → Customer Flow
- [x] E2E: Customer-Dashboard mit Xentral-Daten

---

### **8. Xentral Webhook Integration** (2h)

**Ziel:** Automatische PROSPECT → AKTIV Aktivierung bei Xentral-Bestellung

**Backend:**
- [x] XentralOrderEventHandlerImpl (Sprint 2.1.7.4 Interface)
- [x] XentralWebhookResource (POST /api/xentral/webhook/order-delivered)
- [x] Nutzt customerService.activateCustomer() (Sprint 2.1.7.4)

**Tests:** 4 Tests (Webhook + Integration)

---

## 🔧 SUB-TRIGGERS & TECHNICAL FOUNDATIONS

### **D11: Server-Driven Customer Cards** (COMPLETE)

**Status:** ✅ COMPLETE (2025-10-25)
**Trigger-Dokument:** [TRIGGER_SPRINT_2_1_7_2_D11_SERVER_DRIVEN_CARDS.md](TRIGGER_SPRINT_2_1_7_2_D11_SERVER_DRIVEN_CARDS.md)

**Ziel:** Server-Driven UI Architektur für Customer Cards - Backend als Single Source of Truth für Schema und Daten

**Completed Work:**
- ✅ **Backend Phase 1**: 5 fehlende Enum-Endpoints implementiert
  - `/api/enums/legal-forms` (LegalForm.java)
  - `/api/enums/customer-types` (CustomerType.java)
  - `/api/enums/payment-terms` (PaymentTerms.java)
  - `/api/enums/delivery-conditions` (DeliveryCondition.java)
  - `/api/enums/expansion-plan` (ExpansionPlan.java)
- ✅ **Frontend Phase 1**: Browser-Fehler behoben
  - MUI Grid v7 Migration (DynamicField.tsx)
  - DATE Format Fix (yyyy-MM-dd)
- ✅ **Design System Enforcement**: Pre-Commit Hook erweitert
  - Custom Palette Usage Checks (theme.palette.status.won)
  - Shadow System Checks (theme.shadows[X])
  - Transitions Checks (theme.transitions.duration.X)
- ✅ **Documentation**: DESIGN_SYSTEM.md vollständig dokumentiert
  - Component Overrides (Button, Card, Drawer, Sidebar, TextField, Tabs, Chip)
  - Custom Palette Extensions (freshfoodz, status namespaces)
  - MUI v7 Grid API Migration Guide

**Commits:**
- `dcf8ac007`: D11 Phase 1 - Browser-Fehler behoben + Design System Enforcement
- `158a7557f`: Xentral Integration + Testkunden-Strategie + Quality Improvements

**Business Impact:**
- Frontend-Entwickler können Customer-Cards dynamisch erweitern ohne Backend-Deployment
- Design System wird automatisch durchgesetzt (Pre-Commit Hook blockiert Violations)
- Neue Claude-Instanzen haben vollständige Theme-Dokumentation

---

### **D9: Customer UX Polish** (MOVED TO Sprint 2.1.7.7)

**Status:** 📦 MOVED TO Sprint 2.1.7.7
**Estimated Effort:** 4h

**Ziel:** Customer Wizard + Dashboard UX Review und Pain-Points beheben

**Scope:**
- Customer Wizard UX Review (Pain Points + Multi-Contact Support)
- Customer Dashboard Polish (Layout + Informationsarchitektur)
- Wizard-Validation verbessern

**Begründung der Verschiebung:**
- D9 ist kein Blocker für Xentral Integration (D1-D8, D11)
- Sprint 2.1.7.2 Fokus: Core Xentral Integration (100% Complete)
- D9 passt besser zu Sprint 2.1.7.7 (UI/UX Improvements für Multi-Location)

**→ Siehe:** [TRIGGER_SPRINT_2_1_7_7.md](TRIGGER_SPRINT_2_1_7_7.md#d9-customer-ux-polish)

---

### **D10: Multi-Location Vorbereitung** (MOVED TO Sprint 2.1.7.7)

**Status:** 📦 MOVED TO Sprint 2.1.7.7
**Estimated Effort:** 1h

**Ziel:** hierarchyType Dropdown in ConvertToCustomerDialog vorbereiten (UI disabled)

**Scope:**
- hierarchyType Dropdown (HEADQUARTER/FILIALE/STANDALONE)
- Initial disabled (kein CreateBranch UI yet)
- Foundation für Sprint 2.1.7.7

**Begründung der Verschiebung:**
- Sprint 2.1.7.7 implementiert vollständiges Multi-Location Management
- D10 ist Vorbereitung, macht ohne D9 (CreateBranchDialog) keinen Sinn
- Bessere Kohärenz: Alle Multi-Location Features in einem Sprint

**→ Siehe:** [TRIGGER_SPRINT_2_1_7_7.md](TRIGGER_SPRINT_2_1_7_7.md#deliverables)

---

## 📊 SUCCESS METRICS

**Test Coverage:**
- Backend: 46 Tests (API Client 10 + Services 14 + Webhook 4 + Admin 6 + Sync 3 + Integration 9)
- Frontend: 26 Tests (Components 12 + Integration 8 + E2E 6)
- **Total: 72 Tests**

**Code Changes:**
- 2 Migrations (V10034: xentral_customer_id, V10035: months_active)
- 10 Backend-Dateien (XentralApiClient, Services, Webhook, Admin, Metrics)
- 8 Frontend-Dateien (ConvertDialog, RevenueMetricsCard, DeviationsWidget, Dashboard, Admin-UI)

**Business Impact:**
- Vertriebler haben Self-Service-Dashboard
- Churn-Prevention: Frühwarnung 14-90 Tage
- Automatische Aktivierung: Keine manuelle Arbeit
- Seasonal Business Support: Food-Branche korrekt behandelt

---

## ✅ DEFINITION OF DONE

### **Functional**
- [x] Opportunity → Customer Conversion funktioniert
- [x] Xentral-Kunden-Dropdown zeigt verkäufer-eigene Kunden
- [x] Customer-Dashboard zeigt Xentral-Umsatzdaten
- [x] Zahlungsverhalten-Ampel funktioniert
- [x] Churn-Alarm pro Kunde konfigurierbar
- [x] Admin-UI für Xentral-Einstellungen vorhanden
- [x] Sales-Rep Auto-Sync läuft täglich
- [x] Xentral Webhook aktiviert PROSPECT automatisch

### **Technical**
- [x] XentralApiClient implementiert (4 Endpoints)
- [x] Feature-Flag: mock-mode (Hybrid-Ansatz)
- [x] XentralOrderEventHandlerImpl (Sprint 2.1.7.4 Interface)
- [x] XentralWebhookResource (POST /webhook/order-delivered)
- [x] SalesRepSyncJob (@Scheduled)
- [x] ChurnDetectionService Integration (Sprint 2.1.7.4)
- [x] Seasonal Business Support (Sprint 2.1.7.4)

### **Quality**
- [x] Tests: 72/72 GREEN
- [x] TypeScript: type-check PASSED
- [x] Code Review: Self-reviewed
- [x] Documentation: Updated

---

## 📅 TIMELINE

**Tag 1 (8h):**
- Opportunity → Customer Conversion (4h)
- Xentral-API-Client Implementation (3h)
- Admin-UI für Xentral-Einstellungen (1h)

**Tag 2 (9h):**
- Customer-Dashboard mit echten Daten (8h) ← erweitert mit UX
- Churn-Alarm Konfiguration (1h)

**Tag 3 (7h):**
- Sales-Rep Mapping Auto-Sync (1h)
- Xentral Webhook Integration (2h)
- Testing & Bugfixes (3h)
- Documentation (1h)

**Total:** 25h (3+ Arbeitstage)

---

## 📄 ARTEFAKTE

**Technische Spezifikation:**
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_2_TECHNICAL.md`
- Xentral API Client Implementation
- ConvertToCustomerDialog (vollständig)
- Customer-Dashboard Code-Beispiele
- Xentral Webhook Integration
- SalesRepSyncJob Implementation
- Test Specifications

**Design Decisions:**
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_2_DESIGN_DECISIONS.md`
- Sales-Rep Mapping Strategie (Email-basiert)
- Mock vs Real Development (Hybrid-Ansatz)
- Churn-Alarm Konfiguration (Pro Kunde vs Global)
- Zahlungsverhalten-Ampel (Schwellwerte)
- Status-Architektur Integration (Sprint 2.1.7.4)
- Webhook vs Polling (Real-Time vs Batch)

**Design System:**
→ `/docs/planung/grundlagen/DESIGN_SYSTEM.md`
- Freshfoodz Color Palette (#94C456, #004F7B)
- Typography (Antonio Bold, Poppins)
- Component Patterns

---

## 🚀 PREREQUISITES

### **✅ BEREITS GEKLÄRT (2025-10-18):**

1. **Sales-Rep Mapping Strategie:**
   - ✅ User-Tabelle erweitern mit `xentral_sales_rep_id`
   - ✅ Auto-Sync-Job: Email-basiertes Matching (täglich 2:00 Uhr)
   - ✅ Admin-UI: Manuelle Pflege unter `/admin/users`

2. **Entwicklungs-Strategie:**
   - ✅ Mock-Enabled Development (Feature-Flag)
   - ✅ Hybrid-Ansatz: Foundation mit Mocks, später echte API
   - ✅ Admin-UI: Settings-Seite für API-Konfiguration

### **✅ IT-TEAM ANTWORT (2025-10-21):**

**Xentral-Version:** v25.39.5 PRO ✅
**API-Zugang:** Personal Access Token (PAT) vorhanden ✅
**Entscheidung:** **Neue Xentral API verwenden** (NICHT Legacy v1 REST API!)

**Warum neue API?**
- ✅ v1 REST API ist in Maintenance Mode (keine neuen Features)
- ✅ Neue API (v25.39+) ist aktiv entwickelt
- ✅ JSON:API Standard (RFC 7159)
- ✅ 2025 Feature: Customer Financial Data inkludiert!
- ✅ Webhooks (BETA) verfügbar
- ✅ Zukunftssicher (v1 wird irgendwann abgeschaltet)

**IT-Integration Checklist (GEKLÄRT):**
1. ✅ Xentral API Endpoints: Neue API (v25.39+) - 4 Endpoints verfügbar
2. ✅ API Authentication: Personal Access Token (PAT)
3. ✅ Sales-Rep Mapping: Email-basiert (automatischer Sync)
4. ⏳ Rate Limits: Mit IT klären
5. ✅ Test-Zugang: PAT vorhanden
6. ⚠️ Webhooks: BETA-Feature in v25.39 (Manual Setup in Xentral Admin erforderlich)
7. ✅ Support-Kontakt: api@xentral.com

**Hybrid-Ansatz ermöglicht:**
- ✅ Start ohne Webhooks (Mock-Mode + Manual Activation Button)
- ✅ Später: Webhooks aktivieren (BETA-Feature konfigurieren)

---

## 🎯 NÄCHSTE SCHRITTE

**Sprint-Reihenfolge (AKTUALISIERT - 2025-10-19):**

```
✅ Sprint 2.1.7.3 COMPLETE (Customer → Opportunity Workflow)
   ↓
📋 Sprint 2.1.7.4 (Customer Status Architecture) ← ZUERST!
   ↓
📋 Sprint 2.1.7.2 (Xentral Integration) ← DANN!
```

**Warum Sprint 2.1.7.4 zuerst?**
- CustomerStatus.LEAD muss entfernt werden (konzeptionell falsch!)
- PROSPECT → AKTIV Logik muss definiert sein
- XentralOrderEventHandler Interface wird von Sprint 2.1.7.2 genutzt
- ChurnDetectionService wird von Sprint 2.1.7.2 genutzt

**Nach Sprint 2.1.7.4 COMPLETE:**
1. Sprint 2.1.7.2 starten (Xentral Integration)
2. ConvertToCustomerDialog zeigt PROSPECT Status Info
3. Xentral Webhook nutzt activateCustomer() aus Sprint 2.1.7.4
4. ChurnDetectionService mit Seasonal Business Support

---

## 📝 NOTES

### **User-Entscheidungen (2025-10-18 + 2025-10-19)**

Alle Design-Entscheidungen sind dokumentiert in:
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_2_DESIGN_DECISIONS.md`

**Highlights:**
- Sales-Rep Mapping: Email-basiert (Auto-Sync täglich)
- Hybrid-Ansatz: Mock-Mode für Development (Feature-Flag)
- Churn-Alarm: Pro Kunde konfigurierbar (14-365 Tage)
- Zahlungsverhalten: Ampel-System (🟢🟡🔴)
- Status-Architektur: PROSPECT → AKTIV bei erster Bestellung (Sprint 2.1.7.4)
- Webhook Integration: Automatische Aktivierung (kein manueller Button)

### **Technical Debt**

- Xentral-API Caching: Aktuell keine Caching-Strategie (später: Redis/Caffeine)
- Error Recovery: Wenn Xentral down → Fallback-Daten? Retry-Logic?
- Mock → Real Switch: Aktuell Feature-Flag - später: automatische Erkennung

### **⚠️ WICHTIG: XentralApiConfig NICHT benötigt!**

**Entscheidung (2025-10-24):** Xentral Connect API (2024/25) benötigt **KEINE** separate `XentralApiConfig.java`

**Begründung:**
- Neue Xentral Connect API verwendet **Token-basierte Authentifizierung** (PAT)
- Nur 3 Properties benötigt: `xentral.api.base-url`, `xentral.api.token`, `xentral.api.mock-mode`
- **`@ConfigProperty`** Injection reicht vollständig aus
- **`@ConfigMapping`** mit `XentralApiConfig` führt zu Configuration-Validierungsfehlern

**Architektur:**
```java
// ✅ RICHTIG: Direct @ConfigProperty Injection
@ConfigProperty(name = "xentral.api.base-url")
String baseUrl;

@ConfigProperty(name = "xentral.api.token")
String token;

@ConfigProperty(name = "xentral.api.mock-mode", defaultValue = "true")
boolean mockMode;

// ❌ FALSCH: XentralApiConfig mit @ConfigMapping
// → Verursacht Property Validation Errors
// → Nicht benötigt für neue API
```

**Betroffene Dateien:**
- ✅ `XentralSettingsResource.java` → Verwendet `@ConfigProperty`
- ✅ `XentralApiService.java` → Verwendet `@ConfigProperty`
- ❌ `XentralApiConfig.java` → **DELETED** (war Legacy-Code, nie benötigt)

**Commit:** `015822ffa` (2025-10-24)

**Siehe auch:** [ADR-0008: Xentral API Configuration via @ConfigProperty](adr/ADR-0008-xentral-config-property-injection.md)

---

## 🔗 RELATED WORK

**Dependent Sprints:**
- Sprint 2.1.7.1: Lead → Opportunity UI Integration (COMPLETE)
- Sprint 2.1.7.4: Customer Status Architecture (COMPLETE - REQUIRED!)
  - CustomerStatus.LEAD entfernt
  - PROSPECT → AKTIV Logik
  - XentralOrderEventHandler Interface
  - ChurnDetectionService mit Seasonal Business Support

**Follow-up Sprints:**
- Sprint 2.1.7.3: Customer → Opportunity Workflow (kann parallel laufen)
- Sprint 2.1.7.6: Customer Lifecycle Management

**Integration Points:**
- Sprint 2.1.7.4 liefert: XentralOrderEventHandler Interface
- Sprint 2.1.7.2 implementiert: XentralOrderEventHandlerImpl
- Sprint 2.1.7.4 liefert: customerService.activateCustomer()
- Sprint 2.1.7.2 nutzt: activateCustomer() in Webhook

---

**✅ SPRINT STATUS: 📋 READY TO START - Nach Sprint 2.1.7.4 COMPLETE**

**Letzte Aktualisierung:** 2025-10-24 (XentralApiConfig Architecture Decision dokumentiert)
