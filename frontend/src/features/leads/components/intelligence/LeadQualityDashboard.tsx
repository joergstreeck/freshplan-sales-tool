/**
 * Lead-Qualität Dashboard
 *
 * Zeigt Lead-Score-Verteilung, Qualifizierungs-Level und kritische Datenlücken
 *
 * Sprint 2.1.6 Phase 4 - Frontend-only mit Mock-Daten
 * Sprachregeln: DESIGN_SYSTEM.md konform
 */

import React from 'react';
import {
  Card,
  CardContent,
  Grid,
  Typography,
  Alert,
  Box,
  LinearProgress,
  Stack,
  Paper,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
} from '@mui/material';
import {
  TrendingUp,
  Warning,
  CheckCircle,
  Info,
  Star,
  ErrorOutline,
  Email,
  Phone,
  Business,
} from '@mui/icons-material';
import {
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  Tooltip as RechartsTooltip,
  Legend,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
} from 'recharts';

// Mock-Daten - später durch Backend ersetzen
const MOCK_LEAD_QUALITY_DATA = {
  totalLeads: 38,
  qualificationLevels: {
    complete: 18, // Vollständig qualifiziert
    partial: 12, // Teilweise qualifiziert
    minimal: 8, // Minimal-Daten
  },
  scoreDistribution: {
    top: 5, // 80-100
    good: 13, // 60-79
    medium: 12, // 40-59
    weak: 8, // 0-39
  },
  criticalGaps: {
    noContactPerson: 6,
    noEmail: 11,
    noPhone: 4,
    noBusinessType: 3,
    noBudgetInfo: 15,
  },
  averageScore: 62,
  qualityTrend: +5, // +5% improvement
};

// Farben für Lead-Score (FreshFoodz CI)
const SCORE_COLORS = {
  top: '#94C456', // FreshFoodz Grün
  good: '#4CAF50', // Grün
  medium: '#FF9800', // Orange
  weak: '#F44336', // Rot
};

const QUALIFICATION_COLORS = {
  complete: '#94C456',
  partial: '#FF9800',
  minimal: '#F44336',
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
  color = '#004F7B',
  icon,
}) => {
  const getTrendIcon = () => {
    if (!trend) return null;
    if (trend > 0) return <TrendingUp sx={{ color: 'success.main', fontSize: 20 }} />;
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
          <Box display="flex" flexDirection="column" alignItems="flex-end" gap={1}>
            {icon}
            {getTrendIcon()}
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
};

export const LeadQualityDashboard: React.FC = () => {
  const data = MOCK_LEAD_QUALITY_DATA;

  // Score-Verteilung für Pie Chart
  const scoreData = [
    { name: 'Top-Leads (80-100)', value: data.scoreDistribution.top, color: SCORE_COLORS.top },
    { name: 'Gute Leads (60-79)', value: data.scoreDistribution.good, color: SCORE_COLORS.good },
    {
      name: 'Mittlere Leads (40-59)',
      value: data.scoreDistribution.medium,
      color: SCORE_COLORS.medium,
    },
    { name: 'Schwache Leads (0-39)', value: data.scoreDistribution.weak, color: SCORE_COLORS.weak },
  ];

  // Qualifizierungs-Level für Bar Chart
  const qualificationData = [
    {
      name: 'Vollständig',
      count: data.qualificationLevels.complete,
      percentage: Math.round((data.qualificationLevels.complete / data.totalLeads) * 100),
    },
    {
      name: 'Teilweise',
      count: data.qualificationLevels.partial,
      percentage: Math.round((data.qualificationLevels.partial / data.totalLeads) * 100),
    },
    {
      name: 'Minimal',
      count: data.qualificationLevels.minimal,
      percentage: Math.round((data.qualificationLevels.minimal / data.totalLeads) * 100),
    },
  ];

  return (
    <Box sx={{ p: 3 }}>
      <Typography
        variant="h5"
        gutterBottom
        sx={{ mb: 3, color: 'secondary.main', fontWeight: 'bold' }}
      >
        Lead-Qualität Übersicht
      </Typography>

      {/* KPIs */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <MetricCard
            title="Durchschnittlicher Lead-Score"
            value={`${data.averageScore}/100`}
            subtitle="Basis: Alle Leads"
            trend={data.qualityTrend}
            color="#004F7B"
            icon={<Star sx={{ fontSize: 40, color: 'primary.main' }} />}
          />
        </Grid>

        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <MetricCard
            title="Top-Leads"
            value={data.scoreDistribution.top}
            subtitle="Score 80-100 Punkte"
            color="#94C456"
            icon={<CheckCircle sx={{ fontSize: 40, color: 'primary.main' }} />}
          />
        </Grid>

        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <MetricCard
            title="Vollständig qualifiziert"
            value={`${Math.round((data.qualificationLevels.complete / data.totalLeads) * 100)}%`}
            subtitle={`${data.qualificationLevels.complete}/${data.totalLeads} Leads`}
            color="#4CAF50"
            icon={<CheckCircle sx={{ fontSize: 40, color: 'success.main' }} />}
          />
        </Grid>

        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <MetricCard
            title="Nachqualifizierung nötig"
            value={data.qualificationLevels.minimal}
            subtitle="Kritische Datenlücken"
            color="#F44336"
            icon={<Warning sx={{ fontSize: 40, color: 'error.main' }} />}
          />
        </Grid>
      </Grid>

      {/* Charts */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        {/* Lead-Score-Verteilung */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 2, height: '100%' }}>
            <Typography variant="h6" gutterBottom sx={{ color: 'secondary.main' }}>
              Lead-Score-Verteilung
            </Typography>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={scoreData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, value }) => `${name}: ${value}`}
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {scoreData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <RechartsTooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* Qualifizierungs-Level */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 2, height: '100%' }}>
            <Typography variant="h6" gutterBottom sx={{ color: 'secondary.main' }}>
              Qualifizierungs-Level
            </Typography>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={qualificationData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <RechartsTooltip />
                <Legend />
                <Bar dataKey="count" fill="#004F7B" name="Anzahl Leads" />
                <Bar dataKey="percentage" fill="#94C456" name="Prozent %" />
              </BarChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>
      </Grid>

      {/* Kritische Datenlücken */}
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 6 }}>
          <Alert severity="warning" sx={{ mb: 2 }}>
            <Typography variant="h6" gutterBottom>
              ⚠️ Kritische Datenlücken
            </Typography>
            <List dense>
              {data.criticalGaps.noContactPerson > 0 && (
                <ListItem>
                  <ListItemIcon>
                    <Business color="warning" />
                  </ListItemIcon>
                  <ListItemText
                    primary={`${data.criticalGaps.noContactPerson} Leads ohne Ansprechpartner`}
                    secondary="Kontaktperson fehlt für Direktansprache"
                  />
                </ListItem>
              )}
              {data.criticalGaps.noEmail > 0 && (
                <ListItem>
                  <ListItemIcon>
                    <Email color="warning" />
                  </ListItemIcon>
                  <ListItemText
                    primary={`${data.criticalGaps.noEmail} Leads ohne E-Mail-Adresse`}
                    secondary="E-Mail-Marketing nicht möglich"
                  />
                </ListItem>
              )}
              {data.criticalGaps.noPhone > 0 && (
                <ListItem>
                  <ListItemIcon>
                    <Phone color="warning" />
                  </ListItemIcon>
                  <ListItemText
                    primary={`${data.criticalGaps.noPhone} Leads ohne Telefonnummer`}
                    secondary="Telefonische Kontaktaufnahme nicht möglich"
                  />
                </ListItem>
              )}
              {data.criticalGaps.noBudgetInfo > 0 && (
                <ListItem>
                  <ListItemIcon>
                    <ErrorOutline color="warning" />
                  </ListItemIcon>
                  <ListItemText
                    primary={`${data.criticalGaps.noBudgetInfo} Leads ohne Budget-Information`}
                    secondary="Umsatzpotenzial unklar"
                  />
                </ListItem>
              )}
            </List>
          </Alert>
        </Grid>

        {/* Empfehlungen */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Alert severity="info">
            <Typography variant="h6" gutterBottom>
              💡 Empfehlungen zur Qualitätsverbesserung
            </Typography>
            <List dense>
              <ListItem>
                <ListItemIcon>
                  <Info color="info" />
                </ListItemIcon>
                <ListItemText
                  primary="Nachqualifizierung starten"
                  secondary={`${data.qualificationLevels.minimal} Leads mit Minimal-Daten gezielt nachbearbeiten`}
                />
              </ListItem>
              <ListItem>
                <ListItemIcon>
                  <Star color="info" />
                </ListItemIcon>
                <ListItemText
                  primary="Top-Leads priorisieren"
                  secondary={`${data.scoreDistribution.top} Top-Leads (Score >80) sollten bevorzugt bearbeitet werden`}
                />
              </ListItem>
              <ListItem>
                <ListItemIcon>
                  <CheckCircle color="info" />
                </ListItemIcon>
                <ListItemText
                  primary="Datenvollständigkeit erhöhen"
                  secondary="Fehlende Pflichtfelder bei Erstkontakt erfassen (E-Mail, Telefon, Ansprechpartner)"
                />
              </ListItem>
            </List>
          </Alert>
        </Grid>
      </Grid>

      {/* Qualifizierungs-Fortschritt */}
      <Box sx={{ mt: 3 }}>
        <Paper sx={{ p: 2 }}>
          <Typography variant="h6" gutterBottom sx={{ color: 'secondary.main' }}>
            Qualifizierungs-Fortschritt
          </Typography>
          <Stack spacing={2}>
            <Box>
              <Box display="flex" justifyContent="space-between" mb={1}>
                <Typography variant="body2">Vollständig qualifiziert</Typography>
                <Typography variant="body2" fontWeight="bold">
                  {Math.round((data.qualificationLevels.complete / data.totalLeads) * 100)}%
                </Typography>
              </Box>
              <LinearProgress
                variant="determinate"
                value={(data.qualificationLevels.complete / data.totalLeads) * 100}
                sx={{
                  height: 10,
                  borderRadius: 5,
                  backgroundColor: '#E0E0E0',
                  '& .MuiLinearProgress-bar': { backgroundColor: QUALIFICATION_COLORS.complete },
                }}
              />
            </Box>

            <Box>
              <Box display="flex" justifyContent="space-between" mb={1}>
                <Typography variant="body2">Teilweise qualifiziert</Typography>
                <Typography variant="body2" fontWeight="bold">
                  {Math.round((data.qualificationLevels.partial / data.totalLeads) * 100)}%
                </Typography>
              </Box>
              <LinearProgress
                variant="determinate"
                value={(data.qualificationLevels.partial / data.totalLeads) * 100}
                sx={{
                  height: 10,
                  borderRadius: 5,
                  backgroundColor: '#E0E0E0',
                  '& .MuiLinearProgress-bar': { backgroundColor: QUALIFICATION_COLORS.partial },
                }}
              />
            </Box>

            <Box>
              <Box display="flex" justifyContent="space-between" mb={1}>
                <Typography variant="body2">Minimal-Daten</Typography>
                <Typography variant="body2" fontWeight="bold">
                  {Math.round((data.qualificationLevels.minimal / data.totalLeads) * 100)}%
                </Typography>
              </Box>
              <LinearProgress
                variant="determinate"
                value={(data.qualificationLevels.minimal / data.totalLeads) * 100}
                sx={{
                  height: 10,
                  borderRadius: 5,
                  backgroundColor: '#E0E0E0',
                  '& .MuiLinearProgress-bar': { backgroundColor: QUALIFICATION_COLORS.minimal },
                }}
              />
            </Box>
          </Stack>
        </Paper>
      </Box>
    </Box>
  );
};
