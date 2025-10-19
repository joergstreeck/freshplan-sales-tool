/**
 * CustomerOpportunitiesList Component
 * Sprint 2.1.7.3 - Customer → Opportunity Workflow (Bestandskunden)
 *
 * @description Displays all opportunities for a specific customer, grouped by status
 * @features
 * - Gruppierung: Offen / Gewonnen / Verloren (MUI Accordion)
 * - Sortierung: Neueste zuerst
 * - Click → OpportunityDetailPage
 * - Empty State wenn keine Opportunities
 *
 * @since 2025-10-19
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
  Accordion,
  AccordionSummary,
  AccordionDetails,
} from '@mui/material';
import { useTheme } from '@mui/material/styles';
import { useNavigate } from 'react-router-dom';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import EuroIcon from '@mui/icons-material/Euro';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import PendingActionsIcon from '@mui/icons-material/PendingActions';
import { httpClient } from '../../../lib/apiClient';
import type { Opportunity, OpportunityType, OpportunityStage } from '../../opportunity/types';

interface CustomerOpportunitiesListProps {
  /** Customer ID to fetch opportunities for */
  customerId: string;
  /** Callback function to notify parent of opportunity count changes */
  onCountChange?: (count: number) => void;
}

/**
 * Helper: Categorize opportunity stage into Offen/Gewonnen/Verloren
 */
type OpportunityCategory = 'open' | 'won' | 'lost';

const categorizeOpportunity = (stage?: OpportunityStage): OpportunityCategory => {
  if (!stage) return 'open';

  if (stage === 'CLOSED_WON') return 'won';
  if (stage === 'CLOSED_LOST') return 'lost';
  return 'open';
};

export const CustomerOpportunitiesList: React.FC<CustomerOpportunitiesListProps> = ({
  customerId,
  onCountChange,
}) => {
  const theme = useTheme();
  const navigate = useNavigate();
  const [opportunities, setOpportunities] = useState<Opportunity[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchOpportunities = async () => {
      setLoading(true);
      setError(null);
      try {
        // API Endpoint: GET /api/customers/{customerId}/opportunities
        // TODO: Backend muss diesen Endpoint noch implementieren!
        const response = await httpClient.get<Opportunity[]>(
          `/api/customers/${customerId}/opportunities`
        );
        const fetchedOpportunities = response.data || [];

        // Sortierung: Neueste zuerst
        const sorted = fetchedOpportunities.sort((a, b) => {
          const dateA = new Date(a.createdAt || 0).getTime();
          const dateB = new Date(b.createdAt || 0).getTime();
          return dateB - dateA; // Descending (newest first)
        });

        setOpportunities(sorted);

        // Notify parent component of count change
        if (onCountChange) {
          onCountChange(sorted.length);
        }
      } catch (err: unknown) {
        console.error('Failed to fetch opportunities:', err);
        const error = err as { response?: { data?: { error?: string } } };
        setError(error.response?.data?.error || 'Fehler beim Laden der Opportunities');

        // Reset count on error
        if (onCountChange) {
          onCountChange(0);
        }
      } finally {
        setLoading(false);
      }
    };

    fetchOpportunities();
  }, [customerId, onCountChange]);

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

  const getOpportunityTypeColor = (
    type?: OpportunityType
  ): 'primary' | 'secondary' | 'info' | 'warning' | 'default' => {
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

  // Group opportunities by category
  const openOpportunities = opportunities.filter(o => categorizeOpportunity(o.stage) === 'open');
  const wonOpportunities = opportunities.filter(o => categorizeOpportunity(o.stage) === 'won');
  const lostOpportunities = opportunities.filter(o => categorizeOpportunity(o.stage) === 'lost');

  // Opportunity Card (shared component)
  const OpportunityCard: React.FC<{ opportunity: Opportunity }> = ({ opportunity }) => (
    <Card
      onClick={() => navigate(`/opportunities/${opportunity.id}`)}
      sx={{
        border: '1px solid',
        borderColor: 'divider',
        cursor: 'pointer',
        '&:hover': {
          boxShadow: theme.shadows[4],
          borderColor: theme.palette.primary.main,
          backgroundColor: theme.palette.action.hover,
        },
        transition: 'all 0.2s ease',
      }}
    >
      <CardContent>
        <Stack spacing={2}>
          {/* Header: Name + Stage */}
          <Box
            sx={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'flex-start',
              gap: 2,
            }}
          >
            <Box sx={{ flex: 1 }}>
              <Typography
                variant="h6"
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 1,
                }}
              >
                <TrendingUpIcon fontSize="small" color="primary" />
                {opportunity.name}
              </Typography>
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

          {/* Metadata Row 1: Type */}
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
                  '& .MuiChip-label': {
                    px: 1,
                    lineHeight: 1.2,
                  },
                }}
              />
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
  );

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
        Noch keine Opportunities für diesen Kunden erstellt. Nutzen Sie den Button "Neue Opportunity
        erstellen" oben.
      </Alert>
    );
  }

  // Opportunities list (grouped by status via Accordion)
  return (
    <Stack spacing={2}>
      {/* Offen Accordion (default expanded) */}
      {openOpportunities.length > 0 && (
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <PendingActionsIcon color="primary" />
              <Typography variant="h6">Offen ({openOpportunities.length})</Typography>
            </Box>
          </AccordionSummary>
          <AccordionDetails>
            <Stack spacing={2}>
              {openOpportunities.map(opportunity => (
                <OpportunityCard key={opportunity.id} opportunity={opportunity} />
              ))}
            </Stack>
          </AccordionDetails>
        </Accordion>
      )}

      {/* Gewonnen Accordion (default expanded) */}
      {wonOpportunities.length > 0 && (
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <CheckCircleIcon color="success" />
              <Typography variant="h6">Gewonnen ({wonOpportunities.length})</Typography>
            </Box>
          </AccordionSummary>
          <AccordionDetails>
            <Stack spacing={2}>
              {wonOpportunities.map(opportunity => (
                <OpportunityCard key={opportunity.id} opportunity={opportunity} />
              ))}
            </Stack>
          </AccordionDetails>
        </Accordion>
      )}

      {/* Verloren Accordion (default collapsed) */}
      {lostOpportunities.length > 0 && (
        <Accordion>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <CancelIcon color="error" />
              <Typography variant="h6">Verloren ({lostOpportunities.length})</Typography>
            </Box>
          </AccordionSummary>
          <AccordionDetails>
            <Stack spacing={2}>
              {lostOpportunities.map(opportunity => (
                <OpportunityCard key={opportunity.id} opportunity={opportunity} />
              ))}
            </Stack>
          </AccordionDetails>
        </Accordion>
      )}

      {/* Summary */}
      <Alert severity="success" icon={<TrendingUpIcon />} sx={{ mt: 1 }}>
        <strong>{opportunities.length}</strong>{' '}
        {opportunities.length === 1 ? 'Opportunity' : 'Opportunities'} für diesen Kunden
        {openOpportunities.length > 0 && (
          <>
            {' '}
            • <strong>{openOpportunities.length}</strong> offen
          </>
        )}
        {wonOpportunities.length > 0 && (
          <>
            {' '}
            • <strong>{wonOpportunities.length}</strong> gewonnen
          </>
        )}
      </Alert>
    </Stack>
  );
};
