/**
 * Universal Layout - Works in both Dev and Production mode
 * Provides consistent layout with sidebar navigation
 */

import React from 'react';
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import { MainLayout } from './MainLayout';
import freshfoodzTheme from '@/theme/freshfoodz';

interface UniversalLayoutProps {
  children: React.ReactNode;
}

export const UniversalLayout: React.FC<UniversalLayoutProps> = ({ 
  children 
}) => {
  // No auth check - just provide the layout
  // Note: ThemeProvider should already be provided by parent
  return (
    <MainLayout>
      {children}
    </MainLayout>
  );
};