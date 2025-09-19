import React, { useLayoutEffect, useMemo } from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';

export type ThemeMode = 'light'|'dark';
export function useZeroFlickerTheme(mode: ThemeMode){
  useLayoutEffect(()=>{
    document.documentElement.style.setProperty('--color-primary-500', getComputedStyle(document.documentElement).getPropertyValue('--color-primary-500').trim() || '#94C456');
    document.documentElement.style.setProperty('--color-secondary-500', getComputedStyle(document.documentElement).getPropertyValue('--color-secondary-500').trim() || '#004F7B');
    document.documentElement.dataset.theme = mode;
  },[mode]);
  return useMemo(()=> createTheme({
    palette:{ mode, primary:{ main:'var(--color-primary-500)' }, secondary:{ main:'var(--color-secondary-500)' } },
    shape:{ borderRadius: 10 }
  }),[mode]);
}

export const SettingsThemeProvider: React.FC<{mode:ThemeMode, children: React.ReactNode}> = ({mode, children}) => {
  const theme = useZeroFlickerTheme(mode);
  return <ThemeProvider theme={theme}>{children}</ThemeProvider>;
};
