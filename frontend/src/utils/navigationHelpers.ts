import type { NavigationItem } from '@/types/navigation';

/**
 * Creates an efficient lookup map for navigation paths.
 * This converts O(n²) lookups to O(1) for breadcrumb generation.
 *
 * @param config - The navigation configuration array
 * @returns A Map with path as key and navigation item details as value
 */
export function createNavigationPathMap(
  config: NavigationItem[]
): Map<string, { item: NavigationItem; parent?: NavigationItem }> {
  const pathMap = new Map<string, { item: NavigationItem; parent?: NavigationItem }>();

  // First pass: Add all main items
  config.forEach(item => {
    pathMap.set(item.path, { item });
  });

  // Second pass: Add all sub-items with parent reference
  config.forEach(parentItem => {
    if (parentItem.subItems) {
      parentItem.subItems.forEach(subItem => {
        // Create a pseudo NavigationItem for sub-items
        const subItemWithPath: NavigationItem = {
          id: `${parentItem.id}-${subItem.label.toLowerCase().replace(/\s+/g, '-')}`,
          label: subItem.label,
          path: subItem.path || '',
          icon: parentItem.icon, // Inherit parent icon
          permissions: subItem.permissions,
        };

        if (subItem.path) {
          pathMap.set(subItem.path, {
            item: subItemWithPath,
            parent: parentItem
          });
        }
      });
    }
  });

  return pathMap;
}

/**
 * Constants for performance optimization
 */
export const ANIMATION_DELAY = 50; // ms - Consistent animation delay
export const MAX_RECENT_ITEMS = 5; // Maximum recently visited items to keep
export const CACHE_TTL = 15 * 60 * 1000; // 15 minutes cache TTL

/**
 * Debounce function for search input
 * @param func - Function to debounce
 * @param wait - Delay in milliseconds
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: NodeJS.Timeout;

  return function executedFunction(...args: Parameters<T>) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };

    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
}

/**
 * Format path segment to readable label
 * @param segment - URL segment
 * @returns Formatted label
 */
export function formatPathSegment(segment: string): string {
  return segment
    .split('-')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}