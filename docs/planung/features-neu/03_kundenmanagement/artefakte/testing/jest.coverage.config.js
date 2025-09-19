// Jest Coverage Configuration für Frontend (Modul 03)
// Foundation Standards: 80%+ Coverage erforderlich

module.exports = {
  // Coverage thresholds
  coverageThreshold: {
    global: {
      lines: 80,
      functions: 80,
      branches: 75,
      statements: 80
    },
    // Spezifische Thresholds für kritische Komponenten
    './src/components/customer/': {
      lines: 85,
      functions: 85,
      branches: 80,
      statements: 85
    },
    './src/routes/': {
      lines: 75,
      functions: 75,
      branches: 70,
      statements: 75
    }
  },

  // Coverage collection
  collectCoverageFrom: [
    'src/**/*.{ts,tsx}',
    '!src/**/*.d.ts',
    '!src/**/*.stories.{ts,tsx}',
    '!src/**/*.test.{ts,tsx}',
    '!src/**/index.ts',
    '!src/main.tsx',
    '!src/vite-env.d.ts'
  ],

  // Coverage reporters
  coverageReporters: [
    'text',
    'text-summary',
    'html',
    'lcov',
    'json-summary'
  ],

  // Coverage directory
  coverageDirectory: 'coverage',

  // Fail on coverage threshold
  coverageThreshold: {
    global: {
      lines: 80,
      functions: 80,
      branches: 75,
      statements: 80
    }
  }
};