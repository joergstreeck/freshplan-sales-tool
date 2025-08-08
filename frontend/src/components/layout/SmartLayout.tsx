/**
 * SmartLayout - Intelligente Layout-Komponente für Design System V2
 *
 * Analysiert automatisch den Content-Typ und wählt die optimale Darstellung:
 * - Tabellen/Listen: Volle Breite
 * - Formulare: Begrenzte Breite (800px)
 * - Text/Artikel: Moderate Breite (1200px)
 * - Dashboard: Volle Breite mit weniger Padding
 *
 * @since 2.0.0
 */

import React, { useMemo } from 'react';
import { Box, Paper, useTheme, useMediaQuery, Theme } from '@mui/material';

export type ContentWidth = 'full' | 'content' | 'narrow' | 'form';

export interface SmartLayoutProps {
  children: React.ReactNode;
  /** Override für automatische Breiten-Erkennung */
  forceWidth?: ContentWidth;
  /** Deaktiviert Paper-Container für spezielle Layouts */
  noPaper?: boolean;
  /** Zusätzliche Klasse für spezielle Markierung */
  className?: string;
}

/**
 * Erkennt den Content-Typ basierend auf React-Children
 */
function detectContentType(children: React.ReactNode): ContentWidth {
  const elements = React.Children.toArray(children);

  // Zähle verschiedene Element-Typen
  let tableCount = 0;
  let formCount = 0;
  let textFieldCount = 0;
  let gridCount = 0;
  let dataWideCount = 0;

  const countElements = (element: React.ReactElement): void => {
    if (!element || !element.type) return;

    // Check für DataGrid oder Table
    if (
      element.type?.name === 'Table' ||
      element.type?.name === 'DataGrid' ||
      element.type?.displayName === 'DataGrid'
    ) {
      tableCount++;
    }

    // Check für Forms
    if (element.type === 'form' || element.type?.name === 'form') {
      formCount++;
    }

    // Check für TextFields
    if (element.type?.name === 'TextField' || element.type?.name === 'FormControl') {
      textFieldCount++;
    }

    // Check für Grid-Container mit mehreren Cards
    if (element.type?.name === 'Grid' || element.type?.name === 'Grid2') {
      gridCount++;
    }

    // Check für data-wide Attribute
    if (element.props?.['data-wide'] === 'true') {
      dataWideCount++;
    }

    // Rekursiv in Children suchen
    if (element.props?.children) {
      React.Children.forEach(element.props.children, countElements);
    }
  };

  elements.forEach(countElements);

  // Entscheidungslogik
  if (tableCount > 0 || dataWideCount > 0) {
    return 'full';
  }

  if (formCount > 0 || textFieldCount >= 3) {
    return 'form';
  }

  if (gridCount > 2) {
    return 'full'; // Dashboard-Style
  }

  // Default: Content-Breite für Text/Artikel
  return 'content';
}

/**
 * Berechnet die optimalen Container-Styles basierend auf der Breite
 */
function getContainerStyles(width: ContentWidth, theme: Theme, isMobile: boolean) {
  const baseStyles = {
    width: '100%',
    mx: 'auto',
    transition: theme.transitions.create(['max-width', 'padding'], {
      easing: theme.transitions.easing.easeInOut,
      duration: theme.transitions.duration.standard,
    }),
  };

  // Mobile: Immer volle Breite
  if (isMobile) {
    return {
      ...baseStyles,
      px: 2,
      py: 2,
    };
  }

  // Desktop: Content-basierte Breite
  switch (width) {
    case 'full':
      return {
        ...baseStyles,
        maxWidth: '100%',
        px: 2,
        py: 3,
      };

    case 'form':
      return {
        ...baseStyles,
        maxWidth: 800,
        px: 4,
        py: 4,
      };

    case 'narrow':
      return {
        ...baseStyles,
        maxWidth: 600,
        px: 4,
        py: 4,
      };

    case 'content':
    default:
      return {
        ...baseStyles,
        maxWidth: 1200,
        px: 3,
        py: 3,
      };
  }
}

/**
 * Paper-Styles mit visueller Hierarchie
 */
function getPaperStyles(theme: Theme) {
  return {
    p: { xs: 2, sm: 3, md: 4 },
    boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
    borderRadius: 2,
    backgroundColor: '#FFFFFF',
    minHeight: { xs: 'auto', sm: 400 },
    '&:hover': {
      boxShadow: '0 2px 4px rgba(0,0,0,0.08)',
    },
    transition: theme.transitions.create('box-shadow', {
      duration: theme.transitions.duration.short,
    }),
  };
}

export const SmartLayout: React.FC<SmartLayoutProps> = ({
  children,
  forceWidth,
  noPaper = false,
  className,
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  // Automatische Content-Typ Erkennung
  const detectedType = useMemo(
    () => forceWidth || detectContentType(children),
    [children, forceWidth]
  );

  const containerStyles = useMemo(
    () => getContainerStyles(detectedType, theme, isMobile),
    [detectedType, theme, isMobile]
  );

  const paperStyles = useMemo(() => getPaperStyles(theme), [theme]);

  return (
    <Box sx={containerStyles} className={className} data-layout-type={detectedType}>
      {noPaper ? children : <Paper sx={paperStyles}>{children}</Paper>}
    </Box>
  );
};

// Export für einfache Verwendung
export default SmartLayout;
