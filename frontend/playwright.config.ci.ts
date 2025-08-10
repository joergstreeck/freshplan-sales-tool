import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: true,
  retries: 2,
  workers: 1,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:5173', // Changed to dev server port
    trace: 'on-first-retry',
    // Add browser context options to handle storage issues
    contextOptions: {
      // Disable same-origin policy for testing
      bypassCSP: true,
      // Allow insecure content
      ignoreHTTPSErrors: true,
    },
    // Add launch options for better CI compatibility
    launchOptions: {
      args: [
        '--disable-web-security',
        '--disable-features=IsolateOrigins',
        '--disable-site-isolation-trials',
      ],
    },
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],

  // No webServer config - we'll start it manually in CI
});
