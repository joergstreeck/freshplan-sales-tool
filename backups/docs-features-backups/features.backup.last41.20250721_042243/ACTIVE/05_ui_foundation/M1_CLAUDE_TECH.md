# M1 Navigation System - CLAUDE TECH 🤖

## 🧠 QUICK-LOAD (30 Sekunden bis zur Produktivität)

**Feature:** Permission-based Navigation mit Responsive Sidebar und User Context  
**Stack:** React + MUI + React Router + TypeScript  
**Status:** 40% fertig - MainLayoutV2.tsx existiert, braucht Enhancement  
**Dependencies:** FC-009 Permissions, FC-008 Security | Integriert mit: M3 Cockpit, M7 Settings  

**Jump to:** [📚 Recipes](#-implementation-recipes) | [🧪 Tests](#-test-patterns) | [🔌 Integration](#-integration-cookbook) | [🎨 UI Patterns](#-ui-patterns)

**Core Purpose in 1 Line:** `User Permissions → Dynamic Menu Items → Responsive Navigation → Context Actions`

---

## 🍳 IMPLEMENTATION RECIPES

### Recipe 1: Permission-Aware Navigation in 5 Minuten
```typescript
// 1. Permission Gate Component (copy-paste ready)
export const PermissionGate: React.FC<{
    permission: string;
    children: React.ReactNode;
    fallback?: React.ReactNode;
}> = ({ permission, children, fallback = null }) => {
    const { hasPermission } = usePermissions();
    return hasPermission(permission) ? <>{children}</> : <>{fallback}</>;
};

// 2. Enhanced Navigation Menu
export const NavigationMenu: React.FC = () => {
    const location = useLocation();
    const { user } = useAuth();
    
    const menuItems = [
        { 
            path: '/cockpit', 
            label: 'Sales Cockpit', 
            icon: <DashboardIcon />, 
            permission: 'cockpit:view' 
        },
        { 
            path: '/customers', 
            label: 'Kunden', 
            icon: <PeopleIcon />, 
            permission: 'customers:read' 
        },
        { 
            path: '/opportunities', 
            label: 'Pipeline', 
            icon: <TrendingUpIcon />, 
            permission: 'opportunities:read' 
        },
        { 
            path: '/calculator', 
            label: 'Kalkulator', 
            icon: <CalculateIcon />, 
            permission: 'calculator:use' 
        }
    ];
    
    return (
        <List>
            {menuItems.map(item => (
                <PermissionGate key={item.path} permission={item.permission}>
                    <ListItem 
                        button 
                        selected={location.pathname === item.path}
                        onClick={() => navigate(item.path)}
                    >
                        <ListItemIcon>{item.icon}</ListItemIcon>
                        <ListItemText primary={item.label} />
                    </ListItem>
                </PermissionGate>
            ))}
        </List>
    );
};
```

### Recipe 2: Responsive Sidebar Enhancement
```typescript
// Enhance existing MainLayoutV2.tsx
export const MainLayoutV2Enhanced: React.FC<{ children: ReactNode }> = ({ children }) => {
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));
    const [mobileOpen, setMobileOpen] = useState(false);
    const [desktopOpen, setDesktopOpen] = useState(true);
    
    const drawerWidth = 240;
    
    const drawer = (
        <>
            <Toolbar>
                <Typography variant="h6" noWrap>
                    FreshPlan
                </Typography>
            </Toolbar>
            <Divider />
            <NavigationMenu />
            <Divider />
            <UserContextPanel />
        </>
    );
    
    return (
        <Box sx={{ display: 'flex' }}>
            <CssBaseline />
            <AppBar 
                position="fixed"
                sx={{ 
                    width: { md: `calc(100% - ${drawerWidth}px)` },
                    ml: { md: `${drawerWidth}px` }
                }}
            >
                <Toolbar>
                    <IconButton
                        color="inherit"
                        edge="start"
                        onClick={() => isMobile ? setMobileOpen(!mobileOpen) : setDesktopOpen(!desktopOpen)}
                        sx={{ mr: 2 }}
                    >
                        <MenuIcon />
                    </IconButton>
                    <Box sx={{ flexGrow: 1 }} />
                    <QuickActionsBar />
                    <UserMenu />
                </Toolbar>
            </AppBar>
            
            {/* Mobile Drawer */}
            <Drawer
                variant="temporary"
                open={mobileOpen}
                onClose={() => setMobileOpen(false)}
                sx={{
                    display: { xs: 'block', md: 'none' },
                    '& .MuiDrawer-paper': { width: drawerWidth }
                }}
            >
                {drawer}
            </Drawer>
            
            {/* Desktop Drawer */}
            <Drawer
                variant="persistent"
                open={desktopOpen}
                sx={{
                    display: { xs: 'none', md: 'block' },
                    '& .MuiDrawer-paper': { width: drawerWidth }
                }}
            >
                {drawer}
            </Drawer>
            
            <Box
                component="main"
                sx={{
                    flexGrow: 1,
                    p: 3,
                    width: { md: `calc(100% - ${drawerWidth}px)` },
                    ml: { md: desktopOpen ? `${drawerWidth}px` : 0 },
                    mt: '64px'
                }}
            >
                <BreadcrumbNavigation />
                {children}
            </Box>
        </Box>
    );
};
```

### Recipe 3: Context-Aware Quick Actions
```typescript
// Dynamic Quick Actions based on current page
export const QuickActionsBar: React.FC = () => {
    const location = useLocation();
    const navigate = useNavigate();
    
    const getContextActions = () => {
        switch (location.pathname) {
            case '/customers':
                return [
                    { label: 'Neuer Kunde', icon: <AddIcon />, action: () => navigate('/customers/new') },
                    { label: 'Import', icon: <UploadIcon />, action: () => navigate('/customers/import') }
                ];
            case '/opportunities':
                return [
                    { label: 'Neue Opportunity', icon: <AddIcon />, action: () => navigate('/opportunities/new') },
                    { label: 'Pipeline Board', icon: <ViewKanbanIcon />, action: () => navigate('/pipeline') }
                ];
            default:
                return [];
        }
    };
    
    return (
        <Box sx={{ display: 'flex', gap: 1 }}>
            {getContextActions().map((action, index) => (
                <Tooltip key={index} title={action.label}>
                    <IconButton color="inherit" onClick={action.action}>
                        {action.icon}
                    </IconButton>
                </Tooltip>
            ))}
        </Box>
    );
};
```

---

## 🧪 TEST PATTERNS

### Pattern 1: Permission-Based Navigation Test
```typescript
// Copy-paste Test Suite
describe('NavigationMenu', () => {
    it('should hide menu items without permissions', () => {
        const mockPermissions = {
            hasPermission: (perm: string) => perm !== 'customers:read'
        };
        
        render(
            <PermissionsProvider value={mockPermissions}>
                <NavigationMenu />
            </PermissionsProvider>
        );
        
        expect(screen.queryByText('Kunden')).not.toBeInTheDocument();
        expect(screen.getByText('Sales Cockpit')).toBeInTheDocument();
    });
    
    it('should highlight active route', () => {
        render(
            <MemoryRouter initialEntries={['/customers']}>
                <NavigationMenu />
            </MemoryRouter>
        );
        
        const customerItem = screen.getByRole('button', { name: /kunden/i });
        expect(customerItem).toHaveClass('Mui-selected');
    });
});
```

### Pattern 2: Responsive Behavior Test
```typescript
describe('Responsive Sidebar', () => {
    it('should show mobile drawer on small screens', () => {
        // Mock mobile viewport
        window.matchMedia = jest.fn().mockImplementation(query => ({
            matches: query === '(max-width: 960px)',
            media: query,
            addEventListener: jest.fn(),
            removeEventListener: jest.fn()
        }));
        
        render(<MainLayoutV2Enhanced>Content</MainLayoutV2Enhanced>);
        
        const menuButton = screen.getByRole('button', { name: /menu/i });
        fireEvent.click(menuButton);
        
        expect(screen.getByRole('presentation')).toBeInTheDocument();
    });
});
```

---

## 🔌 INTEGRATION COOKBOOK

### Mit Permission System (FC-009)
```typescript
// usePermissions Hook Integration
export const usePermissions = () => {
    const { user } = useAuth();
    
    const hasPermission = useCallback((permission: string) => {
        if (!user) return false;
        
        // Admin has all permissions
        if (user.role === 'admin') return true;
        
        // Check specific permissions
        return user.permissions?.includes(permission) ?? false;
    }, [user]);
    
    return { hasPermission, permissions: user?.permissions ?? [] };
};
```

### Mit Sales Cockpit (M3)
```typescript
// Navigation Integration in Sales Cockpit
export const SalesCockpitV2: React.FC = () => {
    const { setNavigationContext } = useNavigation();
    
    useEffect(() => {
        // Update navigation context
        setNavigationContext({
            breadcrumbs: [
                { label: 'Home', path: '/' },
                { label: 'Sales Cockpit', path: '/cockpit' }
            ],
            quickActions: [
                { label: 'Refresh', action: () => window.location.reload() }
            ]
        });
    }, []);
    
    return <CockpitContent />;
};
```

---

## 🎨 UI PATTERNS

### Breadcrumb Navigation
```typescript
export const BreadcrumbNavigation: React.FC = () => {
    const location = useLocation();
    const navigate = useNavigate();
    
    const pathnames = location.pathname.split('/').filter(x => x);
    
    return (
        <Breadcrumbs sx={{ mb: 2 }}>
            <Link 
                component="button" 
                onClick={() => navigate('/')}
                underline="hover"
            >
                Home
            </Link>
            {pathnames.map((value, index) => {
                const to = `/${pathnames.slice(0, index + 1).join('/')}`;
                const isLast = index === pathnames.length - 1;
                
                return isLast ? (
                    <Typography key={to} color="text.primary">
                        {value}
                    </Typography>
                ) : (
                    <Link
                        key={to}
                        component="button"
                        onClick={() => navigate(to)}
                        underline="hover"
                    >
                        {value}
                    </Link>
                );
            })}
        </Breadcrumbs>
    );
};
```

### User Context Panel
```typescript
export const UserContextPanel: React.FC = () => {
    const { user } = useAuth();
    
    return (
        <Box sx={{ p: 2 }}>
            <Typography variant="caption" color="text.secondary">
                Angemeldet als
            </Typography>
            <Typography variant="body2">
                {user?.name}
            </Typography>
            <Chip 
                label={user?.role} 
                size="small" 
                color="primary" 
                sx={{ mt: 1 }}
            />
        </Box>
    );
};
```

---

## 📚 DEEP KNOWLEDGE (Bei Bedarf expandieren)

<details>
<summary>🏗️ Vollständige Architektur</summary>

### Navigation State Management
```typescript
interface NavigationState {
    sidebarOpen: boolean;
    breadcrumbs: Breadcrumb[];
    quickActions: QuickAction[];
    activeModule: string;
}

const NavigationContext = createContext<NavigationState>({
    sidebarOpen: true,
    breadcrumbs: [],
    quickActions: [],
    activeModule: ''
});
```

### Advanced Permission Patterns
```typescript
// Role-based + Feature-based Permissions
interface Permission {
    resource: string;
    actions: string[];
    conditions?: PermissionCondition[];
}

// Hierarchical Permissions
const permissionHierarchy = {
    'customers:*': ['customers:read', 'customers:write', 'customers:delete'],
    'opportunities:*': ['opportunities:read', 'opportunities:write']
};
```

</details>

<details>
<summary>🎨 Design System Integration</summary>

### Freshfoodz CI Colors
```typescript
const navigationTheme = {
    primary: '#94C456',     // Freshfoodz Grün
    secondary: '#004F7B',   // Dunkelblau
    sidebar: {
        background: '#f5f5f5',
        hover: 'rgba(148, 196, 86, 0.08)',
        selected: 'rgba(148, 196, 86, 0.12)'
    }
};
```

### Typography Standards
```css
/* Antonio Bold für Headlines */
.MuiTypography-h6 {
    font-family: 'Antonio', sans-serif;
    font-weight: 700;
}

/* Poppins für Text */
.MuiTypography-body1 {
    font-family: 'Poppins', sans-serif;
}
```

</details>

---

**🎯 Nächster Schritt:** MainLayoutV2.tsx mit Permission-Gates erweitern