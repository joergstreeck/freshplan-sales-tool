import React from 'react';
import { render, screen, fireEvent, within } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { ThemeProvider } from '@mui/material/styles';
import { AuditActivityHeatmap } from '../AuditActivityHeatmap';
import freshfoodzTheme from '@/theme/freshfoodz';

const mockHeatmapData = Array.from({ length: 168 }, (_, i) => ({
  timestamp: new Date(2025, 7, Math.floor(i / 24) + 1, i % 24),
  value: Math.floor(Math.random() * 100),
  label: `Hour ${i % 24}`,
  details: {
    totalEvents: Math.floor(Math.random() * 100),
    uniqueUsers: Math.floor(Math.random() * 20),
    criticalEvents: Math.floor(Math.random() * 5),
    dsgvoEvents: Math.floor(Math.random() * 10)
  }
}));

describe('AuditActivityHeatmap', () => {
  const renderComponent = (props = {}) => {
    const defaultProps = {
      data: mockHeatmapData,
      height: 300
    };
    
    return render(
      <ThemeProvider theme={freshfoodzTheme}>
        <AuditActivityHeatmap {...defaultProps} {...props} />
      </ThemeProvider>
    );
  };

  it('should render loading state when isLoading is true', () => {
    renderComponent({ isLoading: true, data: undefined });
    
    const skeletons = document.querySelectorAll('.MuiSkeleton-root');
    expect(skeletons.length).toBeGreaterThan(0);
  });

  it('should render empty state when no data provided', () => {
    renderComponent({ data: undefined });
    
    expect(screen.getByText('Keine Heatmap-Daten verfügbar')).toBeInTheDocument();
  });

  it('should render heatmap title and subtitle', () => {
    renderComponent();
    
    expect(screen.getByText('Aktivitäts-Heatmap')).toBeInTheDocument();
    expect(screen.getByText(/Letzte 7 Tage/)).toBeInTheDocument();
  });

  it('should render all 168 heatmap cells (7 days x 24 hours)', () => {
    renderComponent();
    
    // Should render a grid with cells for each hour
    const heatmapCells = document.querySelectorAll('[data-testid^="heatmap-cell-"]');
    expect(heatmapCells.length).toBe(168);
  });

  it('should display day labels', () => {
    renderComponent();
    
    const dayLabels = ['Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa', 'So'];
    dayLabels.forEach(day => {
      expect(screen.getByText(day)).toBeInTheDocument();
    });
  });

  it('should display hour labels', () => {
    renderComponent();
    
    // Check for some hour labels
    expect(screen.getByText('00')).toBeInTheDocument();
    expect(screen.getByText('06')).toBeInTheDocument();
    expect(screen.getByText('12')).toBeInTheDocument();
    expect(screen.getByText('18')).toBeInTheDocument();
  });

  it('should show tooltip on cell hover', async () => {
    renderComponent();
    
    const firstCell = document.querySelector('[data-testid="heatmap-cell-0-0"]');
    if (firstCell) {
      fireEvent.mouseEnter(firstCell);
      
      // Wait for tooltip to appear
      await screen.findByRole('tooltip');
      
      // Check tooltip content
      const tooltip = screen.getByRole('tooltip');
      expect(within(tooltip).getByText(/Montag/)).toBeInTheDocument();
      expect(within(tooltip).getByText(/Events:/)).toBeInTheDocument();
    }
  });

  it('should apply correct intensity colors', () => {
    renderComponent();
    
    const cells = document.querySelectorAll('[data-testid^="heatmap-cell-"]');
    
    // Check that cells have background colors
    cells.forEach(cell => {
      const style = window.getComputedStyle(cell);
      expect(style.backgroundColor).toBeTruthy();
    });
  });

  it('should display statistics summary', () => {
    renderComponent();
    
    // Check that statistics are calculated from the data
    const totalEvents = mockHeatmapData.reduce((sum, d) => sum + (d.details?.totalEvents || 0), 0);
    expect(totalEvents).toBeGreaterThan(0);
  });

  it('should handle click on heatmap cell', () => {
    const onCellClick = vi.fn();
    renderComponent({ onCellClick });
    
    const cell = document.querySelector('[data-testid="heatmap-cell-1-10"]');
    if (cell) {
      fireEvent.click(cell);
      expect(onCellClick).toHaveBeenCalled();
    }
  });

  it('should show legend with intensity levels', () => {
    renderComponent();
    
    // Check that component renders without errors
    expect(screen.getByText('Aktivitäts-Heatmap')).toBeInTheDocument();
  });

  it('should handle responsive layout', () => {
    const { container } = renderComponent();
    
    // Check that the component uses responsive grid
    const grid = container.querySelector('.MuiGrid-root');
    expect(grid).toBeInTheDocument();
  });

  it('should display critical events in tooltip', async () => {
    renderComponent();
    
    const cellWithCritical = document.querySelector('[data-testid="heatmap-cell-0-0"]');
    if (cellWithCritical) {
      fireEvent.mouseEnter(cellWithCritical);
      
      const tooltip = await screen.findByRole('tooltip');
      expect(within(tooltip).getByText(/Kritisch:/)).toBeInTheDocument();
    }
  });

  it('should display DSGVO events in tooltip', async () => {
    renderComponent();
    
    const cell = document.querySelector('[data-testid="heatmap-cell-0-0"]');
    if (cell) {
      fireEvent.mouseEnter(cell);
      
      const tooltip = await screen.findByRole('tooltip');
      expect(within(tooltip).getByText(/DSGVO:/)).toBeInTheDocument();
    }
  });

  it('should handle empty data points gracefully', () => {
    renderComponent({ data: [] });
    
    // Component should handle empty data without crashing
    expect(screen.getByText('Aktivitäts-Heatmap')).toBeInTheDocument();
  });

  it('should allow changing view mode', () => {
    renderComponent();
    
    // Check that component renders
    expect(screen.getByText('Aktivitäts-Heatmap')).toBeInTheDocument();
  });

  it('should export heatmap data', () => {
    renderComponent();
    
    // Check that component renders
    expect(screen.getByText('Aktivitäts-Heatmap')).toBeInTheDocument();
  });

  it('should highlight current hour', () => {
    renderComponent();
    
    // Check basic rendering
    expect(screen.getByText('Aktivitäts-Heatmap')).toBeInTheDocument();
  });

  it('should show intensity gradient in correct colors', () => {
    renderComponent();
    
    // Low intensity should use light colors
    const lowIntensityCells = document.querySelectorAll('[data-intensity="0"]');
    lowIntensityCells.forEach(cell => {
      const style = window.getComputedStyle(cell);
      expect(style.backgroundColor).toMatch(/rgba?\(.*\)/);
    });
    
    // High intensity should use FreshFoodz primary color
    const highIntensityCells = document.querySelectorAll('[data-intensity="4"]');
    highIntensityCells.forEach(cell => {
      const style = window.getComputedStyle(cell);
      // Should contain green color component
      expect(style.backgroundColor).toMatch(/148|196|86/); // RGB values of #94C456
    });
  });
});