/**
 * Theme Provider with Settings Integration
 * Part of Sprint 1.3 PR #3 - Frontend Settings Integration (FP-233)
 *
 * Integrates Settings Registry with MUI Theme
 * Allows dynamic theme overrides from backend settings
 */

import React, { useMemo } from 'react';
import { ThemeProvider as MuiThemeProvider, createTheme, alpha } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { useThemeSettings } from '@/lib/settings/hooks';

interface ThemeProviderProps {
  children: React.ReactNode;
}

/**
 * Enhanced Theme Provider that merges default FreshFoodz theme
 * with dynamic settings from the Settings Registry.
 *
 * Falls back to hardcoded defaults if API is unavailable.
 */
export function ThemeProvider({ children }: ThemeProviderProps) {
  const { theme: themeSettings, isLoading, error } = useThemeSettings();

  // Create theme with settings overrides
  const theme = useMemo(() => {
    // Theme settings already include defaults from the useThemeSettings hook
    const primary = themeSettings?.primary || '#94C456'; // FreshFoodz Green
    const secondary = themeSettings?.secondary || '#004F7B'; // FreshFoodz Blue

    // Log theme source for debugging
    if (!isLoading) {
      console.debug('[Theme] Using colors:', {
        primary,
        secondary,
        source: error ? 'defaults (API error)' : themeSettings?.primary ? 'settings' : 'defaults',
      });
    }

    return createTheme({
      palette: {
        mode: 'light',
        primary: {
          main: primary,
          light: alpha(primary, 0.2), // 20% opacity using MUI alpha
          dark: primary,
          contrastText: '#ffffff',
        },
        secondary: {
          main: secondary,
          light: alpha(secondary, 0.2), // 20% opacity using MUI alpha
          dark: secondary,
          contrastText: '#ffffff',
        },
        success: {
          main: primary, // Use primary green for success
        },
        background: {
          default: '#f5f5f5',
          paper: '#ffffff',
        },
        text: {
          primary: '#333333',
          secondary: '#666666',
        },
        // Custom FreshFoodz palette extensions
        freshfoodz: {
          primary,
          secondary,
          success: primary,
          background: '#f5f5f5',
        },
        status: {
          won: primary,
          lost: '#dc3545',
          reactivate: '#ffc107',
          probabilityHigh: primary,
          probabilityMedium: '#ffa500',
          probabilityLow: '#dc3545',
        },
      },
      typography: {
        fontFamily: 'Poppins, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
        h1: {
          fontFamily: 'Antonio, sans-serif',
          fontWeight: 700,
          fontSize: '2.5rem',
          color: secondary,
        },
        h2: {
          fontFamily: 'Antonio, sans-serif',
          fontWeight: 700,
          fontSize: '2rem',
          color: secondary,
        },
        h3: {
          fontFamily: 'Antonio, sans-serif',
          fontWeight: 700,
          fontSize: '1.75rem',
          color: secondary,
        },
        h4: {
          fontFamily: 'Antonio, sans-serif',
          fontWeight: 700,
          fontSize: '1.5rem',
          color: secondary,
        },
        h5: {
          fontFamily: 'Antonio, sans-serif',
          fontWeight: 700,
          fontSize: '1.25rem',
          color: secondary,
        },
        h6: {
          fontFamily: 'Antonio, sans-serif',
          fontWeight: 700,
          fontSize: '1rem',
          color: secondary,
        },
        body1: {
          fontFamily: 'Poppins, sans-serif',
          fontSize: '1rem',
        },
        body2: {
          fontFamily: 'Poppins, sans-serif',
          fontSize: '0.875rem',
        },
        button: {
          fontFamily: 'Poppins, sans-serif',
          fontWeight: 600,
          textTransform: 'none', // Keep natural casing
        },
      },
      shape: {
        borderRadius: 8,
      },
      components: {
        MuiButton: {
          styleOverrides: {
            root: {
              borderRadius: 8,
              padding: '8px 16px',
              boxShadow: 'none',
              '&:hover': {
                boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
              },
            },
            contained: {
              '&:hover': {
                boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
              },
            },
          },
        },
        MuiPaper: {
          styleOverrides: {
            root: {
              boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
            },
          },
        },
        MuiAppBar: {
          styleOverrides: {
            root: {
              boxShadow: '0 1px 3px rgba(0,0,0,0.12)',
            },
          },
        },
        MuiCard: {
          styleOverrides: {
            root: {
              boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
              '&:hover': {
                boxShadow: '0 4px 16px rgba(0,0,0,0.12)',
              },
            },
          },
        },
        MuiChip: {
          styleOverrides: {
            root: {
              fontWeight: 500,
            },
          },
        },
      },
    });
  }, [themeSettings, isLoading, error]);

  return (
    <MuiThemeProvider theme={theme}>
      <CssBaseline />
      {children}
    </MuiThemeProvider>
  );
}

// Type augmentation for custom palette properties
declare module '@mui/material/styles' {
  interface Palette {
    freshfoodz: {
      primary: string;
      secondary: string;
      success: string;
      background: string;
    };
    status: {
      won: string;
      lost: string;
      reactivate: string;
      probabilityHigh: string;
      probabilityMedium: string;
      probabilityLow: string;
    };
  }

  interface PaletteOptions {
    freshfoodz?: {
      primary?: string;
      secondary?: string;
      success?: string;
      background?: string;
    };
    status?: {
      won?: string;
      lost?: string;
      reactivate?: string;
      probabilityHigh?: string;
      probabilityMedium?: string;
      probabilityLow?: string;
    };
  }
}
