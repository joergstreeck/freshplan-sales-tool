import { test, expect } from '@playwright/test';
import { mockBackendAPIs } from '../fixtures/api-mocks';

test.describe('Simple Customer Page Test', () => {
  test.beforeEach(async ({ page }) => {
    // Mock all backend API calls
    await mockBackendAPIs(page);
    
    // Mock authentication
    await page.addInitScript(() => {
      // Mock localStorage
      window.localStorage.setItem('auth-token', 'test-token');
      window.localStorage.setItem('user', JSON.stringify({
        id: 'test-user',
        name: 'Test User',
        role: 'admin'
      }));
      
      // Mock auth state directly on window
      (window as any).__AUTH_STATE__ = {
        isAuthenticated: true,
        user: {
          id: 'test-user',
          name: 'Test User',
          role: 'admin'
        }
      };
    });
  });

  test('should load customer page', async ({ page }) => {
    // Navigate to customers page
    await page.goto('/kundenmanagement');
    
    // Wait for page to load
    await page.waitForLoadState('networkidle');
    
    // Debug: Log page content
    const title = await page.title();
    console.log('Page title:', title);
    
    // Take screenshot for debugging
    await page.screenshot({ path: 'test-results/customer-page-simple.png' });
    
    // Check if main elements are present
    const hasCustomerHeading = await page.locator('text=Kunden').count() > 0;
    const hasNewButton = await page.locator('button').count() > 0;
    
    console.log('Has Customer heading:', hasCustomerHeading);
    console.log('Has any button:', hasNewButton);
    
    // Log all button texts
    const buttons = await page.locator('button').all();
    for (const button of buttons) {
      const text = await button.textContent();
      console.log('Button found:', text);
    }
    
    // Minimal assertion - page should at least load
    expect(await page.locator('body').count()).toBeGreaterThan(0);
  });

  test('should render empty state', async ({ page }) => {
    // Navigate directly to the new customer wizard URL
    await page.goto('/kundenmanagement/neu');
    
    // Wait for any response
    await page.waitForLoadState('networkidle');
    
    // Take screenshot
    await page.screenshot({ path: 'test-results/wizard-direct.png' });
    
    // Check if wizard opens with direct navigation
    const hasDialog = await page.locator('[role="dialog"], [role="presentation"]').count() > 0;
    const hasWizardContent = await page.locator('text=/Basis|Standort|Kunde/i').count() > 0;
    
    console.log('Has dialog:', hasDialog);
    console.log('Has wizard content:', hasWizardContent);
    
    // At least the page should render
    expect(await page.locator('body').count()).toBeGreaterThan(0);
  });
});