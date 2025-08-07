/**
 * FC-005 Customer Onboarding Page Object
 *
 * ZWECK: Page Object Model für Customer Onboarding Wizard E2E Tests
 * PHILOSOPHIE: Wiederverwendbare Seiteninteraktionen für alle E2E Tests
 */

import { Page, Locator, expect } from '@playwright/test';

export class CustomerOnboardingPage {
  readonly page: Page;

  // Page elements
  readonly stepIndicator: Locator;
  readonly stepTitle: Locator;
  readonly nextButton: Locator;
  readonly previousButton: Locator;
  readonly saveButton: Locator;
  readonly cancelButton: Locator;

  // Step 1 - Customer Data Elements
  readonly companyNameField: Locator;
  readonly industrySelect: Locator;
  readonly chainCustomerSelect: Locator;
  readonly emailField: Locator;
  readonly phoneField: Locator;
  readonly websiteField: Locator;

  // Industry-specific fields
  readonly hotelStarsSelect: Locator;
  readonly employeeCountField: Locator;
  readonly bedCountField: Locator;

  // Step 2 - Locations Elements
  readonly addLocationButton: Locator;
  readonly locationNameField: Locator;
  readonly locationStreetField: Locator;
  readonly locationPostalCodeField: Locator;
  readonly locationCityField: Locator;
  readonly locationContactPersonField: Locator;
  readonly locationPhoneField: Locator;
  readonly locationEmailField: Locator;
  readonly removeLocationButton: Locator;

  // Step 3 - Detailed Locations Elements
  readonly addDetailedLocationButton: Locator;
  readonly detailedLocationNameField: Locator;
  readonly detailedLocationCategorySelect: Locator;
  readonly detailedLocationFloorField: Locator;
  readonly detailedLocationCapacityField: Locator;
  readonly detailedLocationHoursField: Locator;
  readonly detailedLocationResponsibleField: Locator;
  readonly detailedLocationInternalPhoneField: Locator;
  readonly detailedLocationRequirementsField: Locator;

  // Validation and feedback elements
  readonly errorMessages: Locator;
  readonly successMessage: Locator;
  readonly loadingIndicator: Locator;
  readonly autoSaveIndicator: Locator;
  readonly draftRecoveryMessage: Locator;

  constructor(page: Page) {
    this.page = page;

    // Navigation elements
    this.stepIndicator = page.locator('[data-testid="step-indicator"], .step-indicator');
    this.stepTitle = page.locator('h1, h2, [data-testid="step-title"]');
    this.nextButton = page.locator('button:has-text("Weiter"), [data-testid="next-button"]');
    this.previousButton = page.locator(
      'button:has-text("Zurück"), [data-testid="previous-button"]'
    );
    this.saveButton = page.locator(
      'button:has-text("Speichern"), button:has-text("Abschließen"), [data-testid="save-button"]'
    );
    this.cancelButton = page.locator('button:has-text("Abbrechen"), [data-testid="cancel-button"]');

    // Step 1 - Customer Data
    this.companyNameField = page.locator('input[name="companyName"], [data-testid="company-name"]');
    this.industrySelect = page.locator('select[name="industry"], [data-testid="industry-select"]');
    this.chainCustomerSelect = page.locator(
      'select[name="chainCustomer"], [data-testid="chain-customer-select"]'
    );
    this.emailField = page.locator('input[name="email"], [data-testid="email"]');
    this.phoneField = page.locator('input[name="phone"], [data-testid="phone"]');
    this.websiteField = page.locator('input[name="website"], [data-testid="website"]');

    // Industry-specific fields
    this.hotelStarsSelect = page.locator('select[name="hotelStars"], [data-testid="hotel-stars"]');
    this.employeeCountField = page.locator(
      'input[name="employeeCount"], [data-testid="employee-count"]'
    );
    this.bedCountField = page.locator('input[name="bedCount"], [data-testid="bed-count"]');

    // Step 2 - Locations
    this.addLocationButton = page.locator(
      'button:has-text("Standort hinzufügen"), [data-testid="add-location"]'
    );
    this.locationNameField = page.locator(
      'input[name*="location"][name*="name"], [data-testid*="location-name"]'
    );
    this.locationStreetField = page.locator(
      'input[name*="location"][name*="street"], [data-testid*="location-street"]'
    );
    this.locationPostalCodeField = page.locator(
      'input[name*="location"][name*="postalCode"], [data-testid*="location-postal"]'
    );
    this.locationCityField = page.locator(
      'input[name*="location"][name*="city"], [data-testid*="location-city"]'
    );
    this.locationContactPersonField = page.locator(
      'input[name*="location"][name*="contactPerson"], [data-testid*="location-contact"]'
    );
    this.locationPhoneField = page.locator(
      'input[name*="location"][name*="phone"], [data-testid*="location-phone"]'
    );
    this.locationEmailField = page.locator(
      'input[name*="location"][name*="email"], [data-testid*="location-email"]'
    );
    this.removeLocationButton = page.locator(
      'button:has-text("Entfernen"), [data-testid*="remove-location"]'
    );

    // Step 3 - Detailed Locations
    this.addDetailedLocationButton = page.locator(
      'button:has-text("Detail hinzufügen"), [data-testid="add-detailed-location"]'
    );
    this.detailedLocationNameField = page.locator(
      'input[name*="detailed"][name*="name"], [data-testid*="detailed-name"]'
    );
    this.detailedLocationCategorySelect = page.locator(
      'select[name*="detailed"][name*="category"], [data-testid*="detailed-category"]'
    );
    this.detailedLocationFloorField = page.locator(
      'input[name*="detailed"][name*="floor"], [data-testid*="detailed-floor"]'
    );
    this.detailedLocationCapacityField = page.locator(
      'input[name*="detailed"][name*="capacity"], [data-testid*="detailed-capacity"]'
    );
    this.detailedLocationHoursField = page.locator(
      'input[name*="detailed"][name*="hours"], [data-testid*="detailed-hours"]'
    );
    this.detailedLocationResponsibleField = page.locator(
      'input[name*="detailed"][name*="responsible"], [data-testid*="detailed-responsible"]'
    );
    this.detailedLocationInternalPhoneField = page.locator(
      'input[name*="detailed"][name*="internalPhone"], [data-testid*="detailed-internal-phone"]'
    );
    this.detailedLocationRequirementsField = page.locator(
      'textarea[name*="detailed"][name*="requirements"], [data-testid*="detailed-requirements"]'
    );

    // Feedback elements
    this.errorMessages = page.locator('.error, .alert-error, [data-testid*="error"]');
    this.successMessage = page.locator('.success, .alert-success, [data-testid="success"]');
    this.loadingIndicator = page.locator('.loading, .spinner, [data-testid="loading"]');
    this.autoSaveIndicator = page.locator('.auto-save, [data-testid="auto-save"]');
    this.draftRecoveryMessage = page.locator('.draft-recovery, [data-testid="draft-recovery"]');
  }

  // Navigation methods
  async goto() {
    await this.page.goto('/customers/new');
  }

  async waitForPage() {
    await this.stepTitle.waitFor();
    await this.companyNameField.waitFor();
  }

  async goToNextStep() {
    await this.nextButton.click();
    await this.page.waitForTimeout(500); // Wait for step transition
  }

  async goToPreviousStep() {
    await this.previousButton.click();
    await this.page.waitForTimeout(500);
  }

  async saveCustomer() {
    await this.saveButton.click();
  }

  async cancelWizard() {
    await this.cancelButton.click();
  }

  // Step 1 - Customer Data methods
  async fillCustomerBasicData(customerData: any) {
    await this.companyNameField.fill(customerData.companyName);

    if (customerData.industry) {
      await this.industrySelect.selectOption(customerData.industry);
    }

    if (customerData.chainCustomer) {
      await this.chainCustomerSelect.selectOption(customerData.chainCustomer);
    }

    if (customerData.email) {
      await this.emailField.fill(customerData.email);
    }

    if (customerData.phone) {
      await this.phoneField.fill(customerData.phone);
    }

    if (customerData.website) {
      await this.websiteField.fill(customerData.website);
    }
  }

  async fillIndustrySpecificFields(customerData: any) {
    // Hotel industry fields
    if (customerData.hotelStars && (await this.hotelStarsSelect.isVisible())) {
      await this.hotelStarsSelect.selectOption(customerData.hotelStars);
    }

    // Office industry fields
    if (customerData.employees && (await this.employeeCountField.isVisible())) {
      await this.employeeCountField.fill(customerData.employees);
    }

    // Healthcare industry fields
    if (customerData.bedCount && (await this.bedCountField.isVisible())) {
      await this.bedCountField.fill(customerData.bedCount);
    }
  }

  async fillCompleteCustomerData(customerData: any) {
    await this.fillCustomerBasicData(customerData);
    await this.fillIndustrySpecificFields(customerData);
  }

  // Step 2 - Locations methods
  async addLocation(locationData: any, index: number = 0) {
    await this.addLocationButton.click();

    const nameField = this.page.locator(
      `[data-testid="location-name-${index}"], input[name="locations[${index}].name"]`
    );
    const streetField = this.page.locator(
      `[data-testid="location-street-${index}"], input[name="locations[${index}].street"]`
    );
    const postalCodeField = this.page.locator(
      `[data-testid="location-postal-${index}"], input[name="locations[${index}].postalCode"]`
    );
    const cityField = this.page.locator(
      `[data-testid="location-city-${index}"], input[name="locations[${index}].city"]`
    );
    const contactField = this.page.locator(
      `[data-testid="location-contact-${index}"], input[name="locations[${index}].contactPerson"]`
    );
    const phoneField = this.page.locator(
      `[data-testid="location-phone-${index}"], input[name="locations[${index}].phone"]`
    );
    const emailField = this.page.locator(
      `[data-testid="location-email-${index}"], input[name="locations[${index}].email"]`
    );

    await nameField.fill(locationData.name);
    await streetField.fill(locationData.street);
    await postalCodeField.fill(locationData.postalCode);
    await cityField.fill(locationData.city);
    await contactField.fill(locationData.contactPerson);
    await phoneField.fill(locationData.phone);
    await emailField.fill(locationData.email);
  }

  async addMultipleLocations(locationsData: any[]) {
    for (let i = 0; i < locationsData.length; i++) {
      await this.addLocation(locationsData[i], i);
    }
  }

  async removeLocation(index: number) {
    const removeButton = this.page.locator(`[data-testid="remove-location-${index}"]`);
    await removeButton.click();
  }

  // Step 3 - Detailed Locations methods
  async addDetailedLocation(detailedLocationData: any, locationIndex: number = 0) {
    await this.addDetailedLocationButton.click();

    await this.detailedLocationNameField.fill(detailedLocationData.name);
    await this.detailedLocationCategorySelect.selectOption(detailedLocationData.category);
    await this.detailedLocationFloorField.fill(detailedLocationData.floor);
    await this.detailedLocationCapacityField.fill(detailedLocationData.capacity);
    await this.detailedLocationHoursField.fill(detailedLocationData.operatingHours);
    await this.detailedLocationResponsibleField.fill(detailedLocationData.responsiblePerson);
    await this.detailedLocationInternalPhoneField.fill(detailedLocationData.internalPhone);
    await this.detailedLocationRequirementsField.fill(detailedLocationData.specialRequirements);
  }

  // Validation methods
  async expectValidationError(fieldName: string, errorMessage: string) {
    const errorElement = this.page.locator(
      `[data-testid="${fieldName}-error"], .error:has-text("${errorMessage}")`
    );
    await expect(errorElement).toBeVisible();
    await expect(errorElement).toContainText(errorMessage);
  }

  async expectNoValidationErrors() {
    await expect(this.errorMessages).toHaveCount(0);
  }

  async expectStepNumber(stepNumber: number) {
    await expect(this.stepIndicator).toContainText(`${stepNumber}`);
  }

  async expectChainCustomerStepVisible() {
    await expect(this.stepTitle).toContainText(/standort|location/i);
    await expect(this.addLocationButton).toBeVisible();
  }

  async expectChainCustomerStepHidden() {
    // Should skip locations step and go directly to final step or summary
    await expect(this.stepTitle).not.toContainText(/standort|location/i);
  }

  // Auto-save and draft methods
  async waitForAutoSave() {
    await this.autoSaveIndicator.waitFor({ state: 'visible', timeout: 5000 });
    await this.autoSaveIndicator.waitFor({ state: 'hidden', timeout: 10000 });
  }

  async expectDraftRecovery() {
    await expect(this.draftRecoveryMessage).toBeVisible();
    await expect(this.draftRecoveryMessage).toContainText(/wiederhergestellt|draft/i);
  }

  async expectSuccessfulSave() {
    await expect(this.successMessage).toBeVisible();
    await expect(this.successMessage).toContainText(/erfolgreich|gespeichert/i);
  }

  // Accessibility methods
  async expectProperHeadingStructure() {
    const headings = this.page.locator('h1, h2, h3, h4, h5, h6');
    const headingCount = await headings.count();
    expect(headingCount).toBeGreaterThan(0);

    // Check that h1 exists
    await expect(this.page.locator('h1')).toBeVisible();
  }

  async expectKeyboardNavigation() {
    // Test tab navigation through form fields
    await this.page.keyboard.press('Tab');
    const firstFocusedElement = await this.page.evaluate(() => document.activeElement?.tagName);
    expect(['INPUT', 'SELECT', 'BUTTON']).toContain(firstFocusedElement);
  }

  async expectAriaLabels() {
    // Check that form fields have proper labels
    const companyNameLabel = await this.companyNameField.getAttribute('aria-label');
    const industryLabel = await this.industrySelect.getAttribute('aria-label');

    expect(
      companyNameLabel || (await this.page.locator('label[for*="companyName"]').textContent())
    ).toBeTruthy();
    expect(
      industryLabel || (await this.page.locator('label[for*="industry"]').textContent())
    ).toBeTruthy();
  }

  // Performance methods
  async measurePageLoadTime(): Promise<number> {
    const startTime = Date.now();
    await this.waitForPage();
    return Date.now() - startTime;
  }

  async measureFormFillTime(customerData: any): Promise<number> {
    const startTime = Date.now();
    await this.fillCompleteCustomerData(customerData);
    return Date.now() - startTime;
  }
}
