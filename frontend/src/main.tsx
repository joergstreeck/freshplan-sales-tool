import { StrictMode, Suspense } from 'react';
import { createRoot } from 'react-dom/client';
import './i18n'; // i18n vor allen anderen Imports!
import './styles/legacy/variables.css'; // Legacy design system variables - must be first
import './styles/design-tokens.css'; // Design tokens - must be before files that use them
import './styles/globals.css'; // Legacy base styles
import './index.css'; // FreshPlan CI Design System (überschreibt Legacy)
import './styles/variables-mapping.css'; // Map legacy variables to new FreshPlan CI
import './styles/freshplan-design-system.css'; // Main design system
import { AppProviders } from './providers.tsx';
import { AUTH_TOKEN_KEY, MOCK_JWT_TOKEN, BACKEND_BASE_URL, PING_ENDPOINT, BACKEND_CHECK_TIMEOUT } from './constants';

// Enable MSW for development if backend is not available
async function enableMocking() {
  if (!import.meta.env.DEV) {
    // In production/preview mode, don't use MSW but still check backend availability
    // This prevents the app from hanging if backend is slow to start
    console.log('Production mode: Skipping MSW setup');
    return;
  }

  // Set mock token for development
  localStorage.setItem(AUTH_TOKEN_KEY, MOCK_JWT_TOKEN);

  // Check if backend is available with longer timeout for CI
  try {
    const response = await fetch(`${BACKEND_BASE_URL}${PING_ENDPOINT}`, {
      method: 'GET',
      signal: AbortSignal.timeout(BACKEND_CHECK_TIMEOUT),
    });
    if (response.ok) {
      // Backend is available, using real API
      console.log('Backend available: Using real API');
      return;
    }
  } catch (error) {
    // Backend not available, starting MSW mock server
    console.log('Backend not available, starting MSW:', error);
  }

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

// Function to render the React app - eliminates DRY violation
const renderApp = () => {
  createRoot(rootElement).render(
    <StrictMode>
      <Suspense fallback={<div>Loading...</div>}>
        <AppProviders />
      </Suspense>
    </StrictMode>
  );
};

// Start the app with optional mocking
enableMocking()
  .catch((error) => {
    console.error('Failed to initialize app:', error);
    // Render app anyway - don't let initialization errors block the entire app
  })
  .finally(() => {
    renderApp();
  });
