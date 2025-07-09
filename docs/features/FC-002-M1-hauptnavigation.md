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

#### 1. **Finale MainLayoutV2 Implementierung**

**Status:** ✅ Implementiert und getestet

Die neue `MainLayoutV2.tsx` wurde als Clean-Slate-Implementierung erstellt mit folgenden Kern-Features:

```typescript
// frontend/src/components/layout/MainLayoutV2.tsx
import React from 'react';
import { Box, AppBar, Toolbar, Typography, useMediaQuery, useTheme, IconButton } from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import { SidebarNavigation } from './SidebarNavigation';
import { useNavigationStore } from '@/store/navigationStore';

// Layout-Konstanten
const DRAWER_WIDTH = 280;
const DRAWER_WIDTH_COLLAPSED = 64;
const APP_BAR_HEIGHT = 64;

interface MainLayoutV2Props {
  children: React.ReactNode;
  showAppBar?: boolean;
  appBarContent?: React.ReactNode;
}
```

**Gelöste Probleme:**
1. **Isolierte Scroll-Contexts**: Main Content und Sidebar scrollen unabhängig
2. **Responsive Design**: Mobile-First mit Breakpoints für Tablet/Desktop
3. **Smooth Transitions**: Beim Collapse/Expand der Sidebar
4. **Mobile Overlay**: Backdrop beim Öffnen der Sidebar auf Mobile
5. **Box-Model Isolation**: `contain: 'layout style'` verhindert Layout-Bleed

**Kernmerkmale der Implementierung:**
- Keine CSS-Dateien, ausschließlich MUI sx-Props
- Berechnung der Drawer-Breite basierend auf Collapse-State
- Optional AppBar für flexible Nutzung
- TypeScript-Typisierung für alle Props
- Theme-basierte Transitions und Z-Index

#### 2. **Implementierte Layout-Hierarchie**

**Status:** ✅ Vollständig umgesetzt

```
CockpitPageV2
├── ThemeProvider (freshfoodzTheme)
├── CssBaseline (MUI Normalisierung)
└── MainLayoutV2
    ├── SidebarNavigation (permanent drawer)
    └── Main Content Area
        ├── Optional AppBar (sticky)
        └── Page Content Container
            └── CockpitViewV2 (isolierter Context)
```

**Besonderheiten der Implementierung:**
- `CockpitPageV2` handle Authentication und Theme
- `MainLayoutV2` stellt Layout-Struktur bereit
- `CockpitViewV2` ist komplett isoliert von Layout-Concerns
- Mobile Overlay wird conditional gerendert

### 🛡️ CSS-Strategie: Implementierte Lösung

**Status:** ✅ Phase 1 abgeschlossen

#### 1. **CSS-Cleanup durchgeführt**
- ✅ **Verschoben:** 8 CSS-Dateien nach `frontend/src/styles/legacy-to-remove/`
  - SalesCockpit.css
  - MyDayColumn.css
  - FocusListColumn.css
  - ActionCenterColumn.css
  - CockpitHeader.css
  - DashboardStats.css
  - ActivityTimeline.css
  - ErrorBoundary.css
- ✅ **Entfernt:** Alle CSS-Imports aus Cockpit-Komponenten
- ✅ **Ersetzt:** Mit Kommentar `// CSS import removed - migrating to MUI sx props`

#### 2. **Box-Model Isolation implementiert**
```typescript
// In CockpitViewV2.tsx
const PageContainer = styled(Box)(({ theme }) => ({
  position: 'relative',
  width: '100%',
  height: '100%',
  overflow: 'auto',
  contain: 'layout style', // Verhindert Layout-Bleed
}));
```

#### 3. **Theme-Integration**
- Verwendung von `freshfoodzTheme` in CockpitPageV2
- Alle Spacings über `theme.spacing()`
- Breakpoints über `theme.breakpoints`
- Z-Index über `theme.zIndex`

### 📋 Migrationsplan: Implementierte Strategie

**Status:** ✅ Phase 1 erfolgreich abgeschlossen

#### Phase 1: Layout-Fundament ✅ ABGESCHLOSSEN

1. **Feature-Branch erstellt**
   - Branch: `feature/FC-002-M1-layout-refactoring`
   - Parallel-Entwicklung ohne Breaking Changes

2. **MainLayoutV2 implementiert**
   - Datei: `frontend/src/components/layout/MainLayoutV2.tsx`
   - Vollständig MUI-basiert
   - Responsive mit Mobile-Support
   - Isolierte Scroll-Contexts

3. **Test-Route eingerichtet**
   - Route: `/cockpit-v2`
   - CockpitPageV2 mit Authentication-Check
   - CockpitViewV2 als Content-Container
   - Parallel zur alten Route `/cockpit`

4. **CSS-Cleanup durchgeführt**
   - 8 CSS-Dateien verschoben nach `legacy-to-remove`
   - Alle Imports entfernt und dokumentiert
   - Vorbereitung für MUI-Migration

#### Phase 2: Komponenten-Migration (Nächste Schritte)

1. **MUI-Migration der Cockpit-Komponenten**
   - SalesCockpit → Entfernen der CSS-Klassen
   - MyDayColumn → MeinTag mit MUI Paper
   - ActionCenterColumn → AktionsCenter mit MUI Card
   - Nutzung von sx-Props statt className

2. **Integration bestehender MUI-Komponenten**
   - SalesCockpitMUI weiter ausbauen
   - FilterBar aus FC-001 integrieren
   - ResizablePanels implementieren

#### Phase 3: Finalisierung

1. **Alte Route ersetzen**
   - `/cockpit` auf neue Implementierung umstellen
   - AuthenticatedLayout durch MainLayoutV2 ersetzen
   - Legacy-Code entfernen

2. **Cleanup & Optimierung**
   - CSS-Dateien löschen
   - Bundle-Size prüfen
   - Performance-Tests

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

### 🎯 Implementiertes Vorgehen & Ergebnisse

**Status:** ✅ Proof of Concept erfolgreich erstellt

#### Umgesetzte Strategie:

1. **Parallele Entwicklung etabliert**
   - `MainLayoutV2.tsx` als Clean-Slate-Implementierung
   - Route `/cockpit-v2` für risikofreies Testen
   - Alte Route `/cockpit` bleibt unverändert

2. **Vorteile des gewählten Ansatzes:**
   - ✅ Kein Breaking Change für bestehende Nutzer
   - ✅ Iterative Migration möglich
   - ✅ A/B-Testing zwischen alt und neu
   - ✅ Rollback jederzeit möglich

3. **Technische Implementierung:**
   - CockpitPageV2 als Wrapper mit Theme und Auth
   - MainLayoutV2 als Layout-Provider
   - CockpitViewV2 als isolierter Content
   - Wiederverwendung der SalesCockpitMUI Komponente

4. **Nächste Freigabe-Schritte:**
   - Test der Route `/cockpit-v2`
   - Vergleich mit `/cockpit`
   - Bei Erfolg: Phase 2 starten

### 📦 Commit-Historie

```bash
# Feature-Branch erstellt
git checkout -b feature/FC-002-M1-layout-refactoring

# Phase 1 Implementation
git commit -m "feat(FC-002-M1): implement clean slate layout architecture"
```

### 🎯 Empfehlung für Weiterführung

Nach Freigabe dieses Plans:
1. Test der neuen Route `/cockpit-v2`
2. Visual Regression Testing
3. Performance-Vergleich alt vs. neu
4. Bei Erfolg: Phase 2 beginnen