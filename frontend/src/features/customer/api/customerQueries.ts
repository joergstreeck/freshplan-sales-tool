import { useQuery } from '@tanstack/react-query';
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
