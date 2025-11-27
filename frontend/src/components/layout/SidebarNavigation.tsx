import React, { useEffect, useMemo } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Box, Drawer, List, IconButton, Tooltip, Typography } from '@mui/material';
import { styled } from '@mui/material/styles';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import { useNavigationStore } from '@/store/navigationStore';
import { useAuth } from '@/hooks/useAuth';
import { NavigationItem } from './NavigationItem';
import { navigationConfig } from '../../config/navigation.config';
import { useKeyboardNavigation } from '@/hooks/useKeyboardNavigation';

const DRAWER_WIDTH = 320; // Erhöht von 280px für bessere Textdarstellung
const DRAWER_WIDTH_COLLAPSED = 64;

const StyledDrawer = styled(Drawer, {
  shouldForwardProp: prop => prop !== 'collapsed',
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
    backgroundColor: 'grey.50', // Leicht grauer Hintergrund für besseren Kontrast
    borderRight: '2px solid #94C456', // Freshfoodz Grün als Akzent
    boxShadow: 1,
  },
}));

export const SidebarNavigation: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user } = useAuth();
  const {
    activeMenuId,
    expandedMenuId,
    isCollapsed,
    setActiveMenu,
    toggleSubmenu,
    openSubmenu,
    closeAllSubmenus,
    toggleSidebar,
    addToRecentlyVisited,
  } = useNavigationStore();

  // Derive permissions from user roles
  const userPermissions = useMemo(() => {
    if (!user?.roles) return [];

    const permissions: string[] = [];

    // Map roles to permissions
    const roleToPermissions: Record<string, string[]> = {
      admin: [
        'cockpit.view',
        'customers.create',
        'customers.view',
        'reports.view',
        'settings.view',
        'admin.view',
        'auditor.view',
        'manager.view',
      ],
      manager: [
        'cockpit.view',
        'customers.create',
        'customers.view',
        'reports.view',
        'settings.view',
        'manager.view',
      ],
      sales: ['cockpit.view', 'customers.create', 'customers.view', 'settings.view'],
      auditor: ['cockpit.view', 'reports.view', 'auditor.view'],
    };

    // Collect all permissions for user's roles
    user.roles.forEach(role => {
      const rolePermissions = roleToPermissions[role.toLowerCase()];
      if (rolePermissions) {
        permissions.push(...rolePermissions);
      }
    });

    // Return unique permissions
    return [...new Set(permissions)];
  }, [user?.roles]);

  // Keyboard shortcuts
  useKeyboardNavigation();

  // Track visited pages
  useEffect(() => {
    addToRecentlyVisited(location.pathname);
  }, [location.pathname, addToRecentlyVisited]);

  // Set active menu based on current path
  useEffect(() => {
    const currentPath = location.pathname;

    // Find matching navigation item for highlighting
    const matchingItem = navigationConfig.find(item => {
      // Check sub-items first for more specific match
      if (item.subItems) {
        const hasSubMatch = item.subItems.some(subItem => {
          return subItem.path && currentPath.startsWith(subItem.path);
        });
        if (hasSubMatch) return true;
      }

      // Direct path match
      return currentPath.startsWith(item.path);
    });

    if (matchingItem) {
      // Set the active menu for highlighting
      setActiveMenu(matchingItem.id);

      // If this item has a dashboard page and subItems, ensure it's expanded
      if (matchingItem.hasOwnPage && matchingItem.subItems && matchingItem.subItems.length > 0) {
        // Auto-expand if we're on the dashboard page
        if (currentPath === matchingItem.path || currentPath.startsWith(matchingItem.path + '/')) {
          // Use setTimeout to ensure the state update happens after render
          setTimeout(() => {
            openSubmenu(matchingItem.id);
          }, 0);
        }
      }
    }
  }, [location.pathname, setActiveMenu, openSubmenu]);

  // Auto-expand menu on initial page load only
  useEffect(() => {
    const currentPath = location.pathname;

    // Find if we're on a submenu page
    const matchingItem = navigationConfig.find(item => {
      if (item.subItems) {
        return item.subItems.some(subItem => {
          return subItem.path && currentPath === subItem.path;
        });
      }
      return false;
    });

    if (matchingItem && !expandedMenuId) {
      // Only expand on initial load when no menu is expanded yet
      toggleSubmenu(matchingItem.id);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Empty dependency - run only once on mount

  // Filter navigation items based on permissions
  const visibleItems = navigationConfig.filter(
    item => !item.permissions || item.permissions.some(p => userPermissions.includes(p))
  );

  return (
    <StyledDrawer variant="permanent" collapsed={isCollapsed}>
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          p: 2,
          minHeight: 64,
          borderBottom: '1px solid rgba(148, 196, 86, 0.2)',
        }}
      >
        {!isCollapsed && (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Box
              component="img"
              src="/freshfoodz-logo.svg"
              alt="FreshPlan"
              sx={{ height: 32 }}
              onError={e => {
                const target = e.target as HTMLImageElement;
                target.style.display = 'none';
              }}
            />
            <Typography
              variant="h6"
              sx={{
                color: 'secondary.main',
                fontFamily: theme => theme.typography.h4.fontFamily,
                fontWeight: 700,
              }}
            >
              FreshPlan
            </Typography>
          </Box>
        )}
        <Tooltip title={isCollapsed ? 'Navigation erweitern' : 'Navigation einklappen'}>
          <IconButton
            onClick={toggleSidebar}
            size="small"
            sx={{
              color: 'primary.main',
              '&:hover': {
                backgroundColor: 'rgba(148, 196, 86, 0.1)',
              },
            }}
          >
            {isCollapsed ? <ChevronRightIcon /> : <ChevronLeftIcon />}
          </IconButton>
        </Tooltip>
      </Box>

      <List component="nav" sx={{ px: 1 }}>
        {visibleItems.map(item => (
          <NavigationItem
            key={item.id}
            item={item}
            isActive={activeMenuId === item.id}
            isExpanded={expandedMenuId === item.id}
            isCollapsed={isCollapsed}
            userPermissions={userPermissions}
            onItemClick={() => {
              if (!item.subItems || item.subItems.length === 0) {
                // No submenu - navigate directly
                setActiveMenu(item.id);
                navigate(item.path);
                closeAllSubmenus();
              } else if (item.hasOwnPage) {
                // Has submenu BUT also has its own page
                setActiveMenu(item.id);

                // ALWAYS open the submenu immediately
                openSubmenu(item.id);

                // Then navigate
                navigate(item.path);
              } else {
                // Has submenu, no own page - just toggle submenu
                setActiveMenu(item.id);
                toggleSubmenu(item.id);
              }
            }}
            onSubItemClick={(pathOrAction, isAction) => {
              if (isAction) {
                // Handle action
                if (pathOrAction === 'OPEN_CUSTOMER_WIZARD') {
                  // Dispatch event to open customer wizard
                  window.dispatchEvent(new CustomEvent('freshplan:new-customer'));
                }
              } else {
                // Navigate to path but keep submenu open
                navigate(pathOrAction);
                setActiveMenu(item.id);
                // Keep the current submenu expanded - don't close it
              }
            }}
          />
        ))}
      </List>
    </StyledDrawer>
  );
};
