import { test, expect } from '@playwright/test';

test.describe('FreshPlan Sales Tool', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    // Wait for app to load
    await page.waitForSelector('#app.loaded', { timeout: 10000 });
  });

  test.describe('Navigation', () => {
    test('sollte alle Tabs anzeigen', async ({ page }) => {
      await expect(page.locator('button[data-tab="demonstrator"]')).toBeVisible();
      await expect(page.locator('button[data-tab="customer"]')).toBeVisible();
      await expect(page.locator('button[data-tab="profile"]')).toBeVisible();
      await expect(page.locator('button[data-tab="offer"]')).toBeVisible();
      await expect(page.locator('button[data-tab="settings"]')).toBeVisible();
    });

    test('sollte zwischen Tabs wechseln können', async ({ page }) => {
      // Zu Kundendaten wechseln
      await page.click('button[data-tab="customer"]');
      await expect(page.locator('#customer.tab-panel')).toBeVisible();
      await expect(page.locator('#demonstrator.tab-panel')).not.toBeVisible();
      
      // Zurück zum Demonstrator
      await page.click('button[data-tab="demonstrator"]');
      await expect(page.locator('#demonstrator.tab-panel')).toBeVisible();
      await expect(page.locator('#customer.tab-panel')).not.toBeVisible();
    });
  });

  test.describe('Rabattrechner', () => {
    test('sollte Rabatte korrekt berechnen', async ({ page }) => {
      // Bestellwert auf 30.000€ setzen
      await page.fill('#orderValue', '30000');
      await page.waitForTimeout(500); // Wait for calculation
      
      // Prüfe Basisrabatt (sollte 8% sein für 30.000€)
      await expect(page.locator('#baseDiscount')).toContainText('8%');
      
      // Vorlaufzeit auf 30 Tage setzen
      await page.fill('#leadTime', '30');
      await page.waitForTimeout(500);
      
      // Prüfe Frühbucherrabatt (sollte 3% sein für 30 Tage)
      await expect(page.locator('#earlyDiscount')).toContainText('3%');
      
      // Selbstabholung aktivieren
      await page.check('#pickupToggle');
      await expect(page.locator('#pickupDiscount')).toContainText('2%');
      
      // Gesamtrabatt prüfen (8% + 3% + 2% = 13%)
      await expect(page.locator('#totalDiscount')).toContainText('13%');
    });

    test('sollte Mindestbestellwert beachten', async ({ page }) => {
      // Bestellwert unter 5.000€
      await page.fill('#orderValue', '3000');
      await page.waitForTimeout(500);
      
      // Basisrabatt sollte 0% sein
      await expect(page.locator('#baseDiscount')).toContainText('0%');
    });

    test('sollte maximalen Rabatt von 15% einhalten', async ({ page }) => {
      // Maximale Werte setzen
      await page.fill('#orderValue', '100000');
      await page.fill('#leadTime', '45');
      await page.check('#pickupToggle');
      await page.waitForTimeout(500);
      
      // Gesamtrabatt sollte auf 15% begrenzt sein
      await expect(page.locator('#totalDiscount')).toContainText('15%');
    });
  });

  test.describe('Neukunden-Check', () => {
    test.beforeEach(async ({ page }) => {
      // Zu Kundendaten Tab wechseln
      await page.click('button[data-tab="customer"]');
      await page.waitForSelector('#customer.tab-panel', { state: 'visible' });
    });

    test('sollte Warnung bei Neukunde anzeigen', async ({ page }) => {
      // Neukunde auswählen
      await page.selectOption('#customerType', 'neukunde');
      
      // Warnung sollte erscheinen
      await expect(page.locator('#newCustomerAlert')).toBeVisible();
      await expect(page.locator('#newCustomerAlert')).toContainText('Neukunde - Zahlungsart beachten!');
    });

    test('sollte keine Warnung bei Bestandskunde anzeigen', async ({ page }) => {
      // Erst Neukunde auswählen
      await page.selectOption('#customerType', 'neukunde');
      await expect(page.locator('#newCustomerAlert')).toBeVisible();
      
      // Dann zu Bestandskunde wechseln
      await page.selectOption('#customerType', 'bestandskunde');
      await expect(page.locator('#newCustomerAlert')).not.toBeVisible();
    });

    test('sollte Bonitätsprüfung-Button funktionieren', async ({ page }) => {
      await page.selectOption('#customerType', 'neukunde');
      
      // Button sollte sichtbar sein
      const creditCheckButton = page.locator('#requestCreditCheck');
      await expect(creditCheckButton).toBeVisible();
      
      // Button klicken (sollte nicht abstürzen)
      await creditCheckButton.click();
      
      // Seite sollte noch funktionieren
      await expect(page.locator('#customerForm')).toBeVisible();
    });

    test('sollte Management-Anfrage-Button funktionieren', async ({ page }) => {
      await page.selectOption('#customerType', 'neukunde');
      
      // Button sollte sichtbar sein
      const managementButton = page.locator('#requestManagement');
      await expect(managementButton).toBeVisible();
      
      // Button klicken
      await managementButton.click();
      
      // Seite sollte noch funktionieren
      await expect(page.locator('#customerForm')).toBeVisible();
    });
  });

  test.describe('Formular-Funktionen', () => {
    test.beforeEach(async ({ page }) => {
      await page.click('button[data-tab="customer"]');
      await page.waitForSelector('#customer.tab-panel', { state: 'visible' });
    });

    test('sollte Formular leeren können', async ({ page }) => {
      // Einige Felder ausfüllen
      await page.fill('#companyName', 'Test GmbH');
      await page.selectOption('#legalForm', 'gmbh');
      
      // Dialog bestätigen
      page.on('dialog', dialog => dialog.accept());
      
      // Formular leeren Button klicken
      await page.click('#clearForm');
      
      // Felder sollten leer sein
      await expect(page.locator('#companyName')).toHaveValue('');
      await expect(page.locator('#legalForm')).toHaveValue('');
    });

    test('sollte Bonitätsprüfung Status anzeigen', async ({ page }) => {
      // Status-Elemente sollten existieren
      await expect(page.locator('#creditStatus')).toBeVisible();
      await expect(page.locator('#creditLimit')).toBeVisible();
      await expect(page.locator('#creditRating')).toBeVisible();
      
      // Initial-Status prüfen
      await expect(page.locator('#creditStatus')).toContainText('Nicht geprüft');
    });
  });

  test.describe('Responsive Design', () => {
    test('sollte auf mobilen Geräten funktionieren', async ({ page }) => {
      // Viewport auf mobile Größe setzen
      await page.setViewportSize({ width: 375, height: 667 });
      
      // Navigation sollte sichtbar sein
      await expect(page.locator('.nav-tabs')).toBeVisible();
      
      // Tab wechseln sollte funktionieren
      await page.click('button[data-tab="customer"]');
      await expect(page.locator('#customer.tab-panel')).toBeVisible();
    });
  });
});