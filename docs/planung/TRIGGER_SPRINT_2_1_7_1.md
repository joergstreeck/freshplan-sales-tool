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

**SCHRITT 1: Prüfen ob RENEWAL-Daten existieren (VOR Backend-Cleanup!)**
```bash
# DB-Check ausführen:
PGPASSWORD=freshplan123 psql -h localhost -U freshplan_user -d freshplan_db -c "
SELECT
  stage,
  COUNT(*) as count,
  ARRAY_AGG(name ORDER BY created_at DESC) FILTER (WHERE name IS NOT NULL) as example_names
FROM opportunities
GROUP BY stage
ORDER BY
  CASE stage
    WHEN 'NEW_LEAD' THEN 1
    WHEN 'QUALIFICATION' THEN 2
    WHEN 'NEEDS_ANALYSIS' THEN 3
    WHEN 'PROPOSAL' THEN 4
    WHEN 'NEGOTIATION' THEN 5
    WHEN 'CLOSED_WON' THEN 6
    WHEN 'CLOSED_LOST' THEN 7
    WHEN 'RENEWAL' THEN 8
    ELSE 99
  END;
"
```

**SCHRITT 2: Aktion basierend auf Ergebnis:**

**Szenario A: 0 RENEWAL-Daten** ✅ (wahrscheinlich)
- ❌ Keine Migration nötig
- ✅ Nur OpportunityStage.java bereinigen (RENEWAL löschen)
- ⏱️ Aufwand: 15 Minuten

**Szenario B: 1-5 RENEWAL-Daten** ⚠️ (möglich in DEV)
- ✅ Migration V10030 erstellen (RENEWAL → NEEDS_ANALYSIS + opportunityType='RENEWAL')
- ✅ OpportunityStage.java bereinigen
- ⏱️ Aufwand: 30 Minuten

**Migration V10030 (falls RENEWAL-Daten existieren):**
```sql
-- V10030__remove_renewal_stage.sql
UPDATE opportunities
SET
    stage = 'NEEDS_ANALYSIS',
    opportunity_type = COALESCE(opportunity_type, 'RENEWAL')
WHERE stage = 'RENEWAL';

-- Validierung:
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM opportunities WHERE stage = 'RENEWAL') THEN
        RAISE EXCEPTION 'Migration failed: RENEWAL stage still exists';
    END IF;
END $$;
```

**Szenario C: Viele RENEWAL-Daten** 🚨 (unwahrscheinlich)
- Entscheidung mit Jörg: RENEWAL beibehalten bis Sprint 2.1.7.3?

**Hinweis:** RENEWAL-Workflow kommt in Sprint 2.1.7.3 als `opportunityType` Feld zurück!

---

## 🔍 CODE REVIEW & EXISTING COMPONENTS

### **Existierende OpportunityCard.tsx - Analyse**

**Datei:** `frontend/src/features/opportunity/components/OpportunityCard.tsx`

**Status:** ✅ Sehr gute Basis - Production Ready mit kleinen Verbesserungen nötig

**Was ist implementiert:**
- ✅ **Performance-optimiert**: React.memo, useMemo, useCallback
- ✅ **Drag & Drop**: Dedizierter Drag Handle (⋮⋮ Icon oben rechts)
- ✅ **Error Handling**: Try-Catch + Structured Logging (componentLogger)
- ✅ **Accessibility**: Tooltips, Semantic HTML
- ✅ **Freshfoodz CI**: Korrekte Farben (#94C456, #004F7B, Antonio Font)
- ✅ **Anzeige**: Name, Customer, Value, Probability (Progress Bar), Date, Assigned User Avatar

**⚠️ Was fehlt für Sprint 2.1.7.1:**

1. **Customer-Name Fallback** (Zeile 203-216):
   ```tsx
   // PROBLEM: Bei Lead→Opportunity gibt es noch KEINEN Customer
   // Card zeigt "Unbekannt" statt Lead-Name

   // ✅ LÖSUNG:
   {opportunity.customerName || opportunity.leadCompanyName || 'Potenzieller Kunde'}
   ```

2. **Lead-Origin Indicator** (für Traceability):
   ```tsx
   // Zeige Lead-Quelle wenn vorhanden
   {opportunity.leadId && (
     <Chip
       label={`von Lead #${opportunity.leadId}`}
       size="small"
       variant="outlined"
       sx={{ mt: 0.5 }}
     />
   )}
   ```

3. **Stage Color Border** (dynamisch):
   ```tsx
   // Aktuell: Grüner Border für alle
   border: '1px solid rgba(148, 196, 86, 0.2)',

   // ✅ LÖSUNG: Border-Farbe aus OpportunityStage.getColor()
   border: `2px solid ${opportunity.stageColor || 'rgba(148, 196, 86, 0.2)'}`,
   ```

4. **Deal Type / Opportunity Type Badge**:
   ```tsx
   // Zeige Opportunity-Typ wenn vorhanden
   {opportunity.opportunityType && (
     <Chip
       label={opportunity.opportunityType}
       size="small"
       color={opportunity.opportunityType === 'RENEWAL' ? 'warning' : 'default'}
       sx={{ mt: 0.5 }}
     />
   )}
   ```

**Aufwand für Verbesserungen:** ~30 Min (Quick Win!)

---

## 🧪 TEST-STRATEGIE (Solide Abdeckung)

### **Level 1: Unit Tests (Jest + React Testing Library)** ~2-3h

**Datei:** `frontend/src/features/opportunity/components/OpportunityCard.test.tsx`

```typescript
describe('OpportunityCard', () => {
  it('renders all fields correctly', () => {
    // ✅ Name, Value, Probability, Date, Assigned User
  });

  it('formats currency with German locale', () => {
    // ✅ 20000 → "20.000 €"
  });

  it('formats date with German locale', () => {
    // ✅ "2025-11-13" → "13.11.25"
  });

  it('shows probability color correctly', () => {
    // ✅ 80% → Grün, 60% → Freshfoodz-Grün, 40% → Orange, 20% → Rot
  });

  it('calls onClick when card clicked (not dragging)', () => {
    // ✅ Card-Klick triggert onClick Handler
  });

  it('does NOT call onClick while dragging', () => {
    // ✅ Während Drag kein onClick
  });

  it('shows drag handle on hover', () => {
    // ✅ ⋮⋮ Icon wird grün bei Hover
  });

  it('fallback to lead name if no customer', () => {
    // ✅ "Müller Catering (Lead)" statt "Unbekannt"
  });
});
```

**Coverage:** ~85% der Card-Logik

---

### **Level 2: Integration Tests (React Testing Library + MSW)** ~3-4h

**Datei:** `frontend/src/features/opportunity/components/OpportunityPipeline.integration.test.tsx`

```typescript
describe('OpportunityPipeline with Cards', () => {
  it('loads opportunities from API and renders cards', async () => {
    // ✅ Mock: GET /api/opportunities → 7 Cards erscheinen
  });

  it('filters cards by status (Active/Closed/All)', async () => {
    // ✅ Klick "Aktive" → nur 5 Spalten sichtbar
  });

  it('searches cards by name', async () => {
    // ✅ Eingabe "Müller" → nur Müller-Cards sichtbar
  });

  it('drags card from NEW_LEAD to QUALIFICATION', async () => {
    // ✅ Drag → PUT /api/opportunities/{id}/stage/QUALIFICATION
    // ✅ Card erscheint in neuer Spalte
  });

  it('shows error toast on API failure', async () => {
    // ✅ API Error → Toast "Fehler beim Laden"
  });
});
```

**Coverage:** ~70% der Pipeline-Interaktionen

---

### **Level 3: E2E Tests (Playwright)** ~2-3h (1 Happy Path)

**Datei:** `e2e/lead-to-opportunity.spec.ts`

```typescript
test('Lead → Opportunity Happy Path', async ({ page }) => {
  // 1. Login als Vertriebler
  await page.goto('/login');
  await page.fill('input[name=username]', 'testuser');
  await page.fill('input[name=password]', 'test123');
  await page.click('button[type=submit]');

  // 2. Öffne Lead Detail
  await page.goto('/lead-generation/leads/mueller-catering-90001');
  await page.waitForSelector('h4:has-text("Müller Catering")');

  // 3. Klick "In Opportunity konvertieren"
  await page.click('button:has-text("In Opportunity konvertieren")');

  // 4. Dialog öffnet sich
  await page.waitForSelector('dialog:has-text("Opportunity erstellen")');

  // 5. Felder sind vorausgefüllt
  const nameInput = page.locator('input[name=name]');
  await expect(nameInput).toHaveValue(/Vertragschance.*Müller Catering/);

  // 6. Wähle Deal Type
  await page.click('select[name=dealType]');
  await page.click('option:has-text("Liefervertrag")');

  // 7. Submit
  await page.click('button:has-text("Erstellen")');

  // 8. Toast erscheint
  await page.waitForSelector('div:has-text("Opportunity erstellt!")');

  // 9. Redirect zu Pipeline
  await expect(page).toHaveURL(/\/opportunities/);

  // 10. Card ist in NEW_LEAD Spalte sichtbar
  await page.waitForSelector('.pipeline-stage-NEW_LEAD');
  const card = page.locator('.opportunity-card:has-text("Müller Catering")');
  await expect(card).toBeVisible();

  // 11. Card hat korrekten Wert
  await expect(card).toContainText('50.000 €');
});
```

**Coverage:** 100% des User-Flows

---

### **Minimale Testabdeckung für "solide" Sprint-Abnahme:**

| Test-Level | Tool | Aufwand | Coverage | Wann ausführen |
|------------|------|---------|----------|----------------|
| **Unit Tests** | Jest + RTL | 2-3h | 85% | Pre-Commit Hook |
| **Integration Tests** | Jest + MSW | 3-4h | 70% | CI Pipeline (Push) |
| **E2E Tests (1 Happy Path)** | Playwright | 2-3h | Lead→Opp Flow | Vor Merge in main |

**Total: ~7-10h** für robuste Testabdeckung

**Schutz gegen:**
- 🛡️ Regressions (Card-Format, Drag & Drop, Filter)
- 🐛 Bugs (Validation, API-Errors, State-Management)
- 📊 ~75% Code Coverage
- 🎯 Critical Path gesichert (Lead→Opportunity)

---

## 📦 DELIVERABLES

### **0. Quick Wins - OpportunityCard Verbesserungen** (30 Min)

**Datei:** `frontend/src/features/opportunity/components/OpportunityCard.tsx`

**Änderung 1: Customer-Name Fallback** (Zeile 203)
```tsx
// VORHER:
{opportunity.customerName && (
  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
    <PersonIcon fontSize="small" sx={{ color: theme.palette.grey[600], mr: 0.5 }} />
    <Typography variant="body2">{opportunity.customerName}</Typography>
  </Box>
)}

// NACHHER:
{(opportunity.customerName || opportunity.leadCompanyName) && (
  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
    <PersonIcon fontSize="small" sx={{ color: theme.palette.grey[600], mr: 0.5 }} />
    <Typography variant="body2" sx={{ color: theme.palette.text.secondary, fontSize: '0.875rem' }}>
      {opportunity.customerName || opportunity.leadCompanyName || 'Potenzieller Kunde'}
    </Typography>
  </Box>
)}
```

**Änderung 2: Lead-Origin Indicator** (nach Customer-Name, Zeile ~217)
```tsx
{/* Lead-Origin Badge für Traceability */}
{opportunity.leadId && (
  <Chip
    label={`von Lead #${opportunity.leadId}`}
    size="small"
    variant="outlined"
    sx={{
      height: 20,
      fontSize: '0.7rem',
      mb: 1,
      borderColor: theme.palette.grey[300],
      color: theme.palette.text.secondary,
    }}
  />
)}
```

**Änderung 3: Stage Color Border** (Zeile 142)
```tsx
// VORHER:
border: '1px solid rgba(148, 196, 86, 0.2)',

// NACHHER (dynamisch aus Stage-Farbe):
border: `2px solid ${opportunity.stageColor ? `${opportunity.stageColor}40` : 'rgba(148, 196, 86, 0.2)'}`,
// Hinweis: `${color}40` = Hex-Farbe + 40 = 25% Opacity
```

**Typ-Erweiterung nötig:**
```tsx
// frontend/src/features/opportunity/types.ts
export interface Opportunity {
  id: string;
  name: string;
  stage: OpportunityStage;
  stageColor?: string;        // ← NEU! Farbe aus Backend
  customerName?: string;
  leadCompanyName?: string;   // ← NEU! Fallback bei Lead→Opportunity
  leadId?: number;            // ← NEU! Für Traceability
  value?: number;
  probability?: number;
  expectedCloseDate?: string;
  assignedToName?: string;
  // ...
}
```

**Backend-Änderung (OpportunityResponse DTO):**
```java
// backend/.../dto/OpportunityResponse.java
public class OpportunityResponse {
    private UUID id;
    private String name;
    private OpportunityStage stage;
    private String stageColor;        // ← NEU! stage.getColor()
    private String leadCompanyName;   // ← NEU! lead != null ? lead.companyName : null
    private Long leadId;              // ← NEU! lead != null ? lead.id : null
    // ...
}

// OpportunityMapper.java
public OpportunityResponse toResponse(Opportunity opportunity) {
    OpportunityResponse response = new OpportunityResponse();
    // ...
    response.setStageColor(opportunity.getStage().getColor());
    if (opportunity.getLead() != null) {
        response.setLeadCompanyName(opportunity.getLead().getCompanyName());
        response.setLeadId(opportunity.getLead().getId());
    }
    return response;
}
```

**Aufwand:** 30 Min (Frontend) + 15 Min (Backend DTO) = **45 Min Total**

---

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

## 📅 TIMELINE (Realistisch + Quick Wins!)

**Tag 0 - Vorbereitung (1h):**
- DB-Check: RENEWAL-Daten prüfen (5 Min)
- Backend-Cleanup: RENEWAL aus OpportunityStage.java entfernen (15 Min)
- OpportunityCard Quick Wins (Frontend 30 Min + Backend DTO 15 Min = 45 Min)

**Tag 1 (8h):**
- Drag & Drop Fix (2h) ← Quick Win zuerst!
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
- Testing & Bugfixes (2h)
- Unit Tests schreiben (1h)

**Tag 3 (optional - Tests) (3h):**
- Integration Tests (2h)
- E2E Happy Path Test (1h)

**Core Development: 18h = 2 Arbeitstage** ✅
**Mit Tests: 21h = 2.5 Arbeitstage** ✅

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

## 🎯 SPRINT ZUSAMMENFASSUNG & KEY INSIGHTS

### **Was wir bereits haben (Analyse 2025-10-16):**

✅ **OpportunityCard.tsx** ist production-ready:
- Performance-optimiert (React.memo, useMemo, useCallback)
- Drag & Drop mit dediziertem Handle
- Error Handling + Structured Logging
- Freshfoodz CI konform

✅ **Backend API** ist komplett (Sprint 2.1.7.0):
- POST /api/opportunities/from-lead/{leadId}
- OpportunityService.createFromLead()
- V10026 Migration (opportunities.lead_id FK)
- 10 DEV-SEED Opportunities

### **Was wir bauen müssen:**

🔨 **Quick Wins (1h):**
- Customer-Name Fallback (zeigt Lead-Name statt "Unbekannt")
- Lead-Origin Badge (Traceability: "von Lead #90001")
- Stage Color Border (dynamisch aus OpportunityStage.getColor())
- Backend DTO: leadCompanyName, leadId, stageColor

🔨 **Core Features (17h):**
1. CreateOpportunityDialog (3h) - MUI Dialog mit Validation
2. LeadDetailPage Integration (2h) - Button + Opportunities-Sektion
3. LeadOpportunitiesList (2h) - Card-Liste für Lead-Detail
4. OpportunityPipeline Filter-UI (6h) - Status/Meine Deals/Search/Pagination
5. Drag & Drop Fix (2h) - transformOrigin Bug beheben
6. Testing & Bugfixes (2h)

🧪 **Tests (optional 3-10h):**
- Unit Tests (2-3h) - OpportunityCard, CreateOpportunityDialog
- Integration Tests (3-4h) - Pipeline mit Filtern
- E2E Tests (2-3h) - Lead → Opportunity Happy Path

### **Kritische Entscheidungen:**

⚠️ **RENEWAL-Stage entfernen:**
- Aktuell: 8 Stages (mit RENEWAL)
- Ziel: 7 Stages (ohne RENEWAL)
- Migration V10030 nur nötig falls RENEWAL-Daten existieren
- DB-Check VORHER durchführen!

✅ **Filter-Standard:**
- Default: "Aktive" (nur NEW_LEAD → NEGOTIATION)
- CLOSED_WON/LOST ausgeblendet (übersichtlicher)

✅ **Lead Status = CONVERTED:**
- ONE-WAY conversion (Industry Standard)
- Lead bleibt sichtbar (Traceability)
- opportunity.lead_id verlinkt zurück

### **Risiken & Mitigations:**

| Risiko | Wahrscheinlichkeit | Impact | Mitigation |
|--------|-------------------|--------|-----------|
| RENEWAL-Daten existieren | Mittel | Mittel | DB-Check vorher, Migration V10030 vorbereitet |
| Drag & Drop Bug bleibt | Niedrig | Hoch | Fix zuerst (Quick Win), Test auf allen Geräten |
| Backend DTO Breaking Change | Niedrig | Mittel | Neue Felder optional, Fallbacks im Frontend |
| Tests schlagen fehl | Mittel | Niedrig | CI-Integration erst nach manuellem Test |

### **Success Metrics:**

- ✅ Lead → Opportunity: Max 3 Klicks, < 30 Sekunden
- ✅ Pipeline lädt < 2s (bei 50 Opportunities)
- ✅ Filter-Switch < 500ms
- ✅ Drag & Drop: 60 FPS, Card bleibt unter Mauszeiger
- ✅ ~75% Test Coverage (mit Unit + Integration + E2E)

---

**Sprint bereit für Kickoff!** 🚀

**Nächster Schritt:**
1. ✅ Feature-Branch erstellt: `feature/sprint-2-1-7-1-lead-opportunity`
2. Migration-Nummer gecheckt: **V10030** (nächste freie)
3. DB-Check: RENEWAL-Daten prüfen (Szenario A/B/C)
4. Quick Win: OpportunityCard Verbesserungen (30 Min)
5. Los geht's! 💪

**Dokumentation vollständig ergänzt:** 2025-10-16 18:45 Uhr
