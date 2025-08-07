/**
 * Global test utilities for all tests
 */

import React from 'react';
import { render as rtlRender, RenderOptions } from '@testing-library/react';
import { vi } from 'vitest';

// Mock the customer field theme provider for tests that need it
const MockCustomerFieldThemeProvider = ({ children }: { children: React.ReactNode }) => {
  return <div data-testid="theme-provider">{children}</div>;
};

// Custom render function with providers
interface CustomRenderOptions extends Omit<RenderOptions, 'wrapper'> {
  withTheme?: boolean;
}

export function render(ui: React.ReactElement, options: CustomRenderOptions = {}) {
  const { withTheme = false, ...renderOptions } = options;

  function AllTheProviders({ children }: { children: React.ReactNode }) {
    if (withTheme) {
      return <MockCustomerFieldThemeProvider>{children}</MockCustomerFieldThemeProvider>;
    }
    return <>{children}</>;
  }

  return rtlRender(ui, { wrapper: AllTheProviders, ...renderOptions });
}

// Mock functions
export const createMockFunction = () => vi.fn();

// Common test data factories
export const createMockField = (overrides = {}) => ({
  key: 'test-field',
  label: 'Test Field',
  fieldType: 'text',
  required: false,
  placeholder: 'Enter text...',
  ...overrides,
});

export const createMockCustomer = (overrides = {}) => ({
  id: 'test-customer-id',
  customerNumber: '12345',
  companyName: 'Test Customer',
  industry: 'hotel',
  fieldValues: {},
  locations: [],
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
  ...overrides,
});

// Re-export everything
export * from '@testing-library/react';
export { default as userEvent } from '@testing-library/user-event';
