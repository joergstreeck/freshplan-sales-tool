import { defineConfig } from 'vite';
import path from 'path';
import legacy from '@vitejs/plugin-legacy';
import checker from 'vite-plugin-checker';

export default defineConfig({
  root: '.',
  base: './',
  
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@core': path.resolve(__dirname, './src/core'),
      '@modules': path.resolve(__dirname, './src/modules'),
      '@utils': path.resolve(__dirname, './src/utils'),
      '@types': path.resolve(__dirname, './src/types'),
      '@store': path.resolve(__dirname, './src/store'),
      '@hooks': path.resolve(__dirname, './src/hooks'),
      '@domain': path.resolve(__dirname, './src/domain')
    },
    extensions: ['.ts', '.tsx', '.js', '.jsx', '.json']
  },
  
  build: {
    outDir: 'dist',
    emptyOutDir: true,
    sourcemap: true,
    
    rollupOptions: {
      input: {
        main: path.resolve(__dirname, 'index.html')
      },
      output: {
        manualChunks: {
          // Vendor chunks
          'vendor-zustand': ['zustand'],
          'vendor-pdf': ['jspdf'],
          
          // Core modules
          'core': [
            './src/core/EventBus.ts',
            './src/core/StateManager.ts',
            './src/core/DOMHelper.ts',
            './src/core/Module.ts'
          ],
          
          // Feature modules
          'modules': [
            './src/modules/CalculatorModule.ts',
            './src/modules/CustomerModule.ts',
            './src/modules/SettingsModule.ts',
            './src/modules/ProfileModule.ts',
            './src/modules/PDFModule.ts',
            './src/modules/i18nModule.ts',
            './src/modules/TabNavigationModule.ts'
          ]
        },
        
        // Asset naming
        assetFileNames: (assetInfo) => {
          if (!assetInfo.name) return 'assets/[name]-[hash][extname]';
          
          const info = assetInfo.name.split('.');
          const ext = info[info.length - 1];
          
          if (/png|jpe?g|svg|gif|tiff|bmp|ico/i.test(ext)) {
            return `assets/images/[name]-[hash][extname]`;
          } else if (/woff2?|ttf|otf|eot/i.test(ext)) {
            return `assets/fonts/[name]-[hash][extname]`;
          } else if (ext === 'css') {
            return `assets/css/[name]-[hash][extname]`;
          }
          
          return `assets/[name]-[hash][extname]`;
        },
        
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js'
      }
    },
    
    // Optimization
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },
    
    // Performance
    reportCompressedSize: true,
    chunkSizeWarningLimit: 1000, // 1MB
    
    // CSS
    cssCodeSplit: true,
    cssMinify: true
  },
  
  // Plugins
  plugins: [
    checker({
      typescript: true,
    }),
    legacy({
      targets: ['defaults', 'not IE 11'],
      additionalLegacyPolyfills: ['regenerator-runtime/runtime']
    })
  ],
  
  // Dev server
  server: {
    port: 3000,
    open: true,
    cors: true,
    strictPort: false,
    
    // HMR
    hmr: {
      overlay: true
    }
  },
  
  // Preview server
  preview: {
    port: 3001,
    open: true,
    cors: true
  },
  
  // Optimizations
  optimizeDeps: {
    include: ['zustand', 'jspdf'],
    exclude: ['msw', 'msw/node']
  },
  
  // Environment variables
  define: {
    __APP_VERSION__: JSON.stringify(process.env.npm_package_version),
    // Force Phase2 in CI builds
    'import.meta.env.VITE_FORCE_PHASE2': JSON.stringify(process.env.CI === 'true')
  }
});