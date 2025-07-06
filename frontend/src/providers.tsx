// App providers setup - React Query + Auth + Router
import { ReactNode } from 'react';
import { QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { BrowserRouter } from 'react-router-dom';
import { queryClient } from './lib/queryClient';
import { AuthProvider } from './contexts/AuthContext';
import { KeycloakProvider } from './contexts/KeycloakContext';
import { ErrorBoundary } from './components/ErrorBoundary';
import { USE_KEYCLOAK_IN_DEV, IS_DEV_MODE } from './lib/constants';

interface AppProvidersProps {
  children: ReactNode;
}

export const AppProviders = ({ children }: AppProvidersProps) => {
  // Conditional Auth Provider based on development settings
  const AuthWrapper = ({ children }: { children: ReactNode }) => {
    if (IS_DEV_MODE && !USE_KEYCLOAK_IN_DEV) {
      // Use existing AuthProvider for fallback mode
      return <AuthProvider>{children}</AuthProvider>;
    }

    // Use Keycloak for production or when enabled in dev
    return <KeycloakProvider>{children}</KeycloakProvider>;
  };

  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <AuthWrapper>{children}</AuthWrapper>
        </BrowserRouter>

        {/* React Query DevTools - only in development */}
        {import.meta.env.DEV && (
          <ReactQueryDevtools initialIsOpen={false} buttonPosition="bottom-right" />
        )}
      </QueryClientProvider>
    </ErrorBoundary>
  );
};
