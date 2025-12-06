import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import tseslint from 'typescript-eslint'

export default tseslint.config(
  { ignores: ['dist', 'public/mockServiceWorker.js', 'src/api/generated/**', 'coverage/**'] },
  {
    extends: [js.configs.recommended, ...tseslint.configs.recommended],
    files: ['**/*.{ts,tsx}'],
    languageOptions: {
      ecmaVersion: 2020,
      globals: globals.browser,
    },
    plugins: {
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
    },
    rules: {
      ...reactHooks.configs.recommended.rules,
      'react-refresh/only-export-components': 'off', // Too noisy for CI, dev-only issue
      '@typescript-eslint/no-unused-vars': ['error', {
        argsIgnorePattern: '^_',
        varsIgnorePattern: '^_'
      }],
      '@typescript-eslint/no-explicit-any': 'error',
      'react-hooks/exhaustive-deps': 'error',
      'react-hooks/rules-of-hooks': 'error'
    },
  },
  // Mock-Governance: Business-Logic mock-frei (Test-Dateien ausgenommen)
  {
    files: [
      'src/app/**/*.{ts,tsx}',
      'src/features/**/*.{ts,tsx}',
      'src/lib/**/*.{ts,tsx}',
      'src/hooks/**/*.{ts,tsx}',
      'src/store/**/*.{ts,tsx}',
    ],
    ignores: [
      '**/__tests__/**',
      '**/*.test.{ts,tsx}',
      '**/*.spec.{ts,tsx}',
    ],
    rules: {
      'no-restricted-imports': ['error', {
        patterns: [
          // Nur projektinterne Mock-Pfade verbieten
          { group: ['**/data/mock*', '**/__mocks__/**', '**/mocks/**'], message: 'Mocks in Business-Logic verboten.' }
        ]
      }]
    }
  }
)
