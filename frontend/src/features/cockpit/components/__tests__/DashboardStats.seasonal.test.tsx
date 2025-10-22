/**
 * DashboardStats - Seasonal Business Tests
 *
 * Sprint 2.1.7.4 - Section 7.5
 * Tests for Seasonal Paused MetricCard
 */

import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { DashboardStats } from '../DashboardStats';
import type { DashboardStatistics } from '../../types/salesCockpit';

describe('DashboardStats - Seasonal Widget', () => {
  /**
   * Test 1: Seasonal Paused MetricCard renders with correct data
   */
  it('should render Seasonal Paused metric card', () => {
    // GIVEN: Statistics with seasonal data
    const statistics: DashboardStatistics = {
      totalCustomers: 100,
      activeCustomers: 80,
      customersAtRisk: 5,
      openTasks: 10,
      overdueItems: 2,
      prospects: 15,
      conversionRate: 75.5,
      seasonalActive: 8,
      seasonalPaused: 3, // � NEW: Out-of-season seasonal businesses
    };

    // WHEN: Rendering DashboardStats
    render(<DashboardStats statistics={statistics} loading={false} error={null} />);

    // THEN: Seasonal Paused card should be visible
    expect(screen.getByText('Saisonal Pausiert')).toBeInTheDocument();
    expect(screen.getByText('3')).toBeInTheDocument(); // Value
    expect(screen.getByText(/Saison \(normal\)/)).toBeInTheDocument(); // Subtitle
  });

  /**
   * Test 2: Seasonal Paused shows zero when no seasonal businesses are paused
   */
  it('should show zero when no seasonal businesses are paused', () => {
    // GIVEN: Statistics with zero seasonal paused
    const statistics: DashboardStatistics = {
      totalCustomers: 100,
      activeCustomers: 80,
      customersAtRisk: 5,
      openTasks: 10,
      overdueItems: 2,
      prospects: 15,
      conversionRate: 75.5,
      seasonalActive: 10,
      seasonalPaused: 0, // � No paused seasonal businesses
    };

    // WHEN: Rendering DashboardStats
    render(<DashboardStats statistics={statistics} loading={false} error={null} />);

    // THEN: Seasonal Paused card should show 0
    expect(screen.getByText('Saisonal Pausiert')).toBeInTheDocument();
    expect(screen.getByText('0')).toBeInTheDocument();
  });

  /**
   * Test 3: Seasonal Paused card has correct styling (blue color)
   */
  it('should have blue color styling for seasonal paused card', () => {
    // GIVEN: Statistics with seasonal data
    const statistics: DashboardStatistics = {
      totalCustomers: 100,
      activeCustomers: 80,
      customersAtRisk: 5,
      openTasks: 10,
      overdueItems: 2,
      prospects: 15,
      conversionRate: 75.5,
      seasonalActive: 8,
      seasonalPaused: 3,
    };

    // WHEN: Rendering DashboardStats
    const { container } = render(
      <DashboardStats statistics={statistics} loading={false} error={null} />
    );

    // THEN: Card with "Saisonal Pausiert" should exist
    const allBlueCards = container.querySelectorAll('.stat-blue');
    expect(allBlueCards.length).toBeGreaterThan(0);

    // Find the card with "Saisonal Pausiert" text
    const seasonalCardExists = Array.from(allBlueCards).some(card =>
      card.textContent?.includes('Saisonal Pausiert')
    );
    expect(seasonalCardExists).toBe(true);
  });
});
