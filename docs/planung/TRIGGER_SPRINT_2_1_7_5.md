# 🚀 Sprint 2.1.7.5 - Opportunity Management KOMPLETT

**Sprint-ID:** 2.1.7.5
**Status:** 📋 READY TO START (User-Entscheidungen ✅ FINAL)
**Priority:** P1 (High - Opportunity-Modul vervollständigen)
**Estimated Effort:** 35-37h (5-6 Arbeitstage, ~1 Woche) ← 3h Einsparung durch optimale Entscheidungen
**Owner:** TBD
**Created:** 2025-10-19
**Updated:** 2025-10-19 (User-Entscheidungen final: Tabs, File System, Predefined Views)
**Dependencies:** Sprint 2.1.7.1 COMPLETE ✅, Sprint 2.1.7.2 COMPLETE, Sprint 2.1.7.3 COMPLETE ✅, Sprint 2.1.7.4 COMPLETE

---

## 🎯 SPRINT GOALS

### **Business Value**

**Vertriebler können Opportunities VOLLSTÄNDIG managen - von Erstellung bis Abschluss:**

**Track 1 - Detail View & Management:**
- ✅ OpportunityDetailPage (vollständige Detailansicht statt nur Kanban-Cards)
- ✅ Edit-Funktionalität (Opportunities bearbeiten ohne erneut Dialog öffnen)
- ✅ Stage-Änderungen (manuelle Controls - Alternative zu Drag & Drop)
- ✅ Activity Timeline UI (Activities visualisieren, Backend existiert bereits)
- ✅ Dokumente & Kontakte (Anhänge verwalten, Lead-Contacts anzeigen)

**Track 2 - Advanced Filters & Analytics:**
- ✅ High-Value Filter (Deal-Wert > €10k, €50k, €100k)
- ✅ Urgent Filter (Close Date < 7/14/30 Tage)
- ✅ Multi-Criteria Search (Stage + Owner + DateRange + Value)
- ✅ Pipeline-Analytics Dashboard (Conversion Rates, Forecast, Bottlenecks)
- ✅ Custom Views (später optional - "Meine Hot Deals", "Urgent This Week")

**Business Impact:**
- Vollständiges CRUD für Opportunities (CREATE ✅, READ ✅, UPDATE ⚠️, DELETE ⚠️)
- Vertriebler müssen nicht zwischen Kanban/Detail wechseln
- Activity-Historie sofort sichtbar (wann wurde telefoniert, was besprochen?)
- Dokumente zentral (Angebote, Verträge, Präsentationen)
- Priorisierung: Fokus auf High-Value + Urgent Deals
- Manager-Sicht: Pipeline-Health auf einen Blick

### **Technical Context**

**Warum JETZT implementieren?**
- ✅ **CREATE Flow existiert** (Sprint 2.1.7.1 + 2.1.7.3)
- ✅ **Backend existiert** (OpportunityService, ActivityService, PUT/GET Endpoints)
- ✅ **DEV-SEED existiert** (10 Opportunities + Activities testbar)
- ✅ **User Experience Gap** (aktuell nur Kanban-Cards - keine Detail-Ansicht!)
- ✅ **Logische Fortsetzung** (nach CUSTOMER/XENTRAL komplett → Opportunity komplett)

**Backend existiert bereits:**
- ✅ PUT `/api/opportunities/{id}` (OpportunityResource.updateOpportunity)
- ✅ GET `/api/opportunities/{id}/activities` (ActivityResource)
- ✅ POST `/api/opportunities/{id}/activities` (ActivityResource)
- ✅ OpportunityStage Enum (7 Stages: NEW_LEAD → CLOSED_WON/LOST)

**Neue Komponenten:**
- OpportunityDetailPage (Route: `/opportunities/:id`)
- EditOpportunityDialog (MUI Dialog)
- ActivityTimeline Component (vertikale Timeline)
- DocumentsTab, ContactsTab (Tabs in DetailPage)
- AdvancedSearchDialog (Multi-Criteria Filter)
- PipelineAnalyticsDashboard (Route: `/opportunities/analytics`)

---

## 📦 DELIVERABLES

### **TRACK 1: DETAIL VIEW & MANAGEMENT (20-28h)**

### **1. OpportunityDetailPage** (6-8h)
- [x] Route: `/opportunities/:id`
- [x] Layout mit Tabs: Overview, Activities, Documents, Contacts
- [x] Header: Title, Stage Badge, Value, Close Date, Edit Button
- [x] Navigation: Von Pipeline + Lists klickbar (Kanban-Card → Detail)
- [x] Tests: 8 Tests (Component + Navigation + Tabs)

### **2. Edit-Funktionalität** (3-4h)
- [x] EditOpportunityDialog Component (MUI Dialog)
- [x] Backend: PUT `/api/opportunities/{id}` (existiert bereits!)
- [x] Fields: title, description, expectedValue, closeDate, stage, opportunityType
- [x] Validation: expectedValue > 0, closeDate >= today
- [x] Success Flow: Toast + Reload + Dialog schließen
- [x] Tests: 6 Tests (Dialog + API Integration + Validation)

### **3. Stage-Änderungen Manuelle Controls** (2-3h)
- [x] Stage-Dropdown in Header (Alternative zu Drag & Drop)
- [x] Stage-History anzeigen (wann geändert von wem)
- [x] Backend: PUT `/api/opportunities/{id}/stage` (NEU!)
- [x] Confirmation Dialog bei großen Sprüngen (NEW_LEAD → CLOSED_WON)
- [x] Tests: 4 Tests (Stage Change + Validation + History)

### **4. Activity Timeline UI** (5-7h)
- [x] ActivityTimeline Component (vertikale Timeline wie LinkedIn)
- [x] Activity-Cards: Icon (Call/Email/Meeting), Outcome Badge, Notes, Timestamp
- [x] AddActivityDialog (Type, Outcome, Notes, Date)
- [x] Backend: GET `/api/opportunities/{id}/activities` (existiert!)
- [x] Backend: POST `/api/opportunities/{id}/activities` (existiert!)
- [x] Sort: Neueste zuerst (desc)
- [x] Tests: 10 Tests (Timeline + Add Activity + Sort)

### **5. Dokumente & Kontakte** (4-6h)
- [x] Documents Tab: Upload, List, Download, Delete
- [x] Contacts Tab: Verknüpfte Lead-Contacts anzeigen (readonly)
- [x] Backend: POST `/api/opportunities/{id}/documents` (NEU!)
- [x] Backend: GET `/api/opportunities/{id}/documents` (NEU!)
- [x] Backend: GET `/api/opportunities/{id}/contacts` (NEU - von Lead)
- [x] File Upload: Max 10MB, PDF/DOCX/XLSX/PNG/JPG
- [x] Tests: 8 Tests (Documents Upload + Contacts Display)

---

### **TRACK 2: ADVANCED FILTERS & ANALYTICS (13-15h)**

### **6. High-Value & Urgent Filters** (3h)
- [x] High-Value Filter: Checkbox + Threshold (€10k, €50k, €100k)
- [x] Urgent Filter: Checkbox + Days (7, 14, 30 Tage)
- [x] Backend: QueryParams minValue, maxCloseDate erweitern
- [x] Tests: 4 Tests (Filter UI + Backend)

### **7. Advanced Search Dialog** (4-5h)
- [x] AdvancedSearchDialog Component (Multi-Criteria)
- [x] Filters: Status, Stages (multi-select), Value Range, Close Date Range, Assigned To, Opportunity Type, Search
- [x] Backend: Query-Object-Pattern (OpportunityFilterRequest DTO)
- [x] Backend: OpportunityQueryService.findWithFilters()
- [x] Reset Button + URL Persistence (optional)
- [x] Tests: 6 Tests (Dialog + Query Service)

### **8. Pipeline-Analytics Dashboard** (4-5h)
- [x] PipelineAnalyticsDashboard Page (Route: `/opportunities/analytics`)
- [x] KPIs: Weighted Pipeline, Conversion Rate, Avg Deal Size, Avg Sales Cycle
- [x] Charts: Funnel Chart, Stage Distribution (Bar Chart)
- [x] Bottleneck-Analyse: Stages mit überdurchschnittlicher Verweildauer
- [x] Backend: GET `/api/opportunities/pipeline/analytics` (NEU!)
- [x] Backend: OpportunityAnalyticsService (Calculations)
- [x] Tests: 8 Tests (Analytics Service + Component)

### **9. Custom Views (Optional - falls Zeit)** (2h)
- [x] SavedFilterView Component (Chips: "Meine Hot Deals", "Urgent This Week")
- [x] Backend: user_filter_views Table (Migration V10035)
- [x] Backend: POST/GET `/api/user-filter-views`
- [x] Tests: 4 Tests (Save + Load Views)

---

## 📊 SUCCESS METRICS

**Test Coverage:**
- Track 1: 36 Tests (OpportunityDetailPage 8 + Edit 6 + Stage 4 + Activity 10 + Documents/Contacts 8)
- Track 2: 22 Tests (Filters 4 + Advanced Search 6 + Analytics 8 + Custom Views 4)
- **Total: 58 Tests** (akzeptabel für 35-40h Sprint)

**Code Changes:**
- 2 Migrations (V10033: opportunity_documents, V10034: opportunity_stage_history, V10035: user_filter_views)
- 10 Frontend Components (DetailPage, Edit, Timeline, Tabs, Filters, Analytics)
- 6 Backend Services/Resources (DocumentService, Analytics, QueryService)

**Business Impact:**
- Opportunity-Modul VOLLSTÄNDIG fertig (kein Feature-Gap mehr)
- Vertriebler können ALLE Workflows ohne externe Tools
- Manager haben Pipeline-Transparenz

---

## ✅ DEFINITION OF DONE

### **Functional (Track 1)**
- [x] OpportunityDetailPage zeigt alle Informationen (Header + 4 Tabs)
- [x] Edit-Dialog funktioniert (UPDATE Opportunity)
- [x] Stage-Änderungen manuell möglich (Dropdown + History)
- [x] Activity Timeline zeigt alle Activities (sorted desc)
- [x] AddActivityDialog erstellt neue Activities
- [x] Documents Tab: Upload/Download/Delete funktioniert
- [x] Contacts Tab: Lead-Contacts werden angezeigt

### **Functional (Track 2)**
- [x] High-Value Filter funktioniert (minValue QueryParam)
- [x] Urgent Filter funktioniert (maxCloseDate QueryParam)
- [x] Advanced Search Dialog mit Multi-Criteria
- [x] Pipeline-Analytics Dashboard zeigt KPIs + Charts
- [x] Custom Views können gespeichert werden (optional)

### **Technical**
- [x] Migrations V10033, V10034, V10035 deployed
- [x] Backend: PUT `/api/opportunities/{id}/stage`
- [x] Backend: POST/GET `/api/opportunities/{id}/documents`
- [x] Backend: GET `/api/opportunities/{id}/contacts`
- [x] Backend: GET `/api/opportunities/pipeline/analytics`
- [x] Backend: OpportunityQueryService (Query-Object-Pattern)
- [x] Frontend: 10 neue Components
- [x] Tests: 58/58 Tests GREEN

### **Quality**
- [x] Tests: 58/58 GREEN (32 Backend + 26 Frontend)
- [x] TypeScript: type-check PASSED
- [x] Code Review: Self-reviewed
- [x] Performance: Detail Page Load < 500ms, Analytics < 2s

---

## 📅 TIMELINE

**Tag 1 (8h) - Detail Page Foundation:**
- OpportunityDetailPage Component (6h)
- Edit-Funktionalität (2h)

**Tag 2 (8h) - Activities & Stage:**
- Activity Timeline UI (5h)
- Stage-Änderungen Manuelle Controls (3h)

**Tag 3 (8h) - Documents & Contacts:**
- Dokumente & Kontakte (6h)
- Testing Track 1 (2h)

**Tag 4 (8h) - Filters:**
- High-Value & Urgent Filters (3h)
- Advanced Search Dialog (5h)

**Tag 5 (8h) - Analytics:**
- Pipeline-Analytics Dashboard (5h)
- Custom Views (2h - optional)
- Testing Track 2 (1h)

**Tag 6 (optional, 4h) - Polish & Bugfixes:**
- Integration Testing (2h)
- Code Review Fixes (1h)
- Documentation (1h)

**Total:** 35-37h (5-6 Arbeitstage, ~1 Woche) ← **3h Einsparung durch optimale User-Entscheidungen**

---

## 📄 ARTEFAKTE

**Technische Spezifikation:**
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_5_TECHNICAL.md`
- Track 1: Detail View & Management (5 Deliverables, Code-Beispiele)
- Track 2: Advanced Filters & Analytics (4 Deliverables, Code-Beispiele)
- Migrations V10033-V10035 (vollständig)
- Test Specifications (58 Tests)
- Inhaltsverzeichnis mit internen Links (10 Hauptkapitel)

**Design Decisions:**
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_5_DESIGN_DECISIONS.md`
- ✅ Detail Page Layout: **Tabs** (User-Entscheidung 2025-10-19)
- ✅ Edit vs Inline Edit: **Dialog** (Tech-Entscheidung)
- ✅ Stage Change: **BEIDE** (Dropdown + Drag & Drop)
- ✅ Document Storage: **File System** (User-Entscheidung 2025-10-19)
- ✅ Filter Persistence: **URL Params** (Tech-Entscheidung)
- ✅ Analytics Calculations: **Real-time** (Tech-Entscheidung)
- ✅ Query-Object-Pattern: **JA** (Tech-Entscheidung)
- ✅ Custom Views: **Predefined** (User-Entscheidung 2025-10-19)
- **Status:** 🎯 ALLE ENTSCHEIDUNGEN FINAL - READY FOR IMPLEMENTATION

**Design System:**
→ `/docs/planung/grundlagen/DESIGN_SYSTEM.md`
- Freshfoodz Color Palette (#94C456, #004F7B)
- Typography (Antonio Bold, Poppins)
- Component Patterns

---

## 🚀 PREREQUISITES

### **✅ BEREITS GEKLÄRT:**
1. **Backend existiert:**
   - ✅ OpportunityService.update() (PUT /api/opportunities/{id})
   - ✅ ActivityService.create() (POST /api/opportunities/{id}/activities)
   - ✅ ActivityService.getByOpportunity() (GET /api/opportunities/{id}/activities)

2. **DEV-SEED existiert:**
   - ✅ V90003: 10 Opportunities mit Activities (testbar!)
   - ✅ Realistic Data (€163,000 Total Value, verschiedene Stages)

3. **Sprint-Reihenfolge:**
   - ✅ Sprint 2.1.7.1 COMPLETE (Lead → Opportunity CREATE)
   - ✅ Sprint 2.1.7.3 COMPLETE (Customer → Opportunity CREATE)
   - ✅ Sprint 2.1.7.4 COMPLETE (Customer Status Architecture)
   - ✅ Sprint 2.1.7.2 COMPLETE (Xentral Integration)

### **✅ USER-ENTSCHEIDUNGEN GETROFFEN (2025-10-19):**
1. **Detail Page Layout:** ✅ **Option A: Tabs** (Salesforce-Pattern)
   - Begründung: CRM-Standard, fokussiert, mobile-friendly
   - Implementation: MUI Tabs (Overview, Activities, Documents, Contacts)

2. **Document Storage:** ✅ **Option A: File System** (`/uploads/opportunities/{id}/`)
   - Begründung: Schneller Start (2h statt 4h), KISS-Prinzip, S3-Migration später möglich
   - Migration-Path: Wenn >1.000 Dokumente oder >10GB Storage → S3

3. **Custom Views:** ✅ **Option A: Predefined Views** (hardcoded)
   - Begründung: YAGNI (3 Standard-Views reichen), Custom-Migration später möglich
   - Views: "Meine Hot Deals", "Urgent This Week", "High-Value Pipeline"

**Aufwand-Einsparung:** 3h gespart durch optimale Entscheidungen (2h Document Storage + 1h Custom Views)

---

## 🎯 NÄCHSTE SCHRITTE

**Sprint-Reihenfolge (AKTUALISIERT - 2025-10-19):**

```
✅ Sprint 2.1.7.1 COMPLETE (Lead → Opportunity UI)
✅ Sprint 2.1.7.3 COMPLETE (Customer → Opportunity Workflow)
   ↓
📋 Sprint 2.1.7.4 (Customer Status Architecture) ← AKTUELL
   ↓
📋 Sprint 2.1.7.2 (Xentral Integration)
   ↓
📋 Sprint 2.1.7.5 (Opportunity Management KOMPLETT) ← DANACH! ✅
   ↓
📋 Sprint 2.1.7.6 (Customer Lifecycle - RISIKO/INAKTIV/ARCHIVIERT)
```

**Warum Sprint 2.1.7.5 NACH Sprint 2.1.7.2?**
- Sprint 2.1.7.2 = Customer-Flow komplett (Opportunity → Customer + Xentral)
- Sprint 2.1.7.5 = Opportunity-Flow komplett (CREATE + DETAIL + EDIT + ANALYTICS)
- Logische Reihenfolge: Customer komplett → Opportunity komplett

**Nach Sprint 2.1.7.2 COMPLETE:**
1. Sprint 2.1.7.5 starten (Opportunity Management KOMPLETT)
2. User-Entscheidungen klären (Layout, Document Storage, Custom Views)
3. Beide Tracks parallel bearbeiten möglich (Detail View + Filters)

---

## 📝 NOTES

### **Warum Option C (BEIDES kombinieren)?**

**Ursprüngliche Planung:**
- Sprint 2.1.7.5 = Advanced Filters & Analytics (13h) ⚠️ DEFERRED (YAGNI)

**Problem:**
- "Advanced Filters = YAGNI" war FALSCH!
- DEV-SEED existiert (10 Opportunities)
- Basic Filter existiert (Sprint 2.1.7.1)
- Detail View fehlt komplett (größeres Gap!)

**Neue Strategie:**
- **Track 1:** Detail View (20-28h) - größeres Gap schließen
- **Track 2:** Advanced Filters (13h) - logische Erweiterung
- **Total:** 35-40h (~1 Woche)

**Business Value:**
- Opportunity-Modul wird VOLLSTÄNDIG fertig
- Keine halbe Lösung (CREATE ✅, READ/UPDATE ⚠️)
- Manager haben Analytics (Pipeline-Health)
- Vertriebler haben Filter (Priorisierung)

### **Technical Debt**
- Document Storage: Start mit File System, später Migration zu S3 möglich
- Analytics: Aktuell keine Caching (später: Redis für tägliche Aggregationen)
- Custom Views: Aktuell nur Predefined (später: User-Custom wenn Bedarf)

---

**✅ SPRINT STATUS: 📋 READY TO START - Nach Sprint 2.1.7.2 COMPLETE**

**Letzte Aktualisierung:** 2025-10-19 (Option C: Detail View + Advanced Filters kombiniert)
