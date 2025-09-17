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
import { Box, useMediaQuery, useTheme } from '@mui/material';
import { SidebarNavigation } from './SidebarNavigation';
import { HeaderV2 } from './HeaderV2';
import { BreadcrumbNavigation } from './BreadcrumbNavigation';
import { KeyboardShortcutsHelp } from './KeyboardShortcutsHelp';
import { useNavigationStore } from '@/store/navigationStore';

// Layout-Konstanten
const DRAWER_WIDTH = 320; // Angepasst an SidebarNavigation
const DRAWER_WIDTH_COLLAPSED = 64;
const HEADER_HEIGHT = 64; // Standard Header-Höhe
const HEADER_HEIGHT_MOBILE = 112; // Mit Suchleiste auf Mobile

interface MainLayoutV2Props {
  children: React.ReactNode;
  showHeader?: boolean;
  hideHeader?: boolean; // Für spezielle Seiten wie Login
}

export const MainLayoutV2: React.FC<MainLayoutV2Props> = ({
  children,
  showHeader = true,
  hideHeader = false,
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const { isCollapsed, toggleSidebar } = useNavigationStore();

  // Berechne die aktuelle Drawer-Breite
  const drawerWidth = isCollapsed ? DRAWER_WIDTH_COLLAPSED : DRAWER_WIDTH;

  // Header soll angezeigt werden wenn nicht explizit versteckt
  const shouldShowHeader = !hideHeader && showHeader;

  // Berechne Header-Höhe basierend auf Gerät
  const headerHeight = isMobile ? HEADER_HEIGHT_MOBILE : HEADER_HEIGHT;

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      {/* Sidebar Container - Immer links */}
      <Box
        component="nav"
        sx={{
          width: { xs: 0, md: drawerWidth },
          flexShrink: { md: 0 },
          // Auf Mobile wird die Sidebar als Overlay angezeigt
          ...(isMobile && {
            position: 'fixed',
            top: 0,
            left: 0,
            zIndex: theme.zIndex.drawer,
            height: '100%',
            width: drawerWidth,
            transform: isCollapsed ? 'translateX(-100%)' : 'translateX(0)',
            transition: theme.transitions.create('transform', {
              easing: theme.transitions.easing.sharp,
              duration: theme.transitions.duration.enteringScreen,
            }),
          }),
        }}
      >
        <SidebarNavigation />
      </Box>

      {/* Haupt-Container mit Header und Content */}
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          flexGrow: 1,
          width: { xs: '100%', md: `calc(100% - ${drawerWidth}px)` },
          minHeight: '100vh',
        }}
      >
        {/* Header - Nimmt volle Breite des verbleibenden Raums */}
        {shouldShowHeader && <HeaderV2 showMenuIcon={isMobile} onMenuClick={toggleSidebar} />}

        {/* Spacer für fixed Header */}
        {shouldShowHeader && (
          <Box sx={{ height: headerHeight, flexShrink: 0 }} />
        )}

        {/* Breadcrumb Navigation */}
        {shouldShowHeader && <BreadcrumbNavigation />}

        {/* Main Content Area */}
        <Box
          component="main"
          sx={{
            flexGrow: 1,
            backgroundColor: 'background.default',
            // Wichtig: Isolierter Scroll-Context
            overflow: 'auto',
            position: 'relative',
          }}
        >
          {/* Page Content Container */}
          <Box
            sx={{
              // Content-spezifisches Padding
              p: { xs: 2, sm: 3, md: 4 },
              // Maximale Breite für bessere Lesbarkeit auf großen Screens
              maxWidth: 'xl',
              mx: 'auto',
              // Minimale Höhe für Content
              minHeight: '100vh',
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

      {/* Keyboard Shortcuts Help Button */}
      <KeyboardShortcutsHelp />
    </Box>
  );
};

// Export mit Default-Props für einfache Migration
export default MainLayoutV2;
