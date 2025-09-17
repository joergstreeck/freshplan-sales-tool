import { useState, useEffect, useRef } from 'react';

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
    // Clear any existing timeout
    if (loadTimeoutRef.current) {
      clearTimeout(loadTimeoutRef.current);
    }

    if (isExpanded || preload) {
      // If already loaded, return cached items immediately
      if (loadedItems) {
        return;
      }

      setIsLoading(true);

      // Simulate async loading with a small delay for smooth animation
      loadTimeoutRef.current = setTimeout(() => {
        setLoadedItems(cacheRef.current);
        setIsLoading(false);
      }, 50); // Small delay to ensure smooth animation
    } else {
      // Optional: Clear loaded items when collapsed to save memory
      // Uncomment if you want aggressive memory optimization
      // loadTimeoutRef.current = setTimeout(() => {
      //   setLoadedItems(null);
      // }, 300); // Wait for collapse animation to finish
    }

    return () => {
      if (loadTimeoutRef.current) {
        clearTimeout(loadTimeoutRef.current);
      }
    };
  }, [isExpanded, preload, loadedItems]);

  // Preload on hover for better UX
  const preloadItems = () => {
    if (!loadedItems && !isLoading) {
      setIsLoading(true);
      loadTimeoutRef.current = setTimeout(() => {
        setLoadedItems(cacheRef.current);
        setIsLoading(false);
      }, 0);
    }
  };

  return {
    items: loadedItems || [],
    isLoading,
    preloadItems,
    hasItems: items.length > 0,
  };
};

// Cache for frequently accessed submenus
const submenuCache = new Map<string, SubMenuItem[]>();

export const useCachedSubMenu = (
  menuId: string,
  loader: () => SubMenuItem[] | Promise<SubMenuItem[]>
) => {
  const [items, setItems] = useState<SubMenuItem[]>(() => {
    // Check cache first
    return submenuCache.get(menuId) || [];
  });
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    // If items are already in cache, we're done
    if (submenuCache.has(menuId)) {
      return;
    }

    setIsLoading(true);

    const loadItems = async () => {
      try {
        const result = await loader();
        submenuCache.set(menuId, result);
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