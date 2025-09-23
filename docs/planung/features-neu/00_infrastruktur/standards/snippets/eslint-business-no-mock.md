# ESLint Mock-Governance Configuration

**Ziel:** Mock-Imports in Business-Logic-Pfaden verbieten

## Frontend ESLint Setup

**Datei:** `frontend/.eslintrc.cjs`

```js
module.exports = {
  // ... existing config ...
  overrides: [
    // ... existing overrides ...
    {
      // Business-Logic-Pfade: hier sind Mocks verboten
      files: [
        'src/app/**/*.{ts,tsx}',
        'src/features/**/*.{ts,tsx}',
        'src/lib/**/*.{ts,tsx}',
        'src/hooks/**/*.{ts,tsx}',
        'src/store/**/*.{ts,tsx}'
      ],
      excludedFiles: [
        '**/{__tests__,tests,__mocks__,fixtures,.storybook,storybook}/**',
        '**/*.{spec,test,stories}.*'
      ],
      rules: {
        'no-restricted-imports': ['error', {
          patterns: [{
            group: ['**/mock*', '**/*Mock*', '**/mocks/**', '**/data/mock*'],
            message: 'Mock-Daten in Business-Logic verboten. Bitte echte APIs/Seeds/Test-Factories nutzen.'
          }]
        }]
      }
    }
  ]
};
```

## Validation

**Test die Regel:**
```bash
# Sollte Fehler werfen:
echo "import { mockData } from './mockData';" > src/features/test.ts
npx eslint src/features/test.ts

# Sollte OK sein:
echo "import { mockData } from './mockData';" > src/features/__tests__/test.spec.ts
npx eslint src/features/__tests__/test.spec.ts
```

## Integration in package.json

```json
{
  "scripts": {
    "lint": "eslint src --ext .ts,.tsx",
    "lint:fix": "eslint src --ext .ts,.tsx --fix",
    "lint:mock-guard": "eslint src/app src/features src/lib src/hooks src/store --ext .ts,.tsx --max-warnings 0"
  }
}
```