import { render, screen, waitFor } from '../../../../test/test-utils';
import userEvent from '@testing-library/user-event';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { PainScoreForm } from '../PainScoreForm';
import type { Lead } from '../../types';

// Mock useScoreSchema hook (Component uses Server-Driven UI!)
vi.mock('@/hooks/useScoreSchema', () => ({
  useScoreSchema: vi.fn(() => ({
    data: [
      {
        cardId: 'pain_score',
        title: 'Pain Score',
        sections: [
          {
            sectionId: 'pain_assessment',
            title: 'Schmerzpunkte-Bewertung',
            fields: [
              {
                fieldKey: 'painStaffShortage',
                label: 'Personalmangel',
                type: 'BOOLEAN',
                required: false,
              },
              {
                fieldKey: 'painHighCosts',
                label: 'Hohe Kosten',
                type: 'BOOLEAN',
                required: false,
              },
              {
                fieldKey: 'painFoodWaste',
                label: 'Lebensmittelverschwendung',
                type: 'BOOLEAN',
                required: false,
              },
              {
                fieldKey: 'painQualityInconsistency',
                label: 'Qualitätsschwankungen',
                type: 'BOOLEAN',
                required: false,
              },
              {
                fieldKey: 'painUnreliableDelivery',
                label: 'Unzuverlässige Lieferung',
                type: 'BOOLEAN',
                required: false,
              },
              {
                fieldKey: 'painPoorService',
                label: 'Schlechter Service',
                type: 'BOOLEAN',
                required: false,
              },
              {
                fieldKey: 'painSupplierQuality',
                label: 'Lieferantenqualität',
                type: 'BOOLEAN',
                required: false,
              },
              {
                fieldKey: 'painTimePressure',
                label: 'Zeitdruck',
                type: 'BOOLEAN',
                required: false,
              },
              {
                fieldKey: 'urgencyLevel',
                label: 'Dringlichkeit',
                type: 'ENUM',
                required: false,
                enumSource: '/api/enums/urgency-levels',
              },
              {
                fieldKey: 'painNotes',
                label: 'Notizen',
                type: 'TEXTAREA',
                required: false,
              },
            ],
          },
        ],
      },
    ],
    isLoading: false,
    error: null,
  })),
}));

// Mock useEnumOptions hook (Component uses Server-Driven urgency levels)
vi.mock('@/hooks/useEnumOptions', () => ({
  useEnumOptions: vi.fn((enumSource: string) => {
    // Return urgency level options
    if (enumSource === '/api/enums/urgency-levels') {
      return {
        data: [
          { value: 'NORMAL', label: 'Normal (0 Punkte)' },
          { value: 'HIGH', label: 'Hoch (22 Punkte)' },
          { value: 'URGENT', label: 'Dringend (45 Punkte)' },
        ],
        isLoading: false,
        error: null,
      };
    }
    // Default: empty options
    return {
      data: [],
      isLoading: false,
      error: null,
    };
  }),
}));

// Mock Lead data
const mockLead: Lead = {
  id: 1,
  companyName: 'Test Company',
  painScore: 0,
  painStaffShortage: false,
  painHighCosts: false,
  painFoodWaste: false,
  painQualityInconsistency: false,
  painUnreliableDelivery: false,
  painPoorService: false,
  painSupplierQuality: false,
  painTimePressure: false,
  urgencyLevel: 'NORMAL',
  painNotes: '',
  status: 'REGISTERED',
  ownerUserId: 'user1',
  createdBy: 'user1',
  countryCode: 'DE',
  contacts: [],
  version: 0,
};

describe('PainScoreForm Auto-Save', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should auto-save when pain checkbox changes', async () => {
    const user = userEvent.setup();
    const onUpdate = vi.fn().mockResolvedValue(undefined);

    render(<PainScoreForm lead={mockLead} onUpdate={onUpdate} />);

    // Find and click the "Personalmangel" checkbox
    const checkbox = screen.getByLabelText(/Personalmangel/);
    await user.click(checkbox);

    // Wait for auto-save to trigger (immediate mode)
    await waitFor(
      () => {
        expect(onUpdate).toHaveBeenCalledWith(expect.objectContaining({ painStaffShortage: true }));
      },
      { timeout: 3000 }
    );

    expect(onUpdate).toHaveBeenCalledTimes(1);
  });

  it('should not trigger infinite loop', async () => {
    const user = userEvent.setup();
    const onUpdate = vi.fn().mockResolvedValue(undefined);

    render(<PainScoreForm lead={mockLead} onUpdate={onUpdate} />);

    // Click checkbox
    const checkbox = screen.getByLabelText(/Personalmangel/);
    await user.click(checkbox);

    // Wait for potential multiple calls
    await waitFor(
      () => {
        expect(onUpdate).toHaveBeenCalled();
      },
      { timeout: 5000 }
    );

    // Should only be called ONCE, not in a loop
    expect(onUpdate).toHaveBeenCalledTimes(1);
  });

  it('should display updated score after save', async () => {
    const user = userEvent.setup();
    const mockLeadWithScore = { ...mockLead, painScore: 0 };
    const onUpdate = vi.fn().mockResolvedValue({ painScore: 35 });

    const { rerender } = render(<PainScoreForm lead={mockLeadWithScore} onUpdate={onUpdate} />);

    // Initial score display
    expect(screen.getByText(/Score: 0\/100/)).toBeInTheDocument();

    // Click checkbox
    const checkbox = screen.getByLabelText(/Personalmangel/);
    await user.click(checkbox);

    // Wait for auto-save
    await waitFor(() => expect(onUpdate).toHaveBeenCalled());

    // Simulate parent re-render with updated lead
    rerender(<PainScoreForm lead={{ ...mockLeadWithScore, painScore: 35 }} onUpdate={onUpdate} />);

    // Score should update
    expect(screen.getByText(/Score: 35\/100/)).toBeInTheDocument();
  });

  it('should show "Speichert..." during save', async () => {
    const user = userEvent.setup();
    let resolveUpdate: () => void;
    const updatePromise = new Promise<void>(resolve => {
      resolveUpdate = resolve;
    });

    const onUpdate = vi.fn().mockReturnValue(updatePromise);

    render(<PainScoreForm lead={mockLead} onUpdate={onUpdate} />);

    // Click checkbox
    const checkbox = screen.getByLabelText(/Personalmangel/);
    await user.click(checkbox);

    // Should show "Speichert..." immediately
    await waitFor(() => {
      expect(screen.getByText(/Speichert.../)).toBeInTheDocument();
    });

    // Resolve the promise
    resolveUpdate!();

    // Should show "Gespeichert ✓"
    await waitFor(() => {
      expect(screen.getByText(/Gespeichert ✓/)).toBeInTheDocument();
    });
  });

  it('should update urgency level immediately', async () => {
    const user = userEvent.setup();
    const onUpdate = vi.fn().mockResolvedValue(undefined);

    render(<PainScoreForm lead={mockLead} onUpdate={onUpdate} />);

    // Find urgency select by searching for the text within it
    const urgencySelect = screen.getByText(/Normal \(0 Punkte\)/).closest('[role="combobox"]');
    expect(urgencySelect).toBeTruthy();

    await user.click(urgencySelect!);

    // Click on "Hoch" option
    const highOption = await screen.findByText(/Hoch \(22 Punkte\)/);
    await user.click(highOption);

    // Should trigger auto-save with HIGH urgency
    await waitFor(() => {
      expect(onUpdate).toHaveBeenCalledWith(expect.objectContaining({ urgencyLevel: 'HIGH' }));
    });

    expect(onUpdate).toHaveBeenCalledTimes(1);
  });
});
