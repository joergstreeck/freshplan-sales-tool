# 📘 M8 IMPLEMENTATION GUIDE

**Zweck:** Calculator Modal mit Context-Awareness  
**Prinzip:** Legacy Calculator wrappen, nicht refactoren  

---

## <a id="modal-setup"></a>🎨 Modal Setup

### Basic Modal Structure
```typescript
import { Dialog, DialogContent, DialogActions, IconButton } from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import LegacyCalculator from '../../../legacy/Calculator'; // Existing Calculator

interface CalculatorModalProps {
  open: boolean;
  onClose: () => void;
  context?: CalculatorContext;
  onSave?: (calculation: Calculation) => void;
}

export const CalculatorModal: React.FC<CalculatorModalProps> = ({
  open,
  onClose,
  context,
  onSave
}) => {
  const [isDirty, setIsDirty] = useState(false);
  
  // Warnung bei ungespeicherten Änderungen
  const handleClose = () => {
    if (isDirty) {
      if (confirm('Ungespeicherte Änderungen verwerfen?')) {
        onClose();
      }
    } else {
      onClose();
    }
  };
  
  return (
    <Dialog
      open={open}
      onClose={handleClose}
      maxWidth="xl"
      fullWidth
      sx={{
        '& .MuiDialog-paper': {
          height: '90vh',
          maxHeight: '900px'
        }
      }}
    >
      {/* Header mit Context Info */}
      <Box sx={{ 
        display: 'flex', 
        alignItems: 'center', 
        p: 2, 
        borderBottom: 1, 
        borderColor: 'divider' 
      }}>
        <Box flex={1}>
          <Typography variant="h6">
            Angebot erstellen
          </Typography>
          {context && (
            <Typography variant="body2" color="text.secondary">
              {context.customerName} • {context.opportunityTitle}
            </Typography>
          )}
        </Box>
        <IconButton onClick={handleClose}>
          <CloseIcon />
        </IconButton>
      </Box>
      
      {/* Calculator Content */}
      <DialogContent sx={{ p: 0, overflow: 'hidden' }}>
        <Box sx={{ height: '100%', overflow: 'auto' }}>
          <CalculatorContextProvider context={context}>
            <LegacyCalculator 
              onDataChange={() => setIsDirty(true)}
            />
          </CalculatorContextProvider>
        </Box>
      </DialogContent>
      
      {/* Guided Action Buttons */}
      <CalculatorActions 
        onSave={onSave}
        onClose={onClose}
        isDirty={isDirty}
        context={context}
      />
    </Dialog>
  );
};
```

### Guided Actions Component
```typescript
const CalculatorActions: React.FC<ActionsProps> = ({ 
  onSave, 
  onClose, 
  isDirty,
  context 
}) => {
  const [saving, setSaving] = useState(false);
  
  const handleSaveAndEmail = async () => {
    setSaving(true);
    const calculation = await saveCalculation();
    onSave?.(calculation);
    
    // Öffne E-Mail Dialog mit Kontext
    openEmailComposer({
      to: context?.customerEmail,
      subject: `Angebot: ${context?.opportunityTitle}`,
      attachments: [calculation.pdfUrl],
      template: 'proposal'
    });
    
    onClose();
  };
  
  const handleSaveAsPDF = async () => {
    setSaving(true);
    const calculation = await saveCalculation();
    const pdfUrl = await generatePDF(calculation);
    
    // Download
    window.open(pdfUrl, '_blank');
    
    // Attach to Opportunity
    if (context?.opportunityId) {
      await attachToOpportunity(context.opportunityId, pdfUrl);
    }
    
    onSave?.(calculation);
    onClose();
  };
  
  return (
    <DialogActions sx={{ p: 2, gap: 1 }}>
      {/* Sekundäre Aktionen links */}
      <Button
        startIcon={<SaveAltIcon />}
        onClick={() => saveAsTemplate()}
        disabled={!isDirty}
      >
        Als Vorlage
      </Button>
      
      <Box flex={1} />
      
      {/* Primäre Aktionen rechts */}
      <Button onClick={onClose} disabled={saving}>
        Abbrechen
      </Button>
      <Button 
        onClick={handleSaveAsPDF}
        disabled={!isDirty || saving}
        startIcon={<PictureAsPdfIcon />}
      >
        PDF erstellen
      </Button>
      <Button
        variant="contained"
        onClick={handleSaveAndEmail}
        disabled={!isDirty || saving}
        startIcon={<EmailIcon />}
      >
        Speichern & E-Mail
      </Button>
    </DialogActions>
  );
};
```

---

## <a id="context-flow"></a>🔄 Context Flow Architecture

### Calculator Context Provider
```typescript
interface CalculatorContext {
  // Von Opportunity
  opportunityId?: string;
  opportunityTitle?: string;
  opportunityValue?: number;
  
  // Von Customer
  customerId?: string;
  customerName?: string;
  customerEmail?: string;
  customerDiscount?: number;
  
  // Von letzter Bestellung
  lastOrderItems?: OrderItem[];
  preferredProducts?: Product[];
}

const CalculatorContextProvider: React.FC<{
  context?: CalculatorContext;
  children: React.ReactNode;
}> = ({ context, children }) => {
  
  useEffect(() => {
    if (context) {
      // Injiziere Daten in Legacy Calculator
      injectContextData(context);
    }
  }, [context]);
  
  return (
    <CalculatorContextContext.Provider value={context}>
      {children}
    </CalculatorContextContext.Provider>
  );
};

// Injection in Legacy Calculator
function injectContextData(context: CalculatorContext) {
  // Kunde vorauswählen
  if (context.customerId) {
    window.legacyCalculator?.selectCustomer(context.customerId);
  }
  
  // Produkte vorschlagen
  if (context.lastOrderItems?.length) {
    window.legacyCalculator?.suggestProducts(context.lastOrderItems);
  }
  
  // Rabatt setzen
  if (context.customerDiscount) {
    window.legacyCalculator?.setDiscount(context.customerDiscount);
  }
}
```

### Integration Points
```typescript
// 1. Von Opportunity Board
const handleCreateProposal = (opportunity: Opportunity) => {
  openCalculatorModal({
    opportunityId: opportunity.id,
    opportunityTitle: opportunity.title,
    opportunityValue: opportunity.value,
    customerId: opportunity.customer.id,
    customerName: opportunity.customer.name,
    customerEmail: opportunity.customer.email
  });
};

// 2. Von Customer Detail
const handleNewCalculation = (customer: Customer) => {
  openCalculatorModal({
    customerId: customer.id,
    customerName: customer.name,
    customerEmail: customer.email,
    customerDiscount: customer.defaultDiscount,
    lastOrderItems: customer.lastOrder?.items
  });
};

// 3. Standalone (Menü)
const handleStandaloneCalculator = () => {
  openCalculatorModal({
    // Kein Context
  });
};
```

---

## <a id="calculator-api"></a>🔌 Calculator API Integration

### Save Calculation
```typescript
interface SaveCalculationDTO {
  customerId?: string;
  opportunityId?: string;
  items: CalculationItem[];
  totalNet: number;
  totalGross: number;
  discount: number;
  notes?: string;
}

const saveCalculation = async (data: SaveCalculationDTO) => {
  const response = await api.post('/api/calculations', data);
  
  // Verknüpfe mit Opportunity
  if (data.opportunityId) {
    await api.patch(`/api/opportunities/${data.opportunityId}`, {
      calculationId: response.data.id,
      stage: 'PROPOSAL' // Auto-advance Stage
    });
  }
  
  return response.data;
};
```

### Load Templates
```typescript
const useCalculationTemplates = (customerId?: string) => {
  return useQuery({
    queryKey: ['calculation-templates', customerId],
    queryFn: async () => {
      // Globale Templates
      const globalTemplates = await api.get('/api/calculations/templates');
      
      // Kunden-spezifische Templates
      let customerTemplates = [];
      if (customerId) {
        customerTemplates = await api.get(
          `/api/calculations/templates?customerId=${customerId}`
        );
      }
      
      return {
        global: globalTemplates.data,
        customer: customerTemplates.data
      };
    }
  });
};
```

---

## <a id="state-management"></a>💾 State Management

### Global Calculator State (Zustand)
```typescript
interface CalculatorStore {
  // Modal State
  isOpen: boolean;
  context: CalculatorContext | null;
  
  // Calculation State
  currentCalculation: Calculation | null;
  isDirty: boolean;
  
  // Actions
  openCalculator: (context?: CalculatorContext) => void;
  closeCalculator: () => void;
  saveCalculation: () => Promise<Calculation>;
  loadTemplate: (templateId: string) => Promise<void>;
}

const useCalculatorStore = create<CalculatorStore>((set, get) => ({
  isOpen: false,
  context: null,
  currentCalculation: null,
  isDirty: false,
  
  openCalculator: (context) => {
    set({ 
      isOpen: true, 
      context,
      isDirty: false 
    });
    
    // Track Event
    analytics.track('calculator_opened', {
      hasContext: !!context,
      source: context?.opportunityId ? 'opportunity' : 'standalone'
    });
  },
  
  closeCalculator: () => {
    const { isDirty } = get();
    
    if (isDirty) {
      // Handled by Component
      return;
    }
    
    set({ 
      isOpen: false, 
      context: null,
      currentCalculation: null 
    });
  },
  
  saveCalculation: async () => {
    const { currentCalculation, context } = get();
    
    const saved = await api.saveCalculation({
      ...currentCalculation,
      customerId: context?.customerId,
      opportunityId: context?.opportunityId
    });
    
    set({ isDirty: false });
    return saved;
  }
}));
```

---

## <a id="guided-actions"></a>🎯 Guided Actions Flow

### Nach-Kalkulation Workflow
```typescript
const PostCalculationGuide: React.FC<{
  calculation: Calculation;
  context?: CalculatorContext;
}> = ({ calculation, context }) => {
  const [step, setStep] = useState<'email' | 'followup' | 'done'>('email');
  
  if (step === 'email') {
    return (
      <EmailComposer
        to={context?.customerEmail}
        subject={`Ihr Angebot: ${calculation.title}`}
        template="proposal"
        attachments={[calculation.pdfUrl]}
        onSent={() => setStep('followup')}
      />
    );
  }
  
  if (step === 'followup') {
    return (
      <FollowUpScheduler
        customerId={context?.customerId}
        opportunityId={context?.opportunityId}
        defaultDate={addDays(new Date(), 3)}
        onScheduled={() => setStep('done')}
        onSkip={() => setStep('done')}
      />
    );
  }
  
  return (
    <SuccessMessage
      message="Angebot wurde erstellt und versendet!"
      actions={[
        {
          label: 'Zur Opportunity',
          onClick: () => navigateToOpportunity(context?.opportunityId)
        },
        {
          label: 'Neue Kalkulation',
          onClick: () => openCalculator()
        }
      ]}
    />
  );
};
```

### Smart Suggestions
```typescript
const CalculatorSuggestions: React.FC = () => {
  const { context, currentCalculation } = useCalculatorStore();
  const suggestions = useSmartSuggestions(context, currentCalculation);
  
  if (!suggestions.length) return null;
  
  return (
    <Alert 
      severity="info" 
      sx={{ m: 2 }}
      action={
        <IconButton size="small" onClick={() => dismissSuggestions()}>
          <CloseIcon />
        </IconButton>
      }
    >
      <AlertTitle>Vorschläge</AlertTitle>
      <Stack spacing={1}>
        {suggestions.map(suggestion => (
          <Box key={suggestion.id} display="flex" alignItems="center">
            <suggestion.icon sx={{ mr: 1 }} />
            <Typography variant="body2">{suggestion.text}</Typography>
            <Button size="small" onClick={suggestion.action}>
              {suggestion.actionLabel}
            </Button>
          </Box>
        ))}
      </Stack>
    </Alert>
  );
};

// Suggestion Engine
const useSmartSuggestions = (context, calculation) => {
  const suggestions = [];
  
  // Kontext-basierte Vorschläge
  if (context?.lastOrderItems && !calculation?.items?.length) {
    suggestions.push({
      id: 'use-last-order',
      icon: HistoryIcon,
      text: 'Letzte Bestellung als Vorlage nutzen',
      actionLabel: 'Übernehmen',
      action: () => loadLastOrder(context.lastOrderItems)
    });
  }
  
  // Value-basierte Vorschläge
  if (context?.opportunityValue && calculation?.totalNet) {
    const diff = Math.abs(context.opportunityValue - calculation.totalNet);
    if (diff > context.opportunityValue * 0.2) {
      suggestions.push({
        id: 'value-mismatch',
        icon: WarningIcon,
        text: `Angebotswert weicht ${Math.round(diff)}€ von Opportunity ab`,
        actionLabel: 'Anpassen',
        action: () => showValueAdjustment()
      });
    }
  }
  
  return suggestions;
};
```