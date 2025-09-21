// Theme V2 – Foundation compliant (no hex hardcoding, using CSS variables)
import { createTheme } from '@mui/material/styles';
export const themeV2 = createTheme({
  palette: {
    primary:   { main: 'var(--color-primary-500)' },   // #94C456
    secondary: { main: 'var(--color-secondary-500)' }  // #004F7B
  },
  shape: { borderRadius: 10 },
  typography: {
    fontFamily: 'Poppins, system-ui, -apple-system, Segoe UI, Roboto, Arial',
    h1: { fontFamily: 'Antonio, Poppins, sans-serif', fontWeight: 700 },
    h2: { fontFamily: 'Antonio, Poppins, sans-serif', fontWeight: 700 }
  },
  components: {
    MuiButton: { styleOverrides: { root: { borderRadius: '10px', textTransform: 'none', boxShadow: 'var(--shadow-sm)' } } }
  }
});
