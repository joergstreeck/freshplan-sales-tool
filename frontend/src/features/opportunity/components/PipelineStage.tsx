import React from 'react';
import { 
  Box, 
  Paper, 
  Typography, 
  Badge,
  Stack 
} from '@mui/material';
import { useTheme } from '@mui/material/styles';
import { useDroppable } from '@dnd-kit/core';

import { OpportunityStage } from '../types/stages';

interface StageConfig {
  stage: OpportunityStage;
  label: string;
  color: string;
  bgColor: string;
}

// Freshfoodz Corporate Identity Stage-Farben
const STAGE_CONFIGS: Record<OpportunityStage, StageConfig> = {
  [OpportunityStage.LEAD]: {
    stage: OpportunityStage.LEAD,
    label: 'Lead',
    color: '#004F7B', // Freshfoodz Blau
    bgColor: 'rgba(0, 79, 123, 0.05)'
  },
  [OpportunityStage.NEEDS_ANALYSIS]: {
    stage: OpportunityStage.NEEDS_ANALYSIS,
    label: 'Bedarfsanalyse',
    color: '#94C456', // Freshfoodz Grün
    bgColor: 'rgba(148, 196, 86, 0.05)'
  },
  [OpportunityStage.PROPOSAL]: {
    stage: OpportunityStage.PROPOSAL,
    label: 'Angebot',
    color: '#FFA726', // Orange für Aktivität
    bgColor: 'rgba(255, 167, 38, 0.05)'
  },
  [OpportunityStage.NEGOTIATION]: {
    stage: OpportunityStage.NEGOTIATION,
    label: 'Verhandlung',
    color: '#FF7043', // Orange-Rot für Dringlichkeit
    bgColor: 'rgba(255, 112, 67, 0.05)'
  },
  [OpportunityStage.CLOSED_WON]: {
    stage: OpportunityStage.CLOSED_WON,
    label: 'Gewonnen',
    color: '#66BB6A', // Grün für Erfolg
    bgColor: 'rgba(102, 187, 106, 0.05)'
  },
  [OpportunityStage.CLOSED_LOST]: {
    stage: OpportunityStage.CLOSED_LOST,
    label: 'Verloren',
    color: '#EF5350', // Rot für Verlust
    bgColor: 'rgba(239, 83, 80, 0.05)'
  }
};

interface PipelineStageProps {
  stage: OpportunityStage;
  opportunityCount: number;
  totalValue?: number;
  children: React.ReactNode;
}

export const PipelineStage: React.FC<PipelineStageProps> = ({
  stage,
  opportunityCount,
  totalValue,
  children
}) => {
  const theme = useTheme();
  const config = STAGE_CONFIGS[stage];
  
  // Drag & Drop Setup
  const { setNodeRef, isOver } = useDroppable({
    id: stage,
  });

  return (
    <Paper
      ref={setNodeRef}
      elevation={isOver ? 4 : 1}
      sx={{
        minHeight: 400,
        p: 2,
        bgcolor: isOver ? 'rgba(148, 196, 86, 0.12)' : config.bgColor,
        border: isOver ? '3px dashed #94C456' : '1px solid rgba(0, 0, 0, 0.12)',
        borderRadius: 2,
        transition: theme.transitions.create(['border', 'background-color', 'box-shadow', 'transform'], {
          duration: theme.transitions.duration.short,
        }),
        transform: isOver ? 'scale(1.02)' : 'scale(1)',
        boxShadow: isOver ? theme.shadows[4] : theme.shadows[1],
        width: '280px', // Feste Breite für gleichmäßige Spalten
        minWidth: '280px',
      }}
    >
      {/* Stage Header */}
      <Box sx={{ mb: 2 }}>
        <Box sx={{ 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'space-between',
          mb: 1 
        }}>
          <Typography
            variant="h6"
            sx={{
              color: config.color,
              fontFamily: 'Antonio, sans-serif',
              fontWeight: 700,
              fontSize: '1.1rem',
            }}
          >
            {config.label}
          </Typography>
          
          <Badge
            badgeContent={opportunityCount}
            color="primary"
            sx={{
              '& .MuiBadge-badge': {
                bgcolor: config.color,
                color: 'white',
                fontWeight: 600,
              }
            }}
          />
        </Box>
        
        {/* Total Value (optional) */}
        {totalValue !== undefined && (
          <Typography
            variant="body2"
            sx={{
              color: theme.palette.text.secondary,
              fontWeight: 500,
            }}
          >
            Gesamt: {new Intl.NumberFormat('de-DE', {
              style: 'currency',
              currency: 'EUR',
              minimumFractionDigits: 0,
            }).format(totalValue)}
          </Typography>
        )}
      </Box>

      {/* Opportunities Container */}
      <Stack spacing={1.5}>
        {children}
      </Stack>
    </Paper>
  );
};

// Export Stage Configs für externe Verwendung
export { STAGE_CONFIGS };