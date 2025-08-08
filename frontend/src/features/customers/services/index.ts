/**
 * Customer Services Export
 *
 * Zentrale Exportstelle für alle Customer Management Services.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/04-api-integration.md
 */

// API Clients
export { apiClient } from './api-client';
export { customerApi } from './customerApi';
export { fieldDefinitionApi } from './fieldDefinitionApi';
export { locationApi } from './locationApi';

// React Query Hooks
export {
  // Query Keys
  queryKeys,

  // Customer Hooks
  useCreateCustomerDraft,
  useCustomerDraft,
  useUpdateCustomerDraft,
  useFinalizeCustomerDraft,
  useSearchCustomers,
  useCustomer,
  useUpdateCustomer,
  useRecentCustomers,
  useCustomerStatistics,

  // Location Hooks
  useCustomerLocations,
  useCreateLocation,
  useUpdateLocation,

  // Field Definition Hooks
  useFieldDefinitions,
  useIndustries,
  usePreloadFieldDefinitions,
} from './hooks';

// Re-export types for convenience
export type { RequestConfig } from './api-client';
