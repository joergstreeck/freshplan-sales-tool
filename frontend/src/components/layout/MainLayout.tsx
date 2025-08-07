import React from 'react';
import { Box } from '@mui/material';
import { styled } from '@mui/material/styles';
import { SidebarNavigation } from './SidebarNavigation';
import { useNavigationStore } from '@/store/navigationStore';

const DRAWER_WIDTH = 280;
const DRAWER_WIDTH_COLLAPSED = 64;

interface MainLayoutProps {
  children: React.ReactNode;
}

const MainContainer = styled(Box)(({ theme }) => ({
  display: 'flex',
  minHeight: '100vh',
  backgroundColor: theme.palette.background.default,
}));

const ContentContainer = styled(Box, {
  shouldForwardProp: prop => prop !== 'collapsed',
})<{ collapsed: boolean }>(({ theme, collapsed }) => ({
  flexGrow: 1,
  marginLeft: collapsed ? DRAWER_WIDTH_COLLAPSED : DRAWER_WIDTH,
  transition: theme.transitions.create('margin', {
    easing: theme.transitions.easing.sharp,
    duration: theme.transitions.duration.enteringScreen,
  }),
  padding: 0,
  overflow: 'hidden',
}));

export const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  const { isCollapsed } = useNavigationStore();

  return (
    <MainContainer>
      <SidebarNavigation />
      <ContentContainer collapsed={isCollapsed}>{children}</ContentContainer>
    </MainContainer>
  );
};
