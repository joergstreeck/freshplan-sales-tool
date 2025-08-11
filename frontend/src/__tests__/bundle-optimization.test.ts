import { describe, it, expect } from 'vitest';
import fs from 'fs';
import path from 'path';

describe('Bundle Optimization Configuration', () => {
  it('should have vite config with manual chunks configured', () => {
    const viteConfigPath = path.resolve(__dirname, '../../vite.config.ts');
    const viteConfig = fs.readFileSync(viteConfigPath, 'utf-8');

    // Check that manual chunks are configured
    expect(viteConfig).toContain('manualChunks');
    expect(viteConfig).toContain('vendor-react');
    expect(viteConfig).toContain('vendor-mui');
    expect(viteConfig).toContain('vendor-charts');
  });

  it('should have terser configured for minification', () => {
    const viteConfigPath = path.resolve(__dirname, '../../vite.config.ts');
    const viteConfig = fs.readFileSync(viteConfigPath, 'utf-8');

    // Check that terser is configured
    expect(viteConfig).toContain("minify: 'terser'");
    expect(viteConfig).toContain('terserOptions');
    expect(viteConfig).toContain('drop_console: true');
  });

  it('should have terser as a dev dependency', () => {
    const packageJsonPath = path.resolve(__dirname, '../../package.json');
    const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, 'utf-8'));

    // Check that terser is installed
    expect(packageJson.devDependencies).toHaveProperty('terser');
  });

  it('should have source maps disabled for production', () => {
    const viteConfigPath = path.resolve(__dirname, '../../vite.config.ts');
    const viteConfig = fs.readFileSync(viteConfigPath, 'utf-8');

    // Check that sourcemaps are disabled
    expect(viteConfig).toContain('sourcemap: false');
  });

  it('should use lazy loading for heavy pages', () => {
    const providersPath = path.resolve(__dirname, '../../src/providers.tsx');
    const providers = fs.readFileSync(providersPath, 'utf-8');

    // Check that lazy loading is used
    expect(providers).toContain('lazy(');
    expect(providers).toContain('UsersPage = lazy(');
    expect(providers).toContain('CustomersPageV2 = lazy(');
    expect(providers).toContain('CockpitPage = lazy(');
  });
});
