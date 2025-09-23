# Husky Pre-commit Hook (Simplified)

**Ziel:** ESLint auf staged TypeScript files vor commit

## Setup

**Install Husky (if not already):**
```bash
cd frontend
npm install --save-dev husky
npx husky install
npm pkg set scripts.prepare="husky install"
```

## Pre-commit Hook

**Datei:** `.husky/pre-commit`

```bash
#!/bin/sh
. "$(dirname "$0")/_/husky.sh"

# Get staged TypeScript files
FILES=$(git diff --cached --name-only --diff-filter=ACMR | grep -E '\\.tsx?$' || true)

# Exit if no TypeScript files staged
[ -z "$FILES" ] && exit 0

echo "🔎 Running ESLint on staged TypeScript files..."

# Run ESLint on staged files
cd frontend
npx eslint --max-warnings 0 $FILES

echo "✅ ESLint passed on staged files."
```

## Alternative: With lint-staged

**Install lint-staged:**
```bash
npm install --save-dev lint-staged
```

**package.json:**
```json
{
  "lint-staged": {
    "*.{ts,tsx}": [
      "eslint --max-warnings 0",
      "prettier --write"
    ]
  }
}
```

**.husky/pre-commit:**
```bash
#!/bin/sh
. "$(dirname "$0")/_/husky.sh"

cd frontend
npx lint-staged
```

## Permissions

```bash
chmod +x .husky/pre-commit
```

## Testing

```bash
# Test the hook manually:
.husky/pre-commit

# Or stage some files and commit:
echo "// test" > src/test.ts
git add src/test.ts
git commit -m "test commit"  # Should trigger hook
```