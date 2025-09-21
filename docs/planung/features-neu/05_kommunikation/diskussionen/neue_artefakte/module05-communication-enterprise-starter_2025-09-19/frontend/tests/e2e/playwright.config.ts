import { defineConfig } from '@playwright/test';
export default defineConfig({
  use: { baseURL: process.env.BASE_URL, trace: 'on-first-retry' },
  reporter: [['list'], ['html', { outputFolder: 'test-results' }]]
});