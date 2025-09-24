import React from 'react';
import {
  Box,
  Container,
  Typography,
  Card,
  CardContent,
  CardActions,
  Button,
  Paper,
  Chip,
  Avatar,
  IconButton,
  List,
  ListItem,
  ListItemText,
  Divider,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import { useNavigate } from 'react-router-dom';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';

// Icons
import PeopleIcon from '@mui/icons-material/People';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import AssignmentIcon from '@mui/icons-material/Assignment';
import StarIcon from '@mui/icons-material/Star';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';

interface CustomerToolCard {
  title: string;
  description: string;
  icon: React.ReactNode;
  path?: string;
  action?: string;
  stats: string;
  badge?: string;
  color: string;
}

export function KundenmanagementDashboard() {
  const navigate = useNavigate();

  const customerTools: CustomerToolCard[] = [
    {
      title: 'Alle Kunden',
      description: 'Verwalten Sie Ihre gesamte Kundenbasis',
      icon: <PeopleIcon sx={{ fontSize: 48, color: '#004F7B' }} />,
      path: '/customers',
      stats: '1.247 aktiv',
      color: '#004F7B',
    },
    {
      title: 'Verkaufschancen',
      description: 'Verfolgen Sie aktive Opportunities',
      icon: <TrendingUpIcon sx={{ fontSize: 48, color: '#FFA726' }} />,
      path: '/kundenmanagement/opportunities',
      stats: '€2.3M Pipeline',
      color: '#FFA726',
    },
    {
      title: 'Aktivitäten',
      description: 'Anstehende Aufgaben und Termine',
      icon: <AssignmentIcon sx={{ fontSize: 48, color: '#004F7B' }} />,
      path: '/kundenmanagement/aktivitaeten',
      stats: '34 heute',
      color: '#004F7B',
    },
  ];

  const topCustomers = [
    { name: 'Hotel Adler GmbH', revenue: '€124.500', trend: 'up', growth: '+12%' },
    { name: 'Gasthaus zur Post', revenue: '€89.200', trend: 'up', growth: '+8%' },
    { name: 'Restaurant Marina', revenue: '€67.800', trend: 'down', growth: '-3%' },
    { name: 'Café Central', revenue: '€45.600', trend: 'up', growth: '+15%' },
    { name: 'Berghotel Panorama', revenue: '€38.900', trend: 'neutral', growth: '0%' },
  ];

  const recentActivities = [
    { type: 'call', customer: 'Hotel Adler', time: 'vor 10 Min', user: 'MS' },
    { type: 'meeting', customer: 'Gasthaus Rose', time: 'vor 1 Std', user: 'JD' },
    { type: 'email', customer: 'Restaurant Sonne', time: 'vor 2 Std', user: 'AK' },
    { type: 'deal', customer: 'Café Mozart', time: 'vor 3 Std', user: 'MS' },
  ];

  const getActivityIcon = (type: string) => {
    switch (type) {
      case 'call':
        return '📞';
      case 'meeting':
        return '🤝';
      case 'email':
        return '✉️';
      case 'deal':
        return '💰';
      default:
        return '📅';
    }
  };

  return (
    <MainLayoutV2>
      <Container maxWidth="xl" sx={{ py: 4 }}>
        {/* Header */}
        <Box
          sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}
        >
          <Box>
            <Typography
              variant="h3"
              sx={{
                mb: 1,
                fontFamily: 'Antonio, sans-serif',
                fontWeight: 'bold',
                color: '#004F7B',
              }}
            >
              Kundenmanagement
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Zentrale Verwaltung Ihrer Kunden, Opportunities und Aktivitäten
            </Typography>
          </Box>
          <Button
            variant="contained"
            startIcon={<PersonAddIcon />}
            sx={{
              backgroundColor: '#94C456',
              '&:hover': { backgroundColor: '#7BA347' },
              fontWeight: 'bold',
            }}
            onClick={() => navigate('/customers/new')}
          >
            Neuer Kunde
          </Button>
        </Box>

        {/* KPI Cards */}
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Paper sx={{ p: 3, background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }}>
              <Typography variant="h4" sx={{ color: 'white', fontWeight: 'bold' }}>
                1.247
              </Typography>
              <Typography variant="body1" sx={{ color: 'rgba(255,255,255,0.9)' }}>
                Aktive Kunden
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
                <ArrowUpwardIcon sx={{ fontSize: 16, color: '#94C456' }} />
                <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.8)' }}>
                  +67 diesen Monat
                </Typography>
              </Box>
            </Paper>
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Paper sx={{ p: 3, background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' }}>
              <Typography variant="h4" sx={{ color: 'white', fontWeight: 'bold' }}>
                €2.3M
              </Typography>
              <Typography variant="body1" sx={{ color: 'rgba(255,255,255,0.9)' }}>
                Pipeline Wert
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
                <ArrowUpwardIcon sx={{ fontSize: 16, color: '#94C456' }} />
                <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.8)' }}>
                  42 Opportunities
                </Typography>
              </Box>
            </Paper>
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Paper sx={{ p: 3, background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' }}>
              <Typography variant="h4" sx={{ color: 'white', fontWeight: 'bold' }}>
                89%
              </Typography>
              <Typography variant="body1" sx={{ color: 'rgba(255,255,255,0.9)' }}>
                Kundenzufriedenheit
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
                <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.8)' }}>
                  NPS Score: 72
                </Typography>
              </Box>
            </Paper>
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Paper sx={{ p: 3, background: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' }}>
              <Typography variant="h4" sx={{ color: 'white', fontWeight: 'bold' }}>
                €145k
              </Typography>
              <Typography variant="body1" sx={{ color: 'rgba(255,255,255,0.9)' }}>
                MRR
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
                <ArrowUpwardIcon sx={{ fontSize: 16, color: '#2e7d32' }} />
                <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.8)' }}>
                  +12% zum Vormonat
                </Typography>
              </Box>
            </Paper>
          </Grid>
        </Grid>

        {/* Main Content */}
        <Grid container spacing={3}>
          {/* Tool Cards */}
          <Grid size={{ xs: 12, lg: 8 }}>
            <Grid container spacing={3}>
              {customerTools.map(tool => (
                <Grid key={tool.title} size={{ xs: 12, sm: 6 }}>
                  <Card
                    sx={{
                      height: '100%',
                      transition: 'all 0.2s',
                      cursor: 'pointer',
                      '&:hover': {
                        boxShadow: 4,
                        transform: 'translateY(-2px)',
                      },
                    }}
                    onClick={() => {
                      if (tool.path) {
                        navigate(tool.path);
                      }
                    }}
                  >
                    <CardContent>
                      <Box sx={{ display: 'flex', alignItems: 'flex-start', mb: 2 }}>
                        {tool.icon}
                        <Box sx={{ ml: 'auto' }}>
                          {tool.badge && (
                            <Chip
                              label={tool.badge}
                              size="small"
                              sx={{ backgroundColor: '#94C456', color: 'white' }}
                            />
                          )}
                        </Box>
                      </Box>
                      <Typography
                        variant="h6"
                        sx={{ mb: 1, fontFamily: 'Antonio, sans-serif', color: '#004F7B' }}
                      >
                        {tool.title}
                      </Typography>
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                        {tool.description}
                      </Typography>
                      <Typography variant="h6" sx={{ color: tool.color, fontWeight: 'medium' }}>
                        {tool.stats}
                      </Typography>
                    </CardContent>
                    <CardActions>
                      <Button
                        fullWidth
                        variant="contained"
                        sx={{ backgroundColor: tool.color, '&:hover': { opacity: 0.9 } }}
                      >
                        {tool.action ? 'Wizard starten' : 'Öffnen'}
                      </Button>
                    </CardActions>
                  </Card>
                </Grid>
              ))}
            </Grid>

            {/* Top Customers */}
            <Paper sx={{ p: 3, mt: 3 }}>
              <Box
                sx={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  mb: 2,
                }}
              >
                <Typography
                  variant="h6"
                  sx={{ fontFamily: 'Antonio, sans-serif', color: '#004F7B' }}
                >
                  Top Kunden nach Umsatz
                </Typography>
                <Button size="small" sx={{ color: '#94C456' }}>
                  Alle anzeigen →
                </Button>
              </Box>
              <List>
                {topCustomers.map((customer, index) => (
                  <React.Fragment key={customer.name}>
                    <ListItem sx={{ px: 0 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', mr: 2 }}>
                        <Typography
                          variant="h6"
                          sx={{ color: '#94C456', fontWeight: 'bold', minWidth: 20 }}
                        >
                          {index + 1}
                        </Typography>
                        <StarIcon
                          sx={{ fontSize: 20, color: index === 0 ? '#FFD700' : '#e0e0e0', ml: 1 }}
                        />
                      </Box>
                      <ListItemText
                        primary={customer.name}
                        secondary={`Jahresumsatz: ${customer.revenue}`}
                      />
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        {customer.trend === 'up' && (
                          <ArrowUpwardIcon sx={{ fontSize: 16, color: '#94C456' }} />
                        )}
                        {customer.trend === 'down' && (
                          <ArrowDownwardIcon sx={{ fontSize: 16, color: '#ef5350' }} />
                        )}
                        <Typography
                          variant="body2"
                          sx={{
                            color:
                              customer.trend === 'up'
                                ? '#94C456'
                                : customer.trend === 'down'
                                  ? '#ef5350'
                                  : '#757575',
                            fontWeight: 'medium',
                          }}
                        >
                          {customer.growth}
                        </Typography>
                      </Box>
                      <IconButton size="small" sx={{ ml: 2 }}>
                        <MoreVertIcon />
                      </IconButton>
                    </ListItem>
                    {index < topCustomers.length - 1 && <Divider />}
                  </React.Fragment>
                ))}
              </List>
            </Paper>
          </Grid>

          {/* Sidebar */}
          <Grid size={{ xs: 12, lg: 4 }}>
            {/* Quick Stats */}
            <Paper sx={{ p: 3, mb: 3 }}>
              <Typography
                variant="h6"
                sx={{ mb: 2, fontFamily: 'Antonio, sans-serif', color: '#004F7B' }}
              >
                Quick Stats
              </Typography>
              <Grid container spacing={2}>
                <Grid size={6}>
                  <Box sx={{ textAlign: 'center' }}>
                    <Typography variant="h5" sx={{ color: '#94C456', fontWeight: 'bold' }}>
                      42
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      Neue Kunden (30T)
                    </Typography>
                  </Box>
                </Grid>
                <Grid size={6}>
                  <Box sx={{ textAlign: 'center' }}>
                    <Typography variant="h5" sx={{ color: '#004F7B', fontWeight: 'bold' }}>
                      3.2
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      Ø Bestellungen/Kunde
                    </Typography>
                  </Box>
                </Grid>
                <Grid size={6}>
                  <Box sx={{ textAlign: 'center', mt: 2 }}>
                    <Typography variant="h5" sx={{ color: '#FFA726', fontWeight: 'bold' }}>
                      18
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      Risiko-Kunden
                    </Typography>
                  </Box>
                </Grid>
                <Grid size={6}>
                  <Box sx={{ textAlign: 'center', mt: 2 }}>
                    <Typography variant="h5" sx={{ color: '#29B6F6', fontWeight: 'bold' }}>
                      96%
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      Retention Rate
                    </Typography>
                  </Box>
                </Grid>
              </Grid>
            </Paper>

            {/* Recent Activities */}
            <Paper sx={{ p: 3 }}>
              <Typography
                variant="h6"
                sx={{ mb: 2, fontFamily: 'Antonio, sans-serif', color: '#004F7B' }}
              >
                Letzte Aktivitäten
              </Typography>
              <List>
                {recentActivities.map((activity, index) => (
                  <ListItem key={index} sx={{ px: 0 }}>
                    <Box sx={{ mr: 2, fontSize: 24 }}>{getActivityIcon(activity.type)}</Box>
                    <ListItemText primary={activity.customer} secondary={activity.time} />
                    <Avatar sx={{ width: 32, height: 32, fontSize: 12, bgcolor: '#94C456' }}>
                      {activity.user}
                    </Avatar>
                  </ListItem>
                ))}
              </List>
              <Button
                fullWidth
                variant="outlined"
                sx={{ mt: 2, borderColor: '#94C456', color: '#94C456' }}
              >
                Alle Aktivitäten
              </Button>
            </Paper>
          </Grid>
        </Grid>
      </Container>
    </MainLayoutV2>
  );
}
