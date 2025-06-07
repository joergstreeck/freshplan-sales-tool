import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import './index.css';
import App from './App.tsx';
import { AuthProvider } from './contexts/AuthContext.tsx';
import { LoginBypassPage } from './pages/LoginBypassPage.tsx';
import { ErrorBoundary } from './components/ErrorBoundary.tsx';

// Only include login bypass in test mode
const isTestMode = import.meta.env.MODE === 'test' || import.meta.env.DEV;

const rootElement = document.getElementById('root');
if (!rootElement) {
  throw new Error('Root element not found');
}

createRoot(rootElement).render(
  <StrictMode>
    <ErrorBoundary>
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route path="/" element={<App />} />
            {isTestMode && <Route path="/login-bypass" element={<LoginBypassPage />} />}
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </ErrorBoundary>
  </StrictMode>
);
