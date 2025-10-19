/**
 * MainLayoutV3 - Hauptlayout mit intelligentem Design System V2
 *
 * Features:
 * - Intelligente Content-Breiten-Erkennung via SmartLayout
 * - Visuelle Header-Trennung (8px Gap + Shadow)
 * - Optimierte Sidebar-Integration
 * - Freshfoodz CI-konform
 *
 * @since 2.0.0
 */

import React from 'react';
import { Box, useMediaQuery, useTheme } from '@mui/material';
import { SidebarNavigation } from './SidebarNavigation';
import { HeaderV2 } from './HeaderV2';
import SmartLayout, { type ContentWidth } from './SmartLayout';
import { useNavigationStore } from '@/store/navigationStore';

// Layout-Konstanten
const DRAWER_WIDTH = 320;
const DRAWER_WIDTH_COLLAPSED = 64;
const HEADER_HEIGHT = 64;
const HEADER_HEIGHT_MOBILE = 112;
const HEADER_CONTENT_GAP = 8; // Visueller Abstand

interface MainLayoutV3Props {
  children: React.ReactNode;
  /** Versteckt den Header komplett (z.B. für Login) */
  hideHeader?: boolean;
  /** Override für SmartLayout Breiten-Erkennung */
  forceContentWidth?: ContentWidth;
  /** Deaktiviert Paper-Container im SmartLayout */
  noPaper?: boolean;
  /** Hintergrundfarbe für Content-Bereich */
  contentBackground?: string;
}

export const MainLayoutV3: React.FC<MainLayoutV3Props> = ({
  children,
  hideHeader = false,
  forceContentWidth,
  noPaper = false,
  contentBackground = theme.palette.grey[50],
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const { isCollapsed, toggleSidebar } = useNavigationStore();

  // Berechne die aktuelle Drawer-Breite
  const drawerWidth = isCollapsed ? DRAWER_WIDTH_COLLAPSED : DRAWER_WIDTH;

  // Berechne Header-Höhe basierend auf Gerät
  const headerHeight = isMobile ? HEADER_HEIGHT_MOBILE : HEADER_HEIGHT;

  return (
    <Box
      sx={{
        display: 'flex',
        minHeight: '100vh',
        bgcolor: contentBackground,
      }}
    >
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
          position: 'relative',
        }}
      >
        {/* Header mit visueller Trennung */}
        {!hideHeader && (
          <>
            <Box
              sx={{
                position: 'sticky',
                top: 0,
                zIndex: theme.zIndex.appBar,
                boxShadow: '0 2px 4px rgba(0,0,0,0.08)',
              }}
            >
              <HeaderV2 showMenuIcon={isMobile} onMenuClick={toggleSidebar} />
            </Box>

            {/* Visueller Abstand zwischen Header und Content */}
            <Box sx={{ height: HEADER_CONTENT_GAP }} />
          </>
        )}

        {/* Main Content Area mit SmartLayout */}
        <Box
          component="main"
          sx={{
            flexGrow: 1,
            backgroundColor: contentBackground,
            overflow: 'auto',
            position: 'relative',
            // Minimale Höhe berechnen
            minHeight: !hideHeader
              ? `calc(100vh - ${headerHeight}px - ${HEADER_CONTENT_GAP}px)`
              : '100vh',
          }}
        >
          {/* SmartLayout übernimmt die intelligente Breiten-Steuerung */}
          <SmartLayout forceWidth={forceContentWidth} noPaper={noPaper}>
            {children}
          </SmartLayout>
        </Box>
      </Box>

      {/* Mobile Overlay wenn Sidebar offen */}
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

// Convenience Export
export default MainLayoutV3;
