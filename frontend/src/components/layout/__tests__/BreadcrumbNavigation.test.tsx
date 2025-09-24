import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { BreadcrumbNavigation } from '../BreadcrumbNavigation';
import { useLocation, useNavigate } from 'react-router-dom';

// Mock react-router-dom
vi.mock('react-router-dom', () => ({
  useLocation: vi.fn(),
  useNavigate: vi.fn(),
}));

// Mock navigation config
vi.mock('@/config/navigation.config', () => ({
  navigationConfig: [
    {
      label: 'Dashboard',
      path: '/dashboard',
      subItems: [{ label: 'Reports', path: '/dashboard/reports' }],
    },
    {
      label: 'Customers',
      path: '/customers',
    },
  ],
}));

describe('BreadcrumbNavigation', () => {
  const mockNavigate = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    (useNavigate as jest.Mock).mockReturnValue(mockNavigate);
  });

  it('should render home/cockpit as first breadcrumb', () => {
    (useLocation as jest.Mock).mockReturnValue({ pathname: '/dashboard' });

    render(<BreadcrumbNavigation />);

    expect(screen.getByText('Cockpit')).toBeInTheDocument();
  });

  it('should build breadcrumb trail from path', () => {
    (useLocation as jest.Mock).mockReturnValue({ pathname: '/dashboard/reports' });

    render(<BreadcrumbNavigation />);

    expect(screen.getByText('Cockpit')).toBeInTheDocument();
    expect(screen.getByText('Dashboard')).toBeInTheDocument();
    expect(screen.getByText('Reports')).toBeInTheDocument();
  });

  it('should navigate when clicking breadcrumb link', () => {
    (useLocation as jest.Mock).mockReturnValue({ pathname: '/dashboard/reports' });

    render(<BreadcrumbNavigation />);

    const dashboardLink = screen.getByRole('button', { name: /Dashboard/i });
    fireEvent.click(dashboardLink);

    expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
  });

  it('should not make last breadcrumb clickable', () => {
    (useLocation as jest.Mock).mockReturnValue({ pathname: '/dashboard/reports' });

    render(<BreadcrumbNavigation />);

    // The last item should be Typography, not a Link
    const reportsBreadcrumb = screen.getByText('Reports');
    expect(reportsBreadcrumb.tagName).not.toBe('BUTTON');
  });

  it('should handle paths not in navigation config', () => {
    (useLocation as jest.Mock).mockReturnValue({ pathname: '/unknown/path' });

    render(<BreadcrumbNavigation />);

    expect(screen.getByText('Cockpit')).toBeInTheDocument();
    expect(screen.getByText('Unknown')).toBeInTheDocument();
    expect(screen.getByText('Path')).toBeInTheDocument();
  });

  it('should capitalize and format unknown segments', () => {
    (useLocation as jest.Mock).mockReturnValue({ pathname: '/customer-details/123' });

    render(<BreadcrumbNavigation />);

    expect(screen.getByText('Customer Details')).toBeInTheDocument();
    expect(screen.getByText('123')).toBeInTheDocument();
  });
});
