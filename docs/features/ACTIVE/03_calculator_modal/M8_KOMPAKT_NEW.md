# M8 Calculator Modal - KOMPAKT ⚡

**Feature-Typ:** 🎨 FRONTEND Enhancement  
**Priorität:** HIGH  
**Aufwand:** 2-3 Tage  
**Status:** 📋 READY  

---

## 🎯 ÜBERBLICK

### Business Context
Calculator Modal erweitert den bestehenden Calculator um eine moderne, modal-basierte UI, die von überall in der App aufrufbar ist. Bietet intelligente Kontextintegration, Template-Management und nahtlose Workflow-Integration.

### Technical Vision  
React Modal-System mit Material-UI, 4-Step-Wizard (Grunddaten → Positionen → Kalkulation → Zusammenfassung), Smart Context Loading und Export-Funktionalität. Baut auf bestehendem Calculator-Code auf.

---

## 🏗️ CORE ARCHITEKTUR

### Modal-System Structure
```typescript
// Hauptkomponente - Calculator Modal
export const CalculatorModal: React.FC<CalculatorModalProps> = ({
    open, onClose, customerId, opportunityId, mode = 'create'
}) => {
    const [activeStep, setActiveStep] = useState(0);
    const [calculation, setCalculation] = useState<Calculation | null>(null);
    
    return (
        <Dialog open={open} maxWidth="xl" fullWidth>
            <DialogTitle>
                <CalculatorToolbar onSave={handleSave} onExport={handleExport} />
            </DialogTitle>
            <DialogContent>
                <CalculatorStepper activeStep={activeStep} />
                <CalculatorStepContent step={activeStep} calculation={calculation} />
            </DialogContent>
            <DialogActions>
                <CalculatorActions activeStep={activeStep} onNext={handleNext} />
            </DialogActions>
        </Dialog>
    );
};

// 4-Step System
const steps = ['Grunddaten', 'Positionen', 'Kalkulation', 'Zusammenfassung'];

// Step Components
- BasicDataStep: Customer/Opportunity selection, basic settings
- PositionsStep: Enhanced DataGrid for calculation positions  
- CalculationStep: Rules engine, price calculation
- SummaryStep: Review, export options (PDF, Excel, Email)
```

### Key Integration Points
```typescript
// Context-Aware Integration
<CalculatorModal 
    customerId={customer.id}
    context={{ source: 'customer-details', customerName: customer.name }}
/>

// Quick Actions Integration  
<Button onClick={() => setCalculatorOpen(true)}>
    <CalculateIcon /> Neue Kalkulation
</Button>

// API Service Enhancement
export class CalculatorApiService {
    async createCalculation(data: CreateCalculationRequest): Promise<Calculation>
    async saveAsTemplate(id: string, name: string): Promise<CalculationTemplate>
    async exportCalculation(id: string, format: 'pdf'|'excel'): Promise<Blob>
}
```

---

## 🔗 DEPENDENCIES

- **M4 Opportunity Pipeline:** Kalkulation-zu-Opportunity-Verknüpfung
- **FC-009 Advanced Permissions:** Berechtigungsbasierte Funktionen  
- **M2 Quick Create Actions:** Quick-Access zur Calculator-Erstellung
- **Bestehender Calculator:** Code-Basis für Berechnungslogik

---

## 🧪 TESTING

### Unit Tests
```typescript
describe('CalculatorModal', () => {
    it('should open with correct context from customer', () => {
        render(<CalculatorModal customerId="123" context={{source: "customer"}} />);
        expect(screen.getByDisplayValue('customer-123')).toBeInTheDocument();
    });
    
    it('should navigate through steps correctly', async () => {
        render(<CalculatorModal open={true} />);
        fireEvent.click(screen.getByText('Weiter'));
        expect(screen.getByText('Positionen')).toHaveClass('Mui-active');
    });
});
```

### Integration Tests
- Calculator von verschiedenen Kontexten aufrufbar
- Template-Speicherung und -Wiederherstellung
- Export-Funktionalität (PDF, Excel, E-Mail)
- Auto-Save und Validierung

---

## 📋 QUICK IMPLEMENTATION

### 🕒 15-Min Claude Working Section

**Aufgabe:** Bestehenden Calculator in Modal-System transformieren

**Sofort loslegen:**
1. Material-UI Dialog mit responsive Layout erstellen
2. Stepper-Navigation zwischen 4 Steps implementieren  
3. Context-Integration für Customer/Opportunity-Vorauswahl
4. Erste Step-Komponente (BasicDataStep) umsetzen

**Quick-Win Code:**
```typescript
// 1. Modal Basis erstellen
export const CalculatorModal: React.FC<CalculatorModalProps> = ({ open, onClose }) => (
    <Dialog open={open} onClose={onClose} maxWidth="xl" fullWidth>
        <CalculatorContent />
    </Dialog>
);

// 2. Stepper hinzufügen
const [activeStep, setActiveStep] = useState(0);
const steps = ['Grunddaten', 'Positionen', 'Kalkulation', 'Zusammenfassung'];

// 3. Context-Integration
useEffect(() => {
    if (context?.customerId) {
        setPreselectedCustomer(context.customerId);
    }
}, [context]);

// 4. Integration in Customer Details
<Button onClick={() => setCalculatorOpen(true)}>
    <CalculateIcon /> Neue Kalkulation
</Button>
```

**Nächste Schritte:**
- Phase 1: Modal-Grundgerüst (4h)  
- Phase 2: Enhanced Steps (4h)
- Phase 3: Advanced Features (3h)

---

**🔗 DETAIL-DOCS:** 
- [TECH_CONCEPT](/docs/features/ACTIVE/03_calculator_modal/M8_TECH_CONCEPT.md) - Vollständige technische Spezifikation
- [IMPLEMENTATION_GUIDE](/docs/features/ACTIVE/03_calculator_modal/M8_IMPLEMENTATION_GUIDE.md) - Detaillierte Umsetzungsanleitung

**🎯 Nächster Schritt:** Modal-Grundgerüst mit Material-UI Dialog erstellen