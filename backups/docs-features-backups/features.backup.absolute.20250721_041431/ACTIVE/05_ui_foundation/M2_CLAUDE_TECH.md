# M2 Quick Create Actions - CLAUDE TECH 🤖

## 🧠 QUICK-LOAD (30 Sekunden bis zur Produktivität)

**Feature:** Floating Action Button + Context-aware Quick Create Modals (<30s Erstellung)  
**Stack:** React + MUI FAB + React Hook Form + Context Detection  
**Status:** 📋 Ready to Start (nach M4 Pipeline)  
**Dependencies:** M1 Navigation (FAB placement), M4 Pipeline (opportunity creation) | Erweitert: M3 Cockpit, Calculator  

**Jump to:** [📚 Recipes](#-implementation-recipes) | [🧪 Tests](#-test-patterns) | [🔌 Integration](#-integration-cookbook) | [⚡ Speed Patterns](#-speed-patterns)

**Core Purpose in 1 Line:** `Current Context → Smart Defaults → Quick Form → <30s Creation → Direct Navigation`

---

## 🍳 IMPLEMENTATION RECIPES

### Recipe 1: FAB + Quick Create in 5 Minuten
```typescript
// 1. Floating Action Button with Speed Dial (copy-paste ready)
export const QuickCreateFAB: React.FC = () => {
    const [open, setOpen] = useState(false);
    const location = useLocation();
    const navigate = useNavigate();
    const { hasPermission } = usePermissions();
    
    // Context-aware actions
    const actions = [
        { 
            icon: <PersonAddIcon />, 
            name: 'Neuer Kunde',
            permission: 'customers:create',
            action: () => openQuickCreate('customer')
        },
        { 
            icon: <TrendingUpIcon />, 
            name: 'Neue Opportunity',
            permission: 'opportunities:create',
            action: () => openQuickCreate('opportunity')
        },
        { 
            icon: <EventIcon />, 
            name: 'Neuer Termin',
            permission: 'events:create',
            action: () => openQuickCreate('event')
        }
    ].filter(action => hasPermission(action.permission));
    
    const openQuickCreate = (type: string) => {
        setOpen(false);
        // Open modal with smart defaults based on current page
        const context = getContextFromLocation(location.pathname);
        openQuickCreateModal(type, context);
    };
    
    return (
        <>
            <SpeedDial
                ariaLabel="Quick Create"
                sx={{ position: 'fixed', bottom: 16, right: 16 }}
                icon={<SpeedDialIcon />}
                onClose={() => setOpen(false)}
                onOpen={() => setOpen(true)}
                open={open}
            >
                {actions.map((action) => (
                    <SpeedDialAction
                        key={action.name}
                        icon={action.icon}
                        tooltipTitle={action.name}
                        onClick={action.action}
                    />
                ))}
            </SpeedDial>
        </>
    );
};

// 2. Smart Context Detection
const getContextFromLocation = (pathname: string): QuickCreateContext => {
    const context: QuickCreateContext = {
        source: pathname,
        timestamp: new Date().toISOString()
    };
    
    // Extract context from URL
    const pathSegments = pathname.split('/').filter(Boolean);
    
    if (pathSegments[0] === 'customers' && pathSegments[1]) {
        context.customerId = pathSegments[1];
    }
    
    if (pathname.includes('calculator')) {
        context.source = 'calculator';
        context.needsQuote = true;
    }
    
    if (pathname === '/cockpit') {
        context.priority = 'high';
        context.assignToMe = true;
    }
    
    return context;
};

// 3. Quick Create Modal Manager
export const QuickCreateModalManager: React.FC = () => {
    const [modal, setModal] = useState<QuickCreateModal | null>(null);
    
    // Global event listener for quick create
    useEffect(() => {
        const handleQuickCreate = (event: CustomEvent) => {
            setModal({
                type: event.detail.type,
                context: event.detail.context,
                open: true
            });
        };
        
        window.addEventListener('quickcreate', handleQuickCreate);
        return () => window.removeEventListener('quickcreate', handleQuickCreate);
    }, []);
    
    if (!modal) return null;
    
    return (
        <Dialog 
            open={modal.open} 
            onClose={() => setModal(null)}
            maxWidth="sm"
            fullWidth
        >
            <DialogTitle>
                Schnell erstellen: {getTypeLabel(modal.type)}
            </DialogTitle>
            <DialogContent>
                <QuickCreateForm 
                    type={modal.type}
                    context={modal.context}
                    onSuccess={(created) => {
                        setModal(null);
                        handleQuickCreateSuccess(created);
                    }}
                    onCancel={() => setModal(null)}
                />
            </DialogContent>
        </Dialog>
    );
};
```

### Recipe 2: Ultra-Fast Customer Creation Form
```typescript
// Minimal fields for <30s creation
export const QuickCustomerForm: React.FC<QuickCreateProps> = ({ context, onSuccess, onCancel }) => {
    const { register, handleSubmit, formState: { errors }, setValue } = useForm<QuickCustomerData>({
        defaultValues: getSmartDefaults('customer', context)
    });
    
    const [loading, setLoading] = useState(false);
    const queryClient = useQueryClient();
    
    // Auto-complete from existing data
    const { data: suggestions } = useQuery({
        queryKey: ['customer-suggestions'],
        queryFn: () => customerApi.getSuggestions(),
        staleTime: 5 * 60 * 1000
    });
    
    const onSubmit = async (data: QuickCustomerData) => {
        setLoading(true);
        try {
            const customer = await customerApi.quickCreate({
                ...data,
                source: context.source,
                createdVia: 'quick-create'
            });
            
            // Optimistic update
            queryClient.setQueryData(['customers'], (old: any) => [...(old || []), customer]);
            
            onSuccess(customer);
            toast.success(`Kunde "${customer.companyName}" erstellt!`);
        } catch (error) {
            toast.error('Fehler beim Erstellen');
        } finally {
            setLoading(false);
        }
    };
    
    return (
        <form onSubmit={handleSubmit(onSubmit)}>
            <Stack spacing={2} sx={{ mt: 2 }}>
                <TextField
                    {...register('companyName', { 
                        required: 'Firmenname erforderlich',
                        minLength: { value: 2, message: 'Mindestens 2 Zeichen' }
                    })}
                    label="Firmenname"
                    autoFocus
                    error={!!errors.companyName}
                    helperText={errors.companyName?.message}
                    InputProps={{
                        endAdornment: (
                            <InputAdornment position="end">
                                <BusinessIcon />
                            </InputAdornment>
                        )
                    }}
                />
                
                <Grid container spacing={2}>
                    <Grid item xs={6}>
                        <TextField
                            {...register('contactFirstName')}
                            label="Vorname"
                            placeholder="Max"
                        />
                    </Grid>
                    <Grid item xs={6}>
                        <TextField
                            {...register('contactLastName')}
                            label="Nachname"
                            placeholder="Mustermann"
                        />
                    </Grid>
                </Grid>
                
                <TextField
                    {...register('email', {
                        pattern: {
                            value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                            message: 'Ungültige E-Mail'
                        }
                    })}
                    label="E-Mail"
                    type="email"
                    error={!!errors.email}
                    helperText={errors.email?.message}
                />
                
                <TextField
                    {...register('phone')}
                    label="Telefon"
                    placeholder="+49 123 456789"
                />
                
                {/* Smart suggestions */}
                {suggestions && (
                    <Alert severity="info">
                        💡 Ähnliche Kunden: {suggestions.map(s => s.name).join(', ')}
                    </Alert>
                )}
            </Stack>
            
            <DialogActions sx={{ mt: 3 }}>
                <Button onClick={onCancel}>Abbrechen</Button>
                <LoadingButton
                    type="submit"
                    variant="contained"
                    loading={loading}
                    loadingPosition="start"
                    startIcon={<SaveIcon />}
                >
                    Kunde erstellen
                </LoadingButton>
            </DialogActions>
        </form>
    );
};

// Smart defaults based on context
const getSmartDefaults = (type: string, context: QuickCreateContext): any => {
    const defaults: any = {
        createdAt: new Date().toISOString(),
        createdBy: context.userId
    };
    
    switch (type) {
        case 'customer':
            if (context.source === 'calculator') {
                defaults.leadSource = 'calculator';
                defaults.needsQuote = true;
            }
            break;
            
        case 'opportunity':
            defaults.customerId = context.customerId;
            defaults.stage = 'qualification';
            if (context.source === '/cockpit') {
                defaults.priority = 'high';
                defaults.assignedTo = context.userId;
            }
            break;
    }
    
    return defaults;
};
```

### Recipe 3: Keyboard Shortcuts Integration
```typescript
// Global keyboard shortcuts for power users
export const QuickCreateKeyboardShortcuts: React.FC = () => {
    const openQuickCreate = useQuickCreate();
    
    useEffect(() => {
        const handleKeyboard = (e: KeyboardEvent) => {
            // Ctrl/Cmd + K = Quick Create
            if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
                e.preventDefault();
                openQuickCreate('menu');
            }
            
            // Ctrl/Cmd + Shift + C = New Customer
            if ((e.ctrlKey || e.metaKey) && e.shiftKey && e.key === 'C') {
                e.preventDefault();
                openQuickCreate('customer');
            }
            
            // Ctrl/Cmd + Shift + O = New Opportunity
            if ((e.ctrlKey || e.metaKey) && e.shiftKey && e.key === 'O') {
                e.preventDefault();
                openQuickCreate('opportunity');
            }
        };
        
        window.addEventListener('keydown', handleKeyboard);
        return () => window.removeEventListener('keydown', handleKeyboard);
    }, []);
    
    return null; // No UI, just keyboard listener
};
```

---

## 🧪 TEST PATTERNS

### Pattern 1: Context Detection Test
```typescript
describe('Quick Create Context', () => {
    it('should extract customer context from URL', () => {
        const context = getContextFromLocation('/customers/123/details');
        
        expect(context.customerId).toBe('123');
        expect(context.source).toBe('/customers/123/details');
    });
    
    it('should set high priority for cockpit', () => {
        const context = getContextFromLocation('/cockpit');
        
        expect(context.priority).toBe('high');
        expect(context.assignToMe).toBe(true);
    });
    
    it('should detect calculator context', () => {
        const context = getContextFromLocation('/calculator/session/456');
        
        expect(context.source).toBe('calculator');
        expect(context.needsQuote).toBe(true);
    });
});
```

### Pattern 2: Speed Test (<30s creation)
```typescript
describe('Quick Create Performance', () => {
    it('should create customer in less than 30 seconds', async () => {
        const startTime = Date.now();
        
        render(<QuickCustomerForm context={{}} onSuccess={jest.fn()} />);
        
        // Fill minimal required fields
        fireEvent.change(screen.getByLabelText('Firmenname'), {
            target: { value: 'Test GmbH' }
        });
        
        // Submit
        fireEvent.click(screen.getByText('Kunde erstellen'));
        
        // Wait for success
        await waitFor(() => {
            expect(screen.queryByText('Kunde erstellen')).not.toBeDisabled();
        });
        
        const endTime = Date.now();
        expect(endTime - startTime).toBeLessThan(30000); // 30 seconds
    });
});
```

---

## 🔌 INTEGRATION COOKBOOK

### Mit Calculator (M8)
```typescript
// Auto-create customer after calculation
export const CalculatorWithQuickCreate: React.FC = () => {
    const { openQuickCreate } = useQuickCreate();
    
    const handleCalculationComplete = (result: CalculationResult) => {
        // Offer to create customer
        if (!result.customerId) {
            openQuickCreate('customer', {
                source: 'calculator',
                calculationId: result.id,
                companyName: result.projectName,
                needsQuote: true,
                quoteAmount: result.totalAmount
            });
        }
    };
    
    return <Calculator onComplete={handleCalculationComplete} />;
};
```

### Mit Sales Cockpit (M3)
```typescript
// Quick actions in cockpit
export const CockpitQuickActions: React.FC = () => {
    const { selectedCustomer } = useCockpitContext();
    const { openQuickCreate } = useQuickCreate();
    
    return (
        <SpeedDial
            ariaLabel="Cockpit Actions"
            sx={{ position: 'absolute', bottom: 16, right: 16 }}
            icon={<AddIcon />}
        >
            <SpeedDialAction
                icon={<EventIcon />}
                tooltipTitle="Neuer Termin"
                onClick={() => openQuickCreate('event', {
                    customerId: selectedCustomer?.id,
                    priority: 'high'
                })}
            />
            <SpeedDialAction
                icon={<NoteAddIcon />}
                tooltipTitle="Neue Notiz"
                onClick={() => openQuickCreate('note', {
                    customerId: selectedCustomer?.id
                })}
            />
        </SpeedDial>
    );
};
```

---

## ⚡ SPEED PATTERNS

### Optimistic Updates
```typescript
// Immediate UI feedback
const createCustomerOptimistic = useMutation({
    mutationFn: customerApi.quickCreate,
    onMutate: async (newCustomer) => {
        // Cancel outgoing refetches
        await queryClient.cancelQueries(['customers']);
        
        // Snapshot previous value
        const previousCustomers = queryClient.getQueryData(['customers']);
        
        // Optimistically update
        queryClient.setQueryData(['customers'], (old: any) => [
            ...old,
            { ...newCustomer, id: 'temp-' + Date.now(), isOptimistic: true }
        ]);
        
        return { previousCustomers };
    },
    onError: (err, newCustomer, context) => {
        // Rollback on error
        queryClient.setQueryData(['customers'], context.previousCustomers);
    },
    onSettled: () => {
        queryClient.invalidateQueries(['customers']);
    }
});
```

### Auto-Save Draft
```typescript
// Save draft while typing
const useQuickCreateDraft = (type: string) => {
    const [draft, setDraft] = useState(() => {
        const saved = localStorage.getItem(`quick-create-draft-${type}`);
        return saved ? JSON.parse(saved) : {};
    });
    
    const updateDraft = useDebouncedCallback((data: any) => {
        setDraft(data);
        localStorage.setItem(`quick-create-draft-${type}`, JSON.stringify(data));
    }, 500);
    
    const clearDraft = () => {
        localStorage.removeItem(`quick-create-draft-${type}`);
        setDraft({});
    };
    
    return { draft, updateDraft, clearDraft };
};
```

---

## 📚 DEEP KNOWLEDGE (Bei Bedarf expandieren)

<details>
<summary>🎨 Advanced UI Patterns</summary>

### Micro-Interactions
```typescript
// Smooth animations
const fabVariants = {
    initial: { scale: 0, rotate: -180 },
    animate: { 
        scale: 1, 
        rotate: 0,
        transition: { type: "spring", stiffness: 200 }
    },
    tap: { scale: 0.9 },
    hover: { scale: 1.1 }
};

// Haptic feedback on mobile
const triggerHaptic = () => {
    if ('vibrate' in navigator) {
        navigator.vibrate(10);
    }
};
```

### Accessibility
```typescript
// Full keyboard navigation
const QuickCreateAccessible: React.FC = () => {
    const trapFocus = useFocusTrap();
    
    return (
        <div ref={trapFocus} role="dialog" aria-modal="true">
            <h2 id="quick-create-title">Schnell erstellen</h2>
            <div role="radiogroup" aria-labelledby="quick-create-title">
                <label>
                    <input type="radio" name="type" value="customer" />
                    Neuer Kunde (Alt+K)
                </label>
                <label>
                    <input type="radio" name="type" value="opportunity" />
                    Neue Opportunity (Alt+O)
                </label>
            </div>
        </div>
    );
};
```

</details>

<details>
<summary>⚡ Performance Optimization</summary>

### Code Splitting
```typescript
// Lazy load forms
const QuickCustomerForm = lazy(() => 
    import('./forms/QuickCustomerForm')
);

const QuickOpportunityForm = lazy(() => 
    import('./forms/QuickOpportunityForm')
);

// Preload on hover
const preloadForm = (type: string) => {
    switch (type) {
        case 'customer':
            import('./forms/QuickCustomerForm');
            break;
        case 'opportunity':
            import('./forms/QuickOpportunityForm');
            break;
    }
};
```

### Service Worker Cache
```javascript
// Cache API responses
self.addEventListener('fetch', (event) => {
    if (event.request.url.includes('/api/quick-create/defaults')) {
        event.respondWith(
            caches.match(event.request).then((response) => {
                return response || fetch(event.request).then((response) => {
                    return caches.open('quick-create-v1').then((cache) => {
                        cache.put(event.request, response.clone());
                        return response;
                    });
                });
            })
        );
    }
});
```

</details>

---

**🎯 Nächster Schritt:** FAB mit Speed Dial in MainLayout integrieren