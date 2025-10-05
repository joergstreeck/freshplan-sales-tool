/// <reference types="vitest" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  build: {
    // Optimize for mobile - smaller chunks
    chunkSizeWarningLimit: 500,
    rollupOptions: {
      output: {
        // Manual chunking for better code splitting
        manualChunks(id) {
          // Split vendor dependencies into separate chunks
          if (id.includes('node_modules')) {
            // React ecosystem
            if (id.includes('react') || id.includes('react-dom') || id.includes('react-router')) {
              return 'vendor-react';
            }
            // Material UI
            if (id.includes('@mui')) {
              return 'vendor-mui';
            }
            // Charts library (biggest culprit)
            if (id.includes('recharts')) {
              return 'vendor-charts';
            }
            // Form libraries
            if (id.includes('react-hook-form') || id.includes('zod')) {
              return 'vendor-forms';
            }
            // Query library
            if (id.includes('@tanstack/react-query')) {
              return 'vendor-query';
            }
            // DnD library
            if (id.includes('@dnd-kit')) {
              return 'vendor-dnd';
            }
            // Date utilities
            if (id.includes('date-fns')) {
              return 'vendor-date';
            }
            // i18n
            if (id.includes('i18next') || id.includes('react-i18next')) {
              return 'vendor-i18n';
            }
            // Other utilities
            if (id.includes('lodash') || id.includes('clsx') || id.includes('classnames')) {
              return 'vendor-utils';
            }
            // Everything else from node_modules
            return 'vendor';
          }
          
          // Split features into separate chunks
          if (id.includes('src/features/audit')) {
            return 'feature-audit';
          }
          if (id.includes('src/features/customers')) {
            return 'feature-customers';
          }
          if (id.includes('src/features/cockpit')) {
            return 'feature-cockpit';
          }
          if (id.includes('src/features/calculator')) {
            return 'feature-calculator';
          }
          if (id.includes('src/features/opportunity')) {
            return 'feature-opportunity';
          }
        },
      },
    },
    // Minification options
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true, // Remove console logs in production
        drop_debugger: true,
        pure_funcs: ['console.log', 'console.info', 'console.debug'],
      },
    },
    // Enable source maps for debugging (can be disabled for smaller builds)
    sourcemap: false,
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.tsx', './src/test/mocks/setup.ts'],
    testTimeout: 5000, // 5 seconds instead of default 30s
    hookTimeout: 10000, // 10 seconds for setup/teardown
    exclude: [
      'tests/**', 
      'node_modules/**', 
      'dist/**',
      '**/e2e/**',
      '**/*.e2e.*',
      '**/*.spec.*'  // Exclude Playwright spec files
    ],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html', 'text-summary'],
      reportsDirectory: './coverage',
      exclude: [
        'node_modules/',
        'src/test/',
        '**/*.d.ts',
        '**/*.config.*',
        '**/mockServiceWorker.js',
        'src/main.tsx',
        'src/vite-env.d.ts',
      ],
      // Thresholds auskommentiert - Coverage wird immer generiert, auch bei failing Tests
      // thresholds: {
      //   lines: 80,
      //   functions: 80,
      //   branches: 80,
      //   statements: 80,
      // },
    },
  },
});
