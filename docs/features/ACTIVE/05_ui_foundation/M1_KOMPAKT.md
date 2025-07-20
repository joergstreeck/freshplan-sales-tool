# M1 Navigation System - KOMPAKT ⚡

**Feature-Typ:** 🎨 FRONTEND ENHANCEMENT  
**Priorität:** MEDIUM  
**Aufwand:** 3-4 Tage  
**Status:** 🔄 40% fertig (Enhancement-Mode)  

---

## 🎯 ÜBERBLICK

### Business Context
Permission-basierte, responsive Navigation System für FreshPlan Sales Tool. Enhancement des bestehenden MainLayoutV2.tsx (40% fertig) mit User-Permissions und Mobile-Optimierung.

### Technical Vision
Enterprise-Grade Navigation mit Permission-based Menu Items, Responsive Sidebar, User-Context Integration und Quick Actions Bar. Aufbau auf bestehende MainLayoutV2.tsx Basis.

---

## 🏗️ CORE ARCHITEKTUR

### Enhancement Strategy
```
BESTEHEND (40%):           ENHANCED (100%):
MainLayoutV2.tsx     →     MainLayoutV2Enhanced.tsx
├── Header          →     ├── UserAwareHeader  
├── Sidebar         →     ├── PermissionAwareSidebar
├── Content         →     ├── BreadcrumbContent
└── Basic Routing   →     └── ContextualActionBar
```

### Permission-Aware Navigation
```typescript
// 1. Enhanced Main Layout
export const MainLayoutV2Enhanced: React.FC<MainLayoutProps> = ({ children }) => {
    const { user } = useAuth();
    const { hasPermission } = usePermissions();
    const isMobile = useMediaQuery('(max-width: 768px)');
    
    return (
        <Box sx={{ display: 'flex' }}>
            <UserAwareHeader onMenuClick={() => setMobileOpen(!mobileOpen)} />
            <PermissionAwareSidebar 
                open={!isMobile ? sidebarOpen : mobileOpen}
                onClose={() => setMobileOpen(false)}
            />
            <BreadcrumbContent>{children}</BreadcrumbContent>
        </Box>
    );
};

// 2. Permission-Aware Sidebar
export const PermissionAwareSidebar: React.FC = ({ open, onClose }) => {
    const { hasPermission } = usePermissions();
    
    return (
        <Drawer open={open} onClose={onClose}>
            <List>
                <PermissionGate permission="customers:read">
                    <NavigationItem icon={<PeopleIcon />} text="Kunden" path="/customers" />
                </PermissionGate>
                <PermissionGate permission="opportunities:read">
                    <NavigationItem icon={<TrendingUpIcon />} text="Pipeline" path="/opportunities" />
                </PermissionGate>
                <PermissionGate permission="calculator:use">
                    <NavigationItem icon={<CalculateIcon />} text="Calculator" path="/calculator" />
                </PermissionGate>
            </List>
        </Drawer>
    );
};

// 3. User-Aware Header
export const UserAwareHeader: React.FC = ({ onMenuClick }) => {
    const { user } = useAuth();
    
    return (
        <AppBar position="fixed">
            <Toolbar>
                <IconButton onClick={onMenuClick}><MenuIcon /></IconButton>
                <Typography variant="h6" sx={{ flexGrow: 1 }}>FreshPlan</Typography>
                <UserMenu user={user} />
            </Toolbar>
        </AppBar>
    );
};
```

### Mobile-Responsive Design
```typescript
// Responsive Sidebar Management
const useResponsiveNavigation = () => {
    const isMobile = useMediaQuery('(max-width: 768px)');
    const [sidebarOpen, setSidebarOpen] = useState(!isMobile);
    const [mobileOpen, setMobileOpen] = useState(false);
    
    return {
        isMobile,
        sidebarOpen: isMobile ? mobileOpen : sidebarOpen,
        toggleSidebar: isMobile ? setMobileOpen : setSidebarOpen
    };
};
```

---

## 🔗 DEPENDENCIES

- **Benötigt:** FC-009 Permissions (permission-based menu visibility)
- **Erweitert:** FC-008 Security (user context for personalized navigation)
- **Integriert mit:** M3 Sales Cockpit, M7 Settings (navigation between features)

---

## 🧪 TESTING

### Permission-Based Tests
```typescript
describe('PermissionAwareSidebar', () => {
    it('should hide menu items without permissions', () => {
        const mockUsePermissions = { hasPermission: () => false };
        render(<PermissionAwareSidebar />);
        expect(screen.queryByText('Kunden')).not.toBeInTheDocument();
    });
    
    it('should show menu items with permissions', () => {
        const mockUsePermissions = { hasPermission: () => true };
        render(<PermissionAwareSidebar />);
        expect(screen.getByText('Kunden')).toBeInTheDocument();
    });
});
```

### Responsive Tests
```typescript
describe('ResponsiveNavigation', () => {
    it('should collapse sidebar on mobile', () => {
        Object.defineProperty(window, 'innerWidth', { value: 600 });
        render(<MainLayoutV2Enhanced />);
        expect(screen.queryByTestId('desktop-sidebar')).not.toBeVisible();
    });
});
```

---

## 📋 QUICK IMPLEMENTATION

### 🕒 15-Min Claude Working Section

**Aufgabe:** Permission-Aware Navigation Enhancement implementieren

**Sofort loslegen:**
1. MainLayoutV2.tsx um Permission-Gates erweitern
2. PermissionGate Component für Menu-Items
3. Responsive Sidebar mit Mobile-Support
4. User-Context in Header integrieren

**Quick-Win Code:**
```typescript
// 1. Permission Gate Component
export const PermissionGate: React.FC<{
    permission: string;
    children: React.ReactNode;
}> = ({ permission, children }) => {
    const { hasPermission } = usePermissions();
    return hasPermission(permission) ? <>{children}</> : null;
};

// 2. Enhanced Navigation Item
export const NavigationItem: React.FC<{
    icon: React.ReactNode;
    text: string;
    path: string;
}> = ({ icon, text, path }) => {
    const navigate = useNavigate();
    const location = useLocation();
    const isActive = location.pathname === path;
    
    return (
        <ListItem 
            button 
            onClick={() => navigate(path)}
            selected={isActive}
        >
            <ListItemIcon>{icon}</ListItemIcon>
            <ListItemText primary={text} />
        </ListItem>
    );
};

// 3. Mobile-Responsive Hook
const isMobile = useMediaQuery('(max-width: 768px)');
```

**Nächste Schritte:**
- Phase 1: Permission Gates implementieren (1h)
- Phase 2: Mobile Responsive Design (1h)
- Phase 3: User Context Integration (1h)
- Phase 4: Breadcrumb Navigation (1h)

**Erfolgs-Kriterien:**
- ✅ Menu-Items basierend auf Permissions sichtbar
- ✅ Mobile-responsive Sidebar funktional
- ✅ User-Context in Navigation integriert
- ✅ Breadcrumb-Navigation implementiert

---

**🔗 DETAIL-DOCS:**
- [TECH_CONCEPT](/docs/features/ACTIVE/05_ui_foundation/M1_TECH_CONCEPT.md) - Vollständige technische Spezifikation
- [IMPLEMENTATION_GUIDE](/docs/features/ACTIVE/05_ui_foundation/M1_IMPLEMENTATION_GUIDE.md) - Detaillierte Umsetzungsanleitung

**🎯 Nächster Schritt:** Permission-Gates in bestehende MainLayoutV2.tsx integrieren