import React from 'react';
import DashboardIcon from '@mui/icons-material/Dashboard';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import PeopleIcon from '@mui/icons-material/People';
import AssessmentIcon from '@mui/icons-material/Assessment';
import SettingsIcon from '@mui/icons-material/Settings';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import ApiIcon from '@mui/icons-material/Api';
import BugReportIcon from '@mui/icons-material/BugReport';
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
}

interface NavigationItemType {
  id: string;
  label: string;
  icon: React.ComponentType<React.SVGProps<SVGSVGElement>>;
  path: string;
  permissions?: string[];
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
    path: '/neukundengewinnung',
    permissions: ['customers.create'],
    subItems: [
      {
        label: 'E-Mail Posteingang',
        path: '/neukundengewinnung/posteingang',
      },
      {
        label: 'Lead-Erfassung',
        path: '/neukundengewinnung/leads',
        disabled: true, // Sprint 2: Noch nicht implementiert
        tooltip: 'Verfügbar in Phase 2 (FC-020 Lead Management)',
      },
      {
        label: 'Kampagnen',
        path: '/neukundengewinnung/kampagnen',
      },
    ],
  },
  {
    id: 'kundenmanagement',
    label: 'Kundenmanagement',
    icon: PeopleIcon,
    path: '/kundenmanagement',
    permissions: ['customers.view'],
    subItems: [
      {
        label: 'Alle Kunden',
        path: '/customers', // Angepasst an tatsächliche Route
      },
      {
        label: 'Neuer Kunde', // NEU für Sprint 2!
        action: 'OPEN_CUSTOMER_WIZARD', // Triggert Modal statt Navigation
      },
      {
        label: 'Verkaufschancen',
        path: '/kundenmanagement/opportunities', // Korrigierter Pfad zur tatsächlichen Route
      },
      {
        label: 'Aktivitäten',
        path: '/kundenmanagement/aktivitaeten',
      },
    ],
  },
  {
    id: 'berichte',
    label: 'Auswertungen',
    icon: AssessmentIcon,
    path: '/berichte',
    permissions: ['reports.view'],
    subItems: [
      {
        label: 'Umsatzübersicht',
        path: '/berichte/umsatz',
      },
      {
        label: 'Kundenanalyse',
        path: '/berichte/kunden',
      },
      {
        label: 'Aktivitätsberichte',
        path: '/berichte/aktivitaeten',
      },
    ],
  },
  {
    id: 'einstellungen',
    label: 'Einstellungen',
    icon: SettingsIcon,
    path: '/einstellungen',
    permissions: ['settings.view'],
  },
  {
    id: 'hilfe',
    label: 'Hilfe & Support',
    icon: HelpOutlineIcon,
    path: '/hilfe',
    subItems: [
      {
        label: '🚀 Erste Schritte',
        path: '/hilfe/erste-schritte',
      },
      {
        label: '📖 Handbücher',
        path: '/hilfe/handbuecher',
      },
      {
        label: '🎥 Video-Tutorials',
        path: '/hilfe/videos',
      },
      {
        label: '❓ Häufige Fragen',
        path: '/hilfe/faq',
      },
      {
        label: '💬 Support kontaktieren',
        path: '/hilfe/support',
      },
    ],
  },
  {
    id: 'admin',
    label: 'Administration',
    icon: AdminPanelSettingsIcon,
    path: '/admin',
    permissions: ['admin.view', 'auditor.view'],
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
        label: '🔧 System',
        path: '/admin/system',
        permissions: ['admin.view'],
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
            label: 'Einstellungen',
            path: '/admin/settings',
            permissions: ['admin.view'],
          },
        ],
      },
      {
        label: '📚 Hilfe-Konfiguration',
        path: '/admin/help',
        permissions: ['admin.view'],
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
