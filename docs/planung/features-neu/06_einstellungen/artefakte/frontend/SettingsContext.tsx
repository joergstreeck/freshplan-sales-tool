import React, { createContext, useContext } from 'react';
import { useEffectiveSettings } from './useSettings';
import { SettingsThemeProvider } from '../ThemeProvider';

const Ctx = createContext<{ mode:'light'|'dark', settings: any }>({ mode:'light', settings:{} });

export const SettingsProvider: React.FC<{children:React.ReactNode}> = ({children}) => {
  const { data } = useEffectiveSettings();
  const mode = (data?.blob?.['ui.theme'] ?? 'light') as 'light'|'dark';
  return (
    <Ctx.Provider value={{ mode, settings: data?.blob || {} }}>
      <SettingsThemeProvider mode={mode}>{children}</SettingsThemeProvider>
    </Ctx.Provider>
  );
};
export function useSettings(){ return useContext(Ctx); }
