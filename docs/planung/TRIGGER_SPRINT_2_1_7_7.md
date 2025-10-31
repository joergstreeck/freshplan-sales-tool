# 🚀 Sprint 2.1.7.7 - Multi-Location Management & Xentral Filial-Sync

**Sprint-ID:** 2.1.7.7
**Status:** 📋 PLANNING
**Priority:** P2 (Medium - Business Impact: Hotelketten!)
**Estimated Effort:** 16-19h (2-3 Arbeitstage)
**Owner:** TBD
**Created:** 2025-10-21
**Updated:** 2025-10-31 (D9 Customer UX Polish von Sprint 2.1.7.2 verschoben)
**Dependencies:** Sprint 2.1.7.4 COMPLETE, Sprint 2.1.7.2 COMPLETE

---

## 🎯 SPRINT GOALS

### **Business Value**

**Vertriebler können Filialbetriebe professionell managen - kritisch für Hotelketten!**

**Key Deliverables:**
- Filial-Anlage UI (Create Branch Dialog)
- Opportunity→Branch Tracking (pro Filiale!)
- Xentral Address-Matching Service (Lieferadresse → Filiale)
- Parent Hierarchy Dashboard (Roll-Up Metriken wie Salesforce)

**Business Impact:**
- **Hotelketten-Support** (NH Hotels, Motel One, etc.)
- **Bäckerei-Ketten** (Mehrere Standorte pro Stadt)
- **Restaurant-Gruppen** (Zentral-Einkauf, Filial-Tracking)
- **Filial-spezifische Opportunities** (Sortimentserweiterung nur in Berlin)

### **User-Anforderung (2025-10-21)**

> "Wir haben oft Filialbetriebe, besonders Hotelketten"

**Kritische Fragen geklärt:**
1. **Customer-Anlage:** Wie legt Vertriebler Filialbetriebe nutzerfreundlich an?
2. **Lead-Conversion:** Ist der konvertierte Lead Hauptbetrieb oder Filiale?
3. **Xentral-Umsatz:** Wie ordnen wir Umsatz pro Filiale zu?
4. **Opportunities:** Pro Filiale oder pro Kette?

**Xentral-Realität:**
- ❌ **KEINE Filial-ID** in Xentral
- ✅ **Gleiche Kundennummer** für alle Filialen (z.B. `56037`)
- ✅ **Unterscheidung:** Nur über **Lieferadresse** (Fuzzy-Matching!)

### **Technical Context**

**Status Quo (nach Sprint 2.1.7.4):**
- ✅ Customer-Hierarchie Support (`parentCustomer`, `childCustomers`)
- ✅ CustomerHierarchyType Enum (HEADQUARTER, FILIALE, FRANCHISE, STANDALONE)
- ✅ CustomerLocation Entity (locations per Customer)
- ✅ `branchCount`, `isChain` Metadaten aus Lead
- ❌ **KEIN** Filial-Anlage UI
- ❌ **KEIN** Xentral-Filial-Mapping
- ❌ **KEIN** Parent Hierarchy Dashboard

**Architektur-Entscheidung (User-validated):**
- ✅ **Option A: Separate Customers** (CRM Best Practice)
- Jede Filiale = Customer-Eintrag (mit `parentCustomer` Link)
- Opportunities direkt an Branch-Customer (kein `location_id` nötig)
- Salesforce/HubSpot-Pattern (Account Hierarchy)

---

## 📦 DELIVERABLES

### **D9: Customer UX Polish** (4h) 🎨

**Ziel:** Customer Wizard + Dashboard UX Review und Pain-Points beheben

**Status:** 📋 PLANNING (Verschoben von Sprint 2.1.7.2)

**Scope:**
- [x] Customer Wizard UX Review (Pain Points identifizieren)
- [x] Multi-Contact Support (mehrere Ansprechpartner pro Customer)
- [x] Customer Dashboard Polish (Layout + Informationsarchitektur)
- [x] Wizard-Validation verbessern (Fehlermeldungen UX)
- [x] Field Tooltips ergänzen (Hilfe-Texte für komplexe Felder)

**Frontend:**
- [x] CustomerWizard Component Review + Pain-Point Fixes
- [x] Multi-Contact Widget (Add/Remove/Edit Contacts)
- [x] CustomerDashboard Layout Improvements
- [x] Form Validation Messages (UX-optimiert)

**Tests:** 6 Tests (4 Frontend + 2 Integration)

**Begründung:**
- D9 passt besser zu Sprint 2.1.7.7 (UI/UX Focus)
- Kombiniert gut mit Multi-Location UX (CreateBranchDialog, HierarchyDashboard)
- Sprint 2.1.7.2 fokussiert auf Core Xentral Integration (100% Complete)

---

### **1. CreateBranchDialog Component** (4h)

**Ziel:** Vertriebler kann Filialen nutzerfreundlich anlegen

**Frontend:**
- [x] CreateBranchDialog Component (MUI Dialog)
- [x] Filial-Formular (Name, Adresse, Filialleiter)
- [x] Info-Alert: "Gleiche Xentral-Kundennummer wie Hauptbetrieb"
- [x] API Integration: POST /api/customers/{id}/branches

**Backend:**
- [x] POST /api/customers/{parentId}/branches Endpoint
- [x] BranchService.createBranch() Implementierung
- [x] Validation (parentCustomer muss HEADQUARTER sein)

**Tests:** 8 Tests (5 Frontend + 3 Backend)

---

### **2. XentralAddressMatcher Service** (4h)

**Ziel:** Lieferadressen aus Xentral automatisch Filialen zuordnen

**Backend:**
- [x] XentralAddressMatcher Service
- [x] Fuzzy-Matching (Levenshtein Distance, 80% Threshold)
- [x] Fallback auf Main Location (wenn kein Match)
- [x] Logging + Warnings (für manuelle Nachbearbeitung)

**Integration:**
- [x] CustomerRevenueService Integration
- [x] Invoice → Branch Mapping
- [x] Revenue-Aggregation pro Branch

**Tests:** 10 Tests (Fuzzy-Matching + Fallback + Integration)

---

### **3. HierarchyMetricsService** (2h)

**Ziel:** Parent Dashboard zeigt aggregierte Metriken (Salesforce Roll-Up Pattern)

**Backend:**
- [x] HierarchyMetricsService.getHierarchyMetrics()
- [x] GET /api/customers/{id}/hierarchy-metrics
- [x] Revenue Aggregation (SUM aller Branches)
- [x] Percentage Calculation pro Branch

**Tests:** 5 Tests (Aggregation + Edge Cases)

---

### **4. HierarchyDashboard Components** (3h)

**Ziel:** Parent Customer zeigt Gesamt-Metriken + Branch-Übersicht

**Frontend:**
- [x] HierarchyDashboard Component (Tab "Filialen")
- [x] Gesamt-Metriken Cards (Total Revenue, Branch Count)
- [x] Branch-Tabelle (sortiert nach Revenue)
- [x] HierarchyTreeView Component (Tree-Struktur optional)

**Tests:** 6 Tests (Dashboard + TreeView)

---

### **5. Opportunity→Branch Integration** (1h)

**Ziel:** Opportunity direkt an Branch-Customer zuordnen (kein neues Feld!)

**Tasks:**
- [x] CreateOpportunityDialog: Branch-Dropdown (wenn Parent HEADQUARTER)
- [x] Opportunity-List: Branch-Name anzeigen (nicht nur Parent)
- [x] OpportunityDetailPage: Branch-Link

**Tests:** 3 Tests (Dropdown + Navigation)

---

### **6. Documentation & Testing** (2h)

**Ziel:** Integration Testing + E2E Testing

**Tasks:**
- [x] Backend Integration Tests (5 Tests)
- [x] Frontend Integration Tests (5 Tests)
- [x] E2E: Filial-Anlage → Opportunity → Xentral-Sync

---

## 📊 SUCCESS METRICS

**Test Coverage:**
- Backend: 23 Tests (BranchService 3 + AddressMatcher 10 + HierarchyMetrics 5 + Integration 5)
- Frontend: 25 Tests (D9 UX Polish 6 + CreateBranchDialog 5 + HierarchyDashboard 6 + TreeView 3 + Integration 5)
- **Total: 48 Tests**

**Code Changes:**
- 0 Migrations (Option A braucht keine neuen Felder!)
- 3 Backend Services (BranchService, XentralAddressMatcher, HierarchyMetricsService)
- 6 Frontend Components (D9: CustomerWizard+MultiContact 2 + CreateBranchDialog, HierarchyDashboard, HierarchyTreeView, BranchDropdown)

**Business Impact:**
- ✅ Hotelketten-Support (kritisch!)
- ✅ Filial-spezifische Opportunities
- ✅ Umsatz-Tracking pro Standort
- ✅ Xentral-Sync trotz fehlender Filial-ID

---

## ✅ DEFINITION OF DONE

### **Functional**
- [ ] Filial-Anlage UI funktioniert (CreateBranchDialog)
- [ ] Opportunity→Branch Dropdown funktioniert
- [ ] Xentral Address-Matching Service deployed
- [ ] Parent Dashboard zeigt Filial-Umsätze (Roll-Up)
- [ ] HierarchyTreeView zeigt Filial-Struktur

### **Technical**
- [ ] BranchService implementiert
- [ ] XentralAddressMatcher implementiert
- [ ] Fuzzy-Matching mit 80% Threshold
- [ ] Fallback auf Main Location
- [ ] HierarchyMetricsService (Roll-Up Aggregation)

### **Quality**
- [ ] Tests: 42/42 GREEN
- [ ] TypeScript: type-check PASSED
- [ ] Code Review: Self-reviewed
- [ ] Performance: Address-Matching < 100ms
- [ ] Documentation: Updated

---

## 📅 TIMELINE

**Tag 1 (8h):**
- D9: Customer UX Polish (4h)
- CreateBranchDialog Component (4h)

**Tag 2 (8h):**
- XentralAddressMatcher Service (4h)
- HierarchyMetricsService (2h)
- HierarchyDashboard Components (2h)

**Tag 3 (3h):**
- HierarchyDashboard Components (1h fortsetzen)
- Opportunity→Branch Integration (1h)
- Testing & Documentation (1h)

**Total:** 19h (2-3 Arbeitstage)

---

## 📄 ARTEFAKTE

**Technische Spezifikation:**
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_7_TECHNICAL.md`
- BranchService Implementation (vollständig)
- XentralAddressMatcher Service (Fuzzy-Matching 80%)
- HierarchyMetricsService (Salesforce Roll-Up Pattern)
- Frontend Components (CreateBranchDialog, HierarchyDashboard, TreeView)
- API Specifications (POST /branches, GET /hierarchy-metrics)
- Test Specifications (42 Tests total)

**Design Decisions:**
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_7_DESIGN_DECISIONS.md`
- **Option A vs. Option B** (CRM Best Practice)
- **Parent Hierarchy Dashboard** (Salesforce Roll-Up Pattern)
- **Xentral Address-Matching** (Fuzzy-Matching Strategie)
- **Opportunity→Customer Mapping** (kein location_id nötig!)
- **Lead→Customer Conversion** bei Filialen
- **Dashboard-Filter** "Nur Hauptbetriebe"

**Umsatz-Konzept:**
→ `/docs/planung/artefakte/UMSATZ_KONZEPT_DECISION.md`
- 3-Phasen-Modell (Lead → Customer → Xentral)
- Revenue-Tracking pro Filiale

**Design System:**
→ `/docs/planung/grundlagen/DESIGN_SYSTEM.md`
- Freshfoodz Color Palette (#94C456, #004F7B)
- Typography (Antonio Bold, Poppins)
- Component Patterns

---

## 🚀 PREREQUISITES

### **Dependencies:**
- ✅ Sprint 2.1.7.4 COMPLETE (hierarchyType Foundation)
- ✅ Sprint 2.1.7.2 COMPLETE (Xentral Integration)

### **Architektur-Entscheidung (GETROFFEN - 2025-10-21):**

**Option A: Separate Customers** (CRM Best Practice)
- ✅ User-Entscheidung: "dann bauen wir das nach Option A"
- ✅ Jede Filiale = Customer-Eintrag (mit `parentCustomer` Link)
- ✅ Opportunities direkt an Branch-Customer
- ✅ Salesforce/HubSpot-Pattern (Account Hierarchy)
- ✅ Keine Migration nötig (kein neues `location_id` Feld)

---

## 🎯 NÄCHSTE SCHRITTE

**Sprint-Reihenfolge:**

```
✅ Sprint 2.1.7.1 COMPLETE (Lead → Opportunity UI)
✅ Sprint 2.1.7.3 COMPLETE (Customer → Opportunity Workflow)
   ↓
📋 Sprint 2.1.7.4 (Customer Status Architecture) ← AKTUELL
   ↓
📋 Sprint 2.1.7.2 (Xentral Integration)
   ↓
📋 Sprint 2.1.7.5 (Opportunity Management KOMPLETT)
   ↓
📋 Sprint 2.1.7.6 (Customer Lifecycle - PROSPECT Warnings)
   ↓
📋 Sprint 2.1.7.7 (Multi-Location Management) ← HIER! 🎯
```

**Warum Sprint 2.1.7.7 nach 2.1.7.6?**
- Abhängig von Sprint 2.1.7.4 (hierarchyType) ✅
- Abhängig von Sprint 2.1.7.2 (Xentral-Sync) ✅
- Unabhängig von Opportunity/Lifecycle-Features
- Kann parallel zu 2.1.7.5/6 vorbereitet werden

**Vor Sprint-Start:**
1. ✅ Architektur-Entscheidung getroffen (Option A)
2. ✅ Technical Specification erstellt
3. ✅ Design Decisions dokumentiert

---

## 📝 NOTES

### **Warum WICHTIG für Food-Branche?**

**Hotelketten:**
- NH Hotels: 20+ Standorte in Deutschland
- Motel One: 50+ Hotels europaweit
- Zentral-Einkauf, aber Filial-Tracking nötig

**Bäckerei-Ketten:**
- Lokale Ketten: 5-15 Filialen pro Stadt
- Jede Filiale = eigener Opportunity-Bedarf

**Restaurant-Gruppen:**
- Mehrere Konzepte (Italiener, Burger, Sushi)
- Zentrale Verwaltung, aber Standort-spezifisches Sortiment

### **Xentral-Realität akzeptiert**

**Keine Filial-ID = kein Problem!**
- Fuzzy Address-Matching ist Industrie-Standard
- Fallback auf Main Location ist pragmatisch
- 80% Accuracy reicht für Food-Branche

### **Technical Debt**

**Potenzielle Verbesserungen (später):**
- Machine Learning für Address-Matching (95%+ Accuracy)
- Xentral Custom-Field "filial_id" (wenn möglich)
- Geo-Coding für Location-Matching (Google Maps API)

---

**✅ SPRINT STATUS: 📋 READY TO START - Architektur-Entscheidung getroffen (Option A)**

**Letzte Aktualisierung:** 2025-10-21 (Gekürzt auf ~250 Zeilen, Artefakte referenziert)
