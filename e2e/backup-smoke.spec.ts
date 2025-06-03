import { test, expect } from '@playwright/test';

test.describe('Backup Smoke Tests - Kritische Funktionen', () => {
  test('1. App lädt ohne JS-Fehler', async ({ page }) => {
    const errors: Error[] = [];
    page.on('pageerror', err => errors.push(err));
    
    await page.goto('/');
    await page.waitForLoadState('networkidle');
    
    // Keine JavaScript-Fehler sollten auftreten
    expect(errors).toHaveLength(0);
  });

  test('2. Kritische Module werden initialisiert', async ({ page }) => {
    await page.goto('/?phase2=true');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    
    // Prüfe ob FreshPlanApp existiert und Module geladen sind
    const appState = await page.evaluate(() => {
      const app = (window as any).FreshPlanApp;
      return {
        exists: !!app,
        modulesCount: app?.modules?.size || 0,
        hasCustomerModule: app?.modules?.has('customer') || false
      };
    });
    
    expect(appState.exists).toBeTruthy();
    expect(appState.modulesCount).toBeGreaterThan(0);
    expect(appState.hasCustomerModule).toBeTruthy();
  });

  test('3. Save-Button existiert und ist klickbar', async ({ page }) => {
    await page.goto('/?phase2=true');
    await page.waitForLoadState('networkidle');
    
    // Navigiere zum Customer-Tab
    await page.click('[data-tab="customer"]');
    await page.waitForTimeout(500);
    
    // Prüfe Save-Button
    const saveBtn = page.locator('.header-btn-save');
    await expect(saveBtn).toBeVisible();
    await expect(saveBtn).toBeEnabled();
    
    // Button sollte klickbar sein (ohne tatsächlich zu speichern)
    const isClickable = await saveBtn.evaluate(el => {
      const rect = el.getBoundingClientRect();
      return rect.width > 0 && rect.height > 0 && !el.hasAttribute('disabled');
    });
    expect(isClickable).toBeTruthy();
  });

  test('4. Sprachwechsel funktioniert', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');
    
    // Prüfe ob Sprachauswahl existiert
    const languageSelect = page.locator('#languageSelect');
    await expect(languageSelect).toBeVisible();
    
    // Wechsle zu Englisch
    await page.selectOption('#languageSelect', 'en');
    await page.waitForTimeout(500);
    
    // Prüfe ob UI-Texte geändert wurden
    const saveButtonText = await page.locator('.header-btn-save span').textContent();
    expect(saveButtonText?.toLowerCase()).toContain('save');
    
    // Wechsle zurück zu Deutsch
    await page.selectOption('#languageSelect', 'de');
    await page.waitForTimeout(500);
    
    const saveButtonTextDe = await page.locator('.header-btn-save span').textContent();
    expect(saveButtonTextDe?.toLowerCase()).toContain('speichern');
  });

  test('5. LocalStorage persistiert Daten', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');
    
    // Setze Test-Wert
    await page.evaluate(() => {
      localStorage.setItem('backup-test', 'ok');
      localStorage.setItem('backup-timestamp', new Date().toISOString());
    });
    
    // Seite neu laden
    await page.reload();
    await page.waitForLoadState('networkidle');
    
    // Prüfe ob Werte noch vorhanden sind
    const storageData = await page.evaluate(() => {
      return {
        testValue: localStorage.getItem('backup-test'),
        timestamp: localStorage.getItem('backup-timestamp'),
        totalItems: localStorage.length
      };
    });
    
    expect(storageData.testValue).toBe('ok');
    expect(storageData.timestamp).toBeTruthy();
    expect(storageData.totalItems).toBeGreaterThanOrEqual(2);
    
    // Aufräumen
    await page.evaluate(() => {
      localStorage.removeItem('backup-test');
      localStorage.removeItem('backup-timestamp');
    });
  });
});