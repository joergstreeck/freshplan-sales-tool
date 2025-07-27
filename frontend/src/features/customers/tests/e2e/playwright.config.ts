/**
 * FC-005 Playwright Configuration for E2E Tests
 * 
 * ZWECK: Konfiguration für End-to-End Tests des Customer Management Moduls
 * PHILOSOPHIE: Realistische Browser-Tests für kritische User-Journeys
 */

import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './specs',
  
  // Test artifacts
  outputDir: '../../../../../test-results',
  
  // Global test timeout
  timeout: 30 * 1000, // 30 seconds per test
  
  // Global expect timeout
  expect: {
    timeout: 5 * 1000 // 5 seconds for assertions
  },
  
  // Test execution settings
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  
  // Reporter configuration
  reporter: [
    ['html', { outputFolder: '../../../../../playwright-report' }],
    ['json', { outputFile: '../../../../../test-results/results.json' }],
    ['line']
  ],
  
  // Global test setup
  use: {
    // Base URL for the application
    baseURL: 'http://localhost:5173',
    
    // Browser settings
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    
    // Timeouts
    navigationTimeout: 30 * 1000,
    actionTimeout: 10 * 1000,
    
    // Locale
    locale: 'de-DE',
    timezoneId: 'Europe/Berlin',
    
    // Permissions (if needed for features)
    permissions: ['clipboard-read', 'clipboard-write']
  },
  
  // Test projects for different browsers
  projects: [
    {
      name: 'chromium',
      use: { 
        ...devices['Desktop Chrome'],
        // Viewport for customer onboarding
        viewport: { width: 1280, height: 720 }
      },
    },
    
    {
      name: 'firefox',
      use: { 
        ...devices['Desktop Firefox'],
        viewport: { width: 1280, height: 720 }
      },
    },
    
    {
      name: 'webkit',
      use: { 
        ...devices['Desktop Safari'],
        viewport: { width: 1280, height: 720 }
      },
    },
    
    // Mobile testing for responsive design
    {
      name: 'Mobile Chrome',
      use: { 
        ...devices['Pixel 5'],
      },
    },
    
    {
      name: 'Mobile Safari',
      use: { 
        ...devices['iPhone 12'],
      },
    },
  ],
  
  // Development server (if we need to start it automatically)
  webServer: process.env.CI ? undefined : {
    command: 'npm run dev',
    port: 5173,
    timeout: 120 * 1000,
    reuseExistingServer: !process.env.CI,
  },
  
  // Global setup and teardown
  globalSetup: require.resolve('./setup/global-setup.ts'),
  globalTeardown: require.resolve('./setup/global-teardown.ts'),
});