import React, { useCallback, useMemo } from 'react';
import { Card, CardContent, Typography, Box, Avatar, LinearProgress, Tooltip } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import { useDraggable } from '@dnd-kit/core';
import DragIndicatorIcon from '@mui/icons-material/DragIndicator';
import PersonIcon from '@mui/icons-material/Person';
import EuroIcon from '@mui/icons-material/Euro';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import type { Opportunity } from '../types';
import { logger } from '../../../lib/logger';
import { useErrorHandler } from '../../../components/ErrorBoundary';

// Component logger instance
const componentLogger = logger.child('OpportunityCard');

/**
 * Opportunity Card Component Props
 * @interface OpportunityCardProps
 */
interface OpportunityCardProps {
  /** The opportunity data to display */
  opportunity: Opportunity;
  /** Click handler for card selection */
  onClick?: (opportunity: Opportunity) => void;
}

/**
 * OpportunityCard Component
 * @description Enterprise-grade opportunity card with drag & drop support
 * Features:
 * - Drag & drop functionality via @dnd-kit
 * - Performance optimized with React.memo and useMemo
 * - Structured logging and error handling
 * - Freshfoodz CI compliant design
 * - Accessible drag handle
 * @component
 * @since 2.0.0
 * @example
 * ```tsx
 * <OpportunityCard
 *   opportunity={opportunity}
 *   onClick={handleOpportunityClick}
 * />
 * ```
 */
export const OpportunityCard: React.FC<OpportunityCardProps> = React.memo(
  ({ opportunity, onClick }) => {
    const theme = useTheme();
    const errorHandler = useErrorHandler('OpportunityCard');

    // Drag & Drop Setup
    const { attributes, listeners, setNodeRef, transform, isDragging, setActivatorNodeRef } =
      useDraggable({
        id: opportunity.id,
        data: {
          opportunity,
        },
      });

    // Transform für Drag-Animation (memoized)
    const style = useMemo(() => {
      if (!transform) return undefined;
      return {
        transform: `translate3d(${transform.x}px, ${transform.y}px, 0)`,
      };
    }, [transform]);

    /**
     * Get color based on probability percentage
     * @memoized
     */
    const getProbabilityColor = useCallback(
      (probability?: number) => {
        if (!probability) return theme.palette.grey[400];
        if (probability >= 80) return '#66BB6A'; // Grün
        if (probability >= 60) return '#94C456'; // Freshfoodz Grün
        if (probability >= 40) return '#FFA726'; // Orange
        if (probability >= 20) return '#FF7043'; // Orange-Rot
        return '#EF5350'; // Rot
      },
      [theme]
    );

    /**
     * Format date to German locale
     * @memoized
     */
    const formatDate = useCallback((dateString?: string) => {
      if (!dateString) return '-';
      try {
        return new Date(dateString).toLocaleDateString('de-DE', {
          day: '2-digit',
          month: '2-digit',
          year: '2-digit',
        });
      } catch (_error) { void _error;
        componentLogger.warn('Invalid date format', { dateString, error });
        return '-';
      }
    }, []);

    /**
     * Format currency value
     * @memoized
     */
    const formatValue = useCallback((value?: number) => {
      if (!value) return 'Kein Wert';
      try {
        return new Intl.NumberFormat('de-DE', {
          style: 'currency',
          currency: 'EUR',
          minimumFractionDigits: 0,
          maximumFractionDigits: 0,
        }).format(value);
      } catch (_error) { void _error;
        componentLogger.warn('Error formatting value', { value, error });
        return `${value} €`;
      }
    }, []);

    return (
      <Card
        ref={setNodeRef}
        style={style}
        {...attributes}
        sx={{
          cursor: isDragging ? 'grabbing' : 'pointer', // Pointer für onClick, Drag nur über Handle
          opacity: isDragging ? 0 : 1, // Komplett ausblenden während Drag
          transition: isDragging
            ? 'none'
            : theme.transitions.create(['transform', 'box-shadow', 'opacity'], {
                duration: theme.transitions.duration.short,
              }),
          '&:hover': !isDragging
            ? {
                boxShadow: theme.shadows[4],
                transform: 'translateY(-2px)',
              }
            : {},
          border: '1px solid rgba(148, 196, 86, 0.2)',
          borderRadius: 2,
          position: 'relative',
          bgcolor: 'background.paper',
          // Drag & Drop hat Priorität über onClick
          pointerEvents: 'auto',
        }}
        onClick={useCallback(() => {
          // Nur onClick wenn nicht gedraggt wird
          if (!isDragging && onClick) {
            try {
              componentLogger.debug('Card clicked', { opportunityId: opportunity.id });
              onClick(opportunity);
            } catch (_error) { void _error;
              componentLogger.error('Error in onClick handler', { error });
              errorHandler(error as Error);
            }
          }
        }, [isDragging, onClick, opportunity, errorHandler])}
      >
        {/* Drag Handle - Dedizierter Drag-Punkt für bessere Hand-Auge-Koordination */}
        <Box
          ref={setActivatorNodeRef}
          {...listeners}
          sx={{
            position: 'absolute',
            top: 8,
            right: 8,
            color: theme.palette.grey[400],
            cursor: 'grab',
            padding: '4px', // Größerer Touch-Target
            '&:hover': {
              color: '#94C456',
            },
            '&:active': {
              cursor: 'grabbing',
            },
          }}
        >
          <DragIndicatorIcon fontSize="small" />
        </Box>

        <CardContent sx={{ pb: '16px !important', pr: 5 }}>
          {/* Opportunity Name */}
          <Typography
            variant="h6"
            sx={{
              color: '#004F7B', // Freshfoodz Blau
              fontFamily: 'Antonio, sans-serif',
              fontWeight: 600,
              fontSize: '1rem',
              mb: 1,
              lineHeight: 1.3,
              wordBreak: 'break-word',
            }}
          >
            {opportunity.name}
          </Typography>

          {/* Customer */}
          {opportunity.customerName && (
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
              <PersonIcon fontSize="small" sx={{ color: theme.palette.grey[600], mr: 0.5 }} />
              <Typography
                variant="body2"
                sx={{
                  color: theme.palette.text.secondary,
                  fontSize: '0.875rem',
                }}
              >
                {opportunity.customerName}
              </Typography>
            </Box>
          )}

          {/* Value */}
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
            <EuroIcon fontSize="small" sx={{ color: '#94C456', mr: 0.5 }} />
            <Typography
              variant="body2"
              sx={{
                color: theme.palette.text.primary,
                fontWeight: 600,
                fontSize: '0.875rem',
              }}
            >
              {formatValue(opportunity.value)}
            </Typography>
          </Box>

          {/* Probability Progress */}
          {opportunity.probability !== undefined && (
            <Box sx={{ mb: 1.5 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                <Typography variant="caption" sx={{ color: theme.palette.text.secondary }}>
                  Wahrscheinlichkeit
                </Typography>
                <Typography
                  variant="caption"
                  sx={{
                    color: getProbabilityColor(opportunity.probability),
                    fontWeight: 600,
                  }}
                >
                  {opportunity.probability}%
                </Typography>
              </Box>
              <LinearProgress
                variant="determinate"
                value={opportunity.probability}
                sx={{
                  height: 6,
                  borderRadius: 3,
                  bgcolor: 'rgba(0, 0, 0, 0.08)',
                  '& .MuiLinearProgress-bar': {
                    bgcolor: getProbabilityColor(opportunity.probability),
                    borderRadius: 3,
                  },
                }}
              />
            </Box>
          )}

          {/* Footer Info */}
          <Box
            sx={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              mt: 1,
            }}
          >
            {/* Expected Close Date */}
            {opportunity.expectedCloseDate && (
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <CalendarTodayIcon
                  fontSize="small"
                  sx={{ color: theme.palette.grey[500], mr: 0.5 }}
                />
                <Typography variant="caption" sx={{ color: theme.palette.text.secondary }}>
                  {formatDate(opportunity.expectedCloseDate)}
                </Typography>
              </Box>
            )}

            {/* Assigned User Avatar */}
            {opportunity.assignedToName && (
              <Tooltip title={`Zugewiesen: ${opportunity.assignedToName}`}>
                <Avatar
                  sx={{
                    width: 24,
                    height: 24,
                    bgcolor: '#94C456',
                    fontSize: '0.75rem',
                    fontWeight: 600,
                  }}
                >
                  {opportunity.assignedToName.charAt(0).toUpperCase()}
                </Avatar>
              </Tooltip>
            )}
          </Box>
        </CardContent>
      </Card>
    );
  }
);

OpportunityCard.displayName = 'OpportunityCard';
