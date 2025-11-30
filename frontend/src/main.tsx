import { StrictMode, Suspense } from 'react';
import { createRoot } from 'react-dom/client';
import './i18n'; // i18n vor allen anderen Imports!
import './styles/globals.css'; // Legacy base styles
import './index.css'; // FreshPlan CI Design System (überschreibt Legacy)
import './styles/variables-mapping.css'; // Map legacy variables to new FreshPlan CI
import './styles/animations.css'; // Global animations for highlighting
import { AppProviders } from './providers.tsx';

// Enable MSW for development or CI E2E testing
async function enableMocking() {
  // Security: MSW must be explicitly enabled via environment variable
  const USE_MSW = import.meta.env.VITE_USE_MSW === 'true';

  // Security: Only enable mocking in development OR explicit E2E mode
  // - DEV: Local development with `npm run dev`
  // - VITE_E2E_MODE: CI builds for Stage 2 (UI Smoke Tests with MSW)
  // In real production, both are false -> MSW code is dead-code-eliminated
  const shouldEnableMocks = import.meta.env.DEV || import.meta.env.VITE_E2E_MODE === 'true';

  if (!shouldEnableMocks) {
    return;
  }

  // Security: Only set mock token when MSW is explicitly enabled
  if (USE_MSW) {
    // Security: Use a more secure mock token format
    const mockToken = `mock_${Date.now()}_${Math.random().toString(36).substring(7)}`;
    localStorage.setItem('auth-token', mockToken);

    if (import.meta.env.DEV) {
      // MSW enabled with mock authentication
    }
  } else {
    // Security: Always clean up mock tokens when MSW is disabled
    localStorage.removeItem('auth-token');

    if (import.meta.env.DEV) {
      // MSW disabled: Using real backend API
    }
    return;
  }

  // For now, always use MSW in development when enabled
  // TODO: Add backend detection when backend is implemented

  const { worker } = await import('./mocks/browser');

  // Start the worker
  return worker.start({
    onUnhandledRequest: 'bypass',
    serviceWorker: {
      url: '/mockServiceWorker.js',
    },
  });
}

const rootElement = document.getElementById('root');
if (!rootElement) {
  throw new Error('Root element not found');
}

// Enable MSW in development mode (only if VITE_USE_MSW=true)
// MSW handlers use 'bypass' for unhandled requests, so real backend is always used
enableMocking()
  .then(() => {
    createRoot(rootElement).render(
      <StrictMode>
        <Suspense fallback={<div>Loading...</div>}>
          <AppProviders />
        </Suspense>
      </StrictMode>
    );
  })
  .catch(_error => {
    // Failed to initialize MSW - render app anyway with real backend
    createRoot(rootElement).render(
      <StrictMode>
        <Suspense fallback={<div>Loading...</div>}>
          <AppProviders />
        </Suspense>
      </StrictMode>
    );
  });
