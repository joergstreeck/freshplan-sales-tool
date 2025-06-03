import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,  // Enable parallelism
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,  // 1 retry in CI for stability
  workers: 2,  // 2 workers for parallel execution
  reporter: [
    ['list'],
    ['html', { open: 'never' }]
  ],
  
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'off',
    screenshot: 'only-on-failure',
    video: 'off',
    // Erhöhte Timeouts für CI-Stabilität
    actionTimeout: 15000,      // 15s für einzelne Aktionen
    navigationTimeout: 15000,  // 15s für Navigation
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
    command: 'npm run preview:ci',
    port: 3000,
    reuseExistingServer: false,
    timeout: 30000,
  },

  timeout: 30000,  // 30s global timeout
});