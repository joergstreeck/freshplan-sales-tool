# M2 Quick Create Actions - KOMPAKT ⚡

**Feature-Typ:** 🎨 FRONTEND  
**Priorität:** HIGH  
**Aufwand:** 3-4 Tage  
**Status:** 📋 Ready to Start (nach M4 Pipeline)  

---

## 🎯 ÜBERBLICK

### Business Context
Floating Action Button + Quick-Create-Modals für schnelle Erstellung von Kunden/Opportunities während Telefonaten. Sales-Team braucht Erfassung in <30 Sekunden von jeder Seite aus.

### Technical Vision
Context-aware FAB mit Smart Defaults basierend auf aktueller Seite. Quick-Create-Modals mit Auto-Suggestions und Direct Integration in Sales Cockpit + Pipeline.

---

## 🏗️ CORE ARCHITEKTUR

### FAB + Modal System
```
Current Page → Context Detection → Smart Defaults → Quick Modal → Success Action
      ↓              ↓                ↓              ↓            ↓
   /cockpit    High Priority    Pre-fill Data   Quick Form   Redirect
   /customers  Customer Focus   Location Data   Customer     Refresh List
   /pipeline   Opportunity     Pipeline Stage   Opportunity  Update Pipeline
```

### Core Components
```typescript
// 1. Floating Action Button - Context Aware
export const QuickCreateFAB: React.FC = () => {
    const location = useLocation();
    const { user } = useAuth();
    const [open, setOpen] = useState(false);
    const [createType, setCreateType] = useState<'customer' | 'opportunity'>('customer');
    
    const getContextActions = (pathname: string) => {
        switch (pathname) {
            case '/cockpit':
                return [
                    { type: 'opportunity', icon: <TrendingUpIcon />, label: 'Neue Opportunity' },
                    { type: 'customer', icon: <PersonAddIcon />, label: 'Neuer Kunde' }
                ];
            case '/customers':
                return [{ type: 'customer', icon: <PersonAddIcon />, label: 'Neuer Kunde' }];
            case '/pipeline':
                return [{ type: 'opportunity', icon: <TrendingUpIcon />, label: 'Neue Opportunity' }];
            default:
                return [
                    { type: 'customer', icon: <PersonAddIcon />, label: 'Neuer Kunde' },
                    { type: 'opportunity', icon: <TrendingUpIcon />, label: 'Neue Opportunity' }
                ];
        }
    };
    
    return (
        <>
            <Fab 
                color="primary" 
                sx={{ position: 'fixed', bottom: 16, right: 16 }}
                onClick={() => setOpen(true)}
            >
                <AddIcon />
            </Fab>
            <QuickCreateModal 
                open={open}
                onClose={() => setOpen(false)}
                actions={getContextActions(location.pathname)}
                context={{ currentPage: location.pathname, user }}
            />
        </>
    );
};

// 2. Quick Create Modal with Smart Defaults
export const QuickCreateModal: React.FC<QuickCreateModalProps> = ({
    open, onClose, actions, context
}) => {
    const [activeForm, setActiveForm] = useState<string | null>(null);
    const smartDefaults = getSmartDefaults(context);
    
    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle>Schnell erstellen</DialogTitle>
            <DialogContent>
                {!activeForm ? (
                    <Grid container spacing={2}>
                        {actions.map(action => (
                            <Grid item xs={6} key={action.type}>
                                <Card 
                                    sx={{ cursor: 'pointer' }}
                                    onClick={() => setActiveForm(action.type)}
                                >
                                    <CardContent sx={{ textAlign: 'center' }}>
                                        {action.icon}
                                        <Typography variant="h6">{action.label}</Typography>
                                    </CardContent>
                                </Card>
                            </Grid>
                        ))}
                    </Grid>
                ) : (
                    <QuickCreateForm 
                        type={activeForm}
                        defaults={smartDefaults}
                        onSuccess={(created) => {
                            onClose();
                            handleQuickCreateSuccess(created, context);
                        }}
                        onCancel={() => setActiveForm(null)}
                    />
                )}
            </DialogContent>
        </Dialog>
    );
};

// 3. Quick Create Forms - Minimal Fields Only
export const QuickCustomerForm: React.FC = ({ defaults, onSuccess, onCancel }) => {
    const { handleSubmit, control } = useForm({
        defaultValues: {
            companyName: defaults?.companyName || '',
            contactPerson: defaults?.contactPerson || '',
            email: defaults?.email || '',
            phone: defaults?.phone || '',
            ...defaults
        }
    });
    
    const onSubmit = async (data: QuickCustomerData) => {
        try {
            const customer = await customerService.quickCreate(data);
            onSuccess(customer);
        } catch (error) {
            console.error('Quick create failed:', error);
        }
    };
    
    return (
        <form onSubmit={handleSubmit(onSubmit)}>
            <Grid container spacing={2}>
                <Grid item xs={12}>
                    <Controller
                        name="companyName"
                        control={control}
                        rules={{ required: 'Firmenname ist erforderlich' }}
                        render={({ field, fieldState }) => (
                            <TextField
                                {...field}
                                label="Firmenname"
                                fullWidth
                                error={!!fieldState.error}
                                helperText={fieldState.error?.message}
                                autoFocus
                            />
                        )}
                    />
                </Grid>
                <Grid item xs={6}>
                    <Controller
                        name="contactPerson"
                        control={control}
                        render={({ field }) => (
                            <TextField {...field} label="Ansprechpartner" fullWidth />
                        )}
                    />
                </Grid>
                <Grid item xs={6}>
                    <Controller
                        name="email"
                        control={control}
                        render={({ field }) => (
                            <TextField {...field} label="E-Mail" type="email" fullWidth />
                        )}
                    />
                </Grid>
            </Grid>
            <DialogActions sx={{ mt: 2 }}>
                <Button onClick={onCancel}>Abbrechen</Button>
                <Button type="submit" variant="contained">Erstellen</Button>
            </DialogActions>
        </form>
    );
};
```

### Smart Defaults Logic
```typescript
// Context-aware defaults
const getSmartDefaults = (context: QuickCreateContext) => {
    const defaults: any = {
        assignedTo: context.user?.id,
        createdAt: new Date().toISOString()
    };
    
    // Page-specific defaults
    switch (context.currentPage) {
        case '/cockpit':
            return {
                ...defaults,
                priority: 'high',
                source: 'phone_call'
            };
        case '/calculator':
            return {
                ...defaults,
                priority: 'medium',
                source: 'calculator_session',
                needsQuote: true
            };
        default:
            return defaults;
    }
};
```

---

## 🔗 DEPENDENCIES

- **Benötigt:** M1 Navigation (FAB integration), M4 Pipeline (opportunity creation)
- **Erweitert:** M3 Sales Cockpit (quick actions), FC-008 Security (user context)
- **Integration:** Calculator, Pipeline, Customer Management

---

## 🧪 TESTING

### Quick Create Tests
```typescript
describe('QuickCreateFAB', () => {
    it('should show context-appropriate actions', () => {
        const { rerender } = render(<QuickCreateFAB />, {
            wrapper: ({ children }) => (
                <MemoryRouter initialEntries={['/customers']}>
                    {children}
                </MemoryRouter>
            )
        });
        
        fireEvent.click(screen.getByRole('button'));
        expect(screen.getByText('Neuer Kunde')).toBeInTheDocument();
        expect(screen.queryByText('Neue Opportunity')).not.toBeInTheDocument();
    });
    
    it('should apply smart defaults based on context', () => {
        const context = { currentPage: '/cockpit', user: { id: '123' } };
        const defaults = getSmartDefaults(context);
        
        expect(defaults.priority).toBe('high');
        expect(defaults.assignedTo).toBe('123');
    });
});
```

---

## 📋 QUICK IMPLEMENTATION

### 🕒 15-Min Claude Working Section

**Aufgabe:** Quick Create FAB mit Context-aware Modals implementieren

**Sofort loslegen:**
1. FloatingActionButton in MainLayout integrieren
2. QuickCreateModal mit Action Selection
3. Quick Forms für Customer/Opportunity (minimal fields)
4. Smart Defaults basierend auf Page Context

**Quick-Win Code:**
```typescript
// 1. FAB Integration in MainLayout
export const MainLayoutV2WithFAB: React.FC = ({ children }) => {
    return (
        <Box sx={{ position: 'relative' }}>
            {children}
            <QuickCreateFAB />
        </Box>
    );
};

// 2. Minimal Quick Customer Form
export const QuickCustomerForm: React.FC = ({ onSuccess }) => {
    const [formData, setFormData] = useState({
        companyName: '',
        contactPerson: '',
        email: ''
    });
    
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        const customer = await customerService.quickCreate(formData);
        onSuccess(customer);
    };
    
    return (
        <form onSubmit={handleSubmit}>
            <TextField 
                label="Firmenname" 
                value={formData.companyName}
                onChange={(e) => setFormData({...formData, companyName: e.target.value})}
                fullWidth 
                autoFocus 
                required 
            />
            {/* Weitere minimal fields */}
        </form>
    );
};
```

**Nächste Schritte:**
- Phase 1: FAB Integration und Modal Framework (1h)
- Phase 2: Quick Forms mit Validation (2h)
- Phase 3: Smart Defaults und Context Detection (1h)
- Phase 4: Success Actions und Navigation (1h)

**Erfolgs-Kriterien:**
- ✅ FAB von jeder Seite erreichbar
- ✅ Context-aware Action Selection
- ✅ Customer/Opportunity in <30 Sekunden erstellbar
- ✅ Smart Defaults basierend auf Page Context

---

**🔗 DETAIL-DOCS:**
- [TECH_CONCEPT](/docs/features/ACTIVE/05_ui_foundation/M2_QUICK_CREATE_KOMPAKT.md) - Bestehende Dokumentation
- [IMPLEMENTATION_GUIDE](/docs/features/ACTIVE/05_ui_foundation/M2_IMPLEMENTATION_GUIDE.md) - Detaillierte Umsetzungsanleitung

**🎯 Nächster Schritt:** FAB in MainLayoutV2 integrieren und Modal Framework aufbauen