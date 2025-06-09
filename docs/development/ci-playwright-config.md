# Aktuelle Playwright-Konfiguration (CI)

**Pfad:** `playwright.config.ts` (im Root-Verzeichnis)

## Wichtige Erkenntnisse:

1. **testDir**: `./e2e` - Tests werden nur im e2e Verzeichnis gesucht
2. **webServer**: 
   - `reuseExistingServer: true` - sollte Port-Konflikte verhindern
   - `timeout: 120000` - 2 Minuten Timeout für Server-Start
3. **CI-spezifische Settings**:
   - `workers: 1` in CI (keine Parallelisierung)
   - `retries: 2` in CI
   - Längere Timeouts (30s statt 10s)
4. **Projects**: Alle Browser definiert (chromium, firefox, webkit, mobile)

## Vollständige Konfiguration:

```ts
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ['html'],
    ['json', { outputFile: 'test-results/results.json' }],
    ['junit', { outputFile: 'test-results/junit.xml' }],
  ],
  
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
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
        channel: 'chrome',
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
          channel: 'chrome',
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
    command: 'npm run dev',
    port: 3000,
    reuseExistingServer: true,   // <<< NEU – verhindert den Konflikt
    timeout: 120000,
    // Mehr Details für CI debugging
    stdout: 'pipe',
    stderr: 'pipe',
  },

  // Test output settings
  outputDir: 'test-results/',
  
  // Global test timeout
  timeout: process.env.CI ? 60000 : 30000,
  
  // Expect timeout
  expect: {
    timeout: process.env.CI ? 10000 : 5000,
  },
});
```

## Potenzielle Probleme:

1. **testDir ist auf `./e2e` gesetzt** - das könnte erklären, warum Playwright keine Tests findet, wenn das Verzeichnis in CI anders heißt oder Case-Sensitivity-Probleme hat
2. **Mobile Browser** sind auch definiert - diese könnten ungewollt mitlaufen
3. **SMOKE_TEST Environment Variable** - wird nicht in den Workflows gesetzt, könnte aber das Verhalten beeinflussen