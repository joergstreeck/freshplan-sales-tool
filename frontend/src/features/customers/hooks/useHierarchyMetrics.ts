/**
 * useHierarchyMetrics - Sprint 2.1.7.7 Multi-Location Management
 *
 * React Query hook for fetching hierarchy metrics from backend.
 * Integrates with HierarchyMetricsService (D3).
 *
 * @module useHierarchyMetrics
 * @since Sprint 2.1.7.7
 */

import { useQuery } from '@tanstack/react-query';
import { httpClient } from '@/lib/apiClient';

/**
 * Branch revenue detail from backend
 */
export interface BranchRevenueDetail {
  branchId: string;
  branchName: string;
  city: string;
  country: string;
  revenue: number;
  percentage: number;
  openOpportunities: number;
  status: string;
}

/**
 * Hierarchy metrics response from backend (D3: HierarchyMetricsService)
 */
export interface HierarchyMetrics {
  totalRevenue: number;
  averageRevenue: number;
  branchCount: number;
  totalOpenOpportunities: number;
  branches: BranchRevenueDetail[];
}

/**
 * Fetch hierarchy metrics for a parent customer
 *
 * @param customerId - Parent customer UUID
 * @returns React Query result with metrics data
 *
 * @example
 * const { data: metrics, isLoading } = useHierarchyMetrics(parentCustomer.id);
 */
export function useHierarchyMetrics(customerId: string) {
  return useQuery<HierarchyMetrics>({
    queryKey: ['hierarchyMetrics', customerId],
    queryFn: async () => {
      const response = await httpClient.get<HierarchyMetrics>(
        `/api/customers/${customerId}/hierarchy/metrics`
      );
      return response.data;
    },
    enabled: !!customerId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
}
