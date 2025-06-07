import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { Routes, Route } from 'react-router-dom';
import './styles/globals.css';
import App from './App.tsx';
import { LoginBypassPage } from './pages/LoginBypassPage.tsx';
import { UsersPage } from './pages/UsersPage.tsx';
import { CalculatorPage } from './pages/CalculatorPage.tsx';
import { AppProviders } from './app/providers.tsx';

// Only include login bypass in development mode
// SECURITY: Never include this route in production builds!
const isDevelopmentMode = import.meta.env.DEV && import.meta.env.MODE !== 'production';

const rootElement = document.getElementById('root');
if (!rootElement) {
  throw new Error('Root element not found');
}

createRoot(rootElement).render(
  <StrictMode>
    <AppProviders>
      <Routes>
        <Route path="/" element={<App />} />
        <Route path="/users" element={<UsersPage />} />
        <Route path="/calculator" element={<CalculatorPage />} />
        {isDevelopmentMode && <Route path="/login-bypass" element={<LoginBypassPage />} />}
      </Routes>
    </AppProviders>
  </StrictMode>
);
