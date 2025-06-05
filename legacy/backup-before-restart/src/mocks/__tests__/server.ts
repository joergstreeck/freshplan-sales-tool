/**
 * MSW Server Setup for Node.js (Testing)
 */

import { setupServer } from 'msw/node';
import { handlers } from './handlers';

// Create server instance
export const server = setupServer(...handlers);

// Reset handlers after each test
export function resetHandlers() {
  server.resetHandlers();
}

// Add custom handlers for specific tests
export function useHandlers(...customHandlers: any[]) {
  server.use(...customHandlers);
}