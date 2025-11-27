import { describe, test, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@/test-utils';
import userEvent from '@testing-library/user-event';
import { EngagementScoreForm } from '../EngagementScoreForm';
import type { Lead } from '../../types';

/**
 * Enterprise-Level Tests for EngagementScoreForm
 *
 * Tests cover:
 * - Auto-save behavior (immediate for dropdowns)
 * - Infinite re-render loop prevention
 * - Field validation
 * - Score calculation triggers
 *
 * NOTE: MUI Select aria-labelledby attributes are not rendered in JSDOM
 * Solution: Use queryAllByRole('combobox') without name filter, access by index
 */

/**
 * Helper: Wait for schema to load and return form elements
 */
async function waitForSchemaAndGetElements(screen: typeof import('@testing-library/react').screen) {
  // Wait for loading spinner to disappear
  await waitFor(
    () => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    },
    { timeout: 10000 }
  );

  // Get all comboboxes (ENUM fields)
  const comboboxes = screen.queryAllByRole('combobox');

  // Get text fields by placeholder (more reliable than getByLabelText in JSDOM)
  const championField = screen.getByPlaceholderText(/Name des Fürsprechers/i);
  const competitorField = screen.getByPlaceholderText(/z\.B\. Metro, CHEFS CULINAR/i);

  // Based on MSW handlers.ts line 329-343, combobox order is:
  // 0: relationshipStatus
  // 1: decisionMakerAccess
  return {
    relationshipDropdown: comboboxes[0],
    decisionMakerDropdown: comboboxes[1],
    championField,
    competitorField,
  };
}

describe('EngagementScoreForm', () => {
  const mockLead: Lead = {
    id: '123',
    companyName: 'Test Company',
    ownerUserId: 'user1',
    status: 'REGISTERED',
    stage: 0,
    registeredAt: new Date().toISOString(),
    relationshipStatus: 'COLD',
    decisionMakerAccess: 'UNKNOWN',
    engagementScore: 0,
    leadScore: 0,
  };

  let onUpdate: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    onUpdate = vi.fn();
    vi.clearAllMocks();
  });

  // ================================================================================
  // AUTO-SAVE BEHAVIOR TESTS
  // ================================================================================

  test('auto-saves immediately on relationship status change', async () => {
    const user = userEvent.setup();
    render(<EngagementScoreForm lead={mockLead} onUpdate={onUpdate} />);

    // Wait for schema to load and get form elements
    const { relationshipDropdown } = await waitForSchemaAndGetElements(screen);

    // Click relationship status dropdown
    await user.click(relationshipDropdown);

    // Select "ADVOCATE" option
    const advocateOption = await screen.findByText(/Kämpft aktiv für uns/i);
    await user.click(advocateOption);

    // Should trigger onUpdate immediately (no debounce for dropdowns)
    await waitFor(() => {
      expect(onUpdate).toHaveBeenCalledTimes(1);
      expect(onUpdate).toHaveBeenCalledWith(
        expect.objectContaining({
          relationshipStatus: 'ADVOCATE',
        })
      );
    });
  });

  test('auto-saves immediately on decision maker access change', async () => {
    const user = userEvent.setup();
    render(<EngagementScoreForm lead={mockLead} onUpdate={onUpdate} />);

    // Wait for schema to load and get form elements
    const { decisionMakerDropdown } = await waitForSchemaAndGetElements(screen);

    // Click decision maker access dropdown
    await user.click(decisionMakerDropdown);

    // Select "IS_DECISION_MAKER" option
    const isDecisionMakerOption = await screen.findByText(/Unser Kontakt IST der Entscheider/i);
    await user.click(isDecisionMakerOption);

    // Should trigger onUpdate immediately
    await waitFor(() => {
      expect(onUpdate).toHaveBeenCalledTimes(1);
      expect(onUpdate).toHaveBeenCalledWith(
        expect.objectContaining({
          decisionMakerAccess: 'IS_DECISION_MAKER',
        })
      );
    });
  });

  test('auto-saves internal champion name with 2s debounce', async () => {
    const user = userEvent.setup();
    render(<EngagementScoreForm lead={mockLead} onUpdate={onUpdate} />);

    // Wait for schema to load and get form elements
    const { championField } = await waitForSchemaAndGetElements(screen);

    // Type champion name
    await user.clear(championField);
    await user.type(championField, 'Max Mustermann');

    // Should NOT save immediately
    expect(onUpdate).not.toHaveBeenCalled();

    // Wait for debounce (2 seconds)
    await waitFor(
      () => {
        expect(onUpdate).toHaveBeenCalledTimes(1);
        expect(onUpdate).toHaveBeenCalledWith(
          expect.objectContaining({
            internalChampionName: 'Max Mustermann',
          })
        );
      },
      { timeout: 3000 }
    );
  });

  // ================================================================================
  // INFINITE LOOP PREVENTION TESTS
  // ================================================================================

  test('prevents infinite re-render loop on score updates', async () => {
    const user = userEvent.setup();

    // Mock onUpdate that returns updated lead with new score
    const mockUpdateWithScore = vi.fn(updates => {
      // Simulate backend returning updated scores
      return Promise.resolve({
        ...mockLead,
        ...updates,
        engagementScore: 50, // Backend calculates this
        leadScore: 40,
      });
    });

    render(<EngagementScoreForm lead={mockLead} onUpdate={mockUpdateWithScore} />);

    // Wait for schema to load and get form elements
    const { relationshipDropdown } = await waitForSchemaAndGetElements(screen);

    // Change relationship status
    await user.click(relationshipDropdown);

    const engagedOption = await screen.findByText(/Mehrere Touchpoints, positiv/i);
    await user.click(engagedOption);

    // Wait for update
    await waitFor(() => {
      expect(mockUpdateWithScore).toHaveBeenCalled();
    });

    // Should NOT trigger infinite loop (onUpdate called only once)
    await new Promise(resolve => setTimeout(resolve, 100));
    expect(mockUpdateWithScore).toHaveBeenCalledTimes(1);
  });

  test('handles rapid changes without multiple saves', async () => {
    const user = userEvent.setup();
    render(<EngagementScoreForm lead={mockLead} onUpdate={onUpdate} />);

    // Wait for schema to load and get form elements
    const { championField } = await waitForSchemaAndGetElements(screen);

    // Rapid typing (should debounce)
    await user.clear(championField);
    await user.type(championField, 'A');
    await user.type(championField, 'n');
    await user.type(championField, 'n');
    await user.type(championField, 'a');

    // Wait for debounce
    await waitFor(
      () => {
        expect(onUpdate).toHaveBeenCalled();
      },
      { timeout: 3000 }
    );

    // Should only save once after debounce, not 4 times
    expect(onUpdate).toHaveBeenCalledTimes(1);
    expect(onUpdate).toHaveBeenCalledWith(
      expect.objectContaining({
        internalChampionName: 'Anna',
      })
    );
  });

  // ================================================================================
  // FIELD VALIDATION TESTS
  // ================================================================================

  test(
    'shows all relationship status options',
    async () => {
      const user = userEvent.setup();
      render(<EngagementScoreForm lead={mockLead} onUpdate={onUpdate} />);

    // Wait for schema to load and get form elements
    const { relationshipDropdown } = await waitForSchemaAndGetElements(screen);

    await user.click(relationshipDropdown);

    // Verify all options are present (MUI Dropdown rendering can take time)
    await waitFor(
      () => {
        const options = screen.getAllByRole('option');
        expect(options).toHaveLength(6);
        expect(screen.getByRole('option', { name: /Kein Kontakt/i })).toBeInTheDocument();
        expect(screen.getByRole('option', { name: /Erstkontakt hergestellt/i })).toBeInTheDocument();
        expect(screen.getByRole('option', { name: /Mehrere Touchpoints, skeptisch/i })).toBeInTheDocument();
        expect(screen.getByRole('option', { name: /Mehrere Touchpoints, positiv/i })).toBeInTheDocument();
        expect(screen.getByRole('option', { name: /Vertrauensbasis vorhanden/i })).toBeInTheDocument();
        expect(screen.getByRole('option', { name: /Kämpft aktiv für uns/i })).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
    },
    15000
  );

  test(
    'shows all decision maker access options',
    async () => {
    const user = userEvent.setup();
    render(<EngagementScoreForm lead={mockLead} onUpdate={onUpdate} />);

    // Wait for schema to load and get form elements
    const { decisionMakerDropdown } = await waitForSchemaAndGetElements(screen);

    await user.click(decisionMakerDropdown);

    // Verify all options are present (MUI Dropdown rendering can take time)
    await waitFor(
      () => {
        const options = screen.getAllByRole('option');
        expect(options).toHaveLength(5);
        expect(screen.getByRole('option', { name: /Noch nicht identifiziert/i })).toBeInTheDocument();
        expect(screen.getByRole('option', { name: /Entscheider bekannt, aber Zugang blockiert/i })).toBeInTheDocument();
        expect(screen.getByRole('option', { name: /Zugang über Dritte \(Assistent, Mitarbeiter, Partner\)/i })).toBeInTheDocument();
        expect(screen.getByRole('option', { name: /Direkter Kontakt zum Entscheider/i })).toBeInTheDocument();
        expect(screen.getByRole('option', { name: /Unser Kontakt IST der Entscheider/i })).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
    },
    15000
  );

  test('internal champion field accepts text input', async () => {
    const user = userEvent.setup();
    render(<EngagementScoreForm lead={mockLead} onUpdate={onUpdate} />);

    // Wait for schema to load and get form elements
    const { championField } = await waitForSchemaAndGetElements(screen);

    await user.clear(championField);
    await user.type(championField, 'Dr. Maria Schmidt');

    expect(championField).toHaveValue('Dr. Maria Schmidt');
  });

  test('competitor field accepts text input', async () => {
    const user = userEvent.setup();
    render(<EngagementScoreForm lead={mockLead} onUpdate={onUpdate} />);

    // Wait for schema to load and get form elements
    const { competitorField } = await waitForSchemaAndGetElements(screen);

    await user.clear(competitorField);
    await user.type(competitorField, 'Metro AG');

    expect(competitorField).toHaveValue('Metro AG');
  });

  // ================================================================================
  // SCORE CALCULATION TRIGGER TESTS
  // ================================================================================

  test('updates engagement score when relationship status changes to high value', async () => {
    const user = userEvent.setup();

    const leadWithScore = {
      ...mockLead,
      relationshipStatus: 'COLD' as const,
      engagementScore: 0,
    };

    render(<EngagementScoreForm lead={leadWithScore} onUpdate={onUpdate} />);

    // Wait for schema to load and get form elements
    const { relationshipDropdown } = await waitForSchemaAndGetElements(screen);

    // Change to ADVOCATE (highest value)
    await user.click(relationshipDropdown);

    const advocateOption = await screen.findByText(/Kämpft aktiv für uns/i);
    await user.click(advocateOption);

    await waitFor(() => {
      expect(onUpdate).toHaveBeenCalledWith(
        expect.objectContaining({
          relationshipStatus: 'ADVOCATE',
        })
      );
    });
  });

  test('updates engagement score when decision maker access improves', async () => {
    const user = userEvent.setup();

    const leadWithScore = {
      ...mockLead,
      decisionMakerAccess: 'UNKNOWN' as const,
      engagementScore: 0,
    };

    render(<EngagementScoreForm lead={leadWithScore} onUpdate={onUpdate} />);

    // Wait for schema to load and get form elements
    const { decisionMakerDropdown } = await waitForSchemaAndGetElements(screen);

    // Change to IS_DECISION_MAKER (highest value)
    await user.click(decisionMakerDropdown);

    const isDecisionMakerOption = await screen.findByText(/Unser Kontakt IST der Entscheider/i);
    await user.click(isDecisionMakerOption);

    await waitFor(() => {
      expect(onUpdate).toHaveBeenCalledWith(
        expect.objectContaining({
          decisionMakerAccess: 'IS_DECISION_MAKER',
        })
      );
    });
  });

  test('adding internal champion should trigger score update', async () => {
    const user = userEvent.setup();
    render(<EngagementScoreForm lead={mockLead} onUpdate={onUpdate} />);

    // Wait for schema to load and get form elements
    const { championField } = await waitForSchemaAndGetElements(screen);

    await user.clear(championField);
    await user.type(championField, 'Anna Schmidt');

    // Wait for debounced save
    await waitFor(
      () => {
        expect(onUpdate).toHaveBeenCalledWith(
          expect.objectContaining({
            internalChampionName: 'Anna Schmidt',
          })
        );
      },
      { timeout: 3000 }
    );
  });
});
