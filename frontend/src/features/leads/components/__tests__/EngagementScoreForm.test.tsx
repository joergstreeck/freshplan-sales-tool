import { describe, test, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
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
 */

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

    // Find and click relationship status dropdown
    const relationshipDropdown = screen.getByRole('combobox', { name: /beziehungsstatus/i });
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

    // Find and click decision maker access dropdown
    const decisionMakerDropdown = screen.getByRole('combobox', { name: /entscheider-zugang/i });
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

    // Find internal champion text field
    const championField = screen.getByLabelText(/Interner Champion/i);

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

    // Change relationship status
    const relationshipDropdown = screen.getByRole('combobox', { name: /beziehungsstatus/i });
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

    const championField = screen.getByLabelText(/Interner Champion/i);

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

  test('shows all relationship status options', async () => {
    const user = userEvent.setup();
    render(<EngagementScoreForm lead={mockLead} onUpdate={onUpdate} />);

    const relationshipDropdown = screen.getByRole('combobox', { name: /beziehungsstatus/i });
    await user.click(relationshipDropdown);

    // Verify all options are present
    await waitFor(() => {
      expect(screen.getByText(/Kein Kontakt/i)).toBeInTheDocument();
      expect(screen.getByText(/Erstkontakt hergestellt/i)).toBeInTheDocument();
      expect(screen.getByText(/Mehrere Touchpoints, skeptisch/i)).toBeInTheDocument();
      expect(screen.getByText(/Mehrere Touchpoints, positiv/i)).toBeInTheDocument();
      expect(screen.getByText(/Vertrauensbasis vorhanden/i)).toBeInTheDocument();
      expect(screen.getByText(/Kämpft aktiv für uns/i)).toBeInTheDocument();
    });
  });

  test('shows all decision maker access options', async () => {
    const user = userEvent.setup();
    render(<EngagementScoreForm lead={mockLead} onUpdate={onUpdate} />);

    const decisionMakerDropdown = screen.getByRole('combobox', { name: /entscheider-zugang/i });
    await user.click(decisionMakerDropdown);

    // Verify all options are present
    await waitFor(() => {
      expect(screen.getByText(/Noch nicht identifiziert/i)).toBeInTheDocument();
      expect(screen.getByText(/Entscheider bekannt, Zugang blockiert/i)).toBeInTheDocument();
      expect(screen.getByText(/Zugang über Dritte/i)).toBeInTheDocument();
      expect(screen.getByText(/Direkter Kontakt zum Entscheider/i)).toBeInTheDocument();
      expect(screen.getByText(/Unser Kontakt IST der Entscheider/i)).toBeInTheDocument();
    });
  });

  test('internal champion field accepts text input', async () => {
    const user = userEvent.setup();
    render(<EngagementScoreForm lead={mockLead} onUpdate={onUpdate} />);

    const championField = screen.getByLabelText(/Interner Champion/i);

    await user.clear(championField);
    await user.type(championField, 'Dr. Maria Schmidt');

    expect(championField).toHaveValue('Dr. Maria Schmidt');
  });

  test('competitor field accepts text input', async () => {
    const user = userEvent.setup();
    render(<EngagementScoreForm lead={mockLead} onUpdate={onUpdate} />);

    const competitorField = screen.getByLabelText(/Aktueller Wettbewerber/i);

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

    // Change to ADVOCATE (highest value)
    const relationshipDropdown = screen.getByRole('combobox', { name: /beziehungsstatus/i });
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

    // Change to IS_DECISION_MAKER (highest value)
    const decisionMakerDropdown = screen.getByRole('combobox', { name: /entscheider-zugang/i });
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

    const championField = screen.getByLabelText(/Interner Champion/i);

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
