import React, { ReactElement } from 'react';
import { render, RenderOptions } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, MemoryRouter } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import { AuthProvider } from '../contexts/AuthContext';
import { KeycloakProvider } from '../contexts/KeycloakContext';
import { I18nextProvider } from 'react-i18next';
import i18n from '../i18n';

// Create a default theme for tests
const testTheme = createTheme({
  palette: {
    primary: {
      main: '#004F7B',
    },
    secondary: {
      main: '#94C456',
    },
    background: {
      default: '#f5f5f5',
      paper: '#ffffff',
    },
  },
});

// Create a test query client with shorter defaults
const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        staleTime: 0,
        gcTime: 0,
      },
    },
  });

interface AllTheProvidersProps {
  children: React.ReactNode;
}

// All providers that components might need
const AllTheProviders: React.FC<AllTheProvidersProps> = ({ children }) => {
  const queryClient = createTestQueryClient();

  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={testTheme}>
        <CssBaseline />
        <BrowserRouter>
          <KeycloakProvider>
            <AuthProvider>{children}</AuthProvider>
          </KeycloakProvider>
        </BrowserRouter>
      </ThemeProvider>
    </QueryClientProvider>
  );
};

// Custom render function
const customRender = (ui: ReactElement, options?: Omit<RenderOptions, 'wrapper'>) =>
  render(ui, { wrapper: AllTheProviders, ...options });

// Render with only Router context (for components that need minimal context)
interface MinimalRouterProviderProps {
  children: React.ReactNode;
}

const MinimalRouterProvider: React.FC<MinimalRouterProviderProps> = ({ children }) => {
  const queryClient = createTestQueryClient();

  return (
    <QueryClientProvider client={queryClient}>
      <I18nextProvider i18n={i18n}>
        <ThemeProvider theme={testTheme}>
          <CssBaseline />
          <MemoryRouter>{children}</MemoryRouter>
        </ThemeProvider>
      </I18nextProvider>
    </QueryClientProvider>
  );
};

// Render with Router context but without Auth (for leads tests that mock useAuth)
const renderWithRouter = (ui: ReactElement, options?: Omit<RenderOptions, 'wrapper'>) =>
  render(ui, { wrapper: MinimalRouterProvider, ...options });

// Re-export everything
export * from '@testing-library/react';
export {
  customRender as render,
  renderWithRouter,
  createTestQueryClient,
  testTheme,
  AllTheProviders,
  MinimalRouterProvider,
};
