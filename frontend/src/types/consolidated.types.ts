/**
 * Consolidated Type Definitions
 * 
 * @module types/consolidated
 * @description Zentrale Type-Definitionen für das gesamte Frontend.
 *              Vermeidet Redundanzen und stellt Single Source of Truth sicher.
 * 
 * @since 2.0.0
 */

// ============================================================================
// CUSTOMER TYPES
// ============================================================================

/**
 * Customer Status Enum - Unified across all modules
 */
export enum CustomerStatus {
  // Legacy Status Values (für Backward Compatibility)
  LEAD = 'LEAD',
  PROSPECT = 'PROSPECT',
  AKTIV = 'AKTIV',
  RISIKO = 'RISIKO',
  INAKTIV = 'INAKTIV',
  ARCHIVIERT = 'ARCHIVIERT',
  
  // Field-Based Architecture Status
  DRAFT = 'DRAFT',
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  DELETED = 'DELETED',
}

/**
 * Customer Type Classification
 */
export enum CustomerType {
  UNTERNEHMEN = 'UNTERNEHMEN',
  EINZELUNTERNEHMEN = 'EINZELUNTERNEHMEN',
  FILIALE = 'FILIALE',
  OEFFENTLICH = 'OEFFENTLICH',
  SONSTIGES = 'SONSTIGES',
}

/**
 * Industry Classification
 */
export enum Industry {
  HOTEL = 'HOTEL',
  RESTAURANT = 'RESTAURANT',
  CAFE = 'CAFE',
  BAR = 'BAR',
  KANTINE = 'KANTINE',
  CATERING = 'CATERING',
  BILDUNG = 'BILDUNG',
  GESUNDHEIT = 'GESUNDHEIT',
  EINZELHANDEL = 'EINZELHANDEL',
  SONSTIGES = 'SONSTIGES',
}

// ============================================================================
// SHARED BASE TYPES
// ============================================================================

/**
 * Base Entity Interface
 * Alle Entities sollten diese Properties haben
 */
export interface BaseEntity {
  id: string;
  createdAt: string;
  createdBy: string;
  updatedAt?: string;
  updatedBy?: string;
}

/**
 * Soft Delete Support
 */
export interface SoftDeletable {
  isDeleted: boolean;
  deletedAt?: string;
  deletedBy?: string;
}

/**
 * Paginated Response Structure
 */
export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

// ============================================================================
// API RESPONSE TYPES
// ============================================================================

/**
 * Standard API Error Response
 */
export interface ApiError {
  error: string;
  message: string;
  timestamp: string;
  path?: string;
  status?: number;
}

/**
 * Standard Success Response
 */
export interface ApiSuccess<T = unknown> {
  data: T;
  message?: string;
  timestamp: string;
}

// ============================================================================
// FORM & VALIDATION TYPES
// ============================================================================

/**
 * Form Field Status
 */
export enum FieldStatus {
  PRISTINE = 'PRISTINE',
  TOUCHED = 'TOUCHED',
  DIRTY = 'DIRTY',
  VALID = 'VALID',
  INVALID = 'INVALID',
}

/**
 * Validation Result
 */
export interface ValidationResult {
  isValid: boolean;
  errors?: Record<string, string>;
  warnings?: Record<string, string>;
}

// ============================================================================
// UI COMPONENT TYPES
// ============================================================================

/**
 * Loading States for Components
 */
export enum LoadingState {
  IDLE = 'IDLE',
  LOADING = 'LOADING',
  SUCCESS = 'SUCCESS',
  ERROR = 'ERROR',
}

/**
 * Standard Component Size
 */
export type ComponentSize = 'small' | 'medium' | 'large';

/**
 * Standard Component Variant
 */
export type ComponentVariant = 'primary' | 'secondary' | 'tertiary' | 'danger' | 'success';

// ============================================================================
// PERMISSION TYPES
// ============================================================================

/**
 * Permission Code Format
 */
export type PermissionCode = `${string}:${string}`;

/**
 * User Role
 */
export enum UserRole {
  ADMIN = 'admin',
  MANAGER = 'manager',
  SALES = 'sales',
  VIEWER = 'viewer',
}

// ============================================================================
// TYPE GUARDS
// ============================================================================

/**
 * Type Guard für BaseEntity
 */
export function isBaseEntity(obj: unknown): obj is BaseEntity {
  return (
    typeof obj === 'object' &&
    obj !== null &&
    'id' in obj &&
    'createdAt' in obj &&
    'createdBy' in obj
  );
}

/**
 * Type Guard für ApiError
 */
export function isApiError(obj: unknown): obj is ApiError {
  return (
    typeof obj === 'object' &&
    obj !== null &&
    'error' in obj &&
    'message' in obj
  );
}

/**
 * Type Guard für PaginatedResponse
 */
export function isPaginatedResponse<T>(obj: unknown): obj is PaginatedResponse<T> {
  return (
    typeof obj === 'object' &&
    obj !== null &&
    'content' in obj &&
    Array.isArray((obj as any).content) &&
    'totalElements' in obj
  );
}

// ============================================================================
// UTILITY TYPES
// ============================================================================

/**
 * Makes all properties optional recursively
 */
export type DeepPartial<T> = {
  [P in keyof T]?: T[P] extends object ? DeepPartial<T[P]> : T[P];
};

/**
 * Makes specific properties required
 */
export type RequireFields<T, K extends keyof T> = T & Required<Pick<T, K>>;

/**
 * Omit multiple properties
 */
export type OmitMultiple<T, K extends keyof T> = Pick<T, Exclude<keyof T, K>>;

/**
 * Extract non-nullable type
 */
export type NonNullableFields<T> = {
  [P in keyof T]-?: NonNullable<T[P]>;
};

// ============================================================================
// RE-EXPORTS für Backward Compatibility
// ============================================================================

// Diese Exports stellen sicher, dass bestehender Code weiter funktioniert
export type { Customer } from '../features/customers/types/customer.types';
export type { Contact } from '../features/customers/types/contact.types';
export type { FieldDefinition } from '../features/customers/types/field.types';