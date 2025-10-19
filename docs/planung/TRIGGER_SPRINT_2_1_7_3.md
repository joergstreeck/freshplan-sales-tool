# 🚀 Sprint 2.1.7.3 - Bestandskunden-Workflow

**Sprint-ID:** 2.1.7.3
**Status:** ✅ COMPLETE (95%) - Core Deliverables Done, Bug-Fixes Pending
**Priority:** P2 (Medium)
**Estimated Effort:** 30-31h (3-4 Arbeitstage)
**Owner:** Claude
**Created:** 2025-10-16
**Updated:** 2025-10-19
**Dependencies:** KEINE

---

## 🎯 SPRINT GOALS

### **Business Value**

Vertriebler können Bestandskunden erweitern (Sortimentserweiterung/Neuer Standort/Vertragsverlängerung) mit **intelligenter Umsatzschätzung** basierend auf echten Xentral-Daten.

**Key Features:**
- Customer → Opportunity Flow (< 3 Klicks)
- Customer-Opportunity-Historie (gruppiert: Offen/Gewonnen/Verloren)
- Business-Type-Matrix (9 BusinessTypes × 4 OpportunityTypes = 36 Multipliers)
- 3-Tier Fallback (Xentral → Lead → Manual)
- Settings-Foundation (Database-driven, konfigurierbar)

**Business Impact:**
- **Datengetriebene Schätzung** statt Bauchgefühl
- **Erweiterungspotenzial erkennbar** - Verkäufer sieht: "Kunde kauft regelmäßig → Zeit für Produkterweiterung!"
- **Customer-Lifetime-Value sichtbar** - Alle Deals zu einem Kunden auf einen Blick

---

## 📦 DELIVERABLES

### **Core Features (95% COMPLETE)**

- [x] **1. Customer → Opportunity Button** (2h) ✅ COMPLETE
  - [x] CustomerDetailPage Integration
  - [x] CreateOpportunityForCustomerDialog (21 Tests GREEN)
  - [x] Business-Type-Matrix Integration (intelligente Schätzung)

- [x] **2. Customer-Opportunity-Historie** (3h) ✅ COMPLETE
  - [x] CustomerOpportunitiesList Component (19 Tests GREEN)
  - [x] Accordion-Gruppierung (Offen/Gewonnen/Verloren)
  - [x] OpportunityCard Sub-Component

- [x] **3. Backend API** (1h) ✅ COMPLETE
  - [x] GET /api/customers/{id}/opportunities (4 Tests GREEN)
  - [x] OpportunityService.findByCustomerId()

- [x] **6. Settings-Foundation** (9h) ✅ COMPLETE
  - [x] Migration V10031: opportunity_multipliers (36 entries)
  - [x] OpportunityMultiplier Entity + Service
  - [x] GET /api/settings/opportunity-multipliers (39 Tests GREEN)

### **Bug-Fixes & Extensions (5% PENDING)**

- [ ] **4. OpportunityType Backend-Validierung** (30 Min) ❌ PENDING
  - **BUG:** `createForCustomer()` setzt OpportunityType NICHT!
  - **Fix:** `opportunity.setOpportunityType(type)` hinzufügen

- [ ] **5. Activity-Tracking** (1h) ❌ PENDING
  - [ ] ActivityType Enum erweitern (3 neue: EXPANSION_CALL, PRODUCT_DEMO, CONTRACT_RENEWAL)
  - [ ] ActivityDialog Frontend erweitern

---

## 📊 SUCCESS METRICS

**Test Coverage:**
- Backend: 43/43 Tests GREEN ✅
- Frontend: 47/47 Tests GREEN ✅
- **Total: 90/90 Tests GREEN** ✅

**Commits:**
- 90b385945 - Backend Business-Type-Matrix (39 tests)
- 753a95245 - CreateOpportunityForCustomerDialog
- a7f7944ef - Tests CreateOpportunityForCustomerDialog (21 tests)
- 6b8e8ed28 - CustomerOpportunitiesList
- a95d21bf1 - Tests CustomerOpportunitiesList (19 tests)
- 87cf9d65f - Migration V10031 CHECK constraints fix
- e4d1f1304 - findByCustomerId Integration Tests (4 tests)
- 3a1e84f36 - CustomerDetailPage Integration

---

## ✅ DEFINITION OF DONE

### **Functional**
- [x] Customer kann neue Opportunity erstellen (< 3 Klicks)
- [x] expectedValue wird intelligent geschätzt (Business-Type-Matrix)
- [x] Customer-Opportunity-Historie gruppiert (Offen/Gewonnen/Verloren)
- [ ] OpportunityType-Logik korrekt (Backend-Bug fix pending)
- [ ] Activity-Typen erweitert (3 neue für Bestandskunden)

### **Technical**
- [x] Backend: GET /api/customers/{id}/opportunities
- [x] Backend: GET /api/settings/opportunity-multipliers
- [x] Frontend: CreateOpportunityForCustomerDialog
- [x] Frontend: CustomerOpportunitiesList
- [x] Migration: V10031 (opportunity_multipliers mit CHECK constraints)
- [ ] Backend: createForCustomer() setzt OpportunityType
- [ ] Backend: ActivityType Enum erweitert

### **Quality**
- [x] Tests: 90/90 GREEN (Backend 43, Frontend 47)
- [x] TypeScript: type-check PASSED
- [x] Code Review: Self-reviewed
- [ ] E2E Testing: Pending (optional)

---

## 📅 TIMELINE

**Tag 1-2 (16h):** ✅ COMPLETE
- Customer → Opportunity Flow
- Customer-Opportunity-Historie
- Backend API
- Settings-Foundation (Migration + Backend)

**Tag 3 (8h):** ✅ COMPLETE
- Business-Type-Matrix Integration (Frontend)
- CreateOpportunityForCustomerDialog Tests
- CustomerOpportunitiesList Tests
- CustomerDetailPage Integration

**Tag 4 (6h):** ❌ PENDING
- OpportunityType Backend-Validierung (Bug-Fix)
- Activity-Tracking (3 neue Typen)
- E2E Testing (optional)

**Total:** ~30h / 30-31h estimated (95% COMPLETE)

---

## 📄 ARTEFAKTE

**Technische Spezifikation:**
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_3_TECHNICAL.md`
- API Specifications
- Component Specifications
- Database Schema (Migration V10031)
- Code Examples
- Test Specifications

**Design Decisions:**
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_3_DESIGN_DECISIONS.md`
- Business-Type-Matrix Rationale
- 3-Tier Fallback Strategy (Xentral → Lead → Manual)
- Settings-System Architecture (OHNE Territory-Overrides)
- Admin Route (/admin/settings/opportunities)
- Audit-Log (JA - created_by, updated_by)

**Design System:**
→ `/docs/planung/grundlagen/DESIGN_SYSTEM.md`
- Freshfoodz Color Palette (#94C456, #004F7B)
- Typography (Antonio Bold, Poppins)
- Component Patterns

---

## 🎯 NÄCHSTE SCHRITTE

**Bereit für Completion:**
1. ✅ Feature-Branch: `feature/sprint-2-1-7-3-renewal-workflow`
2. ✅ Migration V10031 deployed
3. ✅ 90 Tests GREEN
4. ⏳ **PENDING:** OpportunityType Bug-Fix (30 Min)
5. ⏳ **PENDING:** Activity-Tracking (1h)
6. ⏳ **OPTIONAL:** E2E Testing (1-2h)

**PR Ready:** Nach Bug-Fixes (Deliverables 4 + 5)

---

## 📝 NOTES

### **User-Validierte Entscheidungen (2025-10-19)**

1. **Settings OHNE Territory-Overrides:**
   > "Territory nicht nötig, weil auf Xentral Daten zugegriffen wird.
   > Das sind echte Umsätze und beinhalten ja quasi die territorialen unterschiede."

2. **Audit-Log JA:**
   > "Audit ja."

3. **Admin Route:**
   > `/admin/settings/opportunities` (Business Settings, nicht System)

4. **OpportunityType als VARCHAR:**
   > "Das sollte schon alles harmonisiert sein, wir verwenden VARCHAR"

### **Technical Debt**

- **OpportunityType Backend-Validierung:** Critical Bug - createForCustomer() setzt Feld NICHT
- **Activity-Tracking:** 3 neue Typen fehlen (EXPANSION_CALL, PRODUCT_DEMO, CONTRACT_RENEWAL)
- **E2E Testing:** Manuelles Browser-Testing ausstehend (optional)

---

## 🔗 RELATED WORK

**Dependent Sprints:**
- Sprint 2.1.7.0: Opportunity Base Features (COMPLETE)
- Sprint 2.1.7.1: OpportunityType Enum (COMPLETE)
- Sprint 2.1.7.2: Xentral Integration (actualAnnualVolume field ready)

**Follow-up Sprints:**
- Sprint 2.1.7.4: Advanced Filters & Analytics
- Modul 08: Admin-UI für Multiplier-Settings

---

**✅ SPRINT STATUS: 95% COMPLETE - Bereit für Bug-Fixes & PR**

**Letzte Aktualisierung:** 2025-10-19 (Dokumentations-Refactoring)
