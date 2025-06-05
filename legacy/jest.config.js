export default {
  testEnvironment: 'jsdom',
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/js/$1',
    '\\.css$': '<rootDir>/tests/__mocks__/styleMock.js'
  },
  collectCoverageFrom: [
    'js/**/*.js',
    '!js/main.js'
  ],
  coverageThreshold: {
    global: {
      branches: 80,
      functions: 80,
      lines: 80,
      statements: 80
    }
  },
  testMatch: [
    '<rootDir>/tests/**/*.test.js'
  ]
};