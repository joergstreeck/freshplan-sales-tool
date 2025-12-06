/**
 * Universal Search Hook
 *
 * Custom hook for performing universal search across customers and contacts
 * with debouncing, caching, and error handling.
 *
 * @module useUniversalSearch
 * @since FC-005 PR4
 */

import { useState, useEffect, useCallback, useRef } from 'react';
import { useDebounce } from './useDebounce';

// Types
interface ContactSearchResult {
  id: string;
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  position?: string;
  customerId: string;
  customerName: string;
  isPrimary?: boolean;
}

interface CustomerSearchResult {
  id: string;
  companyName: string;
  customerNumber: string;
  status: string;
  contactEmail?: string;
  contactPhone?: string;
  contactCount?: number;
}

interface SearchResult<T> {
  type: 'customer' | 'contact';
  id: string;
  data: T;
  relevanceScore: number;
  matchedFields: string[];
}

interface SearchResults {
  customers: SearchResult<CustomerSearchResult>[];
  contacts: SearchResult<ContactSearchResult>[];
  totalCount: number;
  executionTime: number;
  metadata?: {
    query: string;
    queryType: 'EMAIL' | 'PHONE' | 'CUSTOMER_NUMBER' | 'TEXT';
    truncated: boolean;
    suggestions?: string[];
  };
}

interface UseUniversalSearchOptions {
  includeContacts?: boolean;
  includeInactive?: boolean;
  limit?: number;
  debounceMs?: number;
  minQueryLength?: number;
  context?: 'leads' | 'customers'; // NEW: Search context
}

interface UseUniversalSearchReturn {
  searchResults: SearchResults | null;
  isLoading: boolean;
  error: string | null;
  search: (query: string) => void;
  clearResults: () => void;
}

// Cache for search results with size limits
const MAX_CACHE_SIZE = 100; // Maximum number of cached searches
const searchCache = new Map<string, SearchResults>();
const CACHE_TTL = 60000; // 1 minute
const cacheTimestamps = new Map<string, number>();

// LRU cache eviction when size limit reached
function evictOldestCacheEntry() {
  if (searchCache.size >= MAX_CACHE_SIZE) {
    const oldestKey = Array.from(cacheTimestamps.entries()).sort((a, b) => a[1] - b[1])[0]?.[0];
    if (oldestKey) {
      searchCache.delete(oldestKey);
      cacheTimestamps.delete(oldestKey);
    }
  }
}

/**
 * Hook for universal search functionality
 */
export const useUniversalSearch = (
  options: UseUniversalSearchOptions = {}
): UseUniversalSearchReturn => {
  const {
    includeContacts = true,
    includeInactive = false,
    limit = 10,
    debounceMs = 300,
    minQueryLength = 2,
    context = 'customers', // NEW: Default to customers
  } = options;

  const [searchQuery, setSearchQuery] = useState<string>('');
  const [searchResults, setSearchResults] = useState<SearchResults | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const abortControllerRef = useRef<AbortController | null>(null);
  const debouncedQuery = useDebounce(searchQuery, debounceMs);

  /**
   * Generates cache key from search parameters
   */
  const getCacheKey = useCallback(
    (query: string): string => {
      return `${query}-${includeContacts}-${includeInactive}-${limit}-${context}`;
    },
    [includeContacts, includeInactive, limit, context]
  );

  /**
   * Checks if cached result is still valid
   */
  const isCacheValid = useCallback((key: string): boolean => {
    const timestamp = cacheTimestamps.get(key);
    if (!timestamp) return false;
    return Date.now() - timestamp < CACHE_TTL;
  }, []);

  /**
   * Performs the search API call
   * For leads context: Uses pg_trgm fuzzy search (Sprint 2.1.8)
   * For customers context: Uses standard universal search
   */
  const performSearch = useCallback(
    async (query: string) => {
      // Check minimum query length
      if (query.length < minQueryLength) {
        setSearchResults(null);
        setError(null);
        return;
      }

      // Check cache first
      const cacheKey = getCacheKey(query);
      if (searchCache.has(cacheKey) && isCacheValid(cacheKey)) {
        setSearchResults(searchCache.get(cacheKey)!);
        setError(null);
        return;
      }

      // Cancel previous request
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }

      // Create new abort controller
      abortControllerRef.current = new AbortController();

      setIsLoading(true);
      setError(null);

      try {
        const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';
        let transformedResults: SearchResults;

        if (context === 'leads') {
          // Sprint 2.1.8: Use pg_trgm fuzzy search for leads
          const fuzzyParams = new URLSearchParams({
            q: query,
            limit: limit.toString(),
            includeInactive: includeInactive.toString(),
          });

          const response = await fetch(
            `${apiUrl}/api/leads/search/fuzzy?${fuzzyParams.toString()}`,
            {
              signal: abortControllerRef.current.signal,
              credentials: 'include',
              headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
              },
            }
          );

          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
          }

          const fuzzyData = await response.json();

          // Transform fuzzy search response to SearchResults format
          // fuzzyData format: { data: Lead[], total: number, query: string, fuzzyEnabled: boolean }
          transformedResults = {
            customers: (fuzzyData.data || []).map(
              (lead: {
                id: string | number;
                companyName?: string;
                status?: string;
                email?: string;
                phone?: string;
                city?: string;
                contacts?: unknown[];
              }) => ({
                type: 'customer' as const,
                id: String(lead.id),
                data: {
                  id: String(lead.id),
                  companyName: lead.companyName || '',
                  customerNumber: '', // Leads don't have customer numbers
                  status: lead.status || 'REGISTERED',
                  contactEmail: lead.email,
                  contactPhone: lead.phone,
                  contactCount: lead.contacts?.length || 0,
                },
                relevanceScore: 100, // pg_trgm already sorted by relevance
                matchedFields: [
                  'companyName',
                  lead.city ? 'city' : '',
                  lead.email ? 'email' : '',
                ].filter(Boolean),
              })
            ),
            contacts: [], // Fuzzy search doesn't include separate contact results
            totalCount: fuzzyData.total || 0,
            executionTime: 0,
            metadata: {
              query: fuzzyData.query || query,
              queryType: 'TEXT' as const,
              truncated: false,
              suggestions: fuzzyData.fuzzyEnabled
                ? ['Tippfehler-tolerante Suche aktiv']
                : undefined,
            },
          };
        } else {
          // Standard universal search for customers
          const params = new URLSearchParams({
            query,
            includeContacts: includeContacts.toString(),
            includeInactive: includeInactive.toString(),
            limit: limit.toString(),
            context: context,
          });

          const response = await fetch(`${apiUrl}/api/search/universal?${params.toString()}`, {
            signal: abortControllerRef.current.signal,
            credentials: 'include',
            headers: {
              Accept: 'application/json',
              'Content-Type': 'application/json',
            },
          });

          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
          }

          const data = (await response.json()) as SearchResults;

          transformedResults = {
            customers: data.customers || [],
            contacts: data.contacts || [],
            totalCount: data.totalCount || 0,
            executionTime: data.executionTime || 0,
            metadata: data.metadata,
          };
        }

        // Update cache with size limit check
        evictOldestCacheEntry();
        searchCache.set(cacheKey, transformedResults);
        cacheTimestamps.set(cacheKey, Date.now());

        setSearchResults(transformedResults);
        setError(null);
      } catch (_err) {
        // Ignore aborted requests
        if (_err instanceof Error && _err.name === 'AbortError') {
          return;
        }

        const error = _err as Error & { response?: { data?: { message?: string } } };
        setError(error.response?.data?.message || error.message || 'Fehler bei der Suche');
        setSearchResults(null);
      } finally {
        setIsLoading(false);
      }
    },
    [minQueryLength, includeContacts, includeInactive, limit, context, getCacheKey, isCacheValid]
  );

  /**
   * Public search function
   */
  const search = useCallback((query: string) => {
    setSearchQuery(query);
  }, []);

  /**
   * Clear search results
   */
  const clearResults = useCallback(() => {
    setSearchQuery('');
    setSearchResults(null);
    setError(null);

    // Cancel any pending request
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
  }, []);

  /**
   * Effect to perform search on debounced query change
   */
  useEffect(() => {
    if (debouncedQuery) {
      performSearch(debouncedQuery);
    } else {
      setSearchResults(null);
      setError(null);
    }

    // Cleanup on unmount
    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, [debouncedQuery, performSearch]);

  return {
    searchResults,
    isLoading,
    error,
    search,
    clearResults,
  };
};

/**
 * Hook for quick search (autocomplete)
 */
export const useQuickSearch = (
  limit: number = 5,
  debounceMs: number = 200
): UseUniversalSearchReturn => {
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [searchResults, setSearchResults] = useState<SearchResults | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const abortControllerRef = useRef<AbortController | null>(null);
  const debouncedQuery = useDebounce(searchQuery, debounceMs);

  const performQuickSearch = useCallback(
    async (query: string) => {
      if (query.length < 1) {
        setSearchResults(null);
        return;
      }

      // Cancel previous request
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }

      abortControllerRef.current = new AbortController();
      setIsLoading(true);
      setError(null);

      try {
        const params = new URLSearchParams({
          query,
          limit: limit.toString(),
        });

        const response = await fetch(
          `http://localhost:8080/api/search/quick?${params.toString()}`,
          {
            signal: abortControllerRef.current.signal,
            headers: {
              Accept: 'application/json',
              'Content-Type': 'application/json',
            },
          }
        );

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = (await response.json()) as SearchResults;
        setSearchResults(data);
        setError(null);
      } catch (_err) {
        void _err;
        if (err instanceof Error && err.name === 'AbortError') return;

        setError('Fehler bei der Schnellsuche');
        setSearchResults(null);
      } finally {
        setIsLoading(false);
      }
    },
    [limit]
  );

  const search = useCallback((query: string) => {
    setSearchQuery(query);
  }, []);

  const clearResults = useCallback(() => {
    setSearchQuery('');
    setSearchResults(null);
    setError(null);
  }, []);

  useEffect(() => {
    if (debouncedQuery) {
      performQuickSearch(debouncedQuery);
    } else {
      setSearchResults(null);
    }

    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, [debouncedQuery, performQuickSearch]);

  return {
    searchResults,
    isLoading,
    error,
    search,
    clearResults,
  };
};
