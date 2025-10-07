/**
 * Context-based Configuration for Leads vs Customers
 *
 * Defines context-specific table columns and sort options
 *
 * @module contextConfig
 * @since Sprint 2.1.5 - Context-Prop Architecture
 */

import type { TableColumn } from '../../../customer/store/focusListStore';

export interface SortOption {
  field: string;
  label: string;
  icon?: string;
}

/**
 * Table columns for Leads context
 * Sprint 2.1.6 Phase 4: Added leadScore, changed industry→businessType
 */
export const LEADS_TABLE_COLUMNS: TableColumn[] = [
  { id: 'companyName', label: 'Lead', field: 'companyName', visible: true, order: 0 },
  { id: 'status', label: 'Status', field: 'status', visible: true, order: 1 },
  {
    id: 'businessType',
    label: 'Branche',
    field: 'businessType',
    visible: true,
    order: 2,
  },
  {
    id: 'leadScore',
    label: 'Score',
    field: 'leadScore',
    visible: true,
    order: 3,
    align: 'center',
  },
  {
    id: 'expectedAnnualVolume',
    label: 'Erwarteter Umsatz',
    field: 'expectedAnnualVolume',
    visible: true,
    order: 4,
    align: 'right',
  },
  {
    id: 'stage',
    label: 'Stage',
    field: 'stage',
    visible: false,
    order: 5,
    align: 'center',
  },
  {
    id: 'createdAt',
    label: 'Erstellt am',
    field: 'createdAt',
    visible: true,
    order: 6,
  },
  { id: 'assignedTo', label: 'Zugewiesen an', field: 'assignedTo', visible: false, order: 7 },
  { id: 'actions', label: 'Aktionen', field: 'actions', visible: true, order: 8, align: 'right' },
];

/**
 * Table columns for Customers context
 */
export const CUSTOMERS_TABLE_COLUMNS: TableColumn[] = [
  { id: 'companyName', label: 'Kunde', field: 'companyName', visible: true, order: 0 },
  {
    id: 'customerNumber',
    label: 'Kundennummer',
    field: 'customerNumber',
    visible: false,
    order: 1,
  },
  { id: 'status', label: 'Status', field: 'status', visible: true, order: 2 },
  {
    id: 'riskScore',
    label: 'Risiko',
    field: 'riskScore',
    visible: true,
    order: 3,
    align: 'center',
  },
  { id: 'industry', label: 'Branche', field: 'industry', visible: true, order: 4 },
  {
    id: 'expectedAnnualVolume',
    label: 'Jahresumsatz',
    field: 'expectedAnnualVolume',
    visible: false,
    order: 5,
    align: 'right',
  },
  {
    id: 'lastContactDate',
    label: 'Letzter Kontakt',
    field: 'lastContactDate',
    visible: false,
    order: 6,
  },
  { id: 'assignedTo', label: 'Betreuer', field: 'assignedTo', visible: false, order: 7 },
  { id: 'actions', label: 'Aktionen', field: 'actions', visible: true, order: 8, align: 'right' },
];

/**
 * Sort options for Leads context
 */
export const LEADS_SORT_OPTIONS: SortOption[] = [
  { field: 'companyName', label: 'Name', icon: '📋' },
  { field: 'status', label: 'Status', icon: '🔄' },
  { field: 'expectedAnnualVolume', label: 'Erwarteter Umsatz', icon: '💰' },
  { field: 'createdAt', label: 'Erstellt am', icon: '🌟' },
];

/**
 * Sort options for Customers context
 */
export const CUSTOMERS_SORT_OPTIONS: SortOption[] = [
  { field: 'companyName', label: 'Name', icon: '📋' },
  { field: 'status', label: 'Status', icon: '✅' },
  { field: 'expectedAnnualVolume', label: 'Umsatz', icon: '💰' },
  { field: 'riskScore', label: 'Risiko', icon: '🚨' },
  { field: 'lastContactDate', label: 'Letzter Kontakt', icon: '📞' },
];

/**
 * Get table columns for a specific context
 */
export function getTableColumnsForContext(context: 'customers' | 'leads'): TableColumn[] {
  return context === 'leads' ? LEADS_TABLE_COLUMNS : CUSTOMERS_TABLE_COLUMNS;
}

/**
 * Get sort options for a specific context
 */
export function getSortOptionsForContext(context: 'customers' | 'leads'): SortOption[] {
  return context === 'leads' ? LEADS_SORT_OPTIONS : CUSTOMERS_SORT_OPTIONS;
}
