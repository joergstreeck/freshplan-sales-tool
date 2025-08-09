/**
 * MSW Mock Server Setup für FC-005 Integration Tests
 *
 * ZWECK: Simuliert Backend-APIs für realistische Integration Tests
 * PHILOSOPHIE: Flexibel konfigurierbar für verschiedene Test-Szenarien
 */

import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import type { FieldDefinition } from '../../types/field.types';

// Mock Field Definitions für Tests
const mockFieldDefinitions: FieldDefinition[] = [
  {
    id: '1',
    key: 'companyName',
    label: 'Firmenname',
    fieldType: 'text',
    entityType: 'customer',
    required: true,
    sortOrder: 1,
    industryFilter: null,
    validationRules: {
      minLength: 2,
      maxLength: 100,
    },
  },
  {
    id: '2',
    key: 'industry',
    label: 'Branche',
    fieldType: 'select',
    entityType: 'customer',
    required: true,
    sortOrder: 2,
    industryFilter: null,
    options: [
      { value: 'hotel', label: 'Hotel & Gastronomie' },
      { value: 'office', label: 'Büro & Verwaltung' },
      { value: 'healthcare', label: 'Gesundheitswesen' },
    ],
  },
  {
    id: '3',
    key: 'chainCustomer',
    label: 'Kettenkunde',
    fieldType: 'select',
    entityType: 'customer',
    required: true,
    sortOrder: 3,
    industryFilter: null,
    options: [
      { value: 'ja', label: 'Ja' },
      { value: 'nein', label: 'Nein' },
    ],
    triggerWizardStep: {
      step: 'locations',
      when: 'ja',
    },
  },
  {
    id: '4',
    key: 'hotelStars',
    label: 'Hotel Sterne',
    fieldType: 'select',
    entityType: 'customer',
    required: false,
    sortOrder: 4,
    industryFilter: 'hotel',
    options: [
      { value: '3', label: '3 Sterne' },
      { value: '4', label: '4 Sterne' },
      { value: '5', label: '5 Sterne' },
    ],
  },
];

// Default Mock Handlers
const defaultHandlers = [
  // Field Definitions API
  http.get('/api/field-definitions', ({ request }) => {
    const url = new URL(request.url);
    const entityType = url.searchParams.get('entityType');
    const industry = url.searchParams.get('industry');

    let filteredFields = mockFieldDefinitions;

    if (entityType) {
      filteredFields = filteredFields.filter(field => field.entityType === entityType);
    }

    if (industry) {
      filteredFields = filteredFields.filter(
        field => !field.industryFilter || field.industryFilter === industry
      );
    }

    return HttpResponse.json({
      success: true,
      data: filteredFields,
    });
  }),

  // Customer Draft API
  http.post('/api/customers/draft', async ({ request }) => {
    const body = (await request.json()) as any;

    // Simuliere verschiedene Erfolgs- und Fehler-Szenarien
    if (body.customerData?.companyName === 'NETWORK_ERROR') {
      return HttpResponse.error();
    }

    if (body.customerData?.companyName === 'SERVER_ERROR') {
      return HttpResponse.json({ success: false, error: 'Internal Server Error' }, { status: 500 });
    }

    if (body.customerData?.companyName === 'VALIDATION_ERROR') {
      return HttpResponse.json(
        {
          success: false,
          error: 'Validation failed',
          validationErrors: {
            companyName: ['Firmenname ist zu kurz'],
          },
        },
        { status: 400 }
      );
    }

    // Erfolgreiche Antwort
    return HttpResponse.json({
      success: true,
      data: {
        draftId: 'draft-123',
        savedAt: new Date().toISOString(),
        customerData: body.customerData,
        locations: body.locations || [],
        detailedLocations: body.detailedLocations || [],
      },
    });
  }),

  // Customer Draft Loading API
  http.get('/api/customers/draft/:draftId', ({ params }) => {
    const { draftId } = params;

    if (draftId === 'not-found') {
      return HttpResponse.json({ success: false, error: 'Draft not found' }, { status: 404 });
    }

    return HttpResponse.json({
      success: true,
      data: {
        draftId,
        savedAt: '2025-07-27T03:00:00Z',
        customerData: {
          companyName: 'Test GmbH',
          industry: 'hotel',
          chainCustomer: 'ja',
        },
        locations: [
          { id: 'loc-1', name: 'Berlin' },
          { id: 'loc-2', name: 'Hamburg' },
        ],
        detailedLocations: [
          {
            id: 'detail-1',
            locationId: 'loc-1',
            address: 'Berliner Str. 123',
            contactPerson: 'Max Mustermann',
            phone: '+49 30 123456',
          },
        ],
      },
    });
  }),

  // Final Customer Creation API
  http.post('/api/customers', async ({ request }) => {
    const body = (await request.json()) as any;

    // Simuliere Validierungsfehler
    if (!body.customerData?.companyName) {
      return HttpResponse.json(
        {
          success: false,
          error: 'Validation failed',
          validationErrors: {
            companyName: ['Firmenname ist erforderlich'],
          },
        },
        { status: 400 }
      );
    }

    // Erfolgreiche Erstellung
    return HttpResponse.json({
      success: true,
      data: {
        id: 'customer-456',
        customerId: 'CUST-2025-001',
        createdAt: new Date().toISOString(),
        ...body,
      },
    });
  }),

  // Health Check (für Connection Tests)
  http.get('/api/health', () => {
    return HttpResponse.json({
      status: 'ok',
      timestamp: new Date().toISOString(),
    });
  }),
];

// Mock Server Setup
export const mockServer = setupServer(...defaultHandlers);

// Helper Functions für Test Configuration
export const configureMockServer = {
  // Simuliere Netzwerk-Latenz
  withDelay: (ms: number) => {
    mockServer.use(
      http.all('*', async ({ request }) => {
        await new Promise(resolve => setTimeout(resolve, ms));
        return new Response(null, { status: 500 }); // Fallback
      })
    );
  },

  // Simuliere Rate Limiting
  withRateLimit: () => {
    let requestCount = 0;
    mockServer.use(
      http.all('/api/*', ({ request }) => {
        requestCount++;
        if (requestCount > 5) {
          return HttpResponse.json({ error: 'Rate limit exceeded' }, { status: 429 });
        }
        return new Response(null, { status: 500 }); // Fallback
      })
    );
  },

  // Simuliere verschiedene Field Catalog Responses
  withCustomFieldCatalog: (fields: FieldDefinition[]) => {
    mockServer.use(
      http.get('/api/field-definitions', () => {
        return HttpResponse.json({
          success: true,
          data: fields,
        });
      })
    );
  },

  // Simuliere temporäre Server-Probleme
  withTemporaryServerError: (duration: number) => {
    const errorStartTime = Date.now();
    mockServer.use(
      http.all('/api/*', () => {
        if (Date.now() - errorStartTime < duration) {
          return HttpResponse.json({ error: 'Temporary server maintenance' }, { status: 503 });
        }
        return new Response(null, { status: 500 }); // Fallback
      })
    );
  },
};

// Test Data Helpers
export const testData = {
  validCustomerData: {
    companyName: 'Test Hotel GmbH',
    industry: 'hotel',
    chainCustomer: 'ja',
    hotelStars: '4',
  },

  validLocations: [
    { id: 'loc-1', name: 'Berlin Mitte' },
    { id: 'loc-2', name: 'Hamburg Altona' },
  ],

  validDetailedLocations: [
    {
      id: 'detail-1',
      locationId: 'loc-1',
      address: 'Berliner Str. 123, 10179 Berlin',
      contactPerson: 'Anna Schmidt',
      phone: '+49 30 1234567',
      email: 'anna.schmidt@test-hotel.de',
    },
  ],

  invalidCustomerData: {
    companyName: '', // Empty required field
    industry: 'invalid-industry',
  },
};

export default mockServer;
