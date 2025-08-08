import React from 'react';
import { render, screen, fireEvent, within } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ThemeProvider } from '@mui/material/styles';
import { AuditDashboard } from '../AuditDashboard';
import freshfoodzTheme from '@/theme/freshfoodz';

// Mock child components
vi.mock('../AuditStatisticsCards', () => ({
  AuditStatisticsCards: () => <div data-testid="audit-statistics-cards">Statistics Cards</div>
}));

vi.mock('../AuditActivityHeatmap', () => ({
  AuditActivityHeatmap: () => <div data-testid="audit-activity-heatmap">Activity Heatmap</div>
}));

vi.mock('../AuditStreamMonitor', () => ({
  AuditStreamMonitor: () => <div data-testid="audit-stream-monitor">Stream Monitor</div>
}));

const mockMetrics = {
  totalEventsToday: 1234,
  activeUsers: 45,
  criticalEventsToday: 2,
  coverage: 94.5,
  retentionCompliance: 92,
  integrityStatus: 'valid' as const,
  topEventTypes: [
    { eventType: 'UPDATE', count: 456 },
    { eventType: 'READ', count: 389 },
    { eventType: 'CREATE', count: 234 },
    { eventType: 'DELETE', count: 89 },
    { eventType: 'EXPORT', count: 66 }
  ],
  eventsByHour: Array.from({ length: 24 }, (_, i) => ({
    hour: i,
    count: Math.floor(Math.random() * 100)
  })),
  usersByDepartment: {
    'IT': 12,
    'Vertrieb': 18,
    'Management': 5,
    'Support': 10
  },
  recentAlerts: [
    {
      id: 'alert-1',
      type: 'security',
      message: 'Ungewöhnliche Aktivität erkannt',
      timestamp: '2025-08-08T14:30:00Z',
      severity: 'warning'
    }
  ]
};

describe('AuditDashboard', () => {
  const renderComponent = (props = {}) => {
    const defaultProps = {
      metrics: mockMetrics,
      dateRange: {
        from: new Date('2025-08-01'),
        to: new Date('2025-08-08')
      }
    };
    
    return render(
      <ThemeProvider theme={freshfoodzTheme}>
        <AuditDashboard {...defaultProps} {...props} />
      </ThemeProvider>
    );
  };

  it('should render dashboard with all main sections', () => {
    renderComponent();
    
    // Statistics cards
    expect(screen.getByText('Ereignisse heute')).toBeInTheDocument();
    expect(screen.getByText('Aktive Benutzer')).toBeInTheDocument();
    expect(screen.getByText('Kritische Ereignisse')).toBeInTheDocument();
    expect(screen.getByText('Audit Coverage')).toBeInTheDocument();
  });

  it('should display correct metric values', () => {
    renderComponent();
    
    expect(screen.getByText('1.234')).toBeInTheDocument(); // Formatted with German locale
    expect(screen.getByText('45')).toBeInTheDocument();
    expect(screen.getByText('2')).toBeInTheDocument();
    expect(screen.getByText('94.5%')).toBeInTheDocument();
  });

  it('should show trend indicators', () => {
    renderComponent();
    
    const trendChips = screen.getAllByText(/\+\d+%/);
    expect(trendChips.length).toBeGreaterThan(0);
  });

  it('should display compliance status section', () => {
    renderComponent();
    
    expect(screen.getByText('Compliance Status')).toBeInTheDocument();
    expect(screen.getByText('DSGVO Compliance')).toBeInTheDocument();
    expect(screen.getByText('92%')).toBeInTheDocument();
    
    expect(screen.getByText('Integrity Status')).toBeInTheDocument();
    expect(screen.getByText('valid')).toBeInTheDocument();
  });

  it('should show compliance status chip with correct color', () => {
    renderComponent();
    
    const validChip = screen.getByText('Gültig');
    expect(validChip).toHaveClass('MuiChip-colorSuccess');
  });

  it('should display top event types', () => {
    renderComponent();
    
    expect(screen.getByText('Top Ereignistypen')).toBeInTheDocument();
    expect(screen.getByText('UPDATE')).toBeInTheDocument();
    expect(screen.getByText('456')).toBeInTheDocument();
    expect(screen.getByText('READ')).toBeInTheDocument();
    expect(screen.getByText('389')).toBeInTheDocument();
  });

  it('should limit top event types to 5', () => {
    renderComponent();
    
    const eventTypes = ['UPDATE', 'READ', 'CREATE', 'DELETE', 'EXPORT'];
    eventTypes.forEach(type => {
      expect(screen.getByText(type)).toBeInTheDocument();
    });
    
    // Should not show more than 5
    const allEventTypeElements = screen.getAllByText(/UPDATE|READ|CREATE|DELETE|EXPORT/);
    expect(allEventTypeElements.length).toBeLessThanOrEqual(5);
  });

  it('should display activity timeline placeholder', () => {
    renderComponent();
    
    expect(screen.getByText('Aktivitätsverlauf (7 Tage)')).toBeInTheDocument();
    expect(screen.getByText(/Activity Chart Placeholder/)).toBeInTheDocument();
  });

  it('should use correct colors for critical events', () => {
    renderComponent();
    
    const criticalCard = screen.getByText('Kritische Ereignisse').closest('.MuiPaper-root');
    const icon = within(criticalCard!).getByTestId('WarningIcon');
    
    // Should use red color when there are critical events
    expect(icon.parentElement).toHaveStyle({ color: '#f44336' });
  });

  it('should use green color when no critical events', () => {
    const metricsNoCritical = {
      ...mockMetrics,
      criticalEventsToday: 0
    };
    
    renderComponent({ metrics: metricsNoCritical });
    
    const criticalCard = screen.getByText('Kritische Ereignisse').closest('.MuiPaper-root');
    const icon = within(criticalCard!).getByTestId('WarningIcon');
    
    // Should use green color when no critical events
    expect(icon.parentElement).toHaveStyle({ color: '#4caf50' });
  });

  it('should show progress bars with correct values', () => {
    renderComponent();
    
    const progressBars = document.querySelectorAll('.MuiLinearProgress-root');
    expect(progressBars.length).toBeGreaterThan(0);
    
    // Check DSGVO compliance progress
    const dsgvoProgress = progressBars[0];
    expect(dsgvoProgress).toHaveAttribute('aria-valuenow', '92');
  });

  it('should color code compliance progress bars', () => {
    renderComponent();
    
    // High compliance (>80%) should be green
    const progressBar = document.querySelector('.MuiLinearProgress-bar');
    expect(progressBar).toHaveStyle({ backgroundColor: expect.stringContaining('94C456') });
  });

  it('should show warning color for low compliance', () => {
    const lowComplianceMetrics = {
      ...mockMetrics,
      retentionCompliance: 65
    };
    
    renderComponent({ metrics: lowComplianceMetrics });
    
    const progressBar = document.querySelector('.MuiLinearProgress-bar');
    expect(progressBar).toHaveStyle({ backgroundColor: '#ff9800' });
  });

  it('should display correct icons for stat cards', () => {
    renderComponent();
    
    expect(screen.getByTestId('AssignmentIcon')).toBeInTheDocument();
    expect(screen.getByTestId('PeopleIcon')).toBeInTheDocument();
    expect(screen.getByTestId('WarningIcon')).toBeInTheDocument();
    expect(screen.getByTestId('SecurityIcon')).toBeInTheDocument();
  });

  it('should format large numbers with locale', () => {
    const largeNumberMetrics = {
      ...mockMetrics,
      totalEventsToday: 1234567
    };
    
    renderComponent({ metrics: largeNumberMetrics });
    
    // Should format with German locale (dots as thousand separators)
    expect(screen.getByText('1.234.567')).toBeInTheDocument();
  });

  it('should show subtitle for active users', () => {
    renderComponent();
    
    const activeUsersCard = screen.getByText('Aktive Benutzer').closest('.MuiPaper-root');
    expect(within(activeUsersCard!).getByText('Heute aktiv')).toBeInTheDocument();
  });

  it('should handle missing metrics gracefully', () => {
    renderComponent({ metrics: undefined });
    
    expect(screen.getByText('Keine Daten verfügbar')).toBeInTheDocument();
  });

  it('should apply FreshFoodz theme colors', () => {
    renderComponent();
    
    // Check primary color usage
    const statCards = document.querySelectorAll('.MuiPaper-root');
    statCards.forEach(card => {
      const icons = card.querySelectorAll('[class*="MuiSvgIcon"]');
      icons.forEach(icon => {
        const parentBox = icon.closest('[class*="MuiBox"]');
        if (parentBox) {
          const style = window.getComputedStyle(parentBox);
          // Should use theme colors
          expect(style.color).toMatch(/#004F7B|#94C456|#f44336|#4caf50|#ff9800/);
        }
      });
    });
  });

  it('should use Antonio font for headers', () => {
    renderComponent();
    
    const headers = screen.getAllByRole('heading', { level: 4 });
    headers.forEach(header => {
      expect(header).toHaveStyle({ fontFamily: expect.stringContaining('Antonio') });
    });
  });

  it('should use Poppins font for body text', () => {
    renderComponent();
    
    const subtitles = document.querySelectorAll('.MuiTypography-subtitle2');
    subtitles.forEach(subtitle => {
      expect(subtitle).toHaveStyle({ fontFamily: expect.stringContaining('Poppins') });
    });
  });

  it('should display event type chips with correct styling', () => {
    renderComponent();
    
    const chips = screen.getAllByText(/\d+/).filter(el => 
      el.classList.contains('MuiChip-label')
    );
    
    chips.forEach(chip => {
      const chipElement = chip.closest('.MuiChip-root');
      expect(chipElement).toHaveStyle({ 
        backgroundColor: expect.stringContaining('004F7B15'),
        color: '#004F7B'
      });
    });
  });

  it('should show border between event type items', () => {
    renderComponent();
    
    const eventTypeSection = screen.getByText('Top Ereignistypen')
      .closest('.MuiPaper-root');
    
    const items = within(eventTypeSection!).getAllByRole('generic')
      .filter(el => el.style.borderBottom);
    
    expect(items.length).toBeGreaterThan(0);
  });

  it('should handle invalid integrity status', () => {
    const invalidIntegrityMetrics = {
      ...mockMetrics,
      integrityStatus: 'invalid' as const
    };
    
    renderComponent({ metrics: invalidIntegrityMetrics });
    
    const invalidChip = screen.getByText('Prüfung erforderlich');
    expect(invalidChip).toHaveClass('MuiChip-colorWarning');
  });

  it('should display all grid items with correct spacing', () => {
    const { container } = renderComponent();
    
    const gridContainer = container.querySelector('.MuiGrid-container');
    expect(gridContainer).toHaveStyle({ gap: expect.stringContaining('24px') });
    
    const gridItems = container.querySelectorAll('.MuiGrid-item');
    expect(gridItems.length).toBeGreaterThan(0);
  });

  it('should use responsive grid layout', () => {
    const { container } = renderComponent();
    
    // Check responsive breakpoints
    const smallGridItems = container.querySelectorAll('[class*="MuiGrid-grid-sm-6"]');
    const mediumGridItems = container.querySelectorAll('[class*="MuiGrid-grid-md-3"]');
    
    expect(smallGridItems.length).toBeGreaterThan(0);
    expect(mediumGridItems.length).toBeGreaterThan(0);
  });

  it('should handle date range changes', () => {
    const onDateRangeChange = vi.fn();
    const dateRange = {
      from: new Date('2025-08-01'),
      to: new Date('2025-08-08')
    };
    
    renderComponent({ dateRange, onDateRangeChange });
    
    // Component renders with provided date range
    expect(screen.getByText('Ereignisse heute')).toBeInTheDocument();
  });

  it('should show zero state for metrics', () => {
    const zeroMetrics = {
      ...mockMetrics,
      totalEventsToday: 0,
      activeUsers: 0,
      criticalEventsToday: 0
    };
    
    renderComponent({ metrics: zeroMetrics });
    
    expect(screen.getByText('0')).toBeInTheDocument();
  });
});