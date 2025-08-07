/**
 * FC-005 Chain Customer Flow E2E Tests
 *
 * ZWECK: End-to-End Tests für Chain Customer Onboarding mit mehreren Standorten
 * PHILOSOPHIE: Validiert komplexe Multi-Location Customer Journey
 */

import { test, expect } from '@playwright/test';
import { CustomerOnboardingPage } from '../page-objects/CustomerOnboardingPage';
import { testCustomers, testLocations, testDetailedLocations } from '../fixtures/test-data';

test.describe('FC-005 Chain Customer Flow', () => {
  let onboardingPage: CustomerOnboardingPage;

  test.beforeEach(async ({ page }) => {
    onboardingPage = new CustomerOnboardingPage(page);
    await onboardingPage.goto();
    await onboardingPage.waitForPage();
  });

  test('should complete chain customer onboarding with multiple locations', async ({ page }) => {
    // Step 1: Fill chain customer data
    await test.step('Fill chain customer basic data', async () => {
      await onboardingPage.fillCompleteCustomerData(testCustomers.chainCustomer);

      // Verify chain customer selection
      await expect(onboardingPage.chainCustomerSelect).toHaveValue('ja');
    });

    // Step 2: Navigate to locations step
    await test.step('Navigate to locations step', async () => {
      await onboardingPage.goToNextStep();

      // Should show locations step because chainCustomer = 'ja'
      await onboardingPage.expectChainCustomerStepVisible();
      await onboardingPage.expectStepNumber(2);
    });

    // Step 3: Add multiple locations
    await test.step('Add multiple locations', async () => {
      // Add first location
      await onboardingPage.addLocation(testLocations[0], 0);

      // Add second location
      await onboardingPage.addLocation(testLocations[1], 1);

      // Add third location
      await onboardingPage.addLocation(testLocations[2], 2);

      // Verify all locations are visible
      await expect(page.locator('text=' + testLocations[0].name)).toBeVisible();
      await expect(page.locator('text=' + testLocations[1].name)).toBeVisible();
      await expect(page.locator('text=' + testLocations[2].name)).toBeVisible();
    });

    // Step 4: Navigate to detailed locations step
    await test.step('Navigate to detailed locations step', async () => {
      await onboardingPage.goToNextStep();

      await onboardingPage.expectStepNumber(3);
      await expect(onboardingPage.stepTitle).toContainText(/detail.*standort/i);
    });

    // Step 5: Add detailed locations for first location
    await test.step('Add detailed locations', async () => {
      const berlinDetails = testDetailedLocations.find(loc => loc.locationName === 'Berlin Mitte');

      if (berlinDetails) {
        for (const detail of berlinDetails.details) {
          await onboardingPage.addDetailedLocation(detail, 0);
        }
      }

      // Verify detailed locations are added
      await expect(page.locator('text=Hauptrestaurant')).toBeVisible();
      await expect(page.locator('text=Tagungsraum Alpha')).toBeVisible();
    });

    // Step 6: Complete chain customer creation
    await test.step('Complete chain customer creation', async () => {
      await onboardingPage.saveCustomer();
      await onboardingPage.expectSuccessfulSave();

      // Verify redirect
      await page.waitForURL(/customers/);
    });
  });

  test('should allow editing and removing locations', async ({ page }) => {
    // Setup chain customer and navigate to locations
    await test.step('Setup chain customer', async () => {
      await onboardingPage.fillCompleteCustomerData(testCustomers.chainCustomer);
      await onboardingPage.goToNextStep();
      await onboardingPage.expectChainCustomerStepVisible();
    });

    // Add initial locations
    await test.step('Add initial locations', async () => {
      await onboardingPage.addLocation(testLocations[0], 0);
      await onboardingPage.addLocation(testLocations[1], 1);
    });

    // Edit first location
    await test.step('Edit first location', async () => {
      const nameField = page.locator('[data-testid="location-name-0"]');
      await nameField.clear();
      await nameField.fill('Berlin Mitte (Bearbeitet)');

      // Verify change
      await expect(nameField).toHaveValue('Berlin Mitte (Bearbeitet)');
    });

    // Remove second location
    await test.step('Remove second location', async () => {
      await onboardingPage.removeLocation(1);

      // Verify location is removed
      await expect(page.locator('text=' + testLocations[1].name)).not.toBeVisible();
    });

    // Add new location
    await test.step('Add replacement location', async () => {
      await onboardingPage.addLocation(
        {
          name: 'Neuer Standort Frankfurt',
          street: 'Zeil 100',
          postalCode: '60313',
          city: 'Frankfurt am Main',
          contactPerson: 'Frankfurt Manager',
          phone: '+49 69 123456',
          email: 'frankfurt@chain.de',
        },
        1
      );

      await expect(page.locator('text=Neuer Standort Frankfurt')).toBeVisible();
    });
  });

  test('should validate location data before allowing progression', async ({ page }) => {
    // Setup chain customer
    await test.step('Setup chain customer', async () => {
      await onboardingPage.fillCompleteCustomerData(testCustomers.chainCustomer);
      await onboardingPage.goToNextStep();
    });

    // Try to proceed without adding locations
    await test.step('Attempt to proceed without locations', async () => {
      await onboardingPage.goToNextStep();

      // Should show validation error
      await onboardingPage.expectValidationError('locations', 'mindestens ein Standort');

      // Should still be on locations step
      await onboardingPage.expectStepNumber(2);
    });

    // Add location with incomplete data
    await test.step('Add location with incomplete data', async () => {
      await onboardingPage.addLocationButton.click();

      // Fill only name, leave other fields empty
      const nameField = page.locator('[data-testid="location-name-0"]');
      await nameField.fill('Incomplete Location');

      // Try to proceed
      await onboardingPage.goToNextStep();

      // Should show validation errors for required location fields
      await onboardingPage.expectValidationError('location-street', 'erforderlich');
      await onboardingPage.expectValidationError('location-city', 'erforderlich');
    });

    // Complete location data
    await test.step('Complete location data', async () => {
      await onboardingPage.addLocation(testLocations[0], 0);

      // Should now be able to proceed
      await onboardingPage.goToNextStep();
      await onboardingPage.expectStepNumber(3);
    });
  });

  test('should handle maximum number of locations', async ({ page }) => {
    // Setup chain customer
    await test.step('Setup chain customer', async () => {
      await onboardingPage.fillCompleteCustomerData(testCustomers.chainCustomer);
      await onboardingPage.goToNextStep();
    });

    // Add maximum allowed locations (e.g., 10)
    await test.step('Add maximum locations', async () => {
      const maxLocations = 10;

      for (let i = 0; i < maxLocations; i++) {
        await onboardingPage.addLocation(
          {
            name: `Standort ${i + 1}`,
            street: `Teststraße ${i + 1}`,
            postalCode: `${10000 + i}`.padStart(5, '0'),
            city: `Stadt ${i + 1}`,
            contactPerson: `Manager ${i + 1}`,
            phone: `+49 30 ${1000000 + i}`,
            email: `manager${i + 1}@test.de`,
          },
          i
        );
      }

      // Verify all locations are added
      for (let i = 0; i < maxLocations; i++) {
        await expect(page.locator(`text=Standort ${i + 1}`)).toBeVisible();
      }
    });

    // Try to add one more location
    await test.step('Attempt to exceed maximum', async () => {
      // Add location button should be disabled or show warning
      const addButton = onboardingPage.addLocationButton;

      if (await addButton.isVisible()) {
        await addButton.click();

        // Should show warning about maximum locations
        await expect(page.locator('text*=maximum')).toBeVisible();
      } else {
        // Button should be disabled
        await expect(addButton).toBeDisabled();
      }
    });
  });

  test('should show location-specific detailed location forms', async ({ page }) => {
    // Setup chain customer with 2 locations
    await test.step('Setup chain customer with multiple locations', async () => {
      await onboardingPage.fillCompleteCustomerData(testCustomers.chainCustomer);
      await onboardingPage.goToNextStep();

      await onboardingPage.addLocation(testLocations[0], 0); // Berlin
      await onboardingPage.addLocation(testLocations[1], 1); // München

      await onboardingPage.goToNextStep();
      await onboardingPage.expectStepNumber(3);
    });

    // Verify location selection for detailed locations
    await test.step('Verify location-specific detailed forms', async () => {
      // Should show dropdown or tabs to select location for detailed data
      const locationSelector = page.locator(
        '[data-testid="detailed-location-selector"], select[name*="location"]'
      );

      if (await locationSelector.isVisible()) {
        // Test Berlin location
        await locationSelector.selectOption(testLocations[0].name);

        // Add detailed location for Berlin
        await onboardingPage.addDetailedLocation(
          {
            name: 'Berlin Restaurant',
            category: 'restaurant',
            floor: 'EG',
            capacity: '100',
            operatingHours: '07:00-22:00',
            responsiblePerson: 'Berlin Chef',
            internalPhone: '101',
            specialRequirements: 'Vegetarische Küche',
          },
          0
        );

        // Switch to München location
        await locationSelector.selectOption(testLocations[1].name);

        // Add detailed location for München
        await onboardingPage.addDetailedLocation(
          {
            name: 'München Brasserie',
            category: 'restaurant',
            floor: 'EG',
            capacity: '80',
            operatingHours: '06:00-23:00',
            responsiblePerson: 'München Chef',
            internalPhone: '102',
            specialRequirements: 'Bayerische Spezialitäten',
          },
          1
        );

        // Verify both detailed locations exist
        await expect(page.locator('text=Berlin Restaurant')).toBeVisible();
        await expect(page.locator('text=München Brasserie')).toBeVisible();
      }
    });
  });

  test('should maintain performance with many locations and detailed locations', async ({
    page,
  }) => {
    // Setup chain customer
    await test.step('Setup chain customer', async () => {
      await onboardingPage.fillCompleteCustomerData(testCustomers.chainCustomer);
      await onboardingPage.goToNextStep();
    });

    // Add many locations
    await test.step('Add multiple locations and measure performance', async () => {
      const startTime = Date.now();
      const locationCount = 5;

      for (let i = 0; i < locationCount; i++) {
        await onboardingPage.addLocation(
          {
            name: `Performance Test Location ${i + 1}`,
            street: `Performance Str. ${i + 1}`,
            postalCode: `${12000 + i}`.padStart(5, '0'),
            city: `Performance Stadt ${i + 1}`,
            contactPerson: `Manager ${i + 1}`,
            phone: `+49 30 ${2000000 + i}`,
            email: `perf${i + 1}@test.de`,
          },
          i
        );
      }

      const locationAddTime = Date.now() - startTime;
      console.log(`Adding ${locationCount} locations took ${locationAddTime}ms`);

      // Should complete within reasonable time (e.g., 10 seconds)
      expect(locationAddTime).toBeLessThan(10000);
    });

    // Navigate to detailed locations
    await test.step('Navigate to detailed locations and measure performance', async () => {
      const startTime = Date.now();

      await onboardingPage.goToNextStep();
      await onboardingPage.expectStepNumber(3);

      const navigationTime = Date.now() - startTime;
      console.log(`Navigation to detailed locations took ${navigationTime}ms`);

      // Should navigate within reasonable time
      expect(navigationTime).toBeLessThan(5000);
    });

    // Add detailed locations
    await test.step('Add detailed locations and measure performance', async () => {
      const startTime = Date.now();

      // Add 2 detailed locations for first location
      await onboardingPage.addDetailedLocation(
        {
          name: 'Performance Restaurant',
          category: 'restaurant',
          floor: 'EG',
          capacity: '150',
          operatingHours: '06:00-22:00',
          responsiblePerson: 'Performance Chef',
          internalPhone: '201',
          specialRequirements: 'High-volume testing',
        },
        0
      );

      await onboardingPage.addDetailedLocation(
        {
          name: 'Performance Meeting Room',
          category: 'meeting',
          floor: '1. OG',
          capacity: '50',
          operatingHours: '08:00-18:00',
          responsiblePerson: 'Event Manager',
          internalPhone: '301',
          specialRequirements: 'Video conferencing',
        },
        0
      );

      const detailedAddTime = Date.now() - startTime;
      console.log(`Adding detailed locations took ${detailedAddTime}ms`);

      // Should complete within reasonable time
      expect(detailedAddTime).toBeLessThan(8000);
    });
  });

  test('should support copy location data feature', async ({ page }) => {
    // Setup chain customer with one location
    await test.step('Setup chain customer with first location', async () => {
      await onboardingPage.fillCompleteCustomerData(testCustomers.chainCustomer);
      await onboardingPage.goToNextStep();

      await onboardingPage.addLocation(testLocations[0], 0);
    });

    // Use copy feature to duplicate location
    await test.step('Copy location data to new location', async () => {
      // Look for copy button
      const copyButton = page.locator(
        '[data-testid="copy-location-0"], button:has-text("Kopieren")'
      );

      if (await copyButton.isVisible()) {
        await copyButton.click();

        // Should add new location with copied data
        await expect(page.locator(`text=${testLocations[0].name}`)).toHaveCount(2);

        // Modify the copied location
        const copiedNameField = page.locator('[data-testid="location-name-1"]');
        await copiedNameField.fill(testLocations[0].name + ' (Kopie)');

        await expect(page.locator('text=' + testLocations[0].name + ' (Kopie)')).toBeVisible();
      }
    });
  });
});
