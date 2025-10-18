import React, { useState, useCallback } from 'react';
import {
  Card,
  CardContent,
  Typography,
  Box,
  Avatar,
  LinearProgress,
  IconButton,
  Tooltip,
} from '@mui/material';
import { useTheme } from '@mui/material/styles';
import PersonIcon from '@mui/icons-material/Person';
import EuroIcon from '@mui/icons-material/Euro';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import RestoreIcon from '@mui/icons-material/Restore';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import { Chip } from '@mui/material';

import { OpportunityStage, OpportunityType, type Opportunity } from '../../types';

// Aktive Pipeline Stages (immer sichtbar)
const ACTIVE_STAGES = [
  OpportunityStage.NEW_LEAD,
  OpportunityStage.QUALIFICATION,
  OpportunityStage.NEEDS_ANALYSIS,
  OpportunityStage.PROPOSAL,
  OpportunityStage.NEGOTIATION,
];

interface OpportunityCardProps {
  /** The opportunity data to display */
  opportunity: Opportunity;
  /** Whether the card is being dragged */
  isDragging?: boolean;
  /** Callback for quick actions (win/lose/reactivate) */
  onQuickAction?: (opportunityId: string, action: 'won' | 'lost' | 'reactivate') => void;
  /** Whether to show action buttons */
  showActions?: boolean;
}

export const OpportunityCard: React.FC<OpportunityCardProps> = React.memo(
  ({ opportunity, isDragging = false, onQuickAction, showActions = false }) => {
    const theme = useTheme();
    const [, setIsHovered] = useState(false);

    // Clean opportunity name from type prefix (Sprint 2.1.7.1 - Production Safety)
    const cleanOpportunityName = useCallback((name: string): string => {
      return name.replace(
        /^(Neuer Standort|Sortimentserweiterung|Verlängerung|Verlaengerung|Neugeschäft|Neugeschaeft):\s*/i,
        ''
      );
    }, []);

    const getOpportunityTypeLabel = useCallback((type?: OpportunityType): string => {
      if (!type) return '';
      const labels: Record<OpportunityType, string> = {
        [OpportunityType.NEUGESCHAEFT]: 'Neugeschäft',
        [OpportunityType.SORTIMENTSERWEITERUNG]: 'Sortimentserweiterung',
        [OpportunityType.NEUER_STANDORT]: 'Neuer Standort',
        [OpportunityType.VERLAENGERUNG]: 'Vertragsverlängerung',
      };
      return labels[type] || type;
    }, []);

    const getOpportunityTypeIcon = useCallback((type?: OpportunityType): string => {
      if (!type) return '💼';
      const icons: Record<OpportunityType, string> = {
        [OpportunityType.NEUGESCHAEFT]: '🆕',
        [OpportunityType.SORTIMENTSERWEITERUNG]: '📈',
        [OpportunityType.NEUER_STANDORT]: '📍',
        [OpportunityType.VERLAENGERUNG]: '🔁',
      };
      return icons[type] || '💼';
    }, []);

    const getOpportunityTypeColor = useCallback(
      (type?: OpportunityType): 'primary' | 'secondary' | 'info' | 'warning' | 'default' => {
        if (!type) return 'default';
        const colors: Record<OpportunityType, 'primary' | 'secondary' | 'info' | 'warning'> = {
          [OpportunityType.NEUGESCHAEFT]: 'primary',
          [OpportunityType.SORTIMENTSERWEITERUNG]: 'secondary',
          [OpportunityType.NEUER_STANDORT]: 'info',
          [OpportunityType.VERLAENGERUNG]: 'warning',
        };
        return colors[type] || 'default';
      },
      []
    );

    const getProbabilityColor = useCallback(
      (probability?: number) => {
        if (!probability) return theme.palette.grey[400];
        if (probability >= 80) return theme.palette.status.probabilityHigh;
        if (probability >= 60) return theme.palette.primary.main;
        if (probability >= 40) return theme.palette.status.probabilityMedium;
        if (probability >= 20) return theme.palette.status.probabilityLow;
        return theme.palette.status.lost;
      },
      [theme]
    );

    const formatDate = useCallback((dateString?: string) => {
      if (!dateString) return '-';
      return new Date(dateString).toLocaleDateString('de-DE', {
        day: '2-digit',
        month: '2-digit',
        year: '2-digit',
      });
    }, []);

    const formatValue = useCallback((value?: number) => {
      if (!value) return 'Kein Wert';
      return new Intl.NumberFormat('de-DE', {
        style: 'currency',
        currency: 'EUR',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
      }).format(value);
    }, []);

    return (
      <Card
        onMouseEnter={() => setIsHovered(true)}
        onMouseLeave={() => setIsHovered(false)}
        sx={{
          opacity: isDragging ? 0.5 : 1,
          boxShadow: isDragging ? theme.shadows[8] : theme.shadows[2],
          border: opportunity.stageColor
            ? `1px solid ${opportunity.stageColor}40` // 40 = 25% opacity (Sprint 2.1.7.1)
            : `1px solid ${theme.palette.divider}`,
          cursor: 'grab',
          // Sprint 2.1.7.1: NO transform here! SortableOpportunityCard handles it
          '&:hover': {
            boxShadow: theme.shadows[4],
          },
        }}
      >
        <CardContent sx={{ pb: '16px !important' }}>
          {/* Opportunity Name */}
          <Typography
            variant="h6"
            sx={{
              mb: 1,
              lineHeight: 1.3,
            }}
          >
            {cleanOpportunityName(opportunity.name)}
          </Typography>

          {/* Customer / Lead Badge (Sprint 2.1.7.1 - Customer Fallback + Lead Badge) */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1, flexWrap: 'wrap' }}>
            {/* Kundenname oder Lead-Fallback */}
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <PersonIcon fontSize="small" sx={{ color: theme.palette.grey[600], mr: 0.5 }} />
              <Typography
                variant="body2"
                sx={{ color: theme.palette.text.secondary, fontSize: '0.875rem' }}
              >
                {opportunity.customerName || opportunity.leadCompanyName || 'Unbekannt'}
              </Typography>
            </Box>

            {/* Lead-Origin Badge (wenn von Lead konvertiert) */}
            {opportunity.leadId && (
              <Chip
                icon={<TrendingUpIcon sx={{ fontSize: '0.875rem' }} />}
                label={`von Lead #${opportunity.leadId}`}
                size="small"
                sx={{
                  height: '20px',
                  fontSize: '0.75rem',
                  bgcolor: theme => `${theme.palette.primary.main}15`, // FreshFoodz Green 15% opacity (Design System konform)
                  color: theme => theme.palette.secondary.main, // FreshFoodz Blue aus Theme
                  fontWeight: 500,
                  '& .MuiChip-icon': {
                    color: theme => theme.palette.primary.main, // FreshFoodz Green aus Theme
                  },
                  border: theme => `1px solid ${theme.palette.primary.main}30`, // 30% opacity
                }}
              />
            )}

            {/* OpportunityType Badge (Freshfoodz Business Types - Sprint 2.1.7.1) */}
            {opportunity.opportunityType && (
              <Chip
                label={`${getOpportunityTypeIcon(opportunity.opportunityType)} ${getOpportunityTypeLabel(opportunity.opportunityType)}`}
                size="small"
                color={getOpportunityTypeColor(opportunity.opportunityType)}
                sx={{
                  height: '22px',
                  fontSize: '0.7rem',
                  '& .MuiChip-label': {
                    px: 1,
                    lineHeight: 1.2,
                  },
                }}
              />
            )}
          </Box>

          {/* Value */}
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
            <EuroIcon fontSize="small" sx={{ color: theme.palette.primary.main, mr: 0.5 }} />
            <Typography variant="subtitle2" sx={{ color: theme.palette.text.primary }}>
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
                    fontWeight: 'bold',
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

          {/* Footer */}
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
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
            </Box>

            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              {/* Action Buttons - je nach Stage */}
              {showActions && onQuickAction && (
                <>
                  {/* Buttons für aktive Stages */}
                  {ACTIVE_STAGES.includes(opportunity.stage) && (
                    <>
                      <Tooltip title="Als gewonnen markieren">
                        <IconButton
                          size="small"
                          onClick={(e: React.MouseEvent<HTMLButtonElement>) => {
                            e.stopPropagation();
                            onQuickAction(opportunity.id, 'won');
                          }}
                          sx={{
                            width: 24,
                            height: 24,
                            color: theme.palette.status.won,
                            opacity: 0.7,
                            transition: 'all 0.2s ease',
                            '&:hover': {
                              bgcolor: `${theme.palette.status.won}20`,
                              opacity: 1,
                              transform: 'scale(1.1)',
                            },
                          }}
                        >
                          <CheckCircleIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Als verloren markieren">
                        <IconButton
                          size="small"
                          onClick={(e: React.MouseEvent<HTMLButtonElement>) => {
                            e.stopPropagation();
                            onQuickAction(opportunity.id, 'lost');
                          }}
                          sx={{
                            width: 24,
                            height: 24,
                            color: theme.palette.status.lost,
                            opacity: 0.7,
                            transition: 'all 0.2s ease',
                            '&:hover': {
                              bgcolor: `${theme.palette.status.lost}20`,
                              opacity: 1,
                              transform: 'scale(1.1)',
                            },
                          }}
                        >
                          <CancelIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </>
                  )}

                  {/* Reaktivieren-Button für verlorene Opportunities */}
                  {opportunity.stage === OpportunityStage.CLOSED_LOST && (
                    <Tooltip title="Opportunity reaktivieren">
                      <IconButton
                        size="small"
                        onClick={(e: React.MouseEvent<HTMLButtonElement>) => {
                          e.stopPropagation();
                          onQuickAction(opportunity.id, 'reactivate');
                        }}
                        sx={{
                          width: 24,
                          height: 24,
                          color: theme.palette.status.reactivate,
                          opacity: 0.7,
                          transition: 'all 0.2s ease',
                          '&:hover': {
                            bgcolor: `${theme.palette.status.reactivate}20`,
                            opacity: 1,
                            transform: 'scale(1.1)',
                          },
                        }}
                      >
                        <RestoreIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  )}
                </>
              )}

              {opportunity.assignedToName && (
                <Avatar
                  sx={{
                    width: 24,
                    height: 24,
                    bgcolor: theme.palette.primary.main,
                    fontSize: '0.75rem',
                    fontWeight: 'bold',
                  }}
                >
                  {opportunity.assignedToName.charAt(0).toUpperCase()}
                </Avatar>
              )}
            </Box>
          </Box>
        </CardContent>
      </Card>
    );
  }
);

OpportunityCard.displayName = 'OpportunityCard';
