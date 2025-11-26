import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { customerApi } from './customerApi';
import type { CustomerStatus } from '../types/customer.types';

// Query Keys
export const customerKeys = {
  all: ['customers'] as const,
  lists: () => [...customerKeys.all, 'list'] as const,
  list: (page?: number, size?: number, sortBy?: string) =>
    [...customerKeys.lists(), { page, size, sortBy }] as const,
  byStatus: (status: CustomerStatus, page?: number, size?: number) =>
    [...customerKeys.lists(), 'status', status, { page, size }] as const,
  search: (query: string, page?: number, size?: number) =>
    [...customerKeys.all, 'search', query, { page, size }] as const,
  // Sprint 2.1.7.7 D4: Branch Management
  branches: (headquarterId: string) => [...customerKeys.all, 'branches', headquarterId] as const,
};

// Hooks
export const useCustomers = (page = 0, size = 20, sortBy = 'companyName') => {
  return useQuery({
    queryKey: customerKeys.list(page, size, sortBy),
    queryFn: () => customerApi.getCustomers(page, size, sortBy),
    staleTime: 1000 * 60 * 5, // 5 minutes
  });
};

export const useCustomersByStatus = (
  status: CustomerStatus,
  page = 0,
  size = 20,
  enabled = true
) => {
  return useQuery({
    queryKey: customerKeys.byStatus(status, page, size),
    queryFn: () => customerApi.getCustomersByStatus(status, page, size),
    enabled,
    staleTime: 1000 * 60 * 5,
  });
};

export const useCustomerSearch = (query: string, page = 0, size = 20, enabled = true) => {
  return useQuery({
    queryKey: customerKeys.search(query, page, size),
    queryFn: () => customerApi.searchCustomers(query, page, size),
    enabled: enabled && query.length > 0,
    staleTime: 1000 * 60 * 2, // 2 minutes for search results
  });
};

//Server-Side Filtering with advanced filters
export const useCustomerSearchAdvanced = (
  searchRequest: {
    globalSearch?: string;
    filters?: Array<{
      field: string;
      operator: string;
      value: string | string[];
    }>;
    sort?: {
      field: string;
      direction: 'ASC' | 'DESC';
    };
  },
  page = 0,
  size = 50,
  enabled = true
) => {
  return useQuery({
    queryKey: [...customerKeys.all, 'advanced-search', searchRequest, page, size],
    queryFn: () => customerApi.searchCustomersAdvanced(searchRequest, page, size),
    enabled,
    staleTime: 1000 * 30, // 30 seconds
    gcTime: 1000 * 60 * 5, // 5 minutes
  });
};

// ========== BRANCH MANAGEMENT HOOKS (Sprint 2.1.7.7 D4) ==========

/**
 * Hook to get all branches for a HEADQUARTER customer
 *
 * @param headquarterId UUID of the parent HEADQUARTER
 * @param enabled Whether to enable the query (default: true)
 * @returns React Query result with branches array
 */
export const useGetBranches = (headquarterId: string | null, enabled = true) => {
  return useQuery({
    queryKey: headquarterId ? customerKeys.branches(headquarterId) : ['customers', 'branches'],
    queryFn: () => customerApi.getBranches(headquarterId!),
    enabled: enabled && !!headquarterId,
    staleTime: 1000 * 60 * 5, // 5 minutes
  });
};

/**
 * Hook to create a new branch under a HEADQUARTER
 *
 * Sprint 2.1.7.7: Vollständiger Wizard mit allen relevanten Feldern
 *
 * @returns React Query mutation for creating branches
 */
export const useCreateBranch = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      headquarterId,
      branchData,
    }: {
      headquarterId: string;
      branchData: {
        // Basisdaten
        companyName: string;
        tradingName?: string;
        businessType?: string;
        customerType?: string;
        status?: string;
        expectedAnnualVolume?: number;
        // Adressdaten
        address?: {
          street?: string;
          postalCode?: string;
          city?: string;
          country?: string;
        };
        // Kontaktdaten
        contact?: {
          phone?: string;
          email?: string;
        };
      };
    }) => customerApi.createBranch(headquarterId, branchData),
    onSuccess: (newBranch, variables) => {
      // Invalidate branches list for this headquarter
      queryClient.invalidateQueries({
        queryKey: customerKeys.branches(variables.headquarterId),
      });
      // Invalidate all customer lists
      queryClient.invalidateQueries({
        queryKey: customerKeys.lists(),
      });
    },
  });
};
