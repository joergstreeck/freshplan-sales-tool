import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useBusinessTypes } from '../useBusinessTypes';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import React from 'react';

// Mock fetch globally
global.fetch = vi.fn();

describe('useBusinessTypes', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    });
    vi.clearAllMocks();
  });

  afterEach(() => {
    queryClient.clear();
  });

  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );

  it('should fetch business types from API', async () => {
    const mockData = [
      { value: 'RESTAURANT', label: 'Restaurant' },
      { value: 'HOTEL', label: 'Hotel' },
      { value: 'CATERING', label: 'Catering' },
    ];

    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      json: async () => mockData,
    } as Response);

    const { result } = renderHook(() => useBusinessTypes(), { wrapper });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toEqual(mockData);
    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining('/api/enums/business-types'),
      expect.objectContaining({
        headers: expect.objectContaining({
          'Content-Type': 'application/json',
        }),
      })
    );
  });

  it('should return all 9 business types', async () => {
    const mockData = [
      { value: 'RESTAURANT', label: 'Restaurant' },
      { value: 'HOTEL', label: 'Hotel' },
      { value: 'CATERING', label: 'Catering' },
      { value: 'KANTINE', label: 'Kantine' },
      { value: 'GROSSHANDEL', label: 'Großhandel' },
      { value: 'LEH', label: 'LEH (Lebensmitteleinzelhandel)' },
      { value: 'BILDUNG', label: 'Bildungseinrichtung' },
      { value: 'GESUNDHEIT', label: 'Gesundheitswesen' },
      { value: 'SONSTIGES', label: 'Sonstiges' },
    ];

    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      json: async () => mockData,
    } as Response);

    const { result } = renderHook(() => useBusinessTypes(), { wrapper });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toHaveLength(9);
    expect(result.current.data?.map(item => item.value)).toEqual([
      'RESTAURANT',
      'HOTEL',
      'CATERING',
      'KANTINE',
      'GROSSHANDEL',
      'LEH',
      'BILDUNG',
      'GESUNDHEIT',
      'SONSTIGES',
    ]);
  });

  it('should cache data for 5 minutes (staleTime)', async () => {
    const mockData = [{ value: 'RESTAURANT', label: 'Restaurant' }];

    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: true,
      json: async () => mockData,
    } as Response);

    const { result, rerender } = renderHook(() => useBusinessTypes(), {
      wrapper,
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(global.fetch).toHaveBeenCalledTimes(1);

    // Re-render should NOT trigger new fetch (within staleTime)
    rerender();

    expect(global.fetch).toHaveBeenCalledTimes(1); // Still 1!
  });

  it('should handle API errors', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      ok: false,
      statusText: 'Internal Server Error',
    } as Response);

    const { result } = renderHook(() => useBusinessTypes(), { wrapper });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error).toBeDefined();
    expect(result.current.data).toBeUndefined();
  });

  it('should handle network errors', async () => {
    (global.fetch as ReturnType<typeof vi.fn>).mockRejectedValueOnce(new Error('Network error'));

    const { result } = renderHook(() => useBusinessTypes(), { wrapper });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error).toBeDefined();
    expect(result.current.data).toBeUndefined();
  });
});
