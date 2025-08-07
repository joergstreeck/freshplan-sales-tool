import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: process.env.CI ? [['list'], ['html', { open: 'never' }]] : 'html',
  timeout: 60000, // 60 seconds timeout per test
  use: {
    baseURL: 'http://localhost:5173',
    trace: process.env.CI ? 'on' : 'on-first-retry',
    screenshot: 'only-on-failure',
    video: process.env.CI ? 'retain-on-failure' : 'off',
    actionTimeout: 15000, // 15 seconds for actions
    navigationTimeout: 30000, // 30 seconds for navigation
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
      ? 'npx serve dist -l 5173 --no-request-logging' 
      : 'npm run preview',
    port: 5173,
    reuseExistingServer: !process.env.CI,
    timeout: 120 * 1000, // 2 minutes timeout for CI
    stdout: 'ignore',
    stderr: 'pipe',
  },
});
