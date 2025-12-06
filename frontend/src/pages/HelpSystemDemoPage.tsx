import React from 'react';
import {
  Container,
  Typography,
  Box,
  Paper,
  Grid,
  Card,
  CardContent,
  Button,
  Alert,
  Stack,
} from '@mui/material';
import { HelpTooltip, useHelp, useHelpStore } from '../features/help';
import {
  PlayCircleOutline as PlayCircleIcon,
  BugReport as BugReportIcon,
  Analytics as AnalyticsIcon,
} from '@mui/icons-material';

export const HelpSystemDemoPage: React.FC = () => {
  // Help hooks are loaded but not used in demo
  useHelp({
    feature: 'help-demo',
    autoLoad: true,
  });

  const { startTour, detectStruggle, analytics, loadAnalytics } = useHelpStore();

  // Simulate user struggle
  const simulateStruggle = () => {
    // Simulate repeated failed attempts
    const actions = [
      { type: 'form-submit', timestamp: Date.now() - 3000, success: false },
      { type: 'form-submit', timestamp: Date.now() - 2000, success: false },
      { type: 'form-submit', timestamp: Date.now() - 1000, success: false },
    ];

    detectStruggle({
      feature: 'help-demo',
      actions,
    });
  };

  React.useEffect(() => {
    loadAnalytics();
  }, [loadAnalytics]);

  return (
    <Box component="main">
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Typography variant="h3" component="h1" gutterBottom>
          In-App Help System Demo
          <HelpTooltip feature="help-demo" />
        </Typography>

        <Alert severity="info" sx={{ mb: 4 }}>
          Diese Seite demonstriert alle Features des neuen In-App Help Systems!
        </Alert>

        <Grid container spacing={3}>
          {/* Help Tooltip Demo */}
          <Grid size={{ xs: 12, md: 6 }}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center" mb={2}>
                  <Typography variant="h5">1. Help Tooltips</Typography>
                  <HelpTooltip feature="cost-management" />
                </Box>
                <Typography variant="body2" color="text.secondary" paragraph>
                  Klicken Sie auf das Help-Icon neben der Überschrift!
                </Typography>

                <Box sx={{ mt: 3 }}>
                  <Typography variant="subtitle1" gutterBottom>
                    Budget-Limit
                    <HelpTooltip feature="cost-management-budget" />
                  </Typography>
                  <Typography variant="body2">Monatliches Limit: €500</Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>

          {/* Feature Tour Demo */}
          <Grid size={{ xs: 12, md: 6 }}>
            <Card>
              <CardContent>
                <Typography variant="h5" gutterBottom>
                  2. Feature Tours
                </Typography>
                <Typography variant="body2" color="text.secondary" paragraph>
                  Interaktive Schritt-für-Schritt Anleitungen
                </Typography>
                <Button
                  variant="contained"
                  startIcon={<PlayCircleIcon />}
                  onClick={() => startTour('cost-management')}
                  fullWidth
                >
                  Tour starten
                </Button>
              </CardContent>
            </Card>
          </Grid>

          {/* Proactive Help Demo */}
          <Grid size={{ xs: 12, md: 6 }}>
            <Card>
              <CardContent>
                <Typography variant="h5" gutterBottom>
                  3. Proaktive Hilfe
                </Typography>
                <Typography variant="body2" color="text.secondary" paragraph>
                  Das System erkennt wenn Nutzer Probleme haben
                </Typography>
                <Button
                  variant="outlined"
                  startIcon={<BugReportIcon />}
                  onClick={simulateStruggle}
                  fullWidth
                >
                  User-Struggle simulieren
                </Button>
                <Typography variant="caption" display="block" sx={{ mt: 1 }}>
                  Klicken Sie hier um zu sehen, wie das System bei Problemen hilft
                </Typography>
              </CardContent>
            </Card>
          </Grid>

          {/* Analytics Demo */}
          <Grid size={{ xs: 12, md: 6 }}>
            <Card>
              <CardContent>
                <Typography variant="h5" gutterBottom>
                  4. Help Analytics
                </Typography>
                <Typography variant="body2" color="text.secondary" paragraph>
                  Tracking und Analyse der Hilfe-Nutzung
                </Typography>
                {analytics && (
                  <Stack spacing={1}>
                    <Typography variant="body2">Total Views: {analytics.totalViews}</Typography>
                    <Typography variant="body2">
                      Helpfulness Rate: {analytics.overallHelpfulnessRate.toFixed(1)}%
                    </Typography>
                    <Typography variant="body2">
                      Feature Coverage: {analytics.featureCoverage} Features
                    </Typography>
                  </Stack>
                )}
                <Button
                  variant="outlined"
                  startIcon={<AnalyticsIcon />}
                  onClick={() => loadAnalytics()}
                  fullWidth
                  sx={{ mt: 2 }}
                >
                  Analytics aktualisieren
                </Button>
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        {/* Test Areas for Tours */}
        <Paper sx={{ p: 3, mt: 4 }}>
          <Typography variant="h5" gutterBottom>
            Tour Test Areas
          </Typography>

          <Grid container spacing={2} sx={{ mt: 2 }}>
            <Grid size={{ xs: 4 }}>
              <Box
                className="warmth-indicator"
                sx={{
                  p: 2,
                  bgcolor: 'success.light',
                  borderRadius: 1,
                  textAlign: 'center',
                }}
              >
                <Typography>Warmth Score: 73</Typography>
              </Box>
            </Grid>

            <Grid size={{ xs: 4 }}>
              <Box
                className="smart-suggestions"
                sx={{
                  p: 2,
                  bgcolor: 'info.light',
                  borderRadius: 1,
                  textAlign: 'center',
                }}
              >
                <Typography>Smart Suggestions</Typography>
              </Box>
            </Grid>

            <Grid size={{ xs: 4 }}>
              <Box
                className="contact-timeline"
                sx={{
                  p: 2,
                  bgcolor: 'warning.light',
                  borderRadius: 1,
                  textAlign: 'center',
                }}
              >
                <Typography>Contact Timeline</Typography>
              </Box>
            </Grid>
          </Grid>
        </Paper>

        {/* Instructions */}
        <Paper sx={{ p: 3, mt: 4, bgcolor: 'grey.100' }}>
          <Typography variant="h6" gutterBottom>
            So funktioniert das Help System:
          </Typography>
          <ol>
            <li>
              <strong>Help Icons:</strong> Klicken Sie auf die ? Icons für kontextuelle Hilfe
            </li>
            <li>
              <strong>Feature Tours:</strong> Starten Sie interaktive Anleitungen
            </li>
            <li>
              <strong>Proaktive Hilfe:</strong> Das System erkennt Probleme und bietet Hilfe an
            </li>
            <li>
              <strong>Feedback:</strong> Bewerten Sie die Hilfe als hilfreich oder nicht
            </li>
            <li>
              <strong>Analytics:</strong> Alle Interaktionen werden getrackt und analysiert
            </li>
          </ol>
        </Paper>
      </Container>
    </Box>
  );
};
