import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
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
