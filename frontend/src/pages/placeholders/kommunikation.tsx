import React from 'react';
import { PlaceholderPage } from '../PlaceholderPage';
import ChatIcon from '@mui/icons-material/Chat';
import AnnouncementIcon from '@mui/icons-material/Announcement';
import NotesIcon from '@mui/icons-material/Notes';
import MailIcon from '@mui/icons-material/Mail';
import PersonIcon from '@mui/icons-material/Person';
import NotificationsIcon from '@mui/icons-material/Notifications';
import PaletteIcon from '@mui/icons-material/Palette';
import SecurityIcon from '@mui/icons-material/Security';

// Kommunikation
export const TeamChat = () => (
  <PlaceholderPage
    title="Team-Chat"
    subtitle="Echtzeitkommunikation mit Ihrem Team"
    description="Bleiben Sie mit Ihrem Team in Kontakt - direkt in FreshPlan."
    expectedDate="Q2 2025"
    features={[
      'Echtzeit-Messaging',
      'Team-Channels',
      'Datei-Sharing',
    ]}
    icon={<ChatIcon sx={{ fontSize: 80, color: '#94C456' }} />}
  />
);

export const Ankuendigungen = () => (
  <PlaceholderPage
    title="Ankündigungen"
    subtitle="Wichtige Mitteilungen an das gesamte Team"
    description="Teilen Sie wichtige Updates und Neuigkeiten mit allen Mitarbeitern."
    expectedDate="Q1 2025"
    features={[
      'Unternehmens-News',
      'Prioritäts-Markierungen',
      'Lesebestätigungen',
    ]}
    icon={<AnnouncementIcon sx={{ fontSize: 80, color: '#94C456' }} />}
  />
);

export const Notizen = () => (
  <PlaceholderPage
    title="Notizen"
    subtitle="Persönliche und geteilte Notizen verwalten"
    description="Halten Sie Ideen fest und teilen Sie Notizen mit Kollegen."
    expectedDate="Q2 2025"
    features={[
      'Rich-Text Editor',
      'Notiz-Kategorien',
      'Team-Notizen',
    ]}
    icon={<NotesIcon sx={{ fontSize: 80, color: '#94C456' }} />}
  />
);

export const InterneNachrichten = () => (
  <PlaceholderPage
    title="Interne Nachrichten"
    subtitle="Direktnachrichten zwischen Teammitgliedern"
    description="Privater Nachrichtenaustausch innerhalb Ihres Teams."
    expectedDate="Q2 2025"
    features={[
      'Direkt-Nachrichten',
      'Gruppen-Nachrichten',
      'Nachrichtenverlauf',
    ]}
    icon={<MailIcon sx={{ fontSize: 80, color: '#94C456' }} />}
  />
);

// Einstellungen
export const MeinProfil = () => (
  <PlaceholderPage
    title="Mein Profil"
    subtitle="Verwalten Sie Ihre persönlichen Informationen"
    description="Aktualisieren Sie Ihr Profil, Kontaktdaten und Präferenzen."
    expectedDate="Q1 2025"
    features={[
      'Profil-Informationen',
      'Avatar-Upload',
      'Kontaktdaten',
    ]}
    icon={<PersonIcon sx={{ fontSize: 80, color: '#004F7B' }} />}
  />
);

export const Benachrichtigungen = () => (
  <PlaceholderPage
    title="Benachrichtigungen"
    subtitle="Steuern Sie, worüber Sie informiert werden"
    description="Personalisieren Sie Ihre Benachrichtigungseinstellungen."
    expectedDate="Q1 2025"
    features={[
      'E-Mail-Benachrichtigungen',
      'In-App Notifications',
      'Push-Nachrichten',
    ]}
    icon={<NotificationsIcon sx={{ fontSize: 80, color: '#004F7B' }} />}
  />
);

export const Darstellung = () => (
  <PlaceholderPage
    title="Darstellung"
    subtitle="Passen Sie das Aussehen von FreshPlan an"
    description="Themes, Layouts und visuelle Präferenzen anpassen."
    expectedDate="Q2 2025"
    features={[
      'Dark Mode',
      'Farbschemas',
      'Schriftgrößen',
    ]}
    icon={<PaletteIcon sx={{ fontSize: 80, color: '#004F7B' }} />}
  />
);

export const Sicherheit = () => (
  <PlaceholderPage
    title="Sicherheit"
    subtitle="Schützen Sie Ihr Konto mit erweiterten Sicherheitsoptionen"
    description="Verwalten Sie Passwörter, Zwei-Faktor-Authentifizierung und aktive Sessions."
    expectedDate="Q1 2025"
    features={[
      'Passwort ändern',
      'Zwei-Faktor-Authentifizierung',
      'Session-Verwaltung',
    ]}
    icon={<SecurityIcon sx={{ fontSize: 80, color: '#004F7B' }} />}
  />
);