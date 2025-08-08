/**
 * Authenticated Layout Wrapper
 *
 * Provides consistent layout for all authenticated pages with:
 * - MainLayout with Sidebar Navigation
 * - Theme Provider with Freshfoodz CI
 * - Auth Guard protection
 */

import React from 'react';
import { Navigate } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import { useAuth } from '@/hooks/useAuth';
import { MainLayout } from './MainLayout';
import { freshfoodzTheme } from '@/theme/freshfoodz';

interface AuthenticatedLayoutProps {
  children: React.ReactNode;
}

export const AuthenticatedLayout: React.FC<AuthenticatedLayoutProps> = ({ children }) => {
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
          <h2>Loading authentication...</h2>
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
      <MainLayout>{children}</MainLayout>
    </ThemeProvider>
  );
};
