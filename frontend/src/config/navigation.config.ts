import React from 'react';
import DashboardIcon from '@mui/icons-material/Dashboard';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import PeopleIcon from '@mui/icons-material/People';
import AssessmentIcon from '@mui/icons-material/Assessment';
import SettingsIcon from '@mui/icons-material/Settings';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import ApiIcon from '@mui/icons-material/Api';
import BugReportIcon from '@mui/icons-material/BugReport';
import ChatIcon from '@mui/icons-material/Chat';
import IntegrationInstructionsIcon from '@mui/icons-material/IntegrationInstructions';
// import SecurityIcon from '@mui/icons-material/Security'; // Not used currently
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';

// Temporär: Direkte Type-Definition um Import-Probleme zu umgehen
interface NavigationSubItem {
  label: string;
  path?: string; // Optional für action-based items
  action?: string; // NEU für Modal-Trigger
  permissions?: string[];
  disabled?: boolean; // NEU für disabled items
  tooltip?: string; // NEU für Erklärung
  subItems?: NavigationSubItem[]; // NEU für verschachtelte Menüs
}

interface NavigationItemType {
  id: string;
  label: string;
  icon: React.ComponentType<React.SVGProps<SVGSVGElement>>;
  path: string;
  permissions?: string[];
  hasOwnPage?: boolean; // NEU: Gibt an, ob Hauptmenüpunkt eine eigene Seite hat
  subItems?: NavigationSubItem[];
}

export const navigationConfig: NavigationItemType[] = [
  {
    id: 'cockpit',
    label: 'Mein Cockpit',
    icon: DashboardIcon,
    path: '/cockpit',
    permissions: ['cockpit.view'],
  },
  {
    id: 'neukundengewinnung',
    label: 'Neukundengewinnung',
    icon: PersonAddIcon,
    path: '/lead-generation',
    permissions: ['customers.create'],
    hasOwnPage: true, // NEU: Hat eigenes Dashboard
    subItems: [
      {
        label: 'E-Mail Posteingang',
        path: '/lead-generation/inbox',
      },
      {
        label: 'Lead-Erfassung',
        path: '/lead-generation/leads',
      },
      {
        label: 'Kampagnen',
        path: '/lead-generation/campaigns',
      },
    ],
  },
  {
    id: 'kundenmanagement',
    label: 'Kundenmanagement',
    icon: PeopleIcon,
    path: '/customer-management',
    permissions: ['customers.view'],
    hasOwnPage: true, // NEU: Hat eigenes Dashboard
    subItems: [
      {
        label: 'Alle Kunden',
        path: '/customers', // Angepasst an tatsächliche Route
      },
      {
        label: 'Verkaufschancen',
        path: '/customer-management/opportunities', // Korrigierter Pfad zur tatsächlichen Route
      },
      {
        label: 'Aktivitäten',
        path: '/customer-management/activities',
      },
    ],
  },
  {
    id: 'berichte',
    label: 'Auswertungen',
    icon: AssessmentIcon,
    path: '/reports',
    permissions: ['reports.view'],
    hasOwnPage: true, // NEU: Hat eigenes Dashboard
    subItems: [
      {
        label: 'Umsatzübersicht',
        path: '/reports/revenue',
      },
      {
        label: 'Kundenanalyse',
        path: '/reports/customers',
      },
      {
        label: 'Aktivitätsberichte',
        path: '/reports/activities',
      },
    ],
  },
  {
    id: 'kommunikation',
    label: 'Kommunikation',
    icon: ChatIcon,
    path: '/communication',
    hasOwnPage: true, // NEU: Hat eigenes Dashboard
    subItems: [
      {
        label: 'Team-Chat',
        path: '/communication/chat',
      },
      {
        label: 'Ankündigungen',
        path: '/communication/announcements',
      },
      {
        label: 'Notizen',
        path: '/communication/notes',
      },
      {
        label: 'Interne Nachrichten',
        path: '/communication/messages',
      },
    ],
  },
  {
    id: 'einstellungen',
    label: 'Einstellungen',
    icon: SettingsIcon,
    path: '/settings',
    permissions: ['settings.view'],
    hasOwnPage: true, // NEU: Hat eigenes Dashboard
    subItems: [
      {
        label: 'Mein Profil',
        path: '/settings/profile',
      },
      {
        label: 'Benachrichtigungen',
        path: '/settings/notifications',
      },
      {
        label: 'Darstellung',
        path: '/settings/appearance',
      },
      {
        label: 'Sicherheit',
        path: '/settings/security',
        tooltip: 'Passwort ändern, 2FA, Session-Management',
      },
    ],
  },
  {
    id: 'hilfe',
    label: 'Hilfe & Support',
    icon: HelpOutlineIcon,
    path: '/help',
    hasOwnPage: true, // NEU: Hat eigenes Dashboard
    subItems: [
      {
        label: 'Erste Schritte',
        path: '/help/getting-started',
      },
      {
        label: 'Handbücher',
        path: '/help/manuals',
      },
      {
        label: 'Video-Tutorials',
        path: '/help/videos',
      },
      {
        label: 'Häufige Fragen',
        path: '/help/faq',
      },
      {
        label: 'Support kontaktieren',
        path: '/help/support',
      },
    ],
  },
  {
    id: 'admin',
    label: 'Administration',
    icon: AdminPanelSettingsIcon,
    path: '/admin',
    permissions: ['admin.view', 'auditor.view'],
    hasOwnPage: true, // NEU: Zeigt an, dass dieser Hauptmenüpunkt eine eigene Seite hat
    subItems: [
      {
        label: 'Audit Dashboard',
        path: '/admin/audit',
        permissions: ['admin.view', 'auditor.view'],
      },
      {
        label: 'Benutzerverwaltung',
        path: '/admin/users',
        permissions: ['admin.view'],
      },
      {
        label: 'System',
        path: '/admin/system',
        permissions: ['admin.view'],
        hasOwnPage: true, // NEU: Hat eigenes Dashboard
        subItems: [
          {
            label: 'API Status',
            path: '/admin/system/api-test',
            permissions: ['admin.view'],
          },
          {
            label: 'System-Logs',
            path: '/admin/system/logs',
            permissions: ['admin.view'],
          },
          {
            label: 'Performance',
            path: '/admin/system/performance',
            permissions: ['admin.view'],
          },
          {
            label: 'Backup & Recovery',
            path: '/admin/system/backup',
            permissions: ['admin.view'],
          },
        ],
      },
      {
        label: 'Integrationen',
        path: '/admin/integrations',
        permissions: ['admin.view'],
        hasOwnPage: true, // NEU: Hat eigenes Dashboard
        subItems: [
          {
            label: 'KI-Anbindungen',
            path: '/admin/integrations/ai',
            permissions: ['admin.view'],
            tooltip: 'ChatGPT, Claude, andere KI-Services',
          },
          {
            label: 'Xentral',
            path: '/admin/integrations/xentral',
            permissions: ['admin.view'],
          },
          {
            label: 'E-Mail Services',
            path: '/admin/integrations/email',
            permissions: ['admin.view'],
            tooltip: 'SMTP, Outlook, Gmail Integration',
          },
          {
            label: 'Payment Provider',
            path: '/admin/integrations/payment',
            permissions: ['admin.view'],
          },
          {
            label: 'Webhooks',
            path: '/admin/integrations/webhooks',
            permissions: ['admin.view'],
          },
          {
            label: 'Neue Integration',
            path: '/admin/integrations/new',
            permissions: ['admin.view'],
          },
        ],
      },
      {
        label: 'Hilfe-Konfiguration',
        path: '/admin/help',
        permissions: ['admin.view'],
        hasOwnPage: true, // NEU: Hat eigenes Dashboard
        subItems: [
          {
            label: 'Hilfe-System Demo',
            path: '/admin/help/demo',
            permissions: ['admin.view'],
          },
          {
            label: 'Tooltips verwalten',
            path: '/admin/help/tooltips',
            permissions: ['admin.view'],
          },
          {
            label: 'Touren erstellen',
            path: '/admin/help/tours',
            permissions: ['admin.view'],
          },
          {
            label: 'Analytics',
            path: '/admin/help/analytics',
            permissions: ['admin.view'],
          },
        ],
      },
      {
        label: 'Compliance Reports',
        path: '/admin/reports',
        permissions: ['admin.view', 'auditor.view', 'manager.view'],
      },
    ],
  },
];
