// App providers setup - React Query + Auth + Router
import { ReactNode } from 'react';
import { QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { queryClient } from './lib/queryClient';
import { AuthProvider } from './contexts/AuthContext';
import { KeycloakProvider } from './contexts/KeycloakContext';
import { ErrorBoundary } from './components/ErrorBoundary';
import { USE_KEYCLOAK_IN_DEV, IS_DEV_MODE } from './lib/constants';
import App from './App';
import { LoginBypassPage } from './pages/LoginBypassPage';
import { UsersPage } from './pages/UsersPage';
import { LegacyToolPage } from './pages/LegacyToolPage';
import { IntegrationTestPage } from './pages/IntegrationTestPage';
import CustomersPage from './pages/CustomersPage';
import { SalesCockpit } from './features/cockpit/components/SalesCockpit';

interface AppProvidersProps {
  children?: ReactNode;
}

export const AppProviders = ({ children: mainChildren }: AppProvidersProps) => {
  // Only include login bypass in development mode
  const isDevelopmentMode = import.meta.env.DEV && import.meta.env.MODE !== 'production';
  
  // Conditional Auth Provider based on development settings
  const AuthWrapper = ({ children: authChildren }: { children: ReactNode }) => {
    if (IS_DEV_MODE && !USE_KEYCLOAK_IN_DEV) {
      // Use existing AuthProvider for fallback mode
      return <AuthProvider>{authChildren}</AuthProvider>;
    }

    // Use Keycloak for production or when enabled in dev
    return <KeycloakProvider>{authChildren}</KeycloakProvider>;
  };

  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <AuthWrapper>
            {mainChildren || (
              <Routes>
                <Route path="/" element={<App />} />
                <Route path="/cockpit" element={<SalesCockpit />} />
                <Route path="/users" element={<UsersPage />} />
                <Route path="/customers" element={<CustomersPage />} />
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
      </QueryClientProvider>
    </ErrorBoundary>
  );
};
