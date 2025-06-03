import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,  // Reduce retries for faster feedback
  workers: process.env.CI ? 2 : undefined,  // GitHub runners have 2 CPUs, use 2 workers for stability
  reporter: [
    ['list'],
    [process.env.CI ? 'github' : 'html'],
    ['json', { outputFile: 'test-results/results.json' }],
    ['junit', { outputFile: 'test-results/junit.xml' }],
  ],
  
  use: {
    baseURL: 'http://localhost:3000',
    trace: process.env.CI ? 'retain-on-failure' : 'on-first-retry',
    screenshot: 'only-on-failure',
    video: process.env.CI ? 'retain-on-failure' : 'off',
    // Viewport for consistent cross-browser testing
    viewport: { width: 1280, height: 720 },
    // Longer timeouts for CI
    actionTimeout: process.env.CI ? 30000 : 10000,
    navigationTimeout: process.env.CI ? 30000 : 10000,
  },

  projects: [
    {
      name: 'chromium',
      use: { 
        ...devices['Desktop Chrome'],
        // Remove channel for better headless compatibility
      },
    },
    {
      name: 'firefox',
      use: { 
        ...devices['Desktop Firefox'],
        // Firefox-specific settings
        launchOptions: {
          firefoxUserPrefs: {
            // Disable Firefox's smooth scrolling for more consistent tests
            'general.smoothScroll': false,
          },
        },
      },
    },
    {
      name: 'webkit',
      use: { 
        ...devices['Desktop Safari'],
        // WebKit-specific settings for better CI compatibility
        launchOptions: {
          args: ['--no-sandbox'],
        },
      },
    },
    // Mobile testing (optional)
    {
      name: 'mobile-chrome',
      use: { ...devices['Pixel 5'] },
    },
    {
      name: 'mobile-safari',
      use: { ...devices['iPhone 12'] },
    },
  ],

  // Filter projects for smoke tests
  ...(process.env.SMOKE_TEST && {
    projects: [
      {
        name: 'chromium',
        use: { 
          ...devices['Desktop Chrome'],
          // Remove channel for better headless compatibility
        },
      },
      {
        name: 'firefox',
        use: { 
          ...devices['Desktop Firefox'],
        },
      },
      {
        name: 'webkit',
        use: { 
          ...devices['Desktop Safari'],
        },
      },
    ],
  }),

  webServer: {
    command: process.env.CI ? 'npm run preview:ci' : 'npm run dev',
    port: 3000,
    reuseExistingServer: !process.env.CI,  // Preview in CI immer neu, Dev lokal reused
    timeout: 60000,  // 60s reicht, weil preview in <1s da ist
    // Mehr Details für CI debugging
    stdout: 'pipe',
    stderr: 'pipe',
  },

  // Test output settings
  outputDir: 'test-results/',
  
  // Global test timeout (increased for CI stability)
  timeout: process.env.CI ? 90000 : 30000,
  
  // Expect timeout
  expect: {
    timeout: process.env.CI ? 30000 : 5000,  // 30s in CI für langsamen Vite-Start
  },
});