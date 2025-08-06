import React from 'react';
import DashboardIcon from '@mui/icons-material/Dashboard';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import PeopleIcon from '@mui/icons-material/People';
import AssessmentIcon from '@mui/icons-material/Assessment';
import SettingsIcon from '@mui/icons-material/Settings';

// Temporär: Direkte Type-Definition um Import-Probleme zu umgehen
interface NavigationSubItem {
  label: string;
  path: string;
  permissions?: string[];
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
        path: '/kundenmanagement/liste',
      },
      {
        label: 'Neuer Kunde',
        path: '/kundenmanagement/neu',
      },
      {
        label: 'Verkaufschancen',
        path: '/kundenmanagement/opportunities',
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
];