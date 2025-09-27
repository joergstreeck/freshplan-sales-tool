import { http, HttpResponse } from 'msw';
// import { calculatorHandlers } from './calculatorHandlers';

// Mock lead data
const mockLeads = [
  {
    id: '1',
    name: 'Demo Restaurant Berlin',
    email: 'info@demo-restaurant-berlin.de',
    createdAt: '2025-09-25T10:00:00Z',
  },
  {
    id: '2',
    name: 'FoodTruck München',
    email: 'kontakt@foodtruck-muenchen.de',
    createdAt: '2025-09-26T14:30:00Z',
  },
  {
    id: '3',
    name: 'Café Sonnenschein',
    email: null,
    createdAt: '2025-09-27T09:15:00Z',
  },
];

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
  // Health/Ping - verhindert MSW-Passthrough-Errors im Dev
  http.get('http://localhost:8080/api/ping', () => {
    return HttpResponse.json({
      message: 'pong',
      timestamp: new Date().toISOString(),
      user: 'mock-user',
    });
  }),

  // Sales Cockpit API endpoints
  http.get('http://localhost:8080/api/sales-cockpit/dashboard/dev', () => {
    return HttpResponse.json({
      totalLeads: 3,
      totalCustomers: 5,
      monthlyRevenue: 25000,
      conversionRate: 15.2
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
  http.post('http://localhost:8080/api/customers/search', ({ request }) => {
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

  // Lead API endpoints
  http.get('http://localhost:8080/api/leads', () => {
    return HttpResponse.json(mockLeads);
  }),

  http.post('http://localhost:8080/api/leads', async ({ request }) => {
    const newLead = await request.json() as { name: string; email?: string };

    // Validate required fields
    if (!newLead.name?.trim()) {
      return HttpResponse.json(
        {
          title: 'Validation Failed',
          status: 400,
          detail: 'Name is required',
          errors: {
            name: ['Name ist erforderlich']
          }
        },
        { status: 400 }
      );
    }

    // Check for duplicate email (case-insensitive)
    const norm = (s?: string | null) => (s ?? '').trim().toLowerCase();
    const isDup = newLead.email && mockLeads.some(l => norm(l.email) === norm(newLead.email));
    if (isDup) {
      return HttpResponse.json(
        {
          title: 'Duplicate lead',
          status: 409,
          detail: 'Lead mit dieser E-Mail existiert bereits.',
          errors: { email: ['E-Mail ist bereits vergeben'] }
        },
        { status: 409 }
      );
    }

    // Create new lead
    const lead = {
      id: String(Date.now()), // Simple ID generation
      name: newLead.name.trim(),
      email: newLead.email?.trim() || null,
      createdAt: new Date().toISOString(),
    };

    mockLeads.push(lead);
    return HttpResponse.json(lead, { status: 201 });
  }),

  // Calculator API handlers - DEAKTIVIERT, verwende echtes Backend
  // ...calculatorHandlers,
];
