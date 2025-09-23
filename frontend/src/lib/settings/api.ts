/**
 * Settings API Client with ETag Support
 * Part of Sprint 1.3 PR #3 - Frontend Settings Integration (FP-233)
 *
 * Implements conditional requests with ETag/If-None-Match headers
 * to optimize cache performance (target: ≥70% hit rate)
 */

import { API_BASE_URL } from '@/lib/constants';

// In-memory ETag store for conditional requests
const etagStore = new Map<string, string>();

// Cache for 304 responses
const settingsCache = new Map<string, SettingResponse>();

export interface SettingResponse {
  id: string;
  scope: 'GLOBAL' | 'TENANT' | 'TERRITORY' | 'ACCOUNT' | 'CONTACT_ROLE';
  scopeId?: string;
  key: string;
  value: Record<string, unknown>;
  metadata?: Record<string, unknown>;
  etag: string;
  version: number;
  updatedAt?: string;
}

/**
 * Fetches a setting from the API with ETag support for conditional requests.
 * Sends If-None-Match header if we have a cached ETag.
 * Returns cached data on 304 Not Modified response.
 */
export async function fetchSetting(
  key: string,
  scope: 'GLOBAL' | 'TENANT' | 'TERRITORY' | 'ACCOUNT' | 'CONTACT_ROLE' = 'GLOBAL',
  scopeId?: string
): Promise<SettingResponse | null> {
  const cacheKey = `${scope}:${scopeId || ''}:${key}`;
  const etag = etagStore.get(cacheKey);

  const headers: HeadersInit = {
    'Content-Type': 'application/json',
  };

  // Add If-None-Match header for conditional request
  if (etag) {
    headers['If-None-Match'] = etag;
  }

  try {
    const queryParams = new URLSearchParams({
      scope,
      key,
    });

    if (scopeId) {
      queryParams.append('scopeId', scopeId);
    }

    const response = await fetch(
      `${API_BASE_URL}/api/settings?${queryParams}`,
      {
        method: 'GET',
        headers,
        credentials: 'include', // Include cookies for auth
      }
    );

    // Handle 304 Not Modified - return cached data
    if (response.status === 304) {
      console.debug(`[Settings] Cache hit for ${cacheKey} (304 Not Modified)`);
      return settingsCache.get(cacheKey) || null;
    }

    // Handle 404 - setting not found
    if (response.status === 404) {
      console.debug(`[Settings] Setting not found: ${cacheKey}`);
      return null;
    }

    // Handle successful response
    if (response.ok) {
      const data: SettingResponse = await response.json();

      // Update ETag store
      const newEtag = response.headers.get('ETag');
      if (newEtag) {
        etagStore.set(cacheKey, newEtag);
      }

      // Update cache
      settingsCache.set(cacheKey, data);

      console.debug(`[Settings] Fetched ${cacheKey}, ETag: ${newEtag}`);
      return data;
    }

    // Throw error for unexpected status codes to trigger retry
    const errorMsg = `[Settings] Unexpected status ${response.status} for ${cacheKey}`;
    console.error(errorMsg);
    throw new Error(errorMsg);

  } catch (error) {
    console.error('[Settings] Fetch error:', error);
    // Return cached data as fallback on network errors
    return settingsCache.get(cacheKey) || null;
  }
}

/**
 * Resolves a setting hierarchically based on context.
 * The API will return the most specific setting available.
 */
export async function resolveSetting(
  key: string,
  context?: {
    tenantId?: string;
    territory?: string;
    accountId?: string;
    contactRole?: string;
  }
): Promise<SettingResponse | null> {
  // Create stable cache key by sorting context keys
  const sortedContext = context ?
    Object.keys(context).sort().reduce((acc, key) => {
      if (context[key as keyof typeof context] !== undefined) {
        acc[key] = context[key as keyof typeof context];
      }
      return acc;
    }, {} as any) : {};

  const cacheKey = `resolve:${key}:${JSON.stringify(sortedContext)}`;
  const etag = etagStore.get(cacheKey);

  const headers: HeadersInit = {
    'Content-Type': 'application/json',
  };

  if (etag) {
    headers['If-None-Match'] = etag;
  }

  try {
    const queryParams = new URLSearchParams();

    if (context?.tenantId) queryParams.append('tenantId', context.tenantId);
    if (context?.territory) queryParams.append('territory', context.territory);
    if (context?.accountId) queryParams.append('accountId', context.accountId);
    if (context?.contactRole) queryParams.append('contactRole', context.contactRole);

    const response = await fetch(
      `${API_BASE_URL}/api/settings/resolve/${encodeURIComponent(key)}?${queryParams}`,
      {
        method: 'GET',
        headers,
        credentials: 'include',
      }
    );

    // Handle 304 Not Modified
    if (response.status === 304) {
      console.debug(`[Settings] Cache hit for resolved ${key} (304 Not Modified)`);
      return settingsCache.get(cacheKey) || null;
    }

    // Handle 404 - setting not found at any level
    if (response.status === 404) {
      console.debug(`[Settings] No setting found for ${key} in hierarchy`);
      return null;
    }

    if (response.ok) {
      const data: SettingResponse = await response.json();

      // Update ETag store
      const newEtag = response.headers.get('ETag');
      if (newEtag) {
        etagStore.set(cacheKey, newEtag);
      }

      // Update cache
      settingsCache.set(cacheKey, data);

      console.debug(`[Settings] Resolved ${key} at scope ${data.scope}, ETag: ${newEtag}`);
      return data;
    }

    console.error(`[Settings] Unexpected status ${response.status} for resolved ${key}`);
    return null;

  } catch (error) {
    console.error('[Settings] Resolve error:', error);
    return settingsCache.get(cacheKey) || null;
  }
}

/**
 * Clears the ETag and cache stores.
 * Useful for testing or when forcing a refresh.
 */
export function clearSettingsCache(): void {
  etagStore.clear();
  settingsCache.clear();
  console.debug('[Settings] Cache cleared');
}

/**
 * Gets cache statistics for monitoring.
 */
export function getCacheStats(): {
  etagCount: number;
  cacheCount: number;
  etags: string[];
  cachedKeys: string[];
} {
  return {
    etagCount: etagStore.size,
    cacheCount: settingsCache.size,
    etags: Array.from(etagStore.keys()),
    cachedKeys: Array.from(settingsCache.keys()),
  };
}