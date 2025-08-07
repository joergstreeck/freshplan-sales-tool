/**
 * Customer Entity Types
 *
 * Core types for customer entities in the Field-Based Architecture.
 * Customer data is stored as dynamic field values, not fixed properties.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/01-entities.md
 */

/**
 * Customer lifecycle status
 */
export enum CustomerStatus {
  /** Initial draft state during wizard flow */
  DRAFT = 'DRAFT',
  /** Active customer after wizard completion */
  ACTIVE = 'ACTIVE',
  /** Temporarily inactive customer */
  INACTIVE = 'INACTIVE',
  /** Soft-deleted customer (for DSGVO compliance) */
  DELETED = 'DELETED',
}

/**
 * Core customer entity - minimal fixed properties
 * All business data is stored in field values
 */
export interface Customer {
  /** UUID primary key */
  id: string;
  /** Current lifecycle status */
  status: CustomerStatus;
  /** ISO timestamp of creation */
  createdAt: string;
  /** User ID who created the draft */
  createdBy: string;
  /** ISO timestamp when draft was finalized */
  finalizedAt?: string;
  /** User ID who finalized the draft */
  finalizedBy?: string;
  /** Audit timestamps */
  updatedAt: string;
  updatedBy?: string;
}

/**
 * Customer with resolved field values
 * Used for API responses and UI display
 */
export interface CustomerWithFields extends Customer {
  /** Resolved field values from field_values table */
  fields: Record<string, any>;
  /** Number of associated locations (if chain customer) */
  locationCount?: number;
}

/**
 * Customer list item for overview pages
 * Optimized projection with key fields
 */
export interface CustomerListItem {
  id: string;
  status: CustomerStatus;
  /** Resolved from field: companyName */
  companyName: string;
  /** Resolved from field: industry */
  industry?: string;
  /** Resolved from field: city */
  city?: string;
  /** Resolved from field: contactName */
  contactName?: string;
  /** Resolved from field: contactEmail */
  contactEmail?: string;
  /** Number of locations if chain customer */
  locationCount?: number;
  createdAt: string;
  updatedAt: string;
}

/**
 * Customer search/filter criteria
 */
export interface CustomerSearchCriteria {
  /** Text search across multiple fields */
  searchTerm?: string;
  /** Filter by status */
  status?: CustomerStatus[];
  /** Filter by industry */
  industry?: string[];
  /** Filter by chain customer */
  isChainCustomer?: boolean;
  /** Filter by city */
  city?: string;
  /** Date range filters */
  createdAfter?: string;
  createdBefore?: string;
  /** Pagination */
  page?: number;
  pageSize?: number;
  /** Sort field and order */
  sortBy?: 'companyName' | 'createdAt' | 'updatedAt';
  sortOrder?: 'asc' | 'desc';
}
