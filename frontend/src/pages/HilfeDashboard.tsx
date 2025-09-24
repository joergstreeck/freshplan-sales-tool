import {
  Box,
  Container,
  Typography,
  Card,
  CardContent,
  CardActions,
  Button,
  Paper,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import { useNavigate } from 'react-router-dom';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import VideoLibraryIcon from '@mui/icons-material/VideoLibrary';
import QuizIcon from '@mui/icons-material/Quiz';
import SupportAgentIcon from '@mui/icons-material/SupportAgent';
import RocketLaunchIcon from '@mui/icons-material/RocketLaunch';

export function HilfeDashboard() {
  const navigate = useNavigate();

  const resources = [
    {
      title: 'Erste Schritte',
      description: 'Schnelleinstieg und Grundlagen',
      icon: <RocketLaunchIcon sx={{ fontSize: 48, color: '#94C456' }} />,
      path: '/hilfe/erste-schritte',
      items: ['Quick Start Guide', 'Onboarding Tour', 'Basis-Setup'],
    },
    {
      title: 'Handbücher',
      description: 'Ausführliche Dokumentation',
      icon: <MenuBookIcon sx={{ fontSize: 48, color: '#004F7B' }} />,
      path: '/hilfe/handbuecher',
      items: ['Benutzerhandbuch', 'Admin-Guide', 'API-Docs'],
    },
    {
      title: 'Video-Tutorials',
      description: 'Schritt-für-Schritt Anleitungen',
      icon: <VideoLibraryIcon sx={{ fontSize: 48, color: '#29B6F6' }} />,
      path: '/hilfe/videos',
      items: ['23 Videos', '4 Std. Content', 'DE/EN'],
    },
    {
      title: 'Häufige Fragen',
      description: 'Antworten auf die wichtigsten Fragen',
      icon: <QuizIcon sx={{ fontSize: 48, color: '#FFA726' }} />,
      path: '/hilfe/faq',
      items: ['156 FAQs', 'Suchbar', 'Kategorisiert'],
    },
    {
      title: 'Support kontaktieren',
      description: 'Direkter Kontakt zum Support-Team',
      icon: <SupportAgentIcon sx={{ fontSize: 48, color: '#EF5350' }} />,
      path: '/hilfe/support',
      items: ['Live-Chat', 'E-Mail', 'Telefon'],
    },
  ];

  return (
    <MainLayoutV2>
      <Container maxWidth="xl" sx={{ py: 4 }}>
        <Box sx={{ mb: 4 }}>
          <Typography
            variant="h3"
            sx={{ mb: 1, fontFamily: 'Antonio, sans-serif', fontWeight: 'bold', color: '#004F7B' }}
          >
            Hilfe & Support
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Finden Sie schnell Antworten und Lösungen für Ihre Fragen
          </Typography>
        </Box>

        {/* Quick Search */}
        <Paper sx={{ p: 3, mb: 4, backgroundColor: '#f8f9fa', textAlign: 'center' }}>
          <Typography
            variant="h5"
            sx={{ mb: 2, fontFamily: 'Antonio, sans-serif', color: '#004F7B' }}
          >
            Wie können wir Ihnen helfen?
          </Typography>
          <Box sx={{ maxWidth: 600, mx: 'auto' }}>
            <Button
              variant="contained"
              size="large"
              sx={{ backgroundColor: '#94C456', '&:hover': { backgroundColor: '#7BA347' } }}
              onClick={() => navigate('/hilfe/faq')}
            >
              Suche starten
            </Button>
          </Box>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
            Oder wählen Sie einen Bereich unten aus
          </Typography>
        </Paper>

        {/* Resource Cards */}
        <Grid container spacing={3}>
          {resources.map(resource => (
            <Grid
              key={resource.title}
              size={{
                xs: 12,
                sm: 6,
                lg: resource.title === 'Erste Schritte' ? 12 : 6,
                xl: resource.title === 'Erste Schritte' ? 4 : 3,
              }}
            >
              <Card
                sx={{
                  height: '100%',
                  transition: 'all 0.2s',
                  cursor: 'pointer',
                  border: resource.title === 'Erste Schritte' ? '2px solid #94C456' : 'none',
                  '&:hover': { boxShadow: 4, transform: 'translateY(-2px)' },
                }}
                onClick={() => navigate(resource.path)}
              >
                <CardContent>
                  <Box sx={{ textAlign: 'center', py: 2 }}>
                    {resource.icon}
                    <Typography
                      variant="h6"
                      sx={{ mt: 2, mb: 1, fontFamily: 'Antonio, sans-serif', color: '#004F7B' }}
                    >
                      {resource.title}
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                      {resource.description}
                    </Typography>
                    <Box
                      sx={{ display: 'flex', gap: 1, justifyContent: 'center', flexWrap: 'wrap' }}
                    >
                      {resource.items.map(item => (
                        <Typography
                          key={item}
                          variant="caption"
                          sx={{ px: 1, py: 0.5, backgroundColor: '#f5f5f5', borderRadius: 1 }}
                        >
                          {item}
                        </Typography>
                      ))}
                    </Box>
                  </Box>
                </CardContent>
                <CardActions>
                  <Button
                    fullWidth
                    variant={resource.title === 'Support kontaktieren' ? 'contained' : 'text'}
                    sx={{
                      backgroundColor:
                        resource.title === 'Support kontaktieren' ? '#EF5350' : 'transparent',
                      color: resource.title === 'Support kontaktieren' ? 'white' : '#004F7B',
                      '&:hover': {
                        backgroundColor:
                          resource.title === 'Support kontaktieren'
                            ? '#c62828'
                            : 'rgba(0, 79, 123, 0.04)',
                      },
                    }}
                  >
                    {resource.title === 'Support kontaktieren'
                      ? 'Kontakt aufnehmen'
                      : 'Mehr erfahren'}
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>

        {/* Popular Topics */}
        <Paper sx={{ p: 3, mt: 4, backgroundColor: '#e8f5e9', borderLeft: '4px solid #94C456' }}>
          <Typography variant="h6" sx={{ mb: 1, color: '#004F7B' }}>
            Beliebte Themen diese Woche
          </Typography>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, sm: 4 }}>
              <Typography variant="body2">• Neue Dashboard-Features</Typography>
            </Grid>
            <Grid size={{ xs: 12, sm: 4 }}>
              <Typography variant="body2">• CSV-Import Anleitung</Typography>
            </Grid>
            <Grid size={{ xs: 12, sm: 4 }}>
              <Typography variant="body2">• API-Integration Guide</Typography>
            </Grid>
          </Grid>
        </Paper>
      </Container>
    </MainLayoutV2>
  );
}
