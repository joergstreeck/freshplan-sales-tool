// Theme V2 – keine Hardcodes, nur Tokens
import { createTheme } from '@mui/material/styles';
export const themeV2 = createTheme({
  palette:{ primary:{ main:'var(--color-primary-500)' }, secondary:{ main:'var(--color-secondary-500)' } },
  shape:{ borderRadius:10 },
  typography:{ fontFamily:'Poppins, system-ui, -apple-system, Segoe UI, Roboto, Arial' }
});