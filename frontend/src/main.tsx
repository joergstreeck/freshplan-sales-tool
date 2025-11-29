import { StrictMode, Suspense } from 'react';
import { createRoot } from 'react-dom/client';
import './i18n'; // i18n vor allen anderen Imports!
import './styles/globals.css'; // Legacy base styles
import './index.css'; // FreshPlan CI Design System (überschreibt Legacy)
import './styles/variables-mapping.css'; // Map legacy variables to new FreshPlan CI
import './styles/animations.css'; // Global animations for highlighting
import { AppProviders } from './providers.tsx';

// Enable MSW for development or E2E testing
async function enableMocking() {
  // Security: MSW can be enabled via:
  // - VITE_USE_MSW=true (for local development)
  // - VITE_E2E_MODE=true (for E2E tests in CI - works with production builds)
  const USE_MSW = import.meta.env.VITE_USE_MSW === 'true';
  const E2E_MODE = import.meta.env.VITE_E2E_MODE === 'true';

  // Enable MSW if either flag is set
  // Note: E2E_MODE is baked into the build at compile time, allowing MSW in production builds
  const shouldEnableMSW = USE_MSW || E2E_MODE;

  if (!shouldEnableMSW) {
    // MSW disabled: Using real backend API
    return;
  }

  // Security: Set mock token for authentication bypass in E2E mode
  if (E2E_MODE) {
    const mockToken = `e2e_mock_${Date.now()}_${Math.random().toString(36).substring(7)}`;
    localStorage.setItem('auth-token', mockToken);
  }

  try {
    // Dynamic import of MSW browser module
    const { worker } = await import('./mocks/browser');

    // Start the worker with bypass for unhandled requests
    // This allows real backend calls to pass through when needed
    return worker.start({
      onUnhandledRequest: 'bypass',
      serviceWorker: {
        url: '/mockServiceWorker.js',
      },
    });
  } catch (error) {
    // MSW failed to start - continue without mocking
    console.warn('[MSW] Failed to start:', error);
    return;
  }
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
