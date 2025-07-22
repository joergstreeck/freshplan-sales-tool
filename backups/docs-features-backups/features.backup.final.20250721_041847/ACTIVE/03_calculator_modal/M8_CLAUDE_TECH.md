# M8 Calculator Modal - CLAUDE TECH 🤖

## 🧠 QUICK-LOAD (30 Sekunden bis zur Produktivität)

**Feature:** Modal-basierter Calculator mit Stepper UI + Smart Features + PDF Export  
**Stack:** React + MUI Dialog + React Hook Form + Stepper + Context API  
**Status:** 📋 Ready - Frontend Enhancement  
**Dependencies:** Existing Calculator Logic | Erweitert: M4 Opportunity, FC-024 File Management  

**Jump to:** [📚 Recipes](#-implementation-recipes) | [🧪 Tests](#-test-patterns) | [🔌 Integration](#-integration-cookbook) | [🎯 Smart Features](#-smart-features)

**Core Purpose in 1 Line:** `Open Modal → Fill Steps → Calculate Prices → Generate PDF → Save/Export`

---

## 🍳 IMPLEMENTATION RECIPES

### Recipe 1: Calculator Modal in 5 Minuten
```typescript
// 1. Main Calculator Modal Component (copy-paste ready)
export const CalculatorModal: React.FC<{
    open: boolean;
    onClose: () => void;
    customerId?: string;
    opportunityId?: string;
    mode?: 'create' | 'edit' | 'duplicate';
}> = ({ open, onClose, customerId, opportunityId, mode = 'create' }) => {
    const [activeStep, setActiveStep] = useState(0);
    const [calculation, setCalculation] = useState<Calculation>(getInitialCalculation());
    const { saveCalculation, isLoading } = useSaveCalculation();
    
    const steps = ['Grunddaten', 'Positionen', 'Kalkulation', 'Zusammenfassung'];
    
    const handleNext = () => {
        if (activeStep === steps.length - 1) {
            handleSave();
        } else {
            setActiveStep(prev => prev + 1);
        }
    };
    
    const handleSave = async () => {
        try {
            await saveCalculation.mutateAsync({
                ...calculation,
                customerId,
                opportunityId
            });
            toast.success('Kalkulation gespeichert');
            onClose();
        } catch (error) {
            toast.error('Fehler beim Speichern');
        }
    };
    
    return (
        <Dialog
            open={open}
            onClose={onClose}
            maxWidth="lg"
            fullWidth
            PaperProps={{ sx: { height: '90vh' } }}
        >
            <DialogTitle>
                <Box display="flex" alignItems="center" justifyContent="space-between">
                    <Typography variant="h6">
                        {mode === 'edit' ? 'Kalkulation bearbeiten' : 'Neue Kalkulation'}
                    </Typography>
                    <IconButton onClick={onClose}>
                        <CloseIcon />
                    </IconButton>
                </Box>
            </DialogTitle>
            
            <Stepper activeStep={activeStep} sx={{ p: 3 }}>
                {steps.map((label) => (
                    <Step key={label}>
                        <StepLabel>{label}</StepLabel>
                    </Step>
                ))}
            </Stepper>
            
            <DialogContent sx={{ flex: 1, overflow: 'auto' }}>
                <CalculatorStepContent
                    step={activeStep}
                    calculation={calculation}
                    onChange={setCalculation}
                />
            </DialogContent>
            
            <DialogActions sx={{ p: 2, borderTop: 1, borderColor: 'divider' }}>
                <Button onClick={onClose}>Abbrechen</Button>
                <Button 
                    disabled={activeStep === 0} 
                    onClick={() => setActiveStep(prev => prev - 1)}
                >
                    Zurück
                </Button>
                <LoadingButton
                    variant="contained"
                    onClick={handleNext}
                    loading={isLoading}
                >
                    {activeStep === steps.length - 1 ? 'Speichern' : 'Weiter'}
                </LoadingButton>
            </DialogActions>
        </Dialog>
    );
};

// 2. Step Content Router
const CalculatorStepContent: React.FC<{
    step: number;
    calculation: Calculation;
    onChange: (calc: Calculation) => void;
}> = ({ step, calculation, onChange }) => {
    switch (step) {
        case 0:
            return <BasicDataStep calculation={calculation} onChange={onChange} />;
        case 1:
            return <PositionsStep calculation={calculation} onChange={onChange} />;
        case 2:
            return <CalculationStep calculation={calculation} onChange={onChange} />;
        case 3:
            return <SummaryStep calculation={calculation} />;
        default:
            return null;
    }
};
```

### Recipe 2: Dynamic Positions Management
```typescript
// 3. Positions Step with Dynamic Form (copy-paste ready)
const PositionsStep: React.FC<{
    calculation: Calculation;
    onChange: (calc: Calculation) => void;
}> = ({ calculation, onChange }) => {
    const { fields, append, remove, update } = useFieldArray({
        name: 'positions',
        defaultValue: calculation.positions || []
    });
    
    const addPosition = () => {
        append({
            id: uuidv4(),
            name: '',
            quantity: 1,
            unitPrice: 0,
            unit: 'Stück',
            category: 'product'
        });
    };
    
    const calculateTotal = () => {
        return fields.reduce((sum, pos) => sum + (pos.quantity * pos.unitPrice), 0);
    };
    
    return (
        <Box>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant="h6">Positionen</Typography>
                <Button
                    startIcon={<AddIcon />}
                    onClick={addPosition}
                    variant="outlined"
                >
                    Position hinzufügen
                </Button>
            </Box>
            
            <TableContainer component={Paper} variant="outlined">
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Bezeichnung</TableCell>
                            <TableCell align="right">Menge</TableCell>
                            <TableCell>Einheit</TableCell>
                            <TableCell align="right">Einzelpreis</TableCell>
                            <TableCell align="right">Gesamt</TableCell>
                            <TableCell width={50}></TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {fields.map((position, index) => (
                            <TableRow key={position.id}>
                                <TableCell>
                                    <TextField
                                        value={position.name}
                                        onChange={(e) => update(index, { ...position, name: e.target.value })}
                                        placeholder="Produktname"
                                        fullWidth
                                        size="small"
                                    />
                                </TableCell>
                                <TableCell align="right">
                                    <TextField
                                        type="number"
                                        value={position.quantity}
                                        onChange={(e) => update(index, { ...position, quantity: +e.target.value })}
                                        size="small"
                                        sx={{ width: 100 }}
                                    />
                                </TableCell>
                                <TableCell>
                                    <Select
                                        value={position.unit}
                                        onChange={(e) => update(index, { ...position, unit: e.target.value })}
                                        size="small"
                                    >
                                        <MenuItem value="Stück">Stück</MenuItem>
                                        <MenuItem value="kg">kg</MenuItem>
                                        <MenuItem value="l">Liter</MenuItem>
                                        <MenuItem value="h">Stunden</MenuItem>
                                    </Select>
                                </TableCell>
                                <TableCell align="right">
                                    <TextField
                                        type="number"
                                        value={position.unitPrice}
                                        onChange={(e) => update(index, { ...position, unitPrice: +e.target.value })}
                                        size="small"
                                        sx={{ width: 120 }}
                                        InputProps={{
                                            startAdornment: <InputAdornment position="start">€</InputAdornment>
                                        }}
                                    />
                                </TableCell>
                                <TableCell align="right">
                                    <Typography>
                                        €{(position.quantity * position.unitPrice).toFixed(2)}
                                    </Typography>
                                </TableCell>
                                <TableCell>
                                    <IconButton
                                        size="small"
                                        onClick={() => remove(index)}
                                    >
                                        <DeleteIcon />
                                    </IconButton>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                    <TableFooter>
                        <TableRow>
                            <TableCell colSpan={4} align="right">
                                <Typography variant="h6">Gesamt:</Typography>
                            </TableCell>
                            <TableCell align="right">
                                <Typography variant="h6">
                                    €{calculateTotal().toFixed(2)}
                                </Typography>
                            </TableCell>
                            <TableCell />
                        </TableRow>
                    </TableFooter>
                </Table>
            </TableContainer>
        </Box>
    );
};
```

### Recipe 3: Calculator Context Provider
```typescript
// 4. Global Calculator Context (copy-paste ready)
interface CalculatorContextType {
    openCalculator: (options?: CalculatorOptions) => void;
    closeCalculator: () => void;
    isOpen: boolean;
    currentCalculation?: Calculation;
}

const CalculatorContext = createContext<CalculatorContextType | null>(null);

export const CalculatorProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [isOpen, setIsOpen] = useState(false);
    const [options, setOptions] = useState<CalculatorOptions>({});
    
    const openCalculator = (opts: CalculatorOptions = {}) => {
        setOptions(opts);
        setIsOpen(true);
    };
    
    const closeCalculator = () => {
        setIsOpen(false);
        setOptions({});
    };
    
    return (
        <CalculatorContext.Provider value={{ openCalculator, closeCalculator, isOpen }}>
            {children}
            <CalculatorModal
                open={isOpen}
                onClose={closeCalculator}
                {...options}
            />
        </CalculatorContext.Provider>
    );
};

export const useCalculator = () => {
    const context = useContext(CalculatorContext);
    if (!context) throw new Error('useCalculator must be used within CalculatorProvider');
    return context;
};

// Usage anywhere in the app:
const MyComponent = () => {
    const { openCalculator } = useCalculator();
    
    return (
        <Button onClick={() => openCalculator({ customerId: '123', mode: 'create' })}>
            Neue Kalkulation
        </Button>
    );
};
```

---

## 🧪 TEST PATTERNS

### Pattern 1: Modal Integration Test
```typescript
describe('CalculatorModal', () => {
    it('should navigate through all steps', async () => {
        const onClose = jest.fn();
        const { getByText, getByRole } = render(
            <CalculatorModal open={true} onClose={onClose} />
        );
        
        // Step 1: Basic Data
        expect(getByText('Grunddaten')).toBeInTheDocument();
        
        // Fill basic data
        fireEvent.change(getByRole('textbox', { name: /projektname/i }), {
            target: { value: 'Test Projekt' }
        });
        
        // Go to next step
        fireEvent.click(getByText('Weiter'));
        
        // Step 2: Positions
        await waitFor(() => {
            expect(getByText('Positionen')).toBeInTheDocument();
        });
        
        // Add position
        fireEvent.click(getByText('Position hinzufügen'));
        
        // Continue through steps...
    });
});
```

### Pattern 2: Calculation Logic Test
```typescript
describe('Calculator Logic', () => {
    it('should calculate totals correctly', () => {
        const positions = [
            { quantity: 10, unitPrice: 5.50 },
            { quantity: 5, unitPrice: 12.00 },
            { quantity: 2, unitPrice: 25.00 }
        ];
        
        const total = calculateTotal(positions);
        expect(total).toBe(165.00); // 55 + 60 + 50
        
        const withMarkup = applyMarkup(total, 15); // 15% markup
        expect(withMarkup).toBe(189.75);
        
        const withDiscount = applyDiscount(withMarkup, 10); // 10% discount
        expect(withDiscount).toBe(170.775);
    });
});
```

---

## 🔌 INTEGRATION COOKBOOK

### Mit Opportunity Pipeline (M4)
```typescript
// Auto-open calculator from opportunity
export const OpportunityActions: React.FC<{ opportunity: Opportunity }> = ({ opportunity }) => {
    const { openCalculator } = useCalculator();
    
    const handleCreateQuote = () => {
        openCalculator({
            customerId: opportunity.customerId,
            opportunityId: opportunity.id,
            mode: 'create',
            // Pre-fill with opportunity data
            defaultValues: {
                projectName: opportunity.title,
                projectValue: opportunity.value,
                customerName: opportunity.customer.name
            }
        });
    };
    
    return (
        <Button
            startIcon={<CalculateIcon />}
            onClick={handleCreateQuote}
            variant="contained"
        >
            Angebot erstellen
        </Button>
    );
};
```

### Mit Customer Detail
```typescript
// Calculator history in customer view
export const CustomerCalculations: React.FC<{ customerId: string }> = ({ customerId }) => {
    const { data: calculations } = useCustomerCalculations(customerId);
    const { openCalculator } = useCalculator();
    
    return (
        <Card>
            <CardHeader
                title="Kalkulationen"
                action={
                    <Button
                        size="small"
                        onClick={() => openCalculator({ customerId, mode: 'create' })}
                    >
                        Neue Kalkulation
                    </Button>
                }
            />
            <CardContent>
                <List>
                    {calculations?.map(calc => (
                        <ListItem
                            key={calc.id}
                            button
                            onClick={() => openCalculator({ 
                                calculationId: calc.id, 
                                mode: 'edit' 
                            })}
                        >
                            <ListItemText
                                primary={calc.projectName}
                                secondary={`€${calc.totalAmount} - ${formatDate(calc.createdAt)}`}
                            />
                            <ListItemSecondaryAction>
                                <IconButton
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        downloadPdf(calc.id);
                                    }}
                                >
                                    <DownloadIcon />
                                </IconButton>
                            </ListItemSecondaryAction>
                        </ListItem>
                    ))}
                </List>
            </CardContent>
        </Card>
    );
};
```

---

## 🎯 SMART FEATURES

### Template System
```typescript
// Save and reuse calculation templates
const useCalculationTemplates = () => {
    const saveAsTemplate = async (calculation: Calculation, templateName: string) => {
        const template = {
            ...calculation,
            id: undefined,
            isTemplate: true,
            templateName,
            positions: calculation.positions.map(p => ({
                ...p,
                id: undefined
            }))
        };
        
        await api.post('/api/calculation-templates', template);
        toast.success('Template gespeichert');
    };
    
    const loadTemplate = async (templateId: string): Promise<Calculation> => {
        const template = await api.get(`/api/calculation-templates/${templateId}`);
        return {
            ...template,
            isTemplate: false,
            templateName: undefined,
            createdAt: new Date()
        };
    };
    
    return { saveAsTemplate, loadTemplate };
};
```

### Auto-Save Draft
```typescript
// Automatic draft saving
const useAutoSaveDraft = (calculation: Calculation) => {
    const [lastSaved, setLastSaved] = useState<Date | null>(null);
    
    useEffect(() => {
        if (!calculation.id) return;
        
        const saveTimer = setTimeout(async () => {
            try {
                await api.put(`/api/calculations/${calculation.id}/draft`, calculation);
                setLastSaved(new Date());
            } catch (error) {
                console.error('Auto-save failed:', error);
            }
        }, 2000); // Save after 2 seconds of inactivity
        
        return () => clearTimeout(saveTimer);
    }, [calculation]);
    
    return lastSaved;
};
```

### PDF Export
```typescript
// Generate and download PDF
const useExportPdf = () => {
    const exportPdf = async (calculationId: string) => {
        try {
            const response = await api.get(
                `/api/calculations/${calculationId}/export/pdf`,
                { responseType: 'blob' }
            );
            
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `Kalkulation_${calculationId}.pdf`);
            document.body.appendChild(link);
            link.click();
            link.remove();
            
            toast.success('PDF exportiert');
        } catch (error) {
            toast.error('PDF Export fehlgeschlagen');
        }
    };
    
    return { exportPdf };
};
```

---

## 📚 DEEP KNOWLEDGE (Bei Bedarf expandieren)

<details>
<summary>🎨 Advanced UI Features</summary>

### Keyboard Shortcuts
```typescript
const useCalculatorKeyboard = () => {
    useEffect(() => {
        const handleKeyPress = (e: KeyboardEvent) => {
            // Ctrl/Cmd + K to open calculator
            if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
                e.preventDefault();
                openCalculator();
            }
            
            // Ctrl/Cmd + S to save
            if ((e.ctrlKey || e.metaKey) && e.key === 's' && isOpen) {
                e.preventDefault();
                handleSave();
            }
        };
        
        window.addEventListener('keydown', handleKeyPress);
        return () => window.removeEventListener('keydown', handleKeyPress);
    }, [isOpen]);
};
```

### Collaborative Editing
```typescript
// Real-time collaboration indicator
const CollaborationIndicator: React.FC<{ calculationId: string }> = ({ calculationId }) => {
    const { activeUsers } = useCollaboration(calculationId);
    
    return (
        <AvatarGroup max={3}>
            {activeUsers.map(user => (
                <Tooltip key={user.id} title={user.name}>
                    <Avatar src={user.avatar} alt={user.name}>
                        {user.initials}
                    </Avatar>
                </Tooltip>
            ))}
        </AvatarGroup>
    );
};
```

</details>

<details>
<summary>📊 Analytics Integration</summary>

### Track Calculator Usage
```typescript
const trackCalculatorEvent = (event: string, data?: any) => {
    if (window.gtag) {
        window.gtag('event', event, {
            event_category: 'Calculator',
            event_label: data?.mode || 'unknown',
            value: data?.totalAmount
        });
    }
};

// Usage
trackCalculatorEvent('calculator_opened', { mode: 'create' });
trackCalculatorEvent('calculation_saved', { totalAmount: 1500 });
```

</details>

---

**🎯 Nächster Schritt:** Calculator Modal Component implementieren und mit bestehendem Calculator-Code verbinden