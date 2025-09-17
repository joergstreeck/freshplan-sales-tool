import React from 'react';
import { PlaceholderPage } from '../PlaceholderPage';
import EmailIcon from '@mui/icons-material/Email';
import CampaignIcon from '@mui/icons-material/Campaign';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import AssessmentIcon from '@mui/icons-material/Assessment';
import SettingsIcon from '@mui/icons-material/Settings';
import StorageIcon from '@mui/icons-material/Storage';
import ArticleIcon from '@mui/icons-material/Article';
import EditNoteIcon from '@mui/icons-material/EditNote';
import RouteIcon from '@mui/icons-material/Route';
import InsightsIcon from '@mui/icons-material/Insights';
import LeaderboardIcon from '@mui/icons-material/Leaderboard';
import GroupIcon from '@mui/icons-material/Group';
import LocalActivityIcon from '@mui/icons-material/LocalActivity';
import LibraryBooksIcon from '@mui/icons-material/LibraryBooks';
import VideoLibraryIcon from '@mui/icons-material/VideoLibrary';
import QuizIcon from '@mui/icons-material/Quiz';
import SupportAgentIcon from '@mui/icons-material/SupportAgent';
import SchoolIcon from '@mui/icons-material/School';
import HelpIcon from '@mui/icons-material/Help';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import AccountTreeIcon from '@mui/icons-material/AccountTree';
import NotificationsIcon from '@mui/icons-material/Notifications';
import PaletteIcon from '@mui/icons-material/Palette';
import SecurityIcon from '@mui/icons-material/Security';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import BackupIcon from '@mui/icons-material/Backup';
import SpeedIcon from '@mui/icons-material/Speed';
import DashboardIcon from '@mui/icons-material/Dashboard';

// Neukundengewinnung
export const EmailPosteingang = () => (
  <PlaceholderPage
    title="E-Mail Posteingang"
    subtitle="Verwalten Sie eingehende Leads direkt aus Ihrem Posteingang"
    description="Integrierte E-Mail-Verwaltung für eine nahtlose Lead-Bearbeitung."
    expectedDate="Q2 2025"
    features={[
      'E-Mail-Integration mit Outlook/Gmail',
      'Automatische Lead-Erkennung',
      'Quick-Actions für Lead-Konvertierung',
    ]}
    icon={<EmailIcon sx={{ fontSize: 80, color: '#94C456' }} />}
  />
);

export const Kampagnen = () => (
  <PlaceholderPage
    title="Kampagnen"
    subtitle="Planen und verwalten Sie Ihre Marketing-Kampagnen"
    description="Erstellen Sie zielgerichtete Kampagnen für maximale Neukundengewinnung."
    expectedDate="Q2 2025"
    features={[
      'Kampagnen-Designer',
      'E-Mail-Templates',
      'Performance-Tracking',
    ]}
    icon={<CampaignIcon sx={{ fontSize: 80, color: '#94C456' }} />}
  />
);

export const LeadErfassung = () => (
  <PlaceholderPage
    title="Lead-Erfassung"
    subtitle="Erfassen und qualifizieren Sie neue Leads"
    description="Strukturierte Lead-Erfassung mit automatischer Qualifizierung."
    expectedDate="Q2 2025"
    features={[
      'Lead-Formular',
      'Automatische Qualifizierung',
      'Lead-Scoring',
      'Integration mit CRM-Prozessen',
    ]}
    icon={<PersonAddIcon sx={{ fontSize: 80, color: '#94C456' }} />}
  />
);

export const Verkaufschancen = () => (
  <PlaceholderPage
    title="Verkaufschancen"
    subtitle="Verwalten Sie Ihre Sales Pipeline"
    description="Behalten Sie alle Verkaufschancen im Blick und optimieren Sie Ihre Abschlussrate."
    expectedDate="Q1 2025"
    features={[
      'Pipeline-Übersicht',
      'Opportunity-Management',
      'Forecast-Berichte',
      'Automatische Nachfass-Erinnerungen',
    ]}
    icon={<AccountTreeIcon sx={{ fontSize: 80, color: '#94C456' }} />}
  />
);

// Kundenmanagement
export const Aktivitaeten = () => (
  <PlaceholderPage
    title="Aktivitäten"
    subtitle="Behalten Sie alle Kundeninteraktionen im Blick"
    description="Vollständige Historie aller Aktivitäten und Interaktionen mit Ihren Kunden."
    expectedDate="Q1 2025"
    features={[
      'Aktivitäts-Timeline',
      'Automatische Erfassung',
      'Filter und Suche',
    ]}
    icon={<LocalActivityIcon sx={{ fontSize: 80, color: '#94C456' }} />}
  />
);

// Berichte
export const UmsatzBericht = () => (
  <PlaceholderPage
    title="Umsatzübersicht"
    subtitle="Detaillierte Umsatzanalysen und Prognosen"
    description="Verstehen Sie Ihre Umsatzentwicklung mit interaktiven Dashboards."
    expectedDate="Q2 2025"
    features={[
      'Umsatz-Dashboards',
      'Trend-Analysen',
      'Prognose-Tools',
    ]}
    icon={<TrendingUpIcon sx={{ fontSize: 80, color: '#94C456' }} />}
  />
);

export const KundenAnalyse = () => (
  <PlaceholderPage
    title="Kundenanalyse"
    subtitle="Tiefgreifende Einblicke in Ihre Kundenbasis"
    description="Verstehen Sie Ihre Kunden besser durch datengetriebene Analysen."
    expectedDate="Q2 2025"
    features={[
      'Kundensegmentierung',
      'Verhaltensanalysen',
      'Churn-Prognosen',
    ]}
    icon={<GroupIcon sx={{ fontSize: 80, color: '#94C456' }} />}
  />
);

export const AktivitaetsberBerichte = () => (
  <PlaceholderPage
    title="Aktivitätsberichte"
    subtitle="Messen Sie die Effektivität Ihrer Vertriebsaktivitäten"
    description="Detaillierte Berichte über alle Vertriebsaktivitäten und deren Erfolg."
    expectedDate="Q2 2025"
    features={[
      'Aktivitäts-Metriken',
      'Team-Performance',
      'Conversion-Analysen',
    ]}
    icon={<AssessmentIcon sx={{ fontSize: 80, color: '#94C456' }} />}
  />
);

// Einstellungen
export const Einstellungen = () => (
  <PlaceholderPage
    title="Einstellungen"
    subtitle="Passen Sie FreshPlan an Ihre Bedürfnisse an"
    description="Konfigurieren Sie Systemeinstellungen, Benachrichtigungen und mehr."
    expectedDate="Q1 2025"
    features={[
      'Persönliche Einstellungen',
      'Benachrichtigungen',
      'Integrationen',
    ]}
    icon={<SettingsIcon sx={{ fontSize: 80, color: '#94C456' }} />}
  />
);

// Admin
export const AdminSettings = () => (
  <PlaceholderPage
    title="System-Einstellungen"
    subtitle="Globale Systemkonfiguration für Administratoren"
    description="Verwalten Sie systemweite Einstellungen und Konfigurationen."
    expectedDate="Q2 2025"
    features={[
      'Globale Konfiguration',
      'Sicherheitseinstellungen',
      'Backup-Management',
    ]}
    icon={<SettingsIcon sx={{ fontSize: 80, color: '#004F7B' }} />}
  />
);

export const SystemLogs = () => (
  <PlaceholderPage
    title="System-Logs"
    subtitle="Überwachen Sie alle Systemaktivitäten"
    description="Echtzeit-Logs und historische Systemereignisse für Troubleshooting."
    expectedDate="Q2 2025"
    features={[
      'Echtzeit-Logs',
      'Log-Filterung',
      'Export-Funktionen',
    ]}
    icon={<StorageIcon sx={{ fontSize: 80, color: '#004F7B' }} />}
  />
);

export const ComplianceReports = () => (
  <PlaceholderPage
    title="Compliance Reports"
    subtitle="Erfüllen Sie alle regulatorischen Anforderungen"
    description="Automatische Generierung von Compliance- und Audit-Berichten."
    expectedDate="Q3 2025"
    features={[
      'DSGVO-Berichte',
      'Audit-Trails',
      'Automatische Reports',
    ]}
    icon={<ArticleIcon sx={{ fontSize: 80, color: '#004F7B' }} />}
  />
);

export const TooltipsVerwalten = () => (
  <PlaceholderPage
    title="Tooltips verwalten"
    subtitle="Konfigurieren Sie kontextuelle Hilfe-Tooltips"
    description="Erstellen und bearbeiten Sie Tooltips für bessere Benutzerführung."
    expectedDate="Q2 2025"
    features={[
      'Tooltip-Editor',
      'Kontext-Regeln',
      'A/B-Testing',
    ]}
    icon={<EditNoteIcon sx={{ fontSize: 80, color: '#94C456' }} />}
  />
);

export const TourenErstellen = () => (
  <PlaceholderPage
    title="Touren erstellen"
    subtitle="Gestalten Sie interaktive Produkttouren"
    description="Führen Sie neue Benutzer mit geführten Touren durch die Anwendung."
    expectedDate="Q2 2025"
    features={[
      'Tour-Designer',
      'Schritt-für-Schritt Editor',
      'Benutzer-Segmentierung',
    ]}
    icon={<RouteIcon sx={{ fontSize: 80, color: '#94C456' }} />}
  />
);

export const HelpAnalytics = () => (
  <PlaceholderPage
    title="Help Analytics"
    subtitle="Verstehen Sie, wie Benutzer das Hilfe-System nutzen"
    description="Detaillierte Analysen zur Nutzung und Effektivität des Hilfe-Systems."
    expectedDate="Q3 2025"
    features={[
      'Nutzungs-Statistiken',
      'Effektivitäts-Metriken',
      'Verbesserungs-Vorschläge',
    ]}
    icon={<InsightsIcon sx={{ fontSize: 80, color: '#94C456' }} />}
  />
);

// Hilfe-Center
export const ErsteSchritte = () => (
  <PlaceholderPage
    title="Erste Schritte"
    subtitle="Der perfekte Start mit FreshPlan"
    description="Schritt-für-Schritt Anleitungen für einen erfolgreichen Start."
    expectedDate="Verfügbar"
    features={[
      'Quick-Start Guide',
      'Video-Tutorials',
      'Best Practices',
    ]}
    icon={<SchoolIcon sx={{ fontSize: 80, color: '#94C456' }} />}
  />
);

export const Handbuecher = () => (
  <PlaceholderPage
    title="Handbücher"
    subtitle="Umfassende Dokumentation aller Funktionen"
    description="Detaillierte Anleitungen und Referenzmaterial für alle FreshPlan-Features."
    expectedDate="Kontinuierlich erweitert"
    features={[
      'Feature-Dokumentation',
      'Use-Case Beispiele',
      'Troubleshooting Guides',
    ]}
    icon={<LibraryBooksIcon sx={{ fontSize: 80, color: '#004F7B' }} />}
  />
);

export const VideoTutorials = () => (
  <PlaceholderPage
    title="Video-Tutorials"
    subtitle="Lernen Sie FreshPlan visuell kennen"
    description="Professionelle Video-Anleitungen für effizientes Arbeiten."
    expectedDate="Q1 2025"
    features={[
      'Grundlagen-Videos',
      'Feature-Deep-Dives',
      'Tips & Tricks',
    ]}
    icon={<VideoLibraryIcon sx={{ fontSize: 80, color: '#94C456' }} />}
  />
);

export const FAQ = () => (
  <PlaceholderPage
    title="Häufige Fragen"
    subtitle="Schnelle Antworten auf häufige Fragen"
    description="Finden Sie Antworten auf die am häufigsten gestellten Fragen."
    expectedDate="Verfügbar"
    features={[
      'Kategorisierte FAQs',
      'Suchfunktion',
      'Community-Antworten',
    ]}
    icon={<QuizIcon sx={{ fontSize: 80, color: '#004F7B' }} />}
  />
);

export const Support = () => (
  <PlaceholderPage
    title="Support kontaktieren"
    subtitle="Wir sind für Sie da"
    description="Erreichen Sie unser Support-Team für persönliche Unterstützung."
    expectedDate="Verfügbar"
    features={[
      'Ticket-System',
      'Live-Chat',
      'Telefon-Support',
    ]}
    icon={<SupportAgentIcon sx={{ fontSize: 80, color: '#94C456' }} />}
  />
);

// Weitere Einstellungen
export const MeinProfil = () => (
  <PlaceholderPage
    title="Mein Profil"
    subtitle="Verwalten Sie Ihre persönlichen Informationen"
    description="Bearbeiten Sie Ihr Profil, Passwort und persönliche Einstellungen."
    expectedDate="Q1 2025"
    features={[
      'Profil-Bearbeitung',
      'Avatar-Upload',
      'Passwort-Verwaltung',
      'Zwei-Faktor-Authentifizierung',
    ]}
    icon={<AccountCircleIcon sx={{ fontSize: 80, color: '#004F7B' }} />}
  />
);

export const Benachrichtigungen = () => (
  <PlaceholderPage
    title="Benachrichtigungen"
    subtitle="Steuern Sie, wie und wann Sie informiert werden"
    description="Konfigurieren Sie E-Mail-, Push- und In-App-Benachrichtigungen."
    expectedDate="Q1 2025"
    features={[
      'Benachrichtigungs-Typen',
      'Zeitplanung',
      'Kanal-Präferenzen',
      'Stummschaltung',
    ]}
    icon={<NotificationsIcon sx={{ fontSize: 80, color: '#004F7B' }} />}
  />
);

export const Darstellung = () => (
  <PlaceholderPage
    title="Darstellung"
    subtitle="Passen Sie das Erscheinungsbild an"
    description="Themes, Layouts und visuelle Präferenzen nach Ihrem Geschmack."
    expectedDate="Q2 2025"
    features={[
      'Dark/Light Mode',
      'Farbschema-Anpassung',
      'Layout-Optionen',
      'Schriftgröße',
    ]}
    icon={<PaletteIcon sx={{ fontSize: 80, color: '#94C456' }} />}
  />
);

export const Sicherheit = () => (
  <PlaceholderPage
    title="Sicherheit"
    subtitle="Schützen Sie Ihr Konto"
    description="Erweiterte Sicherheitseinstellungen und Zugriffskontrolle."
    expectedDate="Q1 2025"
    features={[
      'Login-Historie',
      'Aktive Sitzungen',
      'API-Schlüssel',
      'Berechtigungen',
    ]}
    icon={<SecurityIcon sx={{ fontSize: 80, color: '#004F7B' }} />}
  />
);

// Admin-spezifische Placeholders
export const Performance = () => (
  <PlaceholderPage
    title="Performance-Monitoring"
    subtitle="System-Performance im Blick"
    description="Überwachen Sie Ladezeiten, Ressourcennutzung und Engpässe."
    expectedDate="Q2 2025"
    features={[
      'Real-time Metriken',
      'Performance-Trends',
      'Bottleneck-Analyse',
      'Optimierungs-Vorschläge',
    ]}
    icon={<SpeedIcon sx={{ fontSize: 80, color: '#004F7B' }} />}
  />
);

export const BackupRecovery = () => (
  <PlaceholderPage
    title="Backup & Recovery"
    subtitle="Datensicherung und Wiederherstellung"
    description="Verwalten Sie Backups und Disaster Recovery Prozesse."
    expectedDate="Q2 2025"
    features={[
      'Automatische Backups',
      'Manuelle Snapshots',
      'Recovery-Points',
      'Disaster Recovery Plan',
    ]}
    icon={<BackupIcon sx={{ fontSize: 80, color: '#004F7B' }} />}
  />
);

export const SystemVerwaltung = () => (
  <PlaceholderPage
    title="System-Verwaltung"
    subtitle="Zentrale Systemadministration"
    description="Verwalten Sie alle wichtigen Systemkomponenten an einem Ort."
    expectedDate="Q2 2025"
    features={[
      'Service-Status',
      'Konfiguration',
      'Wartungsmodus',
      'System-Updates',
    ]}
    icon={<DashboardIcon sx={{ fontSize: 80, color: '#004F7B' }} />}
  />
);

// Re-export from other files
export * from './kommunikation';
export * from './integrationen';