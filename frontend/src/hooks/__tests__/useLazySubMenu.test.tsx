import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';
import { useLazySubMenu, useCachedSubMenu } from '../useLazySubMenu';

describe('useLazySubMenu', () => {
  const mockItems = [
    { label: 'Item 1', path: '/item1' },
    { label: 'Item 2', path: '/item2' },
    { label: 'Item 3', path: '/item3' },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.runOnlyPendingTimers();
    vi.useRealTimers();
  });

  it('should not load items initially when not expanded', () => {
    const { result } = renderHook(() =>
      useLazySubMenu({
        items: mockItems,
        isExpanded: false,
      })
    );

    expect(result.current.items).toEqual([]);
    expect(result.current.isLoading).toBe(false);
    expect(result.current.hasItems).toBe(true);
  });

  it('should load items when expanded', () => {
    const { result } = renderHook(() =>
      useLazySubMenu({
        items: mockItems,
        isExpanded: true,
      })
    );

    expect(result.current.isLoading).toBe(true);

    act(() => {
      vi.advanceTimersByTime(50);
    });

    expect(result.current.items).toEqual(mockItems);
    expect(result.current.isLoading).toBe(false);
  });

  it('should preload items when preload is true', () => {
    const { result } = renderHook(() =>
      useLazySubMenu({
        items: mockItems,
        isExpanded: false,
        preload: true,
      })
    );

    expect(result.current.isLoading).toBe(true);

    act(() => {
      vi.advanceTimersByTime(50);
    });

    expect(result.current.items).toEqual(mockItems);
    expect(result.current.isLoading).toBe(false);
  });

  it('should cache loaded items and not reload', () => {
    const { result, rerender } = renderHook(
      ({ isExpanded }) =>
        useLazySubMenu({
          items: mockItems,
          isExpanded,
        }),
      {
        initialProps: { isExpanded: true },
      }
    );

    // Wait for initial load
    act(() => {
      vi.advanceTimersByTime(50);
    });

    expect(result.current.items).toEqual(mockItems);

    // Collapse
    rerender({ isExpanded: false });

    // Expand again
    rerender({ isExpanded: true });

    // Should not be loading again
    expect(result.current.isLoading).toBe(false);
    expect(result.current.items).toEqual(mockItems);
  });

  it('should handle preloadItems function', () => {
    const { result } = renderHook(() =>
      useLazySubMenu({
        items: mockItems,
        isExpanded: false,
      })
    );

    expect(result.current.items).toEqual([]);

    act(() => {
      result.current.preloadItems();
      vi.advanceTimersByTime(0);
    });

    expect(result.current.items).toEqual(mockItems);
    expect(result.current.isLoading).toBe(false);
  });

  it('should handle empty items array', () => {
    const { result } = renderHook(() =>
      useLazySubMenu({
        items: [],
        isExpanded: true,
      })
    );

    act(() => {
      vi.advanceTimersByTime(50);
    });

    expect(result.current.items).toEqual([]);
    expect(result.current.hasItems).toBe(false);
  });

  it('should not trigger multiple loads when preloadItems is called repeatedly', () => {
    const { result } = renderHook(() =>
      useLazySubMenu({
        items: mockItems,
        isExpanded: false,
      })
    );

    act(() => {
      result.current.preloadItems();
      result.current.preloadItems();
      result.current.preloadItems();
    });

    // Should only be loading once
    expect(result.current.isLoading).toBe(true);
  });
});

describe('useCachedSubMenu', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should load items from loader function', async () => {
    const mockLoader = vi.fn(() => [
      { label: 'Cached Item 1', path: '/cached1' },
      { label: 'Cached Item 2', path: '/cached2' },
    ]);

    const { result } = renderHook(() =>
      useCachedSubMenu('test-menu', mockLoader)
    );

    expect(result.current.isLoading).toBe(true);

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
      expect(result.current.items).toHaveLength(2);
      expect(result.current.items[0].label).toBe('Cached Item 1');
    });

    expect(mockLoader).toHaveBeenCalledTimes(1);
  });

  it('should use cached items on subsequent renders', async () => {
    const mockLoader = vi.fn(() => [
      { label: 'Cached Item', path: '/cached' },
    ]);

    const { result: result1 } = renderHook(() =>
      useCachedSubMenu('test-menu-2', mockLoader)
    );

    await waitFor(() => {
      expect(result1.current.isLoading).toBe(false);
    });

    // Second hook with same menuId should use cache
    const { result: result2 } = renderHook(() =>
      useCachedSubMenu('test-menu-2', mockLoader)
    );

    expect(result2.current.isLoading).toBe(false);
    expect(result2.current.items).toEqual(result1.current.items);
    expect(mockLoader).toHaveBeenCalledTimes(1); // Should not call loader again
  });

  it('should handle async loader functions', async () => {
    const mockAsyncLoader = vi.fn(async () => {
      await new Promise(resolve => setTimeout(resolve, 10));
      return [{ label: 'Async Item', path: '/async' }];
    });

    const { result } = renderHook(() =>
      useCachedSubMenu('async-menu', mockAsyncLoader)
    );

    expect(result.current.isLoading).toBe(true);

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
      expect(result.current.items[0].label).toBe('Async Item');
    });
  });

  it('should handle loader errors gracefully', async () => {
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    const mockErrorLoader = vi.fn(() => {
      throw new Error('Load failed');
    });

    const { result } = renderHook(() =>
      useCachedSubMenu('error-menu', mockErrorLoader)
    );

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
      expect(result.current.items).toEqual([]);
    });

    expect(consoleErrorSpy).toHaveBeenCalledWith(
      expect.stringContaining('Failed to load submenu error-menu:'),
      expect.any(Error)
    );

    consoleErrorSpy.mockRestore();
  });
});