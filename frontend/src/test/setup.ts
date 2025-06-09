import '@testing-library/jest-dom';
import { cleanup } from '@testing-library/react';
import { afterEach } from 'vitest';

// Cleanup nach jedem Test
afterEach(() => {
  cleanup();
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

// Mock für import.meta.env
Object.defineProperty(import.meta, 'env', {
  value: {
    DEV: true,
    PROD: false,
    MODE: 'test',
    VITEST: true,
    VITE_API_URL: 'http://localhost:8080',
    VITE_KEYCLOAK_URL: 'http://localhost:8180',
    VITE_KEYCLOAK_REALM: 'test-realm',
    VITE_KEYCLOAK_CLIENT: 'test-client',
    VITE_TEST_USER_EMAIL: 'test@example.com',
    VITE_TEST_USER_PASSWORD: 'test-password',
  },
});
