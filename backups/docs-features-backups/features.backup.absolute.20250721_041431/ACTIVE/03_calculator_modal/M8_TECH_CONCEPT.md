# M8 Calculator Modal Tech Concept - Enhanced Calculator Experience

**Feature-Code:** M8  
**Feature-Typ:** 🎨 FRONTEND Enhancement  
**Priorität:** HIGH  
**Aufwand:** 2-3 Tage  
**Status:** 📋 TECH CONCEPT  

---

## 🎯 ÜBERBLICK

### Geschäftlicher Kontext
Das Calculator Modal-System erweitert den bestehenden Calculator um eine moderne, modal-basierte Benutzeroberfläche mit erweiterten Funktionen. Es transformiert das aktuelle Calculator-Interface in ein flexibles, wiederverwendbares Modal-System, das von jeder Stelle der Anwendung aufgerufen werden kann und intelligent auf den Kontext reagiert.

### Technische Vision
Entwicklung eines intelligenten Calculator-Modal-Systems mit Material-UI, das den bestehenden Calculator-Code als Basis nutzt und um Smart-Features wie automatische Preisberechnung, Mehrfach-Kalkulationen, Template-Management und Echtzeit-Collaboration erweitert. Das System bietet eine nahtlose Integration in bestehende Workflows.

### Business Value
- **Flexibilität:** Calculator von überall in der App aufrufbar
- **Effizienz:** Schnelle Kalkulationen ohne Seitenwechsel
- **Intelligenz:** Kontextbezogene Vorschläge und Automatisierung
- **Collaboration:** Gemeinsame Kalkulationen und Kommentare

---

## 🏗️ ARCHITEKTUR

### Modal-System Architektur (React + Material-UI)
```typescript
// Hauptkomponente - Calculator Modal System
export interface CalculatorModalProps {
    open: boolean;
    onClose: () => void;
    customerId?: string;
    opportunityId?: string;
    mode?: 'create' | 'edit' | 'duplicate' | 'template';
    calculationId?: string;
    context?: CalculationContext;
}

export const CalculatorModal: React.FC<CalculatorModalProps> = ({
    open,
    onClose,
    customerId,
    opportunityId,
    mode = 'create',
    calculationId,
    context
}) => {
    const [calculation, setCalculation] = useState<Calculation | null>(null);
    const [loading, setLoading] = useState(false);
    const [activeStep, setActiveStep] = useState(0);
    const { user } = useAuth();
    const { hasPermission } = usePermissions();
    
    return (
        <Dialog
            open={open}
            onClose={onClose}
            maxWidth="xl"
            fullWidth
            sx={{ '& .MuiDialog-paper': { height: '90vh' } }}
        >
            <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    <CalculateIcon color="primary" />
                    <Typography variant="h5">
                        {getModalTitle(mode, context)}
                    </Typography>
                    {calculation?.status && (
                        <Chip
                            label={calculation.status}
                            color={getStatusColor(calculation.status)}
                            size="small"
                        />
                    )}
                </Box>
                <Box sx={{ display: 'flex', gap: 1 }}>
                    <CalculatorToolbar 
                        calculation={calculation}
                        onSave={handleSave}
                        onTemplate={handleSaveAsTemplate}
                        onDuplicate={handleDuplicate}
                        onExport={handleExport}
                        hasPermission={hasPermission}
                    />
                    <IconButton onClick={onClose}>
                        <CloseIcon />
                    </IconButton>
                </Box>
            </DialogTitle>
            
            <DialogContent sx={{ p: 0, display: 'flex', flexDirection: 'column' }}>
                <CalculatorStepper
                    activeStep={activeStep}
                    onStepChange={setActiveStep}
                    calculation={calculation}
                />
                
                <Box sx={{ flex: 1, overflow: 'hidden' }}>
                    <CalculatorStepContent
                        step={activeStep}
                        calculation={calculation}
                        onCalculationChange={setCalculation}
                        context={context}
                        mode={mode}
                    />
                </Box>
            </DialogContent>
            
            <DialogActions sx={{ p: 2, borderTop: 1, borderColor: 'divider' }}>
                <CalculatorActions
                    activeStep={activeStep}
                    onNext={() => setActiveStep(prev => prev + 1)}
                    onBack={() => setActiveStep(prev => prev - 1)}
                    onSave={handleSave}
                    onCancel={onClose}
                    calculation={calculation}
                    hasUnsavedChanges={hasUnsavedChanges}
                />
            </DialogActions>
        </Dialog>
    );
};
```

### Stepper-Navigation System
```typescript
// Multi-Step Calculator Interface
export const CalculatorStepper: React.FC<CalculatorStepperProps> = ({
    activeStep,
    onStepChange,
    calculation
}) => {
    const steps = [
        { label: 'Grunddaten', icon: <InfoIcon /> },
        { label: 'Positionen', icon: <ListIcon /> },
        { label: 'Kalkulation', icon: <CalculateIcon /> },
        { label: 'Zusammenfassung', icon: <SummaryIcon /> }
    ];
    
    return (
        <Stepper activeStep={activeStep} sx={{ p: 3, bgcolor: 'background.paper' }}>
            {steps.map((step, index) => (
                <Step key={step.label} completed={isStepCompleted(index, calculation)}>
                    <StepButton onClick={() => onStepChange(index)}>
                        <StepLabel
                            optional={getStepOptional(index)}
                            icon={step.icon}
                        >
                            {step.label}
                        </StepLabel>
                    </StepButton>
                </Step>
            ))}
        </Stepper>
    );
};

// Step Content Router
export const CalculatorStepContent: React.FC<StepContentProps> = ({
    step,
    calculation,
    onCalculationChange,
    context,
    mode
}) => {
    switch (step) {
        case 0:
            return (
                <BasicDataStep
                    calculation={calculation}
                    onChange={onCalculationChange}
                    context={context}
                />
            );
        case 1:
            return (
                <PositionsStep
                    calculation={calculation}
                    onChange={onCalculationChange}
                    mode={mode}
                />
            );
        case 2:
            return (
                <CalculationStep
                    calculation={calculation}
                    onChange={onCalculationChange}
                />
            );
        case 3:
            return (
                <SummaryStep
                    calculation={calculation}
                    context={context}
                />
            );
        default:
            return null;
    }
};
```

### Enhanced Calculation Steps
```typescript
// 1. Grunddaten Step - Smart Context Integration
export const BasicDataStep: React.FC<BasicDataStepProps> = ({
    calculation,
    onChange,
    context
}) => {
    const [customer, setCustomer] = useState<Customer | null>(null);
    const [opportunity, setOpportunity] = useState<Opportunity | null>(null);
    
    useEffect(() => {
        if (context?.customerId) {
            loadCustomer(context.customerId).then(setCustomer);
        }
        if (context?.opportunityId) {
            loadOpportunity(context.opportunityId).then(setOpportunity);
        }
    }, [context]);
    
    return (
        <Container maxWidth="lg" sx={{ py: 3 }}>
            <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                    <Paper sx={{ p: 3 }}>
                        <Typography variant="h6" gutterBottom>
                            Allgemeine Informationen
                        </Typography>
                        
                        <TextField
                            label="Kalkulationsname"
                            value={calculation?.name || ''}
                            onChange={(e) => updateCalculation('name', e.target.value)}
                            fullWidth
                            margin="normal"
                            required
                        />
                        
                        <CustomerSelector
                            value={customer}
                            onChange={handleCustomerChange}
                            disabled={!!context?.customerId}
                            preselected={context?.customerId}
                        />
                        
                        <OpportunitySelector
                            customerId={customer?.id}
                            value={opportunity}
                            onChange={handleOpportunityChange}
                            disabled={!!context?.opportunityId}
                            preselected={context?.opportunityId}
                        />
                        
                        <DatePicker
                            label="Gültig bis"
                            value={calculation?.validUntil}
                            onChange={(date) => updateCalculation('validUntil', date)}
                            sx={{ mt: 2, width: '100%' }}
                        />
                    </Paper>
                </Grid>
                
                <Grid item xs={12} md={6}>
                    <Paper sx={{ p: 3 }}>
                        <Typography variant="h6" gutterBottom>
                            Kalkulationseinstellungen
                        </Typography>
                        
                        <FormControl fullWidth margin="normal">
                            <InputLabel>Kalkulationstyp</InputLabel>
                            <Select
                                value={calculation?.type || 'standard'}
                                onChange={(e) => updateCalculation('type', e.target.value)}
                            >
                                <MenuItem value="standard">Standard</MenuItem>
                                <MenuItem value="express">Express</MenuItem>
                                <MenuItem value="complex">Komplex</MenuItem>
                                <MenuItem value="template">Aus Template</MenuItem>
                            </Select>
                        </FormControl>
                        
                        <TextField
                            label="Marge (%)"
                            type="number"
                            value={calculation?.margin || 20}
                            onChange={(e) => updateCalculation('margin', parseFloat(e.target.value))}
                            fullWidth
                            margin="normal"
                            inputProps={{ min: 0, max: 100, step: 0.1 }}
                        />
                        
                        <FormControlLabel
                            control={
                                <Switch
                                    checked={calculation?.includeDelivery || false}
                                    onChange={(e) => updateCalculation('includeDelivery', e.target.checked)}
                                />
                            }
                            label="Lieferkosten einschließen"
                        />
                        
                        <FormControlLabel
                            control={
                                <Switch
                                    checked={calculation?.includeTax || true}
                                    onChange={(e) => updateCalculation('includeTax', e.target.checked)}
                                />
                            }
                            label="MwSt. einschließen"
                        />
                    </Paper>
                </Grid>
            </Grid>
        </Container>
    );
};

// 2. Positionen Step - Enhanced Position Management
export const PositionsStep: React.FC<PositionsStepProps> = ({
    calculation,
    onChange,
    mode
}) => {
    const [positions, setPositions] = useState<CalculationPosition[]>(
        calculation?.positions || []
    );
    const [selectedPositions, setSelectedPositions] = useState<string[]>([]);
    
    const handleAddPosition = (position: Partial<CalculationPosition>) => {
        const newPosition: CalculationPosition = {
            id: generateId(),
            quantity: 1,
            unit: 'Stück',
            unitPrice: 0,
            discount: 0,
            ...position
        };
        
        const updatedPositions = [...positions, newPosition];
        setPositions(updatedPositions);
        updateCalculation('positions', updatedPositions);
    };
    
    return (
        <Container maxWidth="xl" sx={{ py: 3 }}>
            <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="h6">
                    Kalkulationspositionen
                </Typography>
                <Box sx={{ display: 'flex', gap: 1 }}>
                    <Button
                        variant="outlined"
                        startIcon={<AddIcon />}
                        onClick={() => setAddPositionDialogOpen(true)}
                    >
                        Position hinzufügen
                    </Button>
                    <Button
                        variant="outlined"
                        startIcon={<LibraryAddIcon />}
                        onClick={handleAddFromTemplate}
                    >
                        Aus Template
                    </Button>
                    {selectedPositions.length > 0 && (
                        <Button
                            variant="outlined"
                            color="error"
                            startIcon={<DeleteIcon />}
                            onClick={handleDeleteSelected}
                        >
                            Löschen ({selectedPositions.length})
                        </Button>
                    )}
                </Box>
            </Box>
            
            <EnhancedDataGrid
                rows={positions}
                columns={getPositionColumns()}
                checkboxSelection
                onRowSelectionModelChange={setSelectedPositions}
                autoHeight
                sx={{ minHeight: 400 }}
                slots={{
                    toolbar: PositionsToolbar,
                    row: PositionRow
                }}
                slotProps={{
                    toolbar: {
                        onBulkEdit: handleBulkEdit,
                        onImport: handleImportPositions,
                        onExport: handleExportPositions
                    }
                }}
            />
            
            <PositionSummaryCard
                positions={positions}
                margin={calculation?.margin || 20}
                includeDelivery={calculation?.includeDelivery}
                includeTax={calculation?.includeTax}
            />
        </Container>
    );
};

// 3. Kalkulation Step - Advanced Calculation Logic
export const CalculationStep: React.FC<CalculationStepProps> = ({
    calculation,
    onChange
}) => {
    const [calculationRules, setCalculationRules] = useState<CalculationRule[]>([]);
    const [appliedRules, setAppliedRules] = useState<string[]>([]);
    const calculationResult = useCalculationEngine(calculation, appliedRules);
    
    return (
        <Container maxWidth="xl" sx={{ py: 3 }}>
            <Grid container spacing={3}>
                <Grid item xs={12} md={8}>
                    <Paper sx={{ p: 3 }}>
                        <Typography variant="h6" gutterBottom>
                            Kalkulationsregeln
                        </Typography>
                        
                        <CalculationRulesEngine
                            rules={calculationRules}
                            appliedRules={appliedRules}
                            onRulesChange={setAppliedRules}
                            calculation={calculation}
                        />
                        
                        <Box sx={{ mt: 3 }}>
                            <Typography variant="h6" gutterBottom>
                                Preisberechnung
                            </Typography>
                            <PriceCalculationMatrix
                                positions={calculation?.positions || []}
                                rules={appliedRules}
                                result={calculationResult}
                            />
                        </Box>
                    </Paper>
                </Grid>
                
                <Grid item xs={12} md={4}>
                    <Paper sx={{ p: 3, position: 'sticky', top: 20 }}>
                        <Typography variant="h6" gutterBottom>
                            Kalkulationsergebnis
                        </Typography>
                        
                        <CalculationResultDisplay
                            result={calculationResult}
                            margin={calculation?.margin}
                            showDetails
                        />
                        
                        <Divider sx={{ my: 2 }} />
                        
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                            <Button
                                variant="outlined"
                                startIcon={<RefreshIcon />}
                                onClick={recalculate}
                                fullWidth
                            >
                                Neu berechnen
                            </Button>
                            <Button
                                variant="outlined"
                                startIcon={<SaveIcon />}
                                onClick={saveCalculation}
                                fullWidth
                            >
                                Zwischenspeichern
                            </Button>
                        </Box>
                    </Paper>
                </Grid>
            </Grid>
        </Container>
    );
};

// 4. Zusammenfassung Step - Final Review and Actions
export const SummaryStep: React.FC<SummaryStepProps> = ({
    calculation,
    context
}) => {
    const [exportFormat, setExportFormat] = useState<'pdf' | 'excel' | 'email'>('pdf');
    const [sendToCustomer, setSendToCustomer] = useState(false);
    
    return (
        <Container maxWidth="lg" sx={{ py: 3 }}>
            <Grid container spacing={3}>
                <Grid item xs={12} md={8}>
                    <Paper sx={{ p: 3 }}>
                        <Typography variant="h6" gutterBottom>
                            Kalkulationszusammenfassung
                        </Typography>
                        
                        <CalculationSummaryView
                            calculation={calculation}
                            showAllDetails
                        />
                        
                        <Divider sx={{ my: 3 }} />
                        
                        <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                            <Button
                                variant="contained"
                                startIcon={<PictureAsPdfIcon />}
                                onClick={() => handleExport('pdf')}
                            >
                                PDF erstellen
                            </Button>
                            <Button
                                variant="outlined"
                                startIcon={<TableChartIcon />}
                                onClick={() => handleExport('excel')}
                            >
                                Excel Export
                            </Button>
                            <Button
                                variant="outlined"
                                startIcon={<EmailIcon />}
                                onClick={() => handleSendEmail()}
                            >
                                Per E-Mail senden
                            </Button>
                        </Box>
                    </Paper>
                </Grid>
                
                <Grid item xs={12} md={4}>
                    <Paper sx={{ p: 3 }}>
                        <Typography variant="h6" gutterBottom>
                            Nächste Schritte
                        </Typography>
                        
                        <FormControlLabel
                            control={
                                <Checkbox
                                    checked={sendToCustomer}
                                    onChange={(e) => setSendToCustomer(e.target.checked)}
                                />
                            }
                            label="An Kunde senden"
                        />
                        
                        <FormControlLabel
                            control={
                                <Checkbox
                                    checked={createOpportunity}
                                    onChange={(e) => setCreateOpportunity(e.target.checked)}
                                />
                            }
                            label="Opportunity erstellen"
                        />
                        
                        <FormControlLabel
                            control={
                                <Checkbox
                                    checked={saveAsTemplate}
                                    onChange={(e) => setSaveAsTemplate(e.target.checked)}
                                />
                            }
                            label="Als Template speichern"
                        />
                    </Paper>
                </Grid>
            </Grid>
        </Container>
    );
};
```

### Integration in bestehende Komponenten
```typescript
// Integration in Customer Details
export const CustomerActions: React.FC<CustomerActionsProps> = ({ customer }) => {
    const [calculatorOpen, setCalculatorOpen] = useState(false);
    
    return (
        <>
            <Button
                variant="contained"
                startIcon={<CalculateIcon />}
                onClick={() => setCalculatorOpen(true)}
            >
                Neue Kalkulation
            </Button>
            
            <CalculatorModal
                open={calculatorOpen}
                onClose={() => setCalculatorOpen(false)}
                customerId={customer.id}
                context={{ 
                    customerId: customer.id,
                    customerName: customer.name,
                    source: 'customer-details'
                }}
            />
        </>
    );
};

// Integration in Opportunity Pipeline
export const OpportunityActions: React.FC<OpportunityActionsProps> = ({ opportunity }) => {
    const [calculatorOpen, setCalculatorOpen] = useState(false);
    
    return (
        <>
            <IconButton onClick={() => setCalculatorOpen(true)}>
                <CalculateIcon />
            </IconButton>
            
            <CalculatorModal
                open={calculatorOpen}
                onClose={() => setCalculatorOpen(false)}
                customerId={opportunity.customerId}
                opportunityId={opportunity.id}
                context={{
                    customerId: opportunity.customerId,
                    opportunityId: opportunity.id,
                    source: 'opportunity-pipeline'
                }}
            />
        </>
    );
};
```

---

## 🔗 ABHÄNGIGKEITEN

### Direkte Dependencies
- **M4 Opportunity Pipeline:** Integration für Kalkulation-zu-Opportunity-Verknüpfung
- **FC-009 Advanced Permissions:** Berechtigungsbasierte Funktionen
- **M2 Quick Create Actions:** Quick-Access zur Calculator-Erstellung

### API Dependencies
```typescript
// Calculator API Service - Enhancement des bestehenden Services
export class CalculatorApiService {
    async createCalculation(data: CreateCalculationRequest): Promise<Calculation> {
        return await apiClient.post('/api/calculations', data);
    }
    
    async updateCalculation(id: string, data: UpdateCalculationRequest): Promise<Calculation> {
        return await apiClient.put(`/api/calculations/${id}`, data);
    }
    
    async getCalculationTemplates(): Promise<CalculationTemplate[]> {
        return await apiClient.get('/api/calculations/templates');
    }
    
    async saveAsTemplate(id: string, name: string): Promise<CalculationTemplate> {
        return await apiClient.post(`/api/calculations/${id}/template`, { name });
    }
    
    async duplicateCalculation(id: string): Promise<Calculation> {
        return await apiClient.post(`/api/calculations/${id}/duplicate`);
    }
    
    async exportCalculation(id: string, format: 'pdf' | 'excel'): Promise<Blob> {
        return await apiClient.get(`/api/calculations/${id}/export/${format}`, {
            responseType: 'blob'
        });
    }
}
```

### Backend Requirements (Quarkus)
```java
@Path("/api/calculations")
@ApplicationScoped
public class CalculationResource {
    
    @Inject
    CalculationService calculationService;
    
    @POST
    @RolesAllowed({"admin", "manager", "sales"})
    public Response createCalculation(CreateCalculationRequest request) {
        var calculation = calculationService.createCalculation(request);
        return Response.status(201).entity(calculation).build();
    }
    
    @POST
    @Path("/{id}/template")
    @RolesAllowed({"admin", "manager"})
    public Response saveAsTemplate(@PathParam("id") UUID id, SaveTemplateRequest request) {
        var template = calculationService.saveAsTemplate(id, request);
        return Response.ok(template).build();
    }
    
    @GET
    @Path("/{id}/export/{format}")
    @RolesAllowed({"admin", "manager", "sales"})
    public Response exportCalculation(@PathParam("id") UUID id, @PathParam("format") String format) {
        var export = calculationService.exportCalculation(id, format);
        return Response.ok(export)
            .header("Content-Disposition", "attachment; filename=calculation." + format)
            .build();
    }
}
```

---

## 🧪 TESTING-STRATEGIE

### Unit Tests (Jest + React Testing Library)
```typescript
describe('CalculatorModal', () => {
    it('should open with correct context from customer', () => {
        render(
            <CalculatorModal
                open={true}
                onClose={jest.fn()}
                customerId="customer-123"
                context={{ customerId: "customer-123", source: "customer-details" }}
            />
        );
        
        expect(screen.getByDisplayValue('customer-123')).toBeInTheDocument();
    });
    
    it('should navigate through steps correctly', async () => {
        render(<CalculatorModal open={true} onClose={jest.fn()} />);
        
        // Start at step 0
        expect(screen.getByText('Grunddaten')).toHaveClass('Mui-active');
        
        // Navigate to step 1
        fireEvent.click(screen.getByText('Weiter'));
        await waitFor(() => {
            expect(screen.getByText('Positionen')).toHaveClass('Mui-active');
        });
    });
});

describe('PositionsStep', () => {
    it('should add new position correctly', async () => {
        const mockOnChange = jest.fn();
        render(<PositionsStep calculation={emptyCalculation} onChange={mockOnChange} />);
        
        fireEvent.click(screen.getByText('Position hinzufügen'));
        
        const dialog = screen.getByRole('dialog');
        fireEvent.change(within(dialog).getByLabelText('Artikel'), { target: { value: 'Test Artikel' } });
        fireEvent.change(within(dialog).getByLabelText('Menge'), { target: { value: '5' } });
        fireEvent.click(within(dialog).getByText('Hinzufügen'));
        
        await waitFor(() => {
            expect(mockOnChange).toHaveBeenCalledWith(
                expect.objectContaining({
                    positions: expect.arrayContaining([
                        expect.objectContaining({
                            name: 'Test Artikel',
                            quantity: 5
                        })
                    ])
                })
            );
        });
    });
});
```

### Integration Tests
```typescript
describe('Calculator Integration', () => {
    it('should create calculation from customer context', async () => {
        const { user } = render(<App />);
        
        // Navigate to customer details
        await user.click(screen.getByText('Kunden'));
        await user.click(screen.getByText('Firma ABC'));
        
        // Open calculator
        await user.click(screen.getByText('Neue Kalkulation'));
        
        // Verify customer is preselected
        expect(screen.getByDisplayValue('Firma ABC')).toBeInTheDocument();
        
        // Complete calculation
        await user.type(screen.getByLabelText('Kalkulationsname'), 'Test Kalkulation');
        await user.click(screen.getByText('Weiter'));
        
        // Add position
        await user.click(screen.getByText('Position hinzufügen'));
        await user.type(screen.getByLabelText('Artikel'), 'Test Produkt');
        await user.type(screen.getByLabelText('Menge'), '10');
        await user.type(screen.getByLabelText('Einzelpreis'), '50');
        await user.click(screen.getByText('Hinzufügen'));
        
        // Complete and save
        await user.click(screen.getByText('Weiter'));
        await user.click(screen.getByText('Weiter'));
        await user.click(screen.getByText('Speichern'));
        
        // Verify calculation was created
        await waitFor(() => {
            expect(screen.getByText('Kalkulation gespeichert')).toBeInTheDocument();
        });
    });
});
```

### E2E Tests (Playwright)
```typescript
test('Complete calculator workflow', async ({ page }) => {
    await page.goto('/login');
    await page.fill('[data-testid="username"]', 'sales@freshplan.de');
    await page.fill('[data-testid="password"]', 'sales123');
    await page.click('[data-testid="login-button"]');
    
    // Open calculator from quick actions
    await page.click('[data-testid="quick-create-fab"]');
    await page.click('[data-testid="quick-create-calculation"]');
    
    // Fill basic data
    await page.fill('[data-testid="calculation-name"]', 'E2E Test Kalkulation');
    await page.click('[data-testid="customer-selector"]');
    await page.click('text=Test Customer GmbH');
    
    // Add positions
    await page.click('[data-testid="next-step"]');
    await page.click('[data-testid="add-position"]');
    await page.fill('[data-testid="position-name"]', 'Test Produkt');
    await page.fill('[data-testid="position-quantity"]', '5');
    await page.fill('[data-testid="position-price"]', '100');
    await page.click('[data-testid="save-position"]');
    
    // Review calculation
    await page.click('[data-testid="next-step"]');
    await page.click('[data-testid="next-step"]');
    
    // Verify summary
    await expect(page.locator('[data-testid="total-amount"]')).toContainText('500,00 €');
    
    // Save and export
    await page.click('[data-testid="save-calculation"]');
    await page.click('[data-testid="export-pdf"]');
    
    // Verify download started
    const downloadPromise = page.waitForEvent('download');
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toContain('.pdf');
});
```

---

## 📋 IMPLEMENTIERUNGSPLAN

### 🕒 15-Minuten Claude Working Section

**Aufgabe:** Bestehenden Calculator in Modal-System transformieren

**Sofort loslegen:**
1. Aktuellen Calculator-Code analysieren und Modal-Wrapper erstellen
2. Material-UI Dialog mit Stepper-Navigation implementieren
3. Erste Step-Komponente (BasicDataStep) umsetzen
4. Context-Integration für Customer/Opportunity-Vorauswahl

**Quick-Win Schritte:**
```typescript
// 1. Modal Basis erstellen
// frontend/src/features/calculator/CalculatorModal.tsx

// 2. Stepper Navigation hinzufügen
const steps = ['Grunddaten', 'Positionen', 'Kalkulation', 'Zusammenfassung'];

// 3. Context-Integration implementieren
useEffect(() => {
    if (context?.customerId) {
        setPreselectedCustomer(context.customerId);
    }
}, [context]);

// 4. Bestehende Calculator-Logik integrieren
<CalculatorStepContent step={activeStep} calculation={calculation} />
```

### Phase 1: Modal-Grundgerüst (Tag 1 - 4h)
- ✅ Material-UI Dialog mit responsive Layout
- ✅ Stepper-Navigation zwischen 4 Steps implementieren
- ✅ Context-basierte Vorauswahl von Customer/Opportunity
- ✅ Grundlegende State-Management-Struktur

### Phase 2: Enhanced Steps (Tag 2 - 4h)
- ✅ BasicDataStep mit Smart-Defaults
- ✅ PositionsStep mit Enhanced DataGrid
- ✅ CalculationStep mit Rules Engine
- ✅ Auto-Save und Validierung

### Phase 3: Advanced Features (Tag 3 - 3h)
- ✅ Template-Management System
- ✅ Export-Funktionalität (PDF, Excel, E-Mail)
- ✅ Collaboration Features (Kommentare, Sharing)
- ✅ Integration in bestehende Workflows

### Erfolgs-Kriterien
- ✅ Calculator von überall in der App aufrufbar
- ✅ Context-bewusste Vorauswahl und Intelligenz
- ✅ Nahtlose Integration in Customer/Opportunity-Workflows
- ✅ Export und Template-Management funktional
- ✅ Mobile-responsive Design
- ✅ Unit-Test-Coverage ≥ 80%

---

## 🎯 QUALITÄTSSTANDARDS

### Performance
- **Modal-Öffnung:** < 300ms für alle Calculator-Modi
- **Step-Navigation:** Sofortige Reaktion ohne Verzögerung
- **Calculation Engine:** < 100ms für Standard-Kalkulationen
- **Export-Generation:** < 2s für PDF/Excel-Export

### UX-Standards
- **Context-Awareness:** Intelligente Vorauswahl basierend auf Quelle
- **Progressive Enhancement:** Erweiterte Features für Power-User
- **Keyboard-Navigation:** Vollständige Tastatur-Unterstützung
- **Auto-Save:** Kontinuierliche Speicherung ohne Benutzereingabe

### Integration
- **API-Kompatibilität:** Kompatibel mit bestehenden Calculator-APIs
- **Permission-Awareness:** Respektiert alle Benutzerberechtigungen
- **Responsive Design:** Optimiert für Desktop, Tablet und Mobile
- **Accessibility:** WCAG 2.1 AA Compliance für alle Modal-Inhalte

---

## 🔗 ABSOLUTE NAVIGATION ZU ALLEN 40 FEATURES

### 🟢 ACTIVE Features (In Entwicklung - 9 Features)
| Code | Feature | Dokument |
|------|---------|----------|
| FC-008 | Security Foundation | [TECH_CONCEPT](/docs/features/ACTIVE/01_security_foundation/FC-008_TECH_CONCEPT.md) |
| M4 | Opportunity Pipeline | [KOMPAKT](/docs/features/ACTIVE/02_opportunity_pipeline/M4_TECH_CONCEPT.md) |
| **M8** | **Calculator Modal** | **[TECH_CONCEPT](/docs/features/ACTIVE/03_calculator_modal/M8_TECH_CONCEPT.md)** ⭐ |
| FC-009 | Advanced Permissions | [TECH_CONCEPT](/docs/features/ACTIVE/04_permissions_system/FC-009_TECH_CONCEPT.md) |
| M1 | Navigation System | [TECH_CONCEPT](/docs/features/ACTIVE/05_ui_foundation/M1_TECH_CONCEPT.md) |
| M2 | Quick Create Actions | [TECH_CONCEPT](/docs/features/ACTIVE/05_ui_foundation/M2_TECH_CONCEPT.md) |
| M3 | Sales Cockpit Enhancement | [TECH_CONCEPT](/docs/features/ACTIVE/05_ui_foundation/M3_TECH_CONCEPT.md) |
| M7 | Settings Enhancement | [TECH_CONCEPT](/docs/features/ACTIVE/05_ui_foundation/M7_TECH_CONCEPT.md) |

### 🔵 PLANNED Features (Geplant - 31 Features)

#### Customer & Sales Features (FC-001 bis FC-007)
| Code | Feature | Dokument |
|------|---------|----------|
| FC-001 | Customer Acquisition Engine | [TECH_CONCEPT](/docs/features/PLANNED/01_customer_acquisition/FC-001_TECH_CONCEPT.md) |
| FC-002 | Smart Customer Insights | [TECH_CONCEPT](/docs/features/PLANNED/02_smart_insights/FC-002_TECH_CONCEPT.md) |
| FC-003 | E-Mail Integration | [KOMPAKT](/docs/features/PLANNED/06_email_integration/FC-003_TECH_CONCEPT.md) |
| FC-004 | Verkäuferschutz | [KOMPAKT](/docs/features/PLANNED/07_verkaeuferschutz/FC-004_TECH_CONCEPT.md) |
| FC-005 | Xentral Integration | [KOMPAKT](/docs/features/PLANNED/08_xentral_integration/FC-005_TECH_CONCEPT.md) |
| FC-006 | Mobile App | [KOMPAKT](/docs/features/PLANNED/09_mobile_app/FC-006_TECH_CONCEPT.md) |
| FC-007 | Chef-Dashboard | [KOMPAKT](/docs/features/PLANNED/10_chef_dashboard/FC-007_TECH_CONCEPT.md) |

#### Core Infrastructure Features (FC-008 bis FC-021)
| Code | Feature | Dokument |
|------|---------|----------|
| FC-010 | Customer Import | [KOMPAKT](/docs/features/PLANNED/11_customer_import/FC-010_TECH_CONCEPT.md) |
| FC-011 | Bonitätsprüfung | [KOMPAKT](/docs/features/ACTIVE/02_opportunity_pipeline/integrations/FC-011_TECH_CONCEPT.md) |
| FC-012 | Team Communication | [KOMPAKT](/docs/features/PLANNED/14_team_communication/FC-012_TECH_CONCEPT.md) |
| FC-013 | Duplicate Detection | [KOMPAKT](/docs/features/PLANNED/15_duplicate_detection/FC-013_TECH_CONCEPT.md) |
| FC-014 | Activity Timeline | [KOMPAKT](/docs/features/PLANNED/16_activity_timeline/FC-014_TECH_CONCEPT.md) |
| FC-015 | Deal Loss Analysis | [KOMPAKT](/docs/features/PLANNED/17_deal_loss_analysis/FC-015_TECH_CONCEPT.md) |
| FC-016 | Opportunity Cloning | [KOMPAKT](/docs/features/PLANNED/18_opportunity_cloning/FC-016_TECH_CONCEPT.md) |
| FC-017 | Sales Gamification | [KOMPAKT](/docs/features/PLANNED/99_sales_gamification/FC-017_TECH_CONCEPT.md) |
| FC-018 | Mobile PWA | [KOMPAKT](/docs/features/PLANNED/09_mobile_app/FC-018_MOBILE_FIELD_SALES.md) |
| FC-019 | Advanced Sales Metrics | [KOMPAKT](/docs/features/PLANNED/19_advanced_metrics/FC-019_TECH_CONCEPT.md) |
| FC-020 | Quick Wins | [KOMPAKT](/docs/features/PLANNED/20_quick_wins/FC-020_TECH_CONCEPT.md) |
| FC-021 | Integration Hub | [KOMPAKT](/docs/features/PLANNED/21_integration_hub/FC-021_TECH_CONCEPT.md) |

#### Modern Platform Features (FC-022 bis FC-036)
| Code | Feature | Dokument |
|------|---------|----------|
| FC-022 | Mobile Light | [KOMPAKT](/docs/features/PLANNED/22_mobile_light/FC-022_TECH_CONCEPT.md) |
| FC-023 | Event Sourcing | [KOMPAKT](/docs/features/PLANNED/23_event_sourcing/FC-023_TECH_CONCEPT.md) |
| FC-024 | File Management | [KOMPAKT](/docs/features/PLANNED/24_file_management/FC-024_TECH_CONCEPT.md) |
| FC-025 | DSGVO Compliance | [KOMPAKT](/docs/features/PLANNED/25_dsgvo_compliance/FC-025_TECH_CONCEPT.md) |
| FC-026 | Analytics Platform | [KOMPAKT](/docs/features/PLANNED/26_analytics_platform/FC-026_TECH_CONCEPT.md) |
| FC-027 | Magic Moments | [KOMPAKT](/docs/features/PLANNED/27_magic_moments/FC-027_TECH_CONCEPT.md) |
| FC-028 | WhatsApp Business | [KOMPAKT](/docs/features/PLANNED/28_whatsapp_integration/FC-028_TECH_CONCEPT.md) |
| FC-029 | Voice-First Interface | [KOMPAKT](/docs/features/PLANNED/29_voice_first/FC-029_TECH_CONCEPT.md) |
| FC-030 | One-Tap Actions | [KOMPAKT](/docs/features/PLANNED/30_one_tap_actions/FC-030_TECH_CONCEPT.md) |
| FC-031 | Smart Templates | [KOMPAKT](/docs/features/PLANNED/31_smart_templates/FC-031_TECH_CONCEPT.md) |
| FC-032 | Offline-First | [KOMPAKT](/docs/features/PLANNED/32_offline_first/FC-032_TECH_CONCEPT.md) |
| FC-033 | Visual Customer Cards | [TECH_CONCEPT](/docs/features/PLANNED/33_visual_cards/FC-033_TECH_CONCEPT.md) |
| FC-034 | Instant Insights | [TECH_CONCEPT](/docs/features/PLANNED/34_instant_insights/FC-034_TECH_CONCEPT.md) |
| FC-035 | Social Selling Helper | [TECH_CONCEPT](/docs/features/PLANNED/35_social_selling/FC-035_TECH_CONCEPT.md) |
| FC-036 | Beziehungsmanagement | [TECH_CONCEPT](/docs/features/PLANNED/36_relationship_mgmt/FC-036_TECH_CONCEPT.md) |

#### Enterprise Platform Features (FC-037 bis FC-040)
| Code | Feature | Dokument |
|------|---------|----------|
| FC-037 | Advanced Reporting | [TECH_CONCEPT](/docs/features/PLANNED/37_advanced_reporting/FC-037_TECH_CONCEPT.md) |
| FC-038 | Multi-Tenant Architecture | [TECH_CONCEPT](/docs/features/PLANNED/38_multitenant/FC-038_TECH_CONCEPT.md) |
| FC-039 | API Gateway | [TECH_CONCEPT](/docs/features/PLANNED/39_api_gateway/FC-039_TECH_CONCEPT.md) |
| FC-040 | Performance Monitoring | [TECH_CONCEPT](/docs/features/PLANNED/40_performance_monitoring/FC-040_TECH_CONCEPT.md) |

#### Module Features (M1 bis M6)
| Code | Feature | Dokument |
|------|---------|----------|
| M5 | Customer Refactor | [TECH_CONCEPT](/docs/features/PLANNED/12_customer_refactor_m5/M5_TECH_CONCEPT.md) |
| M6 | Analytics Module | [TECH_CONCEPT](/docs/features/PLANNED/13_analytics_m6/M6_TECH_CONCEPT.md) |

#### Future Features
| Code | Feature | Status |
|------|---------|--------|
| FC-041 | Future Feature Slot | Noch zu definieren |

### 📊 Tech Concept Coverage
- **Abgeschlossen:** 40 von 42 Features (95.2%)
- **Verbleibend:** FC-041 + 1 Future Feature  
- **Session 16 Status:** 2 von 12 Features verbleibend für 100% Coverage

---

**🎯 Nächster Schritt:** FC-041 Future Feature Tech Concept erstellen