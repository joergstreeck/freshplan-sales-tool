/**
 * Sales Cockpit V2 - Optimized for MainLayoutV2
 * Angepasst für das neue Standard-Layout ohne feste Höhenberechnungen
 */

import React from 'react';
import { 
  Box, 
  Paper, 
  Typography, 
  Card, 
  CardContent, 
  Chip, 
  Button
} from '@mui/material';
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
  transition: 'all 0.3s ease',
  '&:hover': {
    transform: 'translateY(-2px)',
    boxShadow: theme.shadows[4],
  }
}));

const ColumnPaper = styled(Paper)(({ theme }) => ({
  padding: theme.spacing(2),
  height: '100%',
  display: 'flex',
  flexDirection: 'column',
  backgroundColor: theme.palette.background.paper,
}));

const ScrollableContent = styled(Box)(({ theme }) => ({
  flexGrow: 1,
  overflowY: 'auto',
  overflowX: 'hidden',
  '&::-webkit-scrollbar': {
    width: '8px',
  },
  '&::-webkit-scrollbar-track': {
    backgroundColor: theme.palette.grey[100],
    borderRadius: '4px',
  },
  '&::-webkit-scrollbar-thumb': {
    backgroundColor: theme.palette.grey[400],
    borderRadius: '4px',
    '&:hover': {
      backgroundColor: theme.palette.grey[500],
    },
  },
}));

export function SalesCockpitV2() {
  return (
    <Box sx={{ 
      height: '100%', 
      display: 'flex', 
      flexDirection: 'column',
      overflow: 'hidden' 
    }}>
      {/* Dashboard Header */}
      <Box sx={{ mb: 3 }}>
        <Typography 
          variant="h4" 
          component="h1" 
          sx={{ 
            color: 'primary.main', 
            fontWeight: 'bold',
            fontFamily: 'Antonio Bold, sans-serif' 
          }}
        >
          FreshPlan Sales Command Center
        </Typography>
      </Box>
      
      {/* Dashboard Stats */}
      <Box sx={{ mb: 3 }}>
        <Box sx={{ 
          display: 'grid', 
          gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(4, 1fr)' },
          gap: 2 
        }}>
          <StatsCard>
              <GroupIcon sx={{ fontSize: 40, color: 'primary.main' }} />
              <Typography variant="h4">156</Typography>
              <Typography variant="body2" color="text.secondary">Kunden gesamt</Typography>
          </StatsCard>
          <StatsCard>
              <TrendingUpIcon sx={{ fontSize: 40, color: 'success.main' }} />
              <Typography variant="h4">142</Typography>
              <Typography variant="body2" color="text.secondary">Aktive Kunden</Typography>
          </StatsCard>
          <StatsCard>
              <TaskIcon sx={{ fontSize: 40, color: 'warning.main' }} />
              <Typography variant="h4">8</Typography>
              <Typography variant="body2" color="text.secondary">Risiko-Kunden</Typography>
          </StatsCard>
          <StatsCard>
              <ErrorIcon sx={{ fontSize: 40, color: 'error.main' }} />
              <Typography variant="h4">3</Typography>
              <Typography variant="body2" color="text.secondary">Überfällig</Typography>
          </StatsCard>
        </Box>
      </Box>

      {/* 3-Column Layout - Flex-grow to fill remaining space */}
      <Box sx={{ flexGrow: 1, overflow: 'hidden' }}>
        <Box sx={{ 
          display: 'grid', 
          gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' },
          gap: 2,
          height: '100%' 
        }}>
          {/* Column 1: Mein Tag */}
          <Box sx={{ display: 'flex' }}>
            <ColumnPaper elevation={1} sx={{ width: '100%' }}>
              <Typography variant="h6" gutterBottom sx={{ color: 'secondary.main' }}>
                Mein Tag
              </Typography>
              
              <ScrollableContent>
                <Box sx={{ mb: 2 }}>
                  <Typography variant="subtitle2" gutterBottom>Aktuelle Benachrichtigungen</Typography>
                  <Card sx={{ mb: 1, bgcolor: '#e8f5e9' }}>
                    <CardContent sx={{ py: 1 }}>
                      <Typography variant="body2">
                        🔔 Umsatzchance bei Premium Partner GmbH
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        Kunde hatte lange keinen Kontakt - Idealer Zeitpunkt für Cross-Selling
                      </Typography>
                    </CardContent>
                  </Card>
                </Box>

                <Box sx={{ mb: 2 }}>
                  <Typography variant="subtitle2" gutterBottom>Prioritäts-Aufgaben (3)</Typography>
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
                  {/* Weitere Aufgaben für Scroll-Test */}
                  {[2, 3, 4, 5].map((i) => (
                    <Card key={i} sx={{ mb: 1 }}>
                      <CardContent sx={{ py: 1 }}>
                        <Typography variant="body2">
                          Aufgabe {i}: Beispiel-Aufgabe
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          Geplant für heute
                        </Typography>
                      </CardContent>
                    </Card>
                  ))}
                </Box>
              </ScrollableContent>
            </ColumnPaper>
          </Box>

          {/* Column 2: Fokus-Liste */}
          <Box sx={{ display: 'flex' }}>
            <ColumnPaper elevation={1} sx={{ width: '100%' }}>
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

              <ScrollableContent>
                <Card sx={{ mb: 1 }}>
                  <CardContent>
                    <Typography variant="body1">Premium Partner GmbH</Typography>
                    <Typography variant="caption" color="text.secondary">
                      Letzte Aktivität vor 45 Tagen
                    </Typography>
                  </CardContent>
                </Card>
                {/* Weitere Kunden für Scroll-Test */}
                {['Mustermann GmbH', 'Test AG', 'Sample Company', 'Demo Ltd.', 'Example Corp'].map((name, i) => (
                  <Card key={i} sx={{ mb: 1 }}>
                    <CardContent>
                      <Typography variant="body1">{name}</Typography>
                      <Typography variant="caption" color="text.secondary">
                        Letzte Aktivität vor {i + 1} Tagen
                      </Typography>
                    </CardContent>
                  </Card>
                ))}
              </ScrollableContent>
            </ColumnPaper>
          </Box>

          {/* Column 3: Aktions-Center */}
          <Box sx={{ display: 'flex' }}>
            <ColumnPaper elevation={1} sx={{ width: '100%' }}>
              <Typography variant="h6" gutterBottom sx={{ color: 'secondary.main' }}>
                Aktions-Center
              </Typography>
              
              <ScrollableContent>
                <Box sx={{ textAlign: 'center', py: 4 }}>
                  <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
                    Wählen Sie einen Kunden aus der Fokus-Liste aus, um mit der Bearbeitung zu beginnen.
                  </Typography>
                  <Button variant="contained" color="primary">
                    Kunde auswählen
                  </Button>
                </Box>
              </ScrollableContent>
            </ColumnPaper>
          </Box>
        </Box>
      </Box>
    </Box>
  );
}