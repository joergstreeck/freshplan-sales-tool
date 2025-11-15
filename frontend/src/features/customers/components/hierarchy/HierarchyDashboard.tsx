/**
 * HierarchyDashboard - Sprint 2.1.7.7 D5: Multi-Location Management
 *
 * Dashboard für Standort-Übersicht bei HEADQUARTER Kunden.
 * Zeigt Gesamt-Metriken, Branch-Liste und ermöglicht Filial-Erstellung.
 *
 * Design System: MUI v7 Grid API, Theme Colors, Deutsche Sprache
 *
 * @module HierarchyDashboard
 * @since Sprint 2.1.7.7
 */

import React from 'react';
import {
  Card,
  CardHeader,
  CardContent,
  Grid,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  Typography,
  Chip,
  IconButton,
  Button,
  Alert,
  Stack,
  Box,
} from '@mui/material';
import { OpenInNew as OpenInNewIcon, Add as AddIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useHierarchyMetrics } from '../../hooks/useHierarchyMetrics';
import { MetricCard } from '../MetricCard';
import { formatCurrency } from '@/features/shared/utils/dataFormatters';

interface HierarchyDashboardProps {
  /** Parent customer (HEADQUARTER) */
  parentCustomerId: string;

  /** Callback when "Neue Filiale anlegen" clicked */
  onCreateBranch: () => void;
}

/**
 * HierarchyDashboard Component
 *
 * Displays:
 * - Overall metrics (total revenue, average, branch count, opportunities)
 * - Branch table (sorted by revenue desc)
 * - "Create Branch" action button
 *
 * @example
 * <HierarchyDashboard
 *   parentCustomerId={customer.id}
 *   onCreateBranch={() => setDialogOpen(true)}
 * />
 */
export const HierarchyDashboard: React.FC<HierarchyDashboardProps> = ({
  parentCustomerId,
  onCreateBranch,
}) => {
  const navigate = useNavigate();
  const { data: metrics, isLoading } = useHierarchyMetrics(parentCustomerId);

  if (isLoading) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography>Lädt Standort-Daten...</Typography>
      </Box>
    );
  }

  if (!metrics) {
    return null;
  }

  /**
   * Get status chip color from MUI theme
   */
  const getStatusColor = (status: string): 'success' | 'info' | 'warning' | 'error' | 'default' => {
    switch (status) {
      case 'AKTIV':
        return 'success';
      case 'PROSPECT':
        return 'info';
      case 'RISIKO':
        return 'warning';
      case 'CHURNED':
        return 'error';
      default:
        return 'default';
    }
  };

  return (
    <Stack spacing={3}>
      {/* Header with Actions */}
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Typography variant="h5">Standort-Übersicht</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={onCreateBranch}
          color="primary"
        >
          Neue Filiale anlegen
        </Button>
      </Box>

      {/* Gesamt-Metriken (Grid v2 API) */}
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, md: 3 }}>
          <MetricCard
            title="Gesamt-Umsatz (Konzern)"
            value={formatCurrency(metrics.totalRevenue)}
            subtitle={`${metrics.branchCount} Standorte`}
            color="primary"
          />
        </Grid>

        <Grid size={{ xs: 12, md: 3 }}>
          <MetricCard
            title="Durchschnitt pro Standort"
            value={formatCurrency(metrics.averageRevenue)}
            subtitle="12 Monate"
            color="info"
          />
        </Grid>

        <Grid size={{ xs: 12, md: 3 }}>
          <MetricCard
            title="Standorte"
            value={metrics.branchCount}
            subtitle="Filialen"
            color="success"
          />
        </Grid>

        <Grid size={{ xs: 12, md: 3 }}>
          <MetricCard
            title="Opportunities"
            value={metrics.totalOpenOpportunities}
            subtitle="Alle Standorte"
            color="warning"
          />
        </Grid>
      </Grid>

      {/* Alert wenn keine Standorte */}
      {metrics.branchCount === 0 && (
        <Alert severity="info">
          Noch keine Filialen angelegt. Klicken Sie auf &quot;Neue Filiale anlegen&quot; um zu
          starten.
        </Alert>
      )}

      {/* Standort-Tabelle */}
      {metrics.branchCount > 0 && (
        <Card>
          <CardHeader title="Standort-Details (sortiert nach Umsatz)" />
          <CardContent>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Standort</TableCell>
                  <TableCell align="right">Umsatz (12 Monate)</TableCell>
                  <TableCell align="right">Anteil</TableCell>
                  <TableCell align="right">Opportunities</TableCell>
                  <TableCell align="center">Status</TableCell>
                  <TableCell>Aktionen</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {metrics.branches
                  .sort((a, b) => Number(b.revenue) - Number(a.revenue))
                  .map(branch => (
                    <TableRow
                      key={branch.branchId}
                      hover
                      onClick={() => navigate(`/customers/${branch.branchId}`)}
                      sx={{ cursor: 'pointer' }}
                    >
                      <TableCell>
                        <Stack>
                          <Typography variant="body1" fontWeight="bold">
                            {branch.branchName}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {branch.city}, {branch.country}
                          </Typography>
                        </Stack>
                      </TableCell>

                      <TableCell align="right">
                        <Typography variant="h6">{formatCurrency(branch.revenue)}</Typography>
                      </TableCell>

                      <TableCell align="right">
                        <Typography variant="body2" color="text.secondary">
                          {branch.percentage}%
                        </Typography>
                      </TableCell>

                      <TableCell align="right">
                        <Chip
                          label={`${branch.openOpportunities} offen`}
                          size="small"
                          color={branch.openOpportunities > 0 ? 'primary' : 'default'}
                        />
                      </TableCell>

                      <TableCell align="center">
                        <Chip
                          label={branch.status}
                          color={getStatusColor(branch.status)}
                          size="small"
                        />
                      </TableCell>

                      <TableCell>
                        <IconButton
                          size="small"
                          onClick={e => {
                            e.stopPropagation();
                            navigate(`/customers/${branch.branchId}`);
                          }}
                        >
                          <OpenInNewIcon />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}
    </Stack>
  );
};
