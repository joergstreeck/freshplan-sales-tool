---
Navigation: [⬅️ Previous](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/02-integration-tests.md) | [🏠 Home](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md) | [➡️ Next](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/04-performance-tests.md)
Parent: [📁 Testing](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/README.md)
Related: [🔗 Frontend Components](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/01-components.md) | [🔗 User Flows](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/README.md)
---

# 🎭 FC-005 E2E TESTS

**Fokus:** Critical User Journeys  
**Framework:** Playwright  
**Coverage:** 100% der kritischen Pfade  

## 📋 Test Setup

### Playwright Configuration

```typescript
// playwright.config.ts
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },
    // Mobile viewports
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] },
    },
  ],

  webServer: {
    command: 'npm run dev',
    port: 5173,
    reuseExistingServer: !process.env.CI,
  },
});
```

## 📋 Customer Onboarding Flow

### Complete Customer Creation Journey

```typescript
// e2e/customer-onboarding.spec.ts
import { test, expect } from '@playwright/test';
import { CustomerPage } from './pages/CustomerPage';
import { LoginPage } from './pages/LoginPage';

test.describe('Customer Onboarding Flow', () => {
  let customerPage: CustomerPage;
  
  test.beforeEach(async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.login('sales@freshplan.de', 'test123');
    
    customerPage = new CustomerPage(page);
    await customerPage.navigateToCustomers();
  });
  
  test('should complete full customer onboarding', async ({ page }) => {
    // Step 1: Start onboarding
    await customerPage.clickNewCustomer();
    await expect(page).toHaveURL(/.*\/customers\/new/);
    
    // Step 2: Fill customer data
    await customerPage.fillCustomerStep({
      companyName: 'E2E Test Hotel GmbH',
      industry: 'hotel',
      expectedVolume: '500000',
      email: 'e2e@testhotel.de',
      phone: '+49 123 456789',
      chainCustomer: 'ja'
    });
    
    // Verify chain customer shows location tab
    await expect(page.getByRole('tab', { name: 'Standorte' })).toBeVisible();
    
    // Step 3: Continue to locations
    await customerPage.clickNext();
    
    // Step 4: Add location
    await customerPage.addLocation({
      locationType: 'hauptstandort',
      street: 'Teststraße 123',
      zipCode: '12345',
      city: 'Berlin',
      contactPerson: 'Max Mustermann'
    });
    
    // Step 5: Add detailed location
    await customerPage.addDetailedLocation({
      name: 'Restaurant Terrasse',
      kitchenType: 'aufwaermkueche',
      seatingCapacity: '80'
    });
    
    // Step 6: Review and save
    await customerPage.clickNext();
    await expect(page.getByText('Zusammenfassung')).toBeVisible();
    
    // Verify summary
    await expect(page.getByText('E2E Test Hotel GmbH')).toBeVisible();
    await expect(page.getByText('1 Standort')).toBeVisible();
    
    // Step 7: Save customer
    await customerPage.saveCustomer();
    
    // Verify redirect to customer list
    await expect(page).toHaveURL(/.*\/customers$/);
    await expect(page.getByText('Kunde erfolgreich angelegt')).toBeVisible();
    
    // Verify customer in list
    await page.getByPlaceholder('Suchen...').fill('E2E Test Hotel');
    await expect(page.getByText('E2E Test Hotel GmbH')).toBeVisible();
  });
  
  test('should handle draft saving and resuming', async ({ page }) => {
    // Start creating customer
    await customerPage.clickNewCustomer();
    
    // Fill partial data
    await customerPage.fillCustomerStep({
      companyName: 'Draft Test GmbH',
      industry: 'krankenhaus'
    });
    
    // Navigate away (triggers auto-save)
    await page.getByRole('link', { name: 'Dashboard' }).click();
    
    // Confirm navigation with unsaved changes
    await page.getByRole('button', { name: 'Entwurf speichern' }).click();
    
    // Return to customers
    await customerPage.navigateToCustomers();
    
    // Find draft in list
    await page.getByRole('tab', { name: 'Entwürfe' }).click();
    await expect(page.getByText('Draft Test GmbH')).toBeVisible();
    
    // Resume draft
    await page.getByText('Draft Test GmbH').click();
    await page.getByRole('button', { name: 'Bearbeiten' }).click();
    
    // Verify data is preserved
    await expect(page.getByDisplayValue('Draft Test GmbH')).toBeVisible();
    await expect(page.getByDisplayValue('krankenhaus')).toBeVisible();
  });
  
  test('should validate required fields', async ({ page }) => {
    await customerPage.clickNewCustomer();
    
    // Try to continue without filling required fields
    await customerPage.clickNext();
    
    // Verify validation messages
    await expect(page.getByText('Firmenname ist erforderlich')).toBeVisible();
    await expect(page.getByText('Branche ist erforderlich')).toBeVisible();
    
    // Fill one field and verify error clears
    await page.getByLabel('Firmenname *').fill('Test GmbH');
    await expect(page.getByText('Firmenname ist erforderlich')).not.toBeVisible();
  });
});
```

### Page Object Model

```typescript
// e2e/pages/CustomerPage.ts
import { Page } from '@playwright/test';

export class CustomerPage {
  constructor(private page: Page) {}
  
  async navigateToCustomers() {
    await this.page.getByRole('link', { name: 'Kundenmanagement' }).click();
    await this.page.getByRole('link', { name: 'Alle Kunden' }).click();
  }
  
  async clickNewCustomer() {
    await this.page.getByRole('button', { name: 'Neuer Kunde' }).click();
  }
  
  async fillCustomerStep(data: CustomerData) {
    if (data.companyName) {
      await this.page.getByLabel('Firmenname *').fill(data.companyName);
    }
    
    if (data.industry) {
      await this.page.getByLabel('Branche *').selectOption(data.industry);
    }
    
    if (data.expectedVolume) {
      await this.page.getByLabel('Erwartetes Volumen').fill(data.expectedVolume);
    }
    
    if (data.email) {
      await this.page.getByLabel('E-Mail').fill(data.email);
    }
    
    if (data.phone) {
      await this.page.getByLabel('Telefon').fill(data.phone);
    }
    
    if (data.chainCustomer) {
      await this.page.getByLabel(
        data.chainCustomer === 'ja' ? 'Ja' : 'Nein'
      ).check();
    }
  }
  
  async addLocation(data: LocationData) {
    await this.page.getByRole('button', { name: 'Standort hinzufügen' }).click();
    
    await this.page.getByLabel('Standorttyp *').selectOption(data.locationType);
    await this.page.getByLabel('Straße *').fill(data.street);
    await this.page.getByLabel('PLZ *').fill(data.zipCode);
    await this.page.getByLabel('Ort *').fill(data.city);
    
    if (data.contactPerson) {
      await this.page.getByLabel('Ansprechpartner').fill(data.contactPerson);
    }
    
    await this.page.getByRole('button', { name: 'Speichern' }).click();
  }
  
  async addDetailedLocation(data: DetailedLocationData) {
    await this.page.getByRole('button', { name: 'Ausgabestelle hinzufügen' }).click();
    
    await this.page.getByLabel('Bezeichnung *').fill(data.name);
    await this.page.getByLabel('Küchentyp').selectOption(data.kitchenType);
    await this.page.getByLabel('Sitzplätze').fill(data.seatingCapacity);
    
    await this.page.getByRole('button', { name: 'Hinzufügen' }).click();
  }
  
  async clickNext() {
    await this.page.getByRole('button', { name: 'Weiter' }).click();
  }
  
  async saveCustomer() {
    await this.page.getByRole('button', { name: 'Kunde anlegen' }).click();
  }
}
```

## 📋 Field Management Scenarios

### Dynamic Field Rendering Test

```typescript
test('should show industry-specific fields', async ({ page }) => {
  const customerPage = new CustomerPage(page);
  await customerPage.navigateToCustomers();
  await customerPage.clickNewCustomer();
  
  // Select hotel industry
  await page.getByLabel('Branche *').selectOption('hotel');
  
  // Verify hotel-specific fields appear
  await expect(page.getByLabel('Anzahl Zimmer')).toBeVisible();
  await expect(page.getByLabel('Sterne-Kategorie')).toBeVisible();
  
  // Change to krankenhaus
  await page.getByLabel('Branche *').selectOption('krankenhaus');
  
  // Verify hospital-specific fields
  await expect(page.getByLabel('Anzahl Betten')).toBeVisible();
  await expect(page.getByLabel('Stationen')).toBeVisible();
  
  // Hotel fields should be hidden
  await expect(page.getByLabel('Anzahl Zimmer')).not.toBeVisible();
});
```

## 📋 Error Handling Scenarios

### Network Error Handling

```typescript
test('should handle network errors gracefully', async ({ page, context }) => {
  // Intercept API calls and simulate error
  await context.route('**/api/customers/draft', route => {
    route.abort('failed');
  });
  
  const customerPage = new CustomerPage(page);
  await customerPage.navigateToCustomers();
  await customerPage.clickNewCustomer();
  
  // Fill and try to save
  await customerPage.fillCustomerStep({
    companyName: 'Network Test GmbH',
    industry: 'hotel'
  });
  
  await customerPage.clickNext();
  
  // Should show error message
  await expect(page.getByText('Verbindungsfehler')).toBeVisible();
  await expect(page.getByText('Bitte versuchen Sie es später erneut')).toBeVisible();
  
  // Data should be preserved
  await expect(page.getByDisplayValue('Network Test GmbH')).toBeVisible();
});
```

## 🔧 Test Utilities

### Authentication Helper

```typescript
// e2e/helpers/auth.ts
import { Page } from '@playwright/test';

export async function authenticateUser(
  page: Page, 
  username: string, 
  password: string
) {
  await page.goto('/login');
  await page.getByLabel('E-Mail').fill(username);
  await page.getByLabel('Passwort').fill(password);
  await page.getByRole('button', { name: 'Anmelden' }).click();
  
  // Wait for redirect
  await page.waitForURL(/.*\/dashboard/);
  
  // Store auth state for reuse
  await page.context().storageState({ path: 'e2e/.auth/user.json' });
}
```

### Visual Regression Tests

```typescript
test('customer form should match visual snapshot', async ({ page }) => {
  const customerPage = new CustomerPage(page);
  await customerPage.navigateToCustomers();
  await customerPage.clickNewCustomer();
  
  // Wait for form to be fully loaded
  await page.waitForLoadState('networkidle');
  
  // Take screenshot
  await expect(page).toHaveScreenshot('customer-form.png', {
    fullPage: true,
    animations: 'disabled'
  });
});
```

## 📚 Weiterführende Links

- [Performance Tests →](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/04-performance-tests.md)
- [Test Data Setup](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/02-integration-tests.md#test-data-management)
- [CI/CD Integration](/Users/joergstreeck/freshplan-sales-tool/CLAUDE.md#ci-monitoring)