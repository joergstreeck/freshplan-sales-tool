import React, { createContext, useContext, useMemo } from 'react';
import type { CustomerFieldTheme } from './customerFieldTheme';
import { getTheme, generateCSSVariables } from './customerFieldTheme';

interface CustomerFieldThemeContextValue {
  theme: CustomerFieldTheme;
  cssVariables: Record<string, string>;
}

const CustomerFieldThemeContext = createContext<CustomerFieldThemeContextValue | null>(null);

interface CustomerFieldThemeProviderProps {
  children: React.ReactNode;
  mode?: 'standard' | 'anpassungsfähig';
}

/**
 * Theme Provider für Kundenfelder
 * Stellt das Freshfoodz CI konforme Theme für alle Kundenformulare bereit
 */
export const CustomerFieldThemeProvider: React.FC<CustomerFieldThemeProviderProps> = ({
  children,
  mode = 'anpassungsfähig',
}) => {
  const value = useMemo(() => {
    const theme = getTheme(mode);
    const cssVariables = generateCSSVariables(theme);
    return { theme, cssVariables };
  }, [mode]);

  return (
    <CustomerFieldThemeContext.Provider value={value}>
      <div style={value.cssVariables} className="kunde-theme-container">
        {children}
      </div>
    </CustomerFieldThemeContext.Provider>
  );
};

/**
 * Hook zum Zugriff auf das Kunden-Theme
 */
export const useCustomerFieldTheme = () => {
  const context = useContext(CustomerFieldThemeContext);
  if (!context) {
    throw new Error(
      'useCustomerFieldTheme muss innerhalb eines CustomerFieldThemeProvider verwendet werden'
    );
  }
  return context;
};
