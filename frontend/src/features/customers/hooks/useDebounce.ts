/**
 * Debounce Hook for delayed value updates
 *
 * @module useDebounce
 * @since FC-005 PR4
 */

import { useState, useEffect } from 'react';

/**
 * Debounces a value by the specified delay
 *
 * @param value - The value to debounce
 * @param delay - Delay in milliseconds
 * @returns The debounced value
 */
export function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}
