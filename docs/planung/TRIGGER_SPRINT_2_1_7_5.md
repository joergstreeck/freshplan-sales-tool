# 🚀 Sprint 2.1.7.5 - Advanced Filters & Analytics

**Sprint-ID:** 2.1.7.5
**Status:** 📋 PLANNING
**Priority:** P3 (Low - erst wenn echte Daten!)
**Estimated Effort:** 16h (2 Arbeitstage)
**Owner:** TBD
**Created:** 2025-10-16
**Updated:** 2025-10-16 (Manuelle Opportunity-Erstellung ergänzt)
**Dependencies:** Sprint 2.1.7.1 COMPLETE + echte Produktions-Daten

---

## ⚠️ WICHTIGE KLARSTELLUNGEN

### **Opportunity Pipeline Stages: 7 Stages (FINAL)**

Nach Diskussion und Entscheidung in Sprint 2.1.7.3:

```
1. NEW_LEAD       (10%)
2. QUALIFICATION  (25%)
3. NEEDS_ANALYSIS (40%)
4. PROPOSAL       (60%)
5. NEGOTIATION    (80%)
6. CLOSED_WON     (100%)
7. CLOSED_LOST    (0%)
```

**OpportunityType statt RENEWAL-Stage:**
- ❌ RENEWAL ist KEINE Stage mehr (entfernt in Sprint 2.1.7.1)
- ✅ OpportunityType Enum mit Freshfoodz Business Types implementiert
- ✅ NEUGESCHAEFT, SORTIMENTSERWEITERUNG, NEUER_STANDORT, VERLAENGERUNG

**Customer-Opportunities starten bei NEEDS_ANALYSIS:**
- NEW_LEAD + QUALIFICATION entfällt (Kunde ist bereits qualifiziert!)
- Nur für Lead-Conversion: Start bei NEW_LEAD

**Dependencies:** Sprint 2.1.7.1 COMPLETE + echte Produktions-Daten

---

## 🎯 SPRINT GOALS

### **Business Value**

**Vertriebler können Opportunities intelligent filtern, priorisieren und manuell erstellen:**

- ✅ **Manuelle Opportunity-Erstellung** (ohne Lead/Customer - z.B. Messestand, Kaltakquise)
- ✅ High-Value Filter (Deal-Wert > X€)
- ✅ Urgent Filter (Close Date < 14 Tage)
- ✅ Advanced Search Dialog (multi-criteria)
- ✅ Pipeline-Analytics Dashboard (Konversionsrate, Forecast, Bottlenecks)
- ✅ Custom Views speichern ("Meine Hot Deals", "Urgent This Week")

**Business Impact:**
- **Fokus auf High-Value-Deals:** Verkäufer priorisiert große Deals
- **Deadline-Management:** Kein Deal mehr verpassen (Urgent Filter!)
- **Pipeline-Health sichtbar:** Manager sieht: Wo hakt es? (Bottleneck-Analyse)
- **Revenue-Forecast:** Geschäftsleitung sieht: Welcher Umsatz kommt? (Weighted Pipeline)

### **Technical Context**

**Warum JETZT NICHT implementieren?**
- ❌ **Keine echten Daten:** DEV-SEED hat nur 10 Opportunities
- ❌ **Filter-Bedarf unklar:** Wir wissen nicht, was Verkäufer WIRKLICH brauchen
- ❌ **YAGNI-Prinzip:** Baue Features nur wenn Pain Point existiert

**Wann implementieren?**
- ✅ **Nach Go-Live:** Wenn 100+ Opportunities im System
- ✅ **Nach User-Feedback:** "Ich brauche Filter X!"
- ✅ **Nach Migration:** Wenn 500-1000 Opportunities aus altem System importiert

**Was JETZT schon vorbereitet wird:**
- ✅ Backend-Infrastruktur: Filter-Query-Object-Pattern
- ✅ Frontend-Platzhalter: "Advanced Filters (Coming Soon)"
- ✅ Dokumentation: Was wollen wir bauen?

---

## 📦 DELIVERABLES (für SPÄTER!)

### **0. Manuelle Opportunity-Erstellung** (3h)

#### **0.1 "Neue Opportunity" Button in OpportunityPipeline** (30 Min)

**Datei:** `frontend/src/features/opportunity/components/OpportunityPipeline.tsx`

**Button hinzufügen:**
```tsx
{/* Header mit "Neue Opportunity" Button */}
<Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 2 }}>
  <Typography variant="h5">Pipeline Übersicht</Typography>

  <Box sx={{ flexGrow: 1 }} />

  {/* NEU: Manuelle Opportunity-Erstellung */}
  <Button
    variant="contained"
    color="primary"
    startIcon={<AddIcon />}
    onClick={() => setShowCreateDialog(true)}
  >
    Neue Opportunity
  </Button>

  {/* Bestehende Filter-Toggles... */}
</Stack>

<CreateOpportunityManualDialog
  open={showCreateDialog}
  onClose={() => setShowCreateDialog(false)}
  onSuccess={() => loadOpportunities()}
/>
```

#### **0.2 CreateOpportunityManualDialog Component** (1,5h)

**Neue Datei:** `frontend/src/features/opportunity/components/CreateOpportunityManualDialog.tsx`

**Anforderungen:**
- MUI Dialog mit Form-Validation
- Felder:
  - **Opportunity-Quelle** (Select: "Messestand", "Kaltakquise", "Empfehlung", "Networking-Event", "Social Media", "Sonstiges")
  - **Name** (Text, required)
  - **Company Name** (Text, required - wird später zu Lead/Customer)
  - **Deal Type** (Select: "Liefervertrag", "Testphase", "Pilot", "Vollversorgung", "Rahmenvertrag")
  - **Expected Value** (Number, EUR, required)
  - **Expected Close Date** (DatePicker, default: +30 Tage)
  - **Description** (TextArea, optional)

**Validation:**
- Company Name required (min 2 Zeichen)
- Expected Value > 0
- Close Date > Today
- Opportunity-Quelle selected

**API Call:**
```typescript
POST /api/opportunities
Body: {
  source: 'EVENT' | 'COLD_OUTREACH' | 'REFERRAL' | 'NETWORKING' | 'SOCIAL_MEDIA' | 'OTHER',
  name: string,
  companyName: string,
  dealType: string,
  expectedValue: number,
  expectedCloseDate: string (ISO-8601),
  description?: string
}

Response: {
  id: UUID,
  name: string,
  stage: 'NEW_LEAD',
  opportunityType: OpportunityType.NEUGESCHAEFT,
  ...
}
```

**Success Flow:**
```tsx
const handleCreate = async () => {
  try {
    const opportunity = await httpClient.post('/api/opportunities', {
      source: source,
      name: name,
      companyName: companyName,
      dealType: dealType,
      expectedValue: expectedValue,
      expectedCloseDate: expectedCloseDate.toISOString(),
      description: description
    });

    toast.success('Opportunity erstellt! 🎉');
    onSuccess();
    onClose();
    navigate(`/opportunities/${opportunity.id}`);

  } catch (error) {
    toast.error('Fehler beim Erstellen der Opportunity');
  }
};
```

**Info-Box im Dialog:**
```tsx
<Alert severity="info" sx={{ mb: 2 }}>
  💡 <strong>Hinweis:</strong> Diese Opportunity ist keinem Lead oder Kunden zugeordnet.
  Du kannst später manuell einen Lead oder Kunden verknüpfen.
</Alert>
```

#### **0.3 Backend: Manuelle Opportunity-Erstellung** (1h)

**Datei:** `backend/src/main/java/de/freshplan/api/resources/OpportunityResource.java`

**Neuer Endpoint:**
```java
/**
 * Erstellt eine Opportunity OHNE Lead/Customer (manuelle Erstellung)
 *
 * POST /api/opportunities
 */
@POST
@RolesAllowed({"admin", "manager", "sales"})
public Response createOpportunity(@Valid CreateOpportunityManualRequest request) {
    logger.debug("Creating manual opportunity: {}", request.getName());

    // Validate: CompanyName required
    if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
        throw new BadRequestException("Company name is required");
    }

    Opportunity opportunity = opportunityService.createManual(request, getCurrentUser());

    return Response.status(Response.Status.CREATED)
        .entity(opportunityMapper.toResponse(opportunity))
        .build();
}
```

**OpportunityService.createManual():**
```java
public Opportunity createManual(CreateOpportunityManualRequest request, User currentUser) {
    logger.debug("Creating manual opportunity for company: {}", request.getCompanyName());

    Opportunity opportunity = new Opportunity();
    opportunity.setName(request.getName());
    opportunity.setCompanyName(request.getCompanyName()); // Temporär - später zu Lead/Customer
    opportunity.setSource(request.getSource()); // "EVENT", "COLD_OUTREACH", etc.
    opportunity.setDealType(request.getDealType());
    opportunity.setOpportunityType(OpportunityType.NEUGESCHAEFT); // Hardcoded (manuelle Opp = immer NEUGESCHAEFT)
    opportunity.setStage(OpportunityStage.NEW_LEAD); // Start bei NEW_LEAD
    opportunity.setExpectedValue(request.getExpectedValue());
    opportunity.setExpectedCloseDate(request.getExpectedCloseDate());
    opportunity.setDescription(request.getDescription());
    opportunity.setProbability(OpportunityStage.NEW_LEAD.getDefaultProbability()); // 10%
    opportunity.setAssignedTo(currentUser);
    opportunity.setCreatedBy(currentUser);

    opportunityRepository.persist(opportunity);

    logger.info("Manual opportunity created: {} (ID: {})", opportunity.getName(), opportunity.getId());

    return opportunity;
}
```

**Neue Felder in Opportunity.java:**
```java
@Column(name = "source")
private String source; // "EVENT", "COLD_OUTREACH", "REFERRAL", etc.

@Column(name = "company_name")
private String companyName; // Temporär - solange kein Lead/Customer verknüpft
```

**Migration erstellen:**
```bash
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
# Beispiel: V10035__add_opportunity_source_and_company_name.sql
```

```sql
-- Opportunity Source (für manuelle Erstellung ohne Lead)
ALTER TABLE opportunities ADD COLUMN source VARCHAR(50);
ALTER TABLE opportunities ADD COLUMN company_name VARCHAR(255);

COMMENT ON COLUMN opportunities.source IS
'Opportunity-Quelle für manuelle Erstellung (EVENT, COLD_OUTREACH, REFERRAL, NETWORKING, SOCIAL_MEDIA, OTHER)';

COMMENT ON COLUMN opportunities.company_name IS
'Temporärer Company-Name (wenn noch kein Lead/Customer verknüpft). Wird NULL sobald Lead/Customer gesetzt.';

-- Beispiel-Daten:
UPDATE opportunities SET source = 'LEAD_CONVERSION' WHERE lead_id IS NOT NULL;
UPDATE opportunities SET source = 'CUSTOMER_EXPANSION' WHERE customer_id IS NOT NULL AND opportunity_type IN ('SORTIMENTSERWEITERUNG', 'NEUER_STANDORT', 'VERLAENGERUNG');
```

---

### **1. High-Value Filter** (2h)

#### **1.1 OpportunityPipeline Filter-UI erweitern** (1h)

**Datei:** `frontend/src/features/opportunity/components/OpportunityPipeline.tsx`

**Checkbox hinzufügen:**
```tsx
const [highValueOnly, setHighValueOnly] = useState(false);
const [highValueThreshold, setHighValueThreshold] = useState(10000); // Default: 10.000€

<Stack direction="row" spacing={2} alignItems="center">
  <FormControlLabel
    control={
      <Checkbox
        checked={highValueOnly}
        onChange={(e) => setHighValueOnly(e.target.checked)}
      />
    }
    label="Nur High-Value Deals"
  />

  {/* Threshold-Einstellung (nur wenn aktiviert) */}
  {highValueOnly && (
    <TextField
      type="number"
      value={highValueThreshold}
      onChange={(e) => setHighValueThreshold(parseInt(e.target.value))}
      size="small"
      InputProps={{
        startAdornment: <span>≥</span>,
        endAdornment: <span>€</span>
      }}
      sx={{ width: 150 }}
    />
  )}
</Stack>

// API Call erweitern:
const params = new URLSearchParams();
params.append('status', statusFilter);
if (myDealsOnly) {
  params.append('assignedTo', currentUser.id);
}
if (highValueOnly) {
  params.append('minValue', highValueThreshold.toString());
}

const response = await httpClient.get(`/api/opportunities?${params.toString()}`);
```

#### **1.2 Backend Filter-Endpoint erweitern** (1h)

**Datei:** `backend/src/main/java/de/freshplan/api/resources/OpportunityResource.java`

```java
@GET
@RolesAllowed({"admin", "manager", "sales"})
public Response getAllOpportunities(
    @QueryParam("page") @DefaultValue("0") int page,
    @QueryParam("size") @DefaultValue("20") int size,
    @QueryParam("status") @DefaultValue("active") String status,
    @QueryParam("assignedTo") UUID assignedToUserId,
    @QueryParam("minValue") BigDecimal minValue) { // ← NEU!

    logger.debug("Fetching opportunities - status: {}, minValue: {}", status, minValue);

    // ... existing filtering ...

    // Zusätzlich: MinValue-Filtering
    if (minValue != null) {
        opportunities = opportunities.stream()
            .filter(opp -> opp.getExpectedValue() != null &&
                          opp.getExpectedValue().compareTo(minValue) >= 0)
            .collect(Collectors.toList());
    }

    return Response.ok(opportunities).build();
}
```

---

### **2. Urgent Filter** (2h)

#### **2.1 OpportunityPipeline Filter-UI erweitern** (1h)

**Checkbox hinzufügen:**
```tsx
const [urgentOnly, setUrgentOnly] = useState(false);
const [urgentDays, setUrgentDays] = useState(14); // Default: 14 Tage

<FormControlLabel
  control={
    <Checkbox
      checked={urgentOnly}
      onChange={(e) => setUrgentOnly(e.target.checked)}
    />
  }
  label="Nur dringende Deals"
/>

{/* Threshold-Einstellung */}
{urgentOnly && (
  <TextField
    type="number"
    value={urgentDays}
    onChange={(e) => setUrgentDays(parseInt(e.target.value))}
    size="small"
    InputProps={{
      startAdornment: <span>Close Date &lt;</span>,
      endAdornment: <span>Tage</span>
    }}
    sx={{ width: 180 }}
  />
)}

// API Call:
if (urgentOnly) {
  const urgentDate = new Date();
  urgentDate.setDate(urgentDate.getDate() + urgentDays);
  params.append('maxCloseDate', urgentDate.toISOString());
}
```

#### **2.2 Backend Filter-Endpoint erweitern** (1h)

```java
@QueryParam("maxCloseDate") String maxCloseDate) { // ← NEU! ISO-8601 String

    // ... existing filtering ...

    // Zusätzlich: MaxCloseDate-Filtering
    if (maxCloseDate != null) {
        LocalDate threshold = LocalDate.parse(maxCloseDate);
        opportunities = opportunities.stream()
            .filter(opp -> opp.getExpectedCloseDate() != null &&
                          opp.getExpectedCloseDate().isBefore(threshold))
            .collect(Collectors.toList());
    }

    return Response.ok(opportunities).build();
}
```

---

### **3. Advanced Search Dialog** (4h)

#### **3.1 AdvancedSearchDialog Component** (2h)

**Neue Datei:** `frontend/src/features/opportunity/components/AdvancedSearchDialog.tsx`

**Multi-Criteria Filter:**
```tsx
export function AdvancedSearchDialog({ open, onClose, onApply }: Props) {
  const [filters, setFilters] = useState<OpportunityFilters>({
    status: 'active',
    stages: [],
    minValue: null,
    maxValue: null,
    minCloseDate: null,
    maxCloseDate: null,
    assignedTo: null,
    opportunityType: null,
    search: ''
  });

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Erweiterte Suche</DialogTitle>
      <DialogContent>
        <Grid container spacing={2}>
          {/* Row 1: Status + Stages */}
          <Grid item xs={12} md={6}>
            <FormControl fullWidth>
              <InputLabel>Status</InputLabel>
              <Select
                value={filters.status}
                onChange={(e) => setFilters({...filters, status: e.target.value})}
              >
                <MenuItem value="active">Aktiv</MenuItem>
                <MenuItem value="closed">Geschlossen</MenuItem>
                <MenuItem value="all">Alle</MenuItem>
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControl fullWidth>
              <InputLabel>Stages</InputLabel>
              <Select
                multiple
                value={filters.stages}
                onChange={(e) => setFilters({...filters, stages: e.target.value})}
                renderValue={(selected) => selected.join(', ')}
              >
                {Object.values(OpportunityStage).map(stage => (
                  <MenuItem key={stage} value={stage}>
                    <Checkbox checked={filters.stages.includes(stage)} />
                    <ListItemText primary={stage} />
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>

          {/* Row 2: Value Range */}
          <Grid item xs={12} md={6}>
            <TextField
              label="Min. Wert (€)"
              type="number"
              value={filters.minValue || ''}
              onChange={(e) => setFilters({...filters, minValue: parseFloat(e.target.value) || null})}
              fullWidth
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              label="Max. Wert (€)"
              type="number"
              value={filters.maxValue || ''}
              onChange={(e) => setFilters({...filters, maxValue: parseFloat(e.target.value) || null})}
              fullWidth
            />
          </Grid>

          {/* Row 3: Close Date Range */}
          <Grid item xs={12} md={6}>
            <DatePicker
              label="Close Date von"
              value={filters.minCloseDate}
              onChange={(date) => setFilters({...filters, minCloseDate: date})}
              slotProps={{ textField: { fullWidth: true } }}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <DatePicker
              label="Close Date bis"
              value={filters.maxCloseDate}
              onChange={(date) => setFilters({...filters, maxCloseDate: date})}
              slotProps={{ textField: { fullWidth: true } }}
            />
          </Grid>

          {/* Row 4: Assigned To + Type */}
          <Grid item xs={12} md={6}>
            <Autocomplete
              options={users}
              getOptionLabel={(user) => user.fullName}
              value={filters.assignedTo}
              onChange={(e, value) => setFilters({...filters, assignedTo: value})}
              renderInput={(params) => (
                <TextField {...params} label="Verkäufer" fullWidth />
              )}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControl fullWidth>
              <InputLabel>Opportunity-Typ</InputLabel>
              <Select
                value={filters.opportunityType || ''}
                onChange={(e) => setFilters({...filters, opportunityType: e.target.value || null})}
              >
                <MenuItem value="">Alle</MenuItem>
                <MenuItem value="NEUGESCHAEFT">Neugeschäft</MenuItem>
                <MenuItem value="SORTIMENTSERWEITERUNG">Sortimentserweiterung</MenuItem>
                <MenuItem value="NEUER_STANDORT">Neuer Standort</MenuItem>
                <MenuItem value="VERLAENGERUNG">Vertragsverlängerung</MenuItem>
              </Select>
            </FormControl>
          </Grid>

          {/* Row 5: Search */}
          <Grid item xs={12}>
            <TextField
              label="Suche (Name, Beschreibung, Kunde)"
              value={filters.search}
              onChange={(e) => setFilters({...filters, search: e.target.value})}
              fullWidth
              InputProps={{
                startAdornment: <SearchIcon />
              }}
            />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleReset}>Zurücksetzen</Button>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button onClick={() => onApply(filters)} variant="contained">
          Filter anwenden
        </Button>
      </DialogActions>
    </Dialog>
  );
}
```

#### **3.2 Backend: Query-Object-Pattern** (2h)

**Neue Datei:** `backend/src/main/java/de/freshplan/domain/opportunity/service/dto/OpportunityFilterRequest.java`

```java
@Data
@Builder
public class OpportunityFilterRequest {
    private String status; // "active", "closed", "all"
    private List<OpportunityStage> stages;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private LocalDate minCloseDate;
    private LocalDate maxCloseDate;
    private UUID assignedTo;
    private String opportunityType;
    private String search; // Fulltext search (name, description, customer)

    // Pagination
    private int page = 0;
    private int size = 20;
}
```

**OpportunityQueryService erweitern:**
```java
/**
 * Erweiterte Suche mit Query-Object
 *
 * @param filter OpportunityFilterRequest
 * @return Gefilterte Opportunities
 */
public List<OpportunityResponse> findWithFilters(OpportunityFilterRequest filter) {
    StringBuilder jpql = new StringBuilder("SELECT o FROM Opportunity o WHERE 1=1");
    Map<String, Object> params = new HashMap<>();

    // Status filtering
    if ("active".equals(filter.getStatus())) {
        jpql.append(" AND o.stage NOT IN (:closedStages)");
        params.put("closedStages", List.of(OpportunityStage.CLOSED_WON, OpportunityStage.CLOSED_LOST));
    } else if ("closed".equals(filter.getStatus())) {
        jpql.append(" AND o.stage IN (:closedStages)");
        params.put("closedStages", List.of(OpportunityStage.CLOSED_WON, OpportunityStage.CLOSED_LOST));
    }

    // Stage filtering
    if (filter.getStages() != null && !filter.getStages().isEmpty()) {
        jpql.append(" AND o.stage IN (:stages)");
        params.put("stages", filter.getStages());
    }

    // Value range filtering
    if (filter.getMinValue() != null) {
        jpql.append(" AND o.expectedValue >= :minValue");
        params.put("minValue", filter.getMinValue());
    }
    if (filter.getMaxValue() != null) {
        jpql.append(" AND o.expectedValue <= :maxValue");
        params.put("maxValue", filter.getMaxValue());
    }

    // Close Date range filtering
    if (filter.getMinCloseDate() != null) {
        jpql.append(" AND o.expectedCloseDate >= :minCloseDate");
        params.put("minCloseDate", filter.getMinCloseDate());
    }
    if (filter.getMaxCloseDate() != null) {
        jpql.append(" AND o.expectedCloseDate <= :maxCloseDate");
        params.put("maxCloseDate", filter.getMaxCloseDate());
    }

    // Assigned To filtering
    if (filter.getAssignedTo() != null) {
        jpql.append(" AND o.assignedTo.id = :assignedTo");
        params.put("assignedTo", filter.getAssignedTo());
    }

    // Opportunity Type filtering
    if (filter.getOpportunityType() != null) {
        jpql.append(" AND o.opportunityType = :opportunityType");
        params.put("opportunityType", filter.getOpportunityType());
    }

    // Fulltext Search
    if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
        jpql.append(" AND (LOWER(o.name) LIKE :search OR LOWER(o.description) LIKE :search OR LOWER(o.customer.companyName) LIKE :search)");
        params.put("search", "%" + filter.getSearch().toLowerCase() + "%");
    }

    jpql.append(" ORDER BY o.stageChangedAt DESC");

    // Execute query
    TypedQuery<Opportunity> query = entityManager.createQuery(jpql.toString(), Opportunity.class);
    params.forEach(query::setParameter);

    query.setFirstResult(filter.getPage() * filter.getSize());
    query.setMaxResults(filter.getSize());

    List<Opportunity> opportunities = query.getResultList();
    return opportunities.stream()
        .map(opportunityMapper::toResponse)
        .collect(Collectors.toList());
}
```

---

### **4. Pipeline-Analytics Dashboard** (3h)

#### **4.1 PipelineAnalyticsDashboard Component** (2h)

**Neue Datei:** `frontend/src/features/opportunity/pages/PipelineAnalyticsDashboard.tsx`

**Metriken:**
```tsx
export function PipelineAnalyticsDashboard() {
  const [analytics, setAnalytics] = useState<PipelineAnalytics | null>(null);

  useEffect(() => {
    const loadAnalytics = async () => {
      const response = await httpClient.get('/api/opportunities/pipeline/analytics');
      setAnalytics(response.data);
    };
    loadAnalytics();
  }, []);

  return (
    <MainLayoutV2>
      <Typography variant="h4" sx={{ mb: 3 }}>Pipeline Analytics</Typography>

      <Grid container spacing={3}>
        {/* Row 1: KPIs */}
        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Typography variant="caption" color="text.secondary">
                Weighted Pipeline
              </Typography>
              <Typography variant="h5">
                {formatCurrency(analytics.weightedPipeline)}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                (Erwarteter Umsatz basierend auf Probability)
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Typography variant="caption" color="text.secondary">
                Konversionsrate
              </Typography>
              <Typography variant="h5">
                {analytics.conversionRate}%
              </Typography>
              <Typography variant="caption" color="text.secondary">
                (Gewonnen / Gesamt)
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Typography variant="caption" color="text.secondary">
                Durchschn. Deal-Größe
              </Typography>
              <Typography variant="h5">
                {formatCurrency(analytics.avgDealSize)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Typography variant="caption" color="text.secondary">
                Durchschn. Sales Cycle
              </Typography>
              <Typography variant="h5">
                {analytics.avgSalesCycle} Tage
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        {/* Row 2: Charts */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardHeader title="Konversions-Funnel" />
            <CardContent>
              <FunnelChart data={analytics.funnelData} />
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardHeader title="Pipeline-Verteilung" />
            <CardContent>
              <BarChart data={analytics.stageDistribution} />
            </CardContent>
          </Card>
        </Grid>

        {/* Row 3: Bottleneck-Analyse */}
        <Grid item xs={12}>
          <Card>
            <CardHeader title="Bottleneck-Analyse" />
            <CardContent>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Stages mit überdurchschnittlicher Verweildauer:
              </Typography>
              <Stack spacing={1}>
                {analytics.bottlenecks.map(bottleneck => (
                  <Alert key={bottleneck.stage} severity="warning">
                    <strong>{bottleneck.stage}:</strong> Ø {bottleneck.avgDaysInStage} Tage
                    (Normalwert: {bottleneck.expectedDays} Tage)
                    → {bottleneck.opportunityCount} Opportunities betroffen
                  </Alert>
                ))}
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </MainLayoutV2>
  );
}
```

#### **4.2 Backend: Analytics-Endpoint** (1h)

**Datei:** `backend/src/main/java/de/freshplan/api/resources/OpportunityResource.java`

```java
/**
 * Pipeline Analytics
 *
 * GET /api/opportunities/pipeline/analytics
 */
@GET
@Path("/pipeline/analytics")
@RolesAllowed({"admin", "manager", "sales"})
public Response getPipelineAnalytics() {
    logger.debug("Generating pipeline analytics");

    PipelineAnalyticsResponse analytics = opportunityService.getPipelineAnalytics();

    return Response.ok(analytics).build();
}
```

**OpportunityService:**
```java
public PipelineAnalyticsResponse getPipelineAnalytics() {
    // Weighted Pipeline (Summe: expectedValue * probability)
    BigDecimal weightedPipeline = opportunityRepository.calculateForecast();

    // Konversionsrate
    Double conversionRate = opportunityRepository.getConversionRate();

    // Durchschnittliche Deal-Größe
    BigDecimal avgDealSize = opportunityRepository.getAverageDealSize();

    // Durchschnittlicher Sales Cycle
    Integer avgSalesCycle = opportunityRepository.getAverageSalesCycle();

    // Funnel-Daten
    List<FunnelStageDTO> funnelData = opportunityRepository.getFunnelData();

    // Stage-Verteilung
    Map<OpportunityStage, Long> stageDistribution = opportunityRepository.getStageDistribution();

    // Bottleneck-Analyse
    List<BottleneckDTO> bottlenecks = opportunityRepository.findBottlenecks();

    return PipelineAnalyticsResponse.builder()
        .weightedPipeline(weightedPipeline)
        .conversionRate(conversionRate)
        .avgDealSize(avgDealSize)
        .avgSalesCycle(avgSalesCycle)
        .funnelData(funnelData)
        .stageDistribution(stageDistribution)
        .bottlenecks(bottlenecks)
        .build();
}
```

---

### **5. Custom Views speichern** (2h)

#### **5.1 SavedFilterView Component** (1h)

**Neue Datei:** `frontend/src/features/opportunity/components/SavedFilterView.tsx`

**Konzept:**
```tsx
export function SavedFilterView() {
  const [savedViews, setSavedViews] = useState<FilterView[]>([]);

  const predefinedViews = [
    { name: 'Meine Hot Deals', filter: { assignedTo: currentUser.id, minValue: 10000 } },
    { name: 'Urgent This Week', filter: { maxCloseDate: addDays(new Date(), 7) } },
    { name: 'High-Value Pipeline', filter: { minValue: 20000, status: 'active' } },
  ];

  return (
    <Stack direction="row" spacing={1} sx={{ mb: 2 }}>
      <Typography variant="caption" color="text.secondary" sx={{ pt: 1 }}>
        Gespeicherte Ansichten:
      </Typography>
      {predefinedViews.map(view => (
        <Chip
          key={view.name}
          label={view.name}
          onClick={() => applyFilter(view.filter)}
          variant="outlined"
        />
      ))}
      <Button
        size="small"
        startIcon={<AddIcon />}
        onClick={() => setShowSaveDialog(true)}
      >
        Neue Ansicht
      </Button>
    </Stack>
  );
}
```

#### **5.2 Backend: User-Filter-Views speichern** (1h)

**Migration:** `V10034__create_user_filter_views.sql`

```sql
CREATE TABLE user_filter_views (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES app_user(id),
    view_name VARCHAR(100) NOT NULL,
    filter_config JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, view_name)
);

CREATE INDEX idx_user_filter_views_user_id ON user_filter_views(user_id);

COMMENT ON TABLE user_filter_views IS
'Gespeicherte Filter-Ansichten pro User (Custom Views für Opportunity-Pipeline)';
```

---

## ✅ DEFINITION OF DONE

### **Functional:**
- [x] High-Value Filter funktioniert (minValue QueryParam)
- [x] Urgent Filter funktioniert (maxCloseDate QueryParam)
- [x] Advanced Search Dialog mit Multi-Criteria
- [x] Pipeline-Analytics Dashboard zeigt KPIs
- [x] Custom Views können gespeichert werden

### **Technical:**
- [x] Backend: Query-Object-Pattern implementiert
- [x] Backend: GET /api/opportunities/pipeline/analytics
- [x] Backend: POST /api/user-filter-views (speichern)
- [x] Backend: GET /api/user-filter-views (laden)
- [x] Frontend: AdvancedSearchDialog
- [x] Frontend: PipelineAnalyticsDashboard
- [x] Frontend: SavedFilterView
- [x] Migration: V10034 (user_filter_views Tabelle)
- [x] Unit Tests: OpportunityQueryService.findWithFilters()

### **Quality:**
- [x] Code Review: 1 Approval
- [x] UI/UX: Filter-Dialog nicht überladen!
- [x] Performance: Advanced Search < 1s (bei 500 Opportunities)

### **Documentation:**
- [x] TRIGGER_SPRINT_2_1_7_4.md erstellt (✅ Done)
- [x] TRIGGER_INDEX.md aktualisiert
- [x] CRM_COMPLETE_MASTER_PLAN_V5.md: Session Log

---

## 📊 SUCCESS METRICS

### **Usability:**
- Advanced Search: Max 5 Klicks, < 30 Sekunden
- Analytics Dashboard: Auf einen Blick erkennbar: "Wo hakt es?"

### **Performance:**
- Filter-Query: < 1s (bei 500 Opportunities)
- Analytics-Query: < 2s (komplexe Aggregationen)

### **Business Impact:**
- Deal-Win-Rate: Messbar (nach 3 Monaten: Improvement durch Priorisierung?)
- Forecast-Accuracy: Messbar (Weighted Pipeline vs. tatsächlicher Umsatz)

---

## 🚀 PREREQUISITES

### **KRITISCH: Erst NACH Go-Live!**
- ⚠️ **Keine echten Daten:** DEV-SEED hat nur 10 Opportunities
- ⚠️ **Filter-Bedarf unklar:** User-Feedback fehlt
- ⚠️ **YAGNI:** Baue nur was WIRKLICH gebraucht wird!

### **Ready to Start (später!):**
- ✅ 100+ Opportunities im System (echte Daten!)
- ✅ User-Feedback: "Ich brauche Filter X!"
- ✅ Sprint 2.1.7.1-3 COMPLETE

### **Blockers:**
- ❌ Keine echten Produktions-Daten (BLOCKER!)

---

## 📅 TIMELINE (für SPÄTER!)

**Tag 1 (8h):**
- Manuelle Opportunity-Erstellung (3h)
- High-Value Filter (2h)
- Urgent Filter (2h)
- Advanced Search Dialog - Start (1h)

**Tag 2 (8h):**
- Advanced Search Dialog - Fertigstellung (3h)
- Pipeline-Analytics Dashboard (3h)
- Custom Views speichern (2h)

**Total: 16h = 2 Arbeitstage** ✅

---

## 🔗 RELATED WORK

### **Dependent Sprints:**
- Sprint 2.1.7.1: Lead → Opportunity (COMPLETE)
- Sprint 2.1.7.2: Customer-Management + Xentral (COMPLETE)
- Sprint 2.1.7.3: Bestandskunden-Workflow (COMPLETE)

### **Follow-up Sprints:**
- Sprint 2.1.8.x: AI-basierte Cross-Selling-Vorschläge (weit später!)
- Sprint 2.1.9.x: Opportunity ROI Calculator (später!)

---

## 📝 NOTES

### **Design Decisions:**

1. **Query-Object-Pattern (statt viele QueryParams):**
   - Backend: OpportunityFilterRequest DTO
   - Sauberere API (keine 10+ QueryParams)
   - Einfacher erweiterbar

2. **Advanced Search als Dialog (nicht Sidebar):**
   - Weniger UI-Overhead im Kanban
   - Fokus auf Standard-Filter (Active/Closed/Meine Deals)
   - Advanced nur bei Bedarf (Power-User)

3. **Custom Views als User-Präferenz (nicht global):**
   - Jeder User hat eigene Views
   - Keine "Global Views" (zu komplex für Start)
   - Später: Team-Views (wenn Bedarf)

4. **Analytics als eigenes Dashboard (nicht im Kanban):**
   - Kanban bleibt fokussiert (Arbeit!)
   - Analytics ist für Manager/Geschäftsleitung
   - Separater Menüpunkt: "Pipeline Analytics"

### **Technical Debt:**
- Query-Object: Aktuell keine Validierung (später: @Valid OpportunityFilterRequest)
- Analytics: Aktuell keine Caching (später: Redis für tägliche Aggregationen)
- Custom Views: Aktuell nur Frontend-State (später: Backend-Persistierung)

### **Warum NICHT jetzt?**
1. **YAGNI-Prinzip:** Baue nur was WIRKLICH gebraucht wird
2. **Keine Daten:** 10 DEV-SEED Opportunities sind kein realistischer Test
3. **User-Feedback fehlt:** Wir raten nur, was Verkäufer brauchen
4. **Time-to-Market:** Fokus auf Kern-Workflows (Sprint 2.1.7.1-3)

### **Wann bauen?**
- ✅ **Nach Go-Live:** Wenn 100+ Opportunities im System
- ✅ **Nach User-Feedback:** "Ich brauche Filter X!"
- ✅ **Nach Migration:** Wenn 500-1000 Opportunities aus altem System

---

**Sprint ist GEPLANT, aber NOCH NICHT READY FÜR KICKOFF!** ⏸️

**Nächster Schritt:**
1. **Warten auf echte Daten** (Go-Live!)
2. **User-Feedback sammeln** (Was brauchen Verkäufer wirklich?)
3. **Dann erst:** Sprint 2.1.7.4 priorisieren

**Aktuell:** Fokus auf Sprint 2.1.7.1 → 2.1.7.2 → 2.1.7.3 (Kern-Workflows!)
