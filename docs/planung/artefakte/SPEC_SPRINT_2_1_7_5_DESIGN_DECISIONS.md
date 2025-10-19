# 🎯 Sprint 2.1.7.5 - Design Decisions

**Sprint-ID:** 2.1.7.5
**Titel:** Opportunity Management KOMPLETT (Detail View + Advanced Filters)
**Erstellt:** 2025-10-19
**Status:** READY TO START

---

## 📋 Übersicht

Dieses Dokument beschreibt alle wichtigen Architekturentscheidungen für Sprint 2.1.7.5. Einige Entscheidungen erfordern **User-Validierung** vor Start.

---

## 1️⃣ Detail Page Layout - Tabs vs Sections ✅ **USER-ENTSCHEIDUNG: OPTION A (TABS)**

### **Problem**

OpportunityDetailPage muss 4 Bereiche darstellen:
1. Overview (Details, Metadata)
2. Activities (Timeline)
3. Documents (Upload/Download)
4. Contacts (Lead-Contacts)

**Frage:** Wie strukturieren wir die DetailPage?

### **Optionen**

**Option A: Tabs (MUI Tabs)** ← **✅ GEWÄHLT (2025-10-19)**
```tsx
<Tabs>
  <Tab label="Übersicht" />
  <Tab label="Aktivitäten" />
  <Tab label="Dokumente" />
  <Tab label="Kontakte" />
</Tabs>
```

**Pro:**
- ✅ Weniger Scroll (nur aktiver Tab sichtbar)
- ✅ Fokussiert (User sieht nur relevanten Bereich)
- ✅ Standard-Pattern (z.B. Salesforce, HubSpot verwenden Tabs)
- ✅ Mobile-Friendly (weniger vertikal scrollen)

**Con:**
- ⚠️ Keine Gesamtübersicht auf einen Blick
- ⚠️ Wechsel zwischen Tabs erforderlich

---

**Option B: Sections (Scroll-basiert)**
```tsx
<Stack spacing={3}>
  <OverviewSection />
  <ActivitiesSection />
  <DocumentsSection />
  <ContactsSection />
</Stack>
```

**Pro:**
- ✅ Alles auf einen Blick (kein Tab-Wechsel)
- ✅ Einfacher zu implementieren
- ✅ Scroll-basiert (gewohnte UX)

**Con:**
- ⚠️ Viel Scroll (lange Seite)
- ⚠️ Unübersichtlich bei vielen Activities/Documents
- ⚠️ Performance-Impact (alles gleichzeitig laden)

---

**Option C: Accordion (Expandable Sections)**
```tsx
<Accordion defaultExpanded>
  <AccordionSummary>Übersicht</AccordionSummary>
  <AccordionDetails>...</AccordionDetails>
</Accordion>
```

**Pro:**
- ✅ Kompakt (collapsed Sections)
- ✅ Alles auf einer Seite

**Con:**
- ⚠️ Extra Klick zum Expandieren
- ⚠️ Weniger Standard-Pattern

---

### **✅ FINALE ENTSCHEIDUNG: Option A (Tabs)**

**Entschieden am:** 2025-10-19
**Entschieden von:** User (Jörg)

**Begründung:**
- ✅ Standard-Pattern in CRM-Systemen (Salesforce, HubSpot, Pipedrive)
- ✅ Fokussiert: User sieht nur relevanten Bereich
- ✅ Mobile-Friendly
- ✅ Performance: Lazy Loading möglich (nur aktiver Tab lädt)
- ✅ Verkäufer-Feedback: "Weniger Ablenkung, schneller Überblick"

**Referenz:**
- Salesforce: Tabs (Details, Activity, Chatter, Related)
- HubSpot: Tabs (About, Activity, Deals, Tickets)

**Implementation:**
```tsx
<Tabs value={currentTab} onChange={(e, val) => setCurrentTab(val)}>
  <Tab label="Übersicht" />
  <Tab label="Aktivitäten" />
  <Tab label="Dokumente" />
  <Tab label="Kontakte" />
</Tabs>
```

---

## 2️⃣ Edit vs Inline Edit

### **Problem**

Wie bearbeitet User eine Opportunity?

### **Entscheidung:** Dialog-basiert (EditOpportunityDialog)

**Begründung:**
- ✅ **Konsistent:** Gleicher Dialog wie CREATE (CreateOpportunityDialog)
- ✅ **Validation:** Zentralisiert (nicht über Seite verteilt)
- ✅ **Undo:** Abbrechen-Button schließt Dialog (keine Änderungen)
- ✅ **Mobile-Friendly:** Dialog nimmt ganzen Screen
- ✅ **Weniger Fehler:** User sieht alle Felder auf einmal

**Alternative (Inline Edit):**
- ⚠️ Komplexer zu implementieren (Validation pro Feld)
- ⚠️ Undo schwieriger (User muss jeden Wert zurücksetzen)
- ⚠️ Mehr State Management (Dirty Flags pro Feld)

**Referenz:**
- HubSpot: Dialog-basiert
- Salesforce: Inline Edit + Dialog (Hybrid)

**Unser Ansatz:** Start mit Dialog, später Inline Edit optional

---

## 3️⃣ Stage Change - Dropdown vs Drag & Drop

### **Problem**

Opportunities haben bereits Drag & Drop in Kanban-Board. Brauchen wir zusätzlich Dropdown in DetailPage?

### **Entscheidung:** BEIDE (Dropdown + Drag & Drop)

**Begründung:**
- ✅ **Drag & Drop:** Visuelle Pipeline-Ansicht (Kanban bleibt!)
- ✅ **Dropdown:** Schnelle Änderung in DetailPage (ohne Kanban zu öffnen)
- ✅ **Use Case 1:** User ist in DetailPage → Dropdown schneller
- ✅ **Use Case 2:** User ist in Kanban → Drag & Drop intuitiver

**Implementation:**
- StageChangeDropdown Component (DetailPage Header)
- Confirmation bei großen Sprüngen (NEW_LEAD → CLOSED_WON)
- Stage History Display (Timeline zeigt alle Änderungen)

**Referenz:**
- Pipedrive: BEIDE (Kanban + Dropdown im Deal-Detail)
- HubSpot: BEIDE (Kanban + Dropdown)

---

## 4️⃣ Document Storage - File System vs S3 ✅ **USER-ENTSCHEIDUNG: OPTION A (FILE SYSTEM)**

### **Problem**

Wo speichern wir hochgeladene Dokumente (PDFs, DOCX, etc.)?

### **Optionen**

**Option A: File System (`/uploads/opportunities/{id}/`)** ← **✅ GEWÄHLT (2025-10-19)**

**Pro:**
- ✅ Einfach zu implementieren (kein externes System)
- ✅ Schneller Start (keine S3/MinIO Setup)
- ✅ Kein zusätzlicher Kostenfaktor (S3 = €€€)
- ✅ Backup einfach (rsync, tar.gz)

**Con:**
- ⚠️ Skalierung schwieriger (Disk-Space auf Server)
- ⚠️ Kein CDN (Downloads langsamer bei vielen Usern)
- ⚠️ Disaster Recovery komplexer (File-Restore aus Backup)

**Aufwand:** 2h (OpportunityDocumentService + File I/O)

---

**Option B: S3-kompatibel (MinIO für Start, später AWS S3)**

**Pro:**
- ✅ Skalierbar (unbegrenzter Storage)
- ✅ CDN möglich (CloudFront für schnelle Downloads)
- ✅ Disaster Recovery einfacher (S3 Versioning + Replication)
- ✅ Migration zu AWS S3 später einfach (MinIO ist S3-kompatibel)

**Con:**
- ⚠️ Komplexer Setup (MinIO Docker-Container + S3 SDK)
- ⚠️ Zeitaufwand höher (4h statt 2h)
- ⚠️ Kosten bei AWS S3 (später)

**Aufwand:** 4h (MinIO Setup + S3 SDK Integration)

---

**Option C: Database BLOB (nicht empfohlen)**

**Pro:**
- ✅ Einfach (kein File System, kein S3)

**Con:**
- ❌ Performance-Problem bei >1MB Dateien
- ❌ Database Backup wird riesig (10GB Opportunities + 50GB Dokumente)
- ❌ Nicht skalierbar

**Aufwand:** 1h (aber nicht empfohlen!)

---

### **✅ FINALE ENTSCHEIDUNG: Option A (File System) für START**

**Entschieden am:** 2025-10-19
**Entschieden von:** User (Jörg)

**Begründung:**
- ✅ Schneller Start (2h statt 4h)
- ✅ Keine externe Abhängigkeit (kein MinIO/S3 Setup)
- ✅ Einfach zu debuggen
- ✅ Reicht für 1-2 Jahre bei aktueller Nutzung
- ✅ Migration zu S3 später möglich (File Path → S3 Key)
- ✅ KISS-Prinzip: Keep It Simple, Stupid

**Implementation:**
```java
@ApplicationScoped
public class OpportunityDocumentService {
    @ConfigProperty(name = "upload.directory", defaultValue = "/uploads/opportunities")
    String uploadDirectory;

    public String saveFileToDisk(UUID opportunityId, String fileName, byte[] fileData) {
        Path directoryPath = Paths.get(uploadDirectory, opportunityId.toString());
        Files.createDirectories(directoryPath);
        Path filePath = directoryPath.resolve(fileName);
        Files.write(filePath, fileData);
        return filePath.toString();
    }
}
```

**Migration-Path zu S3 (später):**
```java
// Jetzt: File System
String filePath = "/uploads/opportunities/{id}/document.pdf";

// Später: S3 (wenn >1000 Dokumente oder >10GB Storage)
String s3Key = "opportunities/{id}/document.pdf";
```

**Wann migrieren zu S3?**
- Wenn >1.000 Dokumente (File System wird unübersichtlich)
- Wenn >10GB Storage (Disk-Space kritisch)
- Wenn CDN benötigt (viele parallele Downloads weltweit)

---

## 5️⃣ Filter Persistence - URL vs LocalStorage

### **Problem**

User setzt Filter (High-Value > €50k, Urgent < 14 Tage). Wie speichern wir diese Filter?

### **Entscheidung:** URL Query Parameters (nicht LocalStorage)

**Begründung:**
- ✅ **Shareable:** User kann URL kopieren und teilen (`/opportunities?minValue=50000&urgent=14`)
- ✅ **Bookmarkable:** Browser-Bookmark speichert Filter
- ✅ **Browser Back/Forward:** Navigation funktioniert korrekt
- ✅ **No State Sync:** Kein Sync zwischen URL und State nötig

**Example:**
```
/opportunities?status=active&minValue=50000&maxCloseDate=2025-11-01
```

**Alternative (LocalStorage):**
- ⚠️ Nicht shareable (URL bleibt gleich)
- ⚠️ Nicht bookmarkable
- ⚠️ State Sync komplexer

**Referenz:**
- Google Mail: URL Query Parameters (search=xyz)
- GitHub: URL Query Parameters (is:open label:bug)

---

## 6️⃣ Analytics Calculations - Real-time vs Cached

### **Problem**

Pipeline-Analytics Dashboard zeigt:
- Weighted Pipeline (€)
- Conversion Rate (%)
- Avg Deal Size (€)
- Avg Sales Cycle (Tage)

**Frage:** Real-time berechnen oder cachen?

### **Entscheidung:** Real-time (kein Cache) für START

**Begründung:**
- ✅ **Einfacher:** Keine Caching-Logik nötig
- ✅ **Korrekt:** Immer aktuellste Daten
- ✅ **Weniger Code:** Kein Cache-Invalidierung-Code
- ✅ **Performance OK:** Bei <500 Opportunities ist Query schnell (<1s)

**Implementation:**
```java
@GET
@Path("/pipeline/analytics")
public Response getPipelineAnalytics() {
    // Real-time Calculation
    BigDecimal weightedPipeline = opportunityRepository.calculateForecast();
    Double conversionRate = opportunityRepository.getConversionRate();
    // ...
}
```

**Performance:**
- <100 Opportunities: ~200ms
- 100-500 Opportunities: ~500ms
- >500 Opportunities: ~1-2s

**Wann cachen?**
- Wenn >500 Opportunities (Performance >2s)
- Wenn >100 Requests/Tag zum Analytics-Dashboard
- Dann: Redis Cache mit TTL 1h

**Migration-Path:**
```java
// Später: Mit Cache
@Cacheable(value = "pipeline-analytics", ttl = 3600)
public PipelineAnalyticsResponse getPipelineAnalytics() { ... }
```

---

## 7️⃣ Query-Object-Pattern - Statt viele QueryParams

### **Problem**

Advanced Search Dialog hat 8 Filter-Kriterien:
- Status, Stages (multi), Value Range, Close Date Range, Assigned To, Type, Search

**Alte Ansatz (viele QueryParams):**
```java
@GET
public Response getAllOpportunities(
    @QueryParam("status") String status,
    @QueryParam("stage") List<String> stages,
    @QueryParam("minValue") BigDecimal minValue,
    @QueryParam("maxValue") BigDecimal maxValue,
    @QueryParam("minCloseDate") String minCloseDate,
    @QueryParam("maxCloseDate") String maxCloseDate,
    @QueryParam("assignedTo") UUID assignedTo,
    @QueryParam("opportunityType") String opportunityType,
    @QueryParam("search") String search
) { ... }
```

**Probleme:**
- ⚠️ Zu viele Parameter (9!)
- ⚠️ Nicht erweiterbar (10. Parameter?)
- ⚠️ Validation komplex

### **Entscheidung:** Query-Object-Pattern

**Implementation:**
```java
@Data
@Builder
public class OpportunityFilterRequest {
    private String status;
    private List<OpportunityStage> stages;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private LocalDate minCloseDate;
    private LocalDate maxCloseDate;
    private UUID assignedTo;
    private String opportunityType;
    private String search;

    // Pagination
    private int page = 0;
    private int size = 20;
}

@POST
@Path("/search")
public Response searchOpportunities(@Valid OpportunityFilterRequest filter) {
    List<OpportunityResponse> results = opportunityQueryService.findWithFilters(filter);
    return Response.ok(results).build();
}
```

**Begründung:**
- ✅ **Sauberer:** Ein Parameter statt 9
- ✅ **Erweiterbar:** Neue Filter hinzufügen ohne Signatur zu ändern
- ✅ **Validation:** @Valid annotiert, Bean Validation möglich
- ✅ **Testbar:** Einfacher zu mocken

**Referenz:**
- Spring Data JPA: Specification Pattern
- CQRS: Query Objects

---

## 8️⃣ Custom Views - Predefined vs User-Custom ✅ **USER-ENTSCHEIDUNG: OPTION A (PREDEFINED)**

### **Problem**

Soll User eigene Filter-Views speichern können?

### **Optionen**

**Option A: Nur Predefined Views (hardcoded)** ← **✅ GEWÄHLT (2025-10-19)**

**Views:**
- "Meine Hot Deals" (assignedTo = currentUser, minValue = €10k)
- "Urgent This Week" (maxCloseDate = today + 7 Tage)
- "High-Value Pipeline" (minValue = €20k, status = active)

**Pro:**
- ✅ Schnell zu implementieren (kein Backend, nur Frontend)
- ✅ Kein State Management (hardcoded)
- ✅ Weniger Code (keine DB-Tabelle)

**Con:**
- ⚠️ Nicht anpassbar (User kann keine eigenen Views erstellen)

**Aufwand:** 1h (Frontend Component)

---

**Option B: User-Custom Views (Datenbank-Persistierung)**

**Implementation:**
- Migration V10035: `user_filter_views` Tabelle
- Backend: POST/GET `/api/user-filter-views`
- Frontend: SaveFilterDialog (Name, Filter-Config JSON)

**Pro:**
- ✅ Flexibel (jeder User kann eigene Views erstellen)
- ✅ Power-User freundlich

**Con:**
- ⚠️ Zeitaufwand höher (2h statt 1h)
- ⚠️ Mehr Code (Migration + Service + Resource)

**Aufwand:** 2h (Migration + Backend + Frontend)

---

### **✅ FINALE ENTSCHEIDUNG: Option A (Predefined) für START**

**Entschieden am:** 2025-10-19
**Entschieden von:** User (Jörg)

**Begründung:**
- ✅ Schneller Start (1h statt 2h - keine Migration V10035 nötig)
- ✅ YAGNI (3 Standard-Views decken 95% der Use Cases ab)
- ✅ Migration später möglich (Predefined bleiben, Custom kommen dazu)
- ✅ User-Feedback: "Die 3 Standard-Filter reichen für den Start"

**Predefined Views:**
```tsx
const predefinedViews = [
  {
    name: 'Meine Hot Deals',
    filter: { assignedTo: currentUser.id, minValue: 10000 },
    icon: <LocalFireDepartmentIcon />
  },
  {
    name: 'Urgent This Week',
    filter: { maxCloseDate: addDays(new Date(), 7) },
    icon: <AccessTimeIcon />
  },
  {
    name: 'High-Value Pipeline',
    filter: { minValue: 20000, status: 'active' },
    icon: <EuroIcon />
  }
];
```

**Migration-Path zu Custom Views (später):**
```tsx
// Jetzt: Hardcoded (1h Aufwand)
const views = predefinedViews;

// Später: Hybrid (Predefined + Custom) (2h Aufwand)
const customViews = await httpClient.get('/api/user-filter-views');
const allViews = [...predefinedViews, ...customViews];
```

**Wann Custom Views bauen?**
- Wenn 3+ User sagen: "Ich brauche einen speziellen Filter"
- Wenn Power-User sehr individuelle Filter benötigen
- Migration: V10035 (user_filter_views Tabelle) + Backend Endpoints

---

## 9️⃣ Warum Option C (BEIDES kombinieren)?

### **Ursprüngliche Planung**

**Sprint 2.1.7.5 = Advanced Filters & Analytics (13h)** ⚠️ DEFERRED (YAGNI)

**Begründung damals:**
- "Keine echten Daten" (nur 10 DEV-SEED Opportunities)
- "Filter-Bedarf unklar" (User-Feedback fehlt)
- "YAGNI-Prinzip" (baue nur was WIRKLICH gebraucht wird)

### **Problem mit dieser Entscheidung**

**ANALYSE (2025-10-19):**
1. ✅ **DEV-SEED existiert:** 10 Opportunities sind testbar
2. ✅ **Basic Filter existiert:** High-Value/Urgent sind logische Erweiterung
3. ⚠️ **Detail View fehlt:** GRÖSSERES Gap als Filter!
4. ⚠️ **User Experience:** Aktuell nur Kanban-Cards (keine Detail-Ansicht)

### **Neue Strategie (Option C)**

**Sprint 2.1.7.5 NEU = Opportunity Management KOMPLETT**
- **Track 1:** Detail View & Management (20-28h) - größeres Gap schließen
- **Track 2:** Advanced Filters & Analytics (13h) - logische Erweiterung
- **Total:** 35-40h (~1 Woche)

### **Warum BEIDES?**

**Business Value:**
1. ✅ **Vollständiges CRUD:**
   - CREATE ✅ (Sprint 2.1.7.1 + 2.1.7.3)
   - READ ✅ (OpportunityDetailPage - NEU!)
   - UPDATE ✅ (EditOpportunityDialog - NEU!)
   - DELETE ⚠️ (später optional)

2. ✅ **Keine halbe Lösung:**
   - Opportunity-Modul wird VOLLSTÄNDIG fertig
   - Kein Feature-Gap mehr
   - Vertriebler können ALLE Workflows ohne externe Tools

3. ✅ **Manager haben Analytics:**
   - Pipeline-Health sichtbar (Conversion Rate, Bottlenecks)
   - Revenue-Forecast (Weighted Pipeline)
   - Datengetriebene Entscheidungen möglich

4. ✅ **Vertriebler haben Filter:**
   - Priorisierung: High-Value + Urgent Deals
   - Zeit-Management: Fokus auf wichtigste Opportunities
   - Produktivität steigt

### **Logische Reihenfolge**

```
✅ Sprint 2.1.7.1 COMPLETE (Lead → Opportunity CREATE)
✅ Sprint 2.1.7.3 COMPLETE (Customer → Opportunity CREATE)
   ↓
📋 Sprint 2.1.7.4 (Customer Status Architecture)
   ↓
📋 Sprint 2.1.7.2 (Xentral Integration)
   ↓
📋 Sprint 2.1.7.5 (Opportunity Management KOMPLETT) ✅
   → Track 1: Detail View (READ + UPDATE)
   → Track 2: Advanced Filters (Manager-Tools)
   ↓
📋 Sprint 2.1.7.6 (Customer Lifecycle - RISIKO/INAKTIV)
```

**Begründung:**
- Sprint 2.1.7.2 = Customer-Flow komplett
- Sprint 2.1.7.5 = Opportunity-Flow komplett
- Logisch: Customer komplett → Opportunity komplett

---

## 📊 Zusammenfassung - Entscheidungen

| #  | Entscheidung              | Gewählt           | User-Validierung      | Aufwand      | Status        |
|----|---------------------------|-------------------|-----------------------|--------------|---------------|
| 1  | Detail Page Layout        | **Tabs**          | ✅ VALIDIERT (19.10.) | -            | ✅ FINAL      |
| 2  | Edit vs Inline Edit       | **Dialog**        | ✅ Tech-Entscheidung  | 3h           | ✅ FINAL      |
| 3  | Stage Change              | **BEIDE**         | ✅ Tech-Entscheidung  | 2-3h         | ✅ FINAL      |
| 4  | Document Storage          | **File System**   | ✅ VALIDIERT (19.10.) | 2h (vs 4h)   | ✅ FINAL      |
| 5  | Filter Persistence        | **URL Params**    | ✅ Tech-Entscheidung  | -            | ✅ FINAL      |
| 6  | Analytics Calculations    | **Real-time**     | ✅ Tech-Entscheidung  | 4h (vs 6h)   | ✅ FINAL      |
| 7  | Query-Object-Pattern      | **JA**            | ✅ Tech-Entscheidung  | 4h           | ✅ FINAL      |
| 8  | Custom Views              | **Predefined**    | ✅ VALIDIERT (19.10.) | 1h (vs 2h)   | ✅ FINAL      |
| 9  | Sprint-Struktur           | **Option C**      | ✅ Tech-Entscheidung  | 35-40h       | ✅ FINAL      |

**✅ ALLE ENTSCHEIDUNGEN FINAL (2025-10-19)**

**User-Entscheidungen getroffen:**
- ✅ **Layout: Tabs** ← CRM-Standard (Salesforce-Pattern)
- ✅ **Document Storage: File System** ← Einfacher Start, Migration zu S3 später möglich
- ✅ **Custom Views: Predefined** ← YAGNI (3 Standard-Views reichen), Custom später möglich

**Aufwand-Einsparung durch Entscheidungen:**
- Document Storage: 2h gespart (File System statt S3: 2h vs 4h)
- Custom Views: 1h gespart (Predefined statt Custom: 1h vs 2h)
- **Total:** 3h gespart, Sprint bleibt bei 35-37h (statt 38-40h)

---

**✅ DESIGN DECISIONS COMPLETE - READY FOR IMPLEMENTATION**

**User-Entscheidungen getroffen:** 2025-10-19 (alle 3 validiert)
- ✅ Detail Page Layout: **Tabs**
- ✅ Document Storage: **File System**
- ✅ Custom Views: **Predefined**

**Nächste Schritte:**
1. ✅ ~~User-Entscheidungen klären~~ DONE (2025-10-19)
2. Sprint starten nach Sprint 2.1.7.2 COMPLETE
3. Track 1 + Track 2 parallel bearbeitbar
4. Aufwand: 35-37h (3h Einsparung durch optimale Entscheidungen)

**Migration-Path für spätere Optimierungen:**
- Document Storage → S3: Wenn >1.000 Dokumente oder >10GB Storage
- Custom Views → User-Custom: Wenn >3 User-Requests oder Power-User-Bedarf

**Letzte Aktualisierung:** 2025-10-19 (User-Entscheidungen final)
