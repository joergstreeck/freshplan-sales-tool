/**
 * MSW Browser Setup (for development)
 */

import { setupWorker } from 'msw/browser';
import { handlers } from './handlers';

// Create browser worker instance
export const worker = setupWorker(...handlers);

// Start worker function
export async function startWorker() {
  if (typeof window !== 'undefined' && import.meta.env.DEV) {
    return worker.start({
      onUnhandledRequest: 'bypass',
    });
  }
}