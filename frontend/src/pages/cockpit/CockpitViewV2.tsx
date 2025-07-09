/**
 * CockpitViewV2 - MUI-basierte Cockpit-Implementierung
 * 
 * Clean Slate Implementation ohne CSS-Dateien
 * Nutzt ausschließlich MUI's sx-Prop und Theme
 */

import React from 'react';
import { Box, Grid, Paper, Typography, Container } from '@mui/material';
import { styled } from '@mui/material/styles';
import { useCockpitStore } from '@/store/cockpitStore';
import { SalesCockpitMUI } from '@/features/cockpit/components/SalesCockpitMUI';

// Styled Components für wiederverwendbare Styles
const ContentPaper = styled(Paper)(({ theme }) => ({
  padding: theme.spacing(3),
  height: '100%',
  overflow: 'auto',
  backgroundColor: theme.palette.background.paper,
  boxShadow: theme.shadows[1],
}));

const PageContainer = styled(Box)(({ theme }) => ({
  // Reset any inherited styles
  position: 'relative',
  width: '100%',
  height: '100%',
  // Eigener Scroll-Context
  overflow: 'auto',
  // Verhindert Layout-Bleed
  contain: 'layout style',
}));

export function CockpitViewV2() {
  // Temporär: Nutze die existierende SalesCockpitMUI Komponente
  // Diese wird in Phase 2 durch einzelne MUI-Komponenten ersetzt
  
  return (
    <PageContainer>
      <Container maxWidth={false} sx={{ height: '100%', p: 0 }}>
        <SalesCockpitMUI />
      </Container>
    </PageContainer>
  );
}

export default CockpitViewV2;