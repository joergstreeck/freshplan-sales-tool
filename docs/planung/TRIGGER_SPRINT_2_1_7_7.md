# 🚀 Sprint 2.1.7.7 - Multi-Location Management & Xentral Filial-Sync

**Sprint-ID:** 2.1.7.7
**Status:** 📋 PLANNING
**Priority:** P2 (Medium - Business Impact: Hotelketten!)
**Estimated Effort:** 12-15h (2 Arbeitstage)
**Owner:** TBD
**Created:** 2025-10-21
**Updated:** 2025-10-21
**Dependencies:** Sprint 2.1.7.4 COMPLETE, Sprint 2.1.7.2 COMPLETE

---

## 🎯 SPRINT GOALS

### **Business Value**

**Vertriebler können Filialbetriebe professionell managen - kritisch für Hotelketten!**

**Key Deliverables:**
- Filial-Anlage UI (Wie erstellen? Separate Customer oder Location?)
- Opportunity→Location Link (Pro Filiale tracken!)
- Xentral Address-Matching Service (Lieferadresse → Filiale)
- Umsatz-Tracking pro Filiale (Dashboard pro Standort)

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
- ✅ **Unterscheidung:** Nur über **Lieferadresse** (String-Matching!)

### **Technical Context**

**Status Quo (nach Sprint 2.1.7.4):**
- ✅ Customer-Hierarchie Support (`parentCustomer`, `childCustomers`)
- ✅ CustomerHierarchyType Enum (HEADQUARTER, FILIALE, FRANCHISE, STANDALONE)
- ✅ CustomerLocation Entity (locations per Customer)
- ✅ `branchCount`, `isChain` Metadaten aus Lead
- ❌ **KEIN** Opportunity→Location Link
- ❌ **KEIN** Filial-Anlage UI
- ❌ **KEIN** Xentral-Filial-Mapping

---

## 📦 DELIVERABLES

### **1. Architektur-Entscheidung: Separate Customer vs. Location** (2h)

**Ziel:** Finale Entscheidung treffen basierend auf User-Präferenz

**Option A: Separate Customers pro Filiale**
```
Hauptbetrieb: Customer #1 (hierarchyType = HEADQUARTER)
  ├─ Filiale Berlin:  Customer #2 (hierarchyType = FILIALE, parentCustomer = #1)
  ├─ Filiale München: Customer #3 (hierarchyType = FILIALE, parentCustomer = #1)
```

**Vorteile:**
- Separate Opportunities pro Filiale
- Separate Umsatz-Tracking (einfach)
- Separate Contacts (Filialleiter)

**Nachteile:**
- Viele Einträge (100 Filialen = 101 Customers)
- Aggregation nötig für Gesamt-Umsatz

**Option B: Locations innerhalb Customer**
```
Customer #1 (Bäckerei Müller GmbH)
  ├─ Location "Zentrale Hamburg"
  ├─ Location "Filiale Berlin"
```

**Vorteile:**
- Übersichtlich (1 Customer)
- Gesamt-Umsatz direkt

**Nachteile:**
- Opportunity→Location Link nötig
- Address-Matching komplex

**Tasks:**
- [ ] User-Entscheidung einholen (Option A oder B?)
- [ ] Design Document erstellen
- [ ] Migration-Strategie festlegen

---

### **2. Filial-Anlage UI** (4-5h)

**Ziel:** Vertriebler kann Filialen nutzerfreundlich anlegen

**UI (Customer-DetailPage):**
```tsx
// Tab "Filialen/Standorte"
<Tab label="Filialen" count={customer.childCustomers.length}>
  <Stack spacing={2}>
    <Button
      variant="contained"
      startIcon={<AddIcon />}
      onClick={() => openCreateBranchDialog()}
    >
      Neue Filiale anlegen
    </Button>

    {/* Liste bestehender Filialen */}
    <List>
      {customer.childCustomers.map(branch => (
        <BranchListItem
          branch={branch}
          onEdit={handleEdit}
          onDelete={handleDelete}
        />
      ))}
    </List>
  </Stack>
</Tab>
```

**CreateBranchDialog Component:**
```tsx
// Option A: Separate Customer
<Dialog title="Neue Filiale anlegen">
  <TextField label="Filialname" placeholder="Filiale Berlin Mitte" />
  <TextField label="Straße" />
  <TextField label="PLZ, Stadt" />
  <TextField label="Filialleiter" type="contact" />
  <TextField label="Xentral Kundennummer" value={customer.xentralCustomerId} disabled />
  <Alert severity="info">
    Gleiche Kundennummer wie Hauptbetrieb.
    Unterscheidung über Lieferadresse.
  </Alert>
</Dialog>

// Option B: Location
<Dialog title="Neuer Standort anlegen">
  <TextField label="Standortname" />
  <TextField label="Standortcode" />
  {/* ... Address Fields ... */}
</Dialog>
```

**Backend (Option A):**
```java
// POST /api/customers/{id}/branches
public Customer createBranch(UUID parentId, CreateBranchRequest request) {
  Customer parent = customerRepository.findById(parentId);

  Customer branch = new Customer();
  branch.setCompanyName(request.branchName);
  branch.setParentCustomer(parent);
  branch.setHierarchyType(CustomerHierarchyType.FILIALE);
  branch.setXentralCustomerId(parent.getXentralCustomerId()); // ← Gleiche!
  branch.setStatus(CustomerStatus.PROSPECT);

  // Address aus Request
  CustomerLocation location = new CustomerLocation();
  location.setLocationName(request.branchName);
  location.setIsMainLocation(true);
  branch.addLocation(location);

  return customerRepository.persist(branch);
}
```

**Tests:** 8 Tests (Dialog + Backend + Validation)

---

### **3. Opportunity→Location Link** (3h)

**Ziel:** Opportunity kann spezifischer Filiale zugeordnet werden

**Migration V10036:**
```sql
-- Opportunity → Location Link
ALTER TABLE opportunities
ADD COLUMN location_id UUID REFERENCES customer_locations(id);

-- Optional: Falls Option A (Separate Customer) gewählt
-- Dann: location_id wird NICHT genutzt, stattdessen customer_id = branch
```

**Opportunity.java:**
```java
// NEU (nur wenn Option B - Locations)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "location_id")
private CustomerLocation location;
```

**UI Integration:**
```tsx
// CreateOpportunityDialog erweitern
{customer.hierarchyType === 'HEADQUARTER' && customer.childCustomers.length > 0 && (
  <Select label="Filiale/Standort" value={selectedBranch}>
    <MenuItem value={null}>Alle Filialen</MenuItem>
    {customer.childCustomers.map(branch => (
      <MenuItem value={branch.id}>{branch.companyName}</MenuItem>
    ))}
  </Select>
)}
```

**Tests:** 6 Tests (Migration + UI + Backend)

---

### **4. Xentral Address-Matching Service** (3-4h)

**Ziel:** Lieferadressen aus Xentral automatisch Filialen zuordnen

**Service:**
```java
// XentralAddressMatcher.java

public CustomerLocation matchDeliveryAddress(
    Customer customer,
    String xentralDeliveryAddress
) {
  // Schritt 1: Alle Locations des Customers
  List<CustomerLocation> locations = customer.getActiveLocations();

  // Schritt 2: Fuzzy-Matching (Levenshtein-Distance)
  for (CustomerLocation location : locations) {
    CustomerAddress primaryAddress = location.getPrimaryShippingAddress();
    if (primaryAddress == null) continue;

    String fullAddress = buildFullAddress(primaryAddress);
    int distance = calculateLevenshteinDistance(fullAddress, xentralDeliveryAddress);

    // Threshold: 80% Übereinstimmung
    if (distance <= fullAddress.length() * 0.2) {
      return location;
    }
  }

  // Fallback: Main Location
  return customer.getMainLocation().orElse(null);
}

private String buildFullAddress(CustomerAddress address) {
  return String.join(", ",
    address.getStreet(),
    address.getZipCode(),
    address.getCity()
  );
}
```

**Xentral-Sync Integration:**
```java
// CustomerRevenueService.calculateActualAnnualVolume()

List<Invoice> invoices = xentralApiClient.getInvoices(
  customer.getXentralCustomerId(),
  LocalDate.now().minusMonths(12),
  LocalDate.now()
);

// Gruppe invoices nach Location
Map<CustomerLocation, List<Invoice>> invoicesByLocation = invoices.stream()
  .collect(Collectors.groupingBy(invoice ->
    addressMatcher.matchDeliveryAddress(customer, invoice.getDeliveryAddress())
  ));

// Pro Location Umsatz berechnen
for (Map.Entry<CustomerLocation, List<Invoice>> entry : invoicesByLocation.entrySet()) {
  CustomerLocation location = entry.getKey();
  BigDecimal locationRevenue = entry.getValue().stream()
    .map(Invoice::getTotalAmount)
    .reduce(BigDecimal.ZERO, BigDecimal::add);

  // Store location revenue
  locationRevenueRepository.save(new LocationRevenue(
    location.getId(),
    locationRevenue,
    LocalDate.now()
  ));
}
```

**Tests:** 10 Tests (Fuzzy Matching + Fallback + Integration)

---

### **5. Dashboard: Filial-Umsätze** (2h)

**Ziel:** Customer-Dashboard zeigt Umsatz pro Filiale

**UI (Customer-DetailPage):**
```tsx
// Tab "Umsätze" erweitert
{customer.hierarchyType === 'HEADQUARTER' && (
  <Card>
    <CardHeader>Umsatz pro Filiale (letzte 12 Monate)</CardHeader>
    <CardContent>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Filiale</TableCell>
            <TableCell align="right">Umsatz</TableCell>
            <TableCell align="right">Anteil</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {branchRevenues.map(br => (
            <TableRow key={br.branchId}>
              <TableCell>{br.branchName}</TableCell>
              <TableCell align="right">{formatCurrency(br.revenue)}</TableCell>
              <TableCell align="right">{br.percentage}%</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </CardContent>
  </Card>
)}
```

**Backend:**
```java
// GET /api/customers/{id}/branch-revenues

public record BranchRevenueResponse(
  UUID branchId,
  String branchName,
  BigDecimal revenue,
  BigDecimal percentage
) {}

public List<BranchRevenueResponse> getBranchRevenues(UUID customerId) {
  Customer customer = customerRepository.findById(customerId);

  List<Customer> branches = customer.getChildCustomers();
  BigDecimal totalRevenue = BigDecimal.ZERO;

  // Revenue pro Branch
  Map<Customer, BigDecimal> revenueMap = new HashMap<>();
  for (Customer branch : branches) {
    BigDecimal branchRevenue = calculateBranchRevenue(branch);
    revenueMap.put(branch, branchRevenue);
    totalRevenue = totalRevenue.add(branchRevenue);
  }

  // Percentage berechnen
  return revenueMap.entrySet().stream()
    .map(entry -> new BranchRevenueResponse(
      entry.getKey().getId(),
      entry.getKey().getCompanyName(),
      entry.getValue(),
      entry.getValue().divide(totalRevenue, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
    ))
    .sorted((a, b) -> b.revenue().compareTo(a.revenue()))
    .toList();
}
```

**Tests:** 5 Tests (Dashboard + Backend)

---

## 📊 SUCCESS METRICS

**Test Coverage:**
- Backend: 29 Tests (8 Filial-Anlage + 6 Opportunity-Link + 10 Address-Matching + 5 Dashboard)
- Frontend: 13 Tests (8 UI + 5 Integration)
- **Total: 42 Tests**

**Code Changes:**
- 1 Migration (V10036: opportunity.location_id - optional)
- 5 Backend Services (BranchService, AddressMatcher, RevenueService, OpportunityService)
- 4 Frontend Components (CreateBranchDialog, BranchListItem, BranchRevenueTable, OpportunityLocationSelect)

**Business Impact:**
- ✅ Hotelketten-Support (kritisch!)
- ✅ Filial-spezifische Opportunities
- ✅ Umsatz-Tracking pro Standort
- ✅ Xentral-Sync trotz fehlender Filial-ID

---

## ✅ DEFINITION OF DONE

### **Functional**
- [ ] User-Entscheidung getroffen (Separate Customer vs. Location)
- [ ] Filial-Anlage UI funktioniert
- [ ] Opportunity→Location/Branch Link funktioniert
- [ ] Xentral Address-Matching Service deployed
- [ ] Dashboard zeigt Filial-Umsätze

### **Technical**
- [ ] Migration V10036 (optional: opportunity.location_id)
- [ ] BranchService implementiert
- [ ] XentralAddressMatcher implementiert
- [ ] Fuzzy-Matching mit 80% Threshold
- [ ] Fallback auf Main Location

### **Quality**
- [ ] Tests: 42/42 GREEN
- [ ] TypeScript: type-check PASSED
- [ ] Code Review: Self-reviewed
- [ ] Performance: Address-Matching < 100ms

---

## 📅 TIMELINE

**Tag 1 (8h):**
- Architektur-Entscheidung + Design (2h)
- Filial-Anlage UI (4h)
- Opportunity→Location Link (2h)

**Tag 2 (7h):**
- Xentral Address-Matching Service (4h)
- Dashboard Filial-Umsätze (2h)
- Testing & Bugfixes (1h)

**Total:** 15h (2 Arbeitstage)

---

## 📄 ARTEFAKTE

**Technische Spezifikation:** (zu erstellen)
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_7_TECHNICAL.md`

**Design Decisions:** (bereits dokumentiert)
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_4_DESIGN_DECISIONS.md` (Section 10)

**Design System:**
→ `/docs/planung/grundlagen/DESIGN_SYSTEM.md`

---

## 🚀 PREREQUISITES

### **Dependencies:**
- ✅ Sprint 2.1.7.4 COMPLETE (hierarchyType Foundation)
- ✅ Sprint 2.1.7.2 COMPLETE (Xentral Integration)

### **User-Entscheidung BENÖTIGT:**

**KRITISCH:** Vor Implementierung entscheiden!

**Frage:** Wie sollen Filialen modelliert werden?

**Option A: Separate Customers**
- Vorteil: Einfaches Opportunity-Tracking, separate Contacts
- Nachteil: Viele Einträge

**Option B: Locations innerhalb Customer**
- Vorteil: Übersichtlich, ein Customer-Eintrag
- Nachteil: Opportunity→Location Migration, komplexeres Address-Matching

**Empfehlung:** Option A (Separate Customers) - pragmatischer für Food-Branche

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
1. User-Entscheidung einholen (Separate Customer vs. Location)
2. Technical Specification erstellen
3. Migration-Strategie finalisieren

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

**✅ SPRINT STATUS: 📋 PLANNING - User-Entscheidung benötigt!**

**Letzte Aktualisierung:** 2025-10-21 (Initial Creation nach Filial-Architektur-Diskussion)
