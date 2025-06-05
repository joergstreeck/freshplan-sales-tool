# Test Status

## Aktuelle Situation

Die Tests sind teilweise überarbeitet worden. Folgende Tests funktionieren:

✅ **Funktionierende Tests:**
- Domain Logic Tests (Calculator) - 34 Tests
- EventBus Tests - 17 Tests 
- Integration Tests (teilweise) - 4 von 7 Tests

❌ **Nicht funktionierende Tests:**
- Store Tests - Probleme mit Zustand State Management zwischen Tests
- DOM-abhängige Tests - Benötigen echte DOM-Operationen
- Module Tests - Stark DOM-abhängig

## Probleme

1. **Store State Isolation**: Die Zustand Store Instance wird zwischen Tests geteilt, was zu unerwarteten Zuständen führt
2. **DOM Mocking**: Viele Module sind stark DOM-abhängig und die Mocks sind nicht vollständig genug
3. **Event System**: Events werden nicht immer korrekt zwischen gemockten Modulen propagiert

## Empfehlung

Für eine vollständige Test-Abdeckung sollten folgende Maßnahmen ergriffen werden:

1. **E2E Tests**: Verwenden Sie Tools wie Playwright oder Cypress für echte DOM-Tests
2. **Store Isolation**: Erstellen Sie für jeden Test eine neue Store Instance
3. **Module Refactoring**: Trennen Sie Business Logic von DOM-Manipulation für bessere Testbarkeit

## Test-Befehle

```bash
# Alle Tests ausführen
npm test

# Nur funktionierende Tests
npm test -- src/domain src/core/__tests__/EventBus.test.ts

# Mit Coverage
npm run test:coverage
```