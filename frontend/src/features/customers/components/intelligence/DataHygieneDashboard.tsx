import React from 'react';
import {
  Card,
  CardContent,
  Grid,
  Typography,
  Alert,
  Box,
  LinearProgress,
  Chip,
  IconButton,
  Tooltip,
  Button,
  Stack,
  Paper,
} from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  Warning,
  CheckCircle,
  Info,
  Refresh,
  Download,
  Schedule,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import {
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  Tooltip as RechartsTooltip,
  Legend,
} from 'recharts';
import { contactInteractionApi } from '../../services/contactInteractionApi';
import type { DataQualityMetricsDTO } from '../../types/intelligence.types';

// Color scheme for data freshness
const FRESHNESS_COLORS = {
  fresh: '#4caf50',
  aging: '#ff9800',
  stale: '#f44336',
  critical: '#b71c1c',
};

interface MetricCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  trend?: number;
  color?: string;
  icon?: React.ReactNode;
}

const MetricCard: React.FC<MetricCardProps> = ({
  title,
  value,
  subtitle,
  trend,
  color = '#1976d2',
  icon,
}) => {
  const getTrendIcon = () => {
    if (!trend) return null;
    if (trend > 0) return <TrendingUp sx={{ color: '#4caf50' }} />;
    if (trend < 0) return <TrendingDown sx={{ color: '#f44336' }} />;
    return null;
  };

  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Box display="flex" justifyContent="space-between" alignItems="flex-start">
          <Box>
            <Typography color="textSecondary" gutterBottom variant="body2">
              {title}
            </Typography>
            <Typography variant="h4" component="div" sx={{ color, mb: 1 }}>
              {value}
            </Typography>
            {subtitle && (
              <Typography variant="body2" color="textSecondary">
                {subtitle}
              </Typography>
            )}
          </Box>
          <Box display="flex" flexDirection="column" alignItems="flex-end">
            {icon}
            {getTrendIcon()}
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
};

const getQualityColor = (score: number): string => {
  if (score >= 80) return '#4caf50';
  if (score >= 60) return '#ff9800';
  if (score >= 40) return '#ff5722';
  return '#f44336';
};

const getQualityLabel = (quality: string): { label: string; color: string } => {
  const map: Record<string, { label: string; color: string }> = {
    EXCELLENT: { label: 'Exzellent', color: '#4caf50' },
    GOOD: { label: 'Gut', color: '#8bc34a' },
    FAIR: { label: 'Befriedigend', color: '#ff9800' },
    POOR: { label: 'Mangelhaft', color: '#ff5722' },
    CRITICAL: { label: 'Kritisch', color: '#f44336' },
    UNKNOWN: { label: 'Unbekannt', color: '#9e9e9e' },
  };
  return map[quality] || map.UNKNOWN;
};

export const DataHygieneDashboard: React.FC = () => {
  const {
    data: metrics,
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ['dataQualityMetrics'],
    queryFn: contactInteractionApi.getDataQualityMetrics,
    refetchInterval: 60000, // Refresh every minute
  });

  if (isLoading) {
    return (
      <Box p={3}>
        <LinearProgress />
      </Box>
    );
  }

  if (error) {
    return <Alert severity="error">Fehler beim Laden der Datenqualitäts-Metriken</Alert>;
  }

  if (!metrics) {
    return null;
  }

  // Prepare data for pie chart
  const freshnessData = [
    {
      name: 'Aktuell (<90 Tage)',
      value: metrics.freshContacts || 0,
      color: FRESHNESS_COLORS.fresh,
    },
    {
      name: 'Veraltet (90-180 Tage)',
      value: metrics.agingContacts || 0,
      color: FRESHNESS_COLORS.aging,
    },
    {
      name: 'Stark veraltet (180-365 Tage)',
      value: metrics.staleContacts || 0,
      color: FRESHNESS_COLORS.stale,
    },
    {
      name: 'Kritisch (>365 Tage)',
      value: metrics.criticalContacts || 0,
      color: FRESHNESS_COLORS.critical,
    },
  ].filter(item => item.value > 0);

  const qualityInfo = getQualityLabel(metrics.overallDataQuality || 'UNKNOWN');

  return (
    <Box>
      {/* Header */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" component="h2">
          Data Intelligence Dashboard
        </Typography>
        <Box>
          <Tooltip title="Daten aktualisieren">
            <IconButton onClick={() => refetch()} size="small">
              <Refresh />
            </IconButton>
          </Tooltip>
          <Tooltip title="Bericht exportieren">
            <IconButton size="small">
              <Download />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      {/* Key Metrics */}
      <Grid container spacing={3} mb={3}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <MetricCard
            title="Datenqualität Gesamt"
            value={`${Math.round(metrics.dataCompletenessScore || 0)}%`}
            subtitle={qualityInfo.label}
            color={qualityInfo.color}
            icon={<CheckCircle sx={{ color: qualityInfo.color }} />}
          />
        </Grid>

        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <MetricCard
            title="Kontakte mit Interaktionen"
            value={`${Math.round(metrics.interactionCoverage || 0)}%`}
            subtitle={`${metrics.contactsWithInteractions} von ${metrics.totalContacts}`}
            color={metrics.interactionCoverage! >= 50 ? '#4caf50' : '#ff9800'}
          />
        </Grid>

        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <MetricCard
            title="Ø Interaktionen"
            value={metrics.averageInteractionsPerContact?.toFixed(1) || '0'}
            subtitle="pro Kontakt"
            icon={<Schedule sx={{ color: '#1976d2' }} />}
          />
        </Grid>

        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <MetricCard
            title="Warmth Score Abdeckung"
            value={`${metrics.contactsWithWarmthScore || 0}`}
            subtitle="Kontakte mit Score"
            color="#9c27b0"
          />
        </Grid>
      </Grid>

      {/* Data Freshness Chart */}
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Kontakte nach Aktualität
              </Typography>

              {freshnessData.length > 0 ? (
                <Box height={250}>
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={freshnessData}
                        cx="50%"
                        cy="50%"
                        labelLine={false}
                        label={({ value, percent }) => `${value} (${(percent * 100).toFixed(0)}%)`}
                        outerRadius={80}
                        fill="#8884d8"
                        dataKey="value"
                      >
                        {freshnessData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={entry.color} />
                        ))}
                      </Pie>
                      <RechartsTooltip />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                </Box>
              ) : (
                <Box height={250} display="flex" alignItems="center" justifyContent="center">
                  <Typography color="textSecondary">Keine Kontaktdaten vorhanden</Typography>
                </Box>
              )}

              {metrics.criticalContacts! > 0 && (
                <Alert severity="error" sx={{ mt: 2 }}>
                  <Typography variant="body2">
                    {metrics.criticalContacts} Kontakte wurden über 1 Jahr nicht aktualisiert!
                  </Typography>
                  <Button size="small" sx={{ mt: 1 }}>
                    Kritische Kontakte anzeigen
                  </Button>
                </Alert>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Recommendations */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Empfehlungen zur Datenqualität
              </Typography>

              {metrics.showDataCollectionHints && (
                <Stack spacing={2}>
                  {metrics.criticalDataGaps?.map((gap, index) => (
                    <Alert key={index} severity="warning" icon={<Warning />}>
                      {gap}
                    </Alert>
                  ))}

                  <Typography variant="subtitle2" color="textSecondary" sx={{ mt: 2 }}>
                    Verbesserungsvorschläge:
                  </Typography>

                  {metrics.improvementSuggestions?.map((suggestion, index) => (
                    <Paper key={index} sx={{ p: 2, bgcolor: 'grey.50' }}>
                      <Box display="flex" alignItems="center" gap={1}>
                        <Info color="action" />
                        <Typography variant="body2">{suggestion}</Typography>
                      </Box>
                    </Paper>
                  ))}
                </Stack>
              )}

              {!metrics.showDataCollectionHints && (
                <Alert severity="success">Ihre Datenqualität ist ausgezeichnet! Weiter so!</Alert>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Data Quality Progress */}
      <Box mt={3}>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Datenqualitäts-Fortschritt
            </Typography>
            <Typography variant="body2" color="textSecondary" gutterBottom>
              Je mehr Interaktionen erfasst werden, desto präziser werden die Intelligenz-Features
            </Typography>

            <Box mt={3}>
              <Stack spacing={2}>
                <Box>
                  <Box display="flex" justifyContent="space-between" mb={1}>
                    <Typography variant="body2">Bootstrap Phase (0-7 Tage)</Typography>
                    <Chip label="Aktuell" size="small" color="primary" />
                  </Box>
                  <LinearProgress variant="determinate" value={30} />
                  <Typography variant="caption" color="textSecondary">
                    Basic CRUD, Manuelle Notizen
                  </Typography>
                </Box>

                <Box>
                  <Box display="flex" justifyContent="space-between" mb={1}>
                    <Typography variant="body2">Learning Phase (7-30 Tage)</Typography>
                  </Box>
                  <LinearProgress variant="determinate" value={0} />
                  <Typography variant="caption" color="textSecondary">
                    Erste Warmth Trends, Simple Vorschläge
                  </Typography>
                </Box>

                <Box>
                  <Box display="flex" justifyContent="space-between" mb={1}>
                    <Typography variant="body2">Intelligent Phase (30+ Tage)</Typography>
                  </Box>
                  <LinearProgress variant="determinate" value={0} />
                  <Typography variant="caption" color="textSecondary">
                    Predictive Analytics, Smart Recommendations
                  </Typography>
                </Box>
              </Stack>
            </Box>
          </CardContent>
        </Card>
      </Box>
    </Box>
  );
};
