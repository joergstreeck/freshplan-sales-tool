/**
 * FC-005 Customer Management - Type Definitions
 *
 * @module features/customers/types
 * @description Zentrale Type-Definitionen für das Field-Based Customer Management System.
 *              Alle Types basieren auf der Field Catalog Struktur und nutzen die
 *              konsolidierten Base-Types aus der zentralen Type-Bibliothek.
 *
 * @see {@link ../../../types/consolidated.types} - Zentrale Type-Definitionen
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/data/fieldCatalog.json
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/README.md
 * 
 * @since 2.0.0
 */

// Import consolidated base types
export {
  BaseEntity,
  SoftDeletable,
  PaginatedResponse,
  ApiError,
  ApiSuccess,
  FieldStatus,
  ValidationResult,
  LoadingState,
  ComponentSize,
  ComponentVariant,
  // Type Guards
  isBaseEntity,
  isApiError,
  isPaginatedResponse,
  // Utility Types
  type DeepPartial,
  type RequireFields,
  type OmitMultiple,
  type NonNullableFields,
} from '../../../types/consolidated.types';

// Re-export specific customer types
export * from './customer.types';
export * from './field.types';
export * from './api.types';
export * from './location.types';
