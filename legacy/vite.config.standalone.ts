import { defineConfig } from 'vite';
import path from 'path';
import { viteSingleFile } from 'vite-plugin-singlefile';

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
    outDir: 'dist-standalone',
    emptyOutDir: true,
    sourcemap: false,
    
    rollupOptions: {
      input: {
        main: path.resolve(__dirname, 'index.html')
      },
      output: {
        inlineDynamicImports: true,
        entryFileNames: '[name].js',
        chunkFileNames: '[name].js',
        assetFileNames: '[name].[ext]'
      }
    },
    
    // Optimization for single file
    assetsInlineLimit: 100000000, // 100MB - inline all assets
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: false,
        drop_debugger: false
      }
    },
    
    // Don't split CSS
    cssCodeSplit: false,
    cssMinify: true
  },
  
  // Plugins
  plugins: [
    viteSingleFile({
      removeViteModuleLoader: true,
      deleteInlinedFiles: true
    })
  ],
  
  // Environment variables
  define: {
    __APP_VERSION__: JSON.stringify(process.env.npm_package_version)
  }
});