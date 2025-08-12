/**
 * FC-005 Playwright Global Setup
 *
 * ZWECK: Globale Vorbereitung für E2E Tests (DB Seed, Auth Setup, etc.)
 * PHILOSOPHIE: Saubere Test-Umgebung für jeden Test-Lauf
 */

import { chromium, FullConfig } from '@playwright/test';

async function globalSetup(config: FullConfig) {
  // Check if development server is available
  const baseURL = config.projects[0].use.baseURL || 'http://localhost:5173';

  try {
    const response = await fetch(`${baseURL}/api/health`);
    if (!response.ok) {
    } else {
    }
  } catch (error) {}

  // Setup test data if needed
  try {
    await seedTestData(baseURL);
  } catch (error) {}

  // Setup authentication state for tests that need it
  try {
    await setupAuthState(config);
  } catch (error) {}
}

async function seedTestData(baseURL: string) {
  // Seed test customers for E2E tests
  const testCustomers = [
    {
      customerData: {
        companyName: 'E2E Test Hotel GmbH',
        industry: 'hotel',
        chainCustomer: 'nein',
        hotelStars: '4',
      },
    },
    {
      customerData: {
        companyName: 'E2E Chain Restaurant AG',
        industry: 'hotel',
        chainCustomer: 'ja',
        hotelStars: '5',
      },
      locations: [
        { id: 'e2e-loc-1', name: 'Berlin Mitte' },
        { id: 'e2e-loc-2', name: 'München Zentrum' },
      ],
    },
  ];

  for (const customer of testCustomers) {
    try {
      const response = await fetch(`${baseURL}/api/customers`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(customer),
      });

      if (!response.ok) {
      }
    } catch {
      // Ignore individual seed failures
    }
  }
}

async function setupAuthState(config: FullConfig) {
  const browser = await chromium.launch();
  const context = await browser.newContext();
  const page = await context.newPage();

  const baseURL = config.projects[0].use.baseURL || 'http://localhost:5173';

  try {
    // Navigate to login page
    await page.goto(`${baseURL}/login`);

    // If there's a login form, fill it out
    const loginForm = page.locator('form');
    if (await loginForm.isVisible({ timeout: 2000 })) {
      // Fill login credentials (adjust selectors based on actual login form)
      await page.fill('[name="username"], [type="email"]', 'e2e-test@freshplan.de');
      await page.fill('[name="password"], [type="password"]', 'test-password');
      await page.click('button[type="submit"], .login-button');

      // Wait for successful login
      await page.waitForURL('**/dashboard', { timeout: 10000 });

      // Save authentication state
      await context.storageState({ path: './e2e-auth-state.json' });
    }
  } catch {
  } finally {
    await browser.close();
  }
}

export default globalSetup;
