import React, { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Box, Drawer, List, IconButton, Tooltip, Typography } from '@mui/material';
import { styled } from '@mui/material/styles';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import { useNavigationStore } from '@/store/navigationStore';
import { useAuthStore } from '@/store/authStore';
import { NavigationItem } from './NavigationItem';
import { navigationConfig } from '../../config/navigation.config';
import { useNavigationShortcuts } from '@/hooks/useNavigationShortcuts';

const DRAWER_WIDTH = 320; // Erhöht von 280px für bessere Textdarstellung
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
    backgroundColor: '#FAFAFA', // Leicht grauer Hintergrund für besseren Kontrast
    borderRight: '2px solid #94C456', // Freshfoodz Grün als Akzent
    boxShadow: '2px 0 8px rgba(0, 0, 0, 0.05)',
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
    closeAllSubmenus,
    toggleSidebar,
    addToRecentlyVisited
  } = useNavigationStore();

  // Keyboard shortcuts
  useNavigationShortcuts();

  // Track visited pages
  useEffect(() => {
    addToRecentlyVisited(location.pathname);
  }, [location.pathname, addToRecentlyVisited]);

  // Auto-set active menu based on current URL
  useEffect(() => {
    const currentPath = location.pathname;
    
    // Find matching navigation item (including sub-items)
    const matchingItem = navigationConfig.find(item => {
      // Direct path match
      if (currentPath.startsWith(item.path)) {
        return true;
      }
      
      // Sub-item path match
      if (item.subItems) {
        return item.subItems.some(subItem => currentPath.startsWith(subItem.path));
      }
      
      return false;
    });
    
    if (matchingItem) {
      // Always set the active menu to ensure correct highlighting
      setActiveMenu(matchingItem.id);
      
      // Check if we're on a sub-page
      const isOnSubPage = matchingItem.subItems?.some(sub => currentPath.startsWith(sub.path));
      
      // Auto-expand submenu if on a sub-page and not already expanded
      if (isOnSubPage && expandedMenuId !== matchingItem.id) {
        toggleSubmenu(matchingItem.id);
      }
    }
  }, [location.pathname]); // Remove dependencies to always update on path change

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
        justifyContent: 'space-between',
        p: 2,
        minHeight: 64,
        borderBottom: '1px solid rgba(148, 196, 86, 0.2)',
      }}>
        {!isCollapsed && (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Box
              component="img"
              src="/freshfoodz-logo.svg"
              alt="FreshPlan"
              sx={{ height: 32 }}
              onError={(e) => {
                const target = e.target as HTMLImageElement;
                target.style.display = 'none';
              }}
            />
            <Typography
              variant="h6"
              sx={{
                color: '#004F7B',
                fontFamily: 'Antonio, sans-serif',
                fontWeight: 700,
              }}
            >
              FreshPlan
            </Typography>
          </Box>
        )}
        <Tooltip title={isCollapsed ? "Navigation erweitern" : "Navigation einklappen"}>
          <IconButton 
            onClick={toggleSidebar} 
            size="small"
            sx={{
              color: '#94C456',
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
                // Navigate to the page and close any open submenus
                navigate(item.path);
                closeAllSubmenus();
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