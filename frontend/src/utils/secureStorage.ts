/**
 * Secure localStorage utilities with validation and sanitization
 */

/**
 * Safely get a boolean value from localStorage with validation
 * @param key - The localStorage key
 * @param defaultValue - Default value if key doesn't exist or is invalid
 * @returns Validated boolean value
 */
export function getSecureBoolean(key: string, defaultValue: boolean = false): boolean {
  try {
    if (typeof window === 'undefined' || !window.localStorage) {
      return defaultValue;
    }

    const value = window.localStorage.getItem(key);

    // Validate that the value is exactly 'true' or 'false'
    if (value === 'true') return true;
    if (value === 'false') return false;

    // If invalid or null, return default
    return defaultValue;
  } catch (error) {
    console.warn(`Failed to read localStorage key "${key}":`, error);
    return defaultValue;
  }
}

/**
 * Safely set a boolean value in localStorage
 * @param key - The localStorage key
 * @param value - Boolean value to store
 */
export function setSecureBoolean(key: string, value: boolean): void {
  try {
    if (typeof window === 'undefined' || !window.localStorage) {
      return;
    }

    window.localStorage.setItem(key, value.toString());
  } catch (error) {
    console.warn(`Failed to write localStorage key "${key}":`, error);
  }
}

/**
 * Safely get a string value from localStorage with length validation
 * @param key - The localStorage key
 * @param defaultValue - Default value if key doesn't exist or is invalid
 * @param maxLength - Maximum allowed length (default: 1000)
 * @returns Validated string value
 */
export function getSecureString(
  key: string,
  defaultValue: string = '',
  maxLength: number = 1000
): string {
  try {
    if (typeof window === 'undefined' || !window.localStorage) {
      return defaultValue;
    }

    const value = window.localStorage.getItem(key);

    if (value === null) {
      return defaultValue;
    }

    // Validate length to prevent memory attacks
    if (value.length > maxLength) {
      console.warn(`localStorage value for "${key}" exceeds max length, clearing`);
      window.localStorage.removeItem(key);
      return defaultValue;
    }

    return value;
  } catch (error) {
    console.warn(`Failed to read localStorage key "${key}":`, error);
    return defaultValue;
  }
}

/**
 * List of allowed localStorage keys for additional security
 */
const ALLOWED_KEYS = [
  'hasSeenKeyboardHelp',
  'theme',
  'language',
  'sidebarCollapsed',
] as const;

/**
 * Validate that a localStorage key is in the allowlist
 * @param key - The key to validate
 * @returns Whether the key is allowed
 */
export function isAllowedKey(key: string): boolean {
  return ALLOWED_KEYS.includes(key as keyof typeof ALLOWED_KEYS);
}

/**
 * Clear all application localStorage data
 */
export function clearAppStorage(): void {
  try {
    if (typeof window === 'undefined' || !window.localStorage) {
      return;
    }

    ALLOWED_KEYS.forEach(key => {
      window.localStorage.removeItem(key);
    });
  } catch (error) {
    console.warn('Failed to clear app storage:', error);
  }
}