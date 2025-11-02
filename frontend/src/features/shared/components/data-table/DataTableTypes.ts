/**
 * Shared DataTable Types
 *
 * Generic type definitions for reusable table components.
 * Extracted during Migration M1 (Sprint 2.1.7.7)
 *
 * @module DataTableTypes
 * @since Sprint 2.1.7.7 (Migration M1)
 */

import { ReactNode } from 'react';

/**
 * Column configuration for DataTable
 *
 * @template T - The type of data being displayed
 */
export interface DataTableColumn<T = unknown> {
  /** Unique identifier for the column */
  id: string;

  /** Display label for column header */
  label: string;

  /** Field name in the data object (optional if using custom render) */
  field?: keyof T;

  /** Whether the column is visible */
  visible: boolean;

  /** Display order (lower = left) */
  order?: number;

  /** Text alignment */
  align?: 'left' | 'center' | 'right';

  /** Column width (CSS value) */
  width?: string | number;

  /** Whether this column is sortable */
  sortable?: boolean;

  /** Custom render function */
  render?: (row: T) => ReactNode;

  /** Header render function (for custom header content) */
  renderHeader?: () => ReactNode;
}

/**
 * Sort configuration
 */
export interface SortConfig {
  /** Field to sort by */
  field: string;

  /** Sort direction */
  direction: 'asc' | 'desc';
}

/**
 * Pagination configuration
 */
export interface PaginationConfig {
  /** Current page (0-indexed) */
  page: number;

  /** Rows per page */
  rowsPerPage: number;

  /** Total number of rows */
  totalRows: number;

  /** Available rows per page options */
  rowsPerPageOptions?: number[];
}

/**
 * DataTable Props (generic)
 */
export interface DataTableProps<T> {
  /** Data to display */
  data: T[];

  /** Column configuration */
  columns: DataTableColumn<T>[];

  /** Function to get unique ID for each row */
  getRowId: (row: T) => string;

  /** Row click handler */
  onRowClick?: (row: T) => void;

  /** Edit button handler */
  onEdit?: (row: T) => void;

  /** Delete button handler */
  onDelete?: (row: T) => void;

  /** Show action column */
  showActions?: boolean;

  /** Enable pagination */
  pagination?: boolean;

  /** Pagination config (controlled) */
  paginationConfig?: PaginationConfig;

  /** Pagination change handler */
  onPaginationChange?: (page: number, rowsPerPage: number) => void;

  /** Sort config (controlled) */
  sortConfig?: SortConfig;

  /** Sort change handler */
  onSortChange?: (sort: SortConfig) => void;

  /** Highlight new rows (created < 24h ago) */
  highlightNew?: boolean;

  /** Loading state */
  loading?: boolean;

  /** Empty state message */
  emptyMessage?: string;

  /** Table height (for virtualization) */
  height?: number;

  /** Row height (for virtualization) */
  rowHeight?: number;
}

/**
 * Filter configuration
 */
export interface FilterConfig {
  /** Text search */
  text?: string;

  /** Status filter */
  status?: string[];

  /** Industry filter */
  industry?: string[];

  /** Risk level filter */
  riskLevel?: string[];

  /** Has contacts filter */
  hasContacts?: boolean | null;

  /** Last contact days filter */
  lastContactDays?: number | null;

  /** Revenue range filter */
  revenueRange?: {
    min?: number | null;
    max?: number | null;
  } | null;

  /** Created days filter */
  createdDays?: number | null;

  /** Custom filters (extensible) */
  [key: string]: unknown;
}

/**
 * Filter definition (for FilterBar configuration)
 */
export interface FilterDefinition {
  /** Unique filter ID */
  id: string;

  /** Display label */
  label: string;

  /** Filter type */
  type: 'text' | 'select' | 'multiselect' | 'date' | 'daterange' | 'number' | 'numberrange';

  /** Options (for select/multiselect) */
  options?: Array<{
    value: string;
    label: string;
  }>;

  /** Default value */
  defaultValue?: unknown;

  /** Whether this filter is visible */
  visible?: boolean;

  /** Icon (optional) */
  icon?: string;
}
