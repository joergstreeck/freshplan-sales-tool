/**
 * Customer Compact View Component
 *
 * Sprint 2.1.7.2 D11: Server-Driven Customer Cards - Phase 1
 *
 * Kompakte Kundenübersicht für den 80%-Use-Case.
 * Progressive Disclosure: Alle wichtigen Infos auf einen Blick.
 *
 * Features:
 * - Kompakte Darstellung (Name, Status, Umsatz, Health Score)
 * - Risiko-Indikatoren
 * - Nächste Schritte
 * - Button "Alle Details anzeigen" → öffnet Modal/Drawer (Phase 2)
 * - Multi-Location Summary (Phase 3)
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import React from 'react';
import {
  Box,
  Paper,
  Typography,
  Grid,
  Chip,
  Button,
  Alert,
  LinearProgress,
  Stack,
  Divider,
  Card,
  CardContent,
} from '@mui/material';
import {
  Visibility as VisibilityIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  Warning as WarningIcon,
  CheckCircle as CheckCircleIcon,
  Event as EventIcon,
  Euro as EuroIcon,
  Speed as SpeedIcon,
} from '@mui/icons-material';
import { formatDistanceToNow } from 'date-fns';
import { de } from 'date-fns/locale';
import type { Customer } from '../../../customer/types/customer.types';
import { customerStatusLabels, getCustomerStatusColor } from '../../../customer/types/customer.types';
import { useTheme } from '@mui/material/styles';

interface CustomerCompactViewProps {
  customer: Customer;
  onShowDetails?: () => void;
}

/**
 * Customer Compact View
 *
 * Zeigt die wichtigsten Kundeninformationen in kompakter Form.
 * 80% der täglichen Arbeit kann direkt hier erledigt werden.
 */
export const CustomerCompactView: React.FC<CustomerCompactViewProps> = ({
  customer,
  onShowDetails,
}) => {
  const theme = useTheme();

  // Format currency
  const formatCurrency = (value?: number): string => {
    if (!value) return 'Nicht erfasst';
    return new Intl.NumberFormat('de-DE', {
      style: 'currency',
      currency: 'EUR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  };

  // Calculate health score (0-100, lower = riskier)
  const healthScore = customer.atRisk ? Math.max(0, 100 - customer.riskScore) : 80;

  // Determine health score color
  const getHealthScoreColor = (score: number): string => {
    if (score >= 80) return theme.palette.success.main; // Grün
    if (score >= 50) return theme.palette.warning.main; // Orange
    return theme.palette.error.main; // Rot
  };

  // Get status color
  const statusColor = getCustomerStatusColor(customer.status, theme);

  return (
    <Box>
      {/* Header Section */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <Box sx={{ flex: 1 }}>
            <Typography variant="h5" gutterBottom>
              {customer.companyName}
            </Typography>
            <Stack direction="row" spacing={1} sx={{ mb: 2 }}>
              <Chip
                label={customerStatusLabels[customer.status]}
                sx={{ bgcolor: statusColor, color: 'white', fontWeight: 600 }}
                size="small"
              />
              {customer.classification && (
                <Chip label={customer.classification} variant="outlined" size="small" />
              )}
              {customer.industry && (
                <Chip label={customer.industry} variant="outlined" size="small" />
              )}
            </Stack>
          </Box>

          {onShowDetails && (
            <Button
              variant="contained"
              startIcon={<VisibilityIcon />}
              onClick={onShowDetails}
              sx={{ ml: 2 }}
            >
              Alle Details anzeigen
            </Button>
          )}
        </Box>
      </Paper>

      {/* Risk Alert */}
      {customer.atRisk && (
        <Alert severity="error" icon={<WarningIcon />} sx={{ mb: 3 }}>
          <Typography variant="body2" fontWeight="bold">
            Risikokunde!
          </Typography>
          <Typography variant="caption">
            Churn-Risiko erkannt. Bitte Kontakt aufnehmen.
          </Typography>
        </Alert>
      )}

      {/* Key Metrics Grid */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        {/* Umsatz */}
        <Grid size={{ xs: 12, md: 4 }}>
          <Card>
            <CardContent>
              <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
                <EuroIcon color="primary" />
                <Typography variant="subtitle2" color="text.secondary">
                  Jahresumsatz (erwartet)
                </Typography>
              </Stack>
              <Typography variant="h6">
                {formatCurrency(customer.expectedAnnualVolume)}
              </Typography>
              {customer.actualAnnualVolume && customer.actualAnnualVolume > 0 && (
                <Typography variant="caption" color="text.secondary">
                  Tatsächlich: {formatCurrency(customer.actualAnnualVolume)}
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Health Score */}
        <Grid size={{ xs: 12, md: 4 }}>
          <Card>
            <CardContent>
              <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
                <SpeedIcon color="primary" />
                <Typography variant="subtitle2" color="text.secondary">
                  Health Score
                </Typography>
              </Stack>
              <Typography
                variant="h6"
                sx={{
                  color: getHealthScoreColor(healthScore),
                }}
              >
                {healthScore}/100
              </Typography>
              <LinearProgress
                variant="determinate"
                value={healthScore}
                sx={{
                  mt: 1,
                  height: 8,
                  borderRadius: 4,
                  bgcolor: 'grey.200',
                  '& .MuiLinearProgress-bar': {
                    bgcolor: getHealthScoreColor(healthScore),
                  },
                }}
              />
            </CardContent>
          </Card>
        </Grid>

        {/* Letzter Kontakt */}
        <Grid size={{ xs: 12, md: 4 }}>
          <Card>
            <CardContent>
              <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
                <EventIcon color="primary" />
                <Typography variant="subtitle2" color="text.secondary">
                  Letzter Kontakt
                </Typography>
              </Stack>
              {customer.lastContactDate ? (
                <>
                  <Typography variant="h6">
                    {formatDistanceToNow(new Date(customer.lastContactDate), {
                      addSuffix: true,
                      locale: de,
                    })}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {new Date(customer.lastContactDate).toLocaleDateString('de-DE')}
                  </Typography>
                </>
              ) : (
                <Typography variant="body2" color="text.secondary">
                  Noch kein Kontakt
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Nächste Schritte & Info */}
      <Grid container spacing={2}>
        {/* Nächste Schritte */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom fontWeight="bold">
              Nächste Schritte
            </Typography>
            <Divider sx={{ mb: 2 }} />
            {customer.nextFollowUpDate ? (
              <Stack spacing={1}>
                <Stack direction="row" spacing={1} alignItems="center">
                  <EventIcon fontSize="small" color="primary" />
                  <Typography variant="body2">
                    Follow-up:{' '}
                    {formatDistanceToNow(new Date(customer.nextFollowUpDate), {
                      addSuffix: true,
                      locale: de,
                    })}
                  </Typography>
                </Stack>
                <Typography variant="caption" color="text.secondary">
                  {new Date(customer.nextFollowUpDate).toLocaleDateString('de-DE')}
                </Typography>
              </Stack>
            ) : (
              <Typography variant="body2" color="text.secondary">
                Keine Folgeaktionen geplant
              </Typography>
            )}
          </Paper>
        </Grid>

        {/* Schnellinfo */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom fontWeight="bold">
              Schnellinfo
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <Stack spacing={1}>
              <Stack direction="row" justifyContent="space-between">
                <Typography variant="body2" color="text.secondary">
                  Kundennummer:
                </Typography>
                <Typography variant="body2" fontWeight="medium">
                  {customer.customerNumber}
                </Typography>
              </Stack>
              {customer.hierarchyType && (
                <Stack direction="row" justifyContent="space-between">
                  <Typography variant="body2" color="text.secondary">
                    Hierarchie:
                  </Typography>
                  <Typography variant="body2" fontWeight="medium">
                    {customer.hierarchyType}
                  </Typography>
                </Stack>
              )}
              {customer.paymentTerms && (
                <Stack direction="row" justifyContent="space-between">
                  <Typography variant="body2" color="text.secondary">
                    Zahlungsziel:
                  </Typography>
                  <Typography variant="body2" fontWeight="medium">
                    {customer.paymentTerms}
                  </Typography>
                </Stack>
              )}
              <Stack direction="row" justifyContent="space-between">
                <Typography variant="body2" color="text.secondary">
                  Kontakte:
                </Typography>
                <Typography variant="body2" fontWeight="medium">
                  {customer.contactsCount || 0}
                </Typography>
              </Stack>
            </Stack>
          </Paper>
        </Grid>
      </Grid>

      {/* Info Banner */}
      <Alert severity="info" sx={{ mt: 3 }}>
        <Typography variant="body2">
          <strong>Hinweis:</strong> Dies ist eine kompakte Übersicht der wichtigsten
          Kundeninformationen. Klicken Sie auf "Alle Details anzeigen" für vollständige
          Unternehmensdaten, Standorte, Verträge und mehr.
        </Typography>
      </Alert>
    </Box>
  );
};
