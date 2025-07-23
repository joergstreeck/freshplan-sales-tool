/**
 * Freshfoodz Corporate Identity Theme
 * 
 * Basiert auf den offiziellen Freshfoodz CI-Vorgaben:
 * - Primärgrün: #94C456
 * - Dunkelblau: #004F7B  
 * - Typography: Antonio Bold (Headlines), Poppins (Text)
 */

import { createTheme } from '@mui/material/styles';

declare module '@mui/material/styles' {
  interface Palette {
    freshfoodz: {
      primary: string;
      secondary: string;
      success: string;
      background: string;
    };
  }

  interface PaletteOptions {
    freshfoodz?: {
      primary?: string;
      secondary?: string;
      success?: string;
      background?: string;
    };
  }
}

export const freshfoodzTheme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#94C456',        // Freshfoodz Primärgrün
      light: '#a8d06d',
      dark: '#7fb03f',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#004F7B',        // Freshfoodz Dunkelblau
      light: '#3374a0',
      dark: '#003856',
      contrastText: '#ffffff',
    },
    background: {
      default: '#fafafa',
      paper: '#ffffff',
    },
    text: {
      primary: '#000000',     // Schwarz für Haupttext
      secondary: '#004F7B',   // Dunkelblau für sekundären Text
    },
    divider: '#e0e0e0',
    // Custom Freshfoodz Palette
    freshfoodz: {
      primary: '#94C456',
      secondary: '#004F7B',
      success: '#94C456',
      background: '#ffffff',
    },
  },
  typography: {
    fontFamily: 'Poppins, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
    h1: {
      fontFamily: 'Antonio, sans-serif',
      fontWeight: 700,
      fontSize: '2.5rem',
      lineHeight: 1.2,
      color: '#004F7B',
    },
    h2: {
      fontFamily: 'Antonio, sans-serif',
      fontWeight: 700,
      fontSize: '2rem',
      lineHeight: 1.25,
      color: '#004F7B',
    },
    h3: {
      fontFamily: 'Antonio, sans-serif',
      fontWeight: 700,
      fontSize: '1.75rem',
      lineHeight: 1.3,
      color: '#004F7B',
    },
    h4: {
      fontFamily: 'Antonio, sans-serif',
      fontWeight: 700,
      fontSize: '1.5rem',
      lineHeight: 1.35,
      color: '#004F7B',
    },
    h5: {
      fontFamily: 'Antonio, sans-serif',
      fontWeight: 700,
      fontSize: '1.25rem',
      lineHeight: 1.4,
      color: '#004F7B',
    },
    h6: {
      fontFamily: 'Antonio, sans-serif',
      fontWeight: 700,
      fontSize: '1rem',
      lineHeight: 1.45,
      color: '#004F7B',
    },
    body1: {
      fontFamily: 'Poppins, sans-serif',
      fontWeight: 400,
      fontSize: '1rem',
      lineHeight: 1.5,
    },
    body2: {
      fontFamily: 'Poppins, sans-serif',
      fontWeight: 400,
      fontSize: '0.875rem',
      lineHeight: 1.43,
    },
    button: {
      fontFamily: 'Poppins, sans-serif',
      fontWeight: 500,
      textTransform: 'none' as const,
    },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        // Google Fonts werden über index.html geladen (vermeidet @import-Konflikte)
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          textTransform: 'none',
          fontWeight: 500,
          fontFamily: 'Poppins, sans-serif',
          '&:hover': {
            transform: 'translateY(-1px)',
            boxShadow: '0 4px 8px rgba(0,0,0,0.12)',
          },
        },
        contained: {
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
          '&:hover': {
            boxShadow: '0 4px 8px rgba(0,0,0,0.15)',
          },
        },
        containedPrimary: {
          backgroundColor: '#94C456',
          color: '#FFFFFF',
          '&:hover': {
            backgroundColor: '#7fb03f',
          },
        },
        containedSecondary: {
          backgroundColor: '#004F7B',
          color: '#FFFFFF',
          '&:hover': {
            backgroundColor: '#003856',
          },
        },
        text: {
          color: '#004F7B',
          '&:hover': {
            backgroundColor: 'rgba(0, 79, 123, 0.08)',
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
          '&:hover': {
            boxShadow: '0 4px 16px rgba(0,0,0,0.12)',
          },
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          borderRight: '1px solid #e0e0e0',
          background: '#ffffff',
        },
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          margin: '2px 0',
          '&.Mui-selected': {
            backgroundColor: 'rgba(148, 196, 86, 0.12)',
            color: '#94C456',
            borderLeft: '3px solid #94C456',
            '&:hover': {
              backgroundColor: 'rgba(148, 196, 86, 0.18)',
            },
          },
          '&:hover': {
            backgroundColor: 'rgba(148, 196, 86, 0.08)',
          },
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: 16,
        },
        colorPrimary: {
          backgroundColor: '#94C456',
          color: '#ffffff',
        },
        colorSecondary: {
          backgroundColor: '#004F7B',
          color: '#ffffff',
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            borderRadius: 8,
            '&:hover .MuiOutlinedInput-notchedOutline': {
              borderColor: '#94C456',
            },
            '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
              borderColor: '#94C456',
            },
          },
        },
      },
    },
    MuiTabs: {
      styleOverrides: {
        indicator: {
          backgroundColor: '#94C456',
          height: 3,
        },
      },
    },
    MuiTab: {
      styleOverrides: {
        root: {
          fontFamily: 'Poppins, sans-serif',
          color: '#004F7B',
          '&.Mui-selected': {
            color: '#94C456',
            fontWeight: 600,
          },
          '&:hover': {
            backgroundColor: 'rgba(148, 196, 86, 0.08)',
          },
        },
      },
    },
    MuiTypography: {
      styleOverrides: {
        h1: {
          fontFamily: 'Antonio, sans-serif',
          fontWeight: 700,
          color: '#004F7B',
        },
        h2: {
          fontFamily: 'Antonio, sans-serif',
          fontWeight: 700,
          color: '#004F7B',
        },
        h3: {
          fontFamily: 'Antonio, sans-serif',
          fontWeight: 700,
          color: '#004F7B',
        },
        h4: {
          fontFamily: 'Antonio, sans-serif',
          fontWeight: 700,
          color: '#004F7B',
        },
        h5: {
          fontFamily: 'Antonio, sans-serif',
          fontWeight: 700,
          color: '#004F7B',
        },
        h6: {
          fontFamily: 'Antonio, sans-serif',
          fontWeight: 700,
          color: '#004F7B',
        },
      },
    },
  },
  shape: {
    borderRadius: 8,
  },
  shadows: [
    'none',
    '0px 2px 4px rgba(0,0,0,0.05)',
    '0px 4px 8px rgba(0,0,0,0.08)',
    '0px 6px 12px rgba(0,0,0,0.12)',
    '0px 8px 16px rgba(0,0,0,0.15)',
    '0px 10px 20px rgba(0,0,0,0.18)',
    '0px 12px 24px rgba(0,0,0,0.20)',
    '0px 14px 28px rgba(0,0,0,0.22)',
    '0px 16px 32px rgba(0,0,0,0.24)',
    '0px 18px 36px rgba(0,0,0,0.26)',
    '0px 20px 40px rgba(0,0,0,0.28)',
    '0px 22px 44px rgba(0,0,0,0.30)',
    '0px 24px 48px rgba(0,0,0,0.32)',
    '0px 26px 52px rgba(0,0,0,0.34)',
    '0px 28px 56px rgba(0,0,0,0.36)',
    '0px 30px 60px rgba(0,0,0,0.38)',
    '0px 32px 64px rgba(0,0,0,0.40)',
    '0px 34px 68px rgba(0,0,0,0.42)',
    '0px 36px 72px rgba(0,0,0,0.44)',
    '0px 38px 76px rgba(0,0,0,0.46)',
    '0px 40px 80px rgba(0,0,0,0.48)',
    '0px 42px 84px rgba(0,0,0,0.50)',
    '0px 44px 88px rgba(0,0,0,0.52)',
    '0px 46px 92px rgba(0,0,0,0.54)',
    '0px 48px 96px rgba(0,0,0,0.56)',
  ] as [
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string
  ],
  transitions: {
    easing: {
      easeInOut: 'cubic-bezier(0.4, 0, 0.2, 1)',
      easeOut: 'cubic-bezier(0.0, 0, 0.2, 1)',
      easeIn: 'cubic-bezier(0.4, 0, 1, 1)',
      sharp: 'cubic-bezier(0.4, 0, 0.6, 1)',
    },
    duration: {
      shortest: 150,
      shorter: 200,
      short: 250,
      standard: 300,
      complex: 375,
      enteringScreen: 225,
      leavingScreen: 195,
    },
  },
});

export default freshfoodzTheme;