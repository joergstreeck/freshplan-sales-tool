import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  // Ignore tests that require a running backend (real-backend tests)
  // These tests are intended for local development only
  testIgnore: process.env.CI ? ['**/real-backend*.spec.ts'] : [],
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0, // Reduced from 2 to 1
  workers: process.env.CI ? 2 : undefined, // Increased from 1 to 2 for parallel execution
  reporter: process.env.CI ? [['list'], ['html', { open: 'never' }]] : 'html',
  timeout: 30000, // Reduced from 60s to 30s per test
  expect: {
    timeout: 3000, // Reduced from default 5000ms to 3000ms
  },
  use: {
    baseURL: 'http://localhost:5173',
    trace: process.env.CI ? 'on-first-retry' : 'on-first-retry', // Only on retry to save time
    screenshot: 'only-on-failure',
    video: process.env.CI ? 'on-first-retry' : 'off', // Only on retry to save time
    actionTimeout: 10000, // Reduced from 15s to 10s
    navigationTimeout: 20000, // Reduced from 30s to 20s
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
  ],

  webServer: {
    command: process.env.CI
      ? 'npx serve dist -l 5173 --no-request-logging -s' // -s flag for SPA routing
      : 'npm run dev',
    port: 5173,
    reuseExistingServer: true, // Always reuse if server is already running (e2e-critical-paths workflow starts preview manually)
    timeout: 120 * 1000, // 2 minutes timeout for CI
    stdout: 'ignore',
    stderr: 'pipe',
    env: {
      VITE_E2E_MODE: 'true', // E2E test mode - disable Keycloak
      VITE_AUTH_BYPASS: 'true', // Bypass authentication
    },
  },
});
