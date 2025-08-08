/**
 * FC-005 Customer Creation Happy Path E2E Tests
 *
 * ZWECK: End-to-End Tests für den erfolgreichen Kunden-Erstellungsprozess
 * PHILOSOPHIE: Validiert kritische User-Journey für Single-Customer-Onboarding
 */

import { test, expect } from '@playwright/test';
import { CustomerOnboardingPage } from '../page-objects/CustomerOnboardingPage';
import { testCustomers } from '../fixtures/test-data';

test.describe('FC-005 Customer Creation Happy Path', () => {
  let onboardingPage: CustomerOnboardingPage;

  test.beforeEach(async ({ page }) => {
    onboardingPage = new CustomerOnboardingPage(page);
    await onboardingPage.goto();
    await onboardingPage.waitForPage();
  });

  test('should complete single customer onboarding successfully', async ({ page }) => {
    // Test Step 1: Fill customer basic data
    await test.step('Fill customer basic information', async () => {
      await onboardingPage.fillCompleteCustomerData(testCustomers.singleLocation);

      // Verify data was entered correctly
      await expect(onboardingPage.companyNameField).toHaveValue(
        testCustomers.singleLocation.companyName
      );
      await expect(onboardingPage.industrySelect).toHaveValue(
        testCustomers.singleLocation.industry
      );
      await expect(onboardingPage.chainCustomerSelect).toHaveValue(
        testCustomers.singleLocation.chainCustomer
      );
    });

    // Test Step 2: Verify industry-specific fields appear
    await test.step('Verify hotel-specific fields are visible', async () => {
      await expect(onboardingPage.hotelStarsSelect).toBeVisible();
      await expect(onboardingPage.hotelStarsSelect).toHaveValue(
        testCustomers.singleLocation.hotelStars
      );
    });

    // Test Step 3: Verify auto-save functionality
    await test.step('Verify auto-save works', async () => {
      // Wait for auto-save indicator to appear and disappear
      await onboardingPage.waitForAutoSave();
    });

    // Test Step 4: Navigate to next step (should skip locations for single customer)
    await test.step('Navigate to final step', async () => {
      await onboardingPage.goToNextStep();

      // For single customer (chainCustomer = 'nein'), should skip locations step
      await onboardingPage.expectChainCustomerStepHidden();

      // Should be on final step/summary
      await expect(onboardingPage.stepTitle).toContainText(/zusammenfassung|abschluss|final/i);
    });

    // Test Step 5: Review data and submit
    await test.step('Review and submit customer', async () => {
      // Verify customer data is displayed in summary
      await expect(page.locator('text=' + testCustomers.singleLocation.companyName)).toBeVisible();
      await expect(page.locator('text=' + testCustomers.singleLocation.industry)).toBeVisible();

      // Submit the customer
      await onboardingPage.saveCustomer();

      // Verify success message
      await onboardingPage.expectSuccessfulSave();
    });

    // Test Step 6: Verify redirect to customer list or detail page
    await test.step('Verify post-submission navigation', async () => {
      // Should redirect to customer list or customer detail page
      await page.waitForURL(/customers|kunde/);

      // Verify we're not on the onboarding page anymore
      await expect(page.url()).not.toContain('/new');
    });
  });

  test('should handle industry change and show/hide fields dynamically', async ({ page }) => {
    // Start with hotel industry
    await test.step('Select hotel industry', async () => {
      await onboardingPage.fillCustomerBasicData({
        companyName: 'Dynamic Field Test Hotel',
        industry: 'hotel',
        chainCustomer: 'nein',
      });

      // Hotel-specific fields should be visible
      await expect(onboardingPage.hotelStarsSelect).toBeVisible();
    });

    // Change to office industry
    await test.step('Change to office industry', async () => {
      await onboardingPage.industrySelect.selectOption('office');

      // Hotel fields should disappear
      await expect(onboardingPage.hotelStarsSelect).not.toBeVisible();

      // Office fields should appear
      await expect(onboardingPage.employeeCountField).toBeVisible();
    });

    // Change to healthcare industry
    await test.step('Change to healthcare industry', async () => {
      await onboardingPage.industrySelect.selectOption('healthcare');

      // Office fields should disappear
      await expect(onboardingPage.employeeCountField).not.toBeVisible();

      // Healthcare fields should appear
      await expect(onboardingPage.bedCountField).toBeVisible();
    });
  });

  test('should maintain form state during navigation', async ({ page }) => {
    const testData = {
      companyName: 'State Persistence Test GmbH',
      industry: 'hotel',
      chainCustomer: 'ja', // This will enable locations step
      hotelStars: '4',
      email: 'test@state-persistence.de',
    };

    // Fill customer data
    await test.step('Fill customer data on step 1', async () => {
      await onboardingPage.fillCompleteCustomerData(testData);
    });

    // Navigate to step 2 (locations)
    await test.step('Navigate to locations step', async () => {
      await onboardingPage.goToNextStep();
      await onboardingPage.expectChainCustomerStepVisible();
      await onboardingPage.expectStepNumber(2);
    });

    // Go back to step 1
    await test.step('Navigate back to step 1', async () => {
      await onboardingPage.goToPreviousStep();
      await onboardingPage.expectStepNumber(1);
    });

    // Verify data is still there
    await test.step('Verify data persistence', async () => {
      await expect(onboardingPage.companyNameField).toHaveValue(testData.companyName);
      await expect(onboardingPage.industrySelect).toHaveValue(testData.industry);
      await expect(onboardingPage.chainCustomerSelect).toHaveValue(testData.chainCustomer);
      await expect(onboardingPage.hotelStarsSelect).toHaveValue(testData.hotelStars);
      await expect(onboardingPage.emailField).toHaveValue(testData.email);
    });
  });

  test('should show proper progress indication', async ({ page }) => {
    // Check initial step indicator
    await test.step('Verify initial step indicator', async () => {
      await onboardingPage.expectStepNumber(1);
      await expect(onboardingPage.stepTitle).toContainText(/schritt.*1|kundendaten/i);
    });

    // Fill data and go to next step
    await test.step('Navigate to step 2 and verify indicator', async () => {
      await onboardingPage.fillCompleteCustomerData(testCustomers.chainCustomer);
      await onboardingPage.goToNextStep();

      await onboardingPage.expectStepNumber(2);
      await expect(onboardingPage.stepTitle).toContainText(/schritt.*2|standort/i);
    });

    // Go to step 3
    await test.step('Navigate to step 3 and verify indicator', async () => {
      // Add at least one location to proceed
      await onboardingPage.addLocation(
        {
          name: 'Test Location',
          street: 'Test Street 1',
          postalCode: '12345',
          city: 'Test City',
          contactPerson: 'Test Person',
          phone: '+49 30 123456',
          email: 'test@location.de',
        },
        0
      );

      await onboardingPage.goToNextStep();

      await onboardingPage.expectStepNumber(3);
      await expect(onboardingPage.stepTitle).toContainText(/schritt.*3|detail/i);
    });
  });

  test('should handle browser refresh gracefully', async ({ page }) => {
    const testData = testCustomers.singleLocation;

    // Fill some data
    await test.step('Fill partial customer data', async () => {
      await onboardingPage.fillCustomerBasicData(testData);
      await onboardingPage.waitForAutoSave();
    });

    // Refresh the page
    await test.step('Refresh browser page', async () => {
      await page.reload();
      await onboardingPage.waitForPage();
    });

    // Verify draft recovery
    await test.step('Verify draft data recovery', async () => {
      // Should show draft recovery message
      await onboardingPage.expectDraftRecovery();

      // Data should be restored
      await expect(onboardingPage.companyNameField).toHaveValue(testData.companyName);
      await expect(onboardingPage.industrySelect).toHaveValue(testData.industry);
    });
  });

  test('should validate required fields before allowing progression', async ({ page }) => {
    // Try to proceed without filling required fields
    await test.step('Attempt to proceed with empty form', async () => {
      await onboardingPage.goToNextStep();

      // Should still be on step 1
      await onboardingPage.expectStepNumber(1);

      // Should show validation errors
      await onboardingPage.expectValidationError('companyName', 'erforderlich');
      await onboardingPage.expectValidationError('industry', 'erforderlich');
    });

    // Fill required fields and try again
    await test.step('Fill required fields and proceed', async () => {
      await onboardingPage.fillCustomerBasicData({
        companyName: 'Required Fields Test',
        industry: 'hotel',
        chainCustomer: 'nein',
      });

      // Should clear validation errors
      await onboardingPage.expectNoValidationErrors();

      // Should now be able to proceed
      await onboardingPage.goToNextStep();

      // Should progress past step 1
      await expect(page.url()).not.toContain('step=1');
    });
  });

  test('should handle extremely long company names gracefully', async ({ page }) => {
    const longCompanyName =
      'A'.repeat(200) + ' Extremely Long Company Name GmbH & Co. KG mit sehr langem Namen für Test';

    await test.step('Enter very long company name', async () => {
      await onboardingPage.companyNameField.fill(longCompanyName);
      await onboardingPage.industrySelect.selectOption('hotel');
      await onboardingPage.chainCustomerSelect.selectOption('nein');
    });

    await test.step('Verify handling of long text', async () => {
      // Field should accept the long text (up to its limit)
      const actualValue = await onboardingPage.companyNameField.inputValue();
      expect(actualValue.length).toBeGreaterThan(50); // Should accept reasonable length

      // Auto-save should still work
      await onboardingPage.waitForAutoSave();
    });

    await test.step('Verify long text in summary', async () => {
      await onboardingPage.goToNextStep();

      // Summary should handle long text gracefully (ellipsis, wrapping, etc.)
      const summaryText = await page.textContent('body');
      expect(summaryText).toContain('Extremely Long Company Name');
    });
  });
});
