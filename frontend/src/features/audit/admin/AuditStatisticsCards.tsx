import React from 'react';
import {
  Grid,
  Card,
  CardContent,
  Typography,
  Box,
  Skeleton,
  Tooltip as _Tooltip,
  IconButton as _IconButton,
  Chip,
  LinearProgress,
} from '@mui/material';
import {
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  Security as SecurityIcon,
  People as PeopleIcon,
  Warning as WarningIcon,
  CheckCircle as CheckCircleIcon,
  Storage as StorageIcon,
  Speed as SpeedIcon,
  Info as _InfoIcon,
  Assessment as AssessmentIcon,
} from '@mui/icons-material';
import { format } from 'date-fns';
import { de as _de } from 'date-fns/locale';

interface StatCard {
  title: string;
  value: number | string;
  change?: number;
  changeLabel?: string;
  icon: React.ReactNode;
  color: string;
  subtitle?: string;
  severity?: 'success' | 'warning' | 'error' | 'info';
}

interface AuditStatisticsCardsProps {
  stats?: {
    totalEvents: number;
    criticalEvents: number;
    dsgvoRelevantEvents: number;
    activeUsers: number;
    complianceScore: number;
    openSecurityAlerts: number;
    integrityValid: boolean;
    averageResponseTime?: number;
    eventsByType?: Record<string, number>;
  };
  dateRange?: {
    from: Date;
    to: Date;
  };
  isLoading?: boolean;
}

export const AuditStatisticsCards: React.FC<AuditStatisticsCardsProps> = ({
  stats,
  _dateRange,
  isLoading = false,
}) => {
  const formatNumber = (num: number): string => {
    if (num >= 1000000) {
      return `${(num / 1000000).toFixed(1)}M`;
    }
    if (num >= 1000) {
      return `${(num / 1000).toFixed(1)}K`;
    }
    return num.toString();
  };

  const getChangeIcon = (change: number) => {
    if (change > 0) {
      return <TrendingUpIcon fontSize="small" color="success" />;
    }
    if (change < 0) {
      return <TrendingDownIcon fontSize="small" color="error" />;
    }
    return null;
  };

  const getComplianceColor = (score: number): 'success' | 'warning' | 'error' => {
    if (score >= 90) return 'success';
    if (score >= 70) return 'warning';
    return 'error';
  };

  const cards: StatCard[] = stats
    ? [
        {
          title: 'Gesamt-Events',
          value: formatNumber(stats.totalEvents),
          icon: <StorageIcon />,
          color: 'secondary.main',
          subtitle: dateRange
            ? `${format(dateRange.from, 'dd.MM.')} - ${format(dateRange.to, 'dd.MM.yyyy')}`
            : 'Letzten 7 Tage',
        },
        {
          title: 'Kritische Events',
          value: formatNumber(stats.criticalEvents),
          icon: <WarningIcon />,
          color: stats.criticalEvents > 0 ? 'error.main' : 'success.main',
          severity:
            stats.criticalEvents > 10 ? 'error' : stats.criticalEvents > 5 ? 'warning' : 'success',
          subtitle: 'DELETE, BULK_DELETE, PERMISSION_CHANGE',
        },
        {
          title: 'DSGVO-Relevant',
          value: formatNumber(stats.dsgvoRelevantEvents),
          icon: <SecurityIcon />,
          color: 'info.main',
          subtitle: 'Personenbezogene Daten',
        },
        {
          title: 'Aktive Benutzer',
          value: formatNumber(stats.activeUsers),
          icon: <PeopleIcon />,
          color: 'primary.main',
          subtitle: 'Unique Users im Zeitraum',
        },
        {
          title: 'Compliance Score',
          value: `${stats.complianceScore.toFixed(1)}%`,
          icon: <AssessmentIcon />,
          color:
            getComplianceColor(stats.complianceScore) === 'success'
              ? 'success.main'
              : getComplianceColor(stats.complianceScore) === 'warning'
                ? 'warning.main'
                : 'error.main',
          severity: getComplianceColor(stats.complianceScore),
          subtitle: stats.complianceScore < 80 ? 'Maßnahmen erforderlich!' : 'Gut',
        },
        {
          title: 'Sicherheits-Alerts',
          value: stats.openSecurityAlerts,
          icon: stats.openSecurityAlerts > 0 ? <WarningIcon /> : <CheckCircleIcon />,
          color: stats.openSecurityAlerts > 0 ? 'error.main' : 'success.main',
          severity: stats.openSecurityAlerts > 0 ? 'error' : 'success',
          subtitle: stats.openSecurityAlerts > 0 ? 'Sofortige Prüfung!' : 'Keine offenen Alerts',
        },
        {
          title: 'Hash-Chain Status',
          value: stats.integrityValid ? 'Intakt' : 'Kompromittiert',
          icon: stats.integrityValid ? <CheckCircleIcon /> : <WarningIcon />,
          color: stats.integrityValid ? 'success.main' : 'error.main',
          severity: stats.integrityValid ? 'success' : 'error',
          subtitle: stats.integrityValid
            ? 'Audit-Integrität gewährleistet'
            : 'MANIPULATION ERKANNT!',
        },
        {
          title: 'Ø Response Time',
          value: stats.averageResponseTime ? `${stats.averageResponseTime}ms` : '-',
          icon: <SpeedIcon />,
          color:
            stats.averageResponseTime && stats.averageResponseTime < 200
              ? 'success.main'
              : 'warning.main',
          subtitle: 'API Performance',
        },
      ]
    : [];

  if (isLoading) {
    return (
      <Grid container spacing={2}>
        {[...Array(8)].map((_, index) => (
          <Grid size={{ xs: 12, sm: 6, md: 3 }} key={index}>
            <Card sx={{ height: '100%' }}>
              <CardContent>
                <Skeleton variant="text" width="60%" />
                <Skeleton variant="text" width="40%" height={40} />
                <Skeleton variant="text" width="80%" />
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    );
  }

  if (!stats) {
    return (
      <Box sx={{ p: 3, textAlign: 'center' }}>
        <Typography color="text.secondary">Keine Statistikdaten verfügbar</Typography>
      </Box>
    );
  }

  return (
    <Grid container spacing={2}>
      {cards.map((card, index) => (
        <Grid size={{ xs: 12, sm: 6, md: 3 }} key={index}>
          <Card
            sx={{
              height: '100%',
              position: 'relative',
              overflow: 'visible',
              transition: 'transform 0.2s, box-shadow 0.2s',
              '&:hover': {
                transform: 'translateY(-4px)',
                boxShadow: 4,
              },
            }}
          >
            {/* Colored accent bar */}
            <Box
              sx={{
                position: 'absolute',
                top: 0,
                left: 0,
                right: 0,
                height: 4,
                bgcolor: card.color,
              }}
            />

            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="flex-start">
                <Box flex={1}>
                  <Typography
                    color="text.secondary"
                    gutterBottom
                    variant="caption"
                    sx={{
                      fontFamily: theme => theme.typography.body1.fontFamily,
                      fontWeight: 500,
                      textTransform: 'uppercase',
                      letterSpacing: '0.5px',
                    }}
                  >
                    {card.title}
                  </Typography>

                  <Typography
                    variant="h4"
                    component="div"
                    sx={{
                      fontFamily: theme => theme.typography.h4.fontFamily,
                      fontWeight: 'bold',
                      color:
                        card.severity === 'error'
                          ? 'error.main'
                          : card.severity === 'warning'
                            ? 'warning.main'
                            : card.severity === 'success'
                              ? 'success.main'
                              : 'text.primary',
                      mb: 1,
                    }}
                  >
                    {card.value}
                  </Typography>

                  {card.subtitle && (
                    <Typography
                      variant="caption"
                      color="text.secondary"
                      sx={{
                        display: 'block',
                        fontFamily: theme => theme.typography.body1.fontFamily,
                        fontSize: '0.7rem',
                      }}
                    >
                      {card.subtitle}
                    </Typography>
                  )}

                  {card.change !== undefined && (
                    <Box display="flex" alignItems="center" gap={0.5} mt={1}>
                      {getChangeIcon(card.change)}
                      <Typography
                        variant="caption"
                        color={card.change > 0 ? 'success.main' : 'error.main'}
                      >
                        {Math.abs(card.change)}% {card.changeLabel || 'vs. Vorperiode'}
                      </Typography>
                    </Box>
                  )}
                </Box>

                <Box
                  sx={{
                    color: card.color,
                    opacity: 0.8,
                  }}
                >
                  {card.icon}
                </Box>
              </Box>

              {/* Progress bar for compliance score */}
              {card.title === 'Compliance Score' && stats && (
                <Box sx={{ mt: 2 }}>
                  <LinearProgress
                    variant="determinate"
                    value={stats.complianceScore}
                    color={getComplianceColor(stats.complianceScore)}
                    sx={{ height: 6, borderRadius: 3 }}
                  />
                </Box>
              )}

              {/* Alert chip for critical values */}
              {card.severity === 'error' && (
                <Chip
                  label="Aktion erforderlich"
                  color="error"
                  size="small"
                  sx={{
                    position: 'absolute',
                    top: 8,
                    right: 8,
                    fontSize: '0.65rem',
                  }}
                />
              )}
            </CardContent>
          </Card>
        </Grid>
      ))}

      {/* Event Type Distribution - Additional Card */}
      {stats.eventsByType && (
        <Grid size={{ xs: 12 }}>
          <Card>
            <CardContent>
              <Typography
                variant="h6"
                gutterBottom
                sx={{ fontFamily: theme => theme.typography.h4.fontFamily, fontWeight: 'bold' }}
              >
                Event-Verteilung nach Typ
              </Typography>
              <Grid container spacing={2}>
                {Object.entries(stats.eventsByType).map(([type, count]) => (
                  <Grid size={{ xs: 6, sm: 4, md: 2 }} key={type}>
                    <Box textAlign="center">
                      <Typography
                        variant="caption"
                        color="text.secondary"
                        sx={{ fontFamily: theme => theme.typography.body1.fontFamily }}
                      >
                        {type}
                      </Typography>
                      <Typography
                        variant="h6"
                        sx={{
                          fontFamily: theme => theme.typography.h4.fontFamily,
                          fontWeight: 'bold',
                        }}
                      >
                        {formatNumber(count)}
                      </Typography>
                    </Box>
                  </Grid>
                ))}
              </Grid>
            </CardContent>
          </Card>
        </Grid>
      )}
    </Grid>
  );
};
