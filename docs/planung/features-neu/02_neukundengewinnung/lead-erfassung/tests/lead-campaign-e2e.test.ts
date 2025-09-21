// lead-campaign-e2e.test.ts – End-to-End (Playwright)
import { test, expect } from '@playwright/test';

test('Lead → Sample → Campaign flow', async ({ page }) => {
  await page.goto('/');
  await page.click('text=Neuer Lead');
  await page.fill('input[name="name"]', 'Hotel Aurora');
  await page.click('text=Speichern');
  await expect(page.locator('text=Lead erstellt')).toBeVisible();

  await page.click('text=Sample anfordern');
  await expect(page.locator('text=Sample REQUESTED')).toBeVisible();

  await page.goto('/campaigns');
  await expect(page.locator('text=ROI-Pipeline')).toBeVisible();
});
