import React, { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Box, Drawer, List, IconButton, Tooltip, Typography } from '@mui/material';
import { styled } from '@mui/material/styles';
import MenuIcon from '@mui/icons-material/Menu';
import MenuOpenIcon from '@mui/icons-material/MenuOpen';
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
    toggleSidebar,
    addToRecentlyVisited
  } = useNavigationStore();

  // Keyboard shortcuts
  useNavigationShortcuts();

  // Track visited pages and set active menu based on current location
  useEffect(() => {
    addToRecentlyVisited(location.pathname);
    
    // Find which navigation item matches the current path
    for (const item of navigationConfig) {
      if (item.path === location.pathname) {
        setActiveMenu(item.id);
        break;
      }
      // Check sub-items
      if (item.subItems) {
        for (const subItem of item.subItems) {
          if (subItem.path === location.pathname) {
            setActiveMenu(item.id);
            toggleSubmenu(item.id);
            break;
          }
        }
      }
    }
  }, [location.pathname, addToRecentlyVisited, setActiveMenu, toggleSubmenu]);

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
        <Tooltip title={isCollapsed ? "Menü öffnen" : "Menü schließen"}>
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
{isCollapsed ? <MenuIcon /> : <MenuOpenIcon />}
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
            userPermissions={userPermissions}
            onItemClick={() => {
              setActiveMenu(item.id);
              if (!item.subItems) {
                // Hauptmenü ohne Sub-Items: Direkt navigieren
                navigate(item.path);
              } else {
                // Hauptmenü mit Sub-Items: Smart Behavior
                if (isCollapsed) {
                  // Eingeklappte Sidebar: Aufklappen + Sub-Items zeigen
                  toggleSidebar(); // Sidebar aufklappen
                  toggleSubmenu(item.id); // Sub-Items zeigen
                } else {
                  // Ausgeklappte Sidebar: Sub-Items togglen
                  toggleSubmenu(item.id);
                }
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