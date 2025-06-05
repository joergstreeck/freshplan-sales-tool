import { test, expect } from '@playwright/test';

test.describe('Backend Integration', () => {
  test('user can ping backend API', async ({ page }) => {
    // Go to the app
    await page.goto('/');

    // Click the Ping API button
    await page.getByRole('button', { name: 'Ping API' }).click();

    // Wait for and check the response
    const result = page.locator('pre');
    await expect(result).toBeVisible();

    // Check that we got a pong response
    const text = await result.textContent();
    expect(text).toContain('"message": "pong"');

    // For mocked backend, we might see an error instead
    // In that case, just verify the button works
    if (text?.includes('Error')) {
      expect(text).toContain('Error: API Error');
    }
  });
});
