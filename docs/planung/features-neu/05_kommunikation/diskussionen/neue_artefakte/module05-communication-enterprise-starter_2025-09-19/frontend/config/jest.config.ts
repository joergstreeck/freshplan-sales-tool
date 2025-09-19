export default {
  testEnvironment: 'jsdom',
  collectCoverage: true,
  collectCoverageFrom: ['src/**/*.{ts,tsx}', '!src/**/__tests__/**'],
  coverageThreshold: {
    global: { branches: 0.75, functions: 0.8, lines: 0.8, statements: 0.8 }
  }
};
