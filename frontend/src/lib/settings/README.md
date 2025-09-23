# Settings Module - Frontend Integration

## Sprint 1.3 PR #3 - Frontend Settings Integration (FP-233)

This module provides React hooks and API clients for integrating with the Settings Registry backend.

## Features

- ✅ **ETag Support**: Automatic conditional requests with If-None-Match headers
- ✅ **304 Handling**: Efficient caching with 304 Not Modified responses
- ✅ **React Query Integration**: Built-in caching with staleTime and gcTime
- ✅ **Type Safety**: Full TypeScript support
- ✅ **Fallback Handling**: Graceful degradation on API errors

## Usage

### Theme Settings

```tsx
import { useThemeSettings } from '@/lib/settings';

function MyComponent() {
  const { theme, isLoading, error } = useThemeSettings();

  if (isLoading) return <div>Loading theme...</div>;

  return (
    <div style={{ color: theme.primary }}>
      FreshFoodz themed content
    </div>
  );
}
```

### Feature Flags

```tsx
import { useFeatureFlag } from '@/lib/settings';

function ExperimentalFeature() {
  const { isEnabled, isLoading } = useFeatureFlag('experimental');

  if (!isEnabled) return null;

  return <div>Experimental feature content</div>;
}
```

### Direct API Access

```tsx
import { fetchSetting, resolveSetting } from '@/lib/settings';

// Fetch a specific setting
const setting = await fetchSetting('ui.theme', 'GLOBAL');

// Resolve hierarchically
const resolved = await resolveSetting('tax.config', {
  tenantId: 'freshfoodz',
  territory: 'DE',
});
```

## Performance

- **Cache Hit Rate**: Target ≥70% through ETag validation
- **Network Reduction**: -60% through 304 responses
- **Response Time**: <50ms for cached settings
- **Fallback**: Safe defaults on API errors

## Architecture

1. **API Client** (`api.ts`): Handles HTTP requests with ETag support
2. **React Hooks** (`hooks.ts`): React Query integration
3. **Theme Provider** (`ThemeProvider.tsx`): MUI theme integration
4. **Tests** (`api.test.ts`): Comprehensive test coverage

## Testing

```bash
# Run tests
npm run test -- src/lib/settings

# Check types
npx tsc --noEmit

# Lint
npx eslint src/lib/settings
```

## Configuration

The module uses these default cache settings:
- **staleTime**: 60 seconds
- **gcTime**: 10 minutes
- **retry**: 1 attempt

These can be overridden per-hook if needed.

## Dependencies

- `@tanstack/react-query`: For caching and data fetching
- `@mui/material`: For theme integration
- Backend Settings Registry API (Sprint 1.2 PR #2)