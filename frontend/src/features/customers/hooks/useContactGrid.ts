/**
 * useContactGrid Hook
 * 
 * Custom hook for responsive grid configuration of contact cards.
 * Part of FC-005 Contact Management UI - Smart Contact Cards.
 * 
 * @see /docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_CONTACT_CARDS.md
 */

import { useTheme } from '@mui/material/styles';
import { useMediaQuery } from '@mui/material';

export interface ContactGridConfig {
  columns: number;
  spacing: number;
  gridProps: {
    container: boolean;
    spacing: number;
    sx: any;
  };
}

/**
 * Hook for responsive contact grid configuration
 * 
 * @returns Grid configuration based on current breakpoint
 */
export const useContactGrid = (): ContactGridConfig => {
  const theme = useTheme();
  
  // Breakpoint detection
  const isXs = useMediaQuery(theme.breakpoints.down('sm'));
  const isSm = useMediaQuery(theme.breakpoints.between('sm', 'md'));
  const isMd = useMediaQuery(theme.breakpoints.between('md', 'lg'));
  const isLg = useMediaQuery(theme.breakpoints.between('lg', 'xl'));
  
  const getGridConfig = () => {
    if (isXs) return { columns: 1, spacing: 1 }; // Mobile: 1 column
    if (isSm) return { columns: 2, spacing: 2 }; // Tablet: 2 columns
    if (isMd) return { columns: 3, spacing: 2 }; // Small desktop: 3 columns
    if (isLg) return { columns: 4, spacing: 3 }; // Desktop: 4 columns
    return { columns: 5, spacing: 3 }; // Large desktop: 5 columns
  };
  
  const config = getGridConfig();
  
  return {
    ...config,
    gridProps: {
      container: true,
      spacing: config.spacing,
      sx: {
        // Ensure equal height cards
        '& > .MuiGrid-item': {
          display: 'flex',
          '& > *': {
            flex: 1,
            display: 'flex',
            flexDirection: 'column',
          },
        },
      },
    },
  };
};