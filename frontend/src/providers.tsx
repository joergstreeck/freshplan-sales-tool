// App providers setup - React Query + Auth + Router
import { ReactNode } from 'react';
import { QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { CacheProvider } from '@emotion/react';
import { emotionCache } from './emotion-cache';
import { queryClient } from './lib/queryClient';
import { AuthProvider } from './contexts/AuthContext';
import { KeycloakProvider } from './contexts/KeycloakContext';
import { ErrorBoundary } from './components/ErrorBoundary';
import freshfoodzTheme from './theme/freshfoodz';
import App from './App';
import { LoginBypassPage } from './pages/LoginBypassPage';
import { UsersPage } from './pages/UsersPage';
import { LegacyToolPage } from './pages/LegacyToolPage';
import { IntegrationTestPage } from './pages/IntegrationTestPage';
import CustomersPage from './pages/CustomersPage';
import { CockpitPage } from './pages/CockpitPage';
import { CockpitPageV2 } from './pages/CockpitPageV2';
import { SettingsPage } from './pages/SettingsPage';
import { CalculatorPageV2 } from './pages/CalculatorPageV2';
import { OpportunityPipelinePage } from './pages/OpportunityPipelinePage';

interface AppProvidersProps {
  children?: ReactNode;
}

export const AppProviders = ({ children: mainChildren }: AppProvidersProps) => {
  // Only include login bypass in development mode
  const isDevelopmentMode = import.meta.env.DEV && import.meta.env.MODE !== 'production';
  
  // Auth Provider wrapper - AuthProvider always depends on KeycloakContext
  const AuthWrapper = ({ children: authChildren }: { children: ReactNode }) => {
    return (
      <KeycloakProvider>
        <AuthProvider>{authChildren}</AuthProvider>
      </KeycloakProvider>
    );
  };

  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <CacheProvider value={emotionCache}>
          <ThemeProvider theme={freshfoodzTheme}>
            <CssBaseline />
          <BrowserRouter>
            <AuthWrapper>
              {mainChildren || (
                <Routes>
                <Route path="/" element={<App />} />
                <Route path="/cockpit" element={<CockpitPage />} />
                <Route path="/cockpit-v2" element={<CockpitPageV2 />} />
                <Route path="/users" element={<UsersPage />} />
                <Route path="/einstellungen" element={<SettingsPage />} />
                <Route path="/customers" element={<CustomersPage />} />
                <Route path="/kundenmanagement/opportunities" element={<OpportunityPipelinePage />} />
                <Route path="/calculator-v2" element={<CalculatorPageV2 />} />
                <Route path="/legacy-tool" element={<LegacyToolPage />} />
                {/* Login Bypass temporär reaktiviert - Auto-Login Problem */}
                {isDevelopmentMode && <Route path="/login-bypass" element={<LoginBypassPage />} />}
                {isDevelopmentMode && (
                  <Route path="/integration-test" element={<IntegrationTestPage />} />
                )}
              </Routes>
            )}
          </AuthWrapper>
        </BrowserRouter>

            {/* React Query DevTools - only in development */}
            {import.meta.env.DEV && (
              <ReactQueryDevtools initialIsOpen={false} buttonPosition="bottom-right" />
            )}
          </ThemeProvider>
        </CacheProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  );
};
