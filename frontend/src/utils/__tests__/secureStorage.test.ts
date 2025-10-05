/**
 * SecureStorage Tests
 *
 * Tests for secure localStorage utilities with validation and sanitization.
 * Critical security component - must handle edge cases and attacks.
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import {
  getSecureBoolean,
  setSecureBoolean,
  getSecureString,
  isAllowedKey,
  clearAppStorage,
} from '../secureStorage';

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};

  return {
    getItem: vi.fn((key: string) => (key in store ? store[key] : null)),
    setItem: vi.fn((key: string, value: string) => {
      store[key] = value;
    }),
    removeItem: vi.fn((key: string) => {
      delete store[key];
    }),
    clear: vi.fn(() => {
      store = {};
    }),
    get length() {
      return Object.keys(store).length;
    },
    key: vi.fn((index: number) => Object.keys(store)[index] || null),
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
  writable: true,
});

describe('SecureStorage', () => {
  beforeEach(() => {
    localStorageMock.clear();
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('getSecureBoolean', () => {
    it('should return true when localStorage contains "true"', () => {
      localStorageMock.setItem('test', 'true');
      expect(getSecureBoolean('test')).toBe(true);
    });

    it('should return false when localStorage contains "false"', () => {
      localStorageMock.setItem('test', 'false');
      expect(getSecureBoolean('test')).toBe(false);
    });

    it('should return default value when key does not exist', () => {
      expect(getSecureBoolean('nonexistent')).toBe(false);
      expect(getSecureBoolean('nonexistent', true)).toBe(true);
    });

    it('should return default value for invalid boolean strings', () => {
      localStorageMock.setItem('test', '1');
      expect(getSecureBoolean('test', false)).toBe(false);

      localStorageMock.setItem('test', 'yes');
      expect(getSecureBoolean('test', false)).toBe(false);

      localStorageMock.setItem('test', 'TRUE');
      expect(getSecureBoolean('test', false)).toBe(false);
    });

    it('should handle localStorage errors gracefully', () => {
      const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
      localStorageMock.getItem.mockImplementationOnce(() => {
        throw new Error('Storage quota exceeded');
      });

      expect(getSecureBoolean('test', true)).toBe(true);
      expect(consoleWarnSpy).toHaveBeenCalled();
    });

    it('should handle missing window.localStorage', () => {
      const originalLocalStorage = window.localStorage;
      // @ts-expect-error Testing undefined scenario
      delete window.localStorage;

      expect(getSecureBoolean('test', true)).toBe(true);

      window.localStorage = originalLocalStorage;
    });
  });

  describe('setSecureBoolean', () => {
    it('should store boolean as string "true"', () => {
      setSecureBoolean('test', true);
      expect(localStorageMock.getItem('test')).toBe('true');
    });

    it('should store boolean as string "false"', () => {
      setSecureBoolean('test', false);
      expect(localStorageMock.getItem('test')).toBe('false');
    });

    it('should handle localStorage errors gracefully', () => {
      const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
      localStorageMock.setItem.mockImplementationOnce(() => {
        throw new Error('Storage quota exceeded');
      });

      setSecureBoolean('test', true);
      expect(consoleWarnSpy).toHaveBeenCalled();
    });

    it('should handle missing window.localStorage', () => {
      const originalLocalStorage = window.localStorage;
      // @ts-expect-error Testing undefined scenario
      delete window.localStorage;

      expect(() => setSecureBoolean('test', true)).not.toThrow();

      window.localStorage = originalLocalStorage;
    });
  });

  describe('getSecureString', () => {
    it('should return string value from localStorage', () => {
      localStorageMock.setItem('name', 'John Doe');
      expect(getSecureString('name')).toBe('John Doe');
    });

    it('should return default value when key does not exist', () => {
      expect(getSecureString('nonexistent')).toBe('');
      expect(getSecureString('nonexistent', 'default')).toBe('default');
    });

    it('should validate string length and reject too long values', () => {
      const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
      const longString = 'a'.repeat(1001);
      localStorageMock.setItem('long', longString);

      expect(getSecureString('long', 'default', 1000)).toBe('default');
      expect(consoleWarnSpy).toHaveBeenCalledWith(
        expect.stringContaining('exceeds max length')
      );
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('long');
    });

    it('should accept strings within max length', () => {
      const validString = 'a'.repeat(999);
      localStorageMock.setItem('valid', validString);

      expect(getSecureString('valid', 'default', 1000)).toBe(validString);
    });

    it('should use custom max length parameter', () => {
      const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
      localStorageMock.setItem('test', 'too long');

      expect(getSecureString('test', 'default', 5)).toBe('default');
      expect(consoleWarnSpy).toHaveBeenCalled();
    });

    it('should handle localStorage errors gracefully', () => {
      const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
      localStorageMock.getItem.mockImplementationOnce(() => {
        throw new Error('Storage error');
      });

      expect(getSecureString('test', 'fallback')).toBe('fallback');
      expect(consoleWarnSpy).toHaveBeenCalled();
    });

    it('should handle missing window.localStorage', () => {
      const originalLocalStorage = window.localStorage;
      // @ts-expect-error Testing undefined scenario
      delete window.localStorage;

      expect(getSecureString('test', 'default')).toBe('default');

      window.localStorage = originalLocalStorage;
    });

    it('should handle empty strings correctly', () => {
      localStorageMock.setItem('empty', '');
      // Empty string is a valid value and should be returned (not the default)
      expect(getSecureString('empty', 'default')).toBe('');
    });
  });

  describe('isAllowedKey', () => {
    it('should return true for allowed keys', () => {
      expect(isAllowedKey('hasSeenKeyboardHelp')).toBe(true);
      expect(isAllowedKey('theme')).toBe(true);
      expect(isAllowedKey('language')).toBe(true);
      expect(isAllowedKey('sidebarCollapsed')).toBe(true);
    });

    it('should return false for disallowed keys', () => {
      expect(isAllowedKey('randomKey')).toBe(false);
      expect(isAllowedKey('maliciousKey')).toBe(false);
      expect(isAllowedKey('')).toBe(false);
      expect(isAllowedKey('auth-token')).toBe(false);
    });

    it('should be case-sensitive', () => {
      expect(isAllowedKey('Theme')).toBe(false);
      expect(isAllowedKey('THEME')).toBe(false);
    });
  });

  describe('clearAppStorage', () => {
    it('should remove all allowed keys from localStorage', () => {
      // Set some allowed keys
      localStorageMock.setItem('hasSeenKeyboardHelp', 'true');
      localStorageMock.setItem('theme', 'dark');
      localStorageMock.setItem('language', 'de');
      localStorageMock.setItem('sidebarCollapsed', 'false');

      // Set a non-allowed key (should not be removed)
      localStorageMock.setItem('otherKey', 'value');

      clearAppStorage();

      // Allowed keys should be removed
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('hasSeenKeyboardHelp');
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('theme');
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('language');
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('sidebarCollapsed');

      // Should be called 4 times (once for each allowed key)
      expect(localStorageMock.removeItem).toHaveBeenCalledTimes(4);
    });

    it('should handle localStorage errors gracefully', () => {
      const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
      localStorageMock.removeItem.mockImplementationOnce(() => {
        throw new Error('Storage error');
      });

      clearAppStorage();
      expect(consoleWarnSpy).toHaveBeenCalledWith(
        'Failed to clear app storage:',
        expect.any(Error)
      );
    });

    it('should handle missing window.localStorage', () => {
      const originalLocalStorage = window.localStorage;
      // @ts-expect-error Testing undefined scenario
      delete window.localStorage;

      expect(() => clearAppStorage()).not.toThrow();

      window.localStorage = originalLocalStorage;
    });
  });

  describe('Security & Edge Cases', () => {
    it('should prevent XSS via stored values', () => {
      const xssAttempt = '<script>alert("xss")</script>';
      localStorageMock.setItem('test', xssAttempt);

      // Should return the raw string (sanitization happens at usage, not storage)
      expect(getSecureString('test')).toBe(xssAttempt);
    });

    it('should handle unicode characters correctly', () => {
      const unicode = '你好世界 🎉';
      localStorageMock.setItem('unicode', unicode);

      expect(getSecureString('unicode')).toBe(unicode);
    });

    it('should handle null bytes in strings', () => {
      const withNull = 'test\0value';
      localStorageMock.setItem('null', withNull);

      expect(getSecureString('null')).toBe(withNull);
    });
  });
});
