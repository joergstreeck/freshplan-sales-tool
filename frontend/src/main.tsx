import { StrictMode, Suspense } from 'react';
import { createRoot } from 'react-dom/client';
import { Routes, Route } from 'react-router-dom';
import './i18n'; // i18n vor allen anderen Imports!
import './styles/globals.css'; // Legacy base styles
import './index.css'; // FreshPlan CI Design System (überschreibt Legacy)
import './styles/variables-mapping.css'; // Map legacy variables to new FreshPlan CI
import App from './App.tsx';
import { LoginBypassPage } from './pages/LoginBypassPage.tsx';
import { UsersPage } from './pages/UsersPage.tsx';
import { LegacyToolPage } from './pages/LegacyToolPage.tsx';
import { IntegrationTestPage } from './pages/IntegrationTestPage.tsx';
import CustomersPage from './pages/CustomersPage.tsx';
import { SalesCockpit } from './features/cockpit/components/SalesCockpit.tsx';
import { AppProviders } from './providers.tsx';

// Only include login bypass in development mode
// SECURITY: Never include this route in production builds!
const isDevelopmentMode = import.meta.env.DEV && import.meta.env.MODE !== 'production';

// Enable MSW for development if backend is not available
async function enableMocking() {
  if (!import.meta.env.DEV) {
    return;
  }

  // Check if backend is available
  try {
    const response = await fetch('http://localhost:8080/api/ping', {
      method: 'GET',
      signal: AbortSignal.timeout(1000), // 1 second timeout
    });
    if (response.ok) {
      console.log('✅ Backend is available, using real API');
      return;
    }
  } catch {
    console.log('⚠️ Backend not available, starting MSW mock server');
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

// Start the app with optional mocking
enableMocking().then(() => {
  createRoot(rootElement).render(
    <StrictMode>
      <Suspense fallback={<div>Loading...</div>}>
        <AppProviders>
          <Routes>
            <Route path="/" element={<App />} />
            <Route path="/cockpit" element={<SalesCockpit />} />
            <Route path="/users" element={<UsersPage />} />
            <Route path="/customers" element={<CustomersPage />} />
            <Route path="/legacy-tool" element={<LegacyToolPage />} />
            {/* Login Bypass temporär reaktiviert - Auto-Login Problem */}
            {isDevelopmentMode && <Route path="/login-bypass" element={<LoginBypassPage />} />}
            {isDevelopmentMode && (
              <Route path="/integration-test" element={<IntegrationTestPage />} />
            )}
          </Routes>
        </AppProviders>
      </Suspense>
    </StrictMode>
  );
});
