# 🚀 Sprint 2.1.7.1 - Opportunities UI Integration

**Sprint-ID:** 2.1.7.1
**Status:** 🔶 PLANNING
**Priority:** P1 (High)
**Estimated Effort:** 16-24h (2-3 Tage)
**Owner:** TBD
**Created:** 2025-10-13

---

## 🎯 SPRINT GOALS

### **Business Value**

Vollständige UI-Integration für Lead-Opportunity-Customer Lifecycle ermöglichen:
- Vertriebler können Leads in Opportunities konvertieren (per Klick)
- Vertriebler können Opportunities für Kunden erstellen (Upsell/Cross-sell)
- Kanban Board bleibt übersichtlich (Filter + Archiv)
- Lead-Detail zeigt Opportunity-Status (Traceability)

### **Technical Context**

**Backend ist fertig (Sprint 2.1.7.0):**
- ✅ V10026: `opportunities.lead_id` FK
- ✅ OpportunityService: 3 Service-Methoden
- ✅ REST APIs: 3 Endpoints
- ✅ V90003: 10 DEV-SEED Opportunities

**Was fehlt:**
- ❌ UI zum Anlegen von Opportunities
- ❌ "Lead → Opportunity" Button
- ❌ Kanban Filter (Nur offene / Archiv)
- ❌ Lead/Customer zeigt Opportunity-Status

---

## 📦 DELIVERABLES

### **Phase 1: Lead → Opportunity UI** (8h)

#### **1.1 CreateOpportunityDialog Component** (3h)

**Neue Datei:** `frontend/src/features/opportunity/components/CreateOpportunityDialog.tsx`

**Anforderungen:**
- MUI Dialog mit Form
- Felder:
  - Name (Text, optional - auto-generated from Lead)
  - Deal Type (Select: "Liefervertrag", "Testphase", "Pilot", "Vollversorgung")
  - Timeframe (Text: "Q2 2025", "H1 2025", etc.)
  - Expected Value (Number, EUR)
  - Expected Close Date (DatePicker)
- Pre-fill mit Lead-Daten:
  - Name: `{lead.companyName} - {dealType} {timeframe}`
  - Expected Value: aus Lead.estimatedVolume
- Validation:
  - Expected Value > 0
  - Close Date > Today
- Error Handling:
  - API Errors anzeigen
  - Validation Errors inline
- Success:
  - Toast "Opportunity erstellt!"
  - Redirect zu Opportunity-Detail oder Kanban

**API Call:**
```typescript
POST /api/opportunities/from-lead/{leadId}
Body: CreateOpportunityFromLeadRequest
```

#### **1.2 Lead Detail Page Integration** (2h)

**Datei:** `frontend/src/features/leads/pages/LeadDetailPage.tsx`

**Änderungen:**

Button hinzufügen:
```tsx
{lead.status === 'QUALIFIED' || lead.status === 'ACTIVE' ? (
  <Button
    variant="contained"
    color="primary"
    startIcon={<TrendingUpIcon />}
    onClick={() => setShowOpportunityDialog(true)}
  >
    In Opportunity konvertieren
  </Button>
) : null}
```

Opportunities-Sektion hinzufügen:
```tsx
<Section title="Verkaufschancen">
  {lead.status === 'CONVERTED' ? (
    <Alert severity="success">
      Lead wurde zu Opportunity konvertiert am {formatDate(lead.convertedAt)}
    </Alert>
  ) : null}

  <LeadOpportunitiesList leadId={lead.id} />
</Section>
```

**API Call:**
```typescript
GET /api/leads/{id}/opportunities
```

#### **1.3 LeadOpportunitiesList Component** (2h)

**Neue Datei:** `frontend/src/features/leads/components/LeadOpportunitiesList.tsx`

**Anforderungen:**
- Liste aller Opportunities für diesen Lead
- Zeigt: Name, Stage, Expected Value, Close Date
- Link zu Opportunity-Detail
- Empty State: "Noch keine Opportunities"

#### **1.4 Opportunity Detail Enhancement** (1h)

**Datei:** `frontend/src/features/opportunity/pages/OpportunityDetailPage.tsx`

**Änderungen:**

Lead-Badge anzeigen:
```tsx
{opportunity.lead ? (
  <Alert severity="info" icon={<InfoIcon />}>
    <Typography variant="body2">
      Entstanden aus Lead:{' '}
      <Link to={`/leads/${opportunity.lead.id}`}>
        {opportunity.lead.companyName}
      </Link>
      {' '}(Score: {opportunity.lead.leadScore})
    </Typography>
  </Alert>
) : null}
```

---

### **Phase 2: Kanban Board Enhancements** (6h)

#### **2.1 Filter Implementation** (4h)

**Datei:** `frontend/src/features/opportunity/pages/OpportunityPipeline.tsx`

**Änderungen:**

Filter-UI hinzufügen:
```tsx
const [filterStatus, setFilterStatus] = useState<'active' | 'all' | 'closed'>('active');

<ToggleButtonGroup value={filterStatus} exclusive onChange={handleFilterChange}>
  <ToggleButton value="active">Nur offene</ToggleButton>
  <ToggleButton value="all">Alle</ToggleButton>
  <ToggleButton value="closed">Archiv</ToggleButton>
</ToggleButtonGroup>
```

Backend Filter:
```typescript
const fetchOpportunities = async () => {
  const params = new URLSearchParams();

  if (filterStatus === 'active') {
    params.append('status', 'active');
  } else if (filterStatus === 'closed') {
    params.append('status', 'closed');
  }

  const response = await httpClient.get<Opportunity[]>(
    `/api/opportunities?${params.toString()}`
  );

  return response.data;
};
```

**Backend Endpoint erweitern:**
```java
// OpportunityResource.java
@GET
public Response list(@QueryParam("status") String status) {
    List<Opportunity> opportunities;

    if ("active".equals(status)) {
        opportunities = Opportunity.list(
            "stage NOT IN ('CLOSED_WON', 'CLOSED_LOST') AND is_deleted = false"
        );
    } else if ("closed".equals(status)) {
        opportunities = Opportunity.list(
            "stage IN ('CLOSED_WON', 'CLOSED_LOST') AND is_deleted = false"
        );
    } else {
        opportunities = Opportunity.list("is_deleted = false");
    }

    return Response.ok(opportunities).build();
}
```

#### **2.2 Create Opportunity Button** (2h)

**Datei:** `frontend/src/features/opportunity/pages/OpportunityPipeline.tsx`

Button hinzufügen:
```tsx
<Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
  <Typography variant="h4">Pipeline Übersicht</Typography>

  <Button
    variant="contained"
    color="primary"
    startIcon={<AddIcon />}
    onClick={() => setShowCreateDialog(true)}
  >
    Neue Opportunity
  </Button>
</Box>

<CreateOpportunityDialog
  open={showCreateDialog}
  onClose={() => setShowCreateDialog(false)}
  mode="standalone"  // Nicht an Lead gebunden
/>
```

---

### **Phase 3: Customer → Opportunity UI** (4h)

#### **3.1 Customer Detail Page Integration** (2h)

**Datei:** `frontend/src/features/customers/pages/CustomerDetailPage.tsx`

**Änderungen:**

Button hinzufügen:
```tsx
<Button
  variant="outlined"
  color="primary"
  startIcon={<TrendingUpIcon />}
  onClick={() => setShowOpportunityDialog(true)}
>
  Neue Opportunity erstellen
</Button>
```

Opportunities-Sektion:
```tsx
<Section title="Verkaufschancen">
  <CustomerOpportunitiesList customerId={customer.id} />
</Section>
```

#### **3.2 CustomerOpportunitiesList Component** (1h)

**Neue Datei:** `frontend/src/features/customers/components/CustomerOpportunitiesList.tsx`

**Anforderungen:**
- Liste aller Opportunities für diesen Customer
- Gruppiert nach Status: Offen / Gewonnen / Verloren
- Link zu Opportunity-Detail

**API Call:**
```typescript
GET /api/customers/{id}/opportunities
```

#### **3.3 Opportunity → Customer Conversion** (1h)

**Datei:** `frontend/src/features/opportunity/pages/OpportunityDetailPage.tsx`

Button hinzufügen:
```tsx
{opportunity.stage === 'CLOSED_WON' && !opportunity.customer ? (
  <Button
    variant="contained"
    color="success"
    startIcon={<CheckCircleIcon />}
    onClick={handleConvertToCustomer}
  >
    Als Kunde anlegen
  </Button>
) : null}
```

Handler:
```typescript
const handleConvertToCustomer = async () => {
  try {
    const customer = await httpClient.post(
      `/api/opportunities/${opportunity.id}/convert-to-customer`,
      {
        companyName: opportunity.name,
        createContactFromLead: true
      }
    );

    toast.success('Kunde erfolgreich angelegt!');
    navigate(`/customers/${customer.id}`);

  } catch (error) {
    toast.error('Fehler beim Anlegen des Kunden');
  }
};
```

---

### **Phase 4: Testing & Polish** (2-4h)

#### **4.1 Unit Tests**
- CreateOpportunityDialog: Form validation
- Filter Logic: Status filtering
- API Integration: Mock responses

#### **4.2 E2E Tests (Playwright)**
```typescript
test('Lead to Opportunity to Customer flow', async ({ page }) => {
  // 1. Open Lead Detail
  await page.goto('/leads/90003');

  // 2. Click "In Opportunity konvertieren"
  await page.click('button:has-text("In Opportunity konvertieren")');

  // 3. Fill form
  await page.fill('input[name="dealType"]', 'Liefervertrag');
  await page.fill('input[name="expectedValue"]', '336000');

  // 4. Submit
  await page.click('button:has-text("Erstellen")');

  // 5. Verify redirect to Kanban
  await expect(page).toHaveURL(/\/opportunities/);

  // 6. Verify Opportunity in NEW_LEAD column
  await expect(page.locator('.kanban-column-NEW_LEAD')).toContainText('Hotel Seeblick');
});
```

#### **4.3 UI/UX Polish**
- Loading States (Spinner beim Erstellen)
- Empty States (keine Opportunities)
- Error States (API Fehler)
- Accessibility (ARIA labels, Keyboard navigation)

---

## ✅ DEFINITION OF DONE

### **Functional:**
- [ ] User kann via UI Lead → Opportunity erstellen (< 3 Klicks)
- [ ] User kann via UI Customer → Opportunity erstellen
- [ ] User kann via UI Opportunity → Customer konvertieren
- [ ] Kanban zeigt nur offene Opportunities (Filter "Nur offene")
- [ ] Lead Detail zeigt Opportunity-Status + Link
- [ ] Customer Detail zeigt Opportunities + Link

### **Technical:**
- [ ] Backend Endpoint `/api/opportunities?status=active` implementiert
- [ ] Frontend Filter State Management funktioniert
- [ ] Unit Tests: >80% Coverage
- [ ] E2E Tests: Happy Path getestet
- [ ] Keine Console Errors
- [ ] TypeScript Compilation: 0 Errors

### **Quality:**
- [ ] Code Review: 2 Approvals
- [ ] UI/UX Review: Jörg Approval
- [ ] Performance: Kanban lädt < 2s
- [ ] Accessibility: WCAG AA compliant

### **Documentation:**
- [ ] User Guide: Screenshots + Workflow
- [ ] Technical Docs: API Endpoints updated
- [ ] Changelog: Sprint Summary

---

## 📊 SUCCESS METRICS

### **Usability:**
- Lead → Opportunity: Max 3 Klicks, < 30 Sekunden
- Kanban Übersichtlichkeit: Max 10 Opportunities pro Spalte sichtbar (Filter!)

### **Adoption:**
- Nach 2 Wochen: 80% der Leads werden zu Opportunities konvertiert
- Feedback: "Workflow ist klar und intuitiv" (Jörg Approval)

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

## 📅 TIMELINE (Estimated)

**Tag 1 (8h):**
- Phase 1: Lead → Opportunity UI complete

**Tag 2 (8h):**
- Phase 2: Kanban Enhancements complete
- Phase 3: 50% (Customer Detail Integration)

**Tag 3 (8h):**
- Phase 3: Customer → Opportunity complete
- Phase 4: Testing & Polish

**Puffer:** +4h für Bugfixes

---

## 🔗 RELATED WORK

### **Dependent Sprints:**
- Sprint 2.1.7.0: Backend Integration (COMPLETE ✅)
- Sprint 2.1.6: Lead Management (COMPLETE ✅)

### **Follow-up Sprints:**
- Sprint 2.1.7.2: Opportunity Forecasting & Reports
- Sprint 2.1.7.3: Opportunity ROI Calculator Integration

---

## 📝 NOTES

### **Design Decisions:**
- Lead Status wird auf CONVERTED gesetzt bei Opportunity-Erstellung (ONE-WAY, Industry Standard)
- Kanban Filter: Default = "Nur offene" (bessere UX)
- CLOSED Opportunities werden nach 30 Tagen automatisch archiviert (Backend Job, separate Story)

### **Technical Debt:**
- OpportunityActivities benötigen app_user.id (aktuell optional in V90003)
- Pagination für Kanban-Spalten (erst bei >50 Opportunities pro Stage)

---

**Sprint bereit für Kickoff!** 🚀
