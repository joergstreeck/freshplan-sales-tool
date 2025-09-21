import { test, expect } from '@playwright/test';

/**
 * Hybrid E2E Tests: KI-Framework + echte Communication-User-Journeys
 * Basiert auf den echten Business-Workflows aus der alten Planung
 */

test.describe('Communication Module E2E', () => {
  test.beforeEach(async ({ page }) => {
    // Setup: Login mit Territory-berechtigung
    await page.goto('/login');
    await page.fill('[data-testid="username"]', 'test@freshplan.de');
    await page.fill('[data-testid="password"]', 'test123');
    await page.click('[data-testid="login-button"]');

    // Warte auf erfolgreichen Login
    await expect(page.locator('[data-testid="user-menu"]')).toBeVisible();
  });

  test('complete communication workflow - email thread creation and reply', async ({ page }) => {
    // Navigation zur Communication-Page
    await page.goto('/communication');
    await expect(page).toHaveTitle(/FreshPlan/);

    // Step 1: Create new email thread
    await page.click('[data-testid="new-thread-button"]');
    await expect(page.locator('[data-testid="thread-dialog"]')).toBeVisible();

    // Echte Business-Logic: Email-Thread mit Customer-Selection
    await page.selectOption('[data-testid="customer-select"]', 'customer-123');
    await page.fill('[data-testid="subject-input"]', 'Sample Follow-up T+3 - Geschmackstest Anfrage');
    await page.selectOption('[data-testid="channel-select"]', 'EMAIL');
    await page.fill('[data-testid="recipient-input"]', 'kunde@restaurant.de');
    await page.fill('[data-testid="message-body"]', 'Hallo, wie ist der Geschmackstest verlaufen?');

    await page.click('[data-testid="send-thread-button"]');

    // Step 2: Verify thread appears in list
    await expect(page.locator('[data-testid="thread-list"]')).toBeVisible();
    await expect(page.locator('text=Sample Follow-up T+3')).toBeVisible();
    await expect(page.locator('[data-testid="channel-chip-EMAIL"]')).toBeVisible();

    // Step 3: Open thread for reply
    await page.click('text=Sample Follow-up T+3');
    await expect(page.locator('[data-testid="thread-detail"]')).toBeVisible();

    // Step 4: Reply to thread (ETag-optimistic-locking)
    await page.click('[data-testid="reply-button"]');
    await expect(page.locator('[data-testid="reply-composer"]')).toBeVisible();

    await page.fill('[data-testid="reply-body"]', 'Vielen Dank für Ihr Feedback! Können wir einen Termin vereinbaren?');
    await page.click('[data-testid="send-reply-button"]');

    // Step 5: Verify reply appears in thread
    await expect(page.locator('text=Vielen Dank für Ihr Feedback!')).toBeVisible();

    // Business-Logic: Unread-Count sollte sich aktualisieren
    await expect(page.locator('[data-testid="unread-count"]')).toHaveText('0');
  });

  test('territory-based access control in UI', async ({ page }) => {
    // Business-Logic: Territory-Filtering sollte in UI sichtbar sein
    await page.goto('/communication');

    // Filter by territory
    await page.selectOption('[data-testid="territory-filter"]', 'BER');

    // Nur Threads für Berlin-Territory sollten sichtbar sein
    const threadItems = page.locator('[data-testid="thread-item"]');
    await expect(threadItems.first()).toContainText('BER');

    // Switch to Munich territory
    await page.selectOption('[data-testid="territory-filter"]', 'MUNICH');

    // Threads sollten sich ändern
    await expect(threadItems.first()).toContainText('MUNICH');
  });

  test('phone call logging workflow', async ({ page }) => {
    // Echte Business-Logic: Phone-Call-Logging aus CommActivity
    await page.goto('/communication');

    await page.click('[data-testid="log-call-button"]');
    await expect(page.locator('[data-testid="call-dialog"]')).toBeVisible();

    // Phone-Call-Details
    await page.selectOption('[data-testid="customer-select"]', 'customer-456');
    await page.selectOption('[data-testid="call-direction"]', 'OUTBOUND');
    await page.fill('[data-testid="call-notes"]', 'Anruf bezüglich Liefertermin. Kunde zufrieden.');
    await page.fill('[data-testid="call-duration"]', '15'); // Minuten

    await page.click('[data-testid="save-call-button"]');

    // Verify call appears as thread
    await expect(page.locator('text=Telefonat')).toBeVisible();
    await expect(page.locator('[data-testid="channel-chip-PHONE"]')).toBeVisible();
    await expect(page.locator('text=15 Min')).toBeVisible();
  });

  test('meeting scheduling and documentation', async ({ page }) => {
    // Business-Logic: Meeting-Documentation aus CommActivity
    await page.goto('/communication');

    await page.click('[data-testid="schedule-meeting-button"]');
    await expect(page.locator('[data-testid="meeting-dialog"]')).toBeVisible();

    // Meeting-Details
    await page.selectOption('[data-testid="customer-select"]', 'customer-789');
    await page.fill('[data-testid="meeting-subject"]', 'Quartalsplanung 2025 - Menüentwicklung');
    await page.fill('[data-testid="meeting-date"]', '2025-10-15');
    await page.fill('[data-testid="meeting-time"]', '14:00');
    await page.fill('[data-testid="meeting-location"]', 'Restaurant vor Ort');
    await page.fill('[data-testid="meeting-participants"]', 'Küchenchef, Einkaufsleiter');
    await page.fill('[data-testid="meeting-agenda"]', 'Neue Wintergerichte besprechen, Lieferlogistik optimieren');

    await page.click('[data-testid="save-meeting-button"]');

    // Verify meeting appears as thread
    await expect(page.locator('text=Quartalsplanung 2025')).toBeVisible();
    await expect(page.locator('[data-testid="channel-chip-MEETING"]')).toBeVisible();
    await expect(page.locator('text=15.10.2025')).toBeVisible();
  });

  test('bounce handling and email status tracking', async ({ page }) => {
    // Business-Logic: Email-Bounce-Tracking aus BounceEvent
    await page.goto('/communication');

    // Simulate email with bounce
    await page.click('[data-testid="thread-item"]'); // Existing email thread
    await expect(page.locator('[data-testid="thread-detail"]')).toBeVisible();

    // Check for bounce status
    const bounceIndicator = page.locator('[data-testid="bounce-status"]');
    if (await bounceIndicator.isVisible()) {
      await expect(bounceIndicator).toContainText('E-Mail zugestellt');
    }

    // Check message status
    await expect(page.locator('[data-testid="message-status-SENT"]')).toBeVisible();
  });

  test('SLA follow-up automation visibility', async ({ page }) => {
    // Business-Logic: SLA-Engine T+3/T+7 Follow-ups
    await page.goto('/communication');

    // Filter for SLA-triggered threads
    await page.click('[data-testid="filter-button"]');
    await page.check('[data-testid="filter-sla-followups"]');

    // Should show SLA-generated follow-up threads
    await expect(page.locator('text=Follow-up T+3')).toBeVisible();
    await expect(page.locator('text=Follow-up T+7')).toBeVisible();

    // Check SLA badge
    await expect(page.locator('[data-testid="sla-badge"]')).toBeVisible();
  });
});