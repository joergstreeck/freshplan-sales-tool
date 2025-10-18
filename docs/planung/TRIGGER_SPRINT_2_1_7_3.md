# 🚀 Sprint 2.1.7.3 - Bestandskunden-Workflow (SORTIMENTSERWEITERUNG/VERLAENGERUNG)

**Sprint-ID:** 2.1.7.3
**Status:** 📋 PLANNING
**Priority:** P2 (Medium)
**Estimated Effort:** 16h (2 Arbeitstage)
**Owner:** TBD
**Created:** 2025-10-16
**Updated:** 2025-10-18 (OpportunityDetailPage Features hinzugefügt)
**Dependencies:** Sprint 2.1.7.2 COMPLETE

---

## 🎯 SPRINT GOALS

### **Business Value**

**Vertriebler können Bestandskunden erweitern (Sortimentserweiterung/Neuer Standort/Vertragsverlängerung):**

- ✅ "Neue Opportunity für Customer" Button in CustomerDetailPage
- ✅ Customer-Opportunity-Historie (alle Deals zu diesem Kunden)
- ✅ OpportunityType-Logik klären (VERLAENGERUNG vs. NEUGESCHAEFT)
- ✅ Activity-Tracking für Bestandskunden-Geschäft
- ✅ Opportunity-Verlinkung: Kunde → Opportunities (bidirektional)

**Business Impact:**
- **Erweiterungspotenzial erkennbar:** Verkäufer sieht: "Kunde kauft regelmäßig → Zeit für Produkterweiterung!"
- **Provision-Transparenz:** SORTIMENTSERWEITERUNG/VERLAENGERUNG-Deals werden separat getrackt (für Provision-Berechnung)
- **Customer-Lifetime-Value:** Alle Deals zu einem Kunden sichtbar (Historie)
- **Beziehungs-Management:** Verkäufer bleibt in Kontakt mit bestehenden Kunden

### **Technical Context**

**Backend existiert bereits:**
- ✅ POST /api/opportunities/for-customer/{customerId} (aus Sprint 2.1.7.0)
- ✅ OpportunityService.createForCustomer() implementiert
- ✅ OpportunityType Enum mit Freshfoodz Business Types (Sprint 2.1.7.1)

**Was JETZT gebaut wird:**
- ✅ CustomerDetailPage: "Neue Opportunity" Button
- ✅ CreateOpportunityForCustomerDialog (ähnlich wie CreateOpportunityDialog aus 2.1.7.1)
- ✅ CustomerOpportunitiesList Component (zeigt alle Opportunities zu diesem Kunden)
- ✅ Opportunity-Historie Timeline (gruppiert nach Status: Offen/Gewonnen/Verloren)
- ✅ Activity-Tracking: "SORTIMENTSERWEITERUNG-Gespräch", "Product-Demo"

**NICHT in diesem Sprint:**
- ❌ Advanced Analytics (Revenue Forecasting) → Sprint 2.1.7.4
- ❌ Opportunity ROI Calculator → Später
- ❌ Automatische Cross-Selling-Vorschläge (AI-basiert) → Weit später

---

## 📦 DELIVERABLES

### **1. "Neue Opportunity für Customer" Button** (2h)

#### **1.1 CustomerDetailPage Integration** (1h)

**Datei:** `frontend/src/pages/CustomerDetailPage.tsx`

**Button hinzufügen:**
```tsx
{/* Nur bei AKTIV Status */}
{customer.status === 'AKTIV' && (
  <Button
    variant="outlined"
    color="primary"
    startIcon={<TrendingUpIcon />}
    onClick={() => setShowOpportunityDialog(true)}
    sx={{
      borderColor: theme.palette.primary.main,
      '&:hover': {
        borderColor: theme.palette.primary.dark,
        bgcolor: alpha(theme.palette.primary.main, 0.1)
      }
    }}
  >
    Neue Opportunity erstellen
  </Button>
)}

{/* Opportunities-Sektion */}
<Accordion
  expanded={expandedSection === 'opportunities'}
  onChange={handleSectionChange('opportunities')}
  sx={{ mb: 2, border: '1px solid', borderColor: 'divider' }}
>
  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
    <Box sx={{ display: 'flex', alignItems: 'center', width: '100%', gap: 1 }}>
      <TrendingUpIcon color="primary" />
      <Box sx={{ flexGrow: 1 }}>
        <Typography variant="h6">
          Verkaufschancen ({opportunities.length})
        </Typography>
        {expandedSection !== 'opportunities' && opportunities.length > 0 && (
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
            {opportunities.filter(o => !['CLOSED_WON', 'CLOSED_LOST'].includes(o.stage)).length} offen •
            {opportunities.filter(o => o.stage === 'CLOSED_WON').length} gewonnen
          </Typography>
        )}
      </Box>
    </Box>
  </AccordionSummary>

  <AccordionDetails>
    <CustomerOpportunitiesList customerId={customer.id} />
  </AccordionDetails>
</Accordion>

<CreateOpportunityForCustomerDialog
  open={showOpportunityDialog}
  onClose={() => setShowOpportunityDialog(false)}
  customer={customer}
  onSuccess={() => loadOpportunities()}
/>
```

#### **1.2 CreateOpportunityForCustomerDialog Component** (1h)

**Neue Datei:** `frontend/src/features/opportunity/components/CreateOpportunityForCustomerDialog.tsx`

**Anforderungen:**
- MUI Dialog mit Form
- Felder:
  - **Opportunity-Typ** (Select: "NEUGESCHAEFT", "SORTIMENTSERWEITERUNG", "NEUER_STANDORT", "VERLAENGERUNG")
  - **Name** (Text, auto-generated: "{companyName} - {type} {timeframe}")
  - **Timeframe** (Text: "Q2 2025", "H1 2025", etc.)
  - **Expected Value** (Number, EUR)
  - **Expected Close Date** (DatePicker)
  - **Description** (TextArea)

**Pre-fill aus Customer-Daten:**
```tsx
const defaultValues = {
  opportunityType: OpportunityType.SORTIMENTSERWEITERUNG, // Default für Bestandskunden
  name: customer.companyName,  // ✅ NUR Company-Name! Typ zeigt der Badge
  expectedValue: customer.expectedAnnualVolume ? customer.expectedAnnualVolume * 0.2 : 0, // 20% vom Jahresvolumen
  expectedCloseDate: addDays(new Date(), 60), // 60 Tage (Bestandskunden-Deals brauchen länger)
  description: `${opportunityType}-Opportunity für Bestandskunde ${customer.companyName}. Aktuelles Jahresvolumen: ${customer.expectedAnnualVolume}€`,
};
```

**Validation:**
- Expected Value > 0 (required)
- Close Date > Today
- Opportunity Type selected (required)

**API Call:**
```typescript
POST /api/opportunities/for-customer/{customerId}
Body: {
  opportunityType: 'NEUGESCHAEFT' | 'SORTIMENTSERWEITERUNG' | 'NEUER_STANDORT' | 'VERLAENGERUNG',
  name: string,
  timeframe?: string,
  expectedValue: number,
  expectedCloseDate: string (ISO-8601),
  description?: string
}
```

**Success Flow:**
```tsx
const handleCreate = async () => {
  try {
    const opportunity = await httpClient.post(
      `/api/opportunities/for-customer/${customer.id}`,
      {
        opportunityType: opportunityType,
        name: name,
        timeframe: timeframe,
        expectedValue: expectedValue,
        expectedCloseDate: expectedCloseDate.toISOString(),
        description: description
      }
    );

    toast.success('Opportunity erstellt! 🎉');
    onSuccess();
    onClose();

  } catch (error) {
    if (error.status === 400) {
      toast.error('Kunde muss AKTIV sein, um Opportunity zu erstellen');
    } else {
      toast.error('Fehler beim Erstellen der Opportunity');
    }
  }
};
```

---

### **2. Customer-Opportunity-Historie** (3h)

#### **2.1 CustomerOpportunitiesList Component** (2h)

**Neue Datei:** `frontend/src/features/customers/components/CustomerOpportunitiesList.tsx`

**Anforderungen:**
- Zeigt alle Opportunities für diesen Customer
- Gruppiert nach Status: **Offen** / **Gewonnen** / **Verloren**
- Sortierung: Neueste zuerst
- Link zu Opportunity-Detail
- Badge: Opportunity-Typ (NEUGESCHAEFT/SORTIMENTSERWEITERUNG/NEUER_STANDORT/VERLAENGERUNG)

**Darstellung:**
```tsx
export function CustomerOpportunitiesList({ customerId }: Props) {
  const [opportunities, setOpportunities] = useState<Opportunity[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadOpportunities = async () => {
      try {
        const response = await httpClient.get(
          `/api/customers/${customerId}/opportunities`
        );
        setOpportunities(response.data);
      } catch (error) {
        console.error('Failed to load opportunities:', error);
        toast.error('Fehler beim Laden der Opportunities');
      } finally {
        setLoading(false);
      }
    };

    loadOpportunities();
  }, [customerId]);

  if (loading) return <CircularProgress />;

  const openOpportunities = opportunities.filter(
    o => !['CLOSED_WON', 'CLOSED_LOST'].includes(o.stage)
  );
  const wonOpportunities = opportunities.filter(o => o.stage === 'CLOSED_WON');
  const lostOpportunities = opportunities.filter(o => o.stage === 'CLOSED_LOST');

  return (
    <Stack spacing={3}>
      {/* Gruppe 1: Offene Opportunities */}
      {openOpportunities.length > 0 && (
        <Box>
          <Typography variant="h6" sx={{ mb: 2 }}>
            🔥 Offen ({openOpportunities.length})
          </Typography>
          <Stack spacing={2}>
            {openOpportunities.map(opp => (
              <OpportunityHistoryCard key={opp.id} opportunity={opp} />
            ))}
          </Stack>
        </Box>
      )}

      {/* Gruppe 2: Gewonnene Opportunities */}
      {wonOpportunities.length > 0 && (
        <Box>
          <Typography variant="h6" sx={{ mb: 2 }}>
            ✅ Gewonnen ({wonOpportunities.length})
          </Typography>
          <Stack spacing={2}>
            {wonOpportunities.map(opp => (
              <OpportunityHistoryCard key={opp.id} opportunity={opp} />
            ))}
          </Stack>
        </Box>
      )}

      {/* Gruppe 3: Verlorene Opportunities */}
      {lostOpportunities.length > 0 && (
        <Accordion>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography variant="h6">
              ❌ Verloren ({lostOpportunities.length})
            </Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Stack spacing={2}>
              {lostOpportunities.map(opp => (
                <OpportunityHistoryCard key={opp.id} opportunity={opp} />
              ))}
            </Stack>
          </AccordionDetails>
        </Accordion>
      )}

      {/* Empty State */}
      {opportunities.length === 0 && (
        <Alert severity="info">
          Noch keine Opportunities für diesen Kunden erstellt.
        </Alert>
      )}
    </Stack>
  );
}
```

#### **2.2 OpportunityHistoryCard Component** (1h)

**Neue Datei:** `frontend/src/features/customers/components/OpportunityHistoryCard.tsx`

**Darstellung:**
```tsx
export function OpportunityHistoryCard({ opportunity }: Props) {
  const navigate = useNavigate();

  const opportunityTypeConfig = {
    'NEUGESCHAEFT': { color: 'primary', icon: '🆕' },
    'SORTIMENTSERWEITERUNG': { color: 'secondary', icon: '📈' },
    'NEUER_STANDORT': { color: 'info', icon: '📍' },
    'VERLAENGERUNG': { color: 'warning', icon: '🔁' },
  };

  const typeInfo = opportunityTypeConfig[opportunity.opportunityType] || opportunityTypeConfig['Standard'];

  return (
    <Card
      sx={{
        cursor: 'pointer',
        '&:hover': { boxShadow: 3 },
        border: '1px solid',
        borderColor: 'divider'
      }}
      onClick={() => navigate(`/opportunities/${opportunity.id}`)}
    >
      <CardContent>
        <Stack direction="row" justifyContent="space-between" alignItems="start">
          {/* Links: Name + Typ */}
          <Box sx={{ flexGrow: 1 }}>
            <Stack direction="row" spacing={1} alignItems="center">
              <Typography variant="h6">{opportunity.name}</Typography>
              <Chip
                label={`${typeInfo.icon} ${opportunity.opportunityType}`}
                color={typeInfo.color}
                size="small"
              />
            </Stack>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              {opportunity.description}
            </Typography>
          </Box>

          {/* Rechts: Stage Badge */}
          <Chip
            label={opportunity.stage}
            color={getStageColor(opportunity.stage)}
            size="small"
          />
        </Stack>

        <Divider sx={{ my: 1.5 }} />

        {/* Metriken */}
        <Stack direction="row" spacing={3}>
          <Box>
            <Typography variant="caption" color="text.secondary">Wert</Typography>
            <Typography variant="body2" fontWeight="bold">
              {formatCurrency(opportunity.expectedValue)}
            </Typography>
          </Box>
          <Box>
            <Typography variant="caption" color="text.secondary">Close Date</Typography>
            <Typography variant="body2">
              {formatDate(opportunity.expectedCloseDate)}
            </Typography>
          </Box>
          <Box>
            <Typography variant="caption" color="text.secondary">Probability</Typography>
            <Typography variant="body2">{opportunity.probability}%</Typography>
          </Box>
          <Box>
            <Typography variant="caption" color="text.secondary">Erstellt</Typography>
            <Typography variant="body2">{formatDate(opportunity.createdAt)}</Typography>
          </Box>
        </Stack>
      </CardContent>
    </Card>
  );
}
```

---

### **3. Backend-Erweiterung** (1h)

#### **3.1 CustomerResource: Opportunities-Endpoint** (30 Min)

**Datei:** `backend/src/main/java/de/freshplan/api/resources/CustomerResource.java`

```java
/**
 * Holt alle Opportunities für einen Customer
 *
 * GET /api/customers/{id}/opportunities
 */
@GET
@Path("/{id}/opportunities")
@RolesAllowed({"admin", "manager", "sales"})
public Response getCustomerOpportunities(@PathParam("id") UUID customerId) {
    logger.debug("Fetching opportunities for customer: {}", customerId);

    // Validate customer exists
    Customer customer = customerRepository
        .findByIdOptional(customerId)
        .orElseThrow(() -> new NotFoundException("Customer not found"));

    // Fetch opportunities
    List<OpportunityResponse> opportunities =
        opportunityService.findByCustomerId(customerId);

    return Response.ok(opportunities).build();
}
```

#### **3.2 OpportunityService: findByCustomerId()** (30 Min)

**Datei:** `backend/src/main/java/de/freshplan/domain/opportunity/service/OpportunityService.java`

```java
/**
 * Findet alle Opportunities für einen Customer
 *
 * @param customerId Customer-ID
 * @return Liste von Opportunities (sortiert: neueste zuerst)
 */
public List<OpportunityResponse> findByCustomerId(UUID customerId) {
    logger.debug("Finding opportunities for customer: {}", customerId);

    List<Opportunity> opportunities = opportunityRepository
        .find("customer.id = ?1 ORDER BY createdAt DESC", customerId)
        .list();

    return opportunities.stream()
        .map(opportunityMapper::toResponse)
        .collect(Collectors.toList());
}
```

---

### **4. OpportunityType-Logik implementieren** (1h)

#### **4.1 Design-Entscheidung: OpportunityType statt RENEWAL-Stage** (30 Min)

**Bereits umgesetzt in Sprint 2.1.7.1:**
- ✅ OpportunityType Enum mit Freshfoodz Business Types implementiert
- ✅ RENEWAL-Stage wurde entfernt (war obsolete)
- ✅ Migration V10030 hat alte Daten migriert

**Aktuelle Architektur:**
```
OpportunityType Enum (Freshfoodz Business Types):
- NEUGESCHAEFT (Neukunden-Akquise, Standard bei Lead-Conversion)
- SORTIMENTSERWEITERUNG (Produkterweiterung/Volumen-Erhöhung)
- NEUER_STANDORT (Zusätzliche Location)
- VERLAENGERUNG (Rahmenvertrag-Renewal)

Stages: 7 Stages für ALLE Opportunities
- NEW_LEAD → QUALIFICATION → NEEDS_ANALYSIS → PROPOSAL → NEGOTIATION → CLOSED_WON/LOST

Customer-Opportunities starten bei NEEDS_ANALYSIS (Kunde bereits qualifiziert!)
```

**Was JETZT implementiert wird:**
- ✅ CreateOpportunityForCustomerDialog: OpportunityType-Auswahl (Default: SORTIMENTSERWEITERUNG)
- ✅ OpportunityHistoryCard: Badges mit Freshfoodz Business Types

#### **4.2 Backend-Validierung: OpportunityType-Setzen** (30 Min)

**Backend-Service erweitern:**
```java
// OpportunityService.java - createForCustomer() Methode

public Opportunity createForCustomer(
    UUID customerId,
    CreateOpportunityForCustomerRequest request,
    User currentUser) {

    Customer customer = customerRepository.findByIdOptional(customerId)
        .orElseThrow(() -> new NotFoundException("Customer not found"));

    Opportunity opportunity = new Opportunity();
    opportunity.setName(request.getName());
    opportunity.setCustomer(customer);

    // WICHTIG: OpportunityType aus Request übernehmen!
    // Default: SORTIMENTSERWEITERUNG (Bestandskunden-Standard)
    OpportunityType type = request.getOpportunityType() != null
        ? request.getOpportunityType()
        : OpportunityType.SORTIMENTSERWEITERUNG;
    opportunity.setOpportunityType(type);

    // Customer-Opportunities starten bei NEEDS_ANALYSIS (bereits qualifiziert!)
    opportunity.setStage(OpportunityStage.NEEDS_ANALYSIS);
    opportunity.setProbability(OpportunityStage.NEEDS_ANALYSIS.getDefaultProbability()); // 40%

    // ... rest bleibt gleich

    return opportunity;
}
```

**DTO aktualisieren:**
```java
// CreateOpportunityForCustomerRequest.java

public class CreateOpportunityForCustomerRequest {
    private String name;
    private OpportunityType opportunityType; // ← MUSS übergeben werden!
    private String description;
    private BigDecimal expectedValue;
    private LocalDate expectedCloseDate;

    // ... getters/setters/builder
}
```

---

### **5. Activity-Tracking für Bestandskunden-Geschäft** (1h)

#### **5.1 Activity-Typen erweitern** (30 Min)

**Datei:** `backend/src/main/java/de/freshplan/domain/opportunity/entity/OpportunityActivity.java`

```java
public enum ActivityType {
    NOTE("Notiz"),
    STAGE_CHANGED("Status geändert"),
    CALL("Anruf"),
    MEETING("Meeting"),
    EMAIL("E-Mail"),
    EXPANSION_CALL("Sortimentserweiterungs-Gespräch"), // ← NEU!
    PRODUCT_DEMO("Produktpräsentation"), // ← NEU!
    CONTRACT_RENEWAL("Vertragsverlängerungs-Verhandlung"); // ← NEU!

    private final String displayName;

    ActivityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
```

#### **5.2 ActivityDialog erweitern** (30 Min)

**Datei:** `frontend/src/features/opportunity/components/ActivityDialog.tsx`

**Activity-Typ-Auswahl erweitern:**
```tsx
<Select
  label="Aktivitäts-Typ"
  value={activityType}
  onChange={(e) => setActivityType(e.target.value)}
>
  <MenuItem value="NOTE">Notiz</MenuItem>
  <MenuItem value="CALL">Anruf</MenuItem>
  <MenuItem value="MEETING">Meeting</MenuItem>
  <MenuItem value="EMAIL">E-Mail</MenuItem>
  <MenuItem value="EXPANSION_CALL">📈 Sortimentserweiterungs-Gespräch</MenuItem>
  <MenuItem value="PRODUCT_DEMO">📊 Produktpräsentation</MenuItem>
  <MenuItem value="CONTRACT_RENEWAL">🔁 Vertragsverlängerungs-Verhandlung</MenuItem>
</Select>
```

---

### **6. Testing & Bugfixes** (1h)

**Unit Tests (30 Min):**
- CreateOpportunityForCustomerDialog: Form validation
- OpportunityService.findByCustomerId(): Sortierung korrekt

**Manual E2E Testing (30 Min):**
```
Test Case 1: Customer → Opportunity Flow
1. Open CustomerDetailPage (Kunde "Bella Italia")
2. Click "Neue Opportunity erstellen"
3. Dialog öffnet sich
4. Verify pre-filled values:
   - Type: "SORTIMENTSERWEITERUNG"
   - Name: "Bella Italia - Sortimentserweiterung Q4 2025"
   - Expected Value: 20% vom Jahresvolumen
   - Close Date: +60 Tage
5. Submit
6. Verify Toast: "Opportunity erstellt!"
7. Verify: Opportunity in "Offen"-Gruppe
8. Verify: Stage = NEEDS_ANALYSIS (nicht NEW_LEAD!)
9. Verify: opportunityType = "SORTIMENTSERWEITERUNG"

Test Case 2: Customer-Opportunity-Historie
1. CustomerDetailPage mit 3 Opportunities:
   - 1x Offen (PROPOSAL)
   - 1x Gewonnen (CLOSED_WON)
   - 1x Verloren (CLOSED_LOST)
2. Verify: 3 Gruppen sichtbar (Offen/Gewonnen/Verloren)
3. Verify: Verloren-Gruppe in Accordion (zugeklappt)
4. Verify: Sortierung neueste zuerst
5. Click auf Opportunity-Card
6. Verify: Redirect zu OpportunityDetailPage

Test Case 3: OpportunityType Backend-Validierung
1. Backend API Call: POST /api/opportunities/for-customer/{customerId}
2. Body: { opportunityType: "VERLAENGERUNG", ... }
3. Verify: Opportunity created mit opportunityType = "VERLAENGERUNG"
4. Verify: Stage = "NEEDS_ANALYSIS" (nicht NEW_LEAD!)
5. Verify: probability = 40% (NEEDS_ANALYSIS default)
```

---

### **7. OpportunityDetailPage: Edit-Funktionalität** (3h) 📝 HINZUGEFÜGT 2025-10-18

**Business Value:** Verkäufer können Opportunity-Details korrigieren ohne Backend-Zugriff

#### **7.1 Edit-Mode aktivieren** (1h)

**Datei:** `frontend/src/pages/OpportunityDetailPage.tsx`

```tsx
const [isEditMode, setIsEditMode] = useState(false);
const [editedOpportunity, setEditedOpportunity] = useState<Opportunity | null>(null);

// Edit-Button im Header
{!isEditMode && (
  <Button
    variant="outlined"
    startIcon={<EditIcon />}
    onClick={() => {
      setEditedOpportunity({ ...opportunity });
      setIsEditMode(true);
    }}
  >
    Bearbeiten
  </Button>
)}

// Save/Cancel Buttons im Edit-Mode
{isEditMode && (
  <Stack direction="row" spacing={2}>
    <Button
      variant="contained"
      startIcon={<SaveIcon />}
      onClick={handleSave}
    >
      Speichern
    </Button>
    <Button
      variant="outlined"
      onClick={() => setIsEditMode(false)}
    >
      Abbrechen
    </Button>
  </Stack>
)}
```

#### **7.2 Editable Fields** (2h)

**Felder die bearbeitet werden können:**
- Name (TextField)
- Expected Value (NumberField mit Euro-Formatierung)
- Expected Close Date (DatePicker)
- Probability (Slider 0-100%)
- Description (TextArea)
- OpportunityType (Select - nur bei Customer-Opportunities)

**Backend:**
```java
@PUT
@Path("/{id}")
@RolesAllowed({"USER", "MANAGER", "ADMIN"})
public Response updateOpportunity(@PathParam("id") UUID id, UpdateOpportunityRequest request) {
    // Validation + Update
}
```

---

### **8. Stage-Änderungen: Manuelle Controls** (2h) 🔀 HINZUGEFÜGT 2025-10-18

**Business Value:** Verkäufer können Stage ohne Drag&Drop ändern (z.B. auf Mobile, oder bei Bulk-Updates)

#### **8.1 Stage-Dropdown in OpportunityDetailPage** (1h)

```tsx
<FormControl fullWidth>
  <InputLabel>Pipeline-Status</InputLabel>
  <Select
    value={opportunity.stage}
    onChange={(e) => handleStageChange(e.target.value)}
    disabled={isClosedStage(opportunity.stage)}
  >
    {STAGE_OPTIONS.map((stage) => (
      <MenuItem key={stage.value} value={stage.value}>
        {stage.icon} {stage.label} ({stage.probability}%)
      </MenuItem>
    ))}
  </Select>
</FormControl>

{/* Warning bei Rückwärts-Bewegung */}
{isBackwardMove && (
  <Alert severity="warning">
    Stage-Rückschritt: Bitte Grund angeben (optional)
  </Alert>
)}
```

#### **8.2 Quick-Action Buttons** (1h)

**OpportunityCard:** Schnellzugriff für häufige Stage-Änderungen

```tsx
<Stack direction="row" spacing={1}>
  {stage !== 'CLOSED_WON' && (
    <IconButton
      size="small"
      color="success"
      onClick={() => handleQuickAction('won')}
      title="Als gewonnen markieren"
    >
      <CheckCircleIcon />
    </IconButton>
  )}

  {stage !== 'CLOSED_LOST' && (
    <IconButton
      size="small"
      color="error"
      onClick={() => handleQuickAction('lost')}
      title="Als verloren markieren"
    >
      <CancelIcon />
    </IconButton>
  )}
</Stack>
```

---

### **9. Activity Timeline UI** (2h) 📊 HINZUGEFÜGT 2025-10-18

**Business Value:** Verkäufer sehen komplette Opportunity-Historie auf einen Blick

**Note:** Activity-Tracking Backend bereits in Deliverable 5 geplant - hier nur UI!

#### **9.1 Timeline Component** (1.5h)

**Neue Datei:** `frontend/src/features/opportunity/components/ActivityTimeline.tsx`

```tsx
export const ActivityTimeline: React.FC<{ opportunityId: string }> = ({ opportunityId }) => {
  const [activities, setActivities] = useState<Activity[]>([]);

  useEffect(() => {
    // Load from API
    httpClient.get(`/api/opportunities/${opportunityId}/activities`).then(...);
  }, [opportunityId]);

  return (
    <Timeline>
      {activities.map((activity) => (
        <TimelineItem key={activity.id}>
          <TimelineSeparator>
            <TimelineDot color={getActivityColor(activity.type)}>
              {getActivityIcon(activity.type)}
            </TimelineDot>
            <TimelineConnector />
          </TimelineSeparator>
          <TimelineContent>
            <Typography variant="subtitle2">
              {activity.type} - {activity.title}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {formatDate(activity.createdAt)} • {activity.createdBy}
            </Typography>
            {activity.description && (
              <Typography variant="body2" sx={{ mt: 1 }}>
                {activity.description}
              </Typography>
            )}
          </TimelineContent>
        </TimelineItem>
      ))}
    </Timeline>
  );
};
```

#### **9.2 Integration in OpportunityDetailPage** (30 Min)

```tsx
<Accordion>
  <AccordionSummary>
    <Typography variant="h6">
      Aktivitäts-Historie ({activities.length})
    </Typography>
  </AccordionSummary>
  <AccordionDetails>
    <ActivityTimeline opportunityId={opportunity.id} />
  </AccordionDetails>
</Accordion>
```

---

### **10. Dokumente & Kontakte** (3h) 📎 HINZUGEFÜGT 2025-10-18

**Business Value:** Zentrale Ablage für Vertrags-Dokumente und Ansprechpartner

**SCOPE-DECISION:** MVP-Version nur Frontend-Platzhalter, Backend in späterem Sprint

#### **10.1 Dokumente-Sektion (Placeholder)** (1h)

```tsx
<Accordion>
  <AccordionSummary>
    <Typography variant="h6">
      Dokumente ({documents.length})
    </Typography>
  </AccordionSummary>
  <AccordionDetails>
    <Alert severity="info">
      Dokumenten-Management kommt in Sprint 2.2.x
      <br />
      Geplant: Angebote, Verträge, Präsentationen hochladen
    </Alert>

    {/* Placeholder UI */}
    <Stack spacing={2} sx={{ mt: 2, opacity: 0.5 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        <AttachFileIcon />
        <Typography>Angebot_Bella_Italia_Q4_2025.pdf</Typography>
        <Chip label="Placeholder" size="small" />
      </Box>
    </Stack>
  </AccordionDetails>
</Accordion>
```

#### **10.2 Kontakte-Sektion (Linked Entities)** (2h)

**Backend-Integration:** Link zu bestehenden LeadContacts/CustomerContacts

```tsx
<Accordion>
  <AccordionSummary>
    <Typography variant="h6">
      Ansprechpartner ({contacts.length})
    </Typography>
  </AccordionSummary>
  <AccordionDetails>
    {/* Load from Lead/Customer contacts */}
    {opportunity.leadId && (
      <Typography variant="caption">
        Ansprechpartner aus Lead #{opportunity.leadId}
      </Typography>
    )}

    <Stack spacing={2}>
      {contacts.map((contact) => (
        <Card key={contact.id} variant="outlined">
          <CardContent>
            <Stack direction="row" spacing={2} alignItems="center">
              <Avatar>{contact.name[0]}</Avatar>
              <Box>
                <Typography variant="subtitle2">{contact.name}</Typography>
                <Typography variant="caption">{contact.role}</Typography>
                <Stack direction="row" spacing={1} sx={{ mt: 0.5 }}>
                  <Chip icon={<PhoneIcon />} label={contact.phone} size="small" />
                  <Chip icon={<EmailIcon />} label={contact.email} size="small" />
                </Stack>
              </Box>
            </Stack>
          </CardContent>
        </Card>
      ))}
    </Stack>
  </AccordionDetails>
</Accordion>
```

---

### **11. Testing & Integration** (2h) 🧪 ERWEITERT

**Unit Tests (1h):**
- OpportunityDetailPage Edit-Mode: Save/Cancel Flow
- Stage-Change Validation: Backward-Move Warning
- ActivityTimeline: Rendering & Grouping

**Manual E2E Testing (1h):**
```
Test Case 4: Edit-Funktionalität
1. Open OpportunityDetailPage
2. Click "Bearbeiten"
3. Change: Name, Value, Close Date
4. Click "Speichern"
5. Verify: Changes persisted
6. Verify: Activity logged ("Opportunity bearbeitet")

Test Case 5: Stage-Änderung Manual
1. OpportunityDetailPage (Stage: QUALIFICATION)
2. Change Stage-Dropdown → PROPOSAL
3. Verify: Stage updated
4. Verify: Probability auto-updated (25% → 60%)
5. Verify: Activity logged ("Stage geändert: QUALIFICATION → PROPOSAL")

Test Case 6: Activity Timeline
1. OpportunityDetailPage mit 5 Activities
2. Open "Aktivitäts-Historie" Accordion
3. Verify: Timeline chronologisch (neueste oben)
4. Verify: Icons korrekt (CALL = 📞, MEETING = 👥, etc.)
5. Verify: User-Namen angezeigt
```

---

## ✅ DEFINITION OF DONE

### **Functional:**
- [x] Customer kann neue Opportunity erstellen (< 3 Klicks)
- [x] Customer-Opportunity-Historie zeigt alle Deals (gruppiert)
- [x] OpportunityType-Logik implementiert (SORTIMENTSERWEITERUNG Default für Bestandskunden)
- [x] Activity-Typen erweitert (Expansion-Call, Product-Demo, Contract-Renewal)

### **Technical:**
- [x] Backend: GET /api/customers/{id}/opportunities
- [x] Backend: OpportunityService.findByCustomerId()
- [x] Frontend: CreateOpportunityForCustomerDialog
- [x] Frontend: CustomerOpportunitiesList
- [x] Frontend: OpportunityHistoryCard
- [x] Backend: createForCustomer() setzt OpportunityType korrekt
- [x] Backend: CreateOpportunityForCustomerRequest hat opportunityType Feld
- [x] Unit Tests: CreateOpportunityForCustomerDialog
- [x] E2E Tests: Customer → Opportunity Flow

### **Quality:**
- [x] Code Review: 1 Approval
- [x] UI/UX: Historie übersichtlich (gruppiert, nicht chaotisch!)
- [x] Performance: Historie lädt < 1s (bei 20 Opportunities)

### **Documentation:**
- [x] TRIGGER_SPRINT_2_1_7_3.md erstellt (✅ Done)
- [x] TRIGGER_INDEX.md aktualisiert
- [x] CRM_COMPLETE_MASTER_PLAN_V5.md: Session Log

---

## 📊 SUCCESS METRICS

### **Usability:**
- Customer → Opportunity: Max 3 Klicks, < 20 Sekunden
- Historie: Auf einen Blick erkennbar: Offen/Gewonnen/Verloren

### **Business Impact:**
- Erweiterungsrate: Messbar (nach 3 Monaten: % Kunden mit SORTIMENTSERWEITERUNG-Opportunity)
- Customer-Lifetime-Value: Trackbar (Summe aller gewonnenen Opportunities pro Kunde)

---

## 🚀 PREREQUISITES

### **Ready to Start:**
- ✅ Sprint 2.1.7.2 COMPLETE (Customer-Dashboard funktioniert)
- ✅ Backend: POST /api/opportunities/for-customer/{id} existiert

### **Blockers:**
- ❌ Keine bekannt

---

## 📅 TIMELINE (Realistisch!)

**Tag 1 (8h):**
- "Neue Opportunity für Customer" Button (2h)
  - CustomerDetailPage Integration (1h)
  - CreateOpportunityForCustomerDialog (1h)
- Customer-Opportunity-Historie (3h)
  - CustomerOpportunitiesList (2h)
  - OpportunityHistoryCard (1h)
- Backend-Erweiterung (1h)
  - CustomerResource Endpoint (30 Min)
  - OpportunityService.findByCustomerId() (30 Min)
- OpportunityType-Logik implementieren (1h)
  - Backend: createForCustomer() erweitern (30 Min)
  - DTO: opportunityType Feld hinzufügen (30 Min)
- Activity-Tracking (1h)
  - Activity-Typen erweitern (30 Min)
  - ActivityDialog erweitern (30 Min)

**Puffer (optional):**
- Testing & Bugfixes (1h)

**Total: 8h = 1 Arbeitstag** ✅

---

## 🔗 RELATED WORK

### **Dependent Sprints:**
- Sprint 2.1.7.2: Customer-Management + Xentral (COMPLETE erforderlich!)

### **Follow-up Sprints:**
- Sprint 2.1.7.4: Advanced Filters & Analytics

---

## 📝 NOTES

### **Design Decisions:**

1. **OpportunityType für Bestandskunden-Unterscheidung:**
   - ✅ Freshfoodz Business Types umgesetzt (Sprint 2.1.7.1)
   - ✅ NEUGESCHAEFT, SORTIMENTSERWEITERUNG, NEUER_STANDORT, VERLAENGERUNG
   - ✅ Default für Customer-Opportunities: SORTIMENTSERWEITERUNG

2. **Customer-Opportunities starten bei NEEDS_ANALYSIS:**
   - NEW_LEAD + QUALIFICATION entfällt (Kunde ist ja schon qualifiziert!)
   - Nur Stages: NEEDS_ANALYSIS → PROPOSAL → NEGOTIATION → CLOSED_WON/LOST

3. **Opportunity-Historie gruppiert:**
   - Offen (sichtbar, aufgeklappt)
   - Gewonnen (sichtbar, aufgeklappt)
   - Verloren (Accordion, zugeklappt - weniger wichtig)

4. **Activity-Tracking erweitert:**
   - Expansion-Call, Product-Demo, Contract-Renewal
   - Für besseres Reporting (später: "Wie viele Expansion-Calls pro Monat?")

### **Technical Debt:**
- Opportunity-Historie: Aktuell kein Filtering (später: Filter nach Opportunity-Typ)
- Activity-Tracking: Aktuell keine Analytics (später: Dashboard "Aktivitäts-Übersicht")
- OpportunityType Validierung: Keine Backend-Validierung ob Type zu Customer-Status passt

---

**Sprint bereit für Kickoff!** 🚀

**Nächster Schritt:**
1. Feature-Branch: `git checkout -b feature/sprint-2-1-7-3-renewal-workflow`
2. Migration erstellen: `MIGRATION=$(./scripts/get-next-migration.sh | tail -1)`
3. Los geht's! 💪
