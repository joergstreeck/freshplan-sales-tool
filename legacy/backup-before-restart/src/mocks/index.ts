/**
 * MSW Mock Export
 * Provides unified interface for mocks in different environments
 */

export { handlers, errorHandlers, delayHandlers } from './handlers';

// Export worker for browser development
export { worker } from './browser';

// Helper to start mocks in development
export async function startMocks() {
  if (typeof window === 'undefined') {
    // Node environment - server is started in setup-tests.ts
    return;
  }
  
  // Browser environment
  const { worker } = await import('./browser');
  return worker.start({
    onUnhandledRequest: 'bypass',
  });
}