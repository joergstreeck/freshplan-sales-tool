/**
 * Test Setup File
 * Configure MSW and other test utilities
 */

import { beforeAll, afterEach, afterAll, vi } from 'vitest';

// Only import server in test environment
let server: any;

if (import.meta.env.MODE === 'test') {
  const { server: mswServer } = await import('./mocks/__tests__/server');
  server = mswServer;
}

// Start MSW server before tests
beforeAll(() => {
  if (server) {
    server.listen({ onUnhandledRequest: 'error' });
  }
});

// Reset handlers after each test
afterEach(() => {
  if (server) {
    server.resetHandlers();
  }
});

// Clean up after tests
afterAll(() => {
  if (server) {
    server.close();
  }
});

// Mock DOM APIs if needed
if (typeof window !== 'undefined') {
  // Mock matchMedia
  Object.defineProperty(window, 'matchMedia', {
    writable: true,
    value: vi.fn().mockImplementation((query: string) => ({
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
}