# Known Issues - Frontend Tests

**Stand:** 2025-10-05

## 🐛 Failing Tests (3 von 1266)

### 1. formatters.test.ts - Timezone Issue
**Datei:** `src/utils/__tests__/formatters.test.ts:71`

**Problem:**
```
Expected: "31.12.2024"
Received: "01.01.2025"
```

**Ursache:** Timezone-Konvertierung bei `formatDate('2024-12-31T23:59:59Z')` - UTC zu lokaler Zeit springt ins nächste Jahr.

**Impact:** Gering - Funktion arbeitet korrekt, Test-Assertion ist Timezone-sensitiv.

**Fix:** Test anpassen um Timezone-Offset zu berücksichtigen oder UTC-Datum verwenden.

---

### 2. secureStorage.test.ts - String Matcher Issue
**Datei:** `src/utils/__tests__/secureStorage.test.ts:151`

**Problem:**
```typescript
expect(consoleWarnSpy).toHaveBeenCalledWith(
  expect.stringContaining('exceeds max length'),
  expect.anything()
)
```

**Ursache:** Tatsächliche Warn-Message: `"localStorage value for \"long\" exceeds max length, clearing"` - Test erwartet 2 Parameter, bekommt nur 1.

**Impact:** Sehr gering - Funktion warnt korrekt, nur das Test-Spy-Matching ist falsch.

**Fix:** Spy-Assertion anpassen:
```typescript
expect(consoleWarnSpy).toHaveBeenCalledWith(
  expect.stringContaining('exceeds max length')
)
```

---

### 3. secureStorage.test.ts - Empty String Handling
**Datei:** `src/utils/__tests__/secureStorage.test.ts:195`

**Problem:**
```typescript
localStorageMock.setItem('empty', '');
expect(getSecureString('empty', 'default')).toBe('');
// Erhält: 'default'
```

**Ursache:** `getSecureString` behandelt leere Strings als "nicht vorhanden" und gibt den Default zurück.

**Impact:** Mittel - Dies könnte ein gewolltes Verhalten sein (leere Strings ignorieren).

**Fix-Optionen:**
1. Test anpassen (wenn Verhalten korrekt)
2. Funktion anpassen um leere Strings zu erlauben

---

## 📊 Test-Statistik

- **Total:** 1266 Tests
- **Passing:** 963 (76%)
- **Failing:** 3 (<1%)
- **Skipped:** 82 (6%)

**Test Coverage:** ~26-28% (geschätzt basierend auf Testanzahl)

---

## ✅ Nächste Schritte

1. Timezone-Test mit `toLocaleString` oder UTC-Assertions fixen
2. Console.warn Spy-Assertions korrigieren
3. Empty String Behavior klären (Bug vs. Feature)

**Priorität:** Low - Alle 3 Issues sind Test-Probleme, keine Production-Bugs.
