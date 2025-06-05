# E2E Test Guide für FreshPlan Sales Tool

## Überblick

Wir haben jetzt End-to-End (E2E) Tests mit Playwright eingerichtet, die sicherstellen, dass die Anwendung im Browser korrekt funktioniert.

## Was die E2E-Tests prüfen:

### 1. **Navigation**
- Alle Tabs sind sichtbar
- Tab-Wechsel funktioniert
- Aktive Tab-Markierung

### 2. **Rabattrechner**
- Korrekte Rabattberechnung
- Mindestbestellwert (5.000€)
- Maximaler Rabatt (15%)
- Slider-Funktionalität

### 3. **Neukunden-Check**
- Warnung erscheint bei "Neukunde"
- Warnung verschwindet bei "Bestandskunde"
- Bonitätsprüfung-Button funktioniert
- Management-Anfrage-Button funktioniert

### 4. **Formular-Funktionen**
- Formular leeren
- Bonitätsprüfung Status-Anzeige
- Validierung

### 5. **Responsive Design**
- Mobile Ansicht funktioniert

## Tests ausführen:

```bash
# Alle E2E-Tests ausführen (headless)
npm run test:e2e

# Tests mit UI ausführen (empfohlen für Debugging)
npm run test:e2e:ui

# Tests im Browser sichtbar ausführen
npm run test:e2e:headed

# Einzelnen Test debuggen
npm run test:e2e:debug
```

## Standalone-Version testen:

Die Tests prüfen auch, ob `freshplan-complete.html` ohne Server funktioniert:

```bash
# Nur Standalone-Tests
npx playwright test standalone.spec.ts
```

## Bei Fehlern:

1. **Screenshots**: Playwright erstellt automatisch Screenshots bei Fehlern im `test-results/` Ordner

2. **Trace Viewer**: Bei Fehlern wird ein Trace erstellt:
```bash
npx playwright show-trace trace.zip
```

3. **Debug-Modus**: Pausiert den Test und erlaubt Inspektion:
```bash
npm run test:e2e:debug
```

## Neue Tests hinzufügen:

```typescript
// e2e/mein-neuer-test.spec.ts
test('sollte neue Funktion testen', async ({ page }) => {
  await page.goto('/');
  await page.click('#meinButton');
  await expect(page.locator('#ergebnis')).toBeVisible();
});
```

## CI/CD Integration:

Die Tests sind bereit für CI/CD-Pipelines:

```yaml
# .github/workflows/test.yml
- name: Run E2E tests
  run: npm run test:e2e
```

## Warum das Probleme verhindert:

1. **Echte Browser-Tests**: Tests laufen in echten Browsern (Chrome, Firefox)
2. **JavaScript-Ausführung**: Prüft ob JS geladen und ausgeführt wird
3. **Event-Handler**: Testet ob Buttons und Interaktionen funktionieren
4. **Visuelle Regression**: Kann erweitert werden um Design-Änderungen zu erkennen

## Best Practices:

1. **Vor jedem Release**: `npm run test:e2e` ausführen
2. **Bei neuen Features**: E2E-Test schreiben
3. **Bei Bugs**: Erst Test schreiben, dann Bug fixen
4. **Regelmäßig**: Tests auch auf verschiedenen Browsern ausführen