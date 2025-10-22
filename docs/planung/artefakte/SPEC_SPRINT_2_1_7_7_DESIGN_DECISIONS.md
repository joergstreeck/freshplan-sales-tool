# Sprint 2.1.7.7 - Design Decisions

**Sprint-ID:** 2.1.7.7
**Created:** 2025-10-21
**Status:** 📋 FINAL
**Owner:** Claude + Jörg (User Validation)

---

## 📋 ÜBERSICHT

Dieses Dokument konsolidiert **alle Architektur- und Design-Entscheidungen** für Sprint 2.1.7.7 - Multi-Location Management.

**Hauptentscheidungen:**
1. **Option A vs. Option B** - Separate Customers vs. Locations (CRM Best Practice)
2. **Parent Hierarchy Dashboard** - Wie Salesforce Roll-Up Metriken
3. **Xentral Address-Matching** - Fuzzy-Matching ohne Filial-ID
4. **Opportunity→Customer Mapping** - Direkt oder mit Location-Link?

---

## 1️⃣ Option A vs. Option B - Die zentrale Architektur-Entscheidung ⭐

### **User-Anforderung (2025-10-21)**

> "Wir haben oft Filialbetriebe, besonders Hotelketten"

**Kritische Fragen:**
1. Wie legt Vertriebler Filialbetriebe nutzerfreundlich an?
2. Ist der konvertierte Lead Hauptbetrieb oder Filiale?
3. Wie ordnen wir Xentral-Umsatz pro Filiale zu?
4. Sollen Opportunities pro Filiale oder pro Kette sein?

**Xentral-Realität:**
- ❌ KEINE Filial-ID in Xentral
- ✅ Gleiche Kundennummer für alle Filialen (z.B. `56037`)
- ✅ Unterscheidung NUR über Lieferadresse (String-Matching!)

### **Die zwei Modelle**

#### **OPTION A: Parent-Child IN der CUSTOMER-Tabelle**

```sql
-- CUSTOMERS Tabelle
ID | company_name              | hierarchy_type | parent_customer_id | xentral_customer_id
---|---------------------------|----------------|--------------------|-----------------
1  | NH Hotels GmbH            | HEADQUARTER    | NULL               | 56037
2  | NH Hotel Berlin           | FILIALE        | 1                  | 56037
3  | NH Hotel München          | FILIALE        | 1                  | 56037
```

**Jede Filiale:**
- ✅ IST ein Customer (eigene Row in `customers`)
- ✅ HAT Parent-Link (`parent_customer_id`)
- ✅ HAT hierarchyType = FILIALE
- ✅ HAT gleiche Xentral-Kundennummer wie Parent

**Opportunity-Mapping:**
```java
Opportunity.customer → Customer "NH Hotel Berlin" (FILIALE)
// EINFACH: Nur eine Verknüpfung!
```

#### **OPTION B: Parent-Child IN der LOCATION-Tabelle**

```sql
-- CUSTOMERS Tabelle
ID | company_name              | hierarchy_type | xentral_customer_id
---|---------------------------|----------------|-----------------
1  | NH Hotels GmbH            | HEADQUARTER    | 56037

-- CUSTOMER_LOCATIONS Tabelle
ID | customer_id | location_name        | is_main_location
---|-------------|----------------------|-----------------
101| 1           | Zentrale Berlin      | true
102| 1           | Filiale München      | false
```

**Jede Filiale:**
- ❌ IST KEIN Customer (nicht in `customers`)
- ✅ IST eine Location (in `customer_locations`)
- ✅ HAT Customer-Link (`customer_id`)

**Opportunity-Mapping:**
```java
Opportunity.customer → Customer "NH Hotels GmbH"
Opportunity.location → Location "Filiale München"
// KOMPLEX: Zwei Verknüpfungen!
```

---

## 2️⃣ CRM vs. ERP Best Practice Analyse

### **CRM Best Practice = Option A**

**Alle CRM-Marktführer nutzen Option A:**

**Salesforce:**
```
Account "NH Hotels Deutschland GmbH" (Parent Account)
  ├─ Account "NH Hotel Berlin Alexanderplatz" (Child Account)
  └─ Account "NH Hotel München Deutscher Kaiser" (Child Account)

Opportunity "Frühstück erweitern"
  → Account: "NH Hotel Berlin Alexanderplatz"
```

**Microsoft Dynamics 365:**
```
Parent Account: Bäckerei Müller GmbH
  ├─ Child Account: Bäckerei Müller Filiale Hauptstraße
  └─ Child Account: Bäckerei Müller Filiale Bahnhofstraße

Deal → Account (Child Account)
```

**HubSpot:**
```
Parent Company: Restaurant-Gruppe "Mamma Mia"
  ├─ Child Company: Mamma Mia Berlin Mitte
  └─ Child Company: Mamma Mia Hamburg Reeperbahn

Deal → Company (Child Company)
```

**Warum nutzen ALLE CRMs Option A?**

1. **Sales-Prozess denkt in "Kunden":**
   - Vertriebler sagt: "Ich habe das **NH Hotel Berlin** gewonnen" (Customer)
   - NICHT: "Ich habe den **Standort Berlin von NH Hotels** gewonnen" (Location)

2. **Opportunity-Tracking ist einfach:**
   - CRM-Standard: `Opportunity.customer_id`
   - KEIN zweites Feld `location_id` nötig

3. **Contacts sind Customer-gebunden:**
   - Contact "Herr Schmidt" (Filialleiter) → Customer "NH Hotel Berlin"
   - NICHT: Contact → Customer + Location (2 Links)

4. **Dashboards & Reporting:**
   - "Meine Kunden": 20 Hotels (klar!)
   - NICHT: "Meine Kunden": 1 Kette + 20 Locations (verwirrend)

### **ERP Best Practice = Option B**

**ERP-Systeme (SAP, Xentral) nutzen Option B:**

**Xentral:**
```
Kunde 56037: NH Hotels GmbH
  ├─ Lieferadresse: Alexanderplatz 1, Berlin
  └─ Lieferadresse: Stachus 10, München

Order → Customer + Ship-To Address
```

**SAP S/4HANA:**
```
Business Partner: NH Hotels GmbH
  ├─ Plant/Site 1000: Berlin
  └─ Plant/Site 2000: München

Order → Business Partner + Plant
```

**Warum nutzen ERPs Option B?**

1. **Fokus: LOGISTIK** (nicht Sales)
   - "Wo hinliefern?" (Ship-To Address)
   - NICHT: "Wer ist Ansprechpartner?" (CRM)

2. **Billing ist zentral:**
   - Eine Rechnung für alle Filialen
   - Payment Terms auf Konzern-Ebene

3. **Master Data Management:**
   - Weniger Duplikate (1 Customer statt 20)
   - Zentrale Stammdaten

---

## 3️⃣ ENTSCHEIDUNG: Option A (CRM Best Practice) ✅

### **Begründung**

**FreshPlan ist ein SALES-TOOL (CRM), KEIN ERP!**

**Vorteile Option A:**
1. ✅ **CRM-Industrie-Standard** (Salesforce, Dynamics, HubSpot)
2. ✅ **Einfache Opportunity-Zuordnung** (nur `customer_id`)
3. ✅ **Klare Vertriebler-Sicht** ("Meine Kunden" = 20 Hotels)
4. ✅ **Einfache Contact-Zuordnung** (Contact → Customer)
5. ✅ **Standard-Queries** (`COUNT(customers)`, `SUM(revenue)`)
6. ✅ **CRM-ERP Integration** (Standard-Pattern: Parent-Child → Ship-To Mapping)

**Nachteile Option A (und warum sie verkraftbar sind):**
1. ❌ "Viele Einträge (100 Filialen = 101 Customers)"
   - **Counter:** Salesforce hat Millionen Accounts - funktioniert!
   - **Counter:** Filter `hierarchyType = HEADQUARTER` zeigt nur Hauptbetriebe

2. ❌ "Gesamt-Umsatz = Aggregation über Children"
   - **Counter:** CRMs haben "Roll-Up" Features (Standard!)
   - **Counter:** SQL: `SUM(revenue) WHERE parent_id = X` (trivial)

**Alternativen (verworfen):**
- ❌ **Option B (Locations):** Nicht CRM Best Practice, zu komplex für Sales
- ❌ **Hybrid (beides):** Over-Engineering, verwirrt User

---

## 4️⃣ Parent Hierarchy Dashboard - Salesforce Roll-Up Pattern

### **Problem**

User-Frage:
> "Dann bekommt die gesamte Organisation ein separates Dashboard, um den Überblick zu sehen?"

**Antwort: JA! Genau wie Salesforce.**

### **Lösung: Zwei Dashboard-Typen**

#### **1. Parent-Dashboard (Konzern-Übersicht)**

**Zeigt:**
- Gesamt-Umsatz aller Standorte (Roll-Up)
- Anzahl Standorte
- Gesamt-Opportunities aller Standorte
- Top-10 Standorte nach Umsatz (Ranking-Table)
- Tree-View Hierarchie (Drill-Down)
- Tab "Standorte" mit Details

**Salesforce nennt das:**
- "Account Hierarchy View"
- "Parent Account Dashboard"
- "Roll-Up Summary Fields"

#### **2. Child-Dashboard (Einzelstandort)**

**Zeigt:**
- Nur eigenen Standort-Umsatz
- Nur eigene Opportunities
- Link zum Parent ("Teil von: NH Hotels GmbH ↑")

### **Roll-Up Metriken (wie Salesforce)**

**Konzept:** Parent berechnet automatisch Summen aller Children

```java
// Backend: Roll-Up Service
public HierarchyMetrics getHierarchyMetrics(UUID parentId) {
  Customer parent = customerRepository.findById(parentId);
  List<Customer> children = parent.getChildCustomers();

  // Gesamt-Umsatz (Parent + alle Children)
  BigDecimal totalRevenue = children.stream()
    .map(Customer::getActualAnnualVolume)
    .filter(Objects::nonNull)
    .reduce(BigDecimal.ZERO, BigDecimal::add);

  // Gesamt-Opportunities (alle Children)
  int totalOpportunities = opportunityRepository
    .countByCustomerInAndStageNot(children, OpportunityStage.CLOSED_WON);

  return new HierarchyMetrics(
    totalRevenue,
    children.size(),
    totalOpportunities
  );
}
```

### **Frontend: Hierarchy-Tab**

```tsx
// CustomerDetailPage.tsx - Tab "Standorte"

{customer.hierarchyType === 'HEADQUARTER' && (
  <Tab label={`Standorte (${customer.childCustomers.length})`}>
    <HierarchyDashboard parent={customer} />
  </Tab>
)}
```

**Components:**
1. **HierarchyMetricsCards** - Gesamt-KPIs (Umsatz, Standorte, Opps)
2. **LocationRankingTable** - Top-10 Standorte nach Umsatz
3. **HierarchyTreeView** - Tree-View wie Salesforce (expandable)

### **Alternativen (verworfen)**

❌ **Kein separates Dashboard:** Verwirrt User (wo sind die Filialen?)
❌ **Nur Liste ohne Metriken:** Zu wenig Insight (keine Roll-Ups)
❌ **Nur Tree-View ohne Table:** Zu wenig Detail (kein Ranking)

---

## 5️⃣ Xentral Address-Matching - Fuzzy-Matching ohne Filial-ID

### **Problem**

**Xentral-Realität:**
- ❌ Keine Filial-ID
- ✅ Gleiche Kundennummer (`56037`) für alle Filialen
- ✅ Unterscheidung nur über Lieferadresse

**Challenge:**
```
Xentral Invoice:
  customerId: 56037
  deliveryAddress: "Alexanderplatz 1, 10178 Berlin"

FreshPlan:
  Customer "NH Hotel Berlin" (xentral_customer_id = 56037)
  Customer "NH Hotel München" (xentral_customer_id = 56037)

→ Welcher Customer?
```

### **Lösung: Fuzzy Address-Matching**

**Algorithmus:**
```java
// XentralAddressMatcher.java

public Customer matchDeliveryAddress(
    Customer parent,
    String xentralDeliveryAddress
) {
  // Schritt 1: Alle Children mit gleicher Xentral-ID
  List<Customer> candidates = parent.getChildCustomers().stream()
    .filter(c -> c.getXentralCustomerId().equals(parent.getXentralCustomerId()))
    .toList();

  // Schritt 2: Fuzzy-Matching (Levenshtein-Distance)
  for (Customer candidate : candidates) {
    CustomerLocation mainLocation = candidate.getMainLocation().orElse(null);
    if (mainLocation == null) continue;

    CustomerAddress address = mainLocation.getPrimaryShippingAddress().orElse(null);
    if (address == null) continue;

    String fullAddress = buildFullAddress(address);
    int distance = calculateLevenshteinDistance(fullAddress, xentralDeliveryAddress);

    // Threshold: 80% Übereinstimmung
    double similarity = 1.0 - (double) distance / Math.max(fullAddress.length(), xentralDeliveryAddress.length());

    if (similarity >= 0.80) {
      return candidate;  // Match gefunden!
    }
  }

  // Fallback: Parent (Hauptbetrieb)
  log.warn("No address match found for {}, using parent", xentralDeliveryAddress);
  return parent;
}

private String buildFullAddress(CustomerAddress address) {
  return String.join(", ",
    address.getStreet(),
    address.getZipCode(),
    address.getCity()
  ).toLowerCase().trim();
}
```

**Levenshtein-Distance:**
- Misst String-Ähnlichkeit
- Beispiel: "Alexanderplatz 1" vs. "Alexanderplatz 1, 10178" → 80% Match
- Industrie-Standard für Address-Matching

**Threshold: 80%**
- < 80%: Kein Match → Fallback auf Parent
- ≥ 80%: Match → Filiale zugeordnet

**Fallback-Strategie:**
- Kein Match → Umsatz wird Parent zugeordnet
- Warnung loggen für manuelle Review
- Besser als Fehler (robuster)

### **Alternativen (verworfen)**

❌ **Exaktes String-Matching:** Zu fragil (Tippfehler, Formatierung)
❌ **Geo-Coding (Google Maps API):** Over-Engineering, Kosten
❌ **Machine Learning:** YAGNI für Phase 1

---

## 6️⃣ Opportunity→Customer Mapping (Option A)

### **Problem**

**Frage:** Wie werden Opportunities zugeordnet?

**Option A (gewählt):**
```java
// Opportunity.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "customer_id")
private Customer customer;  // ← NH Hotel Berlin (FILIALE)

// KEIN location_id Feld nötig!
```

**Option B (verworfen):**
```java
// Opportunity.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "customer_id")
private Customer customer;  // ← NH Hotels GmbH (HEADQUARTER)

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "location_id")
private CustomerLocation location;  // ← Filiale München

// Komplex: Zwei Verknüpfungen!
```

### **Entscheidung: Opportunity → Customer (Filiale) direkt**

**Begründung:**
1. ✅ **CRM-Standard:** Alle CRMs machen das so
2. ✅ **Einfache Queries:** `Opportunity.customer → Customer "NH Hotel Berlin"`
3. ✅ **Klare Vertriebler-Sicht:** "Meine Opportunities" = Opportunities meiner Filialen
4. ✅ **Keine Migration nötig:** Opportunity.customer_id existiert bereits!

**UI-Workflow:**
```tsx
// CreateOpportunityDialog.tsx

{customer.hierarchyType === 'HEADQUARTER' && customer.childCustomers.length > 0 ? (
  <Select label="Standort" value={selectedBranch}>
    <MenuItem value={customer.id}>{customer.companyName} (Zentrale)</MenuItem>
    {customer.childCustomers.map(branch => (
      <MenuItem value={branch.id}>{branch.companyName}</MenuItem>
    ))}
  </Select>
) : (
  <TextField label="Kunde" value={customer.companyName} disabled />
)}
```

**Backend:**
```java
// OpportunityService.createOpportunity()

Opportunity opp = new Opportunity();
opp.setName(request.name);
opp.setCustomer(selectedCustomer);  // ← Kann Filiale sein!
// KEIN location_id nötig
```

### **Alternativen (verworfen)**

❌ **Option B (customer + location):** Komplex, nicht CRM-Standard
❌ **Opportunity nur an Parent:** Verliert Filial-Kontext

---

## 7️⃣ Lead→Customer Conversion bei Filialen

### **Problem**

**Frage:** Wenn Lead "Bäckerei Müller - Filiale Berlin", was wird daraus?

**User-Workflow:**
1. Lead erfassen: "Bäckerei Müller - Filiale Berlin Hauptstraße"
2. Opportunity WON → Auto-Convert
3. Was entsteht?

### **Lösung: Lead wird zu FILIALE (mit hierarchyType)**

**Sprint 2.1.7.4 (AKTUELL - Minimal-Support):**
```java
// LeadConvertService.java

Customer customer = new Customer();
customer.setCompanyName(lead.companyName);  // "Bäckerei Müller - Filiale Berlin"
customer.setStatus(CustomerStatus.PROSPECT);

// Hierarchy Type bestimmen
if (lead.isChain != null && lead.isChain) {
    customer.setHierarchyType(CustomerHierarchyType.HEADQUARTER);
} else {
    customer.setHierarchyType(CustomerHierarchyType.STANDALONE);
}

// branchCount, isChain als Metadaten
customer.setBranchCount(lead.branchCount);
customer.setIsChain(lead.isChain);

// ❌ KEIN automatisches Filial-Splitting
// ❌ KEIN Parent-Link (vorerst)
```

**Sprint 2.1.7.7 (ERWEITERT - Full Support):**

**UI-Enhancement:**
```tsx
// ConvertToCustomerDialog.tsx (erweitert)

<Dialog>
  <Select label="Typ">
    <MenuItem value="STANDALONE">Einzelbetrieb</MenuItem>
    <MenuItem value="HEADQUARTER">Zentrale/Hauptbetrieb</MenuItem>
    <MenuItem value="FILIALE">Filiale (gehört zu...)</MenuItem>
  </Select>

  {hierarchyType === 'FILIALE' && (
    <Autocomplete
      label="Gehört zu (Parent)"
      options={headquarterCustomers}
      onChange={setParentCustomer}
    />
  )}
</Dialog>
```

**Backend:**
```java
// LeadConvertService.java (Sprint 2.1.7.7)

Customer customer = new Customer();
customer.setCompanyName(lead.companyName);
customer.setStatus(CustomerStatus.PROSPECT);

// NEU: hierarchyType aus Dialog
customer.setHierarchyType(request.hierarchyType);

// NEU: Parent-Link (falls FILIALE)
if (request.hierarchyType == CustomerHierarchyType.FILIALE) {
    Customer parent = customerRepository.findById(request.parentCustomerId);
    customer.setParentCustomer(parent);
    customer.setXentralCustomerId(parent.getXentralCustomerId());  // Gleiche!
}
```

### **Alternativen**

**Option A (gewählt):** UI-Enhancement in ConvertDialog (Sprint 2.1.7.7)
**Option B (verworfen):** Lead-Tabelle um `parentLeadId` erweitern (Over-Engineering)
**Option C (verworfen):** Automatisches Matching (zu komplex, fehleranfällig)

---

## 8️⃣ Dashboard-Filter "Nur Hauptbetriebe"

### **Problem**

**Bei 100 Filialen:**
- Customer-Liste: 101 Einträge (1 Hauptbetrieb + 100 Filialen)
- Vertriebler will oft nur Hauptbetriebe sehen

### **Lösung: Filter-Chips im Dashboard**

```tsx
// CustomerDashboard.tsx

<Stack direction="row" spacing={1}>
  <Chip
    label="Alle Kunden"
    onClick={() => setFilter('ALL')}
    color={filter === 'ALL' ? 'primary' : 'default'}
  />
  <Chip
    label="Nur Hauptbetriebe"
    onClick={() => setFilter('HEADQUARTER')}
    color={filter === 'HEADQUARTER' ? 'primary' : 'default'}
  />
  <Chip
    label="Nur Filialen"
    onClick={() => setFilter('FILIALE')}
    color={filter === 'FILIALE' ? 'default'}
  />
</Stack>
```

**Backend:**
```java
// GET /api/customers?hierarchyType=HEADQUARTER

public List<Customer> getCustomers(
    @QueryParam("hierarchyType") CustomerHierarchyType hierarchyType
) {
  if (hierarchyType != null) {
    return customerRepository.findByHierarchyType(hierarchyType);
  }
  return customerRepository.listAll();
}
```

---

## 🛠️ TECHNICAL DEBT

### **Potenzielle Verbesserungen (später)**

1. **Machine Learning Address-Matching (95%+ Accuracy)**
   - Aktuell: Levenshtein (80%)
   - Später: ML-Modell (höhere Genauigkeit)

2. **Xentral Custom-Field "filial_id"**
   - Falls Xentral später Filial-ID einführt
   - Dann: Einfaches ID-Matching statt Address-Matching

3. **Geo-Coding (Google Maps API)**
   - Location-Matching via Koordinaten
   - Höhere Genauigkeit als String-Matching

4. **Multi-Level Hierarchies**
   - Aktuell: 2 Ebenen (Parent → Children)
   - Später: 3+ Ebenen (Konzern → Region → Filiale)

---

## 🔗 RELATED DOCUMENTATION

**Technical Specification:**
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_7_TECHNICAL.md`

**Trigger Document:**
→ `/docs/planung/TRIGGER_SPRINT_2_1_7_7.md`

**Sprint 2.1.7.4 Design Decisions:**
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_4_DESIGN_DECISIONS.md` (Section 10)

**Design System:**
→ `/docs/planung/grundlagen/DESIGN_SYSTEM.md`

---

**✅ DESIGN DECISIONS STATUS: 📋 FINAL - User-Validated (Option A)**

**Letzte Aktualisierung:** 2025-10-21 (Initial Creation - Option A als CRM Best Practice bestätigt)
