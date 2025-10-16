# 🚀 Sprint 2.1.7.1 - Lead → Opportunity Workflow (FOKUSSIERT)

**Sprint-ID:** 2.1.7.1
**Status:** 🔶 PLANNING
**Priority:** P1 (High)
**Estimated Effort:** 17h (2 Arbeitstage)
**Owner:** TBD
**Created:** 2025-10-13
**Updated:** 2025-10-16 (nach Diskussion mit AI + Jörg)

---

## 🎯 SPRINT GOALS

### **Business Value**

**FOKUSSIERT auf EINEN Workflow:** Lead → Opportunity Conversion

Vertriebler können:
- ✅ Leads in Opportunities konvertieren (per Klick)
- ✅ Opportunity-Pipeline filtern (Active/Closed/All)
- ✅ "Meine Deals" anzeigen
- ✅ Opportunities durchsuchen (Quick-Search)
- ✅ Lead-Detail zeigt Opportunity-Status (Traceability)

**NICHT in diesem Sprint (verschoben zu 2.1.7.2/2.1.7.3):**
- ❌ Customer → Opportunity (Upsell/Cross-sell) → Sprint 2.1.7.3
- ❌ Opportunity → Customer Conversion → Sprint 2.1.7.2
- ❌ Xentral-Integration → Sprint 2.1.7.2
- ❌ Customer-Dashboard → Sprint 2.1.7.2

### **Technical Context**

**Backend ist fertig (Sprint 2.1.7.0):**
- ✅ V10026: `opportunities.lead_id` FK
- ✅ OpportunityService.createFromLead()
- ✅ REST API: POST /api/opportunities/from-lead/{leadId}
- ✅ V90003: 10 DEV-SEED Opportunities

**Was JETZT gebaut wird:**
- ✅ UI zum Anlegen von Opportunities aus Leads
- ✅ "Lead → Opportunity" Button in LeadDetailPage
- ✅ Kanban Filter (Active/Closed/All) + "Meine Deals"
- ✅ Quick-Search in Pipeline
- ✅ Pagination (15 pro Spalte)
- ✅ Drag & Drop Fix (transformOrigin Bug)

---

## ⚠️ WICHTIG: Backend-Vorbereitung (VOR Sprint-Start!)

### **OpportunityStage Enum: RENEWAL entfernen**

**KRITISCH:** RENEWAL-Stage muss VOR Frontend-Development entfernt werden!

**Backend-Cleanup:**
```java
// backend/src/main/java/de/freshplan/domain/opportunity/entity/OpportunityStage.java
public enum OpportunityStage {
    NEW_LEAD("Neuer Lead", "#ff9800", 10),
    QUALIFICATION("Qualifizierung", "#2196f3", 25),
    NEEDS_ANALYSIS("Bedarfsanalyse", "#009688", 40),
    PROPOSAL("Angebotserstellung", "#94c456", 60),
    NEGOTIATION("Verhandlung", "#4caf50", 80),
    CLOSED_WON("Gewonnen", "#4caf50", 100),
    CLOSED_LOST("Verloren", "#f44336", 0);
    // RENEWAL entfernt! ❌ (wird durch opportunityType ersetzt)
}
```

**Migration (wenn RENEWAL-Daten existieren):**
```bash
# Nur FALLS bereits RENEWAL-Stage Daten im System sind:
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
# Erstelle Migration zum Migrieren von RENEWAL → NEEDS_ANALYSIS
```

**Hinweis:** RENEWAL-Workflow kommt in Sprint 2.1.7.3 als `opportunityType` Feld zurück!

---

## 📦 DELIVERABLES

### **1. CreateOpportunityDialog Component** (3h)

**Neue Datei:** `frontend/src/features/opportunity/components/CreateOpportunityDialog.tsx`

**Anforderungen:**
- MUI Dialog mit Form-Validation
- Felder:
  - **Name** (Text, optional - auto-generated: "Vertragschance - {companyName} ({dealType})")
  - **Deal Type** (Select: "Liefervertrag", "Testphase", "Pilot", "Vollversorgung", "Rahmenvertrag")
  - **Timeframe** (Text: "Q2 2025", "H1 2025", "2025", etc.)
  - **Expected Value** (Number, EUR, pre-filled aus Lead.estimatedVolume)
  - **Expected Close Date** (DatePicker, default: +30 Tage)
  - **Description** (TextArea, optional)

**Pre-fill aus Lead-Daten:**
```tsx
const defaultValues = {
  name: `Vertragschance - ${lead.companyName} (${dealType})`,
  expectedValue: lead.estimatedVolume || 0,
  expectedCloseDate: addDays(new Date(), 30),
  description: `Deal mit ${lead.companyName}. Lead-Score: ${lead.leadScore}/100`,
};
```

**Validation:**
- Expected Value > 0 (required)
- Close Date > Today (must be in future)
- Deal Type selected (required)

**Error Handling:**
- API Errors → Toast + Dialog bleibt offen
- Validation Errors → Inline unter Feld (MUI helperText)
- 412 Conflict (ETag) → Reload Lead + Toast "Daten wurden zwischenzeitlich geändert"

**Success:**
- Toast: "Opportunity erstellt! 🎉"
- Lead Status → CONVERTED (automatisch via Backend)
- Redirect zu OpportunityPipeline (mit neuem Card highlighted)

**API Call:**
```typescript
POST /api/opportunities/from-lead/{leadId}
Body: {
  name?: string,
  dealType: string,
  timeframe?: string,
  expectedValue: number,
  expectedCloseDate: string (ISO-8601),
  description?: string
}
```

---

### **2. LeadDetailPage Integration** (2h)

**Datei:** `frontend/src/pages/LeadDetailPage.tsx`

**Änderung 1: Button hinzufügen**
```tsx
{/* Nur bei QUALIFIED oder ACTIVE Status */}
{(lead.status === 'QUALIFIED' || lead.status === 'ACTIVE') && (
  <Button
    variant="contained"
    color="primary"
    startIcon={<TrendingUpIcon />}
    onClick={() => setShowOpportunityDialog(true)}
    sx={{
      bgcolor: theme.palette.primary.main,
      '&:hover': { bgcolor: theme.palette.primary.dark }
    }}
  >
    In Opportunity konvertieren
  </Button>
)}

{/* Bei CONVERTED Status: Info-Badge */}
{lead.status === 'CONVERTED' && (
  <Alert severity="success" icon={<CheckCircleIcon />}>
    Lead wurde zu Opportunity konvertiert am {formatDate(lead.updatedAt)}
  </Alert>
)}
```

**Änderung 2: Opportunities-Sektion hinzufügen**
```tsx
{/* Neuer Accordion in LeadDetailPage */}
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
            {opportunities[0].name} • {opportunities[0].stage}
          </Typography>
        )}
      </Box>
    </Box>
  </AccordionSummary>

  <AccordionDetails>
    <LeadOpportunitiesList leadId={lead.id} />
  </AccordionDetails>
</Accordion>
```

**Backend-Erweiterung nötig:**
```java
// LeadResource.java - Neuer Endpoint
@GET
@Path("/{id}/opportunities")
@RolesAllowed({"admin", "manager", "sales"})
public Response getLeadOpportunities(@PathParam("id") Long leadId) {
    List<OpportunityResponse> opportunities =
        opportunityService.findByLeadId(leadId);
    return Response.ok(opportunities).build();
}

// OpportunityService.java - Neue Methode
public List<OpportunityResponse> findByLeadId(Long leadId) {
    List<Opportunity> opportunities = opportunityRepository
        .find("lead.id", leadId)
        .list();
    return opportunities.stream()
        .map(opportunityMapper::toResponse)
        .collect(Collectors.toList());
}
```

---

### **3. LeadOpportunitiesList Component** (2h)

**Neue Datei:** `frontend/src/features/leads/components/LeadOpportunitiesList.tsx`

**Anforderungen:**
- Zeigt alle Opportunities für diesen Lead
- Karten-Layout (ähnlich wie OpportunityCard)
- Sortierung: Neueste zuerst
- Link zu Opportunity-Detail

**Darstellung:**
```tsx
<Stack spacing={2}>
  {opportunities.length === 0 && (
    <Alert severity="info">
      Noch keine Opportunities für diesen Lead erstellt.
    </Alert>
  )}

  {opportunities.map(opp => (
    <Card key={opp.id} sx={{ cursor: 'pointer' }} onClick={() => navigate(`/opportunities/${opp.id}`)}>
      <CardContent>
        <Stack direction="row" justifyContent="space-between" alignItems="start">
          <Box>
            <Typography variant="h6">{opp.name}</Typography>
            <Typography variant="body2" color="text.secondary">
              {opp.description}
            </Typography>
          </Box>
          <Chip
            label={opp.stage}
            color={getStageColor(opp.stage)}
            size="small"
          />
        </Stack>

        <Divider sx={{ my: 1 }} />

        <Stack direction="row" spacing={3}>
          <Box>
            <Typography variant="caption" color="text.secondary">Wert</Typography>
            <Typography variant="body2" fontWeight="bold">
              {formatCurrency(opp.expectedValue)}
            </Typography>
          </Box>
          <Box>
            <Typography variant="caption" color="text.secondary">Close Date</Typography>
            <Typography variant="body2">
              {formatDate(opp.expectedCloseDate)}
            </Typography>
          </Box>
          <Box>
            <Typography variant="caption" color="text.secondary">Probability</Typography>
            <Typography variant="body2">{opp.probability}%</Typography>
          </Box>
        </Stack>
      </CardContent>
    </Card>
  ))}
</Stack>
```

---

### **4. OpportunityPipeline Filter-UI** (6h)

**Datei:** `frontend/src/features/opportunity/components/OpportunityPipeline.tsx`

**Feature 1: Status Filter (2h)**
```tsx
const [statusFilter, setStatusFilter] = useState<'active' | 'closed' | 'all'>('active');

<Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 2 }}>
  <Typography variant="h5">Pipeline Übersicht</Typography>

  <Box sx={{ flexGrow: 1 }} />

  {/* Status Toggle */}
  <ToggleButtonGroup
    value={statusFilter}
    exclusive
    onChange={(e, value) => value && setStatusFilter(value)}
    size="small"
  >
    <ToggleButton value="active">
      🔥 Aktive ({activeCounts})
    </ToggleButton>
    <ToggleButton value="closed">
      📦 Geschlossene ({closedCounts})
    </ToggleButton>
    <ToggleButton value="all">
      📊 Alle ({totalCounts})
    </ToggleButton>
  </ToggleButtonGroup>
</Stack>
```

**Backend-Erweiterung:**
```java
// OpportunityResource.java - Erweitern
@GET
@RolesAllowed({"admin", "manager", "sales"})
public Response getAllOpportunities(
    @QueryParam("page") @DefaultValue("0") int page,
    @QueryParam("size") @DefaultValue("20") int size,
    @QueryParam("status") @DefaultValue("active") String status) {

    logger.debug("Fetching opportunities - page: {}, size: {}, status: {}", page, size, status);

    List<OpportunityResponse> opportunities;

    switch (status.toLowerCase()) {
        case "closed":
            opportunities = opportunityService.findClosedOpportunities(Page.of(page, size));
            break;
        case "all":
            opportunities = opportunityService.findAllOpportunities(Page.of(page, size));
            break;
        case "active":
        default:
            opportunities = opportunityService.findActiveOpportunities(Page.of(page, size));
            break;
    }

    return Response.ok(opportunities).build();
}

// OpportunityService.java - Neue Methoden
public List<OpportunityResponse> findActiveOpportunities(Page page) {
    List<Opportunity> opportunities = opportunityRepository.findAllActive(page);
    return opportunities.stream()
        .map(opportunityMapper::toResponse)
        .collect(Collectors.toList());
}

public List<OpportunityResponse> findClosedOpportunities(Page page) {
    List<Opportunity> opportunities = opportunityRepository
        .find("stage IN (?1, ?2)",
              Sort.by("stageChangedAt").descending(),
              OpportunityStage.CLOSED_WON,
              OpportunityStage.CLOSED_LOST)
        .page(page)
        .list();
    return opportunities.stream()
        .map(opportunityMapper::toResponse)
        .collect(Collectors.toList());
}
```

**Feature 2: "Meine Deals" Filter (1h)**
```tsx
const [myDealsOnly, setMyDealsOnly] = useState(false);

<FormControlLabel
  control={
    <Checkbox
      checked={myDealsOnly}
      onChange={(e) => setMyDealsOnly(e.target.checked)}
    />
  }
  label="Nur meine Deals"
/>

// API Call erweitern:
const params = new URLSearchParams();
params.append('status', statusFilter);
if (myDealsOnly) {
  params.append('assignedTo', currentUser.id);
}

const response = await httpClient.get(`/api/opportunities?${params.toString()}`);
```

**Backend-Erweiterung:**
```java
// OpportunityResource.java - Erweitern
@GET
@RolesAllowed({"admin", "manager", "sales"})
public Response getAllOpportunities(
    @QueryParam("page") @DefaultValue("0") int page,
    @QueryParam("size") @DefaultValue("20") int size,
    @QueryParam("status") @DefaultValue("active") String status,
    @QueryParam("assignedTo") UUID assignedToUserId) {

    // ... status filtering wie oben ...

    // Zusätzlich: User-Filtering
    if (assignedToUserId != null) {
        opportunities = opportunities.stream()
            .filter(opp -> opp.getAssignedTo() != null &&
                          opp.getAssignedTo().getId().equals(assignedToUserId))
            .collect(Collectors.toList());
    }

    return Response.ok(opportunities).build();
}
```

**Feature 3: Quick-Search (2h)**
```tsx
const [searchQuery, setSearchQuery] = useState('');

<TextField
  placeholder="Suche nach Name oder Kunde..."
  value={searchQuery}
  onChange={(e) => setSearchQuery(e.target.value)}
  size="small"
  InputProps={{
    startAdornment: <SearchIcon />
  }}
  sx={{ width: 300 }}
/>

// Client-side filtering (da bereits geladen):
const filteredOpportunities = opportunities.filter(opp =>
  opp.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
  opp.customer?.companyName?.toLowerCase().includes(searchQuery.toLowerCase())
);
```

**Feature 4: Pagination (15 pro Spalte) (1h)**
```tsx
const CARDS_PER_COLUMN = 15;

{opportunities.slice(0, CARDS_PER_COLUMN).map(opp => (
  <OpportunityCard key={opp.id} opportunity={opp} />
))}

{opportunities.length > CARDS_PER_COLUMN && (
  <Button
    variant="text"
    onClick={() => setShowAll(true)}
    fullWidth
  >
    ... {opportunities.length - CARDS_PER_COLUMN} weitere laden
  </Button>
)}
```

---

### **5. Drag & Drop Fix** (2h)

**Problem:** Card "springt" beim Greifen (transformOrigin: '0 0' Bug)

**Datei:** `frontend/src/features/opportunity/components/OpportunityPipeline.tsx`

**Fix 1: PointerSensor hinzufügen (30 Min)**
```tsx
import {
  DndContext,
  PointerSensor,  // ← NEU!
  useSensor,
  useSensors,
} from '@dnd-kit/core';

const sensors = useSensors(
  useSensor(PointerSensor, {
    activationConstraint: {
      distance: 8,  // 8px Bewegung nötig zum Start (verhindert Klick-Konflikte)
    },
  })
);

<DndContext sensors={sensors} onDragEnd={handleDragEnd}>
  {/* ... */}
</DndContext>
```

**Fix 2: transformOrigin entfernen (30 Min)**
```tsx
// VORHER (Zeile 287-312 - FEHLERHAFT):
<DragOverlay
  adjustScale={false}
  wrapperElement="div"
  style={{
    transformOrigin: '0 0',  // ← ENTFERNEN!
  }}
  dropAnimation={{
    duration: 250,
    easing: 'cubic-bezier(0.18, 0.67, 0.6, 1.22)',
  }}
>
  {activeOpportunity && (
    <Box sx={{
      transform: 'rotate(5deg)',  // ← ÄNDERN!
      opacity: 0.9,
      boxShadow: 4,
    }}>
      <OpportunityCard opportunity={activeOpportunity} />
    </Box>
  )}
</DragOverlay>

// NACHHER (KORREKT):
<DragOverlay
  adjustScale={false}
  wrapperElement="div"
  dropAnimation={{
    duration: 250,
    easing: 'cubic-bezier(0.18, 0.67, 0.6, 1.22)',
  }}
>
  {activeOpportunity && (
    <Box sx={{
      transform: 'scale(1.03)',  // ✅ SCALE statt ROTATE!
      opacity: 0.95,  // Etwas höher für bessere Sichtbarkeit
      boxShadow: '0 8px 32px rgba(0,0,0,0.2)',  // Schöner Schatten
      cursor: 'grabbing',
    }}>
      <OpportunityCard
        opportunity={activeOpportunity}
        isDragging={true}  // Prop für spezielle Styles
      />
    </Box>
  )}
</DragOverlay>
```

**Fix 3: SortableOpportunityCard Update (30 Min)**
```tsx
// frontend/src/features/opportunity/components/SortableOpportunityCard.tsx

export function SortableOpportunityCard({ opportunity }: Props) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: opportunity.id,
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.3 : 1,  // ← GEÄNDERT (vorher 0.5)
    pointerEvents: isDragging ? 'none' : 'auto',  // ← NEU!
  };

  return (
    <Box ref={setNodeRef} style={style} {...attributes} {...listeners}>
      <OpportunityCard opportunity={opportunity} />
    </Box>
  );
}
```

**Testing (30 Min):**
- Test auf verschiedenen Auflösungen (1920×1080, 2560×1440, 4K, Laptop 1366×768)
- Test auf Touch-Devices (iPad Simulator)
- Verify: Card bleibt unter Mauszeiger beim Greifen ✅

---

### **6. Testing & Bugfixes** (2h)

**Unit Tests (1h):**
```typescript
// CreateOpportunityDialog.test.tsx
describe('CreateOpportunityDialog', () => {
  it('validates expected value > 0', () => {
    const { getByLabelText, getByText } = render(<CreateOpportunityDialog />);

    const valueInput = getByLabelText('Expected Value');
    fireEvent.change(valueInput, { target: { value: '-100' } });

    expect(getByText('Wert muss größer als 0 sein')).toBeInTheDocument();
  });

  it('validates close date in future', () => {
    const { getByLabelText, getByText } = render(<CreateOpportunityDialog />);

    const dateInput = getByLabelText('Expected Close Date');
    fireEvent.change(dateInput, { target: { value: '2020-01-01' } });

    expect(getByText('Datum muss in der Zukunft liegen')).toBeInTheDocument();
  });

  it('auto-generates name from lead data', () => {
    const lead = { companyName: 'Test GmbH', estimatedVolume: 50000 };
    const { getByLabelText } = render(
      <CreateOpportunityDialog lead={lead} dealType="Liefervertrag" />
    );

    const nameInput = getByLabelText('Name');
    expect(nameInput.value).toBe('Vertragschance - Test GmbH (Liefervertrag)');
  });
});
```

**Manual E2E Testing (1h):**
```
Test Case 1: Lead → Opportunity Flow
1. Open LeadDetailPage (Lead #90001 - qualifiziert)
2. Click "In Opportunity konvertieren"
3. Dialog opens
4. Verify pre-filled values:
   - Name: "Vertragschance - Müller Catering (Liefervertrag)"
   - Expected Value: 2000
   - Close Date: 30 days from now
5. Submit
6. Verify Toast: "Opportunity erstellt!"
7. Verify Redirect to OpportunityPipeline
8. Verify NEW_LEAD column contains new card
9. Verify Lead Status = CONVERTED

Test Case 2: Filter Functionality
1. Open OpportunityPipeline
2. Default: "Aktive" selected (nur 5 Stages sichtbar: NEW_LEAD bis NEGOTIATION)
3. Click "Geschlossene"
4. Verify: Nur CLOSED_WON + CLOSED_LOST sichtbar
5. Click "Alle"
6. Verify: Alle 7 Stages sichtbar
7. Enable "Nur meine Deals"
8. Verify: Nur Opportunities mit assignedTo = currentUser

Test Case 3: Drag & Drop
1. Grab card from NEW_LEAD
2. Verify: Card bleibt unter Mauszeiger (kein Sprung!)
3. Move to QUALIFICATION
4. Verify: Smooth animation
5. Drop
6. Verify: Card in neuer Spalte
7. Verify: Backend Stage updated (API Call)
```

---

## ✅ DEFINITION OF DONE

### **Functional:**
- [x] User kann via UI Lead → Opportunity erstellen (< 3 Klicks)
- [x] Kanban zeigt nur aktive Opportunities (Filter "Aktive" default)
- [x] "Meine Deals" Filter funktioniert
- [x] Quick-Search funktioniert (Name + Customer)
- [x] Pagination: Max 15 Cards pro Spalte, dann "... X weitere laden"
- [x] Lead Detail zeigt Opportunity-Status + Link
- [x] Drag & Drop: Card bleibt unter Mauszeiger (kein Sprung!)

### **Technical:**
- [x] Backend Endpoint `/api/opportunities?status=active` implementiert
- [x] Backend Endpoint `/api/opportunities?assignedTo={userId}` implementiert
- [x] Backend Endpoint `/api/leads/{id}/opportunities` implementiert
- [x] OpportunityService.findActiveOpportunities() implementiert
- [x] OpportunityService.findClosedOpportunities() implementiert
- [x] OpportunityService.findByLeadId() implementiert
- [x] Frontend State Management: Filter + Search funktioniert
- [x] Unit Tests: CreateOpportunityDialog Validation
- [x] E2E Tests: Lead → Opportunity Happy Path
- [x] Keine Console Errors
- [x] TypeScript Compilation: 0 Errors
- [x] Spotless Apply + Prettier Format (vor Commit!)

### **Quality:**
- [x] Code Review: 1 Approval (selbst wenn solo - dokumentiert in PR!)
- [x] UI/UX: Intuitiv, keine unnötigen Klicks
- [x] Performance: Kanban lädt < 2s (bei 50 Opportunities)
- [x] Drag & Drop: Funktioniert auf allen Auflösungen (1366×768 bis 4K)

### **Documentation:**
- [x] TRIGGER_SPRINT_2_1_7_1.md aktualisiert (✅ Done)
- [x] TRIGGER_INDEX.md: Sprint-Status auf "IN_PROGRESS" → "COMPLETE"
- [x] CRM_COMPLETE_MASTER_PLAN_V5.md: Session Log aktualisiert

---

## 📊 SUCCESS METRICS

### **Usability:**
- Lead → Opportunity: Max 3 Klicks, < 30 Sekunden
- Kanban Übersichtlichkeit: Max 15 Opportunities pro Spalte sichtbar (Pagination!)

### **Performance:**
- Pipeline-Load: < 2s (bei 50 Opportunities)
- Filter-Switch: < 500ms
- Drag & Drop: 60 FPS (smooth animation)

---

## 🚀 PREREQUISITES

### **Ready to Start:**
- ✅ Backend komplett (Sprint 2.1.7.0)
- ✅ V10026 Migration deployed
- ✅ V90003 DEV-SEED deployed
- ✅ 10 Test-Opportunities im Kanban sichtbar

### **Blockers:**
- ❌ Keine bekannt

---

## 📅 TIMELINE (Realistisch!)

**Tag 1 (8h):**
- CreateOpportunityDialog (3h)
- LeadDetailPage Integration (2h)
- LeadOpportunitiesList (2h)
- Break + Bugfixes (1h)

**Tag 2 (9h):**
- OpportunityPipeline Filter-UI (6h)
  - Status Filter (2h)
  - "Meine Deals" Filter (1h)
  - Quick-Search (2h)
  - Pagination (1h)
- Drag & Drop Fix (2h)
- Testing & Bugfixes (1h)

**Total: 17h = 2 Arbeitstage** ✅

---

## 🔗 RELATED WORK

### **Dependent Sprints:**
- Sprint 2.1.7.0: Backend Integration (COMPLETE ✅)
- Sprint 2.1.6: Lead Management (COMPLETE ✅)

### **Follow-up Sprints:**
- **Sprint 2.1.7.2:** Customer-Management + Xentral-Integration
  - Opportunity → Customer Conversion
  - Xentral-Kunden-Dropdown (verkäufer-gefiltert)
  - Customer-Dashboard (Umsatz + Zahlungsverhalten)
  - Churn-Alarm (variabel pro Kunde)

- **Sprint 2.1.7.3:** RENEWAL-Workflow (Upsell/Cross-sell)
  - "Neue Opportunity für Customer" Button
  - RENEWAL-Stage Logik
  - Customer-Opportunity-Historie

- **Sprint 2.1.7.4:** Advanced Filters & Analytics
  - High-Value Filter (erst wenn echte Daten!)
  - Urgent Filter (Close Date < 14 Tage)
  - Pipeline-Analytics Dashboard

---

## 📝 NOTES

### **Design Decisions (aus Diskussion mit Jörg):**

1. **Fokussierter Scope:**
   - NUR Lead → Opportunity in diesem Sprint
   - Customer-Conversion verschoben zu 2.1.7.2 (zusammen mit Xentral-Integration!)
   - Grund: Customer-Dashboard ohne Xentral-Daten ist nutzlos

2. **Filter-Standard:**
   - Default: "Aktive" (nur NEW_LEAD → NEGOTIATION)
   - CLOSED_WON/LOST ausgeblendet (übersichtlicher)
   - User kann umschalten: Aktive / Geschlossene / Alle

3. **Lead Status = CONVERTED:**
   - ONE-WAY conversion (Industry Standard)
   - Lead bleibt sichtbar (für Traceability)
   - Opportunity.lead_id verlinkt zurück

4. **Drag & Drop Fix:**
   - transformOrigin: '0 0' entfernt (Ursache für Position-Jump!)
   - scale(1.03) statt rotate(5deg) (sauberer, funktioniert überall)
   - PointerSensor mit 8px activationConstraint (verhindert Klick-Konflikte)

### **Technical Debt:**
- Pagination: Aktuell nur "X weitere laden" Button - später: Infinite Scroll oder echte Pagination
- Backend-Filtering: Aktuell kombiniert (Status + AssignedTo) - später: Dedicated Query-Object
- Drag & Drop: Aktuell keine Optimistic Updates - später: Optimistic UI + Rollback bei Fehler

---

**Sprint bereit für Kickoff!** 🚀

**Nächster Schritt:**
1. Feature-Branch erstellen: `git checkout -b feature/sprint-2-1-7-1-lead-opportunity`
2. Migration-Nummer checken: `./scripts/get-next-migration.sh` (falls Backend-Änderungen nötig)
3. Los geht's! 💪
