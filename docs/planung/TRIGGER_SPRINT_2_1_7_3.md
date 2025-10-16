# 🚀 Sprint 2.1.7.3 - RENEWAL-Workflow (Upsell/Cross-sell)

**Sprint-ID:** 2.1.7.3
**Status:** 📋 PLANNING
**Priority:** P2 (Medium)
**Estimated Effort:** 8h (1 Arbeitstag)
**Owner:** TBD
**Created:** 2025-10-16
**Dependencies:** Sprint 2.1.7.2 COMPLETE

---

## 🎯 SPRINT GOALS

### **Business Value**

**Vertriebler können Bestandskunden neue Produkte verkaufen (Upsell/Cross-sell/Renewal):**

- ✅ "Neue Opportunity für Customer" Button in CustomerDetailPage
- ✅ Customer-Opportunity-Historie (alle Deals zu diesem Kunden)
- ✅ RENEWAL-Stage Logik klären (vs. NEW_LEAD)
- ✅ Activity-Tracking für Upsell/Cross-sell
- ✅ Opportunity-Verlinkung: Kunde → Opportunities (bidirektional)

**Business Impact:**
- **Upsell-Opportunity erkennbar:** Verkäufer sieht: "Kunde kauft regelmäßig → Zeit für Produkterweiterung!"
- **Provision-Transparenz:** RENEWAL-Deals werden separat getrackt (für Provision-Berechnung)
- **Customer-Lifetime-Value:** Alle Deals zu einem Kunden sichtbar (Historie)
- **Beziehungs-Management:** Verkäufer bleibt in Kontakt mit bestehenden Kunden

### **Technical Context**

**Backend existiert bereits:**
- ✅ POST /api/opportunities/for-customer/{customerId} (aus Sprint 2.1.7.0)
- ✅ OpportunityService.createForCustomer() implementiert
- ✅ RENEWAL-Stage in OpportunityStage Enum

**Was JETZT gebaut wird:**
- ✅ CustomerDetailPage: "Neue Opportunity" Button
- ✅ CreateOpportunityForCustomerDialog (ähnlich wie CreateOpportunityDialog aus 2.1.7.1)
- ✅ CustomerOpportunitiesList Component (zeigt alle Opportunities zu diesem Kunden)
- ✅ Opportunity-Historie Timeline (gruppiert nach Status: Offen/Gewonnen/Verloren)
- ✅ Activity-Tracking: "Upsell-Call", "Product-Demo"

**NICHT in diesem Sprint:**
- ❌ Advanced Analytics (Revenue Forecasting) → Sprint 2.1.7.4
- ❌ Opportunity ROI Calculator → Später
- ❌ Automatische Upsell-Vorschläge (AI-basiert) → Weit später

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
  - **Opportunity-Typ** (Select: "Upsell", "Cross-sell", "Renewal", "Standard")
  - **Name** (Text, auto-generated: "{companyName} - {type} {timeframe}")
  - **Timeframe** (Text: "Q2 2025", "H1 2025", etc.)
  - **Expected Value** (Number, EUR)
  - **Expected Close Date** (DatePicker)
  - **Description** (TextArea)

**Pre-fill aus Customer-Daten:**
```tsx
const defaultValues = {
  opportunityType: 'Upsell', // Default
  name: `${customer.companyName} - Upsell ${currentQuarter}`,
  expectedValue: customer.expectedAnnualVolume ? customer.expectedAnnualVolume * 0.2 : 0, // 20% vom Jahresvolumen
  expectedCloseDate: addDays(new Date(), 60), // 60 Tage (RENEWAL-Deals brauchen länger)
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
  opportunityType: 'Upsell' | 'Cross-sell' | 'Renewal' | 'Standard',
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
- Badge: Opportunity-Typ (Upsell/Cross-sell/Renewal)

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
    'Upsell': { color: 'primary', icon: '📈' },
    'Cross-sell': { color: 'secondary', icon: '🔄' },
    'Renewal': { color: 'warning', icon: '🔁' },
    'Standard': { color: 'default', icon: '💼' },
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

### **4. RENEWAL-Stage Logik klären** (1h)

#### **4.1 Design-Entscheidung dokumentieren** (30 Min)

**Frage:** Wie unterscheiden sich RENEWAL-Opportunities von NEW_LEAD-Opportunities?

**Option A: RENEWAL als eigene Stage (aktuell implementiert):**
```
Stages: NEW_LEAD → QUALIFICATION → NEEDS_ANALYSIS → PROPOSAL → NEGOTIATION → CLOSED_WON/LOST → RENEWAL
```

**Problem:** RENEWAL kommt NACH CLOSED - das macht keinen Sinn!

**Option B: RENEWAL als Opportunity-Typ (EMPFOHLEN!):**
```
Opportunity.opportunityType = 'Upsell' | 'Cross-sell' | 'Renewal' | 'Standard'
Opportunity.stage = NEEDS_ANALYSIS (Start-Stage für Customer-Opportunities)

Stages: NEEDS_ANALYSIS → PROPOSAL → NEGOTIATION → CLOSED_WON/LOST
(NEW_LEAD + QUALIFICATION entfällt - Kunde ist ja schon qualifiziert!)
```

**Entscheidung:**
- RENEWAL-Stage wird ENTFERNT aus Enum (Breaking Change!)
- Stattdessen: `opportunityType` Feld nutzen (bereits in V10026 vorhanden!)
- Customer-Opportunities starten bei NEEDS_ANALYSIS (nicht NEW_LEAD)

#### **4.2 Migration: RENEWAL-Stage entfernen** (30 Min)

**Migration erstellen:**
```bash
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
# Beispiel: V10033__remove_renewal_stage.sql
```

```sql
-- VORSICHT: Breaking Change! Nur wenn keine Production-Daten!

-- Schritt 1: Prüfe ob RENEWAL-Stage in Nutzung
SELECT COUNT(*) FROM opportunities WHERE stage = 'RENEWAL';
-- Wenn > 0: STOPP! Daten müssen migriert werden!

-- Schritt 2: Migriere RENEWAL zu NEEDS_ANALYSIS + opportunityType = 'Renewal'
UPDATE opportunities
SET
    stage = 'NEEDS_ANALYSIS',
    opportunity_type = 'Renewal'
WHERE stage = 'RENEWAL';

-- Schritt 3: Backend OpportunityStage Enum anpassen (Code-Change!)
-- Entferne: RENEWAL("Verlängerung", 50)
-- Behalte: 7 Stages (NEW_LEAD → CLOSED_LOST)

COMMENT ON COLUMN opportunities.stage IS
'Opportunity Stage (7 Stages total):
NEW_LEAD (10%), QUALIFICATION (25%), NEEDS_ANALYSIS (40%),
PROPOSAL (60%), NEGOTIATION (80%), CLOSED_WON (100%), CLOSED_LOST (0%)

RENEWAL-Deals nutzen opportunityType = "Renewal" und starten bei NEEDS_ANALYSIS';
```

**Backend-Code-Change:**
```java
// OpportunityStage.java - RENEWAL entfernen!
public enum OpportunityStage {
    NEW_LEAD("Neuer Lead", 10),
    QUALIFICATION("Qualifizierung", 25),
    NEEDS_ANALYSIS("Bedarfsanalyse", 40),
    PROPOSAL("Angebot", 60),
    NEGOTIATION("Verhandlung", 80),
    CLOSED_WON("Gewonnen", 100),
    CLOSED_LOST("Verloren", 0);
    // RENEWAL entfernt! ← BREAKING CHANGE!

    // ... rest bleibt gleich
}
```

---

### **5. Activity-Tracking für Upsell** (1h)

#### **5.1 Activity-Typen erweitern** (30 Min)

**Datei:** `backend/src/main/java/de/freshplan/domain/opportunity/entity/OpportunityActivity.java`

```java
public enum ActivityType {
    NOTE("Notiz"),
    STAGE_CHANGED("Status geändert"),
    CALL("Anruf"),
    MEETING("Meeting"),
    EMAIL("E-Mail"),
    UPSELL_CALL("Upsell-Gespräch"), // ← NEU!
    PRODUCT_DEMO("Produktpräsentation"), // ← NEU!
    RENEWAL_NEGOTIATION("Verlängerungs-Verhandlung"); // ← NEU!

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
  <MenuItem value="UPSELL_CALL">🎯 Upsell-Gespräch</MenuItem>
  <MenuItem value="PRODUCT_DEMO">📊 Produktpräsentation</MenuItem>
  <MenuItem value="RENEWAL_NEGOTIATION">🔁 Verlängerungs-Verhandlung</MenuItem>
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
   - Type: "Upsell"
   - Name: "Bella Italia - Upsell Q4 2025"
   - Expected Value: 20% vom Jahresvolumen
   - Close Date: +60 Tage
5. Submit
6. Verify Toast: "Opportunity erstellt!"
7. Verify: Opportunity in "Offen"-Gruppe
8. Verify: Stage = NEEDS_ANALYSIS (nicht NEW_LEAD!)

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

Test Case 3: RENEWAL-Stage Migration
1. Wenn RENEWAL-Stage-Daten existieren:
2. Run Migration (get-next-migration.sh)
3. Verify: Alle RENEWAL → NEEDS_ANALYSIS
4. Verify: opportunityType = 'Renewal' gesetzt
5. Verify: Backend kompiliert (ohne RENEWAL Enum!)
```

---

## ✅ DEFINITION OF DONE

### **Functional:**
- [x] Customer kann neue Opportunity erstellen (< 3 Klicks)
- [x] Customer-Opportunity-Historie zeigt alle Deals (gruppiert)
- [x] RENEWAL-Stage entfernt (opportunityType stattdessen!)
- [x] Activity-Typen erweitert (Upsell-Call, Product-Demo, Renewal-Negotiation)

### **Technical:**
- [x] Backend: GET /api/customers/{id}/opportunities
- [x] Backend: OpportunityService.findByCustomerId()
- [x] Frontend: CreateOpportunityForCustomerDialog
- [x] Frontend: CustomerOpportunitiesList
- [x] Frontend: OpportunityHistoryCard
- [x] Migration: RENEWAL-Stage entfernen (Script: get-next-migration.sh)
- [x] OpportunityStage Enum: 7 Stages (ohne RENEWAL)
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
- Upsell-Rate: Messbar (nach 3 Monaten: % Kunden mit Upsell-Opportunity)
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
- RENEWAL-Stage Logik klären (1h)
  - Design-Entscheidung (30 Min)
  - Migration erstellen (30 Min)
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

1. **RENEWAL als Opportunity-Typ (nicht Stage!):**
   - RENEWAL-Stage wird entfernt (Breaking Change!)
   - Stattdessen: `opportunityType` Feld nutzen
   - Customer-Opportunities starten bei NEEDS_ANALYSIS

2. **Customer-Opportunities starten bei NEEDS_ANALYSIS:**
   - NEW_LEAD + QUALIFICATION entfällt (Kunde ist ja schon qualifiziert!)
   - Nur Stages: NEEDS_ANALYSIS → PROPOSAL → NEGOTIATION → CLOSED_WON/LOST

3. **Opportunity-Historie gruppiert:**
   - Offen (sichtbar, aufgeklappt)
   - Gewonnen (sichtbar, aufgeklappt)
   - Verloren (Accordion, zugeklappt - weniger wichtig)

4. **Activity-Tracking erweitert:**
   - Upsell-Call, Product-Demo, Renewal-Negotiation
   - Für besseres Reporting (später: "Wie viele Upsell-Calls pro Monat?")

### **Technical Debt:**
- RENEWAL-Stage Migration: Muss SAUBER getestet werden (Breaking Change!)
- Opportunity-Historie: Aktuell kein Filtering (später: Filter nach Opportunity-Typ)
- Activity-Tracking: Aktuell keine Analytics (später: Dashboard "Aktivitäts-Übersicht")

---

**Sprint bereit für Kickoff!** 🚀

**Nächster Schritt:**
1. Feature-Branch: `git checkout -b feature/sprint-2-1-7-3-renewal-workflow`
2. Migration erstellen: `MIGRATION=$(./scripts/get-next-migration.sh | tail -1)`
3. Los geht's! 💪
