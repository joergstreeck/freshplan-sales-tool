# 🧪 Frontend Testing Guide

## 🚀 Quick Start

### Lokale Development

```bash
# Interaktive UI mit Live-Coverage (Empfohlen!)
npm run test:ui

# Quick Coverage-Check + HTML-Report
npm run test:coverage
open coverage/index.html

# Watch-Mode mit Coverage
npm run test:watch

# Einmalige Tests (CI-Mode)
npm run test:ci
```

## 🎯 Vitest UI (Beste Developer Experience)

**Start:**
```bash
npm run test:ui
```

**Features:**
- 🔥 Live-Updates beim Code-Ändern
- 📊 Coverage-Heatmaps direkt im Code
- 🔍 Filtert Tests nach Name/File/Status
- 🐛 Zeigt Failures übersichtlich mit Stack Traces
- ⚡ Re-run einzelner Tests mit einem Klick

**Browser öffnet sich automatisch:** http://localhost:51204/__vitest__/

## 📊 Coverage-Report

**HTML-Report generieren:**
```bash
npm run test:coverage
open coverage/index.html
```

**Was zeigt der Report?**
- ✅ Grün: Zeilen sind getestet
- 🟡 Gelb: Branches teilweise getestet
- 🔴 Rot: Zeilen/Branches nicht getestet
- Prozentsätze pro Datei und Gesamt

## 📝 Test-Struktur

```
frontend/src/
├── components/
│   └── __tests__/          # Component tests
├── utils/
│   └── __tests__/          # Utility tests
├── features/
│   └── [feature]/
│       └── __tests__/      # Feature-specific tests
└── test/
    ├── setup.tsx           # Global test setup
    └── mocks/              # Test mocks
```

## ✍️ Test schreiben

### Unit Test (Utility)

```typescript
// src/utils/__tests__/validation.test.ts
import { describe, it, expect } from 'vitest';
import { isValidEmail } from '../validation';

describe('isValidEmail', () => {
  it('should accept valid email', () => {
    expect(isValidEmail('test@example.com')).toBe(true);
  });

  it('should reject invalid email', () => {
    expect(isValidEmail('invalid')).toBe(false);
  });
});
```

### Component Test

```typescript
// src/components/__tests__/Button.test.tsx
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Button } from '../Button';

describe('Button', () => {
  it('should call onClick when clicked', async () => {
    const user = userEvent.setup();
    const onClick = vi.fn();

    render(<Button onClick={onClick}>Click me</Button>);

    await user.click(screen.getByRole('button'));

    expect(onClick).toHaveBeenCalledOnce();
  });
});
```

### Integration Test (mit React Query)

```typescript
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { renderHook, waitFor } from '@testing-library/react';
import { useCustomers } from '../hooks/useCustomers';

describe('useCustomers', () => {
  it('should fetch customers', async () => {
    const queryClient = new QueryClient();
    const wrapper = ({ children }) => (
      <QueryClientProvider client={queryClient}>
        {children}
      </QueryClientProvider>
    );

    const { result } = renderHook(() => useCustomers(), { wrapper });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toHaveLength(5);
  });
});
```

## 🎭 Mocking

### Mock API Calls

```typescript
import { vi } from 'vitest';
import { httpClient } from '@/shared/lib/apiClient';

vi.mock('@/shared/lib/apiClient', () => ({
  httpClient: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

// In Test:
vi.mocked(httpClient.get).mockResolvedValue({
  data: { id: 1, name: 'Test' },
  status: 200,
  statusText: 'OK',
});
```

### Mock i18n

```typescript
vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => key, // Returns translation key
    i18n: { language: 'de' },
  }),
}));
```

## 🐛 Debugging

### Run single test file

```bash
npm test src/utils/__tests__/validation.test.ts
```

### Run tests matching pattern

```bash
npm test -- --grep "email"
```

### Show console.log in tests

```bash
npm test -- --reporter=verbose
```

### Debug in VS Code

`.vscode/launch.json`:
```json
{
  "type": "node",
  "request": "launch",
  "name": "Debug Vitest Tests",
  "runtimeExecutable": "npm",
  "runtimeArgs": ["run", "test"],
  "console": "integratedTerminal",
  "internalConsoleOptions": "neverOpen"
}
```

## 📏 Coverage-Ziele

- **Utilities:** 100% (pure functions)
- **Business Logic:** ≥85%
- **Components:** ≥70%
- **Integration Tests:** Critical flows

**Regel:** PRs dürfen Coverage nicht senken!

## 🔧 Troubleshooting

**Test timeout:**
```typescript
it('slow test', async () => {
  // ...
}, 10000); // 10 seconds timeout
```

**Portal/Dialog not found:**
```typescript
import { waitFor } from '@testing-library/react';

await waitFor(() => {
  expect(screen.getByRole('dialog')).toBeInTheDocument();
});
```

**Act() warnings:**
```typescript
import { act } from '@testing-library/react';

await act(async () => {
  await user.click(button);
});
```

## 📚 Docs & Resources

- **Vitest:** https://vitest.dev
- **Testing Library:** https://testing-library.com/react
- **User Event:** https://testing-library.com/docs/user-event/intro
- **Codecov Setup:** [../docs/CODECOV_SETUP.md](../docs/CODECOV_SETUP.md)

---

**Happy Testing!** 🎉
