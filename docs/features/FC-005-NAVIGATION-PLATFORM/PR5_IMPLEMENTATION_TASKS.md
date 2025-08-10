# 🛠️ FC-005 PR5: Implementation Tasks
**Konkrete Umsetzungsschritte für die Navigation Platform**

**Status:** 🚧 Ready for Implementation  
**Datum:** 10.08.2025  
**Sprint:** FC-005 Sprint 3  
**Geschätzter Aufwand:** 3-4 Tage

## 📚 Referenz-Dokumente

- [⬆️ PR5 Navigation Platform Plan](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-NAVIGATION-PLATFORM/PR5_NAVIGATION_PLATFORM_PLAN.md)
- [📋 Master Plan V5](/Users/joergstreeck/freshplan-sales-tool/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)
- [🏠 CLAUDE.md](/Users/joergstreeck/freshplan-sales-tool/CLAUDE.md)

## 📋 Task-Übersicht

### Phase 1: Navigation-Struktur (Tag 1)
- [ ] MainNavigation Component erstellen
- [ ] NavigationConfig definieren
- [ ] MenuItem Types implementieren
- [ ] Navigation Store (Zustand)

### Phase 2: Rechteverwaltung (Tag 2)
- [ ] Permission Service (Backend)
- [ ] Permission Guards (Frontend)
- [ ] Role-based Visibility
- [ ] API Endpoints

### Phase 3: User Features (Tag 3)
- [ ] Favoriten-System
- [ ] Schnellzugriffe
- [ ] Recently Visited
- [ ] Breadcrumbs

### Phase 4: Integration & Testing (Tag 4)
- [ ] Bestehende Features einbinden
- [ ] Admin-Bereich umziehen
- [ ] E2E Tests
- [ ] Documentation

## 🔨 Detaillierte Tasks

### Task 1: MainNavigation Component

**Datei:** `/frontend/src/components/navigation/MainNavigation.tsx`

```typescript
import React from 'react';
import { 
  Box, 
  List, 
  ListItem, 
  ListItemIcon, 
  ListItemText,
  Collapse,
  Divider,
  Typography,
  Badge
} from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { useNavigationStore } from '../../store/navigationStore';
import { getVisibleMenuItems } from '../../services/navigationService';

interface MainNavigationProps {
  collapsed?: boolean;
}

export const MainNavigation: React.FC<MainNavigationProps> = ({ collapsed = false }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const { favorites, recentlyVisited } = useNavigationStore();
  
  const menuItems = getVisibleMenuItems(user);
  
  return (
    <Box sx={{ width: collapsed ? 64 : 280, transition: 'width 0.3s' }}>
      {/* Schnellzugriffe */}
      {!collapsed && favorites.length > 0 && (
        <>
          <Typography variant="overline" sx={{ px: 2, py: 1 }}>
            Schnellzugriffe
          </Typography>
          <List dense>
            {favorites.map(item => (
              <NavigationItem key={item.id} item={item} />
            ))}
          </List>
          <Divider />
        </>
      )}
      
      {/* Hauptnavigation */}
      <List>
        {menuItems.map(section => (
          <NavigationSection key={section.id} section={section} />
        ))}
      </List>
    </Box>
  );
};
```

### Task 2: Navigation Configuration

**Datei:** `/frontend/src/config/navigation.config.ts`

```typescript
import { 
  Home, 
  People, 
  Analytics, 
  Settings,
  TrendingUp,
  Assignment,
  Email,
  Campaign
} from '@mui/icons-material';

export interface MenuItem {
  id: string;
  label: string;
  path?: string;
  icon?: React.ComponentType;
  badge?: number | string;
  requiredRoles?: Role[];
  requiredPermissions?: string[];
  children?: MenuItem[];
}

export interface NavigationSection {
  id: string;
  title: string;
  icon?: React.ComponentType;
  requiredRoles?: Role[];
  items: MenuItem[];
}

export const navigationConfig: NavigationSection[] = [
  {
    id: 'workspace',
    title: 'Mein Arbeitsplatz',
    icon: Home,
    items: [
      {
        id: 'cockpit',
        label: 'Mein Cockpit',
        path: '/cockpit',
        icon: Home,
        requiredRoles: ['all']
      },
      {
        id: 'tasks',
        label: 'Meine Aufgaben',
        path: '/tasks',
        icon: Assignment,
        requiredRoles: ['all']
      },
      {
        id: 'calendar',
        label: 'Meine Termine',
        path: '/calendar',
        icon: Event,
        requiredRoles: ['all']
      }
    ]
  },
  {
    id: 'sales',
    title: 'Vertrieb',
    icon: TrendingUp,
    requiredRoles: ['sales', 'manager', 'admin'],
    items: [
      {
        id: 'pipeline',
        label: 'Pipeline Management',
        icon: TrendingUp,
        children: [
          {
            id: 'opportunities',
            label: 'Verkaufschancen',
            path: '/opportunities'
          },
          {
            id: 'deals',
            label: 'Deal-Tracking',
            path: '/deals'
          },
          {
            id: 'forecast',
            label: 'Forecast',
            path: '/forecast'
          }
        ]
      },
      {
        id: 'customers',
        label: 'Kundenbetreuung',
        icon: People,
        children: [
          {
            id: 'all-customers',
            label: 'Alle Kunden',
            path: '/customers'
          },
          {
            id: 'new-customer',
            label: 'Neuer Kunde',
            path: '/customers/new'
          },
          {
            id: 'activities',
            label: 'Aktivitäten',
            path: '/activities'
          }
        ]
      }
    ]
  },
  {
    id: 'insights',
    title: 'Insights',
    icon: Analytics,
    requiredRoles: ['sales', 'manager', 'admin'],
    items: [
      {
        id: 'dashboard',
        label: 'Dashboard',
        path: '/insights/dashboard',
        icon: Dashboard
      },
      {
        id: 'reports',
        label: 'Berichte',
        icon: Assessment,
        children: [
          {
            id: 'revenue',
            label: 'Umsatzanalyse',
            path: '/insights/revenue'
          },
          {
            id: 'customer-analysis',
            label: 'Kundenentwicklung',
            path: '/insights/customers'
          },
          {
            id: 'team-performance',
            label: 'Team-Performance',
            path: '/insights/team'
          }
        ]
      }
    ]
  },
  {
    id: 'admin',
    title: 'Administration',
    icon: Settings,
    requiredRoles: ['manager', 'admin', 'auditor'],
    items: [
      {
        id: 'users',
        label: 'Benutzerverwaltung',
        path: '/admin/users',
        icon: People,
        requiredRoles: ['admin']
      },
      {
        id: 'audit',
        label: 'Audit & Compliance',
        path: '/admin/audit',
        icon: Security,
        requiredRoles: ['admin', 'auditor']
      },
      {
        id: 'settings',
        label: 'Systemkonfiguration',
        path: '/admin/settings',
        icon: Settings,
        requiredRoles: ['admin']
      }
    ]
  }
];
```

### Task 3: Navigation Store (Zustand)

**Datei:** `/frontend/src/store/navigationStore.ts`

```typescript
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface NavigationState {
  // Favoriten
  favorites: string[];
  addFavorite: (itemId: string) => void;
  removeFavorite: (itemId: string) => void;
  
  // Recently Visited
  recentlyVisited: RecentItem[];
  addRecentItem: (item: RecentItem) => void;
  
  // Collapsed State
  isCollapsed: boolean;
  toggleCollapse: () => void;
  
  // Active Section
  activeSection: string | null;
  setActiveSection: (sectionId: string | null) => void;
}

interface RecentItem {
  id: string;
  label: string;
  path: string;
  timestamp: number;
}

export const useNavigationStore = create<NavigationState>()(
  persist(
    (set) => ({
      // Favoriten
      favorites: [],
      addFavorite: (itemId) => 
        set((state) => ({
          favorites: [...state.favorites, itemId].slice(0, 5) // Max 5
        })),
      removeFavorite: (itemId) =>
        set((state) => ({
          favorites: state.favorites.filter(id => id !== itemId)
        })),
      
      // Recently Visited
      recentlyVisited: [],
      addRecentItem: (item) =>
        set((state) => {
          const filtered = state.recentlyVisited
            .filter(r => r.id !== item.id);
          return {
            recentlyVisited: [item, ...filtered].slice(0, 10) // Max 10
          };
        }),
      
      // UI State
      isCollapsed: false,
      toggleCollapse: () =>
        set((state) => ({ isCollapsed: !state.isCollapsed })),
      
      activeSection: null,
      setActiveSection: (sectionId) =>
        set({ activeSection: sectionId })
    }),
    {
      name: 'navigation-store',
      partialize: (state) => ({
        favorites: state.favorites,
        recentlyVisited: state.recentlyVisited,
        isCollapsed: state.isCollapsed
      })
    }
  )
);
```

### Task 4: Permission Service (Backend)

**Datei:** `/backend/src/main/java/de/freshplan/services/PermissionService.java`

```java
package de.freshplan.services;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class PermissionService {
    
    @Inject
    UserService userService;
    
    @Inject
    RoleService roleService;
    
    public record Permission(
        String resource,
        String action,
        String scope,
        List<Condition> conditions
    ) {}
    
    public record Condition(
        String field,
        String operator,
        Object value
    ) {}
    
    public List<Permission> getUserPermissions(String userId) {
        var user = userService.findById(userId);
        var roles = user.getRoles();
        
        return roles.stream()
            .flatMap(role -> getPermissionsForRole(role).stream())
            .distinct()
            .collect(Collectors.toList());
    }
    
    public boolean hasPermission(String userId, Permission required) {
        var userPermissions = getUserPermissions(userId);
        
        return userPermissions.stream()
            .anyMatch(p -> matchesPermission(p, required));
    }
    
    private boolean matchesPermission(Permission userPerm, Permission required) {
        // Resource match
        if (!userPerm.resource().equals(required.resource())) {
            return false;
        }
        
        // Action match
        if (!userPerm.action().equals(required.action()) 
            && !userPerm.action().equals("*")) {
            return false;
        }
        
        // Scope check (all > team > own)
        if (!isScopeSufficient(userPerm.scope(), required.scope())) {
            return false;
        }
        
        // Conditions check
        return checkConditions(userPerm.conditions(), required.conditions());
    }
    
    private List<Permission> getPermissionsForRole(String role) {
        return switch (role.toLowerCase()) {
            case "admin" -> getAdminPermissions();
            case "manager" -> getManagerPermissions();
            case "sales" -> getSalesPermissions();
            case "auditor" -> getAuditorPermissions();
            default -> List.of();
        };
    }
    
    private List<Permission> getAdminPermissions() {
        return List.of(
            new Permission("*", "*", "all", null)  // Full access
        );
    }
    
    private List<Permission> getManagerPermissions() {
        return List.of(
            new Permission("customer", "*", "team", null),
            new Permission("opportunity", "*", "team", null),
            new Permission("report", "read", "team", null),
            new Permission("user", "read", "team", null)
        );
    }
    
    private List<Permission> getSalesPermissions() {
        return List.of(
            new Permission("customer", "*", "own", 
                List.of(new Condition("status", "!=", "locked"))),
            new Permission("opportunity", "*", "own", null),
            new Permission("report", "read", "own", null)
        );
    }
}
```

### Task 5: Permission Guards (Frontend)

**Datei:** `/frontend/src/guards/PermissionGuard.tsx`

```typescript
import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { hasPermission } from '../services/permissionService';

interface PermissionGuardProps {
  children: React.ReactNode;
  requiredRoles?: string[];
  requiredPermissions?: string[];
  fallback?: string;
}

export const PermissionGuard: React.FC<PermissionGuardProps> = ({
  children,
  requiredRoles = [],
  requiredPermissions = [],
  fallback = '/unauthorized'
}) => {
  const { user } = useAuth();
  
  if (!user) {
    return <Navigate to="/login" />;
  }
  
  // Check roles
  if (requiredRoles.length > 0) {
    const hasRole = requiredRoles.some(role => 
      user.roles.includes(role) || role === 'all'
    );
    if (!hasRole) {
      return <Navigate to={fallback} />;
    }
  }
  
  // Check permissions
  if (requiredPermissions.length > 0) {
    const hasPerms = requiredPermissions.every(perm =>
      hasPermission(user, perm)
    );
    if (!hasPerms) {
      return <Navigate to={fallback} />;
    }
  }
  
  return <>{children}</>;
};

// Route wrapper
export const ProtectedRoute: React.FC<{
  element: React.ReactElement;
  requiredRoles?: string[];
  requiredPermissions?: string[];
}> = ({ element, ...guards }) => {
  return (
    <PermissionGuard {...guards}>
      {element}
    </PermissionGuard>
  );
};
```

### Task 6: Breadcrumbs Component

**Datei:** `/frontend/src/components/navigation/Breadcrumbs.tsx`

```typescript
import React from 'react';
import { Breadcrumbs as MuiBreadcrumbs, Link, Typography } from '@mui/material';
import { NavigateNext } from '@mui/icons-material';
import { useLocation, Link as RouterLink } from 'react-router-dom';
import { getBreadcrumbs } from '../../services/navigationService';

export const Breadcrumbs: React.FC = () => {
  const location = useLocation();
  const breadcrumbs = getBreadcrumbs(location.pathname);
  
  return (
    <MuiBreadcrumbs
      separator={<NavigateNext fontSize="small" />}
      aria-label="breadcrumb"
      sx={{ mb: 2 }}
    >
      {breadcrumbs.map((crumb, index) => {
        const isLast = index === breadcrumbs.length - 1;
        
        if (isLast) {
          return (
            <Typography key={crumb.path} color="text.primary">
              {crumb.label}
            </Typography>
          );
        }
        
        return (
          <Link
            key={crumb.path}
            component={RouterLink}
            to={crumb.path}
            underline="hover"
            color="inherit"
          >
            {crumb.label}
          </Link>
        );
      })}
    </MuiBreadcrumbs>
  );
};
```

### Task 7: Favoriten Management

**Datei:** `/frontend/src/components/navigation/FavoritesManager.tsx`

```typescript
import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Checkbox,
  Button,
  Box
} from '@mui/material';
import { Star, StarBorder, Delete } from '@mui/icons-material';
import { useNavigationStore } from '../../store/navigationStore';
import { navigationConfig } from '../../config/navigation.config';

interface FavoritesManagerProps {
  open: boolean;
  onClose: () => void;
}

export const FavoritesManager: React.FC<FavoritesManagerProps> = ({
  open,
  onClose
}) => {
  const { favorites, addFavorite, removeFavorite } = useNavigationStore();
  
  const allItems = navigationConfig
    .flatMap(section => section.items)
    .flatMap(item => [item, ...(item.children || [])]);
  
  const handleToggle = (itemId: string) => {
    if (favorites.includes(itemId)) {
      removeFavorite(itemId);
    } else {
      addFavorite(itemId);
    }
  };
  
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Schnellzugriffe verwalten</DialogTitle>
      <DialogContent>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Wählen Sie bis zu 5 Favoriten für den Schnellzugriff
        </Typography>
        
        <List>
          {allItems.map(item => (
            <ListItem key={item.id}>
              <ListItemIcon>
                <Checkbox
                  icon={<StarBorder />}
                  checkedIcon={<Star />}
                  checked={favorites.includes(item.id)}
                  onChange={() => handleToggle(item.id)}
                  disabled={!favorites.includes(item.id) && favorites.length >= 5}
                />
              </ListItemIcon>
              <ListItemText
                primary={item.label}
                secondary={item.path}
              />
            </ListItem>
          ))}
        </List>
        
        <Box sx={{ mt: 2, display: 'flex', justifyContent: 'flex-end' }}>
          <Button onClick={onClose}>Schließen</Button>
        </Box>
      </DialogContent>
    </Dialog>
  );
};
```

### Task 8: API Endpoints für Navigation

**Datei:** `/backend/src/main/java/de/freshplan/api/NavigationResource.java`

```java
package de.freshplan.api;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/api/navigation")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NavigationResource {
    
    @Inject
    NavigationService navigationService;
    
    @Inject
    PermissionService permissionService;
    
    @GET
    @Path("/menu")
    public Response getMenuForUser(@HeaderParam("Authorization") String auth) {
        var userId = extractUserId(auth);
        var menu = navigationService.getMenuForUser(userId);
        return Response.ok(menu).build();
    }
    
    @GET
    @Path("/favorites")
    public Response getFavorites(@HeaderParam("Authorization") String auth) {
        var userId = extractUserId(auth);
        var favorites = navigationService.getFavorites(userId);
        return Response.ok(favorites).build();
    }
    
    @POST
    @Path("/favorites/{itemId}")
    public Response addFavorite(
        @PathParam("itemId") String itemId,
        @HeaderParam("Authorization") String auth
    ) {
        var userId = extractUserId(auth);
        navigationService.addFavorite(userId, itemId);
        return Response.ok().build();
    }
    
    @DELETE
    @Path("/favorites/{itemId}")
    public Response removeFavorite(
        @PathParam("itemId") String itemId,
        @HeaderParam("Authorization") String auth
    ) {
        var userId = extractUserId(auth);
        navigationService.removeFavorite(userId, itemId);
        return Response.ok().build();
    }
    
    @POST
    @Path("/recent")
    public Response addRecentItem(
        RecentItemRequest request,
        @HeaderParam("Authorization") String auth
    ) {
        var userId = extractUserId(auth);
        navigationService.addRecentItem(userId, request);
        return Response.ok().build();
    }
}
```

## 🧪 Test-Strategie

### Unit Tests

```typescript
// navigation.test.ts
describe('NavigationService', () => {
  it('should filter menu items based on user roles', () => {
    const user = { roles: ['sales'] };
    const items = getVisibleMenuItems(user);
    
    expect(items).not.toContainEqual(
      expect.objectContaining({ id: 'admin' })
    );
  });
  
  it('should limit favorites to 5 items', () => {
    const store = useNavigationStore.getState();
    
    for (let i = 0; i < 10; i++) {
      store.addFavorite(`item-${i}`);
    }
    
    expect(store.favorites).toHaveLength(5);
  });
});
```

### E2E Tests

```typescript
// navigation.e2e.ts
test('Sales user should not see admin menu', async ({ page }) => {
  await loginAs(page, 'sales@test.com');
  await page.goto('/');
  
  await expect(page.locator('[data-testid="nav-admin"]')).not.toBeVisible();
  await expect(page.locator('[data-testid="nav-sales"]')).toBeVisible();
});

test('Favorites should persist across sessions', async ({ page }) => {
  await loginAs(page, 'user@test.com');
  
  // Add favorite
  await page.click('[data-testid="manage-favorites"]');
  await page.click('[data-testid="favorite-customers"]');
  await page.click('[data-testid="close-dialog"]');
  
  // Reload
  await page.reload();
  
  // Check favorite is still there
  await expect(page.locator('[data-testid="quick-access-customers"]')).toBeVisible();
});
```

## 📁 Datei-Struktur nach Implementation

```
frontend/src/
├── components/
│   └── navigation/
│       ├── MainNavigation.tsx
│       ├── NavigationItem.tsx
│       ├── NavigationSection.tsx
│       ├── Breadcrumbs.tsx
│       ├── FavoritesManager.tsx
│       └── QuickAccess.tsx
├── config/
│   └── navigation.config.ts
├── guards/
│   └── PermissionGuard.tsx
├── services/
│   ├── navigationService.ts
│   └── permissionService.ts
└── store/
    └── navigationStore.ts

backend/src/main/java/de/freshplan/
├── api/
│   └── NavigationResource.java
├── domain/
│   └── navigation/
│       ├── entity/
│       │   └── UserFavorite.java
│       └── service/
│           └── NavigationService.java
└── services/
    └── PermissionService.java
```

## ✅ Acceptance Criteria

- [ ] Navigation zeigt nur erlaubte Menüpunkte
- [ ] Favoriten werden persistiert
- [ ] Breadcrumbs funktionieren auf allen Seiten
- [ ] Admin-Bereich ist unter `/admin/*`
- [ ] Keyboard-Navigation funktioniert
- [ ] Mobile-responsive
- [ ] Performance < 100ms
- [ ] Alle Tests grün

## 🔗 Weiterführende Dokumente

- [⬆️ PR5 Navigation Platform Plan](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-NAVIGATION-PLATFORM/PR5_NAVIGATION_PLATFORM_PLAN.md)
- [📋 Master Plan V5](/Users/joergstreeck/freshplan-sales-tool/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)
- [🎯 Next Steps](/Users/joergstreeck/freshplan-sales-tool/NEXT_STEP.md)

---

**Status:** Ready for Implementation  
**Nächster Schritt:** Task 1 - MainNavigation Component erstellen