import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import './index.css';
import App from './App.tsx';
import { AuthProvider } from './contexts/AuthContext.tsx';
import { LoginBypassPage } from './pages/LoginBypassPage.tsx';

// Only include login bypass in test mode
const isTestMode = import.meta.env.MODE === 'test' || import.meta.env.DEV;

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<App />} />
          {isTestMode && (
            <Route path="/login-bypass" element={<LoginBypassPage />} />
          )}
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>
);
