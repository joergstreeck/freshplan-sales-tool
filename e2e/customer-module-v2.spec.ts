import { test, expect } from '@playwright/test';

test.describe('CustomerModuleV2 E2E Tests', () => {
  // Test mit Phase 2 aktiviert
  test.beforeEach(async ({ page }) => {
    await page.goto('/?phase2=true');
    
    // Warte auf App-Initialisierung
    await page.waitForSelector('#customer', { state: 'attached' });
    
    // Navigiere zum Kundendaten-Tab
    await page.click('[data-tab="customer"]');
    await page.waitForSelector('#customerForm', { state: 'visible' });
  });

  test.describe('Formular-Validierung', () => {
    test('sollte Pflichtfelder validieren', async ({ page }) => {
      // Versuche mit leerem Formular zu speichern
      await page.click('.header-btn-save');
      
      // Erwarte Validierungsfehler
      const alertDialog = page.waitForEvent('dialog');
      const dialog = await alertDialog;
      expect(dialog.message()).toContain('Pflichtfelder');
      await dialog.accept();
    });

    test('sollte E-Mail-Format validieren', async ({ page }) => {
      // Fülle alle Felder außer E-Mail korrekt
      await page.fill('#companyName', 'Test GmbH');
      await page.selectOption('#legalForm', 'gmbh');
      await page.selectOption('#customerType', 'single');
      await page.selectOption('#customerStatus', 'neukunde');
      await page.selectOption('#industry', 'hotel');
      await page.fill('#street', 'Teststraße 1');
      await page.fill('#postalCode', '12345');
      await page.fill('#city', 'Berlin');
      await page.fill('#contactName', 'Max Mustermann');
      await page.fill('#contactPhone', '0123456789');
      await page.fill('#contactEmail', 'ungueltige-email'); // Ungültig
      await page.fill('#expectedVolume', '50000');
      await page.selectOption('#paymentMethod', 'vorkasse');
      
      // Speichern
      await page.click('.header-btn-save');
      
      // Erwarte Validierungsfehler
      const alertDialog = page.waitForEvent('dialog');
      const dialog = await alertDialog;
      expect(dialog.message()).toContain('E-Mail');
      await dialog.accept();
    });

    test('sollte PLZ-Format validieren', async ({ page }) => {
      // Fülle alle Felder, aber PLZ ungültig
      await page.fill('#companyName', 'Test GmbH');
      await page.selectOption('#legalForm', 'gmbh');
      await page.selectOption('#customerType', 'single');
      await page.selectOption('#customerStatus', 'neukunde');
      await page.selectOption('#industry', 'hotel');
      await page.fill('#street', 'Teststraße 1');
      await page.fill('#postalCode', '123'); // Zu kurz
      await page.fill('#city', 'Berlin');
      await page.fill('#contactName', 'Max Mustermann');
      await page.fill('#contactPhone', '0123456789');
      await page.fill('#contactEmail', 'test@example.com');
      await page.fill('#expectedVolume', '50000');
      await page.selectOption('#paymentMethod', 'vorkasse');
      
      // Speichern
      await page.click('.header-btn-save');
      
      // Erwarte Validierungsfehler
      const alertDialog = page.waitForEvent('dialog');
      const dialog = await alertDialog;
      expect(dialog.message()).toContain('PLZ');
      await dialog.accept();
    });
  });

  test.describe('Neukunde + Rechnung Warnung', () => {
    test('sollte Warnung bei Neukunde + Rechnung anzeigen', async ({ page }) => {
      // Wähle Neukunde
      await page.selectOption('#customerStatus', 'neukunde');
      
      // Wähle Rechnung
      await page.selectOption('#paymentMethod', 'rechnung');
      
      // Erwarte sofortige Warnung
      const alertDialog = page.waitForEvent('dialog');
      const dialog = await alertDialog;
      expect(dialog.message()).toContain('Bonitätsprüfung');
      await dialog.accept();
    });

    test('sollte keine Warnung bei Bestandskunde + Rechnung zeigen', async ({ page }) => {
      // Wähle Bestandskunde
      await page.selectOption('#customerStatus', 'bestandskunde');
      
      // Wähle Rechnung - sollte keine Warnung geben
      await page.selectOption('#paymentMethod', 'rechnung');
      
      // Warte kurz um sicherzustellen, dass keine Warnung kommt
      await page.waitForTimeout(500);
      
      // Kein Alert sollte erschienen sein
      const alerts = await page.evaluate(() => window.alert);
      expect(alerts).toBeFalsy();
    });
  });

  test.describe('Datenpersistenz', () => {
    test('sollte Daten speichern und nach Reload wiederherstellen', async ({ page }) => {
      const testData = {
        companyName: 'Persistenz Test GmbH',
        legalForm: 'gmbh',
        customerType: 'single',
        customerStatus: 'bestandskunde',
        industry: 'restaurant',
        street: 'Persistenzstraße 42',
        postalCode: '54321',
        city: 'Hamburg',
        contactName: 'Petra Persistent',
        contactPhone: '+49 40 123456',
        contactEmail: 'petra@persistent.de',
        expectedVolume: '75000',
        paymentMethod: 'sepa'
      };

      // Fülle Formular
      await page.fill('#companyName', testData.companyName);
      await page.selectOption('#legalForm', testData.legalForm);
      await page.selectOption('#customerType', testData.customerType);
      await page.selectOption('#customerStatus', testData.customerStatus);
      await page.selectOption('#industry', testData.industry);
      await page.fill('#street', testData.street);
      await page.fill('#postalCode', testData.postalCode);
      await page.fill('#city', testData.city);
      await page.fill('#contactName', testData.contactName);
      await page.fill('#contactPhone', testData.contactPhone);
      await page.fill('#contactEmail', testData.contactEmail);
      await page.fill('#expectedVolume', testData.expectedVolume);
      await page.selectOption('#paymentMethod', testData.paymentMethod);

      // Speichern
      const saveDialog = page.waitForEvent('dialog');
      await page.click('.header-btn-save');
      const dialog = await saveDialog;
      expect(dialog.message()).toContain('erfolgreich gespeichert');
      await dialog.accept();

      // Seite neu laden
      await page.reload();
      
      // Warte auf Neuinitialisierung
      await page.waitForSelector('#customerForm', { state: 'visible' });
      
      // Zum Kundendaten-Tab navigieren
      await page.click('[data-tab="customer"]');

      // Prüfe ob Daten wiederhergestellt wurden
      await expect(page.locator('#companyName')).toHaveValue(testData.companyName);
      await expect(page.locator('#legalForm')).toHaveValue(testData.legalForm);
      await expect(page.locator('#customerType')).toHaveValue(testData.customerType);
      await expect(page.locator('#customerStatus')).toHaveValue(testData.customerStatus);
      await expect(page.locator('#industry')).toHaveValue(testData.industry);
      await expect(page.locator('#street')).toHaveValue(testData.street);
      await expect(page.locator('#postalCode')).toHaveValue(testData.postalCode);
      await expect(page.locator('#city')).toHaveValue(testData.city);
      await expect(page.locator('#contactName')).toHaveValue(testData.contactName);
      await expect(page.locator('#contactPhone')).toHaveValue(testData.contactPhone);
      await expect(page.locator('#contactEmail')).toHaveValue(testData.contactEmail);
      await expect(page.locator('#expectedVolume')).toHaveValue(testData.expectedVolume);
      await expect(page.locator('#paymentMethod')).toHaveValue(testData.paymentMethod);
    });

    test('sollte Daten löschen können', async ({ page }) => {
      // Erst Daten speichern
      await page.fill('#companyName', 'Zu löschende GmbH');
      await page.fill('#contactEmail', 'delete@test.de');
      await page.fill('#postalCode', '99999');
      
      // Löschen
      page.on('dialog', dialog => dialog.accept()); // Bestätige automatisch
      await page.click('.header-btn-clear');
      
      // Prüfe ob Felder geleert wurden
      await expect(page.locator('#companyName')).toHaveValue('');
      await expect(page.locator('#contactEmail')).toHaveValue('');
      
      // Prüfe LocalStorage
      const customerData = await page.evaluate(() => {
        const data = localStorage.getItem('freshplanData');
        return data ? JSON.parse(data).customer : null;
      });
      expect(customerData).toBeNull();
    });
  });

  test.describe('Chain Customer Integration', () => {
    test('sollte Standorte-Tab bei Kettenauswahl anzeigen', async ({ page }) => {
      // Prüfe Initial-Zustand
      const locationsTab = page.locator('[data-tab="locations"]');
      await expect(locationsTab).toBeHidden();
      
      // Wähle Kette
      await page.selectOption('#chainCustomer', 'ja');
      
      // Standorte-Tab sollte sichtbar werden
      await expect(locationsTab).toBeVisible();
      
      // Wähle wieder Einzelstandort
      await page.selectOption('#chainCustomer', 'nein');
      
      // Standorte-Tab sollte wieder versteckt sein
      await expect(locationsTab).toBeHidden();
    });
  });

  test.describe('Währungsformatierung', () => {
    test('sollte Bestellvolumen formatieren', async ({ page }) => {
      // Eingabe ohne Formatierung
      await page.fill('#expectedVolume', '50000');
      
      // Trigger blur event
      await page.press('#expectedVolume', 'Tab');
      
      // Prüfe Formatierung
      await expect(page.locator('#expectedVolume')).toHaveValue('50.000');
      
      // Teste mit größerer Zahl
      await page.fill('#expectedVolume', '1250000');
      await page.press('#expectedVolume', 'Tab');
      await expect(page.locator('#expectedVolume')).toHaveValue('1.250.000');
    });
  });

  test.describe('Performance', () => {
    test('sollte große Datenmengen verarbeiten können', async ({ page }) => {
      const startTime = Date.now();
      
      // Fülle Formular 10 mal
      for (let i = 0; i < 10; i++) {
        await page.fill('#companyName', `Performance Test ${i}`);
        await page.fill('#contactEmail', `test${i}@example.com`);
        await page.fill('#postalCode', '12345');
        
        // Speichern ohne auf Dialog zu warten
        page.on('dialog', dialog => dialog.accept());
        await page.click('.header-btn-save');
        
        // Kurze Pause
        await page.waitForTimeout(100);
      }
      
      const endTime = Date.now();
      const duration = endTime - startTime;
      
      // Sollte unter 5 Sekunden für 10 Speichervorgänge sein
      expect(duration).toBeLessThan(5000);
    });
  });

  test.describe('Browser-Kompatibilität', () => {
    test('sollte in verschiedenen Viewports funktionieren', async ({ page }) => {
      // Desktop
      await page.setViewportSize({ width: 1920, height: 1080 });
      await expect(page.locator('#customerForm')).toBeVisible();
      
      // Tablet
      await page.setViewportSize({ width: 768, height: 1024 });
      await expect(page.locator('#customerForm')).toBeVisible();
      
      // Mobile
      await page.setViewportSize({ width: 375, height: 667 });
      await expect(page.locator('#customerForm')).toBeVisible();
      
      // Formular sollte in allen Größen bedienbar sein
      await page.fill('#companyName', 'Responsive Test');
      await expect(page.locator('#companyName')).toHaveValue('Responsive Test');
    });
  });
});