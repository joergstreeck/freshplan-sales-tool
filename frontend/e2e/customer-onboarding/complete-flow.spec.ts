import { test, expect } from '@playwright/test';
import type { Page } from '@playwright/test';
import { mockBackendAPIs } from '../fixtures/api-mocks';

// Test data
const testCustomer = {
  // Step 1 - Basic Info
  name: 'Gourmet Restaurant Berlin GmbH',
  street: 'Kurfürstendamm 123',
  zipCode: '10719',
  city: 'Berlin',
  phone: '030 12345678',
  email: 'info@gourmet-berlin.de',
  website: 'www.gourmet-berlin.de',
  
  // Step 2 - Challenges
  numberOfLocations: '3',
  monthlyRevenue: '250000',
  
  // Step 3 - Contacts
  primaryContact: {
    salutation: 'Herr',
    firstName: 'Thomas',
    lastName: 'Müller',
    position: 'Geschäftsführer',
    email: 'thomas.mueller@gourmet-berlin.de',
    phone: '030 12345678-100',
    mobile: '0170 1234567'
  },
  secondaryContact: {
    salutation: 'Frau',
    firstName: 'Sandra',
    lastName: 'Schmidt',
    position: 'Einkaufsleitung',
    email: 'sandra.schmidt@gourmet-berlin.de',
    phone: '030 12345678-200'
  }
};

async function fillBasicInfo(page: Page) {
  // Company details
  await page.fill('[name="name"]', testCustomer.name);
  await page.fill('[name="street"]', testCustomer.street);
  await page.fill('[name="zipCode"]', testCustomer.zipCode);
  await page.fill('[name="city"]', testCustomer.city);
  
  // Contact info
  await page.fill('[name="phone"]', testCustomer.phone);
  await page.fill('[name="email"]', testCustomer.email);
  await page.fill('[name="website"]', testCustomer.website);
  
  // Industry selection
  await page.click('[name="branche"]');
  await page.click('text=Gastronomie');
  
  // Customer type
  await page.click('text=Neukunde');
}

async function fillChallengesAndPotential(page: Page) {
  // Wait for step to load
  await expect(page.getByText('Herausforderungen & Potenzial')).toBeVisible();
  
  // Fill number of locations
  await page.fill('[name="numberOfLocations"]', testCustomer.numberOfLocations);
  
  // Fill monthly revenue
  await page.fill('[name="monthlyRevenue"]', testCustomer.monthlyRevenue);
  
  // Select some challenges
  await page.click('text=Hohe Lebensmittelkosten');
  await page.click('text=Ineffiziente Bestellprozesse');
  await page.click('text=Fehlende Kostentransparenz');
  
  // Add a custom challenge
  await page.fill('[placeholder*="Weitere Herausforderung"]', 'Saisonale Schwankungen im Einkauf');
  await page.click('button[aria-label="Herausforderung hinzufügen"]');
}

async function addContact(page: Page, contact: any) {
  await page.click('button:has-text("Neuen Kontakt hinzufügen")');
  
  // Basic info
  if (contact.salutation) {
    await page.selectOption('[name="salutation"]', contact.salutation);
  }
  await page.fill('[name="firstName"]', contact.firstName);
  await page.fill('[name="lastName"]', contact.lastName);
  if (contact.position) {
    await page.fill('[name="position"]', contact.position);
  }
  
  // Contact details
  await page.click('text=Kontaktdaten');
  await page.fill('[name="email"]', contact.email);
  if (contact.phone) {
    await page.fill('[name="phone"]', contact.phone);
  }
  if (contact.mobile) {
    await page.fill('[name="mobile"]', contact.mobile);
  }
  
  // Submit
  await page.click('button:has-text("Kontakt hinzufügen")');
}

async function fillServicesAndOffer(page: Page) {
  // Wait for step to load
  await expect(page.getByText('Angebot & Services')).toBeVisible();
  
  // Select services
  await page.click('text=Bestellplattform');
  await page.click('text=Preisvergleich');
  await page.click('text=Ausgabenanalyse');
  
  // Set pricing
  await page.fill('[name="monthlyPrice"]', '299');
  
  // Add discount
  await page.fill('[name="discountPercentage"]', '20');
  await page.fill('[name="discountReason"]', 'Neukunden-Rabatt für 3 Monate');
  
  // Contract duration
  await page.selectOption('[name="contractDuration"]', '24');
  
  // Implementation timeline
  await page.fill('[name="implementationWeeks"]', '2');
}

test.describe('Complete Customer Onboarding Flow', () => {
  test.beforeEach(async ({ page }) => {
    // Mock all backend API calls BEFORE navigation
    await mockBackendAPIs(page);
    
    // Mock authentication
    await page.addInitScript(() => {
      window.localStorage.setItem('auth-token', 'test-token');
      // Also mock user context for auth
      window.localStorage.setItem('user', JSON.stringify({
        id: 'test-user',
        name: 'Test User',
        role: 'admin'
      }));
    });
  });

  test('should complete full onboarding flow successfully', async ({ page }) => {
    // Navigate to customers page first
    await page.goto('/kundenmanagement');
    
    // Wait for page to load
    await page.waitForLoadState('networkidle');
    
    // Click the "Neuen Kunden anlegen" button to open wizard
    await page.click('button:has-text("Neuen Kunden anlegen")');
    
    // Wait for wizard dialog to appear
    await page.waitForSelector('[role="dialog"], [role="presentation"]', { timeout: 5000 });
    
    // Step 1: Basic Information
    await expect(page.getByText('Basis & Standort')).toBeVisible();
    await fillBasicInfo(page);
    
    // Validate and continue
    await page.click('button:has-text("Weiter")');
    
    // Step 2: Challenges & Potential
    await fillChallengesAndPotential(page);
    await page.click('button:has-text("Weiter")');
    
    // Step 3: Contacts
    await expect(page.getByText('Ansprechpartner verwalten')).toBeVisible();
    
    // Add primary contact
    await addContact(page, testCustomer.primaryContact);
    await expect(page.getByText('Hauptansprechpartner')).toBeVisible();
    
    // Add secondary contact
    await addContact(page, testCustomer.secondaryContact);
    
    // Verify both contacts
    await expect(page.getByText(`${testCustomer.primaryContact.salutation} ${testCustomer.primaryContact.firstName} ${testCustomer.primaryContact.lastName}`)).toBeVisible();
    await expect(page.getByText(`${testCustomer.secondaryContact.salutation} ${testCustomer.secondaryContact.firstName} ${testCustomer.secondaryContact.lastName}`)).toBeVisible();
    
    await page.click('button:has-text("Weiter")');
    
    // Step 4: Services & Offer
    await fillServicesAndOffer(page);
    
    // Complete onboarding
    await page.click('button:has-text("Kunde anlegen")');
    
    // Should redirect to customer detail page
    await expect(page).toHaveURL(/\/customers\/[a-f0-9-]+/);
    
    // Verify customer was created
    await expect(page.getByText(testCustomer.name)).toBeVisible();
    await expect(page.getByText('Kunde erfolgreich angelegt')).toBeVisible();
  });

  test('should validate required fields in each step', async ({ page }) => {
    // Navigate to customers page and open wizard
    await page.goto('/kundenmanagement');
    await page.waitForLoadState('networkidle');
    await page.click('button:has-text("Neuen Kunden anlegen")');
    await page.waitForSelector('[role="dialog"], [role="presentation"]', { timeout: 5000 });
    
    // Try to proceed without filling required fields
    await page.click('button:has-text("Weiter")');
    
    // Should show validation errors
    await expect(page.getByText('Firmenname ist erforderlich')).toBeVisible();
    await expect(page.getByText('Straße ist erforderlich')).toBeVisible();
    await expect(page.getByText('PLZ ist erforderlich')).toBeVisible();
    await expect(page.getByText('Stadt ist erforderlich')).toBeVisible();
  });

  test('should allow navigation between steps', async ({ page }) => {
    // Navigate to customers page and open wizard
    await page.goto('/kundenmanagement');
    await page.waitForLoadState('networkidle');
    await page.click('button:has-text("Neuen Kunden anlegen")');
    await page.waitForSelector('[role="dialog"], [role="presentation"]', { timeout: 5000 });
    
    // Fill step 1
    await fillBasicInfo(page);
    await page.click('button:has-text("Weiter")');
    
    // Should be on step 2
    await expect(page.getByText('Herausforderungen & Potenzial')).toBeVisible();
    
    // Go back to step 1
    await page.click('button:has-text("Zurück")');
    await expect(page.getByText('Basis & Standort')).toBeVisible();
    
    // Data should be preserved
    const nameInput = page.locator('[name="name"]');
    await expect(nameInput).toHaveValue(testCustomer.name);
  });

  test('should show progress indicator', async ({ page }) => {
    // Navigate to customers page and open wizard
    await page.goto('/kundenmanagement');
    await page.waitForLoadState('networkidle');
    await page.click('button:has-text("Neuen Kunden anlegen")');
    await page.waitForSelector('[role="dialog"], [role="presentation"]', { timeout: 5000 });
    
    // Check initial progress
    const progressBar = page.locator('[role="progressbar"]');
    await expect(progressBar).toHaveAttribute('aria-valuenow', '25');
    
    // Move to step 2
    await fillBasicInfo(page);
    await page.click('button:has-text("Weiter")');
    await expect(progressBar).toHaveAttribute('aria-valuenow', '50');
    
    // Move to step 3
    await fillChallengesAndPotential(page);
    await page.click('button:has-text("Weiter")');
    await expect(progressBar).toHaveAttribute('aria-valuenow', '75');
    
    // Move to step 4
    await addContact(page, testCustomer.primaryContact);
    await page.click('button:has-text("Weiter")');
    await expect(progressBar).toHaveAttribute('aria-valuenow', '100');
  });

  test('should handle errors gracefully', async ({ page }) => {
    // Navigate to customers page and open wizard
    await page.goto('/kundenmanagement');
    await page.waitForLoadState('networkidle');
    await page.click('button:has-text("Neuen Kunden anlegen")');
    await page.waitForSelector('[role="dialog"], [role="presentation"]', { timeout: 5000 });
    
    // Mock API error
    await page.route('**/api/customers', route => {
      route.fulfill({
        status: 500,
        body: JSON.stringify({ error: 'Internal Server Error' })
      });
    });
    
    // Complete all steps
    await fillBasicInfo(page);
    await page.click('button:has-text("Weiter")');
    
    await fillChallengesAndPotential(page);
    await page.click('button:has-text("Weiter")');
    
    await addContact(page, testCustomer.primaryContact);
    await page.click('button:has-text("Weiter")');
    
    await fillServicesAndOffer(page);
    
    // Try to submit
    await page.click('button:has-text("Kunde anlegen")');
    
    // Should show error message
    await expect(page.getByText('Fehler beim Anlegen des Kunden')).toBeVisible();
    
    // Should stay on current step
    await expect(page.getByText('Angebot & Services')).toBeVisible();
  });

  test('should work on mobile devices', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 812 });
    
    // Navigate to customers page and open wizard
    await page.goto('/kundenmanagement');
    await page.waitForLoadState('networkidle');
    await page.click('button:has-text("Neuen Kunden anlegen")');
    await page.waitForSelector('[role="dialog"], [role="presentation"]', { timeout: 5000 });
    
    // Complete flow on mobile
    await fillBasicInfo(page);
    await page.click('button:has-text("Weiter")');
    
    // Check responsive layout
    const formContainer = page.locator('form').first();
    const containerBox = await formContainer.boundingBox();
    expect(containerBox?.width).toBeLessThan(360);
    
    // Continue with flow
    await fillChallengesAndPotential(page);
    await page.click('button:has-text("Weiter")');
    
    // Add contact on mobile
    await addContact(page, testCustomer.primaryContact);
    
    // Verify mobile-optimized contact display
    const contactCard = page.locator('[class*="MuiPaper"]').first();
    const cardBox = await contactCard.boundingBox();
    expect(cardBox?.width).toBeLessThan(360);
  });

  test('should persist data in browser storage', async ({ page }) => {
    // Navigate to customers page and open wizard
    await page.goto('/kundenmanagement');
    await page.waitForLoadState('networkidle');
    await page.click('button:has-text("Neuen Kunden anlegen")');
    await page.waitForSelector('[role="dialog"], [role="presentation"]', { timeout: 5000 });
    
    // Fill first step
    await fillBasicInfo(page);
    await page.click('button:has-text("Weiter")');
    
    // Reload page
    await page.reload();
    
    // Should restore to step 2 with data preserved
    await expect(page.getByText('Herausforderungen & Potenzial')).toBeVisible();
    
    // Go back to step 1
    await page.click('button:has-text("Zurück")');
    
    // Check data is still there
    const nameInput = page.locator('[name="name"]');
    await expect(nameInput).toHaveValue(testCustomer.name);
  });

  test('should calculate and display pricing summary', async ({ page }) => {
    // Navigate to customers page and open wizard
    await page.goto('/kundenmanagement');
    await page.waitForLoadState('networkidle');
    await page.click('button:has-text("Neuen Kunden anlegen")');
    await page.waitForSelector('[role="dialog"], [role="presentation"]', { timeout: 5000 });
    
    // Navigate to final step
    await fillBasicInfo(page);
    await page.click('button:has-text("Weiter")');
    
    await fillChallengesAndPotential(page);
    await page.click('button:has-text("Weiter")');
    
    await addContact(page, testCustomer.primaryContact);
    await page.click('button:has-text("Weiter")');
    
    // Fill pricing
    await page.click('text=Bestellplattform');
    await page.click('text=Preisvergleich');
    await page.fill('[name="monthlyPrice"]', '299');
    await page.fill('[name="discountPercentage"]', '20');
    
    // Check summary calculation
    await expect(page.getByText('239,20 €')).toBeVisible(); // With 20% discount
    await expect(page.getByText('2.870,40 €')).toBeVisible(); // Annual
  });
});