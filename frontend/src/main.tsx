import { StrictMode, Suspense } from 'react';
import { createRoot } from 'react-dom/client';
import './i18n'; // i18n vor allen anderen Imports!
import './styles/globals.css'; // Legacy base styles
import './index.css'; // FreshPlan CI Design System (überschreibt Legacy)
import './styles/variables-mapping.css'; // Map legacy variables to new FreshPlan CI
import './styles/animations.css'; // Global animations for highlighting
import { AppProviders } from './providers.tsx';

// Enable MSW for development if backend is not available
async function enableMocking() {
  // Security: MSW must be explicitly enabled via environment variable
  const USE_MSW = import.meta.env.VITE_USE_MSW === 'true';

  // Security: Never use MSW in production
  if (!import.meta.env.DEV) {
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

// TEMPORARY FIX: MSW Service Worker blocks Lead POST requests with 400
// Disable MSW completely until we fix the handler configuration
// enableMocking()
//   .then(() => {
createRoot(rootElement).render(
  <StrictMode>
    <Suspense fallback={<div>Loading...</div>}>
      <AppProviders />
    </Suspense>
  </StrictMode>
);
//   })
//   .catch(_error => {
//     // Failed to initialize app - error handled
//     // Render app anyway - don't let initialization errors block the entire app
//     createRoot(rootElement).render(
//       <StrictMode>
//         <Suspense fallback={<div>Loading...</div>}>
//           <AppProviders />
//         </Suspense>
//       </StrictMode>
//     );
//   });
