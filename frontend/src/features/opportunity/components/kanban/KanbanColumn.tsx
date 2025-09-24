import React from 'react';
import { Paper, Box, Typography, Badge } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import { useDroppable } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';

import { OpportunityStage, type Opportunity } from '../../types';
import { STAGE_CONFIGURATIONS } from '../../config/stage-config';
import { SortableOpportunityCard } from '../SortableOpportunityCard';

// Convert STAGE_CONFIGURATIONS array to Record for easier lookup
const STAGE_CONFIGS_RECORD: Record<string, (typeof STAGE_CONFIGURATIONS)[0]> = {};
STAGE_CONFIGURATIONS.forEach(config => {
  STAGE_CONFIGS_RECORD[config.stage] = config;
});

interface KanbanColumnProps {
  /** The stage this column represents */
  stage: OpportunityStage;
  /** Opportunities in this stage */
  opportunities: Opportunity[];
  /** Callback for quick actions */
  onQuickAction?: (opportunityId: string, action: 'won' | 'lost' | 'reactivate') => void;
  /** Set of opportunity IDs currently animating */
  animatingIds: Set<string>;
}

export const KanbanColumn: React.FC<KanbanColumnProps> = React.memo(
  ({ stage, opportunities, onQuickAction, animatingIds }) => {
    const theme = useTheme();
    const config = STAGE_CONFIGS_RECORD[stage];
    const totalValue = opportunities.reduce((sum, opp) => sum + (opp.value || 0), 0);

    // Make the column a drop target
    const { setNodeRef, isOver } = useDroppable({
      id: stage,
    });

    return (
      <Paper
        ref={setNodeRef}
        sx={{
          minHeight: 400,
          flex: '1 1 0',
          minWidth: { xs: 260, sm: 280 }, // Responsive Breite
          maxWidth: { xs: 350, sm: 450 },
          p: { xs: 1.5, sm: 2 },
          bgcolor: isOver ? 'rgba(148, 196, 86, 0.1)' : config.bgColor,
          border: isOver
            ? `2px solid ${theme.palette.primary.main}`
            : '1px solid rgba(0, 0, 0, 0.12)',
          borderRadius: 2,
          transition: 'all 0.2s ease',
        }}
      >
        {/* Column Header */}
        <Box sx={{ mb: 2 }}>
          <Box
            sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}
          >
            <Typography
              variant="h6"
              sx={{
                color: config.color,
              }}
            >
              {config.label}
            </Typography>

            <Badge
              badgeContent={opportunities.length}
              color="primary"
              sx={{
                '& .MuiBadge-badge': {
                  bgcolor: config.color,
                  color: 'white',
                  fontWeight: 'bold',
                },
              }}
            />
          </Box>

          {totalValue > 0 && (
            <Typography
              variant="body2"
              sx={{ color: theme.palette.text.secondary, fontWeight: 'medium' }}
            >
              Gesamt:{' '}
              {new Intl.NumberFormat('de-DE', {
                style: 'currency',
                currency: 'EUR',
                minimumFractionDigits: 0,
              }).format(totalValue)}
            </Typography>
          )}
        </Box>

        {/* Sortable Area */}
        <SortableContext
          items={opportunities.map(o => o.id)}
          strategy={verticalListSortingStrategy}
        >
          <Box sx={{ minHeight: 400, maxHeight: 'calc(100vh - 400px)', overflowY: 'auto' }}>
            {opportunities.map(opportunity => (
              <SortableOpportunityCard
                key={opportunity.id}
                opportunity={opportunity}
                onQuickAction={onQuickAction}
                isAnimating={animatingIds.has(opportunity.id)}
              />
            ))}
          </Box>
        </SortableContext>
      </Paper>
    );
  }
);

KanbanColumn.displayName = 'KanbanColumn';
