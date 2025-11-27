import '@testing-library/jest-dom';
import { cleanup, configure } from '@testing-library/react';
import { afterEach, beforeAll, afterAll, vi } from 'vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import React from 'react';
import { server } from '@/mocks/server';

// Configure Testing Library with appropriate timeouts
configure({
  asyncUtilTimeout: 5000, // 5 seconds for async operations (MSW + React Query need time)
  computedStyleSupportsPseudoElements: false,
});

// MSW Server Setup
beforeAll(() => {
  server.listen({ onUnhandledRequest: 'warn' });
});

afterEach(() => {
  cleanup();
  server.resetHandlers();
});

afterAll(() => {
  server.close();
});

// Mock für window.matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
});

// Mock für ResizeObserver (needed for Radix UI components)
global.ResizeObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}));

// Mock für IntersectionObserver
global.IntersectionObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}));

// Mock für import.meta.env
Object.defineProperty(import.meta, 'env', {
  value: {
    DEV: true,
    PROD: false,
    MODE: 'test',
    VITE_API_URL: 'http://localhost:8080',
    VITE_KEYCLOAK_URL: 'http://localhost:8180',
    VITE_KEYCLOAK_REALM: 'test-realm',
    VITE_KEYCLOAK_CLIENT: 'test-client',
    VITE_TEST_USER_EMAIL: 'test@example.com',
    VITE_TEST_USER_PASSWORD: 'test-password',
  },
});

// Mock missing stores
vi.mock('/src/features/customers/stores/customerOnboardingStore', () => ({
  useCustomerOnboardingStore: () => ({
    // Field management
    fieldValues: {},
    setFieldValue: vi.fn(),
    setLocationFieldValue: vi.fn(),
    setCustomerField: vi.fn(),
    getFieldValue: vi.fn(() => ''),

    // Location management
    locations: [],
    addLocation: vi.fn(),
    removeLocation: vi.fn(),
    locationFieldValues: {},
    setLocationField: vi.fn(),

    // Detailed locations
    detailedLocations: [],
    addDetailedLocation: vi.fn(),
    addBatchDetailedLocations: vi.fn(),

    // Customer data
    customerData: {},
    customer: null,

    // Field definitions
    fieldDefinitions: [],
    customerFields: [],
    locationFields: [],
    setFieldDefinitions: vi.fn(),

    // Validation
    validate: vi.fn(() => true),
    validateField: vi.fn(),
    validateCurrentStep: vi.fn(() => true),
    validationErrors: {},
    errors: {},
    isValid: true,

    // Contacts management
    contacts: [],
    addContact: vi.fn(),
    updateContact: vi.fn(),
    removeContact: vi.fn(),
    setPrimaryContact: vi.fn(),
    validateContacts: vi.fn().mockResolvedValue(true),
    contactValidationErrors: {},
    validateContactField: vi.fn(),

    // Wizard flow
    currentStep: 0,
    setCurrentStep: vi.fn(),
    canProgressToNextStep: vi.fn(() => false),

    // State management
    isDirty: false,
    lastSaved: null,
    draftId: null,
    reset: vi.fn(),

    // Draft management
    saveAsDraft: vi.fn().mockResolvedValue(undefined),
    loadDraft: vi.fn().mockResolvedValue(undefined),
  }),
}));

// Mock CustomerFieldThemeProvider
vi.mock('/src/features/customers/theme/CustomerFieldThemeProvider', () => ({
  CustomerFieldThemeProvider: ({ children }: { children: React.ReactNode }) => children,
  useCustomerFieldTheme: () => ({
    theme: {
      colors: {
        primary: '#94C456',
        secondary: '#004F7B',
      },
    },
    cssVariables: {},
  }),
}));

// QueryClient für Tests erstellen
const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        gcTime: 0, // Updated from cacheTime
        staleTime: 0,
      },
      mutations: {
        retry: false,
      },
    },
  });

// Test-Wrapper für Komponenten mit QueryClient
// NOTE: Use test-utils.tsx render() instead of this wrapper for component tests
// This is kept for backward compatibility with tests that explicitly use TestWrapper
export const TestWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const queryClient = createTestQueryClient();
  return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
};
