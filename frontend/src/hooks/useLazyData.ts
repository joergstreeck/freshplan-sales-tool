/**
 * useLazyData Hook
 *
 * Custom hook for lazy loading data with intersection observer.
 * Useful for deferring expensive API calls until component is visible.
 *
 * @module useLazyData
 * @since FC-005 PR4
 */

import { useState, useEffect, useRef, useCallback } from 'react';
import { useInView } from 'react-intersection-observer';

interface UseLazyDataOptions {
  threshold?: number;
  rootMargin?: string;
  triggerOnce?: boolean;
  enabled?: boolean;
  delay?: number;
}

interface UseLazyDataResult<T> {
  data: T | null;
  loading: boolean;
  error: Error | null;
  ref: (node?: Element | null) => void;
  inView: boolean;
  reload: () => void;
}

/**
 * Hook for lazy loading data when element comes into view
 *
 * @param fetcher - Function that fetches the data
 * @param options - Configuration options
 * @returns Object with data, loading state, error, ref, and reload function
 */
export function useLazyData<T>(
  fetcher: () => Promise<T>,
  options: UseLazyDataOptions = {}
): UseLazyDataResult<T> {
  const {
    threshold = 0.1,
    rootMargin = '50px',
    triggerOnce = true,
    enabled = true,
    delay = 0,
  } = options;

  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);
  const [shouldFetch, setShouldFetch] = useState(false);
  const abortControllerRef = useRef<AbortController | null>(null);

  const { ref, inView } = useInView({
    threshold,
    rootMargin,
    triggerOnce,
    skip: !enabled,
  });

  // Load data function
  const loadData = useCallback(async () => {
    // Cancel previous request if exists
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }

    // Create new abort controller
    abortControllerRef.current = new AbortController();

    setLoading(true);
    setError(null);

    try {
      // Optional delay for demo purposes
      if (delay > 0) {
        await new Promise(resolve => setTimeout(resolve, delay));
      }

      const result = await fetcher();

      // Check if request was aborted
      if (!abortControllerRef.current?.signal.aborted) {
        setData(result);
      }
    } catch (_err) { void _err;
      // Ignore abort errors
      if (err instanceof Error && err.name === 'AbortError') {
        return;
      }

      setError(err instanceof Error ? err : new Error('Failed to load data'));
    } finally {
      setLoading(false);
    }
  }, [fetcher, delay]);

  // Trigger fetch when in view
  useEffect(() => {
    if (inView && enabled && !data && !loading && !error) {
      setShouldFetch(true);
    }
  }, [inView, enabled, data, loading, error]);

  // Execute fetch
  useEffect(() => {
    if (shouldFetch) {
      loadData();
      setShouldFetch(false);
    }
  }, [shouldFetch, loadData]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, []);

  // Reload function
  const reload = useCallback(() => {
    setData(null);
    setError(null);
    loadData();
  }, [loadData]);

  return {
    data,
    loading,
    error,
    ref,
    inView,
    reload,
  };
}

/**
 * Hook for infinite scrolling with lazy loading
 */
export function useLazyInfiniteData<T>(
  fetcher: (page: number) => Promise<T[]>,
  options: UseLazyDataOptions & { pageSize?: number } = {}
): {
  data: T[];
  loading: boolean;
  error: Error | null;
  hasMore: boolean;
  loadMore: () => void;
  ref: (node?: Element | null) => void;
} {
  const { pageSize = 20, ...lazyOptions } = options;

  const [data, setData] = useState<T[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const { ref, inView } = useInView({
    threshold: lazyOptions.threshold || 0.1,
    rootMargin: lazyOptions.rootMargin || '100px',
    skip: !hasMore || loading,
  });

  const loadMore = useCallback(async () => {
    if (loading || !hasMore) return;

    setLoading(true);
    setError(null);

    try {
      const newData = await fetcher(page);

      if (newData.length === 0 || newData.length < pageSize) {
        setHasMore(false);
      }

      setData(prev => [...prev, ...newData]);
      setPage(prev => prev + 1);
    } catch (_err) { void _err;
      setError(err instanceof Error ? err : new Error('Failed to load more data'));
    } finally {
      setLoading(false);
    }
  }, [fetcher, page, pageSize, loading, hasMore]);

  useEffect(() => {
    if (inView && hasMore && !loading) {
      loadMore();
    }
  }, [inView, hasMore, loading, loadMore]);

  return {
    data,
    loading,
    error,
    hasMore,
    loadMore,
    ref,
  };
}
