// Validation Test: Check actual MUI CSS classes rendered by components
import { describe, it } from 'vitest';
import { render } from '@testing-library/react';
import { ThemeProvider } from '@mui/material';
import freshfoodzTheme from '../../../theme/freshfoodz';
import LeadProtectionBadge from '../LeadProtectionBadge';
import ActivityTimeline from '../ActivityTimeline';
import type { LeadProtectionInfo, LeadActivity } from '../types';

const Wrapper = ({ children }: { children: React.ReactNode }) => (
  <ThemeProvider theme={freshfoodzTheme}>{children}</ThemeProvider>
);

describe('MUI CSS Classes Validation', () => {
  describe('LeadProtectionBadge - Actual CSS Classes', () => {
    it('should log actual CSS classes for protected status', () => {
      const protectionInfo: LeadProtectionInfo = {
        status: 'protected',
        daysUntilExpiry: 30,
      };

      const { container } = render(<LeadProtectionBadge protectionInfo={protectionInfo} />, { wrapper: Wrapper });

      const chip = container.querySelector('.MuiChip-root');
      console.log('=== PROTECTED BADGE CSS CLASSES ===');
      console.log('All classes:', chip?.className);
      console.log('Has MuiChip-colorSuccess:', chip?.classList.contains('MuiChip-colorSuccess'));
      console.log('Has MuiChip-filled:', chip?.classList.contains('MuiChip-filled'));

      const icon = container.querySelector('.MuiChip-icon');
      console.log('Icon element:', icon);
      console.log('Icon classes:', icon?.className);

      const svg = icon?.querySelector('svg');
      console.log('SVG element:', svg);
      console.log('SVG classes:', svg?.getAttribute('class'));
      console.log('SVG data-testid:', svg?.getAttribute('data-testid'));
    });

    it('should log actual CSS classes for warning status', () => {
      const protectionInfo: LeadProtectionInfo = {
        status: 'warning',
        daysUntilExpiry: 5,
      };

      const { container } = render(<LeadProtectionBadge protectionInfo={protectionInfo} />, { wrapper: Wrapper });

      const chip = container.querySelector('.MuiChip-root');
      console.log('=== WARNING BADGE CSS CLASSES ===');
      console.log('All classes:', chip?.className);
      console.log('Has MuiChip-colorWarning:', chip?.classList.contains('MuiChip-colorWarning'));
    });

    it('should log actual CSS classes for expired status', () => {
      const protectionInfo: LeadProtectionInfo = {
        status: 'expired',
        daysUntilExpiry: -10,
      };

      const { container } = render(<LeadProtectionBadge protectionInfo={protectionInfo} />, { wrapper: Wrapper });

      const chip = container.querySelector('.MuiChip-root');
      console.log('=== EXPIRED BADGE CSS CLASSES ===');
      console.log('All classes:', chip?.className);
      console.log('Has MuiChip-colorError:', chip?.classList.contains('MuiChip-colorError'));
    });
  });

  describe('ActivityTimeline - Actual CSS Classes', () => {
    it('should log actual CSS classes for progress activity', () => {
      const activity: LeadActivity = {
        id: 'act-1',
        leadId: 'lead-1',
        userId: 'user-1',
        activityType: 'QUALIFIED_CALL',
        activityDate: '2025-10-01T10:00:00Z',
        countsAsProgress: true,
        summary: 'Test activity',
        createdAt: '2025-10-01T10:00:00Z',
      };

      const { container } = render(<ActivityTimeline activities={[activity]} />, { wrapper: Wrapper });

      console.log('=== PROGRESS ACTIVITY CSS CLASSES ===');

      const timelineDot = container.querySelector('.MuiTimelineDot-root');
      console.log('TimelineDot classes:', timelineDot?.className);
      console.log('Has MuiTimelineDot-colorSuccess:', timelineDot?.classList.contains('MuiTimelineDot-colorSuccess'));
      console.log('Has MuiTimelineDot-outlinedSuccess:', timelineDot?.classList.contains('MuiTimelineDot-outlinedSuccess'));
      console.log('Has MuiTimelineDot-filledSuccess:', timelineDot?.classList.contains('MuiTimelineDot-filledSuccess'));

      // Check all possible MUI color classes
      const allDots = container.querySelectorAll('[class*="TimelineDot"]');
      console.log('All timeline dots:', allDots.length);
      allDots.forEach((dot, idx) => {
        console.log(`Dot ${idx} classes:`, dot.className);
      });
    });

    it('should log actual CSS classes for non-progress activity', () => {
      const activity: LeadActivity = {
        id: 'act-2',
        leadId: 'lead-1',
        userId: 'user-1',
        activityType: 'NOTE',
        activityDate: '2025-10-01T10:00:00Z',
        countsAsProgress: false,
        summary: 'Test note',
        createdAt: '2025-10-01T10:00:00Z',
      };

      const { container } = render(<ActivityTimeline activities={[activity]} />, { wrapper: Wrapper });

      console.log('=== NON-PROGRESS ACTIVITY CSS CLASSES ===');

      const timelineDot = container.querySelector('.MuiTimelineDot-root');
      console.log('TimelineDot classes:', timelineDot?.className);
      console.log('Has MuiTimelineDot-colorGrey:', timelineDot?.classList.contains('MuiTimelineDot-colorGrey'));
      console.log('Has MuiTimelineDot-grey:', timelineDot?.classList.contains('MuiTimelineDot-grey'));
    });

    it('should log Timeline layout classes', () => {
      const activity: LeadActivity = {
        id: 'act-1',
        leadId: 'lead-1',
        userId: 'user-1',
        activityType: 'MEETING',
        activityDate: '2025-10-01T10:00:00Z',
        countsAsProgress: true,
        createdAt: '2025-10-01T10:00:00Z',
      };

      const { container: containerFull } = render(
        <ActivityTimeline activities={[activity]} variant="full" />,
        { wrapper: Wrapper }
      );

      console.log('=== TIMELINE LAYOUT CSS CLASSES ===');

      const timelineFull = containerFull.querySelector('.MuiTimeline-root');
      console.log('Full variant Timeline classes:', timelineFull?.className);
      console.log('Has MuiTimeline-positionAlternate:', timelineFull?.classList.contains('MuiTimeline-positionAlternate'));
      console.log('Has MuiTimeline-positionAlternating:', timelineFull?.classList.contains('MuiTimeline-positionAlternating'));

      const { container: containerCompact } = render(
        <ActivityTimeline activities={[activity]} variant="compact" />,
        { wrapper: Wrapper }
      );

      const timelineCompact = containerCompact.querySelector('.MuiTimeline-root');
      console.log('Compact variant Timeline classes:', timelineCompact?.className);
      console.log('Has MuiTimeline-positionRight:', timelineCompact?.classList.contains('MuiTimeline-positionRight'));
    });
  });
});
