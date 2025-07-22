# M2 Quick Create Actions - Tech Concept

**Feature-Code:** M2  
**Feature-Name:** Quick Create Actions  
**Feature-Typ:** 🎨 FRONTEND ENHANCEMENT  
**Erstellt:** 2025-07-20  
**Status:** Tech Concept (Enhancement-Mode)  
**Priorität:** HIGH - UI Foundation  
**Geschätzter Aufwand:** 2-3 Tage (Enhancement)  

---

## 🧠 CLAUDE WORKING SECTION (15-Min Context Chunk)

### 📍 Kontext laden für produktive Arbeit

**Was ist M2?** Quick Create Actions - Floating Action Button System für schnelle Erstellung von Kunden, Opportunities und Berechnungen

**Warum Enhancement statt Neuentwicklung?** Basis-Navigation existiert bereits (MainLayoutV2.tsx) - wir fügen intelligent ein Smart Action System hinzu

**Aktueller Zustand:**
- ✅ **Basis-Layout:** MainLayoutV2.tsx mit Navigation verfügbar
- ✅ **Routing System:** React Router funktioniert für /customers/new, /opportunities/new
- ❌ **Quick Actions:** Kein Floating Action Button oder Quick-Create System
- ❌ **Context-Awareness:** Keine pagebasierte Action-Vorschläge
- ❌ **Keyboard Shortcuts:** Keine Hotkeys für häufige Aktionen

**Abhängigkeiten verstehen:**
- **Benötigt:** M1 Navigation System (✅ VERFÜGBAR) - Basis-Layout für Actions
- **Erweitert:** FC-009 Permissions (✅ VERFÜGBAR) - Permission-aware Quick Actions
- **Integriert mit:** M4 Pipeline, M8 Calculator - Quick Create Targets

**Technischer Kern - Context-Aware Quick Actions:**
```typescript
export const QuickCreateActions: React.FC = () => {
    const location = useLocation();
    const { hasPermission } = usePermissions();
    
    const getContextualActions = () => {
        const path = location.pathname;
        
        if (path.startsWith('/customers')) {
            return [
                {
                    label: 'Neuer Kunde',
                    icon: <PersonAddIcon />,
                    permission: 'customers:write',
                    action: () => navigate('/customers/new'),
                    hotkey: 'Ctrl+Shift+C'
                },
                {
                    label: 'Kunde importieren',
                    icon: <UploadIcon />,
                    permission: 'customers:import',
                    action: () => navigate('/customers/import'),
                    hotkey: 'Ctrl+Shift+I'
                }
            ];
        }
        
        if (path.startsWith('/opportunities')) {
            return [
                {
                    label: 'Neue Opportunity',
                    icon: <BusinessIcon />,
                    permission: 'opportunities:write',
                    action: () => navigate('/opportunities/new'),
                    hotkey: 'Ctrl+Shift+O'
                },
                {
                    label: 'Kalkulation erstellen',
                    icon: <CalculateIcon />,
                    permission: 'calculator:use',
                    action: () => openCalculatorModal(),
                    hotkey: 'Ctrl+Shift+K'
                }
            ];
        }
        
        // Global Actions - immer verfügbar
        return [
            {
                label: 'Schnellsuche',
                icon: <SearchIcon />,
                permission: 'search:global',
                action: () => openGlobalSearch(),
                hotkey: 'Ctrl+K'
            }
        ];
    };
    
    return (
        <SpeedDial
            ariaLabel="Quick Create Actions"
            sx={{ position: 'fixed', bottom: 24, right: 24 }}
            icon={<AddIcon />}
            direction="up"
        >
            {getContextualActions().map((action) => (
                <PermissionGate key={action.label} permission={action.permission}>
                    <SpeedDialAction
                        icon={action.icon}
                        tooltipTitle={`${action.label} (${action.hotkey})`}
                        onClick={action.action}
                    />
                </PermissionGate>
            ))}
        </SpeedDial>
    );
};
```

---

## 🎯 ÜBERSICHT

### Business Value
**Problem:** User müssen durch mehrere Navigation-Ebenen klicken für häufige Aktionen - ineffizient und frustrierend

**Lösung:** Context-aware Quick Actions mit intelligenten Shortcuts und Hotkeys für 80% der täglichen Aktionen

**ROI:** 
- **Kosten:** 2-3 Entwicklertage (~€3.000) - Enhancement statt Neuentwicklung
- **Time Saving:** 40% weniger Klicks für häufige Aktionen (5-10 Sekunden pro Aktion)
- **User Productivity:** +25% Effizienz bei täglichen Sales-Tasks
- **ROI-Ratio:** 8:1 (Break-even nach 2 Wochen)

### Kernfunktionen (Enhancement)
1. **Floating Speed Dial** - Material-UI SpeedDial für Quick Actions
2. **Context-Aware Actions** - Intelligente Aktionen basierend auf aktueller Seite
3. **Permission-Based Visibility** - Nur erlaubte Aktionen anzeigen
4. **Keyboard Shortcuts** - Hotkeys für Power-User (Ctrl+Shift+C, Ctrl+K, etc.)
5. **Quick Search** - Global Search mit Ctrl+K Shortcut
6. **Calculator Integration** - Direkte Kalkulation von jeder Seite

---

## 🏗️ ARCHITEKTUR

### Enhancement Strategy - Smart Addition to Existing Layout
```
BESTEHEND (M1 Navigation):        →    ENHANCED (M1 + M2):
MainLayoutV2.tsx                       MainLayoutV2.tsx
├── Header                              ├── Enhanced Header + Search
├── Sidebar                             ├── Sidebar
├── Content                             ├── Content
└── Basic Routing                       ├── QuickCreateActions (NEW)
                                        ├── GlobalSearchModal (NEW)
                                        └── HotkeyManager (NEW)
```

### Component Architecture - Quick Actions System
```typescript
// Enhanced Main Layout (builds on M1 Navigation)
export const MainLayoutV2Enhanced: React.FC<MainLayoutProps> = ({ children }) => {
    const [globalSearchOpen, setGlobalSearchOpen] = useState(false);
    const [calculatorOpen, setCalculatorOpen] = useState(false);
    
    useHotkeys('ctrl+k', () => setGlobalSearchOpen(true));
    useHotkeys('ctrl+shift+c', () => navigate('/customers/new'));
    useHotkeys('ctrl+shift+o', () => navigate('/opportunities/new'));
    useHotkeys('ctrl+shift+k', () => setCalculatorOpen(true));
    
    return (
        <Box sx={{ display: 'flex', minHeight: '100vh' }}>
            {/* Existing M1 Layout Components */}
            <M1NavigationComponents />
            
            {/* Enhanced Main Content */}
            <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
                {children}
                
                {/* M2 Quick Create Enhancements */}
                <QuickCreateActions />
                <GlobalSearchModal 
                    open={globalSearchOpen} 
                    onClose={() => setGlobalSearchOpen(false)} 
                />
                <CalculatorModal 
                    open={calculatorOpen} 
                    onClose={() => setCalculatorOpen(false)} 
                />
            </Box>
        </Box>
    );
};

// Context-Aware Quick Actions
interface QuickAction {
    label: string;
    icon: React.ReactElement;
    permission: string;
    action: () => void;
    hotkey: string;
    category: 'create' | 'search' | 'calculate' | 'import';
}

export const QuickCreateActions: React.FC = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const { hasPermission } = usePermissions();
    const { openCalculator } = useCalculator();
    const { openGlobalSearch } = useGlobalSearch();
    
    const getPageActions = (): QuickAction[] => {
        const path = location.pathname;
        
        // Customer-spezifische Actions
        if (path.startsWith('/customers')) {
            return [
                {
                    label: 'Neuer Kunde',
                    icon: <PersonAddIcon />,
                    permission: 'customers:write',
                    action: () => navigate('/customers/new'),
                    hotkey: 'Ctrl+Shift+C',
                    category: 'create'
                },
                {
                    label: 'CSV Import',
                    icon: <UploadIcon />,
                    permission: 'customers:import',
                    action: () => navigate('/customers/import'),
                    hotkey: 'Ctrl+Shift+I',
                    category: 'import'
                },
                {
                    label: 'Kunden exportieren',
                    icon: <DownloadIcon />,
                    permission: 'customers:export',
                    action: () => exportCustomers(),
                    hotkey: 'Ctrl+Shift+E',
                    category: 'export'
                }
            ];
        }
        
        // Opportunity-spezifische Actions
        if (path.startsWith('/opportunities')) {
            return [
                {
                    label: 'Neue Opportunity',
                    icon: <BusinessIcon />,
                    permission: 'opportunities:write',
                    action: () => navigate('/opportunities/new'),
                    hotkey: 'Ctrl+Shift+O',
                    category: 'create'
                },
                {
                    label: 'Angebot kalkulieren',
                    icon: <CalculateIcon />,
                    permission: 'calculator:use',
                    action: () => openCalculator(),
                    hotkey: 'Ctrl+Shift+K',
                    category: 'calculate'
                },
                {
                    label: 'Pipeline Report',
                    icon: <AssessmentIcon />,
                    permission: 'reports:read',
                    action: () => navigate('/reports/pipeline'),
                    hotkey: 'Ctrl+Shift+R',
                    category: 'report'
                }
            ];
        }
        
        // Dashboard-spezifische Actions
        if (path.startsWith('/cockpit') || path === '/') {
            return [
                {
                    label: 'Schnell-Opportunity',
                    icon: <BusinessIcon />,
                    permission: 'opportunities:write',
                    action: () => navigate('/opportunities/new?from=cockpit'),
                    hotkey: 'Ctrl+Shift+O',
                    category: 'create'
                },
                {
                    label: 'Schnell-Kunde',
                    icon: <PersonAddIcon />,
                    permission: 'customers:write',
                    action: () => navigate('/customers/new?from=cockpit'),
                    hotkey: 'Ctrl+Shift+C',
                    category: 'create'
                },
                {
                    label: 'Tages-Report',
                    icon: <TodayIcon />,
                    permission: 'reports:read',
                    action: () => navigate('/reports/daily'),
                    hotkey: 'Ctrl+Shift+T',
                    category: 'report'
                }
            ];
        }
        
        // Global Actions - immer verfügbar
        return [
            {
                label: 'Globale Suche',
                icon: <SearchIcon />,
                permission: 'search:global',
                action: () => openGlobalSearch(),
                hotkey: 'Ctrl+K',
                category: 'search'
            }
        ];
    };
    
    const actions = getPageActions().filter(action => hasPermission(action.permission));
    
    if (actions.length === 0) return null;
    
    return (
        <SpeedDial
            ariaLabel="Quick Create Actions"
            sx={{
                position: 'fixed',
                bottom: 24,
                right: 24,
                zIndex: 1300,
                '& .MuiFab-primary': {
                    background: 'linear-gradient(135deg, #94C456 0%, #004F7B 100%)', // Freshfoodz CI
                    '&:hover': {
                        background: 'linear-gradient(135deg, #7FB03D 0%, #003A5C 100%)',
                    }
                }
            }}
            icon={<SpeedDialIcon />}
            direction="up"
            FabProps={{
                size: 'large',
                color: 'primary'
            }}
        >
            {actions.map((action) => (
                <SpeedDialAction
                    key={action.label}
                    icon={action.icon}
                    tooltipTitle={`${action.label} (${action.hotkey})`}
                    onClick={action.action}
                    FabProps={{
                        sx: {
                            bgcolor: getCategoryColor(action.category),
                            '&:hover': {
                                bgcolor: getCategoryColorHover(action.category),
                            }
                        }
                    }}
                />
            ))}
        </SpeedDial>
    );
};

// Global Search Modal
interface GlobalSearchModalProps {
    open: boolean;
    onClose: () => void;
}

export const GlobalSearchModal: React.FC<GlobalSearchModalProps> = ({ open, onClose }) => {
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState<SearchResult[]>([]);
    const [isSearching, setIsSearching] = useState(false);
    const { hasPermission } = usePermissions();
    const navigate = useNavigate();
    
    const debouncedSearch = useDebounce(searchQuery, 300);
    
    useEffect(() => {
        if (debouncedSearch.length >= 2) {
            performGlobalSearch(debouncedSearch);
        } else {
            setSearchResults([]);
        }
    }, [debouncedSearch]);
    
    const performGlobalSearch = async (query: string) => {
        if (!hasPermission('search:global')) return;
        
        setIsSearching(true);
        try {
            const response = await apiClient.get(`/api/search/global?q=${encodeURIComponent(query)}`);
            setSearchResults(response.data.results);
        } catch (error) {
            console.error('Global search failed:', error);
        } finally {
            setIsSearching(false);
        }
    };
    
    const handleResultClick = (result: SearchResult) => {
        navigate(result.url);
        onClose();
        setSearchQuery('');
    };
    
    return (
        <Dialog
            open={open}
            onClose={onClose}
            maxWidth="md"
            fullWidth
            PaperProps={{
                sx: {
                    position: 'fixed',
                    top: '10%',
                    m: 0,
                    borderRadius: 2,
                    minHeight: '60vh'
                }
            }}
        >
            <DialogTitle sx={{ pb: 1 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <SearchIcon color="primary" />
                    <Typography variant="h6">Globale Suche</Typography>
                    <Chip label="Ctrl+K" size="small" variant="outlined" sx={{ ml: 'auto' }} />
                </Box>
            </DialogTitle>
            
            <DialogContent sx={{ pt: 1 }}>
                <TextField
                    autoFocus
                    fullWidth
                    placeholder="Kunden, Opportunities, Produkte..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    InputProps={{
                        startAdornment: (
                            <InputAdornment position="start">
                                {isSearching ? <CircularProgress size={20} /> : <SearchIcon />}
                            </InputAdornment>
                        ),
                        sx: { fontSize: '1.1rem' }
                    }}
                    sx={{ mb: 2 }}
                />
                
                {searchResults.length > 0 && (
                    <Box>
                        <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 1 }}>
                            {searchResults.length} Ergebnisse gefunden
                        </Typography>
                        
                        <List>
                            {searchResults.map((result) => (
                                <ListItem
                                    key={result.id}
                                    button
                                    onClick={() => handleResultClick(result)}
                                    sx={{
                                        borderRadius: 1,
                                        mb: 0.5,
                                        '&:hover': { bgcolor: 'action.hover' }
                                    }}
                                >
                                    <ListItemIcon>
                                        {getResultIcon(result.type)}
                                    </ListItemIcon>
                                    <ListItemText
                                        primary={result.title}
                                        secondary={result.description}
                                        primaryTypographyProps={{ fontWeight: 'medium' }}
                                    />
                                    <Chip 
                                        label={result.type} 
                                        size="small" 
                                        color="primary" 
                                        variant="outlined" 
                                    />
                                </ListItem>
                            ))}
                        </List>
                    </Box>
                )}
                
                {searchQuery.length >= 2 && searchResults.length === 0 && !isSearching && (
                    <Box sx={{ textAlign: 'center', py: 4 }}>
                        <SearchOffIcon sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
                        <Typography variant="h6" color="text.secondary">
                            Keine Ergebnisse gefunden
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            Versuche andere Suchbegriffe
                        </Typography>
                    </Box>
                )}
                
                {searchQuery.length < 2 && (
                    <Box sx={{ textAlign: 'center', py: 4 }}>
                        <Typography variant="body1" color="text.secondary">
                            Gib mindestens 2 Zeichen ein um zu suchen
                        </Typography>
                        
                        <Box sx={{ mt: 3 }}>
                            <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 1 }}>
                                Schnell-Aktionen:
                            </Typography>
                            <Box sx={{ display: 'flex', gap: 1, justifyContent: 'center', flexWrap: 'wrap' }}>
                                <Chip 
                                    label="Neuer Kunde (Ctrl+Shift+C)" 
                                    clickable 
                                    onClick={() => { navigate('/customers/new'); onClose(); }}
                                />
                                <Chip 
                                    label="Neue Opportunity (Ctrl+Shift+O)" 
                                    clickable 
                                    onClick={() => { navigate('/opportunities/new'); onClose(); }}
                                />
                                <Chip 
                                    label="Kalkulation (Ctrl+Shift+K)" 
                                    clickable 
                                    onClick={() => { /* Open Calculator */ onClose(); }}
                                />
                            </Box>
                        </Box>
                    </Box>
                )}
            </DialogContent>
        </Dialog>
    );
};

// Hotkey Manager
export const HotkeyManager: React.FC = () => {
    const navigate = useNavigate();
    const { openGlobalSearch } = useGlobalSearch();
    const { openCalculator } = useCalculator();
    const { hasPermission } = usePermissions();
    
    // Global Hotkeys
    useHotkeys('ctrl+k', (e) => {
        e.preventDefault();
        if (hasPermission('search:global')) {
            openGlobalSearch();
        }
    });
    
    useHotkeys('ctrl+shift+c', (e) => {
        e.preventDefault();
        if (hasPermission('customers:write')) {
            navigate('/customers/new');
        }
    });
    
    useHotkeys('ctrl+shift+o', (e) => {
        e.preventDefault();
        if (hasPermission('opportunities:write')) {
            navigate('/opportunities/new');
        }
    });
    
    useHotkeys('ctrl+shift+k', (e) => {
        e.preventDefault();
        if (hasPermission('calculator:use')) {
            openCalculator();
        }
    });
    
    useHotkeys('ctrl+shift+h', (e) => {
        e.preventDefault();
        navigate('/cockpit'); // Home/Dashboard
    });
    
    useHotkeys('ctrl+shift+s', (e) => {
        e.preventDefault();
        navigate('/settings');
    });
    
    return null; // Invisible component for hotkey management
};

// Utility Functions
const getCategoryColor = (category: string): string => {
    const colors = {
        create: '#4CAF50',    // Green
        search: '#2196F3',    // Blue  
        calculate: '#FF9800', // Orange
        import: '#9C27B0',    // Purple
        export: '#607D8B',    // Blue Grey
        report: '#F44336'     // Red
    };
    return colors[category] || '#94C456'; // Freshfoodz Green as default
};

const getCategoryColorHover = (category: string): string => {
    const colors = {
        create: '#45A049',
        search: '#1976D2',
        calculate: '#F57C00',
        import: '#7B1FA2',
        export: '#455A64',
        report: '#D32F2F'
    };
    return colors[category] || '#7FB03D';
};

const getResultIcon = (type: string): React.ReactElement => {
    const icons = {
        customer: <PersonIcon />,
        opportunity: <BusinessIcon />,
        product: <InventoryIcon />,
        calculation: <CalculateIcon />,
        report: <AssessmentIcon />
    };
    return icons[type] || <SearchIcon />;
};
```

---

## 🔄 ABHÄNGIGKEITEN

### Benötigt diese Features:
- **M1 Navigation System** - Basis-Layout für Quick Actions Integration ✅ VERFÜGBAR
- **FC-009 Advanced Permissions** - Permission-basierte Action-Sichtbarkeit ✅ VERFÜGBAR
- **React Router** - Navigation für Quick Actions ✅ VORHANDEN

### Ermöglicht diese Features:
- **M8 Calculator Modal** - Quick Calculator Access über Hotkey
- **Global Search** - Feature-übergreifende Suchfunktionalität  
- **M4 Pipeline** - Quick Opportunity Creation
- **Alle Create-Actions** - Schneller Zugang zu Erstellungsflows

### Integriert mit:
- **Material-UI SpeedDial** - Native Component für Floating Actions
- **react-hotkeys-hook** - Keyboard Shortcut Management
- **Alle Frontend-Features** - Universal Quick Access Layer

---

## 🧪 TESTING-STRATEGIE

### Component Tests - Quick Actions
```typescript
describe('QuickCreateActions', () => {
    it('should show context-specific actions based on current page', () => {
        const mockLocation = { pathname: '/customers' };
        jest.mocked(useLocation).mockReturnValue(mockLocation);
        
        const mockPermissions = {
            hasPermission: jest.fn((permission) => 
                permission === 'customers:write' || permission === 'customers:import'
            )
        };
        jest.mocked(usePermissions).mockReturnValue(mockPermissions);
        
        render(<QuickCreateActions />);
        
        // SpeedDial should be present
        expect(screen.getByLabelText('Quick Create Actions')).toBeInTheDocument();
        
        // Click to open actions
        fireEvent.click(screen.getByLabelText('Quick Create Actions'));
        
        // Customer-specific actions should be visible
        expect(screen.getByLabelText('Neuer Kunde (Ctrl+Shift+C)')).toBeInTheDocument();
        expect(screen.getByLabelText('CSV Import (Ctrl+Shift+I)')).toBeInTheDocument();
    });
    
    it('should respect user permissions for action visibility', () => {
        const mockPermissions = {
            hasPermission: jest.fn((permission) => permission === 'customers:read') // Only read permission
        };
        jest.mocked(usePermissions).mockReturnValue(mockPermissions);
        
        render(<QuickCreateActions />);
        
        fireEvent.click(screen.getByLabelText('Quick Create Actions'));
        
        // Write actions should not be visible
        expect(screen.queryByLabelText(/Neuer Kunde/)).not.toBeInTheDocument();
        expect(screen.queryByLabelText(/CSV Import/)).not.toBeInTheDocument();
    });
    
    it('should handle action clicks correctly', () => {
        const mockNavigate = jest.fn();
        jest.mocked(useNavigate).mockReturnValue(mockNavigate);
        
        render(<QuickCreateActions />);
        
        fireEvent.click(screen.getByLabelText('Quick Create Actions'));
        fireEvent.click(screen.getByLabelText('Neuer Kunde (Ctrl+Shift+C)'));
        
        expect(mockNavigate).toHaveBeenCalledWith('/customers/new');
    });
});

describe('GlobalSearchModal', () => {
    it('should perform search when query length >= 2', async () => {
        const mockApiClient = {
            get: jest.fn().mockResolvedValue({
                data: { results: [
                    { id: '1', title: 'Test Customer', description: 'test@example.com', type: 'customer', url: '/customers/1' }
                ]}
            })
        };
        jest.mocked(apiClient).mockImplementation(() => mockApiClient);
        
        render(<GlobalSearchModal open={true} onClose={jest.fn()} />);
        
        const searchInput = screen.getByPlaceholderText('Kunden, Opportunities, Produkte...');
        fireEvent.change(searchInput, { target: { value: 'Test' } });
        
        await waitFor(() => {
            expect(mockApiClient.get).toHaveBeenCalledWith('/api/search/global?q=Test');
        });
        
        expect(screen.getByText('Test Customer')).toBeInTheDocument();
        expect(screen.getByText('test@example.com')).toBeInTheDocument();
    });
    
    it('should handle search result clicks', () => {
        const mockNavigate = jest.fn();
        const mockOnClose = jest.fn();
        jest.mocked(useNavigate).mockReturnValue(mockNavigate);
        
        render(<GlobalSearchModal open={true} onClose={mockOnClose} />);
        
        // Simulate search results
        const resultButton = screen.getByText('Test Customer');
        fireEvent.click(resultButton);
        
        expect(mockNavigate).toHaveBeenCalledWith('/customers/1');
        expect(mockOnClose).toHaveBeenCalled();
    });
});

describe('HotkeyManager', () => {
    it('should register global hotkeys correctly', () => {
        const mockUseHotkeys = jest.fn();
        jest.mocked(useHotkeys).mockImplementation(mockUseHotkeys);
        
        render(<HotkeyManager />);
        
        // Check that hotkeys are registered
        expect(mockUseHotkeys).toHaveBeenCalledWith('ctrl+k', expect.any(Function));
        expect(mockUseHotkeys).toHaveBeenCalledWith('ctrl+shift+c', expect.any(Function));
        expect(mockUseHotkeys).toHaveBeenCalledWith('ctrl+shift+o', expect.any(Function));
        expect(mockUseHotkeys).toHaveBeenCalledWith('ctrl+shift+k', expect.any(Function));
    });
    
    it('should respect permissions for hotkey actions', () => {
        const mockPermissions = {
            hasPermission: jest.fn((permission) => permission !== 'customers:write')
        };
        jest.mocked(usePermissions).mockReturnValue(mockPermissions);
        
        const mockNavigate = jest.fn();
        jest.mocked(useNavigate).mockReturnValue(mockNavigate);
        
        render(<HotkeyManager />);
        
        // Simulate Ctrl+Shift+C hotkey
        const hotkeyCallback = jest.mocked(useHotkeys).mock.calls
            .find(call => call[0] === 'ctrl+shift+c')[1];
        
        const mockEvent = { preventDefault: jest.fn() };
        hotkeyCallback(mockEvent);
        
        // Should not navigate if no permission
        expect(mockNavigate).not.toHaveBeenCalled();
    });
});
```

### Integration Tests - Search Functionality
```typescript
describe('Global Search Integration', () => {
    it('should integrate with backend search API', async () => {
        const searchResults = [
            { id: '1', title: 'John Doe', description: 'Customer from Berlin', type: 'customer', url: '/customers/1' },
            { id: '2', title: 'ABC Opportunity', description: 'Potential deal worth €50k', type: 'opportunity', url: '/opportunities/2' }
        ];
        
        const mockResponse = { data: { results: searchResults } };
        jest.mocked(apiClient.get).mockResolvedValue(mockResponse);
        
        render(<GlobalSearchModal open={true} onClose={jest.fn()} />);
        
        const searchInput = screen.getByPlaceholderText('Kunden, Opportunities, Produkte...');
        fireEvent.change(searchInput, { target: { value: 'John' } });
        
        await waitFor(() => {
            expect(screen.getByText('John Doe')).toBeInTheDocument();
            expect(screen.getByText('Customer from Berlin')).toBeInTheDocument();
            expect(screen.getByText('ABC Opportunity')).toBeInTheDocument();
        });
        
        expect(apiClient.get).toHaveBeenCalledWith('/api/search/global?q=John');
    });
});
```

### E2E Tests - Quick Actions Flow
```typescript
describe('Quick Actions E2E', () => {
    it('should provide complete quick create workflow', async () => {
        await page.goto('/customers');
        
        // Test floating action button
        await expect(page.locator('[aria-label="Quick Create Actions"]')).toBeVisible();
        await page.click('[aria-label="Quick Create Actions"]');
        
        // Test context-specific actions
        await expect(page.locator('text=Neuer Kunde')).toBeVisible();
        await page.click('text=Neuer Kunde');
        
        // Should navigate to customer creation
        await expect(page.locator('[data-testid=customer-form]')).toBeVisible();
        expect(page.url()).toContain('/customers/new');
    });
    
    it('should support keyboard shortcuts', async () => {
        await page.goto('/cockpit');
        
        // Test global search hotkey
        await page.keyboard.press('Control+KeyK');
        await expect(page.locator('[data-testid=global-search-modal]')).toBeVisible();
        
        // Test quick customer creation
        await page.keyboard.press('Escape'); // Close search
        await page.keyboard.press('Control+Shift+KeyC');
        
        await expect(page.locator('[data-testid=customer-form]')).toBeVisible();
        expect(page.url()).toContain('/customers/new');
    });
});
```

---

## 🚀 IMPLEMENTATION GUIDE

### Phase 1: Basic Quick Actions (1 Tag)
1. **SpeedDial Component** - Material-UI Floating Action Button System
2. **Context Detection** - Page-based Action Logic implementieren
3. **Permission Integration** - FC-009 Permission Gates für Actions

### Phase 2: Global Search (1 Tag)
1. **Search Modal** - Full-Screen Search Interface
2. **API Integration** - Backend Global Search Endpoint
3. **Hotkey Support** - Ctrl+K Global Search Shortcut

### Phase 3: Advanced Features (0.5 Tage)
1. **Additional Hotkeys** - Ctrl+Shift+C, Ctrl+Shift+O, etc.
2. **Calculator Integration** - M8 Calculator Quick Access
3. **Visual Polish** - Freshfoodz CI Colors und Animations

### Phase 4: Testing & Optimization (0.5 Tage)
1. **Component Tests** - Jest + React Testing Library
2. **E2E Tests** - Playwright Quick Action Flows
3. **Performance** - Lazy Loading und Optimization

---

## 📊 SUCCESS CRITERIA

### Funktionale Anforderungen
- ✅ Context-aware Quick Actions für alle Hauptseiten
- ✅ Permission-basierte Action-Sichtbarkeit
- ✅ Global Search mit Ctrl+K Hotkey
- ✅ Keyboard Shortcuts für häufige Aktionen
- ✅ Integration mit bestehenden Create-Flows
- ✅ Calculator Quick Access

### Performance-Anforderungen
- ✅ SpeedDial Render < 30ms
- ✅ Search Modal Open < 100ms
- ✅ Global Search API < 500ms
- ✅ Hotkey Response < 50ms
- ✅ Context Detection < 10ms

### UX-Anforderungen
- ✅ Intuitive Floating Action Position
- ✅ Clear Tooltips mit Hotkey Information
- ✅ Smooth Animations für Actions
- ✅ Consistent Freshfoodz CI (#94C456, #004F7B)
- ✅ Accessibility (Keyboard Navigation, Screen Reader)

---

## 🔗 NAVIGATION ZU ALLEN 40 FEATURES

### Core Sales Features
- [FC-001 Customer Acquisition](/docs/features/PLANNED/01_customer_acquisition/FC-001_TECH_CONCEPT.md) | [FC-002 Smart Customer Insights](/docs/features/PLANNED/02_smart_insights/FC-002_TECH_CONCEPT.md) | [M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md) | [M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_KOMPAKT.md)
- [FC-004 Verkäuferschutz](/docs/features/PLANNED/07_verkaeuferschutz/FC-004_TECH_CONCEPT.md) | [FC-013 Duplicate Detection](/docs/features/PLANNED/15_duplicate_detection/FC-013_TECH_CONCEPT.md) | [FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_TECH_CONCEPT.md)
- [FC-015 Deal Loss Analysis](/docs/features/PLANNED/17_deal_loss_analysis/FC-015_TECH_CONCEPT.md) | [FC-016 Opportunity Cloning](/docs/features/PLANNED/18_opportunity_cloning/FC-016_TECH_CONCEPT.md) | [FC-019 Advanced Sales Metrics](/docs/features/PLANNED/19_advanced_metrics/FC-019_TECH_CONCEPT.md)

### Security & Foundation Features  
- [FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_TECH_CONCEPT.md) | [FC-009 Advanced Permissions](/docs/features/ACTIVE/04_permissions_system/FC-009_TECH_CONCEPT.md) | [FC-025 DSGVO Compliance](/docs/features/PLANNED/25_dsgvo_compliance/FC-025_TECH_CONCEPT.md)
- [FC-023 Event Sourcing](/docs/features/PLANNED/23_event_sourcing/FC-023_TECH_CONCEPT.md) | [FC-038 Multi-Tenant Architecture](/docs/features/PLANNED/38_multitenant/FC-038_TECH_CONCEPT.md) | [FC-039 API Gateway](/docs/features/PLANNED/39_api_gateway/FC-039_TECH_CONCEPT.md)

### Mobile & Field Service Features
- [FC-006 Mobile App](/docs/features/PLANNED/09_mobile_app/FC-006_TECH_CONCEPT.md) | [FC-018 Mobile PWA](/docs/features/PLANNED/09_mobile_app/FC-018_MOBILE_FIELD_SALES.md) | [FC-022 Mobile Light](/docs/features/PLANNED/22_mobile_light/FC-022_TECH_CONCEPT.md)
- [FC-029 Voice-First Interface](/docs/features/PLANNED/29_voice_first/FC-029_TECH_CONCEPT.md) | [FC-030 One-Tap Actions](/docs/features/PLANNED/30_one_tap_actions/FC-030_TECH_CONCEPT.md) | [FC-032 Offline-First](/docs/features/PLANNED/32_offline_first/FC-032_TECH_CONCEPT.md)

### Communication Features
- [FC-003 E-Mail Integration](/docs/features/PLANNED/06_email_integration/FC-003_TECH_CONCEPT.md) | [FC-012 Team Communication](/docs/features/PLANNED/14_team_communication/FC-012_TECH_CONCEPT.md) | [FC-028 WhatsApp Business](/docs/features/PLANNED/28_whatsapp_integration/FC-028_TECH_CONCEPT.md)
- [FC-035 Social Selling Helper](/docs/features/PLANNED/35_social_selling/FC-035_TECH_CONCEPT.md)

### Analytics & Intelligence Features
- [M6 Analytics Module](/docs/features/PLANNED/13_analytics_m6/M6_KOMPAKT.md) | [FC-007 Chef-Dashboard](/docs/features/PLANNED/10_chef_dashboard/FC-007_TECH_CONCEPT.md) | [FC-026 Analytics Platform](/docs/features/PLANNED/26_analytics_platform/FC-026_TECH_CONCEPT.md)
- [FC-034 Instant Insights](/docs/features/PLANNED/34_instant_insights/FC-034_TECH_CONCEPT.md) | [FC-037 Advanced Reporting](/docs/features/PLANNED/37_advanced_reporting/FC-037_TECH_CONCEPT.md) | [FC-040 Performance Monitoring](/docs/features/PLANNED/40_performance_monitoring/FC-040_TECH_CONCEPT.md)

### Integration & Infrastructure Features
- [FC-005 Xentral Integration](/docs/features/PLANNED/08_xentral_integration/FC-005_TECH_CONCEPT.md) | [FC-010 Customer Import](/docs/features/PLANNED/11_customer_import/FC-010_KOMPAKT.md) | [FC-021 Integration Hub](/docs/features/PLANNED/21_integration_hub/FC-021_TECH_CONCEPT.md)
- [FC-024 File Management](/docs/features/PLANNED/24_file_management/FC-024_TECH_CONCEPT.md) | [M5 Customer Refactor](/docs/features/PLANNED/12_customer_refactor_m5/M5_KOMPAKT.md)

### UI & Productivity Features
- [M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_TECH_CONCEPT.md) | [M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md) | [M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_KOMPAKT.md)
- [FC-020 Quick Wins](/docs/features/PLANNED/20_quick_wins/FC-020_TECH_CONCEPT.md) | [FC-031 Smart Templates](/docs/features/PLANNED/31_smart_templates/FC-031_TECH_CONCEPT.md) | [FC-033 Visual Customer Cards](/docs/features/PLANNED/33_visual_cards/FC-033_TECH_CONCEPT.md)

### Advanced Features
- [FC-011 Bonitätsprüfung](/docs/features/ACTIVE/02_opportunity_pipeline/integrations/FC-011_KOMPAKT.md) | [FC-017 Sales Gamification](/docs/features/PLANNED/99_sales_gamification/FC-017_KOMPAKT.md) | [FC-027 Magic Moments](/docs/features/PLANNED/27_magic_moments/FC-027_TECH_CONCEPT.md)
- [FC-036 Beziehungsmanagement](/docs/features/PLANNED/36_relationship_mgmt/FC-036_TECH_CONCEPT.md) | [M8 Calculator Modal](/docs/features/ACTIVE/03_calculator_modal/M8_KOMPAKT.md)

---

**🚀 M2 Quick Create Actions - Ready for Enhancement!**  
**⏱️ Geschätzter Aufwand:** 2-3 Tage | **Enhancement:** Smart Quick Actions + Global Search + Hotkeys