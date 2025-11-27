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
 * Default theme value for components outside provider
 * Falls back to standard mode (non-adaptive) for simple dialogs
 */
const defaultThemeValue: CustomerFieldThemeContextValue = {
  theme: getTheme('standard'),
  cssVariables: generateCSSVariables(getTheme('standard')),
};

/**
 * Hook zum Zugriff auf das Kunden-Theme
 *
 * Returns default values if used outside of CustomerFieldThemeProvider.
 * This allows DynamicFieldRenderer to work in simple dialogs (CreateBranchDialog, ContactFormDialog)
 * without requiring the full provider wrapper.
 */
export const useCustomerFieldTheme = () => {
  const context = useContext(CustomerFieldThemeContext);

  // Return default theme if no provider (graceful fallback)
  if (!context) {
    return defaultThemeValue;
  }

  return context;
};
