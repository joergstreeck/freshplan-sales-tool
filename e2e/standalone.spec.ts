import { test, expect } from '@playwright/test';

test.describe('Standalone HTML Version', () => {
  // Test für die standalone HTML-Datei
  test('freshplan-complete.html sollte ohne Server funktionieren', async ({ page }) => {
    // Direkt die HTML-Datei öffnen
    await page.goto('file://' + process.cwd() + '/freshplan-complete.html');
    
    // Prüfe ob die Seite lädt
    await expect(page.locator('.header')).toBeVisible();
    await expect(page.locator('.nav-tabs')).toBeVisible();
    
    // Navigation testen
    await page.click('button[data-tab="customer"]');
    
    // Prüfe ob Tab-Wechsel funktioniert
    const customerPanel = page.locator('#customer');
    await expect(customerPanel).toHaveClass(/active/);
    
    // Neukunden-Funktionalität testen
    await page.selectOption('#customerType', 'neukunde');
    await expect(page.locator('#newCustomerAlert')).toHaveClass(/show/);
    
    // Buttons sollten funktionieren
    await page.click('button:has-text("Bonitätsprüfung anfragen")');
    // Seite sollte nicht abstürzen
    await expect(page.locator('#customerForm')).toBeVisible();
  });

  test('Rabattrechner sollte in Standalone funktionieren', async ({ page }) => {
    await page.goto('file://' + process.cwd() + '/freshplan-complete.html');
    
    // Warte auf Slider
    const orderValueSlider = page.locator('#orderValue');
    await expect(orderValueSlider).toBeVisible();
    
    // Teste Berechnung
    await orderValueSlider.fill('50000');
    
    // JavaScript sollte funktionieren
    await expect(page.locator('#orderValueDisplay')).toContainText('50.000');
    await expect(page.locator('#baseDiscount')).toContainText('10%');
  });
});