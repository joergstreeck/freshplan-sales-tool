/**
 * MainLayoutV2 - Clean Slate Layout Architecture
 * 
 * Eine saubere, MUI-basierte Layout-Komponente die:
 * - Keine globalen CSS-Dateien benötigt
 * - Isolierte Scroll-Contexts bietet
 * - Responsive von Anfang an ist
 * - Theme-First Approach nutzt
 */

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

export const MainLayoutV2: React.FC<MainLayoutV2Props> = ({ 
  children, 
  showAppBar = false,
  appBarContent 
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const { isCollapsed, toggleSidebar } = useNavigationStore();
  
  // Berechne die aktuelle Drawer-Breite
  const drawerWidth = isCollapsed ? DRAWER_WIDTH_COLLAPSED : DRAWER_WIDTH;
  
  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      {/* Sidebar Container */}
      <Box
        component="nav"
        sx={{
          width: { md: drawerWidth },
          flexShrink: { md: 0 },
          // Auf Mobile wird die Sidebar als Overlay angezeigt
          ...(isMobile && {
            position: 'fixed',
            zIndex: theme.zIndex.drawer,
            height: '100%',
          }),
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
          // Smooth transition when sidebar collapses
          transition: theme.transitions.create(['width', 'margin'], {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.enteringScreen,
          }),
        }}
      >
        {/* Optional: Top App Bar */}
        {showAppBar && (
          <AppBar 
            position="sticky" 
            sx={{ 
              backgroundColor: 'background.paper',
              color: 'text.primary',
              boxShadow: 1,
            }}
          >
            <Toolbar sx={{ minHeight: APP_BAR_HEIGHT }}>
              {/* Mobile Menu Toggle */}
              {isMobile && (
                <IconButton
                  edge="start"
                  onClick={toggleSidebar}
                  sx={{ mr: 2, display: { md: 'none' } }}
                >
                  <MenuIcon />
                </IconButton>
              )}
              
              {/* App Bar Content */}
              {appBarContent || (
                <Typography variant="h6" noWrap component="div">
                  FreshPlan Sales Tool
                </Typography>
              )}
            </Toolbar>
          </AppBar>
        )}
        
        {/* Page Content Container */}
        <Box
          sx={{
            // Content-spezifisches Padding
            p: { xs: 2, sm: 3, md: 4 },
            // Maximale Breite für bessere Lesbarkeit auf großen Screens
            maxWidth: showAppBar ? '100%' : 'xl',
            mx: 'auto',
            // Volle Höhe minus AppBar
            minHeight: showAppBar 
              ? `calc(100vh - ${APP_BAR_HEIGHT}px)` 
              : '100vh',
            // Box-Model Isolation
            position: 'relative',
            width: '100%',
            // Verhindert Layout-Bleed
            contain: 'layout style',
          }}
        >
          {children}
        </Box>
      </Box>
      
      {/* Mobile Overlay when Sidebar is open */}
      {isMobile && !isCollapsed && (
        <Box
          onClick={toggleSidebar}
          sx={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            zIndex: theme.zIndex.drawer - 1,
          }}
        />
      )}
    </Box>
  );
};

// Export mit Default-Props für einfache Migration
export default MainLayoutV2;