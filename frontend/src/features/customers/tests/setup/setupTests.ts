/**
 * @fileoverview Test Setup für FC-005 Customer Management
 * @module tests/setup/setupTests
 *
 * Zentrale Test-Konfiguration für alle Test-Typen:
 * - Vitest Environment Setup
 * - MSW (Mock Service Worker) Initialisierung
 * - React Testing Library Konfiguration
 * - Global Test Utilities
 */

import '@testing-library/jest-dom';
import { cleanup, render as rtlRender } from '@testing-library/react';
import { afterEach, beforeAll, afterAll, vi } from 'vitest';
import React from 'react';
import { server } from './mockServer';
import { CustomerFieldThemeProvider } from '../../theme/CustomerFieldThemeProvider';

// =============================================================================
// MSW Server Setup
// =============================================================================

/**
 * Start MSW server before all tests
 */
beforeAll(() => {
  server.listen({
    onUnhandledRequest: 'error',
  });
});

/**
 * Reset handlers after each test
 */
afterEach(() => {
  server.resetHandlers();
  cleanup();
});

/**
 * Close server after all tests
 */
afterAll(() => {
  server.close();
});

// =============================================================================
// Global Mocks
// =============================================================================

/**
 * Mock window.matchMedia
 */
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

/**
 * Mock IntersectionObserver
 */
global.IntersectionObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}));

/**
 * Mock ResizeObserver
 */
global.ResizeObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}));

// =============================================================================
// Test Environment Configuration
// =============================================================================

/**
 * Set test environment variables
 */
process.env.NODE_ENV = 'test';
process.env.VITE_API_URL = 'http://localhost:8080';

/**
 * Configure console output
 */
const originalError = console.error;
beforeAll(() => {
  console.error = (...args: any[]) => {
    if (typeof args[0] === 'string' && args[0].includes('Consider adding an error boundary')) {
      return;
    }
    originalError.call(console, ...args);
  };
});

afterAll(() => {
  console.error = originalError;
});

// =============================================================================
// Custom Test Matchers
// =============================================================================

/**
 * Custom matcher for checking field visibility
 */
expect.extend({
  toBeVisibleField(received, fieldId: string) {
    const field = received.querySelector(`[data-field-id="${fieldId}"]`);
    const pass = field !== null && !field.classList.contains('hidden');

    return {
      pass,
      message: () =>
        pass
          ? `Expected field ${fieldId} not to be visible`
          : `Expected field ${fieldId} to be visible`,
    };
  },
});

// =============================================================================
// Global Test Utilities
// =============================================================================

/**
 * Wait for async operations
 */
export const waitForAsync = (ms: number = 100) => new Promise(resolve => setTimeout(resolve, ms));

/**
 * Mock API delay
 */
export const mockApiDelay = (ms: number = 50) => new Promise(resolve => setTimeout(resolve, ms));

/**
 * Create mock field definition
 */
export const createMockFieldDefinition = (overrides = {}) => ({
  id: 'test-field',
  name: 'Test Field',
  label: 'Test Field',
  type: 'text',
  required: false,
  visible: true,
  editable: true,
  category: 'basic',
  industry: 'all',
  ...overrides,
});

/**
 * Create mock customer
 */
export const createMockCustomer = (overrides = {}) => ({
  id: 'test-customer-id',
  customerNumber: '12345',
  name: 'Test Customer',
  industry: 'hotel',
  fieldValues: {},
  locations: [],
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
  ...overrides,
});

/**
 * Custom render function that includes CustomerFieldThemeProvider
 */
interface RenderOptions {
  wrapperProps?: {
    mode?: 'standard' | 'anpassungsfähig';
  };
}

export const renderWithTheme = (ui: React.ReactElement, options: RenderOptions = {}) => {
  const { wrapperProps = {} } = options;

  const AllTheProviders = ({ children }: { children: React.ReactNode }) => {
    return React.createElement(CustomerFieldThemeProvider, wrapperProps, children);
  };

  return rtlRender(ui, { wrapper: AllTheProviders, ...options });
};

// Re-export everything from @testing-library/react
export * from '@testing-library/react';
// Override render method
export { renderWithTheme as render };

// Type augmentation for custom matchers
declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace Vi {
    interface Assertion {
      toBeVisibleField(fieldId: string): void;
    }
  }
}
