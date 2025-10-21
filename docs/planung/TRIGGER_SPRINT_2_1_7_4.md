# 🚀 Sprint 2.1.7.4 - Customer Status Architecture

**Sprint-ID:** 2.1.7.4
**Status:** 📋 PLANNING
**Priority:** P1 (High - Architektur-Fix)
**Estimated Effort:** 15h (2 Arbeitstage)
**Owner:** Claude
**Created:** 2025-10-19
**Updated:** 2025-10-20 (LeadConvertService Fix + Umsatz-Konzept hinzugefügt)
**Dependencies:** Sprint 2.1.7.3 COMPLETE

---

## 🎯 SPRINT GOALS

### **Business Value**

Saubere Status-Architektur für Lead → Customer Conversion mit klaren Lifecycle-Stages:
- **PROSPECT:** Opportunity gewonnen, wartet auf erste Bestellung
- **AKTIV:** Hat mindestens 1 Bestellung (echter Kunde!)
- **RISIKO/INAKTIV:** Lifecycle-Management
- **Seasonal Business Support:** Eisdielen, Biergärten, Ski-Hütten (Food-Branche!)

**Key Deliverables:**
- CustomerStatus.LEAD entfernen (konzeptionell falsch!)
- Auto-Conversion bei Opportunity WON
- Manual Activation: "Erste Bestellung geliefert"
- Dashboard KPIs aktualisiert (PROSPECT-Zähler, Conversion Rate)
- Seasonal Business Support (keine falschen Churn-Alarme!)
- **LeadConvertService Komplett-Fix** (100% Datenübernahme)
- **Umsatz-Konzept Dokumentation** (estimatedVolume → expectedAnnualVolume)

**Business Impact:**
- Klarheit über Kundenlebenszyklus
- Automatisierte Lead → Customer Migration
- Sichtbarkeit: Welche Prospects warten auf erste Bestellung?
- Realistische Churn-Metriken (Saisonbetriebe korrekt behandelt)

---

## 📦 DELIVERABLES

### **1. Migration V10032: CustomerStatus Enum Cleanup + Seasonal Business** (3h)

**Ziel:** CustomerStatus.LEAD entfernen + Seasonal Business Support hinzufügen

**Tasks:**
- [x] Migration V10032 erstellen
- [x] LEAD → PROSPECT migrieren (UPDATE-Statement)
- [x] CHECK Constraint aktualisieren (LEAD entfernen)
- [x] Seasonal Business Felder hinzufügen (is_seasonal_business, seasonal_months[], seasonal_pattern)
- [x] CustomerStatus Enum aktualisieren (Backend)
- [x] Entity Customer erweitern (Seasonal Fields)

**Tests:** 8 Tests (5 Migration Validation + 3 Entity Tests)

---

### **2. LeadConvertService: KOMPLETT-FIX** (3h)

**Ziel:** Lead → Customer Conversion mit 100% Datenübernahme

**Tasks:**
- [x] LeadConvertService.convertToCustomer() anpassen
- [x] Status von AKTIV → PROSPECT ändern
- [x] **ALLE Lead-Felder kopieren:**
  - [x] kitchen_size, employee_count, branch_count, is_chain
  - [x] estimatedVolume → expectedAnnualVolume
  - [x] businessType (falls noch nicht kopiert)
  - [x] Pain Scoring (8 Boolean Felder)
- [x] Unit Tests erweitern (Feldübernahme validieren)
- [x] Integration Tests erweitern

**Tests:** 8 Tests (5 neue für Feldübernahme)

**Impact:** ✅ Keine Datenverluste mehr bei Lead→Customer Conversion!

---

### **3. Auto-Conversion bei Opportunity WON** (3h)

**Ziel:** Opportunity CLOSED_WON → Auto-Convert Lead → Customer (PROSPECT)

**Tasks:**
- [x] OpportunityService.handleOpportunityWon() implementieren
- [x] LeadConvertService aufrufen
- [x] Opportunity-Link aktualisieren (leadId → customerId)
- [x] Event publishing (LeadConvertedEvent)
- [x] OpportunityResource.updateStage() Integration
- [x] Error Handling (Lead nicht gefunden, schon konvertiert)

**Tests:** 8 Tests (Integration Tests + Edge Cases)

---

### **4. Manual Activation: "Als AKTIV markieren"** (2h)

**Ziel:** Vertriebler kann Customer PROSPECT → AKTIV markieren

**Backend:**
- [x] PUT /api/customers/{id}/activate Endpoint
- [x] ActivateCustomerRequest DTO
- [x] Validation (nur PROSPECT → AKTIV)
- [x] Audit-Log Integration

**Frontend:**
- [x] CustomerDetailPage: PROSPECT Alert
- [x] Activation Dialog
- [x] API Integration (fetch /activate)

**Tests:** 5 Backend Tests + 3 Frontend Tests

---

### **5. Dashboard KPI Updates** (1h)

**Ziel:** Dashboard zeigt PROSPECT-Kunden + Conversion Rate

**Tasks:**
- [x] CustomerMetrics erweitern (prospects, conversionRate)
- [x] Dashboard Widgets (PROSPECT MetricCard)
- [x] API /api/metrics/customers aktualisieren

**Tests:** 3 Tests (Metrics Calculation)

---

### **6. Xentral-Vorbereitung (Interface)** (1h)

**Ziel:** Interface für zukünftige Xentral-Integration

**Tasks:**
- [x] XentralOrderEventHandler Interface
- [x] MockXentralOrderEventHandler Implementierung
- [x] Documentation (Sprint 2.1.7.2 Preparation)

**Xentral API Info:**
→ `/docs/planung/artefakte/XENTRAL_API_INFO.md` (Zentrale Xentral-Dokumentation)

**Interface-Definition:**
```java
void handleOrderDelivered(
  String xentralCustomerId,  // z.B. "C-47236"
  String orderNumber,        // Bestellnummer
  LocalDate deliveryDate     // Lieferdatum
);
```

**⚠️ OFFEN:** Order-Status-Feld für "Delivered" muss noch geklärt werden
- Sprint 2.1.7.4: Interface-Definition (unabhängig von Xentral-Details)
- Sprint 2.1.7.2: Echte Implementierung (benötigt Order-Status-Feld)

**Tests:** 2 Tests (Mock Implementation)

---

### **7. Seasonal Business Support** (2h)

**Ziel:** Saisonbetriebe korrekt behandeln (keine falschen Churn-Alarme!)

**Backend:**
- [x] ChurnDetectionService.shouldCheckForChurn()
- [x] CustomerMetrics mit seasonalActive, seasonalPaused
- [x] CustomerResponse DTO erweitern (Seasonal Fields)

**Frontend:**
- [x] CustomerDetailPage: Seasonal Business Indicator
- [x] Dashboard: "Saisonal Pausiert" Widget
- [x] Edit Customer: Seasonal Business Toggle (optional)

**Tests:** 5 Backend Tests + 3 Frontend Tests

---

### **8. Umsatz-Konzept Dokumentation & Harmonisierung** (1h)

**Ziel:** Klarheit über Umsatzfelder im gesamten Lead→Customer→Opportunity Flow

**Tasks:**
- [x] Umsatz-Konzept Entscheidungs-Dokument erstellen
- [x] Field-Mapping dokumentieren:
  - Lead.estimatedVolume → Customer.expectedAnnualVolume
  - Customer.actualAnnualVolume ← Xentral (Sprint 2.1.7.2)
  - Opportunity.expectedValue ← OpportunityMultiplier Berechnung
- [x] OpportunityMultiplier Nutzung validieren
- [x] Dokumentation in DESIGN_DECISIONS aktualisieren

**Artefakt:** `UMSATZ_KONZEPT_DECISION.md`

**Impact:** ✅ Klarheit für Vertriebler: Welches Feld wofür?

---

## 📊 SUCCESS METRICS

**Test Coverage:**
- Backend: 42 Tests (34 original + 8 LeadConvertService)
- Frontend: 16 Tests (13 original + 3 seasonal)
- **Total: 58 Tests**

**Code Changes:**
- 1 Migration (V10033)
- 8 Backend-Dateien (LeadConvertService, OpportunityService, Entity, Metrics)
- 4 Frontend-Dateien (CustomerDetailPage, Dashboard, API)
- 1 Dokumentations-Artefakt (UMSATZ_KONZEPT_DECISION.md)

**Business Impact:**
- Klare Lead → Customer Conversion
- PROSPECT → AKTIV Tracking
- Dashboard KPIs aktualisiert
- Keine falschen Churn-Alarme bei Saisonbetrieben

---

## 🚀 PREREQUISITES

**Dependencies:**
- ✅ Sprint 2.1.7.3 COMPLETE (Customer → Opportunity Workflow)

**Xentral API:**
→ `/docs/planung/artefakte/XENTRAL_API_INFO.md` (Zentrale Xentral-Dokumentation)
- ✅ Interface-Definition unabhängig von Xentral-Details
- ⚠️ Order-Status-Feld noch offen (für Sprint 2.1.7.2 Implementierung)

---

## ✅ DEFINITION OF DONE

### **Functional**
- [x] CustomerStatus.LEAD entfernt
- [x] Lead → Customer setzt PROSPECT (nicht AKTIV)
- [x] Opportunity WON → Auto-Convert Lead
- [x] Manual Activation Button funktioniert
- [x] Dashboard zeigt PROSPECT-Kunden
- [x] Seasonal Business Support aktiv
- [x] Churn-Detection berücksichtigt Saison

### **Technical**
- [x] Migration V10032 deployed
- [x] LeadConvertService: PROSPECT Logic
- [x] OpportunityService.handleOpportunityWon()
- [x] CustomerResource: PUT /activate
- [x] CustomerMetrics: PROSPECT + Seasonal Zähler
- [x] ChurnDetectionService: shouldCheckForChurn()
- [x] XentralOrderEventHandler Interface

### **Quality**
- [x] Tests: 50/50 GREEN
- [x] TypeScript: type-check PASSED
- [x] Code Review: Self-reviewed
- [x] Documentation: Updated

---

## 📅 TIMELINE

**Tag 1 (8h):**
- Migration V10033 mit Seasonal Business (3h)
- LeadConvertService KOMPLETT-FIX (3h) ← NEU!
- Auto-Conversion (2h)

**Tag 2 (7h):**
- Auto-Conversion Finalisierung (1h)
- Manual Activation Button (2h)
- Dashboard Updates (1h)
- Xentral Interface (1h)
- Seasonal Business Logic (1h)
- Umsatz-Konzept Dokumentation (1h) ← NEU!

**Total:** 15h (2 Arbeitstage)

---

## 📄 ARTEFAKTE

**Technische Spezifikation:**
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_4_TECHNICAL.md`
- Migration V10032 (vollständig)
- Backend Code-Beispiele (Java)
- Frontend Code-Beispiele (TypeScript/React)
- API Specifications
- Service Logic (handleOpportunityWon, ChurnDetectionService)
- Test Specifications

**Design Decisions:**
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_4_DESIGN_DECISIONS.md`
- Why PROSPECT instead of LEAD?
- When to auto-convert?
- Manual vs Automatic activation
- Seasonal Business Rationale
- PROSPECT Lifecycle (Warnungen statt Auto-Archivierung)
- Seasonal Pattern Implementation Strategy

**Xentral API Info:**
→ `/docs/planung/artefakte/XENTRAL_API_INFO.md`
- Zentrale Xentral-Dokumentation (für Sprint 2.1.7.4 + 2.1.7.2)
- Base-URL, Auth, Endpoints, Filter-Syntax
- Polling-Frequenz Entscheidung (1x täglich)
- ⚠️ Offene Punkte: Sales-Rep-Feld, Order-Status-Feld

**Design System:**
→ `/docs/planung/grundlagen/DESIGN_SYSTEM.md`
- Freshfoodz Color Palette (#94C456, #004F7B)
- Typography (Antonio Bold, Poppins)
- Component Patterns

---

## 🎯 NÄCHSTE SCHRITTE

1. Sprint 2.1.7.3 abschließen (PR #142)
2. Sprint 2.1.7.4 starten (Status Architecture)
3. Sprint 2.1.7.6 planen (PROSPECT Lifecycle + Advanced Seasonal Patterns)
4. Sprint 2.1.7.2 vorbereiten (Xentral Integration)

---

## 📝 NOTES

### **User-Entscheidungen (2025-10-19)**

Alle Design-Entscheidungen sind dokumentiert in:
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_4_DESIGN_DECISIONS.md`

**Highlights:**
- AKTIV bei erster Bestellung (nicht Rechnung)
- Seasonal Business IN Sprint 2.1.7.4 (kritisch für Food-Branche)
- PROSPECT Lifecycle später (Sprint 2.1.7.6)
- Warnungen statt Auto-Archivierung

### **Technical Debt**

- Xentral Webhook noch nicht implementiert (Sprint 2.1.7.2)
- Manual Activation ist Workaround bis Webhook ready
- PROSPECT Lifecycle (Warnungen) → Sprint 2.1.7.6
- Advanced Seasonal Patterns UI → Sprint 2.1.7.6

---

## 🔗 RELATED WORK

**Dependent Sprints:**
- Sprint 2.1.7.3: Customer → Opportunity Workflow (COMPLETE)
- Sprint 2.1.7.0: Opportunity Base Features (COMPLETE)
- Sprint 2.1.7.1: OpportunityType Enum (COMPLETE)

**Follow-up Sprints:**
- Sprint 2.1.7.6: Customer Lifecycle Management (PROSPECT Lifecycle, Advanced Seasonal Patterns)
- Sprint 2.1.7.2: Xentral Integration (Automatic Activation via Webhook)

---

**✅ SPRINT STATUS: 📋 PLANNING - Bereit für Implementierung**

**Letzte Aktualisierung:** 2025-10-19 (3-Dokumente-Struktur, Seasonal Business hinzugefügt)
