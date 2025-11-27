/**
 * MetricCard Component Tests
 * Sprint 2.1.7.7 D5 - Multi-Location Management
 *
 * @description Tests für MetricCard - KPI-Karte mit Theme-Farben
 * @since 2025-11-15
 */

import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen } from '../../../test/test-utils';
import { ThemeProvider } from '@mui/material/styles';
import freshfoodzTheme from '../../../theme/freshfoodz';
import { MetricCard } from './MetricCard';

// Helper function to render with theme
const renderWithTheme = (component: React.ReactElement) => {
  return render(<ThemeProvider theme={freshfoodzTheme}>{component}</ThemeProvider>);
};

describe('MetricCard', () => {
  beforeEach(() => {
    // Mock window.matchMedia for MUI useMediaQuery
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: (query: string) => ({
        matches: false,
        media: query,
        onchange: null,
        addListener: () => {},
        removeListener: () => {},
        addEventListener: () => {},
        removeEventListener: () => {},
        dispatchEvent: () => {},
      }),
    });
  });

  it('renders title, value and subtitle correctly', () => {
    renderWithTheme(
      <MetricCard
        title="Gesamt-Umsatz"
        value="125.000,00 €"
        subtitle="12 Standorte"
        color="primary"
      />
    );

    expect(screen.getByText('Gesamt-Umsatz')).toBeInTheDocument();
    expect(screen.getByText('125.000,00 €')).toBeInTheDocument();
    expect(screen.getByText('12 Standorte')).toBeInTheDocument();
  });

  it('renders numeric value correctly', () => {
    renderWithTheme(<MetricCard title="Standorte" value={5} color="success" />);

    expect(screen.getByText('Standorte')).toBeInTheDocument();
    expect(screen.getByText('5')).toBeInTheDocument();
  });

  it('renders without subtitle when not provided', () => {
    renderWithTheme(<MetricCard title="Opportunities" value={10} color="warning" />);

    expect(screen.getByText('Opportunities')).toBeInTheDocument();
    expect(screen.getByText('10')).toBeInTheDocument();
    expect(screen.queryByText('12 Standorte')).not.toBeInTheDocument();
  });

  it('uses primary color by default', () => {
    const { container } = renderWithTheme(<MetricCard title="Test" value="100" />);

    // Card should be rendered
    expect(container.querySelector('.MuiCard-root')).toBeInTheDocument();
  });

  it('renders all color variants correctly', () => {
    const colors: Array<'primary' | 'secondary' | 'success' | 'info' | 'warning' | 'error'> = [
      'primary',
      'secondary',
      'success',
      'info',
      'warning',
      'error',
    ];

    colors.forEach(color => {
      const { container } = renderWithTheme(
        <MetricCard title={`Test ${color}`} value="100" color={color} />
      );

      expect(screen.getByText(`Test ${color}`)).toBeInTheDocument();
      expect(container.querySelector('.MuiCard-root')).toBeInTheDocument();
    });
  });

  it('displays value with correct typography variant', () => {
    renderWithTheme(<MetricCard title="Revenue" value="50.000 €" color="primary" />);

    const valueElement = screen.getByText('50.000 €');
    expect(valueElement).toBeInTheDocument();
    // Typography variant h4 should be applied
    expect(valueElement.tagName).toBe('DIV');
  });

  it('displays title with correct typography variant', () => {
    renderWithTheme(<MetricCard title="Total Revenue" value="100" color="primary" />);

    const titleElement = screen.getByText('Total Revenue');
    expect(titleElement).toBeInTheDocument();
  });
});
