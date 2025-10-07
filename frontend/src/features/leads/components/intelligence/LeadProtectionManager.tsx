/**
 * Schutzfristen-Manager für Leads
 *
 * Zeigt Lead-Schutzfristen-Status und ermöglicht Bulk-Aktionen
 *
 * Sprint 2.1.6 Phase 4 - Frontend-only mit Mock-Daten
 * Sprachregeln: DESIGN_SYSTEM.md konform
 */

import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Alert,
  AlertTitle,
  Grid,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Chip,
  Paper,
  IconButton,
  Tooltip,
  LinearProgress,
} from '@mui/material';
import {
  CheckCircle,
  Warning,
  Error,
  Schedule,
  Pause,
  Archive,
  Refresh,
  TrendingDown,
  TrendingUp,
} from '@mui/icons-material';
import {
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  Tooltip as RechartsTooltip,
  Legend,
} from 'recharts';

// Mock-Daten - später durch Backend ersetzen
const MOCK_PROTECTION_DATA = {
  totalLeads: 38,
  protectionStatus: {
    active: 23,      // Innerhalb 60 Tage Schutzfrist
    expiring: 12,    // <10 Tage bis Ablauf
    expired: 5,      // Schutzfrist abgelaufen
    forgotten: 3,    // >90 Tage kein Kontakt
  },
  paused: 4,         // Leads mit pausierter Schutzfrist
  averageDaysRemaining: 32,
  expiringNextWeek: 8,
  criticalLeads: [
    { id: 1, companyName: 'Bäckerei Müller GmbH', daysRemaining: 2, lastContact: '2025-10-05', reason: 'Urlaub' },
    { id: 2, companyName: 'Restaurant Schmidt', daysRemaining: 5, lastContact: '2025-10-02', reason: null },
    { id: 3, companyName: 'Hotel Meier & Co', daysRemaining: 7, lastContact: '2025-09-30', reason: null },
  ],
  forgottenLeads: [
    { id: 4, companyName: 'Konditorei Wagner', daysSinceContact: 120, lastContact: '2025-06-08' },
    { id: 5, companyName: 'Café Braun', daysSinceContact: 95, lastContact: '2025-07-03' },
    { id: 6, companyName: 'Pizzeria Rossi', daysSinceContact: 180, lastContact: '2025-04-09' },
  ],
};

// Farben für Schutzfristen-Status (FreshFoodz CI)
const STATUS_COLORS = {
  active: '#94C456',    // FreshFoodz Grün
  expiring: '#FF9800',  // Orange
  expired: '#F44336',   // Rot
  forgotten: '#B71C1C', // Dunkelrot
};

interface MetricCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  color?: string;
  icon?: React.ReactNode;
  onClick?: () => void;
}

const MetricCard: React.FC<MetricCardProps> = ({
  title,
  value,
  subtitle,
  color = '#004F7B',
  icon,
  onClick,
}) => {
  return (
    <Card
      sx={{
        height: '100%',
        cursor: onClick ? 'pointer' : 'default',
        '&:hover': onClick ? { boxShadow: 4 } : {},
      }}
      onClick={onClick}
    >
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
          {icon && <Box>{icon}</Box>}
        </Box>
      </CardContent>
    </Card>
  );
};

export const LeadProtectionManager: React.FC = () => {
  const [selectedStatus, setSelectedStatus] = useState<'expiring' | 'expired' | 'forgotten' | null>(null);
  const data = MOCK_PROTECTION_DATA;

  // Schutzfristen-Verteilung für Pie Chart
  const statusData = [
    { name: 'Aktiv', value: data.protectionStatus.active, color: STATUS_COLORS.active },
    { name: 'Läuft ab', value: data.protectionStatus.expiring, color: STATUS_COLORS.expiring },
    { name: 'Abgelaufen', value: data.protectionStatus.expired, color: STATUS_COLORS.expired },
    { name: 'Vergessen', value: data.protectionStatus.forgotten, color: STATUS_COLORS.forgotten },
  ];

  const handleBulkAction = (action: string) => {
    console.log(`Bulk action: ${action}`, selectedStatus);
    // TODO: Backend-Integration
    alert(`Aktion "${action}" für ${selectedStatus}-Leads wird ausgeführt (später implementiert)`);
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" gutterBottom sx={{ mb: 3, color: '#004F7B', fontWeight: 'bold' }}>
        Schutzfristen-Verwaltung
      </Typography>

      {/* KPIs */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <MetricCard
            title="Aktive Schutzfristen"
            value={data.protectionStatus.active}
            subtitle="Innerhalb 60 Tage"
            color="#94C456"
            icon={<CheckCircle sx={{ fontSize: 40, color: '#94C456' }} />}
          />
        </Grid>

        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <MetricCard
            title="Laufen bald ab"
            value={data.protectionStatus.expiring}
            subtitle="<10 Tage verbleibend"
            color="#FF9800"
            icon={<Warning sx={{ fontSize: 40, color: '#FF9800' }} />}
            onClick={() => setSelectedStatus('expiring')}
          />
        </Grid>

        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <MetricCard
            title="Abgelaufene Schutzfristen"
            value={data.protectionStatus.expired}
            subtitle="Aktion erforderlich"
            color="#F44336"
            icon={<Error sx={{ fontSize: 40, color: '#F44336' }} />}
            onClick={() => setSelectedStatus('expired')}
          />
        </Grid>

        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <MetricCard
            title="Vergessene Leads"
            value={data.protectionStatus.forgotten}
            subtitle=">90 Tage kein Kontakt"
            color="#B71C1C"
            icon={<TrendingDown sx={{ fontSize: 40, color: '#B71C1C' }} />}
            onClick={() => setSelectedStatus('forgotten')}
          />
        </Grid>
      </Grid>

      {/* Schutzfristen-Übersicht */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        {/* Pie Chart */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 2, height: '100%' }}>
            <Typography variant="h6" gutterBottom sx={{ color: '#004F7B' }}>
              Schutzfristen-Verteilung
            </Typography>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={statusData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, value }) => `${name}: ${value}`}
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {statusData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <RechartsTooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* Statistiken */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 2, height: '100%' }}>
            <Typography variant="h6" gutterBottom sx={{ color: '#004F7B' }}>
              Kennzahlen
            </Typography>
            <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 3 }}>
              <Box>
                <Box display="flex" justifyContent="space-between" mb={1}>
                  <Typography variant="body2">Durchschn. verbleibende Tage</Typography>
                  <Typography variant="body2" fontWeight="bold">
                    {data.averageDaysRemaining} Tage
                  </Typography>
                </Box>
                <LinearProgress
                  variant="determinate"
                  value={(data.averageDaysRemaining / 60) * 100}
                  sx={{
                    height: 10,
                    borderRadius: 5,
                    backgroundColor: '#E0E0E0',
                    '& .MuiLinearProgress-bar': { backgroundColor: '#94C456' },
                  }}
                />
              </Box>

              <Box>
                <Typography variant="body2" gutterBottom>
                  Ablauf nächste 7 Tage:
                </Typography>
                <Chip
                  label={`${data.expiringNextWeek} Leads`}
                  color="warning"
                  size="medium"
                  sx={{ fontWeight: 'bold' }}
                />
              </Box>

              <Box>
                <Typography variant="body2" gutterBottom>
                  Pausierte Schutzfristen:
                </Typography>
                <Chip
                  label={`${data.paused} Leads`}
                  color="default"
                  size="medium"
                  icon={<Pause />}
                />
              </Box>
            </Box>
          </Paper>
        </Grid>
      </Grid>

      {/* Kritische Leads */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, md: 6 }}>
          <Alert severity="error">
            <AlertTitle>🚨 Kritisch: Läuft bald ab</AlertTitle>
            <Typography variant="body2" gutterBottom>
              Diese Leads verlieren in Kürze ihre Schutzfrist:
            </Typography>
            <List dense>
              {data.criticalLeads.map((lead) => (
                <ListItem key={lead.id} sx={{ px: 0 }}>
                  <ListItemIcon>
                    <Warning color="error" />
                  </ListItemIcon>
                  <ListItemText
                    primary={lead.companyName}
                    secondary={
                      <>
                        <Chip
                          label={`${lead.daysRemaining} Tage`}
                          size="small"
                          color="error"
                          sx={{ height: 20, mr: 1 }}
                        />
                        {lead.reason && (
                          <Chip
                            label={`⏸️ ${lead.reason}`}
                            size="small"
                            variant="outlined"
                            sx={{ height: 20, mr: 1 }}
                          />
                        )}
                        <Typography component="span" variant="caption" color="text.secondary">
                          Letzter Kontakt: {new Date(lead.lastContact).toLocaleDateString('de-DE')}
                        </Typography>
                      </>
                    }
                    secondaryTypographyProps={{ component: 'div' }}
                  />
                  <Tooltip title="Jetzt kontaktieren">
                    <IconButton size="small" color="primary">
                      <Refresh />
                    </IconButton>
                  </Tooltip>
                </ListItem>
              ))}
            </List>
            <Box sx={{ mt: 2, display: 'flex', gap: 1 }}>
              <Button
                variant="contained"
                size="small"
                startIcon={<Refresh />}
                onClick={() => handleBulkAction('verlängern')}
              >
                Alle verlängern
              </Button>
              <Button
                variant="outlined"
                size="small"
                startIcon={<Pause />}
                onClick={() => handleBulkAction('pausieren')}
              >
                Pausieren
              </Button>
            </Box>
          </Alert>
        </Grid>

        <Grid size={{ xs: 12, md: 6 }}>
          <Alert severity="warning">
            <AlertTitle>⚠️ Vergessene Leads</AlertTitle>
            <Typography variant="body2" gutterBottom>
              Diese Leads haben seit über 90 Tagen keinen Kontakt:
            </Typography>
            <List dense>
              {data.forgottenLeads.map((lead) => (
                <ListItem key={lead.id} sx={{ px: 0 }}>
                  <ListItemIcon>
                    <TrendingDown color="warning" />
                  </ListItemIcon>
                  <ListItemText
                    primary={lead.companyName}
                    secondary={
                      <>
                        <Chip
                          label={`${lead.daysSinceContact} Tage`}
                          size="small"
                          color="warning"
                          sx={{ height: 20, mr: 1 }}
                        />
                        <Typography component="span" variant="caption" color="text.secondary">
                          Letzter Kontakt: {new Date(lead.lastContact).toLocaleDateString('de-DE')}
                        </Typography>
                      </>
                    }
                    secondaryTypographyProps={{ component: 'div' }}
                  />
                  <Tooltip title="Archivieren">
                    <IconButton size="small" color="default">
                      <Archive />
                    </IconButton>
                  </Tooltip>
                </ListItem>
              ))}
            </List>
            <Box sx={{ mt: 2, display: 'flex', gap: 1 }}>
              <Button
                variant="contained"
                size="small"
                startIcon={<Archive />}
                onClick={() => handleBulkAction('archivieren')}
              >
                Alle archivieren
              </Button>
              <Button
                variant="outlined"
                size="small"
                startIcon={<Refresh />}
                onClick={() => handleBulkAction('reaktivieren')}
              >
                Reaktivieren
              </Button>
            </Box>
          </Alert>
        </Grid>
      </Grid>

      {/* Empfehlungen */}
      <Alert severity="info">
        <AlertTitle>💡 Empfehlungen</AlertTitle>
        <List dense>
          <ListItem>
            <ListItemIcon>
              <Schedule color="info" />
            </ListItemIcon>
            <ListItemText
              primary="Tägliche Prüfung"
              secondary={`${data.expiringNextWeek} Leads laufen in den nächsten 7 Tagen ab - tägliche Überprüfung empfohlen`}
            />
          </ListItem>
          <ListItem>
            <ListItemIcon>
              <Warning color="info" />
            </ListItemIcon>
            <ListItemText
              primary="Vergessene Leads aufräumen"
              secondary={`${data.protectionStatus.forgotten} Leads seit >90 Tagen ohne Kontakt - Archivierung oder Reaktivierung prüfen`}
            />
          </ListItem>
          <ListItem>
            <ListItemIcon>
              <TrendingUp color="info" />
            </ListItemIcon>
            <ListItemText
              primary="Pausenfunktion nutzen"
              secondary="Bei Urlaub oder geplanten Kontaktpausen die Schutzfrist pausieren statt ablaufen lassen"
            />
          </ListItem>
        </List>
      </Alert>
    </Box>
  );
};
