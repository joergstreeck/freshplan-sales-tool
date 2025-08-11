/**
 * API Request/Response Types
 *
 * Type definitions for all API endpoints in the Customer Management system.
 * Based on the REST API specification.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/03-rest-api.md
 */

import type {
  CustomerWithFields,
  CustomerListItem,
  CustomerSearchCriteria,
} from './customer.types';
import type {
  LocationWithFields,
  DetailedLocation,
  DetailedLocationBatch,
} from './location.types';
import type { FieldValue } from './field.types';
import { EntityType } from './field.types';

/**
 * Generic API response wrapper
 */
export interface ApiResponse<T> {
  /** Response data */
  data: T;
  /** Success status */
  success: boolean;
  /** Optional message */
  message?: string;
  /** Timestamp of response */
  timestamp: string;
}

/**
 * Paginated response wrapper
 */
export interface PaginatedResponse<T> {
  /** Page data */
  content: T[];
  /** Current page (0-based) */
  page: number;
  /** Page size */
  size: number;
  /** Total elements */
  totalElements: number;
  /** Total pages */
  totalPages: number;
  /** Is first page */
  first: boolean;
  /** Is last page */
  last: boolean;
}

/**
 * Error response from API
 */
export interface ApiError {
  /** HTTP status code */
  status: number;
  /** Error code for client handling */
  code: string;
  /** Human-readable error message */
  message: string;
  /** Field-specific errors for validation */
  fieldErrors?: Record<string, string[]>;
  /** Timestamp */
  timestamp: string;
}

// ===== Customer API Types =====

/**
 * Create customer draft request
 * Empty body - user context from auth
 */
export type CreateCustomerDraftRequest = Record<string, never>; // Empty body - user ID comes from security context

/**
 * Update customer draft request
 */
export interface UpdateCustomerDraftRequest {
  /** Field values to update */
  fieldValues: Record<string, unknown>;
}

/**
 * Finalize customer draft request
 */
export interface FinalizeCustomerDraftRequest {
  /** Final field values before activation */
  fieldValues?: Record<string, unknown>;
  /** Optional comment for audit trail */
  comment?: string;
}

/**
 * Customer draft response
 */
export interface CustomerDraftResponse extends CustomerWithFields {
  /** Last auto-save timestamp */
  lastSaved?: string;
  /** Validation warnings (non-blocking) */
  warnings?: string[];
}

/**
 * Customer search request
 */
export type CustomerSearchRequest = CustomerSearchCriteria; // Inherits all search criteria

/**
 * Customer search response
 */
export interface CustomerSearchResponse extends PaginatedResponse<CustomerListItem> {
  /** Applied filters for UI state */
  appliedFilters: CustomerSearchCriteria;
}

// ===== Location API Types =====

/**
 * Create location request
 */
export interface CreateLocationRequest {
  /** Customer ID */
  customerId: string;
  /** Field values for location */
  fieldValues: Record<string, unknown>;
}

/**
 * Update location request
 */
export interface UpdateLocationRequest {
  /** Updated field values */
  fieldValues: Record<string, unknown>;
}

/**
 * Location response
 */
export interface LocationResponse extends LocationWithFields {
  /** Associated detailed locations */
  detailedLocations?: DetailedLocation[];
}

// ===== Field Value API Types =====

/**
 * Bulk update field values request
 */
export interface BulkUpdateFieldValuesRequest {
  /** Entity ID */
  entityId: string;
  /** Entity type */
  entityType: EntityType;
  /** Field values to update */
  fieldValues: Record<string, unknown>;
}

/**
 * Field value response
 */
export interface FieldValueResponse extends FieldValue {
  /** Resolved field definition for UI */
  fieldDefinition?: {
    label: string;
    fieldType: string;
  };
}

// ===== Field Definition API Types =====

/**
 * Get field definitions request
 */
export interface GetFieldDefinitionsRequest {
  /** Entity type filter */
  entityType: EntityType;
  /** Industry filter */
  industry?: string;
  /** Include inactive fields */
  includeInactive?: boolean;
}

/**
 * Field definitions response
 */
export interface FieldDefinitionsResponse {
  /** Base fields */
  baseFields: FieldValue[];
  /** Industry-specific fields */
  industryFields: Record<string, FieldValue[]>;
}

// ===== Import/Export API Types =====

/**
 * Customer export request
 */
export interface CustomerExportRequest {
  /** Export format */
  format: 'csv' | 'excel' | 'json';
  /** Filter criteria */
  criteria?: CustomerSearchCriteria;
  /** Fields to include */
  includeFields?: string[];
  /** Include locations */
  includeLocations?: boolean;
}

/**
 * Customer import request
 */
export interface CustomerImportRequest {
  /** Import format */
  format: 'csv' | 'excel' | 'json';
  /** File content (base64) */
  fileContent: string;
  /** Import mode */
  mode: 'create' | 'update' | 'upsert';
  /** Field mapping */
  fieldMapping?: Record<string, string>;
}

/**
 * Import result response
 */
export interface ImportResultResponse {
  /** Total rows processed */
  totalRows: number;
  /** Successfully imported */
  successCount: number;
  /** Failed imports */
  errorCount: number;
  /** Detailed errors */
  errors?: Array<{
    row: number;
    field?: string;
    message: string;
  }>;
}
