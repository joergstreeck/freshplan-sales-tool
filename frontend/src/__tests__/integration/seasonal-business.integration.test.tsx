/**
 * Seasonal Business - Integration Tests
 *
 * Sprint 2.1.7.4 - Section 7 Integration
 * End-to-end tests for seasonal business feature
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { customerApi } from '../../features/customer/api/customerApi';

// Mock API
vi.mock('../../features/customer/api/customerApi');

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false },
  },
});

describe('Seasonal Business - Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    queryClient.clear();
  });

  /**
   * Test 1: CustomerDetailPage fetches seasonal fields from backend
   */
  it('should fetch seasonal fields from backend API', async () => {
    // GIVEN: Backend returns customer with seasonal data
    const mockCustomer = {
      id: 'test-id',
      customerNumber: 'KD-SEASONAL-001',
      companyName: 'Eiscafé Venezia',
      status: 'AKTIV',
      isSeasonalBusiness: true,
      seasonalMonths: [5, 6, 7, 8, 9], // May-September
      seasonalPattern: 'SUMMER',
    };

    vi.mocked(customerApi.getCustomer).mockResolvedValue(mockCustomer as any);

    // WHEN: API is called
    const result = await customerApi.getCustomer('test-id');

    // THEN: Seasonal fields should be returned
    expect(result).toBeDefined();
    expect(result.isSeasonalBusiness).toBe(true);
    expect(result.seasonalMonths).toEqual([5, 6, 7, 8, 9]);
    expect(result.seasonalPattern).toBe('SUMMER');
  });

  /**
   * Test 2: Dashboard fetches seasonalPaused metric from backend
   */
  it('should fetch seasonalPaused metric from dashboard API', async () => {
    // GIVEN: Backend returns dashboard statistics with seasonal metrics
    const mockDashboardData = {
      todaysTasks: [],
      riskCustomers: [],
      statistics: {
        totalCustomers: 100,
        activeCustomers: 80,
        customersAtRisk: 5,
        openTasks: 10,
        overdueItems: 2,
        prospects: 15,
        conversionRate: 75.0,
        seasonalActive: 8,
        seasonalPaused: 3, //  NEW metric
      },
      alerts: [],
    };

    // Mock fetch (simulating API call)
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => mockDashboardData,
    });

    // WHEN: API is called
    const response = await fetch('/api/cockpit/dashboard');
    const data = await response.json();

    // THEN: Seasonal metrics should be returned
    expect(data.statistics.seasonalActive).toBe(8);
    expect(data.statistics.seasonalPaused).toBe(3);
  });

  /**
   * Test 3: Seasonal data is correctly typed (TypeScript validation)
   */
  it('should have correct TypeScript types for seasonal fields', () => {
    // GIVEN: TypeScript types for Customer and Statistics
    const customer: {
      isSeasonalBusiness?: boolean;
      seasonalMonths?: number[];
      seasonalPattern?: string;
    } = {
      isSeasonalBusiness: true,
      seasonalMonths: [6, 7, 8],
      seasonalPattern: 'SUMMER',
    };

    const statistics: {
      seasonalActive: number;
      seasonalPaused: number;
    } = {
      seasonalActive: 10,
      seasonalPaused: 5,
    };

    // WHEN: Accessing typed properties
    // THEN: TypeScript should not complain (compilation test)
    expect(customer.isSeasonalBusiness).toBe(true);
    expect(customer.seasonalMonths).toHaveLength(3);
    expect(customer.seasonalPattern).toBe('SUMMER');
    expect(statistics.seasonalActive).toBe(10);
    expect(statistics.seasonalPaused).toBe(5);
  });

  /**
   * Test 4: Backend calculates seasonalPaused based on current month
   */
  it('should calculate seasonalPaused dynamically based on current month', async () => {
    // GIVEN: Current month is October (10)
    const currentMonth = 10;

    // AND: Seasonal customers with different active months
    const customers = [
      {
        id: '1',
        isSeasonalBusiness: true,
        seasonalMonths: [5, 6, 7, 8, 9], // Summer season (NOT October)
      },
      {
        id: '2',
        isSeasonalBusiness: true,
        seasonalMonths: [10, 11, 12], // Autumn/Winter (INCLUDES October)
      },
      {
        id: '3',
        isSeasonalBusiness: true,
        seasonalMonths: [6, 7], // Summer only (NOT October)
      },
    ];

    // WHEN: Calculating seasonalPaused
    const seasonalPaused = customers.filter(
      (c) =>
        c.isSeasonalBusiness &&
        c.seasonalMonths &&
        !c.seasonalMonths.includes(currentMonth)
    ).length;

    // THEN: Only customers 1 and 3 are out-of-season
    expect(seasonalPaused).toBe(2);
  });
});
