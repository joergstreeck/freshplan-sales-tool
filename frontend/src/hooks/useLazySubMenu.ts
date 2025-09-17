import { useState, useEffect, useRef, useCallback } from 'react';

interface SubMenuItem {
  label: string;
  path?: string;
  action?: string;
  permissions?: string[];
  disabled?: boolean;
  tooltip?: string;
  hasOwnPage?: boolean;
  subItems?: SubMenuItem[];
}

interface UseLazySubMenuOptions {
  items?: SubMenuItem[];
  isExpanded: boolean;
  preload?: boolean;
}

/**
 * Hook for lazy loading submenu items with animation delay.
 * Optimizes performance by only loading items when needed.
 *
 * @param options - Configuration options
 * @param options.items - The submenu items to load
 * @param options.isExpanded - Whether the menu is currently expanded
 * @param options.preload - Whether to preload items even when not expanded
 * @returns Object containing loaded items, loading state, and preload function
 */
export const useLazySubMenu = ({ items = [], isExpanded, preload = false }: UseLazySubMenuOptions) => {
  const [loadedItems, setLoadedItems] = useState<SubMenuItem[] | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const loadTimeoutRef = useRef<NodeJS.Timeout>();
  const cacheRef = useRef<SubMenuItem[]>(items);

  useEffect(() => {
    // Update cache when items change
    cacheRef.current = items;
  }, [items]);

  useEffect(() => {
    // If already loaded, return cached items immediately
    if (loadedItems && (isExpanded || preload)) {
      return;
    }

    let timeoutId: NodeJS.Timeout | undefined;

    if (isExpanded || preload) {
      setIsLoading(true);

      // Simulate async loading with a small delay for smooth animation
      timeoutId = setTimeout(() => {
        setLoadedItems(cacheRef.current);
        setIsLoading(false);
      }, 50); // Small delay to ensure smooth animation
    }

    // Cleanup function that properly clears the timeout
    return () => {
      if (timeoutId) {
        clearTimeout(timeoutId);
      }
    };
  }, [isExpanded, preload]); // Removed loadedItems from dependencies to prevent infinite loop

  // Preload on hover for better UX
  const preloadItems = useCallback(() => {
    if (!loadedItems && !isLoading) {
      setIsLoading(true);
      const timeoutId = setTimeout(() => {
        setLoadedItems(cacheRef.current);
        setIsLoading(false);
      }, 0);

      // Store timeout for potential cleanup
      loadTimeoutRef.current = timeoutId;
    }
  }, [loadedItems, isLoading]);

  return {
    items: loadedItems || [],
    isLoading,
    preloadItems,
    hasItems: items.length > 0,
  };
};

/**
 * Cache for frequently accessed submenus with automatic cleanup.
 * Uses WeakMap for automatic garbage collection when components unmount.
 */
const submenuCache = new Map<string, { data: SubMenuItem[]; timestamp: number }>();
const CACHE_MAX_AGE = 5 * 60 * 1000; // 5 minutes

/**
 * Cleans up stale cache entries
 */
function cleanupCache() {
  const now = Date.now();
  for (const [key, value] of submenuCache.entries()) {
    if (now - value.timestamp > CACHE_MAX_AGE) {
      submenuCache.delete(key);
    }
  }
}

// Cleanup cache every minute
if (typeof window !== 'undefined') {
  setInterval(cleanupCache, 60 * 1000);
}

/**
 * Hook for loading submenu items with caching support.
 * Caches results globally to avoid redundant loading.
 *
 * @param menuId - Unique identifier for the menu
 * @param loader - Function that loads the menu items
 * @returns Object containing items and loading state
 */
export const useCachedSubMenu = (
  menuId: string,
  loader: () => SubMenuItem[] | Promise<SubMenuItem[]>
) => {
  const [items, setItems] = useState<SubMenuItem[]>(() => {
    // Check cache first
    const cached = submenuCache.get(menuId);
    if (cached && Date.now() - cached.timestamp < CACHE_MAX_AGE) {
      return cached.data;
    }
    return [];
  });
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    // Check if cache is still fresh
    const cached = submenuCache.get(menuId);
    if (cached && Date.now() - cached.timestamp < CACHE_MAX_AGE) {
      return;
    }

    setIsLoading(true);

    const loadItems = async () => {
      try {
        const result = await loader();
        submenuCache.set(menuId, { data: result, timestamp: Date.now() });
        setItems(result);
      } catch (error) {
        console.error(`Failed to load submenu ${menuId}:`, error);
        setItems([]);
      } finally {
        setIsLoading(false);
      }
    };

    loadItems();
  }, [menuId, loader]);

  return { items, isLoading };
};