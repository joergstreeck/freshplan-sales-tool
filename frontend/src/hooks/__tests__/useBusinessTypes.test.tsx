import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useBusinessTypes } from '../useBusinessTypes';
import { vi, describe, it, expect, beforeEach, afterEach, beforeAll, afterAll } from 'vitest';
import React from 'react';
import { server } from '@/mocks/server';
import { http, HttpResponse } from 'msw';

describe('useBusinessTypes', () => {
  let queryClient: QueryClient;

  beforeAll(() => {
    server.listen({ onUnhandledRequest: 'bypass' });
  });

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
    server.resetHandlers();
  });

  afterAll(() => {
    server.close();
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

    server.use(
      http.get('*/api/enums/business-types', () => {
        return HttpResponse.json(mockData);
      })
    );

    const { result } = renderHook(() => useBusinessTypes(), { wrapper });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toEqual(mockData);
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

    server.use(
      http.get('*/api/enums/business-types', () => {
        return HttpResponse.json(mockData);
      })
    );

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

    let fetchCount = 0;

    server.use(
      http.get('*/api/enums/business-types', () => {
        fetchCount++;
        return HttpResponse.json(mockData);
      })
    );

    const { result, rerender } = renderHook(() => useBusinessTypes(), {
      wrapper,
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    const initialFetchCount = fetchCount;

    // Re-render should NOT trigger new fetch (within staleTime)
    rerender();

    // Wait a bit to ensure no new fetch happens
    await new Promise(resolve => setTimeout(resolve, 50));

    expect(fetchCount).toBe(initialFetchCount); // Should not increase
  });

  it('should handle API errors', async () => {
    server.use(
      http.get('*/api/enums/business-types', () => {
        return HttpResponse.json({ error: 'Internal Server Error' }, { status: 500 });
      })
    );

    const { result } = renderHook(() => useBusinessTypes(), { wrapper });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error).toBeDefined();
    expect(result.current.data).toBeUndefined();
  });

  it('should handle network errors', async () => {
    server.use(
      http.get('*/api/enums/business-types', () => {
        return HttpResponse.error();
      })
    );

    const { result } = renderHook(() => useBusinessTypes(), { wrapper });

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.error).toBeDefined();
    expect(result.current.data).toBeUndefined();
  });
});
