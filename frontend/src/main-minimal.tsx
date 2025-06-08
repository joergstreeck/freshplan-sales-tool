import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';

function MinimalApp() {
  return <h1>Minimal App Works!</h1>;
}

const rootElement = document.getElementById('root');
if (!rootElement) {
  throw new Error('Root element not found');
}

createRoot(rootElement).render(
  <StrictMode>
    <MinimalApp />
  </StrictMode>
);