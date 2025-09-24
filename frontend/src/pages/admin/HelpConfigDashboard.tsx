import {
  Box,
  Container,
  Typography,
  Card,
  CardContent,
  CardActions,
  Button,
  Paper,
  Breadcrumbs,
  Link,
  Chip,
  Stack,
  Avatar,
  AvatarGroup,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import { useNavigate } from 'react-router-dom';
import { MainLayoutV2 } from '../../components/layout/MainLayoutV2';

// Icons
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import NavigateNextIcon from '@mui/icons-material/NavigateNext';
import PlayCircleIcon from '@mui/icons-material/PlayCircle';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import TourIcon from '@mui/icons-material/Tour';
import AnalyticsIcon from '@mui/icons-material/Analytics';
import ArticleIcon from '@mui/icons-material/Article';
import VideoLibraryIcon from '@mui/icons-material/VideoLibrary';
import QuizIcon from '@mui/icons-material/Quiz';
import SchoolIcon from '@mui/icons-material/School';

interface HelpToolCard {
  id: string;
  title: string;
  description: string;
  icon: React.ReactNode;
  path: string;
  stats?: {
    label: string;
    value: string;
    color?: string;
  };
  badges?: string[];
  users?: number;
  lastUpdated?: string;
}

export function HelpConfigDashboard() {
  const navigate = useNavigate();

  const helpTools: HelpToolCard[] = [
    {
      id: 'demo',
      title: 'Hilfe-System Demo',
      description: 'Testen Sie das kontextsensitive Hilfesystem mit Tooltips und Touren',
      icon: <PlayCircleIcon sx={{ fontSize: 48, color: '#94C456' }} />,
      path: '/admin/help/demo',
      stats: {
        label: 'Aktive Features',
        value: '12',
        color: '#94C456',
      },
      badges: ['Live', 'Interaktiv'],
      lastUpdated: 'Heute',
    },
    {
      id: 'tooltips',
      title: 'Tooltips verwalten',
      description: 'Erstellen und bearbeiten Sie kontextuelle Hilfehinweise',
      icon: <HelpOutlineIcon sx={{ fontSize: 48, color: '#004F7B' }} />,
      path: '/admin/help/tooltips',
      stats: {
        label: 'Tooltips',
        value: '247',
      },
      badges: ['Mehrsprachig'],
      users: 1240,
      lastUpdated: 'vor 2 Tagen',
    },
    {
      id: 'tours',
      title: 'Touren erstellen',
      description: 'Interaktive Schritt-für-Schritt Anleitungen für neue Features',
      icon: <TourIcon sx={{ fontSize: 48, color: '#004F7B' }} />,
      path: '/admin/help/tours',
      stats: {
        label: 'Aktive Touren',
        value: '8',
      },
      badges: ['Onboarding', 'Features'],
      users: 523,
      lastUpdated: 'vor 1 Woche',
    },
    {
      id: 'analytics',
      title: 'Analytics',
      description:
        'Analysieren Sie die Nutzung von Hilfe-Features und identifizieren Sie Problembereiche',
      icon: <AnalyticsIcon sx={{ fontSize: 48, color: '#004F7B' }} />,
      path: '/admin/help/analytics',
      stats: {
        label: 'Hilfeanfragen heute',
        value: '142',
        color: '#FFA726',
      },
      badges: ['Dashboard', 'Reports'],
      lastUpdated: 'Live',
    },
  ];

  const additionalResources = [
    {
      id: 'docs',
      title: 'Dokumentation',
      icon: <ArticleIcon sx={{ fontSize: 32, color: '#004F7B' }} />,
      count: '45 Artikel',
      updated: 'Täglich',
    },
    {
      id: 'videos',
      title: 'Video-Tutorials',
      icon: <VideoLibraryIcon sx={{ fontSize: 32, color: '#004F7B' }} />,
      count: '23 Videos',
      updated: 'Wöchentlich',
    },
    {
      id: 'faq',
      title: 'FAQ Verwaltung',
      icon: <QuizIcon sx={{ fontSize: 32, color: '#004F7B' }} />,
      count: '156 Fragen',
      updated: 'vor 3 Tagen',
    },
    {
      id: 'training',
      title: 'Schulungen',
      icon: <SchoolIcon sx={{ fontSize: 32, color: '#004F7B' }} />,
      count: '4 Kurse',
      updated: 'Monatlich',
    },
  ];

  return (
    <MainLayoutV2>
      <Container maxWidth="xl" sx={{ py: 4 }}>
        {/* Breadcrumbs */}
        <Breadcrumbs separator={<NavigateNextIcon fontSize="small" />} sx={{ mb: 3 }}>
          <Link
            component="button"
            variant="body1"
            onClick={() => navigate('/admin')}
            sx={{
              textDecoration: 'none',
              color: '#004F7B',
              '&:hover': { textDecoration: 'underline' },
            }}
          >
            Administration
          </Link>
          <Typography color="text.primary">Hilfe-Konfiguration</Typography>
        </Breadcrumbs>

        {/* Header */}
        <Box sx={{ mb: 4, display: 'flex', alignItems: 'center', gap: 2 }}>
          <Button
            startIcon={<ArrowBackIcon />}
            onClick={() => navigate('/admin')}
            sx={{ color: '#004F7B' }}
          >
            Zurück
          </Button>
          <Box sx={{ flex: 1 }}>
            <Typography
              variant="h3"
              sx={{
                mb: 1,
                fontFamily: 'Antonio, sans-serif',
                fontWeight: 'bold',
                color: '#004F7B',
              }}
            >
              Hilfe-System Verwaltung
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Kontextsensitive Hilfe, Tooltips, Touren und Onboarding konfigurieren
            </Typography>
          </Box>
        </Box>

        {/* Stats Overview */}
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid size={{ xs: 12, md: 3 }}>
            <Paper sx={{ p: 2, textAlign: 'center', backgroundColor: '#f8f9fa' }}>
              <Typography variant="h4" sx={{ color: '#94C456', fontWeight: 'bold' }}>
                89%
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Nutzer verwenden Hilfe
              </Typography>
            </Paper>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Paper sx={{ p: 2, textAlign: 'center', backgroundColor: '#f8f9fa' }}>
              <Typography variant="h4" sx={{ color: '#004F7B', fontWeight: 'bold' }}>
                4.6/5
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Zufriedenheit
              </Typography>
            </Paper>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Paper sx={{ p: 2, textAlign: 'center', backgroundColor: '#f8f9fa' }}>
              <Typography variant="h4" sx={{ color: '#004F7B', fontWeight: 'bold' }}>
                -32%
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Support-Tickets
              </Typography>
            </Paper>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Paper sx={{ p: 2, textAlign: 'center', backgroundColor: '#f8f9fa' }}>
              <Typography variant="h4" sx={{ color: '#FFA726', fontWeight: 'bold' }}>
                15 Min
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Ø Onboarding Zeit
              </Typography>
            </Paper>
          </Grid>
        </Grid>

        {/* Main Tools */}
        <Typography
          variant="h5"
          sx={{
            mb: 2,
            fontFamily: 'Antonio, sans-serif',
            color: '#004F7B',
          }}
        >
          Hauptfunktionen
        </Typography>
        <Grid container spacing={3} sx={{ mb: 4 }}>
          {helpTools.map(tool => (
            <Grid key={tool.id} size={{ xs: 12, md: 6 }}>
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
                    {tool.badges && (
                      <Box sx={{ ml: 'auto', display: 'flex', gap: 1 }}>
                        {tool.badges.map(badge => (
                          <Chip
                            key={badge}
                            label={badge}
                            size="small"
                            sx={{
                              backgroundColor: badge === 'Live' ? '#94C456' : '#e0e0e0',
                              color: badge === 'Live' ? 'white' : 'text.secondary',
                            }}
                          />
                        ))}
                      </Box>
                    )}
                  </Box>

                  <Typography
                    variant="h5"
                    sx={{
                      mb: 1,
                      fontFamily: 'Antonio, sans-serif',
                      color: '#004F7B',
                    }}
                  >
                    {tool.title}
                  </Typography>

                  <Typography variant="body2" color="text.secondary" sx={{ mb: 3, minHeight: 40 }}>
                    {tool.description}
                  </Typography>

                  <Stack direction="row" spacing={3} alignItems="center">
                    {tool.stats && (
                      <Box>
                        <Typography variant="caption" color="text.secondary">
                          {tool.stats.label}
                        </Typography>
                        <Typography
                          variant="h6"
                          sx={{
                            fontWeight: 'medium',
                            color: tool.stats.color || 'inherit',
                          }}
                        >
                          {tool.stats.value}
                        </Typography>
                      </Box>
                    )}
                    {tool.users && (
                      <Box>
                        <Typography variant="caption" color="text.secondary">
                          Genutzt von
                        </Typography>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <AvatarGroup
                            max={3}
                            sx={{ '& .MuiAvatar-root': { width: 24, height: 24 } }}
                          >
                            <Avatar sx={{ width: 24, height: 24 }} />
                            <Avatar sx={{ width: 24, height: 24 }} />
                            <Avatar sx={{ width: 24, height: 24 }} />
                          </AvatarGroup>
                          <Typography variant="body2">
                            {tool.users.toLocaleString('de-DE')}+
                          </Typography>
                        </Box>
                      </Box>
                    )}
                    <Box sx={{ ml: 'auto' }}>
                      <Typography variant="caption" color="text.secondary">
                        Aktualisiert
                      </Typography>
                      <Typography variant="body2">{tool.lastUpdated}</Typography>
                    </Box>
                  </Stack>
                </CardContent>

                <CardActions sx={{ px: 2, pb: 2 }}>
                  <Button
                    fullWidth
                    variant="contained"
                    sx={{
                      backgroundColor: '#94C456',
                      '&:hover': { backgroundColor: '#7BA347' },
                    }}
                  >
                    {tool.id === 'demo' ? 'Demo starten' : 'Öffnen'}
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>

        {/* Additional Resources */}
        <Typography
          variant="h5"
          sx={{
            mb: 2,
            fontFamily: 'Antonio, sans-serif',
            color: '#004F7B',
          }}
        >
          Weitere Ressourcen
        </Typography>
        <Grid container spacing={2}>
          {additionalResources.map(resource => (
            <Grid key={resource.id} size={{ xs: 12, sm: 6, md: 3 }}>
              <Paper
                sx={{
                  p: 2,
                  cursor: 'pointer',
                  transition: 'all 0.2s',
                  '&:hover': {
                    backgroundColor: '#f5f5f5',
                    transform: 'translateY(-2px)',
                  },
                }}
              >
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  {resource.icon}
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 'medium' }}>
                      {resource.title}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {resource.count}
                    </Typography>
                  </Box>
                </Box>
                <Typography
                  variant="caption"
                  color="text.secondary"
                  sx={{ display: 'block', mt: 1 }}
                >
                  Aktualisiert: {resource.updated}
                </Typography>
              </Paper>
            </Grid>
          ))}
        </Grid>

        {/* Info Box */}
        <Paper
          sx={{
            mt: 4,
            p: 3,
            backgroundColor: '#e8f5e9',
            borderLeft: '4px solid #94C456',
          }}
        >
          <Typography variant="h6" sx={{ mb: 1, color: '#004F7B' }}>
            Tipp: Proaktive Hilfe aktivieren
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Aktivieren Sie proaktive Hilfehinweise, um Nutzer automatisch bei häufigen Problemen zu
            unterstützen. Dies reduziert Support-Anfragen um durchschnittlich 40%.
          </Typography>
          <Button
            variant="text"
            sx={{ mt: 1, color: '#94C456' }}
            onClick={() => navigate('/admin/help/demo')}
          >
            Mehr erfahren →
          </Button>
        </Paper>
      </Container>
    </MainLayoutV2>
  );
}
