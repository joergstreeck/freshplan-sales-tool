import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,  // No parallelism
  forbidOnly: !!process.env.CI,
  retries: 0,  // No retries
  workers: 1,  // Single worker
  reporter: [['list']],
  
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