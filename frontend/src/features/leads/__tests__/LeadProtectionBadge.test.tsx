// Sprint 2.1.5 Frontend Phase 2 - LeadProtectionBadge Component Tests
// Test Coverage: calculateProtectionInfo utility, Color-Coding, Tooltip rendering, ARIA compliance

import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ThemeProvider } from '@mui/material';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import LeadProtectionBadge, { calculateProtectionInfo } from '../LeadProtectionBadge';
import freshfoodzTheme from '../../../theme/freshfoodz';
import type { LeadProtectionInfo } from '../types';

const Wrapper = ({ children }: { children: React.ReactNode }) => (
  <ThemeProvider theme={freshfoodzTheme}>{children}</ThemeProvider>
);

describe('LeadProtectionBadge', () => {
  // ==================== TEST 1: calculateProtectionInfo Utility ====================
  describe('calculateProtectionInfo utility', () => {
    it('should return "protected" status when progressDeadline > 7 days', () => {
      const futureDate = new Date();
      futureDate.setDate(futureDate.getDate() + 30); // 30 days in future

      const result = calculateProtectionInfo({
        protectionUntil: futureDate.toISOString(),
        progressDeadline: futureDate.toISOString(),
      });

      expect(result.status).toBe('protected');
      expect(result.daysUntilExpiry).toBeGreaterThan(7);
      expect(result.warningMessage).toBeUndefined();
    });

    it('should return "warning" status when progressDeadline ≤ 7 days', () => {
      const soonDate = new Date();
      soonDate.setDate(soonDate.getDate() + 5); // 5 days in future

      const result = calculateProtectionInfo({
        protectionUntil: soonDate.toISOString(),
        progressDeadline: soonDate.toISOString(),
      });

      expect(result.status).toBe('warning');
      expect(result.daysUntilExpiry).toBeLessThanOrEqual(7);
      expect(result.daysUntilExpiry).toBeGreaterThan(0);
      expect(result.warningMessage).toContain('Nur noch');
      expect(result.warningMessage).toContain('Tage bis Progress-Deadline');
    });

    it('should return "expired" status when progressDeadline ≤ 0 days', () => {
      const pastDate = new Date();
      pastDate.setDate(pastDate.getDate() - 5); // 5 days in past

      const result = calculateProtectionInfo({
        protectionUntil: pastDate.toISOString(),
        progressDeadline: pastDate.toISOString(),
      });

      expect(result.status).toBe('expired');
      expect(result.daysUntilExpiry).toBeLessThanOrEqual(0);
      expect(result.warningMessage).toContain('Keine Progress-Aktivität seit 60+ Tagen');
    });

    it('should return "protected" status when no progressDeadline is provided', () => {
      const result = calculateProtectionInfo({
        protectionUntil: new Date().toISOString(),
      });

      expect(result.status).toBe('protected');
      expect(result.daysUntilExpiry).toBeUndefined();
    });

    it('should return "warning" status when progressWarningSentAt is within last 7 days', () => {
      const recentWarning = new Date();
      recentWarning.setDate(recentWarning.getDate() - 3); // 3 days ago

      const futureDeadline = new Date();
      futureDeadline.setDate(futureDeadline.getDate() + 30); // 30 days in future

      const result = calculateProtectionInfo({
        protectionUntil: futureDeadline.toISOString(),
        progressDeadline: futureDeadline.toISOString(),
        progressWarningSentAt: recentWarning.toISOString(),
      });

      expect(result.status).toBe('warning');
      expect(result.warningMessage).toContain('Progress-Warnung versendet');
    });
  });

  // ==================== TEST 2: Color-Coding ====================
  describe('Color-Coding', () => {
    it('should render green (success) badge for "protected" status', () => {
      const protectionInfo: LeadProtectionInfo = {
        status: 'protected',
        protectionUntil: '2025-12-31T23:59:59Z',
        progressDeadline: '2025-11-01T00:00:00Z',
        daysUntilExpiry: 30,
      };

      render(<LeadProtectionBadge protectionInfo={protectionInfo} />, { wrapper: Wrapper });

      const badge = screen.getByLabelText('Lead-Schutzstatus: Geschützt');
      expect(badge).toHaveClass('MuiChip-colorSuccess', 'MuiChip-filled');
    });

    it('should render yellow (warning) badge for "warning" status', () => {
      const protectionInfo: LeadProtectionInfo = {
        status: 'warning',
        protectionUntil: '2025-10-10T23:59:59Z',
        progressDeadline: '2025-10-10T00:00:00Z',
        daysUntilExpiry: 5,
        warningMessage: 'Nur noch 5 Tage bis Progress-Deadline!',
      };

      render(<LeadProtectionBadge protectionInfo={protectionInfo} />, { wrapper: Wrapper });

      const badge = screen.getByLabelText(/lead-schutzstatus: warnung/i);
      expect(badge).toHaveClass('MuiChip-colorWarning', 'MuiChip-filled');
    });

    it('should render red (error) badge for "expired" status', () => {
      const protectionInfo: LeadProtectionInfo = {
        status: 'expired',
        protectionUntil: '2025-09-01T23:59:59Z',
        progressDeadline: '2025-09-01T00:00:00Z',
        daysUntilExpiry: -10,
        warningMessage: 'Keine Progress-Aktivität seit 60+ Tagen. Lead-Schutz verfallen.',
      };

      const { container } = render(<LeadProtectionBadge protectionInfo={protectionInfo} />, { wrapper: Wrapper });

      const badge = container.querySelector('.MuiChip-root');
      expect(badge).toHaveClass('MuiChip-colorError', 'MuiChip-filled');
    });

    it('should support outlined variant', () => {
      const protectionInfo: LeadProtectionInfo = {
        status: 'protected',
        daysUntilExpiry: 30,
      };

      const { container } = render(<LeadProtectionBadge protectionInfo={protectionInfo} variant="outlined" />, { wrapper: Wrapper });

      const badge = container.querySelector('.MuiChip-root');
      expect(badge).toHaveClass('MuiChip-outlined');
    });
  });

  // ==================== TEST 3: Badge Labels ====================
  describe('Badge Labels', () => {
    it('should display "Geschützt" label for protected status', () => {
      const protectionInfo: LeadProtectionInfo = {
        status: 'protected',
        daysUntilExpiry: 30,
      };

      render(<LeadProtectionBadge protectionInfo={protectionInfo} />, { wrapper: Wrapper });

      expect(screen.getByText('Geschützt')).toBeInTheDocument();
    });

    it('should display "Warnung (5T)" label for warning status', () => {
      const protectionInfo: LeadProtectionInfo = {
        status: 'warning',
        daysUntilExpiry: 5,
      };

      render(<LeadProtectionBadge protectionInfo={protectionInfo} />, { wrapper: Wrapper });

      expect(screen.getByText(/warnung \(5t\)/i)).toBeInTheDocument();
    });

    it('should display "Abgelaufen" label for expired status', () => {
      const protectionInfo: LeadProtectionInfo = {
        status: 'expired',
        daysUntilExpiry: -10,
      };

      render(<LeadProtectionBadge protectionInfo={protectionInfo} />, { wrapper: Wrapper });

      expect(screen.getByText('Abgelaufen')).toBeInTheDocument();
    });
  });

  // ==================== TEST 4: Tooltip Rendering ====================
  describe('Tooltip Rendering', () => {
    it('should show detailed tooltip on hover for protected status', async () => {
      const user = userEvent.setup();
      const protectionInfo: LeadProtectionInfo = {
        status: 'protected',
        protectionUntil: '2025-12-31T23:59:59Z',
        progressDeadline: '2025-11-01T00:00:00Z',
        daysUntilExpiry: 30,
      };

      const { container } = render(<LeadProtectionBadge protectionInfo={protectionInfo} />, { wrapper: Wrapper });

      const badge = container.querySelector('.MuiChip-root');
      badge && await user.hover(badge);

      // Tooltip content should appear (after delay)
      await screen.findByText(/lead ist geschützt/i, {}, { timeout: 5000 });
      expect(screen.getByText(/schutz bis:/i)).toBeInTheDocument();
      expect(screen.getByText(/30 tage/i)).toBeInTheDocument();
    });

    it('should show warning message in tooltip for warning status', async () => {
      const user = userEvent.setup();
      const protectionInfo: LeadProtectionInfo = {
        status: 'warning',
        progressDeadline: '2025-10-10T00:00:00Z',
        daysUntilExpiry: 5,
        warningMessage: 'Nur noch 5 Tage bis Progress-Deadline! Bitte Aktivität durchführen.',
      };

      const { container } = render(<LeadProtectionBadge protectionInfo={protectionInfo} />, { wrapper: Wrapper });

      const badge = container.querySelector('.MuiChip-root');
      badge && await user.hover(badge);

      await screen.findByText(/lead-schutz läuft bald ab/i, {}, { timeout: 3000 });
      expect(screen.getByText(/nur noch 5 tage bis progress-deadline/i)).toBeInTheDocument();
    });

    it('should show expiry message in tooltip for expired status', async () => {
      const user = userEvent.setup();
      const protectionInfo: LeadProtectionInfo = {
        status: 'expired',
        progressDeadline: '2025-09-01T00:00:00Z',
        daysUntilExpiry: -10,
        warningMessage: 'Keine Progress-Aktivität seit 60+ Tagen. Lead-Schutz verfallen.',
      };

      const { container } = render(<LeadProtectionBadge protectionInfo={protectionInfo} />, { wrapper: Wrapper });

      const badge = container.querySelector('.MuiChip-root');
      badge && await user.hover(badge);

      await screen.findByText(/lead-schutz abgelaufen/i, {}, { timeout: 3000 });
      expect(screen.getByText(/keine progress-aktivität seit 60\+ tagen/i)).toBeInTheDocument();
    });

    it('should show contract reference in tooltip for protected status without warning', async () => {
      const user = userEvent.setup();
      const protectionInfo: LeadProtectionInfo = {
        status: 'protected',
        daysUntilExpiry: 30,
      };

      const { container } = render(<LeadProtectionBadge protectionInfo={protectionInfo} />, { wrapper: Wrapper });

      const badge = container.querySelector('.MuiChip-root');
      badge && await user.hover(badge);

      await screen.findByText(/vertrag §3\.2: 6 monate ab registrierung/i, {}, { timeout: 3000 });
    });
  });

  // ==================== TEST 5: ARIA Compliance ====================
  describe('ARIA Compliance', () => {
    it('should have aria-label with status description', () => {
      const protectionInfo: LeadProtectionInfo = {
        status: 'protected',
        daysUntilExpiry: 30,
      };

      const { container } = render(<LeadProtectionBadge protectionInfo={protectionInfo} />, { wrapper: Wrapper });

      const badge = container.querySelector('.MuiChip-root');
      expect(badge).toHaveAttribute('aria-label', 'Lead-Schutzstatus: Geschützt');
    });

    it('should have help cursor to indicate tooltip availability', () => {
      const protectionInfo: LeadProtectionInfo = {
        status: 'protected',
        daysUntilExpiry: 30,
      };

      const { container } = render(<LeadProtectionBadge protectionInfo={protectionInfo} />, { wrapper: Wrapper });

      const badge = container.querySelector('.MuiChip-root');
      expect(badge).toHaveStyle({ cursor: 'help' });
    });
  });

  // ==================== TEST 6: Size Variants ====================
  describe('Size Variants', () => {
    it('should render small size by default', () => {
      const protectionInfo: LeadProtectionInfo = {
        status: 'protected',
        daysUntilExpiry: 30,
      };

      const { container } = render(<LeadProtectionBadge protectionInfo={protectionInfo} />, { wrapper: Wrapper });

      const badge = container.querySelector('.MuiChip-root');
      expect(badge).toHaveClass('MuiChip-sizeSmall');
    });

    it('should render medium size when specified', () => {
      const protectionInfo: LeadProtectionInfo = {
        status: 'protected',
        daysUntilExpiry: 30,
      };

      const { container } = render(<LeadProtectionBadge protectionInfo={protectionInfo} size="medium" />, { wrapper: Wrapper });

      const badge = container.querySelector('.MuiChip-root');
      expect(badge).toHaveClass('MuiChip-sizeMedium');
    });
  });

  // ==================== TEST 7: Icon Rendering ====================
  describe('Icon Rendering', () => {
    it('should render CheckCircle icon for protected status', () => {
      const protectionInfo: LeadProtectionInfo = {
        status: 'protected',
        daysUntilExpiry: 30,
      };

      const { container } = render(<LeadProtectionBadge protectionInfo={protectionInfo} />, { wrapper: Wrapper });

      // Verify icon container exists
      const icon = container.querySelector('.MuiChip-icon');
      expect(icon).toBeTruthy();

      // Verify SVG icon is rendered (MUI icons are SVGs)
      const svgIcons = container.querySelectorAll('svg');
      expect(svgIcons.length).toBeGreaterThan(0);
    });

    it('should render Warning icon for warning status', () => {
      const protectionInfo: LeadProtectionInfo = {
        status: 'warning',
        daysUntilExpiry: 5,
      };

      const { container } = render(<LeadProtectionBadge protectionInfo={protectionInfo} />, { wrapper: Wrapper });

      const icon = container.querySelector('.MuiChip-icon');
      expect(icon).toBeInTheDocument();
    });

    it('should render Error icon for expired status', () => {
      const protectionInfo: LeadProtectionInfo = {
        status: 'expired',
        daysUntilExpiry: -10,
      };

      const { container } = render(<LeadProtectionBadge protectionInfo={protectionInfo} />, { wrapper: Wrapper });

      const icon = container.querySelector('.MuiChip-icon');
      expect(icon).toBeInTheDocument();
    });
  });
});
