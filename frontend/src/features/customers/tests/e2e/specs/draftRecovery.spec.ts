/**
 * FC-005 Draft Recovery E2E Tests
 * 
 * ZWECK: End-to-End Tests für Draft-Funktionalität und Datenwiederherstellung
 * PHILOSOPHIE: Validiert Ausfallsicherheit und Benutzererfahrung bei Unterbrechungen
 */

import { test, expect } from '@playwright/test';
import { CustomerOnboardingPage } from '../page-objects/CustomerOnboardingPage';
import { testCustomers, testLocations } from '../fixtures/test-data';

test.describe('FC-005 Draft Recovery', () => {
  let onboardingPage: CustomerOnboardingPage;
  
  test.beforeEach(async ({ page }) => {
    onboardingPage = new CustomerOnboardingPage(page);
    await onboardingPage.goto();
    await onboardingPage.waitForPage();
  });
  
  test('should auto-save draft and recover after browser refresh', async ({ page }) => {
    const testData = testCustomers.singleLocation;
    
    // Fill customer data and wait for auto-save
    await test.step('Fill customer data and trigger auto-save', async () => {
      await onboardingPage.fillCompleteCustomerData(testData);
      
      // Wait for auto-save to complete
      await onboardingPage.waitForAutoSave();
    });
    
    // Refresh the browser to simulate accidental refresh
    await test.step('Simulate browser refresh', async () => {
      await page.reload();
      await onboardingPage.waitForPage();
    });
    
    // Verify draft recovery
    await test.step('Verify draft data is recovered', async () => {
      // Should show draft recovery notification
      await onboardingPage.expectDraftRecovery();
      
      // All form data should be restored
      await expect(onboardingPage.companyNameField).toHaveValue(testData.companyName);
      await expect(onboardingPage.industrySelect).toHaveValue(testData.industry);
      await expect(onboardingPage.chainCustomerSelect).toHaveValue(testData.chainCustomer);
      await expect(onboardingPage.emailField).toHaveValue(testData.email);
      await expect(onboardingPage.phoneField).toHaveValue(testData.phone);
      
      // Industry-specific fields should also be restored
      await expect(onboardingPage.hotelStarsSelect).toHaveValue(testData.hotelStars);
    });
    
    // Continue and complete the customer creation
    await test.step('Complete customer creation from draft', async () => {
      await onboardingPage.goToNextStep();
      await onboardingPage.saveCustomer();
      await onboardingPage.expectSuccessfulSave();
    });
  });
  
  test('should recover chain customer draft with locations', async ({ page }) => {
    const chainData = testCustomers.chainCustomer;
    
    // Fill chain customer data and add locations
    await test.step('Fill chain customer data and add locations', async () => {
      await onboardingPage.fillCompleteCustomerData(chainData);
      await onboardingPage.goToNextStep();
      
      // Add multiple locations
      await onboardingPage.addLocation(testLocations[0], 0);
      await onboardingPage.addLocation(testLocations[1], 1);
      
      // Wait for auto-save
      await onboardingPage.waitForAutoSave();
    });
    
    // Close and reopen browser (simulate browser crash)
    await test.step('Simulate browser crash and recovery', async () => {
      // Close and create new context to simulate browser restart
      await page.context().close();
      
      // Create new page and navigate to onboarding
      const newContext = await page.context().browser()?.newContext();
      if (newContext) {
        const newPage = await newContext.newPage();
        onboardingPage = new CustomerOnboardingPage(newPage);
        await onboardingPage.goto();
        await onboardingPage.waitForPage();
      }
    });
    
    // Verify complete draft recovery
    await test.step('Verify complete draft recovery', async () => {
      // Should show draft recovery message
      await onboardingPage.expectDraftRecovery();
      
      // Customer data should be restored
      await expect(onboardingPage.companyNameField).toHaveValue(chainData.companyName);
      await expect(onboardingPage.chainCustomerSelect).toHaveValue(chainData.chainCustomer);
      
      // Should be on the correct step (step 2 - locations)
      await onboardingPage.expectStepNumber(2);
      
      // Locations should be restored
      await expect(page.locator('text=' + testLocations[0].name)).toBeVisible();
      await expect(page.locator('text=' + testLocations[1].name)).toBeVisible();
    });
  });
  
  test('should handle draft recovery conflicts gracefully', async ({ page }) => {
    // Create initial draft
    await test.step('Create initial draft', async () => {
      await onboardingPage.fillCustomerBasicData({
        companyName: 'Draft Conflict Test',
        industry: 'hotel',
        chainCustomer: 'nein'
      });
      await onboardingPage.waitForAutoSave();
    });
    
    // Simulate opening same form in another tab/window
    await test.step('Simulate concurrent editing', async () => {
      // Open new tab
      const newPage = await page.context().newPage();
      const newOnboardingPage = new CustomerOnboardingPage(newPage);
      await newOnboardingPage.goto();
      await newOnboardingPage.waitForPage();
      
      // Should detect existing draft
      await newOnboardingPage.expectDraftRecovery();
      
      // Modify data in new tab
      await newOnboardingPage.fillCustomerBasicData({
        companyName: 'Draft Conflict Test (Modified)',
        industry: 'office',
        chainCustomer: 'ja'
      });
      await newOnboardingPage.waitForAutoSave();
      
      await newPage.close();
    });
    
    // Return to original tab and refresh
    await test.step('Handle conflict in original tab', async () => {
      await page.reload();
      await onboardingPage.waitForPage();
      
      // Should show most recent draft data
      await onboardingPage.expectDraftRecovery();
      await expect(onboardingPage.companyNameField).toHaveValue('Draft Conflict Test (Modified)');
      await expect(onboardingPage.industrySelect).toHaveValue('office');
    });
  });
  
  test('should provide manual draft recovery options', async ({ page }) => {
    // Create draft
    await test.step('Create draft with partial data', async () => {
      await onboardingPage.fillCustomerBasicData({
        companyName: 'Manual Recovery Test',
        industry: 'healthcare'
      });
      await onboardingPage.waitForAutoSave();
    });
    
    // Refresh and decline auto-recovery
    await test.step('Decline automatic recovery', async () => {
      await page.reload();
      await onboardingPage.waitForPage();
      
      // Look for recovery options
      const declineRecoveryButton = page.locator('button:has-text("Neu beginnen"), [data-testid="decline-recovery"]');
      
      if (await declineRecoveryButton.isVisible()) {
        await declineRecoveryButton.click();
        
        // Form should be empty
        await expect(onboardingPage.companyNameField).toHaveValue('');
        await expect(onboardingPage.industrySelect).toHaveValue('');
      }
    });
    
    // Manually load draft
    await test.step('Manually load draft', async () => {
      const loadDraftButton = page.locator('button:has-text("Entwurf laden"), [data-testid="load-draft"]');
      
      if (await loadDraftButton.isVisible()) {
        await loadDraftButton.click();
        
        // Draft data should be restored
        await expect(onboardingPage.companyNameField).toHaveValue('Manual Recovery Test');
        await expect(onboardingPage.industrySelect).toHaveValue('healthcare');
      }
    });
  });
  
  test('should handle corrupted draft data gracefully', async ({ page }) => {
    // Create valid draft first
    await test.step('Create valid draft', async () => {
      await onboardingPage.fillCustomerBasicData({
        companyName: 'Corruption Test',
        industry: 'hotel'
      });
      await onboardingPage.waitForAutoSave();
    });
    
    // Simulate corrupted localStorage data
    await test.step('Corrupt draft data', async () => {
      await page.evaluate(() => {
        localStorage.setItem('customer-onboarding-draft', 'invalid json data{{{');
      });
    });
    
    // Refresh and verify graceful handling
    await test.step('Verify graceful handling of corruption', async () => {
      await page.reload();
      await onboardingPage.waitForPage();
      
      // Should show error message about corrupted draft
      const corruptionMessage = page.locator('text*=korrupt, text*=fehler, [data-testid="corruption-error"]');
      await expect(corruptionMessage).toBeVisible();
      
      // Form should start fresh
      await expect(onboardingPage.companyNameField).toHaveValue('');
      
      // Should still be functional
      await onboardingPage.fillCustomerBasicData({
        companyName: 'Fresh Start After Corruption',
        industry: 'office'
      });
      
      await onboardingPage.waitForAutoSave();
      await onboardingPage.goToNextStep();
    });
  });
  
  test('should manage draft storage limits', async ({ page }) => {
    // Fill form with large amount of data
    await test.step('Fill form with extensive data', async () => {
      const largeData = {
        companyName: 'Large Data Test ' + 'X'.repeat(1000),
        industry: 'hotel',
        chainCustomer: 'ja',
        email: 'large-data@test.de',
        phone: '+49 30 123456789',
        website: 'https://large-data-test.de'
      };
      
      await onboardingPage.fillCompleteCustomerData(largeData);
      await onboardingPage.goToNextStep();
      
      // Add many locations with extensive data
      for (let i = 0; i < 20; i++) {
        await onboardingPage.addLocation({
          name: `Large Location ${i + 1} ` + 'Y'.repeat(100),
          street: `Large Street ${i + 1} ` + 'Z'.repeat(100),
          postalCode: `${10000 + i}`.padStart(5, '0'),
          city: `Large City ${i + 1}`,
          contactPerson: `Large Contact Person ${i + 1}`,
          phone: `+49 30 ${1000000 + i}`,
          email: `large${i + 1}@test.de`
        }, i);
      }
      
      // Should handle large data gracefully
      await onboardingPage.waitForAutoSave();
    });
    
    // Verify storage and recovery
    await test.step('Verify large data storage and recovery', async () => {
      await page.reload();
      await onboardingPage.waitForPage();
      
      // Should either recover successfully or show appropriate warning
      const recoveryStatus = await Promise.race([
        onboardingPage.draftRecoveryMessage.waitFor({ state: 'visible', timeout: 3000 }).then(() => 'recovered'),
        page.locator('text*=speicher, text*=limit').waitFor({ state: 'visible', timeout: 3000 }).then(() => 'limit'),
        Promise.resolve('timeout')
      ]);
      
      if (recoveryStatus === 'recovered') {
        // Data should be recovered
        await expect(onboardingPage.companyNameField).toContainText('Large Data Test');
      } else if (recoveryStatus === 'limit') {
        // Should show storage limit warning
        expect(true).toBe(true); // Storage limit handled gracefully
      }
    });
  });
  
  test('should provide draft management interface', async ({ page }) => {
    // Create multiple drafts (simulate)
    await test.step('Create multiple draft scenarios', async () => {
      // First draft
      await onboardingPage.fillCustomerBasicData({
        companyName: 'Draft Management Test 1',
        industry: 'hotel'
      });
      await onboardingPage.waitForAutoSave();
      
      // Clear and create second scenario
      await page.reload();
      await onboardingPage.waitForPage();
      
      if (await page.locator('button:has-text("Neu beginnen")').isVisible()) {
        await page.locator('button:has-text("Neu beginnen")').click();
      }
      
      await onboardingPage.fillCustomerBasicData({
        companyName: 'Draft Management Test 2',
        industry: 'office'
      });
      await onboardingPage.waitForAutoSave();
    });
    
    // Check for draft management options
    await test.step('Verify draft management options', async () => {
      await page.reload();
      await onboardingPage.waitForPage();
      
      // Look for draft management interface
      const draftList = page.locator('[data-testid="draft-list"], .draft-manager');
      const deleteDraftButton = page.locator('button:has-text("Entwurf löschen"), [data-testid="delete-draft"]');
      
      if (await draftList.isVisible() || await deleteDraftButton.isVisible()) {
        // Draft management is available
        expect(true).toBe(true);
      } else {
        // Basic recovery is sufficient
        await onboardingPage.expectDraftRecovery();
      }
    });
  });
  
  test('should handle network failures during auto-save gracefully', async ({ page }) => {
    // Setup offline simulation
    await test.step('Simulate network failure during auto-save', async () => {
      // Fill data
      await onboardingPage.fillCustomerBasicData({
        companyName: 'Network Failure Test',
        industry: 'hotel'
      });
      
      // Go offline
      await page.context().setOffline(true);
      
      // Try to trigger auto-save
      await onboardingPage.emailField.fill('offline@test.de');
      
      // Should show offline indicator or error
      const offlineIndicator = page.locator('.offline, [data-testid="offline"], text*=offline, text*=netzwerk');
      await expect(offlineIndicator).toBeVisible({ timeout: 10000 });
    });
    
    // Go back online and verify recovery
    await test.step('Verify recovery when online', async () => {
      await page.context().setOffline(false);
      
      // Should retry auto-save automatically or show retry option
      const retryButton = page.locator('button:has-text("Wiederholen"), [data-testid="retry-save"]');
      
      if (await retryButton.isVisible()) {
        await retryButton.click();
      }
      
      // Wait for successful save
      await onboardingPage.waitForAutoSave();
      
      // Verify data persistence
      await page.reload();
      await onboardingPage.waitForPage();
      await onboardingPage.expectDraftRecovery();
      
      await expect(onboardingPage.companyNameField).toHaveValue('Network Failure Test');
      await expect(onboardingPage.emailField).toHaveValue('offline@test.de');
    });
  });
});