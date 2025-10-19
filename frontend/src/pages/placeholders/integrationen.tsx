import React from 'react';
import { PlaceholderPage } from '../PlaceholderPage';
import SmartToyIcon from '@mui/icons-material/SmartToy';
import InventoryIcon from '@mui/icons-material/Inventory';
import EmailIcon from '@mui/icons-material/Email';
import PaymentIcon from '@mui/icons-material/Payment';
import WebhookIcon from '@mui/icons-material/Webhook';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import SpeedIcon from '@mui/icons-material/Speed';
import BackupIcon from '@mui/icons-material/Backup';

// Admin Integrationen
export const KIAnbindungen = () => (
  <PlaceholderPage
    title="KI-Anbindungen"
    subtitle="Künstliche Intelligenz für smartere Prozesse"
    description="Integrieren Sie ChatGPT, Claude und andere KI-Services für automatisierte Textgenerierung und intelligente Assistenz."
    expectedDate="Q2 2025"
    features={[
      'ChatGPT Integration für Angebotserstellung',
      'Claude API für Datenanalyse',
      'Automatische E-Mail-Antworten mit KI',
    ]}
    icon={<SmartToyIcon sx={{ fontSize: 80, color: 'primary.main' }} />}
  />
);

export const XentralIntegration = () => (
  <PlaceholderPage
    title="Xentral Integration"
    subtitle="Nahtlose Verbindung zu Ihrem ERP-System"
    description="Synchronisieren Sie Kunden, Aufträge und Produkte automatisch mit Xentral."
    expectedDate="Q1 2025"
    features={[
      'Bidirektionale Datensynchronisation',
      'Automatischer Auftragsimport',
      'Lagerbestands-Abgleich',
    ]}
    icon={<InventoryIcon sx={{ fontSize: 80, color: 'secondary.main' }} />}
  />
);

export const EmailServices = () => (
  <PlaceholderPage
    title="E-Mail Services"
    subtitle="Professionelle E-Mail-Integration"
    description="Verbinden Sie SMTP-Server, Outlook oder Gmail für nahtlose E-Mail-Kommunikation."
    expectedDate="Q1 2025"
    features={['SMTP-Server Konfiguration', 'Microsoft 365 Integration', 'Gmail API Anbindung']}
    icon={<EmailIcon sx={{ fontSize: 80, color: 'secondary.main' }} />}
  />
);

export const PaymentProvider = () => (
  <PlaceholderPage
    title="Payment Provider"
    subtitle="Zahlungsabwicklung direkt in FreshPlan"
    description="Integrieren Sie Stripe, PayPal und andere Zahlungsanbieter."
    expectedDate="Q3 2025"
    features={['Stripe Integration', 'PayPal Checkout', 'SEPA-Lastschrift']}
    icon={<PaymentIcon sx={{ fontSize: 80, color: 'primary.main' }} />}
  />
);

export const Webhooks = () => (
  <PlaceholderPage
    title="Webhooks"
    subtitle="Event-basierte Integrationen"
    description="Konfigurieren Sie Webhooks für Echtzeit-Benachrichtigungen an externe Systeme."
    expectedDate="Q2 2025"
    features={['Webhook-Endpunkte verwalten', 'Event-Filter konfigurieren', 'Retry-Mechanismen']}
    icon={<WebhookIcon sx={{ fontSize: 80, color: 'secondary.main' }} />}
  />
);

export const NeueIntegration = () => (
  <PlaceholderPage
    title="Neue Integration hinzufügen"
    subtitle="Erweitern Sie FreshPlan mit neuen Anbindungen"
    description="Assistent zum Hinzufügen und Konfigurieren neuer Integrationen."
    expectedDate="Q2 2025"
    features={['Integration-Wizard', 'OAuth 2.0 Setup', 'API-Key Management']}
    icon={<AddCircleIcon sx={{ fontSize: 80, color: 'primary.main' }} />}
  />
);

// Admin System
export const Performance = () => (
  <PlaceholderPage
    title="System Performance"
    subtitle="Überwachen Sie die Systemleistung in Echtzeit"
    description="Detaillierte Performance-Metriken und Optimierungsvorschläge."
    expectedDate="Q2 2025"
    features={['Response-Time Monitoring', 'Database Query Analyzer', 'Resource-Auslastung']}
    icon={<SpeedIcon sx={{ fontSize: 80, color: 'secondary.main' }} />}
  />
);

export const BackupRecovery = () => (
  <PlaceholderPage
    title="Backup & Recovery"
    subtitle="Sichern und Wiederherstellen Ihrer Daten"
    description="Automatische Backups und schnelle Wiederherstellung im Notfall."
    expectedDate="Q2 2025"
    features={['Automatische Backups', 'Point-in-Time Recovery', 'Backup-Verschlüsselung']}
    icon={<BackupIcon sx={{ fontSize: 80, color: 'secondary.main' }} />}
  />
);
