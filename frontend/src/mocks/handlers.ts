import { http, HttpResponse } from 'msw';

// Sprint 2.1.6: Lead API uses real backend - no mocks needed
// MSW is configured with onUnhandledRequest: 'bypass' to allow real API calls

// Mock customer data
const mockCustomers = [
  {
    id: '1',
    customerNumber: 'K-2025-001',
    companyName: 'FreshFood GmbH',
    tradingName: 'FreshFood Berlin',
    customerType: 'EXISTING',
    status: 'ACTIVE',
    industry: 'GASTRONOMY',
    expectedAnnualVolume: 150000,
    lastContactDate: '2025-08-01',
    riskScore: 25,
    atRisk: false,
  },
  {
    id: '2',
    customerNumber: 'K-2025-002',
    companyName: 'Restaurant Zur goldenen Gans',
    tradingName: null,
    customerType: 'NEW',
    status: 'ACTIVE',
    industry: 'RESTAURANT',
    expectedAnnualVolume: 85000,
    lastContactDate: '2025-07-28',
    riskScore: 15,
    atRisk: false,
  },
  {
    id: '3',
    customerNumber: 'K-2025-003',
    companyName: 'Kantine Plus AG',
    tradingName: 'Kantine Plus',
    customerType: 'EXISTING',
    status: 'AT_RISK',
    industry: 'CANTEEN',
    expectedAnnualVolume: 220000,
    lastContactDate: '2025-06-15',
    riskScore: 75,
    atRisk: true,
  },
  {
    id: '4',
    customerNumber: 'K-2025-004',
    companyName: 'Hotel Vier Jahreszeiten',
    tradingName: null,
    customerType: 'EXISTING',
    status: 'ACTIVE',
    industry: 'HOTEL',
    expectedAnnualVolume: 180000,
    lastContactDate: '2025-07-30',
    riskScore: 35,
    atRisk: false,
  },
  {
    id: '5',
    customerNumber: 'K-2025-005',
    companyName: 'Café am Markt',
    tradingName: 'Marktcafé',
    customerType: 'NEW',
    status: 'INACTIVE',
    industry: 'CAFE_BAR',
    expectedAnnualVolume: 45000,
    lastContactDate: '2025-05-20',
    riskScore: 90,
    atRisk: true,
  },
];

export const handlers = [
  // Sales Cockpit API endpoints
  http.get('http://localhost:8080/api/sales-cockpit/dashboard/dev', () => {
    return HttpResponse.json({
      totalLeads: 3,
      totalCustomers: 5,
      monthlyRevenue: 25000,
      conversionRate: 15.2,
    });
  }),

  // Customer API endpoints
  http.get('http://localhost:8080/api/customers', ({ request }) => {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') || '0');
    const size = parseInt(url.searchParams.get('size') || '20');
    const sort = url.searchParams.get('sort') || 'companyName';

    // Simple sorting
    const sortedCustomers = [...mockCustomers].sort((a, b) => {
      if (sort === 'customerNumber') {
        return a.customerNumber.localeCompare(b.customerNumber);
      } else if (sort === 'status') {
        return a.status.localeCompare(b.status);
      } else if (sort === 'lastContactDate') {
        return new Date(b.lastContactDate).getTime() - new Date(a.lastContactDate).getTime();
      }
      return a.companyName.localeCompare(b.companyName);
    });

    // Pagination
    const start = page * size;
    const end = start + size;
    const paginatedCustomers = sortedCustomers.slice(start, end);

    return HttpResponse.json({
      content: paginatedCustomers,
      page: page,
      size: size,
      totalElements: mockCustomers.length,
      totalPages: Math.ceil(mockCustomers.length / size),
      first: page === 0,
      last: page === Math.ceil(mockCustomers.length / size) - 1,
    });
  }),

  // Customer search endpoint
  http.post('http://localhost:8080/api/customers/search', () => {
    return HttpResponse.json({
      content: mockCustomers.slice(0, 3), // Return first 3 customers
      page: 0,
      size: 50,
      totalElements: mockCustomers.length,
      totalPages: 1,
      first: true,
      last: true,
    });
  }),

  // Sprint 2.1.6: Lead API endpoints use real backend
  // No mock handlers needed - MSW configured with 'bypass' for unhandled requests
];
