# FC-002-M1: Hauptnavigation (Sidebar)

**Modul:** M1  
**Feature:** FC-002  
**Status:** 🔄 In Arbeit (0%)  
**Geschätzter Aufwand:** 2-3 Tage  

## 📋 Implementierungs-Checkliste

- [ ] SidebarNavigation.tsx Komponente erstellen
- [ ] NavigationItem.tsx implementieren
- [ ] NavigationSubMenu.tsx implementieren
- [ ] navigationStore.ts mit Zustand erstellen
- [ ] Routing-Integration
- [ ] Keyboard Shortcuts (Alt + 1-5)
- [ ] Collapse/Expand Animation
- [ ] Mobile Responsive Behavior
- [ ] Unit Tests schreiben
- [ ] Accessibility Tests

## 🏗️ Komponenten-Struktur

```
frontend/src/
├── components/
│   └── layout/
│       ├── SidebarNavigation.tsx      # Haupt-Sidebar-Komponente
│       ├── NavigationItem.tsx          # Einzelnes Navigationselement
│       ├── NavigationSubMenu.tsx       # Untermenü-Komponente
│       ├── SidebarToggle.tsx          # Collapse/Expand Button
│       └── __tests__/
│           └── SidebarNavigation.test.tsx
├── store/
│   └── navigationStore.ts              # Zustand für Navigation
├── hooks/
│   └── useNavigationShortcuts.ts      # Keyboard Shortcuts
└── types/
    └── navigation.types.ts             # TypeScript Definitionen
```

## 📝 Detaillierte Spezifikation

### 1. Haupt-Komponente: SidebarNavigation.tsx

```typescript
// frontend/src/components/layout/SidebarNavigation.tsx
import React, { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Box, Drawer, List, IconButton, Tooltip } from '@mui/material';
import { styled } from '@mui/material/styles';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import { useNavigationStore } from '@/store/navigationStore';
import { useAuthStore } from '@/store/authStore';
import { NavigationItem } from './NavigationItem';
import { navigationConfig } from '@/config/navigation.config';
import { useNavigationShortcuts } from '@/hooks/useNavigationShortcuts';

const DRAWER_WIDTH = 280;
const DRAWER_WIDTH_COLLAPSED = 64;

const StyledDrawer = styled(Drawer, {
  shouldForwardProp: (prop) => prop !== 'collapsed',
})<{ collapsed: boolean }>(({ theme, collapsed }) => ({
  width: collapsed ? DRAWER_WIDTH_COLLAPSED : DRAWER_WIDTH,
  flexShrink: 0,
  whiteSpace: 'nowrap',
  boxSizing: 'border-box',
  '& .MuiDrawer-paper': {
    width: collapsed ? DRAWER_WIDTH_COLLAPSED : DRAWER_WIDTH,
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
    overflowX: 'hidden',
    backgroundColor: theme.palette.background.paper,
    borderRight: `1px solid ${theme.palette.divider}`,
  },
}));

export const SidebarNavigation: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { userPermissions } = useAuthStore();
  const {
    activeMenuId,
    expandedMenuId,
    isCollapsed,
    setActiveMenu,
    toggleSubmenu,
    toggleSidebar,
    addToRecentlyVisited
  } = useNavigationStore();

  // Keyboard shortcuts
  useNavigationShortcuts();

  // Track visited pages
  useEffect(() => {
    addToRecentlyVisited(location.pathname);
  }, [location.pathname, addToRecentlyVisited]);

  // Filter navigation items based on permissions
  const visibleItems = navigationConfig.filter(item => 
    !item.permissions || item.permissions.some(p => userPermissions.includes(p))
  );

  return (
    <StyledDrawer
      variant="permanent"
      collapsed={isCollapsed}
    >
      <Box sx={{ 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'flex-end',
        p: 1,
        minHeight: 64
      }}>
        <Tooltip title={isCollapsed ? "Navigation erweitern" : "Navigation einklappen"}>
          <IconButton onClick={toggleSidebar} size="small">
            {isCollapsed ? <ChevronRightIcon /> : <ChevronLeftIcon />}
          </IconButton>
        </Tooltip>
      </Box>

      <List component="nav" sx={{ px: 1 }}>
        {visibleItems.map((item) => (
          <NavigationItem
            key={item.id}
            item={item}
            isActive={activeMenuId === item.id}
            isExpanded={expandedMenuId === item.id}
            isCollapsed={isCollapsed}
            onItemClick={() => {
              setActiveMenu(item.id);
              if (!item.subItems) {
                navigate(item.path);
              } else {
                toggleSubmenu(item.id);
              }
            }}
            onSubItemClick={(subPath) => {
              navigate(subPath);
              setActiveMenu(item.id);
            }}
          />
        ))}
      </List>
    </StyledDrawer>
  );
};
```

### 2. Navigation Item Komponente

```typescript
// frontend/src/components/layout/NavigationItem.tsx
import React from 'react';
import {
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Collapse,
  List,
  Tooltip,
} from '@mui/material';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { NavigationItemType } from '@/types/navigation.types';
import { NavigationSubMenu } from './NavigationSubMenu';

interface NavigationItemProps {
  item: NavigationItemType;
  isActive: boolean;
  isExpanded: boolean;
  isCollapsed: boolean;
  onItemClick: () => void;
  onSubItemClick: (path: string) => void;
}

export const NavigationItem: React.FC<NavigationItemProps> = ({
  item,
  isActive,
  isExpanded,
  isCollapsed,
  onItemClick,
  onSubItemClick,
}) => {
  const Icon = item.icon;
  
  const button = (
    <ListItemButton
      onClick={onItemClick}
      selected={isActive}
      sx={{
        borderRadius: 1,
        mb: 0.5,
        '&.Mui-selected': {
          backgroundColor: 'primary.lighter',
          '&:hover': {
            backgroundColor: 'primary.light',
          },
        },
      }}
    >
      <ListItemIcon sx={{ minWidth: 40 }}>
        <Icon />
      </ListItemIcon>
      {!isCollapsed && (
        <>
          <ListItemText primary={item.label} />
          {item.subItems && (
            isExpanded ? <ExpandLessIcon /> : <ExpandMoreIcon />
          )}
        </>
      )}
    </ListItemButton>
  );

  return (
    <>
      {isCollapsed ? (
        <Tooltip title={item.label} placement="right">
          {button}
        </Tooltip>
      ) : (
        button
      )}
      
      {item.subItems && !isCollapsed && (
        <Collapse in={isExpanded} timeout="auto" unmountOnExit>
          <NavigationSubMenu
            items={item.subItems}
            onItemClick={onSubItemClick}
          />
        </Collapse>
      )}
    </>
  );
};
```

### 3. Navigation Store

```typescript
// frontend/src/store/navigationStore.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface NavigationState {
  // Zustand
  activeMenuId: string | null;
  expandedMenuId: string | null;
  isCollapsed: boolean;
  recentlyVisited: string[];
  favorites: string[];
  
  // Actions
  setActiveMenu: (menuId: string) => void;
  toggleSubmenu: (menuId: string) => void;
  toggleSidebar: () => void;
  addToRecentlyVisited: (path: string) => void;
  clearRecentlyVisited: () => void;
  toggleFavorite: (menuId: string) => void;
}

export const useNavigationStore = create<NavigationState>()(
  persist(
    (set) => ({
      activeMenuId: 'cockpit',
      expandedMenuId: null,
      isCollapsed: false,
      recentlyVisited: [],
      favorites: [],
      
      setActiveMenu: (menuId) => set({ activeMenuId: menuId }),
      
      toggleSubmenu: (menuId) => set((state) => ({
        expandedMenuId: state.expandedMenuId === menuId ? null : menuId
      })),
      
      toggleSidebar: () => set((state) => ({ 
        isCollapsed: !state.isCollapsed,
        expandedMenuId: null
      })),
      
      addToRecentlyVisited: (path) => set((state) => {
        const updated = [path, ...state.recentlyVisited.filter(p => p !== path)];
        return { recentlyVisited: updated.slice(0, 5) };
      }),
      
      clearRecentlyVisited: () => set({ recentlyVisited: [] }),
      
      toggleFavorite: (menuId) => set((state) => ({
        favorites: state.favorites.includes(menuId)
          ? state.favorites.filter(id => id !== menuId)
          : [...state.favorites, menuId]
      })),
    }),
    {
      name: 'navigation-storage',
      partialize: (state) => ({
        isCollapsed: state.isCollapsed,
        favorites: state.favorites,
      }),
    }
  )
);
```

### 4. Navigation Konfiguration

```typescript
// frontend/src/config/navigation.config.ts
import DashboardIcon from '@mui/icons-material/Dashboard';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import PeopleIcon from '@mui/icons-material/People';
import AssessmentIcon from '@mui/icons-material/Assessment';
import SettingsIcon from '@mui/icons-material/Settings';
import { NavigationItemType } from '@/types/navigation.types';

export const navigationConfig: NavigationItemType[] = [
  {
    id: 'cockpit',
    label: 'Mein Cockpit',
    icon: DashboardIcon,
    path: '/cockpit',
    permissions: ['cockpit.view'],
  },
  {
    id: 'neukundengewinnung',
    label: 'Neukundengewinnung',
    icon: PersonAddIcon,
    path: '/neukundengewinnung',
    permissions: ['customers.create'],
    subItems: [
      {
        label: 'E-Mail Posteingang',
        path: '/neukundengewinnung/posteingang',
      },
      {
        label: 'Lead-Erfassung',
        path: '/neukundengewinnung/leads',
      },
      {
        label: 'Kampagnen',
        path: '/neukundengewinnung/kampagnen',
      },
    ],
  },
  {
    id: 'kundenmanagement',
    label: 'Kundenmanagement',
    icon: PeopleIcon,
    path: '/kundenmanagement',
    permissions: ['customers.view'],
    subItems: [
      {
        label: 'Alle Kunden',
        path: '/kundenmanagement/liste',
      },
      {
        label: 'Verkaufschancen',
        path: '/kundenmanagement/opportunities',
      },
      {
        label: 'Aktivitäten',
        path: '/kundenmanagement/aktivitaeten',
      },
    ],
  },
  {
    id: 'berichte',
    label: 'Auswertungen & Berichte',
    icon: AssessmentIcon,
    path: '/berichte',
    permissions: ['reports.view'],
    subItems: [
      {
        label: 'Umsatzübersicht',
        path: '/berichte/umsatz',
      },
      {
        label: 'Kundenanalyse',
        path: '/berichte/kunden',
      },
      {
        label: 'Aktivitätsberichte',
        path: '/berichte/aktivitaeten',
      },
    ],
  },
  {
    id: 'einstellungen',
    label: 'Einstellungen',
    icon: SettingsIcon,
    path: '/einstellungen',
    permissions: ['settings.view'],
  },
];
```

### 5. Keyboard Shortcuts Hook

```typescript
// frontend/src/hooks/useNavigationShortcuts.ts
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { navigationConfig } from '@/config/navigation.config';
import { useNavigationStore } from '@/store/navigationStore';

export const useNavigationShortcuts = () => {
  const navigate = useNavigate();
  const { setActiveMenu } = useNavigationStore();

  useEffect(() => {
    const handleKeyPress = (event: KeyboardEvent) => {
      // Alt + 1-5 für Hauptnavigation
      if (event.altKey && event.key >= '1' && event.key <= '5') {
        event.preventDefault();
        const index = parseInt(event.key) - 1;
        const navItem = navigationConfig[index];
        
        if (navItem) {
          setActiveMenu(navItem.id);
          navigate(navItem.path);
        }
      }
    };

    window.addEventListener('keydown', handleKeyPress);
    return () => window.removeEventListener('keydown', handleKeyPress);
  }, [navigate, setActiveMenu]);
};
```

## 🎨 Styling & Theme

### Freshfoodz CI Integration

```typescript
// Farben gemäß Freshfoodz CI
const theme = {
  primary: {
    main: '#94C456',      // Primärgrün
    light: '#a8d06d',
    lighter: '#e8f5e9',
  },
  secondary: {
    main: '#004F7B',      // Dunkelblau
  },
};

// Typography
const typography = {
  fontFamily: 'Poppins, sans-serif',
  h1: {
    fontFamily: 'Antonio Bold, sans-serif',
  },
};
```

## 🧪 Test-Szenarien

```typescript
// frontend/src/components/layout/__tests__/SidebarNavigation.test.tsx
describe('SidebarNavigation', () => {
  it('sollte alle erlaubten Menüpunkte anzeigen');
  it('sollte aktiven Menüpunkt hervorheben');
  it('sollte Untermenüs auf Klick öffnen/schließen');
  it('sollte bei Collapse nur Icons zeigen');
  it('sollte Keyboard Shortcuts (Alt+1-5) unterstützen');
  it('sollte zuletzt besuchte Seiten tracken');
  it('sollte Favoriten speichern können');
});
```

## 📐 Responsive Verhalten

- **Desktop (>1280px)**: Volle Sidebar immer sichtbar
- **Tablet (768-1280px)**: Sidebar kollabiert standardmäßig
- **Mobile (<768px)**: Overlay-Drawer mit Hamburger Menu

## ⚡ Performance-Optimierung

- Lazy Loading für Untermenü-Komponenten
- Memoization für Navigation Items
- Debounced Resize Handler
- Persistierung nur relevanter State-Teile

---

**Nächste Schritte:**
1. SidebarNavigation.tsx implementieren
2. Store und Types anlegen
3. Routing einbinden
4. Tests schreiben

---

## 🏗️ LAYOUT-ARCHITEKTUR-ANALYSE (09.07.2025)

### 🔍 IST-Analyse: CSS-Konflikte und ihre Ursachen

**Hauptproblem:** Die bestehende `SalesCockpit.css` und die neue `SidebarNavigation` haben fundamentale Layout-Konflikte:

#### 1. **Konkurrierende Layout-Systeme**
```css
/* SalesCockpit.css */
.sales-cockpit {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  margin: 0;
  padding: 0;
}
```
**Problem:** Erwartet die volle Viewport-Höhe und -Breite, kollidiert mit MainLayout's margin-left.

#### 2. **Position und Z-Index Konflikte**
```css
/* SalesCockpit.css */
.cockpit-main {
  position: relative;
}

/* MUI Drawer (SidebarNavigation) */
.MuiDrawer-paper {
  position: fixed;
  z-index: 1200;
}
```
**Problem:** Fixed Positioning der Sidebar überlagert relative Elemente im Cockpit.

#### 3. **Grid vs. Flexbox Konflikte**
- MainLayout nutzt Flexbox mit margin-left
- SalesCockpit nutzt CSS Grid für 3-Spalten
- Die Kombination führt zu Overflow und falschen Breiten-Berechnungen

#### 4. **Globale CSS-Variablen**
```css
/* Temporäre Variablen in SalesCockpit.css */
:root {
  --gray-50: #f9fafb;
  --z-10: 10;
}
```
**Problem:** Überschreiben potentiell MUI Theme-Variablen.

### 🎯 SOLL-Konzept: Die neue MainLayout Architektur

#### 1. **Neue MainLayout.tsx Struktur**
```typescript
// Die ideale MainLayout-Komponente mit MUI
import { Box, useMediaQuery, useTheme } from '@mui/material';

const MainLayout = ({ children }) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  
  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      {/* Sidebar Container */}
      <Box
        component="nav"
        sx={{
          width: { md: drawerWidth },
          flexShrink: { md: 0 },
        }}
      >
        <SidebarNavigation />
      </Box>
      
      {/* Main Content Area */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          width: { md: `calc(100% - ${drawerWidth}px)` },
          minHeight: '100vh',
          backgroundColor: 'background.default',
          // Wichtig: Isolierter Scroll-Context
          overflow: 'auto',
          position: 'relative',
        }}
      >
        {/* Optional: Top App Bar */}
        <AppBar position="sticky">
          {/* Breadcrumbs, User Menu, etc. */}
        </AppBar>
        
        {/* Page Content Container */}
        <Box
          sx={{
            // Content-spezifisches Padding
            p: { xs: 2, sm: 3 },
            // Maximale Breite für Lesbarkeit
            maxWidth: 'xl',
            mx: 'auto',
          }}
        >
          {children}
        </Box>
      </Box>
    </Box>
  );
};
```

#### 2. **Layout-Hierarchie**
```
App
├── ThemeProvider (MUI + Freshfoodz CI)
├── CssBaseline (Normalisierung)
└── MainLayout
    ├── SidebarNavigation (fixed/persistent)
    └── ContentArea
        ├── AppBar (optional, sticky)
        └── PageContent (scrollable)
            └── Individual Pages (CockpitView, etc.)
```

### 🛡️ CSS-Strategie: Isolation und Konsistenz

#### 1. **Keine globalen CSS-Dateien mehr**
- ❌ Entfernen: Alle `.css` Imports in Komponenten
- ✅ Nutzen: MUI's `sx` prop und `styled` API
- ✅ Theme-basierte Styles für Konsistenz

#### 2. **Box-Model Isolation**
```typescript
// Jede Page-Komponente erhält einen isolierten Container
const PageContainer = styled(Box)(({ theme }) => ({
  // Reset any inherited styles
  position: 'relative',
  width: '100%',
  height: '100%',
  // Eigener Scroll-Context
  overflow: 'auto',
  // Verhindert Layout-Bleed
  contain: 'layout style',
}));
```

#### 3. **Theme-First Approach**
```typescript
// Alle Spacing, Colors, Breakpoints aus dem Theme
const theme = createTheme({
  spacing: 8, // Basis-Unit
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          // Keine globalen Overrides!
        },
      },
    },
  },
});
```

### 📋 Migrationsplan

#### Phase 1: Layout-Fundament (Tag 1)
1. **Neues MainLayout.tsx erstellen**
   - Saubere MUI-basierte Struktur
   - Responsive Breakpoints
   - Isolierte Content-Area

2. **CSS-Cleanup**
   - Alle CSS-Dateien in `/legacy` verschieben
   - CSS-Imports aus Komponenten entfernen
   - Temporäre sx-Props als Zwischenlösung

#### Phase 2: Komponenten-Migration (Tag 2)
1. **CockpitPage.tsx → CockpitView.tsx**
   ```typescript
   // Alt: Mit CSS-Klassen
   <div className="sales-cockpit">
   
   // Neu: Mit MUI Grid
   <Grid container spacing={2} sx={{ height: '100%' }}>
   ```

2. **Spalten-Komponenten migrieren**
   - MyDayColumn → MeinTag (MUI Paper)
   - FocusListColumn (bereits mit FC-001 kompatibel)
   - ActionCenterColumn → AktionsCenter (MUI Card)

#### Phase 3: Integration & Polish (Tag 3)
1. **Route-Integration**
   - Alle Routes nutzen MainLayout
   - AuthenticatedLayout wird obsolet
   - Konsistente Navigation

2. **Testing & Optimierung**
   - Visual Regression Tests
   - Performance-Messungen
   - Accessibility-Audit

### 💡 Technische Einschätzung & Empfehlungen

#### Robustester Ansatz:
1. **"Clean Slate" Migration**
   - Komplett neue Komponenten mit MUI
   - Alte CSS-basierten Komponenten parallel behalten
   - Feature-Flag für schrittweise Umstellung

2. **Component Composition Pattern**
   ```typescript
   // Statt Vererbung: Composition
   <MainLayout>
     <CockpitLayout> {/* Spezifisches Sub-Layout */}
       <CockpitContent />
     </CockpitLayout>
   </MainLayout>
   ```

#### Potenzielle "Gotchas":

1. **CSS-in-JS Performance**
   - Bei vielen dynamischen Styles kann Emotion (MUI) langsam werden
   - Lösung: `styled` für statische, `sx` nur für dynamische Styles

2. **Theme-Überladung**
   - Zu viele Custom-Theme-Erweiterungen machen es unübersichtlich
   - Lösung: Design-Tokens als Konstanten, Theme nur für MUI-Standards

3. **Migration Fatigue**
   - Entwickler müssen zwei Systeme parallel verstehen
   - Lösung: Klare Trennung alt/neu, gute Dokumentation

4. **Bundle Size**
   - MUI + Emotion + alte CSS = großes Bundle
   - Lösung: CSS-Purge nach Migration, Tree-Shaking

#### Wartbarkeit-Garantien:

1. **Single Source of Truth**
   - Layout nur in MainLayout definiert
   - Keine Layout-Logik in Pages

2. **Testbarkeit**
   - Reine MUI-Komponenten sind einfacher zu testen
   - Visual Regression Tests mit Storybook

3. **Developer Experience**
   - TypeScript-Support für alle sx-Props
   - Konsistente Patterns überall

### 🎯 Empfohlenes Vorgehen

**Start mit einem Proof of Concept:**
1. Neue `MainLayoutV2.tsx` parallel erstellen
2. Eine Route (z.B. `/cockpit-v2`) zum Testen
3. Schrittweise Features migrieren
4. Alte Komponenten erst entfernen wenn alles stabil

Dieser Ansatz minimiert Risiko und erlaubt iterative Verbesserungen.