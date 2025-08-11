/**
 * Helper for safely mocking authentication in E2E tests
 * Handles cases where localStorage/sessionStorage might not be available
 */
import type { Page } from '@playwright/test';

export async function mockAuth(page: Page) {
  // Set up auth mocking BEFORE any navigation
  await page.addInitScript(() => {
    const mockUser = {
      id: 'test-user',
      name: 'Test User',
      role: 'admin'
    };
    
    // Create a mock storage implementation
    const createMockStorage = () => {
      const data: Record<string, string> = {
        'auth-token': 'test-token',
        'user': JSON.stringify(mockUser)
      };
      
      return {
        getItem: (key: string) => data[key] || null,
        setItem: (key: string, value: string) => { data[key] = value; },
        removeItem: (key: string) => { delete data[key]; },
        clear: () => { Object.keys(data).forEach(key => delete data[key]); },
        get length() { return Object.keys(data).length; },
        key: (index: number) => Object.keys(data)[index] || null
      };
    };
    
    // Only override storage if we're in a chrome-error page or if storage is not accessible
    try {
      // Test if localStorage is accessible
      window.localStorage.getItem('test');
      // If we get here, localStorage works, so just set our values
      window.localStorage.setItem('auth-token', 'test-token');
      window.localStorage.setItem('user', JSON.stringify(mockUser));
      window.sessionStorage.setItem('auth-token', 'test-token');
      window.sessionStorage.setItem('user', JSON.stringify(mockUser));
    } catch {
      // localStorage is not accessible, override it
      console.log('Storage not accessible, using mock implementation');
      
      const mockLocalStorage = createMockStorage();
      const mockSessionStorage = createMockStorage();
      
      Object.defineProperty(window, 'localStorage', {
        value: mockLocalStorage,
        writable: false,
        configurable: true
      });
      
      Object.defineProperty(window, 'sessionStorage', {
        value: mockSessionStorage,
        writable: false,
        configurable: true
      });
    }
    
    // Always set auth state on window object as a fallback
    (window as Record<string, unknown>).__AUTH_STATE__ = {
      isAuthenticated: true,
      user: mockUser
    };
    
    // Mock the auth methods that the app might use
    (window as Record<string, unknown>).__mockAuth = {
      getToken: () => 'test-token',
      getUser: () => mockUser,
      isAuthenticated: () => true
    };
    
    // Override any auth checks in the app
    (window as Record<string, unknown>).isAuthenticated = true;
    (window as Record<string, unknown>).currentUser = mockUser;
  });
}