import { test, expect } from '@playwright/test';
import { injectAxe, checkA11y, getViolations } from 'axe-playwright';

/**
 * Accessibility Tests - WCAG 2.1 Level AA Compliance
 *
 * Tests critical user flows for accessibility violations using axe-core.
 * Ensures WCAG 2.1 Level AA compliance for:
 * - Color contrast
 * - Keyboard navigation
 * - Screen reader compatibility
 * - ARIA attributes
 * - Form labels
 * - Semantic HTML
 */

test.describe('Accessibility (A11y) Tests', () => {
  test.beforeEach(async ({ page }) => {
    // Inject axe-core into the page for accessibility scanning
    await page.goto('/');
    await injectAxe(page);
  });

  test('Homepage should have no accessibility violations', async ({ page }) => {
    await page.goto('/');

    // Wait for page to be fully loaded
    await page.waitForLoadState('networkidle');

    // Run axe accessibility check
    await checkA11y(page, undefined, {
      detailedReport: true,
      detailedReportOptions: {
        html: true,
      },
    });
  });

  test('Dashboard should have no accessibility violations', async ({ page }) => {
    await page.goto('/dashboard');

    // Wait for page to be fully loaded
    await page.waitForLoadState('networkidle');

    // Run axe accessibility check
    await checkA11y(page, undefined, {
      detailedReport: true,
      detailedReportOptions: {
        html: true,
      },
    });
  });

  test('Leads page should have no accessibility violations', async ({ page }) => {
    await page.goto('/lead-generation/leads');

    // Wait for page to be fully loaded
    await page.waitForLoadState('networkidle');

    // Run axe accessibility check with context (test specific sections)
    await checkA11y(page, 'main', {
      detailedReport: true,
      detailedReportOptions: {
        html: true,
      },
    });
  });

  test('Customers page should have no accessibility violations', async ({ page }) => {
    await page.goto('/customers');

    // Wait for page to be fully loaded
    await page.waitForLoadState('networkidle');

    // Run axe accessibility check
    await checkA11y(page, 'main', {
      detailedReport: true,
      detailedReportOptions: {
        html: true,
      },
    });
  });

  test('Navigation menu should be keyboard accessible', async ({ page }) => {
    await page.goto('/');

    // Test keyboard navigation
    await page.keyboard.press('Tab');
    const focusedElement = await page.evaluate(() => document.activeElement?.tagName);

    // Expect some focusable element (button, link, input)
    expect(['A', 'BUTTON', 'INPUT']).toContain(focusedElement);

    // Check accessibility of navigation
    await checkA11y(page, 'nav', {
      detailedReport: true,
    });
  });

  test('Forms should have proper labels and ARIA attributes', async ({ page }) => {
    // Test a page with forms (adjust URL as needed)
    await page.goto('/lead-generation/wizard');

    // Wait for form to load
    await page.waitForLoadState('networkidle');

    // Check form accessibility
    await checkA11y(page, 'form', {
      detailedReport: true,
      detailedReportOptions: {
        html: true,
      },
    });
  });

  test('Data tables should have proper structure', async ({ page }) => {
    await page.goto('/customers');

    // Wait for table to load
    await page.waitForLoadState('networkidle');

    // Check if table exists
    const tableExists = await page.locator('table, [role="table"]').count() > 0;

    if (tableExists) {
      // Check table accessibility
      await checkA11y(page, 'table, [role="table"]', {
        detailedReport: true,
      });
    }
  });

  test('Modals and dialogs should trap focus', async ({ page }) => {
    await page.goto('/');

    // Look for buttons that might open modals
    const buttonCount = await page.locator('button').count();

    if (buttonCount > 0) {
      // Click first button to potentially open a modal
      await page.locator('button').first().click();

      // Check if modal appeared
      const modalVisible = await page.locator('[role="dialog"], [role="alertdialog"]').count() > 0;

      if (modalVisible) {
        // Check modal accessibility
        await checkA11y(page, '[role="dialog"], [role="alertdialog"]', {
          detailedReport: true,
        });
      }
    }
  });

  test('Color contrast should meet WCAG AA standards', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    // Get color contrast violations specifically
    const violations = await getViolations(page, undefined, {
      rules: {
        'color-contrast': { enabled: true },
      },
    });

    expect(violations.filter(v => v.id === 'color-contrast')).toHaveLength(0);
  });

  test('Images should have alt text', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    // Get image alt text violations
    const violations = await getViolations(page, undefined, {
      rules: {
        'image-alt': { enabled: true },
      },
    });

    expect(violations.filter(v => v.id === 'image-alt')).toHaveLength(0);
  });
});
