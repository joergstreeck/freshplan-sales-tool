import React from 'react';
import { Grid } from '@mui/material';
import {
  Box,
  Paper,
  Typography,
  Alert,
  AlertTitle,

  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Chip,
  Button,
  Divider as _Divider,
} from '@mui/material';
import {
  CheckCircle as CheckIcon,
  Warning as WarningIcon,
  Error as ErrorIcon,
  Info as InfoIcon,
  GetApp as DownloadIcon,
  Schedule as ScheduleIcon,
} from '@mui/icons-material';
import type { ComplianceAlert, AuditDashboardMetrics } from '../types';

interface CompliancePanelProps {
  alerts?: ComplianceAlert[];
  metrics?: AuditDashboardMetrics;
  _dateRange: {
    from: Date;
    to: Date;
  };
}

export const CompliancePanel: React.FC<CompliancePanelProps> = ({
  alerts = [],
  metrics,
  _dateRange,
}) => {
  const getAlertIcon = (type: string) => {
    switch (type) {
      case 'ERROR':
        return <ErrorIcon color="error" />;
      case 'WARNING':
        return <WarningIcon color="warning" />;
      case 'INFO':
        return <InfoIcon color="info" />;
      default:
        return <InfoIcon />;
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'CRITICAL':
        return 'error';
      case 'HIGH':
        return 'error';
      case 'MEDIUM':
        return 'warning';
      case 'LOW':
        return 'info';
      default:
        return 'default';
    }
  };

  const criticalAlerts = alerts.filter(a => a.severity === 'CRITICAL' || a.severity === 'HIGH');
  const warningAlerts = alerts.filter(a => a.severity === 'MEDIUM');
  const infoAlerts = alerts.filter(a => a.severity === 'LOW');

  return (
    <Box>
      <Grid container spacing={3}>
        {/* Compliance Score Overview */}
        <Grid size={{ xs: 12 }}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h5" gutterBottom sx={{ fontFamily: 'Antonio, sans-serif', mb: 3 }}>
              DSGVO Compliance Übersicht
            </Typography>

            <Grid container spacing={3}>
              <Grid size={{ xs: 12, md: 4 }}>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography
                    variant="h2"
                    sx={{
                      fontFamily: 'Antonio, sans-serif',
                      color:
                        metrics?.retentionCompliance && metrics.retentionCompliance >= 80
                          ? '#94C456'
                          : '#ff9800',
                    }}
                  >
                    {metrics?.retentionCompliance || 0}%
                  </Typography>
                  <Typography variant="subtitle1" color="text.secondary">
                    Gesamt-Compliance
                  </Typography>
                </Box>
              </Grid>

              <Grid size={{ xs: 12, md: 8 }}>
                <List dense>
                  <ListItem>
                    <ListItemIcon>
                      <CheckIcon color="success" />
                    </ListItemIcon>
                    <ListItemText
                      primary="Audit-Logs verschlüsselt"
                      secondary="Alle Audit-Einträge werden verschlüsselt gespeichert"
                    />
                  </ListItem>
                  <ListItem>
                    <ListItemIcon>
                      <CheckIcon color="success" />
                    </ListItemIcon>
                    <ListItemText
                      primary="Hash-Chain intakt"
                      secondary="Manipulationssicherheit durch kryptografische Verkettung"
                    />
                  </ListItem>
                  <ListItem>
                    <ListItemIcon>
                      {metrics?.integrityStatus === 'valid' ? (
                        <CheckIcon color="success" />
                      ) : (
                        <WarningIcon color="warning" />
                      )}
                    </ListItemIcon>
                    <ListItemText
                      primary="Integritätsprüfung"
                      secondary={`Status: ${metrics?.integrityStatus === 'valid' ? 'Gültig' : 'Prüfung erforderlich'}`}
                    />
                  </ListItem>
                </List>
              </Grid>
            </Grid>
          </Paper>
        </Grid>

        {/* Critical Alerts */}
        {criticalAlerts.length > 0 && (
          <Grid size={{ xs: 12 }}>
            <Alert severity="error">
              <AlertTitle sx={{ fontWeight: 'bold' }}>
                Kritische Compliance-Probleme ({criticalAlerts.length})
              </AlertTitle>
              <List dense>
                {criticalAlerts.map((alert, index) => (
                  <ListItem key={index}>
                    <ListItemIcon sx={{ minWidth: 36 }}>{getAlertIcon(alert.type)}</ListItemIcon>
                    <ListItemText
                      primary={alert.message}
                      secondary={
                        <Chip label={alert.severity} size="small" color="error" sx={{ mt: 0.5 }} />
                      }
                    />
                  </ListItem>
                ))}
              </List>
            </Alert>
          </Grid>
        )}

        {/* Warning Alerts */}
        {warningAlerts.length > 0 && (
          <Grid size={{ xs: 12 }}>
            <Alert severity="warning">
              <AlertTitle sx={{ fontWeight: 'bold' }}>
                Warnungen ({warningAlerts.length})
              </AlertTitle>
              <List dense>
                {warningAlerts.map((alert, index) => (
                  <ListItem key={index}>
                    <ListItemIcon sx={{ minWidth: 36 }}>{getAlertIcon(alert.type)}</ListItemIcon>
                    <ListItemText primary={alert.message} />
                  </ListItem>
                ))}
              </List>
            </Alert>
          </Grid>
        )}

        {/* Info Alerts */}
        {infoAlerts.length > 0 && (
          <Grid size={{ xs: 12 }}>
            <Alert severity="info">
              <AlertTitle sx={{ fontWeight: 'bold' }}>Hinweise ({infoAlerts.length})</AlertTitle>
              <List dense>
                {infoAlerts.map((alert, index) => (
                  <ListItem key={index}>
                    <ListItemIcon sx={{ minWidth: 36 }}>{getAlertIcon(alert.type)}</ListItemIcon>
                    <ListItemText primary={alert.message} />
                  </ListItem>
                ))}
              </List>
            </Alert>
          </Grid>
        )}

        {/* Retention Policies */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom sx={{ fontFamily: 'Antonio, sans-serif' }}>
              Aufbewahrungsrichtlinien
            </Typography>

            <List>
              <ListItem>
                <ListItemIcon>
                  <ScheduleIcon />
                </ListItemIcon>
                <ListItemText primary="Standard Audit-Logs" secondary="90 Tage Aufbewahrung" />
              </ListItem>
              <ListItem>
                <ListItemIcon>
                  <ScheduleIcon />
                </ListItemIcon>
                <ListItemText
                  primary="DSGVO-relevante Logs"
                  secondary="Gemäß gesetzlicher Vorgaben"
                />
              </ListItem>
              <ListItem>
                <ListItemIcon>
                  <ScheduleIcon />
                </ListItemIcon>
                <ListItemText
                  primary="Sicherheitskritische Events"
                  secondary="365 Tage Aufbewahrung"
                />
              </ListItem>
            </List>
          </Paper>
        </Grid>

        {/* Report Generation */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom sx={{ fontFamily: 'Antonio, sans-serif' }}>
              Compliance Reports
            </Typography>

            <Box sx={{ mt: 2 }}>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Generieren Sie DSGVO-konforme Berichte für den ausgewählten Zeitraum
              </Typography>

              <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 1 }}>
                <Button
                  variant="outlined"
                  startIcon={<DownloadIcon />}
                  fullWidth
                  sx={{ justifyContent: 'flex-start' }}
                >
                  DSGVO Compliance Report
                </Button>
                <Button
                  variant="outlined"
                  startIcon={<DownloadIcon />}
                  fullWidth
                  sx={{ justifyContent: 'flex-start' }}
                >
                  Audit Trail Vollständigkeitsprüfung
                </Button>
                <Button
                  variant="outlined"
                  startIcon={<DownloadIcon />}
                  fullWidth
                  sx={{ justifyContent: 'flex-start' }}
                >
                  Berechtigungsaudit
                </Button>
              </Box>
            </Box>
          </Paper>
        </Grid>

        {/* No Issues */}
        {alerts.length === 0 && (
          <Grid size={{ xs: 12 }}>
            <Alert severity="success">
              <AlertTitle sx={{ fontWeight: 'bold' }}>
                Keine Compliance-Probleme gefunden
              </AlertTitle>
              Alle Compliance-Anforderungen werden erfüllt. Die Audit-Logs sind vollständig und
              manipulationssicher.
            </Alert>
          </Grid>
        )}
      </Grid>
    </Box>
  );
};
