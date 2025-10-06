/**
 * Filter Types for Intelligent Customer Filter Bar
 *
 * @module filter.types
 * @since FC-005 PR4
 */

import type { CustomerStatus } from './customer.types';

/**
 * Risk level for customers
 */
export enum RiskLevel {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL',
}

/**
 * Main filter configuration
 */
export interface FilterConfig {
  text?: string;
  status?: CustomerStatus[];
  /**
   * @deprecated Use businessType instead. Will be removed in future version.
   * @since 2.1.6 - Replaced by businessType field
   */
  industry?: string[];
  location?: string[];
  revenueRange?: { min: number | null; max: number | null } | null;
  riskLevel?: RiskLevel[];
  hasContacts?: boolean | null;
  lastContactDays?: number | null;
  tags?: string[];
  createdDays?: number | null; // Für "Neue Kunden" Filter
  customFields?: Record<string, unknown>;
}

/**
 * Sort configuration for columns
 */
export interface SortConfig {
  field: string;
  direction: 'asc' | 'desc';
  priority: number;
}

/**
 * Column visibility and configuration
 */
export interface ColumnConfig {
  id: string;
  label: string;
  visible: boolean;
  locked?: boolean;
  width?: number;
  align?: 'left' | 'center' | 'right';
}

/**
 * Saved filter set for reuse
 */
export interface SavedFilterSet {
  id: string;
  name: string;
  filters: FilterConfig;
  columns: ColumnConfig[];
  sort: SortConfig;
  createdAt: string;
  shared?: boolean;
  ownerId?: string;
}

/**
 * Filter combination operator
 */
export type FilterOperator = 'AND' | 'OR';

/**
 * Advanced filter configuration
 */
export interface AdvancedFilter {
  field: string;
  operator:
    | 'equals'
    | 'contains'
    | 'starts_with'
    | 'ends_with'
    | 'greater'
    | 'less'
    | 'between'
    | 'in'
    | 'not_in';
  value: unknown;
  caseSensitive?: boolean;
}

/**
 * Search options for universal search
 */
export interface SearchOptions {
  includeContacts?: boolean;
  includeInactive?: boolean;
  fuzzyMatch?: boolean;
  maxResults?: number;
}

/**
 * Query type detection
 */
export type QueryType = 'email' | 'phone' | 'customerNumber' | 'text';

/**
 * Search result with relevance scoring
 */
export interface SearchResult {
  type: 'customer' | 'contact';
  id: string;
  data: unknown;
  relevanceScore: number;
  matchedFields: string[];
}

/**
 * Combined search results
 */
export interface SearchResults {
  customers: SearchResult[];
  contacts: SearchResult[];
  totalCount: number;
  executionTime: number;
}
