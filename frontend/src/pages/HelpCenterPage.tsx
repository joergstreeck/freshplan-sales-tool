import React from 'react';
import {
  Box,
  Container,
  Typography,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  Paper,
  List,
  ListItem,
  ListItemText,
  Divider,
} from '@mui/material';
import { useLocation, useNavigate } from 'react-router-dom';
import RocketLaunchIcon from '@mui/icons-material/RocketLaunch';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import VideoLibraryIcon from '@mui/icons-material/VideoLibrary';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import SupportAgentIcon from '@mui/icons-material/SupportAgent';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';

const helpSections = {
  'erste-schritte': {
    title: '🚀 Erste Schritte',
    icon: <RocketLaunchIcon sx={{ fontSize: 40, color: '#94C456' }} />,
    description: 'Schnelleinstieg in FreshPlan',
    items: [
      { title: 'Willkommen bei FreshPlan', content: 'Lernen Sie die Grundlagen kennen' },
      { title: 'Erste Anmeldung', content: 'So melden Sie sich zum ersten Mal an' },
      { title: 'Dashboard verstehen', content: 'Überblick über Ihr persönliches Cockpit' },
      { title: 'Ersten Kunden anlegen', content: 'Schritt-für-Schritt Anleitung' },
      { title: 'Angebot erstellen', content: 'Ihr erstes Angebot in 5 Minuten' },
    ],
  },
  'handbuecher': {
    title: '📖 Handbücher',
    icon: <MenuBookIcon sx={{ fontSize: 40, color: '#004F7B' }} />,
    description: 'Detaillierte Anleitungen',
    items: [
      { title: 'Kundenverwaltung', content: 'Alles über Kunden, Kontakte und Standorte' },
      { title: 'Angebotserstellung', content: 'Professionelle Angebote erstellen' },
      { title: 'Benutzerverwaltung', content: 'Benutzer und Rollen verwalten' },
      { title: 'Berichte & Auswertungen', content: 'Daten analysieren und exportieren' },
      { title: 'Systemeinstellungen', content: 'FreshPlan an Ihre Bedürfnisse anpassen' },
    ],
  },
  'videos': {
    title: '🎥 Video-Tutorials',
    icon: <VideoLibraryIcon sx={{ fontSize: 40, color: '#94C456' }} />,
    description: 'Lernen mit Videos',
    items: [
      { title: 'FreshPlan in 10 Minuten', content: 'Kompakter Überblick', duration: '10:23' },
      { title: 'Kundenwizard erklärt', content: 'Neuen Kunden optimal anlegen', duration: '8:45' },
      { title: 'Tipps & Tricks', content: 'Effizienter arbeiten mit FreshPlan', duration: '12:15' },
      { title: 'Reporting Masterclass', content: 'Berichte wie ein Profi', duration: '15:30' },
    ],
  },
  'faq': {
    title: '❓ Häufige Fragen',
    icon: <HelpOutlineIcon sx={{ fontSize: 40, color: '#004F7B' }} />,
    description: 'Antworten auf häufige Fragen',
    items: [
      { title: 'Wie importiere ich Kundendaten?', category: 'Datenimport' },
      { title: 'Kann ich Angebote duplizieren?', category: 'Angebote' },
      { title: 'Wie ändere ich mein Passwort?', category: 'Account' },
      { title: 'Was bedeuten die Status-Farben?', category: 'System' },
      { title: 'Wie exportiere ich Berichte?', category: 'Export' },
    ],
  },
  'support': {
    title: '💬 Support kontaktieren',
    icon: <SupportAgentIcon sx={{ fontSize: 40, color: '#94C456' }} />,
    description: 'Wir helfen Ihnen gerne',
    contactInfo: {
      email: 'support@freshplan.de',
      phone: '+49 (0) 123 456789',
      hours: 'Mo-Fr 8:00 - 18:00 Uhr',
    },
  },
};

export const HelpCenterPage: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const pathParts = location.pathname.split('/');
  const currentSection = pathParts[2] || '';

  const section = helpSections[currentSection as keyof typeof helpSections];

  if (!currentSection || currentSection === '') {
    // Hauptseite des Hilfe-Centers
    return (
      <MainLayoutV2>
        <Container maxWidth="lg" sx={{ py: 4 }}>
          <Typography
            variant="h3"
            sx={{ mb: 1, fontFamily: 'Antonio, sans-serif', color: '#004F7B' }}
          >
            Hilfe-Center
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
            Finden Sie schnell Antworten und lernen Sie FreshPlan optimal zu nutzen
          </Typography>

          <Grid container spacing={3}>
            {Object.entries(helpSections).map(([key, sec]) => (
              <Grid size={{ xs: 12, md: 6 }} key={key}>
                <Card
                  sx={{
                    height: '100%',
                    cursor: 'pointer',
                    transition: 'all 0.3s',
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      boxShadow: 4,
                    },
                  }}
                  onClick={() => navigate(`/hilfe/${key}`)}
                >
                  <CardContent>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                      {sec.icon}
                      <Typography variant="h5" sx={{ ml: 2, fontFamily: 'Antonio, sans-serif' }}>
                        {sec.title}
                      </Typography>
                    </Box>
                    <Typography variant="body2" color="text.secondary">
                      {sec.description}
                    </Typography>
                  </CardContent>
                  <CardActions>
                    <Button
                      size="small"
                      endIcon={<ArrowForwardIcon />}
                      sx={{ color: '#94C456' }}
                    >
                      Öffnen
                    </Button>
                  </CardActions>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Container>
      </MainLayoutV2>
    );
  }

  if (section) {
    // Unterseite
    return (
      <MainLayoutV2>
        <Container maxWidth="lg" sx={{ py: 4 }}>
          <Button onClick={() => navigate('/hilfe')} sx={{ mb: 2 }}>
            ← Zurück zur Übersicht
          </Button>

          <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
            {section.icon}
            <Typography
              variant="h3"
              sx={{ ml: 2, fontFamily: 'Antonio, sans-serif', color: '#004F7B' }}
            >
              {section.title}
            </Typography>
          </Box>

          <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
            {section.description}
          </Typography>

          {currentSection === 'support' && section.contactInfo ? (
            <Grid container spacing={3}>
              <Grid size={{ xs: 12, md: 6 }}>
                <Paper sx={{ p: 3 }}>
                  <Typography variant="h6" sx={{ mb: 2 }}>
                    Kontaktmöglichkeiten
                  </Typography>
                  <List>
                    <ListItem>
                      <ListItemText
                        primary="E-Mail"
                        secondary={section.contactInfo.email}
                      />
                    </ListItem>
                    <ListItem>
                      <ListItemText
                        primary="Telefon"
                        secondary={section.contactInfo.phone}
                      />
                    </ListItem>
                    <ListItem>
                      <ListItemText
                        primary="Servicezeiten"
                        secondary={section.contactInfo.hours}
                      />
                    </ListItem>
                  </List>
                </Paper>
              </Grid>
              <Grid size={{ xs: 12, md: 6 }}>
                <Paper sx={{ p: 3 }}>
                  <Typography variant="h6" sx={{ mb: 2 }}>
                    Schnelle Hilfe
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Bevor Sie uns kontaktieren, schauen Sie doch in unseren FAQ oder Handbüchern
                    nach. Viele Fragen lassen sich dort schnell beantworten.
                  </Typography>
                  <Box sx={{ mt: 2 }}>
                    <Button
                      variant="contained"
                      sx={{
                        backgroundColor: '#94C456',
                        '&:hover': { backgroundColor: '#7BA347' },
                      }}
                    >
                      Support-Ticket erstellen
                    </Button>
                  </Box>
                </Paper>
              </Grid>
            </Grid>
          ) : (
            <List>
              {section.items?.map((item, index) => (
                <React.Fragment key={index}>
                  <ListItem
                    sx={{
                      cursor: 'pointer',
                      '&:hover': { backgroundColor: 'action.hover' },
                    }}
                  >
                    <ListItemText
                      primary={item.title}
                      secondary={
                        'duration' in item
                          ? `Dauer: ${item.duration}`
                          : 'category' in item
                          ? `Kategorie: ${item.category}`
                          : item.content
                      }
                    />
                  </ListItem>
                  {index < section.items.length - 1 && <Divider />}
                </React.Fragment>
              ))}
            </List>
          )}
        </Container>
      </MainLayoutV2>
    );
  }

  // Fallback
  return (
    <MainLayoutV2>
      <Container>
        <Typography>Seite nicht gefunden</Typography>
      </Container>
    </MainLayoutV2>
  );
};