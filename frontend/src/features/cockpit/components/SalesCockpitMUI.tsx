/**
 * Sales Cockpit - MUI Version
 * Saubere Implementierung mit Material-UI Components
 */

import React from 'react';
import { Box, Paper, Typography, Grid, Card, CardContent, Chip, Button, useTheme, alpha } from '@mui/material';
import { styled } from '@mui/material/styles';
import GroupIcon from '@mui/icons-material/Group';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TaskIcon from '@mui/icons-material/Task';
import ErrorIcon from '@mui/icons-material/Error';

const StatsCard = styled(Card)(({ theme }) => ({
  textAlign: 'center',
  padding: theme.spacing(2),
  height: '100%',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
}));

const ColumnPaper = styled(Paper)(({ theme }) => ({
  padding: theme.spacing(2),
  height: 'calc(100vh - 200px)',
  overflowY: 'auto',
  backgroundColor: theme.palette.background.paper,
}));

export function SalesCockpitMUI() {
  const theme = useTheme();

  return (
    <Box sx={{ p: 3, height: '100%', bgcolor: 'background.default' }}>
      {/* Dashboard Header */}
      <Typography
        variant="h4"
        component="h1"
        gutterBottom
        sx={{ color: 'primary.main', fontWeight: 'bold' }}
      >
        FreshPlan Vertriebsübersicht
      </Typography>

      {/* Dashboard Stats */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatsCard>
            <GroupIcon sx={{ fontSize: 40, color: 'primary.main' }} />
            <Typography variant="h4">156</Typography>
            <Typography variant="body2" color="text.secondary">
              Kunden gesamt
            </Typography>
          </StatsCard>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatsCard>
            <TrendingUpIcon sx={{ fontSize: 40, color: 'success.main' }} />
            <Typography variant="h4">142</Typography>
            <Typography variant="body2" color="text.secondary">
              Aktive Kunden
            </Typography>
          </StatsCard>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatsCard>
            <TaskIcon sx={{ fontSize: 40, color: 'warning.main' }} />
            <Typography variant="h4">8</Typography>
            <Typography variant="body2" color="text.secondary">
              Risiko-Kunden
            </Typography>
          </StatsCard>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatsCard>
            <ErrorIcon sx={{ fontSize: 40, color: 'error.main' }} />
            <Typography variant="h4">3</Typography>
            <Typography variant="body2" color="text.secondary">
              Überfällig
            </Typography>
          </StatsCard>
        </Grid>
      </Grid>

      {/* 3-Column Layout */}
      <Grid container spacing={2}>
        {/* Column 1: Mein Tag */}
        <Grid size={{ xs: 12, md: 4 }}>
          <ColumnPaper elevation={1}>
            <Typography variant="h6" gutterBottom sx={{ color: 'secondary.main' }}>
              Mein Tag
            </Typography>

            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle2" gutterBottom>
                Aktuelle Benachrichtigungen
              </Typography>
              <Card sx={{ mb: 1, bgcolor: alpha(theme.palette.success.light, 0.25) }}>
                <CardContent sx={{ py: 1 }}>
                  <Typography variant="body2">🔔 Umsatzchance bei Premium Partner GmbH</Typography>
                  <Typography variant="caption" color="text.secondary">
                    Kunde hatte lange keinen Kontakt - Idealer Zeitpunkt für Cross-Selling
                  </Typography>
                </CardContent>
              </Card>
            </Box>

            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle2" gutterBottom>
                Prioritäts-Aufgaben (3)
              </Typography>
              <Card sx={{ mb: 1 }}>
                <CardContent sx={{ py: 1 }}>
                  <Typography variant="body2">
                    📅 ÜBERFÄLLIG: Follow-up mit Mustermann GmbH
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    08:09
                  </Typography>
                </CardContent>
              </Card>
            </Box>
          </ColumnPaper>
        </Grid>

        {/* Column 2: Fokus-Liste */}
        <Grid size={{ xs: 12, md: 4 }}>
          <ColumnPaper elevation={1}>
            <Typography variant="h6" gutterBottom sx={{ color: 'secondary.main' }}>
              Fokus-Liste
            </Typography>

            <Box sx={{ mb: 2 }}>
              <Chip label="Aktive" color="primary" size="small" sx={{ mr: 1 }} />
              <Chip label="Risiko" variant="outlined" size="small" sx={{ mr: 1 }} />
              <Chip label="Neue" variant="outlined" size="small" />
            </Box>

            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Zeige 10 von 156 Kunden
            </Typography>

            {/* Customer Cards would go here */}
            <Card sx={{ mb: 1 }}>
              <CardContent>
                <Typography variant="body1">Premium Partner GmbH</Typography>
                <Typography variant="caption" color="text.secondary">
                  Letzte Aktivität vor 45 Tagen
                </Typography>
              </CardContent>
            </Card>
          </ColumnPaper>
        </Grid>

        {/* Column 3: Aktions-Center */}
        <Grid size={{ xs: 12, md: 4 }}>
          <ColumnPaper elevation={1}>
            <Typography variant="h6" gutterBottom sx={{ color: 'secondary.main' }}>
              Aktions-Center
            </Typography>

            <Box sx={{ textAlign: 'center', py: 4 }}>
              <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
                Wählen Sie einen Kunden aus der Fokus-Liste aus, um mit der Bearbeitung zu beginnen.
              </Typography>
              <Button variant="contained" color="primary">
                Kunde auswählen
              </Button>
            </Box>
          </ColumnPaper>
        </Grid>
      </Grid>
    </Box>
  );
}
