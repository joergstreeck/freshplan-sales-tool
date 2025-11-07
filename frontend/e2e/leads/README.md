# LeadWizard E2E Tests

## Übersicht

Diese E2E-Tests decken die 9 Szenarien ab, die aus den Integration Tests migriert wurden, weil `@testing-library/react` nicht mit MUI Autocomplete-Komponenten interagieren kann.

## Test-Dateien

### `lead-wizard-complete-flow.spec.ts`

**Coverage:**
- ✅ Stage 0: Required fields + Submit button activation
- ✅ Erstkontakt Logic: MESSE/TELEFON vs WEB_FORMULAR
- ✅ Stage 1: Contact Person fields + validation
- ✅ Multi-Stage Navigation: Stage 0 → Stage 1 → Back
- ✅ API Integration: Payload validation

**9 migrated tests:**
1. `should enable submit button when required fields are filled`
2. `should show Erstkontakt fields for MESSE source`
3. `should NOT show Erstkontakt fields for WEB_FORMULAR source`
4. `should display contact person fields in Stage 1`
5. `should require at least email OR phone for contact person`
6. `should submit Stage 0 data successfully`
7. `should include stage number in API payload`
8. `should navigate from Stage 0 to Stage 1`
9. `should show Zurück button in Stage 1`

## ✅ Implementation Details

**Wizard öffnen:**
- Button: `"Lead erfassen"` auf `/leads` Seite
- Dialog: Öffnet LeadWizard als Modal

**Feldstruktur (Server-Driven UI):**
- ✅ Felder haben **keine `name` Attribute**
- ✅ Selektion via **Labels** (z.B. `page.getByLabel(/firmenname.*\*/i)`)
- ✅ Autocomplete "Quelle": MUI Autocomplete mit Label-Selektion
- ✅ Dynamisches Rendering aus Backend-Schema (`field.fieldKey`)

## Lokale Ausführung

### 1. Build erstellen (benötigt für E2E)
```bash
npm run build
```

### 2. E2E-Tests ausführen
```bash
# Alle E2E-Tests (headless)
npm run test:e2e

# Nur LeadWizard-Tests
npx playwright test e2e/leads/lead-wizard-complete-flow.spec.ts

# Mit UI (interaktiv)
npm run test:e2e:ui

# Nur Chrome
npx playwright test --project=chromium

# Debug-Modus
npx playwright test --debug
```

### 3. Test-Reports
```bash
# HTML-Report öffnen (nach Test-Run)
npx playwright show-report
```

## CI-Integration

Die E2E-Tests sind bereits in der CI-Pipeline konfiguriert (siehe `playwright.config.ts`):
- **Chromium + Firefox**: Beide Browser werden getestet
- **Retries**: 1 Retry bei Fehlern
- **Workers**: 2 parallel (CI), unbegrenzt (lokal)
- **Timeout**: 30s pro Test, 3s pro Assertion

## API-Mocking

Alle Backend-APIs werden gemockt (siehe `mockLeadAPIs()` in der Test-Datei):
- ✅ `/api/leads/schema` - Server-Driven UI Schema
- ✅ `/api/enums/lead-sources` - Enum-Optionen
- ✅ `/api/enums/business-types` - Enum-Optionen
- ✅ `/api/enums/kitchen-sizes` - Enum-Optionen
- ✅ `/api/leads` (POST) - Lead-Erstellung

## Debugging

**Test failed? Folge dieser Checkliste:**

1. **Screenshots prüfen:** `test-results/` Ordner nach Screenshots durchsuchen
2. **HTML-Report:** `npx playwright show-report` für detaillierte Fehlerinfos
3. **Debug-Mode:** `npx playwright test --debug` für Step-by-Step Debugging
4. **Selector prüfen:** Playwright Inspector zeigt, welche Selektoren funktionieren
5. **API-Mocking:** Browser DevTools Network-Tab prüfen (mit `--headed` Flag)

## Nächste Schritte

Nach erfolgreichem Test-Run:
1. ✅ Integration Tests: Alle Tests sollten 6 PASS / 9 SKIPPED zeigen
2. ✅ E2E Tests: Alle 9 Tests sollten GRÜN sein
3. ✅ CI-Pipeline: Sollte beide Test-Suiten erfolgreich ausführen
4. 📊 Coverage: E2E-Tests ergänzen Integration Tests für Vollständigkeit

## Troubleshooting

### Problem: "button:has-text('Lead erfassen')" nicht gefunden
**Lösung:** Stelle sicher, dass du auf `/leads` bist und der Button sichtbar ist.

### Problem: "getByLabel('Firmenname *')" nicht gefunden
**Lösung:** Felder werden dynamisch aus Schema generiert. Prüfe ob Backend `/api/leads/schema` korrekt antwortet.

### Problem: Tests timeout nach 30s
**Lösung:** Backend-Mock prüfen. Playwright wartet auf API-Response. `await page.waitForLoadState('networkidle')` kann helfen.

### Problem: Autocomplete-Interaktion funktioniert nicht
**Lösung:** Playwright kann MUI Autocomplete handhaben. Pattern:
```typescript
await page.click('input[name="source"]'); // Autocomplete öffnen
await page.click('li[role="option"]:has-text("Messe")'); // Option auswählen
```

---

**Migration von Integration → E2E abgeschlossen ✅**
- Integration Tests: 6 aktive, 9 skipped (MUI Autocomplete Limitation)
- E2E Tests: 9 neu (decken alle skipped Szenarien ab)
- **Kein Coverage-Verlust**, nur Verschiebung zur E2E-Ebene
