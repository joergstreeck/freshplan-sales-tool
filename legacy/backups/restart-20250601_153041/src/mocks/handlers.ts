/**
 * MSW Request Handlers for API Mocking
 */

import { http, HttpResponse } from 'msw';

// Monday.com API handlers
export const mondayHandlers = [
  // Create item in Monday board
  http.post('https://api.monday.com/v2', async ({ request }) => {
    const body = await request.json() as { query: string };
    
    if (body.query.includes('create_item')) {
      return HttpResponse.json({
        data: {
          create_item: {
            id: '123456',
            name: 'Test Customer',
            created_at: new Date().toISOString(),
            column_values: []
          }
        }
      });
    }
    
    if (body.query.includes('boards')) {
      return HttpResponse.json({
        data: {
          boards: [{
            id: 'test-board-id',
            name: 'Test Board',
            columns: [
              { id: 'name', title: 'Name' },
              { id: 'email', title: 'Email' },
              { id: 'discount', title: 'Discount' }
            ]
          }]
        }
      });
    }
    
    return HttpResponse.json({ data: {} });
  }),
];

// Email API handlers
export const emailHandlers = [
  // Send email
  http.post('/api/email/send', async ({ request }) => {
    const body = await request.json() as {
      to: string;
      subject: string;
      body: string;
      attachments?: any[];
    };
    
    // Simulate successful email send
    return HttpResponse.json({
      success: true,
      messageId: `msg-${Date.now()}`,
      timestamp: new Date().toISOString(),
      recipient: body.to
    });
  }),
  
  // Validate SMTP settings
  http.post('/api/email/validate', async ({ request }) => {
    const body = await request.json() as {
      smtpServer: string;
      smtpEmail: string;
      smtpPassword: string;
    };
    
    // Simulate validation
    if (!body.smtpServer || !body.smtpEmail || !body.smtpPassword) {
      return HttpResponse.json({
        success: false,
        error: 'Missing required SMTP configuration'
      }, { status: 400 });
    }
    
    return HttpResponse.json({
      success: true,
      message: 'SMTP configuration is valid'
    });
  }),
];

// Xentral API handlers
export const xentralHandlers = [
  // Create customer
  http.post('*/api/v1/adressen', async ({ request }) => {
    const body = await request.json() as any;
    
    return HttpResponse.json({
      success: true,
      id: 12345,
      data: {
        id: 12345,
        kundennummer: 'CUST-12345',
        name: body.name || 'Test Customer',
        email: body.email || 'test@example.com',
        created_at: new Date().toISOString()
      }
    });
  }),
  
  // Create offer
  http.post('*/api/v1/belege/angebote', async ({ request }) => {
    const body = await request.json() as any;
    
    return HttpResponse.json({
      success: true,
      id: 67890,
      data: {
        id: 67890,
        belegnr: 'OFFER-67890',
        kunde: body.adresse || 12345,
        status: 'angelegt',
        summe: body.summe || 10000,
        created_at: new Date().toISOString()
      }
    });
  }),
  
  // Get customer
  http.get('*/api/v1/adressen/:id', ({ params }) => {
    const { id } = params;
    
    return HttpResponse.json({
      success: true,
      data: {
        id: Number(id),
        kundennummer: `CUST-${id}`,
        name: 'Test Customer',
        email: 'test@example.com',
        strasse: 'Test Street 123',
        plz: '12345',
        ort: 'Test City'
      }
    });
  }),
];

// Combined handlers
export const handlers = [
  ...mondayHandlers,
  ...emailHandlers,
  ...xentralHandlers,
  
  // Generic health check
  http.get('/api/health', () => {
    return HttpResponse.json({
      status: 'healthy',
      timestamp: new Date().toISOString()
    });
  }),
];

// Error scenario handlers for testing
export const errorHandlers = {
  monday: [
    http.post('https://api.monday.com/v2', () => {
      return HttpResponse.json({
        errors: [{
          message: 'Invalid API token',
          extensions: { code: 'UNAUTHENTICATED' }
        }]
      }, { status: 401 });
    }),
  ],
  
  email: [
    http.post('/api/email/send', () => {
      return HttpResponse.json({
        success: false,
        error: 'SMTP connection failed'
      }, { status: 500 });
    }),
  ],
  
  xentral: [
    http.post('*/api/v1/adressen', () => {
      return HttpResponse.json({
        success: false,
        error: 'Invalid API key'
      }, { status: 403 });
    }),
  ],
};

// Delay handlers for testing loading states
export const delayHandlers = {
  monday: [
    http.post('https://api.monday.com/v2', async () => {
      await new Promise(resolve => setTimeout(resolve, 2000));
      return HttpResponse.json({
        data: { create_item: { id: '123456' } }
      });
    }),
  ],
  
  email: [
    http.post('/api/email/send', async () => {
      await new Promise(resolve => setTimeout(resolve, 1500));
      return HttpResponse.json({
        success: true,
        messageId: 'delayed-msg'
      });
    }),
  ],
};