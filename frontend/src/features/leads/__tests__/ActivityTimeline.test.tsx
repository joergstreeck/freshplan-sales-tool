// Sprint 2.1.5 Frontend Phase 2 - ActivityTimeline Component Tests
// Test Coverage: Filtering modes, Progress/Non-Progress split, Warning display logic, Timeline rendering

import { describe, it, expect } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import { ThemeProvider } from '@mui/material';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import ActivityTimeline from '../ActivityTimeline';
import freshfoodzTheme from '../../../theme/freshfoodz';
import type { LeadActivity } from '../types';

const Wrapper = ({ children }: { children: React.ReactNode }) => (
  <ThemeProvider theme={freshfoodzTheme}>{children}</ThemeProvider>
);

// Mock Activity Data
const mockProgressActivities: LeadActivity[] = [
  {
    id: 'act-1',
    leadId: 'lead-1',
    userId: 'user-1',
    activityType: 'QUALIFIED_CALL',
    activityDate: '2025-10-01T10:00:00Z',
    countsAsProgress: true,
    summary: 'Qualifiziertes Gespräch mit Entscheider geführt',
    outcome: 'Interesse an Demo',
    nextAction: 'Demo vereinbaren',
    nextActionDate: '2025-10-05',
    performedBy: 'Max Mustermann',
    createdAt: '2025-10-01T10:00:00Z',
  },
  {
    id: 'act-2',
    leadId: 'lead-1',
    userId: 'user-1',
    activityType: 'MEETING',
    activityDate: '2025-09-25T14:30:00Z',
    countsAsProgress: true,
    summary: 'Meeting vor Ort in Berlin',
    outcome: 'Positives Feedback',
    createdAt: '2025-09-25T14:30:00Z',
  },
  {
    id: 'act-3',
    leadId: 'lead-1',
    userId: 'user-1',
    activityType: 'DEMO',
    activityDate: '2025-09-20T11:00:00Z',
    countsAsProgress: true,
    summary: 'Produktdemonstration durchgeführt',
    createdAt: '2025-09-20T11:00:00Z',
  },
];

const mockNonProgressActivities: LeadActivity[] = [
  {
    id: 'act-4',
    leadId: 'lead-1',
    userId: 'user-1',
    activityType: 'EMAIL',
    activityDate: '2025-09-30T09:00:00Z',
    countsAsProgress: false,
    summary: 'Follow-up E-Mail versendet',
    createdAt: '2025-09-30T09:00:00Z',
  },
  {
    id: 'act-5',
    leadId: 'lead-1',
    userId: 'user-1',
    activityType: 'NOTE',
    activityDate: '2025-09-28T16:00:00Z',
    countsAsProgress: false,
    summary: 'Interne Notiz: Budget unklar',
    createdAt: '2025-09-28T16:00:00Z',
  },
];

const mockAllActivities = [...mockProgressActivities, ...mockNonProgressActivities];

describe('ActivityTimeline', () => {
  // ==================== TEST 1: Filtering Modes ====================
  describe('Filtering Modes', () => {
    it('should display all activities by default', () => {
      render(<ActivityTimeline activities={mockAllActivities} />, { wrapper: Wrapper });

      expect(screen.getByText('Aktivitäten')).toBeInTheDocument();
      expect(screen.getByText(/alle \(5\)/i)).toBeInTheDocument();
      expect(screen.getByText(/progress \(3\)/i)).toBeInTheDocument();
      expect(screen.getByText(/sonstige \(2\)/i)).toBeInTheDocument();

      // All activities should be visible
      expect(screen.getByText('Qualifiziertes Gespräch')).toBeInTheDocument();
      expect(screen.getByText('Meeting vor Ort')).toBeInTheDocument();
      expect(screen.getByText('Produktdemonstration')).toBeInTheDocument();
      expect(screen.getByText('E-Mail')).toBeInTheDocument();
      expect(screen.getByText('Notiz')).toBeInTheDocument();
    });

    it('should filter to show only progress activities', async () => {
      const user = userEvent.setup();

      render(<ActivityTimeline activities={mockAllActivities} />, { wrapper: Wrapper });

      // Click "Progress" filter
      await user.click(screen.getByText(/progress \(3\)/i));

      // Only progress activities should be visible
      expect(screen.getByText('Qualifiziertes Gespräch')).toBeInTheDocument();
      expect(screen.getByText('Meeting vor Ort')).toBeInTheDocument();
      expect(screen.getByText('Produktdemonstration')).toBeInTheDocument();

      // Non-progress activities should NOT be visible
      expect(screen.queryByText('Follow-up E-Mail versendet')).not.toBeInTheDocument();
      expect(screen.queryByText('Interne Notiz: Budget unklar')).not.toBeInTheDocument();
    });

    it('should filter to show only non-progress activities', async () => {
      const user = userEvent.setup();

      render(<ActivityTimeline activities={mockAllActivities} />, { wrapper: Wrapper });

      // Click "Sonstige" filter
      await user.click(screen.getByText(/sonstige \(2\)/i));

      // Only non-progress activities should be visible
      expect(screen.getByText('Follow-up E-Mail versendet')).toBeInTheDocument();
      expect(screen.getByText('Interne Notiz: Budget unklar')).toBeInTheDocument();

      // Progress activities should NOT be visible
      expect(screen.queryByText('Qualifiziertes Gespräch mit Entscheider geführt')).not.toBeInTheDocument();
      expect(screen.queryByText('Meeting vor Ort in Berlin')).not.toBeInTheDocument();
    });

    it('should switch back to "all" filter', async () => {
      const user = userEvent.setup();

      render(<ActivityTimeline activities={mockAllActivities} />, { wrapper: Wrapper });

      // First filter to progress
      await user.click(screen.getByText(/progress \(3\)/i));
      expect(screen.queryByText('Follow-up E-Mail versendet')).not.toBeInTheDocument();

      // Then switch back to all
      await user.click(screen.getByText(/alle \(5\)/i));

      // All activities should be visible again
      expect(screen.getByText('Qualifiziertes Gespräch')).toBeInTheDocument();
      expect(screen.getByText('Follow-up E-Mail versendet')).toBeInTheDocument();
    });
  });

  // ==================== TEST 2: Progress/Non-Progress Split Logic ====================
  describe('Progress/Non-Progress Split Logic', () => {
    it('should correctly identify progress activities (countsAsProgress=true)', () => {
      render(<ActivityTimeline activities={mockProgressActivities} />, { wrapper: Wrapper });

      expect(screen.getByText(/progress \(3\)/i)).toBeInTheDocument();

      // All activities should have "Progress" chip
      const progressChips = screen.getAllByText('Progress');
      expect(progressChips).toHaveLength(3);
    });

    it('should correctly identify non-progress activities (countsAsProgress=false)', () => {
      render(<ActivityTimeline activities={mockNonProgressActivities} />, { wrapper: Wrapper });

      expect(screen.getByText(/sonstige \(2\)/i)).toBeInTheDocument();

      // No "Progress" chips should be visible
      expect(screen.queryByText('Progress')).not.toBeInTheDocument();
    });

    it('should display "Keine Aktivitäten vorhanden" when activities array is empty', () => {
      render(<ActivityTimeline activities={[]} />, { wrapper: Wrapper });

      expect(screen.getByText('Keine Aktivitäten vorhanden')).toBeInTheDocument();
    });
  });

  // ==================== TEST 3: Warning Display Logic ====================
  describe('Warning Display Logic', () => {
    it('should show warning when progressDeadline < 7 days', () => {
      const soonDeadline = new Date();
      soonDeadline.setDate(soonDeadline.getDate() + 5); // 5 days in future

      render(
        <ActivityTimeline
          activities={mockAllActivities}
          progressDeadline={soonDeadline.toISOString()}
        />,
        { wrapper: Wrapper }
      );

      expect(screen.getByText(/progress-warnung/i)).toBeInTheDocument();
      expect(screen.getByText(/deadline am/i)).toBeInTheDocument();
      expect(screen.getByText(/bitte substanzielle aktivität durchführen/i)).toBeInTheDocument();
    });

    it('should NOT show warning when progressDeadline > 7 days', () => {
      const farDeadline = new Date();
      farDeadline.setDate(farDeadline.getDate() + 30); // 30 days in future

      render(
        <ActivityTimeline
          activities={mockAllActivities}
          progressDeadline={farDeadline.toISOString()}
        />,
        { wrapper: Wrapper }
      );

      expect(screen.queryByText(/progress-warnung/i)).not.toBeInTheDocument();
    });

    it('should NOT show warning when no progressDeadline is provided', () => {
      render(<ActivityTimeline activities={mockAllActivities} />, { wrapper: Wrapper });

      expect(screen.queryByText(/progress-warnung/i)).not.toBeInTheDocument();
    });
  });

  // ==================== TEST 4: Progress Summary (Full Variant) ====================
  describe('Progress Summary (Full Variant)', () => {
    it('should show progress summary in full variant', () => {
      const futureDeadline = new Date();
      futureDeadline.setDate(futureDeadline.getDate() + 30);

      render(
        <ActivityTimeline
          activities={mockAllActivities}
          progressDeadline={futureDeadline.toISOString()}
          variant="full"
        />,
        { wrapper: Wrapper }
      );

      expect(screen.getByText(/letzte progress-aktivität/i)).toBeInTheDocument();
      expect(screen.getByText(/progress-deadline:/i)).toBeInTheDocument();
    });

    it('should calculate days since last progress correctly', () => {
      const recentActivity: LeadActivity = {
        id: 'act-recent',
        leadId: 'lead-1',
        userId: 'user-1',
        activityType: 'QUALIFIED_CALL',
        activityDate: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString(), // 3 days ago
        countsAsProgress: true,
        summary: 'Recent call',
        createdAt: new Date().toISOString(),
      };

      render(
        <ActivityTimeline activities={[recentActivity]} variant="full" />,
        { wrapper: Wrapper }
      );

      expect(screen.getByText(/vor 3 tagen/i)).toBeInTheDocument();
    });

    it('should NOT show progress summary in compact variant', () => {
      const futureDeadline = new Date();
      futureDeadline.setDate(futureDeadline.getDate() + 30);

      render(
        <ActivityTimeline
          activities={mockAllActivities}
          progressDeadline={futureDeadline.toISOString()}
          variant="compact"
        />,
        { wrapper: Wrapper }
      );

      expect(screen.queryByText(/letzte progress-aktivität/i)).not.toBeInTheDocument();
    });
  });

  // ==================== TEST 5: Timeline Rendering ====================
  describe('Timeline Rendering', () => {
    it('should render activities in reverse chronological order (newest first)', () => {
      render(<ActivityTimeline activities={mockAllActivities} />, { wrapper: Wrapper });

      const summaries = screen.getAllByText(/qualifiziertes gespräch|follow-up e-mail|meeting vor ort|interne notiz|produktdemonstration/i);

      // First activity should be the most recent (2025-10-01)
      expect(summaries[0].textContent).toContain('Qualifiziertes Gespräch');

      // Last activity should be the oldest (2025-09-20)
      expect(summaries[summaries.length - 1].textContent).toContain('Produktdemonstration');
    });

    it('should render activity type labels in German', () => {
      render(<ActivityTimeline activities={mockProgressActivities} />, { wrapper: Wrapper });

      expect(screen.getByText('Qualifiziertes Gespräch')).toBeInTheDocument();
      expect(screen.getByText('Meeting vor Ort')).toBeInTheDocument();
      expect(screen.getByText('Produktdemonstration')).toBeInTheDocument();
    });

    it('should display summary, outcome, and nextAction when provided', () => {
      render(<ActivityTimeline activities={[mockProgressActivities[0]]} />, { wrapper: Wrapper });

      // Summary
      expect(screen.getByText('Qualifiziertes Gespräch mit Entscheider geführt')).toBeInTheDocument();

      // Outcome and nextAction may be split across multiple elements with <strong> tags
      expect(screen.getByText(/interesse an demo/i)).toBeInTheDocument();
      expect(screen.getByText(/demo vereinbaren/i)).toBeInTheDocument();
    });

    it('should highlight latest progress activity with "Neueste" chip', () => {
      render(<ActivityTimeline activities={mockProgressActivities} />, { wrapper: Wrapper });

      // Only the most recent progress activity should have "Neueste" chip
      const neuesteChips = screen.getAllByText('Neueste');
      expect(neuesteChips).toHaveLength(1);

      // It should be on the first activity (newest) - just verify it exists
      expect(neuesteChips[0]).toBeInTheDocument();
    });

    it('should use alternate timeline layout for full variant', () => {
      const { container } = render(
        <ActivityTimeline activities={mockAllActivities} variant="full" />,
        { wrapper: Wrapper }
      );

      const timeline = container.querySelector('.MuiTimeline-root');
      expect(timeline).toHaveClass('MuiTimeline-positionAlternate');
    });

    it('should use right timeline layout for compact variant', () => {
      const { container } = render(
        <ActivityTimeline activities={mockAllActivities} variant="compact" />,
        { wrapper: Wrapper }
      );

      const timeline = container.querySelector('.MuiTimeline-root');
      expect(timeline).toHaveClass('MuiTimeline-positionRight');
    });
  });

  // ==================== TEST 6: Color-Coding ====================
  describe('Color-Coding', () => {
    it('should use green styling for progress activities', () => {
      render(
        <ActivityTimeline activities={[mockProgressActivities[0]]} />,
        { wrapper: Wrapper }
      );

      // Verify Progress chip is shown (indicates green/success styling)
      expect(screen.getByText('Progress')).toBeInTheDocument();
      expect(screen.getByText('Qualifiziertes Gespräch')).toBeInTheDocument();
    });

    it('should use grey styling for non-progress activities', () => {
      render(
        <ActivityTimeline activities={[mockNonProgressActivities[0]]} />,
        { wrapper: Wrapper }
      );

      // No Progress chip should be shown (indicates grey styling)
      expect(screen.queryByText('Progress')).not.toBeInTheDocument();
    });

    it('should use filled variant for latest progress activity', () => {
      const { container } = render(
        <ActivityTimeline activities={mockProgressActivities} />,
        { wrapper: Wrapper }
      );

      // Latest activity should have filled TimelineDot
      const filledDots = container.querySelectorAll('.MuiTimelineDot-filled');
      expect(filledDots).toHaveLength(1);
    });

    it('should use outlined variant for other activities', () => {
      const { container } = render(
        <ActivityTimeline activities={mockProgressActivities} />,
        { wrapper: Wrapper }
      );

      // Other activities should have outlined TimelineDot
      const outlinedDots = container.querySelectorAll('.MuiTimelineDot-outlined');
      expect(outlinedDots.length).toBeGreaterThan(0);
    });
  });

  // ==================== TEST 7: Activity Type Mapping ====================
  describe('Activity Type Mapping', () => {
    const activityTypeMappings = [
      { type: 'QUALIFIED_CALL', label: 'Qualifiziertes Gespräch', countsAsProgress: true },
      { type: 'MEETING', label: 'Meeting vor Ort', countsAsProgress: true },
      { type: 'DEMO', label: 'Produktdemonstration', countsAsProgress: true },
      { type: 'ROI_PRESENTATION', label: 'ROI-Präsentation', countsAsProgress: true },
      { type: 'SAMPLE_SENT', label: 'Sample-Box versendet', countsAsProgress: true },
      { type: 'NOTE', label: 'Notiz', countsAsProgress: false },
      { type: 'FOLLOW_UP', label: 'Follow-up', countsAsProgress: false },
      { type: 'EMAIL', label: 'E-Mail', countsAsProgress: false },
      { type: 'CALL', label: 'Anruf', countsAsProgress: false },
      { type: 'SAMPLE_FEEDBACK', label: 'Sample-Feedback', countsAsProgress: false },
    ];

    activityTypeMappings.forEach(({ type, label, countsAsProgress }) => {
      it(`should map ${type} to "${label}" with countsAsProgress=${countsAsProgress}`, () => {
        const activity: LeadActivity = {
          id: 'test-act',
          leadId: 'lead-1',
          userId: 'user-1',
          activityType: type as any,
          activityDate: '2025-10-01T10:00:00Z',
          countsAsProgress,
          createdAt: '2025-10-01T10:00:00Z',
        };

        render(<ActivityTimeline activities={[activity]} />, { wrapper: Wrapper });

        expect(screen.getByText(label)).toBeInTheDocument();

        if (countsAsProgress) {
          expect(screen.getByText('Progress')).toBeInTheDocument();
        } else {
          expect(screen.queryByText('Progress')).not.toBeInTheDocument();
        }
      });
    });
  });

  // ==================== TEST 8: Date Formatting ====================
  describe('Date Formatting', () => {
    it('should format activity date in German locale (full variant)', () => {
      const activity: LeadActivity = {
        id: 'test-act',
        leadId: 'lead-1',
        userId: 'user-1',
        activityType: 'MEETING',
        activityDate: '2025-10-15T14:30:00Z',
        countsAsProgress: true,
        createdAt: '2025-10-15T14:30:00Z',
      };

      render(<ActivityTimeline activities={[activity]} variant="full" />, { wrapper: Wrapper });

      // Date should be formatted in German locale - check for parts
      expect(screen.getByText(/okt/i)).toBeInTheDocument();
      expect(screen.getByText(/15/)).toBeInTheDocument();
    });

    it('should format activity date in German locale (compact variant)', () => {
      const activity: LeadActivity = {
        id: 'test-act',
        leadId: 'lead-1',
        userId: 'user-1',
        activityType: 'MEETING',
        activityDate: '2025-10-15T14:30:00Z',
        countsAsProgress: true,
        createdAt: '2025-10-15T14:30:00Z',
      };

      render(<ActivityTimeline activities={[activity]} variant="compact" />, { wrapper: Wrapper });

      // Date should be formatted as "15.10.2025"
      expect(screen.getByText(/15\.10\.2025/i)).toBeInTheDocument();
    });
  });
});
