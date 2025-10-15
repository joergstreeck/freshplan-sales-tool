/**
 * CockpitPageV2 - Test Page mit neuem Layout
 *
 * Nutzt MainLayoutV2 und CockpitViewV2 für Clean Slate Test
 */

import React from 'react';
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import { Navigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { MainLayoutV2 } from '@/components/layout/MainLayoutV2';
import { CockpitViewV2 } from './cockpit/CockpitViewV2';
import { freshfoodzTheme } from '@/theme/freshfoodz';

export function CockpitPageV2() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <ThemeProvider theme={freshfoodzTheme}>
        <CssBaseline />
        <div
          style={{
            padding: '20px',
            textAlign: 'center',
            minHeight: '100vh',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <h2>Authentifizierung lädt...</h2>
        </div>
      </ThemeProvider>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  return (
    <ThemeProvider theme={freshfoodzTheme}>
      <CssBaseline />
      <MainLayoutV2 maxWidth="full">
        <CockpitViewV2 />
      </MainLayoutV2>
    </ThemeProvider>
  );
}

export default CockpitPageV2;
