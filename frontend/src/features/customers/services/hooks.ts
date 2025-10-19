/**
 * React Query Hooks für Customer Management
 *
 * Bietet typsichere Hooks für alle API-Operationen mit:
 * - Automatisches Caching und Invalidierung
 * - Optimistic Updates
 * - Error Handling
 * - Loading States
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/04-api-integration.md
 */

import {
  useQuery,
  useMutation,
  useQueryClient,
  type UseQueryOptions,
  type UseMutationOptions,
} from '@tanstack/react-query';
import { customerApi } from './customerApi';
import { fieldDefinitionApi } from './fieldDefinitionApi';
import { locationApi } from './locationApi';
import type {
  CustomerWithFields,
  CustomerDraftResponse,
  CustomerSearchResponse,
  CustomerSearchRequest,
  UpdateCustomerDraftRequest,
  FinalizeCustomerDraftRequest,
  CreateLocationRequest,
  LocationResponse,
  PaginatedResponse,
  ApiError,
} from '../types/api.types';
import type { Customer } from '../types/customer.types';
import type { FieldDefinition, EntityType } from '../types/field.types';
import type { LocationWithFields } from '../types/location.types';

// Query Keys
export const queryKeys = {
  customers: {
    all: ['customers'] as const,
    search: (params: CustomerSearchRequest) => ['customers', 'search', params] as const,
    detail: (id: string) => ['customers', 'detail', id] as const,
    drafts: ['customers', 'drafts'] as const,
    draft: (id: string) => ['customers', 'draft', id] as const,
    recent: ['customers', 'recent'] as const,
    statistics: ['customers', 'statistics'] as const,
  },
  locations: {
    all: ['locations'] as const,
    byCustomer: (customerId: string) => ['locations', 'customer', customerId] as const,
    detail: (id: string) => ['locations', 'detail', id] as const,
    statistics: ['locations', 'statistics'] as const,
  },
  fieldDefinitions: {
    all: ['fieldDefinitions'] as const,
    byEntity: (entityType: EntityType, industry?: string) =>
      ['fieldDefinitions', entityType, industry || 'all'] as const,
    industries: ['fieldDefinitions', 'industries'] as const,
  },
};

// ===== Customer Hooks =====

/**
 * Hook to create a new customer draft
 */
export function useCreateCustomerDraft(
  options?: UseMutationOptions<CustomerDraftResponse, ApiError, void>
) {
  const queryClient = useQueryClient();

  return useMutation<CustomerDraftResponse, ApiError, void>({
    mutationFn: () => customerApi.createDraft(),
    onSuccess: data => {
      // Invalidate drafts list
      queryClient.invalidateQueries({ queryKey: queryKeys.customers.drafts });
      // Set draft data in cache
      queryClient.setQueryData(queryKeys.customers.draft(data.id), data);
    },
    ...options,
  });
}

/**
 * Hook to get customer draft by ID
 */
export function useCustomerDraft(
  draftId: string | null,
  options?: UseQueryOptions<CustomerDraftResponse, ApiError>
) {
  return useQuery<CustomerDraftResponse, ApiError>({
    queryKey: queryKeys.customers.draft(draftId!),
    queryFn: () => customerApi.getDraft(draftId!),
    enabled: !!draftId,
    staleTime: 30000, // 30 seconds
    ...options,
  });
}

/**
 * Hook to update customer draft
 */
export function useUpdateCustomerDraft(
  options?: UseMutationOptions<
    CustomerDraftResponse,
    ApiError,
    {
      draftId: string;
      data: UpdateCustomerDraftRequest;
    }
  >
) {
  const queryClient = useQueryClient();

  return useMutation<
    CustomerDraftResponse,
    ApiError,
    {
      draftId: string;
      data: UpdateCustomerDraftRequest;
    }
  >({
    mutationFn: ({ draftId, data }) => customerApi.updateDraft(draftId, data),
    onSuccess: (data, variables) => {
      // Update cache with new data
      queryClient.setQueryData(queryKeys.customers.draft(variables.draftId), data);
    },
    ...options,
  });
}

/**
 * Hook to finalize customer draft
 */
export function useFinalizeCustomerDraft(
  options?: UseMutationOptions<
    Customer,
    ApiError,
    {
      draftId: string;
      data?: FinalizeCustomerDraftRequest;
    }
  >
) {
  const queryClient = useQueryClient();

  return useMutation<
    Customer,
    ApiError,
    {
      draftId: string;
      data?: FinalizeCustomerDraftRequest;
    }
  >({
    mutationFn: ({ draftId, data }) => customerApi.finalizeDraft(draftId, data),
    onSuccess: (customer, variables) => {
      // Remove draft from cache
      queryClient.removeQueries({ queryKey: queryKeys.customers.draft(variables.draftId) });
      // Invalidate drafts list
      queryClient.invalidateQueries({ queryKey: queryKeys.customers.drafts });
      // Add new customer to cache
      queryClient.setQueryData(queryKeys.customers.detail(customer.id), customer);
      // Invalidate customer lists
      queryClient.invalidateQueries({ queryKey: queryKeys.customers.all });
    },
    ...options,
  });
}

/**
 * Hook to search customers
 */
export function useSearchCustomers(
  params: CustomerSearchRequest,
  options?: UseQueryOptions<CustomerSearchResponse, ApiError>
) {
  return useQuery<CustomerSearchResponse, ApiError>({
    queryKey: queryKeys.customers.search(params),
    queryFn: () => customerApi.searchCustomers(params),
    staleTime: 60000, // 1 minute
    ...options,
  });
}

/**
 * Hook to get customer by ID
 */
export function useCustomer(
  customerId: string | null,
  options?: UseQueryOptions<CustomerWithFields, ApiError>
) {
  return useQuery<CustomerWithFields, ApiError>({
    queryKey: queryKeys.customers.detail(customerId!),
    queryFn: () => customerApi.getCustomer(customerId!),
    enabled: !!customerId,
    staleTime: 60000, // 1 minute
    ...options,
  });
}

/**
 * Hook to update customer
 */
export function useUpdateCustomer(
  options?: UseMutationOptions<
    CustomerWithFields,
    ApiError,
    {
      customerId: string;
      fieldValues: Record<string, unknown>;
    }
  >
) {
  const queryClient = useQueryClient();

  return useMutation<
    CustomerWithFields,
    ApiError,
    {
      customerId: string;
      fieldValues: Record<string, unknown>;
    }
  >({
    mutationFn: ({ customerId, fieldValues }) =>
      customerApi.updateCustomer(customerId, fieldValues),
    onSuccess: (data, variables) => {
      // Update cache
      queryClient.setQueryData(queryKeys.customers.detail(variables.customerId), data);
      // Invalidate lists
      queryClient.invalidateQueries({ queryKey: queryKeys.customers.all });
    },
    ...options,
  });
}

/**
 * Hook to get recent customers
 */
export function useRecentCustomers(
  limit: number = 10,
  options?: UseQueryOptions<ReturnType<typeof customerApi.getRecentCustomers>, ApiError>
) {
  return useQuery({
    queryKey: queryKeys.customers.recent,
    queryFn: () => customerApi.getRecentCustomers(limit),
    staleTime: 300000, // 5 minutes
    ...options,
  });
}

/**
 * Hook to get customer statistics
 */
export function useCustomerStatistics(
  options?: UseQueryOptions<ReturnType<typeof customerApi.getStatistics>, ApiError>
) {
  return useQuery({
    queryKey: queryKeys.customers.statistics,
    queryFn: () => customerApi.getStatistics(),
    staleTime: 300000, // 5 minutes
    ...options,
  });
}

// ===== Location Hooks =====

/**
 * Hook to get customer locations
 */
export function useCustomerLocations(
  customerId: string | null,
  page: number = 0,
  size: number = 20,
  options?: UseQueryOptions<PaginatedResponse<LocationWithFields>, ApiError>
) {
  return useQuery<PaginatedResponse<LocationWithFields>, ApiError>({
    queryKey: [...queryKeys.locations.byCustomer(customerId!), { page, size }],
    queryFn: () => locationApi.getCustomerLocations(customerId!, page, size),
    enabled: !!customerId,
    staleTime: 60000, // 1 minute
    ...options,
  });
}

/**
 * Hook to create location
 */
export function useCreateLocation(
  options?: UseMutationOptions<LocationResponse, ApiError, CreateLocationRequest>
) {
  const queryClient = useQueryClient();

  return useMutation<LocationResponse, ApiError, CreateLocationRequest>({
    mutationFn: data => locationApi.createLocation(data),
    onSuccess: (data, variables) => {
      // Invalidate customer locations
      queryClient.invalidateQueries({
        queryKey: queryKeys.locations.byCustomer(variables.customerId),
      });
    },
    ...options,
  });
}

/**
 * Hook to update location
 */
export function useUpdateLocation(
  options?: UseMutationOptions<
    LocationResponse,
    ApiError,
    {
      locationId: string;
      data: Record<string, unknown>;
    }
  >
) {
  const queryClient = useQueryClient();

  return useMutation<
    LocationResponse,
    ApiError,
    {
      locationId: string;
      data: Record<string, unknown>;
    }
  >({
    mutationFn: ({ locationId, data }) =>
      locationApi.updateLocation(locationId, { fieldValues: data }),
    onSuccess: (data, variables) => {
      // Update cache
      queryClient.setQueryData(queryKeys.locations.detail(variables.locationId), data);
      // Invalidate lists
      queryClient.invalidateQueries({ queryKey: queryKeys.locations.all });
    },
    ...options,
  });
}

// ===== Field Definition Hooks =====

/**
 * Hook to get field definitions
 */
export function useFieldDefinitions(
  entityType: EntityType,
  industry?: string,
  options?: UseQueryOptions<FieldDefinition[], ApiError>
) {
  return useQuery<FieldDefinition[], ApiError>({
    queryKey: queryKeys.fieldDefinitions.byEntity(entityType, industry),
    queryFn: () => fieldDefinitionApi.getFieldDefinitions(entityType, industry),
    staleTime: 300000, // 5 minutes
    cacheTime: 600000, // 10 minutes
    ...options,
  });
}

/**
 * Hook to get available industries
 */
export function useIndustries(options?: UseQueryOptions<string[], ApiError>) {
  return useQuery<string[], ApiError>({
    queryKey: queryKeys.fieldDefinitions.industries,
    queryFn: () => fieldDefinitionApi.getIndustries(),
    staleTime: 600000, // 10 minutes
    ...options,
  });
}

/**
 * Hook to preload field definitions
 */
export function usePreloadFieldDefinitions() {
  return useMutation({
    mutationFn: () => fieldDefinitionApi.preloadCommonDefinitions(),
    onSuccess: () => {
      // Definitions are now cached
    },
  });
}
