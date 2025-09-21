import { defineConfig, splitVendorChunkPlugin } from 'vite'
import react from '@vitejs/plugin-react'

// Foundation: MUI v5, Theme V2, route/feature/vendor splits
export default defineConfig({
  build: {
    sourcemap: false,
    target: ['es2022'],
    cssTarget: 'chrome100',
    minify: 'esbuild',
    rollupOptions: {
      output: {
        manualChunks(id) {
          // Route/feature boundaries (modules 01..08)
          if (id.includes('/src/modules/01-cockpit/')) return 'mod01-cockpit'
          if (id.includes('/src/modules/02-neukundengewinnung/')) return 'mod02-leads'
          if (id.includes('/src/modules/03-kundenmanagement/')) return 'mod03-customers'
          if (id.includes('/src/modules/04-auswertungen/')) return 'mod04-reports'
          if (id.includes('/src/modules/05-kommunikation/')) return 'mod05-comm'
          if (id.includes('/src/modules/06-einstellungen/')) return 'mod06-settings'
          if (id.includes('/src/modules/07-hilfe/')) return 'mod07-help'
          if (id.includes('/src/modules/08-administration/')) return 'mod08-admin'
          // Heavy libs: charts/editor only when needed
          if (id.includes('chart.js') || id.includes('echarts')) return 'charts'
          if (id.includes('slate') || id.includes('quill')) return 'editor'
          // MUI in eigenen Chunk (tree-shakable)
          if (id.includes('@mui/')) return 'mui'
          // Date/utility libs
          if (id.includes('date-fns') || id.includes('dayjs')) return 'date'
          if (id.includes('lodash') || id.includes('lodash-es')) return 'lodash'
        },
        chunkFileNames: 'assets/[name]-[hash].js',
        entryFileNames: 'assets/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash][extname]'
      }
    }
  },
  plugins: [react(), splitVendorChunkPlugin()]
})
