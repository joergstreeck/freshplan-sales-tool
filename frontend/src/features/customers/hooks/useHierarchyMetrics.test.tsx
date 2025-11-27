/**
 * useHierarchyMetrics Hook Tests
 * Sprint 2.1.7.7 D5 - Multi-Location Management
 *
 * @description Tests für useHierarchyMetrics - React Query Hook für Hierarchy Metrics
 * @since 2025-11-15
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useHierarchyMetrics, HierarchyMetrics } from './useHierarchyMetrics';
import { httpClient } from '@/lib/apiClient';
import React from 'react';

// Mock httpClient
vi.mock('@/lib/apiClient', () => ({
  httpClient: {
    get: vi.fn(),
  },
}));

describe('useHierarchyMetrics', () => {
  let queryClient: QueryClient;

  const mockCustomerId = '550e8400-e29b-41d4-a716-446655440000';

  const mockMetricsData: HierarchyMetrics = {
    totalRevenue: 300000,
    averageRevenue: 100000,
    branchCount: 3,
    totalOpenOpportunities: 8,
    branches: [
      {
        branchId: '550e8400-e29b-41d4-a716-446655440001',
        branchName: 'NH Hotel München',
        city: 'München',
        country: 'DEU',
        revenue: 120000,
        percentage: 40.0,
        openOpportunities: 3,
        status: 'AKTIV',
      },
      {
        branchId: '550e8400-e29b-41d4-a716-446655440002',
        branchName: 'NH Hotel Hamburg',
        city: 'Hamburg',
        country: 'DEU',
        revenue: 100000,
        percentage: 33.3,
        openOpportunities: 3,
        status: 'AKTIV',
      },
      {
        branchId: '550e8400-e29b-41d4-a716-446655440003',
        branchName: 'NH Hotel Berlin',
        city: 'Berlin',
        country: 'DEU',
        revenue: 80000,
        percentage: 26.7,
        openOpportunities: 2,
        status: 'PROSPECT',
      },
    ],
  };

  beforeEach(() => {
    // Create fresh QueryClient for each test
    queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false, // Disable retries in tests
          gcTime: 0, // Disable garbage collection
        },
      },
    });

    vi.clearAllMocks();
  });

  afterEach(() => {
    queryClient.clear();
  });

  // Helper to create wrapper with QueryClient
  const createWrapper = () => {
    return ({ children }: { children: React.ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );
  };

  it('fetches hierarchy metrics successfully', async () => {
    // Mock successful API response
    vi.mocked(httpClient.get).mockResolvedValueOnce({
      data: mockMetricsData,
    });

    const { result } = renderHook(() => useHierarchyMetrics(mockCustomerId), {
      wrapper: createWrapper(),
    });

    // Initially loading
    expect(result.current.isLoading).toBe(true);
    expect(result.current.data).toBeUndefined();

    // Wait for data to load
    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    // Verify data
    expect(result.current.data).toEqual(mockMetricsData);
    expect(result.current.data?.totalRevenue).toBe(300000);
    expect(result.current.data?.branchCount).toBe(3);
    expect(result.current.data?.branches).toHaveLength(3);

    // Verify API was called with correct URL
    expect(httpClient.get).toHaveBeenCalledWith(
      `/api/customers/${mockCustomerId}/hierarchy/metrics`
    );
  });

  it('handles API error correctly', async () => {
    // Mock API error
    const mockError = new Error('Failed to fetch hierarchy metrics');
    vi.mocked(httpClient.get).mockRejectedValueOnce(mockError);

    const { result } = renderHook(() => useHierarchyMetrics(mockCustomerId), {
      wrapper: createWrapper(),
    });

    // Wait for error state
    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });

    expect(result.current.error).toBeTruthy();
    expect(result.current.data).toBeUndefined();
  });

  it('does not fetch when customerId is empty', () => {
    const { result } = renderHook(() => useHierarchyMetrics(''), {
      wrapper: createWrapper(),
    });

    // Should not trigger fetch
    expect(result.current.isLoading).toBe(false);
    expect(result.current.data).toBeUndefined();
    expect(httpClient.get).not.toHaveBeenCalled();
  });

  it('uses correct queryKey', async () => {
    vi.mocked(httpClient.get).mockResolvedValueOnce({
      data: mockMetricsData,
    });

    const { result } = renderHook(() => useHierarchyMetrics(mockCustomerId), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    // Verify queryKey exists in cache
    const queryData = queryClient.getQueryData(['hierarchyMetrics', mockCustomerId]);
    expect(queryData).toEqual(mockMetricsData);
  });

  it('respects staleTime configuration (5 minutes)', async () => {
    vi.mocked(httpClient.get).mockResolvedValueOnce({
      data: mockMetricsData,
    });

    const { result, rerender } = renderHook(() => useHierarchyMetrics(mockCustomerId), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    // First call should have been made
    expect(httpClient.get).toHaveBeenCalledTimes(1);

    // Rerender immediately should NOT trigger new fetch (due to staleTime)
    rerender();

    // Still only 1 call (data is not stale yet)
    expect(httpClient.get).toHaveBeenCalledTimes(1);
  });

  it('returns correct branch details structure', async () => {
    vi.mocked(httpClient.get).mockResolvedValueOnce({
      data: mockMetricsData,
    });

    const { result } = renderHook(() => useHierarchyMetrics(mockCustomerId), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    const firstBranch = result.current.data?.branches[0];
    expect(firstBranch).toHaveProperty('branchId');
    expect(firstBranch).toHaveProperty('branchName');
    expect(firstBranch).toHaveProperty('city');
    expect(firstBranch).toHaveProperty('country');
    expect(firstBranch).toHaveProperty('revenue');
    expect(firstBranch).toHaveProperty('percentage');
    expect(firstBranch).toHaveProperty('openOpportunities');
    expect(firstBranch).toHaveProperty('status');
  });

  it('handles empty branches array', async () => {
    const emptyMetrics: HierarchyMetrics = {
      totalRevenue: 0,
      averageRevenue: 0,
      branchCount: 0,
      totalOpenOpportunities: 0,
      branches: [],
    };

    vi.mocked(httpClient.get).mockResolvedValueOnce({
      data: emptyMetrics,
    });

    const { result } = renderHook(() => useHierarchyMetrics(mockCustomerId), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data?.branchCount).toBe(0);
    expect(result.current.data?.branches).toEqual([]);
  });
});
