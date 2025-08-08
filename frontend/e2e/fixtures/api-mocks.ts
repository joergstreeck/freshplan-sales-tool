import { Page, Route } from '@playwright/test';

/**
 * Mock all backend API calls for E2E tests
 * This ensures tests run independently of backend availability
 */
export async function mockBackendAPIs(page: Page) {
  // Mock /api/customers endpoint (GET and POST)
  await page.route('**/api/customers*', async (route: Route) => {
    if (route.request().method() === 'POST') {
      // Handle customer creation
      const body = route.request().postDataJSON();
      await route.fulfill({
        status: 201,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 'test-customer-' + Date.now(),
          ...body,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        })
      });
    } else {
      // Handle GET requests
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          content: [],
          totalElements: 0,
          totalPages: 0,
          size: 20,
          number: 0
        })
      });
    }
  });

  // Mock /api/ping endpoint
  await page.route('**/api/ping', async (route: Route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 'ok',
        timestamp: new Date().toISOString()
      })
    });
  });

  // Mock field definitions
  await page.route('**/api/field-definitions*', async (route: Route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([])
    });
  });

  // Mock auth endpoints
  await page.route('**/api/auth/**', async (route: Route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        authenticated: true,
        user: {
          id: 'test-user',
          name: 'Test User',
          role: 'admin'
        }
      })
    });
  });

  // Mock opportunities
  await page.route('**/api/opportunities*', async (route: Route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        content: [],
        totalElements: 0
      })
    });
  });

  // Catch-all for other API routes
  await page.route('**/api/**', async (route: Route) => {
    console.log(`Mocking unhandled API route: ${route.request().url()}`);
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({})
    });
  });
}