# 🧭 M1 NAVIGATION IMPLEMENTATION GUIDE

**Fokus:** Nur Navigation Enhancement  
**Zeilen:** <200 für optimale Claude-Arbeitsweise  
**Prerequisite:** [M1_TECH_CONCEPT.md](/docs/features/ACTIVE/01_security/M1_TECH_CONCEPT.md) gelesen  
**Zurück zur Übersicht:** [IMPLEMENTATION_GUIDE.md](/docs/features/ACTIVE/01_security/IMPLEMENTATION_GUIDE.md)  

---

## 🚀 PHASE 1: ROLLE-BASIERTE NAVIGATION (Tag 1-2)

### 1.1: Navigation Hook erstellen

```typescript
// hooks/useRoleBasedNavigation.ts (NEU)
import { useUser } from '/docs/features/users/userQueries';

interface NavigationItem {
  id: string;
  label: string;
  icon: React.ReactNode;
  path: string;
  roles: string[];
  children?: NavigationItem[];
}

export const useRoleBasedNavigation = () => {
  const { data: currentUser } = useUser();
  
  const allNavigationItems: NavigationItem[] = [
    {
      id: 'cockpit',
      label: 'Sales Cockpit',
      icon: <DashboardIcon />,
      path: '/cockpit',
      roles: ['admin', 'manager', 'sales']
    },
    {
      id: 'customers',
      label: 'Kunden',
      icon: <PeopleIcon />,
      path: '/customers',
      roles: ['admin', 'manager', 'sales']
    },
    {
      id: 'pipeline',
      label: 'Pipeline',
      icon: <TrendingUpIcon />,
      path: '/pipeline',
      roles: ['admin', 'manager', 'sales']
    },
    {
      id: 'settings',
      label: 'Einstellungen',
      icon: <SettingsIcon />,
      path: '/einstellungen',
      roles: ['admin', 'manager']
    }
  ];
  
  const visibleItems = allNavigationItems.filter(item => 
    item.roles.some(role => currentUser?.roles.includes(role))
  );
  
  return { navigationItems: visibleItems, currentUser };
};
```

### 1.2: MainLayoutV2 erweitern

```typescript
// components/layout/MainLayoutV2.tsx (BESTEHEND ERWEITERN!)
import { useRoleBasedNavigation } from '../../hooks/useRoleBasedNavigation';

export const MainLayoutV2: React.FC<Props> = ({ children }) => {
  const { navigationItems, currentUser } = useRoleBasedNavigation();
  
  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar position="fixed" sx={{ zIndex: theme.zIndex.drawer + 1 }}>
        <Toolbar>
          {/* BESTEHENDE Toolbar-Items */}
          
          {/* NEU: Breadcrumbs hinzufügen */}
          <Breadcrumbs separator="›" sx={{ ml: 2 }}>
            <BreadcrumbNavigation />
          </Breadcrumbs>
          
          {/* NEU: User-Context erweitern */}
          <Box sx={{ ml: 'auto' }}>
            <NotificationBell />
            <UserAvatar user={currentUser} />
          </Box>
        </Toolbar>
      </AppBar>
      
      <Drawer variant="permanent" sx={{ width: 240 }}>
        <Toolbar />
        <Box sx={{ overflow: 'auto' }}>
          <List>
            {navigationItems.map(item => (
              <ListItem key={item.id} disablePadding>
                <ListItemButton component={Link} to={item.path}>
                  <ListItemIcon>{item.icon}</ListItemIcon>
                  <ListItemText primary={item.label} />
                </ListItemButton>
              </ListItem>
            ))}
          </List>
        </Box>
      </Drawer>
      
      <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
        <Toolbar />
        {children}
      </Box>
    </Box>
  );
};
```

---

## 📍 PHASE 2: BREADCRUMBS SYSTEM (Tag 3)

### 2.1: Breadcrumb Navigation Component

```typescript
// components/navigation/BreadcrumbNavigation.tsx (NEU)
import { useBreadcrumbs } from '../../hooks/useBreadcrumbs';

export const BreadcrumbNavigation: React.FC = () => {
  const breadcrumbs = useBreadcrumbs();
  
  return (
    <Breadcrumbs separator="›">
      {breadcrumbs.map((crumb, index) => (
        <Link
          key={crumb.path}
          color={index === breadcrumbs.length - 1 ? 'text.primary' : 'inherit'}
          href={crumb.path}
          underline="hover"
        >
          {crumb.label}
        </Link>
      ))}
    </Breadcrumbs>
  );
};
```

### 2.2: Breadcrumb Hook

```typescript
// hooks/useBreadcrumbs.ts (NEU)
import { useLocation } from 'react-router-dom';

interface BreadcrumbItem {
  label: string;
  path: string;
}

export const useBreadcrumbs = (): BreadcrumbItem[] => {
  const location = useLocation();
  
  const breadcrumbMap: Record<string, BreadcrumbItem[]> = {
    '/cockpit': [
      { label: 'Home', path: '/' },
      { label: 'Sales Cockpit', path: '/cockpit' }
    ],
    '/customers': [
      { label: 'Home', path: '/' },
      { label: 'Kunden', path: '/customers' }
    ],
    '/einstellungen': [
      { label: 'Home', path: '/' },
      { label: 'Einstellungen', path: '/einstellungen' }
    ]
  };
  
  return breadcrumbMap[location.pathname] || [{ label: 'Home', path: '/' }];
};
```

---

## 🔔 PHASE 3: NOTIFICATIONS (Tag 4)

### 3.1: Notification Bell Component

```typescript
// components/navigation/NotificationBell.tsx (NEU)
import { Badge, IconButton, Menu, MenuItem } from '@mui/material';
import { Notifications as NotificationsIcon } from '@mui/icons-material';

export const NotificationBell: React.FC = () => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const { data: notifications } = useNotifications();
  
  const unreadCount = notifications?.filter(n => !n.read).length || 0;
  
  return (
    <>
      <IconButton
        color="inherit"
        onClick={(e) => setAnchorEl(e.currentTarget)}
      >
        <Badge badgeContent={unreadCount} color="error">
          <NotificationsIcon />
        </Badge>
      </IconButton>
      
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={() => setAnchorEl(null)}
      >
        {notifications?.slice(0, 5).map(notification => (
          <MenuItem key={notification.id}>
            {notification.message}
          </MenuItem>
        ))}
      </Menu>
    </>
  );
};
```

---

## 🎯 SUCCESS CRITERIA

**Nach M1 Enhancement:**
1. ✅ Rolle-basierte Navigation funktioniert
2. ✅ Breadcrumbs auf allen Seiten
3. ✅ Notification Bell mit Unread-Count
4. ✅ User Avatar mit Context-Menü
5. ✅ <100ms Navigation-Updates

**Geschätzter Aufwand:** 4 Tage
**Voraussetzung:** Entscheidung D3 (Navigation-Style) geklärt

---

## 📞 NÄCHSTE SCHRITTE

1. **Entscheidung D3 klären** - Sidebar vs. Hybrid
2. **useRoleBasedNavigation implementieren** - Hook erstellen
3. **MainLayoutV2 erweitern** - Navigation integrieren
4. **Breadcrumbs implementieren** - Navigation-Context
5. **Notifications hinzufügen** - Bell + Menu

**WICHTIG:** MainLayoutV2 Basis ist exzellent - nur erweitern!