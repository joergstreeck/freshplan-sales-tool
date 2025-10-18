import React, { useMemo } from 'react';
import { Box, Paper, Typography, Badge } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import { useDroppable } from '@dnd-kit/core';

import { OpportunityStage } from '../types/opportunity.types';
import { STAGE_CONFIGURATIONS } from '../config/stage-config';
import { logger } from '../../../lib/logger';
import { useErrorHandler } from '../../../components/ErrorBoundary';

// Component logger instance
const componentLogger = logger.child('PipelineStage');

// Convert STAGE_CONFIGURATIONS array to Record for easier lookup (memoized at module level)
const STAGE_CONFIGS_RECORD: Record<string, (typeof STAGE_CONFIGURATIONS)[0]> =
  STAGE_CONFIGURATIONS.reduce(
    (acc, config) => {
      acc[config.stage] = config;
      return acc;
    },
    {} as Record<string, (typeof STAGE_CONFIGS)[0]>
  );

/**
 * Pipeline Stage Component Props
 * @interface PipelineStageProps
 */
interface PipelineStageProps {
  /** The pipeline stage to render */
  stage: OpportunityStage;
  /** Number of opportunities in this stage */
  opportunityCount: number;
  /** Total monetary value of opportunities in stage */
  totalValue?: number;
  /** Opportunity cards to display in this stage */
  children: React.ReactNode;
}

/**
 * PipelineStage Component
 * @description Enterprise-grade pipeline stage container with drag & drop support
 * Features:
 * - Drag & drop target via @dnd-kit
 * - Visual feedback on drag over
 * - Freshfoodz CI compliant design
 * - Performance optimized with React.memo
 * - Structured logging
 * @component
 * @since 2.0.0
 * @example
 * ```tsx
 * <PipelineStage
 *   stage={OpportunityStage.LEAD}
 *   opportunityCount={5}
 *   totalValue={50000}
 * >
 *   <OpportunityCard />
 * </PipelineStage>
 * ```
 */
export const PipelineStage: React.FC<PipelineStageProps> = React.memo(
  ({ stage, opportunityCount, totalValue, children }) => {
    const theme = useTheme();
    const errorHandler = useErrorHandler('PipelineStage');
    const config = STAGE_CONFIGS_RECORD[stage];

    // Log stage configuration issues
    React.useEffect(() => {
      if (!config) {
        componentLogger.error('Invalid stage configuration', { stage });
        errorHandler(new Error(`Invalid stage: ${stage}`));
      } else {
        componentLogger.debug('Stage initialized', {
          stage,
          opportunityCount,
          totalValue,
        });
      }
    }, [stage, config, opportunityCount, totalValue, errorHandler]);

    // Drag & Drop Setup
    const { setNodeRef, isOver } = useDroppable({
      id: stage,
    });

    // Format currency value (memoized)
    const formattedValue = useMemo(() => {
      if (totalValue === undefined) return null;

      try {
        return new Intl.NumberFormat('de-DE', {
          style: 'currency',
          currency: 'EUR',
          minimumFractionDigits: 0,
        }).format(totalValue);
      } catch (_error) {
        void _error;
        componentLogger.warn('Error formatting currency', { totalValue, error });
        return `${totalValue} €`;
      }
    }, [totalValue]);

    // Stage styles (memoized)
    const stageStyles = useMemo(
      () => ({
        minHeight: 400,
        p: 2,
        bgcolor: isOver
          ? 'rgba(148, 196, 86, 0.12)'
          : config?.bgColor || theme.palette.background.paper,
        border: isOver ? '3px dashed #94C456' : '1px solid rgba(0, 0, 0, 0.12)',
        borderRadius: 2,
        transition: theme.transitions.create(
          ['border', 'background-color', 'box-shadow'],
          {
            duration: theme.transitions.duration.short,
          }
        ),
        // Sprint 2.1.7.1 FIX: REMOVED transform - caused inverted X-axis bug!
        // transform: isOver ? 'scale(1.02)' : 'scale(1)',
        boxShadow: isOver ? theme.shadows[4] : theme.shadows[1],
        width: '280px',
        minWidth: '280px',
      }),
      [isOver, config?.bgColor, theme]
    );

    // Badge styles (memoized)
    const badgeStyles = useMemo(
      () => ({
        '& .MuiBadge-badge': {
          bgcolor: config?.color || theme.palette.primary.main,
          color: 'white',
          fontWeight: 600,
        },
      }),
      [config?.color, theme]
    );

    if (!config) {
      return null;
    }

    return (
      <Paper ref={setNodeRef} elevation={isOver ? 4 : 1} sx={stageStyles}>
        {/* Stage Header */}
        <Box sx={{ mb: 2 }}>
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              mb: 1,
            }}
          >
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

            <Badge badgeContent={opportunityCount} color="primary" sx={badgeStyles} />
          </Box>

          {/* Total Value (optional) */}
          {formattedValue && (
            <Typography
              variant="body2"
              sx={{
                color: theme.palette.text.secondary,
                fontWeight: 500,
              }}
            >
              Gesamt: {formattedValue}
            </Typography>
          )}
        </Box>

        {/* Opportunities Container */}
        {children}
      </Paper>
    );
  }
);

PipelineStage.displayName = 'PipelineStage';
