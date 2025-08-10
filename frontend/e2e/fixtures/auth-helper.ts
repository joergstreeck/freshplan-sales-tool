/**
 * Helper for safely mocking authentication in E2E tests
 * Handles cases where localStorage/sessionStorage might not be available
 */
export async function mockAuth(page: any) {
  await page.addInitScript(() => {
    const mockUser = {
      id: 'test-user',
      name: 'Test User',
      role: 'admin'
    };
    
    // Try localStorage
    try {
      window.localStorage.setItem('auth-token', 'test-token');
      window.localStorage.setItem('user', JSON.stringify(mockUser));
    } catch (e) {
      console.log('localStorage not available');
    }
    
    // Try sessionStorage as fallback
    try {
      window.sessionStorage.setItem('auth-token', 'test-token');
      window.sessionStorage.setItem('user', JSON.stringify(mockUser));
    } catch (e) {
      console.log('sessionStorage not available');
    }
    
    // Always set auth state on window object
    (window as any).__AUTH_STATE__ = {
      isAuthenticated: true,
      user: mockUser
    };
    
    // Mock the auth methods that the app might use
    (window as any).__mockAuth = {
      getToken: () => 'test-token',
      getUser: () => mockUser,
      isAuthenticated: () => true
    };
  });
}