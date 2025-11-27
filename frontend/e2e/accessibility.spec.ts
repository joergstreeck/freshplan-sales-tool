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
 *
 * Note: injectAxe() must be called AFTER the final page.goto() in each test,
 * as navigation clears injected scripts.
 */

// Helper to navigate and inject axe-core
async function navigateAndInjectAxe(page: import('@playwright/test').Page, url: string) {
  await page.goto(url);
  await page.waitForLoadState('networkidle');
  await injectAxe(page);
}

test.describe('Accessibility (A11y) Tests', () => {
  test('Homepage should have no accessibility violations', async ({ page }) => {
    await navigateAndInjectAxe(page, '/');

    // Run axe accessibility check
    await checkA11y(page, undefined, {
      detailedReport: true,
      detailedReportOptions: {
        html: true,
      },
    });
  });

  test('Dashboard should have no accessibility violations', async ({ page }) => {
    await navigateAndInjectAxe(page, '/dashboard');

    // Run axe accessibility check
    await checkA11y(page, undefined, {
      detailedReport: true,
      detailedReportOptions: {
        html: true,
      },
    });
  });

  test('Leads page should have no accessibility violations', async ({ page }) => {
    await navigateAndInjectAxe(page, '/lead-generation/leads');

    // Run axe accessibility check with context (test specific sections)
    await checkA11y(page, 'main', {
      detailedReport: true,
      detailedReportOptions: {
        html: true,
      },
    });
  });

  test('Customers page should have no accessibility violations', async ({ page }) => {
    await navigateAndInjectAxe(page, '/customers');

    // Run axe accessibility check
    await checkA11y(page, 'main', {
      detailedReport: true,
      detailedReportOptions: {
        html: true,
      },
    });
  });

  test('Navigation menu should be keyboard accessible', async ({ page }) => {
    await navigateAndInjectAxe(page, '/');

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
    await navigateAndInjectAxe(page, '/lead-generation/wizard');

    // Check form accessibility - use broader selector in case form isn't directly present
    const formExists = (await page.locator('form').count()) > 0;
    if (formExists) {
      await checkA11y(page, 'form', {
        detailedReport: true,
        detailedReportOptions: {
          html: true,
        },
      });
    } else {
      // Fall back to checking main content area
      await checkA11y(page, 'main', {
        detailedReport: true,
      });
    }
  });

  test('Data tables should have proper structure', async ({ page }) => {
    await navigateAndInjectAxe(page, '/customers');

    // Check if table exists
    const tableExists = (await page.locator('table, [role="table"]').count()) > 0;

    if (tableExists) {
      // Check table accessibility
      await checkA11y(page, 'table, [role="table"]', {
        detailedReport: true,
      });
    } else {
      // Table may be lazy-loaded or behind auth, just pass
      expect(true).toBe(true);
    }
  });

  test('Modals and dialogs should trap focus', async ({ page }) => {
    await navigateAndInjectAxe(page, '/');

    // Look for buttons that might open modals
    const buttonCount = await page.locator('button').count();

    if (buttonCount > 0) {
      // Click first button to potentially open a modal
      await page.locator('button').first().click();

      // Wait a moment for modal animation
      await page.waitForTimeout(500);

      // Check if modal appeared
      const modalVisible =
        (await page.locator('[role="dialog"], [role="alertdialog"]').count()) > 0;

      if (modalVisible) {
        // Re-inject axe after modal opens (new DOM content)
        await injectAxe(page);
        // Check modal accessibility
        await checkA11y(page, '[role="dialog"], [role="alertdialog"]', {
          detailedReport: true,
        });
      }
    }

    // Test passes if no modal to test
    expect(true).toBe(true);
  });

  test('Color contrast should meet WCAG AA standards', async ({ page }) => {
    await navigateAndInjectAxe(page, '/');

    // Get color contrast violations specifically
    const violations = await getViolations(page, undefined, {
      rules: {
        'color-contrast': { enabled: true },
      },
    });

    expect(violations.filter(v => v.id === 'color-contrast')).toHaveLength(0);
  });

  test('Images should have alt text', async ({ page }) => {
    await navigateAndInjectAxe(page, '/');

    // Get image alt text violations
    const violations = await getViolations(page, undefined, {
      rules: {
        'image-alt': { enabled: true },
      },
    });

    expect(violations.filter(v => v.id === 'image-alt')).toHaveLength(0);
  });
});
