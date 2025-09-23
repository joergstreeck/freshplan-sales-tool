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
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  LinearProgress,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import { useNavigate } from 'react-router-dom';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';

// Icons
import EmailIcon from '@mui/icons-material/Email';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import CampaignIcon from '@mui/icons-material/Campaign';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import ScheduleIcon from '@mui/icons-material/Schedule';

interface LeadCard {
  title: string;
  description: string;
  icon: React.ReactNode;
  path: string;
  stats: {
    primary: string;
    secondary?: string;
    trend?: 'up' | 'down' | 'neutral';
  };
  badge?: string;
}

export function NeukundengewinnungDashboard() {
  const navigate = useNavigate();

  const leadTools: LeadCard[] = [
    {
      title: 'E-Mail Posteingang',
      description: 'Verwalten Sie eingehende Anfragen und konvertieren Sie diese zu Leads',
      icon: <EmailIcon sx={{ fontSize: 48, color: '#94C456' }} />,
      path: '/neukundengewinnung/posteingang',
      stats: {
        primary: '23 neue',
        secondary: '142 gesamt',
        trend: 'up',
      },
      badge: 'Priorität',
    },
    {
      title: 'Lead-Erfassung',
      description: 'Erfassen und qualifizieren Sie neue Interessenten systematisch',
      icon: <PersonAddIcon sx={{ fontSize: 48, color: '#004F7B' }} />,
      path: '/neukundengewinnung/leads',
      stats: {
        primary: '67 Leads',
        secondary: 'Diese Woche',
        trend: 'up',
      },
    },
    {
      title: 'Kampagnen',
      description: 'Planen und verwalten Sie Ihre Marketing-Kampagnen',
      icon: <CampaignIcon sx={{ fontSize: 48, color: '#004F7B' }} />,
      path: '/neukundengewinnung/kampagnen',
      stats: {
        primary: '5 aktiv',
        secondary: '12% Conv. Rate',
        trend: 'neutral',
      },
    },
  ];

  const recentLeads = [
    { name: 'Max Mustermann', company: 'Gastro GmbH', status: 'new', time: 'vor 5 Min' },
    { name: 'Anna Schmidt', company: 'Hotel Adler', status: 'qualified', time: 'vor 1 Std' },
    { name: 'Thomas Weber', company: 'Restaurant Sonne', status: 'contacted', time: 'vor 2 Std' },
    { name: 'Maria Fischer', company: 'Café Central', status: 'new', time: 'vor 3 Std' },
  ];

  const upcomingActions = [
    { action: 'Follow-up Call', contact: 'Hotel Bergblick', time: '14:00' },
    { action: 'Demo Meeting', contact: 'Gasthaus Rose', time: '15:30' },
    { action: 'Angebot senden', contact: 'Restaurant Marina', time: 'Morgen' },
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'new': return '#94C456';
      case 'qualified': return '#29B6F6';
      case 'contacted': return '#FFA726';
      default: return '#757575';
    }
  };

  return (
    <MainLayoutV2>
      <Container maxWidth="xl" sx={{ py: 4 }}>
        {/* Header */}
        <Box sx={{ mb: 4 }}>
          <Typography
            variant="h3"
            sx={{
              mb: 1,
              fontFamily: 'Antonio, sans-serif',
              fontWeight: 'bold',
              color: '#004F7B',
            }}
          >
            Neukundengewinnung
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Verwalten Sie Leads, Anfragen und Kampagnen zur Kundengewinnung
          </Typography>
        </Box>

        {/* KPI Overview */}
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Paper sx={{ p: 2, textAlign: 'center' }}>
              <Typography variant="h4" sx={{ color: '#94C456', fontWeight: 'bold' }}>
                234
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Leads diesen Monat
              </Typography>
              <Typography variant="caption" sx={{ color: '#94C456' }}>
                +18% zum Vormonat
              </Typography>
            </Paper>
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Paper sx={{ p: 2, textAlign: 'center' }}>
              <Typography variant="h4" sx={{ color: '#004F7B', fontWeight: 'bold' }}>
                28%
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Conversion Rate
              </Typography>
              <Typography variant="caption" sx={{ color: '#94C456' }}>
                +3% diese Woche
              </Typography>
            </Paper>
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Paper sx={{ p: 2, textAlign: 'center' }}>
              <Typography variant="h4" sx={{ color: '#004F7B', fontWeight: 'bold' }}>
                €45k
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Pipeline Wert
              </Typography>
              <Typography variant="caption" color="text.secondary">
                12 Opportunities
              </Typography>
            </Paper>
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Paper sx={{ p: 2, textAlign: 'center' }}>
              <Typography variant="h4" sx={{ color: '#FFA726', fontWeight: 'bold' }}>
                7 Tage
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Ø Lead-Zeit
              </Typography>
              <Typography variant="caption" sx={{ color: '#94C456' }}>
                -2 Tage verbessert
              </Typography>
            </Paper>
          </Grid>
        </Grid>

        {/* Main Content */}
        <Grid container spacing={3}>
          {/* Tool Cards */}
          <Grid size={{ xs: 12, lg: 8 }}>
            <Grid container spacing={3}>
              {leadTools.map((tool) => (
                <Grid key={tool.title} size={{ xs: 12, md: 6, lg: 4 }}>
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
                    onClick={() => navigate(tool.path)}
                  >
                    <CardContent>
                      <Box sx={{ display: 'flex', alignItems: 'flex-start', mb: 2 }}>
                        {tool.icon}
                        {tool.badge && (
                          <Chip
                            label={tool.badge}
                            size="small"
                            sx={{
                              ml: 'auto',
                              backgroundColor: '#94C456',
                              color: 'white',
                            }}
                          />
                        )}
                      </Box>
                      <Typography
                        variant="h6"
                        sx={{
                          mb: 1,
                          fontFamily: 'Antonio, sans-serif',
                          color: '#004F7B',
                        }}
                      >
                        {tool.title}
                      </Typography>
                      <Typography
                        variant="body2"
                        color="text.secondary"
                        sx={{ mb: 2, minHeight: 40 }}
                      >
                        {tool.description}
                      </Typography>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Box>
                          <Typography variant="h6" sx={{ color: tool.stats.trend === 'up' ? '#94C456' : '#004F7B' }}>
                            {tool.stats.primary}
                          </Typography>
                          {tool.stats.secondary && (
                            <Typography variant="caption" color="text.secondary">
                              {tool.stats.secondary}
                            </Typography>
                          )}
                        </Box>
                        {tool.stats.trend && (
                          <TrendingUpIcon sx={{ color: tool.stats.trend === 'up' ? '#94C456' : '#757575' }} />
                        )}
                      </Box>
                    </CardContent>
                    <CardActions>
                      <Button fullWidth sx={{ color: '#004F7B' }}>
                        Öffnen
                      </Button>
                    </CardActions>
                  </Card>
                </Grid>
              ))}

              {/* Lead Funnel */}
              <Grid size={{ xs: 12, lg: 12 }}>
                <Paper sx={{ p: 3 }}>
                  <Typography variant="h6" sx={{ mb: 2, fontFamily: 'Antonio, sans-serif', color: '#004F7B' }}>
                    Lead Funnel
                  </Typography>
                  <Box sx={{ mb: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="body2">Anfragen</Typography>
                      <Typography variant="body2" fontWeight="bold">234</Typography>
                    </Box>
                    <LinearProgress variant="determinate" value={100} sx={{ height: 8, borderRadius: 1, backgroundColor: '#e0e0e0', '& .MuiLinearProgress-bar': { backgroundColor: '#94C456' } }} />
                  </Box>
                  <Box sx={{ mb: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="body2">Qualifiziert</Typography>
                      <Typography variant="body2" fontWeight="bold">142</Typography>
                    </Box>
                    <LinearProgress variant="determinate" value={60} sx={{ height: 8, borderRadius: 1, backgroundColor: '#e0e0e0', '& .MuiLinearProgress-bar': { backgroundColor: '#29B6F6' } }} />
                  </Box>
                  <Box sx={{ mb: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="body2">Opportunity</Typography>
                      <Typography variant="body2" fontWeight="bold">67</Typography>
                    </Box>
                    <LinearProgress variant="determinate" value={28} sx={{ height: 8, borderRadius: 1, backgroundColor: '#e0e0e0', '& .MuiLinearProgress-bar': { backgroundColor: '#FFA726' } }} />
                  </Box>
                  <Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="body2">Gewonnen</Typography>
                      <Typography variant="body2" fontWeight="bold">23</Typography>
                    </Box>
                    <LinearProgress variant="determinate" value={10} sx={{ height: 8, borderRadius: 1, backgroundColor: '#e0e0e0', '& .MuiLinearProgress-bar': { backgroundColor: '#004F7B' } }} />
                  </Box>
                </Paper>
              </Grid>
            </Grid>
          </Grid>

          {/* Sidebar */}
          <Grid size={{ xs: 12, lg: 4 }}>
            {/* Recent Leads */}
            <Paper sx={{ p: 3, mb: 3 }}>
              <Typography variant="h6" sx={{ mb: 2, fontFamily: 'Antonio, sans-serif', color: '#004F7B' }}>
                Neue Leads
              </Typography>
              <List>
                {recentLeads.map((lead, index) => (
                  <ListItem key={index} sx={{ px: 0 }}>
                    <ListItemAvatar>
                      <Avatar sx={{ bgcolor: getStatusColor(lead.status) }}>
                        {lead.name.split(' ').map(n => n[0]).join('')}
                      </Avatar>
                    </ListItemAvatar>
                    <ListItemText
                      primary={lead.name}
                      secondary={lead.company}
                    />
                    <Box sx={{ textAlign: 'right' }}>
                      <Typography variant="caption" color="text.secondary">
                        {lead.time}
                      </Typography>
                      <Chip
                        label={lead.status === 'new' ? 'Neu' : lead.status === 'qualified' ? 'Qualifiziert' : 'Kontaktiert'}
                        size="small"
                        sx={{
                          display: 'block',
                          mt: 0.5,
                          backgroundColor: getStatusColor(lead.status),
                          color: 'white',
                        }}
                      />
                    </Box>
                  </ListItem>
                ))}
              </List>
              <Button fullWidth variant="text" sx={{ mt: 1, color: '#94C456' }}>
                Alle Leads anzeigen →
              </Button>
            </Paper>

            {/* Upcoming Actions */}
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" sx={{ mb: 2, fontFamily: 'Antonio, sans-serif', color: '#004F7B' }}>
                Anstehende Aktionen
              </Typography>
              <List>
                {upcomingActions.map((action, index) => (
                  <ListItem key={index} sx={{ px: 0 }}>
                    <ListItemAvatar>
                      <Avatar sx={{ bgcolor: '#f5f5f5' }}>
                        <ScheduleIcon sx={{ color: '#004F7B' }} />
                      </Avatar>
                    </ListItemAvatar>
                    <ListItemText
                      primary={action.action}
                      secondary={action.contact}
                    />
                    <Typography variant="body2" sx={{ color: '#94C456', fontWeight: 'medium' }}>
                      {action.time}
                    </Typography>
                  </ListItem>
                ))}
              </List>
              <Button fullWidth variant="contained" sx={{ mt: 2, backgroundColor: '#94C456', '&:hover': { backgroundColor: '#7BA347' } }}>
                Neue Aktion planen
              </Button>
            </Paper>
          </Grid>
        </Grid>
      </Container>
    </MainLayoutV2>
  );
}