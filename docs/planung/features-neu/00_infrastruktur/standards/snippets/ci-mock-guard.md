# CI Mock-Guard Configuration

**Ziel:** PR-Check bricht bei Mock-Imports in Business-Logic

## GitHub Actions Setup

**Datei:** `.github/workflows/mock-guard.yml`

```yaml
name: mock-guard
on:
  pull_request:
    branches: [ main ]

jobs:
  scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Install ripgrep
        run: sudo apt-get update -y >/dev/null && sudo apt-get install -y ripgrep >/dev/null

      - name: Frontend mock scan (business logic only)
        run: |
          set -euo pipefail
          PATHS=$(printf "%s\n" \
            "frontend/src/app" \
            "frontend/src/features" \
            "frontend/src/lib" \
            "frontend/src/hooks" \
            "frontend/src/store")
          PATTERN='(import|from).*mock|mockData|__mocks__/|/mocks?/'
          FOUND=""

          for P in $PATHS; do
            if [ -d "$P" ]; then
              HITS=$(rg -n "$PATTERN" "$P" \
                --glob '!**/{__tests__,tests,__mocks__,fixtures,.storybook,storybook}/**' \
                --glob '!**/*.{spec,test,stories}.*' || true)
              if [ -n "$HITS" ]; then
                FOUND="$FOUND\\n$HITS"
              fi
            fi
          done

          if [ -n "$FOUND" ]; then
            echo "❌ Disallowed mock references in business logic:"
            echo -e "$FOUND"
            exit 1
          fi
          echo "✅ No disallowed mocks in business logic."

      - name: Backend guard (no sample data in prod migrations)
        run: |
          set -euo pipefail
          if [ -d "backend/src/main/resources/db/migration" ]; then
            SUSPICIOUS=$(rg -n --glob 'backend/src/main/resources/db/migration/**' \
              '(INSERT|UPDATE).*(Sample|Mock|Demo|Fixture|Beispiel|\[TEST\]|\[DEV\])' || true)
            if [ -n "$SUSPICIOUS" ]; then
              echo "❌ Suspicious sample/mock/demo data in PRODUCTION migrations:"
              echo "$SUSPICIOUS"
              exit 1
            fi
          fi
          echo "✅ No suspicious data in production migrations."
```

## Optional: Integration mit ESLint

```yaml
      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '18'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json

      - name: Install dependencies
        run: cd frontend && npm ci

      - name: ESLint mock-guard
        run: cd frontend && npm run lint:mock-guard
```

## Local Testing

```bash
# Test the ripgrep pattern:
rg -n '(import|from).*mock|mockData' frontend/src/features \
  --glob '!**/{__tests__,tests,__mocks__,fixtures,.storybook,storybook}/**' \
  --glob '!**/*.{spec,test,stories}.*'
```