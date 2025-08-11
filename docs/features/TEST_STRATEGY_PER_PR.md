# 🧪 Test-Strategie für Code Quality PRs

**Erstellt:** 12.08.2025  
**Prinzip:** Jede PR braucht Tests, die die Änderungen validieren und Regressionen verhindern

## 📋 Übersicht Test-Strategien pro PR

| PR | Fokus | Test-Strategie | Coverage-Ziel |
|----|-------|----------------|---------------|
| #1 | Security & MSW | Unit + Integration | 100% für Security-Code |
| #2 | Console Cleanup | Automated Verification | 100% (keine console.*) |
| #3 | TypeScript Arrays | Type-Check + Unit | 95% Type Coverage |
| #4 | Event Handlers | Type-Check + Integration | 90% Event Coverage |
| #5 | Component Refactor | Unit + Snapshot | Maintain 89% |
| #6 | Service Refactor | Unit + Integration | Increase to 92% |

---

## 🔒 PR #1: Security & MSW Hardening

### Test-Suite: `msw-token.test.ts`
```typescript
describe('MSW Token Security', () => {
  it('should NOT set token when MSW is disabled')
  it('should set token ONLY when MSW is explicitly enabled')
  it('should remove token when MSW is disabled after being enabled')
  it('should validate token format when set')
  it('should not expose token in console or network')
});
```

### Test-Suite: `EnvironmentConfigTest.java`
```java
@Test void shouldNotHaveHardcodedCredentials()
@Test void shouldHaveEnvExampleFile()
@Test void shouldLoadFromEnvironment()
@Test void shouldFailWithoutRequiredEnvVars()
```

### E2E Security Test:
```typescript
describe('Security E2E', () => {
  it('should not leak credentials in docker-compose')
  it('should require .env file for startup')
  it('should mask passwords in logs')
});
```

**Metriken:**
- 0 hardcoded credentials
- 100% environment variable coverage
- 0 security warnings from npm audit

---

## 🧹 PR #2: Console Cleanup

### Test-Suite: `no-console.test.ts`
```typescript
describe('Console Statement Verification', () => {
  it('should have NO console statements in production code')
  it('should use logger instead of console in services')
  it('should allow console in test files only')
  it('should not affect debugging capabilities')
});
```

### Performance Test:
```typescript
describe('Performance after Console Removal', () => {
  it('should reduce bundle size by at least 1%')
  it('should improve initial load time')
  it('should not break error reporting')
});
```

### CI Integration:
```yaml
- name: Verify No Console
  run: |
    npm run test:no-console
    # Fail if any console found
    ! grep -r "console\." src/ --include="*.ts" --include="*.tsx" --exclude-dir=__tests__
```

**Metriken:**
- 0 console.* in src/
- Bundle size reduction ≥ 1%
- All existing tests still pass

---

## 🔧 PR #3: TypeScript Array Types

### Test-Suite: `type-safety.test.ts`
```typescript
describe('TypeScript Type Safety', () => {
  it('should not have any[] in production code')
  it('should use specific types for common arrays')
  it('should compile with --strict flag')
  it('should have proper type inference')
});
```

### Compile-Time Tests:
```bash
# Strict Type Checking
npx tsc --noEmit --strict

# No Implicit Any
npx tsc --noEmit --noImplicitAny

# Strict Null Checks
npx tsc --noEmit --strictNullChecks
```

### Type Coverage Report:
```bash
# Using type-coverage tool
npx type-coverage --at-least 95

# Should output:
# 95.2% type coverage
# 1621 anys / 34567 total
```

**Metriken:**
- < 5 any[] remaining
- Type coverage > 95%
- 0 TypeScript errors with --strict

---

## 🎯 PR #4: Event Handler Types

### Test-Suite: `event-handlers.test.tsx`
```typescript
describe('Event Handler Types', () => {
  it('onClick handlers should have MouseEvent type')
  it('onChange handlers should have ChangeEvent type')
  it('onSubmit handlers should have FormEvent type')
  it('custom handlers should have proper signatures')
});
```

### Integration Tests:
```typescript
describe('Event Handler Integration', () => {
  it('should handle button clicks with proper types')
  it('should handle form submissions with validation')
  it('should propagate events correctly')
  it('should prevent default when needed')
});
```

### React Testing Library Tests:
```typescript
it('should type event in handler correctly', async () => {
  const handleClick = vi.fn((e: React.MouseEvent<HTMLButtonElement>) => {
    expect(e.currentTarget.tagName).toBe('BUTTON');
  });
  
  render(<Button onClick={handleClick}>Click</Button>);
  await userEvent.click(screen.getByRole('button'));
  
  expect(handleClick).toHaveBeenCalledWith(
    expect.objectContaining({
      type: 'click',
      currentTarget: expect.any(HTMLButtonElement)
    })
  );
});
```

**Metriken:**
- 100% typed event handlers
- 0 any in event signatures
- All event tests pass

---

## 🏗️ PR #5: Component Refactoring

### Test-Suite: Component Tests
```typescript
describe('Refactored Components', () => {
  // For each refactored component:
  it('should maintain same public API')
  it('should pass all existing tests')
  it('should improve performance')
  it('should reduce complexity')
});
```

### Snapshot Tests:
```typescript
describe('Component Snapshots', () => {
  it('should match snapshot for SalesCockpitHeader')
  it('should match snapshot for SalesCockpitGrid')
  // etc. for all new sub-components
});
```

### Performance Tests:
```typescript
describe('Component Performance', () => {
  it('should render faster than before')
  it('should have fewer re-renders')
  it('should use less memory')
});
```

**Metriken:**
- All components < 250 lines
- Maintain 89% test coverage
- Performance improvement ≥ 10%

---

## 🔨 PR #6: Service Refactoring

### Test-Suite: Service Tests
```java
@Test
class RefactoredServiceTests {
  @Test void shouldMaintainBackwardsCompatibility()
  @Test void shouldImproveQueryPerformance()
  @Test void shouldReduceCyclomaticComplexity()
  @Test void shouldFollowCQRSPattern()
}
```

### Integration Tests:
```java
@QuarkusIntegrationTest
class ServiceIntegrationTests {
  @Test void shouldHandleCommandsCorrectly()
  @Test void shouldHandleQueriesEfficiently()
  @Test void shouldMaintainTransactionBoundaries()
}
```

### Performance Benchmarks:
```java
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class ServiceBenchmark {
  @Benchmark
  public void measureQueryPerformance() { }
  
  @Benchmark
  public void measureCommandPerformance() { }
}
```

**Metriken:**
- All services < 250 lines
- Test coverage > 92%
- Query performance improvement ≥ 20%

---

## 🚀 Continuous Testing Strategy

### Pre-Commit Hooks:
```bash
#!/bin/bash
# .husky/pre-commit

# Run type checks
npm run type-check

# Run relevant tests
npm test -- --related --passWithNoTests

# Check for console statements
! grep -r "console\." src/ --include="*.ts" --include="*.tsx"
```

### CI Pipeline Tests:
```yaml
test-matrix:
  strategy:
    matrix:
      test-suite:
        - unit
        - integration
        - type-safety
        - security
        - performance
```

### Regression Prevention:
1. **Automated Tests** run on every commit
2. **Type Checks** prevent any regressions
3. **Performance Benchmarks** track improvements
4. **Security Scans** ensure no vulnerabilities
5. **Coverage Reports** maintain/improve coverage

---

## 📊 Success Metrics

### Overall Goals:
- **Test Coverage:** 89% → 95%
- **Type Coverage:** 85% → 98%
- **Performance:** +20% improvement
- **Security:** 0 vulnerabilities
- **Code Quality:** 0 ESLint errors (maintained)

### Per-PR Validation:
Each PR must:
1. ✅ Have dedicated test suite
2. ✅ Pass all existing tests
3. ✅ Improve or maintain coverage
4. ✅ Include regression tests
5. ✅ Document test strategy

---

## 🎯 Test Execution Commands

### Quick Test Suite:
```bash
# For each PR
npm test -- --run --reporter=verbose <test-file>

# Type checking
npm run type-check

# Lint checking
npm run lint

# Build verification
npm run build
```

### Full Test Suite:
```bash
# Complete test run
npm test -- --run --coverage

# E2E tests
npm run test:e2e

# Performance tests
npm run test:perf

# Security audit
npm audit
```

---

**Wichtig:** Jede PR muss ihre eigene Test-Suite haben, die:
1. Die spezifischen Änderungen validiert
2. Regressionen verhindert
3. Als Dokumentation der Änderungen dient
4. In CI/CD automatisch läuft