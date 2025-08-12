/**
 * Constants and translations for the Intelligent Filter Bar
 * 
 * @module constants
 * @since FC-005 PR4
 */

import { CustomerStatus } from '../../types/customer.types';
import { RiskLevel } from '../../types/filter.types';

// Deutsche Übersetzungen
export const STATUS_LABELS: Record<CustomerStatus, string> = {
  [CustomerStatus.DRAFT]: 'Entwurf',
  [CustomerStatus.ACTIVE]: 'Aktiv',
  [CustomerStatus.INACTIVE]: 'Inaktiv',
  [CustomerStatus.DELETED]: 'Gelöscht',
};

export const RISK_LABELS: Record<RiskLevel, string> = {
  [RiskLevel.LOW]: 'Niedrig',
  [RiskLevel.MEDIUM]: 'Mittel',
  [RiskLevel.HIGH]: 'Hoch',
  [RiskLevel.CRITICAL]: 'Kritisch',
};

export const DEFAULT_COLUMNS = [
  'name',
  'status',
  'location',
  'riskLevel',
  'revenue',
  'lastContact',
];

export const COLUMN_LABELS: Record<string, string> = {
  name: 'Name',
  status: 'Status',
  location: 'Standort',
  revenue: 'Umsatz',
  riskLevel: 'Risiko',
  lastContact: 'Letzter Kontakt',
  created: 'Erstellt',
  modified: 'Geändert',
  contacts: 'Kontakte',
  opportunities: 'Chancen',
  tags: 'Tags',
};