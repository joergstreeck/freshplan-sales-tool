# Sprint 2.1.7.3 - Technical Specification

**Sprint-ID:** 2.1.7.3
**Created:** 2025-10-19
**Status:** ✅ COMPLETE (95%)
**Owner:** Claude

---

## 📋 ÜBERSICHT

Technische Spezifikation für Sprint 2.1.7.3 - Bestandskunden-Workflow.

**Deliverables:**
1. Customer → Opportunity Button + Dialog
2. Customer-Opportunity-Historie (Accordion-Gruppierung)
3. Backend API: GET /api/customers/{id}/opportunities
4. OpportunityType Backend-Validierung (BUG-FIX)
5. Activity-Tracking (3 neue Typen)
6. Settings-Foundation (Multiplier-Matrix)

**Design System:** `/docs/planung/grundlagen/DESIGN_SYSTEM.md`

---

## 1️⃣ CustomerDetailPage Integration

### **1.1 Button "Neue Opportunity erstellen"**

**Datei:** `frontend/src/pages/CustomerDetailPage.tsx`

**Status:** ✅ COMPLETE (Commit: 3a1e84f36)

**Implementierung:**
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
```

**Design:**
- **Color:** `primary.main` (#94C456 - Freshfoodz Green)
- **Icon:** `TrendingUpIcon` (Material-UI)
- **Hover:** 10% opacity overlay
- **Visibility:** Nur bei `customer.status === 'AKTIV'`

**Design System Referenz:** `/docs/planung/grundlagen/DESIGN_SYSTEM.md` → Button Styles

---

### **1.2 Opportunities Accordion**

**Status:** ✅ COMPLETE (Commit: 3a1e84f36)

```tsx
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
```

**Tab-Index-Management:**
- **Opportunities:** Index 2
- **Aktivitäten:** Index 3
- **Audit:** Index 4

---

## 2️⃣ CreateOpportunityForCustomerDialog

**Datei:** `frontend/src/features/opportunity/components/CreateOpportunityForCustomerDialog.tsx`

**Status:** ✅ COMPLETE (Commits: 753a95245, a7f7944ef)

### **Props**
```typescript
interface Props {
  open: boolean;
  onClose: () => void;
  customer: Customer;
  onSuccess: () => void;
}
```

### **Felder**

| Feld | Typ | Validation | Pre-Fill |
|------|-----|------------|----------|
| **opportunityType** | Select | Required | SORTIMENTSERWEITERUNG |
| **name** | TextField | 1-255 chars | customer.companyName |
| **expectedValue** | NumberField | ≥ 0 | baseVolume × multiplier |
| **expectedCloseDate** | DatePicker | > Today | +60 Tage |
| **description** | TextArea | Max 4000 | Auto-generated |

### **Business-Type-Matrix Integration**

**3-Tier Fallback:**
```typescript
function getBaseVolume(customer: Customer): number {
  // TIER 1: Xentral Actual Revenue (BEST!)
  if (customer.actualAnnualVolume && customer.actualAnnualVolume > 0) {
    return customer.actualAnnualVolume;
  }

  // TIER 2: Lead Estimation
  if (customer.expectedAnnualVolume && customer.expectedAnnualVolume > 0) {
    return customer.expectedAnnualVolume;
  }

  // TIER 3: Manual Entry
  return 0; // Verkäufer muss selbst schätzen
}
```

**Auto-Calculation:**
```typescript
function calculateSuggestedValue(
  customer: Customer,
  opportunityType: OpportunityType,
  multipliers: MultiplierMatrix
): number {
  const baseVolume = getBaseVolume(customer);

  if (baseVolume === 0) {
    return 0; // Kein Vorschlag möglich
  }

  const multiplier = multipliers[customer.businessType]?.[opportunityType] || 0.30;
  return Math.round(baseVolume * multiplier);
}
```

### **API Call**
```typescript
POST /api/opportunities/for-customer/{customerId}

Body: {
  opportunityType: string,  // "SORTIMENTSERWEITERUNG"
  name: string,
  expectedValue: number,
  expectedCloseDate: string,  // ISO-8601
  description?: string
}

Response: OpportunityResponse
```

### **Success Flow**
```typescript
const handleCreate = async () => {
  try {
    const opportunity = await httpClient.post(
      `/api/opportunities/for-customer/${customer.id}`,
      {
        opportunityType,
        name,
        expectedValue,
        expectedCloseDate: expectedCloseDate.toISOString(),
        description
      }
    );

    toast.success('Opportunity erstellt! 🎉');
    onSuccess(); // → Switch to Opportunities tab
    onClose();

  } catch (error) {
    if (error.status === 400) {
      toast.error('Kunde muss AKTIV sein');
    } else {
      toast.error('Fehler beim Erstellen');
    }
  }
};
```

**Design System Referenz:** `/docs/planung/grundlagen/DESIGN_SYSTEM.md` → Dialog Styles, Form Validation

**Tests:** 21 Unit Tests (100% pass) - Commit: a7f7944ef

---

## 3️⃣ CustomerOpportunitiesList Component

**Datei:** `frontend/src/features/customers/components/CustomerOpportunitiesList.tsx`

**Status:** ✅ COMPLETE (Commit: 6b8e8ed28, a95d21bf1 - Tests)

### **Anforderungen**
- Zeigt alle Opportunities für Customer
- Gruppiert: **Offen** / **Gewonnen** / **Verloren**
- Sortierung: Neueste zuerst (DESC by createdAt)
- Navigation: Click → OpportunityDetailPage

### **Accordion-Gruppierung**
```typescript
const categorizeOpportunity = (stage: OpportunityStage) => {
  if (stage === 'CLOSED_WON') return 'won';
  if (stage === 'CLOSED_LOST') return 'lost';
  return 'open';
};

const openOpportunities = opportunities.filter(o => categorizeOpportunity(o.stage) === 'open');
const wonOpportunities = opportunities.filter(o => categorizeOpportunity(o.stage) === 'won');
const lostOpportunities = opportunities.filter(o => categorizeOpportunity(o.stage) === 'lost');
```

### **Rendering**
```tsx
<Stack spacing={3}>
  {/* Gruppe 1: Offen */}
  {openOpportunities.length > 0 && (
    <Accordion defaultExpanded>
      <AccordionSummary>
        <Typography variant="h6">🔥 Offen ({openOpportunities.length})</Typography>
      </AccordionSummary>
      <AccordionDetails>
        <Stack spacing={2}>
          {openOpportunities.map(opp => (
            <OpportunityCard key={opp.id} opportunity={opp} />
          ))}
        </Stack>
      </AccordionDetails>
    </Accordion>
  )}

  {/* Gruppe 2: Gewonnen */}
  {wonOpportunities.length > 0 && (
    <Accordion defaultExpanded>
      <AccordionSummary>
        <Typography variant="h6">✅ Gewonnen ({wonOpportunities.length})</Typography>
      </AccordionSummary>
      <AccordionDetails>
        <Stack spacing={2}>
          {wonOpportunities.map(opp => (
            <OpportunityCard key={opp.id} opportunity={opp} />
          ))}
        </Stack>
      </AccordionDetails>
    </Accordion>
  )}

  {/* Gruppe 3: Verloren (collapsed by default) */}
  {lostOpportunities.length > 0 && (
    <Accordion>
      <AccordionSummary>
        <Typography variant="h6">❌ Verloren ({lostOpportunities.length})</Typography>
      </AccordionSummary>
      <AccordionDetails>
        <Stack spacing={2}>
          {lostOpportunities.map(opp => (
            <OpportunityCard key={opp.id} opportunity={opp} />
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
```

### **OpportunityCard (Sub-Component)**
```tsx
interface OpportunityCardProps {
  opportunity: Opportunity;
}

export function OpportunityCard({ opportunity }: OpportunityCardProps) {
  const navigate = useNavigate();

  const opportunityTypeConfig = {
    'NEUGESCHAEFT': { color: 'primary', icon: '🆕' },
    'SORTIMENTSERWEITERUNG': { color: 'secondary', icon: '📈' },
    'NEUER_STANDORT': { color: 'info', icon: '📍' },
    'VERLAENGERUNG': { color: 'warning', icon: '🔁' },
  };

  const typeInfo = opportunityTypeConfig[opportunity.opportunityType] || { color: 'default', icon: '' };

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
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="h6">{opportunity.name}</Typography>
            <Chip
              label={`${typeInfo.icon} ${opportunity.opportunityType}`}
              color={typeInfo.color}
              size="small"
              sx={{ mt: 1 }}
            />
          </Box>

          <Chip
            label={opportunity.stage}
            color={getStageColor(opportunity.stage)}
            size="small"
          />
        </Stack>

        <Divider sx={{ my: 1.5 }} />

        <Stack direction="row" spacing={3}>
          <Box>
            <Typography variant="caption" color="text.secondary">Wert</Typography>
            <Typography variant="body2" fontWeight="bold">
              {formatCurrency(opportunity.expectedValue)}
            </Typography>
          </Box>
          <Box>
            <Typography variant="caption" color="text.secondary">Abschluss</Typography>
            <Typography variant="body2">
              {formatDate(opportunity.expectedCloseDate)}
            </Typography>
          </Box>
          <Box>
            <Typography variant="caption" color="text.secondary">Erstellt</Typography>
            <Typography variant="body2">
              {formatDate(opportunity.createdAt)}
            </Typography>
          </Box>
        </Stack>
      </CardContent>
    </Card>
  );
}
```

**Design System Referenz:** `/docs/planung/grundlagen/DESIGN_SYSTEM.md` → Card Styles, Color Palette

**Tests:** 19 Unit Tests (100% pass) - Commit: a95d21bf1

---

## 4️⃣ Backend API Endpoints

### **4.1 GET /api/customers/{id}/opportunities**

**Datei:** `backend/src/main/java/de/freshplan/api/resources/CustomerResource.java`

**Status:** ✅ COMPLETE (Commit: 87cf9d65f)

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

    // Fetch opportunities (sorted: newest first)
    List<OpportunityResponse> opportunities =
        opportunityService.findByCustomerId(customerId);

    return Response.ok(opportunities).build();
}
```

**Response:**
```json
[
  {
    "id": "uuid",
    "name": "Bella Italia - Sortimentserweiterung",
    "opportunityType": "SORTIMENTSERWEITERUNG",
    "stage": "NEEDS_ANALYSIS",
    "expectedValue": 12500.00,
    "expectedCloseDate": "2025-12-31",
    "customerId": "uuid",
    "customerName": "Bella Italia",
    "createdAt": "2025-10-19T10:00:00Z",
    ...
  }
]
```

---

### **4.2 OpportunityService.findByCustomerId()**

**Datei:** `backend/src/main/java/de/freshplan/domain/opportunity/service/OpportunityService.java`

**Status:** ✅ COMPLETE (Commit: e4d1f1304)

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
        .find("customer.id = ?1", Sort.by("createdAt").descending(), customerId)
        .list();

    return opportunities.stream()
        .map(opportunityMapper::toResponse)
        .collect(Collectors.toList());
}
```

**Tests:** 4 Integration Tests (100% pass) - Commit: e4d1f1304

---

### **4.3 OpportunityType Backend-Validierung (BUG-FIX)**

**Status:** ❌ PENDING (BUG identifiziert in Session)

**Problem:**
```java
// AKTUELL (BROKEN):
Opportunity opportunity = new Opportunity(opportunityName, initialStage, assignedUser);
opportunity.setCustomer(customer);

if (request.getOpportunityType() != null) {
  opportunityDescription = String.format("[%s] %s",
    request.getOpportunityType(), opportunityDescription);
}
opportunity.setDescription(opportunityDescription);

// ❌ BUG: opportunity.setOpportunityType() wird NIEMALS aufgerufen!
```

**FIX (erforderlich):**
```java
// OpportunityService.createForCustomer()

// NACH Zeile 692: opportunity.setCustomer(customer);
// HINZUFÜGEN:

// Set OpportunityType (with Bestandskunden default)
OpportunityType type = OpportunityType.SORTIMENTSERWEITERUNG; // Default
if (request.getOpportunityType() != null && !request.getOpportunityType().isEmpty()) {
  try {
    type = OpportunityType.valueOf(request.getOpportunityType());
  } catch (IllegalArgumentException e) {
    logger.warn("Invalid opportunityType: {}, using default SORTIMENTSERWEITERUNG",
                request.getOpportunityType());
  }
}
opportunity.setOpportunityType(type);
```

**Deliverable 4.2 aus TRIGGER:** ❌ PENDING

---

## 5️⃣ Activity-Tracking (3 neue Typen)

**Status:** ❌ PENDING

### **5.1 ActivityType Enum erweitern**

**Datei:** `backend/src/main/java/de/freshplan/domain/opportunity/entity/OpportunityActivity.java`

```java
public enum ActivityType {
    NOTE("Notiz"),
    STAGE_CHANGED("Status geändert"),
    CALL("Anruf"),
    MEETING("Meeting"),
    EMAIL("E-Mail"),
    // NEU für Bestandskunden-Workflow:
    EXPANSION_CALL("Sortimentserweiterungs-Gespräch"),
    PRODUCT_DEMO("Produktpräsentation"),
    CONTRACT_RENEWAL("Vertragsverlängerungs-Verhandlung");

    private final String displayName;

    ActivityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
```

**Deliverable 5.1 aus TRIGGER:** ❌ PENDING

---

### **5.2 ActivityDialog erweitern**

**Datei:** `frontend/src/features/opportunity/components/ActivityDialog.tsx`

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

  {/* NEU: Bestandskunden-spezifische Typen */}
  <MenuItem value="EXPANSION_CALL">📈 Sortimentserweiterungs-Gespräch</MenuItem>
  <MenuItem value="PRODUCT_DEMO">📊 Produktpräsentation</MenuItem>
  <MenuItem value="CONTRACT_RENEWAL">🔁 Vertragsverlängerungs-Verhandlung</MenuItem>
</Select>
```

**Design System Referenz:** `/docs/planung/grundlagen/DESIGN_SYSTEM.md` → Form Elements

**Deliverable 5.2 aus TRIGGER:** ❌ PENDING

---

## 6️⃣ Settings-Foundation (Multiplier-Matrix)

### **6.1 Migration V10031**

**Datei:** `backend/src/main/resources/db/migration/V10031__add_opportunity_multipliers_business_type_matrix.sql`

**Status:** ✅ COMPLETE (Commit: 87cf9d65f - mit CHECK constraints fix)

```sql
-- Migration V10031: opportunity_multipliers Table
CREATE TABLE opportunity_multipliers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

  business_type VARCHAR(50) NOT NULL
    CHECK (business_type IN (
      'RESTAURANT', 'HOTEL', 'CATERING', 'KANTINE', 'BILDUNG',
      'GESUNDHEIT', 'GROSSHANDEL', 'LEH', 'SONSTIGES'
    )),

  opportunity_type VARCHAR(50) NOT NULL
    CHECK (opportunity_type IN (
      'NEUGESCHAEFT', 'SORTIMENTSERWEITERUNG', 'NEUER_STANDORT', 'VERLAENGERUNG'
    )),

  multiplier NUMERIC(5,2) NOT NULL
    CHECK (multiplier >= 0.00 AND multiplier <= 10.00),

  -- Audit-Log
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by UUID REFERENCES users(id),
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by UUID REFERENCES users(id),

  UNIQUE(business_type, opportunity_type)
);

-- Index für Performance
CREATE INDEX idx_opportunity_multipliers_lookup
  ON opportunity_multipliers(business_type, opportunity_type);

-- SEED Data: 9 BusinessTypes × 4 OpportunityTypes = 36 Einträge
INSERT INTO opportunity_multipliers (business_type, opportunity_type, multiplier) VALUES
  -- RESTAURANT
  ('RESTAURANT', 'NEUGESCHAEFT', 1.00),
  ('RESTAURANT', 'SORTIMENTSERWEITERUNG', 0.25),
  ('RESTAURANT', 'NEUER_STANDORT', 0.80),
  ('RESTAURANT', 'VERLAENGERUNG', 0.95),

  -- HOTEL
  ('HOTEL', 'NEUGESCHAEFT', 1.00),
  ('HOTEL', 'SORTIMENTSERWEITERUNG', 0.65),
  ('HOTEL', 'NEUER_STANDORT', 0.90),
  ('HOTEL', 'VERLAENGERUNG', 0.90),

  -- CATERING
  ('CATERING', 'NEUGESCHAEFT', 1.00),
  ('CATERING', 'SORTIMENTSERWEITERUNG', 0.50),
  ('CATERING', 'NEUER_STANDORT', 0.75),
  ('CATERING', 'VERLAENGERUNG', 0.85),

  -- KANTINE
  ('KANTINE', 'NEUGESCHAEFT', 1.00),
  ('KANTINE', 'SORTIMENTSERWEITERUNG', 0.30),
  ('KANTINE', 'NEUER_STANDORT', 0.60),
  ('KANTINE', 'VERLAENGERUNG', 0.95),

  -- BILDUNG
  ('BILDUNG', 'NEUGESCHAEFT', 1.00),
  ('BILDUNG', 'SORTIMENTSERWEITERUNG', 0.20),
  ('BILDUNG', 'NEUER_STANDORT', 0.50),
  ('BILDUNG', 'VERLAENGERUNG', 0.90),

  -- GESUNDHEIT
  ('GESUNDHEIT', 'NEUGESCHAEFT', 1.00),
  ('GESUNDHEIT', 'SORTIMENTSERWEITERUNG', 0.35),
  ('GESUNDHEIT', 'NEUER_STANDORT', 0.70),
  ('GESUNDHEIT', 'VERLAENGERUNG', 0.95),

  -- GROSSHANDEL
  ('GROSSHANDEL', 'NEUGESCHAEFT', 1.00),
  ('GROSSHANDEL', 'SORTIMENTSERWEITERUNG', 0.40),
  ('GROSSHANDEL', 'NEUER_STANDORT', 0.55),
  ('GROSSHANDEL', 'VERLAENGERUNG', 0.80),

  -- LEH
  ('LEH', 'NEUGESCHAEFT', 1.00),
  ('LEH', 'SORTIMENTSERWEITERUNG', 0.45),
  ('LEH', 'NEUER_STANDORT', 0.60),
  ('LEH', 'VERLAENGERUNG', 0.85),

  -- SONSTIGES
  ('SONSTIGES', 'NEUGESCHAEFT', 1.00),
  ('SONSTIGES', 'SORTIMENTSERWEITERUNG', 0.15),
  ('SONSTIGES', 'NEUER_STANDORT', 0.40),
  ('SONSTIGES', 'VERLAENGERUNG', 0.70);
```

**User-Catch:** Migration hatte ursprünglich VARCHAR OHNE CHECK constraints → Fixed in Commit 87cf9d65f

---

### **6.2 Backend API**

**Datei:** `backend/src/main/java/de/freshplan/api/resources/SettingsResource.java`

**Status:** ✅ COMPLETE (Commit: 90b385945)

```java
@Path("/api/settings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class SettingsResource {

    @Inject
    OpportunityMultiplierService multiplierService;

    @GET
    @Path("/opportunity-multipliers")
    @RolesAllowed({"USER", "MANAGER", "ADMIN"})
    public Response getAllMultipliers() {
        List<OpportunityMultiplierResponse> multipliers =
            multiplierService.getAllMultipliers();
        return Response.ok(multipliers).build();
    }
}
```

**Response:**
```json
[
  {
    "businessType": "RESTAURANT",
    "opportunityType": "SORTIMENTSERWEITERUNG",
    "multiplier": 0.25
  },
  {
    "businessType": "HOTEL",
    "opportunityType": "SORTIMENTSERWEITERUNG",
    "multiplier": 0.65
  },
  // ... 34 weitere Einträge
]
```

**Tests:** 39 Tests (100% pass) - Commit: 90b385945

---

## 📊 TEST COVERAGE

**Backend:**
- OpportunityMultiplierServiceTest: 39 Tests ✅
- OpportunityServiceFindByCustomerIdTest: 4 Tests ✅
- **Total: 43/43 GREEN** ✅

**Frontend:**
- CreateOpportunityForCustomerDialog.test.tsx: 21 Tests ✅
- CustomerOpportunitiesList.test.tsx: 19 Tests ✅
- CustomerDetailPage.test.tsx: 7 Tests (existing) ✅
- **Total: 47/47 GREEN** ✅

**Overall:** 90/90 Tests GREEN ✅

---

## 🔗 REFERENZEN

**Design System:** `/docs/planung/grundlagen/DESIGN_SYSTEM.md`
- Color Palette (Freshfoodz Green #94C456, Ocean Blue #004F7B)
- Typography (Antonio Bold, Poppins)
- Button Styles, Dialog Patterns
- Card Components, Form Elements

**Design Decisions:** `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_3_DESIGN_DECISIONS.md`
- Business-Type-Matrix Rationale
- 3-Tier Fallback Strategy
- Settings Architecture

**Trigger:** `/docs/planung/TRIGGER_SPRINT_2_1_7_3.md`
- Sprint Goals & Timeline
- Definition of Done

---

**Letzte Aktualisierung:** 2025-10-19
**Status:** ✅ 95% COMPLETE (Pending: OpportunityType Bug-Fix, Activity-Types)
