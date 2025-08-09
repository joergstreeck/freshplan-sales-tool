/**
 * Universal Layout - Works in both Dev and Production mode
 * Provides consistent layout with sidebar navigation
 */

import React from 'react';
// import { ThemeProvider } from '@mui/material/styles'; // Already provided by parent
// import { CssBaseline } from '@mui/material'; // Already provided by parent
import { MainLayout } from './MainLayout';
// import freshfoodzTheme from '@/theme/freshfoodz'; // Already provided by parent

interface UniversalLayoutProps {
  children: React.ReactNode;
}

export const UniversalLayout: React.FC<UniversalLayoutProps> = ({ children }) => {
  // No auth check - just provide the layout
  // Note: ThemeProvider should already be provided by parent
  return <MainLayout>{children}</MainLayout>;
};
