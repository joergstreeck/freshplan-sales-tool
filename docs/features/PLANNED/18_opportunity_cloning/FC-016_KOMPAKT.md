# 🔄 FC-016 OPPORTUNITY CLONING (KOMPAKT)

**Feature Code:** FC-016  
**Feature-Typ:** 🎨 FRONTEND  
**Geschätzter Aufwand:** 2-3 Tage  
**Priorität:** MEDIUM - Produktivitäts-Booster  
**ROI:** 80% Zeitersparnis bei Folgegeschäften  

---

## 🎯 PROBLEM & LÖSUNG IN 30 SEKUNDEN

**Problem:** Jahresvertrag verlängern = alles nochmal eintippen, gleiche Produkte, gleiche Konditionen  
**Lösung:** Ein-Klick Opportunity Cloning mit intelligenten Anpassungen  
**Impact:** 10 Min → 30 Sek für Folge-Opportunities  

---

## 🧠 CLONE WORKFLOW

```
BESTEHENDE OPPORTUNITY
         ↓
[🔄 Ähnliche erstellen]
         ↓
CLONE DIALOG:
┌────────────────────────┐
│ Was übernehmen?        │
│ ☑ Kunde & Kontakte     │
│ ☑ Produkte & Mengen    │
│ ☑ Rabatte & Konditionen│
│ ☐ Notizen & Dokumente  │
│ ☐ Team-Mitglieder      │
└────────────────────────┘
         ↓
AUTO-ANPASSUNGEN:
• Titel: "Copy of..." → Editierbar
• Stage: → "Lead" (Reset)
• Datum: → Heute + 30 Tage
• Wert: → Übernommen (editierbar)
         ↓
[💾 Opportunity erstellen]
```

---

## 📋 FEATURES IM DETAIL

### 1. Clone Configuration Interface

```typescript
interface CloneConfiguration {
  // Basis-Daten (immer geklont)
  customerId: string;
  contactIds: string[];
  
  // Optionale Übernahmen
  includeProducts: boolean;      // Standard: true
  includeDiscounts: boolean;     // Standard: true
  includeNotes: boolean;         // Standard: false
  includeAttachments: boolean;   // Standard: false
  includeTeamMembers: boolean;   // Standard: true
  
  // Auto-Anpassungen
  titlePrefix: string;           // Standard: "Copy of "
  expectedCloseDate: Date;       // Standard: heute + 30 Tage
  stage: OpportunityStage;       // Standard: "LEAD"
}

interface CloneResult {
  originalId: string;
  clonedId: string;
  clonedFields: string[];
  skippedFields: string[];
  url: string;
}
```

### 2. Clone Dialog Component

```typescript
const OpportunityCloneDialog: React.FC<{opportunity}> = ({ opportunity }) => {
  const [config, setConfig] = useState<CloneConfiguration>({
    customerId: opportunity.customerId,
    contactIds: opportunity.contactIds,
    includeProducts: true,
    includeDiscounts: true,
    includeNotes: false,
    includeAttachments: false,
    includeTeamMembers: true,
    titlePrefix: "Copy of ",
    expectedCloseDate: addDays(new Date(), 30),
    stage: 'LEAD'
  });

  const [newTitle, setNewTitle] = useState(`Copy of ${opportunity.title}`);

  return (
    <Dialog open maxWidth="sm" fullWidth>
      <DialogTitle>
        <Box display="flex" alignItems="center">
          <ContentCopy sx={{ mr: 1 }} />
          Opportunity klonen
        </Box>
      </DialogTitle>
      
      <DialogContent>
        {/* Neuer Titel */}
        <TextField
          fullWidth
          label="Titel der neuen Opportunity"
          value={newTitle}
          onChange={(e) => setNewTitle(e.target.value)}
          sx={{ mb: 3 }}
          autoFocus
          helperText="Tipp: Fügen Sie das Jahr oder Quartal hinzu"
        />

        {/* Clone-Optionen */}
        <FormGroup>
          <FormLabel component="legend">Was soll übernommen werden?</FormLabel>
          
          {/* Immer übernommen */}
          <FormControlLabel
            control={<Checkbox checked disabled />}
            label={
              <Box>
                <Typography>Kunde & Kontakte</Typography>
                <Typography variant="caption" color="text.secondary">
                  {opportunity.customerName} ({opportunity.contacts.length} Kontakte)
                </Typography>
              </Box>
            }
          />

          {/* Produkte & Konditionen */}
          <FormControlLabel
            control={
              <Checkbox
                checked={config.includeProducts}
                onChange={(e) => setConfig({
                  ...config, 
                  includeProducts: e.target.checked,
                  includeDiscounts: e.target.checked // Discounts nur mit Produkten
                })}
              />
            }
            label={
              <Box>
                <Typography>Produkte & Mengen</Typography>
                <Typography variant="caption" color="text.secondary">
                  {opportunity.lineItems?.length || 0} Positionen, 
                  Wert: {formatCurrency(opportunity.value)}
                </Typography>
              </Box>
            }
          />

          {config.includeProducts && (
            <FormControlLabel
              control={
                <Checkbox
                  checked={config.includeDiscounts}
                  onChange={(e) => setConfig({...config, includeDiscounts: e.target.checked})}
                />
              }
              label="Rabatte & Sonderkonditionen"
              sx={{ ml: 3 }}
            />
          )}

          {/* Team */}
          <FormControlLabel
            control={
              <Checkbox
                checked={config.includeTeamMembers}
                onChange={(e) => setConfig({...config, includeTeamMembers: e.target.checked})}
              />
            }
            label={
              <Box>
                <Typography>Team-Mitglieder</Typography>
                <Typography variant="caption" color="text.secondary">
                  {opportunity.teamMembers?.map(m => m.name).join(', ')}
                </Typography>
              </Box>
            }
          />

          {/* Notizen & Dokumente */}
          <FormControlLabel
            control={
              <Checkbox
                checked={config.includeNotes}
                onChange={(e) => setConfig({...config, includeNotes: e.target.checked})}
              />
            }
            label="Notizen & Kommentare"
          />

          <FormControlLabel
            control={
              <Checkbox
                checked={config.includeAttachments}
                onChange={(e) => setConfig({...config, includeAttachments: e.target.checked})}
              />
            }
            label={
              <Box>
                <Typography>Dokumente & Anhänge</Typography>
                <Typography variant="caption" color="text.secondary">
                  {opportunity.attachments?.length || 0} Dateien
                </Typography>
              </Box>
            }
          />
        </FormGroup>

        {/* Erwartetes Abschlussdatum */}
        <TextField
          fullWidth
          type="date"
          label="Erwarteter Abschluss"
          value={config.expectedCloseDate.toISOString().split('T')[0]}
          onChange={(e) => setConfig({
            ...config, 
            expectedCloseDate: new Date(e.target.value)
          })}
          sx={{ mt: 3 }}
          InputLabelProps={{ shrink: true }}
        />

        {/* Smart Suggestions */}
        <Alert severity="info" sx={{ mt: 2 }}>
          <AlertTitle>Intelligente Vorschläge</AlertTitle>
          {opportunity.type === 'RENEWAL' && (
            <Typography variant="body2">
              Dies sieht nach einer Verlängerung aus. Der neue Titel könnte 
              "{opportunity.title.replace(/2024/g, '2025')}" sein.
            </Typography>
          )}
          {opportunity.recurringRevenue && (
            <Typography variant="body2">
              Wiederkehrender Umsatz erkannt. Erwarteter Wert: 
              {formatCurrency(opportunity.recurringRevenue * 12)}
            </Typography>
          )}
        </Alert>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onCancel}>Abbrechen</Button>
        <Button 
          variant="contained" 
          startIcon={<ContentCopy />}
          onClick={() => handleClone(newTitle, config)}
        >
          Opportunity klonen
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

### 3. Backend Clone Service

```java
@Path("/api/opportunities/{id}/clone")
@POST
@RolesAllowed({"sales", "admin"})
public Response cloneOpportunity(@PathParam("id") UUID opportunityId,
                                CloneConfiguration config) {
    
    Opportunity original = opportunityRepository.findById(opportunityId)
        .orElseThrow(() -> new NotFoundException("Opportunity not found"));
    
    // Security Check
    if (!securityContext.canAccess(original)) {
        return Response.status(Status.FORBIDDEN).build();
    }
    
    Opportunity cloned = opportunityService.clone(original, config);
    
    // Audit Log
    auditService.log(AuditAction.OPPORTUNITY_CLONED, 
        "Cloned opportunity " + original.getTitle() + " to " + cloned.getTitle());
    
    return Response.ok(CloneResult.builder()
        .originalId(original.getId())
        .clonedId(cloned.getId())
        .clonedFields(config.getIncludedFields())
        .url("/opportunities/" + cloned.getId())
        .build()).build();
}
```

### 4. Quick Clone Actions

```typescript
// Direkt-Aktionen ohne Dialog
const QuickCloneActions: React.FC<{opportunity}> = ({ opportunity }) => (
  <SpeedDial
    ariaLabel="Quick Clone"
    icon={<ContentCopy />}
    direction="left"
  >
    <SpeedDialAction
      icon={<Refresh />}
      tooltipTitle="Verlängerung erstellen"
      onClick={() => quickClone('renewal')}
    />
    <SpeedDialAction
      icon={<Add />}
      tooltipTitle="Zusatzauftrag"
      onClick={() => quickClone('addon')}
    />
    <SpeedDialAction
      icon={<SwapHoriz />}
      tooltipTitle="Alternativ-Angebot"
      onClick={() => quickClone('alternative')}
    />
  </SpeedDial>
);

// Quick Clone Templates
const quickCloneTemplates = {
  renewal: {
    titleTransform: (title: string) => 
      title.replace(/202\d/g, new Date().getFullYear().toString()),
    dateOffset: 365, // 1 Jahr später
    includeAll: true
  },
  addon: {
    titlePrefix: "Zusatz zu ",
    dateOffset: 30,
    includeProducts: false, // Neue Produkte
    includeDiscounts: true
  },
  alternative: {
    titlePrefix: "Alternative: ",
    dateOffset: 0, // Gleiches Datum
    includeAll: true,
    stage: 'PROPOSAL' // Gleiche Stage
  }
};
```

### 5. Bulk Cloning für Serien

```typescript
// Mehrere Opportunities auf einmal klonen (z.B. Jahresverträge)
const BulkCloneWizard: React.FC = () => {
  const [selectedOpportunities, setSelected] = useState<Opportunity[]>([]);
  const [cloneConfig, setConfig] = useState<BulkCloneConfig>({
    dateOffsetMonths: 12,
    titlePattern: '{original} {year}',
    staggerDays: 0 // Tage zwischen Clones
  });

  return (
    <Stepper activeStep={activeStep}>
      <Step>
        <StepLabel>Opportunities wählen</StepLabel>
        <StepContent>
          <DataGrid
            rows={renewableOpportunities}
            columns={[
              { field: 'title', headerName: 'Titel', flex: 1 },
              { field: 'customer', headerName: 'Kunde', width: 200 },
              { field: 'value', headerName: 'Wert', width: 120 },
              { field: 'closeDate', headerName: 'Ablauf', width: 120 }
            ]}
            checkboxSelection
            onSelectionModelChange={setSelected}
          />
        </StepContent>
      </Step>
      
      <Step>
        <StepLabel>Clone-Einstellungen</StepLabel>
        <StepContent>
          {/* Bulk Config */}
        </StepContent>
      </Step>
      
      <Step>
        <StepLabel>Bestätigung</StepLabel>
        <StepContent>
          <Typography>
            {selectedOpportunities.length} Opportunities werden geklont:
          </Typography>
          {/* Preview */}
        </StepContent>
      </Step>
    </Stepper>
  );
};
```

---

## 🎯 BUSINESS VALUE

- **Zeitersparnis:** 10 Min → 30 Sek pro Folge-Opportunity
- **Fehlerreduktion:** Keine vergessenen Produkte/Rabatte
- **Schnellere Verlängerungen:** Proaktiv vor Vertragsende
- **Konsistenz:** Gleiche Konditionen automatisch übernommen

---

## 🚀 IMPLEMENTIERUNGS-PHASEN

1. **Phase 1:** Basis Clone-Funktion mit Dialog
2. **Phase 2:** Quick Clone Templates
3. **Phase 3:** Bulk Cloning für Serien
4. **Phase 4:** Smart Suggestions mit KI

---

## 🔗 ABHÄNGIGKEITEN

- **Benötigt:** M4 Opportunity Pipeline
- **Integration:** M8 Calculator (Produkte klonen)
- **Nice-to-have:** FC-014 Activity Timeline (Clone-Historie)

---

## 📊 SUCCESS METRICS

- **Clone Usage Rate:** > 50% der Renewal Opportunities
- **Time Saved:** Ø 8 Min pro Clone
- **Error Rate:** < 2% nachträgliche Korrekturen
- **User Satisfaction:** > 90% finden es hilfreich

---

**Nächster Schritt:** Clone-Button in OpportunityCard implementieren