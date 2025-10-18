/**
 * LeadOpportunitiesList Component
 * Sprint 2.1.7.1 - Lead → Opportunity Conversion
 *
 * @description Displays all opportunities that were created from a specific lead
 * @since 2025-10-18
 */

import React, { useEffect, useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Stack,
  Chip,
  CircularProgress,
  Alert,
  Link as MuiLink,
} from '@mui/material';
import { useTheme } from '@mui/material/styles';
import { Link } from 'react-router-dom';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import EuroIcon from '@mui/icons-material/Euro';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import PersonIcon from '@mui/icons-material/Person';
import { apiClient } from '../../../lib/apiClient';
import type { Opportunity, OpportunityType, OpportunityStage } from '../../opportunity/types';

interface LeadOpportunitiesListProps {
  /** Lead ID to fetch opportunities for */
  leadId: number;
}

export const LeadOpportunitiesList: React.FC<LeadOpportunitiesListProps> = ({ leadId }) => {
  const theme = useTheme();
  const [opportunities, setOpportunities] = useState<Opportunity[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchOpportunities = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await apiClient.get<Opportunity[]>(`/api/leads/${leadId}/opportunities`);
        setOpportunities(response.data || []);
      } catch (err: any) {
        console.error('Failed to fetch opportunities:', err);
        setError(err.response?.data?.error || 'Fehler beim Laden der Opportunities');
      } finally {
        setLoading(false);
      }
    };

    fetchOpportunities();
  }, [leadId]);

  // Helper functions
  const formatValue = (value?: number) => {
    if (!value) return 'Kein Wert';
    return new Intl.NumberFormat('de-DE', {
      style: 'currency',
      currency: 'EUR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('de-DE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
  };

  const getOpportunityTypeLabel = (type?: OpportunityType): string => {
    if (!type) return '';
    const labels: Record<string, string> = {
      NEUGESCHAEFT: 'Neugeschäft',
      SORTIMENTSERWEITERUNG: 'Sortimentserweiterung',
      NEUER_STANDORT: 'Neuer Standort',
      VERLAENGERUNG: 'Vertragsverlängerung',
    };
    return labels[type] || type;
  };

  const getOpportunityTypeIcon = (type?: OpportunityType): string => {
    if (!type) return '💼';
    const icons: Record<string, string> = {
      NEUGESCHAEFT: '🆕',
      SORTIMENTSERWEITERUNG: '📈',
      NEUER_STANDORT: '📍',
      VERLAENGERUNG: '🔁',
    };
    return icons[type] || '💼';
  };

  const getOpportunityTypeColor = (type?: OpportunityType): 'primary' | 'secondary' | 'info' | 'warning' | 'default' => {
    if (!type) return 'default';
    const colors: Record<string, 'primary' | 'secondary' | 'info' | 'warning'> = {
      NEUGESCHAEFT: 'primary',
      SORTIMENTSERWEITERUNG: 'secondary',
      NEUER_STANDORT: 'info',
      VERLAENGERUNG: 'warning',
    };
    return colors[type] || 'default';
  };

  const getStageLabel = (stage?: OpportunityStage): string => {
    if (!stage) return '';
    const labels: Record<string, string> = {
      NEW_LEAD: 'Neuer Lead',
      QUALIFICATION: 'Qualifizierung',
      NEEDS_ANALYSIS: 'Bedarfsanalyse',
      PROPOSAL: 'Angebot',
      NEGOTIATION: 'Verhandlung',
      CLOSED_WON: 'Gewonnen',
      CLOSED_LOST: 'Verloren',
    };
    return labels[stage] || stage;
  };

  const getStageColor = (stage?: OpportunityStage): string => {
    if (!stage) return theme.palette.grey[500];
    const colors: Record<string, string> = {
      NEW_LEAD: theme.palette.info.main,
      QUALIFICATION: theme.palette.primary.light,
      NEEDS_ANALYSIS: theme.palette.primary.main,
      PROPOSAL: theme.palette.secondary.main,
      NEGOTIATION: theme.palette.warning.main,
      CLOSED_WON: theme.palette.success.main,
      CLOSED_LOST: theme.palette.error.main,
    };
    return colors[stage] || theme.palette.grey[500];
  };

  // Loading state
  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  // Error state
  if (error) {
    return (
      <Alert severity="error" sx={{ mb: 2 }}>
        {error}
      </Alert>
    );
  }

  // Empty state
  if (opportunities.length === 0) {
    return (
      <Alert severity="info" icon={<TrendingUpIcon />}>
        Noch keine Opportunities für diesen Lead erstellt.
      </Alert>
    );
  }

  // Opportunities list
  return (
    <Stack spacing={2}>
      {opportunities.map((opportunity) => (
        <Card
          key={opportunity.id}
          sx={{
            border: '1px solid',
            borderColor: 'divider',
            '&:hover': {
              boxShadow: theme.shadows[4],
              borderColor: theme.palette.primary.main,
            },
            transition: 'all 0.2s ease',
          }}
        >
          <CardContent>
            <Stack spacing={2}>
              {/* Header: Name + Stage */}
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 2 }}>
                <Box sx={{ flex: 1 }}>
                  <MuiLink
                    component={Link}
                    to={`/opportunities/${opportunity.id}`}
                    sx={{
                      textDecoration: 'none',
                      color: 'inherit',
                      '&:hover': {
                        color: theme.palette.primary.main,
                      },
                    }}
                  >
                    <Typography
                      variant="h6"
                      sx={{
                        fontWeight: 600,
                        display: 'flex',
                        alignItems: 'center',
                        gap: 1,
                      }}
                    >
                      <TrendingUpIcon fontSize="small" color="primary" />
                      {opportunity.name}
                    </Typography>
                  </MuiLink>
                </Box>
                <Chip
                  label={getStageLabel(opportunity.stage)}
                  size="small"
                  sx={{
                    bgcolor: `${getStageColor(opportunity.stage)}20`,
                    color: getStageColor(opportunity.stage),
                    fontWeight: 600,
                    border: `1px solid ${getStageColor(opportunity.stage)}40`,
                  }}
                />
              </Box>

              {/* Metadata Row 1: Type + Customer */}
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1.5, alignItems: 'center' }}>
                {/* OpportunityType Badge */}
                {opportunity.opportunityType && (
                  <Chip
                    label={`${getOpportunityTypeIcon(opportunity.opportunityType)} ${getOpportunityTypeLabel(opportunity.opportunityType)}`}
                    size="small"
                    color={getOpportunityTypeColor(opportunity.opportunityType)}
                    sx={{
                      height: '22px',
                      fontSize: '0.7rem',
                      fontFamily: 'Antonio, sans-serif !important',
                      fontWeight: '700 !important',
                      '& .MuiChip-label': {
                        px: 1,
                        lineHeight: 1.2,
                        fontFamily: 'Antonio, sans-serif !important',
                        fontWeight: '700 !important',
                      },
                    }}
                  />
                )}

                {/* Customer Name */}
                {(opportunity.customerName || opportunity.leadCompanyName) && (
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <PersonIcon fontSize="small" sx={{ color: theme.palette.grey[600] }} />
                    <Typography variant="body2" sx={{ color: theme.palette.text.secondary }}>
                      {opportunity.customerName || opportunity.leadCompanyName}
                    </Typography>
                  </Box>
                )}
              </Box>

              {/* Metadata Row 2: Value + Date */}
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2, alignItems: 'center' }}>
                {/* Value */}
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                  <EuroIcon fontSize="small" sx={{ color: theme.palette.primary.main }} />
                  <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
                    {formatValue(opportunity.value)}
                  </Typography>
                </Box>

                {/* Expected Close Date */}
                {opportunity.expectedCloseDate && (
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <CalendarTodayIcon fontSize="small" sx={{ color: theme.palette.grey[600] }} />
                    <Typography variant="body2" sx={{ color: theme.palette.text.secondary }}>
                      Abschluss: {formatDate(opportunity.expectedCloseDate)}
                    </Typography>
                  </Box>
                )}
              </Box>

              {/* Description (if exists) */}
              {opportunity.description && (
                <Typography
                  variant="body2"
                  sx={{
                    color: theme.palette.text.secondary,
                    fontStyle: 'italic',
                    pl: 1,
                    borderLeft: `2px solid ${theme.palette.divider}`,
                  }}
                >
                  {opportunity.description}
                </Typography>
              )}
            </Stack>
          </CardContent>
        </Card>
      ))}

      {/* Summary */}
      <Alert severity="success" icon={<TrendingUpIcon />} sx={{ mt: 1 }}>
        <strong>{opportunities.length}</strong>{' '}
        {opportunities.length === 1 ? 'Opportunity' : 'Opportunities'} aus diesem Lead erstellt
      </Alert>
    </Stack>
  );
};
