import React, { useState } from 'react';
import {
  Typography,
  Box,
  Paper,
  Grid,
  Card,
  CardContent,
  Button,
  Alert,
  Chip,
  Switch,
  FormControlLabel,
  LinearProgress,
  Tooltip,
  useTheme,
  alpha,
} from '@mui/material';
import PlayCircleIcon from '@mui/icons-material/PlayCircle';
import BugReportIcon from '@mui/icons-material/BugReport';
import AnalyticsIcon from '@mui/icons-material/Analytics';
import TipsAndUpdatesIcon from '@mui/icons-material/TipsAndUpdates';
import SchoolIcon from '@mui/icons-material/School';
import TouchAppIcon from '@mui/icons-material/TouchApp';
import AutoFixHighIcon from '@mui/icons-material/AutoFixHigh';
import InfoIcon from '@mui/icons-material/Info';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import { HelpTooltip, useHelp, useHelpStore } from '../features/help';

export const HelpSystemDemoPageV2: React.FC = () => {
  const theme = useTheme();
  const [tooltipsEnabled, setTooltipsEnabled] = useState(true);
  const [proactiveHelpEnabled, setProactiveHelpEnabled] = useState(true);
  const [tourRunning, setTourRunning] = useState(false);
  const [strugglesDetected, setStrugglesDetected] = useState(0);

  // Help hooks
  useHelp({
    feature: 'help-demo',
    autoLoad: true,
  });

  const { startTour, detectStruggle, analytics, loadAnalytics } = useHelpStore();

  // Simulate starting a tour
  const handleStartTour = () => {
    setTourRunning(true);
    startTour('help-demo');
    setTimeout(() => {
      setTourRunning(false);
    }, 5000);
  };

  // Simulate user struggle
  const simulateStruggle = () => {
    const actions = [
      { type: 'form-submit', timestamp: Date.now() - 3000, success: false },
      { type: 'form-submit', timestamp: Date.now() - 2000, success: false },
      { type: 'form-submit', timestamp: Date.now() - 1000, success: false },
    ];

    detectStruggle({
      feature: 'help-demo',
      actions,
    });

    setStrugglesDetected(prev => prev + 1);
  };

  React.useEffect(() => {
    loadAnalytics();
  }, [loadAnalytics]);

  const features = [
    {
      title: 'Kontextuelle Tooltips',
      description: 'Hilfreiche Hinweise genau dort, wo sie gebraucht werden',
      icon: <TipsAndUpdatesIcon sx={{ fontSize: 40, color: theme.palette.primary.main }} />,
      enabled: tooltipsEnabled,
    },
    {
      title: 'Geführte Touren',
      description: 'Schritt-für-Schritt Anleitungen für komplexe Prozesse',
      icon: <SchoolIcon sx={{ fontSize: 40, color: theme.palette.secondary.main }} />,
      enabled: true,
    },
    {
      title: 'Proaktive Hilfe',
      description: 'Automatische Hilfe bei erkannten Schwierigkeiten',
      icon: <AutoFixHighIcon sx={{ fontSize: 40, color: theme.palette.primary.main }} />,
      enabled: proactiveHelpEnabled,
    },
    {
      title: 'Interaktive Demos',
      description: 'Lerne durch Ausprobieren in sicherer Umgebung',
      icon: <TouchAppIcon sx={{ fontSize: 40, color: theme.palette.secondary.main }} />,
      enabled: true,
    },
  ];

  const analyticsData = [
    { metric: 'Tooltips angezeigt', value: analytics?.tooltipsShown || 0 },
    { metric: 'Touren gestartet', value: analytics?.toursStarted || 0 },
    { metric: 'Struggles erkannt', value: strugglesDetected },
    { metric: 'Hilfe-Anfragen', value: analytics?.helpRequests || 0 },
  ];

  return (
    <MainLayoutV2>
      <Box sx={{ py: 4 }}>
        {/* Header */}
        <Box sx={{ mb: 4 }}>
          <Typography
            variant="h3"
            sx={{ mb: 1, color: theme.palette.secondary.main }}
          >
            Hilfe-System Konfiguration
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Verwalten und testen Sie das intelligente Hilfe-System
          </Typography>
        </Box>

        {/* Configuration Panel */}
        <Paper sx={{ p: 3, mb: 4 }}>
          <Typography variant="h5" sx={{ mb: 3 }}>
            System-Einstellungen
          </Typography>
          <Grid container spacing={3}>
            <Grid size={{ xs: 12, md: 4 }}>
              <FormControlLabel
                control={
                  <Switch
                    checked={tooltipsEnabled}
                    onChange={e => setTooltipsEnabled(e.target.checked)}
                    sx={{
                      '& .MuiSwitch-switchBase.Mui-checked': {
                        color: theme.palette.primary.main,
                      },
                      '& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track': {
                        backgroundColor: theme.palette.primary.main,
                      },
                    }}
                  />
                }
                label="Tooltips aktiviert"
              />
            </Grid>
            <Grid size={{ xs: 12, md: 4 }}>
              <FormControlLabel
                control={
                  <Switch
                    checked={proactiveHelpEnabled}
                    onChange={e => setProactiveHelpEnabled(e.target.checked)}
                    sx={{
                      '& .MuiSwitch-switchBase.Mui-checked': {
                        color: theme.palette.primary.main,
                      },
                      '& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track': {
                        backgroundColor: theme.palette.primary.main,
                      },
                    }}
                  />
                }
                label="Proaktive Hilfe"
              />
            </Grid>
            <Grid size={{ xs: 12, md: 4 }}>
              <Chip label="Analytics Aktiv" color="success" icon={<CheckCircleIcon />} />
            </Grid>
          </Grid>
        </Paper>

        {/* Features Grid */}
        <Typography variant="h5" sx={{ mb: 3 }}>
          Hilfe-System Features
        </Typography>
        <Grid container spacing={3} sx={{ mb: 4 }}>
          {features.map((feature, index) => (
            <Grid size={{ xs: 12, md: 6 }} key={index}>
              <Card sx={{ height: '100%' }}>
                <CardContent>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                    {feature.icon}
                    <Chip
                      label={feature.enabled ? 'Aktiv' : 'Inaktiv'}
                      size="small"
                      color={feature.enabled ? 'success' : 'default'}
                    />
                  </Box>
                  <Typography variant="h6" sx={{ mb: 1 }}>
                    {feature.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {feature.description}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>

        {/* Test Actions */}
        <Paper sx={{ p: 3, mb: 4 }}>
          <Typography variant="h5" sx={{ mb: 3 }}>
            Test-Aktionen
          </Typography>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: 4 }}>
              <Button
                fullWidth
                variant="contained"
                startIcon={<PlayCircleIcon />}
                onClick={handleStartTour}
                disabled={tourRunning}
                sx={{
                  backgroundColor: theme.palette.primary.main,
                  '&:hover': { backgroundColor: theme.palette.primary.dark },
                  '&:disabled': { backgroundColor: theme.palette.grey[300] },
                }}
              >
                {tourRunning ? 'Tour läuft...' : 'Tour starten'}
              </Button>
              {tourRunning && <LinearProgress sx={{ mt: 1 }} />}
            </Grid>
            <Grid size={{ xs: 12, md: 4 }}>
              <Button
                fullWidth
                variant="outlined"
                startIcon={<BugReportIcon />}
                onClick={simulateStruggle}
                sx={{
                  borderColor: theme.palette.secondary.main,
                  color: theme.palette.secondary.main,
                  '&:hover': {
                    borderColor: theme.palette.secondary.dark,
                    backgroundColor: alpha(theme.palette.secondary.main, 0.04),
                  },
                }}
              >
                Struggle simulieren
              </Button>
            </Grid>
            <Grid size={{ xs: 12, md: 4 }}>
              <Button
                fullWidth
                variant="outlined"
                startIcon={<AnalyticsIcon />}
                onClick={() => loadAnalytics()}
                sx={{
                  borderColor: theme.palette.secondary.main,
                  color: theme.palette.secondary.main,
                  '&:hover': {
                    borderColor: theme.palette.secondary.dark,
                    backgroundColor: alpha(theme.palette.secondary.main, 0.04),
                  },
                }}
              >
                Analytics laden
              </Button>
            </Grid>
          </Grid>
        </Paper>

        {/* Analytics Dashboard */}
        <Paper sx={{ p: 3, mb: 4 }}>
          <Typography variant="h5" sx={{ mb: 3 }}>
            Analytics Dashboard
          </Typography>
          <Grid container spacing={3}>
            {analyticsData.map((item, index) => (
              <Grid size={{ xs: 6, md: 3 }} key={index}>
                <Card sx={{ textAlign: 'center' }}>
                  <CardContent>
                    <Typography
                      variant="h3"
                      sx={{ color: theme.palette.primary.main }}
                    >
                      {item.value}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {item.metric}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Paper>

        {/* Demo Component with Tooltip */}
        <Paper sx={{ p: 3 }}>
          <Typography variant="h5" sx={{ mb: 3 }}>
            Interaktive Demo
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
            <HelpTooltip
              title="Dies ist ein Beispiel-Tooltip"
              content="Tooltips können zusätzliche Informationen und Kontext zu UI-Elementen bereitstellen."
            >
              <Button
                variant="contained"
                sx={{ backgroundColor: theme.palette.primary.main, '&:hover': { backgroundColor: theme.palette.primary.dark } }}
              >
                Hover für Tooltip
              </Button>
            </HelpTooltip>

            <Tooltip title="Standard Material-UI Tooltip">
              <Button variant="outlined">Standard Tooltip</Button>
            </Tooltip>

            <Button
              variant="outlined"
              startIcon={<InfoIcon />}
              sx={{
                borderColor: theme.palette.secondary.main,
                color: theme.palette.secondary.main,
              }}
            >
              Mehr Infos
            </Button>
          </Box>

          <Alert severity="info" sx={{ mt: 3 }}>
            Das Hilfe-System passt sich automatisch an das Nutzerverhalten an und bietet
            kontextuelle Unterstützung genau dann, wenn sie benötigt wird.
          </Alert>
        </Paper>
      </Box>
    </MainLayoutV2>
  );
};
