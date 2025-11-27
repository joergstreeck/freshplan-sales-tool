# 🚀 Sprint 2.1.7.4 - Customer Status Architecture

**Sprint-ID:** 2.1.7.4
**Status:** ✅ GEMERGED (PR #143 - MERGED TO MAIN - 22.10.2025 17:06:22 UTC)
**Priority:** P1 (High - Architektur-Fix)
**Estimated Effort:** 15h (2 Arbeitstage)
**Actual Effort:** 15h (2 Arbeitstage)
**Owner:** Claude
**Created:** 2025-10-19
**Updated:** 2025-10-31 (PR #143 MERGED - Merge Commit: ade7fc2fa)
**Completed:** 2025-10-22
**PR:** #143 - https://github.com/joergstreeck/freshplan-sales-tool/pull/143
**Merge Commit:** ade7fc2fa
**CI Status:** ✅ 29/29 Workflows GREEN
**Dependencies:** Sprint 2.1.7.3 COMPLETE ✅

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

### **1. Migration V10032: CustomerStatus Enum Cleanup + Seasonal Business** (3h) ✅ DONE

**Ziel:** CustomerStatus.LEAD entfernen + Seasonal Business Support hinzufügen

**Tasks:**
- [x] Migration V10032 erstellt ✅
- [x] LEAD → PROSPECT migriert (UPDATE-Statement) ✅
- [x] CHECK Constraint aktualisiert (LEAD entfernt) ✅
- [x] Seasonal Business Felder hinzugefügt (is_seasonal_business, seasonal_months[], seasonal_pattern) ✅
- [x] CustomerStatus Enum aktualisiert (Backend) ✅
- [x] Entity Customer erweitert (Seasonal Fields) ✅

**Tests:** 8 Tests (5 Migration Validation + 3 Entity Tests) ✅ GREEN

---

### **2. LeadConvertService: KOMPLETT-FIX** (3h) ✅ DONE

**Ziel:** Lead → Customer Conversion mit 100% Datenübernahme

**Tasks:**
- [x] LeadConvertService.convertToCustomer() angepasst ✅
- [x] Status von AKTIV → PROSPECT geändert ✅
- [x] **ALLE Lead-Felder kopiert:**
  - [x] kitchen_size, employee_count, branch_count, is_chain ✅
  - [x] estimatedVolume → expectedAnnualVolume ✅
  - [x] businessType (falls noch nicht kopiert) ✅
  - [x] Pain Scoring (8 Boolean Felder) ✅
- [x] Unit Tests erweitert (Feldübernahme validiert) ✅
- [x] Integration Tests erweitert ✅

**Tests:** 8 Tests (5 neue für Feldübernahme) ✅ GREEN

**Impact:** ✅ Keine Datenverluste mehr bei Lead→Customer Conversion!

---

### **3. Auto-Conversion bei Opportunity WON** (3h) ✅ DONE

**Ziel:** Opportunity CLOSED_WON → Auto-Convert Lead → Customer (PROSPECT)

**Tasks:**
- [x] OpportunityService.handleOpportunityWon() implementiert ✅
- [x] LeadConvertService aufgerufen ✅
- [x] Opportunity-Link aktualisiert (leadId → customerId) ✅
- [x] Event publishing (LeadConvertedEvent) ✅
- [x] OpportunityResource.updateStage() Integration ✅
- [x] Error Handling (Lead nicht gefunden, schon konvertiert) ✅

**Tests:** 8 Tests (Integration Tests + Edge Cases) ✅ GREEN

---

### **4. Manual Activation: "Als AKTIV markieren"** (2h) ✅ DONE

**Ziel:** Vertriebler kann Customer PROSPECT → AKTIV markieren

**Backend:**
- [x] PUT /api/customers/{id}/activate Endpoint ✅
- [x] ActivateCustomerRequest DTO ✅
- [x] Validation (nur PROSPECT → AKTIV) ✅
- [x] Audit-Log Integration ✅

**Frontend:**
- [x] CustomerDetailPage: PROSPECT Alert ✅
- [x] Activation Dialog ✅
- [x] API Integration (fetch /activate) ✅

**Tests:** 5 Backend Tests + 3 Frontend Tests ✅ GREEN

---

### **5. Dashboard KPI Updates** (1h) ✅ DONE

**Ziel:** Dashboard zeigt PROSPECT-Kunden + Conversion Rate

**Tasks:**
- [x] CustomerMetrics erweitert (prospects, conversionRate) ✅
- [x] Dashboard Widgets (PROSPECT MetricCard) ✅
- [x] API /api/metrics/customers aktualisiert ✅

**Tests:** 3 Tests (Metrics Calculation) ✅ GREEN

---

### **6. Xentral-Vorbereitung (Interface)** (1h) ✅ DONE

**Ziel:** Interface für zukünftige Xentral-Integration

**Tasks:**
- [x] XentralOrderEventHandler Interface ✅
- [x] MockXentralOrderEventHandler Implementierung ✅
- [x] Documentation (Sprint 2.1.7.2 Preparation) ✅

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

**✅ RESOLVED:** Interface definiert - Implementierung in Sprint 2.1.7.2

**Tests:** 2 Tests (Mock Implementation) ✅ GREEN

---

### **7. Seasonal Business Support** (2h) ✅ DONE

**Ziel:** Saisonbetriebe korrekt behandeln (keine falschen Churn-Alarme!)

**Backend:**
- [x] ChurnDetectionService.shouldCheckForChurn() ✅
- [x] CustomerMetrics mit seasonalActive, seasonalPaused ✅
- [x] CustomerResponse DTO erweitert (Seasonal Fields) ✅

**Frontend:**
- [x] CustomerDetailPage: Seasonal Business Indicator ✅
- [x] Dashboard: "Saisonal Pausiert" Widget ✅
- [x] Edit Customer: Seasonal Business Toggle (optional) ✅

**Tests:** 5 Backend Tests + 3 Frontend Tests ✅ GREEN

---

### **8. Umsatz-Konzept Dokumentation & Harmonisierung** (1h) ✅ DONE

**Ziel:** Klarheit über Umsatzfelder im gesamten Lead→Customer→Opportunity Flow

**Tasks:**
- [x] Umsatz-Konzept Entscheidungs-Dokument erstellt ✅
- [x] Field-Mapping dokumentiert:
  - Lead.estimatedVolume → Customer.expectedAnnualVolume ✅
  - Customer.actualAnnualVolume ← Xentral (Sprint 2.1.7.2) ✅
  - Opportunity.expectedValue ← OpportunityMultiplier Berechnung ✅
- [x] OpportunityMultiplier Nutzung validiert ✅
- [x] Dokumentation in DESIGN_DECISIONS aktualisiert ✅

**Artefakt:** `UMSATZ_KONZEPT_DECISION.md` ✅

**Impact:** ✅ Klarheit für Vertriebler: Welches Feld wofür?

---

## 📊 SUCCESS METRICS ✅ ACHIEVED

**Test Coverage:**
- **Total: 1617/1617 Tests GREEN** ✅ 100%
- All Backend Tests GREEN ✅
- All Frontend Tests GREEN ✅
- Integration Tests GREEN ✅

**Code Changes:**
- 3 Migrationen (V10032, V10033, V90008) ✅
- 12 Backend-Dateien (LeadConvertService, OpportunityService, Entity, Metrics, Services) ✅
- 8 Frontend-Dateien (CustomerDetailPage, Dashboard, Badges, Dialogs) ✅
- 1 Dokumentations-Artefakt (UMSATZ_KONZEPT_DECISION.md) ✅

**Business Impact:**
- ✅ Klare Lead → Customer Conversion (100% Datenübernahme)
- ✅ PROSPECT → AKTIV Tracking implementiert
- ✅ Dashboard KPIs aktualisiert (PROSPECT-Zähler, Conversion Rate)
- ✅ Keine falschen Churn-Alarme bei Saisonbetrieben
- ✅ XentralOrderEventHandler Interface ready für Sprint 2.1.7.2

---

## 🚀 PREREQUISITES

**Dependencies:**
- ✅ Sprint 2.1.7.3 COMPLETE (Customer → Opportunity Workflow)

**Xentral API:**
→ `/docs/planung/artefakte/XENTRAL_API_INFO.md` (Zentrale Xentral-Dokumentation)
- ✅ Interface-Definition unabhängig von Xentral-Details
- ⚠️ Order-Status-Feld noch offen (für Sprint 2.1.7.2 Implementierung)

---

## ✅ DEFINITION OF DONE - ALL CRITERIA MET

### **Functional** ✅ COMPLETE
- [x] CustomerStatus.LEAD entfernt ✅
- [x] Lead → Customer setzt PROSPECT (nicht AKTIV) ✅
- [x] Opportunity WON → Auto-Convert Lead ✅
- [x] Manual Activation Button funktioniert ✅
- [x] Dashboard zeigt PROSPECT-Kunden ✅
- [x] Seasonal Business Support aktiv ✅
- [x] Churn-Detection berücksichtigt Saison ✅

### **Technical** ✅ COMPLETE
- [x] Migrationen V10032, V10033, V90008 deployed ✅
- [x] LeadConvertService: PROSPECT Logic + 100% Datenübernahme ✅
- [x] OpportunityService.handleOpportunityWon() ✅
- [x] CustomerResource: PUT /activate ✅
- [x] CustomerMetrics: PROSPECT + Seasonal Zähler ✅
- [x] ChurnDetectionService: shouldCheckForChurn() ✅
- [x] XentralOrderEventHandler Interface ✅

### **Quality** ✅ COMPLETE
- [x] Tests: 1617/1617 GREEN ✅ 100%
- [x] TypeScript: type-check PASSED ✅
- [x] Code Review: Self-reviewed ✅
- [x] Documentation: Updated ✅
- [x] PR #143: MERGED TO MAIN ✅
- [x] Commit: ade7fc2fa ✅

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

**✅ SPRINT STATUS: ✅ COMPLETE - MERGED TO MAIN**

**Merge Info:**
- **PR:** #143
- **Commit:** ade7fc2fa
- **Merged:** 2025-10-22 17:06:22 UTC
- **Deliverables:** 8/8 COMPLETE
- **Tests:** 1617/1617 GREEN
- **Migrations:** V10032, V10033, V90008

**Letzte Aktualisierung:** 2025-10-22 (COMPLETE - Dokumentation aktualisiert)
