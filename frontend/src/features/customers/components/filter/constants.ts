/**
 * Constants and translations for the Intelligent Filter Bar
 *
 * @module constants
 * @since FC-005 PR4
 */

// Import the correct CustomerStatus from the customer feature
import { CustomerStatus } from '../../../customer/types/customer.types';
import { RiskLevel } from '../../types/filter.types';

// Deutsche Übersetzungen
export const STATUS_LABELS: Record<CustomerStatus, string> = {
  [CustomerStatus.LEAD]: 'Lead',
  [CustomerStatus.PROSPECT]: 'Interessent',
  [CustomerStatus.AKTIV]: 'Aktiv',
  [CustomerStatus.RISIKO]: 'Risiko',
  [CustomerStatus.INAKTIV]: 'Inaktiv',
  [CustomerStatus.ARCHIVIERT]: 'Archiviert',
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
