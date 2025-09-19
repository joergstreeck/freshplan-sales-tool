/**
 * FreshFoodz Theme V2 - Foundation Standards Compliant
 *
 * @see ../../grundlagen/DESIGN_SYSTEM.md - FreshFoodz Corporate Identity
 * @see ../../grundlagen/CODING_STANDARDS.md - TypeScript Standards
 * @see ../../grundlagen/PERFORMANCE_STANDARDS.md - Frontend Performance Requirements
 *
 * This theme provides Foundation Standards compliant FreshFoodz CI integration
 * with correct primary/secondary color mapping and accessibility support.
 *
 * @author Frontend Team
 * @version 1.1
 * @since 2025-09-19
 */
import { createTheme } from '@mui/material/styles';

export const themeV2 = createTheme({
  palette: {
    primary:   { main: 'var(--color-primary-500)' },   // #94C456 (Primärgrün)
    secondary: { main: 'var(--color-secondary-500)' }  // #004F7B (Dunkelblau)
  },
  shape: { borderRadius: 10 },
  typography: {
    fontFamily: 'Poppins, sans-serif',
    h1: { fontFamily: 'Antonio, sans-serif', fontWeight: 700, color: 'var(--color-secondary-500)' },
    h2: { fontFamily: 'Antonio, sans-serif', fontWeight: 700, color: 'var(--color-secondary-500)' },
    button: { fontFamily: 'Poppins, sans-serif', fontWeight: 500 }
  },
  components: {
    MuiButton: { styleOverrides: { root: { borderRadius: '10px', textTransform: 'none', boxShadow: 'var(--shadow-sm)' } } }
  }
});
